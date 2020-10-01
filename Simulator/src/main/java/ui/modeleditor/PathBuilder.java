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
import java.util.stream.Collectors;

import language.Language;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;
import ui.modeleditor.elements.ModelElementWayPoint;
import ui.modeleditor.elements.WayPointRecord;

/**
 * Erstellt mittels Wegpunkten Pfade für die Transporter
 * @author Alexander Herzog
 * @see PathEditorDialog
 */
public class PathBuilder {
	private static final boolean LOG_NO_PATH_FOUND=false;

	private final ModelSurface mainSurface;
	private final List<Connection> connections;
	private final List<String> runLog;

	/**
	 * Konstruktor der Klasse
	 * @param mainSurface	Zeichenfläche auf der Hauptebene
	 */
	public PathBuilder(final ModelSurface mainSurface) {
		this.mainSurface=mainSurface;
		connections=new ArrayList<>();
		runLog=new ArrayList<>();
	}

	/**
	 * Löscht alle bisherigen Von-Nach-Verknüpfungen
	 * @see PathBuilder#add(ModelElement, List)
	 */
	public void clear() {
		connections.clear();
	}

	/**
	 * Fügt eine Von-Nach-Verknüpfung zu der Liste, aus der die Pfade aufgebaut werden sollen, hinzu
	 * @param previous	Ausgangsstation
	 * @param next	Liste der Stationen, die von dieser Ausgangsstation aus direkt erreichbar sein sollen
	 * @see PathBuilder#clear()
	 */
	public void add(final ModelElement previous, List<ModelElement> next) {
		if (previous==null || next==null) return;
		next=next.stream().filter(n->(n!=null)).collect(Collectors.toList());
		if (next.size()==0) return;
		connections.add(new Connection(previous,next));
	}

