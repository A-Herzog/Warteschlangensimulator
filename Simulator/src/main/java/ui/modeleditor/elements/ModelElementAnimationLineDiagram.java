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
package ui.modeleditor.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationTableDialog;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt ein sich während der Animation aktualisierendes Liniendiagramm an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationLineDiagram extends ModelElementAnimationDiagramBase {
	/**
	 * Darstellungsmodus für einen Graphen
	 */
	public enum LineMode {
		/** Darstellung als durchgezogene Linie */
		LINE(null,()->Language.tr("Surface.ExpressionTableModel.Dialog.Mode.Line"),test->Language.trAll("Surface.ExpressionTableModel.Dialog.Mode.Line",test)),
		/** Darstellung als Punkte */
		POINTS(null,()->Language.tr("Surface.ExpressionTableModel.Dialog.Mode.Points"),test->Language.trAll("Surface.ExpressionTableModel.Dialog.Mode.Points",test)),
		/** Darstellung als gestrichelte Linie (kurze Strichlängen) */
		DASHED_SHORT(new float[] {3},()->Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashedShort"),test->Language.trAll("Surface.ExpressionTableModel.Dialog.Mode.DashedShort",test)),
		/** Darstellung als gestrichelte Linie (mittellange Strichlängen) */
		DASHED_MEDIUM(new float[] {5},()->Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashedMedium"),test->Language.trAll("Surface.ExpressionTableModel.Dialog.Mode.DashedMedium",test)),
		/** Darstellung als gestrichelte Linie (lange Strichlängen) */
		DASHED_LONG(new float[] {7},()->Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashedLong"),test->Language.trAll("Surface.ExpressionTableModel.Dialog.Mode.DashedLong",test)),
		/** /** Darstellung als strichpunktierte Linie */
		POINT_DASH(new float[] {5,3,1,3},()->Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashDotted"),test->Language.trAll("Surface.ExpressionTableModel.Dialog.Mode.DashDotted",test));

		/**
		 * Stroke-Modus (kann <code>null</code> sein)
		 */
		public final float[] dash;

		/**
		 * Abfragefunktion für den XML-Bezeichner für den Linienstil in der aktuellen Sprache
		 */
		private final Supplier<String> xmlNameGetter;

		/**
		 * Prüffunktion für einen XML-Bezeichner für den Linienstil
		 */
		private final Predicate<String> xmlNameTester;

		/**
		 * Konstruktor des Enum
		 * @param dash	Stroke-Modus (kann <code>null</code> sein)
		 * @param xmlNameGetter	Abfragefunktion für den XML-Bezeichner für den Linienstil in der aktuellen Sprache
		 * @param xmlNameTester Prüffunktion für einen XML-Bezeichner für den Linienstil
		 */
		LineMode(final float[] dash, final Supplier<String> xmlNameGetter, final Predicate<String> xmlNameTester) {
			this.dash=dash;
			this.xmlNameGetter=xmlNameGetter;
			this.xmlNameTester=xmlNameTester;
		}

		/**
		 * Liefert den XML-Bezeichner für den Linienstil in der aktuellen Sprache.
		 * @return	XML-Bezeichner für den Linienstil in der aktuellen Sprache
		 */
		public String getXMLName() {
			return xmlNameGetter.get();
		}

		/**
		 * Liefert den Linienstil, der zu dem angegebenen XML-Bezeichner passt.
		 * @param xmlName	XML-Bezeichner
		 * @return	Zugehöriger Linienstil (oder Fallback-Wert)
		 */
		public static LineMode getFromXMLName(final String xmlName) {
			for (var lineMode: values()) if (lineMode.xmlNameTester.test(xmlName)) return lineMode;
			return LineMode.LINE;
		}
	}

	/**
	 * Repräsentiert einen einzelnen anzuzeigenden Graphen
	 */
	public static class Series {
		/** Rechenausdruck */
		public AnimationExpression expression;
		/** Minimalwert */
		public double minValue;
		/** Maximalwert */
		public double maxValue;
		/** Farbe */
		public Color color;
		/** Linienbreite */
		public int width;
		/** Linienmodus */
		public LineMode lineMode;

		/**
		 * Konstruktor
		 */
		public Series() {
			expression=new AnimationExpression("0");
			minValue=0;
			maxValue=10;
			color=Color.BLUE;
			width=1;
			lineMode=LineMode.LINE;
		}

		/**
		 * Konstruktor
		 * @param expression	Rechenausdruck
		 * @param minValue	Minimalwert
		 * @param maxValue	Maximalwert
		 * @param color	Farbe
		 * @param width	Linienbreite
		 * @param lineMode	Linienmodus
		 */
		public Series(final AnimationExpression expression, final double minValue, final double maxValue, final Color color, final int width, final LineMode lineMode) {
			this.expression=expression;
			this.minValue=minValue;
			this.maxValue=maxValue;
			this.color=color;
			this.width=width;
			this.lineMode=lineMode;
		}

		/**
		 * Konstruktor
		 * @param expression	Rechenausdruck
		 * @param minValue	Minimalwert
		 * @param maxValue	Maximalwert
		 * @param color	Farbe
		 * @param width	Linienbreite
		 * @param lineMode	Linienmodus
		 */
		public Series(final String expression, final double minValue, final double maxValue, final Color color, final int width, final LineMode lineMode) {
			this(new AnimationExpression(expression),minValue,maxValue,color,width,lineMode);
		}

		/**
		 * Copy-Konstruktor
		 * @param copySource	Zu kopierendes Ausgangsobjekt
		 */
		public Series(final Series copySource) {
			expression=new AnimationExpression(copySource.expression);
			minValue=copySource.minValue;
			maxValue=copySource.maxValue;
			color=copySource.color;
			width=copySource.width;
			lineMode=copySource.lineMode;
		}

		/**
		 * Vergleicht diesen Graphendatensatz mit einem anderen Graphendatensatz
		 * @param otherSeries	Zum Vergleich heranzuziehender Graphendatensatz
		 * @return Liefert <code>true</code>, wenn die beiden Datensätze inhaltlich identisch sind
		 */
		public boolean equalsSeries(final Series otherSeries) {
			if (otherSeries==null) return false;
			if (!expression.equalsAnimationExpression(otherSeries.expression)) return false;
			if (minValue!=otherSeries.minValue) return false;
			if (maxValue!=otherSeries.maxValue) return false;
			if (color!=otherSeries.color) return false;
			if (width!=otherSeries.width) return false;
			if (lineMode!=otherSeries.lineMode) return false;
			return true;
		}

		/**
		 * Speichert die Daten in einem xml-Element.
		 * @param node	xml-Element in das die Daten geschrieben werden sollen.
		 */
		public void save(final Element node) {
			expression.storeToXML(node);
			node.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.Minimum"),NumberTools.formatSystemNumber(minValue));
			node.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.Maximum"),NumberTools.formatSystemNumber(maxValue));
			node.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineColor"),EditModel.saveColor(color));
			node.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineWidth"),""+width);
			node.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineStyle"),lineMode.getXMLName());
		}

		/**
		 * Lädt die Daten aus einem xml-Element.
		 * @param node	xml-Element, aus dem das Datum geladen werden soll
		 * @param name	Name des xml-Elements
		 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
		 */
		public String load(final Element node, final String name) {
			Double D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.Minimum",node)));
			if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.Minimum"),name,node.getParentNode().getNodeName());
			minValue=D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.Maximum",node)));
			if (D==null || D<=minValue) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.Maximum"),name,node.getParentNode().getNodeName());
			maxValue=D;

			color=EditModel.loadColor(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineColor",node));
			if (color==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineColor"),name,node.getParentNode().getNodeName());

			Integer I;
			I=NumberTools.getInteger(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineWidth",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineWidth"),name,node.getParentNode().getNodeName());
			width=I;
			if (width<0) {
				lineMode=LineMode.POINTS;
				width=Math.abs(width);
			}

			lineMode=LineMode.getFromXMLName(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineStyle",node));

			expression=new AnimationExpression();
			expression.loadFromXML(node);

			return null;
		}
	}

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #recordedDrawValues}
	 * {@link #recordedDrawValues}, {@link #recordedDrawValuesHeight}
	 * und {@link #recordedTimeStamps} zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Während der Animation aufgezeichnete Werte
	 */
	private List<double[]> recordedValues;

	/**
	 * Während der Animation aufgezeichnete Werte umgerechnet in Bildschirm-Koordinaten
	 */
	private List<Integer[]> recordedDrawValues;

	/**
	 * Höhe des Zeichenbereichs, um {@link #recordedValues} in
	 * {@link #recordedDrawValues} umzurechnen
	 */
	private double recordedDrawValuesHeight;

	/**
	 * Zeitpunkte zu denen {@link #recordedValues} erfasst wurden
	 * @see #recordedValues
	 */
	private long[] recordedTimeStamps;

	/**
	 * Umrechnungsfaktor von Simulationszeit zu Sekunden
	 * @see #initAnimation(SimulationData)
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private long scaleToSimTime;

	/**
	 * Darzustellende Graphen
	 */
	private final List<Series> series=new ArrayList<>();

	/**
	 * Im Diagramm darzustellender Zeitbereich (in Sekunden)
	 * @see #getTimeArea()
	 * @see #setTimeArea(int)
	 */
	private long timeArea=60*5;

	/**
	 * Sollen Beschriftungen an der x-Achse angezeigt werden?
	 */
	private AxisDrawer.Mode xAxisLabels=AxisDrawer.Mode.OFF;

	/**
	 * Sollen Beschriftungen an der y-Achse angezeigt werden?
	 */
	private AxisDrawer.Mode yAxisLabels=AxisDrawer.Mode.OFF;

	/**
	 * Beschriftungstext an der y-Achse
	 */
	private String axisLabelText="";

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationLineDiagram(final EditModel model, final ModelSurface surface) {
		super(model,surface);
	}

	/**
	 * Stellt die Größe der umrandenden Box ein
	 * @param size	Größe der Box
	 */
	@Override
	public void setSize(final Dimension size) {
		super.setSize(size);
	}

	@Override
	public boolean isVisualOnly() {
		return true;
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationDiagram.Tooltip");
	}

	/**
	 * Liefert eine Liste der vorhandenen Diagramm-Einträge.
	 * @return	Liste der Diagramm-Einträge
	 */
	public List<Series> getExpressionData() {
		return series.stream().map(graph->new Series(graph)).collect(Collectors.toList());
	}

	/**
	 * Ersetzt die bisherigen Diagramm-Einträge durch eine neue Liste.
	 * @param data	Liste der neuen Diagramm-Einträge
	 */
	public void setExpressionData(final List<Series> data) {
		series.clear();
		data.stream().map(graph->new Series(graph)).forEach(series::add);
	}

	/**
	 * Gibt an, wie groß der darzustellende Bereich (in Sekunden) sein soll
	 * @return	Im Diagramm darzustellender Zeitbereich (in Sekunden)
	 */
	public long getTimeArea() {
		return timeArea;
	}

	/**
	 * Stellt ein, wie groß der darzustellende Bereich (in Sekunden) sein soll
	 * @param timeArea	Im Diagramm darzustellender Zeitbereich (in Sekunden)
	 */
	public void setTimeArea(final int timeArea) {
		if (timeArea>=1) this.timeArea=timeArea;
		fireChanged();
	}

	/**
	 * Sollen X-Achsenbeschriftungen dargestellt werden?
	 * @return	X-Achsenbeschriftungen darstellen
	 */
	public AxisDrawer.Mode getXAxisLabels() {
		return xAxisLabels;
	}

	/**
	 * Stellt ein, ob X-Achsenbeschriftungen darstellen werden sollen.
	 * @param axisLabels	X-Achsenbeschriftungen darstellen
	 */
	public void setXAxisLabels(final AxisDrawer.Mode axisLabels) {
		this.xAxisLabels=axisLabels;
		setTimeXAxis(0,AxisDrawer.Mode.OFF,null);
		fireChanged();
	}

	/**
	 * Sollen Y-Achsenbeschriftungen dargestellt werden?
	 * @return	Y-Achsenbeschriftungen darstellen
	 */
	public AxisDrawer.Mode getYAxisLabels() {
		return yAxisLabels;
	}

	/**
	 * Stellt ein, ob Y-Achsenbeschriftungen darstellen werden sollen.
	 * @param axisLabels	Y-Achsenbeschriftungen darstellen
	 */
	public void setYAxisLabels(final AxisDrawer.Mode axisLabels) {
		this.yAxisLabels=axisLabels;
		setYAxis(0,0,AxisDrawer.Mode.OFF,axisLabelText);
		fireChanged();
	}

	/**
	 * Liefert den Beschriftungstext an der y-Achse.
	 * @return	Beschriftungstext an der y-Achse
	 */
	public String getAxisLabelText() {
		return axisLabelText;
	}

	/**
	 * Stellt den Beschriftungstext an der y-Achse ein.
	 * @param axisLabelText	Beschriftungstext an der y-Achse
	 */
	public void setAxisLabelText(final String axisLabelText) {
		this.axisLabelText=(axisLabelText==null)?"":axisLabelText;
		setYAxis(0,0,AxisDrawer.Mode.OFF,axisLabelText);
		fireChanged();
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationLineDiagram)) return false;
		final ModelElementAnimationLineDiagram other=(ModelElementAnimationLineDiagram)element;

		if (series.size()!=other.series.size()) return false;
		for (int i=0;i<series.size();i++) if (!series.get(i).equalsSeries(other.series.get(i))) return false;

		if (timeArea!=other.timeArea) return false;
		if (xAxisLabels!=other.xAxisLabels) return false;
		if (yAxisLabels!=other.yAxisLabels) return false;
		if (!axisLabelText.equals(other.axisLabelText)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationLineDiagram) {
			final ModelElementAnimationLineDiagram source=(ModelElementAnimationLineDiagram)element;

			series.clear();
			source.series.stream().map(graph->new Series(graph)).forEach(series::add);

			timeArea=source.timeArea;
			xAxisLabels=source.xAxisLabels;
			yAxisLabels=source.yAxisLabels;
			axisLabelText=source.axisLabelText;

			setYAxis(0,0,AxisDrawer.Mode.OFF,axisLabelText);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationLineDiagram clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationLineDiagram element=new ModelElementAnimationLineDiagram(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Zeichnet eine einzelne Dummy-Linie während der Editor aktiv ist (und noch keine Animationsdaten vorliegen)
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param delta	Vertikale Position (gültige Werte sind 0 bis 2)
	 * @see #drawDummyDiagramLines(Graphics2D, Rectangle, double)
	 */
	private void drawDummyLine(final Graphics2D g, final Rectangle rectangle, int delta) {
		delta=Math.min(2,Math.max(0,delta));

		final int x1=rectangle.x;
		final int x2=(int)FastMath.round(rectangle.x+1.0/4.0*rectangle.width);
		final int x3=(int)FastMath.round(rectangle.x+2/4.0*rectangle.width);
		final int x4=(int)FastMath.round(rectangle.x+3/4.0*rectangle.width);

		final int y1=(int)FastMath.round(rectangle.y+3.0/4.0*rectangle.height-delta*rectangle.height/10.0);
		final int y2=(int)FastMath.round(rectangle.y+1.0/4.0*rectangle.height-delta*rectangle.height/10.0);
		final int y3=(int)FastMath.round(rectangle.y+2.0/4.0*rectangle.height-delta*rectangle.height/10.0);

		g.drawLine(x1,y1,x2,y2);
		g.drawLine(x2,y2,x3,y3);
		g.drawLine(x3,y3,x4,y2);
	}

	/**
	 * Zeichnet eine einzelne Dummy-Punktereihe während der Editor aktiv ist (und noch keine Animationsdaten vorliegen)
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param radius	Radius der Punkte (bereits mit Zoom skaliert)
	 * @param delta	Vertikale Position (gültige Werte sind 0 bis 2)
	 * @see #drawDummyDiagramLines(Graphics2D, Rectangle, double)
	 */
	private void drawDummyPoints(final Graphics2D g, final Rectangle rectangle, final int radius, int delta) {
		delta=Math.min(2,Math.max(0,delta));

		final int x1=rectangle.x;
		final int x2=(int)FastMath.round(rectangle.x+1.0/4.0*rectangle.width);
		final int x3=(int)FastMath.round(rectangle.x+2/4.0*rectangle.width);
		final int x4=(int)FastMath.round(rectangle.x+3/4.0*rectangle.width);

		final int y1=(int)FastMath.round(rectangle.y+3.0/4.0*rectangle.height-delta*rectangle.height/10.0);
		final int y2=(int)FastMath.round(rectangle.y+1.0/4.0*rectangle.height-delta*rectangle.height/10.0);
		final int y3=(int)FastMath.round(rectangle.y+2.0/4.0*rectangle.height-delta*rectangle.height/10.0);

		final int steps=(int)Math.max(2,Math.round(10*Math.min(rectangle.width/600.0,rectangle.height/200.0)));

		for (int i=0;i<steps;i++) {
			final int x=x1+(x2-x1)*i/steps;
			final int y=y1+(y2-y1)*i/steps;
			g.fillOval(x-radius,y-radius,2*radius,2*radius);
		}
		for (int i=0;i<steps;i++) {
			final int x=x2+(x3-x2)*i/steps;
			final int y=y2+(y3-y2)*i/steps;
			g.fillOval(x-radius,y-radius,2*radius,2*radius);
		}
		for (int i=0;i<=steps;i++) {
			final int x=x3+(x4-x3)*i/steps;
			final int y=y3+(y2-y3)*i/steps;
			g.fillOval(x-radius,y-radius,2*radius,2*radius);
		}
	}

	/**
	 * Zeichnet Dummy-Linien während der Editor aktiv ist (und noch keine Animationsdaten vorliegen)
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param zoom	Zoomfaktor
	 */
	private void drawDummyDiagramLines(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		if (series.size()==0) {
			g.setColor(Color.BLUE);
			g.setStroke(new BasicStroke(Math.max(1,Math.round(2*zoom))));
			drawDummyLine(g,rectangle,0);
		} else {
			for (int i=0;i<Math.min(3,series.size());i++) {
				final Series s=series.get(i);
				g.setColor(s.color);
				if (s.lineMode==LineMode.POINTS) {
					drawDummyPoints(g,rectangle,(int)Math.max(1,Math.round(s.width*zoom)),i);
				} else {
					final int width=s.width;
					final float strokeWide=Math.max(1,Math.round(width*zoom));
					final float[] baseDash=s.lineMode.dash;
					if (baseDash==null) {
						g.setStroke(new BasicStroke(strokeWide));
					} else {
						final float[] dash=new float[baseDash.length];
						final float scale=width*(float)zoom;
						for (int j=0;j<baseDash.length;j++) dash[j]=baseDash[j]*scale;
						g.setStroke(new BasicStroke(strokeWide,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash,0.0f));
					}
					drawDummyLine(g,rectangle,i);
				}
			}
		}

		setTimeXAxis(-timeArea,xAxisLabels,null);
		boolean drawYAxis=series.size()>0;
		if (drawYAxis) {
			double min=0;
			double max=0;
			Double minD=series.get(0).minValue;
			Double maxD=series.get(0).maxValue;
			if (minD!=null && maxD!=null) {min=minD; max=maxD;}
			for (int i=1;i<series.size();i++) {
				minD=series.get(i).minValue;
				maxD=series.get(i).maxValue;
				if (minD==null || minD!=min || maxD==null || maxD!=max) {drawYAxis=false; break;}
			}
			if (drawYAxis) setYAxis(min,max,yAxisLabels,axisLabelText);
		}
	}

	/**
	 * Zoomfaktor beim letzten Aufruf von
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private double lastZoom;

	/**
	 * Zeichenstile für die Datenreihen beim letzten Aufruf von
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private BasicStroke[] drawCacheStroke;

	/**
	 * Farben für die Datenreihen beim letzten Aufruf von
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private Color[] drawCacheColor;

	/**
	 * Cache des Arrays für die x-Werte für die Datenreihen beim letzten Aufruf von
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private int[] drawCacheXValues;

	/**
	 * Cache des Arrays für die x-Werte für die Datenreihen beim letzten Aufruf von
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private int[] drawCacheYValues;

	/**
	 * Cache für positive {@link Integer}-Werte in
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private static final Integer[] drawIntegersPlus=new Integer[1000];

	/**
	 * Cache für positive {@link Integer}-Werte in
	 * {@link #drawDiagramData(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private static final Integer[] drawIntegersMinus=new Integer[1000];

	static {
		for (int i=0;i<drawIntegersPlus.length;i++) drawIntegersPlus[i]=i;
		for (int i=0;i<drawIntegersMinus.length;i++) drawIntegersMinus[i]=-i;
	}

	/**
	 * Cache für das PolyLine-x-Koordinaten-Array für gestrichelte Linien
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private int[] xPointsCache=null;

	/**
	 * Cache für das PolyLine-y-Koordinaten-Array für gestrichelte Linien
	 * @see #drawDiagramData(Graphics2D, Rectangle, double)
	 */
	private int[] yPointsCache=null;

	@Override
	protected void drawDiagramData(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		if (recordedValues==null) {
			drawDummyDiagramLines(g,rectangle,zoom);
			return;
		}

		if (recordedValues.size()<2) return;

		boolean needRecalcAll=(recordedDrawValuesHeight!=rectangle.height);
		if (needRecalcAll) recordedDrawValuesHeight=rectangle.height;

		drawLock.acquireUninterruptibly();
		try {
			/* Beim ersten Aufruf: Stroke und Color vorbereiten */
			if (drawCacheStroke==null || zoom!=lastZoom) {
				lastZoom=zoom;
				drawCacheStroke=new BasicStroke[series.size()];
				for (int i=0;i<drawCacheStroke.length;i++) {
					final Series s=series.get(i);
					final int width=s.width;
					if (s.lineMode!=LineMode.POINTS) {
						final float strokeWide=Math.max(1,Math.round(width*zoom));
						final float[] baseDash=s.lineMode.dash;
						final BasicStroke stroke;
						if (baseDash==null) {
							stroke=new BasicStroke(strokeWide);
						} else {
							final float[] dash=new float[baseDash.length];
							for (int j=0;j<baseDash.length;j++) dash[j]=baseDash[j]*strokeWide;
							stroke=new BasicStroke(strokeWide,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash,0.0f);
						}
						if (i==0 || !stroke.equals(drawCacheStroke[i-1])) drawCacheStroke[i]=stroke; else drawCacheStroke[i]=null;
					}
				}
				drawCacheColor=new Color[series.size()];
				for (int i=0;i<drawCacheColor.length;i++) {
					final Color color=series.get(i).color;
					if (i==0 || !color.equals(drawCacheColor[i-1])) drawCacheColor[i]=color; else drawCacheColor[i]=null;
				}
			}

			/* Daten-Array-Cache vorbereiten */
			final int valuesLength=recordedValues.size();
			if (drawCacheYValues==null || drawCacheYValues.length<valuesLength) drawCacheYValues=new int[FastMath.max(1000,valuesLength)];
			if (drawCacheXValues==null || drawCacheXValues.length<valuesLength) drawCacheXValues=new int[FastMath.max(1000,valuesLength)];

			/* x-Positionen bestimmen */
			final double maxTime=recordedTimeStamps[recordedValues.size()-1];
			final double minTime=maxTime-timeArea*scaleToSimTime;
			final double scaleX=rectangle.width/(maxTime-minTime);
			for (int i=0;i<valuesLength;i++) drawCacheXValues[i]=rectangle.x+(int)FastMath.round((recordedTimeStamps[i]-minTime)*scaleX);

			/* Für alle Datenreihen... */
			for (int i=0;i<series.size();i++) {
				final Series s=series.get(i);
				final double min=s.minValue;
				final double max=s.maxValue;
				final double scaleY=(rectangle.height-2)/(max-min);

				/* y-Positionen bestimmen */
				for (int j=0;j<valuesLength;j++) {
					final int yInt;
					final Integer I=recordedDrawValues.get(j)[i];
					if (needRecalcAll || I==null) {
						final double d=recordedValues.get(j)[i];
						yInt=rectangle.y+1+rectangle.height-2-(int)FastMath.round((d-min)*scaleY);
						Integer J=null;
						if (yInt>=0 && yInt<drawIntegersPlus.length) J=drawIntegersPlus[yInt];
						if (yInt<0 && -yInt<drawIntegersMinus.length) J=drawIntegersMinus[-yInt];
						if (J==null) J=yInt;
						recordedDrawValues.get(j)[i]=J;
					} else {
						yInt=I.intValue();
					}
					drawCacheYValues[j]=yInt;
				}

				/* Linienfarbe und -breite */
				if (drawCacheColor[i]!=null) g.setColor(drawCacheColor[i]);
				if (drawCacheStroke[i]!=null) g.setStroke(drawCacheStroke[i]);

				/* Zeichnen */
				if (valuesLength==0) continue;
				final LineMode mode=s.lineMode;

				if (mode.dash==null) {
					/* Durchgezogene Linie oder Punkte -> einfach */
					final int width=s.width;
					final int radius=(int)Math.round(width*zoom);
					int x2=drawCacheXValues[0];
					int y2=drawCacheYValues[0];
					for (int j=1;j<valuesLength;j++) {
						final int x1=x2;
						final int y1=y2;
						x2=drawCacheXValues[j];
						y2=drawCacheYValues[j];
						if (x1==x2 && y1==y2) continue;
						if (mode!=LineMode.POINTS) {
							/* Linie */
							if (x1==x2) {
								/* Nur y-Änderung */
								g.drawLine(x1,y1,x2,y2);
							} else {
								/* x(alt)->x(neu) dann y(alt)->y(neu) */
								g.drawLine(x1,y1,x2,y1);
								g.drawLine(x2,y1,x2,y2);
							}
						} else {
							g.fillOval(x2-radius,y2-radius,2*radius,2*radius);
						}
					}
				} else {
					/* Gestrichelte Linie -> als PolyLine */
					final int maxPointsNeeded=2*drawCacheXValues.length+1;
					if (xPointsCache==null || xPointsCache.length<maxPointsNeeded) {
						xPointsCache=new int[maxPointsNeeded];
						yPointsCache=new int[maxPointsNeeded];
					}
					xPointsCache[0]=drawCacheXValues[0];
					yPointsCache[0]=drawCacheYValues[0];
					int pointsUsed=1;

					int x2=drawCacheXValues[0];
					int y2=drawCacheYValues[0];
					for (int j=1;j<valuesLength;j++) {
						final int x1=x2;
						final int y1=y2;
						x2=drawCacheXValues[j];
						y2=drawCacheYValues[j];
						if (x1==x2 && y1==y2) continue;
						if (x1==x2) {
							/* Nur y-Änderung */
							xPointsCache[pointsUsed]=x2;
							yPointsCache[pointsUsed]=y2;
							pointsUsed++;
						} else {
							/* x(alt)->x(neu) dann y(alt)->y(neu) */
							xPointsCache[pointsUsed]=x2;
							yPointsCache[pointsUsed]=y1;
							pointsUsed++;
							xPointsCache[pointsUsed]=x2;
							yPointsCache[pointsUsed]=y2;
							pointsUsed++;
						}
					}
					g.drawPolyline(xPointsCache,yPointsCache,pointsUsed);
				}
			}
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationDiagram.Name");
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationLineDiagramDialog(owner,ModelElementAnimationLineDiagram.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationLineDiagramDialog(owner,ModelElementAnimationLineDiagram.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationDiagram.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		for (int i=0;i<series.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.Set"));
			node.appendChild(sub);
			series.get(i).save(sub);
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.Range"));
		node.appendChild(sub);
		sub.setTextContent(""+timeArea);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.LabelsTime"));
		node.appendChild(sub);
		sub.setTextContent(""+xAxisLabels.nr);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.Labels"));
		node.appendChild(sub);
		sub.setTextContent(""+yAxisLabels.nr);
		if (!axisLabelText.isBlank()) sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.LabelText"),axisLabelText);
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.AnimationDiagram.XML.Set",name)) {
			final Series s=new Series();
			error=s.load(node,name);
			if (error!=null) return error;
			series.add(s);
			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.Range",name)) {
			Long L=NumberTools.getPositiveLong(content);
			if (L==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			timeArea=L;
			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.LabelsTime",name)) {
			xAxisLabels=AxisDrawer.Mode.fromNr(content);
			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.Labels",name)) {
			yAxisLabels=AxisDrawer.Mode.fromNr(content);
			axisLabelText=Language.trAllAttribute("Surface.AnimationDiagram.XML.LabelText",node);
			return null;
		}

		return null;
	}

	/**
	 * Cache für Datenwerte für die Datenreihen in
	 * {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private List<double[]> cacheDouble=new ArrayList<>();

	/**
	 * Cache für Ganzzahlwerte für die Datenreihen in
	 * {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private List<Integer[]> cacheInteger=new ArrayList<>();

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (isPreview) return false;

		if (recordedValues==null) {
			drawLock.acquireUninterruptibly();
			try {
				recordedValues=new ArrayList<>();
				recordedDrawValues=new ArrayList<>();
				recordedTimeStamps=new long[0];
			} finally {
				drawLock.release();
			}
		}

		final int cacheDoubleSize=cacheDouble.size();
		final int cacheIntegerSize=cacheInteger.size();

		final double[] data=(cacheDoubleSize==0)?new double[series.size()]:cacheDouble.remove(cacheDoubleSize-1);
		for (int i=0;i<data.length;i++) data[i]=series.get(i).expression.getAnimationValue(this,simData);

		final Integer[] drawData;
		if (cacheIntegerSize==0) {
			drawData=new Integer[data.length];
		} else {
			drawData=cacheInteger.remove(cacheIntegerSize-1);
			Arrays.fill(drawData,null);
		}

		drawLock.acquireUninterruptibly();
		try {
			int size=recordedValues.size();
			if (size>0 && recordedTimeStamps[size-1]==simData.currentTime) {
				cacheDouble.add(recordedValues.set(size-1,data));
				cacheInteger.add(recordedDrawValues.set(size-1,drawData));
			} else {
				recordedValues.add(data);
				recordedDrawValues.add(drawData);
				if (recordedDrawValues.size()>recordedTimeStamps.length) {
					if (recordedTimeStamps.length==0) {
						recordedTimeStamps=new long[1000];
					} else {
						recordedTimeStamps=Arrays.copyOf(recordedTimeStamps,2*recordedTimeStamps.length);
					}
				}
				recordedTimeStamps[recordedDrawValues.size()-1]=simData.currentTime;

				int removeCount=0;
				size=recordedValues.size();
				final long limitValue=simData.currentTime-timeArea*simData.runModel.scaleToSimTime;
				for (int i=0;i<size;i++) {
					long l=recordedTimeStamps[i];
					if (l>=limitValue) break;
					removeCount++;
				}
				if (removeCount>0) {
					for (int i=0;i<removeCount;i++) {
						cacheDouble.add(recordedValues.remove(0));
						cacheInteger.add(recordedDrawValues.remove(0));
					}
					for (int i=removeCount;i<size;i++) recordedTimeStamps[i-removeCount]=recordedTimeStamps[i];
				}
			}
		} finally {
			drawLock.release();
		}

		return true;
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		scaleToSimTime=simData.runModel.scaleToSimTime;

		recordedValues=null;
		drawCacheStroke=null;
		drawCacheColor=null;
		drawCacheXValues=null;
		drawCacheYValues=null;
		cacheDouble.clear();
		cacheInteger.clear();

		for (Series s: series) s.expression.initAnimation(this,simData);

		if (series.size()>0) {
			boolean drawYAxis=true;
			double drawYAxisMin=series.get(0).minValue;
			double drawYAxisMax=series.get(0).maxValue;
			for (int i=1;i<series.size();i++) if (drawYAxisMin!=series.get(i).minValue || drawYAxisMax!=series.get(i).maxValue) {
				drawYAxis=false;
				break;
			}
			setTimeXAxis(-timeArea,xAxisLabels,null);
			if (drawYAxis) setYAxis(drawYAxisMin,drawYAxisMax,yAxisLabels,axisLabelText);
		} else {
			setYAxis(0,0,AxisDrawer.Mode.OFF,axisLabelText);
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationDiagram";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationDiagram(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationDiagram(rect,borderColor,borderWidth,fillColor) {\n");

		sb.append("  if (typeof(fillColor)!=\"undefined\") {\n");
		sb.append("    context.fillStyle=fillColor;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("  context.strokeStyle=\"blue\";\n");
		sb.append("  context.lineWidth=2;\n");
		sb.append("  var x1=rect.x;\n");
		sb.append("  var x2=Math.round(rect.x+0.25*rect.w);\n");
		sb.append("  var x3=Math.round(rect.x+0.5*rect.w);\n");
		sb.append("  var x4=Math.round(rect.x+0.75*rect.w);\n");
		sb.append("  var y1=Math.round(rect.y+0.75*rect.h);\n");
		sb.append("  var y2=Math.round(rect.y+0.25*rect.h);\n");
		sb.append("  var y3=Math.round(rect.y+0.5*rect.h);\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(x1,y1);\n");
		sb.append("  context.lineTo(x2,y2);\n");
		sb.append("  context.lineTo(x3,y3);\n");
		sb.append("  context.lineTo(x4,y2);\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationDiagram",builder->getHTMLAnimationDiagram(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationDiagram("+rect+","+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationDiagram("+rect+","+border+","+borderWidth+","+fill+");\n");
		}
	}

	@Override
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		return simData!=null;
	}

	/**
	 * Liefert die Daten in Tabellenform für die Ausgabe einer Datentabelle während der Animation
	 * @param simData	Simulationsdatenobjekt
	 * @return	Tabelle mit den aktuellen Ausgabedaten
	 */
	private Table getAnimationRunTimeTableData(final SimulationData simData) {
		final Table table=new Table();

		final int colCount=series.size()+1;
		List<String> line=new ArrayList<>(colCount);
		line.add(Language.tr("Statistic.Viewer.Chart.Time"));
		for (int i=0;i<series.size();i++) {
			final String info;
			switch (series.get(i).expression.getMode()) {
			case Expression: info=series.get(i).expression.getExpression(); break;
			case Java: info=Language.tr("ModelDescription.Expression.Java"); break;
			case Javascript: info=Language.tr("ModelDescription.Expression.Javascript"); break;
			default: info=series.get(i).expression.getExpression(); break;
			}
			line.add(info);
		}
		table.addLine(line);

		drawLock.acquireUninterruptibly();
		try {
			for (int i=0;i<recordedDrawValues.size();i++) {
				line=new ArrayList<>(colCount);
				line.add(TimeTools.formatLongTime(recordedTimeStamps[i]*simData.runModel.scaleToSeconds));
				for (double value: recordedValues.get(i)) line.add(NumberTools.formatNumber(value));
				table.addLine(line);
			}
		} finally {
			drawLock.release();
		}

		return table;
	}

	@Override
	public void showElementAnimationStatisticsData(final Component owner, final SimulationData simData) {
		if (simData==null) return;
		new ModelElementAnimationTableDialog(owner,getContextMenuElementName()+" (id="+getId()+")",()->getAnimationRunTimeTableData(simData),this);
	}

	@Override
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
		if (simData==null) return;
		ModelElementAnimationTableDialog.buildPopupMenuItem(owner,menu,getAnimationRunTimeTableData(simData));
	}

	/**
	 * Erstellt einen Kontextmenü-Menüpunkt zur Festlegung einer Standardgröße für das Element
	 * @param x	Horizontale Größe (gemessen in 50-Pixel-Kästen)
	 * @param y	Vertikale Größe (gemessen in 50-Pixel-Kästen)
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem sizeItem(final int x, final int y) {
		final JMenuItem item=new JMenuItem(x+"x"+y);
		item.addActionListener(e->{
			setSize(new Dimension(x*50,y*50));
			fireChanged();
		});
		return item;
	}

	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		super.addContextMenuItems(owner,popupMenu,surfacePanel,point,readOnly);

		final JMenu sizesMenu=new JMenu(Language.tr("Surface.PopupMenu.DefaultSizes"));
		sizesMenu.setIcon(Images.SETUP_WINDOW_SIZE_FULL.getIcon());
		popupMenu.add(sizesMenu);

		sizesMenu.add(sizeItem(2,2));
		sizesMenu.add(sizeItem(5,5));
		sizesMenu.add(sizeItem(8,5));
		sizesMenu.add(sizeItem(12,5));
		sizesMenu.add(sizeItem(16,5));
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<series.size();i++) {
			final int index=i;
			final AnimationExpression ex=series.get(i).expression;
			if (ex.getMode()==AnimationExpression.ExpressionMode.Expression) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.Expression"),ex.getExpression(),newExpression->ex.setExpression(newExpression));
			}
			searcher.testDouble(this,String.format(Language.tr("Editor.DialogBase.Search.MinValueForExpression"),series.get(index).expression),series.get(index).minValue,newMinValue->series.get(index).minValue=newMinValue);
			searcher.testDouble(this,String.format(Language.tr("Editor.DialogBase.Search.MaxValueForExpression"),series.get(index).expression),series.get(index).maxValue,newMaxValue->series.get(index).maxValue=newMaxValue);
			searcher.testInteger(this,String.format(Language.tr("Editor.DialogBase.Search.LineWidthForExpression"),series.get(index).expression),series.get(index).width,newLineWidth->{if (newLineWidth>0) series.get(index).width=newLineWidth;});
		}
	}
}