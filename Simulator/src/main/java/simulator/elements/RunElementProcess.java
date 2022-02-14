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
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementProcess</code>
 * @author Alexander Herzog
 * @see ModelElementProcess
 */
public class RunElementProcess extends RunElement implements FreeResourcesListener, PickUpQueue {
	/** ID der Station an die erfolgreiche Kunden weitergeleitet werden */
	private int connectionIdSuccess;
	/** ID der Station an die Warteabbrecher weitergeleitet werden */
	private int connectionIdCancel;
	/** Station an die erfolgreiche Kunden weitergeleitet werden (Übersetzung von {@link #connectionIdSuccess}) */
	private RunElement connectionSuccess;
	/** Station an die Warteabbrecher weitergeleitet werden (Übersetzung von {@link #connectionIdSuccess}) */
	private RunElement connectionCancel;

	/** Minimale Batch-Größe */
	public int batchMinSize;
	/** Maximale Batch-Größe */
	public int batchMaxSize;

	/** Multiplikationsfaktor um bei den Verteilungs- und Ausdruckswerten für Bedienzeiten usw. auf Sekunden zu kommen */
	public double timeBaseMultiply;
	/** Rüstzeit-Verteilungen */
	public AbstractRealDistribution[][] distributionSetup;
	/** Bedienzeit-Verteilungen */
	public AbstractRealDistribution[] distributionProcess;
	/** Nachbearbeitungszeit-Verteilungen */
	public AbstractRealDistribution[] distributionPostProcess;
	/** Wartezeittoleranz-Verteilungen */
	public AbstractRealDistribution[] distributionCancel;
	/** Rüstzeit-Rechenausdrücke */
	public String[][] expressionSetup;
	/** Bedienzeit-Rechenausdrücke */
	public String[] expressionProcess;
	/** Nachbearbeitungszeit-Rechenausdrücke */
	public String[] expressionPostProcess;
	/** Wartezeittoleranz-Rechenausdrücke */
	public String[] expressionCancel;
	/** Prioritäts-Rechenausdrücke */
	public String[] priority;

	/** Art der Zählung der Prozesszeiten */
	private ModelElementProcess.ProcessType processTimeType=ModelElementProcess.ProcessType.PROCESS_TYPE_PROCESS;

	/** Ressourcenpriorität */
	public String resourcePriority;
	/** Ressourcenbedarf pro Ressourcen-Alternative */
	public int[][] resources;

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
	 * @param element	Zugehöriges Editor-Element
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

		/* Zeitbasis */
		process.timeBaseMultiply=processElement.getTimeBase().multiply;

		/* Bedienzeit ist ... */
		process.processTimeType=processElement.getProcessTimeType();

