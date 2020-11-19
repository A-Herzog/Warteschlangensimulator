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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import language.Language;
import tools.SetupData;
import ui.dialogs.FindElementDialog;
import ui.dialogs.SelectElementByIdDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Diese Klasse bietet statische Hilfsroutinen zum
 * Zeichnen von Elementen-Icons und Listen aus Elementen-Icons
 * (ggf. mit zusätzlicher Beschriftung) an. Die Icons
 * werden während der Anzeige direkt über Zeichenroutinen
 * der Elemente gezeichnet, ohne Umweg über ein Bitmap-Bild.
 * (Damit werden auch bei einer Desktop-Skalierung korrekte
 * Bilder angezeigt.)
 * @see ModelElementNavigatorListCellRenderer
 * @see ModelElementCatalogListCellRenderer
 * @see SelectElementByIdDialog
 * @see FindElementDialog
 */
public class ElementRendererTools {
	/**
	 * Referenz auf das Setup-Singleton.
	 */
	private static final SetupData setup=SetupData.getSetup();


	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden, sie bietet nur statische Hilfsroutinen an.
	 */
	private ElementRendererTools() {
	}

	/**
	 * Berechnet die Größe eines Elementes.
	 * @param element	Element dessen Größe berechnet werden soll (darf <code>null</code> sein)
	 * @param zoom	Zoomfaktor
	 * @param showIDs	ID an der Station anzeigen?
	 * @return	Größe des Elements
	 */
	public static Dimension getElementBoxSize(final ModelElementPosition element, final double zoom, final boolean showIDs) {
		if (element==null) return new Dimension(0,0);

		final Point p1=element.getPosition(true);
		final Point p2=element.getLowerRightPosition();
		int width=p2.x-p1.x;
		int height=p2.y-p1.y;

		if (width==0 && height==0) {
			width=40;
			height=40;
		}

		return new Dimension(
				(int)Math.round((width+Shapes.SHADOW_WIDTH)*zoom)+10,
				(int)Math.round((height+Shapes.SHADOW_WIDTH)*zoom)+10+(showIDs?5:0)
				);
	}

	/**
	 * Zeichnet ein Element auf eine Zeichenfläche unter Ausnutzung des zur Verfügung stehenden Platzes.
	 * @param element	Zu zeichnendes Element
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param width	Breite des Ausgabebereichs
	 * @param clip	Optionaler Clipping-Bereich für die Ausgabe (kann <code>null</code> sein)
	 * @param height	Höhe des Ausgabebereichs
	 * @param showIDs	ID an der Station anzeigen?
	 * @param maxZoom	Maximal zu verwendender Zoomfaktor
	 */
	public static void drawElement(final ModelElementPosition element, final Graphics graphics, final int width, final int height, final Rectangle clip, final boolean showIDs, final double maxZoom) {
		final Point p1=element.getPosition(true);
		final Point p2=element.getLowerRightPosition();
		final int elementWidth=p2.x-p1.x;
		final int elementHeight=p2.y-p1.y;

		element.temporaryMoveToTop();
		try {
			final boolean saveShowIDs=setup.showIDs;
			try {
				setup.showIDs=showIDs;

				double zoom;

				if (elementWidth==0 || elementHeight==0) {
					zoom=1;
				} else {
					final double z1=((double)(width-10-Shapes.SHADOW_WIDTH))/elementWidth;
					final double z2=((double)(height-10-Shapes.SHADOW_WIDTH))/(elementHeight+(showIDs?5:0));
					zoom=Math.round(Math.min(z1,z2)*10)/10.0;
				}
				zoom=Math.min(zoom,maxZoom);

				element.drawToGraphics(graphics,(clip!=null)?clip:new Rectangle(0,0,width,height),zoom,false);

			} finally {
				setup.showIDs=saveShowIDs;
			}

		} finally {
			element.temporaryMoveRestore();
		}
	}

	/**
	 * Liefert den Tooltip, der zu einem Element angezeigt werden soll.
	 * @param element	Element (kann <code>null</code> sein)
	 * @return	Tooltip
	 */
	private static String getTooltip(final ModelElementPosition element) {
		if (element==null) return "";

		String tooltip=element.getToolTip();
		if (tooltip==null) tooltip=element.getContextMenuElementName();
		return tooltip;
	}

