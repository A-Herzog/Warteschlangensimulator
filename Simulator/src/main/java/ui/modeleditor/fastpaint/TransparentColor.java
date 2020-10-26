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

/**
 * Bestimmt zu einer Farbe eine teiltransparente Farbe und
 * cacht diese falls die nächste Anfrage sich auf
 * dieselbe Farbe bezieht.
 * @author Alexander Herzog
 */
public class TransparentColor {
	private Color lastColor;
	private double lastAlpha;
	private Color lastProcessedColor;

	/**
	 * Konstruktor der Klasse
	 */
	public TransparentColor() {
	}

	/**
	 * Bestimmt eine teiltransparente Farbe zu einer gegebenen Farbe.
	 * @param color	Farbe, zu der eine teiltransparente Farbe bestimmt werden soll
	 * @param alpha	Deckkraft
	 * @return	Teiltransparente Variante der gegebenen Farbe
	 * @see #get(Color, double)
	 */
	private static Color build(final Color color, final double alpha) {
		if (color==null) return Color.BLACK;
		final int alphaInt=Math.max(0,Math.min(255,(int)Math.round(alpha*255)));
		return new Color(color.getRed(),color.getGreen(),color.getBlue(),alphaInt);
	}

	/**
	 * Bestimmt eine teiltransparente Farbe zu einer gegebenen Farbe.
	 * @param color	Farbe, zu der eine teiltransparente Farbe bestimmt werden soll
	 * @param alpha	Deckkraft
	 * @return	Teiltransparente Variante der gegebenen Farbe
	 */
	public synchronized Color get(final Color color, final double alpha) {
		if (alpha>=1) return color;

		if (lastColor==null || !lastColor.equals(color) || lastAlpha!=alpha) {
			lastProcessedColor=build(color,alpha);
			lastColor=color;
			lastAlpha=alpha;
		}

		return lastProcessedColor;
	}
}