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
package simulator.editmodel;

import xml.XMLData;

/**
 * Diese Klasse enthält einige allgemeine Hilfsfunktionen, die später innerhalb der Editor-Modell-Klasse verwendet werden können.
 */
public abstract class EditModelBase extends XMLData {
	/**
	 * Konstruktor der Klasse
	 */
	public EditModelBase() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Vergleich zwei Versionsnummern der Form x.y.z in Bezug auf die Komponenten x und y miteinander
	 * @param currentVersion	Programmversion
	 * @param dataVersion	Datendateiversion
	 * @return	Liefert <code>true</code> zurück, wenn die Datendateiversion neuer als die Programmversion ist
	 */
	public static boolean isNewerVersion(String currentVersion, String dataVersion) {
		if (dataVersion==null || dataVersion.isEmpty()) return false;
		if (currentVersion==null || currentVersion.isEmpty()) return false;
		String[] newVer=dataVersion.split("\\.");
		String[] curVer=currentVersion.split("\\.");
		if (newVer.length<3 || curVer.length<3) return false;

		try {
			int new1=Integer.parseInt(newVer[0]);
			int new2=Integer.parseInt(newVer[1]);
			int cur1=Integer.parseInt(curVer[0]);
			int cur2=Integer.parseInt(curVer[1]);
			return (new1>cur1 || (new1==cur1 && new2>cur2));
		} catch (NumberFormatException e) {return false;}
	}

	/**
	 * Prüft ob die angegebene Datendateiversion neuer als die in dem Feld <code>systemVersion</code> hinterlegte Version ist
	 * @param dataVersion	Datendateiversion
	 * @param systemVersion	Systemversion
	 * @return	Liefert <code>true</code> wenn die Datendateiversion in Bezug auf die ersten zwei Komponenten neuer als die in dem Feld <code>systemVersion</code> hinterlegte Version ist
	 */
	public static boolean isNewerVersionSystem(String dataVersion, String systemVersion) {
		return isNewerVersion(systemVersion,dataVersion);
	}

	/**
	 * Prüft ob die angegebene Datendateiversion älter als die in dem Feld <code>systemVersion</code> hinterlegte Version ist
	 * @param dataVersion	Datendateiversion
	 * @param systemVersion	Systemversion
	 * @return	Liefert <code>true</code> wenn die Datendateiversion in Bezug auf die ersten zwei Komponenten älter als die in dem Feld <code>systemVersion</code> hinterlegte Version ist
	 */
	public static boolean isOlderVersionSystem(String dataVersion, String systemVersion) {
		return isNewerVersion(dataVersion,systemVersion);
	}

	/**
	 * Prüft, ob die Datendatei- und die Systemversion übereinstimmen (in Bezug auf die ersten beiden Komponenten)
	 * @param dataVersion	Datendateiversion
	 * @param systemVersion	Systemversion
	 * @return	Liefert <code>true</code> wenn die Datendatei- und die Systemversion in Bezug auf die ersten zwei Komponenten identisch sind
	 */
	public static boolean isOtherVersionSystem(String dataVersion, String systemVersion) {
		return isNewerVersion(dataVersion,systemVersion) || isNewerVersion(systemVersion,dataVersion);
	}

}
