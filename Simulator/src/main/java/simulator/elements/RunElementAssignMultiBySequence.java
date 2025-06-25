/**
 * Copyright 2025 Alexander Herzog
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

import java.util.List;

import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementAssignMulti;

/**
 * Äquivalent zu {@link ModelElementAssignMulti} (im Modus <code>MODE_SEQUENCE</code>)
 * @author Alexander Herzog
 * @see ModelElementAssignMulti
 */
public class RunElementAssignMultiBySequence extends RunElementAssignMultiBase {
	/** Liste der Kundentypen, die angibt, welcher Kundentyp der nächste Kunde zugewiesen werden sollen */
	private int[] clientTypeConnectionIndex;

	/**
	 * Konstruktor der Klasse
	 * @param element Mehrfach-Typzuweisungs-Station zu diesem Datenelement
	 */
	public RunElementAssignMultiBySequence(ModelElementAssignMulti element) {
		super(element,DecideRecord.DecideMode.MODE_SEQUENCE);
	}

	@Override
	protected String buildDecideData(EditModel editModel, RunModel runModel, ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		final List<Integer> edgesMultiplicity=record.getMultiplicity();
		clientTypeConnectionIndex=new int[edgesMultiplicity.stream().mapToInt(Integer::intValue).sum()];

		int nr=0;
		for (int i=0;i<edgesMultiplicity.size();i++) {
			final int count=edgesMultiplicity.get(i);
			for (int j=0;j<count;j++) clientTypeConnectionIndex[nr++]=i;
		}

		return null;
	}

	@Override
	protected RunModelCreatorStatus testDecideData(ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementAssignMultiBySequenceData getData(final SimulationData simData) {
		RunElementAssignMultiBySequenceData data;
		data=(RunElementAssignMultiBySequenceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAssignMultiBySequenceData(this,condition,simData.runModel.variableNames,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	protected int getOptionIndex(SimulationData simData, RunDataClient client, RunElementAssignMultiBaseData data, int decideCount) {
		final RunElementAssignMultiBySequenceData dataBySequence=(RunElementAssignMultiBySequenceData)data;

		final int nr=dataBySequence.nextNr;
		dataBySequence.nextNr++;
		if (dataBySequence.nextNr>=clientTypeConnectionIndex.length) dataBySequence.nextNr=0;

		return clientTypeConnectionIndex[nr];
	}
}
