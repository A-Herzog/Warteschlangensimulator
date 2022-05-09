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
package systemtools.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Zeigt einen Dialog zur Volltextsuche in der Hilfe an.
 * @author Alexander Herzog
 * @see IndexSystem
 * @see HTMLPanel
 */
public class HTMLPanelSearchDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8612950523646295491L;

	/**
	 * Liste der Suchtreffer
	 */
	private final JList<ResultRecord> results;

	/**
	 * Listenmodell, welche die Daten für {@link #results} vorhält
	 * @see #results
	 */
	private DefaultListModel<ResultRecord> resultsModel;

	/**
	 * Zuletzt eingegebener Suchbegriff
	 */
	private String lastSearchString;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public HTMLPanelSearchDialog(final Component owner) {
		super(owner,HelpBase.buttonSearch);

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Suchfeld */
		final JPanel setup=new JPanel(new BorderLayout());
		content.add(setup,BorderLayout.NORTH);
		final JLabel label=new JLabel(HelpBase.buttonSearchString+": ");
		setup.add(label,BorderLayout.WEST);
		final JTextField input=new JTextField();
		setup.add(input,BorderLayout.CENTER);
		label.setLabelFor(input);
		input.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateSearch(input.getText());}
			@Override public void keyReleased(KeyEvent e) {updateSearch(input.getText());}
			@Override public void keyPressed(KeyEvent e) {updateSearch(input.getText());}
		});

		/* Ergebnisanzeige */
		content.add(new JScrollPane(results=new JList<>(resultsModel=new DefaultListModel<>())));
		results.setCellRenderer(new ResultsRenderer());
		results.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) close(BaseDialog.CLOSED_BY_OK);
			}
		});

		/* Dialog anzeigen */
		setMinSizeRespectingScreensize(640,480);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Aktualisiert die Ausgabe
	 * @param searchString	Suchbegriff
	 */
	private void updateSearch(final String searchString) {
		if (searchString.equals(lastSearchString)) return;
		lastSearchString=searchString;

		final DefaultListModel<ResultRecord> resultsModel=new DefaultListModel<>();
		if (searchString.length()>=3) {
			final IndexSystem index=IndexSystem.getInstance();

			for (Map.Entry<String,String> entry: index.getTitleHits(searchString).entrySet()) {
				resultsModel.addElement(new ResultRecord(entry.getKey(),entry.getValue()));
			}

			for (Map.Entry<String,Set<String>> entry: index.getIndexHits(searchString).entrySet()) {
				resultsModel.addElement(new ResultRecord(entry.getKey(),entry.getValue()));
			}
		}

		this.resultsModel=resultsModel;
		results.setModel(resultsModel);
	}

	@Override
	protected boolean checkData() {
		if (results.getSelectedIndex()<0) {
			MsgBox.error(this,HelpBase.buttonSearch,HelpBase.buttonSearchNoHitSelected);
			return false;
		}

		return true;
	}

	/**
	 * Liefert das ausgewählte Ergebnis.
	 * @return	Seiten für das ausgewählte Ergebnis
	 */
	public Set<String> getResult() {
		if (results.getSelectedIndex()<0) return null;
		final ResultRecord record=resultsModel.get(results.getSelectedIndex());
		if (record.page!=null) return new HashSet<>(Arrays.asList(record.page));
		if (record.pages!=null) return record.pages;
		return null;
	}

	/**
	 * Datensatz für einen Suchtreffer
	 */
	private static class ResultRecord {
		/**
		 * Name des Suchtreffers
		 */
		public final String hitName;

		/**
		 * Name der Seite mit dem entsprechenden Titel
		 */
		public final String page;

		/**
		 * Liste der Seiten mit entsprechendem Inhalt
		 */
		public final Set<String> pages;

		/**
		 * Enthält {@link #pages} genau einen Eintrag, so steht
		 * hier (wenn vorhanden) der zugehörige Name des Treffers
		 */
		public final String pagesName;

		/**
		 * Konstruktor der Klasse
		 * @param hitName	Name des Suchtreffers
		 * @param page	Zugehörige Seite
		 */
		public ResultRecord(final String hitName, final String page) {
			this.hitName=hitName;
			this.page=page;
			this.pages=null;
			this.pagesName=null;
		}

		/**
		 * Konstruktor der Klasse
		 * @param hitName	Name des Suchtreffers
		 * @param pages	Zugehörige Seiten
		 */
		public ResultRecord(final String hitName, final Set<String> pages) {
			this.hitName=hitName;
			this.page=null;
			this.pages=pages;
			this.pagesName=(pages.size()==1)?getPageName(pages.toArray(new String[0])[0]):null;
		}

		/**
		 * Liefert den Titel der Seite zu einem Dateiname.
		 * @param page	Dateiname
		 * @return	Titel der Seite oder <code>null</code>, wenn zu den angegebenen Dateinamen keine passende Seite existiert
		 */
		private static String getPageName(final String page) {
			return IndexSystem.getInstance().getPageName(page);
		}
	}

	/**
	 * Renderer für HTMLPanelSearchDialog#results
	 * @see HTMLPanelSearchDialog#results
	 */
	private static class ResultsRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-6622230833352872930L;

		/**
		 * Konstruktor der Klasse
		 */
		public ResultsRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
		 * HTML-Entitäten um.
		 * @param line	Umzuwandelnder Text
		 * @return	Umgewandelter Text
		 */
		private String encodeHTMLentities(final String line) {
			if (line==null) return "";
			String result;
			result=line.replaceAll("&","&amp;");
			result=result.replaceAll("<","&lt;");
			result=result.replaceAll(">","&gt;");
			return result;
		}

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			final JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

			label.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));

			if (value instanceof ResultRecord) {
				final ResultRecord record=(ResultRecord)value;

				final StringBuilder text=new StringBuilder();
				text.append("<html><body>");

				if (record.page!=null) {
					text.append(HelpBase.buttonSearchResultTypePage);
					text.append(": ");
					label.setIcon(SimToolsImages.HELP_PAGE.getIcon());
				}
				if (record.pages!=null) {
					text.append(HelpBase.buttonSearchResultTypeIndex);
					text.append(": ");
					label.setIcon(SimToolsImages.HELP_SEARCH.getIcon());
				}

				text.append("<b>");
				text.append(encodeHTMLentities(record.hitName));
				text.append("</b>");

				if (record.pages!=null) {
					text.append(" (");
					if (record.pages.size()==1 && record.pagesName!=null) {
						text.append(String.format(HelpBase.buttonSearchResultOnPage,encodeHTMLentities(record.pagesName)));
					} else {
						if (record.pages.size()==1) {
							text.append(String.format(HelpBase.buttonSearchResultCountSingular,record.pages.size()));
						} else {
							text.append(String.format(HelpBase.buttonSearchResultCountPlural,record.pages.size()));
						}
					}
					text.append(")");
				}

				label.setText(text.toString());
			}
			return label;
		}
	}
}
