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
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementTank;
import ui.modeleditor.elements.ModelElementTankFlowData;
import ui.modeleditor.elements.SimDataBuilder;

/**
 * Diese Klasse hält die Laufzeit-Flussinformationen vor.
 * @author Alexander Herzog
 * @see RunElementTankFlowByClient
 * @see RunElementTankFlowBySignal
 */
public class RunElementTankFlow {
	/**
	 * Zeitdauer nach der der Fluss gestoppt werden soll
	 */
	private final double stopTime;

	/**
	 * ID des Ausgangstanks (oder -1, wenn der Fluss in das System einläuft)
	 */
	public final int sourceID;

	/**
	 * 0-basierende Nummer des Ventils am Ausgangstank (wenn sourceID&ge;0 ist)
	 */
	public final int sourceValveNr;

	/**
	 * ID des Zieltanks (oder -1, wenn der Fluss aus dem System ausläuft)
	 */
	public final int destinationID;

	/**
	 * 0-basierende Nummer des Ventils am Zieltank (wenn destinationID&gt;0 ist)
	 */
	public final int destinationValveNr;

	/**
	 * Bedingung zum Beenden des Flusses
	 */
	public final ModelElementTankFlowData.FlowStopCondition stopCondition;

	/**
	 * Zeitdauer in MS für den Fluss
	 */
	private final long stopTimeMS;

	/**
	 * Durchflussmenge, nach der der Fluss beendet werden soll
	 */
	public final double stopQuantity;

	/**
	 * Name des Signals, das den Fluss stoppen soll
	 */
	public final String stopSignal;

	/* === Laufzeitdaten === */

	/**
	 * Quelltank (oder <code>null</code>, wenn Fluss aus dem Nichts)
	 */
	public final RunElementTank source;

	/**
	 * Zieltank (oder <code>null</code>, wenn Fluss in das Nichts verschwindet)
	 */
	public final RunElementTank destination;

	/**
	 * Datenobjekt des Quelltanks
	 * @see #source
	 */
	private RunElementTankData sourceData;

	/**
	 * Datenobjekt des Zieltanks
	 * @see #destination
	 */
	private RunElementTankData destinationData;

	/**
	 * Zeitpunkt, an dem der Fluss gestartet wurde
	 */
	public final long flowStartTime;

	/**
	 * Letzter Zeitpunkt, an dem die Durchflussmenge aktualisiert wurde
	 */
	public long lastTime;

	/**
	 * Bisherige tatsächliche Durchflussmenge
	 */
	public double flowQuantity;

	/**
	 * Gibt an, dass der Fluss beendet wurde.
	 */
	public boolean flowDone;

	/**
	 * Konstruktor der Klasse
	 * @param modelData	Modell-Flussinformations-Element aus dem die Daten geladen werden sollen
	 * @param scaleToSimTime	Faktor zur Umrechnung von Sekunden in Simulationszeit (z.B. MS)
	 */
	public RunElementTankFlow(final ModelElementTankFlowData modelData, final double scaleToSimTime) {
		sourceID=modelData.getSourceID();
		sourceValveNr=modelData.getSourceValveNr();
		destinationID=modelData.getDestinationID();
		destinationValveNr=modelData.getDestinationValveNr();
		stopCondition=modelData.getStopCondition();
		stopTime=modelData.getStopTime();
		stopTimeMS=FastMath.round(modelData.getStopTime()*scaleToSimTime);
		stopQuantity=modelData.getStopQuantity();
		stopSignal=modelData.getStopSignal();

		source=null;
		destination=null;
		flowStartTime=0;
		lastTime=0;
		flowQuantity=0;
		flowDone=false;
	}

	/**
	 * Legt basierend auf einem Vorlage-Element ein konkretes Arbeitselement an
	 * @param template	Vorlage
	 * @param source	Tatsächliche Quellelement des Flusses
	 * @param destination	Tatsächliches Zielelement des Flusses
	 * @param startTime	Zeitpunkt, an dem der Fluss gestartet wird
	 */
	public RunElementTankFlow(final RunElementTankFlow template, final RunElementTank source, final RunElementTank destination, final long startTime) {
		sourceID=template.sourceID;
		sourceValveNr=template.sourceValveNr;
		destinationID=template.destinationID;
		destinationValveNr=template.destinationValveNr;
		stopCondition=template.stopCondition;
		stopTime=template.stopTime;
		stopTimeMS=template.stopTimeMS;
		stopQuantity=template.stopQuantity;
		stopSignal=template.stopSignal;

		this.source=source;
		this.destination=destination;
		flowStartTime=startTime;
		lastTime=startTime;
		flowQuantity=0;
		flowDone=false;
	}

