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
import java.util.Arrays;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModelFixer;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurface.TimeBase;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementCounterMulti;
import ui.modeleditor.elements.ModelElementInteractiveSlider;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSectionEnd;
import ui.modeleditor.elements.ModelElementSectionStart;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSplit;
import ui.modeleditor.elements.ModelElementStateStatistics;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementTeleportSource;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;

/**
 * Diese Klasse enthält die statischen Hilfsroutinen, die es ermöglichen
 * bestimmte Korrekturen an dem Modell vorzunehmen.
 * @author Alexander Herzog
 * @see RunModelCreatorStatus
 * @see RunModelFixer
 */
public class RunModelFixerHelper {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Hilfsfunktionen bereit
	 */
	private RunModelFixerHelper() {}

	private static boolean nameInUse(final ModelSurface surface, final String name) {
		for (ModelElement element: surface.getElements()) {
			if (element.getName().equalsIgnoreCase(name)) return true;
			if (element instanceof ModelElementSub) {
				if (nameInUse(((ModelElementSub)element).getSubSurface(),name)) return true;
			}
		}
		return false;
	}

	private static String makeIndividualName(final EditModel model, final String baseName) {
		int count=1;
		while (true) {
			String name=(count==1)?baseName:(baseName+count);
			if (!nameInUse(model.surface,name)) return name;
			count++;
		}
	}

	/**
	 * Korrektur für "Ein notwendige Elementenname fehlt."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#NO_NAME
	 */
	public static List<RunModelFixer> setName(final ModelElementPosition element, final RunModelCreatorStatus status) {
		if (!(element instanceof ModelElementBox)) return new ArrayList<>();
		final String name=makeIndividualName(element.getModel(),((ModelElementBox)element).getTypeName());
		return Arrays.asList(new RunModelFixer(
				element,
				status,
				String.format(Language.tr("Surface.PopupMenu.QuickFix.General.Name"),name),
				fix->element.setName(name)
				));
	}

	/**
	 * Korrektur für "Ein notwendiger Gruppenname fehlt."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#NO_GROUP_NAME
	 */
	public static List<RunModelFixer> setGroupName(final ModelElementPosition element, final RunModelCreatorStatus status) {
		if (element instanceof ModelElementCounter) {
			final ModelElementCounter counter=(ModelElementCounter)element;
			final String name=Language.tr("Surface.Counter.DefaultCounterName");
			return Arrays.asList(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.General.SetGroupName"),name),
					fix->counter.setGroupName(name)
					));
		}

