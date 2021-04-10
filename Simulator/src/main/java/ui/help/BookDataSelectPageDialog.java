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
package ui.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;

/**
 * Zeigt einen Dialog an, in dem bei mehreren möglichen Seiten für einen Sachverzeichnis-Eintrag
 * die aufzurufende Seite ausgewählt werden kann.
 * @author Alexander Herzog
 * @see BookDataDialog
 */
public class BookDataSelectPageDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7330542806522379614L;

	/**
	 * Liste der passenden Seitennummern
	 */
	private final int[] pages;

	/**
	 * Radiobuttons zur Auswahl der aufzurufenden Seite
	 * @see #pages
	 */
	private final JRadioButton[] radioButtons;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param key	Sachverzeichnis-Schlüssel zu dem es mehrere passende Seiten gibt
	 * @param pages	Liste der passenden Seitennummern
	 */
	public BookDataSelectPageDialog(final Component owner, final String key, final int[] pages) {
		super(owner,Language.tr("BookData.SelectPage.Title"));
		this.pages=pages;

		/* GUI */
		final JPanel all=createGUI(null);
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;

		/* Info-Label */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(String.format(Language.tr("BookData.SelectPage.Info"),key)));

		/* Auswahlboxen */
		final BookData data=BookData.getInstance();
		radioButtons=new JRadioButton[pages.length];
		final ButtonGroup buttonGroup=new ButtonGroup();
		for (int i=0;i<pages.length;i++) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			final int page=pages[i];
			final BookData.BookSection section=data.getSection(page);

			final String text;
			if (section==null) {
				text=Language.tr("BookData.page")+" "+page;
			} else {
				text=section.id+" "+section.name+" ("+Language.tr("BookData.page")+" "+page+")";
			}

			line.add(radioButtons[i]=new JRadioButton(text));
			buttonGroup.add(radioButtons[i]);
			radioButtons[i].addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) close(BaseDialog.CLOSED_BY_OK);}
			});
			radioButtons[i].addKeyListener(new KeyAdapter() {
				@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) close(BaseDialog.CLOSED_BY_OK);}
			});
		}
		radioButtons[0].setSelected(true);

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert die gewählte Seite.
	 * @return	Aufzurufende Seite
	 */
	public int getSelectedPage() {
		for (int i=0;i<radioButtons.length;i++) if (radioButtons[i].isSelected()) return pages[i];
		return 1;
	}
}
