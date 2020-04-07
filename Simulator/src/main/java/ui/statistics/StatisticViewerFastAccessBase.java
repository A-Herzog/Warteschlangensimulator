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
package ui.statistics;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import language.Language;
import simulator.statistics.Statistics;
import ui.images.Images;

/**
 * Diese Basisklasse stellt Funktionen zur Implementierung von Ergebnisfiltern zur Verfügung.
 * @author Alexander Herzog
 * @see StatisticViewerFastAccess
 */
public abstract class StatisticViewerFastAccessBase extends JPanel {
	private static final long serialVersionUID = -7832961660281079068L;

	private final Runnable helpFastAccess;
	private final Runnable resultsChanged;
	private String results;

	/**
	 * Hilfe-Runnable für modale Dialoge
	 */
	protected final Runnable helpFastAccessModal;

	/**
	 * Zu filternde Statistik
	 */
	protected Statistics statistics;

	/**
	 * Konstruktor der Klasse
	 * @param helpFastAccess	Hilfe für Schnellzugriff-Seite
	 * @param helpFastAccessModal	Hilfe für Schnellzugriff-Dialog
	 * @param statistics	Statistik-Objekt, dem die Daten entnommen werden sollen
	 * @param resultsChanged	Runnable das aufgerufen wird, wenn sich die Ergebnisse verändert haben
	 * @param addToolsButton	Soll das Tools-Button in der Symbolleiste angezeigt werden?
	 */
	public StatisticViewerFastAccessBase(final Runnable helpFastAccess, final Runnable helpFastAccessModal, final Statistics statistics, final Runnable resultsChanged, final boolean addToolsButton) {
		super();
		this.helpFastAccess=helpFastAccess;
		this.helpFastAccessModal=helpFastAccessModal;
		setStatistics(statistics);
		this.resultsChanged=resultsChanged;
		results="";

		setLayout(new BorderLayout());

		final JToolBar toolbar;
		add(toolbar=new JToolBar(),BorderLayout.NORTH);
		toolbar.setFloatable(false);

		final JButton toolbarNew;
		toolbar.add(toolbarNew=new JButton(Language.tr("Dialog.Button.New")));
		toolbarNew.addActionListener(e->commandNew());
		toolbarNew.setToolTipText(Language.tr("Statistic.FastAccess.New.Tooltip"));
		toolbarNew.setIcon(Images.SCRIPT_NEW.getIcon());

		final JButton toolbarLoad;
		toolbar.add(toolbarLoad=new JButton(Language.tr("Dialog.Button.Load")));
		toolbarLoad.addActionListener(e->commandLoad());
		toolbarLoad.setToolTipText(Language.tr("Statistic.FastAccess.Load.Tooltip"));
		toolbarLoad.setIcon(Images.SCRIPT_LOAD.getIcon());

		final JButton toolbarSave;
		toolbar.add(toolbarSave=new JButton(Language.tr("Dialog.Button.Save")));
		toolbarSave.addActionListener(e->commandSave());
		toolbarSave.setToolTipText(Language.tr("Statistic.FastAccess.Save.Tooltip"));
		toolbarSave.setIcon(Images.SCRIPT_SAVE.getIcon());

		addCustomToolbarButtons(toolbar);

		if (addToolsButton) {
			toolbar.addSeparator();
			final JButton toolbarTools;
			toolbar.add(toolbarTools=new JButton(Language.tr("Dialog.Button.Commands")));
			toolbarTools.addActionListener(e->commandTools(toolbarTools));
			toolbarTools.setIcon(Images.SCRIPT_TOOLS.getIcon());
		}

		if (helpFastAccess!=null) {
			toolbar.addSeparator();
			final JButton toolbarHelp;
			toolbar.add(toolbarHelp=new JButton(Language.tr("Dialog.Button.Help")));
			toolbarHelp.addActionListener(e->commandHelp());
			toolbarHelp.setToolTipText(Language.tr("Statistic.FastAccess.Help.Tooltip"));
			toolbarHelp.setIcon(Images.HELP.getIcon());
		}
	}

	/**
	 * Liefert das Icon für den Tab dieses Filters
	 * @return	Icon für den Tab dieses Filters
	 */
	protected abstract Icon getIcon();

	/**
	 * Fügt einen XML-Selektor zur Ausgabe hinzu
	 * @param selector	Zur Ausgabe hinzuzufügender XML-Selektor
	 */
	protected abstract void addXML(final String selector);

	/**
	 * Funktionen, die beim Setzen einer neuen Statistik ausgeführt werden sollen
	 * @param statistics	Neue Statistik
	 */
	public void setStatistics(final Statistics statistics) {
		this.statistics=statistics;
	}

	/**
	 * Diese Methode kann in abgeleiteten Klassen überschrieben werden, um benutzerdefinierte Toolbar-Einträge hinzuzufügen.
	 * @param toolbar	Toolbar zu dem Elemente hinzugefügt werden sollen
	 */
	protected void addCustomToolbarButtons(final JToolBar toolbar) {
	}

	/**
	 * Diese Methode kann von abgeleiteten Klassen aufgerufen werden, wenn neue Filterergebnisse vorliegen.
	 * Die Benachrichtigung für die Ausgabe erfolgt dann automatisch.
	 * @param result	Auszugebendes Ergebnis
	 */
	protected final void setResults(final String result) {
		if (result==null) results=""; else results=result;
		if (resultsChanged!=null) resultsChanged.run();
	}

	/**
	 * Liefert die aktuellen Ergebnisse nach außen.
	 * @return	Aktuelle Filterergebnisse
	 */
	public final String getResults() {
		return results;
	}

	/**
	 * Wird beim Klicken auf das "Neu"-Button in der Symbolleiste aufgerufen.
	 */
	protected abstract void commandNew();

	/**
	 * Wird beim Klicken auf das "Laden"-Button in der Symbolleiste aufgerufen.
	 */
	protected abstract void commandLoad();

	/**
	 * Wird beim Klicken auf das "Speichern"-Button in der Symbolleiste aufgerufen.
	 */
	protected abstract void commandSave();

	/**
	 * Wird beim Klicken auf das "Tools"-Button in der Symbolleiste aufgerufen.
	 * @param sender	Button von dem die Aktion ausgeht (zur Ausrichtung des Popup-Menüs)
	 */
	protected abstract void commandTools(final JButton sender);

	private void commandHelp() {
		if (helpFastAccess!=null) helpFastAccess.run();
	}
}