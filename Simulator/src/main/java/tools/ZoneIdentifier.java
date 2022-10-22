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
package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mathtools.NumberTools;

/**
 * Diese Klasse stellt statische Hilfsroutinen zur Verfügung mit denen geprüft
 * werden kann, ob eine Datei aus dem Internet heruntergeladen wurde. (Es wird
 * dafür unter Windows der "Zone.Identifier"-Alternative Data Stream ausgelesen.)
 * @author Alexander Herzog
 */
public class ZoneIdentifier {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
	 */
	private ZoneIdentifier() {
	}

	/**
	 * Liest den "Zone.Identifier"-Alternative Data Stream aus einer Datei aus
	 * und liefert die Daten als uninterpretierte Textzeilen zurück
	 * @param file	Zu verarbeitende Datei
	 * @return	"Zone.Identifier"-Alternative Data Stream in Form von Textzeilen (kann leer sein, wenn kein ADS vorhanden ist, ist aber nie <code>null</code>)
	 */
	public static List<String> getRawZoneIdentifier(final File file) {
		/* Existiert die Datei überhaupt? */
		if (file==null) return new ArrayList<>();
		if (!file.isFile()) return new ArrayList<>();

		/* "Zone.Identifier"-Alternative Data Stream auslesen */
		final File adsFile = new File(file.toString()+":Zone.Identifier");
		try (BufferedReader bf = new BufferedReader( new FileReader(adsFile))) {
			final List<String> lines=new ArrayList<>();
			String line;
			while ((line=bf.readLine())!=null) lines.add(line);
			return lines;
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	/**
	 * Liest den "Zone.Identifier"-Alternative Data Stream aus einer Datei aus
	 * und liefert die interpretierten INI-Daten als Zuordnung zurück.<br>
	 * Es wird die [ZoneTransfer]-Sektion gelesen. Darin sind üblicherweise die
	 * Schlüssel "ZoneId", "ReferrerUrl" und "HostUrl" enthalten. Ausgelesen
	 * und Bereitgestellt werden aber alle Schlüssel in der Sektion.
	 * @param file	Zu verarbeitende Datei
	 * @return	"Zone.Identifier"-Alternative Data Stream Daten in Form einer Zuordnung (kann leer sein, wenn kein ADS vorhanden ist, ist aber nie <code>null</code>)
	 */
	public static Map<String,String> getZoneIdentifier(final File file) {
		final Map<String,String> result=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		final List<String> lines=getRawZoneIdentifier(file);

		boolean inZoneIdentifier=false;

		for (String line: lines) {
			line=line.trim();

			if (line.startsWith("[") && line.endsWith("]") && line.length()>2) {
				final String section=line.substring(1,line.length()-1);
				inZoneIdentifier=section.equalsIgnoreCase("ZoneTransfer");
				continue;
			}

			if (line.contains("=")) {
				if (inZoneIdentifier) {
					final String[] parts=line.split("=",2);
					result.put(parts[0],parts[1]);
				}
				continue;
			}
		}

		return result;
	}

	/**
	 * Prüft, ob die Datei aus dem Internet stammt.
	 * @param file	Zu prüfende Datei
	 * @return	Liefert <code>true</code>, wenn die Datei aus dem Netz stammt.
	 */
	public static boolean isFileFromInternet(final File file) {
		final Map<String,String> zoneIdentifier=getZoneIdentifier(file);
		final Integer id=NumberTools.getInteger(zoneIdentifier.get("ZoneId"));
		if (id==null) return false;

		/* Werte für den ZoneId-Schlüssel: 0: lokal, 1: Intranet, 2: Trusted sites, 3: Internet, 4: Restricted sites */
		return (id==3 || id==4);
	}
}
