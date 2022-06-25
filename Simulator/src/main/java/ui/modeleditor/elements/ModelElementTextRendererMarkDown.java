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
import java.util.ArrayList;
import java.util.List;

/**
 * Text-Renderer, der Markdown interpretiert
 * @author Alexander Herzog
 * @see ModelElementText
 */
public class ModelElementTextRendererMarkDown extends ModelElementTextRenderer {
	/**
	 * Einzelnes Textelement in einer Zeile
	 */
	private static class LineElement {
		/**
		 * Text in dem Element
		 */
		public final String text;

		/**
		 * Überschriftslevel (0 für normalen Text oder 1 bis 3 für h1 bis h3)
		 */
		private final int hLevel;

		/**
		 * Text fett ausgeben?
		 */
		private final boolean bold;

		/**
		 * Text kursiv ausgeben?
		 */
		private final boolean italic;

		/**
		 * Font-Elemente, welches aus den Einstellungen zusammengesetzt wird
		 * @see #setupFont(int, String, double)
		 */
		public Font font;

		/**
		 * Konstruktor der Klasse
		 * @param text	Text in dem Element
		 * @param hLevel	Überschriftslevel (0 für normalen Text oder 1 bis 3 für h1 bis h3)
		 * @param bold	Text fett ausgeben?
		 * @param italic	Text kursiv ausgeben?
		 */
		public LineElement(final String text, final int hLevel, final boolean bold, final boolean italic) {
			this.text=text;
			this.hLevel=hLevel;
			this.bold=bold;
			this.italic=italic;
		}

