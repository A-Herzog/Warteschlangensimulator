package scripting.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import language.Language;
import mathtools.Table;
import scripting.java.ClassLoaderCache.ExtendedStatus;

/**
 * Dieser Loader ermöglicht das Auflisten und Ausführen von
 * Methoden innerhalb von externen Java-Klassendateien in
 * einem bestimmten Verzeichnis.
 */
public class ExternalConnect {
	/**
	 * Verzeichnis der Klassendateien
	 */
	private final File folder;

	/**
	 * Zuordnung von Klassendateinamen zu Methodennamen-Listen
	 */
	private final Map<String,FileInfo> informationMap;

	/**
	 * Zuordnung von Klassendateinamen zu Methodennamen zu Starter-Objekten
	 */
	private final Map<String,Map<String,RunRecord>> runnerMap;

	/**
	 * Klasse mit deren Hilfe Nutzer-Java-Codes auf allgemeine, simulationsunabhängige Daten zugreifen kann.
	 */
	private RuntimeInterface runtime;

	/**
	 * Dateinamenserweiterung für Klassendateien
	 */
	private static final String EXTENSION=".class";

	/**
	 * Dateien die bei der Verarbeitung nicht berücksichtigt werden sollen
	 * (d.h. in denen nicht nach passenden Plugin-Methoden gesucht werden soll)
	 */
	private static final String[] IGNORE_FILES=new String[] {
			"ClientsInterface",
			"RuntimeInterface",
			"SystemInterface",
			"OutputInterface",
			"ClientInterface"
	};

	/**
	 * Konstruktor der Klasse
	 * @param folder	Verzeichnis der Klassendateien
	 */
	public ExternalConnect(final File folder) {
		this.folder=folder;
		informationMap=new HashMap<>();
		runnerMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		processFiles(folder,true,new String[0]);
	}

	/**
	 * Erfasst alle Klassendateien in einem Verzeichnis.
	 * @param folder	Verzeichnis
	 * @param isRootDir	Ist dies das Hauptverzeichnis der betrachteten Verzeichnisstruktur?
	 * @param packages	Pfad in Bezug auf die Package-Darstellung der Klassen in dem Verzeichnis
	 */
	private void processFiles(final File folder, final boolean isRootDir, final String[] packages) {
		if (folder==null) return;
		if (!folder.isDirectory()) return;
		final String[] fileList=folder.list();
		if (fileList==null) return;

		for (String fileName: fileList) {
			final File file=new File(folder,fileName);
			if (file.isFile()) {
				final String name=file.getName();
				if (!name.toLowerCase().endsWith(EXTENSION)) continue;

				boolean ignore=false;
				for (String ignoreFile: IGNORE_FILES) if (name.equalsIgnoreCase(ignoreFile+EXTENSION)) {ignore=true; break;}
				if (ignore) continue;

				final String shortFileName=fileName.substring(0,fileName.toLowerCase().lastIndexOf(EXTENSION));
				String packagesStrings=String.join(".",packages);
				if (!packagesStrings.isEmpty()) packagesStrings+=".";

				final FileInfo fileInfo=processFile(this.folder,file,isRootDir,packages,shortFileName);
				if (fileInfo==null) continue;
				if (fileInfo.error!=null) {
					informationMap.put(packagesStrings+shortFileName,fileInfo);
				} else {
					if (folder.equals(this.folder) || (fileInfo.methods!=null && fileInfo.methods.size()==0)) {
						informationMap.put(packagesStrings+shortFileName,fileInfo);
					}
				}
			}

			if (file.isDirectory()) {
				final String[] subPackages=Arrays.copyOf(packages,packages.length+1);
				subPackages[subPackages.length-1]=file.getName();
				processFiles(file,false,subPackages);
			}
		}
	}

