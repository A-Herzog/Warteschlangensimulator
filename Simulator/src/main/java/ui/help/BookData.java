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
package ui.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import language.Language;
import mathtools.NumberTools;

/**
 * Inhaltsverzeichnis- und Sachverzeichnis-Einträge aus dem Buch zum Simulator
 * @author Alexander Herzog
 */
public class BookData {
	/**
	 * Zuordnung von Sachverzeichnis-Bezeichnern zu Seitenzahlen
	 */
	private final Map<String,List<Integer>> index;

	/**
	 * Liste der Sachverzeichnis-Bezeichner in Kleinbuchstaben
	 * @see #indexNormal
	 */
	private String[] indexLower;

	/**
	 * Liste der Sachverzeichnis-Bezeichner in normaler Schreibweise
	 * @see #indexLower
	 */
	private String[] indexNormal;

	/**
	 * Inhaltsverzeichniseinträge
	 */
	private final List<BookSection> toc;

	/**
	 * Namen der Inhaltsverzeichniseinträge in Kleinbuchstaben
	 * @see #toc
	 */
	private String[] tocNameLower;

	/**
	 * Zuordnung von Seitenzahlen zu Inhaltsverzeichniseinträgen
	 */
	private BookSection[] pageToTOC;

	/**
	 * Start-Buchseitenzahl, ab dem {@link #pageOffset} gelten soll.
	 * @see #pageOffsetBookPageRangeEnd
	 * @see #pageOffset
	 */
	private int[] pageOffsetBookPageRangeStart;

	/**
	 * End-Buchseitenzahl, ab dem {@link #pageOffset} gelten soll.
	 * @see #pageOffsetBookPageRangeStart
	 * @see #pageOffset
	 */
	private int[] pageOffsetBookPageRangeEnd;

	/**
	 * Differenz zwischen Buch-Seite und pdf-Seite
	 * @see #getPageOffset(int)
	 */
	private int[] pageOffset;

	/**
	 * Singleton-Instanz der Klasse
	 */
	private static BookData instance;

	/**
	 * Liefert die Instanz des Singletons
	 * @return	Instanz des Singletons
	 */
	public static synchronized BookData getInstance() {
		if (instance==null) instance=new BookData();
		return instance;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
	 * Um die Instanz zu erhalten, muss {@link #getInstance()} aufgerufen werden.
	 */
	private BookData() {
		index=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		toc=new ArrayList<>();
		pageToTOC=new BookSection[100];
		try(InputStream inputStream=BookData.class.getResourceAsStream("bookinfo/Simulation.idx")) {
			processIndex(loadTextFromRessource(inputStream));
		} catch (IOException e) {}
		try(InputStream inputStream=BookData.class.getResourceAsStream("bookinfo/Simulation.toc")) {
			processTOC(loadTextFromRessource(inputStream));
		} catch (IOException e) {}
		try(InputStream inputStream=BookData.class.getResourceAsStream("bookinfo/PageOffset.txt")) {
			processPageOffset(loadTextFromRessource(inputStream));
		} catch (IOException e) {}
	}

	/**
	 * Lädt einen Text aus einem Input-Stream.
	 * @param inputStream	Input-Stream aus dem der Text geladen werden soll
	 * @return	Geladener Text
	 */
	private static List<String> loadTextFromRessource(final InputStream inputStream) {
		final List<String> list=new ArrayList<>();
		if (inputStream!=null) {
			try (BufferedReader br=new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8))) {
				String line=null;
				while ((line=br.readLine())!=null) list.add(line);
			} catch (IOException e) {}
		}
		return list;
	}