		/* Rüstzeiten */
		if (process.batchMaxSize>1 && processElement.getSetupTimes().isActive()) return String.format(Language.tr("Simulation.Creator.ProcessCannotMixBatchAndSetupTimes"),element.getId());
		process.distributionSetup=processElement.getSetupTimes().getAllDistributions(runModel.clientTypes);
		process.expressionSetup=processElement.getSetupTimes().getAllExpressions(runModel.clientTypes);
		for (int i=0;i<process.expressionSetup.length;i++) for (int j=0;j<process.expressionSetup.length;j++) {
			final String s=process.expressionSetup[i][j];
			if (s!=null) {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
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
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
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
					final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
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
					final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
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

		/* Prioritäten */
		process.priority=new String[runModel.clientTypes.length];
		for (int i=0;i<process.priority.length;i++) {
			String priorityString=processElement.getPriority(runModel.clientTypes[i]);
			if (priorityString==null || priorityString.trim().isEmpty()) priorityString=ModelElementProcess.DEFAULT_CLIENT_PRIORITY;
			if (priorityString.equalsIgnoreCase(ModelElementProcess.DEFAULT_CLIENT_PRIORITY)) {
				process.priority[i]=null; /* Default Priorität als null vermerken */
			} else {
				final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
				final int error=calc.parse(priorityString);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessClientPriority"),element.getId(),runModel.clientTypes[i],priorityString,error+1);

				process.priority[i]=priorityString;
			}
		}

		/* Ressourcen-Priorität */
		if (!processElement.getResourcePriority().equals("1")) {
			final int error=ExpressionCalc.check(processElement.getResourcePriority(),runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.ProcessResourcePriority"),element.getId(),processElement.getResourcePriority());
		}
		process.resourcePriority=processElement.getResourcePriority();

		/* Ressourcen */
		final int[][] res=runModel.resourcesTemplate.getNeededResourcesRecord(processElement.getNeededResources());
		if (res==null || res.length==0) return String.format(Language.tr("Simulation.Creator.ProcessResource"),element.getId());
		for (int[] r: res) if (r==null || r.length==0) return String.format(Language.tr("Simulation.Creator.ProcessResource"),element.getId());
		process.resources=res;

		/* Kosten */
		String text;

		text=processElement.getCosts();
		if (text==null || text.trim().isEmpty()  || text.trim().equals("0")) {
			process.costs=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcess"),text,element.getId(),error+1);
			process.costs=text;
		}

		text=processElement.getCostsPerProcessSecond();
		if (text==null || text.trim().isEmpty()  || text.trim().equals("0")) {
			process.costsPerProcessSecond=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcessPerProcessSecond"),text,element.getId(),error+1);
			process.costsPerProcessSecond=text;
		}

		text=processElement.getCostsPerPostProcessSecond();
		if (text==null || text.trim().isEmpty()  || text.trim().equals("0")) {
			process.costsPerPostProcessSecond=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
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

		/* Rüstzeiten */
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
			data=new RunElementProcessData(this,simData.runModel.variableNames,costs,costsPerProcessSecond,costsPerPostProcessSecond);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSecFactor=1.0/1000.0;

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
			return (((double)simData.currentTime)-client.lastWaitingStart)*toSecFactor;
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
	 * Erfasst die Kosten für einen Bedienvorgang
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
			costsPerProcessSecondValue=0.0; /* Wir können uns die Berechnung sparen, wenn überhaupt keine entsprechende Zeit angefallen ist. */
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
			costsPerPostProcessSecondValue=0.0;  /* Wir können uns die Berechnung sparen, wenn überhaupt keine entsprechende Zeit angefallen ist. */
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
	 * @param resourceAlternative	Gewählte Ressourcen-Alternative (0-basierend)
	 * @param additionalPrepareTime	Notwendige Rüstzeit (kann 0 sein)
	 * @see #processArrival(SimulationData, RunDataClient)
	 */
	private void startProcessingSingle(final SimulationData simData, final RunElementProcessData data, final int resourceAlternative, final double additionalPrepareTime) {
		final RunElementProcessData processData=getData(simData);

		RunDataClient selected;
		int bestIndex=-1;
		if (processData.allFirstComeFirstServe) {
			/* Ganzes System ist FCFS */
			selected=data.waitingClients.get(0);
			bestIndex=0;
		} else {
			/* Kunden mit dem höchsten Score wählen */
			final int count=data.waitingClients.size();
			selected=data.waitingClients.get(0);
			bestIndex=0;
			double bestScore=-Double.MAX_VALUE;
			if (count>1) for (int i=1;i<count;i++) {
				final RunDataClient client=data.waitingClients.get(i);
				final double score=getClientScore(simData,processData,client);
				if (score>bestScore) {
					/* Ein Kunde weiter hinten in der Liste (=spätere Ankunft) braucht eine höhere Score, um den vorherigen zu überbieten. D.h. bei Score-Gleichstand zwischen zwei Kunden gilt FIFO. */
					bestScore=score;
					bestIndex=i;
					selected=client;
				}
			}
		}

		/* Bedienzeit bestimmen */
		double setupTime=processData.getSetupTime(simData,selected);
		double processingTime=processData.getProcessTime(simData,selected)+additionalPrepareTime;
		double postProcessingTime=processData.getPostProcessTime(simData,selected);
		long setupTimeMS=FastMath.round(setupTime*1000);
		long processingTimeMS=FastMath.round(processingTime*1000);
		long postProcessingTimeMS=FastMath.round(postProcessingTime*1000);

		/* Kunden aus der Warteschlange entfernen */
		final long waitingTimeMS=data.removeClientFromQueue(selected,bestIndex,simData.currentTime,true,simData);

		/* Logging */
		if (simData.loggingActive) {
			if (setupTimeMS>0) {
				log(simData,Language.tr("Simulation.Log.ProcessService"),String.format(Language.tr("Simulation.Log.ProcessService.InfoWithSetupTime"),selected.logInfo(simData),name,TimeTools.formatTime(processingTimeMS/1000),TimeTools.formatTime(setupTimeMS/1000)));
			} else {
				log(simData,Language.tr("Simulation.Log.ProcessService"),String.format(Language.tr("Simulation.Log.ProcessService.Info"),selected.logInfo(simData),name,TimeTools.formatTime(processingTimeMS/1000)));
			}
		}

		/* Bedienzeit in Statistik */
		final long residenceTimeMS=waitingTimeMS+setupTimeMS+processingTimeMS;
		switch (processTimeType) {
		case PROCESS_TYPE_WAITING: simData.runData.logStationProcess(simData,this,selected,residenceTimeMS,0,0,residenceTimeMS); break;
		case PROCESS_TYPE_TRANSFER: simData.runData.logStationProcess(simData,this,selected,waitingTimeMS,setupTimeMS+processingTimeMS,0,residenceTimeMS); break;
		case PROCESS_TYPE_PROCESS: simData.runData.logStationProcess(simData,this,selected,waitingTimeMS,0,setupTimeMS+processingTimeMS,residenceTimeMS); break;
		case PROCESS_TYPE_NOTHING: simData.runData.logStationProcess(simData,this,selected,waitingTimeMS,0,0,residenceTimeMS); break; /* nicht erfassen */
		}
		switch (processTimeType) {
		case PROCESS_TYPE_WAITING: selected.addStationTime(id,waitingTimeMS+setupTimeMS+processingTimeMS,0,0,waitingTimeMS+setupTimeMS+processingTimeMS); break;
		case PROCESS_TYPE_TRANSFER: selected.addStationTime(id,waitingTimeMS,setupTimeMS+processingTimeMS,0,waitingTimeMS+setupTimeMS+processingTimeMS); break;
		case PROCESS_TYPE_PROCESS: selected.addStationTime(id,waitingTimeMS,0,setupTimeMS+processingTimeMS,waitingTimeMS+setupTimeMS+processingTimeMS); break;
		case PROCESS_TYPE_NOTHING: /* nicht erfassen */ break;
		}
		selected.lastAlternative=resourceAlternative+1;

		/* Weiterleitung zu nächster Station nach Bedienzeit-Ende */
		final StationLeaveEvent leaveEvent=StationLeaveEvent.addLeaveEvent(simData,selected,this,setupTimeMS+processingTimeMS);

		/* Kosten in Statistik erfassen */
		if (processData.hasCosts) {
			logCosts(simData,processData,selected,setupTime+processingTime,postProcessingTime);
		}

		/* Erfassung der Zwischenabgangszeiten auf Batch-Basis (bei dynamischen Bedien-Batches - hier also einzelnen Kunden) */
		simData.runData.logStationBatchLeave(simData.currentTime+setupTimeMS+processingTimeMS,simData,this,data);

		/* Belegte Ressourcen am Ende der Nachbearbeitungszeit wieder freigeben */
		final ProcessReleaseResources releaseEvent=(ProcessReleaseResources)simData.getEvent(ProcessReleaseResources.class);
		releaseEvent.init(simData.currentTime+setupTimeMS+processingTimeMS+postProcessingTimeMS);
		releaseEvent.station=this;
		releaseEvent.resourceAlternative=resourceAlternative;
		leaveEvent.addNextEvent=releaseEvent; /* Freigabeereignis gesichert erst nach dem Abgangsereignis ausführen */
	}

	/**
	 * Startet die Bedienung eines Batches.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param resourceAlternative	Gewählte Ressourcen-Alternative (0-basierend)
	 * @param additionalPrepareTime	Notwendige Rüstzeit (kann 0 sein)
	 * @see #processArrival(SimulationData, RunDataClient)
	 */
	private void startProcessingBatch(final SimulationData simData, final RunElementProcessData data, final int resourceAlternative, final double additionalPrepareTime) {
		final RunElementProcessData processData=getData(simData);

		final int count=data.waitingClients.size();

		if (!processData.allFirstComeFirstServe) {
			/* Scorewerte für alle wartenden Kunden berechnen */
			if (processData.score==null || processData.score.length<count) processData.score=new double[count];

			for (int i=0;i<count;i++) {
				final RunDataClient client=data.waitingClients.get(i);
				final ExpressionCalc calc=processData.priority[client.type];
				if (calc==null) { /* = Text war "w", siehe RunElementProcessData()  */
					final double waitingTime=(((double)simData.currentTime)-client.lastWaitingStart)*toSecFactor;
					processData.score[i]=waitingTime;
				} else {
					simData.runData.setClientVariableValues(simData.currentTime-client.lastWaitingStart,client.transferTime,client.processTime);
					try {
						processData.score[i]=calc.calc(simData.runData.variableValues,simData,client);
					} catch (MathCalcError e) {
						simData.calculationErrorStation(calc,this);
						processData.score[i]=0;
					}
				}
			}
		}

		List<RunDataClient> selectedForService;
		if (processData.canUseGlobalSelectedForService) {
			if (processData.globalSelectedForService==null) processData.globalSelectedForService=new ArrayList<>(batchMaxSize);
			selectedForService=processData.globalSelectedForService;
		} else {
			selectedForService=new ArrayList<>(batchMaxSize);
		}
		processData.canUseGlobalSelectedForService=false;

		try {
			/* Die maximal batchMaxSize Kunden mit den höchsten Scorewerten bestimmen */
			selectedForService.clear();
			while (selectedForService.size()<batchMaxSize && selectedForService.size()<data.waitingClients.size()) {
				if (processData.allFirstComeFirstServe) {
					selectedForService.add(data.waitingClients.get(selectedForService.size()));
				} else {
					double maxScore=-Double.MAX_VALUE;
					RunDataClient maxScoreClient=data.waitingClients.get(0);
					for (int i=0;i<count;i++) {
						final RunDataClient client=data.waitingClients.get(i);
						if (selectedForService.indexOf(client)>=0) continue;
						if (processData.score[i]>maxScore) {maxScore=processData.score[i]; maxScoreClient=client;}
					}
					selectedForService.add(maxScoreClient);
				}
			}

			/* Bedienzeiten bestimmen */
			if (processData.processingTimes==null) processData.processingTimes=new double[simData.runModel.clientTypes.length]; else Arrays.fill(processData.processingTimes,0.0);
			if (processData.postProcessingTimes==null) processData.postProcessingTimes=new double[simData.runModel.clientTypes.length]; else Arrays.fill(processData.postProcessingTimes,0.0);
			for (int i=0;i<selectedForService.size();i++) {
				final RunDataClient client=selectedForService.get(i);  /* Iterator würde mehr Arbeitsspeicher brauchen */
				final int type=client.type;
				if (processData.processingTimes[type]!=0.0) continue;
				/* hier keine Rüstzeiten; wird schon im Builder ausgeschlossen */
				processData.processingTimes[type]=processData.getProcessTime(simData,client)+additionalPrepareTime;
				processData.postProcessingTimes[type]=processData.getPostProcessTime(simData,client);
			}

			/* Kunden aus der Warteschlange entfernen */
			for (int i=0;i<selectedForService.size();i++) {
				final RunDataClient client=selectedForService.get(i); /* Iterator würde mehr Arbeitsspeicher brauchen */
				final int type=client.type;

				/* Bedien- und Nachbearbeitungszeit aus Liste (s.o.) wählen */
				final long waitingTime=data.removeClientFromQueue(client,-1,simData.currentTime,true,simData);
				final long processingTime=FastMath.round(processData.processingTimes[type]*1000);

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ProcessService"),String.format(Language.tr("Simulation.Log.ProcessService.Info"),client.logInfo(simData),name,TimeTools.formatTime(processingTime/1000)));

				/* Bedienzeit in Statistik */
				final long residenceTime=waitingTime+processingTime;
				switch (processTimeType) {
				case PROCESS_TYPE_WAITING: simData.runData.logStationProcess(simData,this,client,waitingTime+processingTime,0,0,residenceTime); break;
				case PROCESS_TYPE_TRANSFER: simData.runData.logStationProcess(simData,this,client,waitingTime,processingTime,0,residenceTime); break;
				case PROCESS_TYPE_PROCESS: simData.runData.logStationProcess(simData,this,client,waitingTime,0,processingTime,residenceTime); break;
				case PROCESS_TYPE_NOTHING: simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,residenceTime); break; /* nicht erfassen */
				}
				switch (processTimeType) {
				case PROCESS_TYPE_WAITING: client.addStationTime(id,waitingTime+processingTime,0,0,waitingTime+processingTime); break;
				case PROCESS_TYPE_TRANSFER: client.addStationTime(id,waitingTime,processingTime,0,waitingTime+processingTime); break;
				case PROCESS_TYPE_PROCESS: client.addStationTime(id,waitingTime,0,processingTime,waitingTime+processingTime); break;
				case PROCESS_TYPE_NOTHING: /* nicht erfassen */ break;
				}
				client.lastAlternative=resourceAlternative+1;

				/* Weiterleitung zu nächster Station nach Bedienzeit-Ende */
				StationLeaveEvent.addLeaveEvent(simData,client,this,processingTime);
			}
		} finally {
			if (selectedForService==processData.globalSelectedForService) processData.canUseGlobalSelectedForService=true;
		}

		/* Zeit bestimmen, die die Bediener blockiert sind */
		double resourcesBlockedTimeProcessing=0;
		double resourcesBlockedTimePostProcessing=0;
		for (double d: processData.processingTimes) resourcesBlockedTimeProcessing=FastMath.max(resourcesBlockedTimeProcessing,d);
		for (double d: processData.postProcessingTimes) resourcesBlockedTimePostProcessing=FastMath.max(resourcesBlockedTimePostProcessing,d);

		/* Kosten in Statistik erfassen */
		if (processData.hasCosts) {
			logCosts(simData,processData,null,resourcesBlockedTimeProcessing,resourcesBlockedTimePostProcessing);
		}

		/* Erfassung der Zwischenabgangszeiten auf Batch-Basis (bei dynamischen Bedien-Batches) */
		simData.runData.logStationBatchLeave(simData.currentTime+FastMath.round(resourcesBlockedTimeProcessing*1000),simData,this,data);

		/* Belegte Ressourcen am Ende der Nachbearbeitungszeit wieder freigeben */
		ProcessReleaseResources event=(ProcessReleaseResources)simData.getEvent(ProcessReleaseResources.class);
		/* Da bei mehreren Kunden-Abgangs-Ereignissen nicht anders sichergestellt werden kann, dass die Ressourcenfreigabe erst sicher nach dem letzten Abgang erfolgt, wird hier das Zeit-Delta benötigt. */
		final int TIME_DELTA=(resourcesBlockedTimePostProcessing>0)?0:1; /* Zeit in ms die vor der Freigabe der Ressourcen noch verstreichen sollen, damit die Freigabe wirklich erst nach dem Verlassen des Kundens der Station erfolgt */
		event.init(simData.currentTime+TIME_DELTA+FastMath.round((resourcesBlockedTimeProcessing+resourcesBlockedTimePostProcessing)*1000));
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
			while (clientServingStarted) { /* Evtl. mehrere Bedienungen anstoßen, wenn mehrere Bediener freigeworden sind ... */
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
						/* Es waren vorher schon genug Kunden da, um eine Bedienung zu starten, und eben ist einer weiterer eingetroffen. => Das prüfen, ob eine Ressource freigeworden ist, können wir uns sparen. */
						return;
					}

					/* Gibt es freie Bediener? */
					for (int i=0;i<resources.length;i++) {
						final double additionalTime=simData.runData.resources.tryLockResources(resources[i],simData,id);
						if (additionalTime>=0) {
							/* Fixe Batch-Größe oder wir sind bereits im ProcessWaitingClientsEvent-Fall (oder Check durch freigewordenen Bediener). */
							if (batchMaxSize==1) {
								startProcessingSingle(simData,data,i,additionalTime);
							} else {
								startProcessingBatch(simData,data,i,additionalTime);
							}
							clientServingStarted=true;
							break;
						}
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
		StationLeaveEvent.sendToStation(simData,client,this,client.lastQueueSuccess?connectionSuccess:connectionCancel);
	}

	/**
	 * Wird von {@link WaitingCancelEvent} aufgerufen, wenn die Wartezeittoleranz
	 * eines Kunden erschöpft ist.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde, der das Warten aufgibt
	 */
	public void processWaitingCancel(final SimulationData simData, final RunDataClient client) {
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

		/* Weiter zur nächsten Station */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
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
		/* Benötigt viel, viel mehr Speicher: for (RunDataClient client: data.waitingClients) */
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