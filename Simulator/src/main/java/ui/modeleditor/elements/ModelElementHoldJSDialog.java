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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementHoldJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementHoldJS
 */
public class ModelElementHoldJSDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2971721070323863626L;

	/** Eingabebereich für das Skript */
	private ScriptEditorPanel editor;
	/** Nur bei Kundenankunft prüfen? */
	private JComboBox<String> onlyCheckOnArrival;
	/** Option: Bedingung zusätzlich zeitgesteuert prüfen */
	private JCheckBox useTimedChecks;
	/** Eingabefeld für die optionale Bedingung für die Skriptausführung */
	private JTextField condition;
	/** Auswahlbox für die Art der Erfassung der Verzögerungszeit */
	private JComboBox<String> processTimeType;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementHoldJS}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementHoldJSDialog(final Component owner, final ModelElementHoldJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.HoldJS.Dialog.Title"),element,"ModelElementHoldJS",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationHoldJS;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		if (element instanceof ModelElementHoldJS) {
			final ModelElementHoldJS holdJS=(ModelElementHoldJS)element;

			final String script=holdJS.getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (holdJS.getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			final JPanel content=new JPanel(new BorderLayout());
			content.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.HoldJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationHold),BorderLayout.CENTER);

			final JPanel setup=new JPanel();
			setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
			content.add(setup,BorderLayout.SOUTH);

			final Object[] data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.HoldJS.Dialog.Trigger")+":",Arrays.asList(
					Language.tr("Surface.HoldJS.Dialog.Trigger.StateChange"),
					Language.tr("Surface.HoldJS.Dialog.Trigger.OnlyOnArrival")
					));
			setup.add((JPanel)data[0]);
			onlyCheckOnArrival=(JComboBox<String>)data[1];
			onlyCheckOnArrival.setSelectedIndex(holdJS.isOnlyCheckOnArrival()?1:0);
			onlyCheckOnArrival.setEnabled(!readOnly);

			JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			setup.add(line);
			line.add(useTimedChecks=new JCheckBox(Language.tr("Surface.HoldJS.Dialog.TimeBasedCheck"),holdJS.isUseTimedChecks()));
			useTimedChecks.setEnabled(!readOnly);

			final Object[] obj=getInputPanel(Language.tr("Surface.HoldJS.Dialog.Condition")+":",holdJS.getCondition());
			setup.add(line=(JPanel)obj[0]);
			condition=(JTextField)obj[1];
			line.add(getExpressionEditButton(this,condition,true,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
			condition.setEnabled(!readOnly);
			condition.addKeyListener(new KeyAdapter() {
				@Override public void keyReleased(KeyEvent e) {checkCondition(false);}
			});

			JLabel label;
			setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(label=new JLabel(Language.tr("Surface.HoldJS.Dialog.HoldTimeIs")));
			line.add(processTimeType=new JComboBox<>(new String[]{
					Language.tr("Surface.HoldJS.Dialog.HoldTimeIs.WaitingTime"),
					Language.tr("Surface.HoldJS.Dialog.HoldTimeIs.TransferTime"),
					Language.tr("Surface.HoldJS.Dialog.HoldTimeIs.ProcessTime"),
					Language.tr("Surface.HoldJS.Dialog.HoldTimeIs.Nothing")
			}));

			processTimeType.setEnabled(!readOnly);
			switch (holdJS.getDelayType()) {
			case DELAY_TYPE_WAITING: processTimeType.setSelectedIndex(0); break;
			case DELAY_TYPE_TRANSFER: processTimeType.setSelectedIndex(1); break;
			case DELAY_TYPE_PROCESS: processTimeType.setSelectedIndex(2); break;
			case DELAY_TYPE_NOTHING: processTimeType.setSelectedIndex(3); break;
			}
			label.setLabelFor(processTimeType);

			return content;
		} else {
			return new JPanel();
		}
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
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

	/**
	 * Prüft die in {@link #condition} angegebene Bedingung
	 * @param showErrorMessage	Im Fehlerfall eine Meldung ausgeben?
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean checkCondition(final boolean showErrorMessage) {
		final String text=condition.getText().trim();

		if (text.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			return true;
		}

		final int error=ExpressionMultiEval.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false),element.getModel().userFunctions);
		if (error>=0) {
			condition.setBackground(Color.red);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.HoldJS.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.HoldJS.Dialog.Condition.Error.Info"),text,error+1));
			return false;
		}
		condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		return true;
	}

	@Override
	protected boolean checkData() {
		if (!editor.checkData()) return false;
		if (!checkCondition(true)) return false;

		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementHoldJS) {
			final ModelElementHoldJS holdJS=(ModelElementHoldJS)element;

			holdJS.setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript: holdJS.setMode(ModelElementHoldJS.ScriptMode.Javascript); break;
			case Java: holdJS.setMode(ModelElementHoldJS.ScriptMode.Java); break;
			}
			holdJS.setOnlyCheckOnArrival(onlyCheckOnArrival.getSelectedIndex()==1);
			holdJS.setUseTimedChecks(useTimedChecks.isSelected());
			holdJS.setCondition(condition.getText());

			switch (processTimeType.getSelectedIndex()) {
			case 0: holdJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_WAITING); break;
			case 1: holdJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_TRANSFER); break;
			case 2: holdJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_PROCESS); break;
			case 3: holdJS.setDelayType(ModelElementDelay.DelayType.DELAY_TYPE_NOTHING); break;
			}
		}
	}
}