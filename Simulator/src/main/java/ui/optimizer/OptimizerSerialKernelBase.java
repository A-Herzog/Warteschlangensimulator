/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.optimizer;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.ModelChanger;
import ui.optimizer.OptimizerSetup.ControlVariable;

/**
 * Diese Klasse stellt die Basis für alle seriell arbeitenden Optimierungskernel dar.
 * Sie stellt Basisfunktionen, die alle seriellen Kernel benötigen, zur Verfügung.
 * @author Alexander Herzog
 */
public abstract class OptimizerSerialKernelBase extends OptimizerKernelBase {
	private boolean isFirstStep;
	private double previousTargetValue;

	/**
	 * Aktuelle Werte der Kontrollvariablen
	 */
	protected final double[] controlValues;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Zu verwendende Optimierereinstellungen
	 * @param startModel	Ausgangs-Editor-Modell
	 */
	public OptimizerSerialKernelBase(final OptimizerSetup setup, final EditModel startModel) {
		super(setup,startModel);
		controlValues=getInitialControlVariables();
		isFirstStep=true;
	}

	/**
	 * Erstellt das Modell für den nächsten Optimierungsschritt.<br>
	 * @param lastTargetValue	Wert der Zielgröße im letzten Optimierungsschritt
	 * @param simulationWasEmergencyStopped	Gibt an, ob die Simulation im letzten Optimierungsschritt durch den Simulator selbst abgebrochen wurde
	 * @return	Nächstes zu simulierendes Modell oder <code>null</code> wenn die Optimierung abgebrochen werden soll.
	 */
	public final EditModel setupNextStep(final double lastTargetValue, final boolean simulationWasEmergencyStopped) {
		clearMessages();

		if (simulationWasEmergencyStopped) {
			return setupNextStepIntern(false);
		}

		boolean lastChangeWasImprovement=false;
		if (!isFirstStep) {
			switch (setup.targetDirection) {
			case -1:
				lastChangeWasImprovement=(lastTargetValue<previousTargetValue);
				break;
			case 0:
				double oldDelta=0;
				if (previousTargetValue>setup.targetRangeMax) oldDelta=Math.abs(setup.targetRangeMax-previousTargetValue);
				if (previousTargetValue<setup.targetRangeMin) oldDelta=Math.abs(setup.targetRangeMin-previousTargetValue);
				double newDelta=0;
				if (lastTargetValue>setup.targetRangeMax) newDelta=Math.abs(setup.targetRangeMax-lastTargetValue);
				if (lastTargetValue<setup.targetRangeMin) newDelta=Math.abs(setup.targetRangeMin-lastTargetValue);
				lastChangeWasImprovement=(newDelta<oldDelta);
				break;
			case 1:
				lastChangeWasImprovement=(lastTargetValue>previousTargetValue);
				break;
			}
		}
		previousTargetValue=lastTargetValue;
		isFirstStep=false;

		return setupNextStepIntern(lastChangeWasImprovement);
	}

	/**
	 * Erstellt das Modell für den nächsten Optimierungsschritt.<br>
	 * Diese Methode muss von den abgeleiteten Optimierungs-Kerneln überschrieben werden,
	 * um konkrete Optimierungsmethoden zu implementieren.
	 * @param lastChangeWasImprovement	Gibt an, ob der letzte Optimierungsschritt eine Verbesserung darstellte
	 * @return	Nächstes zu simulierendes Modell oder <code>null</code> wenn die Optimierung abgebrochen werden soll.
	 */
	protected abstract EditModel setupNextStepIntern(final boolean lastChangeWasImprovement);

	/**
	 * Liefert das Modell in seiner aktuellen (zuvor veränderten) Form zurück, ohne weitere Veränderungen an den Variablen vorzunehmen
	 * @param currentControlIndex	0-basierender Index, um den es gerade geht (oder -1 für Startfall)
	 * @return	Gibt das Modell zurück oder <code>null</code> wenn irgendetwas fundamental schief gelaufen ist und die Optimierung abgebrochen werden muss.
	 */
	protected final EditModel getUnchangedModel(int currentControlIndex) {
		Object obj=generateModel(controlValues);
		if (obj instanceof String) {
			addMessage((String)obj);
			return null;
		}

		if (currentControlIndex>=0) {
			addMessage(String.format(Language.tr("Optimizer.ControlVariableValue"),currentControlIndex+1,NumberTools.formatNumber(controlValues[currentControlIndex])));
		} else {
			addMessage(Language.tr("Optimizer.SimulationOfTheInitialModel"));
			((EditModel)obj).description=Language.tr("Optimizer.SimulationOfTheInitialModel.Info")+"\n\n"+((EditModel)obj).description;
		}
		return (EditModel)obj;
	}

	/**
	 * Erhöht den Wert der aktuellen Kontrollvariable
	 * @param index	Index der zu ändernden Kontrollvariable
	 * @param speed	Gibt an, die stark der Wert der Variable erhöht werden soll (0=keine Änderung, 1=auf Maximalwert stellen)
	 * @return	Gibt <code>true</code> zurück, wenn der Wert der Variable erhöht werden konnte
	 */
	protected final boolean changeUp(final int index, double speed) {
		final ControlVariable controlVariable=setup.controlVariables.get(index);

		if (controlValues[index]==controlVariable.rangeTo) return false;
		final double oldValue=controlValues[index];

		while (true) {

			double newValue=(1-speed)*controlValues[index]+speed*controlVariable.rangeTo;
			if (controlVariable.integerValue || controlVariable.mode==ModelChanger.Mode.MODE_RESOURCE) newValue=Math.round(newValue);
			if (Math.abs(newValue-controlValues[index])<1E-8) {
				if (newValue+1>controlVariable.rangeTo) return false;
				newValue++;
			}

			controlValues[index]=newValue;
			if (!controlValuesValide(controlValues)) {
				controlValues[index]=oldValue;
				speed=speed/2;
				if (speed<0.001) return false;
			} else {
				return true;
			}
		}
	}

	/**
	 * Verringert den Wert der aktuellen Kontrollvariable
	 * @param index	Index der zu ändernden Kontrollvariable
	 * @param speed	Gibt an, die stark der Wert der Variable verringert werden soll (0=keine Änderung, 1=auf Minimalwert stellen)
	 * @return	Gibt <code>true</code> zurück, wenn der Wert der Variable verringert werden konnte
	 */
	protected final boolean changeDown(final int index, double speed) {
		final ControlVariable controlVariable=setup.controlVariables.get(index);

		if (controlValues[index]==controlVariable.rangeFrom) return false;
		final double oldValue=controlValues[index];

		while (true) {

			double newValue=(1-speed)*controlValues[index]+speed*controlVariable.rangeFrom;
			if (controlVariable.integerValue || controlVariable.mode==ModelChanger.Mode.MODE_RESOURCE) newValue=Math.round(newValue);
			if (Math.abs(newValue-controlValues[index])<1E-8) {
				if (newValue-1<controlVariable.rangeFrom) return false;
				newValue--;
			}

			controlValues[index]=newValue;
			if (!controlValuesValide(controlValues)) {
				controlValues[index]=oldValue;
				speed=speed/2;
				if (speed<0.001) return false;
			} else {
				return true;
			}
		}
	}
}