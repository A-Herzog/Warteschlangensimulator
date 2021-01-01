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

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import mathtools.MultiTable;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import xml.XMLData;

/**
 * Diese Klasse kapselt den Reportgenerator, der innerhalb von <code>StatisticPanel</code> verwendet wird.
 * @author Alexander Herzog
 * @version 1.3
 * @see StatisticViewer
 */
public class StatisticViewerReport extends StatisticViewerSpecialBase {
	/**
	 * Dateiformate zum Speichern des Reports.
	 * @author Alexander Herzog
	 * @see StatisticViewerReport#save(Component, File, FileFormat, boolean)
	 */
	public enum FileFormat {
		/**
		 * Dateiformat aus Dateiendung ableiten
		 */
		FORMAT_FROM_FILEEXTENSION,

		/**
		 * html-Datei mit separaten Bildern
		 */
		FORMAT_HTML,

		/**
		 * html-Datei mit eingebetteten Bildern
		 */
		FORMAT_HTML_INLINE,

		/**
		 * html-Datei, die eine interaktive js-basierende App bildet
		 */
		FORMAT_HTML_JS,

		/**
		 * html-Datei, die eine interaktive js-basierende App bildet
		 */
		FORMAT_LATEX,

		/**
		 * Word-docx-Datei
		 */
		FORMAT_DOCX,

		/**
		 * pdf-Datei
		 */
		FORMAT_PDF
	}

	/**
	 * Optionales Statistik-XML-Objekt welches beim Report-Export als html-Datei base64-codiert eingebettet werden soll (darf <code>null</code> sein)
	 */
	private final XMLData statisticsXml;

	/**
	 * Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 */
	private final Runnable helpRunnable;

	/**
	 * Optional Titel f�r den html-Web-App-Export (kann auch <code>null</code> sein)
	 */
	private final String modelName;

	/**
	 * Namen der Viewer
	 * @see #viewers
	 */
	private final List<String> names;

	/**
	 * Vollst�nde Pfade der Viewer im Baum
	 * @see #viewers
	 */
	private final List<String> fullPath;

	/**
	 * Liste der Viewer
	 */
	private final List<StatisticViewer> viewers;

	/**
	 * Tabellendarstellung der zum Export verf�gbaren Viewer
	 * @see #getViewer(boolean)
	 */
	private JReportCheckboxTable table=null;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerReport</code>
	 * @param root	Referenz auf das innerhalb von <code>StatisticPanel</code> verwendete Basis-<code>StatisticNode</code>-Objekt
	 * @param statisticsXml	Optionales Statistik-XML-Objekt welches beim Report-Export als html-Datei base64-codiert eingebettet werden soll (darf <code>null</code> sein)
	 * @param modelName	Optional Titel f�r den html-Web-App-Export (kann auch <code>null</code> sein)
	 * @param viewerIndex Gibt an, auf welchen der m�glicherweise mehreren Viewer in den <code>StatisticNode</code> sich dieses Report-Objekt beziehen soll
	 * @param helpRunnable Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 * @see StatisticNode
	 */
	public StatisticViewerReport(final StatisticNode root, final XMLData statisticsXml, final String modelName, final int viewerIndex, final Runnable helpRunnable) {
		this.statisticsXml=statisticsXml;
		this.helpRunnable=helpRunnable;
		this.modelName=modelName;
		names=new ArrayList<>();
		fullPath=new ArrayList<>();
		viewers=new ArrayList<>();
		addChildNodes(root,viewerIndex);
	}

	/**
	 * F�gt ausgehend von einem Elternelement alle Kindelemente und deren Kindelement zu der Liste der Viewer f�r die Liste der Ausgabem�glichkeiten hinzu
	 * @param parent	Elternelement
	 * @param viewerIndex	Gibt an, auf welchen der m�glicherweise mehreren Viewer in den <code>StatisticNode</code> sich dieses Report-Objekt beziehen soll
	 */
	private void addChildNodes(final StatisticNode parent, final int viewerIndex) {
		if (parent==null) return;
		for (int i=0;i<parent.getChildCount();i++) {
			final StatisticNode node=parent.getChild(i);
			if (node.getChildCount()>0) {addChildNodes(node,viewerIndex); continue;}

			if (node.viewer.length<=viewerIndex) continue;

			final String add;
			switch(node.viewer[viewerIndex].getType()) {
			case TYPE_TEXT: add=" ("+StatisticsBasePanel.typeText+")"; break;
			case TYPE_TABLE: add=" ("+StatisticsBasePanel.typeTable+")"; break;
			case TYPE_IMAGE: add=" ("+StatisticsBasePanel.typeImage+")"; break;
			default: add=""; break;
			}
			String path=node.name+add;
			String name=node.name+add;
			StatisticNode branch=node;
			while (branch.getParent()!=null) {
				branch=branch.getParent();
				if (branch.name!=null && !branch.name.isEmpty()) {
					name=branch.name+" - "+name;
					path=branch.name+"\n"+path;
				}
			}

			if (node.viewer.length==0) continue;
			if (node.viewer[viewerIndex].getType()==StatisticViewer.ViewerType.TYPE_REPORT || node.viewer[viewerIndex].getType()==StatisticViewer.ViewerType.TYPE_SPECIAL) continue;
			viewers.add(node.viewer[viewerIndex]);
			names.add(name);
			fullPath.add(path);
		}
	}

