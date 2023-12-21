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
package simulator.runmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.elements.TransporterPosition;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsTimePerformanceIndicator;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.ModelTransporterFailure;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;

/**
 * Diese Klasse hält die Laufzeitdaten für alle Transporter vor.
 * @author Alexander Herzog
 * @see RunDataTransporter
 */
public final class RunDataTransporters implements Cloneable {
	/** Liste aller globalen Variablen in dem Modell */
	private String[] variables;

	/**
	 * Zuordnung von Namen zu Index-Werten der Transporter
	 * @see RunDataTransporter#type
	 */
	public String[] type;

	/**
	 * Formeln zur Umrechnung von Entfernungs-Matrix-Einträgen zu Fahrtzeiten
	 */
	private String[] expressionString;

	/**
	 * Formelobjekte zur Umrechnung von Entfernungs-Matrix-Einträgen zu Fahrtzeiten
	 * @see #expressionString
	 */
	private ExpressionCalc[] expression;

	/**
	 * Entfernungs-Matrixen (einzelne Teil-Arrays können <code>null</code> sein)
	 */
	private double[][][] distances;

	/**
	 * Liste der Variablen - ergänzt um die Distanz-Variable
	 */
	private String[] variableNamesWithDistance;

	/**
	 * Liste aller Transporter in allen Gruppen
	 */
	private RunDataTransporter[][] transporters;

	/**
	 * Konstruktor der Klasse
	 */
	public RunDataTransporters() {
		variables=new String[0];
	}

	/**
	 * Liefert den Index des Transportertyps in der Liste der Transportertypen
	 * @param name	Name des Tranportertyps
	 * @return	Index in der Liste oder -1, wenn es den Transportertyp nicht in der Liste gibt
	 * @see RunDataTransporters#type
	 */
	public int getTransporterIndex(final String name) {
		for (int i=0;i<type.length;i++) if (type[i].equalsIgnoreCase(name)) return i;
		return -1;
	}

	/**
	 * Liefert eine Liste der Stationen die als Zielpunkte für Transporterfahrten in Frage kommen
	 * @param surface	Zeichenfläche die durchsucht werden soll (auch Unter-Zeichenflächen werden berücksichtigt)
	 * @return	Zuordnung von Namen zu IDs der möglichen Zielpunkte von Transporterfahrten
	 */
	private Map<String,Integer> getDestinationMatrixStations(final ModelSurface surface) {
		final Map<String,Integer> map=new HashMap<>();
		for (ModelElement element: surface.getElements()) {
			final String name=element.getName();
			if (!name.isEmpty() && (element instanceof ModelElementTransportTransporterSource || element instanceof ModelElementTransportParking || element instanceof ModelElementTransportDestination)) map.put(name,element.getId());
			if (element instanceof ModelElementSub) {
				for (ModelElement element2: ((ModelElementSub)element).getSubSurface().getElements()) {
					final String name2=element2.getName();
					if (!name2.isEmpty() && (element2 instanceof ModelElementTransportTransporterSource || element2 instanceof ModelElementTransportParking || element2 instanceof ModelElementTransportDestination)) map.put(name2,element2.getId());
				}
			}
		}
		return map;
	}

