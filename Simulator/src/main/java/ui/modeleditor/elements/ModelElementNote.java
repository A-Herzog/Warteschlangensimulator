/**
 * Copyright 2021 Alexander Herzog
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Notiz-Element
 * @author Alexander Herzog
 */
public class ModelElementNote extends ModelElementPosition {
	/**
	 * Größe des Icons auf der Zeichenfläche
	 */
	public static final int ICON_SIZE=30;

	/**
	 * Notiz in diesem Element
	 */
	private String note;

	/**
	 * Icon zur Darstellung auf der Zeichenfläche
	 */
	private String icon;

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource;

	/**
	 * Konstruktor der Klasse <code>ModelElementText</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementNote(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(ICON_SIZE,ICON_SIZE),Shapes.ShapeType.SHAPE_RECTANGLE);
		useSizeOnCompare=false;
		note="";
		icon=AnimationImageSource.ICONS.get(AnimationImageSource.iconNameNote);
		imageSource=new AnimationImageSource();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TEXT.getIcon();
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Liefert eine gekürzte Version der Notiz zur Anzeige in dem Tooltip.
	 * @return	Gekürzte Version der Notiz
	 */
	private String getNoteStart() {
		final StringBuilder result=new StringBuilder();
		int count=0;

		for (String line: note.split("\\\n")) {
			final String l=line.trim();
			if (l.isEmpty()) continue;

			if (result.length()>0) result.append("<br>");
			if (l.length()>40) {
				result.append(encodeHTMLentities(l.substring(0,40)));
				result.append("...");
			} else {
				result.append(encodeHTMLentities(l));
			}

			count++;
			if (count>=3) break;
		}

		return result.toString();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		if (note==null || note.trim().isEmpty()) return Language.tr("Surface.Note.Tooltip");

		final StringBuilder result=new StringBuilder();
		result.append(Language.tr("Surface.Note.Tooltip"));
		result.append("<br>");
		result.append(getNoteStart());
		return result.toString();
	}

	/**
	 * Liefert die Notiz
	 * @return	Notiz
	 */
	public String getNote() {
		return note;
	}

	/**
	 * Stellt die Notiz ein.
	 * @param note	Notiz
	 */
	public void setNote(final String note) {
		if (note==null) this.note=""; else this.note=note;
		fireChanged();
	}

