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
package scripting.js;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.elements.RunElementAnalogValue;
import simulator.elements.RunElementTank;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import tools.NetHelper;

/**
 * Stellt das "System"- bzw. "Simulation"-Objekt in Javascript-Umgebungen zur Verfügung
 * @author Alexander Herzog
 */
public final class JSCommandSystem extends JSBaseCommand {
	private SimulationData simData;
	private RunDataClient client;
	private double inputValue;

	private Map<String,ExpressionCalc> expressionCache;

	/**
	 * Konstruktor der Klasse <code>JSFilterCommandSystem</code>
	 * 	 */
	public JSCommandSystem() {
		super(null);
		expressionCache=new HashMap<>();
		inputValue=0.0;
	}

	/**
	 * Stellt die Simulationsdaten und den aktuellen Kunden für die Abfrage durch das Javascript-Verknüpfungs-Objekt ein
	 * @param simData	Simulationsdaten-Objekt (kann auch <code>null</code> sein)
	 * @param client	Aktueller Kunde (kann auch <code>null</code> sein)
	 */
	public void setSimulationData(final SimulationData simData, final RunDataClient client) {
		this.simData=simData;
		this.client=client;
	}

	/**
	 * Stellt eine Eingangsgröße für die Abfrage durch das Javascript-Verknüpfungs-Objekt ein
	 * @param value	Eingangsgröße (z.B. aus einer Datei geladener Zahlenwert)
	 */
	public void setSimulationInputValue(final double value) {
		inputValue=value;
	}

	private Object getExpression(final String text) {
		ExpressionCalc expression=expressionCache.get(text);
		if (expression!=null) return expression;

		if (simData==null) {
			expression=new ExpressionCalc(new String[0]);
		} else {
			expression=new ExpressionCalc(simData.runModel.variableNames);
		}
		final int errorPos=expression.parse(text);
		if (errorPos>=0) return String.format(Language.tr("Statistics.Filter.CoundNotProcessExpression.Info"),errorPos+1);
		expressionCache.put(text,expression);
		return expression;
	}

	/**
	 * Berechnet den übergebenen String und liefert das Ergebnis als Double
	 * @param obj	Zu berechnender String
	 * @return	Liefert das Ergebnis der Berechnung als Double oder eine Fehlermeldung als String oder <code>null</code>, wenn kein String als Parameter übergeben wurde
	 */
	public Object calc(final Object obj) {
		if (obj instanceof Integer) return obj;
		if (obj instanceof Double) return obj;
		if (obj instanceof Long) return obj;

		if (!(obj instanceof String)) return null;

		final Object result=getExpression((String)obj);
		if (result instanceof String) return result;
		final ExpressionCalc calc=(ExpressionCalc)result;

		try {
			if (simData==null) {
				return NumberTools.fastBoxedValue(calc.calc());
			} else {
				return NumberTools.fastBoxedValue(calc.calc(simData.runData.variableValues,simData,client)); /* Client kann evtl. nicht gesetzt und damit <code>null</code> sein. Das ist ok. */
			}
		} catch (MathCalcError e) {
			return Language.tr("Statistics.Filter.CoundNotProcessExpression.Title");
		}
	}

	/**
	 * Ist per <code>setSimulationData</code> ein Simulationsdaten-Objekt gesetzt, so liefert
	 * diese Funktion die aktuelle Simulationszeiten als Doublewert in Sekunden. Ist kein
	 * Simulationsdaten-Objekt eingetragen, so liefert die Funktion die aktuelle Computerzeit
	 * in Millisekunden für Laufzeitmessungen.
	 * @return	Long-Wert, welcher die aktuelle Zeit liefert
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 */
	public Object time() {
		if (simData==null) {
			/* Aufruf aus Statistik-Ausgabe, dann Ausgabe der Computer-Zeit (in Millisekunden) für Skript-Laufzeitmessungen */
			return System.currentTimeMillis();
		} else {
			/* Aufruf aus laufender Simulation, dann Ausgabe der aktuellen Zeit in der Simulation (in Sekunden) */
			return simData.currentTime/1000.0;
		}
	}

