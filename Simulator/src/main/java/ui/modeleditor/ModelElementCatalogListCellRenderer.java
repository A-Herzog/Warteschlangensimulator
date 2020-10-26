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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import language.Language;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Ermöglicht das Zeichnen der Liste der Modell-Elemente zur Auswahl eines Elements zum Einfügen in die Liste
 * @author Alexander Herzog
 * @param <E>	Müssen von <code>ModelElementPosition</code> abgeleitete Elemente sein.
 */
public class ModelElementCatalogListCellRenderer<E extends ModelElementPosition> implements ListCellRenderer<E> {
	/**
	 * Zoomlevel
	 * @see #setZoom(double)
	 * @see #getElementRenderer(ModelElementPosition, boolean)
	 */
	private double zoom=1.0;

	/**
	 * Zoomlevel einstellen
	 * @param zoom	Neues Zoomlevel (1.0=100%)
	 */
	public void setZoom(final double zoom) {
		this.zoom=zoom;
	}

	/** Icon für "-" */
	private Icon iconMinus=null;
	/** Icon für "+" */
	private Icon iconPlus=null;

	private Component getGroupRenderer(final String groupName, final boolean statusOpen) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));

		/* verbraucht mehr Speicher: final JLabel label=new JLabel("<html><b>"+groupName+"</b></html>"); */
		final JLabel label=new JLabel(groupName);
		panel.add(label);
		final Font font=label.getFont();
		label.setFont(new Font(font.getName(),Font.BOLD,font.getSize()));

		final Icon icon;
		if (statusOpen) {
			if (iconMinus==null) iconMinus=Images.MODELEDITOR_GROUP_MINUS.getIcon();
			icon=iconMinus;
		} else {
			if (iconPlus==null) iconPlus=Images.MODELEDITOR_GROUP_PLUS.getIcon();
			icon=iconPlus;
		}
		if (icon!=null) label.setIcon(icon);

		panel.setToolTipText(Language.tr("Editor.TemplateFilter.CategoryTooltip"));

		return panel;
	}

	private BufferedImage tempImage1=null;
	private BufferedImage tempImage2=null;

	private Component getElementRenderer(final ModelElementPosition element, final boolean isSelected) {
		if (tempImage1==null) {
			tempImage1=new BufferedImage(200+2*Shapes.SHADOW_WIDTH,200+2*Shapes.SHADOW_WIDTH,BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			final Graphics2D graphics=(Graphics2D)tempImage1.getGraphics();
			graphics.setBackground(new Color(255,255,255,0));
			graphics.clearRect(0,0,tempImage1.getWidth(),tempImage1.getHeight());
		}

		element.drawToGraphics(tempImage1.getGraphics(),new Rectangle(Shapes.SHADOW_WIDTH,Shapes.SHADOW_WIDTH,200,200),zoom,false); /* damit getLowerRightPosition() einen korrekten Wert enthält */

		final Point p=element.getLowerRightPosition();
		final Dimension d=new Dimension(p.x,p.y);
		final Dimension size=new Dimension(
				(int)Math.round(d.width*zoom)+20,
				Math.max(25,(int)Math.round((d.height+Shapes.SHADOW_WIDTH)*zoom))+1
				);
		final Color backgroundColor=isSelected?(new Color(200,220,255)):Color.WHITE;

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBackground(backgroundColor);

		if (tempImage2==null || tempImage2.getWidth()!=size.width || tempImage2.getHeight()!=size.height) {
			tempImage2=new BufferedImage(size.width,size.height,BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			final Graphics2D graphics=(Graphics2D)tempImage2.getGraphics();
			graphics.setBackground(new Color(255,255,255,0));
			graphics.clearRect(0,0,tempImage2.getWidth(),tempImage2.getHeight());
		}
		element.drawToGraphics(tempImage2.getGraphics(),new Rectangle(0,0,size.width,size.height-1),zoom,false);

		final JPanel surface=new JPanel() {
			private static final long serialVersionUID = -4343888464611276523L;
			@Override public void paint(Graphics g) {g.drawImage(tempImage2,0,0,null);}
		};
		panel.add(surface,BorderLayout.CENTER);
		surface.setSize(size);
		surface.setPreferredSize(size);
		surface.setMinimumSize(size);
		surface.setMaximumSize(size);

		final String name=ModelElementCatalog.getCatalog().getMenuNameWithDefault(element);
		final JPanel info=new JPanel(new FlowLayout(FlowLayout.CENTER));
		info.setBackground(backgroundColor);
		panel.add(info,BorderLayout.SOUTH);
		info.add(new JLabel(name));

		String tooltip=element.getToolTip();
		if (tooltip==null) tooltip=element.getContextMenuElementName();
		panel.setToolTipText(tooltip);

		return panel;
	}

	private Component getErrorRenderer(final String unknownClassName) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("<html><span style=\"color: red\">"+unknownClassName+"</span></html>"));
		return panel;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof ModelElementListGroup) {
			return getGroupRenderer(((ModelElementListGroup)value).getTypeName(),((ModelElementListGroup)value).isShowSub());
		}
		if (value instanceof ModelElementPosition) {

			boolean groupOpen=true;
			int lastGroupIndex=index-1;
			while (lastGroupIndex>=0) {
				final ModelElementPosition element=list.getModel().getElementAt(lastGroupIndex);
				if (element instanceof ModelElementListGroup) {
					groupOpen=((ModelElementListGroup)element).isShowSub();
					break;
				}
				lastGroupIndex--;
			}

			if (groupOpen) {
				return getElementRenderer(value,isSelected);
			} else {
				JPanel p=new JPanel();
				p.setMinimumSize(new Dimension(0,0));
				p.setPreferredSize(new Dimension(0,0));
				p.setMaximumSize(new Dimension(0,0));
				p.setBorder(BorderFactory.createEmptyBorder());
				return p;
			}
		}
		return getErrorRenderer(value.getClass().getName());
	}
}