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

/**
 * Einstellungen f�r das dynamische Laden von Methoden
 * @author Alexander Herzog
 * @see DynamicMethod
 */
public interface DynamicSetup {
	/**
	 * Art des Aufrufs des Kompilers
	 * @author Alexander Herzog
	 * @see DynamicSetup#getCompileMode()
	 */
	public enum CompileMode {
		/**
		 * javac.exe als externen Prozess aufrufen
		 */
		EXTERNAL_COMPILER(false,DynamicClassExternalCompiler.class),

		/**
		 * Java Kompiler �ber das API ansprechen
		 */
		INTERNAL_COMPILER(false,DynamicClassInternalCompiler.class),

		/**
		 * Java Kompiler �ber das API ansprechen, keine tempor�re java-Datei verwenden
		 */
		INTERNAL_COMPILER_NO_JAVA_FILE(false,DynamicClassInternalCompilerHalfMemory.class),

		/**
		 * Java Kompiler �ber das API ansprechen, �berhaupt keine tempor�ren Dateien verwenden
		 */
		INTERNAL_COMPILER_NO_FILES(true,DynamicClassInternalCompilerFullMemory.class);

		/**
		 * Werden tempor�re Dateien verwendet?
		 */
		final boolean inMemoryProcessing;

		/**
		 * Klasse, die zum Kompilieren und Laden der Klasse verwendet werden soll.
		 * @see DynamicClassBase
		 */
		final Class<? extends DynamicClassBase> dynamicLoaderClass;

		/**
		 * Konstruktor des Enum
		 * @param inMemoryProcessing	Werden tempor�re Dateien verwendet?
		 * @param dynamicLoaderClass	Klasse, die zum Kompilieren und Laden der Klasse verwendet werden soll.
		 */
		CompileMode(final boolean inMemoryProcessing, final Class<? extends DynamicClassBase> dynamicLoaderClass) {
			this.inMemoryProcessing=inMemoryProcessing;
			this.dynamicLoaderClass=dynamicLoaderClass;
		}
	}

	/**
	 * Liefert den Pfad zum JDK.
	 * @return	Pfad zum JDK
	 */
	String getJDKPath();

	/**
	 * Name des Unterverzeichnisses des System-Temp-Ordners, in dem die tempor�ren class-Dateien abgelegt werden sollen.
	 * @return	Unterverzeichnis des System-Temp-Ordners (darf nicht <code>null</code> oder leer sein)
	 */
	String getTempFolderName();

	/**
	 * Basisname f�r die zu erzeugende Klasse
	 * @return	Basisname f�r die zu erzeugende Klasse (darf nicht <code>null</code> oder leer sein)
	 */
	String getTempClassName();

	/**
	 * Liste mit Imports (ohne das "Import"-Schl�sselwort und ohne abschlie�endes ";")
	 * @param userImports	Optionale nutzerdefinierte Imports (kann <code>null</code> oder leer sein)
	 * @return	Liste mit Imports (darf <code>null</code> oder leer sein)
	 */
	String[] getImports(final String userImports);

	/**
	 * Zu verwendender Class-Path
	 * @return	Zu verwendender Class-Path (darf <code>null</code> oder leer sein)
	 */
	String getClassPath();

	/**
	 * Gibt an, ob der Kompiler direkt �ber das Java Compiler API oder als Datei/externer Prozess angesprochen werden soll.
	 * @return	Art des Aufrufs des Kompilers
	 * @see DynamicSetup.CompileMode
	 */
	CompileMode getCompileMode();
}
