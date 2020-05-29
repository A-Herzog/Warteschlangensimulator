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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import simulator.events.ResourcesReCheckEvent;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResourceFailure;
import ui.modeleditor.ModelSchedule;
import ui.modeleditor.ModelSchedules;
import ui.modeleditor.ModelSurface;

/**
 * Enthält Informationen darüber, wie viele Bediener eines bestimmten Typs momentan verfügbar sind
 * @author Alexander Herzog
 */
public final class RunDataResource implements Cloneable {
	private boolean firstRequest;
	private String name;
	private String icon;
	private String[] variables;

	private AbstractRealDistribution moveDistribution;
	private String moveExpression;
	private ModelSurface.TimeBase moveTimeBase;

	private int available;
	private ModelSchedule availableSchedule;
	private int inUse;
	private int inDownTime;
	private long lastStateChange;

	private RunDataResourceFailure[] failuresGlobal;

	private RunDataResourceOperatorFull[] operators;
	private RunDataResourceOperator[] operatorsThin;

	private boolean needToFullCount;

	private StatisticsTimePerformanceIndicator statisticsCount;
	private StatisticsTimePerformanceIndicator statisticsUsage;
	private StatisticsTimePerformanceIndicator statisticsDownTime;
	private StatisticsValuePerformanceIndicator statisticsCostsActive;
	private StatisticsValuePerformanceIndicator statisticsCostsProcess;
	private StatisticsValuePerformanceIndicator statisticsCostsIdle;

	private double costsPerActiveHour;
	private double costsPerProcessHour;
	private double costsPerIdleHour;

	/**
	 * Konstruktor der Klasse <code>RunDataResource</code>
	 */
	public RunDataResource() {
		lastStateChange=0;
		inUse=0;
		inDownTime=0;
		firstRequest=true;
	}