	/**
	 * Lädt die Transporter-Daten aus der zugehörigen Editor-Klasse
	 * @param transporters	Editor-Klasse mit den Daten für die Transporter
	 * @param surface	Zeichenoberfläche (aus der die Namen der Stationen ausgelesen werden)
	 * @param variables	Liste der globalen Variablen
	 * @param runModel	Laufzeitmodell
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 * @see ModelTransporters
	 */
	public String loadFromEditTransporters(final ModelTransporters transporters, final ModelSurface surface, final String[] variables, final RunModel runModel) {
		this.variables=variables;

		/* Temporäre Listen anlegen */
		final List<String> typeList=new ArrayList<>();
		final List<double[][]> distancesList=new ArrayList<>();
		final List<String> expressionList=new ArrayList<>();
		final List<RunDataTransporter[]> transporterList=new ArrayList<>();

		/* Liste der Variablen - ergänzt um die Distanz-Variable */
		final List<String> variablesList=new ArrayList<>(Arrays.asList(variables));
		variablesList.add(ModelTransporter.DEFAULT_DISTANCE);
		variableNamesWithDistance=variablesList.toArray(new String[0]);

		/* Alle Transportertypen durchlaufen */
		for (ModelTransporter transporter: transporters.getTransporters()) {
			/* Name des Transportertyps */
			final String name=transporter.getName();
			if (typeList.contains(name)) return String.format(Language.tr("Simulation.Creator.Transporter.DoubleNameUsage"),name);
			typeList.add(name);
			final int typeIndex=typeList.size()-1;

			/* Kapazität der Transporter dieses Typs */
			final int capacity=transporter.getCapacity();
			if (capacity<=0) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidCapacity"),name,capacity);

			/* Lade- und Entladezeiten */
			final Object load=transporter.getLoadTime();
			final Object unload=transporter.getUnloadTime();
			if (load instanceof String) {
				final int error=ExpressionCalc.check((String)load,variables);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.TransporterLoadExpression"),load,error+1);
			}
			if (unload instanceof String) {
				final int error=ExpressionCalc.check((String)unload,variables);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.TransporterUnloadExpression"),load,error+1);
			}

			/* Icon für Animation */
			final String iconEastEmpty=transporter.getEastEmptyIcon();
			final String iconWestEmpty=transporter.getWestEmptyIcon();
			final String iconEastLoaded=transporter.getEastLoadedIcon();
			final String iconWestLoaded=transporter.getWestLoadedIcon();

			/* Ausdruck zur Umrechnung der Entfernung in eine Zeit */
			int error=ExpressionCalc.check(transporter.getExpression(),variableNamesWithDistance);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidExpression"),name,transporter.getExpression(),error+1);
			expressionList.add(transporter.getExpression());

			/* Entfernungsmatrix für Transportertyp */
			final int maxID=surface.getMaxId();
			double[][] distancesForType=null;
			final Map<String,Integer> stations=getDestinationMatrixStations(surface);
			for (Map.Entry<String,Integer> stationA: stations.entrySet()) {
				final int idA=stationA.getValue().intValue();
				final String nameA=stationA.getKey();
				for (Map.Entry<String,Integer> stationB: stations.entrySet()) {
					final int idB=stationB.getValue().intValue();
					final String nameB=stationB.getKey();
					if (idA==idB) continue;
					final double d=transporter.getDistance(nameA,nameB);
					if (d==0.0) continue;
					if (distancesForType==null) distancesForType=new double[maxID+1][];
					if (distancesForType[idA]==null) distancesForType[idA]=new double[maxID+1];
					distancesForType[idA][idB]=d;
				}
			}
			distancesList.add(distancesForType);

			/* Ausfälle */
			final List<RunDataTransporterFailure> failures=new ArrayList<>();
			for (ModelTransporterFailure editFailure : transporter.getFailures()) {
				final RunDataTransporterFailure runFailure=new RunDataTransporterFailure(this);
				runFailure.failureMode=editFailure.getFailureMode();
				runFailure.failureNumber=editFailure.getFailureNumber();
				runFailure.failureTime=FastMath.round(editFailure.getFailureTimeOrDistance()*runModel.scaleToSimTime);
				runFailure.failureDistance=editFailure.getFailureTimeOrDistance();
				runFailure.failureDistribution=editFailure.getFailureDistribution();
				if (runFailure.failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_DISTRIBUTION && runFailure.failureDistribution==null) return String.format(Language.tr("Simulation.Creator.MissingTransporterInterDownTimeDistribution"),name);
				if (runFailure.failureMode==ModelTransporterFailure.FailureMode.FAILURE_BY_EXPRESSION) {
					runFailure.failureExpression=new ExpressionCalc(variables);
					error=runFailure.failureExpression.parse(editFailure.getFailureExpression());
					if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidTransporterInterDownTimeExpression"),name,editFailure.getFailureExpression(),error+1);
				}
				runFailure.downTimeExpressionString=editFailure.getDownTimeExpression();
				if (runFailure.downTimeExpressionString==null) {
					runFailure.downTimeDistribution=DistributionTools.cloneDistribution(editFailure.getDownTimeDistribution());
				} else {
					runFailure.downTimeExpression=new ExpressionCalc(variables);
					error=runFailure.downTimeExpression.parse(runFailure.downTimeExpressionString);
					if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidTransporterDownTimeExpression"),name,runFailure.downTimeExpressionString,error+1);
				}
				failures.add(runFailure);
			}
			final RunDataTransporterFailure[] failuresArray=failures.toArray(new RunDataTransporterFailure[0]);

			/* Anzahl pro Station für Transportertyp */
			final List<RunDataTransporter> runTransporterList=new ArrayList<>();
			for (Map.Entry<String,Integer> entry: transporter.getCount().entrySet()) {
				if (entry.getValue()<0) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidCountAtStation"),name,entry.getKey(),entry.getValue().intValue());
				if (entry.getValue()==0) continue;

				final Integer stationID=stations.get(entry.getKey());
				if (stationID==null) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidStation"),name,entry.getKey());
				final ModelElement element=surface.getByIdIncludingSubModels(stationID.intValue());
				if (!(element instanceof ModelElementTransportTransporterSource) && !(element instanceof ModelElementTransportParking)) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidStation2"),name,entry.getKey());
				if (element instanceof ModelElementTransportTransporterSource) {
					final ModelElementTransportTransporterSource source=(ModelElementTransportTransporterSource)element;
					if (!name.equals(source.getTransporterType())) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidStation2"),name,entry.getKey());
				}
				if (element instanceof ModelElementTransportParking) {
					final ModelElementTransportParking parking=(ModelElementTransportParking)element;
					if (!name.equals(parking.getTransporterType())) return String.format(Language.tr("Simulation.Creator.Transporter.InvalidStation2"),name,entry.getKey());
				}

				for (int i=0;i<entry.getValue();i++) {
					final RunDataTransporter runTransporter=new RunDataTransporter(typeIndex,i,capacity,load,unload,iconEastEmpty,iconWestEmpty,iconEastLoaded,iconWestLoaded,failuresArray,this,variables);
					runTransporter.position=stationID.intValue();
					runTransporterList.add(runTransporter);
				}
			}
			transporterList.add(runTransporterList.toArray(new RunDataTransporter[0]));
		}

		/* Daten in globale Arrays eintragen */
		type=typeList.toArray(new String[0]);
		distances=distancesList.toArray(new double[0][][]);
		expressionString=expressionList.toArray(new String[0]);
		expression=new ExpressionCalc[expressionString.length];
		this.transporters=transporterList.toArray(new RunDataTransporter[0][]);

		return null;
	}

