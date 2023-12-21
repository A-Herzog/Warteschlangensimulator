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
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.events.TransporterPauseEndEvent;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelTransporterFailure;

/**
 * Hält die Daten für ein Transporter-Ausfall-Ereignis vor
 * @author Alexander Herzog
 * @see RunDataTransporter
 */
public class RunDataTransporterFailure {
	/** Liste mit allen Transportern im Modell */
	private final RunDataTransporters list;
	/** Durch was soll der Ausfall ausgelöst werden */
	ModelTransporterFailure.FailureMode failureMode;
	/** Anzahl an Bedienungen nach denen die Pause aktiv wird */
	int failureNumber;
	/** Gearbeitete Zeit (in Millisekunden) nach der die Pause aktiv wird */
	long failureTime;
	/** Gefahrene Strecke nach der die Pause aktiv wird */
	double failureDistance;
	/** Abstand der Ausfälle gemäß Verteilung */
	AbstractRealDistribution failureDistribution;
	/** Abstand der Ausfälle gemäß Rechenausdruck */
	ExpressionCalc failureExpression;
	/** Verteilung zur Bestimmung der Länge der Pausenzeit */
	AbstractRealDistribution downTimeDistribution;
	/** Ausdruck (als String) zur Bestimmung der Länge der Pausenzeit */
	String downTimeExpressionString;
	/** Ausdruck (als Rechenobjekt) zur Bestimmung der Länge der Pausenzeit */
	ExpressionCalc downTimeExpression;

	/** Anzahl der Bedienungen */
	private int servedCount;
	/** Geleistete Bedienzeit */
	private long servedTime;
	/** Gefahrene Strecke */
	private double servedDistance;
	/** Startzeitpunkt (in Millisekunden) der aktuellen Pausezeit */
	public long pauseStartTime;

	/**
	 * Konstruktor der Klasse
	 * @param list	Liste mit allen Transportern im Modell
	 */
	public RunDataTransporterFailure(final RunDataTransporters list) {
		this.list=list;
		servedCount=0;
		servedTime=0;
		servedDistance=0;
		pauseStartTime=-1;
	}

	/**
	 * Konstruktor der Klasse
	 * @param failure	Ausfall-Objekt dessen Daten in dieses Objekt kopiert werden sollen
	 * @param list	Liste mit allen Transportern im Modell
	 * @param variables	Liste mit allen im System vorhandenen Variablennamen
	 */
	public RunDataTransporterFailure(final RunDataTransporterFailure failure, final RunDataTransporters list, final String[] variables) {
		this(list);
		failureMode=failure.failureMode;
		failureNumber=failure.failureNumber;
		failureTime=failure.failureTime;
		failureDistance=failure.failureDistance;
		failureDistribution=DistributionTools.cloneDistribution(failure.failureDistribution);
		failureExpression=failure.failureExpression;
		downTimeDistribution=DistributionTools.cloneDistribution(failure.downTimeDistribution);
		if (failure.downTimeExpression!=null) {
			downTimeExpressionString=failure.downTimeExpressionString;
			downTimeExpression=new ExpressionCalc(variables);
			downTimeExpression.parse(downTimeExpressionString);
		}
	}

	/**
	 * Plant das Ereignis zum Ende der aktuellen Pausenzeit für einen Transporter ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param transporter	Transporter
	 */
	private void scheduleTransporterPauseEndEvent(final SimulationData simData, final RunDataTransporter transporter) {
		if (simData.runData.stopp) return;
		final TransporterPauseEndEvent event=(TransporterPauseEndEvent)(simData.getEvent(TransporterPauseEndEvent.class));
		event.transporter=transporter;
		event.failure=this;
		event.init(transporter.onlineAgainAt);
		simData.eventManager.addEvent(event);
	}

	/**
	 * Plant den nächsten Ausfall des Transporters ein
	 * @param simData	Simulationsdatenobjekt
	 * @param availableStartTime	Startzeitpunkt ab dem der Transporter verfügbar wird
	 * @param logTransporterName	Name des Transporter
	 */
	public void scheduleDownTime(final SimulationData simData, final long availableStartTime, final String logTransporterName) {
		if (failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_DISTRIBUTION) {
			double d=DistributionRandomNumber.randomNonNegative(failureDistribution);
			pauseStartTime=availableStartTime+FastMath.round(d*simData.runModel.scaleToSimTime);
			if (pauseStartTime<=simData.currentTime) pauseStartTime=simData.currentTime+1;
		}

		if (failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_EXPRESSION) {
			try {
				final double d=failureExpression.calc(simData.runData.variableValues,simData,null);
				pauseStartTime=availableStartTime+FastMath.round(d*simData.runModel.scaleToSimTime);
				if (pauseStartTime<=simData.currentTime) pauseStartTime=simData.currentTime+1;
			} catch (MathCalcError e) {
				simData.calculationErrorTransporter(failureExpression,logTransporterName);
			}
		}
	}

