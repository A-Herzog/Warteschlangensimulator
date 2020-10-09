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
package simulator.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import language.Language;
import simulator.coreelements.RunElement;
import simulator.runmodel.RunModelFixer;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementEdge;

/**
 * Enthält einen Status bestehend aus einem Enumerationswert und einem Meldungstext
 * zu der Prüfung eines {@link ModelElement}-Objektes.
 * @author Alexander Herzog
 * @see RunElement#test(ModelElement)
 * @see RunModelCreator#testElement(ui.modeleditor.coreelements.ModelElementPosition)
 */
public class RunModelCreatorStatus {
	/**
	 * Mögliche Statuswerte die in einem {@link RunModelCreatorStatus}-Objekt zurückgegeben werden können.
	 * @author Alexander Herzog
	 */
	public enum Status {
		/** Es ist kein Fehler aufgetreten. */
		NO_ERROR,

		/** Es ist ein nicht weiter ausdifferenzierbarer Fehler aufgetreten. */
		UNKNOWN_ERROR,

		/** Dem Element fehlt eine auslaufende Kante. */
		NO_EDGE_OUT,

		/** Eine auslaufende Kante führt zu keinem weiteren Element über das die Differenzierung der Kanten erfolgen könnte. */
		EDGE_TO_NOWHERE,

		/** Ein notwendige Elementenname fehlt. */
		NO_NAME((e,s)->RunModelFixerHelper.setName(e,s)),

		/** Ein notwendiger Gruppenname fehlt. */
		NO_GROUP_NAME((e,s)->RunModelFixerHelper.setGroupName(e,s)),

		/** Ein notwendige Kundendatensatz-Name fehlt. */
		NO_SOURCE_RECORD_NAME((e,s)->RunModelFixerHelper.setSourceRecordName(e,s)),

		/** Es wurde ein negativer Zeitpunkt für den Start der Planung der ersten Kundenankunft gewählt. */
		NEGATIVE_ARRIVAL_START_TIME((e,s)->RunModelFixerHelper.setArrivalStartTime(e,s)),

		/** Bei einem Analogwert-Änderungs-Benachrichtigung ist der zeitliche Abstand der Prüfungen negativ. */
		ANALOG_NOTIFY_NEGATIVE((e,s)->RunModelFixerHelper.setAnalogNotifyTime(e,s)),

		/** Initialer Wert für Analog-Wert-Element niedriger als angegebener Minimalwert. */
		ANALOG_INITIAL_LOWER_THAN_MIN((e,s)->RunModelFixerHelper.setAnalogInitialHigher(e,s)),

		/** Initialer Wert für Analog-Wert-Element höher als angegebener Maximalwert. */
		ANALOG_INITIAL_HIGHER_THAN_MAX((e,s)->RunModelFixerHelper.setAnalogInitialLower(e,s)),

		/** Maximalwert für Analog-Wert-Element niedriger als angegebener Minimalwert. */
		ANALOG_MAX_LOWER_THAN_MIN((e,s)->RunModelFixerHelper.setAnalogMinMax(e,s)),

		/** Bei einer externen Kundenquelle ist die Tabelle, in der die zu ladenden Kundentypen angegeben wird, leer. */
		NO_CLIENT_TYPES_TABLE,

		/** Bei einer Fertigungsplanzuweisung wurde kein Fertigungsplan gewählt. */
		NO_SEQUENCE,

		/** Die (feste) Batch-Größe an einer Batch-Station ist kleiner als 1. */
		FIXED_BATCH_SIZE_LOWER_THAN_1((e,s)->RunModelFixerHelper.setBatchFixedError(e,s)),

		/** Die minimale Batch-Größe an einer Batch-Station ist kleiner als 1. */
		MIN_BATCH_SIZE_LOWER_THAN_1((e,s)->RunModelFixerHelper.setBatchMinError(e,s)),

		/** Die maximale Batch-Größe an einer Batch-Station ist kleiner als die minimale Batch-Größe. */
		MAX_BATCH_SIZE_LOWER_THAN_MIN((e,s)->RunModelFixerHelper.setBatchMaxError(e,s)),

		/** An einer Icon-Station wurde kein Icon angegeben. */
		NO_ICON,

		/** Es wurde eine negative Fließbandkapazität angegeben. */
		CONVEYOR_CAPACITY_NEGATIVE((e,s)->RunModelFixerHelper.setConveyorCapacity(e,s)),

		/** Es wurde eine negative Zeitdauer für den Transport auf einem Fließband angegeben. */
		CONVEYOR_TIME_NEGATIVE((e,s)->RunModelFixerHelper.setConveyorTime(e,s)),

		/** An einer Decide-by-Script-Station, an der ein Skript also zwingend erforderlich ist, fehlt eben dieses. */
		NO_SCRIPT,

		/** An einer Eingabe/Eingabe-JS-Station fehlt die Angabe zu einer Eingabedatei. */
		NO_INPUT_FILE,

