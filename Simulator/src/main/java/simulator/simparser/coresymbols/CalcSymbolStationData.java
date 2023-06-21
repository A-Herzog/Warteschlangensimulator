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

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementAssign;
import simulator.elements.RunElementSource;
import simulator.elements.RunElementSourceMulti;
import simulator.elements.RunElementSourceMultiData;
import simulator.runmodel.SimulationData;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Basisklasse für Funktionen, die Stationsdaten aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolStationData extends CalcSymbolSimData {
	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationData() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Berechnung der Daten auf Basis des Laufzeit-Datenobjekts
	 * @param data	Laufzeit-Datenobjekt
	 * @return	Berechneter Wert
	 */
	protected abstract double calc(final RunElementData data);

	/**
	 * Kann der Wert über alle Stationen hinweg berechnet werden?
	 * @return	Wird hier <code>true</code> zurückgegeben, so muss {@link #calcAll()} implementiert werden.
	 */
	protected boolean hasAllData() {
		return false;
	}

	/**
	 * Können Daten für einen einzelnen Kundentyp (identifiziert über eine Quelle- oder Zuweisungsstation) berechnet werden?
	 * @return	Wird hier <code>true</code> zurückgegeben, so muss {@link #calcSingleClient(String)} implementiert werden.
	 */
	protected boolean hasSingleClientData() {
		return false;
	}

	/**
	 * Können Daten für eine Station und darin für einen einzelnen Kundentyp (identifiziert über eine Quelle- oder Zuweisungsstation) berechnet werden?
	 * @return	Wird hier <code>true</code> zurückgegeben, so muss {@link #calcStationClient(RunElementData, int)} implementiert werden.
	 */
	protected boolean hasStationAndClientData() {
		return false;
	}

	/**
	 * Berechnet die Daten über alle Stationen hinweg
	 * @return	Wert berechnet über alle Stationen hinweg
	 * @see #hasAllData()
	 */
	protected double calcAll() {
		return 0.0;
	}

	/**
	 * Berechnet den Wert für einen Kundentyp
	 * @param name	Kundentyp für den der Wert berechnet werden soll
	 * @return	Wert berechnet für den angegebenen Kundentyp
	 */
	protected double calcSingleClient(final String name) {
		return 0.0;
	}

	/**
	 * Berechnet den Wert für eine Station und darin für einen Kundentyp
	 * @param data	Laufzeit-Datenobjekt der Station
	 * @param clientTypeIndex	Index des Kundentyps für den der Wert berechnet werden soll
	 * @return	Wert berechnet für Station und darin den angegebenen Kundentyp
	 */
	protected double calcStationClient(final RunElementData data, final int clientTypeIndex) {
		return 0.0;
	}

	/**
	 * Zuletzt in {@link #calc(double[])} oder {@link #calcOrDefault(double[], double)}
	 * als Kundentyp innerhalb einer Station abgefragt Kundentyp-Quell-ID
	 * @see #calc(double[])
	 * @see #lastStationClientTypeClientIndex
	 */
	private int lastStationClientTypeStationID=-1;

	/**
	 * Zuletzt in {@link #calc(double[])} oder {@link #calcOrDefault(double[], double)}
	 * als Kundentyp innerhalb einer Mehrfachquelle abgefragt Kundentyp-Index
	 * @see #calc(double[])
	 * @see #lastStationClientTypeClientIndex
	 */
	private int lastStationClientTypeStationIDSub=-1;

	/**
	 * Zuletzt in {@link #calc(double[])} oder {@link #calcOrDefault(double[], double)}
	 * als Kundentyp-Index innerhalb einer Station verwendeter Index auf Basis einer
	 * Kundentyp-Quell-ID
	 * @see #calc(double[])
	 * @see #lastStationClientTypeStationID
	 */
	private int lastStationClientTypeClientIndex=-1;

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (getSimData()==null) throw error();

		/* Gesamtwert */
		if (parameters.length==0 && hasAllData()) return calcAll();

		if (parameters.length==1) {
			/* Kundentyp */
			if (hasSingleClientData()) {
				final RunElement element=getRunElementForID(parameters[0]);
				if (element==null) throw error();

				String name=null;
				if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
				if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
				if (name!=null) return calcSingleClient(name);
				/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
			}

			/* Station */
			final RunElementData stationData=getRunElementDataForID(parameters[0]);
			if (stationData==null) return 0.0;
			return calc(stationData);
		}

		/* Station und Kundentyp */
		if (parameters.length==2 && hasStationAndClientData()) {
			final RunElementData stationData=getRunElementDataForID(parameters[0]);
			if (stationData==null) return 0.0;

			final int intID=(int)FastMath.round(parameters[1]);
			int clientTypeIndex=-1;
			if (lastStationClientTypeStationID>=0) {
				if (intID==lastStationClientTypeStationID) clientTypeIndex=lastStationClientTypeClientIndex;
			}

			/* Zweiter Parameter ist Index des Kundentyps an der Quelle */
			if (stationData instanceof RunElementSourceMultiData) {
				final RunElementSourceMulti source=(RunElementSourceMulti)getRunElementForID(parameters[0]);
				final String name=source.getClientTypeName(intID-1); /* Umrechnung 1-basiert -> 0-basiert */
				if (!hasSingleClientData()) throw error();
				return calcSingleClient(name);
			}

			/* Zweiter Parameter ist ID der Kundenquell-Station */
			if (clientTypeIndex<0) {
				String name=null;
				final RunElement clientElement=getRunElementForID(parameters[1]);
				if (clientElement==null) throw error();
				if (clientElement instanceof RunElementSource) name=((RunElementSource)clientElement).clientTypeName;
				if (clientElement instanceof RunElementAssign) name=((RunElementAssign)clientElement).clientTypeName;
				if (name==null) throw error();

				final Integer clientTypeIndexObj=getSimData().runModel.clientTypesMap.get(name);
				if (clientTypeIndexObj!=null) clientTypeIndex=clientTypeIndexObj;

				lastStationClientTypeStationID=intID;
				lastStationClientTypeClientIndex=clientTypeIndex;
			}
			if (clientTypeIndex<0) throw error();

			return calcStationClient(stationData,clientTypeIndex);
		}

		/* Station und Kundentyp an Mehrfachquelle */
		if (parameters.length==3 && hasStationAndClientData()) {
			final RunElementData stationData=getRunElementDataForID(parameters[0]);
			if (stationData==null) return 0.0;

			final int intID=(int)FastMath.round(parameters[1]);
			final int intID2=(int)FastMath.round(parameters[2]);
			int clientTypeIndex=-1;
			if (lastStationClientTypeStationID>=0 && lastStationClientTypeStationIDSub>=0) {
				if (intID==lastStationClientTypeStationID && intID2==lastStationClientTypeStationIDSub) clientTypeIndex=lastStationClientTypeClientIndex;
			}

			if (clientTypeIndex<0) {
				String name=null;
				final RunElement clientElement=getRunElementForID(parameters[1]);
				if (clientElement==null) throw error();
				if (clientElement instanceof RunElementSourceMulti) name=((RunElementSourceMulti)clientElement).getClientTypeName(intID2-1); /* Umrechnung 1-basiert -> 0-basiert */
				if (name==null) throw error();

				final Integer clientTypeIndexObj=getSimData().runModel.clientTypesMap.get(name);
				if (clientTypeIndexObj!=null) clientTypeIndex=clientTypeIndexObj;

				lastStationClientTypeStationID=intID;
				lastStationClientTypeStationIDSub=intID2;
				lastStationClientTypeClientIndex=clientTypeIndex;
			}
			if (clientTypeIndex<0) throw error();

			return calcStationClient(stationData,clientTypeIndex);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (getSimData()==null) return fallbackValue;

		/* Gesamtwert */
		if (parameters.length==0 && hasAllData()) return calcAll();

		if (parameters.length==1) {
			/* Kundentyp */
			if (hasSingleClientData()) {
				final RunElement element=getRunElementForID(parameters[0]);
				if (element==null) return fallbackValue;

				String name=null;
				if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
				if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
				if (name!=null) return calcSingleClient(name);
				/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
			}

			/* Station */
			RunElementData data=getRunElementDataForID(parameters[0]);
			if (data==null) return 0;
			return calc(data);
		}

		/* Station und Kundentyp */
		if (parameters.length==2 && hasStationAndClientData()) {
			final RunElementData stationData=getRunElementDataForID(parameters[0]);
			if (stationData==null) return 0.0;

			final int intID=(int)FastMath.round(parameters[1]);
			int clientTypeIndex=-1;
			if (lastStationClientTypeStationID>=0) {
				if (intID==lastStationClientTypeStationID) clientTypeIndex=lastStationClientTypeClientIndex;
			}

			/* Zweiter Parameter ist Index des Kundentyps an der Quelle */
			if (stationData instanceof RunElementSourceMultiData) {
				final RunElementSourceMulti source=(RunElementSourceMulti)getRunElementForID(parameters[0]);
				final String name=source.getClientTypeName(intID-1); /* Umrechnung 1-basiert -> 0-basiert */
				if (!hasSingleClientData()) return fallbackValue;
				return calcSingleClient(name);
			}

			/* Zweiter Parameter ist ID der Kundenquell-Station */
			if (clientTypeIndex<0) {
				String name=null;
				final RunElement clientElement=getRunElementForID(parameters[1]);
				if (clientElement==null) return fallbackValue;
				if (clientElement instanceof RunElementSource) name=((RunElementSource)clientElement).clientTypeName;
				if (clientElement instanceof RunElementAssign) name=((RunElementAssign)clientElement).clientTypeName;
				if (name==null) return fallbackValue;

				final Integer clientTypeIndexObj=getSimData().runModel.clientTypesMap.get(name);
				if (clientTypeIndexObj!=null) clientTypeIndex=clientTypeIndexObj;

				lastStationClientTypeStationID=intID;
				lastStationClientTypeClientIndex=clientTypeIndex;
			}
			if (clientTypeIndex<0) return fallbackValue;

			return calcStationClient(stationData,clientTypeIndex);
		}

		/* Station und Kundentyp an Mehrfachquelle */
		if (parameters.length==3 && hasStationAndClientData()) {
			final RunElementData stationData=getRunElementDataForID(parameters[0]);
			if (stationData==null) return 0.0;

			final int intID=(int)FastMath.round(parameters[1]);
			final int intID2=(int)FastMath.round(parameters[2]);
			int clientTypeIndex=-1;
			if (lastStationClientTypeStationID>=0 && lastStationClientTypeStationIDSub>=0) {
				if (intID==lastStationClientTypeStationID && intID2==lastStationClientTypeStationIDSub) clientTypeIndex=lastStationClientTypeClientIndex;
			}

			if (clientTypeIndex<0) {
				String name=null;
				final RunElement clientElement=getRunElementForID(parameters[1]);
				if (clientElement==null) return fallbackValue;
				if (clientElement instanceof RunElementSourceMulti) name=((RunElementSourceMulti)clientElement).getClientTypeName(intID2-1); /* Umrechnung 1-basiert -> 0-basiert */
				if (name==null) return fallbackValue;

				final Integer clientTypeIndexObj=getSimData().runModel.clientTypesMap.get(name);
				if (clientTypeIndexObj!=null) clientTypeIndex=clientTypeIndexObj;

				lastStationClientTypeStationID=intID;
				lastStationClientTypeStationIDSub=intID2;
				lastStationClientTypeClientIndex=clientTypeIndex;
			}
			if (clientTypeIndex<0) return fallbackValue;

			return calcStationClient(stationData,clientTypeIndex);
		}

		return fallbackValue;
	}

	/**
	 * Kundentyp beim letzten Aufruf von {@link #getClientDataIndicator(String, StatisticsMultiPerformanceIndicator)}
	 * oder {@link #getClientTimeIndicator(String, StatisticsMultiPerformanceIndicator)}
	 * @see #getClientDataIndicator(String, StatisticsMultiPerformanceIndicator)
	 * @see #getClientTimeIndicator(String, StatisticsMultiPerformanceIndicator)
	 * @see #lastDataIndicator
	 * @see #lastTimeIndicator
	 */
	private String lastClientType;

	/**
	 * Zurückgeliefertes Statistikobjekt beim letzten Aufruf von {@link #getClientDataIndicator(String, StatisticsMultiPerformanceIndicator)}
	 * @see #getClientDataIndicator(String, StatisticsMultiPerformanceIndicator)
	 * @see #lastClientType
	 */
	private StatisticsDataPerformanceIndicator lastDataIndicator;

	/**
	 * Zurückgeliefertes Statistikobjekt beim letzten Aufruf von {@link #getClientTimeIndicator(String, StatisticsMultiPerformanceIndicator)}
	 * @see #getClientTimeIndicator(String, StatisticsMultiPerformanceIndicator)
	 * @see #lastClientType
	 */
	private StatisticsTimePerformanceIndicator lastTimeIndicator;

	/**
	 * Liefert das zu einem Kundentyp gehörige Statistikobjekt.
	 * Dabei wird, wenn möglich ein Cache verwendet, statt die Map abfragen zu müssen.
	 * @param clientTypeName	Kundentyp, für den das zugehörige Statistikobjekt ermittelt werden soll
	 * @param indicators	Statistikobjekte-Sammlung aus der das konkrete Statistikobjekt geholt werden soll
	 * @return	Statistikobjekt zu dem Kundentyp
	 */
	protected final StatisticsDataPerformanceIndicator getClientDataIndicator(final String clientTypeName, final StatisticsMultiPerformanceIndicator indicators) {
		if (lastClientType==null || !clientTypeName.equals(lastClientType)) {
			lastDataIndicator=((StatisticsDataPerformanceIndicator)indicators.get(clientTypeName));
			lastClientType=clientTypeName;
		}
		return lastDataIndicator;
	}

	/**
	 * Liefert das zu einem Kundentyp gehörige Statistikobjekt.
	 * Dabei wird, wenn möglich ein Cache verwendet, statt die Map abfragen zu müssen.
	 * @param clientTypeName	Kundentyp, für den das zugehörige Statistikobjekt ermittelt werden soll
	 * @param indicators	Statistikobjekte-Sammlung aus der das konkrete Statistikobjekt geholt werden soll
	 * @return	Statistikobjekt zu dem Kundentyp
	 */
	protected final StatisticsTimePerformanceIndicator getClientTimeIndicator(final String clientTypeName, final StatisticsMultiPerformanceIndicator indicators) {
		if (lastClientType==null || !clientTypeName.equals(lastClientType)) {
			lastTimeIndicator=((StatisticsTimePerformanceIndicator)indicators.get(clientTypeName));
			lastClientType=clientTypeName;
		}
		return lastTimeIndicator;
	}
}
