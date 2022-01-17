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
package simulator.simparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.CalcSystem;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;
import simulator.Simulator;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataResource;
import simulator.runmodel.RunDataTransporters;
import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolSimData;
import simulator.simparser.symbols.*;
import simulator.statistics.Statistics;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Formelparser der zusätzlich Informationen im Kontext
 * der Simulation bereitstellt.
 * @author Alexander Herzog
 * @see CalcSystem
 * @see SimulationData
 */
public class ExpressionCalc extends CalcSystem {
	/** Liste der Laufzeit-Element Datenobjekte */
	private RunElementData[] runElementData;
	/** Liste der Laufzeit-Element Objekte */
	private RunElement[] runElements;
	/** Erfassung der Ressourcen Nutzung */
	private StatisticsTimePerformanceIndicator[] resourceUsage;
	/** Erfassung der Transporter Nutzung */
	private StatisticsTimePerformanceIndicator[] transporterUsage;
	/** Aktuelles Simulationsdatenobjekt */
	private SimulationData simData;
	/** Aktuelles Kundenobjekt */
	private RunDataClient currentClient;

	/**
	 * Konstruktor der Klasse
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public ExpressionCalc(final String[] variables) {
		super(variables);
	}

	/**
	 * Liefert das zentrale {@link SimulationData}-Objekt für Berechnungen.
	 * @return	Basis-Objekt der Simulation
	 * @see CalcSymbolSimData#getSimData()
	 */
	public SimulationData getSimData() {
		return simData;
	}

	/**
	 * Liefert ein Array der zu den Stationen gehörenden {@link RunElementData}-Objekten gemäß ihren IDs
	 * @return	Array der zu den Stationen gehörenden {@link RunElementData}-Objekten
	 * @see CalcSymbolSimData#getRunElementData()
	 */
	public RunElementData[] getRunElementData() {
		if (runElementData==null && runElements!=null && simData!=null) { /* Lazy Initialisierung von runElementData (spart den Speicher für das Array ein, wenn nie auf RunElementData zugegriffen wird) */
			runElementData=new RunElementData[runElements.length];
			for (int i=0;i<runElements.length;i++) if (runElements[i]!=null) runElementData[i]=runElements[i].getData(simData);
		}
		return runElementData;
	}

	/**
	 * Liefert ein Array der Stationen gemäß ihren IDs
	 * @return	Array der Stationen
	 * @see CalcSymbolSimData#getRunElements()
	 */
	public RunElement[] getRunElements() {
		return runElements;
	}

	/**
	 * Liefert die Auslastungsstatistik aller Ressourcen als Array zurück
	 * @return Auslastungsstatistik aller Ressourcen
	 * @see	RunDataResource#getStatisticsUsage(SimulationData)
	 * @see CalcSymbolSimData#getUsageStatistics()
	 */
	public StatisticsTimePerformanceIndicator[] getUsageStatistics() {
		return resourceUsage;
	}

	/**
	 * Liefert die Auslastungsstatistik aller Transportergruppen als Array zurück
	 * @return Auslastungsstatistik aller Transportergruppen
	 * @see	RunDataTransporters#getUsageStatistics(SimulationData)
	 * @see CalcSymbolSimData#getTransporterUsageStatistics()
	 */
	public StatisticsTimePerformanceIndicator[] getTransporterUsageStatistics() {
		return transporterUsage;
	}

	/**
	 * Liefert den aktuellen Kunden in dessen Kontext die Rechnung erfolgt (oder <code>null</code>, wenn es keinen solchen gibt)
	 * @return	Aktueller Kunde
	 * @see CalcSymbolSimData#getCurrentClient()
	 */
	public RunDataClient getCurrentClient() {
		return currentClient;
	}

