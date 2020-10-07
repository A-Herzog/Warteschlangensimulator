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
 * Diese Klasse stellt ein paar statische Funktionen zur Initialisierung der grafischen Oberfl�che zur Verf�gung.
 * @author Alexander Herzog
 * @version 1.3
 */
public class GUITools {
	/**
	 * Fehlermeldung, die auf der Konsole ausgegeben wird, wenn die grafische Oberfl�che
	 * gestartet werden soll, aber keine Grafikausgabe m�glich ist (weil der Nutzer �ber
	 * eine Telnet/SSH-Verbindung auf einer Konsole arbeitet).
	 */
	public static String errorNoGraphicsOutputAvailable="Da keine grafische Oberfl�che zur Verf�gung steht, kann der Simulator nur im Konsolen-Modus betrieben werden. Rufen Sie den Simulator mit dem Parameter \"Hilfe\" auf, um eine �bersicht �ber die verf�gbaren Konsolen-Befehle zu erhalten.";

	/**
	 * Eingestellter Skalierungsfaktor
	 * @see #getScaleFactor()
	 */
	private static double scaleFactor=1.0;

	/**
	 * Liefert den �ber {@link #setupFontSize(double)} eingestellten Skalierungsfaktor
	 * @return	Skalierungsfaktor
	 */
	public static double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt lediglich statische Hilfroutinen zur Verf�gung.
	 */
	private GUITools() {}

	/**
	 * W�hlt als Look&amp;Feel-Manager das Betriebssystem-typische Format aus.
	 * @return	Gibt <code>true</code> zur�ck, wenn das System Look&amp;Feel aktiviert werden konnte
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
	 * W�hlt einen bestimmten Look&amp;Feel-Manager aus.
	 * Steht das Look&amp;Feel nicht zur Verf�gung, so wird als Fallback das Betriebssystem-typische Format gew�hlt.
	 * @param name	Name des Look&amp;Feel
	 * @return	Gibt <code>true</code> zur�ck, wenn das gew�nschte oder das Fallback Look&amp;Feel aktiviert werden konnte
	 * @see #listLookAndFeels()
	 */
	public static boolean setupUI(final String name) {
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
	 * Listet die Namen der verf�gbaren Look&amp;Feels aus.
	 * @return	Namen der verf�gbaren Look&amp;Feels
	 * @see #setupUI(String)
	 */
	public static String[] listLookAndFeels() {
		return Arrays.asList(UIManager.getInstalledLookAndFeels()).stream().map(info->info.getName()).toArray(String[]::new);
	}

	/**
	 * Pr�ft, ob eine Ausgabe grafischer Fenster m�glich ist oder ob das Programm in einer Text-Modus-only Konsole l�uft
	 * @return	Gibt <code>true</code> zur�ck, wenn grafische Fenster ausgegeben werden k�nnen
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
	 * Skaliert alle Schriftarten.<br>
	 * Muss vor dem �ffnen des ersten Fensters aufgerufen werden.
	 * @param scaleFactor	Skalierungsfaktor (1.0=Standardgr��e)
	 */
	public static void setupFontSize(double scaleFactor) {
		GUITools.scaleFactor=scaleFactor;
		if (scaleFactor==1) return;
		UIDefaults defaults=UIManager.getDefaults();
		Enumeration<Object> e=defaults.keys();
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
}