	/**
	 * Gibt an, ob sich die Simulation noch in der Einschwingphase befindet.
	 * @return	Gibt <code>true</code> zurück, wenn sich das System noch in der Einschwingphase befindet
	 */
	public boolean isWarmUp() {
		if (simData==null) return false;
		return simData.runData.isWarmUp;
	}

	/**
	 * Gibt an, ob der Kunde während der Einschwingphase generiert wurde.
	 * @return	Gibt <code>true</code> zurück, wenn der Kunde während der Einschwingphase generiert wurde
	 */
	public boolean isWarmUpClient() {
		if (simData==null || client==null) return false;
		return client.isWarmUp;
	}

	/**
	 * Gibt an, ob der Kunde in der Statistik erfasst werden soll.<br>
	 * Diese Einstellung ist unabhängig von der Einschwingphase. Ein Kunde wird nur erfasst, wenn er außerhalb
	 * der Einschwingphase generiert wurde und hier nicht falsch zurückgeliefert wird.
	 * @return	Erfassung des Kunden in der Statistik
	 */
	public boolean isClientInStatistics() {
		if (simData==null || client==null) return false;
		return client.inStatistics;
	}

	/**
	 * Stellt ein, ob der Kunde in der Statistik erfasst werden soll.<br>
	 * Diese Einstellung ist unabhängig von der Einschwingphase. Ein Kunde wird nur erfasst, wenn er außerhalb
	 * der Einschwingphase generiert wurde und hier nicht falsch eingestellt wurde.
	 * @param inStatistics	Erfassung des Kunden in der Statistik
	 */
	public void setClientInStatistics(final boolean inStatistics) {
		if (simData==null || client==null) return;
		client.inStatistics=inStatistics;
	}

	/**
	 * Liefert die bei 1 beginnende, fortlaufende Nummer des aktuellen Kunden.
	 * @return	Fortlaufende Nummer des Kunden
	 */
	public long clientNumber() {
		if (simData==null || client==null) return 0;
		return client.clientNumber;
	}

	/**
	 * Liefert den Namen des aktuellen Kunden
	 * @return	Name des Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 */
	public String clientTypeName() {
		if (simData==null || client==null) return "";
		return simData.runModel.clientTypes[client.type];
	}

	/**
	 * Liefert die bisherige Wartezeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Wartezeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientWaitingTime()
	 */
	public double clientWaitingSeconds() {
		if (simData==null || client==null) return 0.0;
		return client.waitingTime/1000.0;
	}

	/**
	 * Liefert die bisherige Wartezeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Wartezeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientWaitingSeconds()
	 */
	public String clientWaitingTime() {
		if (simData==null || client==null) return "";
		return TimeTools.formatExactTime(((double)client.waitingTime)/1000);
	}

	/**
	 * Stellt die bisherige Wartezeit des Kunden, der die Verarbeitung des Skripts ausgelöst hat, ein.
	 * @param seconds	Neue Wartezeit (in Sekunden)
	 */
	public void clientWaitingSecondsSet(final double seconds) {
		final long l=(long)(seconds*1000+0.5);
		client.waitingTime=(l>0)?l:0;
	}

	/**
	 * Liefert die bisherige Transferzeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Transferzeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientTransferTime()
	 */
	public double clientTransferSeconds() {
		if (simData==null || client==null) return 0.0;
		return client.transferTime/1000.0;
	}

	/**
	 * Liefert die bisherige Transferzeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Transferzeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientTransferSeconds()
	 */
	public String clientTransferTime() {
		if (simData==null || client==null) return "";
		return TimeTools.formatExactTime(((double)client.transferTime)/1000);
	}

	/**
	 * Stellt die bisherige Transferzeit des Kunden, der die Verarbeitung des Skripts ausgelöst hat, ein.
	 * @param seconds	Neue Transferzeit (in Sekunden)
	 */
	public void clientTransferSecondsSet(final double seconds) {
		final long l=(long)(seconds*1000+0.5);
		client.transferTime=(l>0)?l:0;
	}

