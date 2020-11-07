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
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

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
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationImage}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationImage
 */
public class ModelElementAnimationImageDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7981574083085828123L;

	/** Tabelle zur Konfiguration der anzuzeigenden Bilder */
	private ModelElementAnimationImageTableModel tableImages;
	/** Auswahlbox für die Rahmenbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Rahmenfarbe */
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationImage}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationImageDialog(final Component owner, final ModelElementAnimationImage element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationImage.Dialog.Title"),element,"ModelElementAnimationImage",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel tab, sub, line;

		/* Tab: Bilder */
		tabs.add(Language.tr("Surface.AnimationImage.Dialog.Tab.Images"),tab=new JPanel(new BorderLayout()));

		final JTableExt table;
		tab.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(tableImages=new ModelElementAnimationImageTableModel(table,(ModelElementAnimationImage)element,readOnly,helpRunnable));
		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		/* Tab: Rahmen */
		tabs.add(Language.tr("Surface.AnimationImage.Dialog.Tab.Border"),tab=new JPanel(new BorderLayout()));

		/* Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.AnimationImage.Dialog.FrameWidth")+":",0,15,((ModelElementAnimationImage)element).getBorderWidth());
		tab.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Rahmenfarbe */
		tab.add(sub=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("Surface.AnimationImage.Dialog.FrameColor")+":"));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);
		line.add(colorChooser=new SmallColorChooser(((ModelElementAnimationImage)element).getBorderColor()));
		colorChooser.setEnabled(!readOnly);

		/* Icons für Tabs laden */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_BORDER.getIcon());

		return tabs;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(650,550);
		pack();
		setResizable(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementAnimationImage image=(ModelElementAnimationImage)element;

		tableImages.storeData();

		image.setBorderWidth(lineWidth.getSelectedIndex());
		image.setBorderColor(colorChooser.getColor());
	}
}