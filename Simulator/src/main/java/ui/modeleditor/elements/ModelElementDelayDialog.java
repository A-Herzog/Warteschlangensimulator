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
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
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
	private static final long serialVersionUID = 491234735571463778L;

	private JComboBox<String> timeBase;
	private JComboBox<String> processTimeType;
	private DistributionOrExpressionByClientTypeEditor distributions;
	private JTextField textCosts;

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

		JPanel sub;
		JLabel label;

		/* Auswahl der Zeitbasis */
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Delay.Dialog.TimeBase")+":"));
		sub.add(timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase.setEnabled(!readOnly);
		timeBase.setSelectedIndex(((ModelElementDelay)element).getTimeBase().id);
		label.setLabelFor(timeBase);

		sub.add(label=new JLabel(Language.tr("Surface.Delay.Dialog.DelayTimeIs")));
		sub.add(processTimeType=new JComboBox<>(new String[]{
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.WaitingTime"),
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.TransferTime"),
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.ProcessTime"),
				Language.tr("Surface.Delay.Dialog.DelayTimeIs.Nothing")
		}));

		processTimeType.setEnabled(!readOnly);
		switch (((ModelElementDelay)element).getDelayType()) {
		case DELAY_TYPE_WAITING: processTimeType.setSelectedIndex(0); break;
		case DELAY_TYPE_TRANSFER: processTimeType.setSelectedIndex(1); break;
		case DELAY_TYPE_PROCESS: processTimeType.setSelectedIndex(2); break;
		case DELAY_TYPE_NOTHING: processTimeType.setSelectedIndex(3); break;
		}
		label.setLabelFor(processTimeType);

		/* Daten aus Element laden */
		content.add(distributions=new DistributionOrExpressionByClientTypeEditor(element.getModel(),element.getSurface(),readOnly,Language.tr("Surface.Delay.Dialog.DelayDistribution"),Language.tr("Surface.Delay.Dialog.DelayExpression")),BorderLayout.CENTER);
		distributions.setData(((ModelElementDelay)element).getDelayTime(),((ModelElementDelay)element).getDelayExpression());
		for (String clientType : distributions.getClientTypes()) distributions.setData(clientType,((ModelElementDelay)element).getDelayTime(clientType),((ModelElementDelay)element).getDelayExpression(clientType));


		/* Kosten */
		final Object[] data=getInputPanel(Language.tr("Surface.Delay.Dialog.CostsPerClient")+":",NumberTools.formatNumber(((ModelElementDelay)element).getCosts()),10);
		textCosts=(JTextField)data[1];
		content.add(sub=(JPanel)data[0],BorderLayout.SOUTH);
		textCosts.setEditable(!readOnly);
		textCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* GUI aufbauen */
		distributions.start();

		return content;
	}

	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		final Double D=NumberTools.getDouble(textCosts,true);
		if (D==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Delay.Dialog.CostsPerClient.Error.Title"),String.format(Language.tr("Surface.Delay.Dialog.CostsPerClient.Error.Info"),textCosts.getText()));
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
		((ModelElementDelay)element).setTimeBase(ModelSurface.TimeBase.byId(timeBase.getSelectedIndex()));
		((ModelElementDelay)element).setDelayTime(distributions.getGlobalDistribution(),distributions.getGlobalExpression());
		switch (processTimeType.getSelectedIndex()) {
		case 0: ((ModelElementDelay)element).setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_WAITING); break;
		case 1: ((ModelElementDelay)element).setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_TRANSFER); break;
		case 2: ((ModelElementDelay)element).setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_PROCESS); break;
		case 3: ((ModelElementDelay)element).setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_NOTHING); break;
		}
		for(Map.Entry<String,AbstractRealDistribution> entry: distributions.getDistributions().entrySet()) ((ModelElementDelay)element).setDelayTime(entry.getKey(),entry.getValue(),null);
		for(Map.Entry<String,String> entry: distributions.getExpressions().entrySet()) ((ModelElementDelay)element).setDelayTime(entry.getKey(),null,entry.getValue());
		((ModelElementDelay)element).setCosts(NumberTools.getDouble(textCosts,true));
	}
}
