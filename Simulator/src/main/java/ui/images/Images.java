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
package ui.images;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Diese Enumerations-Klasse hält die Icons für Toolbars und Menüs vor.
 * @author Alexander Herzog
 */
public enum Images {
	/* Allgemeine Icons */

	/** Symbol "Drucken" */
	GENERAL_PRINT("printer.png"),

	/** Symbol "Einstellungen" (Programmsetup) */
	GENERAL_SETUP("wrench.png"),

	/** Symbol "Speichern" */
	GENERAL_LOAD("folder_page_white.png"),

	/** Symbol "Speichern" */
	GENERAL_SAVE("disk.png"),

	/** Symbol "Ende" */
	GENERAL_EXIT("door_in.png"),

	/** Symbol "Papierkorb" */
	GENERAL_TRASH("bin_closed.png"),

	/** Symbol "Zahlen" */
	GENERAL_NUMBERS("Counter.png"),

	/** Symbol "Prozent" */
	GENERAL_PERCENT("percent.gif"),

	/** Symbol "Suchen" */
	GENERAL_FIND("find.png"),

	/** Symbol "Suchen - nach ID" */
	GENERAL_FIND_BY_ID("Counter.png"),

	/** Symbol "Suchen - nach Name" */
	GENERAL_FIND_BY_NAME("font.png"),

	/** Symbol "Suchen - über alles" */
	GENERAL_FIND_BY_ALL("find.png"),

	/** Symbol "Anwendung" (für Zugriff auf Anwendungsfunktionen über die Quick-Access-Funktion */
	GENERAL_APPLICATION("application.png"),

	/** Symbol "Information" */
	GENERAL_INFO("information.png"),

	/** Symbol "Fehler" */
	GENERAL_WARNING("error.png"),

	/** Symbol "Fehler" bzw. "Fehlerhafte Einstellungen" (für das Quick-Fix-Popupmenü) */
	GENERAL_WARNING_BUG("bug_error.png"),

	/** Symbol "Abbruch" */
	GENERAL_CANCEL("cancel.png"),

	/** Symbol "Tools" */
	GENERAL_TOOLS("cog.png"),

	/** Symbol "Datei auswählen" */
	GENERAL_SELECT_FILE("folder_page_white.png"),

	/** Symbol "Tabelle in Datei auswählen" */
	GENERAL_SELECT_TABLE_IN_FILE("page_excel.png"),

	/** Symbol "Verzeichnis auswählen" */
	GENERAL_SELECT_FOLDER("folder.png"),

	/** Symbol "Aus" */
	GENERAL_OFF("cross.png"),

	/** Symbol "An" */
	GENERAL_ON("accept.png"),

	/** Symbol "Zeit" */
	GENERAL_TIME("time.png"),

	/** Symbol "Script" */
	GENERAL_SCRIPT("page_white_code_red.png"),

	/** Symbol "Startseite" */
	GENERAL_HOME("house.png"),

	/** Symbol "Warten" (animiert) */
	GENERAL_WAIT_INDICATOR("ajax-loader.gif"),

	/** Symbol "Wert erhöhen" */
	GENERAL_INCREASE("Plain_Plus.png"),

	/** Symbol "Wert verringern" */
	GENERAL_DECREASE("Plain_Minus.png"),

	/** Symbol "Versionsgeschichte" */
	GENERAL_CHANGELOG("calendar.png"),

	/** Symbol "Lizenzen" */
	GENERAL_LICENSE("key.png"),

	/** Symbol "Schloss offen" (z.B. kein Schreibschutz) */
	GENERAL_LOCK_OPEN("lock_open.png"),

	/** Symbol "Schloss geschlossen" (z.B. Schreibschutz) */
	GENERAL_LOCK_CLOSED("lock.png"),

	/** Symbol "Schriftart" */
	GENERAL_FONT("font.png"),

	/** Symbol "Bearbeiten" */
	GENERAL_EDIT("pencil.png"),

	/** Symbol "Tabelle" */
	GENERAL_TABLE("Table.png"),

	/** Symbol "Aktualisieren" */
	GENERAL_UPDATE("arrow_refresh.png"),

	/** Symbol "Abspielen starten" */
	GENERAL_PLAY("control_play_blue.png"),

	/** Symbol "Maus" */
	GENERAL_MOUSE("mouse.png"),

	/** Sound-Typ-Auswahl "Systemereignis-Sounds" */
	SOUND_EVENT("flag_red.png"),

	/** Sound-Typ-Auswahl "Windows-Sounds" */
	SOUND_WINDOWS("windows.png"),

	/** Sound-Typ-Auswahl "Sound-Dateien" */
	SOUND_FILE("folder_page_white.png"),

	/** Symbol "Rechtschreibprüfung" */
	SPELL_CHECK("spellcheck.png"),

	/** Symbol "Wörterbücher" */
	SPELL_CHECK_DICTIONARIES("book.png"),

	/** Symbol "Benutzerdefinierte Wörter" */
	SPELL_CHECK_USER_WORDS("user.png"),

	/** Symbol "Zu prüfende Eingaben" */
	SPELL_CHECK_ELEMENTS("find.png"),

	/** Symbol "Dialog-Button 'Ok'" */
	MSGBOX_OK("accept.png"),

	/** Symbol "Dialog-Button 'Ja'" */
	MSGBOX_YES("tick.png"),

	/** Symbol "Dialog-Button 'Ja, speichern'" */
	MSGBOX_YES_SAVE("disk.png"),

	/** Symbol "Dialog-Button 'Nein'" */
	MSGBOX_NO("cancel.png"),

	/** Symbol "Dialog-Button 'Abbruch/Zurück'" */
	MSGBOX_CANCEL("arrow_redo2.png"),

	/** Symbol für Definitionsmodus "über Verteilung" */
	MODE_DISTRIBUTION("chart_curve.png"),

	/** Symbol für Definitionsmodus "über Ausdruck" */
	MODE_EXPRESSION("fx.png"),

	/** Symbol "Nach unten" */
	ARROW_DOWN("arrow_down.png"),

	/** Symbol "Nach unten ans Ende" */
	ARROW_DOWN_END("arrow_down_double.png"),

	/** Symbol "Nach links" */
	ARROW_LEFT("arrow_left.png"),

	/** Symbol "Nach rechts" */
	ARROW_RIGHT("arrow_right.png"),

	/** Symbol "Nach oben" */
	ARROW_UP("arrow_up.png"),

	/** Symbol "2x nach oben" */
	ARROW_UP_DOUBLE("arrow_up_double.png"),

	/** Symbol "2x nach unten" */
	ARROW_DOWN_DOUBLE("arrow_down_double.png"),

	/** Symbol "Nach links" (kurzer Pfeil) */
	ARROW_LEFT_SHORT("arrow_left.gif"),

	/** Symbol "Nach rechts" (kurzer Pfeil) */
	ARROW_RIGHT_SHORT("arrow_right.gif"),

	/** Symbol "Pfeilgröße 'aus'" */
	ARROW_SIZE_OFF("arrow_size_off.png"),

	/** Symbol "Pfeilgröße 'klein'" */
	ARROW_SIZE_SMALL("arrow_size_small.png"),

	/** Symbol "Pfeilgröße 'mittel'" */
	ARROW_SIZE_MEDIUM("arrow_size_medium.png"),

	/** Symbol "Pfeilgröße 'groß'" */
	ARROW_SIZE_LARGE("arrow_size_large.png"),

	/** Symbol "Pfeile austauschen" */
	ARROW_SWITCH("arrow_switch.png"),

	/** Symbol "Text - linksbündig" */
	TEXT_ALIGN_LEFT("text_align_left.png"),

	/** Symbol "Text - zentriert" */
	TEXT_ALIGN_CENTER("text_align_center.png"),

	/** Symbol "Text - rechtsbündig" */
	TEXT_ALIGN_RIGHT("text_align_right.png"),

	/** Symbol "Git" */
	GIT("sitemap_color.png"),

	/** Symbol "Authentifizierung - Aus" */
	AUTH_OFF("cross.png"),

	/** Symbol "Authentifizierung - Name &amp; Passwort" */
	AUTH_USER("user.png"),

	/** Symbol "Authentifizierung - Schlüssel" */
	AUTH_KEY("key.png"),

	/* Bearbeiten */

	/** Symbol "Bearbeiten - Rückgängig" */
	EDIT_UNDO("arrow_undo.png"),

	/** Symbol "Bearbeiten - Wiederholen" */
	EDIT_REDO("arrow_redo.png"),

	/** Symbol "Bearbeiten - Kopieren" */
	EDIT_COPY("page_copy.png"),

	/** Symbol "Bearbeiten - Kopieren (als Bild) */
	EDIT_COPY_AS_IMAGE("image.png"),

	/** Symbol "Bearbeiten - Ausschneiden" */
	EDIT_CUT("cut.gif"),

	/** Symbol "Bearbeiten - Einfügen" */
	EDIT_PASTE("paste_plain.png"),

	/** Symbol "Bearbeiten - Hinzufügen" */
	EDIT_ADD("add.png"),

	/** Symbol "Bearbeiten - Löschen" */
	EDIT_DELETE("delete.png"),

	/** Symbol "Ebenen" */
	EDIT_LAYERS("layers.png"),

	/** Symbol "Ebene - sichtbar" */
	EDIT_LAYERS_VISIBLE("lightbulb.png"),

	/** Symbol "Ebene - unsichtbar" */
	EDIT_LAYERS_INVISIBLE("lightbulb_off.png"),

	/** Symbol "Hintergrundfarbe" */
	EDIT_BACKGROUND_COLOR("color_wheel.png"),

	/** Symbol "Hintergrundbild" */
	EDIT_BACKGROUND_IMAGE("image.png"),

