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
package ui.modeleditor.coreelements;

import java.io.File;
import java.util.List;
import java.util.Map;

import language.Language;
import net.dde.DDEConnect;
import simulator.db.DBConnect;
import simulator.db.DBSettings;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse hält die Ergebnisse der Prüfung der externen Datenquellen
 * für ein Modell-Element vor.
 * @author Alexander Herzog
 * @see EditModel#getDataCheckResults()
 * @see ModelElementBox#checkExternalData()
 */
public class DataCheckResult {
	/**
	 * Mögliche Ergebnisse der Prüfung
	 * @author Alexander Herzog
	 * @see DataCheckResult#status
	 */
	public enum Status {
		/**
		 * Das Element verwendet keine externen Daten.<br>
		 * In diesem Fall sind die Felder {@link DataCheckResult#data} und {@link DataCheckResult#errorMessage} jeweils <code>null</code>.
		 */
		NO_EXTERNAL_DATA,

		/**
		 * Die externen Daten sind erreichbar.<br>
		 * In diesem Fall enthält das Feld {@link DataCheckResult#data} den Namen der externen Quelle und das Feld {@link DataCheckResult#errorMessage} ist <code>null</code>.
		 */
		OK,

		/**
		 * Die externen Daten sind <b>nicht</b> erreichbar.<br>
		 * In diesem Fall enthält das Feld {@link DataCheckResult#data} den Namen der externen Quelle und das Feld {@link DataCheckResult#errorMessage} eine Fehlermeldung.
		 */
		ERROR
	}

	/**
	 * Gibt an um was für eine Art von Daten es sich handelt
	 * @author Alexander Herzog
	 * @see DataCheckResult#dataType
	 */
	public enum DataType {
		/**
		 * Keine Daten
		 */
		NONE,

		/**
		 * Datei
		 */
		FILE,

		/**
		 * Datenbank-Verbindung
		 */
		DB,

		/**
		 * DDE-Verbindung
		 */
		DDE
	}

	/**
	 * Modell-Element auf das sich die Prüfung bezieht.
	 */
	public final ModelElementBox element;

	/**
	 * Ergebnis der Prüfung.
	 * @see Status
	 */
	public final Status status;

	/**
	 * Type der Daten
	 * @see DataType
	 */
	public final DataType dataType;

	/**
	 * Name der externen Datenquelle.<br>
	 * Ist <code>null</code> im Falle von <code>status==NO_EXTERNAL_DATA</code>. Sonst immer mit einem Wert belegt.
	 */
	public final String data;

	/**
	 * Fehlermeldung beim Zugriff auf die Daten.<br>
	 * Ist nur im Falle von <code>status==ERROR</code> mit einem Wert belegt. Sonst immer <code>null</code>.
	 */
	public final String errorMessage;

