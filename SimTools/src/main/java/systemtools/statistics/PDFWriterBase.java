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
package systemtools.statistics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

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
 * Diese Klasse stellt Low-Level-Methoden zur Erzeugung von pdf-Dokumenten zur Verfügung.
 * @author Alexander Herzog
 */
public class PDFWriterBase {
	/**
	 * PDF-Dokument
	 */
	private final PDDocument doc;

	/**
	 * Ausgabestrem für die aktuelle Seite
	 * @see #newPage()
	 */
	private PDPageContentStream contentStream;

	/**
	 * Ordner in dem das PDF-System einen Font-Cache-Datei ablegt.
	 */
	public static File cacheFolder=getProgramFolder();

	/**
	 * Gibt an, ob das System korrekt initialisiert werden konnte (wird vom Konstruktor gesetzt).
	 * Wenn <code>systemOK</code> auf <code>false</code> steht, sind keine weiteren Bearbeitungen möglich.
	 */
	protected boolean systemOK;

	/**
	 * Liefert die Höhe der aktuellen Seiten in PT.<br>
	 * (Wird beim ersten Aufruf von {@link #newPage()} gesetzt.)
	 */
	protected int pageHeightPT;

	/**
	 * Liefert die Breite der aktuellen Seiten in PT.<br>
	 * (Wird beim ersten Aufruf von {@link #newPage()} gesetzt.)
	 */
	protected int pageWidthPT;

	/**
	 * Konstruktor der Klasse
	 */
	public PDFWriterBase() {
		/* Verzeichnis für ".pdfbox.cache"-Datei - lässt sich leider nicht komplett abschalten. */
		System.setProperty("pdfbox.fontcache",cacheFolder.toString());

		/* Dokument erstellen */
		doc=new PDDocument();

		systemOK=true;
	}

