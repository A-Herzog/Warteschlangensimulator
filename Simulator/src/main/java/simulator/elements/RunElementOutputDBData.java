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
package simulator.elements;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.db.DBConnect;
import simulator.db.DBSettings;

/**
 * Laufzeitdaten eines <code>RunElementOutputDB</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementOutputDB
 * @see RunElementData
 */
public class RunElementOutputDBData extends RunElementData {
	/**
	 * Offene Datenbankverbindung oder <code>null</code>, wenn keine Verbindung besteht oder diese geschlossen wurde
	 */
	public DBConnect connect;

	/**
	 * Hält ein thread-lokales Array der passenden Länge zum Zwischenspeichern der Werte, die in die Tabelle geschrieben werden sollen, vor.<br>
	 * Das Array wird vom RunElement selbst initialisiert.
	 */
	public String[] cellValueBuffer;

	/**
	 * Konstruktor der Klasse <code>RunElementOutputDBData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param settings	Einstellungen für die Datenbankverbindung
	 */
	public RunElementOutputDBData(final RunElement station, final DBSettings settings) {
		super(station);

		connect=new DBConnect(settings,false);
		if (connect.getInitError()!=null) {
			connect.close();
			connect=null;
		}
	}

	/**
	 * Schließt die Datenbankverbindung, sofern diese besteht.
	 */
	public void closeDB() {
		if (connect!=null) {
			connect.close();
			connect=null;
		}
	}
}
