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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Scanner zur Indizierung der Hilfe-Dateien.
 * @author Alexander Herzog
 * @see IndexSystem
 */
class IndexScanner {
	/**
	 * Index-Daten pro Sprache
	 */
	private final Map<String,Index> data;

	/**
	 * Beim Lesen der Hilfedateien zu verwendender Zeichensatz.
	 */
	private final Charset charset;

	/**
	 * Konstruktor der Klasse
	 * @param charset	Beim Lesen der Hilfedateien zu verwendender Zeichensatz
	 */
	public IndexScanner(final Charset charset) {
		data=new HashMap<>();
		this.charset=charset;
	}

	/**
	 * Konstruktor der Klasse
	 */
	public IndexScanner() {
		this(StandardCharsets.UTF_8);
	}

	/**
	 * Soll diese Datei erfasst werden?
	 * @param name	Dateiname
	 * @return	Liefert <code>true</code>, wenn die Datei erfasst werden soll
	 */
	private static boolean isScanFile(final String name) {
		if (name==null || name.trim().isEmpty()) return false;
		final String nameLower=name.toLowerCase();
		if (nameLower.endsWith(".html")) return true;
		if (nameLower.endsWith(".htm")) return true;
		return false;
	}

	/**
	 * Erfasst alle Dateien in einem Verzeichnis.
	 * @param dataPath	Zu erfassendes Verzeichnis
	 * @param index	Index-Objekt im dem die Daten erfasst werden sollen
	 * @return	Anzahl an gescannten Dateien
	 */
	private int scanPath(final Path dataPath, final Index index) {
		int scannedFiles=0;
		try (Stream<Path> walk=Files.walk(dataPath,1)) {
			final Iterator<Path> iterator=walk.iterator();
			while (iterator.hasNext()) {
				final Path file=iterator.next();
				if (file==null) continue;
				final Path fileName=file.getFileName();
				if (fileName==null) continue;
				final String name=fileName.toString();
				if (!isScanFile(name)) continue;
				final String text=String.join("\n",Files.lines(file,charset).toArray(String[]::new));
				if (text!=null) {
					index.scan(name,text);
					scannedFiles++;
				}
			}
		} catch (IOException e) {
			return scannedFiles;
		}

		return scannedFiles;
	}

	/**
	 * Erfasst alle Dateien in einem Verzeichnis.
	 * @param language	Sprachbezeichner über den später auf die Daten zugegriffen werden kann
	 * @param folder	Zu erfassendes Verzeichnis
	 * @param cls	Ausgangspunkt für die relativen Namen der Ressourcen
	 */
	public void scan(final String language, final String folder, final Class<?> cls) {
		if (folder==null || folder.trim().isEmpty() || language==null || language.trim().isEmpty()) return;

		final Index index=new Index(folder+"/");

		/* Pfad ermitteln */
		int scannedFiles=0;
		final URI uri;
		try {uri=cls.getResource(folder+"/").toURI();} catch (URISyntaxException e) {return;}
		if (uri.getScheme().equals("jar")) {
			try (FileSystem fileSystem=FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
				final String[] parts=cls.getName().split("\\.");
				final StringBuilder path=new StringBuilder();
				for (int i=0;i<parts.length-1;i++) {path.append(parts[i]); path.append("/");}
				scannedFiles=scanPath(fileSystem.getPath("/"+path.toString()+folder+"/"),index);
			} catch (IOException e) {
				return;
			}
		} else {
			scannedFiles=scanPath(Paths.get(uri),index);
		}

		/* Bestimmte Tokens löschen */
		final int limit=Math.max(20,scannedFiles/10); /* Tokens entfernen, die in mehr als 10% der Dateien auftreten */
		index.reduce(limit);

		data.put(language,index);
	}

	/**
	 * Liefert die Index-Daten für eine Sprache.
	 * @param language	Sprache
	 * @return	Index-Daten
	 */
	public Index getIndex(final String language) {
		return data.get(language);
	}

	/**
	 * Index-Daten für eine Sprache
	 */
	static class Index {
		/**
		 * Ressourcen-Basisverzeichnis für die Sprache
		 */
		private final String folder;

		/**
		 * Erfasste Seitentitel
		 * @see #getTitleHits(String)
		 */
		private final Map<String,String> titles;

		/**
		 * Inverse Zuordnung zu {@link #titles}
		 * @see #getPageName(String)
		 */
		private final Map<String,String> pages;

		/**
		 * Erfasste Index-Tokens
		 * @see #getIndexHits(String)
		 */
		private final Map<String,Set<String>> tokens;

		/**
		 * Konstruktor der Klasse
		 * @param folder	Ressourcen-Basisverzeichnis für die Sprache
		 */
		public Index(final String folder) {
			this.folder=folder;
			titles=new HashMap<>();
			pages=new HashMap<>();
			tokens=new HashMap<>();
		}

