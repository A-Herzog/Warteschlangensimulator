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
import java.awt.geom.AffineTransform;
import java.util.Objects;

import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import mathtools.TimeTools;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.tools.FlatLaFHelper;

/**
 * Ermöglicht das Zeichnen von Beschriftungen an eine Animationsdiagramm-Y-Achse
 * @author Alexander Herzog
 */
public class AxisDrawer {
	/**
	 * Welche Werte sollen an der Achse angezeigt werden?
	 */
	public enum Mode {
		/** Keine Werte anzeigen */
		OFF(0),
		/** Nur Minimum und Maximum anzeigen */
		MIN_MAX(1),
		/** Minimum, Zwischenwerte und Maximum anzeigen */
		FULL(2);

		/**
		 * Nummer des Modus (zum Speichern)
		 */
		public final int nr;

		/**
		 * Konstruktor des Enum
		 * @param nr	Nummer des Modus
		 */
		Mode(final int nr) {
			this.nr=nr;
		}

		/**
		 * Liefert den zu einer Nummer zugehörigen Modus
		 * @param nr	Nummer
		 * @return	Zugehöriger Modus (oder Fallback-Wert)
		 */
		public static Mode fromNr(final int nr) {
			for (Mode mode: values()) if (mode.nr==nr) return mode;
			return OFF;
		}

		/**
		 * Liefert den zu einer Nummer zugehörigen Modus
		 * @param nr	Nummer
		 * @return	Zugehöriger Modus (oder Fallback-Wert)
		 */
		public static Mode fromNr(final String nr) {
			final Integer I=NumberTools.getInteger(nr);
			if (I==null) return OFF;
			return fromNr(I.intValue());
		}
	}

	/**
	 * Zahlenformat der Achsenbeschriftungswerte
	 */
	private enum NumberFormat {
		/** Beschriftung mit Zahlenwerten */
		NUMBER,
		/** Beschriftung mit Zeitangaben */
		TIME
	}

	/** Zahlenformat der Achsenbeschriftungswerte */
	private NumberFormat numberFormat=NumberFormat.NUMBER;

	/** Rotations-System für die Beschriftung der y-Achse */
	private final AffineTransform transformRotate;

	/**
	 * Aktueller Minimalwert
	 */
	private double minValue;

	/**
	 * Aktueller Maximalwert
	 */
	private double maxValue;

	/**
	 * Darstellungsmodus
	 */
	private Mode mode=Mode.OFF;

	/**
	 * Müssen die Texte beim nächsten Zeichnen aktualisiert werden?
	 * @see #setAxisValues(double, double, Mode, String)
	 * @see #prepare(Graphics2D, double, int)
	 */
	private boolean needUpdateText;

	/**
	 * Anzuzeigende Werte
	 * @see #prepare(Graphics2D, double, int)
	 */
	private String[] text;

	/**
	 * Breiten der anzuzeigenden Texte
	 * @see #prepare(Graphics2D, double, int)
	 */
	private int[] textWidth;

	/**
	 * Optionale Textbeschriftung für die Achse
	 */
	private String label;

	/**
	 * Breite für die optionale Textbeschriftung für die Achse
	 * @see #label
	 * @see #prepare(Graphics2D, double, int)
	 */
	private int labelWidth;

	/**
	 * Schriftfarbe
	 * @see #prepare(Graphics2D, double, int)
	 */
	private Color fontColor;

	/**
	 * Schriftart für die Achenbeschriftung
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
	 * Höhe der Schrift {@link #axisFont} unter der Grundlinie
	 * @see #axisFont
	 */
	private int axisFontDescent;

	/**
	 * Erzwingt eine höhere Anzahl an Nachkommastellen (wird nur verwendet, wenn nur 2 Werte ausgegeben werden sollen)
	 */
	private final boolean forceMoreDigits;

	/**
	 * Abstand zwischen zwei Wertebeschriftungen
	 */
	private static final int VALUE_STEP_WIDE=50;

	/**
	 * Abstand zwischen zwei Zeitbeschriftungen
	 */
	private static final int TIME_STEP_WIDE=150;

	/**
	 * Konstruktor der Klasse
	 * @param forceMoreDigits	Erzwingt eine höhere Anzahl an Nachkommastellen (wird nur verwendet, wenn nur 2 Werte ausgegeben werden sollen)
	 */
	public AxisDrawer(final boolean forceMoreDigits) {
		transformRotate=new AffineTransform();
		transformRotate.rotate(Math.toRadians(-90));
		this.forceMoreDigits=forceMoreDigits;
	}

	/**
	 * Konstruktor der Klasse
	 */
	public AxisDrawer() {
		this(false);
	}

