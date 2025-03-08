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
package simulator.db;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import language.Language;
import mathtools.NumberTools;

/**
 * Ermöglicht den lesenden und schreibenden Zugriff auf Datenbanken,
 * um Zahlen aus einer Spalte zu lesen oder ganze Zeilen zu schreiben.
 * @author Alexander Herzog
 */
public class DBConnect implements Closeable {
	/**
	 * Sortierfolge bei Abfrage der Daten
	 * @see DBConnect#readTableColumn(String, String, String, SortMode, String[])
	 * @author Alexander Herzog
	 *
	 */
	public enum SortMode {
		/**
		 * Aufsteigende Sortierung
		 */
		ASCENDING("ASC"),

		/**
		 * Absteigende Sortierung
		 */
		DESCENDING("DESC");

		/** SQL-Sortier-Anweisung */
		public final String sql;

		/**
		 * Konstruktor des Enum
		 * @param sql	SQL-Sortier-Anweisung
		 */
		SortMode(final String sql) {
			this.sql=sql;
		}
	}

	/**
	 * Im Konstruktor gewählter Typ.<br>
	 * Kann später nicht mehr geändert werden.
	 */
	public final DBConnectSetup type;

	/**
	 * Im Konstruktor gewählte Konfiguration (Serveradresse, Dateiname usw.).<br>
	 * Kann später nicht mehr geändert werden.
	 */
	public String config;

	/** Datenbankverbindungselement */
	private final Connection connection;
	/** Statemenet-Element innerhalb des Verbindungselement über das konkrete Anfragen ausgeführt werden können */
	private final Statement statement;
	/** Bereits bei der Abarbeitung des Konstruktors aufgetretener Fehler. */
	private String initError;
	/** Liste der Tabellen in der Datenbank */
	private List<String> tableNames;
	/** Listen der Spaltennamen pro Tabelle */
	private Map<String,List<String>> columnNames;

	/**
	 * Konstruktor der Klasse
	 * @param type	Datenbanktyp
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 * @param user	Optional notwendiger Nutzername für den Zugriff auf die Datenbank
	 * @param password	Optional notwendiges Passwort für den Zugriff auf die Datenbank
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbanverzeichnis angelegt werden dürfen
	 * @see DBConnect#getInitError()
	 * @see DBConnect#close()
	 */
	public DBConnect(final DBConnectSetup type, final String config, final String user, final String password, final boolean allowCreateLocalFile) {
		initError=null;
		this.type=type;
		this.config=config;

		if (type==null) {
			initError="No database type selected.";
			connection=null;
			statement=null;
			return;
		}

		/* Treiber laden */
		try {Class.forName(type.driver);} catch (ClassNotFoundException e1) {initError="Driver "+type.driver+" not found.";}

		connection=initConnection((user==null)?"":user,(password==null)?"":password,type.properties,allowCreateLocalFile);
		statement=initStatement();
	}

	/**
	 * Konstruktor der Klasse
	 * @param type	Datenbanktyp
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbanverzeichnis angelegt werden dürfen
	 * @see DBConnect#getInitError()
	 * @see DBConnect#close()
	 */
	public DBConnect(final DBConnectSetup type, final String config, final boolean allowCreateLocalFile) {
		this(type,config,null,null,allowCreateLocalFile);
	}

	/**
	 * Konstruktor der Klasse
	 * @param settings	Einstellungenobjekt aus dem die Konfiguration geladen werden soll
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbanverzeichnis angelegt werden dürfen
	 * @see DBSettings
	 */
	public DBConnect(final DBSettings settings, final boolean allowCreateLocalFile) {
		this((settings==null)?null:DBConnectSetups.getByType(settings.getType()),(settings==null)?null:settings.getProcessedConfig(),(settings==null || settings.getUser().isBlank())?null:settings.getUser(),(settings==null || settings.getPassword().isBlank())?null:settings.getPassword(),allowCreateLocalFile);
	}

	/**
	 * Prüft, ob die lokale Engine nicht extra für diesen Test eine Datei oder ein Verzeichnis anlegt.
	 * @param setup	Datenbanktyp
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #initConnection(String, String, String, boolean)
	 */
	private boolean localDataTest(final DBConnectSetup setup, final String config) {
		if (setup.selectSource.isFile) {
			if (config==null || config.isBlank()) return false;
			final File file=new File(config);
			if (!file.isFile()) {
				initError="File does not exist: "+config;
				return false;
			}
			return true;
		}

		if (setup.selectSource.isFolder) {
			if (config==null || config.isBlank()) return false;
			final File file=new File(config);
			if (!file.isDirectory()) {
				initError="Folder does not exist: "+config;
				return false;
			}
			return true;
		}

		/* Bei Remote-Server ist dieser Test immer ok. */
		return true;
	}

