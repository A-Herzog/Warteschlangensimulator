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
package tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.formdev.flatlaf.FlatLightLaf;

import gitconnect.GitSetup;
import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import mathtools.NumberTools;
import scripting.java.SimDynamicSetup;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelProcessor;
import simulator.simparser.ExpressionCalcUserFunctionsManager;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.GUITools;
import systemtools.SetupBase;
import systemtools.statistics.ChartSetup;
import systemtools.statistics.ReportStyle;
import ui.EditorPanelStatistics;
import ui.MainFrame;
import ui.UpdateSystem;
import ui.infopanel.InfoPanel;
import ui.modeleditor.HeatMapImage;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.elements.NextStationHelper;
import ui.script.ScriptEditorAreaBuilder;
import ui.statistics.StatisticViewerOverviewText;
import ui.tools.BatchPanel;
import xml.XMLTools;

/**
 * Diese Klasse kapselt alle Setup-Daten des Programms und automatisiert das Laden und Speichern der Daten
 * @see SetupBase
 * @author Alexander Herzog
 */
public class SetupData extends SetupBase {
	/**
	 * Fenstergröße beim Programmstart
	 * @see SetupData#startSizeMode
	 */
	public enum StartSizeMode {
		/** Starten des Programms mit Vorgabe-Fenstergröße */
		START_MODE_DEFAULT,

		/** Starten des Programms im Vollbildmodus */
		START_MODE_FULLSCREEN,

		/** Wiederherstellung der letzten Fenstergröße beim Programmstart */
		START_MODE_LASTSIZE
	}

	/**
	 * Sichtbarkeit der Elemente-Vorlagen-Leiste beim Programmstart
	 * @see SetupData#startTemplateMode
	 */
	public enum StartTemplateMode {
		/** Starten des Programms mit ausgeblendeter Elemente-Vorlagen-Leiste */
		START_TEMPLATE_HIDDEN,

		/** Starten des Programms mit eingeblendeter Elemente-Vorlagen-Leiste */
		START_TEMPLATE_VISIBLE,

		/** Wiederherstellung des letzten Sichtbarkeitsstatus der Elemente-Vorlagen-Leiste beim Programmstart */
		START_TEMPLATE_LASTSTATE
	}

	/**
	 * Gibt an, wie mit der Einschwingphase in der Animation umgegangen werden soll.
	 * @see SetupData#animationWarmUpMode
	 */
	public enum AnimationMode {
		/** Einschwingphase in der Animation immer normal ausführen. */
		ANIMATION_WARMUP_NORMAL,

		/** Fragen, ob die Einschwingphase deaktiviert oder im Schnellvorlauf durchgeführt werden soll, wenn Statistik-Anzeige-Elemente vorhanden sind. */
		ANIMATION_WARMUP_ASK,

		/** Einschwingphase deaktivieren, wenn Statistik-Anzeige-Elemente vorhanden sind. */
		ANIMATION_WARMUP_SKIP,

		/** Einschwingphase im schnellen Vorlauf ohne Visualisierung durchlaufen. */
		ANIMATION_WARMUP_FAST
	}

	/**
	 * Modell im Hintergrund prüfen und in Statuszeile anzeigen, ob Fehler vorliegen.
	 * @see SetupData#backgroundSimulation
	 */
	public enum BackgroundProcessingMode {
		/** Keine Hintergrundverarbeitung */
		BACKGROUND_NOTHING,

		/** Modell im Hintergrund prüfen, keine Hintergrundsimulation */
		BACKGROUND_CHECK_ONLY,

		/** Modell im Hintergrund prüfen und Simulation starten, wenn genügend Leistung verfügbar */
		BACKGROUND_SIMULATION,

		/** Modell im Hintergrund prüfen und auch immer simulieren */
		BACKGROUND_SIMULATION_ALWAYS
	}

	/**
	 * Modell automatisch speichern?
	 * @see SetupData#autoSaveMode
	 */
	public enum AutoSaveMode {
		/** Keine automatische Speicherung */
		AUTOSAVE_OFF,

		/** Vor dem Start von Simulationen automatisch speichern */
		AUTOSAVE_SIMULATION,

		/** Alle Änderungen automatisch speichern */
		AUTOSAVE_ALWAYS
	}

	/**
	 * Wie soll beim Laden von Modellen mit potentiell sicherheitskritischen Elementen verfahren werden?
	 * @see SetupData#modelSecurity
	 */
	public enum ModelSecurity {
		/** Modell mit potentiell sicherheitskritischen Element nie laden */
		STRICT,

		/** Nutzer beim Laden von Modellen mit potentiell sicherheitskritischen Element fragen */
		ASK,

		/** Alle Modell ohne Prüfung laden */
		ALLOWALL,
	}

	/**
	 * Benachrichtigung beim Ende von Simulation, Parameterreihe oder Optimierung in System-Tray anzeigen?
	 * @see SetupData#notifyMode
	 */
	public enum NotifyMode {
		/** Keine Benachrichtigungen anzeigen */
		OFF,

		/** Benachrichtigungen anzeigen, wenn Operation lauge gedauert hat */
		LONGRUN,

		/** Immer über Abschluss von Simulation usw. benachrichtigen */
		ALWAYS
	}

	/**
	 * Aufzeichnungsmodus: in Datei oder per DDE zu Excel
	 * @see SetupData#logMode
	 */
	public enum LogMode {
		/** Logging-Daten in Datei speichern */
		FILE,

		/** Logging-Daten per DDE zu Excel transferieren */
		DDE
	}

	/**
	 * Wie soll nach dem Abbruch einer Simulation oder Animation
	 * mit den unvollständigen Statistikdaten umgegangen werden?
	 * @see SetupData#canceledAnimationStatistics
	 * @see SetupData#canceledSimulationStatistics
	 */
	public enum CanceledSimulationStatistics {
		/** Unvollständige Statistik verwerfen */
		OFF("Off"),
		/** Nutzer fragen, ob die unvollständige Statistik angezeigt werden soll */
		ASK("Ask"),
		/** Unvollständige Statistik immer anzeigen */
		SHOW("Show");

		/** Name des Statistik-Modus zum Speichern in der Konfiguration */
		public final String name;

		/**
		 * Konstruktor des Enum
		 * @param name	Name des Statistik-Modus zum Speichern in der Konfiguration
		 */
		CanceledSimulationStatistics(final String name) {
			this.name=name;
		}

		/**
		 * Liefert zu einem Namen das passende Statistik-Modus-Enum.
		 * @param name	Name (aus der Konfiguration geladen)
		 * @param defaultValue	Fallback-Wert falls es keinen Eintrag mit dem angegebenen Namen gibt
		 * @return	Passendes Enum (oder Fallback-Wert)
		 * @see #name
		 */
		public static CanceledSimulationStatistics getByName(final String name, final CanceledSimulationStatistics defaultValue) {
			for (CanceledSimulationStatistics mode: values()) if (mode.name.equalsIgnoreCase(name)) return mode;
			return defaultValue;
		}
	}

	/**
	 * Modus für Zahlen- und Datumsformat
	 */
	public enum NumberFormat {
		/** Gemäß gewählter Programmsprache festlegen */
		BY_LANGUAGE("language"),
		/** Gemäß Betriebssystemvorgabe festlegen */
		BY_SYSTEM("os"),
		/** Immer Dezimalkomma verwenden */
		COMMA("comma"),
		/** Immer Dezimalpunkt verwenden */
		POINT("point");

		/**
		 * Konstruktor des Enum
		 * @param name	Name für den Zahlenformat-Modus
		 */
		NumberFormat(final String name) {
			this.name=name;
		}

		/**
		 * Name des Zahlenformat-Modus
		 */
		public final String name;

		/**
		 * Liefert den Zahlenformat-Modus auf Basis eines Namens.
		 * @param name	Name für den der Zahlenformat-Modus ermittelt werden soll
		 * @return	Zahlenformat-Modus
		 */
		public static NumberFormat fromName(final String name) {
			for (var numberFormat: values()) if (numberFormat.name.equalsIgnoreCase(name)) return numberFormat;
			return BY_SYSTEM;
		}
	}

	/**
	 * Programmsprache
	 */
	public String language;

	/**
	 * Modus für Zahlen- und Datumsformat
	 * @see NumberFormat
	 */
	public NumberFormat numberFormat;

	/**
	 * Fenstergröße beim Programmstart
	 * @see StartSizeMode#START_MODE_DEFAULT
	 * @see StartSizeMode#START_MODE_FULLSCREEN
	 * @see StartSizeMode#START_MODE_LASTSIZE
	 */
	public StartSizeMode startSizeMode;

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier gespeichert, ob das Fenster im Vollbildmodus dargestellt wird oder nicht
	 */
	public int lastSizeMode;

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier die letzte Position des Fensters gespeichert
	 */
	public Point lastPosition;

	/**
	 * Ist startSizeMode=START_MODE_LASTSIZE gewählt, so wird hier die letzte Größe des Fensters gespeichert
	 */
	public Dimension lastSize;

	/**
	 * Zuletzt eingestellter Zoomfaktor
	 */
	public double lastZoom;

	/**
	 * Fenstergrößen von Untermodell-Editoren wiederherstellen?
	 */
	public boolean restoreSubEditWindowSize;

	/**
	 * Sichtbarkeit der Elemente-Vorlagen-Leiste beim Programmstart
	 * @see StartTemplateMode#START_TEMPLATE_HIDDEN
	 * @see StartTemplateMode#START_TEMPLATE_VISIBLE
	 * @see StartTemplateMode#START_TEMPLATE_LASTSTATE
	 */
	public StartTemplateMode startTemplateMode;

	/**
	 * Beispiel-Modell, welches beim Starten geöffnet werden soll
	 */
	public String startModel;

	/**
	 * Wann sollen Info-Texte direkt auf der Zeichenfläche angezeigt werden?
	 * @author Alexander Herzog
	 * @see SetupData#surfaceHelp
	 */
	public enum SurfaceHelp {
		/**
		 * Nie
		 */
		NEVER,

		/**
		 * Nur direkt nach dem Start
		 */
		START_ONLY,

		/**
		 * Immer, wenn die Zeichenfläche leer ist
		 */
		ALWAYS
	}

	/**
	 * Wann sollen Info-Texte direkt auf der Zeichenfläche angezeigt werden?
	 */
	public SurfaceHelp surfaceHelp;

	/**
	 * Sollten halbtransparente Hinweise auf der Zeichenfläche angezeigt werden?
	 */
	public boolean surfaceGlassInfos;

	/**
	 * Skalierung der Programmoberfläche
	 */
	public double scaleGUI;

	/**
	 * Zu verwendendes Theme
	 * @see GUITools#listLookAndFeels()
	 */
	public String lookAndFeel;

	/**
	 * Soll die Menüzeile wenn möglich in die Titelzeile integriert werden?
	 */
	public boolean lookAndFeelCombinedMenu;

	/**
	 * Modell automatisch speichern?
	 */
	public AutoSaveMode autoSaveMode;

	/**
	 * Gibt an, in welchen Dialogen Hinweise angezeigt werden sollen.
	 * @see InfoPanel
	 */
	public String hintDialogs;

	/**
	 * Gibt die Größe von Bildern beim Speichern an
	 */
	public int imageSize;

	/**
	 * Pfad zum Speichern von Bildern während der Animation (leer=Home-Verzeichnis)
	 */
	public String imagePathAnimation;

	/**
	 * Anzeige eines Rasters auf der Zeichenfläche
	 */
	public ModelSurface.Grid grid;

	/**
	 * Anzeige der IDs unter den Station im Edit-Modus
	 */
	public boolean showIDs;

	/**
	 * Stationbeschreibungen in den Tooltips anzeigen?
	 */
	public boolean showStationDescription;

	/**
	 * Schrift auf der Zeichenfläche glätten
	 */
	public boolean antialias;

	/**
	 * Soll eine leichte Abweichung von der vollkommenen Transparenz
	 * für den Hintergrund beim Exportieren von Modellen als Bilddateien
	 * verwendet werden (damit die Windows-Bildanzeige keinen schwarzen
	 * Hintergrund anzeigt)?
	 */
	public boolean useTransparencyExportFix;

	/**
	 * Anzeige von Statistikdaten in Tooltips?
	 */
	public boolean statisticInTooltips;

	/**
	 * Tooltips auf der Zeichenfläche auch dann anzeigen, wenn durch einen offenen Dialog blockiert ist.
	 */
	public boolean showBackgroundTooltips;

	/**
	 * Anzeige von Heatmap-Statistikdaten auf der Zeichenfläche
	 * @see ui.EditorPanelStatistics.HeatMapMode
	 */
	public EditorPanelStatistics.HeatMapMode statisticHeatMap;

	/**
	 * Maximale Größe der Heatmap-Wolken (bezogen auf einen Zoomfaktor von 100%)
	 */
	public int statisticHeatMapSize;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensität von 0%
	 */
	public double statisticHeatMapIntensityMin;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensität von 100%
	 */
	public double statisticHeatMapIntensityMax;

	/**
	 * Heatmap-Farbe für niedrige Intensität
	 */
	public Color statisticHeatMapColorLow;

	/**
	 * Heatmap-Farbe für hohe Intensität
	 */
	public Color statisticHeatMapColorHigh;

	/**
	 * Gibt an, ob die Bilder bei HTML-Reports inline oder als separate Dateien ausgegeben werden sollen.
	 */
	public boolean imagesInline;

	/**
	 * Gibt an, welche Einträge im Reportgenerator zuletzt aktiviert waren
	 */
	public String reportSettings;

	/**
	 * Formatierungseinstellungen für den pdf- und docx-Export im Reportgenerator
	 * @see ReportStyle
	 */
	public ReportStyle reportStyle;

	/**
	 * Sollen die zuletzt verwendeten Dateien erfasst werden?
	 * @see #lastFiles
	 */
	public boolean useLastFiles;

	/**
	 * Liste der zuletzt verwendeten Dateien
	 * @see #useLastFiles
	 */
	public String[] lastFiles;

	/**
	 * Soll geprüft werden, ob eine veraltete Java-Version verwendet wird?
	 */
	public boolean testJavaVersion;

	/**
	 * Soll sich das Programm (wenn möglich) automatisch aktualisieren?
	 * @see SetupData#autoUpdate
	 */
	public enum AutoUpdate {
		/** Nicht nach Updates suchen */
		OFF("Off"),
		/** Nach Updates suchen, aber nicht automatisch installieren */
		SEARCH("Search"),
		/** Nach Updates suchen und wenn möglich automatisch installieren */
		INSTALL("Install");

		/** Name des Auto-Updates-Modus zum Speichern in der Konfiguration */
		public final String name;

		/**
		 * Konstruktor des Enum
		 * @param name	Name des Auto-Updates-Modus zum Speichern in der Konfiguration
		 */
		AutoUpdate(final String name) {
			this.name=name;
		}

		/**
		 * Liefert zu einem Namen das passende Auto-Update-Enum.
		 * @param name	Name (aus der Konfiguration geladen)
		 * @return	Passendes Enum (oder Fallback-Wert)
		 * @see #name
		 */
		public static AutoUpdate getByName(final String name) {
			for (AutoUpdate autoUpdate: values()) if (autoUpdate.name.equalsIgnoreCase(name)) return autoUpdate;
			return INSTALL;
		}
	}

	/**
	 * Programminterner Updater verfügbar?
	 * @see UpdateSystem#UPDATER_BLOCK_FILE
	 */
	public final boolean updaterAvailable;

	/**
	 * Soll sich das Programm (wenn möglich) automatisch aktualisieren?
	 */
	public AutoUpdate autoUpdate;

	/**
	 * Alle CPU-Kerne für Simulation nutzen?
	 */
	public boolean useMultiCoreSimulation;

