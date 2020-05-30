package xml;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import mathtools.distribution.swing.CommonVariables;

/**
 * Diese Klasse enthält Funktionen zum Laden und Speichern von Daten aus bzw. in XML-Dateien
 * @author Alexander Herzog
 * @version 1.9
 */
public final class XMLTools {
	/**
	 * Basis-URL zur Generierung eines Namespace
	 */
	public static String homeURL="www.mathematik.tu-clausthal.de";

	/**
	 * URL unter der die dtd- und die xsd-Datei zu finden sind
	 */
	public static String mediaURL="https://"+homeURL+"/";

	/**
	 * Name der dtd-Datei
	 * @see #mediaURL
	 */
	public static String dtd="Simulator.dtd";

	/**
	 * Name der xsd-Datei
	 * @see #mediaURL
	 */
	public static String xsd="Simulator.xsd";

	/** Fehlermeldung "Der XML-Interpreter konnte nicht initalisiert werden." */
	public static String errorInitXMLInterpreter="Der XML-Interpreter konnte nicht initalisiert werden.";
	/** Fehlermeldung "Der XML-Interpreter konnte die Datei nicht verarbeiten." */
	public static String errorXMLProcess="Der XML-Interpreter konnte die Datei nicht verarbeiten.";
	/** Fehlermeldung "Es ist ein Fehler beim Verarbeiten der Datei\n%s\naufgetreten." */
	public static String errorXMLProcessFile="Es ist ein Fehler beim Verarbeiten der Datei\n%s\naufgetreten.";
	/** Fehlermeldung "Interner Fehler: Es wurde kein Eingabeobjekt angegeben." */
	public static String errorInternalErrorNoInputObject="Interner Fehler: Es wurde kein Eingabeobjekt angegeben.";
	/** Fehlermeldung "Interner Fehler: Es wurde kein Ausgabeobjekt angegeben." */
	public static String errorInternalErrorNoOutputObject="Interner Fehler: Es wurde kein Ausgabeobjekt angegeben.";
	/** Fehlermeldung "Es ist ein Fehler beim Anlegen einer Datei innerhalb des Zip-Datenstroms aufgetreten." */
	public static String errorZipCreating="Es ist ein Fehler beim Anlegen einer Datei innerhalb des Zip-Datenstroms aufgetreten.";
	/** Fehlermeldung "Es ist ein Fehler beim Anlegen einer Datei innerhalb der Zip-Datei\n%s\naufgetreten." */
	public static String errorZipCreatingFile="Es ist ein Fehler beim Anlegen einer Datei innerhalb der Zip-Datei\n%s\naufgetreten.";
	/** Fehlermeldung "Es ist ein Fehler beim Schließen des Datenstroms aufgetreten." */
	public static String errorZipClosing="Es ist ein Fehler beim Schließen des Datenstroms aufgetreten.";
	/** Fehlermeldung "Es ist ein Fehler beim Öffnen der Datei\n%s\naufgetreten." */
	public static String errorOpeningFile="Es ist ein Fehler beim Öffnen der Datei\n%s\naufgetreten.";
	/** Fehlermeldung "Es ist ein Fehler beim Schließen der Datei\n%s\naufgetreten." */
	public static String errorClosingFile="Es ist ein Fehler beim Schließen der Datei\n%s\naufgetreten.";
	/** Fehlermeldung "Der Vorgang wurde vom Nutzer abgebrochen." */
	public static String errorCanceledByUser="Der Vorgang wurde vom Nutzer abgebrochen.";
	/** Fehlermeldung "Es ist ein Fehler beim Verschlüsseln der Datei\n%s\naufgetreten." */
	public static String errorEncryptingFile="Es ist ein Fehler beim Verschlüsseln der Datei\n%s\naufgetreten.";
	/** Fehlermeldung "Es ist ein Fehler beim Entschlüsseln der Datei\n%s\naufgetreten." */
	public static String errorDecryptingFile="Es ist ein Fehler beim Entschlüsseln der Datei\n%s\naufgetreten.";
	/** Fehlermeldung "Die angegebene Datei\n%s\nexistiert nicht." */
	public static String errorFileDoesNotExists="Die angegebene Datei\n%s\nexistiert nicht.";
	/** Fehlermeldung "Es sind keine eingebetteten xml-Daten vorhanden." */
	public static String errorNoEmbeddedXMLData="Es sind keine eingebetteten xml-Daten vorhanden.";
	/** Fehlermeldung "Es ist ein Fehler beim Verarbeiten des Datenstroms aufgetreten." */
	public static String errorStreamProcessing="Es ist ein Fehler beim Verarbeiten des Datenstroms aufgetreten.";
	/** Fehlermeldung "Es ist ein Fehler beim Schließen des Datenstroms aufgetreten." */
	public static String errorStreamClosing="Es ist ein Fehler beim Schließen des Datenstroms aufgetreten.";

	/** Dialog-Meldungstext "Bitte Passwort eingeben:" */
	public static String enterPassword="Bitte Passwort eingeben:";

	/** Bezeichner für Dateiformat xml (im Dateiauswahldialog) */
	public static String fileTypeXML="xml-Dateien";
	/** Bezeichner für Dateiformat compressed xml (im Dateiauswahldialog) */
	public static String fileTypeCompressedXML="Komprimierte xml-Dateien";
	/** Bezeichner für Dateiformat tar.gz (im Dateiauswahldialog) */
	public static String fileTypeTARCompressedXML="GZip-komprimierte tar-Dateien";
	/** Bezeichner für Dateiformat json (im Dateiauswahldialog) */
	public static String fileTypeJSON="json-Dateien";
	/** Bezeichner für Dateiformat encrypted xml (im Dateiauswahldialog) */
	public static String fileTypeEncryptedXML="Verschlüsselte xml-Dateien";
	/** Bezeichner für Dateiformat "Alle unterstützen Dateiformate" (im Dateiauswahldialog) */
	public static String fileTypeAll="Alle unterstützen Dateiformate";

	private String lastError=null;

	private final File file;
	private final FileType fileType;
	private final String inputText;
	private final InputStream input;
	private final OutputStream output;

	/**
	 * Dateiformat
	 * @author Alexander Herzog
	 */
	public enum FileType {
		/**
		 * Dateiformat aus Dateiendung ableiten
		 */
		AUTO,

		/**
		 * XML-Format
		 */
		XML,

		/**
		 * JSON-Format
		 */
		JSON,

