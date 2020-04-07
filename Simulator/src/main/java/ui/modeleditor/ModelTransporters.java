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

/**
 * Daten für alle Transportergruppen
 * @author Alexander Herzog
 * @see ModelTransporter
 */
public final class ModelTransporters implements Cloneable {
	/**
	 * Name des XML-Elements, das die Transporter-Daten enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"Transporters"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	private final List<ModelTransporter> transporters;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelTransporters() {
		transporters=new ArrayList<>();
	}

	/**
	 * Liefert die Anzahl an definierten Transportern
	 * @return	Anzahl an definierten Transportern
	 */
	public int count() {
		return transporters.size();
	}

	/**
	 * Liefert die Liste der Transporter
	 * @return	Liste der Transporter
	 */
	public List<ModelTransporter> getTransporters() {
		return transporters;
	}

	/**
	 * Liefert eine Liste mit den Namen der Transporter
	 * @return	Liste mit den Namen der Transporter
	 */
	public String[] getNames() {
		return transporters.stream().map(t->t.getName()).toArray(String[]::new);
	}

	/**
	 * Liefert ein Transporterobjekt zu einem Transporternamen
	 * @param name	Name des Transporterobjektes
	 * @return	Transporterobjekt oder <code>null</code>, wenn es kein Transporterobjekt mit diesem Namen gibt
	 */
	public ModelTransporter get(final String name) {
		for (ModelTransporter transporter: transporters) if (transporter.getName().equals(name)) return transporter;
		return null;
	}

	/**
	 * Löscht alle Transporterdaten
	 */
	public void clear() {
		transporters.clear();
	}

	/**
	 * Vergleicht zwei Transporterlisten
	 * @param otherTransporters	Andere Transporterliste für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Transporterlisten inhaltlich identisch sind
	 */
	public boolean equalsModelTransporters(final ModelTransporters otherTransporters) {
		if (otherTransporters==null) return false;
		if (transporters.size()!=otherTransporters.transporters.size()) return false;
		for (int i=0;i<transporters.size();i++) if (!transporters.get(i).equalsModelTransporter(otherTransporters.transporters.get(i))) return false;
		return true;
	}

	/**
	 * Kopiert die Transporterdaten aus einer anderen Transporterdaten-Liste in dieses Objekt
	 * @param otherTransporters	Quell-Transporterdaten-Liste aus der die Daten kopiert werden sollen
	 */
	public void setDataFrom(final ModelTransporters otherTransporters) {
		clear();
		for (ModelTransporter transporter: otherTransporters.transporters) transporters.add(transporter.clone());
	}

	/**
	 * Erstellt eine Kopie der Transporterliste
	 */
	@Override
	public ModelTransporters clone() {
		final ModelTransporters clone=new ModelTransporters();
		for (ModelTransporter transporter: transporters) clone.transporters.add(transporter.clone());
		return clone;
	}

	/**
	 * Speichert das Transporterlisten-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		if (transporters.size()==0) return;

		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (ModelTransporter transporter: transporters) transporter.addDataToXML(doc,node);
	}

	/**
	 * Versucht die Daten des Transporterlisten-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Transporterlisten-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			for (String test: ModelTransporter.XML_NODE_NAME) if (e.getNodeName().equalsIgnoreCase(test)) {
				final ModelTransporter transporter=new ModelTransporter();
				final String error=transporter.loadFromXML(e);
				if (error!=null) return error;
				transporters.add(transporter);
				break;
			}
		}

		return null;
	}
}