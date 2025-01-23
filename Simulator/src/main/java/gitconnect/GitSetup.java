/**
 * Copyright 2021 Alexander Herzog
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
package gitconnect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.Table;

/**
 * Diese Klasse hält einen Git-Konfigurationsdaten vor.
 * @author Alexander Herzog
 */
public class GitSetup {
	/**
	 * Name des xml-Elements das die einzelnen Git-Konfigurationsdatensätze enthält
	 * @see #load(Element)
	 * @see #save(Element)
	 */
	public static final String XML_PARENT_NAME="GitSetups";

	/**
	 * Name des xml-Element für einen einzelnen Git-Konfigurationsdatensatz
	 * @see #load(Element)
	 * @see #save(Element)
	 */
	public static final String XML_NAME="GitSetup";

	/**
	 * Welche Daten sollen beim Speichern ins Git übertragen werden?
	 * @see GitSetup#saveData
	 */
	public enum GitSaveMode {
		/** Modelldateien */
		MODELS("Models"),
		/** Parameterreihen */
		PARAMETER_SERIES("ParameterSeries"),
		/** Statistikdateien */
		STATISTICS("Statistics"),
		/** Optimierer-Einstellungen */
		OPTIMIZATION_SETUPS("Optimizations");

		/**
		 * Beim Speichern der Konfiguration zu verwendender zugehöriger XML-Bezeichner
		 */
		public final String xmlName;

		/**
		 * Konstruktor der Klasse
		 * @param xmlName	Beim Speichern der Konfiguration zu verwendender zugehöriger XML-Bezeichner
		 */
		GitSaveMode(final String xmlName) {
			this.xmlName=xmlName;
		}

		/**
		 * Liefert das zu dem XML-Bezeichner passende Enum-Objekt
		 * @param xmlName	XML-Bezeichner
		 * @return	Enum-Objekt oder <code>null</code>, wenn kein Objekt zu dem Bezeichner passt
		 */
		public static GitSaveMode getFromXMLName(final String xmlName) {
			return Stream.of(values()).filter(mode->mode.xmlName.equalsIgnoreCase(xmlName)).findFirst().orElse(null);
		}
	}

	/**
	 * Authentifizierungsmethode gegenüber dem Git-Server
	 * @see GitSetup#serverAuth
	 */
	public enum ServerAuthMode {
		/** Keine Authentifizierung */
		NONE("None"),
		/** Authentifizierung über Nutzername und Passwort */
		PASSWORD("Password"),
		/** Authentifizierung über Key-Datei */
		KEY("Key");

		/**
		 * Beim Speichern der Konfiguration zu verwendender zugehöriger XML-Bezeichner
		 */
		public final String xmlName;

		/**
		 * Konstruktor der Klasse
		 * @param xmlName	Beim Speichern der Konfiguration zu verwendender zugehöriger XML-Bezeichner
		 */
		ServerAuthMode(final String xmlName) {
			this.xmlName=xmlName;
		}

		/**
		 * Liefert das zu dem XML-Bezeichner passende Enum-Objekt
		 * @param xmlName	XML-Bezeichner
		 * @return	Enum-Objekt oder <code>null</code>, wenn kein Objekt zu dem Bezeichner passt
		 */
		public static ServerAuthMode getFromXMLName(final String xmlName) {
			return Stream.of(values()).filter(mode->mode.xmlName.equalsIgnoreCase(xmlName)).findFirst().orElse(null);
		}
	}

	/**
	 * Rückgabewert der Funktion {@link GitSetup#getPrivateKeyStatus()};
	 * gibt an, ob der in {@link GitSetup#authKey} angegebene Schlüssel
	 * korrekt verarbeitet werden kann
	 * @see GitSetup#getPrivateKeyStatus()
	 * @see GitSetup#authKey
	 */
	public enum PrivateKeyStatus {
		/** Es wurde keine Schlüsseldatei angegeben */
		NO_KEY,
		/** Die angegebene Schlüsseldatei existiert nicht */
		NO_FILE,
		/** Das Format der Schlüsseldatei ist ungültig */
		WRONG_FORMAT,
		/** Die Schlüsseldatei kann korrekt verarbeitet werden */
		OK
	}

