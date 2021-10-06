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

import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsLongRunPerformanceIndicator;
import ui.modeleditor.ModelLongRunStatistics;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zu diesem Laufzeitelement gibt es kein Editor-Äquivalent.
 * Es dient als Listener zur Aufzeichnung von Laufzeitstatistiken.
 * @author Alexander Herzog
 * @see ModelLongRunStatistics
 */
public class RunElementLongRunStatistics extends RunElement implements StateChangeListener {
	/**
	 * Auszuwertende Ausdrücke
	 */
	private String[] expressions;

	/**
	 * Modi für die Erfassung der einzelnen Werte in {@link #expressions}
	 */
	private StatisticsLongRunPerformanceIndicator.Mode[] modes;

	/**
	 * Erfassungsschrittweite in Millisekunden
	 */
	private long stepWideMS;

	/**
	 * Sollen zum Simulationsende letzte Intervalle abgeschlossen werden?
	 */
	private boolean closeLastInterval;

	/**
	 * Konstruktor der Klasse
	 * @param id	Pseudo-ID des Elements; es wird hier üblicherweise <code>editModel.surface.getMaxId()+1</code> verwendet
	 */
	public RunElementLongRunStatistics(final int id) {
		super(id,"SpecialStatistics");
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		final List<ModelLongRunStatisticsElement> list=editModel.longRunStatistics.getData();

		final RunElementLongRunStatistics run=new RunElementLongRunStatistics(editModel.surface.getMaxId()+1);
		run.expressions=new String[list.size()];
		run.modes=new StatisticsLongRunPerformanceIndicator.Mode[list.size()];

		for (int i=0;i<list.size();i++) {
			run.expressions[i]=list.get(i).expression;
			final int error=ExpressionCalc.check(run.expressions[i],runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.SetInvalidExpression"),i+1,run.id,error+1);
			run.modes[i]=list.get(i).mode;
		}

		run.stepWideMS=editModel.longRunStatistics.getStepWideSec()*1000;
		run.closeLastInterval=editModel.longRunStatistics.isCloseLastInterval();

		return run;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		/* tritt nicht auf */
		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementLongRunStatisticsData getData(final SimulationData simData) {
		RunElementLongRunStatisticsData data;
		data=(RunElementLongRunStatisticsData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementLongRunStatisticsData(this,expressions,modes,stepWideMS,closeLastInterval,simData.runModel);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* tritt nicht auf */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* tritt nicht auf */
	}

	@Override
	public boolean systemStateChangeNotify(SimulationData simData) {
		final RunElementLongRunStatisticsData data=getData(simData);
		data.process(simData);
		return false;
	}
}
