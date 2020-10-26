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
package ui.modeleditor.fastpaint;

import java.awt.Color;

import org.apache.commons.math3.util.FastMath;

/**
 * Bestimmt zu einer Farbe eine hellere Farbe und
 * cacht diese falls die nächste Anfrage sich auf
 * dieselbe Farbe bezieht.
 * @author Alexander Herzog
 */
public class BrighterColor {
	/**
	 * Maximalwert um den die drei Farbkanäle jeweils verändert werden
	 */
	private static final int BRIGHTER_STEP=128;

	/**
	 * Zuletzt umgewandelte Farbe
	 */
	private Color lastColor;

	/**
	 * Ergebnis der letzten Umwandlung
	 * (um dieses ggf. wieder wiederverwenden zu können)
	 */
	private Color lastBrighterColor;

	/**
	 * Konstruktor der Klasse
	 */
	public BrighterColor() {
	}

	/**
	 * Berechnet zu einer Farbe eine hellere Farbe
	 * @param color	Ausgangsfarbe
	 * @return	Hellere Variante der Farbe
	 * @see #get(Color)
	 */
	private static Color build(final Color color) {
		final Color brighter=color.brighter();
		if (!brighter.equals(color)) return brighter;
		return new Color(FastMath.min(color.getRed()+BRIGHTER_STEP,255),FastMath.min(color.getGreen()+BRIGHTER_STEP,255),FastMath.min(color.getBlue()+BRIGHTER_STEP,255));
	}

	/**
	 * Bestimmt eine hellere Farbe zu einer gegebenen Farbe
	 * @param color	Farbe, zu der eine hellere Farbe bestimmt werden soll
	 * @return	Hellere Variante der gegebenen Farbe
	 */
	public synchronized Color get(final Color color) {
		if (lastColor==null || !lastColor.equals(color)) {
			lastBrighterColor=build(color);
			lastColor=color;
		}

		return lastBrighterColor;
	}
}
