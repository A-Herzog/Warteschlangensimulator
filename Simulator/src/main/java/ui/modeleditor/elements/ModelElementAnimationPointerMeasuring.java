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
import java.util.Objects;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationCustomDrawExpression;
import ui.modeleditor.elements.FontCache.FontFamily;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt eine Analog-Skala w�hrend der Animation an
 * �ber die ein aktueller Simulationswert angezeigt werden kann.
 * @author Alexander Herzog
 */
public class ModelElementAnimationPointerMeasuring extends ModelElementAnimationCustomDrawExpression {
	/**
	 * Standard-Farbe f�r den Zeiger
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
	 * Soll der gelbe Bereich verwendet werden?
	 */
	private boolean yellowRangeUse;


	/**
	 * Startwert f�r den gelben Bereich
	 */
	private int yellowRangeStart;

	/**
	 * Startwert f�r den roten Bereich
	 */
	private boolean redRangeUse;

	/**
	 * Soll der rote Bereich verwendet werden?
	 */
	private int redRangeStart;

	/**
	 * Farbe f�r den Zeiger
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color=DEFAULT_COLOR;

	/**
	 * Farbobjekt f�r die Darstellung des gelben Bereichs.
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private final Color yellowAreaColor=new Color(255,255,0);

	/**
	 * Farbobjekt f�r die Darstellung des roten Bereichs.
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private final Color redAreaColor=new Color(255,0,0);

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationPointerMeasuring(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(100,50));
		minValue=0;
		maxValue=10;
		yellowRangeStart=-1;
		yellowRangeUse=false;
		yellowRangeStart=7;
		redRangeUse=false;
		redRangeStart=9;
		expression.setExpression("wip()");
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_POINTER_MEASURING.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
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
	 * Gibt an, ob der gelbe Bereich verwendet werden soll.
	 * @return	Soll der gelbe Bereich verwendet werden?
	 */
	public boolean isYellowRangeUse() {
		return yellowRangeUse;
	}

	/**
	 * Stellt ein, ob der gelbe Bereich verwendet werden soll.
	 * @param yellowRangeUse	Soll der gelbe Bereich verwendet werden?
	 */
	public void setYellowRangeUse(final boolean yellowRangeUse) {
		this.yellowRangeUse=yellowRangeUse;
		fireChanged();
	}

	/**
	 * Liefert den Startwert f�r den gelben Bereich.
	 * @return	Startwert f�r den gelben Bereich
	 */
	public int getYellowRangeStart() {
		return yellowRangeStart;
	}

	/**
	 * Stellt den Startwert f�r den gelben Bereich ein.
	 * @param yellowRangeStart	Startwert f�r den gelben Bereich
	 */
	public void setYellowRangeStart(final int yellowRangeStart) {
		this.yellowRangeStart=yellowRangeStart;
		yellowRangeUse=true;
		fireChanged();
	}

	/**
	 * Gibt an, ob der rote Bereich verwendet werden soll.
	 * @return	Soll der rote Bereich verwendet werden?
	 */
	public boolean isRedRangeUse() {
		return redRangeUse;
	}

	/**
	 * Stellt ein, ob der rote Bereich verwendet werden soll.
	 * @param redRangeUse	Soll der rote Bereich verwendet werden?
	 */
	public void setRedRangeUse(final boolean redRangeUse) {
		this.redRangeUse=redRangeUse;
		fireChanged();
	}

	/**
	 * Liefert den Startwert f�r den roten Bereich.
	 * @return	Startwert f�r den roten Bereich
	 */
	public int getRedRangeStart() {
		return redRangeStart;
	}

	/**
	 * Stellt den Startwert f�r den roten Bereich ein.
	 * @param redRangeStart	Startwert f�r den roten Bereich
	 */
	public void setRedRangeStart(final int redRangeStart) {
		this.redRangeStart=redRangeStart;
		redRangeUse=true;
		fireChanged();
	}

	/**
	 * Liefert die Farbe f�r den Zeiger.
	 * @return	Farbe f�r den Zeiger
	 */
	public Color getColor() {
		return (color==null)?DEFAULT_COLOR:color;
	}

