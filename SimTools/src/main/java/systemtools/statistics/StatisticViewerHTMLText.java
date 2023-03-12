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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.swing.JOpenURL;
import systemtools.MsgBox;

/**
 * Gibt Texte in Form eines HTML-Panels aus.
 * Formatierungen erfolgen direkt über HTML-Befehle, eine Ausgabe als Word- und pdf-Dateien ist somit nicht möglich.
 * Für allgemeine Statistikergebnisse sollte daher eher die Klasse <code>StatisticViewerText</code> verwendet werden.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @see StatisticViewerText
 * @author Alexander Herzog
 */
class StatisticViewerHTMLText implements StatisticViewer {
	/**
	 * Viewer für den html-Text
	 */
	private JTextPane textPane=null;

	/**
	 * Auszugebender Text
	 * @see #textPane
	 */
	private final String infoText;

	/**
	 * Die hier optional angegeben {@link Runnable}-Objekte werden aufgerufen, wenn der Nutzer auf einen Link mit dem Ziel "special:nr" klickt; dabei ist nr-1 der Index der {@link Runnable}-Objektes in dem Array.
	 */
	private final Runnable[] specialLinkListener;

	/**
	 * HTML-Kopfbereich für die Anzeige des html-formatierten Textes in {@link #textPane}.
	 * @see #foot
	 * @see #textPane
	 */
	private static final String head=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <meta charset=\"utf-8\">\n"+
					"  <meta name=\"author\" content=\"Alexander Herzog\">\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #FFFFF3; margin: 2px;}\n"+
					"  ul.big li {margin-bottom: 5px;}\n"+
					"  ol.big li {margin-bottom: 5px;}\n"+
					"  a {text-decoration: none;}\n"+
					"  a.box {margin-top: 10px; margin-botton: 10px; border: 1px solid black; background-color: #DDDDDD; padding: 5px;}\n"+
					"  h2 {margin-bottom: 0px;}\n"+
					"  p.red {color: red;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";

	/**
	 * HTML-Kopfbereich für die Anzeige des html-formatierten Textes in {@link #textPane} (im dunklen Modus).
	 * @see #head
	 * @see #foot
	 * @see #textPane
	 */
	private static final String headDark=
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"+
					"<html>\n"+
					"<head>\n"+
					"  <meta charset=\"utf-8\">\n"+
					"  <meta name=\"author\" content=\"Alexander Herzog\">\n"+
					"  <style type=\"text/css\">\n"+
					"  body {font-family: Verdana, Lucida, sans-serif; background-color: #404040; margin: 2px; color: silver;}\n"+
					"  ul.big li {margin-bottom: 5px;}\n"+
					"  ol.big li {margin-bottom: 5px;}\n"+
					"  a {text-decoration: none;}\n"+
					"  a.box {margin-top: 10px; margin-botton: 10px; border: 1px solid black; background-color: #DDDDDD; padding: 5px;}\n"+
					"  h2 {margin-bottom: 0px;}\n"+
					"  p.red {color: red;}\n"+
					"  </style>\n"+
					"</head>\n"+
					"<body>\n";

	/**
	 * HTML-Fußbereich für die Anzeige des html-formatierten Textes in {@link #textPane}.
	 * @see #head
	 * @see #textPane
	 */
	private static final String foot="</body></html>";

	/**
	 * Erfolgt die Darstellung im Dark-Modus?
	 */
	private final boolean isDark;

	/**
	 * Konstruktor der Klasse
	 * @param infoText	Auszugebender Text
	 * @param specialLinkListener	Die hier optional angegeben {@link Runnable}-Objekte werden aufgerufen, wenn der Nutzer auf einen Link mit dem Ziel "special:nr" klickt; dabei ist nr-1 der Index der {@link Runnable}-Objektes in dem Array
	 */
	public StatisticViewerHTMLText(String infoText, Runnable[] specialLinkListener) {
		this.infoText=infoText;
		this.specialLinkListener=specialLinkListener;

		final Color textBackground=UIManager.getColor("TextField.background");
		isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
	}

	/**
	 * Konstruktor der Klasse
	 * @param infoText	Auszugebender Text
	 */
	public StatisticViewerHTMLText(String infoText) {
		this(infoText,null);
	}