	/**
	 * Lädt die Laufzeit-Ressourcen-Daten aus einem Editor-Ressourcen-Objekt
	 * @param resource	Editor-Ressourcen-Objekt, aus dem die Informationen, wie viele Bediener eines bestimmten Typs vorhanden sind, ausgelesen werden sollen
	 * @param schedules	Editor-Zeitpläne-Objekt, aus dem ebenfalls Informationen, wie viele Bediener welchen Typs wann vorhanden sind, ausgelesen werden sollen
	 * @param variables	Liste der verfügbaren Variablen
	 * @return Gibt <code>null</code> zurück, wenn die Ressourcendaten korrekt geladen werden konnten, sonst eine Fehlermeldung.
	 */
	public String loadFromResource(final ModelResource resource, final ModelSchedules schedules, final String[] variables) {
		lastStateChange=0;
		inUse=0;
		inDownTime=0;
		firstRequest=true;
		name=resource.getName();
		icon=resource.getIcon();
		this.variables=variables;

		/* Verfügbare Bediener */
		switch (resource.getMode()) {
		case MODE_NUMBER:
			available=resource.getCount();
			availableSchedule=null;
			break;
		case MODE_SCHEDULE:
			available=-2;
			final ModelSchedule schedule=schedules.getSchedule(resource.getSchedule());
			if (schedule==null) return String.format(Language.tr("Simulation.Creator.UnknownScheduleForResource"),name,resource.getSchedule());
			availableSchedule=schedule.clone();
			break;
		default:
			return String.format(Language.tr("Simulation.Creator.UnknownNumberForResource"),name);
		}

		/* Ausfälle */
		final List<RunDataResourceFailure> failures=new ArrayList<>();
		for (ModelResourceFailure editFailure : resource.getFailures()) {
			final RunDataResourceFailure runFailure=new RunDataResourceFailure(name);
			runFailure.failureMode=editFailure.getFailureMode();
			runFailure.failureNumber=editFailure.getFailureNumber();
			runFailure.failureTime=FastMath.round(editFailure.getFailureTime()*1000);
			runFailure.failureDistribution=editFailure.getFailureDistribution();
			if (runFailure.failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_DISTRIBUTION && runFailure.failureDistribution==null) return String.format(Language.tr("Simulation.Creator.MissingResourceInterDownTimeDistribution"),name);
			if (runFailure.failureMode==ModelResourceFailure.FailureMode.FAILURE_BY_EXPRESSION) {
				runFailure.failureExpression=new ExpressionCalc(variables);
				final int error=runFailure.failureExpression.parse(editFailure.getFailureExpression());
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidResourceInterDownTimeExpression"),name,editFailure.getFailureExpression(),error+1);
			}
			runFailure.downTimeExpressionString=editFailure.getDownTimeExpression();
			if (runFailure.downTimeExpressionString==null) {
				runFailure.downTimeDistribution=DistributionTools.cloneDistribution(editFailure.getDownTimeDistribution());
			} else {
				runFailure.downTimeExpression=new ExpressionCalc(variables);
				final int error=runFailure.downTimeExpression.parse(runFailure.downTimeExpressionString);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidResourceDownTimeExpression"),name,runFailure.downTimeExpressionString,error+1);
			}
			failures.add(runFailure);
		}
		this.failuresGlobal=failures.toArray(new RunDataResourceFailure[0]);

		/* Ausfälle und Schichtpläne können nicht gleichzeitig verwendet werden. */
		if (this.failuresGlobal.length>0 && available==-2) return String.format(Language.tr("Simulation.Creator.NoFailureAndSchedule"),name);

		/* Kosten */
		costsPerActiveHour=resource.getCostsPerActiveHour();
		costsPerProcessHour=resource.getCostsPerProcessHour();
		costsPerIdleHour=resource.getCostsPerIdleHour();

		/* Rüstzeiten beim Stationswechsel */
		final Object moveTimes=resource.getMoveTimes();
		if (moveTimes instanceof AbstractRealDistribution) {
			moveDistribution=(AbstractRealDistribution)moveTimes;
		}
		if (moveTimes instanceof String) {
			moveExpression=(String)moveTimes;
			final int error=ExpressionCalc.check(moveExpression,variables);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidResourceMoveTimeExpression"),name,moveExpression,error+1);
		}
		moveTimeBase=resource.getMoveTimeBase();

		return null;
	}

	/**
	 * Muss zum Zeitpunkt 0 aufgerufen werden, um evtl. Ausfallzeitpunkte einzuplanen
	 * und Bediener zu initialisieren, wenn Rüstzeiten auf Bedienerseite vorgesehen sind.
	 * @param simData	Simulationsdatenobjekt
	 * @see RunDataResources#prepareOperatorObjects(SimulationData)
	 */
	public void prepareOperatorObjects(final SimulationData simData) {
		if (failuresGlobal.length>0 || moveDistribution!=null || moveExpression!=null) {
			needToFullCount=true;
			initOperators(simData);
		}
	}

	private void initOperators(final SimulationData simData) {
		if (available>0) {
			operators=new RunDataResourceOperatorFull[available];
			for (int i=0;i<operators.length;i++) operators[i]=new RunDataResourceOperatorFull(i,name,icon,moveDistribution,moveExpression,moveTimeBase,simData,failuresGlobal);
			operatorsThin=operators;
			for (int i=0;i<FastMath.min(inUse,operators.length);i++) operators[i].startWorking(simData,simData.runModel.elementsFast.length+1);
		}
		if (available==-2) {
			operatorsThin=new RunDataResourceOperator[availableSchedule.getMaxValue()];
			for (int i=0;i<operatorsThin.length;i++) operatorsThin[i]=new RunDataResourceOperator(i,name,icon,moveDistribution,moveExpression,moveTimeBase);
		}
	}

