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

import java.io.File;
import java.util.function.Consumer;

import language.Language;
import tools.SetupData;
import ui.images.Images;

/**
 * Erstellt Schnellzugriffeinträge basierend auf den zuletzt verwendeten Dateien
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderLastFiles extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderLastFiles(String quickAccessText) {
		super(Language.tr("QuickAccess.RecentlyUsed"),Language.tr("QuickAccess.RecentlyUsed.Hint"),quickAccessText,false);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param loadFile	Callback zum Laden einer Datei
	 */
	public void work(final Consumer<File> loadFile) {
		if (quickAccessText.length()<2) return;

		final Consumer<JQuickAccessRecord> callback=record->loadFile.accept((File)record.data);
		final String pre=Language.tr("QuickAccess.RecentlyUsed.Pre");
		final String hint=Language.tr("QuickAccess.RecentlyUsed.Hint");

		final SetupData setup=SetupData.getSetup();
		if (setup.lastFiles!=null && setup.useLastFiles) for (String lastFile: setup.lastFiles) if (lastFile!=null) {
			final File file=new File(lastFile);
			if (!file.isFile()) continue;
			test(pre,file.getName(),hint+file.toString(),Images.MODEL_LOAD.getIcon(),callback,file);
		}
	}
}