		/** An einer Ausgabe/Ausgabe-JS-Station fehlt die Angabe zu einer Ausgabedatei. */
		NO_OUTPUT_FILE,

		/** Bei einem Slider ist ein kleinerer Maximalwert als der Minimalwert angegeben. */
		SLIDER_MAX_LOWER_THAN_MIN((e,s)->RunModelFixerHelper.setSliderMinMax(e,s)),

		/** Bei einem Slider ist keine Schrittweite größer als 0 angegeben. */
		SLIDER_STEP_LESS_OR_EQUAL_0((e,s)->RunModelFixerHelper.setSliderStep(e,s)),

		/** Bei einer Station zum Verlassen eines Bereichs ist kein Name für den Bereich, der verlassen werden soll, angegeben. */
		NO_SECTION_NAME((e,s)->RunModelFixerHelper.setSectionEndSection(e,s)),

		/** Es wurde eine ungültige Kapazität (kleiner oder gleich 0) für einen Tank angegeben. */
		TANK_CAPACITY_LESS_OR_EQUAL_0((e,s)->RunModelFixerHelper.setTankCapacity(e,s)),

		/** Es wurde ein ungültiger Startfüllstand (kleiner als das Minimum oder größer als das Maximum) für einen Tank angegeben. */
		TANK_INVALID_INITIAL_VALUE((e,s)->RunModelFixerHelper.setTankInitialValue(e,s)),

		/** Die für einen Teleport-Transport angegebene Zielstation ist ungültig. */
		TELEPORT_INVALID_DESTINATION((e,s)->RunModelFixerHelper.setTeleportDestination(e,s)),

		/** Der Parkplatz besitzt eine negative Kapazität. */
		PARKING_NEGATIVE_CAPACITY((e,s)->RunModelFixerHelper.setParkingCapacity(e,s)),

		/** Ein Transporter-Startpunkt besitzt eine negative Parkplatzkapazität. */
		TRANSPORTER_SOURCE_NEGATIVE_PARKING_CAPACITY((e,s)->RunModelFixerHelper.setTransporterParkingCapacity(e,s)),

		/** Ein Transporter-Startpunkt besitzt nicht-positive Mindestanzahl an wartenden Kunden um einen Transporter anzufordern. */
		TRANSPORTER_SOURCE_MIN_REQUEST_NUMBER_LESS_OR_EQUAL_0((e,s)->RunModelFixerHelper.setTransporterMinRequest(e,s)),

		/** DDE steht auf der aktuellen Plattform generell nicht zur Verfügung */
		NO_DDE,

		/** Ungültige Startzeile für die DDE-Ausgabe. */
		DDE_OUTPUT_INVALID_ROW((e,s)->RunModelFixerHelper.setDDEOutputRow(e,s)),

		/** Ungültige Spalte für die DDE-Ausgabe. */
		DDE_OUTPUT_INVALID_COL((e,s)->RunModelFixerHelper.setDDEOutputCol(e,s)),

		/** Es wurde an einer Bedienstation eine Wartezeittoleranz angegeben, aber es wurde keine auslaufende Kante für die Warteabbrecher angegeben. */
		PROCESS_CANCELATION_TIME_BUT_NO_EDGE((e,s)->RunModelFixerHelper.processTurnOffCancelTimes(e,s)),

		/** Es wurden keine notwendigen Ressourcen an der Bedienstation angegeben. */
		PROCESS_NO_RESOURCE((e,s)->RunModelFixerHelper.processResources(e,s)),

		/** Es wurden an der Bedienstation Batch-Bedienungen und Rüstzeiten gleichzeitig verwendet, was nicht zulässig ist. */
		PROCESS_MIX_BATCH_AND_SETUP((e,s)->RunModelFixerHelper.processBatchSetup(e,s)),

		/** An einer Bedienstation wurde eine minimale Bedien-Batchgröße von weniger als 1 angegeben. */
		PROCESS_MIN_BATCH_LOWER_THAN_1((e,s)->RunModelFixerHelper.processMinBatchSize(e,s)),

		/** An einer Bedienstation wurde eine maximale Bedien-Batchgröße angegeben, die geringer ist als die minimale Bedien-Batchgröße. */
		PROCESS_MAX_BATCH_LOWER_THAN_MIN((e,s)->RunModelFixerHelper.processMaxBatchSize(e,s));

		private final BiFunction<ModelElementPosition,RunModelCreatorStatus,List<RunModelFixer>> fix;

		/**
		 * Konstruktor der Enum
		 */
		Status() {
			this.fix=(e,s)->new ArrayList<>();
		}

		Status(final BiFunction<ModelElementPosition,RunModelCreatorStatus,List<RunModelFixer>> fix) {
			this.fix=fix;
		}
	}

	/**
	 * Fehlermeldung. Ist im Fall des Status {@link Status#NO_ERROR} ein leerer String, sonst ein nicht leerer String. Ist nie <code>null</code>.
	 */
	public final String message;

