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
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
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

	/**
	 * Einzelne Rechen-Panels
	 */
	private final CalculatorWindowPageCalculatorPanel[] calculationPanel;

	/**
	 * Anzahl an zu berechnenden Wiederholungen
	 */
	private CalculatorWindowPageCalculatorPanel.CommonCalculationRepeatCount calculationRepeatCount;

	/**
	 * Konstruktor der Klasse
	 * @param window	Gesamtes Fenster
	 * @param tabs	Tabs-Element in das dieses Tab eingefügt werden soll
	 * @param initialExpression	Initial anzuzeigender Ausdruck (kann <code>null</code> sein)
	 */
	public CalculatorWindowPageCalculator(final Window window, final JTabbedPane tabs, final String initialExpression) {
		super(tabs);

		final JPanel lines=new JPanel();
		add(lines,BorderLayout.NORTH);
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		calculationRepeatCount=new CalculatorWindowPageCalculatorPanel.CommonCalculationRepeatCount();
		calculationPanel=new CalculatorWindowPageCalculatorPanel[5];

		for (int i=0;i<calculationPanel.length;i++) {
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			line.add(new JLabel("<html><body><b>"+String.format(Language.tr("CalculatorDialog.Tab.Calculator.ExpressionNr"),i+1)+"</b></body></html>"));
			lines.add(line);
			final String expression=(i==0)?initialExpression:null;
			lines.add(calculationPanel[i]=new CalculatorWindowPageCalculatorPanel(window,calculationRepeatCount,expression));
		}
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
		int index=calculationPanel.length-1;
		for (int i=0;i<calculationPanel.length;i++) if (calculationPanel[i].isEmpty()) {index=i; break;}
		calculationPanel[index].setExpression(expression);

		showPage();
	}
}
