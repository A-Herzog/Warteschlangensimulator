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
package mathtools.distribution.swing;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Diese Enumerations-Klasse hält von SimSystem verwendete die Icons für Toolbars und Menüs vor.
 * @author Alexander Herzog
 */
public enum SimSystemsSwingImages {
	/** Symbol "Dialogbutton 'Ok'" */
	OK("accept.png"),

	/** Symbol "Dialogbutton 'Abbruch'" */
	CANCEL("cancel.png"),

	/** Symbol "Kopieren" */
	COPY("page_copy.png"),

	/** Symbol "Als Tabelle kopieren" */
	COPY_AS_TABLE("Table.png"),

	/** Symbol "Als Bild kopieren" */
	COPY_AS_IMAGE("chart_curve.png"),

	/** Symbol "Einfügen (aus Zwischenablage) */
	PASTE("paste_plain.png"),

	/** Symbol "Aus Datei laden" */
	LOAD("folder_page_white.png"),

	/** Symbol "In Datei speichern" */
	SAVE("disk.png"),

	/** Symbol "Bearbeiten" */
	EDIT("chart_curve.png"),

	/** Symbol "Wert um 10 verringern" */
	NUMBER_DOWN_10("bullet_arrow_bottom.png"),

	/** Symbol "Wert um 1 verringern" */
	NUMBER_DOWN_1("bullet_arrow_down.png"),

	/** Symbol "Wert um 1 erhöhen" */
	NUMBER_UP_1("bullet_arrow_up.png"),

	/** Symbol "Wert um 10 erhöhen" */
	NUMBER_UP_10("bullet_arrow_top.png"),

	/** Symbol "Hilfe" */
	HELP("help.png");

	private final String name;
	private URL url;
	private Icon icon;

	SimSystemsSwingImages(final String name) {
		this.name=name;
	}

	/**
	 * Liefert die URL des Icons
	 * @return	URL des Icons
	 */
	public URL getURL() {
		if (url==null) url=getClass().getResource("res/"+name);
		assert(url!=null);
		return url;
	}

	/**
	 * Liefert das Icon
	 * @return	Icon
	 */
	public Icon getIcon() {
		if (icon==null) {
			final URL url=getURL();
			if (url!=null) icon=new ImageIcon(url);
		}
		assert(icon!=null);
		return icon;
	}

	/**
	 * Prüft, ob alle Icons vorhanden sind.
	 * @param output	Soll eine Statusmeldung auf der Konsole ausgegeben werden?
	 * @return	Gibt an, ob alle Icons existieren
	 */
	public static boolean checkAll(final boolean output) {
		boolean allOk=true;
		for (SimSystemsSwingImages image: values()) {
			final boolean ok=(image.getIcon()!=null);
			if (!ok) allOk=false;
			if (output) System.out.print(image.name+": "+(ok?"ok":"missing"));
		}
		return allOk;
	}
}
