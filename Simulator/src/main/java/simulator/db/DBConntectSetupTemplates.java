package simulator.db;

import java.util.Arrays;
import java.util.List;

import simulator.db.DBConnectSetup.ProcessSettings;
import simulator.db.DBConnectSetup.SelectSource;

/**
 * Vorlagen für {@link DBConnectSetup}-Datensätze, wenn
 * diese nicht aus der Konfigurationsdatei geladen wurden.
 * @author Alexander Herzog
 * @see DBConnectSetup
 * @see DBConnectSetups
 */
public class DBConntectSetupTemplates {
	/**
	 * Liste der unterstützten Datenbankformate
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
				true,
				DBConnectSetup.ProcessSettings.NONE,
				DBConnectSetup.SelectSource.FILE_SQLITE),

		/**
		 * HSQLDB (Hyper Structured Query Language Database) in Verzeichnis<br>
		 * Hinweis: Config ist Pfad und muss abschließenden Trenner beinhalten, Nutzername ist "SA" und Passwort "".
		 */
		HSQLDB_LOCAL(
				"HSQLDB Local",
				"org.hsqldb.jdbc.JDBCDriver",
				"hsqldb:file",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE='TABLE';",
				null,
				true,
				DBConnectSetup.ProcessSettings.NONE,
				DBConnectSetup.SelectSource.FOLDER),

		/**
		 * HSQLDB (Hyper Structured Query Language Database) über Server<br>
		 * Hinweis: Config ist Serveradresse und muss mit "//" beginnen und mit "/" enden. Im Standardfall sind Nutzername "SA" und Passwort "".
		 */
		HSQLDB_SERVER(
				"HSQLDB Server",
				"org.hsqldb.jdbc.JDBCDriver",
				"hsqldb:hsql",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE='TABLE';",
				null,
				true,
				DBConnectSetup.ProcessSettings.NONE,
				DBConnectSetup.SelectSource.NONE),

		/**
		 * Postgre SQL über Server<br>
		 * Hinweis: Config ist Serveradresse und Datenbankname im Format "//host/db". Nutzername und Passwort müssen gesetzt sein.
		 */
		POSTGRESQL_SERVER(
				"PostgreSQL Server",
				"org.postgresql.Driver",
				"postgresql",
				"SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';",
				null,
				true,
				DBConnectSetup.ProcessSettings.NONE,
				DBConnectSetup.SelectSource.NONE),

		/**
		 * MariaDB über Server<br>
		 * Hinweis: Config ist Serveradresse und Datenbankname im Format "//host:3306/db". Nutzername und Passwort müssen gesetzt sein.
		 */
		MARIADB_SERVER(
				"MariaDB Server",
				"org.mariadb.jdbc.Driver",
				"mariadb",
				"SHOW TABLES;",
				null,
				true,
				DBConnectSetup.ProcessSettings.NONE,
				DBConnectSetup.SelectSource.NONE),

