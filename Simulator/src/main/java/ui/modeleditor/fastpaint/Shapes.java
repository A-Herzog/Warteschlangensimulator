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
package ui.modeleditor.fastpaint;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import tools.SetupData;
import ui.modeleditor.ScaledImageCache;
import ui.modeleditor.elements.FontCache;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeichnet die verschiedenen Formen auf die Zeichenoberfläche
 * @author Alexander Herzog
 */
public class Shapes {
	/**
	 * Farbe für Schatten
	 */
	public static final Color SHADOW_COLOR=new Color(192,192,192,128);

	/**
	 * Schattenbreite
	 */
	public static final int SHADOW_WIDTH=4;

	/**
	 * x-Richtung für den Schatten (1=nach rechts)
	 */
	public static final int SHADOW_DIRECTION_X=1;

	/**
	 * y-Richtung für den Schatten (1=nach unten)
	 */
	public static final int SHADOW_DIRECTION_Y=1;

	/**
	 * Formen
	 * (wird im Konstruktor übergeben)
	 */
	public enum ShapeType {
		/** Nichts zeichnen */
		SHAPE_NONE("none"),

		/** Rechteck */
		SHAPE_RECTANGLE("rectangle"),

		/** Abgerundetes Rechteck */
		SHAPE_ROUNDED_RECTANGLE("roundedRectangle"),

		/** Pfeil nach rechts */
		SHAPE_ARROW_RIGHT("arrowRight"),

		/** Pfeil nach rechts */
		SHAPE_ARROW_RIGHT_DOUBLE("arrowRightDouble"),

		/** Pfeil nach links */
		SHAPE_ARROW_LEFT("arrowLeft"),

		/** Rechteck mit eckig abgerundeten Ecken (Achteck) */
		SHAPE_OCTAGON("octagon"),

		/** Rechteck mit eckig abgerundeten Ecken (Achteck) mit Linien links und rechts */
		SHAPE_OCTAGON_DOUBLE_LINE("octagonDoubleLine"),

		/** Nach rechts zeigender Keil */
		SHAPE_WEDGE_ARROW_RIGHT("wedgeArrowRight"),

		/** Nach links zeigender Keil */
		SHAPE_WEDGE_ARROW_LEFT("wedgeArrowLeft"),

		/** Rechteck mit Linien links und rechts */
		SHAPE_RECTANGLE_DOUBLE_LINE("rectangleDoubleLine"),

		/** Rechteck mit "123" oben links */
		SHAPE_RECTANGLE_123("rectangleNumbers"),

		/** Rechteck mit "ABC" oben links */
		SHAPE_RECTANGLE_ABC("rectangleLetters"),

		/** Rechteck mit "+/-" oben links */
		SHAPE_RECTANGLE_PLUSMINUS("rectanglePlusMinus"),

		/** Abgerundetes Rechteck mit "123" oben links */
		SHAPE_ROUNDED_RECTANGLE_123("roundedRectangleNumbers"),

		/** Abgerundetes Rechteck mit "ABC" oben links */
		SHAPE_ROUNDED_RECTANGLE_ABC("roundedRectangleLetters"),

		/** Abgerundetes Rechteck mit "+/-" oben links */
		SHAPE_ROUNDED_RECTANGLE_PLUSMINUS("roundedRectanglePlusMinus"),

		/** Rechteck mit Wellenlinie auf der Unterseite */
		SHAPE_DOCUMENT("document"),

		/** Schaltfläche */
		SHAPE_BUTTON("button");

		/** Name der Form */
		public final String name;

		/**
		 * Konstruktor der Enum
		 * @param name	Name der Form
		 */
		ShapeType(final String name) {
			this.name=name;
		}
	}

	/**
	 * Im Konstruktor gewählte Form
	 * @see ShapeType
	 */
	public final ShapeType shapeType;

	/**  Zur der Form gehörendes Clipping-Objekt */
	private IntersectionClipping clipper;
	/** Cache für Formen */
	private ShapeCache cache;
	/** Cache für Schriftarten */
	private final FontCache fontCache;

	/** Füllstand bei Rechtecken (0..1) oder ein Wert &lt;0, wenn keine Füllstandsanzeige erfolgen soll */
	private double fillLevel;
	/** Farbe für den unteren Anteil der Füllstandsanzeige (oder <code>null</code> für keine Füllung) */
	private Color lowerColor;
	/** Farbverlauf für den unteren Anteil der Füllstandsanzeige */
	private GradientFill lowerFill;
	/** Farbe für den oberen Anteil der Füllstandsanzeige (oder <code>null</code> für keine Füllung) */
	private Color upperColor;
	/** Farbverlauf für den oberen Anteil der Füllstandsanzeige */
	private GradientFill upperFill;

	/** Zusätzliches Icon das auf das Shape gezeichnet werden soll (kann <code>null</code> sein) */
	private BufferedImage icon;
	/** Gemäß {@link #iconZoomedLevel} skaliertes Icon */
	private BufferedImage iconZoomed;
	/** Zoomfaktor in dem {@link #iconZoomed} vorliegt */
	private double iconZoomedLevel;

	/** Optional statt der Form zu zeichnendes Bild (kann <code>null</code> sein) */
	private BufferedImage customImage;

	/**
	 * Konstruktor der Klasse
	 * @param shapeType	Form, die gezeichnet werden soll (siehe SHAPE_*-Konstanten)
	 * @see Shapes#shapeType
	 * @see ShapeType
	 */
	public Shapes(final ShapeType shapeType) {
		this.shapeType=shapeType;
		clipper=null;
		cache=null;
		fontCache=FontCache.getFontCache();
		fillLevel=-1;
		lowerFill=null;
		upperFill=null;
		customImage=null;
	}

	/**
	 * Initialisiert den Cache für die Formen.
	 * @see #cache
	 */
	private void initCache() {
		if (cache!=null) return;
		if (clipper==null) clipper=new IntersectionClipping();
		cache=new ShapeCache(clipper) {
			@Override protected void internFill(final Graphics graphics, final Rectangle rect, final int offsetX, final int offsetY, final boolean isShadow) {Shapes.this.fill(graphics,rect,offsetX,offsetY,isShadow);}
			@Override protected Polygon getPolygonIntern(Rectangle rect) {return buildShape(rect);}
		};
	}

	/**
	 * Liefert das Objekt, das für die Einstellung des Clippings verwendet wird
	 * @return	Clipping-Steuerungs-Objekt
	 */
	public IntersectionClipping getClip() {
		if (clipper==null) clipper=new IntersectionClipping();
		return clipper;
	}

