/**
 * Copyright 2022 Alexander Herzog
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
package ui.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fife.com.swabunga.spell.engine.SpellDictionary;
import org.fife.com.swabunga.spell.engine.Word;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;

import dumonts.hunspell.Hunspell;
import tools.SetupData;

/**
 * Diese Singleton-Klasse kapselt alle Hunspell-Wörterbücher und ermöglicht es,
 * direkt {@link SpellingParser} zu generieren.
 * @author Alexander Herzog
 * @see HunspellDictionaries
 * @see RSyntaxTextArea
 * @see SpellDictionary
 * @see SpellingParser
 */
public class HunspellDictionaries {
	/**
	 * Instanz dieses Singletons
	 * @see #getInstance()
	 */
	private static volatile HunspellDictionaries instance;

	/**
	 * Liste der verfügbaren Sprachdatensätze
	 */
	private final List<HunspellDictionaryRecord> records;

	/**
	 * Datei mit nutzerdefinierten Wörtern (im selben Verzeichnis wie die Einstellungendatei)
	 * @see #getUserDictionaryFile()
	 */
	private static final String USER_SPELL_FILE="User-spelling.cfg";

	/**
	 * Verzögerung nach dem Aufruf von {@link #initPreloadDirectories()} bis der
	 * eigentliche Ladevorgang (im Hintergrund) startet.
	 */
	private static final int INIT_WAIT_SECONDS=1;

	/**
	 * Fest eingebaute Wörterbuchbegriffe (alles in Kleinbuchstaben)
	 * @see HunspellDictionary#isCorrect(String)
	 */
	public static final Set<String> internalDictionary=new HashSet<>(Arrays.asList(
			/* Allgemein */
			"bzw",
			"usw",
			"sek",
			/* Eigennamen */
			"warteschlangensimulator",
			"erlang",
			"erlang-",
			"erlang-b",
			"erlang-c",
			"rechner",
			"formel",
			"erlang-c-formel",
			"erlang-c-rechner",
			/* Begriffe aus der Warteschlangentheorie */
			"abbrecher",
			"abbrecherquote",
			"wiederholern",
			"wiederholer",
			"warteschlangentheorie",
			"warteschlangenmodell",
			"wartezeitabhängige",
			"zwischenankunftszeitenverteilung",
			"zwischenankunfts",
			"nachbearbeitungszeiten",
			"exponentialverteilung",
			"exponentialverteilt",
			"exp-verteilt",
			"erlang-verteilt",
			"poisson-verteilung",
			"zählvariable",
			"batche",
			"batchen",
			"2er",
			"3er",
			"push-produktion",
			"pull-produktion",
			"pull-Schranke",
			"nachgelagerten",
			"lastabhängige",
			"auslastungszustände",
			"rüstzeit",
			"rüstzeiten",
			"kundentypwechselt",
			"nachbearbeitet",
			"zuflussrate",
			"zeitkontinuierlicher",
			"zeitkontinuierlichen",
			"multi-skill-agenten",
			"cancelations",
			"cancelation",
			"queueing",
			"retryers",
			"galton",
			"vergleichsmodell",
			/* Bezeichner */
			"mu",
			"lambda",
			"rho",
			"nu",
			"infty",
			"cv",
			"var",
			"e",
			"std",
			"kurt",
			"nq",
			"n",
			/* Internet */
			"https",
			"http",
			"www",
			"tu-clausthal",
			"mathematik",
			"warteschlangenrechnerdesign",
			"wikipedia",
			"galtonbrett",
			"stochastik",
			"zentralergrenzwertsatz",
			/* Programmcode */
			"getoutput",
			"println",
			"getruntime",
			"getmapglobal",
			"getclass",
			"getname",
			"getoutput",
			"getmaplocal",
			"getclass",
			"getname"
			));

