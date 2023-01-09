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

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationPause}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationPause
 */
public class ModelElementAnimationPauseDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7445475635725548851L;

	/**
	 * Combobox: Soll nur einmal abgespielt oder immer angehalten werden?
	 */
	private JComboBox<?> onlyOneActivation;

	/**
	 * Combobox: Kundentyp (bzw. "alle") bei dem angehalten werden soll
	 */
	private JComboBox<?> clientType;

	/**
	 * Checkbox: Soll die Bedingung verwendet werden?
	 */
	private JCheckBox useCondition;

	/**
	 * Eingabefeld für die Bedingung zur Auslösung des Haltepunktes
	 */
	private JTextField condition;

	/**
	 * Auswahl für die Abstände der Haltepunkt-Auslösung
	 */
	private SpinnerModel counter;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationPause}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationPauseDialog(final Component owner, final ModelElementAnimationPause element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationPause.Dialog.Title"),element,"ModelElementAnimationPause",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		pack();
		final int h=getSize().height;
		setMinSizeRespectingScreensize(600,h);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationPause;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementAnimationPause pause=(ModelElementAnimationPause)element;
		final EditModel model=pause.getModel();
		final List<String> clientTypesNames=model.surface.getClientTypes();

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;

		data=getComboBoxPanel(Language.tr("Surface.AnimationPause.Dialog.Repetition")+":",new String[] {
				Language.tr("Surface.AnimationPause.Dialog.Repetition.OnlyOnce"),
				Language.tr("Surface.AnimationPause.Dialog.Repetition.OnEachClientArrival")
		});
		content.add((JPanel)data[0]);
		onlyOneActivation=(JComboBox<?>)data[1];
		onlyOneActivation.setSelectedIndex(pause.isOnlyOneActivation()?0:1);
		onlyOneActivation.setEnabled(!readOnly);

		data=ModelBreakPointDialog.getClientTypesComboBox(model);
		content.add((JPanel)data[0]);
		clientType=(JComboBox<?>)data[1];
		final String clientTypeName=pause.getClientType();
		if (clientTypeName.isEmpty()) {
			clientType.setSelectedIndex(0);
		} else {
			final int index=clientTypesNames.indexOf(clientTypeName);
			if (index<0) clientType.setSelectedIndex(0); else clientType.setSelectedIndex(1+index);
		}
		clientType.setEnabled(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.AnimationPause.Dialog.Condition.Use")+":",!pause.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationPause.Dialog.Condition")+":",pause.getCondition());
		content.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,model,model.surface),BorderLayout.EAST);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationPause.Dialog.CounterPart1")));
		final JSpinner counterSpinner=new JSpinner(counter=new SpinnerNumberModel(1,1,1_000_000,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(counterSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(8);
		counterSpinner.setEditor(editor);
		counterSpinner.setEnabled(!readOnly);
		line.add(counterSpinner);
		label.setLabelFor(counterSpinner);
		line.add(new JLabel(Language.tr("Surface.AnimationPause.Dialog.CounterPart2")));
		counter.setValue(Math.max(1,(int)pause.getCounter()));

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
					MsgBox.error(this,Language.tr("Surface.AnimationPause.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.AnimationPause.Dialog.Condition.Error.Info"),condition,error+1));
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

		final ModelElementAnimationPause pause=(ModelElementAnimationPause)element;
		final EditModel model=pause.getModel();
		final List<String> clientTypesNames=model.surface.getClientTypes();

		pause.setOnlyOneActivation(onlyOneActivation.getSelectedIndex()==0);
		final int clientTypeIndex=clientType.getSelectedIndex();
		if (clientTypeIndex==0) pause.setClientType(""); else pause.setClientType(clientTypesNames.get(clientTypeIndex-1));
		if (useCondition.isSelected()) pause.setCondition(condition.getText().trim()); else pause.setCondition("");
		final int count=((Integer)counter.getValue()).intValue();
		pause.setCounter((count==1)?0:count);
	}
}
