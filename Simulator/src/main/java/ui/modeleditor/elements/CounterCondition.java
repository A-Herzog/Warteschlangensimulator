/**
 * Copyright 2023 Alexander Herzog
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
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse kapselt optionale Bedingungen für die Auslösung eines Zählers.
 * @author Alexander Herzog
 */
public class CounterCondition {
	/**
	 * Bedingung, die für eine Zählung erfüllt sein muss
	 */
	private String condition;

	/**
	 * Zu zählende Kundentypen
	 */
	private final List<String> clientTypes;

	/**
	 * Konstruktor der Klasse
	 */
	public CounterCondition() {
		condition="";
		clientTypes=new ArrayList<>();
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param copySource	Zu kopierendes Ausgangsobjekt
	 */
	public CounterCondition(final CounterCondition copySource) {
		this();
		copyFrom(copySource);
	}

	/**
	 * Kopiert die Daten aus einem anderen Objekt dieses Typs in dieses
	 * @param copySource	Zu kopierendes Ausgangsobjekt
	 */
	public void copyFrom(final CounterCondition copySource) {
		if (copySource==null) return;
		condition=copySource.condition;
		clientTypes.clear();
		clientTypes.addAll(copySource.clientTypes);
	}

	/**
	 * Prüft, ob dieses Objekt inhaltlich identisch mit einem zweiten Objekt desselben Typs ist.
	 * @param otherCounterCondition	Anderes Objekt zum Vergleichen
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsCounterCondition(final CounterCondition otherCounterCondition) {
		if (otherCounterCondition==null) return false;
		if (!condition.equalsIgnoreCase(otherCounterCondition.condition)) return false;
		if (!Objects.deepEquals(clientTypes,otherCounterCondition.clientTypes)) return false;
		return true;
	}

	/**
	 * Liefert die Bedingung, die für eine Zählung erfüllt sein muss.
	 * @return	Bedingung, die für eine Zählung erfüllt sein muss
	 * @see #setCondition(String)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung, die für eine Zählung erfüllt sein muss, ein.
	 * @param condition	Bedingung, die für eine Zählung erfüllt sein muss
	 * @see #getCondition()
	 */
	public void setCondition(final String condition) {
		this.condition=(condition==null)?"":condition;
	}

	/**
	 * Liefert die Liste der zu zählenden Kundentypen.
	 * @return	Zu zählende Kundentypen
	 */
	public List<String> getClientTypes() {
		return clientTypes;
	}

	/**
	 * Speichert die Einstellungen in Unterelementen eines XML-Knotens.
	 * @param parent	Übergeordneter XML-Knotens
	 */
	public void saveToXML(final Element parent) {
		final Document doc=parent.getOwnerDocument();
		Element node;

		if (!condition.isBlank()) {
			parent.appendChild(node=doc.createElement(Language.tr("Surface.CounterCondition.Condition")));
			node.setTextContent(condition);
		}

		for (String clientType: clientTypes) {
			parent.appendChild(node=doc.createElement(Language.tr("Surface.CounterCondition.ClientType")));
			node.setTextContent(clientType);
		}
	}

	/**
	 * Handelt es sich bei dem XML-Knoten um einen Eintrag mit Daten zu diesem Objekt?
	 * @param node	Zu prüfender XML-Knoten
	 * @return	Liefert <code>true</code>, wenn {@link #loadFromXML(Element)} dieses XML-Element verarbeiten kann
	 */
	public boolean isCounterConditionElement(final Element node) {
		final String name=node.getNodeName();
		if (Language.trAll("Surface.CounterCondition.Condition",name)) return true;
		if (Language.trAll("Surface.CounterCondition.ClientType",name)) return true;
		return false;

	}

	/**
	 * Lädt Daten aus einem XML-Knoten
	 * @param node	XML-Knoten aus dem die Daten geladen werden sollen
	 */
	public void loadFromXML(final Element node) {
		final String name=node.getNodeName();

		if (Language.trAll("Surface.CounterCondition.Condition",name)) {
			condition=node.getTextContent().trim();
		}
		if (Language.trAll("Surface.CounterCondition.ClientType",name)) {
			clientTypes.add(node.getTextContent().trim());
		}
	}

	/**
	 * Benachrichtigt das Objekt, dass ein Kundentyp umbenannt wurde.
	 * @param oldClientTypeName	Alter Kundentypname
	 * @param newClientTypeName	Neuer Kundentypname
	 */
	public void clientTypeRenamed(final String oldClientTypeName, final String newClientTypeName) {
		if (oldClientTypeName==null || oldClientTypeName.isBlank() || newClientTypeName==null || newClientTypeName.isEmpty()) return;
		final int index=clientTypes.indexOf(oldClientTypeName);
		if (index>=0) clientTypes.set(index,newClientTypeName);
	}

	/**
	 * Gibt die Einstellungen in diesem Objekt als Elementbeschreibung aus
	 * @param descriptionBuilder	Elementbeschreibung-Generator
	 * @param position	Position, an der die Daten ausgegeben werden sollen
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder, final int position) {
		if (!condition.isBlank()) {
			descriptionBuilder.addProperty(Language.tr("Surface.CounterCondition.Condition.Name"),condition,2000);
		}

		for (String clientType: clientTypes) {
			descriptionBuilder.addProperty(Language.tr("Surface.CounterCondition.ClientType.Name"),clientType,2000);
		}
	}
}
