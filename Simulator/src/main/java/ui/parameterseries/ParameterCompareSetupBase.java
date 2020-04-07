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
package ui.parameterseries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;

/**
 * Basisklasse zum Speichern von Teilinformationen der
 * Parameter-Vergleichs-Funktion
 * @author Alexander Herzog
 * @see ParameterCompareSetup
 */
public abstract class ParameterCompareSetupBase {
	private String name;

	/**
	 * Konstruktor der Klasse
	 */
	public ParameterCompareSetupBase() {
		name="";
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name für das Element
	 */
	public ParameterCompareSetupBase(final String name) {
		this.name=name;
	}

	/**
	 * Liefert den Namen des Elements
	 * @return	Name des Elements
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Stellt einen neuen Namen für das Element ein
	 * @param name	Neuer Name
	 */
	public final void setName(final String name) {
		if (name!=null) this.name=name.trim();
	}

	/**
	 * Liefert die Liste der Sprach-Bezeichner für die XML-Basisklasse Elements
	 * @return	Name der XML-Basisklasse des Elements in den verschiedenen Sprachen (aktuelle Sprache zuerst)
	 */
	public abstract String[] getRootNodeNames();


	/**
	 * Lädt einen einzelnen Unterschlüssel für das Element aus einer XML-Datei.<br>
	 * Diese Methode muss von abgeleiteten Klassen überschrieben werden, aber die Basismethode
	 * muss aufgerufen werden, um den Namen zu laden.
	 * @param name	Name des Unterschlüssels
	 * @param content	Textinhalt es Unterschlüssels
	 * @param node	Unterschlüssel
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected String loadPropertyFromXML(final String name, final String content, final Element node) {
		if (Language.trAll("ParameterCompare.XML.Name",name)) {
			this.name=content;
			return null;
		}

		return null;
	}

	/**
	 * Lädt das Element aus einem XML-Knoten
	 * @param node	XML-Knoten, aus dem das Element geladen werden soll
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public final String loadFromXML(Element node) {
		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			final String error=loadPropertyFromXML(e.getNodeName(),e.getTextContent(),e);
			if (error!=null) return error;
		}

		return null;
	}

	/**
	 * Speichert die Daten des Elements als Unterschlüssel unter einem XML-Knoten.<br>
	 * Diese Methode muss von abgeleiteten Klassen überschrieben werden, aber die Basismethode
	 * muss aufgerufen werden, um den Namen zu speichern.
	 * @param doc	XML-Dokument
	 * @param node	XML-Knoten für das Element
	 */
	protected void addPropertiesToXML(Document doc, Element node) {
		if (name!=null && !name.trim().isEmpty()) {
			final Element sub=doc.createElement(Language.tr("ParameterCompare.XML.Name"));
			node.appendChild(sub);
			sub.setTextContent(name);
		}
	}

	/**
	 * Speichert die Daten des Elements in einem XML-Dokument
	 * @param doc	XML-Dokument
	 * @param parent	Übergeordneter XML-Knoten
	 */
	public final void addDataToXML(Document doc, Element parent) {
		final Element node=doc.createElement(getRootNodeNames()[0]);
		parent.appendChild(node);

		addPropertiesToXML(doc,node);
	}
}
