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
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Basisklasse f�r Liniendiagramme und �hnliche Elemente
 * @author Alexander Herzog
 */
public abstract class ModelElementAnimationDiagramBase extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Gr��e der Markierungsboxen an den Ecken des Elements
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
	 * F�llfarbe des Kastens
	 */
	protected Color backgroundColor=null;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationDiagramBase</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationDiagramBase(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(75,75),Shapes.ShapeType.SHAPE_NONE);
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
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
	 * @param borderWidth	Neue Breite f�r die Linie
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
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationDiagramBase)) return false;

		if (borderWidth!=((ModelElementAnimationDiagramBase)element).borderWidth) return false;
		if (!((ModelElementAnimationDiagramBase)element).borderColor.equals(borderColor)) return false;
		if (!(((ModelElementAnimationDiagramBase)element).backgroundColor==null && backgroundColor==null)) {
			if (((ModelElementAnimationDiagramBase)element).backgroundColor==null || backgroundColor==null) return false;
			if (!((ModelElementAnimationDiagramBase)element).backgroundColor.equals(backgroundColor)) return false;
		}

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationDiagramBase) {
			borderWidth=((ModelElementAnimationDiagramBase)element).borderWidth;
			borderColor=((ModelElementAnimationDiagramBase)element).borderColor;
			backgroundColor=((ModelElementAnimationDiagramBase)element).backgroundColor;
		}
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
	 * System zur Darstellung der x-Achsenbeschriftung
	 */
	private AxisDrawer xAxisDrawer;

	/**
	 * Deaktiviert die Darstellung der x-Achsenbeschriftung
	 * @see #xAxisDrawer
	 * @see #setXAxis(double, double)
	 */
	protected final void setXAxisOff() {
		xAxisDrawer=null;
	}

	/**
	 * Definiert den Minimal- und den Maximalwert f�r die x-Achsenbeschriftung
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @see #xAxisDrawer
	 * @see #setXAxisOff()
	 */
	protected final void setXAxis(final double min, final double max) {
		if (xAxisDrawer==null) xAxisDrawer=new AxisDrawer();
		xAxisDrawer.setAxisValues(min,max);
	}

	/**
	 * System zur Darstellung der y-Achsenbeschriftung
	 */
	private AxisDrawer yAxisDrawer;

	/**
	 * Deaktiviert die Darstellung der y-Achsenbeschriftung
	 * @see #yAxisDrawer
	 * @see #setYAxis(double, double)
	 */
	protected final void setYAxisOff() {
		yAxisDrawer=null;
	}

	/**
	 * Definiert den Minimal- und den Maximalwert f�r die y-Achsenbeschriftung
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @see #yAxisDrawer
	 * @see #setYAxisOff()
	 */
	protected final void setYAxis(final double min, final double max) {
		if (yAxisDrawer==null) yAxisDrawer=new AxisDrawer();
		yAxisDrawer.setAxisValues(min,max);
	}

	/**
	 * Zeichnet die eigentlichen Diagrammdaten
	 * @param g	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param rectangle	Gem�� dem Zoomfaktor umgerechneter sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 */
	protected abstract void drawDiagramData(final Graphics2D g, final Rectangle rectangle, final double zoom);

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		final Graphics2D g2=(Graphics2D)graphics;

		/* Vorbereitungen */
		setClip(graphics,drawRect,null);
		final Point p=getPosition(true);
		final Dimension s=getSize();

		/* Zeichenbereich bestimmen */
		final Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));

		/* Nur zeichnen, wenn �berhaupt im sichtbaren Bereich */
		final int delta=(int)Math.round(50*zoom);
		if (drawRect.x>rectangle.x+rectangle.width+delta) return;
		if (drawRect.y>rectangle.y+rectangle.height+delta) return;
		if (drawRect.x+drawRect.width<rectangle.x-delta) return;
		if (drawRect.y+drawRect.height<rectangle.y-delta) return;

		/* Zeichenstift speichern */
		final Stroke saveStroke=g2.getStroke();

		/* Hintergrund f�llen */
		if (backgroundColor!=null) {
			g2.setColor(backgroundColor);
			g2.fill(rectangle);
		}

		/* Diagrammdaten zeichnen */
		setClip(g2,drawRect,rectangle);
		drawDiagramData(g2,rectangle,zoom);
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
			g2.draw(rectangle);
		}

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}

		/* Achsenbeschriftung ausgeben */
		if (xAxisDrawer!=null) xAxisDrawer.drawX(g2,zoom,rectangle);
		if (yAxisDrawer!=null) yAxisDrawer.drawY(g2,zoom,rectangle);

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
			g2.setColor(backgroundColor);
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
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
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