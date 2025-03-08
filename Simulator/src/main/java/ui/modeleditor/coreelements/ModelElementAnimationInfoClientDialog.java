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
import java.awt.FontMetrics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.coreelements.RunElement;
import simulator.runmodel.RunModel;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;

/**
 * Zeigt alle Daten zu einem einzelnen Kunden an.
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
	 * Soll nach dem Schließen des Dialog der Kundendaten-Editor-Dialog geöffnet werden?
	 */
	private boolean showEditorDialog;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Simulationsmodell mit Informationen zu den Stationen usw.
	 * @param clientInfo	Daten zu dem anzuzeigenden Kunden
	 * @param isWaitingClientsList	Handelt es sich um einen noch wartenden Kunden?
	 * @param showEditButton	Soll angeboten werden, den Kundendaten-Editor-Dialog zu öffnen?
	 */
	public ModelElementAnimationInfoClientDialog(final Component owner, final RunModel model, final ModelElementAnimationInfoDialog.ClientInfo clientInfo, final boolean isWaitingClientsList, final boolean showEditButton) {
		super(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Title"));
		showEditorDialog=false;

		/* GUI */
		if (showEditButton) addUserButton(Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient"),Images.GENERAL_EDIT.getIcon());
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
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ID"),""+clientInfo.id);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ClientType"),""+clientInfo.typeName,"id="+clientInfo.typeId);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Icon"),clientInfo.getIcon(new AnimationImageSource()));

		if (clientInfo.currentPosition>=0 && clientInfo.currentPosition<model.elementsFast.length) {
			final RunElement station=model.elementsFast[clientInfo.currentPosition];
			if (station!=null) {
				final StringBuilder position=new StringBuilder();
				if (station.parentId>=0) {
					position.append(model.elementsFast[station.parentId].name);
					position.append(" - ");
				}
				position.append(station.name);
				addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.CurrentStation"),position.toString());
			}
		}

		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.IsWarmUp"),clientInfo.isWarmUp);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.InStatistics"),!clientInfo.isWarmUp && clientInfo.inStatistics);
		if (clientInfo.batch>0) {
			final StringBuilder batchInfo=new StringBuilder();
			for (int i=0;i<Math.min(3,clientInfo.batch);i++) {
				if (i>0) batchInfo.append(", ");
				batchInfo.append(clientInfo.batchTypeNames[i]);
			}
			if (clientInfo.batch>3) batchInfo.append(", ...");
			addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.BatchSize"),""+clientInfo.batch,batchInfo.toString());
		}

		/* Tab "Zeiten" */
		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		final String info=(isWaitingClientsList)?Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"):null;
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Waiting"),TimeTools.formatExactTime(clientInfo.waitingTime),info);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Transfer"),TimeTools.formatExactTime(clientInfo.transferTime),info);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Process"),TimeTools.formatExactTime(clientInfo.processTime),info);
		addInfo(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Residence"),TimeTools.formatExactTime(clientInfo.residenceTime),info);

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
			table.getTableHeader().setReorderingAllowed(false);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			autoSizeColumn(table,0,true);
			autoSizeColumn(table,1,true);
		}

		/* Tab "Textbasierte Datenfelder" */
		if (clientInfo.clientTextData.size()>0) {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Text.Short"),tabOuter=new JPanel(new BorderLayout()));
			tabOuter.add(new JScrollPane(table=new JTable(new DefaultTableModel(processClientTexts(clientInfo.clientTextData),new Vector<>(Arrays.asList(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Table.Key"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Table.Value")))))));
			table.setEnabled(false);
			table.getTableHeader().setReorderingAllowed(false);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			autoSizeColumn(table,0,true);
			autoSizeColumn(table,1,true);
		}

		/* Tab "Pfad" */
		if (clientInfo.path!=null && clientInfo.path.length>0) {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Path"),tabOuter=new JPanel(new BorderLayout()));
			final List<String> path=new ArrayList<>();
			for (int id: clientInfo.path) {
				if (model.elementsFast[id].parentId>=0) {
					path.add(model.elementsFast[model.elementsFast[id].parentId].name+" - "+model.elementsFast[id].name);
				} else {
					path.add(model.elementsFast[id].name);
				}
			}
			tabOuter.add(new JScrollPane(new JList<>(path.toArray(String[]::new))));
		}

		/* Icons auf den Tabs */
		int nr=0;
		tabs.setIconAt(nr++,Images.GENERAL_INFO.getIcon());
		tabs.setIconAt(nr++,Images.MODELEDITOR_ELEMENT_ANIMATION_CLOCK.getIcon());
		if (clientInfo.waitingCosts!=0.0 || clientInfo.transferCosts!=0.0 || clientInfo.processCosts!=0.0) tabs.setIconAt(nr++,Images.MODELEDITOR_ELEMENT_COSTS.getIcon());
		if (clientInfo.clientData.size()>0) tabs.setIconAt(nr++,Images.GENERAL_NUMBERS.getIcon());
		if (clientInfo.clientTextData.size()>0) tabs.setIconAt(nr++,Images.GENERAL_FONT.getIcon());
		if (clientInfo.path!=null && clientInfo.path.length>0) tabs.setIconAt(nr++,Images.MODELPROPERTIES_PATH_RECORDING.getIcon());

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
		if (name==null) {
			line.add(new JLabel("<html><body>"+value+"</body></html>"));
		} else {
			line.add(new JLabel("<html><body>"+name+": <b>"+value+"</b></body></html>"));
		}
	}

	/**
	 * Erzeugt eine Ausgabezeile
	 * @param parent	Übergeordnetes Element in das die neue Zeile eingefügt werden soll
	 * @param name	Name des Datenfeldes
	 * @param value	Wert des Datenfeldes
	 * @param info	Zusätzliche Informationen zur Anzeige hinter dem Wert (kann leer oder <code>null</code> sein)
	 */
	private void addInfo(final JPanel parent, final String name, final String value, final String info) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		if (info!=null && !info.isBlank()) {
			line.add(new JLabel("<html><body>"+name+": <b>"+value+"</b> ("+info+")</body></html>"));
		} else {
			line.add(new JLabel("<html><body>"+name+": <b>"+value+"</b></body></html>"));
		}
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

	/**
	 * Stellt die Breite einer Spalte basierend auf dem Inhalt der Spalte ein.
	 * @param table	Tabelle bei der die Breite einer Spalte eingestellt werden soll.
	 * @param columnIndex	0-basierter Spaltenindex
	 * @param includeHeader	Nur den Inhalt der Spalte (<code>false</code>) oder auch die Spaltenüberschrift (<code>true</code>) in die Berechnung der benötigten Breite mit einbeziehen.
	 */
	private void autoSizeColumn(final JTable table, final int columnIndex, final boolean includeHeader) {
		/* Spaltenbreite */
		int widthContent=0;
		final TableModel model=table.getModel();
		final FontMetrics fontMetrics=table.getFontMetrics(table.getFont());
		final int rowCount=table.getRowCount();
		for (int i=0;i<rowCount;i++) {
			widthContent=Math.max(widthContent,SwingUtilities.computeStringWidth(fontMetrics,(String)model.getValueAt(i,columnIndex)));
		}

		/* Überschriftenbreite */
		int widthHeader=0;
		if (includeHeader) {
			final JTableHeader header=table.getTableHeader();
			final String title=table.getColumnName(columnIndex);
			widthHeader=SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()),title);
		}

		/* Neue Breite einstellen */
		setColWidth(table,columnIndex,Math.max(widthContent,widthHeader));
	}

	/**
	 * Liefert die Spaltenabstände.
	 * @param table	Tabelle von der die Spaltenabstände berechnet werden sollen.
	 * @return	Spaltenabstände
	 */
	private int getSpacing(final JTable table) {
		return table.getIntercellSpacing().width+5; /* "+5"=Border+Inset */
	}

	/**
	 * Stellt die Spalte einer Breite ein.
	 * @param table	Tabelle bei der die Breite einer Spalte eingestellt werden soll.
	 * @param columnIndex	0-basierter Spaltenindex
	 * @param width	Neue Spaltenbreite
	 */
	private void setColWidth(final JTable table, final int columnIndex, final int width) {
		/* Abstände zwischen den Zellen */
		final int spacing=getSpacing(table);

		/* Neue Breite einstellen */
		final TableColumnModel columnModel=table.getColumnModel();
		final TableColumn column=columnModel.getColumn(columnIndex);
		column.setMinWidth(30);
		column.setPreferredWidth(width+2*spacing);
		column.setWidth(width+2*spacing);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		showEditorDialog=true;
		close(BaseDialog.CLOSED_BY_OK);
	}

	/**
	 * Soll nach dem Schließen des Dialog der Kundendaten-Editor-Dialog geöffnet werden?
	 * @return	Soll nach dem Schließen des Dialog der Kundendaten-Editor-Dialog geöffnet werden?
	 */
	public boolean getShowEditorDialog() {
		return showEditorDialog;
	}
}