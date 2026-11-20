/**
 * Copyright 2026 Alexander Herzog
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurface.TimeBase;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDropDuplicates}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDropDuplicates
 */
public class ModelElementDropDuplicatesDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8796772729725452706L;

	/**
	 * Index des Kundendatenfeldes über das der Abgleich erfolgen soll
	 */
	private SpinnerModel clientDataIndex;

	/**
	 * Für wie lange sollen die IDs gespeichert werden?
	 * @see #timeBase
	 */
	private JTextField storageTime;

	/**
	 * Zeiteinheit für {@link #storageTime}
	 * @see #storageTime
	 */
	private JComboBox<String> timeBase;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDropDuplicates}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDropDuplicatesDialog(final Component owner, final ModelElementDropDuplicates element, final boolean readOnly) {
		super(owner,Language.tr("Surface.DropDuplicates.Dialog.Title"),element,"ModelElementDropDuplicates",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		setMaxSizeRespectingScreensize(600,1000);
		pack();
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
		setMaxSizeRespectingScreensize(600,1000);
		setSize(getWidth(),getHeight()+(int)Math.round(25*windowScaling));
	}


	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDropDuplicates;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementDropDuplicates dropDuplicatesElement=(ModelElementDropDuplicates)element;

		final JPanel content=new JPanel(new BorderLayout());
		final JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.NORTH);

		JPanel line;

		/* Kundendatenfeld-Index */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel label=new JLabel(Language.tr("Surface.DropDuplicates.Dialog.ClientDataIndex")+":");
		line.add(label);
		final JSpinner spinner=new JSpinner(clientDataIndex=new SpinnerNumberModel(0,0,999,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(4);
		spinner.setEditor(editor);
		editor.setEnabled(!readOnly);
		spinner.setEnabled(!readOnly);
		clientDataIndex.setValue(dropDuplicatesElement.getClientDataIndex());
		line.add(spinner);

		/* Speicherzeitdauer */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DropDuplicates.Dialog.StorageTime"),NumberTools.formatNumberMax(dropDuplicatesElement.getStorageTime()),10);
		setup.add(line=(JPanel)data[0]);
		storageTime=(JTextField)data[1];
		storageTime.setEnabled(!readOnly);
		storageTime.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(Box.createHorizontalStrut(10));
		line.add(timeBase=new JComboBox<>(TimeBase.getNames()));
		timeBase.setEnabled(!readOnly);
		timeBase.setSelectedIndex(dropDuplicatesElement.getTimeBase().id);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		final Double D=NumberTools.getPositiveDouble(storageTime,true);
		if (D==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.DropDuplicates.Dialog.StorageTime.ErrorTitle"),Language.tr("Surface.DropDuplicates.Dialog.StorageTime.ErrorInfo"));
				return false;
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

		final ModelElementDropDuplicates dropDuplicatesElement=(ModelElementDropDuplicates)element;

		final int index=(Integer)clientDataIndex.getValue();
		dropDuplicatesElement.setClientDataIndex(index);

		dropDuplicatesElement.setStorageTime(NumberTools.getPositiveDouble(storageTime,true));

		dropDuplicatesElement.setTimeBase(ModelSurface.TimeBase.byId(timeBase.getSelectedIndex()));
	}
}