	@Override
	public RunDataTransporters clone() {
		final RunDataTransporters clone=new RunDataTransporters();

		clone.type=type; /* brauchen wir nicht kopieren, ist statisch - Arrays.copyOf(type,type.length); */
		clone.expressionString=expressionString; /* brauchen wir nicht kopieren, ist statisch -  Arrays.copyOf(expressionString,expressionString.length); */
		clone.expression=new ExpressionCalc[clone.expressionString.length];
		clone.distances=distances;
		/* brauchen wir nicht kopieren, ist statisch
		clone.distances=new double[distances.length][][];
		for (int i=0;i<distances.length;i++) {
			clone.distances[i]=new double[distances[i].length][];
			for (int j=0;j<distances[i].length;j++) clone.distances[i][j]=Arrays.copyOf(distances[i][j],distances[i][j].length);
		}
		 */
		clone.variableNamesWithDistance=variableNamesWithDistance; /* brauchen wir nicht kopieren, ist statisch -  Arrays.copyOf(variableNamesWithDistance,variableNamesWithDistance.length); */
		clone.transporters=new RunDataTransporter[transporters.length][];
		for (int i=0;i<transporters.length;i++) clone.transporters[i]=Arrays.asList(transporters[i]).stream().map(t->t.clone(variables,clone)).toArray(RunDataTransporter[]::new);

		return clone;
	}

