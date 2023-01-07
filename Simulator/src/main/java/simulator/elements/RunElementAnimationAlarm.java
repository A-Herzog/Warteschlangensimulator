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
import simulator.simparser.ExpressionMultiEval;
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
	/**
	 * Soll der Sound nur einmal abgespielt werden?
	 */
	private boolean onlyOneActivation;

	/**
	 * Soll der Sound bei jedem Kundentyp (&lt;0) oder nur bei einem bestimmten Kundentyp (&ge;0) abgespielt werden?
	 */
	private int clientType;

	/**
	 * Bedingung, die für eine Sound-Ausgabe erfüllt sein muss (kann <code>null</code> sein)
	 */
	private String condition;

	/**
	 * Soll der Sound bei jeder Ankunft (&le;0) oder nur bei jeder n-ten Ankunft (&gt;0) abgespielt werden?
	 */
	private long counter;

	/**
	 * Abzuspielender Sound
	 */
	private String sound;

	/**
	 * Maximaldauer des abzuspielenden Sounds
	 */
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

		/* Auslaufende Kanten */
		final String edgeError=alarm.buildEdgeOut(alarmElement);
		if (edgeError!=null) return edgeError;

		/* Einstellungen zum abzuspielenden Sound */
		alarm.sound=alarmElement.getSound();
		alarm.soundMaxSeconds=alarmElement.getSoundMaxSeconds();

		/* Auslösebedingungen */
		alarm.onlyOneActivation=alarmElement.isOnlyOneActivation();
		if (alarmElement.getClientType().isEmpty()) {
			alarm.clientType=-1;
		} else {
			final Integer I=runModel.clientTypesMap.get(alarmElement.getClientType());
			if (I==null) return String.format(Language.tr("Simulation.Creator.SetInternalError"),id);
			alarm.clientType=I;
		}
		alarm.counter=alarmElement.getCounter();

		/* Bedingung */
		final String condition=alarmElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			alarm.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.AlarmCondition"),condition,element.getId(),error+1);
			alarm.condition=condition;
		}

		return alarm;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementAnimationAlarm)) return null;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementAnimationAlarmData getData(final SimulationData simData) {
		RunElementAnimationAlarmData data;
		data=(RunElementAnimationAlarmData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAnimationAlarmData(this,condition,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Prüft, ob unter den aktuellen Randbedingungen ein Ton abgespielt werden soll.
	 * @param simData	Simulationsdaten
	 * @param client	Kunde
	 * @return	Liefert <code>true</code>, wenn der eingestellte Ton abgespielt werden soll
	 */
	private boolean testPlaySound(SimulationData simData, RunDataClient client) {
		RunElementAnimationAlarmData data=null;

		/* Nur bei Animation Sound abspielen */
		if (!simData.runModel.isAnimation) return false;

		/* Passender Kundentyp? */
		if (clientType>=0) {
			if (client.type!=clientType) return false;
		}

		/* Bedingung */
		if (condition!=null) {
			if (data==null) data=getData(simData);
			simData.runData.setClientVariableValues(client);
			if (!data.condition.eval(simData.runData.variableValues,simData,client)) return false;
		}

		/* Zähler */
		if (counter>0) {
			if (data==null) data=getData(simData);
			data.counter++;
			if (data.counter<counter) return false;
			data.counter=0;
		}

		/* Nur einmal ausführen? */
		if (onlyOneActivation) {
			if (data==null) data=getData(simData);
			if (!data.active) return false;
			data.active=false;
		}

		return true;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Sound abspielen */
		if (testPlaySound(simData,client)) SoundSystem.getInstance().playAll(sound,soundMaxSeconds);

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
