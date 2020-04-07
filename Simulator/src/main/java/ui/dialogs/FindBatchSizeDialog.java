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
package ui.dialogs;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import language.Language;
import mathtools.NumberTools;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.BaseDialog;
import ui.help.Help;

/**
 * Dieser Dialog ermöglicht es, nach der Durchführung einer Simulation
 * zur Bestimmung der Autokorrelation der Wartezeiten auf dieser Basis
 * eine Batch-Größe für die Batch-Means Methode auszuwählen.
 * @author Alexander Herzog
 */
public class FindBatchSizeDialog extends BaseDialog {
	private static final long serialVersionUID = 1353952651319816828L;

	private final static double[] LEVELS=new double[]{0.2,0.1,0.05,0.01,0.005,0.001};
	private final static double DEFAULT_LEVEL=0.05;

	private final List<JRadioButton> radioButtons;
	private final List<Integer> distances;
	private int defaultDistance;
	private final JCheckBox runNow;

	/**
	 * Konstruktor der Klasse<br>
	 * Erstellt den Dialog, zeigt ihn aber noch nicht an.
	 * @param owner	Übergeordnetes Element
	 * @param statistics	Statistik-Objekt aus dem die Autokorrelationsdaten entnommen werden sollen.
	 */
	public FindBatchSizeDialog(final Component owner, final Statistics statistics) {
		super(owner,Language.tr("FindBatchSizeSimulation.Title"));

		final JPanel content=createGUI(()->{Help.topicModal(this,"FindBatchSize");});
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		JPanel line;
		final ButtonGroup buttonGroup=new ButtonGroup();

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("FindBatchSizeSimulation.SelectLabel")));

		radioButtons=new ArrayList<>();
		distances=new  ArrayList<>();
		defaultDistance=10;
		for (double level: LEVELS) {
			int distance=getDistance(statistics,level);
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			final JRadioButton radioButton=new JRadioButton(String.format(Language.tr("FindBatchSizeSimulation.SelectEntry"),NumberTools.formatPercent(level),NumberTools.formatLong(distance)));
			line.add(radioButton);
			buttonGroup.add(radioButton);
			radioButtons.add(radioButton);
			distances.add(distance);
			radioButton.setSelected(level==DEFAULT_LEVEL);
			if (level==DEFAULT_LEVEL) defaultDistance=distance;
		}

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(runNow=new JCheckBox(Language.tr("FindBatchSizeSimulation.RunSimulationNow"),true));

		pack();
		setLocationRelativeTo(this.owner);
	}

	private int getDistance(final Statistics statistics, final double level) {
		if (!statistics.clientsAllWaitingTimes.isCorrelationAvailable()) return 10;

		int max=statistics.clientsAllWaitingTimes.getCorrelationLevelDistance(level);

		for (String name: statistics.clientsWaitingTimes.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name);
			if (indicator.isCorrelationAvailable()) max=Math.max(max,indicator.getCorrelationLevelDistance(level));
		}

		for (String name: statistics.stationsWaitingTimes.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name);
			if (indicator.isCorrelationAvailable()) max=Math.max(max,indicator.getCorrelationLevelDistance(level));
		}

		for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimesByClientType.get(name);
			if (indicator.isCorrelationAvailable()) max=Math.max(max,indicator.getCorrelationLevelDistance(level));
		}

		return max;
	}

	/**
	 * Liefert, wenn der Dialog mit "Ok" geschlossen wurde die
	 * zu der erlaubten Rest-Korrelation zugehörige Batch-Größe zurück.
	 * @return	Einzustellende Batch-Größe (oder -1, wenn der Dialog nicht per "Ok" geschlossen wurde)
	 */
	public int getBatchSize() {
		if (getClosedBy()!=CLOSED_BY_OK) return 1;
		for (int i=0;i<radioButtons.size();i++) if (radioButtons.get(i).isSelected()) return distances.get(i);
		return defaultDistance;
	}

	/**
	 * Gibt an, ob der Nutzer ausgewählt hat, dass mit den veränderten Daten sofort ein neuer Simulationslauf gestartet werden soll.
	 * @return	Neuen Simulationslauf mit geändertem Modell jetzt starten?
	 */
	public boolean runSimulationNow() {
		return runNow.isSelected();
	}
}