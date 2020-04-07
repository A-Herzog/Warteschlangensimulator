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

/**
 * Dies ist das "System"-Thema, über das alle DDE-Server verfügen müssen.
 * Es wird bei jedem {@link DDEServerSystem} automatisch eingebunden,
 * taucht aber dort in der {@link DDEServerSystem#topics}-Liste nicht auf.
 * @author Alexander Herzog
 * @see DDEServerSystem
 */
public class DDETopicSystem extends DDETopic {
	/**
	 * Konstruktor der Klasse
	 * @param server	DDE-Server
	 */
	public DDETopicSystem(final DDEServerSystem server) {
		super(server,"System");

		addItemDynamic("SysItems",()->String.join("\t",getItemsStream().map(item->item.name).toArray(String[]::new)));
		addItemStatic("Help","See online help.");
		addItemStatic("Status","Ready");
		addItemDynamic("Topics",()->String.join("\t",server.getTopicNames()));
	}
}