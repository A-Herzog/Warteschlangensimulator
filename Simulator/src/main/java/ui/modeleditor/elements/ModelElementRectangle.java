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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

import javax.swing.Icon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementDecoration;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.TransparentColor;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Rechteck (zur optischen Gestaltung)
 * @author Alexander Herzog
 */
public class ModelElementRectangle extends ModelElementDecoration {
	/**
	 * F�llfarbe des Kastens (kann <code>null</code> sein f�r transparent)
	 * @see #getFillColor()
	 * @see #setFillColor(Color)
	 */
	private Color fillColor=null;

	/**
	 * Optionale zweite F�llfarbe des Kastens f�r Farbverl�ufe
	 * @see #getGradientFillColor()
	 * @see #setGradientFillColor(Color)
	 */
	private Color gradientColor=null;

	/**
	 * Deckkraft der Hintergrundfarbe
	 * @see #getFillAlpha()
	 * @see #setFillAlpha(double)
	 */
	private double fillAlpha=1.0;

	/**
	 * Eckenabrundungsfaktor (Wert von 0 bis 1)
	 * @see #getRounding()
	 * @see #setRounding(double)
	 */
	private double rounding=0;

	/**
	 * Drehwinkel (0..90)
	 */
	private double rotationAlpha=0;

	/**
	 * Konstruktor der Klasse <code>ModelElementRectangle</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementRectangle(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50));
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_RECTANGLE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Rectangle.Tooltip");
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Rectangle.Name");
	}

	/**
	 * Liefert die aktuelle F�llfarbe des Kastens
	 * @return	Aktuelle F�llfarbe des Kastens (kann <code>null</code> sein f�r transparent)
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Stellt die F�llfarbe des Kastens ein
	 * @param color	F�llfarbe des Kastens (oder <code>null</code> f�r transparent)
	 */
	public void setFillColor(final Color color) {
		fillColor=color;
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
	 * Liefert die Deckkraft der Hintergrundfarbe.
	 * @return	Deckkraft der Hintergrundfarbe (Wert zwischen 0 und 1 jeweils einschlie�lich)
	 */
	public double getFillAlpha() {
		return fillAlpha;
	}

	/**
	 * Stellt die Deckkraft der Hintergrundfarbe ein.
	 * @param fillAlpha	Deckkraft der Hintergrundfarbe (Wert zwischen 0 und 1 jeweils einschlie�lich)
	 */
	public void setFillAlpha(double fillAlpha) {
		this.fillAlpha=Math.max(0,Math.min(1,fillAlpha));
		fireChanged();
	}

	/**
	 * Liefert den Eckenabrundungsfaktor.
	 * @return	Eckenabrundungsfaktor (Wert von 0 bis 1)
	 */
	public double getRounding() {
		return Math.max(0,Math.min(1,rounding));
	}

	/**
	 * Stellt den Eckenabrundungsfaktor ein.
	 * @param rounding	Eckenabrundungsfaktor (Wert von 0 bis 1)
	 */
	public void setRounding(double rounding) {
		this.rounding=Math.max(0,Math.min(1,rounding));
		fireChanged();
	}


	/**
	 * Liefert den Drehwinkel.
	 * @return	Drehwinkel (Wert von 0 bis 90)
	 */
	public double getRotationAlpha() {
		return rotationAlpha;
	}

	/**
	 * Stellt den Drehwinkel ein.
	 * @param rotationAlpha	Drehwinkel (Wert von 0 bis 90)
	 */
	public void setRotationAlpha(double rotationAlpha) {
		rotationAlpha=rotationAlpha%90.0;
		if (rotationAlpha<0) rotationAlpha+=90;
		if (Math.abs(this.rotationAlpha-rotationAlpha)<0.0001) return;
		this.rotationAlpha=rotationAlpha;
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
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;

		if (!(element instanceof ModelElementRectangle)) return false;
		final ModelElementRectangle otherRectangle=(ModelElementRectangle)element;

		if (!Objects.equals(fillColor,otherRectangle.fillColor)) return false;
		if (!Objects.equals(gradientColor,otherRectangle.gradientColor)) return false;
		if (fillAlpha!=otherRectangle.fillAlpha) return false;
		if (rounding!=otherRectangle.rounding) return false;
		if (Math.abs(rotationAlpha-otherRectangle.rotationAlpha)>0.0001) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementRectangle) {
			final ModelElementRectangle copySource=(ModelElementRectangle)element;

			fillColor=copySource.fillColor;
			gradientColor=copySource.gradientColor;
			fillAlpha=copySource.fillAlpha;
			rounding=copySource.rounding;
			rotationAlpha=copySource.rotationAlpha;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementRectangle clone(final EditModel model, final ModelSurface surface) {
		final ModelElementRectangle element=new ModelElementRectangle(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Pr�ft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom,lineWidth)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom,lineWidth)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom,lineWidth)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom,lineWidth)) return true;

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

	/**
	 * Objekt f�r eine Farbverlaufsf�llung
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private GradientFill gradientFill=null;

	/**
	 * Bereich f�r die Farbverlaufsf�llung
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Rectangle gradientFillRectangle=null;

	/**
	 * Objekt zur Berechnung einer teiltransparenten Farbe
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private TransparentColor alphaColor=null;

	/**
	 * Darstellung des Rechtecks
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param w	Breite des Rechtecks
	 * @param h	H�he des Rechtecks
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	private void drawRectangle(final Graphics graphics, final int x, final int y, final int w, final int h, final double zoom, final boolean showSelectionFrames) {
		final Graphics2D g2=(Graphics2D)graphics;

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

		final Shape shape;
		if (rounding<0.01) {
			shape=new Rectangle(x,y,w,h);
		} else {
			final double r=Math.min(w,h)*Math.min(1,rounding);
			shape=new RoundRectangle2D.Double(x,y,w,h,r,r);
		}
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
				if (gradientFill==null) gradientFill=new GradientFill(false);
				gradientFill.set(g2,gradientFillRectangle,alphaColor.get(gradientColor,fillAlpha),alphaColor.get(fillColor,fillAlpha),true);
			}
			g2.fill(shape);
		}
		g2.setColor(lineColor);
		if (drawLine) g2.draw(shape);
	}

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

		final int x=(int)Math.round(Math.min(p.x,p.x+s.width)*zoom);
		final int y=(int)Math.round(Math.min(p.y,p.y+s.height)*zoom);
		final int w=(int)Math.round(Math.abs(s.width)*zoom);
		final int h=(int)Math.round(Math.abs(s.height)*zoom);

		double r=rotationAlpha%90.0;
		if (r<0) r+=90.0;

		if (r!=0) g2.rotate(r/180.0*Math.PI,x+w/2,y+h/2);
		drawRectangle(graphics,x,y,w,h,zoom,showSelectionFrames);

		if (r!=0) g2.rotate(-r/180.0*Math.PI,x+w/2,y+h/2);

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
			new ModelElementRectangleDialog(owner,ModelElementRectangle.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementRectangleDialog(owner,ModelElementRectangle.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Rectangle.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (fillColor!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.Rectangle.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(fillColor));
			if (fillAlpha<1) sub.setAttribute(Language.trPrimary("Surface.Rectangle.XML.BackgroundColor.Alpha"),NumberTools.formatSystemNumber(fillAlpha));
		}

		if (gradientColor!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.Rectangle.XML.GradientColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(gradientColor));
		}

		if (rounding>0) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.Rectangle.XML.Rounding"));
			node.appendChild(sub);
			sub.setTextContent(NumberTools.formatSystemNumber(rounding));
		}

		if (rotationAlpha!=0.0) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.Rectangle.XML.Rotation"));
			node.appendChild(sub);
			sub.setTextContent(NumberTools.formatSystemNumber(rotationAlpha));
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

		if (Language.trAll("Surface.Rectangle.XML.BackgroundColor",name) && !content.isBlank()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			fillColor=color;
			final String alpha=Language.trAllAttribute("Surface.Rectangle.XML.BackgroundColor.Alpha",node);
			if (!alpha.isBlank()) {
				final Double D=NumberTools.getDouble(alpha);
				if (D==null || D<0 || D>1) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Rectangle.XML.BackgroundColor.Alpha"),name,node.getParentNode().getNodeName());
				fillAlpha=D;
			}
			return null;
		}

		if (Language.trAll("Surface.Rectangle.XML.GradientColor",name) && !content.isBlank()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			gradientColor=color;
			return null;
		}

		if (Language.trAll("Surface.Rectangle.XML.Rounding",name)) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			rounding=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.Rectangle.XML.Rotation",name)) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			setRotationAlpha(D.doubleValue());
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementRectangle";
	}

	/**
	 * Liefert die Javascript-Daten f�r die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten f�r die Station
	 */
	private String getHTMLDrawRectangle(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawRectangleBox(p1,p2,borderColor,borderWidth,fillColor) {\n");

		sb.append("  if (typeof(fillColor)!=\"undefined\") {\n");
		sb.append("    context.fillStyle=fillColor;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(p1.x,p1.y);\n");
		sb.append("    context.lineTo(p2.x,p1.y);\n");
		sb.append("    context.lineTo(p2.x,p2.y);\n");
		sb.append("    context.lineTo(p1.x,p2.y);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(p1.x,p1.y);\n");
		sb.append("    context.lineTo(p2.x,p1.y);\n");
		sb.append("    context.lineTo(p2.x,p2.y);\n");
		sb.append("    context.lineTo(p1.x,p2.y);\n");
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
		outputBuilder.addJSUserFunction("drawRectangleBox",builder->getHTMLDrawRectangle(builder));

		final Point p=getPosition(true);
		final Dimension s=getSize();
		if (fillColor!=null) {
			outputBuilder.outputBody.append("drawRectangleBox({x: "+p.x+",y: "+p.y+"},{x: "+(p.x+s.width)+",y: "+(p.y+s.height)+"},\""+HTMLOutputBuilder.colorToHTML(color)+"\","+lineWidth+",\""+HTMLOutputBuilder.colorToHTML(fillColor)+"\");\n");
		} else {
			outputBuilder.outputBody.append("drawRectangleBox({x: "+p.x+",y: "+p.y+"},{x: "+(p.x+s.width)+",y: "+(p.y+s.height)+"},\""+HTMLOutputBuilder.colorToHTML(color)+"\","+lineWidth+");\n");
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