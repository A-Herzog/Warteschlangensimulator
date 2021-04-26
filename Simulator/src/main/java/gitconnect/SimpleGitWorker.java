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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Überträgt Dateien in ein Git-Repoitory
 * @author Alexander Herzog
 */
public class SimpleGitWorker implements Closeable {
	/**
	 * Rückmeldung verschiedener Git-Befehle
	 */
	public enum Status {
		/** Operation erfolgreich ausgeführt */
		OK(true),
		/** Es wurde noch kein {@link SimpleGitWorker#init(File)} ausgeführt */
		NOT_INIT(false),
		/** Die Operation ist fehlgeschlagen */
		ERROR(false),
		/** Es wurden keine Dateien verarbeitet bzw. der Zweig existiert gar nicht */
		NO_FILES(false);

		/** Handelt es sich um eine Erfolgsrückmeldung? */
		public final boolean isOk;

		/**
		 * Konstruktor des Enum
		 * @param isOk	Handelt es sich um eine Erfolgsrückmeldung?
		 */
		Status(final Boolean isOk) {
			this.isOk=isOk;
		}
	}

	/**
	 * Personendatensatz, der bei Commits verwendet werden soll
	 * @see #SimpleGitWorker(String, String)
	 * @see #commit(String)
	 */
	private final PersonIdent author;

	/**
	 * Optionaler {@link ProgressMonitor}, der bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 */
	private final ProgressMonitor progressMonitor;

	/**
	 * Internes JGit-Objekt
	 * @see #init(File)
	 */
	private Git git;

	/**
	 * Nutzername für die Verbindung zum Git-Server
	 * @see #initRemote(String, String, String)
	 */
	private String remoteName;

	/**
	 * Passwort für die Verbindung zum Git-Server
	 * @see #initRemote(String, String, String)
	 */
	private String remotePassword;

	/**
	 * Private-Key-Datei für die Verbindung zum Git-Server
	 * @see #initRemote(String, File)
	 * @see #initRemote(String, File, String)
	 */
	private File keyFile;

	/**
	 * Optionales Passwort zum Entsperren von {@link #keyFile}
	 * @see #initRemote(String, File, String)
	 */
	private String passphrase;

	/**
	 * Wird diese Instanz verwendet, um ein Repository zu clonen,
	 * so wird hier der lokale Zielpfad gespeichert.
	 * @see #initForClone(File)
	 */
	private File cloneDestination;

	/**
	 * Wird diese Instanz verwendet, um einen Verbindungstest durchzuführen?
	 * @see #initForConnectionTest()
	 */
	private boolean connectionTest;

	/**
	 * Wird diese Instanz verwendet, um ein Repository zu clonen
	 * oder um einen Verbindungstest durchzuführen,
	 * so wird hier die URL des Servers gespeichert.
	 * @see #initRemote(String, File)
	 * @see #initRemote(String, File, String)
	 * @see #initRemote(String, String, String)
	 */
	private String remoteURL;

	/**
	 * Wurde eine Operation mit {@link Status#ERROR} abgeschlossen,
	 * so können hier evtl. zusätzliche Informationen stehen
	 * (kann aber auch <code>null</code> sein).
	 * @see #getLastError()
	 */
	private String lastError;

	/**
	 * Konstruktor der Klasse
	 * @param authorName	Name des Autors, der bei Commits verwendet werden soll
	 * @param authorEMail	E-Mail-Adresse des Autors, die bei Commits verwendet werden soll
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 */
	public SimpleGitWorker(final String authorName, final String authorEMail, final ProgressMonitor progressMonitor) {
		author=new PersonIdent(authorName,authorEMail);
		this.progressMonitor=progressMonitor;
	}