	@Override
	public RunDataResource clone() {
		final RunDataResource clone=new RunDataResource();
		clone.name=name;
		clone.icon=icon;

		/* Verfügbare Bediener */
		clone.available=available;
		clone.availableSchedule=availableSchedule;

		/* Ausfälle */
		clone.addFailures(failuresGlobal);

		/* Kosten */
		clone.costsPerActiveHour=costsPerActiveHour;
		clone.costsPerProcessHour=costsPerProcessHour;
		clone.costsPerIdleHour=costsPerIdleHour;

		/* Rüstzeiten beim Stationswechsel */
		clone.moveDistribution=moveDistribution;
		clone.moveExpression=moveExpression;
		clone.moveTimeBase=moveTimeBase;

		return clone;
	}

	private void addFailures(RunDataResourceFailure[] failures) {
		failuresGlobal=new RunDataResourceFailure[failures.length];
		for (int i=0;i<failures.length;i++) failuresGlobal[i]=new RunDataResourceFailure(failures[i],name,variables);
	}

	private void initStatistics(final SimulationData simData) {
		if (statisticsUsage==null) {
			statisticsCount=(StatisticsTimePerformanceIndicator)simData.statistics.resourceCount.get(name);
			statisticsUsage=(StatisticsTimePerformanceIndicator)simData.statistics.resourceUtilization.get(name);
			statisticsDownTime=(StatisticsTimePerformanceIndicator)simData.statistics.resourceInDownTime.get(name);
			statisticsCostsActive=(StatisticsValuePerformanceIndicator)simData.statistics.resourceTimeCosts.get(name);
			statisticsCostsProcess=(StatisticsValuePerformanceIndicator)simData.statistics.resourceWorkCosts.get(name);
			statisticsCostsIdle=(StatisticsValuePerformanceIndicator)simData.statistics.resourceIdleCosts.get(name);
		}
	}

	/**
	 * Liefert den Namen der Ressource
	 * @return	Name der Ressource
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gibt an, wie viele Bediener in dieser Ressource maximal verfügbar sein können
	 * @return	Maximal verfügbare Anzahl an Bedienern (kann <code>Integer.MAX_VALUE</code> sein, wenn keine Restriktion vorliegt oder diese nicht direkt berechenbar ist)
	 */
	public int getMaxAvailable() {
		if (available<0) return Integer.MAX_VALUE;
		return available;
	}

	/**
	 * Liefert das Auslastungs-Statistik-Objekt für diese Ressource zurück
	 * @param simData	Simulationsdaten
	 * @return	Auslastungs-Statistik-Objekt
	 */
	public StatisticsTimePerformanceIndicator getStatisticsUsage(final SimulationData simData) {
		if (statisticsUsage==null) initStatistics(simData);
		return statisticsUsage;
	}

	private double calcAvailableHours(final long timeMS1, final long timeMS2) {
		if (available==-1) return 0; /* unendlich viele Bediener -> hier wird ein Overhead von 0 angenommen */
		if (available==-2) return availableSchedule.getAvailbaleHoursByTimeRange(timeMS1,timeMS2); /* Schichtplan */
		return available*(timeMS2-timeMS1)/1000.0/3600.0; /* Feste Anzahl an Bedienern */
	}

	private void timesToStatistics(final SimulationData simData) {
		/* Wenn noch nicht geschehen: Statistik-Sub-Objekte für schnelleren Zugriff direkt cachen */
		if (statisticsUsage==null) initStatistics(simData);

		final long timeMS=simData.currentTime;
		final double time=timeMS/1000.0;

		/* Anzahl an vorhandenen Bedienern */
		if (operators!=null) statisticsCount.set(time,operators.length); else statisticsCount.set(time,available);

		/* Kosten für Arbeitszeit erfassen */
		if (!simData.runData.isWarmUp && timeMS!=lastStateChange) {
			final int lastState=statisticsUsage.getCurrentState(); /* CurrentState muss ausgelesen werden, bevor unten per statisticsUsage.set der neue Zustand eingestellt wird. */
			if (costsPerProcessHour!=0.0 && lastState>0) {
				final double usedHours=lastState*(timeMS-lastStateChange)/1000.0/3600.0;
				statisticsCostsProcess.add(costsPerProcessHour*usedHours);
			}
			if (costsPerActiveHour!=0.0 || costsPerIdleHour!=0.0) {
				final double availableHours=calcAvailableHours(lastStateChange,timeMS);
				final double usedHours=lastState*(timeMS-lastStateChange)/1000.0/3600.0;

				statisticsCostsActive.add(costsPerActiveHour*FastMath.max(usedHours,availableHours));
				statisticsCostsIdle.add(costsPerIdleHour*FastMath.max(0,availableHours-usedHours));
			}
		}

		/* Auslastung in die Statistik eintragen */
		statisticsUsage.set(time,inUse);
		//System.out.println(timeMS+"\t"+inUse);

		lastStateChange=timeMS;
	}