	@Override
	public ViewerType getType() {return ViewerType.TYPE_REPORT;}

	@Override
	public Container getViewer(boolean needReInit) {
		table=new JReportCheckboxTable(names.toArray(new String[0]));
		table.setSetup(getSelectSettings());
		return new JScrollPane(table);
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		final StringWriter st=new StringWriter();
		writeReportToBufferedWriter(st,null,FileFormat.FORMAT_HTML_INLINE,false);
		clipboard.setContents(new StringSelection(st.toString()),null);
	}

	@Override
	public boolean print() {
		if (viewers.isEmpty()) return false;
		File dir=new File(System.getProperty("java.io.tmpdir")+"CSSim");
		if (!dir.isDirectory() && !dir.mkdir()) return false;
		File file=new File(dir,"Report.html");
		if (!save(null,file,FileFormat.FORMAT_HTML_INLINE,false)) return false;
		try {
			Desktop.getDesktop().print(file);
		} catch (IOException e) {
			MsgBox.error(null,StatisticsBasePanel.viewersNoHTMLApplicationTitle,StatisticsBasePanel.viewersNoHTMLApplicationInfo);
			try {Desktop.getDesktop().open(file);} catch (IOException e1) {return false;}
			return false;
		}
		return true;
	}

	/**
	 * Status eines {@link InlineReportThread}
	 * @see InlineReportThread#status
	 */
	private enum InlineReportThreadStatus {
		/** Thread wartet noch */
		WAITING,
		/** Thread arbeitet gerade */
		RUNNING,
		/** Thread hat seine Arbeit abgeschlossen */
		DONE
	}

	/**
	 * Art des Reports
	 * @see InlineReportThread
	 */
	private enum ExportMode {
		/** HTML-Report erstellen */
		HTML,
		/** LaTeX-Report erstellen */
		LATEX
	}

	/**
	 * Thread der zu einem Viewer den zugeh�rigen HTML- oder LaTeX-Code erstellt
	 * @see StatisticViewerReport#buildInlineData(List, List)
	 */
	private class InlineReportThread extends Thread {
		/**
		 * Art des Reports
		 * @see ExportMode
		 */
		private final ExportMode mode;

		/**
		 * Zu exportierender Viewer
		 */
		private final StatisticViewer viewer;

		/**
		 * Name des Viewers
		 */
		private final String name;

		/**
		 * Ausgaben des Threads (exportierte bzw. passend formatierte Daten)
		 */
		public String result="";

		/**
		 * Status der Verarbeitung
		 * @see InlineReportThreadStatus
		 */
		public InlineReportThreadStatus status;

		/**
		 * Konstruktor der Klasse
		 * @param mode	Art des Reports
		 * @param group	Zugeh�rige Thread-Gruppe
		 * @param viewer	Zu exportierender Viewer
		 * @param name	Name des Viewers
		 * @see ExportMode
		 */
		public InlineReportThread(final ExportMode mode, final ThreadGroup group, final StatisticViewer viewer, final String name) {
			super(group,"Report "+name);
			this.mode=mode;
			this.viewer=viewer;
			this.name=name;
			status=InlineReportThreadStatus.WAITING;
		}

		@Override
		public synchronized void start() {
			status=InlineReportThreadStatus.RUNNING;
			super.start();
		}

		@Override
		public void run() {
			final StringWriter st=new StringWriter();
			final BufferedWriter bw=new BufferedWriter(st);

			try {
				switch (mode) {
				case HTML:
					bw.write("<h1>"+name+"</h1>");
					bw.newLine();
					viewer.saveHtml(bw,null,1,true);
					bw.newLine();
					break;
				case LATEX:
					bw.write("\\section{"+name+"}");
					bw.newLine();
					viewer.saveLaTeX(bw,null,1);
					bw.newLine();
					break;
				}
				bw.flush();
			} catch (IOException e) {return;}

			result=st.toString();
			status=InlineReportThreadStatus.DONE;
		}
	}

