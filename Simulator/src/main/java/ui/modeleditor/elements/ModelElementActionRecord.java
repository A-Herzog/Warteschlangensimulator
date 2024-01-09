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
import mathtools.TimeTools;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;
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
		/** Aktion zeitgesteuert auslösen */
		CONDITION_TIME,
		/** Aktion durch Bedingung auslösen */
		CONDITION_CONDITION,
		/** Aktion durch Schwellenwert auslösen */
		CONDITION_THRESHOLD,
		/** Aktion durch Signalauslösen */
		CONDITION_SIGNAL
	}

	/**
	 * Art der Aktion, die ausgelöst wird
	 */
	public enum ActionType {
		/** Aktion: Variablenzuweisung vornehmen */
		ACTION_ASSIGN,
		/** Aktion: Signal auslösen */
		ACTION_SIGNAL,
		/** Aktion: Analogwert ändern */
		ACTION_ANALOG_VALUE,
		/** Aktion: Skript ausführen */
		ACTION_SCRIPT,
		/** Aktion: Simulation beenden */
		ACTION_STOP,
		/** Aktion: Sound abspielen */
		ACTION_SOUND
	}

	/**
	 * Wenn die Aktion auf Basis eines Schwellenwertes ausgelöst werden soll:
	 * Bei Über- oder Unterschreitung des Schwellenwertes?
	 */
	public enum ThresholdDirection {
		/** Aktion auslösen bei Überschreitung des Schwellenwertes */
		THRESHOLD_UP,
		/** Aktion auslösen bei Unterschreitung des Schwellenwertes */
		THRESHOLD_DOWN
	}

	/**
	 * Gibt an, ob der Datensatz aktiv ist.
	 */
	private boolean active;

	/**
	 * Gibt an, ob der Datensatz Ursache und Wirkung oder nur Wirkung enthält
	 */
	private final ActionMode actionMode;

	/**
	 * Art der auslösenden Bedingung
	 * @see ConditionType
	 */
	private ConditionType conditionType;

	/**
	 * Art der Aktion, die ausgelöst werden soll
	 * @see ActionType
	 */
	private ActionType actionType;

	/* Auslöser: Zeit */

	/** Zeitdauer bis zu ersten Auslösung des Ereignisses */
	private double timeInitial;
	/** Zeitabstände zwischen den Auslösungen der Ereignisse */
	private double timeRepeat;
	/** Anzahl der Wiederholungen der Ereignisauslösungen (Werte &le;0 für unbegrenzt) */
	private int timeRepeatCount;

	/* Auslöser: Bedingung */

	/** Auslösende Bedingung im Fall <code>conditionType=ConditionType.CONDITION_CONDITION</code> */
	private String condition;
	/** Minimaler zeitliche Abstand zur Auslösung von Sktionen durch {@link #condition} im Fall <code>conditionType=ConditionType.CONDITION_CONDITION</code> */
	private double conditionMinDistance;

	/* Auslöser: Schwellenwert */

	/** Schwellenwert-Rechenausdruck im Fall <code>conditionType=ConditionType.CONDITION_THRESHOLD</code> */
	private String thresholdExpression;
	/** Schwellenwert-Zahlenwert im Fall <code>conditionType=ConditionType.CONDITION_THRESHOLD</code> */
	private double thresholdValue;
	/** Aktion beim unter- oder überschreiten des Schwellenwerts im Fall <code>conditionType=ConditionType.CONDITION_THRESHOLD</code> auslösen? */
	private ThresholdDirection thresholdDirection;

	/* Auslöser: Signal */

	/** Signal das im Fall <code>conditionType=ConditionType.CONDITION_SIGNAL</code> die Aktion auslöst */
	private String conditionSignal;

	/* Aktion: Variablenzuweisung vornehmen */

	/** Name der Variable für die Variablenzuweisung bei Aktionstyp <code>actionType=ActionType.ACTION_ASSIGN</code> */
	private String assignVariable;
	/** Wert für die Variable für die Variablenzuweisung bei Aktionstyp <code>actionType=ActionType.ACTION_ASSIGN</code> */
	private String assignExpression;

	/* Aktion: Signal auslösen */

	/** Name des auszulösenden Signals bei Aktionstyp <code>actionType=ActionType.ACTION_SIGNAL</code> */
	private String signalName;

	/* Aktion: Analogwert ändern */

	/** ID der Analogwert-Station im Fall bei Aktionstyp <code>actionType=ActionType.ACTION_ANALOG_VALUE</code> */
	private int analogID;
	/** Zuzuweisender Analogwert im Fall bei Aktionstyp <code>actionType=ActionType.ACTION_ANALOG_VALUE</code> */
	private String analogValue;

	/* Aktion: Skript ausführen */

	/** Auszuführendes Skript bei Aktionstyp <code>actionType=ActionType.ACTION_SCRIPT</code> */
	private String script;
	/** Sprache des auszuführenden Skripts bei Aktionstyp <code>actionType=ActionType.ACTION_SCRIPT</code> */
	private ScriptMode scriptMode;

	/* Aktion: Simulation beenden */
	/* keine Einstellungen */

	/* Aktion: Sound abspielen */

	/** Abzuspielender Sound bei <code>actionType=ActionType.ACTION_SOUD</code> */
	private String sound;
	/** Maximaldauer (in Sekunden) des abzuspielenden Sounds bei <code>actionType=ActionType.ACTION_SOUD</code> */
	private int soundMaxSeconds;

	/**
	 * Konstruktor der Klasse
	 * @param actionMode	Gibt an, ob der Datensatz Ursache und Wirkung oder nur Wirkung enthält
	 */
	public ModelElementActionRecord(final ActionMode actionMode) {
		active=true;
		this.actionMode=actionMode;
		conditionType=ConditionType.CONDITION_CONDITION;
		actionType=ActionType.ACTION_ASSIGN;

		timeInitial=120;
		timeRepeat=60;
		timeRepeatCount=-1;
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
		sound="";
		soundMaxSeconds=-1;
	}

	/**
	 * Copy-Konstruktor für die Klasse
	 * @param copySource	Ausgangsobjekt von dem die Daten kopiert werden sollen
	 */
	public ModelElementActionRecord(final ModelElementActionRecord copySource) {
		this((copySource==null)?ActionMode.TRIGGER_AND_ACTION:copySource.actionMode);

		if (copySource!=null) {
			active=copySource.active;
			conditionType=copySource.conditionType;
			actionType=copySource.actionType;

			timeInitial=copySource.timeInitial;
			timeRepeat=copySource.timeRepeat;
			timeRepeatCount=copySource.timeRepeatCount;
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
			sound=copySource.sound;
			soundMaxSeconds=copySource.soundMaxSeconds;
		}
	}

	/**
	 * Vergleicht zwei Aktionsdatensätze
	 * @param otherRecord	Zweiter Aktionsdatensatz, der mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code> wenn die beiden Datensätze inhaltlich identisch sind
	 */
	public boolean equalsRecord(final ModelElementActionRecord otherRecord) {
		if (otherRecord==null) return false;

		if (active!=otherRecord.active) return false;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			if (conditionType!=otherRecord.conditionType) return false;
		}
		if (actionType!=otherRecord.actionType) return false;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			switch (conditionType) {
			case CONDITION_TIME:
				if (timeInitial!=otherRecord.timeInitial) return false;
				if (timeRepeat!=otherRecord.timeRepeat) return false;
				if (timeRepeatCount!=otherRecord.timeRepeatCount) return false;
				break;
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
		case ACTION_STOP:
			/* Keine weiteren Einstellungen */
			break;
		case ACTION_SOUND:
			if (!Objects.equals(sound,otherRecord.sound)) return false;
			if (soundMaxSeconds!=otherRecord.soundMaxSeconds) return false;
			break;
		}

		return true;
	}

	/**
	 * Gibt an, ob der Datensatz aktiv ist.
	 * @return	Ist der Datensatz aktiv?
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Stellt ein, ob der Datensatz aktiv ist.
	 * @param active	Ist der Datensatz aktiv?
	 */
	public void setActive(boolean active) {
		this.active=active;
	}

	/**
	 * Gibt an, ob der Datensatz Ursache und Wirkung oder nur Wirkung enthält
	 * @return	Ursache des Datensatzes
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
	 * Liefert die Zeitdauer bis zu ersten Auslösung des Ereignisses.
	 * @return	Zeitdauer (in Sekunden) bis zu ersten Auslösung des Ereignisses
	 * @see #getConditionType()
	 * @see #setTimeInitial(double)
	 */
	public double getTimeInitial() {
		return timeInitial;
	}

	/**
	 * Stellt die Zeitdauer bis zu ersten Auslösung des Ereignisses ein.
	 * @param timeInitial	Zeitdauer (in Sekunden) bis zu ersten Auslösung des Ereignisses
	 * @see #getConditionType()
	 * @see #getTimeInitial()
	 */
	public void setTimeInitial(double timeInitial) {
		conditionType=ConditionType.CONDITION_TIME;
		this.timeInitial=Math.max(0,timeInitial);
	}

	/**
	 * Liefert die Zeitabstände zwischen den Auslösungen der Ereignisse.
	 * @return	Zeitabstände (in Sekunden) zwischen den Auslösungen der Ereignisse
	 * @see #getConditionType()
	 * @see #setTimeRepeat(double)
	 */
	public double getTimeRepeat() {
		return timeRepeat;
	}

	/**
	 * Stellt die Zeitabstände zwischen den Auslösungen der Ereignisse ein.
	 * @param timeRepeat	Zeitabstände (in Sekunden) zwischen den Auslösungen der Ereignisse
	 * @see #getConditionType()
	 * @see #getTimeRepeat()
	 */
	public void setTimeRepeat(double timeRepeat) {
		conditionType=ConditionType.CONDITION_TIME;
		this.timeRepeat=Math.max(0.001,timeRepeat);
	}

	/**
	 * Liefert Anzahl der Wiederholungen der Ereignisauslösungen.
	 * @return	Anzahl der Wiederholungen der Ereignisauslösungen (Werte &le;0 für unbegrenzt)
	 * @see #getConditionType()
	 * @see #setTimeRepeatCount(int)
	 */
	public int getTimeRepeatCount() {
		return timeRepeatCount;
	}

	/**
	 * Stellt die Anzahl der Wiederholungen der Ereignisauslösungen ein.
	 * @param timeRepeatCount	Anzahl der Wiederholungen der Ereignisauslösungen (Werte &le;0 für unbegrenzt)
	 * @see #getConditionType()
	 * @see #getTimeRepeatCount()
	 */
	public void setTimeRepeatCount(int timeRepeatCount) {
		conditionType=ConditionType.CONDITION_TIME;
		this.timeRepeatCount=timeRepeatCount;
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
	 * @return	ID der Analogwertstation, an der ein Wert eingestellt werden soll
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
	 * Liefert im Fall, dass bei der Aktion Javascript- oder Java-Code ausgeführt werden soll, diesen Code.
	 * @return	Als Aktion Auszuführender Javascript- oder Java-Code
	 * @see ModelElementActionRecord#getActionType()
	 * @see ModelElementActionRecord#setScript(String)
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Stellt im Fall, dass bei der Aktion Javascript- oder Java-Code ausgeführt werden soll, diesen Code ein.
	 * @param script	Als Aktion Auszuführender Javascript-oder Java-Code
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
	 * Liefert den auszugebenden Sound.
	 * @return	Auszugebender Sound
	 * @see #setSound(String)
	 * @see #getSoundMaxSeconds()
	 */
	public String getSound() {
		return sound;
	}

	/**
	 * Stellt den auszugebenden Sound ein.
	 * @param sound	Auszugebender Sound
	 * @see #getSound()
	 * @see #setSoundMaxSeconds(int)
	 */
	public void setSound(final String sound) {
		actionType=ActionType.ACTION_SOUND;
		this.sound=(sound==null)?"":sound;
	}

	/**
	 * Stellt den auszugebenden Sound ein.
	 * @param sound	Auszugebender Sound
	 * @param soundMaxSeconds	Maximale Anzahl an Sekunden für die Sound-Ausgabe (oder ein Wert &le;0 für keine Beschränkung)
	 * @see #getSound()
	 * @see #setSoundMaxSeconds(int)
	 */
	public void setSound(final String sound, final int soundMaxSeconds) {
		actionType=ActionType.ACTION_SOUND;
		this.sound=(sound==null)?"":sound;
		this.soundMaxSeconds=soundMaxSeconds;
	}

	/**
	 * Liefert die maximale Anzahl an Sekunden, die {@link #getSound()} ausgegeben werden soll.
	 * @return	Maximale Anzahl an Sekunden für die Sound-Ausgabe (oder ein Wert &le;0 für keine Beschränkung)
	 * @see #setSoundMaxSeconds(int)
	 * @see #getSound()
	 */
	public int getSoundMaxSeconds() {
		return soundMaxSeconds;
	}

	/**
	 * Stellt die maximale Anzahl an Sekunden, die {@link #getSound()} ausgegeben werden soll, ein.
	 * @param soundMaxSeconds	Maximale Anzahl an Sekunden für die Sound-Ausgabe (oder ein Wert &le;0 für keine Beschränkung)
	 * @see #getSoundMaxSeconds()
	 * @see #setSound(String)
	 */
	public void setSoundMaxSeconds(final int soundMaxSeconds) {
		actionType=ActionType.ACTION_SOUND;
		this.soundMaxSeconds=soundMaxSeconds;
	}

	/**
	 * Stellt ein, das die Aktion darin bestehen soll, die Simulation zu beenden.
	 */
	public void setStopSimulation() {
		actionType=ActionType.ACTION_STOP;
	}

	/**
	 * Speichert die Eigenschaften des Datensatzes als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element node;
		parent.appendChild(node=doc.createElement(Language.trPrimary("Surface.Action.XML.Record")));
		node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Active"),active?"1":"0");

		String type;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Typ der Bedingung */
			switch (conditionType) {
			case CONDITION_TIME: type=Language.trPrimary("Surface.Action.XML.Record.ConditionType.Time"); break;
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
		case ACTION_STOP: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.Stopp"); break;
		case ACTION_SOUND: type=Language.trPrimary("Surface.Action.XML.Record.ActionType.PlaySound"); break;
		default: type=""; break;
		}
		node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.ActionType"),type);

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_TIME:
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Time.Initial"),NumberTools.formatSystemNumber(timeInitial));
				node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Time.Interval"),NumberTools.formatSystemNumber(timeRepeat));
				if (timeRepeatCount>0) node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Time.RepeatCount"),""+timeRepeatCount);
				break;
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
		case ACTION_STOP:
			/* Keine weiteren Einstellungen */
			break;
		case ACTION_SOUND:
			node.setTextContent(sound);
			if (soundMaxSeconds>0) node.setAttribute(Language.trPrimary("Surface.Action.XML.Record.Action.SoundMaxSeconds"),""+soundMaxSeconds);
			break;
		}
	}

	/**
	 * Lädt die Einstellungen des Datensatzes aus einem xml-Knoten
	 * @param node	xml-Element, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final String activeString=Language.trAllAttribute("Surface.Action.XML.Record.Active",node);
		if (activeString.equals("0")) active=false;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Typ der Bedingung */
			final String conditionTypeString=Language.trAllAttribute("Surface.Action.XML.Record.ConditionType",node);
			if (Language.trAll("Surface.Action.XML.Record.ConditionType.Time",conditionTypeString)) conditionType=ConditionType.CONDITION_TIME;
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
		if (Language.trAll("Surface.Action.XML.Record.ActionType.Stopp",actionTypeString)) actionType=ActionType.ACTION_STOP;
		if (Language.trAll("Surface.Action.XML.Record.ActionType.PlaySound",actionTypeString)) actionType=ActionType.ACTION_SOUND;

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_TIME:
				final Double D1=NumberTools.getNotNegativeDouble(Language.trAllAttribute("Surface.Action.XML.Record.Time.Initial",node));
				if (D1==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Action.XML.Record.Time.Initial"),node.getNodeName(),node.getParentNode().getNodeName());
				timeInitial=D1;
				final Double D2=NumberTools.getPositiveDouble(Language.trAllAttribute("Surface.Action.XML.Record.Time.Interval",node));
				if (D2==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Action.XML.Record.Time.Interval"),node.getNodeName(),node.getParentNode().getNodeName());
				timeRepeat=D2;
				final Integer I=NumberTools.getInteger(Language.trAllAttribute("Surface.Action.XML.Record.Time.RepeatCount",node));
				if (I!=null && I>0) timeRepeatCount=I; else timeRepeatCount=-1;
				break;
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
		case ACTION_STOP:
			/* Keine weiteren Einstellungen */
			break;
		case ACTION_SOUND:
			sound=node.getTextContent();
			final Long L=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.Action.XML.Record.Action.SoundMaxSeconds",node));
			if (L!=null) soundMaxSeconds=L.intValue();
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

		if (!active) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Action.EnabledStatus"),Language.tr("ModelDescription.Action.EnabledStatus.IsDisabled"),level);
		}

		if (actionMode==ActionMode.TRIGGER_AND_ACTION) {
			/* Bedingung */
			switch (conditionType) {
			case CONDITION_TIME:
				descriptionBuilder.addProperty(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Time.Initial"),TimeTools.formatExactLongTime(timeInitial),level);
				if (timeRepeatCount!=1) {
					descriptionBuilder.addProperty(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Time.Interval"),TimeTools.formatExactLongTime(timeRepeat),level);
				}
				if (timeRepeatCount>0) {
					if (timeRepeatCount>1) descriptionBuilder.addProperty(Language.tr("Surface.Action.Dialog.Info.Time.Repeat"),""+timeRepeatCount,level);
				} else {
					descriptionBuilder.addProperty(Language.tr("Surface.Action.Dialog.Info.Time.Repeat"),Language.tr("Surface.Action.Dialog.Info.Time.Repeat.Unlimited"),level);
				}
				break;
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
		case ACTION_STOP:
			s=Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.EndSimulation");
			break;
		case ACTION_SOUND:
			s=Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.PlaySound")+": "+sound;
			break;
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Action.Action"),s,level+1);
	}

	/**
	 * Sucht einen Text in den Daten dieses Datensatzes.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		/* Ursache */
		if (actionMode==ActionMode.TRIGGER_AND_ACTION) switch (conditionType) {
		case CONDITION_TIME:
			searcher.testDouble(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Time.Initial"),timeInitial,newTimeInitial->{if(newTimeInitial>=0) timeInitial=newTimeInitial;});
			if (timeRepeatCount!=1) {
				searcher.testDouble(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Time.Interval"),timeRepeat,newTimeRepeat->{if(newTimeRepeat>0) timeRepeat=newTimeRepeat;});
			}
			if (timeRepeatCount>0) {
				searcher.testInteger(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Time.LimitRepetitions"),timeRepeatCount,newTimeRepeatCount->{if(newTimeRepeatCount>0) timeRepeatCount=newTimeRepeatCount;});
			}
			break;
		case CONDITION_CONDITION:
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition"),condition,newCondition->{condition=newCondition;});
			searcher.testDouble(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.MinDistance"),conditionMinDistance,newConditionMinDistance->{if(newConditionMinDistance>0) conditionMinDistance=newConditionMinDistance;});
			break;
		case CONDITION_THRESHOLD:
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdExpression"),thresholdExpression,newThresholdExpression->{thresholdExpression=newThresholdExpression;});
			searcher.testDouble(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdValue"),thresholdValue,newThresholdValue->{thresholdValue=newThresholdValue;});
			break;
		case CONDITION_SIGNAL:
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Signal"),conditionSignal,newConditionSignal->{conditionSignal=newConditionSignal;});
			break;
		}

		/* Wirkung */
		switch (actionType) {
		case ACTION_ASSIGN:
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Variable"),assignVariable,newAssignVariable->{assignVariable=newAssignVariable;});
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Expression"),assignExpression,newAssignExpression->{assignExpression=newAssignExpression;});
			break;
		case ACTION_SIGNAL:
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Signal.Name"),signalName,newSignalName->{signalName=newSignalName;});
			break;
		case ACTION_ANALOG_VALUE:
			if (searcher.isTestIDs()) {
				searcher.testInteger(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Element.ID"),analogID);
			}
			searcher.testString(station,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Expression"),analogValue);
			break;
		case ACTION_SCRIPT:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.Script"),script,newScript->{script=newScript;});
			break;
		case ACTION_STOP:
			/* Keine zu prüfenden Einstellungen */
			break;
		case ACTION_SOUND:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.Sound"),sound,newSound->{sound=newSound; soundMaxSeconds=-1;});
			break;
		}
	}
}