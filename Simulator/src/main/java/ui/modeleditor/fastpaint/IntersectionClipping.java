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

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Stellt das Clipping gemäß Zeichenfläche und Objektgröße ein
 * @author Alexander Herzog
 */
public class IntersectionClipping {
	private Rectangle lastIntersection=null;
	private Rectangle lastObjectRect=null;
	private Rectangle lastDrawRect=null;

	/**
	 * Konstruktor der Klasse
	 */
	public IntersectionClipping() {
	}

	/**
	 * Stellt den Zeichenbereich auf die gesamte Zeichenfläche zurück.
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param drawRect	Gültiger Zeichenbereich des übergeordneten <code>JViewPort</code>-Elements
	 * @see IntersectionClipping#set(Graphics, Rectangle, Rectangle)
	 */
	public void clear(final Graphics graphics, final Rectangle drawRect) {
		graphics.setClip(drawRect.x,drawRect.y,drawRect.width+1,drawRect.height+1);
	}

	/**
	 * Stellt den Zeichenbereich für ein Zeichenobjekt ein.
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param drawRect	Gültiger Zeichenbereich des übergeordneten <code>JViewPort</code>-Elements
	 * @param objectRect	Zeichenobjektgröße, auf die der Zeichenbereich eingeschränkt werden soll
	 * @see IntersectionClipping#clear(Graphics, Rectangle)
	 */
	public final synchronized void set(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect) {
		if (objectRect==null) {
			clear(graphics,drawRect);
			return;
		}

		if (lastObjectRect==null) lastObjectRect=new Rectangle();
		if (lastDrawRect==null) lastDrawRect=new Rectangle();

		if (lastIntersection==null ||  !lastObjectRect.equals(objectRect) || !lastDrawRect.equals(drawRect)) {
			FastPaintTools.copyRectangleData(objectRect,lastObjectRect);
			FastPaintTools.copyRectangleData(drawRect,lastDrawRect);
			lastIntersection=objectRect.intersection(drawRect);
		}

		graphics.setClip(lastIntersection.x,lastIntersection.y,lastIntersection.width+1,lastIntersection.height+1);
	}
}
