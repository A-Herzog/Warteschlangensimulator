/**
 * Copyright 2024 Alexander Herzog
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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.Table;
import mathtools.distribution.tools.DistributionTools;
import systemtools.BaseDialog;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;

/**
 * Zeigt die von {@link ClientTypeLoader} geladenen Daten an
 * bevor diese übernommen werden.
 * @see ClientTypeLoader
 */
public class ClientTypeLoaderDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=3589229264277786668L;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element
	 */
	public ClientTypeLoaderDialog(final Component owner) {
		super(owner,Language.tr("ClientTypeLoader.Title"));
	}

	/**
	 * Modus "Bedienzeiten" aktivieren
	 * @param allClientTypes	Liste mit allen Kundentypen (auch die, für die bisher keine Werte gesetzt sind)
	 * @param oldProcessingTimes	Zuordnung mit den bisherigen Einstellungen
	 * @param newProcessingTimes	Zuordnung mit den neu geladenen Einstellungen
	 */
	public void initProcessTimes(final List<String> allClientTypes, final Map<String,Object> oldProcessingTimes, final Map<String,Object> newProcessingTimes) {
		final JPanel main=createGUI(800,800,null);
		main.setLayout(new BorderLayout());
		final JTableExt table=new JTableExt(new TableTableModel(new String[] {
				Language.tr("ClientTypeLoader.HeadingClientType"),
				Language.tr("ClientTypeLoader.HeadingCurrentValue"),
				Language.tr("ClientTypeLoader.HeadingNewValue")
		},buildProcessingTimesTable(allClientTypes,oldProcessingTimes,newProcessingTimes)));
		main.add(new JScrollPane(table));

		table.getColumnModel().getColumn(0).setMaxWidth(175);
		table.getColumnModel().getColumn(0).setMinWidth(175);

		start();
	}

	/**
	 * Modus "Rüstzeiten" aktivieren
	 * @param allClientTypes	Liste mit allen Kundentypen (auch die, für die bisher keine Werte gesetzt sind)
	 * @param oldSetupTimes	Zuordnung mit den bisherigen Einstellungen
	 * @param newSetupTimes	Zuordnung mit den neu geladenen Einstellungen
	 */
	public void initSetupTimes(final List<String> allClientTypes, final Map<String,Map<String,Object>> oldSetupTimes, final Map<String,Map<String,Object>> newSetupTimes) {
		final JPanel main=createGUI(800,800,null);
		main.setLayout(new BorderLayout());
		final JTableExt table=new JTableExt(new TableTableModel(new String[] {
				Language.tr("ClientTypeLoader.HeadingClientType")+" 1",
				Language.tr("ClientTypeLoader.HeadingClientType")+" 2",
				Language.tr("ClientTypeLoader.HeadingCurrentValue"),
				Language.tr("ClientTypeLoader.HeadingNewValue")
		},buildSetupTimesTable(allClientTypes,oldSetupTimes,newSetupTimes)));
		main.add(new JScrollPane(table));

		table.getColumnModel().getColumn(0).setMaxWidth(175);
		table.getColumnModel().getColumn(0).setMinWidth(175);
		table.getColumnModel().getColumn(1).setMaxWidth(175);
		table.getColumnModel().getColumn(1).setMinWidth(175);

		start();
	}

	/**
	 * Tabellenmodell, welches ein {@link Table}-Objekt anzeigt
	 * @see Table
	 * @see JTableExt
	 */
	private static class TableTableModel extends JTableExtAbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=7944804149259487607L;

		/**
		 * Überschriften
		 */
		private final String[] headings;

		/**
		 * Tabelleninhalt
		 */
		private final Table tableData;

		/**
		 * Konstruktor
		 * @param headings	Überschriften
		 * @param tableData	Tabelleninhalt
		 */
		public TableTableModel(final String[] headings, final Table tableData) {
			this.headings=headings;
			this.tableData=tableData;
		}

		@Override
		public String getColumnName(int column) {
			if (column<0 || column>=headings.length) return "";
			return headings[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return tableData.getValue(rowIndex,columnIndex);
		}

		@Override
		public int getRowCount() {
			return tableData.getSize(0);
		}

		@Override
		public int getColumnCount() {
			return tableData.getSize(1);
		}
	}

	/**
	 * Startet den Dialog.
	 */
	private void start() {
		setMinSizeRespectingScreensize(800,800);
		setSizeRespectingScreensize(800,800);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Erzeugt den Inhalt für eine Zelle
	 * @param obj	Einzutragendes Objekt: Verteilung, Rechenausdruck oder <code>null</code>
	 * @param fallback	Standardwert im Falle von <code>null</code>
	 * @return	Zelleninhalt
	 */
	private String buildCell(final Object obj, final String fallback) {
		if (obj instanceof AbstractRealDistribution) {
			return "<html><body>"+Language.tr("ClientTypeLoader.ModeProbabilityDistribution")+"<br><b>"+DistributionTools.distributionToString((AbstractRealDistribution)obj)+"</b></body></html>";
		}

		if (obj instanceof String) {
			return "<html><body>"+Language.tr("ClientTypeLoader.ModeCalculationExpression")+"<br><b>"+((String)obj)+"</b></body></html>";
		}

		return fallback;
	}

	/**
	 * Erzeugt eine Bedienzeitentabelle.
	 * @param allClientTypes	Liste mit allen Kundentypen (auch die, für die bisher keine Werte gesetzt sind)
	 * @param oldProcessingTimes	Zuordnung mit den bisherigen Einstellungen
	 * @param newProcessingTimes	Zuordnung mit den neu geladenen Einstellungen
	 * @return	Bedienzeitentabelle
	 */
	private Table buildProcessingTimesTable(final List<String> allClientTypes, final Map<String,Object> oldProcessingTimes, final Map<String,Object> newProcessingTimes) {
		final Table table=new Table();

		for (var name: allClientTypes.stream().sorted(String::compareToIgnoreCase).toArray(String[]::new)) {
			final String[] row=new String[3];
			row[0]=name;
			row[1]=buildCell(oldProcessingTimes.get(name),Language.tr("ClientTypeLoader.NoValueOld"));
			row[2]=buildCell(newProcessingTimes.get(name),Language.tr("ClientTypeLoader.NoValueNew"));

			table.addLine(row);
		}

		return table;
	}

	/**
	 * Erzeugt eine Rüstzeitentabelle.
	 * @param allClientTypes	Liste mit allen Kundentypen (auch die, für die bisher keine Werte gesetzt sind)
	 * @param oldSetupTimes	Zuordnung mit den bisherigen Einstellungen
	 * @param newSetupTimes	Zuordnung mit den neu geladenen Einstellungen
	 * @return	Rüstzeitentabelle
	 */
	private Table buildSetupTimesTable(final List<String> allClientTypes, final Map<String,Map<String,Object>> oldSetupTimes, final Map<String,Map<String,Object>> newSetupTimes) {
		final Table table=new Table();

		final String[] names=allClientTypes.stream().sorted(String::compareToIgnoreCase).toArray(String[]::new);

		for (var name1: names) for (var name2: names) {
			final String[] row=new String[4];
			row[0]=name1;
			row[1]=name2;
			row[2]=buildCell(oldSetupTimes.containsKey(name1)?oldSetupTimes.get(name1).get(name2):null,Language.tr("ClientTypeLoader.NoValueOld"));
			row[3]=buildCell(newSetupTimes.containsKey(name1)?newSetupTimes.get(name1).get(name2):null,Language.tr("ClientTypeLoader.NoValueNew"));

			table.addLine(row);
		}

		return table;
	}
}
