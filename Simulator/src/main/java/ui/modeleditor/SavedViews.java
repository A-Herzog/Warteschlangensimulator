/**
 * Copyright 2021 Alexander Herzog
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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse hält die gespeicherten Modellansichten vor.
 * @author Alexander Herzog
 * @see EditModel
 */
public class SavedViews {
	/**
	 * Name des XML-Elements, das die Datensätze enthält
	 */
	public static String[] XML_NODE_NAME=new String[] {"GespeicherteAnsicht"};

	/**
	 * Liste der gespeicherten Ansichten
	 * @see #getViews()
	 */
	private final List<SavedView> list;

	/**
	 * Konstruktor der Klasse
	 */
	public SavedViews() {
		list=new ArrayList<>();
	}

	/**
	 * Löscht alle gespeicherten Ansichten
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * Vergleicht die Liste der gespeicherten Ansichten mit einer weiteren Liste.
	 * @param savedViews	Weitere Liste mit gespeicherten Ansichten zum Vergleichen
	 * @return	Liefert <code>true</code>, wenn beide Listen inhaltlich identisch sind
	 */
	public boolean equalsSavedViews(final SavedViews savedViews) {
		if (list.size()!=savedViews.list.size()) return false;
		for (int i=0;i<list.size();i++) if (!list.get(i).equalsSavedView(savedViews.list.get(i))) return false;
		return true;
	}

	/**
	 * Überträgt die gespeicherten Ansichten aus einem anderen Objekt in dieses.
	 * @param copySource	Quellobjekt aus dem die gespeicherten Ansichten in dieses übertragen werden sollen
	 */
	public void copyFrom(final SavedViews copySource) {
		clear();
		copySource.list.forEach(record->list.add(new SavedView(record)));
	}

	/**
	 * Versucht die Informationen zu den zu ladenden Daten aus einem xml-Element zu laden
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final String name=Language.trAllAttribute("SavedView.XML.Name",node);
		if (name.isBlank()) return null;
		if (nameInUse(name)) return String.format("SavedView.XML.ErrorName",name);

		final Integer x=NumberTools.getNotNegativeInteger(Language.trAllAttribute("SavedView.XML.X",node));
		if (x==null) return String.format("SavedView.XML.ErrorX",name);

		final Integer y=NumberTools.getNotNegativeInteger(Language.trAllAttribute("SavedView.XML.Y",node));
		if (y==null) return String.format("SavedView.XML.ErrorY",name);

		final Double zoom=NumberTools.getPositiveDouble(Language.trAllAttribute("SavedView.XML.Zoom",node));
		if (zoom==null) return String.format("SavedView.XML.ErrorZoom",name);

		list.add(new SavedView(name,new Point(x,y),zoom));

		return null;
	}

	/**
	 * Speichert die Informationen zu den zu ladenden Daten in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		for (SavedView view: list) {
			final Element node=doc.createElement(XML_NODE_NAME[0]);
			parent.appendChild(node);
			node.setAttribute(Language.tr("SavedView.XML.Name"),view.getName());
			node.setAttribute(Language.tr("SavedView.XML.X"),""+view.getX());
			node.setAttribute(Language.tr("SavedView.XML.Y"),""+view.getY());
			node.setAttribute(Language.tr("SavedView.XML.Zoom"),NumberTools.formatSystemNumber(view.getZoom()));
		}
	}

	/**
	 * Liefert die Liste mit allen gespeicherten Ansichten.<br>
	 * Es handelt sich um die Originalliste. Veränderungen
	 * an der Liste führen zu Änderungen in diesem Objekt.
	 * @return	Liste mit allen gespeicherten Ansichten
	 */
	public List<SavedView> getViews() {
		return list;
	}

	/**
	 * Prüft, ob ein Name für eine gespeicherte Ansicht bereits verwendet wird.
	 * @param name	Zu prüfender Name
	 * @return	Liefert <code>true</code>, wenn es bereits eine gespeicherte Ansicht mit dem angegebenen Namen gibt
	 */
	public boolean nameInUse(final String name) {
		return list.stream().map(view->view.getName()).filter(s->s.equals(name)).findFirst().isPresent();
	}

	/**
	 * Stellt ein, welche Ansicht als aktuell angesehen werden soll.
	 * @param view	Als aktuell einzustellende Ansicht
	 */
	public void setSelected(final SavedView view) {
		list.forEach(v->v.setSelected(false));
		if (view!=null) view.setSelected(true);
	}

