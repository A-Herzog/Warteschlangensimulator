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
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import language.Language;
import mathtools.NumberTools;

/**
 * Ermöglicht es, dynamisch Klassen aus Texten oder aus java- oder aus class-Dateien zu laden.<br>
 * Es wird dafür das Java Compiler API verwendet und die java-Eingabedatei nur im Arbeitsspeicher gehalten.
 * @author Alexander Herzog
 */
public class DynamicClassInternalCompilerHalfMemory extends DynamicClassFileBased {
	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 */
	public DynamicClassInternalCompilerHalfMemory(final DynamicSetup setup) {
		super(setup);
	}

	@Override
	protected Object createJavaFileAndCompile(final String text, final String className, final String classPath, final File outputFolder) {
		/* Kompilieren */
		return compileFullyInternal(className,text,outputFolder);
	}

	/**
	 * Kompiliert eine java-Datei mit Hilfe des Java Compiler API und speicherbasierten Dateien
	 * @param javaClassName	Klassenname der Klasse
	 * @param javaText	Text, der den Inhalt der java-Datei darstellt
	 * @param outputFolder	Ausgabeverzeichnis
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst ein Objekt vom Typ {@link DynamicStatus} oder einen String, der dann eine zusätzliche Fehlermeldung zum Typ {@link DynamicStatus#COMPILE_ERROR} liefert.
	 */
	private Object compileFullyInternal(final String javaClassName, final String javaText, final File outputFolder) {
		final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
		if (compiler==null) return DynamicStatus.NO_COMPILER;

		final DiagnosticCollector<JavaFileObject> diagnostics=new DiagnosticCollector<>();
		final JavaFileObject file=new CharSequenceJavaFileObject(javaClassName,javaText);
		final Iterable<? extends JavaFileObject> compilationUnits=Arrays.asList(file);
		final Iterable<String> options=Arrays.asList("-d",outputFolder.toString());

		final CompilationTask task=compiler.getTask(null,null,diagnostics,options,null,compilationUnits);

		if (task.call()) return null;

		final StringBuilder sb=new StringBuilder();
		for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
			if (diagnostic.getKind()!=javax.tools.Diagnostic.Kind.ERROR) continue;
			long lineNr=diagnostic.getLineNumber();
			final long colNr=diagnostic.getColumnNumber();
			if (lineNr<0 || colNr<0) continue;
			lineNr--; /* Vorspann berücksichtigen */
			final String[] lines=javaText.split("\\n");
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
	}

	@Override
	protected Object compile(File javaFile, String classPath, File outputFolder) {
		/* Wird von dieser Klasse nicht verwendet, da createJavaFileAndCompile überschrieben wurde. */
		return null;
	}
}
