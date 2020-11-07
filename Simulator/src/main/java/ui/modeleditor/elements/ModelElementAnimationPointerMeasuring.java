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
import ui.modeleditor.elements.FontCache.FontFamily;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt eine Analog-Skala während der Animation an
 * über die ein aktueller Simulationswert angezeigt werden kann.
 * @author Alexander Herzog
 */
public class ModelElementAnimationPointerMeasuring extends ModelElementAnimationCustomDrawExpression {
	/**
	 * Standard-Farbe für den Zeiger
	 * @see #color
	 */
	private static final Color DEFAULT_COLOR=Color.BLUE;

	/**
	 * Minimalwert der Skala
	 * @see #getMinValue()
	 * @see #setMinValue(int)
	 */
	private int minValue;

	/**
	 * Maximalwert der Skala
	 * @see #getMaxValue()
	 * @see #setMaxValue(int)
	 */
	private int maxValue;

	/**
	 * Farbe für den Zeiger
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color=DEFAULT_COLOR;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationPointerMeasuring(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(100,50));
		minValue=0;
		maxValue=10;
		setExpression("wip()");
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_POINTER_MEASURING.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationPointerMeasuring.Tooltip");
	}

	/**
	 * Liefert den Minimalwert der Skala.
	 * @return	Minimalwert der Skala
	 */
	public int getMinValue() {
		return Math.max(0,minValue);
	}

	/**
	 * Stellt den Minimalwert der Skala ein.
	 * @param minValue	Minimalwert der Skala
	 */
	public void setMinValue(final int minValue) {
		this.minValue=Math.max(0,minValue);
		fireChanged();
	}

	/**
	 * Liefert den Maximalwert der Skala.
	 * @return	Maximalwert der Skala
	 */
	public int getMaxValue() {
		return Math.max(1,maxValue);
	}

	/**
	 * Stellt den Maximalwert der Skala ein.
	 * @param maxValue	Maximalwert der Skala
	 */
	public void setMaxValue(final int maxValue) {
		this.maxValue=Math.max(1,maxValue);
		fireChanged();
	}

	/**
	 * Liefert die Farbe für den Zeiger.
	 * @return	Farbe für den Zeiger
	 */
	public Color getColor() {
		return (color==null)?DEFAULT_COLOR:color;
	}

