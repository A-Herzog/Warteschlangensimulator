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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelBreakPointDialog;
import ui.tools.SoundSystem;
import ui.tools.SoundSystemPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationAlarm}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationAlarm
 */
public class ModelElementAnimationAlarmDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8756803822304491805L;

	/**
	 * Combobox: Soll der Sound nur einmal abgespielt werden?
	 */
	private JComboBox<?> onlyOneActivation;

	/**
	 * Combobox: Kundentyp (bzw. "alle") bei dem ein Sound abgespielt werden soll
	 */
	private JComboBox<?> clientType;

	/**
	 * Checkbox: Soll die Bedingung verwendet werden?
	 */
	private JCheckBox useCondition;

	/**
	 * Eingabefeld für die Bedingung zur Auslösung des Sounds
	 */
	private JTextField condition;

	/**
	 * Auswahl für die Abstände der Sound-Auslösung
	 */
	private SpinnerModel counter;

	/**
	 * Panel in dem die eigentliche Konfiguration erfolgt
	 */
	private SoundSystemPanel soundPanel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationAlarm}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationAlarmDialog(final Component owner, final ModelElementAnimationAlarm element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationAlarm.Dialog.Title"),element,"ModelElementAnimationAlarm",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		pack();
		final int h=getSize().height;
		setMinSizeRespectingScreensize(800,h);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationAlarm;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementAnimationAlarm alarm=(ModelElementAnimationAlarm)element;
		final EditModel model=alarm.getModel();
		final List<String> clientTypesNames=model.surface.getClientTypes();

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;

		/* Bedingungen */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition")+"</b></body></html>"));

		data=getComboBoxPanel(Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.Repeat")+":",new String[] {
				Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.Repeat.OnlyOnce"),
				Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.Repeat.Multi")
		});
		content.add((JPanel)data[0]);
		onlyOneActivation=(JComboBox<?>)data[1];
		onlyOneActivation.setSelectedIndex(alarm.isOnlyOneActivation()?0:1);

		data=ModelBreakPointDialog.getClientTypesComboBox(model);
		content.add((JPanel)data[0]);
		clientType=(JComboBox<?>)data[1];
		final String clientTypeName=alarm.getClientType();
		if (clientTypeName.isEmpty()) {
			clientType.setSelectedIndex(0);
		} else {
			final int index=clientTypesNames.indexOf(clientTypeName);
			if (index<0) clientType.setSelectedIndex(0); else clientType.setSelectedIndex(1+index);
		}

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.UseCondition")+":",!alarm.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.Condition")+":",alarm.getCondition());
		content.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,model,model.surface),BorderLayout.EAST);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.CounterPart1")));
		final JSpinner counterSpinner=new JSpinner(counter=new SpinnerNumberModel(1,1,1_000_000,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(counterSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(8);
		counterSpinner.setEditor(editor);
		line.add(counterSpinner);
		label.setLabelFor(counterSpinner);
		line.add(new JLabel(Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.CounterPart2")));
		counter.setValue(Math.max(1,(int)alarm.getCounter()));

		/* Sound */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.AnimationAlarm.Dialog.Section.Sound")+"</b></body></html>"));

		content.add(soundPanel=new SoundSystemPanel(alarm.getSound(),alarm.getSoundMaxSeconds(),readOnly));

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		final EditModel model=element.getModel();

		boolean ok=true;

		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.Condition.Error.Title"),String.format(Language.tr("Surface.AnimationAlarm.Dialog.Section.Condition.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
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

		final ModelElementAnimationAlarm alarm=(ModelElementAnimationAlarm)element;
		final EditModel model=alarm.getModel();
		final List<String> clientTypesNames=model.surface.getClientTypes();

		alarm.setOnlyOneActivation(onlyOneActivation.getSelectedIndex()==0);
		final int clientTypeIndex=clientType.getSelectedIndex();
		if (clientTypeIndex==0) alarm.setClientType(""); else alarm.setClientType(clientTypesNames.get(clientTypeIndex-1));
		if (useCondition.isSelected()) alarm.setCondition(condition.getText().trim()); else alarm.setCondition("");
		final int count=((Integer)counter.getValue()).intValue();
		alarm.setCounter((count==1)?0:count);

		alarm.setSound(soundPanel.getSound(),soundPanel.getMaxSeconds());
	}

	@Override
	public void setVisible(final boolean visible) {
		if (!visible) SoundSystem.getInstance().stopSoundFile();
		super.setVisible(visible);
	}
}
