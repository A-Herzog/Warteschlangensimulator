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
import java.util.function.Function;

import language.Language;
import mathtools.NumberTools;

/**
 * Erm�glicht den lesenden und schreibenden Zugriff auf Datenbanken,
 * um Zahlen aus einer Spalte zu lesen oder ganze Zeilen zu schreiben.
 * @author Alexander Herzog
 */
public class DBConnect implements Closeable {
	/**
	 * Liste der unterst�tzten Datenbankformate
	 * @author Alexander Herzog
	 */
	public enum DBType {
		/**
		 * SQLite-Dateien<br>
		 * Hinweis: Config ist Dateiname, Nutzername und Passwort werden nicht verwendet.
		 */
		SQLITE_FILE(
				"SQLite",
				"org.sqlite.JDBC",
				"sqlite",
				"SELECT tbl_name FROM sqlite_master WHERE type='table';",
				null,
				true),

		/**
		 * HSQLDB (Hyper Structured Query Language Database) in Verzeichnis<br>
		 * Hinweis: Config ist Pfad und muss abschlie�enden Trenner beinhalten, Nutzername ist "SA" und Passwort "".
		 */
		HSQLDB_LOCAL(
				"HSQLDB Local",
				"org.hsqldb.jdbc.JDBCDriver",
				"hsqldb:file",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE='TABLE';",
				null,
				true),

		/**
		 * HSQLDB (Hyper Structured Query Language Database) �ber Server<br>
		 * Hinweis: Config ist Serveradresse und muss mit "//" beginnen und mit "/" enden. Im Standardfall sind Nutzername "SA" und Passwort "".
		 */
		HSQLDB_SERVER(
				"HSQLDB Server",
				"org.hsqldb.jdbc.JDBCDriver",
				"hsqldb:hsql",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE='TABLE';",
				null,
				true),

		/**
		 * Postgre SQL �ber Server<br>
		 * Hinweis: Config ist Serveradresse und Datenbankname im Format "//host/db". Nutzername und Passwort m�ssen gesetzt sein.
		 */
		POSTGRESQL_SERVER(
				"PostgreSQL Server",
				"org.postgresql.Driver",
				"postgresql",
				"SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';",
				null,
				true),

		/**
		 * MariaDB �ber Server<br>
		 * Hinweis: Config ist Serveradresse und Datenbankname im Format "//host:3306/db". Nutzername und Passwort m�ssen gesetzt sein.
		 */
		MARIADB_SERVER(
				"MariaDB Server",
				"org.mariadb.jdbc.Driver",
				"mariadb",
				"SHOW TABLES;",
				null,
				true),

		/**
		 * Firebird-Server
		 */
		FIREBIRD_SERVER(
				"Firebird Server",
				"org.firebirdsql.jdbc.FBDriver",
				"firebirdsql",
				"select rdb$relation_name from rdb$relations where rdb$view_blr is null and (rdb$system_flag is null or rdb$system_flag = 0);",
				"encoding=UTF8",
				true),

		/**
		 * Access
		 */
		ACCESS(
				"Access",
				"net.ucanaccess.jdbc.UcanaccessDriver",
				"ucanaccess",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE='TABLE' AND TABLE_SCHEM='PUBLIC';",
				null,
				false,
				s->"//"+s.replace("\\","/"));

		/** Name des Datenbank-Servers */
		public final String name;

		/** Treiber in der Java-Klassen-Notation */
		public final String driver;

		/** Bezeichnung f�r den Konnektor */
		public final String connector;

		/** SQL-Befehl in der entsprechenden Datenbanknotation um die Liste aller Tabellen abzurufen */
		public final String listAllTablesCommand;

		/** D�rfen Spaltenbezeichner in Anf�hrungszeichen gesetzt werden? */
		public final boolean useQuotes;

		/** Optional weitere Einstellungen, die beim Aufbau der Verbindung ben�tigt werden. Kann <code>null</code> sein. */
		public final String properties;

