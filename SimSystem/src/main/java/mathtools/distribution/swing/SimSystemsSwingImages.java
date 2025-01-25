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

import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
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

	/** Symbol "Zufallszahlen erzeugen und kopieren" */
	COPY_RANDOM_NUMBERS("123.png"),

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
	HELP("help.png"),

	/** Symbol "Webseite" */
	WEB("world.png"),

	/** Symbol "Einstellungen" */
	SETUP("cog.png"),

	/** Listentrenner für Verteilungsliste */
	LIST_DIVIDER("Divider.png"),

	/** Symbol "f(x)" */
	EXPRESSION("fx.png");

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
	SimSystemsSwingImages(final String name) {
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
			urls=list.toArray(URL[]::new);
		}
		assert(urls!=null);
		return urls;
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

		final Image[] images=Arrays.asList(urls).stream().map(url->{
			try {
				return ImageIO.read(url);
			} catch (IOException e) {
				return image=getDefaultImage(urls);
			}
		}).toArray(Image[]::new);

		return image=new BaseMultiResolutionImage(0,images);
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
			if (output) System.out.println(image.name+": "+(ok?"ok":"missing"));
		}
		return allOk;
	}
}