	/**
	 * Stellt die Farbe für den Zeiger.
	 * @param color	Farbe für den Zeiger
	 */
	public void setColor(final Color color) {
		this.color=(color==null)?DEFAULT_COLOR:color;
		fireChanged();
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationPointerMeasuring)) return false;
		final ModelElementAnimationPointerMeasuring other=(ModelElementAnimationPointerMeasuring)element;

		if (minValue!=other.minValue) return false;
		if (maxValue!=other.maxValue) return false;
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
		if (element instanceof ModelElementAnimationPointerMeasuring) {
			final ModelElementAnimationPointerMeasuring source=(ModelElementAnimationPointerMeasuring)element;
			minValue=source.minValue;
			maxValue=source.maxValue;
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
	public ModelElementAnimationPointerMeasuring clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationPointerMeasuring element=new ModelElementAnimationPointerMeasuring(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Zeichenstil für den Rahmen beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private Stroke borderStroke=null;

	/**
	 * Linienbreite für den Rahmen beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int borderStrokeWidth=0;

	/**
	 * Zeichenstil für den Zeiger beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private Stroke pointerStroke=null;

	/**
	 * Linienbreite für den Zeiger beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int pointerStrokeWidth=0;

	/**
	 * Beschriftung für den Minimalwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private String lastMinValueString=null;

	/**
	 * Beschriftung für den Mittelwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private String lastMiddleValueString=null;

	/**
	 * Beschriftung für den Maximalwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private String lastMaxValueString=null;

	/**
	 * Minimalwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 * @see #lastMinValueString
	 */
	private int lastMinValue=0;

	/**
	 * Maximalwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 * @see #lastMaxValueString
	 */
	private int lastMaxValue=0;

	@Override
	protected void drawData(final Graphics2D graphics, final Rectangle rectangle, final double zoom) {
		/* Zeichenstile und Texte berechnen */

		final int newBorderStrokeWidth=Math.max(1,Math.min(rectangle.height,rectangle.width)/75);
		if (newBorderStrokeWidth!=borderStrokeWidth || borderStroke==null) {
			borderStroke=new BasicStroke(newBorderStrokeWidth);
			borderStrokeWidth=newBorderStrokeWidth;
		}

		final int newPointerStrokeWidth=Math.max(1,Math.min(rectangle.height,rectangle.width)/25);
		if (newPointerStrokeWidth!=pointerStrokeWidth || pointerStroke==null) {
			pointerStroke=new BasicStroke(newPointerStrokeWidth);
			pointerStrokeWidth=newPointerStrokeWidth;
		}

		if (lastMinValue!=minValue || lastMaxValue!=maxValue || lastMinValueString==null || lastMiddleValueString==null || lastMaxValueString==null) {
			lastMinValueString=NumberTools.formatLongNoGrouping(minValue);
			lastMiddleValueString=NumberTools.formatNumber((minValue+maxValue)*0.5);
			lastMaxValueString=NumberTools.formatLongNoGrouping(maxValue);
			lastMinValue=minValue;
			lastMaxValue=maxValue;
		}

		/* Skala füllen */

		graphics.setColor(Color.WHITE);
		graphics.fillArc(rectangle.x,rectangle.y,rectangle.width,2*rectangle.height,180,-90);
		graphics.fillArc(rectangle.x,rectangle.y,rectangle.width,2*rectangle.height,90,-90);

		/* Skalarahmen zeichnen */

		graphics.setStroke(borderStroke);
		graphics.setColor(Color.BLACK);
		graphics.drawLine(rectangle.x,rectangle.y+rectangle.height,rectangle.x+rectangle.width,rectangle.y+rectangle.height);
		graphics.drawArc(rectangle.x,rectangle.y,rectangle.width,2*rectangle.height,180,-90);
		graphics.drawArc(rectangle.x,rectangle.y,rectangle.width,2*rectangle.height,90,-90);

		/* Markierungsstiche zeichnen */

		for (int i=1;i<=9;i++) {
			final double x=Math.cos(Math.PI*(1-i/10.0));
			final double y=Math.sin(Math.PI*(1-i/10.0));
			final int xOut=rectangle.x+(int)Math.round(rectangle.width*((1+x)/2));
			final int yOut=rectangle.y+(int)Math.round(2*rectangle.height*((1-y)/2));
			final int xIn=rectangle.x+(int)Math.round(rectangle.width*0.05+0.9*rectangle.width*((1+x)/2));
			final int yIn=rectangle.y+(int)Math.round(2*rectangle.height*0.05+0.9*2*rectangle.height*((1-y)/2));
			graphics.drawLine(xOut,yOut,xIn,yIn);
		}

		/* Beschriftungen für Skala */

		int w;

		graphics.setFont(FontCache.getFontCache().getFont(FontFamily.SANS.name,0,(int)FastMath.round(12*zoom)));

		graphics.drawString(lastMinValueString,rectangle.x+5*newBorderStrokeWidth,rectangle.y+rectangle.height-2*newBorderStrokeWidth);

		w=graphics.getFontMetrics().stringWidth(lastMaxValueString);
		graphics.drawString(lastMaxValueString,rectangle.x+rectangle.width-5*newBorderStrokeWidth-w,rectangle.y+rectangle.height-2*newBorderStrokeWidth);

		w=graphics.getFontMetrics().stringWidth(lastMiddleValueString);
		graphics.drawString(lastMiddleValueString,rectangle.x+(int)Math.round(rectangle.width/2.0-w/2.0),rectangle.y+(int)Math.round(rectangle.height*0.1)+graphics.getFontMetrics().getMaxAscent());

		/* Zeiger einzeichnen */

		final double value;
		if (animationRunning) {
			final Double D=getAnimationDouble();
			if (D==null) value=0; else value=D.doubleValue();
		} else {
			value=minValue+(maxValue-minValue)*0.3;
		}

		graphics.setStroke(pointerStroke);
		graphics.setColor(color);
		final double valueScaled=Math.max(0,Math.min(1,(value-minValue)/(maxValue-minValue)));
		final double x=Math.cos(Math.PI*(1-valueScaled));
		final double y=Math.sin(Math.PI*(1-valueScaled));
		final int xIn=rectangle.x+(int)Math.round(rectangle.width*0.025+0.95*rectangle.width*((1+x)/2));
		final int yIn=rectangle.y+(int)Math.round(2*rectangle.height*0.025+0.95*2*rectangle.height*((1-y)/2));
		graphics.drawLine(rectangle.x+rectangle.width/2,rectangle.y+rectangle.height,xIn,yIn);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationPointerMeasuring.Name");
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
			new ModelElementAnimationPointerMeasuringDialog(owner,ModelElementAnimationPointerMeasuring.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationPointerMeasuring.XML.Root");
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression"));
		node.appendChild(sub);
		sub.setTextContent(getExpression());
		sub.setAttribute(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.MinValue"),""+minValue);
		sub.setAttribute(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.MaxValue"),""+maxValue);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Color"));
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

		if (Language.trAll("Surface.AnimationPointerMeasuring.XML.Expression",name)) {
			setExpression(content);
			final String minValueString=Language.trAllAttribute("Surface.AnimationPointerMeasuring.XML.Expression.MinValue",node);
			if (!minValueString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(minValueString);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.MinValue"),name,node.getParentNode().getNodeName());
				minValue=L.intValue();
			}
			final String maxValueString=Language.trAllAttribute("Surface.AnimationPointerMeasuring.XML.Expression.MaxValue",node);
			if (!maxValueString.isEmpty()) {
				final Long L=NumberTools.getPositiveLong(maxValueString);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.MaxValue"),name,node.getParentNode().getNodeName());
				maxValue=L.intValue();
			}
			return null;
		}

		if (Language.trAll("Surface.AnimationPointerMeasuring.XML.Color",name)) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationPointerMeasuring";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationPointerMeasuring(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationPointerMeasuring(rect) {\n");

		sb.append("  context.fillStyle=\"white\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.arcTo(rect.x,rect.y+rect.h,rect.x+rect.w/2,rect.y,20);\n");
		sb.append("  context.arcTo(rect.x+rect.w/2,rect.y,rect.x+rect.w,rect.y+rect.h,20);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h,rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.arcTo(rect.x,rect.y+rect.h,rect.x+rect.w/2,rect.y,Math.min(rect.h,rect.w));\n");
		sb.append("  context.arcTo(rect.x+rect.w/2,rect.y,rect.x+rect.w,rect.y+rect.h,Math.min(rect.h,rect.w));\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h,rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(HTMLOutputBuilder outputBuilder) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		outputBuilder.addJSUserFunction("drawAnimationPointerMeasuring",builder->getHTMLAnimationPointerMeasuring(builder));
		outputBuilder.outputBody.append("drawAnimationPointerMeasuring({x: "+p.x+",y: "+p.y+",w: "+s.width+",h: "+s.height+"});\n");
	}
}
