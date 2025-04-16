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
package systemtools.commandline;

import java.awt.Color;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

/**
 * Stellt verschiedene ANSI-Formatierungen für die
 * Ausgabe über <code>System.out</code> ein.
 * @author Alexander Herzog
 * @see <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797</a>
 */
public class ANSIFormat {
	/**
	 * Sind die Voraussetzungen für die Verwendung von Formatierungen erfolgt?<br>
	 * (Ausgabe erfolgt über <code>System.out</code> und <code>System.out</code> wurde
	 * nicht in eine Datei umgeleitet.)
	 */
	public final boolean active;

	/**
	 * Zuordnung von Farben von ANSI-Farbcodes für Vorder- und Hintergrundfarbe
	 */
	private static Map<Color,int[]> colors;

	static {
		colors=new HashedMap<>();
		colors.put(Color.BLACK,new int[] {30,40});
		colors.put(Color.RED,new int[] {31,41});
		colors.put(Color.GREEN,new int[] {32,42});
		colors.put(Color.YELLOW,new int[] {33,43});
		colors.put(Color.BLUE,new int[] {34,44});
		colors.put(Color.MAGENTA,new int[] {35,45});
		colors.put(Color.CYAN,new int[] {36,46});
		colors.put(Color.WHITE,new int[] {37,47});
		colors.put(null,new int[] {39,49});
	}

	/**
	 * Konstruktor der Klasse
	 * @param out	{@link PrintStream}-Objekt über das die Textausgabe erfolgt (nur wenn dieses mit <code>System.out</code> übereinstimmt, aktiviert sich das Formatierungssystem)
	 */
	public ANSIFormat(final PrintStream out) {
		active=(System.console()!=null) && (System.out==out);
	}

	/**
	 * Sendet einen einzelnen ANSI-[m-Code ab
	 * @param command	ANSI-[m-Code
	 */
	protected final void send(final int command) {
		if (!active) return;
		System.out.print("\033["+command+"m");
		System.out.flush();
	}

	/**
	 * Sendet einen oder mehrere ANSI-[m-Code in einem Befehl ab
	 * @param commands	ANSI-[m-Codes
	 */
	protected final void send(final int[] commands) {
		if (!active) return;
		if (commands==null || commands.length==0) return;
		final String command=String.join(";",Arrays.stream(commands).mapToObj(cmd->""+cmd).toArray(String[]::new));
		System.out.print("\033["+command+"m");
		System.out.flush();
	}

	/**
	 * Verschiebt den Cursor um die angegebene Anzahl an Zeilen nach oben.
	 * @param lineCount	Anzahl an Zeilen
	 * @see #moveCursorDown(int)
	 */
	public final void moveCursorUp(final int lineCount) {
		if (!active) return;
		System.out.print("\033["+lineCount+"A");
		System.out.flush();
	}

	/**
	 * Verschiebt den Cursor um die angegebene Anzahl an Zeilen nach unten.
	 * @param lineCount	Anzahl an Zeilen
	 * @see #moveCursorUp(int)
	 */
	public final void moveCursorDown(final int lineCount) {
		if (!active) return;
		System.out.print("\033["+lineCount+"B");
		System.out.flush();
	}

	/**
	 * Löscht auf dem Bildschirm alles nach der aktuellen Cursorposition.
	 */
	public final void eraseAfterCursor() {
		if (!active) return;
		System.out.print("\033[0J");
		System.out.flush();
	}

	/**
	 * Setzt alle Formateinstellungen zurück.<br>
	 * ("ESC[0m")
	 */
	public void reset() {
		send(0);
	}

	/**
	 * Schaltet die Fett-Darstellung ein oder aus.
	 * @param bold	Text fett darstellen?
	 */
	public void setBold(final boolean bold) {
		send(bold?1:22);
	}

	/**
	 * Schaltet die Kursiv-Darstellung ein oder aus.
	 * @param italic Text kursiv darstellen?
	 */
	public void setItalic(final boolean italic) {
		send(italic?3:23);
	}

	/**
	 * Schaltet die Unterstrichen-Darstellung ein oder aus.
	 * @param underline	Text unterstrichen darstellen?
	 */
	public void setUnderline(final boolean underline) {
		send(underline?4:24);
	}

	/**
	 * Schaltet die Blinkend-Darstellung ein oder aus.
	 * @param blinking	Text blinkend darstellen?
	 */
	public void setBlinking(final boolean blinking) {
		send(blinking?5:25);
	}

	/**
	 * Schaltet die Invers-Darstellung ein oder aus.
	 * @param inverse	Text invers darstellen?
	 */
	public void setInverse(final boolean inverse) {
		send(inverse?7:27);
	}

	/**
	 * Schaltet die Durchgestrichen-Darstellung ein oder aus.
	 * @param strikethrough	Text durchgestrichen darstellen?
	 */
	public void setStrikethrough(final boolean strikethrough) {
		send(strikethrough?9:2);
	}

	/**
	 * Gibt an, ob eine bestimmte Farbe für die Konsole zur Verfügung steht.
	 * @param color	Zu prüfende Farbe
	 * @return	Liefert <code>true</code>, wenn die Farbe verwendet werden kann
	 * @see #setColor(Color)
	 * @see #setBackgroundColor(Color)
	 */
	public static boolean isColorSupported(final Color color) {
		return colors.containsKey(color);
	}

	/**
	 * Stellt eine Schriftfarbe ein.
	 * @param color	Zu verwendende Farbe oder <code>null</code> für Standardfarbe
	 * @see #isColorSupported(Color)
	 * @see #setBackgroundColor(Color)
	 */
	public void setColor(final Color color) {
		send(colors.get(isColorSupported(color)?color:null)[0]);
	}

	/**
	 * Stellt eine Hintergrundfarbe ein.
	 * @param color	Zu verwendende Farbe oder <code>null</code> für Standardfarbe
	 * @see #isColorSupported(Color)
	 * @see #setColor(Color)
	 */
	public void setBackgroundColor(final Color color) {
		send(colors.get(isColorSupported(color)?color:null)[1]);
	}

	/**
	 * Stellt das Format für Fehlermeldungen ein.
	 */
	public void setErrorStyle() {
		setColor(Color.RED);
		setBold(true);
	}

	/**
	 * Stellt das Format für normalen Text ein.
	 */
	public void setNormalStyle() {
		setBold(false);
		setColor(null);
	}
}