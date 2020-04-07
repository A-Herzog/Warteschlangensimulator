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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import simulator.coreelements.RunElementAnalogProcessing;
import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.runmodel.SimulationData;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import ui.modeleditor.elements.ModelElementTankFlowData;

/**
 * Laufzeitdaten eines <code>RunElementTank</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementTank
 * @see RunElementAnalogProcessingData
 */
public class RunElementTankData extends RunElementAnalogProcessingData {
	/**
	 * Kapazität
	 */
	private final double capacity;

	/**
	 * Initialer maximalem Durchfluss (pro Sekunde) pro Ventil
	 */
	private final double[] initialValveValues;

	/**
	 * Maximaler Durchfluss (pro Sekunde) pro Ventil
	 */
	private double[] valveValues;

	/**
	 * Bis wann ist das Ventil bereits belegt?
	 */
	private long[] valveTimes;

	private final Semaphore lock;
	private final List<RunElementTankFlow> outgoing;
	private final List<RunElementTankFlow> incoming;

	/**
	 * Konstruktor der Klasse
	 * @param station	Zugehöriges RunElement
	 * @param capacity	Kapazität des Tanks
	 * @param initialValveValues	Initialer maximalem Durchfluss (pro Sekunde) pro Ventil
	 * @param statistics	Statistikobjekt, in dem die Größe des analogen Wertes erfasst wird
	 */
	public RunElementTankData(final RunElementAnalogProcessing station, final double capacity, final double[] initialValveValues, final StatisticsTimeAnalogPerformanceIndicator statistics) {
		super(station,statistics);

		this.capacity=capacity;
		this.initialValveValues=initialValveValues;
		valveValues=Arrays.copyOf(initialValveValues,initialValveValues.length);
		valveTimes=new long[valveValues.length];
		Arrays.fill(valveTimes,0);

		outgoing=new ArrayList<>();
		incoming=new ArrayList<>();
		lock=new Semaphore(1);
	}

	@Override
	protected void init(final SimulationData simData) {
		valveValues=Arrays.copyOf(initialValveValues,initialValveValues.length);
		valveTimes=new long[valveValues.length];
		Arrays.fill(valveTimes,0);
	}

	@Override
	protected void updateValue(SimulationData simData) {
		lock.acquireUninterruptibly();
		try {

			/* Flüsse verarbeiten */
			for (RunElementTankFlow flow: outgoing) flow.processFlow(simData);
			for (RunElementTankFlow flow: incoming) flow.processFlow(simData);

			int index;

			/* Prüfen, ob auslaufende Flüsse abgeschlossen werden können */
			index=0;
			while (index<outgoing.size()) if (outgoing.get(index).flowDone) {
				endOutgoingFlow(index,simData);
			} else {
				index++;
			}

			/* Prüfen, ob einlaufende Flüsse abgeschlossen werden können */
			index=0;
			while (index<incoming.size()) if (incoming.get(index).flowDone) {
				endIncomingFlow(index,simData);
			} else {
				index++;
			}
		} finally {
			lock.release();
		}
	}

	/**
	 * Liefert den aktuellen maximalen Durchfluss an den Ventilen
	 * @return	Aktueller maximaler Durchfluss an den Ventilen
	 */
	public double[] getValveValues() {
		return valveValues;
	}

	/**
	 * Stellt den aktuellen Wert ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param value	Neuer Wert
	 */
	@Override
	public void setValue(final SimulationData simData, double value) {
		if (value<0) value=0;
		if (value>capacity) value=capacity;
		super.setValue(simData,value);
	}

	/**
	 * Stellt den maximalen Durchfluss an einem Ventil ein
	 * @param nr	0-basierte Nummer des Ventils
	 * @param maxFlow	Maximaler Fluss (Wert &ge; 0)
	 */
	public void setValveMaxFlow(final int nr, final double maxFlow) {
		if (nr<0 || nr>=valveValues.length) return;
		valveValues[nr]=FastMath.max(0,maxFlow);
	}

	@Override
	protected boolean isRateZero() {
		lock.acquireUninterruptibly();
		try {
			return incoming.size()==0 && outgoing.size()==0;
		} finally {
			lock.release();
		}
	}

	/**
	 * Fügt einen Fluss zu der Liste der einlaufenden Flüsse hinzu
	 * @param flow	Neuer Fluss
	 * @param simData	Simulationsdatenobjekt
	 */
	public void addIncomingFlow(final RunElementTankFlow flow, final SimulationData simData) {
		lock.acquireUninterruptibly();
		try {
			incoming.add(flow);
			rateActivated(simData);
		} finally {
			lock.release();
		}
	}

	/**
	 * Fügt einen Fluss zu der Liste der auslaufenden Flüsse hinzu
	 * @param flow	Neuer Fluss
	 * @param simData	Simulationsdatenobjekt
	 */
	public void addOutgoingFlow(final RunElementTankFlow flow, final SimulationData simData) {
		lock.acquireUninterruptibly();
		try {
			outgoing.add(flow);
			rateActivated(simData);
		} finally {
			lock.release();
		}
	}

