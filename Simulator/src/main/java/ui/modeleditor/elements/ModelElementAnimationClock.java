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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.Objects;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationCustomDrawExpression;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.tools.FlatLaFHelper;

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
	 * Zeigerfarbe (kann <code>null</code> sein für automatische Bestimmung)
	 * @see #getLineColor()
	 * @see #setLineColor(Color)
	 */
	private Color lineColor=null;

	/**
	 * Zusätzlich Uhrzeit digital anzeigen?
	 * @see #isShowDigitalTime()
	 * @see #setShowDigitalTime(boolean)
	 */
	public boolean showDigitalTime;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationClock</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationClock(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50));
		expression.setExpression("TNow()");
		showDigitalTime=false;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_CLOCK.getIcon();
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
	 * Liefert die aktuelle Farbe.
	 * @return	Aktuelle Farbe
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Stellt die Farbe ein.
	 * @param color	Neue Farbe
	 */
	public void setColor(final Color color) {
		if (color!=null) this.color=color;
	}

	/**
	 * Liefert die aktuelle Zeigerfarbe.
	 * @return	Aktuelle Zeigerfarbe
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * Stellt die Zeigerfarbe ein.
	 * @param lineColor	Neue Zeigerfarbe
	 */
	public void setLineColor(final Color lineColor) {
		this.lineColor=lineColor;
	}

	/**
	 * Soll die Uhrzeit zusätzlich Uhrzeit digital angezeigt werden?
	 * @return	Uhrzeit digital anzeigen?
	 */
	public boolean isShowDigitalTime() {
		return showDigitalTime;
	}

	/**
	 * Stellt ein, ob die Uhrzeit zusätzlich Uhrzeit digital angezeigt werden soll?
	 * @param showDigitalTime	Uhrzeit digital anzeigen?
	 */
	public void setShowDigitalTime(final boolean showDigitalTime) {
		this.showDigitalTime=showDigitalTime;
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
		final ModelElementAnimationClock other=(ModelElementAnimationClock)element;

		if (!other.color.equals(color)) return false;
		if (!Objects.equals(other.lineColor,lineColor)) return false;
		if (other.showDigitalTime!=showDigitalTime) return false;

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
			final ModelElementAnimationClock source=(ModelElementAnimationClock)element;
			color=source.color;
			lineColor=source.lineColor;
			showDigitalTime=source.showDigitalTime;
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

	/**
	 * Farbe für die digitale Anzeige der Zeit
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private final Color digitalTimeColor=FlatLaFHelper.isDark()?Color.LIGHT_GRAY:Color.BLACK;

	/**
	 * Cache für das Builder-Objekt für die Zusammensetzung der digitalen Zeitanzeige
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private StringBuilder digitalTimeBuilder=null;

	/**
	 * Breite des Ausgaberechtecks beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int lastDigitalWidth=-1;

	/**
	 * Gewählte Schriftgröße beim letzten Aufruf von {@link #drawData(Graphics2D, Rectangle, double)}
	 * @see #drawData(Graphics2D, Rectangle, double)
	 */
	private int lastDigitalSize=-1;

	/**
	 * Gibt die Zeit in digitaler Form (hh:mm) aus.
	 * @param g	<code>Graphics2D</code>-Objekt in das die Ausgabe eingefügt werden soll
	 * @param rectangle	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param time Aktuelle Zeit in Sekunden
	 * @return	Verwendete Höhe
	 */
	private int drawDigital(final Graphics2D g, final Rectangle rectangle, final double zoom, int time) {
		/* Auszugebenden Wert bestimmen */
		time/=60;
		final int min=time%60;
		final int h=time/60;
		if (digitalTimeBuilder==null) digitalTimeBuilder=new StringBuilder(); else digitalTimeBuilder.setLength(0);
		if (h<10) digitalTimeBuilder.append('0');
		digitalTimeBuilder.append(h);
		digitalTimeBuilder.append(':');
		if (min<10) digitalTimeBuilder.append('0');
		digitalTimeBuilder.append(min);

		final FontCache fontCache=FontCache.getFontCache();

		/* Berechnung der Schriftgröße */
		if (lastDigitalWidth!=rectangle.width) {
			final Font testFont=fontCache.getFont(FontCache.defaultFamily,0,12);
			final int widthAt12pt=g.getFontMetrics(testFont).stringWidth("99:99");
			lastDigitalSize=Math.round(12.0f/widthAt12pt*rectangle.width);
			lastDigitalWidth=rectangle.width;
		}

		/* Ausgabe der Zeit in digitaler Form */
		final Font font=fontCache.getFont(FontCache.defaultFamily,0,lastDigitalSize);
		final int ascent=g.getFontMetrics(font).getAscent();
		g.setFont(font);
		g.setColor(digitalTimeColor);
		g.drawString(digitalTimeBuilder.toString(),rectangle.x,rectangle.y+rectangle.height);

		return ascent;
	}

	/**
	 * Zuletzt in {@link #drawData(Graphics2D, Rectangle, double)} verwendete Hintergrundfarbe
	 * @see #drawClock(Graphics2D, Rectangle, double, int)
	 */
	private Color lastColor;

	/**
	 * Zuletzt in {@link #drawData(Graphics2D, Rectangle, double)} verwendete Linienfarbe
	 * @see #drawClock(Graphics2D, Rectangle, double, int)
	 */
	private Color lastColorLine;

	/**
	 * Zuletzt in {@link #drawData(Graphics2D, Rectangle, double)} verwendete Linienbreite
	 * @see #drawClock(Graphics2D, Rectangle, double, int)
	 */
	private int lastWidth;

	/**
	 * Zuletzt in {@link #drawData(Graphics2D, Rectangle, double)} verwendeter Zeichenstil
	 * @see #drawClock(Graphics2D, Rectangle, double, int)
	 */
	private BasicStroke lastWidthStroke;

	/**
	 * Zeichenstil für Info-Ausgaben
	 * @see #drawClock(Graphics2D, Rectangle, double, int)
	 */
	private final BasicStroke infoStroke=new BasicStroke(1);

	/**
	 * Zeigt die Zeit in Form einer runden 12-Stunden-Uhr an.
	 * @param g	<code>Graphics2D</code>-Objekt in das die Uhr eingezeichnet werden soll
	 * @param rectangle	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param time Aktuelle Zeit in Sekunden
	 */
	private void drawClock(final Graphics2D g, final Rectangle rectangle, final double zoom, final int time) {
		/* Hintergrund zeichnen */
		g.setColor(color);
		g.fill(new Ellipse2D.Double(rectangle.x,rectangle.y,rectangle.width,rectangle.height));

		/* Zeigerfarbe bestimmen */
		if (lineColor!=null) {
			g.setColor(lineColor);
		} else {
			if (lastColor==null || !lastColor.equals(color) || lastColorLine==null) {
				lastColor=color;
				lastColorLine=new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue());
			}
			g.setColor(lastColorLine);
		}

		/* Zeiger zeichnen */
		final int mx=rectangle.x+rectangle.width/2;
		final int my=rectangle.y+rectangle.height/2;
		final double rx=rectangle.width/2;
		final double ry=rectangle.height/2;

		int x;
		int y;

		/* FastMath.sin und FastMath.cos benötigt mehr Speicher, daher verwenden wir Math.sin und Math.cos */

		g.setStroke(infoStroke);
		for (int i=0;i<12;i++) {
			x=mx+(int)FastMath.round(0.9*rx*Math.sin(i/12.0*2*FastMath.PI));
			y=my-(int)FastMath.round(0.9*ry*Math.cos(i/12.0*2*FastMath.PI));
			final int x2=mx+(int)FastMath.round(rx*Math.sin(i/12.0*2*FastMath.PI));
			final int y2=my-(int)FastMath.round(ry*Math.cos(i/12.0*2*FastMath.PI));
			g.drawLine(x,y,x2,y2);
		}

		if (lastWidth!=rectangle.width || lastWidthStroke==null) {
			lastWidth=rectangle.width;
			lastWidthStroke=new BasicStroke(FastMath.round(FastMath.ceil(rectangle.width/100.0)));
		}
		g.setStroke(lastWidthStroke);

		x=mx+(int)FastMath.round(2.0/3.0*rx*Math.sin((time%43200)/43200.0*2*FastMath.PI));
		y=my-(int)FastMath.round(2.0/3.0*ry*Math.cos((time%43200)/43200.0*2*FastMath.PI));
		g.drawLine(mx,my,x,y);

		x=mx+(int)FastMath.round(0.95*rx*Math.sin((time%3600)/3600.0*2*FastMath.PI));
		y=my-(int)FastMath.round(0.95*ry*Math.cos((time%3600)/3600.0*2*FastMath.PI));
		g.drawLine(mx,my,x,y);
	}

	@Override
	protected void drawData(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		final int time=(int)(getAnimationLong()%86400);

		final int usedHeight=showDigitalTime?drawDigital(g,rectangle,zoom,time):0;

		rectangle.height-=usedHeight;
		drawClock(g,rectangle,zoom,time);
		rectangle.height+=usedHeight;
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
			new ModelElementAnimationClockDialog(owner,ModelElementAnimationClock.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationClockDialog(owner,ModelElementAnimationClock.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
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

		if (lineColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationClock.XML.LineColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(lineColor));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationClock.XML.Expression"));
		node.appendChild(sub);
		expression.storeToXML(sub);

		if (showDigitalTime) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationClock.XML.ShowDigital"));
			node.appendChild(sub);
			sub.setTextContent("1");
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

		if (Language.trAll("Surface.AnimationClock.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationClock.XML.LineColor",name) && !content.trim().isEmpty()) {
			lineColor=EditModel.loadColor(content);
			if (lineColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationClock.XML.Expression",name)) {
			expression.loadFromXML(node);
			return null;
		}

		if (Language.trAll("Surface.AnimationClock.XML.ShowDigital",name)) {
			showDigitalTime=content.equals("1");
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

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
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
		final Color useLineColor=(lineColor!=null)?lineColor:new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue());
		final String line="\""+HTMLOutputBuilder.colorToHTML(useLineColor)+"\"";

		outputBuilder.outputBody.append("drawAnimationClock("+rect+","+line+","+fill+");\n");
	}
}