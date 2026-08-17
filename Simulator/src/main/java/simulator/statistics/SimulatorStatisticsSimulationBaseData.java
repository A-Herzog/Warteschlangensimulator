/**
 * Copyright 2026 Alexander Herzog
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
package simulator.statistics;

import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import org.w3c.dom.Element;

import language.Language;
import statistics.StatisticsSimulationBaseData;
import ui.MainFrame;
import ui.MainPanel;

/**
 * Erweiterte Fassung von {@link StatisticsSimulationBaseData},
 * die zusätzliche, simulatorspezifische Datenfelder besitzt.
 */
public class SimulatorStatisticsSimulationBaseData extends StatisticsSimulationBaseData {
	/**
	 * XML-Schlüssel zum Speichern des Build-Status
	 */
	public static String[] xmlBuild=Language.trAll("Statistics.XML.Element.Simulation.Build");
	/**
	 * XML-Name für "Build-Status: Offizielle Version"
	 */
	public static String[] xmlBuildOfficial=Language.trAll("Statistics.XML.Element.Simulation.Build.Official");
	/**
	 * XML-Name für "Build-Status: Entwicklerversion"
	 */
	public static String[] xmlBuildDeveloper=Language.trAll("Statistics.XML.Element.Simulation.Build.Developer");
	/**
	 * XML-Name für "Build-Status: Nutzererstellte Version, nicht offiziell"
	 */
	public static String[] xmlBuildCustom=Language.trAll("Statistics.XML.Element.Simulation.Build.Custom");
	/**
	 * XML-Name für "Build-Status konnte nicht ermittelt werden"
	 */
	public static String[] xmlBuildUnknow=Language.trAll("Statistics.XML.Element.Simulation.Build.Unknown");

	/**
	 * Interner Datenfeldbezeichner für den Build-Status
	 */
	private static final String buildCustomKey="build";

	/**
	 * Mögliche Werte für den Build-Status
	 */
	public enum BuildState {
		/**
		 * Build-Status: Offizielle Version
		 */
		OFFICIAL(()->xmlBuildOfficial),
		/**
		 * Build-Status: Entwicklerversion
		 */
		DEVELOPER(()->xmlBuildDeveloper),
		/**
		 * Build-Status: Nutzererstellte Version, nicht offiziell
		 */
		CUSTOM(()->xmlBuildCustom),
		/**
		 * Build-Status konnte nicht ermittelt werden
		 */
		UNKNOWN(()->xmlBuildUnknow);

		/**
		 * Callback, welches die möglichen XML-Namen für den jeweiligen Build-Status liefert
		 */
		private final Supplier<String[]> getNames;

		/**
		 * Konstruktor des Enum
		 * @param getNames	Callback, welches die möglichen XML-Namen für den jeweiligen Build-Status liefert
		 */
		BuildState(final Supplier<String[]> getNames) {
			this.getNames=getNames;
		}

		/**
		 * Liefert den primären XML-Namen für den Build-Status.
		 * @return	Primärer XML-Name für den Build-Status.
		 * @see #fromName(String)
		 */
		public String getName() {
			return getNames.get()[0];
		}

		/**
		 * Liefert den Build-Status auf Basis eines XML-Namens.
		 * @param name	XML-Name
		 * @return	Build-Status
		 * @see #getName()
		 */
		public static BuildState fromName(final String name) {
			for (var value: values()) {
				for (var testName: value.getNames.get()) if (testName.equalsIgnoreCase(name)) return value;
			}
			return UNKNOWN;
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param xmlNodeName	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public SimulatorStatisticsSimulationBaseData(final String[] xmlNodeName) {
		super(xmlNodeName);
	}

	@Override
	protected void setupCustomData() {
		addCustomData(buildCustomKey,()->List.of(xmlBuild),()->xmlBuild[0]);
	}

	@Override
	public StatisticsSimulationBaseData cloneEmpty() {
		return new SimulatorStatisticsSimulationBaseData(xmlNodeNames);
	}

	@Override
	public String loadFromXML(final Element node) {
		return super.loadFromXML(node);
	}

	/**
	 * Liefert den Build-Status.
	 * @return	Build-Status
	 * @see BuildState
	 */
	public BuildState getBuildState() {
		return BuildState.fromName(getCustomData(buildCustomKey));
	}

	/**
	 * Stellt den Build-Status ein.
	 * @param buildState	Build-Status
	 * @see BuildState
	 */
	public void setBuildState(final BuildState buildState) {
		setCustomData(buildCustomKey,(buildState==null)?null:buildState.getName());
	}

	/**
	 * Ermittelt den Build-Status der laufenden Simulatorversion.
	 * @return	Build-Status der laufenden Simulatorversion
	 * @see BuildState
	 */
	public static BuildState detectBuildState() {
		final URL imageURL=MainFrame.class.getResource("res/Warteschlangennetz.png");
		if (imageURL==null) return BuildState.CUSTOM;
		return MainPanel.RELEASE_BUILD?BuildState.OFFICIAL:BuildState.DEVELOPER;
	}
}
