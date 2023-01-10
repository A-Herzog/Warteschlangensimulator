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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementSectionStart;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;

/**
 * Die <code>RunDataClient</code>-Klasse hält alle Laufzeitinformationen über einen Kunden vor.<br>
 * Ein <code>RunDataClient</code>-Objekt wird von <code>RunElementSource</code> erzeugt und wird
 * von <code>RunElementDispose</code> in der Statistik erfasst (womit ein Lebenszyklus endet).
 * @author Alexander Herzog
 */
public class RunDataClient {
	/**
	 * Höchster zulässiger Index für die Nutzerdaten.
	 * @see RunDataClient#getUserData(int)
	 * @see RunDataClient#setUserData(int, double)
	 */
	public static final int MAX_USER_DATA_INDEX=10_000;

	/**
	 * Fortlaufende Nummer der Kunden (wird von <code>RunDataClients.getClient()</code> gezählt)
	 */
	public long clientNumber;

	/**
	 * Kundentyp (Index im <code>RunModel.clientTypes</code>-Array)
	 * @see RunModel#clientTypes
	 */
	public int type;

	/**
	 * Kundentyp, der vor der aktuellen Zuweisung vorlag (Index im <code>RunModel.clientTypes</code>-Array).<br>
	 * Wird nur während der Animation verwendet, um das korrekte Icon zu verwenden.
	 * @see RunModel#clientTypes
	 */
	public int typeLast;

	/**
	 * ID der Station, an der der Kunde erzeugt wurde oder der Typ neu zugewiesen wurde
	 */
	public int sourceStationID;

	/**
	 * Handelt es sich um einen Kunden, der während der Warm-Up-Phase erstellt wurde?
	 * Wenn ja, nicht in der Statistik erfassen.
	 */
	public boolean isWarmUp;

	/**
	 * Soll der Kunde in der Statistik erfasst werden?<br>
	 * Dieses Feld kann unabhängig vom Warm-Up-Status eingestellt werden.
	 * Eine Statistikerfassung erfolgt nur, wenn der Kunde kein Warm-Up-Kunde ist und
	 * hier <code>true</code> hinterlegt ist.
	 */
	public boolean inStatistics;

	/**
	 * ID der Station, an der sich der Kunde zuvor befunden hat
	 */
	public int lastStationID;

	/**
	 * ID der Station, an die der Kunde geleitet wird
	 */
	public int nextStationID;

	/**
	 * ID an der der Kunde als letztes angekommen ist
	 */
	public int arrivalProcessedStationID;

	/**
	 * Summe der Wartezeiten des Kunden
	 */
	public long waitingTime;

	/**
	 * Summe der Transferzeiten des Kunden
	 */
	public long transferTime;

	/**
	 * Summe der Bedienzeiten des Kunden
	 */
	public long processTime;

	/**
	 * Summe der Verweilzeiten des Kunden
	 */
	public long residenceTime;

	/**
	 * Zuordnung von Stations-IDs zu Wartezeit-Zeitendauern, die der jeweilige Kunde insgesamt (evtl. in mehreren Anläufen) an der Station war
	 * @see #recordStationTotalClientTimes
	 */
	public Map<Integer,Long> stationTotalWaitingTime;

	/**
	 * Zuordnung von Stations-IDs zu Transferzeit-Zeitendauern, die der jeweilige Kunde insgesamt (evtl. in mehreren Anläufen) an der Station war
	 * @see #recordStationTotalClientTimes
	 */
	public Map<Integer,Long> stationTotalTransferTime;

	/**
	 * Zuordnung von Stations-IDs zu Bedienzeit-Zeitendauern, die der jeweilige Kunde insgesamt (evtl. in mehreren Anläufen) an der Station war
	 * @see #recordStationTotalClientTimes
	 */
	public Map<Integer,Long> stationTotalProcessTime;

	/**
	 * Zuordnung von Stations-IDs zu Verweilzeit-Zeitendauern, die der jeweilige Kunde insgesamt (evtl. in mehreren Anläufen) an der Station war
	 * @see #recordStationTotalClientTimes
	 */
	public Map<Integer,Long> stationTotalResidenceTime;

	/**
	 * Zusätzliche Kosten, die am Ende mit als Wartezeit-Kosten gezählt werden sollen
	 */
	public double waitingAdditionalCosts;

	/**
	 * Zusätzliche Kosten, die am Ende mit als Transferzeit-Kosten gezählt werden sollen
	 */
	public double transferAdditionalCosts;

	/**
	 * Zusätzliche Kosten, die am Ende mit als Bedienzeit-Kosten gezählt werden sollen
	 */
	public double processAdditionalCosts;

	/**
	 * Gibt an, ob der Kunde die Warteschlange erfolgreich durchlaufen hat.
	 */
	public boolean lastQueueSuccess;

	/**
	 * Gibt den Zeitpunkt an, an dem der letzte Wartevorgang gestartet wurde.
	 */
	public long lastWaitingStart;

	/**
	 * Letzter zu zählender Kunde für die Simulation.
	 */
	public boolean isLastClient;

	/**
	 * Wird nur innerhalb der Verarbeitung einer Station optional zum Routen verwendet.<br>
	 * (Verliert also beim Verlassen des Kunden an einer Station seine Gültigkeit.)
	 */
	public int stationInformationInt;