	/**
	 * Muss zu Beginn der Simulation aufgerufen werden, um alle Stationen über ihren
	 * Transporter-Anfangsbestand zu benachrichtigen
	 * @param simData	Simulationsdatenobjekt
	 */
	public void prepare(final SimulationData simData) {
		for (RunDataTransporter[] type: transporters) for (RunDataTransporter transporter: type) {
			transporter.prepareFailureSystem(simData,this.type[transporter.type]);
			transporter.moveTo(transporter.position,0,simData);
		}
	}

	/**
	 * Liefert die Strecke, die ein Transporter zurücklegen muss, um von einer Station zu einer anderen zu fahren
	 * @param indexTransporter	Typ des Transporters
	 * @param idFrom	ID der Ausgangsstation
	 * @param idTo	ID der Zielstation
	 * @return	Strecke
	 */
	public double getTransferDistance(final int indexTransporter, final int idFrom, final int idTo) {
		/* Wenn nötig Ausdruck vorbereiten */
		if (this.expression[indexTransporter]==null) {
			this.expression[indexTransporter]=new ExpressionCalc(variableNamesWithDistance);
			this.expression[indexTransporter].parse(expressionString[indexTransporter]);
		}

		/* Entfernung bestimmen */
		final double[][] arr1=distances[indexTransporter];
		if (arr1==null) return 0.0;
		final double[] arr2=arr1[idFrom];
		if (arr2==null) return 0.0;
		return arr2[idTo];
	}

	/**
	 * Cache für das Variablenwerte-Array zur Nutzung in {@link #getTransferTime(RunDataTransporter, double, boolean, SimulationData)}
	 * @see #getTransferTime(RunDataTransporter, double, boolean, SimulationData)
	 */
	private double[] variableValues=null;

	/**
	 * Liefert die Zeit, die ein Transporter benötigt, um eine bestimmte Distanz zurückzulegen
	 * @param transporter	Transporter für den die Fahrtzeit (und zusätzlich ggf. die Lade- und Entladezeit) bestimmt werden soll
	 * @param distance	Zurückzulegende Distanz
	 * @param carriesClients	Gibt an, ob der Transporter bei der Fahrt Kunden transportiert
	 * @param simData	Simulationdatenobjekt
	 * @return	Fahrtzeit in Sekunden
	 */
	public double getTransferTime(final RunDataTransporter transporter, final double distance, final boolean carriesClients, final SimulationData simData) {
		/* Passenden Ausdruck wählen */
		final ExpressionCalc expression=this.expression[transporter.type];

		double time=0;

		/* Ausdruck berechnen */
		if (variableValues==null) variableValues=new double[simData.runData.variableValues.length+1];
		for (int i=0;i<simData.runData.variableValues.length;i++) variableValues[i]=simData.runData.variableValues[i];
		variableValues[variableValues.length-1]=distance;
		try {
			final double d=expression.calc(variableValues,simData,null);
			if (d>0) time+=d;
		} catch (MathCalcError e) {
			simData.calculationErrorStation(expression,type[transporter.type]);
		}

		if (carriesClients) {
			/* Lade- und Entladezeiten */
			if (transporter.loadDistribution!=null) {
				time+=DistributionRandomNumber.randomNonNegative(transporter.loadDistribution);
			}
			if (transporter.loadExpression!=null) {
				time+=FastMath.max(0,transporter.loadExpression.calcOrDefault(simData.runData.variableValues,0.0));
			}
			if (transporter.unloadDistribution!=null) {
				time+=DistributionRandomNumber.randomNonNegative(transporter.unloadDistribution);
			}
			if (transporter.unloadExpression!=null) {
				time+=FastMath.max(0,transporter.unloadExpression.calcOrDefault(simData.runData.variableValues,0.0));
			}
		}

		return time;
	}

