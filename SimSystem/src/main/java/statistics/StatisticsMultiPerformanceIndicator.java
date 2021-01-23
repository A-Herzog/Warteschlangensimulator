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
package statistics;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Statistik-Klasse, die mehrere untergeordnete Statistik-Elemente (des jeweils selben Typs) aufnehmen kann.
 * Auf diese Weise können z.B. die Warteschlangenlängen an allen Stationen eines Systems in einer übergeordneten
 * Klasse erfasst werden.<br>
 * Über {@link StatisticsMultiPerformanceIndicator#get(String)} können einzelne untergeordnete Statistik-Objekte
 * abgerufen werden. Existiert für einen angegebenen Namen noch kein Objekt, so wird eines als Kopie des im
 * Konstruktor angegebenen Vorlage-Objektes angelegt.
 * @author Alexander Herzog
 * @version 1.6
 */
public final class StatisticsMultiPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "Typ" */
	public static String[] xmlTypeName=new String[]{"Typ"};
	/** Fehlermeldung für "Interner Fehler" */
	public static String xmlInternalError="Interner Fehler";

	/**
	 * Vorlagen-Objekt von dem konkrete Statistik-Objekte als Kopie abgeleitet werden
	 */
	private final StatisticsPerformanceIndicator template;

	/**
	 * Zuordnung von Namen zu Teilindikatoren (Namen werden ohne Berücksichtigung von Groß- und Kleinschreibung erfasst)
	 * @see #get(String)
	 */
	private Map<String,StatisticsPerformanceIndicator> indicators;

	/**
	 * Zuordnung von Namen zu Teilindikatoren (Namen werden <em>mit</em> Berücksichtigung von Groß- und Kleinschreibung erfasst)<br>
	 * Erst wird in dieser Zuordnung nach einem Namen gesucht. Wenn er hier nicht gefunden wurde (z.B. wegen abweichender Groß-/Kleinschreibung)
	 * wird in {@link #indicators} gesucht.
	 * @see #get(String)
	 */
	private Map<String,StatisticsPerformanceIndicator> cache;

	/**
	 * Zuordnung von Teilindikatoren zu Namen.
	 * @see #getName(StatisticsPerformanceIndicator)
	 */
	private Map<StatisticsPerformanceIndicator,String> cacheName;

	/**
	 * Liste der Namen der untergeordneten Statistik-Elemente.<br>
	 * (Dient als Cache. Wird gelöscht, wenn sich die untergeordneten Statistik-Elemente ändern.)
	 * @see #getNames()
	 */
	private String[] namesList;

	/**
	 * Konstruktor der Klasse <code>StatisticsMultiPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param template	Vorlagen-Objekt von dem konkrete Statistik-Objekte als Kopie abgeleitet werden
	 */
	public StatisticsMultiPerformanceIndicator(final String[] xmlNodeNames, final StatisticsPerformanceIndicator template) {
		super(xmlNodeNames);
		this.template=template;
		reset();
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		indicators=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		cache=new HashMap<>();
		cacheName=new HashMap<>();
		namesList=null;
	}

	/**
	 * Erstellt einen neuen Sub-Indikator
	 * @return	Neues Objekt vom Typ <code>StatisticsPerformanceIndicator</code>
	 */
	protected StatisticsPerformanceIndicator createSubIndicator() {
		return template.cloneEmpty();
	}

	/**
	 * Liefert die Anzahl an Sub-Indikatoren
	 * @return	Anzahl an Sub-Indikatoren
	 */
	public int size() {
		return indicators.size();
	}

	/**
	 * Fügt ein neues Statistik-Teilobjekt zu der Liste der verfügbaren Statistikobjekte hinzu
	 * @param name	Name für das neue Statistik-Teilobjekt
	 * @param indicator	Statistik-Teilobjekt
	 * @see #get(String)
	 */
	private void addIndicator(final String name, final StatisticsPerformanceIndicator indicator) {
		indicators.put(name,indicator);
		indicator.setGroup(this);
		cache.put(name,indicator);
		cacheName.put(indicator,name);
	}

	/**
	 * Liefert das Teil-Statistik-Objekt zu einem angegebenen Namen. Der Name darf "" sein, aber nicht <code>null</code>.<br>
	 * Existiert zu dem Namen bislang noch kein Statistik-Objekt, so wird dieses neu angelegt.
	 * @param name	Name, zu dem das Teil-Statistik-Objekt geliefert werden soll
	 * @return	Teil-Statistik-Objekt
	 */
	public StatisticsPerformanceIndicator get(final String name) {
		StatisticsPerformanceIndicator indicator=getOrNull(name);
		if (indicator==null) {
			indicator=createSubIndicator();
			if (indicator==null) return null;
			addIndicator(name,indicator);
			namesList=null;
		}
		return indicator;
	}

	/**
	 * Liefert das Teil-Statistik-Objekt zu einem angegebenen Namen. Der Name darf "" sein, aber nicht <code>null</code>.<br>
	 * Existiert zu dem Namen bislang noch kein Statistik-Objekt, so wird <code>null</code> zurückgeliefert.
	 * @param name	Name, zu dem das Teil-Statistik-Objekt geliefert werden soll
	 * @return	Teil-Statistik-Objekt oder <code>null</code>, wenn es zu dem Namen kein Statistik-Objekt gibt.
	 */
	public StatisticsPerformanceIndicator getOrNull(final String name) {
		StatisticsPerformanceIndicator indicator=cache.get(name);
		if (indicator==null) {
			indicator=indicators.get(name);
			if (indicator!=null) {
				cache.put(name,indicator);
				cacheName.put(indicator,name);
			}
		}
		return indicator;
	}

	/**
	 * Prüft, ob dieses Statistikobjekt ein bestimmtes Teil-Objekt enthält.
	 * @param indicator	Teil-Objekt bei dem geprüft werden soll, ob es in diesem Statistikobjekt enthalten ist.
	 * @return	Liefert <code>true</code> wenn das Teil-Objekt in diesem Statistikobjekt enthalten ist
	 */
	public boolean contains(final StatisticsPerformanceIndicator indicator) {
		return cacheName.containsKey(indicator);
	}

	/**
	 * Liefert alle Teil-Statistik-Objekte in der Reihenfolge, in der <code>listIndicators</code> die Namen auflistet.
	 * @return	Array bestehend aus allen Teil-Statistik-Objekten
	 */
	public StatisticsPerformanceIndicator[] getAll() {
		final String[] names=getNames();
		final StatisticsPerformanceIndicator[] indicators=new StatisticsPerformanceIndicator[names.length];
		for (int i=0;i<names.length;i++) indicators[i]=get(names[i]);
		return indicators;
	}

	/**
	 * Liefert alle Teil-Statistik-Objekte in der Reihenfolge, in der <code>listIndicators</code> die Namen auflistet.<br>
	 * Die Objekte werden dabei alle auf den als Parameter übergebenen Klassentyp gecastet.
	 * Das Ergebnis-Array ist ebenfalls von dem entsprechenden Typ.
	 * @param <T>	Klassentyp auf den die Einträge gecasted werden sollen
	 * @param T	Klassentyp auf den die Einträge gecasted werden sollen
	 * @return	Array bestehend aus allen Teil-Statistik-Objekten
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] getAll(Class<? extends StatisticsPerformanceIndicator> T) {
		final String[] names=getNames();
		final T[] indicators=(T[])Array.newInstance(T,names.length);
		for (int i=0;i<names.length;i++) indicators[i]=(T)get(names[i]);
		return indicators;
	}

	/**
	 * Liefert den Namen eines Teil-Statistik-Objektes
	 * @param indicator	Teil-Statistik-Objektes für das der Name ermittelt werden soll
	 * @return	Name oder <code>null</code>, wenn sich das angegebene Teil-Statistik-Objektes nicht in dieser Gruppe befindet
	 */
	public String getName(final StatisticsPerformanceIndicator indicator) {
		if (indicator==null) return null;
		return cacheName.get(indicator);
	}

	/**
	 * Listet alle vorhandenen Teil-Statistik-Objekte auf
	 * @return	Array mit den Namen aller in dem <code>StatisticsMultiPerformanceIndicator</code>-Objekt enthaltenen Teil-Statistik-Objekten
	 */
	public String[] getNames() {
		if (namesList==null) namesList=indicators.keySet().toArray(new String[0]);
		return namesList;
	}

	@Override
	public void add(StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsMultiPerformanceIndicator)) return;
		StatisticsMultiPerformanceIndicator moreMultiStatistics=(StatisticsMultiPerformanceIndicator)moreStatistics;

		for (Map.Entry<String,StatisticsPerformanceIndicator> entry : moreMultiStatistics.indicators.entrySet()) {
			final StatisticsPerformanceIndicator ownSub=get(entry.getKey());
			final StatisticsPerformanceIndicator newSub=entry.getValue();
			if (ownSub!=null) ownSub.add(newSub);
		}
	}

	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		for (Map.Entry<String,StatisticsPerformanceIndicator> entry : indicators.entrySet()) {
			Element sub=entry.getValue().addToXML(node.getOwnerDocument(),node,recycleStringBuilder);
			if (!entry.getKey().trim().isEmpty()) sub.setAttribute(xmlTypeName[0],entry.getKey());
		}
	}

	@Override
	public String loadFromXML(Element node) {
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			for (String test: template.xmlNodeNames) if (e.getNodeName().equalsIgnoreCase(test)) {
				StatisticsPerformanceIndicator indicator=createSubIndicator();
				if (indicator==null) return xmlInternalError;
				final String error=indicator.loadFromXML(e);
				if (error!=null) return error;
				String type=getAttributeValue(e,xmlTypeName);
				addIndicator(type,indicator);
				namesList=null;
				break;
			}
		}
		return null;
	}

	/**
	 * Führt ggf. nach dem Laden oder Zusammenführen notwendige Berechnungen durch
	 */
	@Override
	public void calc() {
		for (Map.Entry<String,StatisticsPerformanceIndicator> entry : indicators.entrySet()) entry.getValue().calc();
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsMultiPerformanceIndicator)) return;

		final Map<String,StatisticsPerformanceIndicator> sources=((StatisticsMultiPerformanceIndicator)indicator).indicators;
		for (Map.Entry<String,StatisticsPerformanceIndicator> entry : sources.entrySet()) addIndicator(entry.getKey(),entry.getValue().clone());
		namesList=null;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsMultiPerformanceIndicator clone() {
		final StatisticsMultiPerformanceIndicator indicator=new StatisticsMultiPerformanceIndicator(xmlNodeNames,template.clone());
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsMultiPerformanceIndicator cloneEmpty() {
		return new StatisticsMultiPerformanceIndicator(xmlNodeNames,template.clone());
	}
}
