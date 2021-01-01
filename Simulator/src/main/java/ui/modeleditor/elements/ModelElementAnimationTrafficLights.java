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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationCustomDraw;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt während der Animation eine Ampel an, bei der über
 * verschiedene Bedingungen gesteuert werden kann, welches
 * Licht leuchtend dargestellt werden soll.
 * @author Alexander Herzog
 */
public class ModelElementAnimationTrafficLights extends ModelElementAnimationCustomDraw {

	/**
	 * Drei Lichter in der der Ampel?
	 * @see #getLightsCount()
	 * @see #setLightsCount(int)
	 */
	private boolean threeLights=false;

	/**
	 * Bedingung für das erste Licht
	 * @see #getExpressionOne()
	 * @see #setExpressionOne(String)
	 */
	private String expressionOne="";

	/**
	 * Bedingung für das zweite Licht
	 * @see #getExpressionTwo()
	 * @see #setExpressionTwo(String)
	 */
	private String expressionTwo="";

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationTrafficLights</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationTrafficLights(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,100));
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationTrafficLights.Tooltip");
	}

	/**
	 * Gibt an, ob die Ampel zwei oder drei Lichter besitzen soll.
	 * @return	Anzahl der Lichter der Ampel (kann 2 oder 3 sein)
	 */
	public int getLightsCount() {
		return threeLights?3:2;
	}

	/**
	 * Stellt ein, ob die Ampel zwei oder drei Lichter besitzen soll.	 *
	 * @param lightsCount	Anzahl der Lichter der Ampel (kann 2 oder 3 sein)
	 */
	public void setLightsCount(final int lightsCount) {
		if (lightsCount==2 || lightsCount==3) threeLights=(lightsCount==3);
		fireChanged();
	}

	/**
	 * Liefert die erste Bedingung (Bedingung, dass das rote Licht leuchtet).
	 * @return	Bedingung für das erste Licht
	 */
	public String getExpressionOne() {
		return expressionOne;
	}

	/**
	 * Stellt die erste Bedingung ein (Bedingung, dass das rote Licht leuchtet).
	 * @param expressionOne	Bedingung für das erste Licht
	 */
	public void setExpressionOne(final String expressionOne) {
		if (expressionOne!=null) this.expressionOne=expressionOne;
		fireChanged();
	}

	/**
	 * Liefert die zweite Bedingung (Bedingung, dass das gelbe Licht - wenn vorhanden - leuchtet).
	 * @return	Bedingung für das zweite Licht
	 */
	public String getExpressionTwo() {
		return expressionTwo;
	}

	/**
	 * Stellt die zweite Bedingung ein (Bedingung, dass das gelbe Licht - wenn vorhanden - leuchtet).
	 * @param expressionTwo Bedingung für das zweite Licht
	 */
	public void setExpressionTwo(final String expressionTwo) {
		if (expressionTwo!=null) this.expressionTwo=expressionTwo;
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
		if (!(element instanceof ModelElementAnimationTrafficLights)) return false;

		if (threeLights!=((ModelElementAnimationTrafficLights)element).threeLights) return false;
		if (!expressionOne.equals(((ModelElementAnimationTrafficLights)element).expressionOne)) return false;
		if (!expressionTwo.equals(((ModelElementAnimationTrafficLights)element).expressionTwo)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationTrafficLights) {
			threeLights=((ModelElementAnimationTrafficLights)element).threeLights;
			expressionOne=((ModelElementAnimationTrafficLights)element).expressionOne;
			expressionTwo=((ModelElementAnimationTrafficLights)element).expressionTwo;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationTrafficLights clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationTrafficLights element=new ModelElementAnimationTrafficLights(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	@Override
	protected void drawData(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		Integer value=getAnimationInteger();

		g.setColor(Color.BLACK);
		g.fill(rectangle);

		final int maxWidth=(int)FastMath.round(0.9*rectangle.width);
		final int maxHeight=(int)FastMath.round(0.9*rectangle.height);

		double y;

		if (threeLights) {
			final int diameter=FastMath.min(maxWidth,maxHeight/3);
			final double x=rectangle.x+(rectangle.width-diameter)/2.0;

			y=rectangle.y+1.0*rectangle.height/6.0-diameter/2.0;
			if (value==null || value!=0) g.setColor(Color.GRAY); else g.setColor(Color.RED);
			g.fill(new Ellipse2D.Double(x,y,diameter,diameter));

			y=rectangle.y+3.0*rectangle.height/6.0-diameter/2.0;
			if (value==null || value!=1) g.setColor(Color.GRAY); else g.setColor(Color.YELLOW);
			g.fill(new Ellipse2D.Double(x,y,diameter,diameter));

			y=rectangle.y+5.0*rectangle.height/6.0-diameter/2.0;
			if (value==null || value!=2) g.setColor(Color.GRAY); else g.setColor(Color.GREEN);
			g.fill(new Ellipse2D.Double(x,y,diameter,diameter));
		} else {
			final int diameter=FastMath.min(maxWidth,maxHeight/2);
			final double x=rectangle.x+(rectangle.width-diameter)/2.0;

			y=rectangle.y+1.0*rectangle.height/4.0-diameter/2.0;
			if (value==null || value!=0) g.setColor(Color.GRAY); else g.setColor(Color.RED);
			g.fill(new Ellipse2D.Double(x,y,diameter,diameter));

			y=rectangle.y+3.0*rectangle.height/4.0-diameter/2.0;
			if (value==null || value!=1) g.setColor(Color.GRAY); else g.setColor(Color.GREEN);
			g.fill(new Ellipse2D.Double(x,y,diameter,diameter));
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationTrafficLights.Name");
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
			new ModelElementAnimationTrafficLightsDialog(owner,ModelElementAnimationTrafficLights.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationTrafficLights.XML.Root");
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition"));
		node.appendChild(sub);
		sub.setAttribute(Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition.Light"),Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition.Light.Red"));
		sub.setTextContent(expressionOne);

		if (threeLights) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition"));
			node.appendChild(sub);
			sub.setAttribute(Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition.Light"),Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition.Light.Yellow"));
			sub.setTextContent(expressionTwo);
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

		if (Language.trAll("Surface.AnimationTrafficLights.XML.Condition",name)) {
			String light=Language.trAllAttribute("Surface.AnimationTrafficLights.XML.Condition.Light",node);
			if (Language.trAll("Surface.AnimationTrafficLights.XML.Condition.Light.Yellow",light)) {
				threeLights=true;
				expressionTwo=content;
				return null;
			} else {
				if (!Language.trAll("Surface.AnimationTrafficLights.XML.Condition.Light.Red",light)) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationTrafficLights.XML.Condition.Light"),name,node.getParentNode().getNodeName());
				expressionOne=content;
				return null;
			}
		}

		return null;
	}

	/**
	 * Rechenobjekt für {@link #expressionOne} zur Berechnung
	 * der Formel während der Animation
	 * @see #expressionOne
	 * @see #evalExpression(SimulationData, int)
	 */
	private ExpressionMultiEval animationExpressionOne;

	/**
	 * Rechenobjekt für {@link #expressionTwo} zur Berechnung
	 * der Formel während der Animation
	 * @see #expressionTwo
	 * @see #evalExpression(SimulationData, int)
	 */
	private ExpressionMultiEval animationExpressionTwo;

	/**
	 * Wertet {@link #animationExpressionOne} oder
	 * {@link #animationExpressionTwo} aus und liefert
	 * den zu zeichnenden Wert zurück.
	 * @param simData	Simulationsdatenobjekt
	 * @param nr	Index der auszuwertenden Bedingung
	 * @return	Ist die Bedingung erfüllt?
	 * @see #animationExpressionOne
	 * @see #animationExpressionTwo
	 */
	private boolean evalExpression(final SimulationData simData, final int nr) {
		final ExpressionMultiEval eval=(nr==1)?animationExpressionOne:animationExpressionTwo;
		if (eval==null) return false;
		simData.runData.setClientVariableValues(null);
		return eval.eval(simData.runData.variableValues,simData,null);
	}

	@Override
	protected void updateDrawData(final SimulationData simData) {
		int value;
		if (evalExpression(simData,1)) value=0; else {
			if (threeLights) {
				if (evalExpression(simData,2)) value=1; else value=2;
			} else {
				value=1;
			}
		}

		setAnimationInteger(value);
	}

	@Override
	public void initAnimation(SimulationData simData) {
		animationExpressionOne=new ExpressionMultiEval(simData.runModel.variableNames);
		if (animationExpressionOne.parse(expressionOne)>=0) animationExpressionOne=null;

		if (threeLights) {
			animationExpressionTwo=new ExpressionMultiEval(simData.runModel.variableNames);
			if (animationExpressionTwo.parse(expressionTwo)>=0) animationExpressionTwo=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationTrafficLights";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationTrafficLightsThree(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationTrafficLightsThree(rect) {\n");

		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("  context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  var r=Math.round(0.9*Math.min(rect.w,rect.h/3)/2);\n");
		sb.append("  var mx=Math.round(rect.x+rect.w/2);\n");
		sb.append("  context.fillStyle=\"gray\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.ellipse(mx,rect.y+Math.round(1*rect.h/6),r,r,0,0,2*Math.PI);\n");
		sb.append("  context.fill();\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.ellipse(mx,rect.y+Math.round(3*rect.h/6),r,r,0,0,2*Math.PI);\n");
		sb.append("  context.fill();\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.ellipse(mx,rect.y+Math.round(5*rect.h/6),r,r,0,0,2*Math.PI);\n");
		sb.append("  context.fill();\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationTrafficLightsTwo(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationTrafficLightsTwo(rect) {\n");

		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("  context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  var r=Math.round(0.9*Math.min(rect.w,rect.h/2)/2);\n");
		sb.append("  var mx=Math.round(rect.x+rect.w/2);\n");
		sb.append("  context.fillStyle=\"gray\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.ellipse(mx,rect.y+Math.round(1*rect.h/4),r,r,0,0,2*Math.PI);\n");
		sb.append("  context.fill();\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.ellipse(mx,rect.y+Math.round(3*rect.h/4),r,r,0,0,2*Math.PI);\n");
		sb.append("  context.fill();\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (threeLights) {
			outputBuilder.addJSUserFunction("drawAnimationTrafficLightsThree",builder->getHTMLAnimationTrafficLightsThree(builder));
			outputBuilder.outputBody.append("drawAnimationTrafficLightsThree({x: "+p.x+",y: "+p.y+",w: "+s.width+",h: "+s.height+"});\n");
		} else {
			outputBuilder.addJSUserFunction("drawAnimationTrafficLightsTwo",builder->getHTMLAnimationTrafficLightsTwo(builder));
			outputBuilder.outputBody.append("drawAnimationTrafficLightsTwo({x: "+p.x+",y: "+p.y+",w: "+s.width+",h: "+s.height+"});\n");
		}
	}
}