	/**
	 * Statuscode zu dem Fehler bzw. der Rückmeldung.
	 */
	public final Status status;

	/**
	 * Konstruktor der Klasse<br>
	 * Legt ein "kein Fehler"-Statusobjekt an.
	 * @see Status#NO_ERROR
	 */
	private RunModelCreatorStatus() {
		this.message="";
		this.status=Status.NO_ERROR;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Legt ein Statusobjekt mit einem nicht näher spezifizierten Fehler an
	 * @param message	Fehlermeldung (sollte ein nicht leerer String sein)
	 * Status#UNKNOWN_ERROR
	 */
	public RunModelCreatorStatus(final String message) {
		this.message=(message==null || message.isEmpty())?"Error":message;
		status=Status.UNKNOWN_ERROR;
	}

	/**
	 * Konstruktor der Klasse
	 * @param message	Fehlermeldung (sollte bei {@link Status#NO_ERROR} ein leerer String sein, sonst ein nicht leerer String)
	 * @param status	Fehlerstatus gemäß {@link Status}
	 */
	public RunModelCreatorStatus(final String message, final Status status) {
		this.message=message;
		this.status=status;
	}

	/**
	 * Gibt an, ob es sich bei der Rückmeldung um einen Fehlercode oder um eine
	 * Erfolgsmeldung handelt.
	 * @return	Gibt <code>true</code> zurück, wenn kein Fehler aufgetreten ist
	 */
	public boolean isOk() {
		return status==Status.NO_ERROR;
	}

	/**
	 * Liefert eine Liste mit möglichen Korrekturen für eine Station
	 * @param element	Zu prüfende Station
	 * @return	Korrekturvorschläge (kann eine leere Liste sein, ist aber nie <code>null</code> - selbst wenn die Station vollständig in Ordnung ist)
	 * @see RunModelFixer
	 */
	public List<RunModelFixer> getFix(final ModelElementPosition element) {
		if (element==null || isOk() || status.fix==null) return new ArrayList<>();
		return status.fix.apply(element,this);
	}

	/**
	 * Enthält das "kein Fehler"-Statusobjekt.
	 */
	public static RunModelCreatorStatus ok=new RunModelCreatorStatus();

	/**
	 * Liefert den Fehlerstatus "kein Name"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_NAME
	 */
	public static RunModelCreatorStatus noName(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoName"),element.getId()),Status.NO_NAME);
	}

	/**
	 * Liefert den Fehlerstatus "kein Gruppenname"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_GROUP_NAME
	 */
	public static RunModelCreatorStatus noGroupName(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoGroupName"),element.getId()),Status.NO_GROUP_NAME);
	}

	/**
	 * Liefert den Fehlerstatus "kein auslaufende Kante"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_EDGE_OUT
	 */
	public static RunModelCreatorStatus noEdgeOut(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId()),Status.NO_EDGE_OUT);
	}

	/**
	 * Liefert den Fehlerstatus "kein auslaufende Kante"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_EDGE_OUT
	 */
	public static RunModelCreatorStatus noEdgeOut(final ModelElementEdgeOut element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoEdgeOut"),element.getId()),Status.NO_EDGE_OUT);
	}

	/**
	 * Liefert den Fehlerstatus "Auslaufende Kante führt zu keiner gültigen Folgestation"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @param edge	Editor-Verbindungskante auf die sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#EDGE_TO_NOWHERE
	 */
	public static RunModelCreatorStatus edgeToNowhere(final ModelElement element, final ModelElementEdge edge) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.EdgeToNowhere"),element.getId(),edge.getId()),Status.EDGE_TO_NOWHERE);
	}

	/**
	 * Liefert den Fehlerstatus "DDE nicht verfügbar"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_DDE
	 */
	public static RunModelCreatorStatus noDDE(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.DDENotAvailable"),element.getId()),Status.NO_DDE);
	}

	/**
	 * Liefert den Fehlerstatus "Kein Skript angegeben"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_SCRIPT
	 */
	public static RunModelCreatorStatus noScript(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoScript"),element.getId()),Status.NO_SCRIPT);
	}

	/**
	 * Liefert den Fehlerstatus "keine Eingabedatei angegeben"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_INPUT_FILE
	 */
	public static RunModelCreatorStatus noInputFile(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoInputFile"),element.getId()),Status.NO_INPUT_FILE);
	}

	/**
	 * Liefert den Fehlerstatus "keine Ausgabedatei angegeben"
	 * @param element	Editor-Element auf den sich der Fehler bezieht
	 * @return	Fehlerstatusobjekt
	 * @see Status#NO_OUTPUT_FILE
	 */
	public static RunModelCreatorStatus noOutputFile(final ModelElement element) {
		return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoOutputFile"),element.getId()),Status.NO_OUTPUT_FILE);
	}
}