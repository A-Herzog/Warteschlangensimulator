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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.elements.FreeResourcesListener;
import simulator.runmodel.RunData;

/**
 * Diese Klasse hält die Informationen für das Editor-Modell vor
 * wie viele Bediener welchen Typs vorhanden sind.
 * @author Alexander Herzog
 * @see EditModel
 * @see ModelResource
 */
public final class ModelResources implements Cloneable {
	/**
	 * Name des XML-Elements, das die Ressourcen-Elemente enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"Ressourcen"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	private List<ModelResource> list;

	/**
	 * Mögliche Modi zur Bestimmung der Stations-Ressourcen-Priorität bei Gleichstand in der ersten Ebene
	 * @author Alexander Herzog
	 * @see RunData#fireReleasedResourcesNotify(simulator.runmodel.SimulationData)
	 */
	public enum SecondaryResourcePriority {
		/**
		 * Zufällige Wahl der Station bei Gleichstand
		 */
		RANDOM,

		/**
		 * Betrachtung der Kundenpriorität bei Gleichstand
		 * @see FreeResourcesListener#getSecondaryResourcePriority(simulator.runmodel.SimulationData)
		 */
		CLIENT_PRIORITY
	}

	/**
	 * Art der Bestimmung der Stationsreihenfolge in Bezug auf die Ressourcen-Priorität bei Gleichstand in der ersten Ebene
	 * @see ModelResources.SecondaryResourcePriority
	 */
	public SecondaryResourcePriority secondaryResourcePriority;

	/**
	 * Konstruktor der Klasse <code>ModelResources</code>
	 */
	public ModelResources() {
		list=new ArrayList<>();
		secondaryResourcePriority=SecondaryResourcePriority.RANDOM;
	}

	/**
	 * Liefert die Anzahl an definierten Ressourcen
	 * @return	Anzahl an definierten Ressourcen
	 */
	public int count() {
		return list.size();
	}

	/**
	 * Liefert die Daten für eine Ressource eines bestimmten Typs.<br>
	 * Existiert noch keine Ressource mit dem angegebenen Namen, so wird eine entsprechende Ressource angelegt.
	 * @param name	Name der Ressource (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @return	Ressourcenobjekt
	 * @see #getNoAutoAdd(String)
	 */
	public ModelResource get(final String name) {
		ModelResource resource=getNoAutoAdd(name);
		if (resource==null) list.add(resource=new ModelResource(name));
		return resource;
	}

	/**
	 * Liefert die Daten für eine Ressource eines bestimmten Typs.<br>
	 * Existiert noch keine Ressource mit dem angegebenen Namen, so liefert die Funktion <code>null</code>.
	 * @param name	Name der Ressource (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @return	Ressourcenobjekt
	 * @see #get(String)
	 */
	public ModelResource getNoAutoAdd(final String name) {
		for (ModelResource resource: list) if (resource.getName().equalsIgnoreCase(name)) return resource;
		return null;
	}

	/**
	 * Fügt eine Ressource zur Liste der Ressourcen hinzu
	 * @param resource	Bedienerobjekt
	 */
	public void add(final ModelResource resource) {
		list.add(resource);
	}

	/**
	 * Stellt die Daten zu einem Bedienerobjekt ein
	 * @param resource Bedienerobjekt
	 */
	public void set(final ModelResource resource) {
		if (resource==null) return;
		final int index=getIndex(resource.getName());
		if (index>=0) list.set(index,resource); else list.add(resource);
	}

	/**
	 * Löscht den Datensatz zu einem Bedienertyps
	 * @param name	Name des Bedienertyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden), der gelöscht werden soll
	 */
	public void delete(final String name) {
		for (int i=0;i<list.size();i++) if (list.get(i).getName().equalsIgnoreCase(name)) {
			list.remove(i);
			break;
		}
	}

	/**
	 * Liefert den Namen einer Bedienergruppe basierend auf dem Index in der Liste
	 * @param index	Index des Eintrags
	 * @return	Name der Bedienergruppe
	 * @see #size()
	 */
	public String getName(final int index) {
		if (index<0 || index>=list.size()) return null;
		return list.get(index).getName();
	}

	/**
	 * Liefert den Index einer Bedienergruppe basierend auf dem Namen
	 * @param name	Name des Eintrags
	 * @return	Index der Bedienergruppe
	 * @see #size()
	 */
	public int getIndex(final String name) {
		for (int i=0;i<list.size();i++) if (list.get(i).getName().equalsIgnoreCase(name)) return i;
		return -1;
	}

