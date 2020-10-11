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
	private Map<String,ExpressionCalc> expressionCache;

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
}