	/**
	 * Sollen wiederholte Simulationsläufe ggf. aufgeteilt werden, um alle CPU-Kerne auszulasten?
	 */
	public boolean useMultiCoreSimulationOnRepeatedSimulations;

	/**
	 * Ist {@link #useMultiCoreSimulation} aktiv, so gibt dieser Wert die maximale
	 * Anzahl an Simulationsthreads an.
	 */
	public int useMultiCoreSimulationMaxCount;

	/**
	 * Mehrere CPU-Kerne für Animation nutzen?
	 */
	public boolean useMultiCoreAnimation;

	/**
	 * Animation künstlich verlangsamen, um Veränderungen analoger Werte besser abzubilden
	 */
	public boolean useSlowModeAnimation;

	/**
	 * Datenmodelle für einzelne Threads trennen (benötigt mehr Speicher) um auf NUMA-Systemen mehr Performance zu erreichen.
	 */
	public boolean useNUMAMode;

	/**
	 * Kundenankünfte wenn möglich dynamisch zwischen den Threads aufteilen
	 */
	public boolean useDynamicThreadBalance;

	/**
	 * Laufzeitdaten der Stationen während der Animation anzeigen?
	 */
	public boolean showStationRunTimeData;

	/**
	 * Logging-Daten im Animations-Einzelschritt-Modus anzeigen
	 */
	public boolean showSingleStepLogData;

	/**
	 * Ressourcen und Transporter in der Animation darstellen
	 */
	public boolean animateResources;

	/**
	 * Animation bei Pause-Skriptanweisung unterbrechen
	 */
	public boolean respectPauseCommand;

	/**
	 * Dateiname der letzten Logdatei
	 */
	public String lastLogFile;

	/**
	 * Eine oder zwei Zeilen pro Simulationslogfile-Eintrag ?
	 */
	public boolean singleLineEventLog;

	/**
	 * Einträge in der Logdatei gruppieren?
	 */
	public boolean logGrouped;

	/**
	 * Farben in der Logdatei?
	 */
	public boolean logColors;

	/**
	 * Zeitangaben in HH:MM:SS,s in der Logdatei ausgeben
	 */
	public boolean logFormatedTime;

	/**
	 * IDs der jeweiligen Stationen in separater Spalte in Logdatei ausgeben
	 */
	public boolean logPrintIDs;

	/**
	 * Maximale Anzahl an auszugebenden Logging-Einträgen (es werden nur die letzten ausgegeben). Wird nur berücksichtigt, wenn &ge;1.
	 */
	public int logMaxRecords;

	/**
	 * Aufzeichnungsmodus: in Datei oder per DDE zu Excel
	 */
	public LogMode logMode;

	/**
	 * Excel-Arbeitsmappe für DDE-Aufzeichnung
	 */
	public String logDDEworkbook;

	/**
	 * Excel-Tabelle für DDE-Aufzeichnung
	 */
	public String logDDEsheet;

	/**
	 * IDs der Stationen deren Daten aufgezeichnet werden sollen (leer bedeutet alls IDs)
	 */
	public String logStationIDs;

	/**
	 * Log-Erfassung von Kundenankünften an Stationen
	 */
	public boolean logTypeArrival;

	/**
	 * Log-Erfassung von Kundenabgängen von Stationen
	 */
	public boolean logTypeLeave;

	/**
	 * Log-Erfassung allgemeinen Stationsinformationen
	 */
	public boolean logTypeInfoStation;

	/**
	 * Log-Erfassung allgemeinen Systeminformationen
	 */
	public boolean logTypeInfoSystem;

	/**
	 * Hohe Priorität (=normal priority, <code>true</code>) statt niedriger Priorität (<code>false</code>) verwenden
	 */
	public boolean highPriority;

	/**
	 * Vorlageleiste beim Start anzeigen (wenn im Modus START_TEMPLATE_LASTSTATE)
	 */
	public boolean showTemplates;

	/**
	 * Verzögerung bei der Animation (0..1000)
	 */
	public int animationDelay;

	/**
	 * Gibt an, wie mit der Einschwingphase in der Animation umgegangen werden soll.
	 */
	public AnimationMode animationWarmUpMode;

	/**
	 * Filtertext (Java)
	 */
	public String filterJavascript;

	/**
	 * Filtertext (Javascript)
	 */
	public String filterJava;

	/**
	 * Filterlisteneinträge
	 */
	public String filterList;

	/**
	 * Welcher Filter-Art-Tab war zuletzt aktiv?
	 */
	public int lastFilterMode;

	/**
	 *  Skript für Script-Runner
	 */
	public String scriptScriptRunner;

	/**
	 *  Skript für Rechner
	 */
	public String scriptCalculator;

	/**
	 * Schriftgröße in Skript-Eingabefeldern
	 */
	public int scriptFontSize;

	/**
	 * Optional eine abweichende programmweite Schriftart
	 */
	public String fontName;

	/**
	 * Zuletzt im Kommandozeilen-Befehle-Dialog verwendete Parameter
	 */
	public String commandLineDialogParameters;

	/**
	 * Sollen bei der Konsolenausgabe von Kommandozeilenbefehlen ANSI-Escape-Sequenzen verwendet werden?
	 */
	public boolean commandLineUseANSI;

	/**
	 * Modell im Hintergrund prüfen und in Statuszeile anzeigen, ob Fehler vorliegen.
	 * @see SetupData.BackgroundProcessingMode#BACKGROUND_NOTHING
	 * @see SetupData.BackgroundProcessingMode#BACKGROUND_CHECK_ONLY
	 * @see SetupData.BackgroundProcessingMode#BACKGROUND_SIMULATION
	 * @see SetupData.BackgroundProcessingMode#BACKGROUND_SIMULATION_ALWAYS
	 */
	public BackgroundProcessingMode backgroundSimulation;

	/**
	 * Neu eingefügte Element wenn möglich automatisch verbinden.
	 */
	public ModelSurfacePanel.ConnectMode autoConnect;

	/**
	 * Beim Einfügen einer Kopie einer Stationen Namen ändern
	 * @author Alexander Herzog
	 * @see SetupData#renameOnCopy
	 */
	public enum RenameOnCopyMode {
		/** Nicht umbenennen */
		OFF,
		/** Umbenennen, wenn Name auf Zahl endet */
		SMART,
		/** Umbenennen, wenn Name nicht leer ist */
		ALWAYS
	}

	/**
	 * Beim Einfügen einer Kopie einer Stationen Namen ändern
	 * @see SetupData.RenameOnCopyMode
	 */
	public RenameOnCopyMode renameOnCopy;

	/**
	 * Fügt bei der Aufzeichnung in das Video den jeweils aktuellen Simulationszeit-Wert ein
	 */
	public boolean paintTimeStamp;

	/**
	 * Skalierungsfaktor für Videoaufzeichnungen (0.01..1)
	 */
	public double animationFrameScale;

	/**
	 * Videoaufzeichnung von Animationen sofort starten?
	 */
	public boolean animationRecordStartImmediately;

	/**
	 * Startet die Animation im Einzelschrittmodus
	 */
	public boolean animationStartPaused;

	/**
	 * Schaltet die Animation unmittelbar vor ihrem Ende noch einmal auf Pause
	 */
	public boolean animationFinishPaused;

	/**
	 * Datum des vorausgegangenen letzten Programmstarts
	 */
	public String lastStart;

	/**
	 * Vorlagengruppen, die in der Vorlagenliste sichtbar sein sollen
	 */
	public String visibleTemplateGroups;

	/**
	 * Vorlagengruppen, die in der Vorlagenliste ausgeklappt sein sollen
	 */
	public String openTemplateGroups;

	/**
	 * Namen der Vorlagen, die in der Favoriten-Kategorie angezeigt werden sollen
	 */
	public String favoriteTemplates="";

	/**
	 * Gibt an, ob stets nur eine Vorlagengruppe ausgeklappt sein darf.
	 */
	public boolean onlyOneOpenTemplatesGroup;

	/**
	 * Farbverläufe verwenden
	 */
	public boolean useGradients;

	/**
	 * Schatten an den Elementen anzeigen
	 */
	public boolean useShadows;

	/**
	 * Hohe Kontraste verwenden
	 */
	public boolean useHighContrasts;

	/**
	 * Nutzungsstatistik (simulierte Ankünfte)<br>
	 * Wird von {@link UsageStatistics} verwendet.
	 */
	public String usageStatistics;

	/**
	 * Nutzungsstatistik (Volllast-CPU-Sekunden)<br>
	 * Wird von {@link UsageStatistics} verwendet.
	 */
	public String usageCPUTime;

	/**
	 * Schaltfläche "Feedback" in der Symbolleiste anzeigen
	 */
	public boolean showFeedbackButton;

	/**
	 * Schnellzugriff-Eingabefeld in der Symbolleiste anzeigen
	 */
	public boolean showQuickAccess;

	/**
	 * Elementenliste-Filter-Eingabefeld anzeigen
	 */
	public boolean showQuickFilter;

	/**
	 * Soll das Ein- und Ausklappen von Elementenvorlagenleiste
	 * und Navigator über eine Slide-Animation erfolgen?
	 */
	public boolean useAnimations;

	/**
	 * Zugangsdaten für die Simulation auf einem Server.<br>
	 * Entweder <code>Host:Port</code> oder <code>Host:Port:Passwort</code>
	 */
	public String serverData;

	/**
	 * Gibt an, ob die Server-Zugangsdaten tatsächlich genutzt werden sollen
	 */
	public boolean serverUse;

	/**
	 * Simulationsserver beim Start des Programmes starten.
	 */
	public boolean simulationServerAutoStart;

	/**
	 * Beim letzten Aufruf des Simulationsservers über die GUI verwendeter Port
	 */
	public int simulationServerPort;

	/**
	 * Beim letzten Aufruf des Simulationsservers über die GUI verwendetes Passwort
	 */
	public String simulationServerPasswort;

	/**
	 * Beim letzten Aufruf des Simulationsservers über die GUI verwendete Thread-Anzahl-Limitierungseinstellung
	 */
	public boolean simulationServerLimitThreadCount;

	/**
	 * Fernsteuerungs-Server beim Start des Programmes starten.
	 */
	public boolean webServerAutoStart;

	/**
	 * Beim letzten Aufruf des Fernsteuerungs-Servers verwendeter Port
	 */
	public int webServerPort;

	/**
	 * Webserver beim Start des Programmes starten.
	 */
	public boolean calcWebServerAutoStart;

	/**
	 * Beim letzten Aufruf des Webservers verwendeter Port
	 */
	public int calcWebServerPort;

	/**
	 * Adresse des MQTT-Brokers
	 */
	public String mqttBroker;

	/**
	 * Falls die Verbindung zum Broker verschlüsselt aufgebaut wird:
	 * Soll das Zertifikat des Brokers verifiziert werden?
	 */
	public boolean mqttVerifyCertificates;

	/**
	 * Beim MQTT-Broker zu abonnierendes Thema
	 */
	public String mqttTopic;

	/**
	 * Thema über das Statusmeldungen zur Auslastung an den MQTT-Broker übermittelt werden
	 */
	public String mqttLoadTopic;

	/**
	 * MQTT-Klienten beim Start des Programmes starten.
	 */
	public boolean mqttServerAutoStart;

	/**
	 * Benutzername, den Web-Rechen- und Web-Fernsteuerungsserver für die Authentifizierung verwenden sollen
	 */
	public String serverAuthName;

	/**
	 * Passwort, das Web-Rechen- und Web-Fernsteuerungsserver für die Authentifizierung verwenden sollen
	 */
	public String serverAuthPassword;

	/**
	 * TLS-Key-Store-Datei für den Web-Rechen- und den Web-Fernsteuerungsserver
	 */
	public String serverTLSKeyStoreFile;

	/**
	 * Passwort für TLS-Key-Store-Datei für den Web-Rechen- und den Web-Fernsteuerungsserver
	 */
	public String serverTLSKeyStorePassword;

	/**
	 * DDE-Server beim Start des Programmes starten.
	 */
	public boolean ddeServerAutoStart;

	/**
	 * Nutzerdefierter Bezeichner für die Zeile in Excel beim DDE-Zugriff.
	 * Wird hier ein leerer String angegeben, so wird die Sprachvorgabe verwendet
	 * (im Englischen "R" und im Deutschen "Z").
	 */
	public String customExcelRowName;

	/**
	 * Nutzerdefierter Bezeichner für die Spalte in Excel beim DDE-Zugriff.
	 * Wird hier ein leerer String angegeben, so wird die Sprachvorgabe verwendet
	 * (im Englischen "C" und im Deutschen "S").
	 */
	public String customExcelColName;

	/**
	 * Socket-Server beim Start des Programmes starten.
	 */
	public boolean socketServerAutoStart;

	/**
	 * Beim letzten Aufruf des Socket-Servers verwendeter Port
	 */
	public int socketServerPort;

	/**
	 * Pfad zur JDK-Umgebung (die den Java-Kompiler javac enthält)
	 */
	public String javaJDKPath;

	/**
	 * Name der bevorzugten JS-Engine
	 */
	public String jsEngine;

	/**
	 * Simulation bei einem Scripting-Fehler abbrechen
	 */
	public boolean cancelSimulationOnScriptError;

	/**
	 * Maximale Laufzeit für Javascript-Code zur Filterung von
	 * Simulationsergebnissen und zur Skriptausführung während
	 * einer Simulation.
	 */
	public int maxJSRunTimeSeconds;

	/**
	 * Wie soll beim Laden von Modellen mit potentiell sicherheitskritischen Elementen verfahren werden?
	 */
	public ModelSecurity modelSecurity;

	/**
	 * Soll {@link #modelSecurity} für alle Dateien (<code>false</code>) oder nur für Dateien,
	 * die aus dem Netz geladen wurden (<code>true</code>) gelten?
	 */
	public boolean modelSecurityOnlyOnInternetFiles;

	/**
	 * Modelle, die Skripte enthalten, signieren
	 */
	public boolean signModels;

	/**
	 * Erlaubt das Ausführen externer Programme durch Skripte
	 */
	public boolean modelSecurityAllowExecuteExternal;

	/**
	 * Verhalten beim Anklicken von Links
	 */
	public boolean allowToOpenLinks;

	/**
	 * Benachrichtigung beim Ende von Simulation, Parameterreihe oder Optimierung in System-Tray anzeigen?
	 * @see Notifier
	 */
	public NotifyMode notifyMode;

	/**
	 * Benachrichtigung beim Ende von Simulation, Parameterreihe oder Optimierung über MQTT senden?
	 * @see #notifyMQTTTopic
	 */
	public boolean notifyMQTT;

	/**
	 * MQTT-Topic über das Benachrichtigung beim Ende von Simulation, Parameterreihe oder Optimierung gesendet werden sollen
	 * @see #notifyMQTT
	 */
	public String notifyMQTTTopic;

	/**
	 * Proxy-Server verwenden ja/nein (unabhängig von den anderen Proxy-Einstellungen kann die Server-Verwendung deaktiviert werden)
	 */
	public boolean useProxy;

	/**
	 * Name des Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public String proxyHost;

	/**
	 * Port des Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public int proxyPort;

	/**
	 * Nutzername für Anmeldung am Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public String proxyUser;

	/**
	 * Passwort für Anmeldung am Proxy-Server (Server wird nur verwendet, wenn {@link SetupData#useProxy} wahr ist)
	 */
	public String proxyPassword;

	/**
	 * Aktuelles Modell beim Beenden automatisch speichern?
	 */
	public boolean autoRestore;

	/**
	 * Quantile in den Statistik-Text-Seiten anzeigen?
	 */
	public boolean showQuantils;

	/**
	 * Erlang-C-Vergleichssseite in der Statistikansicht anzeigen
	 */
	public boolean showErlangC;

