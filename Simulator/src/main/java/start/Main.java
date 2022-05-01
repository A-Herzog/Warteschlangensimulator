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
package start;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import mathtools.Table;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionEditorPanelRecord;
import systemtools.BaseDialog;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.MsgBoxBackendTaskDialog;
import systemtools.statistics.PDFWriter;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.MainFrame;
import ui.MainPanel;
import ui.UpdateSystem;
import ui.commandline.CommandLineSystem;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.FontCache;
import ui.modeleditor.elements.ModelElementImage;
import ui.tools.FlatLaFHelper;
import xml.XMLTools;

/**
 * Main-Klasse des Simulators
 * Der Simulator kann über diese Klasse sowohl im GUI- als auch im Kommandozeilen-Modus gestartet werden.
 * @author Alexander Herzog
 */
public class Main {
	/**
	 * Wird der Simulator mit einem einfachen Dateinamen als Parameter aufgerufen, so wird angenommen, dass es sich dabei
	 * um eine zu ladende Datei handelt. Diese wird hier gespeichert.
	 */
	private static File loadFile;

	/**
	 * Verarbeitet mögliche Kommandozeilen-Parameter
	 * @param args	Die an <code>main</code> übergebenen Parameter
	 * @return	Gibt <code>true</code> zurück, wenn alle Verarbeitungen bereits auf der Kommandozeile ausgeführt werden konnten und die grafische Oberfläche nicht gestartet werden muss
	 */
	private static boolean processCommandLineArguments(final String[] args) {
		if (args.length==0) return false;

		final CommandLineSystem commandLineSystem=new CommandLineSystem();
		loadFile=commandLineSystem.checkLoadFile(args);
		if (loadFile==null) {
			if (commandLineSystem.run(args)) {
				ModelElementImage.shutdownBackgroundLoadService();
				return true;
			}
		}

		return false;
	}

	/**
	 * Nimmt alle GUI-unabhängigen Vorbereitungen zum Start des Simulators vor.
	 */
	public static void prepare() {
		/* Sprache */
		Language.init(SetupData.getSetup().language);
		LanguageStaticLoader.setLanguage();
		if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();

		/* Basiseinstellungen zu den xml-Dateiformaten */
		XMLTools.homeURL=MainPanel.HOME_URL;
		XMLTools.mediaURL="https://"+XMLTools.homeURL+"/Warteschlangensimulator/";
		XMLTools.dtd="Simulator.dtd";
		XMLTools.xsd="Simulator.xsd";

		/* Cache-Ordner für PDFWriter einstellen */
		PDFWriter.cacheFolder=SetupData.getSetupFolder();

		/* Programmname für Export */
		Table.ExportTitle=MainFrame.PROGRAM_NAME;
		StatisticsBasePanel.program_name=MainFrame.PROGRAM_NAME;

		/* Update */
		UpdateSystem.getUpdateSystem();
	}

	/**
	 * Hauptroutine des gesamten Programms
	 * @param args	Kommandozeilen-Parameter
	 */
	public static void main(final String[] args) {
		/* Jede Art der Log4J-Erfassung deaktivieren. */
		final Properties systemProperties=System.getProperties();
		systemProperties.setProperty("org.apache.logging.log4j.level","OFF"); /* wird von org.apache.logging.log4j.core.config.AbstractConfiguration.setToDefault() gelesen */
		systemProperties.setProperty("log4j2.formatMsgNoLookups","true"); /* wird von org.apache.logging.log4j.core.util.Constants gelesen */

		/* System initialisieren */
		try {
			prepare();
		} catch (NoClassDefFoundError e) {
			if (GraphicsEnvironment.isHeadless()) {
				System.out.println("The required libraries in the \"libs\" subfolder are missing.");
				System.out.println("Therefore, the program cannot be executed.");
			} else {
				JOptionPane.showMessageDialog(null,"The required libraries in the \"libs\" subfolder are missing.\nTherefore, the program cannot be executed.","Missing libraries",JOptionPane.ERROR_MESSAGE);
			}
			return;
		}

		/* Parameter verarbeiten */
		if (args.length>0) {
			if (processCommandLineArguments(args)) return;
		}

		/* Grafische Oberfläche verfügbar? */
		if (!GUITools.isGraphicsAvailable()) return;

		/* Grafische Oberfläche starten */
		try {
			SwingUtilities.invokeAndWait(new RunSimulator());
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ausführen der grafischen Oberfläche über ein <code>invokeLater</code>.
	 */
	private static final class RunSimulator implements Runnable {
		@Override
		public void run() {
			final SetupData setup=SetupData.getSetup();

			/* Look & Feel */
			FlatLaFHelper.init();
			FlatLaFHelper.setCombinedMenuBar(setup.lookAndFeelCombinedMenu);
			GUITools.setupUI(setup.lookAndFeel);
			FlatLaFHelper.setup();

			/* Skalierung */
			final double scaling=setup.scaleGUI;
			GUITools.setupFontSize(scaling);
			BaseDialog.windowScaling=scaling;

			/* Meldungsdialoge */
			MsgBox.setBackend(new MsgBoxBackendTaskDialog());

			/* Filter für Verteilungsliste in Verteilungseditoren */
			JDistributionEditorPanel.registerFilterGetter(()->{
				final String s=setup.distributionListFilter.trim();
				return (s.isEmpty())?String.join("\n",JDistributionEditorPanelRecord.getDefaultHighlights()):s;
			});
			JDistributionEditorPanel.registerFilterSetter(list->{
				setup.distributionListFilter=list;
				setup.saveSetup();
			});

			/* Schriftart */
			if (setup.fontName!=null && !setup.fontName.trim().isEmpty()) {
				GUITools.setFontName(setup.fontName);
				ModelElementBox.DEFAULT_FONT_LARGE=new Font(setup.fontName,ModelElementBox.DEFAULT_FONT_LARGE.getStyle(),ModelElementBox.DEFAULT_FONT_LARGE.getSize());
				ModelElementBox.DEFAULT_FONT_SMALL=new Font(setup.fontName,ModelElementBox.DEFAULT_FONT_SMALL.getStyle(),ModelElementBox.DEFAULT_FONT_SMALL.getSize());
				ModelElementBox.DEFAULT_FONT_TYPE=setup.fontName;
				FontCache.defaultFamily.name=setup.fontName;
			}

			/* Start */
			new MainFrame(loadFile,null);
		}
	}
}