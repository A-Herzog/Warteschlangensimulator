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
import java.io.Serializable;
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
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementProcess;

/**
 * Dieser Dialog ermöglicht es, nach der Durchführung einer Simulation
 * zur Bestimmung der Autokorrelation der Wartezeiten auf dieser Basis
 * eine Batch-Größe für die Batch-Means Methode auszuwählen.
 * @author Alexander Herzog
 */
public class FindBatchSizeDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1353952651319816828L;

	/** Sinnvolle mögliche Restabhängigkeiten */
	private static final double[] LEVELS=new double[]{0.2,0.1,0.05,0.01,0.005,0.001};
	/** Standardmäßig zulässige Restabhängigkeit */
	private static final double DEFAULT_LEVEL=0.05;

	/** Radiobuttons zur Auswahl der Batchgrößen */
	private final List<JRadioButton> radioButtons;
	/** Batchgrößen */
	private final List<Integer> distances;
	/** Batchgröße für {@link #DEFAULT_LEVEL} */
	private int defaultDistance;
	/** Direkt nach Schließen des Dialogs einen Simulationslauf starten? */
	private final JCheckBox runNow;

	/**
	 * Konstruktor der Klasse<br>
	 * Erstellt den Dialog, zeigt ihn aber noch nicht an.
	 * @param owner	Übergeordnetes Element
	 * @param statistics	Statistik-Objekt aus dem die Autokorrelationsdaten entnommen werden sollen.
	 */
	public FindBatchSizeDialog(final Component owner, final Statistics statistics) {
		super(owner,Language.tr("FindBatchSizeSimulation.Title"));

		final JPanel content=createGUI(()->Help.topicModal(this,"FindBatchSize"));
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

	/**
	 * Liefert ein Modellelement basierend auf dem in den Statistikdaten
	 * eingebetteten Modell und dem Namen der Station
	 * @param statistics	Statistikdaten
	 * @param name	Name der Station (muss "(id=...)" enthalten)
	 * @return	Modellelement oder <code>null</code>, wenn kein Modellelement mit dem entsprechenden Namen gefunden werden konnte
	 * @see #getDistance(Statistics, double)
	 */
	private ModelElement getElementFromStatisticName(final Statistics statistics, String name) {
		if (statistics==null) return null;
		final EditModel model=statistics.editModel;
		if (model==null) return null;

		final int index1=name.indexOf("(id=");
		if (index1<0) return null;
		name=name.substring(index1+4);

		final int index2=name.indexOf(")");
		if (index2<0) return null;
		name=name.substring(0,index2);

		final Integer I=NumberTools.getInteger(name);
		if (I==null) return null;

		return model.surface.getByIdIncludingSubModels(I.intValue());
	}

	/**
	 * Bestimmt den notwendigen Abstand für ein bestimmte maximales Rest-Autokorrelations-Level
	 * @param statistics	Statistik-Quelldaten
	 * @param level	Maximal zulässiges Rest-Autokorrelations-Level
	 * @return	Liefert die notwendige Distanz
	 */
	private int getDistance(final Statistics statistics, final double level) {
		if (!statistics.clientsAllWaitingTimes.isCorrelationAvailable()) return 10;

		int max=statistics.clientsAllWaitingTimes.getCorrelationLevelDistance(level);

		for (String name: statistics.clientsWaitingTimes.getNames()) {
			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name);
			if (indicator.isCorrelationAvailable()) max=Math.max(max,indicator.getCorrelationLevelDistance(level));
		}

		for (String name: statistics.stationsWaitingTimes.getNames()) {
			final ModelElement element=getElementFromStatisticName(statistics,name);
			if (!(element instanceof ModelElementProcess)) continue;

			final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name);
			if (indicator.isCorrelationAvailable()) max=Math.max(max,indicator.getCorrelationLevelDistance(level));
		}

		for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
			final ModelElement element=getElementFromStatisticName(statistics,name);
			if (!(element instanceof ModelElementProcess)) continue;

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