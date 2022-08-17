/**
 * Copyright 2022 Alexander Herzog
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
package ui.tools;

import java.awt.Component;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;

/**
 * System zum Abspielen von beliebigen Klängen
 * (z.B. wenn bestimmte Punkte in einer Animation erreicht wurden).<br>
 * Das Sound-System ist ein Singleton und kann nicht direkt, sondern
 * nur über die statische Methode {@link #getInstance()} instanziert werden.
 * @author Alexander Herzog
 */
public class SoundSystem {
	/**
	 * Instanz des Singletons
	 * @see #getInstance()
	 */
	private static SoundSystem instance;

	/**
	 * Pseudo-Bezeichner für den Piepton-System-Sound.
	 * @see #getSystemSounds()
	 * @see #playSystemSound(String)
	 */
	public static final String BEEP_SOUND="beep";

	/**
	 * Prefixe für Windows-System-Sounds in der Auflistung der AWT-Eigenschaften
	 * @see #getSystemSounds()
	 */
	private static final String WIN_SOUND_PREFIX="win.sound.";

	/**
	 * Name des Verzeichnisses unterhalb des Windows-Ordners in dem sich Sound-Dateien befinden
	 * @see #winSoundFilesFolder
	 * @see #getSoundFiles()
	 */
	private static final String WIN_SOUND_FILES_SUB_FOLDER="Media";

	/**
	 * Referenz auf das globale AWT-Toolkit-Objekt
	 */
	private final Toolkit toolkit;

	/**
	 * Ordner unterhalb des Windows-Ordners in dem sich Sound-Dateien befinden
	 * (kann <code>null</code> sein, wenn es keinen solchen Ordner gibt)
	 * @see #getSoundFiles()
	 */
	private final File winSoundFilesFolder;

	/**
	 * Liste der unterstützten Dateiformate (=Dateiendungen)
	 * @see #getSupportedFormats()
	 */
	private String[] supportedFormats;

	/**
	 * Liste der System-Sounds
	 * @see #getSystemSounds()
	 */
	private String[] systemSounds;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt ein Singleton dar. Daher kann der Konstruktor
	 * nicht direkt aufgerufen werden, sondern es muss die statische
	 * Methode {@link #getInstance()} verwendet werden.
	 * @see #getInstance()
	 */
	private SoundSystem() {
		toolkit=Toolkit.getDefaultToolkit();
		final String winDir=System.getenv("windir");
		if (winDir==null) {
			winSoundFilesFolder=null;
		} else {
			final File folder=new File(winDir,WIN_SOUND_FILES_SUB_FOLDER);
			winSoundFilesFolder=folder.isDirectory()?folder:null;
		}
	}

	/**
	 * Liefert die Instanz dieses Singletons.
	 * @return	Instanz des Singletons
	 */
	public static synchronized SoundSystem getInstance() {
		if (instance==null) instance=new SoundSystem();
		return instance;
	}

	/**
	 * Liefert eine Liste der unterstützten Dateiformate (=Dateiendungen).
	 * @return	Liste der unterstützten Dateiformate (=Dateiendungen)
	 */
	public synchronized String[] getSupportedFormats() {
		if (supportedFormats==null) supportedFormats=Arrays.asList(AudioSystem.getAudioFileTypes()).stream().map(type->type.getExtension()).toArray(String[]::new);
		return supportedFormats;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren System-Sounds.
	 * @return	Unter Nicht-Windows-Systemen enthält die Liste nur {@link #BEEP_SOUND}, sonst mehr Einträge.
	 * @see #playSystemSound(String)
	 */
	public synchronized String[] getSystemSounds() {
		if (systemSounds==null) {
			final List<String> list=new ArrayList<>();
			list.add(BEEP_SOUND);
			final String propNames[]=(String[])toolkit.getDesktopProperty("win.propNames");
			if (propNames!=null) Arrays.asList(propNames).stream().filter(propName->propName.startsWith(WIN_SOUND_PREFIX)).map(propName->propName.substring(WIN_SOUND_PREFIX.length())).forEach(list::add);
			systemSounds=list.toArray(new String[0]);
		}
		return systemSounds;
	}

	/**
	 * Spielt einen System-Sound ab.
	 * @param name	Name des abzuspielenden System-Sounds. Wird ein ungültiger Name oder <code>null</code> übergeben, so wird der Standard-Piepton abgespielt.
	 */
	public void playSystemSound(final String name) {
		if (name==null || name.equals(BEEP_SOUND)) {
			toolkit.beep();
			return;
		}

		final Runnable soundPlayer=(Runnable)toolkit.getDesktopProperty(WIN_SOUND_PREFIX+name);
		if (soundPlayer==null) {
			toolkit.beep();
		} else {
			soundPlayer.run();
		}
	}

	/**
	 * Liefert eine Liste mit allen systemweiten Sound-Dateien (aus dem Windows-Medien-Ordner).
	 * @return	Liste mit allen systemweiten Sound-Dateien (ist insbesondere auf Nicht-Windows-Systemen leer, aber ist nie <code>null</code>)
	 */
	public File[] getSoundFiles() {
		if (winSoundFilesFolder==null) return new File[0];
		final File[] files=winSoundFilesFolder.listFiles(pathName->pathName.toString().toLowerCase().endsWith(".wav"));
		if (files==null) return new File[0];
		return files;
	}

	/**
	 * Aktuelle Ausgabe
	 * @see #playSoundFile(File)
	 * @see #playSoundFile(File, int)
	 * @see #stopSoundFile()
	 */
	private Clip lastClip;

	/**
	 * Timer-Objekt zum zeitgesteuerten Abbruch der Ausgabe
	 * @see #playSoundFile(File)
	 * @see #playSoundFile(File, int)
	 */
	private Timer lastSoundStopTimer;

	/**
	 * Bricht die Ausgabe der aktuellen Sound-Datei ab.
	 * @see #playSoundFile(File)
	 * @see #playSoundFile(File, int)
	 */
	public synchronized void stopSoundFile() {
		if (lastClip!=null) {
			lastClip.stop();
			lastClip.close();
			lastClip=null;
		}
	}

	/**
	 * Spielt eine Sound-Datei bis zum Ende ab.
	 * @param file	Abzuspielende Datei
	 * @return	Liefert <code>true</code>, wenn die Ausgabe gestartet werden konnte
	 * @see #playSoundFile(File, int)
	 */
	public boolean playSoundFile(final File file) {
		return playSoundFile(file,-1);
	}

	/**
	 * Spielt eine Sound-Datei bis zum Ende ab.
	 * @param fileName	Abzuspielende Datei
	 * @return	Liefert <code>true</code>, wenn die Ausgabe gestartet werden konnte
	 * @see #playSoundFile(File)
	 */
	public boolean playSoundFile(final String fileName) {
		final File file=(fileName==null)?null:new File(fileName);
		return playSoundFile(file,-1);
	}

	/**
	 * Spielt eine Sound-Datei bis zum Ende ab bzw. bis zu einer bestimmten Dauer ab.
	 * @param fileName	Abzuspielende Datei
	 * @param maxSeconds	Maximal abzuspielende Dauer (ohne Begrenzung, wenn ein Wert &le;0 übergeben wird)
	 * @return	Liefert <code>true</code>, wenn die Ausgabe gestartet werden konnte
	 * @see #playSoundFile(File, int)
	 */
	public boolean playSoundFile(final String fileName, final int maxSeconds) {
		final File file=(fileName==null)?null:new File(fileName);
		return playSoundFile(file,maxSeconds);
	}

	/**
	 * Spielt eine Sound-Datei bis zum Ende ab bzw. bis zu einer bestimmten Dauer ab.
	 * @param file	Abzuspielende Datei
	 * @param maxSeconds	Maximal abzuspielende Dauer (ohne Begrenzung, wenn ein Wert &le;0 übergeben wird)
	 * @return	Liefert <code>true</code>, wenn die Ausgabe gestartet werden konnte
	 * @see #playSoundFile(File)
	 */
	public synchronized boolean playSoundFile(final File file, final int maxSeconds) {
		/* Bisherige Ausgaben abbrechen */

		if (lastSoundStopTimer!=null) {
			lastSoundStopTimer.cancel();
			lastSoundStopTimer=null;
		}
		if (lastClip!=null) {
			lastClip.stop();
			lastClip.close();
			lastClip=null;
		}

		/* Neue Ausgabe starten */

		if (file==null || !file.isFile()) return false;
		try {
			lastClip=AudioSystem.getClip();
			try (AudioInputStream inputStream=AudioSystem.getAudioInputStream(file)) {
				lastClip.open(inputStream);
			}
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			lastClip=null;
			return false;
		}
		lastClip.start();

		/* Ggf. Abbruchtimer einrichten */

		if (maxSeconds>0) {
			final long duration=lastClip.getMicrosecondLength();
			if (duration==AudioSystem.NOT_SPECIFIED || duration>maxSeconds*1000*1000) {
				lastSoundStopTimer=new Timer("SoundStopper",true);
				lastSoundStopTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						stopSoundFile();
					}
				},1000*maxSeconds);
			}
		}

		return true;
	}