	/**
	 * Liest die passenden Methodennamen aus einer Klassendatei aus.
	 * @param folder	Basisverzeichnis aus Package-Sicht
	 * @param file	Zu verarbeitende Datei
	 * @param isRootDir	Handelt es sich um eine Datei im Hauptverzeichnis der betrachteten Verzeichnisstruktur?
	 * @param packages	Pfad in Bezug auf die Package-Darstellung der Klasse
	 * @param shortFileName	Dateiname der Klasse ohne Erweiterung
	 * @return	Liste der Methoden in der Klassendatei, Fehlermeldung oder <code>null</code>, wenn die Klasse aus unbekannten Gründen nicht verarbeitet werden konnte
	 */
	private FileInfo processFile(final File folder, final File file, final boolean isRootDir, final String[] packages, final String shortFileName) {
		final List<String> list=new ArrayList<>();

		try (final URLClassLoader loader=new URLClassLoader(new URL[]{folder.toURI().toURL()})) {
			String packagesString=String.join(".",packages);
			if (!packagesString.isEmpty()) packagesString=packagesString+".";
			final Class<?> cls=loader.loadClass(packagesString+shortFileName);

			/* Plugin-Methoden suchen */
			for (Method m: cls.getMethods()) {
				final Parameter[] p=m.getParameters();
				if (p.length!=3) continue;
				if (p[0].getType()!=RuntimeInterface.class) continue;
				if (p[1].getType()!=SystemInterface.class) continue;
				if (p[2].getType()!=Object.class) continue;
				if (m.getReturnType()!=Object.class) continue;
				list.add(m.getName());
			}
		} catch (NoClassDefFoundError e) {
			final List<String> error=processFileError(folder,file,packages,shortFileName);
			if (error==null) return null;
			return new FileInfo(file,isRootDir,null,error);
		} catch (ClassNotFoundException | SecurityException | IOException | IllegalArgumentException e) {
			return null;
		}

		return new FileInfo(file,isRootDir,list,null);
	}

	/**
	 * Ermittelt den Package-Eintrag aus einer Klassendatei
	 * (bzw. aus deren zugehöriger Java-Data)
	 * @param file	Klassendatei
	 * @return	Package-Anweisung (oder <code>null</code>, wenn kein Eintrag ermittelt werden konnte)
	 * @see #processFileError(File, File, String[], String)
	 */
	private String getPackageDirective(final File file) {
		String fileName=file.toString();
		if (!fileName.endsWith(".class")) return null;
		fileName=fileName.substring(0,fileName.length()-6)+".java";

		final String packageId="package ";
		final List<String> lines=Table.loadTextLinesFromFile(new File(fileName));
		if (lines!=null) for (String line: lines) {
			String s=line.trim();
			if (!s.startsWith(packageId)) continue;
			s=s.substring(packageId.length()).trim();
			if (!s.endsWith(";")) return null;
			return s.substring(0,s.length()-1);
		}
		return null;
	}

	/**
	 * Versucht eine Erklärung für einen Lade-Fehler zu ermitteln
	 * @param folder	Basisverzeichnis aus Package-Sicht
	 * @param file	Zu verarbeitende Datei
	 * @param packages	Pfad in Bezug auf die Package-Darstellung der Klasse
	 * @param shortFileName	Dateiname der Klasse ohne Erweiterung
	 * @return	Erklärung für einen Klassen-Lade-Fehler oder <code>null</code>, wenn keine Fehlerbeschreibung erstellt werden konnte
	 */
	private List<String> processFileError(final File folder, final File file, final String[] packages, final String shortFileName) {
		final String packageNameByClassFile=getPackageDirective(file);
		if (packageNameByClassFile==null) return null;
		final String packageNameByFolder=String.join(".",packages);

		if (packageNameByClassFile.equals(packageNameByFolder)) return null;

		final List<String> error=new ArrayList<>();

		error.add(Language.tr("ExternalConnect.FolderPackageError"));
		if (packageNameByFolder.isEmpty()) {
			error.add(Language.tr("ExternalConnect.FolderPackageError.NeedSubfolder"));
		} else {
			error.add(Language.tr("ExternalConnect.FolderPackageError.PackageByClassFile")+": "+packageNameByClassFile);
			error.add(Language.tr("ExternalConnect.FolderPackageError.PackageByFolder")+": "+packageNameByFolder);
		}

		final String packageNameByParentFolder;
		if (packageNameByFolder.isEmpty()) packageNameByParentFolder=folder.getName(); else packageNameByParentFolder=packageNameByFolder+"."+folder.getName();
		if (packageNameByClassFile.equals(packageNameByParentFolder)) {
			error.add(Language.tr("ExternalConnect.FolderPackageError.Solution"));
		}

		return error;
	}

