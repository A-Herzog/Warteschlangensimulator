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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import tools.SetupData;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Ermöglicht das Zeichnen der Liste der aktiven Modell-Elemente (Navigator)
 * @author Alexander Herzog
 * @param <E>	Müssen von <code>ModelElementBox</code> abgeleitete Elemente sein.
 */
public class ModelElementNavigatorListCellRenderer<E extends ModelElementBox> implements ListCellRenderer<E> {
	/**
	 * Referenz auf das Setup-Singleton.
	 */
	private final SetupData setup=SetupData.getSetup();

	/**
	 * Zoomlevel
	 * @see #setZoom(double)
	 * @see #getElementRenderer(ModelElementPosition, boolean)
	 */
	private double zoom=1.0;

	/**
	 * Stellt den Zoomlevel ein
	 * @param zoom	Neuer Zoomlevel (1.0=100%)
	 */
	public void setZoom(final double zoom) {
		this.zoom=zoom;
	}

	/**
	 * Erstes zwischengespeichertes Bildobjekt für {@link #getElementRenderer(ModelElementPosition, boolean)}<br>
	 * Es wird nicht der Bildinhalt, sondern nur das Objekt im Speicher wiederverwendet.
	 * @see #getElementRenderer(ModelElementPosition, boolean)
	 */
	private BufferedImage tempImage1=null;

	/**
	 * Zweites zwischengespeichertes Bildobjekt für {@link #getElementRenderer(ModelElementPosition, boolean)}<br>
	 * Es wird nicht der Bildinhalt, sondern nur das Objekt im Speicher wiederverwendet.
	 * @see #getElementRenderer(ModelElementPosition, boolean)
	 */
	private BufferedImage tempImage2=null;

	/**
	 * Liefert den Listenzellen-Renderer für ein Element
	 * @param element	Element das dargestellt werden soll
	 * @param isSelected	Soll das Element markiert dargestellt werden?
	 * @return	Listenzellen-Renderer für das Element
	 */
	private Component getElementRenderer(final ModelElementPosition element, final boolean isSelected) {
		if (tempImage1==null) {
			tempImage1=new BufferedImage(200+2*Shapes.SHADOW_WIDTH,200+2*Shapes.SHADOW_WIDTH,BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			final Graphics2D graphics =(Graphics2D)tempImage1.getGraphics();
			graphics.setBackground(new Color(255,255,255,0));
			graphics.clearRect(0,0,tempImage1.getWidth(),tempImage1.getHeight());
		}

		element.drawToGraphics(tempImage1.getGraphics(),new Rectangle(Shapes.SHADOW_WIDTH,Shapes.SHADOW_WIDTH,200,200),zoom,false); /* damit getLowerRightPosition() einen korrekten Wert enthält */

		element.temporaryMoveToTop();
		try {

			final Point p1=element.getPosition(true);
			final Point p2=element.getLowerRightPosition();

			final Dimension dFull=new Dimension(p2.x,p2.y);
			final Dimension sizeFull=new Dimension(
					(int)Math.round(dFull.width*zoom)+20,
					(int)Math.round((dFull.height+Shapes.SHADOW_WIDTH)*zoom)+15
					);

			final Dimension dSmall=new Dimension(p2.x-p1.x,p2.y-p1.y);
			final Dimension sizeSmall=new Dimension(
					(int)Math.round(dSmall.width*zoom)+20,
					(int)Math.round((dSmall.height+Shapes.SHADOW_WIDTH)*zoom)+15
					);

			final Color backgroundColor=isSelected?(new Color(200,220,255)):Color.WHITE;

			final JPanel panel=new JPanel(new BorderLayout());
			panel.setBackground(backgroundColor);

			if (tempImage2==null || tempImage2.getWidth()!=sizeFull.width || tempImage2.getHeight()!=sizeFull.height) {
				tempImage2=new BufferedImage(sizeFull.width,sizeFull.height,BufferedImage.TYPE_4BYTE_ABGR);
			} else {
				final Graphics2D graphics =(Graphics2D)tempImage2.getGraphics();
				graphics.setBackground(new Color(255,255,255,0));
				graphics.clearRect(0,0,tempImage2.getWidth(),tempImage2.getHeight());
			}

			final boolean saveShowIDs=setup.showIDs;
			try {
				setup.showIDs=true;
				element.drawToGraphics(tempImage2.getGraphics(),new Rectangle(0,0,sizeFull.width,sizeFull.height-1),zoom,false);
			} finally {
				setup.showIDs=saveShowIDs;
			}

			final int xStart=(int)Math.round((p1.x-5)*zoom);
			final int yStart=(int)Math.round((p1.y-5)*zoom);

			final JPanel surface=new JPanel() {
				private static final long serialVersionUID = -4343888464611276523L;
				@Override public void paint(Graphics g) {g.drawImage(tempImage2,-xStart,-yStart,null);}
			};
			panel.add(surface,BorderLayout.CENTER);

			surface.setSize(sizeSmall);
			surface.setPreferredSize(sizeSmall);
			surface.setMinimumSize(sizeSmall);
			surface.setMaximumSize(sizeSmall);

			final String name=ModelElementCatalog.getCatalog().getMenuNameWithDefault(element);
			final JPanel info=new JPanel(new FlowLayout(FlowLayout.CENTER));
			info.setBackground(backgroundColor);
			panel.add(info,BorderLayout.SOUTH);
			info.add(new JLabel(name));

			String tooltip=element.getToolTip();
			if (tooltip==null) tooltip=element.getContextMenuElementName();
			panel.setToolTipText(tooltip);

			return panel;
		} finally {
			element.temporaryMoveRestore();
		}
	}

	/**
	 * Liefert einen Listenzellen-Renderer für ein fehlendes Element
	 * @param unknownClassName	Name der Klasse für die kein Element existiert
	 * @return	Listenzellen-Renderer für das fehlende Element
	 */
	private Component getErrorRenderer(final String unknownClassName) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("<html><span style=\"color: red\">"+unknownClassName+"</span></html>"));
		return panel;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof ModelElementPosition) return getElementRenderer(value,isSelected);
		return getErrorRenderer(value.getClass().getName());
	}

}
