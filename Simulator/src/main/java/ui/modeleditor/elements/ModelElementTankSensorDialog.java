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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTankSensor}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTankSensor
 */
public class ModelElementTankSensorDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1887665763539776879L;

	private List<Integer> tankIDs;
	private String[] tankNames;

	private JComboBox<String> tankCombo;
	private JTextField thresholdEdit;
	private JRadioButton thresholdPercent;
	private JRadioButton thresholdUp;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTankSensor}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTankSensorDialog(final Component owner, final ModelElementTankSensor element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TankSensor.Dialog.Title"),element,"ModelElementTankSensor",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	private Object[] getSelect(final JPanel content, final String title, final String option1, final String option2) {
		JPanel line;
		JRadioButton button1, button2;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+title+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(button1=new JRadioButton(option1));
		button1.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(button2=new JRadioButton(option2));
		button2.setEnabled(!readOnly);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(button1);
		buttonGroup.add(button2);

		return new Object[]{button1,button2};
	}

	private void buildTankIDs() {
		tankIDs=new ArrayList<>();
		final List<String> names=new ArrayList<>();

		final ModelSurface mainSurface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		for (ModelElement element1: mainSurface.getElements()) {
			if (element1 instanceof ModelElementTank) {
				names.add(String.format(Language.tr("Surface.TankSensor.Dialog.Tank.Name"),element1.getName(),element1.getId()));
				tankIDs.add(element1.getId());
			}
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementTank) {
					names.add(String.format(Language.tr("Surface.TankSensor.Dialog.Tank.Name"),element2.getName(),element2.getId()));
					tankIDs.add(element2.getId());
				}
			}
		}

		tankNames=names.toArray(new String[0]);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTankSensor;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;

		buildTankIDs();

		/* Tank ID */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TankSensor.Dialog.Tank")+":"));
		line.add(tankCombo=new JComboBox<>(tankNames));
		tankCombo.setEnabled(!readOnly);
		label.setLabelFor(tankCombo);

		/* Schwellenwert */
		data=getInputPanel(Language.tr("Surface.TankSensor.Dialog.Threshold")+":","",10);
		content.add((JPanel)data[0]);
		thresholdEdit=(JTextField)data[1];
		thresholdEdit.setEditable(!readOnly);
		thresholdEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Absolut/Prozent */
		final JRadioButton thresholdAbsolute;
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		data=getSelect(content,Language.tr("Surface.TankSensor.Dialog.Threshold.Type"),Language.tr("Surface.TankSensor.Dialog.Threshold.Type.Absolute"),Language.tr("Surface.TankSensor.Dialog.Threshold.Type.Percent"));
		thresholdAbsolute=(JRadioButton)data[0];
		thresholdPercent=(JRadioButton)data[1];

		/* Richtung */
		final JRadioButton thresholdDown;
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		data=getSelect(content,Language.tr("Surface.TankSensor.Dialog.Threshold.Direction"),Language.tr("Surface.TankSensor.Dialog.Threshold.Direction.Up"),Language.tr("Surface.TankSensor.Dialog.Threshold.Direction.Down"));
		thresholdUp=(JRadioButton)data[0];
		thresholdDown=(JRadioButton)data[1];

		/* Daten laden */
		if (element instanceof ModelElementTankSensor) {
			final ModelElementTankSensor sensor=(ModelElementTankSensor)element;

			/* Tank ID */
			int index=tankIDs.indexOf(sensor.getTankId());
			if (index<0 && tankIDs.size()>0) index=0;
			if (index>=0) tankCombo.setSelectedIndex(index);

			/* Schwellenwert */
			thresholdEdit.setText(NumberTools.formatNumberMax(sensor.getThreshold()));

			/* Absolut/Prozent */
			thresholdAbsolute.setSelected(!sensor.isThresholdIsPercent());
			thresholdPercent.setSelected(sensor.isThresholdIsPercent());

			/* Richtung */
			thresholdUp.setSelected(sensor.getThresholdDirection()==ModelElementTankSensor.ThresholdDirection.DIRECTION_UP);
			thresholdDown.setSelected(sensor.getThresholdDirection()==ModelElementTankSensor.ThresholdDirection.DIRECTION_DOWN);
		}

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Tank ID */
		if (tankCombo.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankSensor.Dialog.Tank.ErrorTitle"),Language.tr("Surface.TankSensor.Dialog.Tank.ErrorInfo"));
				return false;
			}
		}

		/* Schwellenwert */
		final Double D=NumberTools.getNotNegativeDouble(thresholdEdit,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankSensor.Dialog.Threshold.ErrorTitle"),String.format(Language.tr("Surface.TankSensor.Dialog.Threshold.ErrorInfo"),thresholdEdit.getText()));
				return false;
			}
		}

		return ok;
	}

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

		if (element instanceof ModelElementTankSensor) {
			final ModelElementTankSensor sensor=(ModelElementTankSensor)element;

			/* Tank ID */
			final int index=tankCombo.getSelectedIndex();
			if (index>=0) sensor.setTankId(tankIDs.get(index));

			/* Schwellenwert */
			sensor.setThreshold(NumberTools.getNotNegativeDouble(thresholdEdit,true));

			/* Absolut/Prozent */
			sensor.setThresholdIsPercent(thresholdPercent.isSelected());

			/* Richtung */
			if (thresholdUp.isSelected()) {
				sensor.setThresholdDirection(ModelElementTankSensor.ThresholdDirection.DIRECTION_UP);
			} else {
				sensor.setThresholdDirection(ModelElementTankSensor.ThresholdDirection.DIRECTION_DOWN);
			}
		}
	}
}