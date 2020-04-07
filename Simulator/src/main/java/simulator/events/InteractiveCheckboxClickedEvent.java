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
import simulator.elements.RunElementInteractiveCheckbox;
import simulator.runmodel.SimulationData;

/**
 * Benachrichtigung einer Checkbox, dass sie angeklickt wurde
 * @author Alexander Herzog
 * @see RunElementInteractiveCheckbox
 */
public class InteractiveCheckboxClickedEvent extends Event {
	/**
	 * Zu benachrichtigende Checkbox
	 */
	public RunElementInteractiveCheckbox interactiveCheckbox;

	@Override
	public void run(SimData data) {
		interactiveCheckbox.triggered((SimulationData)data);
	}
}
