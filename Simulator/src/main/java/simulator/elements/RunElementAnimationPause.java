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
import ui.modeleditor.elements.ModelElementAnimationPause;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu {@link ModelElementAnimationPause}
 * @author Alexander Herzog
 * @see ModelElementAnimationPause
 */
public class RunElementAnimationPause extends RunElementPassThrough {
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
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementAnimationPause(final ModelElementAnimationPause element) {
		super(element,buildName(element,Language.tr("Simulation.Element.AnimationPause.Name")));
	}

	@Override
	public Object build(EditModel editModel, RunModel runModel, ModelElement element, ModelElementSub parent, boolean testOnly) {
		if (!(element instanceof ModelElementAnimationPause)) return null;
		final ModelElementAnimationPause pauseElement=(ModelElementAnimationPause)element;
		final RunElementAnimationPause pause=new RunElementAnimationPause(pauseElement);

		/* Auslaufende Kanten */
		final String edgeError=pause.buildEdgeOut(pauseElement);
		if (edgeError!=null) return edgeError;

		/* Auslösebedingungen */
		pause.onlyOneActivation=pauseElement.isOnlyOneActivation();
		if (pauseElement.getClientType().isEmpty()) {
			pause.clientType=-1;
		} else {
			final Integer I=runModel.clientTypesMap.get(pauseElement.getClientType());
			if (I==null) return String.format(Language.tr("Simulation.Creator.SetInternalError"),id);
			pause.clientType=I;
		}
		pause.counter=pauseElement.getCounter();

		/* Bedingung */
		final String condition=pauseElement.getCondition();
		if (condition==null || condition.trim().isEmpty()) {
			pause.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.PauseCondition"),condition,element.getId(),error+1);
			pause.condition=condition;
		}

		return pause;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementAnimationPause)) return null;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementAnimationPauseData getData(final SimulationData simData) {
		RunElementAnimationPauseData data;
		data=(RunElementAnimationPauseData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementAnimationPauseData(this,condition,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Prüft, ob unter den aktuellen Randbedingungen die Animation angehalten werden soll.
	 * @param simData	Simulationsdaten
	 * @param client	Kunde
	 * @return	Liefert <code>true</code>, wenn die Animation angehalten werden soll
	 */
	private boolean testPause(SimulationData simData, RunDataClient client) {
		RunElementAnimationPauseData data=null;

		/* Nur bei Animation */
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
		/* Animation bei Kundenankunft pausieren */
		if (testPause(simData,client)) simData.runModel.animationConnect.animationViewer.pauseAnimation();

		/* Kunde zur nächsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}
