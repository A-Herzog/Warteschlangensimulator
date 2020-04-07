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
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse hält die Daten zu den Transportzielen
 * für Transportquellen vor.
 * @author Alexander Herzog
 * @see ModelElementTransportSource
 */
public final class TransportTargetSystem implements Cloneable {
	/**
	 * Auf welche Art soll das Routing erfolgen?
	 * @see TransportTargetSystem#getMode()
	 * @see TransportTargetSystem#setMode(RoutingMode)
	 */
	public enum RoutingMode {
		/**
		 * Transport gemäß hier explizit hinterlegtem Zielstationenplan
		 */
		ROUTING_MODE_EXPLICITE,

		/**
		 * Transport gemäß dem Fertigungsplan des Kunden
		 */
		ROUTING_MODE_SEQUENCE,

		/**
		 * Routing gemäß dem Wert in einer Eigenschaft des Kunden
		 * @see TransportTargetSystem#getRoutingProperty()
		 * @see TransportTargetSystem#setRoutingProperty(String)
		 */
		ROUTING_MODE_TEXT_PROPERTY,
	}

	private RoutingMode mode;
	private final List<TransportTargetRecord> routing;
	private String property;
	private String defaultStation;

	/**
	 * Konstruktor der Klasse
	 */
	public TransportTargetSystem() {
		mode=RoutingMode.ROUTING_MODE_EXPLICITE;
		routing=new ArrayList<>();
		property="";
		defaultStation="";
	}

	/**
	 * Liefert den gewählten Modus zur Auswahl der Zielstation
	 * @return	Modus zur Auswahl der Zielstation
	 * @see RoutingMode#ROUTING_MODE_EXPLICITE
	 * @see RoutingMode#ROUTING_MODE_SEQUENCE
	 * @see RoutingMode#ROUTING_MODE_TEXT_PROPERTY
	 */
	public RoutingMode getMode() {
		return mode;
	}

	/**
	 * Stellt den Modus zur Auswahl der Zielstation ein
	 * @param mode	Modus zur Auswahl der Zielstation
	 * @see RoutingMode#ROUTING_MODE_EXPLICITE
	 * @see RoutingMode#ROUTING_MODE_SEQUENCE
	 * @see RoutingMode#ROUTING_MODE_TEXT_PROPERTY
	 */
	public void setMode(final RoutingMode mode) {
		this.mode=mode;
	}

	/**
	 * Liefert die Liste der Routing-Einträge
	 * @return	Routing-Liste
	 */
	public List <TransportTargetRecord> getExpliciteRouting() {
		return routing;
	}

	/**
	 * Liefert den Namen der Station, zu der die Kunden geroutet werden sollen, wenn keine der Routing-Regeln zutrifft.
	 * @return	Name der Zielstation, wenn keine der Routing-Regeln zutrifft
	 * @see #getRouting()
	 */
	public String getDefaultStation() {
		return defaultStation;
	}

	/**
	 * Stellt den Namen der Station ein, zu der die Kunden geroutet werden sollen, wenn keine der Routing-Regeln zutrifft.
	 * @param defaultStation	Name der Zielstation, wenn keine der Routing-Regeln zutrifft
	 * @see #getRouting()
	 */
	public void setDefaultStation(final String defaultStation) {
		if (defaultStation!=null) this.defaultStation=defaultStation;
	}

	/**
	 * Liefert die Liste der Routing-Einträge
	 * @return	Routing-Liste
	 */
	public List <TransportTargetRecord> getRouting() {
		return routing;
	}


	/**
	 * Soll das Routing-Ziel der Kunden gemäß dem Textwert einer Kundeneigenschaft
	 * gewählt werden, so liefert diese Methode den Namen der Kundeneigenschaft.
	 * @return	Name der Text-Kundeneigenschaft zur Bestimmung des Routing-Ziels
	 * @see RoutingMode#ROUTING_MODE_TEXT_PROPERTY
	 * @see #setRoutingProperty(String)
	 */
	public String getRoutingProperty() {
		return property;
	}

