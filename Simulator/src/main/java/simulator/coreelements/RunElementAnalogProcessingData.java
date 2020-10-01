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
package simulator.coreelements;

import simulator.events.AnalogSystemChangeEvent;
import simulator.runmodel.SimulationData;
import statistics.StatisticsTimeAnalogPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementAnalogProcessing</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAnalogProcessing
 * @see RunElementData
 */
public abstract class RunElementAnalogProcessingData extends RunElementData {
	/**
	 * Anfangswert
	 */
	protected final double initialValue;

	/**
	 * Aktueller Wert
	 */
	protected double value;

	/**
	 * Zeitpunkt zu dem der Wert gilt.
	 */
	protected long valueTime;

	/**
	 * Statistik zur Erfassung der Werte
	 */
	private final StatisticsTimeAnalogPerformanceIndicator statistics;

	/**
	 * Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen
	 */
	private final long analogNotifiyMS;

	/**
	 * Wurde bereits ein Notify-Event eingeplant, aber noch nicht ausgel�st?
	 */
	private boolean notifyTriggered;

	/**
	 * Konstruktor der Klasse
	 * @param station	Zugeh�riges RunElement
	 * @param statistics	Statistikobjekt, in dem die Gr��e des analogen Wertes erfasst wird
	 */
	public RunElementAnalogProcessingData(final RunElementAnalogProcessing station, final StatisticsTimeAnalogPerformanceIndicator statistics) {
		super(station);
		this.initialValue=station.getInitialValue();
		this.statistics=statistics;
		this.analogNotifiyMS=station.getAnalogNotifiyMS();
		valueTime=-1;
	}

	/**
	 * Initialisiert (wenn n�tig) den Wert und ruft (ebenfalls wenn n�tig)
	 * die nutzerdefinierte Initialisierungsfunktion {@link RunElementAnalogProcessingData#init(SimulationData)} auf.
	 * @param simData	Simulationsdatenobjekt
	 */
	protected final void checkInit(final SimulationData simData) {
		if (valueTime<0 || valueTime>simData.currentTime) {
			valueTime=0;
			value=initialValue;
			init(simData);
		}
	}

	/**
	 * Erm�glicht den abgeleiteten Klassen nutzerdefinierte Initialisierungen
	 * beim Simulationsbeginn bzw. beim Beginn eines neues Tages.<br>
	 * Diese Methode wird passend von {@link RunElementAnalogProcessingData#checkInit(SimulationData)}
	 * aufgerufen. Sie muss nicht manuell aufgerufen werden.
	 * @param simData	Simulationsdatenobjekt
	 */
	protected void init(final SimulationData simData) {
	}

	private static final double toSec=1.0/1000.0;

	/**
	 * Erfasst eine Ver�nderung des analogen Wertes in der Statistik
	 * @param timeMS	Zeitpunkt (in MS), f�r den der Wert gilt
	 * @param value	Neuer Wert
	 */
	protected final void setStatisticValue(final long timeMS, final double value) {
		statistics.set(timeMS*toSec,value);
	}

	/**
	 * Aktualisiert die Statistik (z.B. am Simulationsende), f�hrt sonst keine Verarbeitungen durch.<br>
	 * Au�er bei besonderen Ereignissen wie dem Simulationsende muss diese Funktion nicht manuell aufgerufen werden.
	 * @param timeMS	Aktueller Zeitpunkt
	 */
	public void updateStatistics(final long timeMS) {
		setStatisticValue(timeMS,getValueNoUpdate());
	}

	private void triggerNextUpdateEvent(final SimulationData simData) {
		final AnalogSystemChangeEvent event=(AnalogSystemChangeEvent)simData.getEvent(AnalogSystemChangeEvent.class);
		event.init(simData.currentTime+analogNotifiyMS);
		event.analogProcessingData=this;
		simData.eventManager.addEvent(event);
		notifyTriggered=true;
	}

	/**
	 * Aktualisiert den Wert
	 * @param simData	Simulationsdatenobjekt
	 */
	protected abstract void updateValue(final SimulationData simData);

	/**
	 * Liefert den aktuellen Wert
	 * @param simData	Simulationsdatenobjekt
	 * @return	Aktueller Wert
	 */
	public final double getValue(final SimulationData simData) {
		final long time=simData.currentTime;
		checkInit(simData);

		if (valueTime<time) {
			if (!isRateZero()) updateValue(simData);
			valueTime=time;
			setStatisticValue(time,value);
		}

		return value;
	}

	/**
	 * Liefert den aktuellen Wert, ohne dabei im Hintergrund n�tigenfalls einen Update-Schritt durchzuf�hren
	 * @return	Aktueller Wert
	 * @see RunElementAnalogProcessingData#getValue(SimulationData)
	 */
	public final double getValueNoUpdate() {
		return value;
	}
	/**
	 * Stellt den aktuellen Wert ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param value	Neuer Wert
	 */
	public void setValue(final SimulationData simData, final double value) {
		checkInit(simData);

		final long time=simData.currentTime;
		this.value=value;
		valueTime=time;
		setStatisticValue(time,value);
	}

	/**
	 * F�hrt eine Wert-Aktualisierung durch, aber liefert nicht den Wert zur�ck,
	 * sondern die Information, ob sich der Wert ver�ndert hat.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zur�ck, wenn der Wert aktualisiert wurde.
	 */
	private boolean valueChanged(final SimulationData simData) {
		final double oldValue=value;
		final double newValue=getValue(simData);
		return Math.abs(newValue-oldValue)>10E-10;
	}

	/**
	 * Gibt an, ob momentan eine �nderungsrate ungleich 0 vorliegt.
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Zustand momentan �ndert.
	 */
	protected abstract boolean isRateZero();

	/**
	 * Meldet, das die �nderungsrate auf einen Wert ungleich 0 gesetzt wurde.
	 * (Damit wird das Update-Notify-System, sofern es pausierte, wieder in Gang gesetzt.
	 * @param simData	Simulationsdatenobjekt
	 */
	protected final void rateActivated(final SimulationData simData) {
		if (!notifyTriggered) triggerNextUpdateEvent(simData);
	}

	/**
	 * Reagiert auf ein Update-Notify-Event
	 * @param simData	Simulationsdatenobjekt
	 * @param firstEvent	Ist dies der erste Notify-Aufruf?
	 * @see AnalogSystemChangeEvent
	 */
	public final void processUpdateEvent(final SimulationData simData, final boolean firstEvent) {
		notifyTriggered=false;

		if (firstEvent) {
			/* Ganz zu Beginn einfach Event triggern. */
			triggerNextUpdateEvent(simData);
			return;
		}

		if (valueChanged(simData)) {
			triggerNextUpdateEvent(simData);
			simData.runData.fireStateChangeNotify(simData);
		} else {
			if (!isRateZero()) triggerNextUpdateEvent(simData);
		}
	}
}
