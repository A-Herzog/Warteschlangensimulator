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
package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.w3c.dom.Element;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelBase;
import systemtools.MsgBox;
import tools.SetupData;
import ui.dialogs.ModelSecurityCheckDialog;
import ui.modeleditor.FilePathHelper;
import ui.modeleditor.ModelLoadData;
import xml.XMLTools;

/**
 * Abstrakte Basisklasse für die grafische Bearbeitung der Daten eines <code>EditModel</code> Objekts
 * Diese Klasse baut keine GUI auf, hält aber Methoden zum Laden und Speichern von <code>EditModel</code>-Objekten usw. bereit
 * @see EditModel
 * @author Alexander Herzog
 * @version 1.1
 */
public abstract class EditorPanelBase extends JPanel {
	private static final long serialVersionUID = -2225095205646314638L;

	/**
	 * Titel für mögliche Speichern-Dialoge zum Laden des Modells
	 */
	public static String SAVE_MODEL="Modell speichern";

	/**
	 * Fehlermeldung, dass das Speichern eines Modells fehlgeschlagen ist.
	 */
	public static String SAVE_MODEL_ERROR="Beim Speichern der Daten ist ein Fehler aufgetreten.";

	/**
	 * Titel für mögliche Lade-Dialoge zum Laden des Modells
	 */
	public static String LOAD_MODEL="Modell laden";

	/**
	 * Titel der Warnmeldung, dass das geladene Modell von einer neueren Simulator-Version stammt.
	 */
	public static String NEWER_VERSION_TITLE="Modelldatei wurde mit neuerer Version erstellt";

	/**
	 * Inhalt der Warnmeldung, dass das geladene Modell von einer neueren Simulator-Version stammt.
	 */
	public static String NEWER_VERSION_INFO="Das geladene Modell wurde mit der neueren Version %s des Simulators erstellt.\nIn dem Modell sind evtl. Eigenschaften enthalten, die diese Version des Simulators nicht berücksichtigen kann.\nWenn Sie das Modell ändern und dann speichern, gehen diese zusätzlichen Eigenschaften verloren.";

	/**
	 * Titel der Warnmeldung, dass in der Modelldatei unbekannte Elemente enthalten waren.
	 */
	public static String UNKNOWN_ELEMENTS_TITLE="Unbekannte Elemente in Modelldatei";

	/**
	 * Inhalt der Warnmeldung, dass in der Modelldatei unbekannte Elemente enthalten waren.
	 */
	public static String UNKNOWN_ELEMENTS_INFO="Die Modelldatei enthielt unbekannte Elemente, die beim Laden übersprungen wurden. Das Modell ist daher nicht vollständig.";

	private boolean isGUIReady;
	private JTabbedPane tabs=null;

	/**
	 * Modell, welches hier bearbeitet werden soll
	 */
	protected EditModel model;

	/**
	 * Gibt an, ob die Daten nur angezeigt (<code>true</code>) oder auch zum Bearbeiten freigegeben werden sollen (<code>false</code>)
	 */
	protected final boolean readOnly;

	private EditModel modelOriginal;
	private boolean modelChanged;
	private File lastFile=null;
	private final Semaphore mutexGetSetModel=new Semaphore(1);

	private final Set<Runnable> changedStateListeners=new HashSet<>();

	/**
	 * Konstruktor der Klasse <code>EditorPanelBase</code>
	 * @param model	Initial zu verwendendes Modell (kann <code>null</code> sein). Ist <code>model!=null</code>, so wird eine Kopie erstellt und diese verwendet.
	 * @param readOnly	Gibt an, ob die Daten nur angezeigt (<code>true</code>) oder auch bearbeitet werden können (<code>false</code>)
	 */
	public EditorPanelBase(final EditModel model, final boolean readOnly) {
		super();
		isGUIReady=false;

		setModel(model);
		this.readOnly=readOnly;

		buildGUI();

		isGUIReady=true;
		loadGUIDataFromModel();
	}