	/**
	 * Konstruktor der Klasse<br>
	 * Kann nicht direkt aufgerufen werden.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @see DataCheckResult#noCheckNeeded(ModelElementBox)
	 */
	private DataCheckResult(final ModelElementBox element) {
		this.element=element;
		status=Status.NO_EXTERNAL_DATA;
		dataType=DataType.NONE;
		data=null;
		errorMessage=null;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Kann nicht direkt aufgerufen werden.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @param dataType	Typ der Daten
	 * @param data	Name der externen Datenquelle
	 * @see DataCheckResult#checkFile(ModelElementBox, String)
	 * @see DataCheckResult#checkDB(ModelElementBox, DBSettings)
	 * @see DataCheckResult#checkDDE(ModelElementBox, String, String)
	 */
	private DataCheckResult(final ModelElementBox element, final DataType dataType, final String data) {
		this.element=element;
		status=Status.OK;
		this.dataType=dataType;
		this.data=data;
		errorMessage=null;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Kann nicht direkt aufgerufen werden.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @param dataType	Typ der Daten
	 * @param data	Name der externen Datenquelle
	 * @param errorMessage	Fehlermeldung beim Zugriff auf die Daten
	 * @see DataCheckResult#checkFile(ModelElementBox, String)
	 * @see DataCheckResult#checkDB(ModelElementBox, DBSettings)
	 * @see DataCheckResult#checkDDE(ModelElementBox, String, String)
	 */
	private DataCheckResult(final ModelElementBox element, final DataType dataType, final String data, final String errorMessage) {
		this.element=element;
		status=Status.ERROR;
		this.dataType=dataType;
		this.data=data;
		this.errorMessage=errorMessage;
	}

	/**
	 * Erzeugt ein {@link DataCheckResult}-Objekt, welches angibt, dass das zugehörige Element keine externen Daten verwendet.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @return	Prüfungsergebnis
	 */
	public static DataCheckResult noCheckNeeded(final ModelElementBox element) {
		return new DataCheckResult(element);
	}

	/**
	 * Prüft die angegebene Datei auf Existenz und liefert ein entsprechendes {@link DataCheckResult}-Objekt zurück.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @param fileName	Zu prüfende Datei
	 * @return	Prüfungsergebnis
	 */
	public static DataCheckResult checkFile(final ModelElementBox element, final String fileName) {
		if (fileName==null || fileName.isEmpty()) return new DataCheckResult(element,DataType.FILE,"",Language.tr("DataCheck.NoInputFile"));

		final File file=new File(fileName);
		if (!file.isFile()) return new DataCheckResult(element,DataType.FILE,fileName,Language.tr("DataCheck.FileDoesNotExist"));

		return new DataCheckResult(element,DataType.FILE,fileName);
	}

	/**
	 * Prüft die Verbindung zu einer Datenbank und liefert ein entsprechendes {@link DataCheckResult}-Objekt zurück.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @param dbSettings	Zu prüfende Datenbankverbindung
	 * @return	Prüfungsergebnis
	 */
	public static DataCheckResult checkDB(final ModelElementBox element, final DBSettings dbSettings) {
		try (final DBConnect dbConnect=new DBConnect(dbSettings,false)) {
			final String error=dbConnect.getInitError();
			final String data=dbSettings.toString();
			if (error==null) return new DataCheckResult(element,DataType.DB,data); else return new DataCheckResult(element,DataType.DB,data,error);
		}
	}

	/**
	 * Prüft eine DDE-Verbindung und liefert ein entsprechendes {@link DataCheckResult}-Objekt zurück.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @param workbook	Arbeitsmappe auf die über die DDE-Verbindung zugegriffen werden soll
	 * @param table	Tabelle auf die über die DDE-Verbindung zugegriffen werden soll
	 * @return	Prüfungsergebnis
	 */
	public static DataCheckResult checkDDE(final ModelElementBox element, final String workbook, final String table) {
		if (workbook==null || workbook.isEmpty()) return new DataCheckResult(element,DataType.DDE,"",Language.tr("DataCheck.DDENoWorkbook"));
		if (table==null || table.isEmpty()) return new DataCheckResult(element,DataType.DDE,"",Language.tr("DataCheck.DDENoTable"));
		final String data=workbook+";"+table;

		final DDEConnect dde=new DDEConnect();
		if (!dde.available()) return new DataCheckResult(element,DataType.DDE,data,Language.tr("DataCheck.DDENotAvailable"));

		for (Map.Entry<String,List<String>> entry: dde.listTables().entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(workbook)) continue;
			for (String tableName: entry.getValue()) if (tableName.equalsIgnoreCase(table)) return new DataCheckResult(element,DataType.DDE,data);
			return new DataCheckResult(element,DataType.DDE,data,Language.tr("DataCheck.DDEUnknownTable"));
		}

		return new DataCheckResult(element,DataType.DDE,data,Language.tr("DataCheck.DDEUnknownWorkbook"));
	}

	/**
	 * Prüft eine DDE-Verbindung und liefert ein entsprechendes {@link DataCheckResult}-Objekt zurück.
	 * @param element	Modell-Element auf das sich die Prüfung bezieht
	 * @param workbook	Arbeitsmappe auf die über die DDE-Verbindung zugegriffen werden soll
	 * @param table	Tabelle auf die über die DDE-Verbindung zugegriffen werden soll
	 * @return	Prüfungsergebnis
	 */
	public static DataCheckResult checkDDEAllowEmptyTable(final ModelElementBox element, final String workbook, String table) {
		if (workbook==null || workbook.isEmpty()) return new DataCheckResult(element,DataType.DDE,"",Language.tr("DataCheck.DDENoWorkbook"));
		if (table==null) table="";
		final String data=workbook+";"+table;

		final DDEConnect dde=new DDEConnect();
		if (!dde.available()) return new DataCheckResult(element,DataType.DDE,data,Language.tr("DataCheck.DDENotAvailable"));

		for (Map.Entry<String,List<String>> entry: dde.listTables().entrySet()) {
			if (!entry.getKey().equalsIgnoreCase(workbook)) continue;
			if (table.isEmpty()) {
				if (!entry.getValue().isEmpty()) return new DataCheckResult(element,DataType.DDE,data);
			} else {
				for (String tableName: entry.getValue()) if (tableName.equalsIgnoreCase(table)) return new DataCheckResult(element,DataType.DDE,data);
			}
			return new DataCheckResult(element,DataType.DDE,data,Language.tr("DataCheck.DDEUnknownTable"));
		}

		return new DataCheckResult(element,DataType.DDE,data,Language.tr("DataCheck.DDEUnknownWorkbook"));
	}
}
