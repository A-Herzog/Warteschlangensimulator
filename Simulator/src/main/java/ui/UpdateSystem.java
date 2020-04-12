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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.MsgBoxBackendTaskDialog;
import tools.NetHelper;
import tools.SetupData;

/**
 * Update-System
 * @author Alexander Herzog
 */
public class UpdateSystem {
	private static final File updateInstallerPart=new File(System.getProperty("java.io.tmpdir"),"SimulatorSetup.exe.part");
	private static final File updateInstaller=new File(System.getProperty("java.io.tmpdir"),"SimulatorSetup.exe");
	private static final File updateInstallerRun=new File(System.getProperty("java.io.tmpdir"),"SimulatorSetupWork.exe");

	private static UpdateSystem updateSystem;
	private static final Lock mutex=new ReentrantLock();

	private enum UpdateStatus {
		STATUS_NOTHING,
		STATUS_LOADING,
		STATUS_CHECKING,
		STATUS_FAILED,
		STATUS_SUCCESS
	}

	private boolean automaticUpdatePossible;
	private boolean checkFailed;
	private String newVersionAvailable;
	private boolean firstStartToday;
	private volatile UpdateStatus updateDownloadStatus=UpdateStatus.STATUS_NOTHING;
	private volatile int updateDownloadStatusFullSize=0;
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
	 * Konstruktor der Klasse <code>UpdateSystem</code><br>
	 * Da es sich um ein Singleton handelt, kann der Konstruktor nicht direkt aufgerufen werden,
	 * sondern muss die Instanz �ber <code>getUpdateSystem()</code> abgerufen werden.
	 * @see #getUpdateSystem()
	 */
	private UpdateSystem() {
		checkFailed=false;
		if (checkUpdateNow()) return;
		checkAutomaticUpdatePossible();
		checkLastStart();
		checkUpdateAvailable(false);
		if (newVersionAvailable!=null && automaticUpdatePossible && SetupData.getSetup().autoUpdate) downloadUpdate();
	}

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

	private boolean runCommand(final String cmd) {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {return false;}
		return true;
	}

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

			final String cmd=updateInstallerRun.getAbsolutePath()+" /S /D="+SetupData.getProgramFolder().toString();
			Runtime.getRuntime().addShutdownHook(new Thread(()->runCommand(cmd),"RunUpdateThread"));
			System.exit(0);

			return true;
		}

		return false;
	}

	/**
	 * Pr�ft, ob automatische Updates f�r diese Installation m�glich sind.
	 * @see automaticUpdatePossible
	 */
	private void checkAutomaticUpdatePossible() {
		boolean b=(System.getProperty("os.name").toUpperCase().contains("WIN") && SetupData.getProgramFolder().toString().equals(SetupData.getSetupFolder().toString()));
		if (b) {
			final File programFile=new File(SetupData.getProgramFolder(),"Simulator.exe");
			b=programFile.exists();
		}
		automaticUpdatePossible=b;
	}

	/**
	 * Pr�ft, ob das Programm heute bereits mindestens einmal zuvor gestartet wurde
	 * @see firstStartToday
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
	 * @param request	Update-Request
	 * @return	Serververbindung oder <code>null</code>, wenn die Verbindung fehlgeschlagen ist.
	 */
	private InputStream openServerFile(final String urlString) {
		/* URL zusammenbauen */
		URL url;
		try {
			url=new URL("https://"+urlString);
		} catch (MalformedURLException e1) {return null;}

		try {
			/* Verbindung �ffnen */
			final URLConnection connect=NetHelper.openConnection(url,false,true);
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
	 * @see newVersionAvailable
	 */
	private void checkUpdateAvailable(final boolean force) {
		if (!firstStartToday && !force) return;

		final String line=downloadTextFile(MainPanel.HOME_URL+"/Warteschlangensimulator/version.txt");
		if (line==null) {
			checkFailed=true;
			newVersionAvailable=null;
		}
		if (line!=null) {
			checkFailed=false;
			if (isNewerVersionFull(line.trim())) newVersionAvailable=line; else newVersionAvailable=null;
		}
	}

	private String downloadTextFile(final String urlString) {
		URL url;
		try {
			url=new URL("https://"+urlString);
		} catch (MalformedURLException e1) {return null;}

		return NetHelper.loadText(url,false,true);
	}

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

	private boolean downloadFile(final String urlString, final File outputFile) {
		try (InputStream inputStream=openServerFile(urlString)) {
			return downloadFile(inputStream,outputFile);
		} catch (IOException e1) {
			return false;
		}
	}

	private boolean deleteInstallerPart() {
		if (!updateInstallerPart.exists()) return true;
		return updateInstallerPart.delete();
	}

	private void setFailedStatus() {
		updateDownloadStatus=UpdateStatus.STATUS_FAILED;
		deleteInstallerPart();
	}

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
		switch (isNewVersionAvailable()) {
		case CHECK_FAILED:
			return Language.tr("Update.Status.CheckFailed");
		case NEW_VERSION_AVAILABLE:
			/* Verarbeitung weiter unten. */
			break;
		case VERSION_IS_UPTODATE:
			return Language.tr("Update.Status.UpToDate");
		}
		if (!isAutomaticUpdatePossible()) return String.format(Language.tr("Update.Status.NewVersionNoAutomaticUpdate"),getNewVersion());

		switch (updateDownloadStatus) {
		case STATUS_NOTHING: return String.format(Language.tr("Update.Status.NewVersion"),getNewVersion());
		case STATUS_LOADING: return String.format(Language.tr("Update.Status.Loading"),getNewVersion(),updateDownloadStatusPercent);
		case STATUS_CHECKING: return String.format(Language.tr("Update.Status.Checking"),getNewVersion());
		case STATUS_FAILED: return String.format(Language.tr("Update.Status.Failed"),getNewVersion());
		case STATUS_SUCCESS: return String.format(Language.tr("Update.Status.Done"),getNewVersion());
		default: return String.format(Language.tr("Update.Status.NewVersion"),getNewVersion());
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
			if (SetupData.getSetup().autoUpdate || !checkOnly) downloadUpdate();
		}
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