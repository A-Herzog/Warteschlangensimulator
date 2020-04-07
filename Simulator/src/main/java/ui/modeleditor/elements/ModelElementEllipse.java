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
import java.awt.geom.Ellipse2D;
import java.net.URL;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementDecoration;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.TransparentColor;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Ellipse (zur optischen Gestaltung)
 * @author Alexander Herzog
 */
public class ModelElementEllipse extends ModelElementDecoration {
	private Color fillColor=null;
	private Color gradientColor=null;
	private double fillAlpha=1.0;

	/**
	 * Konstruktor der Klasse <code>ModelElementEllipse</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementEllipse(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50));
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ELLIPSE.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Ellipse.Tooltip");
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Ellipse.Name");
	}

	/**
	 * Liefert die aktuelle Füllfarbe der Ellipse
	 * @return	Aktuelle Füllfarbe der Ellipse (kann <code>null</code> sein für transparent)
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Stellt die Füllfarbe der Ellipse ein
	 * @param color	Füllfarbe der Ellipse (oder <code>null</code> für transparent)
	 */
	public void setFillColor(final Color color) {
		fillColor=color;
		fireChanged();
	}

	/**
	 * Liefert die optionale zweite Füllfarbe der Ellipse für Farbverläufe
	 * @return	Zweite Füllfarbe der Ellipse (kann <code>null</code> sein für einfarbig bzw. transparent)
	 */
	public Color getGradientFillColor() {
		return gradientColor;
	}

	/**
	 * Stellt die optionale zweite Füllfarbe der Ellipse für Farbverläufe ein
	 * @param color	Zweite Füllfarbe der Ellipse (oder <code>null</code> für einfarbig bzw. transparent)
	 */
	public void setGradientFillColor(final Color color) {
		gradientColor=color;
		fireChanged();
	}

	/**
	 * Liefert die Deckkraft der Hintergrundfarbe.
	 * @return	Deckkraft der Hintergrundfarbe (Wert zwischen 0 und 1 jeweils einschließlich)
	 */
	public double getFillAlpha() {
		return fillAlpha;
	}

	/**
	 * Stellt die Deckkraft der Hintergrundfarbe ein.
	 * @param fillAlpha	Deckkraft der Hintergrundfarbe (Wert zwischen 0 und 1 jeweils einschließlich)
	 */
	public void setFillAlpha(double fillAlpha) {
		this.fillAlpha=Math.max(0,Math.min(1,fillAlpha));
		fireChanged();
	}

