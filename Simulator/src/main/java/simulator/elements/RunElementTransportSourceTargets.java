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

import java.util.ArrayList;
import java.util.List;

import language.Language;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionEval;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementTransportSource;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;
import ui.modeleditor.elements.TransportTargetRecord;
import ui.modeleditor.elements.TransportTargetSystem;

/**
 * Diese Klasse hält die Laufzeitmodelldaten für die Transportziele vor.
 * Sie wird von {@link RunElementTransportSource} verwendet.
 * @author Alexander Herzog
 * @see RunElementTransportSource
 */
public class RunElementTransportSourceTargets {
	/** ID der Zielstation, zu der Kunden transportiert werden sollen, wenn keine der Routing-Regeln zutrifft */
	private int defaultStation;
	/** Modus zur Auswahl der Zielstation */
	private TransportTargetSystem.RoutingMode mode;

	/**
	 * Liste mit Kundentypen, die, wenn erfüllt, ein Transportziel bestimmen.<br>
	 * Einträge können -1 sein und auch die komplette Variable kann <code>null</code> sein.
	 */
	private int[] routingClientType;

	/**
	 * Liste mit den Ausdrücken, die, wenn erfüllt, ein Transportziel bestimmen.<br>
	 * Einträge können <code>null</code> sein und auch die komplette Variable kann <code>null</code> sein.
	 */
	public String[] routingExpression;

	/**
	 * IDs der Ziele gemäß {@link #routingClientType} und {@link #routingExpression}
	 */
	private int[] routingDestination;

	/**
	 * Kundentextdaten-Schlüssel aus dem das Routing-Ziel ausgelesen werden soll
	 */
	private String routingClientPropertyName;

	/**
	 * Zu dem Transport gehörendes Editor-Transport-Start-Element (zum Auslesen der ID für mögliche Fehlermeldungen)
	 */
	private final ModelElement element;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zu dem Transport gehörendes Editor-Transport-Start-Element (zum Auslesen der ID für mögliche Fehlermeldungen)
	 */
	public RunElementTransportSourceTargets(final ModelElement element) {
		this.element=element;
	}

	/**
	 * Lädt die Laufzeitdaten aus einem {@link TransportTargetSystem}-Objekt
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param transportTargetSystem	{@link TransportTargetSystem}-Objekt aus dem die Daten geladen werden sollen
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public String build(final EditModel editModel, final RunModel runModel, final TransportTargetSystem transportTargetSystem) {
		int id;

		/* Default-Transportziel */
		id=RunElementTransportSourceTools.getDestinationID(element,transportTargetSystem.getDefaultStation());
		if (id<0) return String.format(Language.tr("Simulation.Creator.RoutingStation"),transportTargetSystem.getDefaultStation(),element.getId());
		defaultStation=id;

		/* Modus */
		mode=transportTargetSystem.getMode();