	/**
	 * Findet einen an einer bestimmten Station wartenden Transporter
	 * @param indexTransporter	Typ des Transporters
	 * @param stationID	ID der Station, an der ein Transporter gesucht wird
	 * @return	Transporter-Objekt oder <code>null</code>, wenn kein Transporter an der Station verfügbar ist
	 */
	public RunDataTransporter getWaitingTransporter(final int indexTransporter, final int stationID) {
		for (RunDataTransporter transporter: transporters[indexTransporter]) if (!transporter.inTransfer && transporter.onlineAgainAt<=0 && transporter.position==stationID) return transporter;
		return null;
	}

	/**
	 * Findet einen an einer anderen Station als der eigenen wartenden Transporter (um diesen anzufordern)
	 * @param indexTransporter	Typ des Transporters
	 * @param ownStationID	ID der Station, die beim Suchen nach Transporter <b>nicht</b> berücksichtigt werden soll
	 * @param requestPriority	Anfragepriorität (die höher sein muss als die Verteidigungspriorität der bisherigen Station)
	 * @param simData	Simulationsdatenobjekt
	 * @return	Transporter-Objekt oder <code>null</code>, wenn kein Transporter verfügbar ist
	 */
	public RunDataTransporter getWaitingTransporterAtOtherStation(final int indexTransporter, final int ownStationID, final double requestPriority, final SimulationData simData) {
		for (RunDataTransporter transporter: transporters[indexTransporter]) if (!transporter.inTransfer && transporter.onlineAgainAt<=0 && transporter.position!=ownStationID) {
			final RunElement element=(transporter.position>=0)?simData.runModel.elementsFast[transporter.position]:null;
			if (element instanceof TransporterPosition) {
				final Double D=((TransporterPosition)element).stayHerePriority(transporter,simData);
				if (D!=null && D.doubleValue()>=requestPriority) continue; /* Pech gehabt, Ausgangsstation behält den Transporter. */
			}
			return transporter;
		}
		return null;
	}

	/**
	 * Liefert die Anzahl an Transportern, die an einer Station warten
	 * @param indexTransporter	Typ des Transporters
	 * @param stationID	ID der Station, an der ein Transporter gesucht wird
	 * @return	Anzahl an momentan wartenden Transportern
	 */
	public int getWaitingTransporterCount(final int indexTransporter, final int stationID) {
		int count=0;
		for (RunDataTransporter transporter: transporters[indexTransporter]) if (!transporter.inTransfer && transporter.position==stationID) count++;
		return count;
	}

	/**
	 * Wird aufgerufen, wenn ein Transporter wieder frei und verfügbar ist
	 * @param transporter	Wieder verfügbarer Transporter
	 * @param simData	Simulationsdatenobjekt
	 */
	public void transporterFree(final RunDataTransporter transporter, final SimulationData simData) {
		simData.runData.fireReleaseTransporterNotify(transporter,simData);
	}

	/**
	 * Zählt, wie viele Transporter eines Typs momentan im unterwegs sind
	 * @param indexTransporter	Typ des Transporters
	 * @return	Anzahl der Transporter des angegebenen Typs im Leerlauf
	 */
	public int getWorkingTransporters(final int indexTransporter) {
		int count=0;
		for (RunDataTransporter transporter: transporters[indexTransporter]) if (transporter.inTransfer) count++;
		return count;
	}

	/**
	 * Liefert die Anzahl an Transportern des angegebenen Typs
	 * @param indexTransporter	Typ des Transporters
	 * @return	Anzahl an Transportern des Typs. Liegt der Index außerhalb des gültigen Bereiches, so liefert die Funktion 0.
	 */
	public int getTransporterCount(final int indexTransporter) {
		if (indexTransporter<0 || indexTransporter>=transporters.length) return 0;
		return transporters[indexTransporter].length;
	}