		/** Optionale Funktion zur Verarbeitung der vom Nutzer eingegebenen Einstellungen zur anschlie�enden �bergabe an den DB-Treiber. */
		public final Function<String,String> processSettings;

		DBType(final String name, final String driver, final String connector, final String listAllTablesCommand, final String properties, final boolean useQuotes) {
			this.name=name;
			this.driver=driver;
			this.connector=connector;
			this.listAllTablesCommand=listAllTablesCommand;
			this.properties=properties;
			this.useQuotes=useQuotes;
			this.processSettings=null;
		}

		DBType(final String name, final String driver, final String connector, final String listAllTablesCommand, final String properties, final boolean useQuotes, final Function<String,String> processSettings) {
			this.name=name;
			this.driver=driver;
			this.connector=connector;
			this.listAllTablesCommand=listAllTablesCommand;
			this.properties=properties;
			this.useQuotes=useQuotes;
			this.processSettings=processSettings;
		}

		/**
		 * F�hrt eine Verarbeitung der vom Nutzer eingegebenen Einstellungen zur anschlie�enden �bergabe an den DB-Treiber durch.
		 * @param settings	Nutzereinstellungen
		 * @return	Verarbeitete Einstellungen in der Form in der sie an den Treiber �bergeben werden k�nnen
		 */
		public String processSettings(final String settings) {
			if (settings==null || settings.isEmpty()) return settings;
			if (this.processSettings==null) return settings;
			return this.processSettings.apply(settings);
		}
	}

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