	/** Symbol "Verbindungskante" */
	EDIT_EDGES("link.png"),

	/** Symbol "Verbindungskante hinzufügen" */
	EDIT_EDGES_ADD("link_add.png"),

	/** Symbol "Verbindungskante hinzufügen - Panel schließen" */
	EDIT_EDGES_ADD_CLOSEPANEL("application_side_contract2.png"),

	/** Symbol "Verbindungskante entfernen" */
	EDIT_EDGES_DELETE("link_delete.png"),

	/** Symbol für "Raster anzeigen" */
	EDIT_VIEW_RASTER("Raster.png"),

	/** Symbol für "Lineale anzeigen" */
	EDIT_VIEW_RULERS("Ruler.png"),

	/** Symbol "Vertikal ausrichten - Oberkante */
	ALIGN_TOP("shape_align_top.png"),

	/** Symbol "Vertikal ausrichten - Mitte */
	ALIGN_MIDDLE("shape_align_middle.png"),

	/** Symbol "Vertikal ausrichten - Unterkante */
	ALIGN_BOTTOM("shape_align_bottom.png"),

	/** Symbol "Horizontal ausrichten - Linke Kante */
	ALIGN_LEFT("shape_align_left.png"),

	/** Symbol "Horizontal ausrichten - mittig */
	ALIGN_CENTER("shape_align_center.png"),

	/** Symbol "Horizontal ausrichten - Rechte Kante */
	ALIGN_RIGHT("shape_align_right.png"),

	/** Symbol "Anordnen - ganz nach vorne" */
	MOVE_FRONT("shape_move_front.png"),

	/** Symbol "Anordnen - eine Ebene nach vorne" */
	MOVE_FRONT_STEP("shape_move_forwards.png"),

	/** Symbol "Anordnen - eine Ebene nach hinten" */
	MOVE_BACK_STEP("shape_move_backwards.png"),

	/** Symbol "Anordnen - ganz nach hinten" */
	MOVE_BACK("shape_move_back.png"),

	/** Symbol "Zoom" (allgemein) */
	ZOOM("zoom.png"),

	/** Symbol "Hinein zoomen" */
	ZOOM_IN("zoom_in.png"),

	/** Symbol "Heraus zoomen" */
	ZOOM_OUT("zoom_out.png"),

	/** Symbol "Auf Modell zoomen" */
	ZOOM_CENTER_MODEL("arrow_out.png"),

	/** Symbol "Gespeicherte Ansicht" */
	ZOOM_VIEW("application.png"),

	/** Symbol "Gespeicherte Ansichten" */
	ZOOM_VIEWS("application_cascade.png"),

	/** Symbol "Modellüberblick" */
	MODE_OVERVIEW("find.png"),

	/** Symbol "Statistikinformationen im Modell-Editor" */
	STATISTIC_INFO("chart_bar.png"),

	/** Symbol "Heatmap" */
	HEATMAP("palette.png"),

	/* Expression Builder */

	/** Symbol "Expression-Builder" */
	EXPRESSION_BUILDER("wand.png"),

	/** Symbol im Expression-Builder "Konstante" */
	EXPRESSION_BUILDER_CONST("text_letter_omega.png"),

	/** Symbol im Expression-Builder "Variable" */
	EXPRESSION_BUILDER_VARIABLE("font.png"),

	/** Symbol im Expression-Builder "Funktion" */
	EXPRESSION_BUILDER_FUNCTION("fx.png"),

	/** Symbol im Expression-Builder "Nutzerdefinierte Funktion" */
	EXPRESSION_BUILDER_USER_FUNCTION("user.png"),

	/** Symbol im Expression-Builder "Verteilung" */
	EXPRESSION_BUILDER_DISTRIBUTION("chart_curve.png"),

	/** Symbol im Expression-Builder "Simulationsdaten" */
	EXPRESSION_BUILDER_SIMDATA("action_go.gif"),

	/** Symbol im Expression-Builder "Stations-ID aus Name bestimmen" */
	EXPRESSION_BUILDER_STATION_ID("station.png"),

	/** Symbol im Expression-Builder "Daten aus Scripting-Zuordnung abfragen" */
	EXPRESSION_BUILDER_GLOBAL_MAP("page_white_code_red.png"),

	/** Symbol im Expression-Builder "Kundenspezifische Simulationsdaten" */
	EXPRESSION_BUILDER_CLIENT_DATA("user.png"),

	/* Achsenbeschriftung */

	/** Keine Achsenbeschriftung */
	AXIS_OFF("cross.png"),

	/** Nur Minimum und Maximum anzeigen */
	AXIS_MIN_MAX("Bar.png"),

	/** Mehrere Werte an der Achse anzeigen */
	AXIS_FULL("Counter.png"),

	/* Modell */

	/** Symbol "Modell" */
	MODEL("brick.png"),

	/** Symbol "Modell - Neu" */
	MODEL_NEW("brick_add.png"),

	/** Symbol "Modell - Neu mit Generator" */
	MODEL_GENERATOR("wand.png"),

	/** Symbol "Modell - Laden" */
	MODEL_LOAD("brick_go.png"),

	/** Symbol "Modell - Speichern" */
	MODEL_SAVE("disk.png"),

	/** Symbol "Modell - Elementenliste" */
	MODEL_LIST_ELEMENTS("text_list_numbers.png"),

	/** Symbol "Modell - Beschreibung" */
	MODEL_DESCRIPTION("page_gear.png"),

	/** Symbol "Modell - Notizen" */
	MODEL_NOTES("note.png"),

	/** Symbol "Modell - Vergleich mit analytischem Nodell" */
	MODEL_ANALYTIC_COMPARE("fx.png"),

	/** Symbol "Modell - Vorlagen" */
	MODEL_TEMPLATES("pictures.png"),

	/** Symbol "Vergleichen - mehrere Statistikdaten" */
	MODEL_COMPARE("application_tile_horizontal.png"),

	/** Symbol "Vergleichen - Modell festhalten" */
	MODEL_COMPARE_KEEP("basket_put.png"),

	/** Symbol "Vergleichen - festgehaltenes und aktuelles Modell vergleichen" */
	MODEL_COMPARE_COMPARE("basket_go.png"),

	/** Symbol "Vergleichen - Zu festgehaltenem Modell zurückkehren" */
	MODEL_COMPARE_GO_BACK("basket_remove.png"),

	/** Symbol "Modell - Hinzufügen (aus Zwischenablage) - Station" */
	MODEL_ADD_STATION("station.png"),

	/** Symbol "Modell - Hinzufügen (aus Zwischenablage) - Bild" */
	MODEL_ADD_IMAGE("image.png"),

	/** Symbol "Modell - Hinzufügen (aus Zwischenablage) - Text" */
	MODEL_ADD_TEXT("Text.png"),

