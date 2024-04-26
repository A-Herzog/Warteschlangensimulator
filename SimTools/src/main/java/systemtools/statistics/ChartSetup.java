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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Stroke;

import javax.swing.UIManager;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.w3c.dom.Element;

import mathtools.NumberTools;
import systemtools.GUITools;

/**
 * Diese Klasse hält die Einstellungen zur Schriftarten und Farben
 * für die Statistik-Diagrammdarstellung vor.
 * @author Alexander Herzog
 */
public class ChartSetup {
	/**
	 * Allgemeiner GUI-Skalierungsfaktor
	 * @see GUITools#getScaleFactor()
	 */
	private final double systemScale;

	/**
	 * Zusätzlicher Skalierungsfaktor
	 * @see #getUserScale()
	 * @see #setUserScale(double)
	 */
	private double userScale;

	/**
	 * Schriftart für Diagrammtitel
	 */
	public Font titleFont;

	/**
	 * Schriftart für die Namen der Achsen
	 */
	public Font axisLabelFont;

	/**
	 * Schriftart für die Wertangaben an den Achsen
	 */
	public Font axisValueFont;

	/**
	 * Schriftart für die Legende
	 */
	public Font legendFont;

	/**
	 * Hintergrundfarbe 1 (kann <code>null</code> sein)
	 */
	public Color backgroundColor1;

	/**
	 * Hintergrundfarbe 2 (kann <code>null</code> sein)
	 */
	public Color backgroundColor2;

	/**
	 * Rahmenbreite
	 */
	public Stroke outlineStroke;

	/**
	 * Rahmenfarbe
	 */
	public Color outlineColor;

	/**
	 * Erfolgt die Darstellung im Dark-Modus?
	 */
	private final boolean isDark;

	/**
	 * Konstruktor der Klasse
	 */
	public ChartSetup() {
		systemScale=GUITools.getScaleFactor();
		userScale=1;
		final Color textBackground=UIManager.getColor("TextField.background");
		isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
		reset();
	}

	/**
	 * Stellt alle Einstellungen auf die Basiswerte zurück.
	 */
	public void reset() {
		final StandardChartTheme chartTheme=(StandardChartTheme)StandardChartTheme.createJFreeTheme();

		final Font extraLargeFont=chartTheme.getExtraLargeFont();
		final Font largeFont=chartTheme.getLargeFont();
		final Font regularFont=chartTheme.getRegularFont();
		/* final Font smallFont=chartTheme.getSmallFont(); */

		titleFont=changeSize(extraLargeFont,-4);
		axisLabelFont=largeFont;
		axisValueFont=regularFont;
		legendFont=regularFont;

		backgroundColor1=new Color(0xFA,0xFA,0xFF);
		backgroundColor2=new Color(0xEA,0xEA,0xFF);

		outlineStroke=Plot.DEFAULT_OUTLINE_STROKE;
		outlineColor=(Color)Plot.DEFAULT_OUTLINE_PAINT;
	}

	/**
	 * Überträgt die Daten aus einem anderen {@link ChartSetup}-Objekt in dieses.
	 * @param source	Quelle für das Kopieren
	 */
	public void copyFrom(final ChartSetup source) {
		if (source==null) return;

		titleFont=source.titleFont;
		axisLabelFont=source.axisLabelFont;
		axisValueFont=source.axisValueFont;
		legendFont=source.legendFont;

		backgroundColor1=source.backgroundColor1;
		backgroundColor2=source.backgroundColor2;

		outlineStroke=source.outlineStroke;
		outlineColor=source.outlineColor;
	}

	/**
	 * Ändert eine Schriftgröße
	 * @param font	Ausgangsschriftart
	 * @param delta	Relative Veränderung der Schriftgröße
	 * @return	Neue Schriftart
	 */
	public static Font changeSize(final Font font, final int delta) {
		return new Font(font.getName(),font.getStyle(),font.getSize()+delta);
	}

