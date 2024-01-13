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
package scripting.java;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.elements.DelayWithClientsList;
import simulator.elements.RunElementAnalogValue;
import simulator.elements.RunElementHoldJS;
import simulator.elements.RunElementProcess;
import simulator.elements.RunElementSetJS;
import simulator.elements.RunElementTank;
import simulator.events.TriggerScriptExecutionEvent;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Implementierungsklasse für das Interface {@link SystemInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class SystemImpl implements SystemInterface {
	/** Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen */
	public final SimulationData simData;
	/** Laufzeitmodell */
	private final RunModel runModel;
	/** ID der aktuellen Station */
	private final int currentStation;
	/** Liste von Kundenlisten für bestimmte Verzögerung-Stationen */
	private final Map<Integer,ClientsDelayImpl> delayInterfaces;
	/** Liste von Kundenlisten für bestimmte Bedienstationen */
	private final Map<Integer,ClientsProcessQueueImpl> processInterfaces;
	/** Stationslokale Daten */
	private final RuntimeData mapLocal;
	/** Modellweite Daten */
	private final Map<String,Object> mapGlobal;

	/**
	 * Zuordnung von Rechenausdruck-Zeichenketten und bereits erstellten passenden Objekten
	 * @see #getExpression(String)
	 */
	private Map<String,ExpressionCalc> expressionCache;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 * @param currentStation	ID der aktuellen Station
	 */
	public SystemImpl(final SimulationData simData, final int currentStation) {
		this.simData=simData;
		runModel=(simData==null)?null:simData.runModel;
		this.currentStation=currentStation;
		delayInterfaces=new HashMap<>();
		processInterfaces=new HashMap<>();
		mapLocal=new RuntimeData();
		mapGlobal=(simData==null)?null:(simData.runData.getMapGlobal());
	}

	/**
	 * Versucht eine Zeichenkette in ein Rechenobjekt umzuwandeln.
	 * @param text	Zeichenkette, die die Formel enthält
	 * @return	Liefert im Erfolgsfall ein Rechenobjekt, sonst eine Fehlermeldung
	 */
	private Object getExpression(final String text) {
		if (expressionCache==null) expressionCache=new HashMap<>();
		ExpressionCalc expression=expressionCache.get(text);
		if (expression!=null) return expression;

		expression=new ExpressionCalc(runModel.variableNames);
		final int errorPos=expression.parse(text);
		if (errorPos>=0) return String.format(Language.tr("Statistics.Filter.CoundNotProcessExpression.Info"),errorPos+1);
		expressionCache.put(text,expression);
		return expression;
	}

	@Override
	public Object calc(final String expression) {
		final Object result=getExpression(expression);
		if (result instanceof String) return result;
		final ExpressionCalc calc=(ExpressionCalc)result;
		try {
			return NumberTools.fastBoxedValue(calc.calc(simData.runData.variableValues,simData,null));
		} catch (MathCalcError e) {
			return Language.tr("Statistics.Filter.CoundNotProcessExpression.Title");
		}
	}

	@Override
	public double getTime() {
		return simData.currentTime*simData.runModel.scaleToSeconds;
	}

	@Override
	public boolean isWarmUp() {
		return simData.runData.isWarmUp;
	}

	/**
	 * Dynamisch aufgebaute Zuordnung von Stationsnamen zu Laufzeitelementen
	 * @see #elementFromName(String)
	 */
	private Map<String,RunElement> namesToElements;

	/**
	 * Liefert das Stations-Laufzeit-Element zu einem Namen
	 * @param stationName	Name der Station (identisch zu den Namen, die in $("...") Befehlen in Rechenausdrücken verwendet werden können)
	 * @return	Stations-Laufzeit-Element oder <code>null</code>, wenn es keine Station mit dem angegebenen Namen gibt
	 */
	private RunElement elementFromName(final String stationName) {
		if (stationName==null) return null;

		if (namesToElements!=null) {
			final RunElement element=namesToElements.get(stationName);
			if (element!=null) return element;
		}

		if (simData==null) return null;
		final Integer I=simData.runModel.namesToIDs.get(stationName);
		if (I==null) return null;
		final RunElement element=simData.runModel.elementsFast[I];

		if (namesToElements==null) namesToElements=new HashMap<>();
		namesToElements.put(stationName,element);
		return element;
	}

	@Override
	public int getWIP(final int id) {
		if (id<0 || id>=runModel.elementsFast.length) return 0;
		final RunElement element=runModel.elementsFast[id];
		if (element==null) return 0;
		return element.getData(simData).reportedClientsAtStation(simData);
	}

	@Override
	public int getWIP(final String stationName) {
		final RunElement element=elementFromName(stationName);
		if (element==null) return 0;
		return element.getData(simData).reportedClientsAtStation(simData);
	}

	@Override
	public int getNQ(final int id) {
		if (id<0 || id>=runModel.elementsFast.length) return 0;
		final RunElement element=runModel.elementsFast[id];
		if (element==null) return 0;
		return element.getData(simData).clientsAtStationQueue;
	}

	@Override
	public int getNQ(final String stationName) {
		final RunElement element=elementFromName(stationName);
		if (element==null) return 0;
		return element.getData(simData).clientsAtStationQueue;
	}

	@Override
	public int getNS(final int id) {
		if (id<0 || id>=runModel.elementsFast.length) return 0;
		final RunElement element=runModel.elementsFast[id];
		if (element==null) return 0;
		return element.getData(simData).clientsAtStationProcess;
	}

	@Override
	public int getNS(final String stationName) {
		final RunElement element=elementFromName(stationName);
		if (element==null) return 0;
		return element.getData(simData).clientsAtStationProcess;
	}

	@Override
	public int getWIP() {
		return simData.statistics.clientsInSystem.getCurrentState();
	}

	@Override
	public int getNQ() {
		return simData.statistics.clientsInSystemQueues.getCurrentState();
	}

	@Override
	public int getNS() {
		return simData.statistics.clientsInSystemProcess.getCurrentState();
	}

	@Override
	public void set(final String varName, final Object varValue) {
		if (varName==null || varValue==null || !(varName instanceof String)) return;
		if (!(varValue instanceof String) && !(varValue instanceof Double) && !(varValue instanceof Integer) && !(varValue instanceof Long)) return;

		int index=-1;
		for (int i=0;i<runModel.variableNames.length;i++) if (runModel.variableNames[i].equalsIgnoreCase(varName)) {index=i; break;}
		if (index<0) return;

		if (varValue instanceof Integer) {
			simData.runData.variableValues[index]=(Integer)varValue;
			simData.runData.updateVariableValueForStatistics(simData,index);
			return;
		}
		if (varValue instanceof Long) {
			simData.runData.variableValues[index]=(Long)varValue;
			simData.runData.updateVariableValueForStatistics(simData,index);
			return;
		}
		if (varValue instanceof Double) {
			if (Double.isNaN((Double)varValue)) return;
			simData.runData.variableValues[index]=(Double)varValue;
			simData.runData.updateVariableValueForStatistics(simData,index);
			return;
		}
		if (varValue instanceof String) {
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			if (calc.parse((String)varValue)>=0) return;
			try {
				final double d=calc.calc(simData.runData.variableValues,simData,null);
				simData.runData.variableValues[index]=d;
				simData.runData.updateVariableValueForStatistics(simData,index);
			} catch (MathCalcError e) {}
			return;
		}
	}

	/**
	 * Liefert basierend auf einer ID die zugehörige Station.
	 * @param id	ID für die die Station gesucht werden soll
	 * @return	Stationsobjekt oder <code>null</code>, wenn keine zu der ID passende Station gefunden wurde
	 */
	private RunElement getRunElement(final int id) {
		if (id<0 || id>=runModel.elementsFast.length) return null;
		return runModel.elementsFast[id];
	}

	/**
	 * Liefert basierend auf einer ID die zugehörige Station.
	 * @param id	ID für die die Station gesucht werden soll
	 * @return	Stationsobjekt oder <code>null</code>, wenn keine zu der ID passende Station gefunden wurde
	 */
	private RunElement getRunElement(final Object id) {
		if (id==null) return null;
		if (id instanceof Integer) return getRunElement(((Integer)id).intValue());
		if (id instanceof Long) return getRunElement(((Long)id).intValue());
		if (id instanceof Double) return getRunElement((int)FastMath.round((Double)id));
		if (id instanceof String) {
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
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

	/**
	 * Berechnet einen Ausdruck im Kontext der Simulation.
	 * @param value	Zu berechnender Ausdruck
	 * @return	Ergebnis der Berechnung oder <code>null</code>, wenn der Ausdruck nicht berechnet werden konnte
	 */
	private Double evaluateValue(final Object value) {
		if (value instanceof Double) return (Double)value;
		if (value instanceof Integer) return ((Integer)value).doubleValue();
		if (value instanceof Long) return ((Long)value).doubleValue();
		if (value instanceof String) {
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			if (calc.parse((String)value)>=0) return null;
			try {
				return calc.calc(simData.runData.variableValues,simData,null);
			} catch (MathCalcError e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public void setAnalogValue(final Object elementID, final Object value) {
		if (elementID==null || value==null) return;

		final Double D=evaluateValue(value);
		if (D==null) return;
		final double val=D.doubleValue();

		final RunElement element=getRunElement(elementID);
		if (element instanceof RunElementAnalogValue) ((RunElementAnalogValue)element).getData(simData).setValue(simData,val);
		if (element instanceof RunElementTank) ((RunElementTank)element).getData(simData).setValue(simData,val);
	}


	@Override
	public void setAnalogRate(final Object elementID, final Object value) {
		if (elementID==null || value==null) return;

		final Double D=evaluateValue(value);
		if (D==null) return;
		final double val=D.doubleValue();

		final RunElement element=getRunElement(elementID);
		if (element instanceof RunElementAnalogValue) ((RunElementAnalogValue)element).getData(simData).setRate(simData,val);
	}

	@Override
	public void setAnalogValveMaxFlow(final Object elementID, final Object valveNr, final Object value) {
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

	@Override
	public int getAllResourceCount() {
		return simData.runData.resources.getAllCount(simData);
	}

	@Override
	public int getResourceCount(final int resourceId) {
		return simData.runData.resources.getCount(resourceId-1,simData);
	}

	@Override
	public double getAllResourceCountAverage() {
		return simData.runData.resources.getAllCountAverage(simData);
	}

	@Override
	public double getResourceCountAverage(final int resourceId) {
		return simData.runData.resources.getCountAverage(resourceId-1,simData);
	}

	@Override
	public boolean setResourceCount(final int resourceId, final int count) {
		return simData.runData.resources.setCount(resourceId-1,simData,count);
	}

	@Override
	public int getResourceDown(final int resourceId) {
		return simData.runData.resources.getDown(resourceId-1,simData);
	}

	@Override
	public int getAllResourceDown() {
		return simData.runData.resources.getAllDown(simData);
	}

	@Override
	public String getLastClientTypeName(final int id) {
		if (simData==null) return "";
		if (id<0 || id>=simData.runModel.elementsFast.length) return "";
		final RunElement element=simData.runModel.elementsFast[id];
		if (!(element instanceof RunElementProcess)) return "";

		final int type=((RunElementProcess)element).getData(simData).lastClientIndex;
		if (type<0) return "";
		return simData.runModel.clientTypes[type];
	}

	@Override
	public void signal(String signalName) {
		if (signalName==null || signalName.trim().isEmpty()) return;
		if (simData.loggingActive) simData.logEventExecution(Language.tr("Simulation.Log.Signal"),-1,String.format(Language.tr("Simulation.Log.Signal.Info2"),signalName));
		simData.runData.fireSignal(simData,signalName);
	}

	/**
	 * Registriert ein Ereignis zur späteren Ausführung des Skripts an einer Station.
	 * @param stationId	ID der Skript- oder der Skript-Bedingung-Station, an der die Verarbeitung ausgelöst werden soll
	 * @param time	Zeitpunkt der Skriptausführung
	 * @return	Liefert <code>true</code>, wenn ein entsprechendes Ereignis in die Ereignisliste aufgenommen werden konnte
	 */
	@Override
	public boolean triggerScriptExecution(final int stationId, final double time) {
		/* Voraussetzungen prüfen */
		if (simData==null) return false;
		final long timeMS=Math.round(time*simData.runModel.scaleToSimTime);
		if (timeMS<simData.currentTime) return false;
		if (stationId<0 || stationId>=simData.runModel.elementsFast.length) return false;
		final RunElement element=simData.runModel.elementsFast[stationId];
		if (!(element instanceof RunElementSetJS) && !(element instanceof RunElementHoldJS)) return false;

		/* Ereignis anlegen */
		final TriggerScriptExecutionEvent triggerEvent=(TriggerScriptExecutionEvent)simData.getEvent(TriggerScriptExecutionEvent.class);

		/* Ereignis konfigurieren */
		triggerEvent.init(timeMS);
		triggerEvent.station=element;

		/* Zur Ereignisliste hinzufügen */
		simData.eventManager.addEvent(triggerEvent);

		return true;
	}

	@Override
	public Object runPlugin(final String className, final String functionName, final Object data) {
		if (simData.pluginsConnect==null) return null;
		return simData.pluginsConnect.runFunction(className,functionName,this,data,simData.runModel.javaImports.isAllowLoadClasses(),error->{
			if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown("id="+currentStation+": "+error);
		});
	}

	@Override
	public void log(final Object obj) {
		if (obj==null) return;
		if (!simData.logArrival) return;
		if (currentStation<=0) {
			simData.logEventExecution("Log",currentStation,obj.toString());
		} else {
			simData.runModel.elementsFast[currentStation].log(simData,"Log",obj.toString());
		}
	}

	@Override
	public ClientsInterface getDelayStationData(final int id) {
		ClientsDelayImpl delayInterface=delayInterfaces.get(id);
		if (delayInterface==null) {
			final RunElement element=simData.runModel.elementsFast[id];
			if (!(element instanceof DelayWithClientsList)) return null;
			delayInterface=new ClientsDelayImpl(simData,(DelayWithClientsList)element);
			delayInterfaces.put(id,delayInterface);
		}

		delayInterface.updateClients();
		return delayInterface;
	}

	@Override
	public ClientsInterface getProcessStationQueueData(final int id) {
		ClientsProcessQueueImpl processInterface=processInterfaces.get(id);
		if (processInterface==null) {
			final RunElement element=simData.runModel.elementsFast[id];
			if (!(element instanceof RunElementProcess)) return null;
			processInterface=new ClientsProcessQueueImpl(simData,(RunElementProcess)element);
			processInterfaces.put(id,processInterface);
		}

		processInterface.updateClients();
		return processInterface;
	}


	@Override
	public Map<String,Object> getMapLocal() {
		return mapLocal.get();
	}

	@Override
	public Map<String,Object> getMapGlobal() {
		return mapGlobal;
	}

	@Override
	public void terminateSimulation(final String message) {
		if (message!=null) simData.doEmergencyShutDown(message); else simData.doShutDown();
	}

	@Override
	public void pauseAnimation() {
		if (simData.pauseAnimationCallback!=null) simData.pauseAnimationCallback.run();
	}
}