	private void removeOutgoingFlowNotify(final RunElementTankFlow flow, final SimulationData simData) {
		lock.acquireUninterruptibly();
		try {
			outgoing.remove(flow);
		} finally {
			lock.release();
		}
	}

	private void removeIncomingFlowNotify(final RunElementTankFlow flow, final SimulationData simData) {
		lock.acquireUninterruptibly();
		try {
			incoming.remove(flow);
		} finally {
			lock.release();
		}
	}

	private void endIncomingFlow(final int index, final SimulationData simData) {
		/* Aus eigener Liste austragen */
		final RunElementTankFlow flow=incoming.remove(index);

		/* Aus anderer Liste austragen */
		if (flow.source!=null) flow.source.getData(simData).removeOutgoingFlowNotify(flow,simData);

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.Tank"),String.format(Language.tr("Simulation.Log.Tank.StopInfo"),flow.logInfo(),station.name));
	}

	private void endOutgoingFlow(final int index, final SimulationData simData) {
		/* Aus eigener Liste austragen */
		final RunElementTankFlow flow=outgoing.remove(index);

		/* Aus anderer Liste austragen */
		if (flow.destination!=null) flow.destination.getData(simData).removeIncomingFlowNotify(flow,simData);

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.Tank"),String.format(Language.tr("Simulation.Log.Tank.StopInfo"),flow.logInfo(),station.name));
	}

	/**
	 * Das Stations-Element muss {@link SignalListener} implementieren und bei einer
	 * Nachricht darüber diese Methode aufrufen.
	 * @param simData	Simulationsdatenobjekt
	 * @param signalName	Name des ausgelösten Signals
	 */
	public void signalNotify(final SimulationData simData, final String signalName) {
		lock.acquireUninterruptibly();
		try {

			int index;

			index=0;
			while (index<incoming.size()) {
				final RunElementTankFlow flow=incoming.get(index);
				if (flow.stopCondition==ModelElementTankFlowData.FlowStopCondition.STOP_BY_SIGNAL && flow.stopSignal.equals(signalName)) {
					endIncomingFlow(index,simData);
				} else {
					index++;
				}
			}

			index=0;
			while (index<outgoing.size()) {
				final RunElementTankFlow flow=outgoing.get(index);
				if (flow.stopCondition==ModelElementTankFlowData.FlowStopCondition.STOP_BY_SIGNAL && flow.stopSignal.equals(signalName)) {
					endOutgoingFlow(index,simData);
				} else {
					index++;
				}
			}
		} finally {
			lock.release();
		}
	}

	/**
	 * Bestimmt den maximalen Ausfluss über ein Ventil innerhalb einer bestimmten Zeit
	 * @param valveNr	0-basierende Ventilnummer
	 * @param timeDeltaMS	Zeitdauer
	 * @param simData	Simulationsdatenobjekt
	 * @return	Maximaler Ausfluss durch das Ventil in der angegebenen Zeit
	 */
	public double getMaxFlowOut(final int valveNr, final long timeDeltaMS, final SimulationData simData) {
		if (valveTimes[valveNr]>=simData.currentTime) return 0; /* Ventil ist belegt */

		final double maxFlow=valveValues[valveNr]*(timeDeltaMS/1000.0);

		valveTimes[valveNr]=simData.currentTime;
		return FastMath.min(value,maxFlow);
	}

	/**
	 * Bestimmt den maximalen Zufluss über ein Ventil innerhalb einer bestimmten Zeit
	 * @param valveNr	0-basierende Ventilnummer
	 * @param timeDeltaMS	Zeitdauer
	 * @param simData	Simulationsdatenobjekt
	 * @return	Maximaler Zufluss durch das Ventil in der angegebenen Zeit
	 */
	public double getMaxFlowIn(final int valveNr, final long timeDeltaMS, final SimulationData simData) {
		if (valveTimes[valveNr]>=simData.currentTime) return 0; /* Ventil ist belegt */

		final double maxFlow=valveValues[valveNr]*(timeDeltaMS/1000.0);

		valveTimes[valveNr]=simData.currentTime;
		return FastMath.min(capacity-value,maxFlow);
	}

	/**
	 * Ändert den Füllstand des Tanks um einen bestimmten Wert
	 * @param delta	Änderung des Füllstands
	 */
	public void changeValueByFlow(final double delta) {
		value+=delta;
		if (value<0) value=0;
		if (value>capacity) value=capacity;
	}

	/**
	 * Liefert eine Liste mit den aktuellen auslaufenden Flüssen
	 * @return	Liste mit den aktuellen auslaufenden Flüssen
	 */
	public RunElementTankFlow[] getOutgoingFlows() {
		lock.acquireUninterruptibly();
		try {
			return outgoing.toArray(new RunElementTankFlow[0]);
		} finally {
			lock.release();
		}
	}

	/**
	 * Liefert eine Liste mit den aktuellen einlaufenden Flüssen
	 * @return	Liste mit den aktuellen einlaufenden Flüssen
	 */
	public RunElementTankFlow[] getIncomingFlows() {
		lock.acquireUninterruptibly();
		try {
			return incoming.toArray(new RunElementTankFlow[0]);
		} finally {
			lock.release();
		}
	}
}