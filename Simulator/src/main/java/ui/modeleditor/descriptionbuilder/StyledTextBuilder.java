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
package ui.modeleditor.descriptionbuilder;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.text.Paragraph;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;
import systemtools.statistics.PDFWriter;
import systemtools.statistics.XWPFDocumentPictureTools;

/**
 * Diese Klasse sammelt Textzeilen und Überschriften und erlaubt dann
 * die formatierte und unformatierte Ausgabe in diversen Formaten.
 * @author Alexander Herzog
 */
public class StyledTextBuilder {
	private final List<String> lines;
	private final List<Integer> lineTypes; /* 0=Text, 1,2,3,...=Überschriften, -1=Absatzbeginn, -2=Absatzende */

	private static final String HTML_HEADER=
			"<!DOCTYPE html>\n"+
					"<html>\n"+
					"<head>\n"+
					"  <meta charset=\"utf-8\">\n"+
					"  <meta name=\"author\" content=\"Alexander Herzog\">\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n\n";
	private static final String HTML_FOOTER="\n</body></html>";

	/**
	 * Konstruktor der Klasse
	 */
	public StyledTextBuilder() {
		lines=new ArrayList<>();
		lineTypes=new ArrayList<>();
	}

	/**
	 * Fügt eine Überschriftzeile an den Text an
	 * @param level	Art der Überschrift (zulässige Werte sind 1 oder 2)
	 * @param heading	Überschriftzeile
	 */
	public void addHeading(final int level, final String heading) {
		lines.add(heading);
		lineTypes.add(level);
	}

	/**
	 * Fügt eine Textzeile an den Text an
	 * @param line	Textzeile
	 */
	public void addLine(final String line) {
		lines.add(line);
		lineTypes.add(0);
	}


	/**
	 * Fügt mehrere Textzeilen an den Text an
	 * @param lines	Textzeilen
	 */
	public void addLines(final String[] lines) {
		if (lines!=null) for (String line: lines) addLine(line);
	}

	/**
	 * Fügt mehrere Textzeilen an den Text an
	 * @param lines	Textzeilen
	 */
	public void addLines(final List<String> lines) {
		if (lines!=null) for (String line: lines) addLine(line);
	}

	/**
	 * Fügt eine oder mehrere Textzeilen an den Text an
	 * @param lines	Textzeilen; Zeilenumbrüche werden hier erkannt und die Teiltexte dann als einzelne Zeilen behandelt
	 */
	public void addLines(final String lines) {
		for (String line: lines.split("\\n")) addLine(line);
	}

	/**
	 * Startet einen neuen Absatz der seinerseits eine oder mehrere Textzeilen enthalten kann.
	 * @see #endParagraph()
	 */
	public void beginParagraph() {
		lines.add("");
		lineTypes.add((-1));
	}

	/**
	 * Beendet den aktuellen Absatz.
	 * @see #beginParagraph()
	 */
	public void endParagraph() {
		lines.add("");
		lineTypes.add((-2));
	}

	/**
	 * Schreibt den Text in ein {@link JTextPane}-Element
	 * @param textPane	{@link JTextPane}-Element in das der Text geschrieben werden soll
	 */
	public void writeToTextPane(final JTextPane textPane) {
		/* Styles zusammenstellen */
		StyledDocument doc=textPane.getStyledDocument();

		Style defaultStyle=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style s;

		s=doc.addStyle("default",defaultStyle);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+1);

