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
package ui.modeleditor.descriptionbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Basisklasse für die Erzeugung von Beschreibungen für Modelle
 * @author Alexander Herzog
 */
public abstract class ModelDescriptionBuilder {
	/**
	 * Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	private final EditModel model;

	/**
	 * Cache der Liste der Kundentypen<br>
	 * (Wird bei Bedarf dynamisch erstellt.)
	 * @see #getClientTypes()
	 */
	private String[] clientTypes;

	/**
	 * Liste der Elemente, die berücksichtigt werden sollen (kann <code>null</code> sein, dann werden alle Elemente verarbeitet)
	 */
	private final Set<Integer> elementIDs;

	/**
	 * Aktuell in Bearbeitung befindliche Station
	 * @see #beginStation(ModelElement)
	 * @see #done()
	 */
	private ModelElementBox currentStation;

	/**
	 * Für die aktuelle Station erfasste Eigenschaften.
	 */
	private Map<Integer,List<String[]>> properties;

	/**
	 * Vorschlagswert für die als nächstes zu bearbeitende Station
	 * @see #addEdgeOut(ModelElementEdge)
	 * @see #addConditionalEdgeOut(String, ModelElement)
	 * @see #getNextElementSuggestion()
	 */
	private ModelElementBox nextElementSuggestion;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public ModelDescriptionBuilder(final EditModel model) {
		this(model,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 * @param elements	Liste der Elemente, die berücksichtigt werden sollen (kann <code>null</code> sein, dann werden alle Elemente verarbeitet)
	 */
	public ModelDescriptionBuilder(final EditModel model, final List<ModelElement> elements) {
		this.model=model;
		if (elements==null) {
			elementIDs=null;
		} else {
			elementIDs=elements.stream().map(element->Integer.valueOf(element.getId())).collect(Collectors.toSet());
		}
	}

	/**
	 * Liefert das Editor-Modell zu dem die Beschreibung generiert werden soll.
	 * @return	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public EditModel getModel() {
		return model;
	}

	/**
	 * Liefert eine Auflistung der Kundentypen in dem Modell
	 * (und cacht diese dabei für weitere Aufrufe).
	 * @return	Auflistung der Kundentypen in dem Modell
	 */
	public String[] getClientTypes() {
		if (clientTypes==null) {
			clientTypes=model.surface.getClientTypes().toArray(String[]::new);
		}
		return clientTypes;
	}

	/**
	 * Liefert den Namen einer Station
	 * @param element	Station
	 * @return	Name der Station
	 */
	private static String getBoxStationName(final ModelElement element) {
		final StringBuilder sb=new StringBuilder();
		sb.append(element.getContextMenuElementName());
		final String name=element.getName().trim();
		if (!name.isEmpty()) {
			sb.append(" \"");
			sb.append(name);
			sb.append("\"");
		}
		sb.append(String.format(" (id=%d)",element.getId()));
		return sb.toString();
	}

	/**
	 * Liefert einen Vorschlagswert für die Folgestation einer Station
	 * @param element	Station deren auslaufende Kanten betrachtet werden sollen
	 * @return	Vorschlagswert für Folgestation
	 */
	private static ModelElementBox getNextBoxStation(ModelElement element) {
		while (!(element instanceof ModelElementBox)) {
			if (element==null) return null;

			if (element instanceof ModelElementEdge) {
				element=((ModelElementEdge)element).getConnectionEnd();
				continue;
			}

			if (element instanceof ModelElementVertex) {
				element=((ModelElementVertex)element).getEdgeOut();
				continue;
			}
		}
		return (ModelElementBox)element;
	}

	/**
	 * Liefert die Teleport-Ziel-Station zu einem Teleport-Ziel-Stationsnamen
	 * @param destinationName	Teleport-Ziel-Stationsnamen
	 * @return	Teleport-Ziel-Station oder <code>null</code>, wenn zu dem Namen keine Transport-Ziel-Station ermittelt werden konnte
	 */
	private ModelElementTeleportDestination getTeleportDestination(final String destinationName) {
		for (ModelElement e1: model.surface.getElements()) {
			if ((e1 instanceof ModelElementTeleportDestination) && e1.getName().equals(destinationName)) return (ModelElementTeleportDestination)e1;
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if ((e2 instanceof ModelElementTeleportDestination) && e2.getName().equals(destinationName)) return (ModelElementTeleportDestination)e2;
			}
		}
		return null;
	}

	/**
	 * Liefert den Namen der Station oder der nächsten Folgestation, wenn es sich um eine Kante handelt
	 * @param element	Stationen, deren vollständiger Name ermittelt werden soll
	 * @return	Name oder <code>null</code>, wenn kein Name für eine Folgestation ermittelt werden konnte
	 */
	public static final String getStationName(final ModelElement element) {
		final ModelElementBox station=getNextBoxStation(element);
		if (station==null) return null;
		return getBoxStationName(station);
	}

	/**
	 * Liefert den Bezeichner für eine Zeitbasis
	 * @param timeBase	Zeitbasis für die der Name ermittelt werden soll
	 * @return	Bezeichner für die Zeitbasis
	 */
	public static final String getTimeBase(final ModelSurface.TimeBase timeBase) {
		final StringBuilder sb=new StringBuilder();
		sb.append(Language.tr("ModelDescription.TimeBase"));
		sb.append(": ");
		sb.append(ModelSurface.getTimeBaseStrings()[timeBase.id]);
		return sb.toString();
	}

	/**
	 * Liert eine Beschreibung für eine Verteilung
	 * @param distribution	Verteilung, für die die Beschreibung geliefert werden soll
	 * @return	Beschreibung der Verteilung
	 */
	public static final String getDistributionInfo(final AbstractRealDistribution distribution) {
		final StringBuilder sb=new StringBuilder();
		sb.append(DistributionTools.getDistributionName(distribution));
		sb.append(" (");
		sb.append(DistributionTools.getDistributionShortInfo(distribution));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Erstellt den Beschreibungstext für eine Station aus den Daten der Station.
	 * @param station	Station, für die ein Beschreibungstext erstellt werden soll
	 * @param properties	Eigenschaften der Station
	 */
	protected abstract void processStation(final ModelElementBox station, final Map<Integer,List<String[]>> properties);

	/**
	 * Erstellt den Beschreibungstext für die Ressourcen.
	 * @param resources	Liste der Ressourcen
	 */
	protected abstract void processResources(final List<String> resources);

	/**
	 * Erstellt den Beschreibungstext für die Variablen.
	 * @param variables	Liste der Variablen
	 */
	protected abstract void processVariables(final List<String> variables);

	/**
	 * Startet die Speicherung der Daten zu einer neuen Station
	 * @param element	Station, für die die Daten erfasst werden sollen
	 */
	public final void beginStation(final ModelElement element) {
		if (currentStation!=null) {
			if (properties.size()>0) processStation(currentStation,properties);
		}
		properties=null;
		currentStation=getNextBoxStation(element);
		nextElementSuggestion=null;
		if (currentStation!=null) properties=new HashMap<>();
	}

	/**
	 * Fügt eine Eigenschaft zu der Beschreibung der aktuellen Station hinzu
	 * @param name	Name der Eigenschaft
	 * @param value	Inhalt der Eigenschaft
	 * @param position	Position in der Reihenfolge der Eigenschaften
	 */
	public final void addProperty(final String name, final String value, final int position) {
		if (properties==null) return;

		List<String[]> list=properties.get(position);
		if (list==null) properties.put(position,list=new ArrayList<>());
		list.add(new String[] {name,value});
	}

	/**
	 * Fügt die Zeitbasis-Eigenschaft zu der Beschreibung der aktuellen Station hinzu
	 * @param timeBase	Zeitbasis
	 * @param position	Position in der Reihenfolge der Eigenschaften
	 */
	public final void addTimeBaseProperty(final ModelSurface.TimeBase timeBase, final int position) {
		addProperty(Language.tr("ModelDescription.TimeBase"),ModelSurface.getTimeBaseStrings()[timeBase.id],position);
	}

	/**
	 * Wurden Informationen zu Bedienern und Variablen schon ausgegeben?
	 * @see #done()
	 */
	private boolean additionalInformationAdded=false;

	/**
	 * Beendet die Erfassung der Eigenschaften der aktuellen Station
	 */
	public final void done() {
		if (currentStation!=null) {
			if (properties.size()>0) processStation(currentStation,properties);
			currentStation=null;
			properties=null;
			nextElementSuggestion=null;
		}

		if (additionalInformationAdded) return;
		additionalInformationAdded=true;

		/* Bedienergruppen */
		if (model.resources.size()>0) {
			final List<String> lines=new ArrayList<>();
			for (ModelResource resource: model.resources.getResources()) {
				final String name=resource.getName();
				switch (resource.getMode()) {
				case MODE_NUMBER:
					final int count=resource.getCount();
					if (count<0) lines.add(name+": "+Language.tr("ModelDescription.Resources.infinite")); else lines.add(name+": "+NumberTools.formatLong(count));
					break;
				case MODE_SCHEDULE:
					lines.add(name+": "+String.format(Language.tr("ModelDescription.Resources.Schedule"),resource.getSchedule()));
					break;
				}
			}
			processResources(lines);
		}

		/* Initiale Werte für Variablen */
		final Map<String,String> variables=model.getInitialVariablesWithValues();
		if (variables.size()>0) {
			final List<String> lines=new ArrayList<>();
			variables.keySet().stream().sorted().forEach(name->lines.add(name+":="+variables.get(name)));
			processVariables(lines);
		}
	}

	/**
	 * Erfasst die Eigenschaft "Folgestation" zu einer Station
	 * @param edge	Auslaufende Ecke, die zur Folgestation führt
	 */
	public final void addEdgeOut(final ModelElementEdge edge) {
		final ModelElementBox station=getNextBoxStation(edge);
		if (station==null) return;
		final String next=getBoxStationName(station);
		if (next==null) return;
		if (nextElementSuggestion==null) nextElementSuggestion=station;
		addProperty(Language.tr("ModelDescription.NextElement"),next,Integer.MAX_VALUE);
	}

	/**
	 * Erfasst die Eigenschaft "Folgestation" zu einer Station
	 * @param condition	Beschreibung, warum diese Folgestation gewählt wird
	 * @param element	Auslaufende Ecke, die zur Folgestation führt, bzw. Folgestation selbst
	 */
	public final void addConditionalEdgeOut(final String condition, final ModelElement element) {
		final ModelElementBox station=getNextBoxStation(element);
		if (station==null) return;
		final String next=getBoxStationName(station);
		if (next==null) return;
		if (nextElementSuggestion==null) nextElementSuggestion=station;
		if (condition==null || condition.isBlank()) {
			addProperty(Language.tr("ModelDescription.NextElement"),next,Integer.MAX_VALUE);
		} else {
			addProperty(condition,next,Integer.MAX_VALUE);
		}
	}

	/**
	 * Erfasst die Eigenschaft "Folgestation" zu einer Teleport-Station
	 * @param condition	Beschreibung, warum diese Folgestation gewählt wird
	 * @param destinationName	Name des Teleportziels
	 */
	public final void addConditionalTeleportDestination(final String condition, final String destinationName) {
		final ModelElementTeleportDestination destination=getTeleportDestination(destinationName);
		if (destination==null) return;
		if (nextElementSuggestion==null) nextElementSuggestion=destination;
		if (condition==null || condition.isBlank()) {
			addProperty(Language.tr("ModelDescription.NextElement"),destinationName,Integer.MAX_VALUE);
		} else {
			addProperty(condition,destinationName,Integer.MAX_VALUE);
		}
	}

	/**
	 * Liefert basierend auf den Eigenschaften, die die aktuelle Station in die
	 * Beschreibung eingetragen hat, eine Empfehlung für eine Folgestation.
	 * @return	Empfehlung für eine Folgestation oder <code>null</code>, wenn es keine Empfehlung gibt.
	 */
	private final ModelElementBox getNextElementSuggestion() {
		return nextElementSuggestion;
	}

	/**
	 * Prüft, ob eine bestimmte Station bei der Erstellung der Beschreibung übersprungen werden soll
	 * @param element	Zu prüfende Station
	 * @return	Liefert <code>true</code>, wenn die Station übersprungen werden soll
	 * @see #elementIDs
	 */
	private boolean skipElement(final ModelElement element) {
		if (elementIDs==null) return false; /* Alle Elemente verarbeiten */
		return !elementIDs.contains(element.getId());
	}

	/**
	 * Liefert eine nach IDs sortierte Liste aller Box-Stationen in dem
	 * Modell (inkl. Untermodellen).
	 * @return	Nach IDs sortierte Liste aller Box-Stationen
	 */
	private Set<ModelElementBox> getBoxStations() {
		final Map<Integer,ModelElementBox> map=new HashMap<>();

		/* Liste aller Elemente */
		for (ModelElement element: model.surface.getElements()) {
			if (skipElement(element)) continue;
			if (element instanceof ModelElementBox) map.put(element.getId(),(ModelElementBox)element);
			if (element instanceof ModelElementSub) {
				for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) {
					if (element2 instanceof ModelElementBox) map.put(element2.getId(),(ModelElementBox)element2);
				}
			}
		}

		/* Liste der IDs */
		final int[] ids=map.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray();

		/* Sortierte Liste der Elemente */
		Set<ModelElementBox> list=new HashSet<>();
		for (int id: ids) list.add(map.get(id));

		return list;
	}

	/**
	 * Liefert die erste Kundenquelle-Station aus der Liste der Stationen
	 * (ohne die Liste zu verändern)
	 * @param stations	Liste der Stationen
	 * @return	Kundenquell-Station oder <code>null</code>, wenn es keine solche gibt.
	 */
	private ModelElementBox getNextSource(final Set<ModelElementBox> stations) {
		for (ModelElementBox element: stations) {
			if (element instanceof ModelElementSource) return element;
			if (element instanceof ModelElementSourceMulti) return element;
			if (element instanceof ModelElementSourceTable) return element;
			if (element instanceof ModelElementSourceDB) return element;
			if (element instanceof ModelElementSourceDDE) return element;
		}
		return null;
	}

	/**
	 * Führt die Verarbeitung durch.
	 */
	public void run() {
		/* Stationen */
		final Set<ModelElementBox> stations=getBoxStations();

		boolean testSources=true;
		while (!stations.isEmpty()) {
			ModelElementBox station=getNextElementSuggestion();
			if (station!=null && !stations.contains(station)) station=null;
			if (station==null) {
				if (testSources) station=getNextSource(stations);
				if (station==null) testSources=false; /* Alle Quellen aufgebraucht, brauchen wir nicht noch mal prüfen */
			}
			if (station==null) station=stations.iterator().next();
			stations.remove(station);
			station.buildDescription(this);
		}

		/* Bedinergruppen + Initiale Werte für Variablen => done() */
	}

	/**
	 * Liefert eine Überschrift für die Modellbeschreibung
	 * @return	Überschrift für die Modellbeschreibungs
	 */
	protected String getModelDescriptionTitle() {
		final StringBuilder sb=new StringBuilder();
		sb.append(Language.tr("ModelDescription.Dialog.Save.OutputTitle"));
		if (!model.name.isBlank()) {
			sb.append(" \"");
			sb.append(model.name);
			sb.append("\"");
		}
		return sb.toString();
	}
}