	/**
	 * Versucht eine Verbindung zu der Datenbank herzustellen
	 * @param user	Optional notwendiger Nutzername für den Zugriff auf die Datenbank
	 * @param password	Optional notwendiges Passwort für den Zugriff auf die Datenbank
	 * @param settings	Optionale weitere Einstellungen für dne Zugriff auf die Datenbank
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbankverzeichnis angelegt werden dürfen
	 * @return	Liefert im Erfolgsfall ein Verbindungs-Objekt, sonst <code>null</code>
	 * @see #initError
	 */
	private Connection initConnection(String user, String password, String settings, final boolean allowCreateLocalFile) {
		if (initError!=null) return null;
		if (type==null || config==null || config.isEmpty()) {
			initError=Language.tr("ModelDescription.Database.NoSetup");
			return null;
		}
		if (user==null) user="";
		if (password==null) password="";
		if (settings==null) settings="";

		String configAddon="";
		if (allowCreateLocalFile) {
			if (type.processSettings.createFileConfig!=null) configAddon=type.processSettings.createFileConfig;
		} else {
			if (!localDataTest(type,config)) return null;
		}

		/* Zweiter Aufbereitungsschritt (nach der Prüfung der Datei auf Existenz) */
		config=type.processSettings.processcor2.apply(config);

		try {
			if (!user.isEmpty() || !password.isEmpty() || !settings.isEmpty()) {
				Properties properties=new Properties();
				if (!user.isEmpty() || !password.isEmpty()) {
					properties.setProperty("user",user);
					properties.setProperty("password",password);
				}
				if (!settings.isEmpty()) {
					final String[] parts=settings.split("=");
					if (parts.length==2) properties.setProperty(parts[0].trim(),parts[1].trim());
				}
				return DriverManager.getConnection("jdbc:"+type.connector+":"+config+configAddon,properties);
			} else {
				return DriverManager.getConnection("jdbc:"+type.connector+":"+config+configAddon);
			}
		} catch (SQLException e) {
			initError=e.getMessage();
			return null;
		}
	}

	/**
	 * Erstellt basierend auf {@link #connection} ein {@link Statement}-Objekt,
	 * um Datenbank-Anfragen ausführen zu können.
	 * @return	Liefert im Erfolgsfall ein Statement-Objekt, sonst <code>null</code>
	 */
	private Statement initStatement() {
		if (connection==null) return null;
		try {
			return connection.createStatement();
		} catch (SQLException e) {
			initError=e.getMessage();
			return null;
		}
	}

	/**
	 * Ist im Konstruktor ein Fehler aufgetreten, so kann dieser über diese
	 * Methode abgefragt werden.
	 * @return	Fehler beim Aufbauen der Verbindung oder <code>null</code>, wenn die Verbindung erfolgreich hergestellt werden konnte
	 */
	public String getInitError() {
		return initError;
	}

	/**
	 * Schließt die Verbindung zur Datenbank, wenn diese zuvor im Konstruktor
	 * erfolgreich hergestellt werden konnte (siehe {@link DBConnect#getInitError()}.
	 * Konnte zuvor keine Verbindung hergestellt werden, so tut diese Methode
	 * auch nichts weiter (und beschwert sich darüber auch nicht).
	 */
	@Override
	public void close() {
		if (statement!=null) try {if (!statement.isClosed()) statement.close();} catch (SQLException e) {}
		if (connection!=null) try {if (!connection.isClosed()) connection.close();} catch (SQLException e) {}
	}

	/**
	 * Liest die Liste der Tabellen in einer Datenbank aus einer entsprechenden SQL-Antwort aus.
	 * @param list	SQL-Antwort
	 * @return	Liste der Tabellen in der Datenbank
	 * @throws SQLException	Wird ausgelöst, wenn beim Lesen der SQL-Daten ein Fehler aufgetreten ist
	 */
	private List<String> defaultTablesListReader(final ResultSet list) throws SQLException {
		final List<String> tables=new ArrayList<>();

		while (list.next()) {
			tables.add(list.getString(1).trim());
		}
		return tables;
	}

	/**
	 * Liefert eine Liste aller in der Datenbank enthaltenen Tabellen.
	 * @return	Liste aller Tabellen in der Datenbank (kann leer sein, aber ist nie <code>null</code>)
	 * @see #listTables()
	 */
	private List<String> buildTableNamesList() {
		if (statement==null) return new ArrayList<>();

		try (ResultSet result=statement.executeQuery(type.listAllTablesCommand)) {
			return defaultTablesListReader(result);
		} catch (SQLException e) {return new ArrayList<>();}
	}

	/**
	 * Liefert eine Liste aller in der Datenbank enthaltenen Tabellen
	 * @return	Liste aller Tabellen in der Datenbank (kann ein leeres Array sein, aber ist nie <code>null</code>)
	 */
	public String[] listTables() {
		if (tableNames==null) tableNames=buildTableNamesList();
		return tableNames.toArray(String[]::new);
	}

