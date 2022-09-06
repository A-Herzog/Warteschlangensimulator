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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mathtools.NumberTools;
import systemtools.ImageTools;

/**
 * Ausgabe-Stil-Konfiguration für die pdf- und docx-Ausgabe *
 * @author Alexander Herzog
 * @see PDFWriter
 * @see DOCXWriter
 */
public class ReportStyle {
	/**
	 * Schriftfamilie für ein Textobjekt
	 */
	public enum ReportFontFamily {
		/** Serifenbehaftete Schrift */
		SERIF("Serif"),
		/** Schrit ohne Serifen */
		SANS_SERIF("SansSerif"),
		/** Schreibmaschinenschrift */
		TYPEWRITER("Typewriter");

		/**
		 * Name des Elements (z.B. zum Speichern in Konfigurationsdateien)
		 */
		public final String name;

		/**
		 * Konstruktor der Enum
		 * @param name	Name des Elements
		 */
		ReportFontFamily(final String name) {
			this.name=name;
		}

		/**
		 * Liefert ein Element mit einem bestimmten Namen
		 * @param name	Name für den das passende Element gesucht werden soll
		 * @return	Element mit dem angegebenen Namen oder {@link ReportFontFamily#SANS_SERIF} als Fallback
		 */
		public static ReportFontFamily fromName(final String name) {
			for (ReportFontFamily reportFontFamily: values()) if (reportFontFamily.name.equalsIgnoreCase(name)) return reportFontFamily;
			return SANS_SERIF;
		}
	}

	/**
	 * Horizontale Position des Logos oder einer Textzeile
	 */
	public enum ReportPosition {
		/** Logo linksbündig positionieren */
		LEFT,
		/** Logo zentriert positionieren */
		CENTER,
		/** Logo rechtsbündig positionieren */
		RIGHT
	}

	/**
	 * Schriftart (bestehend aus Name, Größe usw.)
	 */
	public static class ReportFont {
		/**
		 * Schriftart
		 */
		public ReportFontFamily family;

		/**
		 * Schriftgröße (in pt)
		 */
		public int size;

		/**
		 * Soll der Text fett dargestellt werden?
		 */
		public boolean bold;

		/**
		 * Optionaler zusätzlicher Abstand unter der Zeile (in pt)
		 */
		public int lineSeparation;

