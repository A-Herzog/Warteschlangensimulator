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
package systemtools.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import systemtools.BaseDialog;
import systemtools.JScrollPaneTouch;

/**
 * Ermöglicht die Auswahl eines bestimmten Suchtreffers, wenn
 * ein Index-Eintrag mit mehreren Treffer-Dateien gewählt wurde.
 * @author Alexander Herzog
 * @see HTMLPanel
 * @see HTMLPanelSearchDialog
 */
public class HTMLPanelSelectDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-782519174200265175L;

	/**
	 * Seiten-Dateinamen der Suchtreffer
	 */
	private String[] pages;

	/**
	 * Darstellung der Suchtreffer
	 */
	private final JList<String> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param pages	Seiten-Dateinamen der Suchtreffer
	 */
	public HTMLPanelSelectDialog(final Component owner, final Set<String> pages) {
		super(owner,HelpBase.buttonSearch);

		this.pages=pages.toArray(String[]::new);

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Infozeile oben */
		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(setup,BorderLayout.NORTH);
		setup.add(new JLabel(HelpBase.buttonSearchResultSelect));

		/* Liste der Seiten */
		final IndexSystem index=IndexSystem.getInstance();
		final String[] pageTitles=new String[this.pages.length];
		for (int i=0;i<this.pages.length;i++) {
			pageTitles[i]=index.getPageName(this.pages[i]);
			if (pageTitles[i]==null) pageTitles[i]=this.pages[i];
		}
		content.add(new JScrollPaneTouch(list=new JList<>(pageTitles)),BorderLayout.CENTER);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) close(BaseDialog.CLOSED_BY_OK);
			}
		});
		list.setSelectedIndex(0);

		/* Dialog anzeigen */
		setMinSizeRespectingScreensize(480,320);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert den Dateinamen des gewählten Suchtreffers.
	 * @return	Dateiname des gewählten Suchtreffers (oder <code>null</code>, wenn kein Treffer gewählt wurde)
	 */
	public String getSelectedPage() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		if (list.getSelectedIndex()<0) return null;
		return pages[list.getSelectedIndex()];
	}
}
