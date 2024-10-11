/**
 * Copyright 2024 Alexander Herzog
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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Diese Klasse erm�glicht es, einen Teil oder alle Stationen
 * auf einer Zeichenfl�che neu anzuordnen.
 */
public class ModelSurfaceArranger {
	/**
	 * Anordnungsmodus
	 */
	public enum Mode {
		/** Nur Stationen am Gitter ausrichten */
		GRID_ALIGN,
		/** Stationen vollst�ndig neu ausrichten */
		FULL_ALIGN
	}

	/**
	 * Zeichenfl�che deren Elemente (teilweise) neu angeordnet werden sollen
	 */
	private final ModelSurface surface;

	/**
	 * Liste der IDs der im Editor momentan ausgew�hlten Stationen
	 */
	private final Set<Integer> selectedIDs;

	/**
	 * Zur Anordnung erfasste Stationen
	 * @see #prepare(Set)
	 */
	private List<Box> boxes;

	/**
	 * Position f�r das erste Element
	 */
	private final Point positionStart;

	/**
	 * Standardm��ig zu verwendende Startposition f�r die Anordnung der Elemente,
	 * wenn die Startposition nicht basierend auf den vorhandenen Elementen
	 * ermittelt werden kann.
	 * @see #autoDetectStartPosition()
	 */
	public static final Point DEFAULT_START_POSITION=new Point(100,100);

	/**
	 * Konstruktor
	 * @param surface	Zeichenfl�che deren Elemente (teilweise) neu angeordnet werden sollen
	 * @param selectedIDs	Liste der IDs der im Editor momentan ausgew�hlten Stationen
	 * @param positionStart	Linke obere Ecke des ersten Elements
	 */
	public ModelSurfaceArranger(final ModelSurface surface, final Set<Integer> selectedIDs, final Point positionStart) {
		this.surface=surface;
		this.selectedIDs=selectedIDs;
		this.positionStart=(positionStart==null)?autoDetectStartPosition():positionStart;
	}

	/**
	 * Konstruktor
	 * @param surface	Zeichenfl�che deren Elemente (teilweise) neu angeordnet werden sollen
	 * @param selectedIDs	Liste der IDs der im Editor momentan ausgew�hlten Stationen
	 */
	public ModelSurfaceArranger(final ModelSurface surface, final Set<Integer> selectedIDs) {
		this(surface,selectedIDs,null);
	}

	/**
	 * Ermittelt die Startposition des Elementes das sich am weitesten oben und das sich am weitesten links befindet.
	 * @return	x-Position des am weitesten links befindlichen und y-Position des am weitesten oben liegenden Elements
	 */
	private Point autoDetectStartPosition() {
		final var positions=surface.getElements().stream().filter(element->(element instanceof ModelElementBox || element instanceof ModelElementVertex)).filter(element->element instanceof ModelElementPosition).map(element->((ModelElementPosition)element).getPosition(true)).collect(Collectors.toList());

		if (positions.size()==0) return DEFAULT_START_POSITION;

		int minX=positions.stream().mapToInt(p->p.x).min().orElse(0);
		int minY=positions.stream().mapToInt(p->p.y).min().orElse(0);

		if (minX==0 || minY==0) return DEFAULT_START_POSITION;

		minX=(int)Math.round(minX/50.0)*50;
		minY=(int)Math.round(minY/50.0)*50;
		if (minX==0) minX=50;
		if (minY==0) minY=50;

		return new Point(minX,minY);
	}

	/**
	 * Liefert eine Liste aller Stationen auf der Zeichenfl�che.
	 * @return	Liste aller Stationen auf der Zeichenfl�che
	 */
	public Set<ModelElementBox> getAllStations() {
		return surface.getElements().stream().filter(element->element instanceof ModelElementBox).map(element->(ModelElementBox)element).collect(Collectors.toSet());
	}

	/**
	 * Liefert eine Liste aller aktuell ausgew�hlten Stationen auf der Zeichenfl�che.
	 * @return	Liste aller ausgew�hlten Stationen auf der Zeichenfl�che
	 */
	public Set<ModelElementBox> getSelectedStations() {
		return getAllStations().stream().filter(element->selectedIDs.contains(element.getId())).collect(Collectors.toSet());
	}

	/**
	 * Gibt an, ob die Zeichenfl�che neben Stationen und Kanten auch dekorative Elemente (die nicht verschoben werden k�nnen) enth�lt.
	 * @return	Liefert <code>true</code>, wenn die Zeichenfl�chen dekorative Elemente enth�lt
	 */
	public boolean hasDecorations() {
		return surface.getElements().stream().filter(element->!(element instanceof ModelElementBox) && !(element instanceof ModelElementEdge) && !(element instanceof ModelElementVertex)).findFirst().isPresent();
	}

