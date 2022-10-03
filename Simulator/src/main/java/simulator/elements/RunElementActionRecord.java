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
package simulator.elements;

import java.awt.Color;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.editmodel.EditModel;
import simulator.events.SystemChangeEvent;
import simulator.events.TimedActionEvent;
import simulator.logging.CallbackLoggerWithJS;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementActionRecord;
import ui.modeleditor.elements.ModelElementActionRecord.ActionType;
import ui.modeleditor.elements.ModelElementActionRecord.ConditionType;
import ui.modeleditor.elements.ModelElementActionRecord.ThresholdDirection;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementTank;
import ui.tools.SoundSystem;

/**
 * Diese Klasse stellt einen einzelnen Action-Datensatz dar
 * und wird innerhalb von {@link RunElementAction} und
 * {@link RunElementInteractiveButton} verwendet.
 * Die Klasse stellt die Laufzeitrepr�sentation der Editor-Klasse
 * {@link ModelElementActionRecord} dar.
 * @author Alexander Herzog
 * @see ModelElementActionRecord
 * @see RunElementAction
 * @see RunElementInteractiveButton
 */
public class RunElementActionRecord {
	/** Zugeh�riges Editor-Element */
	private final ModelElementActionRecord editRecord;
	/** ID der Station aus der das Element stammt */
	private final int stationID;

	/** Ursache des Datensatzes */
	private ModelElementActionRecord.ActionMode actionMode;

	/** Art der Bedingung, die die Aktion ausl�sen soll */
	private ModelElementActionRecord.ConditionType conditionType;
	/** Art der Aktion */
	private ModelElementActionRecord.ActionType actionType;

	/* Ausl�ser */

	/** Zeitpunkt f�r erste zeitgesteuerte Aktionsausl�sung */
	private long timeInitialMS;
	/** Abst�nde zwischen den zeitgesteuerten Aktionsausl�sungen */
	private long timeIntervalMS;
	/** Anzahl an zeitgesteuerten Aktionsausl�sungen */
	private int timeRepeatCount;

	/** Bedingung, die, wenn sie erf�llt ist, die Aktion ausl�st (im Modus {@link ConditionType#CONDITION_CONDITION}) */
	private String condition;
	/** Rechenobjekt zur Bedinung {@link #condition} */
	private ExpressionMultiEval conditionObj;
	/**  Minimaler Zeitabstand zwischen zwei Bedingungspr�fungen in MS (im Modus {@link ConditionType#CONDITION_CONDITION}) */
	private long conditionMinDistance;
	/** Schwellenwertausdruck, dessen �ber- oder Unterschreitung die Aktion ausl�sen soll (im Modus {@link ConditionType#CONDITION_THRESHOLD}) */
	private String thresholdExpression;
	/** Rechenobjekt zu dem Schwellenwert {@link #thresholdExpression} */
	private ExpressionCalc thresholdExpressionObj;
	/** Schwellenwert, dessen �ber- oder Unterschreitung die Aktion ausl�sen soll (im Modus {@link ConditionType#CONDITION_THRESHOLD}) */
	private double thresholdValue;
	/** Angabe, ob die Aktion beim �ber- oder Unterschreiten des Schwellenwertes ausgel�st werden soll (im Modus {@link ConditionType#CONDITION_THRESHOLD}) */
	private ThresholdDirection thresholdDirection;
	/** Signal das die Aktion ausl�sen soll (im Modus {@link ConditionType#CONDITION_SIGNAL}) */
	private String conditionSignalName;

	/* Auszuf�hrende Aktion */

