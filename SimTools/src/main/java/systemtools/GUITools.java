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
package systemtools;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

/**
 * Diese Klasse stellt ein paar statische Funktionen zur Initialisierung der grafischen Oberfläche zur Verfügung.
 * @author Alexander Herzog
 * @version 1.4
 */
public class GUITools {
	/**
	 * Fehlermeldung, die auf der Konsole ausgegeben wird, wenn die grafische Oberfläche
	 * gestartet werden soll, aber keine Grafikausgabe möglich ist (weil der Nutzer über
	 * eine Telnet/SSH-Verbindung auf einer Konsole arbeitet).
	 */
	public static String errorNoGraphicsOutputAvailable="Da keine grafische Oberfläche zur Verfügung steht, kann der Simulator nur im Konsolen-Modus betrieben werden. Rufen Sie den Simulator mit dem Parameter \"Hilfe\" auf, um eine Übersicht über die verfügbaren Konsolen-Befehle zu erhalten.";

	/**
	 * Eingestellter Skalierungsfaktor
	 * @see #getScaleFactor()
	 */
	private static double scaleFactor=1.0;

	/**
	 * Liefert den über {@link #setupFontSize(double)} eingestellten Skalierungsfaktor
	 * @return	Skalierungsfaktor
	 */
	public static double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * Skalierungsfaktor der Programmoberfläche laut Betriebssystemangabe.<br>
	 * (-1 bedeutet, dass der Faktor noch nicht ausgelesen wurde.)
	 * @see #getOSScaleFactor()
	 */
	private static double osScaleFactor=-1;

