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
package systemtools.statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Klasse zur Erstellung von pdf-Dateien.
 * @author Alexander Herzog
 * @see org.apache.pdfbox
 */
public class PDFWriter extends PDFWriterBase {
	/**
	 * Horizontale Ausrichtung der Seitenzahl in der Fußzeile
	 */
	private static final ReportStyle.ReportPosition footerPageNuberPosition=ReportStyle.ReportPosition.CENTER;

	/**
	 * Horizontale Ausrichtung des Datums in der Fußzeile
	 */
	private static final ReportStyle.ReportPosition footerDatePosition=ReportStyle.ReportPosition.RIGHT;

	/**
	 * Abstand (in pt) nach einem Bild
	 */
	private static final int imageVerticalMarginBottomPt=25;

	/**
	 * Konfigurationsobjekt
	 */
	private final ReportStyle style;

	/**
	 * Hält die Schriftgrößen in pt vor
	 */
	private final PDFFontMetrics fontMetrics;

	/**
	 * Seitenränder in pt
	 */
	private final PDFWriterBorderPT border;

	/**
	 * Liefert die aktuelle Y-Position für die Text-Ausgabe.
	 */
	protected int positionY;

	/**
	 * 1-basierte Nummer der aktuellen Seite
	 * @see #newPage()
	 */
	protected int currentPageNumber;

	/**
	 * Konstruktor der Klasse <code>PDFWriterBase</code>
	 * @param owner Übergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird.
	 * @param style	Konfigurationsobjekt
	 */
	public PDFWriter(final Component owner, final ReportStyle style) {
		this.style=(style==null)?new ReportStyle():new ReportStyle(style);

		/* Schriftgrößen bestimmen */
		fontMetrics=new PDFFontMetrics(owner);

		/* Schriftarten ok? */
		systemOK=fontMetrics.systemOK;

		/* Rand in PT speichern */
		border=new PDFWriterBorderPT(style);

		/* Erste Seite anlegen */
		newPage();
	}

	/**
	 * Beginnt eine neue Seite im Dokument.
	 */
	@Override
	protected boolean newPage() {
		if (!super.newPage()) return false;

		positionY=pageHeightPT-border.top;
		currentPageNumber++;

		if (style.logo!=null && (currentPageNumber==1 || style.logoRepeat)) {
			return writeImage(style.logo,style.logoPosition,positionY,style.logoMaxWidthMM,style.logoMaxHeightMM,imageVerticalMarginBottomPt);
		} else {
			return true;
		}
	}

	/**
	 * Liefert basierend auf Schriftfamilie und fett/nicht fett Status die zugehörige PDF-Schriftfamilie
	 * @param font	Schriftart
	 * @return	PDF-Schriftfamilie
	 */
	private PDType1Font getPDFFontFamily(final ReportStyle.ReportFont font) {
		switch (font.family) {
		case SANS_SERIF: return font.bold?PDType1Font.HELVETICA_BOLD:PDType1Font.HELVETICA;
		case SERIF: return font.bold?PDType1Font.TIMES_BOLD:PDType1Font.TIMES_ROMAN;
		case TYPEWRITER: return font.bold?PDType1Font.COURIER_BOLD:PDType1Font.COURIER;
		default: return font.bold?PDType1Font.HELVETICA_BOLD:PDType1Font.HELVETICA;
		}
	}

