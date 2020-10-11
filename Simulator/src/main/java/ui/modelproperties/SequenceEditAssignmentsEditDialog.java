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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zum Bearbeiten einer einzelnen Zuweisung innerhalb
 * eines Fertigungsplan-Schritts
 * @author Alexander Herzog
 * @see SequenceEditAssignmentsDialog
 */
public class SequenceEditAssignmentsEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1248512741080518581L;

	/** Editor-Model (für den Expression-Builder-Dialog) */
	private final EditModel model;

	private final JTextField keyEdit;
	private final JTextField expressionEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param key	Bisherige Nummer für ClientData(Nr)
	 * @param expression	Bisheriger Ausdruck zur Zuweisung an die ClientData(Nr)-Variable
	 * @param help	Hilfe-Runnable
	 * @param model	Editor-Model (für den Expression-Builder-Dialog)
	 */
	public SequenceEditAssignmentsEditDialog(final Component owner, final int key, final String expression, final Runnable help, final EditModel model) {
		super(owner,Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit.Title"));
		this.model=model;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout(5,0));

		Box box;

		box=Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		final JPanel panelLeft=new JPanel(new FlowLayout());
		panelLeft.add(new JLabel(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]+"("));
		panelLeft.add(keyEdit=new JTextField(5));
		panelLeft.add(new JLabel("):="));
		box.add(panelLeft);
		box.add(Box.createVerticalGlue());
		content.add(box,BorderLayout.WEST);

		expressionEdit=new JTextField();
		expressionEdit.setMaximumSize(new Dimension(expressionEdit.getMaximumSize().width,expressionEdit.getPreferredSize().height));
		box=Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		box.add(expressionEdit);
		box.add(Box.createVerticalGlue());
		content.add(box,BorderLayout.CENTER);

		if (key<0) keyEdit.setText("1"); else keyEdit.setText(""+key);
		if (expression!=null) expressionEdit.setText(expression);

		keyEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});
		expressionEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		ModelElementBaseDialog.getExpressionEditButton(content,expressionEdit,false,true,model,model.surface);

		checkData(false);

		setMinSizeRespectingScreensize(500,0);
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

		final Integer I=NumberTools.getNotNegativeInteger(keyEdit,true);
		if (I==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit.KeyError.Title"),String.format(Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit.KeyError.Info"),keyEdit.getText().trim()));
				return false;
			}
		}

		final String expression=expressionEdit.getText().trim();
		final int error=ExpressionCalc.check(expression,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
		if (error>=0) {
			expressionEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit.ExpressionError.Title"),String.format(Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit.ExpressionError.Info"),expression,error+1));
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
	 * Liefert im Erfolgsfall die neue Nummer für ClientData(Nr)
	 * @return	Neue Nummer für ClientData(Nr) oder -1, wenn der Dialog abgebrochen wurde
	 */
	public int getKey() {
		if (getClosedBy()!=CLOSED_BY_OK) return -1;
		return NumberTools.getNotNegativeInteger(keyEdit,true);
	}

	/**
	 * Liefert im Erfolgsfall den neuen Ausdruck, der an ClientData(Nr) zugewiesen werden soll
	 * @return	Neuer Ausdruck für ClientData(Nr) oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public String getExpression() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;
		return expressionEdit.getText().trim();
	}
}
