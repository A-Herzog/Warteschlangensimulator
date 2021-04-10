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

import java.util.function.Consumer;

import language.Language;
import ui.help.BookData;
import ui.images.Images;

/**
 * Versucht einen Ausdruck im Inhaltsverzeichnis oder
 * im Sachverzeichnis des Buches zu finden.
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderBook extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderBook(final String quickAccessText) {
		super(Language.tr("QuickAccess.Book"),Language.tr("QuickAccess.Book.Hint"),quickAccessText,false);
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param openBookDialog	Callback zum Öffnen des Buch-Dialogs
	 */
	public void work(final Consumer<BookData.BookMatch> openBookDialog) {
		final int len=quickAccessText.length();
		if (len<3) return;

		final BookData bookData=BookData.getInstance();
		int count;

		count=0;
		for (BookData.BookSection section: bookData.getTOCMatch(quickAccessText)) {
			getList().add(new JQuickAccessRecord(category,section.name,section.id+" "+section.name,categoryTooltip,Images.HELP_BOOK_CONTENT.getIcon(),record->openBookDialog.accept((BookData.BookMatch)record.data),section));
			count++;
			if (count>=10) break;
		}

		count=0;
		for (BookData.IndexMatch index: bookData.getIndexMatches(quickAccessText)) {
			getList().add(new JQuickAccessRecord(category,index.name,Language.tr("QuickAccess.Book.Index")+": "+index.name,categoryTooltip,Images.HELP_BOOK_INDEX.getIcon(),record->openBookDialog.accept((BookData.BookMatch)record.data),index));
			count++;
			if (count>=10) break;
		}
	}
}
