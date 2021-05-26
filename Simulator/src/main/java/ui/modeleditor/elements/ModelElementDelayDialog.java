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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDelay}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDelay
 */
public class ModelElementDelayDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 491234735571463778L;

	/** Liste aller globalen Variablen in dem Modell */
	private String[] variables;

	/** Auswahlbox für die Zeitbasis */
	private JComboBox<String> timeBase;
	/** Auswahlbox für die Art der Erfassung der Verzögerungszeit */
	private JComboBox<String> processTimeType;
	/** Konfiguration der Verzögerungszeit (Verteilungseditor oder Eingabefeld für Ausdruck) */
	private DistributionOrExpressionByClientTypeEditor distributions;
	/** Eingabefeld für die Kosten pro Bedienvorgang */
	private JTextField textCosts;
	/** Soll eine Liste der Kunden an der Station geführt werden? */
	private JCheckBox hasClientsList;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDelay}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDelayDialog(final Component owner, final ModelElementDelay element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Delay.Dialog.Title"),element,"ModelElementDelay",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(650,650);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDelay;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		final ModelElementDelay delayElement=(ModelElementDelay)element;
		variables=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true);

		JPanel sub;
		JLabel label;

		/* Auswahl der Zeitbasis */
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Delay.Dialog.TimeBase")+":"));
		sub.add(timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase.setEnabled(!readOnly);
		timeBase.setSelectedIndex(delayElement.getTimeBase().id);
		label.setLabelFor(timeBase);

		sub.add(label=new JLabel(Language.tr("Surface.Delay.Dialog.DelayTimeIs")));
		sub.add(processTimeType=new JComboBox<>(new String[]{
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.WaitingTime"),
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.TransferTime"),
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.ProcessTime"),
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.Nothing")
		}));

		processTimeType.setEnabled(!readOnly);
		switch (delayElement.getDelayType()) {
		case DELAY_TYPE_WAITING: processTimeType.setSelectedIndex(0); break;
		case DELAY_TYPE_TRANSFER: processTimeType.setSelectedIndex(1); break;
		case DELAY_TYPE_PROCESS: processTimeType.setSelectedIndex(2); break;
		case DELAY_TYPE_NOTHING: processTimeType.setSelectedIndex(3); break;
		}
		label.setLabelFor(processTimeType);

		/* Daten aus Element laden */
		content.add(distributions=new DistributionOrExpressionByClientTypeEditor(element.getModel(),element.getSurface(),readOnly,Language.tr("Surface.Delay.Dialog.DelayDistribution"),Language.tr("Surface.Delay.Dialog.DelayExpression")),BorderLayout.CENTER);
		distributions.setData(delayElement.getDelayTime(),delayElement.getDelayExpression());
		for (String clientType: distributions.getClientTypes()) distributions.setData(clientType,delayElement.getDelayTime(clientType),delayElement.getDelayExpression(clientType));

		/* Bereich unten */
		final JPanel bottom=new JPanel();
		bottom.setLayout(new BoxLayout(bottom,BoxLayout.PAGE_AXIS));
		content.add(bottom,BorderLayout.SOUTH);

		/* Kosten */
		final Object[] data=getInputPanel(Language.tr("Surface.Delay.Dialog.CostsPerClient")+":",delayElement.getCosts());
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
		sub.add(hasClientsList=new JCheckBox(Language.tr("Surface.Delay.Dialog.HasClientsList"),delayElement.hasClientsList()));
		hasClientsList.setToolTipText(Language.tr("Surface.Delay.Dialog.HasClientsList.Tooltip"));

		/* GUI aufbauen */
		distributions.start();

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

		final String text=textCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				textCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Delay.Dialog.CostsPerClient.Error.Title"),String.format(Language.tr("Surface.Delay.Dialog.CostsPerClient.Error.Info"),text,error+1));
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

		final ModelElementDelay delayElement=(ModelElementDelay)element;

		delayElement.setTimeBase(ModelSurface.TimeBase.byId(timeBase.getSelectedIndex()));
		delayElement.setDelayTime(distributions.getGlobalDistribution(),distributions.getGlobalExpression());
		switch (processTimeType.getSelectedIndex()) {
		case 0: delayElement.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_WAITING); break;
		case 1: delayElement.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_TRANSFER); break;
		case 2: delayElement.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_PROCESS); break;
		case 3: delayElement.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_NOTHING); break;
		}
		for(Map.Entry<String,AbstractRealDistribution> entry: distributions.getDistributions().entrySet()) delayElement.setDelayTime(entry.getKey(),entry.getValue(),null);
		for(Map.Entry<String,String> entry: distributions.getExpressions().entrySet()) delayElement.setDelayTime(entry.getKey(),null,entry.getValue());
		delayElement.setCosts(textCosts.getText());
		delayElement.setHasClientsList(hasClientsList.isSelected());
	}
}
