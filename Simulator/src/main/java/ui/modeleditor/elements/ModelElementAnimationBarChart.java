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
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
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
 * Zeigt ein sich während der Animation aktualisierendes Balkendiagramm an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationBarChart extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
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
	 * Aufgezeichnete Werte die in {@link #drawDiagramBars(Graphics2D, Rectangle)}
	 * gezeichnet werden sollen
	 * @see #drawDiagramBars(Graphics2D, Rectangle)
	 */
	private double[] recordedValues;

	/**
	 * Liste der Rechenausdrücke für die Diagramm-Einträge
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<String> expression=new ArrayList<>();

	/**
	 * Liste der Farben für die Diagramm-Einträge
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
	 * Füllfarbe des Kastens
	 * @see #getBackgroundColor()
	 * @see #setBackgroundColor(Color)
	 */
	private Color backgroundColor=null;

	/**
	 * 3D-Effekte für die Balken
	 * @see #isUse3D()
	 * @see #setUse3D(boolean)
	 */
	private boolean use3D=true;

	/**
	 * Cache der Bereichsfüllung-Zeichners
	 * für {@link #drawDiagramBars(Graphics2D, Rectangle)}
	 * @see #drawDiagramBars(Graphics2D, Rectangle)
	 */
	private GradientFill[] filler;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationBarChart</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationBarChart(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(75,75),Shapes.ShapeType.SHAPE_NONE);
		filler=null;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationBarChart.Tooltip");
	}

	/**
	 * Liefert eine Liste der vorhandenen Diagramm-Einträge.<br>
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (String), Balkenfarbe (Color).
	 * @return	Liste der Diagramm-Einträge
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
	 * Ersetzt die bisherigen Diagramm-Einträge durch eine neue Liste.<br>
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (String), Balkenfarbe (Color).
	 * @param data	Liste der neuen Diagramm-Einträge
	 */
	public void setExpressionData(final List<Object[]> data) {
		expression.clear();
		expressionColor.clear();

		for (Object[] row: data) if (row.length==2) {
			if (!(row[0] instanceof String)) continue;
			if (!(row[1] instanceof Color)) continue;
			expression.add((String)row[0]);
			expressionColor.add((Color)row[1]);
		}
	}

	/**
	 * Liefert den Minimalwert für die Diagrammbalken.
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
	}


	/**
	 * Liefert den Maximalwert für die Diagrammbalken.
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
	 * Sollen 3D-Effekte für die Balken verwendet werden?
	 * @return	3D-Effekte für die Balken
	 */
	public boolean isUse3D() {
		return use3D;
	}

	/**
	 * Sollen 3D-Effekte für die Balken verwendet werden?
	 * @param use3d	3D-Effekte für die Balken
	 */
	public void setUse3D(boolean use3d) {
		this.use3D=use3d;
		fireChanged();
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationBarChart)) return false;

		final ModelElementAnimationBarChart other=(ModelElementAnimationBarChart)element;

		if (expression.size()!=other.expression.size()) return false;
		for (int i=0;i<expression.size();i++) if (!expression.get(i).equals(other.expression.get(i))) return false;

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
		if (!other.borderColor.equals(borderColor)) return false;
		if (!(other.backgroundColor==null && backgroundColor==null)) {
			if (other.backgroundColor==null || backgroundColor==null) return false;
			if (!other.backgroundColor.equals(backgroundColor)) return false;
		}
		if (use3D!=other.use3D) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationBarChart) {
			final ModelElementAnimationBarChart source=(ModelElementAnimationBarChart)element;

			expression.addAll(source.expression);
			expressionColor.addAll(source.expressionColor);

			minValue=source.minValue;
			maxValue=source.maxValue;

			borderWidth=source.borderWidth;
			borderColor=source.borderColor;
			backgroundColor=source.backgroundColor;
			use3D=source.use3D;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationBarChart clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationBarChart element=new ModelElementAnimationBarChart(model,surface);
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
	protected final void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	/**
	 * Farben für die Dummy-Balken (die im Editor, also vor dem Start einer Animation, angezeigt werden)
	 * @see #drawDummyDiagramBars(Graphics2D, Rectangle)
	 */
	private static final Color[] dummyColor={Color.BLUE,Color.RED,Color.GREEN};

	/**
	 * Werte für die Dummy-Balken (die im Editor, also vor dem Start einer Animation, angezeigt werden)
	 * @see #drawDummyDiagramBars(Graphics2D, Rectangle)
	 */
	private static final double[] dummyValue={0.6,0.4,0.8};

	/**
	 * Zeichnet Dummy-Balken während der Editor aktiv ist (und noch keine Animationsdaten vorliegen)
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 */
	private void drawDummyDiagramBars(final Graphics2D g, final Rectangle rectangle) {
		final int w=(rectangle.width-2*10-(dummyValue.length-1)*5)/dummyValue.length;
		int x=rectangle.x+10;

		if (filler==null || filler.length!=dummyValue.length) {
			filler=new GradientFill[dummyValue.length];
			for (int i=0;i<filler.length;i++) filler[i]=new GradientFill();
		}

		for (int i=0;i<dummyValue.length;i++) {
			final int h=(int)FastMath.round(rectangle.height*dummyValue[i]);
			final Rectangle r=new Rectangle(x,rectangle.y+rectangle.height-h,w,h);
			filler[i].set(g,r,dummyColor[i%dummyColor.length],true);
			g.fill(r);
			x+=w+5;
		}
	}

	/**
	 * Cache für das Recheckobjekt das in {@link #drawDiagramBars(Graphics2D, Rectangle)}
	 * benötigt wird.
	 * @see #drawDiagramBars(Graphics2D, Rectangle)
	 */
	private Rectangle barDrawRect;

	/**
	 * Cache für die x-Koordinaten der Balken in {@link #drawDiagramBars(Graphics2D, Rectangle)}
	 * @see #drawDiagramBars(Graphics2D, Rectangle)
	 */
	private int[] xPoints=new int[4];

	/**
	 * Cache für die y-Koordinaten der Balken in {@link #drawDiagramBars(Graphics2D, Rectangle)}
	 * @see #drawDiagramBars(Graphics2D, Rectangle)
	 */
	private int[] yPoints=new int[4];

	/**
	 * Zeichnet die Balken auf die Zeichenfläche
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 */
	private void drawDiagramBars(final Graphics2D g, final Rectangle rectangle) {
		if (recordedValues==null) {
			drawDummyDiagramBars(g,rectangle);
			return;
		}

		if (barDrawRect==null) barDrawRect=new Rectangle();

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
			if (max==min) return;

			final int gap=use3D?10:5;

			final int w=(rectangle.width-2*10-(recordedValues.length-1)*gap)/recordedValues.length;
			int x=rectangle.x+10;
			final int shadow=w/5;

			if (filler==null || filler.length!=recordedValues.length) {
				filler=new GradientFill[recordedValues.length];
				for (int i=0;i<filler.length;i++) filler[i]=new GradientFill();
			}

			for (int i=0;i<recordedValues.length;i++) {
				final double value=recordedValues[i];
				final int h=(int)FastMath.round(rectangle.height*(value-min)/(max-min));
				final Color c=expressionColor.get(i%expressionColor.size());

				final int startX=x;
				final int startY=rectangle.y+rectangle.height-h;

				if (use3D) {
					/* Rechte Seite des Balkens */
					xPoints[0]=startX+w;
					yPoints[0]=startY+h;
					xPoints[1]=startX+w+shadow;
					yPoints[1]=startY+h-shadow;
					xPoints[2]=startX+w+shadow;
					yPoints[2]=startY-shadow;
					xPoints[3]=startX+w;
					yPoints[3]=startY;
					g.setColor(c);
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
					g.setColor(c);
					g.fillPolygon(xPoints,yPoints,4);
				}

				/* Vorderseite des Balkens */
				barDrawRect.setBounds(startX,startY,w,h);
				filler[i].set(g,barDrawRect,c,true);
				g.fill(barDrawRect);

				/* Abstand zwischen den Balken */
				x+=w+gap;
			}
		} finally {
			drawLock.release();
		}
	}

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

		Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
		if (backgroundColor!=null) {
			g2.setColor(backgroundColor);
			g2.fill(rectangle);
		}

		setClip(g2,drawRect,rectangle);
		drawDiagramBars(g2,rectangle);
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
			g2.draw(rectangle);
		}

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}

		g2.setStroke(saveStroke);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationBarChart.Name");
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
			new ModelElementAnimationBarChartDialog(owner,ModelElementAnimationBarChart.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationBarChart.XML.Root");
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

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarChart.XML.Set"));
			node.appendChild(sub);
			sub.setTextContent(expression.get(i));
			sub.setAttribute(Language.trPrimary("Surface.AnimationBarChart.XML.Set.BarColor"),EditModel.saveColor(expressionColor.get(i)));
		}
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

		if (Language.trAll("Surface.AnimationBarChart.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.LineColor",name) && !content.trim().isEmpty()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.Use3D",name)) {
			use3D=(!content.trim().isEmpty() && !content.equals("0"));
			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.Set",name) && !content.trim().isEmpty()) {
			final String colorString=Language.trAllAttribute("Surface.AnimationBarChart.XML.Set.BarColor",node);
			final Color color;
			if (colorString.trim().isEmpty()) {
				color=Color.BLACK;
			} else {
				color=EditModel.loadColor(colorString);
				if (color==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBarChart.XML.Set.BarColor"),name,node.getParentNode().getNodeName());
			}
			expressionColor.add(color);

			expression.add(content);

			return null;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.MinValue",name)) {
			Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			minValue=D;
		}

		if (Language.trAll("Surface.AnimationBarChart.XML.MaxValue",name)) {
			Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			maxValue=D;
		}

		return null;
	}

	/**
	 * Rechenausdruck der während der Animation ausgewertet
	 * werden soll, um den darzustellenden Wert zu erhalten.
	 * @see #initAnimation(SimulationData)
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private ExpressionCalc[] animationExpression;

	/**
	 * Wertet {@link #animationExpression} aus und liefert
	 * den zu zeichnenden Wert zurück.
	 * @param simData	Simulationsdatenobjekt
	 * @param index	Auszuwertender Array-Index
	 * @return	Darzustellender Wert
	 */
	private double calcExpression(final SimulationData simData, final int index) {
		final ExpressionCalc calc=animationExpression[index];
		if (calc==null) return 0.0;
		simData.runData.setClientVariableValues(null);
		return calc.calcOrDefault(simData.runData.variableValues,simData,null,0);
	}

	/**
	 * In {@link #updateSimulationData(SimulationData, boolean)} berechnete Ausgabedaten
	 * {@link #updateSimulationData(SimulationData, boolean)}
	 */
	private double[][] dataSets;

	/**
	 * Für die Ausgabe aktiver Eintrag in {@link #dataSets}
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
			final double value=calcExpression(simData,i);
			dataSets[nextDataSet][i]=value;
			if (!needUpdate) needUpdate=(dataSets[drawActiveDataSet][i]!=value);
		}
		if (!needUpdate) return false;

		drawLock.acquireUninterruptibly();
		try {
			drawActiveDataSet=nextDataSet;
			recordedValues=dataSets[drawActiveDataSet];
		} finally {
			drawLock.release();
		}
		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		recordedValues=null;
		animationExpression=new ExpressionCalc[expression.size()];

		for (int i=0;i<expression.size();i++) {
			animationExpression[i]=new ExpressionCalc(simData.runModel.variableNames);
			if (animationExpression[i].parse(expression.get(i))>=0) animationExpression[i]=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationBarChart";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
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
	 * Liefert die Daten in Tabellenform für die Ausgabe einer Datentabelle während der Animation
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
				line.add(expression.get(i));
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

}