	/**
	 * Diese Klasse kapselt eine einzelne gespeicherte Ansicht.
	 * @see SavedViews#list
	 */
	public static class SavedView {
		/**
		 * Name der gespeicherten Ansicht
		 */
		private final String name;

		/**
		 * Linke obere Ecke der gespeicherten Ansicht
		 */
		private final Point location;

		/**
		 * Zoomfaktor der gespeicherten Ansicht
		 */
		private double zoom;

		/**
		 * Ist die Ansicht gerade aktiv?
		 */
		private boolean selected;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der gespeicherten Ansicht
		 */
		public SavedView(final String name) {
			this.name=name;
			location=new Point();
			zoom=1.0;
			selected=false;
		}

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der gespeicherten Ansicht
		 * @param location	Linke obere Ecke der gespeicherten Ansicht
		 * @param zoom	Zoomfaktor der gespeicherten Ansicht
		 */
		public SavedView(final String name, final Point location, final double zoom) {
			this.name=name;
			this.location=new Point(location);
			this.zoom=zoom;
			selected=false;
		}

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der gespeicherten Ansicht
		 * @param surfacePanel	Zeichenfläche der die Daten entnommen werden sollen
		 */
		public SavedView(final String name, final ModelSurfacePanel surfacePanel) {
			this.name=name;
			this.location=new Point(surfacePanel.getTopPosition());
			this.zoom=surfacePanel.getZoom();
			selected=false;
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param copySource	Ausgangselement zum Kopieren
		 */
		public SavedView(final SavedView copySource) {
			this.name=copySource.name;
			this.location=new Point(copySource.location);
			this.zoom=copySource.zoom;
			this.selected=copySource.selected;
		}

		/**
		 * Vergleicht die Daten in diesem Objekt mit den Daten einer weiteren gespeicherten Ansicht
		 * @param savedView	Weitere gespeicherte Ansicht die mit dieser verglichen werden soll
		 * @return	Liefert <code>true</code>, wenn die beiden Ansichten inhaltlich übereinstimmen
		 */
		public boolean equalsSavedView(final SavedView savedView) {
			if (!savedView.name.equals(name)) return false;
			if (!savedView.location.equals(location)) return false;
			if (savedView.zoom!=zoom) return false;
			/* Selected wird für equals nicht verwendet */
			return true;
		}

		/**
		 * Liefert den Namen der gespeicherten Ansicht.
		 * @return	Name der gespeicherten Ansicht
		 */
		public String getName() {
			return name;
		}

		/**
		 * Liefert die X-Scroll-Position der gespeicherten Ansicht.
		 * @return	X-Scroll-Position der gespeicherten Ansicht
		 */
		public int getX() {
			return location.x;
		}

		/**
		 * Liefert die Y-Scroll-Position der gespeicherten Ansicht.
		 * @return	Y-Scroll-Position der gespeicherten Ansicht
		 */
		public int getY() {
			return location.y;
		}

		/**
		 * Liefert den Zoomfaktor der gespeicherten Ansicht.
		 * @return	Zoomfaktor der gespeicherten Ansicht
		 */
		public double getZoom() {
			return zoom;
		}

		/**
		 * Ist die Ansicht gerade aktiv?
		 * @return	Ist die Ansicht gerade aktiv?
		 * @see #setSelected(boolean)
		 */
		public boolean isSelected() {
			return selected;
		}

		/**
		 * Stellt ein, ob die aktuelle Ansicht als aktiv angesehen werden soll.
		 * @param selected	Ist die Ansicht gerade aktiv?
		 * @see #isSelected()
		 */
		public void setSelected(boolean selected) {
			this.selected=selected;
		}

		/**
		 * Trägt die gespeicherten Daten auf der Zeichenfläche ein.
		 * @param surfacePanel	Zeichenfläche auf der die Daten eingetragen werden sollen
		 */
		public void set(final ModelSurfacePanel surfacePanel) {
			surfacePanel.setTopPosition(location);
			surfacePanel.setZoom(zoom);
		}

		/**
		 * Aktualisiert die gespeicherten Daten basierend auf den Daten der Zeichenfläche
		 * @param surfacePanel	Zeichenfläche der die Daten entnommen und gespeichert werden sollen
		 */
		public void update(final ModelSurfacePanel surfacePanel) {
			final Point topPosition=surfacePanel.getTopPosition();
			location.x=topPosition.x;
			location.y=topPosition.y;
			zoom=surfacePanel.getZoom();
		}
	}
}
