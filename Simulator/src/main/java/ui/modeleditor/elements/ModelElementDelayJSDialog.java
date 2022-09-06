/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDelayJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDelayJS
 */
public class ModelElementDelayJSDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7195066641904878464L;

	/**
	 * Liste aller globalen Variablen in dem Modell
	 */
	private String[] variables;

	/**
	 * Auswahlbox für die Art der Erfassung der Verzögerungszeit
	 */
	private JComboBox<String> processTimeType;

	/**
	 * Editor für das Skript
	 */
	private ScriptEditorPanel editor;

	/**
	 * Eingabefeld für die Kosten pro Bedienvorgang
	 */
	private JTextField textCosts;

	/**
	 * Soll eine Liste der Kunden an der Station geführt werden?
	 */
	private JCheckBox hasClientsList;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDelay}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDelayJSDialog(final Component owner, final ModelElementDelayJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.DelayJS.Dialog.Title"),element,"ModelElementDelayJS",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDelay;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected JComponent getContentPanel() {
		if (!(element instanceof ModelElementDelayJS)) return new JPanel();
		final ModelElementDelayJS delayJS=(ModelElementDelayJS)element;

		variables=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true);

		final JPanel content=new JPanel(new BorderLayout());
		JPanel sub;

		/* Erfassung der Verzögerungszeit */
		JLabel label;
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.DelayJS.Dialog.DelayTimeIs")));
		sub.add(processTimeType=new JComboBox<>(new String[]{
				Language.tr("Surface.DelayJS.Dialog.DelayTimeIs.WaitingTime"),
				Language.tr("Surface.DelayJS.Dialog.DelayTimeIs.TransferTime"),
				Language.tr("Surface.DelayJS.Dialog.DelayTimeIs.ProcessTime"),
				Language.tr("Surface.DelayJS.Dialog.DelayTimeIs.Nothing")
		}));
		processTimeType.setEnabled(!readOnly);
		switch (delayJS.getDelayType()) {
		case DELAY_TYPE_WAITING: processTimeType.setSelectedIndex(0); break;
		case DELAY_TYPE_TRANSFER: processTimeType.setSelectedIndex(1); break;
		case DELAY_TYPE_PROCESS: processTimeType.setSelectedIndex(2); break;
		case DELAY_TYPE_NOTHING: processTimeType.setSelectedIndex(3); break;
		}
		label.setLabelFor(processTimeType);

		/* Skriptedsitor */
		final String script=delayJS.getScript();
		ScriptEditorPanel.ScriptMode mode;
		switch (delayJS.getMode()) {
		case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
		case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
		default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
		}
		content.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.DelayJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationOutput),BorderLayout.CENTER);

		/* Bereich unten */
		final JPanel bottom=new JPanel();
		bottom.setLayout(new BoxLayout(bottom,BoxLayout.PAGE_AXIS));
		content.add(bottom,BorderLayout.SOUTH);

		/* Kosten */
		final Object[] data=getInputPanel(Language.tr("Surface.DelayJS.Dialog.CostsPerClient")+":",delayJS.getCosts());
		textCosts=(JTextField)data[1];
		bottom.add(sub=(JPanel)data[0]);
		textCosts.setEditable(!readOnly);
		textCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,textCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		/* Kundenliste führen? */
		bottom.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(hasClientsList=new JCheckBox(Language.tr("Surface.DelayJS.Dialog.HasClientsList"),delayJS.hasClientsList()));
		hasClientsList.setToolTipText(Language.tr("Surface.DelayJS.Dialog.HasClientsList.Tooltip"));

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

		ok=ok && editor.checkData();

		final String text=textCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				textCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.DelayJS.Dialog.CostsPerClient.Error.Title"),String.format(Language.tr("Surface.DelayJS.Dialog.CostsPerClient.Error.Info"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			textCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
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

		if (!(element instanceof ModelElementDelayJS)) return;
		final ModelElementDelayJS delayJS=(ModelElementDelayJS)element;

		switch (processTimeType.getSelectedIndex()) {
		case 0: delayJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_WAITING); break;
		case 1: delayJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_TRANSFER); break;
		case 2: delayJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_PROCESS); break;
		case 3: delayJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_NOTHING); break;
		}

		delayJS.setScript(editor.getScript());
		switch (editor.getMode()) {
		case Javascript: delayJS.setMode(ModelElementDecideJS.ScriptMode.Javascript); break;
		case Java: delayJS.setMode(ModelElementDecideJS.ScriptMode.Java); break;
		}

		delayJS.setCosts(textCosts.getText());
		delayJS.setHasClientsList(hasClientsList.isSelected());
	}
}
