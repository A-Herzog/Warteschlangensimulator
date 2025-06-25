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
 * Äquivalent zu {@link ModelElementAssignMulti} (im Modus <code>MODE_CLIENTTYPE</code>)
 * @author Alexander Herzog
 * @see ModelElementAssignMulti
 */
public class RunElementAssignMultiByClientType extends RunElementAssignMultiBase {
	/** Liste der Kundentypen, die angibt, welcher Kundentyp zu welchem Kundentyp zugewiesen werden sollen */
	private int[] clientTypeConnectionIndex;

	/**
	 * Konstruktor der Klasse
	 * @param element Mehrfach-Typzuweisungs-Station zu diesem Datenelement
	 */
	public RunElementAssignMultiByClientType(ModelElementAssignMulti element) {
		super(element,DecideRecord.DecideMode.MODE_CLIENTTYPE);
	}

	@Override
	protected String buildDecideData(EditModel editModel, RunModel runModel, ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		/* Array der Verbindungs-Indices erstellen und erstmal alle Kundentypen auf die letzte "Sonst"-Ecke einstellen */
		clientTypeConnectionIndex=new int[runModel.clientTypes.length];
		for (int i=0;i<clientTypeConnectionIndex.length;i++) clientTypeConnectionIndex[i]=decideCount-1;

		/* Pro Kundentyp korrekte Nummer der Ausgangskante (Index in der Liste, nicht ID) bestimmen */
		final List<List<String>> clientTypes=record.getClientTypes();
		for (int i=0;i<Math.min(decideCount,clientTypes.size());i++) for (int j=0;j<clientTypes.get(i).size();j++) {
			int index=runModel.getClientTypeNr(clientTypes.get(i).get(j));
			/*
			if (index<0) return String.format(Language.tr("Simulation.Creator.NoClientTypeInDecide"),element.getId(),clientTypes.get(i),i+1);
			Kanten mit Kundentypen, die es nicht gibt (=die z.B. temporär deaktiviert wurden) einfach ignorieren:
			 */
			if (index>=0) clientTypeConnectionIndex[index]=i;
		}

		return null;
	}

	@Override
	protected RunModelCreatorStatus testDecideData(ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		return RunModelCreatorStatus.ok;
	}

	@Override
	protected int getOptionIndex(SimulationData simData, RunDataClient client, RunElementAssignMultiBaseData data, int decideCount) {
		return clientTypeConnectionIndex[client.type];
	}
}
