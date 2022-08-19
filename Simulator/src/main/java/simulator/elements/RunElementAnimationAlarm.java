/**
 * Copyright 2022 Alexander Herzog
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
package simulator.elements;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnimationAlarm;
import ui.modeleditor.elements.ModelElementSub;
import ui.tools.SoundSystem;

/**
 * Äquivalent zu {@link ModelElementAnimationAlarm}
 * @author Alexander Herzog
 * @see ModelElementAnimationAlarm
 */
public class RunElementAnimationAlarm extends RunElementPassThrough {
	/** Abzuspielender Sound */
	private String sound;
	/** Maximaldauer des abzuspielenden Sounds */
	private int soundMaxSeconds;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAnimationAlarm(final ModelElementAnimationAlarm element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AnimationAlarm.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementAnimationAlarm)) return null;
		final ModelElementAnimationAlarm alarmElement=(ModelElementAnimationAlarm)element;
		final RunElementAnimationAlarm alarm=new RunElementAnimationAlarm(alarmElement);

		alarm.sound=alarmElement.getSound();
		alarm.soundMaxSeconds=alarmElement.getSoundMaxSeconds();

		return null;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementAnimationAlarm)) return null;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Bei Animation Sound abspielen */
		if (simData.runModel.isAnimation) {
			SoundSystem.getInstance().playAll(sound,soundMaxSeconds);
		}

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
