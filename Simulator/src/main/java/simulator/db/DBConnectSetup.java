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

import java.util.Arrays;
import java.util.function.Function;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Datensatz für eine Datenbankverbindung
 * @author Alexander Herzog
 * @see DBConnect
 * @see DBConnectSetups
 */
public class DBConnectSetup {
	/**
	 * Verarbeitungen der Einstellungen vor dem Übermitteln an die Datenbank
	 * @see DBConnectSetup#processSettings
	 */
	public enum ProcessSettings {
		/** Keine zusätzliche Verarbeitung */
		NONE(null,s->s,s->s,null),
		/** Access-Anpassungen vornehmen */
		ACCESS("Access",s->"//"+s.replace("\\","/"),s->s,null),
		/** Apache Derby-Anpassungen vornehmen */
		/* DERBY("Derby",s->s,s->s,";create=true"), */

		/*
		 * Removed from JDBC.cfg:
		 * <Database>
    <Name>Derby</Name>
	<Driver>org.apache.derby.jdbc.AutoloadedDriver</Driver>
	<Connector>derby</Connector>
	<ListAllTablesCommand>select st.tablename from sys.systables st LEFT OUTER join sys.sysschemas ss on (st.schemaid = ss.schemaid) where ss.schemaname ='APP'</ListAllTablesCommand>
	<UseQuotes>0</UseQuotes>
	<Properties></Properties>
	<ProcessSettings>Derby</ProcessSettings>
	<SelectSource>Folder</SelectSource>
  </Database>
		 */

		/** H2 Database (lokal)-Anpassungen vornehmen */
		H2_LOCAL("H2Local",s->s,s->s.replace(".mv.db","").replace(".trace.db",""),null),
		/** H2 Database (Server-Verbindung)-Anpassungen vornehmen */
		H2_SERVER("H2Server",s->"tcp://"+s,s->s,null);

		/** Name für den Enum-Eintrag */
		public final String name;

		/** Vorverarbeitungsmethode */
		public final Function<String,String> processcor1;

		/** Vorverarbeitungsmethode (nach der Prüfung der Datenbankdatei auf Existenz) */
		public final Function<String,String> processcor2;

		/** Zusätzliche Konfiguration zum Erstellen einer Datenbankdatei */
		public final String createFileConfig;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name für den Enum-Eintrag
		 * @param processcor1	Vorverarbeitungsmethode
		 * @param processcor2	Vorverarbeitungsmethode (nach der Prüfung der Datenbankdatei auf Existenz)
		 * @param createFileConfig	Zusätzliche Konfiguration zum Erstellen einer Datenbankdatei
		 */
		ProcessSettings(final String name, final Function<String,String> processcor1, final Function<String,String> processcor2, final String createFileConfig) {
			this.name=name;
			this.processcor1=processcor1;
			this.processcor2=processcor2;
			this.createFileConfig=createFileConfig;
		}
	}

	/**
	 * Auswahlunterstützung für die Datenbank-Datei oder das -Verzeichnis
	 * @see DBConnectSetup#selectSource
	 */
	public enum SelectSource {
		/** Keine Auswahlunterstützung */
		NONE(null,false,false),
		/** Verzeichnisauswahl anbieten */
		FOLDER("Folder",false,true),
		/** Allgemeine Dateiauswahl anbieten */
		FILE_GENERAL("File",true,false),
		/** SQLite-Dateiauswahl anbieten */
		FILE_SQLITE("SQLite",true,false),
		/** Access-Dateiauswahl anbieten */
		FILE_ACCESS("Access",true,false);

		/** Name für den Enum-Eintrag */
		public final String name;

		/** Handelt es sich bei der Quelle um eine Datei? */
		public final boolean isFile;

		/** Handelt es sich bei der Quelle um ein Verzeichnis? */
		public final boolean isFolder;

		/**
		 * Konstruktor der Enum
		 * @param name	Name für den Enum-Eintrag
		 * @param isFile	Handelt es sich bei der Quelle um eine Datei?
		 * @param isFolder	Handelt es sich bei der Quelle um ein Verzeichnis?
		 */
		SelectSource(final String name, final boolean isFile, final boolean isFolder) {
			this.name=name;
			this.isFile=isFile;
			this.isFolder=isFolder;
		}
	}

	/** Name des Datenbank-Servers */
	public String name;

	/** Treiber in der Java-Klassen-Notation */
	public String driver;

	/** Bezeichnung für den Konnektor */
	public String connector;

	/** SQL-Befehl in der entsprechenden Datenbanknotation um die Liste aller Tabellen abzurufen */
	public String listAllTablesCommand;

	/** Dürfen Spaltenbezeichner in Anführungszeichen gesetzt werden? */
	public boolean useQuotes;

	/** Optional weitere Einstellungen, die beim Aufbau der Verbindung benötigt werden. Kann <code>null</code> sein. */
	public String properties;

	/** Optionale Verarbeitungen der Einstellungen vor dem Übermitteln an die Datenbank */
	public ProcessSettings processSettings;

	/** Optional Auswahlunterstützung für die Datenbank-Datei oder das -Verzeichnis */
	public SelectSource selectSource;

	/**
	 * Konstruktor der Klasse
	 */
	public DBConnectSetup() {
		processSettings=ProcessSettings.NONE;
		selectSource=SelectSource.NONE;
	}

	/**
	 * Lädt eine einzelne Eigenschaft des Datensatzes.
	 * @param name	Name der Eigenschaft
	 * @param content	Wert der Eigenschaft
	 */
	private void loadProperty(final String name, final String content) {
		if (name.equalsIgnoreCase("Name")) {this.name=content; return;}
		if (name.equalsIgnoreCase("Driver")) {driver=content; return;	}
		if (name.equalsIgnoreCase("Connector")) {connector=content; return;}
		if (name.equalsIgnoreCase("ListAllTablesCommand")) {listAllTablesCommand=content; return;}
		if (name.equalsIgnoreCase("UseQuotes")) {useQuotes=!content.equals("0"); return;}
		if (name.equalsIgnoreCase("Properties")) {properties=content; return;}

		if (name.equalsIgnoreCase("ProcessSettings")) {
			processSettings=Arrays.asList(ProcessSettings.values()).stream().filter(rec->(rec.name!=null && rec.name.equalsIgnoreCase(content))).findFirst().orElse(ProcessSettings.NONE);
			return;
		}

		if (name.equalsIgnoreCase("SelectSource")) {
			selectSource=Arrays.asList(SelectSource.values()).stream().filter(rec->(rec.name!=null && rec.name.equalsIgnoreCase(content))).findFirst().orElse(SelectSource.NONE);
			return;
		}
	}

	/**
	 * Lädt die Daten zu einem Datenbank-Eintrag aus einem XML-Element und seinen Unterlementen
	 * @param node	XML-Element
	 * @return	Liefert <code>true</code>, wenn der Datensatz korrekt geladen werden konnte
	 */
	public boolean loadFromXML(final Element node) {
		final NodeList l=node.getChildNodes();
		final int length=l.getLength();
		for (int i=0; i<length;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			final Element e=(Element)sub;
			final String content=e.getTextContent().trim();
			if (!content.isEmpty()) loadProperty(e.getNodeName(),content);
		}

		if (name==null || driver==null || connector==null || listAllTablesCommand==null) return false;

		return true;
	}
}
