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
package simcore.eventmanager;

import java.util.List;

import simcore.Event;
import simcore.SimData;

/**
 * Interface für alle EventManager-Implementierungen<br><br>
 * Konkrete Implementierungen sollten von <code>EventManagerBase</code> abgeleitet werden,
 * da diese Klasse bereits alle wichtigen Methoden, die nicht direkt mit der Speicherung
 * der Ereignisse zu tun haben, implementiert.
 * @author Alexander Herzog
 * @version 1.1
 * @see EventManagerBase
 */
public interface EventManager {
	/**
	 * Führt zu einem direkten Abbruch der Ereignisschleife
	 */
	public void directAbortThread();

	/**
	 * Führt das nächste Ereignis in der Liste aus.
	 * @param simData Referenz auf das <code>SimData</code>-Objekt, das an die <code>run</code>-Methode des Ereignisses übergeben werden soll
	 * @param maxExecuteEvents Gibt die Maximalzahl an Events, die in einem Durchgang bearbeitet werden sollen, ab
	 * @param maxRunTime Gibt die maximale Laufzeit (in ms) an, die ein Durchgang dauern darf
	 * @return Liefert <code>true</code> zurück, wenn die angegebene Anzahl an Ereignissen ausgeführt werden konnte.
	 * @see SimData
	 * @see Event
	 */
	public boolean executeNextEvents(final SimData simData, final int maxExecuteEvents, final int maxRunTime);

	/**
	 * Fügt ein Ereignis in die Ereignisliste ein.
	 * @param event Referenz auf das einzufügende Ereignis
	 */
	public void addEvent(final Event event);

	/**
	 * Löscht ein Ereignis aus der Warteschlange ohne es auszuführen. Da die Referenz auf das Ereignis nach dem Löschen
	 * an den EventCache übergeben wird, darf das Ereignis nach dem Löschen nicht mehr verwendet werden.
	 * @param event	Zu löschendes Ereignis
	 * @param simData	Referenz auf das <code>SimData</code>-Objekt (wird benötigt zum recyceln des Ereignisses)
	 */
	public void deleteEvent(final Event event, final SimData simData);

	/**
	 * Löscht alle Ereignisse aus allen Listen, ohne sie dabei zu recyceln.
	 */
	public void deleteAllEvents();

	/**
	 * Wenn von Beginn an mehrere Ereignisse in die Ereignisliste eingefügt werden sollen, so ist es von
	 * Vorteil diese zu sortieren, zu verketten und nur das erste in die Ereignisliste einzufügen.
	 * Beim Abarbeiten des ersten Ereignisses wird dann das zweite (im ersten eingetragene) Ereignis
	 * in die Liste eingefügt. Die Funktion <code>addInitialEvents</code> erledigt genau dies:
	 * Die im Parameter übergebenen Ereignisse werden nach dem Ausführungszeitpunkt sortiert und das
	 * erste Ereignis wird in die Ereignisliste eingefügt. Außerdem wird in das erste Ereignis eingetragen,
	 * dass bei seiner Ausführung das zweite in die Liste eingetragen werden soll usw.
	 * @param	events	Ereignisse, die von Anfang an in der Ereignisliste stehen sollen
	 */
	public void addInitialEvents(final List<? extends Event> events);

	/**
	 * Liefert die Anzahl der ausgeführten Ereignisse zurück.
	 * @return Anzahl der ausgeführten Ereignisse
	 */
	public long eventCount();

	/**
	 * Liefert die momentane Länge der Ereigniswarteschlange zurück.
	 * @return Länge der Ereigniswarteschlange
	 */
	public int eventQueueLength();

	/**
	 * Informiert den EventManager darüber, dass ein neuer Tag begonnen hat.
	 * Nicht alle EventManager müssen diese Information interpretieren. Lediglich bei mehreren Listen
	 * muss hier ein Reset durchgeführt werden.
	 */
	public void resetTime();

	/**
	 * Setzt den Zähler der ausgeführten Ereignisse zurück.
	 */
	public void resetCount();

	/**
	 * Signalisiert, dass die Simulation unterbrochen werden soll und dass <code>executeNextEvents</code>
	 * nach dem Ende der Ausführung des aktuellen Ereignisses direkt zurückkehren soll (und nicht erst
	 * nach der angegebenen Anzahl an auszuführenden Ereignissen).
	 */
	public void setPause();

	/**
	 * Liefert für Debugging-Zwecke alle aktuellen Ereignisse zurück
	 * @return	Liste mit allen aktuellen Ereignissen
	 */
	public List<Event> getAllEvents();
}