	/**
	 * Wandelt einen Tabellennamen in den Tabellennamen mit korrekter Groß- und Kleinschreibung um
	 * @param tableName	Tabellenname
	 * @return	Tabellenname in korrekter Groß- und Kleinschreibung
	 */
	private String getExactTableName(final String tableName) {
		if (tableNames==null) tableNames=buildTableNamesList();
		for (String s: tableNames) if (s.equalsIgnoreCase(tableName)) return s.replace(";","");
		return null;
	}

	/**
	 * Liefert die Spaltennamen einer Tabelle
	 * @param result	SQL-Rückgabewert der die Spaltennamen enthält
	 * @return	Spaltennamen in der Tabelle
	 * @throws SQLException	Wird ausgelöst, wenn das SQL-Objekt die Anforderungen nicht erfüllen kann
	 * @see #buildColumnNamesList(String)
	 */
	private List<String> buildColumnNamesList(final ResultSet result) throws SQLException {
		final ResultSetMetaData meta=result.getMetaData();
		final int colCount=meta.getColumnCount();
		final List<String> colNames=new ArrayList<>(colCount);
		for (int i=0;i<colCount;i++) colNames.add(meta.getColumnName(i+1));
		return colNames;
	}

	/**
	 * Liefert die Spaltennamen einer Tabelle
	 * in einer Firebird-Datenbank
	 * @param result	SQL-Rückgabewert der die Spaltennamen enthält
	 * @return	Spaltennamen in der Tabelle
	 * @throws SQLException	Wird ausgelöst, wenn das SQL-Objekt die Anforderungen nicht erfüllen kann
	 * @see #buildColumnNamesList(String)
	 */
	private List<String> buildColumnNamesListFirebird(final ResultSet result) throws SQLException {
		final List<String> columnNames=new ArrayList<>();
		while (result.next()) columnNames.add(result.getString(1).trim());
		return columnNames;
	}

	/**
	 * Liefert die Spaltennamen einer Tabelle
	 * @param exactTableName	Name der Tabelle
	 * @return	Spaltennamen in der Tabelle
	 */
	private List<String> buildColumnNamesList(final String exactTableName) {
		if (statement==null) return new ArrayList<>();

		try (ResultSet result=statement.executeQuery("SELECT TOP 1 * FROM "+exactTableName+";")) {
			return buildColumnNamesList(result);
		} catch (SQLException e) {}

		try (ResultSet result=statement.executeQuery("SELECT * FROM "+exactTableName+" LIMIT 1;")) {
			return buildColumnNamesList(result);
		} catch (SQLException e) {}

		try (ResultSet result=statement.executeQuery("SELECT * FROM "+exactTableName+" WHERE ROWNUM <= 1;")) {
			return buildColumnNamesList(result);
		} catch (SQLException e) {}

		try (ResultSet result=statement.executeQuery("SELECT * FROM "+exactTableName+" fetch first 1 rows only")) {
			return buildColumnNamesList(result);
		} catch (SQLException e) {}

		try (ResultSet result=statement.executeQuery("select rdb$field_name from rdb$relation_fields where rdb$relation_name='"+exactTableName+"';")) {
			return buildColumnNamesListFirebird(result);
		} catch (SQLException e) {}

		return new ArrayList<>();
	}

	/**
	 * Liefert eine Liste der Namen der Spalten in der angegebenen Tabelle
	 * @param tableName	Name der Tabelle deren Spaltennamen ausgelesen werden sollen
	 * @return	Liste der Namen der Spalten in der Tabellen (kann ein leeres Array sein, aber ist nie <code>null</code>)
	 */
	public String[] listColumns(final String tableName) {
		final String exactTableName=getExactTableName(tableName);
		if (exactTableName==null) return new String[0];

		if (columnNames==null) columnNames=new HashMap<>();
		if (!columnNames.containsKey(exactTableName)) columnNames.put(exactTableName,buildColumnNamesList(exactTableName));
		return columnNames.get(exactTableName).toArray(String[]::new);
	}

	/**
	 * Liefert eine Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen.
	 * @return	Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen
	 */
	public Map<String,List<String>> listAll() {
		for (String tableName: listTables()) listColumns(tableName);
		final Map<String,List<String>> result=new HashMap<>();
		if (columnNames!=null) for (Map.Entry<String,List<String>> entry: columnNames.entrySet()) result.put(entry.getKey(),new ArrayList<>(entry.getValue()));
		return result;
	}

	/**
	 * Liefert die Nummer einer Spalte in einer Tabelle.
	 * @param exactTableName	Name der Tabelle
	 * @param columnName	Name der Spalte
	 * @return	Nummer der Spalte oder -1, wenn Tabelle oder Spalte nicht gefunden werden konnten
	 */
	private int getColumnNumber(final String exactTableName, final String columnName) {
		if (columnName==null || columnName.isBlank()) return 0; /* Wenn keine Spalte angegeben, erste Spalte verwenden */

		final String[] names=listColumns(exactTableName);
		for (int i=0;i<names.length;i++) if (names[i].equalsIgnoreCase(columnName)) return i;

		final Long L=NumberTools.getPositiveLong(columnName); /* Bezeichner als 1-basierende Spaltennummer interpretieren */
		if (L!=null) return L.intValue()-1;

		return -1;
	}

