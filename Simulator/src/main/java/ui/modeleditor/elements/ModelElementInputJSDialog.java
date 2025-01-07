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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementInputJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInputJS
 */
public class ModelElementInputJSDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3995072389487163570L;

	/**
	 * Eingabefeld für den Dateinamen der Eingabedatei
	 */
	private JTextField fileNameEdit;

	/** Option: Nach Dateiende keine Zuweisungen mehr durchführen */
	private JRadioButton optionSkip;
	/** Option: Vorgabewert nach Ende der Datei für Zuweisungen verwenden */
	private JRadioButton optionDefaultValue;
	/** Option: Datei nach Ende erneut von vorne einlesen */
	private JRadioButton optionLoop;
	/** Simulation beim Erreichen des Dateiendes beenden */
	private JRadioButton optionTerminate;
	/** Vorgabewert für den Fall {@link #optionDefaultValue} */
	private JTextField defaultValueEdit;
	/** Checkbox: Datei von unten nach oben lesen */
	private JCheckBox optionReadBottomUp;

	/**
	 * Skripteditor
	 */
	private ScriptEditorPanel editor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInputJS}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementInputJSDialog(final Component owner, final ModelElementInputJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.InputJS.Dialog.Title"),element,"ModelElementInputJS",readOnly);
	}


	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInputJS;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;
		Object[] data;

		if (element instanceof ModelElementInputJS) {
			final ModelElementInputJS input=(ModelElementInputJS)element;

			/* === Upper Area === */

			final JPanel upperPanel=new JPanel();
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));
			content.add(upperPanel,BorderLayout.NORTH);

			/* Datei */
			data=getInputPanel(Language.tr("Surface.InputJS.Dialog.FileName")+":",input.getInputFile());
			upperPanel.add(line=(JPanel)data[0]);
			fileNameEdit=(JTextField)data[1];
			fileNameEdit.setEnabled(!readOnly);
			fileNameEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});

			JButton button=new JButton();
			button.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			button.setToolTipText(Language.tr("Surface.InputJS.Dialog.FileName.Select"));
			button.addActionListener(e->{
				final File newTable=Table.showLoadDialog(this,Language.tr("Surface.InputJS.Dialog.FileName.Select"));
				if (newTable!=null) {
					fileNameEdit.setText(newTable.toString());
					checkData(false);
				}
			});
			button.setEnabled(!readOnly);
			line.add(button,BorderLayout.EAST);

			/* EOF-Modus (& Default-Value) */
			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionSkip=new JRadioButton(Language.tr("Surface.InputJS.Dialog.Mode.Skip"),input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_SKIP));
			optionSkip.setEnabled(!readOnly);
			optionSkip.addActionListener(e->checkData(false));

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionDefaultValue=new JRadioButton(Language.tr("Surface.InputJS.Dialog.Mode.DefaultValue")+":",input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE));
			optionDefaultValue.setEnabled(!readOnly);
			optionDefaultValue.addActionListener(e->checkData(false));
			line.add(defaultValueEdit=new JTextField(NumberTools.formatNumber(input.getDefaultValue()),10));
			ModelElementBaseDialog.addUndoFeature(defaultValueEdit);
			defaultValueEdit.setEnabled(!readOnly);
			defaultValueEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
				@Override public void keyReleased(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
				@Override public void keyPressed(KeyEvent e) {optionDefaultValue.setSelected(true); checkData(false);}
			});

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionLoop=new JRadioButton(Language.tr("Surface.InputJS.Dialog.Mode.Loop"),input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_LOOP));
			optionLoop.setEnabled(!readOnly);
			optionLoop.addActionListener(e->checkData(false));

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionTerminate=new JRadioButton(Language.tr("Surface.InputJS.Dialog.Mode.Terminate"),input.getEofMode()==ModelElementInput.EofModes.EOF_MODE_TERMINATE));
			optionTerminate.setEnabled(!readOnly);
			optionTerminate.addActionListener(e->checkData(false));

			final ButtonGroup buttonGroup=new ButtonGroup();
			buttonGroup.add(optionSkip);
			buttonGroup.add(optionDefaultValue);
			buttonGroup.add(optionLoop);
			buttonGroup.add(optionTerminate);

			upperPanel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(optionReadBottomUp=new JCheckBox(Language.tr("Surface.InputJS.Dialog.ReadBottomToTop"),input.isReadBottomUp()));
			optionReadBottomUp.setEnabled(!readOnly);

			/* === Center Area === */

			final String script=((ModelElementInputJS)element).getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (((ModelElementInputJS)element).getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			content.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.SetJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationInput),BorderLayout.CENTER);

			/* === Start === */

			checkData(false);

		}

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,700);
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
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Datei */
		if (fileNameEdit.getText().trim().isEmpty()) {
			fileNameEdit.setBackground(Color.red);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InputJS.Dialog.FileName.ErrorTitle"),Language.tr("Surface.InputJS.Dialog.FileName.ErrorInfo"));
				return false;
			}
		} else {
			fileNameEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Vorgabewert */
		if (optionDefaultValue.isSelected()) {
			final Double D=NumberTools.getDouble(defaultValueEdit,true);
			if (D==null) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.InputJS.Dialog.DefaultValue.ErrorTitle"),String.format(Language.tr("Surface.InputJS.Dialog.DefaultValue.ErrorInfo"),defaultValueEdit.getText()));
					return false;
				}
			}
		} else {
			defaultValueEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Skript */
		if (showErrorMessage) {
			if (!editor.checkData()) return false;
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

		if (element instanceof ModelElementInputJS) {
			final ModelElementInputJS input=(ModelElementInputJS)element;

			/* Skript */
			input.setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript:
				input.setMode(ModelElementInputJS.ScriptMode.Javascript);
				break;
			case Java:
				input.setMode(ModelElementInputJS.ScriptMode.Java);
				break;
			}

			/* Datei */
			input.setInputFile(fileNameEdit.getText());

			/* EOF-Modus (& Default-Value) */
			if (optionSkip.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_SKIP);
			}
			if (optionDefaultValue.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE);
				input.setDefaultValue(NumberTools.getDouble(defaultValueEdit,true));
			}
			if (optionLoop.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_LOOP);
			}
			if (optionTerminate.isSelected()) {
				input.setEofMode(ModelElementInput.EofModes.EOF_MODE_TERMINATE);
			}

			input.setReadBottomUp(optionReadBottomUp.isSelected());
		}
	}
}