	/**
	 * Wird nur innerhalb der Verarbeitung einer Station optional zum Routen verwendet.<br>
	 * (Verliert also beim Verlassen des Kunden an einer Station seine Gültigkeit.)
	 */
	public long stationInformationLong;

	/**
	 * Wird nur innerhalb der Verarbeitung einer Station optional zum Routen verwendet.<br>
	 * (Verliert also beim Verlassen des Kunden an einer Station seine Gültigkeit.)
	 */
	public double stationInformationDouble;

	/**
	 * Icon für die Darstellung in der Animation (wenn <code>null</code> wird in der Animation das Vorgabe-Icon verwendet)
	 */
	public String icon;

	/**
	 * Icon das vor der Zuweisung aktiv war, für die Darstellung in der Animation (wenn <code>null</code> wird in der Animation das Vorgabe-Icon verwendet).
	 */
	public String iconLast;

	/**
	 * Unsichtbar in der Animation, da Teil eines temporären Batches.
	 */
	public boolean batched;

	/**
	 * Dieses Feld hält die optionalen Nutzerdaten vor.<br>
	 * Beim Zugriff über die getter und setter Methoden wird es automatisch initialisiert.
	 * @see RunDataClient#getUserData(int)
	 * @see RunDataClient#setUserData(int, double)
	 */
	private double[] userData;

	/**
	 * Dieses Feld gibt an, auf welche der Nutzerdatenfelder wirklich schon einmal schreibend zugegriffen wurde.<br>
	 * Beim Zugriff über die setter Methoden wird es automatisch initialisiert.
	 * @see RunDataClient#getUserData(int)
	 * @see RunDataClient#setUserData(int, double)
	 * @see RunDataClient#writeUserDataToStatistics(String, StatisticsMultiPerformanceIndicator, StatisticsMultiPerformanceIndicator)
	 */
	private boolean[] userDataInUse;

	/**
	 * Dieses Feld hält die optionalen Text-Nutzerdaten vor.<br>
	 * Beim Zugriff über die getter und setter Methoden wird es automatisch initialisiert.
	 * @see RunDataClient#getUserDataString(String)
	 * @see RunDataClient#setUserDataString(String, String)
	 */
	private Map<String,String> userDataStrings;

	/**
	 * Wird bei der Bedienung eines Kunden an einer Bedienstation gesetzt und gibt an,
	 * welche Ressourcenalternative zur Bedienung des Kunden gewählt wurde.<br>
	 * (1-basierend; vor der ersten Bedienung steht das Feld auf 0.)
	 */
	public int lastAlternative;

	/**
	 * Name des Kunden fürs Logging
	 * @see RunDataClient#logInfo(SimulationData)
	 */
	private String cacheLogName;

	/**
	 * Typ des Kunden fürs Logging
	 * @see RunDataClient#logInfo(SimulationData)
	 */
	private int cachedType;

	/**
	 * Ist diese Feld ungleich <code>null</code>, so handelt es sich bei diesem
	 * Kunden in Wirklichkeit um einen Batch.
	 */
	private List<RunDataClient> batch;

	/**
	 * Wird {@link #dissolveBatch()} aufgerufen, so wird die Liste
	 * mit den gebatchten Kunden, d.h. das Feld <code>batch</code>,
	 * zurückgeliefert. Der Wert ist damit für dieses Objekt nicht
	 * mehr gültig. Um das Array jedoch wiederverwenden zu können,
	 * wird es nicht auf <code>null</code> gesetzt, sondern als
	 * ungültig deklariert. Weitere Aufrufe von {@link #dissolveBatch()}
	 * liefern dann <code>null</code>, beim Kopieren werden die
	 * verworfenen Daten nicht mit kopiert und bei {@link #addBatchClient(RunDataClient)}
	 * wird die Liste per <code>clear()</code> gelöscht.
	 */
	private boolean batchMarkedAsDone;

	/**
	 * Nummer des aktuellen Sequenz-Plans
	 */
	public int sequenceNr;

	/**
	 * Aktueller Schritt im gewählten Sequenz-Plan<br>
	 * (Die Nummer gibt die nächste zu verwendende Sequenz an.)
	 */
	public int sequenceStep;

	/**
	 * Liste der Bereiche, in denen sich der Kunde gerade befindet.
	 * (Die ganze Liste kann <code>null</code> sein, wenn der Kunde (noch)
	 * in keinem Bereich ist.)
	 */
	private List<RunElementSectionStart> sections;

	/**
	 * Hält den aktuellen Wert der Wartezeit des Kunden beim Betreten eines Bereichs fest,
	 * um so beim Verlassen des Bereichs ermitteln zu können, wie viel Wartezeit in dem
	 * Bereich entstanden ist.
	 * (Die ganze Liste kann <code>null</code> sein, wenn der Kunde (noch)
	 * in keinem Bereich ist. Wenn die Wartezeit beim Betreten des Bereichs 0 war,
	 * wird kein Wert in die HashMap aufgenommen.)
	 */
	public Map<RunElementSectionStart,Long> sectionEnterWaitingTime;

	/**
	 * Hält den aktuellen Wert der Transferzeit des Kunden beim Betreten eines Bereichs fest,
	 * um so beim Verlassen des Bereichs ermitteln zu können, wie viel Transferzeit in dem
	 * Bereich entstanden ist.
	 * (Die ganze Liste kann <code>null</code> sein, wenn der Kunde (noch)
	 * in keinem Bereich ist. Wenn die Transferzeit beim Betreten des Bereichs 0 war,
	 * wird kein Wert in die HashMap aufgenommen.)
	 */
	public Map<RunElementSectionStart,Long> sectionEnterTransferTime;

