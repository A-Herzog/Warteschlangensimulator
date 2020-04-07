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

import net.dde.DDEConnect;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;

/**
 * Laufzeitdaten eines <code>RunElementOutputDDE</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementOutputDDE
 * @see RunElementData
 */
public class RunElementOutputDDEData extends RunElementData {
	/**
	 * Arbeitsmappe in die die Daten geschrieben werden sollen
	 */
	private final String workbook;

	/**
	 * Tabelle innerhalb der Arbeitsmappe in die die Daten geschrieben werden sollen
	 */
	private final String table;

	/**
	 * 0-basierende Nummer der ersten Spalte in die geschrieben werden soll
	 */
	private final int startColumn;

	/**
	 * Excel-DDE-Verbindung
	 */
	private final DDEConnect connect;

	/**
	 * 0-basierende Nummer der Zeile in die als nächstes geschrieben werden soll
	 */
	public int nextRow;

	/**
	 * Konstruktor der Klasse <code>RunElementOutputDDEData</code>
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementOutputDDE</code>-Element
	 * @param workbook	Arbeitsmappe in die die Daten geschrieben werden sollen
	 * @param table	Tabelle innerhalb der Arbeitsmappe in die die Daten geschrieben werden sollen
	 * @param startRow	0-basierende Nummer der Zeile in die als nächstes geschrieben werden soll
	 * @param startColumn	0-basierende Nummer der ersten Spalte in die geschrieben werden soll
	 */
	public RunElementOutputDDEData(final RunElement station, final String workbook, final String table, final int startRow, final int startColumn) {
		super(station);

		this.workbook=workbook;
		this.table=table;
		this.startColumn=startColumn;
		this.nextRow=startRow;
		connect=new DDEConnect();
	}

	/**
	 * Schreibt per DDE eine Zeile in die angegebene Excel-Tabelle
	 * @param line	Zu schreibende Zeile
	 */
	public void writeLine(final String[] line) {
		if (line==null || line.length==0) return;

		for (int i=0;i<line.length;i++) connect.setData(workbook,table,nextRow,startColumn+i,line[i]);
		nextRow++;
	}

	/**
	 * Schließt am Ende der Simulation die DDE-Verbindung.
	 */
	public void closeConnection() {
		connect.closeSetDataConnection();
	}
}