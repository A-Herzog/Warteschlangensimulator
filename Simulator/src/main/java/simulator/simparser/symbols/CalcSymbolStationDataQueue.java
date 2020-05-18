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
import simulator.simparser.coresymbols.CalcSymbolSimData;
import statistics.StatisticsTimePerformanceIndicator;

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
	@Override
	public String[] getNames() {
		return new String[]{"NQ","Queue","Schlange","Warteschlange"};
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) {
			return getSimData().statistics.clientsInSystemQueues.getCurrentState();
		}

		if (parameters.length==1) {
			final RunElement element=getRunElementForID(parameters[0]);
			if (element instanceof RunElementSource) {
				final String name=((RunElementSource)element).clientTypeName;
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.getOrNull(name);
				if (indicator==null) return 0.0;
				return indicator.getCurrentState();
			}
			if (element instanceof RunElementAssign) {
				final String name=((RunElementAssign)element).clientTypeName;
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.getOrNull(name);
				if (indicator==null) return 0.0;
				return indicator.getCurrentState();
			}

			final RunElementData data=getRunElementDataForID(parameters[0]);
			if (data==null) return 0.0;
			return data.clientsAtStationQueue;
		}

		if (parameters.length==2) {
			RunElementData data=getRunElementDataForID(parameters[0]);
			if (!(data instanceof RunElementMultiQueueData)) throw error();
			int nr=(int)FastMath.round(parameters[1])-1;
			if (nr<0 || nr>=((RunElementMultiQueueData)data).getQueueCount()) return 0.0;
			return ((RunElementMultiQueueData)data).getQueueSize(nr);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) {
			return getSimData().statistics.clientsInSystemQueues.getCurrentState();
		}

		if (parameters.length==1) {
			final RunElement element=getRunElementForID(parameters[0]);
			if (element instanceof RunElementSource) {
				final String name=((RunElementSource)element).clientTypeName;
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.getOrNull(name);
				if (indicator==null) return 0;
				return indicator.getCurrentState();
			}
			if (element instanceof RunElementAssign) {
				final String name=((RunElementAssign)element).clientTypeName;
				final StatisticsTimePerformanceIndicator indicator=(StatisticsTimePerformanceIndicator)getSimData().statistics.clientsInSystemByClient.getOrNull(name);
				if (indicator==null) return 0;
				return indicator.getCurrentState();
			}

			final RunElementData data=getRunElementDataForID(parameters[0]);
			if (data==null) return 0;
			return data.clientsAtStationQueue;
		}

		if (parameters.length==2) {
			RunElementData data=getRunElementDataForID(parameters[0]);
			if (!(data instanceof RunElementMultiQueueData)) return fallbackValue;
			int nr=(int)FastMath.round(parameters[1])-1;
			if (nr<0 || nr>=((RunElementMultiQueueData)data).getQueueCount()) return 0;
			return ((RunElementMultiQueueData)data).getQueueSize(nr);
		}

		return fallbackValue;
	}
}