	/**
	 * Liefert die Menge der unmittelbar auf eine Kante folgenden Verbindungsecken (bevor eine neue Station erreicht wird).
	 * @param edge Ausgangskante
	 * @return	Menge der unmittelbar auf eine Kante folgenden Verbindungsecken
	 */
	private Set<ModelElementVertex> getFollowingVertices(ModelElementEdge edge) {
		final Set<ModelElementVertex> vertices=new HashSet<>();
		while (edge!=null) {
			final ModelElement destination=edge.getConnectionEnd();
			if (destination==null) break;
			if (destination instanceof ModelElementVertex) {
				final ModelElementVertex vertex=(ModelElementVertex)destination;
				vertices.add(vertex);
				edge=vertex.getEdgeOut();
			} else {
				break;
			}
		}
		return vertices;
	}

	/**
	 * Liefert die Menge der unmittelbar auf eine Station folgenden Verbindungsecken (bevor eine neue Station erreicht wird).
	 * @param station	Ausgangsstation
	 * @return	Menge der unmittelbar auf eine Kante folgenden Verbindungsecken
	 */
	private Set<ModelElementVertex> getFollowingVertices(final ModelElement station) {
		if (station instanceof ModelElementEdgeOut) {
			return getFollowingVertices(((ModelElementEdgeOut)station).getEdgeOut());
		}

		if (station instanceof ModelElementEdgeMultiOut) {
			final ModelElementEdgeMultiOut element=(ModelElementEdgeMultiOut)station;
			final Set<ModelElementVertex> result=new HashSet<>();
			for (var edge: element.getEdgesOut()) result.addAll(getFollowingVertices(edge));
			return result;
		}

		return Set.of();
	}

	/**
	 * Baut die Liste der internen Boxen auf und erg�nzt ggf. Verbindungsecken.
	 * @param stations	Ausgangsmenge der Zeichenfl�chenstationen
	 */
	private void buildBoxesList(final Set<ModelElementBox> stations) {
		boxes=new ArrayList<>();
		for (var station: stations) {
			boxes.add(new Box(station));
			for (var vertex: getFollowingVertices(station)) {
				if (findBox(vertex)==null) boxes.add(new Box(vertex));
			}
		}
	}

	/**
	 * Wandelt eine Menge an Zeichenfl�chenstationen in die interne Repr�sentation
	 * um und pr�ft dabei, ob die Menge einen schlossenen Graphen bildet.
	 * @param stations	Ausgangsmenge der Zeichenfl�chenstationen
	 * @return	Liefert <code>true</code>, wenn die Zeichenfl�chenstationen erfolgreich verarbeitet werden konnten
	 */
	private boolean prepare(final Set<ModelElementBox> stations) {
		buildBoxesList(stations);

		/* Folge- und Vorg�ngerboxen zu jeder Box */
		for (var box: boxes) {
			box.connectionsTo.addAll(nextBoxes(box));
			for (var box2: box.connectionsTo) {
				box2.connectionsFrom.add(box);
			}
		}

		/* Erreichbarkeit pr�fen */
		for (var box1: boxes) for (var box2: boxes) {
			if (!box1.testConnection(box2)) return false;
		}

		return true;
	}

	/**
	 * Liefert die Menge der Stationen, die nur Ausgangskanten aber keine Eingangskanten besitzen.
	 * @return	Menge der Stationen, die nur Ausgangskanten aber keine Eingangskanten besitzen
	 */
	private Set<Box> getStartElements() {
		final Set<Box> startElements=new HashSet<>();
		for (var box1: boxes) {
			boolean isStartElement=true;
			for (var box2: boxes) if (box2!=box1 && box2.hasConnectionTo(box1)) {isStartElement=false; break;}
			if (isStartElement) startElements.add(box1);
		}
		return startElements;
	}

	/**
	 * Berechnet die Position f�r eine Station und alle Folgestationen
	 * @param x	Einzustellende x-Koordinate der Station
	 * @param y	Einzustellende y-Koordinate der Station
	 * @param element	Station
	 * @return	y-Koordinate der tiefsten eingestellten Station
	 */
	private int calculatePositions(int x, int y, final Box element) {
		if (element.x==0 && element.y==0) {
			element.x=x;
			element.y=y;
		}

		x++;
		boolean first=true;
		for (var box: verticalSort(nextBoxes(element))) {
			if (first) first=false; else y++;
			y=calculatePositions(x,y,box);
		}

		return y;
	}

