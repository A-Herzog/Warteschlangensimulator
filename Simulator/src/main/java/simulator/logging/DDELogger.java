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
package simulator.logging;

import java.awt.Color;

import net.dde.DDEConnect;
import simcore.SimData;
import simcore.logging.SimLogging;

/**
 * Schreibt die Logging-Daten über eine DDE-Verbindung direkt in eine Exceltabelle (in einer laufenden Excel-Instanz).
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 * @see DDEConnect
 */
public class DDELogger implements SimLogging {
	private final String workbook;
	private final String sheet;
	private final boolean printIDs;
	private final DDEConnect connect;
	private final boolean ready;
	private int nextRow;
	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klassw
	 * @param workbook	Zu verwendende Excel-Arbeitsmappe
	 * @param sheet	Zu verwendende Tabelle innerhalb der Arbeitsmappe (die Ausgabe erfolgt ab Zeile 1, Spalte 1; vorhandene Daten werden überschrieben)
	 * @param printIDs	IDs mit ausgeben
	 */
	public DDELogger(final String workbook, final String sheet, final boolean printIDs) {
		this.workbook=workbook;
		this.sheet=sheet;
		this.printIDs=printIDs;
		connect=new DDEConnect();
		ready=connect.available();
		nextRow=0;
	}

	@Override
	public boolean ready() {
		return ready;
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, String info) {
		boolean ok=true;
		int nextCol=0;

		if (!connect.setData(workbook,sheet,nextRow,nextCol,SimData.formatSimTime(time))) ok=false;
		nextCol++;

		if (event!=null) {
			if (!connect.setData(workbook,sheet,nextRow,nextCol,event)) ok=false;
		}
		nextCol++;

		if (printIDs) {
			if (id>=0) {
				if (!connect.setData(workbook,sheet,nextRow,nextCol,""+id)) ok=false;
			}
			nextCol++;
		}

		if (info!=null) {
			if (!connect.setData(workbook,sheet,nextRow,nextCol,info)) ok=false;
		}

		nextRow++;

		return ok;
	}

	@Override
	public boolean done() {
		connect.closeSetDataConnection();
		if (nextLogger!=null) nextLogger.done();
		return true;
	}

	@Override
	public void setNextLogger(final SimLogging logger) {
		nextLogger=logger;
	}

	@Override
	public SimLogging getNextLogger() {
		return nextLogger;
	}
}