	/**
	 * Stellt die Farbe f�r den Zeiger.
	 * @param color	Farbe f�r den Zeiger
	 */
	public void setColor(final Color color) {
		this.color=(color==null)?DEFAULT_COLOR:color;
		fireChanged();
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationPointerMeasuring)) return false;
		final ModelElementAnimationPointerMeasuring other=(ModelElementAnimationPointerMeasuring)element;

		if (minValue!=other.minValue) return false;
		if (maxValue!=other.maxValue) return false;
		if (yellowRangeUse!=other.yellowRangeUse) return false;
		if (yellowRangeUse) {
			if (yellowRangeStart!=other.yellowRangeStart) return false;
		}
		if (redRangeUse!=other.redRangeUse) return false;
		if (redRangeUse) {
			if (redRangeStart!=other.redRangeStart) return false;
		}
		if (!Objects.equals(color,other.color)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationPointerMeasuring) {
			final ModelElementAnimationPointerMeasuring source=(ModelElementAnimationPointerMeasuring)element;
			minValue=source.minValue;
			maxValue=source.maxValue;
			yellowRangeUse=source.yellowRangeUse;
			yellowRangeStart=source.yellowRangeStart;
			redRangeUse=source.redRangeUse;
			redRangeStart=source.redRangeStart;
			color=source.color;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationPointerMeasuring clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationPointerMeasuring element=new ModelElementAnimationPointerMeasuring(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Zeichenstil f�r den Rahmen beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private Stroke borderStroke=null;

	/**
	 * Zeichenstil f�r die Markierung des gelben und des roten Bereichs beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private Stroke areaStroke=null;

	/**
	 * Linienbreite f�r den Rahmen beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int borderStrokeWidth=0;

	/**
	 * Linienbreite f�r die Markierung des gelben und des roten Bereichs beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int areaStrokeWidth=0;

	/**
	 * Zeichenstil f�r den Zeiger beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private Stroke pointerStroke=null;

	/**
	 * Linienbreite f�r den Zeiger beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int pointerStrokeWidth=0;

	/**
	 * Beschriftung f�r den Minimalwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private String lastMinValueString=null;

	/**
	 * Beschriftung f�r den Mittelwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private String lastMiddleValueString=null;

	/**
	 * Beschriftung f�r den Maximalwert der Skala beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
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

		final int newAreaStrokeWidth=Math.max(1,Math.min(rectangle.height,rectangle.width)/10);
		if (newAreaStrokeWidth!=areaStrokeWidth || areaStroke==null) {
			areaStroke=new BasicStroke(newAreaStrokeWidth);
			areaStrokeWidth=newBorderStrokeWidth;
		}

		final int newPointerStrokeWidth=Math.max(1,Math.min(rectangle.height,rectangle.width)/25);
		if (newPointerStrokeWidth!=pointerStrokeWidth || pointerStroke==null) {
			pointerStroke=new BasicStroke(newPointerStrokeWidth);
			pointerStrokeWidth=newPointerStrokeWidth;
		}

		final int xAreaFactor=rectangle.width*1/20;
		final int yAreaFactor=rectangle.width*1/20;

		if (lastMinValue!=minValue || lastMaxValue!=maxValue || lastMinValueString==null || lastMiddleValueString==null || lastMaxValueString==null) {
			lastMinValueString=NumberTools.formatLongNoGrouping(minValue);
			lastMiddleValueString=NumberTools.formatNumber((minValue+maxValue)*0.5);
			lastMaxValueString=NumberTools.formatLongNoGrouping(maxValue);
			lastMinValue=minValue;
			lastMaxValue=maxValue;
		}

		/* Skala f�llen */

		graphics.setColor(Color.WHITE);
		graphics.fillArc(rectangle.x,rectangle.y,rectangle.width,2*rectangle.height,180,-90);
		graphics.fillArc(rectangle.x,rectangle.y,rectangle.width,2*rectangle.height,90,-90);

		/* Ggf. gelben Bereich einzeichnen */

		if (yellowRangeUse && yellowRangeStart<maxValue && maxValue>minValue) {
			graphics.setStroke(areaStroke);
			graphics.setColor(yellowAreaColor);
			double d=(((double)yellowRangeStart)-minValue)/(maxValue-minValue);
			if (d<0) d=0;
			if (d<1) {
				if (d<0.5) {
					final int a1=(int)Math.round(180-d*180);
					final int a2=(int)Math.round(90-180*d);
					graphics.drawArc(rectangle.x+xAreaFactor,rectangle.y+yAreaFactor,rectangle.width-2*xAreaFactor,2*rectangle.height-yAreaFactor-2*yAreaFactor,a1,-a2);
					graphics.drawArc(rectangle.x+xAreaFactor,rectangle.y+yAreaFactor,rectangle.width-2*xAreaFactor,2*rectangle.height-yAreaFactor-2*yAreaFactor,90,-90);
				} else {
					final int a1=(int)Math.round(180-d*180);
					graphics.drawArc(rectangle.x+xAreaFactor,rectangle.y+yAreaFactor,rectangle.width-2*xAreaFactor,2*rectangle.height-yAreaFactor-2*yAreaFactor,a1,-a1);
				}
			}
		}

		/* Ggf. roten Bereich einzeichnen */

		if (redRangeUse && redRangeStart<maxValue) {
			graphics.setStroke(areaStroke);
			graphics.setColor(redAreaColor);
			double d=(((double)redRangeStart)-minValue)/(maxValue-minValue);
			if (d<0) d=0;
			if (d<1) {
				if (d<0.5) {
					final int a1=(int)Math.round(180-d*180);
					final int a2=(int)Math.round(90-180*d);
					graphics.drawArc(rectangle.x+xAreaFactor,rectangle.y+yAreaFactor,rectangle.width-2*xAreaFactor,2*rectangle.height-yAreaFactor-2*yAreaFactor,a1,-a2);
					graphics.drawArc(rectangle.x+xAreaFactor,rectangle.y+yAreaFactor,rectangle.width-2*xAreaFactor,2*rectangle.height-yAreaFactor-2*yAreaFactor,90,-90);
				} else {
					final int a1=(int)Math.round(180-d*180);
					graphics.drawArc(rectangle.x+xAreaFactor,rectangle.y+yAreaFactor,rectangle.width-2*xAreaFactor,2*rectangle.height-yAreaFactor-2*yAreaFactor,a1,-a1);
				}
			}
		}

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

		/* Beschriftungen f�r Skala */

		int w;

		graphics.setColor(Color.BLACK);
		graphics.setFont(FontCache.getFontCache().getFont(FontFamily.SANS.name,0,FastMath.round(12*rectangle.width/150)));

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
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationPointerMeasuring.Name");
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
			new ModelElementAnimationPointerMeasuringDialog(owner,ModelElementAnimationPointerMeasuring.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationPointerMeasuring.XML.Root");
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression"));
		node.appendChild(sub);
		expression.storeToXML(sub);

		sub.setAttribute(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.MinValue"),""+minValue);
		sub.setAttribute(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.MaxValue"),""+maxValue);

		if (yellowRangeUse) sub.setAttribute(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.YellowRangeStart"),""+yellowRangeStart);
		if (redRangeUse) sub.setAttribute(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.RedRangeStart"),""+redRangeStart);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Color"));
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

		if (Language.trAll("Surface.AnimationPointerMeasuring.XML.Expression",name)) {
			expression.loadFromXML(node);
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
			final String yellowRangeStartString=Language.trAllAttribute("Surface.AnimationPointerMeasuring.XML.Expression.YellowRangeStart",node);
			if (!yellowRangeStartString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(yellowRangeStartString);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.YellowRangeStart"),name,node.getParentNode().getNodeName());
				yellowRangeStart=L.intValue();
				yellowRangeUse=true;
			}
			final String redRangeStartString=Language.trAllAttribute("Surface.AnimationPointerMeasuring.XML.Expression.RedRangeStart",node);
			if (!redRangeStartString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(redRangeStartString);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationPointerMeasuring.XML.Expression.RedRangeStart"),name,node.getParentNode().getNodeName());
				redRangeStart=L.intValue();
				redRangeUse=true;
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
	 * Liefert die Javascript-Daten f�r die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten f�r die Station
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

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.MinValue"),minValue,newMinValue->{minValue=newMinValue;});
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.MaxValue"),maxValue,newMaxValue->{maxValue=newMaxValue;});
		if (yellowRangeUse) searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.YellowRange"),yellowRangeStart,newYellowRangeStart->{yellowRangeStart=newYellowRangeStart;});
		if (redRangeUse) searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.RedRange"),redRangeStart,newRedRangeStart->{redRangeStart=newRedRangeStart;});
	}
}