	/**
	 * Liefert den Namen einer Spalte in korrekter Groß- und Kleinschreibung.
	 * @param exactTableName	Name der Tabelle
	 * @param columnName	Name der Spalte
	 * @return	Name der Spalte in korrekter Groß- und Kleinschreibung
	 */
	private String getExactColumnName(final String exactTableName, final String columnName) {
		if (columnName==null || columnName.isBlank()) return null;

		for (String name: listColumns(exactTableName)) if (name.equalsIgnoreCase(columnName)) return name;
		return null;
	}

	/**
	 * Liefert einen Iterator, der die Zahlenwerte in einer Spalte der angegebenen Tabelle in Reihenfolge der Sortierung einer anderen Spalte liefert.
	 * @param tableName	Name der Tabelle von der die Zahlenwerte einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zahlenwerte geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String übergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @param sortColumn	Spalte nach der die Ausgabe sortiert werden soll (Wird hier oder für den Modus <code>null</code> übergeben, so erfolgt keine Sortierung. Wird ein ungültiger Spaltenname angegeben, so werden keine Daten ausgegeben.)
	 * @param sortMode	Art der Sortierung	(Wird hier oder für die Sortierspalte <code>null</code> übergeben, so erfolgt keine Sortierung.)
	 * @param additionalColumns	Optionale Liste mit weiteren Spaltennamen, deren Inhalte ausgelesen werden sollen. (Es kann sowohl der Parameter <code>null</code> sein als auch einzelne Einträge.)
	 * @return	Iterator über die Zahlenwerte in der angegebenen Spalte der angegebenen Tabelle
	 * @see DBConnect.SortMode
	 * @see DBConnect#readAdditionalColumn(Iterator, int)
	 */
	@SuppressWarnings("resource")
	public Iterator<Double> readTableColumn(final String tableName, final String columnName, final String sortColumn, final SortMode sortMode, final String[] additionalColumns) {
		/* Tabelle vorhanden? */
		final String exactTableName=getExactTableName(tableName);
		if (exactTableName==null) return new TableReadDoubleIterator();

		/* Namen aller Spalten */
		final String[] columnNames=listColumns(exactTableName);

		/* Select-Spalten */
		final List<String> selectColumns=new ArrayList<>();

		/* Select-Spalte vorhanden? */
		final int columnNumber=getColumnNumber(exactTableName,columnName);
		if (columnNumber<0) return new TableReadDoubleIterator();
		if (type.useQuotes) {
			selectColumns.add("\""+columnNames[columnNumber]+"\"");
		} else {
			selectColumns.add(columnNames[columnNumber]);
		}

		/* Sortierung */
		String orderQuery="";
		if (sortColumn!=null && sortMode!=null) {
			final String exactSortColumnName=getExactColumnName(exactTableName,sortColumn);
			if (exactSortColumnName==null) return new TableReadDoubleIterator();
			if (type.useQuotes) {
				orderQuery=" ORDER BY \""+exactSortColumnName+"\" "+sortMode.sql;
			} else {
				orderQuery=" ORDER BY "+exactSortColumnName+" "+sortMode.sql;
			}
		}

		/* Zusätzlich auszulesende Spalten */
		final String[] exactAdditionalColumnName=new String[(additionalColumns==null)?0:additionalColumns.length];
		if (additionalColumns!=null) for (int i=0;i<exactAdditionalColumnName.length;i++) {
			String col;
			if (type.useQuotes) {
				col="\""+getExactColumnName(exactTableName,additionalColumns[i])+"\"";
			} else {
				col=getExactColumnName(exactTableName,additionalColumns[i]);
			}
			if (!selectColumns.contains(col)) selectColumns.add(col);
			exactAdditionalColumnName[i]=col;
		}

		/* Iterator bauen */
		final String selectCols=String.join(", ",selectColumns.toArray(String[]::new)).replace(";","");
		final String primary=selectColumns.get(0);
		final List<String> secondary=new ArrayList<>(selectColumns); secondary.remove(0);
		try {
			return new TableReadDoubleIterator(statement.executeQuery("SELECT "+selectCols+" FROM "+exactTableName+orderQuery+";"),primary,secondary.toArray(String[]::new));
		} catch (SQLException e1) {
			try {
				return new TableReadDoubleIterator(statement.executeQuery("SELECT "+selectCols+" FROM "+exactTableName+orderQuery),primary,secondary.toArray(String[]::new));
			} catch (SQLException e2) {
				return new TableReadDoubleIterator();
			}
		}
	}

