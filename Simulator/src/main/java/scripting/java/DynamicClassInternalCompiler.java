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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Ermöglicht es, dynamisch Klassen aus Texten oder aus java- oder aus class-Dateien zu laden.<br>
 * Es wird dafür das Java Compiler API verwendet.
 * @author Alexander Herzog
 */
public class DynamicClassInternalCompiler extends DynamicClassFileBased {
	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 * @param additionalClassPath	Optionaler zusätzlicher über den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 */
	public DynamicClassInternalCompiler(final DynamicSetup setup, final String additionalClassPath) {
		super(setup,additionalClassPath);
	}

	@Override
	protected Object compile(final File javaFile, final String classPath, final File outputFolder) {
		if (!DynamicFactory.hasCompiler()) return DynamicStatus.NO_COMPILER;

		final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
		if (compiler==null) return DynamicStatus.NO_COMPILER;

		List<String> arguments=new ArrayList<>();
		arguments.add("-encoding");
		arguments.add("UTF-8");
		arguments.add("-d");
		arguments.add(outputFolder.toString());

		if (classPath!=null && !classPath.trim().isEmpty()) {
			arguments.add("-cp");
			arguments.add(classPath);
		}
		arguments.add(javaFile.toString());

		final ByteArrayOutputStream out=new ByteArrayOutputStream();
		final ByteArrayOutputStream err=new ByteArrayOutputStream();
		final int result=compiler.run(null,out,err,arguments.toArray(new String[0]));

		String s;

		s=new String(out.toByteArray()).trim();
		if (!s.isEmpty()) return s;

		s=new String(err.toByteArray()).trim();
		if (!s.isEmpty()) return s;

		if (result!=0) return DynamicStatus.COMPILE_ERROR;
		return null;
	}
}
