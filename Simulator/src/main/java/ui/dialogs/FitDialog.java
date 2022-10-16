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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import language.Language;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitter;
import ui.infopanel.InfoPanel;

/**
 * Diese Klasse stellt einen Dialog zur Anpassung einer Verteilung
 * an aufgezeichnete Messwerte zur Verfügung.
 * @author Alexander Herzog
 * @see DistributionFitter
 */
public class FitDialog extends FitDialogBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8263152374892311273L;

	/** Am besten passende Verteilung */
	private JDistributionPanel outputDistribution;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public FitDialog(final Component owner) {
		super(owner,Language.tr("FitDialog.Title"),"Fit",InfoPanel.globalFit);
	}

	@Override
	protected JPanel outputDistributionPanel() {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(outputDistribution=new JDistributionPanel(new DataDistributionImpl(10,10),10,false),BorderLayout.CENTER);
		outputDistribution.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		return panel;
	}

	/**
	 * Führt die Verteilungsanpassung durch.
	 */
	@Override
	protected void calcFit() {
		final DistributionFitter fitter=new DistributionFitter();
		calcFitIntern(fitter);

		outputDistribution.setMaxXValue(inputValuesMax);
		outputDistribution.setDistribution(fitter.getFitDistribution().get(0));
	}
}