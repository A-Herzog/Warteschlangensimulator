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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
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

import mathtools.NumberTools;
import mathtools.distribution.swing.JGetImage;
import systemtools.ImageTools;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt eine Implementierung des <code>StatisticViewer</code>-Interfaces zur
 * Anzeige von Diagrammen dar und verwendet dabei optional ein bestehendes <code>JPanel</code>-Objekt.
 * @author Alexander Herzog
 */
public class StatisticViewerImage implements StatisticViewer, Printable {
	private JPanel panel;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerImage</code>
	 * Soll das Bild gezeichnet werden, so wird <code>paintImage</code> aufgerufen.
	 * @see #paintImage(Graphics)
	 */
	public StatisticViewerImage() {
		panel=new ImagePanel();
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerImage</code>.
	 * @param panel	Zu verwendendes <code>JPanel</code>-Objekt
	 * @see #panelNeeded()
	 */
	public StatisticViewerImage(JPanel panel) {
		this.panel=panel;
	}

	/**
	 * Setzt das anzuzeigende Panel
	 * @param panel	Zu verwendendes <code>JPanel</code>-Objekt
	 */
	public void setPanel(JPanel panel) {
		this.panel=panel;
	}

	/**
	 * Wird <code>StatisticViewerImage</code> mit <code>null</code> als Parameter aufgerufen,
	 * so wird diese Funktion aufgerufen, wenn das Panel tatsächlich benötigt wird. Dies erlaubt
	 * eine verzögerte Erstellung des eigentlichen Inhalts.
	 * @see #setPanel(JPanel)
	 */
	protected void panelNeeded() {}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_IMAGE;
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_LINE;
	}

	@Override
	public Container getViewer(boolean needReInit) {
		if (panel==null || needReInit) panelNeeded();
		return panel;
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		if (panel==null) panelNeeded();
		final int imageSize=getImageSize();
		final Image image=panel.createImage(imageSize,imageSize);
		Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		if (panel instanceof JGetImage) ((JGetImage)panel).paintToGraphics(g); else	panel.paint(g);
		ImageTools.copyImageToClipboard(image,imageSize,imageSize);
	}

	@Override
	public boolean print() {
		if (panel==null) panelNeeded();

		try {
			PrinterJob pjob=PrinterJob.getPrinterJob();

			PrintRequestAttributeSet attributes=new HashPrintRequestAttributeSet();
			attributes.add(DialogTypeSelection.COMMON);

			if (!pjob.printDialog()) return false;
			pjob.setPrintable(this);
			pjob.print();
			return true;
		} catch (Exception e) {return false;}
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
		if (panel==null) panelNeeded();
		if (pageIndex>0) return NO_SUCH_PAGE;
		try {
			if (panel instanceof JGetImage) ((JGetImage)panel).paintToGraphics(graphics); else panel.paint(graphics);
		} catch (Exception e) {return NO_SUCH_PAGE;}
		return PAGE_EXISTS;
	}

	private BufferedImage getImage() {
		if (panel==null) panelNeeded();
		final int imageSize=getImageSize();
		final BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		if (panel instanceof JGetImage) ((JGetImage)panel).paintToGraphics(g); else	{
			Dimension d=panel.getSize();
			panel.setSize(imageSize,imageSize);
			panel.paint(g);
			panel.setSize(d);
		}

		return image;
	}

	/**
	 * Zeigt einen Datei-Speicher-Dialog an
	 * @param owner	Übergeordnetes Element
	 * @return	Ausgewählte Datei oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	protected File showSaveDialog(final Component owner) {
		return ImageTools.showSaveDialog(owner,false);
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
	public boolean save(Component owner, File file) {
		final BufferedImage image=getImage();
		return ImageTools.saveImage(owner,image,file);
	}

	/**
	 * Zeichnet das Bild in ein {@link Graphics}-Objekt
	 * @param g	{@link Graphics}-Objekt in das das Bild gezeichnet werden soll
	 */
	protected void paintImage(Graphics g) {}

	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 487768420672802999L;

		@Override
		public void paint(Graphics g) {paintImage(g);}
	}

	private boolean saveImage(final BufferedImage image, final File imageFile) {
		try {ImageIO.write(image,"png",imageFile);} catch (IOException e) {return false;}
		return true;
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		/* Grafik erzeugen */
		if (panel==null) panelNeeded();
		final int imageSize=getImageSize();
		final BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		if (panel instanceof JGetImage) ((JGetImage)panel).paintToGraphics(g); else	panel.paint(g);

		if (imagesInline) {
			/* Ausgabe als Inline-Grafik */
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			ImageIO.write(image,"png",out);
			final byte[] bytes=out.toByteArray();
			final String base64bytes=Base64.getEncoder().encodeToString(bytes);
			bw.write("<img src=\"data:image/png;base64,"+base64bytes+"\">");
			bw.newLine();

			return nextImageNr;
		} else {
			/* Ausgabe als Datei */
			String s=mainFile.getName();
			int i=s.lastIndexOf('.');
			if (i>=0) s=s.substring(0,i);

			final File imageFile=new File(mainFile.getParent(),s+String.format("%03d",nextImageNr)+".png");

			bw.write("<img src=\""+imageFile.getName()+"\">");
			bw.newLine();

			saveImage(image,imageFile);
			return nextImageNr+1;
		}
	}

	@Override
	public int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		/* Grafik erzeugen */
		if (panel==null) panelNeeded();
		final int imageSize=getImageSize();
		final BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		if (panel instanceof JGetImage) ((JGetImage)panel).paintToGraphics(g); else	panel.paint(g);


		/* Ausgabe als Datei */
		String s=mainFile.getName();
		int i=s.lastIndexOf('.');
		if (i>=0) s=s.substring(0,i);

		final File imageFile=new File(mainFile.getParent(),s+String.format("%03d",nextImageNr)+".png");

		bw.write("\\begin{figure}[ht]"); bw.newLine();
		bw.write("  \\parbox{\\textwidth}{\\includegraphics[width=\\textwidth]{"+imageFile.getName()+"}}"); bw.newLine();
		bw.write("\\end{figure}"); bw.newLine();
		bw.newLine();

		saveImage(image,imageFile);
		return nextImageNr+1;
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_UNZOOM: return false;
		case CAN_DO_COPY: return true;
		case CAN_DO_PRINT: return true;
		case CAN_DO_SAVE: return true;
		default: return false;
		}
	}

	@Override
	public void unZoom() {}

	@Override
	public JButton[] getAdditionalButton() {
		return null;
	}

	@Override
	public boolean saveDOCX(XWPFDocument doc) {
		BufferedImage image=getImage();
		try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
			try {if (!ImageIO.write(image,"jpg",streamOut)) return false;} catch (IOException e) {return false;}
			if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_JPEG,image.getWidth(),image.getHeight())) return false;
		} catch (IOException e) {return false;}
		return true;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		return pdf.writeImage(getImage(),25);
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
			Integer I=NumberTools.getInteger(size);
			if (I!=null && I>0) {
				if (setImageSizeCallback!=null) setImageSizeCallback.accept(I);
				return true;
			}
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveImageSizeErrorTitle,StatisticsBasePanel.viewersSaveImageSizeErrorInfo);
		}
	}

	private int getImageSize() {
		if (getImageSizeCallback==null) return 1000;
		return getImageSizeCallback.getAsInt();
	}

	private IntSupplier getImageSizeCallback;
	private IntConsumer setImageSizeCallback;

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {
		getImageSizeCallback=getImageSize;
	}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {
		setImageSizeCallback=setImageSize;
	}
}
