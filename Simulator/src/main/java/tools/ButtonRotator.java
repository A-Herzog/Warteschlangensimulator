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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

/**
 * Ermöglicht es {@link JButton}-Objekte für den Einsatz auf {@link JToolBar}
 * mit einem um 90° gegen den Uhrzeigersinn gedrehten Text zu erzeugen.
 * Es werden dabei dieselben Zeichenfunktionen wie für die normalen Schaltflächen
 * verwendet und es wird auch - trotz Fehler in den Java-Bibliotheken - das
 * korrekte Antialiasing verwendet.
 * @author Alexander Herzog
 * @see ButtonRotator#getRotatedButton(String, Icon)
 * @see ButtonRotator#getRotatedButton(String, URL)
 */
public class ButtonRotator {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Hilfsklasse enthält nur statische Methoden und kann nicht instanziert werden.
	 */
	private ButtonRotator() {}

	/**
	 * Erstellt ein Bild (inkl. Transparenz und korrektem Antialiasing) in dem
	 * der Text und das Bild wie für ein {@link JButton} auf einem {@link JToolBar}
	 * enthalten sind.
	 * @param text	Auszugebender Text
	 * @param icon	Links neben dem Text auszugebendes Icon (kann <code>null</code> sein)
	 * @param increaseFontSize	Vergrößert die Schriftgrüße um einen Punkt
	 * @return	Bild aus Icon und Text
	 */
	public static BufferedImage getButtonImage(final String text, final Icon icon, final boolean increaseFontSize) {
		/* Fenster */
		final JFrame frame=new JFrame("");
		final Container content=frame.getContentPane();
		content.setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);

		/* Button */
		final JButton button=new JButton(text);
		if (increaseFontSize) {
			final Font f=button.getFont();
			button.setFont(new Font(f.getFontName(),f.getStyle(),f.getSize()+1));
		}
		if (icon!=null) button.setIcon(icon);
		toolbar.add(button);

		/* Fenster anpassen */
		frame.pack();

		/* Als Bild speichern */
		final Dimension size=button.getSize();
		final BufferedImage image=new BufferedImage(size.width,size.height,BufferedImage.TYPE_3BYTE_BGR);
		final Graphics g=image.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0,0,size.width,size.height); /* Das brauchen wir nur für Look&Feel Nimbus */
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		toolbar.paint(g);

		/* Bild mit Transparenz versehen */
		return makeTransparent(image); /* Muss nachträglich erfolgen. Wenn wir direkt in ein TYPE_4BYTE_ABGR Image zeichnen, klappt das Antialiasing nicht richtig. */
	}

	/**
	 * Erstellt ein Bild (inkl. Transparenz und korrektem Antialiasing) in dem
	 * der Text und das Bild wie für ein {@link JButton} auf einem {@link JToolBar}
	 * enthalten sind.
	 * @param text	Auszugebender Text
	 * @param icon	Links neben dem Text auszugebendes Icon (kann <code>null</code> sein)
	 * @return	Bild aus Icon und Text
	 */
	public static BufferedImage getButtonImage(final String text, final URL icon) {
		return getButtonImage(text,(icon==null)?null:new ImageIcon(icon),false);
	}

	/**
	 * RGBA-Farbcode für ein transparentes Pixel
	 */
	private static final int transparent=new Color(0,0,0,0).getRGB();

	/**
	 * Wandelt ein Bild ohne Transparenz in ein Bild mit Transparenz um.<br>
	 * Das linke obere Pixel wird dabei als Transparenzfarbe verwendet, d.h.
	 * eine Alpha-Umrechnung erfolgt nicht. Ein Pixel wird entweder als 0% oder 100%
	 * transparent angesehen.
	 * @param image	Bild ohne Transparenz
	 * @return	Bild mit Index-Transparenz
	 */
	public static BufferedImage makeTransparent(final BufferedImage image) {
		final int width=image.getWidth();
		final int height=image.getHeight();
		final BufferedImage newImage=new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
		newImage.getGraphics().drawImage(image,0,0,null);

		final int transparentMarker=image.getRGB(0,0);

		for(int i=0;i<width;i++) for(int j=0;j<height;j++) {
			final int c=image.getRGB(i,j);
			if (c==transparentMarker) newImage.setRGB(i,j,transparent);
		}

		return newImage;
	}

	/**
	 * Rotiert ein Bild um 90° im Uhrzeigersinn.
	 * @param image	Zu rotierendes Bild
	 * @return	Rotiertes Bild
	 */
	public static BufferedImage rotateCw(final BufferedImage image) {
		final int width=image.getWidth();
		final int height=image.getHeight();
		final BufferedImage newImage=new BufferedImage(height,width,image.getType());

		for(int i=0;i<width;i++) for(int j=0;j<height;j++) newImage.setRGB(height-1-j,i,image.getRGB(i,j));

		return newImage;
	}

	/**
	 * Rotiert ein Bild um 90° gegen den Uhrzeigersinn.
	 * @param image	Zu rotierendes Bild
	 * @return	Rotiertes Bild
	 */
	public static BufferedImage rotateCcw(final BufferedImage image) {
		final int width=image.getWidth();
		final int height=image.getHeight();
		final BufferedImage newImage=new BufferedImage(height,width,image.getType());

		for(int i=0;i<width;i++) for(int j=0;j<height;j++) newImage.setRGB(j,width-1-i,image.getRGB(i,j));

		return newImage;
	}

	/**
	 * Beschneidet ein Bild auf allen Seiten um eine feste Anzahl an Pixeln
	 * @param image	Zu beschneidendes Bild
	 * @param cropPixel	Anzahl an Pixelzeilen bzw. -spalten die oben, unten, links und rechts abgeschnitten werden sollen
	 * @return	Verkleinertes Bild
	 */
	public static BufferedImage crop(final BufferedImage image, int cropPixel) {
		final int width=image.getWidth();
		final int height=image.getHeight();
		final BufferedImage newImage=new BufferedImage(width-2*cropPixel,height-2*cropPixel,image.getType());

		newImage.getGraphics().drawImage(image,-cropPixel,-cropPixel,null);

		return newImage;
	}

	/**
	 * Erzeugt ein {@link JButton}-Objekte für den Einsatz auf einem {@link JToolBar}
	 * mit einem um 90° gegen den Uhrzeigersinn gedrehten Inhalt.
	 * @param text	Auszugebender Text
	 * @param icon	Links neben dem Text auszugebendes Icon (kann <code>null</code> sein)
	 * @return	Neue Schaltfläche
	 */
	public static JButton getRotatedButton(final String text, final URL icon) {
		final BufferedImage image=crop(rotateCcw(getButtonImage(text,icon)),2);

		final JButton button=new JButton("");
		button.setIcon(new ImageIcon(image));
		return button;
	}

	/**
	 * Erzeugt ein {@link JButton}-Objekte für den Einsatz auf einem {@link JToolBar}
	 * mit einem um 90° gegen den Uhrzeigersinn gedrehten Inhalt.
	 * @param text	Auszugebender Text
	 * @param icon	Links neben dem Text auszugebendes Icon (kann <code>null</code> sein)
	 * @return	Neue Schaltfläche
	 */
	public static JButton getRotatedButton(final String text, final Icon icon) {
		final BufferedImage image=crop(rotateCcw(getButtonImage(text,icon,false)),2);

		final JButton button=new JButton("");
		button.setIcon(new ImageIcon(image));
		return button;
	}
}