	/**
	 * Erstellt auf Basis des Simulationsdatenobjektes {@link #simData}
	 * die weiteren Listen mit Simulations-Objekten.
	 * @see #simData
	 * @see #runElements
	 * @see #resourceUsage
	 * @see #transporterUsage
	 */
	private void prepareRunElementData() {
		/* Daten zu den Elementen */
		runElements=simData.runModel.elementsFast;
		/* runElementData wird erst in getRunElementData() (also nur bei Bedarf) initialisiert. */
		/*
		runElementData=new RunElementData[runElements.length];
		for (int i=0;i<runElements.length;i++) if (runElements[i]!=null) runElementData[i]=runElements[i].getData(simData);
		 */

		/* Daten zu den Stationen */
		resourceUsage=simData.runData.resources.getUsageStatistics(simData);
		if (resourceUsage==null) resourceUsage=new StatisticsTimePerformanceIndicator[0];
		transporterUsage=simData.runData.transporters.getUsageStatistics(simData);
		if (transporterUsage==null) transporterUsage=new StatisticsTimePerformanceIndicator[0];
	}

	/**
	 * Statische Liste der Simulator-Funktionen
	 */
	private static List<CalcSymbolPreOperator> functions;

	static {
		functions=new ArrayList<>(256);

		functions.add(new CalcSymbolScriptMap());
		functions.add(new CalcSymbolStationText());

		functions.add(new CalcSymbolSimDataSimTime());
		functions.add(new CalcSymbolSimDataWarmUp());
		functions.add(new CalcSymbolRepeatCount());
		functions.add(new CalcSymbolRepeatCurrent());

		functions.add(new CalcSymbolStationDataWIP());
		functions.add(new CalcSymbolStationDataWIP_avg());
		functions.add(new CalcSymbolStationDataWIP_median());
		functions.add(new CalcSymbolStationDataWIP_quantil());
		functions.add(new CalcSymbolStationDataWIP_min());
		functions.add(new CalcSymbolStationDataWIP_max());
		functions.add(new CalcSymbolStationDataWIP_var());
		functions.add(new CalcSymbolStationDataWIP_std());
		functions.add(new CalcSymbolStationDataWIP_cv());
		functions.add(new CalcSymbolStationDataWIP_scv());
		functions.add(new CalcSymbolStationDataWIP_sk());
		functions.add(new CalcSymbolStationDataWIP_kurt());
		functions.add(new CalcSymbolStationDataWIP_hist());

		functions.add(new CalcSymbolStationDataQueue());
		functions.add(new CalcSymbolStationDataQueue_avg());
		functions.add(new CalcSymbolStationDataQueue_median());
		functions.add(new CalcSymbolStationDataQueue_quantil());
		functions.add(new CalcSymbolStationDataQueue_min());
		functions.add(new CalcSymbolStationDataQueue_max());
		functions.add(new CalcSymbolStationDataQueue_var());
		functions.add(new CalcSymbolStationDataQueue_std());
		functions.add(new CalcSymbolStationDataQueue_cv());
		functions.add(new CalcSymbolStationDataQueue_scv());
		functions.add(new CalcSymbolStationDataQueue_sk());
		functions.add(new CalcSymbolStationDataQueue_kurt());
		functions.add(new CalcSymbolStationDataQueue_hist());

		functions.add(new CalcSymbolStationDataProcessCount());

		functions.add(new CalcSymbolStationDataWaiting_sum());
		functions.add(new CalcSymbolStationDataWaiting_avg());
		functions.add(new CalcSymbolStationDataWaiting_median());
		functions.add(new CalcSymbolStationDataWaiting_quantil());
		functions.add(new CalcSymbolStationDataWaiting_min());
		functions.add(new CalcSymbolStationDataWaiting_max());
		functions.add(new CalcSymbolStationDataWaiting_var());
		functions.add(new CalcSymbolStationDataWaiting_std());
		functions.add(new CalcSymbolStationDataWaiting_cv());
		functions.add(new CalcSymbolStationDataWaiting_scv());
		functions.add(new CalcSymbolStationDataWaiting_sk());
		functions.add(new CalcSymbolStationDataWaiting_kurt());
		functions.add(new CalcSymbolStationDataWaiting_hist());
		functions.add(new CalcSymbolStationDataWaiting_histAll());

		functions.add(new CalcSymbolStationDataTransfer_sum());
		functions.add(new CalcSymbolStationDataTransfer_avg());
		functions.add(new CalcSymbolStationDataTransfer_median());
		functions.add(new CalcSymbolStationDataTransfer_quantil());
		functions.add(new CalcSymbolStationDataTransfer_min());
		functions.add(new CalcSymbolStationDataTransfer_max());
		functions.add(new CalcSymbolStationDataTransfer_var());
		functions.add(new CalcSymbolStationDataTransfer_std());
		functions.add(new CalcSymbolStationDataTransfer_cv());
		functions.add(new CalcSymbolStationDataTransfer_scv());
		functions.add(new CalcSymbolStationDataTransfer_sk());
		functions.add(new CalcSymbolStationDataTransfer_kurt());
		functions.add(new CalcSymbolStationDataTransfer_hist());
		functions.add(new CalcSymbolStationDataTransfer_histAll());

		functions.add(new CalcSymbolStationDataProcess_sum());
		functions.add(new CalcSymbolStationDataProcess_avg());
		functions.add(new CalcSymbolStationDataProcess_median());
		functions.add(new CalcSymbolStationDataProcess_quantil());
		functions.add(new CalcSymbolStationDataProcess_min());
		functions.add(new CalcSymbolStationDataProcess_max());
		functions.add(new CalcSymbolStationDataProcess_var());
		functions.add(new CalcSymbolStationDataProcess_std());
		functions.add(new CalcSymbolStationDataProcess_cv());
		functions.add(new CalcSymbolStationDataProcess_scv());
		functions.add(new CalcSymbolStationDataProcess_sk());
		functions.add(new CalcSymbolStationDataProcess_kurt());
		functions.add(new CalcSymbolStationDataProcess_hist());
		functions.add(new CalcSymbolStationDataProcess_histAll());

		functions.add(new CalcSymbolStationDataResidence_sum());
		functions.add(new CalcSymbolStationDataResidence_avg());
		functions.add(new CalcSymbolStationDataResidence_median());
		functions.add(new CalcSymbolStationDataResidence_quantil());
		functions.add(new CalcSymbolStationDataResidence_min());
		functions.add(new CalcSymbolStationDataResidence_max());
		functions.add(new CalcSymbolStationDataResidence_var());
		functions.add(new CalcSymbolStationDataResidence_std());
		functions.add(new CalcSymbolStationDataResidence_cv());
		functions.add(new CalcSymbolStationDataResidence_scv());
		functions.add(new CalcSymbolStationDataResidence_sk());
		functions.add(new CalcSymbolStationDataResidence_kurt());
		functions.add(new CalcSymbolStationDataResidence_hist());
		functions.add(new CalcSymbolStationDataResidence_histAll());

		functions.add(new CalcSymbolAnalogValue());
		functions.add(new CalcSymbolAnalogRate());
		functions.add(new CalcSymbolAnalogValve());

		functions.add(new CalcSymbolStationDataNumberIn());
		functions.add(new CalcSymbolStationDataNumberOut());
		functions.add(new CalcSymbolStationDataThroughput());
		functions.add(new CalcSymbolStationDataCosts_sum());
		functions.add(new CalcSymbolSimDataCounter());
		functions.add(new CalcSymbolSimDataCounterPart());

		functions.add(new CalcSymbolResourceCount());
		functions.add(new CalcSymbolResourceData_current());
		functions.add(new CalcSymbolResourceData_avg());
		functions.add(new CalcSymbolResourceData_min());
		functions.add(new CalcSymbolResourceData_max());
		functions.add(new CalcSymbolResourceData_var());
		functions.add(new CalcSymbolResourceData_std());
		functions.add(new CalcSymbolResourceData_cv());
		functions.add(new CalcSymbolResourceData_scv());
		functions.add(new CalcSymbolResourceData_sk());
		functions.add(new CalcSymbolResourceData_kurt());
		functions.add(new CalcSymbolResourceData_hist());
		functions.add(new CalcSymbolResourceCosts_sum());
		functions.add(new CalcSymbolResourceDown());

		functions.add(new CalcSymbolTransporterCount());
		functions.add(new CalcSymbolTransporterCapacity());
		functions.add(new CalcSymbolTransporterData_current());
		functions.add(new CalcSymbolTransporterData_avg());
		functions.add(new CalcSymbolTransporterData_min());
		functions.add(new CalcSymbolTransporterData_max());
		functions.add(new CalcSymbolTransporterData_var());
		functions.add(new CalcSymbolTransporterData_std());
		functions.add(new CalcSymbolTransporterData_cv());
		functions.add(new CalcSymbolTransporterData_scv());
		functions.add(new CalcSymbolTransporterData_sk());
		functions.add(new CalcSymbolTransporterData_kurt());
		functions.add(new CalcSymbolTransporterData_hist());
		functions.add(new CalcSymbolTransporterDown());

		functions.add(new CalcSymbolClientNumber());
		functions.add(new CalcSymbolClientUserData());
		functions.add(new CalcSymbolClientStatistics());
		functions.add(new CalcSymbolClientWarmUp());
		functions.add(new CalcSymbolClientResourceAlternative());
		functions.add(new CalcSymbolClientLastStation());
		functions.add(new CalcSymbolClientCostWaiting_sum());
		functions.add(new CalcSymbolClientCostWaiting_avg());
		functions.add(new CalcSymbolClientCostWaiting_current());
		functions.add(new CalcSymbolClientCostTransfer_sum());
		functions.add(new CalcSymbolClientCostTransfer_avg());
		functions.add(new CalcSymbolClientCostTransfer_current());
		functions.add(new CalcSymbolClientCostProcess_sum());
		functions.add(new CalcSymbolClientCostProcess_avg());
		functions.add(new CalcSymbolClientCostProcess_current());
		functions.add(new CalcSymbolClientCurrentWaitingTime());
		functions.add(new CalcSymbolClientBatchSize());

		functions.add(new CalcSymbolUserStatistics_current());
		functions.add(new CalcSymbolUserStatistics_avg());
		functions.add(new CalcSymbolUserStatistics_median());
		functions.add(new CalcSymbolUserStatistics_quantil());
		functions.add(new CalcSymbolUserStatistics_min());
		functions.add(new CalcSymbolUserStatistics_max());
		functions.add(new CalcSymbolUserStatistics_var());
		functions.add(new CalcSymbolUserStatistics_std());
		functions.add(new CalcSymbolUserStatistics_cv());
		functions.add(new CalcSymbolUserStatistics_scv());
		functions.add(new CalcSymbolUserStatistics_sk());
		functions.add(new CalcSymbolUserStatistics_hist());
	}

