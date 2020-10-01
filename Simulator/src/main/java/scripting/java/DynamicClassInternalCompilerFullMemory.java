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

import java.io.IOException;
import java.util.Arrays;

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
	 */
	public DynamicClassInternalCompilerFullMemory(final DynamicSetup setup) {
		super(setup);
	}

	@Override
	public Object prepare(String text) {
		/* Daten vorhanden? */
		if (text==null || text.trim().isEmpty()) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		/* Klassenname bestimmen */
		final String className=getClassName(text);
		if (className==null) return DynamicStatus.COMPILE_ERROR;

		/* Kompiler finden */
		final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
		if (compiler==null) return DynamicStatus.NO_COMPILER;

		/* Vorbereiten */
		try (final JavaFileManager fileManager=new ClassFileManager<JavaFileManager>(compiler.getStandardFileManager(null,null,null))) {
			final DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
			final JavaFileObject file=new CharSequenceJavaFileObject(className,text);
			final Iterable<? extends JavaFileObject> compilationUnits=Arrays.asList(file);

			/* Übersetzen */
			final CompilationTask task=compiler.getTask(null,fileManager,diagnostics,null,null,compilationUnits);

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
				sb.append(String.format(Language.tr("Simulation.Java.Error.CompileError.Line"),lineNr-2)+":\n");
				sb.append(lines[(int)lineNr]);
				sb.append("\n");
				sb.append(String.format(Language.tr("Simulation.Java.Error.CompileError.Column"),colNr+1)+": ");
				sb.append(diagnostic.getMessage(NumberTools.getLocale()));
				sb.append("\n");
			}
			if (sb.length()>0) setError(sb.toString());
			return DynamicStatus.COMPILE_ERROR;

		} catch (IOException e) {
			return DynamicStatus.COMPILE_ERROR;
		} catch (ClassNotFoundException e) {
			return DynamicStatus.LOAD_ERROR;
		}
	}

	@Override
	protected DynamicStatus loadClass(Object classData) {
		if (!(classData instanceof Class<?>)) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		setLoadedClass((Class<?>)classData);
		return DynamicStatus.OK;
	}
}
