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
package ui.modeleditor.elements;

import simulator.editmodel.EditModel;

/**
 * Elemente, die ein Skript beinhalten, implementieren dieses Interface.
 * Darüber kann das Skript abgerufen werden, ohne dass der Aufrufer
 * wissen muss, um was für eine Element es sich genau handelt.
 * @author Alexander Herzog
 */
public interface ElementWithScript {
	/**
	 * Zu verwendende Programmiersprache für das Skript
	 * @author Alexander Herzog
	 * @see ModelElementSetJS#getMode()
	 * @see ModelElementSetJS#setMode(ScriptMode)
	 */
	public enum ScriptMode {
		/** Javascript als Sprache verwenden */
		Javascript,
		/** Java als Sprache verwenden */
		Java
	}

	/**
	 * Gibt die Skriptsprache an
	 * @return	Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	ScriptMode getMode();

	/**
	 * Stellt die Skriptsprache ein.
	 * @param mode	Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	void setMode(final ScriptMode mode);

	/**
	 * Liefert das aktuelle Skript.
	 * @return	Aktuelles Skript
	 */
	String getScript();

	/**
	 * Setzt ein neues Skript.
	 * @param script	Neues Skript
	 */
	void setScript(final String script);

	/**
	 * Prüft, ob das Skript auf externe Daten zugreift, was eine
	 * Parallelisierung der Simulation verhindern würde.
	 * @return	Kann das Skript parallel in mehreren Instanzen ausgeführt werden?
	 * @see EditModel#getSingleCoreReason()
	 */
	default boolean scriptRequiresSingleCoreMode() {
		final ScriptMode mode=getMode();
		final String script=getScript();
		if (mode==null || script==null) return false;

		for (String line: script.split("\n")) {
			final String lower=line.toLowerCase();
			switch (mode) {
			case Javascript:
				if (lower.contains("system.getinput(")) return true;
				if (lower.contains("simulation.getinput(")) return true;
				if (lower.contains("system.execute(")) return true;
				if (lower.contains("simulation.execute(")) return true;
				if (lower.contains("system.executeAndReturnOutput(")) return true;
				if (lower.contains("simulation.executeAndReturnOutput(")) return true;
				if (lower.contains("system.executeAndWait(")) return true;
				if (lower.contains("simulation.executeAndWait(")) return true;
				break;
			case Java:
				if (lower.contains("getruntime().getinput(")) return true;
				if (lower.contains("getruntime().execute(")) return true;
				if (lower.contains("getruntime().executeAndReturnOutput(")) return true;
				if (lower.contains("getruntime().executeAndWait(")) return true;
				if (lower.contains("getinputvalue().getinput(")) return true;
				if (lower.contains("getsystem().runplugin(")) return true;
				break;
			default:
				break;
			}
		}

		return false;
	}
}
