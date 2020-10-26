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
 * Führt eine Optimierung durch, in der bei allen Kontrollvariablen der
 * Reihe nach geprüft wird, ob eine Veränderung in die eine oder andere Richtung
 * zu einer Verbesserung führt. Pro Schritt wird jede Kontrollvariable dabei
 * jeweils so weit bewegt, wie dies sinnvoll ist.<br>
 * Dies ist nur ein Kernel, die weitere Verarbeitung wird über {@link OptimizerSerialStepMax} durchgeführt.
 * @author Alexander Herzog
 * @see OptimizerSerialStepMax
 */
public class OptimizerSerialKernelStepMax extends OptimizerSerialKernelBase {
	/** Aktueller Status der Optimierung */
	private enum Status {
		/** Erster Simulationslauf insgesamt */
		STATUS_START,
		/** Erster Schritt für eine Variable */
		STATUS_INITIAL_VARIABLE_RUN,
		/** Änderung der Variable in Richtung 1 */
		STATUS_CHANGE_DIRECTION_1,
		/** Änderung der Variable in Richtung 2 */
		STATUS_CHANGE_DIRECTION_2
	}

	/** Änderungsgeschwindigkeit der Variablenwerte pro Rund */
	private double[] changeSpeed;
	/** Aktueller Status der Optimierung */
	private Status currentStatus;
	/** Aktuell in Bearbeitung befindliche Kontrollvariable */
	private int currentControlIndex;
	/** Letzte geänderte Kontrollvariable */
	private int lastControlChangeIndex;
	/** Letzt Runde in der eine Kontrollvariable geändert wurde */
	private int lastControlChangeRound;
	/** Letzte Änderungrichtung */
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
	 * Liefert die Änderungsrate für die aktuelle Runde.
	 * @return	Änderungsrate für die aktuelle Runde
	 */
	private double getChangeSpeed() {
		return changeSpeed[Math.min(changeSpeed.length-1,round-1)];
	}

	/**
	 * Erste Änderung der aktuellen Kontrollvariable
	 * @return	Gibt das geänderte Modell zurück oder <code>null</code>, wenn die aktuelle Variable nicht in Richtung 1 geändert werden kann.
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
	 * Ändert die aktuelle Kontrollvariable zum wiederholten Mal in die aktuelle Richtung
	 * @return	Gibt das geänderte Modell zurück oder <code>null</code>, wenn die aktuelle Variable nicht weiter in die aktuelle Richtung geändert werden kann.
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
	 * Prüft, ob die aktuellen Werte der Kontrollvariablen einen neuen,
	 * bisher noch nicht untersuchten Zustand darstellen, speichert diesen
	 * dann ggf. in der Liste der besuchten Zustände und stellt
	 * <code>lastControlChangeIndex</code> und <code>lastControlChangeRound</code>
	 * auf die aktuellen Werte, um festzuhalten, wann das Modell zuletzt erfolgreich
	 * verändert wurde.
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
	 * Erstellt das Modell für den nächsten Optimierungsschritt
	 * @param lastChangeWasImprovement	Gibt an, ob der zuletzt ausgeführte Schritt eine Verbesserung erbracht hat.
	 * @return	Nächstes Modell oder <code>null</code>, wenn die Optimierung beendet werden soll.
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

		/* Initialer Lauf für eine Kontrollvariable abgeschlossen */
		if (currentStatus==Status.STATUS_INITIAL_VARIABLE_RUN) {
			currentStatus=Status.STATUS_CHANGE_DIRECTION_1;
			return initialControlVariableChange();
		}

		/* Variable wurde in Richtung 1 geändert */
		if (currentStatus==Status.STATUS_CHANGE_DIRECTION_1) {
			EditModel model=null;
			if (lastChangeWasImprovement) {
				/* Weiter in diese Richtung */
				countChange(); /* Änderung war Erfolg */
				model=continuedControlVariableChange();
				if (model!=null) return model; /* Evtl. kann die aktuelle Kontrollvariable nicht weiter in diese Richtung geeändert werden, dann ist modell==null. */
			} else {
				/* Andere Richtung probieren */
				controlValues[currentControlIndex]=lastOldValue; /* Letzte Änderung rückgängig machen */
				currentStatus=Status.STATUS_CHANGE_DIRECTION_2;
				lastControlChangeDirection=-lastControlChangeDirection;
				model=continuedControlVariableChange();
				if (model!=null) return model; /* Evtl. kann die aktuelle Kontrollvariable nicht weiter in diese Richtung geeändert werden, dann ist modell==null. */
			}
			/* Weiter mit nächster Variable */
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

		/* Variable wurde in Richtung 2 geändert */
		if (currentStatus==Status.STATUS_CHANGE_DIRECTION_2) {
			EditModel model=null;
			if (lastChangeWasImprovement) {
				/* Weiter in diese Richtung */
				countChange(); /* Änderung war Erfolg */
				model=continuedControlVariableChange();
				if (model!=null) return model; /* Evtl. kann die aktuelle Kontrollvariable nicht weiter in diese Richtung geeändert werden, dann ist modell==null. */
			} else {
				controlValues[currentControlIndex]=lastOldValue; /* Letzte Änderung rückgängig machen */
			}
			/* Weiter mit nächster Variable */
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