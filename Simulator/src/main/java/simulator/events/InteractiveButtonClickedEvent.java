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
import simulator.elements.RunElementInteractiveButton;
import simulator.runmodel.SimulationData;

/**
 * Benachrichtigung eines Buttons, das es angeklickt wurde
 * @author Alexander Herzog
 * @see RunElementInteractiveButton
 */
public class InteractiveButtonClickedEvent extends Event {
	/**
	 * Zu benachrichtigende Schaltfläche
	 */
	public RunElementInteractiveButton interactiveButton;

	@Override
	public void run(SimData data) {
		interactiveButton.triggered((SimulationData)data);
	}
}