	@Override
	protected List<CalcSymbolPreOperator> getUserFunctions() {
		return functions;
	}

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Zahlenwert des Ergebnisses.
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	public double calc(double[] variableValues, final SimulationData simData, final RunDataClient client) throws MathCalcError {
		if (simData!=null && (runElements==null || this.simData!=simData)) {
			this.simData=simData;
			prepareRunElementData();
		}
		currentClient=client;
		return super.calc(variableValues);
	}

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @param simData	Simulationsdatenobjekt aus dem die Daten für die Simulationsdaten-Funktionen entnommen werden sollen
	 * @param client	Aktueller Kunde der die Daten für die Kunden-Simulationsdaten-Funktionen liefert
	 * @param fallbackValue	Vorgabewert der zurückgegeben werden soll, wenn die Berechnung fehlgeschlagen ist
	 * @return	Zahlenwert des Ergebnisses oder im Fehlerfall der Vorgabewert
	 */
	public double calcOrDefault(double[] variableValues, final SimulationData simData, final RunDataClient client, final double fallbackValue) {
		if (isConstValue()) return getConstValue();

		if (simData!=null && (runElements==null || this.simData!=simData)) {
			this.simData=simData;
			prepareRunElementData();
		}
		currentClient=client;
		return super.calcOrDefault(variableValues,fallbackValue);
	}

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param statistics	Statistikobjekt dem die Daten für die Simulationsdaten-Funktionen entnommen werden sollen
	 * @return	Zahlenwert des Ergebnisses
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	public double calc(final Statistics statistics) throws MathCalcError {
		if (statistics==null) throw new MathCalcError(this);
		final SimulationData simData=Simulator.getSimulationDataFromStatistics(statistics);
		if (simData==null) throw new MathCalcError(this);

		simData.runData.initRun(0,simData,simData.runModel.recordIncompleteClients);
		for (RunElement station: simData.runModel.elementsFast) if (station!=null) simData.runData.explicitInitStatistics(simData,station);

		return calc(null,simData,null);
	}

