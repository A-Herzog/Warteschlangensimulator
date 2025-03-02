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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Basisklasse, die es abgeleiteten Klassen erlaubt, dynamisch Klassen aus Texten oder aus java- oder aus class-Dateien zu laden
 * unter Verwendung von Dateisystem-basierenden Dateien für die java- und class-Dateien.
 * @author Alexander Herzog
 */
public abstract class DynamicClassFileBased extends DynamicClassBase {
	/** Name des Unterverzeichnisses des System-Temp-Ordners, in dem die temporären class-Dateien abgelegt werden sollen. */
	private final String tempFolderName;
	/** Zu verwendender Class-Path */
	private final String classPath;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 * @param additionalClassPath	Optionaler zusätzlicher über den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 */
	public DynamicClassFileBased(final DynamicSetup setup, final String additionalClassPath) {
		super(setup,additionalClassPath);
		tempFolderName=setup.getTempFolderName();
		classPath=setup.getClassPath();
	}

	/**
	 * Kompiliert eine java-Datei per Aufruf der javac.exe
	 * @param javaFile	java-Datei
	 * @param classPath	Optionaler zusätzlicher Klassenpfad (darf <code>null</code> sein)
	 * @param outputFolder	Ausgabeverzeichnis
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst ein Objekt vom Typ {@link DynamicStatus} oder einen String, der dann eine zusätzliche Fehlermeldung zum Typ {@link DynamicStatus#COMPILE_ERROR} liefert.
	 */
	protected abstract Object compile(final File javaFile, final String classPath, final File outputFolder);

	/**
	 * Erzeugt ggf. eine java-Datei im Dateisystem und ruft dann den Kompiler auf.
	 * @param text	Als java-Klasse anzusehender Text
	 * @param className	Name der neuen Klasse
	 * @param classPath	Optionaler zusätzlicher Klassenpfad (darf <code>null</code> sein)
	 * @param outputFolder	Ausgabeverzeichnis
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst ein Objekt vom Typ {@link DynamicStatus} oder einen String, der dann eine zusätzliche Fehlermeldung zum Typ {@link DynamicStatus#COMPILE_ERROR} liefert.
	 */
	protected Object createJavaFileAndCompile(final String text, final String className, final String classPath, final File outputFolder) {
		/* java-Datei anlegen */
		final File javaFile=new File(outputFolder,className+".java");
		try {
			Files.write(Paths.get(javaFile.toURI()),text.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE);
		} catch (IOException e) {
			return DynamicStatus.COMPILE_ERROR;
		}
		addFileToDeleteList(javaFile);

		/* Kompilieren */
		return compile(javaFile,classPath,outputFolder);
	}

	@Override
	public Object prepare(final String text) {
		/* Daten vorhanden? */
		if (text==null || text.trim().isEmpty()) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		/* Voraussetzungen ok? */
		if (!DynamicFactory.isWindows()) return DynamicStatus.UNSUPPORTED_OS;
		final File tempFolder=getTempFolder(tempFolderName);
		if (tempFolder==null) return DynamicStatus.NO_TEMP_FOLDER;

		/* Klassenname bestimmen */
		final String className=getClassName(text);
		if (className==null) return DynamicStatus.COMPILE_ERROR;

		/* Java-Datei anlegen und kompilieren */
		final Object result=createJavaFileAndCompile(text,className,classPath,tempFolder);

		/* Ergebnis interpretieren */
		if (result!=null) {
			if (result instanceof DynamicStatus) return result;
			if (result instanceof String) {setError((String)result); return DynamicStatus.COMPILE_ERROR;}
		}

		/* Klassendatei bereitstellen */
		final File classFile=new File(tempFolder,className+".class");
		if (!classFile.isFile()) return DynamicStatus.COMPILE_ERROR;
		addFileToDeleteList(classFile);
		return classFile;
	}

	/**
	 * Lädt eine Klasse aus einer class-Datei
	 * @param classFile	Zu ladende class-Datei
	 * @return	Liefert im Erfolgsfall ein Objekt von einem von {@link Class} abgeleiteten Typ zurück. Im Fehlerfall <code>null</code> oder ein String, der eine detaillierte Fehlermeldung enthält.
	 */
	private Object loadClassFile(final File classFile) {
		if (classFile==null) return null;

		/* URL an der sich die Klassendatei befindet */
		final List<URL> folderURLs=new ArrayList<>();
		try {
			folderURLs.add(classFile.getParentFile().toURI().toURL());
			if (additionalClassPath!=null) folderURLs.add(new File(additionalClassPath).toURI().toURL());
		} catch (MalformedURLException e1) {
			return null;
		}

		/* Name der Klasse */
		String className=classFile.getName();
		final int index=className.lastIndexOf('.');
		if (index<0) return null;
		className=className.substring(0,index);

		/* Klasse laden */
		try (final URLClassLoader loader=new URLClassLoader(folderURLs.toArray(URL[]::new))) {
			return loader.loadClass(className);
		} catch (ClassNotFoundException | UnsupportedClassVersionError | IOException e) {
			return e.getMessage();
		}
	}

	@Override
	protected DynamicStatus loadClass(Object classData) {
		if (!(classData instanceof File)) return DynamicStatus.NO_INPUT_FILE_OR_DATA;

		final Object result=loadClassFile((File)classData);

		if (result==null) return DynamicStatus.LOAD_ERROR;

		if (result instanceof String) {
			setError((String)result);
			return DynamicStatus.LOAD_ERROR;
		}

		if (result instanceof Class<?>) {
			setLoadedClass((Class<?>)result);
			return DynamicStatus.OK;
		}

		return DynamicStatus.LOAD_ERROR;
	}

	/**
	 * Erstellt ein temporäres Verzeichnis
	 * @param tempFolderName	Pfadsegment innerhalb des System-Temp-Ordners
	 * @return	Verzeichnis für temporäre Dateien oder <code>null</code>, wenn die Erstellung fehlgeschlagen ist
	 */
	protected static File getTempFolder(final String tempFolderName) {
		if (tempFolderName==null || tempFolderName.trim().isEmpty()) return null;
		String tempFolder=System.getProperty("java.io.tmpdir");
		if (!tempFolder.endsWith(File.separator)) tempFolder=tempFolder+File.separator;
		tempFolder=tempFolder+tempFolderName+File.separator;
		final File temp=new File(tempFolder);
		if (temp.isDirectory()) return temp;
		if (!temp.mkdir()) return null;
		return temp;
	}
}
