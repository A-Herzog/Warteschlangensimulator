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

import parser.coresymbols.CalcSymbolPreOperator;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Basisklasse für Funktionen, die auf Simulationsdaten zugreifen können sollen.
 * @author Alexander Herzog
 * @see SimulationData
 */
public abstract class CalcSymbolSimData extends CalcSymbolPreOperator {
	/**
	 * Liefert das zentrale {@link SimulationData}-Objekt für Berechnungen.
	 * @return	Basis-Objekt der Simulation
	 * @see ExpressionCalc#getSimData()
	 */
	protected SimulationData getSimData() {
		if (!(calcSystem instanceof ExpressionCalc)) return null;
		return ((ExpressionCalc)calcSystem).getSimData();
	}

	/**
	 * Liefert ein Array der zu den Stationen gehörenden {@link RunElementData}-Objekten gemäß ihren IDs
	 * @return	Array der zu den Stationen gehörenden {@link RunElementData}-Objekten
	 * @see ExpressionCalc#getRunElementData()
	 */
	protected RunElementData[] getRunElementData() {
		if (!(calcSystem instanceof ExpressionCalc)) return null;
		return ((ExpressionCalc)calcSystem).getRunElementData();
	}

	/**
	 * Liefert ein Array der Stationen gemäß ihren IDs
	 * @return	Array der Stationen
	 * @see ExpressionCalc#getRunElements()
	 */
	protected RunElement[] getRunElements() {
		if (!(calcSystem instanceof ExpressionCalc)) return null;
		return ((ExpressionCalc)calcSystem).getRunElements();
	}

	private double lastRunElementDataID=-1;
	private RunElementData lastRunElementData;

	/**
	 * Liefert das {@link RunElementData}-Objekt für eine Element-ID
	 * @param id	Element-ID (wird gerundet) für die das {@link RunElementData}-Objekt geliefert werden soll
	 * @return	Laufzeit-Datenobjekt zu der angegebenen ID oder <code>null</code>, wenn es keine Station mit der angegebenen ID gibt
	 */
	protected RunElementData getRunElementDataForID(final double id) {
		if (id==lastRunElementDataID) return lastRunElementData;
		lastRunElementDataID=id;
		final int intID=(int)FastMath.round(id);
		final RunElementData[] runElementData=getRunElementData();
		if (runElementData==null || intID<0 || intID>=runElementData.length) return lastRunElementData=null;
		return lastRunElementData=runElementData[intID];
	}

	private double lastRunElementID=-1;
	private RunElement lastRunElement;

	/**
	 * Liefert das {@link RunElement}-Objekt für eine Element-ID
	 * @param id	Element-ID (wird gerundet) für die das {@link RunElementData}-Objekt geliefert werden soll
	 * @return	Laufzeit-Objekt zu der angegebenen ID oder <code>null</code>, wenn es keine Station mit der angegebenen ID gibt
	 */
	protected RunElement getRunElementForID(final double id) {
		if (id==lastRunElementID) return lastRunElement;
		lastRunElementID=id;
		final int intID=(int)FastMath.round(id);
		final RunElement[] runElements=getRunElements();
		if (runElements==null || intID<0 || intID>=runElements.length) return lastRunElement=null;
		return lastRunElement=runElements[intID];
	}

	/**
	 * Liefert die Auslastungsstatistik aller Ressourcen als Array zurück
	 * @return Auslastungsstatistik aller Ressourcen
	 * @see	ExpressionCalc#getUsageStatistics()
	 */
	protected StatisticsTimePerformanceIndicator[] getUsageStatistics() {
		if (!(calcSystem instanceof ExpressionCalc)) return new StatisticsTimePerformanceIndicator[0];
		return ((ExpressionCalc)calcSystem).getUsageStatistics();
	}

	/**
	 * Liefert die Auslastungsstatistik aller Transportergruppen als Array zurück
	 * @return Auslastungsstatistik aller Transportergruppen
	 * @see ExpressionCalc#getTransporterUsageStatistics()
	 */
	protected StatisticsTimePerformanceIndicator[] getTransporterUsageStatistics() {
		if (!(calcSystem instanceof ExpressionCalc)) return new StatisticsTimePerformanceIndicator[0];
		return ((ExpressionCalc)calcSystem).getTransporterUsageStatistics();
	}

	/**
	 * Liefert den aktuellen Kunden in dessen Kontext die Rechnung erfolgt (oder <code>null</code>, wenn es keinen solchen gibt)
	 * @return	Aktueller Kunde
	 * @see ExpressionCalc#getCurrentClient()
	 */
	protected RunDataClient getCurrentClient() {
		if (!(calcSystem instanceof ExpressionCalc)) return null;
		return ((ExpressionCalc)calcSystem).getCurrentClient();
	}

	/**
	 * Liefert ein Kundendatenfeld für den aktuellen Kunden in dessen Kontext die Rechnung erfolgt (oder 0.0, wenn es keinen solchen gibt)
	 * @param index	Index des Kundendatenfeldes
	 * @return	Wert des Datenfeldes
	 */
	protected double getClientData(final int index) {
		if (!(calcSystem instanceof ExpressionCalc)) return 0.0;
		final RunDataClient client=((ExpressionCalc)calcSystem).getCurrentClient();
		if (client==null) return 0.0;
		return client.getUserData(index);
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}