	/**
	 * Spielt einen System-Sound oder eine Sound-Datei ab.
	 * @param sound	Bezeichner für den System-Sound oder Dateiname der Sound-Datei
	 * @param maxSeconds	Bei Sound-Dateien maximal abzuspielende Sekunden (Werte &le;0 für keine Begrenzung)
	 * @return	Liefert <code>true</code>, wenn der Sound erfolgreich abgespielt werden konnte
	 */
	public boolean playAll(final String sound, final int maxSeconds) {
		final Set<String> systemSounds=new HashSet<>(Arrays.asList(getSystemSounds()));
		if (systemSounds.contains(sound)) {
			playSystemSound(sound);
			return true;
		} else {
			return playSoundFile(sound,maxSeconds);
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Sound-Datei an.
	 * @param parent	Elternkomponente des Dialogs
	 * @param initialDirectory	Pfad, der anfänglich im Dialog ausgewählt sein soll (kann auch <code>null</code> sein)
	 * @return	Im Erfolgsfall wird der Dateiname zurückgegeben, sonst <code>null</code>
	 */
	public File selectFile(final Component parent, final File initialDirectory) {
		final JFileChooser fc;
		if (initialDirectory!=null) fc=new JFileChooser(initialDirectory.toString()); else {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		}
		fc.setDialogTitle(Language.tr("SoundSelectDialog.Title"));

		final String[] formats=getSupportedFormats();
		final FileFilter all=new FileNameExtensionFilter(Language.tr("SoundSelectDialog.AllSupportedFormats"),formats);
		final FileFilter[] filters=Stream.of(formats).map(format->new FileNameExtensionFilter(format+" "+Language.tr("SoundSelectDialog.Files"),format)).toArray(FileFilter[]::new);

		fc.addChoosableFileFilter(all);
		Arrays.asList(filters).forEach(filter->fc.addChoosableFileFilter(filter));

		fc.setFileFilter(all);
		if (fc.showOpenDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			for (int i=0;i<filters.length;i++) if (filters[i]==fc.getFileFilter()) {
				file=new File(file.getAbsoluteFile()+"."+formats[i]);
				break;
			}
		}
		return file;
	}
}
