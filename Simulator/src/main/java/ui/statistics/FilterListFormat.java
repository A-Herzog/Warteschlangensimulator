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
package ui.statistics;

/**
 * Diese Klasse hält das aktuell zu verwendende Ausgabeformat vor.
 * @author Alexander Herzog
 * @see FilterListRecord#process(simulator.statistics.Statistics, FilterListFormat)
 */
public class FilterListFormat {
	private boolean isTime;
	private boolean isPercent;
	private boolean isSystem;

	/**
	 * Konstruktor der Klasse.<br>
	 * Stellt das Standardformat ein.
	 */
	public FilterListFormat() {
		isTime=false;
		isPercent=false;
		isSystem=false;
	}

	/**
	 * Stellt lokale Notation (statt System-Notation) ein.
	 * @see FilterListFormat#setSystem()
	 */
	public void setLocal() {
		isSystem=false;
	}

	/**
	 * Stellt System-Notation (statt lokaler Notation) ein.
	 * @see FilterListFormat#setSystem()
	 */
	public void setSystem() {
		isSystem=true;
	}

	/**
	 * Stellt die Ausgabe als Dezimalwerte ein (statt Prozentwerte).
	 * @see FilterListFormat#setPercent()
	 */
	public void setFraction() {
		isTime=false;
		isPercent=false;
	}

	/**
	 * Stellt die Ausgabe als Prozentwerte ein (statt Dezimalwerte).
	 * @see FilterListFormat#setFraction()
	 */
	public void setPercent() {
		isTime=false;
		isPercent=true;
	}

	/**
	 * Stellt die Ausgabe als Zeitwerte ein (statt Zahlenwerte).
	 * @see FilterListFormat#setNumber()
	 */
	public void setTime() {
		isTime=true;
	}

	/**
	 * Stellt die Ausgabe als Zahlenwerte ein (statt Zeitwerte).
	 * @see FilterListFormat#setTime()
	 */
	public void setNumber() {
		isTime=false;
	}

	/**
	 * Ist die Ausgabe als Zeitwerte eingestellt (statt als Zahlenwerte)?
	 * @return	Ausgabe als Zeitwerte
	 */
	public boolean isTime() {
		return isTime;
	}

	/**
	 * Ist die Ausgabe als Prozentwerte eingestellt (statt als Dezimalwerte)?
	 * @return	Ausgabe als Prozentwerte
	 */
	public boolean isPercent() {
		return isPercent;
	}

	/**
	 * Ist die Ausgabe in System-Notation eingestellt (statt lokaler Notation)?
	 * @return	Ausgabe in System-Notation
	 */
	public boolean isSystem() {
		return isSystem;
	}
}