	/**
	 * Stellt den Minimal- und den Maximalwert ein.
	 * @param min	Minimalwert
	 * @param max	Maximalwert
	 * @param mode	Darstellungsmodus
	 * @param label	Beschriftung für die Achse (kann <code>null</code> oder leer sein)
	 */
	public void setAxisValues(final double min, final double max, Mode mode, String label) {
		if (mode==null) mode=Mode.OFF;
		if (min==max) mode=Mode.OFF;
		if (label!=null && label.isBlank()) label=null;
		if (minValue==min && maxValue==max && this.mode==mode && Objects.equals(label,this.label) && numberFormat==NumberFormat.NUMBER) return;
		numberFormat=NumberFormat.NUMBER;
		minValue=min;
		maxValue=max;
		this.mode=mode;
		this.label=label;
		needUpdateText=true;
		fontColor=FlatLaFHelper.isDark()?Color.LIGHT_GRAY:Color.BLACK;
	}

	/**
	 * Stellt den Minimalwert für eine Beschriftung mit Zeitangaben ein.
	 * @param min	Minimalwert
	 * @param mode	Darstellungsmodus
	 * @param label	Beschriftung für die Achse (kann <code>null</code> oder leer sein)
	 */
	public void setAxisTimeValues(final long min, Mode mode, String label) {
		if (mode==null) mode=Mode.OFF;
		if (min==0) mode=Mode.OFF;
		if (label!=null && label.isBlank()) label=null;
		if (minValue==min && maxValue==0 && this.mode==mode && Objects.equals(label,this.label) && numberFormat==NumberFormat.TIME) return;
		numberFormat=NumberFormat.TIME;
		minValue=min;
		maxValue=0;
		this.mode=mode;
		this.label=label;
		needUpdateText=true;
		fontColor=FlatLaFHelper.isDark()?Color.LIGHT_GRAY:Color.BLACK;
	}

	/**
	 * Generiert Zahlenwert-Beschriftungen.
	 * @param steps	Anzahl an Schritten
	 * @param forceMoreDigits	Erzwingt eine höhere Anzahl an Nachkommastellen
	 * @see NumberFormat#NUMBER
	 * @see #prepare(Graphics2D, double, int)
	 */
	private void generateAxisTextsNumbers(final int steps, final boolean forceMoreDigits) {
		boolean ok=false;
		int digits=forceMoreDigits?3:1;
		while (!ok) {
			ok=true;
			for (int i=0;i<steps;i++) {
				final double value=minValue+(maxValue-minValue)*i/(steps-1);
				final String s=NumberTools.formatNumber(value,digits);
				if (digits<3) {
					for (int j=0;j<i;j++) if (text[j].equals(s)) {ok=false; break;}
					if (!ok) {digits++; break;}
				}
				text[i]=s;
			}
		}
	}

	/**
	 * Generiert Zeit-Beschriftungen.
	 * @param steps	Anzahl an Schritten
	 * @see NumberFormat#TIME
	 * @see #prepare(Graphics2D, double, int)
	 */
	private void generateAxisTextsTimes(final int steps) {
		for (int i=0;i<steps-1;i++) {
			final double value=minValue+(maxValue-minValue)*i/(steps-1);
			text[i]=TimeTools.formatLongTime(value);
		}
		text[steps-1]="Jetzt";
	}

	/**
	 * Bereitet die Darstellung der Texte vor (Berechnung der Schriftarten usw.)
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor (zur Berechnung der Fontgröße)
	 * @param range	Zeichenbreite bzw. Höhe (zur Berechnung der Anzahl an Zwischenschritten)
	 */
	private void prepare(final Graphics2D graphics, final double zoom, final int range) {
		boolean needUpdateTextWidth=false;

		/* Farbe einstellen */
		graphics.setColor(fontColor);

		/* Font einstellen */
		if (axisFont==null || axisFontZoom!=zoom) {
			axisFont=new Font(ModelElementBox.DEFAULT_FONT_TYPE,Font.PLAIN,(int)FastMath.round(11*zoom));
			axisFontZoom=zoom;
			graphics.setFont(axisFont);
			final FontMetrics fontMetrics=graphics.getFontMetrics();
			axisFontAscent=fontMetrics.getAscent();
			axisFontDescent=fontMetrics.getDescent();
			needUpdateTextWidth=true;
		}
		graphics.setFont(axisFont);

		/* Zahlen an Achsen vorbereiten */
		if (mode!=Mode.OFF) {
			/* Anzahl an Zwischenschritten */
			final int steps;
			if (mode==Mode.FULL) {
				final int delta;
				switch (numberFormat) {
				case NUMBER: delta=VALUE_STEP_WIDE; break;
				case TIME: delta=TIME_STEP_WIDE; break;
				default: delta=VALUE_STEP_WIDE; break;
				}
				steps=(int)Math.round((range/zoom)/delta)+1;
			} else {
				steps=2;
			}

			/* Texte wenn nötig berechnen */
			if (text==null || text.length!=steps) {
				text=new String[steps];
				needUpdateText=true;
			}
			if (needUpdateText) {
				needUpdateTextWidth=true;
				switch (numberFormat) {
				case NUMBER: generateAxisTextsNumbers(steps,steps==2 && forceMoreDigits); break;
				case TIME: generateAxisTextsTimes(steps); break;
				default: generateAxisTextsNumbers(steps,steps==2 && forceMoreDigits); break;
				}
				needUpdateText=false;
			}

			/* Textbreiten wenn nötig neu berechnen */
			if (textWidth==null || textWidth.length!=steps) {
				textWidth=new int[steps];
				needUpdateTextWidth=true;
			}
			if (needUpdateTextWidth) {
				final FontMetrics fontMetrics=graphics.getFontMetrics();
				for (int i=0;i<steps;i++) {
					textWidth[i]=fontMetrics.stringWidth(text[i]);
				}
			}
		}

		/* Achsenbeschriftung vorbereiten */
		if (label!=null) {
			if (needUpdateTextWidth) {
				final FontMetrics fontMetrics=graphics.getFontMetrics();
				labelWidth=fontMetrics.stringWidth(label);
			}
		}

		needUpdateText=false;
	}

