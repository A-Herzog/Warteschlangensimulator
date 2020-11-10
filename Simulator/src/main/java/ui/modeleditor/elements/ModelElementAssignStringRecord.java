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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;

/**
 * Diese Klasse h�lt die konkreten Daten f�r die Textzuweisungen vor.
 * @author Alexander Herzog
 * @see ModelElementAssignString
 * @see ModelElementSourceRecord
 */
public final class ModelElementAssignStringRecord implements Cloneable {
	/**
	 * Liste der Schl�ssel
	 * @see #getKeys()
	 */
	private final List<String> keys;

	/**
	 * Liste der zuzuweisenden Werte
	 * @see #getValues()
	 */
	private final List<String> values;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementAssignStringRecord() {
		keys=new ArrayList<>();
		values=new ArrayList<>();
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param copySource	Ausgangselement dessen Daten kopiert werden sollen
	 */
	public ModelElementAssignStringRecord(final ModelElementAssignStringRecord copySource) {
		this();
		copyDataFrom(copySource);
	}

	/**
	 * Vergleicht diesen Datensatz mit einem weiteren
	 * @param otherRecord	Zweiter Datensatz, der mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn beide Datens�tze inhaltlich �bereinstimmen
	 */
	public boolean equalsModelElementAssignStringRecord(final ModelElementAssignStringRecord otherRecord) {
		if (otherRecord==null) return false;

		if (otherRecord.keys.size()!=keys.size()) return false;
		for (int i=0;i<keys.size();i++) if (!otherRecord.keys.get(i).equals(keys.get(i))) return false;
		if (otherRecord.values.size()!=values.size()) return false;
		for (int i=0;i<values.size();i++) if (!otherRecord.values.get(i).equals(values.get(i))) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param otherRecord	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	public void copyDataFrom(final ModelElementAssignStringRecord otherRecord) {
		if (otherRecord==null) return;

		keys.clear();
		keys.addAll(otherRecord.keys);
		values.clear();
		values.addAll(otherRecord.values);
	}

	@Override
	public ModelElementAssignStringRecord clone() {
		return new ModelElementAssignStringRecord(this);
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param parent	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element sub;

		for (int i=0;i<Math.min(keys.size(),values.size());i++) {
			parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.AssignString.XML.Assignment")));
			sub.setAttribute(Language.trPrimary("Surface.AssignString.XML.Assignment.Key"),keys.get(i));
			sub.setTextContent(values.get(i));
		}
	}

	/**
	 * Gibt an, ob ein XML-Element eines zum Laden eines Datenelements ist
	 * @param node	XML-Element bei dem gepr�ft werden soll, ob es sich um einen Zuweisungsdatensatz handelt
	 * @return	Gibt <code>true</code> zur�ck, wenn es sich um einen Zuweisungsdatensatz handelt
	 */
	public static boolean isSetNode(final Element node) {
		final String nodeName=node.getNodeName();
		if (Language.trAll("Surface.AssignString.XML.Assignment",nodeName)) return true;
		if (Language.trAll("Surface.AssignString.XML.Key",nodeName)) return true;
		if (Language.trAll("Surface.AssignString.XML.Value",nodeName)) return true;
		return false;
	}

	/**
	 * Zu ladender Schl�ssel (im alten XML-Format)
	 * @see #loadXMLNode(Element)
	 */
	private String loadKey=null;

	/**
	 * Zu ladender Wert (im alten XML-Format)
	 * @see #loadXMLNode(Element)
	 */
	private String loadValue=null;

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	public String loadXMLNode(final Element node) {
		if (!isSetNode(node)) return null;

		if (Language.trAll("Surface.AssignString.XML.Assignment",node.getNodeName())) {
			final String key=Language.trAllAttribute("Surface.AssignString.XML.Assignment.Key",node);
			if (!key.trim().isEmpty()) {
				keys.add(key);
				values.add(node.getTextContent());
			}
			return null;
		}

		if (Language.trAll("Surface.AssignString.XML.Key",node.getNodeName())) {
			if (loadValue==null) {
				loadKey=node.getTextContent();
			} else {
				keys.add(node.getTextContent());
				values.add(loadValue);
				loadKey=null;
				loadValue=null;
			}
			return null;
		}

		if (Language.trAll("Surface.AssignString.XML.Value",node.getNodeName())) {
			if (loadKey==null) {
				loadValue=node.getTextContent();
			} else {
				keys.add(loadKey);
				values.add(node.getTextContent());
				loadKey=null;
				loadValue=null;
			}
			return null;
		}

		return null;
	}

	/**
	 * Liefert die Liste aller Schl�ssel
	 * @return	Liste aller Schl�ssel
	 */
	public List<String> getKeys() {
		return keys;
	}

	/**
	 * Liefert die Liste aller Werte
	 * @return	Liste aller Werte
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Liefert die Liste der Zuweisungen f�r die Ausgabe in Form einer Beschreibung
	 * @return	Liste der Zuweisungen
	 */
	public List<String> getDescription() {
		final List<String> list=new ArrayList<>();
		for (int i=0;i<Math.min(keys.size(),values.size());i++) list.add(keys.get(i)+":="+values.get(i));
		return list;
	}
}
