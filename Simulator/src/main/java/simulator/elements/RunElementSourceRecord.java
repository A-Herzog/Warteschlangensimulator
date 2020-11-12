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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simcore.SimData;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.events.SystemArrivalEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.modeleditor.ModelSchedule;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSetRecord;
import ui.modeleditor.elements.ModelElementSignalTrigger;
import ui.modeleditor.elements.ModelElementSourceRecord;

/**
 * Dieses Element stellt die Laufzeit-Repräsentanz eines
 * {@link ModelElementSourceRecord}-Datensatzes dar.
 * Der Datensatz entspricht einer Thread-übergreifenden
 * Station, nicht einem thread-lokalen Datenobjekt.
 * @author Alexander Herzog
 * @see ModelElementSourceRecord
 */
public class RunElementSourceRecord {
	/** Name des Kundentyps */
	public String clientTypeName;
	/** Nummer des Kundentyps, siehe {@link RunModel#clientTypes} */
	public int clientType;

	/** Multiplikationsfaktor um bei den Zwischenankunftszeiten auf Sekunden zu kommen */
	private double timeBaseMultiply;

	/** Verteilung der Zwischenankunftszeiten */
	private AbstractRealDistribution distribution;

	/** Rechenausdruck zur Bestimmung der Zwischenankunftszeiten */
	public String expression;

	/** Zeitplans gemäß dessen die Zwischenankunftszeiten bestimmt werden sollen */
	public ModelSchedule schedule;

	/** Bedingung gemäß derer weitere Kundenankünfte ausgelöst werden sollen */
	public String condition;
	/** Mindestabstand zwischen Kundenankünften (in Millisekunden), die über eine Bedingung ausgelöst werden sollen */
	public long conditionMinDistanceMS;

	/** Für eine Kundenankunft zu prüfender Schwellenwertausdruck */
	public String thresholdExpression;
	/** Schwellenwert gegen den der Schwellenwertausdruck abgeglichen werden soll */
	public double thresholdValue;
	/** Soll eine Kundenankunft beim Überschreiten (<code>true</code>) oder beim Unterschreiten (<code>false</code>) des Schwellenwertes ausgelöst werden? */
	public boolean thresholdDirectionUp;

	/** Signale, die Ankünfte auslösen können solle */
	private String[] signals;

	/** Ankunfts-Batch-Größe oder <code>null</code>, wenn es mehrere verschiedene Batch-Größen geben soll */
	public String batchSize;
	/** Kumulative Wahrscheinlichkeiten für die einzelnen Batch-Größen */
	private double[] batchSizesPSums;

	/** Gesamtanzahl an Ankunftsereignissen (-1 für unendlich viele) */
	public long maxArrivalCount;
	/** Gesamtanzahl an Kundenankünften (-1 für unendlich viele) */
	public long maxArrivalClientCount;

	/** Zeitpunkt (in Millisekunden) am dem die erste Zwischenankunftszeit beginnt */
	private long arrivalStartMS;

	/** Kundendatenfelder-Indices für die Zuweisung von Werten (Zahlen) an neue Kunden **/
	private int[] variableIndex;
	/** Rechenausdrücke deren Ergebnisse an die in {@link #variableIndex} adressierten Kundendatenfelder zugewiesen werden sollen */
	private String[] expressions;

	/** Kundentextdatenfelder-Schlüssel für die Zuweisungen (Texte) an neue Kunden */
	private String[] stringKeys;
	/** Werte die an die in {@link #stringKeys} adressierten Kundentextdatenfelder-Schlüssel zugewiesen werden sollen */
	private String[] stringValues;

	/** Optionaler, zusätzlicher Index dieses Datensatzes (z.B. für Mehrfachquellen) */
	private int index;