	/**
	 * Interpretiert die idx-Datei.
	 * @param list	Text der idx-Datei
	 */
	private void processIndex(final List<String> list) {
		final String LINE_STEP1="\\indexentry{";
		final String LINE_STEP2="|hyperpage}{";
		final String LINE_STEP3="}";

		for (String line: list) {
			/* Zeile zerlegen in Name und Seite */
			if (!line.startsWith(LINE_STEP1)) continue;
			String s=line.substring(LINE_STEP1.length());
			int i=s.indexOf(LINE_STEP2);
			if (i<0) continue;
			String name=s.substring(0,i);
			s=s.substring(i+LINE_STEP2.length());
			i=s.indexOf(LINE_STEP3);
			if (i<0) continue;
			final String page=s.substring(0,i);

			/* Seitenzahl ist iene Zahl? */
			final Integer I=NumberTools.getNotNegativeInteger(page);
			if (I==null) continue;

			/* Bei Namen mit "@" nur den Teil nach dem "@" verwenden */
			i=name.indexOf("@");
			if (i>=0) name=name.substring(i+1);

			/* Name mit "!" umbauen */
			i=name.indexOf("!");
			if (i>=0) {
				final String part1=name.substring(i+1);
				name=part1+(part1.endsWith("-")?"":" ")+name.substring(0,i);
			}

			/* Eintrag speichern */
			name=name.trim();
			name=specialRename(name);
			List<Integer> pages=index.get(name);
			if (pages==null) {
				pages=new ArrayList<>();
				index.put(name,pages);
			}
			pages.add(I);
		}

		/* Array mit Kleinbuchstaben-Einträgen erstellen */
		indexLower=new String[index.size()];
		indexNormal=new String[index.size()];
		int nr=0;
		for (String key: index.keySet()) {
			indexNormal[nr]=key;
			indexLower[nr]=key.toLowerCase();
			nr++;
		}
	}

	/**
	 * Zu ersetzendes Index-Eintragsname
	 * @see #specialRename(String)
	 */
	private static final String RENAME_OLD="$\\sigma$";

	/**
	 * Neuer Index-Eintragsname
	 * @see #specialRename(String)
	 */
	private static final String RENAME_NEW="sigma";

	/**
	 * Ändert die Namen bestimmter Index-Einträge
	 * @param name	Bisheriger Name
	 * @return	Evtl. geänderter neuer Name
	 */
	private String specialRename(String name) {
		int i=name.indexOf(RENAME_OLD);
		if (i>=0) name=name.substring(0,i)+RENAME_NEW+name.substring(i+RENAME_OLD.length());
		return name;
	}

	/**
	 * Interpretiert die toc-Datei.
	 * @param list	Text der toc-Datei
	 */
	private void processTOC(final List<String> list) {
		final String LINE_STEP1="\\contentsline {";
		final String LINE_STEP2="}{\\numberline {";
		final String LINE_STEP3="}";
		final String LINE_STEP4="}{";
		final String LINE_STEP5="}";

		BookSection previousSection=null;

		for (String line: list) {
			/* Zeile zerlegen in ID, Name und Seite */
			if (!line.startsWith(LINE_STEP1)) continue;
			String s=line.substring(LINE_STEP1.length());
			int i=s.indexOf(LINE_STEP2);
			if (i<0) continue;
			s=s.substring(i+LINE_STEP2.length());
			i=s.indexOf(LINE_STEP3);
			if (i<0) continue;
			final String sectionID=s.substring(0,i);
			s=s.substring(i+LINE_STEP3.length());
			i=s.indexOf(LINE_STEP4);
			if (i<0) continue;
			final String sectionName=s.substring(0,i);
			s=s.substring(i+LINE_STEP4.length());
			i=s.indexOf(LINE_STEP5);
			if (i<0) continue;
			final String page=s.substring(0,i);

			/* Seitenzahl ist eine Zahl? */
			final Integer I=NumberTools.getNotNegativeInteger(page);
			if (I==null) continue;

			/* Neuen TOC-Eintrag erstellen */
			final BookSection section=new BookSection(sectionID,sectionName,I.intValue());
			toc.add(section);

			/* Zuordnung Inhaltverzeichniseinträge zu Seiten */
			if (previousSection!=null) addSectionPages(previousSection,previousSection.page,Math.max(previousSection.page,section.page-1));
			previousSection=section;
		}

		tocNameLower=new String[toc.size()];
		for (int i=0;i<toc.size();i++) tocNameLower[i]=toc.get(i).name.toLowerCase();
	}

