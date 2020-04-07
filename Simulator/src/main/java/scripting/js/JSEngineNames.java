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
package scripting.js;

import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

/**
 * Liste der m�glichen JS-Engines
 * @author Alexander Herzog
 */
public enum JSEngineNames {
	/**
	 * Engine durch das Programm ausw�hlen lassen.
	 */
	DEFAULT(null),

	/**
	 * Mozilla-Rhino (intern, daher immer verf�gbar; langsam)
	 */
	RHINO("Rhino"),

	/**
	 * Nashorn-Engine des JDK (in Zukunft evtl. nicht mehr unterst�tzt; scnell)
	 */
	NASHORN("Nashorn"),

	/**
	 * GraalJS-Engine (nur verf�gbar bei Verwendung von GraalVM; l�uft �ber Script-Manager)
	 */
	GRAALJS("GraalJS"),

	/**
	 * GraalJS-Engine (nur verf�gbar bei Verwendung von GraalVM; direkter Aufruf ohne Script-Manager)
	 */
	GRAALJSNative("GraalJSNative");

	/**
	 * Name der Engine als String
	 */
	public final String name;

	JSEngineNames(final String name) {this.name=name;}

	/**
	 * Liefert die Liste der verf�gbaren JS-Engines
	 * @return	Liste der verf�gbaren JS-Engines
	 */
	public static Set<JSEngineNames> available() {
		final Set<JSEngineNames> engines=new HashSet<>();
		final ScriptEngineManager manager=new ScriptEngineManager();

		for (ScriptEngineFactory factory: manager.getEngineFactories()) {
			for (String name: factory.getNames()) if (name!=null) {
				for (JSEngineNames engineName: values()) if (engineName.name!=null) {
					if (name.equalsIgnoreCase(engineName.name)) engines.add(engineName);
				}
			}
		}

		engines.add(RHINO); /* Nicht in der Liste der verf�gbaren Engines, daher manuell hinzuf�gen */
		if (engines.contains(GRAALJS)) engines.add(GRAALJSNative); /* Erkannt wird nur Graal �ber ScriptEngineFactory, aber dann k�nnen wir es auch direkt ansprechen. */

		return engines;
	}


	/**
	 * Liefert ein Engine-Name-Objekt auf Basis einer Zeichenkette.
	 * Dieses kann vom Konstruktor dieser Klasse verwendet werden.
	 * @param name	Zeichenkette (darf auch leer oder <code>null</code> sein)
	 * @return	Engine-Name-Objekt
	 */
	public static JSEngineNames fromName(final String name) {
		if (name==null) return DEFAULT;
		final String lower=name.toLowerCase();
		for (JSEngineNames engineName: values()) if (engineName.name!=null && lower.equals(engineName.name.toLowerCase())) return engineName;
		for (JSEngineNames engineName: values()) if (engineName.name!=null && lower.contains(engineName.name.toLowerCase())) return engineName;
		return null;
	}

	/**
	 * Findet unter den verf�gbaren JS-Engines diejenige, die am besten zur Nutzerauswahl passt
	 * @param userSelect	Vom Nutzer gew�hlte JS-Engine
	 * @return	Zu verwendende JS-Engine
	 */
	public static JSEngineNames bestMatch(final JSEngineNames userSelect) {
		final Set<JSEngineNames> list=JSEngineNames.available();
		if (list.contains(userSelect)) return userSelect;

		if (list.contains(NASHORN)) return NASHORN;
		if (list.contains(GRAALJSNative)) return GRAALJSNative;
		if (list.contains(GRAALJS)) return GRAALJS;
		if (list.contains(RHINO)) return RHINO;
		return null;
	}
}
