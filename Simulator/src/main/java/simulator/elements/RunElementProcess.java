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
package simulator.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.ProcessReleaseResources;
import simulator.events.ProcessWaitingClientsEvent;
import simulator.events.StationLeaveEvent;
import simulator.events.WaitingCancelEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsDataPerformanceIndicator;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementProcess</code>
 * @author Alexander Herzog
 * @see ModelElementProcess
 */
public class RunElementProcess extends RunElement implements FreeResourcesListener, PickUpQueue {
	/** ID der Station an die erfolgreiche Kunden weitergeleitet werden */
	private int connectionIdSuccess;
	/** ID der Station an die Warteabbrecher weitergeleitet werden */
	private int connectionIdCancel;
	/** Station an die erfolgreiche Kunden weitergeleitet werden (�bersetzung von {@link #connectionIdSuccess}) */
	private RunElement connectionSuccess;
	/** Station an die Warteabbrecher weitergeleitet werden (�bersetzung von {@link #connectionIdSuccess}) */
	private RunElement connectionCancel;

	/** Minimale Batch-Gr��e */
	public int batchMinSize;
	/** Maximale Batch-Gr��e */
	public int batchMaxSize;

	/** Kampagnen-Modus */
	private boolean campaignMode;

	/** Multiplikationsfaktor um bei den Verteilungs- und Ausdruckswerten f�r Bedienzeiten usw. auf Sekunden zu kommen */
	public double timeBaseMultiply;
	/** R�stzeit-Verteilungen */
	public AbstractRealDistribution[][] distributionSetup;
	/** Bedienzeit-Verteilungen */
	public AbstractRealDistribution[] distributionProcess;
	/** Nachbearbeitungszeit-Verteilungen */
	public AbstractRealDistribution[] distributionPostProcess;
	/** Wartezeittoleranz-Verteilungen */
	public AbstractRealDistribution[] distributionCancel;
	/** Kann ein Kunde das Warten auch noch w�hrend der R�stzeit aufgeben? */
	private boolean canCancelInSetupTime;
	/** R�stzeit-Rechenausdr�cke */
	public String[][] expressionSetup;
	/** Bedienzeit-Rechenausdr�cke */
	public String[] expressionProcess;
	/** Nachbearbeitungszeit-Rechenausdr�cke */
	public String[] expressionPostProcess;
	/** Wartezeittoleranz-Rechenausdr�cke */
	public String[] expressionCancel;
	/** Priorit�ts-Rechenausdr�cke */
	public String[] priority;

	/** Art der Z�hlung der Prozesszeiten */
	private ModelElementProcess.ProcessType processTimeType=ModelElementProcess.ProcessType.PROCESS_TYPE_PROCESS;

	/** Ressourcenpriorit�t */
	public String resourcePriority;
	/** Ressourcenbedarf pro Ressourcen-Alternative */
	public int[][] resources;
	/** Soll die Ressourcenverf�gbarkeit in der angegebenen Ressourcen-Alternativen-Reihenfolge (<code>false</code>) oder in zuf�lliger Reihenfolge (<code>true</code>) gepr�ft werden? */
	public boolean resourceCheckInRandomOrder;

	/**
	 * Kosten pro Bedienvorgang
	 * @see RunElementProcessData#costs
	 */
	private String costs;

	/**
	 * Kosten pro Bediensekunde
	 * @see RunElementProcessData#costsPerProcessSecond
	 */
	private String costsPerProcessSecond;