	/**
	 * Ändert den Namen einer Ressource
	 * @param index	Index des Eintrags in der Liste
	 * @param name	Neuer Name
	 */
	public void changeName(final int index, final String name) {
		if (index<0 || index>=list.size()) return;
		list.get(index).setName(name);
	}

	/**
	 * Liefert eine Liste mit den Namen aller vorhandenen Bedienertypen
	 * @return	Liste der Namen aller registrierten Bedienertypen
	 */
	public String[] list() {
		final List<String> names=new ArrayList<>();
		for (ModelResource resource: list) names.add(resource.getName());
		return names.toArray(new String[0]);
	}

	/**
	 * Liefert eine Liste mit allen vorhandenen Bedienertypen
	 * @return	Liste aller registrierten Bedienertypen
	 */
	public ModelResource[] getResources() {
		return list.toArray(new ModelResource[0]);
	}

	/**
	 * Liefert die Anzahl an vorhandenen Bedienertypen
	 * @return	Anzahl an Bedienertypen
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Löscht alle Bedienertypen
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * Speichert das Ressourcen-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);
		switch (secondaryResourcePriority) {
		case RANDOM:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.SecondaryResourcePriority"),Language.trPrimary("Surface.XML.Resource.SecondaryResourcePriority.Random"));
			break;
		case CLIENT_PRIORITY:
			node.setAttribute(Language.trPrimary("Surface.XML.Resource.SecondaryResourcePriority"),Language.trPrimary("Surface.XML.Resource.SecondaryResourcePriority.ClientPriority"));
			break;
		}

		if (list.size()==0) return;
		for (ModelResource resource: list) resource.addDataToXML(doc,node);
	}

	/**
	 * Versucht die Daten des Ressourcen-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final String mode=Language.trAllAttribute("Surface.XML.Resource.SecondaryResourcePriority",node);
		if (Language.trAll("Surface.XML.Resource.SecondaryResourcePriority.Random",mode)) secondaryResourcePriority=SecondaryResourcePriority.RANDOM;
		if (Language.trAll("Surface.XML.Resource.SecondaryResourcePriority.ClientPriority",mode)) secondaryResourcePriority=SecondaryResourcePriority.CLIENT_PRIORITY;

		clear();

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			for (String test: ModelResource.XML_NODE_NAME) if (e.getNodeName().equalsIgnoreCase(test)) {
				ModelResource resource=new ModelResource();
				final String error=resource.loadFromXML(e);
				if (error!=null) return error;
				list.add(resource);
				break;
			}
		}

		return null;
	}

	/**
	 * Erstellt eine Kopie des <code>ModelResources</code>-Objekts
	 */
	@Override
	public ModelResources clone() {
		final ModelResources clone=new ModelResources();
		clone.secondaryResourcePriority=secondaryResourcePriority;
		for (ModelResource resource: list) clone.list.add(resource.clone());

		return clone;
	}

	/**
	 * Löscht alle Einträge in der Ressourcenliste und übernimmt die Daten aus einer anderen Liste
	 * @param otherResources	Ressourcenliste aus der die Daten in diese Liste übernommen werden sollen.
	 */
	public void setDataFrom(final ModelResources otherResources) {
		secondaryResourcePriority=otherResources.secondaryResourcePriority;
		clear();
		for (ModelResource resource: otherResources.list) list.add(resource.clone());
	}

	/**
	 * Prüft, ob zwei <code>ModelResources</code>-Objekte inhaltlich identisch sind
	 * @param otherResources	Das zweite <code>ModelResources</code>-Objekt, welches mit diesem Objekt verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn beide Objekte die gleichen Bedienerdaten enthalten
	 */
	public boolean equalsResources(final ModelResources otherResources) {
		if (otherResources==null) return false;
		if (secondaryResourcePriority!=otherResources.secondaryResourcePriority) return false;
		if (list.size()!=otherResources.list.size()) return false;
		for (int i=0;i<list.size();i++) if (!list.get(i).equalsModelResource(otherResources.list.get(i))) return false;

		return true;
	}

	/**
	 * Liefert einen bislang noch nicht verwendeten Namen für eine neue Ressourcengruppe
	 * @return	Name für neue Ressourcengruppe
	 */
	public String getNextAvailableResouceName() {
		final String namePrefix=Language.tr("Surface.Resource.EditName.Dialog.AddNew.DefaultName");
		int count=1;
		while (true) {
			String name=namePrefix;
			if (count>1) name+=" "+count;
			if (getNoAutoAdd(name)==null) return name;
			count++;
		}
	}
}
