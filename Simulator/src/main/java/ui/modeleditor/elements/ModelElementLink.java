/**
 * Copyright 2023 Alexander Herzog
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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.tools.FlatLaFHelper;

/**
 * Hyperlink
 * @author Alexander Herzog
 */
public class ModelElementLink extends ModelElementPosition implements ElementWithAnimationEditOptions {
	/**
	 * Textfarbe (helles Layout)
	 */
	private static final Color LINK_COLOR=Color.BLUE;

	/**
	 * Textfarbe (dunkles Layout)
	 */
	private static final Color LINK_COLOR_DARK=new Color(128,128,255);

	/**
	 * Darzustellender Text
	 * @see #getText()
	 * @see #setText(String)
	 */
	private String text;

	/**
	 * Link
	 * @see #getLink()
	 * @see #setLink(String)
	 */
	private String link;

	/**
	 * Zu verwendende Schriftart
	 * @see #getFontFamily()
	 * @see #setFontFamily(ui.modeleditor.elements.FontCache.FontFamily)
	 */
	private FontCache.FontFamily fontFamily=FontCache.defaultFamily;

	/**
	 * Schriftgröße
	 * @see #getTextSize()
	 * @see #setTextSize(int)
	 */
	private int textSize=14;

	/**
	 * Konstruktor der Klasse {@link ModelElementLink}
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementLink(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		useSizeOnCompare=false;
		text=Language.tr("Surface.Link.DefaultText");
		link="";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_LINK.getIcon();
	}

	@Override
	public boolean isVisualOnly() {
		return true;
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		if (!link.isEmpty()) return link;
		return Language.tr("Surface.Link.Tooltip");
	}

	/**
	 * Liefert den aktuell darzustellenden Text
	 * @return	Darzustellender Text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Stellt den darzustellenden Text ein.
	 * @param text	Darzustellender Text
	 */
	public void setText(String text) {
		if (text==null || text.trim().isEmpty()) text=Language.tr("Surface.Link.DefaultText");
		this.text=text;
		fireChanged();
	}

	/**
	 * Liefert den Link
	 * @return	Link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Stellt den Link ein.
	 * @param link	Link
	 */
	public void setLink(String link) {
		if (link==null || link.trim().isEmpty()) link="";
		this.link=link;
		fireChanged();
	}

	/**
	 * Liefert die momentan eingestellte Schriftart
	 * @return	Aktuelle Schriftart
	 */
	public FontCache.FontFamily getFontFamily() {
		return fontFamily;
	}

