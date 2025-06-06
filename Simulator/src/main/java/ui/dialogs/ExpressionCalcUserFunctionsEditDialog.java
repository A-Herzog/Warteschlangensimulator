/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalcInfo;
import simulator.simparser.ExpressionCalcUserFunctionsManager;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.expressionbuilder.ExpressionBuilder;
import ui.expressionbuilder.ExpressionBuilderAutoComplete;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog zum Bearbeiten einer einzelnen nutzerdefinierten Rechenfunktionen.
 * @author Alexander Herzog
 * @see ExpressionCalcUserFunctionsListDialog
 * @see ExpressionCalcUserFunctionsManager
 */
public class ExpressionCalcUserFunctionsEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1843677887327688322L;

	/**
	 * Namen aller eingebauten Symbole in Kleinbuchstaben
	 */
	private final Set<String> symbolNamesLower;

	/**
	 * Liste der nutzerdefinierten Funktionen (ggf. inkl. der aktuell zu bearbeitenden)
	 */
	private final List<ExpressionCalcUserFunctionsManager.UserFunction> userFunctions;

	/**
	 * Name der nutzerdefinierten Funktion vor dem Aufruf des Dialogs<br>
	 * (Dieser Name aus {@link #userFunctions} darf folglich weiter verwendet werden.)
	 */
	private final String allowedUsedName;

	/**
	 * Eingabefeld f�r den Namen der Funktion
	 */
	private final JTextField nameEdit;

	/**
	 * Auswahlfeld f�r die Anzahl an Parametern;
	 */
	private final SpinnerModel parameterCountEdit;

	/**
	 * Option: Funktion ist Rechenausdruck
	 */
	private final JRadioButton optionExpression;

	/**
	 * Option: Funktion ist Skript
	 */
	private final JRadioButton optionScript;

	/**
	 * Eingabefeld f�r den Rechenbefehl, der beim Aufruf der Funktion ausgef�hrt werden soll
	 */
	private final JTextField contentEdit;

	/**
	 * Skript-Eingabefeld
	 */
	private final ScriptEditorPanel scriptEditor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param userFunctions	Liste aller vorhandenen Nutzerfunktionen (zur Bestimmung, welche Namen bereits vergeben sind)
	 * @param userFunction	Zu bearbeitende Nutzerfunktion (kann <code>null</code> sein)
	 */
	public ExpressionCalcUserFunctionsEditDialog(final Component owner, final List<ExpressionCalcUserFunctionsManager.UserFunction> userFunctions, final ExpressionCalcUserFunctionsManager.UserFunction userFunction) {
		super(owner,Language.tr("UserDefinedFunctions.EditTitle"));

		/* Eingebaute Symbole ermitteln */
		final ExpressionCalcInfo symbolInfo=new ExpressionCalcInfo(true);
		symbolNamesLower=symbolInfo.getAllNames().stream().map(name->name.toLowerCase()).collect(Collectors.toSet());

		/* Nutzerdefinierte Funktionen */
		this.userFunctions=userFunctions;
		if (userFunction==null) allowedUsedName=null; else allowedUsedName=userFunction.name;

		/* GUI */
		final JPanel content=createGUI(()->Help.topicModal(this,"ExpressionsUser"));
		content.setLayout(new BorderLayout());

		final JPanel main=new JPanel();
		content.add(main,BorderLayout.NORTH);
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));

		Object[] data;
		JPanel line;
		JLabel label;

		/* Name der Funktion */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("UserDefinedFunctions.Edit.Name")+":",getValidFunctionName(),20);
		main.add((JPanel)data[0]);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		/* Anzahl an Parametern */
		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("UserDefinedFunctions.Edit.NumberOfParameters")+":"));
		final JSpinner parameterCountSpinner=new JSpinner(parameterCountEdit=new SpinnerNumberModel(1,0,9,1));
		final JSpinner.NumberEditor parameterCountEditorField=new JSpinner.NumberEditor(parameterCountSpinner);
		parameterCountEditorField.getFormat().setGroupingUsed(false);
		parameterCountEditorField.getTextField().setColumns(2);
		parameterCountSpinner.setEditor(parameterCountEditorField);
		line.add(parameterCountSpinner);
		label.setLabelFor(parameterCountSpinner);
		parameterCountEdit.setValue((userFunction==null)?1:userFunction.parameterCount);
		parameterCountEdit.addChangeListener(e->checkData(false));

		/* Option: Funktion ist Rechenausdruck */
		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionExpression=new JRadioButton("<html><body><b>"+Language.tr("UserDefinedFunctions.Edit.ModeExpression")+"</b></body></html>",userFunction==null || userFunction.mode==ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION));

		/* Rechenbefehl */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("UserDefinedFunctions.Edit.CalculationCommand")+":",(userFunction==null || userFunction.mode!=ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION)?"WIP()+Parameter1":userFunction.content);
		main.add(line=(JPanel)data[0]);
		contentEdit=(JTextField)data[1];
		contentEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {
				optionExpression.setSelected(true);
				checkData(false);
			}
		});
		line.add(buildExpressionBuilderButton(),BorderLayout.EAST);

		/* Option: Funktion ist Skript */
		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionScript=new JRadioButton("<html><body><b>"+Language.tr("UserDefinedFunctions.Edit.ModeScript")+"</b></body></html>",userFunction!=null && userFunction.mode!=ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION));

		/* Skripteditor */
		final String script=(userFunction==null || userFunction.mode==ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION)?"Output.print(Parameter1+3);":userFunction.content;
		final ScriptEditorPanel.ScriptMode mode=(userFunction!=null && userFunction.mode==ExpressionCalcUserFunctionsManager.UserFunctionMode.JAVA)?ScriptEditorPanel.ScriptMode.Java:ScriptEditorPanel.ScriptMode.Javascript;
		content.add(scriptEditor=new ScriptEditorPanel(script,mode,false,null,null,null,ScriptEditorPanel.featuresOutputOnly),BorderLayout.CENTER);
		scriptEditor.addKeyActionListener(()->optionScript.setSelected(true));

		/* Optionen verkn�pfen */
		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionExpression);
		buttonGroup.add(optionScript);

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,600);
		setSizeRespectingScreensize(600,500);
		pack();
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erzeugt eine ExpressionBuilder-Schaltfl�che und definiert dabei Variablen
	 * f�r die eingestellte Anzahl an Parametern dynamisch beim Aufruf des Dialogs.
	 * @return	ExpressionBuilder-Schaltfl�che
	 */
	public JButton buildExpressionBuilderButton() {
		final JButton button=new JButton();
		button.setToolTipText(Language.tr("Editor.DialogBase.ExpressionEditTooltip"));
		button.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		button.addActionListener(e->{
			final String[] variableNames=new String[(Integer)parameterCountEdit.getValue()];
			for (int i=0;i<variableNames.length;i++) variableNames[i]="Parameter"+(i+1);
			final ExpressionBuilder dialog=new ExpressionBuilder(owner,contentEdit.getText(),false,variableNames,null,null,null,true,false,false,null,false);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				contentEdit.setText(dialog.getExpression());
				checkData(false);
			}
		});
		final Dimension size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));

		ExpressionBuilderAutoComplete.process(new ExpressionBuilder(owner,contentEdit.getText(),false,null,null,null,null,true,false,false,null,false),contentEdit);

		return button;
	}

	/**
	 * Erstellt initial einen g�ltigen Namen f�r die nutzerdefinierte Funktion
	 * (in dem es entweder den �bergebenen Namen der bisherigen Funktion liefert
	 * oder einen neuen Namen sucht).
	 * @return	Initialer Name f�r die nutzerdefinierte Funktion
	 */
	private String getValidFunctionName() {
		if (allowedUsedName!=null) return allowedUsedName;

		String name="userFunction";
		if (testFunctionName(name)) return name;

		int i=1;
		while (!testFunctionName(name+i)) i++;
		return name+i;
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Name */
		if (testFunctionName(nameEdit.getText())) {
			nameEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			nameEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("UserDefinedFunctions.Edit.NameErrorTitle"),String.format(Language.tr("UserDefinedFunctions.Edit.NameErrorInfo"),nameEdit.getText().trim()));
				return false;
			}
		}

		if (optionExpression.isSelected()) {
			/* Rechenbefehl */
			final int error=ExpressionCalcUserFunctionsManager.test((Integer)parameterCountEdit.getValue(),contentEdit.getText().trim());
			if (error<0) {
				contentEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				contentEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("UserDefinedFunctions.Edit.CalculationCommandErrorTitle"),String.format(Language.tr("UserDefinedFunctions.Edit.CalculationCommandErrorInfo"),contentEdit.getText().trim(),error+1));
					return false;
				}
			}
		}

		return ok;
	}

	/**
	 * Pr�ft, ob ein Name f�r eine nutzerdefinierte Funktion g�ltig ist.
	 * @param name	Zu pr�fender Name
	 * @return	Liefert <code>true</code>, wenn der Name verwendet werden kann
	 */
	private boolean testFunctionName(String name) {
		if (name==null) return false;
		name=name.trim();

		/* Allgemein ung�ltige Namen */
		if (name.isEmpty()) return false;
		if (name.contains("\t")) return false;
		final char c=name.charAt(0);
		if (!((c>='a' && c<='z') || (c>='A' && c<='Z'))) return false;

		/* Bisheriger Name (sofern vorhanden) ist nat�rlich in Ordnung */
		if (allowedUsedName!=null && allowedUsedName.equalsIgnoreCase(name)) return true;

		/* Kollisionen mit Parameterbezeichnern */
		if (name.toLowerCase().startsWith("parameter")) return false;

		/* Vergleich mit eingebauten Funktionen */
		if (symbolNamesLower.contains(name.toLowerCase())) return false;

		/* Vergleich mit anderen nutzerdefinierten Funktionen */
		final String nameFinal=name;
		if (userFunctions.stream().filter(userFunction->userFunction.name.equalsIgnoreCase(nameFinal)).findFirst().isPresent()) return false;

		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die neue nutzerdefinierte Funktion (nur g�ltig nach dem Schlie�en des Dialogs per "Ok").
	 * @return	Neue nutzerdefinierte Funktion
	 */
	public ExpressionCalcUserFunctionsManager.UserFunction getUserFunction() {
		final String name=nameEdit.getText().trim();
		final int count=(Integer)parameterCountEdit.getValue();

		if (optionExpression.isSelected()) {
			return new ExpressionCalcUserFunctionsManager.UserFunction(name,count,contentEdit.getText().trim(),ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION);
		}

		if (optionScript.isSelected()) {
			final ExpressionCalcUserFunctionsManager.UserFunctionMode mode;
			switch (scriptEditor.getMode()) {
			case Javascript:
				mode=ExpressionCalcUserFunctionsManager.UserFunctionMode.JAVASCRIPT;
				break;
			case Java:
				mode=ExpressionCalcUserFunctionsManager.UserFunctionMode.JAVA;
				break;
			default:
				mode=ExpressionCalcUserFunctionsManager.UserFunctionMode.JAVASCRIPT;
				break;
			}
			return new ExpressionCalcUserFunctionsManager.UserFunction(name,count,scriptEditor.getScript(),mode);
		}

		return null;
	}
}