	/**
	 * Soll das Routing-Ziel der Kunden gemäß dem Textwert einer Kundeneigenschaft
	 * gewählt werden, so kann über diese Methode der Namen der Kundeneigenschaft eingestellt werden.
	 * @param property	Name der Text-Kundeneigenschaft zur Bestimmung des Routing-Ziels
	 * @see RoutingMode#ROUTING_MODE_TEXT_PROPERTY
	 * @see #getRoutingProperty()
	 */
	public void setRoutingProperty(final String property) {
		if (property!=null) this.property=property;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param transportTargetSystem	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	public boolean equalsTransportTargetSystem(final TransportTargetSystem transportTargetSystem) {
		if (transportTargetSystem==null) return false;

		if (mode!=transportTargetSystem.mode) return false;
		if (mode==RoutingMode.ROUTING_MODE_EXPLICITE) {
			if (routing.size()!=transportTargetSystem.routing.size()) return false;
			for (int i=0;i<routing.size();i++) if (!routing.get(i).equalsTransportRouting(transportTargetSystem.routing.get(i))) return false;
		}
		if (mode==RoutingMode.ROUTING_MODE_TEXT_PROPERTY) {
			if (!property.equals(transportTargetSystem.property)) return false;
		}
		if (!defaultStation.equals(transportTargetSystem.defaultStation)) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @return	Kopiertes Element
	 */
	@Override
	public TransportTargetSystem clone() {
		final TransportTargetSystem clone=new TransportTargetSystem();

		clone.mode=mode;
		if (mode==RoutingMode.ROUTING_MODE_EXPLICITE) for (TransportTargetRecord record: routing) clone.routing.add(record.clone());
		clone.property=property;
		clone.defaultStation=defaultStation;

		return clone;
	}

	/**
	 * Informiert das Objekt, dass sich der Name einer Ziel-Station geändert hat
	 * @param oldName	Alter Zielstationname
	 * @param newName	Neuer Zielstationname
	 */
	public void destinationRenamed(final String oldName, final String newName) {
		if (newName==null || newName.isEmpty()) return;
		for (TransportTargetRecord record: routing) if (record.station.equalsIgnoreCase(oldName)) record.station=newName;
		if (defaultStation.equalsIgnoreCase(oldName)) defaultStation=newName;
	}

	/**
	 * Informiert das Objekt, dass sich der Name eines Kundentyps geändert hat
	 * @param oldName	Alter Kundentypname
	 * @param newName	Neuer Kundentypname
	 */
	public void clientTypeRenamed(final String oldName, final String newName) {
		if (newName==null || newName.isEmpty()) return;
		for (TransportTargetRecord route: routing) {
			if (route.routingMode!=TransportTargetRecord.RoutingMode.ROUTING_MODE_CLIENT_TYPE) continue;
			if (route.routingCondition.equalsIgnoreCase(oldName)) route.routingCondition=newName;
		}
	}

	/**
	 * Speichert die Eigenschaften des Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void addPropertiesToXML(final Document doc, final Element node) {
		Element sub;

		if (mode==RoutingMode.ROUTING_MODE_EXPLICITE) {
			for (TransportTargetRecord route: routing) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.Route")));
				switch (route.routingMode) {
				case ROUTING_MODE_CLIENT_TYPE:
					sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Route.ClientType"),route.routingCondition);
					break;
				case ROUTING_MODE_EXPRESSION:
					sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Route.Expression"),route.routingCondition);
					break;
				}
				sub.setTextContent(route.station);
			}
		}

		if (mode==RoutingMode.ROUTING_MODE_SEQUENCE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.RouteBySequence")));
			sub.setTextContent("1");
		}

		if (mode==RoutingMode.ROUTING_MODE_TEXT_PROPERTY) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.RouteByProperty")));
			sub.setTextContent(property);
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.Route")));
		sub.setTextContent(defaultStation);
	}

	/**
	 * Versucht eine Teilinformation aus dem angegebenen xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen. (Passt dieser nicht zu dem Element, so wird er ignoriert.)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String loadProperties(final Element node) {
		if (Language.trAll("Surface.TransportSource.XML.Route",node.getNodeName())) {
			final String clientType=Language.trAllAttribute("Surface.TransportSource.XML.Route.ClientType",node);
			if (clientType!=null && !clientType.trim().isEmpty()) {
				routing.add(TransportTargetRecord.getByClientType(clientType,node.getTextContent()));
				mode=RoutingMode.ROUTING_MODE_EXPLICITE;
				return null;
			}
			final String expression=Language.trAllAttribute("Surface.TransportSource.XML.Route.Expression",node);
			if (expression!=null && !expression.trim().isEmpty()) {
				routing.add(TransportTargetRecord.getByExpression(expression,node.getTextContent()));
				mode=RoutingMode.ROUTING_MODE_EXPLICITE;
				return null;
			}
			defaultStation=node.getTextContent();
			return null;
		}

		if (Language.trAll("Surface.TransportSource.XML.RouteBySequence",node.getNodeName())) {
			mode=RoutingMode.ROUTING_MODE_SEQUENCE;
			return null;
		}

		if (Language.trAll("Surface.TransportSource.XML.RouteByProperty",node.getNodeName())) {
			mode=RoutingMode.ROUTING_MODE_TEXT_PROPERTY;
			property=node.getTextContent();
			return null;
		}

		return null;
	}

	/**
	 * Fügt die Beschreibung für die Daten dieses Objekts als Eigenschaft zu der Beschreibung hinzu
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	public void buildDescriptionProperty(final ModelDescriptionBuilder descriptionBuilder) {
		/* Modus */
		switch (mode) {
		case ROUTING_MODE_EXPLICITE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTargetSystem.Mode"),Language.tr("ModelDescription.TransportTargetSystem.Mode.Explicite"),100000);
			break;
		case ROUTING_MODE_SEQUENCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTargetSystem.Mode"),Language.tr("ModelDescription.TransportTargetSystem.Mode.Sequence"),100000);
			break;
		case ROUTING_MODE_TEXT_PROPERTY:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTargetSystem.Mode"),Language.tr("ModelDescription.TransportTargetSystem.Mode.Property")+": "+property,100000);
			break;
		}

		/* Wenn Ziele hier definiert... */
		if (mode==RoutingMode.ROUTING_MODE_EXPLICITE) {
			for (TransportTargetRecord record: routing) {
				switch (record.routingMode) {
				case ROUTING_MODE_CLIENT_TYPE:
					descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.TransportTargetSystem.Conditional.ClientType"),record.routingCondition),record.station,101000);
					break;
				case ROUTING_MODE_EXPRESSION:
					descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.TransportTargetSystem.Conditional.Condition"),record.routingCondition),record.station,101000);
					break;
				}
			}
		}

		/* Standardziel */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTargetSystem.DefaultRouting"),defaultStation,102000);
	}
}