	/**
	 * Gibt ein einfaches Textobjekt aus.
	 * @param text	Auszugebender Text
	 * @param font	Schriftart
	 * @param x	x-Koordinate für das Textobjekt
	 * @param y	y-Koordinate für das Textobjekt
	 * @param color	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	private boolean writeTextObject(final String text, final ReportStyle.ReportFont font, final int x, final int y, final Color color) {
		return writeTextObject(text,font.size,getPDFFontFamily(font),x,y,color);
	}

	/**
	 * Gibt ein einfaches Textobjekt aus.
	 * @param text	Auszugebender Text
	 * @param font	Schriftart
	 * @param align	Horizontale Ausrichtung der Textzeile
	 * @param y	y-Koordinate für das Textobjekt
	 * @param color	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	private boolean writeTextObject(final String text, final ReportStyle.ReportFont font, final ReportStyle.ReportPosition align, final int y, final Color color) {
		final int w=fontMetrics.getTextWidth(text,font);
		final int x;

		switch (align) {
		case LEFT: x=border.left; break;
		case CENTER: x=pageWidthPT/2-w/2; break;
		case RIGHT: x=pageWidthPT-border.right-w; break;
		default: x=border.left; break;
		}
		return writeTextObject(text,font,x,y,color);
	}

	@Override
	protected void outputPageFooter() {
		if (style.footerPageNumbers) {
			final String text=""+currentPageNumber;
			writeTextObject(text,style.footerFont,footerPageNuberPosition,border.bottom,null);
		}
		if (style.footerDate) {
			final String text=LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
			writeTextObject(text,style.footerFont,footerDatePosition,border.bottom,null);
		}
	}

	/**
	 * Zerlegt mehrere Textzeilen gemäß in dem Text enthaltenen \n und gemäß einer vorgegebenen Breite in mehrere Zeilen
	 * @param text	Weiter zu zerlegende Zeilen
	 * @param fontSize	Schriftgröße in PT (für die Laufweitenberechnung)
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll (für die Laufweitenberechnung).
	 * @param maxWidth	Zur Verfügung stehende Breite in PT.
	 * @param forceWidth	Umbruch nur an Leerzeichen oder zur Not (um <code>maxWidth</code> einzuhalten) auch mitten im Wort
	 * @return	Text in Form von einzelnen Zeilen
	 */
	private String[] splitLine(String[] text, int fontSize, boolean fontBold, int maxWidth, boolean forceWidth) {
		final List<String> output=new ArrayList<>();
		for (int i=0;i<text.length;i++) {
			String[] lines=text[i].split("\n");
			for (int j=0;j<lines.length;j++) {
				String rest=lines[j].trim();
				while (!rest.isEmpty()) {
					String line=rest;
					rest="";
					while (fontMetrics.getTextWidth(line,fontSize,fontBold)>maxWidth) {
						int k=line.lastIndexOf(' ');
						if (k==-1) {
							if (!forceWidth) break;
							rest=line.substring(line.length()-1)+rest;
							line=line.substring(0,line.length()-1);
						} else {
							if (!rest.isEmpty()) rest=" "+rest;
							rest=line.substring(k+1)+rest;
							line=line.substring(0,k);
						}
					}
					output.add(line);
				}
			}
		}
		return output.toArray(new String[0]);
	}

	/**
	 * Gibt die auf der aktuellen Seite noch verfügbare Höhe zurück.
	 * @return	Noch verfügbare Höhe in pt
	 */
	private int availableHeight() {
		if (!systemOK) return 0;
		final int footer=(style.footerPageNumbers || style.footerDate)?fontMetrics.getLineHeight(style.footerFont):0;
		return Math.max(0,positionY-footer-border.bottom);
	}

	/**
	 * Gibt an, ob eine weitere Zeile in einer bestimmten Schriftgröße auf die Seite passt (unter Einhaltung der angegebenen Ränder).
	 * @param fontSize	Schriftgröße der nächsten Zeile
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lines	Gibt an, wie viele Zeilen noch auf die Seite passen sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Zeile nicht mehr auf die Seite passt, also eine neue Seite benötigt wird.
	 */
	private boolean newPageNeeded(final int fontSize, final boolean fontBold, final int lines) {
		if (!systemOK) return false;
		return availableHeight()<fontMetrics.getLineHeight(fontSize,fontBold)*lines;
	}

	/**
	 * Gibt an, ob eine weitere Zeile in einer bestimmten Schriftgröße auf die Seite passt (unter Einhaltung der angegebenen Ränder).
	 * @param font	Schriftart
	 * @param lines	Gibt an, wie viele Zeilen noch auf die Seite passen sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Zeile nicht mehr auf die Seite passt, also eine neue Seite benötigt wird.
	 */
	private boolean newPageNeeded(final ReportStyle.ReportFont font, final int lines) {
		if (!systemOK) return false;
		return availableHeight()<fontMetrics.getLineHeight(font)*lines;
	}

