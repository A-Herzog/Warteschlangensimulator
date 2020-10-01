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
import java.awt.Stroke;
import java.net.URL;
import java.util.Objects;

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
import ui.modeleditor.coreelements.ModelElementAnimationCustomDrawExpression;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt während der Animation eine LCD-Anzeige aus einer
 * oder mehreren 7-Segment-Elementen über die ein
 * aktueller Simulationswert angezeigt werden kann.
 * @author Alexander Herzog
 */
public class ModelElementAnimationLCD extends ModelElementAnimationCustomDrawExpression {
	private static final Color DEFAULT_COLOR=Color.RED;

	private int digits;
	private Color color=DEFAULT_COLOR;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationLCD(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(25,50));
		digits=1;
		setExpression("wip()");
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_LCD.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationLCD.Tooltip");
	}

	/**
	 * Liefert die Anzahl an anzuzeigenden 7-Segment-Elementen.
	 * @return	Anzahl an anzuzeigenden 7-Segment-Elementen
	 */
	public int getDigits() {
		return Math.max(1,digits);
	}

	/**
	 * Stellt die Anzahl an anzuzeigenden 7-Segment-Elementen ein.
	 * @param digits	Anzahl an anzuzeigenden 7-Segment-Elementen
	 */
	public void setDigits(final int digits) {
		this.digits=Math.max(1,digits);
	}

	/**
	 * Liefert die Farbe für die aktiven LCD-Segmente.
	 * @return	Farbe für die aktiven LCD-Segmente
	 */
	public Color getColor() {
		return (color==null)?DEFAULT_COLOR:color;
	}

	/**
	 * Stellt die Farbe für die aktiven LCD-Segmente ein.
	 * @param color	Farbe für die aktiven LCD-Segmente
	 */
	public void setColor(final Color color) {
		this.color=(color==null)?DEFAULT_COLOR:color;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationLCD)) return false;
		final ModelElementAnimationLCD other=(ModelElementAnimationLCD)element;

		if (digits!=other.digits) return false;
		if (!Objects.equals(color,other.color)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationLCD) {
			final ModelElementAnimationLCD source=(ModelElementAnimationLCD)element;
			digits=source.digits;
			color=source.color;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationLCD clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationLCD element=new ModelElementAnimationLCD(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private Stroke stroke=null;
	private int strokeWidth=0;

	private void drawMinus(final Graphics2D g, final int rectX, final int rectW, final int rectY, final int rectH) {
		final int min=Math.max(1,Math.min(rectW,rectH)/10);
		if (stroke==null || strokeWidth!=min) {
			stroke=new BasicStroke(min,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
			strokeWidth=min;
		}

		/* Reihe oben */
		final int P1x=rectX+min;
		final int P1y=rectY+min;
		final int P2x=rectX+rectW-min;
		final int P2y=rectY+min;

		/* Reihe Mitte */
		final int P3x=rectX+min;
		final int P3y=rectY+rectH/2;
		final int P4x=rectX+rectW-min;
		final int P4y=rectY+rectH/2;

		/* Reihe unten */
		final int P5x=rectX+min;
		final int P5y=rectY+rectH-min;
		final int P6x=rectX+rectW-min;
		final int P6y=rectY+rectH-min;

		g.setStroke(stroke);

		g.setColor(Color.DARK_GRAY);
		g.drawLine(P1x,P1y,P2x,P2y); /* horizontal oben */
		g.drawLine(P1x,P1y,P3x,P3y); /* vertikal oben links */
		g.drawLine(P2x,P2y,P4x,P4y); /* vertikal oben rechts */
		/* horizontal Mitte -> aktiv */
		g.drawLine(P3x,P3y,P5x,P5y); /* vertikal unten links */
		g.drawLine(P4x,P4y,P6x,P6y); /* vertikal unten rechts */
		g.drawLine(P5x,P5y,P6x,P6y); /* horizontal unten */

		g.setColor(color);
		g.drawLine(P3x,P3y,P4x,P4y); /* horizontal Mitte */
	}

	private void drawDigit(final Graphics2D g, final int rectX, final int rectW, final int rectY, final int rectH, final boolean hasValue, final int value) {
		final int min=Math.max(1,Math.min(rectW,rectH)/10);
		if (stroke==null || strokeWidth!=min) {
			stroke=new BasicStroke(min,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
			strokeWidth=min;
		}

		boolean seg1=false;
		boolean seg2=false;
		boolean seg3=false;
		boolean seg4=false;
		boolean seg5=false;
		boolean seg6=false;
		boolean seg7=false;
		if (hasValue) switch (value) {
		case 0: seg1=true; seg2=true; seg3=true; seg5=true; seg6=true; seg7=true; break;
		case 1: seg3=true; seg6=true; break;
		case 2: seg1=true; seg3=true; seg4=true; seg5=true; seg7=true; break;
		case 3: seg1=true; seg3=true; seg4=true; seg6=true; seg7=true; break;
		case 4: seg2=true; seg3=true; seg4=true; seg6=true; break;
		case 5: seg1=true; seg2=true; seg4=true; seg6=true; seg7=true; break;
		case 6: seg1=true; seg2=true; seg4=true; seg5=true; seg6=true; seg7=true; break;
		case 7: seg1=true; seg3=true; seg6=true; break;
		case 8: seg1=true; seg2=true; seg3=true; seg4=true; seg5=true; seg6=true; seg7=true; break;
		case 9: seg1=true; seg2=true; seg3=true; seg4=true; seg6=true; seg7=true; break;
		}

		/* Reihe oben */
		final int P1x=rectX+min;
		final int P1y=rectY+min;
		final int P2x=rectX+rectW-min;
		final int P2y=rectY+min;

		/* Reihe Mitte */
		final int P3x=rectX+min;
		final int P3y=rectY+rectH/2;
		final int P4x=rectX+rectW-min;
		final int P4y=rectY+rectH/2;

		/* Reihe unten */
		final int P5x=rectX+min;
		final int P5y=rectY+rectH-min;
		final int P6x=rectX+rectW-min;
		final int P6y=rectY+rectH-min;

		g.setStroke(stroke);

		g.setColor(Color.DARK_GRAY);
		if (!seg1) g.drawLine(P1x,P1y,P2x,P2y); /* horizontal oben */
		if (!seg2) g.drawLine(P1x,P1y,P3x,P3y); /* vertikal oben links */
		if (!seg3) g.drawLine(P2x,P2y,P4x,P4y); /* vertikal oben rechts */
		if (!seg4) g.drawLine(P3x,P3y,P4x,P4y); /* horizontal Mitte */
		if (!seg5) g.drawLine(P3x,P3y,P5x,P5y); /* vertikal unten links */
		if (!seg6) g.drawLine(P4x,P4y,P6x,P6y); /* vertikal unten rechts */
		if (!seg7) g.drawLine(P5x,P5y,P6x,P6y); /* horizontal unten */

		g.setColor(color);
		if (seg1) g.drawLine(P1x,P1y,P2x,P2y); /* horizontal oben */
		if (seg2) g.drawLine(P1x,P1y,P3x,P3y); /* vertikal oben links */
		if (seg3) g.drawLine(P2x,P2y,P4x,P4y); /* vertikal oben rechts */
		if (seg4) g.drawLine(P3x,P3y,P4x,P4y); /* horizontal Mitte */
		if (seg5) g.drawLine(P3x,P3y,P5x,P5y); /* vertikal unten links */
		if (seg6) g.drawLine(P4x,P4y,P6x,P6y); /* vertikal unten rechts */
		if (seg7) g.drawLine(P5x,P5y,P6x,P6y); /* horizontal unten */
	}

	@Override
	protected void drawData(final Graphics2D graphics, final Rectangle rectangle, final double zoom) {
		graphics.setColor(Color.BLACK);
		graphics.fill(rectangle);

		final Double value=getAnimationDouble();
		int intValue=0;
		if (value!=null) {
			intValue=(int)Math.round(value.doubleValue());
		}

		final int maxValue=(int)Math.round(FastMath.pow(10,digits))-1;
		final int minValue;
		if (digits==1) {
			minValue=0;
		} else {
			minValue=-((int)Math.round(FastMath.pow(10,digits-1))-1);
		}

		if (intValue>maxValue) intValue=maxValue;
		final boolean minus;
		if (intValue<0) {
			minus=true;
			if (intValue<minValue) intValue=minValue;
			intValue=-intValue;
		} else {
			minus=false;
		}

		boolean firstValue=true;
		for (int i=0;i<digits;i++) {
			boolean hasValue=false;
			int digitValue=0;
			if (value!=null) {
				digitValue=intValue;
				for (int j=1;j<digits-i;j++) digitValue/=10;
				digitValue%=10;
				if (digitValue!=0 || i==digits-1) hasValue=true;
			}
			if (hasValue && firstValue) {
				if (minus) drawMinus(graphics,rectangle.x+(i-1)*rectangle.width/digits,rectangle.width/digits,rectangle.y,rectangle.height);
				firstValue=false;
			}

			drawDigit(graphics,rectangle.x+i*rectangle.width/digits,rectangle.width/digits,rectangle.y,rectangle.height,hasValue,digitValue);
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationLCD.Name");
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
			new ModelElementAnimationLCDDialog(owner,ModelElementAnimationLCD.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationLCD.XML.Root");
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationLCD.XML.Expression"));
		node.appendChild(sub);
		sub.setTextContent(getExpression());

		sub=doc.createElement(Language.trPrimary("Surface.AnimationLCD.XML.Digits"));
		node.appendChild(sub);
		sub.setTextContent(""+digits);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationLCD.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));
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

		if (Language.trAll("Surface.AnimationLCD.XML.Expression",name)) {
			setExpression(content);
			return null;
		}

		if (Language.trAll("Surface.AnimationLCD.XML.Digits",name)) {
			final Long L=NumberTools.getPositiveLong(content);
			if (L==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			digits=L.intValue();
			return null;
		}

		if (Language.trAll("Surface.AnimationLCD.XML.Color",name)) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationLCD";
	}

	private String getHTMLAnimationLCD(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationLCD(rect,digits) {\n");

		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("  context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  var min=Math.max(1,Math.min(rect.w,rect.h)/10);\n");
		sb.append("  for (var i=0;i<digits;i++) {\n");
		/* Reihe oben */
		sb.append("    var P1x=rect.x+min;\n");
		sb.append("    var P1y=rect.y+min;\n");
		sb.append("    var P2x=rect.x+rect.w-min;\n");
		sb.append("    var P2y=rect.y+min;\n");
		/* Reihe Mitte */
		sb.append("    var P3x=rect.x+min;\n");
		sb.append("    var P3y=rect.y+rect.h/2;\n");
		sb.append("    var P4x=rect.x+rect.w-min;\n");
		sb.append("    var P4y=rect.y+rect.h/2;\n");
		/* Reihe unten */
		sb.append("    var P5x=rect.x+min;\n");
		sb.append("    var P5y=rect.y+rect.h-min;\n");
		sb.append("    var P6x=rect.x+rect.w-min;\n");
		sb.append("    var P6y=rect.y+rect.h-min;\n");
		sb.append("    context.strokeStyle=\"gray\";\n");
		sb.append("    context.lineWidth=3;\n");
		/* horizontal oben */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P1x,P1y);\n");
		sb.append("    context.lineTo(P2x,P2y);\n");
		sb.append("    context.stroke();\n");
		/* vertikal oben links */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P1x,P1y);\n");
		sb.append("    context.lineTo(P3x,P3y);\n");
		sb.append("    context.stroke();\n");
		/* vertikal oben rechts */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P2x,P2y);\n");
		sb.append("    context.lineTo(P4x,P4y);\n");
		sb.append("    context.stroke();\n");
		/* horizontal Mitte */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P3x,P3y);\n");
		sb.append("    context.lineTo(P4x,P4y);\n");
		sb.append("    context.stroke();\n");
		/* vertikal unten links */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P3x,P3y);\n");
		sb.append("    context.lineTo(P5x,P5y);\n");
		sb.append("    context.stroke();\n");
		/* vertikal unten rechts */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P4x,P4y);\n");
		sb.append("    context.lineTo(P6x,P6y);\n");
		sb.append("    context.stroke();\n");
		/* horizontal unten */
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(P5x,P5y);\n");
		sb.append("    context.lineTo(P6x,P6y);\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		outputBuilder.addJSUserFunction("drawAnimationLCD",builder->getHTMLAnimationLCD(builder));
		outputBuilder.outputBody.append("drawAnimationLCD({x: "+p.x+",y: "+p.y+",w: "+s.width+",h: "+s.height+"},"+digits+");\n");
	}
}