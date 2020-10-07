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
package systemtools;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Leitet eine {@link ByteArrayOutputStream}-Ausgabe in ein {@link JTextArea} um.<br>
 * Die Klasse kann dabei z.B. als nachgelagerter Stream nach einem {@link PrintStream} folgen
 * und so Statusausgaben, die sonst auf die Konsole geleitet würden, entgegen nehmen.
 * @author Alexander Herzog
 */
public class JTextAreaOutputStream extends ByteArrayOutputStream {
	/** Textfeld in das die Ausgabe umgeleitet werden sollen */
	private final JTextArea textArea;
	/** Lange Zeilen zerteilen? */
	private final boolean lineWrap;

	/**
	 * Konstruktor der Klasse
	 * @param textArea	Textfeld in das die Ausgabe umgeleitet werden sollen
	 * @param lineWrap	Lange Zeilen zerteilen?
	 */
	public JTextAreaOutputStream(final JTextArea textArea, final boolean lineWrap) {
		this.textArea=textArea;
		this.lineWrap=lineWrap;
	}

	/**
	 * Zerlegt einen langen Text in 78 Zeichen lange Zeilen.
	 * @param text	Zu zerlegender Text
	 * @return	Text mit Zeilenumbrüchen
	 * @see #updateText()
	 */
	private String wrapText(final String text) {
		final StringBuilder sb=new StringBuilder();
		for (String line : text.split("\n")) {
			while (!line.isEmpty()) {
				if (line.length()<78) {sb.append(line); break;}
				sb.append(line.substring(0,78));
				line=line.substring(78).trim();
				if (!line.isEmpty()) sb.append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Text in dem {@link JTextArea} aktualisieren.
	 */
	public void updateText() {
		final String text=toString();
		SwingUtilities.invokeLater(()->textArea.setText(lineWrap?wrapText(text):text));
	}

	@Override
	public synchronized void write(int b) {
		super.write(b);
		updateText();
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) {
		super.write(b,off,len);
		updateText();
	}
}
