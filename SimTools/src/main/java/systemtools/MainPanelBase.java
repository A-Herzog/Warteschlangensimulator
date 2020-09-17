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
package systemtools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import systemtools.MainFrameBase.DropTargetRegister;
import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt eine abstrakte Basis f�r das Haupt-Panel im Programm dar.
 * Sie verbindet sich mit einem Objekt vom Type <code>MainFrameBase</code>, um Fenster-Nachrichten austauschen zu k�nnen.
 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
 * @see MainFrameBase
 * @author Alexander Herzog
 * @version 1.9
 */
public abstract class MainPanelBase extends JPanel {
	private static final long serialVersionUID = -7372341094781006117L;

	/**
	 * Objekt vom Typ {@link Runnable}welches ausgel�st wird, wenn die Men�zeile deaktiviert werden soll
	 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	private Runnable menuBarEnable;

	/**
	 * Objekt vom Typ {@link Runnable} welches ausgel�st wird, wenn die Men�zeile aktiviert werden soll
	 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	private Runnable menuBarDisable;

	/**
	 * Objekt vom Typ {@link Runnable} welches ausgel�st wird, wenn sich der Titel ge�ndert hat
	 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	private Runnable titleChanged;

	/**
	 * Objekt vom Typ {@link Runnable} welches ausgel�st wird, wenn das Panel das Programm beenden m�chte
	 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	private Runnable closeRequest;

	/**
	 * Erg�nzung zum Programmtitel anzeigen, ob das Modell seit dem letzten Speichern ver�ndert wurde
	 * @see #getTitleAddonChanged()
	 * @see #setAdditionalTitleChangedMarker(boolean)
	 */
	private boolean titleAddonChanged=false;

	/**
	 * Erg�nzung zum Programmtitel (z.B. Name des geladenen Modells)
	 * @see #getTitleAddon()
	 * @see #setAdditionalTitle(String)
	 */
	private String titleAddon="";

	/**
	 * Toolbar innerhalb des Panels
	 * @see #initToolbar()
	 */
	private JComponent toolBar;

	/**
	 * Panel f�r optionale Meldungen (Fehler oder Hinweise) �ber dem Arbeitsbereich
	 */
	private final JPanel errorPanel;

	/**
	 * Meldung in {@link #errorPanel}
	 * @see #setMessagePanel(String, String, MessagePanelIcon)
	 * @see #setMessagePanel(String, String, String, MessagePanelIcon)
	 */
	private final JLabel errorLabel;

	/**
	 * Panel f�r den Toolbar ({@link #toolBar}) und das Meldungs-Panel ({@link #errorPanel}
	 */
	private final JPanel top;

	/**
	 * Das eigentliche Arbeitspanel innerhalb dieses Panels.
	 * (Men�, Toolbar usw. werden durch diesen Rahmen bereitgestellt. Das Arbeitspanel muss sich nur um die eigentliche Arbeitsfl�che k�mmern.)
	 */
	protected final JPanel mainPanel;

	/**
	 * Dieser {@link ActionListener} kann bei Men�s und Toolbars verwendet werden.
	 * Die Aufrufe von Men�punkten und Buttons triggern die Methode <code>action</code>.
	 * @see #action(Object)
	 */
	protected final ActionListener actionListener;

	/**
	 * Verkn�pft die Men�punkte mit den Aktionen
	 * @see #createMenuItem(JMenu, String, char, String)
	 * @see #action(Object)
	 */
	private final ActionListener actionListenerActionMap;

	/**
	 * �bergeordnetes Fenster
	 */
	protected final Window ownerWindow;

	/**
	 * Name des Programms, der im Konstruktor �bergeben wird
	 */
	protected final String programName;

