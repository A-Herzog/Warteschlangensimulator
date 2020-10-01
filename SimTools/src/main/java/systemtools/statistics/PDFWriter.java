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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

/**
 * Klasse zur Erstellung von pdf-Dateien.
 * Diese Klasse stellt nur Basisfunktionen zur Verfügung.
 * @author Alexander Herzog
 * @see org.apache.pdfbox
 * @version 1.3
 */
public class PDFWriter {
	private final PDDocument doc;
	private PDPageContentStream contentStream;
	private final FontMetrics metricsPlain, metricsBold;

	/**
	 * Ordner in dem das PDF-System einen Font-Cache-Datei ablegt.
	 */
	public static File cacheFolder=getProgramFolder();

	/**
	 * Gibt an, ob das System korrekt initialisiert werden konnte (wird vom Konstruktor gesetzt).
	 * Wenn <code>systemOK</code> auf <code>false</code> steht, sind keine weiteren Bearbeitungen möglich.
	 */
	public final boolean systemOK;

	/**
	 * Liefert die Höhe der aktuellen Seiten in PT.
	 */
	protected int pageHeightPT;

	/**
	 * Liefert die Breite der aktuellen Seiten in PT.
	 */
	protected int pageWidthPT;

	/**
	 * Liefert den Rand oben und unten in PT.
	 */
	protected final int borderTopBottomPT;

	/**
	 * Liefert den Rand links und rechts in PT.
	 */
	protected final int borderLeftRightPT;

	/**
	 * Liefert die aktuelle Y-Position für die Text-Ausgabe.
	 */
	protected int positionY;

	/**
	 * Konstruktor der Klasse <code>PDFWriterBase</code>
	 * @param owner Übergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird.
	 * @param borderTopBottomMM	Linker und rechter Abstand von Text zum Seitenrand in Millimetern
	 * @param borderLeftRightMM	Oberer und unterer Abstand von Text zum Seitenrand in Millimetern
	 */
	public PDFWriter(final Component owner, final int borderTopBottomMM, final int borderLeftRightMM) {
		/* Verzeichnis für ".pdfbox.cache"-Datei - lässt sich leider nicht komplett abschalten. */
		System.setProperty("pdfbox.fontcache",cacheFolder.toString());

		/* Rand in PT speichern */
		borderTopBottomPT=(int)Math.round(borderTopBottomMM/25.4*72);
		borderLeftRightPT=(int)Math.round(borderLeftRightMM/25.4*72);

		/* Dokument erstellen */
		doc=new PDDocument();

		/* In etwa passende AWT-Fonts laden zur späteren Berechnung der Breite von Texten */
		Graphics g=null;
		if (owner!=null) g=owner.getGraphics();

		/* Wenn wir ohne Bildschirmausgabe laufen */
		if (g==null) {
			BufferedImage offscreenImage=new BufferedImage(10,10,BufferedImage.TYPE_INT_BGR);
			g=offscreenImage.getGraphics();
		}

		if (g!=null) {
			metricsPlain=g.getFontMetrics(new Font(Font.SANS_SERIF,Font.PLAIN,100));
			metricsBold=g.getFontMetrics(new Font(Font.SANS_SERIF,Font.BOLD,100));
		} else {
			metricsPlain=null;
			metricsBold=null;
		}

		/* Schriftarten ok? */
		systemOK=(metricsPlain!=null && metricsBold!=null);

		/* Erste Seite anlegen */
		newPage();
	}

