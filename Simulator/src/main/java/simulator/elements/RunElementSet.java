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
package simulator.elements;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSet;
import ui.modeleditor.elements.ModelElementSetRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementSet</code>
 * @author Alexander Herzog
 * @see ModelElementSet
 */
public class RunElementSet extends RunElementPassThrough {
	/**
	 * Indices der Variablen an die Werte zugewiesen werden sollen
	 */
	private int[] variableIndex;

	/**
	 * Rechenausdr�cke deren Ergebnisse an die mit {@link #variableIndex}
	 * bezeichneten Variablen zugewiesen werden sollen
	 */
	private String[] expressions;

	/**
	 * Optionale zus�tzliche Bedingung, die f�r eine Zuweisung erf�llt sein muss (kann <code>null</code> sein)
	 */
	private String condition;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementSet(final ModelElementSet element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Set.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSet)) return null;
		final ModelElementSet setElement=(ModelElementSet)element;
		final RunElementSet set=new RunElementSet((ModelElementSet)element);

		/* Auslaufende Kante */
		final String edgeError=set.buildEdgeOut(setElement);
		if (edgeError!=null) return edgeError;

		/* Zuweisungs-Arrays vorbereiten */
		final String[] variables=setElement.getVariables();
		final String[] expressions=setElement.getExpressions();
		final int size=FastMath.min(variables.length,expressions.length);
		set.variableIndex=new int[size];
		set.expressions=new String[size];

		for (int i=0;i<size;i++) {
			final int clientDataIndex=CalcSymbolClientUserData.testClientData(variables[i]);
			if (clientDataIndex>=0) {
				/* Kundendatenfeld */
				set.variableIndex[i]=-1-clientDataIndex;
			} else {
				/* Variablen */
				int index=-1;
				for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equalsIgnoreCase(variables[i])) {index=j; break;}
				if (index<0) return String.format(Language.tr("Simulation.Creator.SetInternalError"),element.getId());
				set.variableIndex[i]=index;
			}
			/* Ausdr�cke */
			if (!expressions[i].equals(ModelElementSetRecord.SPECIAL_WAITING) && !expressions[i].equals(ModelElementSetRecord.SPECIAL_TRANSFER) && !expressions[i].equals(ModelElementSetRecord.SPECIAL_PROCESS) && !expressions[i].equals(ModelElementSetRecord.SPECIAL_RESIDENCE)) {
				final int error=ExpressionCalc.check(expressions[i],runModel.variableNames,runModel.modelUserFunctions);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.SetInvalidExpression"),i+1,element.getId(),error+1);
			}
			set.expressions[i]=expressions[i];
		}

		/* Optionale Bedingung */
		final String condition=setElement.getCondition();
		if (condition==null || condition.isBlank()) {
			set.condition=null;
		} else {
			final int error=ExpressionMultiEval.check(condition,runModel.variableNames,runModel.modelUserFunctions);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.SetCondition"),condition,element.getId(),error+1);
			set.condition=condition;
		}

		return set;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementSet)) return null;
		final ModelElementSet setElement=(ModelElementSet)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(setElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementSetData getData(final SimulationData simData) {
		RunElementSetData data;
		data=(RunElementSetData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSetData(this,expressions,condition,simData.runModel);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * F�hrt die eigentlichen Zuweisungen durch.
	 * @param simData	Simulationsdatenobjekt
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 */
	private void applySet(final SimulationData simData, final RunElementSetData data, final RunDataClient client) {
		for (int i=0;i<variableIndex.length;i++) {
			/* Zuweisungen durchf�hren */
			double d=0.0;
			boolean ok=true;

			switch (data.mode[i]) {
			case MODE_EXPRESSION:
				simData.runData.setClientVariableValues(client);
				try {
					d=data.expressions[i].calc(simData.runData.variableValues,simData,client);
				} catch (MathCalcError e) {
					ok=false;
					simData.calculationErrorStation(data.expressions[i],this);
				}
				break;
			case MODE_WAITING_TIME:
				d=client.waitingTime*simData.runModel.scaleToSeconds;
				break;
			case MODE_TRANSFER_TIME:
				d=client.transferTime*simData.runModel.scaleToSeconds;
				break;
			case MODE_PROCESS_TIME:
				d=client.processTime*simData.runModel.scaleToSeconds;
				break;
			case MODE_RESIDENCE_TIME:
				d=client.residenceTime*simData.runModel.scaleToSeconds;
				break;
			}

			if (!ok) {
				/* Logging */
				if (simData.loggingActive) {
					final int index=variableIndex[i];
					final String varName;
					if (index<0) {
						varName="ClientData("+(-index-1)+")";
					} else {
						varName=simData.runModel.variableNames[index];
					}
					log(simData,Language.tr("Simulation.Log.SetError"),String.format(Language.tr("Simulation.Log.SetError.Info"),client.logInfo(simData),name,varName));
				}
			} else {
				/* Speichern */
				final int len=simData.runData.variableValues.length;
				final int index=variableIndex[i];

				if (index<0) {
					/* Speichern als Kundendaten-Feld */
					final int clientDataIndex=-(index+1);
					client.setUserData(clientDataIndex,d);

					/* Logging */
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Set"),String.format(Language.tr("Simulation.Log.Set.InfoClientData"),client.logInfo(simData),name,clientDataIndex,NumberTools.formatNumber(d)));
				} else {
					/* Speichern in Variable */
					boolean done=false;
					if (index==len-3) {
						/* Pseudovariable: Wartezeit */
						final long l=(long)(d*simData.runModel.scaleToSimTime+0.5);
						client.waitingTime=(l>0)?l:0;
						client.residenceTime=client.waitingTime+client.transferTime+client.processTime;
						done=true;
					}
					if (index==len-2) {
						/* Pseudovariable: Transferzeit */
						final long l=(long)(d*simData.runModel.scaleToSimTime+0.5);
						client.transferTime=(l>0)?l:0;
						client.residenceTime=client.waitingTime+client.transferTime+client.processTime;
						done=true;
					}
					if (index==len-1) {
						/* Pseudovariable: Bedienzeit */
						final long l=(long)(d*simData.runModel.scaleToSimTime+0.5);
						client.processTime=(l>0)?l:0;
						client.residenceTime=client.waitingTime+client.transferTime+client.processTime;
						done=true;
					}
					if (!done) {
						/* Regul�re Variable speichern */
						simData.runData.variableValues[index]=d;
						simData.runData.updateVariableValueForStatistics(simData,index);
					}

					/* Logging */
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Set"),String.format(Language.tr("Simulation.Log.Set.Info"),client.logInfo(simData),name,simData.runModel.variableNames[variableIndex[i]],NumberTools.formatNumber(d)));
				}
			}
		}
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementSetData data=getData(simData);

		/* Zuweisung durchf�hren */
		if (condition!=null) {
			simData.runData.setClientVariableValues(client);
			if (data.condition.eval(simData.runData.variableValues,simData,client)) applySet(simData,data,client);
		} else {
			applySet(simData,data,client);
		}

		/* Kunde zur n�chsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}