	/**
	 * Überträgt die Daten aus einem {@link ModelElementSourceRecord}-Element in dieses.
	 * @param record	Auszulesender Quell-Datensatz
	 * @param name	Ist nicht {@link ModelElementSourceRecord#hasName()}, so kann hier ein Name für die Kundengruppe angegeben werden.
	 * @param id	ID der Station an der die kunden eintreffen sollen
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param index	Optionaler, zusätzlicher Index dieses Datensatzes (z.B. für Mehrfachquellen)
	 * @return	Liefert immer ein Objekt vom Typ {@link RunModelCreatorStatus}. Dies kann auch Erfolg darstellen.
	 */
	public RunModelCreatorStatus load(final ModelElementSourceRecord record, String name, final int id, final EditModel editModel, final RunModel runModel, int index) {
		this.index=index;

		if (record.hasName()) {
			name=record.getName();
			if (name==null || name.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceNoName"),id),RunModelCreatorStatus.Status.NO_SOURCE_RECORD_NAME);
		} else {
			if (name==null || name.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceNoName"),id),RunModelCreatorStatus.Status.NO_SOURCE_RECORD_NAME);
		}
		clientType=runModel.getClientTypeNr(name);
		clientTypeName=name;

		timeBaseMultiply=record.getTimeBase().multiply;

		int error;
		double arrivalStart;
		if (record.hasOwnArrivals()) switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
			distribution=DistributionTools.cloneDistribution(record.getInterarrivalTimeDistribution());
			arrivalStart=record.getArrivalStart();
			if (arrivalStart<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceArrivalStart"),NumberTools.formatNumber(arrivalStart),id),RunModelCreatorStatus.Status.NEGATIVE_ARRIVAL_START_TIME);
			arrivalStartMS=FastMath.round(arrivalStart*timeBaseMultiply*1000);
			break;
		case NEXT_EXPRESSION:
			final String expression=record.getInterarrivalTimeExpression();
			error=ExpressionCalc.check(expression,runModel.variableNames);
			if (error>=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceExpression"),expression,id,error+1));
			this.expression=expression;
			arrivalStart=record.getArrivalStart();
			if (arrivalStart<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceArrivalStart"),NumberTools.formatNumber(arrivalStart),id),RunModelCreatorStatus.Status.NEGATIVE_ARRIVAL_START_TIME);
			arrivalStartMS=FastMath.round(arrivalStart*timeBaseMultiply*1000);
			break;
		case NEXT_SCHEDULE:
			ModelSchedule schedule=editModel.schedules.getSchedule(record.getInterarrivalTimeSchedule());
			if (schedule==null) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceSchedule"),record.getInterarrivalTimeSchedule(),id));
			this.schedule=schedule;
			arrivalStartMS=0;
			break;
		case NEXT_CONDITION:
			final String condition=record.getArrivalCondition();
			error=ExpressionMultiEval.check(condition,runModel.variableNames);
			if (error>=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceCondition"),condition,id,error+1));
			this.condition=condition;
			if (record.getArrivalConditionMinDistance()<=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceConditionMinDistance"),NumberTools.formatNumber(record.getArrivalConditionMinDistance()),id));
			this.conditionMinDistanceMS=FastMath.round(record.getArrivalConditionMinDistance()*1000);
			arrivalStart=record.getArrivalStart();
			if (arrivalStart<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceArrivalStart"),NumberTools.formatNumber(arrivalStart),id),RunModelCreatorStatus.Status.NEGATIVE_ARRIVAL_START_TIME);
			arrivalStartMS=FastMath.round(arrivalStart*timeBaseMultiply*1000);
			break;
		case NEXT_THRESHOLD:
			final String thresholdExpression=record.getThresholdExpression();
			error=ExpressionCalc.check(thresholdExpression,runModel.variableNames);
			if (error>=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceThresholdExpression"),thresholdExpression,id,error+1));
			this.thresholdExpression=thresholdExpression;
			this.thresholdValue=record.getThresholdValue();
			this.thresholdDirectionUp=record.isThresholdDirectionUp();
			break;
		case NEXT_SIGNAL:
			final List<Integer> signals=new ArrayList<>();
			final List<String> signalNames=new ArrayList<>();
			for (String signal: record.getArrivalSignalNames()) {
				final int signalId=getSignalByName(editModel.surface,signal);
				if (signalId<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SignalUnknown"),id,signal));
				if (!signals.contains(signalId)) {
					signals.add(signalId);
					signalNames.add(signal);
				}
			}
			if (signals.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoSignalForBarrier"),id));
			this.signals=signalNames.toArray(new String[0]);
			break;
		}

		batchSize=record.getBatchSize();
		if (batchSize==null) {
			final double[] rates=record.getMultiBatchSize();
			double sum=0; for (double d: rates) sum+=d;
			if (sum==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceBatchRatesZero"),id));
			batchSizesPSums=new double[rates.length];
			for (int i=0;i<rates.length;i++) {
				batchSizesPSums[i]=rates[i]/sum;
				if (i>0) batchSizesPSums[i]+=batchSizesPSums[i-1];
			}
		} else {
			if (!batchSize.equals("1")) {
				final ExpressionCalc batchSizeTest=new ExpressionCalc(runModel.variableNames);
				final int batchError=batchSizeTest.parse(batchSize);
				if (batchError>=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceInvalidBatchSize"),id,batchSize,batchError+1));
			}
		}
		maxArrivalCount=record.getMaxArrivalCount();
		maxArrivalClientCount=record.getMaxArrivalClientCount();

		/* Zuweisungen (Zahlen) */
		final String[] variables=record.getSetRecord().getVariables();
		final String[] expressions=record.getSetRecord().getExpressions();
		final int size=FastMath.min(variables.length,expressions.length);
		variableIndex=new int[size];
		this.expressions=new String[size];

		for (int i=0;i<size;i++) {
			final int clientDataIndex=CalcSymbolClientUserData.testClientData(variables[i]);
			if (clientDataIndex>=0) {
				/* Kundendatenfeld */
				variableIndex[i]=-1-clientDataIndex;
			} else {
				/* Variablen */
				index=-1;
				for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equalsIgnoreCase(variables[i])) {index=j; break;}
				if (index<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SetInternalError"),id));
				variableIndex[i]=index;
			}
			/* Ausdrücke */
			if (!expressions[i].equals(ModelElementSetRecord.SPECIAL_WAITING) && !expressions[i].equals(ModelElementSetRecord.SPECIAL_TRANSFER) && !expressions[i].equals(ModelElementSetRecord.SPECIAL_PROCESS) && !expressions[i].equals(ModelElementSetRecord.SPECIAL_RESIDENCE)) {
				error=ExpressionCalc.check(expressions[i],runModel.variableNames);
				if (error>=0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SetInvalidExpression"),i+1,id,error+1));
			}
			this.expressions[i]=expressions[i];
		}

		/* Zuweisungen (Texte) */
		final List<String> keys=new ArrayList<>();
		final List<String> values=new ArrayList<>();
		for (int i=0;i<Math.min(record.getStringRecord().getKeys().size(),record.getStringRecord().getValues().size());i++) {
			final String key=record.getStringRecord().getKeys().get(i);
			final String value=record.getStringRecord().getValues().get(i);
			if (key==null || key.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoAssignKey"),id));
			keys.add(key);
			values.add(value);
		}
		stringKeys=keys.toArray(new String[0]);
		stringValues=values.toArray(new String[0]);

		return RunModelCreatorStatus.ok;
	}

	/**
	 * Liefert die ID einer Signal-Station
	 * @param surface	Zeichenfläche die durchsucht werden soll
	 * @param name	Name des Signals
	 * @return	Liefert im Erfolgsfall die ID der Signalstation, sonst -1
	 */
	private int getSignalByName(final ModelSurface surface, final String name) {
		for (ModelElementSignalTrigger element: surface.getAllSignals(false)) {
			if (element.getSignalNames()!=null) for (String signal: element.getSignalNames()) {
				if (signal!=null && signal.equals(name)) return ((ModelElement)element).getId();
			}
		}
		return -1;
	}

	/**
	 * Prüft einen {@link ModelElementSourceRecord}-Datensatz vorab.<br>
	 * Kann in den Test-Methoden von Stationen verwendet werden.
	 * @param record	zu prüfender Datensatz
	 * @param name	Ist nicht {@link ModelElementSourceRecord#hasName()}, so kann hier ein Name für die Kundengruppe angegeben werden.
	 * @param id	ID der Station an der die kunden eintreffen sollen
	 * @return	Liefert immer ein Objekt vom Typ {@link RunModelCreatorStatus}. Dies kann auch Erfolg darstellen.
	 */
	public static RunModelCreatorStatus test(final ModelElementSourceRecord record, String name, final int id) {
		if (record.hasName()) {
			if (record.getName()==null || record.getName().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceNoName"),id),RunModelCreatorStatus.Status.NO_SOURCE_RECORD_NAME);
		} else {
			if (name==null || name.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceNoName"),id),RunModelCreatorStatus.Status.NO_SOURCE_RECORD_NAME);
		}

		double arrivalStart;
		if (record.hasOwnArrivals()) switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
		case NEXT_EXPRESSION:
			arrivalStart=record.getArrivalStart();
			if (arrivalStart<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceArrivalStart"),NumberTools.formatNumber(arrivalStart),id),RunModelCreatorStatus.Status.NEGATIVE_ARRIVAL_START_TIME);
			break;
		case NEXT_SIGNAL:
			if (record.getArrivalSignalNames().size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoSignalForBarrier"),id));
			break;
		case NEXT_CONDITION:
			arrivalStart=record.getArrivalStart();
			if (arrivalStart<0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceArrivalStart"),NumberTools.formatNumber(arrivalStart),id),RunModelCreatorStatus.Status.NEGATIVE_ARRIVAL_START_TIME);
			break;
		case NEXT_THRESHOLD:
			/* Nichts zu testen. */
			break;
		case NEXT_SCHEDULE:
			/* Nichts zu testen. */
			break;
		}

		final String batchSize=record.getBatchSize();
		if (batchSize==null) {
			final double[] rates=record.getMultiBatchSize();
			double sum=0; for (double d: rates) sum+=d;
			if (sum==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceBatchRatesZero"),id));
		} else {
			if (batchSize.trim().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.SourceInvalidBatchSize"),id,batchSize,0));
		}

		return RunModelCreatorStatus.ok;
	}

	/**
	 * Liefert die Batch-Größe für einen Ankunfs-Batch als Zufallswert gemäß einer Verteilung.
	 * @param simData	Simulationsdatenobjekt
	 * @return	Ankunfts-Batch-Größe
	 */
	public int getMultiBatchSize(final SimulationData simData) {
		final double p=DistributionRandomNumber.nextDouble();
		for (int i=0;i<batchSizesPSums.length;i++) if (batchSizesPSums[i]>=p) return i+1;
		return batchSizesPSums.length-1;
	}

	/**
	 * Erfasst die Ausführung eines Ereignisses.
	 * @param simData	Simulationsdatenobjekt
	 * @param event	Name des Ereignisses
	 * @param info	Zusätzliche Daten zu dem Ereignis
	 * @param element	Station an dem das Ereignis aufgetreten ist
	 */
	private void log(final SimulationData simData, final String event, final String info, final RunElement element) {
		if (simData.loggingIDs!=null && !simData.loggingIDs[element.id]) return;
		if (!simData.logInfoStation) return;
		simData.logEventExecution(element.logTextColor,event,element.id,info);
	}

	/**
	 * Plant die Zeit-gesteuerte nächste Kundenankunft (bzw. Batch-Ankunft) ein
	 * @param simData	Simulationsdatenobjekt
	 * @param rawTimeDelta	Zeitabstand direkt basierend auf Basis der Verteilung
	 * @param isFirstArrival	Gibt an, ob es sich um die erste Ankunft durch dieses Element handelt
	 * @param element	Referenz auf das Source- oder SourceMulti-Element
	 * @param stationName	Name der Station
	 * @return	Gibt die Anzahl an erzeugten Kundenankünften zurück
	 */

	private int scheduleNextArrivalTime(final SimulationData simData, final double rawTimeDelta, final boolean isFirstArrival, final RunElement element, final String stationName) {
		/* Zwischenankunftszeit */
		long timeMS=FastMath.round(1000*rawTimeDelta*timeBaseMultiply);
		if (timeMS<0) timeMS=0;

		/* Bei erster Ankunft optionale Startzeit addieren */
		if (isFirstArrival) timeMS+=arrivalStartMS;

		/* Ankunfts-Event-Objekt holen */
		final SystemArrivalEvent nextArrival=(SystemArrivalEvent)simData.getEvent(SystemArrivalEvent.class);

		/* Jetzt+Zwischenankunftszeit als Ausführungszeitpunkt festlegen */
		nextArrival.init(simData.currentTime+timeMS);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Source"),String.format(Language.tr("Simulation.Log.Source.Info"),SimData.formatSimTime(simData.currentTime+timeMS),simData.runModel.clientTypes[clientType],stationName),element);

		/* Konfiguration in Element eintragen */
		nextArrival.source=element;
		nextArrival.scheduleNext=true;
		nextArrival.index=index;

		/* Zur Ereignisliste hinzufügen */
		simData.eventManager.addEvent(nextArrival);

		/* Ankunft zählen */
		return 1;
	}

	/**
	 * Plant die Zeitplan-gesteuerte nächste Kundenankunft (bzw. Batch-Ankunft) ein
	 * @param simData	Simulationsdatenobjekt
	 * @param element	Referenz auf das Source- oder SourceMulti-Element
	 * @param stationName	Name der Station
	 * @return	Gibt die Anzahl an erzeugten Kundenankünften zurück
	 */
	private int scheduleArrivalBySchedule(final SimulationData simData, final RunElement element, final String stationName) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return 0;

		/* Nächstes zu betrachtendes Intervall finden */
		long duration=schedule.getDurationPerSlot();
		long nextSlotNr;
		if (simData.currentTime==0) {
			nextSlotNr=0;
		} else {
			long current=simData.currentTime/1000;
			nextSlotNr=current/duration;
			if (current%duration!=0) nextSlotNr++;
		}

		int arrivalCount=0;

		/* Nächstes Intervall mit Ankünften finden */
		int abort=100000;
		while (abort>0) {
			int count=schedule.getValueBySlotNumber(nextSlotNr);
			if (count<0) return 0; /* keine Kunden jemals mehr */
			if (count>0) {
				for (int i=0;i<count;i++) {
					/* Ankunft zählen */
					arrivalCount++;

					/* Ankunftszeitpunkt */
					long timeMS=FastMath.round((nextSlotNr*duration+duration*DistributionRandomNumber.nextDouble())*1000);

					/* Ankunfts-Event-Objekt holen */
					final SystemArrivalEvent nextArrival=(SystemArrivalEvent)simData.getEvent(SystemArrivalEvent.class);

					/* Ausführungszeitpunkt festlegen */
					nextArrival.init(timeMS);

					/* Logging */
					if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Source"),String.format(Language.tr("Simulation.Log.Source.Info"),SimData.formatSimTime(timeMS),simData.runModel.clientTypes[clientType],stationName),element);

					/* Konfiguration in Element eintragen */
					nextArrival.source=element;
					nextArrival.scheduleNext=(i==0);
					nextArrival.index=index;

					/* Zur Ereignisliste hinzufügen */
					simData.eventManager.addEvent(nextArrival);
				}
				return arrivalCount;
			}
			nextSlotNr++;
			abort--; /* 100000 Intervalle ohne Ankunft - da passiert nichts mehr */
		}

		return arrivalCount;
	}

	/**
	 * Plant die Bedingungs-gesteuerte nächste Kundenankunft (bzw. Batch-Ankunft) ein
	 * @param simData	Simulationsdatenobjekt
	 * @param element	Referenz auf das Source- oder SourceMulti-Element
	 * @param condition	Zu prüfende Bedingung (nur wenn diese erfüllt ist, wird eine Ankunft eingeplant)
	 * @param lastArrival	Letzter Ankunftszeitpunkt (kann -1 sein, wenn noch keine Ankünfte vorhanden sind)
	 * @param stationName	Name der Station
	 * @param arrivalCount	Anzahl an bislang erzeugten Ankünften (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @param arrivalClientCount	Anzahl an bislang erzeugten Kunden  (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @return	Gibt die Anzahl an erzeugten Kundenankünften zurück
	 */
	private int scheduleArrivalByCondition(final SimulationData simData, final RunElement element, final ExpressionMultiEval condition, final long lastArrival, final String stationName, final long arrivalCount, final long arrivalClientCount) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return 0;

		/* Maximalanzahl an Ankünften schon erreicht? */
		if (maxArrivalCount>=0 && arrivalCount>=maxArrivalCount) return 0;

		/* Maximalanzahl an eingetroffenen Kunden schon erreicht? */
		if (maxArrivalClientCount>=0 && arrivalClientCount>=maxArrivalClientCount) return 0;

		simData.runData.setClientVariableValues(null);
		if (condition.eval(simData.runData.variableValues,simData,null) && (lastArrival<0 || simData.currentTime>=lastArrival+conditionMinDistanceMS)) {

			/* Ankunfts-Event-Objekt holen */
			final SystemArrivalEvent nextArrival=(SystemArrivalEvent)simData.getEvent(SystemArrivalEvent.class);

			/* Jetzt als Ausführungszeitpunkt festlegen */
			nextArrival.init(simData.currentTime);

			/* Logging */
			if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Source"),String.format(Language.tr("Simulation.Log.Source.Info"),SimData.formatSimTime(simData.currentTime),simData.runModel.clientTypes[clientType],stationName),element);

			/* Konfiguration in Element eintragen */
			nextArrival.source=element;
			nextArrival.scheduleNext=true;
			nextArrival.index=index;

			/* Zur Ereignisliste hinzufügen */
			simData.eventManager.addEvent(nextArrival);

			/* Ankunft zählen */
			return 1;
		}
		return 0;
	}

	/**
	 * Plant die nächste Kundenankunft (bzw. Batch-Ankunft) ein
	 * @param simData	Simulationsdatenobjekt
	 * @param isFirstArrival	Gibt an, ob es sich um die erste Ankunft durch dieses Element handelt
	 * @param expression	Optional der Ausdruck auf dessen Basis die Zwischenankunftszeiten berechnet werden sollen
	 * @param condition	Optional die Bedingung auf deren Basis Kunden freigegeben werden
	 * @param lastArrival	Letzter Ankunftszeitpunkt (kann -1 sein, wenn noch keine Ankünfte vorhanden sind)
	 * @param element	Referenz auf das Source- oder SourceMulti-Element
	 * @param stationName	Name der Station
	 * @param arrivalCount	Anzahl an bislang erzeugten Ankünften (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @param arrivalClientCount	Anzahl an bislang erzeugten Kunden  (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @return	Gibt die Anzahl an erzeugten Kundenankünften zurück
	 */
	public int scheduleNextArrival(final SimulationData simData, final boolean isFirstArrival, final ExpressionCalc expression, final ExpressionMultiEval condition, final long lastArrival, final RunElement element, final String stationName, final long arrivalCount, final long arrivalClientCount) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return 0;

		if (distribution!=null) {
			/* Zufällige Zwischenankunftszeit bestimmen */
			double rawTimeDelta=DistributionRandomNumber.randomNonNegative(distribution);
			return scheduleNextArrivalTime(simData,rawTimeDelta,isFirstArrival,element,stationName);
		}

		if (expression!=null) {
			simData.runData.setClientVariableValues(null);
			double rawTimeDelta;
			try {
				rawTimeDelta=expression.calc(simData.runData.variableValues,simData,null);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(expression,stationName);
				rawTimeDelta=0;
			}
			return scheduleNextArrivalTime(simData,rawTimeDelta,isFirstArrival,element,stationName);
		}

		if (schedule!=null) {
			return scheduleArrivalBySchedule(simData,element,stationName);
		}

		if (condition!=null) {
			return scheduleArrivalByCondition(simData,element,condition,lastArrival,stationName,arrivalCount,arrivalClientCount);
		}

		if (thresholdExpression!=null) {
			return 0;
		}

		if (signals!=null) {
			return 0;
		}

		return 0;
	}

	/**
	 * Prüft, ob ein Signal eine Kundenankunft auslösen soll und löst diese ggf. auch gleich aus
	 * @param simData	Simulationsdatenobjekt
	 * @param signalName	Name des Signals bei dem geprüft werden soll, ob dadurch Kundenankünfte ausgelöst werden
	 * @param element	Referenz auf das Source- oder SourceMulti-Element
	 * @param arrivalCount	Anzahl an bislang erzeugten Ankünften (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @param arrivalClientCount	Anzahl an bislang erzeugten Kunden  (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @return	Gibt die Anzahl an erzeugten Kundenankünften zurück
	 */
	public int triggeredBySignal(final SimulationData simData, final String signalName, final RunElement element, final long arrivalCount, final long arrivalClientCount) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return 0;

		/* Maximalanzahl an Ankünften schon erreicht? */
		if (maxArrivalCount>=0 && arrivalCount>=maxArrivalCount) return 0;

		/* Maximalanzahl an eingetroffenen Kunden schon erreicht? */
		if (maxArrivalClientCount>=0 && arrivalClientCount>=maxArrivalClientCount) return 0;

		if (signals==null) return 0;
		for (String name: signals) if (name.equals(signalName)) {
			return scheduleNextArrivalTime(simData,0,false,element,element.name);
		}
		return 0;
	}

	/**
	 * Löst eine durch eine Schwellenwert-Überquerung ausgelöste Kundenankunft aus
	 * @param simData	Simulationsdatenobjekt
	 * @param element	Referenz auf das Source- oder SourceMulti-Element
	 * @param arrivalCount	Anzahl an bislang erzeugten Ankünften (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @param arrivalClientCount	Anzahl an bislang erzeugten Kunden  (um ggf. bei einer eingestellten Maximalanzahl keine weiteren Ankünfte mehr zu generieren)
	 * @return	Gibt die Anzahl an erzeugten Kundenankünften zurück
	 */
	public int triggertByThreshold(final SimulationData simData, final RunElement element, final long arrivalCount, final long arrivalClientCount) {
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return 0;

		/* Maximalanzahl an Ankünften schon erreicht? */
		if (maxArrivalCount>=0 && arrivalCount>=maxArrivalCount) return 0;

		/* Maximalanzahl an eingetroffenen Kunden schon erreicht? */
		if (maxArrivalClientCount>=0 && arrivalClientCount>=maxArrivalClientCount) return 0;

		return scheduleNextArrivalTime(simData,0,false,element,element.name);
	}

	/**
	 * Trägt die Text-Zuweisungen in ein neues Kundendatenobjekt ein
	 * @param client	Kundendatenobjekt in das die Texte eingetragen werden sollen
	 */
	public void writeStringsToClient(final RunDataClient client) {
		for (int i=0;i<stringKeys.length;i++) client.setUserDataString(stringKeys[i],stringValues[i]);
	}

	/**
	 * Erzeugt ein Thread-lokales Objekt welches die Zuweisungen enthält, die neue Kunden erhalten sollen
	 * @param variableNames	Liste der verfügbaren Variablen
	 * @return	Thread-lokales Objekt mit Zuweisungsobjekten
	 * @see RunElementSourceRecord.SourceSetExpressions
	 * @see RunElementSourceRecord.SourceSetExpressions#writeNumbersToClient(SimulationData, RunDataClient, String)
	 */
	public SourceSetExpressions getRuntimeExpressions(final String[] variableNames) {
		final SourceSetExpressions data=new SourceSetExpressions();

		data.variableIndex=variableIndex;
		data.expressions=new ExpressionCalc[expressions.length];
		data.mode=new SourceSetExpressions.SetMode[expressions.length];
		for (int i=0;i<expressions.length;i++) {
			data.mode[i]=SourceSetExpressions.SetMode.MODE_EXPRESSION;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_WAITING)) data.mode[i]=SourceSetExpressions.SetMode.MODE_WAITING_TIME;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_TRANSFER)) data.mode[i]=SourceSetExpressions.SetMode.MODE_TRANSFER_TIME;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_PROCESS)) data.mode[i]=SourceSetExpressions.SetMode.MODE_PROCESS_TIME;
			if (expressions[i].equals(ModelElementSetRecord.SPECIAL_RESIDENCE)) data.mode[i]=SourceSetExpressions.SetMode.MODE_RESIDENCE_TIME;
			if (data.mode[i]==SourceSetExpressions.SetMode.MODE_EXPRESSION) {
				data.expressions[i]=new ExpressionCalc(variableNames);
				data.expressions[i].parse(expressions[i]);
			}
		}

		return data;
	}

	/** Umrechnungsfaktor von Millisekunden auf Sekunden, um die Division während der Simulation zu vermeiden */
	private static final double toSec=1.0/1000.0;

	/**
	 * Thread-lokales Objekt mit Zuweisungsobjekten für neue Kunden
	 * @author Alexander Herzog
	 * @see RunElementSourceRecord#getRuntimeExpressions(String[])
	 */
	public static class SourceSetExpressions {
		/**
		 * Art der Datenquelle für die Zuweisung
		 */
		private enum SetMode {
			/** Rechenausdruck auswerten und zuweisen */
			MODE_EXPRESSION,
			/** Bisherige Wartezeit zuweisen */
			MODE_WAITING_TIME,
			/** Bisherige Transferzeit zuweisen */
			MODE_TRANSFER_TIME,
			/** Bisherige Bedienzeit zuweisen */
			MODE_PROCESS_TIME,
			/** Bisherige Verweilzeit zuweisen */
			MODE_RESIDENCE_TIME
		}

		/** Index des Ziel-Kundendatenfeldes für die Zuweisung  */
		private int[] variableIndex;
		/** Auszuwertendes Ausdruck für die Quelldaten im Modus {@link SetMode#MODE_EXPRESSION} */
		private ExpressionCalc[] expressions;
		/** Art der Datenquelle für die Zuweisung */
		private SetMode[] mode;

		/**
		 * Schreibt die Zuweisungen in ein neues Kundendatenobjekt
		 * @param simData	Simulationsdatenobjekt
		 * @param client	Kundenobjekt in das die Daten geschrieben werden
		 * @param stationName	Name der Station
		 */
		public void writeNumbersToClient(final SimulationData simData, final RunDataClient client, final String stationName) {
			for (int i=0;i<variableIndex.length;i++) {

				/* Zuweisungen durchführen */
				double d=0.0;
				boolean ok=true;

				switch (mode[i]) {
				case MODE_EXPRESSION:
					simData.runData.setClientVariableValues(client);
					try {
						d=expressions[i].calc(simData.runData.variableValues,simData,client);
					} catch (MathCalcError e) {
						simData.calculationErrorStation(expressions[i],stationName);
						ok=false;
					}
					break;
				case MODE_WAITING_TIME:
					d=client.waitingTime*toSec;
					break;
				case MODE_TRANSFER_TIME:
					d=client.transferTime*toSec;
					break;
				case MODE_PROCESS_TIME:
					d=client.processTime*toSec;
					break;
				case MODE_RESIDENCE_TIME:
					d=client.residenceTime*toSec;
					break;
				}

				if (!ok) continue;

				/* Speichern */
				final int len=simData.runData.variableValues.length;
				final int index=variableIndex[i];

				if (index<0) {
					/* Speichern als Kundendaten-Feld */
					final int clientDataIndex=-(index+1);
					client.setUserData(clientDataIndex,d);
				} else {
					/* Speichern in Variable */
					boolean done=false;
					if (index==len-3) {
						/* Pseudovariable: Wartezeit */
						client.waitingTime=FastMath.max(0,FastMath.round(d*1000));
						done=true;
					}
					if (index==len-2) {
						/* Pseudovariable: Transferzeit */
						client.transferTime=FastMath.max(0,FastMath.round(d*1000));
						done=true;
					}
					if (index==len-1) {
						/* Pseudovariable: Bedienzeit */
						client.processTime=FastMath.max(0,FastMath.round(d*1000));
						done=true;
					}
					if (!done) {
						/* Reguläre Variable speichern */
						simData.runData.variableValues[index]=d;
					}
				}
			}
		}
	}
}