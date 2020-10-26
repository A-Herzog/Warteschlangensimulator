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
 * Daten f�r alle Fertigungspl�ne
 * @author Alexander Herzog
 * @see ModelSequence
 */
public final class ModelSequences implements Cloneable {
	/**
	 * Name des XML-Elements, das die Fertigungspl�ne-Daten enth�lt
	 */
	public static String[] XML_NODE_NAME=new String[]{"Fertigungsplaene"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Liste der Fertigungspl�ne
	 * @see #getSequences()
	 */
	private final List<ModelSequence> sequences;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelSequences() {
		sequences=new ArrayList<>();
	}

	/**
	 * Liefert die Liste der Fertigungspl�ne
	 * @return	Liste der Fertigungspl�ne
	 */
	public List<ModelSequence> getSequences() {
		return sequences;
	}

	/**
	 * L�scht alle Fertigungspl�ne
	 */
	public void clear() {
		sequences.clear();
	}

	/**
	 * Vergleicht zwei Fertigungspl�ne
	 * @param otherSequences	Anderer Fertigungsplan f�r den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Fertigungspl�ne inhaltlich identisch sind
	 */
	public boolean equalsModelSequences(final ModelSequences otherSequences) {
		if (otherSequences==null) return false;
		if (sequences.size()!=otherSequences.sequences.size()) return false;
		for (int i=0;i<sequences.size();i++) if (!sequences.get(i).equalsModelSequence(otherSequences.sequences.get(i))) return false;
		return true;
	}

	/**
	 * Kopiert die Fertigungspl�ne aus einer anderen Fertigungspl�ne-Liste in dieses Objekt
	 * @param otherSequences	Quell-Fertigungspl�ne-Liste aus der die Daten kopiert werden sollen
	 */
	public void setDataFrom(final ModelSequences otherSequences) {
		clear();
		for (ModelSequence sequence: otherSequences.sequences) sequences.add(sequence.clone());
	}

	/**
	 * Erstellt eine Kopie der Fertigungspl�neliste
	 */
	@Override
	public ModelSequences clone() {
		final ModelSequences clone=new ModelSequences();
		for (ModelSequence sequence: sequences) clone.sequences.add(sequence.clone());
		return clone;
	}

	/**
	 * Speichert das Fertigungsplan-Element in einem xml-Knoten
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		if (sequences.size()==0) return;

		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (ModelSequence sequence: sequences) sequence.addDataToXML(doc,node);
	}

	/**
	 * Versucht die Daten des Fertigungspl�ne-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Fertigungspl�ne-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			for (String test: ModelSequence.XML_NODE_NAME) if (e.getNodeName().equalsIgnoreCase(test)) {
				final ModelSequence sequence=new ModelSequence();
				final String error=sequence.loadFromXML(e);
				if (error!=null) return error;
				sequences.add(sequence);
				break;
			}
		}

		return null;
	}
}