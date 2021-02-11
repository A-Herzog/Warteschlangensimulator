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
package systemtools.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Knoten im Statistikbaum
 * @author Alexander Herzog
 * @version 1.4
 */
public class StatisticNode {
	/**
	 * Elternelement des aktuellen Elements
	 * @see #getParent()
	 * @see #setParent(StatisticNode)
	 */
	private StatisticNode parent=null;

	/**
	 * Kindelemente des aktuellen Elements
	 * @see #getChildCount()
	 * @see #getChild(int)
	 * @see #addChild(StatisticNode)
	 */
	private final List<StatisticNode> children;

	/** Name des Objektes */
	public final String name;

	/** Optionale ID des Objektes */
	public final String id;

	/** Eigentliche Statistik-Objekte, welche in der rechten Fensterhälfte angezeigt werden sollen */
	public final StatisticViewer[] viewer;

	/** Kindelement bei der initialen Anzeige des Baums einklappen? */
	public final boolean collapseChildren;

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 * @param collapseChildren	Sollen die Kindelemente dieses Eintrags eingeklappt angezeigt werden?
	 * @param id Optionale ID des Objektes
	 */
	public StatisticNode(final String name, final StatisticViewer[] viewer, final boolean collapseChildren, final String id) {
		this.name=name;
		this.viewer=(viewer!=null)?viewer:(new StatisticViewer[0]);
		this.collapseChildren=collapseChildren;
		this.id=id;
		children=new ArrayList<>();
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 * @param collapseChildren	Sollen die Kindelemente dieses Eintrags eingeklappt angezeigt werden?
	 */
	public StatisticNode(final String name, final StatisticViewer[] viewer, final boolean collapseChildren) {
		this(name,viewer,collapseChildren,"");
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 * @param collapseChildren	Sollen die Kindelemente dieses Eintrags eingeklappt angezeigt werden?
	 */
	public StatisticNode(final String name, final List<StatisticViewer> viewer, final boolean collapseChildren) {
		this(name,viewer.toArray(new StatisticViewer[0]),collapseChildren);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 */
	public StatisticNode(final String name, final StatisticViewer[] viewer) {
		this(name,viewer,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 */
	public StatisticNode(final String name, final List<StatisticViewer> viewer) {
		this(name,viewer.toArray(new StatisticViewer[0]),false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 * @param id Optionale ID des Objektes
	 */
	public StatisticNode(final String name, final StatisticViewer[] viewer, final String id) {
		this(name,viewer,false,id);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekte, die auf dieser Seite angezeigt werden sollen
	 * @param id Optionale ID des Objektes
	 */
	public StatisticNode(final String name, final List<StatisticViewer> viewer, final String id) {
		this(name,viewer.toArray(new StatisticViewer[0]),false,id);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekt, das auf dieser Seite angezeigt werden soll
	 */
	public StatisticNode(final String name, final StatisticViewer viewer) {
		this(name,new StatisticViewer[]{viewer},false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param viewer	Statistik-Objekt, das auf dieser Seite angezeigt werden soll
	 * @param id Optionale ID des Objektes
	 */
	public StatisticNode(final String name, final StatisticViewer viewer, final String id) {
		this(name,new StatisticViewer[]{viewer},false,id);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 */
	public StatisticNode(final String name) {
		this(name,(StatisticViewer[])null,false);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param id Optionale ID des Objektes
	 */
	public StatisticNode(final String name, final String id) {
		this(name,(StatisticViewer[])null,false,id);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param collapseChildren	Sollen die Kindelemente dieses Eintrags eingeklappt angezeigt werden?
	 */
	public StatisticNode(final String name, final boolean collapseChildren) {
		this(name,(StatisticViewer[])null,collapseChildren);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 * @param name	Name der Statistikseite
	 * @param collapseChildren	Sollen die Kindelemente dieses Eintrags eingeklappt angezeigt werden?
	 * @param id Optionale ID des Objektes
	 */
	public StatisticNode(final String name, final boolean collapseChildren, final String id) {
		this(name,(StatisticViewer[])null,collapseChildren,id);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticNode</code>
	 */
	public StatisticNode() {
		this(null,(StatisticViewer[])null,false);
	}

	/**
	 * Fügt ein Kindelement in die Liste ein.
	 * @param child	Anzufügendes Kindelement
	 */
	public void addChild(StatisticNode child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * Liefert das Elternelement des aktuellen Elements zurück.
	 * Das Elternelement eines <code>StatisticNode</code>-Elements wird beim Aufruf von <code>addChild</code> für das
	 * als Parameter übergebene Element automatisch gesetzt.
	 * @return	Elternelement des aktuellen Elements.
	 * @see #addChild(StatisticNode)
	 * @see #setParent(StatisticNode)
	 */
	public StatisticNode getParent() {
		return parent;
	}

	/**
	 * Setzt das Elternelement des aktuellen Elements.
	 * (Wird ein Element per <code>addChild</code> an ein anderes Element angehägt, so wird
	 * das Element automatisch gesetzt und <code>setParent</code> muss nicht manuell aufgerufen werden.)
	 * @param parent	Neues Elternelement des aktuellen Elements.
	 */
	public void setParent(StatisticNode parent) {
		this.parent=parent;
	}

	/**
	 * Liefert die Anzahl der Kindelemente zurück.
	 * @return	Anzahl der Kindelemente
	 * @see #getChild(int)
	 */
	public int getChildCount() {
		return children.size();
	}

	/**
	 * Liefert ein bestimmtes Kindelement des aktuellen Elements zurück.
	 * @param index	Nummer des angefragten Kindelements (0..getChildCound()-1)
	 * @return	Angefragtes Kindelement oder <code>null</code> falls der Index außerhalb des gültigen Bereiches liegt
	 * @see #getChildCount()
	 */
	public StatisticNode getChild(int index) {
		if (index<0 || index>=children.size()) return null;
		return children.get(index);
	}

	/**
	 * Liefert den Index des Kindelements in der Liste der Kindelemente dieses Elements.
	 * @param child	Kindelement für den der Index ermittelt werden soll
	 * @return	Index oder -1, wenn das angegebene Element kein Kindelement des aktuellen Elements ist
	 */
	public int indexOf(final StatisticNode child) {
		return children.indexOf(child);
	}

	/**
	 * Liefert einen Untereintrag basierend auf einem Pfad
	 * @param path	Pfad für den der Untereintrag ermittelt werden soll
	 * @return	Passender Untereintrag oder <code>null</code>, wenn kein Eintrag ermittelt werden konnte
	 */
	public StatisticNode getChildByPath(final List<Integer> path) {
		if (path==null || path.isEmpty()) return null;
		final int index=path.get(0);
		if (index<0 || index>=children.size()) return null;
		final StatisticNode child=children.get(index);
		if (path.size()==1) {
			return child;
		} else {
			final List<Integer> changedPath=new ArrayList<>(path);
			changedPath.remove(0);
			return child.getChildByPath(changedPath);
		}
	}

	/**
	 * Liefert einen Untereintrag basierend auf einem Pfad
	 * @param path	Pfad besteht aus den {@link #toString()}-Namen der Kindelemente für den der Untereintrag ermittelt werden soll
	 * @return	Passender Untereintrag oder <code>null</code>, wenn kein Eintrag ermittelt werden konnte
	 */
	public StatisticNode getChildByPath(final String[] path) {
		if (path==null || path.length==0) return null;

		for (StatisticNode child: children)
			if (child.toString().equals(path[0])) {
				if (path.length==1) return child;
				return child.getChildByPath(Arrays.copyOfRange(path,1,path.length));
			}
		return null;
	}

	/**
	 * Name des Elemnents
	 * @see #toString()
	 */
	private String nameCache=null;

	/**
	 * Liefert den Namen des Elements erweitert um die Information, ob es sich
	 * um einen Test, eine Tabelle oder eine Grafik handelt. (Dies wird gemäß
	 * dem Typ von <code>viewer</code> ermittelt.)
	 */
	@Override
	public String toString() {
		if (viewer.length==0) return name;

		if (nameCache!=null) return nameCache;

		final StringBuilder result=new StringBuilder();
		result.append(name);
		switch (viewer[0].getType()) {
		case TYPE_TEXT : result.append(" ("); result.append(StatisticsBasePanel.typeText); result.append(")"); break;
		case TYPE_TABLE : result.append(" ("); result.append(StatisticsBasePanel.typeTable); result.append(")"); break;
		case TYPE_IMAGE : result.append(" ("); result.append(StatisticsBasePanel.typeImage); result.append(")"); break;
		default: break; /* Report, Sub */
		}
		return nameCache=result.toString();
	}

	/**
	 * Vollständiger Namen des Knoten (inkl. der Namen der übergeordneten Elemente)
	 * @see #getFullName()
	 */
	private String[] fullNameCache=null;

	/**
	 * Liefert den vollständigen Namen des Knoten (inkl. der Namen der übergeordneten Elemente)
	 * @return	Vollständiger Name des Knoten
	 */
	public String[] getFullName() {
		if (fullNameCache!=null) return fullNameCache;

		final List<String> names=new ArrayList<>();
		if (parent!=null) names.addAll(Arrays.asList(parent.getFullName()));
		final String name=toString();
		if (name!=null) names.add(name);
		return fullNameCache=names.toArray(new String[0]);
	}

	/**
	 * Schreibt alle nichtleeren ID-Bezeichner des Elements und aller Kindelemente in die übergebene Menge.
	 * @param ids	Objekt vom Typ <code>Set</code>, das die IDs aufnehmen soll.
	 */
	public void getIDs(Set<String> ids) {
		if (!id.isEmpty()) ids.add(id);
		for (int i=0;i<children.size();i++) children.get(i).getIDs(ids);
	}

	/**
	 * Liefert den Pfad zu diesem Element in der Baumstruktur
	 * @return	Pfad zu diesem Element
	 * @see #getChildByPath(List)
	 */
	public List<Integer> getPath() {
		if (parent==null) return new ArrayList<>();

		int index=parent.indexOf(this);
		if (index<0) return new ArrayList<>();
		final List<Integer> path=parent.getPath();
		path.add(index);
		return path;
	}
}