	/** Index der Variable an die eine Zuweisung von {@link #assignExpressionObj} erfolgen soll (im Modus {@link ActionType#ACTION_ASSIGN}) */
	private int assignVariableIndex;
	/** Ausdruck f�r die Variablenzuweisung (im Modus {@link ActionType#ACTION_ASSIGN}) */
	private String assignExpression;
	/** Rechenausdruck f�r {@link #assignExpression} */
	private ExpressionCalc assignExpressionObj;
	/** Name des auszul�senden Signals (im Modus {@link ActionType#ACTION_SIGNAL}) */
	private String signalName;
	/** ID der Analogwertstation, an der ein Wert eingestellt werden soll (im Modus {@link ActionType#ACTION_ANALOG_VALUE}) */
	private int analogID;
	/** Datenelement der Analogwert-Station {@link #analogID} */
	private RunElementAnalogProcessingData analogData;
	/** Analogwert der eingestellt werden soll (im Modus {@link ActionType#ACTION_ANALOG_VALUE}) */
	private String analogValue;
	/** Rechenobjekt f�r {@link #analogValue} */
	private ExpressionCalc analogValueObj;
	/** Als Aktion Auszuf�hrender Javascript- oder Java-Code (im Modus {@link ActionType#ACTION_SCRIPT}) */
	private String script;
	/** Liefert die gew�hlte Sprache f�r das Skript in {@link #script} (im Modus {@link ActionType#ACTION_SCRIPT}) */
	private ModelElementActionRecord.ScriptMode scriptMode;
	/** Ausf�hrungssystem f�r Javascript-Code aus {@link #script} */
	private JSRunSimulationData jsRunner;
	/** Ausf�hrungssystem f�r Javas-Code aus {@link #script} */
	private DynamicRunner javaRunner;
	/** Abzuspielender Sound */
	private String sound;
	/** Maximaldauer des abzuspielenden Sounds */
	private int soundMaxSeconds;

	/** Zeitpunkt der letzten Ausl�sung des Ereignisses �ber die Bedingung im Modus ConditionType#CONDITION_CONDITION */
	private long lastConditionTrigger;
	/** Zeitpunkt der Pr�fung des Schwellenwertes im Modus {@link ConditionType#CONDITION_THRESHOLD} */
	private long lastThresholdCheckTime;
	/** Wert des Schwellenwertausdrucks bei der letzten Pr�fung ({@link #lastThresholdCheckTime}) */
	private double lastThresholdCheckValue;

	/**
	 * Konstruktor der Klasse
	 * @param editRecord	Zugeh�riges Editor-Element
	 * @param stationID	ID der Station aus der das Element stammt
	 * @see #build(EditModel, RunModel, boolean)
	 */
	public RunElementActionRecord(final ModelElementActionRecord editRecord, final int stationID) {
		actionMode=editRecord.getActionMode();
		this.editRecord=editRecord;
		this.stationID=stationID;
	}

	/**
	 * Copy-Konstruktor
	 * @param runRecord	Zu kopierendes Ausgangselement
	 */
	public RunElementActionRecord(final RunElementActionRecord runRecord) {
		actionMode=runRecord.actionMode;
		editRecord=runRecord.editRecord;
		stationID=runRecord.stationID;

		conditionType=runRecord.conditionType;
		actionType=runRecord.actionType;

		timeInitialMS=runRecord.timeInitialMS;
		timeIntervalMS=runRecord.timeIntervalMS;
		timeRepeatCount=runRecord.timeRepeatCount;

		condition=runRecord.condition;
		conditionMinDistance=runRecord.conditionMinDistance;
		thresholdExpression=runRecord.thresholdExpression;
		thresholdValue=runRecord.thresholdValue;
		thresholdDirection=runRecord.thresholdDirection;
		conditionSignalName=runRecord.conditionSignalName;

		assignVariableIndex=runRecord.assignVariableIndex;
		assignExpression=runRecord.assignExpression;
		signalName=runRecord.signalName;
		analogID=runRecord.analogID;
		analogValue=runRecord.analogValue;
		script=runRecord.script;
		scriptMode=runRecord.scriptMode;
		sound=runRecord.sound;
		soundMaxSeconds=runRecord.soundMaxSeconds;
	}

