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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.LabeledColorChooserButton;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationRecord}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationRecord
 */
public class ModelElementAnimationRecordDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6357684372735211428L;

	/** ID des Aufzeichnungselemente von dem die Daten stammen sollen */
	private List<Integer> ids;
	/** Auswahlbox zur Wahl der Aufzeichnungsstation */
	private JComboBox<String> selectRecord;
	/** Eingabefeld für die Anzahl an anzuzeigenden Punkten */
	private JTextField displayPoints;
	/** Achsenbeschriftung anzeigen */
	private AxisDrawerEdit axisLabels;
	/** Auswahl der Farbe für die Datenpunkte */
	private LabeledColorChooserButton colorChooserData;
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
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationRecord}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationRecordDialog(final Component owner, final ModelElementAnimationRecord element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationRecord.Dialog.Title"),element,"ModelElementAnimationRecord",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationRecord;
	}

	/**
	 * Liefert eine Auflistung der Stationen zur Aufzeichnung von Daten
	 * @param surface	Zeichenfläche die (inkl. ihrer Unterzeichenflächen) durchsucht werden soll
	 * @return	Zuordnung von IDs zu Stationsnamen der Aufzeichnungsstationen
	 */
	private final Map<Integer,String> getRecordElements(final ModelSurface surface) {
		final Map<Integer,String> map=new HashMap<>();
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementRecord) map.put(element.getId(),String.format("%s (id=%d)",element.getName(),element.getId()));
			if (element instanceof ModelElementSub) map.putAll(getRecordElements(((ModelElementSub)element).getSubSurface()));
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		JPanel content, line, cell;
		Object[] data;

		/* Daten: Aufzeichnungselement und Anzahl an Datenpunkten */
		tabs.addTab(Language.tr("Surface.AnimationRecord.Dialog.Data"),content=new JPanel(new BorderLayout()));
		content.add(cell=new JPanel(),BorderLayout.NORTH);
		cell.setLayout(new BoxLayout(cell,BoxLayout.PAGE_AXIS));

		final Map<Integer,String> map=getRecordElements(element.getModel().surface);
		ids=new ArrayList<>(map.keySet());
		data=getComboBoxPanel(Language.tr("Surface.AnimationRecord.Dialog.Data.SelectRecord")+":",null,map.values());
		cell.add((JPanel)data[0]);
		selectRecord=(JComboBox<String>)data[1];
		selectRecord.setEditable(false);
		selectRecord.setEnabled(!readOnly);

		data=getInputPanel(Language.tr("Surface.AnimationRecord.Dialog.Data.DisplayPoints")+":","1000",5);
		cell.add((JPanel)data[0]);
		displayPoints=(JTextField)data[1];
		displayPoints.setEnabled(!readOnly);
		displayPoints.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Achsenbeschriftung */
		cell.add(axisLabels=new AxisDrawerEdit(AxisDrawer.Mode.OFF,"","",readOnly));

		cell.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooserData=new LabeledColorChooserButton(Language.tr("Surface.AnimationRecord.Dialog.Appearance.DataColor")+":",Color.BLUE));
		colorChooserData.setEnabled(!readOnly);

		/* Darstellung: Farben und Linienbreiten */
		tabs.addTab(Language.tr("Surface.AnimationRecord.Dialog.Appearance"),content=new JPanel(new BorderLayout()));

		data=getLineWidthInputPanel(Language.tr("Surface.AnimationRecord.Dialog.Appearance.FrameWidth")+":",0,15,5);
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Zeile für Farben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Rahmenfarbe */
		line.add(colorChooserLine=new LabeledColorChooserButton(Language.tr("Surface.AnimationRecord.Dialog.Appearance.FrameColor")+":",Color.BLACK));
		colorChooserLine.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Hintergrundfarbe */
		line.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.AnimationRecord.Dialog.Appearance.FillBackground"),null,Color.WHITE));
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->{if (!colorChooserBackground.isActive()) colorChooserGradient.setActive(false);});

		line.add(Box.createHorizontalStrut(10));

		/* Farbverlauf */
		line.add(colorChooserGradient=new OptionalColorChooserButton(Language.tr("Surface.AnimationRecord.Dialog.BackgroundGradient"),null,Color.WHITE));
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{if (colorChooserGradient.isActive()) colorChooserBackground.setActive(true);});

		/* Icons für Tabs */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE.getIcon());

		/* Daten laden */
		if (element instanceof ModelElementAnimationRecord) {
			final ModelElementAnimationRecord record=(ModelElementAnimationRecord)element;
			int index=ids.indexOf(record.getRecordId());
			if (index<0 && ids.size()>0) index=0;
			if (index>=0) selectRecord.setSelectedIndex(index);
			displayPoints.setText(""+record.getDisplayPoints());
			axisLabels.set(record.getAxisLabels(),record.getAxisLabelXText(),record.getAxisLabelYText());
			colorChooserData.setColor(record.getDataColor());
			lineWidth.setSelectedIndex(record.getBorderWidth());
			colorChooserLine.setColor(record.getBorderColor());
			colorChooserBackground.setColor(record.getBackgroundColor());
			colorChooserGradient.setColor(record.getGradientFillColor());
		}

		return tabs;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		if (selectRecord.getSelectedIndex()<0) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.AnimationRecord.Dialog.Data.SelectRecord.Error.Title"),Language.tr("Surface.AnimationRecord.Dialog.Data.SelectRecord.Error.Info"));
				return false;
			}
			ok=false;
		}

		final Long L=NumberTools.getPositiveLong(displayPoints,true);
		if (L==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.AnimationRecord.Dialog.Data.DisplayPoints.Error.Title"),String.format(Language.tr("Surface.AnimationRecord.Dialog.Data.DisplayPoints.Error.Info"),displayPoints.getText()));
				return false;
			}
			ok=false;
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

		if (element instanceof ModelElementAnimationRecord) {
			final ModelElementAnimationRecord record=(ModelElementAnimationRecord)element;

			record.setRecordId(ids.get(selectRecord.getSelectedIndex()));
			record.setDisplayPoints(NumberTools.getPositiveLong(displayPoints,true).intValue());
			record.setAxisLabels(axisLabels.getMode());
			record.setAxisLabelXText(axisLabels.getXLabel());
			record.setAxisLabelYText(axisLabels.getYLabel());
			record.setDataColor(colorChooserData.getColor());

			record.setBorderWidth(lineWidth.getSelectedIndex());
			record.setBorderColor(colorChooserLine.getColor());
			record.setBackgroundColor(colorChooserBackground.getColor());
			record.setGradientFillColor(colorChooserGradient.getColor());
		}
	}
}
