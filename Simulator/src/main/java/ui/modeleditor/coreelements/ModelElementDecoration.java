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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Basisklasse f�r alle Dekorationselemente (Linie, Kasten, ...)
 * @author Alexander Herzog
 */
public class ModelElementDecoration extends ModelElementPosition {
	/**
	 * Vorgabe Linienbreite
	 */
	protected static final int DEFAULT_LINE_WIDTH=2;

	/**
	 * Vorgabe Linienfarbe
	 */
	protected static final Color DEFAULT_COLOR=Color.BLACK;

	/**
	 * Gr��e der Markierungsboxen an den Ecken der Elemente
	 */
	protected static final int SELECT_BOX_SIZE=7;

	/**
	 * Linienbreite
	 */
	protected int lineWidth=DEFAULT_LINE_WIDTH;

	/**
	 * Linienfarbe
	 */
	protected Color color=DEFAULT_COLOR;

	/**
	 * Konstruktor der Klasse <code>ModelElementPosition</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param size	Gr��e der Box
	 */
	public ModelElementDecoration(final EditModel model, final ModelSurface surface, final Dimension size) {
		super(model,surface,size,Shapes.ShapeType.SHAPE_NONE);
	}

	@Override
	public Icon buildIcon() {
		return null; /* Die Elemente k�nnen zu gro� werden, daher hier keine Vorschau. */
	}

	/**
	 * Liefert die aktuelle Breite der Linie
	 * @return	Aktuelle breite der Linie
	 */
	public int getLineWidth() {
		return lineWidth;
	}

	/**
	 * Minimal einstellbare Linienbreite (Standard: 1)
	 * @return	Minimale Breite der Linie
	 */
	protected int getMinLineWidth() {
		return 1;
	}

	/**
	 * Stellt die breite der Linie ein
	 * @param lineWidth	Neue Breite f�r die Linie
	 */
	public void setLineWidth(final int lineWidth) {
		if (lineWidth>=getMinLineWidth() && lineWidth<=50) this.lineWidth=lineWidth;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Farbe der Linie
	 * @return	Aktuelle Farbe der Linie
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Stellt die Farbe der Linie ein
	 * @param color	Farbe der Linie
	 */
	public void setColor(final Color color) {
		if (color!=null) this.color=color;
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

		if (!(element instanceof ModelElementDecoration)) return false;
		if (((ModelElementDecoration)element).lineWidth!=lineWidth) return false;
		if (!((ModelElementDecoration)element).color.equals(color)) return false;
		if (!((ModelElementDecoration)element).getSize().equals(getSize())) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDecoration) {
			lineWidth=((ModelElementDecoration)element).lineWidth;
			color=((ModelElementDecoration)element).color;
		}
	}

	/**
	 * Pr�ft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gem�� dessen der Punkt skaliert wird
	 * @param lineWidth	Linienbreite
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt nahe der Linie befindet
	 */
	protected final boolean isNearLine(final Point p, final Point q, final Point point, final double zoom, final int lineWidth) {
		final Point v=new Point(q.x-p.x,q.y-p.y); /* Verbindungsvektor P->Q */
		final Point x=new Point((int)FastMath.round(point.x/zoom),(int)FastMath.round(point.y/zoom));

		double alpha=0;
		if (v.y!=0) {
			alpha=((double)v.y)/(v.x*v.x+v.y*v.y)*(x.y-((double)v.x)/v.y*(p.x-x.x)-p.y);
		} else {
			alpha=((double)v.x)/(v.y*v.y+v.x*v.x)*(x.x-((double)v.y)/v.x*(p.y-x.y)-p.x);
		}
		final Point y=new Point((int)FastMath.round(p.x+alpha*v.x),(int)FastMath.round(p.y+alpha*v.y)); /* Lotfu�punkt von X auf P->Q */

		if (FastMath.abs(x.x-y.x)>lineWidth/2.0*zoom+MAX_POINT_DELTA || FastMath.abs(x.y-y.y)>lineWidth/2.0*zoom+MAX_POINT_DELTA) return false; /* Abstand von Gerade zu gro�? */
		final double len=FastMath.sqrt(v.x*v.x+v.y*v.y);

		if (alpha*len<-MAX_POINT_DELTA || alpha*len>len+MAX_POINT_DELTA) return false; /* Fu�punkt vor Beginn oder nach Ende der Strecke P->Q? */

		return true;
	}

	/**
	 * Zeichnet eine Markierungs-Box an einer Ecke des Elements
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param point	Position des Punktes
	 * @param zoom	Zoomfaktor
	 */
	protected final void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		final Stroke saveStroke=graphics.getStroke();
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
		graphics.setStroke(saveStroke);
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

		sub=doc.createElement(Language.trPrimary("Surface.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+lineWidth);

		sub=doc.createElement(Language.trPrimary("Surface.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));
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

		if (Language.trAll("Surface.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			lineWidth=I;
			return null;
		}

		if (Language.trAll("Surface.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		return null;
	}
}