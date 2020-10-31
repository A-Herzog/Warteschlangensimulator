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
package ui.modeleditor.elements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Verbindungsecke, um Verbindungskante optisch schöner auszurichten
 * @author Alexander Herzog
 */
public final class ModelElementVertex extends ModelElementPosition implements ModelElementEdgeMultiIn, ModelElementEdgeOut {
	/**
	 * Referenz auf das Setup-Singleton.
	 */
	private final SetupData setup=SetupData.getSetup();

	/**
	 * Liste der einlaufenden Kanten
	 */
	private List<ModelElementEdge> connectionsIn;

	/**
	 * Auslaufende Kante
	 */
	private ModelElementEdge connectionOut;

	/** IDs der einlaufenden Kanten (Wird nur beim Laden und Clonen verwendet.) */
	private List<Integer> connectionsInIds=null;
	/** ID der auflaufenden Kante (Wird nur beim Laden und Clonen verwendet.) */
	private int connectionOutId=-1;

	/**
	 * Konstruktor der Klasse <code>ModelElementVertex</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementVertex(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(10,10),Shapes.ShapeType.SHAPE_RECTANGLE);
		connectionsIn=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_VERTEX.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Vertex.Tooltip");
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmenü angezeigt werden soll.<br>
	 * Generiert im Falle eines Elements mit eigener Position (= einem normal darstellbaren Element) ein Icon basierend auf der Darstellung des Elements selber.
	 * @return	Icon zur Beschriftung des Elements im Kontextmenü oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	@Override
	public Icon buildIcon() {
		final Dimension size=getSize();
		final BufferedImage image=new BufferedImage(size.width+15,size.height+15,BufferedImage.TYPE_4BYTE_ABGR);

		temporaryMoveToTop();
		try {
			final boolean saveShowIDs=setup.showIDs;
			try {
				setup.showIDs=false;
				drawToGraphics(image.getGraphics(),new Rectangle(0,0,size.width+15,size.height+15),1.0,false);
			} finally {
				setup.showIDs=saveShowIDs;
			}
		} finally {
			temporaryMoveRestore();
		}

		return getScaledElementIcon(image);
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementVertex)) return false;

		if (connectionOut==null) {
			if (((ModelElementVertex)element).connectionOut!=null) return false;
		} else {
			if (((ModelElementVertex)element).connectionOut==null) return false;
			if (connectionOut.getId()!=((ModelElementVertex)element).connectionOut.getId()) return false;
		}

		final List<ModelElementEdge> connectionsIn2=((ModelElementVertex)element).connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementVertex) {
			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=((ModelElementVertex)element).connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}
			if (((ModelElementVertex)element).connectionOut!=null) connectionOutId=((ModelElementVertex)element).connectionOut.getId();
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementVertex clone(final EditModel model, final ModelSurface surface) {
		final ModelElementVertex element=new ModelElementVertex(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		ModelElement element;

		if (connectionsInIds!=null) {
			for (int i=0;i<connectionsInIds.size();i++) {
				element=surface.getById(connectionsInIds.get(i));
				if (element instanceof ModelElementEdge) connectionsIn.add((ModelElementEdge)element);
			}
			connectionsInIds=null;
		}

		if (connectionOutId>=0) {
			element=surface.getById(connectionOutId);
			if (element instanceof ModelElementEdge) connectionOut=(ModelElementEdge)element;
			connectionOutId=-1;
		}
	}

	private double infoFontZoom;
	private Font infoFont;
	private int infoFontAscent;

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		Color borderColor;
		int borderWidth;
		if (isSelected() && showSelectionFrames) {
			borderColor=Color.GREEN;
			borderWidth=2;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				borderColor=Color.BLUE;
				borderWidth=2;
			} else {
				borderColor=Color.DARK_GRAY;
				borderWidth=1;
			}
		}

		drawRect(graphics,drawRect,zoom,borderColor,borderWidth,new Color(235,235,235),1);
		drawRect(graphics,drawRect,zoom,borderColor,borderWidth,new Color(235,235,235),2);

		if (setup.showIDs) {
			if (infoFont==null || zoom!=infoFontZoom) {
				infoFontZoom=zoom;
				infoFont=new Font(Font.DIALOG,0,(int)FastMath.round(10*zoom));
				graphics.setFont(infoFont);
				infoFontAscent=graphics.getFontMetrics().getAscent();
			} else {
				graphics.setFont(infoFont);
			}

			final Rectangle objectRect=getRect(zoom);

			final Shape clipShape=graphics.getClip();
			graphics.setClip(drawRect);
			graphics.setColor(Color.GRAY);
			graphics.drawString("id="+getId(),objectRect.x,objectRect.y+objectRect.height+infoFontAscent);
			graphics.setClip(clipShape);
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü ("Verbindungsecke")
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Vertex.Name");
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();
		boolean needSeparator=false;

		if (connectionsIn!=null && connectionsIn.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesIn")));
			item.addActionListener(e->{
				for (ModelElementEdge element : new ArrayList<>(connectionsIn)) surface.remove(element);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}
		if (connectionOut!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener(e->surface.remove(connectionOut));
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}

		if (connectionsIn!=null && connectionsIn.size()==1 && connectionOut!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveThisVertex"),Images.MODELEDITOR_ELEMENT_VERTEX_DELETE.getIcon()));
			item.addActionListener(e->{
				/* Ziel für erste Kante austragen */
				final ModelElementEdge edge=connectionsIn.get(0);
				connectionsIn.clear();
				/* Zweite Kante entfernen */
				final ModelElementPosition element2=(ModelElementPosition)connectionOut.getConnectionEnd();
				element2.removeConnectionNotify(connectionOut);
				surface.remove(connectionOut);
				/* Diese Ecke entfernen */
				surface.remove(this);
				/* Neues Ziel für erste Kante */
				edge.setConnectionEnd(element2);
				element2.addEdgeIn(edge);
			});
		}

		if (needSeparator) popupMenu.addSeparator();
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionsIn!=null) {
			while (connectionsIn.size()>0) {
				ModelElement element=connectionsIn.remove(0);
				surface.remove(element);
			}
		}
		if (connectionOut!=null) surface.remove(connectionOut);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionOut==element) {connectionOut=null; fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Vertex.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionOut!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connectionOut.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
		}
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
			}
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				connectionOutId=I;
			}
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementVertex";
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * Fügt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die einlaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0) {
			connectionsIn.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return connectionOut==null;
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (connectionOut!=null) return false;
		if (edge==null || connectionsIn.indexOf(edge)>=0) return false;
		connectionOut=edge;
		connectionOutId=-1;
		fireChanged();
		return true;
	}

	/**
	 * Auslaufende Kante
	 * @return	Auslaufende Kante
	 */
	@Override
	public ModelElementEdge getEdgeOut() {
		return connectionOut;
	}

	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connectionsIn.toArray(new ModelElementEdge[0]);
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		specialOutputShape(outputBuilder,Color.DARK_GRAY,1,new Color(235,235,235));
	}
}