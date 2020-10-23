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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dieser Dialog erlaubt das Bearbeiten eines Audrucks und einer Farbe f�r einen
 * Teildatensatz von {@link ModelElementAnimationBarStack}. Der Dialog wird
 * von {@link BarStackTableModel} aufgerufen.
 * @author Alexander Herzog
 * @see ModelElementAnimationBarStack
 * @see BarStackTableModel
 */
public class BarStackTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -110037828381567665L;

	/**
	 * Liste mit allen im Modell zur Verf�gung stehenden Variablennamen (zur Pr�fung des Ausdr�cks)
	 */
	private final String[] variableNames;

	private final JTextField editExpression;
	private final SmallColorChooser colorChooserBar;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param expression	Bisheriger Ausdruck
	 * @param color	Bisherige Farbe
	 * @param variableNames	Liste mit allen im Modell zur Verf�gung stehenden Variablennamen (zur Pr�fung des Ausdr�cks)
	 * @param model	Gesamtes Modell (f�r den Expression-Builder)
	 * @param surface	Haupt-Zeichenfl�che (f�r den Expression-Builder)
	 */
	public BarStackTableModelDialog(final Component owner, final Runnable help, final String expression, final Color color, final String[] variableNames, final EditModel model, final ModelSurface surface) {
		super(owner,Language.tr("Surface.AnimationBarStack.Dialog.Edit"));
		this.variableNames=variableNames;

		JPanel line;
		JLabel label;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarStack.Dialog.Expression")+":","");
		content.add((JPanel)data[0]);
		editExpression=(JTextField)data[1];
		editExpression.setEditable(!readOnly);
		((JPanel)data[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,editExpression,false,false,model,surface),BorderLayout.EAST);
		editExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		editExpression.setText((expression==null)?"":expression);

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationBarStack.Dialog.BarColor")+":"));
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooserBar=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserBar.setEnabled(!readOnly);
		label.setLabelFor(colorChooserBar);
		colorChooserBar.setColor((color==null)?Color.RED:color);

		pack();
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final String text=editExpression.getText().trim();
		if (text.isEmpty()) {
			ok=false;
			editExpression.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.Expression.Error.Title"),Language.tr("Surface.AnimationBar.Dialog.Expression.ErrorNoExpression.Info"));
				return false;
			}
		} else {
			int error=ExpressionCalc.check(text,variableNames);
			if (error>=0) {
				ok=false;
				editExpression.setBackground(Color.red);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationBar.Dialog.Expression.ErrorInvalidExpression.Info"),text,error+1));
					return false;
				}
			} else {
				editExpression.setBackground(SystemColor.text);
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wird den neuen Ausdruck
	 * @return	Neuer Ausdruck
	 */
	public String getExpression() {
		return editExpression.getText();
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wird die neue Farbe
	 * @return	Neue Farbe
	 */
	public Color getColor() {
		return colorChooserBar.getColor();
	}
}
