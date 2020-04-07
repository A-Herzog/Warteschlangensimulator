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

/**
 * Basisklasse für alle Klassen, die später in der JavaScript-Engine erreichbar sein sollen
 * @author Alexander Herzog
 */
public class JSBaseCommand {
	private final JSOutputWriter output;

	/**
	 * Konstruktor der Klasse <code>JSBaseCommand</code>
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 */
	public JSBaseCommand(final JSOutputWriter output) {
		this.output=output;
	}

	/**
	 * Gibt eine Meldung aus.
	 * @param line	Auszugebende Meldung
	 */
	protected final void addOutput(final String line) {
		if (output!=null) output.addOutput(line);
	}
}