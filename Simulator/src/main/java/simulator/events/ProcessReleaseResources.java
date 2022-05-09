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
package simulator.events;

import language.Language;
import simcore.Event;
import simcore.SimData;
import simulator.elements.RunElementProcess;
import simulator.runmodel.SimulationData;

/**
 * Gibt die belegten Bediener am Ende der Nachbearbeitungszeit frei
 * @author Alexander Herzog
 */
public class ProcessReleaseResources extends Event {
	/**
	 * Konstruktor der Klasse
	 */
	public ProcessReleaseResources() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Station an der die Nachbearbeitungszeit endet
	 */
	public RunElementProcess station;

	/**
	 * Bedienergruppen-Alternative die für die Bedienung bzw.
	 * Nachbearbeitung aktiv war (damit die richtigen Bediener
	 * freigegeben werden)
	 */
	public int resourceAlternative;

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Ressourcen freigaben */
		simData.runData.resources.releaseResources(station.resources[resourceAlternative],simData);

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.ReleaseResource"),String.format(Language.tr("Simulation.Log.ReleaseResource.Info"),station.name));

		/* Prüfen, ob andere Stationen auf diese Ressourcen warten */
		simData.runData.fireReleasedResourcesNotify(simData);

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}
}
