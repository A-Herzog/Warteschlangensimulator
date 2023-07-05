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
package ui.quickaccess;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import systemtools.JSearchSettingsSync;
import tools.SetupData;
import ui.dialogs.FindAndReplaceDialog;
import ui.images.Images;

/**
 * Sucht die Zeichenkette im gesamten Modell und erzeugt, wenn sie gefunden wurde,
 * einen Eintrag zum Aufruf des regulären Suchdialogs.
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 * @see FullTextSearch
 * @see FindAndReplaceDialog
 */
public class JQuickAccessBuilderFullTextSearch extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderFullTextSearch(final String quickAccessText) {
		super(Language.tr("QuickAccess.FullTextSearch"),Language.tr("QuickAccess.FullTextSearch.Hint"),quickAccessText,false);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param model	Editor-Modell in dem gesucht werden soll
	 * @param openFullTextSearchDialog	Callback zum Öffnen des Suchdialogs
	 */
	public void work(final EditModel model, final Consumer<String> openFullTextSearchDialog) {
		final int len=quickAccessText.length();
		if (len<2) return;

		/* Suche vorbereiten */
		final SetupData setup=SetupData.getSetup();
		final Set<FullTextSearch.SearchOption> options=new HashSet<>();
		if (JSearchSettingsSync.getCaseSensitive()) options.add(FullTextSearch.SearchOption.CASE_SENSITIVE);
		if (setup.searchStationIDs) options.add(FullTextSearch.SearchOption.SEARCH_IDS);
		if (JSearchSettingsSync.getFullMatchOnly()) options.add(FullTextSearch.SearchOption.FULL_MATCH_ONLY);
		if (JSearchSettingsSync.getRegEx()) options.add(FullTextSearch.SearchOption.REGULAR_EXPRESSION);

		/* Suche durchführen */
		FullTextSearch searcher=new FullTextSearch(quickAccessText,options);
		model.search(searcher);
		final int resultsCount=searcher.getResults().size();

		/* Ergebnisanzahl ausgeben */
		if (resultsCount>0) {
			final String text;
			if (resultsCount==1) text=String.format(Language.tr("QuickAccess.FullTextSearch.Result.Singular"),resultsCount); else text=String.format(Language.tr("QuickAccess.FullTextSearch.Result.Plural"),resultsCount);
			getList().add(new JQuickAccessRecord(category,text,text,categoryTooltip,Images.GENERAL_FONT.getIcon(),record->openFullTextSearchDialog.accept((String)record.data),quickAccessText));
		}
	}
}