	/**
	 * Initialisiert das {@link JTextPane}-Element mit dem im Konstruktor übergebenen Text
	 */
	protected final void initTextPane() {
		if (textPane!=null) return;
		textPane=new JTextPane();
		textPane.setEditable(false);
		textPane.addHyperlinkListener(new LinkListener());
		textPane.setContentType("text/html");
		textPane.setText((isDark?headDark:head)+infoText+foot);
	}

	@Override
	public ViewerType getType() {return ViewerType.TYPE_TEXT;}

	@Override
	public ViewerImageType getImageType() {return ViewerImageType.IMAGE_TYPE_NOIMAGE;}

	@Override
	public Container getViewer(boolean needReInit) {
		if (textPane==null || needReInit) initTextPane();
		Container c=new JScrollPane(textPane);
		textPane.setSelectionStart(0);
		textPane.setSelectionEnd(0);
		return c;
	}

	@Override
	public boolean isViewerGenerated() {
		return textPane!=null;
	}

	@Override
	public Transferable getTransferable() {
		return new StringSelection(head+infoText+foot);
	}

	@Override
	public void copyToClipboard(final Clipboard clipboard) {
		final Transferable transferable=getTransferable();
		if (transferable!=null) clipboard.setContents(transferable,null);
	}

	@Override
	public boolean print() {
		if (textPane==null) initTextPane();
		try {textPane.print();} catch (PrinterException e) {return false;}
		return true;
	}

	@Override
	public void save(Component owner) {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveText);
		FileFilter html=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeHTML+" (*.html, *.htm)","html","htm");
		fc.addChoosableFileFilter(html);
		fc.setFileFilter(html);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
		}

		save(owner,file);
	}

	@Override
	public void navigation(JButton button) {
	}

	@Override
	public void search(Component owner) {
	}

	@Override
	public boolean save(final Component owner, final File file) {
		if (textPane==null) initTextPane();

		return Table.saveTextToFile(head+infoText+foot,file);
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		if (textPane==null) initTextPane();

		bw.write(head+infoText+foot);
		return nextImageNr;
	}

	@Override
	public int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		if (textPane==null) initTextPane();

		for (String line: infoText.split("\\n")) {
			bw.write("% ");
			bw.write(line);
			bw.newLine();
		}
		return nextImageNr;
	}

	@Override
	public boolean saveDOCX(DOCXWriter doc) {
		doc.writeText(infoText);
		return true;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		if (!pdf.writeStyledText(infoText)) return false;
		pdf.writeStyledParSkip();
		return true;
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		return false;
	}

	@Override
	public void unZoom() {}

	@Override
	public JButton[] getAdditionalButton() {
		return null;
	}

	@Override
	public String[] ownSettingsName() {
		return null;
	}

	@Override
	public Icon[] ownSettingsIcon() {
		return null;
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		return false;
	}

	/**
	 * Reagiert auf Klicks auf Links und auch auf Mausbewegungen über Links.
	 * @see StatisticViewerHTMLText#textPane
	 * @see StatisticViewerHTMLText#specialLinkListener
	 */
	private class LinkListener implements HyperlinkListener {
		/**
		 * Konstruktor der Klasse
		 */
		public LinkListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
				textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
				textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}

			if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
				if (e instanceof HTMLFrameHyperlinkEvent) {
					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
					HTMLDocument doc = (HTMLDocument)textPane.getDocument();
					doc.processHTMLFrameHyperlinkEvent(evt);
				} else {
					URL url=e.getURL();
					if (url==null) {
						final String specialLink=e.getDescription();
						if (specialLink.startsWith("special:")) {
							Integer i=NumberTools.getInteger(specialLink.substring(8));
							if (i!=null && i>=1 && specialLinkListener!=null && i<=specialLinkListener.length && specialLinkListener[i-1]!=null) SwingUtilities.invokeLater(specialLinkListener[i-1]);
						}
					} else {
						String s=e.getURL().toString();
						if (s.toLowerCase().startsWith("mailto:")) {
							try {Desktop.getDesktop().mail(e.getURL().toURI());} catch (IOException | URISyntaxException e1) {
								MsgBox.error(textPane,StatisticsBasePanel.mailErrorTitle,String.format(StatisticsBasePanel.mailErrorInfo,e.getURL().toString()));
							}
						} else {
							JOpenURL.open(textPane,e.getURL());
						}
					}
				}
			}
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
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}
}
