/**
 * Copyright 2023 Alexander Herzog
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
package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Die statische Methode {@link #work()} dieser Klasse erstellt, wenn im class-File-Modus
 * ausgeführt, eine Liste mit SHA-256-Prüfsummen aller eingebundenen jar-Bibliotheken und
 * speichert diese. Wird das Programm aus einer jar-Datei heraus gestartet, wird diese Liste
 * ausgewertet und mit den Prüfsummen der jar-Bibliotheken verglichen. Bei einer Abweichung
 * wird das gesamte Programm sofort beendet.
 */
public class JarSecurity {
	/**
	 * Dateiname der Listendatei, in der die Hash-Werte gespeichert werden sollen
	 */
	private static final String HASHES_FILE_NAME=JarSecurity.class.getSimpleName()+".txt";

	/**
	 * SHA-256-Algorithmus
	 */
	private static final MessageDigest algorithm;

	/**
	 * Läuft das Programm aus einer jar-Datei heraus?
	 */
	private static final boolean jarFileMode=new File(SetupData.getProgramFolder(),"libs").isDirectory();

	/**
	 * Zu ignorierende Bibliotheken im "libs"-Ordner. Alle anderen werden in die Erfassung einbezogen.
	 */
	private static final Set<String> ignoreJars=Set.of("simsystem.jar", "simtools.jar","simulator.jar");

	/**
	 * Hauptdateien (zu prüfen über {@link #selfJarHashFile}
	 */
	private static List<String> selfJars=List.of("libs"+File.separator+"simsystem.jar","libs"+File.separator+"simtools.jar","Simulator.jar");

	/**
	 * Datei im libs-Ordner aus der die Prüfsumme für die {@link #selfJars} geladen werden sollen
	 */
	private static String selfJarHashFile="simulator.jar";

	/**
	 * Im Falle des class-File-Modus: Die Listendatei, in die die Hashes geschrieben werden sollen (sonst: <code>null</code>)
	 */
	private static final File hashesFile;

	/**
	 * Im Falle des jar-Modus: URL zur Listendatei, aus der die Hashes geladen werden sollen (sonst oder wenn nicht vorhanden: <code>null</code>)
	 */
	private static final URL hashesURL;