	/**
	 * Hält den aktuellen Wert der Bedienzeit des Kunden beim Betreten eines Bereichs fest,
	 * um so beim Verlassen des Bereichs ermitteln zu können, wie viel Bedienzeit in dem
	 * Bereich entstanden ist.
	 * (Die ganze Liste kann <code>null</code> sein, wenn der Kunde (noch)
	 * in keinem Bereich ist. Wenn die Bedienzeit beim Betreten des Bereichs 0 war,
	 * wird kein Wert in die HashMap aufgenommen.)
	 */
	public Map<RunElementSectionStart,Long> sectionEnterProcessTime;

	/**
	 * Hält den aktuellen Wert der Verweilzeit des Kunden beim Betreten eines Bereichs fest,
	 * um so beim Verlassen des Bereichs ermitteln zu können, wie viel Verweilzeit in dem
	 * Bereich entstanden ist.
	 * (Die ganze Liste kann <code>null</code> sein, wenn der Kunde (noch)
	 * in keinem Bereich ist. Wenn die Verweilzeit beim Betreten des Bereichs 0 war,
	 * wird kein Wert in die HashMap aufgenommen.)
	 */
	public Map<RunElementSectionStart,Long> sectionEnterResidenceTime;

	/**
	 * Gibt an, wenn sich der Kunde in einem Logik-Abschnitt befindet,
	 * ob der If-Zweig ausgeführt wurde oder ob noch ein Else ausgeführt
	 * werden soll.
	 */
	private List<Boolean> logic;

	/**
	 * Gibt an, ob der Kunde bei der nächsten Station bereits vorangekündigt wurde.
	 * @see RunElementData#announcedClient
	 */
	public boolean isAnnouncedToStation;

	/**
	 * Erfasste Schritte in der Pfadaufzeichnung.
	 * Ist anfänglich <code>null</code>. Wird erst bei Nutzung initialisiert.
	 * @see #recordPathStep(int)
	 */
	private int[] pathRecording;

	/**
	 * Anzahl der genutzten Einträge im Pfadaufzeichnungsarray.
	 * @see #recordPathStep(int)
	 */
	private int pathRecordingUsed;

	/**
	 * Erfassung der jeweils gesamten Zeiten eines Kunden an einer Station (d.h. mehrere Bedienungen desselben Kunden an einer Station als Summe)?
	 * @see RunModel#recordStationTotalClientTimes
	 */
	private final boolean recordStationTotalClientTimes;

	/**
	 * Konstruktor des <code>RunDataClient</code>
	 * @param type	Kundentyp (Index im <code>RunModel.clientTypes</code>-Array)
	 * @param isWarmUp	Ist der Kunde während der Warm-Up-Phase eingetroffen?
	 * @param recordStationTotalClientTimes	Erfassung der jeweils gesamten Zeiten eines Kunden an einer Station (d.h. mehrere Bedienungen desselben Kunden an einer Station als Summe)?
	 * @param clientNumber	Fortlaufende Nummer der Kunden (wird von <code>RunDataClients.getClient()</code> gezählt)
	 */
	public RunDataClient(final int type, final boolean isWarmUp, final boolean recordStationTotalClientTimes, final long clientNumber) {
		this.recordStationTotalClientTimes=recordStationTotalClientTimes;
		init(type,isWarmUp,clientNumber);
	}

	/**
	 * Reinitialisiert das Objekt (d.h. setzt alle Werte auf 0 zurück)
	 * @param type	Kundentyp (Index im <code>RunModel.clientTypes</code>-Array)
	 * @param isWarmUp	Ist der Kunde während der Warm-Up-Phase eingetroffen?
	 * @param clientNumber	Fortlaufende Nummer der Kunden (wird von <code>RunDataClients.getClient()</code> gezählt)
	 */
	public void init(final int type, final boolean isWarmUp, final long clientNumber) {
		lastStationID=-1;
		nextStationID=-1;
		arrivalProcessedStationID=-1;
		this.type=type;
		this.typeLast=type;
		this.isWarmUp=isWarmUp;
		this.inStatistics=true;
		this.clientNumber=clientNumber;
		waitingTime=0;
		transferTime=0;
		processTime=0;
		residenceTime=0;
		if (recordStationTotalClientTimes) {
			if (stationTotalWaitingTime==null) stationTotalWaitingTime=new HashMap<>(); else stationTotalWaitingTime.clear();
			if (stationTotalTransferTime==null) stationTotalTransferTime=new HashMap<>(); else stationTotalTransferTime.clear();
			if (stationTotalProcessTime==null) stationTotalProcessTime=new HashMap<>(); else stationTotalProcessTime.clear();
			if (stationTotalResidenceTime==null) stationTotalResidenceTime=new HashMap<>(); else stationTotalResidenceTime.clear();
		}
		waitingAdditionalCosts=0;
		transferAdditionalCosts=0;
		processAdditionalCosts=0;
		isLastClient=false;
		icon=null;
		iconLast=null;
		if (userData!=null && userData.length>0) for (int i=0;i<userData.length;i++) userData[i]=0.0;
		if (userDataInUse!=null && userDataInUse.length>0) for (int i=0;i<userDataInUse.length;i++) userDataInUse[i]=false;
		if (userDataStrings!=null) userDataStrings.clear();
		cacheLogName=null;
		lastAlternative=0;
		batched=false;
		sequenceNr=-1;
		sequenceStep=0;
		if (sections!=null) sections.clear();
		if (sectionEnterWaitingTime!=null) sectionEnterWaitingTime.clear();
		if (sectionEnterTransferTime!=null) sectionEnterTransferTime.clear();
		if (sectionEnterProcessTime!=null) sectionEnterProcessTime.clear();
		if (sectionEnterResidenceTime!=null) sectionEnterResidenceTime.clear();
		if (logic!=null) logic.clear();
		isAnnouncedToStation=false;
		pathRecordingUsed=0;
	}

