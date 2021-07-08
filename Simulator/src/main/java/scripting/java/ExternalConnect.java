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
	private final Map<String,List<String>> informationMap;

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
			"SystemInterface"
	};

	/**
	 * Konstruktor der Klasse
	 * @param folder	Verzeichnis der Klassendateien
	 */
	public ExternalConnect(final File folder) {
		this.folder=folder;
		informationMap=new HashMap<>();
		runnerMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		processFiles(folder,new String[0]);
	}

	/**
	 * Erfasst alle Klassendateien in einem Verzeichnis.
	 * @param folder	Verzeichnis
	 * @param packages	Pfad in Bezug auf die Package-Darstellung der Klassen in dem Verzeichnis
	 */
	private void processFiles(final File folder, final String[] packages) {
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

				final List<String> list=processFile(this.folder,file,packages,shortFileName);
				if (folder.equals(this.folder)) {
					if (list!=null && list.size()>0) informationMap.put(shortFileName,list);
				}
			}

			if (file.isDirectory()) {
				final String[] subPackages=Arrays.copyOf(packages,packages.length+1);
				subPackages[subPackages.length-1]=file.getName();
				processFiles(file,subPackages);
			}
		}
	}

	/**
	 * Liest die passenden Methodennamen aus einer Klassendatei aus.
	 * @param folder	Basisverzeichnis aus Package-Sicht
	 * @param file	Zu verarbeitende Datei
	 * @param packages	Pfad in Bezug auf die Package-Darstellung der Klasse
	 * @param shortFileName	Dateiname der Klasse ohne Erweiterung
	 * @return	Liste der Methoden in der Klassendatei
	 */
	private List<String> processFile(final File folder, final File file, final String[] packages, final String shortFileName) {
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
		} catch (ClassNotFoundException | SecurityException | IOException | IllegalArgumentException | NoClassDefFoundError e) {
			e.printStackTrace();
		}

		return list;
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
	public Map<String,List<String>> getInformationMap() {
		return informationMap;
	}

	/**
	 * Legt ein neues Starter-Objekt für eine bestimmte Methode an.
	 * @param className	Name der Klasse
	 * @param functionName	Name der Methode innerhalb der Klasse
	 * @return	Starter-Objekt oder <code>null</code>, wenn die Methode innerhalb der Klasse nicht existiert
	 */
	private RunRecord buildRunRecord(final String className, final String functionName) {
		try (final URLClassLoader loader=new URLClassLoader(new URL[]{folder.toURI().toURL()})) {
			final Class<?> cls=loader.loadClass(className);
			final Method method=cls.getMethod(functionName,RuntimeInterface.class,SystemInterface.class,Object.class);
			return new RunRecord(cls,method);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IOException | IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Liefert ein Starter-Objekt für eine bestimmte Methode (aus dem Cache oder legt es neu an).
	 * @param className	Name der Klasse
	 * @param functionName	Name der Methode innerhalb der Klasse
	 * @return	Starter-Objekt oder <code>null</code>, wenn die Methode innerhalb der Klasse nicht existiert
	 */
	private RunRecord getRunRecord(final String className, final String functionName) {
		Map<String,RunRecord> map=runnerMap.get(className);
		if (map!=null) {
			final RunRecord record=map.get(functionName);
			if (record!=null) return record;
		}

		final RunRecord record=buildRunRecord(className,functionName);
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
	 * @return	Rückgabewert der Nutzer-Methode (liefert <code>null</code>, wenn der Aufruf nicht möglich war)
	 */
	public Object runFunction(final String className, final String functionName, final SystemImpl systemData, final Object data) {
		final RunRecord runRecord=getRunRecord(className,functionName);
		if (runRecord==null) return null;

		if (runtime==null) runtime=new RuntimeImpl((systemData==null)?null:systemData.simData);

		return runRecord.run(runtime,systemData,data);
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
		 * @return	Rückgabewert der Nutzer-Methode (liefert <code>null</code>, wenn der Aufruf nicht möglich war)
		 */
		public Object run(final RuntimeInterface runtimeData, final SystemInterface systemData, final Object data) {
			if (obj==null) try {
				obj=cls.getDeclaredConstructor().newInstance();
			} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
				return null;
			}

			try {
				return method.invoke(obj,runtimeData,systemData,data);
			} catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
				return null;
			}
		}
	}
}