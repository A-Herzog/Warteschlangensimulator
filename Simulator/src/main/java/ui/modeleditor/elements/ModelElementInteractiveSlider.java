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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.net.URL;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementInteractiveSlider;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.speedup.BackgroundSystem;

/**
 * Schieberegler, über den während der Animation eine Variable auf verschiedene Werte,
 * deren Bereich vorab definierte wurde, gestellt werden kann.<br>
 * Für eine normale Simulation (ohne Animationsausgabe) ist diese Station ohne Bedeutung.
 * @author Alexander Herzog
 */
public class ModelElementInteractiveSlider extends ModelElementPosition implements ElementWithAnimationDisplay, ElementWithNewVariableNames, ElementAnimationClickable, InteractiveElement {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Name der einzustellenden Variable
	 * @see #getVariable()
	 * @see #setVariable(String)
	 */
	private String variable;

	/**
	 * Liefert den Minimalwert
	 * @see #getMinValue()
	 * @see #setMaxValue(double)
	 */
	private double minValue;

	/**
	 * Liefert den Maximalwert
	 * @see #getMinValue()
	 * @see #setMaxValue(double)
	 */
	private double maxValue;

	/**
	 * Schrittweite
	 * @see #getStep()
	 * @see #setStep(double)
	 */
	private double step;

	/**
	 * Läuft momentan eine Animation?
	 */
	private volatile boolean animationRunning=false;

	/**
	 * Anzuzeigender Variablenwert
	 * @see #drawSlider(Graphics2D, Rectangle, double)
	 */
	private volatile double variableValue=0;

	/**
	 * Position des Schiebereglers
	 * @see #drawSlider(Graphics2D, Rectangle, double)
	 */
	private volatile double percent=0.33; /* Während Editor Standardwert. Während der Animation wird hier der anzuzeigende Wert hinterlegt. */

	/**
	 * Konstruktor der Klasse <code>ModelElementInteractiveSlider</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInteractiveSlider(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(150,50),Shapes.ShapeType.SHAPE_NONE);
		variable="";
		minValue=0;
		maxValue=10;
		step=1;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INTERACTIVE_SLIDER.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InteractiveSlider.Tooltip");
	}

	/**
	 * Liefert den Namen der einzustellenden Variable
	 * @return	Name der einzustellenden Variable
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * Stellt den Namen der einzustellenden Variable ein
	 * @param variable	Name der einzustellenden Variable
	 */
	public void setVariable(String variable) {
		if (variable!=null) this.variable=variable;
		fireChanged();
	}

	/**
	 * Liefert den Minimalwert
	 * @return	Minimalwert
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * Stellt den Minimalwert ein
	 * @param minValue	Neuer Minimalwert
	 */
	public void setMinValue(final double minValue) {
		this.minValue=minValue;
		fireChanged();
	}

	/**
	 * Liefert den Maximalwert
	 * @return	Maximalwert
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Stellt den Maximalwert ein
	 * @param maxValue	Neuer Maximalwert
	 */
	public void setMaxValue(final double maxValue) {
		this.maxValue=maxValue;
		fireChanged();
	}

	/**
	 * Liefert die Schrittweite
	 * @return	Schrittweite
	 */
	public double getStep() {
		return step;
	}

