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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import parser.MathCalcError;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElement;
import simulator.events.StationLeaveEvent;
import simulator.events.SystemArrivalEvent;
import simulator.runmodel.RunData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeOut;

/**
 * Basisklasse für Quellen-Elemente, die Daten aus Tabellenstrukturen laden
 * @author Alexander Herzog
 */
public abstract class RunElementSourceExtern extends RunElement implements RunSource {
	/** ID der Folgestation */
	private int connectionId;
	/** Folgestation (Übersetzung aus {@link #connectionId}) */
	private RunElement connection;

	/**
	 * IDs der Kundentypen, deren Ankünfte geladen werden sollen
	 */
	protected int[] clientTypes;

	/**
	 * Liste der Arrival-Objekte pro Kundentyp
	 */
	protected Arrival[][] arrivals;

	/**
	 * Konstruktor der Klasse <code>RunElementSourceExtern</code>
	 * @param element	Modell-Element aus dem ID und Farbe ausgelesen werden
	 * @param name	Name der Station
	 */
	public RunElementSourceExtern(final ModelElementBox element, final String name) {
		super(element,name);
	}

	/**
	 * Bildet eine Liste mit Kundentyp-Namen (in ggf. falscher Groß- und Kleinschreibung
	 * und ggf. mit leeren Einträgen und Dubletten) auf Simulationsmodell-Kunden ab
	 * @param rawList	Liste mit Kundentyp-Namen
	 * @return	Aufbereitete Liste mit Kundentyp-Namen
	 */
	private static final String[] getClientTypes(final List<String> rawList) {
		final List<String> newList=new ArrayList<>();
		for (String type: rawList) {
			final String s=type.trim();
			if (s.isEmpty()) continue;
			boolean inList=false;
			for (String rec: newList) if (rec.equalsIgnoreCase(s)) {inList=true; break;}
			if (!inList) newList.add(s);
		}
		return newList.toArray(new String[0]);
	}

	/**
	 * Erzeugt die Ankünfte aus einer Tabelle.<br>
	 * Spalten der Tabelle:	Zahlenwert, Kundentypname, (optional) Zuweisungen
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param externalTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected final String loadTable(final Table table, final List<String> externalTypes, final boolean numbersAreDistances) {
		final String[] types=getClientTypes(externalTypes);
		final List<List<RunElementSourceExtern.Arrival>> arrivalsList=new ArrayList<>(types.length);
		for (int i=0;i<types.length;i++) arrivalsList.add(new ArrayList<>());

		double lastArrivalTime=0;

		/* Tabelle verarbeiten */
		final int rows=table.getSize(0);
		for (int i=0;i<rows;i++) {
			/* Zeile laden */
			final List<String> line=table.getLine(i);
			if (line==null || line.size()<2) continue;

			/* Zahlenwert in erster Spalte? */
			final Double D=NumberTools.getNotNegativeDouble(line.get(0));
			if (D==null) continue;

			/* Ankunftszeit */
			final double arrivalTime;
			if (numbersAreDistances) {
				/* Zwischenankunftszeiten */
				arrivalTime=lastArrivalTime+D.doubleValue();
				lastArrivalTime=arrivalTime;
			} else {
				/* Ankunftszeitpunkte */
				arrivalTime=D.doubleValue();
			}

			/* Erst Ankunftszeit bestimmen, dann bestimmen, ob Zeile übersprungen wird. So sind relative Zeitabstände immer korrekt, auch wenn später übersprungene Zeilen fehlen. */

			/* Gültiger Kundentyp in zweiter Spalte? */
			int index=-1;
			final String s=line.get(1).trim();
			for (int j=0;j<types.length;j++) if (types[j].equalsIgnoreCase(s)) {index=j; break;}
			if (index<0) continue;

			/* Ankunftszeit erfassen */
			final Arrival a=new Arrival(arrivalTime);
			final int error=a.loadData(line,2); /* Weitere Spalten laden */
			if (error>=0) return String.format(Language.tr("Simulation.Creator.TableFile.InvalidData"),id,i+1,error+1);
			arrivalsList.get(index).add(a);
		}

