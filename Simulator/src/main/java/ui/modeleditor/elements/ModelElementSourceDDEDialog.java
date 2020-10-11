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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSourceDDE}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSourceDDE
 */
public class ModelElementSourceDDEDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2479508586936324612L;

	private DDEEditPanel editDDE;
	private JTextArea clientsEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSourceDDE}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSourceDDEDialog(final Component owner, final ModelElementSourceDDE element, final boolean readOnly) {
		super(owner,Language.tr("Surface.SourceDDE.Dialog.Title"),element,"ModelElementSourceDDE",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(550,600);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSourceDDE;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementSourceDDE source=(ModelElementSourceDDE)this.element;

		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;

		/* Oben: DDE-Daten */

		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));

		top.add(editDDE=new DDEEditPanel(this,source,readOnly,helpRunnable));

		/* Center: Kundentypnamen */

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.SourceDDE.Dialog.ClientTypes")+":"));

		content.add(new JScrollPane(clientsEdit=new JTextArea("")),BorderLayout.CENTER);
		clientsEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		clientsEdit.setText(String.join("\n",source.getNewClientTypes()).trim());
		clientsEdit.setEditable(!readOnly);
		addUndoFeature(clientsEdit);

		/* Start */

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		if (!editDDE.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		if (clientsEdit.getText().trim().isEmpty()) {
			clientsEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.SourceDDE.Dialog.ClientTypes.ErrorTitle"),Language.tr("Surface.SourceDDE.Dialog.ClientTypes.ErrorInfo"));
				return false;
			}
		} else {
			clientsEdit.setBackground(SystemColor.text);
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSourceDDE source=(ModelElementSourceDDE)this.element;

		editDDE.storeData();

		final String s=clientsEdit.getText().trim();
		final String[] lines=s.split("\n");
		source.getClientTypeNames().clear();
		for (String line: lines) if (!line.trim().isEmpty()) source.getClientTypeNames().add(line.trim());
	}
}