	/**
	 * Liefert das Verzeichnis der Klassendateien.
	 * @return	Verzeichnis der Klassendateien
	 */
	public File getFolder() {
		return folder;
	}

	/**
	 * Liefert die Zuordnung von Klassendateinamen zu Methodennamen zu Starter-Objekten.
	 * @return	Zuordnung von Klassendateinamen zu Methodennamen zu Starter-Objekten
	 */
	public Map<String,FileInfo> getInformationMap() {
		return informationMap;
	}

	/**
	 * Legt ein neues Starter-Objekt für eine bestimmte Methode an.
	 * @param className	Name der Klasse
	 * @param functionName	Name der Methode innerhalb der Klasse
	 * @param allowLoadMoreClasses	Ist das Laden von allgemeinen, weiteren nutzerdefinierten Klassen aus class-Dateien zulässig?
	 * @return	Starter-Objekt oder <code>null</code>, wenn die Methode innerhalb der Klasse nicht existiert
	 */
	private RunRecord buildRunRecord(final String className, final String functionName, final boolean allowLoadMoreClasses) {
		if (allowLoadMoreClasses) {
			final ExtendedStatus status=ClassLoaderCache.loadExternalClass(folder.toString(),className);
			if (status.status!=DynamicStatus.OK) return null;
			final Class<?> cls=status.loadedClass;
			try {
				final Method method=cls.getMethod(functionName,RuntimeInterface.class,SystemInterface.class,Object.class);
				return new RunRecord(cls,method);
			} catch (NoSuchMethodException e) {
				return null;
			}
		} else {
			try (final URLClassLoader loader=new URLClassLoader(new URL[]{folder.toURI().toURL()})) {
				final Class<?> cls=loader.loadClass(className);
				final Method method=cls.getMethod(functionName,RuntimeInterface.class,SystemInterface.class,Object.class);
				return new RunRecord(cls,method);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IOException | IllegalArgumentException e) {
				return null;
			}
		}
	}

