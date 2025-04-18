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
package simulator.runmodel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.events.ResourcesReCheckEvent;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import ui.modeleditor.ModelResourceFailure;
import ui.modeleditor.ModelResourceFailure.FailureMode;

/**
 * Hält die Daten für ein Ressourcen-Ausfall-Ereignis vor
 * @author Alexander Herzog
 * @see RunDataResource
 */
public class RunDataResourceFailure {
	/** Name der Ressource auf die sich dieser Ausfall bezieht */
	private final String name;

	/**
	 * Wie soll dieser Ausfall initiiert werden?
	 */
	ModelResourceFailure.FailureMode failureMode;

	/**
	 * Zählung der Bedienungen bis Ausfall
	 * @see FailureMode#FAILURE_BY_NUMBER
	 */
	int failureNumber;

	/**
	 * Erfassung der Zeit bis Ausfall
	 * @see FailureMode#FAILURE_BY_AVAILABLE_TIME
	 * @see FailureMode#FAILURE_BY_WORKING_TIME
	 */
	long failureTime;

	/**
	 * Verteilungsfunktion der Ausfallabstände
	 * @see FailureMode#FAILURE_BY_DISTRIBUTION
	 */
	AbstractRealDistribution failureDistribution;

	/**
	 * Ausdruck zur Bestimmung der Ausfallabstände
	 * @see FailureMode#FAILURE_BY_EXPRESSION
	 */
	ExpressionCalc failureExpression;

	/**
	 * Verteilung der Ausfalldauern
	 */
	AbstractRealDistribution downTimeDistribution;

	/**
	 * Ausdruck zur Bestimmung der Ausfalldauern (als String)
	 */
	String downTimeExpressionString;

	/**
	 * Ausdruck zur Bestimmung der Ausfalldauern (als Rechenausdruck)
	 */
	ExpressionCalc downTimeExpression;

	/**
	 * Anzahl an Bedienungen seit letztem Ausfall
	 * @see #countServed(long)
	 */
	private int servedCount;

	/**
	 * Geleistete Bedienzeit (in MS) seit letztem Ausfall
	 * @see #countServed(long)
	 */
	private long servedTime;

	/**
	 * Nächster geplanter Pause-Zeitpunkt
	 * @see #scheduleDownTime(SimulationData, long, String)
	 */
	private long pauseStartTime;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name der Ressource auf die sich dieser Ausfall bezieht
	 */
	public RunDataResourceFailure(final String name) {
		this.name=name;
		servedCount=0;
		servedTime=0;
		pauseStartTime=-1;
	}

	/**
	 * Copy-Konstruktor
	 * @param failure	Zu kopierendes Objekt
	 * @param name	Name der Ressource auf die sich dieser Ausfall bezieht
	 * @param variables	Globale Variablen (für die Rechenausdrücke)
	 * @param userFunctions	Modellspezifische nutzerdefinierte Funktionen
	 */
	public RunDataResourceFailure(final RunDataResourceFailure failure, final String name, final String[] variables, final ExpressionCalcModelUserFunctions userFunctions) {
		this(name);
		failureMode=failure.failureMode;
		failureNumber=failure.failureNumber;
		failureTime=failure.failureTime;
		failureDistribution=DistributionTools.cloneDistribution(failure.failureDistribution);
		failureExpression=failure.failureExpression;
		downTimeDistribution=DistributionTools.cloneDistribution(failure.downTimeDistribution);
		if (failure.downTimeExpression!=null) {
			downTimeExpressionString=failure.downTimeExpressionString;
			downTimeExpression=new ExpressionCalc(variables,userFunctions);
			downTimeExpression.parse(downTimeExpressionString);
		}
	}

	/**
	 * Plant ein Ereignis zur Prüfung, ob ein Bediener jetzt in die Pause geschickt werden kann, ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param eventTime	Zeitpunkt zu dem das Ereignis ausgeführt werden soll
	 */
	private void scheduleResourceCheckEvent(final SimulationData simData, final long eventTime) {
		if (simData.runData.stopp) return;
		final ResourcesReCheckEvent event=(ResourcesReCheckEvent)(simData.getEvent(ResourcesReCheckEvent.class));
		event.init(eventTime);
		event.autoScheduleNext=0;
		simData.eventManager.addEvent(event);
	}