	/**
	 * Maximale Breite der Wertetexte
	 * (zur Bestimmung der x-Position der Textbeschriftung der y-Achse)
	 * @see #drawY(Graphics2D, double, Rectangle)
	 */
	private int lastMaxTextWidth;

	/**
	 * Zeichnet die y-Achsenbeschriftung
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Bereich für das Diagramm
	 */
	public void drawY(final Graphics2D graphics, final double zoom, final Rectangle rectangle) {
		if (mode==Mode.OFF && label==null) return;
		prepare(graphics,zoom,rectangle.height);

		if (mode!=Mode.OFF) {
			final int maxI=text.length-1;
			final int x=rectangle.x-(int)Math.round(zoom);
			final int y1=rectangle.y+rectangle.height;
			final int y2=rectangle.y+axisFontAscent;

			if (maxI==0) {
				graphics.drawString(text[0],x-textWidth[0],y1);
			} else {
				for (int i=0;i<=maxI;i++) {
					graphics.drawString(text[i],x-textWidth[i],y1+(y2-y1)*i/maxI);
				}
			}
		}

		if (label!=null) {
			final AffineTransform transformDefault=graphics.getTransform();
			graphics.transform(transformRotate);

			if (mode==Mode.OFF) {
				lastMaxTextWidth=0;
			} else {
				lastMaxTextWidth=0;
				for (int w: textWidth) lastMaxTextWidth=Math.max(lastMaxTextWidth,w);
			}
			final int x=rectangle.x-3*(int)Math.round(zoom)-lastMaxTextWidth-axisFontDescent;
			final int y=rectangle.y+rectangle.height/2+labelWidth/2;
			graphics.drawString(label,-y,x);

			graphics.setTransform(transformDefault);
		}
	}

	/**
	 * Zeichnet die x-Achsenbeschriftung
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Bereich für das Diagramm
	 */
	public void drawX(final Graphics2D graphics, final double zoom, final Rectangle rectangle) {
		if (mode==Mode.OFF && label==null) return;
		prepare(graphics,zoom,rectangle.width);

		if (mode!=Mode.OFF) {
			final int maxI=text.length-1;
			final int x1=rectangle.x;
			final int x2=rectangle.x+rectangle.width-textWidth[maxI];
			final int y=rectangle.y+rectangle.height+axisFontAscent;

			if (maxI==0) {
				graphics.drawString(text[0],x2,y);
			} else {
				for (int i=0;i<=maxI;i++) {
					graphics.drawString(text[i],x1+(x2-x1)*i/maxI,y);
				}
			}
		}

		if (label!=null) {
			final int y;
			if (mode==Mode.OFF) {
				y=rectangle.y+rectangle.height+axisFontAscent;
			} else {
				y=rectangle.y+rectangle.height+(axisFontAscent+axisFontDescent)+axisFontAscent;
			}
			final int x=rectangle.x+rectangle.width/2-labelWidth/2;
			graphics.drawString(label,x,y);
		}
	}

	/**
	 * Zeichnet die y-Achsenbeschriftung (von oben nach unten)
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Bereich für das Diagramm
	 */
	public void drawYInvers(final Graphics2D graphics, final double zoom, final Rectangle rectangle) {
		double d;
		d=minValue; minValue=maxValue; maxValue=d;
		drawY(graphics,zoom,rectangle);
		d=minValue; minValue=maxValue; maxValue=d;
	}

	/**
	 * Zeichnet die x-Achsenbeschriftung (von rechts nach links)
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Bereich für das Diagramm
	 */
	public void drawXInvers(final Graphics2D graphics, final double zoom, final Rectangle rectangle) {
		double d;
		d=minValue; minValue=maxValue; maxValue=d;
		drawX(graphics,zoom,rectangle);
		d=minValue; minValue=maxValue; maxValue=d;
	}
}
