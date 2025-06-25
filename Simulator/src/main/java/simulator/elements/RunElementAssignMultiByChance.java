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
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementAssignMulti;

/**
 * Äquivalent zu {@link ModelElementAssignMulti} (im Modus <code>MODE_CHANCE</code>)
 * @author Alexander Herzog
 * @see ModelElementAssignMulti
 */
public class RunElementAssignMultiByChance extends RunElementAssignMultiBase {
	/** Wahrscheinlichkeiten für die verschiedenen auslaufenden Kanten */
	private double[] probabilites;
	/** Rechenausdrücke für die Raten für die verschiedenen auslaufenden Kanten */
	private String[] probabilitesStrings;

	/**
	 * Konstruktor der Klasse
	 * @param element Mehrfach-Typzuweisungs-Station zu diesem Datenelement
	 */
	public RunElementAssignMultiByChance(ModelElementAssignMulti element) {
		super(element,DecideRecord.DecideMode.MODE_CHANCE);
	}

	@Override
	protected String buildDecideData(final EditModel editModel, final RunModel runModel, final ModelElementAssignMulti element, final DecideRecord record, final int decideCount) {
		double sum=0;

		probabilites=new double[decideCount];
		probabilitesStrings=new String[decideCount];
		final List<String> editRates=record.getRates();

		for (int i=0;i<decideCount;i++) {
			final String rate=(i>=editRates.size())?"1":editRates.get(i);
			probabilitesStrings[i]=rate;
			final Double D=NumberTools.getPlainDouble(rate);
			if (D==null || sum==-1) {
				sum=-1;
				probabilites=null;
			} else {
				probabilites[i]=Math.max(0,D);
				sum+=Math.max(0,D);
			}
		}

		if (probabilites==null) {
			for (int i=0;i<probabilitesStrings.length;i++) {
				final int error=ExpressionCalc.check(probabilitesStrings[i],runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DecideRate"),i+1,probabilitesStrings[i],element.getId(),error+1);
			}
		} else {
			probabilitesStrings=null;
			if (sum==0) return String.format(Language.tr("Simulation.Creator.NoDecideByChanceRates"),element.getId());
			for (int i=0;i<probabilites.length;i++) probabilites[i]=probabilites[i]/sum;
		}

		return null;
	}

	@Override
	protected RunModelCreatorStatus testDecideData(final ModelElementAssignMulti element, final DecideRecord record, final int decideCount) {
		double sum=0;
		final List<String> editRates=record.getRates();
		for (int i=0;i<decideCount;i++) {
			final String rateString=(i>=editRates.size())?"1":editRates.get(i);
			if (sum>=0) {
				Double D=NumberTools.getDouble(rateString);
				if (sum!=-1 || D==null) sum=-1; else sum+=Math.max(0,D);
			}
		}
		if (sum==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoDecideByChanceRates"),element.getId()));

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementAssignMultiByChanceData getData(final SimulationData simData) {
		RunElementAssignMultiByChanceData data;
		data=(RunElementAssignMultiByChanceData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAssignMultiByChanceData(this,condition,simData.runModel.variableNames,simData,probabilitesStrings);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	protected int getOptionIndex(SimulationData simData, RunDataClient client, RunElementAssignMultiBaseData data, int decideCount) {
		final RunElementAssignMultiByChanceData dataByChance=(RunElementAssignMultiByChanceData)data;

		int nr=-1;
		if (probabilites==null) {
			/* Rechenausdrücke auswerten */
			simData.runData.setClientVariableValues(client);
			nr=dataByChance.getIndex(simData);
			if (nr<0) nr=probabilitesStrings.length-1;
		} else {
			/* Einfache Wahrscheinlichkeiten */
			final double rnd=simData.runData.random.nextDouble();
			double sum=0;
			for (int i=0;i<probabilites.length;i++) {
				sum+=probabilites[i];
				if (sum>=rnd) {nr=i; break;}
			}
			if (nr<0) nr=probabilites.length-1;
		}

		return nr;
	}
}
