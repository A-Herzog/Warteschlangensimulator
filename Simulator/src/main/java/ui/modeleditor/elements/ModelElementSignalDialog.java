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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSignal}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSignal
 */
public class ModelElementSignalDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8113881062196153457L;

	/**
	 * Name der Station beim Aufrufen des Dialogs<br>
	 * (um ggf. beim Schließen des Dialogs das Modell zu benachrichtigen, dass sich der Signalname verändert hat)
	 */
	private final String oldName;

	/**
	 * Eingabefeld für die Verzögerung bei der Signalauslösung
	 */
	private JTextField signalDelay;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSignal}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSignalDialog(final Component owner, final ModelElementSignal element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Signal.Dialog.Title"),element,"ModelElementSignal",readOnly,false);
		oldName=element.getName();
		setVisible(true);
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final Object[] data=getInputPanel(Language.tr("Surface.Signal.Dialog.DelayedExecution")+":",NumberTools.formatNumberMax(((ModelElementSignal)element).getSignalDelay()),7);

		final JPanel panel=(JPanel)data[0];
		signalDelay=(JTextField)data[1];
		signalDelay.setEditable(!readOnly);
		signalDelay.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		panel.add(new JLabel(" ("+Language.tr("Statistic.Seconds")+")"));

		checkData(false);

		return panel;
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
		setSize(getWidth(),getHeight()+(int)Math.round(30*windowScaling));
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSignal;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final Double D=NumberTools.getNotNegativeDouble(signalDelay,true);
		if (D==null) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Signal.Dialog.DelayedExecution.ErrorTitle"),Language.tr("Surface.Signal.Dialog.DelayedExecution.ErrorInfo"));
			return false;
		}
		return true;
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

		if (!oldName.equals(element.getName())) {
			element.getSurface().objectRenamed(oldName,element.getName(),ModelDataRenameListener.RenameType.RENAME_TYPE_SIGNAL,true);
		}

		((ModelElementSignal)element).setSignalDelay(NumberTools.getNotNegativeDouble(signalDelay,true));
	}
}
