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

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt einen sich während der Animation aktualisierenden
 * aus mehreren Teilen zusammengesetzten Balken an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationBarStack extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Richtung in die der Balken aufgefüllt werden soll
	 * @author Alexander Herzog
	 * @see ModelElementAnimationBarStack#getDirection()
	 * @see ModelElementAnimationBarStack#setDirection(FillDirection)
	 */
	public enum FillDirection {
		/** Balken baut sich von unten nach oben auf */
		DIRECTION_UP,

		/** Balken baut sich von links nach rechts auf */
		DIRECTION_RIGHT,

		/** Balken baut sich von oben nach unten auf */
		DIRECTION_DOWN,

		/** Balken baut sich von rechts nach links auf */
		DIRECTION_LEFT
	}

	/**
	 * Richtung, in die sich der Balken aufbaut
	 * @see FillDirection
	 * @see #getDirection()
	 * @see #setDirection(FillDirection)
	 */
	private FillDirection direction=FillDirection.DIRECTION_UP;

	/**
	 * Rechenausdrücke
	 * @see #getExpressions()
	 */
	private final List<AnimationExpression> expressions=new ArrayList<>();

	/**
	 * Sichert ab, dass Simulations- und Zeichen-Thread
	 * nicht gleichzeitig auf {@link #simValues}
	 * zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * In {@link #updateSimulationData(SimulationData, boolean)}
	 * aufgezeichnete und in {@link #fillBox(Graphics2D, Rectangle)}
	 * anzuzeigende Animationsdaten.
	 * @see #updateSimulationData(SimulationData, boolean)
	 * @see #fillBox(Graphics2D, Rectangle)
	 */
	private double[] simValues=null;

	/**
	 * Maximalwert (&le;0 für automatisch)
	 * @see #getMaxValue()
	 * @see #setMaxValue(double)
	 */
	private double maxValue=0.0;

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
	 * Füllfarbe des Kastens
	 * @see #getBackgroundColor()
	 * @see #setBackgroundColor(Color)
	 */
	private Color backgroundColor=new Color(240,240,240);

	/**
	 * Optionale zweite Füllfarbe des Kastens für Farbverläufe
	 * @see #getGradientFillColor()
	 * @see #setGradientFillColor(Color)
	 */
	protected Color gradientColor=null;

	/**
	 * Farbe der Balken
	 * @see #getBarColors()
	 */
	private final List<Color> barColors=new ArrayList<>();

	/**
	 * Sollen Beschriftungen an der y-Achse angezeigt werden?
	 */
	private AxisDrawer.Mode axisLabels=AxisDrawer.Mode.OFF;

	/**
	 * Beschriftungstext an der y-Achse
	 */
	private String axisLabelText="";

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationBarStack</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationBarStack(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50),Shapes.ShapeType.SHAPE_NONE);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK.getIcon();
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
		return Language.tr("Surface.AnimationBarStack.Tooltip");
	}

	/**
	 * Liefert die Richtung, in die sich der Balken aufbaut
	 * @return	Richtung des Balken
	 * @see FillDirection
	 */
	public FillDirection getDirection() {
		return direction;
	}

	/**
	 * Stellt die Richtung ein, in die sich der Balken aufbaut
	 * @param direction	Neue Richtung des Balken
	 * @see FillDirection
	 */
	public void setDirection(final FillDirection direction) {
		this.direction=direction;
		fireChanged();
	}

	/**
	 * Liefert im die aktuellen Ausdrücke.
	 * @return	Aktuelle Ausdrücke
	 */
	public List<AnimationExpression> getExpressions() {
		return expressions;
	}

	/**
	 * Liefert den Maximalwert für die Balkendarstellung
	 * @return	Maximalwert (&le;0 für automatisch)
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Stellt den Maximalwert für die Balkendarstellung ein
	 * @param maxValue	Neuer Maximalwert (&le;0 für automatisch)
	 */
	public void setMaxValue(final double maxValue) {
		this.maxValue=maxValue;
		yAxisDrawer.setAxisValues(0,maxValue,(maxValue<=0)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
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
	 * @param borderWidth	Neue Breite für die Linie
	 */
	public void setBorderWidth(final int borderWidth) {
		if (borderWidth>=0 && borderWidth<=50) this.borderWidth=borderWidth;
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
	 * Liefert die aktuelle Füllfarbe des Kastens
	 * @return	Aktuelle Füllfarbe des Kastens (kann <code>null</code> sein für transparent)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Stellt die Füllfarbe des Kastens ein
	 * @param color	Füllfarbe des Kastens (oder <code>null</code> für transparent)
	 */
	public void setBackgroundColor(final Color color) {
		this.backgroundColor=color;
		fireChanged();
	}

	/**
	 * Liefert die optionale zweite Füllfarbe des Kastens für Farbverläufe
	 * @return	Zweite Füllfarbe des Kastens (kann <code>null</code> sein für einfarbig bzw. transparent)
	 */
	public Color getGradientFillColor() {
		return gradientColor;
	}

	/**
	 * Stellt die optionale zweite Füllfarbe des Kastens für Farbverläufe ein
	 * @param color	Zweite Füllfarbe des Kastens (oder <code>null</code> für einfarbig bzw. transparent)
	 */
	public void setGradientFillColor(final Color color) {
		gradientColor=color;
		fireChanged();
	}

	/**
	 * Liefert die aktuellen Farbe der Balken
	 * @return	Aktuelle Farben der Balken
	 */
	public List<Color> getBarColors() {
		return barColors;
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
		yAxisDrawer.setAxisValues(0,maxValue,(maxValue<=0)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
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
		yAxisDrawer.setAxisValues(0,maxValue,(maxValue<=0)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
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
		if (!(element instanceof ModelElementAnimationBarStack)) return false;
		final ModelElementAnimationBarStack other=(ModelElementAnimationBarStack)element;

		if (direction!=other.direction) return false;
		if (expressions.size()!=other.expressions.size()) return false;
		for (int i=0;i<expressions.size();i++) {
			final AnimationExpression ex1=expressions.get(i);
			final AnimationExpression ex2=other.expressions.get(i);
			if (ex1==null || ex2==null || !ex1.equalsAnimationExpression(ex2)) return false;
		}
		if (maxValue>0) {
			if (maxValue!=other.maxValue) return false;
		} else {
			if (other.maxValue>0) return false;
		}

		if (borderWidth!=other.borderWidth) return false;
		if (!Objects.equals(borderColor,other.borderColor)) return false;
		if (!Objects.equals(backgroundColor,other.backgroundColor)) return false;
		if (!Objects.equals(gradientColor,other.gradientColor)) return false;

		if (barColors.size()!=other.barColors.size()) return false;
		for (int i=0;i<barColors.size();i++) {
			final Color c1=barColors.get(i);
			final Color c2=((ModelElementAnimationBarStack)element).barColors.get(i);
			if (c1==null || c2==null || !c1.equals(c2)) return false;
		}

		if (axisLabels!=other.axisLabels) return false;
		if (!axisLabelText.equals(other.axisLabelText)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationBarStack) {
			final ModelElementAnimationBarStack source=(ModelElementAnimationBarStack)element;

			direction=source.direction;
			expressions.clear();
			expressions.addAll(source.expressions.stream().map(expression->new  AnimationExpression(expression)).collect(Collectors.toList()));
			maxValue=source.maxValue;

			borderWidth=source.borderWidth;
			borderColor=source.borderColor;
			backgroundColor=source.backgroundColor;
			gradientColor=source.gradientColor;
			barColors.clear();
			barColors.addAll(source.barColors);
			axisLabels=source.axisLabels;
			axisLabelText=source.axisLabelText;

			yAxisDrawer.setAxisValues(0,maxValue,(maxValue<=0)?AxisDrawer.Mode.OFF:axisLabels,axisLabelText);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationBarStack clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationBarStack element=new ModelElementAnimationBarStack(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Prüft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem geprüft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gemäß dessen der Punkt skaliert wird
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt nahe der Linie befindet
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
		final Point y=new Point((int)FastMath.round(p.x+alpha*v.x),(int)FastMath.round(p.y+alpha*v.y)); /* Lotfußpunkt von X auf P->Q */

		if (Math.abs(x.x-y.x)>MAX_POINT_DELTA || Math.abs(x.y-y.y)>MAX_POINT_DELTA) return false; /* Abstand von Gerade zu groß? */
		final double len=FastMath.sqrt(v.x*v.x+v.y*v.y);

		if (alpha*len<-MAX_POINT_DELTA || alpha*len>len+MAX_POINT_DELTA) return false; /* Fußpunkt vor Beginn oder nach Ende der Strecke P->Q? */

		return true;
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
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
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index außerhalb des gültigen Bereichs liegt
	 */
	@Override
	public Point getBorderPointPosition(final int index) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		switch (index) {
		case 0: return new Point(p); /* Kopie, damit diese unabhängig von Veränderungen des Wertes ist */
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
	private final void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	/**
	 * Füllt ein Rechteck gemäß dem in {@link #simValues} angegebenen Füllstand.
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Teilweise zu füllendes Rechteck
	 * @return	Tatsächlicher Maximalwert (entweder {@link #maxValue} oder dynamisch berechnet)
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 * @see #simValues
	 */
	private double fillBox(final Graphics2D g, final Rectangle rectangle) {
		double[] d=null;
		double maxVal=4;
		drawLock.acquireUninterruptibly();
		try {
			if (simValues==null || simValues.length==0) {
				d=new double[]{1,1,1};
				maxVal=4;
			} else {
				d=Arrays.copyOf(simValues,simValues.length);
				maxVal=maxValue;
			}
		} finally {
			drawLock.release();
		}

		int maxPixel=1;
		switch (direction) {
		case DIRECTION_UP:
			maxPixel=rectangle.height;
			break;
		case DIRECTION_RIGHT:
			maxPixel=rectangle.width;
			break;
		case DIRECTION_DOWN:
			maxPixel=rectangle.height;
			break;
		case DIRECTION_LEFT:
			maxPixel=rectangle.width;
			break;
		}

		if (maxVal<=0) {
			maxVal=0;
			if (d!=null) for (int i=0;i<d.length;i++) maxVal+=d[i];
		}

		double sum=0;
		if (d!=null) for (int i=0;i<d.length;i++) {
			double value=d[i];

			int delta1=(int)FastMath.round(maxPixel*sum/maxVal);
			int delta2=(int)FastMath.round(maxPixel*value/maxVal);
			delta1=FastMath.max(0,delta1);
			delta1=FastMath.min(maxPixel,delta1);
			delta2=FastMath.max(0,delta2);
			delta2=FastMath.min(maxPixel-delta1,delta2);

			if (barColors==null || barColors.size()<=i || simValues==null) {
				switch (i%4) {
				case 0: g.setColor(Color.RED); break;
				case 1: g.setColor(Color.GREEN); break;
				case 2: g.setColor(Color.BLUE); break;
				case 3: g.setColor(Color.ORANGE); break;
				}
			} else {
				g.setColor(barColors.get(i)); /* keine Farbverläufe hier */
			}

			switch (direction) {
			case DIRECTION_UP:
				g.fillRect(rectangle.x,rectangle.y+rectangle.height-delta1-delta2,rectangle.width,delta2);
				break;
			case DIRECTION_RIGHT:
				g.fillRect(rectangle.x+delta1,rectangle.y,delta2,rectangle.height);
				break;
			case DIRECTION_DOWN:
				g.fillRect(rectangle.x,rectangle.y+delta1,rectangle.width,delta2);
				break;
			case DIRECTION_LEFT:
				g.fillRect(rectangle.x+rectangle.width-delta1-delta2,rectangle.y,delta2,rectangle.height);
				break;
			}

			sum+=value;
		}

		return maxVal;
	}

	/**
	 * Objekt für eine Farbverlaufsfüllung
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
	 * (Zur Prüfung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleX;

	/**
	 * y-Position (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Prüfung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleY;

	/**
	 * Breite (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Prüfung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleW;

	/**
	 * Höhe (in Modellkoordinaten) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Prüfung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int drawRectangleH;

	/**
	 * Zoomfaktor beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}<br>
	 * (Zur Prüfung, ob {@link #drawRectangle} wiederverwendet werden kann.)
	 * @see #drawRectangle
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private double drawRectangleZoom;

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

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

		/* Zeichenbereich bestimmen */
		if (drawRectangle==null || drawRectangleX!=p.x || drawRectangleY!=p.y || drawRectangleW!=s.width || drawRectangleH!=s.height || drawRectangleZoom!=zoom) {
			drawRectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
			drawRectangleX=p.x;
			drawRectangleY=p.y;
			drawRectangleW=s.width;
			drawRectangleH=s.height;
			drawRectangleZoom=zoom;
		}

		/* Hintergrund füllen */
		if (backgroundColor!=null) {
			if (gradientColor==null) {
				g2.setColor(backgroundColor);
			} else {
				if (gradientFill==null) gradientFill=new GradientFill(false);
				gradientFill.set(g2,drawRectangle,gradientColor,backgroundColor,true);
			}
			g2.fill(drawRectangle);
		}

		final double maxVal=fillBox(g2,drawRectangle);

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

		setClip(graphics,drawRect,null);

		if (maxValue<=0) {
			yAxisDrawer.setAxisValues(0,maxVal,axisLabels,axisLabelText);
		}
		switch (direction) {
		case DIRECTION_DOWN:
			yAxisDrawer.drawYInvers(g2,zoom,drawRectangle);
			break;
		case DIRECTION_LEFT:
			yAxisDrawer.drawXInvers(g2,zoom,drawRectangle);
			break;
		case DIRECTION_RIGHT:
			yAxisDrawer.drawX(g2,zoom,drawRectangle);
			break;
		case DIRECTION_UP:
			yAxisDrawer.drawY(g2,zoom,drawRectangle);
			break;
		}

		g2.setStroke(saveStroke);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationBarStack.Name");
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
			new ModelElementAnimationBarStackDialog(owner,ModelElementAnimationBarStack.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationBarStackDialog(owner,ModelElementAnimationBarStack.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationBarStack.XML.Root");
	}

	/**
	 * Übersetzt die Füllrichtung in einen xml-Ausdruck.
	 * @param direction	Füllrichtung
	 * @return	Füllrichtung in einer xml-speicherbaren Form
	 * @see #addPropertiesDataToXML(Document, Element)
	 */
	private String getDirectionString(final FillDirection direction) {
		switch (direction) {
		case DIRECTION_DOWN: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Down");
		case DIRECTION_LEFT: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Left");
		case DIRECTION_RIGHT: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Right");
		case DIRECTION_UP: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Up");
		default: return "";
		}
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

		for (AnimationExpression expression: expressions) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.DataExpression"));
			node.appendChild(sub);
			expression.storeToXML(sub);
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.DataArea"));
		node.appendChild(sub);
		if (maxValue>0) {
			sub.setAttribute(Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Max"),NumberTools.formatSystemNumber(maxValue));
		}
		sub.setAttribute(Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction"),getDirectionString(direction));

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		if (backgroundColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(backgroundColor));
		}

		if (gradientColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.GradientColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(gradientColor));
		}

		for (Color barColor: barColors) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.BarColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(barColor));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.Labels"));
		node.appendChild(sub);
		sub.setTextContent(""+axisLabels.nr);
		if (!axisLabelText.isBlank()) sub.setAttribute(Language.trPrimary("Surface.AnimationBarStack.XML.LabelText"),axisLabelText);
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

		if (Language.trAll("Surface.AnimationBarStack.XML.DataExpression",name)) {
			final AnimationExpression expression=new AnimationExpression();
			expression.loadFromXML(node);
			expressions.add(expression);
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.DataArea",name)) {
			final String maxAttr=Language.trAllAttribute("Surface.AnimationBarStack.XML.DataArea.Max",node).trim();
			if (!maxAttr.isEmpty()) {
				final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(maxAttr));
				if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Max"),name,node.getParentNode().getNodeName());
				maxValue=D;
			}

			final String s=Language.trAllAttribute("Surface.AnimationBarStack.XML.DataArea.Direction",node);
			if (!s.isEmpty()) {
				boolean ok=false;
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Up",s)) {direction=FillDirection.DIRECTION_UP; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Right",s)) {direction=FillDirection.DIRECTION_RIGHT; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Down",s)) {direction=FillDirection.DIRECTION_DOWN; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Left",s)) {direction=FillDirection.DIRECTION_LEFT; ok=true;}
				if (!ok) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction"),name,node.getParentNode().getNodeName());
			}

			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.LineColor",name) && !content.isBlank()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.BackgroundColor",name) && !content.isBlank()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.GradientColor",name) && !content.isBlank()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			gradientColor=color;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.BarColor",name) && !content.isBlank()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			barColors.add(color);
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.Labels",name)) {
			axisLabels=AxisDrawer.Mode.fromNr(content);
			axisLabelText=Language.trAllAttribute("Surface.AnimationBarStack.XML.LabelText",node);
			return null;
		}

		return null;
	}

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (isPreview) return false;
		drawLock.acquireUninterruptibly();
		try {
			for (int i=0;i<expressions.size();i++) {
				simValues[i]=expressions.get(i).getAnimationValue(this,simData);
			}
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
		simValues=new double[expressions.size()];
		for (int i=0;i<expressions.size();i++) {
			expressions.get(i).initAnimation(this,simData);
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationBarStack";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationBarStack(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationBarStack(rect,barRects,barColors,borderColor,borderWidth,fillColor) {\n");

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

		sb.append("  for (var i=0;i<Math.min(barRects.length,barColors.length);i++) {\n");
		sb.append("    context.fillStyle=barColors[i];\n");
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
		outputBuilder.addJSUserFunction("drawAnimationBarStack",builder->getHTMLAnimationBarStack(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		final StringBuilder barRects=new StringBuilder();
		int z;
		switch (direction) {
		case DIRECTION_DOWN:
			z=d.height/4;
			barRects.append("{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+2*z)+", w: "+d.width+", h: "+z+"}");
			break;
		case DIRECTION_LEFT:
			z=d.width/4;
			barRects.append("{x: "+p.x+", y: "+p.y+", w: "+z+", h: "+d.height+"},");
			barRects.append("{x: "+(p.x+z)+", y: "+p.y+", w: "+z+", h: "+d.height+"},");
			barRects.append("{x: "+(p.x+2*z)+", y: "+p.y+", w: "+z+", h: "+d.height+"}");
			break;
		case DIRECTION_RIGHT:
			z=d.width/4;
			barRects.append("{x: "+p.x+", y: "+(p.y+d.width-z)+", w: "+z+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.width-2*z)+", w: "+z+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.width-3*z)+", w: "+z+", h: "+z+"}");
			break;
		case DIRECTION_UP:
			z=d.height/4;
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-2*z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-3*z)+", w: "+d.width+", h: "+z+"}");
			break;
		default:
			z=d.height/4;
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-2*z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-3*z)+", w: "+d.width+", h: "+z+"}");
			break;
		}

		final StringBuilder barColors=new StringBuilder();
		barColors.append("\""+HTMLOutputBuilder.colorToHTML(Color.RED)+"\",");
		barColors.append("\""+HTMLOutputBuilder.colorToHTML(Color.GREEN)+"\",");
		barColors.append("\""+HTMLOutputBuilder.colorToHTML(Color.BLUE)+"\"");

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationBarStack("+rect+",["+barRects.toString()+"],["+barColors.toString()+"],"+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationBarStack("+rect+",["+barRects.toString()+"],["+barColors.toString()+"],"+border+","+borderWidth+","+fill+");\n");
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
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<expressions.size();i++) {
			final AnimationExpression expression=expressions.get(i);
			if (expression.getMode()==AnimationExpression.ExpressionMode.Expression) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.Expression"),expression.getExpression(),newExpression->expression.setExpression(newExpression));
			}
		}
		searcher.testDouble(this,Language.tr("Editor.DialogBase.Search.MaxValue"),maxValue,newMaxValue->{maxValue=newMaxValue;});
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.BorderWidth"),borderWidth,newBorderWidth->{if (newBorderWidth>=0) borderWidth=newBorderWidth;});
	}
}