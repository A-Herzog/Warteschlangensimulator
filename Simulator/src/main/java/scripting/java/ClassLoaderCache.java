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
package scripting.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import language.Language;
import mathtools.NumberTools;

/**
 * Diese Singleton-Klasse stellt sicher, dass alle externen Klassen immer über denselben
 * Classloader geladen werde. Nur so können mehrere Nutzer-Skripte an verschiedenen
 * Stationen dennoch (über die globale Map) auf Objekte externer Klassen zugreifen.
 * @author Alexander Herzog
 */
public class ClassLoaderCache {
	/**
	 * Instanz dieses Singletons
	 * @see #getInstance()
	 */
	private static ClassLoaderCache instance=new ClassLoaderCache();

	/**
	 * Systemweiter Java-Kompiler
	 */
	private static final JavaCompiler compiler;

	/**
	 * Datensatz für den Fall, dass kein externer Plugin-Ordner definiert ist.
	 */
	private CompileData defaultData;

	/**
	 * Datensätze in Abhängigkeit von den externen Plugin-Ordnern
	 */
	private Map<String,CompileData> data;

	static {
		compiler=ToolProvider.getSystemJavaCompiler();
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht direkt instanziert werden.
	 * @see #getInstance()
	 */
	private ClassLoaderCache() {
		data=new HashMap<>();
	}

	/**
	 * Liefert die Instanz dieses Singletons.
	 * @return	Instanz dieses Singletons
	 */
	public static ClassLoaderCache getInstance() {
		return instance;
	}

	/**
	 * Löscht den Cache.
	 */
	public synchronized void clearCache() {
		defaultData=null;
		data.clear();
	}

	/**
	 * Liefert einen Dateimanager- und Classloader-Datensatz in Abhängigkeit
	 * von einem externen Plugins-Verzeichnis.
	 * @param additionalClassPath	Externes Plugins-Verzeichnis (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall den Datensatz. Ist das angegebene Verzeichnis ungleich <code>null</code>, aber ungültig, so liefert die Funktion <code>null</code>.
	 */
	private synchronized CompileData getCompileData(final String additionalClassPath) {
		if (additionalClassPath==null) {
			if (defaultData==null) {
				CompileData newData=new CompileData();
				if (!newData.init(null)) return null;
				defaultData=newData;
			}
			return defaultData;
		}

		CompileData extendedData=data.get(additionalClassPath);
		if (extendedData==null) {
			CompileData newData=new CompileData();
			if (!newData.init(additionalClassPath)) return null;
			data.put(additionalClassPath,extendedData=newData);
		}
		return extendedData;
	}

	/**
	 * Versucht einen Text in eine Klasse zu kompilieren und diese zu laden.
	 * @param additionalClassPath	Externes Plugins-Verzeichnis (kann <code>null</code> sein)
	 * @param className	Name der Klasse
	 * @param text	Klassentext
	 * @return	Liefert den Status der Übersetzung zurück
	 */
	public static ExtendedStatus process(final String additionalClassPath, final String className, final String text) {
		if (compiler==null) return new ExtendedStatus(DynamicStatus.NO_COMPILER);

		/* Vorbereiten */
		final CompileData compileData=getInstance().getCompileData(additionalClassPath);
		if (compileData==null) return new ExtendedStatus(DynamicStatus.NO_COMPILER);

		try {

			final DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
			final JavaFileObject file=new CharSequenceJavaFileObject(className,text);
			final Iterable<? extends JavaFileObject> compilationUnits=Arrays.asList(file);

			final List<String> options=new ArrayList<>();
			options.add("-cp");
			if (additionalClassPath!=null) {
				options.add(System.getProperty("java.class.path")+File.pathSeparatorChar+additionalClassPath);
			} else {
				options.add(System.getProperty("java.class.path"));
			}

			/* Übersetzen */
			final CompilationTask task=compiler.getTask(null,compileData.fileManager,diagnostics,options,null,compilationUnits);

			if (task.call()) {
				/* Klasse laden */
				return new ExtendedStatus(compileData.classLoader.loadClass(className));
			}

			/* Fehlermeldung erstellen */
			final StringBuilder sb=new StringBuilder();
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
				if (diagnostic.getKind()!=javax.tools.Diagnostic.Kind.ERROR) continue;
				long lineNr=diagnostic.getLineNumber();
				final long colNr=diagnostic.getColumnNumber();
				if (lineNr<0 || colNr<0) continue;
				lineNr--; /* Vorspann berücksichtigen */
				final String[] lines=text.split("\\n");
				if (lines==null || lines.length==0 || lines.length<lineNr) continue;
				if (sb.length()>0) sb.append("\n");
				sb.append(String.format(Language.tr("Simulation.Java.Error.CompileError.Line"),lineNr+1)+":\n");
				sb.append(lines[(int)lineNr]);
				sb.append("\n");
				sb.append(String.format(Language.tr("Simulation.Java.Error.CompileError.Column"),colNr+1)+": ");
				sb.append(diagnostic.getMessage(NumberTools.getLocale()));
				sb.append("\n");
			}
			if (sb.length()>0) return new ExtendedStatus(DynamicStatus.COMPILE_ERROR,sb.toString());
			return new ExtendedStatus(DynamicStatus.COMPILE_ERROR);

		} catch (ClassNotFoundException e) {
			return new ExtendedStatus(DynamicStatus.LOAD_ERROR,e.getMessage());
		}
	}