	/**
	 * Liefert das statt einer Form zu zeichnende Bild.
	 * @return	Benutzerdefiniertes Bild oder <code>null</code> wenn die reguläre Form gezeichnet wird
	 */
	public BufferedImage getCustomImage() {
		return customImage;
	}

	/**
	 * Stellt das statt einer Form zu zeichnende Bild ein.
	 * @param customImage	Benutzerdefiniertes Bild oder <code>null</code> wenn die reguläre Form gezeichnet werden soll
	 */
	public void setCustomImage(final BufferedImage customImage) {
		this.customImage=customImage;
	}

	/**
	 * Zeichnet die Füllung für eine Form
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param rect	Bereich für die Form
	 * @param offsetX	Zusätzliche Verschiebung in x-Richtung
	 * @param offsetY	Zusätzliche Verschiebung in y-Richtung
	 * @param isShadow	Handelt es sich bei der zu füllenden Fläche um den Schatten der eigentlichen Form?
	 */
	private void fill(final Graphics graphics, final Rectangle rect, final int offsetX, final int offsetY, final boolean isShadow) {
		switch (shapeType) {
		case SHAPE_RECTANGLE:
		case SHAPE_RECTANGLE_DOUBLE_LINE:
		case SHAPE_RECTANGLE_123:
		case SHAPE_RECTANGLE_ABC:
		case SHAPE_RECTANGLE_PLUSMINUS:
		case SHAPE_BUTTON:
			if (fillLevel<0 || isShadow) {
				graphics.fillRect(rect.x+offsetX,rect.y+offsetY,rect.width,rect.height);
			} else {
				final int levelY=rect.height-(int)FastMath.round(rect.height*fillLevel);
				if (lowerColor==null && upperColor==null) {
					/* Nur unteren Bereich füllen */
					graphics.fillRect(rect.x+offsetX,rect.y+offsetY+levelY,rect.width,rect.height-levelY); /* unten */
				} else {
					if (lowerFill==null) lowerFill=new GradientFill();
					if (upperFill==null) upperFill=new GradientFill();
					if (lowerColor==null) {
						/* Farbe für oberen Bereich gesetzt; unten: Standard, oben: Farbe */
						graphics.fillRect(rect.x+offsetX,rect.y+offsetY+levelY,rect.width,rect.height-levelY); /* unten */
						upperFill.set(graphics,rect,upperColor,false);
						graphics.fillRect(rect.x+offsetX,rect.y+offsetY,rect.width,levelY); /* oben */
					} else {
						if (upperColor==null) {
							/* Farbe für unteren Bereich gesetzt; unten: Farbe, oben: Standard */
							graphics.fillRect(rect.x+offsetX,rect.y+offsetY,rect.width,levelY); /* oben */
							lowerFill.set(graphics,rect,lowerColor,false);
							graphics.fillRect(rect.x+offsetX,rect.y+offsetY+levelY,rect.width,rect.height-levelY); /* unten */
						} else {
							/* Farben für beide Bereiche gesetzt */
							upperFill.set(graphics,rect,upperColor,false);
							graphics.fillRect(rect.x+offsetX,rect.y+offsetY,rect.width,levelY); /* oben */
							lowerFill.set(graphics,rect,lowerColor,false);
							graphics.fillRect(rect.x+offsetX,rect.y+offsetY+levelY,rect.width,rect.height-levelY); /* unten */
						}
					}
				}
			}
			break;
		case SHAPE_ROUNDED_RECTANGLE:
		case SHAPE_ROUNDED_RECTANGLE_123:
		case SHAPE_ROUNDED_RECTANGLE_ABC:
		case SHAPE_ROUNDED_RECTANGLE_PLUSMINUS:
			final int arc=FastMath.min(rect.width,rect.height)/2;
			graphics.fillRoundRect(rect.x+offsetX,rect.y+offsetY,rect.width,rect.height,arc,arc);
			break;
		default: /* Polygon-Formen */
			initCache();
			graphics.fillPolygon(cache.getPolygon(rect,offsetX,offsetY,0));
			break;
		}
	}

