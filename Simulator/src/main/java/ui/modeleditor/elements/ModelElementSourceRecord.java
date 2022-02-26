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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import parser.CalcSystem;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse hält die konkreten Daten für eine Kundenquelle vor.
 * @author Alexander Herzog
 * @see ModelElementSource
 * @see ModelElementSourceMulti
 */
public final class ModelElementSourceRecord implements Cloneable {
	/**
	 * Kann der Datensatz deaktiviert werden?
	 */
	private final boolean hasActivation;

	/**
	 * Gibt an, ob das Element selbst Ankünfte produziert (normale Quelle)
	 * oder nur von außen getriggert wird (Zerteilen-Station).
	 */
	private final boolean hasOwnArrivals;

	/**
	 * @see ModelElementSourceRecord#getNextMode()
	 */
	public enum NextMode {
		/** Zwischenankunftszeit per Wahrscheinlichkeitsverteilung bestimmen */
		NEXT_DISTRIBUTION,

		/** Zwischenankunftszeit per Ausdruck bestimmen */
		NEXT_EXPRESSION,

		/** Zwischenankunftszeit über Zeitplan bestimmen */
		NEXT_SCHEDULE(true),

		/** Freigabe des nächsten Kunden, wenn eine bestimmte Bedingung erfüllt ist (und ein Mindestabstand eingehalten wurde) */
		NEXT_CONDITION,

		/** Freigabe bei Über- oder Unterschreiten eines Schwellenwertes */
		NEXT_THRESHOLD,

		/** Freigabe des nächsten Kunden aus Basis von Signalen */
		NEXT_SIGNAL,

		/** Mehrere Ausdrücke zur Festlegung der jeweils absoluten Anzahl an Ankünften in einzelnen Intervallen */
		NEXT_INTERVAL_EXPRESSIONS(true);

		/**
		 * Setzt dieser Modus den Single-Core-Modus voraus?
		 */
		public final boolean isSingleCoreOnlyMode;

		/**
		 * Konstruktor des Enum
		 */
		NextMode() {
			this.isSingleCoreOnlyMode=false;
		}

		/**
		 * Konstruktor des Enum
		 * @param isSingleCoreOnlyMode	Setzt dieser Modus den Single-Core-Modus voraus?
		 */
		NextMode(final boolean isSingleCoreOnlyMode) {
			this.isSingleCoreOnlyMode=isSingleCoreOnlyMode;
		}
	}

	/**
	 * Ist der Datensatz aktiv?
	 */
	private boolean active;

	/**
	 * Namen des Datensatzes
	 * @see #getName()
	 * @see #setName(String)
	 * @see #hasName()
	 */
	private String name;

	/**
	 * Verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Art der Bestimmung der Zwischenankunftszeiten
	 * @see #getNextMode()
	 */
	private NextMode nextMode;

	/**
	 * Verteilung der Zwischenankunftszeiten
	 * @see #getInterarrivalTimeDistribution()
	 * @see #setInterarrivalTimeDistribution(AbstractRealDistribution)
	 */
	private AbstractRealDistribution distribution;

	/**
	 * Ausdruck gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 * @see #getInterarrivalTimeExpression()
	 * @see #setInterarrivalTimeExpression(String)
	 */
	private String expression;

	/**
	 * Namen des Zeitplans gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 * @see #getInterarrivalTimeSchedule()
	 * @see #setInterarrivalTimeSchedule(String)
	 */
	private String schedule;

	/**
	 * Bedingung gemäß derer weitere Kundenankünfte ausgelöst werden sollen
	 * @see #getArrivalCondition()
	 * @see #setArrivalCondition(String, double)
	 */
	private String condition;

	/**
	 * Mindestabstand zwischen Kundenankünften, die über eine Bedingung ausgelöst werden sollen
	 * @see #getArrivalConditionMinDistance()
	 * @see #setArrivalCondition(String, double)
	 */
	private double conditionMinDistance;

	/**
	 * Liste der Signalnamen, die eine Kundenankunft auslösen sollen
	 * @see #getArrivalSignalNames()
	 * @see #setSignalMode()
	 */
	private final List<String> signalNames;

	/**
	 * Zu prüfender Schwellenwertausdruck
	 * @see #getThresholdExpression()
	 * @see #setThresholdExpression(String)
	 */
	private String thresholdExpression;

	/**
	 * Schwellenwert gegen den der Schwellenwertausdruck abgeglichen werden soll
	 * @see #getThresholdValue()
	 * @see #setThresholdValue(double)
	 */
	private double thresholdValue;

	/**
	 * Gibt an, ob eine Kundenankunft beim Über- oder beim Unterschreiten des Schwellenwertes ausgelöst werden soll.
	 * @see #isThresholdDirectionUp()
	 * @see #setThresholdDirectionUp(boolean)
	 */
	private boolean thresholdDirectionUp;

	/**
	 * Zeitdauer für ein Intervall in {@link #intervalExpressions}
	 * @see #getIntervalExpressionsIntervalTime()
	 * @see #setIntervalExpressionsIntervalTime(int)
	 */
	private int intervalExpressionsIntervalTime;

	/**
	 * Ausdrücke zur Berechnung der Ankünfte pro Intervall
	 * @see #getIntervalExpressions()
	 */
	private final List<String> intervalExpressions;

	/**
	 * Ankunfts-Batch-Größe oder <code>null</code>, wenn es mehrere verschiedene Batch-Größen geben soll
	 * @see #getBatchSize()
	 * @see #setBatchSize(String)
	 */
	private String batchSize; /* wenn hier null eingetragen wird, gelten die <code>batchSizeRates</code> */

	/**
	 * Ankunfts-Batch-Verteilung oder <code>null</code> wenn es eine fixe Batch-Größe gibt
	 * @see #getMultiBatchSize()
	 * @see #setMultiBatchSize(double[])
	 */
	private double[] batchSizeRates; /* erster Eintrag steht für Batch-Größe==1 */

	/**
	 * Gesamtanzahl an Ankunftsereignissen (-1 für unendlich viele)
	 * @see #getMaxArrivalCount()
	 * @see #setMaxArrivalCount(long)
	 */
	private long maxArrivalCount;

	/**
	 * Gesamtanzahl an Kundenankünften (-1 für unendlich viele)
	 * @see #getMaxArrivalClientCount()
	 * @see #setMaxArrivalClientCount(long)
	 */
	private long maxArrivalClientCount;

	/**
	 * Zeitpunkt am dem die erste Zwischenankunftszeit beginnt
	 * @see #getArrivalStart()
	 * @see #setArrivalStart(double)
	 */
	private double arrivalsStart;

	/**
	 * Datensatz für Zahlenzuweisungen
	 * @see #getSetRecord()
	 */
	private final ModelElementSetRecord setRecord;

	/**
	 * Datensatz für Textzuweisungen
	 * @see #getStringRecord()
	 */
	private final ModelElementAssignStringRecord stringRecord;

	/**
	 * Listener, die aufgerufen werden, wenn es eine Änderung im Datensatz gibt.
	 * @see #fireChanged()
	 */
	private List<Runnable> changeListener;