	/**
	 * Interpretiert die Seitenzahl-Offset-Datei.
	 * @param list	Text der Seitenzahl-Offset-Datei
	 */
	private void processPageOffset(final List<String> list) {
		final List<Integer> pageOffsetBookPageRangeStartList=new ArrayList<>();
		final List<Integer> pageOffsetBookPageRangeEndList=new ArrayList<>();
		final List<Integer> pageOffsetList=new ArrayList<>();

		for (String line: list) {
			final int index1=line.indexOf(":");
			if (index1<0) continue;
			final Integer offset=NumberTools.getNotNegativeInteger(line.substring(index1+1).trim());
			if (offset==null) continue;
			final String range=line.substring(0,index1).trim();
			final int index2=range.indexOf("-");
			if (index2<0) continue;
			final Integer pageStart=NumberTools.getNotNegativeInteger(range.substring(0,index2).trim());
			final Integer pageEnd=NumberTools.getNotNegativeInteger(range.substring(index2+1).trim());
			if (pageStart==null || pageEnd==null || pageStart>pageEnd) continue;

			pageOffsetBookPageRangeStartList.add(pageStart);
			pageOffsetBookPageRangeEndList.add(pageEnd);
			pageOffsetList.add(offset);
		}

		pageOffsetBookPageRangeStart=pageOffsetBookPageRangeStartList.stream().mapToInt(Integer::intValue).toArray();
		pageOffsetBookPageRangeEnd=pageOffsetBookPageRangeEndList.stream().mapToInt(Integer::intValue).toArray();
		pageOffset=pageOffsetList.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Fügt ein Kapitel zu {@link #pageToTOC} hinzu.
	 * @param section	Kapitel
	 * @param pageFrom	Startseite ab der dieses Kapitel gelten soll
	 * @param pageTo	Endseite bis zu der dieses Kapitel gelten soll
	 * @see #pageToTOC
	 * @see #processTOC(List)
	 */
	private void addSectionPages(final BookSection section, final int pageFrom, final int pageTo) {
		if (pageToTOC.length<pageTo+1) pageToTOC=Arrays.copyOf(pageToTOC,pageTo*2);
		for (int i=pageFrom;i<=pageTo;i++) pageToTOC[i]=section;
	}

	/**
	 * Liefert den Inhaltsverzeichnisdatensatz zu einer Seite.
	 * @param page	Seitennummer
	 * @return	Zugehöriger Inhaltsverzeichnisdatensatz (kann <code>null</code> sein, wenn kein passender Datensatz existiert)
	 */
	public BookSection getSection(final int page) {
		if (pageToTOC==null || page<0 || pageToTOC.length<=page) return null;
		return pageToTOC[page];
	}

	/**
	 * Liefert zu einer Kapitelnummer das zugehörige Kapitel
	 * @param id	Kapitelnummer
	 * @return	Kapitel oder <code>null</code>, wenn kein Kapitel mit der angegebenen Nummer existiert
	 */
	public BookSection getSection(final String id) {
		return toc.stream().filter(section->section.id.equals(id)).findFirst().orElse(null);
	}

	/**
	 * Liefert alle Inhaltsverzeichniseinträge, die zu einem Suchbegriff passen.
	 * @param searchString	Suchbegriff
	 * @return	Liste der passenden Inhaltsverzeichniseinträge (kann leer sein, ist aber nie <code>null</code>)
	 */
	public List<BookSection> getTOCMatch(final String searchString) {
		final List<BookSection> results=new ArrayList<>();
		if (searchString!=null && !searchString.isEmpty() && indexLower!=null) {
			final String lower=searchString.toLowerCase();
			for (int i=0;i<tocNameLower.length;i++) if (tocNameLower[i].contains(lower)) {
				results.add(toc.get(i));
			}
		}
		return results;
	}

	/**
	 * Liefert alle Sachverzeichniseinträge, die zu einem Suchbegriff passen.
	 * @param searchString	Suchbegriff
	 * @return	Liste der passenden Sachverzeichniseinträge (kann leer sein, ist aber nie <code>null</code>)
	 */
	public List<IndexMatch> getIndexMatches(final String searchString) {
		final List<IndexMatch> results=new ArrayList<>();
		if (searchString!=null && !searchString.isEmpty() && indexLower!=null) {
			final String lower=searchString.toLowerCase();
			for (int i=0;i<indexLower.length;i++) if (indexLower[i].contains(lower)) {
				final String name=indexNormal[i];
				final List<Integer> pages=index.get(name);
				results.add(new IndexMatch(name,pages.stream().mapToInt(Integer::intValue).toArray()));
			}
		}
		return results;
	}

	/**
	 * Stehen Informationen zu Inhaltsverzeichnis oder Sachverzeichnis zur Verfügung?
	 * @return	Liefert <code>true</code>, wenn Informationen zu Inhaltsverzeichnis oder Sachverzeichnis zur Verfügung stehen
	 */
	public boolean isDataAvailable() {
		return (!toc.isEmpty() || !index.isEmpty());
	}

	/**
	 * Liefert das Inhaltsverzeichnis zurück.
	 * @return	Inhaltsverzeichnis
	 */
	public List<BookSection> getTOC() {
		return toc;
	}

	/**
	 * Liefert das Sachverzeichnis zurück.
	 * @return	Sachverzeichnis
	 */
	public Map<String,List<Integer>> getIndex() {
		return index;
	}

	/**
	 * Liefert den Wert, der auf die Buchseitennummer addiert werden,
	 * um auf die Seitennummer in der pdf zu kommen.
	 * @param page	Buchseite für die der Offset berechnet werden soll
	 * @return	Differenz zwischen Seite 1 Buch und Seite 1 pdf
	 */
	public int getPageOffset(final int page) {
		for (int i=0;i<pageOffset.length;i++) {
			if (page>=pageOffsetBookPageRangeStart[i] && page<=pageOffsetBookPageRangeEnd[i]) return pageOffset[i];
		}

		return 0;
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Liefert eine kurze Inhaltsbeschreibung.
	 * @return	Inhaltsbeschreibung (kann leer sein, wenn keine Inhaltsbeschreibung verfügbar ist, ist aber nie <code>null</code>)
	 */
	public static String getContentInfo() {
		try(InputStream inputStream=BookData.class.getResourceAsStream("bookinfo/Content.txt")) {
			return String.join("\n",	loadTextFromRessource(inputStream));
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * Liefert Informationen zur Verfügbarkeit des Buches.
	 * @return	Informationen zur Verfügbarkeit des Buches (kann leer sein, wenn keine Informationen verfügbar sind, ist aber nie <code>null</code>)
	 */
	public static String getAvailableInfo() {
		try(InputStream inputStream=BookData.class.getResourceAsStream("bookinfo/Available.txt")) {
			return String.join("\n",	loadTextFromRessource(inputStream));
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * Interface zu einem Suchtreffer
	 * (Inhaltsverzeichniseintrag oder Sachverzeichniseintrag)
	 */
	public interface BookMatch {}

	/**
	 * Inhaltsverzeichniseintrag
	 */
	public static class BookSection implements BookMatch {
		/**
		 * URL-Vorlage für einzelne Kapitel
		 * @see #getChapterURL()
		 */
		private static final String BOOK_CHAPTER_URL="https://link.springer.com/chapter/10.1007/978-3-658-34668-3_%s";

		/**
		 * Kapitelnummer
		 */
		public final String id;

		/**
		 * Kapitelname
		 */
		public final String name;

		/**
		 * Seitennummer auf der das Kapitel beginnt
		 */
		public final int page;

		/**
		 * Konstruktor der Klasse
		 * @param id	Kapitelnummer
		 * @param name	Kapitelname
		 * @param page	Seitennummer auf der das Kapitel beginnt
		 */
		private BookSection(final String id, final String name, final int page) {
			this.id=id;
			this.name=name;
			this.page=page;
		}

		@Override
		public String toString() {
			return "<html><body>"+id+" <b>"+encodeHTMLentities(name)+"</b> ("+Language.tr("BookData.page")+" "+page+")</body></html>";
		}

		/**
		 * Liefert die Kapitel-URL für das aktuelle Kapitel.
		 * @return	Kapitel-URL
		 */
		public String getChapterURL() {
			final int index=id.indexOf(".");
			if (index<0) {
				return String.format(BOOK_CHAPTER_URL,id);
			} else {
				return String.format(BOOK_CHAPTER_URL,id.substring(0,index));
			}
		}
	}

	/**
	 * Sachverzeichniseintrag
	 */
	public static class IndexMatch implements BookMatch {
		/**
		 * Name des Eintrags
		 */
		public final String name;

		/**
		 * Seiten auf denen der Eintrag auftritt
		 */
		public final int[] pages;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Eintrags
		 * @param pages	Seiten auf denen der Eintrag auftritt
		 */
		private IndexMatch(final String name, final int[] pages) {
			this.name=name;
			this.pages=pages;
		}
	}
}