	/**
	 * Gibt ein einfaches Textobjekt (ohne Zeilenumbrüche und ohne Berücksichtigung der Ränder) aus.
	 * @param text	Auszugebender Text
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Zugehörige Anzahl an PT, um die die Y-Position nach der Zeile verschoben werden soll
	 * @param additionalIndent	Gibt an, um wie viele PT des Textobjekt zusätzlich von links eingerückt werden soll
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	private boolean writeText(final String text, final int fontSize, final boolean fontBold, final int lineSeparation, final int additionalIndent, final Color textColor) {
		if (!writeTextObject(text,fontSize,fontBold,border.left+additionalIndent,positionY,textColor)) return false;
		positionY-=(fontMetrics.getLineHeight(fontSize,fontBold)+lineSeparation);
		return true;
	}

	/**
	 * Gibt ein einfaches Textobjekt (ohne Zeilenumbrüche und ohne Berücksichtigung der Ränder) aus.
	 * @param text	Auszugebender Text
	 * @param font	Schriftart
	 * @param additionalIndent	Gibt an, um wie viele PT des Textobjekt zusätzlich von links eingerückt werden soll
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	private boolean writeText(final String text, final ReportStyle.ReportFont font, final int additionalIndent, final Color textColor) {
		if (!writeTextObject(text,font,border.left+additionalIndent,positionY,textColor)) return false;
		positionY-=fontMetrics.getLineHeight(font);
		return true;
	}

	/**
	 * Gibt mehrere einfache Textobjekte (ohne Zeilenumbrüche und ohne Berücksichtigung der Ränder) untereinander aus.
	 * Nötigenfalls werden Seitenumbrüche eingefügt.
	 * @param lines	Auszugebende Zeilen
	 * @param font	Schriftart
	 * @param additionalIndent	Gibt an, um wie viele PT des Textobjekt zusätzlich von links eingerückt werden soll
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn die Text-Objekte erfolgreich in die pdf eingefügt werden konnten
	 */
	private boolean writeText(final String[] lines, final ReportStyle.ReportFont font, final int additionalIndent, final Color textColor) {
		final ReportStyle.ReportFont fontNoLineSkip=new ReportStyle.ReportFont(font);
		fontNoLineSkip.lineSeparation=0;

		for (int i=0;i<lines.length;i++) {
			if (newPageNeeded(font,1)) newPage();
			if (!writeText(lines[i],(i==lines.length-1)?font:fontNoLineSkip,additionalIndent,textColor)) return false;
		}
		return true;
	}

	/**
	 * Gibt mehrere einfache Textobjekte (ohne Zeilenumbrüche und ohne Berücksichtigung der Ränder) untereinander aus.
	 * Nötigenfalls werden Seitenumbrüche eingefügt.
	 * @param lines	Auszugebende Zeilen
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Zugehörige Anzahl an PT, um die die Y-Position nach der letzten Zeile verschoben werden soll
	 * @param additionalIndent	Gibt an, um wie viele PT des Textobjekt zusätzlich von links eingerückt werden soll
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn die Text-Objekte erfolgreich in die pdf eingefügt werden konnten
	 */
	private boolean writeText(final String[] lines, final int fontSize, final boolean fontBold, final int lineSeparation, final int additionalIndent, final Color textColor) {
		for (int i=0;i<lines.length;i++) {
			if (newPageNeeded(fontSize,fontBold,1)) newPage();
			if (!writeText(lines[i],fontSize,fontBold,(i==lines.length-1)?lineSeparation:0,additionalIndent,textColor)) return false;
		}
		return true;
	}

