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
package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.fastpaint.BrighterColor;

/**
 * Zeichnet optional Lineale um eine Zeichenfläche.
 * @author Alexander Herzog
 * @see ModelSurfacePanel
 */
public class RulerPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7955621332491977714L;

	/**
	 * Zeichenfläche
	 */
	public final ModelSurfacePanel surfacePanel;

	/**
	 * Scroll-Panel das die Zeichenfläche enthält
	 */
	private final JScrollPane scrollPane;

	/**
	 * Lineal oben
	 */
	private final TopRuler topRuler;

	/**
	 * Lineal links
	 */
	private final LeftRuler leftRuler;

	/**
	 * Verschiebung der linken Koordinate des oberen Lineals und des Panels gegeneinander
	 */
	private int leftDelta=0;

	/**
	 * Linke Ecke des sichtbaren Bereichs
	 * @see #update()
	 */
	private int scrollX=0;

	/**
	 * Obere Ecke des sichtbaren Bereichs
	 * @see #update()
	 */
	private int scrollY=0;

	/**
	 * 1/Zoomfaktor
	 * @see #update()
	 */
	private double invZoom=1;

	/**
	 * Aktuelle horizontale Mausposition
	 * @see #mouseY
	 * @see #setMousePosition(Point)
	 * @see #paint(Graphics)
	 */
	private int mouseX=-1;

	/**
	 * Aktuelle vertikale Mausposition
	 * @see #mouseX
	 * @see #setMousePosition(Point)
	 * @see #paint(Graphics)
	 */
	private int mouseY=-1;

	/**
	 * Farbe für den auf den Linealen jeweils markiert dargestellten Bereich
	 */
	private final Color areaColor;

	/**
	 * Konstruktor der Klasse
	 * @param surfacePanel	Zeichenfläche
	 * @param rulerVisible	Lineale anzeigen?
	 */
	public RulerPanel(final ModelSurfacePanel surfacePanel, final boolean rulerVisible) {
		super(new BorderLayout());

		areaColor=new BrighterColor().get(SystemColor.activeCaptionBorder);

		add(scrollPane=new JScrollPane(this.surfacePanel=surfacePanel),BorderLayout.CENTER);

		scrollPane.getVerticalScrollBar().addAdjustmentListener(e->update());
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(e->update());
		surfacePanel.addZoomChangeListener(e->update());
		surfacePanel.addMouseListener(new MouseAdapter() {
			@Override public void mouseExited(MouseEvent e) {setMousePosition(null);}
		});
		surfacePanel.addMouseMotionListener(new MouseMotionAdapter() {
			@Override public void mouseMoved(MouseEvent e) {setMousePosition(e.getPoint());}
			@Override public void mouseDragged(MouseEvent e) {setMousePosition(e.getPoint());}
		});
		surfacePanel.addSelectionListener(e->updateRulersOnly());

		add(topRuler=new TopRuler(),BorderLayout.NORTH);
		add(leftRuler=new LeftRuler(),BorderLayout.WEST);

		topRuler.setVisible(rulerVisible);
		leftRuler.setVisible(rulerVisible);

		update();
	}

	/**
	 * Übermittelt die aktuelle Mausposition aus einem
	 * Maus-Ereignis an diese Klasse, damit sie für die
	 * Darstellung der Lineale zur Verfügung steht.
	 * @param p	Aktuelle Mausposition
	 * @see #mouseX
	 * @see #mouseY
	 */
	private void setMousePosition(final Point p) {
		mouseX=(p==null)?-1:p.x;
		mouseY=(p==null)?-1:p.y;
		updateRulersOnly();
	}

	/**
	 * Sind die Lineale sichtbar?
	 * @return	Lineale sichtbar
	 */
	public boolean isRulerVisible() {
		return topRuler.isVisible();
	}

	/**
	 * Stellt ein, ob die Lineale sichtbar sein sollen.
	 * @param visible	Lineale sichtbar
	 */
	public void setRulerVisible(final boolean visible) {
		topRuler.setVisible(visible);
		leftRuler.setVisible(visible);
	}

	/**
	 * Zeichnet alles neu.<br>
	 * Insbesondere werden die Lineale passend zu der Zeichenfläche aktualisiert.
	 */
	public void update() {
		leftDelta=scrollPane.getLocation().x-topRuler.getLocation().x;
		final Rectangle view=scrollPane.getViewport().getViewRect();
		scrollX=view.x;
		scrollY=view.y;
		invZoom=1/surfacePanel.getZoom();

		surfacePanel.repaint();
		updateRulersOnly();
	}

	/**
	 * Aktualisiert die Darstellung der Lineale.
	 */
	private void updateRulersOnly() {
		if (topRuler.isVisible()) {
			topRuler.repaint();
			leftRuler.repaint();
		}
	}

	/**
	 * Lineal oben
	 * @see RulerPanel#topRuler
	 */
	private class TopRuler extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-6787406004853918103L;

		/**
		 * Konstruktor der Klasse
		 */
		public TopRuler() {
			super();
			setPreferredSize(new Dimension(0,15));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int xAbs=leftDelta;
			double xRel=scrollX*invZoom;

			final int width=getWidth();
			final int height=getHeight();
			final int ascent=g.getFontMetrics().getMaxAscent();

			/* Markierter Bereich */
			final Rectangle rect=surfacePanel.getSelectedArea();
			if (rect!=null) {
				g.setColor(areaColor);
				final int x=Math.max(0,(int)Math.round(rect.x/invZoom));
				final int w=(int)Math.round(rect.width/invZoom);
				g.fillRect(x+leftDelta+1,0,w,height);
			}

			/* Linien und Zahlen */
			g.setColor(SystemColor.activeCaptionBorder);
			long lastNr=(long)(xRel)/25-1;
			while (xAbs<width) {
				long nr=(long)(xRel-0.0001)/25;
				if (nr>lastNr) {
					lastNr=nr;
					if (nr%2==0) {
						g.drawLine(xAbs,0,xAbs,height);
						long x=Math.max(0,Math.round(xRel-invZoom));
						g.drawString(""+x,xAbs+3,ascent+1);
					} else {
						g.drawLine(xAbs,height/2,xAbs,height);
					}
				}
				xRel+=invZoom;
				xAbs++;
			}

			/* Mauszeiger */
			if (mouseX>=0) {
				g.setColor(Color.BLACK);
				int x=mouseX-scrollX+leftDelta;
				g.drawLine(x,0,x,height);
			}
		}
	}

	/**
	 * Lineal links
	 * @see RulerPanel#leftRuler
	 */
	private class LeftRuler extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=8366048604838451140L;

		/** Rotations-System für die Schriften des linken Lineals */
		private final AffineTransform transformRotate;

		/**
		 * Konstruktor der Klasse
		 */
		public LeftRuler() {
			super();
			setPreferredSize(new Dimension(15,0));
			transformRotate=new AffineTransform();
			transformRotate.rotate(Math.toRadians(-90));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			int yAbs=0;
			double yRel=scrollY*invZoom;

			final AffineTransform transformDefault=((Graphics2D)g).getTransform();
			((Graphics2D)g).transform(transformRotate);

			final int width=getWidth();
			final int height=getHeight();
			final int ascent=g.getFontMetrics().getMaxAscent();

			/* Markierter Bereich */
			final Rectangle rect=surfacePanel.getSelectedArea();
			if (rect!=null) {
				g.setColor(areaColor);
				final int x=Math.max(0,(int)Math.round(rect.y/invZoom));
				final int w=(int)Math.round(rect.height/invZoom);
				g.fillRect(-x-1-w,0,w,width);
			}

			/* Linien und Zahlen */
			g.setColor(SystemColor.activeCaptionBorder);
			long lastNr=(long)(yRel)/25-1;
			while (yAbs<height) {
				long nr=(long)(yRel-0.0001)/25;
				if (nr>lastNr) {
					lastNr=nr;
					if (nr%2==0) {
						g.drawLine(-yAbs,0,-yAbs,width);
						long y=Math.max(0,Math.round(yRel-invZoom));
						final int w=g.getFontMetrics().stringWidth(""+y);
						g.drawString(""+y,-yAbs-3-w,ascent+1);
					} else {
						g.drawLine(-yAbs,width/2,-yAbs,width);
					}
				}
				yRel+=invZoom;
				yAbs++;
			}

			/* Mauszeiger */
			if (mouseY>=0) {
				g.setColor(Color.BLACK);
				int y=mouseY-scrollY;
				g.drawLine(-y,0,-y,width);
			}

			((Graphics2D)g).setTransform(transformDefault);
		}
	}
}
