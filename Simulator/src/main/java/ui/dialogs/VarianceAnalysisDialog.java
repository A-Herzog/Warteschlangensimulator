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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.parameterseries.ParameterComparePanel;

/**
 * Ermöglicht die Angabe der Anzahl an zu simulierenden Wiederholungen bei einer Varianzanalyse
 * @author Alexander Herzog
 * @see ParameterComparePanel#setupVarianceAnalysis(int)
 */
public class VarianceAnalysisDialog extends BaseDialog {
	private static final long serialVersionUID = -4704662661702815976L;

	private final JTextField edit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public VarianceAnalysisDialog(final Component owner) {
		super(owner,Language.tr("ParameterCompare.Settings.VarianceAnalysis.Title"));
		final JPanel all=createGUI(()->Help.topic(this,"ParameterSeries"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalVarianceAnalysis);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Eingabefeld */

		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Settings.VarianceAnalysis.Label")+":","100",7);
		content.add((JPanel)obj[0]);
		edit=(JTextField)obj[1];
		edit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */

		setMinSizeRespectingScreensize(500,175);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final Long L=NumberTools.getPositiveLong(edit,true);
		if (L==null) {
			if (showErrorMessages) {
				MsgBox.info(this,Language.tr("ParameterCompare.Settings.VarianceAnalysis.ErrorTitle"),String.format(Language.tr("ParameterCompare.Settings.VarianceAnalysis.ErrorInfo"),edit.getText()));
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
	 * Liefert die eingestellte Anzahl an Wiederholungen
	 * @return	Anzahl an Wiederholungen
	 * @see ParameterComparePanel#setupVarianceAnalysis(int)
	 */
	public int getRepeatCount() {
		final Long L=NumberTools.getPositiveLong(edit,true);
		return (int)((long)L);
	}
}
