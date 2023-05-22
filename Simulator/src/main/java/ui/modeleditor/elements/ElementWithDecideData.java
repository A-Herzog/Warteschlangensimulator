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
import java.util.function.Supplier;

import language.Language;
import ui.modeleditor.elements.ModelElementDecide.DecideMode;

/**
 * Elemente, die dieses Interface implementieren, k�nnen �ber ein {@link DecideDataPanel} bearbeitet werden.
 * @author Alexander Herzog
 * @see DecideDataPanel
 * @see ModelElementDecide
 * @see ModelElementDecideAndTeleport
 */
public interface ElementWithDecideData {
	/**
	 * Gibt an, ob die Verzweigung zuf�llig (gem�� vorgegebenen Raten), nach Bedingungen oder nach Kundentypen erfolgen soll
	 * @return	Verzweigungsmodus
	 * @see DecideMode
	 */
	ModelElementDecide.DecideMode getMode();

	/**
	 * Stellt den Verzweigungsmodus (zuf�llig gem�� vorgegebenen Raten, nach Bedingungen oder nach Kundentypen) ein.
	 * @param mode	Verzweigungsmodus
	 */
	void setMode(final ModelElementDecide.DecideMode mode);

	/**
	 * Liefert die Raten, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Liste der Raten f�r die Verzweigungen
	 */
	List<String> getRates();

	/**
	 * Liefert die Vielfachheiten zur Ansteuerung einzelner Ausg�nge im Reihum-Modus.
	 * @return	Vielfachheiten f�r die einzelnen Ausg�nge
	 */
	List<Integer> getMultiplicity();

	/**
	 * Liefert die Bedingungen, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Liste der Bedingungen f�r die Verzweigungen
	 */
	List<String> getConditions();

	/**
	 * Liefert eine Liste aller Kundentypen-Verzweigungen
	 * @return	Liste der Namen der Kundentypen f�r die Verzweigungen
	 */
	List<List<String>> getClientTypes();

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so liefert diese Funktion den Schl�ssel
	 * @return	Schl�ssel gem�� dessen Werten die Verzweigung erfolgen soll
	 */
	String getKey();

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so kann �ber diese Funktion der Schl�ssel eingestellt werden
	 * @param key	Schl�ssel gem�� dessen Werten die Verzweigung erfolgen soll
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

	/**
	 * Wie soll bei Gleichstand zwischen mehreren Ausg�ngen entschieden werden?
	 */
	enum DecideByStationOnTie {
		/** Folgestation unter den gleichguten besten M�glichkeiten zuf�llig w�hlen */
		RANDOM(()->Language.tr("Surface.Decide.DecideByStationOnTie.XMLNameRandom"),()->Language.trAll("Surface.Decide.DecideByStationOnTie.XMLNameRandom")),
		/** Erste der gleichguten besten M�glichkeiten w�hlen */
		FIRST(()->Language.tr("Surface.Decide.DecideByStationOnTie.XMLNameFirst"),()->Language.trAll("Surface.Decide.DecideByStationOnTie.XMLNameFirst")),
		/** Letzte der gleichguten besten M�glichkeiten w�hlen */
		LAST(()->Language.tr("Surface.Decide.DecideByStationOnTie.XMLNameLast"),()->Language.trAll("Surface.Decide.DecideByStationOnTie.XMLNameLast"));

		/**
		 * Callback, welches den prim�ren Namen des Elements liefert
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Callback, welches alle Namen des Elements liefert
		 */
		private final Supplier<String[]> allNamesGetter;

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback, welches den prim�ren Namen des Elements liefert
		 * @param allNamesGetter	Callback, welches alle Namen des Elements liefert
		 */
		DecideByStationOnTie(final Supplier<String> nameGetter, final Supplier<String[]> allNamesGetter) {
			this.nameGetter=nameGetter;
			this.allNamesGetter=allNamesGetter;
		}

		/**
		 * Liefert den prim�ren Namen des Elements.
		 * @return	Prim�rer Name des Elements
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Versucht zu einem vorgegebenen Namen den passenden Eintrag zu liefern.
		 * @param name	Name f�r den der passende Enum-Eintrag geliefert werden soll.
		 * @return	Enum-Eintrag oder Vorgabewert, wenn kein Eintrag zu dem Namen passt
		 */
		public static DecideByStationOnTie byName(final String name) {
			if (name==null) return RANDOM;
			for (DecideByStationOnTie value: values()) {
				final String[] names=value.allNamesGetter.get();
				for (String test: names) if (test.equalsIgnoreCase(name)) return value;
			}
			return RANDOM;
		}
	}

	/**
	 * Liefert die Einstellung, wie soll bei Gleichstand zwischen mehreren Ausg�ngen entschieden werden soll.
	 * @return	Wie soll bei Gleichstand zwischen mehreren Ausg�ngen entschieden werden?
	 * @see DecideByStationOnTie
	 */
	DecideByStationOnTie getDecideByStationOnTie();

	/**
	 * Stellt ein, wie soll bei Gleichstand zwischen mehreren Ausg�ngen entschieden werden soll.
	 * @param decideByStationOnTie	Wie soll bei Gleichstand zwischen mehreren Ausg�ngen entschieden werden?
	 * @see DecideByStationOnTie
	 */
	void setDecideByStationOnTie(DecideByStationOnTie decideByStationOnTie);
}