	/**
	 * Sortiert eine Reihe von Boxen gem�� der bisherigen vertikalen Ausrichtung der zugeh�rigen Stationen.
	 * @param boxes	Menge der Boxen
	 * @return	Sortierte Liste der Boxen
	 */
	private List<Box> verticalSort(final Set<Box> boxes) {
		return boxes.stream().sorted((b1,b2)->b1.element.getPosition(true).y-b2.element.getPosition(true).y).collect(Collectors.toList());
	}

	/**
	 * Ordnet eine Menge von Stationen neu auf der Zeichenfl�che an.
	 * @param stations	Menge der anzuordnenden Stationen
	 * @return	Liefert <code>true</code>, wenn die Stationen angeordnet werden konnten
	 */
	private boolean fullArrange(final Set<ModelElementBox> stations) {
		if (!prepare(stations)) return false;
		final Set<Box> startElements=getStartElements();
		if (startElements.size()==0) return false;

		int y=1;
		for (var start: verticalSort(startElements)) {
			y=calculatePositions(1,y,start);
			y++;
		}

		for (var box: boxes) box.updateElementPosition(positionStart);

		return true;
	}

	/**
	 * Ordnet eine Menge von Stationen neu am Raster auf der Zeichenfl�che an.
	 * @param stations	Menge der anzuordnenden Stationen
	 * @return	Liefert <code>true</code>, wenn die Stationen angeordnet werden konnten
	 */
	private boolean gridAlign(final Set<ModelElementBox> stations) {
		buildBoxesList(stations);

		for (var element: boxes.stream().map(box->box.element).filter(element->element instanceof ModelElementPosition).map(element->(ModelElementPosition)element).collect(Collectors.toList())) {
			final var p=element.getPosition(true);
			final int x;
			final int y;
			if (element instanceof ModelElementVertex) {
				x=(int)Math.round((p.x-45)/50.0)*50+45;
				y=(int)Math.round((p.y-20)/50.0)*50+20;
			} else {
				x=(int)Math.round(p.x/50.0)*50;
				y=(int)Math.round(p.y/50.0)*50;
			}
			element.setPosition(new Point(x,y));
		}

		return true;
	}

	/**
	 * Ordnet eine Menge von Stationen neu auf der Zeichenfl�che an.
	 * @param stations	Menge der anzuordnenden Stationen
	 * @param mode	Ausrichtungsmodus
	 * @return	Liefert <code>true</code>, wenn die Stationen angeordnet werden konnten
	 */

	public boolean arrange(final Set<ModelElementBox> stations, final Mode mode) {
		switch (mode) {
		case GRID_ALIGN: return gridAlign(stations);
		case FULL_ALIGN: return fullArrange(stations);
		}
		return false;
	}

	/**
	 * Sucht die zu einer Zeichenfl�chenstation zugeh�rige interne Repr�sentation.
	 * @param element	Zeichenfl�chenstation
	 * @return	Repr�sentation innerhalb der Anordnesystems oder <code>null</code>, wenn es keine zugeh�rige Box gibt
	 */
	private Box findBox(final ModelElement element) {
		return boxes.stream().filter(box->box.element==element).findFirst().orElse(null);
	}

	/**
	 * Liefert die auf eine Box folgenden n�chsten Boxen
	 * @param box	Ausgangsbox
	 * @return	N�chste Boxen
	 */
	private Set<Box> nextBoxes(final Box box) {
		final Set<Box> result=new HashSet<>();

		if (box.element instanceof ModelElementEdgeOut) {
			final var edge=((ModelElementEdgeOut)box.element).getEdgeOut();
			if (edge!=null && edge.getConnectionEnd()!=null) {
				final Box nextBox=findBox(edge.getConnectionEnd());
				if (nextBox!=null) result.add(nextBox);
			}
		}

		if (box.element instanceof ModelElementEdgeMultiOut) for (var edge: ((ModelElementEdgeMultiOut)box.element).getEdgesOut()) {
			if (edge!=null && edge.getConnectionEnd()!=null) {
				final Box nextBox=findBox(edge.getConnectionEnd());
				if (nextBox!=null) result.add(nextBox);
			}
		}

		return result;
	}

	/**
	 * Die Objekte dieser Klasse repr�sentieren die Anordnung der Stationen
	 */
	private static class Box {
		/**
		 * Zugeh�rige Zeichenfl�chenStation
		 */
		private final ModelElement element;

		/**
		 * Neue x-Position (in einer Schritten, nicht in Zeichenfl�chen-Koordinaten)
		 */
		private int x;

		/**
		 * Neue y-Position (in einer Schritten, nicht in Zeichenfl�chen-Koordinaten)
		 */
		private int y;

		/**
		 * Liste der Folgeboxen
		 * @see ModelSurfaceArranger#prepare(Set)
		 */
		private final Set<Box> connectionsTo;

