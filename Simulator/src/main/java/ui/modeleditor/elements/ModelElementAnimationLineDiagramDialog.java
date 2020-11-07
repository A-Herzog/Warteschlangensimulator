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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationLineDiagram}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationLineDiagram
 */
public class ModelElementAnimationLineDiagramDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6137736072797243359L;

	/** Eingabefeld für den im Diagramm darzustellenden Zeitbereich */
	private JTextField timeAreaEdit;
	/** Auswahlbox für die Zeiteinheit für {@link #timeAreaEdit} */
	private JComboBox<String> timeAreaComboBox;
	/** Tabelle zur Definition der Datenreihen */
	private ExpressionTableModelLine expressionTableModel;
	/** Auswahlbox für die Rahmenbreite */
	private JComboBox<JLabel> lineWidth;
	/** Farbe der Rahmenbreite */
	private SmallColorChooser colorChooserLine;
	/** Option: Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationLineDiagram}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationLineDiagramDialog(final Component owner, final ModelElementAnimationLineDiagram element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationDiagram.Dialog.Title"),element,"ModelElementAnimationDiagram",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationDiagram;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel content, line, cell;
		JLabel label;
		Object[] data;

		/* Daten: Zeitbereich und Diagrammreihen */
		tabs.addTab(Language.tr("Surface.AnimationDiagram.Dialog.Data"),content=new JPanel(new BorderLayout()));
		data=getInputPanel(Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange")+":","1",5);
		content.add(line=(JPanel)data[0],BorderLayout.NORTH);
		timeAreaEdit=(JTextField)data[1];
		timeAreaEdit.setEditable(!readOnly);
		timeAreaEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(timeAreaComboBox=new JComboBox<>(new String[]{
				Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Seconds"),
				Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Minutes"),
				Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Hours")
		}));
		timeAreaComboBox.setEnabled(!readOnly);
		timeAreaComboBox.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		final JTableExt expressionTable;
		content.add(new JScrollPane(expressionTable=new JTableExt()),BorderLayout.CENTER);
		if (element instanceof ModelElementAnimationLineDiagram) {
			expressionTable.setModel(expressionTableModel=new ExpressionTableModelLine(expressionTable,(ModelElementAnimationLineDiagram)element,readOnly,helpRunnable));
		}
		expressionTable.getColumnModel().getColumn(0).setMaxWidth(200);
		expressionTable.getColumnModel().getColumn(0).setMinWidth(200);
		expressionTable.setIsPanelCellTable(0);
		expressionTable.setIsPanelCellTable(1);
		expressionTable.setIsPanelCellTable(2);
		expressionTable.setEnabled(!readOnly);

		/* Darstellung: Farben und Linienbreiten */
		tabs.addTab(Language.tr("Surface.AnimationDiagram.Dialog.Appearance"),content=new JPanel(new BorderLayout()));

		data=getLineWidthInputPanel(Language.tr("Surface.AnimationDiagram.Dialog.Appearance.FrameWidth")+":",0,15,5);
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationDiagram.Dialog.Appearance.FrameColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserLine=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserLine.setEnabled(!readOnly);
		label.setLabelFor(colorChooserLine);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(background=new JCheckBox(Language.tr("Surface.AnimationDiagram.Dialog.Appearance.FillBackground")),BorderLayout.NORTH);
		background.setEnabled(!readOnly);
		cell.add(colorChooserBackground=new SmallColorChooser(Color.WHITE),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,background.getPreferredSize().height));

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		/* Daten laden */
		if (element instanceof ModelElementAnimationLineDiagram) {
			final ModelElementAnimationLineDiagram diagram=(ModelElementAnimationLineDiagram)element;
			final long timeArea=diagram.getTimeArea();
			if (timeArea>=10*3600) {
				timeAreaEdit.setText(""+(timeArea/3600));
				timeAreaComboBox.setSelectedIndex(2);
			} else {
				if (timeArea>=15*60) {
					timeAreaEdit.setText(""+(timeArea/60));
					timeAreaComboBox.setSelectedIndex(1);
				} else {
					timeAreaEdit.setText(""+timeArea);
					timeAreaComboBox.setSelectedIndex(0);
				}
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
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		if (timeAreaComboBox.getSelectedIndex()==0) {
			/* Sekunden */
			Integer I2=NumberTools.getNotNegativeInteger(timeAreaEdit,true);
			if (I2==null || I2<1) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Error.Title"),String.format(Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Error.InfoSeconds"),timeAreaEdit.getText()));
					return false;
				}
				ok=false;
			}
		} else {
			/* Minuten oder Stunden */
			Double D=NumberTools.getNotNegativeDouble(timeAreaEdit,true);
			if (D==null || D==0.0) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Error.Title"),String.format(Language.tr("Surface.AnimationDiagram.Dialog.Data.TimeRange.Error.InfoMinutesHours"),timeAreaEdit.getText()));
					return false;
				}
				ok=false;
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationLineDiagram) {
			final ModelElementAnimationLineDiagram diagram=(ModelElementAnimationLineDiagram)element;

			final Double D=NumberTools.getNotNegativeDouble(timeAreaEdit,true);
			if (D!=null) switch (timeAreaComboBox.getSelectedIndex()) {
			case 0: diagram.setTimeArea((int)FastMath.round(D)); break;
			case 1: diagram.setTimeArea((int)FastMath.round(D*60)); break;
			case 2: diagram.setTimeArea((int)FastMath.round(D*3600)); break;
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