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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSub}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 2643627718628007295L;

	private JTextField inputField;
	private JTextField outputField;

	private JTextArea description;

	private boolean openEditor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSub}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSubDialog(final Component owner, final ModelElementSub element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Sub.Dialog.Title"),element,"ModelElementSub",readOnly,false);
		setVisible(true);
	}

	@Override
	protected void initUserButtons() {
		addUserButton(Language.tr("Surface.Sub.Dialog.OkAndOpenEditor"),Language.tr("Surface.Sub.Dialog.OkAndOpenEditor.Tooltip"),Images.MODELEDITOR_ELEMENT_SUB_EDIT.getURL());
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,600);
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSub;
	}

	@Override
	protected JComponent getContentPanel() {
		openEditor=false;

		final JPanel content=new JPanel();
		content.setLayout(new BorderLayout());

		if (element instanceof ModelElementSub) {
			JPanel top, main, sub;
			Object[] data;

			content.add(top=new JPanel(),BorderLayout.NORTH);
			top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));

			data=getInputPanel(Language.tr("Surface.Sub.Dialog.NumberInput")+":",""+((ModelElementSub)element).getInputCount(),3);
			top.add((JPanel)data[0]);
			inputField=(JTextField)data[1];
			inputField.setEditable(!readOnly);
			inputField.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});

			data=getInputPanel(Language.tr("Surface.Sub.Dialog.NumberOutput")+":",""+((ModelElementSub)element).getOutputCount(),3);
			top.add((JPanel)data[0]);
			outputField=(JTextField)data[1];
			outputField.setEditable(!readOnly);
			outputField.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});

			content.add(main=new JPanel(new BorderLayout()),BorderLayout.CENTER);
			main.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			sub.add(new JLabel(Language.tr("Surface.Sub.Dialog.Description")+":"));

			main.add(new JScrollPane(description=new JTextArea(((ModelElementSub)element).getDescription())),BorderLayout.CENTER);
			description.setEditable(!readOnly);
			addUndoFeature(description);
		}
		return content;
	}

	/**
	 * Versucht basierend auf einer auslaufenden Kante das nächste Element zu finden
	 * @param connection	Aus dem Start-Element auslaufende Kante (<code>null</code> wird erkannt und als Fehler gemeldet)
	 * @return	Liefert die ID des nächsten Elements oder -1, wenn es kein Ziel gibt
	 */
	protected final int findNextId(final ModelElementEdge connection) {
		if (connection==null) return -1;
		ModelElement element=connection.getConnectionEnd();
		if (element==null) return -1;
		return element.getId();
	}

	/**
	 * Versucht basierend auf einer einlaufenden Kante das vorherige Element zu finden
	 * @param connection	In das Start-Element einlaufende Kante (<code>null</code> wird erkannt und als Fehler gemeldet)
	 * @return	Liefert die ID des vorherigen Elements oder -1, wenn es keine Quelle gibt
	 */
	protected final int findPreviousId(final ModelElementEdge connection) {
		if (connection==null) return -1;
		ModelElement element=connection.getConnectionStart();
		if (element==null) return -1;
		return element.getId();
	}

	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;
		Long L;

		L=NumberTools.getPositiveLong(inputField,true);
		if (L==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Sub.Dialog.NumberInput.Error.Title"),String.format(Language.tr("Surface.Sub.Dialog.NumberInput.Error.Info"),inputField.getText()));
				return false;
			}
			ok=false;
		}

		L=NumberTools.getPositiveLong(outputField,true);
		if (L==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Sub.Dialog.NumberOutput.Error.Title"),String.format(Language.tr("Surface.Sub.Dialog.NumberOutput.Error.Info"),outputField.getText()));
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

		if (element instanceof ModelElementSub) {
			((ModelElementSub)element).setInputCount((int)((long)NumberTools.getPositiveLong(inputField,true)));
			((ModelElementSub)element).setOutputCount((int)((long)NumberTools.getPositiveLong(outputField,true)));
			((ModelElementSub)element).setDescription(description.getText());
		}
	}

	@Override
	protected void userButtonClick(int nr, JButton button) {
		openEditor=true;
		SwingUtilities.invokeLater(()->close(CLOSED_BY_OK));
	}

	/**
	 * Gibt an, ob der Nutzer ausgewählt hat, dass nach dem Schließen dieses Dialogs der Untermodell-Editor geöffnet werden soll.
	 * @return	Liefert <code>true</code>, wenn nach dem Schließen dieses Dialogs der Untermodell-Editor geöffnet werden soll.
	 */
	public boolean getOpenEditor() {
		return openEditor;
	}

}