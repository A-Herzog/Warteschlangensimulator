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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementPickUp}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementPickUp
 */
public class ModelElementPickUpDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3315657286325820864L;

	/** Liste mit den Namen der Stationen, die Warteschlangen besitzen */
	private String[] queueNames;
	/** Liste mit den IDs der Stationen, die Warteschlangen besitzen */
	private int[] queueIDs;

	/** Auswahlbox für die fremde Warteschlange */
	private JComboBox<String> selectQueue;
	/** Option: Kunde allein weiterleiten, wenn Warteschlange ({@link #selectQueue}) leer ist */
	private JCheckBox sendAloneIfQueueEmpty;
	/** Option: Kunden gemeinsam weiterleiten */
	private JRadioButton optionForward;
	/** Option: Temporären Batch bilden */
	private JRadioButton optionTemporary;
	/** Eingabefeld für den neuen Kundentyp für einen temporären Batch */
	private JTextField tempTypeField;
	/** Option: Permanenten Batch bilden */
	private JRadioButton optionNewType;
	/** Eingabefeld für den neuen Kundentyp für einen permanenten Batch */
	private JTextField newTypeField;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementPickUp}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementPickUpDialog(final Component owner, final ModelElementPickUp element, final boolean readOnly) {
		super(owner,Language.tr("Surface.PickUp.Dialog.Title"),element,"ModelElementPickUp",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Sucht nach Stationen mit Warteschlangen.
	 * @see #queueNames
	 * @see #queueIDs
	 */
	private void loadQueueData() {
		final List<String> names=new ArrayList<>();
		final List<Integer> ids=new ArrayList<>();

		for (ModelElement element: element.getSurface().getElements()) {
			if (element instanceof ModelElementProcess) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
			if (element instanceof ModelElementHold) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
			if (element instanceof ModelElementHoldMulti) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
			if (element instanceof ModelElementHoldJS) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
			if (element instanceof ModelElementBarrier) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
			if (element instanceof ModelElementBarrierPull) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
			if (element instanceof ModelElementConveyor) {
				names.add(((element.getName().trim().isEmpty())?Language.tr("Surface.PickUp.Dialog.Station"):element.getName())+" (id="+element.getId()+")");
				ids.add(element.getId());
			}
		}

		if (names.size()==0) {
			queueNames=new String[]{"<"+Language.tr("Surface.PickUp.Dialog.NoQueueAvailable")+">"};
			queueIDs=new int[]{-1};
		} else {
			queueNames=names.toArray(new String[0]);
			queueIDs=new int[queueNames.length];
			for (int i=0;i<queueIDs.length;i++) queueIDs[i]=ids.get(i);
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationPickUp;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		loadQueueData();

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.PickUp.Dialog.Queue")+":"));
		line.add(selectQueue=new JComboBox<>(queueNames));
		selectQueue.setSelectedIndex(0);
		for (int i=0;i<queueIDs.length;i++) if (queueIDs[i]==((ModelElementPickUp)element).getQueueID()) {selectQueue.setSelectedIndex(i); break;}
		label.setLabelFor(selectQueue);
		selectQueue.setEnabled(!readOnly);
		line.add(sendAloneIfQueueEmpty=new JCheckBox(Language.tr("Surface.PickUp.Dialog.DirectForwardIfQueueIsEmpty"),((ModelElementPickUp)element).isSendAloneIfQueueEmpty()));
		sendAloneIfQueueEmpty.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionForward=new JRadioButton(Language.tr("Surface.PickUp.Dialog.Mode.Forward")));
		optionForward.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTemporary=new JRadioButton(Language.tr("Surface.PickUp.Dialog.SendTemporaryBatched")));
		optionTemporary.setEnabled(!readOnly);
		line.add(tempTypeField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(tempTypeField);
		tempTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionTemporary.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {optionTemporary.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {optionTemporary.setSelected(true);}
		});
		tempTypeField.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNewType=new JRadioButton(Language.tr("Surface.PickUp.Dialog.Mode.Batch")));
		optionNewType.setEnabled(!readOnly);
		line.add(newTypeField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(newTypeField);
		newTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionNewType.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {optionNewType.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {optionNewType.setSelected(true);}
		});
		newTypeField.setEditable(!readOnly);

		ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionForward);
		buttonGroup.add(optionTemporary);
		buttonGroup.add(optionNewType);

		switch (((ModelElementPickUp)element).getBatchMode()) {
		case BATCH_MODE_COLLECT:
			optionForward.setSelected(true);
			break;
		case BATCH_MODE_TEMPORARY:
			optionTemporary.setSelected(true);
			tempTypeField.setText(((ModelElementPickUp)element).getNewClientType());
			break;
		case BATCH_MODE_PERMANENT:
			optionNewType.setSelected(true);
			newTypeField.setText(((ModelElementPickUp)element).getNewClientType());
			break;
		}

		return content;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		if (optionNewType.isSelected() && newTypeField.getText().isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.PickUp.Dialog.Mode.Batch.Error.Title"),Language.tr("Surface.PickUp.Dialog.Mode.Batch.Error.Info"));
			return false;
		}

		if (optionTemporary.isSelected() && tempTypeField.getText().isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.PickUp.Dialog.SendTemporaryBatched.Error.Title"),Language.tr("Surface.PickUp.Dialog.SendTemporaryBatched.Error.Info"));
			return false;
		}

		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		((ModelElementPickUp)element).setQueueID(queueIDs[selectQueue.getSelectedIndex()]);
		((ModelElementPickUp)element).setSendAloneIfQueueEmpty(sendAloneIfQueueEmpty.isSelected());

		if (optionForward.isSelected()) ((ModelElementPickUp)element).setBatchMode(ModelElementPickUp.BatchMode.BATCH_MODE_COLLECT);
		if (optionTemporary.isSelected()) {
			((ModelElementPickUp)element).setBatchMode(ModelElementPickUp.BatchMode.BATCH_MODE_TEMPORARY);
			((ModelElementPickUp)element).setNewClientType(tempTypeField.getText());
		}
		if (optionNewType.isSelected()) {
			((ModelElementPickUp)element).setBatchMode(ModelElementPickUp.BatchMode.BATCH_MODE_PERMANENT);
			((ModelElementPickUp)element).setNewClientType(newTypeField.getText());
		}
	}
}
