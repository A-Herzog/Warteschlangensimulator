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
package ui.parameterseries;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt den Dialog zur Übertragung der Werte von einem Eingabeparameter zu einem anderen an.
 * @author Alexander Herzog
 * @see ParameterComparePanel
 * @see ParameterCompareSetup
 */
public class ParameterCompareConnectParametersDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8122348549003835059L;

	/**
	 * Modellspezifische nutzerdefinierte Funktionen
	 */
	private ExpressionCalcModelUserFunctions userFunctions;

	/**
	 * Einstellungen der Parameterreihe
	 * (werden beim Schließen mit "Ok" angepasst)
	 */
	private final ParameterCompareSetup setup;

	/**
	 * Index des zu verändernden Eingabeparameters
	 */
	private final int parameterIndex;

	/**
	 * Eingabefeld für die Formel
	 */
	private final JTextField input;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Basismodell (zum Zugriff auf die modellspezifischen nutzerdefinierten Funktionen)
	 * @param setup	Einstellungen der Parameterreihe
	 * @param parameterIndex	Index des zu verändernden Eingabeparameters
	 */
	public ParameterCompareConnectParametersDialog(final Component owner, final EditModel model, final ParameterCompareSetup setup, final int parameterIndex) {
		super(owner,Language.tr("ParameterCompare.ConnectParameters.Title"));
		this.userFunctions=model.userFunctions;
		this.setup=setup;
		this.parameterIndex=parameterIndex;

		final JPanel content=createGUI(()->Help.topicModal(this,"ParameterSeries"));
		JPanel line;

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Infozeile oben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(String.format(Language.tr("ParameterCompare.ConnectParameters.Info1"),parameterIndex+1)));

		/* Eingabezeile */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(String.format("param%d:=",parameterIndex+1)));
		line.add(Box.createHorizontalStrut(10));
		line.add(input=new JTextField(50));
		ModelElementBaseDialog.addUndoFeature(input);
		if (parameterIndex==0) input.setText("param2"); else input.setText("param1");
		input.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Infozeile unten */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("ParameterCompare.ConnectParameters.Info2")));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final StringBuilder params=new StringBuilder();
		for (int i=0;i<setup.getInput().size();i++) {
			if (i==parameterIndex) continue;
			if (params.length()>0) params.append(", ");
			params.append(String.format("param%d",i+1));
		}
		line.add(new JLabel(params.toString()));

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert ein Rechenobjekt mit registrierten Variablennamen.
	 * @return	Rechenobjekt
	 */
	private ExpressionCalc getExpressionCalc() {
		final List<String> params=new ArrayList<>();
		for (int i=0;i<setup.getInput().size();i++) params.add(String.format("param%d",i+1));

		return new ExpressionCalc(params.toArray(String[]::new),userFunctions);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final ExpressionCalc calc=getExpressionCalc();
		final int error=calc.parse(input.getText().trim());
		if (error>=0) {
			input.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("ParameterCompare.ConnectParameters.Error.Title"),String.format(Language.tr("ParameterCompare.ConnectParameters.Error.Info"),input.getText().trim(),error+1));
			}
			return false;
		} else {
			input.setBackground(NumberTools.getTextFieldDefaultBackground());
			return true;
		}
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	public void storeData() {
		final ExpressionCalc calc=getExpressionCalc();
		if (calc.parse(input.getText().trim())>=0) return;

		final List<String> inputNames=setup.getInput().stream().map(input->input.getName()).collect(Collectors.toList());
		final String setParameterName=inputNames.get(parameterIndex);

		for (ParameterCompareSetupModel model: setup.getModels()) {
			final double[] variableValues=inputNames.stream().mapToDouble(name->model.getInput().get(name)).toArray();
			try {
				final double result=calc.calc(variableValues);
				model.getInput().put(setParameterName,result);
			} catch (MathCalcError e) {
			}
		}
	}
}
