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
package ui.script;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import dumonts.hunspell.Hunspell;
import tools.SetupData;
import xml.XMLTools;

/**
 * Diese Klasse kapselt eine einzelne Hunspell-Sprache
 * @author Alexander Herzog
 * @see HunspellDictionaries
 */
public class HunspellDictionaryRecord {
	/**
	 * oxt-Datei, in der sich die Sprachdaten befinden
	 */
	private final File file;

	/**
	 * Relativer Pfad zur dic-Datei innerhalb der oxt-Datei
	 */
	private final String dicRecord;

	/**
	 * Relativer Pfad zur aff-Datei innerhalb der oxt-Datei
	 */
	private final String affRecord;

	/**
	 * Sprache, die dieser Datensatz repräsentiert
	 */
	public final String locale;

	/**
	 * Hunspell-Objekt (wird erst bei Bedarf erzeugt)
	 * @see #getHunspell()
	 */
	private Hunspell hunspell;

	/**
	 * Muss Hunspell global deaktiviert werden?
	 * (Z.B. wegen für die shared library nicht unterstützter CPU-Plattform.)
	 */
	private static boolean hunspellGlobalOff=false;

	static {
		/* Leider unterstützt Hunspell kein Windows on ARM. Daher müssen wir das System in diesem Fall deaktivieren. */
		if ("aarch64".equals(System.getProperty("os.arch")) && File.separatorChar=='\\') hunspellGlobalOff=true;
		/* Abschalten per Setup */
		if (!SetupData.getSetup().allowSpellCheck) hunspellGlobalOff=true;
	}

	/**
	 * Wurde das Rechtschreibprüfungssystem global deaktiviert?
	 * @return	Liefert <code>true</code>, wenn das System global deaktiviert wurde.
	 */
	public static boolean isGlobalOff() {
		return hunspellGlobalOff;
	}

	/**
	 * Konstruktor der Klasse
	 * @param file	oxt-Datei, in der sich die Sprachdaten befinden
	 * @param dicRecord	Relativer Pfad zur dic-Datei innerhalb der oxt-Datei
	 * @param affRecord	Relativer Pfad zur aff-Datei innerhalb der oxt-Datei
	 * @param locale	Sprache, die dieser Datensatz repräsentiert
	 */
	private HunspellDictionaryRecord(final File file, final String dicRecord, final String affRecord, final String locale) {
		this.file=file;
		this.dicRecord=dicRecord;
		this.affRecord=affRecord;
		this.locale=locale;
	}

	/**
	 * Liefert das Hunspell-Objekt zu diesem Sprachdatensatz
	 * @return	Hunspell-Objekt zu diesem Sprachdatensatz oder <code>null</code>, wenn kein Hunspell-Objekt angelegt werden kann
	 */
	public Hunspell getHunspell() {
		if (hunspellGlobalOff) return null;
		if (hunspell!=null) return hunspell;

		File dicFile=null;
		File affFile=null;

		try (ZipInputStream zipStream=new ZipInputStream(new FileInputStream(file))) {
			ZipEntry zipEntry=zipStream.getNextEntry();
			while (zipEntry!=null) {
				if (zipEntry.getName().equals(dicRecord)) {
					dicFile=buildTempFile(zipStream,"dic");
					if (dicFile==null) return null;
				}
				if (zipEntry.getName().equals(affRecord)) {
					affFile=buildTempFile(zipStream,"add");
					if (affFile==null) return null;
				}
				if (dicFile!=null && affFile!=null) break;
				zipEntry=zipStream.getNextEntry();
			}
		} catch (IOException e) {
			return null;
		}

		if (dicFile==null || affFile==null) return null;

		try {
			hunspell=new Hunspell(dicFile.toPath(),affFile.toPath());
			return hunspell;
		} catch (RuntimeException | NoClassDefFoundError | UnsatisfiedLinkError e) {
			return null;
		}
	}

	/**
	 * Kopiert den Inhalt eines Streams in eine temporäre Datei
	 * @param inputStream	Stream dessen Inhalt in die temporäre Datei kopiert werden soll
	 * @param extension	Dateiendung für die temporäre Datei
	 * @return	Temporäre Datei
	 * @see #getHunspell()
	 */
	private File buildTempFile(final InputStream inputStream, final String extension) {
		File tempFile;
		try {
			tempFile=File.createTempFile("QS-Dict-"+locale+"-",extension);
		} catch (IOException e) {
			return null;
		}
		tempFile.deleteOnExit();

		try (FileOutputStream outputStream=new FileOutputStream(tempFile)) {
			byte[] buffer=new byte[128*1024];
			int bytesRead;
			while ((bytesRead=inputStream.read(buffer))>0) {
				outputStream.write(buffer,0,bytesRead);
			}
		} catch (IOException e) {
			return null;
		}
		return tempFile;
	}

