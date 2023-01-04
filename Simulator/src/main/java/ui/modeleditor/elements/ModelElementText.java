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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.Function;
import java.util.function.Supplier;

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
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Beschriftungstext
 * @author Alexander Herzog
 */
public final class ModelElementText extends ModelElementPosition {
	/**
	 * Vorgabe-Textfarbe
	 * @see #color
	 */
	private static final Color DEFAULT_COLOR=Color.BLACK;

	/**
	 * Darzustellender Text
	 * @see #getText()
	 * @see #setText(String)
	 */
	private String text;

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
	 * Ausgabe des Textes im Fettdruck
	 * @see #getTextBold()
	 * @see #setTextBold(boolean)
	 */
	private boolean bold;

	/**
	 * Ausgabe des Textes im Kursivdruck
	 * @see #getTextItalic()
	 * @see #setTextItalic(boolean)
	 */
	private boolean italic;

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 * @see #isInterpretSymbols()
	 * @see #setInterpretSymbols(boolean)
	 */
	private boolean interpretSymbols;

	/**
	 * Soll Markdown interpretiert werden?
	 * @see #isInterpretMarkdown()
	 * @see #setInterpretMarkdown(boolean)
	 */
	private boolean interpretMarkdown;

	/**
	 * Sollen LaTeX-Ausdrücke interpretiert werden?
	 * @see #isInterpretLaTeX()
	 * @see #setInterpretLaTeX(boolean)
	 */
	private boolean interpretLaTeX;

	/**
	 * Textfarbe
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color=DEFAULT_COLOR;

	/**
	 * Ausrichtung der Textzeilen
	 */
	public enum TextAlign {
		/** Linksbündig */
		LEFT(()->Language.trPrimary("Surface.Text.XML.TextAlign.Left"),name->Language.trAll("Surface.Text.XML.TextAlign.Left",name)),
		/** Zentriert */
		CENTER(()->Language.trPrimary("Surface.Text.XML.TextAlign.Center"),name->Language.trAll("Surface.Text.XML.TextAlign.Center",name)),
		/** Rechtsbündig */
		RIGHT(()->Language.trPrimary("Surface.Text.XML.TextAlign.Right"),name->Language.trAll("Surface.Text.XML.TextAlign.Right",name));

		/** Liefert den Namen der Ausrichtungsart zum Speichern in xml-Dateien */
		private final Supplier<String> getName;
		/** Prüft, ob ein xml-Bezeichner (beim Laden) zu der Ausrichtungsart passt */
		private final Function<String,Boolean> testName;

		/**
		 * Konstruktor des Enum
		 * @param getName	Liefert den Namen der Ausrichtungsart zum Speichern in xml-Dateien
		 * @param testName	Prüft, ob ein xml-Bezeichner (beim Laden) zu der Ausrichtungsart passt
		 */
		TextAlign(final Supplier<String> getName, final Function<String,Boolean> testName) {
			this.getName=getName;
			this.testName=testName;
		}

		/**
		 * Liefert den Namen der Ausrichtungsart zum Speichern in xml-Dateien.
		 * @return	Namen der Ausrichtungsart zum Speichern in xml-Dateien
		 */
		public String getName() {
			return getName.get();
		}

		/**
		 * Ermittelt basierend auf einem xml-Bezeichner die zugehörige Ausrichtungsart.
		 * @param name	xml-Bezeichner
		 * @return	Ausrichtungsart (passt der xml-Bezeichner zu keiner Ausrichtungsart, so wird {@link TextAlign#LEFT} als Fallback geliefert)
		 */
		public static TextAlign fromName(final String name) {
			for (TextAlign textAlign: values()) if (textAlign.testName.apply(name)) return textAlign;
			return LEFT;
		}
	}

	/**
	 * Ausrichtung der Textzeilen
	 * @see #getTextAlign()
	 * @see #setTextAlign(TextAlign)
	 */
	private TextAlign textAlign;

	/**
	 * Füllfarbe des Kastens (kann <code>null</code> sein für transparent)
	 * @see #getFillColor()
	 * @see #setFillColor(Color)
	 */
	private Color fillColor=null;

