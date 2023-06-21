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
package simulator.simparser.symbols;

import org.apache.commons.math3.util.FastMath;

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.coreelements.RunElementMultiQueueData;
import simulator.elements.RunElementAssign;
import simulator.elements.RunElementSource;
import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Im Falle von zwei Parametern:<br>
 * Liefert die aktuelle Anzahl an Kunden in der Teilwarteschlange <code>nr</code> (1-basierend) (2. Parameter) an Station id (1. Parameter).<br>
 * (Kann nur auf "Zusammenführen"-Elemente angewandt werden.)<br>
 * Im Falle von einem Parameter:<br>
 * (a) Liefert die aktuelle Anzahl an wartenden Kunden, deren Name an Quelle bzw. Namenszuweisung id auftritt.<br>
 * (b) Liefert die aktuelle Anzahl an Kunden in der Warteschlange an Station id (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die aktuelle Anzahl an Kunden in allen Warteschlange zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataQueue extends CalcSymbolSimData {
	/**
	 * Namen für das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"NQ","Queue","Schlange","Warteschlange"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataQueue() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	/**
	 * Kundentyp beim letzten Aufruf von {@link #calc(double[])} oder {@link #calcOrDefault(double[], double)}
	 * @see #calc(double[])
	 * @see #calcOrDefault(double[], double)
	 * @see #lastClientTypeIndex
	 */
	private String lastClientType;

	/**
	 * Index des Kundentyps {@link #lastClientTypeIndex}
	 * @see #calc(double[])
	 * @see #calcOrDefault(double[], double)
	 * @see #lastClientTypeIndex
	 */
	private int lastClientTypeIndex;

	/**
	 * Ermittelt zu der ID einer Kundenquell-Station den zugehörigen Index des Kundentyps
	 * @param simData	Simulationsdatenobjekt
	 * @param stationID	ID der Kundenquell-Station
	 * @return	Liefert im Erfolgsfall den 0-basierenden Index des Kundentyps, sonst -1
	 */
	private int getClientTypeIndex(final SimulationData simData, final double stationID) {
		final RunElement element=getRunElementForID(stationID);
		String name=null;
		if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
		if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
		if (name==null) return -1;

		if (lastClientType==null || !name.equals(lastClientType)) {
			final Integer I=simData.runModel.clientTypesMap.get(name);
			lastClientTypeIndex=(I==null)?-1:I.intValue();
			lastClientType=name;
		}

		return lastClientTypeIndex;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		final SimulationData simData=getSimData();

		/* Gesamtwert */
		if (parameters.length==0) {
			final int[] count=simData.runData.clientsInQueuesByType;
			if (count==null) return 0.0;
			double sum=0.0;
			for (int c: count) sum+=c;
			return sum;

			/*
			Funktioniert nicht während Warmup:
			return getSimData().statistics.clientsInSystemQueues.getCurrentState();
			 */
		}

		if (parameters.length==1) {
			/* Kundentyp */
			final int clientTypeIndex=getClientTypeIndex(simData,parameters[0]);
			if (clientTypeIndex>=0) {
				final int[] count=simData.runData.clientsInQueuesByType;
				if (count==null) return 0.0;
				return count[lastClientTypeIndex];
				/*
				Funktioniert nicht während Warmup:
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.getOrNull(name);
				if (indicator==null) return 0.0;
				return indicator.getCurrentState();
				 */
			}

			/* Station */
			final RunElementData data=getRunElementDataForID(parameters[0]);
			if (data==null) return 0.0;
			return data.clientsAtStationQueue;
		}

		/* Station und Kundentyp */
		if (parameters.length==2) {
			/* Zweiter Parameter ist Index des Kundentyps an der Quelle */
			RunElementData data=getRunElementDataForID(parameters[0]);
			if (data instanceof RunElementMultiQueueData) {
				int nr=(int)FastMath.round(parameters[1])-1;
				if (nr<0 || nr>=((RunElementMultiQueueData)data).getQueueCount()) return 0.0;
				return ((RunElementMultiQueueData)data).getQueueSize(nr);
			}
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final SimulationData simData=getSimData();

		/* Gesamtwert */
		if (parameters.length==0) {
			final int[] count=simData.runData.clientsInQueuesByType;
			if (count==null) return 0.0;
			double sum=0.0;
			for (int c: count) sum+=c;
			return sum;
			/*
			Funktioniert nicht während Warmup:
			return getSimData().statistics.clientsInSystemQueues.getCurrentState();
			 */
		}

		if (parameters.length==1) {
			/* Kundentyp */
			final int clientTypeIndex=getClientTypeIndex(simData,parameters[0]);
			if (clientTypeIndex>=0) {
				final int[] count=simData.runData.clientsInQueuesByType;
				if (count==null) return 0.0;
				return count[lastClientTypeIndex];
				/*
				Funktioniert nicht während Warmup:
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.getOrNull(name);
				if (indicator==null) return 0;
				return indicator.getCurrentState();
				 */			}

			/* Station */
			final RunElementData data=getRunElementDataForID(parameters[0]);
			if (data==null) return 0;
			return data.clientsAtStationQueue;
		}

		/* Station und Kundentyp */
		if (parameters.length==2) {
			/* Zweiter Parameter ist Index des Kundentyps an der Quelle */
			RunElementData data=getRunElementDataForID(parameters[0]);
			if (data instanceof RunElementMultiQueueData) {
				int nr=(int)FastMath.round(parameters[1])-1;
				if (nr<0 || nr>=((RunElementMultiQueueData)data).getQueueCount()) return 0;
				return ((RunElementMultiQueueData)data).getQueueSize(nr);
			}
		}

		return fallbackValue;
	}
}