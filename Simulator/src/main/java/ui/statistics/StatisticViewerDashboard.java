/**
 * Copyright 2022 Alexander Herzog
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import language.Language;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.MainFrame;
import ui.images.Images;
import ui.script.ScriptTools;
import ui.statistics.ListPopup.ScriptHelperRecord;
import ui.tools.WrapLayout;

/**
 * Dashboard-Ansicht
 * @author Alexander Herzog
 */
public class StatisticViewerDashboard extends StatisticViewerSpecialBasePlain {
	/**
	 * Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	private final Statistics statistics;

	/**
	 * Elternelement der Kacheln
	 * @see #getViewerIntern()
	 * @see #rebuildViewer()
	 */
	private JPanel viewerContainer;

	/**
	 * Dürfen die Kacheln verändert werden?
	 * @see #testEditable()
	 */
	private boolean readOnly;

	/**
	 * Liste der Datensätze aus denen die Kacheln generiert werden
	 */
	private final List<StatisticViewerDashboardRecord> list;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerDashboard(final Statistics statistics) {
		this.statistics=statistics;
		list=new ArrayList<>();
	}

	/**
	 * Lädt die Datensätze aus dem Setup.
	 */
	private void loadSetup() {
		list.clear();
		if (!loadFromList(SetupData.getSetup().dashboardSetup)) addDefaultsToList();
	}

	/**
	 * Speichert die veränderten Datensätze im Setup.
	 */
	private void saveSetup() {
		final SetupData setup=SetupData.getSetup();
		final List<String> lines=saveToList();
		setup.dashboardSetup.clear();
		setup.dashboardSetup.addAll(lines);
		setup.saveSetup();
	}