	/**
	 * Kopiert die Daten, die nicht bereits dem Konstruktor übergeben werden aus einem anderen <code>RunDataClient</code>-Objekt in dieses.
	 * @param client	Quellobjekt, aus dem die Daten übernommen werden sollen
	 * @param simData	Simulationsdatenobjekt
	 * @param listObject	Objekt vom Typ {@link RunDataClients}, welches Hilfsroutinen für Kunden bereithält
	 */
	public void copyDataFrom(final RunDataClient client, final SimulationData simData, final RunDataClients listObject) {
		/* wird nicht kopiert - clientNumber */
		/* wird nicht kopiert - type */
		/* wird nicht kopiert - isWarmUp */
		inStatistics=client.inStatistics;
		/* wird nicht kopiert - lastStationID */
		/* wird nicht kopiert - nextStationID */
		waitingTime=client.waitingTime;
		transferTime=client.transferTime;
		processTime=client.processTime;
		residenceTime=client.residenceTime;
		if (recordStationTotalClientTimes) {
			stationTotalWaitingTime.clear();
			stationTotalWaitingTime.putAll(client.stationTotalWaitingTime);
			stationTotalTransferTime.clear();
			stationTotalTransferTime.putAll(client.stationTotalTransferTime);
			stationTotalProcessTime.clear();
			stationTotalProcessTime.putAll(client.stationTotalProcessTime);
			stationTotalResidenceTime.clear();
			stationTotalResidenceTime.putAll(client.stationTotalResidenceTime);
		}
		waitingAdditionalCosts=client.waitingAdditionalCosts;
		transferAdditionalCosts=client.transferAdditionalCosts;
		processAdditionalCosts=client.processAdditionalCosts;
		lastQueueSuccess=client.lastQueueSuccess;
		lastWaitingStart=client.lastWaitingStart;
		/* wird nicht kopiert - isLastClient */
		/* wird nicht kopiert - stationInformation */
		icon=client.icon;
		iconLast=client.iconLast;
		batched=client.batched;
		if (client.userData==null || client.userData.length==0) userData=null; else userData=Arrays.copyOf(client.userData,client.userData.length);
		if (client.userDataInUse==null || client.userDataInUse.length==0) userDataInUse=null; else userDataInUse=Arrays.copyOf(client.userDataInUse,client.userDataInUse.length);
		if (client.userDataStrings==null || client.userDataStrings.size()==0) {
			userDataStrings=null;
		} else {
			userDataStrings=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			userDataStrings.putAll(client.userDataStrings);
		}
		lastAlternative=client.lastAlternative;
		/* wird nicht kopiert - cacheLogName */
		if (client.batch==null || client.batchMarkedAsDone) batch=null; else batch=client.batch.stream().map(c->listObject.getClone(c,simData)).collect(Collectors.toList());
		sequenceNr=client.sequenceNr;
		sequenceStep=client.sequenceStep;
		/* wird nicht kopiert - sections */
		if (client.logic!=null && !client.logic.isEmpty()) logic=new ArrayList<>(client.logic);
		isAnnouncedToStation=false;

		if (client.pathRecordingUsed>0) {
			if (pathRecording==null || pathRecording.length<pathRecordingUsed) {
				pathRecording=Arrays.copyOf(client.pathRecording,client.pathRecording.length);
			} else {
				for (int i=0;i<client.pathRecordingUsed;i++) pathRecording[i]=client.pathRecording[i];
			}
		}
		pathRecordingUsed=client.pathRecordingUsed;
	}

	/**
	 * Erfasst eine Zeitdauer für den Kunden.
	 * @param stationID	Stations-ID
	 * @param addWaitingTimeMS	Wartezeit (in MS; wird zur bisherigen addiert)
	 * @param addTransferTimeMS	Transferzeit (in MS; wird zur bisherigen addiert)
	 * @param addProcessTimeMS	Bedienzeit (in MS; wird zur bisherigen addiert)
	 * @param addResidenceTimeMS	Verweilzeit (in MS; wird zur bisherigen addiert)
	 */
	public void addStationTime(final int stationID, final long addWaitingTimeMS, final long addTransferTimeMS, final long addProcessTimeMS, final long addResidenceTimeMS) {
		waitingTime+=addWaitingTimeMS;
		transferTime+=addTransferTimeMS;
		processTime+=addProcessTimeMS;
		residenceTime+=addResidenceTimeMS;

		if (recordStationTotalClientTimes) {
			/* Zeiten==0 dürfen nicht weggelassen werden, weil diese sonst evtl. in der Zählung am Ende fehlen */
			stationTotalWaitingTime.compute(stationID,(k,v)->(v==null)?addWaitingTimeMS:v+addWaitingTimeMS);
			stationTotalTransferTime.compute(stationID,(k,v)->(v==null)?addTransferTimeMS:v+addTransferTimeMS);
			stationTotalProcessTime.compute(stationID,(k,v)->(v==null)?addProcessTimeMS:v+addProcessTimeMS);
			stationTotalResidenceTime.compute(stationID,(k,v)->(v==null)?addResidenceTimeMS:v+addResidenceTimeMS);
		}
	}

