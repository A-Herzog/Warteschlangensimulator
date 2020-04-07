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
package ui.infopanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import ui.images.Images;

/**
 * Diese Klasse ermöglicht das Anzeigen von Informationstexten in Dialogen
 * @author Alexander Herzog
 */
public class InfoPanelSimple {
	private static InfoPanelSimple infoPanel;

	private final Map<Window,List<JPanel>> activeHintsList;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht direkt instanziert werden, sondern
	 * nur indirekt über die statischen Methoden als Singleton
	 * verwendet werden.
	 * @see InfoPanelSimple#addTopPanel(Window, Container, String)
	 * @see InfoPanelSimple#addTopPanelAndGetNewContent(Window, Container, String)
	 */
	private InfoPanelSimple() {
		activeHintsList=new HashMap<>();
	}

	private static InfoPanelSimple getInstance() {
		if (infoPanel==null) infoPanel=new InfoPanelSimple();
		return infoPanel;
	}

	private JPanel buildPanel(final Window window, final String text) {
		final JPanel topInner=new JPanel(new BorderLayout());
		topInner.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		topInner.setBackground(new Color(250,250,240));

		final JLabel label=new JLabel();
		label.setIcon(Images.GENERAL_INFO.getIcon());
		label.setVerticalAlignment(SwingConstants.TOP);
		label.setBorder(BorderFactory.createEmptyBorder(5,5,5,0));
		topInner.add(label,BorderLayout.WEST);

		final JTextPane pane=new JTextPane();
		pane.setOpaque(false);
		pane.setEditable(false);
		pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		topInner.add(pane,BorderLayout.CENTER);

		final JToolBar toolBar=new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		final JButton button=new JButton(Language.tr("Editor.AddEdge.Hint.RemoveButton"));
		button.setBackground(new Color(255,255,240));
		button.setToolTipText(Language.tr("Editor.AddEdge.Hint.RemoveButton.Hint"));
		button.setIcon(Images.INFO_PANEL_CLOSE_THIS.getIcon());
		button.addActionListener(e->SwingUtilities.invokeLater(()->turnOffHints()));
		toolBar.add(button);
		toolBar.setBackground(new Color(255,255,240));
		topInner.add(toolBar,BorderLayout.EAST);

		SwingUtilities.invokeLater(()->pane.setText(text));

		final JPanel topOuter=new JPanel(new BorderLayout());
		topOuter.add(topInner,BorderLayout.CENTER);
		topOuter.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

		return topOuter;
	}

	private void turnOffHints() {
		for (Map.Entry<Window,List<JPanel>> entry: activeHintsList.entrySet()) {
			for (JPanel panel: entry.getValue()) {
				Container parent=panel.getParent();
				parent.remove(panel);
				parent.revalidate();
				parent.repaint();
			}
		}

		activeHintsList.clear();

		/*
		setup.showDialogHints=false;
		setup.saveSetup();
		 */
	}

	private void registerPanel(final Window window, final JPanel hintPanel) {
		if (activeHintsList.get(window)==null) {
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					activeHintsList.remove(window);
				}
			});
		}

		List<JPanel> list=activeHintsList.get(window);
		if (list==null) {
			list=new ArrayList<>();
			activeHintsList.put(window,list);
		}

		list.add(hintPanel);
	}

	/**
	 * Erstellt (wenn im Setup aktiviert) ein Infopanel mit dem angegebenen Text und fügt es ein.
	 * @param window	Übergeordnetes Fenster
	 * @param parent	Übergeordnetes Element (wird von dieser Methode auf {@link BorderLayout} gestellt)
	 * @param text	Anzuzeigender Text
	 */
	public static void addTopPanel(final Window window, final Container parent, final String text) {
		final InfoPanelSimple infoPanel=getInstance();
		/* if (!infoPanel.setup.showDialogHints) return; */
		final JPanel hint=infoPanel.buildPanel(window,text);
		infoPanel.registerPanel(window,hint);
		parent.setLayout(new BorderLayout());
		parent.add(hint,BorderLayout.NORTH);
	}

	/**
	 * Erstellt (wenn im Setup aktiviert) ein Infopanel mit dem angegebenen Text und fügt es ein.
	 * @param window	Übergeordnetes Fenster
	 * @param parent	Übergeordnetes Element (wird von dieser Methode auf {@link BorderLayout} gestellt)
	 * @param text	Anzuzeigender Text
	 * @return	Erstellt ein neues Panel und fügt es als Content in das angegeben übergeordnete Element ein
	 */
	public static JPanel addTopPanelAndGetNewContent(final Window window, final Container parent, final String text) {
		addTopPanel(window,parent,text);
		JPanel content=new JPanel(new BorderLayout());
		parent.add(content,BorderLayout.CENTER);
		return content;
	}
}