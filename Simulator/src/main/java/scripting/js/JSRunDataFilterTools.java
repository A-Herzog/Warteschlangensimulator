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
package scripting.js;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import mathtools.Table;

/**
 * Diese Klasse stellt statische Hilfsmethoden zum Umgang mit
 * Filterskripten bereit.
 * @author Alexander Herzog
 */
public class JSRunDataFilterTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Hilfefunktionen bereit.
	 */
	private JSRunDataFilterTools() {}

	/**
	 * Speichert einen Text (z.B. Filter-Script oder Filter-Ergebnisse) in einer Datei
	 * @param text	Zu speichernder Text
	 * @param output	Ausgabedatei, in der der Text gespeichert werden soll
	 * @param append	Ist dieser Parameter <code>true</code> so werden die Zeilen (so fern die Datei bereits existiert) angehängt. Andernfalls wird die Datei ggf. überschrieben.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	public static boolean saveText(String text, File output, boolean append) {
		try {
			try (BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output,append),StandardCharsets.UTF_8))) {
				bw.write(text);
			}
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Lädt einen Text (z.B. ein Filter-Script) aus einer Datei.
	 * @param input	Zu lesende Eingabedatei.
	 * @return	Gibt im Erfolgsfall den Dateiinhalt zurück, sonst <code>null</code>.
	 */
	public static String loadText(final File input) {
		return Table.loadTextFromFile(input);
	}
}
