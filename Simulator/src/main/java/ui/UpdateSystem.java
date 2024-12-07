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
package ui;

import java.awt.GraphicsEnvironment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.MsgBoxBackendTaskDialog;
import tools.NetHelper;
import tools.SetupData;
import xml.XMLTools;

/**
 * Update-System
 * @author Alexander Herzog
 */
public class UpdateSystem {
	/** Datei zur Speicherung angefangener Downloads bzw. zur Speicherung der Dateianteile w�hrend des Downloads */
	private static final File updateInstallerPart=new File(System.getProperty("java.io.tmpdir"),"SimulatorSetup.exe.part");
	/** Finaler Dateiname der Update-Datei */
	private static final File updateInstaller=new File(System.getProperty("java.io.tmpdir"),"SimulatorSetup.exe");
	/** Dateiname der Update-Datei, wenn diese gerade ausgef�hrt werden soll. (Wird beim Start eine entsprechende Datei gefunden, so nimmt der Simulator an, dass das Update ausgef�hrt wurde und l�scht diese.) */
	private static final File updateInstallerRun=new File(System.getProperty("java.io.tmpdir"),"SimulatorSetupWork.exe");

	/**
	 * Existiert diese Datei im Verzeichnis, in dem sich die Einstellungen befinden,
	 * so wird der Updater vollst�ndig deaktiviert.
	 * @see SetupData#updaterAvailable
	 * @see #globalOn
	 */
	public static final String UPDATER_BLOCK_FILE="noupdater.txt";

	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getUpdateSystem()
	 */
	private static UpdateSystem updateSystem;

	/**
	 * Stellt sicher, dass nicht gleichzeitig mehrere
	 * {@link #getUpdateSystem()}-Aufrufe erfolgen und
	 * so mehrere Singleton-Instanzen generiert werden.
	 * @see #getUpdateSystem()
	 */
	private static final Lock mutex=new ReentrantLock();

	/**
	 * Status der Update-Pr�fung bzw. des Update-Downloads
	 * @see UpdateSystem#updateDownloadStatus
	 */
	private enum UpdateStatus {
		/** Noch keine Verarbeitungen durchgef�hrt */
		STATUS_NOTHING,
		/** Update wird heruntergeladen */
		STATUS_LOADING,
		/** Es wird nach neuen Versionen gesucht */
		STATUS_CHECKING,
		/** Der Download der neuen Version ist fehlgeschlagen */
		STATUS_FAILED,
		/** Der Download der neuen version wurde erfolgreich abgeschlossen */
		STATUS_SUCCESS,
		/** Es steht eine neue Version zur Verf�gung. Diese muss jedoch manuell geladen werden. */
		STATUS_SUCCESS_MANUAL
	}

	/**
	 * Ist das Updater-System als solches aktiv?<br>
	 * (Das hei�t nicht, dass automatisch aktiv nach Updates gesucht wird.)
	 */
	private final boolean globalOn;

	/**
	 * Ist ein automatisches Updates f�r diese Installation m�glich?
	 * @see #isAutomaticUpdatePossible()
	 */
	private boolean automaticUpdatePossible;

	/**
	 * Gibt an, ob die Update-Pr�fung fehlgeschlagen ist.
	 * @see #checkUpdateAvailable(boolean)
	 */
	private boolean checkFailed;

	/**
	 * Gibt an, ob die Update-Pr�fung durchgef�hrt werden konnte.
	 * @see #checkUpdateAvailable(boolean)
	 */
	private boolean checkDone;

	/**
	 * Versionskennung der neuen Version auf dem Server
	 * @see #getNewVersion()
	 */
	private String newVersionAvailable;

	/**
	 * Handelt es sich um den ersten Programmstart heute?
	 * @see #checkLastStart()
	 */
	private boolean firstStartToday;

	/** Status der Update-Pr�fung bzw. des Update-Downloads */
	private volatile UpdateStatus updateDownloadStatus=UpdateStatus.STATUS_NOTHING;

	/**
	 * Gesamtgr��e der Download-Datei zur Berechnung von {@link #updateDownloadStatusPercent}
	 * in {@link #downloadFile(InputStream, File)}
	 * @see #downloadFile(InputStream, File)
	 */
	private volatile int updateDownloadStatusFullSize=0;

	/**
	 * Download-Fortschritt f�r den Setup-Dialog
	 * @see #getInfoString()
	 * @see #downloadFile(InputStream, File)
	 */
	private volatile int updateDownloadStatusPercent=0;

