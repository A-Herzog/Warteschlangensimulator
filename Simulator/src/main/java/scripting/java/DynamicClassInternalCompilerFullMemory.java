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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import language.Language;
import mathtools.NumberTools;

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

	@SuppressWarnings("resource")
	@Override
	public Object prepare(String text) {
		/* Daten vorhanden? */
		if (text==null || text.trim().isEmpty()) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		/* Klassenname bestimmen */
		final String className=getClassName(text);
		if (className==null) return DynamicStatus.COMPILE_ERROR;

		/* Kompiler finden */
		if (!DynamicFactory.hasCompiler()) return DynamicStatus.NO_COMPILER;

		final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
		if (compiler==null) return DynamicStatus.NO_COMPILER;

		/* Vorbereiten */
		URLClassLoader urlLoader=null;
		if (additionalClassPath!=null) {
			try {
				final File folder=new File(additionalClassPath);
				urlLoader=new URLClassLoader(new URL[]{folder.toURI().toURL()});
			} catch (MalformedURLException e1) {
				return DynamicStatus.COMPILE_ERROR;
			}
		}

		try (final JavaFileManager fileManager=new ClassFileManager<JavaFileManager>(compiler.getStandardFileManager(null,null,null),urlLoader)) {
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
			final CompilationTask task=compiler.getTask(null,fileManager,diagnostics,options,null,compilationUnits);

			if (task.call()) {
				/* Klasse laden */
				return fileManager.getClassLoader(null).loadClass(className);
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
			if (sb.length()>0) setError(sb.toString());
			return DynamicStatus.COMPILE_ERROR;

		} catch (IOException e) {
			setError(e.getMessage());
			return DynamicStatus.COMPILE_ERROR;
		} catch (ClassNotFoundException e) {
			setError(e.getMessage());
			return DynamicStatus.LOAD_ERROR;
		}
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