	/**
	 * Gibt eine Überschrift unter Verwendung der Style-Konfiguration aus.
	 * @param text	Auszugebender Text
	 * @param level	Level der Überschrift (1-3)
	 * @param additionalIndent	Zusätzliche Einrückung
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeStyledHeading(final String text, int level, final int additionalIndent, final Color textColor) {
		level=Math.max(1,Math.min(style.headingFont.length,level));

		final ReportStyle.ReportFont baseFont=style.headingFont[level-1];
		final ReportStyle.ReportFont baseFontNoLineSkip=new ReportStyle.ReportFont(baseFont);
		baseFontNoLineSkip.lineSeparation=0;

		final String[] lines=splitLine(new String[]{text},baseFont.size,baseFont.bold,pageWidthPT-border.left-border.right,true);
		for (int i=0;i<lines.length;i++) {
			final ReportStyle.ReportFont font=(i==lines.length-1)?baseFont:baseFontNoLineSkip;
			if (newPageNeeded(font,1)) newPage();
			if (!writeText(lines[i],font,additionalIndent,textColor)) return false;
		}

		return true;
	}

	/**
	 * Gibt eine Überschrift unter Verwendung der Style-Konfiguration aus.
	 * @param text	Auszugebender Text
	 * @param level	Level der Überschrift (1-3)
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeStyledHeading(final String text, int level) {
		return writeStyledHeading(text,level,0,null);
	}

	/**
	 * Gibt einen Text unter Verwendung der Style-Konfiguration aus.
	 * @param text	Auszugebender Text
	 * @param additionalIndent	Zusätzliche Einrückung
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeStyledText(final String text, final int additionalIndent, final Color textColor) {
		final ReportStyle.ReportFont baseFont=style.textFont;

		final ReportStyle.ReportFont baseFontNoLineSkip=new ReportStyle.ReportFont(baseFont);
		baseFontNoLineSkip.lineSeparation=0;

		final String[] lines=splitLine(new String[]{text},baseFont.size,baseFont.bold,pageWidthPT-border.left-border.right,true);
		for (int i=0;i<lines.length;i++) {
			final ReportStyle.ReportFont font=(i==lines.length-1)?baseFont:baseFontNoLineSkip;
			if (newPageNeeded(font,1)) newPage();
			if (!writeText(lines[i],font,additionalIndent,textColor)) return false;
		}

		return true;
	}

	/**
	 * Gibt einen Text unter Verwendung der Style-Konfiguration aus.
	 * @param text	Auszugebender Text
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeStyledText(final String text) {
		return writeStyledText(text,0,null);
	}

	/**
	 * Gibt einen Absatz-Abstand unter Verwendung der Style-Konfiguration aus.
	 */
	public void writeStyledParSkip() {
		positionY-=style.parSkip;
	}

	/**
	 * Fügt etwas Abstand unter der aktuellen Ausgabe ein.
	 * @param lineSeparation	Einzufügender Abstand in PT
	 */
	public void writeEmptySpace(final int lineSeparation) {
		positionY-=lineSeparation;
	}

