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
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public static String compileJavaToClass(final File file, final File baseFolder) {
		try {
			final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
			if (compiler==null) return DynamicFactory.getStatusText(DynamicStatus.NO_COMPILER);
			try (StandardJavaFileManager fileManager=compiler.getStandardFileManager(null,null,null)) {

				Iterable<? extends JavaFileObject> compilationUnits1=fileManager.getJavaFileObjects(file);

				final StringWriter writer=new StringWriter();

				final List<String> options=new ArrayList<>();
				options.add("-cp");
				options.add(baseFolder.toString());
				if (!isJava8()) options.add("--release=8");

				compiler.getTask(writer,fileManager,null,options,null,compilationUnits1).call().booleanValue();

				final String result=writer.getBuffer().toString();
				if (result==null || result.trim().isEmpty()) return null;
				return result;

			} catch (IOException e) {
				return e.getMessage();
			}

		} catch (NoClassDefFoundError e) {
			return e.getMessage();
		}
	}
}
