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

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse hält die Daten zu der für einen Transport
 * benötigten Ressource für Transportquellen vor.
 * @author Alexander Herzog
 * @see ModelElementTransportSource
 */
public final class TransportResourceRecord implements Cloneable {
	/**
	 * Bedienergruppen und deren Anzahlen, die für die Bedienung der Kunden notwendig sind
	 * @see #getResources()
	 */
	private final Map<String,Integer> resources; /* Name der Ressource, benötigte Anzahl */

	/**
	 * Ausdruck zur Berechnung der Priorität für die Ressourcenzuweisung
	 * @see #getResourcePriority()
	 * @see #setResourcePriority(String)
	 */
	private String resourcePriority;

	/**
	 * Objekt mit Daten zur verzögerten Ressourcenfreigabe
	 * @see #getDelayedRelease()
	 */
	private DistributionSystem delayedRelease;

	/**
	 * Verwendete Zeitbasis (ob die Verteilungs-/Ausdruckswerte für die verzögerte Ressourcenfreigabe Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Konstruktor der Klasse
	 */
	public TransportResourceRecord() {
		resources=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		resourcePriority=ModelElementProcess.DEFAULT_RESOURCE_PRIORITY;
		delayedRelease=new DistributionSystem("Surface.TransportSource.XML.DelayedReleaseTarget",null,true);
		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;

		/* Um sicher zu stellen, dass die Language-Strings auch in den Sprachdateien vorhanden sind. Der folgende DistributionSystem-Konstruktor ist kein Scan-Ziel für die Sprachdateien. */
		Language.tr("Surface.TransportSource.XML.DelayedReleaseTarget");
	}

	/**
	 * Liefert die Aufstellung der zur Bearbeitung von Kunden benötigten Ressourcen
	 * @return	Bedienergruppen und deren Anzahlen, die für die Bedienung der Kunden notwendig sind
	 */
	public Map<String,Integer> getResources() {
		return resources;
	}

	/**
	 * Liefert die Priorität für die Ressourcenzuweisung.
	 * @return	Ausdruck zur Berechnung der Priorität für die Ressourcenzuweisung
	 */
	public String getResourcePriority() {
		return resourcePriority;
	}

	/**
	 * Stellt die die Priorität für die Ressourcenzuweisung ein.
	 * @param priority	Ausdruck zur Berechnung der Priorität für die Ressourcenzuweisung
	 */
	public void setResourcePriority(final String priority) {
		if (priority!=null) this.resourcePriority=priority;
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungs-/Ausdruckswerte für die verzögerte Ressourcenfreigabe Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungs-/Ausdruckswerte für die verzögerte Ressourcenfreigabe Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=timeBase;
	}