	/**
	 * Stellt die zu verwendende Schriftart ein
	 * @param fontFamily	Neue Schriftart
	 */
	public void setFontFamily(FontCache.FontFamily fontFamily) {
		if (fontFamily!=null) this.fontFamily=fontFamily;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Größe der Schrift
	 * @return	Aktuelle Schriftgröße
	 */
	public int getTextSize() {
		return textSize;
	}

	/**
	 * Stellt die Schriftgröße ein
	 * @param textSize	Neue Schriftgröße
	 */
	public void setTextSize(final int textSize) {
		this.textSize=Math.max(6,Math.min(128,textSize));
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
		if (!(element instanceof ModelElementLink)) return false;
		final ModelElementLink otherLink=(ModelElementLink)element;

		if (!otherLink.text.equals(text)) return false;
		if (!otherLink.link.equals(link)) return false;
		if (otherLink.fontFamily!=fontFamily) return false;
		if (textSize!=otherLink.textSize) return false;
		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementLink) {
			final ModelElementLink copySource=(ModelElementLink)element;
			text=copySource.text;
			link=copySource.link;
			fontFamily=copySource.fontFamily;
			textSize=copySource.textSize;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementLink clone(final EditModel model, final ModelSurface surface) {
		final ModelElementLink element=new ModelElementLink(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Zu verwendender Text-Renderer
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private final ModelElementTextRenderer textRenderer=new ModelElementTextRendererPlain();

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

		/* Daten in Renderer laden */
		textRenderer.setText(text,false);
		textRenderer.setStyle(textSize,false,false,fontFamily.name,ModelElementText.TextAlign.LEFT);
		textRenderer.calc(graphics,zoom);

		/* Position und Größe berechnen */
		final Point pos=getPosition(true);
		final int canvasX=(zoom==1.0)?pos.x:(int)FastMath.round(pos.x*zoom);
		final int canvasY=(zoom==1.0)?pos.y:(int)FastMath.round(pos.y*zoom);
		final int canvasW=textRenderer.getWidth();
		final int canvasH=textRenderer.getHeight();

		/* Wenn nötig Größe der Box anpassen */
		final int boxW=(zoom==1.0)?canvasW:(int)FastMath.round(canvasW/zoom);
		final int boxH=(zoom==1.0)?canvasH:(int)FastMath.round(canvasH/zoom);
		final Dimension boxSize=getSize();
		if (boxSize.width!=boxW || boxSize.height!=boxH) setSize(new Dimension(boxW,boxH));

		/* Text ausgeben */
		final Color drawColor=FlatLaFHelper.isDark()?LINK_COLOR_DARK:LINK_COLOR;
		textRenderer.draw(graphics,canvasX,canvasY,drawColor);
		final int y=canvasY+canvasH;
		graphics.setColor(drawColor);
		graphics.drawLine(canvasX+1,y,canvasX+canvasW,y);

		/* Rahmen zeichnen */
		if (isSelected() && showSelectionFrames) {
			drawRect(graphics,drawRect,zoom,Color.GREEN,2,null,2);
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				drawRect(graphics,drawRect,zoom,Color.BLUE,2,null,2);
			}
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Link.Name");
	}

	/**
	 * Fragt ab, ob der Link aufgerufen werden soll.
	 * @param owner	Übergeordnetes Element zur Ausrichtung des Dialog
	 * @param link	Aufzurufender Link
	 * @return	Liefert <code>true</code>, wenn der Link verarbeitet wurde; bei <code>false</code> soll stattdessen der Bearbeitendialog geöffnet werden
	 */
	private boolean openLink(final Component owner, final String link) {
		final List<String> options=new ArrayList<>();
		final List<String> info=new ArrayList<>();

		if (MsgBox.allowOpenURL) {
			options.add(MsgBox.OptionYes);
			info.add(MsgBox.OpenURLInfoYes);
		}

		options.add(MsgBox.OptionNo);
		info.add(MsgBox.OpenURLInfoNo);

		options.add(MsgBox.OptionCopyURL);
		info.add(MsgBox.OptionInfoCopyURL);

		options.add(Language.trPrimary("Surface.Link.XML.ConfirmDialog.OptionEdit"));
		info.add(Language.trPrimary("Surface.Link.XML.ConfirmDialog.OptionEditInfo"));

		final int result=MsgBox.options(owner,Language.trPrimary("Surface.Link.XML.ConfirmDialog.Title"),String.format(MsgBox.OpenURLInfo,link),options.toArray(new String[0]),info.toArray(new String[0]));

		if (MsgBox.allowOpenURL) {
			switch (result) {
			case -1:
				return true;
			case 0:
				try {
					Desktop.getDesktop().browse(new URL(link).toURI());
				} catch (IOException | URISyntaxException e) {
					MsgBox.error(owner,MsgBox.OpenURLErrorTitle,String.format(MsgBox.OpenURLErrorMessage,link));
				}
				return true;
			case 1:
				return true;
			case 2:
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(link),null);
				return true;
			case 3:
				return false;
			}
		} else {
			switch (result) {
			case -1:
				return true;
			case 0:
				return true;
			case 1:
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(link),null);
				return true;
			case 2:
				return false;
			}
		}

		return false;
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
			if (link.trim().isEmpty() || !openLink(owner,link)) {
				new ModelElementLinkDialog(owner,ModelElementLink.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
			}
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			if (link.trim().isEmpty() || !openLink(owner,link)) {
				new ModelElementLinkDialog(owner,ModelElementLink.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
			}
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Link.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		setNameInt(""); /* Früher wurden die Daten im Name-Element gespeichert -> Altlasten entsorgen. */
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		/* Text */
		sub=doc.createElement(Language.trPrimary("Surface.Link.XML.Text"));
		node.appendChild(sub);
		sub.setTextContent(text.trim());

		/* Link */
		sub=doc.createElement(Language.trPrimary("Surface.Link.XML.Link"));
		node.appendChild(sub);
		sub.setTextContent(link.trim());


		/* Schriftart */
		if (fontFamily!=FontCache.defaultFamily) {
			sub=doc.createElement(Language.trPrimary("Surface.Link.XML.FontFamily"));
			node.appendChild(sub);
			sub.setTextContent(fontFamily.name);
		}

		/* Schriftgröße */
		sub=doc.createElement(Language.trPrimary("Surface.Link.XML.FontSize"));
		node.appendChild(sub);
		sub.setTextContent(""+textSize);
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

		/* Schriftart */
		if (Language.trAll("Surface.Link.XML.FontFamily",name)) {
			fontFamily=FontCache.getFontCache().getFamilyFromName(content);
			return null;
		}

		/* Schriftgröße */
		if (Language.trAll("Surface.Link.XML.FontSize",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			textSize=I;
			return null;
		}

		/* Text */
		if (Language.trAll("Surface.Link.XML.Text",name)) {
			text=content;
			return null;
		}

		/* Link */
		if (Language.trAll("Surface.Link.XML.Link",name)) {
			link=content;
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementLink";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLText(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawLink(p,text,fontSize) {\n");

		sb.append("  context.font=\"\"+fontSize+\"px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.textAlign=\"left\";\n");
		sb.append("  context.textBaseline=\"hanging\";\n");
		sb.append("  context.fillStyle=\"blue\";\n");
		sb.append("  context.fillText(text,p.x,p.y);\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawLink",builder->getHTMLText(builder));

		final Point p=getPosition(true);
		final String s=HTMLOutputBuilder.encodeHTML(getText(),true).replace("\n","\\n");
		outputBuilder.outputBody.append("drawLink({x: "+p.x+", y: "+p.y+"},\""+s+"\","+textSize+");\n");
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputText"),text,newText->{text=newText;});
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.FontSize"),textSize,newFontSize->{if (newFontSize>0) textSize=newFontSize;});
	}
}