		/**
		 * In eine zip-Datei eingepackte xml-Datei
		 */
		ZIP_XML,

		/**
		 * In eine tar.gz-Datei eingepackte xml-Datei
		 */
		TAR_XML,

		/**
		 * In eine zip-Datei verschlüsselt eingepackte xml-Datei
		 */
		CRYPT_XML
	}

	/**
	 * Standardmäßig im Speichern-Dateiauswahl-Dialog zu wählendes Dateiformat
	 * @author Alexander Herzog
	 * @see XMLTools#showSaveDialog(Container, String)
	 * @see XMLTools#showSaveDialog(Container, String, File)
	 * @see XMLTools#showSaveDialog(Container, String, File, String[], String[])
	 */
	public enum DefaultSaveFormat {
		/**
		 * XML-Format
		 */
		XML("xml",FileType.XML),

		/**
		 * JSON-Format
		 */
		JSON("json",FileType.JSON),

		/**
		 * In eine zip-Datei eingepackte xml-Datei
		 */
		ZIP_XML("zip",FileType.ZIP_XML),

		/**
		 * In eine tar.gz-Datei eingepackte xml-Datei
		 */
		TAR_XML("tar",FileType.TAR_XML),

		/**
		 * In eine zip-Datei verschlüsselt eingepackte xml-Datei
		 */
		CRYPT_XML("crypt",FileType.CRYPT_XML);

		/**
		 * Bezeichner für das Format (nicht notwendig die Dateiendung)
		 * zur Identifikation z.B. beim Speichern von Setup-Einstellungen.
		 */
		public final String identifier;

		/**
		 * Zugehöriger Dateityp
		 * @see FileType
		 */
		public final FileType fileType;

		DefaultSaveFormat(final String identifier, FileType fileType) {
			this.identifier=identifier;
			this.fileType=fileType;
		}

		/**
		 * Versucht basierend auf einem Identifikations-String ein passendes
		 * {@link DefaultSaveFormat}-Objekt zu ermitteln.
		 * @param identifier	Identifikations-String zu dem ein {@link DefaultSaveFormat}-Objekt ermittelt werden soll (darf <code>null</code> oder leer sein)
		 * @return	Passendes Objekt oder <code>null</code>, wenn kein passendes {@link DefaultSaveFormat}-Objekt existiert
		 */
		public static DefaultSaveFormat getFormat(final String identifier) {
			if (identifier==null || identifier.trim().isEmpty()) return null;
			for (DefaultSaveFormat format: values()) if (format.identifier.equalsIgnoreCase(identifier)) return format;
			return null;
		}
	}

	/**
	 * Konstruktor; legt keine Ein- oder Ausgabemethode fest.
	 * In diesem Fall kann das Objekt nur für <code>generateRoot</code>-Aufrufe verwendet werden.
	 */
	public XMLTools() {
		file=null;
		fileType=FileType.AUTO;
		inputText=null;
		input=null;
		output=null;
	}

	/**
	 * Konstruktor; legt fest, dass die Ein- oder Ausgabe in eine Datei erfolgt.<br>
	 * Das Format wird dabei aus dem Dateinamen abgeleitet.
	 * @param file	Datei in die die XML-Daten geschrieben werden sollen bzw. aus der die XML-Daten geladen werden sollen
	 */
	public XMLTools(final File file) {
		this.file=file;
		fileType=FileType.AUTO;
		inputText=null;
		input=null;
		output=null;
	}

	/**
	 * Konstruktor; legt fest, dass die Ein- oder Ausgabe in eine Datei erfolgt
	 * @param file	Datei in die die XML-Daten geschrieben werden sollen bzw. aus der die XML-Daten geladen werden sollen
	 * @param fileType	Format in dem die Daten gespeichert werden sollen.
	 * @see FileType
	 */
	public XMLTools(final File file, final FileType fileType) {
		this.file=file;
		this.fileType=(fileType!=null)?fileType:FileType.AUTO;
		inputText=null;
		input=null;
		output=null;
	}

	/**
	 * Konstruktor; legt fest, dass Daten aus einem Stream geladen werden sollen
	 * @param input	Zu verwendender InputStream
	 */
	public XMLTools(final InputStream input) {
		file=null;
		fileType=FileType.XML;
		inputText=null;
		this.input=input;
		output=null;
	}

	/**
	 * Konstruktor; legt fest, dass Daten aus einem Stream geladen werden sollen
	 * @param input	Zu verwendender InputStream
	 * @param fileType	Dateiformat
	 */
	public XMLTools(final InputStream input, final FileType fileType) {
		file=null;
		this.fileType=(fileType!=null)?fileType:FileType.AUTO;
		inputText=null;
		this.input=input;
		output=null;
	}

	/**
	 * Konstruktor; legt fest, dass Daten aus einem String geladen werden sollen
	 * @param text	Zu verwendender String
	 */
	public XMLTools(final String text) {
		file=null;
		fileType=FileType.AUTO;
		inputText=text;
		input=null;
		output=null;
	}

	/**
	 * Konstruktor; legt fest, dass Daten in einen Stream geschrieben werden sollen
	 * @param output	Zu verwendender OutputStream
	 */
	public XMLTools(final OutputStream output) {
		this(output,FileType.XML);
	}

	/**
	 * Konstruktor; legt fest, dass Daten in einen Stream geschrieben werden sollen
	 * @param output	Zu verwendender OutputStream
	 * @param fileType	Dateiformat
	 */
	public XMLTools(final OutputStream output, final FileType fileType) {
		file=null;
		this.fileType=fileType;
		inputText=null;
		input=null;
		this.output=output;
	}

	/**
	 * Liefert zurück, ob ein Fehler bei der bisherigen Verarbeitung aufgetreten ist.
	 * @return	Liefert im Falle eines Fehlers die Beschreibung als String. Ist kein Fehler aufgetreten, so wird <code>null</code> zurück geliefert.
	 */
	public String getError() {
		return lastError;
	}