	/**
	 * Kosten pro Nachbearbeitungssekunde
	 * @see RunElementProcessData#costsPerPostProcessSecond
	 */
	private String costsPerPostProcessSecond;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementProcess(final ModelElementProcess element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Process.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementProcess)) return null;
		final ModelElementProcess processElement=(ModelElementProcess)element;

		RunElementProcess process=new RunElementProcess((ModelElementProcess)element);

		/* Auslaufende Kanten */
		process.connectionIdSuccess=findNextId(processElement.getEdgeOutSuccess());
		if (process.connectionIdSuccess<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId());
		if (processElement.getEdgeOutCancel()==null) {
			process.connectionIdCancel=-1;
		} else {
			process.connectionIdCancel=findNextId(processElement.getEdgeOutCancel());
		}

		/* Batch-Verarbeitung */
		if (processElement.getBatchMinimum()<1) return String.format(Language.tr("Simulation.Creator.ProcessInvalidBatchSize"),element.getId());
		if (processElement.getBatchMaximum()<processElement.getBatchMinimum()) return String.format(Language.tr("Simulation.Creator.ProcessBatchRange"),element.getId());
		process.batchMinSize=processElement.getBatchMinimum();
		process.batchMaxSize=processElement.getBatchMaximum();

		/* Kampagnen-Modus */
		if (processElement.isCampaignMode() && runModel.clientTypes.length>1) {
			if (process.batchMaxSize>1) return String.format(Language.tr("Simulation.Creator.ProcessCannotCombineBatchAndCampaign"),element.getId());
			process.campaignMode=true;
		}

		/* Zeitbasis */
		process.timeBaseMultiply=processElement.getTimeBase().multiply;

		/* Bedienzeit ist ... */
		process.processTimeType=processElement.getProcessTimeType();

		/* R�stzeiten */
		if (process.batchMaxSize>1 && processElement.getSetupTimes().isActive()) return String.format(Language.tr("Simulation.Creator.ProcessCannotMixBatchAndSetupTimes"),element.getId());
		process.distributionSetup=processElement.getSetupTimes().getAllDistributions(runModel.clientTypes);
		process.expressionSetup=processElement.getSetupTimes().getAllExpressions(runModel.clientTypes);
		for (int i=0;i<process.expressionSetup.length;i++) for (int j=0;j<process.expressionSetup.length;j++) {
			final String s=process.expressionSetup[i][j];
			if (s!=null) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
				final int error=calc.parse(s);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessExpressionSetup"),element.getId(),runModel.clientTypes[i],runModel.clientTypes[j],s,error+1);
			}
		}

		/* Bedienzeiten */
		process.distributionProcess=new AbstractRealDistribution[runModel.clientTypes.length];
		process.expressionProcess=new String[runModel.clientTypes.length];
		for (int i=0;i<process.distributionProcess.length;i++) {
			final Object data=processElement.getWorking().getOrDefault(runModel.clientTypes[i]);
			if (data instanceof String) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
				final int error=calc.parse((String)data);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessExpressionProcessing"),element.getId(),runModel.clientTypes[i],data,error+1);
				process.expressionProcess[i]=(String)data;
			} else {
				process.distributionProcess[i]=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
			}
		}

		/* Nachbearbeitungszeiten */
		process.distributionPostProcess=new AbstractRealDistribution[runModel.clientTypes.length];
		process.expressionPostProcess=new String[runModel.clientTypes.length];
		for (int i=0;i<process.distributionPostProcess.length;i++) {
			final Object data=processElement.getPostProcessing().getOrDefault(runModel.clientTypes[i]);
			if (data!=null) {
				if (data instanceof String) {
					final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
					final int error=calc.parse((String)data);
					if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessExpressionPostProcessing"),element.getId(),runModel.clientTypes[i],data,error+1);
					process.expressionPostProcess[i]=(String)data;
				} else {
					process.distributionPostProcess[i]=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
				}
			}
		}

		/* Wartezeittoleranzen */
		process.distributionCancel=new AbstractRealDistribution[runModel.clientTypes.length];
		process.expressionCancel=new String[runModel.clientTypes.length];
		boolean useCancel=false;
		for (int i=0;i<process.distributionCancel.length;i++) {
			final Object data=processElement.getCancel().getOrDefault(runModel.clientTypes[i]);
			if (data!=null) {
				useCancel=true;
				if (data instanceof String) {
					final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
					final int error=calc.parse((String)data);
					if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessExpressionCancel"),element.getId(),runModel.clientTypes[i],data,error+1);
					process.expressionCancel[i]=(String)data;
				} else {
					process.distributionCancel[i]=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
				}
			}
		}
		if (useCancel) {
			if (process.connectionIdCancel==-1) return String.format(Language.tr("Simulation.Creator.NoCancelationEdge"),element.getId());
		} else {
			if (process.connectionIdCancel!=-1) return String.format(Language.tr("Simulation.Creator.CancelationEdge"),element.getId());
		}

		/* Warteabbr�che in R�stzeit m�glich? */
		if (useCancel && processElement.getSetupTimes().isActive()) {
			process.canCancelInSetupTime=processElement.isCanCancelInSetupTime();
		} else {
			process.canCancelInSetupTime=false;
		}

		/* Priorit�ten */
		process.priority=new String[runModel.clientTypes.length];
		for (int i=0;i<process.priority.length;i++) {
			String priorityString=processElement.getPriority(runModel.clientTypes[i]);
			if (priorityString==null || priorityString.isBlank()) priorityString=ModelElementProcess.DEFAULT_CLIENT_PRIORITY;
			if (priorityString.equalsIgnoreCase(ModelElementProcess.DEFAULT_CLIENT_PRIORITY)) {
				process.priority[i]=null; /* Default Priorit�t als null vermerken */
			} else {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
				final int error=calc.parse(priorityString);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessClientPriority"),element.getId(),runModel.clientTypes[i],priorityString,error+1);

				process.priority[i]=priorityString;
			}
		}

		/* Ressourcen-Priorit�t */
		if (!processElement.getResourcePriority().equals("1")) {
			final int error=ExpressionCalc.check(processElement.getResourcePriority(),runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessResourcePriority"),element.getId(),processElement.getResourcePriority());
		}
		process.resourcePriority=processElement.getResourcePriority();

		/* Ressourcen */
		final int[][] res=runModel.resourcesTemplate.getNeededResourcesRecord(processElement.getNeededResources());
		if (res==null || res.length==0) return String.format(Language.tr("Simulation.Creator.ProcessResource"),element.getId());
		for (int[] r: res) if (r==null || r.length==0) return String.format(Language.tr("Simulation.Creator.ProcessResource"),element.getId());
		process.resources=res;

		/* Ressourcen-Alternativen-Pr�freihenfolge */
		process.resourceCheckInRandomOrder=(process.resources.length>1) && processElement.isResourceCheckInRandomOrder();

		/* Kosten */
		String text;

		text=processElement.getCosts();
		if (text==null || text.isBlank()  || text.trim().equals("0")) {
			process.costs=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcess"),text,element.getId(),error+1);
			process.costs=text;
		}

		text=processElement.getCostsPerProcessSecond();
		if (text==null || text.isBlank()  || text.trim().equals("0")) {
			process.costsPerProcessSecond=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcessPerProcessSecond"),text,element.getId(),error+1);
			process.costsPerProcessSecond=text;
		}

		text=processElement.getCostsPerPostProcessSecond();
		if (text==null || text.isBlank()  || text.trim().equals("0")) {
			process.costsPerPostProcessSecond=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcessPerPostProcessSecond"),text,element.getId(),error+1);
			process.costsPerPostProcessSecond=text;
		}

		return process;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementProcess)) return null;
		final ModelElementProcess processElement=(ModelElementProcess)element;

		/* Auslaufende Kanten */
		if (findNextId(processElement.getEdgeOutSuccess())<0) return RunModelCreatorStatus.noEdgeOut(element);

		/* Batch-Verarbeitung */
		if (processElement.getBatchMinimum()<1) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ProcessInvalidBatchSize"),element.getId()),RunModelCreatorStatus.Status.PROCESS_MIN_BATCH_LOWER_THAN_1);
		if (processElement.getBatchMaximum()<processElement.getBatchMinimum()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ProcessBatchRange"),element.getId()),RunModelCreatorStatus.Status.PROCESS_MAX_BATCH_LOWER_THAN_MIN);

		/* R�stzeiten */
		if (processElement.getBatchMaximum()>1 && processElement.getSetupTimes().isActive()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ProcessCannotMixBatchAndSetupTimes"),element.getId()),RunModelCreatorStatus.Status.PROCESS_MIX_BATCH_AND_SETUP);

		/* Wartezeittoleranzen */
		if (processElement.getCancel().get()!=null) {
			if (findNextId(processElement.getEdgeOutCancel())==-1) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoCancelationEdge"),element.getId()),RunModelCreatorStatus.Status.PROCESS_CANCELATION_TIME_BUT_NO_EDGE);
		} else {
			if (findNextId(processElement.getEdgeOutCancel())!=-1) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.CancelationEdge"),element.getId()));
		}

		/* Ressourcen */
		final List<Map<String,Integer>> res=processElement.getNeededResources();
		if (res==null || res.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ProcessResource"),element.getId()),RunModelCreatorStatus.Status.PROCESS_NO_RESOURCE);
		for (Map<String,Integer> r: res) if (r==null || r.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ProcessResource"),element.getId()),RunModelCreatorStatus.Status.PROCESS_NO_RESOURCE);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connectionSuccess=runModel.elements.get(connectionIdSuccess);
		if (connectionIdCancel>=0) connectionCancel=runModel.elements.get(connectionIdCancel);
	}

	@Override
	public RunElementProcessData getData(final SimulationData simData) {
		RunElementProcessData data;
		data=(RunElementProcessData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementProcessData(this,simData.runModel.variableNames,costs,costsPerProcessSecond,costsPerPostProcessSecond,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Berechnet den Score-Wert eines Kunden
	 * @param simData	Simulationsdatenobjekt
	 * @param processData	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 * @return	Score-Wert des Kunden
	 */
	private double getClientScore(final SimulationData simData, final RunElementProcessData processData, final RunDataClient client) {
		final ExpressionCalc calc=processData.priority[client.type];
		if (calc==null) { /* = Text war "w", siehe RunElementProcessData()  */
			return (((double)simData.currentTime)-client.lastWaitingStart)*simData.runModel.scaleToSeconds;
		} else {
			simData.runData.setClientVariableValues(simData.currentTime-client.lastWaitingStart,client.transferTime,client.processTime);
			try {
				return calc.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(calc,this);
				return 0;
			}
		}
	}

	/**
	 * Erfasst die Kosten f�r einen Bedienvorgang
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Aktueller Kunde
	 * @param timeProcess	Angefallene Bedienzeit
	 * @param timePostProcess	Angefallene Nachbearbeitungszeit
	 */
	private void logCosts(final SimulationData simData, final RunElementProcessData data, final RunDataClient client, final double timeProcess, final double timePostProcess) {
		boolean clientVariablesSet=false;

		/* Kosten pro Bedienung */
		double costsValue;
		if (data.costs==null) {
			costsValue=0.0;
		} else {
			simData.runData.setClientVariableValues(client);
			clientVariablesSet=true;

			try {
				costsValue=data.costs.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.costs,this);
				costsValue=0;
			}
		}

		/* Kosten pro Bediensekunde */
		double costsPerProcessSecondValue;
		if (timeProcess==0.0 || data.costsPerProcessSecond==null) {
			costsPerProcessSecondValue=0.0; /* Wir k�nnen uns die Berechnung sparen, wenn �berhaupt keine entsprechende Zeit angefallen ist. */
		} else {
			if (!clientVariablesSet) {
				simData.runData.setClientVariableValues(client);
				clientVariablesSet=true;
			}

			try {
				costsPerProcessSecondValue=data.costsPerProcessSecond.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.costsPerProcessSecond,this);
				costsPerProcessSecondValue=0;
			}
		}

		/* Kosten pro Nachbearbeitungssekunde */
		double costsPerPostProcessSecondValue;
		if (timePostProcess==0.0 || data.costsPerPostProcessSecond==null) {
			costsPerPostProcessSecondValue=0.0;  /* Wir k�nnen uns die Berechnung sparen, wenn �berhaupt keine entsprechende Zeit angefallen ist. */
		} else {
			if (!clientVariablesSet) {
				simData.runData.setClientVariableValues(client);
				clientVariablesSet=true;
			}

			try {
				costsPerPostProcessSecondValue=data.costsPerPostProcessSecond.calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.costsPerPostProcessSecond,this);
				costsPerPostProcessSecondValue=0;
			}
		}

		/* Kosten erfassen */
		final double costs=costsValue+timeProcess*costsPerProcessSecondValue+timePostProcess*costsPerPostProcessSecondValue;
		if (costs!=0.0) simData.runData.logStationCosts(simData,this,costs);
	}

	/**
	 * Startet die Bedienung eines einzelnen Kunden.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param resourceAlternative	Gew�hlte Ressourcen-Alternative (0-basierend)
	 * @param additionalPrepareTime	Notwendige R�stzeit (kann 0 sein)
	 * @see #processArrival(SimulationData, RunDataClient)
	 */
	private void startProcessingSingle(final SimulationData simData, final RunElementProcessData data, final int resourceAlternative, final double additionalPrepareTime) {
		RunDataClient selected=null;
		int bestIndex=0;

		if (data.allFirstComeFirstServe && !campaignMode) {
			/* Ganzes System ist FCFS */
			selected=data.waitingClients.get(0);
		} else {
			final int count=data.waitingClients.size();
			if (campaignMode && data.lastClientIndex>=0) {
				/* Zuerst versuchen: Kunden mit dem h�chsten Score und passendem Typ w�hlen */
				final int campaignClientIndex=data.lastClientIndex;
				double bestScore=-Double.MAX_VALUE;
				for (int i=0;i<count;i++) {
					final RunDataClient client=data.waitingClients.get(i);
					if (client.type!=campaignClientIndex) continue; /* Erstmal nur passende Kunden */
					if (data.allFirstComeFirstServe) { /* Wenn innerhalb der Kampagne FIFO gilt, dann ersten passenden Kunden w�hlen */
						bestIndex=i;
						selected=client;
						break;
					}
					final double score=getClientScore(simData,data,client);
					if (score>bestScore) {
						/* Ein Kunde weiter hinten in der Liste (=sp�tere Ankunft) braucht eine h�here Score, um den vorherigen zu �berbieten. D.h. bei Score-Gleichstand zwischen zwei Kunden gilt FIFO. */
						bestIndex=i;
						selected=client;
					}
				}
				if (selected==null && data.allFirstComeFirstServe) { /* Wenn es keinen passenden Kunden gibt, aber FIFO gilt, ersten Kunden in Warteschlange w�hlen. */
					selected=data.waitingClients.get(0);
				}
			}
			/* Kunden mit dem h�chsten Score w�hlen */
			if (selected==null) {
				selected=data.waitingClients.get(0);
				double bestScore=-Double.MAX_VALUE;
				if (count>1) for (int i=1;i<count;i++) {
					final RunDataClient client=data.waitingClients.get(i);
					final double score=getClientScore(simData,data,client);
					if (score>bestScore) {
						/* Ein Kunde weiter hinten in der Liste (=sp�tere Ankunft) braucht eine h�here Score, um den vorherigen zu �berbieten. D.h. bei Score-Gleichstand zwischen zwei Kunden gilt FIFO. */
						bestScore=score;
						bestIndex=i;
						selected=client;
					}
				}
			}
		}

		/* Gew�hlte Alternative in Kundenobjekt eintragen (damit diese Information bei der Bestimmung der Bedienzeit verwendet werden kann) */
		selected.lastAlternative=resourceAlternative+1;

		/* Bedienzeit bestimmen */
		double setupTime=data.getSetupTime(simData,selected);
		double processingTime=data.getProcessTime(simData,selected)+additionalPrepareTime;
		double postProcessingTime=data.getPostProcessTime(simData,selected);
		long setupTimeMS=FastMath.round(setupTime*simData.runModel.scaleToSimTime);
		long processingTimeMS=FastMath.round(processingTime*simData.runModel.scaleToSimTime);
		long postProcessingTimeMS=FastMath.round(postProcessingTime*simData.runModel.scaleToSimTime);

		/* Warteabbruch w�hrend der R�stzeit? */
		if (setupTimeMS>0 && canCancelInSetupTime) {
			final WaitingCancelEvent waitingCancelEvent=data.getWaitingCancelEvent(selected,bestIndex);
			if (waitingCancelEvent!=null && waitingCancelEvent.time<simData.currentTime+setupTimeMS) {
				/* Abbruch Ereignis manuell l�schen */
				simData.eventManager.deleteEvent(waitingCancelEvent,simData);

				/* Kunden austragen & Weiterleiten */
				processWaitingCancel(simData,selected,waitingCancelEvent.time-simData.currentTime);

				/* Ressourcen freigaben */
				final ProcessReleaseResources releaseEvent=(ProcessReleaseResources)simData.getEvent(ProcessReleaseResources.class);
				releaseEvent.init(waitingCancelEvent.time);
				releaseEvent.station=this;
				releaseEvent.resourceAlternative=resourceAlternative;
				simData.eventManager.addEvent(releaseEvent);

				return;
			}
		}

		/* Kunden aus der Warteschlange entfernen */
		final long waitingTimeMS=data.removeClientFromQueue(selected,bestIndex,simData.currentTime,true,simData);

		/* Logging */
		if (simData.loggingActive) {
			if (setupTimeMS>0) {
				log(simData,Language.tr("Simulation.Log.ProcessService"),String.format(Language.tr("Simulation.Log.ProcessService.InfoWithSetupTime"),selected.logInfo(simData),name,TimeTools.formatTime(Math.round(processingTimeMS*simData.runModel.scaleToSeconds)),TimeTools.formatTime(Math.round(setupTimeMS*simData.runModel.scaleToSeconds))));
			} else {
				log(simData,Language.tr("Simulation.Log.ProcessService"),String.format(Language.tr("Simulation.Log.ProcessService.Info"),selected.logInfo(simData),name,TimeTools.formatTime(Math.round(processingTimeMS*simData.runModel.scaleToSeconds))));
			}
		}

		/* Bedienzeit in Statistik */
		final long residenceTimeMS=waitingTimeMS+setupTimeMS+processingTimeMS;
		switch (processTimeType) {
		case PROCESS_TYPE_WAITING: simData.runData.logStationProcess(simData,this,selected,residenceTimeMS,0,0,residenceTimeMS); break;
		case PROCESS_TYPE_TRANSFER: simData.runData.logStationProcess(simData,this,selected,waitingTimeMS,setupTimeMS+processingTimeMS,0,residenceTimeMS); break;
		case PROCESS_TYPE_PROCESS: simData.runData.logStationProcess(simData,this,selected,waitingTimeMS,0,setupTimeMS+processingTimeMS,residenceTimeMS); break;
		case PROCESS_TYPE_NOTHING: /* nicht erfassen */ break;
		}
		switch (processTimeType) {
		case PROCESS_TYPE_WAITING: selected.addStationTime(id,waitingTimeMS+setupTimeMS+processingTimeMS,0,0,waitingTimeMS+setupTimeMS+processingTimeMS); break;
		case PROCESS_TYPE_TRANSFER: selected.addStationTime(id,waitingTimeMS,setupTimeMS+processingTimeMS,0,waitingTimeMS+setupTimeMS+processingTimeMS); break;
		case PROCESS_TYPE_PROCESS: selected.addStationTime(id,waitingTimeMS,0,setupTimeMS+processingTimeMS,waitingTimeMS+setupTimeMS+processingTimeMS); break;
		case PROCESS_TYPE_NOTHING: /* nicht erfassen */ break;
		}

		/* R�stzeiten in Statistik */
		if (!simData.runData.isWarmUp) {
			if (data.setupTimes==null) data.setupTimes=(StatisticsDataPerformanceIndicator)simData.statistics.stationsSetupTimes.get(name);
			data.setupTimes.add(setupTime);
		}

		/* Weiterleitung zu n�chster Station nach Bedienzeit-Ende */
		final StationLeaveEvent leaveEvent=StationLeaveEvent.addLeaveEvent(simData,selected,this,setupTimeMS+processingTimeMS);

		/* Kunden als "in Bedienung" erfassen */
		simData.runData.logClientEntersStationProcess(simData,this,data,selected);

		/* Kosten in Statistik erfassen */
		if (data.hasCosts) {
			logCosts(simData,data,selected,setupTime+processingTime,postProcessingTime);
		}

		/* Erfassung der Zwischenabgangszeiten auf Batch-Basis (bei dynamischen Bedien-Batches - hier also einzelnen Kunden) */
		simData.runData.logStationBatchLeave(simData.currentTime+setupTimeMS+processingTimeMS,simData,this,data);

		/* Belegte Ressourcen am Ende der Nachbearbeitungszeit wieder freigeben */
		final ProcessReleaseResources releaseEvent=(ProcessReleaseResources)simData.getEvent(ProcessReleaseResources.class);
		releaseEvent.init(simData.currentTime+setupTimeMS+processingTimeMS+postProcessingTimeMS);
		releaseEvent.station=this;
		releaseEvent.resourceAlternative=resourceAlternative;
		leaveEvent.addNextEvent=releaseEvent; /* Freigabeereignis gesichert erst nach dem Abgangsereignis ausf�hren */
	}

	/**
	 * Startet die Bedienung eines Batches.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param resourceAlternative	Gew�hlte Ressourcen-Alternative (0-basierend)
	 * @param additionalPrepareTime	Notwendige R�stzeit (kann 0 sein)
	 * @see #processArrival(SimulationData, RunDataClient)
	 */
	private void startProcessingBatch(final SimulationData simData, final RunElementProcessData data, final int resourceAlternative, final double additionalPrepareTime) {
		final int count=data.waitingClients.size();

		if (!data.allFirstComeFirstServe) {
			/* Scorewerte f�r alle wartenden Kunden berechnen */
			if (data.score==null || data.score.length<count) data.score=new double[count];

			for (int i=0;i<count;i++) {
				final RunDataClient client=data.waitingClients.get(i);
				final ExpressionCalc calc=data.priority[client.type];
				if (calc==null) { /* = Text war "w", siehe RunElementProcessData()  */
					final double waitingTime=(((double)simData.currentTime)-client.lastWaitingStart)*simData.runModel.scaleToSeconds;
					data.score[i]=waitingTime;
				} else {
					simData.runData.setClientVariableValues(simData.currentTime-client.lastWaitingStart,client.transferTime,client.processTime);
					try {
						data.score[i]=calc.calc(simData.runData.variableValues,simData,client);
					} catch (MathCalcError e) {
						simData.calculationErrorStation(calc,this);
						data.score[i]=0;
					}
				}
			}
		}

		List<RunDataClient> selectedForService;
		if (data.canUseGlobalSelectedForService) {
			if (data.globalSelectedForService==null) data.globalSelectedForService=new ArrayList<>(batchMaxSize);
			selectedForService=data.globalSelectedForService;
		} else {
			selectedForService=new ArrayList<>(batchMaxSize);
		}
		data.canUseGlobalSelectedForService=false;

		try {
			/* Die maximal batchMaxSize Kunden mit den h�chsten Scorewerten bestimmen */
			selectedForService.clear();
			while (selectedForService.size()<batchMaxSize && selectedForService.size()<data.waitingClients.size()) {
				if (data.allFirstComeFirstServe) {
					selectedForService.add(data.waitingClients.get(selectedForService.size()));
				} else {
					double maxScore=-Double.MAX_VALUE;
					RunDataClient maxScoreClient=data.waitingClients.get(0);
					for (int i=0;i<count;i++) {
						final RunDataClient client=data.waitingClients.get(i);
						if (selectedForService.indexOf(client)>=0) continue;
						if (data.score[i]>maxScore) {maxScore=data.score[i]; maxScoreClient=client;}
					}
					selectedForService.add(maxScoreClient);
				}
			}

			/* Bedienzeiten bestimmen */
			if (data.processingTimes==null) data.processingTimes=new double[simData.runModel.clientTypes.length]; else Arrays.fill(data.processingTimes,0.0);
			if (data.postProcessingTimes==null) data.postProcessingTimes=new double[simData.runModel.clientTypes.length]; else Arrays.fill(data.postProcessingTimes,0.0);
			for (int i=0;i<selectedForService.size();i++) {
				final RunDataClient client=selectedForService.get(i);  /* Iterator w�rde mehr Arbeitsspeicher brauchen */
				/* Gew�hlte Alternative in Kundenobjekt eintragen (damit diese Information bei der Bestimmung der Bedienzeit verwendet werden kann) */
				client.lastAlternative=resourceAlternative+1;
				/* Bedienzeit und Nachbearbeitungszeit f�r Kundentyp bestimmen */
				final int type=client.type;
				if (data.processingTimes[type]!=0.0) continue;
				/* hier keine R�stzeiten; wird schon im Builder ausgeschlossen */
				data.processingTimes[type]=data.getProcessTime(simData,client)+additionalPrepareTime;
				data.postProcessingTimes[type]=data.getPostProcessTime(simData,client);
			}

			/* Kunden aus der Warteschlange entfernen */
			for (int i=0;i<selectedForService.size();i++) {
				final RunDataClient client=selectedForService.get(i); /* Iterator w�rde mehr Arbeitsspeicher brauchen */
				final int type=client.type;

				/* Bedien- und Nachbearbeitungszeit aus Liste (s.o.) w�hlen */
				final long waitingTimeMS=data.removeClientFromQueue(client,-1,simData.currentTime,true,simData);
				final long processingTimeMS=FastMath.round(data.processingTimes[type]*simData.runModel.scaleToSimTime);

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ProcessService"),String.format(Language.tr("Simulation.Log.ProcessService.Info"),client.logInfo(simData),name,TimeTools.formatTime(Math.round(processingTimeMS*simData.runModel.scaleToSeconds))));

				/* Bedienzeit in Statistik */
				final long residenceTimeMS=waitingTimeMS+processingTimeMS;
				switch (processTimeType) {
				case PROCESS_TYPE_WAITING: simData.runData.logStationProcess(simData,this,client,waitingTimeMS+processingTimeMS,0,0,residenceTimeMS); break;
				case PROCESS_TYPE_TRANSFER: simData.runData.logStationProcess(simData,this,client,waitingTimeMS,processingTimeMS,0,residenceTimeMS); break;
				case PROCESS_TYPE_PROCESS: simData.runData.logStationProcess(simData,this,client,waitingTimeMS,0,processingTimeMS,residenceTimeMS); break;
				case PROCESS_TYPE_NOTHING: /* nicht erfassen */ break;
				}
				switch (processTimeType) {
				case PROCESS_TYPE_WAITING: client.addStationTime(id,waitingTimeMS+processingTimeMS,0,0,waitingTimeMS+processingTimeMS); break;
				case PROCESS_TYPE_TRANSFER: client.addStationTime(id,waitingTimeMS,processingTimeMS,0,waitingTimeMS+processingTimeMS); break;
				case PROCESS_TYPE_PROCESS: client.addStationTime(id,waitingTimeMS,0,processingTimeMS,waitingTimeMS+processingTimeMS); break;
				case PROCESS_TYPE_NOTHING: /* nicht erfassen */ break;
				}

				/* Kunden als "in Bedienung" erfassen */
				simData.runData.logClientEntersStationProcess(simData,this,data,client);

				/* Weiterleitung zu n�chster Station nach Bedienzeit-Ende */
				StationLeaveEvent.addLeaveEvent(simData,client,this,processingTimeMS);
			}
		} finally {
			if (selectedForService==data.globalSelectedForService) data.canUseGlobalSelectedForService=true;
		}

		/* Zeit bestimmen, die die Bediener blockiert sind */
		double resourcesBlockedTimeProcessing=0;
		double resourcesBlockedTimePostProcessing=0;
		for (double d: data.processingTimes) resourcesBlockedTimeProcessing=FastMath.max(resourcesBlockedTimeProcessing,d);
		for (double d: data.postProcessingTimes) resourcesBlockedTimePostProcessing=FastMath.max(resourcesBlockedTimePostProcessing,d);

		/* Kosten in Statistik erfassen */
		if (data.hasCosts) {
			logCosts(simData,data,null,resourcesBlockedTimeProcessing,resourcesBlockedTimePostProcessing);
		}

		/* Erfassung der Zwischenabgangszeiten auf Batch-Basis (bei dynamischen Bedien-Batches) */
		simData.runData.logStationBatchLeave(simData.currentTime+FastMath.round(resourcesBlockedTimeProcessing*simData.runModel.scaleToSimTime),simData,this,data);

		/* Belegte Ressourcen am Ende der Nachbearbeitungszeit wieder freigeben */
		ProcessReleaseResources event=(ProcessReleaseResources)simData.getEvent(ProcessReleaseResources.class);
		/* Da bei mehreren Kunden-Abgangs-Ereignissen nicht anders sichergestellt werden kann, dass die Ressourcenfreigabe erst sicher nach dem letzten Abgang erfolgt, wird hier das Zeit-Delta ben�tigt. */
		final int TIME_DELTA=(resourcesBlockedTimePostProcessing>0)?0:1; /* Zeit in ms die vor der Freigabe der Ressourcen noch verstreichen sollen, damit die Freigabe wirklich erst nach dem Verlassen des Kundens der Station erfolgt */
		event.init(simData.currentTime+TIME_DELTA+FastMath.round((resourcesBlockedTimeProcessing+resourcesBlockedTimePostProcessing)*simData.runModel.scaleToSimTime));
		event.station=this;
		event.resourceAlternative=resourceAlternative;
		simData.eventManager.addEvent(event);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		final RunElementProcessData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			boolean clientServingStarted=true;
			while (clientServingStarted) { /* Evtl. mehrere Bedienungen ansto�en, wenn mehrere Bediener freigeworden sind ... */
				clientServingStarted=false;

				boolean batchReady;
				if (client==null) {
					batchReady=data.waitingClients.size()>=batchMinSize;
				} else {
					/* Kunde an Warteschlange anstellen */
					batchReady=data.addClientToQueue(client,simData.currentTime,simData);
				}

				/* Noch nicht genug Kunden da? */
				if (!batchReady) return;

				if (batchMaxSize>batchMinSize && data.waitingClients.size()<batchMaxSize && client!=null) {
					/* Echte Kundenankunft, dann warten wir noch 1ms, ob weitere Kunden eintreffen. */
					ProcessWaitingClientsEvent event=(ProcessWaitingClientsEvent)simData.getEvent(ProcessWaitingClientsEvent.class);
					event.init(simData.currentTime+1);
					event.station=this;
					simData.eventManager.addEvent(event);
				} else {
					if (client!=null && data.waitingClients.size()>=batchMaxSize+1) {
						/* Es waren vorher schon genug Kunden da, um eine Bedienung zu starten, und eben ist einer weiterer eingetroffen. => Das pr�fen, ob eine Ressource freigeworden ist, k�nnen wir uns sparen. */
						return;
					}

					/* Gibt es freie Bediener? */
					int startIndex=resourceCheckInRandomOrder?((int)Math.floor(Math.random()*resources.length)):0;
					int i=startIndex;
					while (true) {
						final double additionalTime=simData.runData.resources.tryLockResources(resources[i],simData,id);
						if (additionalTime>=0) {
							/* Fixe Batch-Gr��e oder wir sind bereits im ProcessWaitingClientsEvent-Fall (oder Check durch freigewordenen Bediener). */
							if (batchMaxSize==1) {
								startProcessingSingle(simData,data,i,additionalTime);
							} else {
								startProcessingBatch(simData,data,i,additionalTime);
							}
							clientServingStarted=true;
							break;
						}
						i++;
						if (i>=resources.length) i=0;
						if (i==startIndex) break;
					}
				}

				client=null;
			}
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public void processLeave(final SimulationData simData, final RunDataClient client) {
		if (client.lastQueueSuccess) simData.runData.logClientLeavesStationProcess(simData,this,null,client);
		StationLeaveEvent.sendToStation(simData,client,this,client.lastQueueSuccess?connectionSuccess:connectionCancel);
	}

	/**
	 * Wird von {@link WaitingCancelEvent} aufgerufen, wenn die Wartezeittoleranz
	 * eines Kunden ersch�pft ist.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde, der das Warten aufgibt
	 * @param deltaMS	Verz�gerung in MS bevor der Kunde ide Station verl�sst ausgef�hrt wird
	 */
	public void processWaitingCancel(final SimulationData simData, final RunDataClient client, final long deltaMS) {
		final RunElementProcessData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			/* Kunden aus Warteschlange entfernen */
			final long waitingTime=data.removeClientFromQueue(client,-1,simData.currentTime,false,simData);

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ProcessCancelation"),String.format(Language.tr("Simulation.Log.ProcessCancelation.Info"),client.logInfo(simData),name));

			/* Bedienzeit in Statistik */
			simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
			client.addStationTime(id,waitingTime,0,0,waitingTime);
		} finally {
			data.queueLockedForPickUp=false;
		}

		/* Weiter zur n�chsten Station */
		StationLeaveEvent.addLeaveEvent(simData,client,this,deltaMS);
	}

	@Override
	public void releasedResourcesNotify(SimulationData simData) {
		processArrival(simData,null);
	}

	@Override
	public ExpressionCalc getResourcePriority(SimulationData simData) {
		final RunElementProcessData data=getData(simData);
		return data.resourcePriority;
	}

	@Override
	public double getSecondaryResourcePriority(SimulationData simData) {
		final RunElementProcessData data=getData(simData);

		double maxPriority=-Double.MAX_VALUE;
		boolean hasScore=false;
		/* Ben�tigt viel, viel mehr Speicher: for (RunDataClient client: data.waitingClients) */
		final List<RunDataClient> list=data.waitingClients;
		final int size=list.size();
		for (int i=0;i<size;i++) {
			final double score=getClientScore(simData,data,list.get(i));
			if (score>maxPriority) {maxPriority=score; hasScore=true;}
		}
		if (hasScore) return maxPriority;

		return 0.0;
	}

	@Override
	public RunDataClient getClient(SimulationData simData) {
		final RunElementProcessData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.get(0);

		long waitingTime=data.removeClientFromQueue(client,0,simData.currentTime,true,simData);
		/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0); */
		client.addStationTime(id,waitingTime,0,0,waitingTime);

		return client;
	}

	@Override
	public RunElement getNext() {
		return connectionSuccess;
	}

	/**
	 * Liefert die Liste der Kunden an in der Warteschlange an dieser Station
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liste der Kunden (ist nie <code>null</code>, aber kann leer sein)
	 */
	public List<RunDataClient> getClientsInQueue(final SimulationData simData) {
		final RunElementProcessData data=getData(simData);
		return data.waitingClients;
	}
}