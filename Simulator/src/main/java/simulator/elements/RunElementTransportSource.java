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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.ReleaseReleaseResources;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSectionStart;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.ModelElementTransportSource;
import ui.modeleditor.elements.TransportResourceRecord;
import ui.modeleditor.elements.TransportTimeRecord;

/**
 * Äquivalent zu <code>ModelElementTransportSource</code>
 * @author Alexander Herzog
 * @see ModelElementTransportSource
 */
public class RunElementTransportSource extends RunElement implements FreeResourcesListener {
	/** Laufzeitdaten für die Transportzeiten */
	private final RunElementTransportSourceTime transportTime;
	/** Laufzeitdaten für die Transportziele */
	private final RunElementTransportSourceTargets transportTargets;

	/** Array mit den Ressourcenbedarfszuordnungen  */
	private int[] resources;
	/** Ausdruck zur Berechnung der Priorität für die Ressourcenzuweisung */
	private String resourcePriority;
	/** Optionale transportziel-abhängige Verteilungen zur Verzögerungen bei der Ressourcenfreigabe */
	private AbstractRealDistribution[] releaseDelayDistributions;
	/** Optionale transportziel-abhängige Rechenausdrücke für die Verzögerungen bei der Ressourcenfreigabe */
	private String[] releaseDelayExpressions;
	/** Umrechnungsfaktoren auf Sekunden für {@link #releaseDelayDistributions} und {@link #releaseDelayExpressions} */
	private double releaseDelayTimeBaseMultiply;

