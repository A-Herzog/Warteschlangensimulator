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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * System zur Indizierung der Hilfe-Dateien und
 * zum Suchen in den Index-Treffern.
 * @author Alexander Herzog
 */
public class IndexSystem {
	/**
	 * Singleton-Instanz der Klasse
	 */
	private static IndexSystem instance;

	/**
	 * Verzögerung nach dem Aufruf von {@link #init(Class)} bis der
	 * eigentliche Indizierungsvorgang (im Hintergrund) startet.
	 */
	private static final int INIT_WAIT_SECONDS=2;

	/**
	 * Zu indizierende Ordner.
	 * @see #addLanguage(String, String)
	 * @see #init(Class)
	 */
	private Map<String,String> indexFolders;

	/**
	 * Ist der Indizierungs-Thread mit seiner Arbeit fertig?
	 * @see #isReady()
	 */
	private volatile boolean ready;

	/**
	 * Index-Scanner-System<br>
	 * (wird vom Hintergrund-Thread gestartet und wird später
	 * über die Methoden in dieser Klasse bereitgestellt)
	 */
	private IndexScanner scanner;

	/**
	 * Aktuelle Sprache
	 */
	private String currentLanguage;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt ein Singleton dar und kann daher nicht instanziert werden.
	 * @see #getInstance()
	 */
	private IndexSystem() {
		indexFolders=new HashMap<>();
		ready=false;
	}

	/**
	 * Liefert die Instanz des Singletons
	 * @return	Instanz des Singletons
	 */
	public static synchronized IndexSystem getInstance() {
		if (instance==null) instance=new IndexSystem();
		return instance;
	}

	/**
	 * Benennt ein Verzeichnis in dem sich zu indizierende Dateien befinden.
	 * @param language	Sprachbezeichner über den später auf die Daten zugegriffen werden kann
	 * @param folder	Ressourcen-Verzeichnis relativ zu dem Ort dieser Klasse
	 */
	public void addLanguage(final String language, final String folder) {
		indexFolders.put(language,folder);
		if (currentLanguage==null) currentLanguage=language;
	}

	/**
	 * Startet den (Hintergrund-)Indizierungsvorgang nach dem zuvor
	 * per {@link #addLanguage(String, String)} Verzeichnisse zum
	 * Indizieren angegeben wurden.
	 * @param cls	Ausgangspunkt für die relativen Namen der Ressourcen
	 */
	public void init(final Class<?> cls) {
		new Thread(()->{
			try {Thread.sleep(INIT_WAIT_SECONDS*1000);} catch (InterruptedException e) {}
			initIntern(cls);
		},"HelpIndexScanner").start();
	}

	/**
	 * Wurde die Indizierung abgeschlossen?
	 * @return	Liefert <code>true</code>, wenn die Indizierung abgeschlossen wurde.
	 * @see #init(Class)
	 * @see #getTitleHits(String)
	 * @see #getIndexHits(String)
	 */
	public boolean isReady() {
		return ready;
	}

	/**
	 * Führt die eigentliche Indizierung (im Vordergrund aus).<br>
	 * Diese Methode sollte vom Hintergrund-Indizierungs-Thread aus aufgerufen werden.
	 * @param cls	Ausgangspunkt für die relativen Namen der Ressourcen
	 * @see #init(Class)
	 */
	private void initIntern(final Class<?> cls) {
		scanner=new IndexScanner();
		for (Map.Entry<String,String> entry: indexFolders.entrySet()) {
			scanner.scan(entry.getKey(),entry.getValue(),cls);
		}
		ready=true;
	}

	/**
	 * Stellt den Namen der aktuell aktiven Sprache ein.
	 * @param language	Aktuell aktive Sprache
	 */
	public void setLanguage(final String language) {
		currentLanguage=language;
	}

	/**
	 * Liefert die Index-Treffer zu einem Suchbegriff.<br>
	 * Liefert ein leeres Ergebnis, wenn die Indizierung noch nicht abgeschlossen wurde, {@link #isReady()} muss nicht manuell geprüft werden.
	 * @param searchString	Suchbegriff
	 * @return	Index-Treffer (kann leer sein, ist aber nie <code>null</code>)
	 */
	public Map<String,Set<String>> getIndexHits(final String searchString) {
		if (!ready || currentLanguage==null) return new HashMap<>();
		return scanner.getIndex(currentLanguage).getIndexHits(searchString);
	}

	/**
	 * Liefert die Seitentitel-Treffer zu einem Suchbegriff.<br>
	 * Liefert ein leeres Ergebnis, wenn die Indizierung noch nicht abgeschlossen wurde, {@link #isReady()} muss nicht manuell geprüft werden.
	 * @param searchString	Suchbegriff
	 * @return	Seitentitel-Treffer (kann leer sein, ist aber nie <code>null</code>)
	 */
	public Map<String,String> getTitleHits(final String searchString) {
		if (!ready || currentLanguage==null) return new HashMap<>();
		return scanner.getIndex(currentLanguage).getTitleHits(searchString);
	}

	/**
	 * Liefert den Titel der Seite zu einem Dateiname.
	 * @param page	Dateiname
	 * @return	Titel der Seite oder <code>null</code>, wenn zu den angegebenen Dateinamen keine passende Seite existiert
	 */
	public String getPageName(final String page) {
		if (!ready || currentLanguage==null) return null;
		return scanner.getIndex(currentLanguage).getPageName(page);
	}
}