	/**
	 * Liefert wenn möglich das Programmverzeichnis (in dem sich die jar-Datei befindet), sonst
	 * das Nutzerverzeichnis.<br>
	 * (Um einen Speicherort für die pdf-Font-Cache festzulegen.)
	 * @return	Programmverzeichnis oder als Fallback das Nutzerverzeichnis
	 */
	private static File getProgramFolder() {
		try {
			final File source=new File(PDFWriter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (source.toString().toLowerCase().endsWith(".jar")) return new File(source.getParent());
		} catch (URISyntaxException e1) {}
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Gibt an, ob das System korrekt initialisiert werden konnte (wird vom Konstruktor gesetzt).
	 * @return	System einsatzbereit?
	 */
	public boolean isSystemOk() {
		return systemOK;
	}

	/**
	 * Beginnt eine neue Seite im Dokument.
	 * @return	Liefert <code>true</code>, wenn eine neue Seite angelegt werden konnte
	 */
	protected boolean newPage() {
		if (!systemOK) return false;
		donePage();

		PDPage page=new PDPage(PDRectangle.A4);
		doc.addPage(page);

		if (pageHeightPT==0) {
			final PDRectangle rect=page.getMediaBox();
			pageHeightPT=Math.round(rect.getUpperRightY());
			pageWidthPT=Math.round(rect.getUpperRightX());
		}

		try {contentStream=new PDPageContentStream(doc,page);} catch (IOException e) {
			contentStream=null; /* Für FindBugs. */
			return false;
		}

		return true;
	}

	/**
	 * Beendet die aktuelle Seite.
	 * Wird implizit von {@link #newPage()} und {@link #save(File)}
	 * automatisch aufgerufen.
	 * @see #newPage()
	 * @see #save(File)
	 */
	private void donePage() {
		if (contentStream!=null) {
			outputPageFooter();
			try {contentStream.close();} catch (IOException e) {contentStream=null; /* Für FindBugs. */}
			contentStream=null;
		}
	}

	/**
	 * Gibt eine optionale Fußzeile aus, bevor zur nächsten Seite gewechselt wird.
	 */
	protected void outputPageFooter() {
	}

	/**
	 * Speichert die pdf-Daten in einer Datei.
	 * Nach dem Aufruf von save darf das Objekt nicht weiter verwendet werden.
	 * @param file	Dateiname, in der die pdf-Daten gespeichert werden sollen.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	public final boolean save(File file) {
		if (!systemOK) return false;
		donePage();

		PDDocumentCatalog cat=doc.getDocumentCatalog();
		PDMetadata metadata=new PDMetadata(doc);
		cat.setMetadata(metadata);

		XMPMetadata xmp=XMPMetadata.createXMPMetadata();
		try {
			PDFAIdentificationSchema pdfaid=xmp.createAndAddPDFAIdentificationSchema();
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
	 * Gibt ein einfaches Textobjekt aus.
	 * @param text	Auszugebender Text
	 * @param fontSize	Schriftgröße in PT
	 * @param fontFamily	Schriftfamilie
	 * @param x	x-Koordinate für das Textobjekt
	 * @param y	y-Koordinate für das Textobjekt
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	protected final boolean writeTextObject(final String text, final int fontSize, final PDType1Font fontFamily, final int x, final int y, final Color textColor) {
		if (!systemOK) return false;

		try {
			contentStream.beginText();
			contentStream.setFont(fontFamily,fontSize);
			contentStream.newLineAtOffset(x,y-fontSize);
			if (textColor!=null) contentStream.setNonStrokingColor(textColor);
			contentStream.showText(text);
			contentStream.endText();
			contentStream.saveGraphicsState();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Gibt ein einfaches Textobjekt aus.
	 * @param text	Auszugebender Text
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param x	x-Koordinate für das Textobjekt
	 * @param y	y-Koordinate für das Textobjekt
	 * @param textColor	Gibt optional (d.h. kann auch <code>null</code> sein) eine Textfarbe an
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	protected final boolean writeTextObject(final String text, final int fontSize, final boolean fontBold, final int x, final int y, final Color textColor) {
		return writeTextObject(text,fontSize,fontBold?PDType1Font.HELVETICA_BOLD:PDType1Font.HELVETICA,x,y,textColor);
	}

	/**
	 * Gibt ein einfaches Textobjekt aus.
	 * @param text	Auszugebender Text
	 * @param fontSize	Schriftgröße in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @param x	x-Koordinate für das Textobjekt
	 * @param y	y-Koordinate für das Textobjekt
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	protected final boolean writeTextObject(final String text, final int fontSize, final boolean fontBold, final int x, final int y) {
		return writeTextObject(text,fontSize,fontBold,x,y,null);
	}

	/**
	 * Gibt ein einfaches Textobjekt aus.
	 * @param text	Auszugebender Text
	 * @param fontSize	Schriftgröße in PT
	 * @param fontFamily	Schriftfamilie
	 * @param x	x-Koordinate für das Textobjekt
	 * @param y	y-Koordinate für das Textobjekt
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in die pdf eingefügt werden konnte
	 */
	protected final boolean writeTextObject(final String text, final int fontSize, final PDType1Font fontFamily, final int x, final int y) {
		return writeTextObject(text,fontSize,fontFamily,x,y,null);
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
	protected final boolean drawLine(final float x1, final float y1, final float x2, final float y2, final float lineWidthMM, final Color color) {
		if (!systemOK) return false;

		try {
			contentStream.setLineWidth((float) (lineWidthMM/25.4*72));
			contentStream.setNonStrokingColor(color);
			contentStream.setStrokingColor(color);
			contentStream.moveTo(x1,y1);
			contentStream.lineTo(x2,y2);
			contentStream.stroke();
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Schreibt das angegebene Bild in die pdf-Datei
	 * @param image	Einzufügendes Bild
	 * @param x	x-Koordinate für die Grafik
	 * @param y	y-Koordinate für die Grafik
	 * @param width	Breite der Grafik
	 * @param height	Höhe der Grafik
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich in die pdf eingefügt werden konnte
	 */
	protected final boolean writeImageObject(final BufferedImage image, final float x, final float y, final float width, final float height) {
		if (!systemOK) return false;

		try {
			PDImageXObject img;
			try {
				img=JPEGFactory.createFromImage(doc,image);
			} catch (UnsupportedOperationException e) {
				/* createFromImage hat mit Transparenz in geladenen png-Dateien Probleme; wenn nötig daher hier in ein einfacheres Format umwandeln */
				final BufferedImage bufferedImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_ARGB);
				final Graphics2D graphics=bufferedImage.createGraphics();
				graphics.drawImage(image,0,0,null);
				graphics.dispose();
				img=JPEGFactory.createFromImage(doc,bufferedImage);
			}
			contentStream.drawImage(img,x,y,width,height);
		} catch (IOException e) {
			return false;
		}

		return true;
	}
}
