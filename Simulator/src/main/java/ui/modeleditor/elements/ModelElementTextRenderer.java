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

import org.apache.batik.svggen.SVGGraphics2D;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;

/**
 * Basisklasse f�r Text-Renderer f�r {@link ModelElementText}
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
	 * @see #setText(String, boolean, boolean)
	 */
	private boolean lastInterpretSymbols;

	/**
	 * Sollen f�hrende und abschlie�ende Leerzeichen entfernt werden?
	 * @see #setText(String, boolean, boolean)
	 */
	private boolean lastTrim;

	/**
	 * Erm�glicht die Interpretation von HTML-Entities und LaTeX-Symbolen
	 * @see #setText(String, boolean)
	 * @see TextTransformer
	 */
	private TextTransformer textTransformer;

	/**
	 * Ist eine Neuberechnung der Gr��e notwendig?
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
	 * Berechnete H�he des Textbereichs
	 * @see #calcIntern(Graphics, double)
	 * @see #getHeight()
	 */
	protected int height;

	/**
	 * F�llfarbe beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
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
	 * Berechnete F�llfarbe beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastFillColor
	 * @see #lastFillAlpha
	 */
	private Color lastComputedFillColor=null;

	/**
	 * Schattenfarbe beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastShadowAlpha
	 * @see #lastComputedShadowColor
	 */
	private Color lastShadowColor=null;

	/**
	 * Schatten-Deckkraft beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastShadowColor
	 * @see #lastComputedShadowColor
	 */
	private double lastShadowAlpha=0.0;

	/**
	 * Berechnete Shatten-F�llfarbe beim letzten Aufruf von {@link #draw(Graphics, int, int, Color)}
	 * @see #lastShadowColor
	 * @see #lastShadowAlpha
	 */
	private Color lastComputedShadowColor=null;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementTextRenderer() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Stellt den auszugebenden Text ein.
	 * @param text	Auszugebender Text
	 * @param processSymbols	Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 * @param trim	Sollen f�hrende und abschlie�ende Leerzeichen entfernt werden?
	 */
	public final void setText(String text, final boolean processSymbols, final boolean trim) {
		if (text==null) text="";
		if (text.equals(lastText) && lastInterpretSymbols==processSymbols && lastTrim==trim && !needRecalc) return;

		lastText=text;
		lastInterpretSymbols=processSymbols;
		lastTrim=trim;
		final String[] lines=(trim?lastText.trim():lastText).split("\\n");
		if (processSymbols) {
			if (textTransformer==null) textTransformer=new TextTransformer();
			processLines(textTransformer.process(lines));
		} else {
			processLines(lines);
		}
		needRecalc=true;
	}

	/**
	 * Stellt den auszugebenden Text ein.
	 * @param text	Auszugebender Text
	 * @param processSymbols	Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 */
	public final void setText(String text, final boolean processSymbols) {
		setText(text,processSymbols,true);
	}

	/**
	 * Liefert <code>true</code>, wenn der in den Renderer geladene Text leer ist.
	 * @return	Ist der Text im Renderer leer?
	 */
	public final boolean isEmpty() {
		return lastText.isEmpty();
	}

	/**
	 * Stellt die Schriftart f�r den Text ein.
	 * @param fontSize	Schriftgr��e
	 * @param bold	Text fett ausgeben?
	 * @param italic	Text kursiv ausgeben?
	 * @param fontFamily	Schriftart
	 * @param textAlign	Ausrichtung der Zeilen
	 */
	public abstract void setStyle(final int fontSize, final boolean bold, final boolean italic, final String fontFamily, final ModelElementText.TextAlign textAlign);

	/**
	 * Stellt die Hintergrundfarbe f�r das Textfeld ein.
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
	 * Stellt die Schattenfarbe f�r das Textfeld ein.
	 * @param shadowColor	Schattenfarbe (kann <code>null</code> sein)
	 * @param shadowAlpha	Deckkraft (Wert zwischen 0 und 1)
	 */
	public final void setShadowColor(final Color shadowColor, final double shadowAlpha) {
		if (shadowColor==lastShadowColor && shadowAlpha==lastShadowAlpha) return;
		if (shadowColor==null) {
			lastComputedShadowColor=null;
		} else {
			lastComputedShadowColor=new Color(shadowColor.getRed(),shadowColor.getGreen(),shadowColor.getBlue(),Math.max(0,Math.min(255,((int)Math.round(255*shadowAlpha)))));
		}
		lastShadowColor=shadowColor;
		lastShadowAlpha=shadowAlpha;
	}

	/**
	 * Benachrichtigt das Objekt, dass beim Aufruf von {@link #calc(Graphics, double)}
	 * eine Neuberechnung der Daten durchgef�hrt werden muss.
	 * @see #calc(Graphics, double)
	 * @see #calcIntern(Graphics, double)
	 */
	protected final void setNeedRecalc() {
		needRecalc=true;
	}

	/**
	 * F�hrt eine Vorverarbeitung der �bergebenen Textzeilen durch.<br>
	 * (HTML- und LaTeX-Symbole wurden bereits in Unicode-Zeichen �bersetzt und der Text in Zeilen getrennt.)
	 * @param lines	Auszugebende Zeilen
	 */
	protected abstract void processLines(final String[] lines);

	/**
	 * Bezog sich der letzte Aufruf von {@link #calc(Graphics, double)}
	 * auf einen Export als Vektorgrafik?
	 * @see #calc(Graphics, double)
	 */
	private boolean lastWasExport=false;

	/**
	 * Berechnet die Ausgabedaten neu.<br>
	 * Es wird dabei gepr�ft, ob eine Neuberechnung tats�chlich n�tig ist.
	 * Nur dann wird diese tats�chlich ausgef�hrt.
	 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
	 * @param zoom	Zoom-Level
	 * @see #calcIntern(Graphics, double)
	 */
	public final void calc(final Graphics graphics, final double zoom) {
		final boolean isExport=(graphics instanceof SVGGraphics2D || graphics instanceof VectorGraphics2D);
		if (!needRecalc && lastZoom==zoom && !isExport && !lastWasExport) return;
		lastWasExport=false;
		calcIntern(graphics,zoom);
		if (lastShadowColor!=null) {
			final int delta=getShadowDelta(zoom);
			width+=delta;
			height+=delta;
		}
		needRecalc=!isExport;
		lastZoom=zoom;
		lastWasExport=isExport;
	}

	/**
	 * Berechnet die Ausgabedaten neu.
	 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
	 * @param zoom	Zoom-Level
	 */
	protected abstract void calcIntern(final Graphics graphics, final double zoom);

	/**
	 * Liefert die Pixel-Breite f�r das Textfeld.
	 * @return	Breite f�r das Textfeld
	 * @see #calc(Graphics, double)
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * Liefert die Pixel-H�he f�r das Textfeld.
	 * @return	H�he f�r das Textfeld
	 * @see #calc(Graphics, double)
	 */
	public final int getHeight() {
		return height;
	}

	/**
	 * Berechnet die Verschiebung in Pixeln f�r den Schatten.
	 * @param zoom	Zoom-Level
	 * @return	Verschiebung des Schattens in x- und y-Richtung in Pixeln
	 */
	protected abstract int getShadowDelta(final double zoom);

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

		if (lastComputedShadowColor!=null) {
			graphics.setColor(lastComputedShadowColor);
			final int delta=getShadowDelta(lastZoom);
			drawIntern(graphics,x+delta,y+delta);
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