		/* Explizite Transportziele */
		if (mode==TransportTargetSystem.RoutingMode.ROUTING_MODE_EXPLICITE) {
			/* Transportziele */
			final List<Integer> routingClientTypeList=new ArrayList<>();
			final List<String> routingExpressionList=new ArrayList<>();
			final List<Integer> routingDestinationList=new ArrayList<>();

			TransportTargetSystem targetSystem=null;
			if (element instanceof ModelElementTransportSource) targetSystem=((ModelElementTransportSource)element).getTransportTargetSystem();
			if (element instanceof ModelElementTransportTransporterSource) targetSystem=((ModelElementTransportTransporterSource)element).getTransportTargetSystem();
			if (targetSystem==null) return String.format(Language.tr("Simulation.Creator.SetInternalError"),element.getId());
			for (TransportTargetRecord route : targetSystem.getRouting()) {

				if (route.routingMode==TransportTargetRecord.RoutingMode.ROUTING_MODE_CLIENT_TYPE) {
					/* Kunentyp */
					id=runModel.getClientTypeNr(route.routingCondition);
					if (id<0) return String.format(Language.tr("Simulation.Creator.RoutingCondition.ClientType"),route.routingCondition,element.getId());
					routingClientTypeList.add(id);
					routingExpressionList.add(null);
				} else {
					/* Ausdruck */
					routingClientTypeList.add(-1);
					final int error=ExpressionMultiEval.check(route.routingCondition,runModel.variableNames);
					if (error>=0) return String.format(Language.tr("Simulation.Creator.RoutingCondition.Expression"),route.routingCondition,element.getId(),error+1);
					routingExpressionList.add(route.routingCondition);
				}

				id=RunElementTransportSourceTools.getDestinationID(element,route.station);
				if (id<0) return String.format(Language.tr("Simulation.Creator.RoutingStation"),route.station,element.getId());
				routingDestinationList.add(id);
			}

			routingClientTypeList.add(-1);
			routingExpressionList.add(null);
			routingDestinationList.add(defaultStation);

			routingClientType=new int[routingClientTypeList.size()];
			for (int i=0;i<routingClientTypeList.size();i++) routingClientType[i]=routingClientTypeList.get(i);
			routingExpression=routingExpressionList.toArray(new String[0]);
			routingDestination=new int[routingDestinationList.size()];
			for (int i=0;i<routingDestinationList.size();i++) routingDestination[i]=routingDestinationList.get(i);
		}

		/* Routing nach Kunden-Texteigenschaft */
		if (mode==TransportTargetSystem.RoutingMode.ROUTING_MODE_TEXT_PROPERTY) {
			routingClientPropertyName=transportTargetSystem.getRoutingProperty().trim();
			if (routingClientPropertyName.isEmpty()) return String.format(Language.tr("Simulation.Creator.RoutingClientProperty"),element.getId());
		}

