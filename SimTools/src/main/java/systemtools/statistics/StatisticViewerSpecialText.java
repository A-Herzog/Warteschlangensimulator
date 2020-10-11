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
import java.util.List;

/**
 * Gibt bestimmte vordefinierte Texte in Form eines HTML-Panels aus.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
class StatisticViewerSpecialText extends StatisticViewerHTMLText {
	/**
	 * Anzeigemodus
	 * @author Alexander Herzog
	 */
	public enum SpecialMode {
		/** Anzeige: Bitte Kategorie auswählen. */
		VIEWER_CATEGORY,

		/** Anzeige: Bitte Unterkategorie auswählen. */
		VIEWER_SUBCATEGORY,

		/** Anzeige: Bitte Simulation starten. */
		VIEWER_NODATA
	}

	/**
	 * Erzeugt den auszugebenden Text
	 * @param type Gibt an, was angezeigt werden soll (siehe {@link SpecialMode})
	 * @param startSimulation <code>Runnable</code>-Objekt, das beim Klick auf "Simulation jetzt starten" ausgeführt werden soll.
	 * @param loadStatistics <code>Runnable</code>-Objekt, das beim Klick auf "Statistikdaten laden" ausgeführt werden soll.
	 * @return	Auszugebender Text
	 */
	private static final String buildInfoText(SpecialMode type, Runnable startSimulation, Runnable loadStatistics) {
		switch (type) {
		case VIEWER_CATEGORY: return StatisticsBasePanel.viewersSpecialTextCategory;
		case VIEWER_SUBCATEGORY: return StatisticsBasePanel.viewersSpecialTextSubCategory;
		case VIEWER_NODATA:
			String info=StatisticsBasePanel.viewersSpecialTextNoData;
			int nr=0;
			if (startSimulation!=null) {
				nr++;
				info+="<p><a href=\"special:"+nr+"\">"+StatisticsBasePanel.viewersSpecialTextLoadData+"</a></p>";
			}
			if (loadStatistics!=null) {
				nr++;
				info+="<p><a href=\"special:"+nr+"\">"+StatisticsBasePanel.viewersSpecialTextStartSimulation+"</a></p>";
			}
			return info;
		}
		return "";
	}

	/**
	 * Liefert eine Liste mit Listenern, die auf Klicks auf spezielle Links in dem angezeigten Text reagieren sollen
	 * @param startSimulation <code>Runnable</code>-Objekt, das beim Klick auf "Simulation jetzt starten" ausgeführt werden soll.
	 * @param loadStatistics <code>Runnable</code>-Objekt, das beim Klick auf "Statistikdaten laden" ausgeführt werden soll.
	 * @return	Liste mit den Listenern, die auf Klicks auf spezielle Links in dem angezeigten Text reagieren sollen
	 */
	private static final Runnable[] buildSpecialLinkListener(Runnable startSimulation, Runnable loadStatistics) {
		final List<Runnable> runner=new ArrayList<>();
		if (loadStatistics!=null) runner.add(loadStatistics);
		if (startSimulation!=null) runner.add(startSimulation);
		return runner.toArray(new Runnable[0]);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerSpecialHTMLText</code>
	 * @param type Gibt an, was angezeigt werden soll (siehe {@link SpecialMode})
	 * @param startSimulation <code>Runnable</code>-Objekt, das beim Klick auf "Simulation jetzt starten" ausgeführt werden soll.
	 * @param loadStatistics <code>Runnable</code>-Objekt, das beim Klick auf "Statistikdaten laden" ausgeführt werden soll.
	 */
	public StatisticViewerSpecialText(SpecialMode type, Runnable startSimulation, Runnable loadStatistics) {
		super(buildInfoText(type,startSimulation,loadStatistics),buildSpecialLinkListener(startSimulation,loadStatistics));
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerSpecialHTMLText</code>
	 * @param type Gibt an, was angezeigt werden soll (siehe {@link SpecialMode})
	 */
	public StatisticViewerSpecialText(SpecialMode type) {
		this(type,null,null);
	}
}