	/**
	 * Ändert eine Schriftgröße
	 * @param font	Ausgangsschriftart
	 * @param factor	Veränderungsfaktor für die Schriftgröße
	 * @return	Neue Schriftart
	 */
	public static Font scaleSize(final Font font, final double factor) {
		return new Font(font.getName(),font.getStyle(),(int)Math.round(font.getSize()*factor));
	}

	/**
	 * Stellt eine neue Schriftgröße ein
	 * @param font	Ausgangsschriftart
	 * @param size	Neue Schriftgröße
	 * @return	Neue Schriftart
	 */
	public static Font setSize(final Font font, final int size) {
		return new Font(font.getName(),font.getStyle(),size);
	}

	/**
	 * Ändert den Stil (fett, kursiv, ...) einer Schriftart
	 * @param font	Ausgangsschriftart
	 * @param style	Neuer Stil
	 * @return		Neue Schriftart
	 */
	public static Font setStyle(final Font font, final int style) {
		return new Font(font.getName(),style,font.getSize());
	}

	/**
	 * Berechnet aus ein oder zwei Farbobjekten eine Zeichenfarbe
	 * (Farbobjekt oder Farbverlauf)
	 * @param color1	Farbe 1 (kann <code>null</code> sein)
	 * @param color2	Farbe 2 (kann <code>null</code> sein)
	 * @return	Farbobjekt oder Farbverlauf
	 */
	private Paint getPaint(final Color color1, final Color color2) {
		if (color1==null) {
			if (color2==null) return Color.GRAY;
			return color2;
		} else {
			if (color2==null) return color1;
			return new GradientPaint(1,0,color1,1,150,color2);
		}
	}

	/**
	 * Stellt die Basisdaten für ein Diagramm ein.
	 * @param chart	Diagramm
	 */
	public void setupChart(final JFreeChart chart) {
		final double scale=(userScale==1.0)?systemScale:userScale;
		final Plot plot=chart.getPlot();
		final LegendTitle legend=chart.getLegend();

		if (isDark) {
			plot.setBackgroundPaint(Color.DARK_GRAY);
		} else {
			plot.setBackgroundPaint(getPaint(backgroundColor1,backgroundColor2));
		}
		plot.setOutlineStroke(outlineStroke);
		plot.setOutlinePaint(outlineColor);

		final TextTitle textTitle=chart.getTitle();
		if (textTitle!=null) textTitle.setFont(scaleSize(titleFont,scale));

		if (legend!=null) {
			legend.setBackgroundPaint(null);
			legend.setItemFont(scaleSize(legendFont,scale));
		}

		if (isDark) {
			chart.getTitle().setPaint(Color.LIGHT_GRAY);
			if (legend!=null) legend.setItemPaint(Color.LIGHT_GRAY);
		}
	}

	/**
	 * Stellt die Tortendiagramm-spezifischen Daten ein.
	 * @param pie	Tortendiagramm
	 */
	public void setupPiePlot(final PiePlot<?> pie) {
		final double scale=(userScale==1.0)?systemScale:userScale;
		pie.setLabelFont(scaleSize(legendFont,scale));
	}

	/**
	 * Stellt die Schriftgrößen für eine Achse ein.
	 * @param axis	Achse
	 */
	public void setupAxis(final ValueAxis axis) {
		final double scale=(userScale==1.0)?systemScale:userScale;
		axis.setLabelFont(scaleSize(axisLabelFont,scale));
		axis.setTickLabelFont(scaleSize(axisValueFont,scale));

		if (isDark) {
			axis.setAxisLinePaint(Color.LIGHT_GRAY);
			axis.setLabelPaint(Color.LIGHT_GRAY);
			axis.setTickLabelPaint(Color.LIGHT_GRAY);
			axis.setTickMarkPaint(Color.LIGHT_GRAY);
		}
	}

	/**
	 * Stellt die Schriftgrößen für eine Achse ein.
	 * @param axis	Achse
	 */
	public void setupAxis(final CategoryAxis axis) {
		final double scale=(userScale==1.0)?systemScale:userScale;
		axis.setLabelFont(scaleSize(axisLabelFont,scale));
		axis.setTickLabelFont(scaleSize(axisValueFont,scale));

		if (isDark) {
			axis.setAxisLinePaint(Color.LIGHT_GRAY);
			axis.setLabelPaint(Color.LIGHT_GRAY);
			axis.setTickLabelPaint(Color.LIGHT_GRAY);
			axis.setTickMarkPaint(Color.LIGHT_GRAY);
		}
	}

