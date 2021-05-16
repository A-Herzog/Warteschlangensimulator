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
package systemtools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jfree.chart.JFreeChart;

import mathtools.Table;
import mathtools.TableChart;
import mathtools.distribution.swing.CommonVariables;
import swingtools.ImageIOFormatCheck;
import systemtools.statistics.PDFWriter;
import systemtools.statistics.StatisticsBasePanel;
import systemtools.statistics.XWPFDocumentPictureTools;

/**
 * Diese klasse stellt ein statische Hilfsfunktionen zum Kopieren und Speichern von Bildern zur Verfügung.
 * @author Alexander Herzog
 * @version 1.2
 */
public class ImageTools {

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt lediglich statische Hilfroutinen zur Verfügung.
	 */
	private ImageTools() {}

	/**
	 * Liefert ein {@link Transferable}-Objekt für eine Grafik
	 * (um diese per Drag&amp;Drop zu exportieren oder zu kopieren).
	 * @param image	Bild
	 * @return	Transfer-Objekt für das Bild
	 */
	public static Transferable getTransferable(final BufferedImage image) {
		return getTransferable(image,image.getWidth(),image.getHeight());
	}

	/**
	 * Kopiert ein Bild in die Zwischenablage
	 * @param image	Zu kopierendes Bild
	 */
	public static void copyImageToClipboard(final BufferedImage image) {
		copyImageToClipboard(image,image.getWidth(),image.getHeight());
	}

	/**
	 * Liefert ein {@link Transferable}-Objekt für eine Grafik
	 * (um diese per Drag&amp;Drop zu exportieren oder zu kopieren).
	 * @param image	Bild
	 * @param width	Breite des Bildes
	 * @param height	Höhe des Bildes
	 * @return	Transfer-Objekt für das Bild
	 */
	public static Transferable getTransferable(final Image image, final int width, final int height) {
		if (image==null || width<=0 || height<=0) return null;

		/* see: https://bugs.openjdk.java.net/browse/JDK-8204188 */

		final BufferedImage image2=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2=image2.createGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0,0,width,height);
		g2.drawImage(image,0,0,null);
		g2.dispose();