	/**
	 * Prüft ob die angegebene Anzahl an Bedienern in der Ressource verfügbar sind.<br>
	 * Es erfolgt nur eine Prüfung, die Bediener werden noch nicht belegt.
	 * @param needed	Benötigte Anzahl
	 * @param simData	Simulationsdaten
	 * @return	Gibt <code>true</code> zurück, wenn die Bediener verfügbar sind.
	 * @see #lockDo(int, SimulationData, int)
	 */
	public boolean lockTest(final int needed, final SimulationData simData) {
		if (needed<=0) return true;

		if (firstRequest) {
			firstRequest=false;
			if (availableSchedule!=null) {
				final ResourcesReCheckEvent event=(ResourcesReCheckEvent)(simData.getEvent(ResourcesReCheckEvent.class));
				long duration=availableSchedule.getDurationPerSlot();
				duration*=1000;
				long time=duration;
				if (time<simData.currentTime) time+=duration*(simData.currentTime%duration);
				while (time<simData.currentTime) time+=duration;
				event.init(time);
				event.autoScheduleNext=duration;
				simData.eventManager.addEvent(event);
			}
		}

		if (available>=0) {
			if (operators!=null) {
				int waiting=0;
				for (RunDataResourceOperatorFull operator: operators) {
					if (operator.isWorking()) continue;
					if (operator.isAvailableOrWorking(this,simData)) waiting++;
				}
				if (waiting<needed) return false;
			}

			if (available<inUse+needed) return false; /* nicht genug frei */
			return true;
		}

		if  (available==-1) return true; /* wir haben unendlich viele von der Ressource */

		if  (available==-2) {
			final int availableNow=availableSchedule.getValueAtTime(simData.currentTime/1000);
			if (availableNow<inUse+needed) return false; /* nicht genug frei */
			return true;
		}

		return false;
	}

	/**
	 * Belegt die angegebene Anzahl an Bedienern. Es wird vorausgesetzt, dass vorab per <code>lockTest</code>
	 * geprüft wurde, dass die entsprechende Anzahl auch verfügbar ist.
	 * @param needed	Benötigte Anzahl
	 * @param simData	Simulationsdaten
	 * @param station	ID der Station, an der die Ressource zum Einsatz kommt (für die Animation)
	 * @see RunDataResource#lockTest(int, SimulationData)
	 * @return	Gibt die notwendige zusätzliche Rüstzeit zur Belegung der Ressource an
	 */
	public double lockDo(final int needed, final SimulationData simData, final int station) {
		if (needed<=0) return 0;

		double additionalTime=0;

		if (operators!=null) {
			int allocated=0;
			for (RunDataResourceOperatorFull operator: operators) {
				if (operator.isWorking()) continue;
				if (operator.isAvailableOrWorking(this,simData)) {
					additionalTime=FastMath.max(additionalTime,operator.startWorking(simData,station));
					allocated++;
				}
				if (allocated==needed) break;
			}
		} else {
			if (operatorsThin!=null) { /* Schichtplan und Animation */
				int count=0;
				for (RunDataResourceOperator operator: operatorsThin) if (!operator.isWorking()) {
					additionalTime=FastMath.max(additionalTime,operator.startWorking(simData,station));
					count++;
					if (count==needed) break;
				}

			}
		}

		/* Ressourcen belegen */
		inUse+=needed;

		/* Erfassung der Zeiten in der Statistik */
		timesToStatistics(simData);

		return additionalTime;
	}