	/**
	 * Lokaler Ordner, der als Git-Repository-Basis verwendet werden soll<br>
	 * (Darf leer, aber nicht <code>null</code> sein.)
	 */
	public String localFolder;

	/**
	 * Welche Daten sollen beim Speichern ins Git übertragen werden?
	 */
	public final Set<GitSaveMode> saveData;

	/**
	 * Sollen Daten von einem Git-Server geladen bzw. dort gespeichert werden?
	 */
	public boolean useServer;

	/**
	 * URL des Git-Servers<br>
	 * (Darf leer, aber nicht <code>null</code> sein.)
	 */
	public String serverURL;

	/**
	 * Authentifizierungsmethode gegenüber dem Git-Server<br>
	 * (Für "keine Authentifizierung" den Wert {@link ServerAuthMode#NONE} verwenden, nicht auf <code>null</code> setzen.)
	 */
	public ServerAuthMode serverAuth;

	/**
	 * Nutzername für Authentifizierung über Nutzername/Passwort<br>
	 * (Darf leer, aber nicht <code>null</code> sein.)
	 * @see #serverAuth
	 */
	public String authName;

	/**
	 * Passwort für Authentifizierung über Nutzername/Passwort<br>
	 * (Darf leer, aber nicht <code>null</code> sein.)
	 * @see #serverAuth
	 */
	public String authPassword;

	/**
	 * Dateiname der Schlüsseldatei für Authentifizierung über Schlüssel<br>
	 * (Darf leer, aber nicht <code>null</code> sein.)
	 * @see #serverAuth
	 */
	public String authKey;

	/**
	 * Passwort zum Öffnen der Schlüsseldatei {@link #authKey}<br>
	 * (Darf leer, aber nicht <code>null</code> sein.)
	 * @see #authKey
	 * @see #serverAuth
	 */
	public String authKeyPassphrase;

	/**
	 * Soll bei einem Server-Push die Datenübertragung
	 * erzwungen werden, auch wenn lokaler Branch und
	 * Branch auf dem Server keine gemeinsame Wurzel
	 * haben (Upstream)?
	 */
	public boolean forcePush;

	/**
	 * Soll beim Starten des Programms ein Server-Pull für dieses Repository durchgeführt werden?
	 */
	public boolean pullOnStart;