	/**
	 * Liefert einen Iterator, der die Zahlenwerte in einer Spalte der angegebenen Tabelle in der Reihenfolge, in der sie in der Tabellen stehen, liefert.
	 * @param tableName	Name der Tabelle von der die Zahlenwerte einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zahlenwerte geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String übergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @return	Iterator über die Zahlenwerte in der angegebenen Spalte der angegebenen Tabelle
	 */
	public Iterator<Double> readTableColumn(final String tableName, final String columnName) {
		return readTableColumn(tableName,columnName,null,null,null);
	}

	/**
	 * Liefert einen Iterator, der die Zeichenketten in einer Spalte der angegebenen Tabelle in Reihenfolge der Sortierung einer anderen Spalte liefert.
	 * @param tableName	Name der Tabelle von der die Zeichenketten einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zeichenketten geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String übergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @param sortColumn	Spalte nach der die Ausgabe sortiert werden soll (Wird hier oder für den Modus <code>null</code> übergeben, so erfolgt keine Sortierung. Wird ein ungültiger Spaltenname angegeben, so werden keine Daten ausgegeben.)
	 * @param sortMode	Art der Sortierung	(Wird hier oder für die Sortierspalte <code>null</code> übergeben, so erfolgt keine Sortierung.)
	 * @param additionalColumns	Optionale Liste mit weiteren Spaltennamen, deren Inhalte ausgelesen werden sollen. (Es kann sowohl der Parameter <code>null</code> sein als auch einzelne Einträge.)
	 * @return	Iterator über die Zeichenketten in der angegebenen Spalte der angegebenen Tabelle
	 * @see DBConnect.SortMode
	 * @see DBConnect#readAdditionalColumnString(Iterator, int)
	 */
	@SuppressWarnings("resource")
	public Iterator<String> readStringTableColumn(final String tableName, final String columnName, final String sortColumn, final SortMode sortMode, final String[] additionalColumns) {
		/* Tabelle vorhanden? */
		final String exactTableName=getExactTableName(tableName);
		if (exactTableName==null) return new TableReadStringIterator();

		/* Namen aller Spalten */
		final String[] columnNames=listColumns(exactTableName);

		/* Select-Spalten */
		final List<String> selectColumns=new ArrayList<>();

		/* Select-Spalte vorhanden? */
		final int columnNumber=getColumnNumber(exactTableName,columnName);
		if (columnNumber<0) return new TableReadStringIterator();
		selectColumns.add("\""+columnNames[columnNumber]+"\"");

		/* Sortierung */
		String orderQuery="";
		if (sortColumn!=null && sortMode!=null) {
			final String exactSortColumnName=getExactColumnName(exactTableName,sortColumn);
			if (exactSortColumnName==null) return new TableReadStringIterator();
			orderQuery=" ORDER BY \""+exactSortColumnName+"\" "+sortMode.sql;
		}

		/* Zusätzlich auszulesende Spalten */
		final String[] exactAdditionalColumnName=new String[(additionalColumns==null)?0:additionalColumns.length];
		if (additionalColumns!=null) for (int i=0;i<exactAdditionalColumnName.length;i++) {
			String col="\""+getExactColumnName(exactTableName,additionalColumns[i])+"\"";
			if (!selectColumns.contains(col)) selectColumns.add(col);
			exactAdditionalColumnName[i]=col;
		}

		/* Iterator bauen */
		try {
			final String selectCols=String.join(", ",selectColumns.toArray(String[]::new)).replace(";","");
			final String primary=selectColumns.get(0);
			final List<String> secondary=new ArrayList<>(selectColumns); secondary.remove(0);
			return new TableReadStringIterator(statement.executeQuery("SELECT "+selectCols+" FROM "+exactTableName+orderQuery+";"),primary,secondary.toArray(String[]::new));
		} catch (SQLException e) {
			return new TableReadStringIterator();
		}
	}

	/**
	 * Liefert einen Iterator, der die Zeichenketten in einer Spalte der angegebenen Tabelle in der Reihenfolge, in der sie in der Tabellen stehen, liefert.
	 * @param tableName	Name der Tabelle von der die Zeichenketten einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zeichenketten geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String übergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @return	Iterator über die Zeichenketten in der angegebenen Spalte der angegebenen Tabelle
	 */
	public Iterator<String> readStringTableColumn(final String tableName, final String columnName) {
		return readStringTableColumn(tableName,columnName,null,null,null);
	}