		/**
		 * Konstruktor der Klasse
		 */
		public ReportFont() {
			family=ReportFontFamily.SANS_SERIF;
			size=11;
			bold=false;
			lineSeparation=0;
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param copySource	Zu kopierende Schriftart
		 */
		public ReportFont(final ReportFont copySource) {
			this();
			if (copySource==null) return;
			family=copySource.family;
			size=copySource.size;
			bold=copySource.bold;
			lineSeparation=copySource.lineSeparation;
		}

		/**
		 * Konstruktor der Klasse
		 * @param family	Schriftart
		 * @param size	Schriftgröße (in pt)
		 * @param bold	Soll der Text fett dargestellt werden?
		 * @param lineSeparation	Optionaler zusätzlicher Abstand unter der Zeile (in pt)
		 */
		public ReportFont(final ReportFontFamily family, final int size, final boolean bold, final int lineSeparation) {
			this.family=family;
			this.size=size;
			this.bold=bold;
			this.lineSeparation=lineSeparation;
		}

		/**
		 * Konstruktor der Klasse
		 * @param size	Schriftgröße (in pt)
		 * @param bold	Soll der Text fett dargestellt werden?
		 * @param lineSeparation	Optionaler zusätzlicher Abstand unter der Zeile (in pt)
		 */
		public ReportFont(final int size, final boolean bold, final int lineSeparation) {
			this.family=ReportFontFamily.SANS_SERIF;
			this.size=size;
			this.bold=bold;
			this.lineSeparation=lineSeparation;
		}

		/**
		 * Konstruktor der Klasse
		 * @param size	Schriftgröße (in pt)
		 * @param bold	Soll der Text fett dargestellt werden?
		 */
		public ReportFont(final int size, final boolean bold) {
			this.family=ReportFontFamily.SANS_SERIF;
			this.size=size;
			this.bold=bold;
			this.lineSeparation=0;
		}

		/**
		 * Vergleicht zwei Schriftart-Objekte
		 * @param otherFont	Zweites Schriftart-Objekt
		 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
		 */
		public boolean equalsReportFont(final ReportFont otherFont) {
			if (otherFont==null) return false;
			if (family!=otherFont.family) return false;
			if (size!=otherFont.size) return false;
			if (bold!=otherFont.bold) return false;
			if (lineSeparation!=otherFont.lineSeparation) return false;
			return true;
		}

		/**
		 * Erzeugt einen Unterknoten mit dem angegeben
		 * Namen und speichert die Einstellungen darin.
		 * @param parent	Übergeordneter XML-Knoten unter dem der neue Knoten angelegt werden soll
		 * @param name	Name für den neuen Unterknoten
		 * @see #load(Element)
		 */
		public void save(final Element parent, final String name) {
			final Document doc=parent.getOwnerDocument();
			final Element node=doc.createElement(name);
			parent.appendChild(node);

			node.setAttribute("Name",family.name);
			node.setAttribute("Size",""+size);
			node.setAttribute("Bold",bold?"1":"0");
			node.setAttribute("LineSeparation",""+lineSeparation);
		}

		/**
		 * Lädt die Einstellungen aus einem XML-Knoten.
		 * @param node	XML-Knoten aus dem die Einstellungen geladen werden sollen
		 * @see #save(Element)
		 */
		public void load(final Element node) {
			family=ReportFontFamily.fromName(node.getAttribute("Name"));
			final Long L=NumberTools.getPositiveLong(node.getAttribute("Size"));
			if (L!=null) size=L.intValue();
			bold=node.getAttribute("Bold").equals("1");
			final Integer I=NumberTools.getNotNegativeInteger(node.getAttribute("LineSeparation"));
			if (I!=null) lineSeparation=I;
		}
	}

	/*
	 * Seitenränder
	 */

	/**
	 * Oberer Seitenrand in mm
	 */
	public int borderTopMM;

	/**
	 * Rechter Seitenrand in mm
	 */
	public int borderRightMM;

	/**
	 * Unterer Seitenrand in mm
	 */
	public int borderBottomMM;

	/**
	 * Linker Seitenrand in mm
	 */
	public int borderLeftMM;

	/*
	 * Fußzeile
	 */

	/**
	 * Ausgabe der Seitennummer in der Fußzeile?
	 */
	public boolean footerPageNumbers;

	/**
	 * Ausgabe des aktuellen Datums in der Fußzeile?
	 */
	public boolean footerDate;

	/*
	 * Schriftarten
	 */

	/**
	 * Schriftarten für die Überschriften (3 Ebenen)
	 */
	public final ReportFont[] headingFont;

	/**
	 * Schriftart für normalen Text
	 */
	public ReportFont textFont;

	/**
	 * Schriftart für die Tabellenkopfzeile
	 */
	public ReportFont tableHeadingFont;

	/**
	 * Schriftart für den Tabelleninhalt (abzüglich der letzten Zeile)
	 */
	public ReportFont tableTextFont;

	/**
	 * Schriftart für die letzte Zeile des Tabelleninhalts
	 */
	public ReportFont tableLastLineTextFont;

	/**
	 * Schriftart für die Fußzeile
	 */
	public ReportFont footerFont;

	/**
	 * Absatz-Abstand
	 */
	public int parSkip;

	/*
	 * Logo in der Kopfzeile
	 */

	/**
	 * In der Kopfzeile anzuzeigendes Logo<br>
	 * (kann <code>null</code> sein, wenn kein Logo ausgegeben werden soll)
	 */
	public BufferedImage logo;

	/**
	 * Horizontale Position des Logos in der Kopfzeile
	 */
	public ReportPosition logoPosition;

	/**
	 * Maximalbreite des Logos in MM
	 */
	public int logoMaxWidthMM;

	/**
	 * Maximalhöhe des Logos in MM
	 */
	public int logoMaxHeightMM;

	/**
	 * Soll das Logo auf jeder Seite wiederholt werden?
	 */
	public boolean logoRepeat;

