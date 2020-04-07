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
import java.util.List;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines <code>RunElementSectionStart</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSectionStart
 * @see RunElementData
 */
public class RunElementSectionStartData extends RunElementData {
	private RunElementSectionStartData[] moreSectionStartStations;

	/**
	 * Konstruktor der Klasse <code>RunElementSectionStartData</code>
	 * @param station	Station zu diesem Datenelement
	 */
	public RunElementSectionStartData(final RunElementSectionStart station) {
		super(station);
	}

	@Override
	public int reportedClientsAtStation(final SimulationData simData) {
		/* Alle weiteren Section-Start-Station mit demselben Namen bestimmen */
		if (moreSectionStartStations==null) {
			final String nameLower=((RunElementSectionStart)station).sectionNameLower;
			final List<RunElementSectionStartData> list=new ArrayList<>();
			for (int i=0;i<simData.runModel.elementsFast.length;i++) {
				final RunElement element=simData.runModel.elementsFast[i];
				if (element instanceof RunElementSectionStart && element!=station) {
					if (((RunElementSectionStart)element).sectionNameLower.equals(nameLower)) list.add(((RunElementSectionStart)element).getData(simData));
				}
			}
			moreSectionStartStations=list.toArray(new RunElementSectionStartData[0]);
		}

		/* Kunden, die über die Station selbst und über alle anderen Eingänge in denselben Abschnitt eingetreten sind. */
		int sum=clientsAtStation;
		for (RunElementSectionStartData otherStation: moreSectionStartStations) sum+=otherStation.clientsAtStation;
		return sum;
	}
}