	/**
	 * Liefert die bisherige Bedienzeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Bedienzeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientProcessTime()
	 */
	public double clientProcessSeconds() {
		if (simData==null || client==null) return 0.0;
		return client.processTime/1000.0;
	}

	/**
	 * Liefert die bisherige Bedienzeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Bedienzeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientProcessSeconds()
	 */
	public String clientProcessTime() {
		if (simData==null || client==null) return "";
		return TimeTools.formatExactTime(((double)client.processTime)/1000);
	}

	/**
	 * Stellt die bisherige Bedienzeit des Kunden, der die Verarbeitung des Skripts ausgelöst hat, ein.
	 * @param seconds	Neue Bedienzeit (in Sekunden)
	 */
	public void clientProcessSecondsSet(final double seconds) {
		final long l=(long)(seconds*1000+0.5);
		client.processTime=(l>0)?l:0;
	}

	/**
	 * Liefert die bisherige Verweilzeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Verweilzeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientResidenceTime()
	 */
	public double clientResidenceSeconds() {
		if (simData==null || client==null) return 0.0;
		return client.residenceTime/1000.0;
	}

	/**
	 * Liefert die bisherige Verweilzeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Verweilzeit des aktuellen Kunden
	 * @see JSCommandSystem#setSimulationData(SimulationData, RunDataClient)
	 * @see JSCommandSystem#clientResidenceSeconds()
	 */
	public String clientResidenceTime() {
		if (simData==null || client==null) return "";
		return TimeTools.formatExactTime(((double)client.residenceTime)/1000);
	}

	/**
	 * Stellt die bisherige Verweilzeit des Kunden, der die Verarbeitung des Skripts ausgelöst hat, ein.
	 * @param seconds	Neue Verweilzeit (in Sekunden)
	 */
	public void clientResidenceSecondsSet(final double seconds) {
		client.residenceTime=FastMath.round(FastMath.max(0,seconds)*1000);
	}

	/**
	 * Liefert einen zu dem Kunden gespeicherten Zahlenwert
	 * @param index	Index zu dem der Zahlenwert abgerufen werden soll
	 * @return	Zahlenwert zu dem Index für den Kunden. (Ist kein Wert für den Index gesetzt, so wird 0.0 zurückgeliefert.)
	 */
	public double getClientValue(final int index) {
		if (simData==null || client==null) return 0.0;
		return client.getUserData(index);
	}

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index für den Kunden
	 */
	public void setClientValue(final int index, final int value) {
		if (simData==null || client==null) return;
		client.setUserData(index,value);
	}

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index für den Kunden
	 */
	public void setClientValue(final int index, final double value) {
		if (simData==null || client==null) return;
		client.setUserData(index,value);
	}

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index für den Kunden
	 */
	public void setClientValue(final int index, final Object value) {
		if (simData==null || client==null) return;
		final Object result=calc(value);
		if (result instanceof Double) client.setUserData(index,((Double)result).doubleValue());
		if (result instanceof Integer) client.setUserData(index,((Integer)result).intValue());
		if (result instanceof Long) client.setUserData(index,((Long)result).intValue());
	}

	/**
	 * Liefert einen zu dem Kunden gespeicherten Textwert
	 * @param key	Schlüssel zu dem der Textwert abgerufen werden soll
	 * @return	Textwert zu dem Schlüssel für den Kunden. (Ist kein Wert für den Schlüssel gesetzt, so wird ein leerer String zurückgeliefert.)
	 */
	public String getClientText(final String key) {
		if (simData==null || client==null) return "";
		return client.getUserDataString(key);
	}


	/**
	 * Stellt einen Textwert für einen Kunden ein
	 * @param key	Schlüssel zu dem der Textwert eingestellt werden soll
	 * @param value	Textwert der zu dem Schlüssel hinterlegt werden soll
	 */
	public void setClientText(final String key, final String value) {
		if (simData==null || client==null) return;
		if (key==null || key.trim().isEmpty()) return;
		client.setUserDataString(key,(value==null)?"":value);
	}

