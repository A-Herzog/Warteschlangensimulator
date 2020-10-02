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

import java.util.Map;
import java.util.function.Consumer;

import language.Language;
import ui.images.Images;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.coreelements.ModelElementPosition;

/**
 * Erstellt Schnellzugriffeinträge basierend auf der Liste der Elementenvorlagen
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderNewElements extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderNewElements(final String quickAccessText) {
		super(Language.tr("QuickAccess.Elements"),Language.tr("QuickAccess.Elements.Hint"),quickAccessText,true);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param selectElementTemplate	Callback zur Anzeige eines Elements in der Vorlagenleiste
	 */
	public void work(final Consumer<ModelElementPosition> selectElementTemplate) {
		if (quickAccessText.length()<2) return;

		final Consumer<JQuickAccessRecord> callback=record->selectElementTemplate.accept((ModelElementPosition)record.data);

		final ModelElementCatalog catalog=ModelElementCatalog.getCatalog();
		for (Map.Entry<String,Map<String,ModelElementPosition>> group: catalog.getAll().entrySet()) {
			final String groupName=group.getKey();
			for (Map.Entry<String,ModelElementPosition> element: group.getValue().entrySet()) {
				test(groupName,element.getKey(),Images.EDIT_ADD.getIcon(),callback,element.getValue());
			}
		}
	}
}
