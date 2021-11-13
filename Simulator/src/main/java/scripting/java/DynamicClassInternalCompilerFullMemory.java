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
package scripting.java;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Ermöglicht es, dynamisch Klassen aus Texten oder aus java- oder aus class-Dateien zu laden.<br>
 * Es wird dafür das Java Compiler API verwendet und sowohl die java-Eingabedatei als auch die class-Ausgabedatei
 * werden dabei ausschließlich im Arbeitsspeicher gehalten.<br>
 * @see <a href="http://javapracs.blogspot.com/2011/06/dynamic-in-memory-compilation-using.html">http://javapracs.blogspot.com/2011/06/dynamic-in-memory-compilation-using.html</a>
 * @author Alexander Herzog
 */
public class DynamicClassInternalCompilerFullMemory extends DynamicClassBase {
	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 * @param additionalClassPath	Optionaler zusätzlicher über den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 */
	public DynamicClassInternalCompilerFullMemory(final DynamicSetup setup, final String additionalClassPath) {
		super(setup,additionalClassPath);
	}

	@Override
	public Object prepare(String text) {
		/* Daten vorhanden? */
		if (text==null || text.trim().isEmpty()) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		/* Klassenname bestimmen */
		final String className=getClassName(text);
		if (className==null) return DynamicStatus.COMPILE_ERROR;

		/* Kompiler finden */
		if (!DynamicFactory.hasCompiler()) return DynamicStatus.NO_COMPILER;

		/* Die Klassen müssen vom selben Classloader geladen werden, sonst können Objekte über die globale Map nicht zwischen verschiedenen Stationen ausgetauscht werden. */
		final ClassLoaderCache.ExtendedStatus status=ClassLoaderCache.process(additionalClassPath,className,text);
		if (status.status==DynamicStatus.OK) return status.loadedClass;
		if (status.errorText!=null) setError(status.errorText);
		return status.status;
	}

	@Override
	protected DynamicStatus loadClass(Object classData) {
		if (!(classData instanceof Class<?>)) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		setLoadedClass((Class<?>)classData);
		return DynamicStatus.OK;
	}

	/**
	 * Handelt es sich bei der verwendeten Java-Umgebung um Java 8?
	 * @return	Liefert <code>true</code>, wenn Java 8 verwendet wird, und <code>false</code> bei einer Java-Version &ge;9
	 */
	private static boolean isJava8() {
		final String version=System.getProperty("java.version");
		if (version==null) return true;
		return version.startsWith("1.8");
	}

	/**
	 * Übersetzt eine java-Datei (aus dem Dateisystem) in eine class-Datei (auch im Dateisystem),
	 * d.h. verhält sich so wie ein Aufruf von "javac".
	 * @param file	Eingabe-java-Datei
	 * @param baseFolder	Verzeichnis für den Klassenpfad (wird als "-cp " übergeben)
	 * @return	Liefert ein Statusobjekt zurück (nie <code>null</code>)
	 */
	public static CompilerResult compileJavaToClass(final File file, final File baseFolder) {
		try {
			final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
			if (compiler==null) return CompilerResult.error(DynamicFactory.getStatusText(DynamicStatus.NO_COMPILER));
			try (StandardJavaFileManager fileManager=compiler.getStandardFileManager(null,null,null)) {

				Iterable<? extends JavaFileObject> compilationUnits1=fileManager.getJavaFileObjects(file);

				final StringWriter writer=new StringWriter();

				final List<String> options=new ArrayList<>();
				options.add("-cp");
				options.add(baseFolder.toString());
				if (!isJava8()) options.add("--release=8");

				boolean success=false;
				ClassLoaderCache.globalCompilerLock.lock();
				try {
					success=compiler.getTask(writer,fileManager,null,options,null,compilationUnits1).call().booleanValue();
				} finally {
					ClassLoaderCache.globalCompilerLock.unlock();
				}

				final String result=writer.getBuffer().toString();
				if (result==null || result.trim().isEmpty()) return CompilerResult.success();
				if (success) return CompilerResult.warning(result); else return CompilerResult.error(result);

			} catch (IOException e) {
				return CompilerResult.error(e.getMessage());
			}

		} catch (NoClassDefFoundError e) {
			return CompilerResult.error(e.getMessage());
		}
	}

	/**
	 * Ergebnistyp der Kompilierung
	 * @see CompilerResult
	 */
	public enum CompilerResultType {
		/** Kompilierung erfolgreich ohne jede Warnung */
		SUCCESS,
		/** Kompilierung erfolgreich aber mit Warnung */
		SUCCESS_WARNING,
		/** Kompilierung fehlgeschlagen */
		ERROR
	}

	/**
	 * Status der Kompilierung
	 * @see DynamicClassInternalCompilerFullMemory#compileJavaToClass(File, File)
	 */
	public static class CompilerResult {
		/**
		 * Ergebnistyp
		 */
		public final CompilerResultType type;

		/**
		 * Warnung oder Fehlermeldung<br>
		 * (Ist <code>null</code>, wenn die Kompilierung ohne Warnung erfolgreich war.)
		 */
		public final String message;

		/**
		 * Konstruktor der Klasse<br>
		 * Die Klasse kann nicht direkt instanziert werden.
		 * Stattdessen stehen statische Factory-Methoden zur Verfügung
		 * @see #success()
		 * @see #warning(String)
		 * @see #error(String)
		 * @param type	Ergebnistyp
		 * @param message	Warnung oder Fehlermeldung
		 */
		private CompilerResult(final CompilerResultType type, final String message) {
			this.type=type;
			this.message=message;
		}

		/**
		 * Erzeugt ein Statusobjekt für den Fall, dass die Kompilierung ohne jede Warnung erfolgreich war.
		 * @return	Statusobjekt
		 */
		public static CompilerResult success() {
			return new CompilerResult(CompilerResultType.SUCCESS,null);
		}

		/**
		 * Erzeugt ein Statusobjekt für den Fall, dass die Kompilierung mit Warnung erfolgreich war.
		 * @param message	Warnmeldung
		 * @return	Statusobjekt
		 */
		public static CompilerResult warning(final String message) {
			return new CompilerResult(CompilerResultType.SUCCESS_WARNING,message);
		}

		/**
		 * Erzeugt ein Statusobjekt für den Fall, dass die Kompilierung fehlgeschlagen ist.
		 * @param message	Fehlermeldung
		 * @return	Statusobjekt
		 */
		public static CompilerResult error(final String message) {
			return new CompilerResult(CompilerResultType.ERROR,message);
		}
	}
}
