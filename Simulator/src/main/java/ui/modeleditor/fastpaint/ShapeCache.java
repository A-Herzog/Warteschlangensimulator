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
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

import tools.SetupData;

/**
 * Speichert Objektformen zwischen, um Speicheranforderungen einzusparen.
 * @author Alexander Herzog
 * @see Shapes
 */
public abstract class ShapeCache {
	/** Referenz auf das Setup-Singleton */
	private final SetupData setup;
	/** Objekt für die Farbverläufe */
	private GradientFill filler;
	/**  Zu der Zeichnung gehörendes Clipping-Objekt */
	private final IntersectionClipping clipper;

	/**
	 * Konstruktor der Klasse
	 * @param clipper	Zu der Zeichnung gehörendes Clipping-Objekt, das beim Aufruf von {@link #fill(Graphics, Rectangle, Rectangle, Color, double)} verwendet wird
	 */
	public ShapeCache(final IntersectionClipping clipper) {
		setup=SetupData.getSetup();
		filler=null;
		this.clipper=clipper;
	}

	/**
	 * Zwischenspeichern des Objektes zum Aufstellen des Rechteckbereichs
	 * @see #getPolygon(Rectangle, int, int, int)
	 */
	private Rectangle polygonRect=null;

	/**
	 * Rechteckbereich beim letzten Aufruf von {@link #getPolygon(Rectangle, int, int, int)}
	 * @see #getPolygon(Rectangle, int, int, int)
	 */
	private Rectangle lastPolygonRect=null;

	/**
	 * Letztes Polygon
	 * @see #getPolygon(Rectangle, int, int, int)
	 */
	private Polygon lastPolygon;

	/**
	 * Letztes verschobenes Rechteck
	 * @see #getPolygon(Rectangle, int, int, int)
	 */
	private Rectangle lastPolygonMovedRect=null;

	/**
	 * Letztes verschobenes Polygon
	 * @see #getPolygon(Rectangle, int, int, int)
	 */
	private Polygon lastPolygonMoved;

	/**
	 * Liefert ein Polygon zum Zeichnen entweder aus dem Cache oder indem es intern angefordert wird
	 * @param rect	Rahmen für das Polygon
	 * @param offsetX	Verschiebung in x-Richtung
	 * @param offsetY	Verschiebung in y-Richtung
	 * @param smaller	Anzahl an Pixeln, die das Polygon in alle Richtung kleiner als im Rahmen angegeben sein soll
	 * @return	Polygon zur Verwendung für die Zeichnung
	 * @see ShapeCache#getPolygonIntern(Rectangle)
	 */
	public synchronized Polygon getPolygon(final Rectangle rect, final int offsetX, final int offsetY, final int smaller) {
		if (polygonRect==null) polygonRect=new Rectangle();
		if (lastPolygonRect==null) lastPolygonRect=new Rectangle();
		if (lastPolygonMovedRect==null) lastPolygonMovedRect=new Rectangle();

		FastPaintTools.copyRectangleData(rect,polygonRect);
		polygonRect.x+=(offsetX+smaller);
		polygonRect.y+=(offsetX+smaller);
		polygonRect.width-=(2*smaller);
		polygonRect.height-=(2*smaller);

		if (offsetX==0 && offsetY==0) {
			if (lastPolygon==null || !lastPolygonRect.equals(polygonRect)) {
				lastPolygon=getPolygonIntern(polygonRect);
				FastPaintTools.copyRectangleData(polygonRect,lastPolygonRect);
			}
			return lastPolygon;
		} else {
			if (lastPolygonMoved==null || !lastPolygonMovedRect.equals(polygonRect)) {
				lastPolygonMoved=getPolygonIntern(polygonRect);
				FastPaintTools.copyRectangleData(polygonRect,lastPolygonMovedRect);
			}
			return lastPolygonMoved;
		}
	}

	/**
	 * Wird intern von {@link ShapeCache#getPolygon(Rectangle, int, int, int)} aufgerufen, um ein Polygon zu erstellen
	 * @param rect	Größe des Rahmens (bei dem Verschiebungen und Co. schon eingerechnet sind)
	 * @return	Polygon zur Verwendung für die Zeichnung
	 */
	protected abstract Polygon getPolygonIntern(final Rectangle rect);

	/**
	 * Wird intern von {@link ShapeCache#fill(Graphics, Rectangle, Rectangle, Color, double)} aufgerufen, um die Form gefüllt oder den Schatten zu zeichnen.
	 * Die Berechnung der Verschiebung für den Schatten und die Wahl der Farbe erfolgt bereits durch den Aufrufer.
	 * Diese Funktion muss nur zeichnen.
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param rect	Rahmen für die Form
	 * @param offsetX	Verschiebung in x-Richtung
	 * @param offsetY	Verschiebung in y-Richtung
	 * @param isShadow	Gibt an, ob gerade das Shape oder der Schatten gezeichnet werden
	 * @see ShapeCache#fill(Graphics, Rectangle, Rectangle, Color, double)
	 */
	protected abstract void internFill(final Graphics graphics, final Rectangle rect, final int offsetX, final int offsetY, final boolean isShadow);

	/**
	 * Wird aufgerufen, wenn die Form gezeichnet werden soll (Füllung und Schatten).
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param drawRect	Gültiger Zeichenbereich des übergeordneten <code>JViewPort</code>-Elements
	 * @param objectRect	Zeichenbereich für das Objekt selbst
	 * @param fillColor	Füllfarbe (Farbverlauf wird Setup-abhängig automatisch berechnet)
	 * @param zoom	Zoomfaktor (wird für die Größe des Schattens benötigt)
	 */
	public synchronized void fill(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect, final Color fillColor, final double zoom) {
		if (setup.useShadows) {
			final int w=(int)Math.max(1,Math.round(Shapes.SHADOW_WIDTH*zoom));
			graphics.setColor(Shapes.SHADOW_COLOR);
			final Shape clip=graphics.getClip();
			final Rectangle shadowRect=new Rectangle(objectRect.x+Shapes.SHADOW_DIRECTION_X*w,objectRect.y+Shapes.SHADOW_DIRECTION_Y*w,objectRect.width,objectRect.height);
			clipper.set(graphics,drawRect,shadowRect);
			internFill(graphics,objectRect,Shapes.SHADOW_DIRECTION_X*w,Shapes.SHADOW_DIRECTION_Y*w,true);
			graphics.setClip(clip);
		}
		if (fillColor!=null) {
			if (filler==null) filler=new GradientFill();
			filler.set(graphics,objectRect,fillColor,false);
			internFill(graphics,objectRect,0,0,false);
		}
	}
}
