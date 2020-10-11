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
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.datatransfer.Clipboard;
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

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DialogTypeSelection;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.title.TextTitle;

import mathtools.NumberTools;
import mathtools.TableChart;
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
		});
	}

	/**
	 * Wird aufgerufen, wenn das Diagramm tatsächlich initialisiert werden soll.
	 * Das muss nicht im Konstruktor erfolgen (der bereits aufgerufen wird, wenn
	 * der Statistikbaum aufgebaut wird), sondern erst wenn der entsprechende
	 * Eintrag in der Baumstruktur erstmal angeklickt wurde.
	 */
	protected void firstChartRequest() {}

	/**
	 * Konkretes Anzeigeobjekt, das üger {@link #getViewer(boolean)} geliefert wird.
	 * @see #getViewer(boolean)
	 */
	private Container viewer=null;

	@Override
	public Container getViewer(boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;

		if (chartPanel==null || needReInit) firstChartRequest();
		initDescriptionPane();
		if (descriptionPane==null) return viewer=chartPanel;
		return viewer=descriptionPane.getSplitPanel(chartPanel);
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

		chart.setBackgroundPaint(null);
		chart.getPlot().setBackgroundPaint(new GradientPaint(1,0,new Color(0xFA,0xFA,0xFF),1,150,new Color(0xEA,0xEA,0xFF)));

		setTheme();

		final TextTitle t=chart.getTitle();
		if (t!=null) {
			final Font font=t.getFont();
			t.setFont(new Font(font.getFontName(),Font.PLAIN,font.getSize()-4));
		}

		chart.getLegend().setBackgroundPaint(null);
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
	public void copyToClipboard(Clipboard clipboard) {
		if (chartPanel==null) firstChartRequest();
		final int imageSize=getImageSize();
		ImageTools.copyImageToClipboard(ImageTools.drawToImage(chart,imageSize,imageSize));
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
			pjob.print();
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
		return chart.createBufferedImage(imageSize,imageSize);
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
			super();
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
	public boolean saveDOCX(XWPFDocument doc) {
		if (chartPanel==null) firstChartRequest();

		final int imageSize=getImageSize();
		final BufferedImage image=ImageTools.drawToImage(chart,imageSize,imageSize);

		try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
			try {if (!ImageIO.write(image,"jpg",streamOut)) return false;} catch (IOException e) {return false;}
			if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_JPEG,image.getWidth(),image.getHeight())) return false;
		} catch (IOException e) {return false;}

		return true;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		if (chartPanel==null) firstChartRequest();

		final int imageSize=getImageSize();
		final BufferedImage image=ImageTools.drawToImage(chart,imageSize,imageSize);

		return pdf.writeImage(image,25);
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

		if (excel) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarExcel);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarExcel);
			button.setIcon(SimToolsImages.SAVE_TABLE_EXCEL.getIcon());
			button.addActionListener(e->openExcel());
			return new JButton[]{button};
		}

		return null;
	}

	/**
	 * Öffnet das Diagramm (über eine temporäre Datei) mit Excel
	 */
	private void openExcel() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".xlsx");
			if (ImageTools.storeExcelFile(chart,()->getTableChartFromChart(),file)) {
				file.deleteOnExit();
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	@Override
	public String ownSettingsName() {
		return StatisticsBasePanel.viewersSaveImageSizePrompt;
	}

	@Override
	public Icon ownSettingsIcon() {
		return SimToolsImages.STATISTICS_DIAGRAM_PICTURE.getIcon();
	}

	@Override
	public boolean ownSettings(JPanel owner) {
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
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}
}
