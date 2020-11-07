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
import simulator.elements.RunElementInteractiveCheckbox;
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
 * Checkbox, über die während der Animation eine Variable auf zwei verschiedene,
 * vorab definierte Werte gestellt werden kann.<br>
 * Für eine normale Simulation (ohne Animationsausgabe) ist diese Station ohne Bedeutung.
 * @author Alexander Herzog
 */
public class ModelElementInteractiveCheckbox extends ModelElementPosition implements ElementWithAnimationDisplay, ElementWithNewVariableNames, ElementAnimationClickable, InteractiveElement {
	/**
	 * Name der einzustellenden Variable
	 * @see #getVariable()
	 * @see #setVariable(String)
	 */
	private String variable;

	/**
	 * Wert der Variable, wenn die Checkbox markiert ist
	 * @see #getValueChecked()
	 * @see #setValueChecked(double)
	 */
	private double valueChecked;

	/**
	 * Wert der Variable, wenn die Checkbox nicht markiert ist
	 * @see #getValueUnchecked()
	 * @see #setValueUnchecked(double)
	 */
	private double valueUnchecked;

	private volatile boolean animationRunning=false;
	private volatile boolean boxChecked=false;

	/**
	 * Konstruktor der Klasse <code>ModelElementInteractiveCheckbox</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInteractiveCheckbox(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		setName(Language.tr("Surface.InteractiveCheckbox.DefaultName"));
		variable="";
		valueChecked=1;
		valueUnchecked=0;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INTERACTIVE_CHECKBOX.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InteractiveCheckbox.Tooltip");
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
	 * Liefert den Wert der Variable, wenn die Checkbox markiert ist
	 * @return	Wert der Variable, wenn die Checkbox markiert ist
	 */
	public double getValueChecked() {
		return valueChecked;
	}

	/**
	 * Stellt den Wert der Variable, wenn die Checkbox markiert ist, ein
	 * @param valueChecked	Neuer Wert der Variable, wenn die Checkbox markiert ist
	 */
	public void setValueChecked(final double valueChecked) {
		this.valueChecked=valueChecked;
		fireChanged();
	}

	/**
	 * Liefert den Wert der Variable, wenn die Checkbox nicht markiert ist
	 * @return	Wert der Variable, wenn die Checkbox nicht markiert ist
	 */
	public double getValueUnchecked() {
		return valueUnchecked;
	}

	/**
	 * Stellt den Wert der Variable, wenn die Checkbox nicht markiert ist, ein
	 * @param valueUnchecked	Neuer Wert der Variable, wenn die Checkbox nicht markiert ist
	 */
	public void setValueUnchecked(final double valueUnchecked) {
		this.valueUnchecked=valueUnchecked;
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
		if (!(element instanceof ModelElementInteractiveCheckbox)) return false;

		if (!variable.equalsIgnoreCase(((ModelElementInteractiveCheckbox)element).variable)) return false;
		if (valueChecked!=((ModelElementInteractiveCheckbox)element).valueChecked) return false;
		if (valueUnchecked!=((ModelElementInteractiveCheckbox)element).valueUnchecked) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInteractiveCheckbox) {
			variable=((ModelElementInteractiveCheckbox)element).variable;
			valueChecked=((ModelElementInteractiveCheckbox)element).valueChecked;
			valueUnchecked=((ModelElementInteractiveCheckbox)element).valueUnchecked;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInteractiveCheckbox clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInteractiveCheckbox element=new ModelElementInteractiveCheckbox(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private double infoFontZoom;
	private Font infoFont;
	private int infoFontAscent;
	private int infoFontHeight;

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

	private double lastZoomFont=-1;
	private Font lastFont;
	private Stroke checkLine=new BasicStroke(2);

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
		final String text=getName();

		/* Font vorbereiten */
		if (zoom!=lastZoomFont || lastFont==null) {
			lastFont=FontCache.getFontCache().getFont(FontCache.defaultFamily,Font.PLAIN,(int)Math.round(14*zoom));
			lastZoomFont=zoom;
		}
		graphics.setFont(lastFont);

		/* Höhe und Breite von Box+Text berechnen */
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
		graphics.drawRect(x+2,y+2,height-4,height-4);
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.drawRect(x+3,y+3,height-6,height-6);

		/* Häkchen einzeichnen */
		if (boxChecked || !animationRunning) {
			graphics.setColor(Color.BLUE);
			((Graphics2D)graphics).setStroke(checkLine);
			graphics.drawLine(x+5,y+height/2,x+2*height/4,y+4*height/5);
			graphics.drawLine(x+2*height/4,y+4*height/5,x+height-5,y+5);
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
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InteractiveCheckbox.Name");
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
			new ModelElementInteractiveCheckboxDialog(owner,ModelElementInteractiveCheckbox.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.InteractiveCheckbox.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveCheckbox.XML.Variable")));
		sub.setTextContent(variable);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveCheckbox.XML.CheckedValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(valueChecked));

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InteractiveCheckbox.XML.UncheckedValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(valueUnchecked));
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

		if (Language.trAll("Surface.InteractiveCheckbox.XML.Variable",name)) {
			variable=content.trim();
			return null;
		}

		if (Language.trAll("Surface.InteractiveCheckbox.XML.CheckedValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			valueChecked=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.InteractiveCheckbox.XML.UncheckedValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			valueUnchecked=D.doubleValue();
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementInteractiveCheckbox";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InteractiveCheckbox.Description.Variable"),variable,1000);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InteractiveCheckbox.Description.Values"),NumberTools.formatNumber(valueChecked)+" / "+NumberTools.formatNumber(valueUnchecked)+"]",1100);
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
		((RunElementInteractiveCheckbox)simData.runModel.elementsFast[getId()]).clicked(simData);
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
			final boolean boxCheckedNew=((RunElementInteractiveCheckbox)simData.runModel.elementsFast[getId()]).getBoxChecked(simData);
			if (boxChecked!=boxCheckedNew) needRepaint=true;
			boxChecked=boxCheckedNew;
		}

		return needRepaint;
	}

	@Override
	public String[] getVariables() {
		if (variable==null || variable.trim().isEmpty()) return new String[0]; else return new String[]{variable.trim()};
	}

	private String getHTMCheckbox(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawCheckbox(data) {\n");

		sb.append("  context.font=\"14px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.textAlign=\"left\";\n");
		sb.append("  context.textBaseline=\"hanging\";\n");
		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.fillText(data.text,data.x+18,data.y);\n");

		sb.append("  context.strokeStyle=\"gray\";\n");
		sb.append("  context.lineWidth=2;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(data.x,data.y);\n");
		sb.append("  context.lineTo(data.x+15,data.y);\n");
		sb.append("  context.lineTo(data.x+15,data.y+15);\n");
		sb.append("  context.lineTo(data.x,data.y+15);\n");
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
		outputBuilder.addJSUserFunction("drawCheckbox",builder->getHTMCheckbox(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{text: \""+getName()+"\", x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";

		outputBuilder.outputBody.append("drawCheckbox("+rect+");\n");
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
