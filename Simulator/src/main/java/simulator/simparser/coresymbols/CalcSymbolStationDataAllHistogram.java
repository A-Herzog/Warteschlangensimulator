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

import org.apache.commons.math3.util.FastMath;

import mathtools.distribution.DataDistributionImpl;
import parser.MathCalcError;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;

/**
 * Basisklasse für Funktionen, die Histogramme über jeweils alle Stationen ausgeben.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolStationDataAllHistogram extends CalcSymbolSimData {
	/**
	 * Liefert die Verteilung auf deren Basis das Histogramm erstellt werden soll.
	 * @param statistics	Statistikobjekt aus dem die Verteilung ausgewählt werden soll
	 * @return	Verteilung auf deren Basis das Histogramm erstellt werden soll
	 */
	protected abstract DataDistributionImpl getDistribution(final Statistics statistics);

	/**
	 * Liefert die Summe über die Verteilung auf deren Basis das Histogramm erstellt werden soll.<br>
	 * Hier kann optional eine schnellere Variante hinterlegt werden im Gegensatz zur einfachen Summation über die Werte.
	 * @param statistics	Statistikobjekt aus dem die Verteilung ausgewählt werden soll
	 * @return	Summe über die Verteilung
	 */
	protected double getDistributionSum(final Statistics statistics) {
		return getDistribution(statistics).sum();
	}

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataAllHistogram() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (parameters.length<1 || parameters.length>2) throw error();

		final Statistics statistics=getSimData().statistics;
		final DataDistributionImpl dist=getDistribution(statistics);
		if (dist==null) return 0.0;
		final double sum=getDistributionSum(statistics);
		if (sum<1) return 0.0;

		final int len=dist.densityData.length;
		final double scale=len/dist.upperBound;
		if (parameters.length==1) {
			final int index=(int)FastMath.round(parameters[0]*scale);
			if (index<0 || index>=len) return 0.0;
			return dist.densityData[index]/sum;
		} else {
			final int index1=FastMath.max(-1,(int)FastMath.round(parameters[0]*scale));
			final int index2=FastMath.max(0,(int)FastMath.round(parameters[1]*scale));
			if (index1>=len) return 0.0;
			if (index2>=len) return 0.0;
			if (index2<=index1) return 0.0;
			double part=0;
			final double[] data=dist.densityData;
			for (int i=index1+1;i<=index2;i++) part+=data[i];
			return part/sum;
		}
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length<1 || parameters.length>2) return fallbackValue;

		final DataDistributionImpl dist=getDistribution(getSimData().statistics);
		if (dist==null) return 0;
		final double sum=dist.sum();
		if (sum<1) return 0;

		final double scale=dist.densityData.length/dist.upperBound;
		if (parameters.length==1) {
			final int index=(int)FastMath.round(parameters[0]*scale);
			if (index<0 || index>=dist.densityData.length) return 0;
			return dist.densityData[index]/sum;
		} else {
			final int index1=FastMath.max(-1,(int)FastMath.round(parameters[0]*scale));
			final int index2=FastMath.max(0,(int)FastMath.round(parameters[1]*scale));
			if (index1>=dist.densityData.length) return 0;
			if (index2>=dist.densityData.length) return 0;
			if (index2<=index1) return 0;
			double part=0;
			for (int i=index1+1;i<=index2;i++) part+=dist.densityData[i];
			return part/sum;
		}
	}
}
