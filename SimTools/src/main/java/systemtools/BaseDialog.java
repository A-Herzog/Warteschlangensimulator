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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import mathtools.NumberTools;
import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt einige Basisfunktionen zum Erstellen von Dialogen bereit.
 * @author Alexander Herzog
 * @version 2.4
 */
public class BaseDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -432438550461985704L;

	/**
	 * Bezeichnung der Schlie�en-Schaltfl�che
	 */
	public static String buttonTitleClose="Schlie�en";

	/**
	 * Bezeichnung der OK-Schaltfl�che
	 */
	public static String buttonTitleOk="OK";

	/**
	 * Bezeichnung der Abbrechen-Schaltfl�che
	 */
	public static String buttonTitleCancel="Abbrechen";

	/**
	 * Bezeichnung der Hilfe-Schaltfl�che
	 */
	public static String buttonTitleHelp="Hilfe";

	/**
	 * Skalierung des Fensters
	 * @see BaseDialog#setSizeRespectingScreensize(int, int)
	 */
	public static double windowScaling=1.0;

	/**
	 * Dialog wurde durch "Abbrechen"-Button (oder "x"-Symbol) geschlossen.
	 * @see BaseDialog#getClosedBy()
	 */
	public static final int CLOSED_BY_CANCEL=0;

	/**
	 * Dialog wurde durch "OK"-Button geschlossen.
	 * @see BaseDialog#getClosedBy()
	 */
	public static final int CLOSED_BY_OK=1;

	/**
	 * Dialog wurde durch "Vorheriges"-Button geschlossen.
	 * @see BaseDialog#getClosedBy()
	 */
	public static final int CLOSED_BY_PREVIOUS=2;

	/**
	 * Dialog wurde durch "N�chstes"-Button geschlossen.
	 * @see BaseDialog#getClosedBy()
	 */
	public static final int CLOSED_BY_NEXT=3;

	/**
	 * "Ok" Schaltfl�che (kann abh�ngig vom Typ des Dialog <code>null</code> bleiben)
	 */
	private JButton okButton=null;

	/**
	 * "Abbrechen" Schaltfl�che (kann abh�ngig vom Typ des Dialog <code>null</code> bleiben)
	 */
	private JButton cancelButton=null;

	/**
	 * "Zur�ck" Schaltfl�che (kann abh�ngig vom Typ des Dialog <code>null</code> bleiben)
	 */
	private JButton previousButton=null;

	/**
	 * "Weiter" Schaltfl�che (kann abh�ngig vom Typ des Dialog <code>null</code> bleiben)
	 */
	private JButton nextButton=null;

	/**
	 * "Schlie�en" Schaltfl�che (kann abh�ngig vom Typ des Dialog <code>null</code> bleiben)
	 */
	private JButton closeButton=null;

	/**
	 * "Hilfe" Schaltfl�che (kann <code>null</code> bleiben, wenn kein Hilfe-Callback angegeben ist)
	 */
	private JButton helpButton=null;

	/**
	 * Callback, das beim Anklicken der "Hilfe"-Schaltfl�che aktiviert werden soll (kann <code>null</code> sein)
	 */
	private Runnable helpRunnable=null;

	/**
	 * �bergeordnetes Fenster
	 */
	protected final Window owner;

	/**
	 * Gibt an, wie der Dialog geschlossen wurde.
	 * @see #getClosedBy()
	 * @see #close(int)
	 */
	private int closedBy=CLOSED_BY_CANCEL;

	/**
	 * Benutzerdefinierte Schaltfl�chen
	 * @see #addUserButton(String, URL)
	 * @see #addUserButton(String, String, URL)
	 */
	private final List<JButton> userButtons=new ArrayList<>();

	/**
	 * "Schlie�en" Schaltfl�che statt "Ok" und "Abbrechen" anzeigen
	 */
	protected boolean showCloseButton=false;

	/**
	 * Gibt an, ob �nderungen zul�ssig sind (die "Ok"-Schaltfl�che aktiv ist) oder nicht
	 */
	protected final boolean readOnly;

	/**
	 * Konstruktor der Klasse <code>BaseDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	protected BaseDialog(Component owner, String title, boolean readOnly) {
		super(owner==null?null:((owner instanceof Window)?((Window)owner):SwingUtilities.getWindowAncestor(owner)),title,Dialog.ModalityType.DOCUMENT_MODAL);
		this.owner=(owner==null)?null:((owner instanceof Window)?((Window)owner):SwingUtilities.getWindowAncestor(owner));
		this.readOnly=readOnly;
	}

	/**
	 * Konstruktor der Klasse <code>BaseDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param title	Titel des Fensters
	 */
	protected BaseDialog(Component owner, String title) {
		this(owner,title,false);
	}

	/**
	 * Ersetzt die Methode <code>setSize</code>, ber�cksichtigt dabei die Bildschirmgr��e und reduziert n�tigenfalls die Fenstergr��e.
	 * @param xSize	Gew�nschte Ausdehnung in x-Richtung des Fensters.
	 * @param ySize	Gew�nschte Ausdehnung in y-Richtung des Fensters.
	 * @see BaseDialog#windowScaling
	 */
	protected final void setSizeRespectingScreensize(int xSize, int ySize) {
		xSize=(int)Math.round(xSize*windowScaling);
		ySize=(int)Math.round(ySize*windowScaling);
		Rectangle area=getGraphicsConfiguration().getBounds();
		setSize(Math.min(area.width-50,xSize),Math.min(area.height-50,ySize));
		setMinimumSize(getSize());
	}

	/**
	 * Ersetzt die Methode <code>setSize</code>, ber�cksichtigt dabei die Bildschirmgr��e und reduziert n�tigenfalls die Fenstergr��e.
	 * @param xSize	Gew�nschte Ausdehnung in x-Richtung des Fensters.
	 * @param ySize	Gew�nschte Ausdehnung in y-Richtung des Fensters.
	 * @see BaseDialog#windowScaling
	 */
	protected final void setSizeOnlyRespectingScreensize(int xSize, int ySize) {
		xSize=(int)Math.round(xSize*windowScaling);
		ySize=(int)Math.round(ySize*windowScaling);
		Rectangle area=getGraphicsConfiguration().getBounds();
		setSize(Math.min(area.width-50,xSize),Math.min(area.height-50,ySize));
	}

	/**
	 * Ersetzt die Methode <code>setMinimumSize</code>, ber�cksichtigt dabei die Bildschirmgr��e und reduziert n�tigenfalls die Fenstergr��e.
	 * @param xSize	Gew�nschte Ausdehnung in x-Richtung des Fensters.
	 * @param ySize	Gew�nschte Ausdehnung in y-Richtung des Fensters.
	 */
	protected final void setMinSizeRespectingScreensize(int xSize, int ySize) {
		xSize=(int)Math.round(xSize*windowScaling);
		ySize=(int)Math.round(ySize*windowScaling);
		Rectangle area=getGraphicsConfiguration().getBounds();
		setMinimumSize(new Dimension(Math.min(area.width-50,xSize),Math.min(area.height-50,ySize)));
	}

	/**
	 * Verkleinert das Fenster wenn n�tig auf die angegebene Gr��e.<br>
	 * Der Nutzer wird nicht daran gehindert, das Fenster sp�ter manuell zu vergr��ern.
	 * @param xSize	Maximale Ausdehnung in x-Richtung des Fensters.
	 * @param ySize	Maximale Ausdehnung in y-Richtung des Fensters.
	 */
	protected final void setMaxSizeRespectingScreensize(int xSize, int ySize) {
		xSize=(int)Math.round(xSize*windowScaling);
		ySize=(int)Math.round(ySize*windowScaling);
		final int x=getWidth();
		final int y=getHeight();
		setSize(Math.min(xSize,x),Math.min(ySize,y));
		setMaximumSize(new Dimension(xSize,ySize));
	}

	@Override
	protected JRootPane createRootPane() {
		final JRootPane rootPane=new JRootPane();
		final InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke;

		stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=60488841484805181L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (closeOnEscape() && ((cancelButton!=null && cancelButton.isVisible()) || (closeButton!=null && closeButton.isVisible()))) {
					if (closeButton!=null && closeButton.isVisible()) {if (!closeButtonOK()) return;}
					setVisible(false); dispose();
				}
			}
		});

		stroke=KeyStroke.getKeyStroke("F1");
		inputMap.put(stroke,"F1");
		rootPane.getActionMap().put("F1",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=60488841484805181L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (helpButton!=null && helpButton.isVisible() && helpRunnable!=null) helpRunnable.run();
			}
		});

		return rootPane;
	}

	/**
	 * Soll der Dialog durch einen Escape-Tastendruck mit demselben R�ckgabewert
	 * wie wenn auf "Abbrechen" geklickt wurde geschlossen werden?
	 * @return	Dialog per Escape schlie�en?
	 */
	protected boolean closeOnEscape() {
		return true;
	}

	/**
	 * Erstellt die Rahmeninhalte
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param help Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 * @return	Panel, welches den inneren Arbeitsbereich des Fensters beinhaltet
	 */
	protected final JPanel createGUI(int xSize, int ySize, String previous, String next, Runnable help) {
		Container content=getContentPane();
		content.setLayout(new BorderLayout());

		addFooter(content,xSize,ySize,previous,next,help);

		JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		content.add(p,BorderLayout.CENTER);
		return p;
	}

	/**
	 * Erstellt die Rahmeninhalte
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param help Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 * @return	Panel, welches den inneren Arbeitsbereich des Fensters beinhaltet
	 */
	protected final JPanel createGUI(int xSize, int ySize, Runnable help) {
		return createGUI(xSize,ySize,null,null,help);
	}

	/**
	 * Erstellt die Rahmeninhalte
	 * @param help Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 * @return	Panel, welches den inneren Arbeitsbereich des Fensters beinhaltet
	 */
	protected final JPanel createGUI(Runnable help) {
		return createGUI(800,600,null,null,help);
	}

	/**
	 * Erstellt die Rahmeninhalte
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param help Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 * @return	Panel, welches den inneren Arbeitsbereich des Fensters beinhaltet
	 */
	protected final JPanel createGUI(String previous, String next, Runnable help) {
		return createGUI(800,600,previous,next,help);
	}

	/**
	 * F�gt eine benutzerdefinierte Schaltfl�che in der Fu�zeile hinzu.<br>
	 * Diese Funktion muss <b>vor</b> <code>createGUI</code> aufgerufen werden.
	 * @param caption	Name der Schaltfl�che
	 * @param icon	URL des Icons der Schaltfl�che (kann <code>null</code> sein)
	 * @return	Neu erstellte Schaltfl�che
	 * @see BaseDialog#getUserButton(int)
	 * @see BaseDialog#userButtonClick(int, JButton)
	 */
	protected final JButton addUserButton(String caption, URL icon) {
		return addUserButton(caption,null,icon);
	}

	/**
	 * F�gt eine benutzerdefinierte Schaltfl�che in der Fu�zeile hinzu.<br>
	 * Diese Funktion muss <b>vor</b> <code>createGUI</code> aufgerufen werden.
	 * @param caption	Name der Schaltfl�che
	 * @param icon	Icon der Schaltfl�che (kann <code>null</code> sein)
	 * @return	Neu erstellte Schaltfl�che
	 * @see BaseDialog#getUserButton(int)
	 * @see BaseDialog#userButtonClick(int, JButton)
	 */
	protected final JButton addUserButton(String caption, Icon icon) {
		return addUserButton(caption,null,icon);
	}

	/**
	 * F�gt eine benutzerdefinierte Schaltfl�che in der Fu�zeile hinzu.<br>
	 * Diese Funktion muss <b>vor</b> <code>createGUI</code> aufgerufen werden.
	 * @param caption	Name der Schaltfl�che
	 * @param tooltip	Tooltip f�r die Schaltfl�che (kann <code>null</code> sein)
	 * @param icon	URL des Icons der Schaltfl�che (kann <code>null</code> sein)
	 * @return	Neu erstellte Schaltfl�che
	 * @see BaseDialog#getUserButton(int)
	 * @see BaseDialog#userButtonClick(int, JButton)
	 */
	protected final JButton addUserButton(String caption, String tooltip, URL icon) {
		JButton button=new JButton(caption);
		if (tooltip!=null && !tooltip.isEmpty()) button.setToolTipText(tooltip);
		if (icon!=null)	button.setIcon(new ImageIcon(icon));
		button.addActionListener(e->userButtonClicked(e));
		userButtons.add(button);
		return button;
	}

	/**
	 * F�gt eine benutzerdefinierte Schaltfl�che in der Fu�zeile hinzu.<br>
	 * Diese Funktion muss <b>vor</b> <code>createGUI</code> aufgerufen werden.
	 * @param caption	Name der Schaltfl�che
	 * @param tooltip	Tooltip f�r die Schaltfl�che (kann <code>null</code> sein)
	 * @param icon	Icons der Schaltfl�che (kann <code>null</code> sein)
	 * @return	Neu erstellte Schaltfl�che
	 * @see BaseDialog#getUserButton(int)
	 * @see BaseDialog#userButtonClick(int, JButton)
	 */
	protected final JButton addUserButton(String caption, String tooltip, Icon icon) {
		JButton button=new JButton(caption);
		if (tooltip!=null && !tooltip.isEmpty()) button.setToolTipText(tooltip);
		if (icon!=null)	button.setIcon(icon);
		button.addActionListener(e->userButtonClicked(e));
		userButtons.add(button);
		return button;
	}

	/**
	 * F�gt den Fu�bereich zu dem Dialog hinzu
	 * @param content	Gesamter Inhaltsbereich des Dialogs
	 * @param xSize	Breite des Fensters
	 * @param ySize	H�he des Fensters
	 * @param previous Beschriftung der Vorg�nger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param next Beschriftung der Nachfolger-Schaltl�che (wird ausgeblendet, wenn gleich null oder leer)
	 * @param help Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 * @see #createGUI(int, int, String, String, Runnable)
	 */
	private void addFooter(Container content, int xSize, int ySize, String previous, String next, Runnable help) {
		JPanel p;

		content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);

		if (showCloseButton) {
			p.add(closeButton=new JButton(buttonTitleClose));
			closeButton.addActionListener(e->closeButtonAction(e));
			closeButton.setIcon(SimToolsImages.EXIT.getIcon());
			getRootPane().setDefaultButton(closeButton);
		} else {
			p.add(okButton=new JButton(buttonTitleOk));
			okButton.addActionListener(e->closeButtonAction(e));
			okButton.setEnabled(!readOnly);
			okButton.setIcon(SimToolsImages.OK.getIcon());
			getRootPane().setDefaultButton(okButton);

			p.add(cancelButton=new JButton(buttonTitleCancel));
			cancelButton.addActionListener(e->closeButtonAction(e));
			cancelButton.setIcon(SimToolsImages.CANCEL.getIcon());
		}

		if (help!=null) {
			helpRunnable=help;
			p.add(helpButton=new JButton(buttonTitleHelp));
			helpButton.addActionListener(e->help.run());
			helpButton.setIcon(SimToolsImages.HELP.getIcon());
		}

		if (previous!=null && !previous.isEmpty()) {
			p.add(previousButton=new JButton(previous));
			previousButton.addActionListener(e->closeButtonAction(e));
			previousButton.setIcon(SimToolsImages.ARROW_LEFT.getIcon());
		}

		if (next!=null && !next.isEmpty()) {
			p.add(nextButton=new JButton(next));
			nextButton.addActionListener(e->closeButtonAction(e));
			nextButton.setIcon(SimToolsImages.ARROW_RIGHT.getIcon());
		}

		for (JButton userButton : userButtons) p.add(userButton);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				if (closeButton!=null && closeButton.isVisible()) {if (!closeButtonOK()) return;}
				setVisible(false); dispose();
			}
		});
		setResizable(false);
		setSizeOnlyRespectingScreensize(xSize,ySize);
		setLocationRelativeTo(owner);
	}

	/**
	 * Aktiviert oder deaktiviert alle Schaltfl�chen.
	 * @param enabled	 Gibt an, ob die Schaltfl�chen aktiviert oder deaktiviert werden sollen.
	 */
	protected final void setEnableButtons(final boolean enabled) {
		if (okButton!=null) okButton.setEnabled(enabled);
		if (cancelButton!=null) cancelButton.setEnabled(enabled);
		if (previousButton!=null) previousButton.setEnabled(enabled);
		if (nextButton!=null) nextButton.setEnabled(enabled);
		if (closeButton!=null) closeButton.setEnabled(enabled);
		if (helpButton!=null) helpButton.setEnabled(enabled);
		for (JButton userButton: userButtons) userButton.setEnabled(enabled);
	}

	/**
	 * Aktiviert oder deaktiviert die Ok-Schaltfl�che.
	 * @param enabled	Gibt an, ob die Schaltfl�che aktiviert oder deaktiviert werden soll.
	 */
	protected final void setEnableOk(final boolean enabled) {
		if (okButton!=null) okButton.setEnabled(enabled);
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung, nachfolgendem Button und Label hinter dem Button zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param buttonName	Beschriftung der Schaltfl�che
	 * @param buttonIcon	URL zu dem Icon, dass auf der Schaltfl�che angezeigt werden soll (wird hier <code>null</code> �bergeben, so wird kein Icon angezeigt)
	 * @return	Array aus drei Elementen: Referenz auf das neu erzeugte Textfeld, Rerferenz auf das neu erzeugte Button und Referenz auf das neu erzeugte Label rechts neben dem Button
	 */
	protected final JComponent[] addPercentInputLineWithButton(JPanel p, String name, double initialValue, String buttonName, URL buttonIcon) {
		/* Initialwert f�r Textfeld vorbereiten */
		String s;
		if (initialValue>=0 && initialValue<=1)
			s=NumberTools.formatNumberMax(initialValue*100)+"%";
		else
			s=NumberTools.formatNumberMax(initialValue);

		JPanel subPanel;

		/* Textfeld anlegen */
		JTextField text;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(s,10));
		addUndoFeature(text);

		/* Button anlegen */
		JButton button;

		subPanel.add(Box.createHorizontalStrut(10));
		subPanel.add(button=new JButton(buttonName));
		if (buttonIcon!=null) button.setIcon(new ImageIcon(buttonIcon));

		/* Label anlegen */
		JLabel label;
		subPanel.add(Box.createHorizontalStrut(10));
		subPanel.add(label=new JLabel());

		return new JComponent[]{text,button,label};
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addPercentInputLine(JPanel p, String name, double initialValue) {
		String s;
		if (initialValue>=0 && initialValue<=1)
			s=NumberTools.formatNumberMax(initialValue*100)+"%";
		else
			s=NumberTools.formatNumberMax(initialValue);
		return addPercentInputLine(p,name,s);
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addPercentInputLine(JPanel p, String name, String initialValue) {
		JPanel subPanel;
		JTextField text;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(initialValue,10));
		addUndoFeature(text);
		return text;
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addInputLine(JPanel p, String name, double initialValue) {
		return addInputLine(p,name,NumberTools.formatNumberMax(initialValue));
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addInputLine(JPanel p, String name, String initialValue) {
		return addInputLine(p,name,initialValue,10);
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param columns	Breite des Textfeldes
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addInputLine(JPanel p, String name, String initialValue, int columns) {
		JPanel subPanel;
		JTextField text;

		JLabel label=new JLabel(name);
		p.add(label);
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(initialValue,columns));
		addUndoFeature(text);
		label.setLabelFor(text);
		return text;
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Label und Eingabezeile werden dabei untereinander dargestellt.
	 * Das Panel sollte dabei den Layout-Typ BoxLayout in vertikaler Ausrichtung besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialValue	Anf�nglicher Wert f�r das Textfeld
	 * @param columns	Breite des Textfeldes
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected final JTextField addVerticalInputLine(JPanel p, String name, String initialValue, int columns) {
		JPanel subPanel;
		JTextField text;

		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(new JLabel(name));

		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));
		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(text=new JTextField(initialValue,columns));
		addUndoFeature(text);
		return text;
	}

	/**
	 * Diese Funktion f�gt eine Combobox inkl. Beschreibung zu einem Panel hinzu.
	 * Das Panel sollte dabei den Layout-Typ GridLayout(2n,1) besitzen.
	 * @param p	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r die Combobox
	 * @param values	Werte f�r die Combobox
	 * @return	Referenz auf die neu erzeugte Combobox
	 */
	protected final JComboBox<String> addComboBox(JPanel p, String name, String[] values) {
		JPanel subPanel;
		JComboBox<String> box;

		p.add(new JLabel(name));
		p.add(subPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)));

		subPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		subPanel.add(box=new JComboBox<>(values));
		return box;
	}

	/**
	 * Diese Funktion f�gt ein Textfeld inkl. Beschreibung zu einem Panel hinzu.
	 * Label und Eingabezeile werden dabei nebeneinander dargestellt.
	 * Das Panel sollte dabei den Layout-Typ BoxLayout in vertikaler Ausrichtung besitzen.
	 * @param parent	Panel, in das das Eingabefeld eingef�gt werden soll
	 * @param name	Beschriftung f�r das Textfeld
	 * @param initialText	Anf�nglicher Wert f�r das Textfeld
	 * @return	Referenz auf das neu erzeugte Textfeld
	 */
	protected JTextField addWideInputLine(final JComponent parent, final String name, final String initialText) {
		final JTextField field=new JTextField(initialText);
		addUndoFeature(field);
		addWideComponent(parent,name,field);
		return field;
	}

	/**
	 * Diese Funktion f�gt eine beliebige, bereits bestehende Komponente
	 * inkl. Beschreibung zu einem Panel hinzu.
	 * Label und Komponente werden dabei nebeneinander dargestellt.
	 * Das Panel sollte dabei den Layout-Typ BoxLayout in vertikaler Ausrichtung besitzen.
	 * @param parent	Panel, in das die Komponente eingef�gt werden soll
	 * @param name	Beschriftung
	 * @param component	Einzuf�gende Komponente
	 */
	protected void addWideComponent(final JComponent parent, final String name, final JComponent component) {
		final JPanel line=new JPanel(new BorderLayout());
		parent.add(line);
		final JLabel label=new JLabel(name);
		line.add(label,BorderLayout.WEST);
		line.add(component,BorderLayout.CENTER);
		label.setLabelFor(component);
		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		line.setMaximumSize(new Dimension(line.getMaximumSize().width,Math.max(component.getPreferredSize().height+10,line.getPreferredSize().height)));
		line.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	}

	/**
	 * Versucht den Dialog mit dem angegebenen ClosedBy-Typ zu schlie�en.
	 * @param closedBy	Gibt an, auf welche Art der Dialog geschlossen werden soll (siehe <code>CLOSED_BY_*</code> Konstanten)
	 * @return	Gibt <code>true</code> zur�ck, wenn der Dialog geschlossen wurde.
	 */
	protected final boolean close(int closedBy) {
		if (closedBy!=CLOSED_BY_CANCEL) {
			if (!checkData()) return false;
			storeData();
		}
		this.closedBy=closedBy;
		setVisible(false);
		dispose();
		return true;
	}

	/**
	 * Gibt an, wie der Dialog geschlossen wurde.
	 * @return	Enth�lt eine der <code>CLOSED_BY_*</code>-Konstanten.
	 */
	public final int getClosedBy() {
		return closedBy;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	protected boolean checkData() {
		return true;
	}

	/**
	 * Wird beim Klicken auf "Schlie�en" aufgerufen, um zu pr�fen, ob der Dialog geschlossen werden darf.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Dialog geschlossen werden darf.
	 */
	protected boolean closeButtonOK() {
		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see BaseDialog#checkData()
	 */
	protected void storeData() {
	}

	/**
	 * Fokussiert die "Ok"-Schaltfl�che (sofern diese vorhanden ist).
	 */
	protected final void focusOkButton() {
		if (okButton!=null) okButton.requestFocusInWindow();
	}

	/**
	 * Liefert eines der per <code>addUserButtons</code> angelegten Nutzer-Schaltfl�chen zur�ck
	 * @param index	Index der Schaltfl�che innerhalb der Nutzer-Schaltfl�chen
	 * @return	Schaltfl�chen-Objekt oder <code>null</code>, wenn der Index au�erhalb des zul�ssigen Bereichs liegt.
	 * @see BaseDialog#addUserButton(String, URL)
	 * @see BaseDialog#addUserButton(String, String, URL)
	 */
	protected JButton getUserButton(int index) {
		if (index<0 || index>=userButtons.size()) return null;
		return userButtons.get(index);
	}

	/**
	 * Wird aufgerufen, wenn auf einen Benutzer-Button geklickt wurde
	 * @param nr	0-basierende Nummer des Buttons
	 * @param button	Angeklickter Button
	 * @see BaseDialog#addUserButton(String, URL)
	 * @see BaseDialog#addUserButton(String, String, URL)
	 */
	protected void userButtonClick(final int nr, final JButton button) {}

	/**
	 * Listener f�r die verschiedenen Schaltfl�chen zum Schlie�en des Dialogs.
	 * @param e	Datensatz mit Informationen zu dem Ereignis
	 * @see BaseDialog#closeButton
	 * @see BaseDialog#okButton
	 * @see BaseDialog#cancelButton
	 * @see BaseDialog#previousButton
	 * @see BaseDialog#nextButton
	 */
	private void closeButtonAction(final ActionEvent e) {
		if (e.getSource()==closeButton) {
			if (!closeButtonOK()) return;
			closedBy=CLOSED_BY_OK;
		}
		if (e.getSource()==okButton) {
			if (!checkData()) return;
			storeData();
			closedBy=CLOSED_BY_OK;
		}
		if (e.getSource()==previousButton) {
			if (!readOnly) {
				if (!checkData()) return;
				storeData();
			} closedBy=CLOSED_BY_PREVIOUS;
		}
		if (e.getSource()==nextButton) {
			if (!readOnly) {
				if (!checkData()) return;
				storeData();
			}
			closedBy=CLOSED_BY_NEXT;
		}
		setVisible(false);
		dispose();
	}

	/**
	 * Listener f�r die benutzerdefinierten Schaltfl�chen
	 * @param e	Datensatz mit Informationen zu dem Ereignis
	 */
	private void userButtonClicked(final ActionEvent e) {
		int nr=userButtons.indexOf(e.getSource());
		if (nr<0) return;
		userButtonClick(nr,userButtons.get(nr));
	}

	/**
	 * Aktiviert die Undo/Redo-Funktionen f�r ein Textfeld
	 * @param textField Textfeld, bei dem die Funktionen aktiviert werden sollen
	 */
	public static void addUndoFeature(final JTextField textField) {
		final UndoManager manager=new UndoManager();
		textField.getDocument().addUndoableEditListener(manager);

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown()) {
					try {
						manager.undo();
					} catch (CannotUndoException e2) {
					}
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_Y && e.isControlDown()) {
					try {
						manager.redo();
					} catch (CannotRedoException e2) {
					}
					e.consume();
					return;
				}
			}
		});
	}
}