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
 * @see DynamicFactory#load(String)
 * @see DynamicFactory#testIntern(String)
 */
public class JavaCodeCache {
	/**
	 * Zuordnung von Skripttexten zu �bersetzten Methoden
	 * @see #getCachedMethod(String)
	 * @see #storeMethod(String, DynamicMethod)
	 */
	private final Map<String,DynamicMethod> map;

	/**
	 * Konstruktor der Klasse<br>
	 * Der Cache ist ein Singleton und kann daher nicht direkt instanziert werden.
	 * @see #getJavaCodeCache()
	 */
	private JavaCodeCache() {
		map=new HashMap<>();
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
	 * Pr�ft, ob f�r ein Skript bereits eine �bersetzung vorliegt
	 * und liefert diese ggf. zur�ck.
	 * @param script	Skript f�r das die �bersetzte Methode abgefragt werden soll
	 * @return	Liefert im Erfolgsfall die �bersetzte Methode, sonst <code>null</code>
	 */
	public DynamicMethod getCachedMethod(final String script) {
		synchronized(this) {
			final DynamicMethod dynamicMethod=map.get(script);
			if (dynamicMethod==null) return null;
			return new DynamicMethod(dynamicMethod);
		}
	}

	/**
	 * Speichert eine �bersetzte Methode f�r ein Skript im Cache
	 * @param script	Skript f�r das eine Methode hinterlegt werden soll
	 * @param method	�bersetzte Methode, die zu dem Skript gespeichert werden soll
	 */
	public void storeMethod(final String script, final DynamicMethod method) {
		synchronized(this) {
			map.put(script,method);
		}
	}
}