	/**
	 * Gibt eine Zeichenkette aus, die wenn nötig umgebrochen wird.
	 * @param text	Auszugebende Zeichenkette
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Gibt an, wie viele Punkte nach der Textzeile Abstand gehalten werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeText(final String text, final int fontSize, final boolean fontBold, final int lineSeparation) {
		final String[] lines=splitLine(new String[]{text},fontSize,fontBold,pageWidthPT-border.left-border.right,true);
		return writeText(lines,fontSize,fontBold,lineSeparation,0,null);
	}

	/**
	 * Gibt eine Zeichenkette aus, die wenn nötig umgebrochen wird.
	 * @param text	Auszugebende Zeichenkette
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Gibt an, wie viele Punkte nach der Textzeile Abstand gehalten werden sollen.
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeText(final String text, final int fontSize, final boolean fontBold, final int lineSeparation, final Color textColor) {
		final String[] lines=splitLine(new String[]{text},fontSize,fontBold,pageWidthPT-border.left-border.right,true);
		return writeText(lines,fontSize,fontBold,lineSeparation,0,textColor);
	}

	/**
	 * Gibt eine Tabellenzeile aus (inkl. Rahmen um die Zellen).
	 * Die Spaltenbreiten werden dabei gleich verteilt, allerdings ist die erste Spalte doppelt so breit, wie die anderen.
	 * @param text	Zeichenketten für die einzelnen Spalten.
	 * @param font	Schriftart
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	private boolean writeTableLine(final String[] text, final ReportStyle.ReportFont font) {
		int contentAreaWidthPT=pageWidthPT-border.left-border.right;

		final float lineWidthMM=0.1f;
		final Color lineColor=Color.BLACK;

		final List<String[]> cols=new ArrayList<>();
		int maxLines=0;
		for (int i=0;i<text.length;i++) {
			int colWidth=(int)Math.round((double)contentAreaWidthPT*((i==0)?2:1)/(text.length+1));
			String[] lines=splitLine(new String[]{text[i]},font.size,font.bold,colWidth,true);
			cols.add(lines);
			maxLines=Math.max(maxLines,lines.length);
		}

		if (newPageNeeded(font,maxLines)) newPage();

		int indent=0;
		int storePositionY=positionY;
		int minPositionY=positionY;
		for (int i=0;i<cols.size();i++) {
			int colWidth=(int)Math.round((double)contentAreaWidthPT*((i==0)?2:1)/(text.length+1));
			if (!writeText(cols.get(i),font,indent,null)) return false;
			if (!drawLine(border.left+indent,storePositionY,border.left+indent,storePositionY-maxLines*(font.size+fontMetrics.getDescent(font)),lineWidthMM,lineColor)) return false;
			indent+=colWidth;
			if (i==cols.size()-1) {
				if (!drawLine(border.left+indent,storePositionY,border.left+indent,storePositionY-maxLines*(font.size+fontMetrics.getDescent(font)),lineWidthMM,lineColor)) return false;
			}
			minPositionY=Math.min(minPositionY,positionY);
			if (i<cols.size()-1) positionY=storePositionY; else positionY=minPositionY;
		}
		if (!drawLine(border.left,storePositionY,border.left+indent,storePositionY,lineWidthMM,lineColor)) return false;
		if (!drawLine(border.left,storePositionY-maxLines*(font.size+fontMetrics.getDescent(font)),border.left+indent,storePositionY-maxLines*(font.size+fontMetrics.getDescent(font)),lineWidthMM,lineColor)) return false;

		return true;
	}

	/**
	 * Gibt eine Tabellenüberschriftzeile aus.
	 * @param text	Zellen in der Tabellen Zeile
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeStyledTableHeader(final List<String> text) {
		return writeTableLine(text.toArray(new String[0]),style.tableHeadingFont);
	}

	/**
	 * Gibt eine Tabelleninhaltszeile aus.
	 * @param text	Zellen in der Tabellen Zeile
	 * @param isLastLine	Handelt es sich um die letzte Zeile der Tabelle?
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeStyledTableLine(final List<String> text, final boolean isLastLine) {
		final ReportStyle.ReportFont font=isLastLine?style.tableLastLineTextFont:style.tableTextFont;
		return writeTableLine(text.toArray(new String[0]),font);
	}

	/**
	 * Schreibt das angegebene Bild in die pdf-Datei
	 * @param image	Einzufügendes Bild
	 * @param lineSeparation	Abstand in PT, der unter dem Bild eingefügt werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeImageFullWidth(final BufferedImage image, final int lineSeparation) {
		if (!systemOK) return false;

		final int contentWidth=(pageWidthPT-border.left-border.right);
		final float neededHeight=((float)image.getHeight())/image.getWidth()*contentWidth;

		if (availableHeight()<neededHeight) newPage();
		if (!writeImageObject(image,border.left,positionY-neededHeight,contentWidth,neededHeight)) return false;

		positionY-=(neededHeight+lineSeparation);
		return true;
	}

	/**
	 * Schreibt das angegebene Bild in die pdf-Datei
	 * @param image	Einzufügendes Bild
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeImageFullWidth(final BufferedImage image) {
		return writeImageFullWidth(image,imageVerticalMarginBottomPt);

	}

	/**
	 * Berechnet die Ausgabegröße für ein Bild.
	 * @param image	Auszugebendes Bild
	 * @param maxWidthMM	Maximale Breite in MM
	 * @param maxHeightMM	Maximale Höhe in MM
	 * @return	Größe in pt
	 */
	private Dimension calcImageSize(final BufferedImage image, int maxWidthMM, int maxHeightMM) {
		if (maxWidthMM<=0) maxWidthMM=500;
		if (maxHeightMM<=0) maxHeightMM=500;

		int maxWidth=(int)Math.round(maxWidthMM/25.4*72);
		int maxHeight=(int)Math.round(maxHeightMM/25.4*72);

		final int maxContentWidth=pageWidthPT-border.left-border.right;
		if (maxWidth>maxContentWidth) maxWidth=maxContentWidth;

		final int maxContentHeight=pageHeightPT-border.top-border.bottom-((style.footerPageNumbers || style.footerDate)?fontMetrics.getLineHeight(style.footerFont):0);
		if (maxHeight>maxContentHeight) maxHeight=maxContentHeight;

		final double ratio=((double)image.getWidth())/image.getHeight();

		int width;
		int height;
		if (maxWidth/ratio>maxHeight) {
			height=maxHeight;
			width=(int)Math.round(height*ratio);
		} else {
			width=maxWidth;
			height=(int)Math.round(width/ratio);
		}

		return new Dimension(width,height);
	}

