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
package tools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DialogTypeSelection;

/**
 * Diese Klasse sorgt dafür, dass ein <code>BufferedImage</code> über
 * die <code>PrinterJob</code>-Klasse ausgedruckt werden kann.
 * @author Alexander Herzog
 */
public class ImagePrintable implements Printable {
	private final double x, y, width;
	private final int orientation;
	private final BufferedImage image;

	/**
	 * Konstruktor der Klasse <code>ImagePrintable</code>
	 * @param printJob	Druck-Job-Objekt
	 * @param image	Auszudruckendes Bild
	 */
	public ImagePrintable(PrinterJob printJob, BufferedImage image) {
		PageFormat pageFormat = printJob.defaultPage();
		this.x = pageFormat.getImageableX();
		this.y = pageFormat.getImageableY();
		this.width = pageFormat.getImageableWidth();
		this.orientation = pageFormat.getOrientation();
		this.image = image;
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex == 0) {
			int pWidth = 0;
			int pHeight = 0;
			if (orientation == PageFormat.PORTRAIT) {
				pWidth = (int) Math.min(width, image.getWidth());
				pHeight = pWidth * image.getHeight() / image.getWidth();
			} else {
				pHeight = (int) Math.min(width, image.getHeight());
				pWidth = pHeight * image.getWidth() / image.getHeight();
			}
			g.drawImage(image, (int) x, (int) y, pWidth, pHeight, null);
			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}

	/**
	 * Diese statische Funktion zeigt einen Drucken-Dialog an und druckt das übergeben
	 * Bild bei Bestätigung dieses Dialogs aus.<br>
	 * Eine manuelle Erstellung eines <code>PrinterJob</code> oder eines
	 * <code>ImagePrintable</code>-Objekts ist so nicht nötig.
	 * @param image	Auszudruckendes Bild
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich gedruckt werden konnte
	 */
	public static boolean print(final BufferedImage image) {
		try {
			PrinterJob pjob=PrinterJob.getPrinterJob();

			PrintRequestAttributeSet attributes=new HashPrintRequestAttributeSet();
			attributes.add(DialogTypeSelection.COMMON);
			if (!pjob.printDialog(attributes)) return false;
			pjob.setPrintable(new ImagePrintable(pjob,image));
			pjob.print();
			return true;
		} catch (Exception e) {return false;}
	}
}