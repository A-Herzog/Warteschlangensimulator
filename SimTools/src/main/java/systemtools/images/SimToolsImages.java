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
package systemtools.images;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Diese Enumerations-Klasse hält von SimTools verwendete die Icons für Toolbars und Menüs vor.
 * @author Alexander Herzog
 */
public enum SimToolsImages {

	/** Symbol "Dialogbutton 'Ok' */
	OK("accept.png"),

	/** Symbol "Dialogbutton 'Abbruch' */
	CANCEL("cancel.png"),

	/** Symbol "Dialogbutton 'Beenden' */
	EXIT("door_in.png"),

	/** Symbol "Information' */
	INFO("information.png"),

	/** Symbol "Fehler' */
	ERROR("error.png"),

	/** Symbol "Kopieren" (in Zwischenablage) */
	COPY("copy.gif"),

	/** Symbol "Drucken" */
	PRINT("printer.png"),

	/** Symbol "In externer Anwendung öffnen" */
	OPEN("application_go.png"),

	/** Symbol "Speichern" */
	SAVE("disk.png"),

	/** Symbol "Als Text speichern" */
	SAVE_TEXT("Text.gif"),

	/** Symbol "Text direkt zu Word übertragen" */
	SAVE_TEXT_WORD("page_word.png"),

	/** Symbol "Als Tabelle speichern" */
	SAVE_TABLE("Table.png"),

	/** Symbol "Tabelle direkt zu Excel übertragen" */
	SAVE_TABLE_EXCEL("page_excel.png"),

	/** Symbol "Zoom" */
	ZOOM("zoom.png"),

	/** Symbol "Einstellungen" */
	SETUP("wrench.png"),

	/** Symbol "Hinzufügen" */
	ADD("add.png"),

	/** Symbol "Löschen" */
	DELETE("delete.png"),

	/** Symbol "Ausklappen" (Plus) */
	PLUS("SmallPlus2.png"),

	/** Symbol "Einklappen" (Minus) */
	MINUS("SmallMinus2.png"),

	/** Symbol "Nach links" */
	ARROW_LEFT("arrow_left.gif"),

	/** Symbol "Nach rechts" */
	ARROW_RIGHT("arrow_right.gif"),

	/** Symbol "Nach oben" */
	ARROW_UP("arrow_up.png"),

	/** Symbol "Nach unten" */
	ARROW_DOWN("arrow_down.png"),

	/** Symbol "Hilfe" */
	HELP("help.png"),

	/** Symbol "Startseite" (in der Hilfe) */
	HELP_HOME("house.png"),

	/** Symbol "Zurück" (in der Hilfe) */
	HELP_BACK("resultset_previous.png"),

	/** Symbol "Weiter" (in der Hilfe) */
	HELP_NEXT("resultset_next.png"),

	/** Symbol "Suchen" (in der Hilfe) */
	HELP_FIND("find.png"),

	/** Symbol "Ebene 1" (in der Hilfe) */
	HELP_MARKER_LEVEL1("flag_red.png"),

	/** Symbol "Ebene 2" (in der Hilfe) */
	HELP_MARKER_LEVEL2("resultset_next.png"),

	/** Symbol "Kommandozeile" */
	COMMAND_LINE("application_xp_terminal.png"),

	/** Symbol in der Statistik "Funktionen zum Ein- und Ausklappen von Kategorien" (Toolbar-Icon) */
	STATISTICS_LISTTOOLS("text_list_bullets.png"),

	/** Symbol in der Statistik "Mit vorherigen Statistikergebnissen vergleichen" (Toolbar-Icon) */
	STATISTICS_COMPARE_LAST("application_tile_horizontal.png"),

	/** Symbol in der Statistik "Zusammenfassung" */
	STATISTICS_REPORT("report.png"),

	/** Symbol in der Statistik "Schnellzugriff" */
	STATISTICS_SPECIAL("lightning_go.png"),

	/** Symbol in der Statistik "Text" */
	STATISTICS_TEXT("Text.gif"),

	/** Symbol in der Statistik "Tabelle" */
	STATISTICS_TABLE("Table.gif"),

	/** Symbol in der Statistik "Liniendiagramm" */
	STATISTICS_DIAGRAM_LINE("chart_curve.png"),

	/** Symbol in der Statistik "Balkendiagramm" */
	STATISTICS_DIAGRAM_BAR("chart_bar.png"),

	/** Symbol in der Statistik "Tortendiagramm" */
	STATISTICS_DIAGRAM_PIE("chart_pie.png"),

	/** Symbol in der Statistik "Bild" */
	STATISTICS_DIAGRAM_PICTURE("image_picture.gif"),

	/** Symbol in der Statistik "X-Y-Diagramm" */
	STATISTICS_DIAGRAM_XY("chart_xy.png"),

	/** Symbol in der Statistik "Schichtplan" */
	STATISTICS_DIAGRAM_SHIFT_PLAN("clock.png"),

	/* Meldungsdialoge */

	/** Symbol "Dialog-Button 'Ja'" */
	MSGBOX_YES("tick.png"),

	/** Symbol "Dialog-Button 'Ja, speichern'" */
	MSGBOX_YES_SAVE("disk.png"),

	/** Symbol "Dialog-Button 'Nein'" */
	MSGBOX_NO("cancel.png"),

	/** Symbol "Dialog-Button 'Abbruch/Zurück'" */
	MSGBOX_CANCEL("arrow_redo2.png");

	/**
	 * Dateiname des Icons
	 */
	private final String name;

	/**
	 * URL des Icons
	 */
	private URL url;

	/**
	 * Icon
	 */
	private Icon icon;

	/**
	 * Konstruktor des Enum
	 * @param name	Dateiname des Icons
	 */
	SimToolsImages(final String name) {
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
	 */
	public static void checkAll() {
		for (SimToolsImages image: values()) {
			System.out.print(image.name+": ");
			if (image.getIcon()==null) System.out.println("missing"); else System.out.println("ok");
		}
	}
}
