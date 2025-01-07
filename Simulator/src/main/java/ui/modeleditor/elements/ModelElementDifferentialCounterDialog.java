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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDifferentialCounter}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDifferentialCounter
 */
public class ModelElementDifferentialCounterDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8074769774808895018L;

	/** Eingabefeld für die Änderung des Zählers, wenn ein Kunde die Station passiert */
	private JTextField change;

	/** Eingabebereich für die Bedingungen unter denen die Zählung erfolgen soll */
	private CounterConditionPanel counterConditionPanel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDifferentialCounter}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDifferentialCounterDialog(final Component owner, final ModelElementDifferentialCounter element, final boolean readOnly) {
		super(owner,Language.tr("Surface.DifferentialCounter.Dialog.Title"),element,"ModelElementDifferentialCounter",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDifferentialCounter;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementDifferentialCounter counterElement=(ModelElementDifferentialCounter)element;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final Object[] data=getInputPanel(Language.tr("Surface.DifferentialCounter.Dialog.Increment")+":",""+counterElement.getChange(),7);
		change=(JTextField)data[1];
		change.setEnabled(!readOnly);
		change.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		content.add((JPanel)data[0]);

		content.add(counterConditionPanel=new CounterConditionPanel(element.getModel(),element.getSurface(),readOnly));
		counterConditionPanel.setData(counterElement.getCondition());

		checkData(false);

		return content;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;
		boolean ok=true;

		Integer I=NumberTools.getInteger(change,true);
		if (I==null || I==0) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.DifferentialCounter.Dialog.Increment.Error.Title"),Language.tr("Surface.DifferentialCounter.Dialog.Increment.Error.Info"));
				return false;
			}
			ok=false;
		}

		if (!counterConditionPanel.checkData(showErrorMessage)) {
			if (showErrorMessage) return false;
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
		final ModelElementDifferentialCounter counterElement=(ModelElementDifferentialCounter)element;

		counterElement.setChange(NumberTools.getInteger(change,true));

		counterConditionPanel.getData(counterElement.getCondition());
	}
}