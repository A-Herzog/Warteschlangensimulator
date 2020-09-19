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
import java.util.List;

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
	private RunElementData[] runElementData;
	private RunElement[] runElements;
	private StatisticsTimePerformanceIndicator[] resourceUsage;
	private StatisticsTimePerformanceIndicator[] transporterUsage;
	private SimulationData simData;
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

	private void prepareRunElementData() {
		/* Daten zu den Elementen */
		runElements=Arrays.copyOf(simData.runModel.elementsFast,simData.runModel.elementsFast.length);
		runElementData=new RunElementData[runElements.length];
		for (int i=0;i<runElements.length;i++) if (runElements[i]!=null) runElementData[i]=runElements[i].getData(simData);

		/* Daten zu den Stationen */
		resourceUsage=simData.runData.resources.getUsageStatistics(simData);
		if (resourceUsage==null) resourceUsage=new StatisticsTimePerformanceIndicator[0];
		transporterUsage=simData.runData.transporters.getUsageStatistics(simData);
		if (transporterUsage==null) transporterUsage=new StatisticsTimePerformanceIndicator[0];
	}

	@Override
	protected List<CalcSymbolPreOperator> getUserFunctions() {
		List<CalcSymbolPreOperator> functions=new ArrayList<>(256);

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
		functions.add(new CalcSymbolStationDataResidence_hist());
		functions.add(new CalcSymbolStationDataResidence_histAll());

		functions.add(new CalcSymbolAnalogValue());
		functions.add(new CalcSymbolAnalogRate());
		functions.add(new CalcSymbolAnalogValve());

		functions.add(new CalcSymbolStationDataNumberIn());
		functions.add(new CalcSymbolStationDataNumberOut());
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
		functions.add(new CalcSymbolUserStatistics_hist());

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
		if (simData!=null && (runElementData==null || runElements==null || this.simData!=simData)) {
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

		if (simData!=null && (runElementData==null || runElements==null || this.simData!=simData)) {
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

	private List<String> stationTranslation;
	private int[] stationTranslationCache;

	private StringBuilder parseStringBuilder;

	@Override
	public int parse(final String text) {
		if (text==null || text.isEmpty()) return 0;

		if (text.indexOf('$')<0) return super.parse(text);

		int mode=0;

		if (parseStringBuilder==null) parseStringBuilder=new StringBuilder(); else parseStringBuilder.setLength(0);
		final StringBuilder sb=parseStringBuilder;
		StringBuilder sub=null;
		final int len=text.length();
		for (int i=0;i<len;i++) {
			final char c=text.charAt(i);

			switch (mode) {
			case 0:
				sb.append(c);
				if (c=='$') mode=1;
				break;
			case 1:
				sb.append(c);
				if (c=='(') mode=2; else mode=0;
				break;
			case 2:
				if (c=='"') {mode=3; break;}
				if (c=='\'') {mode=5; break;}
				mode=0; sb.append(c);
				break;
			case 3:
				if (c=='"') {
					if (stationTranslation==null) stationTranslation=new ArrayList<>();
					if (sub==null) stationTranslation.add(""); else stationTranslation.add(sub.toString());
					sub=null;
					sb.append(""+(stationTranslation.size()-1));
					mode=0;
				} else {
					if (c=='\\') {
						mode=4;
					} else {
						if (sub==null) sub=new StringBuilder();
						sub.append(c);
					}
				}
				break;
			case 4:
				if (sub==null) sub=new StringBuilder();
				if (c=='"') sub.append('"'); else {sub.append('\\'); sub.append(c);}
				mode=3;
				break;
			case 5:
				if (c=='\'') {
					if (stationTranslation==null) stationTranslation=new ArrayList<>();
					if (sub==null) stationTranslation.add(""); else stationTranslation.add(sub.toString());
					sub=null;
					sb.append(""+(stationTranslation.size()-1));
					mode=0;
				} else {
					if (c=='\\') {
						mode=6;
					} else {
						if (sub==null) sub=new StringBuilder();
						sub.append(c);
					}
				}
				break;
			case 6:
				if (sub==null) sub=new StringBuilder();
				if (c=='\'') sub.append('\''); else {sub.append('\\'); sub.append(c);}
				mode=5;
				break;

			}
		}
		return super.parse(sb.toString());
	}

	private int getStationIDFromName(final String name) {
		Integer I=simData.runModel.namesToIDs.get(name);
		if (I==null) return -1;
		return I;
	}

	/**
	 * Beim Parsen werden Zeichenketten aus der Zeichenkette entfern und in eine
	 * Übersetzungstabelle eingetragen. In der Parse-Zeichenkette werden dann die
	 * Index-Werte eingetragen.
	 * @param index	Index, der zu einer Stations-ID aufgelöst werden soll
	 * @return	Stations-ID oder -1, wenn es keine Station mit dem angegebenen Namen gibt
	 */
	public int getStationIDFromTranslationIndex(final int index) {
		if (stationTranslation==null || stationTranslation.isEmpty()) return -1;
		if (simData==null) return -1;

		if (stationTranslationCache==null) {
			stationTranslationCache=new int[stationTranslation.size()];
			Arrays.fill(stationTranslationCache,-2);
		}

		if (index<0 || index>=stationTranslationCache.length) return -1;

		if (stationTranslationCache[index]==-2) stationTranslationCache[index]=getStationIDFromName(stationTranslation.get(index));

		return stationTranslationCache[index];
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