	/**
	 * Fügt die Vorgabe-Datensätze zur Liste aller Datensätze hinzu.
	 * @see #list
	 */
	private void addDefaultsToList() {
		final String mean="["+Language.tr("Statistics.XML.Mean")+"]";
		final String value="["+Language.tr("Statistics.XML.Value")+"]";

		StatisticViewerDashboardRecord record;

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(Color.LIGHT_GRAY);
		record.setXMLData(Language.tr("Statistics.XML.Element.ClientsInSystemWaiting")+mean);
		record.setPreText("E[NQ]=");

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(Color.LIGHT_GRAY);
		record.setXMLData(Language.tr("Statistics.XML.Element.ClientsInSystem")+mean);
		record.setPreText("E[N]=");

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(new Color(255,165,0));
		record.setXMLData(Language.tr("Statistics.XML.Element.WaitingAllClients")+mean);
		record.setPreText("E[W]=");
		record.setPostText(" "+Language.tr("Statistic.Seconds"));

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(new Color(255,165,0));
		record.setXMLData(Language.tr("Statistics.XML.Element.ProcessAllClients")+mean);
		record.setPreText("E[S]=");
		record.setPostText(" "+Language.tr("Statistic.Seconds"));

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(new Color(255,165,0));
		record.setXMLData(Language.tr("Statistics.XML.Element.ResidenceAllClients")+mean);
		record.setPreText("E[V]=");
		record.setPostText(" "+Language.tr("Statistic.Seconds"));

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(Color.MAGENTA);
		record.setXMLData(Language.tr("Statistics.XML.Element.UtilizationAll")+mean);
		record.setPreText("E[busy]=");

		list.add(record=new StatisticViewerDashboardRecord());
		record.setBackgroundColor(Color.MAGENTA);
		record.setXMLData(Language.tr("Statistics.XML.Element.UtilizationResourceRhoAll")+value);
		record.setPreText("rho=");
		record.setFormat(StatisticViewerDashboardRecord.Format.PERCENT);
	}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_SPECIAL;
	}

	@Override
	public Container getViewerIntern() {
		loadSetup();

		viewerContainer=new JPanel();
		viewerContainer.setLayout(new WrapLayout(FlowLayout.LEFT));

		rebuildViewer();

		final JScrollPane scroll=new JScrollPane(viewerContainer,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		viewerContainer.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension max = new Dimension(scroll.getWidth(),Short.MAX_VALUE);
				viewerContainer.setMaximumSize(max);
				viewerContainer.revalidate();
				viewerContainer.repaint();
			}
		});

		SwingUtilities.invokeLater(()->testEditable());

		return scroll;
	}

	/**
	 * Prüft, ob es sich um einen normalen (editierbaren) Viewer oder um
	 * eine (in Bezug auf die Einstellungen schreibgeschützte) Darstellung
	 * von Vorgängerwerten handelt.
	 * @see #readOnly
	 * @see #getViewerIntern()
	 * @see #rebuildViewer()
	 */
	private void testEditable() {
		final StatisticsBasePanel statisticPanel=getParentStatisticPanel(viewerContainer);
		if (statisticPanel==null) {
			final Timer timer=new Timer(100,e->testEditable());
			timer.setRepeats(false);
			timer.start();
			return;
		}
		if (!statisticPanel.isDataViewer(this)) {
			readOnly=true;
			rebuildViewer();
		}
	}

	/**
	 * Erzeugt die Liste der Kacheln aus der Datensatzliste.
	 * @see #list
	 * @see #viewerContainer
	 */
	private void rebuildViewer() {
		if (viewerContainer==null) return;

		final boolean isNewViewer=(viewerContainer.getComponentCount()==0);
		if (!isNewViewer) viewerContainer.removeAll();

		final List<ScriptHelperRecord> titleRecords=StatisticViewerDashboardRecord.getTitleRecords(statistics);
		final Consumer<StatisticViewerDashboardRecord> moveUp=readOnly?null:record->recordMoveUp(record);
		final Consumer<StatisticViewerDashboardRecord> moveDown=readOnly?null:record->recordMoveDown(record);
		final Consumer<StatisticViewerDashboardRecord> edit=readOnly?null:record->recordEdit(record);
		final BiConsumer<StatisticViewerDashboardRecord,Boolean> remove=readOnly?null:(record,noConfirm)->recordRemove(record,noConfirm);

		for (int i=0;i<list.size();i++) {
			final JPanel viewer=list.get(i).getViewer(statistics,titleRecords,(i>0)?moveUp:null,(i<list.size()-1)?moveDown:null,edit,remove);
			viewerContainer.add(viewer);
		}

		SwingUtilities.invokeLater(()->{
			viewerContainer.setVisible(false);
			viewerContainer.setVisible(true);
			viewerContainer.invalidate();
		});
	}

	/**
	 * Zeigt einen Bearbeitendialog an.
	 * @param record	Zu bearbeitender Datensatz
	 * @return	Liefert <code>true</code>, wenn der Dialog mit "Ok" geschlossen wurde.
	 */
	private boolean showEditDialog(final StatisticViewerDashboardRecord record) {
		final StatisticViewerDashboardRecordDialog dialog=new StatisticViewerDashboardRecordDialog(viewerContainer,statistics,record);
		return dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK;
	}

	/**
	 * Befehl: Verschiebt den aktuellen Datensatz um eine Position nach vorne.
	 * @param record	Zu verschiebender Datensatz
	 */
	private void recordMoveUp(final StatisticViewerDashboardRecord record) {
		final int index=list.indexOf(record);
		if (index<1) return;
		final StatisticViewerDashboardRecord record2=list.get(index-1);

		list.set(index-1,record);
		list.set(index,record2);

		rebuildViewer();
		saveSetup();
	}

	/**
	 * Befehl: Verschiebt den aktuellen Datensatz um eine Position nach hinten.
	 * @param record	Zu verschiebender Datensatz
	 */
	private void recordMoveDown(final StatisticViewerDashboardRecord record) {
		final int index=list.indexOf(record);
		if (index<0 || index>=list.size()-1) return;
		final StatisticViewerDashboardRecord record2=list.get(index+1);

		list.set(index+1,record);
		list.set(index,record2);

		rebuildViewer();
		saveSetup();
	}

	/**
	 * Befehl: Fügt einen neuen Datensatz zur Liste hinzu.
	 * @return	Liefert <code>true</code>, wenn ein neuer Datensatz hinzugefügt wurde
	 */
	private boolean recordAdd() {
		final StatisticViewerDashboardRecord record=new StatisticViewerDashboardRecord();
		if (!showEditDialog(record)) return false;
		list.add(record);

		rebuildViewer();
		saveSetup();
		return true;
	}

	/**
	 * Befehl: Zeigt den Dialog zum Bearbeiten des aktuellen Datensatzes an.
	 * @param record	Zu bearbeitender Datensatz
	 */
	private void recordEdit(final StatisticViewerDashboardRecord record) {
		if (!showEditDialog(record)) return;

		rebuildViewer();
		saveSetup();
	}

	/**
	 * Befehl: Löscht den aktuellen Datensatz.
	 * @param record	Zu löschender Datensatz
	 * @param noConfirm	Soll eine Sicherheitsabfrage angezeigt werden (<code>false</code>) oder soll der Datensatz direkt gelöscht werden (<code>true</code>)?
	 */
	private void recordRemove(final StatisticViewerDashboardRecord record, final boolean noConfirm) {
		if (!noConfirm) {
			if (!MsgBox.confirm(viewerContainer,Language.tr("Statistics.Dashboard.DeleteTile.Title"),Language.tr("Statistics.Dashboard.DeleteTile.Info"),Language.tr("Statistics.Dashboard.DeleteTile.InfoYes"),Language.tr("Statistics.Dashboard.DeleteTile.InfoNo"))) return;
		}

		list.remove(record);

		rebuildViewer();
		saveSetup();
	}

	/**
	 * Befehl: Alle Kacheln auf Standardwerte zurücksetzen.
	 * @return	Liefert <code>true</code>, wenn die Einstellungen zurückgesetzt wurden
	 */
	private boolean recordReset() {
		if (!MsgBox.confirm(viewerContainer,Language.tr("Statistics.Dashboard.ResetTiles.Title"),Language.tr("Statistics.Dashboard.ResetTiles.Info"),Language.tr("Statistics.Dashboard.ResetTiles.InfoYes"),Language.tr("Statistics.Dashboard.ResetTiles.InfoNo"))) return false;

		list.clear();
		addDefaultsToList();

		rebuildViewer();
		saveSetup();
		return true;
	}

	@Override
	public String[] ownSettingsName() {
		return new String[] {Language.tr("Statistics.Dashboard.AddTile"),Language.tr("Statistics.Dashboard.ResetTiles"),Language.tr("Statistics.Dashboard.ImportTiles"),Language.tr("Statistics.Dashboard.ExportTiles")};
	}

	@Override
	public Icon[] ownSettingsIcon() {
		return new Icon[] {Images.EDIT_ADD.getIcon(),Images.EDIT_UNDO.getIcon(),Images.GENERAL_LOAD.getIcon(),Images.GENERAL_SAVE.getIcon()};
	}

	/**
	 * Lädt das Kachel-Setup aus einer Liste.
	 * @param lines	Liste mit den Daten zu den Kacheln
	 * @return	Liefert <code>true</code>, wenn die Daten geladen werden konnten
	 */
	private boolean loadFromList(final List<String> lines) {
		if (lines==null) return false;
		int nr=1;
		final List<StatisticViewerDashboardRecord> newList=new ArrayList<>();
		while (nr<lines.size()) {
			final StatisticViewerDashboardRecord newRecord=StatisticViewerDashboardRecord.loadFromString(lines,nr);
			nr+=StatisticViewerDashboardRecord.LINES_PER_RECORD;
			if (newRecord!=null) newList.add(newRecord);
		}

		if (newList.size()==0) return false;
		list.clear();
		list.addAll(newList);
		return true;
	}

	/**
	 * Lädt das Kachel-Setup aus einer Datei.
	 * @return	Liefert <code>true</code>, wenn das Laden erfolgreich durchgeführt werden konnte
	 */
	private boolean loadFromFile() {
		final String fileName=ScriptTools.selectTextFile(viewerContainer,null,null);
		if (fileName==null) return false;
		final File file=new File(fileName);

		try (Stream<String> linesStream=Files.lines(file.toPath())) {
			final List<String> lines=linesStream.collect(Collectors.toList());
			if (!loadFromList(lines)) return false;
			rebuildViewer();
			saveSetup();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Speichert das Kachel-Setup in einer Liste.
	 * @return	Liste mit den Daten zu den Kacheln
	 */
	private List<String> saveToList() {
		final List<String> lines=new ArrayList<>();
		lines.add(MainFrame.PROGRAM_NAME+" - Dashboard");
		for (StatisticViewerDashboardRecord record: list) record.storeToString(lines);
		return lines;
	}

	/**
	 * Speichert das Kachel-Setup in einer Datei.
	 * @return	Liefert <code>true</code>, wenn die Einstellungen erfolgreich gespeichert werden konnten
	 */
	private boolean saveToFile() {
		final String fileName=ScriptTools.selectTextSaveFile(viewerContainer,null,null);
		if (fileName==null) return false;
		final File file=new File(fileName);
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(viewer,file)) return false;
		}

		final String text=String.join("\n",saveToList());
		try {
			Files.write(file.toPath(),text.getBytes(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		switch (nr) {
		case 0:
			return recordAdd();
		case 1:
			return recordReset();
		case 2:
			return loadFromFile();
		case 3:
			saveToFile();
			return false;
		}

		return false;
	}
}