		if (element instanceof ModelElementCounterMulti) {
			final ModelElementCounterMulti counterMulti=(ModelElementCounterMulti)element;
			final String name=Language.tr("Surface.Counter.DefaultCounterName");
			return Arrays.asList(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.General.SetGroupName"),name),
					fix->counterMulti.setGroupName(name)
					));
		}

		if (element instanceof ModelElementStateStatistics) {
			final ModelElementStateStatistics statistics=(ModelElementStateStatistics)element;
			final String name=Language.tr("Surface.StateStatistics.DefaultGroupName");
			return Arrays.asList(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.General.SetGroupName"),name),
					fix->statistics.setGroupName(name)
					));
		}

		return new ArrayList<>();
	}

	private static List<ModelElementSourceRecord> getSourceRecord(final ModelElementPosition element) {
		if (element instanceof ModelElementSource)  {
			return Arrays.asList(((ModelElementSource)element).getRecord());
		}
		if (element instanceof ModelElementSourceMulti)  {
			return ((ModelElementSourceMulti)element).getRecords();
		}
		if (element instanceof ModelElementSplit) {
			return ((ModelElementSplit)element).getRecords();
		}

		return null;
	}

	private static List<String> getClientTypeNames(final EditModel model) {
		final List<String> list=model.surface.getClientTypes();
		if (list!=null && !list.isEmpty()) return list;
		return Arrays.asList(Language.tr("Surface.Source.DefaultGroupName"));
	}

	/**
	 * Korrektur für "Ein notwendige Kundendatensatz-Name fehlt."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#NO_SOURCE_RECORD_NAME
	 */
	public static List<RunModelFixer> setSourceRecordName(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		final List<ModelElementSourceRecord> records=getSourceRecord(element);
		final List<String> suggestedNames=getClientTypeNames(element.getModel());
		if (records!=null && !records.isEmpty()) {
			if (!records.get(0).hasName())  {
				/* Name der Station selbst ist leer */
				for (String name: suggestedNames) {
					options.add(new RunModelFixer(
							element,
							status,
							String.format(Language.tr("Surface.PopupMenu.QuickFix.SourceRecord.StationName"),name),
							fix->element.setName(name)
							));
				}
			} else {
				/* Name eines Datensatzes ist leer */
				for (int i=0;i<records.size();i++) {
					final ModelElementSourceRecord record=records.get(i);
					if (record.getName().isEmpty()) for (String name: suggestedNames) {
						options.add(new RunModelFixer(
								element,
								status,
								String.format(Language.tr("Surface.PopupMenu.QuickFix.SourceRecord.RecordName"),i+1,name),
								fix->record.setName(name)
								));
					}
				}
			}
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurde ein negativer Zeitpunkt für den Start der Planung der ersten Kundenankunft gewählt."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#NEGATIVE_ARRIVAL_START_TIME
	 */
	public static List<RunModelFixer> setArrivalStartTime(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		final List<ModelElementSourceRecord> records=getSourceRecord(element);
		if (records!=null) for (int i=0;i<records.size();i++) {
			final ModelElementSourceRecord record=records.get(i);
			final ModelElementSourceRecord.NextMode mode=record.getNextMode();
			if (mode==ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION || mode==ModelElementSourceRecord.NextMode.NEXT_EXPRESSION || mode==ModelElementSourceRecord.NextMode.NEXT_CONDITION) {
				if (record.getArrivalStart()<0) {
					if (records.size()>1) {
						/* Mehrere Datensätze */
						options.add(new RunModelFixer(
								element,
								status,
								String.format(Language.tr("Surface.PopupMenu.QuickFix.SourceRecord.ArrivalStart.Multi"),i+1),
								fix->record.setArrivalStart(0)
								));
					} else {
						/* Nur ein Datensatz (nicht geklärt ob nur einer möglich oder nur einer vorhanden - aber auch nicht relevant) */
						options.add(new RunModelFixer(
								element,
								status,
								String.format(Language.tr("Surface.PopupMenu.QuickFix.SourceRecord.ArrivalStart.Single")),
								fix->record.setArrivalStart(0)
								));
					}
				}
			}
		}
		return options;
	}

	/**
	 * Korrektur für "Bei einem Analogwert-Änderungs-Benachrichtigung ist der zeitliche Abstand der Prüfungen negativ."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#ANALOG_NOTIFY_NEGATIVE
	 */
	public static List<RunModelFixer> setAnalogNotifyTime(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		final double value=60.0;
		if (element instanceof ModelElementAnalogValue) {
			final ModelElementAnalogValue analog=(ModelElementAnalogValue)element;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Analog.NotifyValue"),NumberTools.formatNumber(value)),
					fix->analog.setAnalogNotify(value)
					));
		}
		if (element instanceof ModelElementTank) {
			final ModelElementTank analog=(ModelElementTank)element;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Analog.NotifyValue"),NumberTools.formatNumber(value)),
					fix->analog.setAnalogNotify(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Initialer Wert für Analog-Wert-Element niedriger als angegebener Minimalwert."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#ANALOG_INITIAL_LOWER_THAN_MIN
	 */
	public static List<RunModelFixer> setAnalogInitialHigher(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementAnalogValue) {
			final ModelElementAnalogValue analog=(ModelElementAnalogValue)element;
			final double value=analog.getValueMin();
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Analog.SetInitialValue"),NumberTools.formatNumber(value)),
					fix->analog.setInitialValue(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Initialer Wert für Analog-Wert-Element höher als angegebener Maximalwert."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#ANALOG_INITIAL_HIGHER_THAN_MAX
	 */
	public static List<RunModelFixer> setAnalogInitialLower(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementAnalogValue) {
			final ModelElementAnalogValue analog=(ModelElementAnalogValue)element;
			final double value=analog.getValueMax();
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Analog.SetInitialValue"),NumberTools.formatNumber(value)),
					fix->analog.setInitialValue(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Maximalwert für Analog-Wert-Element niedriger als angegebener Minimalwert."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#ANALOG_MAX_LOWER_THAN_MIN
	 */
	public static List<RunModelFixer> setAnalogMinMax(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementAnalogValue) {
			final ModelElementAnalogValue analog=(ModelElementAnalogValue)element;
			final double value=analog.getValueMin()+10;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Analog.SetMaxValue"),NumberTools.formatNumber(value)),
					fix->analog.setValueMax(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Die Batch-Größe an einer Batch-Station ist kleiner als 1."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#FIXED_BATCH_SIZE_LOWER_THAN_1
	 */
	public static List<RunModelFixer> setBatchFixedError(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementBatch) {
			final ModelElementBatch batch=(ModelElementBatch)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.Batch.SetFixed1"),
					fix->batch.getBatchRecord().setBatchSizeFixed(1)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Die minimale Batch-Größe an einer Batch-Station ist kleiner als 1."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#MIN_BATCH_SIZE_LOWER_THAN_1
	 */
	public static List<RunModelFixer> setBatchMinError(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementBatch) {
			final ModelElementBatch batch=(ModelElementBatch)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.Batch.SetMin1"),
					fix->batch.getBatchRecord().setBatchSizeMin(1)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Die maximale Batch-Größe an einer Batch-Station ist kleiner als die minimale Batch-Größe."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#MAX_BATCH_SIZE_LOWER_THAN_MIN
	 */
	public static List<RunModelFixer> setBatchMaxError(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementBatch) {
			final ModelElementBatch batch=(ModelElementBatch)element;
			final int value=batch.getBatchRecord().getBatchSizeMin();
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Batch.SetMaxMin"),value),
					fix->batch.getBatchRecord().setBatchSizeMax(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurde eine negative Fließbandkapazität angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#CONVEYOR_CAPACITY_NEGATIVE
	 */
	public static List<RunModelFixer> setConveyorCapacity(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementConveyor) {
			final ModelElementConveyor conveyor=(ModelElementConveyor)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.Conveyor.Capacity"),
					fix->conveyor.setCapacityAvailable(1)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurde eine negative Zeitdauer für den Transport auf einem Fließband angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#CONVEYOR_TIME_NEGATIVE
	 */
	public static List<RunModelFixer> setConveyorTime(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementConveyor) {
			final ModelElementConveyor conveyor=(ModelElementConveyor)element;
			final double value=60.0;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Conveyor.Time"),value),
					fix->{conveyor.setTransportTime(value); conveyor.setTimeBase(TimeBase.TIMEBASE_SECONDS);}
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Bei einem Slider ist ein kleinerer Maximalwert als der Minimalwert angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#SLIDER_MAX_LOWER_THAN_MIN
	 */
	public static List<RunModelFixer> setSliderMinMax(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementInteractiveSlider) {
			final ModelElementInteractiveSlider slider=(ModelElementInteractiveSlider)element;
			final double value=slider.getMinValue()+10;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Slider.MaxValue"),NumberTools.formatNumber(value)),
					fix->slider.setMaxValue(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Bei einem Slider ist keine Schrittweite größer als 0 angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#SLIDER_STEP_LESS_OR_EQUAL_0
	 */
	public static List<RunModelFixer> setSliderStep(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementInteractiveSlider) {
			final ModelElementInteractiveSlider slider=(ModelElementInteractiveSlider)element;
			final double value=1;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Slider.Step"),NumberTools.formatNumber(value)),
					fix->slider.setStep(value)
					));
		}
		return options;
	}

	private static List<String> getSectionNames(final ModelSurface surface) {
		final List<String> names=new ArrayList<>();
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSectionStart && !element.getName().trim().isEmpty()) names.add(element.getName());
			if (element instanceof ModelElementSub) names.addAll(getSectionNames(((ModelElementSub)element).getSubSurface()));
		}
		return names;
	}

	/**
	 * Korrektur für "Bei einer Station zum Verlassen eines Bereichs ist kein Name für den Bereich, der verlassen werden soll, angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#NO_SECTION_NAME
	 */
	public static List<RunModelFixer> setSectionEndSection(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementSectionEnd) {
			final ModelElementSectionEnd sectionEnd=(ModelElementSectionEnd)element;
			final List<String> names=getSectionNames(element.getModel().surface);
			for (String name: names) {
				options.add(new RunModelFixer(
						element,
						status,
						String.format(Language.tr("Surface.PopupMenu.QuickFix.SectionEnd.Section"),name),
						fix->sectionEnd.setSectionStartName(name)
						));
			}
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurde eine ungültige Kapazität (kleiner oder gleich 0) für einen Tank angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#TANK_CAPACITY_LESS_OR_EQUAL_0
	 */
	public static List<RunModelFixer> setTankCapacity(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementTank) {
			final ModelElementTank tank=(ModelElementTank)element;
			final double value=100.0;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Tank.Capacity"),NumberTools.formatNumber(value)),
					fix->tank.setCapacity(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurde ein ungültiger Startfüllstand (kleiner als das Minimum oder größer als das Maximum) für einen Tank angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#TANK_INVALID_INITIAL_VALUE
	 */
	public static List<RunModelFixer> setTankInitialValue(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementTank) {
			final ModelElementTank tank=(ModelElementTank)element;
			final double oldInitial=tank.getInitialValue();
			final double max=tank.getCapacity();
			if (oldInitial<0) {
				final double value=0.0;
				options.add(new RunModelFixer(
						element,
						status,
						String.format(Language.tr("Surface.PopupMenu.QuickFix.Tank.Initial"),NumberTools.formatNumber(value)),
						fix->tank.setInitialValue(value)
						));
			} else {
				if (oldInitial>max) {
					final double value=max;
					options.add(new RunModelFixer(
							element,
							status,
							String.format(Language.tr("Surface.PopupMenu.QuickFix.Tank.Initial"),NumberTools.formatNumber(value)),
							fix->tank.setInitialValue(value)
							));
				}
			}
		}
		return options;
	}

	private static List<String> getTeleportDestinations(final ModelSurface surface) {
		final List<String> names=new ArrayList<>();
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementTeleportDestination && !element.getName().trim().isEmpty()) names.add(element.getName());
			if (element instanceof ModelElementSub) names.addAll(getSectionNames(((ModelElementSub)element).getSubSurface()));
		}
		return names;
	}

	/**
	 * Korrektur für "Die für einen Teleport-Transport angegebene Zielstation ist ungültig."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#TELEPORT_INVALID_DESTINATION
	 */
	public static List<RunModelFixer> setTeleportDestination(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementTeleportSource) {
			final ModelElementTeleportSource teleport=(ModelElementTeleportSource)element;
			final List<String> names=getTeleportDestinations(element.getModel().surface);
			for (String name: names) {
				options.add(new RunModelFixer(
						element,
						status,
						String.format(Language.tr("Surface.PopupMenu.QuickFix.Teleport.DestinationName"),name),
						fix->teleport.setDestination(name)
						));
			}
		}
		return options;
	}

	/**
	 * Korrektur für "Der Parkplatz besitzt eine negative Kapazität."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#PARKING_NEGATIVE_CAPACITY
	 */
	public static List<RunModelFixer> setParkingCapacity(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementTransportParking) {
			final ModelElementTransportParking parking=(ModelElementTransportParking)element;
			final int value=1;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Transporter.Parking"),value),
					fix->parking.setWaitingCapacity(value)
					));
		}
		return options;
	}


	/**
	 * Korrektur für "Ein Transporter-Startpunkt besitzt eine negative Parkplatzkapazität."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#TRANSPORTER_SOURCE_NEGATIVE_PARKING_CAPACITY
	 */
	public static List<RunModelFixer> setTransporterParkingCapacity(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementTransportTransporterSource) {
			final ModelElementTransportTransporterSource tranporterSource=(ModelElementTransportTransporterSource)element;
			final int value=0;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Transporter.Parking"),value),
					fix->tranporterSource.setWaitingCapacity(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Ein Transporter-Startpunkt besitzt nicht-positive Mindestanzahl an wartenden Kunden um einen Transporter anzufordern."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#TRANSPORTER_SOURCE_MIN_REQUEST_NUMBER_LESS_OR_EQUAL_0
	 */
	public static List<RunModelFixer> setTransporterMinRequest(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementTransportTransporterSource) {
			final ModelElementTransportTransporterSource tranporterSource=(ModelElementTransportTransporterSource)element;
			final int value=1;
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Transporter.MinRequest"),value),
					fix->tranporterSource.setRequestMinWaiting(value)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Ungültige Startzeile für die DDE-Ausgabe."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#DDE_OUTPUT_INVALID_ROW
	 */
	public static List<RunModelFixer> setDDEOutputRow(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementOutputDDE) {
			final ModelElementOutputDDE output=(ModelElementOutputDDE)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.OutputDDE.Row"),
					fix->output.setStartRow(1)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Ungültige Spalte für die DDE-Ausgabe."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#DDE_OUTPUT_INVALID_COL
	 */
	public static List<RunModelFixer> setDDEOutputCol(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementOutputDDE) {
			final ModelElementOutputDDE output=(ModelElementOutputDDE)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.OutputDDE.Col"),
					fix->output.setColumn("A")
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurde an einer Bedienstation eine Wartezeittoleranz angegeben, aber es wurde keine auslaufende Kante für die Warteabbrecher angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#PROCESS_CANCELATION_TIME_BUT_NO_EDGE
	 */
	public static List<RunModelFixer> processTurnOffCancelTimes(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.Process.DeactivateLimitedWaitingTimeTolerances"),
					fix->process.getCancel().set(null)
					));
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurden keine notwendigen Ressourcen an der Bedienstation angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#PROCESS_NO_RESOURCE
	 */
	public static List<RunModelFixer> processResources(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)element;
			final ModelResources resources=process.getSurface().getResources();

			/* Neue Gruppe anlegen */
			final String newGroup=resources.getNextAvailableResouceName();
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Process.Resource.New"),newGroup),
					fix->{
						resources.add(new ModelResource(newGroup,1));
						process.getNeededResources().get(0).put(newGroup,1);
					}
					));

			/* Bestehende Gruppe verwenden */
			for (ModelResource resource: resources.getResources()) {
				options.add(new RunModelFixer(
						element,
						status,
						String.format(Language.tr("Surface.PopupMenu.QuickFix.Process.Resource.Use"),resource.getName()),
						fix->process.getNeededResources().get(0).put(resource.getName(),1)
						));
			}
		}
		return options;
	}

	/**
	 * Korrektur für "Es wurden an der Bedienstation Batch-Bedienungen und Rüstzeiten gleichzeitig verwendet, was nicht zulässig ist."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#PROCESS_MIX_BATCH_AND_SETUP
	 */
	public static List<RunModelFixer> processBatchSetup(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)element;
			options.add(new RunModelFixer(
					element,
					status,
					Language.tr("Surface.PopupMenu.QuickFix.Process.SetupOff"),
					fix->process.getSetupTimes().clear()
					));
		}
		return options;
	}

	/**
	 * Korrektur für "An einer Bedienstation wurde eine minimale Bedien-Batchgröße von weniger als 1 angegeben."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#PROCESS_MIN_BATCH_LOWER_THAN_1
	 */
	public static List<RunModelFixer> processMinBatchSize(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)element;
			for (int i=1;i<=5;i++) {
				final int size=i;
				options.add(new RunModelFixer(
						element,
						status,
						String.format(Language.tr("Surface.PopupMenu.QuickFix.Process.MinBatchSize"),size),
						fix->process.setBatchMinimum(size)
						));
			}
		}
		return options;
	}

	/**
	 * Korrektur für "An einer Bedienstation wurde eine maximale Bedien-Batchgröße angegeben, die geringer ist als die minimale Bedien-Batchgröße."
	 * @param element	Element an dem der Fehler aufgetreten ist
	 * @param status	Zugehöriger Status für diesen Fehler
	 * @return	Liste der möglichen Schnellkorrekturen
	 * @see RunModelCreatorStatus.Status#PROCESS_MAX_BATCH_LOWER_THAN_MIN
	 */
	public static List<RunModelFixer> processMaxBatchSize(final ModelElementPosition element, final RunModelCreatorStatus status) {
		final List<RunModelFixer> options=new ArrayList<>();
		if (element instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)element;
			final int size=process.getBatchMinimum();
			options.add(new RunModelFixer(
					element,
					status,
					String.format(Language.tr("Surface.PopupMenu.QuickFix.Process.MaxBatchSize"),size),
					fix->process.setBatchMaximum(size)
					));
		}
		return options;
	}
}