	/**
	 * Aktualisiert die Einstellungen für alle Diagrammparameter.
	 * @param chart	Diagramm
	 */
	public void setupAll(final JFreeChart chart) {
		setupChart(chart);
		final Plot plot=chart.getPlot();
		if (plot instanceof PiePlot) setupPiePlot((PiePlot<?>)plot);

		if (plot instanceof CategoryPlot) {
			setupAxis(((CategoryPlot)plot).getDomainAxis());
			setupAxis(((CategoryPlot)plot).getRangeAxis());
		}
		if (plot instanceof XYPlot) {
			setupAxis(((XYPlot)plot).getDomainAxis());
			setupAxis(((XYPlot)plot).getRangeAxis());
		}
		if (plot instanceof FastScatterPlot) {
			setupAxis(((FastScatterPlot)plot).getDomainAxis());
			setupAxis(((FastScatterPlot)plot).getRangeAxis());
		}
	}

	/**
	 * Versucht eine Zeichenkette als Farbe zu interpretieren
	 * @param content	Zeichenkette
	 * @return	Farbwert oder <code>null</code>, wenn die Zeichenkette leer ist oder nicht als Farbe interpretiert werden konnte
	 * @see #saveColor(Color)
	 */
	public static Color loadColor(final String content) {
		if (content==null || content.isBlank()) return null;
		final String[] parts=content.split(",");
		Integer C1=null, C2=null, C3=null;
		if (parts.length==3) {
			C1=NumberTools.getNotNegativeInteger(parts[0]);
			C2=NumberTools.getNotNegativeInteger(parts[1]);
			C3=NumberTools.getNotNegativeInteger(parts[2]);
		}
		if (C1==null || C1>255 || C2==null || C2>255 || C3==null || C3>255)	return null;
		return new Color(C1,C2,C3);
	}

	/**
	 * Lädt eine Schriftart aus einem Attribut eines XML-Elements
	 * @param element	XML-Element
	 * @param attrName	Name des XML-Elements
	 * @param template	Vorlage für die Daten (wird verwenden, wenn keine Daten geladen werden konnten)
	 * @return	Neue Schriftart
	 */
	private static Font loadFont(final Element element, final String attrName, final Font template) {
		final Long size=NumberTools.getPositiveLong(element.getAttribute(attrName+"Size"));
		final Integer style=NumberTools.getNotNegativeInteger(element.getAttribute(attrName+"Style"));

		return new Font(template.getName(),(style==null)?template.getStyle():style,(size==null)?template.getSize():size.intValue());
	}

	/**
	 * Lädt eine Farbe aus einem Attribut eines XML-Elements
	 * @param element	XML-Element
	 * @param attrName	Name des XML-Elements
	 * @param template	Vorlage für die Daten (wird verwenden, wenn keine Daten geladen werden konnten)
	 * @return	Neue Farbe
	 */
	private static Color loadColor(final Element element, final String attrName, final Color template) {
		final String attrValue=element.getAttribute(attrName);
		if (attrValue.equalsIgnoreCase("null")) return null;
		final Color color=loadColor(attrValue);
		return (color==null)?template:color;
	}

	/**
	 * Lädt eine Linienbreite aus einem Attribut eines XML-Elements
	 * @param element	XML-Element
	 * @param attrName	Name des XML-Elements
	 * @param template	Vorlage für die Daten (wird verwenden, wenn keine Daten geladen werden konnten)
	 * @return	Neue Linienbreite
	 */
	private static Stroke loadStroke(final Element element, final String attrName, final Stroke template) {
		final Double width=NumberTools.getPositiveDouble(attrName);
		if (width==null) return template;
		return new BasicStroke(width.floatValue());
	}