	/**
	 * Konstruktor der Klasse <code>EditorPanelBase</code>
	 */
	public EditorPanelBase() {
		this(null,false);
	}

	/**
	 * Liefert das übergeordnete Fenster zurück (z.B. für die Anzeige von Dialogen von Bedeutung)
	 * @return	Übergeordnetes Fenster des Panels
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
	 * Wird vom Konstruktor zum Aufbau der GUI aufgerufen.
	 */
	protected abstract void buildGUI();

	/**
	 * Wird aufgerufen, um die Daten aus den GUI-Elementen in das Modell zu schreiben.
	 */
	protected abstract void writeGUIDataToModel();

	/**
	 * Wird aufgerufen, um die Daten aus dem Modell in die GUI-Elemente zu schreiben.
	 */
	protected abstract void loadGUIDataFromModel();

	/**
	 * Liefert eine Kopie des momentan in Bearbeitung befindlichen Modells
	 * @return	Kopie des aktuellen Modells
	 */
	public final EditModel getModel() {
		mutexGetSetModel.acquireUninterruptibly();
		try {
			if (isGUIReady) writeGUIDataToModel();
			if (!modelChanged) {
				modelChanged=(modelOriginal!=null) && !modelOriginal.equalsEditModel(model);
			}
			return model.clone();
		} finally {
			mutexGetSetModel.release();
			if (modelChanged) fireChangedStateListeners();
		}
	}

	/**
	 * Setzt ein neues Modell im Editor
	 * @param newModel	Neues Modell (kann auch <code>null</code> sein, dann wird ein neues, leeres Modell erstellt)
	 * @param cloneModel	Soll das übergebene Modell selbst verwendet werden (<code>false</code>) oder soll eine Kopie angelegt werden (<code>true</code>)
	 */
	public final void setModel(final EditModel newModel, final boolean cloneModel) {
		mutexGetSetModel.acquireUninterruptibly();
		try {
			if (newModel==null) {
				model=new EditModel();
			} else {
				if (cloneModel) {
					model=newModel.clone();
				} else {
					model=newModel;
				}
			}
			modelOriginal=this.model.clone();
			modelChanged=false;
			lastFile=null;
			if (isGUIReady) loadGUIDataFromModel();
		} finally {
			mutexGetSetModel.release();
			fireChangedStateListeners();
		}
	}

	/**
	 * Setzt ein neues Modell im Editor
	 * @param newModel	Neues Modell (kann auch <code>null</code> sein, dann wird ein neues, leeres Modell erstellt)
	 */
	public final void setModel(final EditModel newModel) {
		setModel(newModel,true);
	}

	/**
	 * Liefert das Original-Datenlade-Objekt aus dem aktuellen Modell.
	 * @return	Original-Datenlade-Objekt
	 */
	public final ModelLoadData getModelExternalData() {
		mutexGetSetModel.acquireUninterruptibly();
		try {
			return model.modelLoadData;
		} finally {
			mutexGetSetModel.release();
		}
	}