	/**
	 * Konstruktor der Klasse
	 */
	public ReportStyle() {
		borderTopMM=15;
		borderRightMM=10;
		borderBottomMM=15;
		borderLeftMM=10;

		footerPageNumbers=true;
		footerDate=false;

		headingFont=new ReportFont[3];
		headingFont[0]=new ReportFont(18,true,10);
		headingFont[1]=new ReportFont(15,true);
		headingFont[2]=new ReportFont(12,true);
		textFont=new ReportFont(11,false);
		tableHeadingFont=new ReportFont(11,true);
		tableTextFont=new ReportFont(11,false);
		tableLastLineTextFont=new ReportFont(11,false,25);
		footerFont=new ReportFont(8,false);
		parSkip=10;

		logo=null;
		logoPosition=ReportPosition.LEFT;
		logoMaxWidthMM=50;
		logoMaxHeightMM=25;
		logoRepeat=false;
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param copySource	Zu kopierender Ausgangs-Style
	 */
	public ReportStyle(final ReportStyle copySource) {
		this();
		if (copySource==null) return;
		borderTopMM=copySource.borderTopMM;
		borderRightMM=copySource.borderRightMM;
		borderBottomMM=copySource.borderBottomMM;
		borderLeftMM=copySource.borderLeftMM;

		footerPageNumbers=copySource.footerPageNumbers;
		footerDate=copySource.footerDate;

		for (int i=0;i<headingFont.length;i++) headingFont[i]=new ReportFont(copySource.headingFont[i]);
		textFont=new ReportFont(copySource.textFont);
		tableHeadingFont=new ReportFont(copySource.tableHeadingFont);
		tableTextFont=new ReportFont(copySource.tableTextFont);
		tableLastLineTextFont=new ReportFont(copySource.tableLastLineTextFont);
		footerFont=new ReportFont(copySource.footerFont);
		parSkip=copySource.parSkip;
		logo=ImageTools.copyImage(copySource.logo);
		logoPosition=copySource.logoPosition;
		logoMaxWidthMM=copySource.logoMaxWidthMM;
		logoMaxHeightMM=copySource.logoMaxHeightMM;
		logoRepeat=copySource.logoRepeat;
	}

	/**
	 * Vergleicht zwei Style-Konfigurations-Objekte
	 * @param otherSetup	Zweites Style-Konfigurations-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsReportStyleSetup(final ReportStyle otherSetup) {
		if (otherSetup==null) return false;

		if (borderTopMM!=otherSetup.borderTopMM) return false;
		if (borderRightMM!=otherSetup.borderRightMM) return false;
		if (borderBottomMM!=otherSetup.borderBottomMM) return false;
		if (borderLeftMM!=otherSetup.borderLeftMM) return false;

		if (footerPageNumbers!=otherSetup.footerPageNumbers) return false;
		if (footerDate!=otherSetup.footerDate) return false;

		for (int i=0;i<headingFont.length;i++) {
			if (!headingFont[i].equalsReportFont(otherSetup.headingFont[i])) return false;
		}
		if (!textFont.equalsReportFont(otherSetup.textFont)) return false;
		if (!tableHeadingFont.equalsReportFont(otherSetup.tableHeadingFont)) return false;
		if (!tableTextFont.equalsReportFont(otherSetup.tableTextFont)) return false;
		if (!tableLastLineTextFont.equalsReportFont(otherSetup.tableLastLineTextFont)) return false;
		if (!footerFont.equalsReportFont(otherSetup.footerFont)) return false;
		if (parSkip!=otherSetup.parSkip) return false;

		if (logo!=null || otherSetup.logo!=null) {
			if (logo==null || otherSetup.logo==null) return false;
			if (!ImageTools.imageToBase64HTML(logo).equals(ImageTools.imageToBase64HTML(otherSetup.logo))) return false;
		}
		if (logoPosition!=otherSetup.logoPosition) return false;
		if (logoMaxWidthMM!=otherSetup.logoMaxWidthMM) return false;
		if (logoMaxHeightMM!=otherSetup.logoMaxHeightMM) return false;
		if (logoRepeat!=otherSetup.logoRepeat)return false;

		return true;
	}

	/**
	 * Name des XML-Knotens, in dem die Einstellungen gespeichert werden.
	 * @see #load(Element)
	 * @see #save(Element)
	 */
	public static final String XML_NODE_NAME="ReportCustomization";

	/**
	 * Name des XML-Knotens <b>in Kleinbuchstaben</b>, in dem die Einstellungen gespeichert werden.
	 */
	public static final String XML_NODE_NAME_LOWER=XML_NODE_NAME.toLowerCase();

	/**
	 * Name des XML-Knoten für die Seitenränder
	 */
	private static final String XML_BORDERS="ReportBorders";

	/**
	 * XML-Attribut für Seitenrand oben
	 */
	private static final String XML_BORDERS_TOP="Top";

	/**
	 * XML-Attribut für Seitenrand rechts
	 */
	private static final String XML_BORDERS_RIGHT="Right";

	/**
	 * XML-Attribut für Seitenrand unten
	 */
	private static final String XML_BORDERS_BOTTOM="Bottom";

	/**
	 * XML-Attribut für Seitenrand links
	 */
	private static final String XML_BORDERS_LEFT="Left";

	/**
	 * Name des XML-Knoten für die Fußzeile
	 */
	private static final String XML_FOOTER="ReportFooter";

	/**
	 * XML-Attribut für die Seitennummer im der Fußzeile
	 */
	private static final String XML_FOOTER_PAGE_NUMBER="PageNumber";

	/**
	 * XML-Attribut für das Datum im der Fußzeile
	 */
	private static final String XML_FOOTER_DATE="Date";

	/**
	 * Name des XML-Knoten für die Kopfzeile
	 */
	private static final String XML_HEADER="ReportHeader";

	/**
	 * XML-Attribut für die Ausrichtung des Logos in der Kopfzeile
	 */
	private static final String XML_HEADER_ALIGNMENT="Alignment";

	/**
	 * XML-Attribut für die maximale Breite des Logos in der Kopfzeile
	 */
	private static final String XML_HEADER_MAX_WIDTH="MaxWidth";

	/**
	 * XML-Attribut für die maximale Höhe des Logos in der Kopfzeile
	 */
	private static final String XML_HEADER_MAX_HEIGHT="MaxHeight";

	/**
	 * XML-Attribut für die Angabe zur Wiederholung des Logos in der Kopfzeile auf jeder Seite
	 */
	private static final String XML_HEADER_REPEAT="EachPage";

	/**
	 * Name des XML-Knoten für die Schriftarten
	 */
	private static final String XML_FONTS="ReportFonts";

	/**
	 * Name des XML-Knoten für die Schriftarten der Überschriften
	 */
	private static final String XML_FONTS_HEADING="ReportFontsHeading";

	/**
	 * Name des XML-Knoten für die Schriftart von normalem Text
	 */
	private static final String XML_FONTS_TEXT="ReportFontsText";

	/**
	 * Name des XML-Knoten für die Schriftart von Tabellenüberschriften
	 */
	private static final String XML_FONTS_TABLE_HEADING="ReportFontsTableHeading";

	/**
	 * Name des XML-Knoten für die Schriftart von Tabelleninhalten
	 */
	private static final String XML_FONTS_TABLE_TEXT="ReportFontsTableText";

	/**
	 * Name des XML-Knoten für die Schriftart der letzten Zeile von Tabelleninhalten
	 */
	private static final String XML_FONTS_TABLE_TEXT_LAST="ReportFontsTableTextLastLine";

	/**
	 * Name des XML-Knoten für die Schriftart der Fußzeile
	 */
	private static final String XML_FONTS_FOOTER="ReportFontsFooter";

	/**
	 * Name des XML-Knoten für den Absatz-Abstand
	 */
	private static final String XML_PAR_SKIP="ReportParSkip";

	/**
	 * Erzeugt einen Unterknoten mit Namen {@link #XML_NODE_NAME}
	 * und speichert die Einstellungen darin.
	 * @param parent	Übergeordneter XML-Knoten unter dem der neue Knoten angelegt werden soll
	 * @see #XML_NODE_NAME
	 * @see #load(Element)
	 */
	public void save(final Element parent) {
		final Document doc=parent.getOwnerDocument();
		final Element node=doc.createElement(XML_NODE_NAME);
		parent.appendChild(node);

		/* Seitenränder */
		if (borderTopMM!=15 || borderRightMM!=10 || borderBottomMM!=15 || borderLeftMM!=10) {
			final Element sub=doc.createElement(XML_BORDERS);
			node.appendChild(sub);
			sub.setAttribute(XML_BORDERS_TOP,""+borderTopMM);
			sub.setAttribute(XML_BORDERS_RIGHT,""+borderRightMM);
			sub.setAttribute(XML_BORDERS_BOTTOM,""+borderBottomMM);
			sub.setAttribute(XML_BORDERS_LEFT,""+borderLeftMM);
		}

		/* Fußzeile */
		if (!footerPageNumbers || footerDate) {
			final Element sub=doc.createElement(XML_FOOTER);
			node.appendChild(sub);
			sub.setAttribute(XML_FOOTER_PAGE_NUMBER,footerPageNumbers?"1":"0");
			sub.setAttribute(XML_FOOTER_DATE,footerDate?"1":"0");
		}

		/* Schriftarten */
		final Element fontsNode=doc.createElement(XML_FONTS);
		node.appendChild(fontsNode);
		for (int i=0;i<headingFont.length;i++) headingFont[i].save(fontsNode,XML_FONTS_HEADING+(i+1));
		textFont.save(fontsNode,XML_FONTS_TEXT);
		tableHeadingFont.save(fontsNode,XML_FONTS_TABLE_HEADING);
		tableTextFont.save(fontsNode,XML_FONTS_TABLE_TEXT);
		tableLastLineTextFont.save(fontsNode,XML_FONTS_TABLE_TEXT_LAST);
		footerFont.save(fontsNode,XML_FONTS_FOOTER);
		if (parSkip!=10) {
			final Element sub=doc.createElement(XML_PAR_SKIP);
			node.appendChild(sub);
			sub.setTextContent(""+parSkip);
		}

		/* Kopfzeile */
		if (logo!=null || logoPosition!=ReportPosition.LEFT || logoMaxWidthMM!=50 || logoMaxHeightMM!=25 || logoRepeat) {
			final Element sub=doc.createElement(XML_HEADER);
			node.appendChild(sub);
			switch (logoPosition) {
			case LEFT: sub.setAttribute(XML_HEADER_ALIGNMENT,"Left"); break;
			case CENTER: sub.setAttribute(XML_HEADER_ALIGNMENT,"Center"); break;
			case RIGHT: sub.setAttribute(XML_HEADER_ALIGNMENT,"Right"); break;
			}
			sub.setAttribute(XML_HEADER_MAX_WIDTH,""+logoMaxWidthMM);
			sub.setAttribute(XML_HEADER_MAX_HEIGHT,""+logoMaxHeightMM);
			sub.setAttribute(XML_HEADER_REPEAT,logoRepeat?"1":"0");
			if (logo!=null) {
				try {
					final ByteArrayOutputStream stream=new ByteArrayOutputStream();
					ImageIO.write(logo,"png",stream);
					sub.setTextContent(new String(Base64.getEncoder().encode(stream.toByteArray())));
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * Lädt die Einstellungen aus einem XML-Knoten.
	 * @param node	XML-Knoten aus dem die Einstellungen geladen werden sollen (der Knoten muss den Tag-Namen {@link #XML_NODE_NAME} haben)
	 * @see #XML_NODE_NAME
	 * @see #save(Element)
	 */
	public void load(final Element node) {
		Integer I;

		final NodeList l=node.getChildNodes();
		final int count=l.getLength();
		for (int i=0;i<count;i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			final String name=e.getNodeName();

			/* Seitenränder */
			if (name.equalsIgnoreCase(XML_BORDERS)) {
				I=NumberTools.getNotNegativeInteger(e.getAttribute(XML_BORDERS_TOP));
				if (I!=null) borderTopMM=I;
				I=NumberTools.getNotNegativeInteger(e.getAttribute(XML_BORDERS_RIGHT));
				if (I!=null) borderRightMM=I;
				I=NumberTools.getNotNegativeInteger(e.getAttribute(XML_BORDERS_BOTTOM));
				if (I!=null) borderBottomMM=I;
				I=NumberTools.getNotNegativeInteger(e.getAttribute(XML_BORDERS_LEFT));
				if (I!=null) borderLeftMM=I;
				continue;
			}

			/* Fußzeile */
			if (name.equalsIgnoreCase(XML_FOOTER)) {
				String attr;
				attr=e.getAttribute(XML_FOOTER_PAGE_NUMBER);
				footerPageNumbers=!(attr.isEmpty() || attr.equals("0"));
				attr=e.getAttribute(XML_FOOTER_DATE);
				footerDate=attr.equals("1");
				continue;
			}

			/* Schriftarten */
			if (name.equalsIgnoreCase(XML_FONTS)) {
				final NodeList l2=e.getChildNodes();
				final int count2=l2.getLength();
				for (int i2=0;i2<count2;i2++) {
					if (!(l2.item(i2) instanceof Element)) continue;
					final Element e2=(Element)l2.item(i2);
					final String name2=e2.getNodeName();

					boolean done=false;
					for (int j=0;j<headingFont.length;j++) if (name2.equalsIgnoreCase(XML_FONTS_HEADING+(j+1))) {
						headingFont[j].load(e2);
						done=true;
						break;
					}
					if (done) continue;

					if (name2.equalsIgnoreCase(XML_FONTS_TEXT)) {
						textFont.load(e2);
						continue;
					}

					if (name2.equalsIgnoreCase(XML_FONTS_TABLE_HEADING)) {
						tableHeadingFont.load(e2);
						continue;
					}

					if (name2.equalsIgnoreCase(XML_FONTS_TABLE_TEXT)) {
						tableTextFont.load(e2);
						continue;
					}

					if (name2.equalsIgnoreCase(XML_FONTS_TABLE_TEXT_LAST)) {
						tableLastLineTextFont.load(e2);
						continue;
					}

					if (name2.equalsIgnoreCase(XML_FONTS_FOOTER)) {
						footerFont.load(e2);
						continue;
					}
				}
				continue;
			}
			if (name.equalsIgnoreCase(XML_PAR_SKIP)) {
				I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I!=null) parSkip=I;
				continue;
			}

			/* Kopfzeile */
			if (name.equalsIgnoreCase(XML_HEADER)) {
				final String alignment=e.getAttribute(XML_HEADER_ALIGNMENT);
				if (alignment.equalsIgnoreCase("Left")) logoPosition=ReportPosition.LEFT;
				if (alignment.equalsIgnoreCase("Center")) logoPosition=ReportPosition.CENTER;
				if (alignment.equalsIgnoreCase("Right")) logoPosition=ReportPosition.RIGHT;
				I=NumberTools.getNotNegativeInteger(e.getAttribute(XML_HEADER_MAX_WIDTH));
				if (I!=null) logoMaxWidthMM=I;
				I=NumberTools.getNotNegativeInteger(e.getAttribute(XML_HEADER_MAX_HEIGHT));
				if (I!=null) logoMaxHeightMM=I;
				logoRepeat=e.getAttribute(XML_HEADER_REPEAT).equals("1");
				final String logoData=e.getTextContent();
				if (!logoData.isEmpty()) {
					try {
						final ByteArrayInputStream stream=new ByteArrayInputStream(Base64.getDecoder().decode(logoData));
						final boolean useCache=ImageIO.getUseCache();
						try {
							ImageIO.setUseCache(false); /* Wird benötigt, wenn im Stream nicht gesprungen werden kann, was bei einem ByteArrayInputStream nun definitiv möglich ist.  */
							final BufferedImage image=ImageIO.read(stream);
							if (image!=null) logo=image;
						} finally {
							ImageIO.setUseCache(useCache);
						}
					} catch (IOException | IllegalArgumentException ex) {}
				}
				continue;
			}
		}
	}
}