	/**
	 * Gibt eine bestimmte Anzahl an Bedienern wieder frei.
	 * @param needed	Anzahl an wieder freizugebenden Bedienern
	 * @param simData	Simulationsdaten
	 */
	public void releaseDo(final int needed, final SimulationData simData) {
		if (needed<=0) return;

		if (operators!=null) {
			int released=0;
			while (released<needed) {
				long minValue=Long.MAX_VALUE;
				int minIndex=-1;
				for (int i=0;i<operators.length;i++) {
					if (!operators[i].isWorking()) continue;
					if (operators[i].getWorkingStartTime()<minValue) {minValue=operators[i].getWorkingStartTime(); minIndex=i;}
				}
				if (minIndex<0) break;
				operators[minIndex].endWorking(this,simData);
				released++;
			}
		} else {
			if (operatorsThin!=null) { /* Schichtplan und Animation */
				int count=0;
				for (RunDataResourceOperator operator: operatorsThin) if (operator.isWorking()) {
					operator.endWorking(this,simData);
					count++;
					if (count==needed) break;
				}
			}
		}

		final int stillUsed=inUse-needed;
		inUse=(stillUsed>=0)?stillUsed:0;

		/* Hat sich die Anzahl an Bedienern verringert und konnten noch nicht alle Datensätze freigegeben werden? */
		if (operators!=null && operators.length>available) {
			timesToStatistics(simData);
			tryReduceOperatorsCount(simData,available);
		}

		/* Erfassung der Zeiten in der Statistik */
		timesToStatistics(simData);
	}

	/**
	 * Gibt an, wie viele Bediener eines bestimmten Typs zu einem Zeitpunkt insgesamt im System vorhanden sind (arbeitend und im Leerlauf)
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bediener (im Falle von unendlich vielen oder einem ungültigen Index "0")
	 */
	public int getCount(final SimulationData simData) {
		int value=available;

		if (value>0 && (operators!=null && needToFullCount)) {
			value=0;
			for (RunDataResourceOperatorFull operator: operators) if (operator.isAvailableOrWorking(this,simData)) value++;
			return value;
		}

		if (value==-1) return 0; /* unendlich viele */
		if (value==-2) return availableSchedule.getValueAtTime(simData.currentTime/1000); /* Zeitplan */
		return value;
	}

	private void tryReduceOperatorsCount(final SimulationData simData, final int count) {
		if (operators.length<=count) return;

		final List<RunDataResourceOperatorFull> operatorsNew=new ArrayList<>();

		int working=0;
		for (RunDataResourceOperatorFull operator: operators) if (operator.isWorking()) working++;

		int addIdle=count-working;

		for (RunDataResourceOperatorFull operator: operators) {
			if (operator.isWorking()) {
				operatorsNew.add(operator);
			} else {
				if (addIdle>0) {
					operatorsNew.add(operator);
					addIdle--;
				}
			}
		}

		for (int i=0;i<operatorsNew.size();i++) operatorsNew.get(i).index=i;

		operators=operatorsNew.toArray(new RunDataResourceOperatorFull[0]);
		operatorsThin=operators;

		/* Animationssystem benachrichtigen, dass es eine neue Ressourcenliste gibt. */
		simData.runData.resources.fireResourceCountChangeListeners(simData);
	}

