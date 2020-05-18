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
package simulator.simparser.coresymbols;

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.elements.RunElementAssign;
import simulator.elements.RunElementSource;
import simulator.runmodel.SimulationData;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;

/**
 * Basisklasse für Funktionen, die Kosten aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolClientCosts extends CalcSymbolSimData {
	/**
	 * Liefert den Wert aus einem {@link StatisticsValuePerformanceIndicator}-Objekt
	 * @param indicator	Statistikobjekt aus dem der Wert ausgelesen werden soll
	 * @return	Wert in dem Statistikobjekt
	 */
	protected final double getValue(StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsValuePerformanceIndicator)) return 0.0;
		return ((StatisticsValuePerformanceIndicator)indicator).getValue();
	}

	/**
	 * Liefert die Anzahl an Kunden eines Typs
	 * @param name	Name des Kundentyp
	 * @return	Anzahl an Kunden des Typs
	 */
	protected final long getCount(final String name) {
		final StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsWaitingTimes.get(name);
		if (!(indicator instanceof StatisticsDataPerformanceIndicator)) return 0;
		return ((StatisticsDataPerformanceIndicator)indicator).getCount();
	}

	/**
	 * Können Daten über alle Kundentypen berechnet werden?
	 * @return	Muss <code>true</code> liefern, wenn {@link #calcAllClients()} definiert ist.
	 */
	protected boolean hasAllClientData() {
		return true;
	}

	/**
	 * Berechnet die Daten über alle Kundentypen hinweg
	 * @return	Daten über alle Kundentypen hinweg
	 * @see #hasAllClientData()
	 */
	protected double calcAllClients() {
		double sum=0;
		for (String name: getSimData().runModel.clientTypes) sum+=calcSingleClient(name);
		return sum;
	}

	/**
	 * Berechnet den Mittelwert (gewichtet mit den Anzahlen an Kunden in den Typen)
	 * über die {@link StatisticsValuePerformanceIndicator}-Objekte in dem
	 * Mehrfach-Indikator
	 * @param multi	Mehrfach-Indikator der die entsprechenden Teil-Indikatoren enthält
	 * @return	Mittelwert über die Teilwerte
	 */
	protected final double calcAverage(final StatisticsMultiPerformanceIndicator multi) {
		long count=0;
		double sum=0;
		for (String name: getSimData().runModel.clientTypes) {
			sum+=getValue(multi.get(name));
			count+=getCount(name);
		}
		if (count==0) return 0.0;
		return sum/count;
	}

	/**
	 * Berechnet die Daten für einen Kundentyp
	 * @param name	Name des Kundentyps für den die Daten berechnet werden sollen
	 * @return	Daten für einen Kundentyp
	 */
	protected abstract double calcSingleClient(final String name);

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length==0) {
			if (!hasAllClientData()) throw error();
			return calcAllClients();
		}
		if (parameters.length==1) {
			final RunElement element=getRunElementForID(parameters[0]);
			if (element==null) throw error();

			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return calcSingleClient(name);
			throw error();
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length==0) {
			if (!hasAllClientData()) return fallbackValue;
			return calcAllClients();
		}
		if (parameters.length==1) {
			final RunElement element=getRunElementForID(parameters[0]);
			if (element==null) return fallbackValue;

			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return calcSingleClient(name);
			return fallbackValue;
		}

		return fallbackValue;
	}
}
