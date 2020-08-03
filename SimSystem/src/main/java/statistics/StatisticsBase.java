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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLData;

/**
 * Basisklasse für die Statistik über die komplette Simulation
 * Die <code>StatisticsBase</code>-Klasse ist dabei so ausgelegt, dass sie sowohl thread-lokal Datensammeln kann als auch am Ende die globale Statistik ausweisen kann
 * @author Alexander Herzog
 */
public abstract class StatisticsBase extends XMLData {
	/**
	 * Liste der Kenngrößen, die auf <code>StatisticsPerformanceIndicator</code>-Objekten basieren
	 */
	protected List<StatisticsPerformanceIndicator> performanceIndicators;

	/**
	 * Konstruktor der Klasse <code>StatisticsBase</code>
	 */
	public StatisticsBase() {
		performanceIndicators=new ArrayList<>();
	}

	/**
	 * Fügt ein Objekt des Typs <code>StatisticsPerformanceIndicator</code> zur Liste der Kenngrößen hinzu.
	 * Die hier hinzugefügten Kenngrößen werden bereits durch das <code>StatisticsBase</code>-Objekt geladen und gespeichert.
	 * @param performanceIndicator	Neue Kenngröße, die automatisch verwaltet werden soll
	 */
	protected final void addPerformanceIndicator(final StatisticsPerformanceIndicator performanceIndicator) {
		performanceIndicators.add(performanceIndicator);
	}

	@Override
	public void resetData() {
		for (StatisticsPerformanceIndicator performanceIndicator : performanceIndicators) performanceIndicator.reset();
	}

	/**
	 * Fügt die Daten eines weiteren Simulations-Threads zu den Statistik-Ergebnissen hinzu.
	 * @param moreStatistics	Anderes <code>Statistics</code>-Objekt, dessen Daten diesem Objekt hinzugefügt werden sollen
	 */
	public void addData(final StatisticsBase moreStatistics) {
		for (int i=0;i<Math.min(performanceIndicators.size(),moreStatistics.performanceIndicators.size());i++)
			performanceIndicators.get(i).add(moreStatistics.performanceIndicators.get(i));
	}

	/**
	 * Berechnet ganz am Ende aus den aufgezeichneten Daten die interessanten Kenngrößen.
	 * Aufgezeichnet werden können z.B. Anzahl von Ereignis X, Summe der Werte von Ereignis X und quadrierter Summe der Werte von Ereignis X.
	 * Die gesuchten Kenngrößen können Mittelwert und Standardabweichung sein.
	 * Die aufzuzeichnenden Größen lassen sich leicht addieren, so dass die Daten von mehreren Threads einfach zusammengeführt werden können.
	 */
	public void calc() {
		for (StatisticsPerformanceIndicator performanceIndicator : performanceIndicators) performanceIndicator.calc();
	}

	@Override
	protected String loadProperty(final String name, final String text, final Element node) {
		for (StatisticsPerformanceIndicator performanceIndicator : performanceIndicators) {
			if (performanceIndicator.xmlNodeNames==null) continue;
			for (String test: performanceIndicator.xmlNodeNames) if (name.equalsIgnoreCase(test)) return performanceIndicator.loadFromXML(node);
		}

		return null;
	}

	@Override
	protected void processLoadedData() {
		calc();
	}

	@Override
	protected void addDataToXML(final Document doc, final Element node, final boolean isPartOfOtherFile, final File file) {
		final StringBuilder sb=new StringBuilder();
		for (StatisticsPerformanceIndicator performanceIndicator : performanceIndicators) performanceIndicator.addToXML(doc,node,sb);
	}

	/**
	 * Prüft, ob der angegebene Indikator in einem {@link StatisticsMultiPerformanceIndicator} enthalten ist und
	 * liefert im Erfolgsfall diesen zurück.
	 * @param indicator	Indikator für den ein Elternobjekt gesucht werden soll.
	 * @return	Liefert im Erfolgsfall das Elternobjekt, sonst <code>null</code>
	 */
	public StatisticsMultiPerformanceIndicator getParent(final StatisticsPerformanceIndicator indicator) {
		final Optional<StatisticsMultiPerformanceIndicator> result=performanceIndicators.stream().filter(i->i instanceof StatisticsMultiPerformanceIndicator).map(i->(StatisticsMultiPerformanceIndicator)i).filter(i->i.contains(indicator)).findFirst();
		return result.orElse(null);
	}
}