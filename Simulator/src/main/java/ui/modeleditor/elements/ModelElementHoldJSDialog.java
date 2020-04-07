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

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementHoldJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementHoldJS
 */
public class ModelElementHoldJSDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 2971721070323863626L;

	private ScriptEditorPanel editor;
	private JCheckBox useTimedChecks;

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

	@Override
	protected JComponent getContentPanel() {
		if (element instanceof ModelElementHoldJS) {
			final String script=((ModelElementHoldJS)element).getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (((ModelElementHoldJS)element).getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			final JPanel content=new JPanel(new BorderLayout());
			content.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.HoldJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationHold),BorderLayout.CENTER);
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			content.add(line,BorderLayout.SOUTH);
			line.add(useTimedChecks=new JCheckBox(Language.tr("Surface.HoldJS.Dialog.TimeBasedCheck"),((ModelElementHoldJS)element).isUseTimedChecks()));
			useTimedChecks.setEnabled(!readOnly);
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
	}

	@Override
	protected boolean checkData() {
		return editor.checkData();
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
			((ModelElementHoldJS)element).setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript: ((ModelElementHoldJS)element).setMode(ModelElementHoldJS.ScriptMode.Javascript); break;
			case Java: ((ModelElementHoldJS)element).setMode(ModelElementHoldJS.ScriptMode.Java); break;
			}
			((ModelElementHoldJS)element).setUseTimedChecks(useTimedChecks.isSelected());
		}
	}
}