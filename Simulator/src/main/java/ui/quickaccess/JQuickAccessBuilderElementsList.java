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
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Erstellt Schnellzugriffeinträge basierend auf der Liste der Element im Modell
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderElementsList extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderElementsList(String quickAccessText) {
		super(Language.tr("QuickAccess.ModelElements"),Language.tr("QuickAccess.ModelElements.Hint"),quickAccessText,true);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param surface	Zeichenfläche die durchsucht werden soll
	 * @param selectElement	Callback zum Selektieren eines Elements
	 */
	public void work(final ModelSurface surface, final Consumer<Integer> selectElement) {
		final Consumer<JQuickAccessRecord> callback=record->selectElement.accept((Integer)record.data);

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementBox) processElement((ModelElementBox)element,callback);
			if (element instanceof ModelElementSub) work(((ModelElementSub)element).getSubSurface(),selectElement);
			if (element instanceof ModelElementVertex) processElement((ModelElementVertex)element,callback);
		}
	}

	private void processElement(final ModelElementPosition element, final Consumer<JQuickAccessRecord> selectElement) {
		final String name;
		if (element.getName().isEmpty()) {
			name="id="+element.getId();
		} else {
			name="\""+element.getName()+"\" (id="+element.getId()+")";
		}

		final String pre=(element instanceof ModelElementBox)?((ModelElementBox)element).getTypeName():element.getContextMenuElementName();
		test(pre,name,new String[]{""+element.getId()},Images.MODEL_ADD_STATION.getIcon(),selectElement,element.getId());
	}
}
