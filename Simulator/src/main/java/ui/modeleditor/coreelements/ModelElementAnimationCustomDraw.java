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
package ui.modeleditor.coreelements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.util.FastMath;

import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.elements.ElementWithAnimationDisplay;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Basisklasse für ein Animationselement, welches sich selber zeichnet.
 * @author Alexander Herzog
 */
public abstract class ModelElementAnimationCustomDraw extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Sichert ab, dass nicht gleichzeitig lesend und schreibend
	 * auf {@link #animationDouble}, {@link #animationInteger} oder
	 * {@link #animationLong} zugegriffen wird.
	 * @see #animationDouble
	 * @see #animationInteger
	 * @see #animationLong
	 * @see #getAnimationDouble()
	 * @see #setAnimationDouble(Double)
	 * @see #getAnimationInteger()
	 * @see #setAnimationInteger(Integer)
	 * @see #getAnimationLong()
	 * @see #setAnimationLong(long)
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Double-Datenobjekt für die Verbindung von
	 * Simulation und Animation.
	 * @see #getAnimationDouble()
	 * @see #setAnimationDouble(Double)
	 */
	private Double animationDouble=null;

	/**
	 * Integer-Datenobjekt für die Verbindung von
	 * Simulation und Animation.
	 * @see #getAnimationInteger()
	 * @see #setAnimationInteger(Integer)
	 */
	private Integer animationInteger=null;

	/**
	 * Long-Wert für die Verbindung von
	 * Simulation und Animation.
	 * @see #getAnimationLong()
	 * @see #setAnimationLong(long)
	 */
	private long animationLong=0;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param size	Größe der Box
	 */
	public ModelElementAnimationCustomDraw(final EditModel model, final ModelSurface surface, final Dimension size) {
		super(model,surface,size,Shapes.ShapeType.SHAPE_NONE);
	}

	/**
	 * Prüft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem geprüft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gemäß dessen der Punkt skaliert wird
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt nahe der Linie befindet
	 */
	private final boolean isNearLine(final Point p, final Point q, final Point point, final double zoom) {
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
	public final boolean containsPoint(final Point point, final double zoom) {
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
	public final Point getBorderPointPosition(final int index) {
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
	public final void setBorderPointPosition(final int index, final Point point) {
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
	private void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	/**
	 * Zeichnet das Element
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param rectangle	Abmessungen des Zeichenobjekts
	 * @param zoom	Zoomfaktor
	 */
	protected abstract void drawData(final Graphics2D graphics, final Rectangle rectangle, final double zoom);

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public final void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		final Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));

		drawData(g2,rectangle,zoom);

		boolean drawBorder=false;
		Color lineColor=Color.BLACK;

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke(3));
			drawBorder=true;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke(3));
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

		setClip(graphics,drawRect,null);
		g2.setStroke(saveStroke);
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	protected abstract void specialOutputHTML(final HTMLOutputBuilder outputBuilder);

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}

	/**
	 * Stellt den anzuzeigenden Wert ein.
	 * @param value	Anzuzeigender Wert
	 */
	protected final void setAnimationDouble(final Double value) {
		drawLock.acquireUninterruptibly();
		try {
			animationDouble=value;
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Stellt den anzuzeigenden Wert ein.
	 * @param value	Anzuzeigender Wert
	 */
	protected final void setAnimationInteger(final Integer value) {
		drawLock.acquireUninterruptibly();
		try {
			animationInteger=value;
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Stellt den anzuzeigenden Wert ein.
	 * @param value	Anzuzeigender Wert
	 */
	protected final void setAnimationLong(final long value) {
		drawLock.acquireUninterruptibly();
		try {
			animationLong=value;
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Liefert den anzuzeigenden wert.
	 * @return	Anzuzeigender Wert
	 */
	protected final Double getAnimationDouble() {
		drawLock.acquireUninterruptibly();
		try {
			return animationDouble;
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Liefert den anzuzeigenden wert.
	 * @return	Anzuzeigender Wert
	 */
	protected final Integer getAnimationInteger() {
		drawLock.acquireUninterruptibly();
		try {
			return animationInteger;
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Liefert den anzuzeigenden wert.
	 * @return	Anzuzeigender Wert
	 */
	protected final long getAnimationLong() {
		drawLock.acquireUninterruptibly();
		try {
			return animationLong;
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Aktualisiert den darzustellenden Wert basierend auf den
	 * Simulationsdaten.
	 * @param simData	Simulationsdatenobjekt
	 */
	protected abstract void updateDrawData(final SimulationData simData);

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (isPreview) return false;

		updateDrawData(simData);

		return true;
	}
}