	/**
	 * Liefert einen der zusätzlich angefragten Spalteninhalte zu dem letzten per <code>next()</code> abgefragten Wert.
	 * @param iterator	Iterator, der über {@link DBConnect#readTableColumn(String, String, String, SortMode, String[])} angefragt wurde
	 * @param additionalIndex	Index der gewünschten Spalte (bezieht sich auf den <code>additionalColumns</code>-Parameter in der Anfrage
	 * @return	Wert in der gewünschten Spalte oder <code>null</code>, wenn es die Spalte nicht gibt oder keine Wert enthält.
	 * @see DBConnect#readTableColumn(String, String, String, SortMode, String[])
	 */
	public String readAdditionalColumn(final Iterator<Double> iterator, final int additionalIndex) {
		if (!(iterator instanceof TableReadDoubleIterator)) return null;
		final TableReadDoubleIterator tableIterator=(TableReadDoubleIterator)iterator;

		if (additionalIndex<0 || additionalIndex>=tableIterator.additional.length) return null;
		return tableIterator.additional[additionalIndex];
	}

	/**
	 * Liefert einen der zusätzlich angefragten Spalteninhalte zu dem letzten per <code>next()</code> abgefragten Wert.
	 * @param iterator	Iterator, der über {@link DBConnect#readTableColumn(String, String, String, SortMode, String[])} angefragt wurde
	 * @param additionalIndex	Index der gewünschten Spalte (bezieht sich auf den <code>additionalColumns</code>-Parameter in der Anfrage
	 * @return	Wert in der gewünschten Spalte oder <code>null</code>, wenn es die Spalte nicht gibt oder keine Wert enthält.
	 * @see DBConnect#readTableColumn(String, String, String, SortMode, String[])
	 */
	public String readAdditionalColumnString(final Iterator<String> iterator, final int additionalIndex) {
		if (!(iterator instanceof TableReadStringIterator)) return null;
		final TableReadStringIterator tableIterator=(TableReadStringIterator)iterator;

		if (additionalIndex<0 || additionalIndex>=tableIterator.additional.length) return null;
		return tableIterator.additional[additionalIndex];
	}

	/**
	 * Iterator-Klasse für {@link Double}-Werte aus einer Tabellenspalte
	 */
	private static class TableReadDoubleIterator implements Iterator<Double> {
		/** SQL-Antwort */
		private final ResultSet result;
		/** Liste mit allen in der Tabelle vorhandenen Spaltennamen */
		private final String[] columnNames;
		/** Index der für den Iterator relevanten Datenspalte */
		private final int numberColumnIndex;
		/** Indices der zusätzlichen Datenspalten für die weiteren Werte */
		private final int[] additionalColumnsIndex;
		/** Nächster auszuliefernder Wert (wird für {@link #hasNext()} bereits einen Aufruf früher gelesen) */
		private Double next;
		/** Zusätzlicher Werte für die nächste Abfrage*/
		private String[] additionalNext;
		/** Aktuelles Set zusätzlicher Werte */
		private String[] additional;

		/**
		 * Konstruktor der Klasse<br>
		 * Erstellt einen leeren Iterator, der keine Daten liefert.
		 */
		private TableReadDoubleIterator() {
			this(null,null,null);
		}

		/**
		 * Konstruktor der Klasse
		 * @param result	SQL-Antwort
		 * @param numberColumn	Name der Spalte
		 * @param additionalColumns	Namen der Spalten für weitere optionale Antwortwerte
		 */
		private TableReadDoubleIterator(final ResultSet result, final String numberColumn, final String[] additionalColumns) {
			this.result=result;
			columnNames=getColumnNames();
			numberColumnIndex=getColumnIndex(numberColumn);
			additionalColumnsIndex=new int[(additionalColumns==null)?0:additionalColumns.length];
			if (additionalColumns!=null) for (int i=0;i<additionalColumns.length;i++) additionalColumnsIndex[i]=getColumnIndex(additionalColumns[i]);

			additional=new String[additionalColumnsIndex.length];
			additionalNext=new String[additionalColumnsIndex.length];
			next=null;

			if (result!=null && numberColumnIndex>=0) readNext();
		}

		/**
		 * Liefert eine Liste mit allen in der Tabelle vorhandenen Spaltennamen.
		 * @return	Liste mit allen in der Tabelle vorhandenen Spaltennamen
		 */
		private String[] getColumnNames() {
			final List<String> columnNamesList=new ArrayList<>();

			if (result!=null) try {
				final ResultSetMetaData meta=result.getMetaData();
				for (int i=0;i<meta.getColumnCount();i++) columnNamesList.add(meta.getColumnName(i+1));
			} catch (SQLException e) {}

			return columnNamesList.toArray(String[]::new);
		}

		/**
		 * Liefert den Spaltenindex einer Tabellenspalte.
		 * @param name	Name der Tabellenspalte
		 * @return	Zugehöriger Index oder -1, wenn die Tabelle keine entsprechende Spalte enthält
		 */
		private int getColumnIndex(String name) {
			if (name==null || name.isBlank()) return -1;

			if (name.startsWith("\"") && name.endsWith("\"")) name=name.substring(1,name.length()-1);

			for (int i=0;i<columnNames.length;i++) if (columnNames[i].equalsIgnoreCase(name)) return i;

			return -1;
		}

