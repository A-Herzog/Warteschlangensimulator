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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;

/**
 * Basisklasse für die Anzeige eines Programmfensters
 * Sie verbindet sich mit einem Objekt vom Type <code>MainPanelBase</code>, um Fenster-Nachrichten austauschen zu können.
 * @see MainPanelBase
 * @version 1.7
 * @author Alexander Herzog
 */
public class MainFrameBase extends JFrame {
	private static final long serialVersionUID = 8212777339799463638L;

	/**
	 * Listener, der beim Schließen des Fensters das Programm beendet.
	 * @see #quitProgram()
	 */
	private final transient WindowListener closeListener;

	/** Fenstertitel (ohne optionale Ergänzungen) */
	private final String defaultWindowTitle;

	/**
	 * Panel mit dem eigentlichen Fensterinhalt
	 * @see #getMainPanel()
	 * @see #setMainPanel(MainPanelBase)
	 */
	private MainPanelBase panel;

	/**
	 * Menüzeile des Fensters
	 * @see MainPanelBase#createMenu()
	 */
	private JMenuBar menubar;

	/**
	 * Konstruktor der Klasse <code>MainFrameBase</code>
	 * @param title	Titel des Fensters
	 * @param loadFile	Datei, die beim Start geladen werden soll. Wird <code>null</code> übergeben, so wird nichts weiter geladen.
	 */
	public MainFrameBase(final String title, final File loadFile) {
		super(title);
		defaultWindowTitle=title;

		/* Fehler abfangen und ermöglichen, diese in eine Logdatei zu schreiben */
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());

		/* Einstellungen zum Fenster als solches */
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(closeListener=new WindowAdapter() {
			@Override public void windowClosing(WindowEvent event) {quitProgram();}
		});

		/* Fenster als solches */
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
		loadWindowSize();

