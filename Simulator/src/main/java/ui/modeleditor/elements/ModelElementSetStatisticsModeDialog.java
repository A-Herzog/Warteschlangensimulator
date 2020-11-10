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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSetStatisticsMode}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSetStatisticsMode
 */
public class ModelElementSetStatisticsModeDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3485729583844072517L;

	/** Option: Statistikerfassung einschalten */
	private JRadioButton optionModeOn;
	/** Option: Statistikerfassung ausschalten */
	private JRadioButton optionModeOff;
	/** Option: Statistikerfassung gemäß Bedingung {@link #editCondition} ein- oder ausschalten */
	private JRadioButton optionModeCondition;
	/** Eingabefeld für die Bedingung zum Ein- oder Aussschalten der Statistik im Fall {@link #optionModeCondition} */
	private JTextField editCondition;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSetStatisticsMode}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSetStatisticsModeDialog(final Component owner, final ModelElementSetStatisticsMode element, final boolean readOnly) {
		super(owner,Language.tr("Surface.SetStatisticsMode.Dialog.Title"),element,"ModelElementSetStatisticsMode",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(650,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSetStatisticsMode;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));

		JPanel line;

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.SetStatisticsMode.Dialog.Mode")+"</b></body></html>"));

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionModeOn=new JRadioButton(Language.tr("Surface.SetStatisticsMode.Dialog.Mode.On")));
		optionModeOn.addActionListener(e->checkData(false));
		optionModeOn.setEnabled(!readOnly);

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionModeOff=new JRadioButton(Language.tr("Surface.SetStatisticsMode.Dialog.Mode.Off")));
		optionModeOff.addActionListener(e->checkData(false));
		optionModeOff.setEnabled(!readOnly);

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionModeCondition=new JRadioButton(Language.tr("Surface.SetStatisticsMode.Dialog.Mode.Condition")));
		optionModeCondition.addActionListener(e->checkData(false));
		optionModeCondition.setEnabled(!readOnly);

		final Object[] data=getInputPanel(Language.tr("Surface.SetStatisticsMode.Dialog.Condition")+":",((ModelElementSetStatisticsMode)element).getCondition());
		top.add((JPanel)data[0]);
		editCondition=(JTextField)data[1];
		editCondition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionModeCondition.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionModeCondition.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionModeCondition.setSelected(true); checkData(false);}
		});
		editCondition.setEditable(!readOnly);
		((JPanel)data[0]).add(getExpressionEditButton(this,editCondition,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionModeOn);
		buttonGroup.add(optionModeOff);
		buttonGroup.add(optionModeCondition);

		switch (((ModelElementSetStatisticsMode)element).getMode()) {
		case ON: optionModeOn.setSelected(true); break;
		case OFF: optionModeOff.setSelected(true); break;
		case CONDITION: optionModeCondition.setSelected(true); break;
		default: optionModeOn.setSelected(true); break;
		}

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (optionModeCondition.isSelected()) {
			final String condition=editCondition.getText().trim();
			final int error=ExpressionMultiEval.check(condition,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				ok=false;
				editCondition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.SetStatisticsMode.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.SetStatisticsMode.Dialog.Condition.Error.Info"),condition,error+1));
					return false;
				}
			} else {
				editCondition.setBackground(SystemColor.text);
			}
		} else {
			editCondition.setBackground(SystemColor.text);
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

		if (optionModeOn.isSelected()) {
			((ModelElementSetStatisticsMode)element).setMode(ModelElementSetStatisticsMode.Mode.ON);
		}

		if (optionModeOff.isSelected()) {
			((ModelElementSetStatisticsMode)element).setMode(ModelElementSetStatisticsMode.Mode.OFF);
		}

		if (optionModeCondition.isSelected()) {
			((ModelElementSetStatisticsMode)element).setMode(ModelElementSetStatisticsMode.Mode.CONDITION);
			((ModelElementSetStatisticsMode)element).setCondition(editCondition.getText());
		}
	}
}
