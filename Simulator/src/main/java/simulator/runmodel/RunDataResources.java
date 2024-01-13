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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;

import statistics.StatisticsTimePerformanceIndicator;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelResources.SecondaryResourcePriority;
import ui.modeleditor.ModelSchedules;

/**
 * Enthält Informationen darüber, wie viele Bediener welchen Typs momentan verfügbar sind
 * @author Alexander Herzog
 */
public final class RunDataResources implements Cloneable {
	/** Namen aller im System vorhandenen Bedienergruppen */
	private String[] names;
	/** Bedienergruppen */
	private RunDataResource[] list;
	/** Listener, die benachrichtigt werden sollen, wenn sich in einer Gruppe die Anzahl an Bedienern geändert hat */
	private List<Consumer<SimulationData>> resourceCountChangeListeners=new ArrayList<>();

	/**
	 * Art der Bestimmung der Stationsreihenfolge in Bezug auf die Ressourcen-Priorität bei Gleichstand in der ersten Ebene
	 * @see SecondaryResourcePriority
	 */
	public ModelResources.SecondaryResourcePriority secondaryResourcePriority;

	/**
	 * Konstruktor der Klasse
	 */
	public RunDataResources() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Lädt die Laufzeit-Ressourcen-Daten aus einem Editor-Ressourcen-Objekt
	 * @param resources	Editor-Ressourcen-Objekt, aus dem die Informationen, wie viele Bediener welchen Typs vorhanden sind, ausgelesen werden sollen
	 * @param schedules	Editor-Zeitpläne-Objekt, aus dem ebenfalls Informationen, wie viele Bediener welchen Typs wann vorhanden sind, ausgelesen werden sollen
	 * @param variables	Liste der verfügbaren Variablen
	 * @param runModel	Laufzeitmodell
	 * @return Gibt <code>null</code> zurück, wenn die Ressourcendaten korrekt geladen werden konnten, sonst eine Fehlermeldung.
	 */
	public String loadFromEditResources(final ModelResources resources, final ModelSchedules schedules, final String[] variables, final RunModel runModel) {
		secondaryResourcePriority=resources.secondaryResourcePriority;
		names=resources.list();
		list=new RunDataResource[names.length];

		for (int i=0;i<list.length;i++) {
			list[i]=new RunDataResource(runModel);
			final String error=list[i].loadFromResource(resources.get(names[i]),schedules,variables,runModel);
			if (error!=null) return error;
		}

		return null;
	}

	@Override
	public RunDataResources clone() {
		final RunDataResources clone=new RunDataResources();

		clone.secondaryResourcePriority=secondaryResourcePriority;
		clone.names=Arrays.copyOf(names,names.length);
		clone.list=new RunDataResource[list.length];
		for (int i=0;i<list.length;i++) clone.list[i]=list[i].clone();
		clone.resourceCountChangeListeners.addAll(resourceCountChangeListeners);

		return clone;
	}

	/**
	 * Bildet basierend auf den Namen der für die Bedienung der Kunden notwendigen Agentengruppen (und Anzahlen von Agenten)
	 * ein Array mit den Ressoucenzuordnungen. Jeder Eintrag entspricht einem der Ressourcen-Objekt-Datenstätze. Werte von 0
	 * bedeuten, dass die Ressource nicht benötigt wird.
	 * @param neededResources	Zuordnung von Ressourcennamen und benötigten Anzahlen
	 * @return	Array mit den Ressourcenbedarfszuordnungsvarianten (oder im Fehlerfall <code>null</code>)
	 */
	public int[][] getNeededResourcesRecord(final List<Map<String,Integer>> neededResources) {
		/* Wurden überhaupt Ressourcen definiert? */
		if (neededResources==null || neededResources.isEmpty()) return null;
		int count=0;
		for (Map<String,Integer> rec: neededResources) count+=rec.size();
		if (count==0) return null;

		final int[][] record=new int[FastMath.max(neededResources.size(),1)][];

		for (int i=0;i<neededResources.size();i++) {
			record[i]=getNeededResourcesRecord(neededResources.get(i));
			if (record[i]==null) return null;
		}

		return record;
	}

	/**
	 * Bildet basierend auf den Namen der für die Bedienung der Kunden notwendigen Agentengruppen (und Anzahlen von Agenten)
	 * ein Array mit den Ressoucenzuordnungen. Jeder Eintrag entspricht einem der Ressourcen-Objekt-Datenstätze. Werte von 0
	 * bedeuten, dass die Ressource nicht benötigt wird.
	 * @param neededResources	Zuordnung von Ressourcennamen und benötigten Anzahlen
	 * @return	Array mit den Ressourcenbedarfszuordnungen (oder im Fehlerfall <code>null</code>)
	 */
	public int[] getNeededResourcesRecord(final Map<String,Integer> neededResources) {
		final int[] record=new int[names.length];

		int sum=0;
		for (Map.Entry<String,Integer> entry: neededResources.entrySet()) {
			final String name=entry.getKey();
			final int needed=entry.getValue();
			int nr=-1;
			for (int i=0;i<names.length;i++) if (names[i].equalsIgnoreCase(name)) {nr=i; break;}
			if (nr<0) return null;
			if (needed>list[nr].getMaxAvailable()) return null;
			record[nr]=needed;
			sum+=needed;
		}
		if (sum==0) return null; /* Es wurden überhaupt keine Ressourcen angefordert. */

		return record;
	}