	/** Symbol "Modell - Plugins-Verzeichnis" */
	MODEL_PLUGINS("plugin.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung'" */
	MODELPROPERTIES_DESCRIPTION("brick_edit.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung' - Titel auf Zeichenfläche einfügen" */
	MODELPROPERTIES_DESCRIPTION_ADD_TO_SURFACE("brick_go.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung' - Autor einstellen" */
	MODELPROPERTIES_DESCRIPTION_SET_AUTHOR("user.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung' - automatisch erstellen" */
	MODELPROPERTIES_DESCRIPTION_AUTO_CREATE("page_gear.png"),

	/** Symbol "Modelleigenschaften - Seite 'Simulation'" */
	MODELPROPERTIES_SIMULATION("action_go.gif"),

	/** Symbol "Modelleigenschaften - Seite 'Statistik'" */
	MODELPROPERTIES_STATISTICS("sum.png"),

	/** Symbol "Modelleigenschaften - Seite 'Simulation' - Startwert für Zufallszahlengenerator festlegen" */
	MODELPROPERTIES_SIMULATION_RANDOM_SEED("calculator.png"),

	/** Symbol "Modelleigenschaften - Seite 'Kunden'" */
	MODELPROPERTIES_CLIENTS("user.png"),

	/** Symbol "Modelleigenschaften - Seite 'Kunden' - Kundengruppe" */
	MODELPROPERTIES_CLIENTS_GROUPS("group.png"),

	/** Symbol "Modelleigenschaften - Seite 'Kunden' - Icon für Kundengruppe" */
	MODELPROPERTIES_CLIENTS_ICON("film_go.png"),

	/** Symbol "Modelleigenschaften - Seite 'Kunden' - Farbe für Kundengruppe" */
	MODELPROPERTIES_CLIENTS_COLOR("color_wheel.png"),

	/** Symbol "Modelleigenschaften - Seite 'Kunden' - Kosten für Kundengruppe" */
	MODELPROPERTIES_CLIENTS_COSTS("money_euro.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener'" */
	MODELPROPERTIES_OPERATORS("group.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - Gruppe hinzufügen" */
	MODELPROPERTIES_OPERATORS_ADD("group_add.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - Gruppe löschen" */
	MODELPROPERTIES_OPERATORS_DELETE("group_delete.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - Kosten für Gruppe" */
	MODELPROPERTIES_OPERATORS_COSTS("money_euro.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - Ausfälle" */
	MODELPROPERTIES_OPERATORS_FAILURES("group_error.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - Rüstzeiten" */
	MODELPROPERTIES_OPERATORS_SETUP("chart_curve.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - nachgelagerte Priorität 'zufällig'" */
	MODELPROPERTIES_PRIORITIES_RANDOM("arrow_switch.png"),

	/** Symbol "Modelleigenschaften - Seite 'Bediener' - nachgelagerte Priorität 'Kundenpriorität'" */
	MODELPROPERTIES_PRIORITIES_CLIENT("user.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter'" */
	MODELPROPERTIES_TRANSPORTERS("lorry.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Gruppe hinzufügen" */
	MODELPROPERTIES_TRANSPORTERS_ADD("lorry_add.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Ausfälle" */
	MODELPROPERTIES_TRANSPORTERS_FAILURE("lorry_error.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Anzahl" */
	MODELPROPERTIES_TRANSPORTERS_COUNT("Counter.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Distanzen" */
	MODELPROPERTIES_TRANSPORTERS_DISTANCES("Dispose.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Distanzen - Tabellen-Werkzeuge" */
	MODELPROPERTIES_TRANSPORTERS_DISTANCES_TABLE_TOOLS("Table.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Beladezeiten" */
	MODELPROPERTIES_TRANSPORTERS_LOADING_TIMES("package_go.png"),

	/** Symbol "Modelleigenschaften - Seite 'Transporter' - Entladezeiten" */
	MODELPROPERTIES_TRANSPORTERS_UNLOADING_TIMES("package_go_left.png"),

	/** Symbol "Modelleigenschaften - Seite 'Zeitpläne'" */
	MODELPROPERTIES_SCHEDULES("time.png"),

	/** Symbol "Modelleigenschaften - Seite 'Zeitpläne' - hinzufügen" */
	MODELPROPERTIES_SCHEDULES_ADD("time_add.png"),

	/** Symbol "Modelleigenschaften - Seite 'Fertigungspläne'" */
	MODELPROPERTIES_SEQUENCES("text_list_numbers.png"),

	/** Symbol "Modelleigenschaften - Seite 'Fertigungspläne' - Kundenvariable zuweisen" */
	MODELPROPERTIES_SEQUENCES_ASSIGNMENT("user.png"),

	/** Symbol "Modelleigenschaften - Seite 'Initiale Variablenwerte'" */
	MODELPROPERTIES_INITIAL_VALUES("font.png"),

	/** Symbol "Modelleigenschaften - Seite 'Laufzeitstatistik'" */
	MODELPROPERTIES_RUNTIME_STATISTICS("chart_curve_add.png"),

	/** Symbol "Modelleigenschaften - Seite 'Ausgabeanalyse'" */
	MODELPROPERTIES_OUTPUT_ANALYSIS("chart_curve.png"),

	/** Symbol "Modelleigenschaften - Seite 'Pfadaufzeichnung'" */
	MODELPROPERTIES_PATH_RECORDING("Vertex.png"),

	/** Symbol "Modelleigenschaften - Seite 'Ausgabeanalyse' - Autokorrelation schnell" */
	MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FAST("user.png"),

	/** Symbol "Modelleigenschaften - Seite 'Ausgabeanalyse' - Autokorrelation vollständig" */
	MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FULL("group.png"),

	/** Symbol "Modelleigenschaften - Seite 'Simulationssystem'" */
	MODELPROPERTIES_INFO("computer.png"),

	/* Statistik */

	/** Symbol "Statistik" */
	STATISTICS("sum.png"),

	/** Symbol "Statistik" (dunkler) */
	STATISTICS_DARK("sum2.png"),

	/** Symbol "Statistik - laden" */
	STATISTICS_LOAD("icon_package_open.gif"),

	/** Symbol "Statistik - speichern" */
	STATISTICS_SAVE("icon_package_get.gif"),

	/** Symbol "Statistik - Modell in Editor laden */
	STATISTICS_SHOW_MODEL("brick.png"),

	/** Symbol "Animation - Pause" */
	STATISTICS_ANIMATION_PAUSE("Pause.png"),

	/** Symbol "Animation - Play" */
	STATISTICS_ANIMATION_PLAY("Play.png"),

	/** Symbol in der Statistik "Sankey" */
	STATISTICS_DIAGRAM_SANKEY("chart_organisation.png"),

	/* Simulation */

	/** Symbol "Simulation - Start" */
	SIMULATION("action_go.gif"),

	/** Symbol "Simulation - In Logdatei aufzeichnen" */
	SIMULATION_LOG("Text.png"),

	/** Symbol "Simulation - Statistikaufzeichnung konfigurieren" */
	SIMULATION_STATISTICS("chart_curve.png"),

	/** Symbol "Simulation - Statistikaufzeichnung konfigurieren" - "Statistikerfassung" */
	SIMULATION_STATISTICS_STATISTICS("Check.png"),

	/** Symbol "Simulation - Statistikaufzeichnung konfigurieren" - "Intervalllänge für Maximaldurchsatz" */
	SIMULATION_STATISTICS_MAXTHROUGHPUT("Process.png"),

	/** Symbol "Simulation - In Logdatei aufzeichnen - als Text" */
	SIMULATION_LOG_MODE_FILE("page.png"),

	/** Symbol "Simulation - In Logdatei aufzeichnen - als Tabelle" */
	SIMULATION_LOG_MODE_TABLE("page_excel.png"),

	/** Symbol "Simulation - Modell prüfen" */
	SIMULATION_CHECK("accept.png"),

	/** Symbol "Simulation - Datenbank prüfen" */
	SIMULATION_CHECK_DATABASE("database.png"),

	/* Animation */

	/** Symbol "Animation starten" */
	ANIMATION("film_go.png"),

	/** Symbol "Animation aus Video aufzeichnen" */
	ANIMATION_RECORD("stop.png"),

	/** Symbol "Animation - Screenshot aufnehmen" */
	ANIMATION_SCREENSHOT("image.png"),

	/** Symbol "Animation - Export" */
	ANIMATION_EXPORT("disk.png"),

	/** Symbol "Animation in Logdatei aufzeichnen" */
	ANIMATION_LOG("Text.png"),

	/** Symbol "Animation - Steuerung 'Play'" */
	ANIMATION_PLAY("control_play_blue.png"),

	/** Symbol "Animation - Steuerung 'Pause'" */
	ANIMATION_PAUSE("control_pause_blue.png"),

	/** Symbol "Animation - Steuerung 'Schritt'" */
	ANIMATION_STEP("control_end_blue.png"),

	/** Symbol "Animation - Steuerung 'Geschwindigkeit'" */
	ANIMATION_SPEED("control_fastforward_blue.png"),

	/** Symbol "Animation - Ausdruck auswerten" */
	ANIMATION_EVALUATE_EXPRESSION("fx.png"),

	/** Symbol "Animation - Skript auswerten" */
	ANIMATION_EVALUATE_SCRIPT("page_white_code_red.png"),

	/** Symbol "Animation - Liste der nächsten Ereignisse" */
	ANIMATION_LIST_NEXT_EVENTS("text_list_numbers.png"),

	/** Symbol "Animation - Stationsdaten anzeigen - Update" */
	ANIMATION_DATA_UPDATE("arrow_refresh.png"),

	/** Symbol "Animation - Stationsdaten anzeigen - Auto-Update" */
	ANIMATION_DATA_UPDATE_AUTO("action_go.gif"),

	/** Symbol "Animation - Haltepunkte" */
	ANIMATION_BREAKPOINTS("control_pause_blue.png"),

	/** Symbol "Animation - Haltepunkte/Pause-Stationen" */
	ANIMATION_BREAKPOINTS_PAUSE_STATIONS("station.png"),

	/* Parameterreihe */

	/** Symbol "Parameterreihe" */
	PARAMETERSERIES("table_gear.png"),

	/** Symbol "Parameterreihe (Varianzanalyse)" */
	PARAMETERSERIES_VARIANCE("chart_curve_error.png"),

	/** Symbol "Parameterreihe - Eingabemodus 'Ressource ändern' */
	PARAMETERSERIES_INPUT_MODE_RESOURCE("group.png"),

	/** Symbol "Parameterreihe - Eingabemodus 'Variablenwert ändern' */
	PARAMETERSERIES_INPUT_MODE_VARIABLE("font.png"),

	/** Symbol "Parameterreihe - Eingabemodus 'XML-Element ändern' */
	PARAMETERSERIES_INPUT_MODE_XML("brick.png"),

	/** Symbol "Parameterreihe - Konfiguration - neu" */
	PARAMETERSERIES_SETUP_NEW("page_add.png"),

	/** Symbol "Parameterreihe - Konfiguration - laden" */
	PARAMETERSERIES_SETUP_LOAD("folder_page_white.png"),

	/** Symbol "Parameterreihe - Konfiguration - speichern" */
	PARAMETERSERIES_SETUP_SAVE("disk.png"),

	/** Symbol "Parameterreihe - Konfiguration - Vorlagen" */
	PARAMETERSERIES_SETUP_TEMPLATES("wand.png"),

	/** Symbol "Parameterreihe - Konfiguration - Ausgangsmodell" */
	PARAMETERSERIES_SETUP_SHOW_BASE_MODEL("brick.png"),

	/** Symbol "Parameterreihe - Konfiguration - Ausgangsmodell in Editor laden" */
	PARAMETERSERIES_SETUP_SHOW_BASE_MODEL_LOAD_TO_EDITOR("brick_go.png"),

	/** Symbol "Parameterreihe - Konfiguration - Eingabeparameter" */
	PARAMETERSERIES_SETUP_INPUT("brick_edit.png"),

	/** Symbol "Parameterreihe - Konfiguration - Ausgabeparameter" */
	PARAMETERSERIES_SETUP_OUTPUT("sum.png"),

	/** Symbol "Parameterreihe - Start" */
	PARAMETERSERIES_RUN("action_go.gif"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten" */
	PARAMETERSERIES_PROCESS_RESULTS("chart_curve.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Ergebnisse löschen" */
	PARAMETERSERIES_PROCESS_RESULTS_CLEAR("delete.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Ergebnisse vergleichen" */
	PARAMETERSERIES_PROCESS_RESULTS_COMPARE("application_tile_horizontal.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Skript anwenden" */
	PARAMETERSERIES_PROCESS_RESULTS_SCRIPT("lightning_go.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Diagramme" */
	PARAMETERSERIES_PROCESS_RESULTS_CHARTS("chart_curve.png"),

	/** Symbol "Parameterreihe - Eingabeparameter verbinden */
	PARAMETERSERIES_CONNECT_INPUT("brick_edit.png"),

	/** Symbol "Parameterreihe - XML-Element wählen */
	PARAMETERSERIES_SELECT_XML("add.png"),

	/** Symbol "Parameterreihe - Einträge per Assistent hinzufügen */
	PARAMETERSERIES_ADD_BY_ASSISTANT("wand.png"),

	/** Symbol "Parameterreihe - Einträge sortieren */
	PARAMETERSERIES_SORT_TABLE("arrow_refresh.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - XML-Element */
	PARAMETERSERIES_OUTPUT_MODE_XML("add.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - Javascript Rückgabe */
	PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVASCRIPT("page_white_code_red.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - Java Rückgabe */
	PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVA("page_white_cup.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - Rechenausdruck */
	PARAMETERSERIES_OUTPUT_MODE_COMMAND("fx.png"),

	/** Symbol "Parameterreihe - Vorlage - Zwischenankunftszeit variieren */
	PARAMETERSERIES_TEMPLATE_MODE_INTERARRIVAL("chart_curve.png"),

	/** Symbol "Parameterreihe - Vorlage - Bedieneranzahl variieren */
	PARAMETERSERIES_TEMPLATE_MODE_OPERATORS("group.png"),

	/** Symbol "Parameterreihe - Vorlage - Bedienzeiten variieren */
	PARAMETERSERIES_TEMPLATE_MODE_SERVICETIMES("chart_curve.png"),

	/** Symbol "Parameterreihe - Vorlage - Wartezeittoleranzen variieren */
	PARAMETERSERIES_TEMPLATE_MODE_WAITINGTIME_TOLERANCES("cancel.png"),

	/** Symbol "Parameterreihe - Vorlage - Variablenwert variieren */
	PARAMETERSERIES_TEMPLATE_MODE_VARIABLES("font.png"),

	/** Symbol "Parameterreihe - Vorlage - Verzögerungszeit variieren */
	PARAMETERSERIES_TEMPLATE_MODE_DELAY("chart_curve.png"),

	/** Symbol "Parameterreihe - Vorlage - Analogwert variieren */
	PARAMETERSERIES_TEMPLATE_MODE_ANALOG("Bar.png"),

	/** Symbol "Parameterreihe - Vorlage - Fließband-Transportzeit variieren */
	PARAMETERSERIES_TEMPLATE_MODE_CONVEYOR("Conveyor.png"),

	/** Symbol "Parameterreihe - Vorlage - Batch-Größe variieren */
	PARAMETERSERIES_TEMPLATE_MODE_BATCHSIZE("Batch.png"),

	/** Symbol "Parameterreihe - Vorlage - Anzahl an Ankünften variieren */
	PARAMETERSERIES_TEMPLATE_MODE_ARRIVAL_COUNT("Counter.png"),

	/** Symbol "Parameterreihe - Model aktiv - Ja" */
	PARAMETERSERIES_MODEL_ACTIVE_YES("lightbulb.png"),

	/** Symbol "Parameterreihe - Model aktiv - Nein" */
	PARAMETERSERIES_MODEL_ACTIVE_NO("lightbulb_off.png"),


	/* Optimierer */

	/** Symbol "Optimierer" */
	OPTIMIZER("chart_bar.png"),

	/** Symbol "Optimierer - Kernel wählen" */
	OPTIMIZER_KERNEL("cog.png"),

	/** Symbol "Optimierer - Speichermodus - alle" */
	OPTIMIZER_SAVE_MODE_ALL("application_double.png"),

	/** Symbol "Optimierer - Speichermodus - letztes Modell" */
	OPTIMIZER_SAVE_MODE_LAST("sum.png"),

	/** Symbol "Optimierer - Einstellungen - neu" */
	OPTIMIZER_SETUP_NEW("page_add.png"),

	/** Symbol "Optimierer - Einstellungen - laden" */
	OPTIMIZER_SETUP_LOAD("folder_page_white.png"),

	/** Symbol "Optimierer - Einstellungen - speichern" */
	OPTIMIZER_SETUP_SAVE("disk.png"),

	/** Symbol "Optimierer - Starten" */
	OPTIMIZER_RUN("action_go.gif"),

	/** Symbol "Optimierer - Nebenbedingungen" */
	OPTIMIZER_CONSTRAIN("fx.png"),

	/** Symbol "Optimierer - XML-Element wählen" */
	OPTIMIZER_SELECT_XML("add.png"),

	/** Symbol "Optimierer - Seite 'Kontrollvariable'" */
	OPTIMIZER_PAGE_CONTROL_VARIABLE("cog.png"),

	/** Symbol "Optimierer - Seite 'Ziel'" */
	OPTIMIZER_PAGE_TARGET("chart_bar.png"),

	/** Symbol "Optimierer - Seite 'Optimierung'" */
	OPTIMIZER_PAGE_OPTIMIZATION("action_go.gif"),

	/** Symbol "Optimierer - Ergebnisdiagramm exportieren - als Text" */
	OPTIMIZER_EXPORT_TEXT("Text.png"),

	/** Symbol "Optimierer - Ergebnistabelle exportieren" */
	OPTIMIZER_EXPORT_TABLE("Table.png"),

	/** Symbol "Optimierer - Ergebnisdiagramm exportieren - als Grafik" */
	OPTIMIZER_EXPORT_CHART("chart_curve.png"),

	/* Weitere Programmfunktionen */

	/** Symbol "Element-Vorlagen-Leiste" */
	ELEMENTTEMPLATES("add.png"),

	/** Symbol "Element-Vorlagen-Leiste - Filtern" */
	ELEMENTTEMPLATES_FILTER("text_list_bullets.png"),

	/** Symbol "Element-Vorlagen-Leiste - Gruppe schließen (Minus)" */
	ELEMENTTEMPLATES_GROUP_CLOSE("SmallMinus2.png"),

	/** Symbol "Element-Vorlagen-Leiste - Gruppe öffnen (Plus)" */
	ELEMENTTEMPLATES_GROUP_OPEN("SmallPlus2.png"),

	/** Symbol "Element-Vorlagen-Leiste - Seitenleiste ausblenden" */
	ELEMENTTEMPLATES_CLOSEPANEL("application_side_contract.png"),

	/** Symbol "Modell-Navigator" */
	NAVIGATOR("Navigator.png"),

	/** Symbol "Modell-Navigator - Seitenleiste ausblenden" */
	NAVIGATOR_CLOSEPANEL("application_side_contract2.png"),

	/* Extras */

	/** Symbol "Rechner" */
	EXTRAS_CALCULATOR("calculator.png"),

	/** Symbol "Rechner - Funktionsplotter" */
	EXTRAS_CALCULATOR_PLOTTER("chart_curve.png"),

	/** Symbol "Rechner - Wahrscheinlichkeitsverteilungen" */
	EXTRAS_CALCULATOR_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Rechner - Skript" */
	EXTRAS_CALCULATOR_SCRIPT("page_white_code_red.png"),

	/** Symbol "Rechner - Funktionsplotter - Funktion löschen" */
	EXTRAS_CALCULATOR_PLOTTER_CLEAR("chart_curve_delete.png"),

	/** Symbol "Warteschlangenrechner (Tab-Icons)" */
	EXTRAS_QUEUE_FUNCTION("fx.png"),

	/** Symbol "Warteschlangenrechner" */
	EXTRAS_QUEUE("Symbol.png"),

	/** Symbol "Extras - Nutzerdefinierte Funktionen" */
	EXTRAS_USER_FUNCTIONS("fx.png"),

	/** Symbol "Verteilung anpassen" */
	EXTRAS_FIT_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Datenbankverbindung testen" */
	EXTRAS_DATABASE_TEST("database_connect.png"),

	/** Symbol "Stapelverarbeitung" */
	EXTRAS_BATCH_PROCESSING("application_cascade.png"),

	/** Symbol "Kommandozeile" */
	EXTRAS_COMMANDLINE("application_xp_terminal.png"),

	/** Symbol "Server" */
	EXTRAS_SERVER("server.png"),

	/** Symbol "Systeminformationen" */
	EXTRAS_SYSTEM_INFO("monitor.png"),

	/* Hilfe */

	/** Symbol "Hilfe" */
	HELP("help.png"),

	/** Symbol "Hilfeinhalt" */
	HELP_CONTENT("book_open.png"),

	/** Symbol "Tutorial (pdf)" */
	HELP_TUTORIAL("book.png"),

	/** Symbol "Tutorial Video" */
	HELP_TUTORIAL_VIDEO("film.png"),

	/** Symbol "Interaktives Tutorial" */
	HELP_TUTORIAL_INTERACTIVE("cursor.png"),

	/** Symbol "Interaktive Stationsbeschreibungen" */
	HELP_STATIONS_INTERACTIVE("station.png"),

	/** Symbol "Skripting-Beschreibung (pdf)" */
	HELP_SCRIPTING("page_white_code_red.png"),

	/** Symbol für Untereinträge von "Empfohlene Literatur" */
	HELP_BOOK("book.png"),

	/** Symbol für Untereinträge von "Inhaltsverzeichnis" */
	HELP_BOOK_CONTENT("book_open.png"),

	/** Symbol für Untereinträge von "Sachverzeichnis" */
	HELP_BOOK_INDEX("book_key.png"),

	/** Symbol "E-Mail" */
	HELP_EMAIL("email.png"),

	/** Symbol "Problemreport erstellen" */
	HELP_BUG_REPORT("bug.png"),

	/** Symbol "Homepage" */
	HELP_HOMEPAGE("world.png"),

	/** Symbol "Tastenkombinationsreferenz (pdf)" */
	HELP_HOTKEY_REFERENCE("keyboard.png"),

	/** Symbol "Datenschutz" */
	HELP_DATA_PRIVACY("lock.png"),

	/* Bilder */

	/** Symbol "Bild - Laden" */
	IMAGE_LOAD("image_add.png"),

	/** Symbol "Bild - Speichern" */
	IMAGE_SAVE("disk.png"),

	/** Symbol "Bild - Vorlagebilder" */
	IMAGE_TEMPLATE("folder_page_white.png"),

	/* Sprache */

	/** Symbol "Sprache - Englisch" */
	LANGUAGE_EN("flag_gb.png"),

	/** Symbol "Sprache - Deutsch" */
	LANGUAGE_DE("flag_de.png"),

	/* Server */

	/** Symbol "Rechenserver" */
	SERVER_CALC("server.png"),

	/** Symbol "Rechenserver - starten" */
	SERVER_CALC_START("server_add.png"),

	/** Symbol "Rechenserver - stoppen" */
	SERVER_CALC_STOP("server_delete.png"),

	/** Symbol "Webserver" */
	SERVER_CALC_WEB("world.png"),

	/** Symbol "Webserver - starten" */
	SERVER_CALC_WEB_START("world_add.png"),

	/** Symbol "Webserver - stoppen" */
	SERVER_CALC_WEB_STOP("world_delete.png"),

	/** Symbol "Fernsteuerungsserver" */
	SERVER_WEB("film.png"),

	/** Symbol "Fernsteuerungsserver - starten" */
	SERVER_WEB_START("film_add.png"),

	/** Symbol "Fernsteuerungsserver - stoppen" */
	SERVER_WEB_STOP("film_delete.png"),

	/** Symbol "MQTT-Server" */
	SERVER_MQTT("transmit.png"),

	/** Symbol "MQTT-Server - starten" */
	SERVER_MQTT_START("transmit_add.png"),

	/** Symbol "MQTT-Server - stoppen" */
	SERVER_MQTT_STOP("transmit_delete.png"),

	/** Symbol "DDE-Server" */
	SERVER_DDE("comment.png"),

	/** Symbol "DDE-Server - starten" */
	SERVER_DDE_START("comment_add.png"),

	/** Symbol "DDE-Server- stoppen" */
	SERVER_DDE_STOP("comment_delete.png"),

	/** Symbol "Socket-Server" */
	SERVER_SOCKET("connect.png"),

	/** Symbol "Socket-Server - starten" */
	SERVER_SOCKET_START("connect.png"),

	/** Symbol "Socket-Server- stoppen" */
	SERVER_SOCKET_STOP("disconnect.png"),

	/* Info-Panel */

	/** Symbol "Infoleiste - ausblenden - für diesen Eintrag" */
	INFO_PANEL_CLOSE_THIS("application_side_expand.png"),

	/** Symbol "Infoleiste - ausblenden - für alle Einträge" */
	INFO_PANEL_CLOSE_ALL("cross.png"),

	/** Symbol "Infoleiste - Konfiguration - Gruppe ausblenden (Minus)" */
	INFO_PANEL_SETUP_HIDE("SmallMinus2.png"),

	/** Symbol "Infoleiste - Konfiguration - Gruppe einblenden (Plus)" */
	INFO_PANEL_SETUP_SHOW("SmallPlus2.png"),

	/* Datenquellen-Prüf-Dialog */

	/** Symbol "Prüfung der Datenquellen" */
	DATA_CHECK("arrow_refresh.png"),

	/** Symbol "Prüfung der Datenquellen - Station" */
	DATA_CHECK_STATION("station.png"),

	/** Symbol "Prüfung der Datenquellen - Quelle 'Tabelle'" */
	DATA_CHECK_MODE_FILE("Table.png"),

	/** Symbol "Prüfung der Datenquellen - Quelle 'Datenbank'" */
	DATA_CHECK_MODE_DB("database.png"),

	/** Symbol "Prüfung der Datenquellen - Quelle 'DDE'" */
	DATA_CHECK_MODE_DDE("comment.png"),

	/** Symbol "Prüfung der Datenquellen - Ergebnis 'Ok'" */
	DATA_CHECK_RESULT_OK("tick.png"),

	/** Symbol "Prüfung der Datenquellen - Ergebnis 'Fehler'" */
	DATA_CHECK_RESULT_ERROR("cross.png"),

	/* Verbindungskanten */

	/** Symbol "Verbindungskanten-Modus - direkt" */
	EDGE_MODE_DIRECT("Line.png"),

	/** Symbol "Verbindungskanten-Modus - abgewinkelt" */
	EDGE_MODE_MULTI_LINE("Line2.png"),

	/** Symbol "Verbindungskanten-Modus - abgerundet" */
	EDGE_MODE_MULTI_LINE_ROUNDED("Line3.png"),

	/** Symbol "Verbindungskanten-Modus - Bezierkurve" */
	EDGE_MODE_CUBIC_CURVE("Line4.png"),

	/* Skripte */

	/** Symbol "Skript ausführen" */
	SCRIPTRUNNER("page_white_code_red.png"),

	/** Symbol "Skript - Neu" */
	SCRIPT_NEW("page_add.png"),

	/** Symbol "Skript - Laden" */
	SCRIPT_LOAD("page_top.png"),

	/** Symbol "Skript - Speichern" */
	SCRIPT_SAVE("disk.png"),

	/** Symbol "Skript - Löschen" */
	SCRIPT_CLEAR("page_delete.png"),

	/** Symbol "Skript - Tools" */
	SCRIPT_TOOLS("cog.png"),

	/** Symbol "Skript - Ausführen" */
	SCRIPT_RUN("action_go.gif"),

	/** Symbol "Skript - Ausführung abbrechen" */
	SCRIPT_CANCEL("cancel.png"),

	/** Symbol "Skript - Vorlagen (für Elemente)" */
	SCRIPT_TEMPLATE("book.png"),

	/** Symbol "Skript - Beispiele (vollständige Skripte)" */
	SCRIPT_EXAMPLE("book.png"),

	/** Symbol "Schnellfilter-Modus - Rechenausdruck" */
	SCRIPT_MODE_EXPRESSION("fx.png"),

	/** Symbol "Schnellfilter-Modus - Javascript" */
	SCRIPT_MODE_JAVASCRIPT("page_white_code_red.png"),

	/** Symbol "Schnellfilter-Modus - Java" */
	SCRIPT_MODE_JAVA("page_white_cup.png"),

	/** Symbol "Schnellfilter-Modus - Liste mit Anweisungen" */
	SCRIPT_MODE_LIST("text_list_bullets.png"),

	/** Symbol "Zuordnung" */
	SCRIPT_MAP("application_tile_horizontal.png"),

	/** Symbol "Skript-Ausdruck - Text" */
	SCRIPT_RECORD_TEXT("font.png"),

	/** Symbol "Skript-Ausdruck - Rechenausdruck" */
	SCRIPT_RECORD_EXPRESSION("calculator.png"),

	/** Symbol "Skript-Ausdruck - XML" */
	SCRIPT_RECORD_XML("add.png"),

	/** Symbol "Skript-Ausdruck - Formatierung" */
	SCRIPT_RECORD_FORMAT("page_white_code_red.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Kunden" */
	SCRIPT_RECORD_DATA_CLIENT("user.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Stationen" */
	SCRIPT_RECORD_DATA_STATION("station.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Stationswarteschlangen" */
	SCRIPT_RECORD_DATA_STATION_QUEUE("Process.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Ressourcen" */
	SCRIPT_RECORD_DATA_RESOURCE("group.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Signal" */
	SCRIPT_RECORD_DATA_SIGNAL("Signal.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Zähler" */
	SCRIPT_RECORD_DATA_COUNTER("Counter.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Kosten" */
	SCRIPT_RECORD_DATA_COSTS("money_euro.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Modell" */
	SCRIPT_RECORD_MODEL("brick.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Modell bearbeiten" */
	SCRIPT_RECORD_MODEL_EDIT("brick_edit.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Statistik" */
	SCRIPT_RECORD_STATISTICS("sum2.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Statistik speichern" */
	SCRIPT_RECORD_STATISTICS_SAVE("icon_package_get.gif"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Zeit" */
	SCRIPT_RECORD_TIME("time.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Laufzeit" */
	SCRIPT_RECORD_RUNTIME("application.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Variable" */
	SCRIPT_RECORD_VARIABLE("font.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Eingabewert" */
	SCRIPT_RECORD_INPUT("keyboard.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Programm ausführen" */
	SCRIPT_RECORD_EXECUTE_PROGRAM("application_xp_terminal.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Analoger Wert" */
	SCRIPT_RECORD_ANALOG_VALUE("Bar.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Kunden freigeben" */
	SCRIPT_RECORD_RELEASE("TrafficLights.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Ausgabewert" */
	SCRIPT_RECORD_OUTPUT("application_xp_terminal.png"),

	/** Symbol "Skript ausführen - Eingabe (=Skript)" */
	SCRIPT_PANEL_INPUT("lightning_go.png"),

	/** Symbol "Skript ausführen - Ausgabe" */
	SCRIPT_PANEL_OUTPUT("application_xp_terminal.png"),

	/** Symbol "Skriptdatei" */
	SCRIPT_FILE("plugin.png"),

	/** Script "DDE-Ausgabe zu Excel" */
	SCRIPT_DDE("comment.png"),

	/* Verteilung anpassen */

	/** Symbol "Verteilung anpassen - Seite 'Werte'" */
	FIT_PAGE_VALUES("Table.png"),

	/** Symbol "Verteilung anpassen - Seite 'Empirische Verteilung'" */
	FIT_PAGE_EMPIRICAL_DISTRIBUTION("chart_bar.png"),

	/** Symbol "Verteilung anpassen - Seite 'Anpassung'" */
	FIT_PAGE_FIT("calculator.png"),

	/** Symbol "Verteilung anpassen - Seite 'Ergebnisse'" */
	FIT_PAGE_RESULT("chart_curve.png"),

	/* Einstellungen */

	/** Symbol "Einstellungen - Seite 'Benutzeroberfläche'" */
	SETUP_PAGE_APPLICATION("application_go.png"),

	/** Symbol "Einstellungen - Seite 'Leistung'" */
	SETUP_PAGE_PERFORMANCE("computer.png"),

	/** Symbol "Einstellungen - Seite 'Animation'" */
	SETUP_PAGE_ANIMATION("film_go.png"),

	/** Symbol "Einstellungen - Seite 'Statistik'" */
	SETUP_PAGE_STATISTICS("sum.png"),

	/** Symbol "Einstellungen - Seite 'Dateiformate'" */
	SETUP_PAGE_FILE_FORMATS("folder_wrench.png"),

	/** Symbol "Dateiformat 'XML'" */
	SETUP_PAGE_FILE_FORMATS_XML("page_white_code_red.png"),

	/** Symbol "Dateiformat 'JSON'" */
	SETUP_PAGE_FILE_FORMATS_JSON("page_world.png"),

	/** Symbol "Dateiformat 'ZIP-komprimierte XML-Datei'" */
	SETUP_PAGE_FILE_FORMATS_ZIP_XML("compress.png"),

	/** Symbol "Dateiformat 'TAR.GZ-komprimierte XML-Datei'" */
	SETUP_PAGE_FILE_FORMATS_TAR_XML("tux.png"),

	/** Symbol "Dateiformat 'Verschlüsselte XML-Datei'" */
	SETUP_PAGE_FILE_FORMATS_CRYPT("lock.png"),

	/** Symbol "Einstellungen - Seite 'Sicherheit'" */
	SETUP_PAGE_SECURITY("lock.png"),

	/** Symbol "Einstellungen - Seite 'Update'" */
	SETUP_PAGE_UPDATE("arrow_refresh.png"),

	/** Symbol "Einstellungen - Proxy" */
	SETUP_PROXY("server.png"),

	/** Symbol "Einstellungen - ... bei Anwendungsstart" */
	SETUP_APPLICATION_START("application.png"),

	/** Symbol "Einstellungen - Nach Abschluss langer Simulation Benachrichtigung anzeigen" */
	SETUP_NOTIFY_ON_LONG_RUN("time.png"),

	/** Symbol "Einstellungen - Lizenz" */
	SETUP_LICENSE("key.png"),

	/** Symbol "Einstellungen - Lizenz - Hinzufügen" */
	SETUP_LICENSE_ADD("key_add.png"),

	/** Symbol "Einstellungen - Lizenz - Entfernen" */
	SETUP_LICENSE_DELETE("key_delete.png"),

	/** Symbol "Einstellungen - Schriftgröße klein" */
	SETUP_FONT_SIZE1("FontSize_1.png"),

	/** Symbol "Einstellungen - Schriftgröße normal" */
	SETUP_FONT_SIZE2("FontSize_2.png"),

	/** Symbol "Einstellungen - Schriftgröße größer" */
	SETUP_FONT_SIZE3("FontSize_3.png"),

	/** Symbol "Einstellungen - Schriftgröße groß" */
	SETUP_FONT_SIZE4("FontSize_4.png"),

	/** Symbol "Einstellungen - Schriftgröße ganz groß" */
	SETUP_FONT_SIZE5("FontSize_5.png"),

	/** Symbol "Einstellungen - Fenstergröße - Vorgabe" */
	SETUP_WINDOW_SIZE_DEFAULT("application_double.png"),

	/** Symbol "Einstellungen - Fenstergröße - Vollbild" */
	SETUP_WINDOW_SIZE_FULL("application.png"),

	/** Symbol "Einstellungen - Fenstergröße - Letzte wiederherstellen" */
	SETUP_WINDOW_SIZE_LAST("application_edit.png"),

	/** Symbol "Einstellungen - Vorlagenleiste beim Programmstart - Ausblenden" */
	SETUP_TEMPLATES_ON_START_HIDE("application_side_contract.png"),

	/** Symbol "Einstellungen - Vorlagenleiste beim Programmstart - Einblenden" */
	SETUP_TEMPLATES_ON_START_SHOW("application_side_expand.png"),

	/** Symbol "Einstellungen - Vorlagenleiste beim Programmstart - Letzter Zustand" */
	SETUP_TEMPLATES_ON_START_LAST("application_side_boxes.png"),

	/** Symbol "Einstellungen - Javascript-Engine - automatisch" */
	SETUP_ENGINE_AUTOMATIC("page_white_find.png"),

	/** Symbol "Einstellungen - Javascript-Engine - Nashorn" */
	SETUP_ENGINE_NASHORN("cup.png"),

	/** Symbol "Einstellungen - Javascript-Engine - Rhino" */
	SETUP_ENGINE_RHINO("MozillaRhino.gif"),

	/** Symbol "Einstellungen - Javascript-Engine - Graal" */
	SETUP_ENGINE_GRAAL("GraalJS.gif"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Anwenden" */
	SETUP_ANIMATION_START_NORMAL("action_go.gif"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Fragen ob überspringen" */
	SETUP_ANIMATION_START_ASK("help.png"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Überspringen" */
	SETUP_ANIMATION_START_SKIP("control_end_blue.png"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Vorab ohne Animation ausführen" */
	SETUP_ANIMATION_START_FAST("control_fastforward_blue.png"),

	/** Symbol "Einstellungen - Restriktionen anwenden auf - Modelle aus dem Internet" */
	SETUP_FILE_SECURITY_INTERNET("world.png"),

	/** Symbol "Einstellungen - Restriktionen anwenden auf - Alle Modelldateien" */
	SETUP_FILE_SECURITY_ALL("computer.png"),

	/* Modelleditor */

	/** Symbol "Modelleditor - Element hinzufügen nicht zulässig" */
	MODELEDITOR_NOT_ALLOWED("NotAllowed.png"),

	/** Symbol "Modelleditor - Kante hinzufügen nicht zulässig" */
	MODELEDITOR_NOT_ALLOWED_EDGE("NotAllowedEdge.png"),

	/** Symbol "Modelleditor - Beschreibung zu einzelnem Element" */
	MODELEDITOR_COMMENT("comment_edit.png"),

	/** Symbol "Modelleditor - Warteschlangendarstellung während der Animation" */
	MODELEDITOR_QUEUE("Symbol.png"),

	/** Symbol "Modelleditor - Vorlagen - Gruppe ausklappen (Plus)" */
	MODELEDITOR_GROUP_PLUS("SmallPlus.png"),

	/** Symbol "Modelleditor - Vorlagen - Gruppe einklappen (Minus)" */
	MODELEDITOR_GROUP_MINUS("SmallMinus.png"),

	/** Symbol "Modelleditor - Eingabetabelle wählen" */
	MODELEDITOR_OPEN_INPUT_FILE("table_go.png"),

	/** Symbol "Modelleditor - Ausgabetabelle wählen" */
	MODELEDITOR_OPEN_OUTPUT_FILE("table_go.png"),

	/** Symbol "Modelleditor - Eigenschaften" */
	MODELEDITOR_ELEMENT_PROPERTIES("cog.png"),

	/** Symbol "Modelleditor - Eigenschaften - Aussehen" */
	MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE("image.png"),

	/** Symbol "Modelleditor - Eigenschaften - Text" */
	MODELEDITOR_ELEMENT_PROPERTIES_TEXT("font.png"),

	/** Symbol "Modelleditor - Eigenschaften - Rahmen" */
	MODELEDITOR_ELEMENT_PROPERTIES_BORDER("Rectangle.png"),

	/** Symbol "Modelleditor - Kontextmenü 'Visualisierung hinzufügen'" */
	MODELEDITOR_ELEMENT_ADD_VISUALIZATION("chart_bar.png"),


	/** Symbol "Modelleditor - Kontextmenü 'Laufzeitstatistik hinzufügen'" */
	MODELEDITOR_ELEMENT_ADD_LONG_RUN_STATISTICS("chart_curve_add.png"),

	/** Symbol "Modelleditor - Kontextmenü 'Typische Folgestation hinzufügen'" */
	MODELEDITOR_ELEMENT_NEXT_STATIONS("station.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Ziel" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET("Dispose.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Transporter" */
	MODELEDITOR_ELEMENT_TRANSPORT_TRANSPORTER("lorry.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Prioritäten" */
	MODELEDITOR_ELEMENT_TRANSPORT_PRIORITIES("user.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Gruppengröße" */
	MODELEDITOR_ELEMENT_TRANSPORT_BATCH("Batch.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Modus 'explizit'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_EXPLICITE("Dispose.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Modus 'Fertigungsplan'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_SEQUENCE("text_list_numbers.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Modus 'nach Eigenschaft'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_PROPERTY("font.png"),

	/* Modelleditor: Eingang/Ausgang */

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle'" */
	MODELEDITOR_ELEMENT_SOURCE("Source.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Verteilung " */
	MODELEDITOR_ELEMENT_SOURCE_MODE_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Ausdruck " */
	MODELEDITOR_ELEMENT_SOURCE_MODE_EXPRESSION("fx.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Zeitplan" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_SCHEDULE("time.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Bedingung" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_CONDITION("TrafficLights.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Schwllenwert" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_THRESHOLD("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Signal" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_SIGNALS("Signal.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Anzahl pro Intervall'" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_INTERVAL_EXPRESSIONS("Table.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zwischenankunftszeiten pro Intervall'" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_INTERVAL_DISTRIBUTIONS("Table.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zahlenwerte'" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_DATA_STREAM("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zwischenankunftszeiten'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_INTERARRIVAL("time.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Batch-Größe'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_BATCH("group.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Anzahl an Kunden'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_COUNT("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Startzeitpunkt'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_START("flag_green.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zuweisung von Kundenvariablen'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_NUMBERS("font.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zuweisung von Texten'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_TEXTS("font.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Mehrfachquelle'" */
	MODELEDITOR_ELEMENT_SOURCE_MULTI("Source.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Tabellenquelle'" */
	MODELEDITOR_ELEMENT_SOURCE_TABLE("Source.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Datenbankquelle'" */
	MODELEDITOR_ELEMENT_SOURCE_DB("database_go.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'DDE-Quelle'" */
	MODELEDITOR_ELEMENT_SOURCE_DDE("database_go.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Ausgang'" */
	MODELEDITOR_ELEMENT_DISPOSE("Dispose.png"),

	/* Modelleditor: Verarbeitung */

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation'" */
	MODELEDITOR_ELEMENT_PROCESS("Process.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Bedienzeiten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_SERVICE("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Rüstzeiten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_SETUP("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Rüstzeiten'" - Schaltfläche "Rüstzeiten-Assistent" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_SETUP_ASSISTANT("wand.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Nachbearbeitungszeiten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_POST_PROCESS("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Wartezeittoleranzen'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_CANCEL("cancel.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Prioritäten und Batch-Größen'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_PRORITY("user.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Bediener'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_RESOURCES("group.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Kosten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_COSTS("money_euro.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Verzögerung'" */
	MODELEDITOR_ELEMENT_DELAY("Delay.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Verzögerung (Skript)'" */
	MODELEDITOR_ELEMENT_DELAY_JS("Delay.png"),

	/* Modelleditor: Zuweisungen */

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Typzuweisung'" */
	MODELEDITOR_ELEMENT_ASSIGN("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Textzuweisung'" */
	MODELEDITOR_ELEMENT_ASSIGN_STRING("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Kosten'" */
	MODELEDITOR_ELEMENT_COSTS("money_euro.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Variable'" */
	MODELEDITOR_ELEMENT_SET("Set.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Script'" */
	MODELEDITOR_ELEMENT_SET_JS("Set.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Zähler'" */
	MODELEDITOR_ELEMENT_COUNTER("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Batch-Zähler'" */
	MODELEDITOR_ELEMENT_COUNTER_BATCH("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Multizähler'" */
	MODELEDITOR_ELEMENT_COUNTER_MULTI("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Durchsatz'" */
	MODELEDITOR_ELEMENT_THROUGHPUT("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Zustand'" */
	MODELEDITOR_ELEMENT_STATE_STATISTICS("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Differenzzähler'" */
	MODELEDITOR_ELEMENT_DIFFERENTIAL_COUNTER("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Bereich betreten'" */
	MODELEDITOR_ELEMENT_SECTION_START("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Bereich verlassen'" */
	MODELEDITOR_ELEMENT_SECTION_END("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Bereich Kundenstatistik'" */
	MODELEDITOR_ELEMENT_SET_STATISTICS_MODE("chart_curve.png"),

	/* Modelleditor: Verzweigungen */

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Duplizieren'" */
	MODELEDITOR_ELEMENT_DUPLICATE("Duplicate.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen'" */
	MODELEDITOR_ELEMENT_DECIDE("Decide.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - zufällig" */
	MODELEDITOR_ELEMENT_DECIDE_BY_CHANCE("arrow_switch.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach Bedingung" */
	MODELEDITOR_ELEMENT_DECIDE_BY_CONDITION("TrafficLights.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach Kundentyp" */
	MODELEDITOR_ELEMENT_DECIDE_BY_CLIENT_TYPE("group.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - abwechselnd" */
	MODELEDITOR_ELEMENT_DECIDE_BY_SEQUENCE("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach kürzester Warteschlange an der nächsten Station" */
	MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_STATION("station.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach kürzester Warteschlange an der nächsten Bedienstation" */
	MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_PROCESS_STATION("Process.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - wenigste Kunden an der nächsten Station" */
	MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_STATION("station.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - wenigste Kunden an der nächsten Bedienstation" */
	MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_PROCESS_STATION("Process.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach Texteigenschaft" */
	MODELEDITOR_ELEMENT_DECIDE_BY_TEXT_PROPERTY("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - bei Gleichstand zwischen Stationen von oben nach unten */
	MODELEDITOR_ELEMENT_DECIDE_AT_TIE_FIRST("arrow_down.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - bei Gleichstand zwischen Stationen zufällig */
	MODELEDITOR_ELEMENT_DECIDE_AT_TIE_RANDOM("arrow_switch.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - bei Gleichstand zwischen Stationen von unten nach oben */
	MODELEDITOR_ELEMENT_DECIDE_AT_TIE_LAST("arrow_up.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen (Skript)'" */
	MODELEDITOR_ELEMENT_DECIDE_JS("Decide.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Zurückschrecken'" */
	MODELEDITOR_ELEMENT_BALKING("Decide.png"),

	/* Modelleditor: Schranken */

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Bedingung'" */
	MODELEDITOR_ELEMENT_HOLD("Hold.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Bedingung' - Dialogseite 'Bedingung'" */
	MODELEDITOR_ELEMENT_HOLD_PAGE_CONDITION("Hold.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Bedingung' - Dialogseite 'Prioritäten'" */
	MODELEDITOR_ELEMENT_HOLD_PAGE_PRIORITY("user.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Multibedingung'" */
	MODELEDITOR_ELEMENT_HOLD_MULTI("HoldMulti.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Bedingung (Skript)'" */
	MODELEDITOR_ELEMENT_HOLD_JS("HoldJS.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Signal'" */
	MODELEDITOR_ELEMENT_SIGNAL("Signal.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Schranke'" */
	MODELEDITOR_ELEMENT_BARRIER("Barrier.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Pull-Schranke'" */
	MODELEDITOR_ELEMENT_BARRIER_PULL("Barrier.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Ressource belegen'" */
	MODELEDITOR_ELEMENT_SEIZE("Seize.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Ressource freigeben'" */
	MODELEDITOR_ELEMENT_RELEASE("Release.png"),

	/* Modelleditor: Kunden verbinden */

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Zusammenfassen'" */
	MODELEDITOR_ELEMENT_BATCH("Batch.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Trennen'" */
	MODELEDITOR_ELEMENT_SEPARATE("Separate.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Zusammenführen'" */
	MODELEDITOR_ELEMENT_MATCH("Match.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Ausleiten'" */
	MODELEDITOR_ELEMENT_PICK_UP("PickUp.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Zerteilen'" */
	MODELEDITOR_ELEMENT_SPLIT("Source.png"),

	/* Modelleditor: Transport */

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Transportstart'" */
	MODELEDITOR_ELEMENT_TRANSPORT_SOURCE("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Haltestelle'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TRANSPORTER_SOURCE("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Transportstart (Fertigungsplan)' (veraltet)" */
	MODELEDITOR_ELEMENT_TRANSPORT_SOURCE_ROUTER("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Transportziel'" */
	MODELEDITOR_ELEMENT_TRANSPORT_DESTINATION("TransportDestination.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Parkplatz'" */
	MODELEDITOR_ELEMENT_TRANSPORT_PARKING("Vertex.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Plan zuweisen'" */
	MODELEDITOR_ELEMENT_ASSIGN_SEQUENCE("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Wegpunkt'" */
	MODELEDITOR_ELEMENT_WAY_POINT("Vertex.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Teleport-Transport Startpunkt &amp; Zielpunkt'" */
	MODELEDITOR_ELEMENT_TELEPORT("Teleport.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Teleport-Transport Startpunkt'" */
	MODELEDITOR_ELEMENT_TELEPORT_SOURCE("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Teleport-Transport Zielpunkt'" */
	MODELEDITOR_ELEMENT_TELEPORT_DESTINATION("TransportDestination.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Fließband'" */
	MODELEDITOR_ELEMENT_CONVEYOR("Conveyor.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Fließband'- Dialogseite 'Benötigte und vorhandene Kapazität'" */
	MODELEDITOR_ELEMENT_CONVEYOR_PAGE_CAPACITY("package.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Fließband'- Dialogseite 'Transportzeit'" */
	MODELEDITOR_ELEMENT_CONVEYOR_PAGE_TIME("time.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Fließband'- Dialogseite 'Animation'" */
	MODELEDITOR_ELEMENT_CONVEYOR_PAGE_ANIMATION("film_go.png"),

	/* Modelleditor: Daten Ein-/Ausgabe */

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe'" */
	MODELEDITOR_ELEMENT_INPUT("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe (Skript)'" */
	MODELEDITOR_ELEMENT_INPUT_JS("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe (DB)'" */
	MODELEDITOR_ELEMENT_INPUT_DB("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe (DDE)'" */
	MODELEDITOR_ELEMENT_INPUT_DDE("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe'" */
	MODELEDITOR_ELEMENT_OUTPUT("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe (Skript)'" */
	MODELEDITOR_ELEMENT_OUTPUT_JS("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe (DB)'" */
	MODELEDITOR_ELEMENT_OUTPUT_DB("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe (DDE)'" */
	MODELEDITOR_ELEMENT_OUTPUT_DDE("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Aufzeichnung'" */
	MODELEDITOR_ELEMENT_RECORD("disk.png"),

	/* Modelleditor: Flusssteuerungslogik */

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'If'" */
	MODELEDITOR_ELEMENT_LOGIC_IF("LogicIf.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'ElseIf'" */
	MODELEDITOR_ELEMENT_LOGIC_ELSE_IF("LogicElseIf.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'Else'" */
	MODELEDITOR_ELEMENT_LOGIC_ELSE("LogicElse.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'EndIf'" */
	MODELEDITOR_ELEMENT_LOGIC_END_IF("LogicEndIf.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'While'" */
	MODELEDITOR_ELEMENT_LOGIC_WHILE("LogicWhile.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'EndWhile'" */
	MODELEDITOR_ELEMENT_LOGIC_END_WHILE("LogicEndWhile.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'Do'" */
	MODELEDITOR_ELEMENT_LOGIC_DO("LogicDo.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'Until'" */
	MODELEDITOR_ELEMENT_LOGIC_UNTIL("LogicUntil.png"),

	/* Modelleditor: Analoge Werte */

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Analoger Wert'" */
	MODELEDITOR_ELEMENT_ANALOG_VALUE("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Analogen W. ändern'" */
	MODELEDITOR_ELEMENT_ANALOG_ASSIGN("Set.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Tank'" */
	MODELEDITOR_ELEMENT_TANK("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Fluss'" */
	MODELEDITOR_ELEMENT_TANK_FLOW_BY_CLIENT("arrow_right.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Fluss (Signal)'" */
	MODELEDITOR_ELEMENT_TANK_FLOW_BY_SIGNAL("arrow_right.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Tank' - Ventil" */
	MODELEDITOR_ELEMENT_TANK_VALVE("database_gear.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Sensor'" */
	MODELEDITOR_ELEMENT_SENSOR("eye.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Ventil-Setup'" */
	MODELEDITOR_ELEMENT_VALVE_SETUP("arrow_right.png"),

	/* Modelleditor: Animation */

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Icon'" */
	MODELEDITOR_ELEMENT_CLIENT_ICON("image.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdaten als Text'" */
	MODELEDITOR_ELEMENT_ANIMATION_TEXT_VALUE("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'LCD-Anzeige'" */
	MODELEDITOR_ELEMENT_ANIMATION_LCD("SevenSegments.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Analogskala'" */
	MODELEDITOR_ELEMENT_ANIMATION_POINTER_MEASURING("Scale.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Skriptergebnis als Text'" */
	MODELEDITOR_ELEMENT_ANIMATION_TEXT_VALUE_JS("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Text gemäß Simulationsdaten'" */
	MODELEDITOR_ELEMENT_ANIMATION_TEXT_SELECT("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdaten als Balken'" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdaten als gestapelter Balken'" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK("BarStack.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenampel (2 Lichter)'" */
	MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS("TrafficLights.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenampel (3 Lichter)'" */
	MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS3("TrafficLights3.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenliniendiagramm'" */
	MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenliniendiagramm' - Reihe hinzufügen" */
	MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM_ADD("chart_curve_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenbalkendiagramm'" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART("chart_bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenbalkendiagramm' - Reihe hinzufügen" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART_ADD("chart_bar_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatentortendiagramm'" */
	MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART("chart_pie.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatentortendiagramm'" - Modus: Ringdiagramm */
	MODELEDITOR_ELEMENT_ANIMATION_DONUT_CHART("chart_donut.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatentortendiagramm' - Segment hinzufügen" */
	MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART_ADD("chart_pie_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationszeit'" */
	MODELEDITOR_ELEMENT_ANIMATION_CLOCK("time.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Animationsbild'" */
	MODELEDITOR_ELEMENT_ANIMATION_IMAGE("image.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Animationsbild' - Beispiel" */
	MODELEDITOR_ELEMENT_ANIMATION_IMAGE_EXAMPLE("Picture_Example.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Datenaufzeichnung anzeigen'" */
	MODELEDITOR_ELEMENT_ANIMATION_RECORD("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Alarm'" */
	MODELEDITOR_ELEMENT_ANIMATION_ALARM("error.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Pause'" */
	MODELEDITOR_ELEMENT_ANIMATION_PAUSE("Pause.png"),

	/* Modelleditor: Animation - Interaktiv */

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Schaltfläche'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_BUTTON("buttonOk.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Schieberegler'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_SLIDER("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Checkbox'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_CHECKBOX("Check.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Radiobutton'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_RADIOBUTTON("Radiobutton.png"),

	/* Modelleditor: Sonstiges */

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Statistik'" */
	MODELEDITOR_ELEMENT_USER_STATISTICS("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Aktion'" */
	MODELEDITOR_ELEMENT_ACTION("eye.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Untermodell'" */
	MODELEDITOR_ELEMENT_SUB("application_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Untermodell' - Bearbeiten" */
	MODELEDITOR_ELEMENT_SUB_EDIT("brick_edit.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Referenz'" */
	MODELEDITOR_ELEMENT_REFERENCE("group.png"),

	/* Modelleditor: Optische Gestaltung */

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Beschreibungstext'" */
	MODELEDITOR_ELEMENT_TEXT("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Verbindungsecke'" */
	MODELEDITOR_ELEMENT_VERTEX("Vertex.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Verbindungsecke' (Löschen)" */
	MODELEDITOR_ELEMENT_VERTEX_DELETE("Vertex_delete.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Linie'" */
	MODELEDITOR_ELEMENT_LINE("Line.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Rechteck'" */
	MODELEDITOR_ELEMENT_RECTANGLE("Rectangle.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Ellipse'" */
	MODELEDITOR_ELEMENT_ELLIPSE("Ellipse.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Bild'" */
	MODELEDITOR_ELEMENT_IMAGE("image.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Bild' - Beispiel" */
	MODELEDITOR_ELEMENT_IMAGE_EXAMPLE("Picture_Example.png");

	/**
	 * Dateiname des Icons
	 */
	private final String name;

	/**
	 * URLs des Icons
	 */
	private URL[] urls;

	/**
	 * Bild
	 */
	private Image image;

	/**
	 * Icon
	 */
	private Icon icon;

	/**
	 * Konstruktor des Enum
	 * @param name	Dateiname des Icons
	 */
	Images(final String name) {
		this.name=name;
	}

	/**
	 * Sucht ein Bild in einem Ordner und fügt es, wenn gefunden, zu einer Liste hinzu.
	 * @param list	Liste mit URLs zu der die neue URL hinzugefügt werden soll
	 * @param folder	Ordner in dem das Bild gesucht werden soll
	 * @param name	Name des Bildes
	 */
	private void addURL(final List<URL> list, final String folder, final String name) {
		URL url;

		url=getClass().getResource(folder+"/"+name);
		if (url!=null) {
			list.add(url);
		} else {
			url=getClass().getResource(folder+"/"+name.replace('_','-'));
			if (url!=null) list.add(url);
		}
	}

	/**
	 * Liefert die URL des Icons
	 * @return	URL des Icons
	 */
	public URL[] getURLs() {
		if (urls==null) {
			List<URL> list=new ArrayList<>();
			addURL(list,"res",name);
			addURL(list,"res24",name);
			addURL(list,"res32",name);
			addURL(list,"res48",name);
			urls=list.toArray(new URL[0]);
		}
		assert(urls!=null);
		return urls;
	}

	/**
	 * Wird das Programm unter Java 9 oder höher ausgeführt, so wird
	 * der Konstruktor der Multi-Resolution-Bild-Objektes geliefert, sonst <code>null</code>.
	 * @return	Multi-Resolution-Bild-Konstruktor oder <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	private static Constructor<Object> getMultiImageConstructor() {
		try {
			final Class<?> cls=Class.forName("java.awt.image.BaseMultiResolutionImage");
			return (Constructor<Object>)cls.getDeclaredConstructor(int.class,Image[].class);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			return null;
		}
	}

	/**
	 * Liefert das Icon.
	 * @return	Icon
	 */
	public Icon getIcon() {
		if (icon==null) {
			final Image image=getImage();
			if (image!=null) icon=new ImageIcon(image);
		}
		assert(icon!=null);
		return icon;
	}

	/**
	 * Liefert basierend auf einer oder mehreren URLs das Standardbild (das Bild für die erste URL)
	 * @param urls	Liste mit URLs
	 * @return	Bild für die erste URL
	 */
	private Image getDefaultImage(final URL[] urls) {
		if (urls==null || urls.length==0) return null;
		try {
			return ImageIO.read(urls[0]);
		} catch (IOException e) {
			assert(false);
			return null;
		}
	}

	/**
	 * Liefert das Bild.
	 * @return	Bild
	 */
	public Image getImage() {
		if (image!=null) return image;

		final URL[] urls=getURLs();
		assert(urls.length>0);

		if (urls.length==1) return image=getDefaultImage(urls);

		final Constructor<Object> multiConstructor=getMultiImageConstructor();
		if (multiConstructor==null) return image=getDefaultImage(urls);

		final Image[] images=Arrays.asList(urls).stream().map(url->{
			try {
				return ImageIO.read(url);
			} catch (IOException e) {
				return image=getDefaultImage(urls);
			}
		}).toArray(Image[]::new);

		try {
			image=(Image)multiConstructor.newInstance(0,images);
			assert(image!=null);
			return image;
		} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
			return image=getDefaultImage(urls);
		}
	}

	/**
	 * Prüft, ob alle Icons vorhanden sind.
	 */
	public static void checkAll() {
		for (Images image: values()) {
			System.out.print(image.name+": ");
			if (image.getIcon()==null) System.out.println("missing"); else System.out.println("ok");
		}
	}
}
