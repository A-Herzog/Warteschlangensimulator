/**
 * Copyright 2024 Alexander Herzog
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
package systemtools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * Fügt Ereignis-Listener zu einem {@link JScrollPane}-Element hinzu, die es ermöglichen,
 * auf Touch-Verschiebungen zu reagieren.
 */
public class JScrollPaneTouchHelper extends MouseAdapter implements MouseListener, MouseMotionListener {
	/**
	 * Innenbereich des Scrollbereichs, der auf Touch-Verschiebungen reagieren soll
	 */
	private final JViewport viewport;

	/**
	 * Bildschirm-Position des Mauszeigern beim Drücken der Maustaste bzw. Berühren des Bildschirms
	 * @see #mousePressed(MouseEvent)
	 */
	private Point lastPos=null;

	/**
	 * Position im {@link #viewport} des Mauszeigern beim Drücken der Maustaste bzw. Berühren des Bildschirms
	 * @see #mousePressed(MouseEvent)
	 */
	private Point relPos=null;

	/**
	 * Konstruktor der Klasse
	 * @param scrollPane	{@link JScrollPane}-Element das auf Touch-Verschiebungen reagieren soll
	 */
	public JScrollPaneTouchHelper(final JScrollPane scrollPane) {
		viewport=scrollPane.getViewport();
		addListener(scrollPane);
	}

	/**
	 * Installiert die Touch-Verschiebung für das {@link JScrollPane} und alle untergeordneten Elemente.
	 * @param c	Element für das die Touch-Verschiebung aktiviert werden soll
	 */
	private void addListener(final Component c) {
		c.addMouseListener(this);
		c.addMouseMotionListener(this);
		if (c instanceof Container) for (int i=0;i<((Container)c).getComponentCount();i++) addListener(((Container)c).getComponent(i));
	}

	@Override
	public void mousePressed(final MouseEvent ev) {
		lastPos=ev.getLocationOnScreen();
		relPos=viewport.getViewPosition();
	}

	@Override
	public void mouseDragged(final MouseEvent ev) {
		if (relPos==null) return;
		if(lastPos==null) lastPos=ev.getLocationOnScreen();

		final int x=lastPos.x-ev.getXOnScreen();
		final int y=lastPos.y-ev.getYOnScreen();

		final Point pos=relPos.getLocation();
		final Dimension viewSize=viewport.getViewSize();

		pos.translate(x,y);
		pos.x=Math.max(pos.x,0);
		pos.y=Math.max(pos.y,0);
		if (pos.x+viewport.getWidth()>viewSize.width) pos.x=viewSize.width-viewport.getWidth();
		if (pos.y+viewport.getHeight()>viewSize.height) pos.y=viewSize.height-viewport.getHeight();

		viewport.setViewPosition(pos);
	}
}