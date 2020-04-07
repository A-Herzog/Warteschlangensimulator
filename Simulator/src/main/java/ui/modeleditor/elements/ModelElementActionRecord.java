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
package ui.modeleditor.elements;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse kapselt eine Aktion, wie sie über das {@link ModelElementAction}
 * durchgeführt werden können.
 * @author Alexander Herzog
 * @see ModelElementAction
 */
public class ModelElementActionRecord {
	/**
	 * Besteht die Aktion aus einem Skript, so kann hier die Sprache angegeben werden
	 * @author Alexander Herzog
	 * @see ModelElementActionRecord#getScriptMode()
	 * @see ModelElementActionRecord#setScriptMode(ScriptMode)
	 */
	public enum ScriptMode {
		/** Javascript als Sprache verwenden */
		Javascript,
		/** Java als Sprache verwenden */
		Java
	}

	/**
	 * Gibt an, ob der Datensatz Ursache und Wirkung oder nur Wirkung enthält
	 * @author Alexander Herzog
	 * @see ModelElementActionRecord#getActionMode()
	 */
	public enum ActionMode {
		/** Ursache und Wirkung */
		TRIGGER_AND_ACTION,
		/** Nur Wirkung */
		ACTION_ONLY
	}

	/**
	 * Art der Bedingung, die die Aktion auslöst
	 * @author Alexander Herzog
	 */
	public enum ConditionType {
		/** Aktion durch Bedingung auslösen */
		CONDITION_CONDITION,
		/** Aktion durch Schwellenwert auslösen */
		CONDITION_THRESHOLD,
		/** Aktion durch Signalauslösen */
		CONDITION_SIGNAL
	}

	/**
	 * Art der Aktion, die ausgelöst wird
	 * @author Alexander Herzog
	 */
	public enum ActionType {
		/** Aktion: Variablenzuweisung vornehmen */
		ACTION_ASSIGN,
		/** Aktion: Signal auslösen */
		ACTION_SIGNAL,
		/** Aktion: Analogwert ändern */
		ACTION_ANALOG_VALUE,
		/** Aktion: Skript ausführen */
		ACTION_SCRIPT
	}

	/**
	 * Wenn die Aktion auf Basis eines Schwellenwertes ausgelöst werden soll:
	 * Bei Über- oder Unterschreitung des Schwellenwertes?
	 * @author Alexander Herzog
	 *
	 */
	public enum ThresholdDirection {
		/** Aktion auslösen bei Überschreitung des Schwellenwertes */
		THRESHOLD_UP,
		/** Aktion auslösen bei Unterschreitung des Schwellenwertes */
		THRESHOLD_DOWN
	}

	private final ActionMode actionMode;

	private ConditionType conditionType;
	private ActionType actionType;

	private String condition;
	private double conditionMinDistance;
	private String thresholdExpression;
	private double thresholdValue;
	private ThresholdDirection thresholdDirection;
	private String conditionSignal;

	private String assignVariable;
	private String assignExpression;
	private String signalName;
	private int analogID;
	private String analogValue;
	private String script;
	private ScriptMode scriptMode;

	/**
	 * Konstruktor der Klasse
	 * @param actionMode	Gibt an, ob der Datensatz Ursache und Wirkung oder nur Wirkung enthält
	 */
	public ModelElementActionRecord(final ActionMode actionMode) {
		this.actionMode=actionMode;
		conditionType=ConditionType.CONDITION_CONDITION;
		actionType=ActionType.ACTION_ASSIGN;

		condition="";
		conditionMinDistance=1.0;
		thresholdExpression="";
		thresholdValue=0.0;
		thresholdDirection=ThresholdDirection.THRESHOLD_UP;
		conditionSignal="";

		assignVariable="";
		assignExpression="";
		signalName="";
		analogID=0;
		analogValue="";
		script="";
		scriptMode=ScriptMode.Javascript;
	}

	/**
	 * Copy-Konstruktor für die Klasse
	 * @param copySource	Ausgangsobjekt von dem die Daten kopiert werden sollen
	 */
	public ModelElementActionRecord(final ModelElementActionRecord copySource) {
		this((copySource==null)?ActionMode.TRIGGER_AND_ACTION:copySource.actionMode);

		if (copySource!=null) {
			conditionType=copySource.conditionType;
			actionType=copySource.actionType;

			condition=copySource.condition;
			conditionMinDistance=copySource.conditionMinDistance;
			thresholdExpression=copySource.thresholdExpression;
			thresholdValue=copySource.thresholdValue;
			thresholdDirection=copySource.thresholdDirection;
			conditionSignal=copySource.conditionSignal;

			assignVariable=copySource.assignVariable;
			assignExpression=copySource.assignExpression;
			signalName=copySource.signalName;
			analogID=copySource.analogID;
			analogValue=copySource.analogValue;
			script=copySource.script;
			scriptMode=copySource.scriptMode;
		}
	}

