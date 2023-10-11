/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementEllipse;
import ui.modeleditor.elements.ModelElementLine;
import ui.modeleditor.elements.ModelElementLink;
import ui.modeleditor.elements.ModelElementRectangle;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.elements.ModelElementVertex;
import ui.modeleditor.fastpaint.Shapes;
import xml.XMLTools;

/**
 * Ermöglicht den Export der Zeichnung als Draw.io-Diagramm<br>
 * <a href="https://drawio-app.com/">https://drawio-app.com/</a>
 * @author Alexander Herzog
 */
public class DrawIOExport {
	/** Ausgabe-xml-Objekt */
	private final XMLTools xml;
	/** Ausgabe-xml-Dokument */
	private final Document doc;
	/** Repräsentation des "mxGraphModel"-xml-Elements in der Ausgabedatei */
	private final Element graphModel;
	/** Wurzelelement des Ausgabe-xml-Dokuments */
	private final Element root;
	/** Zuordnung von Stations-IDs zu Draw.io-IDs */
	private final Map<Integer,String> idsMap;
	/** Basis-ID für die Ausgabeelemente */
	private final String baseId;
	/** Fortlaufende ID der Ausgabeelemente */
	private int drawIds;

	/**
	 * Konstruktor der Klasse
	 * @param file	Datei in die die Ausgabe erfolgen soll
	 */
	public DrawIOExport(final File file) {
		idsMap=new HashMap<>();
		drawIds=1;
		baseId=generateID(13)+"-"+generateID(6);

		xml=new XMLTools(file);

		final Element root=xml.generateRoot("mxfile");
		root.setAttribute("compressed","false");
		root.setAttribute("type","device");
		doc=root.getOwnerDocument();

		final Element diagram=doc.createElement("diagram");
		root.appendChild(diagram);
		diagram.setAttribute("id",generateID(20));

		diagram.appendChild(graphModel=doc.createElement("mxGraphModel"));
		graphModel.setAttribute("grid","1");
		graphModel.setAttribute("gridSize","10");
		graphModel.setAttribute("guides","1");
		graphModel.setAttribute("tooltips","1");
		graphModel.setAttribute("connect","1");
		graphModel.setAttribute("arrows","1");
		graphModel.setAttribute("fold","1");
		graphModel.setAttribute("page","1");
		graphModel.setAttribute("pageScale","1");
		graphModel.setAttribute("math","0");
		graphModel.setAttribute("shadow","0");

		graphModel.appendChild(this.root=doc.createElement("root"));

		Element cell;
		this.root.appendChild(cell=doc.createElement("mxCell"));
		cell.setAttribute("id","0");
		this.root.appendChild(cell=doc.createElement("mxCell"));
		cell.setAttribute("id","1");
		cell.setAttribute("parent","0");
	}

	/**
	 * Gültige Zeichen für eine ID
	 * @see #generateID(int)
	 */
	private static final String idChars="01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * Erzeugt eine ID einer vorgegebenen Länge
	 * @param len	Länge der ID
	 * @return	ID
	 * @see #baseId
	 */
	private String generateID(final int len) {
		final StringBuilder id=new StringBuilder();
		for (int i=0;i<len;i++) id.append(idChars.charAt((int)Math.floor(Math.random()*idChars.length())));
		return id.toString();
	}

