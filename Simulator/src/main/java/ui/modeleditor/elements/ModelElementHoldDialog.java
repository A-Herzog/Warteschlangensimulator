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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementHold}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementHold
 */
public class ModelElementHoldDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5587091033538325111L;

	/** Eingabefeld für die Bedingung */
	private JTextField condition;
	/** Option: Individuelle Prüfung pro Kunde */
	private JCheckBox clientBasedCheck;
	/** Option: Bedingung zusätzlich zeitgesteuert prüfen */
	private JCheckBox useTimedChecks;
	/** Auswahlbox für die Art der Erfassung der Verzögerungszeit */
	private JComboBox<String> processTimeType;
	/** Maximale Wartezeit verwenden? */
	private JCheckBox useMaxWaitingTime;
	/** Zahlenwert für die maximale Wartezeit */
	private JTextField maxWaitingTime;

	/** Tabelle zur Konfiguration der Prioritäten der Kundentypen */
	private PriorityTableModel tablePriorityModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementHold}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementHoldDialog(final Component owner, final ModelElementHold element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Hold.Dialog.Title"),element,"ModelElementHold",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(800,400);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationHold;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementHold hold=(ModelElementHold)element;

		final JPanel content=new JPanel(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter, tab, line;
		JLabel label;

		/* Tab "Bedingung" */
		tabs.addTab(Language.tr("Surface.Hold.Dialog.Tab.Condition"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		final Object[] data=getInputPanel(Language.tr("Surface.Hold.Dialog.Condition")+":",hold.getCondition());
		condition=(JTextField)data[1];
		condition.setEditable(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,condition,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);
		tab.add((JPanel)data[0]);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(clientBasedCheck=new JCheckBox(Language.tr("Surface.Hold.Dialog.ClientBasedCheck"),hold.isClientBasedCheck()));
		clientBasedCheck.setEnabled(!readOnly);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useTimedChecks=new JCheckBox(Language.tr("Surface.Hold.Dialog.TimeBasedCheck"),hold.isUseTimedChecks()));
		useTimedChecks.setEnabled(!readOnly);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Hold.Dialog.HoldTimeIs")));
		line.add(processTimeType=new JComboBox<>(new String[]{
				Language.tr("Surface.Hold.Dialog.HoldTimeIs.WaitingTime"),
				Language.tr("Surface.Hold.Dialog.HoldTimeIs.TransferTime"),
				Language.tr("Surface.Hold.Dialog.HoldTimeIs.ProcessTime"),
				Language.tr("Surface.Hold.Dialog.HoldTimeIs.Nothing")
		}));

		processTimeType.setEnabled(!readOnly);
		switch (hold.getDelayType()) {
		case DELAY_TYPE_WAITING: processTimeType.setSelectedIndex(0); break;
		case DELAY_TYPE_TRANSFER: processTimeType.setSelectedIndex(1); break;
		case DELAY_TYPE_PROCESS: processTimeType.setSelectedIndex(2); break;
		case DELAY_TYPE_NOTHING: processTimeType.setSelectedIndex(3); break;
		}
		label.setLabelFor(processTimeType);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useMaxWaitingTime=new JCheckBox(Language.tr("Surface.Hold.Dialog.AutoRelease")+":",hold.getMaxWaitingTime()>0));
		useMaxWaitingTime.setEnabled(!readOnly);
		useMaxWaitingTime.addActionListener(e->checkData(false));
		line.add(maxWaitingTime=new JTextField((hold.getMaxWaitingTime()>0)?NumberTools.formatNumberMax(hold.getMaxWaitingTime()):"",10));
		maxWaitingTime.setEditable(!readOnly);
		maxWaitingTime.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {useMaxWaitingTime.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {useMaxWaitingTime.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {useMaxWaitingTime.setSelected(true); checkData(false);}
		});
		line.add(new JLabel(Language.tr("Surface.XML.TimeBase.Seconds")));

		/* Tab "Prioritäten" */
		tabs.addTab(Language.tr("Surface.Hold.Dialog.Tab.Priorities"),tabOuter=new JPanel(new BorderLayout()));

		final JTableExt tablePriority;
		tabOuter.add(new JScrollPane(tablePriority=new JTableExt()),BorderLayout.CENTER);
		tablePriority.setModel(tablePriorityModel=new PriorityTableModel(tablePriority,hold,readOnly));
		tablePriority.setIsPanelCellTable(0);
		tablePriority.setIsPanelCellTable(2);
		tablePriority.getColumnModel().getColumn(0).setMaxWidth(200);
		tablePriority.getColumnModel().getColumn(0).setMinWidth(200);
		tablePriority.getColumnModel().getColumn(2).setMaxWidth(75);
		tablePriority.getColumnModel().getColumn(2).setMinWidth(75);
		tablePriority.setEnabled(!readOnly);
		tablePriority.putClientProperty("terminateEditOnFocusLost",true);
		tablePriority.getTableHeader().setReorderingAllowed(false);

		/* Icons auf Tabs */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_HOLD_PAGE_CONDITION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_HOLD_PAGE_PRIORITY.getIcon());

		/* Initiale Prüfung der Daten */
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

		final String text=condition.getText();
		if (text.trim().isEmpty()) {
			ok=false;
			condition.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Hold.Dialog.Condition.Error.Title"),Language.tr("Surface.Hold.Dialog.Condition.Error.InfoEmpty"));
				return false;
			}
		} else {
			final int error=ExpressionMultiEval.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true));
			if (error>=0) {
				ok=false;
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Hold.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Hold.Dialog.Condition.Error.Info"),text,error+1));
					return false;
				}
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		if (useMaxWaitingTime.isSelected()) {
			final Double maxWaitingTimeValue=NumberTools.getPositiveDouble(maxWaitingTime,true);
			if (maxWaitingTimeValue==null) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Hold.Dialog.AutoRelease.ErrorTitle"),String.format(Language.tr("Surface.Hold.Dialog.AutoRelease.ErrorInfo"),maxWaitingTime.getText()));
					return false;
				}
			}
		} else {
			maxWaitingTime.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (!tablePriorityModel.checkInput(showErrorMessage)) {
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
		final ModelElementHold hold=(ModelElementHold)element;
		hold.setCondition(condition.getText());
		hold.setClientBasedCheck(clientBasedCheck.isSelected());
		hold.setUseTimedChecks(useTimedChecks.isSelected());
		switch (processTimeType.getSelectedIndex()) {
		case 0: hold.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_WAITING); break;
		case 1: hold.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_TRANSFER); break;
		case 2: hold.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_PROCESS); break;
		case 3: hold.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_NOTHING); break;
		}
		if (useMaxWaitingTime.isSelected()) hold.setMaxWaitingTime(NumberTools.getPositiveDouble(maxWaitingTime,true)); else hold.setMaxWaitingTime(-1);
		tablePriorityModel.storeData();
	}
}
