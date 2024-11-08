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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DialogTypeSelection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;

import mathtools.NumberTools;
import mathtools.TableChart;
import systemtools.BaseDialog;
import systemtools.GUITools;
import systemtools.ImageTools;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Abstrakte Basisklasse zur Anzeige von {@link JFreeChart}-Diagrammen
 * @see JFreeChart
 * @author Alexander Herzog
 * @version 1.5
 */
public abstract class StatisticViewerJFreeChart implements StatisticViewer {
	/**
	 * Einstellungen zu Schriftgrößen und Farben
	 */
	protected final ChartSetup chartSetup;

	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link ChartPanel}-Element
	 */
	protected ChartPanel chartPanel=null;

	/**
	 * Ermöglicht für abgeleitete Klassen einen Zugriff auf das {@link JFreeChart}-Element
	 */
	protected JFreeChart chart;

	/**
	 * html-Seite mit einer zusätzlichen Erklärung zu dieser Statistikseite
	 * @see #addDescription(URL, Consumer)
	 */
	private URL descriptionURL=null;

	/**
	 * Handler, der Themennamen (angegeben über "help:..."-Links) zum Aufruf normaler Hilfeseiten entgegen nimmt
	 * @see #addDescription(URL, Consumer)
	 */
	private Consumer<String> descriptionHelpCallback=null;

	/**
	 * Darstellung der Hilfe-Seite {@link #descriptionURL}
	 * @see #initDescriptionPane()
	 * @see #addDescription(URL, Consumer)
	 */
	private DescriptionViewer descriptionPane=null;

	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerJFreeChart() {
		chartSetup=new ChartSetup();
	}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_IMAGE;
	}

	/**
	 * Initialisiert die Anzeige der zusätzlichen Beschreibung.
	 * @see #addDescription(URL, Consumer)
	 * @see #descriptionURL
	 * @see #descriptionHelpCallback
	 * @see #descriptionPane
	 */
	private void initDescriptionPane() {
		if (descriptionPane!=null) return;
		if (descriptionURL==null) return;

		descriptionPane=new DescriptionViewer(descriptionURL,link->{
			if (link.toLowerCase().startsWith("help:") && descriptionHelpCallback!=null) {
				descriptionHelpCallback.accept(link.substring("help:".length()));
			}
		},getDescriptionCustomStyles());
	}

	/**
	 * Wird aufgerufen, wenn das Diagramm tatsächlich initialisiert werden soll.
	 * Das muss nicht im Konstruktor erfolgen (der bereits aufgerufen wird, wenn
	 * der Statistikbaum aufgebaut wird), sondern erst wenn der entsprechende
	 * Eintrag in der Baumstruktur erstmal angeklickt wurde.
	 */
	protected void firstChartRequest() {}

	/**
	 * Liefert ein optionales Panel, das unmittelbar unter dem Diagramm dargestellt wird.
	 * @return	Optionales Panel (kann <code>null</code> sein)
	 */
	protected JPanel getInfoPanel() {
		return null;
	}

	/**
	 * Konkretes Anzeigeobjekt, das über {@link #getViewer(boolean)} geliefert wird.
	 * @see #getViewer(boolean)
	 */
	private Container viewer=null;

	@Override
	public Container getViewer(boolean needReInit) {
		/* Bisherigen Viewer weiterhin verwenden? */
		if (viewer!=null && !needReInit) return viewer;

		/* Wenn nötig neues Chart anlegen? */
		if (chartPanel==null || needReInit) firstChartRequest();

		/* Evtl. Info-Panel unter Chart anfügen */
		final Container innerViewer;
		final JPanel infoPanel=getInfoPanel();
		if (infoPanel!=null) {
			final JPanel panel=new JPanel(new BorderLayout());
			panel.add(chartPanel,BorderLayout.CENTER);
			panel.add(infoPanel,BorderLayout.SOUTH);
			innerViewer=panel;
		} else {
			innerViewer=chartPanel;
		}

		/* Evtl. Erklärungs-Panel hinzufügen */
		initDescriptionPane();
		if (descriptionPane==null) return viewer=innerViewer;
		return viewer=descriptionPane.getSplitPanel(innerViewer);
	}

	@Override
	public boolean isViewerGenerated() {
		return viewer!=null;
	}

	/**
	 * Initialisierung des <code>JFreeChart</code>-Objektes.
	 * @param chart	Konkretes <code>JFreeChart</code>-Objekt, welches angezeigt werden soll.
	 */
	protected final void initChart(final JFreeChart chart) {
		this.chart=chart;
		chartPanel=new ChartPanel(
				chart,
				ChartPanel.DEFAULT_WIDTH,
				ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true, /* properties */
				false, /* save */
				true, /* print */
				true, /* zoom */
				true  /* tooltips */
				);
		chartPanel.setPopupMenu(null);

		setTheme(); /* Muss vor der folgenden Farbkonfiguration erfolgen. */

		chart.setBackgroundPaint(null);
		chartSetup.setupChart(chart);
	}

	/**
	 * Konfiguriert die Darstellung der Schriften im Diagramm.
	 */
	private void setTheme() {
		final StandardChartTheme chartTheme = (StandardChartTheme)StandardChartTheme.createJFreeTheme();

		final Font oldExtraLargeFont=chartTheme.getExtraLargeFont();
		final Font oldLargeFont=chartTheme.getLargeFont();
		final Font oldRegularFont=chartTheme.getRegularFont();
		final Font oldSmallFont=chartTheme.getSmallFont();

		final Font extraLargeFont=new Font(oldExtraLargeFont.getFamily(),oldExtraLargeFont.getStyle(),(int)Math.round(oldExtraLargeFont.getSize()*GUITools.getScaleFactor()));
		final Font largeFont=new Font(oldLargeFont.getFamily(),oldLargeFont.getStyle(),(int)Math.round(oldLargeFont.getSize()*GUITools.getScaleFactor()));
		final Font regularFont=new Font(oldRegularFont.getFamily(),oldRegularFont.getStyle(),(int)Math.round(oldRegularFont.getSize()*GUITools.getScaleFactor()));
		final Font smallFont=new Font(oldSmallFont.getFamily(),oldSmallFont.getStyle(),(int)Math.round(oldSmallFont.getSize()*GUITools.getScaleFactor()));

		chartTheme.setExtraLargeFont(extraLargeFont);
		chartTheme.setLargeFont(largeFont);
		chartTheme.setRegularFont(regularFont);
		chartTheme.setSmallFont(smallFont);

		chartTheme.apply(chart);
	}

	@Override
	public Transferable getTransferable() {
		if (chartPanel==null) firstChartRequest();
		final int imageSize=getImageSize();

		chartSetup.setUserScale(Math.max(1,Math.min(5,imageSize/750)));
		chartSetup.setupAll(chart);
		try {
			return ImageTools.getTransferable(ImageTools.drawToImage(chart,imageSize,imageSize));
		} finally {
			chartSetup.setUserScale(1);
			chartSetup.setupAll(chart);
		}
	}

	@Override
	public void copyToClipboard(final Clipboard clipboard) {
		final Transferable transferable=getTransferable();
		if (transferable!=null) clipboard.setContents(transferable,null);
	}

	@Override
	public boolean print() {
		if (chartPanel==null) firstChartRequest();

		try {
			PrinterJob pjob=PrinterJob.getPrinterJob();

			PrintRequestAttributeSet attributes=new HashPrintRequestAttributeSet();
			attributes.add(DialogTypeSelection.COMMON);

			if (!pjob.printDialog(attributes)) return false;
			pjob.setPrintable(chartPanel);
			pjob.print(attributes);
			return true;
		} catch (Exception e) {return false;}
	}

	/**
	 * Gibt an, ob das aktuelle Diagramm auch in Form einer Exceltabelle mit eingebettetem Diagramm exportiert werden kann
	 * @return	Möglichkeit zum Excel-Export
	 * @see #getTableChartFromChart()
	 */
	protected boolean canStoreExcelFile() {
		return false;
	}

	/**
	 * Liefert, im Falle, dass {@link #canStoreExcelFile()} <code>true</code> liefert,
	 * ein {@link TableChart}-Objekt, welches eine Tabelle mit eingebettetem Diagramm
	 * zum Speichern als Exceltabelle enthält.
	 * @return	Tabelle mit eingebettetem Diagramm zum Speichern als Exceltabelle (oder <code>null</code>, wenn das Speichern in diesem Format nicht möglich ist)
	 */
	public TableChart getTableChartFromChart() {
		return null;
	}

	/**
	 * Speichert das Diagramm als Exceltabelle mit eingebettetem Diagramm
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> übergeben werden.
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean storeExcelFile(final File file) {
		final TableChart tableChart=getTableChartFromChart();
		if (tableChart==null) return false;
		String title="";
		if (chart.getTitle()!=null && chart.getTitle().getText()!=null) title=chart.getTitle().getText();
		return tableChart.save(title,file);
	}

	/**
	 * Zeigt einen Datei-Speicher-Dialog an
	 * @param owner	Übergeordnetes Element
	 * @return	Ausgewählte Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	protected File showSaveDialog(final Component owner) {
		return ImageTools.showSaveDialog(owner,canStoreExcelFile());
	}

	@Override
	public void save(Component owner) {
		final File file=showSaveDialog(owner);
		if (file==null) return;

		if (!save(owner,file)) {
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveImageErrorTitle,String.format(StatisticsBasePanel.viewersSaveImageErrorInfo,file.toString()));
		}
	}

	@Override
	public void navigation(JButton button) {
	}

	@Override
	public void search(Component owner) {
	}

	@Override
	public boolean save(Component owner, File file) {
		if (chartPanel==null) firstChartRequest();

		return ImageTools.saveChart(owner,chart,file,getImageSize(),()->getTableChartFromChart());
	}

	/**
	 * Erzeugt ein Bild für den Export.
	 * @return	Bild für den Export
	 */
	private BufferedImage getBufferedImage() {
		if (chartPanel==null) firstChartRequest();
		final int imageSize=getImageSize();

		chartSetup.setUserScale(Math.max(1,Math.min(5,imageSize/750)));
		chartSetup.setupAll(chart);
		try {
			return chart.createBufferedImage(imageSize,imageSize);
		} finally {
			chartSetup.setUserScale(1);
			chartSetup.setupAll(chart);
		}
	}

	/**
	 * Hintergrund-Thread zum Speichern des Bildes
	 */
	private class SaveImageThread extends Thread {
		/** Ausgabedatei */
		private final File file;

		/**
		 * Konstruktor der Klasse
		 * @param file	Ausgabedatei
		 */
		public SaveImageThread(File file) {
			this.file=file;
			start();
		}

		/**
		 * Speichert das Bild unter dem angegebenen Dateinamen
		 * @return	Liefert <code>true</code>, wenn das Speichern erfolgreich war.
		 */
		private boolean saveImage() {
			try {ImageIO.write(getBufferedImage(),"png",file);} catch (IOException e) {return false;}
			return true;
		}

		@Override
		public void run() {
			saveImage();
		}
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		if (chartPanel==null) firstChartRequest();

		if (imagesInline) {
			/* Ausgabe als Inline-Grafik */
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			ImageIO.write(getBufferedImage(),"png",out);
			byte[] bytes = out.toByteArray();

			String base64bytes=Base64.getEncoder().encodeToString(bytes);
			bw.write("<img src=\"data:image/png;base64,"+base64bytes+"\">");
			bw.newLine();

			return nextImageNr;
		} else {
			/* Ausgabe als Datei */
			String s=mainFile.getName();
			int i=s.lastIndexOf('.');
			if (i>=0) s=s.substring(0,i);

			File bildFile=new File(mainFile.getParent(),s+String.format("%03d",nextImageNr)+".png");

			bw.write("<img src=\""+bildFile.getName()+"\">");
			bw.newLine();

			new SaveImageThread(bildFile);

			return nextImageNr+1;
		}
	}

	@Override
	public int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		if (chartPanel==null) firstChartRequest();

		String s=mainFile.getName();
		int i=s.lastIndexOf('.');
		if (i>=0) s=s.substring(0,i);

		File bildFile=new File(mainFile.getParent(),s+String.format("%03d",nextImageNr)+".png");

		bw.write("\\begin{figure}[ht]"); bw.newLine();
		bw.write("  \\parbox{\\textwidth}{\\includegraphics[width=\\textwidth]{"+bildFile.getName()+"}}"); bw.newLine();
		bw.write("\\end{figure}"); bw.newLine();
		bw.newLine();

		new SaveImageThread(bildFile);

		return nextImageNr+1;
	}

	@Override
	public int saveTypst(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		if (chartPanel==null) firstChartRequest();

		String s=mainFile.getName();
		int i=s.lastIndexOf('.');
		if (i>=0) s=s.substring(0,i);

		File bildFile=new File(mainFile.getParent(),s+String.format("%03d",nextImageNr)+".png");

		bw.write("#figure("); bw.newLine();
		bw.write("  image(\""+bildFile.getName()+"\", width: 100%),"); bw.newLine();
		bw.write(")"); bw.newLine();
		bw.newLine();

		new SaveImageThread(bildFile);

		return nextImageNr+1;
	}

	@Override
	public boolean saveDOCX(DOCXWriter doc) {
		if (chartPanel==null) firstChartRequest();

		final int imageSize=getImageSize();
		final BufferedImage image=ImageTools.drawToImage(chart,imageSize,imageSize);

		return doc.writeImage(image);
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		if (chartPanel==null) firstChartRequest();

		final int imageSize=getImageSize();
		final BufferedImage image=ImageTools.drawToImage(chart,imageSize,imageSize);

		return pdf.writeImageFullWidth(image);
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_UNZOOM: return true;
		case CAN_DO_COPY: return true;
		case CAN_DO_PRINT: return true;
		case CAN_DO_SAVE: return true;
		default: return false;
		}
	}

	@Override
	public JButton[] getAdditionalButton() {
		final boolean excel=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.EXCEL);
		final boolean pdf=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.PDF);

		int count=0;
		if (excel) count++;
		if (pdf) count++;

		if (count>1) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarOpenTable);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarOpenTableHint);
			button.setIcon(SimToolsImages.OPEN.getIcon());
			button.addActionListener(e->{
				final JPopupMenu menu=new JPopupMenu();
				JMenuItem item;
				if (excel) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarExcel));
					item.setIcon(SimToolsImages.SAVE_TABLE_EXCEL.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarExcelHint);
					item.addActionListener(ev->openExcel());
				}
				if (pdf) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarPDF));
					item.setIcon(SimToolsImages.SAVE_PDF.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarPDFHint);
					item.addActionListener(ev->openPDF(SwingUtilities.getWindowAncestor(viewer)));
				}
				menu.show(button,0,button.getHeight());

			});
			return new JButton[]{button};
		}

		if (excel) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarExcel);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarExcel);
			button.setIcon(SimToolsImages.SAVE_TABLE_EXCEL.getIcon());
			button.addActionListener(e->openExcel());
			return new JButton[]{button};
		}

		if (pdf) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarPDF);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarPDFHint);
			button.setIcon(SimToolsImages.SAVE_PDF.getIcon());
			button.addActionListener(e->openPDF(SwingUtilities.getWindowAncestor(viewer)));
			return new JButton[]{button};
		}

		return null;
	}

	/**
	 * Wird aufgerufen, um eine externe Datei (mit dem Standardprogramm) zu öffnen.
	 * @param file	Zu öffnende Datei
	 * @throws IOException	Kann ausgelöst werden, wenn die Datei nicht geöffnet werden konnte
	 */
	protected void openExternalFile(final File file) throws IOException {
		Desktop.getDesktop().open(file);
	}

	/**
	 * Öffnet das Diagramm (über eine temporäre Datei) mit Excel
	 */
	private void openExcel() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".xlsx");
			if (ImageTools.storeExcelFile(chart,()->getTableChartFromChart(),file)) {
				file.deleteOnExit();
				openExternalFile(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Öffnet den Text (über eine temporäre Datei) als pdf
	 * @param owner	Übergeordnete Komponente für die eventuelle Anzeige von Dialogen
	 */
	private void openPDF(final Component owner) {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".pdf");

			final PDFWriter pdf=new PDFWriter(owner,new ReportStyle());
			if (!pdf.systemOK) return;
			if (!savePDF(pdf)) return;
			if (!pdf.save(file)) return;

			file.deleteOnExit();
			openExternalFile(file);
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	@Override
	public String[] ownSettingsName() {
		ChartSetup chartSetup=null;
		if (getChartSetupCallback!=null) chartSetup=getChartSetupCallback.get();
		if (chartSetup==null) {
			return new String[] {StatisticsBasePanel.viewersSaveImageSizePrompt};
		} else {
			return new String[] {StatisticsBasePanel.viewersChartSetupTitle};
		}
	}

	@Override
	public Icon[] ownSettingsIcon() {
		ChartSetup chartSetup=null;
		if (getChartSetupCallback!=null) chartSetup=getChartSetupCallback.get();
		if (chartSetup==null) {
			return new Icon[] {SimToolsImages.STATISTICS_DIAGRAM_PICTURE.getIcon()};
		} else {
			return new Icon[] {new ImageIcon(StatisticTreeCellRenderer.getImageViewerIcon(getImageType()))};
		}
	}

	/**
	 * Zeigt einen Dialog zur Veränderung der Bild-Speichergröße an.
	 * @param owner	Übergeordnetes Element
	 * @return	Gibt <code>true</code> zurück, wenn die Konfiguration erfolgreich verändert wurde
	 */
	private boolean changeImageSize(final JPanel owner) {
		String size=""+getImageSize();
		while (true) {
			size=(String)JOptionPane.showInputDialog(owner,StatisticsBasePanel.viewersSaveImageSizePrompt,StatisticsBasePanel.viewersSaveImageSizeTitle,JOptionPane.PLAIN_MESSAGE,null,null,size);
			if (size==null) return true;
			final Integer I=NumberTools.getInteger(size);
			if (I!=null && I>0) {
				if (setImageSizeCallback!=null) setImageSizeCallback.accept(I);
				return true;
			}
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveImageSizeErrorTitle,StatisticsBasePanel.viewersSaveImageSizeErrorInfo);
		}
	}

	/**
	 * Zeigt einen Dialog zur Veränderung der Einstellung der Diagramme an.
	 * @param owner	Übergeordnetes Element
	 * @return	Gibt <code>true</code> zurück, wenn die Konfiguration erfolgreich verändert wurde
	 */
	private boolean changeChartSetup(final JPanel owner) {
		final ChartSetupDialog dialog=new ChartSetupDialog(owner,getImageSize(),getChartSetupCallback.get());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (setImageSizeCallback!=null) setImageSizeCallback.accept(dialog.getSaveSize());
			final ChartSetup newChartSetup=dialog.getChartSetup();
			if (setChartSetupCallback!=null) setChartSetupCallback.accept(newChartSetup);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Aktualisiert die Diagram-Formateinstellungen in dem aktuellen Diagramm.
	 * @param newChartSetup	Neue Diagram-Formateinstellungen
	 */
	public void setChartSetup(final ChartSetup newChartSetup) {
		chartSetup.copyFrom(newChartSetup);
		if (chart!=null) chartSetup.setupChart(chart);
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		ChartSetup chartSetup=null;
		if (getChartSetupCallback!=null) chartSetup=getChartSetupCallback.get();
		if (chartSetup==null) {
			return changeImageSize(owner);
		} else {
			return changeChartSetup(owner);
		}
	}

	/**
	 * Liefert die gewählte Größe des Bildes (sowohl in x- als auch in y-Richtung).
	 * @return	Gewählte Größe des Bildes
	 */
	protected int getImageSize() {
		if (getImageSizeCallback==null) return 1000;
		return getImageSizeCallback.getAsInt();
	}

	/**
	 * Callback zum Auslesen der Bildgröße
	 * @see #setRequestImageSize(IntSupplier)
	 */
	private IntSupplier getImageSizeCallback;

	/**
	 * Callback zum Zurückschreiben der Bildgröße
	 * @see #setUpdateImageSize(IntConsumer)
	 */
	private IntConsumer setImageSizeCallback;

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {
		getImageSizeCallback=getImageSize;
	}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {
		setImageSizeCallback=setImageSize;
	}

	/**
	 * Callback zum Auslesen der Einstellungen der Diagramme
	 * @see #setRequestChartSetup(Supplier)
	 */
	private Supplier<ChartSetup> getChartSetupCallback;

	/**
	 * Callback zum Zurückschreiben der Einstellungen der Diagramme
	 * @see #setUpdateChartSetup(Consumer)
	 */
	private Consumer<ChartSetup> setChartSetupCallback;

	@Override
	public void setRequestChartSetup(final Supplier<ChartSetup> getChartSetup) {
		getChartSetupCallback=getChartSetup;
		if (getChartSetupCallback!=null) chartSetup.copyFrom(getChartSetupCallback.get());
	}

	@Override
	public void setUpdateChartSetup(final Consumer<ChartSetup> setChartSetup) {
		setChartSetupCallback=setChartSetup;
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, die html-Seite der angegebenen Adresse anzeigt.
	 * @param descriptionURL	html-Seite mit einer zusätzlichen Erklärung zu dieser Statistikseite
	 * @param descriptionHelpCallback	Handler, der Themennamen (angegeben über "help:..."-Links) zum Aufruf normaler Hilfeseiten entgegen nimmt
	 */
	protected final void addDescription(final URL descriptionURL, final Consumer<String> descriptionHelpCallback) {
		this.descriptionURL=descriptionURL;
		this.descriptionHelpCallback=descriptionHelpCallback;
	}

	/**
	 * Ermöglicht das Laden zusätzlicher Styles für die Erklärungstexte.
	 * @return	Zusätzliche Stylesheets für Erklärungstexte (kann <code>null</code> oder leer sein)
	 */
	protected String getDescriptionCustomStyles() {
		return null;
	}

	/**
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}
}
