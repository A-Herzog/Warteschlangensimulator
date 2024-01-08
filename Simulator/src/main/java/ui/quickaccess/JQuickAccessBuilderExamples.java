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

import java.awt.Component;
import java.util.function.Consumer;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.examples.EditModelExamples;
import ui.images.Images;
import ui.tools.FlatLaFHelper;

/**
 * Erstellt Schnellzugriffeinträge basierend auf den Beispielmodellen
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderExamples extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderExamples(final String quickAccessText) {
		super(Language.tr("QuickAccess.Examples"),Language.tr("QuickAccess.Examples.Hint"),quickAccessText,true);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param owner	Übergeordnetes Element
	 * @param loader	Callback zum Laden eines Beispielmodells in den Editor
	 */
	public void work(final Component owner, final Consumer<EditModel> loader) {
		if (quickAccessText.length()<3) return;

		final Consumer<JQuickAccessRecord> callback=record->process(owner,(String)record.data,loader);

		for (EditModelExamples.Example example: EditModelExamples.getList()) {
			test(EditModelExamples.getGroupName(example.type),example.names[0],Images.MODEL.getIcon(),callback,example.names[0]);
		}
	}

	/**
	 * Prüft, ob ein Beispiel zu der Sucheingabe passt.
	 * @param owner	Übergeordnetes Element
	 * @param exampleName	Zu prüfendes Beispiel
	 * @param loader	 Beim Anklicken auszuführende Aktion
	 */
	private void process(final Component owner, final String exampleName, final Consumer<EditModel> loader) {
		final int index=EditModelExamples.getExampleIndexFromName(exampleName);
		if (index<0) return;
		final EditModel model=EditModelExamples.getExampleByIndex(owner,index,FlatLaFHelper.isDark());
		if (model==null) return;
		loader.accept(model);
	}
}