	/**
	 * Liefert die Anzahl an Transportern in allen Typen zusammen
	 * @return	Anzahl an Transportern
	 */
	public int getTransporterCount() {
		int count=0;
		for (RunDataTransporter[] transporter: transporters) count+=transporter.length;
		return count;
	}

	/**
	 * Gibt an, wie viele Kunden ein Transporter eines bestimmten Typs transportieren kann
	 * @param indexTransporter	Typ des Transporters
	 * @return	Anzahl an Kunden, die der Transporter gleichzeitig transportieren kann
	 */
	public int getTransporterCapacity(final int indexTransporter) {
		if (indexTransporter<0 || indexTransporter>=transporters.length) return 0;
		if (transporters[indexTransporter].length==0) return 0;
		return transporters[indexTransporter][0].capacity;
	}

	/**
	 * Liefert eine Liste mit allen Transportern im System
	 * @return	Liste mit allen Transportern
	 */
	public RunDataTransporter[] getTransporters() {
		List<RunDataTransporter> transporterList=new ArrayList<>();
		for (RunDataTransporter[] list: transporters) for (RunDataTransporter transporter: list) transporterList.add(transporter);
		return transporterList.toArray(new RunDataTransporter[0]);
	}

	/**
	 * Auslastungsstatistik aller Transportergruppen als Array
	 * @see #getUsageStatistics(SimulationData)
	 */
	private StatisticsTimePerformanceIndicator[] statisticsUsage=null;

	/**
	 * Liefert die Auslastungsstatistik aller Transportergruppen als Array zurück
	 * @param simData	Simulationsdatenobjekt
	 * @return Auslastungsstatistik aller Transportergruppen als Array
	 */
	public StatisticsTimePerformanceIndicator[] getUsageStatistics(final SimulationData simData) {
		if (statisticsUsage==null) {
			statisticsUsage=new StatisticsTimePerformanceIndicator[type.length];
			for (int i=0;i<type.length;i++) statisticsUsage[i]=(StatisticsTimePerformanceIndicator)simData.statistics.transporterUtilization.get(type[i]);
		}
		return statisticsUsage;
	}

	/**
	 * Ausfallstatistik aller Transportergruppen als Array
	 * @see #getDownTimeStatistics(SimulationData)
	 */
	private StatisticsTimePerformanceIndicator[] statisticsDown=null;

	/**
	 * Liefert die Ausfallstatistik aller Transportergruppen als Array zurück
	 * @param simData	Simulationsdatenobjekt
	 * @return Ausfallstatistik aller Transportergruppen als Array
	 */
	public StatisticsTimePerformanceIndicator[] getDownTimeStatistics(final SimulationData simData) {
		if (statisticsDown==null) {
			statisticsDown=new StatisticsTimePerformanceIndicator[type.length];
			for (int i=0;i<type.length;i++) statisticsDown[i]=(StatisticsTimePerformanceIndicator)simData.statistics.transporterInDownTime.get(type[i]);
		}
		return statisticsDown;
	}

	/**
	 * Gibt an, wie viele Transporter eines bestimmten Typs zu einem Zeitpunkt in Ausfallzeit sind
	 * @param index	0-basierender Index des Transportertyps
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Transportern
	 */
	public int getDown(final int index, final SimulationData simData) {
		if (index<0 || index>=type.length) return 0;
		return getDownTimeStatistics(simData)[index].getCurrentState();
	}

	/**
	 * Gibt an, wie viele Transporter zu einem Zeitpunkt insgesamt in Ausfallzeit sind
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Transportern
	 */
	public int getAllDown(final SimulationData simData) {
		int sum=0;
		for (StatisticsTimePerformanceIndicator indicator: getDownTimeStatistics(simData)) sum+=indicator.getCurrentState();
		return sum;
	}
}