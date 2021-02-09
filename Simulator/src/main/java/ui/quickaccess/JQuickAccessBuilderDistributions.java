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
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionTools;
import ui.calculator.CalculatorWindow;
import ui.images.Images;

/**
 * Erstellt Schnellzugriffeinträge zur Auswahl einer Verteilung
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 * @see CalculatorWindow
 */
public class JQuickAccessBuilderDistributions extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText Eingegebener Text
	 */
	public JQuickAccessBuilderDistributions(final String quickAccessText) {
		super(Language.tr("QuickAccess.Distributions"),Language.tr("QuickAccess.Distributions.Hint"),quickAccessText,false);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param openDistributionsDialog Callback zum Öffnen des Verteilungsansichtsdialogs
	 */
	public void work(final Consumer<AbstractDistributionWrapper> openDistributionsDialog) {
		final String quickAccessTextLower=quickAccessText.trim().toLowerCase();
		if (quickAccessTextLower.length()<2) return;

		for (String name: DistributionTools.getDistributionNames()) {
			if (name.toLowerCase().contains(quickAccessTextLower)) {
				test(
						null,
						name,
						Images.EXTRAS_CALCULATOR_PLOTTER.getIcon(),
						record->openDistributionsDialog.accept((AbstractDistributionWrapper)record.data),
						DistributionTools.getWrapper(name));
			}
		}
	}
}