		/**
		 * Liest den nächsten Wert und speichert ihn in {@link #next}.
		 * Das muss vor der eigentlichen Anfrage des Wertes passieren,
		 * damit {@link #hasNext()} eine sinnvolle Antwort geben kann.
		 * Zusätzlich werden auch die weiteren Spaltenwerte gelesen.
		 */
		private void readNext() {
			next=null;
			Arrays.fill(additionalNext,null);

			try {
				while (result.next()) {
					final String cell=result.getString(numberColumnIndex+1);
					final Double D=NumberTools.getDouble(cell);
					if (D!=null) {
						next=D;
						for (int i=0;i<additionalColumnsIndex.length;i++) if (additionalColumnsIndex[i]>=0) additionalNext[i]=result.getString(additionalColumnsIndex[i]+1);
						return;
					}
				}
			} catch (SQLException e) {return;}
		}

		@Override
		public boolean hasNext() {
			return next!=null;
		}

		@Override
		public Double next() {
			if (next==null) return null;
			final Double d=next;
			additional=Arrays.copyOf(additionalNext,additionalNext.length);
			readNext();
			return d;
		}
	}

	/**
	 * Iterator-Klasse für {@link String}-Werte aus einer Tabellenspalte
	 */
	private static class TableReadStringIterator implements Iterator<String> {
		/** SQL-Antwort */
		private final ResultSet result;
		/** Liste mit allen in der Tabelle vorhandenen Spaltennamen */
		private final String[] columnNames;
		/** Index der für den Iterator relevanten Datenspalte */
		private final int numberColumnIndex;
		/** Indices der zusätzlichen Datenspalten für die weiteren Werte */
		private final int[] additionalColumnsIndex;
		/** Nächster auszuliefernder Wert (wird für {@link #hasNext()} bereits einen Aufruf früher gelesen) */
		private String next;
		/** Zusätzlicher Werte für die nächste Abfrage*/
		private String[] additionalNext;
		/** Aktuelles Set zusätzlicher Werte */
		private String[] additional;

		/**
		 * Konstruktor der Klasse<br>
		 * Erstellt einen leeren Iterator, der keine Daten liefert.
		 */
		private TableReadStringIterator() {
			this(null,null,null);
		}

		/**
		 * Konstruktor der Klasse
		 * @param result	SQL-Antwort
		 * @param numberColumn	Name der Spalte
		 * @param additionalColumns	Namen der Spalten für weitere optionale Antwortwerte
		 */
		private TableReadStringIterator(final ResultSet result, final String numberColumn, final String[] additionalColumns) {
			this.result=result;
			columnNames=getColumnNames();
			numberColumnIndex=getColumnIndex(numberColumn);
			additionalColumnsIndex=new int[(additionalColumns==null)?0:additionalColumns.length];
			if (additionalColumns!=null) for (int i=0;i<additionalColumns.length;i++) additionalColumnsIndex[i]=getColumnIndex(additionalColumns[i]);

			additional=new String[additionalColumnsIndex.length];
			additionalNext=new String[additionalColumnsIndex.length];
			next=null;

			if (result!=null && numberColumnIndex>=0) readNext();
		}

		/**
		 * Liefert eine Liste mit allen in der Tabelle vorhandenen Spaltennamen.
		 * @return	Liste mit allen in der Tabelle vorhandenen Spaltennamen
		 */
		private String[] getColumnNames() {
			final List<String> columnNamesList=new ArrayList<>();

			if (result!=null) try {
				final ResultSetMetaData meta=result.getMetaData();
				for (int i=0;i<meta.getColumnCount();i++) columnNamesList.add(meta.getColumnName(i+1));
			} catch (SQLException e) {}

			return columnNamesList.toArray(String[]::new);
		}

		/**
		 * Liefert den Spaltenindex einer Tabellenspalte.
		 * @param name	Name der Tabellenspalte
		 * @return	Zugehöriger Index oder -1, wenn die Tabelle keine entsprechende Spalte enthält
		 */
		private int getColumnIndex(String name) {
			if (name==null || name.isBlank()) return -1;

			if (name.startsWith("\"") && name.endsWith("\"")) name=name.substring(1,name.length()-1);

			for (int i=0;i<columnNames.length;i++) if (columnNames[i].equalsIgnoreCase(name)) return i;

			return -1;
		}

		/**
		 * Liest den nächsten Wert und speichert ihn in {@link #next}.
		 * Das muss vor der eigentlichen Anfrage des Wertes passieren,
		 * damit {@link #hasNext()} eine sinnvolle Antwort geben kann.
		 * Zusätzlich werden auch die weiteren Spaltenwerte gelesen.
		 */
		private void readNext() {
			next=null;
			Arrays.fill(additionalNext,null);

			try {
				if (result.next()) {
					next=result.getString(numberColumnIndex+1);
					for (int i=0;i<additionalColumnsIndex.length;i++) if (additionalColumnsIndex[i]>=0) additionalNext[i]=result.getString(additionalColumnsIndex[i]+1);
				}
			} catch (SQLException e) {return;}
		}