	/**
	 * Ergebnishinweiseseite in der Statistikansicht anzeigen
	 */
	public boolean showRemarks;

	/**
	 * Immer alle Kategorien in der Baumstruktur der Statistikansicht ausklappen?
	 */
	public boolean expandAllStatistics;

	/**
	 * Im Dateiauswahldialog zum Speichern per Vorgabe zu wählendes Format für Modelldateien
	 */
	public XMLTools.DefaultSaveFormat defaultSaveFormatModels;

	/**
	 * Im Dateiauswahldialog zum Speichern per Vorgabe zu wählendes Format für Statistikdateien
	 */
	public XMLTools.DefaultSaveFormat defaultSaveFormatStatistics;

	/**
	 * Im Dateiauswahldialog zum Speichern per Vorgabe zu wählendes Format für Parameterreihen-Dateien
	 */
	public XMLTools.DefaultSaveFormat defaultSaveFormatParameterSeries;

	/**
	 * Im Dateiauswahldialog zum Speichern per Vorgabe zu wählendes Format für Optimierereinstellungen-Dateien
	 */
	public XMLTools.DefaultSaveFormat defaultSaveFormatOptimizerSetups;

	/**
	 * Sicherheitskopien von Modelldateien anlegen
	 */
	public boolean useBackupFiles;

	/**
	 * Vorgabe-Nutzername für neue Modelle und für Statistikdateien
	 */
	public String defaultUserName;

	/**
	 * Vorgabe-E-Mail-Adresse für neue Modelle
	 */
	public String defaultUserEMail;

	/**
	 * Wie viele Nachkommastellen sollen in Parameterreihen-Tabellen angezeigt werden? (mögliche Werte: 1, 3 oder 9 für Maximalanzahl)
	 */
	public int parameterSeriesTableDigits;

	/**
	 * Soll {@link #parameterSeriesTableDigits} auch beim Exportieren verwendet werden (<code>true</code>) oder soll dann immer
	 * alle Nachkommastellen ausgegeben werden (<code>false</code>)?
	 */
	public boolean parameterSeriesTableDigitsUseOnExport;

	/**
	 * Sollen die Parameterreihendaten hochskaliert werden?
	 */
	public int parameterSeriesUpscale;

	/**
	 * Zeitangaben beim Export von Parameterreihen-Tabellen immer in einfache Zahlen umwandeln?
	 */
	public boolean parameterSeriesForceTimeAsNumberOnExport;

	/**
	 * Prozentwerte beim Export von Parameterreihen-Tabellen immer in einfache Zahlen umwandeln?
	 */
	public boolean parameterSeriesForcePercentAsNumberOnExport;

	/**
	 * Ausgewählte Datenquellen für den Schnellzugriff
	 */
	public String quickAccessFilter;

	/**
	 * Sortierungsmöglichkeiten für die Elementenliste und den Navigator
	 * @see SetupData#elementListSort
	 */
	public enum ElementListSort {
		/**
		 * Sortierung nach IDs
		 */
		SORT_BY_IDS("IDs",true),

		/**
		 * Sortierung nach Namen
		 */
		SORT_BY_NAMES("Names");

		/**
		 * XML-Bezeichner für die Sortierungsfolge
		 * (zum Speichern in der Konfiguration)
		 */
		public final String xmlName;

		/**
		 * Ist dies die Standard-Sortierfolge
		 * (die nicht gespeichert werden muss)?
		 */
		public boolean isDefault;

		/**
		 * Konstruktor des Enum
		 * @param xmlName	XML-Bezeichner für die Sortierungsfolge
		 * @param isDefault	Ist dies die Standard-Sortierfolge?
		 */
		ElementListSort(final String xmlName, final boolean isDefault) {
			this.xmlName=xmlName;
			this.isDefault=isDefault;
		}

		/**
		 * Konstruktor des Enum
		 * @param xmlName	XML-Bezeichner für die Sortierungsfolge
		 */
		ElementListSort(final String xmlName) {
			this(xmlName,false);
		}

		/**
		 * Liefert die Standard-Sortierfolge.
		 * @return	Standard-Sortierfolge
		 */
		public static ElementListSort getDefault() {
			for (ElementListSort record: values()) if (record.isDefault) return record;
			return values()[0];
		}

		/**
		 * Bestimmt die Sortierfolge basierend auf einem XML-Bezeichner
		 * @param xmlName	XML-Bezeichner für den die zugehörige Sortierfolge bestimmt werden soll
		 * @return	Sortierfolge (im Fehlerfall die Standard-Sortierfolge)
		 */
		public static ElementListSort getFromXMLName(final String xmlName) {
			for (ElementListSort record: values()) if (record.xmlName.equalsIgnoreCase(xmlName)) return record;
			return getDefault();
		}
	}

	/**
	 * Sortierreihenfolge für die Elementenliste und den Navigator
	 */
	public ElementListSort elementListSort;

	/**
	 * Option auf Statistik-Text-Viewer-Seiten: "Öffnen mit Word"
	 */
	public boolean openWord;

	/**
	 * Option auf Statistik-Text-Viewer-Seiten: "Öffnen mit OpenOffice/LibreOffice"
	 */
	public boolean openODT;

	/**
	 * Option auf Statistik-Tabellen-Viewer-Seiten: "Öffnen mit Excel"
	 */
	public boolean openExcel;

	/**
	 * Option auf Statistik-Tabellen-Viewer-Seiten: "Öffnen mit OpenOffice/LibreOffice"
	 */
	public boolean openODS;

	/**
	 * Option auf Statistik-Viewer-Seiten: "Öffnen als pdf"
	 */
	public boolean openPDF;

	/**
	 * Lineale um die Zeichenfläche anzeigen.
	 */
	public boolean showRulers;

	/**
	 * Zeigt den aktuellen Speicherbedarf in der Menüzeile an.
	 */
	public boolean showMemoryUsage;

	/**
	 * Anzahl an in der Statistikansicht anzuzeigende Nachkommastellen für normale Zahlen
	 */
	public int statisticsNumberDigits;

	/**
	 * Anzahl an in der Statistikansicht anzuzeigende Nachkommastellen für Prozentangaben
	 */
	public int statisticsPercentDigits;

	/**
	 * Soll in Statistiktexten vor gerundeten Null-Werten, die jedoch nicht exakt Null sind, ein Ungefähr-Zeichen angezeigt werden?
	 */
	public boolean showApproxSignOnValuesNearZero;

	/**
	 * Zu welchen Konfidenzniveaus sollen auf Basis der Batch-Means-Methode Konfidenzintervalle ausgegeben werden?<br>
	 * Wenn leer, dann werden 90%, 95% und 99% verwendet.
	 */
	public String batchMeansConfidenceLevels;

	/**
	 * Zu welchen Levels sollen Quantile ausgegeben werden?<br>
	 * Wenn leer, dann werden 0.10, 0.25, 0.5, 0.75 und 0.9.
	 * @see StatisticsDataPerformanceIndicator#storeQuantilValues
	 */
	public String quantilLevels;

	/**
	 * Schriftarten- und Farbeneinstellungen für die Statistikdiagramme
	 */
	public ChartSetup chartSetup;

	/**
	 * Filter für die Abschnitte auf der Statistik-Übersichtsseite
	 */
	public String statisticOverviewFilter;

	/**
	 * Liste der Git-Verbindungs-Konfigurationen
	 * @see GitSetup
	 */
	public List<GitSetup> gitSetups;

	/**
	 * Zusätzlich zu importierende Klassen für dynamisch generierte Klassen
	 * @see SimDynamicSetup#getImports(String)
	 */
	public List<String> dynamicImportClasses;

	/**
	 * E-Book-pdf des Lehrbuchs zum Simulator
	 */
	public String eBook;

	/**
	 * Suchen und Ersetzen: Groß- und Kleinschreibung beachten
	 */
	public boolean searchCaseSensitive;

	/**
	 * Suchen und Ersetzen: Auch nach Stations-IDs suchen
	 */
	public boolean searchStationIDs;

	/**
	 * Suchen und Ersetzen: Nur gesamte Begriffe vergleichen
	 */
	public boolean searchFullMatchOnly;

	/**
	 * Suchen und Ersetzen: Ist regulärer Ausdruck
	 */
	public boolean searchRegularExpression;

	/**
	 * Suchen in Skript-Eingabefeld: Vorwärts suchen
	 */
	public boolean searchForward;

	/**
	 * Speichert den zuletzt im Stapelverarbeitung-Panel hinterlegten Verzeichnisnamen
	 * @see BatchPanel
	 */
	public String batchFolder;

	/**
	 * Bearbeitungsmodus für das Stapelverarbeitung-Panel
	 * @see SetupData#batchMode
	 * @see BatchPanel
	 */
	public enum BatchMode {
		/** Modelle im Verzeichnis simulieren */
		SIMULATION,
		/** Statistikdaten im Verzeichnis filtern */
		FILTER
	}

	/**
	 * Speichert den zuletzt im Stapelverarbeitung-Panel hinterlegten Bearbeitungsmodus
	 * @see BatchPanel
	 */
	public BatchMode batchMode;

	/**
	 * Speichert das zuletzt im Stapelverarbeitung-Panel hinterlegte Filterskript
	 * @see BatchPanel
	 */
	public String batchFilterScript;

	/**
	 * Speichert den zuletzt im Stapelverarbeitung-Panel hinterlegten Ausgabedateiname (für die Filterergebnisse)
	 * @see BatchPanel
	 */
	public String batchOutputFile;

	/**
	 * Liste und Reihenfolge der in der Verteilungsliste hervorgehoben darzustellenden Verteilungen
	 */
	public String distributionListFilter;

	/**
	 * Bookmarks im Statistikbaum
	 */
	public List<String> statisticTreeBookmarks;

	/**
	 * Konfiguration der Kacheln für die Dashboard-Statistik-Ansicht
	 */
	public List<String> dashboardSetup;

	/**
	 * Optionaler Gradient für die Hintergrundfarbe der Elementenvorlagenleiste
	 */
	public boolean gradientTempaltes;

	/**
	 * Optionaler Gradient für die Hintergrundfarbe der Navigatorleiste
	 */
	public boolean gradientNavigator;

	/**
	 * Wie soll nach dem Abbruch einer Animation mit den unvollständigen Statistikdaten umgegangen werden?
	 */
	public CanceledSimulationStatistics canceledAnimationStatistics;

	/**
	 * Wie soll nach dem Abbruch einer Simulation mit den unvollständigen Statistikdaten umgegangen werden?
	 */
	public CanceledSimulationStatistics canceledSimulationStatistics;

	/**
	 * Aktive Sprachen für die Rechtschreibprüfung (durch ";" getrennte Liste der Sprachnamen)
	 */
	public String spellCheckingLanguages;

	/**
	 * Bei der Rechtschreibprüfung zu berücksichtigende Textfelder
	 */
	public Set<ScriptEditorAreaBuilder.TextAreaMode> spellCheckMode;

	/**
	 * Mausposition beim Zoomen per Mausrad festhalten
	 */
	public boolean mouseWheelZoomFixMousePosition;

	/**
	 * Nutzerdefinierte Rechenfunktionen
	 * @see ExpressionCalcUserFunctionsManager
	 */
	public List<String> userDefinedCalculationFunctions;

	/**
	 * Nutzerdefinierte Javascript-basierende Rechenfunktionen
	 * @see ExpressionCalcUserFunctionsManager
	 */
	public List<String> userDefinedJSFunctions;

	/**
	 * Nutzerdefinierte Java-basierende Rechenfunktionen
	 * @see ExpressionCalcUserFunctionsManager
	 */
	public List<String> userDefinedJavaFunctions;

	/**
	 * Wird dieser Wert auf <code>false</code> gesetzt, so wird das Hunspell-System komplett deaktiviert (für den Fall, dass es Fehler verursacht).
	 */
	public boolean allowSpellCheck;

	/**
	 * Wird dieser Wert auf <code>false</code> gesetzt, so erfolgt keine Erfassung,
	 * welche Stationen jeweils wie häufig mit welchen Folgestationen verknüpft wurden
	 * (um dann passende Folgestationen anbieten zu können).
	 * @see EditModelProcessor
	 * @see NextStationHelper
	 */
	public boolean collectNextStationData;

	/**
	 * Soll während einer Simulation zusätzlich zu dem Fortschrittsbalken eine Animation angezeigt werden?
	 */
	public boolean simulationProgressAnimation;

	/**
	 * Sollen Scroll-Felder auf touch-artige Ereignisse reagieren?
	 */
	public boolean touchSupport;

	/**
	 * Letzter Fehler
	 * (Hier wird die Setup-Datei als Logdatei für solche Ereignisse verwendet.)
	 */
	public String lastError;

	/**
	 * Singleton-Instanz des Setup-Objektes
	 * @see #getSetup()
	 */
	private static volatile SetupData setup=null;