	/**
	 * Prüft direkt, ob ein als Zeichenkette angegebener Ausdruck korrekt interpretierbar ist.
	 * @param expression	Zu prüferender Ausdruck
	 * @param variables	Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein)
	 * @return	Liefert -1, wenn der Ausdruck erfolgreich interpretiert werden konnte, ansonsten die 0-basierende Fehlerstelle innerhalb des Strings.
	 */
	public static int check(final String expression, final String[] variables) {
		final ExpressionCalc calc=new ExpressionCalc(variables);
		return calc.parse(expression);
	}

	/**
	 * Prüft, ob eine Zeichenkette einen gültigen möglichen Variablennamen darstellt.
	 * @param variableName	Zu prüfender möglicher Variablenname
	 * @return	Gibt <code>true</code> zurück, wenn die Zeichenkette als Variablenname verwendet werden kann.
	 */
	public static boolean checkVariableName(final String variableName) {
		if (variableName==null || variableName.trim().isEmpty()) return false;
		final ExpressionCalc calc=new ExpressionCalc(null);
		if (calc.isKnownSymbol(variableName)) return false;
		for (int i=0;i<variableName.length();i++) {
			final char c=variableName.charAt(i);
			if (c>='a' && c<='z') continue;
			if (c>='A' && c<='Z') continue;
			if (c=='ä' || c=='Ä' || c=='ö' || c=='Ö' || c=='ü' || c=='Ü' || c=='_' || c=='ß') continue;
			if (i>0) {
				if (c>='0' && c<='9') continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Übersetzungsindex von internen Nummern zu Texten.
	 * @see #parse(String)
	 * @see #getStationIDFromTranslationIndex(int)
	 */
	private Map<String,List<String>> textTranslation;

	/**
	 * Cache zur Übersetzung von Stations-Übersetzungs-Indices
	 * zu Stations-IDs.
	 *  @see #getStationIDFromTranslationIndex(int)
	 */
	private int[] stationTranslationCache;

	/**
	 * Aktueller Modus beim Parsen
	 * @see ExpressionCalc#parse(String)
	 */
	private enum Mode {
		/** Zeichen an primären Parser durchreichen */
		NORMAL,
		/** Erstes Startzeichen für Textausdruck erkannt */
		START_STEP1,
		/** Zweites Startzeichen für Textausdruck erkannt */
		START_STEP2,
		/** Innerhalb eines Textausdrucks */
		TEXT_CONTENT,
		/** Escape-Zeichen innerhalb eines Textausdrucks erkannt */
		ESCAPE
	}

	/**
	 * Speichert das {@link StringBuilder}-Objekt für eine spätere
	 * Verwendung zwischen.
	 * @see #parse(String)
	 */
	private StringBuilder parseStringBuilder;

	@Override
	public int parse(final String text) {
		if (text==null || text.isEmpty()) return 0;

		if (text.indexOf('$')<0 && text.indexOf('§')<0) return super.parse(text);

		char type=' ';
		Mode mode=Mode.NORMAL;
		char delimiter=' ';

		if (parseStringBuilder==null) parseStringBuilder=new StringBuilder(); else parseStringBuilder.setLength(0);
		final StringBuilder sb=parseStringBuilder;
		StringBuilder sub=null;
		final int len=text.length();
		for (int i=0;i<len;i++) {
			final char c=text.charAt(i);

			switch (mode) {
			case NORMAL:
				sb.append(c);
				if (c=='$' || c=='§') {
					type=c;
					mode=Mode.START_STEP1;
				}
				break;
			case START_STEP1:
				sb.append(c);
				if (c=='(') mode=Mode.START_STEP2; else mode=Mode.NORMAL;
				break;
			case START_STEP2:
				if (c=='"' || c=='\'') {delimiter=c; mode=Mode.TEXT_CONTENT; break;}
				mode=Mode.NORMAL; sb.append(c);
				break;
			case TEXT_CONTENT:
				if (c==delimiter) {
					if (textTranslation==null) textTranslation=new HashMap<>();
					final List<String> list=textTranslation.computeIfAbsent(""+type,k->new ArrayList<>());
					if (sub==null) list.add(""); else list.add(sub.toString());
					sub=null;
					sb.append(""+(list.size()-1));
					mode=Mode.NORMAL;
				} else {
					if (c=='\\') {
						mode=Mode.ESCAPE;
					} else {
						if (sub==null) sub=new StringBuilder();
						sub.append(c);
					}
				}
				break;
			case ESCAPE:
				if (sub==null) sub=new StringBuilder();
				if (c==delimiter) sub.append(delimiter); else {sub.append('\\'); sub.append(c);}
				mode=Mode.TEXT_CONTENT;
				break;
			}
		}
		return super.parse(sb.toString());
	}

	/**
	 * Liefert basierend auf dem Namen einer Station die zugehörige ID
	 * @param name	Name der Station
	 * @return	Zugehörige ID oder -1, wenn keine Station mit dem angegebenen Namen existiert
	 */
	private int getStationIDFromName(final String name) {
		Integer I=simData.runModel.namesToIDs.get(name);
		if (I==null) return -1;
		return I;
	}

	/**
	 * Beim Parsen werden Zeichenketten aus der Zeichenkette entfernt und in eine
	 * Übersetzungstabelle eingetragen. In der Parse-Zeichenkette werden dann die
	 * Index-Werte eingetragen.
	 * @param index	Index, der zu einer Stations-ID aufgelöst werden soll
	 * @return	Stations-ID oder -1, wenn es keine Station mit dem angegebenen Namen gibt
	 */
	public int getStationIDFromTranslationIndex(final int index) {
		if (textTranslation==null) return -1;
		final List<String> list=textTranslation.get("$");
		if (list==null) return -1;
		if (simData==null) return -1;

		if (stationTranslationCache==null) {
			stationTranslationCache=new int[list.size()];
			Arrays.fill(stationTranslationCache,-2);
		}

		if (index<0 || index>=stationTranslationCache.length) return -1;

		if (stationTranslationCache[index]==-2) stationTranslationCache[index]=getStationIDFromName(list.get(index));

		return stationTranslationCache[index];
	}

	/**
	 * Liefert einen Text, der beim Parsen durch einen Index ersetzt wurde.
	 * @param key	Startzeichen für die Textbereichs-Erkennung
	 * @param index	Index in dem Rechenausdruck
	 * @return	Ursprünglicher Text an der Stelle
	 */
	public String getTextContent(final String key, final int index) {
		if (textTranslation==null) return null;
		final List<String> list=textTranslation.get(key);
		if (list==null) return null;
		if (index<0 || index>=list.size()) return null;
		return list.get(index);
	}

	/**
	 * Arbeitet ähnlich wie {@link CalcSystem#calcSimple(String)},
	 * nutzt aber alle Symbole von {@link ExpressionCalc}.
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Ergebniswert oder <code>null</code>, wenn der Wert sich nicht zu einem statischen Ergebnis berechnen lässt
	 */
	public static Double calcDirect(final String expression) {
		final ExpressionCalc calc=new ExpressionCalc(null);
		if (calc.parse(expression)>=0) return null;
		if (!calc.isConstValue()) return null;
		try {
			return calc.calc();
		} catch (MathCalcError e) {
			return null;
		}
	}
}