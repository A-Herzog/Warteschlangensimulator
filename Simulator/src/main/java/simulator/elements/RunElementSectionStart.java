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

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSectionStart;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSectionStart</code>
 * @author Alexander Herzog
 * @see ModelElementSectionStart
 */
public class RunElementSectionStart extends RunElementPassThrough {
	/**
	 * Name des Abschnitts in Kleinbuchstaben.<br>
	 * Wird von {@link RunElementSectionStartData#reportedClientsAtStation(SimulationData)} benötigt.
	 */
	public String sectionNameLower;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSectionStart(final ModelElementSectionStart element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SectionStart.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSectionStart)) return null;
		final ModelElementSectionStart sectionStartElement=(ModelElementSectionStart)element;
		final RunElementSectionStart sectionStart=new RunElementSectionStart(sectionStartElement);

		/* Auslaufende Kante */
		final String edgeError=sectionStart.buildEdgeOut(sectionStartElement);
		if (edgeError!=null) return edgeError;

		/* Name der Station */
		if (element.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.NoName"),element.getId());
		sectionStart.sectionNameLower=element.getName().trim().toLowerCase();

		return sectionStart;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSectionStart)) return null;
		final ModelElementSectionStart sectionStartElement=(ModelElementSectionStart)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(sectionStartElement);
		if (edgeError!=null) return edgeError;

		/* Name der Station */
		if (element.getName().isEmpty()) return RunModelCreatorStatus.noName(element);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSectionStartData getData(final SimulationData simData) {
		RunElementSectionStartData data;
		data=(RunElementSectionStartData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSectionStartData(this);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Kunde zu Bereich hinzufügen */
		client.enterSection(this); /* Festhalten der Werte für Warte-, Transfer- und Bedienzeit erfolgt hier. */

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Section"),String.format(Language.tr("Simulation.Log.Section.Enter"),name,client.logInfo(simData)));

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0,false); /* false = Statistik nicht per logStationLeave erfassen */
	}

	/**
	 * Wird von {@link RunDataClient#leaveSection(RunElementSectionStart, SimulationData)} und
	 * von {@link RunDataClient#leaveAllSections(SimulationData)} aufgerufen, wenn ein Kunde
	 * die aktuelle oder alle Abschnitte verlässt.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde, der diesen Abschnitt verlässt
	 */
	public void notifyClientLeavesSection(final SimulationData simData, final RunDataClient client) {
		/* Jetzt erst aus Statistik für diese Station austragen */
		simData.runData.logClientLeavesStation(simData,this,null);

		/* Zeitdauern für Station (=Bereich) erfassen */
		Long L;
		L=client.sectionEnterWaitingTime.remove(this);
		final long waiting=client.waitingTime-((L!=null)?L.longValue():0);
		L=client.sectionEnterTransferTime.remove(this);
		final long transfer=client.transferTime-((L!=null)?L.longValue():0);
		L=client.sectionEnterProcessTime.remove(this);
		final long process=client.processTime-((L!=null)?L.longValue():0);
		L=client.sectionEnterResidenceTime.remove(this);
		final long residence=client.residenceTime-((L!=null)?L.longValue():0);

		simData.runData.logStationProcess(simData,this,client,waiting,transfer,process,residence);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Section"),String.format(Language.tr("Simulation.Log.Section.Leave"),name,client.logInfo(simData)));
	}
}
