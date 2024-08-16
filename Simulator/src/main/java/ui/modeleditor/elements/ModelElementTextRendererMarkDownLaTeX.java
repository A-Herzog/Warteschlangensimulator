/**
 * Copyright 2023 Alexander Herzog
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
import java.util.Arrays;
import java.util.List;

/**
 * Text-Renderer, der Markdown und LaTex-Brüche und -Binomialkoeffizienten interpretiert
 * @author Alexander Herzog
 * @see ModelElementText
 */
public class ModelElementTextRendererMarkDownLaTeX extends ModelElementTextRenderer {
	/**
	 * Beim letzten Aufruf von {@link #setText(String, boolean)} zugewiesene Zeilen.
	 */
	private String[] lastText;

	/**
	 * Sollen Markdown-Formatierungen interpretiert werden?
	 */
	private boolean renderMarkDown;

	/**
	 * Sollen LaTeX-Brüche, -Binomialkoeffizienten und Hoch- und Tiefstellungen interpretiert werden?
	 */
	private boolean renderLaTeX;

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
	 * Auszugebende Textzeilen
	 */
	private final List<List<Token>> lines=new ArrayList<>();

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
	 * Konstruktor der Klasse
	 */
	public ModelElementTextRendererMarkDownLaTeX() {
		renderMarkDown=false;
		renderLaTeX=false;
	}

	/**
	 * Stellt ein, welche Auszeichnungsformate interpretiert werden sollen.
	 * @param renderMarkDown	Sollen Markdown-Formatierungen interpretiert werden?
	 * @param renderLaTeX	Sollen LaTeX-Brüche, -Binomialkoeffizienten und Hoch- und Tiefstellungen interpretiert werden?
	 */
	public void setRenderMode(final boolean renderMarkDown, final boolean renderLaTeX) {
		if (this.renderMarkDown!=renderMarkDown || this.renderLaTeX!=renderLaTeX) setNeedRecalc();
		this.renderMarkDown=renderMarkDown;
		this.renderLaTeX=renderLaTeX;
	}

	@Override
	public void setStyle(final int fontSize, final boolean bold, final boolean italic, final String fontFamily, final ModelElementText.TextAlign textAlign) {
		if (fontSize==this.fontSize && fontFamily.equals(this.fontFamily) && textAlign==this.textAlign && bold==this.bold && italic==this.italic) return;
		this.fontSize=fontSize;
		this.bold=bold;
		this.italic=italic;
		this.fontFamily=fontFamily;
		this.textAlign=textAlign;
		lines.clear(); /* Neuaufbau der Tokens beim nächsten Aufruf von #calcIntern(Graphics, double) erzwingen. */
		setNeedRecalc();
	}