	/**
	 * Speichert die Daten von mehreren Viewern in einem bestimmten Format in einem {@link BufferedWriter}
	 * @param mode	Ausgabemodus
	 * @param viewers	Liste der auszugebenden Viewer
	 * @param names	Beteichner f�r die Viewer
	 * @param bw	Ausgabeziel
	 * @param baseFileName	Basisdateiname f�r Bilder
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see ExportMode
	 */
	private boolean writeReportNodesToBufferedWriterFile(final ExportMode mode, final List<StatisticViewer> viewers, final List<String> names, final BufferedWriter bw, final File baseFileName) throws IOException {
		int nextImageNr=1;
		for (int i=0;i<viewers.size();i++) {
			if (i>0) bw.newLine();
			switch (mode) {
			case HTML:
				bw.write("<h1>"+names.get(i)+"</h1>");
				break;
			case LATEX:
				bw.write("\\section{"+names.get(i)+"}");
				break;
			}

			bw.newLine();

			switch (mode) {
			case HTML:
				nextImageNr=viewers.get(i).saveHtml(bw,baseFileName,nextImageNr,false);
				break;
			case LATEX:
				nextImageNr=viewers.get(i).saveLaTeX(bw,baseFileName,nextImageNr);
				break;
			}
			bw.newLine();
		}
		return true;
	}

	/**
	 * F�gt die base64-codierten und in einen HTML-Kommentar verpackten xml-Statistikdaten an einen {@link BufferedWriter} an.
	 * @param bw	Ausgabeziel
	 * @param statistics	Auszugebende Statistikdaten
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean writeBase64StatisticsData(final BufferedWriter bw, final XMLData statistics) {
		try {
			bw.write("<!--\n");
			bw.write("QSModel\n");
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			statistics.saveToStream(out);
			final String base64bytes=Base64.getEncoder().encodeToString(out.toByteArray());
			bw.write("data:application/xml;base64,"+base64bytes+"\n");
			bw.write("-->\n");
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Anzahl der parallelen Threads bei der Reporterstellung.
	 * @see #buildInlineData(List, List)
	 */
	private static final int MAX_REPORT_BUILDER_THREADS=5; /* Ein zu hoher Wert provoziert OutOfMemory-Probleme. */

	/**
	 * Erstellt zu den angegebenen Viewern mit Hilfe von {@link InlineReportThread}-Objekten
	 * HTML-Report-Texte mit Inline-Bildern
	 * @param viewers	Zu ber�cksichtigende Viewer
	 * @param names	Namen der Viewer
	 * @return	HTML-Texte zu den Viewern
	 * @see #writeReportNodesToBufferedWriterInline(List, List, BufferedWriter)
	 * @see #writeReportNodesToBufferedWriterApp(String, List, List, List, BufferedWriter)
	 */
	private List<String> buildInlineData(final List<StatisticViewer> viewers, final List<String> names) {
		final ThreadGroup group=new ThreadGroup("Report");
		final List<InlineReportThread> list=new ArrayList<>();
		for (int i=0;i<viewers.size();i++) list.add(new InlineReportThread(ExportMode.HTML,group,viewers.get(i),names.get(i)));

		while (true) {
			/* Threads starten */
			if (list.stream().filter(t->t.status==InlineReportThreadStatus.RUNNING).count()<MAX_REPORT_BUILDER_THREADS) {
				Optional<InlineReportThread> nextThread=list.stream().filter(t->t.status==InlineReportThreadStatus.WAITING).findFirst();
				if (nextThread.isPresent()) {
					nextThread.get().start();
					continue;
				}
			}

			/* Alle fertig ? */
			if (list.stream().filter(t->t.status==InlineReportThreadStatus.DONE).count()==list.size()) break;

			/* Warten */
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {}
		}

		final List<String> results=new ArrayList<>(list.size());
		for (int i=0;i<list.size();i++) results.add(list.get(i).result);

		return results;
	}

	/**
	 * Erstellt zu den angegebenen Viewern mit Hilfe von {@link InlineReportThread}-Objekten
	 * HTML-Report-Texte mit Inline-Bildern
	 * @param viewers	Zu ber�cksichtigende Viewer
	 * @param names	Namen der Viewer
	 * @param bw	Ausgabe f�r die HTML-Texte zu den Viewern
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see #writeReportToBufferedWriter(Writer, File, FileFormat, boolean)
	 */
	private boolean writeReportNodesToBufferedWriterInline(final List<StatisticViewer> viewers, final List<String> names, final BufferedWriter bw) throws IOException {
		final List<String> data=buildInlineData(viewers,names);
		for (int i=0;i<viewers.size();i++) bw.write(data.get(i));
		return true;
	}

	/**
	 * Erstellt den Bereich horizontal vor einem Eintrag in der HTML-Baumstruktur;
	 * ber�cksichtigt dabei die Einr�ckungen des aktuellen und des Vorg�ngerknotens.
	 * @param path	Pfad des Vorg�ngerknotens
	 * @param current	Pfad des aktuellen Knotens
	 * @param sb	Ausgabe-{@link StringBuilder}
	 * @see #buildTree(List, List)
	 */
	private void addTreeParents(final List<String> path, final List<String> current, final StringBuilder sb) {
		int matching=0;
		for (int i=0;i<Math.min(path.size()-1,current.size()-1);i++) if (path.get(i).equals(current.get(i))) matching++; else break;

		for (int i=matching;i<current.size()-1;i++) {
			for (int j=1;j<i;j++) sb.append("&nbsp;&nbsp;");
			sb.append("<b>");
			sb.append(current.get(i));
			sb.append("</b><br>\n");
		}

		path.clear();
		path.addAll(current);
	}