	/**
	 * Legt ein XML-Root-Element zur Speicherung von Daten an
	 * @param name	Name des Root-Elements
	 * @param plain	Gibt kein DOCTYPE aus
	 * @return	XML-Root-Element mit dem angegebenen Namen oder <code>null</code> im Fehlerfall
	 */
	public Element generateRoot(final String name, final boolean plain) {
		if (name==null || name.trim().isEmpty()) return null;

		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {db=dbf.newDocumentBuilder();} catch (ParserConfigurationException e) {lastError=errorInitXMLInterpreter; return null;}
		Document doc=db.newDocument();

		Element root=null;
		try {
			root=doc.createElement(name);
		} catch (Exception e) {return null;}
		if (root==null) return null;
		doc.appendChild(root);
		if (!plain) {
			root.setAttribute("xmlns","https://"+homeURL);
			root.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
			if (xsd!=null && !xsd.isEmpty()) root.setAttribute("xsi:schemaLocation","https://"+homeURL+" "+mediaURL+xsd);
		}

		return root;
	}

	/**
	 * Legt ein XML-Root-Element zur Speicherung von Daten an
	 * @param name	Name des Root-Elements
	 * @return	XML-Root-Element mit dem angegebenen Namen oder <code>null</code> im Fehlerfall
	 */
	public Element generateRoot(final String name) {
		return generateRoot(name,false);
	}