		/**
		 * Zeichen, die als Trenner zwischen mehreren Tokens gelten sollen
		 * @see #scan(String, String)
		 */
		private final static String TOKEN_SPLITTER=" \n\t,.;:/\\()[]|=";

		/**
		 * Erfasst eine Datei.
		 * @param name	Name der Datei in dem angegebenen Verzeichnis
		 * @param content	Inhalt der Datei
		 */
		public void scan(final String name, final String content) {
			/* Tokens erstellen */
			final StringBuilder tagName=new StringBuilder();
			final StringBuilder title=new StringBuilder();
			final StringBuilder currentToken=new StringBuilder();
			final Set<String> tokens=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

			boolean inTag=false;
			boolean inTitle=false;
			boolean inBody=false;

			for (char letter: content.toCharArray()) {
				if (letter=='<') {
					if (currentToken.length()>3) tokens.add(currentToken.toString());
					currentToken.setLength(0);
					inTitle=false;
					inTag=true;
					tagName.setLength(0);
					continue;
				}
				if (letter=='>') {
					inTag=false;
					final String s=tagName.toString();
					inTitle=s.equalsIgnoreCase("title");
					inBody=inBody || s.equals("body");
					continue;
				}
				if (inTag) {
					tagName.append(letter);
					continue;
				}
				if (inTitle) {
					title.append(letter);
					continue;
				}
				if (TOKEN_SPLITTER.contains(""+letter)) {
					if (currentToken.length()>3) tokens.add(currentToken.toString());
					currentToken.setLength(0);
					continue;
				}
				currentToken.append(letter);
			}

			/* Titel speichern */
			final String titleString=title.toString();
			titles.put(titleString,name);
			pages.put(name,titleString);

			/* Tokens speichern */
			for (String token: tokens) {
				Set<String> locations=this.tokens.get(token);
				if (locations==null) this.tokens.put(token,locations=new HashSet<>());
				locations.add(name);
			}
		}

		/**
		 * Tritt eines dieser Zeichen in einem Token auf, so wird er entfernt.
		 * @see #reduce(int)
		 */
		private static final String REMOVE_CHARS="()[]|'\"&01234567890\\-+*/:=";

		/**
		 * Entfernt Tokens mit ungültigen Zeichen und die in zu vielen Dateien auftreten
		 * aus der Übersicht.
		 * @param limit	Tokens (als zu generisch) entfernen, wenn sie häufiger als angegeben auftreten
		 * @see #REMOVE_CHARS
		 */
		public void reduce(final int limit) {
			final Set<String> keys=new HashSet<>(tokens.keySet());
			for (String key: keys) {
				boolean remove=false;
				for (char removeChar: REMOVE_CHARS.toCharArray()) if (key.contains(String.valueOf(removeChar))) {remove=true; break;}
				if (remove || tokens.get(key).size()>limit) tokens.remove(key);
			}
		}

		/**
		 * Liefert die Index-Treffer zu einem Suchbegriff.
		 * @param searchString	Suchbegriff
		 * @return	Index-Treffer (kann leer sein, ist aber nie <code>null</code>)
		 */
		public Map<String,Set<String>> getIndexHits(final String searchString) {
			final Map<String,Set<String>> results=new HashMap<>();
			if (searchString!=null && !searchString.trim().isEmpty()) {
				final String searchStringLower=searchString.trim().toLowerCase();
				for (Map.Entry<String,Set<String>> entry: tokens.entrySet()) {
					if (entry.getKey().toLowerCase().contains(searchStringLower)) {
						results.put(entry.getKey(),new HashSet<>(entry.getValue()));
					}
				}
			}
			return results;
		}

		/**
		 * Liefert die Seitentitel-Treffer zu einem Suchbegriff.
		 * @param searchString	Suchbegriff
		 * @return	Seitentitel-Treffer (kann leer sein, ist aber nie <code>null</code>)
		 */
		public Map<String,String> getTitleHits(final String searchString) {
			final Map<String,String> results=new HashMap<>();
			if (searchString!=null && !searchString.trim().isEmpty()) {
				final String searchStringLower=searchString.trim().toLowerCase();
				for (Map.Entry<String,String> entry: titles.entrySet()) {
					if (entry.getKey().toLowerCase().contains(searchStringLower)) {
						results.put(entry.getKey(),entry.getValue());
					}
				}
			}
			return results;
		}

		/**
		 * Liefert den Titel der Seite zu einem Dateiname.
		 * @param page	Dateiname
		 * @return	Titel der Seite oder <code>null</code>, wenn zu den angegebenen Dateinamen keine passende Seite existiert
		 */
		public String getPageName(final String page) {
			return pages.get(page);
		}

		/**
		 * Liefert das Ressourcen-Basisverzeichnis für die Sprache.
		 * @return	Ressourcen-Basisverzeichnis für die Sprache
		 */
		public String getFolder() {
			return folder;
		}
	}
}