		return new TransferableImage(image2);
	}

	/**
	 * Kopiert ein Bild in die Zwischenablage
	 * @param image	Zu kopierendes Bild
	 * @param width	Breite des Bildes
	 * @param height	Höhe des Bildes
	 */
	public static void copyImageToClipboard(final Image image, final int width, final int height) {
		final Transferable transferable=getTransferable(image,width,height);
		if (transferable!=null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable,null);
	}

	/**
	 * Erstellt ein Bild auf Basis eines Diagramms
	 * @param chart	Diagramm das als Bild gespeichert werden soll
	 * @param width	Breite des Bildes
	 * @param height	Höhe des Bildes
	 * @return	Bild das das Diagramm enthält
	 */
	public static BufferedImage drawToImage(final JFreeChart chart, int width, int height) {
		BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2=img.createGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0,0,width,height);
		chart.draw(g2,new Rectangle2D.Double(0, 0, width, height));
		g2.dispose();
		return img;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Dateinamens zum Speichern eines Bildes an
	 * @param owner	Übergeordnetes Element (zum Ausrichten des Dialogs)
	 * @param allowXLSX	Sollen xlsx-Dateien als Zielformate angeboten werden?
	 * @return	Gewählter Dateiname oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	public static File showSaveDialog(final Component owner, final boolean allowXLSX) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveImage);
		final FileFilter jpg=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeJPG+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeGIF+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePNG+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeBMP+" (*.bmp)","bmp");
		final FileFilter tiff=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeTIFF+" (*.tiff, *.tif)","tiff","tif");
		final FileFilter docx=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeWordWithImage+" (*.docx)","docx");
		final FileFilter pdf=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		final FileFilter xlsx=allowXLSX?new FileNameExtensionFilter(Table.FileTypeExcel+" (*.xlsx)","xlsx"):null;
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		if (ImageIOFormatCheck.hasTIFF()) fc.addChoosableFileFilter(tiff);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pdf);
		if (xlsx!=null) fc.addChoosableFileFilter(xlsx);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==tiff) file=new File(file.getAbsoluteFile()+".tiff");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (xlsx!=null && fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return null;
		}

		return file;
	}

	/**
	 * Speichert eine Diagramm als Datei
	 * @param owner	Übergeordnetes Element (zum Ausrichten von Meldungsfenstern)
	 * @param chart	Zu speicherndes Diagramm
	 * @param file	 Dateiname der Datei in der das Diagramm gespeichert werden soll
	 * @param imageSize	Breite bzw. Höhe in der das Diagramm in der Bilddatei gespeichert werden soll
	 * @param tableGetterXLSX	Optionaler Getter für ein {@link TableChart}-Objekt, welches das Diagramm als xlsx-Datei bereitstellt
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public static boolean saveChart(final Component owner, final JFreeChart chart, final File file, int imageSize, final Supplier<TableChart> tableGetterXLSX) {
		String s="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) s="gif";
		if (file.getName().toLowerCase().endsWith(".bmp")) s="bmp";
		if (file.getName().toLowerCase().endsWith(".tiff")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".tif")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".docx")) s="docx";
		if (file.getName().toLowerCase().endsWith(".pdf")) s="pdf";
		if (file.getName().toLowerCase().endsWith(".xlsx")) s="xlsx";

		if (s.equals("xlsx") && tableGetterXLSX!=null) {
			return storeExcelFile(chart,tableGetterXLSX,file);
		}

		imageSize=Math.max(100,imageSize);
		/* Führt bei jpgs zu falschen Farben: BufferedImage image=chart.createBufferedImage(setup.imageSize,setup.imageSize); */
		BufferedImage image=ImageTools.drawToImage(chart,imageSize,imageSize);

		if (s.equals("docx")) {
			try (XWPFDocument doc=new XWPFDocument()) {
				try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
					try {if (!ImageIO.write(image,"png",streamOut)) return false;} catch (IOException e) {return false;}
					if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_PNG,image.getWidth(),image.getHeight())) return false;
				}
				try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);} catch (IOException e) {return false;}
				return true;
			} catch (IOException e) {return false;}
		}

		if (s.equals("pdf")) {
			final PDFWriter pdf=new PDFWriter(owner,15,10);
			if (!pdf.systemOK) return false;
			if (!pdf.writeImage(image,25)) return false;
			return pdf.save(file);
		}

		try {return ImageIO.write(image,s,file);} catch (IOException e) {return false;}
	}

	/**
	 * Speichert ein Bild als Datei
	 * @param owner	Übergeordnetes Element (zum Ausrichten von Meldungsfenstern)
	 * @param image	Zu speicherndes Bild
	 * @param file	Dateiname der Datei in der das Bild gespeichert werden soll
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public static boolean saveImage(final Component owner, final BufferedImage image , final File file) {
		String s="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) s="gif";
		if (file.getName().toLowerCase().endsWith(".bmp")) s="bmp";
		if (file.getName().toLowerCase().endsWith(".tiff")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".tif")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".docx")) s="docx";
		if (file.getName().toLowerCase().endsWith(".pdf")) s="pdf";
		if (file.getName().toLowerCase().endsWith(".xlsx")) s="xlsx";

		if (s.equals("docx")) {
			try (XWPFDocument doc=new XWPFDocument()) {
				try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
					try {if (!ImageIO.write(image,"png",streamOut)) return false;} catch (IOException e) {return false;}
					if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_PNG,image.getWidth(),image.getHeight())) return false;
				}
				try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);} catch (IOException e) {return false;}
				return true;
			} catch (IOException e) {return false;}
		}

		if (s.equals("pdf")) {
			final PDFWriter pdf=new PDFWriter(owner,15,10);
			if (!pdf.systemOK) return false;
			if (!pdf.writeImage(image,25)) return false;
			return pdf.save(file);
		}

		try {return ImageIO.write(image,s,file);} catch (IOException e) {return false;}
	}

	/**
	 * Speichert das Diagramm als Exceltabelle mit eingebettetem Diagramm
	 * @param chart	Zu speicherndes Diagramm
	 * @param tableGetterXLSX	Getter der das Diagramm als xlsx-Daten liefert
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> übergeben werden.
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	public static boolean storeExcelFile(final JFreeChart chart, final Supplier<TableChart> tableGetterXLSX, final File file) {
		if (tableGetterXLSX==null) return false;
		final TableChart tableChart=tableGetterXLSX.get();
		if (tableChart==null) return false;
		String title="";
		if (chart.getTitle()!=null && chart.getTitle().getText()!=null) title=chart.getTitle().getText();
		return tableChart.save(title,file);
	}

	/**
	 * Bild-Objekt zum Einfügen in die Zwischenablage
	 * @see ImageTools#copyImageToClipboard(Image, int, int)
	 */
	private static class TransferableImage implements Transferable{
		/** Bild für die Zwischenablage */
		private final Image image;

		/**
		 * Konstruktor der Klasse
		 * @param image	Bild für die Zwischenablage
		 */
		public TransferableImage(final Image image) {
			this.image=image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}
		@Override

		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return flavor.equals(DataFlavor.imageFlavor);
		}
		@Override
		public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
			if (flavor.equals(DataFlavor.imageFlavor)) return image; else throw new UnsupportedFlavorException(flavor);
		}
	}

	/**
	 * Wandelt ein einfaches Bild in ein {@link BufferedImage} um.
	 * @param image	Eingabgsbild
	 * @return	{@link BufferedImage} (Ist das Eingangsbild bereits ein {@link BufferedImage}, so wird es direkt zurückgeliefert)
	 */
	public static BufferedImage imageToBufferedImage(final Image image) {
		if (image instanceof BufferedImage) return (BufferedImage)image;

		final BufferedImage bufferedImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics=bufferedImage.createGraphics();
		graphics.drawImage(image,0,0,null);
		graphics.dispose();

		return bufferedImage;
	}

	/**
	 * Wandelt ein Bild in ein HTML-Base64-Inline-Bild um.
	 * @param image	Ausgangsbild
	 * @return	HTML-Base64-Inline-Bild
	 */
	public static String imageToBase64HTML(final Image image) {
		final BufferedImage bufferedImage=imageToBufferedImage(image);
		try (final ByteArrayOutputStream output=new ByteArrayOutputStream()) {
			if (!ImageIO.write(bufferedImage,"png",output)) return "";
			final String base64bytes=Base64.getEncoder().encodeToString(output.toByteArray());
			return "data:image/png;base64,"+base64bytes;
		} catch (IOException e) {
			return "";
		}
	}
}