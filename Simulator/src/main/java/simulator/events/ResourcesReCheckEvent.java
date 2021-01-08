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

import simcore.Event;
import simcore.SimData;
import simulator.runmodel.RunDataResource;
import simulator.runmodel.SimulationData;

/**
 * Ereignis zum Prüfen, ob Bediener verfügbar sind, da die Downtime eines Bedieners jetzt endet
 * oder es eine Verfügbarkeitsänderung bedingt durch den Zeitplan gibt.
 * @author Alexander Herzog
 * @see RunDataResource
 */
public class ResourcesReCheckEvent extends Event {
	/**
	 * Wird hier ein Wert größer als 0 angegeben, so werden automatisch
	 * weitere Events in dem angegebenen MS-Abstand generiert.
	 */
	public long autoScheduleNext=0;

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Prüfen, ob andere Stationen auf diese Ressourcen warten */
		simData.runData.fireReleasedResourcesNotify(simData);

		if (autoScheduleNext>0) {
			if (simData.runData.stopp) return;
			final ResourcesReCheckEvent event=(ResourcesReCheckEvent)(simData.getEvent(ResourcesReCheckEvent.class));
			event.init(simData.currentTime+autoScheduleNext);
			event.autoScheduleNext=autoScheduleNext;
			simData.eventManager.addEvent(event);
		}
	}
}