		SortMode(final String sql) {
			this.sql=sql;
		}
	}

	/**
	 * Im Konstruktor gew�hlter Typ.<br>
	 * Kann sp�ter nicht mehr ge�ndert werden.
	 * @see DBConnect.DBType
	 */
	public final DBType type;

	/**
	 * Im Konstruktor gew�hlte Konfiguration (Serveradresse, Dateiname usw.).<br>
	 * Kann sp�ter nicht mehr ge�ndert werden.
	 */
	public final String config;

	private final Connection connection;
	private final Statement statement;
	private String initError;
	private List<String> tableNames;
	private Map<String,List<String>> columnNames;

	/**
	 * Konstruktor der Klasse
	 * @param type	Datenbanktyp (siehe {@link DBConnect.DBType})
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 * @param user	Optional notwendiger Nutzername f�r den Zugriff auf die Datenbank
	 * @param password	Optional notwendiges Passwort f�r den Zugriff auf die Datenbank
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbanverzeichnis angelegt werden d�rfen
	 * @see DBConnect#getInitError()
	 * @see DBConnect#close()
	 */
	public DBConnect(final DBType type, final String config, final String user, final String password, final boolean allowCreateLocalFile) {
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
	 * @param type	Datenbanktyp (siehe {@link DBConnect.DBType})
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbanverzeichnis angelegt werden d�rfen
	 * @see DBConnect#getInitError()
	 * @see DBConnect#close()
	 */
	public DBConnect(final DBType type, final String config, final boolean allowCreateLocalFile) {
		this(type,config,null,null,allowCreateLocalFile);
	}

	/**
	 * Konstruktor der Klasse
	 * @param settings	Einstellungenobjekt aus dem die Konfiguration geladen werden soll
	 * @param allowCreateLocalFile	Soll im Bedarfsfall eine lokale Datenbankdatei / ein lokales Datenbanverzeichnis angelegt werden d�rfen
	 * @see DBSettings
	 */
	public DBConnect(final DBSettings settings, final boolean allowCreateLocalFile) {
		this((settings==null)?null:settings.getType(),(settings==null)?null:settings.getProcessedConfig(),(settings==null || settings.getUser().trim().isEmpty())?null:settings.getUser(),(settings==null || settings.getPassword().trim().isEmpty())?null:settings.getPassword(),allowCreateLocalFile);
	}

	private boolean localDataTest(final DBType type, final String config) {
		/* Pr�ft, ob die lokale Engine nicht extra f�r diesen Test eine Datei oder ein Verzeichnis anlegt. */

		if (type==DBType.SQLITE_FILE) {
			if (config==null || config.trim().isEmpty()) return false;
			final File file=new File(config);
			if (!file.isFile()) {
				initError="File does not exist: "+config;
				return false;
			}
			return true;
		}

		if (type==DBType.HSQLDB_LOCAL) {
			if (config==null || config.trim().isEmpty()) return false;
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

	private Connection initConnection(String user, String password, String settings, final boolean allowCreateLocalFile) {
		if (initError!=null) return null;
		if (type==null || config==null || config.isEmpty()) {
			initError=Language.tr("ModelDescription.Database.NoSetup");
			return null;
		}
		if (user==null) user="";
		if (password==null) password="";
		if (settings==null) settings="";

		if (!allowCreateLocalFile) {
			if (!localDataTest(type,config)) return null;
		}

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
				return DriverManager.getConnection("jdbc:"+type.connector+":"+config,properties);
			} else {
				return DriverManager.getConnection("jdbc:"+type.connector+":"+config);
			}
		} catch (SQLException e) {
			initError=e.getMessage();
			return null;
		}
	}

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
	 * Ist im Konstruktor ein Fehler aufgetreten, so kann dieser �ber diese
	 * Methode abgefragt werden.
	 * @return	Fehler beim Aufbauen der Verbindung oder <code>null</code>, wenn die Verbindung erfolgreich hergestellt werden konnte
	 */
	public String getInitError() {
		return initError;
	}

	/**
	 * Schlie�t die Verbindung zur Datenbank, wenn diese zuvor im Konstruktor
	 * erfolgreich hergestellt werden konnte (siehe {@link DBConnect#getInitError()}.
	 * Konnte zuvor keine Verbindung hergestellt werden, so tut diese Methode
	 * auch nichts weiter (und beschwert sich dar�ber auch nicht).
	 */
	@Override
	public void close() {
		if (statement!=null) try {if (!statement.isClosed()) statement.close();} catch (SQLException e) {}
		if (connection!=null) try {if (!connection.isClosed()) connection.close();} catch (SQLException e) {}
	}

	private List<String> defaultTablesListReader(final ResultSet list, final boolean extendedMode) throws SQLException {
		final List<String> tables=new ArrayList<>();

		final int count=list.getMetaData().getColumnCount();
		/* for (int i=0;i<count;i++) System.out.println(list.getMetaData().getColumnName(i+1)); */

		while (list.next()) {
			if (extendedMode) {
				final StringBuilder sb=new StringBuilder();
				for (int i=0;i<count;i++) {
					if (sb.length()>0) sb.append("\t");
					sb.append(list.getString(i+1));
				}
				tables.add(sb.toString());
			} else {
				tables.add(list.getString(1).trim());
			}
		}
		return tables;
	}

	private List<String> buildTableNamesList() {
		if (statement==null) return new ArrayList<>();

		try (ResultSet result=statement.executeQuery(type.listAllTablesCommand)) {
			return defaultTablesListReader(result,false);
		} catch (SQLException e) {return new ArrayList<>();}
	}

	/**
	 * Liefert eine Liste aller in der Datenbank enthaltenen Tabellen
	 * @return	Liste aller Tabellen in der Datenbank (kann ein leeres Array sein, aber ist nie <code>null</code>)
	 */
	public String[] listTables() {
		if (tableNames==null) tableNames=buildTableNamesList();
		return tableNames.toArray(new String[0]);
	}

	private String getExactTableName(final String tableName) {
		if (tableNames==null) tableNames=buildTableNamesList();
		for (String s: tableNames) if (s.equalsIgnoreCase(tableName)) return s.replace(";","");
		return null;
	}

	private List<String> buildColumnNamesList(final ResultSet result) throws SQLException {
		final ResultSetMetaData meta=result.getMetaData();
		final int colCount=meta.getColumnCount();
		final List<String> colNames=new ArrayList<>(colCount);
		for (int i=0;i<colCount;i++) colNames.add(meta.getColumnName(i+1));
		return colNames;
	}

	private List<String> buildColumnNamesListFirebird(final ResultSet result) throws SQLException {
		final List<String> columnNames=new ArrayList<>();
		while (result.next()) columnNames.add(result.getString(1).trim());
		return columnNames;
	}

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
		return columnNames.get(exactTableName).toArray(new String[0]);
	}

	/**
	 * Liefert eine Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen
	 * @return	Zuordnung aller Tabellennamen zu allen jeweiligen Spaltennamen
	 */
	public Map<String,List<String>> listAll() {
		for (String tableName: listTables()) listColumns(tableName);
		final Map<String,List<String>> result=new HashMap<>();
		for (Map.Entry<String,List<String>> entry: columnNames.entrySet()) result.put(entry.getKey(),new ArrayList<>(entry.getValue()));
		return result;
	}

	private int getColumnNumber(final String exactTableName, final String columnName) {
		if (columnName==null || columnName.trim().isEmpty()) return 0; /* Wenn keine Spalte angegeben, erste Spalte verwenden */

		final String[] names=listColumns(exactTableName);
		for (int i=0;i<names.length;i++) if (names[i].equalsIgnoreCase(columnName)) return i;

		final Long L=NumberTools.getPositiveLong(columnName); /* Bezeichner als 1-basierende Spaltennummer interpretieren */
		if (L!=null) return L.intValue()-1;

		return -1;
	}

	private String getExactColumnName(final String exactTableName, final String columnName) {
		if (columnName==null || columnName.trim().isEmpty()) return null;

		for (String name: listColumns(exactTableName)) if (name.equalsIgnoreCase(columnName)) return name;
		return null;
	}

	/**
	 * Liefert einen Iterator, der die Zahlenwerte in einer Spalte der angegebenen Tabelle in Reihenfolge der Sortierung einer anderen Spalte liefert.
	 * @param tableName	Name der Tabelle von der die Zahlenwerte einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zahlenwerte geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String �bergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @param sortColumn	Spalte nach der die Ausgabe sortiert werden soll (Wird hier oder f�r den Modus <code>null</code> �bergeben, so erfolgt keine Sortierung. Wird ein ung�ltiger Spaltenname angegeben, so werden keine Daten ausgegeben.)
	 * @param sortMode	Art der Sortierung	(Wird hier oder f�r die Sortierspalte <code>null</code> �bergeben, so erfolgt keine Sortierung.)
	 * @param additionalColumns	Optionale Liste mit weiteren Spaltennamen, deren Inhalte ausgelesen werden sollen. (Es kann sowohl der Parameter <code>null</code> sein als auch einzelne Eintr�ge.)
	 * @return	Iterator �ber die Zahlenwerte in der angegebenen Spalte der angegebenen Tabelle
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

		/* Zus�tzlich auszulesende Spalten */
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
		try {
			final String selectCols=String.join(", ",selectColumns.toArray(new String[0])).replace(";","");
			final String primary=selectColumns.get(0);
			final List<String> secondary=new ArrayList<>(selectColumns); secondary.remove(0);
			return new TableReadDoubleIterator(statement.executeQuery("SELECT "+selectCols+" FROM "+exactTableName+orderQuery+";"),primary,secondary.toArray(new String[0]));
		} catch (SQLException e) {
			return new TableReadDoubleIterator();
		}
	}

	/**
	 * Liefert einen Iterator, der die Zahlenwerte in einer Spalte der angegebenen Tabelle in der Reihenfolge, in der sie in der Tabellen stehen, liefert.
	 * @param tableName	Name der Tabelle von der die Zahlenwerte einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zahlenwerte geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String �bergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @return	Iterator �ber die Zahlenwerte in der angegebenen Spalte der angegebenen Tabelle
	 */
	public Iterator<Double> readTableColumn(final String tableName, final String columnName) {
		return readTableColumn(tableName,columnName,null,null,null);
	}

	/**
	 * Liefert einen Iterator, der die Zeichenketten in einer Spalte der angegebenen Tabelle in Reihenfolge der Sortierung einer anderen Spalte liefert.
	 * @param tableName	Name der Tabelle von der die Zeichenketten einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zeichenketten geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String �bergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @param sortColumn	Spalte nach der die Ausgabe sortiert werden soll (Wird hier oder f�r den Modus <code>null</code> �bergeben, so erfolgt keine Sortierung. Wird ein ung�ltiger Spaltenname angegeben, so werden keine Daten ausgegeben.)
	 * @param sortMode	Art der Sortierung	(Wird hier oder f�r die Sortierspalte <code>null</code> �bergeben, so erfolgt keine Sortierung.)
	 * @param additionalColumns	Optionale Liste mit weiteren Spaltennamen, deren Inhalte ausgelesen werden sollen. (Es kann sowohl der Parameter <code>null</code> sein als auch einzelne Eintr�ge.)
	 * @return	Iterator �ber die Zeichenketten in der angegebenen Spalte der angegebenen Tabelle
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

		/* Zus�tzlich auszulesende Spalten */
		final String[] exactAdditionalColumnName=new String[(additionalColumns==null)?0:additionalColumns.length];
		if (additionalColumns!=null) for (int i=0;i<exactAdditionalColumnName.length;i++) {
			String col="\""+getExactColumnName(exactTableName,additionalColumns[i])+"\"";
			if (!selectColumns.contains(col)) selectColumns.add(col);
			exactAdditionalColumnName[i]=col;
		}

		/* Iterator bauen */
		try {
			final String selectCols=String.join(", ",selectColumns.toArray(new String[0])).replace(";","");
			final String primary=selectColumns.get(0);
			final List<String> secondary=new ArrayList<>(selectColumns); secondary.remove(0);
			return new TableReadStringIterator(statement.executeQuery("SELECT "+selectCols+" FROM "+exactTableName+orderQuery+";"),primary,secondary.toArray(new String[0]));
		} catch (SQLException e) {
			return new TableReadStringIterator();
		}
	}

	/**
	 * Liefert einen Iterator, der die Zeichenketten in einer Spalte der angegebenen Tabelle in der Reihenfolge, in der sie in der Tabellen stehen, liefert.
	 * @param tableName	Name der Tabelle von der die Zeichenketten einer Spalte geliefert werden sollen.
	 * @param columnName	Name der Spalte, von der die Zeichenketten geliefert werden sollen. (Wird hier <code>null</code> oder ein leerer String �bergeben, so wird die erste Spalte verwendet. Statt einem Namen kann auch eine 1-basierende Spaltennummer angegeben werden.)
	 * @return	Iterator �ber die Zeichenketten in der angegebenen Spalte der angegebenen Tabelle
	 */
	public Iterator<String> readStringTableColumn(final String tableName, final String columnName) {
		return readStringTableColumn(tableName,columnName,null,null,null);
	}

	/**
	 * Liefert einen der zus�tzlich angefragten Spalteninhalte zu dem letzten per <code>next()</code> abgefragten Wert.
	 * @param iterator	Iterator, der �ber {@link DBConnect#readTableColumn(String, String, String, SortMode, String[])} angefragt wurde
	 * @param additionalIndex	Index der gew�nschten Spalte (bezieht sich auf den <code>additionalColumns</code>-Parameter in der Anfrage
	 * @return	Wert in der gew�nschten Spalte oder <code>null</code>, wenn es die Spalte nicht gibt oder keine Wert enth�lt.
	 * @see DBConnect#readTableColumn(String, String, String, SortMode, String[])
	 */
	public String readAdditionalColumn(final Iterator<Double> iterator, final int additionalIndex) {
		if (!(iterator instanceof TableReadDoubleIterator)) return null;
		final TableReadDoubleIterator tableIterator=(TableReadDoubleIterator)iterator;

		if (additionalIndex<0 || additionalIndex>=tableIterator.additional.length) return null;
		return tableIterator.additional[additionalIndex];
	}

	/**
	 * Liefert einen der zus�tzlich angefragten Spalteninhalte zu dem letzten per <code>next()</code> abgefragten Wert.
	 * @param iterator	Iterator, der �ber {@link DBConnect#readTableColumn(String, String, String, SortMode, String[])} angefragt wurde
	 * @param additionalIndex	Index der gew�nschten Spalte (bezieht sich auf den <code>additionalColumns</code>-Parameter in der Anfrage
	 * @return	Wert in der gew�nschten Spalte oder <code>null</code>, wenn es die Spalte nicht gibt oder keine Wert enth�lt.
	 * @see DBConnect#readTableColumn(String, String, String, SortMode, String[])
	 */
	public String readAdditionalColumnString(final Iterator<String> iterator, final int additionalIndex) {
		if (!(iterator instanceof TableReadStringIterator)) return null;
		final TableReadStringIterator tableIterator=(TableReadStringIterator)iterator;

		if (additionalIndex<0 || additionalIndex>=tableIterator.additional.length) return null;
		return tableIterator.additional[additionalIndex];
	}

	private class TableReadDoubleIterator implements Iterator<Double> {
		private final ResultSet result;
		private final String[] columnNames;
		private final int numberColumnIndex;
		private final int[] additionalColumnsIndex;
		private Double next;
		private String[] additionalNext;
		private String[] additional;

		private TableReadDoubleIterator() {
			this(null,null,null);
		}

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

		private String[] getColumnNames() {
			final List<String> columnNamesList=new ArrayList<>();

			if (result!=null) try {
				final ResultSetMetaData meta=result.getMetaData();
				for (int i=0;i<meta.getColumnCount();i++) columnNamesList.add(meta.getColumnName(i+1));
			} catch (SQLException e) {}

			return columnNamesList.toArray(new String[0]);
		}

		private int getColumnIndex(String name) {
			if (name==null || name.trim().isEmpty()) return -1;

			if (name.startsWith("\"") && name.endsWith("\"")) name=name.substring(1,name.length()-1);

			for (int i=0;i<columnNames.length;i++) if (columnNames[i].equalsIgnoreCase(name)) return i;



			return -1;
		}

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

	private class TableReadStringIterator implements Iterator<String> {
		private final ResultSet result;
		private final String[] columnNames;
		private final int numberColumnIndex;
		private final int[] additionalColumnsIndex;
		private String next;
		private String[] additionalNext;
		private String[] additional;

		private TableReadStringIterator() {
			this(null,null,null);
		}

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

		private String[] getColumnNames() {
			final List<String> columnNamesList=new ArrayList<>();

			if (result!=null) try {
				final ResultSetMetaData meta=result.getMetaData();
				for (int i=0;i<meta.getColumnCount();i++) columnNamesList.add(meta.getColumnName(i+1));
			} catch (SQLException e) {}

			return columnNamesList.toArray(new String[0]);
		}

		private int getColumnIndex(String name) {
			if (name==null || name.trim().isEmpty()) return -1;

			if (name.startsWith("\"") && name.endsWith("\"")) name=name.substring(1,name.length()-1);

			for (int i=0;i<columnNames.length;i++) if (columnNames[i].equalsIgnoreCase(name)) return i;



			return -1;
		}

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
	 * F�gt eine Zeile an eine Tabelle an
	 * @param table	Tabelle an die die Zeile angef�gt werden soll
	 * @param columns	Namen der Spalten in die die Werte geschrieben werden sollen
	 * @param values	Werte die geschrieben werden sollen
	 * @return	Liefert im Erfolgsfall <code>true</code> zur�ck
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

		return names.toArray(new String[0]);
	}

	/**
	 * Erstellt eine Dummy-Tabelle in einer Datenbank
	 * @param output	Ausgaben werden �ber dieses Callback ausgegeben. Es darf nicht <code>null</code> sein.
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
	 * Liest Informationen �ber die in einer Datenbank enthaltenen Tabellen und dann die Daten aus einer Tabelle aus.
	 * @param output	Ausgaben werden �ber dieses Callback ausgegeben. Es darf nicht <code>null</code> sein.
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