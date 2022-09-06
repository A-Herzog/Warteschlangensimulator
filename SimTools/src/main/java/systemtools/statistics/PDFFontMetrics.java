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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Liefert Gr��enangaben (in pt) zu verschiedenen Schriftarten
 * @author Alexander Herzog
 * @see PDFWriter
 */
public final class PDFFontMetrics {
	/**
	 * Ma�e f�r die Schriftart im Normalmodus
	 */
	private final FontMetrics metricsPlain;

	/**
	 * Ma�e f�r die Schriftart im Fett-Modus
	 */
	private final FontMetrics metricsBold;

	/**
	 * Gibt an, ob das System korrekt initialisiert werden konnte (wird vom Konstruktor gesetzt).
	 * Wenn <code>systemOK</code> auf <code>false</code> steht, sind keine weiteren Bearbeitungen m�glich.
	 */
	public final boolean systemOK;

	/**
	 * Konstruktor der Klasse
	 * @param owner �bergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird. Es darf aber <code>null</code> �bergeben werden, dann wird ein internes {@link BufferedImage} zur Ableitung der Schriftgr��en verwendet.
	 */
	public PDFFontMetrics(final Component owner) {
		/* In etwa passende AWT-Fonts laden zur sp�teren Berechnung der Breite von Texten */
		Graphics g=null;
		if (owner!=null) g=owner.getGraphics();

		/* Wenn wir ohne Bildschirmausgabe laufen */
		if (g==null) {
			BufferedImage offscreenImage=new BufferedImage(10,10,BufferedImage.TYPE_INT_BGR);
			g=offscreenImage.getGraphics();
		}

		if (g!=null) {
			metricsPlain=g.getFontMetrics(new Font(Font.SANS_SERIF,Font.PLAIN,100));
			metricsBold=g.getFontMetrics(new Font(Font.SANS_SERIF,Font.BOLD,100));
		} else {
			metricsPlain=null;
			metricsBold=null;
		}

		/* Schriftarten ok? */
		systemOK=(metricsPlain!=null && metricsBold!=null);
	}

	/**
	 * Gibt an, wie viele PT f�r Buchstabenbereiche unter der Basislinie pro Zeile hinzugef�gt werden m�ssen.
	 * @param fontSize	Schriftgr��e in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @return Anzahl an PT, die pro Zeile auf die Schriftgr��e aufaddiert werden m�ssen.
	 */
	public int getDescent(final int fontSize, final boolean fontBold) {
		if (!systemOK) return 0;
		return (int)Math.round((fontBold?metricsBold:metricsPlain).getDescent()/100.0*fontSize);
	}

	/**
	 * Gibt an, wie viele PT f�r Buchstabenbereiche unter der Basislinie pro Zeile hinzugef�gt werden m�ssen.
	 * @param font	Schriftart
	 * @return Anzahl an PT, die pro Zeile auf die Schriftgr��e aufaddiert werden m�ssen.
	 */
	public int getDescent(final ReportStyle.ReportFont font) {
		return getDescent(font.size,font.bold);
	}

	/**
	 * Berechnet die Gesamth�he einer Zeile.
	 * @param fontSize	Schriftgr��e in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @return	Gesamth�he einer Zeile
	 */
	public int getLineHeight(final int fontSize, final boolean fontBold) {
		return fontSize+getDescent(fontSize,fontBold);
	}

	/**
	 * Berechnet die Gesamth�he einer Zeile.
	 * @param font	Schriftart
	 * @return	Gesamth�he einer Zeile
	 */
	public int getLineHeight(final ReportStyle.ReportFont font) {
		return font.size+getDescent(font)+font.lineSeparation;
	}

	/**
	 * Liefert die Breite einer Zeichenkette in PT.
	 * @param text	Zeichenkette, deren Breite bestimmt werden soll
	 * @param fontSize	Schriftgr��e in PT
	 * @param fontBold	Gibt an, ob die Schrift fett oder normal ausgegeben werden soll.
	 * @return	Per AWT-Font gesch�tzte Breite der Zeichenkette
	 */
	public int getTextWidth(final String text, int fontSize, boolean fontBold) {
		if (!systemOK) return 0;
		return (int)Math.round((double)(fontBold?metricsBold:metricsPlain).stringWidth(text)*fontSize/100);
	}

	/**
	 * Liefert die Breite einer Zeichenkette in PT.
	 * @param text	Zeichenkette, deren Breite bestimmt werden soll
	 * @param font	Schriftart
	 * @return	Per AWT-Font gesch�tzte Breite der Zeichenkette
	 */
	public int getTextWidth(final String text, final ReportStyle.ReportFont font) {
		return getTextWidth(text,font.size,font.bold);
	}
}
