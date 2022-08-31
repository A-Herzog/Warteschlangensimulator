/**
 * Copyright 2022 Alexander Herzog
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
package ui.modeleditor.elements;

import java.util.List;

import ui.modeleditor.elements.ModelElementDecide.DecideMode;

/**
 * Elemente, die dieses Interface implementieren, können über ein {@link DecideDataPanel} bearbeitet werden.
 * @author Alexander Herzog
 * @see DecideDataPanel
 * @see ModelElementDecide
 * @see ModelElementDecideAndTeleport
 */
public interface ElementWithDecideData {
	/**
	 * Gibt an, ob die Verzweigung zufällig (gemäß vorgegebenen Raten), nach Bedingungen oder nach Kundentypen erfolgen soll
	 * @return	Verzweigungsmodus
	 * @see DecideMode
	 */
	ModelElementDecide.DecideMode getMode();

	/**
	 * Stellt den Verzweigungsmodus (zufällig gemäß vorgegebenen Raten, nach Bedingungen oder nach Kundentypen) ein.
	 * @param mode	Verzweigungsmodus
	 */
	void setMode(final ModelElementDecide.DecideMode mode);

	/**
	 * Liefert die Raten, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Liste der Raten für die Verzweigungen
	 */
	List<Double> getRates();

	/**
	 * Liefert die Bedingungen, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Liste der Bedingungen für die Verzweigungen
	 */
	List<String> getConditions();

	/**
	 * Liefert eine Liste aller Kundentypen-Verzweigungen
	 * @return	Liste der Namen der Kundentypen für die Verzweigungen
	 */
	List<String> getClientTypes();

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so liefert diese Funktion den Schlüssel
	 * @return	Schlüssel gemäß dessen Werten die Verzweigung erfolgen soll
	 */
	String getKey();

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so kann über diese Funktion der Schlüssel eingestellt werden
	 * @param key	Schlüssel gemäß dessen Werten die Verzweigung erfolgen soll
	 */
	void setKey(final String key);

	/**
	 * Werte anhand denen die Verzweigung erfolgen soll
	 * @return	Verzweigungswerte
	 */
	List<String> getValues();

	/**
	 * Kann jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 * @return	Kann jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 */
	boolean isMultiTextValues();

	/**
	 * Stellt ein, ob jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten darf.
	 * @param multiTextValues	Kann jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 */
	void setMultiTextValues(boolean multiTextValues);
}
