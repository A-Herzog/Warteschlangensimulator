/**
 * Copyright 2025 Alexander Herzog
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
package mathtools.distribution.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.distribution.tools.DistributionTools;

/**
 * Zeigt weitere Informationen zu einer Verteilung an.
 * @see JDistributionPanel
 */
public class JDistributionPanelInfo extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6646224824972265990L;

	/**
	 * Soll das Fenster geschlossen werden, wenn es den Fokus verliert?<br>
	 * (Im Standardfall: ja. Aber nicht, wenn gerade ein URL-Öffne-Dialog angezeigt wird.)
	 */
	private boolean closeOnFocusLost=true;

	/**
	 * HTML-Kopf für die Ausgabe der html-formatierten Lizenztexte.
	 * @see #htmlFooter
	 */
	private static final String htmlHeader="<html><head><style>body {font-family: sans-serif; margin: 5px;} h2 {margin-bottom: 0px;} p {margin-top: 3px; margin-bottom: 7px; font-size: 110%;} li {font-size: 110%;}</style></head><body>";

	/**
	 * HTML-Fußbereich für die Ausgabe der html-formatierten Lizenztexte.
	 * @see #htmlHeader
	 */
	private static final String htmlFooter="</body></html>";

	/**
	 * Konstruktor
	 * @param owner	Übergeordnete Komponente (zur Ausrichtung des Fensters)
	 * @param distribution	Verteilung zu der Informationen angezeigt werden sollen
	 */
	public JDistributionPanelInfo(final Component owner, final AbstractRealDistribution distribution) {
		super(DistributionTools.getDistributionName(distribution));

		/* GUI */
		final Container content=getContentPane();
		content.setLayout(new BorderLayout());

		/* Viewer */
		final JTextPane textPane=new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText(buildText(distribution));
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setBackground(new Color(0,0,0,0));
		textPane.getCaret().setVisible(false);
		textPane.setCaretColor(textPane.getBackground());
		textPane.setHighlighter(null);
		textPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED) return;
				final String link=e.getDescription();
				closeOnFocusLost=false;
				JOpenURL.open(JDistributionPanelInfo.this,link);
			}
		});
		content.add(new JScrollPane(textPane));

		/* Schließen bei Fokus-Verlust */
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				if (closeOnFocusLost) {
					setVisible(false);
					dispose();
				}
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				closeOnFocusLost=true;
			}
		});

		/* Start */
		setSize(600,600);
		if (owner!=null) {
			final Window ownerWindow=(owner instanceof Window)?((Window)owner):SwingUtilities.windowForComponent(owner);
			setLocationRelativeTo(ownerWindow);
		}
		setVisible(true);
	}

	/**
	 * Ergänzt einen Wikipedia-Link zu einem Kenngrößentext
	 * @param text	Kenngrößentext
	 * @return	Kenngrößentext ggf. mit Wikipedia-Link
	 */
	private String addWikipediaLinks(String text) {
		text=text.replace(DistributionTools.DistMean,"<a href='"+DistributionTools.DistMeanWikipedia+"'>"+DistributionTools.DistMean+"</a>");
		text=text.replace(DistributionTools.DistStdDev,"<a href='"+DistributionTools.DistStdDevWikipedia+"'>"+DistributionTools.DistStdDev+"</a>");
		text=text.replace(DistributionTools.DistCV,"<a href='"+DistributionTools.DistCVWikipedia+"'>"+DistributionTools.DistCV+"</a>");
		text=text.replace(DistributionTools.DistSkewness,"<a href='"+DistributionTools.DistSkewnessWikipedia+"'>"+DistributionTools.DistSkewness+"</a>");
		text=text.replace(DistributionTools.DistMode,"<a href='"+DistributionTools.DistModeWikipedia+"'>"+DistributionTools.DistMode+"</a>");
		return text;
	}

	/**
	 * Generiert den anzuzeigenden Text als HTML-Code.
	 * @param distribution	Verteilung für die der Infotext generiert werden soll
	 * @return	HTML-formatierter Infotext
	 */
	private String buildText(final AbstractRealDistribution distribution) {
		final StringBuilder text=new StringBuilder();

		text.append(htmlHeader);

		text.append("<h1>"+DistributionTools.getDistributionName(distribution)+"</h1>");

		text.append(DistributionTools.getDistributionInfoHTML(distribution));

		text.append("<h2>"+JDistributionPanel.InfoWindowParameters+"</h2>");
		text.append("<ul>");
		for (var line: DistributionTools.getDistributionLongInfo(distribution).split(";\\s")) text.append("<li>"+addWikipediaLinks(line)+"</line>");
		text.append("</ul>");

		final URI wikipediaLink=DistributionTools.getDistributionWikipediaLink(distribution);
		final URI webAppLink=DistributionTools.getDistributionWebAppLink(distribution);
		if (webAppLink!=null || wikipediaLink!=null) {
			text.append("<h2>"+JDistributionPanel.InfoWindowMore+"</h2>");
			text.append("<ul>");
			if (wikipediaLink!=null) text.append("<li><a href='"+wikipediaLink.toString()+"'>"+JDistributionPanel.WikiButtonLabel+"</a></li>");
			if (webAppLink!=null) text.append("<li><a href='"+webAppLink.toString()+"'>"+JDistributionPanel.WebAppButtonLabel+"</a></li>");
			text.append("</ul>");
		}

		text.append(htmlFooter);

		return text.toString();
	}

	@Override
	protected JRootPane createRootPane() {
		final JRootPane rootPane=new JRootPane();
		final InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=4891211987692161756L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false); dispose();
			}
		});

		return rootPane;
	}
}