		/**
		 * Generiert ein Font-Objekt aus den Einstellungen
		 * @param fontSize	Schriftgröße
		 * @param fontFamily	Schriftart
		 * @param zoom	Zoom-Level
		 * @see #font
		 */
		public void setupFont(final int fontSize, final String fontFamily, final double zoom) {
			int style=Font.PLAIN;
			if (bold) style+=Font.BOLD;
			if (italic) style+=Font.ITALIC;
			int add=0;
			switch (hLevel) {
			case 1: add=6; if (!bold) style+=Font.BOLD; break;
			case 2: add=4; if (!bold) style+=Font.BOLD; break;
			case 3: add=2; if (!bold) style+=Font.BOLD; break;
			}
			font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round((fontSize+add)*zoom));
		}
	}

	/**
	 * Auszugebende Textzeilen
	 */
	private final List<List<LineElement>> lines=new ArrayList<>();

	/**
	 * Breiten der Zeilen
	 * @see #calcIntern(Graphics, double)
	 */
	private final List<Integer> lineWidth=new ArrayList<>();

	/**
	 * Höhen der Zeilen über den Basislinien
	 * @see #calcIntern(Graphics, double)
	 */
	private final List<Integer> lineAscent=new ArrayList<>();

	/**
	 * Höhen der Zeilen unter den Basislinien
	 * @see #calcIntern(Graphics, double)
	 */
	private final List<Integer> lineDescent=new ArrayList<>();

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

	@Override
	public void setStyle(final int fontSize, final boolean bold, final boolean italic, final String fontFamily, final ModelElementText.TextAlign textAlign) {
		if (fontSize==this.fontSize && fontFamily==this.fontFamily && textAlign==this.textAlign) return;
		this.fontSize=fontSize;
		this.bold=bold;
		this.italic=italic;
		this.fontFamily=fontFamily;
		this.textAlign=textAlign;
		setNeedRecalc();
	}

	@Override
	protected void processLines(String[] lines) {
		this.lines.clear();

		for (String line: lines) {
			this.lines.add(processLine(line));
		}
	}

	/**
	 * Erzeugt einzelne Elemente aus einer Zeile
	 * @param line	Zu interpretierende Zeile
	 * @return	Liste aus Elementen
	 */
	private List<LineElement> processLine(String line) {
		final List<LineElement> result=new ArrayList<>();

		/* Überschriften erkennen */
		int hLevel=0;
		if (line.startsWith("###")) {hLevel=3; if (line.length()==3) line=""; else line=line.substring(3).trim();}
		if (line.startsWith("##")) {hLevel=2; if (line.length()==2) line=""; else line=line.substring(2).trim();}
		if (line.startsWith("#")) {hLevel=1; if (line.length()==1) line=""; else line=line.substring(1).trim();}
		if (hLevel>0) {
			while (line.endsWith("#")) {
				if (line.length()==1) line=""; else line=line.substring(0,line.length()-1);
			}
		}

		/* Tokens erzeugen und in Zeile einfügen */
		boolean localItalic=false;
		boolean localBold=false;
		for (String token: tokenize(line)) {
			if (token.equals("*") || token.equals("_")) {localItalic=!localItalic; continue;}
			if (token.equals("**") || token.equals("__")) {localBold=!localBold; continue;}
			result.add(new LineElement(token,hLevel,bold || localBold,italic || localItalic));
		}

		return result;
	}

	/**
	 * Zerlegt eine Zeichenkette in einzelne Elemente
	 * @param line	Zeichenkette, die eine Zeile repräsentiert
	 * @return	Liste mit einzelnen Elementen
	 */
	private List<String> tokenize(final String line) {
		final List<String> result=new ArrayList<>();

		final StringBuilder part=new StringBuilder();
		final int len=line.length();
		int i=0;
		while (i<len) {
			final char c=line.charAt(i);
			/* Block-Start- und -End-Symbole */
			if (c=='*')  {
				if (i<len-1 && line.charAt(i+1)=='*')  {
					if (part.length()>0) result.add(part.toString());
					part.setLength(0);
					result.add("**");
					i+=2;
					continue;
				}
				if (part.length()>0) result.add(part.toString());
				part.setLength(0);
				result.add("*");
				i++;
				continue;
			}
			if (c=='_')  {
				if (i<len-1 && line.charAt(i+1)=='_')  {
					if (part.length()>0) result.add(part.toString());
					part.setLength(0);
					result.add("__");
					i+=2;
					continue;
				}
				if (part.length()>0) result.add(part.toString());
				part.setLength(0);
				result.add("_");
				i++;
				continue;
			}
			/* Escape-Zeichen */
			if (c=='\\' && i<len-1) {
				final char cNext=line.charAt(i+1);
				if (cNext=='\\' || cNext=='*' || cNext=='_' || cNext=='#' ) {
					part.append(cNext);
					i+=2;
					continue;
				}
			}
			/* Normale Zeilen */
			part.append(c);
			i++;
		}

		/* Zeichen aus Zwischenspeicher als letzten Eintrag in Liste */
		if (part.length()>0) result.add(part.toString());

		return result;
	}

	@Override
	protected void calcIntern(Graphics graphics, double zoom) {
		width=0;
		height=0;
		lineWidth.clear();
		lineAscent.clear();
		lineDescent.clear();

		for (List<LineElement> line: lines) {
			int lWidth=0;
			int lAscent=0;
			int lDescent=0;
			for (LineElement element: line) {
				element.setupFont(fontSize,fontFamily,zoom);
				graphics.setFont(element.font);
				final FontMetrics metrics=graphics.getFontMetrics();
				lWidth+=metrics.stringWidth(element.text);
				lAscent=Math.max(lAscent,metrics.getAscent());
				lDescent=Math.max(lDescent,metrics.getDescent());
			}
			lineWidth.add(lWidth);
			lineAscent.add(lAscent);
			lineDescent.add(lDescent);

			width=Math.max(width,lWidth);
			height+=lAscent+lDescent;
		}
	}

	@Override
	protected void drawIntern(Graphics graphics, int x, int y) {
		for (int i=0;i<lines.size();i++) {
			final List<LineElement> line=lines.get(i);
			final int ascent=lineAscent.get(i);
			final int descent=lineDescent.get(i);
			final int currentWidth=lineWidth.get(i);

			int lineX;
			switch (textAlign) {
			case LEFT: lineX=x; break;
			case CENTER: lineX=x+(width-currentWidth)/2; break;
			case RIGHT: lineX=x+(width-currentWidth); break;
			default: lineX=x; break;
			}

			int lineY=y+ascent;
			for (LineElement element: line) {
				graphics.setFont(element.font);
				final int w=graphics.getFontMetrics().stringWidth(element.text);
				graphics.drawString(element.text,lineX,lineY);
				lineX+=w;
			}

			y+=ascent+descent;
		}
	}
}