	/**
	 * Liefert die Instanz des Singleton <code>UpdateSystem</code>
	 * @return	Instanz von <code>UpdateSystem</code>
	 */
	public static UpdateSystem getUpdateSystem() {
		mutex.lock();
		try {
			if (updateSystem==null) updateSystem=new UpdateSystem();
			return updateSystem;
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht direkt instanziert werden, sondern es kann
	 * nur �ber {@link #getUpdateSystem()} das Singleton-Objekt dieser
	 * Klasse angefordert werden.
	 * @see #getUpdateSystem()
	 */
	private UpdateSystem() {
		final SetupData setup=SetupData.getSetup();

		globalOn=setup.updaterAvailable;
		checkFailed=false;
		checkDone=false;

		if (globalOn) {
			if (checkUpdateNow()) return;
			checkAutomaticUpdatePossible();
			checkLastStart();
			final SetupData.AutoUpdate autoUpdate=setup.autoUpdate;
			if (autoUpdate!=SetupData.AutoUpdate.OFF) {
				checkUpdateAvailable(false);
				if (automaticUpdatePossible && autoUpdate==SetupData.AutoUpdate.INSTALL) {
					if (newVersionAvailable!=null) downloadUpdate();
				}
			}
		}
	}

	/**
	 * F�hrt Aufr�umarbeiten vor einer Update-Suche aus.
	 * @return	Liefert <code>true</code>, wenn sich das System in einem Zustand befindet, in dem eine neue Update-Suche m�glich ist
	 */
	private boolean cleanUpOnCheck() {
		boolean allOk=true;

		/* Aufr�umen */
		if (updateInstallerPart.exists()) {
			if (!updateInstallerPart.delete()) allOk=false;
		}

		/* Wenn Updater gerade ausgef�hrt wurde, Update-Installer l�schen */
		if (updateInstallerRun.exists()) {
			if (!updateInstallerRun.delete()) allOk=false;
		}

		return allOk;
	}

	/**
	 * F�hrt einen Befehl auf der Betriebsystem-Kommandozeile aus
	 * @param cmd	Auszuf�hrender Befehl
	 * @return	Liefert <code>true</code>, wenn der Befehl erfolgreich ans System �bermittelt werden konnte
	 */
	private boolean runCommand(final String[] cmd) {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * F�hrt eine Update-Pr�fung aus.
	 * @return	Liefert <code>true</code>, wenn ein Update vorhanden ist
	 */
	private boolean checkUpdateNow() {
		cleanUpOnCheck();

		/* Update jetzt ausf�hren ? */
		if (updateInstaller.isFile()) {
			if (GraphicsEnvironment.isHeadless()) {
				System.out.println(Language.tr("Update.Updater.Info"));
			} else {
				GUITools.setupUI();
				MsgBox.setBackend(new MsgBoxBackendTaskDialog());
				MsgBox.info(null,Language.tr("Update.Updater.Title"),Language.tr("Update.Updater.Info"));
			}

			if (!updateInstaller.renameTo(updateInstallerRun)) return false;

			final String[] cmd=new String[]{updateInstallerRun.getAbsolutePath(),"/Update","/D="+SetupData.getProgramFolder().toString()};
			Runtime.getRuntime().addShutdownHook(new Thread(()->runCommand(cmd),"RunUpdateThread"));
			System.exit(0);

			return true;
		}

		return false;
	}

	/**
	 * Pr�ft, ob automatische Updates f�r diese Installation m�glich sind.
	 * @see #automaticUpdatePossible
	 */
	private void checkAutomaticUpdatePossible() {
		automaticUpdatePossible=(System.getProperty("os.name").toUpperCase().contains("WIN") && SetupData.getOperationMode()==SetupData.OperationMode.USER_FOLDER_MODE);
	}

	/**
	 * Pr�ft, ob das Programm heute bereits mindestens einmal zuvor gestartet wurde
	 * @see #firstStartToday
	 */
	private void checkLastStart() {
		final SetupData setup=SetupData.getSetup();

		final Calendar cal=Calendar.getInstance();
		final SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");
		final String today=sdf.format(cal.getTime());

		if (setup.lastStart.equals(today)) {
			firstStartToday=false;
		} else {
			firstStartToday=true;
			setup.lastStart=today;
			setup.saveSetup();
		}
	}

	/**
	 * �ffnet die Verbindung zum Server, um eine Datei herunterzuladen
	 * @param urlString	Update-Request
	 * @return	Serververbindung oder <code>null</code>, wenn die Verbindung fehlgeschlagen ist.
	 */
	private InputStream openServerFile(final String urlString) {
		/* URL zusammenbauen */
		URI uri;
		try {
			uri=new URI("https://"+urlString);
		} catch (URISyntaxException e1) {return null;}

		try {
			/* Verbindung �ffnen */
			final URLConnection connect=NetHelper.openConnection(uri,false,true);
			if (connect==null) return null;

			/* InputStream zur�ckliefern */
			updateDownloadStatusFullSize=connect.getContentLength();
			return connect.getInputStream();

		} catch (IOException e) {return null;}
	}

	/**
	 * Pr�ft, ob eine bestimmte Version neuer als eine andere ist
	 * @param currentVersion	Ausgangs-Programmversion
	 * @param dataVersion	Version, von der gepr�ft werden soll, ob diese neuer als die Programmversion ist
	 * @return	Gibt <code>true</code> zur�ck, wenn <code>dataVersion</code> neuer ist als <code>currentVersion</code>
	 */
	public static boolean isNewerVersionFull(String currentVersion, String dataVersion) {
		if (dataVersion==null || dataVersion.isEmpty()) return false;
		if (currentVersion==null || currentVersion.isEmpty()) return false;
		String[] newVer=dataVersion.trim().split("\\.");
		String[] curVer=currentVersion.trim().split("\\.");
		if (newVer.length<3 || curVer.length<3) return false;

		try {
			int new1=Integer.parseInt(newVer[0]);
			int new2=Integer.parseInt(newVer[1]);
			int new3=Integer.parseInt(newVer[2]);
			int cur1=Integer.parseInt(curVer[0]);
			int cur2=Integer.parseInt(curVer[1]);
			int cur3=Integer.parseInt(curVer[2]);
			return (new1>cur1 || (new1==cur1 && new2>cur2) || (new1==cur1 && new2==cur2 && new3>cur3));
		} catch (NumberFormatException e) {return false;}
	}

	/**
	 * Pr�ft, ob eine bestimmte Version neuer als die aktuell verwendete Version ist
	 * @param dataVersion	Version, von der gepr�ft werden soll, ob diese neuer als die aktuell verwendete Programmversion ist
	 * @return	Gibt <code>true</code> zur�ck, wenn <code>dataVersion</code> neuer ist als die aktuell verwendete Programmversion
	 */
	public static boolean isNewerVersionFull(String dataVersion) {
		return isNewerVersionFull(EditModel.systemVersion,dataVersion);
	}

	/**
	 * Pr�ft, ob Update-Dateien auf dem Server vorhanden sind.
	 * @param force	F�hrt die Pr�fung nur durch, wenn die letzte Pr�fung entweder mindestens einen Tag zur�ckliegt oder hier <code>true</code> �bergeben wird.
	 * @see #newVersionAvailable
	 */
	private void checkUpdateAvailable(final boolean force) {
		if (!firstStartToday && !force) return;

		String line=null;
		final String json=downloadTextFile(MainPanel.UPDATE_API_URL);
		if (json!=null) {
			final Element root=XMLTools.jsonToXml("{root: "+json+"}",true);
			if (root!=null) line=root.getAttribute("tag_name");
		}

		if (line==null) {
			checkFailed=true;
			newVersionAvailable=null;
		}
		if (line!=null) {
			checkFailed=false;
			checkDone=true;
			if (isNewerVersionFull(line.trim())) newVersionAvailable=line; else newVersionAvailable=null;
		}
	}

	/**
	 * L�dt eine Textdatei von einer angegebenen Adresse
	 * @param urlString	Zu ladende URL
	 * @return	Inhalt der Textdatei oder im Fehlerfall <code>null</code>
	 */
	private String downloadTextFile(final String urlString) {
		URI uri;
		try {
			uri=new URI("https://"+urlString);
		} catch (URISyntaxException e1) {return null;}

		return NetHelper.loadText(uri,false,true);
	}

	/**
	 * L�dt Daten aus einem URL-Input-Stream und speichert diese in einem Datei-Output-Stream
	 * @param inputStream	URL-Input-Stream
	 * @param outputFile	Datei-Output-Stream
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 * @see #downloadFile(String, File)
	 */
	private boolean downloadFile(final InputStream inputStream, final File outputFile) {
		if (inputStream==null) return false;

		FileOutputStream out;
		try {out=new FileOutputStream(outputFile);} catch (FileNotFoundException e) {return false;}
		try (BufferedOutputStream buf=new BufferedOutputStream(out,32768)) {
			byte[] data=new byte[65536];
			int downloaded=0;
			int size;
			while((size=inputStream.read(data,0,data.length))>=0) {
				downloaded+=size;
				if (updateDownloadStatusFullSize>0) updateDownloadStatusPercent=(int)Math.round(downloaded*100.0/updateDownloadStatusFullSize);
				buf.write(data,0,size);
			}
		} catch (IOException e) {
			try {out.close();} catch (IOException e2) {}
			return false;
		}
		try {out.close();} catch (IOException e) {}
		updateDownloadStatusPercent=100;
		return true;
	}

	/**
	 * L�dt Daten von einer URL und speichert diese in einer Datei
	 * @param urlString	URL von der die Daten geladen werden sollen
	 * @param outputFile	Ausgabedatei
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean downloadFile(final String urlString, final File outputFile) {
		try (InputStream inputStream=openServerFile(urlString)) {
			return downloadFile(inputStream,outputFile);
		} catch (IOException e1) {
			return false;
		}
	}

	/**
	 * L�scht einen angefangenen (und fehlgeschlagenen) Update-Download
	 * @return	Liefert <code>true</code>, wenn die tempor�re Datei entweder gar nicht existierte oder aber erfolgreich gel�scht werden konnte
	 */
	private boolean deleteInstallerPart() {
		if (!updateInstallerPart.exists()) return true;
		return updateInstallerPart.delete();
	}

	/**
	 * Stellt den Status "Download fehlgeschlagen" ein.
	 * @see UpdateStatus#STATUS_FAILED
	 */
	private void setFailedStatus() {
		updateDownloadStatus=UpdateStatus.STATUS_FAILED;
		deleteInstallerPart();
	}

	/**
	 * Startet einen Update-Download-Prozess.
	 */
	private void downloadUpdate() {
		updateDownloadStatus=UpdateStatus.STATUS_LOADING;
		new Thread(()->{
			/* Updater laden */
			if (!downloadFile(MainPanel.UPDATE_URL+"/SimulatorSetup.exe",updateInstallerPart)) {setFailedStatus(); return;}

			updateDownloadStatus=UpdateStatus.STATUS_CHECKING;

			/* Signatur laden */
			final String signature=downloadTextFile(MainPanel.UPDATE_URL+"/SimulatorSetup.sig");
			if (signature==null) {setFailedStatus(); return;}

			/* Signatur pr�fen */
			final UpdateSystemSignature checker=new UpdateSystemSignature(updateInstallerPart);
			if (!checker.verify(signature.trim())) {setFailedStatus(); return;}

			/* Update vorbereiten */
			if (updateInstallerPart.renameTo(updateInstaller)) updateDownloadStatus=UpdateStatus.STATUS_SUCCESS;
		},"UpdateLoader").start();
	}

	/**
	 * Gibt zur�ck, ob die Voraussetzungen f�r eine automatische Aktualisierung gegeben sind.
	 * @return	Automatische Aktualisierung m�glich
	 */
	public boolean isAutomaticUpdatePossible() {
		return automaticUpdatePossible;
	}

	/**
	 * Informationen zum Status der Updatepr�fung
	 * @author Alexander Herzog
	 * @see UpdateSystem#isNewVersionAvailable()
	 */
	public enum NewVersionAvailableStatus {
		/** Noch keine Pr�fung ausgef�hrt. */
		NOT_CHECKED,

		/** Eine neue Version ist verf�gbar. */
		NEW_VERSION_AVAILABLE,

		/** Die momentan installierte Version ist aktuell. */
		VERSION_IS_UPTODATE,

		/** Die Updatepr�fung konnte nicht ausgef�hrt werden bzw. ist fehlgeschlagen. */
		CHECK_FAILED
	}

	/**
	 * Gibt zur�ck, ob auf dem Server eine neue Version vorliegt
	 * @return	Gibt <code>true</code> zur�ck, wenn ein Update vorliegt
	 * @see #getNewVersion()
	 */
	public NewVersionAvailableStatus isNewVersionAvailable() {
		if (!checkDone) return NewVersionAvailableStatus.NOT_CHECKED;
		if (checkFailed) return NewVersionAvailableStatus.CHECK_FAILED;

		if (newVersionAvailable!=null) return NewVersionAvailableStatus.NEW_VERSION_AVAILABLE; else return NewVersionAvailableStatus.VERSION_IS_UPTODATE;
	}

	/**
	 * Versionskennung der neuen Version auf dem Server
	 * @return	Gibt die Versionskennung der Update-Version zur�ck oder <code>null</code> wenn kein Update vorliegt
	 * @see #isNewVersionAvailable()
	 */
	public String getNewVersion() {
		return newVersionAvailable;
	}

	/**
	 * Gibt zur�ck, ob es sich bei dem Programmstart um den ersten Programm des Tages handelt
	 * @return	Gibt <code>true</code> zur�ck, wenn es sich um den ersten Programmstart des Tages handelt
	 */
	public boolean isFirstStartToday() {
		return firstStartToday;
	}

	/**
	 * Gibt an, ob gerade ein Update heruntergeladen wird.
	 * @return	Gibt <code>true</code> zur�ck, wenn gerade ein Update geladen wird.
	 */
	public boolean isLoading() {
		return (updateDownloadStatus==UpdateStatus.STATUS_LOADING || updateDownloadStatus==UpdateStatus.STATUS_CHECKING);
	}

	/**
	 * Liefert einen Text, der �ber den aktuellen Status informiert.
	 * @return	Aktueller Status
	 */
	public String getInfoString() {
		if (!globalOn) return Language.tr("Update.Status.GlobalOff");

		switch (isNewVersionAvailable()) {
		case NOT_CHECKED:
			return Language.tr("Update.Status.NotChecked");
		case CHECK_FAILED:
			return Language.tr("Update.Status.CheckFailed");
		case NEW_VERSION_AVAILABLE:
			/* Verarbeitung weiter unten. */
			break;
		case VERSION_IS_UPTODATE:
			return Language.tr("Update.Status.UpToDate");
		}

		switch (updateDownloadStatus) {
		case STATUS_NOTHING:
			if (!isAutomaticUpdatePossible()) return String.format(Language.tr("Update.Status.NewVersionNoAutomaticUpdate"),getNewVersion());
			return String.format(Language.tr("Update.Status.NewVersion"),getNewVersion());
		case STATUS_LOADING:
			return String.format(Language.tr("Update.Status.Loading"),getNewVersion(),updateDownloadStatusPercent);
		case STATUS_CHECKING:
			return String.format(Language.tr("Update.Status.Checking"),getNewVersion());
		case STATUS_FAILED:
			return String.format(Language.tr("Update.Status.Failed"),getNewVersion());
		case STATUS_SUCCESS:
			return String.format(Language.tr("Update.Status.Done"),getNewVersion());
		case STATUS_SUCCESS_MANUAL:
			return Language.tr("Update.Status.DoneManual");
		default:
			return String.format(Language.tr("Update.Status.NewVersion"),getNewVersion());
		}
	}

	/**
	 * F�hrt jetzt eine Update-Pr�fung durch (unabh�ngig davon, wann zu letzten Mal gepr�ft wurde).
	 * @param checkOnly F�hrt nur eine Pr�fung aus sofern Auto-Update im Setup nicht aktiviert ist.
	 */
	public void checkUpdateNow(final boolean checkOnly) {
		if (updateDownloadStatus!=UpdateStatus.STATUS_NOTHING) return;
		checkUpdateAvailable(true);
		if (newVersionAvailable!=null && automaticUpdatePossible) {
			if (SetupData.getSetup().autoUpdate==SetupData.AutoUpdate.INSTALL || !checkOnly) downloadUpdate();
		}
	}

	/**
	 * L�dt die aktueller Installer-Datei in den angegebenen Ordner herunter.
	 * Es erfolgt dabei keine Pr�fung, ob die angebotene Fassung wirklich neuer
	 * als die aktuelle Fassung ist.
	 * @param folder	Zielordner f�r den Download
	 */
	public void downloadUpdateToFolder(final File folder) {
		updateDownloadStatus=UpdateStatus.STATUS_LOADING;
		new Thread(()->{
			/* Updater laden */
			if (!downloadFile(MainPanel.UPDATE_URL+"/SimulatorSetup.exe",updateInstallerPart)) {setFailedStatus(); return;}

			updateDownloadStatus=UpdateStatus.STATUS_CHECKING;

			/* Signatur laden */
			final String signature=downloadTextFile(MainPanel.UPDATE_URL+"/SimulatorSetup.sig");
			if (signature==null) {setFailedStatus(); return;}

			/* Signatur pr�fen */
			final UpdateSystemSignature checker=new UpdateSystemSignature(updateInstallerPart);
			if (!checker.verify(signature.trim())) {setFailedStatus(); return;}

			/* Update vorbereiten */
			if (updateInstallerPart.renameTo(new File(folder,updateInstaller.getName()))) updateDownloadStatus=UpdateStatus.STATUS_SUCCESS_MANUAL;
		},"UpdateLoader").start();
	}

	/**
	 * L�scht alle m�glicherweise vorhandenen (Teil-)Updatedateien.<br>
	 * Dies stellt sicher, dass der n�chste Programmstart ordnungsgem��
	 * ohne Beeintr�chtigung durch den Updater erfolgen kann.
	 */
	public static void reset() {
		updateInstallerPart.delete();
		updateInstaller.delete();
		updateInstallerRun.delete();
	}
}