	/**
	 * Fügt eine Station zu dem Ausgabedokument hinzu
	 * @param element	Auszugebende Station
	 * @return	Liefert den xml-Knoten im Ausgabedokument
	 */
	private Element addElement(final ModelElement element) {
		final Element node=doc.createElement("mxCell");

		final String id=baseId+"-"+drawIds;
		drawIds++;
		idsMap.put(element.getId(),id);

		node.setAttribute("id",id);
		node.setAttribute("parent","1");
		root.appendChild(node);

		return node;
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" und auch Umlaute usw.
	 * in ihre entsprechenden HTML-Entitäten um. Außerdem werden Zeilenumbrüche
	 * durch &lt;br&gt; umgewandelt.
	 * @param text	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTML(String text) {
		text=text.trim();
		final StringBuilder result=new StringBuilder();
		for (int i=0;i<text.length();i++) {
			final char c=text.charAt(i);
			if (c=='ä') {result.append("&auml;"); continue;}
			if (c=='ö') {result.append("&ouml;"); continue;}
			if (c=='ü') {result.append("&uuml;"); continue;}
			if (c=='ß') {result.append("&szlig;"); continue;}
			if (c=='Ä') {result.append("&Auml;"); continue;}
			if (c=='Ö') {result.append("&Ouml;"); continue;}
			if (c=='Ü') {result.append("&Uuml;"); continue;}
			if (c=='&') {result.append("&amp;"); continue;}
			if (c=='<') {result.append("&lt;"); continue;}
			if (c=='>') {result.append("&gt;"); continue;}
			if (c=='"') {result.append("\\\""); continue;}
			if (c=='\\') {result.append("\\\\"); continue;}
			if (c=='\n') {result.append("<br>"); continue;}
			result.append(c);
		}
		return result.toString();
	}

	/**
	 * Lieft einen CSS-Farbcode für eine Farbe
	 * @param color	Umzuwandelnde Farbe
	 * @return	CSS-Farbcode
	 */
	private String getColor(final Color color) {
		return String.format("#%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue());
	}

	/**
	 * Verarbeitet die Daten zu einem Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelSurface, EditModel)
	 */
	private void process(final ModelElement element) {
		if (element instanceof ModelElementBox) {
			processBox((ModelElementBox)element);
			return;
		}

		if (element instanceof ModelElementText) {
			processText((ModelElementText)element);
			return;
		}

		if (element instanceof ModelElementLink) {
			processLink((ModelElementLink)element);
			return;
		}

		if (element instanceof ModelElementAnimationTextValue) {
			processAnimationTextValue((ModelElementAnimationTextValue)element);
			return;
		}

		if (element instanceof ModelElementLine) {
			processLine((ModelElementLine)element);
			return;
		}

		if (element instanceof ModelElementRectangle) {
			processRectangle((ModelElementRectangle)element);
			return;
		}

		if (element instanceof ModelElementEllipse) {
			processEllipse((ModelElementEllipse)element);
			return;
		}

		if (element instanceof ModelElementPosition) {
			processPosition((ModelElementPosition)element);
			return;
		}
	}

	/**
	 * Erstellt die Stil-Daten für eine Station zusammen
	 * @param element	Zu verarbeitendes Element
	 * @return	Stil-Daten
	 */
	private Map<String,String> getStyle(final ModelElementBox element) {
		final Map<String,String> style=new HashMap<>();

		style.put("whiteSpace","wrap");
		style.put("html","1");
		style.put("aspect","fixed");
		style.put("fillColor",getColor(element.getDrawBackgroundColor()));
		style.put("shadow","1");
		style.put("gradientColor","#ffffff");
		style.put("gradientDirection","east");

		Shapes.ShapeType drawShapeType=element.getShape().shapeType;
		if (element.isFlipShape() && drawShapeType.flippedName!=null) drawShapeType=drawShapeType.getFlipped();

		switch (drawShapeType) {
		case SHAPE_ARROW_LEFT:
			style.put("shape","offPageConnector");
			style.put("direction","south");
			style.put("size","0.1");
			style.put("gradientDirection","north");
			break;
		case SHAPE_ARROW_RIGHT:
			style.put("shape","offPageConnector");
			style.put("direction","north");
			style.put("size","0.1");
			style.put("gradientDirection","south");
			break;
		case SHAPE_ARROW_RIGHT_DOUBLE:
			style.put("shape","step");
			style.put("perimeter","stepPerimeter");
			style.put("size","0.1");
			style.put("gradientDirection","east");
			break;
		case SHAPE_ARROW_LEFT_DOUBLE:
			style.put("shape","step");
			style.put("perimeter","stepPerimeter");
			style.put("size","0.1");
			style.put("direction","west");
			style.put("gradientDirection","west");
			break;
		case SHAPE_BUTTON:
			break;
		case SHAPE_DOCUMENT:
			style.put("shape","document");
			break;
		case SHAPE_NONE:
			break;
		case SHAPE_OCTAGON:
			style.put("shape","hexagon");
			style.put("size","0.1");
			break;
		case SHAPE_OCTAGON_DOUBLE_LINE:
			style.put("shape","hexagon");
			style.put("size","0.1");
			break;
		case SHAPE_RECTANGLE:
			break;
		case SHAPE_RECTANGLE_LINES_INSIDE:
			break;
		case SHAPE_RECTANGLE_TWO_LINES_INSIDE:
			break;
		case SHAPE_RECTANGLE_123:
			break;
		case SHAPE_RECTANGLE_ABC:
			break;
		case SHAPE_RECTANGLE_DOUBLE_LINE:
			style.put("shape","process");
			break;
		case SHAPE_RECTANGLE_PLUSMINUS:
			break;
		case SHAPE_ROUNDED_RECTANGLE:
		case SHAPE_ROUNDED_RECTANGLE_SPEAKER:
		case SHAPE_ROUNDED_RECTANGLE_PAUSE:
			style.put("rounded","1");
			break;
		case SHAPE_ROUNDED_RECTANGLE_123:
			style.put("rounded","1");
			break;
		case SHAPE_ROUNDED_RECTANGLE_ABC:
			style.put("rounded","1");
			break;
		case SHAPE_ROUNDED_RECTANGLE_PLUSMINUS:
			style.put("rounded","1");
			break;
		case SHAPE_WEDGE_ARROW_LEFT:
			style.put("shape","trapezoid");
			style.put("perimeter","trapezoidPerimeter");
			style.put("direction","north");
			style.put("gradientDirection","south");
			break;
		case SHAPE_WEDGE_ARROW_RIGHT:
			style.put("shape","trapezoid");
			style.put("perimeter","trapezoidPerimeter");
			style.put("direction","south");
			style.put("gradientDirection","north");
			break;
		}

		return style;
	}

	/**
	 * Liefert die Darstellungsrichtung (bzw. Ausrichtung des Farbverlaufs) für ein Element
	 * @param element	Zu verarbeitendes Element
	 * @return	Darstellungsrichtung
	 */
	private String getDirection(final ModelElementBox element) {
		final Map<String,String> style=getStyle(element);
		return style.get("direction");
	}

	/**
	 * Wandelt eine Stil-Daten-Zuordnung in eine Zeichenkette um
	 * @param map	Stil-Daten-Zuordnung
	 * @return	Zeichenkette mit den Stil-Daten
	 * @see #getStyle(ModelElementBox)
	 */
	private String styleToString(final Map<String,String> map) {
		final StringBuilder style=new StringBuilder();
		for (Map.Entry<String,String> entry: map.entrySet()) {
			if (style.length()>0) style.append(';');
			style.append(entry.getKey());
			if (entry.getValue()!=null) {
				style.append('=');
				style.append(entry.getValue());
			}
		}
		return style.toString();
	}

	/**
	 * Verarbeitet ein Box-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processBox(final ModelElementBox element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");
		node.setAttribute("style",styleToString(getStyle(element)));
		node.setAttribute("value",encodeHTML(element.getTypeName()+"\n"+element.getName()));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein Text-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processText(final ModelElementText element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");
		int style=0;
		if (element.getTextBold()) style+=1;
		if (element.getTextItalic()) style+=2;
		node.setAttribute("style","text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;rounded=0;fontSize="+element.getTextSize()+";fontStyle="+style+";fontColor="+getColor(element.getColor())+";");
		node.setAttribute("value",encodeHTML(element.getText()));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein Link-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processLink(final ModelElementLink element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");
		node.setAttribute("style","text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;rounded=0;fontSize="+element.getTextSize()+";fontStyle=0;fontColor="+getColor(Color.BLUE)+";");
		node.setAttribute("value",encodeHTML(element.getText()));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein Animations-Text-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processAnimationTextValue(final ModelElementAnimationTextValue element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");
		node.setAttribute("style","text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;rounded=0;");
		node.setAttribute("value",encodeHTML(element.getName()));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein Linien-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processLine(final ModelElementLine element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");

		final Map<String,String> style=new HashMap<>();
		style.put("aspect","fixed");
		style.put("strokeColor",getColor(element.getColor()));
		style.put("strokeWidth",""+element.getLineWidth());
		node.setAttribute("style",styleToString(style));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein Rechteck-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processRectangle(final ModelElementRectangle element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");

		final Map<String,String> style=new HashMap<>();
		style.put("aspect","fixed");
		style.put("strokeColor",getColor(element.getColor()));
		style.put("strokeWidth",""+element.getLineWidth());
		if (element.getFillColor()==null) style.put("fillColor","none"); else style.put("fillColor",getColor(element.getFillColor()));
		if (element.getRounding()>0) style.put("rounded","1");
		node.setAttribute("style",styleToString(style));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein Ellipse-Element.
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processEllipse(final ModelElementEllipse element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");

		final Map<String,String> style=new HashMap<>();
		style.put("ellipse",null);
		style.put("strokeColor",getColor(element.getColor()));
		style.put("strokeWidth",""+element.getLineWidth());
		if (element.getFillColor()==null) style.put("fillColor","none"); else style.put("fillColor",getColor(element.getFillColor()));
		node.setAttribute("style",styleToString(style));

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);
	}

	/**
	 * Verarbeitet ein allgemeines Positions-Element
	 * @param element	Zu verarbeitendes Element
	 * @see #process(ModelElement)
	 */
	private void processPosition(final ModelElementPosition element) {
		final Element node=addElement(element);
		node.setAttribute("vertex","1");
		node.setAttribute("style","whiteSpace=wrap;html=1;aspect=fixed;");
		if (!(element instanceof ModelElementVertex)) {
			node.setAttribute("value",encodeHTML(element.getContextMenuElementName()));
		}

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("x",""+element.getPosition(true).x);
		geometry.setAttribute("y",""+element.getPosition(true).y);
		geometry.setAttribute("width",""+element.getSize().width);
		geometry.setAttribute("height",""+element.getSize().height);

	}

	/**
	 * Verarbeitet eine Verbindungskante
	 * @param edge	Auszugebende Verbindungskante
	 * @param lineMode	Art der Verbindungskante (gemäß Modell)
	 * @param arrowMode	Pfeilgröße (gemäß Model)
	 * @param surface	Zeichenfläche
	 */
	private void processEdge(final ModelElementEdge edge, final ModelElementEdge.LineMode lineMode, final ModelElementEdge.ArrowMode arrowMode, final ModelSurface surface) {
		final Map<String,String> style=new HashMap<>();

		style.put("rounded","0");
		style.put("orthogonalLoop","1");
		style.put("jettySize","auto");
		style.put("html","1");

		switch ((edge.getLineMode()==null)?lineMode:edge.getLineMode()) {
		case DIRECT:
			style.put("edgeStyle","orthogonalEdgeStyle");
			break;
		case MULTI_LINE:
			style.put("edgeStyle","entityRelationEdgeStyle");
			break;
		case MULTI_LINE_ROUNDED:
			style.put("edgeStyle","orthogonalEdgeStyle");
			style.put("curved","1");
			break;
		case CUBIC_CURVE:
			style.put("edgeStyle","orthogonalEdgeStyle");
			style.put("curved","1");
			break;
		default:
			style.put("edgeStyle","orthogonalEdgeStyle");
			break;
		}

		switch ((edge.getArrowMode()==null)?arrowMode:edge.getArrowMode()) {
		case OFF:
			style.put("endArrow","none");
			break;
		case SMALL:
			style.put("endArrow","classicThin");
			break;
		case MEDIUM:
			/* Vorgabewert von Draw.IO verwenden */
			break;
		case LARGE:
			style.put("endArrow","block");
			break;
		}

		final Point[] p=edge.getConnectionLine(1.0);
		if (p!=null) {
			final ModelElement e1=surface.getById(edge.getConnectionStart().getId());
			final ModelElement e2=surface.getById(edge.getConnectionEnd().getId());
			if ((e1 instanceof ModelElementBox) && (e2 instanceof ModelElementBox)) {
				final ModelElementBox box1=(ModelElementBox)e1;
				final ModelElementBox box2=(ModelElementBox)e2;

				final Point box1p=box1.getPosition(false);
				final Dimension box1s=box1.getSize();
				final Point box2p=box2.getPosition(false);
				final Dimension box2s=box2.getSize();
				final String box1dir=getDirection(box1);
				final String box2dir=getDirection(box2);

				double exitX=0.5;
				if (Math.abs(box1p.x-p[0].x)<5) exitX=0;
				if (Math.abs(box1p.x+box1s.width-p[0].x)<5) exitX=1;

				double exitY=0.5;
				if (Math.abs(box1p.y-p[0].y)<5) exitY=0;
				if (Math.abs(box1p.y+box1s.height-p[0].y)<5) exitY=1;

				double entryX=0.5;
				if (Math.abs(box2p.x-p[1].x)<5) entryX=0;
				if (Math.abs(box2p.x+box2s.width-p[1].x)<5) entryX=1;

				double entryY=0.5;
				if (Math.abs(box2p.y-p[1].y)<5) entryY=0;
				if (Math.abs(box2p.y+box2s.height-p[1].y)<5) entryY=1;

				if (box1dir!=null) {
					if (box1dir.equals("north")) {
						double x=exitX;
						double y=exitY;
						exitY=x;
						exitX=1-y;
					}
					if (box1dir.equals("south")) {
						double x=exitX;
						double y=exitY;
						exitY=1-x;
						exitX=y;
					}
				}

				if (box2dir!=null) {
					if (box2dir.equals("north")) {
						double x=entryX;
						double y=entryY;
						entryY=x;
						entryX=1-y;
					}
					if (box2dir.equals("south")) {
						double x=entryX;
						double y=entryY;
						entryY=1-x;
						entryX=y;
					}
				}

				style.put("exitX",NumberTools.formatSystemNumber(exitX));
				style.put("exitY",NumberTools.formatSystemNumber(exitY));
				style.put("entryX",NumberTools.formatSystemNumber(entryX));
				style.put("entryY",NumberTools.formatSystemNumber(entryY));
			}
		}

		final Element node=addElement(edge);
		node.setAttribute("edge","1");
		node.setAttribute("style",styleToString(style));
		node.setAttribute("source",idsMap.get(edge.getConnectionStart().getId()));
		node.setAttribute("target",idsMap.get(edge.getConnectionEnd().getId()));
		if (edge.getName()!=null && !edge.getName().isEmpty()) {
			node.setAttribute("value",encodeHTML(edge.getName()));
		}

		final Element geometry=doc.createElement("mxGeometry");
		node.appendChild(geometry);
		geometry.setAttribute("as","geometry");
		geometry.setAttribute("relative","1");
	}

	/**
	 * Überträgt den Inhalt der Zeichenfläche in ein draw.io-Diagramm
	 * @param surface	Zeichenfläche deren Inhalt übertragen werden soll
	 * @param model	Modell-Objekt aus dem weitere Eigenschaften ausgelesen werden sollen
	 */
	public void process(final ModelSurface surface, final EditModel model) {
		for (ModelElement element: surface.getElements()) if (!(element instanceof ModelElementEdge)) {
			process(element);
		}

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementEdge) {
			processEdge((ModelElementEdge)element,model.edgeLineMode,model.edgeArrowMode,surface);
		}
	}

	/**
	 * Speichert das Diagramm in der im Konstruktor angegebenen Datei.
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public boolean save() {
		return xml.save(doc.getDocumentElement(),true);
	}
}