	/**
	 * Schreibt das angegebene Bild in die pdf-Datei
	 * @param image	Einzufügendes Bild
	 * @param align	Horizontale Ausrichtung des Bildes
	 * @param y	y-Koordinate für das Bild
	 * @param maxWidthMM	Maximale Breite in MM
	 * @param maxHeightMM	Maximale Höhe in MM
	 * @param lineSeparation	Zugehörige Anzahl an PT, um die die Y-Position nach der Zeile verschoben werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich in die pdf eingefügt werden konnte
	 */
	private boolean writeImage(final BufferedImage image, final ReportStyle.ReportPosition align, final int y, final int maxWidthMM, final int maxHeightMM, final int lineSeparation) {
		if (!systemOK) return false;
		final Dimension size=calcImageSize(image,maxWidthMM,maxHeightMM);

		if (availableHeight()<size.height) newPage();
		final int x;
		switch (align) {
		case LEFT: x=border.left; break;
		case CENTER: x=pageWidthPT/2-size.width/2; break;
		case RIGHT: x=pageWidthPT-border.right-size.width; break;
		default: x=border.left; break;
		}
		if (!writeImageObject(image,x,positionY-size.height,size.width,size.height)) return false;

		positionY-=(size.height+lineSeparation);
		return true;
	}

	/**
	 * Funktion zum Testen der PDF-Ausgabe
	 * @param owner	Übergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird.
	 * @param inputImage	Bilddatei, die in die pdf eingebettet wird.
	 * @param outputPDF	Ausgabe-pdf-Datei.
	 * @return	Gibt <code>true</code> zurück, wenn die pdf erfolgreich erstellt werden konnte.
	 */
	public static boolean example(final Component owner, final File inputImage, final File outputPDF) {
		PDFWriter pdf=new PDFWriter(owner,new ReportStyle());
		if (!pdf.systemOK) return false;

		/* Text */
		if (!pdf.writeText("Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. ",12,false,2)) return false;
		if (!pdf.writeText("Zeile 2",12,true,2)) return false;
		for (int i=1;i<100;i++) if (!pdf.writeText("Test "+i,12,false,0)) return false;

		/* Tabelle */
		if (!pdf.writeTableLine(new String[]{"Überschrift Spalte 1","Überschrift Spalte 2","Überschrift Spalte 3","Dies ist eine lange Überschrift für Spalte 4"},new ReportStyle.ReportFont(12,true,0))) return false;
		if (!pdf.writeTableLine(new String[]{"Spalte 1","0123456789012345678901234567890","Spalte 3","Spalte 4"},new ReportStyle.ReportFont(12,false,10))) return false;
		if (!pdf.writeText("Text unter der Tabelle",12,false,2)) return false;

		/* Bilder */
		try {
			BufferedImage image=ImageIO.read(inputImage);
			if (!pdf.writeImageFullWidth(image,10)) return false;
		} catch (IOException e) {return false;}

		return pdf.save(outputPDF);
	}
}