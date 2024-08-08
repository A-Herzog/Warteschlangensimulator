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
package ui.quickaccess;

import java.util.function.Consumer;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import ui.calculator.CalculatorWindow;
import ui.images.Images;

/**
 * Versucht einen Ausdruck als Rechenausdruck zu interpretieren
 * und erstellt, wenn dies möglich ist, einen Schnellzugriffeintrag.
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 * @see CalculatorWindow
 */
public class JQuickAccessBuilderCalc extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderCalc(final String quickAccessText) {
		super(Language.tr("QuickAccess.Expression"),Language.tr("QuickAccess.Expression.Hint"),quickAccessText,false);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param model	Aktuelles Modell (zur Ermittlung der modellspezifischen nutzerdefinierten Funktionen)
	 * @param openCalculationDialog	Callback zum Öffnen des Rechnerdialogs
	 */
	public void work(final EditModel model, final Consumer<String> openCalculationDialog) {
		final int len=quickAccessText.length();
		if (len<2) return;
		boolean needProcessing=false;
		for (int i=0;i<len;i++) {
			final char c=quickAccessText.charAt(i);
			if ((c<'0' || c>'9') && c!='.' && c!=',') {needProcessing=true; break;}
		}
		if (!needProcessing) return;

		final ExpressionCalc calc=new ExpressionCalc(null,model.userFunctions);
		final int error=calc.parse(quickAccessText);
		if (error>=0) return;

		double result;
		try {
			result=calc.calc();
		} catch (MathCalcError e) {
			return;
		}

		final String text=quickAccessText+"="+NumberTools.formatNumberMax(result);

		getList().add(new JQuickAccessRecord(category,text,text,categoryTooltip,Images.EXTRAS_CALCULATOR.getIcon(),record->openCalculationDialog.accept((String)record.data),quickAccessText));
	}
}
