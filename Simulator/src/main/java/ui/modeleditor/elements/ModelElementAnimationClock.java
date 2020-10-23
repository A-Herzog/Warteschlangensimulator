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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.net.URL;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationCustomDrawExpression;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt während der Animation einen Zahlenwert in Form einer Uhr an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationClock extends ModelElementAnimationCustomDrawExpression {
	/**
	 * Standard-Farbe
	 * @see #color
	 */
	private static final Color DEFAULT_COLOR=new Color(225,225,225);

	/**
	 * Aktuelle Farbe
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color=DEFAULT_COLOR;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationClock</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationClock(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50));
		setExpression("TNow()");
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_CLOCK.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationClock.Tooltip");
	}

	/**
	 * Liefert die aktuelle Farbe
	 * @return	Aktuelle Farbe
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Stellt die Farbe ein
	 * @param color	Neue Farbe
	 */
	public void setColor(final Color color) {
		if (color!=null) this.color=color;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationClock)) return false;

		if (!((ModelElementAnimationClock)element).color.equals(color)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationClock) {
			color=((ModelElementAnimationClock)element).color;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationClock clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationClock element=new ModelElementAnimationClock(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private Color lastColor;
	private Color lastColorLine;
	private int lastWidth;
	private BasicStroke lastWidthStroke;
	private final BasicStroke infoStroke=new BasicStroke(1);

	@Override
	protected void drawData(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		final long time=getAnimationLong()%86400;

		g.setColor(color);
		g.fill(new Ellipse2D.Double(rectangle.x,rectangle.y,rectangle.width,rectangle.height));


		if (lastColor==null || !lastColor.equals(color) || lastColorLine==null) {
			lastColor=color;
			lastColorLine=new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue());
		}
		g.setColor(lastColorLine);

		final int mx=rectangle.x+rectangle.width/2;
		final int my=rectangle.y+rectangle.height/2;
		final double rx=rectangle.width/2;
		final double ry=rectangle.height/2;

		int x;
		int y;

		g.setStroke(infoStroke);
		for (int i=0;i<12;i++) {
			x=mx+(int)FastMath.round(0.9*rx*FastMath.sin(i/12.0*2*FastMath.PI));
			y=my-(int)FastMath.round(0.9*ry*FastMath.cos(i/12.0*2*FastMath.PI));
			final int x2=mx+(int)FastMath.round(rx*FastMath.sin(i/12.0*2*FastMath.PI));
			final int y2=my-(int)FastMath.round(ry*FastMath.cos(i/12.0*2*FastMath.PI));
			g.drawLine(x,y,x2,y2);
		}

		if (lastWidth!=rectangle.width || lastWidthStroke==null) {
			lastWidth=rectangle.width;
			lastWidthStroke=new BasicStroke(FastMath.round(FastMath.ceil(rectangle.width/100.0)));
		}
		g.setStroke(lastWidthStroke);

		x=mx+(int)FastMath.round(2.0/3.0*rx*FastMath.sin((time%43200)/43200.0*2*FastMath.PI));
		y=my-(int)FastMath.round(2.0/3.0*ry*FastMath.cos((time%43200)/43200.0*2*FastMath.PI));
		g.drawLine(mx,my,x,y);

		x=mx+(int)FastMath.round(0.95*rx*FastMath.sin((time%3600)/3600.0*2*FastMath.PI));
		y=my-(int)FastMath.round(0.95*ry*FastMath.cos((time%3600)/3600.0*2*FastMath.PI));
		g.drawLine(mx,my,x,y);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationClock.Name");
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
			new ModelElementAnimationClockDialog(owner,ModelElementAnimationClock.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationClock.XML.Root");
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationClock.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));

		sub=doc.createElement(Language.trPrimary("Surface.AnimationClock.XML.Expression"));
		node.appendChild(sub);
		sub.setTextContent(getExpression());
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

		if (Language.trAll("Surface.AnimationClock.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationClock.XML.Expression",name)) {
			setExpression(content);
			return null;
		}

		return null;
	}

	@Override
	protected void updateDrawData(final SimulationData simData) {
		final Double D=getDrawExpression(simData);
		final long l=FastMath.round((D==null)?0.0:D.doubleValue());

		setAnimationLong(l%86400);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationClock";
	}

	private String getHTMLAnimationClock(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationClock(rect,lineColor,fillColor) {\n");

		sb.append("  var m={x: rect.x+Math.round(rect.w/2), y: rect.y+Math.round(rect.h/2)};\n");
		sb.append("  var r={x: Math.round(rect.w/2), y: Math.round(rect.h/2)};\n");

		sb.append("  context.fillStyle=fillColor;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.ellipse(m.x,m.y,r.x,r.y,0,0,2*Math.PI);\n");
		sb.append("  context.fill();\n");

		sb.append("  context.strokeStyle=lineColor;\n");
		sb.append("  for (var i=0;i<12;i++) {\n");
		sb.append("    var x1=m.x+Math.round(0.9*r.x*Math.sin(i/12*2*Math.PI));\n");
		sb.append("    var y1=m.y+Math.round(0.9*r.y*Math.cos(i/12*2*Math.PI));\n");
		sb.append("    var x2=m.x+Math.round(r.x*Math.sin(i/12*2*Math.PI));\n");
		sb.append("    var y2=m.y+Math.round(r.y*Math.cos(i/12*2*Math.PI));\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(x1,y1);\n");
		sb.append("    context.lineTo(x2,y2);\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(m.x,m.y);\n");
		sb.append("  context.lineTo(m.x,m.y-r.y);\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationClock",builder->getHTMLAnimationClock(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String fill="\""+HTMLOutputBuilder.colorToHTML(color)+"\"";
		final String line="\""+HTMLOutputBuilder.colorToHTML(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue()))+"\"";

		outputBuilder.outputBody.append("drawAnimationClock("+rect+","+line+","+fill+");\n");
	}
}