	/**
	 * Wandelt eine Farbe in eine speicherbare Zeichenkette um
	 * @param color	Zu speichernde Farbe
	 * @return	Zeichenkette, die der Farbe entspricht
	 * @see #loadColor(String)
	 */
	public static String saveColor(final Color color) {
		return ""+color.getRed()+","+color.getGreen()+","+color.getBlue();
	}

	/**
	 * Speichert eine Schriftart in einem Attribut eines XML-Elements
	 * @param element	XML-Element
	 * @param attrName	Name des XML-Elements
	 * @param font	Zu speichernde Schriftart
	 */
	private static void saveFont(final Element element, final String attrName, final Font font) {
		element.setAttribute(attrName+"Size",""+font.getSize());
		element.setAttribute(attrName+"Style",""+font.getStyle());
	}

	/**
	 * Speichert eine Farbe in einem Attribut eines XML-Elements
	 * @param element	XML-Element
	 * @param attrName	Name des XML-Elements
	 * @param color	Zu speichernde Farbe
	 */
	private static void saveColor(final Element element, final String attrName, final Color color) {
		if (color==null) {
			element.setAttribute(attrName,"null");
		} else {
			element.setAttribute(attrName,saveColor(color));
		}
	}

	/**
	 * Speichert eine Linienbreite in einem Attribut eines XML-Elements
	 * @param element	XML-Element
	 * @param attrName	Name des XML-Elements
	 * @param stroke	Zu speichernde Linienbreite
	 */
	private static void saveStroke(final Element element, final String attrName, final Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			element.setAttribute(attrName,NumberTools.formatSystemNumber(((BasicStroke)stroke).getLineWidth()));
		}
	}

	/**
	 * Lädt die gesamten Einstellungen aus den Attributes eines XML-Elements
	 * @param element	XML-Element aus dem die Daten geladen werden sollen
	 * @see #saveToXML(Element)
	 */
	public void loadFromXML(final Element element) {
		titleFont=loadFont(element,"Title",titleFont);
		axisLabelFont=loadFont(element,"Axis1",axisLabelFont);
		axisValueFont=loadFont(element,"Axis2",axisValueFont);
		legendFont=loadFont(element,"Legend",legendFont);

		backgroundColor1=loadColor(element,"Background1",backgroundColor1);
		backgroundColor2=loadColor(element,"Background2",backgroundColor2);

		outlineStroke=loadStroke(element,"OutlineWidth",outlineStroke);
		outlineColor	=loadColor(element,"OutlineColor",outlineColor);
	}

	/**
	 * Speichert die gesamten Einstellungen in den Attributen eines XML-Elements
	 * @param element	XML-Element in dem die Daten gespeichert werden sollen
	 * @see #loadFromXML(Element)
	 */
	public void saveToXML(final Element element) {
		final ChartSetup defaults=new ChartSetup();

		if (!titleFont.equals(defaults.titleFont)) saveFont(element,"Title",titleFont);
		if (!axisLabelFont.equals(defaults.axisLabelFont)) saveFont(element,"Axis1",axisLabelFont);
		if (!axisValueFont.equals(defaults.axisValueFont)) saveFont(element,"Axis2",axisValueFont);
		if (!legendFont.equals(defaults.legendFont)) saveFont(element,"Legend",legendFont);

		if (!defaults.backgroundColor1.equals(backgroundColor1)) saveColor(element,"Background1",backgroundColor1);
		if (!defaults.backgroundColor2.equals(backgroundColor2)) saveColor(element,"Background2",backgroundColor2);

		if (!outlineStroke.equals(defaults.outlineStroke)) saveStroke(element,"OutlineWidth",outlineStroke);
		if (!defaults.outlineColor.equals(outlineColor)) saveColor(element,"OutlineColor",outlineColor);
	}

	/**
	 * Liefert den aktuell eingestellten zusätzlichen Skalierungsfaktor.
	 * @return	Zusätzlicher Skalierungsfaktor
	 */
	public double getUserScale() {
		return userScale;
	}

	/**
	 * Stellt den zusätzlichen Skalierungsfaktor ein.
	 * @param userScale	Zusätzlicher Skalierungsfaktor
	 */
	public void setUserScale(final double userScale) {
		this.userScale=userScale;
	}
}
