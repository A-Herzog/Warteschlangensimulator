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
package ui.modeleditor.elements;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Basisklasse für Text-Renderer für {@link ModelElementText}
 * @author Alexander Herzog
 * @see ModelElementText
 */
public abstract class ModelElementTextRenderer {
	/**
	 * Auszugebender Text beim letzten Aufruf von {@link #setText(String, boolean)}
	 * @see #setText(String, boolean)
	 * @see TextTransformer
	 */
	private String lastText="";

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 * @see #setText(String, boolean)
	 */
	private boolean lastInterpretSymbols;

	/**
	 * Ermöglicht die Interpretation von HTML-Entities und LaTeX-Symbolen
	 * @see #setText(String, boolean)
	 * @see TextTransformer
	 */
	private TextTransformer textTransformer;

	/**
	 * Ist eine Neuberechnung der Größe notwendig?
	 * @see #setNeedRecalc()
	 * @see #calcIntern(Graphics, double)
	 */
	private boolean needRecalc=true;

	/**
	 * Zoom-Level beim letzten Aufruf von {@link #calc(Graphics, double)}
	 * @see #calc(Graphics, double)
	 */
	private double lastZoom=-1;

	/**
	 * Berechnete Breite des Textbereichs
	 * @see #calcIntern(Graphics, double)
	 * @see #getWidth()
	 */
	protected int width;

	/**
	 * Berechnete Höhe des Textbereichs
	 * @see #calcIntern(Graphics, double)
	 * @see #getHeight()
	 */
	protected int height;

	/**
	 * Füllfarbe beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastFillAlpha
	 * @see #lastComputedFillColor
	 */
	private Color lastFillColor=null;

	/**
	 * Deckkraft beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastFillColor
	 * @see #lastComputedFillColor
	 */
	private double lastFillAlpha=0.0;

	/**
	 * Berechnete Füllfarbe beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastFillColor
	 * @see #lastFillAlpha
	 */
	private Color lastComputedFillColor=null;

	/**
	 * Stellt den auszugebenden Text ein.
	 * @param text	Auszugebender Text
	 * @param processSymbols	Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 */
	public final void setText(String text, final boolean processSymbols) {
		if (text==null) text="";
		if (text.equals(lastText) && lastInterpretSymbols==processSymbols) return;

		lastText=text;
		lastInterpretSymbols=processSymbols;
		final String[] lines=lastText.trim().split("\\n");
		if (processSymbols) {
			if (textTransformer==null) textTransformer=new TextTransformer();
			processLines(textTransformer.process(lines));
		} else {
			processLines(lines);
		}
		needRecalc=true;
	}

	/**
	 * Stellt die Schriftart für den Text ein.
	 * @param fontSize	Schriftgröße
	 * @param bold	Text fett ausgeben?
	 * @param italic	Text kursiv ausgeben?
	 * @param fontFamily	Schriftart
	 * @param textAlign	Ausrichtung der Zeilen
	 */
	public abstract void setStyle(final int fontSize, final boolean bold, final boolean italic, final String fontFamily, final ModelElementText.TextAlign textAlign);

	/**
	 * Stellt die Hintergrundfarbe für das Textfeld ein.
	 * @param fillColor	Hintergrundfarbe (kann <code>null</code> sein)
	 * @param fillAlpha	Deckkraft (Wert zwischen 0 und 1)
	 */
	public final void setBackgroundColor(final Color fillColor, final double fillAlpha) {
		if (fillColor==lastFillColor && fillAlpha==lastFillAlpha) return;
		if (fillColor==null) {
			lastComputedFillColor=null;
		} else {
			lastComputedFillColor=new Color(fillColor.getRed(),fillColor.getGreen(),fillColor.getBlue(),Math.max(0,Math.min(255,((int)Math.round(255*fillAlpha)))));
		}
		lastFillColor=fillColor;
		lastFillAlpha=fillAlpha;
	}

	/**
	 * Benachrichtigt das Objekt, dass beim Aufruf von {@link #calc(Graphics, double)}
	 * eine Neuberechnung der Daten durchgeführt werden muss.
	 * @see #calc(Graphics, double)
	 * @see #calcIntern(Graphics, double)
	 */
	protected final void setNeedRecalc() {
		needRecalc=true;
	}

	/**
	 * Führt eine Vorverarbeitung der übergebenen Textzeilen durch.<br>
	 * (HTML- und LaTeX-Symbole wurden bereits in Unicode-Zeichen übersetzt und der Text in Zeilen getrennt.)
	 * @param lines	Auszugebende Zeilen
	 */
	protected abstract void processLines(final String[] lines);

	/**
	 * Berechnet die Ausgabedaten neu.<br>
	 * Es wird dabei geprüft, ob eine Neuberechnung tatsächlich nötig ist.
	 * Nur dann wird diese tatsächlich ausgeführt.
	 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
	 * @param zoom	Zoom-Level
	 * @see #calcIntern(Graphics, double)
	 */
	public final void calc(final Graphics graphics, final double zoom) {
		if (!needRecalc && lastZoom==zoom) return;
		calcIntern(graphics,zoom);
		needRecalc=false;
		lastZoom=zoom;
	}

	/**
	 * Berechnet die Ausgabedaten neu.
	 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
	 * @param zoom	Zoom-Level
	 */
	protected abstract void calcIntern(final Graphics graphics, final double zoom);

	/**
	 * Liefert die Pixel-Breite für das Textfeld.
	 * @return	Breite für das Textfeld
	 * @see #calc(Graphics, double)
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * Liefert die Pixel-Höhe für das Textfeld.
	 * @return	Höhe für das Textfeld
	 * @see #calc(Graphics, double)
	 */
	public final int getHeight() {
		return height;
	}

	/**
	 * Gibt den Text aus.
	 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
	 * @param x	Linke Kante des Ausgabebereichs
	 * @param y	Obere Kante des Ausgabebereichs
	 * @param color	Textfarbe
	 */
	public void draw(final Graphics graphics, final int x, final int y, final Color color) {
		if (lastComputedFillColor!=null) {
			graphics.setColor(lastComputedFillColor);
			graphics.fillRect(x,y,width,height);
		}

		graphics.setColor(color);
		drawIntern(graphics,x,y);
	}

	/**
	 * Gibt den Text aus (ohne Hintergrund)
	 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
	 * @param x	Linke Kante des Ausgabebereichs
	 * @param y	Obere Kante des Ausgabebereichs
	 */
	protected abstract void drawIntern(final Graphics graphics, int x, int y);
}
