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
 * jeweils nur einen Schritt in eine Richtung bewegt, dann ist erst die
 * n�chste Variable dran.<br>
 * Dies ist nur ein Kernel, die weitere Verarbeitung wird �ber {@link OptimizerSerialStepWise} durchgef�hrt.
 * @author Alexander Herzog
 * @see OptimizerSerialStepWise
 */
public class OptimizerSerialKernelStepWise extends OptimizerSerialKernelBase {
	/** Aktueller Status der Optimierung */
	private enum Status {
		/** Erster Simulationslauf insgesamt */
		STATUS_START,
		/** Erster Schritt f�r eine Variable */
		STATUS_VARIABLE_START,
		/** Ver�nderung einer Variable */
		STATUS_VARIABLE_CHANGED
	}

	/** �nderungsgeschwindigkeit der Variablenwerte pro Rund */
	private double[] changeSpeed;
	/** Aktueller Status der Optimierung */
	private Status currentStatus;
	/** Aktuell in Bearbeitung befindliche Kontrollvariable */
	private int currentControlIndex;
	/** Aktuelle Optimierungsrunde */
	private int round;
	/** Letzter Wert der aktuellen Kontrollvariable */
	private double lastOldValue;
	/** �nderungsrichtungen der Variable */
	private int[] lastChanceDir;
	/** Liste der bereits ausprobierten Kombinationen */
	private List<double[]> alreadyVisitedStates;
	/** Letzte ge�nderte Kontrollvariable */
	private int lastControlChangeIndex;
	/** Letzt Runde in der eine Kontrollvariable ge�ndert wurde */
	private int lastControlChangeRound;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Zu verwendende Optimierereinstellungen
	 * @param startModel	Ausgangs-Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 */
	public OptimizerSerialKernelStepWise(OptimizerSetup setup, EditModel startModel, final String editModelPath) {
		super(setup,startModel,editModelPath);

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
	 * Ver�ndert das Modell, in dem die jeweils aktuelle
	 * Kontrollvariable in die jeweils vorgegebene Richtung ver�ndert wird.
	 * Ist eine Ver�nderung in diese Richtung nicht (mehr) m�glich, so wird
	 * eine Ver�nderung in die andere Richtung versucht.
	 * @return	Neues Modell oder <code>null</code>, wenn keine Ver�nderung der aktuellen Varialbe m�glich ist.
	 */
	private EditModel changeModel() {
		lastOldValue=controlValues[currentControlIndex];

		if (lastChanceDir[currentControlIndex]==0 || lastChanceDir[currentControlIndex]==1) {
			/* Testen: nach oben */
			if (changeUp(currentControlIndex,getChangeSpeed())) {
				/* ja */
				lastChanceDir[currentControlIndex]=1;
				Object obj=generateModel(controlValues);
				if (obj instanceof String) {addMessage((String)obj); return null;}
				addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
				return (EditModel)obj;
			} else {
				/* nein. Testen: nach unten */
				if (changeDown(currentControlIndex,getChangeSpeed())) {
					/* ja */
					lastChanceDir[currentControlIndex]=-1;
					Object obj=generateModel(controlValues);
					if (obj instanceof String) {addMessage((String)obj); return null;}
					addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
					return (EditModel)obj;
				} else {
					/* nein. Nichts geht */
					return null;
				}
			}
		} else {
			/* Testen: nach unten */
			if (changeDown(currentControlIndex,getChangeSpeed())) {
				/* ja */
				lastChanceDir[currentControlIndex]=-1;
				Object obj=generateModel(controlValues);
				if (obj instanceof String) {addMessage((String)obj); return null;}
				addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
				return (EditModel)obj;
			} else {
				/* nein. Testen: nach oben */
				if (changeUp(currentControlIndex,getChangeSpeed())) {
					/* ja */
					lastChanceDir[currentControlIndex]=1;
					Object obj=generateModel(controlValues);
					if (obj instanceof String) {addMessage((String)obj); return null;}
					addMessage(String.format(Language.tr("Optimizer.ControlVariableChange"),currentControlIndex+1,NumberTools.formatNumber(lastOldValue),NumberTools.formatNumber(controlValues[currentControlIndex])));
					return (EditModel)obj;
				} else {
					/* nein. Nichts geht */
					return null;
				}
			}
		}
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
	protected EditModel setupNextStepIntern(boolean lastChangeWasImprovement) {
		/* Start der Optimierung */
		if (currentStatus==Status.STATUS_START) {
			currentStatus=Status.STATUS_VARIABLE_START;
			currentControlIndex=0;
			round=1;
			lastChanceDir=new int[controlValues.length];
			return getUnchangedModel(-1);
		}

		if (currentStatus==Status.STATUS_VARIABLE_START) {
			int count=0;
			EditModel newModel=changeModel();
			while (newModel==null && count<=controlValues.length) {
				currentControlIndex++;
				if (currentControlIndex>=controlValues.length) currentControlIndex=0;
				count++;
				newModel=changeModel();
			}
			currentStatus=Status.STATUS_VARIABLE_CHANGED;
			return newModel;
		}

		if (currentStatus==Status.STATUS_VARIABLE_CHANGED) {
			if (lastChangeWasImprovement) {
				countChange(); /* �nderung war Erfolg */
			} else {
				controlValues[currentControlIndex]=lastOldValue; /* Alten Zustand wiederherstellen */
				lastChanceDir[currentControlIndex]=-lastChanceDir[currentControlIndex]; /* n�chstes Mal �nderung in die andere Richtung */
				if (currentControlIndex==lastControlChangeIndex && lastControlChangeRound<round-1) {
					/* Zwei Runden lang nichts mehr passiert -> Ende */
					addMessage(Language.tr("Optimizer.FinishedDueToNoChanges"));
					return null;
				}
			}
			currentControlIndex++;
			if (currentControlIndex>=controlValues.length) {currentControlIndex=0; round++;}
			currentStatus=Status.STATUS_VARIABLE_START;
			return setupNextStepIntern(true);

		}

		return null; /* Fehler, hier sollten wir nie hin kommen, wenn currentStatus stets sinnvoll gesetzt wird. */
	}
}
