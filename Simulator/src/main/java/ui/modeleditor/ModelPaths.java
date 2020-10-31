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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse hält die Informationen für die Pfadeditor vor.
 * @author Alexander Herzog
 * @see EditModel
 */
public final class ModelPaths implements Cloneable {
	/**
	 * Name des XML-Elements, das die Pfaddaten enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellPfadsegmente"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Enthält die in diesem Objekt vorgehaltenen Von-Nach-Verbindungen
	 */
	public final Map<String,List<String>> connections;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelPaths() {
		connections=new HashMap<>();
	}

	/**
	 * Löscht alle Daten in dem Objekt.
	 */
	public void clear() {
		connections.clear();
	}

	/**
	 * Vergleicht ob zwei Listen exakt dieselben Einträge (ggf. in abweichender Reihenfolge) beinhalten.
	 * @param list1	Erste Liste
	 * @param list2	Zweite Liste
	 * @return	Liefert <code>true</code>, wenn beide Listen dieselben Eintäge beinhalten
	 */
	private boolean compareLists(final List<String> list1, final List<String> list2) {
		if (list1==null || list2==null) return false;
		if (list1.size()!=list2.size()) return false;

		for (String entry: list1) if (!list2.contains(entry)) return false;
		for (String entry: list2) if (!list1.contains(entry)) return false;

		return true;
	}

	/**
	 * Vergleicht zwei Pfadsegmente-Objekte
	 * @param otherPaths	Anderes Pfadsegmente-Objekt für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Pfadsegmente-Objekte inhaltlich identisch sind
	 */
	public boolean equalsModelPaths(final ModelPaths otherPaths) {
		if (otherPaths==null) return false;

		if (connections.size()!=otherPaths.connections.size()) return false;
		for (Map.Entry<String,List<String>> entry: connections.entrySet()) {
			if (!compareLists(entry.getValue(),otherPaths.connections.get(entry.getKey()))) return false;
		}

		return true;
	}

	/**
	 * Stellt die Daten in diesem Pfadsegmente-Objekt auf die Werte aus einem anderen Pfadsegmente-Objekt
	 * @param otherPaths	Anderes Pfadsegmente-Objekt dessen Daten in dieses Objekt übertragen werden sollen
	 */
	public void setDataFrom(final ModelPaths otherPaths) {
		for (Map.Entry<String,List<String>> entry: otherPaths.connections.entrySet()) {
			final List<String> list=new ArrayList<>();
			list.addAll(entry.getValue());
			connections.put(entry.getKey(),list);
		}
	}

	/**
	 * Erstellt eine Kopie des Pfadsegmente-Objekts
	 */
	@Override
	public ModelPaths clone() {
		final ModelPaths clone=new ModelPaths();
		clone.setDataFrom(this);
		return clone;
	}

	/**
	 * Versucht die Daten eines Pfadsegmente-Objekts aus einem xml-Element zu laden
	 * @param node	XML-Element, das die Daten des Pfadsegmente-Objekts beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			if (Language.trAll("Surface.XML.PathSegments.Connection",e.getNodeName())) {
				final String origin=Language.trAllAttribute("Surface.XML.PathSegments.Connection.From",e);
				final String destination=Language.trAllAttribute("Surface.XML.PathSegments.Connection.To",e);
				if (!origin.isEmpty() && !destination.isEmpty()) {
					List<String> list=connections.get(origin);
					if (list==null) {
						list=new ArrayList<>();
						connections.put(origin,list);
					}
					if (!list.contains(destination)) list.add(destination);
				}
			}
		}

		return null;
	}

	/**
	 * Speichert die Daten des Pfadsegmente-Objekts in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		if (connections.size()==0) return;

		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (Map.Entry<String,List<String>> entry: connections.entrySet()) {
			for (String destination: entry.getValue()) {
				Element sub=doc.createElement(Language.trPrimary("Surface.XML.PathSegments.Connection"));
				node.appendChild(sub);
				sub.setAttribute(Language.trPrimary("Surface.XML.PathSegments.Connection.From"),entry.getKey());
				sub.setAttribute(Language.trPrimary("Surface.XML.PathSegments.Connection.To"),destination);
			}
		}
	}
}
