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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JViewport;

import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;

/**
 * Stellt eine verkleinerte Darstellung des gesamten Modells dar und zeichnet den sichtbaren Bereich ein.
 * Außerdem besteht die Möglichkeit, durch Klicks oder Drag&amp;Drop den sichtbaren Bereich zu verschieben.
 * @author Alexander Herzog
 * @see ModelSurfacePanel
 */
public class ModelExplorer extends JPanel {
	private static final long serialVersionUID = 5439577551953528165L;

	private final ModelSurfacePanel surfacePanel;
	private final ModelSurface surface;
	private final BufferedImage image;

	/**
	 * Konstruktor der Klasse <code>ModelExplorer</code>
	 * @param surfacePanel	Modell-Panel aus dem das darzustellende Modell entnommen wird und auf das die Verschiebungen wirken sollen
	 * @param maxXSize	Maximale Breite der Übersichtsdarstellung
	 * @param maxYSize	Maximale Höhe der Übersichtsdarstellung
	 */
	public ModelExplorer(final ModelSurfacePanel surfacePanel, final int maxXSize, final int maxYSize) {
		super();
		this.surfacePanel=surfacePanel;
		this.surface=surfacePanel.getSurface();

		/* Bild der Zeichenfläche erstellen */
		final Point p1=surface.getUpperLeftModelCorner();
		final Point p2=surface.getLowerRightModelCorner();
		final int xSize=Math.max(100,p2.x-p1.x);
		final int ySize=Math.max(100,p2.y-p1.y);
		if ((int)Math.round(((double)xSize)/ySize*maxYSize)>maxXSize) {
			image=surfacePanel.getImage(maxXSize,(int)Math.round(((double)ySize)/xSize*maxXSize));
		} else {
			image=surfacePanel.getImage((int)Math.round(((double)xSize)/ySize*maxYSize),maxYSize);
		}

		/* Größe einstellen */
		final Dimension d=new Dimension(image.getWidth()+2,image.getHeight()+2);
		setPreferredSize(d);
		setSize(d);
		setMinimumSize(d);
		setMaximumSize(d);

		/* Auf Mausereignisse achten */
		addMouseListener(new ModelExplorerMouseListener());
		addMouseMotionListener(new ModelExplorerMouseListener());
	}

	@Override
	public void paint(Graphics g) {
		final Dimension size=getSize();

		/* Rahmen zeichnen */
		g.setColor(Color.WHITE);
		g.fillRect(0,0,size.width-1,size.height-1);
		g.setColor(Color.BLACK);
		g.drawRect(0,0,size.width-1,size.height-1);

		/* Bild zeichnen */
		g.drawImage(image,1,1,null);

		/* Sichtbaren Bereich einzeichnen */
		if (surfacePanel.getParent() instanceof JViewport) {
			final Rectangle rect=((JViewport)surfacePanel.getParent()).getViewRect();
			final Point p1=surface.getUpperLeftModelCorner();
			final Point p2=surface.getLowerRightModelCorner();
			final double scaleFactor=1/surfacePanel.getZoom()*image.getHeight()/Math.max(100,p2.y-p1.y);
			rect.x-=p1.x;
			rect.y-=p1.y;

			rect.x=(int)Math.round(rect.x*scaleFactor);
			rect.y=(int)Math.round(rect.y*scaleFactor);
			rect.width=(int)Math.round(rect.width*scaleFactor);
			rect.height=(int)Math.round(rect.height*scaleFactor);

			rect.x=Math.max(0,rect.x);
			rect.y=Math.max(0,rect.y);
			rect.width=Math.min(getSize().width-2-rect.x,rect.width);
			rect.height=Math.min(getSize().height-2-rect.y,rect.height);

			g.setColor(new Color(0,0,255,48));
			g.fillRect(rect.x+1,rect.y+1,rect.width,rect.height);
			g.setColor(Color.BLUE);
			g.drawRect(rect.x+1,rect.y+1,rect.width,rect.height);
		}
	}

	private void centerVisibleArea(final int x, final int y) {
		final Point p1=surface.getUpperLeftModelCorner();
		final Point p2=surface.getLowerRightModelCorner();

		final double xRel=((double)(x-1))/image.getWidth();
		final double yRel=((double)(y-1))/image.getHeight();

		final int xCenter=(int)Math.round((xRel*Math.max(100,p2.x-p1.x)+p1.x)*surfacePanel.getZoom());
		final int yCenter=(int)Math.round((yRel*Math.max(100,p2.y-p1.y)+p1.y)*surfacePanel.getZoom());

		if (surfacePanel.getParent() instanceof JViewport) {
			final Rectangle rect=((JViewport)surfacePanel.getParent()).getViewRect();
			int xTop=Math.max(0,xCenter-rect.width/2);
			int yTop=Math.max(0,yCenter-rect.height/2);
			xTop=Math.min(xTop,(int)Math.round(p2.x*surfacePanel.getZoom()-rect.width));
			yTop=Math.min(yTop,(int)Math.round(p2.y*surfacePanel.getZoom()-rect.height));
			((JViewport)surfacePanel.getParent()).setViewPosition(new Point(xTop,yTop));
		}

		repaint();
	}

	private class ModelExplorerMouseListener implements MouseListener, MouseMotionListener {
		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {centerVisibleArea(e.getX(),e.getY());}
		@Override public void mouseReleased(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mouseDragged(MouseEvent e) {centerVisibleArea(e.getX(),e.getY());}
		@Override public void mouseMoved(MouseEvent e) {}
	}
}