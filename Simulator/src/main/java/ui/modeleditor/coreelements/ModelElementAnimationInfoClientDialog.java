/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;

/**
 * Zeigt alle Daten zu einem einzelnen wartenden Kunden an
 * @author Alexander Herzog
 * @see ModelElementAnimationInfoDialog
 */
public class ModelElementAnimationInfoClientDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1193622479235152775L;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param clientInfo	Daten zu dem anzuzeigenden Kunden
	 */
	public ModelElementAnimationInfoClientDialog(final Component owner, final ModelElementAnimationInfoDialog.ClientInfo clientInfo) {
		super(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Title"));

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs);

		JPanel tabOuter;
		JPanel tab;
		JTable table;

		/* Tab "Allgemeines" */
		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.RunningNumber"),""+clientInfo.number);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ClientType"),""+clientInfo.typeName,"id="+clientInfo.typeId);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Icon"),clientInfo.getIcon(new AnimationImageSource()));
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.IsWarmUp"),clientInfo.isWarmUp);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.InStatistics"),!clientInfo.isWarmUp && clientInfo.inStatistics);
		if (clientInfo.batch>0) addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.BatchSize"),""+clientInfo.batch);

		/* Tab "Zeiten" */
		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Waiting"),TimeTools.formatExactTime(clientInfo.waitingTime),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"));
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Transfer"),TimeTools.formatExactTime(clientInfo.transferTime),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"));
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Process"),TimeTools.formatExactTime(clientInfo.processTime),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"));
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Residence"),TimeTools.formatExactTime(clientInfo.residenceTime),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"));

		/* Tab "Kosten" */
		if (clientInfo.waitingCosts!=0.0 || clientInfo.transferCosts!=0.0 || clientInfo.processCosts!=0.0) {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs"),tabOuter=new JPanel(new BorderLayout()));
			tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
			tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
			addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Waiting"),NumberTools.formatNumber(clientInfo.waitingCosts));
			addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Transfer"),NumberTools.formatNumber(clientInfo.transferCosts));
			addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Process"),NumberTools.formatNumber(clientInfo.processCosts));
		}

		/* Tab "Numerische Datenfelder" */
		if (clientInfo.clientData.size()>0) {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Number.Short"),tabOuter=new JPanel(new BorderLayout()));
			tabOuter.add(new JScrollPane(table=new JTable(new DefaultTableModel(processClientNumbers(clientInfo.clientData),new Vector<>(Arrays.asList(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Table.Index"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Table.Value")))))));
			table.setEnabled(false);
		}

		/* Tab "Textbasierte Datenfelder" */
		if (clientInfo.clientTextData.size()>0) {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Text.Short"),tabOuter=new JPanel(new BorderLayout()));
			tabOuter.add(new JScrollPane(table=new JTable(new DefaultTableModel(processClientTexts(clientInfo.clientTextData),new Vector<>(Arrays.asList(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Table.Key"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Table.Value")))))));
			table.setEnabled(false);
		}

		/* Icons auf den Tabs */
		int nr=0;
		tabs.setIconAt(nr++,Images.GENERAL_INFO.getIcon());
		tabs.setIconAt(nr++,Images.MODELEDITOR_ELEMENT_ANIMATION_CLOCK.getIcon());
		if (clientInfo.waitingCosts!=0.0 || clientInfo.transferCosts!=0.0 || clientInfo.processCosts!=0.0) tabs.setIconAt(nr++,Images.MODELEDITOR_ELEMENT_COSTS.getIcon());
		if (clientInfo.clientData.size()>0) tabs.setIconAt(nr++,Images.GENERAL_NUMBERS.getIcon());
		if (clientInfo.clientTextData.size()>0) tabs.setIconAt(nr++,Images.GENERAL_FONT.getIcon());

		/* Dialog starten */
		pack();
		setMinSizeRespectingScreensize(600,400);
		setLocationRelativeTo(getOwner());
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Erzeugt eine Ausgabezeile
	 * @param parent	Übergeordnetes Element in das die neue Zeile eingefügt werden soll
	 * @param name	Name des Datenfeldes
	 * @param value	Wert des Datenfeldes
	 */
	private void addInfo(final JPanel parent, final String name, final String value) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		line.add(new JLabel("<html><body>"+name+": <b>"+value+"</b></body></html>"));
	}

	/**
	 * Erzeugt eine Ausgabezeile
	 * @param parent	Übergeordnetes Element in das die neue Zeile eingefügt werden soll
	 * @param name	Name des Datenfeldes
	 * @param value	Wert des Datenfeldes
	 * @param info	Zusätzliche Informationen zur Anzeige hinter dem Wert
	 */
	private void addInfo(final JPanel parent, final String name, final String value, final String info) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		line.add(new JLabel("<html><body>"+name+": <b>"+value+"</b> ("+info+")</body></html>"));
	}

	/**
	 * Erzeugt eine ja/nein-Ausgabezeile
	 * @param parent	Übergeordnetes Element in das die neue Zeile eingefügt werden soll
	 * @param name	Name des Datenfeldes
	 * @param status	Anzuzeigender ja/nein-Wert
	 */
	private void addInfo(final JPanel parent, final String name, final boolean status) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		final JCheckBox checkBox=new JCheckBox("",status);
		checkBox.setEnabled(false);
		line.add(checkBox);
		line.add(new JLabel(name));
	}

	/**
	 * Erzeugt eine Icon-Ausgabezeile
	 * @param parent	Übergeordnetes Element in das die neue Zeile eingefügt werden soll
	 * @param name	Name des Datenfeldes
	 * @param icon	Anzuzeigendes Icon
	 */
	private void addInfo(final JPanel parent, final String name, final Icon icon) {
		if (icon==null) return;
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		line.add(new JLabel(name+": "));
		line.add(new JLabel(icon));
	}

	/**
	 * Erzeugt die Tabellendaten für die Anzeige der numerischen Kundendaten
	 * @param clientData	Numerische Kundendaten
	 * @return	Tabellendaten
	 */
	private Vector<Vector<String>> processClientNumbers(final Map<Integer,Double> clientData) {
		final int[] indices=clientData.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray();

		final Vector<Vector<String>> list=new Vector<>();

		for (int i: indices) {
			final Vector<String> line=new Vector<>(2);
			line.add(""+i);
			line.add(NumberTools.formatNumberMax(clientData.get(i)));
			list.add(line);
		}

		return list;

	}

	/**
	 * Erzeugt die Tabellendaten für die Anzeige der textbasierten Kundendaten
	 * @param clientTextData	Textbasierende Kundendaten
	 * @return	Tabellendaten
	 */
	private Vector<Vector<String>> processClientTexts(final Map<String,String> clientTextData) {
		final String[] keys=clientTextData.keySet().stream().sorted().toArray(String[]::new);

		final Vector<Vector<String>> list=new Vector<>();

		for (String key: keys) {
			final Vector<String> line=new Vector<>(2);
			line.add(""+key);
			line.add(clientTextData.get(key));
			list.add(line);
		}

		return list;
	}
}