	/**
	 * Deckkraft der Hintergrundfarbe
	 * @see #getFillAlpha()
	 * @see #setFillAlpha(double)
	 */
	private double fillAlpha=1.0;

	/**
	 * Konstruktor der Klasse <code>ModelElementText</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementText(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		useSizeOnCompare=false;
		text=Language.tr("Surface.Text.DefaultText");
		interpretSymbols=true;
		interpretMarkdown=false;
		interpretLaTeX=false;
		textAlign=TextAlign.LEFT;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TEXT.getIcon();
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
		return Language.tr("Surface.Text.Tooltip");
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
		if (text==null || text.trim().isEmpty()) text=Language.tr("Surface.Text.DefaultText");
		this.text=text;
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
	 * Liefert die Angabe, ob der Text fett gedruckt werden soll.
	 * @return	Ausgabe des Textes im Fettdruck
	 */
	public boolean getTextBold() {
		return bold;
	}

	/**
	 * Stellt ein, ob der Text fett gedruckt werden soll.
	 * @param bold	Angabe, ob der Text fett gedruckt werden soll
	 */
	public void setTextBold(final boolean bold) {
		if (this.bold==bold) return;
		this.bold=bold;
		fireChanged();
	}

	/**
	 * Liefert die Angabe, ob der Text kursiv gedruckt werden soll.
	 * @return	Ausgabe des Textes im Kursivdruck
	 */
	public boolean getTextItalic() {
		return italic;
	}

	/**
	 * Stellt ein, ob der Text kursiv gedruckt werden soll.
	 * @param italic	Angabe, ob der Text kursiv gedruckt werden soll
	 */
	public void setTextItalic(final boolean italic) {
		if (this.italic==italic) return;
		this.italic=italic;
		fireChanged();
	}

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden?
	 * @return	HTML- und LaTeX-Symbole interpretieren
	 */
	public boolean isInterpretSymbols() {
		return interpretSymbols;
	}

	/**
	 * Stellt ein, ob HTML- und LaTeX-Symbole interpretiert werden sollen.
	 * @param interpretSymbols	HTML- und LaTeX-Symbole interpretier
	 */
	public void setInterpretSymbols(boolean interpretSymbols) {
		this.interpretSymbols=interpretSymbols;
	}

	/**
	 * Soll Markdown interpretiert werden?
	 * @return	Markdown interpretieren
	 */
	public boolean isInterpretMarkdown() {
		return interpretMarkdown;
	}

	/**
	 * Stellt ein, ob Markdown interpretiert werden soll.
	 * @param interpretMarkdown	Markdown interpretieren
	 */
	public void setInterpretMarkdown(final boolean interpretMarkdown) {
		this.interpretMarkdown=interpretMarkdown;
	}

	/**
	 * Sollen LaTeX-Formatierungen interpretiert werden?
	 * @return	LaTeX-Formatierungen interpretieren
	 */
	public boolean isInterpretLaTeX() {
		return interpretLaTeX;
	}

	/**
	 * Stellt ein, ob LaTeX-Formatierungen interpretiert werden soll.
	 * @param interpretLaTeX	LaTeX-Formatierungen interpretieren
	 */
	public void setInterpretLaTeX(final boolean interpretLaTeX) {
		this.interpretLaTeX=interpretLaTeX;
	}

	/**
	 * Liefert die aktuelle Textfarbe
	 * @return	Aktuelle Textfarbe
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Stellt die Textfarbe ein
	 * @param color	Textfarbe
	 */
	public void setColor(final Color color) {
		if (color!=null) {
			this.color=color;
			fireChanged();
		}
	}

	/**
	 * Liefert die aktuelle Ausrichtung der Textzeilen.
	 * @return	Ausrichtung der Textzeilen
	 */
	public TextAlign getTextAlign() {
		return textAlign;
	}

	/**
	 * Stellt die Ausrichtung der Textzeilen ein.
	 * @param textAlign	Ausrichtung der Textzeilen
	 */
	public void setTextAlign(final TextAlign textAlign) {
		if (textAlign!=null) {
			this.textAlign=textAlign;
			fireChanged();
		}
	}