		/**
		 * Liste der Vorg�ngerboxen
		 * @see ModelSurfaceArranger#prepare(Set)
		 */
		private final Set<Box> connectionsFrom;

		/**
		 * Konstruktor
		 * @param element	Zugeh�rige Zeichenfl�chenStation
		 */
		private Box(final ModelElement element) {
			this.element=element;
			connectionsTo=new HashSet<>();
			connectionsFrom=new HashSet<>();
		}

		/**
		 * Pr�ft, ob es auf der Zeichenfl�che eine Verbindung zwischen einer Kante und einem Element gibt.
		 * @param start	Ausgangskante
		 * @param destination	Zielstation
		 * @return	Liefert <code>true</code>, wenn es eine Verbindung gibt
		 */
		private static boolean hasConnectionTo(final ModelElementEdge start, final ModelElement destination) {
			if (start==null) return false;
			return hasConnectionTo(start.getConnectionEnd(),destination);
		}

		/**
		 * Pr�ft, ob es auf der Zeichenfl�che eine Verbindung zwischen zwei Elementen gibt.
		 * @param start	Ausgangsstation
		 * @param destination	Zielstation
		 * @return	Liefert <code>true</code>, wenn es eine Verbindung gibt
		 */
		private static boolean hasConnectionTo(final ModelElement start, final ModelElement destination) {
			if (start==null) return false;
			if (start==destination) return true;

			if (start instanceof ModelElementEdgeOut) {
				return hasConnectionTo(((ModelElementEdgeOut)start).getEdgeOut(),destination);
			}

			if (start instanceof ModelElementEdgeMultiOut) {
				for (var edge: ((ModelElementEdgeMultiOut)start).getEdgesOut()) if (hasConnectionTo(edge,destination)) return true;
				return false;
			}

			return false;
		}

		/**
		 * Pr�ft, ob das aktuelle Element eine gerichtete Verbindung zu einem zweiten besitzt.
		 * @param element	Zielelement
		 * @return	Liefert <code>true</code>, wenn es eine gerichtete Verbindung gibt
		 */
		private boolean hasConnectionTo(final ModelElement element) {
			if (element==this.element) return true;
			return hasConnectionTo(this.element,element);
		}

		/**
		 * Pr�ft, ob das aktuelle Element eine gerichtete Verbindung zu einem zweiten besitzt.
		 * @param box	Zielelement
		 * @return	Liefert <code>true</code>, wenn es eine gerichtete Verbindung gibt
		 */
		private boolean hasConnectionTo(final Box box) {
			if (box==null) return false;
			return hasConnectionTo(box.element);
		}

		/**
		 * Pr�ft, ob es eine Verbindung (egal in welche Richtung und auch teilweise vor- und r�ckw�rts) zu einem zweiten Element gibt
		 * @param box	Zweites Element
		 * @param ignore	Menge der Elemente, die bei der Suche nicht mehr ber�cksichtigt werden sollen
		 * @return	Liefert <code>true</code>, wenn es eine Verbindung gibt
		 */
		private boolean testConnection(final Box box, final Set<Box> ignore) {
			final Set<Box> ignore2=new HashSet<>(ignore);
			ignore2.add(this);

			for (var b: connectionsFrom) {
				if (ignore.contains(b)) continue;
				if (b==box) return true;
				if (b.testConnection(box,ignore2)) return true;
			}

			for (var b: connectionsTo) {
				if (ignore.contains(b)) continue;
				if (b==box) return true;
				if (b.testConnection(box,ignore2)) return true;
			}

			return false;
		}

		/**
		 * Pr�ft, ob es eine Verbindung (egal in welche Richtung und auch teilweise vor- und r�ckw�rts) zu einem zweiten Element gibt
		 * @param box	Zweites Element
		 * @return	Liefert <code>true</code>, wenn es eine Verbindung gibt
		 */
		private boolean testConnection(final Box box) {
			if (box==null) return false;
			if (box==this) return true;
			return testConnection(box,Set.of());
		}

		/**
		 * Rechnet die neuen, internen Einer-Schritt-Koordinaten in Zeichenfl�chenkoordinaten
		 * um und stellt diese in dem korrespondierenden Zeichenfl�chenelement ein.
		 * @param positionStart	Position f�r das erste Element
		 * @see #x
		 * @see #y
		 * @see ModelSurfaceArranger#positionStart
		 */
		private void updateElementPosition(final Point positionStart) {
			if (element instanceof ModelElementBox) {
				element.setPosition(new Point(positionStart.x+(x-1)*250,positionStart.y+(y-1)*100));
			}

			if (element instanceof ModelElementVertex) {
				element.setPosition(new Point(positionStart.x+(x-1)*250+45,positionStart.y+(y-1)*100+20));
			}
		}
	}
}
