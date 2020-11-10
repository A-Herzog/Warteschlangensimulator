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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementCounterMulti}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementCounterMulti
 */
public class ModelElementCounterMultiDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6583652937859431659L;

	/**
	 * Auswahl des Gruppennamens für den Mehrfachzähler
	 */
	private JComboBox<String> groupName;

	/**
	 * Tabelle zur Konfiguration der Teil-Zähler
	 */
	private CounterMultiTableModel model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementCounterMulti}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementCounterMultiDialog(final Component owner, final ModelElementCounterMulti element, final boolean readOnly) {
		super(owner,Language.tr("Surface.CounterMulti.Dialog.Title"),element,"ModelElementCounterMulti",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationCounterMulti;
	}

	/**
	 * Liefert eine Liste der Zähler-Gruppennamen
	 * @param surface	Zeichenfläche, die durchsucht werden soll (Unter-Zeichenflächen werden ebenfalls durchsucht)
	 * @return	Liste der Zähler-Gruppennamen
	 * @see #groupName
	 */
	private Set<String> getCounterGroupNames(final ModelSurface surface) {
		final Set<String> groupNames=new HashSet<>();
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementCounter) {
				final String group=((ModelElementCounter)element).getGroupName().trim();
				if (!group.isEmpty()) groupNames.add(group);
			}
			if (element instanceof ModelElementSub) {
				groupNames.addAll(getCounterGroupNames(((ModelElementSub)element).getSubSurface()));
			}
		}
		return groupNames;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final ModelElementCounterMulti counterMulti=(ModelElementCounterMulti)element;

		final JPanel content=new JPanel(new BorderLayout());

		Object[] data;

		final JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.NORTH);

		data=getComboBoxPanel(Language.tr("Surface.Counter.Dialog.GroupName")+":",counterMulti.getGroupName(),getCounterGroupNames(element.getModel().surface));
		setup.add((JPanel)data[0]);
		groupName=(JComboBox<String>)data[1];
		groupName.setEnabled(!readOnly);
		groupName.getEditor().getEditorComponent().setEnabled(!readOnly);
		groupName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=CounterMultiTableModel.buildTable(counterMulti,readOnly);
		content.add((JScrollPane)data[0],BorderLayout.CENTER);
		model=(CounterMultiTableModel)data[1];

		checkData(false);

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

		/* Gruppenname */
		final String text=((String)groupName.getEditor().getItem()).trim();
		if (text.isEmpty()) {
			ok=false;
			groupName.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.CounterMulti.Dialog.GroupName.Error.Title"),Language.tr("Surface.CounterMulti.Dialog.GroupName.Error.Info"));
				return false;
			}
		}
		groupName.setBackground(SystemColor.text);

		/* Bedingungen und Zählernamen */
		if (!model.checkData(showErrorMessage)) ok=false;

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

		final ModelElementCounterMulti counter=(ModelElementCounterMulti)element;

		/* Gruppenname */
		counter.setGroupName(((String)groupName.getEditor().getItem()).trim());

		/* Bedingungen und Zählernamen */
		model.storeData();
	}
}
