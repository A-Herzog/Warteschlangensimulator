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

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeOut;

/**
 * Diese Klasse stellt einen Eingang in ein Submodell dar.
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubIn extends ModelElementSubConnect implements ModelElementEdgeOut {
	/**
	 * Auslaufende Kante
	 */
	private ModelElementEdge connectionOut;

	/** ID der auflaufenden Kante (Wird nur beim Laden und Clonen verwendet.) */
	private int connectionOutId=-1;

	/**
	 * Konstruktor der Klasse <code>ModelElementSubConnect</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param connectionNr	0-basierende Nummer des Ein- bzw. Ausgangs
	 * @param connectionStationID ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll
	 */
	public ModelElementSubIn(final EditModel model, final ModelSurface surface, int connectionNr, int connectionStationID) {
		super(model,surface,connectionNr,connectionStationID);
		if (connectionStationID<0) setName(Language.tr("Surface.SubConnect.NotConnected")); else setName(Language.tr("Surface.SubConnect.FromID")+"="+connectionStationID);
	}

	/**
	 * Stellt die anzuzeigenden Daten bzgl. der Verknüpfung an
	 * @param connectionNr	0-basierende Nummer des Ein- bzw. Ausgangs
	 * @param connectionStationID ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll
	 */
	@Override
	public void setConnectionData(int connectionNr, int connectionStationID) {
		super.setConnectionData(connectionNr,connectionStationID);
		if (connectionStationID<0) setName(Language.tr("Surface.SubConnect.NotConnected")); else setName(Language.tr("Surface.SubConnect.FromID")+"="+connectionStationID);
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSubIn)) return false;

		if (connectionOut==null) {
			if (((ModelElementSubIn)element).connectionOut!=null) return false;
		} else {
			if (((ModelElementSubIn)element).connectionOut==null) return false;
			if (connectionOut.getId()!=((ModelElementSubIn)element).connectionOut.getId()) return false;
		}

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSubIn) {
			if (((ModelElementSubIn)element).connectionOut!=null) connectionOutId=((ModelElementSubIn)element).connectionOut.getId();
			if (connectionStationID<0) setName(Language.tr("Surface.SubConnect.NotConnected")); else setName("Zu id="+connectionStationID);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSubIn clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSubIn element=new ModelElementSubIn(model,surface,connectionNr,connectionStationID);
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
		if (connectionOutId>=0) {
			element=surface.getById(connectionOutId);
			if (element instanceof ModelElementEdge) connectionOut=(ModelElementEdge)element;
			connectionOutId=-1;
		}
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.SubConnect.Input")+" "+(connectionNr+1)+ " ("+((connectionStationID>=0)?(Language.tr("Surface.SubConnect.FromID.Small")+"="+connectionStationID):Language.tr("Surface.SubConnect.NotConnected"))+")";
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.SubConnect.Input")+" "+(connectionNr+1)+ " ("+((connectionStationID>=0)?(Language.tr("Surface.SubConnect.FromID.Small")+"="+connectionStationID):Language.tr("Surface.SubConnect.NotConnected"))+")";
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.SubConnect.Input")+" "+(connectionNr+1);
	}

	/**
	 * Fügt Menüpunkte zum Hinzufügen von einlaufenden und auslaufender Kante zum Kontextmenü
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zurück, wenn Elemente in das Kontextmenü eingefügt wurden (und ggf. ein Separator vor dem nächsten Abschnitt gesetzt werden sollte)
	 */
	@Override
	protected final boolean addRemoveEdgesContextMenuItems(final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();

		if (connectionOut!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgeOut")));
			item.addActionListener(e->surface.remove(connectionOut));
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);
		}

		return false;
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionOut!=null) surface.remove(connectionOut);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionOut==element) {connectionOut=null; fireChanged();}
	}


	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.SubConnect.XML.Input");
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
			final Integer I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				connectionOutId=I;
			}
			return null;
		}

		return null;
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
		if (edge==null) return false;
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
}
