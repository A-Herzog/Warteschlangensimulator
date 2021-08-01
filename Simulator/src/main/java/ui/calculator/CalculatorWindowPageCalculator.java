/**
 * Copyright 2021 Alexander Herzog
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
package ui.calculator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import ui.expressionbuilder.ExpressionBuilder;
import ui.expressionbuilder.ExpressionBuilderAutoComplete;
import ui.images.Images;

/**
 * Rechner-Tab innerhalb des Rechner-Fensters
 * @author Alexander Herzog
 * @see CalculatorWindow
 */
public class CalculatorWindowPageCalculator extends CalculatorWindowPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6550861112699836484L;

	/** Eingabezeile */
	private final JTextField inputEdit;
	/** Ergebnisausgabezeile */
	private final JTextField outputEdit;

	/**
	 * Konstruktor der Klasse
	 * @param tabs	Tabs-Element in das dieses Tab eingefügt werden soll
	 * @param initialExpression	Initial anzuzeigender Ausdruck (kann <code>null</code> sein)
	 */
	public CalculatorWindowPageCalculator(final JTabbedPane tabs, final String initialExpression) {
		super(tabs);

		JPanel line;
		Object[] data;

		final JPanel lines=new JPanel();
		add(lines,BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Eingabezeile */
		data=getInputPanel(Language.tr("CalculatorDialog.Expression")+":","",-1);
		lines.add(line=(JPanel)data[0]);
		inputEdit=(JTextField)data[1];
		inputEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {outputEdit.setText(calc(inputEdit.getText()));}
		});

		/* ExpressionBuilder-Button */
		final JButton buildButton=new JButton();
		line.add(buildButton,BorderLayout.EAST);
		buildButton.setPreferredSize(new Dimension(26,26));
		buildButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		buildButton.setToolTipText(Language.tr("CalculatorDialog.Expression.BuildExpression"));

		/* Ausgabezeile */
		data=getInputPanel(Language.tr("CalculatorDialog.Result")+":","",-1);
		lines.add(line=(JPanel)data[0]);
		outputEdit=(JTextField)data[1];
		outputEdit.setEditable(false);

		/* Expression-Builder konfigurieren */
		buildButton.addActionListener(e->{
			final ExpressionBuilder dialog=new ExpressionBuilder(this,inputEdit.getText(),false,new String[0],null,null,null,false,true,true);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				inputEdit.setText(dialog.getExpression());
				outputEdit.setText(calc(inputEdit.getText()));
			}
		});
		ExpressionBuilderAutoComplete.process(new ExpressionBuilder(this,inputEdit.getText(),false,new String[0],null,null,null,false,true,true),inputEdit);

		/* Kopieren-Button */
		final JButton button=new JButton("");
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.setToolTipText(Language.tr("CalculatorDialog.Result.Copy"));
		final Dimension size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		button.addActionListener(e->{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(outputEdit.getText()),null);
		});
		line.add(button,BorderLayout.EAST);

		/* Start */
		if (initialExpression!=null) setExpression(initialExpression);
	}

	@Override
	protected String getTabTitle() {
		return Language.tr("CalculatorDialog.Tab.Calculator");
	}

	@Override
	protected Images getTabIcon() {
		return Images.EXTRAS_CALCULATOR;
	}

	/**
	 * Stellt einen anzuzeigenden Rechenausdruck ein.
	 * @param expression	Neuer Rechenausdruck
	 */
	public void setExpression(final String expression) {
		if (expression==null || expression.trim().isEmpty()) return;

		inputEdit.setText(expression);
		outputEdit.setText(calc(inputEdit.getText()));
		showPage();
	}

	/**
	 * Berechnet einen Ausdruck
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Liefert das Ergebnis als Zeichenkette oder eine Fehlermeldung
	 */
	private String calc(final String expression) {
		final ExpressionCalc calc=new ExpressionCalc(null);
		final int error=calc.parse(expression);
		if (error>=0) return String.format(Language.tr("CalculatorDialog.Expression.ParseError"),error+1);
		double d;
		try {
			d=calc.calc();
		} catch (MathCalcError e) {
			return Language.tr("CalculatorDialog.Expression.CalcError");
		}
		return NumberTools.formatNumberMax(d);
	}
}
