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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import mathtools.Table;

/**
 * Klasse zur Erstellung von docx-basierenden Zusammenfassungen.
 * @author Alexander Herzog
 */
public class DOCXWriter {
	/**
	 * Ausgabesystem
	 */
	private final XWPFDocument doc;

	/**
	 * Konfigurationsobjekt
	 */
	private final ReportStyle style;

	/**
	 * Aktueller Absatz (kann <code>null</code> sein, wenn momentan kein Absatz aktiv ist)
	 * @see #beginParagraph()
	 * @see #endParagraph()
	 */
	private XWPFParagraph paragraph;

	/**
	 * Aktuelles Run-Objekt (kann <code>null</code> sein, wenn momentan kein Run-Objekt aktiv ist)
	 * @see #createRun(XWPFParagraph, systemtools.statistics.ReportStyle.ReportFont)
	 * @see #createRun(systemtools.statistics.ReportStyle.ReportFont)
	 */
	private XWPFRun run;

	/**
	 * Konstruktor der Klasse
	 * @param doc	Ausgabesystem an das die Ausgaben durchgereicht werden sollen
	 * @param style	Konfigurationsobjekt
	 */
	public DOCXWriter(final XWPFDocument doc, final ReportStyle style) {
		this.doc=doc;
		this.style=style;

		/* Seitenränder einstellen */
		final CTSectPr sectPr=doc.getDocument().getBody().addNewSectPr();
		final CTPageMar pageMar=sectPr.addNewPgMar();
		pageMar.setLeft(BigInteger.valueOf(Math.round(style.borderLeftMM/2.54*72*2)));
		pageMar.setTop(BigInteger.valueOf(Math.round(style.borderTopMM/2.54*72*2)));
		pageMar.setRight(BigInteger.valueOf(Math.round(style.borderRightMM/2.54*72*2)));
		pageMar.setBottom(BigInteger.valueOf(Math.round(style.borderBottomMM/2.54*72*2)));
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Erstellt einen docx-Writer, der die Standardkonfiguration verwendet.
	 * Diese Variante sollte nur verwendet werden, wenn einzelne Objekte direkt ausgegeben
	 * werden sollen. Für Reports sollte der Konstruktor, der ein Konfigurationsobjekt
	 * verwendet, genutzt werden.
	 * @param doc	Ausgabesystem an das die Ausgaben durchgereicht werden sollen
	 */
	public DOCXWriter(final XWPFDocument doc) {
		this(doc,new ReportStyle());
	}

	/**
	 * Beginnt einen neuen Absatz.
	 * @see #endParagraph()
	 */
	public void beginParagraph() {
		paragraph=doc.createParagraph();
		run=null;
	}

	/**
	 * Beendet den aktuellen Absatz.
	 * @see #beginParagraph()
	 */
	public void endParagraph() {
		paragraph=null;
		run=null;
	}

	/**
	 * Erzeugt ein Run-Objekt in einem Absatz
	 * @param paragraph	Absatz, in dem das Run-Objekt erzeugt werden soll
	 * @param font	Zu verwendende Schriftart
	 * @return	Neues Run-Objekt
	 */
	private XWPFRun createRun(final XWPFParagraph paragraph, final ReportStyle.ReportFont font) {
		final XWPFRun r=paragraph.createRun();
		r.setFontSize(font.size);
		r.setBold(font.bold);
		switch (font.family) {
		case SANS_SERIF: /* Ist schon eingestellt */ break;
		case SERIF: r.setFontFamily("Times New Roman"); break;
		case TYPEWRITER: r.setFontFamily("Courier New"); break;
		}
		run=r;
		return r;
	}

	/**
	 * Erzeugt ein Run-Objekt im aktuellen Absatz
	 * @param font	Zu verwendende Schriftart
	 * @return	Neues Run-Objekt
	 */
	private XWPFRun createRun(final ReportStyle.ReportFont font) {
		if (paragraph==null) beginParagraph();
		return createRun(paragraph,font);
	}

	/**
	 * Gibt eine Überschrift unter Verwendung der Style-Konfiguration aus.
	 * @param text	Auszugebender Text
	 * @param level	Level der Überschrift (1-3)
	 */
	public void writeHeading(final String text, final int level) {
		beginParagraph();
		final ReportStyle.ReportFont font=style.headingFont[Math.max(1,Math.min(level,style.headingFont.length))-1];
		final XWPFRun r=createRun(font);
		r.setText(text);
		endParagraph();
	}

	/**
	 * Gibt einen Text unter Verwendung der Style-Konfiguration aus.
	 * @param text	Auszugebender Text
	 */
	public void writeText(final String text) {
		if (paragraph!=null && run!=null) run.addBreak();
		final XWPFRun r=createRun(style.textFont);
		r.setText(text);
	}

	/**
	 * Gibt ein Bild aus.
	 * @param image	Auszugebendes Bild
	 * @return	Gibt <code>true</code> zurück, wenn das Text-Objekt erfolgreich in das Dokument eingefügt werden konnte
	 */
	public boolean writeImage(final BufferedImage image) {
		endParagraph();

		try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
			try {if (!ImageIO.write(image,"png",streamOut)) return false;} catch (IOException e) {return false;}
			if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_PNG,image.getWidth(),image.getHeight())) return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Gibt eine Tabelle aus.
	 * @param table	Auszugebende Tabelle
	 */
	public void writeTable(Table table) {
		endParagraph();

		if (table.getMode()==Table.IndexMode.COLS) table=table.transpose();
		final List<List<String>> data=table.getData();

		final XWPFTable outputTable=doc.createTable();

		for (int i=0;i<data.size();i++) {
			final XWPFTableRow outputRow;
			if (outputTable.getRows().size()>i) outputRow=outputTable.getRows().get(i); else outputRow=outputTable.createRow();
			final List<String> dataRow=data.get(i);
			ReportStyle.ReportFont font;
			if (i==0) font=style.tableHeadingFont; else {
				if (i==data.size()-1) font=style.tableLastLineTextFont; else font=style.tableTextFont;
			}

			for (int j=0;j<dataRow.size();j++) {
				XWPFTableCell cell;
				if (outputRow.getTableCells().size()>j) cell=outputRow.getTableCells().get(j); else cell=outputRow.addNewTableCell();
				final XWPFRun r=createRun(cell.addParagraph(),font);
				r.setText(dataRow.get(j));
			}
		}
	}
}
