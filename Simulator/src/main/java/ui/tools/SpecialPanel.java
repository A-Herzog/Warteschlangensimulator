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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ui.MainPanel;
import ui.images.Images;

/**
 * Basisklasse für weitere Panels, die im Arbeitsbereich des <code>SimulatorPanel</code> angezeigt werden können.
 * @see MainPanel
 * @author Alexander Herzog
 */
public class SpecialPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 399014899599365120L;

	/** Name der "Schließen"-Schaltfläche */
	public static String buttonClose="Schließen";

	/** Tooltip für die "Schließen"-Schaltfläche */
	public static String buttonCloseHint="Schließt die aktuelle Ansicht";

	/** Objekt vom Typ <code>Runnable</code> welches aktiviert wird, wenn das Panel seine Arbeit beendet hat und sich schließen möchte (was es durch den Aufruf von <code>close</code> kenntlich macht) */
	private final Runnable closePanel;
	/** Toolbar des Panels */
	private JToolBar toolbar=null;
	/** "Schließen"-Schaltfläche */
	private JButton closeButton=null;
	/** Benutzerdefinierte Schaltflächen auf dem Toolbar */
	private List<JButton> userButtons=new ArrayList<>();
	/** Listener, der benachrichtigt wird, wenn eine Schaltflächen auf dem Toolbar angeklickt wird */
	private ButtonListener buttonListener=new ButtonListener();
	/** Warte-Indicator (ganz rechts in der Symbolleiste) */
	private JLabel waitIndicator=null;

	/**
	 * Konstruktor der Klasse <code>SpecialPanel</code>
	 * @param closePanel	Objekt vom Typ <code>Runnable</code> welches aktiviert wird, wenn das Panel seine Arbeit beendet hat und sich schließen möchte (was es durch den Aufruf von <code>close</code> kenntlich macht)
	 * @see #close()
	 */
	public SpecialPanel(final Runnable closePanel) {
		super();
		this.closePanel=closePanel;

		setLayout(new BorderLayout());

		InputMap inputMap=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		getActionMap().put("ESCAPE",new AbstractAction() {
			private static final long serialVersionUID = -3369192426710383359L;
			@Override
			public void actionPerformed(ActionEvent e) {requestClose();}
		});
	}

	/**
	 * Signalisiert dem übergeordneten Panel, dass dieses Panel seine Arbeit beendet hat und entfernt werden kann.
	 */
	protected void close() {
		if (closePanel!=null) SwingUtilities.invokeLater(closePanel);
	}

	/**
	 * Teilt dem Panel mit, dass es geschlossen werden soll. Das Panel kann dies ignorieren.
	 *  Wenn das Panel bereit ist, geschlossen zu werden, muss es die <code>close</code>-Methode aufrufen.
	 *  @see #close()
	 */
	public void requestClose() {
		close();
	}

	/**
	 * Richtet {@link #toolbar} ein, sofern dieser
	 * noch <code>null</code> ist.
	 */
	private void initToolbar() {
		if (toolbar!=null) return;

		final JPanel top=new JPanel(new BorderLayout());
		add(top,BorderLayout.NORTH);

		top.add(toolbar=new JToolBar(),BorderLayout.CENTER);
		toolbar.setFloatable(false);

		waitIndicator=new JLabel();
		waitIndicator.setIcon(Images.GENERAL_WAIT_INDICATOR.getIcon());
		waitIndicator.setVisible(false);
		top.add(waitIndicator,BorderLayout.EAST);
		waitIndicator.setBorder(BorderFactory.createEmptyBorder(5,5,0,15));
	}

	/**
	 * Fügt eine Schaltfläche auf dem Toolbar hinzu
	 * @param title	Beschriftung der Schaltfläche
	 * @param hint	Optionaler Tooltip (kann <code>null</code> sein)
	 * @param icon	Optionales Icon (kann <code>null</code> sein)
	 * @return	Liefert das neue (bereits hinzugefügte) Button
	 */
	private JButton addButtonInt(final String title, final String hint, final URL icon) {
		initToolbar();
		JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null && !hint.isEmpty()) button.setToolTipText(hint);
		button.addActionListener(buttonListener);
		if (icon!=null) button.setIcon(new ImageIcon(icon));
		return button;
	}

	/**
	 * Macht den Warte-Indicator (ganz rechts in der Symbolleiste) sichtbar oder unsichtbar
	 * @param visible	Sichtbarkeitsstatus des Warte-Indicators
	 */
	protected void setWaitIndicatorVisible(final boolean visible) {
		if (visible) initToolbar();
		waitIndicator.setVisible(visible);
	}

	/**
	 * Fügt eine "Schließen"-Schaltfläche zu der Symbolleiste des Panels hinzu.
	 * (Im Bedarfsfalle wird die Symbolleiste dafür zunächst angelegt.)
	 */
	protected final void addCloseButton() {
		if (closeButton!=null) return;
		closeButton=addButtonInt(buttonClose,buttonCloseHint,Images.GENERAL_EXIT.getURL());
	}

	/**
	 * Fügt einen Trennen in die Symbolleiste des Panels ein.
	 * (Im Bedarfsfalle wird die Symbolleiste dafür zunächst angelegt.)
	 */
	protected final void addSeparator() {
		initToolbar();
		toolbar.addSeparator();
	}

	/**
	 * Fügt eine benutzerdefinierte Schaltfläche zu der Symbolleiste des Panels hinzu.
	 * (Im Bedarfsfalle wird die Symbolleiste dafür zunächst angelegt.)
	 * @param title	Titel der Schaltfläche
	 * @param hint	Optionaler Tooltip-Text für die Schaltfläche (wird <code>null</code> übergeben, so wird kein Tooltip angezeigt)
	 * @param icon	Icon, das auf der Schaltfläche angezeigt werden soll (wird <code>null</code> übergeben, so wird kein Icon angezeigt)
	 * @return	Liefert die Schaltfläche zurück
	 * @see #userButtonClick(int, JButton)
	 */
	protected final JButton addUserButton(final String title, final String hint, final URL icon) {
		JButton button=addButtonInt(title,hint,icon);
		userButtons.add(button);
		return button;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn eine der benutzerdefinierten Schaltflächen angeklickt wurde.
	 * @param index	0-basierender Index der angeklickten Schaltfläche
	 * @param button	Angeklickte Schaltfläche
	 * @see #addUserButton(String, String, URL)
	 */
	protected void userButtonClick(int index, JButton button) {}

	/**
	 * Auf das Anklicken einer Schaltfläche im Toolbar reagieren.
	 */
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object sender=e.getSource();
			if (sender==closeButton) {requestClose(); return;}
			int index=userButtons.indexOf(sender); if (index>=0) {userButtonClick(index,userButtons.get(index)); return;}
		}
	}
}