		/**
		 * Firebird-Server
		 */
		FIREBIRD_SERVER(
				"Firebird Server",
				"org.firebirdsql.jdbc.FBDriver",
				"firebirdsql",
				"select rdb$relation_name from rdb$relations where rdb$view_blr is null and (rdb$system_flag is null or rdb$system_flag = 0);",
				"encoding=UTF8",
				true,
				DBConnectSetup.ProcessSettings.NONE,
				DBConnectSetup.SelectSource.NONE),

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
				DBConnectSetup.ProcessSettings.ACCESS,
				DBConnectSetup.SelectSource.FILE_ACCESS),

		/**
		 * Apache Derby
		 */
		/*
		DERBY(
				"Derby",
				"org.apache.derby.jdbc.AutoloadedDriver",
				"derby",
				"select st.tablename from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'",
				null,
				false,
				DBConnectSetup.ProcessSettings.DERBY,
				DBConnectSetup.SelectSource.FOLDER
				),
		 */

		/**
		 * H2 Database in Datei
		 */
		H2_DATABASE_LOCAL(
				"H2 Database Local",
				"org.h2.Driver",
				"h2",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES AS T WHERE T.TABLE_SCHEMA <> 'INFORMATION_SCHEMA'",
				null,
				true,
				DBConnectSetup.ProcessSettings.H2_LOCAL,
				DBConnectSetup.SelectSource.FILE_GENERAL
				),

		/**
		 * H2 Database
		 */
		H2_DATABASE_SERVER(
				"H2 Database Server",
				"org.h2.Driver",
				"h2",
				"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES AS T WHERE T.TABLE_SCHEMA <> 'INFORMATION_SCHEMA'",
				null,
				true,
				DBConnectSetup.ProcessSettings.H2_SERVER,
				DBConnectSetup.SelectSource.NONE
				);

		/** Name des Datenbank-Servers */
		public final String name;

		/** Treiber in der Java-Klassen-Notation */
		public final String driver;

		/** Bezeichnung für den Konnektor */
		public final String connector;

		/** SQL-Befehl in der entsprechenden Datenbanknotation um die Liste aller Tabellen abzurufen */
		public final String listAllTablesCommand;

		/** Dürfen Spaltenbezeichner in Anführungszeichen gesetzt werden? */
		public final boolean useQuotes;

		/** Optional weitere Einstellungen, die beim Aufbau der Verbindung benötigt werden. Kann <code>null</code> sein. */
		public final String properties;

		/** Optionale Verarbeitungen der Einstellungen vor dem Übermitteln an die Datenbank */
		public final ProcessSettings processSettings;

		/** Optional Auswahlunterstützung für die Datenbank-Datei oder das -Verzeichnis */
		public final SelectSource selectSource;

		/**
		 * Konstruktor des Enum
		 * @param name	Name des Datenbank-Servers
		 * @param driver	Treiber in der Java-Klassen-Notation
		 * @param connector	Bezeichnung für den Konnektor
		 * @param listAllTablesCommand	SQL-Befehl in der entsprechenden Datenbanknotation um die Liste aller Tabellen abzurufen
		 * @param properties	Optional weitere Einstellungen, die beim Aufbau der Verbindung benötigt werden. Kann <code>null</code> sein.
		 * @param useQuotes	Dürfen Spaltenbezeichner in Anführungszeichen gesetzt werden?
		 * @param processSettings	Optionale Verarbeitungen der Einstellungen vor dem Übermitteln an die Datenbank
		 * @param selectSource	Optional Auswahlunterstützung für die Datenbank-Datei oder das -Verzeichnis
		 */
		DBType(final String name, final String driver, final String connector, final String listAllTablesCommand, final String properties, final boolean useQuotes, final ProcessSettings processSettings, final SelectSource selectSource) {
			this.name=name;
			this.driver=driver;
			this.connector=connector;
			this.listAllTablesCommand=listAllTablesCommand;
			this.properties=properties;
			this.useQuotes=useQuotes;
			this.processSettings=processSettings;
			this.selectSource=selectSource;
		}
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse enthält nur statische Hilfsroutinen und kann nicht instanziert werden.
	 */
	private DBConntectSetupTemplates() {
	}

	/**
	 * Ergänzt eine Liste mit {@link DBConnectSetup}-Einträge nach
	 * Bedarf mit Vorgabedaten.
	 * @param list	Zu ergänzende Liste
	 */
	public static void addToList(final List<DBConnectSetup> list) {
		for (DBType type: DBType.values()) {

			boolean isInList=false;
			for (DBConnectSetup setup: list) if (setup.name.equalsIgnoreCase(type.name)) {
				isInList=true;
				break;
			}
			if (isInList) continue;

			final DBConnectSetup setup=new DBConnectSetup();
			setup.name=type.name;
			setup.driver=type.driver;
			setup.connector=type.connector;
			setup.listAllTablesCommand=type.listAllTablesCommand;
			setup.useQuotes=type.useQuotes;
			setup.properties=type.properties;
			setup.processSettings=type.processSettings;
			setup.selectSource=type.selectSource;
			list.add(setup);
		}
	}

	/**
	 * Findet einen Vorlagendatensatz über seinen Namen.
	 * @param name	Name des Vorlagendatensatzes
	 * @return	Vorlagendatensatz oder <code>null</code>, wenn kein Vorlagendatensatz mit dem angegebenen Namen existert
	 */
	public static DBType getByName(final String name) {
		return Arrays.asList(DBType.values()).stream().filter(type->type.name.equalsIgnoreCase(name)).findFirst().orElse(null);
	}
}
