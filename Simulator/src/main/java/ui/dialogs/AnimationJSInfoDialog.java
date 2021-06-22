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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import language.Language;
import simcore.SimData;
import simulator.logging.CallbackLoggerWithJS;
import simulator.logging.CallbackLoggerWithJS.JSData;
import systemtools.BaseDialog;
import ui.AnimationPanel;
import ui.help.Help;

/**
 * Zeigt während der Animation eines Modells Informationen zur Skript-Ausführung
 * (Rückgabewerte) an.
 * @author Alexander Herzog
 * @see AnimationPanel
 */
public class AnimationJSInfoDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5168396052992308637L;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param data	Übersicht über die Ergebnisse der letzten Skript-Ausführungen
	 */
	public AnimationJSInfoDialog(final Component owner, final List<CallbackLoggerWithJS.JSData> data) {
		super(owner,Language.tr("Animation.JSResults.Title"));
		final JPanel content=createGUI(()->Help.topicModal(this.owner,"AnimationJS"));

		/* Liste */
		final JList<CallbackLoggerWithJS.JSData> list=new JList<>(data.toArray(new CallbackLoggerWithJS.JSData[0]));
		list.setCellRenderer(new JSDataListCellRenderer());
		content.setLayout(new BorderLayout());
		content.add(new JScrollPane(list),BorderLayout.CENTER);

		/* Starten */
		setMinSizeRespectingScreensize(500,700);
		setSizeRespectingScreensize(500,700);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Renderer für einen einzelnen Eintrag in der Liste der JS-Ergebnisse.
	 */
	private static class JSDataListCellRenderer implements ListCellRenderer<CallbackLoggerWithJS.JSData> {
		/**
		 * Standard-Zellen-Renderer;
		 * wird in {@link #getListCellRendererComponent(JList, JSData, int, boolean, boolean)} angepasst.
		 */
		private final DefaultListCellRenderer defaultRenderer=new DefaultListCellRenderer();

		/**
		 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
		 * HTML-Entitäten um.
		 * @param text	Umzuwandelnder Text
		 * @return	Umgewandelter Text
		 */
		private String encodeHTML(String text) {
			text=text.replace("&","&amp;");
			text=text.replace("<","&lt;");
			text=text.replace(">","&gt;");
			return text;
		}

		/**
		 * Fügt Stationsinformationen zu der Ausgabe hinzu
		 * @param data	JS-Daten-Objekt
		 * @param sb	Ausgabe
		 * @see #getListCellRendererComponent(JList, JSData, int, boolean, boolean)
		 */
		private void addStationInfo(final CallbackLoggerWithJS.JSData data, final StringBuilder sb) {
			sb.append(SimData.formatSimTime(data.time));
			sb.append(": ");

			final String colorCode;
			if (data.color==null || data.color.equals(Color.BLACK)) {
				colorCode="FFFFFF";
			} else {
				colorCode=Integer.toHexString(data.color.getRed())+Integer.toHexString(data.color.getGreen())+Integer.toHexString(data.color.getBlue());
			}
			sb.append("<b><span style=\"background-color: #"+colorCode+";\" color=\"black\">&nbsp; "+encodeHTML(data.station)+" &nbsp;</span></b><br>\n");
		}

		/**
		 * Wandelt eine als Zahl angegebene Zeilennummer in eine Zeichenkette (mit mindestens 3 Zeichen) um.
		 * @param nr	Zeilennummer
		 * @return	Zeilennummer als Text
		 * @see #addScript(String, StringBuilder)
		 */
		private String lineNumber(final int nr) {
			String s=""+nr;
			while (s.length()<3) s="0"+s;
			return s;
		}

		/**
		 * Fügt den Text eines Skripts zu der Ausgabe hinzu.
		 * @param script	Skript
		 * @param sb	Ausgabe
		 * @see #getListCellRendererComponent(JList, JSData, int, boolean, boolean)
		 */
		private void addScript(final String script, final StringBuilder sb) {
			sb.append("<div style=\"margin-top: 5px; margin-left: 10px;\">\n");
			sb.append("<span style=\"text-decoration: underline;\">"+Language.tr("Animation.JSResults.Script")+":</span><br><tt>\n");
			final String[] lines=script.split("\\n");
			for (int i=0;i<lines.length;i++) {
				sb.append("<span style=\"font-size: smaller; color: gray;\">"+lineNumber(i+1)+"</span>\n");
				String spaces="";
				String line=lines[i];
				while (line.startsWith(" ")) {
					spaces+="&nbsp;";
					line=line.substring(1);
				}

				sb.append("<span style=\"font-size: small;\">"+spaces+encodeHTML(line)+"</span><br>\n");
			}
			sb.append("</tt></div>\n");
		}

		/**
		 * Fügt die Rückgabe eines Skripts zu der Ausgabe hinzu.
		 * @param result	Rückgabe des Skripts
		 * @param sb	Ausgabe
		 * @see #getListCellRendererComponent(JList, JSData, int, boolean, boolean)
		 */
		private void addResult(final String result, final StringBuilder sb) {
			sb.append("<div style=\"margin-top: 5px; margin-left: 10px;\">\n");
			sb.append("<span style=\"text-decoration: underline;\">"+Language.tr("Animation.JSResults.ReturnValue")+":</span><br><b>\n");
			if (result.trim().isEmpty()) {
				sb.append(Language.tr("Animation.JSResults.NoReturnValue")+"<br>\n");
			} else {
				for (String line: result.split("\\n")) sb.append(encodeHTML(line)+"<br>\n");
				sb.append("</b></div>\n");
			}
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JSData> list, JSData value, int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel label=(JLabel)defaultRenderer.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

			final StringBuilder sb=new StringBuilder();
			addStationInfo(value,sb);
			addScript(value.script,sb);
			addResult(value.result,sb);
			label.setText("<html><body style=\"margin-bottom: 10px;\">"+sb.toString()+"</body></html>");
			return label;
		}
	}
}