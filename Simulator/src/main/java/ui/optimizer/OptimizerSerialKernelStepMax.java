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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;

/**
 * F�hrt eine Optimierung durch, in der bei allen Kontrollvariablen der
 * Reihe nach gepr�ft wird, ob eine Ver�nderung in die eine oder andere Richtung
 * zu einer Verbesserung f�hrt. Pro Schritt wird jede Kontrollvariable dabei
 * jeweils so weit bewegt, wie dies sinnvoll ist.<br>
 * Dies ist nur ein Kernel, die weitere Verarbeitung wird �ber {@link OptimizerSerialStepMax} durchgef�hrt.
 * @author Alexander Herzog
 * @see OptimizerSerialStepMax
 */
public class OptimizerSerialKernelStepMax extends OptimizerSerialKernelBase {
	/** Aktueller Status der Optimierung */
	private enum Status {
		/** Erster Simulationslauf insgesamt */
		STATUS_START,
		/** Erster Schritt f�r eine Variable */
		STATUS_INITIAL_VARIABLE_RUN,
		/** �nderung der Variable in Richtung 1 */
		STATUS_CHANGE_DIRECTION_1,
		/** �nderung der Variable in Richtung 2 */
		STATUS_CHANGE_DIRECTION_2
	}

	/** �nderungsgeschwindigkeit der Variablenwerte pro Rund */
	private double[] changeSpeed;
	/** Aktueller Status der Optimierung */
	private Status currentStatus;
	/** Aktuell in Bearbeitung befindliche Kontrollvariable */
	private int currentControlIndex;
	/** Letzte ge�nderte Kontrollvariable */
	private int lastControlChangeIndex;
	/** Letzt Runde in der eine Kontrollvariable ge�ndert wurde */
	private int lastControlChangeRound;
	/** Letzte �nderungrichtung */
	private int lastControlChangeDirection;
	/** Letzter Wert der aktuellen Kontrollvariable */
	private double lastOldValue;
	/** Aktuelle Optimierungsrunde */
	private int round;
	/** Liste der bereits ausprobierten Kombinationen */
	private List<double[]> alreadyVisitedStates;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Zu verwendende Optimierereinstellungen
	 * @param startModel	Ausgangs-Editor-Modell
	 */
	public OptimizerSerialKernelStepMax(final OptimizerSetup setup, final EditModel startModel) {
		super(setup,startModel);

		alreadyVisitedStates=new ArrayList<>();
		currentStatus=Status.STATUS_START;

		changeSpeed=new double[4];
		changeSpeed[0]=Math.max(0.01,Math.min(0.95,setup.serialChangeSpeed1));
		changeSpeed[1]=Math.max(0.01,Math.min(changeSpeed[0],setup.serialChangeSpeed2));
		changeSpeed[2]=Math.max(0.01,Math.min(changeSpeed[1],setup.serialChangeSpeed3));
		changeSpeed[3]=Math.max(0.01,Math.min(changeSpeed[2],setup.serialChangeSpeed4));
	}

	/**
	 * Liefert die �nderungsrate f�r die aktuelle Runde.
	 * @return	�nderungsrate f�r die aktuelle Runde
	 */
	private double getChangeSpeed() {
		return changeSpeed[Math.min(changeSpeed.length-1,round-1)];
	}

	/**
	 * Erste �nderung der aktuellen Kontrollvariable
	 * @return	Gibt das ge�nderte Modell zur�ck oder <code>null</code>, wenn die aktuelle Variable nicht in Richtung 1 ge�ndert werden kann.
	 */
	private EditModel initialControlVariableChange() {
		lastOldValue=controlValues[currentControlIndex];

		if (changeUp(currentControlIndex,getChangeSpeed())) {
			lastControlChangeDirection=1;
			Object obj=generateModel(controlValues);
			if (obj instanceof String) {
				addMessage((String)obj);
				return null;
			}
			addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
			return (EditModel)obj;
		}

		if (changeDown(currentControlIndex,getChangeSpeed())) {
			lastControlChangeDirection=-1;
			Object obj=generateModel(controlValues);
			if (obj instanceof String) {
				addMessage((String)obj);
				return null;
			}
			addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
			return (EditModel)obj;
		}

		addMessage(String.format(Language.tr("Optimizer.Error.ControlVariableCannotBeChanged"),currentControlIndex+1));
		return null;
	}

	/**
	 * �ndert die aktuelle Kontrollvariable zum wiederholten Mal in die aktuelle Richtung
	 * @return	Gibt das ge�nderte Modell zur�ck oder <code>null</code>, wenn die aktuelle Variable nicht weiter in die aktuelle Richtung ge�ndert werden kann.
	 */
	private EditModel continuedControlVariableChange() {
		lastOldValue=controlValues[currentControlIndex];

		if (lastControlChangeDirection==1) {
			if (!changeUp(currentControlIndex,getChangeSpeed())) return null;
		} else {
			if (!changeDown(currentControlIndex,getChangeSpeed())) return null;
		}

		Object obj=generateModel(controlValues);
		if (obj instanceof String) {
			addMessage((String)obj);
			return null;
		}
		addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
		return (EditModel)obj;
	}

