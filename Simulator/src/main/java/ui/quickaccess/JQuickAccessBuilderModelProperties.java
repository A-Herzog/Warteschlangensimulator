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
import ui.modelproperties.ModelPropertiesDialog;

/**
 * Erstellt Schnellzugriffeinträge basierend auf den Dialogseiten des Modelleigenschaftendialogs
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderModelProperties extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderModelProperties(String quickAccessText) {
		super(Language.tr("QuickAccess.ModelProperties"),Language.tr("QuickAccess.ModelProperties.Hint"),quickAccessText,false);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param openPropertiesModelDialog	Callback zum Aufrufen einer Seite des Modelleigenschaftendialogs
	 */
	public void work(final Consumer<ModelPropertiesDialog.InitialPage> openPropertiesModelDialog) {
		if (quickAccessText.length()<2) return;

		final String pre=Language.tr("QuickAccess.ModelProperties.Pre");
		for (ModelPropertiesDialog.InitialPage page: ModelPropertiesDialog.InitialPage.values()) {
			test(
					pre,
					page.getName(),
					page.getIcon(),
					record->openPropertiesModelDialog.accept(((ModelPropertiesDialog.InitialPage)record.data)),
					page);
		}
	}
}