	/**
	 * Bestimmt die Länge einer Ausfallzeit eines Transporters
	 * @param simData	Simulationsdatenobjekt
	 * @param logTransporterName	Bezeichner des Transporters für das Logging
	 * @return	Pausenzeit in MS
	 */
	private long getDownTime(final SimulationData simData, final String logTransporterName) {
		if (downTimeExpression!=null) {
			try {
				final double d=downTimeExpression.calc(simData.runData.variableValues,simData,null);
				return FastMath.round(d*simData.runModel.scaleToSimTime);
			} catch (MathCalcError e) {
				simData.calculationErrorTransporter(downTimeExpression,logTransporterName);
				return 0;
			}
		} else {
			return FastMath.round(DistributionRandomNumber.randomNonNegative(downTimeDistribution)*simData.runModel.scaleToSimTime);
		}
	}

	/**
	 * Prüft, ob ein Transporter gemäß eines Pausenzeiten-Ausfalldatensatzes
	 * in eine Pause geschickt werden muss und bedingt diese ggf. auch gleich.
	 * @param simData	Simulationsdatenobjekt
	 * @param transporter	Zu prüfender Transporter
	 * @return	Liefert <code>true</code>, wenn der Transporter in eine Pausenzeit geschickt wurde
	 */
	public boolean testStartPause(final SimulationData simData, final RunDataTransporter transporter) {
		/* Geplante Pause starten */
		if (pauseStartTime>0 && pauseStartTime<=simData.currentTime) {
			transporter.startDownTime(simData);
			final long downTime=getDownTime(simData,simData.runModel.transportersTemplate.type[transporter.type]);
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.TransporterFailure"),-1,String.format(Language.tr("Simulation.Log.TransporterFailure.Scheduled"),list.type[transporter.type],transporter.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			transporter.onlineAgainAt=simData.currentTime+downTime;
			scheduleTransporterPauseEndEvent(simData,transporter);
			pauseStartTime=0;
			return true;
		}

		/* Bedienungsanzahl-bedingte Pause starten */
		if (failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_NUMBER && servedCount>=failureNumber) {
			transporter.startDownTime(simData);
			final long downTime=getDownTime(simData,simData.runModel.transportersTemplate.type[transporter.type]);
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.TransporterFailure"),-1,String.format(Language.tr("Simulation.Log.TransporterFailure.WorkCount"),list.type[transporter.type],transporter.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			transporter.onlineAgainAt=simData.currentTime+downTime;
			scheduleTransporterPauseEndEvent(simData,transporter);
			servedCount=0;
			return true;
		}

		/* Fahrtstrecken-bedingte Pause beginnen */
		if (failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_DISTANCE && servedDistance>=failureDistance) {
			transporter.startDownTime(simData);
			final long downTime=getDownTime(simData,simData.runModel.transportersTemplate.type[transporter.type]);
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.TransporterFailure"),-1,String.format(Language.tr("Simulation.Log.TransporterFailure.Distance"),list.type[transporter.type],transporter.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			transporter.onlineAgainAt=simData.currentTime+downTime;
			scheduleTransporterPauseEndEvent(simData,transporter);
			servedDistance=0;
			return true;
		}

		/* Arbeitszeit-bedingte Pause beginnen */
		if (failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_WORKING_TIME && servedTime>failureTime) {
			transporter.startDownTime(simData);
			long downTime=getDownTime(simData,simData.runModel.transportersTemplate.type[transporter.type]);
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.TransporterFailure"),-1,String.format(Language.tr("Simulation.Log.TransporterFailure.WorkTime"),list.type[transporter.type],transporter.index+1,simData.formatScaledSimTime(simData.currentTime),simData.formatScaledSimTime(downTime)));
			transporter.onlineAgainAt=simData.currentTime+downTime;
			scheduleTransporterPauseEndEvent(simData,transporter);
			servedTime=0;
			return true;
		}

		return false;
	}

	/**
	 * Erhöht die Zähler für geleistete Arbeit eines Transporters
	 * @param count	Anzahl der Bedienungen um diesen Wert erhöhen
	 * @param time	Geleistete Bedienzeit um diesen Millisekundenwert erhöhen
	 * @param distance	Gefahrene Strecke um diesen Wert erhöhen
	 */
	public void countServed(final int count, final long time, final double distance) {
		servedCount+=count;
		servedTime+=time;
		servedDistance+=distance;
	}
}