	/**
	 * Mutex zum das mehrfache parallele Initialisieren
	 * von {@link #setup} zu verhindern.
	 * @see #setup
	 * @see #getSetup(boolean)
	 */
	private static final Semaphore mutex=new Semaphore(1);

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse ist ein Singleton und kann nicht direkt instanziert werden.
	 * Es muss stattdessen {@link #getSetup()} verwendet werden.
	 * @param loadSetupFile	Zu ladende Setup-Datei
	 * @see #getSetup()
	 */
	private SetupData(final boolean loadSetupFile) {
		super();
		updaterAvailable=testUpdaterAvailable();
		if (loadSetupFile) {
			loadSetupFromFile();
			autoFirstInit();
		}
	}

	@Override
	protected void resetDataToDefaults() {
		language="";
		numberFormat=NumberFormat.BY_SYSTEM;
		startSizeMode=StartSizeMode.START_MODE_DEFAULT;
		lastSizeMode=Frame.NORMAL;
		lastPosition=new Point(0,0);
		lastSize=new Dimension(0,0);
		lastZoom=1.0;
		restoreSubEditWindowSize=true;
		startTemplateMode=StartTemplateMode.START_TEMPLATE_HIDDEN;
		startModel="";
		surfaceHelp=SurfaceHelp.START_ONLY;
		surfaceGlassInfos=true;
		scaleGUI=1;
		lookAndFeel=getDefaultLookAndFeel();
		lookAndFeelCombinedMenu=true;
		autoSaveMode=AutoSaveMode.AUTOSAVE_OFF;
		hintDialogs="";
		imageSize=2000;
		imagePathAnimation="";
		grid=ModelSurface.Grid.LINES;
		showIDs=false;
		showStationDescription=false;
		antialias=true;
		useTransparencyExportFix=true;
		statisticInTooltips=true;
		showBackgroundTooltips=false;
		statisticHeatMap=EditorPanelStatistics.HeatMapMode.OFF;
		statisticHeatMapSize=50;
		statisticHeatMapIntensityMin=HeatMapImage.DEFAULT_INTENSITY_MIN;
		statisticHeatMapIntensityMax=HeatMapImage.DEFAULT_INTENSITY_MAX;
		statisticHeatMapColorLow=HeatMapImage.DEFAULT_COLOR_LOW_INTENSITY;
		statisticHeatMapColorHigh=HeatMapImage.DEFAULT_COLOR_HIGH_INTENSITY;
		imagesInline=true;
		reportSettings="";
		reportStyle=new ReportStyle();
		useLastFiles=true;
		lastFiles=null;
		testJavaVersion=true;
		autoUpdate=AutoUpdate.INSTALL;
		useMultiCoreSimulation=true;
		useMultiCoreSimulationOnRepeatedSimulations=false;
		useMultiCoreSimulationMaxCount=1024;
		useMultiCoreAnimation=true;
		useSlowModeAnimation=true;
		useNUMAMode=true;
		useDynamicThreadBalance=true;
		showStationRunTimeData=true;
		showSingleStepLogData=true;
		animateResources=true;
		respectPauseCommand=true;
		lastLogFile="";
		singleLineEventLog=true;
		logGrouped=true;
		logColors=true;
		logFormatedTime=true;
		logPrintIDs=true;
		logMaxRecords=-1;
		logMode=LogMode.FILE;
		logDDEworkbook="";
		logDDEsheet="";
		logStationIDs="";
		logTypeArrival=true;
		logTypeLeave=true;
		logTypeInfoStation=true;
		logTypeInfoSystem=true;
		highPriority=false;
		showTemplates=false;
		animationDelay=400;
		animationWarmUpMode=AnimationMode.ANIMATION_WARMUP_SKIP;
		filterJavascript="";
		filterJava="";
		filterList="";
		lastFilterMode=0;
		scriptScriptRunner="";
		scriptCalculator="";
		scriptFontSize=ScriptEditorAreaBuilder.DEFAULT_FONT_SIZE;
		fontName="";
		commandLineDialogParameters="";
		commandLineUseANSI=true;
		backgroundSimulation=BackgroundProcessingMode.BACKGROUND_SIMULATION;
		autoConnect=ModelSurfacePanel.ConnectMode.OFF;
		renameOnCopy=RenameOnCopyMode.SMART;
		paintTimeStamp=true;
		animationFrameScale=1.0;
		animationRecordStartImmediately=true;
		animationStartPaused=false;
		animationFinishPaused=false;
		lastStart="";
		visibleTemplateGroups="";
		openTemplateGroups="";
		favoriteTemplates="";
		onlyOneOpenTemplatesGroup=false;
		useGradients=true;
		useShadows=true;
		useHighContrasts=false;
		usageStatistics="";
		usageCPUTime="";
		showFeedbackButton=true;
		showQuickAccess=true;
		showQuickFilter=true;
		useAnimations=true;
		serverData="localhost:8183";
		serverUse=false;
		simulationServerAutoStart=false;
		simulationServerPort=8183;
		simulationServerPasswort="";
		simulationServerLimitThreadCount=false;
		webServerAutoStart=false;
		webServerPort=81;
		calcWebServerAutoStart=false;
		calcWebServerPort=80;
		mqttBroker="tcp://localhost";
		mqttVerifyCertificates=false;
		mqttTopic=MainFrame.PROGRAM_NAME+"/task";
		mqttLoadTopic=MainFrame.PROGRAM_NAME+"/info";
		mqttServerAutoStart=false;
		serverAuthName="";
		serverAuthPassword="";
		serverTLSKeyStoreFile="";
		serverTLSKeyStorePassword="";
		ddeServerAutoStart=false;
		customExcelRowName="";
		customExcelColName="";
		socketServerAutoStart=false;
		socketServerPort=1000;
		javaJDKPath="";
		jsEngine="";
		cancelSimulationOnScriptError=true;
		maxJSRunTimeSeconds=2;
		modelSecurity=ModelSecurity.ASK;
		modelSecurityOnlyOnInternetFiles=true;
		signModels=true;
		modelSecurityAllowExecuteExternal=false;
		allowToOpenLinks=true;
		notifyMode=NotifyMode.LONGRUN;
		notifyMQTT=false;
		notifyMQTTTopic=MainFrame.PROGRAM_NAME+"/notify";
		useProxy=false;
		proxyHost="";
		proxyPort=8080;
		proxyUser="";
		proxyPassword="";
		autoRestore=false;
		showQuantils=true;
		showErlangC=true;
		showRemarks=true;
		expandAllStatistics=false;
		defaultSaveFormatModels=XMLTools.DefaultSaveFormat.XML;
		defaultSaveFormatStatistics=XMLTools.DefaultSaveFormat.XML;
		defaultSaveFormatParameterSeries=XMLTools.DefaultSaveFormat.ZIP_XML;
		defaultSaveFormatOptimizerSetups=XMLTools.DefaultSaveFormat.ZIP_XML;
		useBackupFiles=false;
		defaultUserName=getDisplayUserName();
		defaultUserEMail="";
		parameterSeriesTableDigits=1;
		parameterSeriesTableDigitsUseOnExport=false;
		parameterSeriesUpscale=0;
		parameterSeriesForceTimeAsNumberOnExport=true;
		parameterSeriesForcePercentAsNumberOnExport=true;
		quickAccessFilter="";
		elementListSort=ElementListSort.getDefault();
		openWord=true;
		openODT=false;
		openExcel=true;
		openODS=false;
		openPDF=false;
		showRulers=false;
		showMemoryUsage=false;
		statisticsNumberDigits=1;
		statisticsPercentDigits=1;
		showApproxSignOnValuesNearZero=true;
		batchMeansConfidenceLevels="";
		quantilLevels="";
		if (chartSetup==null) chartSetup=new ChartSetup();
		chartSetup.reset();
		statisticOverviewFilter="";
		if (gitSetups==null) gitSetups=new ArrayList<>();
		gitSetups.clear();
		if (dynamicImportClasses==null) dynamicImportClasses=new ArrayList<>();
		dynamicImportClasses.clear();
		eBook="";
		searchCaseSensitive=false;
		searchStationIDs=false;
		searchFullMatchOnly=false;
		searchRegularExpression=false;
		searchForward=true;
		batchFolder="";
		batchMode=BatchMode.SIMULATION;
		batchFilterScript="";
		batchOutputFile="";
		distributionListFilter="";
		if (statisticTreeBookmarks==null) statisticTreeBookmarks=new ArrayList<>();
		statisticTreeBookmarks.clear();
		if (dashboardSetup==null) dashboardSetup=new ArrayList<>();
		dashboardSetup.clear();
		gradientTempaltes=false;
		gradientNavigator=false;
		canceledAnimationStatistics=CanceledSimulationStatistics.OFF;
		canceledSimulationStatistics=CanceledSimulationStatistics.ASK;
		spellCheckingLanguages="de-DE;en-US";
		if (spellCheckMode==null) spellCheckMode=new HashSet<>();
		mouseWheelZoomFixMousePosition=true;
		if (userDefinedCalculationFunctions==null) userDefinedCalculationFunctions=new ArrayList<>();
		if (userDefinedJSFunctions==null) userDefinedJSFunctions=new ArrayList<>();
		if (userDefinedJavaFunctions==null) userDefinedJavaFunctions=new ArrayList<>();
		userDefinedCalculationFunctions.clear();
		userDefinedJSFunctions.clear();
		userDefinedJavaFunctions.clear();
		allowSpellCheck=true;
		collectNextStationData=true;
		simulationProgressAnimation=true;
		touchSupport=false;
		lastError=null;
	}

	/**
	 * Gibt an, ob Programmsprache usw. beim Programmstart automatisch initialisiert wurden
	 * (oder ob die Programmsprache aus dem Setup geladen wurde).
	 * @see #wasFirstInit()
	 * @see #clarFirstInitFlag()
	 * @see #autoFirstInit()
	 */
	private boolean autoInitActive=false;

	/**
	 * Gibt an, ob die Programmsprache beim Programmstart gemäß der Systemsprache automatisch
	 * eingestellt wurde (oder ob die Programmsprache aus dem Setup geladen wurde).
	 * @return	Gibt <code>true</code> zurück, wenn die Programmsprache automatisch eingestellt wurde
	 */
	public boolean wasFirstInit() {
		return autoInitActive;
	}

	/**
	 * Setzt den Status "Sprache wurde automatisch gesetzt" zurück.
	 */
	public void clarFirstInitFlag() {
		autoInitActive=false;
	}

	/**
	 * Liefert das standardmäßig zu verwendende Look&amp;Feel.
	 * @return	Standardmäßig zu verwendendes Look&amp;Feel
	 */
	private static String getDefaultLookAndFeel() {
		return new FlatLightLaf().getName();
	}

	/**
	 * Prüft, ob der programminterne Updater
	 * überhaupt verwendet werden soll.
	 * @return	Programminterner Updater verfügbar
	 * @see UpdateSystem#UPDATER_BLOCK_FILE
	 * @see #updaterAvailable
	 */
	private boolean testUpdaterAvailable() {
		final File blockUpdaterFile=new File(getSetupFolder(),UpdateSystem.UPDATER_BLOCK_FILE);
		return !blockUpdaterFile.isFile();
	}

	/**
	 * Stellt die Sprache usw., wenn nötig, initial automatisch ein.
	 */
	private void autoFirstInit() {
		/* Sprach-Feld ist immer mit Wert belegt. Ist es leer, haben wir ein komplett neues Setup. */
		if (!language.isEmpty()) return;

		/* Sprache einstellen */
		final String userLanguage=System.getProperty("user.language");
		if (Language.isSupportedLanguage(userLanguage)) language=userLanguage.toLowerCase(); else language="en";

		/* Thema einstellen */
		lookAndFeel=getDefaultLookAndFeel();

		/* Alle Elemente für Rechtschreibprüfung auswählen */
		spellCheckMode.addAll(Arrays.asList(ScriptEditorAreaBuilder.TextAreaMode.values()));

		autoInitActive=true;
		saveSetup();
	}

	/**
	 * Liefert das Setup-Singleton-Objekt zurück.<br>
	 * Der Aufruf wird über ein Mutex-Objekt abgesichert, ist also thread-safe.
	 * @return	Setup-Objekt
	 */
	public static SetupData getSetup() {
		return getSetup(true);
	}

	/**
	 * Liefert das Setup-Singleton-Objekt zurück
	 * @param lock	Gibt an, ob das evtl. notwendige Erstellen des Setup-Objektes über ein Mutex-Objekt vor Parallelaufrufen geschützt werden soll
	 * @return	Setup-Objekt
	 */
	public static SetupData getSetup(final boolean lock) {
		if (!lock) {
			if (setup==null) setup=new SetupData(true);
			return setup;
		}

		mutex.acquireUninterruptibly();
		try {
			if (setup==null) setup=new SetupData(true);
			return setup;
		} finally {
			mutex.release();
		}
	}

	/**
	 * Setzt das Setup auf die Defaultwerte zurück
	 */
	public static void resetSetup() {
		setup=new SetupData(false);
		setup.saveSetup();
	}

	/**
	 * Liefert den Pfadnamen des Verzeichnisses in dem sich die jar-Programmdatei befindet.
	 * @return	Pfad der Programmdatei
	 */
	public static File getProgramFolder() {
		try {
			final File source=new File(SetupData.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (source.toString().toLowerCase().endsWith(".jar")) return new File(source.getParent());
		} catch (URISyntaxException e1) {}
		return new File(System.getProperty("user.dir"));
	}

	/**
	 * Name für den Ordner unterhalb von %APPDATA%, der für Programmeinstellungen verwendet
	 * werden soll, wenn das Programm von innerhalb des "Programme"-Verzeichnisses ausgeführt wird.
	 */
	private static final String USER_CONFIGURATION_FOLDER_NAME="Warteschlangensimulator";

	/**
	 * Liefert den Pfadnamen des Verzeichnisses in dem die Einstellungsdatei abgelegt werden soll.
	 * @see #getSetupFolder()
	 */
	private static File setupFolder;

	/**
	 * Liefert den Pfadnamen des Verzeichnisses in dem die Einstellungsdatei abgelegt werden soll.
	 * @return	Pfad der Einstellungendatei
	 */
	public static synchronized File getSetupFolder() {
		if (setupFolder==null) setupFolder=getSetupFolderInt();
		return setupFolder;
	}

	/**
	 * Prüft, ob das Programmverzeichnis ein Unterverzeichnis des Home-Verzeichnisses ist.
	 * @return	Liefer <code>true</code>, wenn das Programmverzeichnis ein Unterverzeichnis des Home-Verzeichnisses ist
	 */
	private static boolean isProgramInHomeFolder() {
		final File programFolder=getProgramFolder();
		final String homeFolder=System.getProperty("user.home");
		if (homeFolder==null) return true;
		final String s1=homeFolder.toLowerCase();
		final String s2=programFolder.toString().toLowerCase();
		if (s1.equals(s2.substring(0,Math.min(s1.length(),s2.length())))) return true;
		return false;
	}

	/**
	 * Wird intern von {@link #getSetupFolder()} zur Bestimmung des Verzeichnisses
	 * aufgerufen, wenn in {@link #setupFolder} noch kein Wert hinterlegt ist.
	 * @return	Pfad der Einstellungendatei
	 */
	private static File getSetupFolderInt() {
		final File programFolder=getProgramFolder();

		/* Programmverzeichnis ist Unterordner des home-Verzeichnisses */
		if (isProgramInHomeFolder()) return programFolder;

		/* Betriebssystem ermitteln */
		final String osName=System.getProperty("os.name");
		if (osName==null) return programFolder;

		/* Alternativen Speicherort */

		/* Windows */
		if (osName.toLowerCase().contains("windows")) {
			final String appData=System.getenv("APPDATA");
			if (appData==null) return programFolder;
			final File appDataFolder=new File(appData);
			if (!appDataFolder.isDirectory()) return programFolder;
			final File folder=new File(appDataFolder,USER_CONFIGURATION_FOLDER_NAME);
			if (!folder.isDirectory()) {
				if (!folder.mkdir()) return programFolder;
			}
			if (!folder.isDirectory()) return programFolder;
			return folder;
		}

		/* Linux */
		if (osName.toLowerCase().contains("linux")) {
			final String home=System.getProperty("user.home");
			if (home==null) return programFolder;
			final File homeFolder=new File(home);
			if (!homeFolder.isDirectory()) return programFolder;
			final File folder=new File(homeFolder,"."+USER_CONFIGURATION_FOLDER_NAME);
			if (!folder.isDirectory()) {
				if (!folder.mkdir()) return programFolder;
			}
			if (!folder.isDirectory()) return programFolder;
			return folder;
		}

		return programFolder;
	}

	/**
	 * Dateiname der Setup-Datei
	 */
	public static final String SETUP_FILE_NAME="Simulator.cfg";

	@Override
	protected File getSetupFile() {
		return new File(getSetupFolder(),SETUP_FILE_NAME);
	}

	/**
	 * Typ der Installation
	 * @see SetupData#getOperationMode()
	 */
	public enum OperationMode {
		/** Installation im Programmverzeichnis */
		PROGRAM_FOLDER_MODE,
		/** Installation im Nutzerdatenverzeichnis */
		USER_FOLDER_MODE,
		/** Portable Installation */
		PORTABLE_MODE
	}

	/**
	 * Liefert den Typ der Installation zurück.
	 * @return	Typ der Installation
	 * @see OperationMode
	 */
	public static OperationMode getOperationMode() {
		if (!getSetupFolder().equals(getProgramFolder())) return OperationMode.PROGRAM_FOLDER_MODE;

		final String appData=System.getenv("APPDATA");
		if (appData!=null) {
			final String appDataLower=appData.toLowerCase();
			final String setupFolderLower=getSetupFolder().toString().toLowerCase();
			if (setupFolderLower.startsWith(appDataLower)) return OperationMode.USER_FOLDER_MODE;
		}

		return OperationMode.PORTABLE_MODE;
	}

	@Override
	protected void loadSetupFromXML(final Element root) {
		final List<String> files=new ArrayList<>();

		final NodeList l=root.getChildNodes();
		final int count=l.getLength();
		for (int i=0;i<count;i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			final String name=e.getNodeName().toLowerCase();

			if (name.equals("sprache") || name.equals("language")) {
				String t=e.getTextContent().toLowerCase();
				if (Language.isSupportedLanguage(t)) language=t.toLowerCase();
				continue;
			}

			if (name.equals("numberformat")) {
				numberFormat=NumberFormat.fromName(e.getTextContent());
			}

			if (name.equals("fullscreen")) {
				final Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) {
					if (j==1) startSizeMode=StartSizeMode.START_MODE_FULLSCREEN;
					if (j==2) startSizeMode=StartSizeMode.START_MODE_LASTSIZE;
				}
				continue;
			}

			if (name.equals("lastwindowsize")) {
				Integer j=NumberTools.getInteger(e.getAttribute("Mode"));
				if (j!=null && (j==Frame.NORMAL || j==Frame.MAXIMIZED_HORIZ || j==Frame.MAXIMIZED_VERT || j==Frame.MAXIMIZED_BOTH)) lastSizeMode=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("X"));
				if (j!=null) lastPosition.x=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("Y"));
				if (j!=null) lastPosition.y=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("Width"));
				if (j!=null) lastSize.width=j;
				j=NumberTools.getNotNegativeInteger(e.getAttribute("Height"));
				if (j!=null) lastSize.height=j;
				continue;
			}

