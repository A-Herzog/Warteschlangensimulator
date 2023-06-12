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
import java.io.File;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.tools.FileDropper;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementInput}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInput
 */
public class ModelElementInputDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1357526102307520065L;

	/**
	 * Eingabefeld f�r den Dateinamen der Eingabedatei
	 */
	private JTextField fileNameEdit;

	/** Option: Nach Dateiende keine Zuweisungen mehr durchf�hren */
	private JRadioButton optionSkip;
	/** Option: Vorgabewert nach Ende der Datei f�r Zuweisungen verwenden */
	private JRadioButton optionDefaultValue;
	/** Option: Datei nach Ende erneut von vorne einlesen */
	private JRadioButton optionLoop;
	/** Simulation beim Erreichen des Dateiendes beenden */
	private JRadioButton optionTerminate;
	/** Vorgabewert f�r den Fall {@link #optionDefaultValue} */
	private JTextField defaultValueEdit;
	/** Checkbox: Datei von unten nach oben lesen */
	private JCheckBox optionReadBottomUp;

	/**
	 * Eingabefeld f�r den Variablennamen an die die Zuweisung gerichtet werden soll
	 */
	private JTextField variableEdit;

	/**
	 * Zeigt wenn n�tig eine Warnung zu der dem angegebenen Variablennamen an.
	 * @see #variableEdit
	 */
	private JLabel warningLabel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInput}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementInputDialog(final Component owner, final ModelElementInput element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Input.Dialog.Title"),element,"ModelElementInput",readOnly);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(700,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInput;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;
		Object[] data;

		if (element instanceof ModelElementInput) {
			final ModelElementInput input=(ModelElementInput)element;

			final JPanel upperPanel=new JPanel();
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));
			content.add(upperPanel,BorderLayout.NORTH);

			/* Datei */
			data=getInputPanel(Language.tr("Surface.Input.Dialog.FileName")+":",input.getInputFile());
			upperPanel.add(line=(JPanel)data[0]);
			fileNameEdit=(JTextField)data[1];
			fileNameEdit.setEditable(!readOnly);
			fileNameEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});
			FileDropper.addFileDropper(this,fileNameEdit);

			JButton button=new JButton();
			button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			button.setToolTipText(Language.tr("Surface.Input.Dialog.FileName.Select"));
			button.addActionListener(e->{
				final File newTable=Table.showLoadDialog(this,Language.tr("Surface.Input.Dialog.FileName.Select"));
				if (newTable!=null) {
					fileNameEdit.setText(newTable.toString());
					checkData(false);
				}
			});
			button.setEnabled(!readOnly);
			line.add(button,BorderLayout.EAST);

			/* EOF-Modus (& Default-Value) */
			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionSkip=new JRadioButton(Language.tr("Surface.Input.Dialog.Mode.Skip"),input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_SKIP));
			optionSkip.setEnabled(!readOnly);
			optionSkip.addActionListener(e->checkData(false));

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionDefaultValue=new JRadioButton(Language.tr("Surface.Input.Dialog.Mode.DefaultValue")+":",input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE));
			optionDefaultValue.setEnabled(!readOnly);
			optionDefaultValue.addActionListener(e->checkData(false));
			line.add(defaultValueEdit=new JTextField(input.getDefaultValue(),10));
			ModelElementBaseDialog.addUndoFeature(defaultValueEdit);
			defaultValueEdit.setEditable(!readOnly);
			defaultValueEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
				@Override public void keyReleased(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
				@Override public void keyPressed(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
			});

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionLoop=new JRadioButton(Language.tr("Surface.Input.Dialog.Mode.Loop"),input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_LOOP));
			optionLoop.setEnabled(!readOnly);
			optionLoop.addActionListener(e->checkData(false));

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionTerminate=new JRadioButton(Language.tr("Surface.Input.Dialog.Mode.Terminate"),input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_TERMINATE));
			optionTerminate.setEnabled(!readOnly);
			optionTerminate.addActionListener(e->checkData(false));

			final ButtonGroup buttonGroup=new ButtonGroup();
			buttonGroup.add(optionSkip);
			buttonGroup.add(optionDefaultValue);
			buttonGroup.add(optionLoop);
			buttonGroup.add(optionTerminate);

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionReadBottomUp=new JCheckBox(Language.tr("Surface.Input.Dialog.ReadBottomToTop"),input.isReadBottomUp()));
			optionReadBottomUp.setEnabled(!readOnly);

			/* Variable */
			data=getInputPanel(Language.tr("Surface.Input.Dialog.Variable")+":",input.getVariable());
			upperPanel.add(line=(JPanel)data[0]);
			variableEdit=(JTextField)data[1];
			variableEdit.setEditable(!readOnly);
			variableEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(warningLabel=new JLabel(""));
			warningLabel.setVisible(false);

			checkData(false);

		}

		return content;
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Datei */
		if (fileNameEdit.getText().trim().isEmpty()) {
			fileNameEdit.setBackground(Color.red);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Input.Dialog.FileName.ErrorTitle"),Language.tr("Surface.Input.Dialog.FileName.ErrorInfo"));
				return false;
			}
		} else {
			fileNameEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Vorgabewert */
		if (optionDefaultValue.isSelected() && CalcSymbolClientUserData.testClientDataString(variableEdit.getText())==null) {
			final Double D=NumberTools.getDouble(defaultValueEdit,true);
			if (D==null) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Input.Dialog.DefaultValue.ErrorTitle"),String.format(Language.tr("Surface.Input.Dialog.DefaultValue.ErrorInfo"),defaultValueEdit.getText()));
					return false;
				}
			}
		} else {
			defaultValueEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
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
				if (variable.trim().equalsIgnoreCase("w")) warning=Language.tr("Surface.Input.Dialog.Variable.WaitingTime");
				if (variable.trim().equalsIgnoreCase("t")) warning=Language.tr("Surface.Input.Dialog.Variable.TransferTime");
				if (variable.trim().equalsIgnoreCase("p")) warning=Language.tr("Surface.Input.Dialog.Variable.ProcessTime");
				if (warning!=null) warningLabel.setText(warning);
				warningLabel.setVisible(warning!=null);
			}
		}
		pack();

		if (varNameOk) {
			variableEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			variableEdit.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Input.Dialog.Variable.ErrorTitle"),String.format(Language.tr("Surface.Input.Dialog.Variable.ErrorInfo"),variable));
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

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementInput) {
			final ModelElementInput input=(ModelElementInput)element;

			/* Datei */
			input.setInputFile(fileNameEdit.getText());

			/* EOF-Modus (& Default-Value) */
			if (optionSkip.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_SKIP);
			}
			if (optionDefaultValue.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE);
				input.setDefaultValue(defaultValueEdit.getText());
			}
			if (optionLoop.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_LOOP);
			}
			if (optionTerminate.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_TERMINATE);
			}

			input.setReadBottomUp(optionReadBottomUp.isSelected());

			/* Variable */
			input.setVariable(variableEdit.getText());
		}
	}
}