	/**
	 * Liste der Zuordnung von alten zu neuen Dateien.
	 */
	private static final Map<String,String> updateMap;

	static {
		updateMap=new HashMap<>();
		updateMap.put("us_english_dictionary-115.0.xpi","us_english_dictionary-140.0.xpi");
		updateMap.put("german_dictionary_de_de_for_sp-20180701.1webext.xpi","dictionary_german-2.1.xpi");
	}

	/**
	 * Prüft, ob eine neuere Version der Wörterbuchdatei vorhanden ist.
	 * @param path	Pfad in dem nach den Sprachdatensätzen (xpi- und oxt-Dateien) gesucht werden soll
	 * @param file	Zu überprüfende Datei
	 * @return	Neuere Version vorhanden? (Diese nicht verwenden)
	 */
	private static boolean isOldVersion(final File path, final File file) {
		final String newFileName=updateMap.get(file.getName());
		if (newFileName!=null) {
			final File newFile=new File(path,newFileName);
			if (newFile.isFile()) return true;
		}
		return false;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Sprachdatensätzen.
	 * @param path	Pfad in dem nach den Sprachdatensätzen (xpi- und oxt-Dateien) gesucht werden soll
	 * @return	Liste mit allen verfügbaren Sprachdatensätzen (kann leer sein, ist aber nie <code>null</code>)
	 */
	public static List<HunspellDictionaryRecord> getAvailableRecords(final File path) {
		final List<HunspellDictionaryRecord> records=new ArrayList<>();
		if (path!=null && path.isDirectory()) {
			final File[] files=path.listFiles();
			if (files!=null) for (File file: files) {
				final String fileName=file.getName().toLowerCase();
				if (isOldVersion(path,file)) continue;
				if (fileName.endsWith(".oxt")) records.addAll(readRecordsFromOXT(file));
				if (fileName.endsWith(".xpi")) records.addAll(readRecordsFromXPI(file));
			}
		}
		return records;
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Sprachdatensätzen in einer oxt-Datei.
	 * @param oxtFile	oxt-Datei die nach Sprachdatensätzen durchsucht werden soll
	 * @return	Liste mit allen in der oxt-Datei verfügbaren Sprachdatensätzen (kann leer sein, ist aber nie <code>null</code>)
	 * @see #getAvailableRecords(File)
	 */
	private static List<HunspellDictionaryRecord> readRecordsFromOXT(final File oxtFile) {
		try (ZipInputStream zipStream=new ZipInputStream(new FileInputStream(oxtFile))) {
			ZipEntry zipEntry=zipStream.getNextEntry();
			while (zipEntry!=null) {
				if (zipEntry.getName().equals("dictionaries.xcu")) return readRecordsFromXCU(oxtFile,zipStream);
				zipEntry=zipStream.getNextEntry();
			}
		} catch (IOException e) {
		}
		return new ArrayList<>();
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Sprachdatensätzen in einer xpi-Datei.
	 * @param xpiFile	xpi-Datei die nach Sprachdatensätzen durchsucht werden soll
	 * @return	Liste mit allen in der xpi-Datei verfügbaren Sprachdatensätzen (kann leer sein, ist aber nie <code>null</code>)
	 * @see #getAvailableRecords(File)
	 */
	private static List<HunspellDictionaryRecord> readRecordsFromXPI(final File xpiFile) {
		try (ZipInputStream zipStream=new ZipInputStream(new FileInputStream(xpiFile))) {
			ZipEntry zipEntry=zipStream.getNextEntry();
			while (zipEntry!=null) {
				if (zipEntry.getName().equals("manifest.json")) return readRecordsFromJSON(xpiFile,zipStream);
				zipEntry=zipStream.getNextEntry();
			}
		} catch (IOException e) {
		}
		return new ArrayList<>();
	}

	/**
	 * Ließt eine Liste mit allen verfügbaren Sprachdatensätzen aus einer xcu-Datei aus einer oxt-Datei aus.
	 * @param oxtFile	oxt-Datei in der sich die xcu-Datei befindet
	 * @param xcuStream	Eingabestream der zu lesenden xcu-Datei
	 * @return	Liste mit allen in der oxt-Datei verfügbaren Sprachdatensätzen (kann leer sein, ist aber nie <code>null</code>)
	 * @see #readRecordsFromOXT(File)
	 */
	private static List<HunspellDictionaryRecord> readRecordsFromXCU(final File oxtFile, final InputStream xcuStream) {
		final List<HunspellDictionaryRecord> records=new ArrayList<>();

		final XMLTools xml=new XMLTools(xcuStream,XMLTools.FileType.XML);
		final Element root=xml.load();
		final NodeList nodes1=root.getChildNodes();
		for (int i=0;i<nodes1.getLength();i++) if (nodes1.item(i) instanceof Element) {
			final Element element1=(Element)nodes1.item(i);
			if (!element1.getNodeName().equals("node")) continue;
			if (element1.getAttribute("oor:name").equals("ServiceManager")) {
				final NodeList nodes2=element1.getChildNodes();
				for (int j=0;j<nodes2.getLength();j++) if (nodes2.item(j) instanceof Element) {
					final Element element2=(Element)nodes2.item(j);
					if (!element2.getNodeName().equals("node")) continue;
					if (element2.getAttribute("oor:name").equals("Dictionaries")) {
						final NodeList nodes3=element2.getChildNodes();
						for (int k=0;k<nodes3.getLength();k++) if (nodes3.item(k) instanceof Element) {
							final Element element3=(Element)nodes3.item(k);
							if (!element3.getNodeName().equals("node")) continue;
							final HunspellDictionaryRecord record=readRecordFromNode(oxtFile,element3);
							if (record!=null) records.add(record);
						}
					}
				}
			}
		}
		return records;
	}

	/**
	 * Interpretiert einen einzelnen Datensatz in einer xcu-Datei in einer oxt-Datei
	 * @param oxtFile	oxt-Datei in der sich die xcu-Datei befindet
	 * @param node	Zu lesender Datensatz
	 * @return	Liefert im Erfolgsfall einen Sprachdatensatz, sonst <code>null</code>
	 * @see #readRecordsFromXCU(File, InputStream)
	 */
	private static HunspellDictionaryRecord readRecordFromNode(final File oxtFile, final Element node) {
		String locations=null;
		String format=null;
		String locale=null;

		final NodeList properties=node.getChildNodes();
		for (int i=0;i<properties.getLength();i++) if (properties.item(i) instanceof Element) {
			final Element property=(Element)properties.item(i);
			if (!property.getNodeName().equals("prop")) continue;
			final String name=property.getAttribute("oor:name");
			final NodeList values=property.getChildNodes();
			for (int j=0;j<values.getLength();j++) if (values.item(j) instanceof Element) {
				final Element value=(Element)values.item(j);
				if (!value.getNodeName().equals("value")) continue;
				final String content=value.getTextContent();

				if (name.equals("Locations")) locations=content;
				if (name.equals("Format")) format=content;
				if (name.equals("Locales")) locale=content;

				break;
			}
		}

		if (locations==null || format==null || locale==null) return null;
		if (!format.equals("DICT_SPELL")) return null;

		final String[] files=locations.split(" ");
		if (files.length!=2) return null;

		files[0]=files[0].replace("%origin%/","");
		files[1]=files[1].replace("%origin%/","");
		if (files[0].endsWith(".aff")) {
			if (!files[1].endsWith(".dic")) return null;
			return new HunspellDictionaryRecord(oxtFile,files[1],files[0],locale);
		} else {
			if (!files[0].endsWith(".dic")) return null;
			if (!files[1].endsWith(".add")) return null;
			return new HunspellDictionaryRecord(oxtFile,files[0],files[1],locale);
		}
	}

	/**
	 * Ließt eine Liste mit allen verfügbaren Sprachdatensätzen aus einer json-Datei aus einer xpi-Datei aus.
	 * @param xpiFile	xpi-Datei in der sich die json-Datei befindet
	 * @param jsonStream	Eingabestream der zu lesenden json-Datei
	 * @return	Liste mit allen in der json-Datei verfügbaren Sprachdatensätzen (kann leer sein, ist aber nie <code>null</code>)
	 * @see #readRecordsFromXPI(File)
	 */
	private static List<HunspellDictionaryRecord> readRecordsFromJSON(final File xpiFile, final InputStream jsonStream) {
		final List<HunspellDictionaryRecord> records=new ArrayList<>();

		String text=new BufferedReader(new InputStreamReader(jsonStream,StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
		text="{\"manifest\": "+text+"}";
		final ByteArrayInputStream newJsonStream=new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

		final XMLTools xml=new XMLTools(newJsonStream,XMLTools.FileType.JSON);
		final Element root=xml.load();
		final NodeList nodes1=root.getChildNodes();
		for (int i=0;i<nodes1.getLength();i++) if (nodes1.item(i) instanceof Element) {
			final Element element1=(Element)nodes1.item(i);
			if (!element1.getNodeName().equals("dictionaries")) continue;
			final NamedNodeMap attributes=element1.getAttributes();
			for (int j=0;j<attributes.getLength();j++) {
				final String locale=attributes.item(j).getNodeName();
				final String dicFile=element1.getAttribute(locale);
				records.add(new HunspellDictionaryRecord(xpiFile,dicFile,dicFile.replace(".dic",".aff"),locale));
			}
		}

		return records;
	}
}