	/**
	 * Handelt es sich bei einer Station um einen Tank?
	 * @param id	ID der zu prüfenden Station
	 * @param model	Editor-Modell
	 * @return	Liefert <code>true</code>, wenn sich die ID auf einen Tank bezieht
	 */
	private boolean isTank(final int id, final EditModel model) {
		final ModelElement element=model.surface.getByIdIncludingSubModels(id);
		if (element==null) return false;
		return (element instanceof ModelElementTank);
	}

	/**
	 * Handelt es sich bei der angegebenen Ventil-Nummer um eine gültige Angabe?
	 * @param id	ID des Tanks
	 * @param nr	Nummer des Ventils an dem Tank
	 * @param model	Editor-Modell
	 * @return	Liefert <code>true</code>, wenn der angegebene Tank existiert und es an ihm das angegebene Ventil gibt
	 */
	private boolean isValidValveNr(final int id, final int nr, final EditModel model) {
		final ModelElement element=model.surface.getByIdIncludingSubModels(id);
		if (element==null) return false;
		if (!(element instanceof ModelElementTank)) return false;
		final ModelElementTank tank=(ModelElementTank)element;
		return (nr>=0 && nr<tank.getValves().size());
	}

	/**
	 * Prüft, ob die Daten des im Konstruktor übergebenen Modell-Flussinformations-Elements gültig sind
	 * @param id	ID des Elements, das die Daten verwenden soll (für mögliche Fehlermeldungen)
	 * @param model	Editor-Modell (zum Prüfen, ob Quell-/Ziel-ID auch Tanks sind usw.)
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public RunModelCreatorStatus test(final int id, final EditModel model) {
		if (sourceID<0 && destinationID<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.SourceDestinationNotConnected"),id));

		if (sourceID>=0) {
			if (!isTank(sourceID,model)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.SourceNotTank"),id,sourceID));
			if (!isValidValveNr(sourceID,sourceValveNr,model)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.SourceInvalidValveNr"),id,sourceID,sourceValveNr+1));
		}

		if (destinationID>=0) {
			if (!isTank(destinationID,model)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.DestinationNotTank"),id,destinationID));
			if (!isValidValveNr(destinationID,destinationValveNr,model)) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.DestinationInvalidValveNr"),id,destinationID,destinationValveNr+1));
		}

		switch (stopCondition) {
		case STOP_BY_TIME:
			if (stopTime<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.InvalidStopTime"),id,NumberTools.formatNumber(stopTime)));
			break;
		case STOP_BY_QUANTITY:
			if (stopQuantity<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.InvalidStopQuantity"),id,NumberTools.formatNumber(stopQuantity)));
			break;
		case STOP_BY_SIGNAL:
			if (stopSignal.isBlank()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.AnalogFlow.NoStopSignal"),id));
			break;
		default:
			return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SetInternalError"),id));
		}

		return null;
	}

	/**
	 * Liefert die Logging-Daten zu dem Fluss
	 * @return	Logging-Bezeichner für diesen Fluss
	 */
	public String logInfo() {
		final StringBuilder sb=new StringBuilder();
		sb.append(Language.tr("Simulation.Log.FlowData"));
		sb.append(" (id=");
		sb.append(hashCode());
		sb.append(", ");
		sb.append(Language.tr("Simulation.Log.FlowData.Source"));
		sb.append("=");
		if (sourceID>=0) sb.append(sourceID); else sb.append(Language.tr("Simulation.Log.FlowData.Source.NoConnection"));
		sb.append(", ");
		sb.append(Language.tr("Simulation.Log.FlowData.Destination"));
		sb.append("=");
		if (destinationID>=0) sb.append(destinationID); else sb.append(Language.tr("Simulation.Log.FlowData.Destination.NoConnection"));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Führt einen Teil eines konkreten Flusses aus.
	 * @param addQuantity	Zu verlagernde Menge
	 * @param newTime	Aktueller Zeitpunkt
	 * @return	Gibt die Menge an, die verlagert werden konnte.
	 * @see #processFlow(SimulationData)
	 */
	private double addFlowQuanity(final double addQuantity, final long newTime) {
		final double partQuantity;
		switch (stopCondition) {
		case STOP_BY_TIME:
			if (flowStartTime+stopTimeMS<=newTime || newTime==lastTime) {
				if (newTime==lastTime) partQuantity=0; else partQuantity=addQuantity*((flowStartTime+stopTimeMS)-lastTime)/(newTime-lastTime);
			} else {
				partQuantity=addQuantity;
			}
			break;
		case STOP_BY_QUANTITY:
			if (stopQuantity<=flowQuantity+addQuantity) {
				partQuantity=stopQuantity-flowQuantity;
			} else {
				partQuantity=addQuantity;
			}
			break;
		case STOP_BY_SIGNAL:
			partQuantity=addQuantity;
			break;
		default:
			partQuantity=addQuantity;
			break;
		}
		lastTime=newTime;
		flowQuantity+=partQuantity;
		return partQuantity;
	}

	/**
	 * Wie viel Zeit ist bis (z.B. zum geplanten Endes des Flusses) wirklich verstrichen?
	 * @param newTime	Aktuelle Zeit
	 * @return	Vergangene Zeit
	 * @see #processFlow(SimulationData)
	 */
	private long realTimeDelta(final long newTime) {
		if (newTime==lastTime) return 0;
		if (stopCondition==ModelElementTankFlowData.FlowStopCondition.STOP_BY_TIME && flowStartTime+stopTimeMS<newTime) return (flowStartTime+stopTimeMS)-lastTime;
		return newTime-lastTime;
	}

	/**
	 * Versucht einen Fluss im aktuellen Zeitschritt zu verarbeiten
	 * @param simData	Simulationsdatenobjekt
	 */
	public void processFlow(final SimulationData simData) {
		/* Wurde der Fluss bereits zuvor beendet? */
		if (flowDone) return;

		/* Referenzen vorbereiten */
		if (source!=null && sourceData==null) sourceData=source.getData(simData);
		if (destination!=null && destinationData==null) destinationData=destination.getData(simData);

		/* Wie viel Zeit ist bis (z.B. zum geplanten Endes des Flusses) wirklich verstrichen? */
		final long realTimeDelta=realTimeDelta(simData.currentTime);

		/* Wie viel können Quelle und Ziel bieten? */
		double quantity=Double.MAX_VALUE;
		if (source!=null) quantity=FastMath.min(quantity,sourceData.getMaxFlowOut(sourceValveNr,realTimeDelta,simData));
		if (destination!=null) quantity=FastMath.min(quantity,destinationData.getMaxFlowIn(destinationValveNr,realTimeDelta,simData));
		if (quantity<=0) {
			if (stopCondition==ModelElementTankFlowData.FlowStopCondition.STOP_BY_TIME && simData.currentTime>=flowStartTime+stopTimeMS) flowDone=true;
			return;
		}

		/* Versuchen, so viel wie möglich von dem geplanten Fluss verarbeiten */
		final double realFlow=addFlowQuanity(quantity,lastTime+realTimeDelta);
		lastTime=simData.currentTime;

		/* Menge bei Quell aus- und bei Ziel eintragen */
		if (source!=null) sourceData.changeValueByFlow(-realFlow);
		if (destination!=null) destinationData.changeValueByFlow(+realFlow);

		/* Konnte nicht alles verarbeitet werden, so ist der Fluss zu Ende */
		if (realFlow<quantity) flowDone=true;
	}

	/**
	 * Liefert für die Darstellung während der Animation Informationen
	 * zu einem Fluss.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Informationen zu einem Fluss.
	 * @see ModelElementTank#addInformationToAnimationRunTimeData(SimDataBuilder)
	 */
	public String getAnimationInfoText(final SimulationData simData) {
		final StringBuilder sb=new StringBuilder();

		sb.append(Language.tr("Statistics.AnalogValue.Flow"));
		sb.append(": ");

		if (sourceID<0) {
			sb.append(Language.tr("Statistics.AnalogValue.Flow.SourceSystem"));
		} else {
			sb.append(source.name);
			sb.append(" ("+Language.tr("Statistics.AnalogValue.Flow.Valve")+" ");
			sb.append(""+(sourceValveNr+1));
			sb.append(")");
		}

		sb.append(" -> ");

		if (destinationID<0) {
			sb.append(Language.tr("Statistics.AnalogValue.Flow.DestinationSystem"));
		} else {
			sb.append(destination.name);
			sb.append(" ("+Language.tr("Statistics.AnalogValue.Flow.Valve")+" ");
			sb.append(""+(destinationValveNr+1));
			sb.append(")");
		}

		sb.append(" (");
		if (flowDone) {
			sb.append(Language.tr("Statistics.AnalogValue.Flow.Stop.Done"));
		} else {
			switch (stopCondition) {
			case STOP_BY_TIME:
				sb.append(String.format(Language.tr("Statistics.AnalogValue.Flow.Stop.ByTime"),NumberTools.formatNumber(stopTime),NumberTools.formatNumber((stopTimeMS-(lastTime-flowStartTime))*simData.runModel.scaleToSeconds)));
				break;
			case STOP_BY_QUANTITY:
				sb.append(String.format(Language.tr("Statistics.AnalogValue.Flow.Stop.ByQuantity"),NumberTools.formatNumber(stopQuantity),NumberTools.formatNumber(flowQuantity)));
				break;
			case STOP_BY_SIGNAL:
				sb.append(String.format(Language.tr("Statistics.AnalogValue.Flow.Stop.BySignal"),stopSignal));
				break;
			}
		}
		sb.append(")");

		return sb.toString();
	}
}