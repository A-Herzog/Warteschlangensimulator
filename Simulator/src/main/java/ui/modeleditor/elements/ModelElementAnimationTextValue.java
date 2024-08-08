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
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import tools.DateTools;
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
 * Zeigt während der Animation das Ergebnis eines Rechnenausdrucks als Text an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationTextValue extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Standard-Textfarbe
	 * @see #color
	 */
	private static final Color DEFAULT_COLOR=Color.BLACK;

	/**
	 * Wie soll der Ausgabetext formatiert werden?
	 * @author Alexander Herzog
	 * @see ModelElementAnimationTextValue#getMode()
	 * @see ModelElementAnimationTextValue#setMode(ModeExpression)
	 */
	public enum ModeExpression {
		/**
		 * Berechnet einen Ausdruck und zeigt diesen als Zahlenwert an.
		 */
		MODE_EXPRESSION_NUMBER,

		/**
		 * Berechnet einen Ausdruck und zeigt diesen als Prozentwert an.
		 */
		MODE_EXPRESSION_PERCENT,

		/**
		 * Berechnet einen Ausdruck und zeigt diesen als Zeitangabe an.
		 */
		MODE_EXPRESSION_TIME,

		/**
		 * Zeigt die Tage, Stunden, Minuten und Sekunden seit Simulationsstart an.
		 */
		MODE_TIME,

		/**
		 * Zeigt einen Datumswert an, der sich relativ zum Simulationsstart ergibt.
		 */
		MODE_DATE
	}

	/**
	 *  Wie soll der Ausgabetext formatiert werden?
	 *  @see #getMode()
	 *  @see #setMode(ModeExpression)
	 *  @see ModeExpression
	 */
	private ModeExpression mode=ModeExpression.MODE_EXPRESSION_NUMBER;

	/**
	 * Rechenausdruck
	 * @see #getExpression()
	 * @see #setExpression(String)
	 */
	private String expression="123";

	/**
	 * Vor dem eigentlichen Ausdruck auszugebender Text
	 */
	private String preText="";

	/**
	 * Nach dem eigentlichen Ausdruck auszugebender Text
	 */
	private String postText="";

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden? (in {@link #preText} und {@link #postText})
	 * @see #isInterpretSymbols()
	 * @see #setInterpretSymbols(boolean)
	 */
	private boolean interpretSymbols;

	/**
	 * Soll Markdown interpretiert werden? (in {@link #preText} und {@link #postText})
	 * @see #isInterpretMarkdown()
	 * @see #setInterpretMarkdown(boolean)
	 */
	private boolean interpretMarkdown;

	/**
	 * Sollen LaTeX-Ausdrücke interpretiert werden? (in {@link #preText} und {@link #postText})
	 * @see #isInterpretLaTeX()
	 * @see #setInterpretLaTeX(boolean)
	 */
	private boolean interpretLaTeX;

	/**
	 * Anzahl an anzuzeigenden Nachkommastellen
	 * @see #getDigits()
	 * @see #setDigits(int)
	 */
	private int digits=1;

	/**
	 * Unix-Zeitstempel (in Sekunden) der dem Start der Simulation entspricht
	 * @see #getDateZero()
	 * @see #setDateZero(long)
	 */
	private long dateZero=0;

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #simTextValue},
	 * {@link #simTextValueLong} und {@link #simTextValueDouble} zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Während der Animation anzuzeigender Text
	 */
	private String simTextValue=null;

	/**
	 * Während der Animation anzuzeigende Uhrzeit
	 */
	private long simTextValueLong;

	/**
	 * Während der Animation anzuzeigendes Datum
	 */
	private double simTextValueDouble;

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
	 * Konstruktor der Klasse <code>ModelElementTextValue</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationTextValue(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(0,0),Shapes.ShapeType.SHAPE_RECTANGLE);
		useSizeOnCompare=false;
		interpretSymbols=true;
		interpretMarkdown=false;
		interpretLaTeX=false;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_TEXT_VALUE.getIcon();
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
		return Language.tr("Surface.AnimationText.Tooltip");
	}

	/**
	 * Liefert den aktuellen Anzeigemodus
	 * @return	Aktueller Anzeigemodus
	 * @see ModeExpression
	 */
	public ModeExpression getMode() {
		return mode;
	}

	/**
	 * Stellt den aktuellen Anzeigemodus ein
	 * @param mode	Neuer Anzeigemodus
	 * @see ModeExpression
	 */
	public void setMode(final ModeExpression mode) {
		this.mode=mode;
		fireChanged();
	}

	/**
	 * Liefert im Falle, dass Modus <code>MODE_EXPRESSION_NUMBER</code> oder
	 * <code>MODE_EXPRESSION_PERCENT</code> gewählt sind, den aktuellen Ausdruck.
	 * @return	Aktueller Ausdruck
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Stellt den Ausdruck ein, der im Falle von <code>MODE_EXPRESSION_NUMBER</code> oder
	 * <code>MODE_EXPRESSION_PERCENT</code> ausgerechnet werden soll.
	 * @param expression	Neuer Ausdruck
	 */
	public void setExpression(final String expression) {
		if (expression!=null) {
			this.expression=expression;
			fireChanged();
		}
	}

	/**
	 * Gibt an, wie viele Nachkommastellen angezeigt werden sollen.
	 * @return	Anzahl an anzuzeigenden Nachkommastellen
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * Stellt in, wie viele Nachkommastellen angezeigt werden sollen.
	 * @param digits	Anzahl an anzuzeigenden Nachkommastellen
	 */
	public void setDigits(final int digits) {
		if (digits>=0 && digits<=15) this.digits=digits;
	}

	/**
	 * Liefert den Unix-Zeitstempel (in Sekunden) der dem Start der Simulation entsprechen soll.
	 * @return	Unix-Zeitstempel (in Sekunden) der dem Start der Simulation entspricht
	 */
	public long getDateZero() {
		return Math.max(0,dateZero);
	}

	/**
	 * Stellt den Unix-Zeitstempel (in Sekunden) der dem Start der Simulation entsprechen soll ein.
	 * @param dateZero	Unix-Zeitstempel (in Sekunden) der dem Start der Simulation entspricht
	 */
	public void setDateZero(long dateZero) {
		this.dateZero=Math.max(0,dateZero);
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
	 * Liefert den vor dem eigentlichen Ausdruck auszugebenden Text (kann leer sein, ist aber nicht <code>null</code>).
	 * @return	Vor dem eigentlichen Ausdruck auszugebender Text
	 */
	public String getPreText() {
		return (preText==null)?"":preText;
	}

	/**
	 * Stellt den vor dem eigentlichen Ausdruck auszugebenden Text ein (darf <code>null</code> sein).
	 * @param preText	Vor dem eigentlichen Ausdruck auszugebender Text
	 */
	public void setPreText(final String preText) {
		this.preText=(preText==null)?"":preText;
	}

	/**
	 * Liefert den nach dem eigentlichen Ausdruck auszugebenden Text (kann leer sein, ist aber nicht <code>null</code>).
	 * @return	Nach dem eigentlichen Ausdruck auszugebender Text
	 */
	public String getPostText() {
		return (postText==null)?"":postText;
	}

	/**
	 * Stellt den nach dem eigentlichen Ausdruck auszugebenden Text ein (darf <code>null</code> sein).
	 * @param postText	Nach dem eigentlichen Ausdruck auszugebender Text
	 */
	public void setPostText(final String postText) {
		this.postText=(postText==null)?"":postText;
	}

	/**
	 * Sollen HTML- und LaTeX-Symbole interpretiert werden?  (in {@link #preText} und {@link #postText})
	 * @return	HTML- und LaTeX-Symbole interpretieren
	 */
	public boolean isInterpretSymbols() {
		return interpretSymbols;
	}

	/**
	 * Stellt ein, ob HTML- und LaTeX-Symbole interpretiert werden sollen.  (in {@link #preText} und {@link #postText})
	 * @param interpretSymbols	HTML- und LaTeX-Symbole interpretier
	 */
	public void setInterpretSymbols(final boolean interpretSymbols) {
		this.interpretSymbols=interpretSymbols;
	}

	/**
	 * Soll Markdown interpretiert werden?  (in {@link #preText} und {@link #postText})
	 * @return	Markdown interpretieren
	 */
	public boolean isInterpretMarkdown() {
		return interpretMarkdown;
	}

	/**
	 * Stellt ein, ob Markdown interpretiert werden soll.  (in {@link #preText} und {@link #postText})
	 * @param interpretMarkdown	Markdown interpretieren
	 */
	public void setInterpretMarkdown(final boolean interpretMarkdown) {
		this.interpretMarkdown=interpretMarkdown;
	}

	/**
	 * Sollen LaTeX-Formatierungen interpretiert werden?  (in {@link #preText} und {@link #postText})
	 * @return	LaTeX-Formatierungen interpretieren
	 */
	public boolean isInterpretLaTeX() {
		return interpretLaTeX;
	}

	/**
	 * Stellt ein, ob LaTeX-Formatierungen interpretiert werden soll.  (in {@link #preText} und {@link #postText})
	 * @param interpretLaTeX	LaTeX-Formatierungen interpretieren
	 */
	public void setInterpretLaTeX(final boolean interpretLaTeX) {
		this.interpretLaTeX=interpretLaTeX;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationTextValue)) return false;
		final ModelElementAnimationTextValue otherText=(ModelElementAnimationTextValue)element;

		if (mode!=otherText.mode) return false;

		if (mode==ModeExpression.MODE_EXPRESSION_NUMBER || mode==ModeExpression.MODE_EXPRESSION_PERCENT || mode==ModeExpression.MODE_EXPRESSION_TIME) {
			if (!expression.equals(otherText.expression)) return false;
			if (digits!=otherText.digits) return false;
		}

		if (mode==ModeExpression.MODE_DATE) {
			if (dateZero!=otherText.dateZero) return false;
		}

		if (!otherText.color.equals(color)) return false;
		if (fontFamily!=otherText.fontFamily) return false;
		if (textSize!=otherText.textSize) return false;
		if (bold!=otherText.bold) return false;
		if (italic!=otherText.italic) return false;
		if (fillColor!=otherText.fillColor) return false;
		if (fillAlpha!=otherText.fillAlpha) return false;

		if (!Objects.equals(preText,otherText.preText)) return false;
		if (!Objects.equals(postText,otherText.postText)) return false;
		if (interpretSymbols!=otherText.interpretSymbols) return false;
		if (interpretMarkdown!=otherText.interpretMarkdown) return false;
		if (interpretLaTeX!=otherText.interpretLaTeX) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationTextValue) {
			final ModelElementAnimationTextValue copySource=(ModelElementAnimationTextValue)element;
			mode=copySource.mode;
			expression=copySource.expression;
			digits=copySource.digits;
			dateZero=copySource.dateZero;
			fontFamily=copySource.fontFamily;
			textSize=copySource.textSize;
			bold=copySource.bold;
			italic=copySource.italic;
			color=copySource.color;
			fillColor=copySource.fillColor;
			fillAlpha=copySource.fillAlpha;
			preText=copySource.preText;
			postText=copySource.postText;
			interpretSymbols=copySource.interpretSymbols;
			interpretMarkdown=copySource.interpretMarkdown;
			interpretLaTeX=copySource.interpretLaTeX;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationTextValue clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationTextValue element=new ModelElementAnimationTextValue(model,surface);
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
			if (surface==null) return Language.tr("Surface.AnimationText.Type.Property");
			else return getName();
		}

		if (surface==null) return Language.tr("Surface.AnimationText.Type.Value");
		drawLock.acquireUninterruptibly();
		try {
			if (simTextValue!=null) return simTextValue;
		} finally {
			drawLock.release();
		}
		switch (mode) {
		case MODE_EXPRESSION_NUMBER: return Language.tr("Surface.AnimationText.Type.Number");
		case MODE_EXPRESSION_PERCENT: return Language.tr("Surface.AnimationText.Type.PercentValue");
		case MODE_EXPRESSION_TIME: return Language.tr("Surface.AnimationText.Type.TimeValue");
		case MODE_TIME: return Language.tr("Surface.AnimationText.Type.SimulationTime");
		case MODE_DATE: return Language.tr("Surface.AnimationText.Type.Date");
		default: return Language.tr("Surface.AnimationText.Type.Error");
		}
	}

	/**
	 * Zu verwendender Titel-Renderer
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private ModelElementTextRenderer titleRenderer;

	/**
	 * Zu verwendender Text-Renderer für den voran gestellten Text
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private ModelElementTextRenderer preTextRenderer;

	/**
	 * Zu verwendender Text-Renderer für den eigentlichen Text
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private ModelElementTextRenderer mainTextRenderer;

	/**
	 * Zu verwendender Text-Renderer für den nachgestellten Text
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private ModelElementTextRenderer postTextRenderer;

	/**
	 * Bereitet die Renderer für Titel und Textbestandteile vor
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param zoom	Zoomfaktor
	 */
	private void initRenderer(final Graphics graphics, final double zoom) {
		/* === Renderer vorbereiten === */

		/* Titel */
		if (!(titleRenderer instanceof ModelElementTextRendererPlain)) titleRenderer=new ModelElementTextRendererPlain();

		/* Pretext */
		if (interpretMarkdown || interpretLaTeX) {
			if (!(preTextRenderer instanceof ModelElementTextRendererMarkDownLaTeX)) preTextRenderer=new ModelElementTextRendererMarkDownLaTeX();
			((ModelElementTextRendererMarkDownLaTeX)preTextRenderer).setRenderMode(interpretMarkdown,interpretLaTeX);
		} else {
			if (!(preTextRenderer instanceof ModelElementTextRendererPlain)) preTextRenderer=new ModelElementTextRendererPlain();
		}

		/* Main */
		if (interpretMarkdown || interpretLaTeX) {
			if (!(mainTextRenderer instanceof ModelElementTextRendererMarkDownLaTeX)) mainTextRenderer=new ModelElementTextRendererMarkDownLaTeX();
			((ModelElementTextRendererMarkDownLaTeX)mainTextRenderer).setRenderMode(interpretMarkdown,interpretLaTeX);
		} else {
			if (!(mainTextRenderer instanceof ModelElementTextRendererPlain)) mainTextRenderer=new ModelElementTextRendererPlain();
		}

		/* Posttext */
		if (interpretMarkdown || interpretLaTeX) {
			if (!(postTextRenderer instanceof ModelElementTextRendererMarkDownLaTeX)) postTextRenderer=new ModelElementTextRendererMarkDownLaTeX();
			((ModelElementTextRendererMarkDownLaTeX)postTextRenderer).setRenderMode(interpretMarkdown,interpretLaTeX);
		} else {
			if (!(postTextRenderer instanceof ModelElementTextRendererPlain)) postTextRenderer=new ModelElementTextRendererPlain();
		}

		/* === Daten in Renderer laden === */

		final String title=getDisplayText(true);
		final String preText=(this.preText==null)?"":this.preText;
		final String mainText=getDisplayText(false);
		final String postText=(this.postText==null)?"":this.postText;

		/* Titel */
		titleRenderer.setText(title,false);
		titleRenderer.setBackgroundColor(null,1.0);
		titleRenderer.setStyle(9,false,false,FontCache.defaultFamily.name,ModelElementText.TextAlign.LEFT);
		titleRenderer.calc(graphics,zoom);

		/* Pretext */
		if (!preText.isEmpty()) {
			preTextRenderer.setText(preText,interpretSymbols,false);
			preTextRenderer.setBackgroundColor(fillColor,fillAlpha);
			preTextRenderer.setStyle(textSize,bold,italic,fontFamily.name,ModelElementText.TextAlign.LEFT);
			preTextRenderer.calc(graphics,zoom);
		}

		/* Main */
		mainTextRenderer.setText(mainText,interpretSymbols);
		mainTextRenderer.setBackgroundColor(fillColor,fillAlpha);
		mainTextRenderer.setStyle(textSize,bold,italic,fontFamily.name,ModelElementText.TextAlign.LEFT);
		mainTextRenderer.calc(graphics,zoom);

		/* Posttext */
		if (!postText.isEmpty()) {
			postTextRenderer.setText(postText,interpretSymbols,false);
			postTextRenderer.setBackgroundColor(fillColor,fillAlpha);
			postTextRenderer.setStyle(textSize,bold,italic,fontFamily.name,ModelElementText.TextAlign.LEFT);
			postTextRenderer.calc(graphics,zoom);
		}
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

		/* Renderer vorbereiten und Texte in Renderer laden */
		initRenderer(graphics,zoom);

		/* Position und Größe berechnen */
		final Point pos=getPosition(true);
		final int canvasX=(zoom==1.0)?pos.x:(int)FastMath.round(pos.x*zoom);
		final int canvasY=(zoom==1.0)?pos.y:(int)FastMath.round(pos.y*zoom);
		final int canvasW=Math.max(titleRenderer.getWidth(),preTextRenderer.getWidth()+mainTextRenderer.getWidth()+postTextRenderer.getWidth());
		final int canvasH=(titleRenderer.isEmpty()?0:titleRenderer.getHeight())+Math.max(mainTextRenderer.getHeight(),Math.max(preTextRenderer.getHeight(),postTextRenderer.getHeight()));

		/* Wenn nötig Größe der Box anpassen */
		final int boxW=(zoom==1.0)?canvasW:(int)FastMath.round(canvasW/zoom);
		final int boxH=(zoom==1.0)?canvasH:(int)FastMath.round(canvasH/zoom);
		final Dimension boxSize=getSize();
		if (boxSize.width!=boxW || boxSize.height!=boxH) setSize(new Dimension(boxW,boxH));

		/* Text ausgeben */
		int x=canvasX;
		int y=canvasY;
		if (!titleRenderer.isEmpty()) {
			titleRenderer.draw(graphics,x,y,FlatLaFHelper.isDark()?EditModel.BLACK_COLOR_IN_DARK_MODE:Color.BLACK);
			y+=titleRenderer.getHeight();
		}
		if (!preTextRenderer.isEmpty()) {
			preTextRenderer.draw(graphics,x,y,color);
			x+=preTextRenderer.getWidth();
		}
		mainTextRenderer.draw(graphics,x,y,color);
		x+=mainTextRenderer.getWidth();
		if (!postTextRenderer.isEmpty()) {
			postTextRenderer.draw(graphics,x,y,color);
		}

		/* Rahmen zeichnen */
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
		return Language.tr("Surface.AnimationText.Name");
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
			new ModelElementAnimationTextValueDialog(owner,ModelElementAnimationTextValue.this,readOnly?ModelElementBaseDialog.ReadOnlyMode.FULL_READ_ONLY:ModelElementBaseDialog.ReadOnlyMode.ALLOW_ALL);
		};
	}

	@Override
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationTextValueDialog(owner,ModelElementAnimationTextValue.this,ModelElementBaseDialog.ReadOnlyMode.ALLOW_CONTENT_DATA_EDIT);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationText.XML.Root");
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
			sub=doc.createElement(Language.trPrimary("Surface.AnimationText.XML.FontFamily"));
			node.appendChild(sub);
			sub.setTextContent(fontFamily.name);
		}

		/* Schriftgröße, fett/kursiv */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationText.XML.FontSize"));
		node.appendChild(sub);
		sub.setTextContent(""+textSize);
		if (bold) sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.FontSize.Bold"),"1");
		if (italic) sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.FontSize.Italic"),"1");

		/* Textfarbe */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationText.XML.Color"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(color));

		/* Ausgabemodus */
		sub=doc.createElement(Language.trPrimary("Surface.AnimationText.XML.Mode"));
		node.appendChild(sub);

		switch (mode) {
		case MODE_EXPRESSION_NUMBER:
			sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.Mode.Type"),Language.trPrimary("Surface.AnimationText.XML.Mode.Type.Number"));
			break;
		case MODE_EXPRESSION_PERCENT:
			sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.Mode.Type"),Language.trPrimary("Surface.AnimationText.XML.Mode.Type.Percent"));
			break;
		case MODE_EXPRESSION_TIME:
			sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.Mode.Type"),Language.trPrimary("Surface.AnimationText.XML.Mode.Type.TimeValue"));
			break;
		case MODE_TIME:
			sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.Mode.Type"),Language.trPrimary("Surface.AnimationText.XML.Mode.Type.Time"));
			break;
		case MODE_DATE:
			sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.Mode.Type"),Language.trPrimary("Surface.AnimationText.XML.Mode.Type.Date"));
			break;
		}
		if (mode==ModeExpression.MODE_EXPRESSION_NUMBER || mode==ModeExpression.MODE_EXPRESSION_PERCENT || mode==ModeExpression.MODE_EXPRESSION_TIME) {
			sub.setTextContent(expression);
			if (digits!=1) sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.Digits"),""+digits);
		}
		if (mode==ModeExpression.MODE_DATE) {
			sub.setTextContent(NumberTools.formatLongNoGrouping(dateZero));
		}

		/* Hintergrund */
		if (fillColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationText.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(fillColor));
			if (fillAlpha<1) sub.setAttribute(Language.trPrimary("Surface.AnimationText.XML.BackgroundColor.Alpha"),NumberTools.formatSystemNumber(fillAlpha));
		}

		/* Pre- & Posttext */
		if ((preText!=null && !preText.isBlank()) || (postText!=null && !postText.isBlank()) || !interpretSymbols || interpretMarkdown || interpretLaTeX) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationText.XML.AdditionalText"));
			node.appendChild(sub);
			if (!preText.isEmpty()) sub.setAttribute(Language.tr("Surface.AnimationText.XML.AdditionalText.PreText"),preText);
			if (!postText.isEmpty()) sub.setAttribute(Language.tr("Surface.AnimationText.XML.AdditionalText.PostText"),postText);
			sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.Symbols"),interpretSymbols?"1":"0");
			if (interpretMarkdown) sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.Markdown"),"1");
			if (interpretLaTeX) sub.setAttribute(Language.trPrimary("Surface.Text.XML.FontSize.LaTeX"),"1");
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

		/* Schriftgröße, fett/kursiv */
		if (Language.trAll("Surface.AnimationText.XML.FontSize",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			textSize=I;
			bold=(Language.trAllAttribute("Surface.AnimationText.XML.FontSize.Bold",node).equals("1"));
			italic=(Language.trAllAttribute("Surface.AnimationText.XML.FontSize.Italic",node).equals("1"));
			return null;
		}

		/* Schriftart */
		if (Language.trAll("Surface.AnimationText.XML.FontFamily",name)) {
			fontFamily=FontCache.getFontCache().getFamilyFromName(content);
			return null;
		}

		/* Textfarbe */
		if (Language.trAll("Surface.AnimationText.XML.Color",name) && !content.trim().isEmpty()) {
			color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		/* Ausgabemodus */
		if (Language.trAll("Surface.AnimationText.XML.Mode",name)) {
			final String art=Language.trAllAttribute("Surface.AnimationText.XML.Mode.Type",node);
			mode=null;
			if (Language.trAll("Surface.AnimationText.XML.Mode.Type.Number",art)) mode=ModeExpression.MODE_EXPRESSION_NUMBER;
			if (Language.trAll("Surface.AnimationText.XML.Mode.Type.Percent",art)) mode=ModeExpression.MODE_EXPRESSION_PERCENT;
			if (Language.trAll("Surface.AnimationText.XML.Mode.Type.TimeValue",art)) mode=ModeExpression.MODE_EXPRESSION_TIME;
			if (Language.trAll("Surface.AnimationText.XML.Mode.Type.Time",art)) mode=ModeExpression.MODE_TIME;
			if (Language.trAll("Surface.AnimationText.XML.Mode.Type.Date",art)) mode=ModeExpression.MODE_DATE;
			if (mode==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationText.XML.Mode.Type"),name,node.getParentNode().getNodeName());
			if (mode==ModeExpression.MODE_EXPRESSION_NUMBER || mode==ModeExpression.MODE_EXPRESSION_PERCENT || mode==ModeExpression.MODE_EXPRESSION_TIME) {
				expression=content;
				final String digitsText=Language.trAllAttribute("Surface.AnimationText.XML.Digits",node);
				if (!digitsText.isEmpty()) {
					final Integer I=NumberTools.getNotNegativeInteger(digitsText);
					if (I==null || I.intValue()>15) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationText.XML.Digits"),name,node.getParentNode().getNodeName());
					digits=I.intValue();
				}
			}
			if (mode==ModeExpression.MODE_DATE) {
				final Long L=NumberTools.getNotNegativeLong(content);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationText.XML.Digits"),name,node.getParentNode().getNodeName());
				dateZero=L.longValue();
			}
			return null;
		}

		/* Hintergrund */
		if (Language.trAll("Surface.AnimationText.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			fillColor=color;
			final String alpha=Language.trAllAttribute("Surface.AnimationText.XML.BackgroundColor.Alpha",node);
			if (!alpha.trim().isEmpty()) {
				final Double D=NumberTools.getDouble(alpha);
				if (D==null || D<0 || D>1) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationText.XML.BackgroundColor.Alpha"),name,node.getParentNode().getNodeName());
				fillAlpha=D;
			}
			return null;
		}

		/* Pre- & Posttext */
		if (Language.trAll("Surface.AnimationText.XML.AdditionalText",name)) {
			preText=Language.trAllAttribute("Surface.AnimationText.XML.AdditionalText.PreText",node);
			postText=Language.trAllAttribute("Surface.AnimationText.XML.AdditionalText.PostText",node);
			interpretSymbols=!Language.trAllAttribute("Surface.Text.XML.FontSize.Symbols",node).equals("0");
			interpretMarkdown=Language.trAllAttribute("Surface.Text.XML.FontSize.Markdown",node).equals("1");
			interpretLaTeX=Language.trAllAttribute("Surface.Text.XML.FontSize.LaTeX",node).equals("1");
			return null;
		}

		return null;
	}

	/**
	 * Rechenausdruck der während der Animation ausgewertet
	 * werden soll, um den darzustellenden Wert zu erhalten.
	 * @see #initAnimation(SimulationData)
	 * @see #calcExpression(SimulationData)
	 */
	private ExpressionCalc animationExpression;

	/**
	 * Wertet {@link #animationExpression} aus und liefert
	 * den zu zeichnenden Wert zurück.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Darzustellender Wert
	 */
	private double calcExpression(final SimulationData simData) {
		if (animationExpression==null) return 0.0;
		simData.runData.setClientVariableValues(null);
		return animationExpression.calcOrDefault(simData.runData.variableValues,simData,null,0);
	}

	/**
	 * Temporärer {@link StringBuilder} zum
	 * Umwandeln von Zahlen in Zeichenketten
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private StringBuilder animationSB;

	/**
	 * Unterschiede zwischen angezeigten und aktuellem Wert,
	 * die in Abhängigkeit von der Anzahl an Nachkommastellen
	 * kein Neuzeichnen erfordern.
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private static final double[] maxDelta=new double[] {
			FastMath.pow(10,0),
			FastMath.pow(10,-1),
			FastMath.pow(10,-2),
			FastMath.pow(10,-3),
			FastMath.pow(10,-4),
			FastMath.pow(10,-5),
			FastMath.pow(10,-6),
			FastMath.pow(10,-7),
			FastMath.pow(10,-8),
			FastMath.pow(10,-9),
			FastMath.pow(10,-10),
			FastMath.pow(10,-11),
			FastMath.pow(10,-12),
			FastMath.pow(10,-13),
			FastMath.pow(10,-14),
			FastMath.pow(10,-15),
			FastMath.pow(10,-16),
			FastMath.pow(10,-17),
			FastMath.pow(10,-18),
			FastMath.pow(10,-19),
			FastMath.pow(10,-20)
	};

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		String s=null;
		double d;
		long l;
		switch (mode) {
		case MODE_EXPRESSION_NUMBER:
			if (isPreview) return false;
			d=calcExpression(simData);
			if (simTextValue!=null && Math.abs(simTextValueDouble-d)<maxDelta[digits+1]) return false;
			if (animationSB==null) animationSB=new StringBuilder();
			s=NumberTools.formatNumber(d,digits,animationSB);
			simTextValueDouble=d;
			break;
		case MODE_EXPRESSION_PERCENT:
			if (isPreview) return false;
			d=calcExpression(simData);
			if (simTextValue!=null && Math.abs(simTextValueDouble-d)<maxDelta[digits+4]) return false;
			if (animationSB==null) animationSB=new StringBuilder();
			s=NumberTools.formatPercent(d,digits,animationSB);
			simTextValueDouble=d;
			break;
		case MODE_EXPRESSION_TIME:
			d=calcExpression(simData);
			if (simTextValue!=null && Math.abs(simTextValueDouble-d)<0.5) return false;
			s=TimeTools.formatLongTime(Math.floor(d));
			simTextValueDouble=d;
			break;
		case MODE_TIME:
			l=(long)Math.floor(simData.currentTime*simData.runModel.scaleToSeconds);
			if (simTextValue!=null && simTextValueLong==l) return false;
			s=TimeTools.formatLongTime(l);
			simTextValueLong=l;
			break;
		case MODE_DATE:
			l=(long)Math.floor(simData.currentTime*simData.runModel.scaleToSeconds);
			if (simTextValue!=null && simTextValueLong==l) return false;
			s=DateTools.formatUserDate((l+dateZero)*1000);
			simTextValueLong=l;
			break;
		default:
			return false;
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
		simTextValue=null;

		animationExpression=new ExpressionCalc(simData.runModel.variableNames,simData.runModel.modelUserFunctions);
		if (animationExpression.parse(expression)>=0) animationExpression=null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationText";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLText(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationTextValue(p,labelText,text,fontSize,fontBold,fontItalic,color) {\n");

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
		outputBuilder.addJSUserFunction("drawAnimationTextValue",builder->getHTMLText(builder));

		final Point p=getPosition(true);
		final String text=HTMLOutputBuilder.encodeHTML(getDisplayText(false),true).replace("\n","\\n");
		final String title=HTMLOutputBuilder.encodeHTML(getDisplayText(true),true).replace("\n","\\n");

		outputBuilder.outputBody.append("drawAnimationTextValue({x: "+p.x+", y: "+p.y+"},\""+title+"\",\""+text+"\","+textSize+","+(bold?"true":"false")+","+(italic?"true":"false")+",\""+HTMLOutputBuilder.colorToHTML(color)+"\");\n");
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

		if (mode==ModeExpression.MODE_EXPRESSION_NUMBER || mode==ModeExpression.MODE_EXPRESSION_PERCENT || mode==ModeExpression.MODE_EXPRESSION_TIME) {
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputExpression"),expression,newExpression->{expression=newExpression;});
		}
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.FontSize"),textSize,newFontSize->{if (newFontSize>0) textSize=newFontSize;});
	}
}