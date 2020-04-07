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
package ui.script;

import javax.swing.Icon;
import javax.swing.JTextArea;

import ui.script.ScriptPopup.XMLMode;

/**
 * Popupmenü-Eintrag zur Ausgabe eines Statistik-XML-Ausgabebefehls
 * @author Alexander Herzog
 * @see ScriptPopup
 * @see ScriptPopupItem
 */
public class ScriptPopupItemStatistics extends ScriptPopupItem {
	private final XMLMode xmlMode;
	private final String xmlPath;
	private final ScriptPopup.ScriptMode scriptMode;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 * @param xmlMode	Ausgabe als Text oder als Zahl
	 * @param xmlPath	XML-Pfad
	 * @param scriptMode	Ausgabe als Java- oder als Javascript-Befehl
	 */
	public ScriptPopupItemStatistics(final String name, final String hint, final Icon icon, final XMLMode xmlMode, final String xmlPath, final ScriptPopup.ScriptMode scriptMode) {
		super(name,hint,icon);
		this.xmlMode=xmlMode;
		this.xmlPath=xmlPath;
		this.scriptMode=scriptMode;
	}

	private void insert(final String text, final JTextArea textArea, final Runnable update) {
		if (textArea==null) return;

		insertTextIntoTextArea(textArea,text);

		if (update!=null) update.run();
	}

	@Override
	public void insertIntoTextArea(final JTextArea textArea, final Runnable update) {
		if (scriptMode==ScriptPopup.ScriptMode.Javascript) {
			if (xmlMode==XMLMode.XML_NUMBER || xmlMode==XMLMode.XML_NUMBER_TIME) {
				insert("Output.println(Statistics.xmlNumber(\""+xmlPath.replace("\"","\\\"")+"\"));",textArea,update);
			} else {
				insert("Output.println(Statistics.xml(\""+xmlPath.replace("\"","\\\"")+"\"));",textArea,update);
			}
		}

		if (scriptMode==ScriptPopup.ScriptMode.Java) {
			if (xmlMode==XMLMode.XML_NUMBER || xmlMode==XMLMode.XML_NUMBER_TIME) {
				insert("sim.getOutput().println(sim.getStatistics().xmlNumber(\""+xmlPath.replace("\"","\\\"")+"\"));",textArea,update);
			} else {
				insert("sim.getOutput().println(sim.getStatistics().xml(\""+xmlPath.replace("\"","\\\"")+"\"));",textArea,update);
			}
		}
	}
}