	/**
	 * Konstruktor der Klasse
	 * @param authorName	Name des Autors, der bei Commits verwendet werden soll
	 * @param authorEMail	E-Mail-Adresse des Autors, die bei Commits verwendet werden soll
	 */
	public SimpleGitWorker(final String authorName, final String authorEMail) {
		this(authorName,authorEMail,null);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es werden kein Name und keine E-Mail-Adresse für Commits gesetzt.
	 * Daher sollte diese Aufrufvariante nur für Verbindungstests,
	 * Clone-Vorgänge usw. verwendet werden.
	 */
	public SimpleGitWorker() {
		this("","",null);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es werden kein Name und keine E-Mail-Adresse für Commits gesetzt.
	 * Daher sollte diese Aufrufvariante nur für Verbindungstests,
	 * Clone-Vorgänge usw. verwendet werden.
	 * @param progressMonitor	Optionale {@link ProgressMonitor}-Klasse, die bei länger andauernden Tasks benachrichtigt wird (kann <code>null</code> sein)
	 */
	public SimpleGitWorker(final ProgressMonitor progressMonitor) {
		this("","",progressMonitor);
	}

	/**
	 * Wurde eine Operation mit {@link Status#ERROR} abgeschlossen,
	 * so kann diese Methode evtl. zusätzliche Informationen liefern.
	 * @return	Zusätzliche Informationen zum letzten Fehler (kann aber auch nach einem Fehler <code>null</code> sein)
	 */
	public String getLastError() {
		return lastError;
	}

	/**
	 * Initialisiert Git in einem Verzeichnis
	 * @param dataFolder	Verzeichnis
	 * @param gitFolder	Verzeichnis in dem die Git-Daten abgelegt werden sollen (wenn <code>null</code>, wird ".git" unterhalb des Datenverzeichnisses verwendet)
	 * @return	Liefert <code>true</code>, wenn Git in dem angegebenen Verzeichnis initialisiert werden konnte
	 */
	public boolean init(final File dataFolder, final File gitFolder) {
		if (dataFolder==null) return false;

		final InitCommand init=Git.init();
		init.setDirectory(dataFolder);
		if (gitFolder!=null) init.setGitDir(gitFolder);
		try {
			git=init.call();
		} catch (GitAPIException e) {
			return false;
		}

		return true;
	}

	/**
	 * Initialisiert Git in einem Modus, der nur zum Clonen verwendet werden kann.
	 * @param dataFolder	Lokales Zielverzeichnis für das Clonen
	 * @return	Liefert <code>true</code>, wenn das Verzeichnis erfasst werden konnte
	 */
	public boolean initForClone(final File dataFolder) {
		if (dataFolder==null) return false;
		cloneDestination=dataFolder;
		return true;
	}

	/**
	 * Initialisiert Git in einem Modus, der nur für Verbindungstest verwendet werden kann.
	 */
	public void initForConnectionTest() {
		connectionTest=true;
	}

	/**
	 * Bereitet die Verbindung zu einem Git-Server vor (es wird noch keine Verbindung aufgebaut).
	 * @param remoteURL	Adresse des Servers
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich registriert werden konnten
	 * @see #initRemote(String, File)
	 * @see #initRemote(String, File, String)
	 * @see #initRemote(String, String, String)
	 */
	public boolean initRemote(final String remoteURL) {
		if (cloneDestination!=null || connectionTest) {
			this.remoteURL=remoteURL;
			return true;
		}
		if (git==null) return false;

		final RemoteAddCommand remoteAdd=git.remoteAdd();
		remoteAdd.setName("origin");
		try {
			remoteAdd.setUri(new URIish(remoteURL));
		} catch (URISyntaxException e1) {
			return false;
		}
		try {
			remoteAdd.call();
		} catch (GitAPIException e) {
			return false;
		}

		return true;
	}

	/**
	 * Bereitet die Verbindung zu einem Git-Server vor (es wird noch keine Verbindung aufgebaut).
	 * @param remoteURL	Adresse des Servers
	 * @param remoteName	Nutzername für die Verbindung zum Git-Server
	 * @param remotePassword	Passwort für die Verbindung zum Git-Server
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich registriert werden konnten, oder aber, wenn ein vorheriger Aufruf bereits erfolgreich war (in diesem Fall werden die neuen Daten nicht übernommen).
	 */
	public boolean initRemote(final String remoteURL, final String remoteName, final String remotePassword) {
		if (this.remoteName!=null || this.keyFile!=null) return true;
		if (remoteName==null || remoteName.trim().isEmpty() || remotePassword==null || remotePassword.trim().isEmpty()) return false;

		if (!initRemote(remoteURL)) return false;

		this.remoteName=remoteName;
		this.remotePassword=remotePassword;
		return true;
	}

	/**
	 * Bereitet die Verbindung zu einem Git-Server vor (es wird noch keine Verbindung aufgebaut).
	 * @param remoteURL	Adresse des Servers
	 * @param keyFile	Privater Schlüssel für die Verbindung
	 * @param passphrase	Optionales Passwort zum Entsperren des Schlüssels
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich registriert werden konnten, oder aber, wenn ein vorheriger Aufruf bereits erfolgreich war (in diesem Fall werden die neuen Daten nicht übernommen).
	 */
	public boolean initRemote(final String remoteURL, final File keyFile, final String passphrase) {
		if (this.remoteName!=null || this.keyFile!=null) return true;
		if (keyFile==null || !keyFile.isFile()) return false;

		if (!initRemote(remoteURL)) return false;

		this.keyFile=keyFile;
		this.passphrase=passphrase;
		return true;
	}

	/**
	 * Bereitet die Verbindung zu einem Git-Server vor (es wird noch keine Verbindung aufgebaut).
	 * @param remoteURL	Adresse des Servers
	 * @param keyFile	Privater Schlüssel für die Verbindung
	 * @return	Liefert <code>true</code>, wenn die Daten erfolgreich registriert werden konnten, oder aber, wenn ein vorheriger Aufruf bereits erfolgreich war (in diesem Fall werden die neuen Daten nicht übernommen).
	 */
	public boolean initRemote(final String remoteURL, final File keyFile) {
		return initRemote(remoteURL,keyFile,null);
	}

	/**
	 * Initialisiert Git in einem Verzeichnis
	 * @param folder	Verzeichnis
	 * @return	Liefert <code>true</code>, wenn Git in dem angegebenen Verzeichnis initialisiert werden konnte
	 */
	public boolean init(final File folder) {
		return init(folder,null);
	}

	/**
	 * Ermöglicht Zugriff auf das interne Git-Objekt.
	 * @return	Internes Git-Objekt (kann <code>null</code> sein, wenn {@link #init(File)} noch nicht erfolgreich ausgeführt wurde)
	 * @see #init(File)
	 */
	public Git getGit() {
		return git;
	}

	/**
	 * Liefert ein 2-elementiges Array aus kurzen und aus vollständigem Branch-Namen.
	 * @return	2-elementiges Array aus kurzen und aus vollständigem Branch-Namen (im Fehlerfall sind beide Strings leer, aber nicht <code>null</code>)
	 */
	@SuppressWarnings("resource") /* Das Repository wird über git.close() und damit über close() dieses Objektes geschlossen. */
	public String[] getCurrentBranch() {
		final String[] result=new String[] {"",""};

		if (git!=null) try {
			final Repository repository=git.getRepository();
			result[0]=repository.getBranch();
			result[1]=repository.getFullBranch();
		} catch (IOException e) {
			result[0]="";
			result[1]="";
		}

		return result;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Branches (Langnamen).
	 * @return	Liste mit allen verfügbaren Branches (Langnamen)
	 */
	public String[] listBranches() {
		if (git==null) return new String[0];

		try {
			return git.branchList().call().stream().map(ref->ref.getName()).toArray(String[]::new);
		} catch (GitAPIException e) {
			return new String[0];
		}
	}

	/**
	 * Aktiviert einen bestimmten Branch.
	 * @param branchName	Kurz- oder Langname des zu aktivierenden Branches
	 * @return	Liefert <code>true</code>, wenn der Branch aktiviert werden konnte
	 */
	public boolean checkoutBranch(final String branchName) {
		if (git==null) return false;
		if (branchName==null || branchName.trim().isEmpty()) return false;

		try {
			final CheckoutCommand checkout=git.checkout();
			if (progressMonitor!=null) checkout.setProgressMonitor(progressMonitor);
			checkout.setName(branchName);
			checkout.call();
		} catch (GitAPIException e) {
			return false;
		}

		return true;
	}

	/**
	 * Erstellt einen neuen Branch.
	 * @param branchShortName	Kurzname des neuen Branches
	 * @return	Liefert <code>true</code>, wenn der Branch erstellt werden konnte oder schon existierte
	 */
	public boolean createBranch(final String branchShortName) {
		if (git==null) return false;
		if (branchShortName==null || branchShortName.trim().isEmpty() || branchShortName.contains("/")) return false;

		try {
			git.branchCreate().setName(branchShortName).call();
		} catch (RefAlreadyExistsException e) {
			return true;
		} catch (GitAPIException e) {
			return false;
		}

		return true;
	}

	/**
	 * Nimmt mehrere Dateien in den Staging-Bereich auf.
	 * @param files	Liste der zu stagenden Dateien (Pfade relativ zum Git-Basisverzeichnis)
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	public Status stageFiles(final String[] files) {
		if (git==null) return Status.NOT_INIT;
		if (files==null || files.length==0) return Status.NO_FILES;

		final AddCommand add=git.add();
		for (String file: files) add.addFilepattern(file);

		try {
			add.call();
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}

		return Status.OK;
	}

	/**
	 * Nimmt eine Datei in den Staging-Bereich auf.
	 * @param file	Zu stagende Datei (Pfad relativ zum Git-Basisverzeichnis)
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	public Status stageFile(final String file) {
		return stageFiles(new String[] {file});
	}

	/**
	 * Überträgt die per {@link #stageFile(String)} bzw. per {@link #stageFiles(String[])}
	 * ausgewählte Dateien in das Git.
	 * @param message	Nachricht zu dem Commit
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	public Status commit(final String message) {
		if (git==null) return Status.NOT_INIT;

		final CommitCommand commit=git.commit();
		commit.setAuthor(author);
		commit.setAllowEmpty(false);
		commit.setMessage(message);
		try {
			commit.call();
		} catch (EmptyCommitException e) {
			return Status.NO_FILES;
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}

		return Status.OK;
	}

	/**
	 * Befindet sich ein bestimmter Commit im aktuellen Branch?
	 * @param commitName	ID des Commits
	 * @return	Liefert <code>true</code>, wenn sich der Commit im aktuellen Branch befindet
	 */
	@SuppressWarnings("resource") /* Wir wollen das git.getRepository() nicht schließen, dass passiert erst, wenn git.close() aufgerufen wird */
	public boolean isCommitInCurrentBranch(final String commitName) {
		if (git==null) return false;
		final Repository repo=git.getRepository();

		try (RevWalk walk=new RevWalk(repo)) {
			final String branchName=repo.getFullBranch();
			final RevCommit targetCommit=walk.parseCommit(repo.resolve(commitName));
			for (Ref ref: repo.getRefDatabase().getRefs()) {
				if (!ref.getName().startsWith(Constants.R_HEADS)) continue;
				if (walk.isMergedInto(targetCommit,walk.parseCommit(ref.getObjectId()))) {
					final String foundInBranch=ref.getName();
					if (branchName.equals(foundInBranch)) return true;
				}
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Liefert die Anzahl an Abweichungen zwischen lokalem Repository und Server
	 * @param branchName	Name des Zweigs dessen Daten ausgelesen werden sollen
	 * @return	2-elementiges Array mit den Anzahlen an Commits, die das lokale Repository voraus und hinter dem Server hinterher ist
	 */
	@SuppressWarnings("resource")
	public int[] branchStatus(final String branchName) {
		final Repository repository=git.getRepository();
		try {
			final BranchTrackingStatus branchStatus=BranchTrackingStatus.of(repository,branchName);
			if (branchStatus==null) return new int[] {0,0};
			return new int[] {branchStatus.getAheadCount(),branchStatus.getBehindCount()};
		} catch (IOException e) {
			return new int[] {0,0};
		}
	}

	/**
	 * Fügt einen anderen Zweig in den aktuellen Zweig ein.
	 * Bei Konflikten haben die Daten aus dem einzufügenden Zweig Vorrang.
	 * @param branchName	Name des einzufügenden Zweigs
	 * @param message	Meldung die bei dem zum Merge zugehörigen Commit verwendet werden soll
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	@SuppressWarnings("resource") /* Das Repository wird über git.close() und damit über close() dieses Objektes geschlossen. */
	public Status mergeOtherBranchToCurrent(final String branchName, final String message) {
		if (git==null) return Status.NOT_INIT;
		if (branchName==null || branchName.trim().isEmpty()) return Status.NO_FILES;

		final Repository repository=git.getRepository();

		try {
			final ObjectId current=repository.resolve(getCurrentBranch()[1]);
			if (current==null) return Status.ERROR;

			final ObjectId other=repository.resolve(branchName);
			if (other==null) return Status.NO_FILES;

			final ThreeWayMerger testMerger=MergeStrategy.RECURSIVE.newMerger(repository,true);
			if (!testMerger.merge(current,other)) return Status.ERROR;

			final ThreeWayMerger workMerger=MergeStrategy.RECURSIVE.newMerger(repository,false);
			if (progressMonitor!=null) workMerger.setProgressMonitor(progressMonitor);
			if (!workMerger.merge(current,other)) return Status.ERROR;

			final MergeCommand merge=git.merge();
			merge.include(other);
			if (progressMonitor!=null) merge.setProgressMonitor(progressMonitor);
			merge.call();
		} catch (RevisionSyntaxException | IOException | GitAPIException e1) {
			lastError=e1.getMessage();
			return Status.ERROR;
		}

		try {
			git.branchDelete().setBranchNames(branchName).call();
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}

		return Status.OK;
	}

	/**
	 * Legt das Transport-Konfigurationsobjekt an.
	 * @return	Transport-Konfigurationsobjekt
	 * @see #setupTransportCommand(TransportCommand)
	 */
	private TransportConfigCallback getTransportConfigCallback() {
		final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
			@Override
			protected void configure(final Host host, final Session session) {}
			@Override
			public String getType() {return null;}
			@Override
			protected JSch createDefaultJSch(final FS fs) throws JSchException {
				final JSch defaultJSch=super.createDefaultJSch(fs);
				if (passphrase!=null && !passphrase.isEmpty()) {
					defaultJSch.addIdentity(keyFile.toString(),passphrase);
				} else {
					defaultJSch.addIdentity(keyFile.toString());
				}
				return defaultJSch;
			}
		};

		return new TransportConfigCallback() {
			@Override
			public void configure(final Transport transport) {
				final SshTransport sshTransport=(SshTransport)transport;
				sshTransport.setSshSessionFactory(sshSessionFactory);
			}
		};
	}

	/**
	 * Konfiguriert einen Transport-basierten Befehl
	 * in Bezug auf die Serverzugangsdaten.
	 * @param transport	Transport-basierter Befehl
	 */
	private void setupTransportCommand(final TransportCommand<?,?> transport) {
		if (remoteName!=null) transport.setCredentialsProvider(new UsernamePasswordCredentialsProvider(remoteName,remotePassword));
		if (keyFile!=null) transport.setTransportConfigCallback(getTransportConfigCallback());
	}

	/**
	 * Clont ein entferntes Repository in ein lokales Verzeichnis
	 * @return	Rückmeldung des Git-Aufrufs
	 * @see #initForClone(File)
	 */
	public Status runClone() {
		final CloneCommand clone=Git.cloneRepository();

		clone.setURI(remoteURL);
		clone.setDirectory(cloneDestination);
		setupTransportCommand(clone);
		if (progressMonitor!=null) clone.setProgressMonitor(progressMonitor);

		try {
			try (Git git=clone.call()) {
				/* Try wird nur verwendet, um git.close() sicher auszuführen. */
			}
		} catch (GitAPIException | JGitInternalException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}

		return Status.OK;
	}

	/**
	 * Liefert eine Liste der Remote-Branches
	 * (wird genauso initialisiert wie ein {@link #runClone()}-Aufruf).
	 * @return	Liefert im Erfolgsfall eine Liste; im Fehlerfall <code>null</code>
	 */
	public Collection<Ref> listRemote() {
		final LsRemoteCommand ls=Git.lsRemoteRepository();
		ls.setHeads(true);
		ls.setRemote(remoteURL);
		setupTransportCommand(ls);
		try {
			return ls.call();
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return null;
		}
	}

	/**
	 * Führt einen Push-Befehl, in dem die zu pushenden Branches bereit ausgewählt sind, aus.
	 * @param push	Auszuführender Push-Befehl
	 * @param force	Push erzwingen (auch wenn lokaler und remote Branch keine gemeinsame Basis haben)
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	private Status setupAndRunPush(final PushCommand push, final boolean force) {
		setupTransportCommand(push);
		if (progressMonitor!=null) push.setProgressMonitor(progressMonitor);
		if (force) push.setForce(true);

		String msg=null;

		try {
			final Iterable<PushResult> results=push.call();

			for (PushResult result: results) {
				for (RemoteRefUpdate update: result.getRemoteUpdates()) {
					if (update.getStatus()==RemoteRefUpdate.Status.OK) continue;
					if (msg==null) {
						msg=update.getStatus().toString();
					} else {
						msg=msg+"\n"+update.getStatus().toString();
					}
				}
			}
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}

		if (msg!=null) {
			lastError=msg;
			return Status.ERROR;
		}
		return Status.OK;
	}

	/**
	 * Pushed einen Branch auf den Server .
	 * @param branchName	Zu pushender Branch
	 * @param force	Push erzwingen (auch wenn lokaler und remote Branch keine gemeinsame Basis haben)
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	@SuppressWarnings("resource") /* Das Repository wird über git.close() und damit über close() dieses Objektes geschlossen. */
	public Status pushBranch(final String branchName, final boolean force) {
		if (git==null || (remoteName==null && keyFile==null)) return Status.NOT_INIT;
		if (branchName==null || branchName.trim().isEmpty()) return Status.NO_FILES;

		final Repository repository=git.getRepository();
		Ref branch;
		try {
			branch=repository.findRef(branchName);
		} catch (IOException e1) {
			lastError=e1.getMessage();
			return Status.ERROR;
		}
		if (branch==null) return Status.NO_FILES;

		final PushCommand push=git.push();
		push.add(branch);
		return setupAndRunPush(push,force);
	}

	/**
	 * Pushed alle Branches auf den Server.
	 * @param force	Push erzwingen (auch wenn lokaler und remote Branch keine gemeinsame Basis haben)
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	public Status pushAllBranches(final boolean force) {
		if (git==null || (remoteName==null && keyFile==null)) return Status.NOT_INIT;

		final PushCommand push=git.push();
		push.setPushAll();
		return setupAndRunPush(push,force);
	}

	/**
	 * Synchronisiert die Serverdaten in das lokale Repository.
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	public Status pull() {
		if (git==null || (remoteName==null && keyFile==null)) return Status.NOT_INIT;

		final PullCommand pull=git.pull();

		setupTransportCommand(pull);
		if (progressMonitor!=null) pull.setProgressMonitor(progressMonitor);

		try {
			pull.call();
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}

		return Status.OK;
	}

	/**
	 * Liefert den Status des Repositories.
	 * @return	Status des Repositories
	 */
	public org.eclipse.jgit.api.Status getStatus() {
		try {
			return git.status().call();
		} catch (NoWorkTreeException | GitAPIException e) {
			lastError=e.getMessage();
			return null;
		}
	}

	/**
	 * Liefert eine Liste der Commits.
	 * @return	Liste der Commits (oder im Fehlerfall <code>null</code>)
	 */
	public List<String> getLog() {
		if (git==null) return null;

		final Iterable<RevCommit> logs;
		try {
			logs=git.log().all().call();
		} catch (GitAPIException | IOException e) {
			lastError=e.getMessage();
			return null;
		}

		final List<String> list=new ArrayList<>();

		for (RevCommit rev: logs) {
			final StringBuilder record=new StringBuilder();
			record.append(Instant.ofEpochSecond(rev.getCommitTime()));
			record.append(": ");
			record.append(rev.getFullMessage());
			record.append("\n");
			record.append("ID: ");
			record.append(rev.getId().getName());
			record.append("\n");
			record.append("Author: ");
			record.append(rev.getAuthorIdent().getName());
			record.append(" (");
			record.append(rev.getAuthorIdent().getEmailAddress());
			record.append(")");
			list.add(record.toString());
		}

		return list;
	}

	/**
	 * Prüft die Verbindung zum Git-Server.
	 * @return	Rückmeldung des Git-Aufrufs
	 */
	public Status testConnection() {
		/* Verbindungsaufbau vorbereiten */
		final LsRemoteCommand lsCmd=new LsRemoteCommand(null);
		lsCmd.setRemote(remoteURL);
		setupTransportCommand(lsCmd);

		/* Verbindung testen */
		try {
			final Collection<Ref> ref=lsCmd.call();
			if (ref!=null && ref.size()>0) return Status.OK; else return Status.ERROR;
		} catch (GitAPIException e) {
			lastError=e.getMessage();
			return Status.ERROR;
		}
	}

	@Override
	public void close() {
		if (git!=null) {
			git.close();
			git=null;
		}
	}
}