	/**
	 * Liefert die aktuelle Füllfarbe des Kastens
	 * @return	Aktuelle Füllfarbe des Kastens (kann <code>null</code> sein für transparent)
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Stellt die Füllfarbe des Kastens ein
	 * @param color	Füllfarbe des Kastens (oder <code>null</code> für transparent)
	 */
	public void setFillColor(final Color color) {
		fillColor=color;
		fireChanged();
	}

	/**
	 * Liefert die Deckkraft der Hintergrundfarbe.
	 * @return	Deckkraft der Hintergrundfarbe (Wert zwischen 0 und 1 jeweils einschließlich)
	 */
	public double getFillAlpha() {
		return fillAlpha;
	}

	/**
	 * Stellt die Deckkraft der Hintergrundfarbe ein.
	 * @param fillAlpha	Deckkraft der Hintergrundfarbe (Wert zwischen 0 und 1 jeweils einschließlich)
	 */
	public void setFillAlpha(double fillAlpha) {
		this.fillAlpha=Math.max(0,Math.min(1,fillAlpha));
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
		if (!(element instanceof ModelElementText)) return false;
		final ModelElementText otherText=(ModelElementText)element;

		if (!otherText.text.equals(text)) return false;
		if (!otherText.color.equals(color)) return false;
		if (otherText.fontFamily!=fontFamily) return false;
		if (textSize!=otherText.textSize) return false;
		if (bold!=otherText.bold) return false;
		if (italic!=otherText.italic) return false;
		if (interpretSymbols!=otherText.interpretSymbols) return false;
		if (interpretMarkdown!=otherText.interpretMarkdown) return false;
		if (interpretLaTeX!=otherText.interpretLaTeX) return false;
		if (textAlign!=otherText.textAlign) return false;
		if (fillColor!=otherText.fillColor) return false;
		if (fillAlpha!=otherText.fillAlpha) return false;
		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementText) {
			final ModelElementText copySource=(ModelElementText)element;
			text=copySource.text;
			fontFamily=copySource.fontFamily;
			textSize=copySource.textSize;
			bold=copySource.bold;
			italic=copySource.italic;
			interpretSymbols=copySource.interpretSymbols;
			interpretMarkdown=copySource.interpretMarkdown;
			interpretLaTeX=copySource.interpretLaTeX;
			color=copySource.color;
			textAlign=copySource.textAlign;
			fillColor=copySource.fillColor;
			fillAlpha=copySource.fillAlpha;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementText clone(final EditModel model, final ModelSurface surface) {
		final ModelElementText element=new ModelElementText(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Zu verwendender Text-Renderer
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private ModelElementTextRenderer textRenderer;

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

		/* Renderer vorbereiten */
		if (interpretMarkdown || interpretLaTeX) {
			if (!(textRenderer instanceof ModelElementTextRendererMarkDownLaTeX)) textRenderer=new ModelElementTextRendererMarkDownLaTeX();
			((ModelElementTextRendererMarkDownLaTeX)textRenderer).setRenderMode(interpretMarkdown,interpretLaTeX);
		} else {
			if (!(textRenderer instanceof ModelElementTextRendererPlain)) textRenderer=new ModelElementTextRendererPlain();
		}

		/* Daten in Renderer laden */
		textRenderer.setText(text,interpretSymbols);
		textRenderer.setBackgroundColor(fillColor,fillAlpha);
		textRenderer.setStyle(textSize,bold,italic,fontFamily.name,textAlign);
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
		textRenderer.draw(graphics,canvasX,canvasY,color);

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
		return Language.tr("Surface.Text.Name");
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
			new ModelElementTextDialog(owner,ModelElementText.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Text.XML.Root");
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
		for (String line: text.trim().split("\\n")) {
			/* if (line.trim().isEmpty()) continue; */
			sub=doc.createElement(Language.trPrimary("Surface.Text.XML.Text"));
			node.appendChild(sub);
			sub.setTextContent(line.trim());
		}

		/* Schriftart */
		if (fontFamily!=FontCache.defaultFamily) {
			sub=doc.createElement(Language.trPrimary("Surface.Text.XML.FontFamily"));
			node.appendChild(sub);
			sub.setTextContent(fontFamily.name);
		}

		/* Schriftgröße */
		sub=doc.createElement(Language.trPrimary("Surface.Text.XML.FontSize"));
		node.appendChild(sub);
		sub.setTextContent(""+textSize);
		if (bold) sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.Bold"),"1");
		if (italic) sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.Italic"),"1");
		sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.Symbols"),interpretSymbols?"1":"0");
		if (interpretMarkdown) sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.Markdown"),"1");
		if (interpretLaTeX) sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.LaTeX"),"1");

		/* Farbe */
		sub=doc.createElement(Language.trPrimary("Surface.Text.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));

		/* Ausrichtung */
		if (textAlign!=TextAlign.LEFT) {
			sub=doc.createElement(Language.trPrimary("Surface.Text.XML.TextAlign"));
			node.appendChild(sub);
			sub.setTextContent(textAlign.getName());
		}

		/* Hintergrund */
		if (fillColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.Text.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(fillColor));
			if (fillAlpha<1) sub.setAttribute(Language.trPrimary("Surface.Text.XML.BackgroundColor.Alpha"),NumberTools.formatSystemNumber(fillAlpha));
		}
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
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		/* Schriftart */
		if (Language.trAll("Surface.Text.XML.FontFamily",name)) {
			fontFamily=FontCache.getFontCache().getFamilyFromName(content);
			return null;
		}

		/* Schriftgröße */
		if (Language.trAll("Surface.Text.XML.FontSize",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			textSize=I;
			bold=Language.trAllAttribute("Surface.Text.XML.FontSize.Bold",node).equals("1");
			italic=Language.trAllAttribute("Surface.Text.XML.FontSize.Italic",node).equals("1");
			interpretSymbols=!Language.trAllAttribute("Surface.Text.XML.FontSize.Symbols",node).equals("0");
			interpretMarkdown=Language.trAllAttribute("Surface.Text.XML.FontSize.Markdown",node).equals("1");
			interpretLaTeX=Language.trAllAttribute("Surface.Text.XML.FontSize.LaTeX",node).equals("1");
			return null;
		}

		/* Farbe */
		if (Language.trAll("Surface.Text.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		/* Text */
		if (Language.trAll("Surface.Text.XML.Text",name)) {
			if (!lineLoaded) text="";
			lineLoaded=true;
			if (!text.isEmpty()) text+="\n";
			text+=content;
			return null;
		}

		/* Ausrichtung */
		if (Language.trAll("Surface.Text.XML.TextAlign",name)) {
			textAlign=TextAlign.fromName(content);
			return null;
		}

		/* Hintergrund */
		if (Language.trAll("Surface.Text.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			fillColor=color;
			final String alpha=Language.trAllAttribute("Surface.Text.XML.BackgroundColor.Alpha",node);
			if (!alpha.trim().isEmpty()) {
				final Double D=NumberTools.getDouble(alpha);
				if (D==null || D<0 || D>1) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Text.XML.BackgroundColor.Alpha"),name,node.getParentNode().getNodeName());
				fillAlpha=D;
			}
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementText";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLText(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawPlainText(p,text,fontSize,fontBold,fontItalic,color) {\n");

		sb.append("  context.font=\"\"+fontSize+\"px Verdana,Lucida,sans-serif\";\n");
		sb.append("  if (fontBold) context.font=\"bold \"+context.font;\n");
		sb.append("  if (fontItalic) context.font=\"italic \"+context.font;\n");
		sb.append("  context.textAlign=\"left\";\n");
		sb.append("  context.textBaseline=\"hanging\";\n");
		sb.append("  context.fillStyle=color;\n");
		sb.append("  var lines=text.split(\"\\n\");\n");
		sb.append("  for (var i=0;i<lines.length;i++) {\n");
		sb.append("    context.fillText(lines[i],p.x,p.y+fontSize*i);\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawPlainText",builder->getHTMLText(builder));

		final Point p=getPosition(true);
		final String s=HTMLOutputBuilder.encodeHTML(getText(),true).replace("\n","\\n");
		outputBuilder.outputBody.append("drawPlainText({x: "+p.x+", y: "+p.y+"},\""+s+"\","+textSize+","+(bold?"true":"false")+","+(italic?"true":"false")+",\""+HTMLOutputBuilder.colorToHTML(color)+"\");\n");
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
