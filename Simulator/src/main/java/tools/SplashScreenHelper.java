/**
 * Copyright 2025 Alexander Herzog
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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.SplashScreen;

/**
 * Diese Klasse stellt statische Hilfsroutinen zum Umgang mit dem
 * Splashscreen-Fenster zur Verfügung.
 * @see SplashScreen
 */
public class SplashScreenHelper {
	/**
	 * Steht eine Grafikausgabe zur Verfügung?
	 */
	private static boolean isGraphical=!GraphicsEnvironment.isHeadless();

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
	 */
	private SplashScreenHelper() {
	}

	/**
	 * Schließt den Splashscreen (sofern einer angezeigt wurde).<br>
	 * Existiert kein Splashscreen-Fenster oder wurde dieses bereits geschlossen, so passiert nichts.
	 * @return	Liefert <code>true</code>, wenn das Splashscreen-Fenster geschlossen werden konnte.
	 */
	public static boolean close() {
		if (!isGraphical) return false;
		try {
			final SplashScreen splashScreen=SplashScreen.getSplashScreen();
			if (splashScreen!=null) {
				splashScreen.close();
				return true;
			} else {
				return false;
			}
		} catch (UnsupportedOperationException | IllegalStateException e) {
			return false;
		}
	}

	/**
	 * Aktualisiert den Fortschrittsbalken auf dem Splashscreen-Fenster.<br>
	 * Existiert kein Splashscreen-Fenster oder wurde dieses bereits geschlossen, so passiert nichts.
	 * @param fraction	Fortschritt (Wert zwischen 0 und 1)
	 * @return	Liefert <code>true</code>, wenn der Splashscreen erfolgreich aktualisiert werden konnte
	 */
	public static boolean setProgress(final double fraction) {
		return setProgress(fraction,null);
	}

	/**
	 * Aktualisiert den Fortschrittsbalken auf dem Splashscreen-Fenster.<br>
	 * Existiert kein Splashscreen-Fenster oder wurde dieses bereits geschlossen, so passiert nichts.
	 * @param fraction	Fortschritt (Wert zwischen 0 und 1)
	 * @param info	Optionaler Info-Text (kann <code>null</code> oder leer sein)
	 * @return	Liefert <code>true</code>, wenn der Splashscreen erfolgreich aktualisiert werden konnte
	 */
	public static boolean setProgress(final double fraction, final String info) {
		if (!isGraphical) return false;
		try {
			final SplashScreen splashScreen=SplashScreen.getSplashScreen();
			if (splashScreen!=null) {
				final var size=splashScreen.getSize();
				final var graphics=splashScreen.createGraphics();

				graphics.setColor(Color.GRAY);
				graphics.fillRect(0,size.height-20,size.width,size.height);
				graphics.setColor(Color.BLUE);
				graphics.fillRect(0,size.height-20,(int)Math.round(size.width*Math.max(0,Math.min(1,fraction))),size.height);

				if (info!=null && !info.isBlank()) {
					graphics.setColor(Color.WHITE);
					final var metrics=graphics.getFontMetrics();
					final int oldFontHeight=metrics.getAscent()+metrics.getDescent();
					final var oldFontSize=graphics.getFont().getSize();
					graphics.setFont(graphics.getFont().deriveFont(16/oldFontHeight*oldFontSize));
					final var descent=metrics.getDescent();
					graphics.drawString(info,5,size.height-descent-2);
				}

				splashScreen.update();
				return true;
			} else {
				return false;
			}
		} catch (UnsupportedOperationException | IllegalStateException e) {
			return false;
		}
	}
}
