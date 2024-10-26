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
import java.awt.Dimension;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.swing.JDistributionEditorDialog;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionFitter;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import tools.SetupData;
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

	/**
	 * Auswahlbox für die angepasste Verteilung
	 */
	private JComboBox<DistributionFitter.Fit> outputSelect;

	/**
	 * Datenmodell für die Auswahlbox für die angepasste Verteilung
	 * @see #outputSelect
	 */
	private DefaultComboBoxModel<DistributionFitter.Fit> outputSelectModel;

	/**
	 * Anzeige der gewählten angepassten Verteilung
	 */
	private JDistributionPanel outputDistribution;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public FitDialog(final Component owner) {
		super(owner,Language.tr("FitDialog.Title"),"Fit",InfoPanel.globalFit,true,true);
	}

	@Override
	protected JPanel outputDistributionPanel() {
		final JPanel panel=new JPanel(new BorderLayout());
		final JPanel line=new JPanel(new BorderLayout());
		panel.add(line,BorderLayout.NORTH);
		line.add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.NORTH);
		line.add(Box.createRigidArea(new Dimension(7,7)),BorderLayout.EAST);
		line.add(Box.createRigidArea(new Dimension(7,7)),BorderLayout.WEST);
		line.add(outputSelect=new JComboBox<>(outputSelectModel=new DefaultComboBoxModel<>()),BorderLayout.CENTER);
		outputSelect.setRenderer(new FitRenderer());
		outputSelect.addActionListener(e->selectOutput());
		outputSelect.setMaximumRowCount(5);

		panel.add(outputDistribution=new JDistributionPanel(new DataDistributionImpl(10,10),10,false) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=4490740071418221920L;

			@Override
			protected double getRealMaxXValue() {
				return inputValuesMax;
			}
		},BorderLayout.CENTER);
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

		outputSelectModel.removeAllElements();
		outputSelectModel.addAll(fitter.getSortedFits());
		outputSelect.setSelectedIndex(0);
		selectOutput();

		outputDistribution.setMaxXValue(inputValuesMax);
	}

	@Override
	protected double[] generateSampleValues() {
		final JDistributionEditorDialog dialog=new JDistributionEditorDialog(this,new LogNormalDistributionImpl(100,30),1000,JDistributionPanel.BOTH,true,true,SetupData.getSetup().imageSize);
		dialog.setVisible(true);
		final AbstractRealDistribution dist=dialog.getNewDistribution();
		if (dist==null) return null;

		final double[] result=new double[1_000_000];
		for (int i=0;i<result.length;i++) result[i]=DistributionRandomNumber.random(dist);
		return result;
	}

	/**
	 * Wird aufgerufen, wenn in {@link #outputSelect} eine andere Verteilung
	 * ausgwählt wird, um {@link #outputDistribution} zu aktualisieren.
	 */
	private void selectOutput() {
		final var fit=(DistributionFitter.Fit)outputSelect.getSelectedItem();
		outputDistribution.setDistribution((fit==null)?null:fit.distribution);
	}

	/**
	 * Renderer für {@link FitDialog#outputSelect}
	 */
	private class FitRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-7146981632299189728L;

		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			final StringBuilder text=new StringBuilder();
			text.append("<html><body>");
			if (value instanceof DistributionFitter.Fit) {
				final DistributionFitter.Fit fit=(DistributionFitter.Fit)value;
				final String name=DistributionTools.getDistributionName(fit.distribution);
				index=outputSelectModel.getIndexOf(value);
				text.append((index+1)+". ");
				text.append("<b>"+name+"</b>");
				text.append("<br>");
				text.append(DistributionTools.getDistributionInfo(fit.distribution));
				text.append("<br>");
				String diffStr=NumberTools.formatNumber(fit.fit,3);
				if (diffStr.equals("0")) diffStr=NumberTools.formatNumber(fit.fit,9);
				text.append(DistributionFitter.MeanSquares+"="+diffStr);
				final ImageIcon image=DistributionTools.getThumbnailImageForDistributionName(name);
				setIcon(image);
			} else {
				setIcon(null);
			}
			text.append("</body></html>");

			setText(text.toString());
			return this;
		}
	}
}