	@Override
	protected void processLines(String[] lines) {
		lastText=Arrays.copyOf(lines,lines.length);

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
	private List<Token> processLine(String line) {
		/* Überschriften erkennen */
		int hLevel=0;
		if (renderMarkDown) {
			if (line.startsWith("###")) {hLevel=3; if (line.length()==3) line=""; else line=line.substring(3).trim();}
			if (line.startsWith("##")) {hLevel=2; if (line.length()==2) line=""; else line=line.substring(2).trim();}
			if (line.startsWith("#")) {hLevel=1; if (line.length()==1) line=""; else line=line.substring(1).trim();}
			if (hLevel>0) {
				while (line.endsWith("#")) {
					if (line.length()==1) line=""; else line=line.substring(0,line.length()-1);
				}
			}
		}

		/* Einzelne Tokens erzeugen */
		final List<String> texts=tokenize(line);

		/* Tokens interpretieren */
		return processTokens(texts,hLevel,bold,italic,false);
	}

	/**
	 * Verarbeitet eine bereits in Text-Tokens zerlegte Zeile.
	 * @param texts	Text-Tokens	(verarbeitete Einträge werden aus der Liste entfernt)
	 * @param hLevel	Überschriftsstatus der Zeile (0 für normaler Text, 1-3 für Überschriften)
	 * @param bold	Zeile als ganzes fett?
	 * @param italic	Zeile als ganzes kursiv?
	 * @param stopOnBracket	Soll nach einer schließenden geschweiften Klammer mit der Verarbeitung aufgehört werden?
	 * @return	Liste der generierten Tokens
	 * @see #processLine(String)
	 */
	private List<Token> processTokens(final List<String> texts, final int hLevel, final boolean bold, final boolean italic, final boolean stopOnBracket) {
		final List<Token> result=new ArrayList<>();

		/* Tokens erzeugen und in Zeile einfügen */
		boolean localItalic=false;
		boolean localBold=false;

		while (texts.size()>0) {
			final String text=texts.get(0);

			if (renderLaTeX) {
				String laTeXCommand=null;
				int laTeXCommandParameters=1;
				for (int i=0;i<laTeXCommands.length;i++) if (text.equals(laTeXCommands[i])) {laTeXCommand=laTeXCommands[i]; laTeXCommandParameters=laTeXCommandsParameters[i]; break;}
				if (laTeXCommand!=null && texts.size()>1 && texts.get(1).equals("{")) {
					texts.remove(0);
					List<Token> sub1=null;
					if (texts.size()>0 && texts.get(0).equals("{")) {
						texts.remove(0);
						sub1=processTokens(texts,hLevel,bold || localBold,italic || localItalic,true);
					}
					List<Token> sub2=null;
					if (laTeXCommandParameters==2) {
						if (texts.size()>0 && texts.get(0).equals("{")) {
							texts.remove(0);
							sub2=processTokens(texts,hLevel,bold || localBold,italic || localItalic,true);
						}
					}
					if (sub1!=null && (laTeXCommandParameters==1 || sub2!=null)) {
						result.add(new Token(laTeXCommand,sub1,sub2,hLevel,bold || localBold,italic || localItalic));
					}
					continue;
				}
				if (text.equals("}") && stopOnBracket) {
					texts.remove(0);
					return result;
				}
			}

			if (renderMarkDown) {
				if (text.equals("*") || text.equals("_")) {localItalic=!localItalic; texts.remove(0); continue;}
				if (text.equals("**") || text.equals("__")) {localBold=!localBold; texts.remove(0); continue;}
			}

			result.add(new Token(text,hLevel,bold || localBold,italic || localItalic));
			texts.remove(0);
		}

		return result;
	}

	/**
	 * In {@link #tokenize(String)} zu erkennende LaTeX-Befehle
	 * @see #laTeXCommandsParameters
	 * @see #tokenize(String)
	 */
	private static String[] laTeXCommands=new String[] {"\\frac","\\binom","^","_"};

	/**
	 * Anzahl an Parametern in jeweils geschweiften Klammern nach einem der Befehle in {@link #laTeXCommands}
	 * @see #laTeXCommands
	 * @see #tokenize(String)
	 */
	private static int[] laTeXCommandsParameters=new int[] {2,2,1,1};

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

			if (renderLaTeX) {
				String laTeXCommand=null;
				final String linePart=line.substring(i);
				for (String sym: laTeXCommands) if (linePart.startsWith(sym+"{")) {laTeXCommand=sym; break;}
				if (laTeXCommand!=null) {
					if (part.length()>0) result.add(part.toString());
					part.setLength(0);
					result.add(laTeXCommand);
					i+=(laTeXCommand.length());
					continue;
				}
				if (c=='{') {
					if (part.length()>0) result.add(part.toString());
					part.setLength(0);
					result.add("{");
					i++;
					continue;
				}
				if (c=='}') {
					if (part.length()>0) result.add(part.toString());
					part.setLength(0);
					result.add("}");
					i++;
					continue;
				}
			}

			if (renderMarkDown) {
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
	protected void calcIntern(final Graphics graphics, final double zoom) {
		/* Von setStyle werden die gerenderten Zeilen gelöscht. Dann müssen diese hier neu aufgebaut werden. */
		if (lines.size()==0) {
			for (String line: lastText) {
				this.lines.add(processLine(line));
			}
		}

		width=0;
		height=0;
		lineWidth.clear();
		lineAscent.clear();
		lineDescent.clear();

		int defaultAscent=-1;
		int defaultDescent=-1;

		for (List<Token> line: lines) {
			/* Elemente in der Zeile */
			int lWidth=0;
			int lAscent=0;
			int lDescent=0;
			boolean lastIsSupSub=false;
			int storedWidth=0;
			for (Token element: line) {
				element.setupFont(fontSize,fontFamily,zoom);
				final int[] info=element.calcSize(graphics);
				if (lastIsSupSub && !element.isSupSub()) {
					lWidth+=storedWidth;
					lastIsSupSub=false;
					storedWidth=0;
				}
				if (element.isSupSub()) {
					lastIsSupSub=true;
					storedWidth=Math.max(storedWidth,info[0]);
				} else {
					lWidth+=info[0];
				}
				lAscent=Math.max(lAscent,info[1]);
				lDescent=Math.max(lDescent,info[2]);
			}
			if (lastIsSupSub) lWidth+=storedWidth;

			/* Leerzeile? */
			if (line.size()==0) {
				if (defaultAscent<0) {
					int style=Font.PLAIN;
					if (bold) style+=Font.BOLD;
					if (italic) style+=Font.ITALIC;
					final Font font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round(fontSize*zoom));
					graphics.setFont(font);
					final FontMetrics metrics=graphics.getFontMetrics();
					defaultAscent=metrics.getAscent();
					defaultDescent=metrics.getDescent();
				}
				lAscent=defaultAscent;
				lDescent=defaultDescent;
			}

			/* Daten zu Zählung hinzufügen */
			lineWidth.add(lWidth);
			lineAscent.add(lAscent);
			lineDescent.add(lDescent);

			width=Math.max(width,lWidth);
			height+=lAscent+lDescent;
		}
	}

	@Override
	protected int getShadowDelta(final double zoom) {
		return (int)Math.round(fontSize*Math.max(0,Math.log(Math.E*zoom))/10);
	}

	@Override
	protected void drawIntern(final Graphics graphics, int x, int y) {
		for (int i=0;i<lines.size();i++) {
			final List<Token> line=lines.get(i);
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
			int storedW=0;
			for (Token token: line) {
				final boolean isSupSub=token.isSupSub();
				if (!isSupSub) {lineX+=storedW; storedW=0;}
				final int w=token.draw(graphics,lineX,lineY);
				if (isSupSub) storedW=Math.max(storedW,w); else lineX+=w;
			}
			y+=ascent+descent;
		}
	}

	/**
	 *
	 * Skalierungsfaktor für hoch- oder tiefgestellte Texte gegenüber normalen Texten
	 * @see ModelElementTextRendererMarkDownLaTeX.Token
	 */
	private static final double indexScaleFactor=0.66;

	/**
	 * Einzelnes Textelement in einer Zeile
	 */
	private static class Token {
		/**
		 * Text in dem Element
		 */
		private final String text;

		/**
		 * Untergeordnete Elemente 1 (z.B. Zähler in einem Bruch)
		 */
		private final List<Token> sub1;

		/**
		 * Untergeordnete Elemente 2 (z.B. Nenner in einem Bruch)
		 */
		private final List<Token> sub2;

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
		private Font font;

		/**
		 * Konstruktor der Klasse
		 * @param text	Text in dem Element
		 * @param hLevel	Überschriftslevel (0 für normalen Text oder 1 bis 3 für h1 bis h3)
		 * @param bold	Text fett ausgeben?
		 * @param italic	Text kursiv ausgeben?
		 */
		public Token(final String text, final int hLevel, final boolean bold, final boolean italic) {
			this.text=text;
			this.sub1=null;
			this.sub2=null;
			this.hLevel=hLevel;
			this.bold=bold;
			this.italic=italic;
		}

		/**
		 * Konstruktor der Klasse
		 * @param text	Text in dem Element
		 * @param sub1	Untergeordnete Elemente 1 (z.B. Zähler in einem Bruch)
		 * @param sub2	Untergeordnete Elemente 2 (z.B. Nenner in einem Bruch)
		 * @param hLevel	Überschriftslevel (0 für normalen Text oder 1 bis 3 für h1 bis h3)
		 * @param bold	Text fett ausgeben?
		 * @param italic	Text kursiv ausgeben?
		 */
		public Token(final String text, final List<Token> sub1, final List<Token> sub2, final int hLevel, final boolean bold, final boolean italic) {
			this.text=text;
			this.sub1=sub1;
			this.sub2=sub2;
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

			if (sub1==null) {
				/* Normales Element */
				font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round((fontSize+add)*zoom));
			} else {
				/* Element mit Untereinträgen */
				if (text.equals("_")) {
					font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round((fontSize+add)*zoom));
					for (Token token: sub1) token.setupFont(fontSize,fontFamily,zoom*indexScaleFactor);
				}
				if (text.equals("^")) {
					font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round((fontSize+add)*zoom));
					for (Token token: sub1) token.setupFont(fontSize,fontFamily,zoom*indexScaleFactor);
				}
				if (text.equals("\\frac")) {
					font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round((fontSize+add)*zoom));
					for (Token token: sub1) token.setupFont(fontSize,fontFamily,zoom*0.5);
					for (Token token: sub2) token.setupFont(fontSize,fontFamily,zoom*0.5);
				}
				if (text.equals("\\binom")) {
					font=FontCache.getFontCache().getFont(fontFamily,style,(int)Math.round((fontSize+add)*zoom));
					for (Token token: sub1) token.setupFont(fontSize,fontFamily,zoom*0.5);
					for (Token token: sub2) token.setupFont(fontSize,fontFamily,zoom*0.5);
				}
			}
		}

		/**
		 * Berechnet die Größe der Unterelemente 1 des Textelements.
		 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
		 * @return	Array aus 3 Elementen: Breite des Textes, Höhe über der Grundlinie, Tiefe unter der Grundlinie
		 * @see #calcSize(Graphics)
		 */
		private int[] calcSizeSub1(final Graphics graphics) {
			int w=0;
			int ascent=0;
			int descent=0;
			if (sub1!=null) for (Token token: sub1) {
				final int[] info=token.calcSize(graphics);
				w+=info[0];
				ascent=Math.max(ascent,info[1]);
				descent=Math.max(descent,info[2]);
			}
			return new int[] {w,ascent,descent};
		}

		/**
		 * Berechnet die Größe der Unterelemente 2 des Textelements.
		 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
		 * @return	Array aus 3 Elementen: Breite des Textes, Höhe über der Grundlinie, Tiefe unter der Grundlinie
		 * @see #calcSize(Graphics)
		 */
		private int[] calcSizeSub2(final Graphics graphics) {
			int w=0;
			int ascent=0;
			int descent=0;
			if (sub2!=null) for (Token token: sub2) {
				final int[] info=token.calcSize(graphics);
				w+=info[0];
				ascent=Math.max(ascent,info[1]);
				descent=Math.max(descent,info[2]);
			}
			return new int[] {w,ascent,descent};
		}

		/**
		 * Berechnet die Größe des Textelements.
		 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
		 * @return	Array aus 3 Elementen: Breite des Textes, Höhe über der Grundlinie, Tiefe unter der Grundlinie
		 */
		public int[] calcSize(final Graphics graphics) {
			graphics.setFont(font);
			final FontMetrics metrics=graphics.getFontMetrics();
			final int ascent=metrics.getAscent();
			final int descent=metrics.getDescent();

			if (sub1==null || sub2==null) {
				/* Normales Element */
				return new int[] {metrics.stringWidth(text), ascent, descent};
			} else {
				/* Element mit Untereinträgen */
				if (text.equals("_")) {
					final int[] info=calcSizeSub1(graphics);
					return new int[] {info[0], ascent, descent};
				}
				if (text.equals("^")) {
					final int[] info=calcSizeSub1(graphics);
					return new int[] {info[0], ascent, descent};
				}

				if (text.equals("\\frac")) {
					final int[] info1=calcSizeSub1(graphics);
					final int[] info2=calcSizeSub2(graphics);
					return new int[] {Math.max(info1[0],info2[0]), ascent, descent};
				}

				if (text.equals("\\binom")) {
					final int[] info1=calcSizeSub1(graphics);
					final int[] info2=calcSizeSub2(graphics);
					return new int[] {Math.max(info1[0],info2[0]), ascent, descent};
				}

				return new int[] {0,0,0};
			}
		}

		/**
		 * Handelt es sich bei dem Element um eine Hoch- oder Tiefstellung?
		 * @return	Hoch- oder Tiefstellung?
		 */
		public boolean isSupSub() {
			return text.equals("^") || text.equals("_");
		}

		/**
		 * Gibt den Text aus Unterelement 1 aus.
		 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
		 * @param x	Linke Kante des Ausgabebereichs
		 * @param y	Obere Kante des Ausgabebereichs
		 * @return	Breite des ausgegebenen Textes
		 * @see #draw(Graphics, int, int)
		 */
		private int drawSub1(final Graphics graphics, int x, int y) {
			int wSum=0;
			if (sub1!=null) for (Token token: sub1) {
				final int w=token.draw(graphics,x+wSum,y);
				wSum+=w;
			}
			return wSum;
		}

		/**
		 * Gibt den Text aus Unterelement 2 aus.
		 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
		 * @param x	Linke Kante des Ausgabebereichs
		 * @param y	Obere Kante des Ausgabebereichs
		 * @return	Breite des ausgegebenen Textes
		 * @see #draw(Graphics, int, int)
		 */
		private int drawSub2(final Graphics graphics, int x, int y) {
			int wSum=0;
			if (sub2!=null) for (Token token: sub2) {
				final int w=token.draw(graphics,x+wSum,y);
				wSum+=w;
			}
			return wSum;
		}

		/**
		 * Gibt den Text aus.
		 * @param graphics	Grafik-Objekt in das der Text geschrieben werden soll
		 * @param x	Linke Kante des Ausgabebereichs
		 * @param y	Obere Kante des Ausgabebereichs
		 * @return	Breite des ausgegebenen Textes
		 */
		public int draw(final Graphics graphics, int x, int y) {
			graphics.setFont(font);
			final FontMetrics metrics=graphics.getFontMetrics();

			if (sub1==null) {
				/* Normales Element */
				final int w=metrics.stringWidth(text);
				graphics.drawString(text,x,y);
				return w;
			} else {
				/* Element mit Untereinträgen */

				final int ascent=metrics.getAscent();
				final int descent=metrics.getDescent();

				if (text.equals("_")) {
					y=(int)Math.round(y+descent-descent*indexScaleFactor);
					return drawSub1(graphics,x,y);
				}
				if (text.equals("^")) {
					y=(int)Math.round(y-ascent+ascent*indexScaleFactor);
					return drawSub1(graphics,x,y);
				}
				if (text.equals("\\frac")) {
					final int y1=(int)Math.round(y-ascent+ascent*0.5);
					final int w1=drawSub1(graphics,x,y1);
					final int y2=(int)Math.round(y-ascent+(ascent+descent)*0.5+ascent*0.5);
					final int w2=drawSub2(graphics,x,y2);
					final int w=Math.max(w1,w2);
					final int yLine=(int)Math.round(y1+descent*0.5);
					graphics.drawLine(x,yLine,x+w,yLine);
					return w;
				}
				if (text.equals("\\binom")) {
					graphics.drawString("(",x,y);
					int w=metrics.stringWidth("(");
					final int y1=(int)Math.round(y-ascent+ascent*0.5);
					final int w1=drawSub1(graphics,x+w,y1);
					final int y2=(int)Math.round(y-ascent+(ascent+descent)*0.5+ascent*0.5);
					final int w2=drawSub2(graphics,x+w,y2);
					w+=Math.max(w1,w2);
					graphics.setFont(font);
					graphics.drawString(")",x+w,y);
					w+=metrics.stringWidth(")");
					return w;
				}
				return 0;
			}
		}
	}
}