	/**
	 * Liefert den Wert eines bestimmten Nutzerdaten-Feldes.
	 * @param index	Index des Feldes, das abgefragt werden soll. Der Index muss zwischen (einschließlich) 0 und <code>MAX_USER_DATA_INDEX</code> liegen.
	 * @return	Liefert den Wert des Feldes oder 0, wenn noch kein Wert für dieses Feld gesetzt wurde bzw. es außerhalb des gültiges Bereiches liegt.
	 * @see RunDataClient#MAX_USER_DATA_INDEX
	 */
	public double getUserData(final int index) {
		if (userData==null || index<0 || index>=userData.length) return 0.0;
		return userData[index];
	}

	/**
	 * Liefert den höchsten <em>Index</em> innerhalb der numerischen Kundendaten
	 * (d.h. mögliche Werte bewegen sich im Bereich von 0 bis zu dem angegebenen Index, d.h. das Kundendatenarray ist um ein größer als der höchste Index)
	 * @return	Höchster verfügbarer Index (kann -1 sein, wenn überhaupt keine numerischen Kundendaten gespeichert sind)
	 */
	public int getMaxUserDataIndex() {
		if (userData==null) return -1;
		return userData.length-1;
	}

	/**
	 * Setzt den Wert eines bestimmten Nutzerdaten-Feldes.
	 * @param index	Index des Feldes, dessen wert gesetzt werden soll. Der Index muss zwischen (einschließlich) 0 und <code>MAX_USER_DATA_INDEX</code> liegen.
	 * @param value	Neuer Wert für das Feld
	 * @see RunDataClient#MAX_USER_DATA_INDEX
	 */
	public void setUserData(final int index, final double value) {
		if (index<0 || index>MAX_USER_DATA_INDEX) return;

		if (userData==null) userData=new double[index+1];
		if (userDataInUse==null) userDataInUse=new boolean[index+1];
		if (userData.length<=index) userData=Arrays.copyOf(userData,index+1);
		if (userDataInUse.length<=index) userDataInUse=Arrays.copyOf(userDataInUse,index+1); /* Nur für die Statistik wichtig. Nicht genutzte Felder sind immer 0. */

		userData[index]=value;
		userDataInUse[index]=true;
	}

	/**
	 * Liefert alle numerischen Kundendaten.
	 * @param destArray	Array in das, wenn passend, die Daten kopiert werden
	 * @return	Neues Array oder inhaltlich aktualisiertes bisheriges Array
	 * @see #copyUserDataInUse(boolean[])
	 * @see #setUserData(double[], boolean[])
	 */
	public double[] copyUserData(double[] destArray) {
		if (userData==null) {
			if (destArray!=null) Arrays.fill(destArray,0);
			return destArray;
		}

		final int userDataLength=userData.length;

		if (destArray!=null && destArray.length>=userDataLength) {
			for (int i=0;i<userDataLength;i++) destArray[i]=userData[i];
			for (int i=userDataLength;i<destArray.length;i++) destArray[i]=0.0;
		} else {
			destArray=Arrays.copyOf(userData,userDataLength);
		}

		return destArray;
	}

	/**
	 * Liefert die Nutzungsflags für alle numerischen Kundendaten.
	 * @param destArray	Array in das, wenn passend, die Daten kopiert werden
	 * @return	Neues Array oder inhaltlich aktualisiertes bisheriges Array
	 * @see #copyUserDataInUse(boolean[])
	 * @see #setUserData(double[], boolean[])
	 */
	public boolean[] copyUserDataInUse(boolean[] destArray) {
		if (userData==null) {
			if (destArray!=null) Arrays.fill(destArray,false);
			return destArray;
		}

		final int userDataInUseLength=userDataInUse.length;

		if (destArray!=null && destArray.length>=userDataInUseLength) {
			for (int i=0;i<userDataInUseLength;i++) destArray[i]=userDataInUse[i];
			for (int i=userDataInUseLength;i<destArray.length;i++) destArray[i]=false;
		} else {
			destArray=Arrays.copyOf(userDataInUse,userDataInUseLength);
		}

		return destArray;
	}

	/**
	 * Stellt alle numerischen Kundendaten inkl. der zugehörigen Nutzungsflags ein.
	 * @param userData	Numerische Kundendaten
	 * @param userDataInUse	Nutzungsflags zu den Daten
	 * @see #copyUserData(double[])
	 * @see #copyUserDataInUse(boolean[])
	 */
	public void setUserData(final double[] userData, final boolean[] userDataInUse) {
		if (userData!=null) {
			if (this.userData!=null && this.userData.length>=userData.length) {
				final int size=userData.length;
				for (int i=0;i<size;i++) this.userData[i]=userData[i];
				for (int i=size;i<this.userData.length;i++) this.userData[i]=0.0;
			} else {
				this.userData=Arrays.copyOf(userData,userData.length);
			}
		}
		if (userDataInUse!=null) {
			if (this.userDataInUse!=null && this.userDataInUse.length>=userDataInUse.length) {
				final int size=userDataInUse.length;
				for (int i=0;i<size;i++) this.userDataInUse[i]=userDataInUse[i];
				for (int i=size;i<this.userDataInUse.length;i++) this.userDataInUse[i]=false;
			} else {
				this.userDataInUse=Arrays.copyOf(userDataInUse,userDataInUse.length);
			}
		}
	}

