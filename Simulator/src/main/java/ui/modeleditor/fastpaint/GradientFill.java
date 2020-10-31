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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import tools.SetupData;

/**
 * Stellt die Füllfarbe eines {@link Graphics}-Objektes auf
 * einen fixen Wert oder einen Farbverlauf (Setup-abhängig)
 * @author Alexander Herzog
 */
public class GradientFill {
	/** Letzter Zeichenbereich */
	private Rectangle lastPaintRect=new Rectangle();
	/** Letzte Farbe 1 */
	private Color lastPaintColor;
	/** Letzte Farbe 2 */
	private Color lastPaintColor2;
	/** Letzte Zeichenrichtung */
	private boolean lastPaintDirection;
	/** Farbverlauf beim letzten Aufruf von {@link #build(Rectangle, Color, Color, boolean)} */
	private GradientPaint lastPaint;

	/** Referenz auf das Setup-Singleton */
	private final SetupData setup;
	/** Referenz auf das Objekt zur Erstellung von helleren Farben */
	private final BrighterColor brighter;

	/**
	 * Konstruktor der Klasse
	 */
	public GradientFill() {
		setup=SetupData.getSetup();
		brighter=new BrighterColor();
	}

	/**
	 * Stellt die Hintergrundfarbe zum Füllen einer Form ein.
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param fillColor	Hintergrundfarbe (wird Setup-abhängig direkt oder als Farbverlauf von links nach rechts bzw. von oben nach unten verwendet)
	 * @param fillColorBrighter	Hellere Fassung der Hintergrundfarbe
	 * @param verticalGradient	Gibt an, ob der optionale Farbverlauf horizontal (<code>false</code>) oder vertikal (<code>true</code>) gezeichnet werden soll
	 * @return	Farbverlauf
	 */
	private static GradientPaint build(final Rectangle objectRect, final Color fillColor, final Color fillColorBrighter, final boolean verticalGradient) {
		final Point2D p1, p2;

		if (verticalGradient) {
			/* Vertikaler Verlauf von oben nach unten */
			final int w2=objectRect.x+objectRect.width/2;
			if (objectRect.height>0) {
				p1=new Point2D.Double(w2,objectRect.y);
				p2=new Point2D.Double(w2,objectRect.y+objectRect.height);
			} else {
				p1=new Point2D.Double(w2,objectRect.y+objectRect.height);
				p2=new Point2D.Double(w2,objectRect.y);
			}
		} else {
			/* Horizontaler Verlauf von links nach rechts */
			final int h2=objectRect.y+objectRect.height/2;
			if (objectRect.width>0) {
				p1=new Point2D.Double(objectRect.x,h2);
				p2=new Point2D.Double(objectRect.x+objectRect.width,h2);
			} else {
				p1=new Point2D.Double(objectRect.x+objectRect.width,h2);
				p2=new Point2D.Double(objectRect.x,h2);
			}
		}

		return new GradientPaint(p1,fillColor,p2,fillColorBrighter);
	}

	/**
	 * Stellt die Hintergrundfarbe zum Füllen einer Form ein.
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param fillColor	Hintergrundfarbe (wird Setup-abhängig direkt oder als Farbverlauf von links nach rechts bzw. von oben nach unten verwendet)
	 * @param verticalGradient	Gibt an, ob der optionale Farbverlauf horizontal (<code>false</code>) oder vertikal (<code>true</code>) gezeichnet werden soll
	 */
	public synchronized void set(final Graphics graphics, final Rectangle objectRect, final Color fillColor, final boolean verticalGradient) {
		if (!setup.useGradients) {
			graphics.setColor(fillColor);
			return;
		}

		if (lastPaint==null || !lastPaintRect.equals(objectRect) || lastPaintColor==null || !lastPaintColor.equals(fillColor) || lastPaintDirection!=verticalGradient) {
			lastPaint=build(objectRect,fillColor,brighter.get(fillColor),verticalGradient);
			FastPaintTools.copyRectangleData(objectRect,lastPaintRect);
			lastPaintColor=fillColor;
			lastPaintDirection=verticalGradient;
		}

		((Graphics2D)graphics).setPaint(lastPaint);
	}

	/**
	 * Stellt die Hintergrundfarbe zum Füllen einer Form ein.
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param fillColor1	Hintergrundfarbe 1 (wird Setup-abhängig direkt oder als Farbverlauf von links nach rechts bzw. von oben nach unten verwendet)
	 * @param fillColor2	Hintergrundfarbe 2 (wird Setup-abhängig als Farbverlauf von links nach rechts bzw. von oben nach unten verwendet)
	 * @param verticalGradient	Gibt an, ob der optionale Farbverlauf horizontal (<code>false</code>) oder vertikal (<code>true</code>) gezeichnet werden soll
	 */
	public synchronized void set(final Graphics graphics, final Rectangle objectRect, final Color fillColor1, final Color fillColor2, final boolean verticalGradient) {
		if (!setup.useGradients) {
			graphics.setColor(fillColor1);
			return;
		}

		if (lastPaint==null || !lastPaintRect.equals(objectRect) || lastPaintColor==null || lastPaintColor2==null || !lastPaintColor.equals(fillColor1) || !lastPaintColor2.equals(fillColor2) || lastPaintDirection!=verticalGradient) {
			lastPaint=build(objectRect,fillColor1,fillColor2,verticalGradient);
			FastPaintTools.copyRectangleData(objectRect,lastPaintRect);
			lastPaintColor=fillColor1;
			lastPaintColor2=fillColor2;
			lastPaintDirection=verticalGradient;
		}

		((Graphics2D)graphics).setPaint(lastPaint);
	}
}
