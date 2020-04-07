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

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.db.DBConnect.DBType;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Dieses Objekt speichert die Einstellungen für eine Datenbankverbindung
 * @author Alexander Herzog
 * @see DBConnect
 */
public final class DBSettings implements Cloneable {
	/**
	 * Name des XML-Elements, das die Datenbankverbindungseinstellungen speichern soll
	 */
	public static String[] XML_NODE_NAME=new String[]{"DatenbankVerbindung"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	private DBType type;
	private String config;
	private String user;
	private String password;

	/**
	 * Konstruktor der Klasse
	 */
	public DBSettings() {
		type=DBConnect.DBType.SQLITE_FILE;
		config="";
		user="";
		password="";
	}

	/**
	 * Konstruktor der Klasse
	 * @param type	Datenbanktyp (siehe {@link DBConnect.DBType})
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 * @param user	Optional notwendiger Nutzername für den Zugriff auf die Datenbank
	 * @param password	Optional notwendiges Passwort für den Zugriff auf die Datenbank
	 */
	public DBSettings(final DBType type, final String config, final String user, final String password) {
		this();
		setType(type);
		setConfig(config);
		setUser(user);
		setPassword(password);
	}

	/**
	 * Konstruktor der Klasse
	 * @param type	Datenbanktyp (siehe {@link DBConnect.DBType})
	 * @param config	Datenbank-Zugriffs-Konfiguration (Serveradresse, Dateiname usw.)
	 */
	public DBSettings(final DBType type, final String config) {
		this(type,config,null,null);
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param copySource	Ausgangsobjekt dessen Einstellungen kopiert werden sollen
	 */
	public DBSettings(final DBSettings copySource) {
		this();
		copyFrom(copySource);
	}

	/**
	 * Liefert den Datenbanktyp.
	 * @return	Datenbanktyp
	 * @see DBSettings#setType(DBType)
	 * @see DBConnect.DBType
	 */
	public DBType getType() {
		return type;
	}

	/**
	 * Stellt den Datenbanktyp ein.
	 * @param type	Datenbanktyp
	 * @see DBSettings#getType()
	 * @see DBConnect.DBType
	 */
	public void setType(final DBType type) {
		if (type!=null) this.type=type;
	}

	/**
	 * Liefert die Einstellungen (Servername, Dateiname usw.) für die Datenbankverbindung.
	 * @return	Einstellungen für die Datenbankverbindung
	 * @see DBSettings#setConfig(String)
	 */
	public String getConfig() {
		return config;
	}

	/**
	 * Liefert die Einstellungen (Servername, Dateiname usw.) für die Datenbankverbindung
	 * führt dabei optionale in {@link DBType} hinterlegte Verarbeitungen durch.
	 * @return	Einstellungen für die Datenbankverbindung
	 * @see DBSettings#setConfig(String)
	 */
	public String getProcessedConfig() {
		if (type==null) return config;
		return type.processSettings(config);
	}

	/**
	 * Legt die Einstellungen (Servername, Dateiname usw.) für die Datenbankverbindung fest.
	 * @param config	Einstellungen für die Datenbankverbindung
	 * @see DBSettings#getConfig()
	 * @see DBSettings#getProcessedConfig()
	 */
	public void setConfig(final String config) {
		if (config!=null) this.config=config; else this.config="";
	}

	/**
	 * Liefert den Nutzernamen für den Datenbankzugriff.
	 * @return	Nutzernamen für den Datenbankzugriff
	 * @see DBSettings#setUser(String)
	 * @see DBSettings#getPassword()
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Stellt den Nutzernamen für den Datenbankzugriff ein.
	 * @param user	Nutzernamen für den Datenbankzugriff
	 * @see DBSettings#getUser()
	 * @see DBSettings#setPassword(String)
	 */
	public void setUser(final String user) {
		if (user!=null) this.user=user; else this.user="";
	}

	/**
	 * Liefert das Passwort für den Datenbankzugriff.
	 * @return	Passwort für den Datenbankzugriff
	 * @see DBSettings#setPassword(String)
	 * @see DBSettings#getUser()
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Stellt das Passwort für den Datenbankzugriff ein.
	 * @param password	Passwort für den Datenbankzugriff
	 * @see DBSettings#getPassword()
	 * @see DBSettings#setUser(String)
	 */
	public void setPassword(final String password) {
		if (password!=null) this.password=password; else this.password="";
	}

	/**
	 * Vergleicht zwei Datenbankeinstellungsobjekte
	 * @param otherSettings	Zweites Objekt das mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsDBSettings(final DBSettings otherSettings) {
		if (otherSettings==null) return false;
		if (type!=otherSettings.type) return false;
		if (!Objects.equals(config,otherSettings.config)) return false;
		if (!Objects.equals(user,otherSettings.user)) return false;
		if (!Objects.equals(password,otherSettings.password)) return false;
		return true;
	}

	private void copyFrom(final DBSettings copySource) {
		if (copySource==null) return;
		setType(copySource.type);
		setConfig(copySource.config);
		setUser(copySource.user);
		setPassword(copySource.password);
	}

	/**
	 * Erstellt eine Kopie des Datenbankeinstellungsobjekts
	 */
	@Override
	public DBSettings clone() {
		return new DBSettings(this);
	}

	/**
	 * Speichert die Datenbankeinstellungen in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter Knoten des Knotens, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		if (type!=null) node.setAttribute(Language.trPrimary("Surface.XML.Database.Mode"),type.name);
		if (config!=null && !config.isEmpty()) node.setAttribute(Language.trPrimary("Surface.XML.Database.Config"),config);
		if (user!=null && !user.isEmpty()) node.setAttribute(Language.trPrimary("Surface.XML.Database.User"),user);
		if (password!=null && !password.isEmpty()) node.setAttribute(Language.trPrimary("Surface.XML.Database.Password"),password);
	}

	/**
	 * Versucht die Datenbankeinstellungen aus einem gegebenen xml-Element zu laden
	 * @param node	XML-Element, aus dem die Datenbankeinstellungen geladen werden soll
	 * @return Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final String modeString=Language.trAllAttribute("Surface.XML.Database.Mode",node);
		DBType t=null;
		for (DBType test: DBConnect.DBType.values()) if (test.name.equalsIgnoreCase(modeString)) {t=test; break;}
		if (t==null) return String.format(Language.tr("Surface.XML.Database.Mode.ErrorUnknown"),modeString);
		type=t;

		config=Language.trAllAttribute("Surface.XML.Database.Config",node);
		user=Language.trAllAttribute("Surface.XML.Database.User",node);
		password=Language.trAllAttribute("Surface.XML.Database.Password",node);

		return null;
	}

	/**
	 * Prüft, ob es sich bei einem XML-Element um ein Datenbankeinstellungen-Element handelt.
	 * @param node	Zu prüfendes Element
	 * @return	Gibt <code>true</code> zurück, wenn es sich um ein Datenbankeinstellungen-Element handelt, aus dem Daten geladen werden können.
	 */
	public static boolean isDBSettingsNode(final Element node) {
		for (String test: XML_NODE_NAME) if (node.getNodeName().equals(test)) return true;
		return false;
	}

	/**
	 * Erstellt eine Beschreibung für die Datenbankeinstellung
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param position	Position ab der die Zeilen eingefügt werden sollen
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder, final int position) {
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Database.Type"),type.name,position);
		if (config!=null && !config.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Database.Config"),config,position+1);
		if (user!=null && !user.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Database.User"),user,position+2);
		if (password!=null && !password.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Database.Password"),password,position+3);
	}

	@Override
	public String toString() {
		final StringBuilder sb=new StringBuilder();
		sb.append(type.name);
		sb.append(" (");
		boolean first=true;
		if (config!=null && !config.trim().isEmpty()) {
			if (first) first=false; else sb.append(", ");
			sb.append(Language.tr("ModelDescription.Database.Config")+": "+config);
		}
		if (user!=null && !user.trim().isEmpty()) {
			if (first) first=false; else sb.append(", ");
			sb.append(Language.tr("ModelDescription.Database.User")+": "+user);
		}
		if (password!=null && !password.trim().isEmpty()) {
			if (first) first=false; else sb.append(", ");
			sb.append(Language.tr("ModelDescription.Database.Password")+": "+password);
		}
		sb.append(")");
		return sb.toString();
	}
}