			if (name.equals("lastzoom")) {
				Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
				if (D!=null && D<10) lastZoom=D.doubleValue();
				continue;
			}

			if (name.equals("restoresubeditwindowsize")) {
				restoreSubEditWindowSize=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("vorlagenanzeigen") || name.equals("showtemplates")) {
				Integer j=NumberTools.getInteger(e.getAttribute("Mode"));
				if (j!=null) switch (j) {
				case 0: startTemplateMode=StartTemplateMode.START_TEMPLATE_HIDDEN; break;
				case 1: startTemplateMode=StartTemplateMode.START_TEMPLATE_VISIBLE; break;
				case 2: startTemplateMode=StartTemplateMode.START_TEMPLATE_LASTSTATE; break;
				}
				showTemplates=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("startmodell") || name.equals("startmodel")) {
				startModel=e.getTextContent();
				continue;
			}

			if (name.equals("surfacehelp")) {
				final String text=e.getTextContent();
				if (text.equalsIgnoreCase("Never")) {surfaceHelp=SurfaceHelp.NEVER; continue;}
				if (text.equalsIgnoreCase("StartOnly")) {surfaceHelp=SurfaceHelp.START_ONLY; continue;}
				if (text.equalsIgnoreCase("Always")) {surfaceHelp=SurfaceHelp.ALWAYS; continue;}
				continue;
			}

			if (name.equals("surfaceglassinfos")) {
				surfaceGlassInfos=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("scale")) {
				final Double d=NumberTools.getExtProbability(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
				if (d!=null) scaleGUI=Math.min(2,Math.max(0.5,d));
				continue;
			}

			if (name.equals("lookandfeel")) {
				lookAndFeel=e.getTextContent();
				lookAndFeelCombinedMenu=loadBoolean(e.getAttribute("combinedMenu"),true);
				continue;
			}

			if (name.equals("autosave")) {
				final Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) {
					if (j==1) autoSaveMode=AutoSaveMode.AUTOSAVE_SIMULATION;
					if (j==2) autoSaveMode=AutoSaveMode.AUTOSAVE_ALWAYS;
				}
				continue;
			}

			if (name.equals("hintdialogs")) {
				hintDialogs=e.getTextContent();
				continue;
			}