		@Override
		public boolean hasNext() {
			return next!=null;
		}

		@Override
		public String next() {
			if (next==null) return null;
			final String s=next;
			additional=Arrays.copyOf(additionalNext,additionalNext.length);
			readNext();
			return s;
		}
	}

	/**
	 * Fügt eine Zeile an eine Tabelle an
	 * @param table	Tabelle an die die Zeile angefügt werden soll
	 * @param columns	Namen der Spalten in die die Werte geschrieben werden sollen
	 * @param values	Werte die geschrieben werden sollen
	 * @return	Liefert im Erfolgsfall <code>true</code> zurück
	 */
	public boolean writeRow(final String table, final String[] columns, final String[] values) {
		if (statement==null) return false;

		final StringBuilder sb=new StringBuilder();
		sb.append("Insert into ");
		sb.append(table);
		sb.append(" (");
		sb.append(String.join(", ",columns));
		sb.append(") Values (");
		for (int i=0;i<values.length;i++) {
			if (i>0) sb.append(", ");
			final String s=values[i].replaceAll("'","\\'");
			sb.append("'");
			sb.append(s);
			sb.append("'");
		}
		sb.append(")");

		try {
			statement.execute(sb.toString());
			return statement.getUpdateCount()==1;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Liefert eine Liste mit den (Klassen-)Namen aller aktiven JDBC-Treibern
	 * @return	Liste der Namen aller aktiven JDBC-Treibern
	 */
	public static String[] getDriverList() {
		final List<String> names=new ArrayList<>();

		final Enumeration<Driver> drivers=DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			final Driver driver=drivers.nextElement();
			names.add(driver.getClass().getName());
		}

		return names.toArray(String[]::new);
	}

	/**
	 * Erstellt eine Dummy-Tabelle in einer Datenbank
	 * @param output	Ausgaben werden über dieses Callback ausgegeben. Es darf nicht <code>null</code> sein.
	 * @param tableName	Name der anzulegenden Tabelle
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean testAddDummyTable(final Consumer<String> output, final String tableName) {
		final String error=getInitError();
		if (error!=null) {output.accept("Init error: "+error); return false;}

		if (statement==null) return false;
		try {
			statement.execute("Create Table "+tableName+" (\"id\" integer, \"value\" varchar(255));");
			statement.execute("INSERT INTO "+tableName+" (\"id\", \"value\") VALUES (2, 456);");
			statement.execute("INSERT INTO "+tableName+" (\"id\", \"value\") VALUES (3, 123);");
		} catch (SQLException e) {
			output.accept(e.getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Liest Informationen über die in einer Datenbank enthaltenen Tabellen und dann die Daten aus einer Tabelle aus.
	 * @param output	Ausgaben werden über dieses Callback ausgegeben. Es darf nicht <code>null</code> sein.
	 * @param tableName	Tabelle deren Daten ausgelesen werden sollen
	 */
	public void testReadDummyData(final Consumer<String> output, final String tableName) {
		final String error=getInitError();
		if (error!=null) {output.accept("Init error: "+error); return;}

		output.accept("Tables: "+String.join(", ",listTables()));
		output.accept("Columns in \""+tableName+"\": "+String.join(", ",listColumns(tableName)));
		output.accept("Data in \""+tableName+"\":");
		final Iterator<Double> iterator=readTableColumn(tableName,"value","id",DBConnect.SortMode.DESCENDING,new String[]{"id"});
		while (iterator.hasNext()) {
			final Double D=iterator.next();
			output.accept("  value="+NumberTools.formatNumber(D.doubleValue())+", id="+readAdditionalColumn(iterator,0));
		}
	}

	/*
		final DBSettings settings;
		settings=new DBSettings(DBConnect.DBType.SQLITE_FILE,"C:\\Users\\Alexander Herzog\\Desktop\\DB2.s3db");
		//settings=new DBSettings(DBConnect.DBType.HSQLDB_SERVER,"//localhost/","SA","");
		//settings=new DBSettings(DBConnect.DBType.HSQLDB_LOCAL,"C:\\Users\\Alexander Herzog\\Desktop\\DB\\","SA","");
		//settings=new DBSettings(DBConnect.DBType.POSTGRESQL_SERVER,"//localhost/DB","User","User");
		//settings=new DBSettings(DBConnect.DBType.MARIADB_SERVER,"//localhost:3306/test","User","User");
		// settings=new DBSettings(DBConnect.DBType.FIREBIRD_SERVER,"//localhost:3050/test.fdb","dbuser","dbuser");
		try (DBConnect db=new DBConnect(settings,true)) {
			if (!db.testAddDummyTable(System.out::println,"Tab1")) return;
			db.testReadDummyData(System.out::println,"Tab1");
		}
	 */
}