	/**
	 * Stellt die Schrittweite ein
	 * @param step	Neue Schrittweite
	 */
	public void setStep(final double step) {
		this.step=step;
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
		if (!(element instanceof ModelElementInteractiveSlider)) return false;

		if (!variable.equalsIgnoreCase(((ModelElementInteractiveSlider)element).variable)) return false;
		if (minValue!=((ModelElementInteractiveSlider)element).minValue) return false;
		if (maxValue!=((ModelElementInteractiveSlider)element).maxValue) return false;
		if (step!=((ModelElementInteractiveSlider)element).step) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInteractiveSlider) {
			variable=((ModelElementInteractiveSlider)element).variable;
			minValue=((ModelElementInteractiveSlider)element).minValue;
			maxValue=((ModelElementInteractiveSlider)element).maxValue;
			step=((ModelElementInteractiveSlider)element).step;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInteractiveSlider clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInteractiveSlider element=new ModelElementInteractiveSlider(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Prüft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem geprüft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gemäß dessen der Punkt skaliert wird
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt nahe der Linie befindet
	 */
	protected final boolean isNearLine(final Point p, final Point q, final Point point, final double zoom) {
		final Point v=new Point(q.x-p.x,q.y-p.y); /* Verbindungsvektor P->Q */
		final Point x=new Point((int)FastMath.round(point.x/zoom),(int)FastMath.round(point.y/zoom));

		double alpha=0;
		if (v.y!=0) {
			alpha=((double)v.y)/(v.x*v.x+v.y*v.y)*(x.y-((double)v.x)/v.y*(p.x-x.x)-p.y);
		} else {
			alpha=((double)v.x)/(v.y*v.y+v.x*v.x)*(x.x-((double)v.y)/v.x*(p.y-x.y)-p.x);
		}
		final Point y=new Point((int)FastMath.round(p.x+alpha*v.x),(int)FastMath.round(p.y+alpha*v.y)); /* Lotfußpunkt von X auf P->Q */

		if (Math.abs(x.x-y.x)>MAX_POINT_DELTA || Math.abs(x.y-y.y)>MAX_POINT_DELTA) return false; /* Abstand von Gerade zu groß? */
		final double len=FastMath.sqrt(v.x*v.x+v.y*v.y);

		if (alpha*len<-MAX_POINT_DELTA || alpha*len>len+MAX_POINT_DELTA) return false; /* Fußpunkt vor Beginn oder nach Ende der Strecke P->Q? */

		return true;
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		if (super.containsPoint(point,zoom)) return true;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom)) return true;

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
		case 0: return new Point(p); /* Kopie, damit diese unabhängig von Veränderungen des Wertes ist */
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
	 * Zeichnet eine Markierungs-Box an einer Ecke des Elements
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param point	Position des Punktes
	 * @param zoom	Zoomfaktor
	 */
	private void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	/**
	 * Zeichnet den eigentlichen Schieberegler
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param rectangle	Größe des Elements
	 * @param zoom	Zoomfaktor
	 */
	private void drawSlider(final Graphics2D graphics, final Rectangle rectangle, final double zoom) {
		final Color saveColor=graphics.getColor();
		final Stroke saveStroke=graphics.getStroke();

		String name;

		if (animationRunning) {
			name=getName().trim();
			if (name.isEmpty()) {
				name=variable+":="+NumberTools.formatNumber(variableValue,3);
			} else {
				name=name+", "+variable+":="+NumberTools.formatNumber(variableValue,3);
			}
		} else {
			name=getName().trim();
		}

		if (!name.isEmpty()) {
			graphics.setColor(Color.BLACK);
			graphics.setFont(new Font(Font.DIALOG,0,(int)FastMath.round(10*zoom)));
			graphics.drawString(name,rectangle.x,rectangle.y+graphics.getFontMetrics().getAscent());
		}

		graphics.setStroke(new BasicStroke(5));
		final int m=rectangle.y+rectangle.height/2;

		graphics.setColor(Color.GRAY);
		graphics.drawLine(rectangle.x,m,rectangle.x+rectangle.width,m);

		graphics.setColor(Color.BLUE);
		if (percent>1) percent=1;
		if (percent<0) percent=0;
		final int pos=rectangle.x+(int)Math.round(rectangle.width*percent);
		graphics.drawLine(pos,m-15,pos,m+15);

		graphics.setColor(saveColor);
		graphics.setStroke(saveStroke);
	}

	/**
	 * Zoomfaktor beim letzten Aufruf von {@link #drawErrorMessage(Graphics, Rectangle, Rectangle, double)}
	 * @see #drawErrorMessage(Graphics, Rectangle, Rectangle, double)
	 */
	private double infoFontZoom;

	/**
	 * Schriftart beim letzten Aufruf von {@link #drawErrorMessage(Graphics, Rectangle, Rectangle, double)}
	 * @see #drawErrorMessage(Graphics, Rectangle, Rectangle, double)
	 */
	private Font infoFont;

	/**
	 * Höhe der Schrift über der Grundlinie beim letzten Aufruf von {@link #drawErrorMessage(Graphics, Rectangle, Rectangle, double)}
	 * @see #drawErrorMessage(Graphics, Rectangle, Rectangle, double)
	 */
	private int infoFontAscent;

	/**
	 * Gesamthöhe der Schrift beim letzten Aufruf von {@link #drawErrorMessage(Graphics, Rectangle, Rectangle, double)}
	 * @see #drawErrorMessage(Graphics, Rectangle, Rectangle, double)
	 */
	private int infoFontHeight;

	/**
	 * Gibt wenn nötig eine Fehlermeldung zu dem Element aus.
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param objectRect	Größe des Elements
	 * @param zoom	Zoomfaktor
	 */
	private void drawErrorMessage(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect, final double zoom) {
		final String error=BackgroundSystem.checkModelElement(this);
		if (error==null) return;

		final String[] words=error.trim().split("\\s+");
		final StringBuilder sb=new StringBuilder();
		int count=0;
		boolean firstLine=true;
		for (String word: words) {
			if (sb.length()>0) {if (count>20) {sb.append("\n"); count=0;} else {if (firstLine) firstLine=false; else sb.append(" ");}}
			sb.append(word);
			count+=word.length();
		}
		final String[] info=sb.toString().split("\\n");

		if (infoFont==null || zoom!=infoFontZoom) {
			infoFontZoom=zoom;
			infoFont=new Font(Font.DIALOG,0,(int)FastMath.round(10*zoom));
			graphics.setFont(infoFont);
			infoFontAscent=graphics.getFontMetrics().getAscent();
			infoFontHeight=infoFontAscent+graphics.getFontMetrics().getDescent();
		} else {
			graphics.setFont(infoFont);
		}

		final Shape clipShape=graphics.getClip();
		graphics.setClip(drawRect);
		graphics.setColor(Color.RED);
		for (int i=0;i<info.length;i++) graphics.drawString(info[i],objectRect.x,objectRect.y+objectRect.height+infoFontAscent+i*infoFontHeight);
		graphics.setClip(clipShape);
	}

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

		Color lineColor=null;

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke(3));
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke(3));
			}
		}

		final Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
		if (rectangle.width<0) {
			rectangle.x+=rectangle.width;
			rectangle.width=-rectangle.width;
		}
		if (rectangle.height<0) {
			rectangle.y+=rectangle.height;
			rectangle.height=-rectangle.height;
		}

		if (lineColor!=null) {
			g2.setColor(lineColor);
			g2.draw(rectangle);
		}

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}

		if (!animationRunning && surface!=null) drawErrorMessage(graphics,drawRect,rectangle,zoom);

		drawSlider(g2,rectangle,zoom);

		setClip(graphics,drawRect,null);
		g2.setStroke(saveStroke);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InteractiveSlider.Name");
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
			new ModelElementInteractiveSliderDialog(owner,ModelElementInteractiveSlider.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.InteractiveSlider.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveSlider.XML.Variable")));
		sub.setTextContent(variable);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveSlider.XML.MinValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(minValue));

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveSlider.XML.MaxValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(maxValue));

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveSlider.XML.StepWide")));
		sub.setTextContent(NumberTools.formatSystemNumber(step));
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

		if (Language.trAll("Surface.InteractiveSlider.XML.Variable",name)) {
			variable=content.trim();
			return null;
		}

		if (Language.trAll("Surface.InteractiveSlider.XML.MinValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			minValue=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.InteractiveSlider.XML.MaxValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			maxValue=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.InteractiveSlider.XML.StepWide",name)) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			step=D.doubleValue();
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementInteractiveSlider";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InteractiveSlider.Description.Variable"),variable,1000);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InteractiveSlider.Description.Range"),"["+NumberTools.formatNumber(minValue)+";"+NumberTools.formatNumber(maxValue)+"] "+Language.tr("ModelDescription.InteractiveSlider.Description.Step")+"="+NumberTools.formatNumber(step),1100);
	}

	/**
	 * Wurde der Slider angeklickt? (Und wenn ja an welcher Stelle?)
	 * @see #clicked
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private volatile Point clicked=null;

	@Override
	public void clicked(int x, int y) {
		clicked=new Point(x,y);
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		clicked=null;
	}

	/**
	 * Löst die in dem Objekt hinterlegten Aktionen aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param x	x-Koordinate an der auf den Slider geklickt wurde
	 * @param y	y-Koordinate an der auf den Slider geklickt wurde
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private void triggerAction(final SimulationData simData, final int x, final int y) {
		((RunElementInteractiveSlider)simData.runModel.elementsFast[getId()]).clicked(simData,((double)x)/getSize().width);
	}

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (simData!=null && clicked!=null) {
			triggerAction(simData,clicked.x,clicked.y);
			clicked=null;
		}

		boolean needRepaint=false;
		if (simData!=null) {
			if (!animationRunning) needRepaint=true;
			animationRunning=true;
			final int variableIndex=((RunElementInteractiveSlider)simData.runModel.elementsFast[getId()]).variableIndex;
			final double variableValueNew=simData.runData.variableValues[variableIndex];
			if (variableValue!=variableValueNew) needRepaint=true;
			variableValue=variableValueNew;
			percent=(variableValue-minValue)/(maxValue-minValue);
		}

		return needRepaint;
	}

	@Override
	public String[] getVariables() {
		if (variable==null || variable.trim().isEmpty()) return new String[0]; else return new String[]{variable.trim()};
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLSlider(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawSlider(rect) {\n");

		sb.append("  context.strokeStyle=\"black\";\n");
		sb.append("  context.lineWidth=5;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y+Math.round(rect.h/2));\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+Math.round(rect.h/2));\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.stroke();\n");

		sb.append("  context.strokeStyle=\"blue\";\n");
		sb.append("  context.lineWidth=5;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x+Math.round(rect.w/3),rect.y+Math.round(rect.h/2)-15);\n");
		sb.append("  context.lineTo(rect.x+Math.round(rect.w/3),rect.y+Math.round(rect.h/2)+15);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawSlider",builder->getHTMLSlider(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";

		outputBuilder.outputBody.append("drawSlider("+rect+");\n");
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
