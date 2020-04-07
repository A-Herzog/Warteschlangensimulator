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
package ui.modeleditor;

/**
 * Elemente, die dieses Interface implementieren, werden von <code>ModelSurface</code> informiert,
 * wenn eine Kunden-, Bediener- usw. Gruppe umbenannt wird und können daraufhin eigene Strukturen anpassen.
 * @author Alexander Herzog
 * @see ModelSurface#objectRenamed(String, String, RenameType, boolean)
 */
public interface ModelDataRenameListener {
	/**
	 * Gibt an, was für eine Art von Objekt umbenannt wurde.
	 * @author Alexander Herzog
	 */
	public enum RenameType {
		/** Es wurde ein Kundentyp umbenannt. */
		RENAME_TYPE_CLIENT_TYPE,

		/** Es wurde eine Ressource umbenannt. */
		RENAME_TYPE_RESOURCE,

		/** Es wurde ein Signal umbenannt. */
		RENAME_TYPE_SIGNAL,

		/** Es wurde ein Transportziel umbenannt. */
		RENAME_TYPE_TRANSPORT_DESTINATION,

		/** Es wurde ein Transporter umbenannt. */
		RENAME_TYPE_TRANSPORTER,

		/** Es wurde ein Teleportziel umbenannt. */
		RENAME_TYPE_TELEPORT_DESTINATION
	}

	/**
	 * Prüft, ob auf den Aufruf von <code>objectRenamed</code> tatsächlich reagiert werden soll.
	 * @param oldName	Angegebener alter Name
	 * @param newName	Angegebener neuer Name
	 * @param type	Angegebener Typ der Veränderung
	 * @param listenType	Typ, der berücksichtigt bzw. geprüft werden soll
	 * @return	Gibt <code>true</code> zurück, wenn alter und neuer Name sinnvoll belegt sind und der Typ, auf den geachtet werden soll, vorliegt.
	 * @see RenameType
	 */
	default boolean isRenameType(final String oldName, final String newName, final RenameType type, final RenameType listenType) {
		if (oldName==null || oldName.isEmpty() || newName==null || newName.isEmpty() || oldName.equals(newName)) return false;
		return type==listenType;
	}

	/**
	 * Wird von	<code>ModelSurface.objectRenamed</code> bei allen Elementen, die dieses Interface
	 * implementieren, aufgerufen, wenn eine Umbenennung stattfindet.
	 * @param oldName	Alter Name
	 * @param newName	Neuer Name
	 * @param type	Art der Umbenennung
	 * @see RenameType
	 * @see ModelSurface#objectRenamed(String, String, RenameType, boolean)
	 */

	void objectRenamed(final String oldName, final String newName, final RenameType type);
}
