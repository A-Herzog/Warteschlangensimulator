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
package ui.quickaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import language.Language;
import ui.dialogs.SetupDialog;
import ui.images.Images;

/**
 * Erstellt Schnellzugriffeinträge basierend auf den Optionen
 * im Einstellungen-Dialog
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 * @see SetupDialog
 */
public class JQuickAccessBuilderSettings extends JQuickAccessBuilder {
	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText Eingegebener Text
	 */
	public JQuickAccessBuilderSettings(final String quickAccessText) {
		super(Language.tr("QuickAccess.Settings"),Language.tr("QuickAccess.Settings.Hint"),quickAccessText,false);
	}

	/**
	 * Erstellt eine Zuordnung zwischen Bezeichnern und Einstellungendialog-Dialogseiten.
	 * @return	Zuordnung zwischen Bezeichnern und Einstellungendialog-Dialogseiten
	 * @see #work(Consumer)
	 */
	private Map<String,SetupDialog.Page> buildMap() {
		final Map<String,SetupDialog.Page> map=new HashMap<>();
		SetupDialog.Page page;

		/* Seite: Benutzeroberfläche */

		page=SetupDialog.Page.UI;
		map.put(Language.tr("SettingsDialog.Languages"),page);
		map.put(Language.tr("SettingsDialog.LookAndFeel"),page);
		map.put(Language.tr("SettingsDialog.FontSizes"),page);
		map.put(Language.tr("SettingsDialog.HighContrasts"),page);
		map.put(Language.tr("SettingsDialog.AutoSave"),page);
		map.put(Language.tr("SettingsDialog.UseLastFiles"),page);
		map.put(Language.tr("SettingsDialog.AutoRestore"),page);
		map.put(Language.tr("SettingsDialog.WindowSizeProgrmStart"),page);
		map.put(Language.tr("SettingsDialog.TemplatesPanel"),page);
		map.put(Language.tr("SettingsDialog.LoadModelOnProgramStart"),page);
		map.put(Language.tr("SettingsDialog.ModellSecurity"),page);
		map.put(Language.tr("SettingsDialog.SurfaceHelp"),page);
		map.put(Language.tr("SettingsDialog.NotifyMode"),page);

		/* Seite: Leistung */

		page=SetupDialog.Page.PERFORMANCE;
		map.put(Language.tr("SettingsDialog.BackgroundProcessing"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.UseMultiCore"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.UseMultiCoreAnimation"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.UseHighPriority"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA"),page);
		map.put(Language.tr("SettingsDialog.JSEngine"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Performance.CancelSimulationOnScriptError"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.Server.Use"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.Server.Name"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.Server.Port"),page);

		/* Seite: Animation */

		page=SetupDialog.Page.ANIMATION;
		map.put(Language.tr("SettingsDialog.AnimationWarmUp"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.ShowStationData"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.ShowSingleStepLogData"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.AnimationStartPaused"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.AnimateResources"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.UseSlowModeAnimation"),page);

		/* Seite: Statistik */

		page=SetupDialog.Page.STATISTICS;
		map.put(Language.tr("SettingsDialog.Tabs.Statistics.ShowQuantils"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.ShowErlangC"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.ExpandAllStatistics"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Statistics.NumberDigits"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Statistics.PercentDigits"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Statistics.QuantilLevels.Levels"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Statistics.BatchMeansConfidenceLevels.Levels"),page);

		/* Seite: Dateiformate */

		page=SetupDialog.Page.FILE_FORMATS;
		map.put(Language.tr("SettingsDialog.Tabs.DefaultUserName.Name"),page);
		map.put(Language.tr("SettingsDialog.Tabs.DefaultFormats"),page);
		map.put(Language.tr("SettingsDialog.Tabs.BackupFiles"),page);
		map.put(Language.tr("SettingsDialog.Tabs.ExcelDDEConnect"),page);
		map.put(Language.tr("SettingsDialog.ImageResolution"),page);
		map.put(Language.tr("SettingsDialog.ImageAnimation"),page);
		map.put(Language.tr("SettingsDialog.Tabs.Simulation.PaintTimeStamp"),page);

		/* Seite: Update */

		page=SetupDialog.Page.UPDATES;
		map.put(Language.tr("SettingsDialog.TestJavaVersionOnProgramStart"),page);
		map.put(Language.tr("SettingsDialog.AutoUpdate"),page);

		return map;
	}

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param openSetupDialog Callback zum Öffnen des Einstellungendialogs
	 */
	public void work(final Consumer<SetupDialog.Page> openSetupDialog) {
		final String quickAccessTextLower=quickAccessText.trim().toLowerCase();
		if (quickAccessTextLower.length()<2) return;

		final Map<String,SetupDialog.Page> map=buildMap();
		for (Map.Entry<String,SetupDialog.Page> entry: map.entrySet()) {
			if (entry.getKey().toLowerCase().contains(quickAccessTextLower)) {

				test(
						entry.getValue().getName(),
						entry.getKey(),
						Images.GENERAL_SETUP.getIcon(),
						record->openSetupDialog.accept((SetupDialog.Page)record.data),
						entry.getValue());
			}
		}
	}
}