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
package ui.modeleditor.fastpaint;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * {@link GradientPaint}-Variante, die das {@link PaintContext}-Objekt
 * beim Aufruf von {@link #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)}
 * wenn möglich wiederverwendet.
 * @see GradientPaint
 * @see GradientFill
 */
public class GradientPaintWithCachedContext extends GradientPaint {
	/**
	 * Zur Prüfung, ob {@link #cachedContext} wiederverwendet werden kann,
	 * zwischengespeichertes {@link ColorModel}-Objekt
	 * @see #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)
	 * @see #cachedContext
	 */
	private ColorModel cachedCm;

	/**
	 * Zur Prüfung, ob {@link #cachedContext} wiederverwendet werden kann,
	 * zwischengespeicherter Geräte-Größenbereich
	 * @see #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)
	 * @see #cachedContext
	 */
	private Rectangle cachedDeviceBounds;

	/**
	 * Zur Prüfung, ob {@link #cachedContext} wiederverwendet werden kann,
	 * zwischengespeicherter Nutzer-Größenbereich
	 * @see #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)
	 * @see #cachedContext
	 */
	private Rectangle2D cachedUserBounds;

	/**
	 * Zur Prüfung, ob {@link #cachedContext} wiederverwendet werden kann,
	 * zwischengespeichertes Transformationsobjekt
	 * @see #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)
	 * @see #cachedContext
	 */
	private AffineTransform cachedXform;

	/**
	 * Zur Prüfung, ob {@link #cachedContext} wiederverwendet werden kann,
	 * zwischengespeicherter Renderer-Einstellungen
	 * @see #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)
	 * @see #cachedContext
	 */
	private RenderingHints cachedHints;

	/**
	 * Für eine weitere Verwendung zwischengespeicherter Kontext
	 * @see #createContext(ColorModel, Rectangle, Rectangle2D, AffineTransform, RenderingHints)
	 */
	private PaintContext cachedContext;

	/**
	 * Constructs a simple acyclic {@code GradientPaint} object.
	 * @param pt1 the first specified {@code Point} in user space
	 * @param color1 {@code Color} at the first specified
	 * {@code Point}
	 * @param pt2 the second specified {@code Point} in user space
	 * @param color2 {@code Color} at the second specified
	 * {@code Point}
	 * @throws NullPointerException if either one of colors or points
	 * is null
	 */
	public GradientPaintWithCachedContext(Point2D pt1, Color color1, Point2D pt2, Color color2) {
		super(pt1,color1,pt2,color2);
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		if (cachedContext==null || !cm.equals(cachedCm) || !deviceBounds.equals(cachedDeviceBounds) || !userBounds.equals(cachedUserBounds) || !xform.equals(cachedXform) || !hints.equals(cachedHints)) {
			cachedCm=cm;
			cachedDeviceBounds=deviceBounds;
			cachedUserBounds=userBounds;
			cachedXform=xform;
			cachedHints=hints;
			cachedContext=super.createContext(cm,deviceBounds,userBounds,xform,hints);
		}
		return cachedContext;
	}
}