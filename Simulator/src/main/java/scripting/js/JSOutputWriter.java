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
	/**
	 * Maximale Gr��e f�r den {@link #results} Puffer.
	 * @see #results
	 */
	private static final int maxOutputChars=10_000_000;

	/**
	 * Interner Puffer f�r die Ausgaben
	 * (f�r den Fall <code>outputCallback==null</code>).
	 * @see #getResults()
	 */
	private final StringBuilder results;

	/**
	 * Gibt, an ob Ausgaben in {@link #resultsLine} oder {@link #results} erfolgen.
	 */
	private int resultsLineMode;

	/**
	 * Interner Puffer, falls es nur eine Zeile gibt
	 * (f�r den Fall <code>outputCallback==null</code>).
	 * @see #getResults()
	 */
	private String resultsLine;

	/**
	 * Tempor�rer {@link StringBuilder} f�r Ausgaben,
	 * die an {@link #outputCallback} weitergeleitet
	 * werden sollen.
	 * @see #outputCallback
	 */
	private final StringBuilder outputCallbackBuilder;

	/**
	 * Wird hier ein Wert ungleich <code>null</code> angegeben,
	 * so werden die Ausgaben hier weitergeleitet.
	 * Sonst werden sie intern gepuffert.
	 */
	private final Consumer<String> outputCallback;

	/**
	 * Konstruktor der Klasse
	 * @param outputCallback	Wird hier ein Wert ungleich <code>null</code> angegeben, so werden die Ausgaben hier weitergeleitet. Sonst werden sie intern gepuffert.
	 */
	public JSOutputWriter(final Consumer<String> outputCallback) {
		super();
		this.outputCallback=outputCallback;
		if (outputCallback==null) {
			resultsLine=null;
			resultsLineMode=0;
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
		resultsLine=null;
		resultsLineMode=0;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (outputCallback==null) {
			if (resultsLineMode==1) results.append(resultsLine);
			resultsLineMode=2;
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
	 * F�gt einen String zu der Ausgabe hinzu.
	 * @param line	Auszugebender String
	 */
	public void addOutput(final String line) {
		if (outputCallback==null) {
			switch (resultsLineMode) {
			case 0:
				resultsLine=line;
				resultsLineMode=1;
				break;
			case 1:
				results.append(resultsLine);
				if (results.length()<maxOutputChars) results.append(line);
				resultsLineMode=2;
				break;
			case 2:
				if (results.length()<maxOutputChars) results.append(line);
				break;
			}
		} else {
			outputCallback.accept(line);
		}
	}

	/**
	 * F�gt eine Fehlermeldung zur Ausgabe hinzu.
	 * @param e	Exception deren Daten ausgegeben werden sollen.
	 */
	public void addExceptionMessage(final Exception e) {
		if (outputCallback==null) {
			if (resultsLineMode==1) results.append(resultsLine);
			resultsLineMode=2;
			if (results.length()>0) results.append("\n");
			results.append(e.getMessage());
			results.append("\n");
		}  else {
			outputCallback.accept(e.getMessage());
			outputCallback.accept("\n");
		}
	}

	/**
	 * Liefert im Fall der internen Pufferung die Ergebnisse zur�ck.
	 * @return	Ergebnisse; Puffer wird durch die Ausgabe nicht ver�ndert.
	 */
	public String getResults() {
		if (resultsLineMode==1) return resultsLine;
		if (results==null) return "";
		return results.toString();
	}
}