		/* Ggf. Datei laden */
		if (loadFile!=null) {
			SwingUtilities.invokeLater(()->{
				if (panel.loadAnyFile(loadFile,null,null,true)) {
					panel.fileLoadedOnLoad=true;
					CommonVariables.setInitialDirectoryFromFile(loadFile);
				}
			});
		}
	}

	/**
	 * Liefert das Hauptpanel, das das Fenster ausfüllt
	 * @return	Hauptpanel
	 */
	protected MainPanelBase getMainPanel() {
		return panel;
	}

	/**
	 * Stellt das Hauptpanel, das das Fenster ausfüllt, ein
	 * @param panel	Hauptpanel
	 */
	protected void setMainPanel(final MainPanelBase panel) {
		if (this.panel!=null) {
			this.panel.setVisible(false);
			getContentPane().remove(this.panel);
			setJMenuBar(null);
		}

		this.panel=panel;

		/* Haupt-Panel einrichten */
		getContentPane().add(panel);
		panel.connectToFrame(
				new UpdateWindowCallback(COMMAND_ENABLE_MENU),
				new UpdateWindowCallback(COMMAND_DISABLE_MENU),
				new UpdateWindowCallback(COMMAND_UPDATE_TITLE),
				new UpdateWindowCallback(COMMAND_QUIT),
				new DropTargetRegisterImpl()
				);

		/* Menü */
		setJMenuBar(menubar=panel.createMenu());
	}

	/**
	 * Stellt das Programm-Icon ein
	 * @param iconURL	Ressourcen-URL des Programm-Icons (wird <code>null</code> übergeben, so wird kein Icon gesetzt)
	 */
	protected final void setIcon(final URL iconURL) {
		if (iconURL!=null) setIconImage(getToolkit().getImage(iconURL));
	}

	/**
	 * Ermöglicht das Einstellen der Fenstergröße gemäß den Setup-Einstellungen nach dem Erstellen des Fensters
	 */
	protected void loadWindowSize() {}

	/**
	 * Versucht das Programm zu beenden.
	 * @see MainPanelBase#allowQuitProgram()
	 */
	public void quitProgram() {
		if (!panel.allowQuitProgram()) return;

		saveWindowSize();

		final int operation=getDefaultCloseOperation();
		final boolean fireEventAgain=(operation!=WindowConstants.EXIT_ON_CLOSE && operation!=WindowConstants.DISPOSE_ON_CLOSE);
		setDefaultCloseOperation(exitProgramOnCloseWindow()?WindowConstants.EXIT_ON_CLOSE:WindowConstants.DISPOSE_ON_CLOSE); /* <- besser als: System.exit(0); */
		setVisible(false);
		dispose();
		if (fireEventAgain) { /* Wird quitProgram() über den WindowListener aufgerufen, so ist das setzen von EXIT_ON_CLOSE schon zu spät, daher muss das Event noch mal losgeschickt werden... */
			removeWindowListener(closeListener); /* ... aber ohne noch mal den "Wollen Sie jetzt speichern"-Listener zu aktivieren. */
			processWindowEvent(new WindowEvent(this,WindowEvent.WINDOW_CLOSING));
		}
	}

	/**
	 * Soll das Programm beendet werden, wenn das Fenster geschlossen wird?<br>
	 * Im Normalfall <code>true</code>, nur wenn mehrere Fenster verwendet werden ggf. für das nicht-letzte Fenster <code>false</code>.
	 * @return	Programm beim Schließen des Fensters beenden?
	 */
	protected boolean exitProgramOnCloseWindow() {
		return true;
	}

	/**
	 * Ermöglicht das Speichern der Fenstergröße im Setup vor dem Programmende
	 */
	protected void saveWindowSize() {}

	private static final int COMMAND_DISABLE_MENU=0;
	private static final int COMMAND_ENABLE_MENU=1;
	private static final int COMMAND_UPDATE_TITLE=2;
	private static final int COMMAND_QUIT=3;

	private class UpdateWindowCallback implements Runnable {
		private final int command;

		public UpdateWindowCallback(final int command) {
			this.command=command;
		}

		private void setMenuEnable(final boolean enable) {
			for (int i=0;i<menubar.getMenuCount();i++) {
				final JMenu menu=menubar.getMenu(i);
				if (menu!=null) menu.setEnabled(enable);
			}
		}

		@Override
		public void run() {
			switch (command) {
			case COMMAND_DISABLE_MENU:
				setMenuEnable(false);
				SwingUtilities.invokeLater(new UpdateWindow());
				break;
			case COMMAND_ENABLE_MENU:
				setMenuEnable(true);
				SwingUtilities.invokeLater(new UpdateWindow());
				break;
			case COMMAND_UPDATE_TITLE:
				String s=panel.getTitleAddon();
				if (s==null || s.isEmpty()) {
					setTitle(defaultWindowTitle);
				} else {
					final String marker=(panel.getTitleAddonChanged())?"*":"";
					setTitle(defaultWindowTitle+" ["+marker+s+"]");
				}
				break;
			case COMMAND_QUIT:
				quitProgram();
				break;
			}
		}
	}

	private class UpdateWindow implements Runnable {
		@Override
		public void run() {
			panel.setVisible(false);
			panel.setVisible(true);
		}
	}

	/**
	 * Interface über das Komponenten, die auf File-Drop-Ereignisse reagieren sollen, registriert werden können.
	 * @author Alexander Herzog
	 * @see DropTargetRegister#registerJComponent(JComponent)
	 */
	public interface DropTargetRegister {
		/**
		 * Registriert eine Komponente, die auf File-Drop-Ereignisse reagieren sollen
		 * @param component	Zu registrierende Komponente
		 */
		void registerJComponent(final JComponent component);
	}

	private class DropTargetRegisterImpl implements DropTargetRegister {
		@Override
		public void registerJComponent(final JComponent component) {
			if (component==null) return;
			new FileDropper(component,e->{
				if (!(e.getSource() instanceof FileDropperData)) return;
				final FileDropperData drop=(FileDropperData)(e.getSource());
				final File file=drop.getFile();
				if (file.isFile()) {
					drop.dragDropConsumed();
					SwingUtilities.invokeLater(()->{
						if (panel.loadAnyFile(file,drop.getDropComponent(),drop.getDropPosition(),true)) {
							CommonVariables.setInitialDirectoryFromFile(file);
						}
					});
				}
			});
		}
	}

	/**
	 * Diese Methode wird aufgerufen, wenn im Programm eine nicht behandelte Exception aufgetreten ist.
	 * Über diese Methode kann die Exception in eine Logdatei geschrieben werden
	 * @param info	Meldungstext der Exception
	 */
	protected void logException(final String info) {}

	private final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
		private String getTimeStamp() {
			Calendar cal=Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(cal.getTime());
		}

		@Override
		public void uncaughtException(final Thread t, final Throwable e) {
			try {
				logException(getTimeStamp()+": "+e.toString()+": "+getStackTrace(e));
			} finally {
				Thread.setDefaultUncaughtExceptionHandler(null);
				Thread.currentThread().getThreadGroup().uncaughtException(t,e);
				Thread.setDefaultUncaughtExceptionHandler(this);
			}
		}

		private String getStackTrace(final Throwable aThrowable) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			aThrowable.printStackTrace(printWriter);
			return result.toString();
		}
	}
}