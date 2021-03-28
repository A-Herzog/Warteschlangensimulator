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
import java.util.concurrent.Semaphore;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.BrighterColor;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt einen sich während der Animation aktualisierenden Balken an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationBar extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Richtung in die der Balken aufgefüllt werden soll
	 * @author Alexander Herzog
	 * @see ModelElementAnimationBar#getDirection()
	 * @see ModelElementAnimationBar#setDirection(FillDirection)
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
	 * Richtung des Balken
	 * @see #getDirection()
	 * @see #setDirection(FillDirection)
	 */
	private FillDirection direction=FillDirection.DIRECTION_UP;

	/**
	 * Rechenausdruck
	 * @see #getExpression()
	 * @see #setExpression(String)
	 */
	private String expression="123";

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #simValueActive}
	 * und {@link #simValue} zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Soll {@link #simValue} (<code>true</code>) oder
	 * ein Dummy-Wert (<code>false</code>) gezeichnet
	 * werden?
	 */
	private boolean simValueActive;

	/**
	 * Zu zeichnender Wert im Bereich
	 * {@link #minValue} und {@link #maxValue}.
	 */
	private double simValue;

	/**
	 * Minimalwert für die Balkendarstellung
	 * @see #getMinValue()
	 * @see #setMinValue(double)
	 */
	private double minValue=0.0;

	/**
	 * Maximalwert für die Balkendarstellung
	 * @see #getMaxValue()
	 * @see #setMaxValue(double)
	 */
	private double maxValue=10.0;

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
	 * Farbe des Balkens
	 * @see #getBarColor()
	 * @see #setBarColor(Color)
	 */
	private Color barColor=Color.RED;

	/**
	 * Cache des Bereichsfüllung-Zeichners
	 * für {@link #fillBox(Graphics2D, Rectangle)}
	 * @see #fillBox(Graphics2D, Rectangle)
	 */
	private GradientFill filler;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationBar</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationBar(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50),Shapes.ShapeType.SHAPE_NONE);
		filler=null;
	}

	/**
	 * Stellt die Größe der umrandenden Box ein
	 * @param size	Größe der Box
	 */
	@Override
	public void setSize(final Dimension size) {
		super.setSize(size);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_BAR.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationBar.Tooltip");
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
	 * Liefert im Falle, dass Modus <code>MODE_EXPRESSION_NUMBER</code> oder
	 * <code>MODE_EXPRESSION_PERCENT</code> gewählt sind, den aktuellen Ausdruck.
	 * @return	Aktueller Ausdruck
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Stellt den Ausdruck ein, der im Falle von <code>MODE_EXPRESSION_NUMBER</code> oder
	 * <code>MODE_EXPRESSION_PERCENT</code> ausgerechnet werden soll.
	 * @param expression	Neuer Ausdruck
	 */
	public void setExpression(final String expression) {
		if (expression!=null) {
			this.expression=expression;
			fireChanged();
		}
	}

	/**
	 * Liefert den Minimalwert für die Balkendarstellung
	 * @return	Minimalwert
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * Stellt den Minimalwert für die Balkendarstellung ein
	 * @param minValue	Neuer Minimalwert
	 */
	public void setMinValue(final double minValue) {
		this.minValue=minValue;
		fireChanged();
	}

	/**
	 * Liefert den Maximalwert für die Balkendarstellung
	 * @return	Maximalwert
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Stellt den Maximalwert für die Balkendarstellung ein
	 * @param maxValue	Neuer Maximalwert
	 */
	public void setMaxValue(final double maxValue) {
		this.maxValue=maxValue;
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
	 * Liefert die aktuelle Farbe des Balkens
	 * @return	Aktuelle Farbe des Balkens
	 */
	public Color getBarColor() {
		return barColor;
	}

	/**
	 * Stellt die Farbe des Balkensein
	 * @param color	Farbe des Balkens
	 */
	public void setBarColor(final Color color) {
		if (color!=null) this.barColor=color;
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
		if (!(element instanceof ModelElementAnimationBar)) return false;

		if (direction!=((ModelElementAnimationBar)element).direction) return false;
		if (!expression.equals(((ModelElementAnimationBar)element).expression)) return false;
		if (minValue!=((ModelElementAnimationBar)element).minValue) return false;
		if (maxValue!=((ModelElementAnimationBar)element).maxValue) return false;
		if (borderWidth!=((ModelElementAnimationBar)element).borderWidth) return false;

		if (!((ModelElementAnimationBar)element).borderColor.equals(borderColor)) return false;
		if (!(((ModelElementAnimationBar)element).backgroundColor==null && backgroundColor==null)) {
			if (((ModelElementAnimationBar)element).backgroundColor==null || backgroundColor==null) return false;
			if (!((ModelElementAnimationBar)element).backgroundColor.equals(backgroundColor)) return false;
		}
		if (!((ModelElementAnimationBar)element).barColor.equals(barColor)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationBar) {
			direction=((ModelElementAnimationBar)element).direction;
			expression=((ModelElementAnimationBar)element).expression;
			minValue=((ModelElementAnimationBar)element).minValue;
			maxValue=((ModelElementAnimationBar)element).maxValue;
			borderWidth=((ModelElementAnimationBar)element).borderWidth;

			borderColor=((ModelElementAnimationBar)element).borderColor;
			backgroundColor=((ModelElementAnimationBar)element).backgroundColor;
			barColor=((ModelElementAnimationBar)element).barColor;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationBar clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationBar element=new ModelElementAnimationBar(model,surface);
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
	 * Füllt ein Rechteck gemäß dem in {@link #simValue} angegebenen Füllstand.
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Teilweise zu füllendes Rechteck
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 * @see #simValue
	 * @see #simValueActive
	 */
	private void fillBox(final Graphics2D g, final Rectangle rectangle) {
		double d=0;
		drawLock.acquireUninterruptibly();
		try {
			d=simValueActive?simValue:((maxValue-minValue)/3);
		} finally {
			drawLock.release();
		}

		int max=1;
		switch (direction) {
		case DIRECTION_UP:
			max=rectangle.height;
			break;
		case DIRECTION_RIGHT:
			max=rectangle.width;
			break;
		case DIRECTION_DOWN:
			max=rectangle.height;
			break;
		case DIRECTION_LEFT:
			max=rectangle.width;
			break;
		}

		int delta=(int)FastMath.round(max*(1-(d-minValue)/(maxValue-minValue)));
		delta=FastMath.max(0,delta);
		delta=FastMath.min(max,delta);
		Rectangle r=null;

		switch (direction) {
		case DIRECTION_UP:
			r=new Rectangle(rectangle.x,rectangle.y+delta,rectangle.width,rectangle.height-delta);
			break;
		case DIRECTION_RIGHT:
			r=new Rectangle(rectangle.x,rectangle.y,rectangle.width-delta,rectangle.height);
			break;
		case DIRECTION_DOWN:
			r=new Rectangle(rectangle.x,rectangle.y,rectangle.width,rectangle.height-delta);
			break;
		case DIRECTION_LEFT:
			r=new Rectangle(rectangle.x+delta,rectangle.y,rectangle.width-delta,rectangle.height);
			break;
		}
		if (r!=null) {
			if (filler==null) filler=new GradientFill(false);
			filler.set(g,r,barColor,true);
			g.fill(r);
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

		final Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
		if (backgroundColor!=null) {
			g2.setColor(backgroundColor);
			g2.fill(rectangle);
		}

		fillBox(g2,rectangle);

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
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationBar.Name");
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
			new ModelElementAnimationBarDialog(owner,ModelElementAnimationBar.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationBar.XML.Root");
	}

	/**
	 * Übersetzt die Füllrichtung in einen xml-Ausdruck.
	 * @param direction	Füllrichtung
	 * @return	Füllrichtung in einer xml-speicherbaren Form
	 * @see #addPropertiesDataToXML(Document, Element)
	 */
	private String getDirectionString(final FillDirection direction) {
		switch (direction) {
		case DIRECTION_DOWN: return Language.trPrimary("Surface.AnimationBar.XML.DataArea.Direction.Down");
		case DIRECTION_LEFT: return Language.trPrimary("Surface.AnimationBar.XML.DataArea.Direction.Left");
		case DIRECTION_RIGHT: return Language.trPrimary("Surface.AnimationBar.XML.DataArea.Direction.Right");
		case DIRECTION_UP: return Language.trPrimary("Surface.AnimationBar.XML.DataArea.Direction.Up");
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBar.XML.DataExpression"));
		node.appendChild(sub);
		sub.setTextContent(expression);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBar.XML.DataArea"));
		node.appendChild(sub);
		sub.setAttribute(Language.trPrimary("Surface.AnimationBar.XML.DataArea.Min"),NumberTools.formatSystemNumber(minValue));
		sub.setAttribute(Language.trPrimary("Surface.AnimationBar.XML.DataArea.Max"),NumberTools.formatSystemNumber(maxValue));
		sub.setAttribute(Language.trPrimary("Surface.AnimationBar.XML.DataArea.Direction"),getDirectionString(direction));

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBar.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBar.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		if (backgroundColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBar.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(backgroundColor));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBar.XML.BarColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(barColor));
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

		if (Language.trAll("Surface.AnimationBar.XML.DataExpression",name)) {
			expression=content;
			return null;
		}

		if (Language.trAll("Surface.AnimationBar.XML.DataArea",name)) {
			Double D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationBar.XML.DataArea.Min",node)));
			if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBar.XML.DataArea.Min"),name,node.getParentNode().getNodeName());
			minValue=D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationBar.XML.DataArea.Max",node)));
			if (D==null || D<=minValue) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBar.XML.DataArea.Max"),name,node.getParentNode().getNodeName());
			maxValue=D;

			final String s=Language.trAllAttribute("Surface.AnimationBar.XML.DataArea.Direction",node);
			if (!s.isEmpty()) {
				boolean ok=false;
				if (!ok && Language.trAll("Surface.AnimationBar.XML.DataArea.Direction.Up",s)) {direction=FillDirection.DIRECTION_UP; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBar.XML.DataArea.Direction.Right",s)) {direction=FillDirection.DIRECTION_RIGHT; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBar.XML.DataArea.Direction.Down",s)) {direction=FillDirection.DIRECTION_DOWN; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBar.XML.DataArea.Direction.Left",s)) {direction=FillDirection.DIRECTION_LEFT; ok=true;}
				if (!ok) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBar.XML.DataArea.Direction"),name,node.getParentNode().getNodeName());
			}

			return null;
		}

		if (Language.trAll("Surface.AnimationBar.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationBar.XML.LineColor",name) && !content.trim().isEmpty()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBar.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBar.XML.BarColor",name) && !content.trim().isEmpty()) {
			barColor=EditModel.loadColor(content);
			if (barColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		return null;
	}

	/**
	 * Rechenausdruck der während der Animation ausgewertet
	 * werden soll, um den darzustellenden Wert zu erhalten.
	 * @see #initAnimation(SimulationData)
	 * @see #calcExpression(SimulationData)
	 */
	private ExpressionCalc animationExpression;

	/**
	 * Wertet {@link #animationExpression} aus und liefert
	 * den zu zeichnenden Wert zurück.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Darzustellender Wert
	 * @see #simValue
	 */
	private double calcExpression(final SimulationData simData) {
		if (animationExpression==null) return 0.0;
		simData.runData.setClientVariableValues(null);
		return animationExpression.calcOrDefault(simData.runData.variableValues,simData,null,0);
	}

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (isPreview) return false;
		double d=calcExpression(simData);
		drawLock.acquireUninterruptibly();
		try {
			simValue=d;
		} finally {
			drawLock.release();
		}
		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		animationExpression=new ExpressionCalc(simData.runModel.variableNames);
		if (animationExpression.parse(expression)>=0) {
			animationExpression=null;
		} else {
			simValue=0.0;
			simValueActive=true;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationBar";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationBar(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationBar(rect,barRect,barColor1,barColor2,borderColor,borderWidth,fillColor) {\n");

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

		sb.append("  var gradient=context.createLinearGradient(Math.round(barRect.x+barRect.w/2),barRect.y+barRect.h,Math.round(barRect.x+barRect.w/2),barRect.y);\n");
		sb.append("  gradient.addColorStop(0,barColor1);\n");
		sb.append("  gradient.addColorStop(1,barColor2);\n");
		sb.append("  context.fillStyle=gradient;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(barRect.x,barRect.y);\n");
		sb.append("  context.lineTo(barRect.x+barRect.w,barRect.y);\n");
		sb.append("  context.lineTo(barRect.x+barRect.w,barRect.y+barRect.h);\n");
		sb.append("  context.lineTo(barRect.x,barRect.y+barRect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

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
		outputBuilder.addJSUserFunction("drawAnimationBar",builder->getHTMLAnimationBar(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String bar1;
		final String bar2;
		if (SetupData.getSetup().useGradients) {
			bar1="\""+HTMLOutputBuilder.colorToHTML(barColor)+"\"";
			BrighterColor color2=new BrighterColor();
			bar2="\""+HTMLOutputBuilder.colorToHTML(color2.get(barColor))+"\"";
		} else {
			bar1="\""+HTMLOutputBuilder.colorToHTML(barColor)+"\"";
			bar2=bar1;
		}
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		final String barRect;
		int z;
		switch (direction) {
		case DIRECTION_DOWN:
			z=d.height/3;
			barRect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+z+"}";
			break;
		case DIRECTION_LEFT:
			z=d.width/3;
			barRect="{x: "+p.x+", y: "+p.y+", w: "+z+", h: "+d.height+"}";
			break;
		case DIRECTION_RIGHT:
			z=d.width/3;
			barRect="{x: "+p.x+", y: "+(p.y+d.width-z)+", w: "+z+", h: "+z+"}";
			break;
		case DIRECTION_UP:
			z=d.height/3;
			barRect="{x: "+p.x+", y: "+(p.y+d.height-z)+", w: "+d.width+", h: "+z+"}";
			break;
		default:
			z=d.height/3;
			barRect="{x: "+p.x+", y: "+(p.y+d.height-z)+", w: "+d.width+", h: "+z+"}";
			break;
		}

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationBar("+rect+","+barRect+","+bar1+","+bar2+","+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationBar("+rect+","+barRect+","+bar1+","+bar2+","+border+","+borderWidth+","+fill+");\n");
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
}