	/**
	 * Prüft, ob alle genannten Teilressourcen verfügbar sind und reserviert diese im Falle einer vollständigen Verfügbarkeit
	 * @param neededResources	Angefragte Ressourcen
	 * @param simData	Simulationsdaten
	 * @param stationID	ID der Station an der die Ressource belegt werden sollen
	 * @return	Gibt einen Wert größer oder gleich 0 zurück, wenn die Ressource belegt werden konnte. In diesem Fall gibt der Zahlenwert die zusätzliche Rüstzeit an. Ein negativer Wert bedeutet, dass die Ressource nicht belegt werden konnte.
	 */
	public double tryLockResources(final int[] neededResources, final SimulationData simData, final int stationID) {
		final int listLength=list.length;

		/* Alle notwendigen Ressourcen verfügbar? */
		for (int i=0;i<listLength;i++) {
			final int needed=neededResources[i];
			if (needed>0 && !list[i].lockTest(needed,simData)) return -1;
		}

		/* Ressourcen belegen */
		double additionalTime=0;
		for (int i=0;i<listLength;i++) {
			final int needed=neededResources[i];
			if (needed>0) additionalTime=FastMath.max(additionalTime,list[i].lockDo(needed,simData,stationID));
		}

		return additionalTime;
	}

	/**
	 * Gibt die angegebenen Ressourcen frei
	 * @param usedResources	Freizugebende Ressourcen
	 * @param simData Simulationsdaten
	 */
	public void releaseResources(final int[] usedResources, final SimulationData simData) {
		for (int i=0;i<list.length;i++) if (usedResources[i]>0) list[i].releaseDo(usedResources[i],simData);
	}

	/**
	 * Gibt an, wie viele Bediener eines bestimmten Typs zu einem Zeitpunkt insgesamt im System vorhanden sind (arbeitend und im Leerlauf)
	 * @param index	0-basierender Index der Bedienergruppe
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern (im Falle von unendlich vielen oder einem ungültigen Index "0")
	 */
	public int getCount(final int index, final SimulationData simData) {
		if (index<0 || index>=names.length) return 0;
		return list[index].getCount(simData);
	}

	/**
	 * Gibt an, wie viele Bediener eines bestimmten Typs im mittel insgesamt im System vorhanden sind (arbeitend und im Leerlauf)
	 * @param index	0-basierender Index der Bedienergruppe
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern (im Falle von unendlich vielen oder einem ungültigen Index "0")
	 */
	public double getCountAverage(final int index, final SimulationData simData) {
		if (index<0 || index>=names.length) return 0;
		return list[index].getCountAverage(simData);
	}

	/**
	 * Stellt ein, wie viele Bediener eines bestimmten Typs im System vorhanden sein sollen
	 * @param index	0-basierender Index der Bedienergruppe
	 * @param simData	Simulationsdaten
	 * @param count	Neue Anzahl an Bedienern
	 * @return	Liefert <code>true</code> zurück, wenn die Anzahl verändert werden konnte.
	 */
	public boolean setCount(final int index, final SimulationData simData, final int count) {
		if (index<0 || index>=names.length) return false;
		return list[index].setCount(simData,count);
	}

	/**
	 * Gibt an, wie viele Bediener zu einem Zeitpunkt insgesamt verfügbar sind (arbeitend und im Leerlauf)
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern (im Falle von unendlich vielen oder einem ungültigen Index "0")
	 */
	public int getAllCount(final SimulationData simData) {
		int sum=0;
		for (RunDataResource resource: list) sum+=resource.getCount(simData);
		return sum;
	}

	/**
	 * Gibt an, wie viele Bediener im Mittel insgesamt verfügbar sind (arbeitend und im Leerlauf)
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern (im Falle von unendlich vielen oder einem ungültigen Index "0")
	 */
	public double getAllCountAverage(final SimulationData simData) {
		double sum=0;
		for (RunDataResource resource: list) sum+=resource.getCountAverage(simData);
		return sum;
	}

	/**
	 * Gibt an, wie viele Bediener eines bestimmten Typs zu einem Zeitpunkt in Ausfallzeit sind
	 * @param index	0-basierender Index der Bedienergruppe
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern
	 */
	public int getDown(final int index, final SimulationData simData) {
		if (index<0 || index>=names.length) return 0;
		return list[index].getDown(simData);
	}

	/**
	 * Gibt an, wie viele Bediener zu einem Zeitpunkt insgesamt in Ausfallzeit sind
	 * @param simData	Simulationsdaten
	 * @return	Anzahl an Bedienern
	 */
	public int getAllDown(final SimulationData simData) {
		int sum=0;
		for (RunDataResource resource: list) sum+=resource.getDown(simData);
		return sum;
	}

