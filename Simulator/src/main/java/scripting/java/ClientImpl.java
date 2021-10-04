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
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import parser.MathCalcError;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;

/**
 * Implementierungsklasse für das Interface {@link ClientInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class ClientImpl implements ClientInterface {
	/** Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen */
	private final SimulationData simData;
	/** Aktueller Kunde */
	private RunDataClient client;
	/**
	 * Zuordnung von Rechenausdruck-Zeichenketten und bereits erstellten passenden Objekten
	 * @see #getExpression(String)
	 */
	private Map<String,ExpressionCalc> expressionCache;

	/** Umrechnungsfaktor von Millisekunden auf Sekunden (um während der Simulation Divisionen zu vermeiden) */
	private static final double toSec=1.0/1000.0;

	/**
	 * Konstruktor der Klasse
	 * @param simData	Simulationsdatenobjekt, dessen Daten bereitgestellt werden sollen
	 */
	public ClientImpl(final SimulationData simData) {
		this.simData=simData;
	}

	/**
	 * Stellt den Kunden ein, um den es hier geht.
	 * @param client	Aktueller Kunde
	 */
	public void setClient(final RunDataClient client) {
		this.client=client;
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

		expression=new ExpressionCalc(simData.runModel.variableNames);
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
			return NumberTools.fastBoxedValue(calc.calc(simData.runData.variableValues,simData,client));
		} catch (MathCalcError e) {
			return Language.tr("Statistics.Filter.CoundNotProcessExpression.Title");
		}
	}

	@Override
	public boolean isWarmUp() {
		return client.isWarmUp;
	}

	@Override
	public boolean isInStatistics() {
		return client.inStatistics;
	}

	@Override
	public void setInStatistics(final boolean inStatistics) {
		client.inStatistics=inStatistics;
	}

	@Override
	public long getNumber() {
		return client.clientNumber;
	}

	@Override
	public String getTypeName() {
		return simData.runModel.clientTypes[client.type];
	}

	@Override
	public double getWaitingSeconds() {
		return client.waitingTime*toSec;
	}

	@Override
	public String getWaitingTime() {
		return TimeTools.formatExactTime(client.waitingTime*toSec);
	}

	@Override
	public void setWaitingSeconds(double seconds) {
		client.waitingTime=FastMath.max(0,FastMath.round(seconds*1000));
	}

	@Override
	public double getTransferSeconds() {
		return client.transferTime*toSec;
	}

	@Override
	public String getTransferTime() {
		return TimeTools.formatExactTime(client.transferTime*toSec);
	}

	@Override
	public void setTransferSeconds(double seconds) {
		client.transferTime=FastMath.max(0,FastMath.round(seconds*1000));
	}

	@Override
	public double getProcessSeconds() {
		return client.processTime/1000.0;
	}

	@Override
	public String getProcessTime() {
		return TimeTools.formatExactTime(client.processTime*toSec);
	}

	@Override
	public void setProcessSeconds(double seconds) {
		client.processTime=FastMath.max(0,FastMath.round(seconds*1000));
	}

	@Override
	public double getResidenceSeconds() {
		return client.residenceTime*toSec;
	}

	@Override
	public String getResidenceTime() {
		return TimeTools.formatExactTime(client.residenceTime*toSec);
	}

	@Override
	public void setResidenceSeconds(double seconds) {
		client.residenceTime=FastMath.max(0,FastMath.round(seconds*1000));
	}

	@Override
	public double getValue(final int index) {
		return client.getUserData(index);
	}

	@Override
	public void setValue(final int index, final int value) {
		client.setUserData(index,value);
	}

	@Override
	public void setValue(final int index, final double value) {
		client.setUserData(index,value);
	}

	@Override
	public void setValue(final int index, final String value) {
		final Object result=calc(value);
		if (result instanceof Double) client.setUserData(index,((Double)result).doubleValue());
	}

	@Override
	public String getText(final String key) {
		return client.getUserDataString(key);
	}

	@Override
	public void setText(final String key, final String value) {
		if (key==null || key.trim().isEmpty()) return;
		client.setUserDataString(key,(value==null)?"":value);
	}

	@Override
	public double[] getAllValues() {
		final int maxIndex=client.getMaxUserDataIndex();
		final double[] result=new double[maxIndex+1];
		for (int i=0;i<=maxIndex;i++) result[i]=client.getUserData(i);
		return result;
	}

	@Override
	public Map<String,String> getAllTexts() {
		final Map<String,String> result=new HashMap<>();
		for (String key: client.getUserDataStringKeys()) result.put(key,client.getUserDataString(key));
		return result;
	}

	@Override
	public int batchSize() {
		final List<RunDataClient> batch=client.getBatchData();
		if (batch==null) return 0;
		return batch.size();
	}

	@Override
	public String getBatchTypeName(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return null;
		return simData.runModel.clientTypes[batchClient.type];
	}

	@Override
	public double getBatchWaitingSeconds(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return 0.0;
		return batchClient.waitingTime*toSec;
	}

	@Override
	public String getBatchWaitingTime(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return null;
		return TimeTools.formatExactTime(batchClient.waitingTime*toSec);
	}

	@Override
	public double getBatchTransferSeconds(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return 0.0;
		return batchClient.transferTime*toSec;
	}

	@Override
	public String getBatchTransferTime(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return null;
		return TimeTools.formatExactTime(batchClient.transferTime*toSec);
	}

	@Override
	public double getBatchProcessSeconds(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return 0.0;
		return batchClient.processTime/1000.0;
	}

	@Override
	public String getBatchProcessTime(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return null;
		return TimeTools.formatExactTime(batchClient.processTime*toSec);
	}

	@Override
	public double getBatchResidenceSeconds(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return 0.0;
		return batchClient.residenceTime*toSec;
	}

	@Override
	public String getBatchResidenceTime(final int batchIndex) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return null;
		return TimeTools.formatExactTime(batchClient.residenceTime*toSec);
	}

	@Override
	public double getBatchValue(final int batchIndex, final int index) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return 0.0;
		return batchClient.getUserData(index);
	}

	@Override
	public String getBatchText(final int batchIndex, final String key) {
		final RunDataClient batchClient=client.getBatchData(batchIndex);
		if (batchClient==null) return null;
		return batchClient.getUserDataString(key);
	}
}