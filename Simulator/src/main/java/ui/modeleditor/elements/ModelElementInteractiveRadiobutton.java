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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.net.URL;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementInteractiveRadiobutton;
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
 * Radiobutton, �ber den w�hrend der Animation eine Variable auf einen bestimmten,
 * vorab definierten Werte gestellt werden kann. Durch die Verwendung derselben
 * Variable k�nnen mehrere Radiobuttons zusammenarbeiten.<br>
 * F�r eine normale Simulation (ohne Animationsausgabe) ist diese Station ohne Bedeutung.
 * @author Alexander Herzog
 */
public class ModelElementInteractiveRadiobutton extends ModelElementPosition implements ElementWithAnimationDisplay, ElementWithNewVariableNames, ElementAnimationClickable, InteractiveElement {
	private String variable;
	private double valueChecked;

	private volatile boolean animationRunning=false;
	private volatile boolean boxChecked=false;

	/**
	 * Konstruktor der Klasse <code>ModelElementInteractiveRadiobutton</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementInteractiveRadiobutton(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		setName(Language.tr("Surface.InteractiveRadiobutton.DefaultText"));
		variable="";
		valueChecked=1;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INTERACTIVE_RADIOBUTTON.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InteractiveRadiobutton.Tooltip");
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
	 * Liefert den Wert der Variable, wenn das Radiobutton markiert ist
	 * @return	Wert der Variable, wenn das Radiobutton markiert ist
	 */
	public double getValueChecked() {
		return valueChecked;
	}

	/**
	 * Stellt den Wert der Variable, wenn das Radiobutton markiert ist, ein
	 * @param valueChecked	Neuer Wert der Variable, wenn das Radiobutton markiert ist
	 */
	public void setValueChecked(final double valueChecked) {
		this.valueChecked=valueChecked;
		fireChanged();
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementInteractiveRadiobutton)) return false;

		if (!variable.equalsIgnoreCase(((ModelElementInteractiveRadiobutton)element).variable)) return false;
		if (valueChecked!=((ModelElementInteractiveRadiobutton)element).valueChecked) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInteractiveRadiobutton) {
			variable=((ModelElementInteractiveRadiobutton)element).variable;
			valueChecked=((ModelElementInteractiveRadiobutton)element).valueChecked;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInteractiveRadiobutton clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInteractiveRadiobutton element=new ModelElementInteractiveRadiobutton(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private double infoFontZoom;
	private Font infoFont;
	private int infoFontAscent;
	private int infoFontHeight;

	private void drawErrorMessage(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect, final double zoom) {
		final String error=BackgroundSystem.getBackgroundSystem().checkModelElement(this);
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

	private double lastZoomFont=-1;
	private Font lastFont;
	private Ellipse2D.Double drawCircle=new Ellipse2D.Double();

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
		final String text=getName();

		/* Font vorbereiten */
		if (zoom!=lastZoomFont || lastFont==null) {
			lastFont=FontCache.getFontCache().getFont(FontCache.defaultFamily,Font.PLAIN,(int)Math.round(14*zoom));
			lastZoomFont=zoom;
		}
		graphics.setFont(lastFont);

		/* H�he und Breite von Box+Text berechnen */
		final int height=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
		final int width=height+4+graphics.getFontMetrics().stringWidth(text);

		/* Umrechnen auf Zoomfaktor */
		int w=(int)FastMath.round(width/zoom);
		int h=(int)FastMath.round(height/zoom);
		if (getSize().width!=w || getSize().height!=h) setSize(new Dimension(w,h));
		final Point pos=getPosition(true);
		int x=(int)FastMath.round(pos.x*zoom);
		int y=(int)FastMath.round(pos.y*zoom);

		/* Box ausgeben */

		graphics.setColor(Color.BLACK);
		drawCircle.setFrame(x+2,y+2,height-4,height-4);
		((Graphics2D)graphics).draw(drawCircle);
		graphics.setColor(Color.LIGHT_GRAY);
		drawCircle.setFrame(x+3,y+3,height-6,height-6);
		((Graphics2D)graphics).draw(drawCircle);

		/* H�kchen einzeichnen */
		if (boxChecked || !animationRunning) {
			graphics.setColor(Color.BLUE);
			drawCircle.setFrame(x+5,y+5,height-10,height-10);
			((Graphics2D)graphics).fill(drawCircle);
		}

		/* Text ausgeben */
		graphics.setColor(Color.BLACK);
		graphics.drawString(text,x+height+(int)FastMath.round(2*zoom),y+graphics.getFontMetrics().getAscent());

		/* Markierungsrahmen zeichnen */
		if (isSelected() && showSelectionFrames) {
			drawRect(graphics,drawRect,zoom,Color.GREEN,2,null,2);
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				drawRect(graphics,drawRect,zoom,Color.BLUE,2,null,2);
			}
		}

		/* Ggf. Fehlermeldung anzeigen */
		if (!animationRunning && surface!=null) drawErrorMessage(graphics,drawRect,new Rectangle(x,y,w,h),zoom);
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InteractiveRadiobutton.Name");
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
			new ModelElementInteractiveRadiobuttonDialog(owner,ModelElementInteractiveRadiobutton.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.InteractiveRadiobutton.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveRadiobutton.XML.Variable")));
		sub.setTextContent(variable);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveRadiobutton.XML.CheckedValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(valueChecked));
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

		if (Language.trAll("Surface.InteractiveRadiobutton.XML.Variable",name)) {
			variable=content.trim();
			return null;
		}

		if (Language.trAll("Surface.InteractiveRadiobutton.XML.CheckedValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			valueChecked=D.doubleValue();
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementInteractiveRadiobutton";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InteractiveRadiobutton.Description.Variable"),variable,1010);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InteractiveRadiobutton.Description.Value"),NumberTools.formatNumber(valueChecked),1100);
	}

	private volatile boolean clicked=false;

	@Override
	public void clicked(int x, int y) {
		clicked=true;
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		clicked=false;
	}

	private void triggerAction(final SimulationData simData) {
		((RunElementInteractiveRadiobutton)simData.runModel.elementsFast[getId()]).clicked(simData);
	}

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (simData!=null && clicked) {
			triggerAction(simData);
			clicked=false;
		}

		boolean needRepaint=false;
		if (simData!=null) {
			if (!animationRunning) needRepaint=true;
			animationRunning=true;
			final boolean boxCheckedNew=((RunElementInteractiveRadiobutton)simData.runModel.elementsFast[getId()]).getBoxChecked(simData);
			if (boxChecked!=boxCheckedNew) needRepaint=true;
			boxChecked=boxCheckedNew;
		}

		return needRepaint;
	}

	@Override
	public String[] getVariables() {
		if (variable==null || variable.trim().isEmpty()) return new String[0]; else return new String[]{variable.trim()};
	}

	private String getHTMRadiobutton(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawRadiobutton(data) {\n");

		sb.append("  context.font=\"14px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.textAlign=\"left\";\n");
		sb.append("  context.textBaseline=\"hanging\";\n");
		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.fillText(data.text,data.x+18,data.y);\n");

		sb.append("  context.strokeStyle=\"gray\";\n");
		sb.append("  context.lineWidth=2;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.arc(data.x+7,data.y+7,7,7,0,2*Math.PI);\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawRadiobutton",builder->getHTMRadiobutton(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{text: \""+getName()+"\", x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";

		outputBuilder.outputBody.append("drawRadiobutton("+rect+");\n");
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