		/* Sortieren und ausgeben */
		arrivals=new Arrival[types.length][];
		for (int i=0;i<types.length;i++) {
			final Arrival[] list=arrivalsList.get(i).toArray(new Arrival[0]);
			Arrays.sort(list,(a1,a2)->{
				final long l=a1.time-a2.time;
				if (l<0) return -1;
				if (l>0) return 1;
				return 0;
			});
			arrivals[i]=list;
		}
		return null;
	}

	/**
	 * Verknüpft die angegebenen Kundentypen mit den Modell-IDs
	 * @param externalClientTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param runModel	Laufzeit-Modell
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected final String buildClientTypesList(final List<String> externalClientTypes, final RunModel runModel) {
		final String[] types=getClientTypes(externalClientTypes);
		if (types.length==0) return String.format(Language.tr("Simulation.Creator.NoTableClientTypes"),id);
		this.clientTypes=new int[types.length];
		for (int i=0;i<types.length;i++) {
			this.clientTypes[i]=-1;
			for (int j=0;j<runModel.clientTypes.length;j++) if (runModel.clientTypes[j].equalsIgnoreCase(types[i])) {this.clientTypes[i]=j; break;}
			if (this.clientTypes[i]<0) return String.format(Language.tr("Simulation.Creator.SetInternalError"),id);
		}
		return null;
	}

	/**
	 * Baut die Verknüpfung zum Nachfolge-Element auf
	 * @param modelElement	Modell-Element
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected final String buildConnection(final ModelElementEdgeOut modelElement) {
		connectionId=findNextId(modelElement.getEdgeOut());
		if (connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),modelElement.getId());
		return null;
	}

	/**
	 * Prüft die Liste der angegebenen Kundentypen
	 * @param externalClientTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param element	Zugehöriges Modell-Element
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected static final RunModelCreatorStatus testClientTypes(final List<String> externalClientTypes, final ModelElement element) {
		if (getClientTypes(externalClientTypes).length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTableClientTypes"),element.getId()),RunModelCreatorStatus.Status.NO_CLIENT_TYPES_TABLE);
		return null;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connection=runModel.elements.get(connectionId);
	}

	@Override
	public RunElementSourceExternData getData(final SimulationData simData) {
		RunElementSourceExternData data;
		data=(RunElementSourceExternData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementSourceExternData(this,clientTypes.length);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Source-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		/* Wird nie aufgerufen: Source-Elemente haben keine einlaufenden Kanten bzw. führen keine Verarbeitung von Kunden durch. */
	}

	/**
	 * Legt das Ereignis für die nächste Kundenankunft an.
	 * @param simData	Simulationsdatenobjekt
	 * @param index	0-basierte Nummer des Kunden in der Liste
	 */
	private void scheduleNextArrival(final SimulationData simData, final int index) {
		final RunElementSourceExternData data=getData(simData);

		int nextIndex=data.nextIndex[index];
		if (nextIndex>=arrivals[index].length) return; /* Schon alle Ankünfte für diesen Typ erledigt. */
		final long time=arrivals[index][nextIndex].time;
		data.nextIndex[index]++;

		/* Ereignis erstellen */
		final SystemArrivalEvent nextArrival=(SystemArrivalEvent)simData.getEvent(SystemArrivalEvent.class);
		nextArrival.init(time);
		nextArrival.source=this;
		nextArrival.index=index;
		simData.eventManager.addEvent(nextArrival);
	}

	/**
	 * Muss zu Beginn eines jeden Simulationslaufes ausgeführt werden,
	 * um pro Kundentyp jeweils die erste Ankunft einzuplanen.<br>
	 * Aufruf über das {@link RunData#initRun(long, SimulationData, boolean)} über das {@link RunSource}-Interface
	 * @param simData	Simulationsdatenobjekt
	 */
	@Override
	public void scheduleInitialArrivals(SimulationData simData) {
		for (int i=0;i<clientTypes.length;i++) scheduleNextArrival(simData,i);
	}

	/**
	 * Führt die Ankunft eines Kunden durch und plant ggf. die nächste Ankunft ein.<br>
	 * Aufruf über das {@link SystemArrivalEvent} über das {@link RunSource}-Interface
	 * @param simData	Simulationsdatenobjekt
	 * @param scheduleNext	Wird von diesem Quelltyp nicht verwendet
	 * @param index	Index in der Liste der betrachteten Kundentypen
	 */
	@Override
	public void processArrivalEvent(final SimulationData simData, final boolean scheduleNext, final int index) { /* "scheduleNext" wird von diesem Source-Type nicht verwendet */
		final RunElementSourceExternData data=getData(simData);
		boolean isLastClient=false;

		/* Kunde anlegen */
		final RunDataClient newClient=simData.runData.clients.getClient(clientTypes[index],simData);

		final Arrival arrival=arrivals[index][data.nextIndex[index]-1];

		if (data.calc==null) data.calc=new ExpressionCalc(simData.runModel.variableNames);

		/* Ggf. ClientData setzen */
		if (arrival.dataIndex!=null) for (int i=0;i<arrival.dataIndex.length;i++) {
			/* langsam: final ExpressionCalc calc=new ExpressionCalc(simData.runModel.variableNames); - stattdessen verwenden wir das Objekt wieder. */
			if (data.calc.parse(arrival.dataFormula[i])<0) {
				simData.runData.setClientVariableValues(newClient);
				try {
					final double d=data.calc.calc(simData.runData.variableValues,simData,newClient);
					newClient.setUserData(arrival.dataIndex[i],d);
				} catch (MathCalcError e) {
					simData.calculationErrorStation(data.calc,this);
				}
			}
		}

		/* Ggf. Key=Value setzen */
		if (arrival.dataKeyValue!=null) {
			newClient.setUserDataStrings(arrival.dataKeyValue);
		}

		/* Notify-System über Kundenankunft informieren */
		newClient.nextStationID=id;
		simData.runData.fireClientMoveNotify(simData,newClient,false);

		/* Zähler erhöhen, um festzustellen, wann die Simulation beendet werden kann */
		simData.runData.clientsArrived++;

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.SourceArrival"),String.format(Language.tr("Simulation.Log.SourceArrival.Info"),newClient.logInfo(simData),simData.runData.getWarmUpStatus(),name,simData.runData.clientsArrived));

		/* Evtl. WarmUp-Zeit beenden */
		if (simData.runData.isWarmUp) {
			/* Warm-Up-Phasenlänge wird nicht durch Threadanzahl geteilt, sondern auf jedem Kern wird die angegebene Anzahl simuliert */
			if (simData.runData.clientsArrived>=FastMath.round(simData.runModel.warmUpTime*simData.runModel.clientCountModel)) {
				simData.runData.isWarmUp=false;
				simData.endWarmUp();
				simData.runData.clientsArrived=0;
				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.WarmUpEnd"),Language.tr("Simulation.Log.WarmUpEnd.Info"));
			}
		}

		/* Zwischenankunftszeiten in der Statistik erfassen */
		simData.runData.logStationArrival(simData.currentTime,simData,this,data,newClient);

		/* Wenn Ziel-Anzahl an Ankünften erreicht: Kunden Marker mitgeben, dass bei seiner Ankunft im Ziel die Simulation endet.*/
		if (simData.runData.nextClientIsLast(simData)) isLastClient=true;
		newClient.isLastClient=isLastClient;

		/* Kunden zur ersten (echten) Station leiten */
		StationLeaveEvent.sendToStation(simData,newClient,this,connection);

		/* Nächste Ankunft planen */
		scheduleNextArrival(simData,index);

		/* System über Status-Änderung benachrichtigen */
		simData.runData.fireStateChangeNotify(simData);

		/* Maximalzahl an Kunden im System eingehalten */
		if (!simData.testMaxAllowedClientsInSystem()) return;
	}

	@Override
	public boolean isClientCountStation() {
		return false;
	}

	@Override
	public RunElement getNext() {
		return connection;
	}

	/**
	 * Liefert die Gesamtanzahl der aus dieser Quelle eintreffenden Kunden.
	 * @return	Gesamtanzahl der aus dieser Quelle eintreffenden Kunden. Liefert -1, wenn die Quelle noch nicht geladen wurde.
	 */
	public long getArrivalCount() {
		if (arrivals==null) return -1;
		long sum=0;
		for (Arrival[] arrival: arrivals) sum+=arrival.length;
		return sum;
	}

	/**
	 * Ankunftsdatensatz
	 * @see RunElementSourceExtern#arrivals
	 * @see RunElementSourceExtern#loadTable(Table, List, boolean)
	 * @see RunElementSourceExtern#processArrivalEvent(SimulationData, boolean, int)
	 */
	private class Arrival {
		/** Ankunftszeit in MS */
		public final long time;
		/** Kundendatenfelder-Indices für die Zuweisungen der Ergebnisse von {@link #dataFormula} */
		public int[] dataIndex=null;
		/** Rechenformeln deren Ergebnisse an die Kundendatenfelder {@link #dataIndex} zugewiesen werden sollen */
		public String[] dataFormula=null;
		/** Kundentextdatenfelder-Zuweisungen (Schlüssel zu Wert) */
		public Map<String,String> dataKeyValue=null;

		/**
		 * Konstruktor der Klasse
		 * @param time	Ankunftszeit in Sekunden
		 */
		public Arrival(final double time) {
			this.time=FastMath.round(time*1000);
		}

		/**
		 * Verarbeitet eine Kundendaten- bzw. Kundentextdatenfeld-Zuweisung
		 * @param cell	Zu interpretierende Zelle
		 * @param data	Array der Länge 2, welches entweder Index und Formel oder Schlüssel und Wert aufnimmt
		 * @return	Gibt an, ob die Zelle erfolgreich verarbeitet werden konnte
		 */

		private boolean processCell(final String cell, final Object[] data) {
			final int pos=cell.indexOf('=');
			if (pos<1 || pos>=cell.length()-1) return false;
			final String part1=cell.substring(0,pos);
			final String part2=cell.substring(pos+1);

			final int index=CalcSymbolClientUserData.testClientData(part1);
			if (index>=0) {
				data[0]=index;
				data[1]=part2;
				return true;
			}

			final String key=CalcSymbolClientUserData.testClientDataString(part1);
			if (key!=null) {
				data[0]=key;
				data[1]=part2;
				return true;
			}

			return false;
		}

		/**
		 * Verarbeitet eine Tabellenzeile
		 * @param line	Zeile
		 * @param startColumn	Erste zu berücksichtigende Spalte (0-basierend gezählt)
		 * @return	Liefert im Erfolgsfall -1, sonst den 0-basierenden Index der fehlerhaften Spalte
		 */
		public int loadData(final List<String> line, final int startColumn) {
			List<Integer> dataIndex=null;
			List<String> dataFormula=null;

			final int size=line.size();
			final Object[] data=new Object[2];
			for (int i=startColumn;i<size;i++) {
				final String cell=line.get(i).trim();
				if (cell.isEmpty()) continue;

				if (!processCell(cell,data)) return i;

				if (data[0] instanceof Integer) {
					/* Numerischer Wert (der noch berechnet werden will) */
					if (dataIndex==null || dataFormula==null) {
						dataIndex=new ArrayList<>();
						dataFormula=new ArrayList<>();
					}
					dataIndex.add((Integer)data[0]);
					dataFormula.add((String)data[1]);
				}

				if (data[0] instanceof String) {
					/* Key=Value Zuweisung */
					if (dataKeyValue==null) dataKeyValue=new HashedMap<>();
					dataKeyValue.put((String)data[0],(String)data[1]);
				}
			}

			if (dataIndex!=null && dataFormula!=null) {
				this.dataIndex=new int[dataIndex.size()];
				for (int i=0;i<this.dataIndex.length;i++) this.dataIndex[i]=dataIndex.get(i);
				this.dataFormula=dataFormula.toArray(new String[0]);
			}

			return -1;
		}
	}
}