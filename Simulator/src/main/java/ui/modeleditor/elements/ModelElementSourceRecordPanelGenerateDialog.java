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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.stream.DoubleStream;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionRandomNumber;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * In diesem Dialog können Zufallszahlen für den Datenstrom-Modus
 * im {@link ModelElementSourceRecordPanel} generiert werden.
 * @author Alexander Herzog
 * @see ModelElementSourceRecordPanel
 */
public class ModelElementSourceRecordPanelGenerateDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=849660307335410362L;

	/**
	 * Eingabefeld für die Anzahl an zu erzeugenden Zufallszahlen
	 */
	private final JTextField countEdit;

	/**
	 * Tabs zur Auswahl der Art der Datenquelle für die Zufallszahlen
	 */
	private final JTabbedPane tabs;

	/**
	 * Datenquelle: Wahrscheinlichkeitsverteilung
	 */
	private JDistributionPanel editorDistribution;

	/**
	 * Datenquelle: Rechenausdruck
	 */
	private JTextField editorExpression;

	/**
	 * Ausgabe von Beispielwerten
	 */
	private JLabel exampleValues;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ModelElementSourceRecordPanelGenerateDialog(final Component owner) {
		super(owner,Language.tr("GenerateArrivalDataStream.Title"));

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Anzahl an zu erzeugenden Zufallszahlen */
		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(setup,BorderLayout.NORTH);
		final JLabel label=new JLabel(Language.tr("GenerateArrivalDataStream.NumberOfRandomNumbers")+":");
		setup.add(label);
		setup.add(countEdit=new JTextField("1000",10));
		ModelElementBaseDialog.addUndoFeature(countEdit);
		countEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		label.setLabelFor(countEdit);

		/* Datenquelle */
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		JPanel tab;

		/* Datenquelle: Wahrscheinlichkeitsverteilung */
		tabs.addTab(Language.tr("GenerateArrivalDataStream.ProbabilityDistribution"),tab=new JPanel(new BorderLayout()));
		tab.add(editorDistribution=new JDistributionPanel(new ExponentialDistribution(300),3600,true) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=2526962172048624369L;

			@Override
			public void setDistribution(AbstractRealDistribution distribution) {
				super.setDistribution(distribution);
				checkData();
			}
		});

		/* Datenquelle: Rechenausdruck */
		tabs.addTab(Language.tr("GenerateArrivalDataStream.CalculationExpression"),tab=new JPanel(new BorderLayout()));
		final JPanel tabInner=new JPanel();
		tab.add(tabInner,BorderLayout.NORTH);
		tabInner.setLayout(new BoxLayout(tabInner,BoxLayout.PAGE_AXIS));
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("GenerateArrivalDataStream.CalculationExpression")+":","300*Random()");
		final JPanel line=(JPanel)data[0];
		tabInner.add(line);
		editorExpression=(JTextField)data[1];
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,editorExpression,false,false,null,null),BorderLayout.EAST);
		editorExpression.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Tab-Change-Listener erst jetzt möglich */
		tabs.addChangeListener(e->checkData(false));

		/* Beispielwerte */
		final JPanel examplesPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(examplesPanel,BorderLayout.SOUTH);
		examplesPanel.add(exampleValues=new JLabel());
		checkData();

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_SOURCE_MODE_DISTRIBUTION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_SOURCE_MODE_EXPRESSION.getIcon());

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Anzahl an zu erzeugenden Zufallszahlen */
		if (NumberTools.getPositiveLong(countEdit,true)==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("GenerateArrivalDataStream.NumberOfRandomNumbers.ErrorTitle"),String.format(Language.tr("GenerateArrivalDataStream.NumberOfRandomNumbers.ErrorInfo"),countEdit.getText().trim()));
				return false;
			}
			ok=false;
		}

		/* Datenquelle */
		final double[] exampleValues=new double[5];
		switch (tabs.getSelectedIndex()) {
		case 0:
			/* Datenquelle: Wahrscheinlichkeitsverteilung */
			final AbstractRealDistribution dist=editorDistribution.getDistribution();
			for (int i=0;i<exampleValues.length;i++) exampleValues[i]=DistributionRandomNumber.random(dist);
			showExampleValues(exampleValues);
			break;
		case 1:
			/* Datenquelle: Rechenausdruck */
			final ExpressionCalc calc=new ExpressionCalc(null);
			final int error=calc.parse(editorExpression.getText().trim());
			if (error>=0) {
				editorExpression.setBackground(Color.RED);
				showExampleValues(null);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("GenerateArrivalDataStream.CalculationExpression.ErrorTitle"),String.format(Language.tr("GenerateArrivalDataStream.CalculationExpression.ErrorInfo"),editorExpression.getText().trim(),error+1));
					return false;
				}
				ok=false;
			} else {
				editorExpression.setBackground(NumberTools.getTextFieldDefaultBackground());
				for (int i=0;i<exampleValues.length;i++) try {exampleValues[i]=calc.calc();} catch (MathCalcError e) {exampleValues[i]=0;}
				showExampleValues(exampleValues);
			}
			break;
		}

		return ok;
	}

	/**
	 * Gibt eine Reihe von Beispielwerten aus.
	 * @param values	Beispielwerte (kann <code>null</code> oder leer sein)
	 * @see #exampleValues
	 */
	private void showExampleValues(final double[] values) {
		if (values==null || values.length==0) {
			exampleValues.setVisible(false);
			return;
		}

		final StringBuilder result=new StringBuilder();
		result.append("<html><body>");
		result.append("<b>"+Language.tr("GenerateArrivalDataStream.ExampleValues")+":</b> ");
		final String[] list=DoubleStream.of(values).mapToObj(d->NumberTools.formatNumber(d,5)).toArray(String[]::new);
		result.append(String.join(", ",list));
		result.append("</body></html>");

		exampleValues.setText(result.toString());
		exampleValues.setVisible(true);
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog mit "Ok" geschlossen, so liefert
	 * diese Funktion die generierten, neuen Zufallszahlen.
	 * @return	Neue Zufallszahlen in den Zeilen der Zeichenkette
	 */
	public String getNewValues() {
		/* Anzahl an zu erzeugenden Zufallszahlen */
		final int count=(int)Math.max(1,Math.min(10_000_000,NumberTools.getPositiveLong(countEdit,true)));

		final double[] numbers=new double[count];

		/* Datenquelle */
		switch (tabs.getSelectedIndex()) {
		case 0:
			/* Datenquelle: Wahrscheinlichkeitsverteilung */
			final AbstractRealDistribution dist=editorDistribution.getDistribution();
			for (int i=0;i<count;i++) numbers[i]=DistributionRandomNumber.random(dist);
			break;
		case 1:
			/* Datenquelle: Rechenausdruck */
			final ExpressionCalc calc=new ExpressionCalc(null);
			calc.parse(editorExpression.getText().trim());
			for (int i=0;i<count;i++) try {numbers[i]=calc.calc();} catch (MathCalcError e) {numbers[i]=0;}
			break;
		}

		/* Zusammenfassen und ausgeben */
		boolean first=true;
		final StringBuilder result=new StringBuilder();
		for (double d: numbers) {
			if (first) first=false; else result.append('\n');
			result.append(NumberTools.formatNumber(d,5));
		}
		return result.toString();
	}
}