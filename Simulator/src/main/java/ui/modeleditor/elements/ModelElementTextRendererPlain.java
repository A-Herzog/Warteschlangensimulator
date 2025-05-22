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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.math3.util.FastMath;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;

/**
 * Einfacher Text-Renderer
 * @author Alexander Herzog
 * @see ModelElementText
 */
public class ModelElementTextRendererPlain extends ModelElementTextRenderer {
	/**
	 * Auszugebende Textzeilen
	 */
	private String[] lines;

	/**
	 * Schriftgröße beim letzten Aufruf von {@link #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)}
	 * @see #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private int fontSize=12;

	/**
	 * Text fett ausgeben?
	 * @see #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private boolean bold;

	/**
	 * Text kursiv ausgeben?
	 * @see #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private boolean italic;

	/**
	 * Schriftart
	 * @see #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private String fontFamily="";

	/**
	 * Ausrichtung der Zeilen
	 * @see #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private ModelElementText.TextAlign textAlign;

	/**
	 * Berechnete Schriftart
	 * @see #setStyle(int, boolean, boolean, String, ui.modeleditor.elements.ModelElementText.TextAlign)
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private Font font;

	/**
	 * Höhe der Textzeile über der Grundlinie
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private int ascent;

	/**
	 * Höhe einer einzelnen Zeile
	 * @see #calcIntern(Graphics, double)
	 * @see #draw(Graphics, int, int, Color)
	 */
	private int lineHeight;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementTextRendererPlain() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	protected void processLines(String[] lines) {
		this.lines=lines;
	}
	@Override
	public void setStyle(final int fontSize, final boolean bold, final boolean italic, final String fontFamily, final ModelElementText.TextAlign textAlign) {
		if (fontSize==this.fontSize && bold==this.bold && italic==this.italic && fontFamily.equals(this.fontFamily) && textAlign==this.textAlign) return;
		this.fontSize=fontSize;
		this.bold=bold;
		this.italic=italic;
		this.fontFamily=fontFamily;
		this.textAlign=textAlign;
		setNeedRecalc();
	}

	@Override
	protected void calcIntern(final Graphics graphics, final double zoom) {
		int style=Font.PLAIN;
		if (bold) style+=Font.BOLD;
		if (italic) style+=Font.ITALIC;

		/*
		 * "Sans" und "Serif" werden von jedem SVG-Renderer anders umgesetzt,
		 * daher machen wir hier konkrete Vorgaben, so dass die Laufweite
		 * vorab korrekt berechnet werden kann und Texte und Sub- und Subskripte
		 * zusammen passen.
		 */
		String fontFamily=this.fontFamily;
		if (graphics instanceof SVGGraphics2D || graphics instanceof VectorGraphics2D) {
			if (fontFamily.equals(FontCache.FontFamily.DIALOG.name)) fontFamily=FontCache.FontFamily.WIN_VERDANA.name;
			if (fontFamily.equals(FontCache.FontFamily.SANS.name)) fontFamily=FontCache.FontFamily.WIN_VERDANA.name;
			if (fontFamily.equals(FontCache.FontFamily.SERIF.name)) fontFamily=FontCache.FontFamily.WIN_CAMBRIA.name;
		}

		font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round(fontSize*zoom));

		graphics.setFont(font);

		width=0;
		height=0;
		ascent=graphics.getFontMetrics().getAscent();
		lineHeight=ascent+graphics.getFontMetrics().getDescent();

		for (String line: lines) {
			width=FastMath.max(width,graphics.getFontMetrics().stringWidth(line));
			height+=lineHeight;
		}
	}

	@Override
	protected int getShadowDelta(final double zoom) {
		return (int)Math.round(fontSize*Math.max(0,Math.log(Math.E*zoom))/10);
	}

	@Override
	protected void drawIntern(final Graphics graphics, int x, int y) {
		y+=ascent;

		graphics.setFont(font);
		FontMetrics metrics=null;
		int lineWidth;

		for (String line: lines) {
			switch (textAlign) {
			case LEFT:
				graphics.drawString(line,x,y);
				break;
			case CENTER:
				if (metrics==null) metrics=graphics.getFontMetrics();
				lineWidth=metrics.stringWidth(line);
				graphics.drawString(line,x+(width-lineWidth)/2,y);
				break;
			case RIGHT:
				if (metrics==null) metrics=graphics.getFontMetrics();
				lineWidth=metrics.stringWidth(line);
				graphics.drawString(line,x+(width-lineWidth),y);
				break;
			default:
				graphics.drawString(line,x,y);
				break;
			}
			y+=lineHeight;
		}
	}
}