	/**
	 * Liefert ein Starter-Objekt für eine bestimmte Methode (aus dem Cache oder legt es neu an).
	 * @param className	Name der Klasse
	 * @param functionName	Name der Methode innerhalb der Klasse
	 * @param allowLoadMoreClasses	Ist das Laden von allgemeinen, weiteren nutzerdefinierten Klassen aus class-Dateien zulässig?
	 * @return	Starter-Objekt oder <code>null</code>, wenn die Methode innerhalb der Klasse nicht existiert
	 */
	private RunRecord getRunRecord(final String className, final String functionName, final boolean allowLoadMoreClasses) {
		Map<String,RunRecord> map=runnerMap.get(className);
		if (map!=null) {
			final RunRecord record=map.get(functionName);
			if (record!=null) return record;
		}

		final RunRecord record=buildRunRecord(className,functionName,allowLoadMoreClasses);
		if (record!=null) {
			if (map==null) runnerMap.put(className,map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
			map.put(functionName,record);
		}

		return record;
	}

	/**
	 * Ruft einer Nutzer-Java-Methode aus
	 * @param className	Name der Klasse
	 * @param functionName	Name der Methode innerhalb der Klasse
	 * @param systemData	Objekt vom Typ {@link SystemInterface}, das an die Nutzer-Methode weitergereicht wird
	 * @param data	Benutzerdatenobjekt, das an die Nutzer-Methode weitergereicht wird
	 * @param allowLoadMoreClasses	Ist das Laden von allgemeinen, weiteren nutzerdefinierten Klassen aus class-Dateien zulässig?
	 * @param errorCallback	Optionales Callback, welches Fehlermeldungen aufnimmt (darf <code>null</code> sein)
	 * @return	Rückgabewert der Nutzer-Methode (liefert <code>null</code>, wenn der Aufruf nicht möglich war)
	 */
	public Object runFunction(final String className, final String functionName, final SystemImpl systemData, final Object data, final boolean allowLoadMoreClasses, final Consumer<String> errorCallback) {
		final RunRecord runRecord=getRunRecord(className,functionName,allowLoadMoreClasses);
		if (runRecord==null) return null;

		if (runtime==null) runtime=new RuntimeImpl((systemData==null)?null:systemData.simData);

		return runRecord.run(runtime,systemData,data,errorCallback);
	}

	/**
	 * Dieses Objekt kapselt die Daten zu einer geladenen Methode innerhalb einer Klasse.
	 */
	private static class RunRecord {
		/**
		 * Geladene Klasse
		 */
		private final Class<?> cls;

		/**
		 * Instanz von {@link #cls}<br>
		 * (wird bei Bedarf angelegt)
		 */
		private Object obj;

		/**
		 * Auszuführende Methode innerhalb von {@link #cls}
		 */
		private final Method method;

		/**
		 * Konstruktor der Klasse
		 * @param cls	Geladene Klasse
		 * @param method	Auszuführende Methode
		 */
		public RunRecord(final Class<?> cls, final Method method) {
			this.cls=cls;
			this.method=method;
		}

		/**
		 * Führt die angegebene Methode aus.
		 * @param runtimeData	Objekt vom Typ {@link RuntimeInterface}, das an die Nutzer-Methode weitergereicht wird
		 * @param systemData	Objekt vom Typ {@link SystemInterface}, das an die Nutzer-Methode weitergereicht wird
		 * @param data	Benutzerdatenobjekt, das an die Nutzer-Methode weitergereicht wird
		 * @param errorCallback	Optionales Callback, welches Fehlermeldungen aufnimmt (darf <code>null</code> sein)
		 * @return	Rückgabewert der Nutzer-Methode (liefert <code>null</code>, wenn der Aufruf nicht möglich war)
		 */
		public Object run(final RuntimeInterface runtimeData, final SystemInterface systemData, final Object data, final Consumer<String> errorCallback) {
			if (obj==null) try {
				obj=cls.getDeclaredConstructor().newInstance();
			} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
				return null;
			}

			try {
				return method.invoke(obj,runtimeData,systemData,data);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
				if (errorCallback!=null) {
					final String error;
					if (e instanceof InvocationTargetException) {
						final Throwable e2=((InvocationTargetException)e).getTargetException();
						final String msg=e2.getMessage();
						if (msg==null) error=e2.getClass().getName(); else error=e2.getClass().getName()+": "+msg;
					} else {
						final String msg=e.getMessage();
						if (msg==null) error=e.getClass().getName(); else error=e.getClass().getName()+": "+msg;
					}
					errorCallback.accept(error);
				}
				return null;
			}
		}
	}

	/**
	 * Datensatz zu einer Datei
	 * @see ExternalConnect#processFile(File, File, boolean, String[], String)
	 */
	public static class FileInfo {
		/**
		 * Datei auf die sicher dieser Datensatz bezieht
		 */
		public final File file;

		/**
		 * Handelt es sich um eine Datei im Hauptverzeichnis der betrachteten Verzeichnisstruktur?
		 */
		public final boolean isInRootDir;

		/**
		 * Gefundene Methoden
		 */
		public final List<String> methods;

		/**
		 * Gefundene Fehler
		 */
		public final List<String> error;

		/**
		 * Konstruktor der Klasse
		 * @param file	Datei auf die sicher dieser Datensatz bezieht
		 * @param isInRootDir	Handelt es sich um eine Datei im Hauptverzeichnis der betrachteten Verzeichnisstruktur?
		 * @param methods	Gefundene Methoden
		 * @param error	Gefundene Fehler
		 */
		private FileInfo(final File file, final boolean isInRootDir, final List<String> methods, final List<String> error) {
			this.file=file;
			this.isInRootDir=isInRootDir;
			this.methods=methods;
			this.error=error;
		}
	}
}