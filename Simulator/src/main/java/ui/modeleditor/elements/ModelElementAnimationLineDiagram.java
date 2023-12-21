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
	 * Rechenausdrücke
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<AnimationExpression> expression=new ArrayList<>();

	/**
	 * Minimalwerte
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<Double> minValue=new ArrayList<>();

	/**
	 * Maximalwerte
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<Double> maxValue=new ArrayList<>();

	/**
	 * Linienfarben
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<Color> expressionColor=new ArrayList<>();

	/**
	 * Linienbreiten
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<Integer> expressionWidth=new ArrayList<>();

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
	 * Liefert eine Liste der vorhandenen Diagramm-Einträge.<br>
	 * Jeder Diagramm-Eintrag besteht aus 5 Objekten in einem Array: Ausdruck (AnimationExpression), Minimalwert (Double), Maximalwert (Double), Linienfarbe (Color), Linienbreite (Integer).
	 * @return	Liste der Diagramm-Einträge
	 */
	public List<Object[]> getExpressionData() {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=minValue.size()) break;
			if (i>=maxValue.size()) break;
			if (i>=expressionColor.size()) break;
			if (i>=expressionWidth.size()) break;

			Object[] row=new Object[5];
			row[0]=expression.get(i);
			row[1]=minValue.get(i);
			row[2]=maxValue.get(i);
			row[3]=expressionColor.get(i);
			row[4]=expressionWidth.get(i);

			data.add(row);
		}
		return data;
	}

	/**
	 * Ersetzt die bisherigen Diagramm-Einträge durch eine neue Liste.<br>
	 * Jeder Diagramm-Eintrag besteht aus 5 Objekten in einem Array: Ausdruck (AnimationExpression), Minimalwert (Double), Maximalwert (Double), Linienfarbe (Color), Linienbreite (Integer).
	 * @param data	Liste der neuen Diagramm-Einträge
	 */
	public void setExpressionData(final List<Object[]> data) {
		expression.clear();
		minValue.clear();
		maxValue.clear();
		expressionColor.clear();
		expressionWidth.clear();

		for (Object[] row: data) if (row.length==5) {
			if (!(row[0] instanceof AnimationExpression)) continue;
			if (!(row[1] instanceof Double) && !(row[1] instanceof Integer)) continue;
			if (!(row[2] instanceof Double) && !(row[2] instanceof Integer)) continue;
			if (!(row[3] instanceof Color)) continue;
			if (!(row[4] instanceof Integer)) continue;
			expression.add((AnimationExpression)row[0]);
			if (row[1] instanceof Double) minValue.add((Double)row[1]); else minValue.add(Double.valueOf(((Integer)row[1]).intValue()));
			if (row[2] instanceof Double) maxValue.add((Double)row[2]); else maxValue.add(Double.valueOf(((Integer)row[2]).intValue()));
			expressionColor.add((Color)row[3]);
			expressionWidth.add((Integer)row[4]);
		}
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

		if (expression.size()!=other.expression.size()) return false;
		for (int i=0;i<expression.size();i++) if (!expression.get(i).equalsAnimationExpression(other.expression.get(i))) return false;
		if (minValue.size()!=other.minValue.size()) return false;
		for (int i=0;i<minValue.size();i++) {
			Double D1=minValue.get(i);
			Double D2=other.minValue.get(i);
			if (D1==null || D2==null) return false;
			double d1=D1;
			double d2=D2;
			if (d1!=d2) return false;
		}
		if (maxValue.size()!=other.maxValue.size()) return false;
		for (int i=0;i<maxValue.size();i++) {
			Double D1=maxValue.get(i);
			Double D2=other.maxValue.get(i);
			if (D1==null || D2==null) return false;
			double d1=D1;
			double d2=D2;
			if (d1!=d2) return false;
		}
		if (expressionColor.size()!=other.expressionColor.size()) return false;
		for (int i=0;i<expressionColor.size();i++) if (!expressionColor.get(i).equals(other.expressionColor.get(i))) return false;

		if (expressionWidth.size()!=other.expressionWidth.size()) return false;
		for (int i=0;i<expressionWidth.size();i++) {
			Integer I1=expressionWidth.get(i);
			Integer I2=other.expressionWidth.get(i);
			if (I1==null || I2==null) return false;
			int i1=I1;
			int i2=I2;
			if (i1!=i2) return false;
		}

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

			expression.clear();
			expression.addAll(source.expression.stream().map(ex->new AnimationExpression(ex)).collect(Collectors.toList()));
			minValue.clear();
			minValue.addAll(source.minValue);
			maxValue.clear();
			maxValue.addAll(source.maxValue);
			expressionColor.clear();
			expressionColor.addAll(source.expressionColor);
			expressionWidth.clear();
			expressionWidth.addAll(source.expressionWidth);

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
		if (expressionColor.size()==0) {
			g.setColor(Color.BLUE);
			g.setStroke(new BasicStroke(Math.max(1,Math.round(2*zoom))));
			drawDummyLine(g,rectangle,0);
		} else {
			for (int i=0;i<Math.min(3,Math.min(expressionColor.size(),expressionWidth.size()));i++) {
				g.setColor(expressionColor.get(i));
				final int width=expressionWidth.get(i);
				if (width>=0) {
					g.setStroke(new BasicStroke(Math.max(1,Math.round(width*zoom))));
					drawDummyLine(g,rectangle,i);
				} else {
					drawDummyPoints(g,rectangle,(int)Math.max(1,Math.round(-width*zoom)),i);
				}
			}
		}

		setTimeXAxis(-timeArea,xAxisLabels,null);
		boolean drawYAxis=minValue.size()>0;
		if (drawYAxis) {
			double min=0;
			double max=0;
			Double minD=minValue.get(0);
			Double maxD=maxValue.get(0);
			if (minD!=null && maxD!=null) {min=minD; max=maxD;}
			for (int i=1;i<minValue.size();i++) {
				minD=minValue.get(i);
				maxD=maxValue.get(i);
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
				drawCacheStroke=new BasicStroke[expression.size()];
				for (int i=0;i<drawCacheStroke.length;i++) {
					final int width=expressionWidth.get(i);
					if (width>=0) {
						final BasicStroke stroke=new BasicStroke(Math.max(1,Math.round(width*zoom)));
						if (i==0 || !stroke.equals(drawCacheStroke[i-1])) drawCacheStroke[i]=stroke; else drawCacheStroke[i]=null;
					}
				}
				drawCacheColor=new Color[expression.size()];
				for (int i=0;i<drawCacheColor.length;i++) {
					final Color color=expressionColor.get(i);
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
			for (int i=0;i<expression.size();i++) {
				final double min=minValue.get(i);
				final double max=maxValue.get(i);
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
				if (valuesLength>0) {
					final int width=expressionWidth.get(i);
					final int radius=(int)Math.round(-width*zoom);
					int lastIndex=0;
					for (int j=1;j<valuesLength;j++) {
						final int x1=drawCacheXValues[lastIndex];
						final int x2=drawCacheXValues[j];
						final int y1=drawCacheYValues[lastIndex];
						final int y2=drawCacheYValues[j];
						if (x1==x2 && y1==y2) continue;
						if (width>=0) {
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
						lastIndex=j;
					}
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

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.Set"));
			node.appendChild(sub);
			expression.get(i).storeToXML(sub);
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.Minimum"),NumberTools.formatSystemNumber(minValue.get(i)));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.Maximum"),NumberTools.formatSystemNumber(maxValue.get(i)));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineColor"),EditModel.saveColor(expressionColor.get(i)));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineWidth"),""+expressionWidth.get(i));
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
		if (!axisLabelText.trim().isEmpty()) sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.LabelText"),axisLabelText);
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
			Double D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.Minimum",node)));
			if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.Minimum"),name,node.getParentNode().getNodeName());
			double minValue=D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.Maximum",node)));
			if (D==null || D<=minValue) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.Maximum"),name,node.getParentNode().getNodeName());
			this.minValue.add(minValue);
			maxValue.add(D);

			final Color color=EditModel.loadColor(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineColor",node));
			if (color==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineColor"),name,node.getParentNode().getNodeName());
			expressionColor.add(color);

			Integer I;
			I=NumberTools.getInteger(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineWidth",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineWidth"),name,node.getParentNode().getNodeName());
			expressionWidth.add(I);

			final AnimationExpression ex=new AnimationExpression();
			ex.loadFromXML(node);
			expression.add(ex);

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

		final double[] data=(cacheDoubleSize==0)?new double[expression.size()]:cacheDouble.remove(cacheDoubleSize-1);
		for (int i=0;i<data.length;i++) data[i]=expression.get(i).getAnimationValue(this,simData);

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

		for (int i=0;i<expression.size();i++) {
			expression.get(i).initAnimation(this,simData);
		}

		if (expression.size()>0) {
			boolean drawYAxis=true;
			double drawYAxisMin=minValue.get(0);
			double drawYAxisMax=maxValue.get(0);
			for (int i=1;i<minValue.size();i++) if (drawYAxisMin!=minValue.get(i) || drawYAxisMax!=maxValue.get(i)) {
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

		final int colCount=expression.size()+1;
		List<String> line=new ArrayList<>(colCount);
		line.add(Language.tr("Statistic.Viewer.Chart.Time"));
		for (int i=0;i<expression.size();i++) {
			final String info;
			switch (expression.get(i).getMode()) {
			case Expression: info=expression.get(i).getExpression(); break;
			case Java: info=Language.tr("ModelDescription.Expression.Java"); break;
			case Javascript: info=Language.tr("ModelDescription.Expression.Javascript"); break;
			default: info=expression.get(i).getExpression(); break;
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

		for (int i=0;i<expression.size();i++) {
			final int index=i;
			final AnimationExpression ex=expression.get(i);
			if (ex.getMode()==AnimationExpression.ExpressionMode.Expression) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.Expression"),ex.getExpression(),newExpression->ex.setExpression(newExpression));
			}
			searcher.testDouble(this,String.format(Language.tr("Editor.DialogBase.Search.MinValueForExpression"),expression.get(index)),minValue.get(index),newMinValue->minValue.set(index,newMinValue));
			searcher.testDouble(this,String.format(Language.tr("Editor.DialogBase.Search.MaxValueForExpression"),expression.get(index)),maxValue.get(index),newMaxValue->maxValue.set(index,newMaxValue));
			searcher.testInteger(this,String.format(Language.tr("Editor.DialogBase.Search.LineWidthForExpression"),expression.get(index)),expressionWidth.get(index),newLineWidth->{if (newLineWidth>0) expressionWidth.set(index,newLineWidth);});
		}
	}
}