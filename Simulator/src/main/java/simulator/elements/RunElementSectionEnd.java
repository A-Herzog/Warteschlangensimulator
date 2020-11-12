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
import ui.modeleditor.elements.ModelElementSectionEnd;
import ui.modeleditor.elements.ModelElementSectionStart;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSectionEnd</code>
 * @author Alexander Herzog
 * @see ModelElementSectionEnd
 */
public class RunElementSectionEnd extends RunElementPassThrough {
	/** ID der zugehörigen "Bereich betreten"-Station */
	private int sectionID;
	/** Zugehörige "Bereich betreten"-Station (aus {@link #sectionID} übersetzt) */
	private RunElementSectionStart section;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSectionEnd(final ModelElementSectionEnd element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SectionEnd.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSectionEnd)) return null;
		final ModelElementSectionEnd sectionEndElement=(ModelElementSectionEnd)element;
		final RunElementSectionEnd sectionEnd=new RunElementSectionEnd(sectionEndElement);

		/* Auslaufende Kante */
		final String edgeError=sectionEnd.buildEdgeOut(sectionEndElement);
		if (edgeError!=null) return edgeError;

		/* Bereich */
		final String sectionName=sectionEndElement.getSectionStartName();
		if (sectionName.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoSectionName"),element.getId());

		sectionEnd.sectionID=-1;
		for (ModelElement e1 : editModel.surface.getElements()) {
			if (e1 instanceof ModelElementSectionStart && e1.getName().equals(sectionName)) {sectionEnd.sectionID=e1.getId(); break;}
			if (e1 instanceof ModelElementSub) {
				for (ModelElement e2 : editModel.surface.getElements()) {
					if (e2 instanceof ModelElementSectionStart && e2.getName().equals(sectionName)) {sectionEnd.sectionID=e2.getId(); break;}
				}
				if (sectionEnd.sectionID>=0) break;
			}

		}
		if (sectionEnd.sectionID<0) return String.format(Language.tr("Simulation.Creator.InvalidSection"),element.getId(),sectionName);

		return sectionEnd;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSectionEnd)) return null;
		final ModelElementSectionEnd sectionEndElement=(ModelElementSectionEnd)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(sectionEndElement);
		if (edgeError!=null) return edgeError;

		/* Bereich */
		final String sectionName=sectionEndElement.getSectionStartName();
		if (sectionName.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoSectionName"),element.getId()),RunModelCreatorStatus.Status.NO_SECTION_NAME);

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSectionEndData getData(final SimulationData simData) {
		RunElementSectionEndData data;
		data=(RunElementSectionEndData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSectionEndData(this);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Zugehörige Bereich-Start-Station verknüpfen */
		if (section==null) section=(sectionID>=0)?(RunElementSectionStart)simData.runModel.elementsFast[sectionID]:null;

		/* Kunde aus Bereich austragen */
		client.leaveSection(section,simData);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}

	/**
	 * Liefert die Anzahl an Kunden, die sich in dem gesamten Bereich befinden
	 * @param simData	Simulationsdatenobjekt
	 * @return	Anzahl an Kunden, die sich in dem gesamten Bereich befinden
	 */
	public int clientsInSection(final SimulationData simData) {
		/* Zugehörige Bereich-Start-Station verknüpfen */
		if (section==null) section=(sectionID>=0)?(RunElementSectionStart)simData.runModel.elementsFast[sectionID]:null;
		if (section==null) return 0;

		/* Anzahl in Ende = Anzahl in Start (bzw. Anzahl wird in Start-Element geführt) */
		return section.getData(simData).reportedClientsAtStation(simData);
	}
}