	static {
		/* Algorithmus vorbereiten */
		MessageDigest algorithmLocal;
		algorithmLocal=null;
		try {
			algorithmLocal=MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			algorithmLocal=null;
		}
		algorithm=algorithmLocal;

		/* Pfad zu Hashes-Datei bestimmen */
		if (jarFileMode) {
			hashesFile=null;
			hashesURL=JarSecurity.class.getResource(HASHES_FILE_NAME);
		} else {
			final URL url=JarSecurity.class.getResource(".");
			if (url!=null && !url.getFile().isEmpty()) {
				hashesFile=new File(url.getFile(),"../../../src/main/java/tools/"+HASHES_FILE_NAME);
			} else {
				hashesFile=null;
			}
			hashesURL=null;
		}
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link #work()} zur Verfügung.
	 */
	private JarSecurity() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Erstellt eine Liste aller zu prüfenden jar-Dateien.
	 * @return	Liste aller zu prüfenden jar-Dateien oder <code>null</code>, wenn keine Liste erstellt werden konnte
	 */
	private static File[] getLibs() {
		final File mainFolder=SetupData.getProgramFolder();
		final File libFolder=new File(mainFolder,jarFileMode?"libs":("target"+File.separator+"libs"));
		final File[] list=libFolder.listFiles();
		if (list==null) return null;
		return Stream.of(list).filter(file->file.toString().endsWith(".jar")).filter(file->!ignoreJars.contains(file.getName())).toArray(File[]::new);
	}

	/**
	 * Berechnet eine SHA-256-Prüfsumme für eine einzelne Datei.
	 * @param file	Datei für die die Prüfsumme berechnet werden soll
	 * @return	SHA-256-Prüfsumme
	 */
	private static String calcSHA256(final File file) {
		if (file==null || algorithm==null) return null;
		try {
			final byte[] data=Files.readAllBytes(file.toPath());
			final byte[] hash=algorithm.digest(data);
			String hashStr=new BigInteger(1,hash).toString(16);
			while (hashStr.length()<64) hashStr="0"+hashStr;
			return hashStr;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Liefert eine Zuordnung von jar-Bibliotheken zu Prüfsummen, wobei die Prüfsummen von den Dateien erhoben werden.
	 * @return	Zuordnung von jar-Bibliotheken zu tatsächlichen Prüfsummen
	 */
	private static Map<String,String> getActualHashes() {
		final Map<String,String> result=new HashMap<>();
		final File[] files=getLibs();
		if (files!=null) Stream.of(files).forEach(file->result.put(file.getName(),calcSHA256(file)));
		return result;
	}

	/**
	 * Liefert eine Zuordnung von jar-Bibliotheken zu Prüfsummen, wobei die Prüfsummen aus der Listendatei geladen werden.
	 * @return	Zuordnung von jar-Bibliotheken zu tatsächlichen Prüfsummen oder <code>null</code>, wenn die Listendatei nicht geladen werden konnte
	 */
	private static Map<String,String> loadHashes() {
		if (hashesFile!=null) {
			try {
				final Map<String,String> result=new HashMap<>();
				Files.readAllLines(hashesFile.toPath()).forEach(line->{
					final String[] parts=line.split("\\t");
					if (parts.length==2) result.put(parts[0],parts[1]);
				});
				return result;
			} catch (IOException e) {
				return null;
			}
		}

		if (hashesURL!=null) {
			try (InputStream stream=JarSecurity.class.getResourceAsStream(HASHES_FILE_NAME)) {
				final Map<String,String> result=new HashMap<>();
				new BufferedReader(new InputStreamReader(stream)).lines().forEach(line->{
					final String[] parts=line.split("\\t");
					if (parts.length==2) result.put(parts[0],parts[1]);
				});
				return result;
			} catch (IOException e) {
				return null;
			}
		}

		return null;
	}

	/**
	 * Speichert eine Zuordnung von jar-Bibliotheken zu Prüfsummen in einer Listendatei
	 * @param hashes	Zuordnung von jar-Bibliotheken zu Prüfsummen
	 */
	private static void saveHashes(final Map<String,String> hashes) {
		if (hashesFile==null) return;

		final StringBuilder output=new StringBuilder();
		hashes.forEach((key,value)->output.append(key+"\t"+value+"\n"));
		try {
			Files.writeString(hashesFile.toPath(),output.toString(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Prüft die Haupt-jar-Datei des Programms.
	 * @return	Liefert <code>true</code>, wenn die Prüfung erfolgreich war
	 */
	private static boolean selfTest() {
		final File libsFolder=new File(SetupData.getProgramFolder(),"libs");
		if (!libsFolder.isDirectory()) return true;
		final File hashFile=new File(libsFolder,selfJarHashFile);
		if (!hashFile.isFile()) return true;

		final List<String> lines;
		try {
			lines=Files.readAllLines(hashFile.toPath());
		} catch (IOException e) {
			return true;
		}
		if (lines.size()==0) return false;

		int index=0;
		for (var selfJar: selfJars) {
			final File selfJarFile=new File(SetupData.getProgramFolder(),selfJar);
			if (!selfJarFile.isFile()) return true;
			final String selfHash=calcSHA256(selfJarFile);
			if (selfHash==null) return true;

			if (lines.size()<=index) return false;
			final String loadedHash=lines.get(index).trim();
			index++;

			/*
			System.out.println(selfJarFile.toString());
			System.out.println("calc: "+selfHash);
			System.out.println("list: "+loadedHash);
			 */

			if (!selfHash.equals(loadedHash)) return false;
		}

		return true;
	}

	/**
	 * Erstellt oder prüft je nach Anwendungsfall die Liste der Prüfsummen der jar-Bibliotheken.
	 */
	public static void work() {
		new Thread(()->{
			/* Selbsttest */
			if (jarFileMode) {
				if (!selfTest()) {
					System.out.println("Damaged main jar (checksum mismatch).");
					System.exit(1);
				}
			}

			/* Bibliotheken prüfen */
			final Map<String,String> actualHashes=getActualHashes();
			final Map<String,String> loadedHashes=loadHashes();
			final boolean ok=Objects.deepEquals(actualHashes,loadedHashes);

			if (jarFileMode) {
				if (loadedHashes!=null && !ok) {
					System.out.println("Damaged jar libraries (checksum mismatch).");
					System.exit(1);
				}
			} else {
				if (!ok) {
					saveHashes(actualHashes);
					System.out.println("Hashes have changed. Security file was updated.");
				}
			}
		},"Library validation check").start();
	}
}