	/**
	 * Konstruktor der Klasse <code>MainPanelBase</code>
	 * @param ownerWindow	�bergeordnetes Fenster
	 * @param programName	Name des Programms
	 */
	public MainPanelBase(final Window ownerWindow, final String programName) {
		super();
		this.ownerWindow=ownerWindow;
		this.programName=programName;

		/* Allgemeiner ActionListener */
		actionListener=new PanelActionListener();

		/* ActionListener zur Verkn�pfung mit der ActionMap */
		actionListenerActionMap=new PanelActionMapActionListener();

		/* Basis-Layout */
		setLayout(new BorderLayout());

		/* Arbeitspanel anlegen */
		mainPanel=new JPanel(new BorderLayout());
		add(mainPanel,BorderLayout.CENTER);

		/* Top-Panel f�r Toolbar und Fehlermeldungen anlegen */
		top=new JPanel(new BorderLayout());
		add(top,BorderLayout.NORTH);

		/* Fehler-Panel anlegen */
		top.add(errorPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);
		errorPanel.setBackground(new Color(255, 240, 0));
		errorPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		errorPanel.add(errorLabel=new JLabel(), BorderLayout.CENTER);
		errorPanel.setVisible(false);
	}

	/**
	 * Erstellt den Toolbar und bindet ihn ein.
	 */
	protected void initToolbar() {
		top.add(toolBar=createToolBar(),BorderLayout.NORTH);
	}

	/**
	 * Liefert das �bergeordnete Fenster zur�ck (z.B. f�r die Anzeige von Dialogen von Bedeutung)
	 * @return	�bergeordnetes Fenster des Panels
	 */
	protected final Window getOwnerWindow() {
		Container c=getParent();
		while (c!=null) {
			if (c instanceof Window) return (Window)c;
			c=c.getParent();
		}
		return null;
	}

	/**
	 * Mit {@link MainFrameBase}verkn�pfen, um Fenster-Nachrichten austauschen zu k�nnen.
	 * @param menuBarEnable	Objekt vom Typ {@link Runnable}welches ausgel�st wird, wenn die Men�zeile deaktiviert werden soll
	 * @param menuBarDisable	Objekt vom Typ {@link Runnable} welches ausgel�st wird, wenn die Men�zeile aktiviert werden soll
	 * @param titleChanged	Objekt vom Typ {@link Runnable} welches ausgel�st wird, wenn sich der Titel ge�ndert hat
	 * @param closeRequest		Objekt vom Typ {@link Runnable} welches ausgel�st wird, wenn das Panel das Programm beenden m�chte
	 * @param dropTargetRegister	Objekt, �ber das sich Elemente als Drag&amp;Drop-Ziele registrieren k�nnen. Wird eine Datei darauf fallen gelassen, wird die <code>loadFile</code>-Methode ausgel�st
	 * @see #enableMenuBar(boolean)
	 * @see #setAdditionalTitle(String)
	 * @see #clone()
	 * @see #loadAnyFile(File, Component, Point, boolean)
	 */
	public final void connectToFrame(final Runnable menuBarEnable, final Runnable menuBarDisable, final Runnable titleChanged, final Runnable closeRequest, final DropTargetRegister dropTargetRegister) {
		this.menuBarEnable=menuBarEnable;
		this.menuBarDisable=menuBarDisable;
		this.titleChanged=titleChanged;
		this.closeRequest=closeRequest;

		if (dropTargetRegister!=null) {
			dropTargetRegister.registerJComponent(toolBar);
			dropTargetRegister.registerJComponent(mainPanel);
			registerDropTargets(dropTargetRegister);
		}

		if (titleChanged!=null) SwingUtilities.invokeLater(titleChanged);
	}

	/**
	 * Erm�glicht es, weitere Komponenten als Empf�nger f�r Drag&amp;Drop-Aktionen zu registrieren
	 * @param dropTargetRegister	Register-System �ber das weitere Komponenten angemeldet werden k�nnen
	 */
	protected void registerDropTargets(final DropTargetRegister dropTargetRegister) {
	}

	/**
	 * Erstellt den Toolbar f�r das Panel
	 * @return	Toolbar, der oben in dem Panel angezeigt werden soll
	 */
	protected abstract JComponent createToolBar();

	/**
	 * Erzeugt die Men�zeile, die oben im Fenster angezeigt werden soll
	 * @return	Men�zeile, die oben im Fenster angezeigt werden soll
	 */
	public abstract JMenuBar createMenu();