	/**
	 * Vergleicht zwei Aktionsdatensätze
	 * @param otherRecord	Zweiter Aktionsdatensatz, der mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code> wenn die beiden Datensätze inhaltlich identisch sind
	 */
	public boolean equalsRecord(final ModelElementActionRecord otherRecord) {
		if (otherRecord==null) return false;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			if (conditionType!=otherRecord.conditionType) return false;
		}
		if (actionType!=otherRecord.actionType) return false;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			switch (conditionType) {
			case CONDITION_CONDITION:
				if (!Objects.equals(condition,otherRecord.condition)) return false;
				if (conditionMinDistance!=otherRecord.conditionMinDistance) return false;
				break;
			case CONDITION_THRESHOLD:
				if (!Objects.equals(thresholdExpression,otherRecord.thresholdExpression)) return false;
				if (thresholdValue!=otherRecord.thresholdValue) return false;
				if (thresholdDirection!=otherRecord.thresholdDirection) return false;
				break;
			case CONDITION_SIGNAL:
				if (!Objects.equals(conditionSignal,otherRecord.conditionSignal)) return false;
				break;
			}
		}

		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			if (analogID!=otherRecord.analogID) return false;
			if (!Objects.equals(analogValue,otherRecord.analogValue)) return false;
			break;
		case ACTION_ASSIGN:
			if (!Objects.equals(assignVariable,otherRecord.assignVariable)) return false;
			if (!Objects.equals(assignExpression,otherRecord.assignExpression)) return false;
			break;
		case ACTION_SCRIPT:
			if (!Objects.equals(script,otherRecord.script)) return false;
			if (scriptMode!=otherRecord.scriptMode) return false;
			break;
		case ACTION_SIGNAL:
			if (!Objects.equals(signalName,otherRecord.signalName)) return false;
			break;
		}

		return true;
	}

	/**
	 * Gibt an, ob der Datensatz Ursache und Wirkung oder nur Wirkung enthält
	 * @return	Umfang des Datensatzes
	 * @see ModelElementActionRecord.ActionMode
	 */
	public ActionMode getActionMode() {
		return actionMode;
	}

	/**
	 * Gibt an, welcher Art die Bedingung, die die Aktion auslösen soll, sein soll.
	 * @return	Art der Bedingung, die die Aktion auslösen soll
	 * @see ConditionType
	 */
	public ConditionType getConditionType() {
		return conditionType;
	}

	/**
	 * Liefert im Fall, dass die Aktion über eine Bedingung ausgelöst werden soll, diese Bedingung.
	 * @return	Bedingung, die, wenn sie erfüllt ist, die Aktion auslöst
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#setCondition(String)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt im Fall, dass die Aktion über eine Bedingung ausgelöst werden soll, diese Bedingung ein.
	 * @param condition	Bedingung, die, wenn sie erfüllt ist, die Aktion auslöst
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#getCondition()
	 */
	public void setCondition(final String condition) {
		conditionType=ConditionType.CONDITION_CONDITION;
		if (condition==null) this.condition=""; else this.condition=condition;
	}

	/**
	 * Liefert im Fall, dass die Aktion über eine Bedingung ausgelöst werden soll, den minimalen Zeitabstand zwischen zwei Bedingungsprüfungen.
	 * @return	Minimaler Zeitabstand zwischen zwei Bedingungsprüfungen
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#setConditionMinDistance(double)
	 */
	public double getConditionMinDistance() {
		return conditionMinDistance;
	}

	/**
	 * Stellt im Fall, dass die Aktion über eine Bedingung ausgelöst werden soll, den minimalen Zeitabstand zwischen zwei Bedingungsprüfungen ein.
	 * @param conditionMinDistance	Minimaler Zeitabstand zwischen zwei Bedingungsprüfungen
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#getConditionMinDistance()
	 */
	public void setConditionMinDistance(final double conditionMinDistance) {
		conditionType=ConditionType.CONDITION_CONDITION;
		this.conditionMinDistance=Math.max(0,conditionMinDistance);
	}

	/**
	 * Liefert im Fall, dass die Aktion über einen Schwellenwert ausgelöst werden soll, den Schwellenwertausdruck.
	 * @return	Schwellenwertausdruck, dessen Über- oder Unterschreitung die Aktion auslösen soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#setThresholdExpression(String)
	 */
	public String getThresholdExpression() {
		return thresholdExpression;
	}

	/**
	 * Liefert im Fall, dass die Aktion über ein Signal ausgelöst werden soll, den Signalnamen
	 * @return	Signal das die Aktion auslösen soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#setConditionSignal(String)
	 */
	public String getConditionSignal() {
		return conditionSignal;
	}

	/**
	 * Stellt den Namen des Signals, das die Aktion auslösen soll, ein.
	 * @param signalName	Signal das die Aktion auslösen soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#getConditionSignal()
	 */
	public void setConditionSignal(final String signalName) {
		if (signalName==null || signalName.trim().isEmpty()) return;
		conditionType=ConditionType.CONDITION_SIGNAL;
		conditionSignal=signalName.trim();
	}

	/**
	 * Stellt im Fall, dass die Aktion über einen Schwellenwert ausgelöst werden soll, den Schwellenwertausdruck ein.
	 * @param thresholdExpression	Schwellenwertausdruck, dessen Über- oder Unterschreitung die Aktion auslösen soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#getThresholdExpression()
	 */
	public void setThresholdExpression(final String thresholdExpression) {
		conditionType=ConditionType.CONDITION_THRESHOLD;
		if (thresholdExpression==null) this.thresholdExpression=""; else this.thresholdExpression=thresholdExpression;
	}

	/**
	 * Liefert im Fall, dass die Aktion über einen Schwellenwert ausgelöst werden soll, den Schwellenwert (der gegen den Ausdruck geprüft werden soll).
	 * @return	Schwellenwert, dessen Über- oder Unterschreitung die Aktion auslösen soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#setThresholdValue(double)
	 */
	public double getThresholdValue() {
		return thresholdValue;
	}

	/**
	 * Stellt im Fall, dass die Aktion über einen Schwellenwert ausgelöst werden soll, den Schwellenwert (der gegen den Ausdruck geprüft werden soll) ein.
	 * @param thresholdValue	Schwellenwert, dessen Über- oder Unterschreitung die Aktion auslösen soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#getThresholdValue()
	 */
	public void setThresholdValue(final double thresholdValue) {
		conditionType=ConditionType.CONDITION_THRESHOLD;
		this.thresholdValue=thresholdValue;
	}

	/**
	 * Liefert im Fall, dass die Aktion über einen Schwellenwert ausgelöst werden soll, die Information, ob die Aktion beim Über- oder Unterschreiten des Schwellenwertes ausgelöst werden soll.
	 * @return	Angabe, ob die Aktion beim Über- oder Unterschreiten des Schwellenwertes ausgelöst werden soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#setThresholdDirection(ThresholdDirection)
	 */
	public ThresholdDirection getThresholdDirection() {
		return thresholdDirection;
	}

	/**
	 * Stellt im Fall, dass die Aktion über einen Schwellenwert ausgelöst werden soll, die Information, ob die Aktion beim Über- oder Unterschreiten des Schwellenwertes ausgelöst werden soll, ein.
	 * @param thresholdDirection	Angabe, ob die Aktion beim Über- oder Unterschreiten des Schwellenwertes ausgelöst werden soll
	 * @see ModelElementActionRecord#getConditionType()
	 * @see ModelElementActionRecord#getThresholdDirection()
	 */
	public void setThresholdDirection(final ThresholdDirection thresholdDirection) {
		conditionType=ConditionType.CONDITION_THRESHOLD;
		if (thresholdDirection!=null) this.thresholdDirection=thresholdDirection;
	}

	/**
	 * Gibt an, welcher Art die auszulösende Aktion sein soll.
	 * @return	Art der Aktion
	 * @see ActionType
	 */
	public ActionType getActionType() {
		return actionType;
	}

	/**
	 * Liefert im Fall, dass die Aktion eine Variablenzuweisung ist, den Variablennamen.
	 * @return	Variablenname für die Variablenzuweisung
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setAssignVariable(String)
	 */
	public String getAssignVariable() {
		return assignVariable;
	}

	/**
	 * Stellt im Fall, dass die Aktion eine Variablenzuweisung ist, den Variablennamen ein.
	 * @param assignVariable	Variablenname für die Variablenzuweisung
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#getAssignVariable()
	 */
	public void setAssignVariable(final String assignVariable) {
		actionType=ActionType.ACTION_ASSIGN;
		if (assignVariable==null) this.assignVariable=""; else this.assignVariable=assignVariable;
	}

	/**
	 * Liefert im Fall, dass die Aktion eine Variablenzuweisung ist, den zuzuweisenden Ausdruck.
	 * @return	Ausdruck für die Variablenzuweisung
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setAssignExpression(String)
	 */
	public String getAssignExpression() {
		return assignExpression;
	}

	/**
	 * Stellt im Fall, dass die Aktion eine Variablenzuweisung ist, den zuzuweisenden Ausdruck ein.
	 * @param assignExpression	Ausdruck für die Variablenzuweisung
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#getAssignExpression()
	 */
	public void setAssignExpression(final String assignExpression) {
		actionType=ActionType.ACTION_ASSIGN;
		if (assignExpression==null) this.assignExpression=""; else this.assignExpression=assignExpression;
	}

	/**
	 * Liefert im Fall, dass die Aktion eine Signalauslösung ist, den Namen des Signal.
	 * @return	Name des Signals für Signalauslösung
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setSignalName(String)
	 */
	public String getSignalName() {
		return signalName;
	}

	/**
	 * Stellt im Fall, dass die Aktion eine Signalauslösung ist, den Namen des Signal ein.
	 * @param signalName	Name des Signals für Signalauslösung
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#getSignalName()
	 */
	public void setSignalName(final String signalName) {
		actionType=ActionType.ACTION_SIGNAL;
		if (signalName==null) this.signalName=""; else this.signalName=signalName;
	}

	/**
	 * Liefert im Fall, dass die Aktion einen Analogwert einstellen soll, die id der Analogwertstation.
	 * @return	id der Analogwertstation, an der ein Wert eingestellt werden soll
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setAnalogID(int)
	 */
	public int getAnalogID() {
		return analogID;
	}

	/**
	 * Stellt im Fall, dass die Aktion einen Analogwert einstellen soll, die id der Analogwertstation ein.
	 * @param analogID	id der Analogwertstation, an der ein Wert eingestellt werden soll
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#getAnalogID()
	 */
	public void setAnalogID(final int analogID) {
		actionType=ActionType.ACTION_ANALOG_VALUE;
		if (analogID>=0) this.analogID=analogID;
	}

	/**
	 * Liefert im Fall, dass die Aktion einen Analogwert einstellen soll, den Analogwert.
	 * @return	Analogwert der eingestellt werden soll
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setAnalogValue(String)
	 */
	public String getAnalogValue() {
		return analogValue;
	}

	/**
	 * Stellt im Fall, dass die Aktion einen Analogwert einstellen soll, den Analogwert ein.
	 * @param analogValue	Analogwert der eingestellt werden soll
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#getAnalogValue()
	 */
	public void setAnalogValue(final String analogValue) {
		actionType=ActionType.ACTION_ANALOG_VALUE;
		if (analogValue==null) this.analogValue=""; else this.analogValue=analogValue;
	}

	/**
	 * Liefert im Fall, dass bei der Aktion Javascript-Code ausgeführt werden soll, diesen Javascript-Code.
	 * @return	Als Aktion Auszuführender Javascript-Code
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setScript(String)
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Stellt im Fall, dass bei der Aktion Javascript-Code ausgeführt werden soll, diesen Javascript-Code ein.
	 * @param script	Als Aktion Auszuführender Javascript-Code
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#getScript()
	 */
	public void setScript(final String script) {
		actionType=ActionType.ACTION_SCRIPT;
		if (script==null) this.script=""; else this.script=script;
	}

	/**
	 * Liefert die gewählte Sprache für das Skript
	 * @return	Sprache für das Skript
	 * @see ModelElementActionRecord.ScriptMode
	 */
	public ScriptMode getScriptMode() {
		return scriptMode;
	}

	/**
	 * Stellt die Sprache für das Skript ein.
	 * @param scriptMode	Sprache für das Skript
	 * @see ModelElementActionRecord.ScriptMode
	 */
	public void setScriptMode(final ScriptMode scriptMode) {
		if (scriptMode!=null) this.scriptMode=scriptMode;
	}

	/**
	 * Speichert die Eigenschaften des Datensatzes als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element node;
		parent.appendChild(node=doc.createElement(Language.trPrimary("Surface.Action.XML.Record")));

		String type;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Typ der Bedingung */
			switch (conditionType) {
			case CONDITION_CONDITION: type=Language.trPrimary("Surface.Action.XML.Record.ConditionType.Condition"); break;
			case CONDITION_THRESHOLD: type=Language.trPrimary("Surface.Action.XML.Record.ConditionType.Threshold"); break;
			case CONDITION_SIGNAL: type=Language.trPrimary("Surface.Action.XML.Record.ConditionType.Signal"); break;
			default: type=""; break;
			}
			node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.ConditionType"),type);
		}

		/* Typ der Aktion */
		switch (actionType) {
		case ACTION_ANALOG_VALUE: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.AnalogValue"); break;
		case ACTION_ASSIGN: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.Assign"); break;
		case ACTION_SCRIPT:
			switch (scriptMode) {
			case Javascript: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.JS"); break;
			case Java: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.Java"); break;
			default: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.JS"); break;
			}
			break;
		case ACTION_SIGNAL: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.Signal"); break;
		default: type=""; break;
		}
		node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.ActionType"),type);

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_CONDITION:
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.Condition"),condition);
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.ConditionMinDistance"),NumberTools.formatSystemNumber(conditionMinDistance));
				break;
			case CONDITION_THRESHOLD:
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.Threshold"),thresholdExpression);
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.ThresholdValue"),NumberTools.formatSystemNumber(thresholdValue));
				switch (thresholdDirection) {
				case THRESHOLD_DOWN:
					node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.ThresholdDirection"),Language.trPrimary("Surface.Action.XML.Record.Condition.ThresholdDirection.Down"));
					break;
				case THRESHOLD_UP:
					node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.ThresholdDirection"),Language.trPrimary("Surface.Action.XML.Record.Condition.ThresholdDirection.Up"));
					break;
				}
				break;
			case CONDITION_SIGNAL:
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Condition.Signal"),conditionSignal);
				break;
			}
		}

		/* Aktion */
		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Action.AnalogID"),""+analogID);
			node.setTextContent(analogValue);
			break;
		case ACTION_ASSIGN:
			node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Action.AssignVariable"),assignVariable);
			node.setTextContent(assignExpression);
			break;
		case ACTION_SCRIPT:
			node.setTextContent(script);
			break;
		case ACTION_SIGNAL:
			node.setTextContent(signalName);
			break;
		}
	}

	/**
	 * Lädt die Einstellungen des Datensatzes aus einem xml-Knoten
	 * @param node	xml-Element, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Typ der Bedingung */
			final String conditionTypeString=Language.trAllAttribute("Surface.Action.XML.Record.ConditionType",node);
			if (Language.trAll("Surface.Action.XML.Record.ConditionType.Condition",conditionTypeString)) conditionType=ConditionType.CONDITION_CONDITION;
			if (Language.trAll("Surface.Action.XML.Record.ConditionType.Threshold",conditionTypeString)) conditionType=ConditionType.CONDITION_THRESHOLD;
			if (Language.trAll("Surface.Action.XML.Record.ConditionType.Signal",conditionTypeString)) conditionType=ConditionType.CONDITION_SIGNAL;
		}

		/* Typ der Aktion */
		final String actionTypeString=Language.trAllAttribute("Surface.Action.XML.Record.ActionType",node);
		if (Language.trAll("Surface.Action.XML.Record.ActionType.AnalogValue",actionTypeString)) actionType=ActionType.ACTION_ANALOG_VALUE;
		if (Language.trAll("Surface.Action.XML.Record.ActionType.Assign",actionTypeString)) actionType=ActionType.ACTION_ASSIGN;
		if (Language.trAll("Surface.Action.XML.Record.ActionType.JS",actionTypeString)) {actionType=ActionType.ACTION_SCRIPT; scriptMode=ScriptMode.Javascript;}
		if (Language.trAll("Surface.Action.XML.Record.ActionType.Java",actionTypeString)) {actionType=ActionType.ACTION_SCRIPT; scriptMode=ScriptMode.Java;}
		if (Language.trAll("Surface.Action.XML.Record.ActionType.Signal",actionTypeString)) actionType=ActionType.ACTION_SIGNAL;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_CONDITION:
				condition=Language.trAllAttribute("Surface.Action.XML.Record.Condition.Condition",node);
				final String conditionMinDistanceString=Language.trAllAttribute("Surface.Action.XML.Record.Condition.ConditionMinDistance",node);
				if (!conditionMinDistanceString.isEmpty()) {
					final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(conditionMinDistanceString));
					if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Action.XML.Record.Condition.ConditionMinDistance"),node.getNodeName(),node.getParentNode().getNodeName());
					conditionMinDistance=D.doubleValue();
				}
				break;
			case CONDITION_THRESHOLD:
				thresholdExpression=Language.trAllAttribute("Surface.Action.XML.Record.Condition.Threshold",node);
				final String thresholdValueString=Language.trAllAttribute("Surface.Action.XML.Record.Condition.ThresholdValue",node);
				if (!thresholdValueString.isEmpty()) {
					final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(thresholdValueString));
					if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Action.XML.Record.Condition.ThresholdValue"),node.getNodeName(),node.getParentNode().getNodeName());
					thresholdValue=D.doubleValue();
				}
				final String thresholdDirectionString=Language.trAllAttribute("Surface.Action.XML.Record.Condition.ThresholdDirection",node);
				if (Language.trAll("Surface.Action.XML.Record.Condition.ThresholdDirection.Down",thresholdDirectionString)) thresholdDirection=ThresholdDirection.THRESHOLD_DOWN;
				if (Language.trAll("Surface.Action.XML.Record.Condition.ThresholdDirection.Up",thresholdDirectionString)) thresholdDirection=ThresholdDirection.THRESHOLD_UP;
				break;
			case CONDITION_SIGNAL:
				conditionSignal=Language.trAllAttribute("Surface.Action.XML.Record.Condition.Signal",node);
				break;
			}
		}

		/* Aktion */
		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			final String analogIDString=Language.trAllAttribute("Surface.Action.XML.Record.Action.AnalogID",node);
			if (!analogIDString.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(analogIDString);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Action.XML.Record.Action.AnalogID"),node.getNodeName(),node.getParentNode().getNodeName());
				analogID=I.intValue();
			}
			analogValue=node.getTextContent();
			break;
		case ACTION_ASSIGN:
			assignVariable=Language.trAllAttribute("Surface.Action.XML.Record.Action.AssignVariable",node);
			assignExpression=node.getTextContent();
			break;
		case ACTION_SCRIPT:
			script=node.getTextContent();
			break;
		case ACTION_SIGNAL:
			signalName=node.getTextContent();
			break;
		}

		return null;
	}

	/**
	 * Erstellt eine Beschreibung für den Datensatz
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param level		Position in der Reihenfolge der Description-Builder Eigenschaften
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder, final int level) {
		String s;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_CONDITION:
				s=String.format(Language.tr("ModelDescription.Action.Condition.Condition.MinDistance"),NumberTools.formatNumber(conditionMinDistance));
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Action.Condition.Condition"),condition+" ("+s+")",level);
				break;
			case CONDITION_THRESHOLD:
				s="";
				switch (thresholdDirection) {
				case THRESHOLD_DOWN: s=Language.tr("ModelDescription.Action.Condition.Threshold.Down"); break;
				case THRESHOLD_UP: s=Language.tr("ModelDescription.Action.Condition.Threshold.Up"); break;
				}
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Action.Condition.Threshold"),thresholdExpression+", "+Language.tr("ModelDescription.Action.Condition.Threshold.Value")+": "+NumberTools.formatNumber(thresholdValue)+" ("+s+")",level);
				break;
			case CONDITION_SIGNAL:
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Action.Condition.Signal"),conditionSignal,level);
				break;
			}
		}

		/* Aktion */
		s="";
		switch (actionType) {
		case ACTION_ANALOG_VALUE:
			s=String.format(Language.tr("ModelDescription.Action.Action.Analog"),analogID,analogValue);
			break;
		case ACTION_ASSIGN:
			s=Language.tr("ModelDescription.Action.Action.Assign")+": "+assignVariable+":="+assignExpression;
			break;
		case ACTION_SCRIPT:
			switch (scriptMode) {
			case Javascript: s=Language.tr("ModelDescription.Action.Action.JS")+": "+script; break;
			case Java: s=Language.tr("ModelDescription.Action.Action.Java")+": "+script; break;
			}
			break;
		case ACTION_SIGNAL:
			s=Language.tr("ModelDescription.Action.Action.Signal")+": "+signalName;
			break;
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Action.Action"),s,level+1);
	}
}