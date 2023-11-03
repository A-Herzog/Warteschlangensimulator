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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Basisklasse für Liniendiagramme und ähnliche Elemente
 * @author Alexander Herzog
 */
public abstract class ModelElementAnimationDiagramBase extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Breite der Linie
	 */
	protected int borderWidth=1;

	/**
	 * Farbe der Linie
	 */
	protected Color borderColor=Color.BLACK;

	/**
	 * Füllfarbe des Kastens
	 */
	protected Color backgroundColor=new Color(240,240,240);

	/**
	 * Optionale zweite Füllfarbe des Kastens für Farbverläufe
	 * @see #getGradientFillColor()
	 * @see #setGradientFillColor(Color)
	 */
	protected Color gradientColor=null;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationDiagramBase</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationDiagramBase(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(75,75),Shapes.ShapeType.SHAPE_NONE);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM.getIcon();
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
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationDiagramBase)) return false;
		final ModelElementAnimationDiagramBase otherDiagram=(ModelElementAnimationDiagramBase)element;

		if (borderWidth!=otherDiagram.borderWidth) return false;
		if (!Objects.equals(borderColor,otherDiagram.borderColor)) return false;
		if (!Objects.equals(backgroundColor,otherDiagram.backgroundColor)) return false;
		if (!Objects.equals(gradientColor,otherDiagram.gradientColor)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationDiagramBase) {
			final ModelElementAnimationDiagramBase copySource=(ModelElementAnimationDiagramBase)element;
			borderWidth=copySource.borderWidth;
			borderColor=copySource.borderColor;
			backgroundColor=copySource.backgroundColor;
			gradientColor=copySource.gradientColor;
		}
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
	 * System zur Darstellung der x-Achsenbeschriftung
	 */
	private final AxisDrawer xAxisDrawer=new AxisDrawer();

	/**
	 * Definiert den Minimal- und den Maximalwert für die x-Achsenbeschriftung
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param mode	Darstellungsart
	 * @param label	Beschriftungstext an der Achse
	 * @see #xAxisDrawer
	 */
	protected final void setXAxis(final double min, final double max, final AxisDrawer.Mode mode, final String label) {
		xAxisDrawer.setAxisValues(min,max,mode,label);
	}

	/**
	 * Definiert den Minimalwert für die x-Achsenbeschriftung mit Zeitwerten (Maximalwert ist "Jetzt")
	 * @param min	Minimaler Wert
	 * @param mode	Darstellungsart
	 * @param label	Beschriftungstext an der Achse
	 * @see #xAxisDrawer
	 */
	protected final void setTimeXAxis(final long min, final AxisDrawer.Mode mode, final String label) {
		xAxisDrawer.setAxisTimeValues(min,mode,label);
	}

	/**
	 * System zur Darstellung der y-Achsenbeschriftung
	 */
	private final AxisDrawer yAxisDrawer=new AxisDrawer(true);

	/**
	 * Definiert den Minimal- und den Maximalwert für die y-Achsenbeschriftung
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param mode	Darstellungsart
	 * @param label	Beschriftungstext an der Achse
	 * @see #yAxisDrawer
	 */
	protected final void setYAxis(final double min, final double max, final AxisDrawer.Mode mode, final String label) {
		yAxisDrawer.setAxisValues(min,max,mode,label);
	}

	/**
	 * Zeichnet die eigentlichen Diagrammdaten
	 * @param g	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param rectangle	Gemäß dem Zoomfaktor umgerechneter sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 */
	protected abstract void drawDiagramData(final Graphics2D g, final Rectangle rectangle, final double zoom);

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
		final Graphics2D g2=(Graphics2D)graphics;

		/* Vorbereitungen */
		setClip(graphics,drawRect,null);
		final Point p=getPosition(true);
		final Dimension s=getSize();

		/* Zeichenbereich bestimmen */
		if (drawRectangle==null || drawRectangleX!=p.x || drawRectangleY!=p.y || drawRectangleW!=s.width || drawRectangleH!=s.height || drawRectangleZoom!=zoom) {
			drawRectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
			drawRectangleX=p.x;
			drawRectangleY=p.y;
			drawRectangleW=s.width;
			drawRectangleH=s.height;
			drawRectangleZoom=zoom;
		}

		/* Nur zeichnen, wenn überhaupt im sichtbaren Bereich */
		final int delta=(int)Math.round(50*zoom);
		if (drawRect.x>drawRectangle.x+drawRectangle.width+delta) return;
		if (drawRect.y>drawRectangle.y+drawRectangle.height+delta) return;
		if (drawRect.x+drawRect.width<drawRectangle.x-delta) return;
		if (drawRect.y+drawRect.height<drawRectangle.y-delta) return;

		/* Zeichenstift speichern */
		final Stroke saveStroke=g2.getStroke();

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

		/* Diagrammdaten zeichnen */
		setClip(g2,drawRect,drawRectangle);
		drawDiagramData(g2,drawRectangle,zoom);
		setClip(g2,drawRect,null);

		/* Rahmen zeichnen */
		boolean drawBorder=false;
		Color lineColor=borderColor;
		if (borderWidth>0) {
			g2.setStroke(new BasicStroke((float)(borderWidth*zoom)));
			drawBorder=true;
		}

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke((float)(FastMath.max(borderWidth,3)*zoom)));
			drawBorder=true;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke((float)(FastMath.max(borderWidth,3)*zoom)));
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

		/* Achsenbeschriftung ausgeben */
		xAxisDrawer.drawX(g2,zoom,drawRectangle);
		yAxisDrawer.drawY(g2,zoom,drawRectangle);

		/* Zeichenstift wiederherstellen */
		g2.setStroke(saveStroke);
	}

	@Override
	public Icon buildIcon() {
		final Dimension size=getSize();
		final int width;
		final int height;
		if (size.width<0) size.width=-size.width;
		if (size.height<0) size.height=-size.height;
		if (size.width>150 || size.height>150) {
			width=Math.min(150,size.width*150/size.height);
			height=size.height*width/size.width;
		} else {
			width=size.width;
			height=size.height;
		}

		final BufferedImage image=new BufferedImage(width+1,height+1,BufferedImage.TYPE_4BYTE_ABGR);

		final Graphics2D g2=(Graphics2D)image.getGraphics();
		final Stroke saveStroke=g2.getStroke();

		final Rectangle rectangle=new Rectangle(0,0,width,height);
		if (backgroundColor!=null) {
			if (gradientColor==null) {
				g2.setColor(backgroundColor);
			} else {
				if (gradientFill==null) gradientFill=new GradientFill(false);
				gradientFill.set(g2,rectangle,gradientColor,backgroundColor,true);
			}
			g2.fill(rectangle);
		}

		drawDiagramData(g2,rectangle,1.0);

		boolean drawBorder=false;
		Color lineColor=borderColor;
		if (borderWidth>0) {
			g2.setStroke(new BasicStroke(borderWidth));
			drawBorder=true;
		}

		if (drawBorder) {
			g2.setColor(lineColor);
			g2.draw(rectangle);
		}

		g2.setStroke(saveStroke);

		return new ImageIcon(image);
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		if (backgroundColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(backgroundColor));
		}

		if (gradientColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.GradientColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(gradientColor));
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

		if (Language.trAll("Surface.AnimationDiagram.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.LineColor",name) && !content.trim().isEmpty()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.GradientColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			gradientColor=color;
			return null;
		}

		return null;
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

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.BorderWidth"),borderWidth,newBorderWidth->{if (newBorderWidth>=0) borderWidth=newBorderWidth;});
	}
}