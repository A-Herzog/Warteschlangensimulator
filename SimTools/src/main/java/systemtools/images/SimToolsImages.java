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

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
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
	COPY("page_copy.png"),

	/** Symbol "Drucken" */
	PRINT("printer.png"),

	/** Symbol "In externer Anwendung öffnen" */
	OPEN("application_go.png"),

	/** Symbol "Speichern" */
	SAVE("disk.png"),

	/** Symbol "Suchen" */
	SEARCH("find.png"),

	/** Symbol "Als Text speichern" */
	SAVE_TEXT("Text.png"),

	/** Symbol "Text direkt zu Word übertragen" */
	SAVE_TEXT_WORD("page_word.png"),

	/** Symbol "Als Tabelle speichern" */
	SAVE_TABLE("Table.png"),

	/** Symbol "Tabelle direkt zu Excel übertragen" */
	SAVE_TABLE_EXCEL("page_excel.png"),

	/** Symbol "Als pdf speichern" */
	SAVE_PDF("page_white_acrobat.png"),

	/** Symbol "Zoom" */
	ZOOM("zoom.png"),

	/** Symbol "Vollbild" */
	FULLSCREEN("application.png"),

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

	/** Symbol "Rückgängig" */
	UNDO("arrow_undo.png"),

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

	/** Symbol "Inhalt der Seite" (in der Hilfe) */
	HELP_FIND_IN_PAGE("page_white_find.png"),

	/** Symbol "Seite" (in der Hilfe) */
	HELP_PAGE("page.png"),

	/** Symbol "Suchen" (in der Hilfe) */
	HELP_SEARCH("find.png"),

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
	STATISTICS_TEXT("Text.png"),

	/** Symbol in der Statistik "Tabelle" */
	STATISTICS_TABLE("Table.png"),

	/** Symbol in der Statistik "Liniendiagramm" */
	STATISTICS_DIAGRAM_LINE("chart_curve.png"),

	/** Symbol in der Statistik "Balkendiagramm" */
	STATISTICS_DIAGRAM_BAR("chart_bar.png"),

	/** Symbol in der Statistik "Tortendiagramm" */
	STATISTICS_DIAGRAM_PIE("chart_pie.png"),

	/** Symbol in der Statistik "Bild" */
	STATISTICS_DIAGRAM_PICTURE("image.png"),

	/** Symbol in der Statistik "X-Y-Diagramm" */
	STATISTICS_DIAGRAM_XY("chart_xy.png"),

	/** Symbol in der Statistik "Schichtplan" */
	STATISTICS_DIAGRAM_SHIFT_PLAN("time.png"),

	/** Symbol in der Statistik "Sankey-Diagramm" */
	STATISTICS_DIAGRAM_SANKEY("chart_organisation.png"),

	/** Symbol in "Schriftart" */
	STATISTICS_DIAGRAM_FONT_SIZE("font.png"),

	/* Meldungsdialoge */

	/** Symbol "Dialog-Button 'Ja'" */
	MSGBOX_YES("tick.png"),

	/** Symbol "Dialog-Button 'Ja, speichern'" */
	MSGBOX_YES_SAVE("disk.png"),

	/** Symbol "Dialog-Button 'Nein'" */
	MSGBOX_NO("cancel.png"),

	/** Symbol "Dialog-Button 'Abbruch/Zurück'" */
	MSGBOX_CANCEL("arrow_redo2.png"),

	/** Symbol "Dialog-Button 'Kopieren'" */
	MSGBOX_COPY("page_copy.png");

	/**
	 * Dateiname des Icons
	 */
	private final String name;

	/**
	 * URLs des Icons
	 */
	private URL[] urls;

	/**
	 * Bild
	 */
	private Image image;

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
	 * Sucht ein Bild in einem Ordner und fügt es, wenn gefunden, zu einer Liste hinzu.
	 * @param list	Liste mit URLs zu der die neue URL hinzugefügt werden soll
	 * @param folder	Ordner in dem das Bild gesucht werden soll
	 * @param name	Name des Bildes
	 */
	private void addURL(final List<URL> list, final String folder, final String name) {
		URL url;

		url=getClass().getResource(folder+"/"+name);
		if (url!=null) {
			list.add(url);
		} else {
			url=getClass().getResource(folder+"/"+name.replace('_','-'));
			if (url!=null) list.add(url);
		}
	}

	/**
	 * Liefert die URL des Icons
	 * @return	URL des Icons
	 */
	private URL[] getURLs() {
		if (urls==null) {
			List<URL> list=new ArrayList<>();
			addURL(list,"res",name);
			addURL(list,"res24",name);
			addURL(list,"res32",name);
			addURL(list,"res48",name);
			urls=list.toArray(new URL[0]);
		}
		assert(urls!=null);
		return urls;
	}

	/**
	 * Wird das Programm unter Java 9 oder höher ausgeführt, so wird
	 * der Konstruktor der Multi-Resolution-Bild-Objektes geliefert, sonst <code>null</code>.
	 * @return	Multi-Resolution-Bild-Konstruktor oder <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	private static Constructor<Object> getMultiImageConstructor() {
		try {
			final Class<?> cls=Class.forName("java.awt.image.BaseMultiResolutionImage");
			return (Constructor<Object>)cls.getDeclaredConstructor(int.class,Image[].class);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	/**
	 * Liefert das Icon.
	 * @return	Icon
	 */
	public Icon getIcon() {
		if (icon==null) {
			final Image image=getImage();
			if (image!=null) icon=new ImageIcon(image);
		}
		assert(icon!=null);
		return icon;
	}

	/**
	 * Liefert basierend auf einer oder mehreren URLs das Standardbild (das Bild für die erste URL)
	 * @param urls	Liste mit URLs
	 * @return	Bild für die erste URL
	 */
	private Image getDefaultImage(final URL[] urls) {
		if (urls==null || urls.length==0) return null;
		try {
			return ImageIO.read(urls[0]);
		} catch (IOException e) {
			assert(false);
			return null;
		}
	}

	/**
	 * Liefert das Bild.
	 * @return	Bild
	 */
	public Image getImage() {
		if (image!=null) return image;

		final URL[] urls=getURLs();
		assert(urls.length>0);

		if (urls.length==1) return image=getDefaultImage(urls);

		final Constructor<Object> multiConstructor=getMultiImageConstructor();
		if (multiConstructor==null) return image=getDefaultImage(urls);

		final Image[] images=Arrays.asList(urls).stream().map(url->{
			try {
				return ImageIO.read(url);
			} catch (IOException e) {
				return image=getDefaultImage(urls);
			}
		}).toArray(Image[]::new);

		try {
			image=(Image)multiConstructor.newInstance(0,images);
			assert(image!=null);
			return image;
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
			return image=getDefaultImage(urls);
		}
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
