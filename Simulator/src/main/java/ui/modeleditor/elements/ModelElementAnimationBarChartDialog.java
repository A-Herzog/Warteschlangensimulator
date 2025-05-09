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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.LabeledColorChooserButton;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementAnimationBarChart}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationBarChart
 */
public class ModelElementAnimationBarChartDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6830401157286607539L;

	/** Option: Minimalwert manuell festlegen */
	private JCheckBox minValueCheckBox;
	/** Eingabefeld f�r den manuell festgelegten Minimalwert */
	private JTextField minValueEdit;
	/** Option: Maximalwert manuell festlegen */
	private JCheckBox maxValueCheckBox;
	/** Eingabefeld f�r den manuell festgelegten Maximalwert */
	private JTextField maxValueEdit;
	/** Achsenbeschriftung anzeigen */
	private AxisDrawerEdit axisLabels;
	/** Tabelle zur Definition der einzelnen Balken */
	private ExpressionTableModelBar expressionTableModel;
	/** Auswahlbox f�r die Linienbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Linienfarbe */
	private LabeledColorChooserButton colorChooserLine;
	/** Auswahl der Hintergrundfarbe */
	private OptionalColorChooserButton colorChooserBackground;
	/** Auswahl der Farbe f�r den Farbverlauf */
	private OptionalColorChooserButton colorChooserGradient;
	/** Option: 3D-Effekte f�r die Balken? */
	private JCheckBox use3D;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationBarChart}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationBarChartDialog(final Component owner, final ModelElementAnimationBarChart element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationBarChart.Dialog.Title"),element,"ModelElementAnimationBarChart",readOnly);
	}

	@Override
	protected void initUserButtons() {
		if (!readOnly) {
			addUserButton(Language.tr("Surface.AnimationBarChart.HistogramWizard.ButtonTitle"),Language.tr("Surface.AnimationBarChart.HistogramWizard.ButtonHint"),Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART.getIcon());
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationBarChart;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel content, lines, line;
		Object[] data;

		/* Daten: Zeitbereich und Diagrammreihen */
		tabs.addTab(Language.tr("Surface.AnimationBarChart.Dialog.Data"),content=new JPanel(new BorderLayout()));

		content.add(lines=new JPanel(),BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));
		lines.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(maxValueCheckBox=new JCheckBox(Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMaxValue")));
		maxValueCheckBox.setEnabled(!readOnly);
		line.add(maxValueEdit=new JTextField(7));
		ModelElementBaseDialog.addUndoFeature(maxValueEdit);
		maxValueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); maxValueCheckBox.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); maxValueCheckBox.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); maxValueCheckBox.setSelected(true);}
		});
		maxValueEdit.setEnabled(!readOnly);
		lines.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(minValueCheckBox=new JCheckBox(Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMinValue")));
		minValueCheckBox.setEnabled(!readOnly);
		line.add(minValueEdit=new JTextField(7));
		ModelElementBaseDialog.addUndoFeature(minValueEdit);
		minValueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); minValueCheckBox.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); minValueCheckBox.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); minValueCheckBox.setSelected(true);}
		});
		minValueEdit.setEnabled(!readOnly);

		/* Achsenbeschriftung */
		lines.add(axisLabels=new AxisDrawerEdit(AxisDrawer.Mode.OFF,null,"",readOnly));

		final JTableExt expressionTable;
		content.add(new JScrollPane(expressionTable=new JTableExt()),BorderLayout.CENTER);
		if (element instanceof ModelElementAnimationBarChart) {
			expressionTable.setModel(expressionTableModel=new ExpressionTableModelBar(expressionTable,(ModelElementAnimationBarChart)element,readOnly,helpRunnable));
		}
		expressionTable.getColumnModel().getColumn(1).setMaxWidth(100);
		expressionTable.getColumnModel().getColumn(1).setMinWidth(100);
		expressionTable.setIsPanelCellTable(0);
		expressionTable.setIsPanelCellTable(1);
		expressionTable.setEnabled(!readOnly);

		/* Darstellung: Farben und Linienbreiten */
		tabs.addTab(Language.tr("Surface.AnimationBarChart.Dialog.Appearance"),content=new JPanel(new BorderLayout()));
		final JPanel contentInnter=new JPanel();
		contentInnter.setLayout(new BoxLayout(contentInnter,BoxLayout.PAGE_AXIS));
		content.add(contentInnter,BorderLayout.NORTH);

		data=getLineWidthInputPanel(Language.tr("Surface.AnimationBarChart.Dialog.Appearance.FrameWidth")+":",0,15,5);
		contentInnter.add((JPanel)data[0]);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Zeile f�r Farben */
		contentInnter.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* Rahmenfarbe */
		line.add(colorChooserLine=new LabeledColorChooserButton(Language.tr("Surface.AnimationBarChart.Dialog.Appearance.FrameColor")+":",Color.BLACK));
		colorChooserLine.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Hintergrundfarbe */
		line.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.AnimationBarChart.Dialog.Appearance.FillBackground"),null,Color.WHITE));
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->{if (!colorChooserBackground.isActive()) colorChooserGradient.setActive(false);});

		line.add(Box.createHorizontalStrut(10));

		/* Farbverlauf */
		line.add(colorChooserGradient=new OptionalColorChooserButton(Language.tr("Surface.AnimationBarChart.Dialog.BackgroundGradient"),null,Color.WHITE));
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{if (colorChooserGradient.isActive()) colorChooserBackground.setActive(true);});

		/* Zeile f�r 3D-Effekt */
		contentInnter.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* 3D-Effekt verwenden */
		contentInnter.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(use3D=new JCheckBox(Language.tr("Surface.AnimationBarChart.Dialog.Appearance.Use3D")));

		/* Icons f�r Tabs */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		/* Daten laden */
		if (element instanceof ModelElementAnimationBarChart) {
			final ModelElementAnimationBarChart diagram=(ModelElementAnimationBarChart)element;
			Double D;
			D=diagram.getMinValue();
			minValueCheckBox.setSelected(D!=null);
			minValueEdit.setText(NumberTools.formatNumberMax((D==null)?0.0:D));
			D=diagram.getMaxValue();
			maxValueCheckBox.setSelected(D!=null);
			maxValueEdit.setText(NumberTools.formatNumberMax((D==null)?0.0:D));
			axisLabels.set(diagram.getAxisLabels(),null,diagram.getAxisLabelText());
			lineWidth.setSelectedIndex(diagram.getBorderWidth());
			colorChooserLine.setColor(diagram.getBorderColor());
			colorChooserBackground.setColor(diagram.getBackgroundColor());
			colorChooserGradient.setColor(diagram.getGradientFillColor());
			use3D.setSelected(diagram.isUse3D());
		}

		return tabs;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,800);
		setResizable(true);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		Double minValue=null;
		if (minValueCheckBox.isSelected()) {
			minValue=NumberTools.getDouble(minValueEdit,true);
			if (minValue==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMinValue.Error.Title"),String.format(Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMinValue.Error.Info"),minValueEdit.getText()));
					return false;
				}
				ok=false;
			}
		} else {
			minValueEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		Double maxValue=null;
		if (maxValueCheckBox.isSelected()) {
			maxValue=NumberTools.getDouble(maxValueEdit,true);
			if (maxValue==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMaxValue.Error.Title"),String.format(Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMaxValue.Error.Info"),maxValueEdit.getText()));
					return false;
				}
				ok=false;
			} else {
				if (minValue!=null && minValue.doubleValue()>=maxValue.doubleValue()) {
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMaxValue.RangeError.Title"),String.format(Language.tr("Surface.AnimationBarChart.Dialog.Data.ManualMaxValue.RangeError.Info"),maxValueEdit.getText()));
						return false;
					}
					ok=false;
				}
			}
		} else {
			maxValueEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationBarChart) {
			final ModelElementAnimationBarChart diagram=(ModelElementAnimationBarChart)element;

			if (minValueCheckBox.isSelected()) {
				diagram.setMinValue(NumberTools.getDouble(minValueEdit,true));
			} else {
				diagram.setMinValue(null);
			}

			if (maxValueCheckBox.isSelected()) {
				diagram.setMaxValue(NumberTools.getDouble(maxValueEdit,true));
			} else {
				diagram.setMaxValue(null);
			}

			diagram.setAxisLabels(axisLabels.getMode());
			diagram.setAxisLabelText(axisLabels.getYLabel());

			expressionTableModel.storeData(diagram);

			diagram.setBorderWidth(lineWidth.getSelectedIndex());
			diagram.setBorderColor(colorChooserLine.getColor());
			diagram.setBackgroundColor(colorChooserBackground.getColor());
			diagram.setGradientFillColor(colorChooserGradient.getColor());
			diagram.setUse3D(use3D.isSelected());
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (nr==0) {
			final ModelElementAnimationBarChartHistogramWizard dialog=new ModelElementAnimationBarChartHistogramWizard(this,element.getModel(),helpRunnable);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				if (dialog.getReplaceRecords()) expressionTableModel.clear();
				final String[] commands=dialog.getCommands();
				final Color barColor=dialog.getBarColor();
				final Color lastColor=new Color(150,150,150);
				for (int i=0;i<commands.length;i++) expressionTableModel.add(new AnimationExpression(commands[i]),(i<commands.length-1)?barColor:lastColor);
			}
			return;
		}
	}
}