	/**
	 * Liefert das Icon zur Darstellung auf der Zeichenfläche.
	 * @return	Icon zur Darstellung auf der Zeichenfläche
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Stellt das Icon zur Darstellung auf der Zeichenfläche ein.
	 * @param icon	Icon zur Darstellung auf der Zeichenfläche
	 */
	public void setIcon(final String icon) {
		if (imageSource.get(icon,getModel().animationImages,ICON_SIZE)==null) return;
		this.icon=icon;
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
		if (!(element instanceof ModelElementNote)) return false;
		final ModelElementNote otherNote=(ModelElementNote)element;

		if (!otherNote.note.equals(note)) return false;
		if (!otherNote.icon.equals(icon)) return false;
		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementNote) {
			final ModelElementNote copySource=(ModelElementNote)element;
			note=copySource.note;
			icon=copySource.icon;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementNote clone(final EditModel model, final ModelSurface surface) {
		final ModelElementNote element=new ModelElementNote(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Gibt das Bild aus.
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param zoom	Zoomfaktor
	 */
	private void drawImage(final Graphics graphics, final double zoom) {
		final Point p1=getPosition(true);
		final Dimension s1=getSize();

		final BufferedImage image=imageSource.get(icon,getModel().animationImages,(int)Math.round(s1.width*zoom));
		if (image==null) return;

		final Point p2=new Point(p1.x+Math.min(0,s1.width),p1.y+Math.min(0,s1.height));
		final Point p3=new Point((int)Math.round(p2.x*zoom),(int)Math.round(p2.y*zoom));
		graphics.drawImage(image,p3.x,p3.y,null);
	}

	/**
	 * Größe der Markierungsboxen an den Ecken der Elemente
	 */
	protected static final int SELECT_BOX_SIZE=7;

	/**
	 * Zeichnet eine Markierungs-Box an einer Ecke des Elements
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param point	Position des Punktes
	 * @param zoom	Zoomfaktor
	 */
	private void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		final Stroke saveStroke=graphics.getStroke();
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
		graphics.setStroke(saveStroke);
	}

	/**
	 * Zeichnet den Rahmen um die Grafik
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Normaler Rahmen (<code>false</code>) oder Rahmen für ein selektiertes Element (<code>true</code>)
	 */
	private void drawFrame(final Graphics graphics, final double zoom, final boolean showSelectionFrames) {
		if (!showSelectionFrames) return;
		if (!isSelected() && !isSelectedArea()) return;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		Color lineColor;

		if (isSelected()) {
			lineColor=Color.GREEN;
		} else {
			lineColor=Color.BLUE;
		}
		g2.setStroke(new BasicStroke((float)(3*zoom)));

		Rectangle rectangle=new Rectangle((int)Math.round(Math.min(p.x,p.x+s.width)*zoom),(int)Math.round(Math.min(p.y,p.y+s.height)*zoom),(int)Math.round(Math.abs(s.width)*zoom),(int)Math.round(Math.abs(s.height)*zoom));
		g2.setColor(lineColor);
		g2.draw(rectangle);

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}
		g2.setStroke(saveStroke);
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

		/* Bild zeichnen */
		drawImage(graphics,zoom);

		/* Rahmen zeichnen */
		drawFrame(graphics,zoom,showSelectionFrames);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Note.Name");
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
			new ModelElementNoteDialog(owner,ModelElementNote.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Note.XML.Root");
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

		/* Text */
		for (String line: note.trim().split("\\n")) {
			/* if (line.trim().isEmpty()) continue; */
			sub=doc.createElement(Language.trPrimary("Surface.Note.XML.Text"));
			node.appendChild(sub);
			sub.setTextContent(line.trim());
		}

		/* Icon */
		sub=doc.createElement(Language.trPrimary("Surface.Note.XML.Icon"));
		node.appendChild(sub);
		sub.setTextContent(icon);
	}

	/**
	 * Wurde in einem früheren Aufruf von
	 * {@link #loadProperty(String, String, Element)}
	 * bereits eine Textzeile geladen?
	 * @see #loadProperty(String, String, Element)
	 */
	private boolean lineLoaded=false;

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		final String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		/* Text */
		if (Language.trAll("Surface.Note.XML.Text",name)) {
			if (!lineLoaded) note="";
			lineLoaded=true;
			if (!note.isEmpty()) note+="\n";
			note+=content;
			return null;
		}

		/* Icon */
		if (Language.trAll("Surface.Note.XML.Icon",name)) {
			icon=content;
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementNote";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLDrawImage(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawNoteImage(rect,image) {\n");

		sb.append("  var img=new Image();\n");
		sb.append("  img.src=image;\n");
		sb.append("  img.onload=function(){context.drawImage(img,rect.x,rect.y,rect.w,rect.h);};\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawNoteImage",builder->getHTMLDrawImage(builder));

		final Point p=getPosition(true);
		final Dimension s=getSize();

		String image="";
		try {
			final BufferedImage imageObj=imageSource.get(icon,getModel().animationImages,ICON_SIZE);
			if (imageObj!=null) {
				final ByteArrayOutputStream out=new ByteArrayOutputStream();
				ImageIO.write(imageObj,"png",out);
				final byte[] bytes=out.toByteArray();
				final String base64bytes=Base64.getEncoder().encodeToString(bytes);
				image="data:image/png;base64,"+base64bytes;
			}
		} catch (IOException e) {
			image="";
		}

		outputBuilder.outputBody.append("drawNoteImage({x: "+p.x+",y: "+p.y+", w:"+s.width+",h: "+s.height+"},\""+image+"\");\n");
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