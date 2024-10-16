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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementCounter}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementCounter
 */
public class ModelElementCounterDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8074769774808895018L;

	/** Eingabefeld f�r den Gruppennamen des Z�hlers */
	private JComboBox<?> groupName;

	/** Eingabebereich f�r die Bedingungen unter denen die Z�hlung erfolgen soll */
	private CounterConditionPanel counterConditionPanel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementCounter}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementCounterDialog(final Component owner, final ModelElementCounter element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Counter.Dialog.Title"),element,"ModelElementCounter",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationCounter;
	}

	/**
	 * Liefert eine Liste der Z�hler-Gruppennamen
	 * @param surface	Zeichenfl�che, die durchsucht werden soll (Unter-Zeichenfl�chen werden ebenfalls durchsucht)
	 * @return	Liste der Z�hler-Gruppennamen
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

	@Override
	protected JComponent getContentPanel() {
		final ModelElementCounter counterElement=(ModelElementCounter)element;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final Object[] data=getComboBoxPanel(Language.tr("Surface.Counter.Dialog.GroupName")+":",counterElement.getGroupName(),getCounterGroupNames(element.getModel().surface));
		groupName=(JComboBox<?>)data[1];
		groupName.setEnabled(!readOnly);
		groupName.getEditor().getEditorComponent().setEnabled(!readOnly);
		groupName.addKeyListener(new KeyListener() {
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

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;
		boolean ok=true;

		final String text=((String)groupName.getEditor().getItem()).trim();
		if (text.isEmpty()) {
			groupName.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Counter.Dialog.GroupName.Error.Title"),Language.tr("Surface.Counter.Dialog.GroupName.Error.Info"));
				return false;
			}
			ok=false;
		}
		groupName.setBackground(NumberTools.getTextFieldDefaultBackground());

		if (!counterConditionPanel.checkData(showErrorMessage)) {
			if (showErrorMessage) return false;
			ok=false;
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		final ModelElementCounter counterElement=(ModelElementCounter)element;

		counterElement.setGroupName(((String)groupName.getEditor().getItem()).trim());

		counterConditionPanel.getData(counterElement.getCondition());
	}
}