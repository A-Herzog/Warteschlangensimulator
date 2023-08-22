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

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import language.Language;
import systemtools.BaseDialog;
import systemtools.help.HTMLPanelSelectDialog;
import systemtools.help.HelpBase;
import systemtools.help.IndexSystem;
import systemtools.images.SimToolsImages;
import ui.help.Help;
import ui.images.Images;

/**
 * Erstellt Schnellzugriffeinträge basierend auf den Hilfeseiten
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderHelp extends JQuickAccessBuilder {
	/**
	 * System zur Suche im Hilfeindex
	 */
	private final IndexSystem indexSystem;

	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderHelp(final String quickAccessText) {
		super(Language.tr("QuickAccess.Help"),Language.tr("QuickAccess.Help.Hint"),quickAccessText,false);
		indexSystem=IndexSystem.getInstance();
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param owner	Übergeordnetes Element
	 * @param maxTitle	Maximale Anzahl an Seitentitel-Treffern
	 * @param maxIndex	Maximale Anzahl an Index-Treffern
	 */
	public void work(final Component owner, final int maxTitle, final int maxIndex) {
		if (quickAccessText.length()<3) return;

		final List<JQuickAccessRecord> results=getList();
		final Consumer<JQuickAccessRecord> pageLoader=record->processPage(owner,(String)record.data);
		@SuppressWarnings("unchecked")
		final Consumer<JQuickAccessRecord> pagesLoader=record->processPages(owner,(Set<String>)record.data);

		int count;

		count=0;
		for (Map.Entry<String,String> entry: indexSystem.getTitleHits(quickAccessText).entrySet()) {
			if (count>=maxTitle) break;
			final String text=entry.getKey();
			final String page=entry.getValue();
			final int index=text.toLowerCase().indexOf(quickAccessText.toLowerCase());
			results.add(new JQuickAccessRecord(category,text,buildResultText(Language.tr("QuickAccess.Help.Page"),text,-1,index),categoryTooltip,Images.HELP.getIcon(),pageLoader,page));
			count++;
		}

		count=0;
		for (Map.Entry<String,Set<String>> entry: indexSystem.getIndexHits(quickAccessText).entrySet()) {
			if (count>=maxIndex) break;
			final String text=entry.getKey();
			final Set<String> pages=entry.getValue();
			final int index=text.toLowerCase().indexOf(quickAccessText.toLowerCase());
			String info=null;
			if (pages.size()==1) {
				final String pageName=indexSystem.getPageName(pages.toArray(new String[0])[0]);
				if (pageName!=null) info=" ("+String.format(HelpBase.buttonSearchResultOnPage,pageName)+")";
			}
			if (info==null) {
				info=" ("+String.format((pages.size()==1)?HelpBase.buttonSearchResultCountSingular:HelpBase.buttonSearchResultCountPlural,pages.size())+")";
			}

			results.add(new JQuickAccessRecord(category,text,buildResultText(Language.tr("QuickAccess.Help.PageContent"),text+info,-1,index),categoryTooltip,SimToolsImages.HELP_FIND_IN_PAGE.getIcon(),pagesLoader,pages));
			count++;
		}
	}

	/**
	 * Ruft eine Hilfeseite auf.
	 * @param owner	Übergeordnetes Element
	 * @param pageName	Aufzurufende Hilfeseite
	 */
	private void processPage(final Component owner, final String pageName) {
		if (pageName==null) return;

		final int index=pageName.lastIndexOf(".html");
		if (index<0) return;

		Component c=owner;
		while (c!=null && !(c instanceof Container)) c=c.getParent();
		Help.topic((Container)c,pageName.substring(0,index));
	}

	/**
	 * Ruft eine Hilfeseite auf; zeigt dabei vorab ggf. eine Auswahl an.
	 * @param owner	Übergeordnetes Element
	 * @param pageNames	Namen der möglichen aufzurufenden Seiten
	 */
	private void processPages(final Component owner, final Set<String> pageNames) {
		if (pageNames==null || pageNames.size()==0) return;

		if (pageNames.size()==1) {
			processPage(owner,pageNames.toArray(new String[0])[0]);
			return;
		}

		final HTMLPanelSelectDialog selectDialog=new HTMLPanelSelectDialog(owner,pageNames);
		if (selectDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			processPage(owner,selectDialog.getSelectedPage());
		}
	}
}
