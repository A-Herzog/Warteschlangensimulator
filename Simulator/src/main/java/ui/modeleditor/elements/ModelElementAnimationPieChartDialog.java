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
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import language.Language;
import systemtools.LabeledColorChooserButton;
import systemtools.OptionalColorChooserButton;
import tools.IconListCellRenderer;
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

	/** Auswahlbox für die Art der Beschriftung */
	private JComboBox<String> labelMode;
	/** Tabelle zur Definition der Diagrammsegmente */
	private ExpressionTableModelBar expressionTableModel;
	/** Darstellungsart */
	private JComboBox<String> drawMode;
	/** Auswahlbox für die Rahmenbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Rahmenfarbe */
	private LabeledColorChooserButton colorChooserLine;
	/** Auswahl der Hintergrundfarbe */
	private OptionalColorChooserButton colorChooserBackground;
	/** Auswahl der Farbe für den Farbverlauf */
	private OptionalColorChooserButton colorChooserGradient;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationPieChart}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationPieChartDialog(final Component owner, final ModelElementAnimationPieChart element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
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

		JPanel content, lines, line;
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
		content.add(lines=new JPanel(),BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		data=getComboBoxPanel(Language.tr("Surface.AnimationPieChart.Dialog.DiagrameType")+":",Arrays.asList(
				Language.tr("Surface.AnimationPieChart.Dialog.DiagrameType.Pie"),
				Language.tr("Surface.AnimationPieChart.Dialog.DiagrameType.Donut")
				));
		lines.add((JPanel)data[0]);
		drawMode=(JComboBox<String>)data[1];
		drawMode.setEnabled(!readOnly);
		drawMode.setRenderer(new IconListCellRenderer(new Images[] {
				Images.MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART,
				Images.MODELEDITOR_ELEMENT_ANIMATION_DONUT_CHART,
		}));

		data=getLineWidthInputPanel(Language.tr("Surface.AnimationPieChart.Dialog.Appearance.FrameWidth")+":",0,15,5);
		lines.add((JPanel)data[0]);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Rahmenfarbe */
		line.add(colorChooserLine=new LabeledColorChooserButton(Language.tr("Surface.AnimationPieChart.Dialog.Appearance.FrameColor")+":",Color.BLACK));
		colorChooserLine.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Hintergrundfarbe */
		line.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.AnimationPieChart.Dialog.Appearance.FillBackground"),null,Color.WHITE));
		colorChooserBackground.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Farbverlauf */
		line.add(colorChooserGradient=new OptionalColorChooserButton(Language.tr("Surface.AnimationPieChart.Dialog.BackgroundGradient"),null,Color.WHITE));
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{if (colorChooserGradient.isActive()) colorChooserBackground.setActive(true);});

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

			switch (diagram.getDiagramDrawMode()) {
			case PIE: drawMode.setSelectedIndex(0); break;
			case DONUT: drawMode.setSelectedIndex(1); break;
			default: drawMode.setSelectedIndex(0); break;
			}

			lineWidth.setSelectedIndex(diagram.getBorderWidth());
			colorChooserLine.setColor(diagram.getBorderColor());
			colorChooserBackground.setColor(diagram.getBackgroundColor());
			colorChooserGradient.setColor(diagram.getGradientFillColor());
		}

		return tabs;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,800);
		setResizable(true);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
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

			switch (drawMode.getSelectedIndex()) {
			case 0: diagram.setDiagramDrawMode(ModelElementAnimationPieChart.DrawMode.PIE); break;
			case 1: diagram.setDiagramDrawMode(ModelElementAnimationPieChart.DrawMode.DONUT); break;
			}

			diagram.setBorderWidth(lineWidth.getSelectedIndex());
			diagram.setBorderColor(colorChooserLine.getColor());
			diagram.setBackgroundColor(colorChooserBackground.getColor());
			diagram.setGradientFillColor(colorChooserGradient.getColor());
		}
	}
}