	private static File getProgramFolder() {
		try {
			final File source=new File(PDFWriter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (source.toString().toLowerCase().endsWith(".jar")) return new File(source.getParent());
		} catch (URISyntaxException e1) {}
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Beginnt eine neue Seite im Dokument.
	 */
	protected void newPage() {
		if (!systemOK) return;
		donePage();

		PDPage page=new PDPage(PDRectangle.A4);
		doc.addPage(page);

		pageHeightPT=Math.round(page.getMediaBox().getUpperRightY());
		pageWidthPT=Math.round(page.getMediaBox().getUpperRightX());
		positionY=pageHeightPT-borderTopBottomPT;

		try {contentStream=new PDPageContentStream(doc,page);} catch (IOException e) {contentStream=null; /* Für FindBugs. */}
	}

	private void donePage() {
		if (contentStream!=null) {
			try {contentStream.close();} catch (IOException e) {contentStream=null; /* Für FindBugs. */}
			contentStream=null;
		}
	}

	/**
	 * Speichert die pdf-Daten in einer Datei.
	 * Nach dem Aufruf von save darf das Objekt nicht weiter verwendet werden.
	 * @param file	Dateiname, in der die pdf-Daten gespeichert werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	public boolean save(File file) {
		if (!systemOK) return false;
		donePage();

		PDDocumentCatalog cat=doc.getDocumentCatalog();
		PDMetadata metadata=new PDMetadata(doc);
		cat.setMetadata(metadata);

		XMPMetadata xmp=XMPMetadata.createXMPMetadata();
		try {
			PDFAIdentificationSchema pdfaid=xmp.createAndAddPFAIdentificationSchema();
			pdfaid.setConformance("B");
			pdfaid.setPart(1);
			pdfaid.setAboutAsSimple("");
			XmpSerializer serializer=new XmpSerializer();
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			serializer.serialize(xmp,baos,false);
			metadata.importXMPMetadata(baos.toByteArray());
		} catch(BadFieldValueException | TransformerException | IOException e){return false;}

		try (InputStream colorProfile=PDFWriter.class.getResourceAsStream("res/sRGB Color Space Profile.icm")) {

			PDOutputIntent oi;
			try {oi=new PDOutputIntent(doc,colorProfile);} catch (Exception e) {return false;}
			oi.setInfo("sRGB IEC61966-2.1");
			oi.setOutputCondition("sRGB IEC61966-2.1");
			oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
			oi.setRegistryName("http://www.color.org");
			cat.addOutputIntent(oi);

			try {doc.save(file);} catch (IOException e) {return false;}

			try {doc.close();} catch (IOException e) {}
		} catch (IOException e1) {return false;}
		return true;
	}

	/**
	 * Liefert die Breite einer Zeichenkette in PT.
	 * @param text	Zeichenkette, deren Breite bestimmt werden soll
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @return	Per AWT-Font geschätzte Breite der Zeichenkette
	 */
	protected int getTextWidth(String text, int fontSize, boolean fontBold) {
		if (!systemOK) return 0;
		return (int)Math.round((double)(fontBold?metricsBold:metricsPlain).stringWidth(text)*fontSize/100);
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
	protected String[] splitLine(String[] text, int fontSize, boolean fontBold, int maxWidth, boolean forceWidth) {
		final List<String> output=new ArrayList<>();
		for (int i=0;i<text.length;i++) {
			String[] lines=text[i].split("\n");
			for (int j=0;j<lines.length;j++) {
				String rest=lines[j].trim();
				while (!rest.isEmpty()) {
					String line=rest;
					rest="";
					while (getTextWidth(line,fontSize,fontBold)>maxWidth) {
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
	 * Gibt an, wie viele PT für Buchstabenbereiche unter der Basislinie pro Zeile hinzugefügt werden müssen.
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @return Anzahl an PT, die pro Zeile auf die Schriftgröße aufaddiert werden müssen.
	 */
	protected int getDescent(int fontSize, boolean fontBold) {
		return (int)Math.round((fontBold?metricsBold:metricsPlain).getDescent()/100.0*fontSize);
	}

	/**
	 * Gibt an, ob eine weitere Zeile in einer bestimmten Schriftgröße auf die Seite passt (unter Einhaltung der angegebenen Ränder).
	 * @param fontSize	Schriftgröße der nächsten Zeile
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lines	Gibt an, wie viele Zeilen noch auf die Seite passen sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Zeile nicht mehr auf die Seite passt, also eine neue Seite benötigt wird.
	 */
	protected boolean newPageNeeded(int fontSize, boolean fontBold, int lines) {
		if (!systemOK) return false;
		return positionY<=borderTopBottomPT+(fontSize+getDescent(fontSize,fontBold))*lines;
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
	protected boolean writeTextObject(String text, int fontSize, boolean fontBold,int lineSeparation, int additionalIndent, Color textColor) {
		if (!systemOK) return false;

		try {
			contentStream.beginText();
			contentStream.setFont(fontBold?PDType1Font.HELVETICA_BOLD:PDType1Font.HELVETICA,fontSize);
			contentStream.newLineAtOffset(borderLeftRightPT+additionalIndent,positionY-fontSize);
			if (textColor!=null) contentStream.setNonStrokingColor(textColor);
			contentStream.showText(text);
			contentStream.endText();
			contentStream.saveGraphicsState();
		} catch (IOException e) {return false;}

		positionY-=(fontSize+getDescent(fontSize,fontBold)+lineSeparation);

		return true;
	}

	/**
	 * Gibt mehrere einfache Textobjekte (ohne Zeilenumbrüche und ohne Berücksichtigung der Ränder) untereinander aus.
	 * Nötigenfalls werden Seitenumbrüche eingefügt.
	 * @param lines	Auszugebendee Zeilen
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Zugehörige Anzahl an PT, um die die Y-Position nach der letzten Zeile verschoben werden soll
	 * @param additionalIndent	Gibt an, um wie viele PT des Textobjekt zusätzlich von links eingerückt werden soll
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn die Text-Objekte erfolgreich in die pdf eingefügt werden konnten
	 */
	protected boolean writeTextObjects(String[] lines, int fontSize, boolean fontBold, int lineSeparation, int additionalIndent, Color textColor) {
		for (int i=0;i<lines.length;i++) {
			if (newPageNeeded(fontSize,fontBold,1)) newPage();
			if (!writeTextObject(lines[i],fontSize,fontBold,(i==lines.length-1)?lineSeparation:0,additionalIndent,textColor)) return false;
		}
		return true;
	}

	/**
	 * Zeichnet eine Linie
	 * @param x1	Start-X-Koordinate (in PT)
	 * @param y1	Start-Y-Koordinate (in PT)
	 * @param x2	End-X-Koordinate (in PT)
	 * @param y2	End-Y-Koordinate (in PT)
	 * @param lineWidthMM	Linienbreite (in MM)
	 * @param color	Farbe der Linie
	 * @return	Gibt <code>true</code> zurück, wenn die Linie erfolgreich in die pdf eingefügt werden konnte
	 */
	protected boolean drawLine(float x1, float y1, float x2, float y2, float lineWidthMM, Color color) {
		if (!systemOK) return false;

		try {
			contentStream.setLineWidth((float) (lineWidthMM/25.4*72));
			contentStream.setNonStrokingColor(color);
			contentStream.setStrokingColor(color);
			contentStream.moveTo(x1,y1);
			contentStream.lineTo(x2,y2);
			contentStream.stroke();
		} catch (IOException e) {return false;}

		return true;
	}

	/**
	 * Fügt etwas Abstand unter der aktuellen Ausgabe ein.
	 * @param lineSeparation	Einzufügender Abstand in PT
	 */
	public void writeEmptySpace(int lineSeparation) {
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
	public boolean writeText(String text, int fontSize, boolean fontBold, int lineSeparation) {
		String[] lines=splitLine(new String[]{text},fontSize,fontBold,pageWidthPT-2*borderLeftRightPT,true);
		return writeTextObjects(lines,fontSize,fontBold,lineSeparation,0,null);
	}

	/**
	 * Gibt eine Zeichenkette aus, die wenn nötig umgebrochen wird.
	 * @param text	Auszugebende Zeichenkette
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Gibt an, wie viele Punkte nach der Textzeile Abstand gehalten werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeText(String[] text, int fontSize, boolean fontBold, int lineSeparation) {
		String[] lines=splitLine(text,fontSize,fontBold,pageWidthPT-2*borderLeftRightPT,true);
		return writeTextObjects(lines,fontSize,fontBold,lineSeparation,0,null);
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
	public boolean writeText(String text, int fontSize, boolean fontBold, int lineSeparation, Color textColor) {
		String[] lines=splitLine(new String[]{text},fontSize,fontBold,pageWidthPT-2*borderLeftRightPT,true);
		return writeTextObjects(lines,fontSize,fontBold,lineSeparation,0,textColor);
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
	public boolean writeText(String[] text, int fontSize, boolean fontBold, int lineSeparation, Color textColor) {
		String[] lines=splitLine(text,fontSize,fontBold,pageWidthPT-2*borderLeftRightPT,true);
		return writeTextObjects(lines,fontSize,fontBold,lineSeparation,0,textColor);
	}

	/**
	 * Gibt eine Zeichenkette aus, die wenn nötig umgebrochen wird.
	 * @param text	Auszugebende Zeichenkette
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Gibt an, wie viele Punkte nach der Textzeile Abstand gehalten werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeText(List<String> text, int fontSize, boolean fontBold, int lineSeparation) {
		for (int i=0;i<text.size();i++) if (!writeText(text.get(i),fontSize,fontBold,(i==text.size()-1)?lineSeparation:0)) return false;
		return true;
	}

	/**
	 * Gibt eine Tabellenzeile aus (inkl. Rahmen um die Zellen).
	 * Die Spaltenbreiten werden dabei gleich verteilt, allerdings ist die erste Spalte doppelt so breit, wie die anderen.
	 * @param text	Zeichenketten für die einzelnen Spalten.
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Gibt an, wie viele Punkte nach der Zeile an zusätzlichem Abstand eingefügt werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeTableLine(String[] text, int fontSize, boolean fontBold, int lineSeparation) {
		int contentAreaWidthPT=pageWidthPT-2*borderLeftRightPT;

		final float lineWidthMM=0.1f;
		final Color lineColor=Color.BLACK;

		final List<String[]> cols=new ArrayList<>();
		int maxLines=0;
		for (int i=0;i<text.length;i++) {
			int colWidth=(int)Math.round((double)contentAreaWidthPT*((i==0)?2:1)/(text.length+1));
			String[] lines=splitLine(new String[]{text[i]},fontSize,fontBold,colWidth,true);
			cols.add(lines);
			maxLines=Math.max(maxLines,lines.length);
		}

		if (newPageNeeded(fontSize,fontBold,maxLines)) newPage();

		int indent=0;
		int storePositionY=positionY;
		int minPositionY=positionY;
		for (int i=0;i<cols.size();i++) {
			int colWidth=(int)Math.round((double)contentAreaWidthPT*((i==0)?2:1)/(text.length+1));
			if (!writeTextObjects(cols.get(i),fontSize,fontBold,lineSeparation,indent,null)) return false;
			if (!drawLine(borderLeftRightPT+indent,storePositionY,borderLeftRightPT+indent,storePositionY-maxLines*(fontSize+getDescent(fontSize,fontBold)),lineWidthMM,lineColor)) return false;
			indent+=colWidth;
			if (i==cols.size()-1) {
				if (!drawLine(borderLeftRightPT+indent,storePositionY,borderLeftRightPT+indent,storePositionY-maxLines*(fontSize+getDescent(fontSize,fontBold)),lineWidthMM,lineColor)) return false;
			}
			minPositionY=Math.min(minPositionY,positionY);
			if (i<cols.size()-1) positionY=storePositionY; else positionY=minPositionY;
		}
		if (!drawLine(borderLeftRightPT,storePositionY,borderLeftRightPT+indent,storePositionY,lineWidthMM,lineColor)) return false;
		if (!drawLine(borderLeftRightPT,storePositionY-maxLines*(fontSize+getDescent(fontSize,fontBold)),borderLeftRightPT+indent,storePositionY-maxLines*(fontSize+getDescent(fontSize,fontBold)),lineWidthMM,lineColor)) return false;

		return true;
	}

	/**
	 * Gibt eine Tabellenzeile aus (inkl. Rahmen um die Zellen).
	 * Die Spaltenbreiten werden dabei gleich verteilt, allerdings ist die erste Spalte doppelt so breit, wie die anderen.
	 * @param text	Zeichenketten für die einzelnen Spalten.
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param lineSeparation	Gibt an, wie viele Punkte nach der Zeile an zusätzlichem Abstand eingefügt werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich in die pdf aufgenommen werden konnten.
	 */
	public boolean writeTableLine(List<String> text, int fontSize, boolean fontBold, int lineSeparation) {
		return writeTableLine(text.toArray(new String[0]),fontSize,fontBold,lineSeparation);
	}

	/**
	 * Schreibt das angegebene Bild in die pdf-Datei
	 * @param image	Einzufügendes Bild
	 * @param lineSeparation	Abstand in PT, der unter dem Bild eingefügt werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich in die pdf eingefügt werden konnte
	 */
	public boolean writeImage(BufferedImage image, int lineSeparation) {
		if (!systemOK) return false;

		int contentWidth=(pageWidthPT-2*borderLeftRightPT);
		float neededHeight=((float)image.getHeight())/image.getWidth()*contentWidth;
		try {
			if (positionY<=borderTopBottomPT+neededHeight) newPage();
			final PDImageXObject img=JPEGFactory.createFromImage(doc,image);
			contentStream.drawImage(img,borderLeftRightPT,positionY-neededHeight,contentWidth,neededHeight);
		} catch (IOException e) {return false;}
		positionY-=(neededHeight+lineSeparation);

		return true;
	}

	/**
	 * Funktion zum Testen der PDF-Ausgabe
	 * @param owner	Übergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird.
	 * @param inputImage	Bilddatei, die in die pdf eingebettet wird.
	 * @param outputPDF	Ausgabe-pdf-Datei.
	 * @return	Gibt <code>true</code> zurück, wenn die pdf erfolgreich erstellt werden konnte.
	 */
	public static boolean example(Component owner, File inputImage, File outputPDF) {
		PDFWriter pdf=new PDFWriter(owner,10,15);
		if (!pdf.systemOK) return false;

		/* Text */
		if (!pdf.writeText("Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. Dies ist ein ganz, ganz, ganz langer Text. ",12,false,2)) return false;
		if (!pdf.writeText("Zeile 2",12,true,2)) return false;
		for (int i=1;i<100;i++) if (!pdf.writeText("Test "+i,12,false,0)) return false;

		/* Tabelle */
		if (!pdf.writeTableLine(new String[]{"Überschrift Spalte 1","Überschrift Spalte 2","Überschrift Spalte 3","Dies ist eine lange Überschrift für Spalte 4"},12,true,0)) return false;
		if (!pdf.writeTableLine(new String[]{"Spalte 1","0123456789012345678901234567890","Spalte 3","Spalte 4"},12,false,10)) return false;
		if (!pdf.writeText("Text unter der Tabelle",12,false,2)) return false;

		/* Bilder */
		try {
			BufferedImage image=ImageIO.read(inputImage);
			if (!pdf.writeImage(image,10)) return false;
		} catch (IOException e) {return false;}

		return pdf.save(outputPDF);
	}
}