	/**
	 * Liefert die Anzahl an Kunden an einer Station
	 * @param id	ID der Station
	 * @return	Anzahl an Kunden an der Station
	 */
	public int getWIP(final int id) {
		if (simData==null) return 0;
		if (id<0 || id>=simData.runModel.elementsFast.length) return 0;
		final RunElement element=simData.runModel.elementsFast[id];
		if (element==null) return 0;
		return element.getData(simData).reportedClientsAtStation(simData);
	}

	/**
	 * Liefert die Anzahl an Kunden in der Warteschlange an einer Station
	 * @param id	ID der Station
	 * @return	Anzahl an Kunden in der Warteschlange an der Station
	 */
	public int getNQ(final int id) {
		if (simData==null) return 0;
		if (id<0 || id>=simData.runModel.elementsFast.length) return 0;
		final RunElement element=simData.runModel.elementsFast[id];
		if (element==null) return 0;
		return element.getData(simData).clientsAtStationQueue;
	}

	/**
	 * Liefert die Anzahl an Kunden an allen Stationen zusammen
	 * @return	Anzahl an Kunden an allen Stationen zusammen
	 */
	public int getWIP() {
		if (simData==null) return 0;

		return simData.statistics.clientsInSystem.getCurrentState();
	}

	/**
	 * Liefert die Anzahl an Kunden in der Warteschlange an allen Stationen zusammen
	 * @return	Anzahl an Kunden in der Warteschlange an allen Stationen zusammen
	 */
	public int getNQ() {
		if (simData==null) return 0;

		return simData.statistics.clientsInSystemQueues.getCurrentState();
	}

	private int getVariableIndex(final String variableName) {
		if (simData==null) return -1;

		/* Normale Variablen */
		for (int i=0;i<simData.runModel.variableNames.length;i++) if (simData.runModel.variableNames[i].equalsIgnoreCase(variableName)) return i;

		/* ClientData-Felder */
		final int index=CalcSymbolClientUserData.testClientData(variableName);
		if (index>=0) return -2-index;

		return -1;
	}

	private void setValueInt(final int index, final double value) {
		if (simData==null) return;
		if (index>=0) {
			simData.runData.variableValues[index]=value;
			/* Pseudovariable: Wartezeit */
			if (client!=null && index==simData.runData.variableValues.length-3) client.waitingTime=FastMath.max(0,FastMath.round(value*1000));
			/* Pseudovariable: Transferzeit */
			if (client!=null && index==simData.runData.variableValues.length-2) client.transferTime=FastMath.max(0,FastMath.round(value*1000));
			/* Pseudovariable: Bedienzeit */
			if (client!=null && index==simData.runData.variableValues.length-1)	client.processTime=FastMath.max(0,FastMath.round(value*1000));
		} else {
			client.setUserData(-index-2,value);
		}
	}

	/**
	 * Setzt den Wert einer Simulator-Variable. Die Variable muss bereits existieren, sonst erfolgt keine Zuweisung.
	 * @param varName	Name der Variable
	 * @param varValue	Neuer Wert (Integer, Double oder String, der dann zunächst interpretiert wird)
	 */
	public void set(final Object varName, final Object varValue) {
		if (simData==null) return;
		if (varName==null || varValue==null || !(varName instanceof String)) return;
		if (!(varValue instanceof String) && !(varValue instanceof Double) && !(varValue instanceof Integer) && !(varValue instanceof Long)) return;

		final String clientKey=CalcSymbolClientUserData.testClientDataString((String)varName);
		if (clientKey!=null) {
			if (varValue instanceof Integer) setClientText(clientKey,NumberTools.formatLong((Integer)varValue));
			if (varValue instanceof Long) setClientText(clientKey,NumberTools.formatLong((Long)varValue));
			if (varValue instanceof Double) setClientText(clientKey,NumberTools.formatNumber((Double)varValue));
			if (varValue instanceof String) setClientText(clientKey,(String)varValue);
			return;
		}

		final int index=getVariableIndex((String)varName);
		if (index==-1) return;

		if (varValue instanceof Number) {
			setValueInt(index,((Number)varValue).doubleValue());
			return;
		}
		if (varValue instanceof String) {
			final ExpressionCalc calc=new ExpressionCalc(simData.runModel.variableNames);
			if (calc.parse((String)varValue)>=0) return;
			try {
				final double d=calc.calc(simData.runData.variableValues,simData,null);
				setValueInt(index,d);
			} catch (MathCalcError e) {}
			return;
		}
	}

