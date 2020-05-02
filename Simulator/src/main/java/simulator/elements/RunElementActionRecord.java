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
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.editmodel.EditModel;
import simulator.events.SystemChangeEvent;
import simulator.logging.CallbackLoggerWithJS;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementActionRecord;
import ui.modeleditor.elements.ModelElementActionRecord.ConditionType;
import ui.modeleditor.elements.ModelElementActionRecord.ThresholdDirection;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementTank;

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
	private final ModelElementActionRecord editRecord;

	private ModelElementActionRecord.ActionMode actionMode;

	private ModelElementActionRecord.ConditionType conditionType;
	private ModelElementActionRecord.ActionType actionType;

	private String condition;
	private ExpressionMultiEval conditionObj;
	private long conditionMinDistance;
	private String thresholdExpression;
	private ExpressionCalc thresholdExpressionObj;
	private double thresholdValue;
	private ThresholdDirection thresholdDirection;
	private String conditionSignalName;

	private int assignVariableIndex;
	private String assignExpression;
	private ExpressionCalc assignExpressionObj;
	private String signalName;
	private int analogID;
	private RunElementAnalogProcessingData analogData;
	private String analogValue;
	private ExpressionCalc analogValueObj;
	private String script;
	private ModelElementActionRecord.ScriptMode scriptMode;
	private JSRunSimulationData jsRunner;
	private DynamicRunner javaRunner;

	private long lastConditionTrigger;
	private long lastThresholdCheckTime;
	private double lastThresholdCheckValue;

	/**
	 * Konstruktor der Klasse
	 * @param editRecord	Zugeh�riges Editor-Element
	 * @see #build(EditModel, RunModel, boolean)
	 */
	public RunElementActionRecord(final ModelElementActionRecord editRecord) {
		actionMode=editRecord.getActionMode();
		this.editRecord=editRecord;
	}

	/**
	 * Copy-Konstruktor
	 * @param runRecord	Zu kopierendes Ausgangselement
	 */
	public RunElementActionRecord(final RunElementActionRecord runRecord) {
		actionMode=runRecord.actionMode;
		editRecord=runRecord.editRecord;

		conditionType=runRecord.conditionType;
		actionType=runRecord.actionType;

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
				final String scriptError=DynamicFactory.getFactory().test(script,true);
				if (scriptError!=null) return scriptError;
			}
			break;
		case ACTION_SIGNAL:
			signalName=editRecord.getSignalName();
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
			/* hier nichts zu pr�fen */
			break;
		case ACTION_SIGNAL:
			if (editRecord.getSignalName().trim().isEmpty()) Language.tr("Simulation.Creator.Action.NoSignalName");
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
	 */
	public void initRunData(final SimulationData simData) {
		final String[] variableNames=simData.runModel.variableNames;

		if (actionMode==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
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
				javaRunner=DynamicFactory.getFactory().load(script);
				javaRunner.parameter.system=new SystemImpl(simData);
				break;
			}
			break;
		case ACTION_SIGNAL:
			/* nicht vorzubereiten */
			break;
		}
	}

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

	private boolean checkTriggerThreshold(final SimulationData simData, final String stationLogName) {
		final Double D=thresholdExpressionObj.calc(simData.runData.variableValues,simData,null);
		if (D==null) {
			simData.calculationErrorStation(thresholdExpressionObj,stationLogName);
			return false;
		}
		final double newValue=D.doubleValue();

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
		lastThresholdCheckTime=simData.currentTime;
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
	 * F�hrt die in der Action hinterlegte Aktion aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param stationLogName	Name der Station an der die Aktion ausgel�st wird (f�r das Logging)
	 * @param stationLogColor	Farbe beim Logging f�r die Station an der die Aktion ausgel�st wird
	 */
	public void runAction(final SimulationData simData, final String stationLogName, final Color stationLogColor) {
		Double D;

		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			simData.runData.setClientVariableValues(null);
			D=analogValueObj.calc(simData.runData.variableValues,simData,null);
			if (D==null) {
				simData.calculationErrorStation(analogValueObj,stationLogName);
			} else {
				analogData.setValue(simData,D.doubleValue());
			}
			break;
		case ACTION_ASSIGN:
			simData.runData.setClientVariableValues(null);
			D=assignExpressionObj.calc(simData.runData.variableValues,simData,null);
			if (D!=null) {
				simData.runData.variableValues[assignVariableIndex]=D.doubleValue();
			} else {
				simData.calculationErrorStation(assignExpressionObj,stationLogName);
			}
			break;
		case ACTION_SCRIPT:
			if (jsRunner!=null) {
				jsRunner.setSimulationDataNoClient(simData);
				final String result=jsRunner.runCompiled();
				if (simData.logging!=null && (simData.logging instanceof CallbackLoggerWithJS)) {
					((CallbackLoggerWithJS)simData.logging).logJS(simData.currentTime,stationLogName,stationLogColor,script,result);
				}
				if (!jsRunner.getLastSuccess() && simData.runModel.canelSimulationOnScriptError) {
					simData.doEmergencyShutDown(result);
				}
			}
			if (javaRunner!=null) {
				final Object result=javaRunner.run();
				if (javaRunner.getStatus()!=DynamicStatus.OK) simData.doEmergencyShutDown(DynamicFactory.getLongStatusText(javaRunner));
				if (simData.logging!=null && (simData.logging instanceof CallbackLoggerWithJS)) {
					((CallbackLoggerWithJS)simData.logging).logJS(simData.currentTime,stationLogName,stationLogColor,script,(result==null)?"":result.toString());
				}
				if (javaRunner.getStatus()!=DynamicStatus.OK && simData.runModel.canelSimulationOnScriptError) {
					simData.doEmergencyShutDown(DynamicFactory.getLongStatusText(javaRunner));
				}
			}
			break;
		case ACTION_SIGNAL:
			simData.runData.fireSignal(simData,signalName);
			break;
		}
	}
}