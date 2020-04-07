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
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementAssign;
import simulator.elements.RunElementSource;
import simulator.runmodel.SimulationData;

/**
 * Basisklasse für Funktionen, die Histogramme für jeweils eine Station/einen Kundentyp ausgeben.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolStationDataHistogram extends CalcSymbolSimData {
	/**
	 * Liefert die Verteilung auf deren Basis das Histogramm für eine Station erstellt werden soll.
	 * @param data	Stationsdatenobjekt der Station für die das Histogramm erstellt werden soll
	 * @return	Verteilung auf deren Basis das Histogramm für eine Station erstellt werden soll
	 */
	protected abstract DataDistributionImpl getDistribution(final RunElementData data);

	/**
	 * Können Histogramme für einzelne Kundentypen erstellt werden?
	 * @return	Wird hier <code>true</code> zurückgegeben, so müssen die Methoden {@link #getDistributionForClientType(String)} und {@link #getDistributionSumForClientType(String)} implementiert werden.
	 */
	protected boolean hasSingleClientData() {
		return false;
	}

	/**
	 * Liefert die Verteilung auf deren Basis das Histogramm für einen Kundentyp erstellt werden soll.
	 * @param name	Name des Kundentyps
	 * @return	Verteilung auf deren Basis das Histogramm für einen Kundentyp erstellt werden soll
	 * @see #hasSingleClientData()
	 */
	protected DataDistributionImpl getDistributionForClientType(final String name) {
		return null;
	}

	/**
	 * Liefert die Summe über die Verteilung auf deren Basis das Histogramm für eine Station erstellt werden soll.
	 * @param data	Stationsdatenobjekt der Station für die das Histogramm erstellt werden soll
	 * @return	Summe der Verteilungswerte
	 */
	protected abstract double getDistributionSum(final RunElementData data);

	/**
	 * Liefert die Summe über die Verteilung auf deren Basis das Histogramm für einen Kundentyp erstellt werden soll.
	 * @param name	Name des Kundentyps
	 * @return	Summe der Verteilungswerte
	 * @see #hasSingleClientData()
	 */
	protected double getDistributionSumForClientType(final String name) {
		return 0.0;
	}

	/**
	 * Liefert die Verteilung auf Basis einer Stations-ID (entweder um eine Stations-Kenngröße auszulesen oder um indirekt über eine Quelle einen Kundentyp zu identifizieren)
	 * @param id	Stations-ID
	 * @return	Verteilung auf deren Basis das Histogramm erstellt werden soll
	 */
	protected DataDistributionImpl getDistributionByID(final double id) {
		final RunElementData data=getRunElementDataForID(id);
		if (data==null) return null;

		if (hasSingleClientData()) {
			final RunElement element=getRunElementForID(id);
			if (element==null) return null;
			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return getDistributionForClientType(name);
			/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
		}

		return getDistribution(data);
	}

	/**
	 * Liefert die Summe über die Verteilung auf Basis einer Stations-ID (entweder um eine Stations-Kenngröße auszulesen oder um indirekt über eine Quelle einen Kundentyp zu identifizieren)
	 * @param id	Stations-ID
	 * @return	Summe der Verteilungswerte
	 */
	protected double getDistributionSumByID(final double id) {
		if (hasSingleClientData()) {
			final RunElement element=getRunElementForID(id);
			if (element==null) return 0.0;
			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return getDistributionSumForClientType(name);
			/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
		}

		final RunElementData data=getRunElementDataForID(id);
		if (data==null) return 0;
		return getDistributionSum(data);
	}

	private double lastSum;
	private long lastParam1;
	private long lastParam2;
	private double lastResult;

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length<2 || parameters.length>3) return null;

		final DataDistributionImpl dist=getDistributionByID(parameters[0]);
		if (dist==null) return fastBoxedValue(0);
		final double sum=getDistributionSumByID(parameters[0]);
		if (sum<1) return fastBoxedValue(0);

		if (parameters.length==2) {
			final int index=(int)FastMath.round(parameters[1]);
			if (index<0 || index>=dist.densityData.length) return fastBoxedValue(0);
			return fastBoxedValue(dist.densityData[index]/sum);
		} else {
			final int index1=FastMath.max(-1,(int)FastMath.round(parameters[1]));
			int index2=FastMath.max(0,(int)FastMath.round(parameters[2]));
			if (index1<0 || index1>=dist.densityData.length) return fastBoxedValue(0);
			if (index2>=dist.densityData.length) index2=dist.densityData.length-1;
			if (index2<=index1) return fastBoxedValue(0);

			if (lastSum!=sum || lastParam1!=index1 || lastParam2!=index2) {
				double part=0;
				for (int i=index1+1;i<=index2;i++) part+=dist.densityData[i];
				lastSum=sum;
				lastParam1=index1;
				lastParam2=index2;
				lastResult=part/sum;
			}
			return fastBoxedValue(lastResult);
		}
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length<2 || parameters.length>3) return fallbackValue;

		final DataDistributionImpl dist=getDistributionByID(parameters[0]);
		if (dist==null) return 0;
		final double[] densityData=dist.densityData;
		final double sum=getDistributionSumByID(parameters[0]);
		if (sum<1) return 0;

		if (parameters.length==2) {
			final int index=(int)FastMath.round(parameters[1]);
			if (index<0 || index>=densityData.length) return 0;
			return densityData[index]/sum;
		} else {
			final int index1=FastMath.max(-1,(int)FastMath.round(parameters[1]));
			int index2=FastMath.max(0,(int)FastMath.round(parameters[2]));
			if (index1<0 || index1>=densityData.length) return 0;
			if (index2>=densityData.length) index2=densityData.length-1;
			if (index2<=index1) return 0;

			if (lastSum!=sum || lastParam1!=index1 || lastParam2!=index2) {
				double part=0;
				for (int i=index1+1;i<=index2;i++) part+=densityData[i];
				lastSum=sum;
				lastParam1=index1;
				lastParam2=index2;
				lastResult=part/sum;
			}
			return lastResult;
		}
	}
}