	/**
	 * Erfasst die Werte der Nutzerdaten-Felder in der Statistik.
	 * @param clientTypeName	Name des Kundentyps
	 * @param indicators	Statistikobjekt (welches Unterobjekte vom Typ {@link StatisticsDataPerformanceIndicator} enthält), in dem die Daten globalen über alle Kundentypen erfasst werden sollen
	 * @param indicatorsByClientType	Statistikobjekt (welches Unterobjekte vom Typ {@link StatisticsDataPerformanceIndicator} enthält), in dem die Daten pro Kundentyp erfasst werden sollen
	 */
	public void writeUserDataToStatistics(final String clientTypeName, final StatisticsMultiPerformanceIndicator indicators, final StatisticsMultiPerformanceIndicator indicatorsByClientType) {
		if (userData==null || userData.length==0 || userDataInUse==null || userDataInUse.length==0 || indicators==null) return;

		for (int i=0;i<FastMath.min(userData.length,userDataInUse.length);i++) if (userDataInUse[i]) {
			final double value=userData[i];
			final String name=NumberTools.formatLongNoGrouping(i);
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicators.get(name);
			indicator.add(value);
			final StatisticsDataPerformanceIndicatorWithNegativeValues indicatorByClientType=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicatorsByClientType.get(name+"-"+clientTypeName);
			indicatorByClientType.add(value);
		}
	}

	/**
	 * Liefert den Wert eines bestimmten Nutzerdaten-Text-Feldes.
	 * @param key	Name des Feldes
	 * @return	Liefert den Wert des Feldes oder einen leeren String, wenn noch kein Wert für dieses Feld gesetzt wurde.
	 */
	public String getUserDataString(final String key) {
		if (userDataStrings==null || key==null) return "";
		return userDataStrings.getOrDefault(key,"");
	}

	/**
	 * Setzt den Wert eines bestimmten Nutzerdaten-Text-Feldes.
	 * @param key	Name des Feldes
	 * @param value	Neuer Wert für das Feld
	 */
	public void setUserDataString(final String key, final String value) {
		if (userDataStrings==null) userDataStrings=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		if (key==null) return;
		userDataStrings.put(key,value);
	}

	/**
	 * Liefert eine Menge mit den Schlüsseln für die Nutzerdaten-Texte vorliegen.
	 * @return	Schlüssel zu denen Nutzerdaten-Texte vorliegen
	 * @see #getUserDataString(String)
	 */
	public Set<String> getUserDataStringKeys() {
		if (userDataStrings==null) return new HashSet<>();
		return userDataStrings.keySet();
	}

	/**
	 * Liefert eine Kopie aller Nutzerdaten-Texte.
	 * @return	Kopie aller Nutzerdaten-Texte
	 */
	public Map<String,String> getUserDataStringsCopy() {
		if (userDataStrings==null) return null;
		return new HashMap<>(userDataStrings);
	}

	/**
	 * Setzt den Wert eines oder mehrerer Nutzerdaten-Text-Felder.
	 * @param data	Zuordnung, die auf den Kunden übertragen werden soll.
	 */
	public void setUserDataStrings(final Map<String,String> data) {
		if (userDataStrings==null) userDataStrings=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		if (data==null) return;
		userDataStrings.putAll(data);
	}

	/**
	 * Ersetzt die Werte der Nutzerdaten-Text-Felder.
	 * @param data	Zuordnung, die auf den Kunden übertragen werden soll.
	 */
	public void setUserDataStringsReplace(final Map<String,String> data) {
		if (userDataStrings==null) userDataStrings=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		userDataStrings.clear();
		if (data==null) return;
		userDataStrings.putAll(data);
	}

	/**
	 * Liefert den Namen des Kunden (bestehend aus Typenname und ID) für das Logging
	 * @param simData	Simulationsdatenobjekt
	 * @return	Bezeichner dieses Kunden fürs Logging
	 */
	public String logInfo(final SimulationData simData) {
		if (cacheLogName==null || cachedType!=type) {
			final StringBuilder sb=new StringBuilder();
			sb.append("\"");
			sb.append(simData.runModel.clientTypes[type]);
			sb.append("\" (id=");
			sb.append(hashCode());
			sb.append(")");
			cacheLogName=sb.toString();
			cachedType=type;
		}
		return cacheLogName;
	}

	/**
	 * Macht aus dem Kunden einen Batch und fügt einen Kunden zu dem Batch hinzu
	 * @param subClient	Kunde, der in den Batch aufgenommen werden soll.
	 */
	public void addBatchClient(final RunDataClient subClient) {
		if (batch==null) {
			batch=new ArrayList<>();
		} else {
			if (batchMarkedAsDone) batch.clear();
		}
		batchMarkedAsDone=false;
		batch.add(subClient);
		subClient.batched=true;
	}

