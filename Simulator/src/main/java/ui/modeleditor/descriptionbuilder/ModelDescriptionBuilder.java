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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
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
	 * Liste der Elemente, die berücksichtigt werden sollen (kann <code>null</code> sein, dann werden alle Elemente verarbeitet)
	 */
	private final List<Integer> elementIDs;

	private ModelElementBox currentStation;
	private Map<Integer,List<String[]>> properties;
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
			elementIDs=elements.stream().map(element->Integer.valueOf(element.getId())).collect(Collectors.toList());
		}
	}

	/**
	 * Liefert das Editor-Modell zu dem die Beschreibung generiert werden soll.
	 * @return	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public EditModel getModel() {
		return model;
	}

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
		sb.append(DistributionTools.getDistributionLongInfo(distribution));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Erstellt den Beschreibungstext für eine Station aus den Daten der Station
	 * @param station	Station, für die ein Beschreibungstext erstellt werden soll
	 * @param properties	Eigenschaften der Station
	 */
	protected abstract void processStation(final ModelElementBox station, final Map<Integer,List<String[]>> properties);

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
	 * Beendet die Erfassung der Eigenschaften der aktuellen Station
	 */
	protected final void done() {
		if (currentStation!=null) {
			if (properties.size()>0) processStation(currentStation,properties);
			currentStation=null;
			properties=null;
			nextElementSuggestion=null;
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
		if (condition==null || condition.trim().isEmpty()) {
			addProperty(Language.tr("ModelDescription.NextElement"),next,Integer.MAX_VALUE);
		} else {
			addProperty(condition,next,Integer.MAX_VALUE);
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

	private boolean skipElement(final ModelElement element) {
		if (elementIDs==null) return false; /* Alle Elemente verarbeiten */
		return !elementIDs.contains(element.getId());
	}

	/**
	 * Liefert eine nach IDs sortierte Liste aller Box-Stationen in dem
	 * Modell (inkl. Untermodellen).
	 * @return	Nach IDs sortierte Liste aller Box-Stationen
	 */
	private List<ModelElementBox> getBoxStations() {
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
		List<ModelElementBox> list=new ArrayList<>();
		for (int id: ids) list.add(map.get(id));

		return list;
	}

	/**
	 * Liefert die erste Kundenquell-Station aus der Liste der Stationen
	 * (ohne die Liste zu verändern)
	 * @param stations	Liste der Stationen
	 * @return	Kundenquell-Station oder <code>null</code>, wenn es keine solche gibt.
	 */
	private ModelElementBox getNextSource(final List<ModelElementBox> stations) {
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
		final List<ModelElementBox> stations=getBoxStations();

		while (stations.size()>0) {
			ModelElementBox station=getNextElementSuggestion();
			if (station!=null && !stations.contains(station)) station=null;
			if (station==null) station=getNextSource(stations);
			if (station==null) station=stations.get(0);
			stations.remove(station);
			station.buildDescription(this);
		}
	}

	/**
	 * Liefert eine Überschrift für die Modellbeschreibung
	 * @return	Überschrift für die Modellbeschreibungs
	 */
	protected String getModelDescriptionTitle() {
		final StringBuilder sb=new StringBuilder();
		sb.append(Language.tr("ModelDescription.Dialog.Save.OutputTitle"));
		if (!model.name.trim().isEmpty()) {
			sb.append(" \"");
			sb.append(model.name);
			sb.append("\"");
		}
		return sb.toString();
	}
}