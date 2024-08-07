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
import simulator.coreelements.RunElement;
import simulator.runmodel.SimulationData;

/**
 * Gibt die belegten Bediener nach einer Verz�gerung frei
 * @author Alexander Herzog
 */
public class ReleaseReleaseResources extends Event {
	/** Station an der die Bediener freigegeben werden sollen */
	public RunElement station;
	/** Freizugebene Bediener */
	public int[] resources;

	/**
	 * Konstruktor der Klasse
	 */
	public ReleaseReleaseResources() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Ressourcen freigaben */
		simData.runData.resources.releaseResources(resources,simData);

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.Release"),String.format(Language.tr("Simulation.Log.Release.InfoDelay2"),station.name));

		/* Pr�fen, ob andere Stationen auf diese Ressourcen warten */
		simData.runData.fireReleasedResourcesNotify(simData);

		/* System �ber Status-�nderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);
	}
}
