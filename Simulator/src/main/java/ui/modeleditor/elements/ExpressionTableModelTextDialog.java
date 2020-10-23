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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zum Bearbeiten eines einzelnen Eintrags in einer
 * {@link ExpressionTableModelText}-Tabelle für ein
 * {@link ModelElementAnimationTextSelect}-Element.
 * @author Alexander Herzog
 * @see ExpressionTableModelText
 * @see ModelElementAnimationTextSelect
 */
public class ExpressionTableModelTextDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8571023684342497012L;

	/**
	 * Liste der globalen Variablen (zum Prüfen von Ausdrücken)
	 */
	private final String[] variableNames;

	private final JTextField expressionEdit;
	private final JTextField valueEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param expression	Bisherige Bedingung
	 * @param value	Bisher anzuzeigender Text
	 * @param variableNames	Liste der globalen Variablen (zum Prüfen von Ausdrücken)
	 * @param initialVariableValues	Initiale Variablenwerte (für Expression-Builder)
	 * @param stationIDs	Stations-IDs (für Expression-Builder)
	 * @param stationNameIDs	Stationsname (für Expression-Builder)
	 * @param helpRunnable	Hilfe-Runnable
	 */
	public ExpressionTableModelTextDialog(final Component owner, final String expression, final String value, final String[] variableNames, final Map<String,String> initialVariableValues, final Map<Integer,String> stationIDs, final Map<Integer,String> stationNameIDs, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.ExpressionTableModelText.Dialog"));
		this.variableNames=variableNames;

		Object[] data;
		JPanel line;

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.ExpressionTableModelText.Dialog.Expression")+":",expression);
		content.add(line=(JPanel)data[0]);
		expressionEdit=(JTextField)data[1];
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,true,variableNames,initialVariableValues,stationIDs,stationNameIDs,false),BorderLayout.EAST);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.ExpressionTableModelText.Dialog.Value")+":",value);
		content.add((JPanel)data[0]);
		valueEdit=(JTextField)data[1];
		valueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		setMinSizeRespectingScreensize(450,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		int error=ExpressionEval.check(expressionEdit.getText(),variableNames);
		if (error>=0) {
			ok=false;
			expressionEdit.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.ExpressionTableModel.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Dialog.Expression.Error.Info"),expressionEdit.getText(),error+1));
				return false;
			}
		} else {
			expressionEdit.setBackground(SystemColor.text);
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die neue Bedingung.
	 * @return	Neue Bedingung
	 */
	public String getExpression() {
		return expressionEdit.getText().trim();
	}

	/**
	 * Liefert den neuen anzuzeigenden Text.
	 * @return	Neuer anzuzeigender Text
	 */
	public String getValue() {
		return valueEdit.getText().trim();
	}
}
