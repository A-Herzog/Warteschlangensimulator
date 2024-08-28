/**
 * Copyright 2024 Alexander Herzog
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
package tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import systemtools.SetupBase;

/**
 * Liefert eine eindeutige ID des Systems.
 */
public class SystemID {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse liefert nur statische Methoden und kann nicht instanziert werden.
	 */
	private SystemID() {}

	/**
	 * Liefert eine eindeutige ID des Systems.
	 * @return	System ID (stets 32 Zeichen lang)
	 */
	public static String get() {
		final String osName=System.getProperty("os.name").toLowerCase();
		if (osName.contains("windows")) return cleanID(getWindows());
		if (osName.contains("linux")) return cleanID(getLinux());
		return cleanID(null);
	}

	/**
	 * Formatiert eine ausgelesene ID in eine 32 Zeichen lange Zeichenkette.
	 * @param id	Ausgelesene ID (kann <code>null</code> oder leer sein oder auch "-"-Zeichen enthalten)
	 * @return	32 Zeichen lange hexadezimale ID (in Kleinbuchstaben)
	 */
	private static String cleanID(String id) {
		if (id==null) id="";
		id=id.replace("-","");
		while (id.length()<32) id+="0";
		return id.toLowerCase();
	}

	/**
	 * Pfad zum "MachineGuid" Feld in der Registry
	 * @see #MACHINE_ID_KEY
	 * @see #getWindows()
	 */
	private static final String MACHINE_ID_REG_PATH="HKLM\\SOFTWARE\\Microsoft\\Cryptography";

	/**
	 * "MachineGuid" Schlüssel in der Registry
	 * @see #MACHINE_ID_REG_PATH
	 * @see #getWindows()
	 */
	private static final String MACHINE_ID_KEY="MachineGuid";

	/**
	 * Liefert unter Windows eine eindeutige ID für das System.
	 * @return	System ID (ist im Fehlerfall <code>null</code>)
	 */
	private static String getWindows() {
		return SetupBase.processRegistryResult(MACHINE_ID_REG_PATH,MACHINE_ID_KEY,SetupBase.getRegistryValue(MACHINE_ID_REG_PATH,MACHINE_ID_KEY));
	}

	/**
	 * Liefert unter Linux eine eindeutige ID für das System.
	 * @return	System ID (ist im Fehlerfall <code>null</code>)
	 */
	private static String getLinux() {
		final var file=new File(File.separator+"etc"+File.separator+"machine-id");
		try {
			return Files.readAllLines(file.toPath()).stream().filter(line->!line.isBlank()).findFirst().orElse(null);
		} catch (IOException e) {
			return null;
		}
	}
}
