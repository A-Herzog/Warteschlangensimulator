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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionEval;
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
 * Zeigt während der Animation einen Text, der auf Basis von verschiedenen
 * Bedingungen-Rechenausdrücken ausgewählt wird, an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationTextSelect extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Standard-Textfarbe
	 * @see #color
	 */
	private static final Color DEFAULT_COLOR=Color.BLACK;

	/**
	 * Standardtext, der angezeigt wird, wenn keine der Bedingungen erfüllt ist
	 * @see #getDefaultText()
	 * @see #setDefaultText(String)
	 */
	private String textDefault="";

	/**
	 * Ausdrücke zur Bestimmung des anzuzeigenden Textes
	 * @see #getTextExpressions()
	 */
	private List<String> textExpressions=new ArrayList<>();

	/**
	 * Texte, die gemäß der Ausdrücke angezeigt werden sollen
	 * @see #getTextValues()
	 */
	private List<String> textValues=new ArrayList<>();

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #simTextValue}
	 * zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Während der Animation auszugebender Text
	 */
	private String simTextValue=null;

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
	 * Ausgabe des Textes im Fettdruck?
	 * @see #getTextBold()
	 * @see #setTextBold(boolean)
	 */
	private boolean bold;

	/**
	 * Ausgabe des Textes im Kursivdruck?
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
	 * Textfarbe
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color=DEFAULT_COLOR;

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
	 * Konstruktor der Klasse <code>ModelElementTextSelect</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationTextSelect(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		interpretSymbols=true;
		useSizeOnCompare=false;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_TEXT_SELECT.getIcon();
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
		return Language.tr("Surface.AnimationTextSelect.Tooltip");
	}

	/**
	 * Liefert den Standardtext, der angezeigt wird, wenn keine der Bedingungen erfüllt ist.
	 * @return	Standardtext
	 */
	public String getDefaultText() {
		return textDefault;
	}

	/**
	 * Stellt den Standardtext ein, der angezeigt wird, wenn keine der Bedingungen erfüllt ist.
	 * @param text	Standardtext
	 */
	public void setDefaultText(final String text) {
		if (text!=null) textDefault=text;
	}

	/**
	 * Liefert die Ausdrücke, gemäß derer die Texte angezeigt werden sollen
	 * @return	Ausdrücke zur Bestimmung des anzuzeigenden Textes
	 */
	public List<String> getTextExpressions() {
		return textExpressions;
	}

	/**
	 * Liefert die Text, die gemäß der Ausdrücke angezeigt werden sollen
	 * @return	Texte, die gemäß der Ausdrücke angezeigt werden sollen
	 */
	public List<String> getTextValues() {
		return textValues;
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
		this.textSize=FastMath.max(6,FastMath.min(128,textSize));
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
		if (color!=null) this.color=color;
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
		if (!(element instanceof ModelElementAnimationTextSelect)) return false;
		final ModelElementAnimationTextSelect otherText=(ModelElementAnimationTextSelect)element;

		if (!textDefault.equals(otherText.textDefault)) return false;

		if (textExpressions.size()!=otherText.textExpressions.size()) return true;
		for (int i=0;i<textExpressions.size();i++) if (!textExpressions.get(i).equals(otherText.textExpressions.get(i))) return false;

		if (textValues.size()!=otherText.textValues.size()) return true;
		for (int i=0;i<textValues.size();i++) if (!textValues.get(i).equals(otherText.textValues.get(i))) return false;

		if (!otherText.color.equals(color)) return false;
		if (fontFamily!=otherText.fontFamily) return false;
		if (textSize!=otherText.textSize) return false;
		if (bold!=otherText.bold) return false;
		if (italic!=otherText.italic) return false;
		if (interpretSymbols!=otherText.interpretSymbols) return false;
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
		if (element instanceof ModelElementAnimationTextSelect) {
			final ModelElementAnimationTextSelect copySource=(ModelElementAnimationTextSelect)element;

			textDefault=copySource.textDefault;
			textExpressions.clear();
			textExpressions.addAll(copySource.textExpressions);
			textValues.clear();
			textValues.addAll(copySource.textValues);
			fontFamily=copySource.fontFamily;
			textSize=copySource.textSize;
			bold=copySource.bold;
			italic=copySource.italic;
			interpretSymbols=copySource.interpretSymbols;
			color=copySource.color;
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
	public ModelElementAnimationTextSelect clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationTextSelect element=new ModelElementAnimationTextSelect(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Bestimmt den anzuzeigenden Text oder die Überschrift.
	 * @param getTitle	Text (<code>false</code>) oder Überschrift (<code>true</code>) liefern
	 * @return	Anzuzeigender Text oder Überschrift
	 */
	private String getDisplayText(final boolean getTitle) {
		if (getTitle) {
			if (surface==null) return Language.tr("Surface.AnimationTextSelect.TemplateMode.Title");
			else return getName();
		}

		if (surface==null) return Language.tr("Surface.AnimationTextSelect.TemplateMode.Value");
		drawLock.acquireUninterruptibly();
		try {
			if (simTextValue!=null) return simTextValue;
		} finally {
			drawLock.release();
		}
		return Language.tr("Surface.AnimationTextSelect.TemplateMode.Value");
	}

	/**
	 * Schriftgröße beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private int lastTextSize=-1;

	/**
	 * Zoomfaktor beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private double lastZoomFont=-1;

	/**
	 * Schriftausgestaltung (fett, kursiv) beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private double lastStyleFont=-1;

	/**
	 * Schriftart beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private FontCache.FontFamily lastFamily=null;

	/**
	 * In {@link #drawToGraphics(Graphics, Rectangle, double, boolean)} generierte
	 * Schriftart für den Text
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastFontMain;

	/**
	 * In {@link #drawToGraphics(Graphics, Rectangle, double, boolean)} generierte
	 * Schriftart für den Titel
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastFontTitle;

	/**
	 * Farbe für den Titel über dem eigentlichen Text
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Color titleColor;

	/**
	 * Füllfarbe beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #lastFillAlpha
	 * @see #lastComputedFillColor
	 */
	private Color lastFillColor=null;

	/**
	 * Deckkraft beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #lastFillColor
	 * @see #lastComputedFillColor
	 */
	private double lastFillAlpha=0.0;

	/**
	 * Berechnete Füllfarbe beim letzten Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * @see #lastFillColor
	 * @see #lastFillAlpha
	 */
	private Color lastComputedFillColor=null;

	/**
	 * Ermöglicht die Interpretation von HTML-Entities und LaTeX-Symbolen
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 * @see TextTransformer
	 */
	private TextTransformer textTransformer;

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		final String text;
		if (interpretSymbols) {
			if (textTransformer==null) textTransformer=new TextTransformer();
			text=textTransformer.process(getDisplayText(false));
		} else {
			text=getDisplayText(false);
		}
		final String title=getDisplayText(true);

		int style=Font.PLAIN;
		if (bold) style+=Font.BOLD;
		if (italic) style+=Font.ITALIC;
		if (lastFamily!=fontFamily || textSize!=lastTextSize || zoom!=lastZoomFont || style!=lastStyleFont || lastFontMain==null || lastFontTitle==null) {
			lastFontMain=FontCache.getFontCache().getFont(fontFamily,style,(int)FastMath.round(textSize*zoom));
			lastFontTitle=FontCache.getFontCache().getFont(FontCache.defaultFamily,0,(int)FastMath.round(9*zoom));
			lastFamily=fontFamily;
			lastTextSize=textSize;
			lastZoomFont=zoom;
			lastStyleFont=style;
		}

		if (fillColor!=lastFillColor || fillAlpha!=lastFillAlpha) {
			if (fillColor==null) {
				lastComputedFillColor=null;
			} else {
				lastComputedFillColor=new Color(fillColor.getRed(),fillColor.getGreen(),fillColor.getBlue(),Math.max(0,Math.min(255,((int)Math.round(255*fillAlpha)))));
			}
			lastFillColor=fillColor;
			lastFillAlpha=fillAlpha;
		}

		int width;
		int height;
		if (title.trim().isEmpty()) {
			graphics.setFont(lastFontMain);
			width=graphics.getFontMetrics().stringWidth(text);
			height=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
		} else {
			graphics.setFont(lastFontTitle);
			width=graphics.getFontMetrics().stringWidth(title);
			height=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
			graphics.setFont(lastFontMain);
			width=FastMath.max(width,graphics.getFontMetrics().stringWidth(text));
			height+=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
		}
		final Point point=getPosition(true);

		int w=(int)FastMath.round(width/zoom);
		int h=(int)FastMath.round(height/zoom);
		if (getSize().width!=w || getSize().height!=h) setSize(new Dimension(w,h));

		setClip(graphics,drawRect,null);

		if (lastComputedFillColor!=null) {
			graphics.setColor(lastComputedFillColor);
			graphics.fillRect(point.x,point.y,w,h);
			graphics.setColor(color);
		}

		int x=(int)FastMath.round(point.x*zoom);
		if (title.trim().isEmpty()) {
			graphics.setColor(color);
			graphics.setFont(lastFontMain);
			int y=(int)FastMath.round(point.y*zoom)+graphics.getFontMetrics().getAscent();
			graphics.drawString(text,x,y);
		} else {
			if (titleColor==null) titleColor=FlatLaFHelper.isDark()?EditModel.BLACK_COLOR_IN_DARK_MODE:Color.BLACK;
			graphics.setColor(titleColor);
			graphics.setFont(lastFontTitle);
			int y=(int)FastMath.round(point.y*zoom)+graphics.getFontMetrics().getAscent();
			graphics.drawString(title,x,y);
			y+=graphics.getFontMetrics().getDescent();

			graphics.setColor(color);
			graphics.setFont(lastFontMain);
			y+=graphics.getFontMetrics().getAscent();
			graphics.drawString(text,x,y);
		}

		if (isSelected() && showSelectionFrames) {
			drawRect(graphics,drawRect,zoom,Color.GREEN,2,null,2);
		} else {
			if (isSelectedArea() && showSelectionFrames) drawRect(graphics,drawRect,zoom,Color.BLUE,2,null,2);
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationTextSelect.Name");
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
			new ModelElementAnimationTextSelectDialog(owner,ModelElementAnimationTextSelect.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationTextSelectDialog(owner,ModelElementAnimationTextSelect.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationTextSelect.XML.Root");
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

		/* Schriftart */
		if (fontFamily!=FontCache.defaultFamily) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTextSelect.XML.FontFamily"));
			node.appendChild(sub);
			sub.setTextContent(fontFamily.name);
		}

		/* Schriftgröße */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationTextSelect.XML.FontSize"));
		node.appendChild(sub);
		sub.setTextContent(""+textSize);
		if (bold) sub.setAttribute(Language.trPrimary("Surface.AnimationTextSelect.XML.FontSize.Bold"),"1");
		if (italic) sub.setAttribute(Language.trPrimary("Surface.AnimationTextSelect.XML.FontSize.Italic"),"1");
		sub.setAttribute(Language.trPrimary("Surface.AnimationTextSelect.XML.FontSize.Symbols"),interpretSymbols?"1":"0");

		/* Farbe */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationTextSelect.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));

		/* Standardtext */
		if (textDefault!=null && !textDefault.trim().isEmpty()) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTextSelect.XML.TextDefault"));
			node.appendChild(sub);
			sub.setTextContent(textDefault);
		}

		/* Bedingungsabhängige Texte */
		int size=Math.min(textExpressions.size(),textValues.size());
		for (int i=0;i<size;i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTextSelect.XML.TextExpression"));
			node.appendChild(sub);
			sub.setAttribute(Language.trPrimary("Surface.AnimationTextSelect.XML.TextExpression.Expression"),textExpressions.get(i));
			sub.setTextContent(textValues.get(i));
		}

		/* Hintergrund */
		if (fillColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationTextSelect.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(fillColor));
			if (fillAlpha<1) sub.setAttribute(Language.trPrimary("Surface.AnimationTextSelect.XML.BackgroundColor.Alpha"),NumberTools.formatSystemNumber(fillAlpha));
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

		/* Schriftart */
		if (Language.trAll("Surface.AnimationTextSelect.XML.FontFamily",name)) {
			fontFamily=FontCache.getFontCache().getFamilyFromName(content);
			return null;
		}

		/* Schriftgröße */
		if (Language.trAll("Surface.AnimationTextSelect.XML.FontSize",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			textSize=I;
			bold=(Language.trAllAttribute("Surface.AnimationTextSelect.XML.FontSize.Bold",node).equals("1"));
			italic=(Language.trAllAttribute("Surface.AnimationTextSelect.XML.FontSize.Italic",node).equals("1"));
			interpretSymbols=!Language.trAllAttribute("Surface.AnimationTextSelect.XML.FontSize.Symbols",node).equals("0");
			return null;
		}

		/* Farbe */
		if (Language.trAll("Surface.AnimationTextSelect.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		/* Standardtext */
		if (Language.trAll("Surface.AnimationTextSelect.XML.TextDefault",name)) {
			textDefault=content;
			return null;
		}

		/* Bedingungsabhängige Texte */
		if (Language.trAll("Surface.AnimationTextSelect.XML.TextExpression",name)) {
			final String expr=Language.trAllAttribute("Surface.AnimationTextSelect.XML.TextExpression.Expression",node);
			if (!expr.trim().isEmpty()) {
				textExpressions.add(expr);
				textValues.add(content);
			}
			return null;
		}

		/* Hintergrund */
		if (Language.trAll("Surface.AnimationTextSelect.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			fillColor=color;
			final String alpha=Language.trAllAttribute("Surface.AnimationTextSelect.XML.BackgroundColor.Alpha",node);
			if (!alpha.trim().isEmpty()) {
				final Double D=NumberTools.getDouble(alpha);
				if (D==null || D<0 || D>1) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationTextSelect.XML.BackgroundColor.Alpha"),name,node.getParentNode().getNodeName());
				fillAlpha=D;
			}
			return null;
		}

		return null;
	}

	/**
	 * Formelobjekte zu {@link #textExpressions}
	 * @see #initAnimation(SimulationData)
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private ExpressionEval[] animationExpression;

	/**
	 * Animationstexte von {@link #textValues} abgeleitet
	 * @see #textValues
	 */
	private String[] animationText;

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (isPreview) return false;

		String s=null;

		if (animationExpression!=null) {
			boolean ok=false;
			simData.runData.setClientVariableValues(null);
			for (int i=0;i<animationExpression.length;i++) {
				if (animationExpression[i]!=null && animationExpression[i].eval(simData.runData.variableValues,simData,null)) {
					s=animationText[i];
					ok=true;
					break;
				}
			}
			if (!ok) s=textDefault;
		}

		drawLock.acquireUninterruptibly();
		try {
			simTextValue=s;
		} finally {
			drawLock.release();
		}

		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		final int size=Math.min(textExpressions.size(),textValues.size());
		animationExpression=new ExpressionEval[size];
		animationText=new String[size];
		for (int i=0;i<size;i++) {
			animationText[i]=textValues.get(i);
			animationExpression[i]=new ExpressionEval(simData.runModel.variableNames);
			if (animationExpression[i].parse(textExpressions.get(i))>=0) animationExpression[i]=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationTextSelect";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLText(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationTextSelect(p,labelText,text,fontSize,fontBold,fontItalic,color) {\n");

		sb.append("  context.textAlign=\"left\";\n");
		sb.append("  context.textBaseline=\"hanging\";\n");

		sb.append("  context.font=\"9px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.fillStyle=\"black\";\n");
		sb.append("  context.fillText(labelText,p.x,p.y);\n");

		sb.append("  context.font=\"\"+fontSize+\"px Verdana,Lucida,sans-serif\";\n");
		sb.append("  if (fontBold) context.font=\"bold \"+context.font;\n");
		sb.append("  if (fontItalic) context.font=\"italic \"+context.font;\n");
		sb.append("  context.fillStyle=color;\n");
		sb.append("  var lines=text.split(\"\\n\");\n");
		sb.append("  for (var i=0;i<lines.length;i++) {\n");
		sb.append("    context.fillText(lines[i],p.x,p.y+9+fontSize*i);\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationTextSelect",builder->getHTMLText(builder));

		final Point p=getPosition(true);
		final String text=HTMLOutputBuilder.encodeHTML(getDisplayText(false),true).replace("\n","\\n");
		final String title=HTMLOutputBuilder.encodeHTML(getDisplayText(true),true).replace("\n","\\n");

		outputBuilder.outputBody.append("drawAnimationTextSelect({x: "+p.x+", y: "+p.y+"},\""+title+"\",\""+text+"\","+textSize+","+(bold?"true":"false")+","+(italic?"true":"false")+",\""+HTMLOutputBuilder.colorToHTML(color)+"\");\n");
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

		for (int i=0;i<textExpressions.size();i++) {
			final int index=i;
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.Condition"),textExpressions.get(index),newCondition->textExpressions.set(index,newCondition));
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.ValueForCondition"),textExpressions.get(index)),textValues.get(index),newValue->textValues.set(index,newValue));
		}
		searcher.testString(this,Language.tr("Surface.AnimationTextSelect.Dialog.DefaultValue"),textDefault,newTextDefault->{textDefault=newTextDefault;});
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.FontSize"),textSize,newFontSize->{if (newFontSize>0) textSize=newFontSize;});
	}
}