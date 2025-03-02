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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zur Konfiguration von Nebenbedingungen für die Optimierung.
 * @author Alexander Herzog
 * @see OptimizerPanel
 * @see OptimizerSetup#controlVariableConstrains
 */
public class OptimizerPanelConstrainsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5697018498521665681L;

	/** Modellspezifische nutzerdefinierte Funktionen */
	private final ExpressionCalcModelUserFunctions userFunctions;
	/** Liste der Nebenbedingungen (aus dieser Liste wird beim Start geladen und in diese Liste werden die neuen Bedingungen beim Schließen zurückgeschrieben) */
	private final List<String> constrains;
	/** Eingabefeld für die Nebenbedingungen */
	private final JTextArea textArea;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Basismodell (zum Zugriff auf die modellspezifischen nutzerdefinierten Funktionen)
	 * @param constrains	Liste der Nebenbedingungen; beim Schließen des Dialogs mit "Ok" wird direkt diese Liste verändert
	 */
	public OptimizerPanelConstrainsDialog(final Component owner, final EditModel model, final List<String> constrains) {
		super(owner,Language.tr("Optimizer.Tab.ControlVariables.Constrains.DialogTitle"));
		this.userFunctions=model.userFunctions;
		this.constrains=constrains;

		/* Dialog aufbauen */
		final JPanel content=createGUI(600,500,()->Help.topicModal(this,"Optimizer"));
		content.setLayout(new BorderLayout());

		final JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(top,BorderLayout.NORTH);

		final String info="<html><body>"+Language.tr("Optimizer.Tab.ControlVariables.Constrains.DialogInfo").replace("\n","<br>")+"</body></html>";

		top.add(new JLabel(info));
		content.add(new JScrollPane(textArea=new JTextArea(String.join("\n",constrains))),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(textArea);

		/* Dialog sichtbar machen */
		setSizeRespectingScreensize(600,500);
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * Erstellt eine Liste mit den Nebenbedingungen basierend auf dem Eingabefeld
	 * @return	Liste mit den Nebenbedingungen
	 */
	private List<String> getConstrainsFromGUI() {
		final List<String> newConstrains=new ArrayList<>();
		for (String line: textArea.getText().split("\n")) if (!line.trim().isEmpty()) newConstrains.add(line.trim());
		return newConstrains;
	}

	@Override
	protected boolean checkData() {
		final List<String> varsMaxList=new ArrayList<>();
		for (int i=0;i<1_000;i++) varsMaxList.add(String.format("Var%d",i+1));
		final String[] varsMaxArray=varsMaxList.toArray(String[]::new);

		final List<String> newConstrains=getConstrainsFromGUI();
		for (int i=0;i<newConstrains.size();i++) {
			final String constrain=newConstrains.get(i);
			final ExpressionMultiEval eval=new ExpressionMultiEval(varsMaxArray,userFunctions);
			final int error=eval.parse(constrain);
			if (error>=0) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.ControlVariables.Constrains.DialogErrorTitle"),String.format(Language.tr("Optimizer.Tab.ControlVariables.Constrains.DialogErrorInfo"),i+1,constrain,error+1));
				return false;
			}
		}
		return true;
	}

	@Override
	protected void storeData() {
		constrains.clear();
		constrains.addAll(getConstrainsFromGUI());
	}
}