	/**
	 * Liefert den Listenzellen-Renderer für ein Element
	 * @param element	Element das dargestellt werden soll (darf <code>null</code> sein)
	 * @param zoom	Zoomfaktor für die Darstellung
	 * @param showIDs	IDs an den Stationen anzeigen?
	 * @param isSelected	Soll das Element markiert dargestellt werden?
	 * @return	Listenzellen-Renderer für das Element
	 */
	public static JPanel getElementRenderer(final ModelElementPosition element, final double zoom, final boolean showIDs, final boolean isSelected) {
		final String name=ModelElementCatalog.getCatalog().getMenuNameWithDefault(element);
		final Color backgroundColor=isSelected?(new Color(200,220,255)):Color.WHITE;

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setToolTipText(getTooltip(element));

		final JPanel info=new JPanel(new FlowLayout(FlowLayout.CENTER));
		info.setBackground(backgroundColor);
		panel.add(info,BorderLayout.SOUTH);
		info.add(new JLabel(name));

		final RendererPanel renderer=new RendererPanel(element,backgroundColor,showIDs,zoom);
		final Dimension d=ElementRendererTools.getElementBoxSize(element,zoom,showIDs);
		renderer.setSize(d);
		renderer.setPreferredSize(d);
		renderer.setMinimumSize(d);
		renderer.setMaximumSize(d);
		panel.add(renderer,BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Liefert den Listenzellen-Renderer für ein Element
	 * @param element	Element das dargestellt werden soll (darf <code>null</code> sein)
	 * @param infoText	Neben dem Element anzuzeigender Info-Text (wird in {@link JLabel} gerendert, d.h. kann html enthalten, darf <code>null</code> sein)
	 * @param showIDs	IDs an den Stationen anzeigen?
	 * @param backgroundColor	Hintergrundfarbe des Panels (kann <code>null</code> sein, dann wird die Standardhintergrundfarbe verwendet)
	 * @param maxWidth	Maximale Breite der Darstellung des Elements (bei Werten &le;0 wird die Angabe ignoriert)
	 * @param maxHeight	Maximale Höhe der Darstellung des Elements (bei Werten &le;0 wird die Angabe ignoriert)
	 * @return	Listenzellen-Renderer für das Element
	 */
	public static JPanel getElementRenderer(final ModelElementPosition element, final String infoText, final boolean showIDs, final Color backgroundColor, final int maxWidth, final int maxHeight) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setToolTipText(getTooltip(element));

		if (infoText!=null) {
			final JPanel info=new JPanel(new FlowLayout(FlowLayout.LEFT));
			if (backgroundColor!=null) info.setBackground(backgroundColor);
			panel.add(info,BorderLayout.CENTER);
			info.add(new JLabel(infoText));
		}

		final RendererPanel renderer=new RendererPanel(element,backgroundColor,showIDs,1.0);
		final Dimension d=ElementRendererTools.getElementBoxSize(element,1.0,showIDs);

		if (maxWidth>=0 && d.width>maxWidth) {
			d.height=(int)Math.round(((double)d.height)*maxWidth/d.width);
			d.width=maxWidth;
		}

		if (maxHeight>=0 && d.height>maxHeight) {
			d.width=(int)Math.round(((double)d.width)*maxHeight/d.height);
			d.height=maxHeight;
		}

		renderer.setSize(d);
		renderer.setPreferredSize(d);
		renderer.setMinimumSize(d);
		renderer.setMaximumSize(d);
		panel.add(renderer,BorderLayout.WEST);

		return panel;
	}

	/**
	 * Liefert einen Listenzellen-Renderer für ein Icon.
	 * @param icon	Anzuzeigendes Icon (darf <code>null</code> sein)
	 * @param infoText	Neben dem Element anzuzeigender Info-Text (wird in {@link JLabel} gerendert, d.h. kann html enthalten, darf <code>null</code> sein)
	 * @param isSelected	Soll der Eintrag markiert dargestellt werden?
	 * @return	Listenzellen-Renderer für das Icon
	 */
	public static JPanel getIconRenderer(final Icon icon, final String infoText, final boolean isSelected) {
		final Color backgroundColor=isSelected?(new Color(200,220,255)):Color.WHITE;

		final JPanel panel=new JPanel(new BorderLayout());

		if (infoText!=null) {
			final JPanel info=new JPanel(new FlowLayout(FlowLayout.LEFT));
			info.setBackground(backgroundColor);
			panel.add(info,BorderLayout.CENTER);
			info.add(new JLabel(infoText));
		}

		if (icon!=null) {
			final JPanel iconPanel=new JPanel() {
				private static final long serialVersionUID=190398413287700976L;

				@Override
				public void paint(final Graphics g) {
					final Graphics2D graphics=(Graphics2D)g;
					graphics.setBackground(backgroundColor);
					graphics.clearRect(0,0,getWidth(),getHeight());
					icon.paintIcon(this,graphics,0,0);
				}
			};
			final Dimension d=new Dimension(icon.getIconWidth(),icon.getIconHeight());
			iconPanel.setSize(d);
			iconPanel.setPreferredSize(d);
			iconPanel.setMinimumSize(d);
			iconPanel.setMaximumSize(d);
			panel.add(iconPanel,BorderLayout.WEST);
		}

		return panel;
	}

	/**
	 * Liefert einen Listenzellen-Renderer für ein fehlendes Element.
	 * @param unknownClassName	Name der Klasse für die kein Element existiert
	 * @return	Listenzellen-Renderer für das fehlende Element
	 */
	public static JPanel getErrorRenderer(final String unknownClassName) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("<html><span style=\"color: red\">"+unknownClassName+"</span></html>"));
		return panel;
	}

	/**
	 * Liefert einen leeren Listenzellen-Renderer (z.B. für eine eingeklappte Gruppe).
	 * @return	Listenzellen-Renderer mit Höhe und Breite 0
	 */
	public static JPanel getEmptyRenderer() {
		final JPanel p=new JPanel();
		p.setMinimumSize(new Dimension(0,0));
		p.setPreferredSize(new Dimension(0,0));
		p.setMaximumSize(new Dimension(0,0));
		p.setBorder(BorderFactory.createEmptyBorder());
		return p;
	}

	/**
	 * Renderer für ein Stations-Bild (ohne Beschriftung oder ähnliches)
	 * @see ElementRendererTools#getElementRenderer(ModelElementPosition, double, boolean, boolean)
	 */
	private static class RendererPanel extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-7880531204960506751L;

		/**
		 * Darzustellendes Element
		 */
		private final ModelElementPosition element;

		/**
		 * Hintergrundfarbe für das Panel
		 */
		private final Color backgroundColor;

		/**
		 * IDs an den Stationen anzeigen?
		 */
		private final boolean showIDs;

		/**
		 * Maximal zu verwendender Zoomfaktor
		 */
		private final double maxZoom;

		/**
		 * Konstruktor der Klasse
		 * @param element	Darzustellendes Element (darf <code>null</code> sein)
		 * @param backgroundColor	Hintergrundfarbe für das Panel
		 * @param showIDs	IDs an den Stationen anzeigen?
		 * @param maxZoom	Maximal zu verwendender Zoomfaktor
		 */
		public RendererPanel(final ModelElementPosition element, final Color backgroundColor, final boolean showIDs, final double maxZoom) {
			super();
			setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

			this.element=element;
			this.backgroundColor=backgroundColor;
			this.showIDs=showIDs;
			this.maxZoom=maxZoom;
		}

		@Override
		public void paint(final Graphics g) {
			final Graphics2D graphics=(Graphics2D)g;
			final int width=getWidth();
			final int height=getHeight();

			if (backgroundColor!=null) {
				graphics.setBackground(backgroundColor);
				graphics.clearRect(0,0,width,height);
			}

			if (element!=null) {
				drawElement(element,graphics,width,height,graphics.getClipBounds(),showIDs,maxZoom);
			}
		}
	}

	/**
	 * Liefert ein Beschreibungs-Label zu einer Station
	 * @param surface	Haupt-Zeichenfläche
	 * @param id	ID der Station
	 * @return	Beschreibungs-Label zu einer Station
	 */
	public static ElementRendererTools.InfoRecord getRecord(final ModelSurface surface, final int id) {
		/* Element und übergeordnetes Element finden */
		ModelElement element=null;
		ModelElementSub parent=null;
		for (ModelElement el: surface.getElements()) {
			if (el.getId()==id) {element=el; break;}
			if (el instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)el).getSubSurface().getElements()) {
				if (sub.getId()==id) {parent=(ModelElementSub)el; element=sub; break;}
				if (parent!=null) break;
			}
		}

		if (element==null) return new ElementRendererTools.InfoRecord(null,"");

		/* Text aufbauen */
		final StringBuilder sb=new StringBuilder();
		sb.append("<b><span style=\"font-size: larger;\">");
		sb.append(element.getContextMenuElementName());
		sb.append("</span> (");
		final String name=element.getName();
		if (name.isEmpty()) sb.append(Language.tr("FindElement.NoName")); else sb.append(name);
		sb.append(")</b><br><span style=\"color: orange;\">");
		sb.append("id="+element.getId());
		sb.append("</span><br><span style=\"color: blue;\"><i>");
		if (parent==null) sb.append(Language.tr("FindElement.Level.Top")); else sb.append(String.format(Language.tr("FindElement.Level.Sub"),parent.getId()));
		sb.append("</span></i>");

		return new ElementRendererTools.InfoRecord(element,"<html><body>"+sb.toString()+"</body></html>");
	}

	/**
	 * Erstellt eine Listendarstellung mit Elementen und Beschreibungen.
	 * @param data	Datensätze der Elemente
	 * @return	Listendarstellung mit Elementen und Beschreibungen
	 */
	public static JList<InfoRecord> buildList(final List<InfoRecord> data) {
		final JList<ElementRendererTools.InfoRecord> list=new JList<>(data.toArray(new ElementRendererTools.InfoRecord[0]));
		list.setCellRenderer(new InfoRecordListCellRenderer());
		return list;
	}

	/**
	 * Erstellt eine Listendarstellung mit Elementen und Beschreibungen.
	 * @param data	Datensätze der Elemente
	 * @return	Listendarstellung mit Elementen und Beschreibungen
	 */
	public static JList<InfoRecord> buildList(final InfoRecord[] data) {
		final JList<ElementRendererTools.InfoRecord> list=new JList<>(data);
		list.setCellRenderer(new InfoRecordListCellRenderer());
		return list;
	}

	/**
	 * Erstellt eine Listendarstellung mit Elementen und Beschreibungen.
	 * @param mainSurface	Hauptzeichenfläche
	 * @param ids	IDs der anzuzeigenden Elemente (bestimmt auch die Reihenfolge in der Liste)
	 * @return	Listendarstellung mit Elementen und Beschreibungen
	 */
	public static JList<InfoRecord> buildList(final ModelSurface mainSurface, final int[] ids) {
		final List<ElementRendererTools.InfoRecord> data=new ArrayList<>();
		for (int i=0;i<ids.length;i++) data.add(getRecord(mainSurface,ids[i]));

		return ElementRendererTools.buildList(data);
	}

	/**
	 * Datensatz für die Anzeige in einer Liste mit einem
	 * {@link InfoRecordListCellRenderer}
	 * @see InfoRecordListCellRenderer
	 */
	public static class InfoRecord {
		/**
		 * Darzustellendes Element
		 */
		private final ModelElement element;

		/**
		 * Infotext zu dem Element
		 */
		private final String info;

		/**
		 * IDs an der Station anzeigen?
		 */
		private final boolean showIDs;

		/**
		 * Konstruktor der Klasse<br>
		 * Es wird keine ID an der Station angezeigt.
		 * @param element	Darzustellendes Element (darf <code>null</code> sein)
		 * @param info	Infotext zu dem Element (darf <code>null</code> sein)
		 */
		public InfoRecord(final ModelElement element, final String info) {
			this.element=element;
			this.info=info;
			showIDs=false;
		}

		/**
		 * Konstruktor der Klasse
		 * @param element	Darzustellendes Element (darf <code>null</code> sein)
		 * @param info	Infotext zu dem Element (darf <code>null</code> sein)
		 * @param showIDs	IDs an der Station anzeigen?
		 */
		public InfoRecord(final ModelElementPosition element, final String info, final boolean showIDs) {
			this.element=element;
			this.info=info;
			this.showIDs=showIDs;
		}
	}

	/**
	 * Stellt einen {@link InfoRecord}-Datensatz in einer Liste dar.
	 * @see InfoRecord
	 */
	public static class InfoRecordListCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-8887474065442536966L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Color backgroundColor=isSelected?(new Color(200,220,255)):Color.WHITE;

			if (value instanceof InfoRecord) {
				final InfoRecord infoRecord=(InfoRecord)value;
				if (infoRecord.element instanceof ModelElementPosition) {
					return getElementRenderer((ModelElementPosition)infoRecord.element,infoRecord.info,infoRecord.showIDs,backgroundColor,-1,-1);
				} else {
					/* Fallback für Kanten usw., die sich nicht von ModelElementPosition ableiten */
					if (infoRecord.element!=null) {
						return getIconRenderer(infoRecord.element.buildIcon(),infoRecord.info,isSelected);
					}
				}
			}
			return super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		}
	}
}