	/**
	 * Erstellt eine URL relativ zu dem Ort einer abgeleiteten Klasse
	 * @param path	Relativer Pfad als Zeichenkette
	 * @return	URL relativ zu dem Speicherort der abgeleiteten Klasse
	 */
	protected abstract URL getResourceURL(final String path);

	/**
	 * Stellt ein hervorzuhebendes Zeichen f�r den Men�punkt ein
	 * @param menu	Men�punkt
	 * @param languageString	Der erste Buchstabe dieses Strings wird in dem Men�punkt hervorgehoben
	 */
	protected void setMnemonic(final JMenuItem menu, final String languageString) {
		if (languageString==null || languageString.trim().isEmpty()) return;
		final char c=languageString.charAt(0);
		if (c!=' ') menu.setMnemonic(c);
	}

	private Icon getIcon(final String iconName) {
		if (iconName==null || iconName.isEmpty()) return null;
		final URL imgURL=getResourceURL(iconName);
		if (imgURL!=null) return new ImageIcon(imgURL);
		return null;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final Icon icon, final char mnemonic) {
		final JMenuItem item=new JMenuItem(title);
		parent.add(item);
		item.addActionListener(actionListener);
		if (icon!=null) item.setIcon(icon);
		if (mnemonic!='\0') item.setMnemonic(mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final String icon, final char mnemonic) {
		return createMenuItemAction(parent,title,getIcon(icon),mnemonic);
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final char mnemonic) {
		return createMenuItemAction(parent,title,(Icon)null,mnemonic);
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final Icon icon, final String mnemonic) {
		final JMenuItem item=createMenuItemAction(parent,title,icon,'\0');
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final String icon, final String mnemonic) {
		final JMenuItem item=createMenuItemAction(parent,title,icon,'\0');
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final String mnemonic) {
		final JMenuItem item=createMenuItemAction(parent,title,(Icon)null,'\0');
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int hotkey) {
		final JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final String icon, final char mnemonic, final int hotkey) {
		final JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final char mnemonic, final int hotkey) {
		final JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey) {
		final JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final String icon, final String mnemonic, final int hotkey) {
		final JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemAction(final JMenu parent, final String title, final String mnemonic, final int hotkey) {
		final JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlAction(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int ctrlHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlAction(final JMenu parent, final String title, final String icon, final char mnemonic, final int ctrlHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlAction(final JMenu parent, final String title, final char mnemonic, final int ctrlHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlAction(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int ctrlHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlAction(final JMenu parent, final String title, final String icon, final String mnemonic, final int ctrlHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlAction(final JMenu parent, final String title, final String mnemonic, final int ctrlHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShiftAction(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShiftAction(final JMenu parent, final String title, final String icon, final char mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShiftAction(final JMenu parent, final String title, final char mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShiftAction(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShiftAction(final JMenu parent, final String title, final String icon, final String mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShiftAction(final JMenu parent, final String title, final String mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShiftAction(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShiftAction(final JMenu parent, final String title, final String icon, final char mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShiftAction(final JMenu parent, final String title, final char mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShiftAction(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShiftAction(final JMenu parent, final String title, final String icon, final String mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShiftAction(final JMenu parent, final String title, final String mnemonic, final int ctrlShiftHotkey) {
		JMenuItem item=createMenuItemAction(parent,title,(Icon)null,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final Icon icon, final char mnemonic, final String actionCommand) {
		final JMenuItem item=new JMenuItem(title);
		parent.add(item);
		item.setActionCommand(actionCommand);
		item.addActionListener(actionListenerActionMap);
		if (icon!=null) item.setIcon(icon);
		if (mnemonic!='\0') item.setMnemonic(mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final String icon, final char mnemonic, final String actionCommand) {
		return createMenuItem(parent,title,getIcon(icon),mnemonic,actionCommand);
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final char mnemonic, final String actionCommand) {
		return createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final Icon icon, final String mnemonic, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,icon,'\0',actionCommand);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final String icon, final String mnemonic, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,icon,'\0',actionCommand);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final String mnemonic, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,(Icon)null,'\0',actionCommand);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final String icon, final char mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final char mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final String icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItem(final JMenu parent, final String title, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int ctrlHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final String icon, final char mnemonic, final int ctrlHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final char mnemonic, final int ctrlHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int ctrlHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final String icon, final String mnemonic, final int ctrlHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlHotkey	Taste, die als Hotkey in Verbindung mit Strg f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final String mnemonic, final int ctrlHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlHotkey,InputEvent.CTRL_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShift(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShift(final JMenu parent, final String title, final String icon, final char mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShift(final JMenu parent, final String title, final char mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShift(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShift(final JMenu parent, final String title, final String icon, final String mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemShift(final JMenu parent, final String title, final String mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShift(final JMenu parent, final String title, final Icon icon, final char mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShift(final JMenu parent, final String title, final String icon, final char mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShift(final JMenu parent, final String title, final char mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShift(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShift(final JMenu parent, final String title, final String icon, final String mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param ctrlShiftHotkey	Taste, die als Hotkey in Verbindung mit Strg+Umschalt f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Men�punkt
	 */
	protected final JMenuItem createMenuItemCtrlShift(final JMenu parent, final String title, final String mnemonic, final int ctrlShiftHotkey, final String actionCommand) {
		JMenuItem item=createMenuItem(parent,title,(Icon)null,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(ctrlShiftHotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemAction(final JMenu parent, final String title, final String mnemonic) {
		final JCheckBoxMenuItem item=new JCheckBoxMenuItem(title);
		parent.add(item);
		item.addActionListener(actionListener);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIconAction(final JMenu parent, final String title, final Icon icon, final String mnemonic) {
		final JCheckBoxMenuItem item=new JCheckBoxMenuItem(title);
		parent.add(item);
		item.addActionListener(actionListener);
		setMnemonic(item,mnemonic);
		if (icon!=null) item.setIcon(icon);
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIconAction(final JMenu parent, final String title, final String icon, final String mnemonic) {
		return createCheckBoxMenuItemIconAction(parent,title,getIcon(icon),mnemonic);
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIconAction(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey) {
		final JCheckBoxMenuItem item=createCheckBoxMenuItemIconAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIconAction(final JMenu parent, final String title, final String icon, final String mnemonic, final int hotkey) {
		final JCheckBoxMenuItem item=createCheckBoxMenuItemIconAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItem(final JMenu parent, final String title, final String mnemonic, final String actionCommand) {
		final JCheckBoxMenuItem item=new JCheckBoxMenuItem(title);
		parent.add(item);
		item.setActionCommand(actionCommand);
		item.addActionListener(actionListenerActionMap);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIcon(final JMenu parent, final String title, final Icon icon, final String mnemonic, final String actionCommand) {
		final JCheckBoxMenuItem item=new JCheckBoxMenuItem(title);
		parent.add(item);
		item.setActionCommand(actionCommand);
		item.addActionListener(actionListenerActionMap);
		setMnemonic(item,mnemonic);
		if (icon!=null) item.setIcon(icon);
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIcon(final JMenu parent, final String title, final String icon, final String mnemonic, final String actionCommand) {
		return createCheckBoxMenuItemIcon(parent,title,getIcon(icon),mnemonic,actionCommand);
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIcon(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JCheckBoxMenuItem item=createCheckBoxMenuItemIcon(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Checkbox-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JCheckBoxMenuItem createCheckBoxMenuItemIcon(final JMenu parent, final String title, final String icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JCheckBoxMenuItem item=createCheckBoxMenuItemIcon(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemAction(final JMenu parent, final String title, final String mnemonic) {
		final JRadioButtonMenuItem item=new JRadioButtonMenuItem(title);
		parent.add(item);
		item.addActionListener(actionListener);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIconAction(final JMenu parent, final String title, final Icon icon, final String mnemonic) {
		final JRadioButtonMenuItem item=new JRadioButtonMenuItem(title);
		parent.add(item);
		item.addActionListener(actionListener);
		setMnemonic(item,mnemonic);
		if (icon!=null) item.setIcon(icon);
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIconAction(final JMenu parent, final String title, final String icon, final String mnemonic) {
		return createRadioButtonMenuItemIconAction(parent,title,getIcon(icon),mnemonic);
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIconAction(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey) {
		final JRadioButtonMenuItem item=createRadioButtonMenuItemIconAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und senden Aufrufe des Men�punkts an {@link #action(Object)}.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIconAction(final JMenu parent, final String title, final String icon, final String mnemonic, final int hotkey) {
		final JRadioButtonMenuItem item=createRadioButtonMenuItemIconAction(parent,title,icon,mnemonic);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItem(final JMenu parent, final String title, final String mnemonic, final String actionCommand) {
		final JRadioButtonMenuItem item=new JRadioButtonMenuItem(title);
		parent.add(item);
		item.setActionCommand(actionCommand);
		item.addActionListener(actionListenerActionMap);
		setMnemonic(item,mnemonic);
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIcon(final JMenu parent, final String title, final Icon icon, final String mnemonic, final String actionCommand) {
		final JRadioButtonMenuItem item=new JRadioButtonMenuItem(title);
		parent.add(item);
		item.setActionCommand(actionCommand);
		item.addActionListener(actionListenerActionMap);
		setMnemonic(item,mnemonic);
		if (icon!=null) item.setIcon(icon);
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIcon(final JMenu parent, final String title, final String icon, final String mnemonic, final String actionCommand) {
		return createRadioButtonMenuItemIcon(parent,title,getIcon(icon),mnemonic,actionCommand);
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIcon(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JRadioButtonMenuItem item=createRadioButtonMenuItemIcon(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Radiobutton-Men�punkt an und verkn�pft ihn mit einem Action-Befehl.
	 * @param parent	�bergeordnetes Men�-Element
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>null</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Checkbox-Men�punkt
	 */
	protected final JRadioButtonMenuItem createRadioButtonMenuItemIcon(final JMenu parent, final String title, final String icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JRadioButtonMenuItem item=createRadioButtonMenuItemIcon(parent,title,icon,mnemonic,actionCommand);
		item.setAccelerator(KeyStroke.getKeyStroke(hotkey,0));
		return item;
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an und senden Aufrufe des Buttons an {@link #action(Object)}.
	 * @param toolbar	�bergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zus�tzlich anzuzeigender Tooltip f�r den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected final JButton createToolbarButtonAction(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(actionListener);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an und senden Aufrufe des Buttons an {@link #action(Object)}.
	 * @param toolbar	�bergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zus�tzlich anzuzeigender Tooltip f�r den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected final JButton createToolbarButtonAction(final JToolBar toolbar, final String title, final String hint, final String icon) {
		return createToolbarButtonAction(toolbar,title,hint,getIcon(icon));
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an und verkn�pft ihn mit einem Action-Befehl.
	 * @param toolbar	�bergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zus�tzlich anzuzeigender Tooltip f�r den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected final JButton createToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon, final String actionCommand) {
		JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.setActionCommand(actionCommand);
		button.addActionListener(actionListenerActionMap);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an und verkn�pft ihn mit einem Action-Befehl.
	 * @param toolbar	�bergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zus�tzlich anzuzeigender Tooltip f�r den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected final JButton createToolbarButton(final JToolBar toolbar, final String title, final String hint, final String icon, final String actionCommand) {
		return createToolbarButton(toolbar,title,hint,getIcon(icon),actionCommand);
	}

	/**
	 * Wurde ein Men�eintrag oder ein Toolbar-Button nicht �ber die Funktionen in {@link MainPanelBase}
	 * angelegt, so kann �ber diese Methode daf�r gesorgt werden, dass die Schaltfl�che dennoch �ber
	 * die ActionMap (siehe {@link #addAction(String, Consumer)}) ausgewertet werden kann.
	 * @param button	Schaltfl�che bei der die Action-Verarbeitung ver�ndert werden soll
	 * @param actionCommand	Beim Aufruf zu verwendender Action-Befehl
	 */
	protected final void registerAction(final AbstractButton button, final String actionCommand) {
		button.setActionCommand(actionCommand);
		button.addActionListener(actionListenerActionMap);
	}

	/**
	 * Legt eine Aktion an und f�gt diese in die {@link ActionMap} des Panels ein.
	 * @param name	Name der Aktion
	 * @param action	Auszuf�hrendes Callback beim Aufruf der Aktion
	 */
	protected final void addAction(final String name, final Consumer<ActionEvent> action) {
		getActionMap().put(name,new AbstractAction() {
			private static final long serialVersionUID = -2948533211269447928L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (action!=null) action.accept(e);
			}
		});
	}

	/**
	 * Liefert die aktuelle Erg�nzung zum Programmtitel (z.B. Name des geladenen Modells)
	 * @return	Aktuelle Zusatzinformationen, die in der Titelzeile des Fensters angezeigt werden sollen
	 */
	public final String getTitleAddon() {
		return titleAddon;
	}

	/**
	 * Liefert die Erg�nzung zum Programmtitel, ob das Modell seit dem letzten Speichern ver�ndert wurde
	 * @return	Aktuelle Zusatzangabe, ob das Modell seit dem letzten Speichern ver�ndert wurde, die in der Titelzeile des Fensters angezeigt werden sollen
	 */
	public final boolean getTitleAddonChanged() {
		return titleAddonChanged;
	}

	/**
	 * Legt die Zusatzinformationen fest, die in der Titelzeile des Fensters angezeigt werden sollen (z.B. Name des geladenen Modells).
	 * Ein Update des Fensters wird nach dem Aufruf dieser Methode automatisch ausgel�st.
	 * @param addon	Zusatzinformationen, die in der Titelzeile des Fensters angezeigt werden sollen (kann auch <code>null</code> sein)
	 * @see MainPanelBase#connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	protected final void setAdditionalTitle(final String addon) {
		if (addon==null || addon.isEmpty()) titleAddon=null; else titleAddon=addon;
		if (titleChanged!=null) SwingUtilities.invokeLater(titleChanged);
	}

	/**
	 * Stellt ein, ob das Modell als ver�ndert gelten soll (f�r die Anzeige in der Titelzeile).
	 * @param changed	Ist das Modell seit dem letzten Speichern ver�ndert worden?
	 * @see MainPanelBase#connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	protected final void setAdditionalTitleChangedMarker(final boolean changed) {
		titleAddonChanged=changed;
		if (titleChanged!=null) SwingUtilities.invokeLater(titleChanged);
	}

	/**
	 * Aktiviert oder deaktiviert die Men�zeile in dem �bergeordneten Fenster
	 * @param enable	Gibt an, ob die Men�zeile aktiviert (<code>true</code>) oder deaktiviert (<code>false</code>) werden soll
	 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	protected void enableMenuBar(final boolean enable) {
		toolBar.setVisible(enable);
		SwingUtilities.invokeLater(enable?menuBarEnable:menuBarDisable);
	}

	/**
	 * Art des anzuzeigenden Icons
	 * @author Alexander Herzog
	 * @see MainPanelBase#setMessagePanel(String, String, MessagePanelIcon)
	 */
	public enum MessagePanelIcon {
		/** Warnung-Icom */
		WARNING(SimToolsImages.ERROR.getIcon()),
		/** Info-Icon */
		INFO(SimToolsImages.INFO.getIcon());

		/** Iconobjekt zum Anzeigen */
		final Icon icon;

		MessagePanelIcon(final Icon icon) {
			this.icon=icon;
		}
	}

	/**
	 * Zeigt oben in diesem Panel eine Fehlermeldung (z.B. Fehler im Simulationsmodell, die den Start der Simulation verhindern) an.
	 * @param title	Titel der Nachricht (kann <code>null</code> sein)
	 * @param message	Anzuzeigende Meldung oder <code>null</code>, um die Anzeige zu deaktivieren.
	 * @param icon	Anzuzeigendes Icon (kann <code>null</code> sein, dann wird kein Icon angezeigt)
	 * @return Gibt das Panel zur�ck, in dem die Meldung angezeigt wird
	 */
	protected final JPanel setMessagePanel(final String title, final String message, final MessagePanelIcon icon) {
		return setMessagePanel(title,message,null,icon);
	}

	/**
	 * Zeigt oben in diesem Panel eine Fehlermeldung (z.B. Fehler im Simulationsmodell, die den Start der Simulation verhindern) an.
	 * @param title	Titel der Nachricht (kann <code>null</code> sein)
	 * @param message	Anzuzeigende Meldung oder <code>null</code>, um die Anzeige zu deaktivieren.
	 * @param link	Optionaler Link, der hinter der Meldung angezeigt werden soll und der beim Anklicken des Labels aufgerufen werden soll
	 * @param icon	Anzuzeigendes Icon (kann <code>null</code> sein, dann wird kein Icon angezeigt)
	 * @return Gibt das Panel zur�ck, in dem die Meldung angezeigt wird
	 */
	protected final JPanel setMessagePanel(final String title, final String message, final String link, final MessagePanelIcon icon) {
		errorPanel.setVisible(message!=null && !message.isEmpty());
		if (message!=null) {
			String intro="";
			if (title!=null && !title.isEmpty()) intro="<b>"+title+":</b> ";
			final String htmlLink;
			if (link==null || link.trim().isEmpty()) {
				htmlLink="";
			} else {
				htmlLink=" <a href=\""+link+"\">"+link+"</a>";
				errorLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
				errorLabel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						if (SwingUtilities.isLeftMouseButton(e)) {
							try {
								if (!MsgBox.confirmOpenURL(MainPanelBase.this,link)) return;
								Desktop.getDesktop().browse(new URI(link));
							} catch (IOException | URISyntaxException e1) {}
						}
					}
				});
			}
			errorLabel.setText("<html>"+intro+message+htmlLink+"</html>");
			errorLabel.setIcon(icon.icon);
		}
		return errorPanel;
	}

	/**
	 * Teilt dem �bergeordneten Fenster mit, dass das Programm beendet werden soll.
	 * @see #connectToFrame(Runnable, Runnable, Runnable, Runnable, DropTargetRegister)
	 */
	protected final void close() {
		SwingUtilities.invokeLater(closeRequest);
	}

	/**
	 * Wird aufgerufen, wenn eine Datei geladen werden soll (z.B. per Kommandozeile oder per Drag&amp;Drop)
	 * @param file	Zu ladende Datei
	 * @param errorMessageOnFail	Soll im Falle eines Fehlers eine Meldung ausgegeben werden?
	 * @param dropComponent	Falls die Datei per Drag&amp;Drop an das Programm �bergeben wurde, so kann hier optional die Komponente auf der die Drag&amp;Drop-Operation endete angegeben werden.
	 * @param dropPosition	Falls die Datei per Drag&amp;Drop an das Programm �bergeben wurde, so kann hier optional die Position angegeben werden.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Datei erfolgreich geladen werden konnte.
	 */
	public abstract boolean loadAnyFile(final File file, final Component dropComponent, final Point dropPosition, final boolean errorMessageOnFail);

	/**
	 * Wird vom �bergeordneten Frame gesetzt, wenn per <code>loadAnyFile</code> beim Programmstart
	 * ein Modell oder eine Statistikdatei geladen wurde.
	 */
	public boolean fileLoadedOnLoad=false;

	/**
	 * Wird vom �bergeordneten Fenster aufgerufen, wenn der Nutzer auf Schlie�en geklickt hat.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Programm beendet werden darf (z.B. weil alles gespeichert ist).
	 */
	public abstract boolean allowQuitProgram();

	/**
	 * Diese Methode wird durch den als Feld <code>actionListener</code> angebotenen <code>ActionListener</code> aufgerufen.
	 * @param sender	Objekt, welches den <code>ActionListener</code> aufgerufen hat.
	 * @see #actionListener
	 */
	protected void action(final Object sender) {}

	private class PanelActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			action(e.getSource());
		}
	}

	private class PanelActionMapActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final Action action=getActionMap().get(e.getActionCommand());
			if (action!=null) action.actionPerformed(e);
		}
	}
}
