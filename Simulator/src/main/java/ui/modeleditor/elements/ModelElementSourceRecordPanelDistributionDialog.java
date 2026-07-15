/**
 * Copyright 2026 Alexander Herzog
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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import mathtools.distribution.swing.JDataDistributionEditPanel.PlotMode;
import systemtools.BaseDialog;

/**
 * Zeit die Anzahl an Ankünften pro Intervall in einem Dialog an.
 * @see ModelElementSourceRecordPanel
 */
public class ModelElementSourceRecordPanelDistributionDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8293306591832243079L;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element
	 * @param values	Anzuzeigende Anzahlen pro Intervall
	 */
	public ModelElementSourceRecordPanelDistributionDialog(final Component owner, final double[] values) {
		super(owner,Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.ShowDistribution.DialogTitle"));

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		final var distribution=new DataDistributionImpl(values.length-1,values,true);
		final var plotter=new JDataDistributionEditPanel(distribution,PlotMode.PLOT_DENSITY);
		plotter.setUpperBoundAdd(1);

		content.add(plotter,BorderLayout.CENTER);

		/* Dialog starten */
		setMinSizeRespectingScreensize(1000,600);
		pack();
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}
}