	/**
	 * Minimal einstellbare Linienbreite (hier: 0)
	 * @return	Minimale Breite der Linie
	 */
	@Override
	protected int getMinLineWidth() {
		return 0;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;

		if (!(element instanceof ModelElementEllipse)) return false;
		final ModelElementEllipse otherEllipse=(ModelElementEllipse)element;

		if (!Objects.equals(fillColor,otherEllipse.fillColor)) return false;
		if (!Objects.equals(gradientColor,otherEllipse.gradientColor)) return false;
		if (fillAlpha!=otherEllipse.fillAlpha) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementEllipse) {
			final ModelElementEllipse copySource=(ModelElementEllipse)element;

			fillColor=copySource.fillColor;
			gradientColor=copySource.gradientColor;
			fillAlpha=copySource.fillAlpha;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementEllipse clone(final EditModel model, final ModelSurface surface) {
		final ModelElementEllipse element=new ModelElementEllipse(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private boolean ellipsePoint(final Point point, final double zoom) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Point test=new Point((int)Math.round(point.x/zoom),(int)Math.round(point.y/zoom));
		final Point middle=new Point(p.x+s.width/2,p.y+s.height/2);
		final Point centerTest=new Point(test.x-middle.x,test.y-middle.y);

		double diff=((double)centerTest.x)*centerTest.x/Math.abs((double)s.width)/Math.abs((double)s.width)*4+((double)centerTest.y)*centerTest.y/Math.abs((double)s.height)/Math.abs((double)s.height)*4;
		diff=Math.abs(diff-1)*(Math.abs(s.width)+Math.abs(s.height))/2;

		return (diff<15);
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom,1)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom,1)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom,1)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom,1)) return true;

		if (ellipsePoint(point,zoom)) return true;

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
		case 0: return new Point(p);
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

	private GradientFill gradientFill=null;
	private Rectangle gradientFillRectangle=null;
	private TransparentColor alphaColor=null;

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

		Color lineColor=color;
		boolean drawLine=(lineWidth>0);
		g2.setStroke(new BasicStroke((float)(lineWidth*zoom)));

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke((float)(Math.max(lineWidth,3)*zoom)));
			drawLine=true;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke((float)(Math.max(lineWidth,3)*zoom)));
				drawLine=true;
			}
		}

		final int x=(int)Math.round(Math.min(p.x,p.x+s.width)*zoom);
		final int y=(int)Math.round(Math.min(p.y,p.y+s.height)*zoom);
		final int w=(int)Math.round(Math.abs(s.width)*zoom);
		final int h=(int)Math.round(Math.abs(s.height)*zoom);
		final Ellipse2D.Double ellipse=new Ellipse2D.Double(x,y,w,h);
		if (fillColor!=null) {
			if (alphaColor==null) alphaColor=new TransparentColor();
			if (gradientColor==null) {
				g2.setColor(alphaColor.get(fillColor,fillAlpha));
			} else {
				if (gradientFillRectangle==null) gradientFillRectangle=new Rectangle();
				gradientFillRectangle.x=x;
				gradientFillRectangle.y=y;
				gradientFillRectangle.width=w;
				gradientFillRectangle.height=h;
				if (gradientFill==null) gradientFill=new GradientFill();
				gradientFill.set(g2,gradientFillRectangle,alphaColor.get(gradientColor,fillAlpha),alphaColor.get(fillColor,fillAlpha),true);
			}
			g2.fill(ellipse);
		}
		if (drawLine) g2.setColor(lineColor);
		g2.draw(ellipse);

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
			new ModelElementEllipseDialog(owner,ModelElementEllipse.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Ellipse.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (fillColor!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.Ellipse.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(fillColor));
			if (fillAlpha<1) sub.setAttribute(Language.trPrimary("Surface.Ellipse.XML.BackgroundColor.Alpha"),NumberTools.formatSystemNumber(fillAlpha));
		}

		if (gradientColor!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.Ellipse.XML.GradientColor"));
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

		if (Language.trAll("Surface.Ellipse.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			fillColor=color;
			final String alpha=Language.trAllAttribute("Surface.Ellipse.XML.BackgroundColor.Alpha",node);
			if (!alpha.trim().isEmpty()) {
				final Double D=NumberTools.getDouble(alpha);
				if (D==null || D<0 || D>1) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Ellipse.XML.BackgroundColor.Alpha"),name,node.getParentNode().getNodeName());
				fillAlpha=D;
			}
			return null;
		}

		if (Language.trAll("Surface.Ellipse.XML.GradientColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			gradientColor=color;
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementEllipse";
	}

	private String getHTMLDrawEllipse(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawEllipse(p1,p2,borderColor,borderWidth,fillColor) {\n");

		sb.append("  var m={x: Math.round((p1.x+p2.x)/2), y: Math.round((p1.y+p2.y)/2)};\n");
		sb.append("  var r={x: Math.abs((p1.x-p2.x)/2), y: Math.abs((p1.y-p2.y)/2)};\n");

		sb.append("  if (typeof(fillColor)!=\"undefined\") {\n");
		sb.append("    context.fillStyle=fillColor;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.ellipse(m.x,m.y,r.x,r.y,0,0,2*Math.PI);\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.ellipse(m.x,m.y,r.x,r.y,0,0,2*Math.PI);\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawEllipse",builder->getHTMLDrawEllipse(builder));

		final Point p=getPosition(true);
		final Dimension s=getSize();
		if (fillColor!=null) {
			outputBuilder.outputBody.append("drawEllipse({x: "+p.x+",y: "+p.y+"},{x: "+(p.x+s.width)+",y: "+(p.y+s.height)+"},\""+HTMLOutputBuilder.colorToHTML(color)+"\","+lineWidth+",\""+HTMLOutputBuilder.colorToHTML(fillColor)+"\");\n");
		} else {
			outputBuilder.outputBody.append("drawEllipse({x: "+p.x+",y: "+p.y+"},{x: "+(p.x+s.width)+",y: "+(p.y+s.height)+"},\""+HTMLOutputBuilder.colorToHTML(color)+"\","+lineWidth+");\n");
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
}