	/**
	 * Liefert eine Liste aller Kunden innerhalb dieses Kunden.
	 * @return	In diesem Kunden als Batch enthaltene Kunden oder <code>null</code>, wenn es sich nicht um ein Batch handelt.
	 */
	public List<RunDataClient> getBatchData() {
		if (batch==null || batchMarkedAsDone) return null;
		return batch;
	}

	/**
	 * Liefert einen in diesem Batch enthaltenen Kunden.
	 * @param index	0-basierter Index des Kunden in dem Batch
	 * @return	Liefert den Kunden oder <code>null</code>, wenn der äußere Kunde kein temporärer Batch ist oder der Index außerhalb des gültigen Bereichs liegt
	 * @see #getBatchData()
	 */
	public RunDataClient getBatchData(final int index) {
		if (batch==null || batchMarkedAsDone) return null;
		if (index<0 || index>=batch.size()) return null;
		return batch.get(index);
	}

	/**
	 * Löst den Batch auf und gibt die einzelnen Kunden als Liste zurück.<br>
	 * Die im Batch angesammelten Zeiten und Kosten werden dabei auf die einzelnen Kunden übertragen.
	 * @return	Liste der einzelnen Kunden oder <code>null</code> wenn der aktuelle Kunde kein Batch ist.
	 */
	public List<RunDataClient> dissolveBatch() {
		if (batch==null || batchMarkedAsDone) return null;

		batchMarkedAsDone=true;

		final int size=batch.size();
		for (int i=0;i<size;i++) {
			final RunDataClient client=batch.get(i);

			client.waitingTime+=waitingTime;
			client.transferTime+=transferTime;
			client.processTime+=processTime;
			client.residenceTime+=residenceTime;
			if (client.recordStationTotalClientTimes) {
				for (Map.Entry<Integer,Long> entry: stationTotalWaitingTime.entrySet()) client.stationTotalWaitingTime.merge(entry.getKey(),entry.getValue(),Long::sum);
				for (Map.Entry<Integer,Long> entry: stationTotalTransferTime.entrySet()) client.stationTotalTransferTime.merge(entry.getKey(),entry.getValue(),Long::sum);
				for (Map.Entry<Integer,Long> entry: stationTotalProcessTime.entrySet()) client.stationTotalProcessTime.merge(entry.getKey(),entry.getValue(),Long::sum);
				for (Map.Entry<Integer,Long> entry: stationTotalResidenceTime.entrySet()) client.stationTotalResidenceTime.merge(entry.getKey(),entry.getValue(),Long::sum);
			}
			client.waitingAdditionalCosts+=waitingAdditionalCosts;
			client.transferAdditionalCosts+=transferAdditionalCosts;
			client.processAdditionalCosts+=processAdditionalCosts;
			client.batched=false;
			if (lastAlternative>0) client.lastAlternative=lastAlternative;
			client.lastStationID=lastStationID;
			client.nextStationID=nextStationID;
			client.arrivalProcessedStationID=arrivalProcessedStationID;
			client.sequenceNr=sequenceNr;
			client.sequenceStep=sequenceStep;
			for (int j=0;j<pathRecordingUsed;j++) client.recordPathStep(pathRecording[j]);
		}

		return batch;
	}

	/**
	 * Trägt den Bereich in die Liste der Bereiche, in denen sich
	 * der Kunde befindet, ein.<br>
	 * Mit dem Element selbst interagiert diese Funktion nicht.
	 * @param section	Bereich, in dem sich der Kunde jetzt befinden soll
	 */
	public void enterSection(final RunElementSectionStart section) {
		if (sections==null) sections=new ArrayList<>();
		if (!sections.contains(section)) sections.add(section);

		if (sectionEnterWaitingTime==null) sectionEnterWaitingTime=new HashMap<>();
		if (waitingTime>0) sectionEnterWaitingTime.put(section,waitingTime);

		if (sectionEnterTransferTime==null) sectionEnterTransferTime=new HashMap<>();
		if (transferTime>0) sectionEnterTransferTime.put(section,transferTime);

		if (sectionEnterProcessTime==null) sectionEnterProcessTime=new HashMap<>();
		if (processTime>0) sectionEnterProcessTime.put(section,processTime);

		if (sectionEnterResidenceTime==null) sectionEnterResidenceTime=new HashMap<>();
		if (residenceTime>0) sectionEnterResidenceTime.put(section,residenceTime);
	}

	/**
	 * Trägt den Kunden aus einem bestimmten Bereich aus (sofern er sich überhaupt
	 * in diesem befunden hat) und benachrichtigt diesen entsprechend.
	 * @param section	Bereich in dem sich der Kunde nicht mehr befindet
	 * @param simData	Simulationsdatenobjekt
	 * @return	Gibt <code>true</code> zurück, wenn der Kunde zuvor in diesem Bereich war
	 */
	public boolean leaveSection(final RunElementSectionStart section, final SimulationData simData) {
		if (sections==null) return false;
		final int index=sections.indexOf(section);
		if (index<0) return false;
		section.notifyClientLeavesSection(simData,this);
		sections.remove(index);
		return true;
	}

