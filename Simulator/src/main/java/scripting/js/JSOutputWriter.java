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

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * Diese Klasse nimmt Ausgaben der Skripte entgegen
 * und puffert diese oder leitet sie an ein Lambda-Objekt weiter.
 * @author Alexander Herzog
 */
public class JSOutputWriter extends Writer {
	private static final int maxOutputChars=10_000_000;

	private final StringBuilder results;
	private final StringBuilder outputCallbackBuilder;
	private final Consumer<String> outputCallback;

	/**
	 * Konstruktor der Klasse
	 * @param outputCallback	Wird hier ein Wert ungleich <code>null</code> angegeben, so werden die Ausgaben hier weitergeleitet. Sonst werden sie intern gepuffert.
	 */
	public JSOutputWriter(final Consumer<String> outputCallback) {
		super();
		this.outputCallback=outputCallback;
		if (outputCallback==null) {
			results=new StringBuilder();
			outputCallbackBuilder=null;
		} else {
			results=null;
			outputCallbackBuilder=new StringBuilder();
		}
	}

	/**
	 * Werden die Ausgaben intern gepuffert, so wird dieser Puffer durch diesen Aufruf geleert.
	 */
	public void reset() {
		if (results!=null && results.length()>0) results.setLength(0);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (outputCallback==null) {
			if (results.length()<maxOutputChars) results.append(cbuf,off,len);
		} else {
			outputCallbackBuilder.append(cbuf,off,len);
			outputCallback.accept(outputCallbackBuilder.toString());
			outputCallbackBuilder.setLength(0);
		}
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		flush();
	}

	/**
	 * Fügt einen String zu der Ausgabe hinzu.
	 * @param line	Auszugebender String
	 */
	public void addOutput(final String line) {
		if (outputCallback==null) {
			if (results.length()<maxOutputChars) results.append(line);
		} else {
			outputCallback.accept(line);
		}
	}

	/**
	 * Fügt eine Fehlermeldung zur Ausgabe hinzu.
	 * @param e	Exception deren Daten ausgegeben werden sollen.
	 */
	public void addExceptionMessage(final Exception e) {
		if (outputCallback==null) {
			if (results.length()>0) results.append("\n");
			results.append(e.getMessage());
			results.append("\n");
		}  else {
			outputCallback.accept(e.getMessage());
			outputCallback.accept("\n");
		}
	}

	/**
	 * Liefert im Fall der internen Pufferung die Ergebnisse zurück.
	 * @return	Ergebnisse; Puffer wird durch die Ausgabe nicht verändert.
	 */
	public String getResults() {
		if (results==null) return "";
		return results.toString();
	}
}