	/**
	 * Pr�ft, ob die aktuellen Werte der Kontrollvariablen einen neuen,
	 * bisher noch nicht untersuchten Zustand darstellen, speichert diesen
	 * dann ggf. in der Liste der besuchten Zust�nde und stellt
	 * <code>lastControlChangeIndex</code> und <code>lastControlChangeRound</code>
	 * auf die aktuellen Werte, um festzuhalten, wann das Modell zuletzt erfolgreich
	 * ver�ndert wurde.
	 */
	private void countChange() {
		for (double[] values: alreadyVisitedStates) {
			boolean equals=true;
			for (int i=0;i<values.length;i++) if (values[i]!=controlValues[i]) {equals=false; break;}
			if (equals) return; /* hatten wir schon */
		}

		alreadyVisitedStates.add(Arrays.copyOf(controlValues,controlValues.length));
		lastControlChangeIndex=currentControlIndex;
		lastControlChangeRound=round;
	}

	/**
	 * Erstellt das Modell f�r den n�chsten Optimierungsschritt
	 * @param lastChangeWasImprovement	Gibt an, ob der zuletzt ausgef�hrte Schritt eine Verbesserung erbracht hat.
	 * @return	N�chstes Modell oder <code>null</code>, wenn die Optimierung beendet werden soll.
	 */
	@Override
	public EditModel setupNextStepIntern(final boolean lastChangeWasImprovement) {
		/* Start der Optimierung */
		if (currentStatus==Status.STATUS_START) {
			currentStatus=Status.STATUS_INITIAL_VARIABLE_RUN;
			currentControlIndex=0;
			lastControlChangeIndex=0;
			lastControlChangeRound=1;
			round=1;
			return getUnchangedModel(-1);
		}

		/* Initialer Lauf f�r eine Kontrollvariable abgeschlossen */
		if (currentStatus==Status.STATUS_INITIAL_VARIABLE_RUN) {
			currentStatus=Status.STATUS_CHANGE_DIRECTION_1;
			return initialControlVariableChange();
		}

		/* Variable wurde in Richtung 1 ge�ndert */
		if (currentStatus==Status.STATUS_CHANGE_DIRECTION_1) {
			EditModel model=null;
			if (lastChangeWasImprovement) {
				/* Weiter in diese Richtung */
				countChange(); /* �nderung war Erfolg */
				model=continuedControlVariableChange();
				if (model!=null) return model; /* Evtl. kann die aktuelle Kontrollvariable nicht weiter in diese Richtung gee�ndert werden, dann ist modell==null. */
			} else {
				/* Andere Richtung probieren */
				controlValues[currentControlIndex]=lastOldValue; /* Letzte �nderung r�ckg�ngig machen */
				currentStatus=Status.STATUS_CHANGE_DIRECTION_2;
				lastControlChangeDirection=-lastControlChangeDirection;
				model=continuedControlVariableChange();
				if (model!=null) return model; /* Evtl. kann die aktuelle Kontrollvariable nicht weiter in diese Richtung gee�ndert werden, dann ist modell==null. */
			}
			/* Weiter mit n�chster Variable */
			currentStatus=Status.STATUS_INITIAL_VARIABLE_RUN;
			if (currentControlIndex==lastControlChangeIndex && lastControlChangeRound<round) {
				/* Eine ganze Runde lang nichts mehr passiert -> Ende */
				addMessage(Language.tr("Optimizer.FinishedDueToNoChanges"));
				return null;
			}
			currentControlIndex++;
			if (currentControlIndex>=controlValues.length) {currentControlIndex=0; round++;}
			return getUnchangedModel(currentControlIndex);
		}

		/* Variable wurde in Richtung 2 ge�ndert */
		if (currentStatus==Status.STATUS_CHANGE_DIRECTION_2) {
			EditModel model=null;
			if (lastChangeWasImprovement) {
				/* Weiter in diese Richtung */
				countChange(); /* �nderung war Erfolg */
				model=continuedControlVariableChange();
				if (model!=null) return model; /* Evtl. kann die aktuelle Kontrollvariable nicht weiter in diese Richtung gee�ndert werden, dann ist modell==null. */
			} else {
				controlValues[currentControlIndex]=lastOldValue; /* Letzte �nderung r�ckg�ngig machen */
			}
			/* Weiter mit n�chster Variable */
			currentStatus=Status.STATUS_INITIAL_VARIABLE_RUN;
			if (currentControlIndex==lastControlChangeIndex && lastControlChangeRound<round) {
				/* Eine ganze Runde lang nichts mehr passiert -> Ende */
				addMessage(Language.tr("Optimizer.FinishedDueToNoChanges"));
				return null;
			}
			currentControlIndex++;
			if (currentControlIndex>=controlValues.length) {currentControlIndex=0; round++;}
			return getUnchangedModel(currentControlIndex);
		}

		return null; /* Fehler, hier sollten wir nie hin kommen, wenn currentStatus stets sinnvoll gesetzt wird. */
	}
}