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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import language.Language;
import systemtools.SmallColorChooser;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationPieChart}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationPieChart
 */
public class ModelElementAnimationPieChartDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-893526302857552173L;

	/** Auswahlbox für ide Art der Beschriftung */
	private JComboBox<String> labelMode;
	/** Tabelle zur Definition der Diagrammsegmente */
	private ExpressionTableModelBar expressionTableModel;
	/** Auswahlbox für die Rahmenbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Rahmenfarbe */
	private SmallColorChooser colorChooserLine;
	/** Option: Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationPieChart}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationPieChartDialog(final Component owner, final ModelElementAnimationPieChart element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationPieChart.Dialog.Title"),element,"ModelElementAnimationPieChart",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationPieChart;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel content, lines, line, cell;
		JLabel label;
		Object[] data;

		/* Daten: Beschriftung und Diagrammreihen */
		tabs.addTab(Language.tr("Surface.AnimationPieChart.Dialog.Data"),content=new JPanel(new BorderLayout()));

		content.add(lines=new JPanel(),BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));
		lines.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationPieChart.Dialog.LabelMode")+":"));
		line.add(labelMode=new JComboBox<>(new String[] {
				Language.tr("Surface.AnimationPieChart.Dialog.LabelMode.Off"),
				Language.tr("Surface.AnimationPieChart.Dialog.LabelMode.Big"),
				Language.tr("Surface.AnimationPieChart.Dialog.LabelMode.On")
		}));
		label.setLabelFor(labelMode);
		labelMode.setEnabled(!readOnly);

		final JTableExt expressionTable;
		content.add(new JScrollPane(expressionTable=new JTableExt()),BorderLayout.CENTER);
		if (element instanceof ModelElementAnimationPieChart) {
			expressionTable.setModel(expressionTableModel=new ExpressionTableModelBar(expressionTable,(ModelElementAnimationPieChart)element,readOnly,helpRunnable));
		}
		expressionTable.getColumnModel().getColumn(1).setMaxWidth(100);
		expressionTable.getColumnModel().getColumn(1).setMinWidth(100);
		expressionTable.setIsPanelCellTable(0);
		expressionTable.setIsPanelCellTable(1);
		expressionTable.setEnabled(!readOnly);

		/* Darstellung: Farben und Linienbreiten */
		tabs.addTab(Language.tr("Surface.AnimationPieChart.Dialog.Appearance"),content=new JPanel(new BorderLayout()));

		data=getLineWidthInputPanel(Language.tr("Surface.AnimationPieChart.Dialog.Appearance.FrameWidth")+":",0,15,5);
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationPieChart.Dialog.Appearance.FrameColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserLine=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserLine.setEnabled(!readOnly);
		label.setLabelFor(colorChooserLine);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(background=new JCheckBox(Language.tr("Surface.AnimationPieChart.Dialog.Appearance.FillBackground")),BorderLayout.NORTH);
		background.setEnabled(!readOnly);
		cell.add(colorChooserBackground=new SmallColorChooser(Color.WHITE),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,background.getPreferredSize().height));

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		/* Daten laden */
		if (element instanceof ModelElementAnimationPieChart) {
			final ModelElementAnimationPieChart diagram=(ModelElementAnimationPieChart)element;

			switch (diagram.getLabelMode()) {
			case NO_LABELS: labelMode.setSelectedIndex(0); break;
			case BIG_PARTS: labelMode.setSelectedIndex(1); break;
			case ALL_PARTS: labelMode.setSelectedIndex(2); break;
			default: labelMode.setSelectedIndex(1); break;
			}

			lineWidth.setSelectedIndex(diagram.getBorderWidth());
			colorChooserLine.setColor(diagram.getBorderColor());
			background.setSelected(diagram.getBackgroundColor()!=null);
			colorChooserBackground.setColor(diagram.getBackgroundColor());
		}

		return tabs;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,800);
		setResizable(true);
		pack();
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationPieChart) {
			final ModelElementAnimationPieChart diagram=(ModelElementAnimationPieChart)element;

			switch (labelMode.getSelectedIndex()) {
			case 0: diagram.setLabelMode(ModelElementAnimationPieChart.LabelMode.NO_LABELS); break;
			case 1: diagram.setLabelMode(ModelElementAnimationPieChart.LabelMode.BIG_PARTS); break;
			case 2: diagram.setLabelMode(ModelElementAnimationPieChart.LabelMode.ALL_PARTS); break;
			}

			expressionTableModel.storeData(diagram);

			diagram.setBorderWidth(lineWidth.getSelectedIndex());
			diagram.setBorderColor(colorChooserLine.getColor());
			if (background.isSelected()) {
				diagram.setBackgroundColor(colorChooserBackground.getColor());
			} else {
				diagram.setBackgroundColor(null);
			}
		}
	}
}