	/**
	 * Plant den nächsten Ausfall der Ressource ein
	 * @param simData	Simulationsdatenobjekt
	 * @param availableStartTime	Startzeitpunkt ab dem die Ressource verfügbar wird
	 * @param resourceName	Name der Ressourcengruppe
	 */
	public void scheduleDownTime(final SimulationData simData, final long availableStartTime, final String resourceName) {
		if (failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_DISTRIBUTION) {
			double d=simData.runData.random.randomNonNegative(failureDistribution);
			pauseStartTime=availableStartTime+FastMath.round(d*simData.runModel.scaleToSimTime);
			if (pauseStartTime<=simData.currentTime) pauseStartTime=simData.currentTime+1;
			scheduleResourceCheckEvent(simData,pauseStartTime);
		}

		if (failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_EXPRESSION) {
			try {
				final double d=failureExpression.calc(simData.runData.variableValues,simData,null);
				pauseStartTime=availableStartTime+FastMath.round(d*simData.runModel.scaleToSimTime);
				if (pauseStartTime<=simData.currentTime) pauseStartTime=simData.currentTime+1;
				scheduleResourceCheckEvent(simData,pauseStartTime);
			} catch (MathCalcError e) {
				simData.calculationErrorRessource(failureExpression,resourceName);
			}
		}

		if (failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_AVAILABLE_TIME) {
			pauseStartTime=availableStartTime+failureTime;
			if (pauseStartTime<=simData.currentTime) pauseStartTime=simData.currentTime+1;
			scheduleResourceCheckEvent(simData,pauseStartTime);
		}
	}

	/**
	 * Bestimmt die Länge einer Ausfallzeit
	 * @param simData	Simulationsdatenobjekt
	 * @param resourceName	Name der Ressource
	 * @return	Länge der Ausfallzeit
	 */
	private long getDownTime(final SimulationData simData, final String resourceName) {
		if (downTimeExpression!=null) {
			try {
				final double d=downTimeExpression.calc(simData.runData.variableValues,simData,null);
				return FastMath.round(d*simData.runModel.scaleToSimTime);
			} catch (MathCalcError e) {
				simData.calculationErrorRessource(downTimeExpression,resourceName);
				return 0;
			}
		} else {
			return FastMath.round(simData.runData.random.randomNonNegative(downTimeDistribution)*simData.runModel.scaleToSimTime);
		}
	}

	/**
	 * Prüft, ob ein Bediener gemäß eines Pausenzeiten-Ausfalldatensatzes
	 * in eine Pause geschickt werden muss und bedingt diese ggf. auch gleich.
	 * @param simData	Simulationsdatenobjekt
	 * @param resource	Gruppe zu der dieser Bediener gehört
	 * @param operator	Bediener für den die Prüfung erfolgen soll
	 * @param failureIndex	0-basierter Index des Ausfallobjektes in der Liste aller Ausfalldatensätze
	 * @return	Liefert <code>true</code>, wenn der Bediener in eine Pausenzeit geschickt wurde
	 */
	public boolean testStartPause(final SimulationData simData, final RunDataResource resource, final RunDataResourceOperatorFull operator, final int failureIndex) {
		/* Geplante Pause starten */
		if (pauseStartTime>=0 && pauseStartTime<=simData.currentTime) {
			long downTime=getDownTime(simData,resource.getName());
			resource.startDownTime(pauseStartTime);
			operator.currentPauseIndex=failureIndex;
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.ResourceFailure"),-1,String.format(Language.tr("Simulation.Log.ResourceFailure.Scheduled"),name,operator.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			operator.onlineAgainAt=simData.currentTime+downTime;
			scheduleResourceCheckEvent(simData,operator.onlineAgainAt);
			return true;
		}

		/* Bedienungsanzahl-bedingte Pause starten */
		if (failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_NUMBER && servedCount>=failureNumber) {
			resource.startDownTime(simData.currentTime);
			servedCount=0;
			long downTime=getDownTime(simData,resource.getName());
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.ResourceFailure"),-1,String.format(Language.tr("Simulation.Log.ResourceFailure.WorkCount"),name,operator.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			operator.onlineAgainAt=simData.currentTime+downTime;
			scheduleResourceCheckEvent(simData,operator.onlineAgainAt);
			return true;
		}

		/* Anwesenheits-bedingte Pause beginnen */
		if (failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_AVAILABLE_TIME && simData.currentTime>=operator.availableStartTime+failureTime) {
			resource.startDownTime(simData.currentTime);
			long downTime=getDownTime(simData,resource.getName());
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.ResourceFailure"),-1,String.format(Language.tr("Simulation.Log.ResourceFailure.AvailableTime"),name,operator.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			operator.onlineAgainAt=simData.currentTime+downTime;
			scheduleResourceCheckEvent(simData,operator.onlineAgainAt);
			return true;
		}

		/* Arbeitszeit-bedingte Pause beginnen */
		if (failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_WORKING_TIME && servedTime>failureTime) {
			resource.startDownTime(simData.currentTime);
			servedTime=0;
			long downTime=getDownTime(simData,resource.getName());
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.ResourceFailure"),-1,String.format(Language.tr("Simulation.Log.ResourceFailure.WorkTime"),name,operator.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			operator.onlineAgainAt=simData.currentTime+downTime;
			scheduleResourceCheckEvent(simData,operator.onlineAgainAt);
			return true;
		}

		return false;
	}

	/**
	 * Zählung von Anzahl an Bedienungen und gleisteter Bediendauer erhöhen
	 * @param time	Bediendauer in MS
	 */
	public void countServed(final long time) {
		servedCount++;
		servedTime+=time;
	}
}