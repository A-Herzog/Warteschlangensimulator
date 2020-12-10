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

import java.util.Arrays;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;

/**
 * Laufzeitdaten eines {@link RunElementCounterBatch}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementCounterBatch
 * @see RunElementData
 */
public class RunElementCounterBatchData extends RunElementData {
	/**
	 * Name des Zählers
	 */
	private final String counterName;

	/**
	 * Zugehöriges Statistikobjekt
	 */
	private final StatisticsMultiPerformanceIndicator counterBatchStatistic;

	/**
	 * Einzelne Zähler für schnelleren Zugriff
	 */
	public StatisticsDataPerformanceIndicator[] indicators;

	/**
	 * Letzter Ankunftszeitpunkt (bezogen auf bestimmten Batch-Größen)
	 */
	private double[] lastArrivalByBatchSize;

	/**
	 * Letzter Ankunftszeitpunkt (bezogen auf einen Kunden)
	 */
	private long lastArrival=-1;

	/**
	 * Anzahl der Kunden, deren gleichzeitige Ankunft bereits gezählt wurde
	 */
	private int count=0;

	/**
	 * Konstruktor der Klasse {@link RunElementCounterBatchData}
	 * @param station	Station zu diesem Datenelement
	 * @param counterName	Name des Zählers
	 * @param counterBatchStatistic	Zugehöriges Statistikobjekt
	 */
	public RunElementCounterBatchData(final RunElement station, final String counterName, final StatisticsMultiPerformanceIndicator counterBatchStatistic) {
		super(station);
		this.counterName=counterName;
		this.counterBatchStatistic=counterBatchStatistic;
		indicators=null;
	}

	/**
	 * Um wie viel (mehr) soll das {@link #indicators}-Array bei jeder Vergrößerung
	 * wachsen (um so häufige Neuallokationen zu vermeiden)?
	 */
	private static final int ADDITIONAL_GROW_SIZE=10;

	/**
	 * Erfasst die Ankunft eines Batches
	 * @param time	Ankunftszeitpunkt (in Sekunden)
	 * @param batchSize	Batch-Größe
	 */
	private void logBatch(final double time, final int batchSize) {
		if (lastArrivalByBatchSize==null) lastArrivalByBatchSize=new double[batchSize+1+ADDITIONAL_GROW_SIZE];
		if (lastArrivalByBatchSize.length<=batchSize) lastArrivalByBatchSize=Arrays.copyOf(lastArrivalByBatchSize,batchSize+1+ADDITIONAL_GROW_SIZE);

		if (indicators==null) indicators=new StatisticsDataPerformanceIndicator[batchSize+1+ADDITIONAL_GROW_SIZE];
		if (indicators.length<=batchSize) indicators=Arrays.copyOf(indicators,batchSize+1+ADDITIONAL_GROW_SIZE);

		/* Allgemeine Zählung des Batches */
		if (indicators[0]==null) indicators[0]=(StatisticsDataPerformanceIndicator)counterBatchStatistic.get(counterName);
		if (lastArrivalByBatchSize[0]>0 && time>lastArrivalByBatchSize[0]) indicators[0].add(time-lastArrivalByBatchSize[0]);
		lastArrivalByBatchSize[0]=time;

		/* Batch-Größen-abhängige Zählung */
		if (indicators[batchSize]==null) indicators[batchSize]=(StatisticsDataPerformanceIndicator)counterBatchStatistic.get(counterName+" batch="+batchSize);
		if (lastArrivalByBatchSize[batchSize]>0 && time>lastArrivalByBatchSize[batchSize]) indicators[batchSize].add(time-lastArrivalByBatchSize[batchSize]);
		lastArrivalByBatchSize[batchSize]=time;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSec=1.0/1000.0;

	/**
	 * Erfasst eine Kundenankunft und zählt dann ggf. entsprechend einen Batch
	 * @param isWarmUp	Befinden wir uns noch in der Einschwingphase?
	 * @param time	Ankunftszeitpunkt
	 */
	public void logArrival(final boolean isWarmUp, final long time) {
		if (time<lastArrival || lastArrival==-1) {
			/* Neuer Tag */
			lastArrival=-1;
			count=0;
		}

		if (time==lastArrival && lastArrival!=-1) {
			/* Ankunft zählen */
			count++;
		} else {
			/* Neuer Zeitpunkt: Batch zählen, neuen Batch starten */
			if (!isWarmUp && count>0) logBatch(lastArrival*toSec,count);
			count=1;
		}

		lastArrival=time;
	}
}