		return null;
	}

	/**
	 * Prüft, ob die Daten in einem {@link TransportTargetSystem}-Objekt (grob) in Ordnung sind
	 * @param element	Editor-Transport-Start-Element (zum Auslesen der ID für mögliche Fehlermeldungen)
	 * @param transportTargetSystem	{@link TransportTargetSystem}-Objekt das inhaltlich geprüft werden soll
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück und im Fehlerfall eine Fehlermeldung.
	 */
	public static RunModelCreatorStatus test(final ModelElement element, final TransportTargetSystem transportTargetSystem) {
		int id;

		/* Explizite Transportziele */
		if (transportTargetSystem.getMode()==TransportTargetSystem.RoutingMode.ROUTING_MODE_EXPLICITE) {
			for (TransportTargetRecord route : transportTargetSystem.getRouting()) {
				id=RunElementTransportSourceTools.getDestinationID(element,route.station);
				if (id<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.RoutingStation"),route.station,element.getId()));
			}
		}

		/* Routing nach Kunden-Texteigenschaft */
		if (transportTargetSystem.getMode()==TransportTargetSystem.RoutingMode.ROUTING_MODE_TEXT_PROPERTY) {
			final String routingClientPropertyName=transportTargetSystem.getRoutingProperty().trim();
			if (routingClientPropertyName.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.RoutingClientProperty"),element.getId()));
		}

		/* Default-Transportziel */
		id=RunElementTransportSourceTools.getDestinationID(element,transportTargetSystem.getDefaultStation());
		if (id<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.RoutingStation"),transportTargetSystem.getDefaultStation(),element.getId()));

		return null;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSec=1.0/1000.0;

	/**
	 * Zielstation gemäß Fertigungsplan bestimmen
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @param testOnly	Keine Verarbeitung von Zuweisungen und Erhöhung des Feritungsplansschritts, sondern nur die Station liefern
	 * @return	ID der Zielstation
	 * @see #getDestinationStation(SimulationData, RunDataClient, ExpressionMultiEval[], boolean)
	 */
	private int getDestinationStationByClientSequence(final SimulationData simData, final RunDataClient client, final boolean testOnly) {
		final int nr=client.sequenceNr;
		if (nr<0) return defaultStation;
		final int step=client.sequenceStep;
		if (step<0) return defaultStation;

		final int id=simData.runModel.sequenceStepStationIDs[nr][step];

		if (testOnly) return id;

		final int[] assignmentNr=simData.runModel.sequenceStepAssignmentNr[nr][step];
		if (assignmentNr.length>0) {
			final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSec;
			simData.runData.setClientVariableValues(client,additionalWaitingTime);
		}
		for (int i=0;i<assignmentNr.length;i++) {
			try {
				final double value=simData.runData.sequenceStepAssignmentExpression[nr][step][i].calc();
				client.setUserData(assignmentNr[i],value);
			} catch (MathCalcError e) {}
		}

		client.sequenceStep=simData.runModel.sequenceStepNext[nr][step];

		return id;
	}

	/**
	 * Zielstation gemäß Kundentyp und Rechenausdruck bestimmen
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @param routingExpresions	Auszuwertende Rechenausdrücke
	 * @return	ID der Zielstation
	 * @see #getDestinationStation(SimulationData, RunDataClient, ExpressionMultiEval[], boolean)
	 */
	private int getDestinationStationByRouting(final SimulationData simData, final RunDataClient client, final ExpressionMultiEval[] routingExpresions) {
		for (int i=0;i<routingDestination.length;i++) {
			final int clientType=routingClientType[i];
			if (clientType>=0) {
				if (client.type==clientType) return routingDestination[i];
				continue;
			}

			final ExpressionMultiEval eval=routingExpresions[i];
			if (eval!=null) {
				final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSec;
				simData.runData.setClientVariableValues(client,additionalWaitingTime);
				if (eval.eval(simData.runData.variableValues,simData,client)) return routingDestination[i];
				continue;
			}

			return routingDestination[i];
		}

		return routingDestination[routingDestination.length-1];
	}

	/**
	 * Zielstation gemäß Kundentextdatenfeld bestimmen
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	ID der Zielstation
	 * @see #getDestinationStation(SimulationData, RunDataClient, ExpressionMultiEval[], boolean)
	 */
	private int getDestinationStationByClientProperty(final SimulationData simData, final RunDataClient client) {
		/* Texteigenschaft für Zielstation vorhanden? */
		final String stationName=client.getUserDataString(routingClientPropertyName);
		if (stationName.isEmpty()) return defaultStation;

		/* Wert der Texteigenschaft ist Name einer Station? */
		final Integer ID=simData.runModel.namesToIDs.get(stationName);
		if (ID==null) return defaultStation;
		final int id=ID.intValue();

		/* Station ist gültiges Ziel? */
		final RunElement element=simData.runModel.elementsFast[id];
		if (element instanceof RunElementTransportDestination) return id;

		return defaultStation;
	}

	/**
	 * Liefert die ID der Zielstation für einen Transport
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunden, der transportiert werden soll
	 * @param routingExpresions	Liste mit {@link ExpressionEval}-Objekten zur Bestimmung der Ziele (muss in Bezug auf die <code>null</code>-Belegung mit {@link RunElementTransportSourceTargets#routingExpression} übereinstimmen)
	 * @param testOnly	Keine Verarbeitung von Zuweisungen und Erhöhung des Feritungsplansschritts, sondern nur die Station liefern
	 * @return	ID der Zielstation
	 */
	public int getDestinationStation(final SimulationData simData, final RunDataClient client, final ExpressionMultiEval[] routingExpresions, final boolean testOnly) {
		switch (mode) {
		case ROUTING_MODE_EXPLICITE: return getDestinationStationByRouting(simData,client,routingExpresions);
		case ROUTING_MODE_SEQUENCE: return getDestinationStationByClientSequence(simData,client,testOnly);
		case ROUTING_MODE_TEXT_PROPERTY: return getDestinationStationByClientProperty(simData,client);
		default: return defaultStation;
		}
	}
}