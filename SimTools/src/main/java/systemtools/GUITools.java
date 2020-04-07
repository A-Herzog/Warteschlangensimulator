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
import java.util.Enumeration;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

/**
 * Diese Klasse stellt ein paar statische Funktionen zur Initialisierung der grafischen Oberfl�che zur Verf�gung.
 * @author Alexander Herzog
 * @version 1.2
 */
public class GUITools {
	/**
	 * Fehlermeldung, die auf der Konsole ausgegeben wird, wenn die grafische Oberfl�che
	 * gestartet werden soll, aber keine Grafikausgabe m�glich ist (weil der Nutzer �ber
	 * eine Telnet/SSH-Verbindung auf einer Konsole arbeitet).
	 */
	public static String errorNoGraphicsOutputAvailable="Da keine grafische Oberfl�che zur Verf�gung steht, kann der Simulator nur im Konsolen-Modus betrieben werden. Rufen Sie den Simulator mit dem Parameter \"Hilfe\" auf, um eine �bersicht �ber die verf�gbaren Konsolen-Befehle zu erhalten.";

	private GUITools() {}

	/**
	 * W�hlt als Look&amp;Feel-Manager das Betriebssystem-typische Format aus
	 * @return	Gibt <code>true</code> zur�ck, wenn das System Look&amp;Feel aktiviert werden konnte
	 */
	public static boolean setupUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {return false;}
		return true;
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
		if (scaleFactor==1) return;
		UIDefaults defaults=UIManager.getDefaults();
		Enumeration<Object> e=defaults.keys();
		while (e.hasMoreElements()) {
			Object key=e.nextElement();
			Object value=defaults.get(key);
			if (value instanceof Font) {
				Font font=(Font)value;
				int newSize=(int)Math.round(font.getSize()*scaleFactor);
				if (value instanceof FontUIResource) {
					defaults.put(key,new FontUIResource(font.getName(),font.getStyle(),newSize));
				} else {
					defaults.put(key,new Font(font.getName(),font.getStyle(),newSize));
				}
			}
		}
	}
}