	/**
	 * Konstruktor der Klasse
	 */
	public GitSetup() {
		localFolder="";
		saveData=new HashSet<>();
		useServer=false;
		serverURL="";
		serverAuth=ServerAuthMode.NONE;
		authName="";
		authPassword="";
		authKey="";
		authKeyPassphrase="";
		forcePush=false;
		pullOnStart=false;
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param gitSetup	Zu kopierendes Quell-Objekt
	 */
	public GitSetup(final GitSetup gitSetup) {
		this();
		copyFrom(gitSetup);
	}

	/**
	 * Kopiert die Daten aus einem anderen Git-Setup-Objekt in dieses.
	 * @param gitSetup	Zu kopierendes Quell-Objekt
	 */
	public void copyFrom(final GitSetup gitSetup) {
		localFolder=gitSetup.localFolder;
		saveData.clear();
		saveData.addAll(gitSetup.saveData);
		useServer=gitSetup.useServer;
		serverURL=gitSetup.serverURL;
		serverAuth=gitSetup.serverAuth;
		authName=gitSetup.authName;
		authPassword=gitSetup.authPassword;
		authKey=gitSetup.authKey;
		authKeyPassphrase=gitSetup.authKeyPassphrase;
		forcePush=gitSetup.forcePush;
		pullOnStart=gitSetup.pullOnStart;
	}

	/**
	 * Vergleicht zwei Git-Setup-Objekte.
	 * @param gitSetup	Zweites Git-Setup-Objekt welches mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn beide Objekte inhaltlich identisch sind.
	 */
	public boolean equalsGitSetup(final GitSetup gitSetup) {
		if (gitSetup==null) return false;

		if (!localFolder.equals(gitSetup.localFolder)) return false;
		if (saveData.equals(gitSetup.saveData)) return false;
		if (useServer!=gitSetup.useServer) return false;
		if (!serverURL.equals(gitSetup.serverURL)) return false;
		if (serverAuth!=gitSetup.serverAuth) return false;
		if (!authName.equals(gitSetup.authName)) return false;
		if (!authPassword.equals(gitSetup.authPassword)) return false;
		if (!authKey.equals(gitSetup.authKey)) return false;
		if (!authKeyPassphrase.equals(gitSetup.authKeyPassphrase)) return false;
		if (pullOnStart!=gitSetup.pullOnStart) return false;
		if (forcePush!=gitSetup.forcePush) return false;

		return true;
	}

	/**
	 * Speichert die Einstellungen zu diesem Datensatz in einem
	 * Untereintrag eines xml-Elements
	 * @param parent	Übergeordnetes xml-Element
	 */
	public void save(final Element parent) {
		Element node, sub;
		final Document doc=parent.getOwnerDocument();
		parent.appendChild(node=doc.createElement(XML_NAME));

		node.appendChild(sub=doc.createElement("LocalFolder"));
		sub.setTextContent(localFolder);

		for (GitSaveMode saveMode: saveData) {
			node.appendChild(sub=doc.createElement("SaveData"));
			sub.setTextContent(saveMode.xmlName);
		}

		if (useServer || !serverURL.isEmpty()) {
			node.appendChild(sub=doc.createElement("Server"));
			sub.setTextContent(serverURL);
			if (useServer) sub.setAttribute("Active","1");
		}

		if (serverAuth!=ServerAuthMode.NONE) {
			node.appendChild(sub=doc.createElement("AuthentificationMethod"));
			sub.setTextContent(serverAuth.xmlName);
		}

		if (!authName.isEmpty()) {
			node.appendChild(sub=doc.createElement("AuthentificationName"));
			sub.setTextContent(authName);
		}

		if (!authPassword.isEmpty()) {
			node.appendChild(sub=doc.createElement("AuthentificationPassword"));
			sub.setTextContent(authPassword);
		}

		if (!authKey.isEmpty() || !authKeyPassphrase.isEmpty()) {
			node.appendChild(sub=doc.createElement("AuthentificationKey"));
			sub.setTextContent(authKey);
			if (!authKeyPassphrase.isEmpty()) sub.setAttribute("Passphrase",authKeyPassphrase);
		}

		if (forcePush) {
			node.appendChild(sub=doc.createElement("ForcePush"));
			sub.setTextContent("1");
		}

		if (pullOnStart) {
			node.appendChild(sub=doc.createElement("PullOnStart"));
			sub.setTextContent("1");
		}
	}

	/**
	 * Lädt die Einstellungen zu diesem Datensatz aus einem xml-Element
	 * @param node	xml-Element aus dem die Daten geladen werden sollen
	 */
	public void load(final Element node) {
		final NodeList nodes=node.getChildNodes();
		final int count=nodes.getLength();
		for (int i=0;i<count;i++) {
			if (!(nodes.item(i) instanceof Element)) continue;
			final Element element=(Element)nodes.item(i);
			final String name=element.getNodeName();

			if (name.equalsIgnoreCase("LocalFolder")) {
				localFolder=element.getTextContent();
				continue;
			}

			if (name.equalsIgnoreCase("SaveData")) {
				final GitSaveMode mode=GitSaveMode.getFromXMLName(element.getTextContent());
				if (mode!=null) saveData.add(mode);
				continue;
			}

			if (name.equalsIgnoreCase("Server")) {
				serverURL=element.getTextContent();
				useServer=element.getAttribute("Active").equals("1");
				continue;
			}

			if (name.equalsIgnoreCase("AuthentificationMethod")) {
				final ServerAuthMode authMode=ServerAuthMode.getFromXMLName(element.getTextContent());
				if (authMode!=null) serverAuth=authMode;
				continue;
			}

			if (name.equalsIgnoreCase("AuthentificationName")) {
				authName=element.getTextContent();
				continue;
			}

			if (name.equalsIgnoreCase("AuthentificationPassword")) {
				authPassword=element.getTextContent();
				continue;
			}

			if (name.equalsIgnoreCase("AuthentificationKey")) {
				authKey=element.getTextContent();
				authKeyPassphrase=element.getAttribute("Passphrase");
				continue;
			}

			if (name.equalsIgnoreCase("PullOnStart")) {
				pullOnStart=!element.getTextContent().equals("0");
				continue;
			}

			if (name.equalsIgnoreCase("ForcePush")) {
				forcePush=!element.getTextContent().equals("0");
				continue;
			}
		}
	}

	/**
	 * Gibt an, ob das angegebene lokale Verzeichnis existiert.
	 * @return	Liefert <code>true</code>, wenn das angegebene lokale Verzeichnis existiert
	 */
	public boolean isLocalFolderExists() {
		if (localFolder==null || localFolder.isEmpty()) return false;
		final File folder=new File(localFolder);
		return folder.isDirectory();
	}

	/**
	 * Gibt an, ob das angegebene lokale Verzeichnis einen ".git"-Unterordner enthält.
	 * @return	Liefert <code>true</code>, wenn das angegebene lokale Verzeichnis ein Git-Repository enthält
	 */
	public boolean isLocalFolderGit() {
		if (localFolder==null || localFolder.isEmpty()) return false;
		final File folder=new File(localFolder);
		if (!folder.isDirectory()) return false;
		final File gitFolder=new File(folder,".git");
		return gitFolder.isDirectory();
	}

	/**
	 * Prüft den angegebenen privaten Schlüssel {@link #authKey}.
	 * @return	Status des privaten Schlüssels
	 * @see #authKey
	 * @see PrivateKeyStatus
	 */
	public PrivateKeyStatus getPrivateKeyStatus() {
		if (authKey==null || authKey.trim().isEmpty()) return PrivateKeyStatus.NO_KEY;
		final File keyFile=new File(authKey);
		if (!keyFile.isFile()) return PrivateKeyStatus.NO_FILE;

		final List<String> lines=Table.loadTextLinesFromFile(keyFile);
		if (lines==null || lines.size()<3) return PrivateKeyStatus.WRONG_FORMAT;

		if (!lines.get(0).equals("-----BEGIN RSA PRIVATE KEY-----")) return PrivateKeyStatus.WRONG_FORMAT;

		return PrivateKeyStatus.OK;
	}

	/**
	 * Prüft, ob sich eine Datei in dem Verzeichnis oder einem
	 * Unterverzeichnis des Repositories befindet.
	 * @param file	Zu prüfende Datei
	 * @return	Liefert <code>true</code>, wenn sich die Datei in {@link #localFolder} oder einem Unterverzeichnis davon befindet
	 */
	public boolean isFileInRepositoryFolder(final File file) {
		if (file==null || !file.isFile()) return false;
		if (localFolder==null || localFolder.trim().isEmpty()) return false;
		final File folder=new File(localFolder);
		if (!folder.isDirectory()) return false;

		try {
			return file.getCanonicalPath().contains(folder.getCanonicalPath()+File.separator);
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Stellt die Daten für den Server-Zugriff in dem Git-Objekt ein
	 * @param git	Git-Objekt in dem die Server-Daten konfiguriert werden sollen
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean initRemote(final SimpleGitWorker git) {
		switch (serverAuth) {
		case NONE:
			return git.initRemote(serverURL);
		case PASSWORD:
			return git.initRemote(serverURL,authName,authPassword);
		case KEY:
			if (authKeyPassphrase.isEmpty()) return git.initRemote(serverURL,new File(authKey)); else return git.initRemote(serverURL,new File(authKey),authKeyPassphrase);
		default:
			return git.initRemote(serverURL);
		}
	}

	/**
	 * Wandelt eine {@link SimpleGitWorker}-Statusangabe in eine Zeichenkette um.
	 * @param git	Git-Objekt aus dem ggf. weitere Fehlerinformationen abgerufen werden können
	 * @param status	Git-Rückgabestatus der in eine Zeichenkette umgewandelt werden soll
	 * @return	Git-Rückgabe als Zeichenkette
	 */
	private String getResultString(final SimpleGitWorker git, final SimpleGitWorker.Status status) {
		switch (status) {
		case NOT_INIT: return Language.tr("Git.System.Error.Init");
		case NO_FILES: return Language.tr("Git.System.Error.NoFiles");
		case ERROR: if (git.getLastError()!=null) return git.getLastError(); else return Language.tr("Git.System.Error.Error");
		case OK: return null;
		default:	return Language.tr("Git.System.Error.Error");
		}
	}

	/**
	 * Versucht auf Basis der angegebenen Daten eine Verbindung
	 * zum Git-Server herzustellen.
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String testConnection() {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			git.initForConnectionTest();
			if (!initRemote(git)) return Language.tr("Git.System.Error.InitRemote");

			return getResultString(git,git.testConnection());
		}
	}

	/**
	 * Liefert den Status des lokalen Repositories.
	 * @return	Status des lokalen Repositories als Zeichenkette
	 */
	public String getRepositoryStatus() {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			if (!git.init(new File(localFolder))) return Language.tr("Git.System.Error.Init");

			/* Aktueller Zweig */
			final String branchName=git.getCurrentBranch()[1];
			final StringBuilder result=new StringBuilder();
			result.append(Language.tr("Git.System.Status.ActiveBranch"));
			result.append(": ");
			result.append(branchName);

			/* Geänderte Dateien, die noch nicht commited sind */
			result.append("\n");
			final org.eclipse.jgit.api.Status status=git.getStatus();
			if (status.isClean()) {
				result.append(Language.tr("Git.System.Status.Clean"));
			} else {
				final int count=status.getUncommittedChanges().size()+status.getUntracked().size();
				if (count==0) result.append(Language.tr("Git.System.Status.Clean")); else {
					if (count==1) result.append(Language.tr("Git.System.Status.Uncommited.One")); else result.append(String.format(Language.tr("Git.System.Status.Uncommited"),count));
				}
			}

			/* Commits, die noch nicht gepusht sind */
			if (useServer) {
				final int[] diff=git.branchStatus(branchName);
				if (diff[0]>0) {
					result.append("\n");
					if (diff[0]==1) {
						result.append(Language.tr("Git.System.Status.Ahead.One"));
					} else {
						result.append(String.format(Language.tr("Git.System.Status.Ahead"),diff[0]));
					}
				}
				if (diff[1]>0) {
					result.append("\n");
					if (diff[1]==1) {
						result.append(Language.tr("Git.System.Status.Behind.One"));
					} else {
						result.append(String.format(Language.tr("Git.System.Status.Behind"),diff[1]));
					}
				}
			}

			return result.toString();
		}
	}

	/**
	 * Gibt es lokale Commits, die noch nicht auf den Server gepusht wurden?
	 * @return	Liefert <code>true</code>, wenn es lokale Commits gibt, die noch nicht auf dem Server sind
	 */
	public boolean hasCommitsToPush() {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			if (!git.init(new File(localFolder))) return false;
			return git.branchStatus(git.getCurrentBranch()[1])[0]>0;
		}
	}

	/**
	 * Legt ein neues Repository in {@link #localFolder} an.
	 * Es werden keine Daten von einem Server geclont.
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String initLocal() {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			if (!git.init(new File(localFolder))) return Language.tr("Git.System.Error.Init");
			return null;
		}
	}

	/**
	 * Clonet das Repository vom Server in das lokale Verzeichnis.
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String doClone(final ProgressMonitor progressMonitor) {
		try (SimpleGitWorker git=new SimpleGitWorker(progressMonitor)) {
			if (!git.initForClone(new File(localFolder))) return Language.tr("Git.System.Error.Init");
			if (!initRemote(git)) return Language.tr("Git.System.Error.InitRemote");
			return getResultString(git,git.runClone());
		}
	}

	/**
	 * Führt ein Pull vom Git-Server in das lokale Verzeichnis aus.
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String doPull(final ProgressMonitor progressMonitor) {
		try (SimpleGitWorker git=new SimpleGitWorker(progressMonitor)) {
			if (!git.init(new File(localFolder))) return Language.tr("Git.System.Error.Init");
			if (!initRemote(git)) return Language.tr("Git.System.Error.InitRemote");
			return getResultString(git,git.pull());
		}
	}

	/**
	 * Wandelt Dateiobjekte in Zeichenketten relativ zur Basis des Repositories um
	 * @param files	Dateiobjekt
	 * @return	Liste der Angaben relativ zur Basis des Repositories (Dateien, die nicht im Repository liegen, werden nicht in die Ausgabeliste übernommen)
	 */
	private String[] makeFilesRelative(final File[] files) {
		if (files==null || localFolder==null || localFolder.trim().isEmpty()) return new String[0];

		String folderName;
		try {
			final File folder=new File(localFolder);
			folderName=folder.getCanonicalPath();
			if (!folderName.endsWith(""+File.separatorChar)) folderName=folderName+File.separatorChar;
		} catch (IOException e) {
			return new String[0];
		}

		final List<String> list=new ArrayList<>();
		for (File file: files) {
			if (file==null || !file.isFile()) continue;

			String fileName;
			try {
				fileName=file.getCanonicalPath();
			} catch (IOException e) {
				continue;
			}
			if (fileName.length()<=folderName.length()) continue;
			list.add(fileName.substring(folderName.length()));
		}
		return list.toArray(new String[0]);
	}

	/**
	 * Führt ein Push auf den Git-Server aus.
	 * @param files	Zu übertragende Dateien (relativ zum Repository-Basisverzeichnis)
	 * @param authorName	Autorenname für die Commit-Nachricht
	 * @param authorEMail	Autoren-E-Mail-Adresse für die Commit-Nachricht
	 * @param message	Commit-Nachricht
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String doPush(final File[] files, final String authorName, final String authorEMail, final String message, final ProgressMonitor progressMonitor) {
		try (SimpleGitWorker git=new SimpleGitWorker(authorName,authorEMail,progressMonitor)) {
			if (!git.init(new File(localFolder))) return Language.tr("Git.System.Error.Init");
			if (!initRemote(git)) return Language.tr("Git.System.Error.InitRemote");

			final String[] relFiles=makeFilesRelative(files);
			if (relFiles==null || relFiles.length==0) return null;
			git.stageFiles(relFiles);
			git.commit(message);

			if (!useServer) return null;

			final String branchName=git.getCurrentBranch()[1];
			return getResultString(git,git.pushBranch(branchName,forcePush));
		}
	}

	/**
	 * Führt ein Push auf den Git-Server aus.
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	public String doPushOnly(final ProgressMonitor progressMonitor) {
		try (SimpleGitWorker git=new SimpleGitWorker(progressMonitor)) {
			if (!git.init(new File(localFolder))) return Language.tr("Git.System.Error.Init");
			if (!initRemote(git)) return Language.tr("Git.System.Error.InitRemote");

			if (!useServer) return null;

			final String branchName=git.getCurrentBranch()[1];
			return getResultString(git,git.pushBranch(branchName,forcePush));
		}
	}

	/**
	 * Listet alle in dem Repository verfügbaren Branches auf.
	 * @return	Liste der verfügbaren Branches (kann <code>null</code> sein, wenn keine Liste ermittelt werden konnte)
	 */
	public String[] listBranches() {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			if (!git.init(new File(localFolder))) return null;
			return git.listBranches();
		}
	}

	/**
	 * Liefert den vollständigen Namen des aktiven Zweigs.
	 * @return	Vollständiger Namen des aktiven Zweigs
	 */
	public String getCurrentBranchName() {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			if (!git.init(new File(localFolder))) return null;
			return git.getCurrentBranch()[1];
		}
	}

	/**
	 * Fügt einen neuen Zweig zum Repository hinzu und aktiviert ihn.
	 * @param newBranchName	Name des neuen Zweigs
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean addAndCheckoutBranch(final String newBranchName) {
		try (SimpleGitWorker git=new SimpleGitWorker()) {
			if (!git.init(new File(localFolder))) return false;
			if (!git.createBranch(newBranchName)) return false;
			return git.checkoutBranch(newBranchName);
		}
	}

	/**
	 * Aktiviert einen bestimmten Zweig im Repository
	 * @param branchName	Name des zu aktivierenden Zweigs
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean checkoutBranch(final String branchName, final ProgressMonitor progressMonitor) {
		try (SimpleGitWorker git=new SimpleGitWorker(progressMonitor)) {
			if (!git.init(new File(localFolder))) return false;
			return git.checkoutBranch(branchName);
		}
	}
}