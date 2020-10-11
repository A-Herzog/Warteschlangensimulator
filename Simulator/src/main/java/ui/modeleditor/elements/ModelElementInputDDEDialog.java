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
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementInputDDE}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInputDDE
 */
public class ModelElementInputDDEDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4609646068661729008L;

	private DDEEditPanel editDDE;
	private JRadioButton optionSkip;
	private JRadioButton optionDefaultValue;
	private JRadioButton optionLoop;
	private JRadioButton optionTerminate;
	private JTextField defaultValueEdit;
	private JTextField variableEdit;
	private JLabel warningLabel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInputDDE}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementInputDDEDialog(final Component owner, final ModelElementInputDDE element, final boolean readOnly) {
		super(owner,Language.tr("Surface.InputDDE.Dialog.Title"),element,"ModelElementInputDDE",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(700,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInputDDE;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementInputDDE input=(ModelElementInputDDE)this.element;

		JPanel line;
		Object[] data;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* DDE */

		content.add(editDDE=new DDEEditPanel(this,input,readOnly,helpRunnable));

		/* EOF-Modus (& Default-Value) */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionSkip=new JRadioButton(Language.tr("Surface.InputDDE.Dialog.Mode.Skip"),input.getEofMode()==ModelElementInputDDE.EofModes.EOF_MODE_SKIP));
		optionSkip.setEnabled(!readOnly);
		optionSkip.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionDefaultValue=new JRadioButton(Language.tr("Surface.InputDDE.Dialog.Mode.DefaultValue")+":",input.getEofMode()==ModelElementInputDDE.EofModes.EOF_MODE_DEFAULT_VALUE));
		optionDefaultValue.setEnabled(!readOnly);
		optionDefaultValue.addActionListener(e->checkData(false));
		line.add(defaultValueEdit=new JTextField(input.getDefaultValue(),10));
		defaultValueEdit.setEditable(!readOnly);
		defaultValueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionLoop=new JRadioButton(Language.tr("Surface.InputDDE.Dialog.Mode.Loop"),input.getEofMode()==ModelElementInputDDE.EofModes.EOF_MODE_LOOP));
		optionLoop.setEnabled(!readOnly);
		optionLoop.addActionListener(e->checkData(false));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTerminate=new JRadioButton(Language.tr("Surface.InputDDE.Dialog.Mode.Terminate"),input.getEofMode()==ModelElementInputDDE.EofModes.EOF_MODE_TERMINATE));
		optionTerminate.setEnabled(!readOnly);
		optionTerminate.addActionListener(e->checkData(false));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionSkip);
		buttonGroup.add(optionDefaultValue);
		buttonGroup.add(optionLoop);
		buttonGroup.add(optionTerminate);

		/* Variable */

		data=getInputPanel(Language.tr("Surface.InputDDE.Dialog.Variable")+":",input.getVariable());
		content.add(line=(JPanel)data[0]);
		variableEdit=(JTextField)data[1];
		variableEdit.setEditable(!readOnly);
		variableEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(warningLabel=new JLabel(""));
		warningLabel.setVisible(false);

		/* Start */

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		/* DDE */

		if (!editDDE.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		/* Vorgabewert */

		if (optionDefaultValue.isSelected() && CalcSymbolClientUserData.testClientDataString(variableEdit.getText())==null) {
			final Double D=NumberTools.getDouble(defaultValueEdit,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.InputDDE.Dialog.DefaultValue.ErrorTitle"),String.format(Language.tr("Surface.InputDDE.Dialog.DefaultValue.ErrorInfo"),defaultValueEdit.getText()));
					return false;
				}
			}
		} else {
			defaultValueEdit.setBackground(SystemColor.text);
		}

		/* Variable */

		final String variable=variableEdit.getText();
		boolean varNameOk=true;
		if (CalcSymbolClientUserData.testClientData(variable)>=0) {
			warningLabel.setVisible(false);
			/* varNameOk bleibt true */
		} else {
			if (CalcSymbolClientUserData.testClientDataString(variable)!=null) {
				warningLabel.setVisible(false);
				/* varNameOk bleibt true */
			} else {
				varNameOk=ExpressionCalc.checkVariableName(variable);
				String warning=null;
				if (variable.trim().equalsIgnoreCase("w")) warning=Language.tr("Surface.InputDDE.Dialog.Variable.WaitingTime");
				if (variable.trim().equalsIgnoreCase("t")) warning=Language.tr("Surface.InputDDE.Dialog.Variable.TransferTime");
				if (variable.trim().equalsIgnoreCase("p")) warning=Language.tr("Surface.InputDDE.Dialog.Variable.ProcessTime");
				if (warning!=null) warningLabel.setText(warning);
				warningLabel.setVisible(warning!=null);
			}
		}
		pack();

		if (varNameOk) {
			variableEdit.setBackground(SystemColor.text);
		} else {
			variableEdit.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.InputDDE.Dialog.Variable.ErrorTitle"),String.format(Language.tr("Surface.InputDDE.Dialog.Variable.ErrorInfo"),variable));
				return false;
			}
			ok=false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementInputDDE input=(ModelElementInputDDE)this.element;

		/* DDE */

		editDDE.storeData();

		/* EOF-Modus (& Default-Value) */

		if (optionSkip.isSelected()) {
			input.setEofMode(ModelElementInputDDE.EofModes.EOF_MODE_SKIP);
		}
		if (optionDefaultValue.isSelected()) {
			input.setEofMode(ModelElementInputDDE.EofModes.EOF_MODE_DEFAULT_VALUE);
			input.setDefaultValue(defaultValueEdit.getText());
		}
		if (optionLoop.isSelected()) {
			input.setEofMode(ModelElementInputDDE.EofModes.EOF_MODE_LOOP);
		}
		if (optionTerminate.isSelected()) {
			input.setEofMode(ModelElementInputDDE.EofModes.EOF_MODE_TERMINATE);
		}

		/* Variable */

		input.setVariable(variableEdit.getText());
	}
}