	/**
	 * Datensatz für einen Classloader
	 * @see ClassLoaderCache#getCompileData(String)
	 */
	private class CompileData {
		/**
		 * Dateimanager
		 */
		private JavaFileManager fileManager;

		/**
		 * Classloader
		 */
		private ClassLoader classLoader;

		/**
		 * Initialisiert Dateimanager und Classloader in Abhängigkeit
		 * von einem externen Plugins-Verzeichnis.
		 * @param additionalClassPath	Externes Plugins-Verzeichnis (kann <code>null</code> sein)
		 * @return	Liefert im Erfolgsfall <code>true</code>
		 */
		@SuppressWarnings("resource")
		public boolean init(final String additionalClassPath) {
			URLClassLoader urlLoader=null;
			if (additionalClassPath!=null) {
				try {
					final File folder=new File(additionalClassPath);
					urlLoader=new URLClassLoader(new URL[]{folder.toURI().toURL()});
				} catch (MalformedURLException e1) {
					return false;
				}
			}

			fileManager=new ClassFileManager<JavaFileManager>(compiler.getStandardFileManager(null,null,null),urlLoader);
			classLoader=fileManager.getClassLoader(null);
			return true;
		}
	}

	/**
	 * Status der Verarbeitung in {@link ClassLoaderCache#process(String, String, String)}
	 * @see ClassLoaderCache#process(String, String, String)
	 */
	public static class ExtendedStatus {
		/**
		 * Status
		 */
		public final DynamicStatus status;

		/**
		 * Optionaler Fehlermeldungstext<br>
		 * (Ist im Erfolgsfall immer <code>null</code> und kann im Fehlerfall <code>null</code> sein oder auch nicht.)
		 */
		public final String errorText;

		/**
		 * Liefert im Erfolgsfall die geladene Klasse.
		 */
		public final Class<?> loadedClass;

		/**
		 * Konstruktor der Klasse für den Fehlerfall
		 * @param status	Status
		 */
		private ExtendedStatus(final DynamicStatus status) {
			this.status=status;
			errorText=null;
			loadedClass=null;

		}

		/**
		 * Konstruktor der Klasse für den Fehlerfall
		 * @param status	Status
		 * @param errorText	Fehlermeldungstext
		 */
		private ExtendedStatus(final DynamicStatus status, final String errorText) {
			this.status=status;
			this.errorText=errorText;
			loadedClass=null;
		}

		/**
		 * Konstruktor der Klasse für den Erfolgsfall
		 * @param loadedClass	Geladene Klasse
		 */
		private ExtendedStatus(final Class<?> loadedClass) {
			status=DynamicStatus.OK;
			errorText=null;
			this.loadedClass=loadedClass;
		}
	}
}
