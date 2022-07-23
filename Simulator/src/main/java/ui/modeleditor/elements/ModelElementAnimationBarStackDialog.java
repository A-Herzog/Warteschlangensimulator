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

import javax.swing.BoxLayout;
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
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationBarStack}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationBarStack
 */
public class ModelElementAnimationBarStackDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4770924059010228218L;

	/** Auswahlbox für die Richtung, in die sich der Balken aufbaut */
	private JComboBox<String> selectDirection;
	/** Option: Soll ein expliziter Maximalwert verwendet werden? */
	private JCheckBox useMaximum;
	/** Eingabefeld für den Maximalwert */
	private JTextField editMaximum;
	/** Achsenbeschriftung anzeigen */
	private AxisDrawerEdit axisLabels;
	/** Tabelle zur Definition Balkensegmente */
	private BarStackTableModel tableModel;
	/** Auswahlbox für die Linienbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Rahmenfarbe */
	private SmallColorChooser colorChooserLine;
	/** Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationBarStack}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationBarStackDialog(final Component owner, final ModelElementAnimationBarStack element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationBarStack.Dialog.Title"),element,"ModelElementAnimationBarStack",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationBarStack;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel tab, line, cell;
		JLabel label;

		/* Tab: Daten */
		tabs.add(Language.tr("Surface.AnimationBarStack.Dialog.Tab.Data"),tab=new JPanel(new BorderLayout()));

		/* Richtung und Maximum */
		final JPanel setup=new JPanel();
		tab.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationBarStack.Dialog.Bar")+":"));
		line.add(selectDirection=new JComboBox<>(new String[]{
				Language.tr("Surface.AnimationBarStack.Dialog.Bar.up"),
				Language.tr("Surface.AnimationBarStack.Dialog.Bar.right"),
				Language.tr("Surface.AnimationBarStack.Dialog.Bar.down"),
				Language.tr("Surface.AnimationBarStack.Dialog.Bar.left")
		}));
		selectDirection.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_UP,
				Images.ARROW_RIGHT,
				Images.ARROW_DOWN,
				Images.ARROW_LEFT
		}));
		selectDirection.setEnabled(!readOnly);
		label.setLabelFor(selectDirection);

		line.add(useMaximum=new JCheckBox(Language.tr("Surface.AnimationBarStack.Dialog.Maximum")+":"));
		useMaximum.addActionListener(e->checkData(false));
		line.add(editMaximum=new JTextField(10));
		editMaximum.setEditable(!readOnly);
		editMaximum.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Achsenbeschriftung */
		setup.add(axisLabels=new AxisDrawerEdit(AxisDrawer.Mode.OFF,null,""));

		final JTableExt table;
		tab.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(tableModel=new BarStackTableModel(table,(ModelElementAnimationBarStack)element,readOnly,helpRunnable));
		table.getColumnModel().getColumn(1).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setMinWidth(50);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		/* Tab: Layout */
		tabs.add(Language.tr("Surface.AnimationBarStack.Dialog.Tab.Layout"),tab=new JPanel(new BorderLayout()));

		/* Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.AnimationBarStack.Dialog.FrameWidth")+":",1,15);
		tab.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Farben */
		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationBarStack.Dialog.FrameColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserLine=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserLine.setEnabled(!readOnly);
		label.setLabelFor(colorChooserLine);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(background=new JCheckBox(Language.tr("Surface.AnimationBarStack.Dialog.FillBackground")),BorderLayout.NORTH);
		background.setEnabled(!readOnly);
		cell.add(colorChooserBackground=new SmallColorChooser(Color.WHITE),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,background.getPreferredSize().height));

		/* Icons */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationBarStack) {
			final ModelElementAnimationBarStack diagram=(ModelElementAnimationBarStack)element;
			switch (diagram.getDirection()) {
			case DIRECTION_UP: selectDirection.setSelectedIndex(0); break;
			case DIRECTION_RIGHT: selectDirection.setSelectedIndex(1); break;
			case DIRECTION_DOWN: selectDirection.setSelectedIndex(2); break;
			case DIRECTION_LEFT: selectDirection.setSelectedIndex(3); break;
			}
			final double max=diagram.getMaxValue();
			if (max<=0) {
				useMaximum.setSelected(false);
				editMaximum.setText("10");
			} else {
				useMaximum.setSelected(true);
				editMaximum.setText(NumberTools.formatNumberMax(max));
			}
			axisLabels.set(diagram.getAxisLabels(),null,diagram.getAxisLabelText());
			lineWidth.setSelectedIndex(diagram.getBorderWidth()-1);
			colorChooserLine.setColor(diagram.getBorderColor());
			background.setSelected(diagram.getBackgroundColor()!=null);
			if (diagram.getBackgroundColor()!=null) colorChooserBackground.setColor(diagram.getBackgroundColor());
		}

		checkData(false);

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
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		if (useMaximum.isSelected()) {
			final Double Dmax=NumberTools.getDouble(editMaximum,true);
			if (Dmax==null || Dmax<=0) {
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationBarStack.Dialog.Maximum.Error.Title"),String.format(Language.tr("Surface.AnimationBarStack.Dialog.Maximum.Error.Info"),editMaximum.getText()));
					return false;
				}
				ok=true;
			}
		} else {
			editMaximum.setBackground(NumberTools.getTextFieldDefaultBackground());
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

		final ModelElementAnimationBarStack bar=(ModelElementAnimationBarStack)element;

		switch (selectDirection.getSelectedIndex()) {
		case 0: bar.setDirection(ModelElementAnimationBarStack.FillDirection.DIRECTION_UP); break;
		case 1: bar.setDirection(ModelElementAnimationBarStack.FillDirection.DIRECTION_RIGHT); break;
		case 2: bar.setDirection(ModelElementAnimationBarStack.FillDirection.DIRECTION_DOWN); break;
		case 3: bar.setDirection(ModelElementAnimationBarStack.FillDirection.DIRECTION_LEFT); break;
		}

		if (useMaximum.isSelected()) {
			bar.setMaxValue(NumberTools.getDouble(editMaximum,true));
		} else {
			bar.setMaxValue(0);
		}
		bar.setAxisLabels(axisLabels.getMode());
		bar.setAxisLabelText(axisLabels.getYLabel());
		tableModel.storeData();
		bar.setBorderWidth(lineWidth.getSelectedIndex()+1);
		bar.setBorderColor(colorChooserLine.getColor());
		bar.setBackgroundColor((background.isSelected())?colorChooserBackground.getColor():null);
	}
}
