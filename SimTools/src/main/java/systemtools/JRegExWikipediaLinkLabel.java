/**
 * Copyright 2023 Alexander Herzog
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import mathtools.distribution.swing.JOpenURL;

/**
 * Zeigt einen anklickbaren Link zur Wikipedia-"Reguläre Ausdrücke"-Seite an.
 * @author Alexander Herzog
 */
public class JRegExWikipediaLinkLabel extends JLabel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3830281802861531466L;

	/**
	 * Übergeordnetes Element (zur Ausrichtung des Bestätigen-Dialogs)
	 */
	public static String title="Hilfe zu regulären Ausdrücken";

	/**
	 * Tooltip für den Link (kann leer oder <code>null</code> sein)
	 */
	public static String tooltip="Wikipedia-Seite zum Thema \"Reguläre Ausdrücke\"";

	/**
	 * Aufzurufende URL
	 */
	public static String url="https://de.wikipedia.org/wiki/Regul%C3%A4rer_Ausdruck";

	/**
	 * Konstruktor der Klasse
	 * @param parent	Übergeordnetes Element (zur Ausrichtung des Bestätigen-Dialogs)
	 * @param title	Text für den Link
	 * @param tooltip	Tooltip für den Link (kann leer oder <code>null</code> sein)
	 * @param url	Aufzurufende URL
	 * @param addLeftMargin	Soll links etwas Abstand (z.B. zu der vorgelagerten Checkbox) eingefügt werden?
	 */
	public JRegExWikipediaLinkLabel(final Component parent, final String title, final String tooltip, final String url, final boolean addLeftMargin) {
		setText("<html><body><span style=\"color: blue; text-decoration: underline;\">"+title+"</span></body></html>");
		if (tooltip!=null && !tooltip.isBlank()) setToolTipText(tooltip);
		if (addLeftMargin) setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) JOpenURL.open(parent,url);
			}
		});
	}

	/**
	 * Konstruktor der Klasse
	 * @param parent	Übergeordnetes Element (zur Ausrichtung des Bestätigen-Dialogs)
	 */
	public JRegExWikipediaLinkLabel(final Component parent) {
		this(parent,title,tooltip,url,true);
	}
}
