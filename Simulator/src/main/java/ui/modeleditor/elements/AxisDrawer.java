/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.tools.FlatLaFHelper;

/**
 * Ermöglicht das Zeichnen von Beschriftungen an eine Animationsdiagramm-Y-Achse
 * @author Alexander Herzog
 */
public class AxisDrawer {
	/**
	 * Konstruktor der Klasse
	 */
	public AxisDrawer() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Aktueller Minimalwert
	 */
	private double minValue;

	/**
	 * Aktueller Maximalwert
	 */
	private double maxValue;

	/**
	 * Zeichenkette für den Minimalwert
	 * @see #minValue
	 */
	private String minString;

	/**
	 * Zeichenkette für den Maximalwert
	 * @see #minValue
	 */
	private String maxString;

	/**
	 * Breite von {@link #minString} (oder -1, wenn noch keine Breite berechnet wurde)
	 * @see #minString
	 * @see #prepare(Graphics2D, double)
	 */
	private int minStringWidth;

	/**
	 * Breite von {@link #maxString} (oder -1, wenn noch keine Breite berechnet wurde)
	 * @see #maxString
	 * @see #prepare(Graphics2D, double)
	 */
	private int maxStringWidth;

	/**
	 * Schriftart für die Axenbeschriftung
	 */
	private Font axisFont;

	/**
	 * Zoomfaktor zu dem {@link #axisFont} berechnet wurde
	 * @see #axisFont
	 */
	private double axisFontZoom;

	/**
	 * Höhe der Schrift {@link #axisFont} über der Grundlinie
	 * @see #axisFont
	 */
	private int axisFontAscent;

	/**
	 * Stellt den Minimal- und den Maximalwert ein.
	 * @param min	Minimalwert
	 * @param max	Maximalwert
	 */
	public void setAxisValues(final double min, final double max) {
		if (minValue==min && maxValue==max) return;
		minString=NumberTools.formatNumber(min);
		maxString=NumberTools.formatNumber(max);
		minStringWidth=-1;
		maxStringWidth=-1;
	}

	/**
	 * Bereitet die Darstellung der Texte vor (Berechnung der Schriftarten usw.)
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 */
	private void prepare(final Graphics2D graphics, final double zoom) {
		if (axisFont==null || axisFontZoom!=zoom || minStringWidth<0) {
			axisFont=new Font(ModelElementBox.DEFAULT_FONT_TYPE,Font.PLAIN,(int)FastMath.round(11*zoom));
			axisFontZoom=zoom;
			graphics.setFont(axisFont);
			final FontMetrics fontMetrics=graphics.getFontMetrics();
			axisFontAscent=fontMetrics.getAscent();
			minStringWidth=fontMetrics.stringWidth(minString);
			maxStringWidth=fontMetrics.stringWidth(maxString);
		} else {
			graphics.setFont(axisFont);
		}
		graphics.setColor(FlatLaFHelper.isDark()?Color.LIGHT_GRAY:Color.BLACK);
	}

	/**
	 * Zeichnet die y-Achsenbeschriftung
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Bereich für das Diagramm
	 */
	public void drawY(final Graphics2D graphics, final double zoom, final Rectangle rectangle) {
		if (minString==null) return;
		prepare(graphics,zoom);
		graphics.drawString(maxString,rectangle.x-1-maxStringWidth,rectangle.y+axisFontAscent);
		graphics.drawString(minString,rectangle.x-1-minStringWidth,rectangle.y+rectangle.height);
	}

	/**
	 * Zeichnet die x-Achsenbeschriftung
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Bereich für das Diagramm
	 */
	public void drawX(final Graphics2D graphics, final double zoom, final Rectangle rectangle) {
		if (minString==null) return;
		prepare(graphics,zoom);
		final int y=rectangle.y+rectangle.height+axisFontAscent;
		graphics.drawString(minString,rectangle.x,y);
		graphics.drawString(maxString,rectangle.x+rectangle.width-maxStringWidth,y);
	}
}