	/**
	 * Auslastungsstatistik aller Ressourcen als Array<br>
	 * Wird einmalig in {@link #getUsageStatistics(SimulationData)} erstellt
	 * und dann nur noch ausgeliefert.
	 * @see #getUsageStatistics(SimulationData)
	 */
	private StatisticsTimePerformanceIndicator[] statisticsUsage=null;

	/**
	 * Liefert die Auslastungsstatistik aller Ressourcen als Array zurück
	 * @param simData	Simulationsdatenobjekt
	 * @return Auslastungsstatistik aller Ressourcen als Array
	 */
	public StatisticsTimePerformanceIndicator[] getUsageStatistics(final SimulationData simData) {
		if (statisticsUsage==null) {
			statisticsUsage=new StatisticsTimePerformanceIndicator[list.length];
			for (int i=0;i<list.length;i++) statisticsUsage[i]=list[i].getStatisticsUsage(simData);
		}
		return statisticsUsage;
	}

	/**
	 * Liefert die bisher angefallenen Kosten für eine bestimmte Ressource
	 * @param index	Index der Ressource
	 * @return	Bisherige Kosten
	 */
	public double getCosts(final int index) {
		if (index<0 || index>=list.length) return 0.0;
		return list[index].getCosts();
	}

	/**
	 * Liefert die bisher für alle Ressourcen zusammen angefallenen Kosten
	 * @return	Bisherige Kosten
	 */
	public double getAllCosts() {
		double sum=0.0;
		for (RunDataResource resource: list) sum+=resource.getCosts();
		return sum;
	}

	/**
	 * Prüft bei allen Bedienern, ob diese evtl. in Pause gehen müssen bzw. berechnet neue Zeitpunkte dafür.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void updateStatus(final SimulationData simData) {
		for (RunDataResource resource: list) resource.updateStatus(simData);
	}

	/**
	 * Erfasst eine Zeitspanne in für alle Ressourcengruppen in der Statistik.<br>
	 * Muss am Simulationsende und wenn aktuelle Werte aus der Statistik während
	 * einer laufenden Simulation abgerufen werden sollen (und vorher Teilintervalle
	 * in die Statistik übertragen werden sollen) aufgerufen werden.<br>
	 * Bei der Belegung und Freigabe von Ressourcen ist dies nicht nötig;
	 * in diesem Fall erfolgt die Erfassung automatisch.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void updateStatistics(final SimulationData simData) {
		for (RunDataResource resource: list) resource.timesToStatistics(simData);
	}

	/**
	 * Muss zum Zeitpunkt 0 aufgerufen werden, um evtl. Ausfallzeitpunkte einzuplanen
	 * und Bediener zu initialisieren, wenn Rüstzeiten auf Bedienerseite vorgesehen sind.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void prepareOperatorObjects(final SimulationData simData) {
		for (RunDataResource resource: list) resource.prepareOperatorObjects(simData);
	}

	/**
	 * Liste aller Bediener<br>
	 * Wird einmalig in {@link #getOperators(SimulationData, boolean)}
	 * erstellt und dann nur noch ausgeliefert.
	 * @see #getOperators(SimulationData, boolean)
	 */
	private RunDataResourceOperator[] operators;

	/**
	 * Liefert eine Liste aller Bediener (über alle Ressourcen)
	 * @param simData	Simulationsdatenobjekt
	 * @param forceInit	Neueinrichtung der Bediener erzwingen
	 * @return Liefert eine Liste aller Bediener
	 */
	public synchronized RunDataResourceOperator[] getOperators(final SimulationData simData, final boolean forceInit) {
		if (operators==null) {
			List<RunDataResourceOperator> operatorsList=new ArrayList<>();
			for (RunDataResource resource: list) {
				final RunDataResourceOperator[] operators=resource.getOperators(simData,forceInit);
				if (operators!=null) {
					operatorsList.addAll(Arrays.asList(operators));
				}
			}
			operators=operatorsList.toArray(new RunDataResourceOperator[0]);

		}
		return operators;
	}

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn sich in einer Bedienergruppe die Anzahl an Bedienern ändert.
	 * @param listener	Hinzuzufügender Listener
	 * @see RunDataResources#fireResourceCountChangeListeners(SimulationData)
	 */
	public void addResourceCountChangeListeners(final Consumer<SimulationData> listener) {
		if (resourceCountChangeListeners.indexOf(listener)<0) resourceCountChangeListeners.add(listener);
	}

	/**
	 * Benachrichtigt alle Listener, dass sich in einer Gruppe die Anzahl an Bedienern geändert hat.
	 * @param simData	Simulationsdatenobjekt
	 * @see RunDataResources#addResourceCountChangeListeners(Consumer)
	 */
	public void fireResourceCountChangeListeners(final SimulationData simData) {
		operators=null;
		getOperators(simData,false);
		for (Consumer<SimulationData> listener: resourceCountChangeListeners) listener.accept(simData);
	}
}