	/**
	 * Liefert (bzw. generiert wenn nötig) die Instanz dieses Singletons
	 * @return	Instanz dieses Singletons
	 */
	public static synchronized HunspellDictionaries getInstance() {
		if (instance==null) instance=new HunspellDictionaries();
		return instance;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt ein Singleton dar und kann daher nicht von außen instanziert werden.
	 * @see #getInstance()
	 */
	private HunspellDictionaries() {
		records=new ArrayList<>();
		final File folder1=SetupData.getProgramFolder();
		final File folder2=SetupData.getSetupFolder();
		records.addAll(HunspellDictionaryRecord.getAvailableRecords(new File(folder1,"dictionaries")));
		if (!folder1.equals(folder2)) records.addAll(HunspellDictionaryRecord.getAvailableRecords(new File(folder2,"dictionaries")));
		records.addAll(HunspellDictionaryRecord.getAvailableRecords(new File(new File(folder2,"build"),"Dictionaries")));
	}

	/**
	 * Lädt die Wörterbücher vorab, um den ersten realen Zugriff zu beschleunigen.
	 */
	public static void initPreloadDirectories() {
		new Thread(()->{
			try {Thread.sleep(INIT_WAIT_SECONDS*1000);} catch (InterruptedException e) {}
			if (instance!=null) return; /* Wurde schon anderweitig initialisiert, keine weitere Verarbeitung. */
			HunspellDictionaries.getInstance().getSpellDictionary();
		},"SpellingDictionariesLoader").start();
	}

	/**
	 * Liefert eine Auflistung der verfügbaren Sprachdatensätze
	 * @return	Auflistung der verfügbaren Sprachdatensätze
	 */
	public Set<String> getAvailableLocales() {
		return records.stream().map(record->record.locale).collect(Collectors.toSet());
	}

	/**
	 * Aktive Sprachen beim letzten Aufruf von {@link #getSpellDictionary()}
	 * (um dann {@link #lastHunspellDictionary} wiederverwenden zu können)
	 */
	private String lastSetup;

	/**
	 * Generiertes {@link HunspellDictionary} beim letzten Aufruf von {@link #getSpellDictionary()}
	 * (zur Wiederverwendung beim nächsten Aufruf)
	 */
	private HunspellDictionary lastHunspellDictionary;

	/**
	 * Liefert ein {@link SpellDictionary}-Objekt gemäß den aktiven Sprachdatensätzen.
	 * @return	{@link SpellDictionary}-Objekt gemäß den aktiven Sprachdatensätzen
	 */
	public SpellDictionary getSpellDictionary() {
		final String setup=SetupData.getSetup().spellCheckingLanguages;
		if (lastSetup==null || lastHunspellDictionary==null || !lastSetup.equals(setup)) {
			final Set<String> activeLocales=Stream.of(setup.split(";")).map(s->s.toLowerCase()).collect(Collectors.toSet());
			final Hunspell[] hunspells=records.stream().filter(record->activeLocales.contains(record.locale.toLowerCase())).map(record->record.getHunspell()).filter(hunspell->hunspell!=null).toArray(Hunspell[]::new);
			lastSetup=setup;
			lastHunspellDictionary=new HunspellDictionary(hunspells);
		}
		return lastHunspellDictionary;
	}

	/**
	 * Liefert den vollständigen Pfad zu der Datei mit den nutzerdefinierten Wörtern.
	 * @return	Datei mit den nutzerdefinierten Wörtern
	 */
	public static File getUserDictionaryFile() {
		return new File(SetupData.getSetupFolder(),USER_SPELL_FILE);
	}

	/**
	 * Liefert die Liste der nutzerdefinierten Wörter.
	 * @return	Liste der nutzerdefinierten Wörter (kann leer sein, ist aber nie <code>null</code>).
	 * @see #setUserDictionaryWords(List)
	 */
	public static List<String> getUserDictionaryWords() {
		final File file=getUserDictionaryFile();
		if (!file.isFile()) return new ArrayList<>();

		final List<String> list=new ArrayList<>();
		try (BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file),StandardCharsets.ISO_8859_1))) {
			String line;
			while ((line=br.readLine())!=null) {
				line=line.trim();
				if (!line.isEmpty()) list.add(line);
			}
		} catch (IOException e) {}
		return list;
	}

	/**
	 * Stellt die Liste der nutzerdefinierten Wörter ein.
	 * @param words	Liste der nutzerdefinierten Wörter (darf leer oder auch <code>null</code> sein)
	 * @return	Liefert <code>true</code>, wenn die neue Liste gespeichert werden konnte.
	 * @see #getUserDictionaryWords()
	 */
	public static boolean setUserDictionaryWords(final List<String> words) {
		final List<String> list;
		if (words==null) {
			list=new ArrayList<>();
		} else {
			list=words.stream().map(line->line.trim()).filter(line->!line.isEmpty()).collect(Collectors.toList());
		}
		list.add("");

		try {
			Files.write(getUserDictionaryFile().toPath(),String.join("\n",list.toArray(new String[0])).getBytes(StandardCharsets.ISO_8859_1),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Liefert ein {@link SpellingParser}-Objekt gemäß den aktiven Sprachdatensätzen.
	 * @return	{@link SpellingParser}-Objekt gemäß den aktiven Sprachdatensätzen
	 */
	public SpellingParser getSpellingParser() {
		final SpellingParser spellingParser=new SpellingParser(getSpellDictionary());
		try {
			spellingParser.setUserDictionary(getUserDictionaryFile());
		} catch (IOException e) {}
		return spellingParser;
	}

	/**
	 * Hunspell-Implementierung von {@link SpellDictionary}
	 * @see HunspellDictionaries#getSpellDictionary()
	 */
	private static class HunspellDictionary implements SpellDictionary {
		/**
		 * Bei der Prüfung zu verwendende {@link Hunspell}-Objekte (kann leer sein, ist aber nie <code>null</code>)
		 */
		private final Hunspell[] hunspells;

		/**
		 * Konstruktor der Klasse
		 * @param hunspells	Bei der Prüfung zu verwendende {@link Hunspell}-Objekte (kann leer oder <code>null</code> sein)
		 */
		public HunspellDictionary(final Hunspell[] hunspells) {
			this.hunspells=(hunspells==null)?new Hunspell[0]:hunspells;
		}

		@Override
		public boolean addWord(final String word) {
			return true; /* Wird nie aufgerufen. SpellingParser verwendet ein komplett eigenständiges System. */
		}

		@Override
		public boolean isCorrect(final String word) {
			if (hunspells.length==0) return true;
			for (Hunspell hunspell: hunspells) if (hunspell.spell(word)) return true;
			final String lower=word.toLowerCase();
			if (internalDictionary.contains(lower)) return true;
			return false;
		}

		/**
		 * Liefert eine Liste mit möglichen Ersetzungsvorschlägen für ein Wort
		 * @param word	Wort für das die Ersetzungsvorschläge generiert werden sollen
		 * @return	Ersetzungsvorschläge
		 * @see #getSuggestions(String, int)
		 * @see #getSuggestions(String, int, int[][])
		 */
		private List<Word> getSuggestions(final String word) {
			final Map<String,Integer> suggestions=new HashMap<>();

			for (Hunspell hunspell: hunspells) {
				final List<String> list=hunspell.suggest(word);
				if (list!=null) {
					final int size=list.size();
					for (int i=0;i<size;i++) {
						final int score=size-i;
						suggestions.compute(list.get(i),(k,v)->(v==null)?score:Math.max(v,score));
					}
				}
			}

			final List<Word> result=new ArrayList<>();
			suggestions.forEach((k,v)->result.add(new Word(k,v)));
			return result;
		}

		@Override
		public List<Word> getSuggestions(String sourceWord, int scoreThreshold) {
			return getSuggestions(sourceWord);
		}

		@Override
		public List<Word> getSuggestions(String sourceWord, int scoreThreshold, int[][] matrix) {
			return getSuggestions(sourceWord);
		}
	}
}