	private void clearWayPoints(final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementWayPoint) ((ModelElementWayPoint)element).getRecords().clear();
			if (element instanceof ModelElementSub) clearWayPoints(((ModelElementSub)element).getSubSurface());
		}
	}

	private void getStationsList(final List<ModelElement> list, final List<String> names, final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			/* Untermodelle */
			if ((element instanceof ModelElementSub)) {
				getStationsList(list,names,((ModelElementSub)element).getSubSurface());
				continue;
			}

			/* Test: Transporter-relevantes Element? */
			if (element.getName().trim().isEmpty()) continue;
			boolean transporterElement=false;
			if (element instanceof ModelElementTransportTransporterSource) transporterElement=true;
			if (element instanceof ModelElementTransportParking) transporterElement=true;
			if (element instanceof ModelElementTransportDestination) transporterElement=true;
			if (element instanceof ModelElementWayPoint) transporterElement=true;
			if (!transporterElement) continue;

			/* Zur Liste hinzufügen */
			if (!names.contains(element.getName())) {
				names.add(element.getName());
				list.add(element);
			}
		}
	}

	private List<ModelElement> getStationsList() {
		final List<String> names=new ArrayList<>();
		final List<ModelElement> list=new ArrayList<>();
		getStationsList(list,names,mainSurface);
		return list;
	}

	private List<ModelElement> getPath(final ModelElement origin, final ModelElement destination, final List<ModelElement> alreadyUsed, final List<ModelElement> stations) {
		alreadyUsed.add(origin);
		try {

			for (Connection connection: connections) if (connection.previous==origin) for (ModelElement next: connection.next) if (!alreadyUsed.contains(next)) {
				/* Weg gefunden? */
				if (next==destination) {
					final List<ModelElement> path=new ArrayList<>();
					path.add(origin);
					path.add(destination);
					return path;
				}

				/* Folgestation testen */
				final List<ModelElement> partialPath=getPath(next,destination,alreadyUsed,stations);
				if (partialPath!=null) {
					final List<ModelElement> path=new ArrayList<>();
					path.add(origin);
					path.addAll(partialPath);
					return path;
				}
			}
			return null;
		} finally {
			alreadyUsed.remove(alreadyUsed.size()-1);
		}
	}

	private List<ModelElement> getPath(final ModelElement origin, final ModelElement destination, final List<ModelElement> stations) {
		final List<ModelElement> path=new ArrayList<>();

		final List<ModelElement> partialPath=getPath(origin,destination,new ArrayList<>(),stations);
		if (partialPath==null) {
			/* Kein Pfad gefunden, direkte Verbindung von Start zu Ziel */
			path.add(origin);
			path.add(destination);
			if (LOG_NO_PATH_FOUND) {
				runLog.add(String.format(Language.tr("PathEditor.Builder.Log.Search"),getFromToString(origin,destination)));
				runLog.add("  "+Language.tr("PathEditor.Builder.Log.NoPathFound"));
			}
		} else {
			/* Teilpfad (der auch die Zielstation enthält) hinzufügen */
			path.addAll(partialPath);
			ModelElement last=null;
			runLog.add(String.format(Language.tr("PathEditor.Builder.Log.Search"),getFromToString(origin,destination)));
			runLog.add("  "+Language.tr("PathEditor.Builder.Log.Path"));
			for (ModelElement next: partialPath) {
				if (last!=null) runLog.add("    "+getFromToString(last,next));
				last=next;
			}
		}

		return path;
	}

	private void run(final boolean loggingOnly) {
		/* Logging-Daten von früheren Läufen derselben Instanz löschen */
		runLog.clear();

		/* Alte Daten aus allen Wegpunkten löschen */
		if (!loggingOnly) clearWayPoints(mainSurface);

		/* Alle Transporter-Stationen ermitteln */
		final List<ModelElement> stations=getStationsList();

		for (ModelElement origin: stations) for (ModelElement destination: stations) {
			if (origin==destination) continue;
			if (origin instanceof ModelElementWayPoint) continue;
			if (destination instanceof ModelElementWayPoint) continue;
			List<ModelElement> path=getPath(origin,destination,stations);
			int index=1;
			for (ModelElement step: path) if (step instanceof ModelElementWayPoint) {
				final WayPointRecord record=new WayPointRecord(origin.getName(),destination.getName(),index);
				if (index==1) runLog.add("  "+Language.tr("PathEditor.Builder.Log.PathIndex.Title"));
				runLog.add("    "+String.format(Language.tr("PathEditor.Builder.Log.PathIndex.Info"),step.getName(),getFromToString(origin,destination),index));
				if (!loggingOnly) ((ModelElementWayPoint)step).getRecords().add(record);
				index++;
			}
		}
	}

	/**
	 * Erstellt die Pfade in dem Modell basierend auf den definierten Pfad-Elementen
	 * @see PathBuilder#add(ModelElement, List)
	 * @see PathBuilder#runInfo
	 */
	public void run() {
		run(false);

	}

	/**
	 * Erstellt Pfade basierend auf den definieren Pfad-Elementen, trägt diese jedoch
	 * nicht in die Wegpunkte ein, sondern gibt nur die Logging-Daten zurück.
	 * @return Logging-Informationen
	 * @see PathBuilder#run()
	 * @see PathBuilder#getRunLog()
	 */
	public String[] runInfo() {
		run(true);
		return getRunLog();

	}

	private String getFromToString(final ModelElement origin, final ModelElement destination) {
		return origin.getName()+" (id="+origin.getId()+") -> "+destination.getName()+" (id="+destination.getId()+")";
	}

	/**
	 * Liefert eine Liste aller definierten Teil-Pfade als Zeichenketten-Array.<br>
	 * Ausgabe möglich per: <code>Arrays.asList(getSegmentList()).stream().forEach(System.out::println);</code>
	 * @return	Liste aller definierten Teil-Pfade
	 * @see PathBuilder#add(ModelElement, List)
	 */
	public String[] getSegmentList() {
		final List<String> list=new ArrayList<>();
		for (Connection connection: connections) for (ModelElement next: connection.next) {
			list.add(getFromToString(connection.previous,next));
		}
		return list.toArray(new String[0]);
	}

	/**
	 * Gibt die Logging-Informationen des letzten Laufs aus
	 * @return	Logging-Informationen des letzten Laufs
	 */
	public String[] getRunLog() {
		return runLog.toArray(new String[0]);
	}

	private class Connection {
		public final ModelElement previous;
		public final List<ModelElement> next;

		public Connection(final ModelElement previous, final List<ModelElement> next) {
			this.previous=previous;
			this.next=next;
		}
	}
}
