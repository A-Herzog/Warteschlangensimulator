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

import javax.swing.JTextArea;

import org.w3c.dom.Document;

import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.statistics.StatisticViewerFastAccessDialog;

/**
 * Popupmenü-Eintrag zur Auswahl eines XML-Elements über einen Dialog
 * @author Alexander Herzog
 * @see ScriptPopup
 * @see ScriptPopupItem
 */
public class ScriptPopupItemXML extends ScriptPopupItem {
	/** EditModel-Objekt aus dessen XML-Repräsentation die Daten ausgewählt werden sollen (kann <code>null</code> sein) */
	private final EditModel model;
	/** Statistik-Objekt aus dessen XML-Repräsentation die Daten ausgewählt werden sollen (kann <code>null</code> sein) */
	private final Statistics statistics;
	/** Hilfe-Runnable für den Auswahldialog */
	private final Runnable help;
	/** Ausgabe als Java- oder als Javascript-Befehl */
	private final ScriptPopup.ScriptMode scriptMode;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param statistics	Statistik-Objekt aus dessen XML-Repräsentation die Daten ausgewählt werden sollen
	 * @param help	Hilfe-Runnable für den Auswahldialog
	 * @param scriptMode	Ausgabe als Java- oder als Javascript-Befehl
	 */
	public ScriptPopupItemXML(final String name, final String hint, final Statistics statistics, final Runnable help, final ScriptPopup.ScriptMode scriptMode) {
		super(name,hint,Images.SCRIPT_RECORD_XML.getIcon());
		model=null;
		this.statistics=statistics;
		this.help=help;
		this.scriptMode=scriptMode;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param model	EditModel-Objekt aus dessen XML-Repräsentation die Daten ausgewählt werden sollen
	 * @param help	Hilfe-Runnable für den Auswahldialog
	 * @param scriptMode	Ausgabe als Java- oder als Javascript-Befehl
	 */
	public ScriptPopupItemXML(final String name, final String hint, final EditModel model, final Runnable help, final ScriptPopup.ScriptMode scriptMode) {
		super(name,hint,Images.SCRIPT_RECORD_XML.getIcon());
		this.model=model;
		statistics=null;
		this.help=help;
		this.scriptMode=scriptMode;
	}

	private String getBaseCommand() {
		switch (scriptMode) {
		case Javascript:
			if (model!=null) return "Model.xml"; else return "Statistics.xml";
		case Java:
			if (model!=null) return "sim.getModel().xml"; else return "sim.getStatistics().xml";
		default:
			return "";
		}
	}

	private String getOutputBaseCommand() {
		switch (scriptMode) {
		case Javascript: return "Output";
		case Java: return "sim.getOutput()";
		default: return "";
		}
	}

	@Override
	public void insertIntoTextArea(final JTextArea textArea, final Runnable update) {
		Document doc=null;
		if (model!=null) doc=model.saveToXMLDocument();
		if (statistics!=null) doc=statistics.saveToXMLDocument();
		if (doc==null) return;

		final StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(null,doc,help,false);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		String xmlSelector=dialog.getXMLSelector();
		xmlSelector=getBaseCommand()+"(\""+xmlSelector.replace("\"","\\\"")+"\")";
		int insertType=dialog.getInsertType();
		if (insertType==0 || insertType==1) xmlSelector=getOutputBaseCommand()+".println("+xmlSelector+");"; else xmlSelector=xmlSelector+";";

		if (insertType==1 || insertType==3) {
			insertTextIntoTextArea(textArea,xmlSelector);
		} else {
			String s=textArea.getText();
			if (!s.endsWith("\n")) s+="\n";
			s+=xmlSelector;
			textArea.setText(s);
			textArea.requestFocus();
		}

		if (update!=null) update.run();
	}
}
