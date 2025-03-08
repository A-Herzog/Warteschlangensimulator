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
import java.io.InputStream;

/**
 * Ermöglicht es, dynamisch Klassen aus Texten oder aus java- oder aus class-Dateien zu laden.<br>
 * Es wird dafür der javac.exe aus dem JDK verwendet.
 * @author Alexander Herzog
 */
public class DynamicClassExternalCompiler extends DynamicClassFileBased {
	/** Pfad zum JDK */
	private final String JDKPath;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 * @param additionalClassPath	Optionaler zusätzlicher über den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 */
	public DynamicClassExternalCompiler(final DynamicSetup setup, final String additionalClassPath) {
		super(setup,additionalClassPath);
		JDKPath=setup.getJDKPath();
	}

	@Override
	protected Object compile(final File javaFile, final String classPath, final File outputFolder) {
		final File compiler=SimDynamicSetup.getCompilerFromJDKPath(JDKPath);
		if (compiler==null) return DynamicStatus.NO_COMPILER;

		final ProcessBuilder pb=new ProcessBuilder();
		if (classPath!=null && !classPath.isBlank()) {
			pb.command(compiler.toString(),"-encoding","UTF-8","-d",outputFolder.toString(),"-cp",classPath,javaFile.toString());
		} else {
			pb.command(compiler.toString(),"-encoding","UTF-8","-d",outputFolder.toString(),javaFile.toString());
		}
		Process process;
		try {
			process=pb.start();
		} catch (IOException e) {
			return DynamicStatus.COMPILE_ERROR;
		}
		int result=0;
		try {
			result=process.waitFor();
		} catch (InterruptedException e) {
			return DynamicStatus.COMPILE_ERROR;
		}

		try {
			try (InputStream input=process.getInputStream()) {
				final int available=input.available();
				final byte[] b=new byte[available];
				int read=0;
				while (read<available) read+=input.read(b,read,available-read);
				final String s=new String(b).trim();
				if (!s.isEmpty()) return s;
			}

			try (InputStream error=process.getErrorStream()) {
				final int available=error.available();
				final byte[] b=new byte[available];
				int read=0;
				while (read<available) read+=error.read(b,read,available-read);
				final String s=new String(b).trim();
				if (!s.isEmpty()) return s;
			}
		} catch (IOException e) {
			return DynamicStatus.COMPILE_ERROR;
		}

		if (result!=0) return DynamicStatus.COMPILE_ERROR;
		return null;
	}
}
