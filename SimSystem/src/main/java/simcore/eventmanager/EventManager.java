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
 * Interface f�r alle EventManager-Implementierungen<br><br>
 * Konkrete Implementierungen sollten von <code>EventManagerBase</code> abgeleitet werden,
 * da diese Klasse bereits alle wichtigen Methoden, die nicht direkt mit der Speicherung
 * der Ereignisse zu tun haben, implementiert.
 * @author Alexander Herzog
 * @version 1.1
 * @see EventManagerBase
 */
public interface EventManager {
	/**
	 * F�hrt zu einem direkten Abbruch der Ereignisschleife
	 */
	public void directAbortThread();

	/**
	 * F�hrt das n�chste Ereignis in der Liste aus.
	 * @param simData Referenz auf das <code>SimData</code>-Objekt, das an die <code>run</code>-Methode des Ereignisses �bergeben werden soll
	 * @param maxExecuteEvents Gibt die Maximalzahl an Events, die in einem Durchgang bearbeitet werden sollen, ab
	 * @param maxRunTime Gibt die maximale Laufzeit (in ms) an, die ein Durchgang dauern darf
	 * @return Liefert <code>true</code> zur�ck, wenn die angegebene Anzahl an Ereignissen ausgef�hrt werden konnte.
	 * @see SimData
	 * @see Event
	 */
	public boolean executeNextEvents(final SimData simData, final int maxExecuteEvents, final int maxRunTime);

	/**
	 * F�gt ein Ereignis in die Ereignisliste ein.
	 * @param event Referenz auf das einzuf�gende Ereignis
	 */
	public void addEvent(final Event event);

	/**
	 * L�scht ein Ereignis aus der Warteschlange ohne es auszuf�hren. Da die Referenz auf das Ereignis nach dem L�schen
	 * an den EventCache �bergeben wird, darf das Ereignis nach dem L�schen nicht mehr verwendet werden.
	 * @param event	Zu l�schendes Ereignis
	 * @param simData	Referenz auf das <code>SimData</code>-Objekt (wird ben�tigt zum recyceln des Ereignisses)
	 */
	public void deleteEvent(final Event event, final SimData simData);

	/**
	 * L�scht alle Ereignisse aus allen Listen, ohne sie dabei zu recyceln.
	 */
	public void deleteAllEvents();

	/**
	 * Wenn von Beginn an mehrere Ereignisse in die Ereignisliste eingef�gt werden sollen, so ist es von
	 * Vorteil diese zu sortieren, zu verketten und nur das erste in die Ereignisliste einzuf�gen.
	 * Beim Abarbeiten des ersten Ereignisses wird dann das zweite (im ersten eingetragene) Ereignis
	 * in die Liste eingef�gt. Die Funktion <code>addInitialEvents</code> erledigt genau dies:
	 * Die im Parameter �bergebenen Ereignisse werden nach dem Ausf�hrungszeitpunkt sortiert und das
	 * erste Ereignis wird in die Ereignisliste eingef�gt. Au�erdem wird in das erste Ereignis eingetragen,
	 * dass bei seiner Ausf�hrung das zweite in die Liste eingetragen werden soll usw.
	 * @param	events	Ereignisse, die von Anfang an in der Ereignisliste stehen sollen
	 */
	public void addInitialEvents(final List<? extends Event> events);

	/**
	 * Liefert die Anzahl der ausgef�hrten Ereignisse zur�ck.
	 * @return Anzahl der ausgef�hrten Ereignisse
	 */
	public long eventCount();

	/**
	 * Liefert die momentane L�nge der Ereigniswarteschlange zur�ck.
	 * @return L�nge der Ereigniswarteschlange
	 */
	public int eventQueueLength();

	/**
	 * Informiert den EventManager dar�ber, dass ein neuer Tag begonnen hat.
	 * Nicht alle EventManager m�ssen diese Information interpretieren. Lediglich bei mehreren Listen
	 * muss hier ein Reset durchgef�hrt werden.
	 */
	public void resetTime();

	/**
	 * Setzt den Z�hler der ausgef�hrten Ereignisse zur�ck.
	 */
	public void resetCount();

	/**
	 * Signalisiert, dass die Simulation unterbrochen werden soll und dass <code>executeNextEvents</code>
	 * nach dem Ende der Ausf�hrung des aktuellen Ereignisses direkt zur�ckkehren soll (und nicht erst
	 * nach der angegebenen Anzahl an auszuf�hrenden Ereignissen).
	 */
	public void setPause();

	/**
	 * Liefert f�r Debugging-Zwecke alle aktuellen Ereignisse zur�ck
	 * @return	Liste mit allen aktuellen Ereignissen
	 */
	public List<Event> getAllEvents();
}