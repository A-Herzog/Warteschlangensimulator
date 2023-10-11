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
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

import language.Language;
import tools.SetupData;
import ui.dialogs.FindElementDialog;
import ui.dialogs.SelectElementByIdDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.Shapes;
import ui.tools.FlatLaFHelper;

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
	 * Gradientendarstellung für den Hintergrund
	 */
	public enum GradientStyle {
		/**
		 * Keine Gradienten verwenden
		 */
		OFF("off"),

		/**
		 * Gradient für die Darstellung der Liste auf der linken Fensterseite verwenden
		 * @see ElementRendererTools#listBackgroundColorGradientLeft
		 */
		LEFT("left"),

		/**
		 * Gradient für die Darstellung der Liste auf der rechten Fensterseite verwenden
		 * @see ElementRendererTools#listBackgroundColorGradientRight
		 */
		RIGHT("right");

		/**
		 * Bezeichner für den Gradientendarstellungsstil
		 */
		public final String name;

		/**
		 * Konsturktor des Enum
		 * @param name	Bezeichner für den Gradientendarstellungsstil
		 */
		GradientStyle(final String name) {
			this.name=name;
		}

		/**
		 * Liefert einen Gradientendarstellungsstil basierend auf seinem Namen
		 * @param name	Name des Gradientendarstellungsstil
		 * @return	Zugehöriger Gradientendarstellungsstil (mit Fallback zu {@link GradientStyle#OFF})
		 */
		public static GradientStyle fromName(final String name) {
			for (GradientStyle style: values()) if (style.name.equalsIgnoreCase(name)) return style;
			return OFF;
		}
	}

	/**
	 * Allgemeiner HTML-Vorspann (damit eine Zeichenkette als html-Daten erkannt und interpretiert wird)
	 */
	public static final String htmlHead="<html><body>";

	/**
	 * Allgemeiner HTML-Abspann (damit eine Zeichenkette als html-Daten erkannt und interpretiert wird)
	 */
	public static final String htmlFoot="</body></html>";

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
	 * Hintergrundfarbe für Listeneinträge
	 * @see #getBackgroundColor(boolean, GradientStyle)
	 */
	private static final Color[] listBackgroundColor=new Color[] {UIManager.getColor("List.background")};

	/**
	 * Hintergrundfarben-Gradient für Listeneinträge (Liste links am linken Fensterrand)
	 * @see #getBackgroundColor(boolean, GradientStyle)
	 */
	private static final Color[] listBackgroundColorGradientLeft=new Color[] {new Color(149,230,190),new Color(118,196,173)};

	/**
	 * Hintergrundfarben-Gradient für Listeneinträge (Liste links am rechten Fensterrand)
	 * @see #getBackgroundColor(boolean, GradientStyle)
	 */
	private static final Color[] listBackgroundColorGradientRight=new Color[] {new Color(149,230,190),new Color(118,196,173)};

	/**
	 * Hintergrundfarbe für selektierte Listeneinträge (Systemvorgabe)
	 * @see #getBackgroundColor(boolean, GradientStyle)
	 */
	private static final Color[] listBackgroundColorSelected=new Color[] {UIManager.getColor("List.selectionBackground")};

	/**
	 * Hintergrundfarbe für selektierte Listeneinträge (dezentere Variante bei hellen Layouts)
	 * @see #getBackgroundColor(boolean, GradientStyle)
	 */
	private static final Color[] listBackgroundColorSelectedCustom=new Color[] {new Color(200,220,255)};

	/**
	 * Liefert die Hintergrundfarben für einen Listeneintrag für ein Element
	 * @param isSelected	Soll die Hintergrundfarbe für selektierte Einträge verwendet werden?
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 * @return	Hintergrundfarbe für den Listeneintrag
	 */
	public static Color[] getBackgroundColor(final boolean isSelected, final GradientStyle useGradient) {
		if (FlatLaFHelper.isDark()) {
			return isSelected?listBackgroundColorSelected:listBackgroundColor;
		} else {
			if (isSelected) return listBackgroundColorSelectedCustom;
			if (setup.useHighContrasts || useGradient==null) return listBackgroundColor;
			switch (useGradient) {
			case OFF: return listBackgroundColor;
			case LEFT: return listBackgroundColorGradientLeft;
			case RIGHT: return listBackgroundColorGradientRight;
			default: return listBackgroundColor;
			}
		}
	}

	/**
	 * Lädt die Farbeinstellungen neu aus der {@link UIManager}-Konfiguration.
	 */
	public static void reloadColors() {
		listBackgroundColor[0]=UIManager.getColor("List.background");
		listBackgroundColorSelected[0]=UIManager.getColor("List.selectionBackground");
	}

	/**
	 * Liefert den Listenzellen-Renderer für ein Element
	 * @param element	Element das dargestellt werden soll (darf <code>null</code> sein)
	 * @param zoom	Zoomfaktor für die Darstellung
	 * @param showIDs	IDs an den Stationen anzeigen?
	 * @param isSelected	Soll das Element markiert dargestellt werden?
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 * @return	Listenzellen-Renderer für das Element
	 */
	public static JPanel getElementRenderer(final ModelElementPosition element, final double zoom, final boolean showIDs, final boolean isSelected, final GradientStyle useGradient) {
		final String name=ModelElementCatalog.getCatalog().getMenuNameWithDefault(element);
		final Color[] backgroundColors=getBackgroundColor(isSelected,useGradient);

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setToolTipText(getTooltip(element));

		final GradientPanel info=new GradientPanel(new FlowLayout(FlowLayout.CENTER));
		if (backgroundColors!=null) {
			if (backgroundColors.length>1) {
				info.setBackgroundColors(backgroundColors[0],backgroundColors[1]);
			} else {
				info.setBackgroundColors(backgroundColors[0],null);
			}
		}
		panel.add(info,BorderLayout.SOUTH);
		info.add(new JLabel(name));

		final RendererPanel renderer=new RendererPanel(element,backgroundColors,showIDs,zoom);
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
	 * @param backgroundColors	Hintergrundfarbe des Panels (kann <code>null</code> sein, dann wird die Standardhintergrundfarbe verwendet)
	 * @param maxWidth	Maximale Breite der Darstellung des Elements (bei Werten &le;0 wird die Angabe ignoriert)
	 * @param maxHeight	Maximale Höhe der Darstellung des Elements (bei Werten &le;0 wird die Angabe ignoriert)
	 * @return	Listenzellen-Renderer für das Element
	 */
	public static JPanel getElementRenderer(final ModelElementPosition element, final String infoText, final boolean showIDs, final Color[] backgroundColors, final int maxWidth, final int maxHeight) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setToolTipText(getTooltip(element));

		if (infoText!=null) {
			final GradientPanel info=new GradientPanel(new FlowLayout(FlowLayout.LEFT));
			if (backgroundColors!=null) {
				if (backgroundColors.length>1) {
					info.setBackgroundColors(backgroundColors[0],backgroundColors[1]);
				} else {
					info.setBackgroundColors(backgroundColors[0],null);
				}
			} else {
				info.setOpaque(false);
			}
			panel.add(info,BorderLayout.CENTER);
			panel.setOpaque(false);
			final JLabel label=new JLabel(infoText);
			label.setOpaque(false);
			info.add(label);
		}

		final RendererPanel renderer=new RendererPanel(element,backgroundColors,showIDs,1.0);
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
		return getIconRenderer(icon,infoText,backgroundColor);
	}

	/**
	 * Liefert einen Listenzellen-Renderer für ein Icon.
	 * @param icon	Anzuzeigendes Icon (darf <code>null</code> sein)
	 * @param infoText	Neben dem Element anzuzeigender Info-Text (wird in {@link JLabel} gerendert, d.h. kann html enthalten, darf <code>null</code> sein)
	 * @param backgroundColor	Zu verwendende Hintergrundfarbe (<code>null</code> wird als transparenter Hintergrund interpretiert)
	 * @return	Listenzellen-Renderer für das Icon
	 */
	public static JPanel getIconRenderer(final Icon icon, final String infoText, final Color backgroundColor) {
		final JPanel panel=new JPanel(new BorderLayout());

		if (infoText!=null) {
			final JPanel info=new JPanel(new FlowLayout(FlowLayout.LEFT));
			if (backgroundColor==null) info.setOpaque(false); else info.setBackground(backgroundColor);
			panel.add(info,BorderLayout.CENTER);
			info.add(new JLabel(infoText));
		}

		if (icon!=null) {
			final JPanel iconPanel=new JPanel() {
				private static final long serialVersionUID=190398413287700976L;

				@Override
				public void paint(final Graphics g) {
					final Graphics2D graphics=(Graphics2D)g;
					if (backgroundColor!=null) {
						graphics.setBackground(backgroundColor);
						graphics.clearRect(0,0,getWidth(),getHeight());
					}
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

		if (backgroundColor==null) panel.setOpaque(false);

		return panel;
	}

	/**
	 * Liefert einen Listenzellen-Renderer für ein fehlendes Element.
	 * @param unknownClassName	Name der Klasse für die kein Element existiert
	 * @return	Listenzellen-Renderer für das fehlende Element
	 */
	public static JPanel getErrorRenderer(final String unknownClassName) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel(htmlHead+"<span style=\"color: red\">"+unknownClassName+"</span>"+htmlFoot));
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
	 * @see ElementRendererTools#getElementRenderer(ModelElementPosition, double, boolean, boolean, GradientStyle)
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
		private final Color[] backgroundColors;

		/**
		 * IDs an den Stationen anzeigen?
		 */
		private final boolean showIDs;

		/**
		 * Maximal zu verwendender Zoomfaktor
		 */
		private final double maxZoom;

		/**
		 * Zeichen-Hilfsobjekt für Gradienten
		 * @see #paint(Graphics)
		 */
		private final GradientFill gradient;

		/**
		 * Cache des Rechteck-Objektes zur Bestimmung der Ränder
		 * des Gradientenbereichs
		 * @see #gradient
		 * @see #paint(Graphics)
		 */
		private final Rectangle rect;

		/**
		 * Konstruktor der Klasse
		 * @param element	Darzustellendes Element (darf <code>null</code> sein)
		 * @param backgroundColors	Hintergrundfarbe für das Panel
		 * @param showIDs	IDs an den Stationen anzeigen?
		 * @param maxZoom	Maximal zu verwendender Zoomfaktor
		 */
		public RendererPanel(final ModelElementPosition element, final Color[] backgroundColors, final boolean showIDs, final double maxZoom) {
			super();
			setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

			this.element=element;
			this.backgroundColors=backgroundColors;
			this.showIDs=showIDs;
			this.maxZoom=maxZoom;

			gradient=new GradientFill(false);
			rect=new Rectangle();
		}

		@Override
		public void paint(final Graphics g) {
			final Graphics2D graphics=(Graphics2D)g;
			final int width=getWidth();
			final int height=getHeight();

			if (backgroundColors!=null) {
				if (backgroundColors.length==1 || backgroundColors[1]==null) {
					/* Farbe */
					graphics.setBackground(backgroundColors[0]);
					graphics.clearRect(0,0,width,height);
				} else {
					/* Farbverlauf */
					rect.width=getWidth();
					rect.height=getHeight();
					gradient.set(g,rect,backgroundColors[0],backgroundColors[1],false);
					graphics.fillRect(0,0,width,height);
				}
			}

			if (element!=null) {
				drawElement(element,graphics,width,height,graphics.getClipBounds(),showIDs,maxZoom);
			}
		}
	}

	/**
	 * Liefert eine HTML-Beschreibung zu einer Station.
	 * @param element	Station zu der die Beschreibung erstellt werden soll
	 * @param parent	Übergeordnete Station (d.h. eine Untermodell-Station); kann <code>null</code> sein
	 * @return	HTML-Beschreibung der Station
	 */
	public static String getElementHTMLInfo(final ModelElement element, final ModelElementSub parent) {
		final StringBuilder info=new StringBuilder();
		info.append("<b><span style=\"font-size: larger;\">");
		info.append(element.getContextMenuElementName());
		info.append("</span> (");
		final String name=element.getName();
		if (name.isEmpty()) info.append(Language.tr("FindElement.NoName")); else info.append(name);
		info.append(")</b><br><span style=\"color: orange;\">");
		info.append("id="+element.getId());
		info.append("</span><br><span style=\"color: blue;\"><i>");
		if (parent==null) info.append(Language.tr("FindElement.Level.Top")); else info.append(String.format(Language.tr("FindElement.Level.Sub"),parent.getId()));
		info.append("</span></i>");
		return info.toString();
	}

	/**
	 * Liefert eine kurze HTML-Beschreibung zu einer Station.
	 * @param element	Station zu der die Beschreibung erstellt werden soll
	 * @return	HTML-Beschreibung der Station
	 */
	public static String getElementHTMLInfo(final ModelElement element) {
		final StringBuilder info=new StringBuilder();
		info.append("<b><span style=\"font-size: larger;\">");
		info.append(element.getContextMenuElementName());
		info.append("</span> (");
		final String name=element.getName();
		if (name.isEmpty()) info.append(Language.tr("FindElement.NoName")); else info.append(name);
		info.append(")</b><br><span style=\"color: orange;\">");
		info.append("id="+element.getId());
		info.append("</span>");
		return info.toString();
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
			if (el instanceof ModelElementSub) {
				for (ModelElement sub: ((ModelElementSub)el).getSubSurface().getElements()) {
					if (sub.getId()==id) {parent=(ModelElementSub)el; element=sub; break;}
				}
				if (parent!=null) break;
			}
		}

		if (element==null) return new ElementRendererTools.InfoRecord(null,"");

		/* Text aufbauen */
		return new ElementRendererTools.InfoRecord(element,htmlHead+getElementHTMLInfo(element,parent)+htmlFoot);
	}

	/**
	 * Liefert ein Beschreibungs-Label zu einer Station
	 * @param surface	Haupt-Zeichenfläche
	 * @param element	Station
	 * @return	Beschreibungs-Label zu einer Station
	 */
	public static ElementRendererTools.InfoRecord getRecord(final ModelSurface surface, final ModelElement element) {
		/* Element und übergeordnetes Element finden */
		ModelElementSub parent=null;
		for (ModelElement el: surface.getElements()) {
			if (el==element) break;
			if (el instanceof ModelElementSub) {
				for (ModelElement sub: ((ModelElementSub)el).getSubSurface().getElements()) {
					if (sub==element) {parent=(ModelElementSub)el; break;}
				}
				if (parent!=null) break;
			}
		}

		/* Text aufbauen */
		return new ElementRendererTools.InfoRecord(element,htmlHead+getElementHTMLInfo(element,parent)+htmlFoot);
	}

	/**
	 * Erstellt eine Listendarstellung mit Elementen und Beschreibungen.
	 * @param data	Datensätze der Elemente
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 * @return	Listendarstellung mit Elementen und Beschreibungen
	 */
	public static JList<InfoRecord> buildList(final List<InfoRecord> data, final GradientStyle useGradient) {
		final JList<ElementRendererTools.InfoRecord> list=new JList<>(data.toArray(new ElementRendererTools.InfoRecord[0]));
		list.setCellRenderer(new InfoRecordListCellRenderer(useGradient));
		return list;
	}

	/**
	 * Erstellt eine Liste und ein Datenmodell dazu.
	 * @param data	Datensätze, die initial in das Datenmodell aufgenommen werden sollen (kann <code>null</code> sein)
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 * @return	2-elementiges Array aus Liste und Datenmodell
	 */
	public static Object[] buildListAndModel(final InfoRecord[] data, final GradientStyle useGradient) {
		final DefaultListModel<ElementRendererTools.InfoRecord> model=new DefaultListModel<>();
		if (data!=null) for (InfoRecord record: data) model.addElement(record);

		final JList<ElementRendererTools.InfoRecord> list=new JList<>(model);
		list.setCellRenderer(new InfoRecordListCellRenderer(useGradient));
		return new Object[] {list,model};
	}

	/**
	 * Erstellt eine Listendarstellung mit Elementen und Beschreibungen.
	 * @param data	Datensätze der Elemente
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 * @return	Listendarstellung mit Elementen und Beschreibungen
	 */
	public static JList<InfoRecord> buildList(final InfoRecord[] data, final GradientStyle useGradient) {
		final JList<ElementRendererTools.InfoRecord> list=new JList<>(data);
		list.setCellRenderer(new InfoRecordListCellRenderer(useGradient));
		return list;
	}

	/**
	 * Erstellt eine Listendarstellung mit Elementen und Beschreibungen.
	 * @param mainSurface	Hauptzeichenfläche
	 * @param ids	IDs der anzuzeigenden Elemente (bestimmt auch die Reihenfolge in der Liste)
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 * @return	Listendarstellung mit Elementen und Beschreibungen
	 */
	public static JList<InfoRecord> buildList(final ModelSurface mainSurface, final int[] ids, final GradientStyle useGradient) {
		final List<ElementRendererTools.InfoRecord> data=new ArrayList<>();
		for (int i=0;i<ids.length;i++) data.add(getRecord(mainSurface,ids[i]));

		return ElementRendererTools.buildList(data,useGradient);
	}

	/**
	 * Trägt neue Datensätze in ein Modell ein (und löscht vorher die bisherigen)
	 * @param mainSurface	Hauptzeichenfläche
	 * @param model	Datenmodell in das die Datensätze eingetragen werden sollen
	 * @param ids	IDs der anzuzeigenden Elemente (bestimmt auch die Reihenfolge in der Liste)
	 */
	public static void buildList(final ModelSurface mainSurface, final DefaultListModel<ElementRendererTools.InfoRecord> model, final int[] ids) {
		model.clear();
		for (int i=0;i<ids.length;i++) model.addElement(getRecord(mainSurface,ids[i]));
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

		/**
		 * Sollen Farbverläufe im Hintergrund verwendet werden?
		 */
		private final GradientStyle useGradient;

		/**
		 * Konstruktor der Klasse
		 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
		 */
		public InfoRecordListCellRenderer(final GradientStyle useGradient) {
			this.useGradient=useGradient;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Color[] backgroundColors=ElementRendererTools.getBackgroundColor(isSelected,useGradient);

			if (value instanceof InfoRecord) {
				final InfoRecord infoRecord=(InfoRecord)value;
				if (infoRecord.element instanceof ModelElementPosition) {
					return getElementRenderer((ModelElementPosition)infoRecord.element,infoRecord.info,infoRecord.showIDs,backgroundColors,-1,-1);
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

	/**
	 * Panel mit optional einem Farbgradienten als Hintergrund
	 * @author Alexander Herzog
	 */
	public static class GradientPanel extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-1705199321405719435L;

		/**
		 * Hintergrundfarben
		 * (nur ein Wert belegt: Farbe, beide belegt: Gradient)
		 * @see #setBackgroundColors(Color, Color)
		 */
		private final Color[] background;

		/**
		 * Zeichen-Hilfsobjekt für Gradienten
		 * @see #paintComponent(Graphics)
		 */
		private final GradientFill gradient;

		/**
		 * Cache des Rechteck-Objektes zur Bestimmung der Ränder
		 * des Gradientenbereichs
		 * @see #gradient
		 * @see #paintComponent(Graphics)
		 */
		private final Rectangle rect;

		/**
		 * Konstruktor der Klasse
		 * @param layout	Zu verwendender Layout-Manager
		 */
		public GradientPanel(final LayoutManager layout) {
			super(layout);
			background=new Color[2];
			gradient=new GradientFill(false);
			rect=new Rectangle();
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (background[0]!=null) {
				rect.width=getWidth();
				rect.height=getHeight();
				gradient.set(g,rect,background[0],background[1],false);
				g.fillRect(0,0,rect.width,rect.height);

			}
			super.paintComponent(g);
		}

		/**
		 * Stellt die Hintergrundfarben ein
		 * @param color1	Farbe 1 (muss ungleich <code>null</code> sein)
		 * @param color2	Farbe 2; wird ein Wert ungleich <code>null</code> angegeben, so wird ein Gradient gezeichnet; ansonsten wird Farbe 1 als Hintergrundfarbe verwendet
		 */
		public void setBackgroundColors(final Color color1, final Color color2) {
			if (color2==null) {
				setBackground(color1);
				background[0]=null;
				background[1]=null;
			} else {
				setBackground(new Color(0,0,0,0));
				setOpaque(false);
				background[0]=color1;
				background[1]=color2;
			}
		}
	}

	/**
	 * Listendarstellung für Elementenlisten
	 * (Ergänzung gegenüber {@link JList}: Der Hintergrund des ungenutzten Bereiches kann optional als Gradient gezeichnet werden.)
	 * @param <E>	Typen der darzustellenden Elemente (sollte {@link ModelElementPosition} oder {@link ModelElementBox} sein)
	 */
	public static class ElementList<E> extends JList<E> {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=3091995985124192305L;
		/**
		 * Sollen Farbverläufe im Hintergrund verwendet werden?
		 */
		private final boolean useGradient;

		/**
		 * Hintergrundfarben
		 * (nur ein Wert belegt: Farbe, beide belegt: Gradient)
		 */
		private final Color[] background;

		/**
		 * Zeichen-Hilfsobjekt für Gradienten
		 * @see #paintComponent(Graphics)
		 */
		private final GradientFill gradient;

		/**
		 * Cache des Rechteck-Objektes zur Bestimmung der Ränder
		 * des Gradientenbereichs
		 * @see #gradient
		 * @see #paintComponent(Graphics)
		 */
		private final Rectangle rect;

		/**
		 * Konstruktor der Klasse
		 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
		 */
		public ElementList(final GradientStyle useGradient) {
			this.useGradient=(useGradient!=null && useGradient!=GradientStyle.OFF) && !SetupData.getSetup().useHighContrasts;
			gradient=new GradientFill(false);
			rect=new Rectangle();
			background=ElementRendererTools.getBackgroundColor(false,this.useGradient?useGradient:GradientStyle.OFF);
			if (this.useGradient) {
				setBackground(null);
				setOpaque(false);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			if (useGradient && background.length==2 && background[1]!=null) {
				rect.width=getWidth();
				rect.height=getHeight();
				gradient.set(g,rect,background[0],background[1],false);
				g.fillRect(0,0,rect.width,rect.height);
			}
			super.paintComponent(g);
		}
	}
}