	/**
	 * Lieferte die Daten zur verzögerten Ressourcenfreigabe
	 * @return	Objekt mit Daten zur verzögerten Ressourcenfreigabe
	 */
	public DistributionSystem getDelayedRelease() {
		return delayedRelease;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param transportResourceRecord	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	public boolean equalsTransportResourceRecord(final TransportResourceRecord transportResourceRecord) {
		if (transportResourceRecord==null) return false;

		Map<String,Integer> resourcesA=resources;
		Map<String,Integer> resourcesB=transportResourceRecord.resources;
		for (Map.Entry<String,Integer> entry : resourcesA.entrySet()) {
			if (!entry.getValue().equals(resourcesB.get(entry.getKey()))) return false;
		}
		for (Map.Entry<String,Integer> entry : resourcesB.entrySet()) {
			if (!entry.getValue().equals(resourcesA.get(entry.getKey()))) return false;
		}

		if (!resourcePriority.equals(transportResourceRecord.resourcePriority)) return false;

		if (timeBase!=transportResourceRecord.timeBase) return false;

		if (!delayedRelease.equalsDistributionSystem(transportResourceRecord.delayedRelease)) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @return	Kopiertes Element
	 */
	@Override
	public TransportResourceRecord clone() {
		final TransportResourceRecord clone=new TransportResourceRecord();

		clone.resources.putAll(resources);
		clone.resourcePriority=resourcePriority;
		clone.timeBase=timeBase;
		clone.delayedRelease.setData(delayedRelease);

		return clone;
	}

	/**
	 * Informiert das Objekt, dass sich der Name einer Ressource geändert hat
	 * @param oldName	Alter Ressourcenname
	 * @param newName	Neuer Ressourcenname
	 */
	public void resourceRenamed(final String oldName, final String newName) {
		if (newName==null) return;

		final Integer neededNumber=resources.get(oldName);
		if (neededNumber!=null) {
			resources.remove(oldName);
			resources.put(newName,neededNumber);
		}
	}

	/**
	 * Informiert das Objekt, dass sich der Name einer Station geändert hat
	 * @param oldName	Alter Ressourcenname
	 * @param newName	Neuer Ressourcenname
	 */
	public void destinationRenamed(final String oldName, final String newName) {
		if (oldName==null || oldName.isEmpty() || newName==null || newName.isEmpty()) return;
		if (delayedRelease.nameInUse(oldName)) delayedRelease.renameSubType(oldName,newName);
	}

	/**
	 * Speichert die Eigenschaften des Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void addPropertiesToXML(final Document doc, final Element node) {
		if (resources.size()==0) return; /* Keine Ressource verwendet */

		Element sub;

		for (Map.Entry<String,Integer> resource: resources.entrySet()) if (resource.getValue()!=null && resource.getValue().intValue()>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.Resource")));
			sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Resource.Name"),resource.getKey());
			sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Resource.Count"),""+resource.getValue().intValue());
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.ResourcePriority")));
		sub.setTextContent(resourcePriority);

		if (delayedRelease.hasData()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.ResourceDelayedRelease")));
			sub.setAttribute(Language.trPrimary("Surface.ResourceDelayedRelease.XML.Resource.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
			delayedRelease.save(doc,sub,null);
		}
	}

	/**
	 * Versucht eine Teilinformation aus dem angegebenen xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen. (Passt dieser nicht zu dem Element, so wird er ignoriert.)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String loadProperties(final Element node) {

		if (Language.trAll("Surface.TransportSource.XML.Resource",node.getNodeName())) {
			final String resource=Language.trAllAttribute("Surface.TransportSource.XML.Resource.Name",node);
			final String countString=Language.trAllAttribute("Surface.TransportSource.XML.Resource.Count",node);
			if (!countString.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(countString);
				if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
				resources.put(resource,I);
			}
			return null;
		}

		if (Language.trAll("Surface.TransportSource.XML.ResourcePriority",node.getNodeName())) {
			resourcePriority=node.getTextContent();
			return null;
		}

		if (Language.trAll("Surface.TransportSource.XML.ResourceDelayedRelease",node.getNodeName())) {
			timeBase=ModelSurface.getTimeBaseInteger(Language.trAllAttribute("Surface.ResourceDelayedRelease.XML.Resource.TimeBase",node));

			final NodeList l=node.getChildNodes();
			for (int i=0; i<l.getLength();i++) {
				if (!(l.item(i) instanceof Element)) continue;
				final Element e=(Element)l.item(i);

				if (DistributionSystem.isDistribution(e)) {
					final String error=delayedRelease.loadDistribution(e);
					if (error!=null) return error;
				}
				if (DistributionSystem.isExpression(e)) {
					final String error=delayedRelease.loadExpression(e);
					if (error!=null) return error;
				}
			}
		}

		return null;
	}

	/**
	 * Fügt die Beschreibung für die Daten dieses Objekts als Eigenschaft zu der Beschreibung hinzu
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	public void buildDescriptionProperty(final ModelDescriptionBuilder descriptionBuilder) {
		/* Benötigte Ressourcen */
		final StringBuilder sb=new StringBuilder();
		for (Map.Entry<String,Integer> entry: resources.entrySet()) {
			if (entry.getValue().intValue()>0) {
				if (sb.length()>0) sb.append("\n");
				sb.append(String.format(Language.tr("ModelDescription.TransportResourceRecord.Resources.Resource"),entry.getKey(),entry.getValue().intValue()));
			}
		}
		if (sb.length()>0) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportResourceRecord.Resources"),sb.toString(),1000);
		}

		/* Ressourcenpriorität */
		if (resourcePriority==null || resourcePriority.isBlank()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportResourceRecord.ResourcePriority"),ModelElementProcess.DEFAULT_RESOURCE_PRIORITY,2000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportResourceRecord.ResourcePriority"),resourcePriority,2000);
		}

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,3000);

		/* Verzögerte Ressourcenfreigabe */
		delayedRelease.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.TransportResourceRecord.DelayedRelease.ClientType"),Language.tr("ModelDescription.TransportResourceRecord.DelayedRelease.GeneralCase"),4000);
	}

	/**
	 * Sucht einen Text in den Daten dieses Datensatzes.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		/* Ressourcenzuordnung -> keine Suche */

		/* Ressorcen-Priorisierungs-Formel */
		searcher.testString(station,Language.tr("Surface.TransportSource.Dialog.Ressource.Priority"),resourcePriority,newResourcePriority->{resourcePriority=newResourcePriority;});

		/* Verzögerte Ressourcenfreigabe */
		delayedRelease.search(searcher,station,Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Button"));
	}
}