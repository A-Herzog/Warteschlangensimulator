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
package net.dde;

import java.util.function.Supplier;

/**
 * Diese Klasse repräsentiert ein repräsentiert ein Datenfeld
 * innerhalb eines Themas innerhalb eines durch ein
 * {@link DDEServerSystem}-Objekt erzeugten DDE-Servers.
 * @author Alexander Herzog
 * @see DDEServerSystem
 * @see DDETopic
 */
public final class DDEItem {
	/** Lambda-Ausdruck zur Ermittlung des aktuellen Wertes des Datenfeldes */
	private final Supplier<String> getter;
	/** Gibt an, ob der Server benachrichtigt werden soll, wenn sich die Daten geändert haben */
	private final boolean updateable;
	private String lastValue;

	/**
	 * Name des Datenfeldes.<br>
	 * Wird vom Konstruktor gesetzt.
	 */
	public final String name;

	/**
	 * Konstruktor der Klasse
	 * @param itemName	Name des Datenfeldes
	 * @param staticContent	Statischer Inhalt des Datenfeldes
	 * @see DDETopic#addItemStatic(String, String)
	 */
	public DDEItem(final String itemName, final String staticContent) {
		this(itemName,()->staticContent,false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param itemName	Name des Datenfeldes
	 * @param getter	Lambda-Ausdruck zur Ermittlung des aktuellen Wertes des Datenfeldes
	 * @see DDETopic#addItemDynamic(String, Supplier)
	 */
	public DDEItem(final String itemName, final Supplier<String> getter) {
		this(itemName,getter,false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param itemName	Name des Datenfeldes
	 * @param getter	Lambda-Ausdruck zur Ermittlung des aktuellen Wertes des Datenfeldes
	 * @param updateable	Gibt an, ob der Server benachrichtigt werden soll, wenn sich die Daten geändert haben
	 * @see DDETopic#addItemDynamic(String, Supplier)
	 * @see DDETopic#addItemUpdateable(String, Supplier)
	 */
	public DDEItem(final String itemName, final Supplier<String> getter, final boolean updateable) {
		name=itemName;
		this.getter=getter;
		this.updateable=updateable;
	}

	/**
	 * Prüft, sofern im Konstruktor angegeben wurde, dass der Server über Änderungen benachrichtigt werden soll, ob Änderungen vorliegen.
	 * @return	Gibt <code>true</code> zurück, wenn Änderungen vorliegen.
	 */
	public boolean updateTest() {
		if (!updateable) return false;
		final String newValue=getter.get();
		if (!newValue.equals(lastValue)) {
			lastValue=newValue;
			return true;
		}
		return false;
	}

	/**
	 * Liefert den aktuellen Wert des Datenfeldes
	 * @return	Aktueller Wert des Datenfeldes
	 */
	public String get() {
		return getter.get();
	}
}
