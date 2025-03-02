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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.NumberTools;
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
		systemSoundsSet=new HashSet<>(Arrays.asList(getSystemSounds()));
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
			if (toolkit!=null) {
				list.add(BEEP_SOUND);
				final String propNames[]=(String[])toolkit.getDesktopProperty("win.propNames");
				if (propNames!=null) Arrays.asList(propNames).stream().filter(propName->propName.startsWith(WIN_SOUND_PREFIX)).map(propName->propName.substring(WIN_SOUND_PREFIX.length())).forEach(list::add);
			}
			systemSounds=list.toArray(String[]::new);
		}
		return systemSounds;
	}

	/**
	 * Spielt einen System-Sound ab.
	 * @param name	Name des abzuspielenden System-Sounds. Wird ein ungültiger Name oder <code>null</code> übergeben, so wird der Standard-Piepton abgespielt.
	 */
	public void playSystemSound(final String name) {
		if (toolkit==null) return;

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
			if (executor==null) {
				final int coreCount=Runtime.getRuntime().availableProcessors();
				executor=new ThreadPoolExecutor(coreCount,coreCount,2,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),(ThreadFactory)r->new Thread(r,"SoundClipCloser"));
				((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);
			}
			final Clip clipToClose=lastClip;
			executor.execute(()->clipToClose.close());
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
	 * Executor-Pool zum asynchronen Schließen der einzelnen Clips
	 * @see #playSoundFile(File, int)
	 */
	private ExecutorService executor;

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
			if (executor==null) {
				final int coreCount=Runtime.getRuntime().availableProcessors();
				executor=new ThreadPoolExecutor(coreCount,coreCount,2,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),(ThreadFactory)r->new Thread(r,"SoundClipCloser"));
				((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);
			}
			final Clip clipToClose=lastClip;
			executor.execute(()->clipToClose.close());
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
	 * In {@link #createSinWaveBuffer(double, int)} zu verwendende Sample-Rate.
	 * @see #createSinWaveBuffer(double, int)
	 */
	private static final int SAMPLE_RATE=22_050;

	/**
	 * Ausgabeformat für {@link #playSoundFrequency(double, int)}
	 * @see #playSoundFrequency(double, int)
	 */
	private static AudioFormat frequencyAudioFormat=new AudioFormat(SAMPLE_RATE,8,1,true,true);

	/**
	 * Erzeugt einen Sound-Puffer mit einer vorgegebenen Frequenz.
	 * @param frequency	Frequenz des Tons
	 * @param durationMS	Dauer in Millisekunden
	 * @return	Sound-Puffer mit Sample-Rate {@link #SAMPLE_RATE}
	 */
	private static byte[] createSinWaveBuffer(final double frequency, final int durationMS) {
		final int samples=durationMS*SAMPLE_RATE/1000;
		final byte[] output=new byte[samples];
		final double period=SAMPLE_RATE/frequency;

		final int len=output.length;
		for (int i=0;i<len;i++) {
			final double angle=2.0*Math.PI*i/period;
			double scale=1;
			if (len>1000) {
				if (i<500) scale=i/500.0;
				if (i>=len-500) scale=(len-1-i)/500.0;
			}
			output[i]=(byte)(Math.sin(angle)*scale*127f);
		}

		return output;
	}

	/**
	 * Verwendete Frequenz beim letzten Aufruf von {@link #playSoundFrequency(double, int)}
	 * @see #playSoundFrequency(double, int)
	 */
	private double lastFrequency=0;

	/**
	 * Verwendete Dauer beim letzten Aufruf von {@link #playSoundFrequency(double, int)}
	 * @see #playSoundFrequency(double, int)
	 */
	private int lastDurationMS=0;

	/**
	 * Verwendeter Sound-Puffer beim letzten Aufruf von {@link #playSoundFrequency(double, int)}
	 * @see #playSoundFrequency(double, int)
	 */
	private byte[] lastBuffer;

	/**
	 * Spielt einen Ton mit einer vorgegebenen Frequenz ab.
	 * @param frequency	Frequenz des Tons
	 * @param durationMS	Dauer in Millisekunden
	 * @return	Liefert <code>true</code>, wenn die Ausgabe erfolgreich war
	 */
	public synchronized boolean playSoundFrequency(final double frequency, final int durationMS) {
		if (frequency!=lastFrequency || durationMS!=lastDurationMS) {
			lastBuffer=createSinWaveBuffer(frequency,durationMS);
			lastFrequency=frequency;
			lastDurationMS=durationMS;
		}

		try(SourceDataLine line=AudioSystem.getSourceDataLine(frequencyAudioFormat)) {
			line.open(frequencyAudioFormat,SAMPLE_RATE);
			line.start();
			line.write(lastBuffer,0,lastBuffer.length);
			line.drain();
			return true;

		} catch (LineUnavailableException e) {
			return false;
		}
	}

	/**
	 * Spielt einen Ton mit einer vorgegebenen Frequenz für die Dauer von 0,5 Sekunden ab.
	 * @param frequency	Frequenz des Tons
	 * @return	Liefert <code>true</code>, wenn die Ausgabe erfolgreich war
	 */
	public synchronized boolean playSoundFrequency(final double frequency) {
		return playSoundFrequency(frequency,defaultToneDurationMS);
	}

	/**
	 * Wandelt den ersten Buchstaben einer Zeichenkette in einen Großbuchstaben um.
	 * @param str	Umzuwandelnde Zeichenkette
	 * @return	Neue Zeichenkette mit Großbuchstaben am Anfang
	 */
	private static String upperCaseFirst(final String str) {
		if (str.length()==1) return str.toUpperCase();
		return str.substring(0,1).toUpperCase()+str.substring(1);
	}

	/**
	 * Rundet eine Zahl auf zwei Nachkommastellen.
	 * @param d	Zu rundende Zahl
	 * @return	Zahl auf zwei Nachkommastellen gerundet
	 */
	private static double truncateFrequency(final double d) {
		return Math.round(d*100.0)/100.0;
	}

	/**
	 * Standard-Tonlänge für über eine feste Frequenz definierte Tonausgaben
	 * @see #playSoundFrequency(double)
	 */
	private static final int defaultToneDurationMS=500;

	/**
	 * Namen der Töne einer Oktave in Kleinbuchstaben
	 */
	private static final String[] toneNameList=new String[] {"c", "des", "d", "es", "e", "f", "ges", "g", "as", "a", "b", "h"};

	/**
	 * Liste mit offiziellen Bezeichnungen von Tonnamen
	 * @see #fullToneFrequencyList
	 */
	public static final List<String> fullToneNameList=new ArrayList<>();

	/**
	 * Hash-Set der offiziellen Bezeichnungen von Tonnamen
	 * @see #fullToneNameList
	 * @see #playAll(String, int)
	 */
	public static final Set<String> fullToneNameSet=new HashSet<>();

	/**
	 * Liste mit den Frequenzen der Töne aus {@link #fullToneNameList}
	 * @see #fullToneNameList
	 */
	public static final List<Double> fullToneFrequencyList=new ArrayList<>();

	static {
		/* a' -> c': 1/2^9/12 */
		final double baseFreq=440.0/Math.pow(2,9.0/12.0);
		final double factor=Math.pow(2,1.0/12.0);

		double freq;

		/* c' -> ''C: 1/2^4 */
		freq=baseFreq/Math.pow(2,4);
		for (String tone: toneNameList) {
			fullToneNameList.add("''"+upperCaseFirst(tone));
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> 'C: 1/2^3 */
		freq=baseFreq/Math.pow(2,3);
		for (String tone: toneNameList) {
			fullToneNameList.add("'"+upperCaseFirst(tone));
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> C: 1/2^2 */
		freq=baseFreq/Math.pow(2,2);
		for (String tone: toneNameList) {
			fullToneNameList.add(upperCaseFirst(tone));
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> c: 1/2 */
		freq=baseFreq/2;
		for (String tone: toneNameList) {
			fullToneNameList.add(tone);
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> c': 1 */
		freq=baseFreq;
		for (String tone: toneNameList) {
			fullToneNameList.add(tone+"'");
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> c'': 2 */
		freq=baseFreq*2;
		for (String tone: toneNameList) {
			fullToneNameList.add(tone+"''");
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> c''': 2^2 */
		freq=baseFreq*Math.pow(2,2);
		for (String tone: toneNameList) {
			fullToneNameList.add(tone+"'''");
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		/* c' -> c'''': 2^3 */
		freq=baseFreq*Math.pow(2,3);
		for (String tone: toneNameList) {
			fullToneNameList.add(tone+"''''");
			fullToneFrequencyList.add(truncateFrequency(freq));
			freq*=factor;
		}

		fullToneNameSet.addAll(fullToneNameList);
	}

	/**
	 * Hash-Set der Namen der System-Sounds
	 */
	private final Set<String> systemSoundsSet;

	/**
	 * Spielt einen System-Sound oder eine Sound-Datei ab.
	 * @param sound	Bezeichner für den System-Sound oder Dateiname der Sound-Datei
	 * @param maxSeconds	Bei Sound-Dateien maximal abzuspielende Sekunden (Werte &le;0 für keine Begrenzung)
	 * @return	Liefert <code>true</code>, wenn der Sound erfolgreich abgespielt werden konnte
	 */
	public boolean playAll(final String sound, final int maxSeconds) {
		if (systemSoundsSet.contains(sound)) {
			playSystemSound(sound);
			return true;
		}

		if (fullToneNameSet.contains(sound)) {
			final int index=fullToneNameList.indexOf(sound);
			if (index>=0) {
				playSoundFrequency(fullToneFrequencyList.get(index));
				return true;
			}
		}

		final Double frequency=NumberTools.getPositiveDouble(sound);
		if (frequency!=null) {
			playSoundFrequency(frequency);
			return true;
		}

		return playSoundFile(sound,maxSeconds);
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
