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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Ermöglicht es einzustellen, wie viele Bediener aus einer bestimmten Bedienergruppe
 * zur Bedienung der Kunden an einer bestimmten Station benötigt werden.
 * @author Alexander Herzog
 * @see ResourceTableModel
 */
public class ResourceTableModelDialog2 extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8062767531018749001L;

	private final int groupMax;
	private final JTextField input;

	/**
	 * Konstruktor der Klasse <code>ResourceTableModelDialog2</code>
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param help	Wird aufgerufen, wenn der Nutzer auf "Hilfe" klickt
	 * @param groupName	Name der Bedienerguppe
	 * @param groupMax	Maximal verfügbare Anzahl an Bedienern in dieser Gruppe (-1 für unbegrenzt viele)
	 * @param groupValue	Bisher gewählter Wert für den Bedarf an Bedienern
	 */
	public ResourceTableModelDialog2(final Component owner, final Runnable help, final String groupName, final int groupMax, final int groupValue) {
		super(owner,Language.tr("Surface.Resource.EditNumber.Dialog.Title"),false);
		this.groupMax=groupMax;

		JPanel panel;

		JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final Object[] data=ModelElementBaseDialog.getInputPanel("<html><body>"+String.format(Language.tr("Surface.Resource.EditNumber.Dialog.TypeName"),groupName)+":</body></html>",""+groupValue,5);
		content.add((JPanel)data[0]);
		input=(JTextField)data[1];
		input.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		if (groupMax>=0) panel.add(new JLabel("<html><body>"+String.format(Language.tr("Surface.Resource.EditNumber.Dialog.MaxNumber"),groupMax)+"</body></html>"));

		checkData(false);
		pack();
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final Integer I=NumberTools.getNotNegativeInteger(input,true);
		if (groupMax>=0) {
			if (I==null || I<1 || I>groupMax) {
				if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Resource.EditNumber.Dialog.InvalidNumber.Title"),String.format(Language.tr("Surface.Resource.EditNumber.Dialog.InvalidNumber.InfoRange"),input.getText(),groupMax));
				return false;
			}
		} else {
			if (I==null || I<1) {
				if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Resource.EditNumber.Dialog.InvalidNumber.Title"),String.format(Language.tr("Surface.Resource.EditNumber.Dialog.InvalidNumber.Info"),input.getText()));
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
	}

	/**
	 * Gibt zurück, wie viele Bediener benötigt werden, um einen Kunden an der Station bedienen zu können
	 * @return	Gewählte Anzahl an notwendigen Bedienern der Bedienergruppe
	 */
	public int getCount() {
		return NumberTools.getNotNegativeInteger(input,true);
	}
}
