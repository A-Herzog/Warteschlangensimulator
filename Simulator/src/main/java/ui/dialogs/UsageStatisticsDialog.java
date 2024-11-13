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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.swing.JOpenURL;
import systemtools.BaseDialog;
import systemtools.GUITools;
import tools.UsageStatistics;
import ui.MainFrame;
import ui.MainPanel;

/**
 * Zeigt an, wie viele Kunden bislang insgsamt simuliert wurden.
 * @author Alexander Herzog
 * @see UsageStatistics
 */
public class UsageStatisticsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-25602704031321201L;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public UsageStatisticsDialog(final Component owner) {
		super(owner,Language.tr("UsageStatistics.Title"));

		final UsageStatistics usageStatistics=UsageStatistics.getInstance();
		final long count=usageStatistics.getSimulationClients();
		final long seconds=usageStatistics.getCPUSeonds();

		JPanel line;

		/* GUI */
		showCloseButton=true;
		final JPanel all=createGUI(null);
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Anzahl an Ankünften */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final StringBuilder text=new StringBuilder();
		text.append("<html><body>");
		text.append("<b>"+String.format(Language.tr("UsageStatistics.Info"),NumberTools.formatLong(count))+"</b>");
		text.append("<br>");
		text.append("<b>"+String.format(Language.tr("UsageStatistics.InfoSeconds"),TimeTools.formatLongTime(seconds))+"</b>");
		text.append("</body></html>");
		line.add(new JLabel(text.toString()));

		/* GitHub-Link */
		if (count>=1_000_000_000) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel("<html><body>"+Language.tr("UsageStatistics.GitHubStarInfo")+"</body></html>"));
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			final JTextPane label=new JTextPane();
			label.setContentType("text/html");
			label.setText("<html><body style=\"margin: 0px; padding: 0px; font-family: sans; background-color: transparent; font-size: "+Math.round(100*GUITools.getScaleFactor())+"%;\"><a href=\"https://"+MainPanel.REPOSITORY_URL+"\">"+MainFrame.PROGRAM_NAME+" GitHub Repository</a></body></html>");
			label.setEditable(false);
			label.setOpaque(false);
			label.setBackground(new Color(0,0,0,0));
			label.setCaretColor(label.getBackground());
			label.setHighlighter(null);
			line.add(label);
			label.addHyperlinkListener(e->{
				if (e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED) return;
				JOpenURL.open(this,e.getDescription());
			});
		}

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

}
