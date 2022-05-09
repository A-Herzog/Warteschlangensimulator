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

import scripting.js.JSCommandSystem;

/**
 * Klasse zur Speicherung von stationsbezogenen und modellweiten Skript-Daten
 * @author Alexander Herzog
 * @see SystemImpl#mapLocal
 * @see JSCommandSystem#mapLocal
 */
public class RuntimeData extends ThreadLocal<Map<String,Object>> {
	/**
	 * Beim letzten Aufruf von {@link #get()} ermittelter Wert
	 * @see #get()
	 * @see #getLastRequest()
	 */
	private Map<String,Object> lastRequest;

	/**
	 * Konstruktor der Klasse
	 */
	public RuntimeData() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	protected Map<String,Object> initialValue() {
		return new HashMap<>();
	}

	@Override
	public Map<String,Object> get() {
		return lastRequest=super.get();
	}

	/**
	 * Liefert den Wert, der bei der letzten Abfrage per {@link #get()}
	 * geliefert wurde (kann <code>null</code> sein, wenn {@link #get()}
	 * noch nie aufgerufen wurde).
	 * @return Objekt gemäß dem letzten {@link #get()}-Aufruf (muss also nicht zu diesem Thread gehören)
	 */
	public Map<String,Object> getLastRequest() {
		return lastRequest;
	}
}