	/**
	 * Speichert die angegebenen XML-Daten abhängig vom verwendeten Konstruktor in einer Datei oder einem Stream
	 * @param root	XML-Root-Element, welches die zu speichernden Daten enthält
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean save(final Element root) {
		return save(root,false);
	}

	/**
	 * Speichert die angegebenen XML-Daten abhängig vom verwendeten Konstruktor in einer Datei oder einem Stream
	 * @param doc	XML-Document, welches die zu speichernden Daten enthält
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean save(final Document doc) {
		if (doc==null) return false;
		return save(doc.getDocumentElement(),false);
	}

	/**
	 * Speichert die angegebenen XML-Daten abhängig vom verwendeten Konstruktor in einer Datei oder einem Stream
	 * @param root	XML-Root-Element, welches die zu speichernden Daten enthält
	 * @param plain	Gibt kein DOCTYPE aus
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean save(final Element root, final boolean plain) {
		if (root!=null) {
			if (output!=null) return saveToStream(output,root,plain);
			if (file!=null) return saveToFile(file,root,plain);
		}
		lastError=errorInternalErrorNoOutputObject;
		return false;
	}

	private String getPasswordFromUser() {
		if (GraphicsEnvironment.isHeadless()) {
			System.out.println(enterPassword);
			@SuppressWarnings("resource") /* Wir dürfen scanner.close(); nicht aufrufen, sonst später nicht mehr auf Eingaben (zum Beenden des Servers) reagiert werden. */
			Scanner inputReader=new Scanner(System.in);
			return inputReader.nextLine();
		} else {
			return JOptionPane.showInputDialog(null,enterPassword);
		}
	}

	private FileType getFileType(final File file) {
		if (fileType!=null && fileType!=FileType.AUTO) return fileType;

		final String s=file.toString().toUpperCase();

		if (s.endsWith(".ZIP") || s.endsWith(".XMZ") || s.endsWith(".BIN")) return FileType.ZIP_XML;
		if (s.endsWith(".TAR") || s.endsWith(".TAR.GZ")  || s.endsWith(".TARGZ")) return FileType.TAR_XML;
		if (s.endsWith(".CS") || s.endsWith(".CS_ENCRYPT")) return FileType.CRYPT_XML;
		if (s.endsWith(".JSON") || s.endsWith(".JS")) return FileType.JSON;

		return FileType.XML;
	}

	/**
	 * Speichert die angegebenen XML-Daten in der angegebenen XML- oder ZIP-Datei.
	 * @param file	Dateiname der Datei, in der die Daten gespeichert werden soll
	 * @param root	XML-Root-Element, welches die zu speichernden Daten enthält
	 * @param plain	Gibt kein DOCTYPE aus
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	private boolean saveToFile(final File file, final Element root, final boolean plain) {
		try (BufferedOutputStream fileOutput=new BufferedOutputStream(new FileOutputStream(file));) {
			return saveToStream(fileOutput,root,plain,getFileType(file));
		} catch (FileNotFoundException e) {lastError=String.format(errorOpeningFile,file.toString()); return false;} catch (IOException e) {lastError=String.format(errorClosingFile,file.toString()); return false;}
	}

	/**
	 * Speichert die angegebenen XML-Daten in dem angegebenen OutputStream.
	 * @param stream	OutputStream, in dem die Daten gespeichert werden soll
	 * @param root	XML-Root-Element, welches die zu speichernden Daten enthält
	 * @param plain	Gibt kein DOCTYPE aus
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	private boolean saveToStream(final OutputStream stream, final Element root, final boolean plain, final FileType fileType) {
		if (fileType==FileType.ZIP_XML) {
			try (ZipOutputStream zipOutput=new ZipOutputStream(stream)) {
				try {zipOutput.putNextEntry(new ZipEntry("data.xml"));} catch (IOException e1) {lastError=String.format(errorZipCreatingFile,file.toString()); return false;}
				return saveToStream(zipOutput,root,plain,FileType.XML);
			} catch (IOException e) {lastError=String.format(errorClosingFile,file.toString()); return false;}
		}

		if (fileType==FileType.TAR_XML) {
			try (GzipCompressorOutputStream gzip=new GzipCompressorOutputStream(stream)) {
				try (TarArchiveOutputStream tarOutput=new TarArchiveOutputStream(gzip)) {
					final TarArchiveEntry e=new TarArchiveEntry("data.xml");
					try (ByteArrayOutputStream tempStream=new ByteArrayOutputStream()) {
						if (!saveToStream(tempStream,root,plain,FileType.XML)) return false;
						e.setSize(tempStream.size());
						tarOutput.putArchiveEntry(e);
						try {
							tarOutput.write(tempStream.toByteArray());
							return true;
						} finally {
							tarOutput.closeArchiveEntry();
						}
					}
				}
			} catch (IOException e) {lastError=String.format(errorClosingFile,file.toString()); return false;}
		}

		if (fileType==FileType.CRYPT_XML) {
			String password=getPasswordFromUser();
			if (password==null || password.trim().isEmpty()) {lastError=errorCanceledByUser; return false;}

			ByteArrayOutputStream byteOutput1=new ByteArrayOutputStream();

			boolean b=false;
			try (ZipOutputStream zipOutput=new ZipOutputStream(byteOutput1)) {
				try {zipOutput.putNextEntry(new ZipEntry("data.xml"));} catch (IOException e1) {lastError=String.format(errorZipCreatingFile,file.toString()); return false;}
				b=saveToStream(zipOutput,root,plain,FileType.XML);
			} catch (IOException e) {lastError=String.format(errorClosingFile,file.toString()); return false;}

			if (b) {
				ByteArrayOutputStream byteOutput2=ChiperTools.encrypt(byteOutput1,password);
				if (byteOutput2==null) {lastError=String.format(errorEncryptingFile,file.toString()); return false;}
				try {
					byteOutput2.writeTo(stream);
				} catch (IOException e) {
					lastError=String.format(errorClosingFile,file.toString());
					return false;
				}
			}
			return b;
		}

		if (fileType==FileType.JSON) {
			String json=xmlToJson(root,false,true);
			try {
				stream.write(json.getBytes());
				return true;
			} catch (IOException e) {
				lastError=String.format(errorClosingFile,file.toString());
				return false;
			}
		}

		Transformer transformer;
		try {
			transformer=TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e1) {
			lastError=errorInitXMLInterpreter;
			return false;
		}
		transformer.setOutputProperty(OutputKeys.INDENT,"yes");
		if (!plain && dtd!=null && !dtd.isEmpty()) transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,mediaURL+dtd);
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
		try {
			transformer.transform(new DOMSource(root.getOwnerDocument()),new StreamResult(stream));
		} catch (TransformerException e1) {return false;}

		return true;
	}

	/**
	 * Speichert die angegebenen XML-Daten in dem angegebenen OutputStream.
	 * @param stream	OutputStream, in dem die Daten gespeichert werden soll
	 * @param root	XML-Root-Element, welches die zu speichernden Daten enthält
	 * @param plain	Gibt kein DOCTYPE aus
	 * @return	Gibt an, ob die Daten erfolgreich gespeichert werden konnten.
	 */
	private boolean saveToStream(final OutputStream stream, final Element root, final boolean plain) {
		return saveToStream(stream,root,plain,fileType);
	}

	/**
	 * Lädt XML-Daten abhängig vom verwendeten Konstruktor aus einer Datei oder einem Stream
	 * @return	Tritt ein Fehler auf, so wird <code>null</code> zurück gegeben, ansonsten das Root-Element der Daten
	 */
	public Element load() {
		if (inputText!=null) return loadFromString(inputText);
		if (input!=null) return loadFromStream(input,fileType);
		if (file!=null) return loadFromFile(file);
		lastError=errorInternalErrorNoInputObject;
		return null;
	}

	/**
	 * Lädt XML-Daten aus einem String
	 * @param text	String, aus dem die Daten geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird <code>null</code> zurück gegeben, ansonsten das Root-Element der Daten
	 */
	private Element loadFromString(final String text) {
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
			db=dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {lastError=errorInitXMLInterpreter; return null;}
		db.setErrorHandler(null);
		Element root=null;
		InputSource is=new InputSource(new StringReader(text));
		try {root=db.parse(is).getDocumentElement();} catch (SAXException | IOException e) {lastError=errorXMLProcess; return null;}

		return root;
	}

	private FileType guessFileTypeFromStream(final InputStream stream) {
		if (fileType!=FileType.AUTO) return fileType;

		try {
			if (stream==null || stream.available()<2 || !stream.markSupported()) return FileType.XML;
			try {
				final byte[] b=new byte[2];
				if (stream.read(b)<2) return FileType.XML;

				if (b[0]=='P' && b[1]=='K') return FileType.ZIP_XML;
				if (b[0]=='{' && b[1]=='"') return FileType.JSON;
				if (b[0]==31 && b[1]==-117) return FileType.TAR_XML;

				return FileType.XML;
			} finally {
				stream.reset();
			}
		} catch (IOException e) {
			return FileType.XML;
		}
	}

	/**
	 * Lädt XML-Daten aus einem InputStream
	 * @param stream	InputStream, aus dem die Daten geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird <code>null</code> zurück gegeben, ansonsten das Root-Element der Daten
	 */
	private Element loadFromStream(final InputStream stream, final FileType fileType) {
		final FileType type;
		if (fileType==FileType.AUTO) {
			type=guessFileTypeFromStream(stream);
		} else {
			type=fileType;
		}

		if (type==FileType.JSON) {
			StringBuilder sb=new StringBuilder();
			try (Reader r=new InputStreamReader(stream)) {
				char[] buf=new char[100*1024];
				int i=r.read(buf); while(i>0) {sb.append(buf,0,i); i=r.read(buf);}
			} catch (IOException e) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorXMLProcessFile,s);
				return null;
			}
			final Element result=jsonToXml(sb.toString(),true);
			if (result==null) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorXMLProcessFile,s);
			}
			return result;
		}

		if (type==FileType.ZIP_XML) {
			try (ZipInputStream zipInput=new ZipInputStream(stream)) {
				try {zipInput.getNextEntry();} catch (IOException e) {
					final String s=(file==null)?"":file.toString();
					lastError=String.format(errorXMLProcessFile,s);
					return null;
				}
				return loadFromStream(zipInput,FileType.XML);
			} catch (IOException e) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorXMLProcessFile,s);
				return null;
			}
		}

		if (type==FileType.TAR_XML) {
			try (GzipCompressorInputStream gzip=new GzipCompressorInputStream(stream)) {
				try (TarArchiveInputStream tarInput=new TarArchiveInputStream(gzip)) {
					try {tarInput.getNextEntry();} catch (IOException e) {
						final String s=(file==null)?"":file.toString();
						lastError=String.format(errorXMLProcessFile,s);
						return null;
					}
					return loadFromStream(tarInput,FileType.XML);
				}
			} catch (IOException e) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorClosingFile,s);
				return null;
			}
		}

		if (fileType==FileType.CRYPT_XML) {
			String password=getPasswordFromUser();
			if (password==null || password.trim().isEmpty()) {lastError=errorCanceledByUser; return null;}

			ByteArrayOutputStream byteOutput1;
			try {
				byte[] buf=new byte[stream.available()];
				if (stream.read(buf)!=buf.length) {
					final String s=(file==null)?"":file.toString();
					lastError=String.format(errorXMLProcessFile,s);
					return null;
				}
				byteOutput1=new ByteArrayOutputStream();
				byteOutput1.write(buf);
			} catch (IOException e1) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorXMLProcessFile,s);
				return null;
			}
			ByteArrayOutputStream byteOutput2=ChiperTools.decrypt(byteOutput1,password);
			if (byteOutput2==null) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorDecryptingFile,s);
				return null;
			}

			try (ZipInputStream zipInput=new ZipInputStream(new ByteArrayInputStream(byteOutput2.toByteArray()))) {
				try {zipInput.getNextEntry();} catch (IOException e) {
					final String s=(file==null)?"":file.toString();
					lastError=String.format(errorXMLProcessFile,s);
					return null;
				}
				return loadFromStream(zipInput,FileType.XML);
			} catch (IOException e) {
				final String s=(file==null)?"":file.toString();
				lastError=String.format(errorClosingFile,s);
				return null;
			}
		}

		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
			db=dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {lastError=errorInitXMLInterpreter; return null;}
		db.setErrorHandler(null);
		Element root=null;
		try {root=db.parse(stream,"").getDocumentElement();} catch (SAXException | IOException e) {lastError=errorXMLProcess; return null;}

		return root;
	}

	/**
	 * Lädt XML-Daten aus der einer angegebenen XML-Datei
	 * @param file	Dateiname der XML-Datei, aus der die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird <code>null</code> zurück gegeben, ansonsten das Root-Element der Daten
	 */
	private Element loadFromFile(final File file) {
		if (file==null) {lastError="Es wurde keine Datei angegeben."; return null;}
		if (!file.exists()) {lastError=String.format(errorFileDoesNotExists,file.toString()); return null;}

		try (BufferedInputStream fileInput=new BufferedInputStream(new FileInputStream(file))) {
			final FileType fileType=getFileType(file);
			return loadFromStream(fileInput,fileType);
		} catch (FileNotFoundException e1) {lastError=String.format(errorOpeningFile,file.toString()); return null;} catch (IOException e) {lastError=String.format(errorClosingFile,file.toString()); return null;}
	}

	/**
	 * Zeigt einen Dialog zum Laden einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param initialFolder	Ausgangsordner (wird <code>null</code> angegeben, so wird der letzte Ordner verwendet)
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showLoadDialog(final Container parent, final String title, final File initialFolder) {
		return showLoadDialog(parent,title,initialFolder,null,null);
	}

	/**
	 * Zeigt einen Dialog zum Laden einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param initialFolder	Ausgangsordner (wird <code>null</code> angegeben, so wird der letzte Ordner verwendet)
	 * @param userFilterNames	Namen für optionale zusätzlich anzuzeigende Filter
	 * @param userFilterExts	Dateinamenserweiterungen für optionale zusätzlich anzuzeigende Filter
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showLoadDialog(final Container parent, final String title, final File initialFolder, String[] userFilterNames, String[] userFilterExts) {
		Container c=parent; while ((c!=null) && (!(c instanceof Frame))) c=c.getParent();

		final JFileChooser fc;
		if (initialFolder==null) {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		} else {
			fc=new JFileChooser(initialFolder);
		}

		fc.setDialogTitle(title);
		final FileFilter xml=new FileNameExtensionFilter(fileTypeXML+" (*.xml)","xml");
		final FileFilter zip=new FileNameExtensionFilter(fileTypeCompressedXML+" (*.zip, *.xmz)","zip","xmz");
		final FileFilter tar=new FileNameExtensionFilter(fileTypeTARCompressedXML+" (*.tar.gz, *.tar)","tar.gz","tar");
		final FileFilter json=new FileNameExtensionFilter(fileTypeJSON+" (*.json)","json");
		final FileFilter cs=new FileNameExtensionFilter(fileTypeEncryptedXML+" (*.cs)","cs");
		fc.addChoosableFileFilter(xml);
		fc.addChoosableFileFilter(zip);
		fc.addChoosableFileFilter(tar);
		fc.addChoosableFileFilter(json);
		fc.addChoosableFileFilter(cs);

		if (userFilterNames==null) userFilterNames=new String[0];
		if (userFilterExts==null) userFilterExts=new String[0];
		int userCount=Math.min(userFilterNames.length,userFilterExts.length);
		final FileFilter[] userFilter=new FileNameExtensionFilter[userCount];
		for (int i=0;i<userCount;i++) {
			String[] s=userFilterExts[i].split(";");
			fc.addChoosableFileFilter(userFilter[i]=new FileNameExtensionFilter(userFilterNames[i],s));
		}

		final List<String> allTypes=new ArrayList<>();
		allTypes.add("xml");
		allTypes.add("zip");
		allTypes.add("xmz");
		allTypes.add("tar.gz");
		allTypes.add("tar");
		allTypes.add("json");
		allTypes.add("cs");
		if (userFilterExts!=null) allTypes.addAll(Arrays.asList(userFilterExts));
		FileFilter all=new FileNameExtensionFilter(fileTypeAll,allTypes.toArray(new String[0]));
		fc.addChoosableFileFilter(all);
		fc.setFileFilter(all);

		if (fc.showOpenDialog(c)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xml) file=new File(file.getAbsoluteFile()+".xml");
			if (fc.getFileFilter()==zip) file=new File(file.getAbsoluteFile()+".zip");
			if (fc.getFileFilter()==tar) file=new File(file.getAbsoluteFile()+".tar.gz");
			if (fc.getFileFilter()==json) file=new File(file.getAbsoluteFile()+".json");
			if (fc.getFileFilter()==cs) file=new File(file.getAbsoluteFile()+".cs");
			for (int i=0;i<userCount;i++) if (fc.getFileFilter()==userFilter[i]) {
				String[] s=userFilterExts[i].split(";");
				file=new File(file.getAbsoluteFile()+"."+s[0]);
				break;
			}
		}
		return file;
	}

	/**
	 * Zeigt einen Dialog zum Laden einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showLoadDialog(final Container parent, final String title) {
		return showLoadDialog(parent,title,null);
	}

	/**
	 * Zeigt einen Dialog zum Speichern einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param initialFolder	Ausgangsordner (wird <code>null</code> angegeben, so wird der letzte Ordner verwendet)
	 * @param userFilterNames	Namen für optionale zusätzlich anzuzeigende Filter
	 * @param userFilterExts	Dateinamenserweiterungen für optionale zusätzlich anzuzeigende Filter
	 * @param defaultSaveFormat	Standardmäßig in der "Dateityp"-Combobox ausgewählt anzuzeigendes Dateiformat
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showSaveDialog(final Container parent, final String title, final File initialFolder, String[] userFilterNames, String[] userFilterExts, final DefaultSaveFormat defaultSaveFormat) {
		Container c=parent; while ((c!=null) && (!(c instanceof Frame))) c=c.getParent();

		final JFileChooser fc;
		if (initialFolder==null) {
			fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
		} else {
			fc=new JFileChooser(initialFolder);
		}
		fc.setDialogTitle(title);

		final FileFilter xml=new FileNameExtensionFilter(fileTypeXML+" (*.xml)","xml");
		final FileFilter zip=new FileNameExtensionFilter(fileTypeCompressedXML+" (*.zip)","zip");
		final FileFilter tar=new FileNameExtensionFilter(fileTypeTARCompressedXML+" (*.tar.gz)","tar.gz");
		final FileFilter json=new FileNameExtensionFilter(fileTypeJSON+" (*.json)","json");
		final FileFilter cs=new FileNameExtensionFilter(fileTypeEncryptedXML+" (*.cs)","cs");
		fc.addChoosableFileFilter(xml);
		fc.addChoosableFileFilter(zip);
		fc.addChoosableFileFilter(tar);
		fc.addChoosableFileFilter(json);
		fc.addChoosableFileFilter(cs);
		switch (defaultSaveFormat) {
		case XML: fc.setFileFilter(xml); break;
		case JSON: fc.setFileFilter(json); break;
		case ZIP_XML: fc.setFileFilter(zip); break;
		case TAR_XML:  fc.setFileFilter(tar); break;
		case CRYPT_XML:  fc.setFileFilter(cs); break;
		default: fc.setFileFilter(xml); break;
		}

		if (userFilterNames==null) userFilterNames=new String[0];
		if (userFilterExts==null) userFilterExts=new String[0];
		int userCount=Math.min(userFilterNames.length,userFilterExts.length);
		final FileFilter[] userFilter=new FileNameExtensionFilter[userCount];
		for (int i=0;i<userCount;i++) {
			String[] s=userFilterExts[i].split(";");
			fc.addChoosableFileFilter(userFilter[i]=new FileNameExtensionFilter(userFilterNames[i],s));
		}

		if (fc.showSaveDialog(c)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xml) file=new File(file.getAbsoluteFile()+".xml");
			if (fc.getFileFilter()==zip) file=new File(file.getAbsoluteFile()+".zip");
			if (fc.getFileFilter()==tar) file=new File(file.getAbsoluteFile()+".tar.gz");
			if (fc.getFileFilter()==json) file=new File(file.getAbsoluteFile()+".json");
			if (fc.getFileFilter()==cs) file=new File(file.getAbsoluteFile()+".cs");
			for (int i=0;i<userCount;i++) if (fc.getFileFilter()==userFilter[i]) {
				String[] s=userFilterExts[i].split(";");
				file=new File(file.getAbsoluteFile()+"."+s[0]);
				break;
			}
		}
		return file;
	}

	/**
	 * Zeigt einen Dialog zum Speichern einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param initialFolder	Ausgangsordner (wird <code>null</code> angegeben, so wird der letzte Ordner verwendet)
	 * @param userFilterNames	Namen für optionale zusätzlich anzuzeigende Filter
	 * @param userFilterExts	Dateinamenserweiterungen für optionale zusätzlich anzuzeigende Filter
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showSaveDialog(final Container parent, final String title, final File initialFolder, String[] userFilterNames, String[] userFilterExts) {
		return showSaveDialog(parent,title,initialFolder,userFilterNames,userFilterExts,DefaultSaveFormat.XML);
	}

	/**
	 * Zeigt einen Dialog zum Speichern einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param initialFolder	Ausgangsordner (wird <code>null</code> angegeben, so wird der letzte Ordner verwendet)
	 * @param defaultSaveFormat	Standardmäßig in der "Dateityp"-Combobox ausgewählt anzuzeigendes Dateiformat
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showSaveDialog(final Container parent, final String title, final File initialFolder, final DefaultSaveFormat defaultSaveFormat) {
		return showSaveDialog(parent,title,initialFolder,null,null,defaultSaveFormat);
	}

	/**
	 * Zeigt einen Dialog zum Speichern einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param initialFolder	Ausgangsordner (wird <code>null</code> angegeben, so wird der letzte Ordner verwendet)
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showSaveDialog(final Container parent, final String title, final File initialFolder) {
		return showSaveDialog(parent,title,initialFolder,null,null,DefaultSaveFormat.XML);
	}

	/**
	 * Zeigt einen Dialog zum Speichern einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @param defaultSaveFormat	Standardmäßig in der "Dateityp"-Combobox ausgewählt anzuzeigendes Dateiformat
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showSaveDialog(final Container parent, final String title, final DefaultSaveFormat defaultSaveFormat) {
		return showSaveDialog(parent,title,null,null,null,defaultSaveFormat);
	}

	/**
	 * Zeigt einen Dialog zum Speichern einer XML-Datei an
	 * @param parent	Übergeordnetes Element (muss nicht ein Fenster sein, kann auch ein <code>JPanel</code> sein
	 * @param title	Titel des Dialogs
	 * @return	Gibt die gewählte Datei zurück oder im Falle eines Abbruchs durch den Nutzer <code>null</code>
	 */
	public static File showSaveDialog(final Container parent, final String title) {
		return showSaveDialog(parent,title,null,null,null,DefaultSaveFormat.XML);
	}

	private static void addNodeToJson(final StringBuilder sb, final Element node, final String indent) {
		String content=node.getTextContent();
		final List<Element> children=new ArrayList<>();
		final List<Attr> attributes=new ArrayList<>();

		if (content==null) content="";
		NamedNodeMap attrs=node.getAttributes();
		for (int i=0;i<attrs.getLength();i++) {
			Node n=attrs.item(i);
			if (!(n instanceof Attr)) continue;
			String s=((Attr)n).getName();
			if (s.equalsIgnoreCase("xmlns") || s.equalsIgnoreCase("xmlns:xsi") || s.equalsIgnoreCase("xsi:schemaLocation")) continue;
			attributes.add((Attr)n);
		}
		NodeList nodes=node.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node n=nodes.item(i);
			if (n instanceof Element) children.add((Element)n);
		}

		if (children.isEmpty() && attributes.isEmpty() && content.isEmpty()) {
			sb.append(indent+"{\""+node.getNodeName()+"\": {}}");
			return;
		}

		if (children.isEmpty() && attributes.isEmpty()) {
			sb.append(indent+"{\""+node.getNodeName()+"\": {\"xmlcontent\": \""+content.replace("\"","\\\"").replace("\n","\\n")+"\"}}");
			return;
		}

		if (children.isEmpty() && attributes.size()==1 && content.isEmpty()) {
			sb.append(indent+"{\""+node.getNodeName()+"\": {\""+attributes.get(0).getName()+"\": \""+attributes.get(0).getValue().replace("\"","\\\"").replace("\n","\\n")+"\"}}");
			return;
		}

		sb.append(indent+"{\""+node.getNodeName()+"\": {\n");

		boolean first=true;

		for (int i=0;i<attributes.size();i++) {
			if (first) first=false; else sb.append(",\n");
			sb.append(indent+"  \""+attributes.get(i).getName()+"\": \""+attributes.get(i).getValue().replace("\"","\\\"").replace("\n","\\n")+"\"");
		}

		if (children.isEmpty()) {
			if (!content.isEmpty()) {
				if (first) first=false; else sb.append(",\n");
				sb.append(indent+"  \"xmlcontent\": \""+content.replace("\"","\\\"").replace("\n","\\n")+"\"");
			}
		} else {
			if (first) first=false; else sb.append(",\n");
			sb.append(indent+"  \"xmlchildren\": [\n");
			boolean firstChild=true;
			for (int i=0;i<children.size();i++) {
				if (firstChild) firstChild=false; else sb.append(",\n");
				addNodeToJson(sb,children.get(i),indent+"    ");
			}
			sb.append("\n"+indent+"  ]");
		}

		sb.append("\n"+indent+"}}");
	}

	private static void addNodeToJsonSimple(final StringBuilder sb, final Element node, final String indent) {
		String content=node.getTextContent();
		final List<Element> children=new ArrayList<>();
		final List<Attr> attributes=new ArrayList<>();

		if (content==null) content="";
		NamedNodeMap attrs=node.getAttributes();
		for (int i=0;i<attrs.getLength();i++) {
			Node n=attrs.item(i);
			if (!(n instanceof Attr)) continue;
			String s=((Attr)n).getName();
			if (s.equalsIgnoreCase("xmlns") || s.equalsIgnoreCase("xmlns:xsi") || s.equalsIgnoreCase("xsi:schemaLocation")) continue;
			attributes.add((Attr)n);
		}
		NodeList nodes=node.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			Node n=nodes.item(i);
			if (n instanceof Element) children.add((Element)n);
		}

		if (children.isEmpty() && !content.isEmpty()) {
			sb.append("\""+content.replace("\"","\\\"").replace("\n","\\n")+"\"");
			return;
		}

		sb.append("{\n");
		boolean first=true;

		for (int i=0;i<attributes.size();i++) {
			Attr attr=attributes.get(i);
			if (attr==null) continue;
			final List<Attr> attributesOutput=new ArrayList<>();
			attributesOutput.add(attr);
			String name=attr.getNodeName();
			for (int j=i+1;j<attributes.size();j++) if (attributes.get(j)!=null && attributes.get(j).getNodeName().equals(name)) {
				attributesOutput.add(attributes.get(j)); attributes.set(j,null);
			}
			if (first) first=false; else sb.append(",\n");
			if (attributesOutput.size()==1) {
				sb.append(indent+"  \""+attributesOutput.get(0).getNodeName()+"\": \""+attributesOutput.get(0).getNodeValue().replace("\"","\\\"").replace("\n","\\n")+"\"");
			} else {
				sb.append(indent+"  \""+attributesOutput.get(0).getNodeName()+"\": [");
				boolean subFirst=true;
				for (int j=0;j<attributesOutput.size();j++) {
					if (subFirst) subFirst=false; else sb.append(", ");
					sb.append("\""+attributesOutput.get(j).getNodeValue().replace("\"","\\\"").replace("\n","\\n")+"\"");
				}
				sb.append("]");
			}
		}

		for (int i=0;i<children.size();i++) {
			Element element=children.get(i);
			if (element==null) continue;
			final List<Element> childrenOutput=new ArrayList<>();
			childrenOutput.add(element);
			String name=element.getNodeName();
			for (int j=i+1;j<children.size();j++) if (children.get(j)!=null && children.get(j).getNodeName().equals(name)) {
				childrenOutput.add(children.get(j)); children.set(j,null);
			}
			if (first) first=false; else sb.append(",\n");
			if (childrenOutput.size()==1) {
				sb.append(indent+"  \""+childrenOutput.get(0).getNodeName()+"\": ");
				addNodeToJsonSimple(sb,childrenOutput.get(0),indent+"  ");
			} else {
				sb.append(indent+"  \""+childrenOutput.get(0).getNodeName()+"\": [\n");
				boolean subFirst=true;
				for (int j=0;j<childrenOutput.size();j++) {
					if (subFirst) subFirst=false; else sb.append(",\n");
					sb.append(indent+"    ");
					addNodeToJsonSimple(sb,childrenOutput.get(j),indent+"    ");
				}
				sb.append("\n"+indent+"  ]");
			}
		}

		sb.append("\n"+indent+"}");
	}

	/**
	 * Wandelt ein XML-Objekt in einen JSON-String um.
	 * @param root	Umzuwandelndes XML-Objekt
	 * @param simpleJson Einfache, nicht wieder ladbare, aber vom Web-Viewer lesbare Variante verwenden
	 * @param useUTF8	Verwendet UTF-8 (<code>true</code>) oder ISO (<code>false</code>) als Zeichensatz
	 * @return	Repräsentation des XML-Objekts als JSON-String
	 */
	public static String xmlToJson(final Element root, final boolean simpleJson, final boolean useUTF8) {
		if (root==null) return "";
		final StringBuilder sb=new StringBuilder();
		if (simpleJson) {
			/*sb.append("{\n");	sb.append("  \""+root.getNodeName()+"\": "); - Root-Element nicht mit ausgeben*/
			addNodeToJsonSimple(sb,root,"");
			/* sb.append("\n}"); */
		} else {
			addNodeToJson(sb,root,"");
		}
		/* sb.append(";"); */

		final String result=sb.toString();

		if (useUTF8) {
			return new String(StandardCharsets.UTF_8.encode(result).array()).trim();
		} else {
			return result;
		}
	}

	/**
	 * Wandelt ein XML-Objekt in einen JSON-String um.
	 * @param doc	Umzuwandelndes XML-Dokument
	 * @param simpleJson Einfache, nicht wieder ladbare, aber vom Web-Viewer lesbare Variante verwenden
	 * @param useUTF8	Verwendet UTF-8 (<code>true</code>) oder ISO (<code>false</code>) als Zeichensatz
	 * @return	Repräsentation des XML-Objekts als JSON-String
	 */
	public static String xmlToJson(final Document doc, final boolean simpleJson, final boolean useUTF8) {
		if (doc==null) return "";
		return xmlToJson(doc.getDocumentElement(),simpleJson,useUTF8);
	}

	private static String[] splitData(final String json) {
		final List<String> data=new ArrayList<>();

		boolean inString=false;
		boolean lastWasEscape=false;
		boolean isEmpty=true;
		int blockCount=0;
		StringBuilder part=new StringBuilder();

		char[] ch=new char[json.length()];
		json.getChars(0,json.length(),ch,0);
		for (int i=0;i<ch.length;i++) {
			char c=ch[i];
			boolean thisIsEscape=false;
			boolean dataSeparator=false;

			if (c=='\\' && !lastWasEscape) thisIsEscape=true;

			if (inString) {
				if (c=='"' && !lastWasEscape) inString=false;
			} else {
				if (c=='"' && !lastWasEscape) inString=true;
				if ((c=='{' || c=='[') && !lastWasEscape) blockCount++;
				if ((c=='}' || c==']') && !lastWasEscape && blockCount>0) blockCount--;
				if (c==',' && !lastWasEscape && blockCount==0) dataSeparator=true;
			}

			if (dataSeparator) {
				data.add(part.toString().trim());
				part=new StringBuilder();
				isEmpty=true;
			} else {
				if (!isEmpty || c!=' ') {part.append(c); isEmpty=false;}
			}

			lastWasEscape=thisIsEscape;
		}
		if (!isEmpty) data.add(part.toString().trim());

		return data.toArray(new String[0]);
	}

	private static boolean loadJsonContent(final Element element, final String json) {
		String[] data=splitData(json);

		for (int i=0;i<data.length;i++) {
			String s=data[i];
			int j=s.indexOf(':'); if (j<1) return false;
			String name=s.substring(0,j).trim();
			if (name.length()>2 && name.startsWith("\"") && name.endsWith("\"")) name=name.substring(1,name.length()-1).trim();
			String param=s.substring(j+1).trim();
			if (param.length()<2) return false;
			if (name.equalsIgnoreCase("xmlcontent")) {
				if (param.charAt(0)!='"' || param.charAt(param.length()-1)!='"') return false;
				param=param.substring(1,param.length()-1);
				element.setTextContent(param.replace("\\n","\n").replace("\\\"","\""));
				continue;
			}
			if (name.equalsIgnoreCase("xmlchildren")) {
				if (param.charAt(0)!='[' || param.charAt(param.length()-1)!=']') return false;
				param=param.substring(1,param.length()-1);
				String[] children=splitData(param);
				for (int k=0;k<children.length;k++) {
					if (loadJsonObject(element,children[k])==null) return false;
				}
				continue;
			}
			if (param.charAt(0)=='{' && param.charAt(param.length()-1)=='}') {
				param=param.substring(1,param.length()-1);
				Element sub=element.getOwnerDocument().createElement(name);
				element.appendChild(sub);
				if (!loadJsonContent(sub,param)) return false;
				continue;
			}

			if (param.charAt(0)=='"' && param.charAt(param.length()-1)=='"') {
				param=param.substring(1,param.length()-1);
			}
			element.setAttribute(name,param.replace("\\n","\n").replace("\\\"","\""));
		}

		return true;
	}

	private static Element loadJsonObject(final Element parent, String json) {
		if (json==null || json.length()<4) return null;
		if (json.charAt(0)!='{' ||  json.charAt(json.length()-1)!='}') return null;
		json=json.substring(1,json.length()-1);
		int i=json.indexOf(':');
		if (i<1) return null;
		String name=json.substring(0,i).trim();
		if (name.length()>2 && name.startsWith("\"") && name.endsWith("\"")) name=name.substring(1,name.length()-1).trim();
		String data=json.substring(i+1).trim();
		if (data.length()==2) {
			if (data.charAt(0)!='{' ||  data.charAt(data.length()-1)!='}') return null;
			data="";
		} else {
			if (data.length()<3) return null;
			if (data.charAt(0)!='{' ||  data.charAt(data.length()-1)!='}') return null;
			data=data.substring(1,data.length()-1).trim();
		}

		Element element;
		if (parent==null) {
			XMLTools xml=new XMLTools();
			element=xml.generateRoot(name,true);
		} else {
			Document doc=parent.getOwnerDocument();
			parent.appendChild(element=doc.createElement(name));
		}

		if (!data.isEmpty()) {
			if (!loadJsonContent(element,data)) return null;
		}

		return element;
	}

	/**
	 * Lädt XML-Daten aus einem JSON-String
	 * @param json	JSON-String, aus dem die Daten geladen werden soll
	 * @param isUTF8	Gibt an, ob der Quell-String im UTF8-Format ist
	 * @return	Tritt ein Fehler auf, so wird <code>null</code> zurück gegeben, ansonsten das Root-Element der Daten
	 */
	public static Element jsonToXml(String json, final boolean isUTF8) {
		if (json==null) return null;

		if (isUTF8) {
			json=new String(json.getBytes(),StandardCharsets.UTF_8);
		}

		json=json.replace("\n"," ").replace("\r","").trim();
		if (json.length()<4) return null;
		if (json.charAt(json.length()-1)==';') json=json.substring(0,json.length()-1);
		return loadJsonObject(null,json);
	}

	/**
	 * Gibt die angegeben XML-Daten als base64-codiertem zip-gepacktem String zurück.
	 * @param root	XML-Root-Element, welches die zu speichernden Daten enthält
	 * @return	Gibt im Erfolgsfall die base64-codierten (und vorher zip-gepackten) xml-Daten zurück, sonst <code>null</code>.
	 */
	public String getBase64xml(final Element root) {
		try (ByteArrayOutputStream output=new ByteArrayOutputStream()) {
			try (ZipOutputStream zipOutput=new ZipOutputStream(output)) {
				zipOutput.putNextEntry(new ZipEntry("data.xml"));
				if (!saveToStream(zipOutput,root,false)) return null;
			}

			try (ByteArrayOutputStream encodedStream=new ByteArrayOutputStream()) {
				encodedStream.write(Base64.getEncoder().encode(output.toByteArray()));
				/* Java 8: encodedStream.write(Base64.getEncoder().encode(output.toByteArray())); */
				return encodedStream.toString();
			}

		} catch (IOException e) {lastError=errorStreamClosing; return null;}
	}
}
