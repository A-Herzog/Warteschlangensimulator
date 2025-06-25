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

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementAssignMulti;

/**
 * Äquivalent zu {@link ModelElementAssignMulti} (im Modus <code>MODE_CONDITION</code>)
 * @author Alexander Herzog
 * @see ModelElementAssignMulti
 */
public class RunElementAssignMultiByCondition extends RunElementAssignMultiBase {
	/** Bedingungen, die erfüllt sein müssen, damit ein Kunde an einen bestimmten Ausgang geleitet wird (<code>null</code>-Einträge bedeuten: immer erfüllt) */
	private String[] conditions;

	/**
	 * Konstruktor der Klasse
	 * @param element Mehrfach-Typzuweisungs-Station zu diesem Datenelement
	 */
	public RunElementAssignMultiByCondition(ModelElementAssignMulti element) {
		super(element,DecideRecord.DecideMode.MODE_CONDITION);
	}

	@Override
	protected String buildDecideData(EditModel editModel, RunModel runModel, ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		conditions=new String[decideCount];

		final List<String> editConditions=record.getConditions();
		for (int i=0;i<decideCount;i++) {

			if (i<decideCount-1) {
				String condition=(i>=editConditions.size())?"":editConditions.get(i);
				if (condition==null) condition="";
				final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DecideCondition"),i+1,condition,element.getId(),error+1);
				conditions[i]=condition;
			}
		}

		return null;
	}

	@Override
	protected RunModelCreatorStatus testDecideData(ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementAssignMultiByConditionData getData(final SimulationData simData) {
		RunElementAssignMultiByConditionData data;
		data=(RunElementAssignMultiByConditionData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAssignMultiByConditionData(this,condition,simData.runModel.variableNames,simData,conditions);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	protected int getOptionIndex(SimulationData simData, RunDataClient client, RunElementAssignMultiBaseData data, int decideCount) {
		final RunElementAssignMultiByConditionData dataByCondition=(RunElementAssignMultiByConditionData)data;

		int nr=-1;
		simData.runData.setClientVariableValues(client);
		for (int i=0;i<dataByCondition.conditions.length;i++) if (dataByCondition.conditions[i]==null || dataByCondition.conditions[i].eval(simData.runData.variableValues,simData,client)) {nr=i; break;}
		if (nr==-1) nr=dataByCondition.conditions.length; /* Nicht: length-1, denn conditions sind bereits eine weniger als Ausgänge */

		return nr;
	}
}
