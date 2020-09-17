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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mathtools.NumberTools;

/**
 * Statistik-Klasse, die erfasst, wie lange sich das System in einem bestimmten, durch einen
 * (String-)Namen benannten Zustand befunden hat.<br>
 * Die Zählung wird über die Funktion {@link StatisticsStateTimePerformanceIndicator#set(double, String)} realisiert.<br>
 * Sollen hingegen durch einen Integer-Zahlenwert definierte Zustände erfasst werden,
 * so kann dafür die Klasse {@link StatisticsTimePerformanceIndicator} verwendet werden.
 * @author Alexander Herzog
 * @version 1.1
 */
public final class StatisticsStateTimePerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Kindelement für "Zustand" */
	public static String[] xmlChild=new String[]{"Zustand"};
	/** XML-Attribut für "Name" */
	public static String[] xmlChildName=new String[]{"Name"};

	/**
	 * Zeitpunkt der letzten Änderung des Zustands des Systems.
	 */
	private double lastTime=0.0;

	/**
	 * Aktueller Zustand des Systems.
	 */
	private String lastState="";

	/**
	 * Gibt an, ob der Zähler gerade auf eine explizite Startzeit gesetzt wurde.
	 * @see #setTime(double)
	 */
	private boolean explicitTimeInit=false;


	/**
	 * Erfassung der Zeiten, in der sich das System in einem Zustand befunden hat.
	 */
	private final Map<String,Double> map;

	/**
	 * Konstruktor der Klasse <code>StatisticsStateTimePerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsStateTimePerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		map=new HashMap<>();
		reset();
	}

	/**
	 * Erfasst eine Zustandsänderung
	 * @param time	Aktuelle Zeit
	 * @param newState	Neuer Zustand
	 */
	public void set(final double time, String newState) {
		if (newState==null) newState="";

		if (time!=lastTime) {
			final boolean init=(lastTime<=0 && map.size()==0) || explicitTimeInit;
			explicitTimeInit=false;
			if (!init) {
				final Double D=map.get(lastState);
				double newTime=time-lastTime;
				if (D!=null) newTime+=D.doubleValue();
				map.put(lastState,newTime);
			}
			lastTime=time;
		}

		lastState=newState;
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsStateTimePerformanceIndicator)) return;
		StatisticsStateTimePerformanceIndicator moreStateStatistics=(StatisticsStateTimePerformanceIndicator)moreStatistics;

		for (Map.Entry<String,Double> entry: moreStateStatistics.map.entrySet()) {
			final Double D=map.get(entry.getKey());
			final Double E=entry.getValue();
			if (E!=null) {
				double d=E.doubleValue();
				if (D!=null) d+=D.doubleValue();
				map.put(entry.getKey(),d);
			}
		}
	}

	/**
	 * Stellt die aktuelle Systemzeit ein (z.B. wenn der Beginn der Simulation nicht zum Zeitpunkt 0 erfolgt)
	 * @param time	Neue aktuelle Systemzeit
	 */
	public void setTime(final double time) {
		lastTime=time;
		explicitTimeInit=true;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		lastState="";
		lastTime=0;
		map.clear();
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsStateTimePerformanceIndicator)) return;

		map.clear();
		map.putAll(((StatisticsStateTimePerformanceIndicator)indicator).map);

		lastState=((StatisticsStateTimePerformanceIndicator)indicator).lastState;
		lastTime=((StatisticsStateTimePerformanceIndicator)indicator).lastTime;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsStateTimePerformanceIndicator clone() {
		final StatisticsStateTimePerformanceIndicator indicator=new StatisticsStateTimePerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsStateTimePerformanceIndicator cloneEmpty() {
		return new StatisticsStateTimePerformanceIndicator(xmlNodeNames);
	}

	/**
	 * Liefert den zuletzt per <code>set()</code> eingestellten Zustand
	 * @return	Aktuell eingestellter Zustand
	 * @see #set(double, String)
	 */
	public String getCurrentState() {
		return lastState;
	}

	/**
	 * Liefert die Aufenthaltsdauern in allen Zuständen als Zuordnung
	 * @return	Zuordnung von Namen und Aufenthaltsdauern in den Zuständen
	 */
	public Map<String,Double> get() {
		return new HashMap<>(map);
	}

	/**
	 * Liefert die Summe über die Aufenthaltszeiten in allen Zuständen
	 * @return	Summe über die Aufenthaltszeiten in allen Zuständen
	 */
	public double getSum() {
		double sum=0;
		for (Map.Entry<String,Double> entry: map.entrySet()) sum+=entry.getValue();
		return sum;
	}

	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		for (Map.Entry<String,Double> entry: map.entrySet()) {
			Element sub;
			node.appendChild(sub=node.getOwnerDocument().createElement(xmlChild[0]));
			sub.setAttribute(xmlChildName[0],entry.getKey());
			sub.setTextContent(NumberTools.formatSystemNumber(entry.getValue(),recycleStringBuilder));
		}
	}

	@Override
	public String loadFromXML(Element node) {
		reset();

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			for (String test: xmlChild) if (e.getNodeName().equalsIgnoreCase(test)) {
				String name="";
				for (String attr: xmlChildName) {
					name=e.getAttribute(attr);
					if (name!=null && !name.isEmpty()) break;
				}
				final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
				if (D!=null) map.put(name,D);
				break;
			}
		}

		return null;
	}
}