			if (name.equals("images")) {
				final Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) imageSize=Math.min(5000,Math.max(50,j));
				imagesInline=loadBoolean(e.getAttribute("Inline"),true);
				continue;
			}

			if (name.equals("imagesanimation")) {
				imagePathAnimation=e.getTextContent();
				continue;
			}

			if (name.equals("raster")) {
				final String rasterID=e.getTextContent();
				if (rasterID.equals("1")) {
					grid=ModelSurface.Grid.LINES;
				} else {
					if (rasterID.equals("0")) {
						grid=ModelSurface.Grid.OFF;
					} else {
						for (ModelSurface.Grid r: ModelSurface.Grid.values()) if (r.id.equalsIgnoreCase(rasterID)) {
							grid=r;
							break;
						}
					}
				}
				continue;
			}

			if (name.equals("showids")) {
				showIDs=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("showstationdescription")) {
				showStationDescription=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("antialias")) {
				antialias=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("transparencyexportfix")) {
				useTransparencyExportFix=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("statisticintooltips")) {
				statisticInTooltips=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("backgroundtooltips")) {
				showBackgroundTooltips=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("statisticheatmap")) {
				statisticHeatMap=EditorPanelStatistics.HeatMapMode.fromName(e.getTextContent());
				final Long L=NumberTools.getPositiveLong(e.getAttribute("Size"));
				if (L!=null) statisticHeatMapSize=L.intValue();
				final Color colorLow=EditModel.loadColor(e.getAttribute("ColorLow"));
				if (colorLow!=null) statisticHeatMapColorLow=colorLow;
				final Color colorHigh=EditModel.loadColor(e.getAttribute("ColorHigh"));
				if (colorHigh!=null) statisticHeatMapColorHigh=colorHigh;
				Double D;
				D=NumberTools.getNotNegativeDouble(e.getAttribute("IntensityMin"));
				if (D!=null && D<=1.0) statisticHeatMapIntensityMin=D;
				D=NumberTools.getNotNegativeDouble(e.getAttribute("IntensityMax"));
				if (D!=null && D<=1.0) statisticHeatMapIntensityMax=D;
				continue;
			}

			if (name.equals("report")) {
				reportSettings=e.getTextContent();
				continue;
			}

			if (name.equals(ReportStyle.XML_NODE_NAME_LOWER)) {
				reportStyle.load(e);
				continue;
			}

			if (name.equals("uselastfiles")) {
				useLastFiles=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("lastfiles")) {
				files.add(e.getTextContent());
				continue;
			}

			if (name.equals("javaversioncheck")) {
				testJavaVersion=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("autoupdate")) {
				final String content=e.getTextContent();
				autoUpdate=AutoUpdate.INSTALL;
				if (content.equals("0")) {
					autoUpdate=AutoUpdate.OFF;
				} else {
					autoUpdate=AutoUpdate.getByName(content);
				}
				continue;
			}

			if (name.equals("allecpukerne") || name.equals("allcpucores") || name.equals("allcpucoressimulation")) {
				useMultiCoreSimulation=loadBoolean(e.getTextContent(),true);
				final String maxCountString=e.getAttribute("MaxCount");
				if (!maxCountString.isEmpty()) {
					final Long L=NumberTools.getPositiveLong(maxCountString);
					if (L!=null && L<=4096) useMultiCoreSimulationMaxCount=L.intValue();
				}
				useNUMAMode=loadBoolean(e.getAttribute("NUMA"),false);
				useDynamicThreadBalance=loadBoolean(e.getAttribute("Dynamic"),true);
				useMultiCoreSimulationOnRepeatedSimulations=loadBoolean(e.getAttribute("SplitRepeatedRuns"),false);
				continue;
			}

			if (name.equals("allcpucoresanimation")) {
				useMultiCoreAnimation=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("slowanimationforanalogvalues")) {
				useSlowModeAnimation=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("animationstationruntimedata")) {
				showStationRunTimeData=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("animationresourcestransporters")) {
				animateResources=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("animationrespectpausecommand")) {
				respectPauseCommand=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("animationsinglesteplogging")) {
				showSingleStepLogData=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("logging")) {
				singleLineEventLog=loadMultiBoolean(new String[]{e.getAttribute("CompactFormat"),e.getAttribute("KompaktesSimulationsLogFormat")},true);
				logGrouped=loadMultiBoolean(new String[]{e.getAttribute("GroupRecords"),e.getAttribute("LogeintraegeGruppieren")},true);
				logColors=loadMultiBoolean(new String[]{e.getAttribute("UseColors"),e.getAttribute("FarbigeLogdateien")},true);
				logFormatedTime=loadBoolean(e.getAttribute("FormatedTime"),true);
				logPrintIDs=loadBoolean(e.getAttribute("PrintIDs"),true);
				final Long L=NumberTools.getPositiveLong(e.getAttribute("MaxRecords"));
				if (L!=null) logMaxRecords=L.intValue();
				if (loadBoolean(e.getAttribute("DDE"),false)) logMode=LogMode.DDE; else logMode=LogMode.FILE;
				logDDEworkbook=e.getAttribute("DDEWorkbook");
				logDDEsheet=e.getAttribute("DDESheet");
				logStationIDs=e.getAttribute("IDs");
				logTypeArrival=loadBoolean(e.getAttribute("TypeArrival"),true);
				logTypeLeave=loadBoolean(e.getAttribute("TypeLeave"),true);
				logTypeInfoStation=loadBoolean(e.getAttribute("TypeInfoStation"),true);
				logTypeInfoSystem=loadBoolean(e.getAttribute("TypeInfoSystem"),true);
				lastLogFile=e.getTextContent();
				continue;
			}

			if (name.equals("hoheprioritaet") || name.equals("highpriority")) {
				highPriority=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("animationsverzoegerung") || name.equals("animationdelay")) {
				Integer j=NumberTools.getInteger(e.getTextContent());
				if (j!=null) animationDelay=Math.min(1000,Math.max(0,j));
				if (animationDelay<10 && animationDelay!=5) animationDelay*=100; /* Alten Modus umrechnen (Werte <10 gibt's nicht mehr, außer 0 und 5) */
				continue;
			}

			if (name.equals("warmupphaseatanimation")) {
				switch (e.getTextContent().toLowerCase()) {
				case "default": animationWarmUpMode=AnimationMode.ANIMATION_WARMUP_NORMAL; break;
				case "ask": animationWarmUpMode=AnimationMode.ANIMATION_WARMUP_ASK; break;
				case "skipifneeded": animationWarmUpMode=AnimationMode.ANIMATION_WARMUP_SKIP; break;
				case "fastforward": animationWarmUpMode=AnimationMode.ANIMATION_WARMUP_FAST; break;
				}
				continue;
			}

			if (name.equals("laststart")) {
				lastStart=e.getTextContent();
				continue;
			}

			if (name.equals("visibletemplategroups")) {
				visibleTemplateGroups=e.getTextContent();
				continue;
			}

			if (name.equals("opentemplategroups")) {
				openTemplateGroups=e.getTextContent();
				continue;
			}

			if (name.equals("favoritetemplates")) {
				favoriteTemplates=e.getTextContent();
				continue;
			}

			if (name.equals("onlyoneopentemplategroup")) {
				onlyOneOpenTemplatesGroup=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("filterjavascript") || name.equals("filter")) {
				filterJavascript=e.getTextContent();
				continue;
			}

			if (name.equals("filterjava")) {
				filterJava=e.getTextContent();
				continue;
			}

			if (name.equals("filterliste")) {
				filterList=e.getTextContent();
				continue;
			}

			if (name.equalsIgnoreCase("FilterMode")) {
				final Integer I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I!=null) lastFilterMode=I.intValue();
				continue;
			}

			if (name.equals("javascript")) {
				scriptScriptRunner=e.getTextContent();
				continue;
			}

			if (name.equals("calculatorscript")) {
				scriptCalculator=e.getTextContent();
				continue;
			}

			if (name.equals("scriptfontsize")) {
				final Integer I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I!=null && I.intValue()>=6 && I.intValue()<=30) scriptFontSize=I.intValue();
				continue;
			}

			if (name.equals("fontname")) {
				fontName=e.getTextContent();
				continue;
			}

			if (name.equals("commandlineparameters")) {
				commandLineDialogParameters=e.getTextContent();
				continue;
			}

			if (name.equals("commandlineansi")) {
				commandLineUseANSI=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("backgroundsimulation")) {
				final Integer I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I!=null) switch (I.intValue()) {
				case 0: backgroundSimulation=BackgroundProcessingMode.BACKGROUND_NOTHING; break;
				case 1: backgroundSimulation=BackgroundProcessingMode.BACKGROUND_CHECK_ONLY; break;
				case 2: backgroundSimulation=BackgroundProcessingMode.BACKGROUND_SIMULATION; break;
				case 3: backgroundSimulation=BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS; break;
				}
				continue;
			}

			if (name.equals("autoconnect")) {
				final Integer I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I!=null) switch (I.intValue()) {
				case 0: autoConnect=ModelSurfacePanel.ConnectMode.OFF; break;
				case 1: autoConnect=ModelSurfacePanel.ConnectMode.AUTO; break;
				case 2: autoConnect=ModelSurfacePanel.ConnectMode.SMART; break;
				}
				continue;
			}

			if (name.equalsIgnoreCase("renameoncopy")) {
				final Integer I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I!=null) switch (I.intValue()) {
				case 0: renameOnCopy=RenameOnCopyMode.OFF; break;
				case 1: renameOnCopy=RenameOnCopyMode.SMART; break;
				case 2: renameOnCopy=RenameOnCopyMode.ALWAYS; break;
				}
				continue;
			}

			if (name.equals("timestampinvideo")) {
				paintTimeStamp=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("scalevideo")) {
				final Double D=NumberTools.getPositiveDouble(e.getTextContent());
				if (D!=null) animationFrameScale=Math.max(0.01,Math.min(1,D.doubleValue()));
				continue;
			}

			if (name.equals("startvideorecordingimmediately")) {
				animationRecordStartImmediately=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("animationstartstepmode")) {
				animationStartPaused=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("animationwaitattheend")) {
				animationFinishPaused=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("usegradients")) {
				useGradients=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("useshadows")) {
				useShadows=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("usehighcontrasts")) {
				useHighContrasts=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("usagestatistics")) {
				usageStatistics=e.getTextContent();
				continue;
			}

			if (name.equals("usagecputime")) {
				usageCPUTime=e.getTextContent();
				continue;
			}

			if (name.equals("feedbackbutton")) {
				showFeedbackButton=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("quickaccess")) {
				showQuickAccess=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("quickfilter")) {
				showQuickFilter=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("guianimations")) {
				useAnimations=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("networksimulationclient")) {
				serverUse=loadBoolean(e.getAttribute("Active"),false);
				serverData=e.getTextContent();
				continue;
			}

			if (name.equals("networksimulationserver")) {
				simulationServerAutoStart=loadBoolean(e.getAttribute("AutoStart"),false);
				final Integer I=NumberTools.getNotNegativeInteger(e.getAttribute("Port"));
				if (I!=null && I.intValue()>=1 && I.intValue()<=65535) simulationServerPort=I.intValue();
				simulationServerPasswort=e.getAttribute("Passwort");
				simulationServerLimitThreadCount=loadBoolean(e.getAttribute("Limited"),false);
				continue;
			}

			if (name.equals("networkanimationserver")) {
				webServerAutoStart=loadBoolean(e.getAttribute("AutoStart"),false);
				final Integer I=NumberTools.getNotNegativeInteger(e.getAttribute("Port"));
				if (I!=null && I.intValue()>=1 && I.intValue()<=65535) webServerPort=I.intValue();
				continue;
			}

			if (name.equals("networkwebsimulationserver")) {
				calcWebServerAutoStart=loadBoolean(e.getAttribute("AutoStart"),false);
				final Integer I=NumberTools.getNotNegativeInteger(e.getAttribute("Port"));
				if (I!=null && I.intValue()>=1 && I.intValue()<=65535) calcWebServerPort=I.intValue();
				continue;
			}

			if (name.equals("networkmqttsimulationserver")) {
				mqttServerAutoStart=loadBoolean(e.getAttribute("AutoStart"),false);
				mqttBroker=e.getAttribute("Broker");
				mqttTopic=e.getAttribute("Topic");
				mqttLoadTopic=e.getAttribute("TopicInfo");
				mqttVerifyCertificates=loadBoolean(e.getAttribute("VerifyCertificates"),false);
				continue;
			}

			if (name.equals("networkwebserver")) {
				serverAuthName=e.getAttribute("Name");
				serverAuthPassword=e.getAttribute("Password");
				serverTLSKeyStoreFile=e.getAttribute("TLSKeyStoreFile");
				serverTLSKeyStorePassword=e.getAttribute("TLSKeyStorePassword");
				continue;
			}

			if (name.equals("ddeserver")) {
				ddeServerAutoStart=loadBoolean(e.getAttribute("AutoStart"),false);
				continue;
			}

			if (name.equals("exceldde")) {
				customExcelRowName=e.getAttribute("RowIdentifier");
				customExcelColName=e.getAttribute("ColumnIdentifier");
				continue;
			}

			if (name.equals("socketserver")) {
				socketServerAutoStart=loadBoolean(e.getAttribute("AutoStart"),false);
				final Integer I=NumberTools.getNotNegativeInteger(e.getAttribute("Port"));
				if (I!=null && I.intValue()>=1 && I.intValue()<=65535) socketServerPort=I.intValue();
			}

			if (name.equals("jdk")) {
				javaJDKPath=e.getTextContent();
				continue;
			}

			if (name.equals("jsengine")) {
				jsEngine=e.getTextContent();
				continue;
			}

			if (name.equals("cancelsimulationonscripterror") || name.equals("canelsimulationonscripterror")) {
				cancelSimulationOnScriptError=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("maxjavascriptruntime")) {
				final Long L=NumberTools.getPositiveLong(e.getTextContent());
				if (L!=null) maxJSRunTimeSeconds=Math.min(L.intValue(),60);
				continue;
			}

			if (name.equals("modelsecurity")) {
				final String text=e.getTextContent().toLowerCase();
				if (text.equals("allowall")) modelSecurity=ModelSecurity.ALLOWALL;
				if (text.equals("ask")) modelSecurity=ModelSecurity.ASK;
				if (text.equals("strict")) modelSecurity=ModelSecurity.STRICT;
				signModels=loadBoolean(e.getAttribute("SignModels"),true);
				modelSecurityOnlyOnInternetFiles=loadBoolean(e.getAttribute("RestrictOnlyFileFromInternet"),true);
				continue;
			}

			if (name.equals("modelsecurityexternal")) {
				modelSecurityAllowExecuteExternal=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("allowtoopenlinks")) {
				allowToOpenLinks=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("nofitymode")) {
				final String text=e.getTextContent().toLowerCase();
				if (text.equals("always")) {notifyMode=NotifyMode.ALWAYS; continue;}
				if (text.equals("longrun")) {notifyMode=NotifyMode.LONGRUN; continue;}
				if (text.equals("off")) {notifyMode=NotifyMode.OFF; continue;}
				continue;
			}

			if (name.equals("nofitymqtt")) {
				notifyMQTT=loadBoolean(e.getAttribute("active"),false);
				notifyMQTTTopic=e.getTextContent();
				continue;
			}

			if (name.equals("proxy")) {
				useProxy=loadBoolean(e.getAttribute("Active"),false);
				proxyHost=e.getTextContent();
				final Integer I=NumberTools.getNotNegativeInteger(e.getAttribute("Port"));
				if (I!=null && I>0) proxyPort=I.intValue();
				proxyUser=e.getAttribute("Name");
				proxyPassword=e.getAttribute("Password");
				continue;
			}

			if (name.equals("autorestore")) {
				autoRestore=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("showquantils")) {
				showQuantils=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("statisticsshowerlangc")) {
				showErlangC=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("statisticsshowremarks")) {
				showRemarks=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("statisticsexpandall")) {
				expandAllStatistics=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("defaultfileformats")) {
				XMLTools.DefaultSaveFormat format;
				format=XMLTools.DefaultSaveFormat.getFormat(e.getAttribute("models"));
				if (format!=null) defaultSaveFormatModels=format;
				format=XMLTools.DefaultSaveFormat.getFormat(e.getAttribute("statistics"));
				if (format!=null) defaultSaveFormatStatistics=format;
				format=XMLTools.DefaultSaveFormat.getFormat(e.getAttribute("parameterseries"));
				if (format!=null) defaultSaveFormatParameterSeries=format;
				format=XMLTools.DefaultSaveFormat.getFormat(e.getAttribute("optimizersetup"));
				if (format!=null) defaultSaveFormatOptimizerSetups=format;
				continue;
			}

			if (name.equals("backupfiles")) {
				useBackupFiles=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("defaultusername")) {
				defaultUserName=e.getTextContent();
				continue;
			}

			if (name.equals("defaultuseremail")) {
				defaultUserEMail=e.getTextContent();
				continue;
			}

			if (name.equals("parameterseriestabledigits")) {
				final Long L=NumberTools.getPositiveLong(e.getTextContent());
				if (L!=null) {
					final int digits=L.intValue();
					if (digits==1 || digits==3) parameterSeriesTableDigits=digits; else parameterSeriesTableDigits=9;
				}
				parameterSeriesTableDigitsUseOnExport=loadBoolean(e.getAttribute("UseOnExport"),false);
				continue;
			}

			if (name.equals("parameterseriesupscale")) {
				final Long L=NumberTools.getNotNegativeLong(e.getTextContent());
				if (L!=null) {
					parameterSeriesUpscale=Math.min(3,L.intValue());
				}
				continue;
			}

			if (name.equals("parameterseriesforcetimeasnumberonexport")) {
				parameterSeriesForceTimeAsNumberOnExport=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("parameterseriesforcepercentasnumberonexport")) {
				parameterSeriesForcePercentAsNumberOnExport=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("quickaccessfilter")) {
				quickAccessFilter=e.getTextContent();
				continue;
			}

			if (name.equals("elementlistsort")) {
				elementListSort=ElementListSort.getFromXMLName(e.getTextContent());
				continue;
			}

			if (name.equals("openstatistics")) {
				openWord=loadBoolean(e.getAttribute("docx"),true);
				openODT=loadBoolean(e.getAttribute("odt"),false);
				openExcel=loadBoolean(e.getAttribute("xlsx"),true);
				openODS=loadBoolean(e.getAttribute("ods"),false);
				openPDF=loadBoolean(e.getAttribute("pdf"),false);
				continue;
			}

			if (name.equals("showrulers")) {
				showRulers=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("showmemory")) {
				showMemoryUsage=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("digits")) {
				Long L;
				L=NumberTools.getPositiveLong(e.getAttribute("Numbers"));
				if (L!=null) statisticsNumberDigits=Math.min(9,L.intValue());
				L=NumberTools.getPositiveLong(e.getAttribute("Percent"));
				if (L!=null) statisticsPercentDigits=Math.min(9,L.intValue());
				showApproxSignOnValuesNearZero=loadBoolean(e.getAttribute("UseApproxSign"),true);
				continue;
			}

			if (name.equals("batchmeansconfidencelevels")) {
				batchMeansConfidenceLevels=e.getTextContent();
				continue;
			}

			if (name.equals("quantillevels")) {
				quantilLevels=e.getTextContent();
				continue;
			}

			if (name.equals("chartsetup")) {
				chartSetup.loadFromXML(e);
				continue;
			}

			if (name.equals("statisticfilter")) {
				statisticOverviewFilter=e.getTextContent();
				continue;
			}

			if (name.equals(GitSetup.XML_PARENT_NAME.toLowerCase())) {
				final NodeList l2=e.getChildNodes();
				final int count2=l2.getLength();
				for (int j=0;j<count2;j++) {
					if (!(l2.item(j) instanceof Element)) continue;
					final Element e2=(Element)l2.item(j);
					if (e2.getNodeName().equalsIgnoreCase(GitSetup.XML_NAME)) {
						final GitSetup gitSetup=new GitSetup();
						gitSetup.load(e2);
						gitSetups.add(gitSetup);
					}
				}
				continue;
			}

			if (name.equals("dynamicimportclass")) {
				dynamicImportClasses.add(e.getTextContent());
				continue;
			}

			if (name.equals("ebook")) {
				eBook=e.getTextContent();
				continue;
			}

			if (name.equals("search")) {
				searchCaseSensitive=loadBoolean(e.getAttribute("CaseSensitive"),false);
				searchStationIDs=loadBoolean(e.getAttribute("StationIDs"),false);
				searchFullMatchOnly=loadBoolean(e.getAttribute("FullMatchOnly"),false);
				searchRegularExpression=loadBoolean(e.getAttribute("RegularExpression"),false);
				searchForward=loadBoolean(e.getAttribute("Forward"),true);
				continue;
			}

			if (name.equals("batch")) {
				batchFolder=e.getAttribute("Folder");
				if (loadBoolean(e.getAttribute("FilterMode"),false)) batchMode=BatchMode.FILTER;
				batchFilterScript=e.getAttribute("FilterScript");
				batchOutputFile=e.getAttribute("OutputFile");
				continue;
			}

			if (name.equals("distributionlistfilter")) {
				distributionListFilter=e.getTextContent();
				continue;
			}

			if (name.equals("statistictreebookmarks")) {
				statisticTreeBookmarks.add(e.getTextContent());
				continue;
			}

			if (name.equals("statisticdashboard")) {
				dashboardSetup.add(e.getTextContent());
				continue;
			}

			if (name.equals("gradienttemplates")) {
				gradientTempaltes=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("gradientnavigator")) {
				gradientNavigator=loadBoolean(e.getTextContent(),false);
				continue;
			}

			if (name.equals("canceledanimationstatistics")) {
				canceledAnimationStatistics=CanceledSimulationStatistics.getByName(e.getTextContent(),CanceledSimulationStatistics.OFF);
				continue;
			}

			if (name.equals("canceledsimulationstatistics")) {
				canceledSimulationStatistics=CanceledSimulationStatistics.getByName(e.getTextContent(),CanceledSimulationStatistics.ASK);
				continue;
			}

			if (name.equals("spellcheckinglanguages")) {
				spellCheckingLanguages=e.getTextContent();
				continue;
			}

			if (name.equals("spellcheckingmode")) {
				ScriptEditorAreaBuilder.TextAreaMode mode=ScriptEditorAreaBuilder.TextAreaMode.getFromString(e.getTextContent());
				if (mode!=null) spellCheckMode.add(mode);
				continue;
			}

			if (name.equals("mousewheetzoomfixmousePosition")) {
				mouseWheelZoomFixMousePosition=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("userdefinedcalculationfunctions")) {
				userDefinedCalculationFunctions.add(e.getTextContent());
				continue;
			}

			if (name.equals("userdefinedjsfunctions")) {
				userDefinedJSFunctions.add(e.getTextContent());
				continue;
			}

			if (name.equals("userdefinedjavafunctions")) {
				userDefinedJavaFunctions.add(e.getTextContent());
				continue;
			}

			if (name.equals("allowspellcheck")) {
				allowSpellCheck=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("collectnextstationdata")) {
				collectNextStationData=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("simulationprogressanimation")) {
				simulationProgressAnimation=loadBoolean(e.getTextContent(),true);
				continue;
			}

			if (name.equals("touchsupport")) {
				touchSupport=loadBoolean(e.getTextContent(),false);
				continue;
			}
		}

		if (useLastFiles) {
			lastFiles=addToArray(lastFiles,files);
		} else {
			lastFiles=null;
		}

		/* Wenn nichts gewählt ist, dann war das wahrscheinlich ein Update von einer früheren Version. Dann Standard wiederherstellen. */
		if (spellCheckMode.size()==0) {
			spellCheckMode.addAll(Arrays.asList(ScriptEditorAreaBuilder.TextAreaMode.values()));
		}
	}

	@Override
	protected void saveSetupToXML(final Document doc, final Element root) {
		Element node;

		root.appendChild(node=doc.createElement("Language"));
		node.setTextContent(language.toLowerCase());

		if (numberFormat!=NumberFormat.BY_SYSTEM) {
			root.appendChild(node=doc.createElement("NumberFormat"));
			node.setTextContent(numberFormat.name);
		}

		if (startSizeMode!=StartSizeMode.START_MODE_DEFAULT) {
			root.appendChild(node=doc.createElement("Fullscreen"));
			if (startSizeMode==StartSizeMode.START_MODE_FULLSCREEN) node.setTextContent("1");
			if (startSizeMode==StartSizeMode.START_MODE_LASTSIZE) node.setTextContent("2");
		}

		if (startSizeMode==StartSizeMode.START_MODE_LASTSIZE) {
			root.appendChild(node=doc.createElement("LastWindowSize"));
			node.setAttribute("Mode",""+lastSizeMode);
			node.setAttribute("X",""+lastPosition.x);
			node.setAttribute("Y",""+lastPosition.y);
			node.setAttribute("Width",""+lastSize.width);
			node.setAttribute("Height",""+lastSize.height);
		}

		if (lastZoom!=1.0) {
			root.appendChild(node=doc.createElement("LastZoom"));
			node.setTextContent(NumberTools.formatSystemNumber(lastZoom));
		}

		if (!restoreSubEditWindowSize) {
			root.appendChild(node=doc.createElement("RestoreSubEditWindowSize"));
			node.setTextContent("0");
		}

		if (startTemplateMode!=StartTemplateMode.START_TEMPLATE_HIDDEN) {
			root.appendChild(node=doc.createElement("ShowTemplates"));
			switch (startTemplateMode) {
			case START_TEMPLATE_HIDDEN: node.setAttribute("Mode","0"); break;
			case START_TEMPLATE_VISIBLE: node.setAttribute("Mode","1"); break;
			case START_TEMPLATE_LASTSTATE: node.setAttribute("Mode","2"); break;
			}
			node.setTextContent(showTemplates?"1":"0");
		}

		if (startModel!=null && !startModel.isBlank()) {
			root.appendChild(node=doc.createElement("StartModel"));
			node.setTextContent(startModel);
		}

		if (surfaceHelp!=SurfaceHelp.START_ONLY) {
			root.appendChild(node=doc.createElement("SurfaceHelp"));
			switch (surfaceHelp) {
			case NEVER: node.setTextContent("Never"); break;
			case START_ONLY: node.setTextContent("StartOnly"); break;
			case ALWAYS: node.setTextContent("Always"); break;
			}
		}

		if (!surfaceGlassInfos) {
			root.appendChild(node=doc.createElement("SurfaceGlassInfos"));
			node.setTextContent("0");
		}

		if (scaleGUI!=1) {
			root.appendChild(node=doc.createElement("Scale"));
			node.setTextContent(NumberTools.localNumberToSystemNumber(NumberTools.formatNumber(scaleGUI)));
		}

		if ((lookAndFeel!=null && !lookAndFeel.isBlank()) || !lookAndFeelCombinedMenu) {
			root.appendChild(node=doc.createElement("LookAndFeel"));
			if (lookAndFeel!=null) node.setTextContent(lookAndFeel);
			if (!lookAndFeelCombinedMenu) node.setAttribute("combinedMenu","0");
		}

		if (autoSaveMode!=AutoSaveMode.AUTOSAVE_OFF) {
			root.appendChild(node=doc.createElement("AutoSave"));
			switch (autoSaveMode) {
			case AUTOSAVE_OFF:
				node.setTextContent("0");
				break;
			case AUTOSAVE_SIMULATION:
				node.setTextContent("1");
				break;
			case AUTOSAVE_ALWAYS:
				node.setTextContent("2");
				break;
			}
		}

		if (!hintDialogs.isEmpty()) {
			root.appendChild(node=doc.createElement("HintDialogs"));
			node.setTextContent(hintDialogs);
		}

		if (imageSize!=2000 || !imagesInline) {
			root.appendChild(node=doc.createElement("Images"));
			node.setTextContent(""+imageSize);
			if (!imagesInline) node.setAttribute("Inline","0");
		}

		if (imagePathAnimation!=null && !imagePathAnimation.isBlank()) {
			root.appendChild(node=doc.createElement("ImagesAnimation"));
			node.setTextContent(imagePathAnimation);
		}

		if (grid!=ModelSurface.Grid.LINES) {
			root.appendChild(node=doc.createElement("Raster"));
			node.setTextContent(grid.id);
		}

		if (showIDs) {
			root.appendChild(node=doc.createElement("ShowIDs"));
			node.setTextContent("1");
		}

		if (showStationDescription) {
			root.appendChild(node=doc.createElement("ShowStationDescription"));
			node.setTextContent("1");
		}

		if (!antialias) {
			root.appendChild(node=doc.createElement("AntiAlias"));
			node.setTextContent("0");
		}

		if (!useTransparencyExportFix) {
			root.appendChild(node=doc.createElement("TransparencyExportFix"));
			node.setTextContent("0");
		}

		if (!statisticInTooltips) {
			root.appendChild(node=doc.createElement("StatisticInTooltips"));
			node.setTextContent("0");
		}

		if (showBackgroundTooltips) {
			root.appendChild(node=doc.createElement("BackgroundTooltips"));
			node.setTextContent("1");
		}

		if ((statisticHeatMap!=null && statisticHeatMap!=EditorPanelStatistics.HeatMapMode.OFF) || statisticHeatMapSize!=50 || !HeatMapImage.DEFAULT_COLOR_LOW_INTENSITY.equals(statisticHeatMapColorLow) || !HeatMapImage.DEFAULT_COLOR_HIGH_INTENSITY.equals(statisticHeatMapColorHigh) || statisticHeatMapIntensityMin!=HeatMapImage.DEFAULT_INTENSITY_MIN  || statisticHeatMapIntensityMax!=HeatMapImage.DEFAULT_INTENSITY_MAX) {
			root.appendChild(node=doc.createElement("StatisticHeatMap"));
			if (statisticHeatMap==null) {
				node.setTextContent(EditorPanelStatistics.HeatMapMode.OFF.xmlName);
			} else {
				node.setTextContent(statisticHeatMap.xmlName);
			}
			if (statisticHeatMapSize!=50) node.setAttribute("Size",""+statisticHeatMapSize);
			if (!HeatMapImage.DEFAULT_COLOR_LOW_INTENSITY.equals(statisticHeatMapColorLow)) node.setAttribute("ColorLow",EditModel.saveColor(statisticHeatMapColorLow));
			if (!HeatMapImage.DEFAULT_COLOR_HIGH_INTENSITY.equals(statisticHeatMapColorHigh)) node.setAttribute("ColorHigh",EditModel.saveColor(statisticHeatMapColorHigh));
			if (statisticHeatMapIntensityMin!=HeatMapImage.DEFAULT_INTENSITY_MIN) node.setAttribute("IntensityMin",NumberTools.formatSystemNumber(statisticHeatMapIntensityMin));
			if (statisticHeatMapIntensityMax!=HeatMapImage.DEFAULT_INTENSITY_MAX) node.setAttribute("IntensityMax",NumberTools.formatSystemNumber(statisticHeatMapIntensityMax));
		}

		if (reportSettings!=null && !reportSettings.isEmpty()) {
			root.appendChild(node=doc.createElement("Report"));
			node.setTextContent(reportSettings);
		}

		reportStyle.save(root);

		if (useLastFiles) {
			if (lastFiles!=null && lastFiles.length>0) for (int i=0;i<lastFiles.length;i++) {
				root.appendChild(node=doc.createElement("LastFiles"));
				node.setTextContent(lastFiles[i]);
			}
		} else {
			root.appendChild(node=doc.createElement("UseLastFiles"));
			node.setTextContent("0");
		}

		if (!testJavaVersion) {
			root.appendChild(node=doc.createElement("JavaVersionCheck"));
			node.setTextContent("0");
		}

		if (autoUpdate!=AutoUpdate.INSTALL) {
			root.appendChild(node=doc.createElement("AutoUpdate"));
			node.setTextContent(autoUpdate.name);
		}

		if (!useMultiCoreSimulation || useMultiCoreSimulationMaxCount!=1024 || useNUMAMode || !useDynamicThreadBalance || useMultiCoreSimulationOnRepeatedSimulations) {
			root.appendChild(node=doc.createElement("AllCPUCoresSimulation"));
			node.setTextContent(useMultiCoreSimulation?"1":"0");
			if (useMultiCoreSimulationMaxCount!=1024) node.setAttribute("MaxCount",""+useMultiCoreSimulationMaxCount);
			if (useNUMAMode) node.setAttribute("NUMA","1");
			if (!useDynamicThreadBalance) node.setAttribute("Dynamic","0");
			if (useMultiCoreSimulationOnRepeatedSimulations) node.setAttribute("SplitRepeatedRuns","1");
		}

		if (!useMultiCoreAnimation) {
			root.appendChild(node=doc.createElement("AllCPUCoresAnimation"));
			node.setTextContent("0");
		}

		if (!useSlowModeAnimation) {
			root.appendChild(node=doc.createElement("SlowAnimationForAnalogValues"));
			node.setTextContent("0");
		}

		if (!showStationRunTimeData) {
			root.appendChild(node=doc.createElement("AnimationStationRunTimeData"));
			node.setTextContent("0");
		}

		if (!animateResources) {
			root.appendChild(node=doc.createElement("AnimationResourcesTransporters"));
			node.setTextContent("0");
		}

		if (!respectPauseCommand) {
			root.appendChild(node=doc.createElement("AnimationRespectPauseCommand"));
			node.setTextContent("0");
		}

		if (!showSingleStepLogData) {
			root.appendChild(node=doc.createElement("AnimationSingleStepLogging"));
			node.setTextContent("0");
		}

		if (!lastLogFile.isEmpty() || !singleLineEventLog || !logGrouped || !logColors || !logFormatedTime || !logPrintIDs || logMaxRecords>0 || logMode==LogMode.DDE || !logDDEworkbook.isBlank() || !logDDEsheet.isBlank() || !logStationIDs.isEmpty() || !logTypeArrival || !logTypeLeave || !logTypeInfoStation || !logTypeInfoSystem) {
			root.appendChild(node=doc.createElement("Logging"));
			node.setTextContent(lastLogFile);
			if (!singleLineEventLog) node.setAttribute("CompactFormat","0");
			if (!logGrouped) node.setAttribute("GroupRecords","0");
			if (!logColors) node.setAttribute("UseColors","0");
			if (!logFormatedTime) node.setAttribute("FormatedTime","0");
			if (!logPrintIDs) node.setAttribute("PrintIDs","0");
			if (logMaxRecords>0) node.setAttribute("MaxRecords",""+logMaxRecords);
			if (logMode==LogMode.DDE) node.setAttribute("DDE","1");
			if (!logDDEworkbook.isBlank()) node.setAttribute("DDEWorkbook",logDDEworkbook);
			if (!logDDEsheet.isBlank()) node.setAttribute("DDESheet",logDDEsheet);
			if (!logStationIDs.isBlank()) node.setAttribute("IDs",logStationIDs);
			if (!logTypeArrival) node.setAttribute("TypeArrival","0");
			if (!logTypeLeave) node.setAttribute("TypeLeave","0");
			if (!logTypeInfoStation) node.setAttribute("TypeInfoStation","0");
			if (!logTypeInfoSystem) node.setAttribute("TypeInfoSystem","0");
		}

		if (highPriority) {
			root.appendChild(node=doc.createElement("HighPriority"));
			node.setTextContent("1");
		}

		if (animationDelay!=400) {
			root.appendChild(node=doc.createElement("AnimationDelay"));
			node.setTextContent(""+animationDelay);
		}

		if (animationWarmUpMode!=AnimationMode.ANIMATION_WARMUP_SKIP) {
			root.appendChild(node=doc.createElement("WarmUpPhaseAtAnimation"));
			switch (animationWarmUpMode) {
			case ANIMATION_WARMUP_NORMAL: node.setTextContent("Default"); break;
			case ANIMATION_WARMUP_ASK: node.setTextContent("Ask"); break;
			case ANIMATION_WARMUP_SKIP: node.setTextContent("SkipIfNeeded"); break;
			case ANIMATION_WARMUP_FAST: node.setTextContent("FastForward"); break;
			}
		}

		if (lastStart!=null && !lastStart.isEmpty()) {
			root.appendChild(node=doc.createElement("LastStart"));
			node.setTextContent(lastStart);
		}

		if (visibleTemplateGroups!=null && !visibleTemplateGroups.isEmpty()) {
			root.appendChild(node=doc.createElement("VisibleTemplateGroups"));
			node.setTextContent(visibleTemplateGroups);
		}

		if (openTemplateGroups!=null && !openTemplateGroups.isEmpty()) {
			root.appendChild(node=doc.createElement("OpenTemplateGroups"));
			node.setTextContent(openTemplateGroups);
		}

		if (favoriteTemplates!=null && !favoriteTemplates.isEmpty()) {
			root.appendChild(node=doc.createElement("FavoriteTemplates"));
			node.setTextContent(favoriteTemplates);
		}

		if (onlyOneOpenTemplatesGroup) {
			root.appendChild(node=doc.createElement("OnlyOneOpenTemplateGroup"));
			node.setTextContent("1");
		}

		if (filterJavascript!=null && !filterJavascript.isBlank()) {
			root.appendChild(node=doc.createElement("FilterJavascript"));
			node.setTextContent(filterJavascript);
		}

		if (filterJava!=null && !filterJava.isBlank()) {
			root.appendChild(node=doc.createElement("FilterJava"));
			node.setTextContent(filterJava);
		}

		if (filterList!=null && !filterList.isBlank()) {
			root.appendChild(node=doc.createElement("FilterListe"));
			node.setTextContent(filterList);
		}

		if (lastFilterMode!=0) {
			root.appendChild(node=doc.createElement("FilterMode"));
			node.setTextContent(""+lastFilterMode);
		}

		if (scriptScriptRunner!=null && !scriptScriptRunner.isBlank()) {
			root.appendChild(node=doc.createElement("Javascript"));
			node.setTextContent(scriptScriptRunner);
		}

		if (scriptCalculator!=null && !scriptCalculator.isBlank()) {
			root.appendChild(node=doc.createElement("CalculatorScript"));
			node.setTextContent(scriptCalculator);
		}

		if (scriptFontSize!=13) {
			root.appendChild(node=doc.createElement("ScriptFontsize"));
			node.setTextContent(""+scriptFontSize);
		}

		if (fontName!=null && !fontName.isBlank()) {
			root.appendChild(node=doc.createElement("FontName"));
			node.setTextContent(fontName);
		}

		if (commandLineDialogParameters!=null && !commandLineDialogParameters.isBlank()) {
			root.appendChild(node=doc.createElement("CommandLineParameters"));
			node.setTextContent(commandLineDialogParameters);
		}

		if (!commandLineUseANSI) {
			root.appendChild(node=doc.createElement("CommandLineANSI"));
			node.setTextContent("0");
		}

		if (backgroundSimulation!=BackgroundProcessingMode.BACKGROUND_SIMULATION) {
			root.appendChild(node=doc.createElement("BackgroundSimulation"));
			switch (backgroundSimulation) {
			case BACKGROUND_NOTHING: node.setTextContent("0"); break;
			case BACKGROUND_CHECK_ONLY: node.setTextContent("1"); break;
			case BACKGROUND_SIMULATION: node.setTextContent("2"); break;
			case BACKGROUND_SIMULATION_ALWAYS: node.setTextContent("3"); break;
			}
		}

		if (autoConnect!=ModelSurfacePanel.ConnectMode.OFF) {
			root.appendChild(node=doc.createElement("AutoConnect"));
			switch (autoConnect) {
			case OFF: node.setTextContent("0"); break;
			case AUTO: node.setTextContent("1"); break;
			case SMART: node.setTextContent("2"); break;
			}
		}

		if (renameOnCopy!=RenameOnCopyMode.SMART) {
			root.appendChild(node=doc.createElement("RenameOnCopy"));
			switch (renameOnCopy) {
			case OFF: node.setTextContent("0"); break;
			case SMART: node.setTextContent("1"); break;
			case ALWAYS: node.setTextContent("2"); break;
			}
		}

		if (!paintTimeStamp) {
			root.appendChild(node=doc.createElement("TimeStampInVideo"));
			node.setTextContent("0");
		}

		if (animationFrameScale!=1.0) {
			root.appendChild(node=doc.createElement("ScaleVideo"));
			node.setTextContent(NumberTools.formatSystemNumber(animationFrameScale));
		}

		if (!animationRecordStartImmediately) {
			root.appendChild(node=doc.createElement("StartVideoRecordingImmediately"));
			node.setTextContent("0");
		}

		if (animationStartPaused) {
			root.appendChild(node=doc.createElement("AnimationStartStepMode"));
			node.setTextContent("1");
		}

		if (animationFinishPaused) {
			root.appendChild(node=doc.createElement("AnimationWaitAtTheEnd"));
			node.setTextContent("1");
		}

		if (!useGradients) {
			root.appendChild(node=doc.createElement("UseGradients"));
			node.setTextContent("0");
		}

		if (!useShadows) {
			root.appendChild(node=doc.createElement("UseShadows"));
			node.setTextContent("0");
		}

		if (useHighContrasts) {
			root.appendChild(node=doc.createElement("UseHighContrasts"));
			node.setTextContent("1");
		}

		if (!usageStatistics.isEmpty()) {
			root.appendChild(node=doc.createElement("UsageStatistics"));
			node.setTextContent(usageStatistics);
		}

		if (!usageCPUTime.isEmpty()) {
			root.appendChild(node=doc.createElement("UsageCPUTime"));
			node.setTextContent(usageCPUTime);
		}

		if (!showFeedbackButton) {
			root.appendChild(node=doc.createElement("FeedbackButton"));
			node.setTextContent("0");
		}

		if (!showQuickAccess) {
			root.appendChild(node=doc.createElement("QuickAccess"));
			node.setTextContent("0");
		}

		if (!showQuickFilter	) {
			root.appendChild(node=doc.createElement("QuickFilter"));
			node.setTextContent("0");
		}

		if (!useAnimations) {
			root.appendChild(node=doc.createElement("GUIAnimations"));
			node.setTextContent("0");
		}

		if (serverUse || (serverData!=null && !serverData.trim().equals("localhost:8183"))) {
			root.appendChild(node=doc.createElement("NetworkSimulationClient"));
			if (serverUse) node.setAttribute("Active","1");
			if (serverData!=null) node.setTextContent(serverData);
		}

		if (simulationServerAutoStart || simulationServerPort!=8183 || (simulationServerPasswort!=null && !simulationServerPasswort.isBlank()) || simulationServerLimitThreadCount) {
			root.appendChild(node=doc.createElement("NetworkSimulationServer"));
			if (simulationServerAutoStart) node.setAttribute("AutoStart","1");
			node.setAttribute("Port",""+simulationServerPort);
			if (simulationServerPasswort!=null && !simulationServerPasswort.isBlank()) node.setAttribute("Passwort",""+simulationServerPasswort);
			if (simulationServerLimitThreadCount) node.setAttribute("Limited","1");
		}

		if (webServerAutoStart || webServerPort!=81) {
			root.appendChild(node=doc.createElement("NetworkAnimationServer"));
			node.setAttribute("Port",""+webServerPort);
			if (webServerAutoStart) node.setAttribute("AutoStart","1");
		}

		if (calcWebServerAutoStart || calcWebServerPort!=80) {
			root.appendChild(node=doc.createElement("NetworkWebSimulationServer"));
			node.setAttribute("Port",""+calcWebServerPort);
			if (calcWebServerAutoStart) node.setAttribute("AutoStart","1");
		}

		if (mqttServerAutoStart || !mqttBroker.equals("tcp://localhost") || !mqttTopic.equals(MainFrame.PROGRAM_NAME+"/task") || !mqttLoadTopic.equals(MainFrame.PROGRAM_NAME+"/info") || mqttVerifyCertificates) {
			root.appendChild(node=doc.createElement("NetworkMQTTSimulationServer"));
			node.setAttribute("Broker",""+mqttBroker);
			node.setAttribute("Topic",""+mqttTopic);
			node.setAttribute("TopicInfo",""+mqttLoadTopic);
			if (mqttVerifyCertificates) node.setAttribute("VerifyCertificates","1");
			if (mqttServerAutoStart) node.setAttribute("AutoStart","1");
		}

		if (!serverAuthName.isEmpty() || !serverAuthPassword.isEmpty() || !serverTLSKeyStoreFile.isEmpty() || !serverTLSKeyStorePassword.isEmpty()) {
			root.appendChild(node=doc.createElement("NetworkWebServer"));
			node.setAttribute("Name",serverAuthName);
			node.setAttribute("Password",serverAuthPassword);
			node.setAttribute("TLSKeyStoreFile",serverTLSKeyStoreFile);
			node.setAttribute("TLSKeyStorePassword",serverTLSKeyStorePassword);
		}

		if (ddeServerAutoStart) {
			root.appendChild(node=doc.createElement("DDEServer"));
			node.setAttribute("AutoStart","1");
		}

		if ((customExcelRowName!=null && !customExcelRowName.isBlank()) || (customExcelColName!=null && !customExcelColName.isBlank())) {
			root.appendChild(node=doc.createElement("ExcelDDE"));
			if (customExcelRowName!=null && !customExcelRowName.isBlank()) node.setAttribute("RowIdentifier",""+customExcelRowName);
			if (customExcelColName!=null && !customExcelColName.isBlank()) node.setAttribute("ColumnIdentifier",""+customExcelColName);
		}

		if (socketServerAutoStart || socketServerPort!=1000) {
			root.appendChild(node=doc.createElement("SocketServer"));
			node.setAttribute("Port",""+webServerPort);
			if (socketServerAutoStart) node.setAttribute("AutoStart","1");
		}

		if (!javaJDKPath.isEmpty())  {
			root.appendChild(node=doc.createElement("JDK"));
			node.setTextContent(javaJDKPath);
		}

		if (!jsEngine.isEmpty())  {
			root.appendChild(node=doc.createElement("jsEngine"));
			node.setTextContent(jsEngine);
		}

		if (!cancelSimulationOnScriptError) {
			root.appendChild(node=doc.createElement("CancelSimulationOnScriptError"));
			node.setTextContent("0");
		}

		if (maxJSRunTimeSeconds!=2) {
			root.appendChild(node=doc.createElement("MaxJavascriptRunTime"));
			node.setTextContent(""+maxJSRunTimeSeconds);
		}

		if (modelSecurity!=ModelSecurity.ASK || !signModels  || !modelSecurityOnlyOnInternetFiles) {
			root.appendChild(node=doc.createElement("ModelSecurity"));
			switch (modelSecurity) {
			case ALLOWALL: node.setTextContent("AllowAll"); break;
			case ASK: node.setTextContent("Ask"); break;
			case STRICT: node.setTextContent("Strict"); break;
			}
			if (!signModels) node.setAttribute("SignModels","0");
			if (!modelSecurityOnlyOnInternetFiles) node.setAttribute("RestrictOnlyFileFromInternet","0");
		}

		if (modelSecurityAllowExecuteExternal) {
			root.appendChild(node=doc.createElement("ModelSecurityExternal"));
			node.setTextContent("1");
		}

		if (!allowToOpenLinks) {
			root.appendChild(node=doc.createElement("AllowToOpenLinks"));
			node.setTextContent("0");
		}

		if (notifyMode!=NotifyMode.LONGRUN) {
			root.appendChild(node=doc.createElement("NofityMode"));
			switch (notifyMode) {
			case ALWAYS: node.setTextContent("Always"); break;
			case LONGRUN: node.setTextContent("LongRun"); break;
			case OFF: node.setTextContent("Off"); break;
			}
		}

		if (notifyMQTT || !notifyMQTTTopic.equals(MainFrame.PROGRAM_NAME+"/notify")) {
			root.appendChild(node=doc.createElement("NofityMQTT"));
			node.setAttribute("active",notifyMQTT?"1":"0");
			node.setTextContent(notifyMQTTTopic);
		}

		if (useProxy || !proxyHost.isEmpty() || proxyPort!=8080 || !proxyUser.isEmpty() || !proxyPassword.isEmpty()) {
			root.appendChild(node=doc.createElement("Proxy"));
			node.setTextContent(proxyHost);
			node.setAttribute("Port",""+proxyPort);
			if (useProxy) node.setAttribute("Active","1");
			if (!proxyUser.isEmpty()) node.setAttribute("Name",proxyUser);
			if (!proxyPassword.isEmpty()) node.setAttribute("Password",proxyPassword);
		}

		if (autoRestore) {
			root.appendChild(node=doc.createElement("AutoRestore"));
			node.setTextContent("1");
		}

		if (!showQuantils) {
			root.appendChild(node=doc.createElement("ShowQuantils"));
			node.setTextContent("0");
		}

		if (!showErlangC) {
			root.appendChild(node=doc.createElement("StatisticsShowErlangC"));
			node.setTextContent("0");
		}

		if (!showRemarks) {
			root.appendChild(node=doc.createElement("StatisticsShowRemarks"));
			node.setTextContent("0");
		}

		if (expandAllStatistics) {
			root.appendChild(node=doc.createElement("StatisticsExpandAll"));
			node.setTextContent("1");
		}

		boolean needStoreDefaultFileFormats=false;
		if (defaultSaveFormatModels!=XMLTools.DefaultSaveFormat.XML) needStoreDefaultFileFormats=true;
		if (defaultSaveFormatStatistics!=XMLTools.DefaultSaveFormat.XML) needStoreDefaultFileFormats=true;
		if (defaultSaveFormatParameterSeries!=XMLTools.DefaultSaveFormat.ZIP_XML) needStoreDefaultFileFormats=true;
		if (defaultSaveFormatOptimizerSetups!=XMLTools.DefaultSaveFormat.ZIP_XML) needStoreDefaultFileFormats=true;
		if (needStoreDefaultFileFormats) {
			root.appendChild(node=doc.createElement("DefaultFileFormats"));
			if (defaultSaveFormatModels!=XMLTools.DefaultSaveFormat.XML) node.setAttribute("models",defaultSaveFormatModels.identifier);
			if (defaultSaveFormatStatistics!=XMLTools.DefaultSaveFormat.XML) node.setAttribute("statistics",defaultSaveFormatStatistics.identifier);
			if (defaultSaveFormatParameterSeries!=XMLTools.DefaultSaveFormat.ZIP_XML) node.setAttribute("parameterseries",defaultSaveFormatParameterSeries.identifier);
			if (defaultSaveFormatOptimizerSetups!=XMLTools.DefaultSaveFormat.ZIP_XML) node.setAttribute("optimizersetup",defaultSaveFormatOptimizerSetups.identifier);
		}

		if (useBackupFiles) {
			root.appendChild(node=doc.createElement("BackupFiles"));
			node.setTextContent("1");
		}

		if (defaultUserName!=null && !defaultUserName.isBlank()) {
			root.appendChild(node=doc.createElement("DefaultUserName"));
			node.setTextContent(defaultUserName);
		}

		if (defaultUserEMail!=null && !defaultUserEMail.isBlank()) {
			root.appendChild(node=doc.createElement("DefaultUserEMail"));
			node.setTextContent(defaultUserEMail);
		}

		if (parameterSeriesTableDigits!=1 || parameterSeriesTableDigitsUseOnExport) {
			root.appendChild(node=doc.createElement("ParameterSeriesTableDigits"));
			node.setTextContent(""+parameterSeriesTableDigits);
			if (parameterSeriesTableDigitsUseOnExport) node.setAttribute("UseOnExport","1");
		}

		if (parameterSeriesUpscale>0) {
			root.appendChild(node=doc.createElement("ParameterSeriesUpscale"));
			node.setTextContent(""+parameterSeriesUpscale);
		}

		if (!parameterSeriesForceTimeAsNumberOnExport) {
			root.appendChild(node=doc.createElement("ParameterSeriesForceTimeAsNumberOnExport"));
			node.setTextContent("0");
		}

		if (!parameterSeriesForcePercentAsNumberOnExport) {
			root.appendChild(node=doc.createElement("ParameterSeriesForcePercentAsNumberOnExport"));
			node.setTextContent("0");
		}

		if (quickAccessFilter!=null && !quickAccessFilter.isEmpty() && quickAccessFilter.contains("-")) {
			root.appendChild(node=doc.createElement("QuickAccessFilter"));
			node.setTextContent(quickAccessFilter);
		}

		if (!elementListSort.isDefault) {
			root.appendChild(node=doc.createElement("ElementListSort"));
			node.setTextContent(elementListSort.xmlName);
		}

		if (!openWord || openODT || !openExcel || openODS || openPDF) {
			root.appendChild(node=doc.createElement("OpenStatistics"));
			node.setAttribute("docx",openWord?"1":"0");
			node.setAttribute("odt",openODT?"1":"0");
			node.setAttribute("xlsx",openExcel?"1":"0");
			node.setAttribute("ods",openODS?"1":"0");
			node.setAttribute("pdf",openPDF?"1":"0");
		}

		if (showRulers) {
			root.appendChild(node=doc.createElement("ShowRulers"));
			node.setTextContent("1");
		}

		if (showMemoryUsage) {
			root.appendChild(node=doc.createElement("ShowMemory"));
			node.setTextContent("1");
		}

		if (statisticsNumberDigits!=1 || statisticsPercentDigits!=1 || !showApproxSignOnValuesNearZero) {
			root.appendChild(node=doc.createElement("Digits"));
			node.setAttribute("Numbers",""+statisticsNumberDigits);
			node.setAttribute("Percent",""+statisticsPercentDigits);
			if (!showApproxSignOnValuesNearZero) node.setAttribute("UseApproxSign","0");
		}

		if (batchMeansConfidenceLevels!=null && !batchMeansConfidenceLevels.isBlank()) {
			root.appendChild(node=doc.createElement("BatchMeansConfidenceLevels"));
			node.setTextContent(batchMeansConfidenceLevels);
		}

		if (quantilLevels!=null && !quantilLevels.isBlank()) {
			root.appendChild(node=doc.createElement("QuantilLevels"));
			node.setTextContent(quantilLevels);
		}

		if (!StatisticViewerOverviewText.Filter.getDefault().equals(statisticOverviewFilter) && !statisticOverviewFilter.isEmpty()) {
			root.appendChild(node=doc.createElement("StatisticFilter"));
			node.setTextContent(statisticOverviewFilter);
		}

		root.appendChild(node=doc.createElement("ChartSetup"));
		chartSetup.saveToXML(node);

		if (gitSetups.size()>0) {
			root.appendChild(node=doc.createElement(GitSetup.XML_PARENT_NAME));
			for (GitSetup gitSetup: gitSetups) gitSetup.save(node);
		}

		for (String cls: dynamicImportClasses) {
			root.appendChild(node=doc.createElement("DynamicImportClass"));
			node.setTextContent(cls);
		}

		if (eBook!=null && !eBook.isBlank()) {
			root.appendChild(node=doc.createElement("eBook"));
			node.setTextContent(eBook);
		}

		if (searchCaseSensitive || searchStationIDs || searchFullMatchOnly || searchRegularExpression || !searchForward) {
			root.appendChild(node=doc.createElement("Search"));
			if (searchCaseSensitive) node.setAttribute("CaseSensitive","1");
			if (searchStationIDs) node.setAttribute("StationIDs","1");
			if (searchFullMatchOnly) node.setAttribute("FullMatchOnly","1");
			if (searchRegularExpression) node.setAttribute("RegularExpression","1");
			if (!searchForward) node.setAttribute("Forward","1");
		}

		if (!batchFolder.isEmpty() || batchMode!=BatchMode.SIMULATION || !batchFilterScript.isEmpty() || !batchOutputFile.isEmpty()) {
			root.appendChild(node=doc.createElement("Batch"));
			if (!batchFolder.isEmpty()) node.setAttribute("Folder",batchFolder);
			if (batchMode==BatchMode.FILTER) node.setAttribute("FilterMode","1");
			if (!batchFilterScript.isEmpty()) node.setAttribute("FilterScript",batchFilterScript);
			if (!batchOutputFile.isEmpty()) node.setAttribute("OutputFile",batchOutputFile);
		}

		if (distributionListFilter!=null && !distributionListFilter.isBlank()) {
			root.appendChild(node=doc.createElement("DistributionListFilter"));
			node.setTextContent(distributionListFilter.trim());
		}

		if (statisticTreeBookmarks.size()>0) for (String bookmark: statisticTreeBookmarks) {
			root.appendChild(node=doc.createElement("StatisticTreeBookmarks"));
			node.setTextContent(bookmark);
		}

		if (dashboardSetup.size()>0) for (String tile: dashboardSetup) {
			root.appendChild(node=doc.createElement("StatisticDashboard"));
			node.setTextContent(tile);
		}

		if (gradientTempaltes) {
			root.appendChild(node=doc.createElement("GradientTemplates"));
			node.setTextContent("1");
		}

		if (gradientNavigator) {
			root.appendChild(node=doc.createElement("GradientNavigator"));
			node.setTextContent("1");
		}

		if (canceledAnimationStatistics!=CanceledSimulationStatistics.OFF) {
			root.appendChild(node=doc.createElement("CanceledAnimationStatistics"));
			node.setTextContent(canceledAnimationStatistics.name);
		}

		if (canceledSimulationStatistics!=CanceledSimulationStatistics.ASK) {
			root.appendChild(node=doc.createElement("CanceledSimulationStatistics"));
			node.setTextContent(canceledSimulationStatistics.name);
		}

		root.appendChild(node=doc.createElement("SpellCheckingLanguages"));
		node.setTextContent(spellCheckingLanguages);

		for (ScriptEditorAreaBuilder.TextAreaMode mode: spellCheckMode) {
			root.appendChild(node=doc.createElement("SpellCheckingMode"));
			node.setTextContent(mode.name);
		}

		if (!mouseWheelZoomFixMousePosition) {
			root.appendChild(node=doc.createElement("MouseWheelZoomFixMousePosition"));
			node.setTextContent("0");
		}

		for (String userFunction: userDefinedCalculationFunctions) {
			root.appendChild(node=doc.createElement("UserDefinedCalculationFunctions"));
			node.setTextContent(userFunction);
		}

		for (String userFunction: userDefinedJSFunctions) {
			root.appendChild(node=doc.createElement("UserDefinedJSFunctions"));
			node.setTextContent(userFunction);
		}

		for (String userFunction: userDefinedJavaFunctions) {
			root.appendChild(node=doc.createElement("UserDefinedJavaFunctions"));
			node.setTextContent(userFunction);
		}

		if (!allowSpellCheck) {
			root.appendChild(node=doc.createElement("AllowSpellCheck"));
			node.setTextContent("0");
		}

		if (!collectNextStationData) {
			root.appendChild(node=doc.createElement("CollectNextStationData"));
			node.setTextContent("0");
		}

		if (!simulationProgressAnimation) {
			root.appendChild(node=doc.createElement("SimulationProgressAnimation"));
			node.setTextContent("0");
		}

		if (touchSupport) {
			root.appendChild(node=doc.createElement("TouchSupport"));
			node.setTextContent("1");
		}

		if (lastError!=null && !lastError.isBlank()) {
			root.appendChild(node=doc.createElement("LastError"));
			node.setTextContent(lastError);
		}
	}

	/**
	 * Stellt die Systemsprache ein und reinitialisiert
	 * die <code>Language</code>- und <code>LanguageStaticLoader</code>-Systeme.
	 * @param langName	Sprache, "de" oder "en"
	 */
	public void setLanguage(final String langName) {
		language=langName;
		saveSetup();
		Language.init(language);
		LanguageStaticLoader.setLanguage();
		if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();
	}
}