	/**
	 * Erstellt ein HTML-Inline-Icon f�r einen Viewer
	 * @param viewer	Viewer f�r den das Icon erstellt werden soll
	 * @return	Icon als HTML-Code (base64-encodiertes Inline-Bild)
	 * @see #buildTree(List, List)
	 */
	private String htmlIconForViewer(final StatisticViewer viewer) {
		final Image image=StatisticTreeCellRenderer.getImageViewerIcon(viewer);
		if (image==null) return "";

		final ByteArrayOutputStream out=new ByteArrayOutputStream();

		try {
			final BufferedImage bufferedImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_ARGB);
			final Graphics2D bGr=bufferedImage.createGraphics();
			bGr.drawImage(image,0,0,null);
			bGr.dispose();
			ImageIO.write(bufferedImage,"png",out);
		} catch (IOException e) {return "";}
		final byte[] bytes=out.toByteArray();

		final String base64bytes=Base64.getEncoder().encodeToString(bytes);
		return "<img src=\"data:image/png;base64,"+base64bytes+"\">";
	}

	/**
	 * Erstellt eine HTML-Baumstruktur f�r einen interaktiven HTML-Report
	 * @param viewers	Zu ber�cksichtigende Viewer
	 * @param fullPathes	Vollst�ndige Namen der Viewer
	 * @return	HTML-Code f�r die Baumstruktur
	 * @see #writeReportNodesToBufferedWriterApp(String, List, List, List, BufferedWriter)
	 */
	private String buildTree(final List<StatisticViewer> viewers, final List<String> fullPathes) {
		final StringBuilder sb=new StringBuilder();

		final List<String> lastPath=new ArrayList<>();

		for (int i=0;i<fullPathes.size();i++) {
			final String[] currentPath=fullPathes.get(i).split("\n");
			addTreeParents(lastPath,Arrays.asList(currentPath),sb);

			for (int j=1;j<currentPath.length;j++) sb.append("&nbsp;&nbsp;");
			sb.append("<a onclick=\"show("+(i+1)+");\">"+htmlIconForViewer(viewers.get(i))+currentPath[currentPath.length-1]+"</a><br>\n");
		}

		return sb.toString();
	}

	/**
	 * Erstellt den inneren HTML-Code f�r einen interaktiven HTML-Report
	 * @param modelName	Optional Titel f�r den html-Web-App-Export (kann auch <code>null</code> sein)
	 * @param viewers	Zu ber�cksichtigende Viewer
	 * @param names	Namen der Viewer
	 * @param fullPathes	Vollst�ndige Pfade der Viewer
	 * @param bw	Ausgabe f�r die HTML-Texte zu den Viewern
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see #writeReportToBufferedWriter(Writer, File, FileFormat, boolean)
	 */
	private boolean writeReportNodesToBufferedWriterApp(final String modelName, final List<StatisticViewer> viewers, final List<String> names, final List<String> fullPathes, final BufferedWriter bw) throws IOException {
		final List<String> data=buildInlineData(viewers,names);

		final StringBuilder sb=new StringBuilder();
		sb.append("<div class=\"topic\" id=\"topic0\">"+StatisticsBasePanel.viewersReportSaveHTMLAppInfo+"</div>\n");
		for (int i=0;i<data.size();i++) {
			sb.append("<div class=\"topic\" id=\"topic"+(i+1)+"\" style=\"display: none;\">\n");
			sb.append(data.get(i));
			sb.append("\n</div>\n");
		}

		final String title;
		if (modelName==null || modelName.trim().isEmpty()) title=StatisticsBasePanel.viewersReportSaveHTMLAppTitle; else title=modelName+" - "+StatisticsBasePanel.viewersReportSaveHTMLAppTitle;
		bw.write("<h1 class=\"main\">"+title+"</h1>"); bw.newLine();
		bw.write("<div class=\"page\">"); bw.newLine();
		bw.write("<div style=\"flex-grow: 1; resize: horizontal;\">"+buildTree(viewers,fullPathes)+"</div>"); bw.newLine();
		bw.write("<div style=\"flex-grow: 3;\">"); bw.newLine();
		bw.write(sb.toString()); bw.newLine();
		bw.write("</div>"); bw.newLine();
		bw.write("</div>"); bw.newLine();

		bw.write("<script type=\"text/javascript\">"); bw.newLine();
		bw.write("<!--"); bw.newLine();
		bw.write("'use strict';"); bw.newLine();
		bw.write("function show(id) {"); bw.newLine();
		bw.write("  var divs=document.getElementsByClassName(\"topic\");"); bw.newLine();
		bw.write("  for (var i=0;i<divs.length;i++) divs[i].style.display=(divs[i].id==\"topic\"+id)?\"block\":\"none\";"); bw.newLine();
		bw.write("}"); bw.newLine();
		bw.write("//->"); bw.newLine();
		bw.write("</script>"); bw.newLine();

		return true;
	}

	/**
	 * Gibt einen HTML-Kopfbereich f�r HTML-Reports (statisch und dynamisch) aus.
	 * @param bw	Ausgabeziel
	 * @param addStyles	Zus�tzlich auszugebende Styles (darf <code>null</code> sein)
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see #writeReportToBufferedWriter
	 * @see #writeHTMLFoot(BufferedWriter)
	 */
	private void writeHTMLHead(final BufferedWriter bw, final String[] addStyles) throws IOException {
		bw.write("<!DOCTYPE html>"); bw.newLine();
		bw.write("<html>"); bw.newLine();
		bw.write("<head>"); bw.newLine();
		bw.write("  <meta charset=\"utf-8\">"); bw.newLine();
		bw.write("  <title>"+StatisticsBasePanel.program_name+"</title>"); bw.newLine();
		bw.write("  <meta name=\"author\" content=\"Alexander Herzog\">"); bw.newLine();
		bw.write("  <style type=\"text/css\">"); bw.newLine();
		bw.write("    body {font: 82% Verdana,Lucida,sans-serif;}"); bw.newLine();
		bw.write("    table {border: 1px solid black; border-collapse: collapse;}"); bw.newLine();
		bw.write("    td {border: 1px solid black; padding: 2px 5px;}"); bw.newLine();
		if (addStyles!=null) for (String style: addStyles) {
			bw.write("    ");
			bw.write(style);
			bw.newLine();
		}
		bw.write("  </style>"); bw.newLine();
		bw.write("</head>"); bw.newLine();
		bw.write("<body>"); bw.newLine();
		bw.newLine();
	}

	/**
	 * Gibt einen HTML-Fu�bereich f�r HTML-Reports (statisch und dynamisch) aus.
	 * @param bw	Ausgabeziel
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see #writeReportToBufferedWriter
	 * @see #writeHTMLHead(BufferedWriter, String[])
	 */
	private void writeHTMLFoot(final BufferedWriter bw) throws IOException {
		bw.newLine();
		bw.write("</body>");
		bw.newLine();
		bw.write("</html>");
		bw.newLine();
	}

	/**
	 * Gibt einen LaTeX-Kopfbereich f�r LaTeX-Reports aus.
	 * @param bw	Ausgabeziel
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see #writeReportToBufferedWriter
	 * @see #writeLaTeXFoot(BufferedWriter)
	 */
	private void writeLaTeXHead(final BufferedWriter bw) throws IOException {
		bw.write("\\documentclass{article}"); bw.newLine();
		bw.newLine();
		bw.write("\\usepackage[utf8]{inputenc}"); bw.newLine();
		bw.write("\\usepackage[T1]{fontenc}"); bw.newLine();
		bw.write("\\usepackage{lmodern}"); bw.newLine();
		bw.write("\\usepackage[english]{babel}"); bw.newLine();
		bw.write("\\usepackage{amssymb,amsmath,amsfonts}"); bw.newLine();
		bw.write("\\usepackage{graphicx}"); bw.newLine();
		bw.write("\\usepackage{float}"); bw.newLine();
		bw.newLine();
		bw.write("\\parindent0pt"); bw.newLine();
		bw.newLine();
		bw.write("\\begin{document}"); bw.newLine();
		bw.newLine();
	}

	/**
	 * Gibt einen LaTeX-Fu�bereich f�r LaTeX-Reports aus.
	 * @param bw	Ausgabeziel
	 * @throws IOException	Wird bei einem Fehler beim Schreiben in den Writer ausgel�st
	 * @see #writeReportToBufferedWriter
	 * @see #writeLaTeXHead(BufferedWriter)
	 */
	private void writeLaTeXFoot(final BufferedWriter bw) throws IOException {
		bw.newLine();
		bw.write("\\end{document}");
		bw.newLine();
	}

	/**
	 * Zus�tzliche Styles, die �ber {@link #writeHTMLHead(BufferedWriter, String[])} hinausgehen
	 * f�r den dynamischen HTML-Report-Viewer
	 * @see #writeHTMLHead(BufferedWriter, String[])
	 * @see #writeReportToBufferedWriter(Writer, File, FileFormat, boolean)
	 * @see #writeReportHTMLApp(OutputStream)
	 */
	private static String[] addStyles=new String[] {
			"body {margin: 0; padding: 0; height: 100%; width: 100%; position: absolute;}",
			"h1.main {color: white; background-color: blue; margin: 0px; padding: 5px 25px;}",
			".page {display: flex; flex-flow: col nowrap; margin: 0; height: 92%;}",
			".page div {height: 100%; padding: 2px; overflow: scroll;}",
			".page div a {cursor: pointer; text-decoration: none; color: blue;}",
			".page div a:hover {text-decoration: underline;}",
			".topic {overflow: visible !important;}",
			"a img {margin-right: 2px; vertical-align:middle;}"
	};

	/**
	 * Schreibt eine HTML-App �ber alle Statistikdaten in einen Stream.
	 * @param stream	Ausgabestream
	 * @return	Gibt an, ob die Ausgabe erfolgreich war
	 */
	public boolean writeReportHTMLApp(final OutputStream stream) {
		try (OutputStreamWriter writer=new OutputStreamWriter(stream,StandardCharsets.UTF_8)) {
			return writeReportToBufferedWriter(writer,null,FileFormat.FORMAT_HTML_JS,true);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Schreibt einen HTML- oder LaTeX-Report in einen {@link Writer}.
	 * @param writer	Ausgabe-{@link Writer}
	 * @param baseFileName	Basisdateiname f�r Bilder
	 * @param fileFormat	Ausgabeformat
	 * @param exportAllItems	Alle Viewer ausgeben (<code>true</code>) oder nur die gew�hlten (<code>false</code>)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #save(Component, File, FileFormat, boolean)
	 */
	private boolean writeReportToBufferedWriter(final Writer writer, final File baseFileName, final FileFormat fileFormat, final boolean exportAllItems) {
		if (table==null) getViewer(false);

		try {
			try (BufferedWriter bw=new BufferedWriter(writer)) {
				switch (fileFormat) {
				case FORMAT_HTML:
				case FORMAT_HTML_INLINE:
					writeHTMLHead(bw,null);
					break;
				case FORMAT_HTML_JS:
					writeHTMLHead(bw,addStyles);
					bw.write("<noscript>"+StatisticsBasePanel.viewersReportSaveHTMLAppJSError+"</noscript>"); bw.newLine();
					bw.newLine();
					break;
				case FORMAT_LATEX:
					writeLaTeXHead(bw);
					break;
				default:
					break;
				}

				final boolean[] select=table.getSelected();
				final List<StatisticViewer> selectedViewers=new ArrayList<>();
				final List<String> selectedNames=new ArrayList<>();
				final List<String> selectedFullPathes=new ArrayList<>();
				for (int i=0;i<select.length;i++) if (select[i] || exportAllItems) {
					selectedViewers.add(viewers.get(i));
					selectedNames.add(names.get(i));
					selectedFullPathes.add(fullPath.get(i));
				}

				switch (fileFormat) {
				case FORMAT_HTML:
					if (!writeReportNodesToBufferedWriterFile(ExportMode.HTML,selectedViewers,selectedNames,bw,baseFileName)) return false;
					if (statisticsXml!=null) writeBase64StatisticsData(bw,statisticsXml);
					break;
				case FORMAT_HTML_INLINE:
					if (!writeReportNodesToBufferedWriterInline(selectedViewers,selectedNames,bw)) return false;
					if (statisticsXml!=null) writeBase64StatisticsData(bw,statisticsXml);
					break;
				case FORMAT_HTML_JS:
					if (!writeReportNodesToBufferedWriterApp(modelName,selectedViewers,selectedNames,selectedFullPathes,bw)) return false;
					if (statisticsXml!=null) writeBase64StatisticsData(bw,statisticsXml);
					break;
				case FORMAT_LATEX:
					if (!writeReportNodesToBufferedWriterFile(ExportMode.LATEX,selectedViewers,selectedNames,bw,baseFileName)) return false;
					break;
				default:
					return false;
				}

				switch (fileFormat) {
				case FORMAT_HTML:
				case FORMAT_HTML_INLINE:
				case FORMAT_HTML_JS:
					writeHTMLFoot(bw);
					break;
				case FORMAT_LATEX:
					writeLaTeXFoot(bw);
					break;
				default:
					break;

				}
			}
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Speichert den gesamten Report als docx-Datei.
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll.
	 * @param exportAllItems	Alle Viewer ausgeben (<code>true</code>) oder nur die gew�hlten (<code>false</code>)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #save(Component, File)
	 */
	private boolean writeReportToWordFile(File file, boolean exportAllItems) {
		if (table==null) getViewer(false);

		try (XWPFDocument doc=new XWPFDocument()) {

			boolean[] select=table.getSelected();
			for (int i=0;i<select.length;i++) if (select[i] || exportAllItems) {
				XWPFParagraph p=doc.createParagraph();
				XWPFRun r=p.createRun();
				r.setFontSize(24);
				r.setBold(true);
				r.setText(names.get(i));
				if (!viewers.get(i).saveDOCX(doc)) return false;
			}

			try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);}
			return true;

		} catch (IOException e) {return false;}
	}

	/**
	 * Speichert den gesamten Report als pdf-Datei.
	 * @param owner	�bergeordnete Komponente f�r die eventuelle Anzeige von Dialogen
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll.
	 * @param exportAllItems	Alle Viewer ausgeben (<code>true</code>) oder nur die gew�hlten (<code>false</code>)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #save(Component, File)
	 */
	private boolean writeReportToPDFFile(Component owner, File file, boolean exportAllItems) {
		if (table==null) getViewer(false);

		PDFWriter pdf=new PDFWriter(owner,15,10);
		if (!pdf.systemOK) return false;

		boolean[] select=table.getSelected();
		for (int i=0;i<select.length;i++) if (select[i] || exportAllItems) {
			if (!pdf.writeText(names.get(i),24,true,10)) return false;
			if (!viewers.get(i).savePDF(pdf)) return false;
		}

		return pdf.save(file);
	}

	/**
	 * Liefert das Dateiformat f�r den Export
	 * @param fileFormat	Vorab gew�hltes Format (steht hier {@link FileFormat#FORMAT_FROM_FILEEXTENSION} so wird der <code>file</code>-Parameter ber�cksichtigt)
	 * @param file	Vom Dateinamen wird im Fall {@link FileFormat#FORMAT_FROM_FILEEXTENSION} das Format abgeleitet
	 * @return	Ausgabeformat
	 * @see #save(Component, File)
	 */
	private FileFormat getFileFormat(final FileFormat fileFormat, final File file) {
		if (fileFormat!=null && fileFormat!=FileFormat.FORMAT_FROM_FILEEXTENSION) return fileFormat;

		final String name=file.getName().toLowerCase();
		if (name.endsWith(".docx")) return FileFormat.FORMAT_DOCX;
		if (name.endsWith(".pdf")) return FileFormat.FORMAT_PDF;
		if (name.endsWith(".html") || name.endsWith(".htm")) return FileFormat.FORMAT_HTML_INLINE;
		if (name.endsWith(".tex")) return FileFormat.FORMAT_LATEX;

		return null;
	}

	/**
	 * Erstellt einen Report und verwendet dabei das explizit angegebene Dateiformat.<br>
	 * Wird {@link #save(Component, File)} verwendet, so wird das Dateiformat aus der Erweiterung des Dateinamens abgeleitet.
	 * @param owner	�bergeordnete Komponente f�r die eventuelle Anzeige von Dialogen
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> �bergeben werden.
	 * @param fileFormat	Zu verwendendes Dateiformat
	 * @param exportAllItems	Alle verf�gbaren Informationen (<code>true</code>) oder nur die ausgew�hlten (<code>false</code>) exportieren?
	 * @return	Liefert <code>true</code> zur�ck, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean save(final Component owner, final File file, FileFormat fileFormat, boolean exportAllItems) {
		fileFormat=getFileFormat(fileFormat,file);
		if (fileFormat==null) return false;

		switch (fileFormat) {
		case FORMAT_DOCX:
			return writeReportToWordFile(file,exportAllItems);
		case FORMAT_PDF:
			return writeReportToPDFFile(owner,file,exportAllItems);
		case FORMAT_HTML:
		case FORMAT_HTML_INLINE:
		case FORMAT_HTML_JS:
		case FORMAT_LATEX:
			try (final OutputStreamWriter fw=new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8)) {
				return writeReportToBufferedWriter(fw,file,fileFormat,exportAllItems);
			} catch (IOException e) {return false;}
		case FORMAT_FROM_FILEEXTENSION:
			return false; /* Sollte es an dieser Stelle nicht mehr geben. */
		default:
			return false;
		}
	}

	@Override
	public boolean save(Component owner, File file) {
		return save(owner,file,FileFormat.FORMAT_FROM_FILEEXTENSION,false);
	}

	/**
	 * L�dt die Einstellung, ob Bilder bei HTML-Reports inline ausgegeben werden sollen, aus dem Setup (in einer abgeleiteten Klasse)
	 * @return	Gibt an, ob Bilder bei bei HTML-Reports inline ausgegeben werden sollen.
	 */
	protected boolean loadImagesInline() {
		return true;
	}

	/**
	 * Speichert die Einstellung, ob Bilder bei HTML-Reports inline ausgegeben werden sollen, im Setup (in einer abgeleiteten Klasse)
	 * @param imagesInline	Gibt an, ob Bilder bei HTML-Reports inline ausgegeben werden sollen.
	 */
	protected void saveImagesInline(final boolean imagesInline) {}

	@Override
	public void save(Component owner) {
		if (viewers.isEmpty()) return;
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StatisticsBasePanel.viewersReport);
		FileFilter html=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeHTML+" (*.html, *.htm)","html","htm");
		FileFilter htmljs=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeHTMLJS+" (*.html, *.htm)","html","htm");
		FileFilter docx=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeDOCX+" (*.docx)","docx");
		FileFilter pdf=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		FileFilter tex=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeTEX+" (*.tex)","tex");
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pdf);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(htmljs);
		fc.addChoosableFileFilter(tex);
		fc.setFileFilter(docx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==htmljs) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (fc.getFileFilter()==tex) file=new File(file.getAbsoluteFile()+".tex");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (file.getName().toLowerCase().endsWith(".docx")) {
			/* Export als Word-Datei */
			if (!save(owner,file,FileFormat.FORMAT_DOCX,false)) MsgBox.error(owner,StatisticsBasePanel.writeErrorTitle,String.format(StatisticsBasePanel.writeErrorInfo,file.toString()));
			return;
		}

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			/* Export als PDF */
			if (!save(owner,file,FileFormat.FORMAT_PDF,false)) MsgBox.error(owner,StatisticsBasePanel.writeErrorTitle,String.format(StatisticsBasePanel.writeErrorInfo,file.toString()));
			return;
		}

		if (file.getName().toLowerCase().endsWith(".tex")) {
			/* Export als LaTeX-Dokument */
			if (!save(owner,file,FileFormat.FORMAT_LATEX,false)) MsgBox.error(owner,StatisticsBasePanel.writeErrorTitle,String.format(StatisticsBasePanel.writeErrorInfo,file.toString()));
			return;
		}

		/* Export als HTML-Datei */
		if (fc.getFileFilter()==htmljs) {
			save(owner,file,FileFormat.FORMAT_HTML_JS,false);
		} else {
			boolean imagesInline=loadImagesInline();
			StatisticViewerReportDialog dialog=new StatisticViewerReportDialog(owner,imagesInline,helpRunnable);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				boolean imagesInlineOld=imagesInline;
				imagesInline=dialog.isInline();
				if (imagesInline!=imagesInlineOld) saveImagesInline(imagesInline);
				save(owner,file,imagesInline?FileFormat.FORMAT_HTML_INLINE:FileFormat.FORMAT_HTML,false);
			}
		}
	}

	@Override
	public void search(Component owner) {
	}


	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_COPY : return true;
		case CAN_DO_PRINT : return true;
		case CAN_DO_SAVE : return true;
		default: return false;
		}
	}

	/**
	 * Speichert alle in der aktuellen Statistik vorhandenen Tabellen in einer
	 * Arbeitsmappe. Der Dateiname, unter dem diese Daten gespeichert werden sollen,
	 * wird dabei von dieser Funktion abgefragt.
	 * @param parent	�bergeordnete Komponente f�r die Anzeige von Dialogen
	 */
	public void saveTablesToWorkbook(Component parent) {
		if (table==null) getViewer(false);

		MultiTable workbook=new MultiTable();

		boolean[] select=table.getSelected();
		for (int i=0;i<select.length;i++) if (select[i] && viewers.get(i) instanceof StatisticViewerTable) {
			StatisticViewerTable viewer=(StatisticViewerTable)(viewers.get(i));
			workbook.add(names.get(i),viewer.toTable());
		}

		if (workbook.size()==0) {
			MsgBox.error(parent,StatisticsBasePanel.viewersReportNoTablesSelectedTitle,StatisticsBasePanel.viewersReportNoTablesSelectedInfo);
			return;
		}

		File file=MultiTable.showSaveDialog(parent,StatisticsBasePanel.viewersReportSaveWorkbook,null);
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(parent,file)) return;
		}

		if (!workbook.save(file)) {
			MsgBox.error(parent,StatisticsBasePanel.viewersReportSaveWorkbookErrorTitle,String.format(StatisticsBasePanel.viewersReportSaveWorkbookErrorInfo,file.toString()));
		}
	}

	/**
	 * W�hlt alle Statistikdaten in der Reporterstellungs-Auswahltabelle aus.
	 */
	public void selectAll() {
		table.selectAll();
	}

	/**
	 * W�hlt alle Statistikdaten in der Reporterstellungs-Auswahltabelle ab.
	 */
	public void selectNone() {
		table.selectNone();
	}

	/**
	 * Liefert die aktuellen Einstellungen, welche Kategorien in der Reporterstellungs-Auswahltabelle aktiviert sind.
	 * @return	Gew�hlte Reporterstellungs-Kategorien
	 */
	protected String getSelectSettings() {return "";}

	/**
	 * Stellt gem�� den �bergebenen Einstellungen ein, welche Kategorien in der Reporterstellungs-Auswahltabelle aktiviert sein sollen.
	 * @param settings	Zu w�hlende Reporterstellungs-Kategorien
	 */
	protected void setSelectSettings(String settings) {}

	/** Tabelle mit den Checkboxen */
	private final class JReportCheckboxTable extends JCheckboxTable {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 1342890838297765223L;

		/**
		 * Konstruktor der Klasse
		 * @param keys	Array mit den in der Liste anzuzeigenden Texten (darf nicht <code>null</code> sein)
		 */
		public JReportCheckboxTable(String[] keys) {
			super(keys);
		}

		@Override
		protected void selectChanged() {
			setSelectSettings(getSetup());
		}
	}

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {}

	@Override
	public void setRequestChartSetup(Supplier<ChartSetup> getChartSetup) {}

	@Override
	public void setUpdateChartSetup(Consumer<ChartSetup> setChartSetup) {	}

	/**
	 * Soll f�r diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	�bergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}
}