	/**
	 * Stellt die Anzahl an Bedienern in der Gruppe ein
	 * @param simData	Simulationsdaten
	 * @param count	Neue Anzahl an Bedienern in der Gruppe
	 * @return	Gibt <code>true</code> zurück, wenn die Anzahl der Bediener geändert werden konnte.
	 */
	public boolean setCount(final SimulationData simData, final int count) {
		/* Wenn Schichtpläne, unendlich viele Bediener oder Ausfälle aktiv sind, kann die Anzahl nicht verändert werden. */
		if (available<0) return false;
		if (failuresGlobal.length>0) return false;
		needToFullCount=true;

		/* Neue Anzahl gültig? */
		if (count<=0) return false;

		/* Neue Anzahl == alte Anzahl? */
		if (available==count) return true;

		/* Noch gar kein Operators-Objekt initialisiert (normalerweise nur bei der Animation notwendig) ? */
		if (operators==null) initOperators(simData);

		/* Erfassung der Zeiten in der Statistik - Schritt 1 (mit alter Anzahl)  */
		timesToStatistics(simData);

		if (count>available) {
			/* Weitere Bediener hinzufügen */
			final int oldRealCount=operators.length;
			operators=Arrays.copyOf(operators,FastMath.max(oldRealCount,count));
			for (int i=oldRealCount;i<count;i++) operators[i]=new RunDataResourceOperatorFull(i,name,icon,moveDistribution,moveExpression,moveTimeBase,simData,failuresGlobal);
			operatorsThin=operators;
			available=count;

			/* Animationssystem benachrichtigen, dass es eine neue Ressourcenliste gibt. */
			simData.runData.resources.fireResourceCountChangeListeners(simData);

			/* System benachrichtigen: Wir haben mehr Bediener */
			simData.runData.fireReleasedResourcesNotify(simData);
		} else {
			/* Freie Bediener entfernen */
			tryReduceOperatorsCount(simData,count);
			available=count;
		}

		/* Erfassung der Zeiten in der Statistik - Schritt 2 (mit neuer Anzahl)  */
		timesToStatistics(simData);

		return true;
	}

	/**
	 * Gibt an, wie viele Bediener eines bestimmten Typs zu einem Zeitpunkt in Ausfallzeit sind
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern
	 */
	public int getDown(final SimulationData simData) {
		int down=0;
		if (operators!=null) for (RunDataResourceOperatorFull operator: operators) if (!operator.isAvailableOrWorking(this,simData)) down++;
		return down;
	}

	/**
	 * Liefert die bisher angefallenen Kosten für eine bestimmte Ressource
	 * @return	Bisherige Kosten
	 */
	public double getCosts() {
		return statisticsCostsActive.getValue()+statisticsCostsProcess.getValue()+statisticsCostsIdle.getValue();
	}

	/**
	 * Prüft bei allen Bedienern, ob diese evtl. in Pause gehen müssen bzw. berechnet neue Zeitpunkte dafür.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void updateStatus(final SimulationData simData) {
		if (operators!=null) for (RunDataResourceOperatorFull operator: operators) operator.isAvailableOrWorking(this,simData);
	}

	/**
	 * Liefert eine Liste aller Bediener in der Ressource
	 * @param simData	Simulationsdatenobjekt
	 * @param initOperators	Soll die Liste der Bediener neu eingerichtet werden? (Zu Beginn der Simulation)
	 * @return	Liste aller Bediener in der Ressource
	 */
	public RunDataResourceOperator[] getOperators(final SimulationData simData, final boolean initOperators) {
		if (initOperators) initOperators(simData);
		return operatorsThin;
	}

	/**
	 * Startet die Pausezeit eines Bediener
	 * @param time	Zeitpunkt des Starts der Pause
	 */
	public void startDownTime(final long time) {
		inDownTime++;
		statisticsDownTime.set(time/1000.0,inDownTime);
	}

	/**
	 * Beendet die Pausezeit eines Bediener
	 * @param time	Zeitpunkt des Endes der Pause
	 */
	public void endDownTime(final long time) {
		inDownTime--;
		statisticsDownTime.set(time/1000.0,inDownTime);
	}
}