	/**
	 * Speichert das aktuelle Modell in einer Datei. Wird <code>null</code> als Parameter angegeben, so wird zunächst ein Dateiauswahldialog angezeigt.
	 * @param file	Name der Datei, in der das Modell gespeichert werden soll. Wird <code>null</code> übergeben, so wird ein Dialog zur Auswahl der Datei angezeigt.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String saveModel(File file) {
		EditModel model=getModel();

		if (file==null) {
			file=XMLTools.showSaveDialog(getParent(),SAVE_MODEL,SetupData.getSetup().defaultSaveFormatModels);
			if (file==null) return null;
		}

		if (file.exists() && (lastFile==null || !lastFile.equals(file))) {
			if (!MsgBox.confirmOverwrite(getOwnerWindow(),file)) return null;
		}

		if (!model.saveToFile(file)) return SAVE_MODEL_ERROR;
		lastFile=file;
		modelOriginal=getModel(); /* Neuen Abzug von Editor-Modell erstellen. In lokaler Variante wurde evtl. die Versionskennung beim Speichern aktualisiert. */
		modelChanged=false;
		fireChangedStateListeners();
		return null;
	}

	/**
	 * Speichert eine Kopie des aktuellen Modells in einer Datei.<br>
	 * Der Verändert-Status usw. wird dabei nicht geändert.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String saveModelCopy() {
		EditModel model=getModel();

		final File file=XMLTools.showSaveDialog(getParent(),SAVE_MODEL,SetupData.getSetup().defaultSaveFormatModels);
		if (file==null) return null;

		if (file.exists() && (lastFile==null || !lastFile.equals(file))) {
			if (!MsgBox.confirmOverwrite(getOwnerWindow(),file)) return null;
		}

		if (!model.saveToFile(file)) return SAVE_MODEL_ERROR;
		return null;
	}

	/**
	 * Lädt das Modell aus der angegebenen Datei. Wird <code>null</code> als Parameter angegeben, so wird zunächst ein Dateiauswahldialog angezeigt.
	 * @param file	Dateiname der Quelldatei (kann auch <code>null</code> sein; in diesem Fall fragt die Methode per Dialog selbst nach dem Dateinamen.)
	 * @return Gibt <code>null</code> zurück, wenn das Modell erfolgreich geladen wurde. Andernfalls wird die Fehlermeldung als String zurückgegeben.
	 */
	public final String loadModel(File file) {
		if (file==null) {
			file=XMLTools.showLoadDialog(getParent(),LOAD_MODEL);
			if (file==null) return null;
		}

		final EditModel newModel=new EditModel();
		final long loadStart=System.currentTimeMillis();
		final String error=newModel.loadFromFile(file);
		lastLoadTime=System.currentTimeMillis()-loadStart;
		if (error!=null) return error;

		if (!ModelSecurityCheckDialog.doSecurityCheck(newModel,this)) {
			return Language.tr("ModelSecurityCheck.LoadingCanceled");
		}

		if (EditModelBase.isNewerVersionSystem(newModel.version,EditModel.systemVersion)) {
			MsgBox.warning(getOwnerWindow(),NEWER_VERSION_TITLE,String.format(NEWER_VERSION_INFO,newModel.version));
		} else {
			if (newModel.isUnknownElementsOnLoad()) {
				MsgBox.warning(getOwnerWindow(),UNKNOWN_ELEMENTS_TITLE,UNKNOWN_ELEMENTS_INFO);
			}
		}

		FilePathHelper.checkFilePaths(newModel,file);
		setModel(newModel,false);
		lastFile=file;
		return null;
	}

	/**
	 * Lädt das Modell aus dem angegebenen Eingabe-Stream. Wird <code>null</code> als Parameter angegeben, so wird zunächst ein Dateiauswahldialog angezeigt.
	 * @param stream	Eingabe-Stream (kann auch <code>null</code> sein; in diesem Fall fragt die Methode per Dialog nach dem Dateinamen.)
	 * @return Gibt <code>null</code> zurück, wenn das Modell erfolgreich geladen wurde. Andernfalls wird die Fehlermeldung als String zurückgegeben.
	 */

	public final String loadModelFromStream(final InputStream stream) {
		if (stream==null) return loadModel(null);

		final EditModel newModel=new EditModel();
		final long loadStart=System.currentTimeMillis();
		final String error=newModel.loadFromStream(stream);
		lastLoadTime=System.currentTimeMillis()-loadStart;
		if (error!=null) return error;

		if (!ModelSecurityCheckDialog.doSecurityCheck(newModel,this)) {
			return Language.tr("ModelSecurityCheck.LoadingCanceled");
		}

		if (EditModelBase.isNewerVersionSystem(newModel.version,EditModel.systemVersion)) {
			MsgBox.warning(getOwnerWindow(),NEWER_VERSION_TITLE,String.format(NEWER_VERSION_INFO,newModel.version));
		} else {
			if (newModel.isUnknownElementsOnLoad()) {
				MsgBox.warning(getOwnerWindow(),UNKNOWN_ELEMENTS_TITLE,UNKNOWN_ELEMENTS_INFO);
			}
		}

		setModel(newModel,false);
		lastFile=null;
		return null;
	}

	/**
	 * Lädt das Modell aus dem angegebenen root Element.
	 * @param root	XML-Root-Element
	 * @param fileName	Dateiname der Quelldatei
	 * @return Gibt <code>null</code> zurück, wenn das Modell erfolgreich geladen wurde. Andernfalls wird die Fehlermeldung als String zurückgegeben.
	 */
	public final String loadModel(final Element root, final File fileName) {
		final EditModel newModel=new EditModel();
		final long loadStart=System.currentTimeMillis();
		final String error=newModel.loadFromXML(root);
		lastLoadTime=System.currentTimeMillis()-loadStart;
		if (error!=null) return error;

		if (!ModelSecurityCheckDialog.doSecurityCheck(newModel,this)) {
			return Language.tr("ModelSecurityCheck.LoadingCanceled");
		}

		if (EditModelBase.isNewerVersionSystem(newModel.version,EditModel.systemVersion)) {
			MsgBox.warning(getOwnerWindow(),NEWER_VERSION_TITLE,String.format(NEWER_VERSION_INFO,newModel.version));
		} else {
			if (newModel.isUnknownElementsOnLoad()) {
				MsgBox.warning(getOwnerWindow(),UNKNOWN_ELEMENTS_TITLE,UNKNOWN_ELEMENTS_INFO);
			}
		}

		FilePathHelper.checkFilePaths(newModel,fileName);
		setModel(newModel,false);
		lastFile=fileName;
		return null;
	}

	private long lastLoadTime=0;

	/**
	 * Liefert die Zeitdauer, die zum Laden des letzten Modells notwendig war
	 * @return	Zeitdauer in ms
	 */
	public long getLastLoadTime() {
		return lastLoadTime;
	}

	/**
	 * Setzt den zuletzt beim Speichern verwendeten Namen.<br>
	 * (Diese Methode muss nicht beim Speichern manuell aufgerufen werden.)
	 * @param lastFile	Neuer Name, der als zuletzt verwendeter Name angegeben werden soll.
	 */
	public final void setLastFile(final File lastFile) {
		this.lastFile=lastFile;
	}

	/**
	 * Liefert den zuletzt beim Laden oder Speichern des Modells verwendeten Dateinamen.<br>
	 * Wurde das Modell zuletzt per <code>setModel</code> ersetzt, so wird <code>null</code> zurückgegeben.
	 * @return Zuletzt verwendeter Dateiname
	 */
	public final File getLastFile() {
		return lastFile;
	}

	/**
	 * Gibt an, ob das Modell seit dem letzten Speichern verändert wurde.
	 * @return Liefert <code>true</code> zurück, wenn das Modell geändert wurde.
	 */
	public final boolean isModelChanged() {
		/* Ähnlich wie getModel(), aber ohne eine Kopie zu erstellen. */
		mutexGetSetModel.acquireUninterruptibly();
		try {
			if (isGUIReady) writeGUIDataToModel();
			if (!modelChanged) {
				modelChanged=(modelOriginal!=null) && !modelOriginal.equalsEditModel(model);
			}
		} finally {
			mutexGetSetModel.release();
			if (modelChanged) fireChangedStateListeners();
		}

		return modelChanged;
	}

	/**
	 * Setzt den Geändert-Status des Modells. Diese Funktion muss eigentlich nie aufgerufen werden; wird das Modell
	 * im Editor geändert, so wird es automatisch auf "geändert" gesetzt und beim Speichern wird "geändert" automatisch zurückgesetzt.
	 * @param changed	Gibt an, ob das Modell als seit dem letzten Speichern geändert angesehen werden soll oder nicht.
	 */
	public final void setModelChanged(final boolean changed) {
		final boolean fireListeners=(modelChanged!=changed);
		modelChanged=changed;
		if (!modelChanged && model!=null) modelOriginal=model.clone();
		if (fireListeners) fireChangedStateListeners();
	}

	/**
	 * Fügt ggf. ein <code>JTabbedPane</code> in den Content-Bereich des Panels ein und fügt ein weiteres Tab zu diesem <code>JTabbedPane</code> hinzu.
	 * @param name	Titel des neuen Tabs.
	 * @param icon	Optionales Icon für das Tab (kann auch <code>null</code> sein).
	 * @return	Panel innerhalb des neuen Tabs.
	 */
	protected final JPanel addTab(final String name, final URL icon) {
		if (tabs==null) {
			setLayout(new BorderLayout());
			add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		}

		JPanel p;
		tabs.addTab(name,p=new JPanel());
		if (icon!=null) tabs.setIconAt(tabs.getTabCount()-1,new ImageIcon(icon));

		p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS));

		return p;
	}

	/**
	 * Liefert den 0-basierenden Index des momentan aktiven Tabs
	 * @return	Index des aktiven Tabs
	 */
	public int getCurrentTabIndex() {
		return tabs.getSelectedIndex();
	}

	/**
	 * Fügt eine Textzeile zu einem Panel hinzu
	 * @param parent	Übergeordnetes Panel
	 * @param title	Anzuzeigende Textzeile
	 */
	public static final void addLabel(final JPanel parent, final String title) {
		if (title==null || title.isEmpty()) return;

		JPanel p;
		parent.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		JLabel label;
		p.add(label=new JLabel(title));
		Dimension maxSize=p.getMaximumSize();
		maxSize.height=label.getPreferredSize().height;
		p.setMaximumSize(maxSize);
	}

	/**
	 * Fügt eine Eingabezeile zu einem Panel hinzu
	 * @param parent	Übergeordnetes Panel
	 * @param title	Über der Eingabezeile anzuzeigender Titel (wird <code>null</code> übergeben, so wird kein Titel angezeigt)
	 * @param readOnly	Gibt an, ob der Text in dem Feld geändert werden darf
	 * @return	Neu eingefügte Eingabezeile
	 */
	public static final JTextField addInputLine(final JPanel parent, final String title, final boolean readOnly) {
		addLabel(parent,title);

		JPanel p,p2;
		JTextField text;

		parent.add(p=new JPanel(new BorderLayout()));
		p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.LINE_AXIS));
		p2.add(Box.createHorizontalStrut(3));
		p2.add(text=new JTextField(),BorderLayout.CENTER);
		p2.add(Box.createHorizontalStrut(3));

		Dimension maxSize=p.getMaximumSize();
		maxSize.height=text.getPreferredSize().height;
		p.setMaximumSize(maxSize);
		p.setMinimumSize(maxSize);

		if (readOnly) text.setEditable(false);
		return text;
	}

	/**
	 * Fügt ein mehrzeiliges Eingabefeld zu einem Panel hinzu
	 * @param parent	Übergeordnetes Panel
	 * @param title	Über dem Eingabefeld anzuzeigender Titel (wird <code>null</code> übergeben, so wird kein Titel angezeigt)
	 * @param readOnly	Gibt an, ob der Text in dem Feld geändert werden darf
	 * @return	Neu eingefügtes Eingabefeld
	 */
	public static final JTextArea addInputArea(final JPanel parent, final String title, final boolean readOnly) {
		addLabel(parent,title);

		JPanel p,p2;
		JTextArea text;

		parent.add(p=new JPanel(new BorderLayout()));
		p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.LINE_AXIS));
		p2.add(Box.createHorizontalStrut(3));
		p2.add(new JScrollPane(text=new JTextArea()),BorderLayout.CENTER);
		p2.add(Box.createHorizontalStrut(3));

		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		if (readOnly) text.setEditable(false);
		return text;
	}

	/**
	 * Fügt einen Verteilungseditor zu einem Panel hinzu
	 * @param parent	Übergeordnetes Panel
	 * @param title	Über dem Verteilungseditor anzuzeigender Titel (wird <code>null</code> übergeben, so wird kein Titel angezeigt)
	 * @param maxXValue	Maximalwert, der auf der x-Achse im Verteilungseditor angezeigt werden soll
	 * @param readOnly Gibt an, ob die Verteilung bearbeitet werden darf
	 * @return	Neu eingefügter Verteilungseditor
	 */
	public static final JDistributionPanel addDistribution(final JPanel parent, final String title, final double maxXValue, final boolean readOnly) {
		addLabel(parent,title);

		JPanel p;

		parent.add(p=new JPanel(new BorderLayout()));
		JDistributionPanel dist=new JDistributionPanel(null,maxXValue,!readOnly);
		p.add(dist,BorderLayout.CENTER);

		return dist;
	}

	/**
	 * Fügt eine Reihe von zusammengehörenden Radiobuttons zu einem Panel hinzu
	 * @param parent	Übergeordnetes Panel
	 * @param title	Über den Radiobuttons anzuzeigender Titel (wird <code>null</code> übergeben, so wird kein Titel angezeigt)
	 * @param options	Bezeichnungen für die Radiobuttons
	 * @return	Array aus den neu eingefügten Radiobuttons
	 */
	public static final JRadioButton[] addOptions(final JPanel parent, final String title, final String[] options) {
		addLabel(parent,title);

		JPanel p,p2;

		parent.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(p2=new JPanel());
		p2.setLayout(new BoxLayout(p2,BoxLayout.PAGE_AXIS));

		ButtonGroup group=new ButtonGroup();
		List<JRadioButton> buttons=new ArrayList<>();
		for (String option: options) {
			JRadioButton button=new JRadioButton(option);
			buttons.add(button);
			group.add(button);
			p2.add(button);
		}
		return buttons.toArray(new JRadioButton[0]);
	}

	/**
	 * Fügt zu einer Eingabe-Komponente einen <code>KeyListener</code> hinzu, der das angegebene
	 * <code>Runnable</code> bei jedem Tastendruck ausführt und so eine kontinuierliche Prüfung der
	 * Eingaben ermöglicht
	 * @param component	Element bei dem auf die Tastendrücke reagiert werden soll
	 * @param check	Objekt vom Typ <code>Runnable</code>, das bei Tastendrücken aktiviert werden soll
	 */
	public static final void addCheckInput(final Component component, final Runnable check) {
		component.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {check.run();}
			@Override public void keyReleased(KeyEvent e) {check.run();}
			@Override public void keyPressed(KeyEvent e) {check.run();}
		});
	}

	private final Semaphore fireChangedStateListenersRunning = new Semaphore(1);

	private void fireChangedStateListeners() {
		if (!fireChangedStateListenersRunning.tryAcquire()) return;
		try {
			for (Runnable runnable: changedStateListeners) runnable.run();
		} finally {
			fireChangedStateListenersRunning.release();
		}
	}

	/**
	 * Fügt einen Listener zu der Liste der zu benachrichtigenden Listener hinzu, die benachrichtigt werden, wenn sich der Geändert-Status des Modells ändert.
	 * @param changedStateListener	Neuer Listener, der benachrichtigt werden soll, wenn sich der Geändert-Status des Modells ändert.
	 * @return	Gibt <code>true</code> zurück, wenn der Listener noch nicht in der Liste der zu benachrichtigenden Listener war und folglich neu hinzugefügt werden konnte.
	 */
	public final boolean addChangedStateListeners(final Runnable changedStateListener) {
		return changedStateListeners.add(changedStateListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der zu benachrichtigenden Listener, die benachrichtigt werden, wenn sich der Geändert-Status des Modells ändert.
	 * @param changedStateListener	Listener, der nicht mehr benachrichtigt werden soll, wenn sich der Geändert-Status des Modells ändert.
	 * @return	Gibt <code>true</code> zurück, wenn der Listener in der Liste der zu benachrichtigenden Listener war und folglich entfernt werden konnte.
	 */
	public final boolean removeChangedStateListeners(final Runnable changedStateListener) {
		return changedStateListeners.remove(changedStateListener);
	}

}