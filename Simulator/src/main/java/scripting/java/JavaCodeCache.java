/**
 * Copyright 2021 Alexander Herzog
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

import java.util.HashMap;
import java.util.Map;

/**
 * Diese Klasse h�lt bereits einmal �bersetzten Java-Code vor, so dass derselbe
 * Skriptcode nicht immer wieder erneut �bersetzt werden muss.
 * @author Alexander Herzog
 * @see DynamicFactory#load(String, String, String)
 * @see DynamicFactory#testIntern(String, String, String)
 */
public class JavaCodeCache {
	/**
	 * Zuordnung von Skripttexten zu �bersetzten Methoden (inkl. der M�glichkeit verschiedene Imports zu ber�cksichtigen)
	 * @see #getCachedMethod(String, String, String)
	 * @see #storeMethod(String, String, DynamicMethod)
	 */
	private final Map<String,Map<String,DynamicMethod>> map;

	/**
	 * Zuordnung von Skripttexten zu �bersetzten Methoden (f�r den Fall, dass keine nutzerdefinierten Imports vorhanden sind)
	 * @see #getCachedMethod(String, String, String)
	 * @see #storeMethod(String, String, DynamicMethod)
	 */
	private final Map<String,DynamicMethod> mapNoUserImports;

	/**
	 * Konstruktor der Klasse<br>
	 * Der Cache ist ein Singleton und kann daher nicht direkt instanziert werden.
	 * @see #getJavaCodeCache()
	 */
	private JavaCodeCache() {
		map=new HashMap<>();
		mapNoUserImports=new HashMap<>();
	}

	/**
	 * Instanz dieses Singletons
	 * @see #getJavaCodeCache()
	 */
	private static JavaCodeCache javaCodeCache;

	/**
	 * Liefert die Singleton-Instanz dieses Caches.
	 * @return	Instanz dieses Caches
	 */
	public static synchronized JavaCodeCache getJavaCodeCache() {
		if (javaCodeCache==null) javaCodeCache=new JavaCodeCache();
		return javaCodeCache;
	}

	/**
	 * L�scht den Cache.
	 */
	public synchronized void clearCache() {
		map.clear();
		mapNoUserImports.clear();
	}

	/**
	 * Pr�ft, ob f�r ein Skript bereits eine �bersetzung vorliegt
	 * und liefert diese ggf. zur�ck.
	 * @param script	Skript f�r das die �bersetzte Methode abgefragt werden soll
	 * @param imports	Optionale nutzerdefinierte Imports (kann <code>null</code> oder leer sein)
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall die �bersetzte Methode, sonst <code>null</code>
	 */
	public DynamicMethod getCachedMethod(final String script, final String imports, final String additionalClassPath) {
		synchronized(this) {
			final DynamicMethod dynamicMethod;
			if (imports==null || imports.isBlank()) {
				dynamicMethod=mapNoUserImports.get(script);
			} else {
				final Map<String,DynamicMethod> methodsMap=map.computeIfAbsent(imports,i->new HashMap<>());
				dynamicMethod=methodsMap.get(script);
			}
			if (dynamicMethod==null) return null;
			return new DynamicMethod(dynamicMethod,additionalClassPath);
		}
	}

	/**
	 * Speichert eine �bersetzte Methode f�r ein Skript im Cache
	 * @param script	Skript f�r das eine Methode hinterlegt werden soll
	 * @param imports	Optionale nutzerdefinierte Imports (kann <code>null</code> oder leer sein)
	 * @param method	�bersetzte Methode, die zu dem Skript gespeichert werden soll
	 */
	public void storeMethod(final String script, final String imports, final DynamicMethod method) {
		synchronized(this) {
			if (imports==null || imports.isBlank()) {
				mapNoUserImports.put(script,method);
			} else {
				final Map<String,DynamicMethod> methodsMap=map.computeIfAbsent(imports,i->new HashMap<>());
				methodsMap.put(script,method);
			}
		}
	}
}
