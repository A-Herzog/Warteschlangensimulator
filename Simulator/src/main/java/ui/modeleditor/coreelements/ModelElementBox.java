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
package ui.modeleditor.coreelements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import tools.SetupData;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.AnimationExpression;
import ui.modeleditor.elements.ElementWithAnimationDisplay;
import ui.modeleditor.elements.FontCache;
import ui.modeleditor.elements.ModelElementAnimationBar;
import ui.modeleditor.elements.ModelElementAnimationBarChart;
import ui.modeleditor.elements.ModelElementAnimationRecord;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementAnimationTrafficLights;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementReference;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.speedup.BackgroundSystem;

/**
 * Basisklasse f�r alle Box-f�rmigen Modell-Elemente
 * @author Alexander Herzog
 */
public class ModelElementBox extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Benutzerdefinierte Hintergrundfarbe
	 * @see #getUserBackgroundColor()
	 * @see #setUserBackgroundColor(Color)
	 * @see #getDrawBackgroundColor()
	 */
	private Color userBackgroundColor;

	/**
	 * Standardschriftart f�r gro�e Texte in der Elementenbox
	 * @see #boxFontLarge
	 */
	public static Font DEFAULT_FONT_LARGE=new Font(Font.DIALOG,Font.BOLD,13);

	/**
	 * Standardschriftart f�r kleine Texte in der Elementenbox
	 * @see #boxFontSmall
	 */
	public static Font DEFAULT_FONT_SMALL=new Font(Font.DIALOG,Font.PLAIN,11);

	/**
	 * Standardm��ige Schriftart f�r alle m�glichen Beschriftungen
	 */
	public static String DEFAULT_FONT_TYPE=Font.DIALOG;

	/**
	 * Schriftart f�r gro�e Texte in der Elementenbox
	 * @see #getFontLarge()
	 * @see #setFontLarge(Font)
	 */
	private Font boxFontLarge=DEFAULT_FONT_LARGE;

	/**
	 * Schriftart f�r kleine Texte in der Elementenbox
	 * @see #getFontSmall()
	 * @see #setFontSmall(Font)
	 */
	private Font boxFontSmall=DEFAULT_FONT_SMALL;

	/**
	 * Referenz auf das Setup-Singleton.
	 */
	private final SetupData setup=SetupData.getSetup();

	/**
	 * Texte in die Stationsbox zeichnen?
	 * @see #isDrawText()
	 * @see #setDrawText(boolean)
	 */
	private boolean drawText=true;

	/**
	 * Vorgabewert f�r {@link #maxThroughputIntervalSeconds}
	 * @see #maxThroughputIntervalSeconds
	 */
	public static final int DEFAULT_MAX_THROUGHPUT_INTERVAL_SECONDS=3600;

	/**
	 * Intervalll�nge (gemessen in Sekunden) f�r die Erfassung des maximalen Durchsatzes an der Station<br>
	 * (Werte &le;0 f�r "aus")
	 * @see #getMaxThroughputIntervalSeconds()
	 * @see #setMaxThroughputIntervalSeconds(int)
	 */
	private int maxThroughputIntervalSeconds=DEFAULT_MAX_THROUGHPUT_INTERVAL_SECONDS;

	/**
	 * Konstruktor der Klasse <code>ModelElementBox</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param shape	Darzustellende Form ({@link Shapes})
	 */
	public ModelElementBox(final EditModel model, final ModelSurface surface, final Shapes.ShapeType shape) {
		super(model,surface,new Dimension(100,50),shape);
	}

	/**
	 * Gibt an, ob Texte in die Stationsbox gezeichnet werden sollen
	 * @return	Texte in die Stationsbox zeichnen
	 */
	protected boolean isDrawText() {
		return drawText;
	}

	/**
	 * Stellt ein, ob Texte in die Stationsbox gezeichnet werden sollen
	 * @param drawText	Texte in die Stationsbox zeichnen
	 */
	protected void setDrawText(final boolean drawText) {
		this.drawText=drawText;
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmen� angezeigt werden soll.<br>
	 * Generiert im Falle eines Elements mit eigener Position (= einem normal darstellbaren Element) ein Icon basierend auf der Darstellung des Elements selber.
	 * @return	Icon zur Beschriftung des Elements im Kontextmen� oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	@Override
	public Icon buildIcon() {
		final Dimension size=getSize();
		final BufferedImage image=new BufferedImage(size.width+15,size.height+15,BufferedImage.TYPE_4BYTE_ABGR);

		temporaryMoveToTop();
		try {
			final boolean saveShowIDs=setup.showIDs;
			try {
				setup.showIDs=false;
				drawToGraphics(image.getGraphics(),new Rectangle(0,0,size.width+15,size.height+15),1.0,false);
			} finally {
				setup.showIDs=saveShowIDs;
			}
		} finally {
			temporaryMoveRestore();
		}

		return getScaledElementIcon(image);
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementBox)) return false;
		final ModelElementBox box=(ModelElementBox)element;

		if (userBackgroundColor!=null || box.userBackgroundColor!=null) {
			if (userBackgroundColor==null || box.userBackgroundColor==null) return false;
			if (!box.userBackgroundColor.equals(userBackgroundColor)) return false;
		}
		if (!box.boxFontLarge.equals(boxFontLarge)) return false;
		if (!box.boxFontSmall.equals(boxFontSmall)) return false;

		if (box.maxThroughputIntervalSeconds!=maxThroughputIntervalSeconds) return false;

		if (box.drawQueueAll!=drawQueueAll) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementBox) {
			final ModelElementBox box=(ModelElementBox)element;
			userBackgroundColor=box.userBackgroundColor;
			boxFontLarge=box.boxFontLarge;
			boxFontSmall=box.boxFontSmall;
			maxThroughputIntervalSeconds=box.maxThroughputIntervalSeconds;
			drawQueueAll=box.drawQueueAll;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementBox clone(final EditModel model, final ModelSurface surface) {
		final ModelElementBox element=new ModelElementBox(model,surface,shape.shapeType);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Box.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	public String getTypeName() {
		return Language.tr("Surface.Box.Name");
	}

	/**
	 * Liefert einen Infotext, der unter der Box angezeigt werden soll.<br>
	 * Es kann auch <code>null</code> zur�ckgegeben werden.
	 * @return	Optionale Infomeldung oder <code>null</code> wenn keine Meldung angezeigt werden soll.
	 */
	protected String getInfoMessage() {
		return null;
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zur�ckgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	protected String getErrorMessage() {
		return BackgroundSystem.checkModelElement(this);
	}

	/**
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	public String getSubTypeName() {
		return null;
	}

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	public Color getTypeDefaultBackgroundColor() {
		return Color.LIGHT_GRAY;
	}

	/**
	 * Hintergrundfarbe im Falle der Hochkontrast-Darstellung
	 * @see #getDrawBackgroundColor()
	 */
	private static final Color highContrastColor=new Color(255,255,240);

	/**
	 * Liefert die tats�chlich zum Zeichnen zu verwendende Farbe
	 * @return	Zu verwendende Farbe (entweder Vorgabefarbe oder benutzerdefinierte Farbe, aber nie <code>null</code>)
	 */
	public final Color getDrawBackgroundColor() {
		if (setup.useHighContrasts) return highContrastColor;

		if (userBackgroundColor!=null) return userBackgroundColor;
		return getTypeDefaultBackgroundColor();
	}

	/**
	 * Liefert die momentan eingestellte benutzerdefinierte Hintergrundfarbe
	 * @return	Benutzerdefinierte Hintergrundfarbe oder <code>null</code>, wenn die Vorgabefarbe verwendet werden soll.
	 */
	public Color getUserBackgroundColor() {
		return userBackgroundColor;
	}

	/**
	 * Stellt die Hintergrundfarbe ein
	 * @param userBackgroundColor	Hintergrundfarbe oder <code>null</code>, wenn die Vorgabefarbe verwendet werden soll
	 */
	public void setUserBackgroundColor(final Color userBackgroundColor) {
		this.userBackgroundColor=userBackgroundColor;
		fireChanged();
	}

	/**
	 * Liefert das momentan eingestellte benutzerdefiniertes Stationsbild
	 * @return	Benutzerdefiniertes Stationsbild oder <code>null</code>, wenn die Vorgabeform verwendet werden soll.
	 */
	public BufferedImage getUserBackgroundImage() {
		return shape.getCustomImage();
	}

	/**
	 * Stellt die ein benutzerdefiniertes Stationsbild ein
	 * @param customImage	Benutzerdefiniertes Stationsbild oder <code>null</code>, wenn die Vorgabeform verwendet werden soll.
	 */
	public void setUserBackgroundImage(final BufferedImage customImage) {
		shape.setCustomImage(customImage);
		fireChanged();
	}

	/**
	 * Liefert die gro�e Schriftart f�r die Elementenbox.
	 * @return	Gro�e Schriftart f�r die Elementenbox
	 */
	public Font getFontLarge() {
		return boxFontLarge;
	}

	/**
	 * Stellt die gro�e Schriftart f�r die Elementenbox ein.
	 * @param fontLarge	Gro�e Schriftart f�r die Elementenbox
	 */
	public void setFontLarge(final Font fontLarge) {
		if (fontLarge==null) return;
		this.boxFontLarge=fontLarge;
		fireChanged();
	}

	/**
	 * Liefert die kleine Schriftart f�r die Elementenbox.
	 * @return	Kleine Schriftart f�r die Elementenbox
	 */
	public Font getFontSmall() {
		return boxFontSmall;
	}

	/**
	 * Stellt die kleine Schriftart f�r die Elementenbox ein.
	 * @param fontSmall	Klein Schriftart f�r die Elementenbox
	 */
	public void setFontSmall(final Font fontSmall) {
		if (fontSmall==null) return;
		this.boxFontSmall=fontSmall;
		fireChanged();
	}

	/**
	 * Zoomfaktor auf den sich {@link #lastFontDefaultBox} und {@link #lastFontBoldBox} beziehen
	 * @see #lastFontDefaultBox
	 * @see #lastFontBoldBox
	 */
	private double lastZoomFontBox=-1;

	/**
	 * Kontrast-Status auf den sich {@link #lastFontDefaultBox} und {@link #lastFontBoldBox} beziehen
	 * @see #lastFontDefaultBox
	 * @see #lastFontBoldBox
	 */
	private boolean lastUseHighContrasts;

	/**
	 * Cache f�r die normale Schriftart
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastFontDefaultBox;

	/**
	 * Cache f�r eine etwas kleinere Version der normalen Schriftart
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastFontDefaultBoxSmaller;

	/**
	 * Cache f�r fette Schriftart
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastFontBoldBox;

	/**
	 * Schriftart f�r gro�e Schriften in der Box
	 * (um pr�fen zu k�nnen, ob die Schriften neu generiert werden m�ssen)
	 * @see #boxFontLarge
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastBoxFontLarge;

	/**
	 * Schriftart f�r kleine Schriften in der Box
	 * (um pr�fen zu k�nnen, ob die Schriften neu generiert werden m�ssen)
	 * @see #boxFontSmall
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private Font lastBoxFontSmall;

	/**
	 * Bezog sich der letzte Aufruf von {@link #drawToGraphics(Graphics, Rectangle, double, boolean)}
	 * auf einen Export als Vektorgrafik?
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private boolean lastWasExport=false;

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		Color borderColor;
		int borderWidth;
		if (isSelected() && showSelectionFrames) {
			borderColor=Color.GREEN;
			borderWidth=2;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				borderColor=Color.BLUE;
				borderWidth=2;
			} else {
				borderColor=Color.DARK_GRAY;
				borderWidth=1;
			}
		}

		final boolean isExport=(graphics instanceof SVGGraphics2D || graphics instanceof VectorGraphics2D);
		if (isExport || lastWasExport || lastZoomFontBox!=zoom || lastFontDefaultBox==null || lastFontBoldBox==null || lastUseHighContrasts!=setup.useHighContrasts || !boxFontLarge.equals(lastBoxFontLarge) || !boxFontSmall.equals(lastBoxFontSmall)) {
			String fontNameLarge=boxFontLarge.getName();
			String fontNameSmall=boxFontSmall.getName();
			if (isExport) {
				/*
				 * "Sans" und "Serif" werden von jedem SVG-Renderer anders umgesetzt,
				 * daher machen wir hier konkrete Vorgaben, so dass die Laufweite
				 * vorab korrekt berechnet werden kann und Texte und Sub- und Subskripte
				 * zusammen passen.
				 */
				if (fontNameLarge.equals(FontCache.FontFamily.DIALOG.name)) fontNameLarge=FontCache.FontFamily.WIN_VERDANA.name;
				if (fontNameLarge.equals(FontCache.FontFamily.SANS.name)) fontNameLarge=FontCache.FontFamily.WIN_VERDANA.name;
				if (fontNameLarge.equals(FontCache.FontFamily.SERIF.name)) fontNameLarge=FontCache.FontFamily.WIN_CAMBRIA.name;
				if (fontNameSmall.equals(FontCache.FontFamily.DIALOG.name)) fontNameSmall=FontCache.FontFamily.WIN_VERDANA.name;
				if (fontNameSmall.equals(FontCache.FontFamily.SANS.name)) fontNameSmall=FontCache.FontFamily.WIN_VERDANA.name;
				if (fontNameSmall.equals(FontCache.FontFamily.SERIF.name)) fontNameSmall=FontCache.FontFamily.WIN_CAMBRIA.name;
			}
			lastBoxFontLarge=new Font(fontNameLarge,boxFontLarge.getStyle(),boxFontLarge.getSize());
			lastBoxFontSmall=new Font(fontNameSmall,boxFontSmall.getStyle(),boxFontSmall.getSize());
			lastFontDefaultBox=new Font(fontNameSmall,setup.useHighContrasts?Font.BOLD:boxFontSmall.getStyle(),(int)FastMath.round(boxFontSmall.getSize()*zoom));
			lastFontDefaultBoxSmaller=new Font(fontNameSmall,setup.useHighContrasts?Font.BOLD:boxFontSmall.getStyle(),(int)FastMath.round(boxFontSmall.getSize()*zoom*0.85));
			lastFontBoldBox=new Font(fontNameLarge,boxFontLarge.getStyle(),(int)FastMath.round(boxFontLarge.getSize()*zoom));
			lastZoomFontBox=zoom;
			lastUseHighContrasts=setup.useHighContrasts;
		}
		lastWasExport=isExport;

		final Rectangle objectRect=drawRect(graphics,drawRect,zoom,borderColor,borderWidth,getDrawBackgroundColor(),1);
		if (objectRect==null) return;

		graphics.setColor(Color.BLACK);

		setClip(graphics,drawRect,objectRect);

		if (drawText && getUserBackgroundImage()==null) {

			int x,y;
			String textType=getTypeName();
			final String textName=getName();
			final boolean hasName=(textName!=null && !textName.isEmpty());

			int yStart;

			graphics.setFont(lastFontDefaultBox);
			FontMetrics metrics=graphics.getFontMetrics();

			if (textType!=null && !textType.isEmpty()) {
				final String subType=getSubTypeName();
				if (subType!=null && !subType.isEmpty()) {
					int w=metrics.stringWidth(textType);
					if (objectRect.width-2<w) {
						graphics.setFont(lastFontDefaultBoxSmaller);
						metrics=graphics.getFontMetrics();
						w=metrics.stringWidth(textType);
					}
					x=objectRect.x+(objectRect.width-w)/2;
					if (hasName) {
						y=objectRect.y+2*objectRect.height/3-2*metrics.getDescent()-graphics.getFontMetrics().getAscent();
					} else {
						y=objectRect.y+objectRect.height/2-metrics.getDescent();
					}
					graphics.drawString(textType,x,y);
					final String textSub="("+subType+")";
					x=objectRect.x+(objectRect.width-metrics.stringWidth(textSub))/2;
					if (hasName) {
						y=objectRect.y+2*objectRect.height/3-metrics.getDescent();
					} else {
						y=objectRect.y+objectRect.height/2+metrics.getAscent();
					}
					graphics.drawString(textSub,x,y);
					yStart=objectRect.y+2*objectRect.height/3;
				} else {
					int w=metrics.stringWidth(textType);
					if (objectRect.width-2<w) {
						graphics.setFont(lastFontDefaultBoxSmaller);
						metrics=graphics.getFontMetrics();
						w=metrics.stringWidth(textType);
					}
					x=objectRect.x+(objectRect.width-w)/2;
					if (hasName) {
						y=objectRect.y+objectRect.height/2-metrics.getDescent();
					} else {
						y=objectRect.y+objectRect.height/2+(metrics.getAscent()-metrics.getDescent())/2;
					}
					graphics.drawString(textType,x,y);
					yStart=objectRect.y+objectRect.height/2;
				}
			} else {
				yStart=objectRect.y+objectRect.height/2;
			}

			if (hasName) {
				graphics.setFont(lastFontBoldBox);
				metrics=graphics.getFontMetrics();
				x=objectRect.x+(objectRect.width-metrics.stringWidth(textName))/2;
				y=yStart+metrics.getAscent();
				graphics.drawString(textName,x,y);
			}
		}

		drawRect(graphics,drawRect,zoom,borderColor,borderWidth,getDrawBackgroundColor(),2);
	}

	/**
	 * Zoomfaktor auf den sich {@link #infoFont}, {@link #infoFontAscent}
	 * und {@link #infoFontHeight} beziehen.
	 * @see #infoFont
	 * @see #infoFontAscent
	 * @see #infoFontHeight
	 */
	private double infoFontZoom;

	/**
	 * Cache f�r die Schriftart in {@link #drawRect(Graphics, Rectangle, double, Color, int, Color, int)}
	 * @see #drawRect(Graphics, Rectangle, double, Color, int, Color, int)
	 */
	private Font infoFont;

	/**
	 * Berechnete H�he �ber Grundlinie f�r {@link #infoFont}
	 * @see #infoFont
	 * @see #drawRect(Graphics, Rectangle, double, Color, int, Color, int)
	 */
	private int infoFontAscent;

	/**
	 * Berechnete Gesamth�he f�r {@link #infoFont}
	 * @see #infoFont
	 * @see #drawRect(Graphics, Rectangle, double, Color, int, Color, int)
	 */
	private int infoFontHeight;

	/**
	 * Zeichnet das Element inkl. optionalem Markierungsrahmen und optionalen Animations-Laufzeitdaten
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (in Pixeln)
	 * @param fillColor	F�llfarbe (oder <code>null</code>, wenn die Box transparent sein soll)
	 * @param stage	Gibt an, ob nur der Hintergrund (stage=1) oder nur der Rahmen (stage=2) gezeichnet werden soll
	 * @return	Liefert das Rechteck der Box, sofern dieses zumindest teilweise sichtbar ist, oder <code>null</code> wenn die komplette Box au�erhalb des Sichtbereichs liegt und daher nicht gezeichnet werden muss.
	 * @see #drawElementShape(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 * @see #getRunDataAnimationInfo()
	 */
	@Override
	protected final Rectangle drawRect(final Graphics graphics, final Rectangle drawRect, final double zoom, final Color borderColor, final int borderWidth, final Color fillColor, final int stage) {
		Rectangle objectRect=super.drawRect(graphics,drawRect,zoom,borderColor,borderWidth,fillColor,stage);

		if (stage==2) {
			String info=getRunDataAnimationInfo();
			String[] infoMulti=null;
			Color color=Color.GRAY;

			if (surface!=null) {
				final String msg=getInfoMessage();
				if (msg!=null) {
					if (info==null) info=msg; else info=msg+", "+info;
				}

				final String error=getErrorMessage();
				if (error!=null) {
					if (info!=null) {
						infoMulti=(info+"\n"+splitLine(error,20)).split("\\n");
						info=null;
					} else {
						infoMulti=splitLine(error,20).split("\\n");
					}
					color=Color.RED;
				}
			}

			if (info!=null || infoMulti!=null) {
				if (infoFont==null || zoom!=infoFontZoom) {
					infoFontZoom=zoom;
					infoFont=new Font(DEFAULT_FONT_TYPE,Font.PLAIN,(int)FastMath.round(10*zoom));
					graphics.setFont(infoFont);
					infoFontAscent=graphics.getFontMetrics().getAscent();
					infoFontHeight=infoFontAscent+graphics.getFontMetrics().getDescent();
				} else {
					graphics.setFont(infoFont);
				}

				final Shape clipShape=graphics.getClip();
				graphics.setClip(drawRect);
				graphics.setColor(color);
				if (info!=null) {
					graphics.drawString(info,objectRect.x,objectRect.y+objectRect.height+infoFontAscent);
				} else {
					if (infoMulti!=null) for (int i=0;i<infoMulti.length;i++) graphics.drawString(infoMulti[i],objectRect.x,objectRect.y+objectRect.height+infoFontAscent+i*infoFontHeight);
				}
				graphics.setClip(clipShape);
			}
		}

		return objectRect;
	}

	/**
	 * Sollen w�hrend einer Animation immer alle wartenden Kunden gezeichnet
	 * werden (dann ggf. in mehreren Zeilen �bereinander)? Wenn nein, werden
	 * nur maximal 10 wartende Kunden dargestellt.
	 * @see #isDrawQueueAll()
	 * @see #setDrawQueueAll(boolean)
	 */
	private boolean drawQueueAll=false;

	/**
	 * Sollen w�hrend einer Animation immer alle wartenden Kunden gezeichnet
	 * werden (dann ggf. in mehreren Zeilen �bereinander)? Wenn nein, werden
	 * nur maximal 10 wartende Kunden dargestellt.
	 * @return	Immer alle wartenden Kunden w�hrend einer Animation darstellen
	 * @see #setDrawQueueAll(boolean)
	 */
	public boolean isDrawQueueAll() {
		return drawQueueAll;
	}

	/**
	 * Stellt ein, ob w�hrend einer Animation immer alle wartenden Kunden
	 * gezeichnet werden sollen (statt nur max. 10 Kunden).
	 * @param drawQueueAll	Alle wartenden Kunden zeichnen?
	 * @see #isDrawQueueAll()
	 */
	public void setDrawQueueAll(final boolean drawQueueAll) {
		this.drawQueueAll=drawQueueAll;
	}

	/**
	 * Kann an der Station eine Warteschlange auftreten?<br>
	 * (Wenn ja, werden im Eigenschaftendialog Funktionen angeboten,
	 * um einzustellen, ob diese w�hrend einer Animation immer
	 * vollst�ndig oder verk�rzt gezeichnet werden soll.)
	 * @see #isDrawQueueAll()
	 * @see #setDrawQueueAll(boolean)
	 * @return	Kann an der Station eine Warteschlange entstehen?
	 */
	public boolean hasQueue() {
		return false;
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (userBackgroundColor!=null) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(userBackgroundColor));
		}

		final BufferedImage image=shape.getCustomImage();
		if (image!=null) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.BackgroundColor.Image"));
			node.appendChild(sub);
			try {
				final ByteArrayOutputStream stream=new ByteArrayOutputStream();
				ImageIO.write(image,"png",stream);
				sub.setTextContent(new String(Base64.getEncoder().encode(stream.toByteArray())));
			} catch (IOException e) {}
		}

		if (!boxFontLarge.equals(DEFAULT_FONT_LARGE)) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.BoxFont.Large"));
			node.appendChild(sub);
			sub.setAttribute(Language.tr("Surface.XML.BoxFont.Family"),boxFontLarge.getName());
			sub.setAttribute(Language.tr("Surface.XML.BoxFont.Size"),""+boxFontLarge.getSize());
			if (boxFontLarge.isBold()) sub.setAttribute(Language.tr("Surface.XML.BoxFont.Bold"),"1");
			if (boxFontLarge.isItalic()) sub.setAttribute(Language.tr("Surface.XML.BoxFont.Italic"),"1");
		}

		if (!boxFontSmall.equals(DEFAULT_FONT_SMALL)) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.BoxFont.Small"));
			node.appendChild(sub);
			sub.setAttribute(Language.tr("Surface.XML.BoxFont.Family"),boxFontSmall.getName());
			sub.setAttribute(Language.tr("Surface.XML.BoxFont.Size"),""+boxFontSmall.getSize());
			if (boxFontSmall.isBold()) sub.setAttribute(Language.tr("Surface.XML.BoxFont.Bold"),"1");
			if (boxFontSmall.isItalic()) sub.setAttribute(Language.tr("Surface.XML.BoxFont.Italic"),"1");
		}

		if (maxThroughputIntervalSeconds!=DEFAULT_MAX_THROUGHPUT_INTERVAL_SECONDS) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.MaxThroughputSeconds"));
			node.appendChild(sub);
			if (maxThroughputIntervalSeconds<=0) {
				sub.setTextContent("0");
			} else {
				sub.setTextContent(""+maxThroughputIntervalSeconds);
			}
		}
		if (drawQueueAll) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.QueueAnimationComplete"));
			node.appendChild(sub);
			sub.setTextContent("1");
		}
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

		if (Language.trAll("Surface.XML.BackgroundColor",name)) {
			if (!content.isBlank()) {
				final Color color=EditModel.loadColor(content);
				if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
				userBackgroundColor=color;
			}
			return null;
		}

		if (Language.trAll("Surface.XML.BackgroundColor.Image",name)) {
			if (!content.isBlank()) 	try {
				final ByteArrayInputStream stream=new ByteArrayInputStream(Base64.getDecoder().decode(content));
				final boolean useCache=ImageIO.getUseCache();
				try {
					ImageIO.setUseCache(false); /* Wird ben�tigt, wenn im Stream nicht gesprungen werden kann, was bei einem ByteArrayInputStream nun definitiv m�glich ist.  */
					final BufferedImage image=ImageIO.read(stream);
					if (image!=null) shape.setCustomImage(image);
				} finally {
					ImageIO.setUseCache(useCache);
				}
			} catch (IOException | IllegalArgumentException e) {
				return null;
			}
			return null;
		}

		if (Language.trAll("Surface.XML.BoxFont.Large",name)) {
			final String family=Language.trAllAttribute("Surface.XML.BoxFont.Family",node);
			final Long size=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.XML.BoxFont.Size",node));
			int style=Font.PLAIN;
			final String bold=Language.trAllAttribute("Surface.XML.BoxFont.Bold",node);
			final String italic=Language.trAllAttribute("Surface.XML.BoxFont.Italic",node);
			if (!bold.isEmpty() && !bold.equals("0")) style+=Font.BOLD;
			if (!italic.isEmpty() && !italic.equals("0")) style+=Font.ITALIC;
			boxFontLarge=new Font(FontCache.getFontCache().getFamilyFromName(family).name,style,(size==null)?DEFAULT_FONT_LARGE.getSize():size.intValue());
			return null;
		}

		if (Language.trAll("Surface.XML.BoxFont.Small",name)) {
			final String family=Language.trAllAttribute("Surface.XML.BoxFont.Family",node);
			final Long size=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.XML.BoxFont.Size",node));
			int style=Font.PLAIN;
			final String bold=Language.trAllAttribute("Surface.XML.BoxFont.Bold",node);
			final String italic=Language.trAllAttribute("Surface.XML.BoxFont.Italic",node);
			if (!bold.isEmpty() && !bold.equals("0")) style+=Font.BOLD;
			if (!italic.isEmpty() && !italic.equals("0")) style+=Font.ITALIC;
			boxFontSmall=new Font(FontCache.getFontCache().getFamilyFromName(family).name,style,(size==null)?DEFAULT_FONT_SMALL.getSize():size.intValue());
			return null;
		}

		if (Language.trAll("Surface.XML.MaxThroughputSeconds",name)) {
			final Integer I=NumberTools.getInteger(content);
			if (I==null) maxThroughputIntervalSeconds=DEFAULT_MAX_THROUGHPUT_INTERVAL_SECONDS; else maxThroughputIntervalSeconds=I;
			return null;
		}

		if (Language.trAll("Surface.XML.QueueAnimationComplete",name)) {
			drawQueueAll=content.equals("1");
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation �berhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung �bersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation ber�cksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden m�ssten).
	 * @return	Gibt <code>true</code> zur�ck, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	public boolean inputConnected() {
		return true;
	}

	@Override
	protected boolean canHandleBreakPoints() {
		return inputConnected();
	}

	/**
	 * Sichert den Zugriff auf {@link #runDataInfo} und {@link #runDataInfoString} ab.
	 * @see #runDataInfo
	 * @see #runDataInfoString
	 * @see #getRunDataAnimationInfo()
	 * @see #setAnimationStringBuilder(StringBuilder, Runnable)
	 */
	private Semaphore runDataDrawLock=new Semaphore(1);

	/**
	 * Laufzeitdaten zu dem Element
	 * @see #getRunData()
	 * @see #initAnimation(SimulationData)
	 */
	private RunElementData runData=null;

	/**
	 * Bezeichner "aktuell="
	 */
	private String runDataCurrentString;

	/**
	 * Bezeichner "gesamt="
	 */
	private String runDataSumString;

	/**
	 * Ausgabeobjekt f�r die Erstellung von Animations-Ausgabedaten
	 * @see #getRunDataAnimationInfo()
	 * @see #setAnimationStringBuilder(StringBuilder, Runnable)
	 */
	private StringBuilder runDataInfo;

	/**
	 * Zeichenkette zur Ausgabe von Animations-Ausgaben
	 * @see #getRunDataAnimationInfo()
	 * @see #setAnimationStringBuilder(StringBuilder, Runnable)
	 */
	private String runDataInfoString;

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zus�tzlich auch die aktuelle Anzahl an Kunden an der Station
	 * w�hrend der Animation angezeigt werden sollen.<br>
	 * Nur Gesamtanzahl = <code>false</code>, Gesamtanzahl und aktueller Wert = <code>true</code>

	 * @see #showFullAnimationRunData()
	 */
	private boolean runDataFullInformation;

	/**
	 * Liefert den Text, der als Laufzeitinformation w�hrend der Animation
	 * unter dem Element angezeigt werden soll.
	 * @return	Laufzeittext w�hrend der Animation (kann <code>null</code> sein, wenn nichts angezeigt werden soll)
	 */
	protected String getRunDataAnimationInfo() {
		if (runData==null) {
			if (SetupData.getSetup().showIDs) {
				final String s=getIDInfo();
				if (s.length()>30) return s.substring(0,25)+"...";
				return s;
			}
			return null;
		}
		runDataDrawLock.acquireUninterruptibly();
		try {
			if (runDataInfoString==null) {
				if (runDataInfo!=null) runDataInfoString=runDataInfo.toString();
			}
			return runDataInfoString;
		} finally {
			runDataDrawLock.release();
		}
	}

	/**
	 * Liefert den Text, der im Editor-Modus unter dem Element angezeigt werden soll
	 * @return	Informationstext (z.B. ID des Elements)
	 */
	protected String getIDInfo() {
		return "id="+getId();
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station w�hrend der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	public boolean showAnimationRunData() {
		return SetupData.getSetup().showStationRunTimeData;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zus�tzlich auch die aktuelle Anzahl an Kunden an der Station
	 * w�hrend der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	public boolean showFullAnimationRunData() {
		return false;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		if (!showAnimationRunData()) return;
		final RunElement runElement=simData.runModel.elements.get(getId());
		if (runElement==null) return;
		runData=runElement.getData(simData);
		runDataCurrentString=Language.tr("Animation.StationRunInfo.Current")+"=";
		runDataSumString=Language.tr("Animation.StationRunInfo.Sum")+"=";
		runDataFullInformation=showFullAnimationRunData();
	}

	/**
	 * Liefert das Daten-Element zu dem Laufzeit-Element zu diesem Modell-Element.<br>
	 * L�uft keine Animation, so liefert die Funktion <code>null</code>
	 * @return	Laufzeit-Daten-Element w�hrend der Animation
	 */
	protected RunElementData getRunData() {
		return runData;
	}

	/**
	 * Aktuelle Anzahl der Kunden an der Station beim letzten Aufruf von {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private long lastClientsAtStation=-1;

	/**
	 * Gesamtzahl der Kunden an der Station beim letzten Aufruf von {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private long lastClients=-1;

	/**
	 * Array der {@link StringBuilder} f�r die Ausgabe
	 * der Animationsdaten
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private StringBuilder[] animationSB;

	/**
	 * Index des n�chsten {@link StringBuilder}-Objektes f�r die
	 * Ausgabe
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private int animationSBNext;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (runData==null || isPreview) return false;

		if (runDataFullInformation) {
			final int clientsAtStation=runData.reportedClientsAtStation(simData);
			if (clientsAtStation==lastClientsAtStation && runData.clients==lastClients) return false;
			lastClientsAtStation=clientsAtStation;
			lastClients=runData.clients;
		} else {
			if (runData.clients==lastClients) return false;
			lastClients=runData.clients;
		}

		if (runDataFullInformation) {
			if (animationSB==null) {
				animationSB=new StringBuilder[2];
				animationSB[0]=new StringBuilder(runDataCurrentString.length()+10+2+runDataSumString.length()+10);
				animationSB[1]=new StringBuilder(runDataCurrentString.length()+10+2+runDataSumString.length()+10);
			} else {
				animationSB[animationSBNext].setLength(0);
			}
			final StringBuilder sb=animationSB[animationSBNext];
			sb.append(runDataCurrentString);
			NumberTools.formatLongAndAppendToBuilder(lastClientsAtStation,sb);
			sb.append(',');
			sb.append(' ');
			sb.append(runDataSumString);
			NumberTools.formatLongAndAppendToBuilder(lastClients,sb);
		} else {
			if (animationSB==null) {
				animationSB=new StringBuilder[2];
				animationSB[0]=new StringBuilder(runDataSumString.length()+10);
				animationSB[1]=new StringBuilder(runDataSumString.length()+10);
			} else {
				animationSB[animationSBNext].setLength(0);
			}
			final StringBuilder sb=animationSB[animationSBNext];
			sb.append(runDataSumString);
			NumberTools.formatLongAndAppendToBuilder(lastClients,sb);
		}

		setAnimationStringBuilder(animationSB[animationSBNext],updateSimulationDataRunnableInstance);

		return true;
	}

	/**
	 * Runnable zur Aktualisierung des jeweils zu verwndenden {@link StringBuilder}-Objektes
	 * in {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 * @see #setAnimationStringBuilder(StringBuilder, Runnable)
	 */
	private Runnable updateSimulationDataRunnableInstance=()->{ /* Spart Speicherplatz */
		animationSBNext=(animationSBNext++)%2;
	};

	/**
	 * Stellt einen {@link StringBuilder} ein, dem beim Ausgeben der Infodaten unter einer Station w�hrend der Animation der Text entnommen werden soll.
	 * @param sb	{@link StringBuilder} dem w�hrend der Animation der Text mit den Stations-Infodaten entnommen werden soll
	 * @param processLocked	Optionales {@link Runnable}, welches nach der Speicherung des {@link StringBuilder} ausgef�hrt wird.
	 */
	protected final void setAnimationStringBuilder(final StringBuilder sb, final Runnable processLocked) {
		runDataDrawLock.acquireUninterruptibly();
		try {
			runDataInfo=sb;
			runDataInfoString=null;
			if (processLocked!=null) processLocked.run();
		} finally {
			runDataDrawLock.release();
		}
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		descriptionBuilder.beginStation(this);
	}

	/**
	 * Liefert die Implementierung der "infoBox"-Javascript-Funktion
	 * f�r die Ausgabe des Elements in Javascript-Code.
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 * @return	Javascript-Funktion
	 * @see #specialHTMLOutput(HTMLOutputBuilder)
	 */
	private String getInfoBox(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function addBoxElementInfo(rect,info) {\n");
		sb.append("  if (info==\"\") return;\n");
		sb.append("  var span=document.createElement(\"span\");\n");
		sb.append("  span.title=info;\n");
		sb.append("  span.style.position=\"absolute\";\n");
		sb.append("  span.style.left=rect.x+\"px\";\n");
		sb.append("  span.style.top=rect.y+\"px\";\n");
		sb.append("  span.style.width=rect.w+\"px\";\n");
		sb.append("  span.style.height=rect.h+\"px\";\n");
		sb.append("  document.getElementById(\"surface_box\").appendChild(span);\n");
		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert die Implementierung der "boxElementFont"-Javascript-Funktion
	 * f�r die Ausgabe des Elements in Javascript-Code.
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 * @return	Javascript-Funktion
	 * @see #specialHTMLOutput(HTMLOutputBuilder)
	 */
	private String getBoxElementFont(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function setBoxElementFont(large) {\n");

		sb.append("  var size=(typeof(large)!=\"undefined\" && large)?12:9;\n");
		sb.append("  context.font=size+\"px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.textAlign=\"center\";\n");
		sb.append("  context.textBaseline=\"middle\";\n");
		sb.append("  context.fillStyle=\"black\";\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialHTMLOutput(final HTMLOutputBuilder outputBuilder) {
		/* Notwendige JS-Funktionen bereithalten */
		outputBuilder.addJSUserFunction("infoBox",builder->getInfoBox(builder));
		outputBuilder.addJSUserFunction("boxElementFont",builder->getBoxElementFont(builder));

		/* Elementenform ausgeben */
		specialOutputShape(outputBuilder,Color.DARK_GRAY,1,getDrawBackgroundColor());

		final Point p=getPosition(true);
		final Dimension s=getSize();

		/* Text auf Element schreiben */
		if (drawText) {
			String textType=getTypeName();
			final String textName=getName();
			final int x=p.x+s.width/2;
			int y;
			int yStart;

			outputBuilder.outputBody.append("setBoxElementFont();\n");

			if (textType!=null && !textType.isEmpty()) {
				final String subType=getSubTypeName();
				if (subType!=null && !subType.isEmpty()) {
					if (textName!=null && !textName.isEmpty()) y=s.height/6; else y=s.height/3;
					outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textType)+"\","+x+","+(p.y+y)+");\n");
					final String textSub="("+subType+")";
					if (textName!=null && !textName.isEmpty()) y=s.height/3; else y=2*s.height/3;
					outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textSub)+"\","+x+","+(p.y+y)+");\n");
					yStart=2*s.height/3;
				} else {
					if (textName!=null && !textName.isEmpty()) y=s.height/3; else y=s.height/2;
					outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textType)+"\","+x+","+(p.y+y)+");\n");
					yStart=2*s.height/3;
				}
			} else {
				yStart=s.height/2;
			}

			if (textName!=null && !textName.isEmpty()) {
				outputBuilder.outputBody.append("setBoxElementFont(true);\n");
				outputBuilder.outputBody.append("context.fillText(\""+HTMLOutputBuilder.encodeTextFieldHTML(textName)+"\","+x+","+(p.y+yStart)+");\n");
			}
		}

		/* Tooltip-Beschreibung einf�gen */
		final ModelDescriptionBuilderStyled descriptionBuilder=new ModelDescriptionBuilderStyled(getModel(),true);
		buildDescription(descriptionBuilder);
		String info=descriptionBuilder.getText();
		info=HTMLOutputBuilder.encodeTextFieldHTML(info);
		info=info.replaceAll("\n","\\\\n");
		outputBuilder.outputBody.append("addBoxElementInfo({x: "+p.x+",y: "+p.y+",w: "+s.width+",h: "+s.height+"},\""+info+"\");\n");
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialHTMLOutput((HTMLOutputBuilder)outputBuilder);
	}

	/**
	 * Pr�ft m�gliche angebundene externe Datenquellen und liefert das Ergebnis zur�ck.
	 * @return	Informationen zur Verf�gbarkeit externer Datenquellen.
	 * @see DataCheckResult
	 */
	public DataCheckResult checkExternalData() {
		return DataCheckResult.noCheckNeeded(this);
	}

	/**
	 * Stellt neue Verbindungskanten ein, ohne dabei die alten Kanten l�schen und ohne die neuen
	 * Kanten zu benachrichtigen. Diese Funktion wird von {@link ModelElementReference} verwendet,
	 * um die Kopie ein konkreten Elements an der Platzhalterstelle einzuf�gen
	 * @param connectionsIn	Liste der einlaufenden Kanten (kann leer sind, aber ist nie <code>null</code>)
	 * @param connectionsOut	Liste der auslaufenden Kanten (kann leer sind, aber ist nie <code>null</code>)
	 * @return	Gibt <code>true</code> zur�ck, wenn die Anzahl an ein- und auslaufenden Kanten zu dem Element passt und eingetragen werden konnte
	 */
	public boolean setReferenceEdges(final List<ModelElementEdge> connectionsIn, final List<ModelElementEdge> connectionsOut) {
		return false;
	}

	/**
	 * Aktuelles zus�tzliches Icon das auf das Shape gezeichnet wird
	 * @see #setAdditionalClientIconFromName(String)
	 * @see #setAdditionalIconFromName(String)
	 * @see #setAdditionalTransporterIconFromName(String)
	 */
	private String lastAdditionalIcon=null;

	/**
	 * Aktuelles zus�tzliches Kunden-Icon das auf das Shape gezeichnet wird
	 * @see #setAdditionalClientIconFromName(String)
	 * @see #setAdditionalIconFromName(String)
	 * @see #setAdditionalTransporterIconFromName(String)
	 */
	private String lastAdditionalClientIcon=null;

	/**
	 * Aktuelles zus�tzliches Transporter-Icon das auf das Shape gezeichnet wird
	 * @see #setAdditionalClientIconFromName(String)
	 * @see #setAdditionalIconFromName(String)
	 * @see #setAdditionalTransporterIconFromName(String)
	 */
	private String lastAdditionalTransporterIcon=null;

	/**
	 * Stellt ein zus�tzliches Icon ein, welches auf das Shape gezeichnet wird.
	 * @param name	Name des Icons (gem�� {@link ModelClientData}).
	 */
	protected final void setAdditionalClientIconFromName(final String name) {
		lastAdditionalClientIcon=null;
		lastAdditionalTransporterIcon=null;

		final EditModel model=getModel();
		if (model==null || surface==null || shape==null) return;

		if (name==null || name.isBlank()) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}

		lastAdditionalClientIcon=name;
		setAdditionalIconFromName(getModel().clientData.getIcon(name));
	}

	/**
	 * Stellt ein zus�tzliches Icon ein, welches auf das Shape gezeichnet wird.
	 * @param name	Name des Icons (gem�� {@link ModelTransporters})
	 */
	protected final void setAdditionalTransporterIconFromName(final String name) {
		lastAdditionalClientIcon=null;
		lastAdditionalTransporterIcon=null;

		final EditModel model=getModel();
		if (model==null || surface==null || shape==null) return;

		if (name==null || name.isBlank()) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}
		final ModelTransporter transporter=getModel().transporters.get(name);

		if (transporter==null) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}

		lastAdditionalTransporterIcon=name;
		setAdditionalIconFromName(transporter.getEastLoadedIcon());
	}

	/**
	 * Stellt ein zus�tzliches Icon ein, welches auf das Shape gezeichnet wird.
	 * @param name	Name des Icons (gem�� {@link ModelClientData}).
	 */
	protected final void setAdditionalIconFromName(final String name) {
		final EditModel model=getModel();
		if (model==null || surface==null || shape==null) return;

		if (name==null || name.isBlank()) {
			shape.setAdditionalIcon(null);
			lastAdditionalIcon=null;
			return;
		}

		if (lastAdditionalIcon!=null && lastAdditionalIcon.equals(name)) return;
		lastAdditionalIcon=name;
		final AnimationImageSource imageSource=new AnimationImageSource();
		shape.setAdditionalIcon(()->imageSource.get(name,getModel().animationImages,16));
	}

	/**
	 * Aktualisiert die kleinen Icons auf den Stationen, wenn zuvor z.B. das Icon f�r einen Kundentypen ver�ndert wurde.
	 * @see ModelSurface#updateAdditionalIcons()
	 */
	public final void updateAdditionalIcon() {
		if (lastAdditionalClientIcon!=null) {
			setAdditionalIconFromName(getModel().clientData.getIcon(lastAdditionalClientIcon));
		}
		if (lastAdditionalTransporterIcon!=null) {
			final ModelTransporter transporter=getModel().transporters.get(lastAdditionalTransporterIcon);
			if (transporter==null) {
				lastAdditionalTransporterIcon=null;
			} else {
				setAdditionalIconFromName(transporter.getEastLoadedIcon());
			}
		}
	}

	/**
	 * Liefert den Namen dieser Station f�r andere Elemente die auf diese Referenzieren
	 * @return	Name dieser Station f�r andere Elemente die auf diese Referenzieren
	 * @see #addVisualizationTrafficLightsMenuItem(String, JMenu, Consumer)
	 * @see #addVisualizationTrafficLightsMenuItem(String, JMenu, Consumer, String)
	 * @see #addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	private String getNameForReference() {
		final StringBuilder sb=new StringBuilder();
		sb.append(getTypeName());
		if (!getName().isBlank()) {
			sb.append(" \"");
			sb.append(getName());
			sb.append("\"");
		}
		sb.append(String.format(" (id=%d)",getId()));
		return sb.toString();
	}

	/**
	 * Erstellt einen Kontextmen�punkt (und f�gt ihn in ein Men� ein) zur Visualisierung von Daten des aktuellen Elements in Form einer Ampel
	 * @param condition	Bedingung f�r "rot"
	 * @param parentMenu	Men� in das der Men�punkt eingef�gt werden soll
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 * @see ModelElementBox#addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	protected final JMenuItem addVisualizationTrafficLightsMenuItem(final String condition, final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		final String referenceName=getNameForReference();
		final ModelElementAnimationTrafficLights element=new ModelElementAnimationTrafficLights(null,null);
		element.setLightsCount(2);
		element.setExpressionOne(condition);
		element.setName(referenceName);

		final ModelElementText addon=ModelSurfacePanel.getVisualizationPlainText(referenceName,11,0,-20);

		return addVisualizationMenuItem(new ModelElementPosition[] {element,addon},element.getContextMenuElementName(),parentMenu,addElements);
	}

	/**
	 * Erstellt einen Kontextmen�punkt (und f�gt ihn in ein Men� ein) zur Visualisierung von Daten des aktuellen Elements in Form einer Ampel
	 * @param condition	Bedingung f�r "rot"
	 * @param parentMenu	Men� in das der Men�punkt eingef�gt werden soll
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 * @param info	Zus�tzliche Ausgabe beim Men�punkt
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 * @see ModelElementBox#addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	protected final JMenuItem addVisualizationTrafficLightsMenuItem(final String condition, final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements, final String info) {
		final String referenceName=getNameForReference();
		final ModelElementAnimationTrafficLights element=new ModelElementAnimationTrafficLights(null,null);
		element.setLightsCount(2);
		element.setExpressionOne(condition);
		element.setName(referenceName);

		final ModelElementText addon=ModelSurfacePanel.getVisualizationPlainText(referenceName,11,0,-20);

		return addVisualizationMenuItem(new ModelElementPosition[] {element,addon},element.getContextMenuElementName()+((info==null || info.isEmpty())?"":(" - "+info)),parentMenu,addElements);
	}

	/**
	 * Art der zus�tzlichen Visualisierung, die �ber das Kontextmen� angeboten werden soll
	 * @author Alexander Herzog
	 * @see ModelElementBox#addVisualizationMenuItem(JMenu, Consumer, VisualizationType)
	 */
	protected enum VisualizationType {
		/**
		 * Aktuelle Anzahl an Kunden an der Station als Text
		 */
		TEXT_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als Text
		 */
		TEXT_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als Text
		 */
		TEXT_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als Text
		 */
		TEXT_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Text
		 */
		TEXT_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Text
		 */
		TEXT_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Text
		 */
		TEXT_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Text
		 */
		TEXT_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Text
		 */
		TEXT_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Text
		 */
		TEXT_NQ_AVERAGE_3,

		/**
		 * Aktueller Wert des Z�hlers als Text
		 */
		TEXT_COUNTER_VALUE,

		/**
		 * Aktueller Anteil des Z�hlers in der Z�hlergruppe als Text
		 */
		TEXT_COUNTER_PART,

		/**
		 * Aktueller Durchsatzwert als Text
		 */
		TEXT_THROUGHPUT,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als Text
		 */
		TEXT_ANALOG_VALUE,

		/**
		 * �nderungsrate eines "Analoger Wert"-Elements als Text
		 */
		TEXT_ANALOG_RATE,

		/**
		 * Aktuelle Anzahl an Kunden (aller Typen) im System
		 */
		TEXT_WIP_CLIENT,

		/**
		 * Mittlere Wartezeit der Kunden des Typs, der an der aktuellen Station erzeugt oder zugewiesen wird
		 */
		TEXT_W_CLIENT,

		/**
		 * Aktuelle Anzahl an Kunden an der Station als LCD-Anzeige
		 */
		LCD_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als LCD-Anzeige
		 */
		LCD_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als LCD-Anzeige
		 */
		LCD_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als LCD-Anzeige
		 */
		LCD_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als LCD-Anzeige
		 */
		LCD_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als LCD-Anzeige
		 */
		LCD_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als LCD-Anzeige
		 */
		LCD_NQ_AVERAGE_3,

		/**
		 * Aktueller Wert des Z�hlers als LCD-Anzeige
		 */
		LCD_COUNTER_VALUE,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als LCD-Anzeige
		 */
		LCD_ANALOG_VALUE,

		/**
		 * Aktuelle Anzahl an Kunden (aller Typen) im System als LCD-Anzeige
		 */
		LCD_WIP_CLIENT,

		/**
		 * Mittlere Wartezeit der Kunden des Typs, der an der aktuellen Station erzeugt oder zugewiesen wird, als LCD-Anzeige
		 */
		LCD_W_CLIENT,

		/**
		 * Aktuelle Anzahl an Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Analogskala-Anzeige
		 */
		SCALE_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Analogskala-Anzeige
		 */
		SCALE_NQ_AVERAGE_3,

		/**
		 * Aktueller Wert des Z�hlers als Analogskala-Anzeige
		 */
		SCALE_COUNTER_VALUE,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als Analogskala-Anzeige
		 */
		SCALE_ANALOG_VALUE,

		/**
		 * Aktuelle Anzahl an Kunden (aller Typen) im System als Analogskala-Anzeige
		 */
		SCALE_WIP_CLIENT,

		/**
		 * Mittlere Wartezeit der Kunden des Typs, der an der aktuellen Station erzeugt oder zugewiesen wird, als Analogskala-Anzeige
		 */
		SCALE_W_CLIENT,

		/**
		 * Aktuelle Anzahl an Kunden an der Station als Balken
		 */
		BAR_WIP_CURRENT,

		/**
		 * Mittlere Anzahl an Kunden an der Station als Balken
		 */
		BAR_WIP_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station als Balken
		 */
		BAR_NQ_CURRENT,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station als Balken
		 */
		BAR_NQ_AVERAGE,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Balken
		 */
		BAR_NQ_CURRENT_1,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Balken
		 */
		BAR_NQ_CURRENT_2,

		/**
		 * Aktuelle Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Balken
		 */
		BAR_NQ_CURRENT_3,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 1) als Balken
		 */
		BAR_NQ_AVERAGE_1,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 2) als Balken
		 */
		BAR_NQ_AVERAGE_2,

		/**
		 * Mittlere Anzahl an wartenden Kunden an der Station (in Teilwarteschlange 3) als Balken
		 */
		BAR_NQ_AVERAGE_3,

		/**
		 * Aktueller Anteil des Z�hlers in der Z�hlergruppe als Balken
		 */
		BAR_COUNTER_PART,

		/**
		 * Aktueller Wert eines "Analoger Wert"- oder "Tank"-Elements als Balken
		 */
		BAR_ANALOG_VALUE,

		/**
		 * Anzahl an Kunden an der Station als Verlaufsgrafik
		 */
		CHART_WIP,

		/**
		 * Anzahl an wartenden Kunden an der Station als Verlaufsgrafik
		 */
		CHART_NQ,

		/**
		 * Wert eines "Analoger Wert"- oder "Tank"-Elements als Verlaufsgrafik
		 */
		CHART_ANALOG_VALUE,

		/**
		 * Anzahl an Kunden an der Station als Histogramm
		 */
		HISTOGRAM_WIP,

		/**
		 * Anzahl an wartenden Kunden an der Station als Histogramm
		 */
		HISTOGRAM_NQ,

		/**
		 * Anzeige der Werte eines Datenaufzeichnung-Elements
		 */
		RECORD
	}

	/**
	 * Erstellt einen Kontextmen�punkt (und f�gt ihn in ein Men� ein) zur Visualisierung von Daten des aktuellen Elements
	 * @param parentMenu	Men� in das der Men�punkt eingef�gt werden soll
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 * @param type	Art der Visualisierung
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 * @see ModelElementBox.VisualizationType
	 */
	protected final JMenuItem addVisualizationMenuItem(final JMenu parentMenu, Consumer<ModelElementPosition[]> addElements, final VisualizationType type) {
		final Consumer<ModelElementPosition[]> addElementOrig=addElements;
		final String referenceName=getNameForReference();
		String addonInfo=null;
		ModelElementPosition element=null;
		ModelElementPosition addon=null;
		List<Object[]> data;

		final String stationInfo=" "+Language.tr("Surface.Popup.AddVisualization.AtStation")+" "+referenceName;

		switch (type) {
		case TEXT_WIP_CURRENT:
			element=ModelSurfacePanel.getVisualizationText("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			break;
		case TEXT_WIP_AVERAGE:
			element=ModelSurfacePanel.getVisualizationText("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			break;
		case TEXT_NQ_CURRENT:
			element=ModelSurfacePanel.getVisualizationText("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			break;
		case TEXT_NQ_AVERAGE:
			element=ModelSurfacePanel.getVisualizationText("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			break;
		case TEXT_NQ_CURRENT_1:
			element=ModelSurfacePanel.getVisualizationText("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			break;
		case TEXT_NQ_CURRENT_2:
			element=ModelSurfacePanel.getVisualizationText("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			break;
		case TEXT_NQ_CURRENT_3:
			element=ModelSurfacePanel.getVisualizationText("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			break;
		case TEXT_NQ_AVERAGE_1:
			element=ModelSurfacePanel.getVisualizationText("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			break;
		case TEXT_NQ_AVERAGE_2:
			element=ModelSurfacePanel.getVisualizationText("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			break;
		case TEXT_NQ_AVERAGE_3:
			element=ModelSurfacePanel.getVisualizationText("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			break;
		case TEXT_COUNTER_VALUE:
			element=ModelSurfacePanel.getVisualizationText("counter("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			break;
		case TEXT_COUNTER_PART:
			element=ModelSurfacePanel.getVisualizationText("part("+getId()+")");
			((ModelElementAnimationTextValue)element).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterPart");
			break;
		case TEXT_THROUGHPUT:
			element=ModelSurfacePanel.getVisualizationText("counter("+getId()+")");
			((ModelElementAnimationTextValue)element).setDigits(5);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			break;
		case TEXT_ANALOG_VALUE:
			element=ModelSurfacePanel.getVisualizationText("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			break;
		case TEXT_ANALOG_RATE:
			element=ModelSurfacePanel.getVisualizationText("AnalogRate("+getId()+")");
			((ModelElementAnimationTextValue)element).setDigits(5);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogRate");
			break;
		case TEXT_WIP_CLIENT:
			element=ModelSurfacePanel.getVisualizationText("wip()");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal");
			break;
		case TEXT_W_CLIENT:
			element=ModelSurfacePanel.getVisualizationText("WaitingTime_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime");
			break;

			/* LCD-Anzeigen */

		case LCD_WIP_CURRENT:
			element=ModelSurfacePanel.getVisualizationLCD("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValue")+stationInfo,11,0,-20);
			break;
		case LCD_WIP_AVERAGE:
			element=ModelSurfacePanel.getVisualizationLCD("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValue")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_CURRENT:
			element=ModelSurfacePanel.getVisualizationLCD("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_AVERAGE:
			element=ModelSurfacePanel.getVisualizationLCD("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_CURRENT_1:
			element=ModelSurfacePanel.getVisualizationLCD("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_CURRENT_2:
			element=ModelSurfacePanel.getVisualizationLCD("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_CURRENT_3:
			element=ModelSurfacePanel.getVisualizationLCD("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_AVERAGE_1:
			element=ModelSurfacePanel.getVisualizationLCD("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_AVERAGE_2:
			element=ModelSurfacePanel.getVisualizationLCD("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2")+stationInfo,11,0,-20);
			break;
		case LCD_NQ_AVERAGE_3:
			element=ModelSurfacePanel.getVisualizationLCD("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3")+stationInfo,11,0,-20);
			break;
		case LCD_COUNTER_VALUE:
			element=ModelSurfacePanel.getVisualizationLCD("counter("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CounterValue")+stationInfo,11,0,-20);
			break;
		case LCD_ANALOG_VALUE:
			element=ModelSurfacePanel.getVisualizationLCD("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AnalogValue")+stationInfo,11,0,-20);
			break;
		case LCD_WIP_CLIENT:
			element=ModelSurfacePanel.getVisualizationLCD("wip()");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal")+stationInfo,11,0,-20);
			break;
		case LCD_W_CLIENT:
			element=ModelSurfacePanel.getVisualizationLCD("WaitingTime_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime")+stationInfo,11,0,-20);
			break;

			/* Zeigermessinstrumente */

		case SCALE_WIP_CURRENT:
			element=ModelSurfacePanel.getVisualizationPointer("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValue")+stationInfo,11,0,50);
			break;
		case SCALE_WIP_AVERAGE:
			element=ModelSurfacePanel.getVisualizationPointer("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValue")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_CURRENT:
			element=ModelSurfacePanel.getVisualizationPointer("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_AVERAGE:
			element=ModelSurfacePanel.getVisualizationPointer("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_CURRENT_1:
			element=ModelSurfacePanel.getVisualizationPointer("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_CURRENT_2:
			element=ModelSurfacePanel.getVisualizationPointer("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_CURRENT_3:
			element=ModelSurfacePanel.getVisualizationPointer("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_AVERAGE_1:
			element=ModelSurfacePanel.getVisualizationPointer("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_AVERAGE_2:
			element=ModelSurfacePanel.getVisualizationPointer("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2")+stationInfo,11,0,50);
			break;
		case SCALE_NQ_AVERAGE_3:
			element=ModelSurfacePanel.getVisualizationPointer("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3")+stationInfo,11,0,50);
			break;
		case SCALE_COUNTER_VALUE:
			element=ModelSurfacePanel.getVisualizationPointer("counter("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CounterValue")+stationInfo,11,0,50);
			break;
		case SCALE_ANALOG_VALUE:
			element=ModelSurfacePanel.getVisualizationPointer("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AnalogValue")+stationInfo,11,0,50);
			break;
		case SCALE_WIP_CLIENT:
			element=ModelSurfacePanel.getVisualizationPointer("wip()");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal")+stationInfo,11,0,50);
			break;
		case SCALE_W_CLIENT:
			element=ModelSurfacePanel.getVisualizationPointer("WaitingTime_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageWaitingTime")+stationInfo,11,0,50);
			break;

			/* Einzelne Balken */

		case BAR_WIP_CURRENT:
			element=ModelSurfacePanel.getVisualizationBar("wip("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValue")+stationInfo,11,0,-20);
			break;
		case BAR_WIP_AVERAGE:
			element=ModelSurfacePanel.getVisualizationBar("wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValue")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_CURRENT:
			element=ModelSurfacePanel.getVisualizationBar("NQ("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_AVERAGE:
			element=ModelSurfacePanel.getVisualizationBar("NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_CURRENT_1:
			element=ModelSurfacePanel.getVisualizationBar("NQ("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting1")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_CURRENT_2:
			element=ModelSurfacePanel.getVisualizationBar("NQ("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting2")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_CURRENT_3:
			element=ModelSurfacePanel.getVisualizationBar("NQ("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting3")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_AVERAGE_1:
			element=ModelSurfacePanel.getVisualizationBar("NQ_avg("+getId()+";1)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting1")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_AVERAGE_2:
			element=ModelSurfacePanel.getVisualizationBar("NQ_avg("+getId()+";2)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting2")+stationInfo,11,0,-20);
			break;
		case BAR_NQ_AVERAGE_3:
			element=ModelSurfacePanel.getVisualizationBar("NQ_avg("+getId()+";3)");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AverageValueWaiting3")+stationInfo,11,0,-20);
			break;
		case BAR_COUNTER_PART:
			element=ModelSurfacePanel.getVisualizationBar("part("+getId()+")");
			((ModelElementAnimationBar)element).setMaxValue(1);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CounterPart");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CounterPart")+stationInfo,11,0,-20);
			break;
		case BAR_ANALOG_VALUE:
			element=ModelSurfacePanel.getVisualizationBar("AnalogValue("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AnalogValue")+stationInfo,11,0,-20);
			break;

			/* Liniendiagramme */

		case CHART_WIP:
			element=ModelSurfacePanel.getVisualizationChart("wip("+getId()+")","wip_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValue")+stationInfo,0,-20);
			break;
		case CHART_NQ:
			element=ModelSurfacePanel.getVisualizationChart("NQ("+getId()+")","NQ_avg("+getId()+")");
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaiting")+stationInfo,0,-20);
			break;
		case CHART_ANALOG_VALUE:
			data=new ArrayList<>();
			data.add(new Object[]{new AnimationExpression("AnalogValue("+getId()+")"),Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(2)});
			element=ModelSurfacePanel.getVisualizationChart(data);
			addonInfo=Language.tr("Surface.Popup.AddVisualization.AnalogValue");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.AnalogValue")+stationInfo,0,-20);
			break;

			/* Histogramme */

		case HISTOGRAM_WIP:
			element=ModelSurfacePanel.getVisualizationHistogram();
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueHistogram");
			addElements=newElements->{
				final var histData=ModelSurfacePanel.getHistogramData(parentMenu,"WIP_hist("+getId()+";%d)","WIP_hist("+getId()+";%d;%d)");
				if (histData==null) return;
				((ModelElementAnimationBarChart)newElements[0]).setExpressionData(histData);
				addElementOrig.accept(newElements);
			};
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueHistogram")+stationInfo,0,-20);
			break;
		case HISTOGRAM_NQ:
			element=ModelSurfacePanel.getVisualizationHistogram();
			addonInfo=Language.tr("Surface.Popup.AddVisualization.CurrentValueWaitingHistogram");
			addElements=newElements->{
				final var histData=ModelSurfacePanel.getHistogramData(parentMenu,"NQ_hist("+getId()+";%d)","NQ_hist("+getId()+";%d;%d)");
				if (histData==null) return;
				((ModelElementAnimationBarChart)newElements[0]).setExpressionData(histData);
				addElementOrig.accept(newElements);
			};
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.CurrentValueWaitingHistogram")+stationInfo,0,-20);
			break;

			/* X-Y-Diagramme */

		case RECORD:
			element=ModelSurfacePanel.getVisualizationRecord();
			((ModelElementAnimationRecord)element).setRecordId(getId());
			addonInfo=Language.tr("Surface.Popup.AddVisualization.Record");
			addon=ModelSurfacePanel.getVisualizationPlainText(Language.tr("Surface.Popup.AddVisualization.Record")+stationInfo,0,-20);
			break;
		}

		if (element!=null) {
			element.setName(referenceName+((addonInfo==null)?"":(" - "+addonInfo)));
			final List<ModelElementPosition> elements=new ArrayList<>();
			elements.add(element);
			if (addon!=null) elements.add(addon);
			return addVisualizationMenuItem(elements.toArray(ModelElementPosition[]::new),element.getContextMenuElementName()+((addonInfo==null)?"":(" - "+addonInfo)),parentMenu,addElements);
		} else {
			return null;
		}
	}

	/**
	 * Art der Laufzeitstatistik, die �ber das Kontextmen� angeboten werden soll
	 * @author Alexander Herzog
	 * @see ModelElementBox#addLongRunStatisticsMenuItem(JMenu, Consumer, LongRunStatisticsType)
	 */
	protected enum LongRunStatisticsType {
		/**
		 * Anzahl an Kunden an der Station
		 */
		WIP,

		/**
		 * Anzahl an wartenden Kunden an der Station
		 */
		NQ,

		/**
		 * Anzahl an Kunden an der Station in Bedienung
		 */
		PROCESS,

		/**
		 * Anzahl an Ank�nften an der Station
		 */
		NUMBER_IN,

		/**
		 * Anzahl an Abg�ngen an der Station
		 */
		NUMBER_OUT,

		/**
		 * Aktueller Analogwert an der Station
		 */
		ANALOG_VALUE,

		/**
		 * Aktuelle �nderungsrate des Analogwertes an der Station
		 */
		ANALOG_RATE,

		/**
		 * Aktueller Wert des Z�hlers an der Station
		 */
		COUNTER,

		/**
		 * Aktzueller Anteils des Z�hlers an der Station in der Z�hlergruppe
		 */
		COUNTER_PART
	}

	/**
	 * Erstellt einen Kontextmen�punkt (und f�gt ihn in ein Men� ein) zum Hinzuf�gen von Laufzeitstatistikdaten
	 * @param parentMenu	Men� in das der Men�punkt eingef�gt werden soll
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Laufzeitstatistik-Eintrag zu dem Modell hinzugef�gt werden soll
	 * @param type	Art der Laufzeitstatistik
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 * @see ModelElementBox.LongRunStatisticsType
	 */
	protected final JMenuItem addLongRunStatisticsMenuItem(final JMenu parentMenu, final Consumer<String> addLongRunStatistics, final LongRunStatisticsType type) {
		String name=null;
		String value=null;

		switch (type) {
		case WIP:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.WIP");
			value="WIP("+getId()+")";
			break;
		case NQ:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.NQ");
			value="NQ("+getId()+")";
			break;
		case PROCESS:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.Process");
			value="Process("+getId()+")";
			break;
		case NUMBER_IN:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.NumberIn");
			value="NumberIn("+getId()+")";
			break;
		case NUMBER_OUT:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.NumberOut");
			value="NumberOut("+getId()+")";
			break;
		case ANALOG_VALUE:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.AnalogValue");
			value="AnalogValue("+getId()+")";
			break;
		case ANALOG_RATE:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.AnalogRate");
			value="AnalogRate("+getId()+")";
			break;
		case COUNTER:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.Counter");
			value="counter("+getId()+")";
			break;
		case COUNTER_PART:
			name=Language.tr("Surface.Popup.AddLongRunStatistics.Part");
			value="part("+getId()+")";
			break;
		}

		/* Wert schon in Liste? */
		for (ModelLongRunStatisticsElement item: getModel().longRunStatistics.getData()) if (item.expression.equals(value)) return null;

		if (name!=null && value!=null) {
			return addLongRunStatisticsMenuItem(name,value,parentMenu,addLongRunStatistics);
		} else {
			return null;
		}
	}

	/**
	 * Erstellt einen Kontextmen�punkt (und f�gt ihn in ein Men� ein) zum Hinzuf�gen einer typischen Folgestation des aktuellen Elements
	 * @param parentMenu	Men� in das der Men�punkt eingef�gt werden soll
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 * @param nextStationTemplate	Vorlage-Objekt f�r die Folgestation
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 */
	public final JMenuItem addNextStationMenuItem(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation, final ModelElementBox nextStationTemplate) {
		final JMenuItem item=new JMenuItem(nextStationTemplate.getContextMenuElementName());
		item.setToolTipText(nextStationTemplate.getToolTip());
		final Icon icon=nextStationTemplate.getAddElementIcon();
		if (icon!=null) item.setIcon(icon);
		item.addActionListener(e->addNextStation.accept(nextStationTemplate));
		parentMenu.add(item);
		return item;
	}

	/**
	 * Sucht einen Text in den Daten des Elements.
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Schriftgr��en */
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.FontSize.Small"),boxFontSmall.getSize(),newFontSize->{if (newFontSize>0) boxFontSmall=boxFontSmall.deriveFont((float)newFontSize);});
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.FontSize.Large"),boxFontLarge.getSize(),newFontSize->{if (newFontSize>0) boxFontSmall=boxFontLarge.deriveFont((float)newFontSize);});
	}

	/**
	 * Liefert die Intervalll�nge (gemessen in Sekunden) f�r die Erfassung des maximalen Durchsatzes an der Station.
	 * @return	Intervalll�nge (gemessen in Sekunden) f�r die Erfassung des maximalen Durchsatzes (Werte &le;0 f�r "aus")
	 * @see #setMaxThroughputIntervalSeconds(int)
	 */
	public int getMaxThroughputIntervalSeconds() {
		return maxThroughputIntervalSeconds;
	}

	/**
	 * Stellt die Intervalll�nge (gemessen in Sekunden) f�r die Erfassung des maximalen Durchsatzes an der Station ein.
	 * @param maxThroughputIntervalSeconds	Intervalll�nge (gemessen in Sekunden) f�r die Erfassung des maximalen Durchsatzes (Werte &le;0 f�r "aus")
	 * @see #getMaxThroughputIntervalSeconds()
	 */
	public void setMaxThroughputIntervalSeconds(final int maxThroughputIntervalSeconds) {
		this.maxThroughputIntervalSeconds=maxThroughputIntervalSeconds;
	}
}