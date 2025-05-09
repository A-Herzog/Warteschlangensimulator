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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationTableDialog;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.BrighterColor;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt ein sich w�hrend der Animation aktualisierendes Balkendiagramm an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationBarChart extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Gr��e der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #recordedValues}
	 * zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Aufgezeichnete Werte die in {@link #drawDiagramBars(Graphics2D, Rectangle, double)}
	 * gezeichnet werden sollen
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 */
	private double[] recordedValues;

	/**
	 * Liste der Rechenausdr�cke f�r die Diagramm-Eintr�ge
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<AnimationExpression> expression=new ArrayList<>();

	/**
	 * Liste der Farben f�r die Diagramm-Eintr�ge
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<Color> expressionColor=new ArrayList<>();

	/**
	 * Minimalwert (kann <code>null</code> sein, wenn der Minimalwert automatisch ermittelt werden soll)
	 * @see #getMinValue()
	 * @see #getMaxValue()
	 */
	private Double minValue;

	/**
	 * Maximalwert (kann <code>null</code> sein, wenn der Maximalwert automatisch ermittelt werden soll)
	 * @see #getMinValue()
	 * @see #getMaxValue()
	 */
	private Double maxValue;

	/**
	 * Breite der Linie
	 * @see #getBorderWidth()
	 * @see #setBorderWidth(int)
	 */
	private int borderWidth=1;

	/**
	 * Farbe der Linie
	 * @see #getBorderColor()
	 * @see #setBorderColor(Color)
	 */
	private Color borderColor=Color.BLACK;

	/**
	 * F�llfarbe des Kastens
	 * @see #getBackgroundColor()
	 * @see #setBackgroundColor(Color)
	 */
	private Color backgroundColor=new Color(240,240,240);

	/**
	 * Optionale zweite F�llfarbe des Kastens f�r Farbverl�ufe
	 * @see #getGradientFillColor()
	 * @see #setGradientFillColor(Color)
	 */
	protected Color gradientColor=null;

	/**
	 * 3D-Effekte f�r die Balken
	 * @see #isUse3D()
	 * @see #setUse3D(boolean)
	 */
	private boolean use3D=true;

	/**
	 * Cache der Bereichsf�llung-Zeichners
	 * f�r {@link #drawDiagramBars(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 */
	private GradientFill[] filler;

	/**
	 * Sollen Beschriftungen an der y-Achse angezeigt werden?
	 */
	private AxisDrawer.Mode axisLabels=AxisDrawer.Mode.OFF;

	/**
	 * Beschriftungstext an der y-Achse
	 */
	private String axisLabelText="";

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationBarChart</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationBarChart(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(75,75),Shapes.ShapeType.SHAPE_NONE);
		filler=null;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART.getIcon();
	}

	/**
	 * Stellt die Gr��e der umrandenden Box ein
	 * @param size	Gr��e der Box
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
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationBarChart.Tooltip");
	}

	/**
	 * Liefert eine Liste der vorhandenen Diagramm-Eintr�ge.<br>
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (AnimationExpression), Balkenfarbe (Color).
	 * @return	Liste der Diagramm-Eintr�ge
	 */
	public List<Object[]> getExpressionData() {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=expressionColor.size()) break;

			Object[] row=new Object[2];
			row[0]=expression.get(i);
			row[1]=expressionColor.get(i);

			data.add(row);
		}
		return data;
	}

	/**
	 * Ersetzt die bisherigen Diagramm-Eintr�ge durch eine neue Liste.<br>
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (AnimationExpression), Balkenfarbe (Color).
	 * @param data	Liste der neuen Diagramm-Eintr�ge
	 */
	public void setExpressionData(final List<Object[]> data) {
		expression.clear();
		expressionColor.clear();

		for (Object[] row: data) if (row.length==2) {
			if (!(row[0] instanceof AnimationExpression)) continue;
			if (!(row[1] instanceof Color)) continue;
			expression.add((AnimationExpression)row[0]);
			expressionColor.add((Color)row[1]);
		}
	}

	/**
	 * Liefert den Minimalwert f�r die Diagrammbalken.
	 * @return	Minimalwert (kann <code>null</code> sein, wenn der Minimalwert automatisch ermittelt werden soll)
	 */
	public Double getMinValue() {
		return minValue;
	}

	/**
	 * Stellt den Minimalwert der Diagrammbalken ein.
	 * @param minValue	Minimalwert (kann <code>null</code> sein, dann wird Minimalwert automatisch ermittelt)
	 */
	public void setMinValue(final Double minValue) {
		this.minValue=minValue;
		yAxisDrawer.setAxisValues((minValue==null)?0.0:minValue,(maxValue==null)?0.0:maxValue,(minValue==null || maxValue==null)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
		fireChanged();
	}


	/**
	 * Liefert den Maximalwert f�r die Diagrammbalken.
	 * @return	Maximalwert (kann <code>null</code> sein, wenn der Maximalwert automatisch ermittelt werden soll)
	 */
	public Double getMaxValue() {
		return maxValue;
	}

	/**
	 * Stellt den Maximalwert der Diagrammbalken ein.
	 * @param maxValue	Maximalwert (kann <code>null</code> sein, dann wird Maximalwert automatisch ermittelt)
	 */
	public void setMaxValue(final Double maxValue) {
		this.maxValue=maxValue;
		yAxisDrawer.setAxisValues((minValue==null)?0.0:minValue,(maxValue==null)?0.0:maxValue,(minValue==null || maxValue==null)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Breite der Linie
	 * @return	Aktuelle breite der Linie
	 */
	public int getBorderWidth() {
		return borderWidth;
	}

	/**
	 * Stellt die breite der Linie ein
	 * @param borderWidth	Neue Breite f�r die Linie
	 */
	public void setBorderWidth(final int borderWidth) {
		if (borderWidth>=0 && borderWidth<=50) this.borderWidth=borderWidth;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Farbe der Linie
	 * @return	Aktuelle Farbe der Linie
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * Stellt die Farbe der Linie ein
	 * @param color	Farbe der Linie
	 */
	public void setBorderColor(final Color color) {
		if (color!=null) this.borderColor=color;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle F�llfarbe des Kastens
	 * @return	Aktuelle F�llfarbe des Kastens (kann <code>null</code> sein f�r transparent)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Stellt die F�llfarbe des Kastens ein
	 * @param color	F�llfarbe des Kastens (oder <code>null</code> f�r transparent)
	 */
	public void setBackgroundColor(final Color color) {
		this.backgroundColor=color;
		fireChanged();
	}

	/**
	 * Liefert die optionale zweite F�llfarbe des Kastens f�r Farbverl�ufe
	 * @return	Zweite F�llfarbe des Kastens (kann <code>null</code> sein f�r einfarbig bzw. transparent)
	 */
	public Color getGradientFillColor() {
		return gradientColor;
	}

	/**
	 * Stellt die optionale zweite F�llfarbe des Kastens f�r Farbverl�ufe ein
	 * @param color	Zweite F�llfarbe des Kastens (oder <code>null</code> f�r einfarbig bzw. transparent)
	 */
	public void setGradientFillColor(final Color color) {
		gradientColor=color;
		fireChanged();
	}

	/**
	 * Sollen 3D-Effekte f�r die Balken verwendet werden?
	 * @return	3D-Effekte f�r die Balken
	 */
	public boolean isUse3D() {
		return use3D;
	}

	/**
	 * Sollen 3D-Effekte f�r die Balken verwendet werden?
	 * @param use3d	3D-Effekte f�r die Balken
	 */
	public void setUse3D(boolean use3d) {
		this.use3D=use3d;
		fireChanged();
	}

	/**
	 * Sollen Achsenbeschriftungen dargestellt werden?
	 * @return	Achsenbeschriftungen darstellen
	 */
	public AxisDrawer.Mode getAxisLabels() {
		return axisLabels;
	}

	/**
	 * Stellt ein, ob Achsenbeschriftungen darstellen werden sollen.
	 * @param axisLabels	Achsenbeschriftungen darstellen
	 */
	public void setAxisLabels(final AxisDrawer.Mode axisLabels) {
		this.axisLabels=axisLabels;
		yAxisDrawer.setAxisValues((minValue==null)?0.0:minValue,(maxValue==null)?0.0:maxValue,(minValue==null || maxValue==null)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
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
		yAxisDrawer.setAxisValues((minValue==null)?0.0:minValue,(maxValue==null)?0.0:maxValue,(minValue==null || maxValue==null)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
		fireChanged();
	}


	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationBarChart)) return false;

		final ModelElementAnimationBarChart other=(ModelElementAnimationBarChart)element;

		if (expression.size()!=other.expression.size()) return false;
		for (int i=0;i<expression.size();i++) if (!expression.get(i).equalsAnimationExpression(other.expression.get(i))) return false;

		if (expressionColor.size()!=other.expressionColor.size()) return false;
		for (int i=0;i<expressionColor.size();i++) if (!expressionColor.get(i).equals(other.expressionColor.get(i))) return false;

		if (minValue==null) {
			if (other.minValue!=null) return false;
		} else {
			if (other.minValue==null) return false;
			if (minValue.doubleValue()!=other.minValue.doubleValue()) return false;
		}
		if (maxValue==null) {
			if (other.maxValue!=null) return false;
		} else {
			if (other.maxValue==null) return false;
			if (maxValue.doubleValue()!=other.maxValue.doubleValue()) return false;
		}

		if (borderWidth!=other.borderWidth) return false;

		if (!Objects.equals(borderColor,other.borderColor)) return false;
		if (!Objects.equals(backgroundColor,other.backgroundColor)) return false;
		if (!Objects.equals(gradientColor,other.gradientColor)) return false;
		if (use3D!=other.use3D) return false;
		if (axisLabels!=other.axisLabels) return false;
		if (!axisLabelText.equals(other.axisLabelText)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationBarChart) {
			final ModelElementAnimationBarChart source=(ModelElementAnimationBarChart)element;

			expression.clear();
			expression.addAll(source.expression.stream().map(ex->new AnimationExpression(ex)).collect(Collectors.toList()));
			expressionColor.clear();
			expressionColor.addAll(source.expressionColor);

			minValue=source.minValue;
			maxValue=source.maxValue;

			borderWidth=source.borderWidth;
			borderColor=source.borderColor;
			backgroundColor=source.backgroundColor;
			gradientColor=source.gradientColor;
			use3D=source.use3D;
			axisLabels=source.axisLabels;
			axisLabelText=source.axisLabelText;

			yAxisDrawer.setAxisValues((minValue==null)?0.0:minValue,(maxValue==null)?0.0:maxValue,(minValue==null || maxValue==null)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationBarChart clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationBarChart element=new ModelElementAnimationBarChart(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Pr�ft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gem�� dessen der Punkt skaliert wird
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt nahe der Linie befindet
	 */
	protected final boolean isNearLine(final Point p, final Point q, final Point point, final double zoom) {
		final Point v=new Point(q.x-p.x,q.y-p.y); /* Verbindungsvektor P->Q */
		final Point x=new Point((int)FastMath.round(point.x/zoom),(int)FastMath.round(point.y/zoom));

		double alpha=0;
		if (v.y!=0) {
			alpha=((double)v.y)/(v.x*v.x+v.y*v.y)*(x.y-((double)v.x)/v.y*(p.x-x.x)-p.y);
		} else {
			alpha=((double)v.x)/(v.y*v.y+v.x*v.x)*(x.x-((double)v.y)/v.x*(p.y-x.y)-p.x);
		}
		final Point y=new Point((int)FastMath.round(p.x+alpha*v.x),(int)FastMath.round(p.y+alpha*v.y)); /* Lotfu�punkt von X auf P->Q */

		if (Math.abs(x.x-y.x)>MAX_POINT_DELTA || Math.abs(x.y-y.y)>MAX_POINT_DELTA) return false; /* Abstand von Gerade zu gro�? */
		final double len=FastMath.sqrt(v.x*v.x+v.y*v.y);

		if (alpha*len<-MAX_POINT_DELTA || alpha*len>len+MAX_POINT_DELTA) return false; /* Fu�punkt vor Beginn oder nach Ende der Strecke P->Q? */

		return true;
	}

	/**
	 * Pr�ft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		if (super.containsPoint(point,zoom)) return true;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom)) return true;

		return false;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index au�erhalb des g�ltigen Bereichs liegt
	 */
	@Override
	public Point getBorderPointPosition(final int index) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		switch (index) {
		case 0: return new Point(p); /* Kopie, damit diese unabh�ngig von Ver�nderungen des Wertes ist */
		case 1: return new Point(p.x+s.width,p.y);
		case 2: return new Point(p.x+s.width,p.y+s.height);
		case 3: return new Point(p.x,p.y+s.height);
		default: return null;
		}
	}

	/**
	 * Setzt die Position eines Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @param point	Neue Position des Randpunktes
	 */
	@Override
	public void setBorderPointPosition(final int index, final Point point) {
		final Point p1=getPosition(true);
		final Point p2=getLowerRightPosition();

		switch (index) {
		case 0:
			setPosition(point);
			setSize(new Dimension(p2.x-point.x,p2.y-point.y));
			break;
		case 1:
			setPosition(new Point(p1.x,point.y));
			setSize(new Dimension(point.x-p1.x,p2.y-point.y));
			break;
		case 2:
			setSize(new Dimension(point.x-p1.x,point.y-p1.y));
			break;
		case 3:
			setPosition(new Point(point.x,p1.y));
			setSize(new Dimension(p2.x-point.x,point.y-p1.y));
			break;
		}
	}

	/**
	 * Zeichnet eine Markierungs-Box an einer Ecke des Elements
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param point	Position des Punktes
	 * @param zoom	Zoomfaktor
	 */
	protected final void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	/**
	 * Farben f�r die Dummy-Balken (die im Editor, also vor dem Start einer Animation, angezeigt werden)
	 */
	private static final Color[] dummyColor={Color.BLUE,Color.RED,Color.GREEN};

	/**
	 * Werte f�r die Dummy-Balken (die im Editor, also vor dem Start einer Animation, angezeigt werden)
	 */
	private static final double[] dummyValue={0.6,0.4,0.8};

	/**
	 * Cache f�r das Rechteckobjekt das in {@link #drawDiagramBars(Graphics2D, Rectangle, double)}
	 * ben�tigt wird.
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 */
	private Rectangle barDrawRect;

	/**
	 * Cache f�r die x-Koordinaten der Balken in {@link #drawDiagramBars(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 */
	private final int[] xPoints=new int[4];

	/**
	 * Cache f�r die y-Koordinaten der Balken in {@link #drawDiagramBars(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 */
	private final int[] yPoints=new int[4];

	/**
	 * Tats�chlicher Minimalwert
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 * @see #minValue
	 */
	private double drawMin;

	/**
	 * Tats�chlicher Maximalwert
	 * @see #drawDiagramBars(Graphics2D, Rectangle, double)
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 * @see #maxValue
	 */
	private double drawMax;

	/**
	 * Zeichnet die Balken auf die Zeichenfl�che
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param zoom	Zoomfaktor
	 */
	private void drawDiagramBars(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		final double[] recordedValues;
		final List<Color> expressionColor;
		if (this.recordedValues==null) {
			if (this.expressionColor.size()==0) {
				recordedValues=dummyValue;
				expressionColor=Arrays.asList(dummyColor);
			} else {
				expressionColor=this.expressionColor;
				recordedValues=new double[expressionColor.size()];
				final double scale=((maxValue==null)?1:maxValue)-((minValue==null)?0:minValue);
				for (int i=0;i<recordedValues.length;i++) recordedValues[i]=scale*dummyValue[i%dummyValue.length];
			}
		} else {
			recordedValues=this.recordedValues;
			expressionColor=this.expressionColor;
		}

		if (barDrawRect==null) barDrawRect=new Rectangle();

		if (recordedValues.length==0) return;

		drawLock.acquireUninterruptibly();
		try {
			double min;
			if (minValue==null) {
				min=0;
				for (double value: recordedValues) min=FastMath.min(min,value);
			} else {
				min=minValue;
			}
			double max;
			if (maxValue==null) {
				max=FastMath.max(0,min);
				for (double value: recordedValues) max=FastMath.max(max,value);
			} else {
				max=maxValue;
			}

			if (max<min) {final double d=min; min=max; max=d;}
			drawMin=min;
			drawMax=max;
			if (max==min) return;

			final int gap=(int)Math.round(Math.max(1,FastMath.min(use3D?10:5,rectangle.width/(2*recordedValues.length)))*zoom);

			final int w=FastMath.max(1,(rectangle.width-2*10-(recordedValues.length-1)*gap)/recordedValues.length);
			double x=rectangle.x+10;
			final int shadow=w/5;
			final double wDelta=gap+(rectangle.width-2*10-(recordedValues.length-1)*gap)/((double)recordedValues.length);

			if (filler==null || filler.length!=recordedValues.length) {
				filler=new GradientFill[recordedValues.length];
				for (int i=0;i<filler.length;i++) filler[i]=new GradientFill(false);
			}

			final int expressionColorSize=expressionColor.size();
			for (int i=0;i<recordedValues.length;i++) {
				final double value=recordedValues[i];
				final int h=(int)(rectangle.height*(value-min)/(max-min)+0.5);
				if (h>0) {
					final Color c=expressionColor.get(i%expressionColorSize);

					final int startX=(int)(x+0.5);
					final int startY=rectangle.y+rectangle.height-h;

					if (use3D) {
						g.setColor(c);

						/* Rechte Seite des Balkens */
						xPoints[0]=startX+w;
						yPoints[0]=startY+h;
						xPoints[1]=startX+w+shadow;
						yPoints[1]=startY+h-shadow;
						xPoints[2]=startX+w+shadow;
						yPoints[2]=startY-shadow;
						xPoints[3]=startX+w;
						yPoints[3]=startY;
						g.fillPolygon(xPoints,yPoints,4);

						/* Obere Seite des Balkens */
						xPoints[0]=startX;
						yPoints[0]=startY;
						xPoints[1]=startX+w;
						yPoints[1]=startY;
						xPoints[2]=startX+w+shadow;
						yPoints[2]=startY-shadow;
						xPoints[3]=startX+shadow;
						yPoints[3]=startY-shadow;
						g.fillPolygon(xPoints,yPoints,4);
					}

					/* Vorderseite des Balkens */
					barDrawRect.setBounds(startX,startY,w,h);
					filler[i].set(g,barDrawRect,c,true);
					g.fill(barDrawRect);
				}

				/* Abstand zwischen den Balken */
				x+=wDelta;
			}
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Objekt f�r eine Farbverlaufsf�llung
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private GradientFill gradientFill=null;

	/**
	 * Zeichenbereich beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Rectangle drawRectangle;

	/**
	 * x-Position (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Pr�fung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleX;

	/**
	 * y-Position (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Pr�fung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleY;

	/**
	 * Breite (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Pr�fung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleW;

	/**
	 * H�he (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Pr�fung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleH;

	/**
	 * Zoomfaktor beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Pr�fung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private double drawRectangleZoom;

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		/* Zeichenbereich bestimmen */
		if (drawRectangle==null || drawRectangleX!=p.x || drawRectangleY!=p.y || drawRectangleW!=s.width || drawRectangleH!=s.height || drawRectangleZoom!=zoom) {
			drawRectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
			drawRectangleX=p.x;
			drawRectangleY=p.y;
			drawRectangleW=s.width;
			drawRectangleH=s.height;
			drawRectangleZoom=zoom;
		}

		/* Hintergrund f�llen */
		if (backgroundColor!=null) {
			if (gradientColor==null) {
				g2.setColor(backgroundColor);
			} else {
				if (gradientFill==null) gradientFill=new GradientFill(false);
				gradientFill.set(g2,drawRectangle,gradientColor,backgroundColor,true);
			}
			g2.fill(drawRectangle);
		}

		setClip(g2,drawRect,drawRectangle);
		drawDiagramBars(g2,drawRectangle,zoom);
		setClip(g2,drawRect,null);

		boolean drawBorder=false;
		Color lineColor=borderColor;
		if (borderWidth>0) {
			g2.setStroke(new BasicStroke(borderWidth));
			drawBorder=true;
		}

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke(FastMath.max(borderWidth,3)));
			drawBorder=true;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke(FastMath.max(borderWidth,3)));
				drawBorder=true;
			}
		}

		if (drawBorder) {
			g2.setColor(lineColor);
			g2.draw(drawRectangle);
		}

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}

		if ((minValue==null || maxValue==null) && recordedValues!=null) {
			yAxisDrawer.setAxisValues(drawMin,drawMax,axisLabels,axisLabelText);
		}
		yAxisDrawer.drawY(g2,zoom,drawRectangle);

		g2.setStroke(saveStroke);
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationBarChart.Name");
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationBarChartDialog(owner,ModelElementAnimationBarChart.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationBarChartDialog(owner,ModelElementAnimationBarChart.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationBarChart.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		if (backgroundColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(backgroundColor));
		}

		if (gradientColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.Rectangle.XML.GradientColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(gradientColor));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.Use3D"));
		node.appendChild(sub);
		sub.setTextContent(use3D?"1":"0");

		if (minValue!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.MinValue"));
			node.appendChild(sub);
			sub.setTextContent(NumberTools.formatSystemNumber(minValue));
		}

		if (maxValue!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.MaxValue"));
			node.appendChild(sub);
			sub.setTextContent(NumberTools.formatSystemNumber(maxValue));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.Labels"));
		node.appendChild(sub);
		sub.setTextContent(""+axisLabels.nr);
		if (!axisLabelText.isBlank()) sub.setAttribute(Language.trPrimary("Surface.AnimationBarChart.XML.LabelText"),axisLabelText);

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.Set"));
			node.appendChild(sub);
			expression.get(i).storeToXML(sub);
			sub.setAttribute(Language.trPrimary("Surface.AnimationBarChart.XML.Set.BarColor"),EditModel.saveColor(expressionColor.get(i)));
		}
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.AnimationBarChart.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.LineColor",name) && !content.isBlank()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.BackgroundColor",name) && !content.isBlank()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.GradientColor",name) && !content.isBlank()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			gradientColor=color;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.Use3D",name)) {
			use3D=(!content.isBlank() && !content.equals("0"));
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.Set",name) && !content.isBlank()) {
			final String colorString=Language.trAllAttribute("Surface.AnimationBarChart.XML.Set.BarColor",node);
			final Color color;
			if (colorString.isBlank()) {
				color=Color.BLACK;
			} else {
				color=EditModel.loadColor(colorString);
				if (color==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBarChart.XML.Set.BarColor"),name,node.getParentNode().getNodeName());
			}
			expressionColor.add(color);

			final AnimationExpression ex=new AnimationExpression();
			ex.loadFromXML(node);
			expression.add(ex);

			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.MinValue",name)) {
			Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			minValue=D;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.MaxValue",name)) {
			Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			maxValue=D;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.Labels",name)) {
			axisLabels=AxisDrawer.Mode.fromNr(content);
			axisLabelText=Language.trAllAttribute("Surface.AnimationBarChart.XML.LabelText",node);
			return null;
		}

		return null;
	}

	/**
	 * In {@link #updateSimulationData(SimulationData, boolean)} berechnete Ausgabedaten
	 * {@link #updateSimulationData(SimulationData, boolean)}
	 */
	private double[][] dataSets;

	/**
	 * F�r die Ausgabe aktiver Eintrag in {@link #dataSets}
	 * @see #dataSets
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private int drawActiveDataSet;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (isPreview) return false;

		if (dataSets==null) {
			dataSets=new double[2][];
			dataSets[0]=new double[expression.size()];
			dataSets[1]=new double[expression.size()];
			drawActiveDataSet=0;
		}

		boolean needUpdate=false;
		final int nextDataSet=(drawActiveDataSet+1)%2;
		for (int i=0;i<dataSets[0].length;i++) {
			final double value=expression.get(i).getAnimationValue(this,simData);
			dataSets[nextDataSet][i]=value;
			if (!needUpdate) needUpdate=(dataSets[drawActiveDataSet][i]!=value);
		}
		if (!needUpdate && recordedValues!=null) return false; /* Per "recordedValues!=null" wird erzwungen, dass ganz am Anfang wenigstens einmal Daten an die Zeichenroutine �bergeben werden. */

		drawLock.acquireUninterruptibly();
		try {
			drawActiveDataSet=nextDataSet;
			recordedValues=dataSets[drawActiveDataSet];
		} finally {
			drawLock.release();
		}
		return true;
	}

	/**
	 * System zur Darstellung der y-Achsenbeschriftung
	 */
	private final AxisDrawer yAxisDrawer=new AxisDrawer(true);

	@Override
	public void initAnimation(final SimulationData simData) {
		recordedValues=null;
		dataSets=null;

		for (int i=0;i<expression.size();i++) {
			expression.get(i).initAnimation(this,simData);
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationBarChart";
	}

	/**
	 * Liefert die Javascript-Daten f�r die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten f�r die Station
	 */
	private String getHTMLAnimationBarChart(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationBarChart(rect,barRects,barColors1,barColors2,borderColor,borderWidth,fillColor) {\n");

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

		sb.append("  for (var i=0;i<Math.min(Math.min(barRects.length,barColors1.length),barColors2.length);i++) {\n");
		sb.append("    var gradient=context.createLinearGradient(Math.round(barRects[i].x+barRects[i].w/2),barRects[i].y+barRects[i].h,Math.round(barRects[i].x+barRects[i].w/2),barRects[i].y);\n");
		sb.append("    gradient.addColorStop(0,barColors1[i]);\n");
		sb.append("    gradient.addColorStop(1,barColors2[i]);\n");
		sb.append("    context.fillStyle=gradient;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(barRects[i].x,barRects[i].y);\n");
		sb.append("    context.lineTo(barRects[i].x+barRects[i].w,barRects[i].y);\n");
		sb.append("    context.lineTo(barRects[i].x+barRects[i].w,barRects[i].y+barRects[i].h);\n");
		sb.append("    context.lineTo(barRects[i].x,barRects[i].y+barRects[i].h);\n");
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

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationBarChart",builder->getHTMLAnimationBarChart(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		final StringBuilder barRects=new StringBuilder();
		final int w=(d.width-2*10-(dummyValue.length-1)*5)/dummyValue.length;
		int x=p.x+10;
		for (double value: dummyValue) {
			if (barRects.length()>0) barRects.append(",");
			final int h=(int)FastMath.round(d.height*value);
			final Rectangle r=new Rectangle(x,p.y+d.height-h,w,h);
			barRects.append("{x: "+r.x+", y: "+r.y+", w: "+r.width+", h: "+r.height+"}");
			x+=w+5;
		}

		final StringBuilder barColors1=new StringBuilder();
		final StringBuilder barColors2=new StringBuilder();
		final BrighterColor brighter=new BrighterColor();
		for (Color c: dummyColor) {
			if (barColors1.length()>0) barColors1.append(",");
			barColors1.append("\""+HTMLOutputBuilder.colorToHTML(brighter.get(c))+"\"");
			if (barColors2.length()>0) barColors2.append(",");
			barColors2.append("\""+HTMLOutputBuilder.colorToHTML(c)+"\"");
		}

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationBarChart("+rect+",["+barRects.toString()+"],["+barColors1.toString()+"],["+barColors2.toString()+"],"+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationBarChart("+rect+",["+barRects.toString()+"],["+barColors1.toString()+"],["+barColors2.toString()+"],"+border+","+borderWidth+","+fill+");\n");
		}
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}

	@Override
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		return simData!=null;
	}

	/**
	 * Liefert die Daten in Tabellenform f�r die Ausgabe einer Datentabelle w�hrend der Animation
	 * @param simData	Simulationsdatenobjekt
	 * @return	Tabelle mit den aktuellen Ausgabedaten
	 */
	private Table getAnimationRunTimeTableData(final SimulationData simData) {
		final Table table=new Table();

		table.addLine(new String[] {Language.tr("ModelDescription.Expression"),Language.tr("Statistic.Viewer.Chart.Value")});

		drawLock.acquireUninterruptibly();
		try {
			for (int i=0;i<recordedValues.length;i++) {
				final List<String> line=new ArrayList<>(2);
				final String info;
				switch (expression.get(i).getMode()) {
				case Expression: info=expression.get(i).getExpression(); break;
				case Java: info=Language.tr("ModelDescription.Expression.Java"); break;
				case Javascript: info=Language.tr("ModelDescription.Expression.Javascript"); break;
				default: info=expression.get(i).getExpression(); break;
				}
				line.add(info);
				line.add(NumberTools.formatNumber(recordedValues[i],3));
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

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<expression.size();i++) {
			final AnimationExpression ex=expression.get(i);
			if (ex.getMode()==AnimationExpression.ExpressionMode.Expression) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.Expression"),ex.getExpression(),newExpression->ex.setExpression(newExpression));
			}
		}
		if (minValue!=null) searcher.testDouble(this,Language.tr("Editor.DialogBase.Search.MinValue"),minValue,newMinValue->{minValue=newMinValue;});
		if (maxValue!=null) searcher.testDouble(this,Language.tr("Editor.DialogBase.Search.MaxValue"),maxValue,newMaxValue->{maxValue=newMaxValue;});
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.BorderWidth"),borderWidth,newBorderWidth->{if (newBorderWidth>=0) borderWidth=newBorderWidth;});
	}
}
