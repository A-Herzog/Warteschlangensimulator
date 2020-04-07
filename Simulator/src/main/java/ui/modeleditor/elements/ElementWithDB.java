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
package ui.modeleditor.elements;

import simulator.db.DBSettings;
import ui.modeleditor.FilePathHelper;

/**
 * Elemente, die Eingaben Daten aus Datenbanken laden oder in diese schreiben, implementieren dieses Interface,
 * damit der {@link FilePathHelper} bei Bedarf nach dem Laden den Pfad anpassen kann.
 * @author Alexander Herzog
 */
public interface ElementWithDB {
	/**
	 * Liefert die Einstellungen zur Verbindung zur Datenbank
	 * @return	Einstellungen zur Verbindung zur Datenbank
	 */
	DBSettings getDb();
}