		s=doc.addStyle("h1",defaultStyle);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+4);
		StyleConstants.setBold(s,true);

		s=doc.addStyle("h2",defaultStyle);
		StyleConstants.setBold(s,true);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+2);

		s=doc.addStyle("h3",defaultStyle);
		StyleConstants.setFontSize(s,StyleConstants.getFontSize(s)+1);
		StyleConstants.setUnderline(s,true);

		/* Text einfügen */
		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				try {doc.insertString(doc.getLength(),"\n",doc.getStyle("default"));} catch (BadLocationException e) {}
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				try {doc.insertString(doc.getLength(),line+"\n",doc.getStyle("default"));} catch (BadLocationException e) {}
				continue;
			}
			if (type>0) {
				/* Überschriften */
				try {
					if (i>0 && lineTypes.get(i-1)!=-2) doc.insertString(doc.getLength(),"\n",doc.getStyle("default"));
					doc.insertString(doc.getLength(),line+"\n",doc.getStyle("h"+type));
				} catch (BadLocationException e) {}
				continue;
			}
		}
	}


	/** Wandelt eine Zahl zwischen 0 und 15 in ein Hexadezimal-Zeichen um.
	 * @param b	Umzuwandelnde Zahl
	 * @return	Hexadezimal-Zeichen
	 */
	private char hex(final int b) {
		if (b<10) return (char)(b+((short)'0')); else return (char)(b-10+((short)'a'));
	}

	private String convertLineToRTF(final String line) {
		final StringBuilder result=new StringBuilder();
		for (int i=0;i<line.length();i++) {
			char c=line.charAt(i);
			if ((short)c<=127) {result.append(c); continue;}
			short b=(short)c;

			result.append("\\'");
			result.append(hex(b/16));
			result.append(hex(b%16));

		}
		return result.toString();
	}

	/**
	 * Liefert den Text ohne Formatierungen
	 * @return	Text ohne Formatierungen
	 */
	public String getText() {
		final StringBuilder result=new StringBuilder();

		boolean lastWasParagraph=false;
		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				result.append("\n");
				lastWasParagraph=true;
				continue;
			}
			if (type==0) {
				result.append(line+"\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (result.length()>0 && !lastWasParagraph) result.append("\n");
				result.append(line);
				result.append("\n\n");
				lastWasParagraph=true;
				continue;
			}
		}

		return result.toString();
	}

	/**
	 * Liefert den Text als RTF-String
	 * @return	Text als RTF-String
	 */
	public String getRTFText() {
		final StringBuilder result=new StringBuilder();

		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				result.append("\\par\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				result.append(convertLineToRTF(line)+"\\line\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				String fs="22";
				switch (type) {
				case 1: fs="34"; break;
				case 2: fs="28"; break;
				}
				if (i>0 && lineTypes.get(i-1)!=-2) result.append("\\par\n");
				result.append("\\fs"+fs+" "+convertLineToRTF(line)+"\\fs22\\par\n");
				continue;
			}
		}

		return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl\\f0\\fswiss Helvetica;}\\f0\n"+result.toString()+"\n}\n";
	}

	/**
	 * Schreibt den Text in ein Word-Dokument
	 * @param doc	Word-Dokument in das der Text geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveDOCX(final XWPFDocument doc) {
		XWPFParagraph p=null;

		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				p=doc.createParagraph();
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				p=null;
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (p==null) p=doc.createParagraph();
				p.createRun().setText(line);
				p.createRun().addBreak();
				continue;
			}
			if (type>0) {
				/* Überschriften */
				p=doc.createParagraph();
				XWPFRun r=p.createRun();
				r.setBold(true);
				int fs=12;
				switch (type) {
				case 1: fs=18; break;
				case 2: fs=15; break;
				}
				r.setFontSize(fs);
				r.setText(line);
				p=null;
				continue;
			}
		}

		return true;
	}

	/**
	 * Schreibt den Text in ein Word-Dokument
	 * @param file	Dateiname unter dem das Word-Dokument gespeichert werden soll
	 * @param additionalImage	Optionales Bild welches am Anfang eingefügt wird (kann <code>null</code> sein)
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveDOCX(final File file, final BufferedImage additionalImage) {
		try(XWPFDocument doc=new XWPFDocument()) {

			if (additionalImage!=null) {
				try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
					try {if (!ImageIO.write(additionalImage,"jpg",streamOut)) return false;} catch (IOException e) {return false;}
					if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_JPEG,additionalImage.getWidth(),additionalImage.getHeight())) return false;
				} catch (IOException e) {return false;}
			}

			if (!saveDOCX(doc)) return false;
			try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);}
			return true;
		} catch (IOException e) {return false;}
	}

	/**
	 * Schreibt den Text in ein odt-Dokument
	 * @param odt	Odt-Dokument in das der Text geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveODT(final TextDocument odt) {
		Paragraph p=null;

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				p=odt.addParagraph(null);
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				p=null;
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (p==null) p=odt.addParagraph(null);
				p.appendTextContent(line);
				p.appendTextContent("\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				p=odt.addParagraph(null);
				int fs=12;
				switch (type) {
				case 1: fs=18; break;
				case 2: fs=15; break;
				}
				p.setFont(new Font("Arial",StyleTypeDefinitions.FontStyle.BOLD,fs));
				p.appendTextContent(line);
				p=null;
				continue;
			}
		}

		return true;
	}

	/**
	 * Schreibt den Text in ein odt-Dokument
	 * @param file	Dateiname unter dem das odt-Dokument gespeichert werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveODT(final File file) {
		try(TextDocument odt=TextDocument.newTextDocument()) {
			if (!saveODT(odt)) return false;
			odt.save(file);
			return true;
		} catch (Exception e) {return false;}
	}

	/**
	 * Schreibt den Text in eine pdf-Datei
	 * @param pdf	pdf-Datei-Objekt in das der Text geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean savePDF(final PDFWriter pdf) {
		boolean newParagraph=true;

		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				newParagraph=true;
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				newParagraph=true;
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (newParagraph) {pdf.writeEmptySpace(10); newParagraph=false;}
				if (!pdf.writeText(line,11,false,0)) return false;
				continue;
			}
			if (type>0) {
				/* Überschriften */
				int fs=12;
				switch (type) {
				case 1: fs=18; break;
				case 2: fs=15; break;
				}
				pdf.writeEmptySpace(20);
				if (!pdf.writeText(line,fs,true,0)) return false;
				newParagraph=true;
				continue;
			}
		}
		pdf.writeEmptySpace(25);

		return true;
	}

	/**
	 * Schreibt den Text in eine pdf-Datei
	 * @param owner	Übergeordnetes Element
	 * @param file	Dateiname unter dem die pdf-Datei gespeichert werden soll
	 * @param additionalImage	Optionales Bild welches am Anfang eingefügt wird (kann <code>null</code> sein)
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean savePDF(final Component owner, final File file, final BufferedImage additionalImage) {
		final PDFWriter pdf=new PDFWriter(owner,15,10);
		if (!pdf.systemOK) return false;
		if (additionalImage!=null) {
			if (!pdf.writeImage(additionalImage,25)) return false;
		}

		if (!savePDF(pdf)) return false;
		return pdf.save(file);
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTML(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Liefert den Text als HTML-Dokument
	 * @return	Text als HTML-Dokument
	 */
	public String getHTMLText() {
		final StringBuilder result=new StringBuilder();

		result.append(HTML_HEADER);
		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				result.append("<p>\n");
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				result.append("</p>\n");
				continue;
			}
			if (type==0) {
				result.append(encodeHTML(line)+"<br>\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				result.append("<h"+type+">");
				result.append(line);
				result.append("</h"+type+">\n");
				continue;
			}
		}
		result.append(HTML_FOOTER);

		return result.toString();
	}

	/**
	 * Liefert den Text als Markdown
	 * @return	Text als Markdown
	 */
	public String getMDText() {
		final StringBuilder result=new StringBuilder();

		boolean lastWasParagraph=false;
		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				result.append("\n");
				lastWasParagraph=true;
				continue;
			}
			if (type==0) {
				result.append(line+"  \n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (result.length()>0 && !lastWasParagraph) result.append("\n");
				for (int j=1;j<=type;j++) result.append('#');
				result.append(' ');
				result.append(line);
				result.append("\n\n");
				lastWasParagraph=true;
				continue;
			}
		}

		return result.toString();
	}

	/**
	 * Fügt den Text in ein Shape-Objekt einer PowerPoint-Folie ein
	 * @param shape	Shape-Objekt in das der Text eingefügt werden soll
	 * @param skipFirstLines	Wie viele Zeilen der Ausgabe (z.B. mit Überschriften) sollen am Anfang übersprungen werden?
	 */
	public void saveSlideShape(final XSLFTextShape shape, final int skipFirstLines) {
		for (int i=skipFirstLines;i<lines.size();i++) {
			String line=lines.get(i);
			int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				continue;
			}
			if (type==0) {
				/* Text */
				final XSLFTextParagraph paragraph=shape.addNewTextParagraph();
				paragraph.setBullet(false);
				final XSLFTextRun run=paragraph.addNewTextRun();
				run.setText(line);
				run.setFontSize(10d);
				continue;
			}
			if (type>0) {
				/* Überschriften */
				final XSLFTextParagraph paragraph=shape.addNewTextParagraph();
				paragraph.setBullet(false);
				final XSLFTextRun run=paragraph.addNewTextRun();
				run.setText(line);
				switch (type) {
				case 1: run.setFontSize(16d); break;
				case 2: run.setFontSize(14d); break;
				case 3: run.setFontSize(12d); break;
				}
				run.setBold(true);
				continue;
			}
		}
	}

	/**
	 * Kopiert den Text als reinen Text ohne Formatierungen und gleichzeitig
	 * als RTF-Text in die Zwischenablage.
	 */
	public void copyToClipboard() {
		final DataFlavor RTF_FLAVOR=new DataFlavor("text/rtf","Rich Formatted Text");
		final String transferPlain=getText();
		final Object transferRTF=new ByteArrayInputStream(getRTFText().getBytes());

		final Transferable transfer=new Transferable() {
			private final DataFlavor[] flavors=new DataFlavor[]{DataFlavor.stringFlavor,RTF_FLAVOR};

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				for (DataFlavor test: flavors) if (test.equals(flavor)) return true;
				return false;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return flavors;
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (flavor.equals(RTF_FLAVOR)) return transferRTF;
				if (flavor.equals(DataFlavor.stringFlavor)) return transferPlain;
				return null;
			}
		};

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transfer,null);
	}

	/**
	 * Statische Hilfsroutine, die einen Dateiauswahldialog zum Speichern von Textdaten anbietet.<br>
	 * Als Formate werden genau die Dateiformate angeboten, die diese Klasse bieten kann.
	 * @param owner	Übergeordnetes Element (zum Ausrichten des Dialogs)
	 * @param title	Titel des Dialogs
	 * @return	Liefert im Erfolgsfall den Dateinamen oder <code>null</code>, wenn der Nutzer die Auswahl abgebrochen hat
	 */
	public static File getSaveFile(final Component owner, final String title) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);

		fc.setDialogTitle(title);

		final FileFilter docx=new FileNameExtensionFilter(Language.tr("FileType.Word")+" (*.docx)","docx");
		final FileFilter odt=new FileNameExtensionFilter(Language.tr("FileType.FileTypeODT")+" (*.odt)","odt");
		final FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
		final FileFilter html=new FileNameExtensionFilter(Language.tr("FileType.HTML")+" (*.html, *.htm)","html","htm");
		final FileFilter rtf=new FileNameExtensionFilter(Language.tr("FileType.RTF")+" (*.rtf)","rtf");
		final FileFilter txt=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		final FileFilter md=new FileNameExtensionFilter(Language.tr("FileType.md")+" (*.md)","md");
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(odt);
		fc.addChoosableFileFilter(pdf);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(rtf);
		fc.addChoosableFileFilter(txt);
		fc.addChoosableFileFilter(md);
		fc.setFileFilter(docx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==odt) file=new File(file.getAbsoluteFile()+".odt");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==rtf) file=new File(file.getAbsoluteFile()+".rtf");
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
			if (fc.getFileFilter()==md) file=new File(file.getAbsoluteFile()+".md");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return null;
		}

		return file;
	}
}
