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
import ui.modeleditor.elements.ModelElementRectangle;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.elements.ModelElementVertex;
import xml.XMLTools;

/**
 * Ermöglicht den Export der Zeichnung als Draw.io-Diagramm<br>
 * <a href="https://drawio-app.com/">https://drawio-app.com/</a>
 * @author Alexander Herzog
 */
public class DrawIOExport {
	private final XMLTools xml;
	private final Document doc;
	private final Element graphModel;
	private final Element root;
	private final Map<Integer,String> idsMap;
	private final String baseId;
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

	private static final String idChars="01234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private String generateID(final int len) {
		final StringBuilder id=new StringBuilder();
		for (int i=0;i<len;i++) id.append(idChars.charAt((int)Math.floor(Math.random()*idChars.length())));
		return id.toString();
	}

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

	private String getColor(final Color color) {
		return String.format("#%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue());
	}

	private void process(final ModelElement element) {
		if (element instanceof ModelElementBox) {
			processBox((ModelElementBox)element);
			return;
		}

		if (element instanceof ModelElementText) {
			processText((ModelElementText)element);
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

	private Map<String,String> getStyle(final ModelElementBox element) {
		final Map<String,String> style=new HashMap<>();

		style.put("whiteSpace","wrap");
		style.put("html","1");
		style.put("aspect","fixed");
		style.put("fillColor",getColor(element.getDrawBackgroundColor()));
		style.put("shadow","1");
		style.put("gradientColor","#ffffff");
		style.put("gradientDirection","north");

		switch (element.getShape().shapeType) {
		case SHAPE_ARROW_LEFT:
			style.put("shape","offPageConnector");
			style.put("direction","south");
			style.put("size","0.1");
			style.put("gradientDirection","west");
			break;
		case SHAPE_ARROW_RIGHT:
			style.put("shape","offPageConnector");
			style.put("direction","north");
			style.put("size","0.1");
			style.put("gradientDirection","east");
			break;
		case SHAPE_ARROW_RIGHT_DOUBLE:
			style.put("shape","step");
			style.put("perimeter","stepPerimeter");
			style.put("size","0.1");
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
			style.put("gradientDirection","east");
			break;
		case SHAPE_WEDGE_ARROW_RIGHT:
			style.put("shape","trapezoid");
			style.put("perimeter","trapezoidPerimeter");
			style.put("direction","south");
			style.put("gradientDirection","west");
			break;
		}

		return style;
	}

	private String getDirection(final ModelElementBox element) {
		final Map<String,String> style=getStyle(element);
		return style.get("direction");
	}

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

	private void processEdge(final ModelElementEdge edge, final ModelElementEdge.LineMode lineMode, final ModelSurface surface) {
		final Map<String,String> style=new HashMap<>();

		style.put("rounded","0");
		style.put("orthogonalLoop","1");
		style.put("jettySize","auto");
		style.put("html","1");

		switch (lineMode) {
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
		default:
			style.put("edgeStyle","orthogonalEdgeStyle");
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
			processEdge((ModelElementEdge)element,model.edgeLineMode,surface);
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