	private RunElement getRunElement(final int id) {
		if (id<0 || id>=simData.runModel.elementsFast.length) return null;
		return simData.runModel.elementsFast[id];
	}

	private RunElement getRunElement(final Object id) {
		if (id==null) return null;
		if (id instanceof Integer) return getRunElement(((Integer)id).intValue());
		if (id instanceof Long) return getRunElement(((Long)id).intValue());
		if (id instanceof Double) return getRunElement((int)FastMath.round((Double)id));
		if (id instanceof String) {
			final ExpressionCalc calc=new ExpressionCalc(simData.runModel.variableNames);
			if (calc.parse((String)id)>=0) return null;
			try {
				final double d=calc.calc(simData.runData.variableValues,simData,null);
				return getRunElement((int)FastMath.round(d));
			} catch (MathCalcError e) {
				return null;
			}
		}
		return null;
	}

	private Double evaluateValue(final Object value) {
		if (value instanceof Double) return (Double)value;
		if (value instanceof Integer) return ((Integer)value).doubleValue();
		if (value instanceof Long) return ((Long)value).doubleValue();
		if (value instanceof String) {
			final ExpressionCalc calc=new ExpressionCalc(simData.runModel.variableNames);
			if (calc.parse((String)value)>=0) return null;
			try {
				final Double D=NumberTools.fastBoxedValue(calc.calc(simData.runData.variableValues,simData,null));
				return D;
			} catch (MathCalcError e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Stellt den aktuellen Wert an einem "Analoger Wert"- oder "Tank"-Element ein.
	 * @param elementID	ID des Elements an dem der Wert eingestellt werden soll
	 * @param value	Neuer Wert (Zahl oder berechenbarer Ausdruck)
	 */
	public void setValue(final Object elementID, final Object value) {
		if (simData==null) return;
		if (elementID==null || value==null) return;

		final Double D=evaluateValue(value);
		if (D==null) return;
		final double val=D.doubleValue();

		final RunElement element=getRunElement(elementID);
		if (element instanceof RunElementAnalogValue) ((RunElementAnalogValue)element).getData(simData).setValue(simData,val);
		if (element instanceof RunElementTank) ((RunElementTank)element).getData(simData).setValue(simData,val);
	}

	/**
	 * Stellt die aktuelle Änderungsrate an einem "Analoger Wert"-Element ein.
	 * @param elementID	ID des Elements an dem die Änderungsrate eingestellt werden soll
	 * @param value	Neuer Wert (Zahl oder berechenbarer Ausdruck)
	 */
	public void setRate(final Object elementID, final Object value) {
		if (simData==null) return;
		if (elementID==null || value==null) return;

		final Double D=evaluateValue(value);
		if (D==null) return;
		final double val=D.doubleValue();

		final RunElement element=getRunElement(elementID);
		if (element instanceof RunElementAnalogValue) ((RunElementAnalogValue)element).getData(simData).setRate(simData,val);
	}

	/**
	 * Stellt den maximalen Durchfluss an einem Ventil eines Tanks ein
	 * @param elementID	ID des Tanks an dem der maximale Durchfluss an einem Ventil eingestellt werden soll
	 * @param valveNr	1-basierte Nummer des Ventils
	 * @param value	Neuer Wert (Zahl oder berechenbarer Ausdruck)
	 */
	public void setValveMaxFlow(final Object elementID, final Object valveNr, final Object value) {
		if (simData==null) return;
		if (elementID==null || value==null) return;

		final Double D=evaluateValue(value);
		if (D==null) return;
		final double val=D.doubleValue();

		final Double E=evaluateValue(valveNr);
		if (E==null) return;
		final int nr=((int)FastMath.round(E.doubleValue()))-1;

		final RunElement element=getRunElement(elementID);
		if (!(element instanceof RunElementTank)) return;
		final RunElementTank tank=(RunElementTank)element;
		tank.getData(simData).setValveMaxFlow(nr,val);
	}

	/**
	 * Liefert den eingestellten aktuellen Eingabewert (z.B. aus einer Eingabedatei)
	 * @return	Eingabewert
	 */
	public double getInput() {
		return inputValue;
	}

	/**
	 * Lädt einen einzelnen Wert von einer Internetadresse
	 * @param url	Aufzurufende URL
	 * @param errorValue	Wert der im Fehlerfall zurückgeliefert werden soll
	 * @return	Gelesener Wert oder <code>errorValue</code> im Fehlerfall
	 */
	public double getInput(final String url, final double errorValue) {
		URL urlObj=null;
		try {
			urlObj=new URL(url);
		} catch (MalformedURLException e) {
			return errorValue;
		}

		final String text=NetHelper.loadText(urlObj,false,false);
		if (text==null) return errorValue;
		final Double D=NumberTools.getDouble(text);
		if (D==null) return errorValue;
		return D.doubleValue();
	}

	/**
	 * Liefert die Anzahl an vorhandenen Bedienern über alle Bedienergruppen.
	 * @return	Anzahl an vorhandenen Bedienern über alle Bedienergruppen
	 */
	public int getAllResourceCount() {
		if (simData==null) return 0;
		return simData.runData.resources.getAllCount(simData);
	}

	/**
	 * Liefert die Anzahl an vorhandenen Bedienern in einer bestimmten Bedienergruppe.
	 * @param resourceId	1-basierende ID der Bedienergruppe
	 * @return	Anzahl an vorhandenen Bedienern
	 */
	public int getResourceCount(final int resourceId) {
		if (simData==null) return 0;
		return simData.runData.resources.getCount(resourceId-1,simData);
	}

	/**
	 * Stellt die Anzahl an vorhandenen Bedienern in einer bestimmten Bedienergruppe ein.
	 * @param resourceId	1-basierende ID der Bedienergruppe
	 * @param count	Anzahl an vorhandenen Bedienern
	 * @return	Liefert <code>true</code> zurück, wenn die Anzahl verändert werden konnte.
	 */
	public boolean setResourceCount(final int resourceId, final int count) {
		if (simData==null) return false;
		return simData.runData.resources.setCount(resourceId-1,simData,count);
	}

	/**
	 * Gibt an, wie viele Bediener eines bestimmten Typs zu einem Zeitpunkt in Ausfallzeit sind
	 * @param resourceId	1-basierender Index der Bedienergruppe
	 * @return	Anzahl an Bedienern
	 */
	public int getResourceDown(final int resourceId) {
		if (simData==null) return 0;
		return simData.runData.resources.getDown(resourceId-1,simData);
	}

	/**
	 * Gibt an, wie viele Bediener zu einem Zeitpunkt insgesamt in Ausfallzeit sind
	 * @return	Anzahl an Bedienern
	 */
	public int getAllResourceDown() {
		if (simData==null) return 0;
		return simData.runData.resources.getAllDown(simData);
	}

	/**
	 * Löst ein Signal aus.
	 * @param signalName	Name des Signal
	 */
	public void signal(final String signalName) {
		if (simData==null || signalName==null || signalName.trim().isEmpty()) return;
		if (simData.loggingActive) simData.logEventExecution(Language.tr("Simulation.Log.Signal"),-1,String.format(Language.tr("Simulation.Log.Signal.Info2"),signalName));
		simData.runData.fireSignal(simData,signalName);
	}
}