	/**
	 * Versucht den im Konstruktor �bergebenen Editor-Datensatz in dieses Objekt zu �berf�hren
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param testOnly	Wird hier <code>true</code> �bergeben, so werden externe Datenquellen nicht wirklich geladen
	 * @return	Liefert <code>null</code>, wenn die Daten korrekt verarbeitet werden konnten, und eine Zeichenkette im Fehlerfall
	 */
	public String build(final EditModel editModel, final RunModel runModel, final boolean testOnly) {
		final String testError=test();
		if (testError!=null) return testError;

		int error;

		if (actionMode==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			conditionType=editRecord.getConditionType();
			switch (conditionType) {
			case CONDITION_TIME:
				if (editRecord.getTimeInitial()<0) return String.format(Language.tr("Simulation.Creator.Action.InvalidInitialTime"),NumberTools.formatNumber(editRecord.getTimeInitial()));
				if (editRecord.getTimeRepeatCount()!=1) {
					if (editRecord.getTimeRepeat()<0.001) return String.format(Language.tr("Simulation.Creator.Action.InvalidRepeatTime"),NumberTools.formatNumber(editRecord.getTimeRepeat()));
				}
				timeInitialMS=Math.round(editRecord.getTimeInitial()*1000);
				timeIntervalMS=Math.max(1,Math.round(editRecord.getTimeRepeat()*1000));
				timeRepeatCount=(editRecord.getTimeRepeatCount()<=0)?-1:editRecord.getTimeRepeatCount();
				break;
			case CONDITION_CONDITION:
				condition=editRecord.getCondition();
				error=ExpressionMultiEval.check(condition,runModel.variableNames);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.Action.InvalidCondition"),condition,error+1);
				conditionMinDistance=Math.round(editRecord.getConditionMinDistance()*1000);
				break;
			case CONDITION_THRESHOLD:
				thresholdExpression=editRecord.getThresholdExpression();
				error=ExpressionCalc.check(thresholdExpression,runModel.variableNames);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.Action.InvalidThresholdExpression"),thresholdExpression,error+1);
				thresholdValue=editRecord.getThresholdValue();
				thresholdDirection=editRecord.getThresholdDirection();
				break;
			case CONDITION_SIGNAL:
				conditionSignalName=editRecord.getConditionSignal();
				break;
			}
		}

		/* Aktion */
		actionType=editRecord.getActionType();
		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			analogID=editRecord.getAnalogID();
			final ModelElement element=editModel.surface.getByIdIncludingSubModels(analogID);
			if (element==null) return String.format(Language.tr("Simulation.Creator.Action.AnalogNoElement"),analogID);
			if (!(element instanceof ModelElementAnalogValue) && !(element instanceof ModelElementTank)) return String.format(Language.tr("Simulation.Creator.Action.NotAnalogElement"),analogID);
			analogValue=editRecord.getAnalogValue();
			error=ExpressionCalc.check(analogValue,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.Action.InvalidAnalogValue"),analogValue,error+1);
			break;
		case ACTION_ASSIGN:
			int index=-1;
			for (int i=0;i<runModel.variableNames.length;i++) if (editRecord.getAssignVariable().equalsIgnoreCase(runModel.variableNames[i])) {index=i; break;}
			if (index<0) return String.format(Language.tr("Simulation.Creator.Action.InvalidVariableName"),editRecord.getAssignVariable());
			assignVariableIndex=index;
			assignExpression=editRecord.getAssignExpression();
			error=ExpressionCalc.check(assignExpression,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.Action.InvalidAssignValue"),assignExpression,error+1);
			break;
		case ACTION_SCRIPT:
			script=editRecord.getScript();
			scriptMode=editRecord.getScriptMode();
			if (scriptMode==ModelElementActionRecord.ScriptMode.Java && !testOnly) {
				final Object runner=DynamicFactory.getFactory().test(script,runModel.javaImports,true);
				if (runner instanceof String) return String.format(Language.tr("Simulation.Creator.ScriptError"),stationID)+"\n"+runner;
				javaRunner=(DynamicRunner)runner;
			}
			break;
		case ACTION_SIGNAL:
			signalName=editRecord.getSignalName();
			break;
		case ACTION_STOP:
			/* Keine weiteren Einstellungen. */
			break;
		case ACTION_SOUND:
			sound=editRecord.getSound();
			soundMaxSeconds=editRecord.getSoundMaxSeconds();
			break;
		}

		return null;
	}

	/**
	 * Pr�ft, ob der im Konstruktor �bergebene Editor-Datensatz in Ordnung ist
	 * @return	Liefert <code>null</code>, wenn die Daten korrekt verarbeitet werden konnten, und eine Zeichenkette im Fehlerfall
	 */
	public String test() {
		if (actionMode==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (editRecord.getConditionType()) {
			case CONDITION_TIME:
				if (editRecord.getTimeInitial()<0) return String.format(Language.tr("Simulation.Creator.Action.InvalidInitialTime"),NumberTools.formatNumber(editRecord.getTimeInitial()));
				if (editRecord.getTimeRepeatCount()!=1) {
					if (editRecord.getTimeRepeat()<0.001) return String.format(Language.tr("Simulation.Creator.Action.InvalidRepeatTime"),NumberTools.formatNumber(editRecord.getTimeRepeat()));
				}
				break;
			case CONDITION_CONDITION:
				if (editRecord.getConditionMinDistance()<=0) return String.format(Language.tr("Simulation.Creator.Action.InvalidConditionMinDistance"),NumberTools.formatNumber(editRecord.getConditionMinDistance()));
				break;
			case CONDITION_THRESHOLD:
				/* hier nichts zu pr�fen */
				break;
			case CONDITION_SIGNAL:
				/* hier nichts zu pr�fen */
				break;
			default:
				return Language.tr("Simulation.Creator.Action.ErrorUnknownTrigger");
			}
		}

		/* Aktion */
		switch (editRecord.getActionType()) {
		case ACTION_ANALOG_VALUE:
			/* hier nichts zu pr�fen */
			break;
		case ACTION_ASSIGN:
			if (editRecord.getAssignVariable().trim().isEmpty()) Language.tr("Simulation.Creator.Action.NoVariableName");
			break;
		case ACTION_SCRIPT:
			/* nicht vorzubereiten */
			break;
		case ACTION_SIGNAL:
			if (editRecord.getSignalName().trim().isEmpty()) Language.tr("Simulation.Creator.Action.NoSignalName");
			break;
		case ACTION_STOP:
			/* nichts vorzubereiten */
			break;
		case ACTION_SOUND:
			/* nichts vorzubereiten */
			break;
		default:
			return Language.tr("Simulation.Creator.Action.ErrorUnknownAction");
		}

		return null;
	}

	/**
	 * Diese Methode initialisiert die Laufzeit-abh�ngigen Daten und wird
	 * nach dem Start der Simulation, unmittelbar bevor dieses Objekt
	 * an die jeweilige Daten-Klasse der Station �bergeben wird, aufgerufen.
	 * @param simData	Simulationsdatenobjekt
	 * @param actionRecordIndex	Index dieses Datensatzes innerhalb des Action-Elements
	 */
	public void initRunData(final SimulationData simData, final int actionRecordIndex) {
		final String[] variableNames=simData.runModel.variableNames;

		if (actionMode==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_TIME:
				scheduleNextTimedAction(simData,timeInitialMS,actionRecordIndex);
				break;
			case CONDITION_CONDITION:
				conditionObj=new ExpressionMultiEval(variableNames);
				conditionObj.parse(condition);
				lastConditionTrigger=-1;
				break;
			case CONDITION_THRESHOLD:
				thresholdExpressionObj=new ExpressionCalc(variableNames);
				thresholdExpressionObj.parse(thresholdExpression);
				lastThresholdCheckTime=-1;
				break;
			case CONDITION_SIGNAL:
				break;
			}
		}

		/* Aktion */

		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			analogData=(RunElementAnalogProcessingData)simData.runModel.elementsFast[analogID].getData(simData);
			analogValueObj=new ExpressionCalc(variableNames);
			analogValueObj.parse(analogValue);
			break;
		case ACTION_ASSIGN:
			assignExpressionObj=new ExpressionCalc(variableNames);
			assignExpressionObj.parse(assignExpression);
			break;
		case ACTION_SCRIPT:
			switch (scriptMode) {
			case Javascript:
				jsRunner=new JSRunSimulationData(false,false);
				jsRunner.compile(script);
				break;
			case Java:
				if (javaRunner==null) {
					javaRunner=DynamicFactory.getFactory().load(script,simData.runModel.javaImports);
				} else {
					javaRunner=DynamicFactory.getFactory().load(javaRunner,simData.runModel.javaImports);
				}
				javaRunner.parameter.system=new SystemImpl(simData,stationID);
				break;
			}
			break;
		case ACTION_SIGNAL:
			/* nicht vorzubereiten */
			break;
		case ACTION_STOP:
			/* nicht vorzubereiten */
			break;
		case ACTION_SOUND:
			/* nicht vorzubereiten */
			break;
		}
	}

	/**
	 * Z�hlt, wie h�ufig wie Ereigniswarteschlange beim Aufruf von
	 * {@link #scheduleNextTimedAction(SimulationData, long, int)}
	 * vollst�ndig leer war. (Wenn die Ereigniswarteschlange stets
	 * leer ist, ist das ein Indiz, dass die Simulation eigentlich
	 * zu Ende ist nur nur durch die regelm��igen Aktion-Ereignisse
	 * unn�tig weiterl�uft.)
	 * @see #scheduleNextTimedAction(SimulationData, long, int)
	 */
	private int triggerIsOnlyEvent=0;

	/**
	 * Legt ein Ereignis zur zeitgesteuerten Ausf�hrung der Aktion an.
	 * @param simData	Simulationsdatenobjekt
	 * @param time	(Absoluter) Zeitpunkt zu dem das Ereignis ausgel�st werden soll
	 * @param actionRecordIndex	Index dieses Datensatzes innerhalb des Action-Elements
	 */
	private void scheduleNextTimedAction(final SimulationData simData, final long time, final int actionRecordIndex) {
		if (timeRepeatCount<0 && simData.eventManager.eventQueueLength()==0) {
			triggerIsOnlyEvent++;
			if (triggerIsOnlyEvent>1000) return;
		} else {
			triggerIsOnlyEvent=0;
		}

		final TimedActionEvent event=(TimedActionEvent)simData.getEvent(TimedActionEvent.class);
		event.init(time);
		event.actionStation=(RunElementAction)simData.runModel.elementsFast[stationID];
		event.actionIndex=actionRecordIndex;
		simData.eventManager.addEvent(event);
	}

	/**
	 * Pr�ft, ob sich die Simulationsdaten so ver�ndert haben, dass die Bedingung des Action-Datensatzes erf�llt ist.<br>
	 * Modus: Pr�fung einer Bedingung
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zur�ck, wenn die Bedingung f�r die Ausl�sung der Aktion erf�llt ist
	 * @see #checkTrigger(SimulationData, String)
	 */
	private boolean checkTriggerCondition(final SimulationData simData) {
		/* Mindestabstand */
		if (lastConditionTrigger>=0 && lastConditionTrigger<=simData.currentTime) {
			if (simData.currentTime<lastConditionTrigger+conditionMinDistance) return false; /* noch nicht genug Zeit verstrichen */
		}

		/* Bedingung pr�fen */
		simData.runData.setClientVariableValues(null);
		if (!conditionObj.eval(simData.runData.variableValues,simData,null)) return false;

		lastConditionTrigger=simData.currentTime;

		/* Bedingung erf�llt; Recheck-Event, ob die Bedingung immer noch erf�llt ist, anlegen */
		SystemChangeEvent.triggerEvent(simData,conditionMinDistance);

		return true;
	}

	/**
	 * Pr�ft, ob sich die Simulationsdaten so ver�ndert haben, dass die Bedingung des Action-Datensatzes erf�llt ist.<br>
	 * Modus: Pr�fung eines Schwellenwertes
	 * @param simData	Simulationsdatenobjekt
	 * @param stationLogName	Name der Station, an der die Pr�fung stattfindet
	 * @return	Gibt <code>true</code> zur�ck, wenn die Bedingung f�r die Ausl�sung der Aktion erf�llt ist
	 * @see #checkTrigger(SimulationData, String)
	 */
	private boolean checkTriggerThreshold(final SimulationData simData, final String stationLogName) {
		double newValue=0;
		try {
			newValue=thresholdExpressionObj.calc(simData.runData.variableValues,simData,null);
		} catch (MathCalcError e) {
			simData.calculationErrorStation(thresholdExpressionObj,stationLogName);
			return false;
		}

		boolean trigger=false;
		if (lastThresholdCheckTime<0 || lastThresholdCheckTime>simData.currentTime) {
			trigger=false;
		} else {
			switch (thresholdDirection) {
			case THRESHOLD_DOWN:
				trigger=(lastThresholdCheckValue>=thresholdValue && newValue<thresholdValue);
				break;
			case THRESHOLD_UP:
				trigger=(lastThresholdCheckValue<=thresholdValue && newValue>thresholdValue);
				break;
			}
		}

		lastThresholdCheckValue=newValue;
		if (trigger)
			lastThresholdCheckTime=simData.currentTime;
		else
			lastThresholdCheckTime=simData.currentTime-1;
		return trigger;
	}

	/**
	 * Pr�ft, ob sich die Simulationsdaten so ver�ndert haben, dass die Bedingung des Action-Datensatzes erf�llt ist.
	 * @param simData	Simulationsdatenobjekt
	 * @param stationLogName	Name der Station, an der die Pr�fung stattfindet
	 * @return	Gibt <code>true</code> zur�ck, wenn die Bedingung f�r die Ausl�sung der Aktion erf�llt ist
	 */
	public boolean checkTrigger(final SimulationData simData, final String stationLogName) {
		switch (conditionType) {
		case CONDITION_CONDITION: return checkTriggerCondition(simData);
		case CONDITION_THRESHOLD: return checkTriggerThreshold(simData,stationLogName);
		case CONDITION_SIGNAL: return false;
		default: return false;
		}
	}

	/**
	 * Pr�ft, ob ein ausgel�stes Signal als Ausl�ser f�r diese Action g�ltig ist.
	 * @param signalName	Ausgel�stes Signal, welches mit dem in dieser Action hinterlegten Bedingung abgeglichen werden soll
	 * @return	Gibt <code>true</code> zur�ck, wenn die Bedingung f�r die Ausl�sung der Aktion erf�llt ist
	 */
	public boolean checkTriggerSignal(final String signalName) {
		if (conditionType==ConditionType.CONDITION_SIGNAL) {
			if (conditionSignalName==null || conditionSignalName.trim().isEmpty()) return false;
			if (signalName.equalsIgnoreCase(conditionSignalName)) return true;
			return false;
		} else {
			return false;
		}
	}

	/**
	 * F�hrt die in der Action hinterlegte Aktion aus und plant die n�chste zeitgesteuerte Ausf�hrung ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param stationLogName	Name der Station an der die Aktion ausgel�st wird (f�r das Logging)
	 * @param stationLogColor	Farbe beim Logging f�r die Station an der die Aktion ausgel�st wird
	 * @param actionRecordIndex	Index dieses Datensatzes innerhalb des Action-Elements
	 */
	public void runTimedAction(final SimulationData simData, final String stationLogName, final Color stationLogColor, final int actionRecordIndex) {
		/* Ggf. n�chstes Ereignis planen */
		final boolean scheduleNext;
		if (timeRepeatCount<0) {
			/* Unendliche viele Ereignisse */
			scheduleNext=true;
		} else if (timeRepeatCount>0) {
			timeRepeatCount--;
			scheduleNext=(timeRepeatCount>0);
		} else {
			scheduleNext=false;
		}

		if (scheduleNext) {
			scheduleNextTimedAction(simData,simData.currentTime+timeIntervalMS,actionRecordIndex);
		}

		/* Aktion ausl�sen */
		runAction(simData,stationLogName,stationLogColor);
	}

	/**
	 * F�hrt die in der Action hinterlegte Aktion aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param stationLogName	Name der Station an der die Aktion ausgel�st wird (f�r das Logging)
	 * @param stationLogColor	Farbe beim Logging f�r die Station an der die Aktion ausgel�st wird
	 */
	public void runAction(final SimulationData simData, final String stationLogName, final Color stationLogColor) {
		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			simData.runData.setClientVariableValues(null);
			try {
				analogData.setValue(simData,analogValueObj.calc(simData.runData.variableValues,simData,null));
			} catch (MathCalcError e) {
				simData.calculationErrorStation(analogValueObj,stationLogName);
			}
			break;
		case ACTION_ASSIGN:
			simData.runData.setClientVariableValues(null);
			try {
				simData.runData.variableValues[assignVariableIndex]=assignExpressionObj.calc(simData.runData.variableValues,simData,null);
				simData.runData.updateVariableValueForStatistics(simData,assignVariableIndex);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(assignExpressionObj,stationLogName);
			}
			break;
		case ACTION_SCRIPT:
			if (jsRunner!=null) {
				jsRunner.setSimulationDataNoClient(simData,stationID);
				final String result=jsRunner.runCompiled();
				if (simData.logging instanceof CallbackLoggerWithJS) {
					((CallbackLoggerWithJS)simData.logging).logJS(simData.currentTime,stationLogName,stationLogColor,script,result);
				}
				if (!jsRunner.getLastSuccess() && simData.runModel.cancelSimulationOnScriptError) {
					simData.doEmergencyShutDown("id="+stationID+": "+result);
				}
			}
			if (javaRunner!=null) {
				final Object result=javaRunner.run();
				if (javaRunner.getStatus()!=DynamicStatus.OK) simData.doEmergencyShutDown("id="+stationID+": "+DynamicFactory.getLongStatusText(javaRunner));
				if (simData.logging instanceof CallbackLoggerWithJS) {
					((CallbackLoggerWithJS)simData.logging).logJS(simData.currentTime,stationLogName,stationLogColor,script,(result==null)?"":result.toString());
				}
				if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.cancelSimulationOnScriptError) {
					simData.doEmergencyShutDown("id="+stationID+": "+DynamicFactory.getLongStatusText(javaRunner));
				}
			}
			simData.runData.updateMapValuesForStatistics(simData);
			break;
		case ACTION_SIGNAL:
			simData.runData.fireSignal(simData,signalName);
			break;
		case ACTION_STOP:
			simData.doShutDown();
			break;
		case ACTION_SOUND:
			if (simData.runModel.isAnimation) {
				SoundSystem.getInstance().playAll(sound,soundMaxSeconds);
			}
			break;
		}
	}
}