	/** "Bereich betreten"-Station des Bereichs, der bei diesem Transport implizit verlassen werden soll (optional; Übersetzung aus {@link #sectionID}) */
	private RunElementSectionStart section;
	/** ID der "Bereich betreten"-Station des Bereichs, der bei diesem Transport implizit verlassen werden soll (optional) */
	private int sectionID;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementTransportSource(final ModelElementTransportSource element) {
		super(element,buildName(element,Language.tr("Simulation.Element.TransportSource.Name")));
		transportTime=new RunElementTransportSourceTime(element);
		transportTargets=new RunElementTransportSourceTargets(element);
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementTransportSource)) return null;

		final ModelElementTransportSource sourceElement=(ModelElementTransportSource)element;
		final RunElementTransportSource source=new RunElementTransportSource(sourceElement);

		String error;

		/* Transportzeit */
		error=source.transportTime.build(editModel,runModel,sourceElement.getTransportTimeRecord());
		if (error!=null) return error;

		/* Transportziele */
		error=source.transportTargets.build(editModel,runModel,sourceElement.getTransportTargetSystem());
		if (error!=null) return error;

		/* Ressource */
		final TransportResourceRecord resourceRecord=sourceElement.getTransportResourceRecord();
		if (resourceRecord.getResources().size()<1) {
			source.resources=null;
			source.resourcePriority="1";
		} else {
			/* Ressourcenname + Anzahl */
			source.resources=runModel.resourcesTemplate.getNeededResourcesRecord(resourceRecord.getResources());
			if (source.resources==null) return String.format(Language.tr("Simulation.Creator.TransportSourceInvalidResource"),element.getId());
			/* Priorität */
			source.resourcePriority=resourceRecord.getResourcePriority();
			int err=ExpressionCalc.check(source.resourcePriority,runModel.variableNames,runModel.modelUserFunctions);
			if (err>=0) return String.format(Language.tr("Simulation.Creator.TransportSourceResourcePriority"),element.getId(),source.resourcePriority);
			/* Verzögerte Freigabe */
			final int maxID=(editModel.surface.getParentSurface()!=null)?editModel.surface.getParentSurface().getMaxId():editModel.surface.getMaxId();
			source.releaseDelayTimeBaseMultiply=resourceRecord.getTimeBase().multiply;
			source.releaseDelayDistributions=new AbstractRealDistribution[maxID+1];
			source.releaseDelayExpressions=new String[maxID+1];
			for (int i=0;i<source.releaseDelayDistributions.length;i++) {
				final String stationName=getStationNameById(i,editModel.surface);
				if (stationName==null) continue;
				final Object data=resourceRecord.getDelayedRelease().getOrDefault(stationName);
				if (data!=null) {
					if (data instanceof String) {
						final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
						err=calc.parse((String)data);
						if (err>=0) return String.format(Language.tr("Simulation.Creator.TransportSourceResourceReleaseDelayed"),element.getId(),runModel.clientTypes[i],data,err+1);
						source.releaseDelayExpressions[i]=(String)data;
					} else {
						source.releaseDelayDistributions[i]=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
					}
				}
			}
		}

		/* Bereich */
		final String sectionName=sourceElement.getSectionStartName();
		if (!sectionName.isEmpty()) {
			source.sectionID=-1;
			for (ModelElement e1 : editModel.surface.getElements()) {
				if (e1 instanceof ModelElementSectionStart && e1.getName().equals(sectionName)) {source.sectionID=e1.getId(); break;}
				if (e1 instanceof ModelElementSub) {
					for (ModelElement e2 : editModel.surface.getElements()) {
						if (e2 instanceof ModelElementSectionStart && e2.getName().equals(sectionName)) {source.sectionID=e2.getId(); break;}
					}
					if (source.sectionID>=0) break;
				}
			}
			if (source.sectionID<0) return String.format(Language.tr("Simulation.Creator.InvalidSection"),element.getId(),sectionName);
		}

		return source;
	}

	/**
	 * Liefert basierend auf einer ID den Namen einer Station.
	 * @param id	ID der Station
	 * @param surface	Zeichenfläche
	 * @return	Liefert im Erfolgsfall den Namen, sonst <code>null</code>
	 */
	private String getStationNameById(final int id, ModelSurface surface) {
		if (surface.getParentSurface()!=null) surface=surface.getParentSurface();

		ModelElement element=surface.getByIdIncludingSubModels(id);
		if (element==null) return null;
		if (!(element instanceof ModelElementTransportDestination)) return null;
		final String name=element.getName();
		if (name==null || name.isEmpty()) return null;
		return name;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementTransportSource)) return null;

		final RunModelCreatorStatus error=RunElementTransportSourceTargets.test(element,((ModelElementTransportSource)element).getTransportTargetSystem());
		if (error!=null) return error;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementTransportSourceData getData(final SimulationData simData) {
		RunElementTransportSourceData data;
		data=(RunElementTransportSourceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementTransportSourceData(this,transportTime.expression,transportTargets.routingExpression,resourcePriority,releaseDelayExpressions,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Liefert die Zeit, die nach dem Transport vergehen soll, bis die Ressourcen
	 * weider freigegeben werden sollen.
	 * @param simData	Simulationsdatenobjekt
	 * @param targetID	ID der Zielstation zu der der Transport erfolgt ist
	 * @return	Verzögerungszeit vor der Freigabe der Ressourcen
	 */
	private double getReleaseDelay(final SimulationData simData, final int targetID) {
		if (releaseDelayDistributions!=null && releaseDelayDistributions[targetID]!=null) {
			return DistributionRandomNumber.randomNonNegative(releaseDelayDistributions[targetID])*releaseDelayTimeBaseMultiply;
		}
		if (releaseDelayExpressions!=null && releaseDelayExpressions[targetID]!=null) {
			final RunElementTransportSourceData data=getData(simData);
			simData.runData.setClientVariableValues(null);
			try {
				final double time=data.releaseDelayExpressions[targetID].calc(simData.runData.variableValues,simData,null)*releaseDelayTimeBaseMultiply;
				return (time>=0)?time:0;
			} catch (MathCalcError e) {
				simData.calculationErrorStation(data.releaseDelayExpressions[targetID],this);
				return 0;
			}
		}
		return 0.0;
	}

	/**
	 * Befördert einen Kunden zur Zielstation
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Zu befördernder Kunde
	 * @param waitingTime	Wartezeit die vor dem Transport für den Kunden entstanden ist
	 * @param additionalTransportTime	Optionale zusätzliche Rüstzeit
	 */
	private void transportClient(SimulationData simData, RunDataClient client, final long waitingTime, final double additionalTransportTime) {
		final RunElementTransportSourceData data=getData(simData);

		/* Zielstation bestimmen */
		client.stationInformationInt=transportTargets.getDestinationStation(simData,client,data.routingExpresions,false);

		/* Verzögerung bestimmen */
		final double transportDelayTime=transportTime.getTransportTime(simData,client,data.delayExpression,name)+additionalTransportTime;
		final long transportDelayTimeMS=FastMath.round(transportDelayTime*simData.runModel.scaleToSimTime);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TransportSource"),String.format(Language.tr("Simulation.Log.TransportSource.Info"),client.logInfo(simData),name,TimeTools.formatExactTime(transportDelayTime)));

		/* Transportzeit für Kunden und für Station in der Statistik erfassen */
		RunElementTransportSourceTools.logTransportTime(simData,this,transportTime,client,waitingTime,transportDelayTimeMS);

		/* Ggf. Ressourcenfreigabe einplanen */
		if (resources!=null) {
			final double releaseDelay=getReleaseDelay(simData,client.stationInformationInt);
			final long releaseDelayMS=FastMath.round(releaseDelay*simData.runModel.scaleToSimTime);
			ReleaseReleaseResources event=(ReleaseReleaseResources)simData.getEvent(ReleaseReleaseResources.class);
			event.init(simData.currentTime+transportDelayTimeMS+releaseDelayMS);
			event.resources=resources;
			event.station=this;
			simData.eventManager.addEvent(event);

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.TransportSource.Release"),String.format(Language.tr("Simulation.Log.TransportSource.Release.Info"),client.logInfo(simData),name,TimeTools.formatExactTime(transportDelayTime+releaseDelay)));
		}

		/* Kunde aus Bereich austragen */
		if (section==null && sectionID>=0) section=(RunElementSectionStart)simData.runModel.elementsFast[sectionID];
		if (section!=null) client.leaveSection(section,simData);

		/* Zählung der Anzahl an Kunden in Bedienung an der Station */
		if (transportTime.delayType==TransportTimeRecord.DelayType.DELAY_TYPE_PROCESS) {
			simData.runData.logClientEntersStationProcess(simData,this,data,client);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,transportDelayTimeMS);
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		if (resources==null) {
			/* Keine Ressourcen notwendig, Kunden direkt transportieren */
			if (client!=null) {
				client.lastWaitingStart=simData.currentTime;
				transportClient(simData,client,0,0.0);
			}
		} else {
			final RunElementTransportSourceData data=getData(simData);
			if (client!=null) {
				/* Neuen Kunden an Warteschlange anstellen */
				data.addClientToQueue(client,simData.currentTime,simData);
			}

			/* Warten Kunden? */
			if (data.waitingClients.size()>0) {
				/* Ressource verfügbar? */
				final double additionalTime=simData.runData.resources.tryLockResources(resources,simData,id);
				if (additionalTime>=0) {
					/* Kunden aus Warteschlange holen und transportieren */
					client=data.removeClientFromQueue(simData);
					transportClient(simData,client,simData.currentTime-client.lastWaitingStart,additionalTime);
				}
			}
		}
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		final RunElement nextStation=(client.stationInformationInt>=0)?simData.runModel.elementsFast[client.stationInformationInt]:null;
		if (nextStation!=null) {
			/* Zählung der Anzahl an Kunden in Bedienung an der Station */
			if (transportTime.delayType==TransportTimeRecord.DelayType.DELAY_TYPE_PROCESS) {
				simData.runData.logClientLeavesStationProcess(simData,this,null,client);
			}

			StationLeaveEvent.sendToStationByTransporter(simData,client,this,nextStation);
		}
	}

	@Override
	public void releasedResourcesNotify(SimulationData simData) {
		if (resources==null) return; /* Wir brauchen gar keine Ressourcen. */

		processArrival(simData,null);
	}

	@Override
	public ExpressionCalc getResourcePriority(SimulationData simData) {
		final RunElementTransportSourceData data=getData(simData);
		return data.resourcePriority;
	}

	@Override
	public double getSecondaryResourcePriority(SimulationData simData) {
		return 0.0;
	}
}