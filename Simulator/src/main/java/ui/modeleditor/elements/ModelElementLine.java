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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
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
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Linie (zur optischen Gestaltung)
 * @author Alexander Herzog
 */
public class ModelElementLine extends ModelElementDecoration {
	/**
	 * Linientyp
	 * @see #getLineType()
	 * @see #setLineType(int)
	 * @see ComplexLine
	 */
	private int lineType;

	/**
	 * Pfeilgröße am Linienstart
	 * @see #getArrowStart()
	 * @see #setArrowStart(int)
	 */
	private int arrowStart;

	/**
	 * Pfeilgröße am Linienende
	 * @see #getArrowEnd()
	 * @see #setArrowEnd(int)
	 */
	private int arrowEnd;

	/**
	 * Konstruktor der Klasse <code>ModelElementLine</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementLine(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50));
		lineType=0;
		arrowStart=0;
		arrowEnd=0;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_LINE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Line.Tooltip");
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Line.Name");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;

		if (!(element instanceof ModelElementLine)) return false;
		final ModelElementLine otherLine=(ModelElementLine)element;

		if (lineType!=otherLine.lineType) return false;
		if (arrowStart!=otherLine.arrowStart) return false;
		if (arrowEnd!=otherLine.arrowEnd) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementLine) {
			final ModelElementLine copySource=(ModelElementLine)element;

			lineType=copySource.lineType;
			arrowStart=copySource.arrowStart;
			arrowEnd=copySource.arrowEnd;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementLine clone(final EditModel model, final ModelSurface surface) {
		final ModelElementLine element=new ModelElementLine(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		return isNearLine(getPosition(true),getLowerRightPosition(),point,zoom,lineWidth);
	}

	/**
	 * Prüft, ob sich ein Gebiet und das Elements überschneiden
	 * @param selectedArea	Gebiet, bei dem geprüft werden soll, ob es sich mit dem Element überschneidet
	 * @return	Gibt <code>true</code> zurück, wenn sich das Gebiet und das Element überschneiden
	 */
	@Override
	protected boolean containedInArea(final Rectangle selectedArea) {
		final Point position=getPosition(true);
		final Dimension size=getSize();
		for (int i=0;i<=20;i++) {
			if (selectedArea.contains(new Point((int)Math.round(position.x+((double)i)/20*size.width),(int)Math.round(position.y+((double)i)/20*size.height)))) return true;
		}
		return false;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index außerhalb des gültigen Bereichs liegt
	 */
	@Override
	public Point getBorderPointPosition(final int index) {
		switch (index) {
		case 0: return getPosition(false);
		case 1: return getLowerRightPosition();
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
			setSize(new Dimension(point.x-p1.x,point.y-p1.y));
			break;
		}
	}

	/**
	 * Größe des Pfeils (bezogen auf Zoomfaktor=100%)
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private static final int ARROW_SIZE_UNIT=6;

	/**
	 * Cache für das Startpunkt der Linie Objekt
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private final Point line1=new Point();

	/**
	 * Cache für das Endpunkt der Linie Objekt
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private final Point line2=new Point();

	/**
	 * Cache für die erste Pfeilspitze
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private final Point arrow1=new Point();

	/**
	 * Cache für die zweite Pfeilspitze
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private final Point arrow2=new Point();

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

		final Point p1=getPosition(true);
		final Point p2=getLowerRightPosition();

		Color useColor=color;
		int useWidth=lineWidth;
		if (isSelected() && showSelectionFrames) {
			useColor=Color.GREEN;
			useWidth=Math.max(lineWidth,3);
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				useColor=Color.BLUE;
				useWidth=Math.max(lineWidth,3);
			}
		}

		final ComplexLine line=new ComplexLine(useWidth,useColor,lineType);

		/* Basislinie */
		line1.x=(int)Math.round(p1.x*zoom);
		line1.y=(int)Math.round(p1.y*zoom);
		line2.x=(int)Math.round(p2.x*zoom);
		line2.y=(int)Math.round(p2.y*zoom);
		line.draw(graphics,line1,line2,zoom);

		double vx=(p2.x-p1.x)*zoom;
		double vy=(p2.y-p1.y)*zoom;
		final double length=Math.sqrt(vx*vx+vy*vy);
		vx=vx/length;
		vy=vy/length;
		double wx=vy;
		double wy=-vx;

		/* Pfeil am Linienstart */
		if (arrowStart>0) {
			final double len=arrowStart*ARROW_SIZE_UNIT*zoom;
			arrow1.x=(int)FastMath.round(p1.x*zoom+len*vx+len*wx);
			arrow1.y=(int)FastMath.round(p1.y*zoom+len*vy+len*wy);
			arrow2.x=(int)FastMath.round(p1.x*zoom+len*vx-len*wx);
			arrow2.y=(int)FastMath.round(p1.y*zoom+len*vy-len*wy);
			line.draw(graphics,line1,arrow1,zoom);
			line.draw(graphics,line1,arrow2,zoom);
		}

		/* Pfeil am Linienende */
		if (arrowEnd>0) {
			final double len=arrowEnd*ARROW_SIZE_UNIT*zoom;
			arrow1.x=(int)FastMath.round(p2.x*zoom-len*vx+len*wx);
			arrow1.y=(int)FastMath.round(p2.y*zoom-len*vy+len*wy);
			arrow2.x=(int)FastMath.round(p2.x*zoom-len*vx-len*wx);
			arrow2.y=(int)FastMath.round(p2.y*zoom-len*vy-len*wy);
			line.draw(graphics,line2,arrow1,zoom);
			line.draw(graphics,line2,arrow2,zoom);
		}

		if (isSelected() && showSelectionFrames) {
			final Graphics2D g2=(Graphics2D)graphics;
			drawBorderBox(g2,p1,zoom);
			drawBorderBox(g2,p2,zoom);
		}

		setClip(graphics,drawRect,null);
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
			new ModelElementLineDialog(owner,ModelElementLine.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Line.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Line.XML.Type")));
		sub.setTextContent(""+lineType);

		if (arrowStart>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Line.XML.ArrowStart")));
			sub.setTextContent(""+arrowStart);
		}

		if (arrowEnd>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Line.XML.ArrowEnd")));
			sub.setTextContent(""+arrowEnd);
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

		if (Language.trAll("Surface.Line.XML.Type",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null || I>ComplexLine.MAX_TYPE) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			lineType=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.Line.XML.ArrowStart",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null || I>ComplexLine.MAX_TYPE) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			arrowStart=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.Line.XML.ArrowEnd",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null || I>ComplexLine.MAX_TYPE) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			arrowEnd=I.intValue();
			return null;
		}

		return null;
	}

	/**
	 * Liefert den aktuellen Linientyp
	 * @return	Linientyp
	 * @see ComplexLine
	 */
	public int getLineType() {
		return lineType;
	}

	/**
	 * Stellt den Linientyp ein
	 * @param lineType	Linientyp
	 * @see ComplexLine
	 */
	public void setLineType(final int lineType) {
		if (lineType>=0 && lineType<=ComplexLine.MAX_TYPE) {
			this.lineType=lineType;
			fireChanged();
		}
	}

	/**
	 * Gibt die Pfeilgröße am Linienstart an.
	 * @return	Pfeilgröße am Linienstart an
	 */
	public int getArrowStart() {
		return arrowStart;
	}

	/**
	 * Stellt die Pfeilgröße am Linienstart ein.
	 * @param arrowStart	Neue Pfeilgröße am Linienstart
	 */
	public void setArrowStart(final int arrowStart) {
		if (arrowStart>=0) {
			this.arrowStart=arrowStart;
			fireChanged();
		}
	}

	/**
	 * Gibt die Pfeilgröße am Linienende an.
	 * @return	Pfeilgröße am Linienende an
	 */
	public int getArrowEnd() {
		return arrowEnd;
	}

	/**
	 * Stellt die Pfeilgröße am Linienende ein.
	 * @param arrowEnd	Neue Pfeilgröße am Linienende
	 */
	public void setArrowEnd(final int arrowEnd) {
		if (arrowEnd>=0) {
			this.arrowEnd=arrowEnd;
			fireChanged();
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementLine";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLDrawLine(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawLine(p1,p2,color,width) {\n");

		sb.append("  if (width>0) {\n");
		sb.append("    context.strokeStyle=color;\n");
		sb.append("    context.lineWidth=width;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(p1.x,p1.y);\n");
		sb.append("    context.lineTo(p2.x,p2.y);\n");
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
		outputBuilder.addJSUserFunction("drawLine",builder->getHTMLDrawLine(builder));

		final Point p1=getPosition(true);
		final Point p2=getLowerRightPosition();

		outputBuilder.outputBody.append("drawLine({x: "+p1.x+",y: "+p1.y+"},{x: "+p2.x+",y: "+p2.y+"},\""+HTMLOutputBuilder.colorToHTML(color)+"\","+lineWidth+");\n");
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