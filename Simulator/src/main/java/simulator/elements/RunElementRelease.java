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
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.ReleaseReleaseResources;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementRelease;
import ui.modeleditor.elements.ModelElementSeize;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementRelease}
 * @author Alexander Herzog
 * @see ModelElementRelease
 */
public class RunElementRelease extends RunElementPassThrough {
	/** ID des zugehörigen "Ressource belegen"-Elements */
	private int seizeId;
	/** Zugehöriges "Ressource belegen"-Element (aus {@link #seizeId} übersetzt) */
	private RunElementSeize seize;

	/** Multiplikationsfaktor für {@link #distributionDelayedRelease} oder {@link #expressionDelayedRelease} */
	private double timeBaseMultiply;
	/** Zeitdauernverteilungen für die verzögerte Ressourcenfreigabe (jeweils in Abhängigkeit vom Kundentyp) */
	private AbstractRealDistribution[] distributionDelayedRelease;
	/** Rechenausdrücke für die Zeitdauern für die verzögerte Ressourcenfreigabe (jeweils in Abhängigkeit vom Kundentyp) */
	private String[] expressionDelayedRelease;

	/** Gibt an wie viele Bediener in welcher Bedienergruppe freizugeben sind */
	private int[] resources;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementRelease(final ModelElementRelease element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Release.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementRelease)) return null;
		final ModelElementRelease releaseElement=(ModelElementRelease)element;
		final RunElementRelease release=new RunElementRelease(releaseElement);

		/* Auslaufende Kante */
		final String edgeError=release.buildEdgeOut(releaseElement);
		if (edgeError!=null) return edgeError;

		/* Ressourcen */
		ModelElementSeize seizeElement=null;
		for (ModelElement e: element.getSurface().getElements()) if (e instanceof ModelElementSeize && e.getName().equals(releaseElement.getSeizeName())) {
			seizeElement=(ModelElementSeize)e; break;
		}
		if (seizeElement==null) return String.format(Language.tr("Simulation.Creator.ReleaseUnknownResource"),element.getId(),releaseElement.getSeizeName());
		release.seizeId=seizeElement.getId();
		release.resources=runModel.resourcesTemplate.getNeededResourcesRecord(seizeElement.getNeededResources());
		if (release.resources==null) return String.format(Language.tr("Simulation.Creator.ReleaseInvalidResource"),element.getId());

		/* Verzögerte Freigabe */
		release.timeBaseMultiply=releaseElement.getTimeBase().multiply;
		release.distributionDelayedRelease=new AbstractRealDistribution[runModel.clientTypes.length];
		release.expressionDelayedRelease=new String[runModel.clientTypes.length];
		for (int i=0;i<release.distributionDelayedRelease.length;i++) {
			final Object data=releaseElement.getReleaseDelay().getOrDefault(runModel.clientTypes[i]);
			if (data!=null) {
				if (data instanceof String) {
					final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames,runModel.modelUserFunctions);
					final int error=calc.parse((String)data);
					if (error>=0) return String.format(Language.tr("Simulation.Creator.ReleaseDelayed"),element.getId(),runModel.clientTypes[i],data,error+1);
					release.expressionDelayedRelease[i]=(String)data;
				} else {
					release.distributionDelayedRelease[i]=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
				}
			}
		}

		return release;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementRelease)) return null;
		final ModelElementRelease releaseElement=(ModelElementRelease)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(releaseElement);
		if (edgeError!=null) return edgeError;

		/* Ressourcen */
		ModelElementSeize seizeElement=null;
		for (ModelElement e: element.getSurface().getElements()) if (e instanceof ModelElementSeize && e.getName().equals(releaseElement.getSeizeName())) {
			seizeElement=(ModelElementSeize)e; break;
		}
		if (seizeElement==null) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ReleaseUnknownResource"),element.getId(),releaseElement.getSeizeName()));
		if (seizeElement.getNeededResources().size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.ReleaseInvalidResource"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		super.prepareRun(runModel); /* von RunElementPassThrough */
		seize=(RunElementSeize)runModel.elements.get(seizeId);
	}

	@Override
	public RunElementReleaseData getData(final SimulationData simData) {
		RunElementReleaseData data;
		data=(RunElementReleaseData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementReleaseData(this,expressionDelayedRelease,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementSeizeData dataSeize=seize.getData(simData);
		if (dataSeize.blockedRessourcesCount==0) {
			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ReleaseNotLocked"),String.format(Language.tr("Simulation.Log.ReleaseNotLocked.Info"),client.logInfo(simData),name));
		} else {
			/* Verzögerung bei der Ressourcenfreigabe bestimmen */
			double value=0.0;
			if (distributionDelayedRelease[client.type]!=null) {
				value=simData.runData.random.randomNonNegative(distributionDelayedRelease[client.type]);
			} else {
				final ExpressionCalc calc=getData(simData).delayExpression[client.type];
				if (calc!=null) {
					simData.runData.setClientVariableValues(client);
					try {
						value=getData(simData).delayExpression[client.type].calc(simData.runData.variableValues,simData,client);
						if (value<0) value=0;
					} catch (MathCalcError e) {
						simData.calculationErrorStation(getData(simData).delayExpression[client.type],this);
						value=0;
					}
				}
			}

			final double delayTime;
			final long delayTimeMS;
			if (value>0) {
				delayTime=FastMath.max(0,value)*timeBaseMultiply;
				delayTimeMS=FastMath.round(delayTime*simData.runModel.scaleToSimTime);
			} else {
				delayTime=0;
				delayTimeMS=0;
			}

			if (delayTimeMS==0) {
				/* Ressourcen sofort freigaben */
				simData.runData.resources.releaseResources(resources,simData);

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Release"),String.format(Language.tr("Simulation.Log.Release.Info"),client.logInfo(simData),name));
			} else {
				/* Ressourcen später freigeben */
				ReleaseReleaseResources event=(ReleaseReleaseResources)simData.getEvent(ReleaseReleaseResources.class);
				event.init(simData.currentTime+delayTimeMS);
				event.resources=resources;
				event.station=this;
				simData.eventManager.addEvent(event);

				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Release"),String.format(Language.tr("Simulation.Log.Release.InfoDelay1"),client.logInfo(simData),name,TimeTools.formatExactTime(delayTime)));
			}
		}

		/* Prüfen, ob andere Stationen auf diese Ressourcen warten */
		simData.runData.fireReleasedResourcesNotify(simData);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
