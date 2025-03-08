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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Ermöglicht die Bearbeitung einer Zuweisung.
 * @author Alexander Herzog
 * @see VariablesTableModel
 */
public class VariablesTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4069400013879691172L;

	/** Liste aller bisher vorhandenen Variablennamen */
	private final String[] names;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private final ExpressionCalcModelUserFunctions modelUserFunctions;
	/** Nur clientdata(...)-Zuweisungen zulassen und keine globalen Variablen? */
	private final boolean clientDataOnly;
	/** Eingabefeld für den Variablennamen */
	private final JTextField variable;
	/** Warnung, die angezeigt wird, wenn {@link #variable} einen ungültigen Variablennamen enthält */
	private final JLabel warningLabel;
	/** Option: Ausdruck auswerten und zuweisen */
	private final JRadioButton optionExpression;
	/** Eingabefeld für den Rechenausdruck im Fall {@link #optionExpression} */
	private final JTextField expression;
	/** Option: Bisherige Wartezeit des aktuellen Kunden zuweisen */
	private final JRadioButton optionWaiting;
	/** Option: Bisherige Transferzeit des aktuellen Kunden zuweisen */
	private final JRadioButton optionTransfer;
	/** Option: Bisherige Bedienzeit des aktuellen Kunden zuweisen */
	private final JRadioButton optionProcess;
	/** Option: Bisherige Verweilzeit des aktuellen Kunden zuweisen */
	private final JRadioButton optionResidence;
	/** Option: Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden? */
	private final JCheckBox checkAskForValueOnStart;

	/**
	 * Konstruktor der Klasse <code>VariablesTableModelDialog</code>
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param help	Wird aufgerufen, wenn der Nutzer auf "Hilfe" klickt
	 * @param variable	Bisheriger Name der Variable (kann "" für neue Einträge sein)
	 * @param expression	Bisheriger Ausdruck (kann "" für neue Einträge sein)
	 * @param names	Liste aller bisher vorhandenen Variablennamen
	 * @param initialVariableValues	Liste der vorhandenen initialen Variablenzuweisungen
	 * @param stationIDs	Zuordnung von Stations-IDs zu Stationsnamen
	 * @param stationNameIDs	Zuordnung von Stations-IDs zu nutzerdefinierten Stationsnamen
	 * @param showUserSpecificOptions	Auswahloptionen für Wartezeit des Kunden usw.
	 * @param clientDataOnly	Nur clientdata(...)-Zuweisungen zulassen und keine globalen Variablen?
	 * @param modelUserFunctions	Modellspezifische nutzerdefinierte Funktionen (kann <code>null</code> sein)
	 */
	public VariablesTableModelDialog(final Component owner, final Runnable help, final String variable, final String expression, final String[] names, final Map<String,String> initialVariableValues, final Map<Integer,String> stationIDs, final Map<Integer,String> stationNameIDs, final boolean showUserSpecificOptions, final boolean clientDataOnly, final ExpressionCalcModelUserFunctions modelUserFunctions) {
		this(owner,help,variable,expression,null,names,initialVariableValues,stationIDs,stationNameIDs,showUserSpecificOptions,clientDataOnly,modelUserFunctions);
	}

	/**
	 * Konstruktor der Klasse <code>VariablesTableModelDialog</code>
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param help	Wird aufgerufen, wenn der Nutzer auf "Hilfe" klickt
	 * @param variable	Bisheriger Name der Variable (kann "" für neue Einträge sein)
	 * @param expression	Bisheriger Ausdruck (kann "" für neue Einträge sein)
	 * @param askForValueOnStart	Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden? (kann <code>null</code> sein, dann wird diese Option nicht angeboten)
	 * @param names	Liste aller bisher vorhandenen Variablennamen
	 * @param initialVariableValues	Liste der vorhandenen initialen Variablenzuweisungen
	 * @param stationIDs	Zuordnung von Stations-IDs zu Stationsnamen
	 * @param stationNameIDs	Zuordnung von Stations-IDs zu nutzerdefinierten Stationsnamen
	 * @param showUserSpecificOptions	Auswahloptionen für Wartezeit des Kunden usw.
	 * @param clientDataOnly	Nur clientdata(...)-Zuweisungen zulassen und keine globalen Variablen?
	 * @param modelUserFunctions	Modellspezifische nutzerdefinierte Funktionen (kann <code>null</code> sein)
	 */
	public VariablesTableModelDialog(final Component owner, final Runnable help, final String variable, final String expression, final Boolean askForValueOnStart, final String[] names, final Map<String,String> initialVariableValues, final Map<Integer,String> stationIDs, final Map<Integer,String> stationNameIDs, final boolean showUserSpecificOptions, final boolean clientDataOnly, final ExpressionCalcModelUserFunctions modelUserFunctions) {
		super(owner,Language.tr("Surface.Set.Table.Edit"),false);

		this.modelUserFunctions=modelUserFunctions;

		final Set<String> tempVariableNames=new HashSet<>();
		if (names!=null) tempVariableNames.addAll(Arrays.asList(names));
		if (initialVariableValues!=null) tempVariableNames.addAll(initialVariableValues.keySet());
		this.names=tempVariableNames.toArray(String[]::new);

		this.clientDataOnly=clientDataOnly;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel panel;
		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Set.Table.Edit.VariableName")+":",variable);
		this.variable=(JTextField)data[1];
		content.add((JPanel)data[0]);
		this.variable.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(warningLabel=new JLabel(""));
		warningLabel.setVisible(false);

		if (showUserSpecificOptions) {
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionExpression=new JRadioButton(Language.tr("Surface.Set.Table.Edit.Mode.Expression")));
		} else {
			optionExpression=null;
		}

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Set.Table.Edit.Mode.Expression.Expression")+":","0");
		this.expression=(JTextField)data[1];
		content.add(panel=(JPanel)data[0]);
		panel.add(ModelElementBaseDialog.getExpressionEditButton(this,this.expression,false,names,initialVariableValues,stationIDs,stationNameIDs,true,modelUserFunctions),BorderLayout.EAST);
		this.expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (optionExpression!=null) optionExpression.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (optionExpression!=null) optionExpression.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (optionExpression!=null) optionExpression.setSelected(true); checkData(false);}
		});

		if (showUserSpecificOptions) {
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionWaiting=new JRadioButton(Language.tr("Surface.Set.Table.Edit.Mode.WaitingTime")));

			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionTransfer=new JRadioButton(Language.tr("Surface.Set.Table.Edit.Mode.TransferTime")));

			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionProcess=new JRadioButton(Language.tr("Surface.Set.Table.Edit.Mode.ProcessTime")));

			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionResidence=new JRadioButton(Language.tr("Surface.Set.Table.Edit.Mode.ResidenceTime")));

			ButtonGroup buttonGroup=new ButtonGroup();
			buttonGroup.add(optionExpression);
			buttonGroup.add(optionWaiting);
			buttonGroup.add(optionTransfer);
			buttonGroup.add(optionProcess);
			buttonGroup.add(optionResidence);

			optionExpression.setSelected(true);
			optionWaiting.setSelected(expression.equals(ModelElementSetRecord.SPECIAL_WAITING));
			optionTransfer.setSelected(expression.equals(ModelElementSetRecord.SPECIAL_TRANSFER));
			optionProcess.setSelected(expression.equals(ModelElementSetRecord.SPECIAL_PROCESS));
			optionResidence.setSelected(expression.equals(ModelElementSetRecord.SPECIAL_RESIDENCE));
			if (optionExpression.isSelected()) this.expression.setText(expression.isBlank()?"0":expression);
		} else {
			optionWaiting=null;
			optionTransfer=null;
			optionProcess=null;
			optionResidence=null;
			this.expression.setText(expression.isBlank()?"0":expression);
		}

		if (askForValueOnStart!=null) {
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(checkAskForValueOnStart=new JCheckBox(Language.tr("Surface.Set.Table.Edit.AskForValueOnStart"),askForValueOnStart.booleanValue()));

		} else {
			checkAskForValueOnStart=null;
		}

		setMinSizeRespectingScreensize(500,0);
		pack();
		checkData(false);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		String variable=this.variable.getText();
		boolean varNameOk=true;
		if (optionExpression!=null && CalcSymbolClientUserData.testClientData(variable)>=0) {
			warningLabel.setVisible(false);
			/* varNameOk bleibt true */
			variable=null; /* nicht unten als Variablenname verbuchen */
		} else {
			if (clientDataOnly) {
				varNameOk=false;
				warningLabel.setVisible(false);
			} else {
				varNameOk=ExpressionCalc.checkVariableName(variable);
				String warning=null;
				if (variable.trim().equalsIgnoreCase("w")) warning=Language.tr("Surface.Set.Table.Edit.VariableName.WaitingTime");
				if (variable.trim().equalsIgnoreCase("t")) warning=Language.tr("Surface.Set.Table.Edit.VariableName.TransferTime");
				if (variable.trim().equalsIgnoreCase("p")) warning=Language.tr("Surface.Set.Table.Edit.VariableName.ProcessTime");
				if (warning!=null) warningLabel.setText(warning);
				warningLabel.setVisible(warning!=null);
			}
		}

		if (varNameOk) {
			this.variable.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			this.variable.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Set.Table.Edit.VariableName.Error.Title"),String.format(Language.tr("Surface.Set.Table.Edit.VariableName.Error.Info"),variable));
				return false;
			}
			ok=false;
		}

		final String expression=this.expression.getText();
		List<String> namesList=new ArrayList<>(Arrays.asList(names));
		if (ok) {
			boolean inList=false;
			if (variable!=null) {
				for (String s: namesList) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
				if (!inList) namesList.add(variable);
			}
			for (String var: RunModel.additionalVariables) {
				inList=false;
				for (String s: namesList) if (s.equalsIgnoreCase(var)) {inList=true; break;}
				if (!inList) namesList.add(var);
			}
		}
		final int error=ExpressionCalc.check(expression,namesList.toArray(String[]::new),modelUserFunctions);
		if (error<0) {
			this.expression.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			this.expression.setBackground(Color.red);
			if (optionExpression!=null && optionExpression.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Set.Table.Edit.Mode.Expression.Error.Title"),String.format(Language.tr("Surface.Set.Table.Edit.Mode.Expression.Error.Info"),expression,error+1));
					return false;
				}
				ok=false;
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert den neuen Name der Variable.
	 * @return	Name der Variable
	 */
	final String getVariable() {
		return variable.getText();
	}

	/**
	 * Liefert den neuen Rechenausdruck.
	 * @return	Neuer Rechenausdruck
	 */
	final String getExpression() {
		if (optionWaiting!=null) {
			if (optionWaiting.isSelected()) return ModelElementSetRecord.SPECIAL_WAITING;
			if (optionTransfer.isSelected()) return ModelElementSetRecord.SPECIAL_TRANSFER;
			if (optionProcess.isSelected()) return ModelElementSetRecord.SPECIAL_PROCESS;
			if (optionResidence.isSelected()) return ModelElementSetRecord.SPECIAL_RESIDENCE;
		}
		return expression.getText();
	}

	/**
	 * Liefert die Information, ob beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden soll.
	 * @return	Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden?
	 */
	final boolean getAskForValueOnStart() {
		return (checkAskForValueOnStart!=null && checkAskForValueOnStart.isSelected());
	}
}