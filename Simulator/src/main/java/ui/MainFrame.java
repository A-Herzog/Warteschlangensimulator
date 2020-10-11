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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import javax.swing.SwingUtilities;

import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.MainFrameBase;
import systemtools.MainPanelBase;
import systemtools.MsgBox;
import tools.SetupData;

/**
 * Diese Klasse stellt das Programmfenster des Simulators dar.
 * @see MainFrameBase
 * @author Alexander Herzog
 */
public class MainFrame extends MainFrameBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2208131980436341851L;

	/**
	 * Programmname
	 */
	public static final String PROGRAM_NAME="Warteschlangensimulator";

	/**
	 * Dateiname des Taskleisten-Symbols f�r das Programm
	 * @see #ICON_URL
	 */
	private static final String ICON="res/Symbol.png";

	/**
	 * Ressourcen-URL f�r das Taskleisten-Symbol f�r das Programm
	 */
	public static final URL ICON_URL=MainFrame.class.getResource(ICON);

	/**
	 * Konstruktor der Klasse
	 * @param loadFile	Datei, die beim Start geladen werden soll. Wird <code>null</code> �bergeben, so wird nichts weiter geladen.
	 * @param loadExample	Beispielmodell, das beim Start geladen werden soll. Wird <code>null</code> �bergeben, so wird nichts weiter geladen.
	 */
	public MainFrame(final File loadFile, final EditModel loadExample) {
		super(PROGRAM_NAME,loadFile);

		final MainPanelBase panel=new MainPanel(this,PROGRAM_NAME,false);
		setMainPanel(panel);

		if (panel instanceof MainPanel) ((MainPanel)getMainPanel()).setReloadWindow(new ReloadWindow());
		setIcon(ICON_URL);
		if (loadExample!=null) {
			((MainPanel)panel).editorPanel.setModel(loadExample);
			((MainPanel)panel).fileLoadedOnLoad=true;
		}

		setVisible(true);
		ReloadManager.add(this);
	}

	/**
	 * Minimale anf�ngliche Fenstergr��e bezogen auf eine 100%-Skalierung.
	 * @see #getScaledDefaultSize(double)
	 */
	private static Dimension minMainWindowSize=new Dimension(1024,768);

	private Dimension getScaledDefaultSize(double scale) {
		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension(Math.min(screenSize.width-50,(int)Math.round(minMainWindowSize.width*scale)),Math.min(screenSize.height-50,(int)Math.round(minMainWindowSize.height*scale)));
	}

	@Override
	protected void loadWindowSize() {
		setSize(getScaledDefaultSize(SetupData.getSetup().scaleGUI));
		setMinimumSize(getSize());
		setLocationRelativeTo(null);

		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		final SetupData setup=SetupData.getSetup();
		switch (setup.startSizeMode) {
		case START_MODE_FULLSCREEN:
			setExtendedState(Frame.MAXIMIZED_BOTH);
			break;
		case START_MODE_LASTSIZE:
			setExtendedState(setup.lastSizeMode);
			if (setup.lastSizeMode==Frame.NORMAL) {
				final Dimension minSize=getMinimumSize();
				Dimension d=setup.lastSize;
				if (d.width<minSize.width) d.width=minSize.width;
				if (d.width>screenSize.width) d.width=screenSize.width;
				if (d.height<minSize.height) d.height=minSize.height;
				if (d.height>screenSize.height) d.height=screenSize.height;
				setSize(d);

				Point point=setup.lastPosition;
				if (point.x<0) point.x=0;
				if (point.x>=screenSize.width-50) point.x=screenSize.width-50;
				if (point.y<0) point.y=0;
				if (point.y>=screenSize.height-50) point.y=screenSize.height-50;
				setLocation(point);
			}
			break;
		case START_MODE_DEFAULT:
			/* Nichts zu tun. */
			break;
		}
	}

	@Override
	protected void saveWindowSize() {
		final SetupData setup=SetupData.getSetup();
		if (setup.startSizeMode==SetupData.StartSizeMode.START_MODE_LASTSIZE) {
			setup.lastSizeMode=getExtendedState();
			setup.lastPosition=getLocation();
			setup.lastSize=getSize();
			setup.saveSetupWithWarning(this);
		}

		final MainPanelBase main=getMainPanel();
		if (main instanceof MainPanel) {
			ModelRestore.autoSave(((MainPanel)main).editorPanel.getModel());
		}
	}

	@Override
	protected void logException(final String info) {
		SetupData setup=SetupData.getSetup();
		setup.lastError=info;
		setup.saveSetup();
	}

	private class ReloadWindow implements Runnable {
		@Override
		public void run() {
			processReload();
			ReloadManager.notify(MainFrame.this,MainFrame.ReloadMode.FULL);
		}
	}

	private void processReload() {
		if (!(getMainPanel() instanceof MainPanel)) return;

		final Object[] store=((MainPanel)getMainPanel()).getAllData();

		Language.init(SetupData.getSetup().language);
		LanguageStaticLoader.setLanguage();
		if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();

		final MainPanel newMainPanel=new MainPanel(MainFrame.this,PROGRAM_NAME,true);
		setMainPanel(newMainPanel);
		newMainPanel.setReloadWindow(new ReloadWindow());
		newMainPanel.setAllData(store);
	}

	/**
	 * Welche Einstellungen sollen neu geladen werden?
	 * @author Alexander Herzog
	 * @see MainFrame#reload(ReloadMode)
	 */
	public enum ReloadMode {
		/** Daten aus Setup auslesen, Men�punkte konfigurieren, Zeichenfl�che neu zeichnen */
		SETUP,
		/** Alles neu aufbauen (z.B. nach dem Sprachwechsel notwendig) */
		FULL
	}

	/**
	 * L�dt das Fenster neu.
	 * @param reloadMode	Welche Einstellungen sollen neu geladen werden?
	 * @see MainFrame.ReloadMode
	 */
	public void reload(final ReloadMode reloadMode) {
		switch (reloadMode) {
		case SETUP:
			((MainPanel)getMainPanel()).reloadSetup(false);
			break;
		case FULL:
			SwingUtilities.invokeLater(()->processReload());
			break;
		}
	}

	@Override
	protected boolean exitProgramOnCloseWindow() {
		final SetupData setup=SetupData.getSetup();
		final File setupFile=new File(SetupData.getSetupFolder(),SetupData.SETUP_FILE_NAME);

		boolean b=setup.isLastFileSaveSuccessful();
		while (!b) {
			b=setup.saveSetup();
			if (!b) {
				if (!MsgBox.confirm(this,Language.tr("SetupFailure.Title"),String.format(Language.tr("SetupFailure.Info"),setupFile.toString()),Language.tr("SetupFailure.Retry"),Language.tr("SetupFailure.Discard"))) break;
			}
		}

		ReloadManager.remove(this);
		return ReloadManager.isEmpty();
	}

	/**
	 * Liefert das aktuell in dem Fenster dargestellte Modell
	 * @return	Aktuelles Modell
	 */
	public EditModel getModel() {
		return ((MainPanel)getMainPanel()).editorPanel.model;
	}

	/**
	 * Liefert die Statistikdaten aus der Statistikansicht in diesem Fenster
	 * @return	Aktuelle Statistikdaten (kann <code>null</code> sein)
	 */
	public Statistics getStatistics() {
		return ((MainPanel)getMainPanel()).statisticsPanel.getStatistics();
	}
}