	/**
	 * Ermittelt den Skalierungsfaktor der Programmoberfläche laut Betriebssystemangabe.
	 * @return	Skalierungsfaktor der Programmoberfläche laut Betriebssystemangabe
	 */
	public static double getOSScaleFactor() {
		if (osScaleFactor<0) {
			if (GraphicsEnvironment.isHeadless()) {
				osScaleFactor=1.0;
			} else {
				osScaleFactor=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
			}
		}

		return osScaleFactor;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt lediglich statische Hilfroutinen zur Verfügung.
	 */
	private GUITools() {}

	/**
	 * Wählt als Look&amp;Feel-Manager das Betriebssystem-typische Format aus.
	 * @return	Gibt <code>true</code> zurück, wenn das System Look&amp;Feel aktiviert werden konnte
	 */
	public static boolean setupUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			return false;
		}
		return true;
	}

	/**
	 * Wählt einen bestimmten Look&amp;Feel-Manager aus.
	 * Steht das Look&amp;Feel nicht zur Verfügung, so wird als Fallback das Betriebssystem-typische Format gewählt.
	 * @param name	Name des Look&amp;Feel
	 * @return	Gibt <code>true</code> zurück, wenn das gewünschte oder das Fallback Look&amp;Feel aktiviert werden konnte
	 * @see #listLookAndFeels()
	 */
	public static boolean setupUI(final String name) {
		currentLookAndFeel=name;

		String className=null;
		for (LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) if (info.getName().equalsIgnoreCase(name)) {
			className=info.getClassName();
			break;
		}

		if (className==null) return setupUI();

		try {
			UIManager.setLookAndFeel(className);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			return setupUI();
		}
		return true;
	}

	/**
	 * Aktuelles Look &amp; Feel (kann <code>null</code> sein)
	 */
	private static String currentLookAndFeel;

	/**
	 * Liefert das aktuell eingestellte Look &amp; Feel
	 * @return	Aktuelles Look &amp; Feel (kann <code>null</code> sein)
	 */
	public static String getCurrentLookAndFeel() {
		return currentLookAndFeel;
	}

	/**
	 * Listet die Namen der verfügbaren Look&amp;Feels aus.
	 * @return	Namen der verfügbaren Look&amp;Feels
	 * @see #setupUI(String)
	 */
	public static String[] listLookAndFeels() {
		return Arrays.asList(UIManager.getInstalledLookAndFeels()).stream().map(info->info.getName()).toArray(String[]::new);
	}

	/**
	 * Prüft, ob eine Ausgabe grafischer Fenster möglich ist oder ob das Programm in einer Text-Modus-only Konsole läuft
	 * @return	Gibt <code>true</code> zurück, wenn grafische Fenster ausgegeben werden können
	 */
	public static boolean isGraphicsAvailable() {
		boolean ok=!GraphicsEnvironment.isHeadless();
		if (!ok) {
			String info=errorNoGraphicsOutputAvailable;
			while (!info.isEmpty()) {
				String next="";
				while (info.length()>=80) {int i=info.lastIndexOf(' '); next=info.substring(i)+next; info=info.substring(0,i);}
				System.out.println(info); info=next.trim();
			}
		}
		return ok;
	}

	/**
	 * Wird die Windows-seitig die Bildschirmskalierug geändert, so stimmen zwar die
	 * Menüschriftarten, aber die Dialog- und Toolbar-Schrift hat eine falsche Größe.
	 * Für diesem Fall wird die Schriftgröße für Dialogelemente und Toolbar von der
	 * Menüschriftgröße abgeleitet.
	 */
	public static void setupFontSizeFixSystemScaling() {
		/* Korrektur nur unter den Betriebssystemen anwenden, bei denen das Scaling-Problem bekannt ist und nachvollzogen werden kann */
		final String osName=System.getProperty("os.name");
		if (osName==null || !osName.startsWith("Windows")) return;

		final UIDefaults defaults=UIManager.getDefaults();
		if (defaults==null) return;

		final Font menuItemFont=defaults.getFont("MenuItem.font");
		if (menuItemFont==null) return;
		if (!menuItemFont.getFamily().equals("Segoe UI")) return; /* Nichts verstellen, wenn wir nicht die Schriftarten vorfinden, die wir erwarten. */
		final int menuItemFontSize=menuItemFont.getSize();

		final Enumeration<Object> e=defaults.keys();
		while (e.hasMoreElements()) {
			final Object key=e.nextElement();
			final Font font=defaults.getFont(key);
			if (font==null) continue;
			if (!font.getFamily().equals("Tahoma")) continue; /* Nichts verstellen, wenn wir nicht die Schriftarten vorfinden, die wir erwarten. */
			if (font.getSize()==menuItemFontSize-1) continue;
			defaults.put(key,new Font(font.getName(),font.getStyle(),menuItemFontSize-1));
		}
	}

	/**
	 * Skaliert alle Schriftarten.<br>
	 * Muss vor dem Öffnen des ersten Fensters aufgerufen werden.
	 * @param scaleFactor	Skalierungsfaktor (1.0=Standardgröße)
	 */
	public static void setupFontSize(double scaleFactor) {
		GUITools.scaleFactor=scaleFactor;
		if (scaleFactor==1) return;

		final UIDefaults defaults=UIManager.getDefaults();
		if (defaults==null) return;

		final Enumeration<Object> e=defaults.keys();
		while (e.hasMoreElements()) {
			final Object key=e.nextElement();
			final Object value=defaults.get(key);
			if (value instanceof Font) {
				final Font font=(Font)value;
				final int newSize=(int)Math.round(font.getSize()*scaleFactor);
				if (value instanceof FontUIResource) {
					defaults.put(key,new FontUIResource(font.getName(),font.getStyle(),newSize));
				} else {
					defaults.put(key,new Font(font.getName(),font.getStyle(),newSize));
				}
			}
		}
	}

	/**
	 * Stellt programmweit eine andere Schriftart ein.
	 * @param fontName	Name der Schriftart
	 */
	public static void setFontName(final String fontName) {
		if (fontName==null || fontName.isBlank()) return;

		for (Object key: UIManager.getDefaults().keySet()) {
			if (!(key instanceof String)) continue;
			final String keyString=(String)key;
			if (!keyString.toLowerCase().contains("font")) continue;
			final Font oldFont=UIManager.getFont(key);
			if (oldFont==null) continue;
			final Font newFont=new Font(fontName,oldFont.getStyle(),oldFont.getSize());
			UIManager.put(key,newFont);
		}
	}
}