	/**
	 * Wird nicht vom Objekt selbst verwendet (außer bei clone/copyFrom: Dann wird das Feld aus dem Name-Feld der Quelle befüllt).<br>
	 * Kann dazu verwendet werden, den bisherigen Namen des Datensatzes zu speichern, um
	 * so auf Änderungen reagieren zu können.
	 */
	public String saveName=null;

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceRecord</code>
	 * @param hasName	Gibt an, ob der Datensatz sich selbst um seinen Namen kümmern soll (beim Laden und Speichern).
	 * @param hasActivation	Kann der Datensatz deaktiviert werden?
	 * @param hasOwnArrivals	Gibt an, ob diese Quelle von sich aus Kunden generiert oder nur von außen angestoßen wird
	 */
	public ModelElementSourceRecord(final boolean hasName, final boolean hasActivation, final boolean hasOwnArrivals) {
		this.hasActivation=hasActivation;
		this.hasOwnArrivals=hasOwnArrivals;
		if (hasName) name=""; else name=null;
		saveName=name;

		active=true;
		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		nextMode=NextMode.NEXT_DISTRIBUTION;
		distribution=new ExponentialDistribution(null,60,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		expression="";
		schedule="";
		condition="";
		conditionMinDistance=1;
		signalNames=new ArrayList<>();
		thresholdExpression="";
		thresholdValue=0.0;
		thresholdDirectionUp=true;
		intervalExpressionsIntervalTime=1800;
		intervalExpressions=new ArrayList<>();
		batchSize="1";
		batchSizeRates=null;
		maxArrivalCount=-1;
		maxArrivalClientCount=-1;
		arrivalsStart=0;

		setRecord=new ModelElementSetRecord();
		stringRecord=new ModelElementAssignStringRecord();

		changeListener=new ArrayList<>();
	}

	/**
	 * Benachrichtigt alle {@link #changeListener} Listener
	 * über Änderungen an dem Datensatz.
	 * @see #changeListener
	 * @see #addChangeListener(Runnable)
	 * @see #removeChangeListener(Runnable)
	 */
	private void fireChanged() {
		for (Runnable listener: changeListener) listener.run();
	}

	/**
	 * Fügt ein <code>Runnable</code>-Objekt zu der Liste der Listener, die aufgerufen werden,
	 * wenn es eine Änderung im Datensatz gibt.
	 * @param listener	Zu benachrichtigende Listener
	 */
	public void addChangeListener(final Runnable listener) {
		if (changeListener.indexOf(listener)<0) changeListener.add(listener);
	}

	/**
	 * Entfernt ein <code>Runnable</code>-Objekt aus der Liste der Listener, die aufgerufen werden,
	 * wenn es eine Änderung im Datensatz gibt.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte.
	 */
	public boolean removeChangeListener(final Runnable listener) {
		final int index=changeListener.indexOf(listener);
		if (index<0) return false;
		changeListener.remove(listener);
		return true;
	}

	/**
	 * Ist der Datensatz aktiv?
	 * @return	Ist der Datensatz aktiv?
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Stellt ein, ob der Datensatz aktiv sein soll.
	 * @param active	Ist der Datensatz aktiv?
	 */
	public void setActive(boolean active) {
		this.active=active;
	}

	/**
	 * Gibt an, ob das Objekt seinen Namen selbst verwaltet (beim Laden und Speichern)
	 * @return	Gibt <code>true</code> zurück, wenn das Objekt seinen Namen selbst verwaltet
	 * @see ModelElementSourceRecord#getName()
	 * @see ModelElementSourceRecord#setName(String)
	 */
	public boolean hasName() {
		return name!=null;
	}

	/**
	 * Gibt an, ob diese Quelle von sich aus Kunden generiert oder nur von außen angestoßen wird.
	 * @return	Gibt <code>true</code> zurück, wenn die Quelle von sich aus Kunden generieren können soll.
	 */
	public boolean hasOwnArrivals() {
		return hasOwnArrivals;
	}

	/**
	 * Liefert den aktuellen Namen des Datensatzes
	 * @return	Namen des Datensatzes
	 * @see ModelElementSourceRecord#hasName()
	 * @see ModelElementSourceRecord#setName(String)
	 */
	public String getName() {
		return (name!=null)?name:"";
	}

	/**
	 * Stellt, sofern das Objekt seinen Namen selbst verwaltet, diesen ein
	 * @param name	Neuer Name des Datensatzes
	 * @see ModelElementSourceRecord#hasName()
	 * @see ModelElementSourceRecord#getName()
	 */
	public void setName(final String name) {
		if (this.name==null) return;
		if (name==null) this.name=""; else this.name=name;
	}

	/**
	 * Liefert die aktuelle Ankunfts-Batch-Größe
	 * @return Ankunfts-Batch-Größe oder <code>null</code>, wenn es mehrere verschiedene Batch-Größen geben soll
	 * @see ModelElementSourceRecord#getMultiBatchSize()
	 */
	public String getBatchSize() {
		return batchSize;
	}

	/**
	 * Liefert die aktuelle Verteilung der Batch-Größen
	 * @return	Ankunfts-Batch-Verteilung oder <code>null</code> wenn es eine fixe Batch-Größe gibt
	 * @see ModelElementSourceRecord#getBatchSize()
	 */
	public double[] getMultiBatchSize() {
		if (batchSizeRates==null) return null;
		return Arrays.copyOf(batchSizeRates,batchSizeRates.length);
	}

	/**
	 * Stellt die Ankunfts-Batch-Größe ein.
	 * @param batchSize	Neue Ankunfts-Batch-Größe
	 */
	public void setBatchSize(final String batchSize) {
		if (batchSize!=null) {
			this.batchSize=batchSize;
			batchSizeRates=null;
			fireChanged();
		}
	}

	/**
	 * Stellt die Ankunfts-Batch-Größenverteilung ein.
	 * @param batchSizes Neue Ankunfts-Batch-Größenverteilung
	 */
	public void setMultiBatchSize(final double[] batchSizes) {
		batchSize=null;
		batchSizeRates=Arrays.copyOf(batchSizes,batchSizes.length);
		fireChanged();
	}

	/**
	 * Mittlere Anzahl an Ankünften pro Aktivierung des Ankunftsdatensatzes
	 * @return	Mittlere Anzahl an Ankünften
	 */
	public int getAverageBatchSize() {
		if (batchSize!=null) {
			final Double D=CalcSystem.calcSimple(batchSize);
			if (D==null || D<1) return 1;
			return (int)Math.round(D);
		}

		if (batchSizeRates!=null) {
			int count=0;
			int sum=0;
			for (int i=0;i<batchSizeRates.length;i++) {
				count+=batchSizeRates[i];
				sum+=(i+1)*batchSizeRates[i];
			}
			return Math.max((int)Math.round(((double)sum)/count),1);
		}

		return 1;
	}

	/**
	 * Liefert die Gesamtanzahl an Ankunftsereignissen
	 * @return Gesamtanzahl an Ankunftsereignissen (-1 für unendlich viele)
	 */
	public long getMaxArrivalCount() {
		return maxArrivalCount;
	}

	/**
	 * Stellt die Gesamtanzahl an Ankunftsereignissen ein
	 * @param maxArrivalCount Neue Gesamtanzahl an Ankunftsereignissen (-1 für unendlich viele)
	 */
	public void setMaxArrivalCount(final long maxArrivalCount) {
		if (maxArrivalCount<0) {
			this.maxArrivalCount=-1;
		} else {
			this.maxArrivalCount=maxArrivalCount;
			this.maxArrivalClientCount=-1;
		}
		fireChanged();
	}

	/**
	 * Liefert die Gesamtanzahl an Kundenankünften
	 * @return Gesamtanzahl an Kundenankünften (-1 für unendlich viele)
	 */
	public long getMaxArrivalClientCount() {
		return maxArrivalClientCount;
	}

	/**
	 * Stellt die Gesamtanzahl an Kundenankünften ein
	 * @param maxArrivalClientCount Neue Gesamtanzahl an Kundenankünften (-1 für unendlich viele)
	 */
	public void setMaxArrivalClientCount(final long maxArrivalClientCount) {
		if (maxArrivalClientCount<0) {
			this.maxArrivalClientCount=-1;
		} else {
			this.maxArrivalCount=-1;
			this.maxArrivalClientCount=maxArrivalClientCount;
		}
		fireChanged();
	}

	/**
	 * Gibt an, ob die Zwischenankunftszeiten gemäß einer Verteilung, eines Ausdrucks oder auf Basis eines Zeitplans bestimmt werden sollen
	 * @return	Art der Bestimmung der Zwischenankunftszeiten
	 * @see ModelElementSourceRecord.NextMode
	 */
	public NextMode getNextMode() {
		return nextMode;
	}

	/**
	 * Liefert die Verteilung der Zwischenankunftszeiten
	 * @return Verteilung der Zwischenankunftszeiten
	 */
	public AbstractRealDistribution getInterarrivalTimeDistribution() {
		return distribution;
	}

	/**
	 * Liefert den Ausdruck gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 * @return Ausdruck gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 */
	public String getInterarrivalTimeExpression() {
		return expression;
	}

	/**
	 * Liefert den Namen des Zeitplans gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 * @return Namen des Zeitplans gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 */
	public String getInterarrivalTimeSchedule() {
		return schedule;
	}

	/**
	 * Liefert die Bedingung gemäß derer weitere Kundenankünfte ausgelöst werden sollen
	 * @return	Bedingung gemäß derer weitere Kundenankünfte ausgelöst werden sollen
	 * @see ModelElementSourceRecord#getArrivalConditionMinDistance()
	 */
	public String getArrivalCondition() {
		return condition;
	}

	/**
	 * Liefert den zeitlichen Mindestabstand zwischen Kundenankünften, die über eine Bedingung ausgelöst werden sollen
	 * @return	Mindestabstand zwischen Kundenankünften, die über eine Bedingung ausgelöst werden sollen
	 * @see ModelElementSourceRecord#getArrivalCondition()
	 */
	public double getArrivalConditionMinDistance() {
		return conditionMinDistance;
	}

	/**
	 * Liefert die Liste der Signalnamen, die eine Kundenankunft auslösen sollen.
	 * @return	Liste der Signalnamen, die eine Kundenankunft auslösen sollen
	 * @see ModelElementSourceRecord#setSignalMode()
	 */
	public List<String> getArrivalSignalNames() {
		return signalNames;
	}

	/**
	 * Stellt den Modus "Kundenankunft bei Signal" ein.
	 * @see ModelElementSourceRecord#getArrivalSignalNames()
	 */
	public void setSignalMode() {
		nextMode=NextMode.NEXT_SIGNAL;
	}

	/**
	 * Stellt die Verteilung der Zwischenankunftszeiten ein.
	 * @param distribution	Neue Verteilung der Zwischenankunftszeiten
	 */
	public void setInterarrivalTimeDistribution(final AbstractRealDistribution distribution) {
		if (distribution!=null) {
			this.distribution=DistributionTools.cloneDistribution(distribution);
			nextMode=NextMode.NEXT_DISTRIBUTION;
			fireChanged();
		}
	}

	/**
	 * Stellt den Ausdruck gemäß dessen die Zwischenankunftszeiten bestimmt werden sollen ein.
	 * @param expression	Ausdruck gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 */
	public void setInterarrivalTimeExpression(final String expression) {
		if (expression!=null) {
			this.expression=expression.trim();
			nextMode=NextMode.NEXT_EXPRESSION;
			fireChanged();
		}
	}

	/**
	 * Stellt den Namen des Zeitplans gemäß dessen die Zwischenankunftszeiten bestimmt werden sollen ein.
	 * @param schedule	Namen des Zeitplans gemäß dessen die Zwischenankunftszeiten bestimmt werden
	 */
	public void setInterarrivalTimeSchedule(final String schedule) {
		if (schedule!=null) {
			this.schedule=schedule.trim();
			nextMode=NextMode.NEXT_SCHEDULE;
			fireChanged();
		}
	}

	/**
	 * Stellt die Bedingung, gemäß derer weitere Kundenankünfte ausgelöst werden sollen, ein.
	 * @param condition	Bedingung, gemäß derer weitere Kundenankünfte ausgelöst werden sollen
	 * @param conditionMinDistance	Mindestabstand zwischen zwei per Bedinung ausgelösten Kundenankünften
	 */
	public void setArrivalCondition(final String condition, final double conditionMinDistance) {
		if (condition!=null) {
			this.condition=condition.trim();
			this.conditionMinDistance=conditionMinDistance;
			nextMode=NextMode.NEXT_CONDITION;
			fireChanged();
		}
	}

	/**
	 * Liefert den zu prüfenden Schwellenwertausdruck.
	 * @return	Zu prüfender Schwellenwertausdruck
	 * @see ModelElementSourceRecord#setThresholdExpression(String)
	 */
	public String getThresholdExpression() {
		if (thresholdExpression==null) return "";
		return thresholdExpression;
	}

	/**
	 * Stellt den zu prüfenden Schwellenwertausdruck ein.
	 * @param thresholdExpression	Zu prüfender Schwellenwertausdruck
	 * @see ModelElementSourceRecord#getThresholdExpression()
	 * @see ModelElementSourceRecord#setThreshold(String, double, boolean)
	 */
	public void setThresholdExpression(final String thresholdExpression) {
		if (thresholdExpression!=null) this.thresholdExpression=thresholdExpression; else this.thresholdExpression="";
		nextMode=NextMode.NEXT_THRESHOLD;
	}

	/**
	 * Liefert den Schwellenwert gegen der der Schwellenwertausdruck abgeglichen werden soll.
	 * @return	Schwellenwert gegen den der Schwellenwertausdruck abgeglichen werden soll
	 * @see ModelElementSourceRecord#setThresholdValue(double)
	 */
	public double getThresholdValue() {
		return thresholdValue;
	}

	/**
	 * Stellt den Schwellenwert gegen der der Schwellenwertausdruck abgeglichen werden soll ein.
	 * @param thresholdValue	Schwellenwert gegen der der Schwellenwertausdruck abgeglichen werden soll
	 * @see ModelElementSourceRecord#getThresholdValue()
	 * @see ModelElementSourceRecord#setThreshold(String, double, boolean)
	 */
	public void setThresholdValue(final double thresholdValue) {
		this.thresholdValue=thresholdValue;
		nextMode=NextMode.NEXT_THRESHOLD;
	}

	/**
	 * Gibt an, ob eine Kundenankunft beim Über- oder beim Unterschreiten des Schwellenwertes ausgelöst werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn eine Ankunft beim Überschreiten des Schwellenwertes ausgelöst werden soll (sonst beim Unterschreiten).
	 * @see ModelElementSourceRecord#setThresholdDirectionUp(boolean)
	 */
	public boolean isThresholdDirectionUp() {
		return thresholdDirectionUp;
	}

	/**
	 * Stellt ein, ob eine Kundenankunft beim Über- oder beim Unterschreiten des Schwellenwertes ausgelöst werden soll.
	 * @param thresholdDirectionUp	Der Wert <code>true</code> bedeutet, dass eine Ankunft beim Überschreiten des Schwellenwertes ausgelöst werden soll (sonst beim Unterschreiten).
	 * @see ModelElementSourceRecord#isThresholdDirectionUp()
	 * @see ModelElementSourceRecord#setThreshold(String, double, boolean)
	 */
	public void setThresholdDirectionUp(final boolean thresholdDirectionUp) {
		this.thresholdDirectionUp=thresholdDirectionUp;
		nextMode=NextMode.NEXT_THRESHOLD;
	}

	/**
	 * Stellt alle Werte für die schwellenwertabhängigen Kundenankünfte ein.
	 * @param thresholdExpression	Zu prüfender Schwellenwertausdruck
	 * @param thresholdValue	Schwellenwert gegen der der Schwellenwertausdruck abgeglichen werden soll
	 * @param thresholdDirectionUp	Der Wert <code>true</code> bedeutet, dass eine Ankunft beim Überschreiten des Schwellenwertes ausgelöst werden soll (sonst beim Unterschreiten).
	 * @see ModelElementSourceRecord#setThresholdExpression(String)
	 * @see ModelElementSourceRecord#setThresholdValue(double)
	 * @see ModelElementSourceRecord#setThresholdDirectionUp(boolean)
	 */
	public void setThreshold(final String thresholdExpression, final double thresholdValue, final boolean thresholdDirectionUp) {
		if (thresholdExpression!=null) this.thresholdExpression=thresholdExpression; else this.thresholdExpression="";
		this.thresholdValue=thresholdValue;
		this.thresholdDirectionUp=thresholdDirectionUp;
		nextMode=NextMode.NEXT_THRESHOLD;
	}

	/**
	 * Liefert die Zeitdauer für ein Intervall in {@link #getIntervalExpressions()}.
	 * @return	Zeitdauer für ein Intervall
	 * @see #setIntervalExpressionsIntervalTime(int)
	 */
	public int getIntervalExpressionsIntervalTime() {
		return intervalExpressionsIntervalTime;
	}

	/**
	 * Stellt	die Zeitdauer für ein Intervall in {@link #getIntervalExpressions()} ein.
	 * @param intervalExpressionsIntervalTime	Zeitdauer für ein Intervall
	 * @see #getIntervalExpressionsIntervalTime()
	 */
	public void setIntervalExpressionsIntervalTime(int intervalExpressionsIntervalTime) {
		this.intervalExpressionsIntervalTime=intervalExpressionsIntervalTime;
		nextMode=NextMode.NEXT_INTERVAL_EXPRESSIONS;
	}

	/**
	 * Liefert die Liste der Ausdrücke zur Berechnung der Ankünfte pro Intervall.
	 * @return	Liste der Ausdrücke zur Berechnung der Ankünfte pro Intervall
	 */
	public List<String> getIntervalExpressions() {
		return intervalExpressions;
	}

	/**
	 * Stellt den Modus "Ausdrücke zur Festlegung der jeweils absoluten Anzahl an Ankünften pro Intervallen" ein.
	 * @see ModelElementSourceRecord#getArrivalSignalNames()
	 */
	public void setIntervalExpressionsMode() {
		nextMode=NextMode.NEXT_INTERVAL_EXPRESSIONS;
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=timeBase;
	}

	/**
	 * Liefert den Zeitpunkt, ab dem die Zwischenankunftszeiten berechnet werden sollen.<br>
	 * Der Wert ist gemäß der gewählten Zeitbasis zu interpretieren, ist also nicht notwendig ein Sekundenwert.
	 * @return	Zeitpunkt am dem die erste Zwischenankunftszeit beginnt
	 */
	public double getArrivalStart() {
		return arrivalsStart;
	}

	/**
	 * Stellt den Zeitpunkt ein, ab dem die Zwischenankunftszeiten berechnet werden sollen.<br>
	 * Der Wert ist gemäß der gewählten Zeitbasis zu interpretieren, ist also nicht notwendig ein Sekundenwert.
	 * @param arrivalsStart	Zeitpunkt am dem die erste Zwischenankunftszeit beginnt
	 */
	public void setArrivalStart(final double arrivalsStart) {
		this.arrivalsStart=arrivalsStart;
	}

	/**
	 * Liefert den Datensatz für Zahlenzuweisungen innerhalb des Quellendatensatzes
	 * @return	Datensatz für Zahlenzuweisungen
	 */
	public ModelElementSetRecord getSetRecord() {
		return setRecord;
	}

	/**
	 * Liefert den Datensatz für Textzuweisungen innerhalb des Quellendatensatzes
	 * @return	Datensatz für Textzuweisungen
	 */
	public ModelElementAssignStringRecord getStringRecord() {
		return stringRecord;
	}

	/**
	 * Überprüft, ob der Datensatz mit dem angegebenen Datensatz inhaltlich identisch ist.
	 * @param record	Datensatz mit dem dieser Datensatz verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Datensätze identisch sind.
	 */
	public boolean equalsRecord(final ModelElementSourceRecord record) {
		if (hasActivation) {
			if (active!=record.active) return false;
		}

		if (hasOwnArrivals) {
			if (record.timeBase!=timeBase) return false;
			if (nextMode!=record.nextMode) return false;
			switch (nextMode) {
			case NEXT_DISTRIBUTION:
				if (!DistributionTools.compare(distribution,record.distribution)) return false;
				break;
			case NEXT_EXPRESSION:
				if (!expression.equals(record.expression)) return false;
				break;
			case NEXT_SCHEDULE:
				if (!schedule.equals(record.schedule)) return false;
				break;
			case NEXT_CONDITION:
				if (!condition.equals(record.condition)) return false;
				if (conditionMinDistance!=record.conditionMinDistance) return false;
				break;
			case NEXT_THRESHOLD:
				if (!Objects.deepEquals(thresholdExpression,record.thresholdExpression)) return false;
				if (thresholdValue!=record.thresholdValue) return false;
				if (thresholdDirectionUp!=record.thresholdDirectionUp) return false;
				break;
			case NEXT_SIGNAL:
				if (!Objects.deepEquals(signalNames,record.signalNames)) return false;
				break;
			case NEXT_INTERVAL_EXPRESSIONS:
				if (intervalExpressionsIntervalTime!=record.intervalExpressionsIntervalTime) return false;
				if (!Objects.deepEquals(intervalExpressions,record.intervalExpressions)) return false;
				break;
			}
		}

		if (batchSize==null) {
			if (record.batchSize!=null) return false;
			final double[] a1=batchSizeRates;
			final double[] a2=record.batchSizeRates;
			if (a1==null || a2==null) return false;
			for (int i=0;i<Math.max(a1.length,a2.length);i++) {
				final double x=(i>=a1.length)?0:a1[i];
				final double y=(i>=a2.length)?0:a2[i];
				if (x!=y) return false;
			}
		} else {
			if (record.batchSize==null) return false;
			if (!batchSize.equals(record.batchSize)) return false;
		}

		if (hasOwnArrivals) {
			if (record.maxArrivalCount!=maxArrivalCount) return false;
		}

		if (hasOwnArrivals) {
			if (record.maxArrivalClientCount!=maxArrivalClientCount) return false;
		}

		if (hasOwnArrivals) {
			if (record.arrivalsStart!=arrivalsStart) return false;
		}

		if (!record.setRecord.equalsModelElementSetRecord(setRecord)) return false;

		if (!record.stringRecord.equalsModelElementAssignStringRecord(stringRecord)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Datensatz auf diesen.
	 * @param record	Datensatz, von dem alle Einstellungen übernommen werden sollen
	 */
	public void copyDataFrom(ModelElementSourceRecord record) {
		/* hasActivation und hasOwnArrivals werden bereits im Konstruktor gesetzt */
		active=record.active;
		name=record.name;
		saveName=name;
		timeBase=record.timeBase;
		nextMode=record.nextMode;
		if (record.distribution!=null) distribution=DistributionTools.cloneDistribution(record.distribution);
		expression=record.expression;
		schedule=record.schedule;
		condition=record.condition;
		conditionMinDistance=record.conditionMinDistance;
		signalNames.clear();
		signalNames.addAll(record.signalNames);
		thresholdExpression=record.thresholdExpression;
		thresholdValue=record.thresholdValue;
		thresholdDirectionUp=record.thresholdDirectionUp;
		intervalExpressionsIntervalTime=record.intervalExpressionsIntervalTime;
		intervalExpressions.clear();
		intervalExpressions.addAll(record.intervalExpressions);
		batchSize=record.batchSize;
		if (batchSize==null && record.batchSizeRates!=null) batchSizeRates=Arrays.copyOf(record.batchSizeRates,record.batchSizeRates.length);
		maxArrivalCount=record.maxArrivalCount;
		maxArrivalClientCount=record.maxArrivalClientCount;
		arrivalsStart=record.arrivalsStart;

		setRecord.copyDataFrom(record.setRecord);
		stringRecord.copyDataFrom(record.stringRecord);
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSourceRecord clone() {
		final ModelElementSourceRecord record=new ModelElementSourceRecord(hasName(),hasActivation,hasOwnArrivals);
		record.copyDataFrom(this);
		return record;
	}

	/**
	 * Benachrichtigt das Element, dass sich der Name eines Signals
	 * geändert hat. Ob dies für das Element eine Bedeutung hat, prüft es selber.
	 * @param oldName	Alter Name des Signals
	 * @param newName	Neuer Name des Signals
	 */
	public void signalRenamed(final String oldName, final String newName) {
		if (nextMode!=NextMode.NEXT_SIGNAL) return;

		for (int i=0;i<signalNames.size();i++) {
			if (signalNames.get(i).equals(oldName)) signalNames.set(i,newName);
		}
	}

	/**
	 * Speichert den Datensatz in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	xml-Knoten in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element node) {
		Element sub;

		if (hasActivation && !active) {
			sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Active"));
			node.appendChild(sub);
			sub.setTextContent("0");
		}

		if (name!=null && !name.trim().isEmpty()) {
			sub=doc.createElement(Language.trPrimary("Surface.XML.Element.Name"));
			node.appendChild(sub);
			sub.setTextContent(name);
		}

		if (hasOwnArrivals) {
			switch (nextMode) {
			case NEXT_DISTRIBUTION:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Distribution")));
				if (distribution!=null) sub.setTextContent(DistributionTools.distributionToString(distribution));
				sub.setAttribute(Language.trPrimary("Surface.Source.XML.Distribution.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
				if (maxArrivalCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Distribution.Count"),""+maxArrivalCount);
				if (maxArrivalClientCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Distribution.ClientCount"),""+maxArrivalClientCount);
				break;
			case NEXT_EXPRESSION:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Expression")));
				sub.setTextContent(expression);
				sub.setAttribute(Language.trPrimary("Surface.Source.XML.Expression.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
				if (maxArrivalCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Expression.Count"),""+maxArrivalCount);
				if (maxArrivalClientCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Expression.ClientCount"),""+maxArrivalClientCount);
				break;
			case NEXT_SCHEDULE:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Schedule")));
				sub.setTextContent(schedule);
				if (maxArrivalCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Schedule.Count"),""+maxArrivalCount);
				if (maxArrivalClientCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Schedule.ClientCount"),""+maxArrivalClientCount);
				break;
			case NEXT_CONDITION:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Condition")));
				sub.setTextContent(condition);
				sub.setAttribute(Language.trPrimary("Surface.Source.XML.Condition.MinDistance"),NumberTools.formatSystemNumber(conditionMinDistance));
				if (maxArrivalCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Condition.Count"),""+maxArrivalCount);
				if (maxArrivalClientCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Condition.ClientCount"),""+maxArrivalClientCount);
				break;
			case NEXT_THRESHOLD:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Threshold")));
				sub.setTextContent(thresholdExpression);
				sub.setAttribute(Language.trPrimary("Surface.Source.XML.Threshold.Value"),NumberTools.formatSystemNumber(thresholdValue));
				sub.setAttribute(Language.trPrimary("Surface.Source.XML.Threshold.Direction"),thresholdDirectionUp?Language.trPrimary("Surface.Source.XML.Threshold.Direction.Up"):Language.trPrimary("Surface.Source.XML.Threshold.Direction.Down"));
				if (maxArrivalCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Threshold.Count"),""+maxArrivalCount);
				if (maxArrivalClientCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Threshold.ClientCount"),""+maxArrivalClientCount);
				break;
			case NEXT_SIGNAL:
				for (String name: signalNames) {
					node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Signal")));
					sub.setTextContent(name);
					if (maxArrivalCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Signal.Count"),""+maxArrivalCount);
					if (maxArrivalClientCount>0) sub.setAttribute(Language.trPrimary("Surface.Source.XML.Signal.ClientCount"),""+maxArrivalClientCount);
				}
				break;
			case NEXT_INTERVAL_EXPRESSIONS:
				boolean first=true;
				for (String expression: intervalExpressions) {
					node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.IntervalExpression")));
					sub.setTextContent(expression);
					if (first) {
						sub.setAttribute(Language.trPrimary("Surface.Source.XML.IntervalExpression.IntervalTime"),""+intervalExpressionsIntervalTime);
						first=false;
					}
				}
				break;
			}
		}

		if (batchSize!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Batch")));
			sub.setAttribute(Language.trPrimary("Surface.Source.XML.Batch.Size"),batchSize);
		} else {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Batch")));
			final StringBuilder sb=new StringBuilder();
			for (double d: batchSizeRates) {if (sb.length()>0) sb.append(";"); sb.append(NumberTools.formatSystemNumber(d));}
			sub.setAttribute(Language.trPrimary("Surface.Source.XML.Batch.Size"),sb.toString());
		}

		if (hasOwnArrivals) {
			if (arrivalsStart>0 && nextMode!=NextMode.NEXT_SCHEDULE && nextMode!=NextMode.NEXT_CONDITION) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Source.XML.Expression.ArrivalStart")));
				sub.setTextContent(NumberTools.formatSystemNumber(arrivalsStart));
			}
		}

		setRecord.saveToXML(doc,node);
		stringRecord.saveToXML(doc,node);
	}

	/**
	 * Liest ein Datum aus einem XML-Knoten ein
	 * @param name	Name des XML-Knoten
	 * @param content	Textinhalt des XML-Knotens
	 * @param node	Zu interpretierender XML-Knoten
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 * @see #loadPropertyFromXML(Element)
	 */
	private String loadProperty(final String name, final String content, final Element node) {
		if (hasActivation) {
			if (Language.trAll("Surface.Source.XML.Active",name)) {
				final String activeString=content.trim();
				if (activeString.equals("0")) active=false;
				return null;
			}
		}

		if (this.name!=null) {
			if (Language.trAll("Surface.XML.Element.Name",name)) {
				this.name=content.trim();
				return null;
			}
		}

		if (Language.trAll("Surface.Source.XML.Distribution",name)) {
			distribution=DistributionTools.distributionFromString(content,3000);
			if (distribution==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			nextMode=NextMode.NEXT_DISTRIBUTION;
			final String timeBaseName=Language.trAllAttribute("Surface.Source.XML.Distribution.TimeBase",node);
			timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
			final String countString=Language.trAllAttribute("Surface.Source.XML.Distribution.Count",node);
			if (!countString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(countString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Distribution.Count"),name,node.getParentNode().getNodeName());
				maxArrivalCount=L;
			}
			final String clientCountString=Language.trAllAttribute("Surface.Source.XML.Distribution.ClientCount",node);
			if (!clientCountString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(clientCountString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Distribution.ClientCount"),name,node.getParentNode().getNodeName());
				maxArrivalClientCount=L;
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Expression",name)) {
			expression=content;
			nextMode=NextMode.NEXT_EXPRESSION;
			final String timeBaseName=Language.trAllAttribute("Surface.Source.XML.Expression.TimeBase",node);
			timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
			final String countString=Language.trAllAttribute("Surface.Source.XML.Expression.Count",node);
			if (!countString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(countString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Expression.Count"),name,node.getParentNode().getNodeName());
				maxArrivalCount=L;
			}
			final String clientCountString=Language.trAllAttribute("Surface.Source.XML.Expression.ClientCount",node);
			if (!clientCountString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(clientCountString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Expression.ClientCount"),name,node.getParentNode().getNodeName());
				maxArrivalClientCount=L;
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Schedule",name)) {
			schedule=content;
			nextMode=NextMode.NEXT_SCHEDULE;
			final String countString=Language.trAllAttribute("Surface.Source.XML.Schedule.Count",node);
			if (!countString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(countString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Schedule.Count"),name,node.getParentNode().getNodeName());
				maxArrivalCount=L;
			}
			final String clientCountString=Language.trAllAttribute("Surface.Source.XML.Schedule.ClientCount",node);
			if (!clientCountString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(clientCountString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Schedule.ClientCount"),name,node.getParentNode().getNodeName());
				maxArrivalClientCount=L;
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Condition",name)) {
			condition=content;
			nextMode=NextMode.NEXT_CONDITION;
			final String minDistanceString=Language.trAllAttribute("Surface.Source.XML.Condition.MinDistance",node);
			if (!minDistanceString.isEmpty()) {
				final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(minDistanceString));
				if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Condition.MinDistance"),name,node.getParentNode().getNodeName());
				conditionMinDistance=D;
			}
			final String countString=Language.trAllAttribute("Surface.Source.XML.Condition.Count",node);
			if (!countString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(countString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Condition.Count"),name,node.getParentNode().getNodeName());
				maxArrivalCount=L;
			}
			final String clientCountString=Language.trAllAttribute("Surface.Source.XML.Condition.ClientCount",node);
			if (!clientCountString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(clientCountString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Condition.ClientCount"),name,node.getParentNode().getNodeName());
				maxArrivalClientCount=L;
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Threshold",name)) {
			if (!content.trim().isEmpty()) {
				thresholdExpression=content;
				nextMode=NextMode.NEXT_THRESHOLD;
				final String thresholdValueString=Language.trAllAttribute("Surface.Source.XML.Threshold.Value",node);
				if (!thresholdValueString.isEmpty()) {
					final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(thresholdValueString));
					if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Threshold.Value"),name,node.getParentNode().getNodeName());
					thresholdValue=D;
				}
				final String thresholdDirectionUpString=Language.trAllAttribute("Surface.Source.XML.Threshold.Direction",node);
				thresholdDirectionUp=Language.trAll("Surface.Source.XML.Threshold.Direction.Up",thresholdDirectionUpString);
			}
			final String countString=Language.trAllAttribute("Surface.Source.XML.Threshold.Count",node);
			if (!countString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(countString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Threshold.Count"),name,node.getParentNode().getNodeName());
				maxArrivalCount=L;
			}
			final String clientCountString=Language.trAllAttribute("Surface.Source.XML.Threshold.ClientCount",node);
			if (!clientCountString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(clientCountString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Threshold.ClientCount"),name,node.getParentNode().getNodeName());
				maxArrivalClientCount=L;
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Signal",name)) {
			if (!content.trim().isEmpty()) {
				nextMode=NextMode.NEXT_SIGNAL;
				signalNames.add(content);
			}
			final String countString=Language.trAllAttribute("Surface.Source.XML.Signal.Count",node);
			if (!countString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(countString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Signal.Count"),name,node.getParentNode().getNodeName());
				maxArrivalCount=L;
			}
			final String clientCountString=Language.trAllAttribute("Surface.Source.XML.Signal.ClientCount",node);
			if (!clientCountString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(clientCountString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Signal.ClientCount"),name,node.getParentNode().getNodeName());
				maxArrivalClientCount=L;
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.IntervalExpression",name)) {
			if (!content.trim().isEmpty()) {
				nextMode=NextMode.NEXT_INTERVAL_EXPRESSIONS;
				intervalExpressions.add(content);
			}
			final String intervalTimeString=Language.trAllAttribute("Surface.Source.XML.IntervalExpression.IntervalTime",node);
			if (!intervalTimeString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(intervalTimeString);
				if (L==null || L==0) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Signal.Count"),name,node.getParentNode().getNodeName());
				intervalExpressionsIntervalTime=L.intValue();
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Batch",name)) {
			final String size=Language.trAllAttribute("Surface.Source.XML.Batch.Size",node);
			if (!size.isEmpty()) {
				if (size.indexOf(';')>=0) {
					batchSize=null;
					String[] parts=size.split(";");
					List<Double> sizes=new ArrayList<>();
					for (String s: parts) {
						final Double D=NumberTools.getNotNegativeDouble(s);
						if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Source.XML.Batch.Size"),name,node.getParentNode().getNodeName());
						sizes.add(D);
					}
					batchSizeRates=new double[sizes.size()];
					for (int i=0;i<sizes.size();i++) batchSizeRates[i]=sizes.get(i);
				} else {
					batchSize=size;
					batchSizeRates=null;
				}
			}
			return null;
		}

		if (Language.trAll("Surface.Source.XML.Expression.ArrivalStart",name)) {
			Double D=NumberTools.getNotNegativeDouble(content);
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			arrivalsStart=D;
			return null;
		}

		if (ModelElementSetRecord.isSetNode(node)) {
			final String error=setRecord.loadXMLNode(node);
			if (error!=null) return error;
			return null;
		}

		if (ModelElementAssignStringRecord.isSetNode(node)) {
			final String error=stringRecord.loadXMLNode(node);
			if (error!=null) return error;
			return null;
		}

		return null;
	}

	/**
	 * Liest ein Datum aus einem XML-Knoten ein
	 * @param node	Zu interpretierender XML-Knoten
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadPropertyFromXML(final Element node) {
		return loadProperty(node.getNodeName(),node.getTextContent(),node);
	}

	/**
	 * Lädt eine Einstellungen des Datensatzes aus einem xml-Element.
	 * @param node	xml-Element, aus dem die Daten geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final String error=loadPropertyFromXML((Element)l.item(i));
			if (error!=null) return error;
		}

		return null;
	}

	/**
	 * Liefert ein XML-Pfad für die Anzahl an Ankünften.
	 * @return	XML-Pfad für die Anzahl an Ankünften (oder <code>null</code>, wenn ein Pfad in der aktuellen Konfiguration nicht ermittelt werden kann)
	 */
	public String getArrivalCountXMLPath() {
		if (!hasOwnArrivals) return null;
		switch (nextMode) {
		case NEXT_DISTRIBUTION:
			return Language.trPrimary("Surface.Source.XML.Distribution")+"->["+Language.trPrimary("Surface.Source.XML.Distribution.Count")+"]";
		case NEXT_EXPRESSION:
			return Language.trPrimary("Surface.Source.XML.Expression")+"->["+Language.trPrimary("Surface.Source.XML.Expression.Count")+"]";
		case NEXT_SCHEDULE:
			return Language.trPrimary("Surface.Source.XML.Schedule")+"->["+Language.trPrimary("Surface.Source.XML.Schedule.Count")+"]";
		case NEXT_CONDITION:
			return Language.trPrimary("Surface.Source.XML.Condition")+"->["+Language.trPrimary("Surface.Source.XML.Condition.Count")+"]";
		case NEXT_THRESHOLD:
			return Language.trPrimary("Surface.Source.XML.Threshold")+"->["+Language.trPrimary("Surface.Source.XML.Threshold.Count")+"]";
		case NEXT_SIGNAL:
			return Language.trPrimary("Surface.Source.XML.Signal")+"->["+Language.trPrimary("Surface.Source.XML.Signal.Count")+"]";
		default:
			return null;
		}
	}

	/**
	 * Liefert ein XML-Pfad für die Anzahl an Kundenankünften.
	 * @return	XML-Pfad für die Anzahl an Kundenankünften (oder <code>null</code>, wenn ein Pfad in der aktuellen Konfiguration nicht ermittelt werden kann)
	 */
	public String getArrivalClientCountXMLPath() {
		if (!hasOwnArrivals) return null;
		switch (nextMode) {
		case NEXT_DISTRIBUTION:
			return Language.trPrimary("Surface.Source.XML.Distribution")+"->["+Language.trPrimary("Surface.Source.XML.Distribution.ClientCount")+"]";
		case NEXT_EXPRESSION:
			return Language.trPrimary("Surface.Source.XML.Expression")+"->["+Language.trPrimary("Surface.Source.XML.Expression.ClientCount")+"]";
		case NEXT_SCHEDULE:
			return Language.trPrimary("Surface.Source.XML.Schedule")+"->["+Language.trPrimary("Surface.Source.XML.Schedule.ClientCount")+"]";
		case NEXT_CONDITION:
			return Language.trPrimary("Surface.Source.XML.Condition")+"->["+Language.trPrimary("Surface.Source.XML.Condition.ClientCount")+"]";
		case NEXT_THRESHOLD:
			return Language.trPrimary("Surface.Source.XML.Threshold")+"->["+Language.trPrimary("Surface.Source.XML.Threshold.ClientCount")+"]";
		case NEXT_SIGNAL:
			return Language.trPrimary("Surface.Source.XML.Signal")+"->["+Language.trPrimary("Surface.Source.XML.Signal.ClientCount")+"]";
		default:
			return null;
		}
	}

	/**
	 * Fügt die Beschreibung für die Daten dieses Objekts als Eigenschaft zu der Beschreibung hinzu
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	public void buildDescriptionProperty(final ModelDescriptionBuilder descriptionBuilder) {
		final StringBuilder sb=new StringBuilder();

		if (hasOwnArrivals) {
			/* Nächster Kunde */
			switch (nextMode) {
			case NEXT_DISTRIBUTION:
				sb.append(Language.tr("ModelDescription.Arrival.Distribution"));
				sb.append(": ");
				sb.append(ModelDescriptionBuilder.getDistributionInfo(distribution));
				break;
			case NEXT_EXPRESSION:
				sb.append(Language.tr("ModelDescription.Arrival.Expression"));
				sb.append(": ");
				sb.append(expression);
				break;
			case NEXT_SCHEDULE:
				sb.append(Language.tr("ModelDescription.Arrival.Schedule"));
				sb.append(": ");
				sb.append(schedule);
				break;
			case NEXT_CONDITION:
				sb.append(Language.tr("ModelDescription.Arrival.Condition"));
				sb.append(": ");
				sb.append(condition);
				sb.append(" (");
				sb.append(Language.tr("ModelDescription.Arrival.Condition.MinDistance"));
				sb.append(": ");
				sb.append(NumberTools.formatNumber(conditionMinDistance));
				sb.append(")");
				break;
			case NEXT_THRESHOLD:
				sb.append(Language.tr("ModelDescription.Arrival.Threshold"));
				sb.append(": ");
				sb.append(thresholdExpression);
				sb.append(" ");
				if (thresholdDirectionUp) Language.tr("ModelDescription.Arrival.Threshold.Up"); else Language.tr("ModelDescription.Arrival.Threshold.Down");
				sb.append(" ");
				sb.append(NumberTools.formatNumber(thresholdValue));
				break;
			case NEXT_SIGNAL:
				sb.append(Language.tr("ModelDescription.Arrival.Signal"));
				sb.append(": ");
				for (int i=0;i<signalNames.size();i++) {
					if (i>0) sb.append(", ");
					sb.append(signalNames.get(i));
				}
				break;
			case NEXT_INTERVAL_EXPRESSIONS:
				sb.append(Language.tr("ModelDescription.Arrival.IntervalExpressions")+"\n");
				sb.append(Language.tr("ModelDescription.Arrival.IntervalExpressions.IntervalTime")+": "+NumberTools.formatLong(intervalExpressionsIntervalTime)+"\n");
				for (int i=0;i<intervalExpressions.size();i++) {
					sb.append(""+(i+1)+": "+intervalExpressions.get(i)+"\n");
				}
				break;
			}
			sb.append("\n");

			/* Zeitbasis */
			if (nextMode==NextMode.NEXT_DISTRIBUTION || nextMode==NextMode.NEXT_EXPRESSION) {
				sb.append(ModelDescriptionBuilder.getTimeBase(timeBase));
				sb.append("\n");
			}
		}

		/* Batchgröße */
		final List<String> sizes=new ArrayList<>();
		if (batchSize!=null) {
			sizes.add(batchSize);
		} else {
			for (int i=0;i<batchSizeRates.length;i++) if (batchSizeRates[i]>0) sizes.add(""+(i+1));
		}
		if (sizes.size()>0) {
			if (sizes.size()>1 || !sizes.get(0).equals("1")) {
				sb.append((sizes.size()==1)?Language.tr("ModelDescription.Arrival.BatchSize.Fixed"):Language.tr("ModelDescription.Arrival.BatchSize.Multi"));
				sb.append(": ");
				for (int i=0;i<sizes.size();i++) {
					if (i>0) sb.append(", ");
					sb.append(sizes.get(i));
				}
				sb.append("\n");
			}
		}

		if (hasOwnArrivals) {
			/* Anzahl an Ankünften */
			if (maxArrivalCount>=0) {
				sb.append(Language.tr("ModelDescription.Arrival.NumberOfArrivals"));
				sb.append(": ");
				sb.append(NumberTools.formatLong(maxArrivalCount));
				sb.append("\n");
			}
			if (maxArrivalClientCount>=0) {
				sb.append(Language.tr("ModelDescription.Arrival.NumberOfClientArrivals"));
				sb.append(": ");
				sb.append(NumberTools.formatLong(maxArrivalClientCount));
				sb.append("\n");
			}

			/* Start der Ankünfte */
			if (arrivalsStart>0) {
				sb.append(Language.tr("ModelDescription.Arrival.ArrivalStart"));
				sb.append(": ");
				sb.append(NumberTools.formatNumber(arrivalsStart));
				sb.append("\n");
			}
		}

		/* Name für Eigenschaft bestimmen */
		final String propertyName;
		if (name==null)	{
			propertyName=Language.tr("ModelDescription.Arrival");
		} else {
			propertyName=Language.tr("ModelDescription.Arrival")+" \""+name+"\"";
		}

		/* Zuweisungen */
		for (String line: setRecord.getDescription()) {
			sb.append(line);
			sb.append("\n");
		}

		/* Textzuweisungen */
		for (String line: stringRecord.getDescription()) {
			sb.append(line);
			sb.append("\n");
		}

		/* Ergebnis ausgeben */
		descriptionBuilder.addProperty(propertyName,sb.toString(),1000);
	}

	/**
	 * Sucht einen Text in den Daten des Ankunftsdatensatzes.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		/* Name */
		if (hasName()) {
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.ArrivalRecord.Name"),name,newName->{name=newName;});
		}

		/* Abstände zwischen den Ankünften */
		switch (nextMode) {
		case NEXT_DISTRIBUTION:
			searcher.testDistribution(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Distribution"),distribution);
			break;
		case NEXT_EXPRESSION:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Expression"),expression,newExpression->{expression=newExpression;});
			break;
		case NEXT_SCHEDULE:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Schedule"),expression,newSchedule->{schedule=newSchedule;});
			break;
		case NEXT_CONDITION:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Condition"),condition,newCondition->{condition=newCondition;});
			searcher.testDouble(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Condition.MinDistance"),conditionMinDistance,newConditionMinDistance->{if (newConditionMinDistance>0) conditionMinDistance=newConditionMinDistance;});
			break;
		case NEXT_SIGNAL:
			for (int i=0;i<signalNames.size();i++) {
				final int index=i;
				searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Signal"),signalNames.get(index),newSignalName->signalNames.set(index,newSignalName));
			}
			break;
		case NEXT_THRESHOLD:
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Threshold.Expression"),thresholdExpression,newThresholdExpression->{thresholdExpression=newThresholdExpression;});
			searcher.testDouble(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.Threshold.Value"),thresholdValue,newThresholdValue->{thresholdValue=newThresholdValue;});
			break;
		case NEXT_INTERVAL_EXPRESSIONS:
			for (int i=0;i<intervalExpressions.size();i++) {
				final int index=i;
				searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.IntervalExpression"),intervalExpressions.get(index),newintervalExpression->intervalExpressions.set(index,newintervalExpression));
			}
			break;
		}

		/* Batch-Ankünfte */
		if (batchSize!=null) {
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.BatchSize"),batchSize,newBatchSize->{batchSize=newBatchSize;});
		} else {
			if (batchSizeRates!=null) for (int i=0;i<batchSizeRates.length;i++) {
				final int index=i;
				searcher.testDouble(station,String.format(Language.tr("Editor.DialogBase.Search.InterarrivalArrival.BatchSize.Rate"),i+1),batchSizeRates[i],newRate->{if (newRate>=0) batchSizeRates[index]=newRate;});
			}
		}

		/* Anzahl an Ankünften */
		if (maxArrivalCount>0) searcher.testLong(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.ArrivalCount"),maxArrivalCount,newMaxArrivalCount->{if (newMaxArrivalCount>0) maxArrivalCount=newMaxArrivalCount;});
		if (maxArrivalClientCount>0) searcher.testLong(station,Language.tr("Editor.DialogBase.Search.InterarrivalArrival.ArrivalClientCount"),maxArrivalClientCount,newMaxArrivalClientCount->{if (newMaxArrivalClientCount>0) maxArrivalClientCount=newMaxArrivalClientCount;});

		/* Zahlen-Zuweisungen */
		setRecord.search(searcher,station);

		/* Text-Zuweisungen */
		stringRecord.search(searcher,station);
	}
}