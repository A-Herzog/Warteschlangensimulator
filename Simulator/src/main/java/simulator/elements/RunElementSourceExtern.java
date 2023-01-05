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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import ui.inputprocessor.ClientInputTableProcessor;
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
	 * @param onlyFirstClientType	Sollen alle (<code>true</code>) oder nur der erste Kundentyp (<code>false</code>) zurückgegeben werden?
	 * @return	Aufbereitete Liste mit Kundentyp-Namen
	 */
	private static final String[] getClientTypes(final List<String> rawList, final boolean onlyFirstClientType) {
		final Set<String> set=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		final int size=rawList.size();
		for (int i=0;i<size;i++) {
			final String s=rawList.get(i).trim();
			if (s.isEmpty()) continue;
			set.add(s);
			if (onlyFirstClientType && set.size()>0) break;
		}
		return set.toArray(new String[0]);
	}

	/**
	 * Erzeugt die Ankünfte aus einer Tabelle.<br>
	 * Spalten der Tabelle:	Zahlenwert, Kundentypname, (optional) Zuweisungen
	 * @param id	ID der zugehörigen Tabellenkundenquellen-Station
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param externalTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param bottomUp	Tabelle von unten nach oben lesen
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ <code>Arrival[][]</code> zurück, sonst eine Fehlermeldung (<code>String</code>)
	 */
	public static final Object loadTableToArrivals(final int id, final Table table, final List<String> externalTypes, final boolean numbersAreDistances, final boolean bottomUp) {
		final String[] types=getClientTypes(externalTypes,false);
		final Map<String,Integer> typesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (int i=0;i<types.length;i++) typesMap.put(types[i],i);

		final List<List<RunElementSourceExtern.Arrival>> arrivalsList=new ArrayList<>(types.length);
		for (int i=0;i<types.length;i++) arrivalsList.add(new ArrayList<>());

		double lastArrivalTime=0;

		/* Tabelle verarbeiten */
		final int rows=table.getSize(0);
		boolean isSorted=true;
		for (int i=0;i<rows;i++) {
			/* Zeile laden */
			final List<String> line;
			if (bottomUp) {
				line=table.getLine(rows-1-i);
			} else {
				line=table.getLine(i);
			}
			if (line==null || line.size()<2) continue;

			/* Zahlenwert in erster Spalte? */
			Double D=NumberTools.getPlainDouble(line.get(0));
			if (D==null) D=NumberTools.getNotNegativeDouble(line.get(0));
			if (D==null || D<0) continue;

			/* Ankunftszeit */
			final double arrivalTime;
			if (numbersAreDistances) {
				/* Zwischenankunftszeiten */
				arrivalTime=lastArrivalTime+D.doubleValue();
			} else {
				/* Ankunftszeitpunkte */
				arrivalTime=D.doubleValue();
			}
			if (arrivalTime<lastArrivalTime) isSorted=false;
			lastArrivalTime=arrivalTime;

			/* Erst Ankunftszeit bestimmen, dann bestimmen, ob Zeile übersprungen wird. So sind relative Zeitabstände immer korrekt, auch wenn später übersprungene Zeilen fehlen. */

			/* Gültiger Kundentyp in zweiter Spalte? */
			final String clientType=line.get(1).trim();
			final Integer I=typesMap.get(clientType);
			if (I==null) continue;
			final int index=I;

			/* Ankunftszeit erfassen */
			final Arrival a=new Arrival(clientType,arrivalTime);
			final int error=a.loadData(line,2); /* Weitere Spalten laden */
			if (error>=0) return String.format(Language.tr("Simulation.Creator.TableFile.InvalidData"),id,i+1,error+1);
			arrivalsList.get(index).add(a);
		}

		/* Sortieren und ausgeben */
		final Arrival[] dummy=new Arrival[0];
		final Arrival[][] arrivals=new Arrival[types.length][];
		for (int i=0;i<types.length;i++) {
			final Arrival[] list=arrivalsList.get(i).toArray(dummy);
			if (!isSorted) Arrays.sort(list,(a1,a2)->{
				final long l=a1.time-a2.time;
				if (l<0) return -1;
				if (l>0) return 1;
				return 0;
			});
			arrivals[i]=list;
		}
		return arrivals;
	}

	/**
	 * Erzeugt die Ankünfte aus einer Tabelle.<br>
	 * Es wird dabei eine individuelle Spaltenkonfiguration verwendet.
	 * @param id	ID der zugehörigen Tabellenkundenquellen-Station
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param setup	Konfiguration der Spalten
	 * @param externalTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param bottomUp	Tabelle von unten nach oben lesen
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ <code>Arrival[][]</code> zurück, sonst eine Fehlermeldung (<code>String</code>)
	 */
	public static final Object loadTableToArrivals(final int id, final Table table, final String setup, final List<String> externalTypes, final boolean numbersAreDistances, final boolean bottomUp) {
		final String[] types=getClientTypes(externalTypes,false);
		final Map<String,Integer> typesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (int i=0;i<types.length;i++) typesMap.put(types[i],i);

		final ClientInputTableProcessor.ColumnsSetup columnSetup=new ClientInputTableProcessor.ColumnsSetup(setup);
		final int timeColumn=columnSetup.getTimeColumnIndex();
		final int clientTypeColumn=columnSetup.getClientTypeColumnIndex();
		final int minimumNeededColumns=Math.max(timeColumn,clientTypeColumn)+1;

		final List<List<RunElementSourceExtern.Arrival>> arrivalsList=new ArrayList<>(types.length);
		for (int i=0;i<types.length;i++) arrivalsList.add(new ArrayList<>());

		double lastArrivalTime=0;

		/* Tabelle verarbeiten */
		final int rows=table.getSize(0);
		List<String> heading=new ArrayList<>();
		if (rows>0) heading=table.getLine(0);
		boolean isSorted=true;
		for (int i=1;i<rows;i++) { /* Zeile 0 = Überschrift überspringen */
			/* Zeile laden */
			final List<String> line;
			if (bottomUp) {
				line=table.getLine(rows-i);
			} else {
				line=table.getLine(i);
			}
			if (line==null || line.size()<minimumNeededColumns) continue;

			/* Zahlenwert in erster Spalte? */
			Double D=NumberTools.getPlainDouble(line.get(timeColumn));
			if (D==null) D=NumberTools.getNotNegativeDouble(line.get(timeColumn));
			if (D==null || D<0) continue;

			/* Ankunftszeit */
			final double arrivalTime;
			if (numbersAreDistances) {
				/* Zwischenankunftszeiten */
				arrivalTime=lastArrivalTime+D.doubleValue();
			} else {
				/* Ankunftszeitpunkte */
				arrivalTime=D.doubleValue();
			}
			if (arrivalTime<lastArrivalTime) isSorted=false;
			lastArrivalTime=arrivalTime;

			/* Erst Ankunftszeit bestimmen, dann bestimmen, ob Zeile übersprungen wird. So sind relative Zeitabstände immer korrekt, auch wenn später übersprungene Zeilen fehlen. */

			/* Gültiger Kundentyp in zweiter Spalte? */
			final String clientType=line.get(clientTypeColumn).trim();
			final Integer I=typesMap.get(clientType);
			if (I==null) continue;
			final int index=I;

			/* Ankunftszeit erfassen */
			final Arrival a=new Arrival(clientType,arrivalTime);
			final int error=a.loadData(line,heading,columnSetup); /* Weitere Spalten laden */
			if (error>=0) return String.format(Language.tr("Simulation.Creator.TableFile.InvalidData"),id,i+1,error+1);
			arrivalsList.get(index).add(a);
		}

		/* Sortieren und ausgeben */
		final Arrival[] dummy=new Arrival[0];
		final Arrival[][] arrivals=new Arrival[types.length][];
		for (int i=0;i<types.length;i++) {
			final Arrival[] list=arrivalsList.get(i).toArray(dummy);
			if (!isSorted) Arrays.sort(list,(a1,a2)->{
				final long l=a1.time-a2.time;
				if (l<0) return -1;
				if (l>0) return 1;
				return 0;
			});
			arrivals[i]=list;
		}
		return arrivals;
	}

	/**
	 * Erzeugt die Ankünfte aus einer Tabelle.<br>
	 * Spalten der Tabelle:	Zahlenwert, Kundentypname, (optional) Zuweisungen
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param setup	Konfiguration der Spalten (kann <code>null</code> sein, wenn eine bereits aufbereitete Tabelle verwendet werden soll)
	 * @param externalTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param bottomUp	Tabelle von unten nach oben lesen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected final String loadTable(final Table table, final String setup, final List<String> externalTypes, final boolean numbersAreDistances, final boolean bottomUp) {
		final Object result;
		if (setup==null) {
			result=loadTableToArrivals(id,table,externalTypes,numbersAreDistances,bottomUp);
		} else {
			result=loadTableToArrivals(id,table,setup,externalTypes,numbersAreDistances,bottomUp);
		}
		if (result instanceof String) return (String)result;
		arrivals=(Arrival[][])result;
		return null;
	}

	/**
	 * Erzeugt die Ankünfte aus einer Tabelle.<br>
	 * Spalten der Tabelle:	Zahlenwert, Kundentypname, (optional) Zuweisungen
	 * @param table	Tabelle aus der die Daten geladen werden sollen
	 * @param externalTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param bottomUp	Tabelle von unten nach oben lesen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected final String loadTable(final Table table, final List<String> externalTypes, final boolean numbersAreDistances, final boolean bottomUp) {
		return loadTable(table,null,externalTypes,numbersAreDistances,bottomUp);
	}

	/**
	 * Verknüpft die angegebenen Kundentypen mit den Modell-IDs
	 * @param externalClientTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param runModel	Laufzeit-Modell
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	protected final String buildClientTypesList(final List<String> externalClientTypes, final RunModel runModel) {
		final String[] types=getClientTypes(externalClientTypes,false);
		if (types.length==0) return String.format(Language.tr("Simulation.Creator.NoTableClientTypes"),id);
		this.clientTypes=new int[types.length];
		for (int i=0;i<types.length;i++) {
			this.clientTypes[i]=-1;
			Integer I=runModel.clientTypesMap.get(types[i]);
			if (I==null) return String.format(Language.tr("Simulation.Creator.SetInternalError"),id);
			this.clientTypes[i]=I;
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
		if (getClientTypes(externalClientTypes,true).length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoTableClientTypes"),element.getId()),RunModelCreatorStatus.Status.NO_CLIENT_TYPES_TABLE);
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
		/* Nach Abbruch ist wirklich Schluss */
		if (simData.runData.stopp) return;

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
	 * Weist einen geladenen Wert an ein Kundendatenfeld zu.
	 * @param client	Kunde bei dem der Wert zugewiesen werden soll
	 * @param index	Index des Datenfeldes (nicht negativer Wert für normales Datenfeld, negativer Wert für Zeit oder Kosten)
	 * @param value	Zuzuweisender Wert
	 */
	private void setClientValue(final RunDataClient client, final int index, final double value) {
		if (index>=0) {
			/* Normales Kundendatenfeld */
			client.setUserData(index,value);
		} else {
			/* Besonderer Wert */
			switch (index) {
			case -1: client.waitingTime=Math.round(value*1000); break;
			case -2: client.transferTime=Math.round(value*1000); break;
			case -3: client.processTime=Math.round(value*1000); break;
			case -4: client.waitingAdditionalCosts=value; break;
			case -5: client.transferAdditionalCosts=value; break;
			case -6: client.processAdditionalCosts=value; break;
			}
		}
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

		/* Evtl. WarmUp-Zeit beenden */
		if (simData.runData.isWarmUp) {
			/* Warm-Up-Phasenlänge wird nicht durch Threadanzahl geteilt, sondern auf jedem Kern wird die angegebene Anzahl simuliert */
			if (simData.runData.clientsArrived>=FastMath.round(simData.runModel.warmUpTime*simData.runModel.clientCountModel) && simData.runModel.warmUpTime>0) { /* runModel.warmUpTime>0 bedeutet, dass die Beendigung der Einschwingphase nach Zeit nur dann erfolgt, wenn diese in diesem Modus überhaupt aktiv ist. */
				simData.endWarmUp();
				/* Logging */
				if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.WarmUpEnd"),Language.tr("Simulation.Log.WarmUpEnd.Info"));
			}
		}

		/* Kunde anlegen */
		final RunDataClient newClient=simData.runData.clients.getClient(clientTypes[index],simData);

		final Arrival arrival=arrivals[index][data.nextIndex[index]-1];

		if (data.calc==null) data.calc=new ExpressionCalc(simData.runModel.variableNames);

		/* Ggf. ClientData setzen */
		if (arrival.dataIndex!=null) for (int i=0;i<arrival.dataIndex.length;i++) {
			/* Einfache Zahl? */
			final Double D=NumberTools.getPlainDouble(arrival.dataFormula[i]);
			if (D!=null) {
				setClientValue(newClient,arrival.dataIndex[i],D);
				continue;
			}

			/* Rechenausdruck */
			/* langsam: final ExpressionCalc calc=new ExpressionCalc(simData.runModel.variableNames); - stattdessen verwenden wir das Objekt wieder. */
			if (data.calc.parse(arrival.dataFormula[i])<0) {
				simData.runData.setClientVariableValues(newClient);
				try {
					setClientValue(newClient,arrival.dataIndex[i],data.calc.calc(simData.runData.variableValues,simData,newClient));
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

		/* Zwischenankunftszeiten in der Statistik erfassen */
		simData.runData.logStationArrival(simData.currentTime,simData,this,data,newClient);

		/* Ggf. Kunde in Untermodell eintragen */
		if (parentId>=0) simData.runData.logClientEntersStation(simData,simData.runModel.elementsFast[parentId],null,newClient);

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
	 * @see RunElementSourceExtern#loadTable(Table, String, List, boolean, boolean)
	 * @see RunElementSourceExtern#processArrivalEvent(SimulationData, boolean, int)
	 */
	public static class Arrival {
		/** Kundentyp */
		public final String clientType;
		/** Ankunftszeit in MS */
		public final long time;
		/** Kundendatenfelder-Indices für die Zuweisungen der Ergebnisse von {@link #dataFormula} (Werte &ge;0 für Datenfelder, -1=w, -2=t, -3=p, -4=wKosten, -5=tKosten, -6=pKosten */
		public int[] dataIndex=null;
		/** Rechenformeln deren Ergebnisse an die Kundendatenfelder {@link #dataIndex} zugewiesen werden sollen */
		public String[] dataFormula=null;
		/** Kundentextdatenfelder-Zuweisungen (Schlüssel zu Wert) */
		public Map<String,String> dataKeyValue=null;

		/**
		 * Konstruktor der Klasse
		 * @param clientType	Kundentyp
		 * @param time	Ankunftszeit in Sekunden
		 */
		public Arrival(final String clientType, final double time) {
			this.clientType=clientType;
			this.time=FastMath.round(time*1000);
		}

		/**
		 * Verarbeitet eine Kundendaten- bzw. Kundentextdatenfeld-Zuweisung
		 * @param cell	Zu interpretierende Zelle
		 * @param data	Array der Länge 2, welches entweder Index und Formel oder Schlüssel und Wert aufnimmt
		 * @return	Gibt an, ob die Zelle erfolgreich verarbeitet werden konnte
		 */

		public static boolean processCell(final String cell, final Object[] data) {
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

			final String part1Lower=part1.toLowerCase();

			if (part1Lower.equals("w")) {
				data[0]=-1;
				data[1]=part2;
				return true;
			}

			if (part1Lower.equals("t")) {
				data[0]=-2;
				data[1]=part2;
				return true;
			}

			if (part1Lower.equals("p")) {
				data[0]=-3;
				data[1]=part2;
				return true;
			}

			if (part1Lower.equals("wcosts") || part1Lower.equals("wkosten")) {
				data[0]=-4;
				data[1]=part2;
				return true;
			}

			if (part1Lower.equals("tcosts") || part1Lower.equals("tkosten")) {
				data[0]=-5;
				data[1]=part2;
				return true;
			}

			if (part1Lower.equals("pcosts") || part1Lower.equals("pkosten")) {
				data[0]=-6;
				data[1]=part2;
				return true;
			}

			return false;
		}

		/**
		 * Recycling eines Objektes, das sonst bei jedem Aufruf
		 * von {@link #loadData(List, int)}, d.h. für jede Zeile
		 * erneut erstellt werden müsste.
		 * @see #loadData(List, int)
		 */
		final Object[] loadDataLineParts=new Object[2];

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
			for (int i=startColumn;i<size;i++) {
				final String cell=line.get(i).trim();
				if (cell.isEmpty()) continue;

				if (!processCell(cell,loadDataLineParts)) return i;

				if (loadDataLineParts[0] instanceof Integer) {
					/* Numerischer Wert (der noch berechnet werden will) */
					if (dataIndex==null || dataFormula==null) {
						dataIndex=new ArrayList<>();
						dataFormula=new ArrayList<>();
					}
					dataIndex.add((Integer)loadDataLineParts[0]);
					dataFormula.add((String)loadDataLineParts[1]);
					continue;
				}

				if (loadDataLineParts[0] instanceof String) {
					/* Key=Value Zuweisung */
					if (dataKeyValue==null) dataKeyValue=new HashedMap<>();
					dataKeyValue.put((String)loadDataLineParts[0],(String)loadDataLineParts[1]);
					continue;
				}
			}

			if (dataIndex!=null && dataFormula!=null) {
				this.dataIndex=new int[dataIndex.size()];
				for (int i=0;i<this.dataIndex.length;i++) this.dataIndex[i]=dataIndex.get(i);
				this.dataFormula=dataFormula.toArray(new String[0]);
			}

			return -1;
		}

		/**
		 * Verarbeitet eine Tabellenzeile
		 * @param line	Zeile
		 * @param heading	Überschriftenzeile
		 * @param setup	Spaltenkonfiguration
		 * @return	Liefert im Erfolgsfall -1, sonst den 0-basierenden Index der fehlerhaften Spalte
		 */
		public int loadData(final List<String> line, final List<String> heading, final ClientInputTableProcessor.ColumnsSetup setup) {
			List<Integer> dataIndex=null;
			List<String> dataFormula=null;

			for (int i=0;i<setup.columnTypes.length;i++) {
				if (i>=line.size()) break;
				final ClientInputTableProcessor.ColumnMode mode=setup.columnTypes[i];
				final int numberIndex=setup.columnClientDataIndex[i];

				if (mode==ClientInputTableProcessor.ColumnMode.NUMBER) {
					if (numberIndex<0) continue;
					if (dataIndex==null || dataFormula==null) {
						dataIndex=new ArrayList<>();
						dataFormula=new ArrayList<>();
					}
					dataIndex.add(numberIndex);
					dataFormula.add(line.get(i));
				}

				if (mode==ClientInputTableProcessor.ColumnMode.TEXT) {
					if (i>=heading.size()) continue;
					/* Key=Value Zuweisung */
					if (dataKeyValue==null) dataKeyValue=new HashedMap<>();
					dataKeyValue.put(heading.get(i),line.get(i));
					continue;
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