	/**
	 * Erstellt die konkrete Polygon-Form für den
	 * gewählten Formtyp ({@link #shapeType})
	 * @param rect	Rechteck das den Rahmen für die Form bildet
	 * @return	Polygon-Form
	 */
	private Polygon buildShape(final Rectangle rect) {
		final int[] xPoints;
		final int[] yPoints;

		switch (shapeType) {
		case SHAPE_ARROW_RIGHT:
			xPoints=new int[]{rect.x,rect.x+9*(rect.width)/10,rect.x+rect.width,rect.x+9*(rect.width)/10,rect.x};
			yPoints=new int[]{rect.y,rect.y,rect.y+rect.height/2,rect.y+rect.height,rect.y+rect.height};
			break;
		case SHAPE_ARROW_RIGHT_DOUBLE:
			xPoints=new int[]{rect.x,rect.x+9*(rect.width)/10,rect.x+rect.width,rect.x+9*(rect.width)/10,rect.x,rect.x+1*(rect.width)/10};
			yPoints=new int[]{rect.y,rect.y,rect.y+rect.height/2,rect.y+rect.height,rect.y+rect.height,rect.y+rect.height/2};
			break;
		case SHAPE_ARROW_LEFT:
			xPoints=new int[]{rect.x+rect.width/10,rect.x+rect.width,rect.x+rect.width,rect.x+rect.width/10,rect.x};
			yPoints=new int[]{rect.y,rect.y,rect.y+rect.height,rect.y+rect.height,rect.y+rect.height/2};
			break;
		case SHAPE_OCTAGON:
		case SHAPE_OCTAGON_DOUBLE_LINE:
			xPoints=new int[]{rect.x,rect.x+rect.width/10,rect.x+9*rect.width/10,rect.x+rect.width,rect.x+rect.width,rect.x+9*rect.width/10,rect.x+rect.width/10,rect.x};
			yPoints=new int[]{rect.y+2*rect.height/10,rect.y,rect.y,rect.y+2*rect.height/10,rect.y+8*rect.height/10,rect.y+rect.height,rect.y+rect.height,rect.y+8*rect.height/10};
			break;
		case SHAPE_WEDGE_ARROW_RIGHT:
			xPoints=new int[]{rect.x,rect.x+rect.width,rect.x+rect.width,rect.x};
			yPoints=new int[]{rect.y,rect.y+2*rect.height/10,rect.y+8*rect.height/10,rect.y+rect.height};
			break;
		case SHAPE_WEDGE_ARROW_LEFT:
			xPoints=new int[]{rect.x,rect.x+rect.width,rect.x+rect.width,rect.x};
			yPoints=new int[]{rect.y+2*rect.height/10,rect.y,rect.y+rect.height,rect.y+8*rect.height/10};
			break;
		case SHAPE_DOCUMENT:
			xPoints=new int[]{
					rect.x,
					rect.x+rect.width,
					rect.x+rect.width,
					rect.x+rect.width*9/10,
					rect.x+rect.width*8/10,
					rect.x+rect.width*7/10,
					rect.x+rect.width*6/10,
					rect.x+rect.width*5/10,
					rect.x+rect.width*4/10,
					rect.x+rect.width*3/10,
					rect.x+rect.width*2/10,
					rect.x+rect.width*1/10,
					rect.x
			};
			yPoints=new int[]{
					rect.y,
					rect.y,
					rect.y+rect.height*9/10,
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*9/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*8/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*7/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*6/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*5/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*4/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*3/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*2/10)),
					rect.y+rect.height*9/10+(int)FastMath.round(rect.height/10*Math.sin(2*Math.PI*1/10)),
					rect.y+rect.height*9/10
			};
			break;
		default:
			xPoints=new int[0];
			yPoints=new int[0];
			break;
		}

		return new Polygon(xPoints,yPoints,xPoints.length);
	}

	/**
	 * Zeichnet einen Rahmen für ein Rechteck.
	 * Es wird dabei die aktuelle Farbe verwendet.
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderWidth	Rahmenbreite
	 */
	private void drawFrameRectangle(final Graphics graphics, final Rectangle objectRect, final int borderWidth) {
		for (int i=0;i<borderWidth;i++) graphics.drawRect(objectRect.x+i,objectRect.y+i,objectRect.width-2*i,objectRect.height-2*i);
	}

	/**
	 * Farbe für den oberen Bereich des Rahmens in {@link #drawInlinedFrameRectangle(Graphics, Rectangle, int)}
	 * @see #drawInlinedFrameRectangle(Graphics, Rectangle, int)
	 */
	private static final Color inlineUpper=Color.LIGHT_GRAY;

	/**
	 * Farbe für den unteren Bereich des Rahmens in {@link #drawInlinedFrameRectangle(Graphics, Rectangle, int)}
	 * @see #drawInlinedFrameRectangle(Graphics, Rectangle, int)
	 */
	private static final Color inlineLower=Color.GRAY;

	/**
	 * Zeichnet einen Rahmen für ein Rechteck mit verschiedenen Farben für den oberen und den unteren Bereich
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderWidth	Rahmenbreite
	 */
	private void drawInlinedFrameRectangle(final Graphics graphics, final Rectangle objectRect, final int borderWidth) {
		for (int i=0;i<borderWidth;i++) graphics.drawRect(objectRect.x+i,objectRect.y+i,objectRect.width-2*i,objectRect.height-2*i);

		final Color saveColor=graphics.getColor();
		for (int i=0;i<4;i++) {
			final int x1=objectRect.x+borderWidth+i;
			final int y1=objectRect.y+borderWidth+i;
			final int x2=objectRect.x+objectRect.width-borderWidth-i;
			final int y2=objectRect.y+objectRect.height-borderWidth-i;
			graphics.setColor(inlineUpper);
			graphics.drawLine(x1,y2,x1,y1);
			graphics.drawLine(x1,y1,x2,y1);
			graphics.setColor(inlineLower);
			graphics.drawLine(x2,y1,x2,y2);
			graphics.drawLine(x2,y2,x1,y2);
		}
		graphics.setColor(saveColor);
	}

	/**
	 * Zeichnet einen abgerundeten Rahmen für ein Rechteck.
	 * Es wird dabei die aktuelle Farbe verwendet.
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderWidth	Rahmenbreite
	 */
	private void drawFrameRoundedRectangle(final Graphics graphics, final Rectangle objectRect, final int borderWidth) {
		final int arc=FastMath.min(objectRect.width,objectRect.height)/2;
		for (int i=0;i<borderWidth;i++) graphics.drawRoundRect(objectRect.x+i,objectRect.y+i,objectRect.width-2*i,objectRect.height-2*i,arc,arc);
	}

	/**
	 * Zeichnet Linien links und rechts in ein bestehendes Rechteck
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderWidth	Rahmenbreite
	 * @param deltaFactor	Abstand der Linien vom Rahmen (2=50%, 4=25%, ...)
	 */
	private void drawDoubleLine(final Graphics graphics, final Rectangle objectRect, final int borderWidth, final int deltaFactor) {
		final int w=objectRect.width/deltaFactor;
		final int y1=objectRect.y;
		final int y2=objectRect.y+objectRect.height;
		final int x1=objectRect.x+w;
		final int x2=objectRect.x+objectRect.width-w;
		for (int i=0;i<borderWidth;i++) {
			final int x=-borderWidth/2+i;
			graphics.drawLine(x1+x,y1,x1+x,y2);
			graphics.drawLine(x2+x,y1,x2+x,y2);
		}
	}

	/**
	 * Zeichnet einen Text in der linken oberen Ecke eines bestehenden Rechtecks ein
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param text	Auszugebender Text
	 * @param zoom	Zoomfaktor
	 * @param indent	Stärke der Einrückung (0..1)
	 */
	private void drawTextUpperLeftCorner(final Graphics graphics, final Rectangle objectRect, final String text, final double zoom, final double indent) {
		graphics.setFont(fontCache.getFont(FontCache.defaultFamily,Font.BOLD,(int)FastMath.round(8*zoom)));
		graphics.setColor(Color.DARK_GRAY);
		final int indentInt=(int)FastMath.round(FastMath.min(objectRect.width,objectRect.height)*indent);
		final int deltaY=graphics.getFontMetrics().getAscent();
		graphics.drawString(text,objectRect.x+indentInt,objectRect.y+deltaY+indentInt);
	}

	/**
	 * Zeichnet ein Icon in der linken oberen Ecke eines bestehenden Rechtecks ein
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderWidth	Rahmenbreite
	 * @param zoom	Zoomfaktor
	 * @see #icon
	 */
	private void drawIcon(final Graphics graphics, final Rectangle objectRect, final int borderWidth, final double zoom) {
		if (icon==null) return;
		if (iconZoomed==null || iconZoomedLevel!=zoom) {
			iconZoomed=ScaledImageCache.getScaledImageCache().getScaledImage(icon,zoom);
			iconZoomedLevel=zoom;
		}

		graphics.drawImage(iconZoomed,objectRect.x+borderWidth+1,objectRect.y+borderWidth+1,null);
	}

	/**
	 * Zeichnet eine geometrische Form
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite
	 * @param zoom	Zoomfaktor
	 * @see #draw(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 */
	private void drawFrame(final Graphics graphics, final Rectangle objectRect, final Color borderColor, final int borderWidth, final double zoom) {
		if (borderColor==null) return;
		graphics.setColor(borderColor);

		switch (shapeType) {
		case SHAPE_RECTANGLE:
			drawFrameRectangle(graphics,objectRect,borderWidth);
			break;
		case SHAPE_ROUNDED_RECTANGLE:
			drawFrameRoundedRectangle(graphics,objectRect,borderWidth);
			break;
		case SHAPE_RECTANGLE_DOUBLE_LINE:
			drawFrameRectangle(graphics,objectRect,borderWidth);
			drawDoubleLine(graphics,objectRect,borderWidth,15);
			break;
		case SHAPE_RECTANGLE_123:
			drawFrameRectangle(graphics,objectRect,borderWidth);
			drawTextUpperLeftCorner(graphics,objectRect,"123",zoom,0.05);
			break;
		case SHAPE_RECTANGLE_ABC:
			drawFrameRectangle(graphics,objectRect,borderWidth);
			drawTextUpperLeftCorner(graphics,objectRect,"ABC",zoom,0.05);
			break;
		case SHAPE_RECTANGLE_PLUSMINUS:
			drawFrameRectangle(graphics,objectRect,borderWidth);
			drawTextUpperLeftCorner(graphics,objectRect,"+/-",zoom,0.05);
			break;
		case SHAPE_ROUNDED_RECTANGLE_123:
			drawFrameRoundedRectangle(graphics,objectRect,borderWidth);
			drawTextUpperLeftCorner(graphics,objectRect,"123",zoom,0.1);
			break;
		case SHAPE_ROUNDED_RECTANGLE_ABC:
			drawFrameRoundedRectangle(graphics,objectRect,borderWidth);
			drawTextUpperLeftCorner(graphics,objectRect,"ABC",zoom,0.1);
			break;
		case SHAPE_ROUNDED_RECTANGLE_PLUSMINUS:
			drawFrameRoundedRectangle(graphics,objectRect,borderWidth);
			drawTextUpperLeftCorner(graphics,objectRect,"+/-",zoom,0.1);
			break;
		case SHAPE_OCTAGON_DOUBLE_LINE:
			initCache();
			for (int i=0;i<borderWidth;i++) graphics.drawPolygon(cache.getPolygon(objectRect,0,0,i));
			drawDoubleLine(graphics,objectRect,borderWidth,10);
			break;
		case SHAPE_BUTTON:
			drawInlinedFrameRectangle(graphics,objectRect,borderWidth);
			break;
		default: /* Polygon-Formen */
			initCache();
			for (int i=0;i<borderWidth;i++) graphics.drawPolygon(cache.getPolygon(objectRect,0,0,i));
			break;
		}
	}

	/**
	 * Zeichnet statt einer Form ein nutzerdefiniertes Bild
	 * @param graphics	Grafik-Ausgabeobjekt
	 * @param objectRect	Rechteck
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite
	 * @param zoom	Zoomfaktor
	 * @see #draw(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 * @see #customImage
	 */
	private void drawCustomImage(final Graphics graphics, final Rectangle objectRect, final Color borderColor, final int borderWidth, final double zoom) {
		final int w=(int)(100*zoom);
		final int h=(int)(50*zoom);
		final BufferedImage imageZoomed=ScaledImageCache.getScaledImageCache().getScaledImage(customImage,w,h);

		graphics.drawImage(imageZoomed,objectRect.x,objectRect.y,null);

		if (borderColor!=null && !borderColor.equals(Color.DARK_GRAY)) {
			graphics.setColor(borderColor);
			drawFrameRectangle(graphics,objectRect,borderWidth);
		}
	}

	/**
	 * Zeichnet die im Konstruktor gewählte Form.
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param drawRect	Gültiger Zeichenbereich des übergeordneten <code>JViewPort</code>-Elements
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (bezogen auf 100% Zoom)
	 * @param fillColor	Füllfarbe (aus der ggf. ein Farbverlauf berechnet wird)
	 * @param zoom	Zoomfaktor
	 * @param stage	Zeichenstufe (Stufe 1: Hintergrund und Schatten, Stufe 2: Rahmen)
	 */
	public synchronized void draw(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect, final Color borderColor, final int borderWidth, final Color fillColor, final double zoom, final int stage) {
		if (shapeType==ShapeType.SHAPE_NONE) return;

		switch (stage) {
		case 1:
			if (customImage==null) {
				initCache();
				cache.fill(graphics,drawRect,objectRect,fillColor,zoom);
			}
			break;
		case 2:
			if (customImage==null) {
				drawFrame(graphics,objectRect,borderColor,borderWidth,zoom);
			} else {
				drawCustomImage(graphics,objectRect,borderColor,borderWidth,zoom);
			}
			if (icon!=null) {
				drawIcon(graphics,objectRect,borderWidth,zoom);
			}
			break;
		}
	}

	/**
	 * Stellt bei den Rechtecken den Füllstand ein
	 * @param partialFillLevel	Füllstation (Werte zwischen 0 und 1 jeweils einschließlich) oder ein Wert &lt;0, um keine Füllstandsanzeige zu verwenden
	 * @see Shapes#setPartialFillColors(Color, Color)
	 */
	public void setFillLevel(final double partialFillLevel) {
		fillLevel=FastMath.min(1,partialFillLevel); /* Werte<0 sind zulässig: Diese schalten das System auf den Standardfall zurück. */
	}

	/**
	 * Stellt die Farben für die Füllstandsanzeige von Rechtecken ein
	 * @param lower	Farbe für den unteren Bereich (<code>null</code> bedeutet, dass die Standardfarbe verwendet werden soll)
	 * @param upper	Farbe für den oberen Bereich (<code>null</code> bedeutet, dass die Standardfarbe verwendet werden soll)
	 * @see Shapes#setFillLevel(double)
	 */
	public void setPartialFillColors(final Color lower, final Color upper) {
		lowerColor=lower;
		upperColor=upper;
	}

	/**
	 * Liefert das zusätzliche Icon, das auf das Shape gezeichnet wird
	 * @return	Zusätzliches Icon (kann auch <code>null</code> sein)
	 */
	public BufferedImage getAdditionalIcon() {
		return icon;
	}

	/**
	 * Stellt das zusätzliche Icon, das auf das Shape gezeichnet wird, ein.
	 * @param icon	Zusätzliches Icon (kann auch <code>null</code> sein)
	 */
	public void setAdditionalIcon(final BufferedImage icon) {
		this.icon=icon;
		iconZoomed=null;
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines Polygons mit Schatten zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Funktion
	 */
	private String getHTMLShadowPolygon(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());

		final StringBuilder sb=new StringBuilder();
		sb.append("function drawShadowPolygon(rect,points,shadowX,shadowY,shadowColor) {\n");

		sb.append("  var movedPoints=[];\n");
		sb.append("  for (var i=0;i<points.length;i++) movedPoints.push({x: points[i].x+shadowX, y: points[i].y+shadowY});\n");
		sb.append("  drawPolygon(rect,movedPoints,\"#FFFFFF\",0,shadowColor);\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines Polygons zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLPolygon() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawPolygon(rect,points,borderColor,borderWidth,fillColor1,fillColor2) {\n");

		sb.append("  if (typeof(fillColor1)!=\"undefined\") {\n");
		sb.append("    if (typeof(fillColor2)!=\"undefined\") {\n");
		sb.append("      var gradient=context.createLinearGradient(rect.x,Math.round(rect.y+rect.h/2),rect.x+rect.w,Math.round(rect.y+rect.h/2));\n");
		sb.append("      gradient.addColorStop(0,fillColor1);\n");
		sb.append("      gradient.addColorStop(1,fillColor2);\n");
		sb.append("      context.fillStyle=gradient;\n");
		sb.append("    } else {\n");
		sb.append("      context.fillStyle=fillColor1;\n");
		sb.append("    }\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(points[0].x,points[0].y);\n");
		sb.append("    for (var i=1;i<points.length;i++) context.lineTo(points[i].x,points[i].y);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(points[0].x,points[0].y);\n");
		sb.append("    for (var i=1;i<points.length;i++) context.lineTo(points[i].x,points[i].y);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines Rechtecks mit Schatten zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Funktion
	 */
	private String getHTMLShadowRectangle(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("rectangleInt",builder->getHTMLRectangle());

		final StringBuilder sb=new StringBuilder();
		sb.append("function drawShadowRectangleInt(rect,shadowX,shadowY,shadowColor) {\n");

		sb.append("  drawRectangleInt({x: rect.x+shadowX, y: rect.y+shadowY, w: rect.w, h: rect.h},\"#FFFFFF\",0,shadowColor);\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines Rechtecks zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLRectangle() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2) {\n");

		sb.append("  if (typeof(fillColor1)!=\"undefined\") {\n");
		sb.append("    if (typeof(fillColor2)!=\"undefined\") {\n");
		sb.append("      var gradient=context.createLinearGradient(rect.x,Math.round(rect.y+rect.h/2),rect.x+rect.w,Math.round(rect.y+rect.h/2));\n");
		sb.append("      gradient.addColorStop(0,fillColor1);\n");
		sb.append("      gradient.addColorStop(1,fillColor2);\n");
		sb.append("      context.fillStyle=gradient;\n");
		sb.append("    } else {\n");
		sb.append("      context.fillStyle=fillColor1;\n");
		sb.append("    }\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines partiell gefüllten (=Füllstand) Rechtecks zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLRectanglePartial() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawRectanglePartialInt(rect,borderColor,borderWidth,lowerColor,upperColor,fillLevel) {\n");

		sb.append("  var levelY=rect.h-Math.round(rect.h*Math.min(1,Math.max(0,fillLevel)));\n");

		sb.append("  context.fillStyle=upperColor;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+levelY);\n");
		sb.append("  context.lineTo(rect.x,rect.y+levelY);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  context.fillStyle=lowerColor;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y+levelY);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+levelY);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("  context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines Rechtecks mit innerem Schatten zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLRectangleInline() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawRectangleInlineInt(rect,borderColor,borderWidth,fillColor1,fillColor2) {\n");

		sb.append("  if (typeof(fillColor1)!=\"undefined\") {\n");
		sb.append("    if (typeof(fillColor2)!=\"undefined\") {\n");
		sb.append("      var gradient=context.createLinearGradient(rect.x,Math.round(rect.y+rect.h/2),rect.x+rect.w,Math.round(rect.y+rect.h/2));\n");
		sb.append("      gradient.addColorStop(0,fillColor1);\n");
		sb.append("      gradient.addColorStop(1,fillColor2);\n");
		sb.append("      context.fillStyle=gradient;\n");
		sb.append("    } else {\n");
		sb.append("      context.fillStyle=fillColor1;\n");
		sb.append("    }\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("    context.strokeStyle=\"LightGray\";\n");
		sb.append("    context.lineWidth=2;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x+borderWidth,rect.y+rect.h-borderWidth);\n");
		sb.append("    context.lineTo(rect.x+borderWidth,rect.y+borderWidth);\n");
		sb.append("    context.lineTo(rect.x+rect.w-borderWidth,rect.y+borderWidth);\n");
		sb.append("    context.stroke();\n");
		sb.append("    context.strokeStyle=\"DarkGrey\";\n");
		sb.append("    context.lineWidth=2;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x+rect.w-borderWidth,rect.y+borderWidth);\n");
		sb.append("    context.lineTo(rect.x+rect.w-borderWidth,rect.y+rect.h-borderWidth);\n");
		sb.append("    context.lineTo(rect.x+borderWidth,rect.y+rect.h-borderWidth);\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe eines Rechtecks mit innerem Schatten und teilweiser Füllung (=Füllstand) zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLRectangleInlinePartial() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawRectangleInlinePartialInt(rect,borderColor,borderWidth,lowerColor,upperColor,fillLevel) {\n");

		sb.append("  var levelY=rect.h-Math.round(rect.h*Math.min(1,Math.max(0,fillLevel)));\n");

		sb.append("  context.fillStyle=upperColor;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+levelY);\n");
		sb.append("  context.lineTo(rect.x,rect.y+levelY);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  context.fillStyle=lowerColor;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x,rect.y+levelY);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+levelY);\n");
		sb.append("  context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("  context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("  context.closePath();\n");
		sb.append("  context.fill();\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("    context.strokeStyle=\"LightGray\";\n");
		sb.append("    context.lineWidth=2;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x+borderWidth,rect.y+rect.h-borderWidth);\n");
		sb.append("    context.lineTo(rect.x+borderWidth,rect.y+borderWidth);\n");
		sb.append("    context.lineTo(rect.x+rect.w-borderWidth,rect.y+borderWidth);\n");
		sb.append("    context.stroke();\n");
		sb.append("    context.strokeStyle=\"DarkGrey\";\n");
		sb.append("    context.lineWidth=2;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x+rect.w-borderWidth,rect.y+borderWidth);\n");
		sb.append("    context.lineTo(rect.x+rect.w-borderWidth,rect.y+rect.h-borderWidth);\n");
		sb.append("    context.lineTo(rect.x+borderWidth,rect.y+rect.h-borderWidth);\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe von zusätzlichen Linien innerhalb eines Rechtecks zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLDrawDoubleLine() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawDoubleLine(rect,borderColor,borderWidth,delta) {\n");
		sb.append("  var w=Math.round(rect.w/delta);\n");
		sb.append("  context.strokeStyle=borderColor;\n");
		sb.append("  context.lineWidth=borderWidth;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x+w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+w,rect.y+rect.h);\n");
		sb.append("  context.stroke();\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(rect.x+rect.w-w,rect.y);\n");
		sb.append("  context.lineTo(rect.x+rect.w-w,rect.y+rect.h);\n");
		sb.append("  context.stroke();\n");
		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zur Ausgabe von Text auf der Zeichenfläche zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLDrawText() {
		final StringBuilder sb=new StringBuilder();
		sb.append("function drawTextUpperLeftCorner(rect,text,indent) {\n");

		sb.append("  context.font=\"8px Verdana,Lucida,sans-serif\";\n");
		sb.append("  context.textAlign=\"left\";\n");
		sb.append("  context.textBaseline=\"hanging\";\n");
		sb.append("  context.fillStyle=\"DarkGrey\";\n");
		sb.append("  context.fillText(text,rect.x+Math.round(rect.w*indent),rect.y+Math.round(rect.h*indent));\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zum Erstellen eines abgerundeten Rechecks zur Ausgabe des Modells als HTML-Datei
	 * @return	Javascript-Funktion
	 */
	private String getHTMLRoundedRectangle() {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawRoundedRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2) {\n");
		sb.append("  var w=Math.round(Math.min(rect.w,rect.h)/5);\n");

		sb.append("  if (typeof(fillColor1)!=\"undefined\") {\n");
		sb.append("    if (typeof(fillColor2)!=\"undefined\") {\n");
		sb.append("      var gradient=context.createLinearGradient(rect.x,Math.round(rect.y+rect.h/2),rect.x+rect.w,Math.round(rect.y+rect.h/2));\n");
		sb.append("      gradient.addColorStop(0,fillColor1);\n");
		sb.append("      gradient.addColorStop(1,fillColor2);\n");
		sb.append("      context.fillStyle=gradient;\n");
		sb.append("    } else {\n");
		sb.append("      context.fillStyle=fillColor1;\n");
		sb.append("    }\n");
		sb.append("    context.beginPath();\n");

		sb.append("    context.moveTo(rect.x+rect.w-w,rect.y);\n");
		sb.append("    context.arcTo(rect.x+rect.w,rect.y,rect.x+rect.w,rect.y+w,w);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h-w);\n");
		sb.append("    context.arcTo(rect.x+rect.w,rect.y+rect.h,rect.x+rect.w-w,rect.y+rect.h,w);\n");
		sb.append("    context.lineTo(rect.x+w,rect.y+rect.h);\n");
		sb.append("    context.arcTo(rect.x,rect.y+rect.h,rect.x,rect.y+rect.h-w,w);\n");
		sb.append("    context.lineTo(rect.x,rect.y+w);\n");
		sb.append("    context.arcTo(rect.x,rect.y,rect.x+w,rect.y,w);\n");
		sb.append("    context.lineTo(rect.x+rect.w-w,rect.y);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x+rect.w-w,rect.y);\n");
		sb.append("    context.arcTo(rect.x+rect.w,rect.y,rect.x+rect.w,rect.y+w,w);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h-w);\n");
		sb.append("    context.arcTo(rect.x+rect.w,rect.y+rect.h,rect.x+rect.w-w,rect.y+rect.h,w);\n");
		sb.append("    context.lineTo(rect.x+w,rect.y+rect.h);\n");
		sb.append("    context.arcTo(rect.x,rect.y+rect.h,rect.x,rect.y+rect.h-w,w);\n");
		sb.append("    context.lineTo(rect.x,rect.y+w);\n");
		sb.append("    context.arcTo(rect.x,rect.y,rect.x+w,rect.y,w);\n");
		sb.append("    context.lineTo(rect.x+rect.w-w,rect.y);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert eine Javascript-Funktion zum Erstellen eines abgerundeten Rechecks mit Schatten zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Funktion
	 */
	private String getHTMLShadowRoundedRectangle(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("roundedRectangleInt",builder->getHTMLRoundedRectangle());

		final StringBuilder sb=new StringBuilder();
		sb.append("function drawShadowRoundedRectangleInt(rect,shadowX,shadowY,shadowColor) {\n");

		sb.append("  drawRoundedRectangleInt({x: rect.x+shadowX, y: rect.y+shadowY, w: rect.w, h: rect.h},\"#FFFFFF\",0,shadowColor);\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Liefert den Namen für die Javascript-Funktion zum Zeichnen der Form
	 * @return	Name für die Javascript-Funktion zum Zeichnen der Form
	 * @see #getHTMLShape(HTMLOutputBuilder)
	 */
	private String getHTMLShapeDrawFunctionName() {
		return "draw"+shapeType.name.substring(0,1).toUpperCase()+shapeType.name.substring(1);
	}

	/**
	 * Liefert die Javascript-Daten für eine Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLShape(final HTMLOutputBuilder outputBuilder) {
		final SetupData setup=SetupData.getSetup();
		final StringBuilder sb=new StringBuilder();

		/*
		String name=shapeType.name;
		name=name.substring(0,1).toUpperCase()+name.substring(1);
		 */
		sb.append("function "+getHTMLShapeDrawFunctionName()+"(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel) {\n");

		switch (shapeType) {
		case SHAPE_NONE:
			/* Nichts zeichnen */
			break;
		case SHAPE_RECTANGLE:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRectangleInt",builder->getHTMLShadowRectangle(builder));
			outputBuilder.addJSUserFunction("rectangleInt",builder->getHTMLRectangle());
			outputBuilder.addJSUserFunction("rectanglePartialInt",builder->getHTMLRectanglePartial());
			if (setup.useShadows) sb.append("  drawShadowRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  if (typeof(fillLevel)==\"undefined\" || fillLevel<0) {\n");
			sb.append("    drawRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  } else {\n");
			sb.append("    drawRectanglePartialInt(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel);\n");
			sb.append("  }\n");
			break;
		case SHAPE_RECTANGLE_DOUBLE_LINE:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRectangleInt",builder->getHTMLShadowRectangle(builder));
			outputBuilder.addJSUserFunction("rectangleInt",builder->getHTMLRectangle());
			outputBuilder.addJSUserFunction("rectanglePartialInt",builder->getHTMLRectanglePartial());
			outputBuilder.addJSUserFunction("doubleLine",builder->getHTMLDrawDoubleLine());
			if (setup.useShadows) sb.append("  drawShadowRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  if (typeof(fillLevel)==\"undefined\" || fillLevel<0) {\n");
			sb.append("    drawRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  } else {\n");
			sb.append("    drawRectanglePartialInt(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel);\n");
			sb.append("  }\n");
			sb.append("  drawDoubleLine(rect,borderColor,borderWidth,15);\n");
			break;
		case SHAPE_RECTANGLE_123:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRectangleInt",builder->getHTMLShadowRectangle(builder));
			outputBuilder.addJSUserFunction("rectangleInt",builder->getHTMLRectangle());
			outputBuilder.addJSUserFunction("rectanglePartialInt",builder->getHTMLRectanglePartial());
			outputBuilder.addJSUserFunction("text",builder->getHTMLDrawText());
			if (setup.useShadows) sb.append("  drawShadowRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  if (typeof(fillLevel)==\"undefined\" || fillLevel<0) {\n");
			sb.append("    drawRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  } else {\n");
			sb.append("    drawRectanglePartialInt(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel);\n");
			sb.append("  }\n");
			sb.append("  drawTextUpperLeftCorner(rect,\"123\",0.05);\n");
			break;
		case SHAPE_RECTANGLE_ABC:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRectangleInt",builder->getHTMLShadowRectangle(builder));
			outputBuilder.addJSUserFunction("rectangleInt",builder->getHTMLRectangle());
			outputBuilder.addJSUserFunction("rectanglePartialInt",builder->getHTMLRectanglePartial());
			outputBuilder.addJSUserFunction("text",builder->getHTMLDrawText());
			if (setup.useShadows) sb.append("  drawShadowRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  if (typeof(fillLevel)==\"undefined\" || fillLevel<0) {\n");
			sb.append("    drawRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  } else {\n");
			sb.append("    drawRectanglePartialInt(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel);\n");
			sb.append("  }\n");
			sb.append("  drawTextUpperLeftCorner(rect,\"ABC\",0.05);\n");
			break;
		case SHAPE_RECTANGLE_PLUSMINUS:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRectangleInt",builder->getHTMLShadowRectangle(builder));
			outputBuilder.addJSUserFunction("rectangleInt",builder->getHTMLRectangle());
			outputBuilder.addJSUserFunction("rectanglePartialInt",builder->getHTMLRectanglePartial());
			outputBuilder.addJSUserFunction("text",builder->getHTMLDrawText());
			if (setup.useShadows) sb.append("  drawShadowRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  if (typeof(fillLevel)==\"undefined\" || fillLevel<0) {\n");
			sb.append("    drawRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  } else {\n");
			sb.append("    drawRectanglePartialInt(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel);\n");
			sb.append("  }\n");
			sb.append("  drawTextUpperLeftCorner(rect,\"+/-\",0.05);\n");
			break;
		case SHAPE_ROUNDED_RECTANGLE:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRoundedRectangleInt",builder->getHTMLShadowRoundedRectangle(builder));
			outputBuilder.addJSUserFunction("roundedRectangleInt",builder->getHTMLRoundedRectangle());
			if (setup.useShadows) sb.append("  drawShadowRoundedRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawRoundedRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_ROUNDED_RECTANGLE_123:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRoundedRectangleInt",builder->getHTMLShadowRoundedRectangle(builder));
			outputBuilder.addJSUserFunction("roundedRectangleInt",builder->getHTMLRoundedRectangle());
			outputBuilder.addJSUserFunction("text",builder->getHTMLDrawText());
			if (setup.useShadows) sb.append("  drawShadowRoundedRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawRoundedRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  drawTextUpperLeftCorner(rect,\"123\",0.1);\n");
			break;
		case SHAPE_ROUNDED_RECTANGLE_ABC:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRoundedRectangleInt",builder->getHTMLShadowRoundedRectangle(builder));
			outputBuilder.addJSUserFunction("roundedRectangleInt",builder->getHTMLRoundedRectangle());
			outputBuilder.addJSUserFunction("text",builder->getHTMLDrawText());
			if (setup.useShadows) sb.append("  drawShadowRoundedRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawRoundedRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  drawTextUpperLeftCorner(rect,\"ABC\",0.1);\n");
			break;
		case SHAPE_ROUNDED_RECTANGLE_PLUSMINUS:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRoundedRectangleInt",builder->getHTMLShadowRoundedRectangle(builder));
			outputBuilder.addJSUserFunction("roundedRectangleInt",builder->getHTMLRoundedRectangle());
			outputBuilder.addJSUserFunction("text",builder->getHTMLDrawText());
			if (setup.useShadows) sb.append("  drawShadowRoundedRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawRoundedRectangleInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  drawTextUpperLeftCorner(rect,\"+/-\",0.1);\n");
			break;
		case SHAPE_OCTAGON:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var w=Math.round(rect.w/10);\n");
			sb.append("  var h=Math.round(rect.h/10);\n");
			sb.append("  var polygon=[{x: rect.x+w, y: rect.y},{x: rect.x+rect.w-w, y: rect.y},{x: rect.x+rect.w, y: rect.y+h},{x: rect.x+rect.w, y: rect.y+rect.h-h},{x: rect.x+rect.w-w, y: rect.y+rect.h},{x: rect.x+w, y: rect.y+rect.h},{x: rect.x, y: rect.y+rect.h-h},{x: rect.x, y: rect.y+h}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_OCTAGON_DOUBLE_LINE:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			outputBuilder.addJSUserFunction("doubleLine",builder->getHTMLDrawDoubleLine());
			sb.append("  var w=Math.round(rect.w/10);\n");
			sb.append("  var h=Math.round(rect.h/10);\n");
			sb.append("  var polygon=[{x: rect.x+w, y: rect.y},{x: rect.x+rect.w-w, y: rect.y},{x: rect.x+rect.w, y: rect.y+h},{x: rect.x+rect.w, y: rect.y+rect.h-h},{x: rect.x+rect.w-w, y: rect.y+rect.h},{x: rect.x+w, y: rect.y+rect.h},{x: rect.x, y: rect.y+rect.h-h},{x: rect.x, y: rect.y+h}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  drawDoubleLine(rect,borderColor,borderWidth,15);\n");
			break;
		case SHAPE_ARROW_LEFT:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var w=Math.round(rect.w/10);\n");
			sb.append("  var polygon=[{x: rect.x+w, y: rect.y},{x: rect.x+rect.w, y: rect.y},{x: rect.x+rect.w, y: rect.y+rect.h},{x: rect.x+w, y: rect.y+rect.h},{x: rect.x, y: rect.y+Math.round(rect.h/2)}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_ARROW_RIGHT:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var w=Math.round(rect.w/10);\n");
			sb.append("  var polygon=[{x: rect.x, y: rect.y},{x: rect.x+rect.w-w, y: rect.y},{x: rect.x+rect.w, y: rect.y+Math.round(rect.h/2)},{x: rect.x+rect.w-w, y: rect.y+rect.h},{x: rect.x, y: rect.y+rect.h}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_ARROW_RIGHT_DOUBLE:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var w=Math.round(rect.w/10);\n");
			sb.append("  var polygon=[{x: rect.x,y: rect.y},{x: rect.x+rect.w-w,y: rect.y},{x: rect.x+rect.w,y: rect.y+Math.round(rect.h/2)},{x: rect.x+rect.w-w,y: rect.y+rect.h},{x: rect.x,y: rect.y+rect.h},{x: rect.x+w,y: rect.y+Math.round(rect.h/2)}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_WEDGE_ARROW_LEFT:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var h=Math.round(rect.h/5);\n");
			sb.append("  var polygon=[{x: rect.x, y: rect.y+h},{x: rect.x+rect.w, y: rect.y},{x: rect.x+rect.w,y: rect.y+rect.h},{x: rect.x,y: rect.y+rect.h-h}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_WEDGE_ARROW_RIGHT:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var h=Math.round(rect.h/5);\n");
			sb.append("  var polygon=[{x: rect.x,y: rect.y},{x: rect.x+rect.w,y: rect.y+h},{x: rect.x+rect.w,y: rect.y+rect.h-h},{x: rect.x,y: rect.y+rect.h}];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_DOCUMENT:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowPolygon",builder->getHTMLShadowPolygon(builder));
			outputBuilder.addJSUserFunction("polygon",builder->getHTMLPolygon());
			sb.append("  var polygon=[\n");
			sb.append("    {x: rect.x, y: rect.y},\n");
			sb.append("    {x: rect.x+rect.w, y: rect.y},\n");
			sb.append("    {x: rect.x+rect.w, y: rect.y+Math.round(rect.h*9/10)},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*9/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*9/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*8/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*8/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*7/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*7/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*6/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*6/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*5/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*5/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*4/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*4/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*3/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*3/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*2/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*2/10))},\n");
			sb.append("    {x: rect.x+Math.round(rect.w*1/10), y: rect.y+Math.round(rect.h*9/10+rect.h/10*Math.sin(2*Math.PI*1/10))},\n");
			sb.append("    {x: rect.x, y: rect.y+Math.round(rect.h*9/10)}\n");
			sb.append("];\n");
			if (setup.useShadows) sb.append("  drawShadowPolygon(rect,polygon,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  drawPolygon(rect,polygon,borderColor,borderWidth,fillColor1,fillColor2);\n");
			break;
		case SHAPE_BUTTON:
			if (setup.useShadows) outputBuilder.addJSUserFunction("shadowRectangleInt",builder->getHTMLShadowRectangle(builder));
			outputBuilder.addJSUserFunction("rectangleInlineInt",builder->getHTMLRectangleInline());
			outputBuilder.addJSUserFunction("rectangleInlinePartialInt",builder->getHTMLRectangleInlinePartial());
			if (setup.useShadows) sb.append("  drawShadowRectangleInt(rect,"+(SHADOW_WIDTH*SHADOW_DIRECTION_X)+","+(SHADOW_WIDTH*SHADOW_DIRECTION_Y)+",\""+HTMLOutputBuilder.colorToHTML(SHADOW_COLOR)+"\");\n");
			sb.append("  if (typeof(fillLevel)==\"undefined\" || fillLevel<0) {\n");
			sb.append("    drawRectangleInlineInt(rect,borderColor,borderWidth,fillColor1,fillColor2);\n");
			sb.append("  } else {\n");
			sb.append("    drawRectangleInlinePartialInt(rect,borderColor,borderWidth,fillColor1,fillColor2,fillLevel);\n");
			sb.append("  }\n");
			break;
		}

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet die Form in einen {@link HTMLOutputBuilder} ein.
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite
	 * @param fillColor	Füllfarbe (aus der ggf. ein Farbverlauf berechnet wird)
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder, final Rectangle objectRect, final Color borderColor, final int borderWidth, final Color fillColor) {
		outputBuilder.addJSUserFunction(shapeType.name,builder->getHTMLShape(builder));

		final String rect="{x: "+objectRect.x+", y: "+objectRect.y+", w: "+objectRect.width+", h: "+objectRect.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";
		if (fillColor==null) {
			outputBuilder.outputBody.append(getHTMLShapeDrawFunctionName()+"("+rect+","+border+","+borderWidth+");\n");
		} else {
			if (fillLevel>=0) {
				/* Zweiteilig ausfüllen */
				Color c1=lowerColor;
				Color c2=upperColor;
				if (c1==null) c1=fillColor;
				if (c2==null) c2=new Color(230,230,230);
				final String fill1="\""+HTMLOutputBuilder.colorToHTML(c1)+"\"";
				final String fill2="\""+HTMLOutputBuilder.colorToHTML(c2)+"\"";
				outputBuilder.outputBody.append(getHTMLShapeDrawFunctionName()+"("+rect+","+border+","+borderWidth+","+fill1+","+fill2+","+NumberTools.formatSystemNumber(fillLevel)+");\n");
			} else {
				/* Normal ausfüllen */
				if (SetupData.getSetup().useGradients) {
					final String fill1="\""+HTMLOutputBuilder.colorToHTML(fillColor)+"\"";
					BrighterColor color2=new BrighterColor();
					final String fill2="\""+HTMLOutputBuilder.colorToHTML(color2.get(fillColor))+"\"";
					outputBuilder.outputBody.append(getHTMLShapeDrawFunctionName()+"("+rect+","+border+","+borderWidth+","+fill1+","+fill2+");\n");
				} else {
					final String fill="\""+HTMLOutputBuilder.colorToHTML(fillColor)+"\"";
					outputBuilder.outputBody.append(getHTMLShapeDrawFunctionName()+"("+rect+","+border+","+borderWidth+","+fill+");\n");
				}
			}
		}
	}

	/**
	 * Zeichnet die Form in einen {@link SpecialOutputBuilder} ein.
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite
	 * @param fillColor	Füllfarbe (aus der ggf. ein Farbverlauf berechnet wird)
	 */
	public void specialOutput(final SpecialOutputBuilder outputBuilder, final Rectangle objectRect, final Color borderColor, final int borderWidth, final Color fillColor) {
		if (outputBuilder instanceof HTMLOutputBuilder) {
			specialOutputHTML((HTMLOutputBuilder)outputBuilder,objectRect,borderColor,borderWidth,fillColor);
			return;
		}
	}
}