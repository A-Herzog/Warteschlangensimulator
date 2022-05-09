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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.elements.RunElementAnalogValue;
import simulator.elements.RunElementTank;
import simulator.elements.StateChangeListener;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Basisklasse für die Laufzeitelemente, die einen analogen Wert abbilden.
 * @author Alexander Herzog
 * @see RunElementAnalogValue
 * @see RunElementTank
 */
public abstract class RunElementAnalogProcessing extends RunElement implements StateChangeListener {
	/**
	 * Anfangswert
	 */
	protected double initialValue;

	/**
	 * Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen
	 */
	private long analogNotifiyMS;

	/**
	 * Konstruktor der Klasse <code>RunElement</code>
	 * @param element	Modell-Element aus dem ID und Farbe ausgelesen werden
	 * @param name	Name der Station
	 */
	public RunElementAnalogProcessing(final ModelElementBox element, final String name) {
		super(element,name);
	}

	/**
	 * Prüft und lädt den Sekundenwert für den Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen.
	 * @param analogNotifiy	Sekundenwert für den Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected final String loadAnalogNotify(final double analogNotifiy) {
		if (analogNotifiy<=0) return String.format(Language.tr("Simulation.Creator.AnalogNotifyDistance"),id,NumberTools.formatNumber(analogNotifiy));
		analogNotifiyMS=FastMath.round(analogNotifiy*1000);
		return null;
	}

	/**
	 * Prüft beim Erstellen des Laufzeit-Elements, ob der Sekundenwert für den Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen gültig ist.
	 * @param analogNotifiy	Prüft den Sekundenwert für den Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen.
	 * @param id	ID des zugehörigen Elements
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected final RunModelCreatorStatus testAnalogNotify(final double analogNotifiy, final int id) {
		if (analogNotifiy<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogNotifyDistance"),id,NumberTools.formatNumber(analogNotifiy)),RunModelCreatorStatus.Status.ANALOG_NOTIFY_NEGATIVE);
		return null;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Analog-Wert-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Analog-Wert-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public boolean systemStateChangeNotify(final SimulationData simData) {
		if (simData.currentTime==0) {
			/* Beim Start der Simulation das erste Update-Event anlegen */
			((RunElementAnalogProcessingData)getData(simData)).processUpdateEvent(simData,true);
		} else {
			simData.runData.removeStateChangeListener(this);
		}

		return false;
	}

	/**
	 * Liefert den Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen.<br>
	 * Wird von {@link RunElementAnalogProcessingData} verwendet.
	 * @return	Abstand (in MS) zwischen zwei Analog-Value-Update-Notify-Ereignissen
	 * @see RunElementAnalogProcessingData
	 */
	public long getAnalogNotifiyMS() {
		return analogNotifiyMS;
	}

	/**
	 * Liefert den Anfangswert.<br>
	 * Wird von {@link RunElementAnalogProcessingData} verwendet.
	 * @return	Anfangswert
	 * @see RunElementAnalogProcessingData
	 */
	public double getInitialValue() {
		return initialValue;
	}

	/**
	 * Liefert das zu diesem Analog-Wert-Objekt gehörende Statistikobjekt
	 * @param simData	Simulationsdatenobjekt
	 * @return	Statistikobjekt für diesen Analog-Wert
	 */
	protected final StatisticsTimeAnalogPerformanceIndicator getAnalogStatistics(final SimulationData simData) {
		return (StatisticsTimeAnalogPerformanceIndicator)simData.statistics.analogStatistics.get(name);
	}
}
