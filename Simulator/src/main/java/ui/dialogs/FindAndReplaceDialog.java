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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import systemtools.BaseDialog;
import systemtools.JRegExWikipediaLinkLabel;
import systemtools.JSearchSettingsSync;
import tools.JTableExt;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dieser Dialog ermöglicht es, in einem Modell nach Texten zu suchen
 * und diese zu ersetzen.
 * @author Alexander Herzog
 * @see FullTextSearch
 */
public class FindAndReplaceDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4100591202362006952L;

	/**
	 * Setup-Singleton
	 */
	private final SetupData setup;

	/**
	 * Modell in dem gesucht werden soll
	 */
	private final EditModel model;

	/**
	 * Wurden Texte in dem Modell verändert?
	 */
	private boolean dataReplaced;

	/**
	 * Eingabefeld für den Suchbegriff
	 */
	private final JTextField editSearchString;

	/**
	 * Option: Groß- und Kleinschreibung beachten
	 */
	private final JCheckBox optionCaseSensitive;

	/**
	 * Option: Auch nach Stations-IDs suchen
	 */
	private final JCheckBox optionStationIDs;

	/**
	 * Option: Nur gesamte Begriffe vergleichen
	 */
	private final JCheckBox optionFullMatchOnly;

	/**
	 * Option: Ist regulärer Ausdruck
	 */
	private final JCheckBox optionRegularExpression;

	/**
	 * Eingabefeld für den Ersetzungstext
	 */
	private final JTextField editReplaceString;

	/**
	 * Schaltfläche "Ersetzen"
	 */
	private final JButton buttonReplace;

	/**
	 * Schaltfläche "Alle" (Treffer auswählen)
	 */
	private final JButton buttonAll;

	/**
	 * Schaltfläche "Keine" (Treffer auswählen)
	 */
	private final JButton buttonNone;

	/**
	 * Tabelle der Suchtreffer
	 */
	private final JTableExt table;

	/**
	 * Tabellendaten der Suchtreffer
	 * @see #table
	 */
	private final FindAndReplaceDialogTableModel tableModel;

	/**
	 * Anzeige von Informationen zur Anzahl der Suchtreffer.
	 */
	private final JLabel info;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell in dem gesucht werden soll
	 * @param initialSearchTerm	Suchbegriff für initial auszuführende Suche (kann <code>null</code> sein)
	 */
	public FindAndReplaceDialog(final Component owner, final EditModel model, final String initialSearchTerm) {
		super(owner,Language.tr("FindAndReplace.Title"));
		setup=SetupData.getSetup();
		this.model=model.clone();
		dataReplaced=false;

		/* GUI */
		final JPanel content=createGUI(()->Help.topicModal(this,"FindAndReplace"));
		content.setLayout(new BorderLayout());

		JPanel line;
		Object[] data;

		/* Konfigurationsbereich oben */
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Suchbegriff */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("FindAndReplace.SearchString")+":","");
		setup.add(line=(JPanel)data[0]);
		editSearchString=(JTextField)data[1];
		editSearchString.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) {e.consume(); doSearch();}}
		});
		final JButton buttonSearch=new JButton(Language.tr("FindAndReplace.Search"),Images.GENERAL_FIND.getIcon());
		line.add(buttonSearch,BorderLayout.EAST);
		buttonSearch.addActionListener(e->doSearch());
		if (initialSearchTerm!=null) editSearchString.setText(initialSearchTerm);

		/* Option: Groß- und Kleinschreibung beachten */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionCaseSensitive=new JCheckBox(Language.tr("FindAndReplace.Option.CaseSensitive"),JSearchSettingsSync.getCaseSensitive()));
		optionCaseSensitive.addActionListener(e->saveSearchOptions());

		/* Option: Auch nach Stations-IDs suchen */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionStationIDs=new JCheckBox(Language.tr("FindAndReplace.Option.StationIDs"),this.setup.searchStationIDs));
		optionStationIDs.addActionListener(e->saveSearchOptions());

		/* Option: Nur gesamte Begriffe vergleichen */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionFullMatchOnly=new JCheckBox(Language.tr("FindAndReplace.Option.FullMatchOnly"),JSearchSettingsSync.getFullMatchOnly()));
		optionFullMatchOnly.addActionListener(e->saveSearchOptions());

		/* Option: Ist regulärer Ausdruck */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionRegularExpression=new JCheckBox(Language.tr("FindAndReplace.Option.RegularExpression"),JSearchSettingsSync.getRegEx()));
		optionRegularExpression.addActionListener(e->saveSearchOptions());
		line.add(new JRegExWikipediaLinkLabel(this));

		/* Ersetzungstext */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("FindAndReplace.ReplaceString")+":","");
		setup.add(line=(JPanel)data[0]);
		editReplaceString=(JTextField)data[1];
		editReplaceString.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) {e.consume(); doReplace();}}
		});

		line.add(buttonReplace=new JButton(Language.tr("FindAndReplace.Replace"),Images.GENERAL_FONT.getIcon()),BorderLayout.EAST);
		buttonReplace.addActionListener(e->doReplace());
		buttonReplace.setEnabled(false);

		/* Alle/Keine der Ergebnisse wählen */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(buttonAll=new JButton(Language.tr("FindAndReplace.Select.All"),Images.EDIT_ADD.getIcon()));
		buttonAll.addActionListener(e->doSelect(true));
		buttonAll.setEnabled(false);
		line.add(buttonNone=new JButton(Language.tr("FindAndReplace.Select.None"),Images.EDIT_DELETE.getIcon()));
		buttonNone.addActionListener(e->doSelect(false));
		buttonNone.setEnabled(false);

		/* Liste der Ergebnisse */
		content.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) close(BaseDialog.CLOSED_BY_OK);
			}
		});
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setModel(tableModel=new FindAndReplaceDialogTableModel(table,model));
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(0).setMinWidth(50);
		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);

		/* Anzeige der Treffer */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(info=new JLabel());

		/* Evtl. direkt eine Suche ausführen */
		if (initialSearchTerm!=null) doSearch();

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,700);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Löscht die Liste der bisherigen Suchtreffer.
	 */
	private void clearList() {
		tableModel.setData(null);
		buttonReplace.setEnabled(false);
		buttonAll.setEnabled(false);
		buttonNone.setEnabled(false);
		info.setText("");
	}

	/**
	 * Speichert die veränderten Such-Optionen im Setup.
	 */
	private void saveSearchOptions() {
		JSearchSettingsSync.setCaseSensitive(optionCaseSensitive.isSelected());
		JSearchSettingsSync.setFullMatchOnly(optionFullMatchOnly.isSelected());
		JSearchSettingsSync.setRegEx(optionRegularExpression.isSelected());

		setup.searchStationIDs=optionStationIDs.isSelected();
		setup.saveSetup();
	}

	/**
	 * Führt eine Suche durch.
	 */
	private void doSearch() {
		/* Bisherige Treffer löschen */
		clearList();

		/* Suche konfigurieren */
		final String searchString=editSearchString.getText();
		if (searchString.isEmpty()) return;
		final Set<FullTextSearch.SearchOption> options=new HashSet<>();
		if (optionCaseSensitive.isSelected()) options.add(FullTextSearch.SearchOption.CASE_SENSITIVE);
		if (optionStationIDs.isSelected()) options.add(FullTextSearch.SearchOption.SEARCH_IDS);
		if (optionFullMatchOnly.isSelected()) options.add(FullTextSearch.SearchOption.FULL_MATCH_ONLY);
		if (optionRegularExpression.isSelected()) options.add(FullTextSearch.SearchOption.REGULAR_EXPRESSION);

		/* Suche ausführen */
		final FullTextSearch searcher=new FullTextSearch(searchString,options);
		model.search(searcher);

		/* Ergebnisse anzeigen */
		final List<FullTextSearch.SearchMatch> results=searcher.getResults();
		final int resultsCount=results.size();
		final int resultsReplaceableCount=(int)results.stream().filter(match->match.canReplace()).count();
		tableModel.setData(results);

		/* Schaltflächen ggf. aktivieren */
		buttonReplace.setEnabled(resultsCount>0);
		buttonAll.setEnabled(resultsCount>0);
		buttonNone.setEnabled(resultsCount>0);

		/* Anzeige der Anzahl an Suchtreffern */
		if (resultsCount==0) {
			info.setText(Language.tr("FindAndReplace.ResultInfo.NoResults"));
		} else {
			final StringBuilder infoText=new StringBuilder();
			if (resultsCount==1) {
				infoText.append(String.format(Language.tr("FindAndReplace.ResultInfo.Singular"),resultsCount));
			} else {
				infoText.append(String.format(Language.tr("FindAndReplace.ResultInfo.Plural"),resultsCount));
			}
			infoText.append(" ");
			if (resultsReplaceableCount==1) {
				infoText.append(String.format(Language.tr("FindAndReplace.ReplaceInfo.Singular"),resultsReplaceableCount));
			} else {
				infoText.append(String.format(Language.tr("FindAndReplace.ReplaceInfo.Plural"),resultsReplaceableCount));
			}

			info.setText(infoText.toString());
		}
	}

	/**
	 * Markiert alle oder keine der Suchtreffer.
	 * @param select	Suchtreffer markieren?
	 */
	private void doSelect(final boolean select) {
		tableModel.select(select);
	}

	/**
	 * Ersetzt den Suchbegriff durch den neuen Begriff in den gewählten Suchtreffern.
	 */
	private void doReplace() {
		if (!buttonReplace.isEnabled()) return;

		final String replaceString=editReplaceString.getText();

		/* Ersetzung durchführen */
		if (tableModel.doReplace(replaceString)) {
			dataReplaced=true;
			/* Bisherige Treffer löschen */
			clearList();
		}
	}

	/**
	 * Wurde das Modell im Dialog verändert, so liefert diese Methode das
	 * veränderte Modell (sonst <code>null</code>). Die Rückgabe ist unabhängig
	 * davon, ob der Dialog per "Ok" geschlossen wurde oder nicht.
	 * @return	Verändertes Modell oder <code>null</code>, wenn keine Ersetzungen stattgefunden haben
	 */
	public EditModel getModel() {
		if (dataReplaced) return model; else return null;
	}

	/**
	 * Liefert die ID des ausgewählten Elements bezogen auf die Hauptebene, d.h.
	 * bei Kind-Elementen in einem Untermodell die ID des Untermodell-Elements.
	 * Wenn nichts ausgewählt ist, wird -1 zurückgeliefert
	 * @return	ID des ausgewählten Elements oder -1, wenn nichts gewählt ist
	 */
	public int getSelectedId() {
		return tableModel.getRowElementID(table.getSelectedRow());
	}
}