	/**
	 * Trägt den Kunden (am Ende seiner Lebensdauer) aus allen Bereichen
	 * aus und benachrichtigt diese entsprechend.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void leaveAllSections(final SimulationData simData) {
		if (sections!=null) {
			/* Verbraucht viel Speicher: for (RunElementSectionStart section: sections) { */
			final int size=sections.size();
			for (int i=0;i<size;i++) {
				final RunElementSectionStart section=sections.get(i);
				section.notifyClientLeavesSection(simData,this);
			}
			sections.clear();
			simData.runData.fireStateChangeNotify(simData);
		}
	}

	/**
	 * Erfasst, dass der Kunde einen If-Abschnitt betreten hat
	 * und ob die Bedingung erfüllt ist (oder ob spätere Else/ElseIf-Abschnitte
	 * angesteuert werden sollen).
	 * @param conditionOk	Bedingung der If-Abfrage erfüllt
	 */
	public void enterLogic(final boolean conditionOk) {
		if (logic==null) logic=new ArrayList<>();
		logic.add(conditionOk);
	}

	/**
	 * Gibt an, ob die Bedingung der aktuellen If-Abfrage (oder eines ElseIf-Zweiges)
	 * erfüllt ist oder ob weitere Else/ElseIf-Zweige noch von Bedeutung sind.
	 * @return	Gibt <code>true</code> zurück, wenn ein früherer Zweig der Bedingungskette bereits abgearbeitet wurde.
	 */
	public boolean testLogic() {
		if (logic==null) return false;
		final int size=logic.size();
		if (size==0) return false;
		return logic.get(size-1);
	}

	/**
	 * Stellt ein, dass die Bedingung in einem ElseIf-Zweig erfüllt ist und
	 * damit weitere ElseIf/Else-Zweige übersprungen werden können.
	 */
	public void updateLogic() {
		if (logic==null) return;
		final int size=logic.size();
		if (size==0) return;
		logic.set(size-1,true);
	}

	/**
	 * Wird von EndIf aufgerufen und gibt an, dass er Kunde den Bereich
	 * einer verketteten Bedingung verlassen hat.
	 */
	public void leaveLogic() {
		if (logic==null) return;
		final int size=logic.size();
		if (size==0) return;
		logic.remove(size-1);
	}

	/**
	 * Trägt den Kunden (am Ende seiner Lebensdauer) aus allen Logik-Bereichen aus.
	 */
	public void leaveAllLogic() {
		if (logic!=null) logic.clear();
	}

	/**
	 * Ändert den Typ des aktuellen Kunden und benachrichtigt die Statistik für die
	 * Zählung der Anzahl an Kunden im System pro Typ. Beim Erstellen von Kunden
	 * ({@link RunDataClients#getClient(int, SimulationData, int)}) und bei der Freigabe von
	 * Kunden ({@link RunDataClients#disposeClientWithoutStatistics(RunDataClient, SimulationData)})
	 * erfolgt diese Statistikzählung automatisch.
	 * @param newType	Neuer Kundentyp
	 * @param simData	Simulationsdatenobjekt
	 * @param stationID	ID der Station, an der die Zuweisung erfolgt ist
	 * @see #type
	 * @see #typeLast
	 */
	public void changeType(final int newType, final SimulationData simData, final int stationID) {
		if (type==newType) return;

		typeLast=type;
		simData.runData.logClientsInSystemChange(simData,type,-1);
		type=newType;
		simData.runData.logClientsInSystemChange(simData,type,1);

		sourceStationID=stationID;
	}

	/**
	 * Erfasst für die Pfadaufzeichnung, dass sich der aktuelle Kunde
	 * an einer bestimmten Station befindet. Es wird geprüft, die
	 * Statistikerfassung für den aktuellen Kunden überhaupt aktiv ist,
	 * aber es wird nicht geprüft, ob die Pfadaufzeichnung als solches
	 * aktiv ist. Dies muss der Aufrufer übernehmen.
	 * @param id	ID der Station an der sich der Kunde befindet
	 */
	public void recordPathStep(int id) {
		if (isWarmUp || !inStatistics) return;
		if (pathRecording==null) pathRecording=new int[10];
		if (pathRecording.length==pathRecordingUsed) pathRecording=Arrays.copyOf(pathRecording,pathRecording.length+20);
		pathRecording[pathRecordingUsed]=id;
		pathRecordingUsed++;
	}

	/**
	 * Liefert den Pfad, den der Kunden eingeschlagen hat, als Zeichenkette
	 * @param simData	Simulationsdatenobjekt
	 * @return	Pfad des Kunden als Zeichenkette
	 */
	private String buildPathName(final SimulationData simData) {
		final StringBuilder result=new StringBuilder();
		for (int i=0;i<pathRecordingUsed;i++) {
			if (i>0) result.append(" -> ");
			result.append(simData.runModel.elementsFast[pathRecording[i]].name);
		}
		return result.toString();
	}

	/**
	 * Liefert die IDs der Stationen, die der Kunde durchlaufen hat
	 * @return	IDs des Pfades (kann <code>null</code> sein, wenn es keinen Pfad gibt)
	 */
	public int[] getPath() {
		if (pathRecording==null || pathRecordingUsed==0) return null;
		return Arrays.copyOf(pathRecording,pathRecordingUsed);
	}

	/**
	 * Schreibt den evtl. erfassten Pfad für den Kunden in die Statistik.
	 * Muss am Ende des Lebenszyklus des Kundenobjektes aufgerufen werden.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void storePathToStatistics(final SimulationData simData) {
		if (pathRecording==null || pathRecordingUsed==0) return;

		((StatisticsSimpleCountPerformanceIndicator)simData.statistics.clientPaths.get(buildPathName(simData))).add();
	}
}
