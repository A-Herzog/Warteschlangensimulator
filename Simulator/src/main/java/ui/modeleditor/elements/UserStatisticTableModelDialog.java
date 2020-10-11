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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dieser Dialog erlaubt das Bearbeiten eines einzelnen Datensatzes
 * aus einer {@link UserStatisticTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see UserStatisticTableModel
 */
public class UserStatisticTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2104982799396808067L;

	private String[] variableNames;
	private JTextField key;
	private JCheckBox isTime;
	private JTextField expression;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param key	Bisheriger Bezeichner für die Kenngröße
	 * @param isTime	Handelt es sich um eine Zeitangabe (<code>true</code>) oder eine Zahl (<code>false</code>)
	 * @param expression	Bisherige zu erfassende Kenngröße
	 * @param model	Gesamtes Modell (für den Expression-Builder)
	 * @param surface	Haupt-Zeichenfläche (für den Expression-Builder)
	 */
	public UserStatisticTableModelDialog(final Component owner, final Runnable help, final String key, final boolean isTime, final String expression, final EditModel model, final ModelSurface surface) {
		super(owner,Language.tr("Surface.UserStatistic.Table.Edit"));

		variableNames=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true);

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] obj;

		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.UserStatistic.Table.Edit.Key"),key);
		content.add((JPanel)obj[0]);
		this.key=(JTextField)obj[1];
		this.key.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.UserStatistic.Table.Edit.Expression"),expression);
		content.add((JPanel)obj[0]);
		this.expression=(JTextField)obj[1];
		this.expression.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});
		((JPanel)obj[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,this.expression,false,true,model,surface),BorderLayout.EAST);

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(this.isTime=new JCheckBox(Language.tr("Surface.UserStatistic.Table.Edit.IsTime"),isTime));

		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (key.getText().trim().isEmpty()) {
			key.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.UserStatistic.Table.Edit.Key.ErrorTitle"),Language.tr("Surface.UserStatistic.Table.Edit.Key.ErrorInfo"));
				return false;
			}
		} else {
			key.setBackground(SystemColor.text);
		}

		if (expression.getText().trim().isEmpty()) {
			expression.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorTitle"),Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorInfoEmpty"));
				return false;
			}
		} else {
			final int error=ExpressionCalc.check(expression.getText().trim(),variableNames);
			if (error<0) {
				expression.setBackground(SystemColor.text);
			} else {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorTitle"),String.format(Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorInfoInvalid"),expression.getText().trim(),error));
					return false;
				}
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so liefert diese Methode den neuen Bezeichner für die Kenngrößenerfassung.
	 * @return	Neuer Bezeichner für die Kenngrößenerfassung
	 */
	public String getKey() {
		return key.getText().trim();
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so liefert diese Methode die Information darüber,
	 * ob es sich bei der Kenngröße um eine Zeitangabe oder um eine Zahl handelt.
	 * @return	Kenngröße ist Zeit (<code>true</code>) oder Zahl (<code>false</code>)
	 */
	public boolean getIsTime() {
		return isTime.isSelected();
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so liefert diese Methode den neuen Rechenausdruck für die Kenngrößenerfassung.
	 * @return	Neuer Rechenausdruck für die Kenngrößenerfassung
	 */
	public String getExpression() {
		return expression.getText().trim();
	}
}
