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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import tools.SetupData;
import ui.ModelViewerFrame;
import ui.images.Images;
import xml.XMLTools;

/**
 * Tabellendaten für eine Tabelle zur Darstellung der Parameter-Vergleichs-Modelle
 * @author Alexander Herzog
 * @see ParameterCompareTable
 */
public class ParameterCompareTableModel extends JTableExtAbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2832353596146116451L;

	/** Anzahl an Nachkommastellen (mögliche Werte sind 1, 3 und 9 für Maximalanzahl) */
	private int digits;
	/** Zugehörige Tabelle */
	private final JTableExt table;
	/** Parameter-Vergleichs-Einstellungen */
	private final ParameterCompareSetup setup;
	/** Hilfe-Runnable */
	private final Runnable help;
	/** Runnable, das aufgerufen wird, wenn sich die Tabellendaten verändert haben */
	private final Runnable update;
	/** Wird aufgerufen, wenn der Nutzer die Funktion zum Laden eines Modells aus den Ergebnissen in den Editor gewählt hat. */
	private final Consumer<Statistics> loadToEditor;
	/** Wird aufgerufen, wenn der Nutzer den Button zum Vergleichen der Statistikergebnisse verschiedener Modell anklickt */
	private final Runnable compareResults;
	/** Wird aufgerufen, wenn der Nutzer auf eine Schaltfläche in der letzten Zeile (zur Anzeige der Vergleichsdiagramme) klickt */
	private final Consumer<Integer> showResultsChart;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugehörige Tabelle
	 * @param setup	Parameter-Vergleichs-Einstellungen
	 * @param help	Hilfe-Runnable
	 * @param update	Runnable, das aufgerufen wird, wenn sich die Tabellendaten verändert haben
	 * @param loadToEditor	Wird aufgerufen, wenn der Nutzer die Funktion zum Laden eines Modells aus den Ergebnissen in den Editor gewählt hat.
	 * @param compareResults	Wird aufgerufen, wenn der Nutzer den Button zum Vergleichen der Statistikergebnisse verschiedener Modell anklickt
	 * @param showResultsChart	Wird aufgerufen, wenn der Nutzer auf eine Schaltfläche in der letzten Zeile (zur Anzeige der Vergleichsdiagramme) klickt
	 */
	public ParameterCompareTableModel(final JTableExt table, final ParameterCompareSetup setup, final Runnable help, final Runnable update, final Consumer<Statistics> loadToEditor, final Runnable compareResults, final Consumer<Integer> showResultsChart) {
		super();
		digits=1;
		this.table=table;
		this.setup=setup;
		this.help=help;
		this.update=update;
		this.loadToEditor=loadToEditor;
		this.compareResults=compareResults;
		this.showResultsChart=showResultsChart;
	}

	/**
	 * Aktualisiert die Tabelle, nach dem Änderungen an den Einstellungen vorgenommen wurden.
	 */
	public synchronized void updateTable() {
		fireTableDataChanged();
		fireTableStructureChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	/**
	 * Aktualisiert die Tabelle, nach dem Änderungen an den Daten vorgenommen wurden.
	 * @param row	Zu aktualisierende Zeile
	 */
	public synchronized void updateTableContentOnly(final int row) {
		SwingUtilities.invokeLater(()->{
			for (int column=1;column<getColumnCount()-1;column++) fireTableCellUpdated(row,column);

			TableCellEditor cellEditor=table.getCellEditor();
			if (cellEditor!=null) cellEditor.stopCellEditing();

			table.invalidate();
		});
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public int getRowCount() {
		return setup.getModels().size()+1;
	}

	@Override
	public int getColumnCount() {
		return 1+setup.getInput().size()+setup.getOutput().size()+1;
	}

	@Override
	public String getColumnName(int column) {
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");

		boolean done=false;

		/* Modellname */

		if (column==0) {
			sb.append("<b>"+Language.tr("ParameterCompare.Table.Column.Model")+"</b>");
			done=true;
		}

		/* Parameter */

		if (!done) {
			column-=1;
			if (column<setup.getInput().size()) {
				sb.append(Language.tr("ParameterCompare.Table.Column.Input")+"<br>");
				sb.append("<b>"+setup.getInput().get(column).getName()+"</b>");
				done=true;
			}
		}

		/* Ausgabegrößen */

		if (!done) {
			column-=setup.getInput().size();
			if (column<setup.getOutput().size()) {
				sb.append(Language.tr("ParameterCompare.Table.Column.Output")+"<br>");
				sb.append("<b>"+setup.getOutput().get(column).getName()+"</b>");
				done=true;
			}
		}

		/* Steuerung */

		if (!done) {
			sb.append("<b>"+Language.tr("ParameterCompare.Table.Column.Control")+"</b>");
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Erstellt eine neue Schaltfläche
	 * @param title	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche
	 * @param icon	Icon für die Schaltfläche
	 * @param command	Beim Anklicken der Schaltfläche auszuführender Befehl
	 * @return	Neu erstellte Schaltfläche
	 */
	private JButton getButton(final String title, final String hint, final Icon icon, final Runnable command) {
		final JButton button=new JButton(title);
		button.setToolTipText(hint);
		button.addActionListener(e->command.run());
		if (icon!=null) button.setIcon(icon);

		return button;
	}

	private JPanel[] lastRow=null;
	private List<JButton> lastRowChartButtons=new ArrayList<>();

	private Object getValueInLastRow(int columnIndex) {
		if (columnIndex==getColumnCount()-1) return getValueInLastCol(null,-1);

		if (lastRow==null || lastRow.length!=getColumnCount()) {
			lastRow=new JPanel[getColumnCount()];
			lastRowChartButtons.clear();
		}
		final boolean hasStatistics=hasResults();
		for (JButton button: lastRowChartButtons) button.setEnabled(hasStatistics);

		if (lastRow[columnIndex]!=null) return lastRow[columnIndex];

		final JPanel panel=new JPanel(new BorderLayout());
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		panel.add(toolbar,BorderLayout.CENTER);

		if (columnIndex==0) {
			toolbar.add(getButton("",Language.tr("ParameterCompare.Table.AddModel.Hint"),Images.EDIT_ADD.getIcon(),()->commandAdd()));
			toolbar.add(getButton("",Language.tr("ParameterCompare.Table.AddModelByAssistant.Hint"),Images.PARAMETERSERIES_ADD_BY_ASSISTANT.getIcon(),()->commandAddByAssistant()));
			toolbar.add(getButton("",Language.tr("ParameterCompare.Table.SortModels.Hint"),Images.PARAMETERSERIES_SORT_TABLE.getIcon(),()->commandSortByInputParameter()));
		}

		if (columnIndex>setup.getInput().size() && columnIndex<lastRow.length-1) {
			final int nr=columnIndex-setup.getInput().size()-1;
			JButton b=getButton("",Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart"),Images.PARAMETERSERIES_PROCESS_RESULTS_CHARTS.getIcon(),()->commandShowResultsChart(nr));
			toolbar.add(b);
			lastRowChartButtons.add(b);
			b.setEnabled(hasStatistics);
		}

		return lastRow[columnIndex]=panel;
	}

	private boolean hasResults() {
		for (ParameterCompareSetupModel model: setup.getModels()) if (model.isStatisticsAvailable()) return true;
		return false;
	}

	private Map<Integer,LastRowPanel> panelCache=new HashMap<>();
	private int panelCacheRows=-1;

	private LastRowPanel getValueInLastCol(final ParameterCompareSetupModel model, int rowIndex) {
		if (panelCacheRows!=getRowCount()) {panelCache.clear(); panelCacheRows=getRowCount();}
		LastRowPanel panel=panelCache.get(rowIndex);
		if (panel!=null && panel.panelModel!=model) panel=null;

		if (panel!=null) {
			panel.updateButtons();
		} else {
			panel=new LastRowPanel(model,rowIndex);
			panelCache.put(rowIndex,panel);
		}

		return panel;
	}

	private Object getValueInFirstColumn(final ParameterCompareSetupModel model, int rowIndex) {
		return getValueInLastCol(model,rowIndex).getFirstColumn();
	}

	private ImageIcon busyIcon=null;

	private JLabel getBusyMarker(final int percent) {
		final JLabel label;
		if (percent>0) {
			label=new JLabel(" "+percent+"%");
		} else {
			label=new JLabel();
		}

		if (busyIcon==null) {
			busyIcon=new ImageIcon(Images.GENERAL_WAIT_INDICATOR.getURL());
			busyIcon.setImageObserver(table); /* sonst bleibt die Animation stehen, wenn die Zelle den Fokus verliert */
		}

		label.setIcon(busyIcon);
		label.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));

		return label;
	}

	private String formatNumber(final double value) {
		if (digits==1) return NumberTools.formatNumber(value,1);
		if (digits==3) return NumberTools.formatNumber(value,3);
		return NumberTools.formatNumberMax(value);
	}

	private Object getValueAt(final ParameterCompareSetupModel model, int rowIndex, int columnIndex) {
		/* Modellname */

		if (columnIndex==0) {
			final StringBuilder sb=new StringBuilder();
			sb.append("<html><body>");
			sb.append(Language.tr("ParameterCompare.Table.Column.Model")+"<br>");
			sb.append("<b>"+model.getName()+"</b>");
			sb.append("</body></html>");
			return sb.toString();
		}

		/* Parameter */

		columnIndex--;

		if (columnIndex<setup.getInput().size()) {
			final String name=setup.getInput().get(columnIndex).getName();
			final Double value=model.getInput().get(name);
			if (value==null) return "-";
			return formatNumber(value.doubleValue());
		}

		/* Ausgabegrößen */

		columnIndex-=setup.getInput().size();

		if (columnIndex<setup.getOutput().size()) {
			final int process=model.isInProcess();
			if (process>=0) {
				return getBusyMarker(process);
			} else {
				final String name=setup.getOutput().get(columnIndex).getName();
				final Double value=model.getOutput().get(name);
				if (value==null) return "-";
				if (setup.getOutput().get(columnIndex).getIsTime()) {
					return TimeTools.formatExactTime(value.doubleValue());
				} else {
					return formatNumber(value.doubleValue());
				}
			}
		}

		/* Steuerung */

		return getValueInLastCol(model,rowIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==getRowCount()-1) return getValueInLastRow(columnIndex);
		if (columnIndex==0) return getValueInFirstColumn(setup.getModels().get(rowIndex),rowIndex);
		return getValueAt(setup.getModels().get(rowIndex),rowIndex,columnIndex);
	}

	private boolean editModel(final ParameterCompareSetupModel simModel) {
		final ParameterCompareSetupModelDialog dialog=new ParameterCompareSetupModelDialog(table,simModel,setup.getInput(),help);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return false;
		simModel.clearOutputs();
		return true;
	}

	private void commandAdd() {
		final ParameterCompareSetupModel simModel=new ParameterCompareSetupModel();
		if (!editModel(simModel)) return;
		setup.getModels().add(simModel);
		if (update==null) updateTable(); else update.run();
	}

	private boolean commandEdit(final ParameterCompareSetupModel simModel, final int rowIndex) {
		if (!editModel(simModel)) return false;
		panelCache.remove(rowIndex);
		if (update==null) updateTable(); else update.run();
		return true;
	}

	private void commandDelete(final int index, final ParameterCompareSetupModel simModel) {
		if (simModel==null) {
			if (!MsgBox.confirm(table,Language.tr("ParameterCompare.Table.DeleteModel.Confirm.Title"),Language.tr("ParameterCompare.Table.DeleteModel.Confirm.InfoAll"),Language.tr("ParameterCompare.Table.DeleteModel.Confirm.InfoAllYes"),Language.tr("ParameterCompare.Table.DeleteModel.Confirm.InfoAllNo"))) return;
			setup.getModels().clear();
		} else {
			if (!MsgBox.confirm(table,Language.tr("ParameterCompare.Table.DeleteModel.Confirm.Title"),String.format(Language.tr("ParameterCompare.Table.DeleteModel.Confirm.Info"),simModel.getName()),Language.tr("ParameterCompare.Table.DeleteModel.Confirm.InfoYes"),Language.tr("ParameterCompare.Table.DeleteModel.Confirm.InfoNo"))) return;
			setup.getModels().remove(index);
		}
		if (update==null) updateTable(); else update.run();
	}

	private void commandMoveUp(final int index) {
		Collections.swap(setup.getModels(),index,index-1);
		if (update==null) updateTable(); else update.run();
	}

	private void commandMoveDown(final int index) {
		Collections.swap(setup.getModels(),index,index+1);
		if (update==null) updateTable(); else update.run();
	}

	private void commandAddByAssistant() {
		if (setup.getInput().size()==0) {
			MsgBox.error(table,Language.tr("ParameterCompare.Table.AddModelByAssistant.ErrorNoInput.Title"),Language.tr("ParameterCompare.Table.AddModelByAssistant.ErrorNoInput.Info"));
			return;
		}

		final ParameterCompareAssistantDialog dialog=new ParameterCompareAssistantDialog(table,setup,help);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (update==null) updateTable(); else update.run();
		}
	}

	private void commandSortByInputParameter() {
		if (setup.getInput().size()==0) {
			MsgBox.error(table,Language.tr("ParameterCompare.Table.SortModels.ErrorNoInput.Title"),Language.tr("ParameterCompare.Table.SortModels.ErrorNoInput.Info"));
			return;
		}

		if (setup.getModels().size()==0) {
			MsgBox.error(table,Language.tr("ParameterCompare.Table.SortModels.ErrorNoModels.Title"),Language.tr("ParameterCompare.Table.SortModels.ErrorNoModels.Info"));
			return;
		}

		final ParameterCompareSortDialog dialog=new ParameterCompareSortDialog(table,setup,help);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (update==null) updateTable(); else update.run();
		}
	}

	private void commandShowStatistics(final int index) {
		if (index<0) {
			if (compareResults!=null) compareResults.run();
			return;
		}

		final Statistics statistics=setup.getModels().get(index).getStatistics();
		if (statistics==null) return;

		Container c=table.getParent();
		while (c!=null && !(c instanceof Window)) c=c.getParent();

		final ModelViewerFrame viewer=new ModelViewerFrame((Window)c,statistics.editModel,statistics,()->{
			if (loadToEditor!=null) loadToEditor.accept(statistics);
		});

		viewer.setVisible(true);
	}

	private void commandSaveStatistics(final int index) {
		final Statistics statistics=setup.getModels().get(index).getStatistics();

		final File file=XMLTools.showSaveDialog(table.getTopLevelAncestor(),Language.tr("Main.Toolbar.SaveStatistics"),SetupData.getSetup().defaultSaveFormatStatistics);
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(table.getTopLevelAncestor(),file)) return;
		}

		if (!statistics.saveToFile(file)) {
			MsgBox.error(table,Language.tr("XML.SaveErrorTitle"),Language.tr("Main.Statistic.ErrorSaving"));
		}
	}

	private void commandShowResultsChart(final int index) {
		if (showResultsChart!=null) showResultsChart.accept(index);
	}

	/**
	 * Stellt die Anzahl an anzuzeigenden Nachkommastellen ein (wirkt sich nicht auf Kopieren/Speichern der Tabelle).
	 * @param digits	Anzahl an Nachkommastellen (mögliche Werte sind 1, 3 und 9 für Maximalanzahl)
	 */
	public void setDisplayDigits(final int digits) {
		this.digits=digits;
		table.invalidate();
	}

	private class LastRowPanel extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -4461230060003315302L;

		public final ParameterCompareSetupModel panelModel;

		private final int rowIndex;
		private JButton[] buttons;

		public LastRowPanel(final ParameterCompareSetupModel model, final int rowIndex) {
			super();
			panelModel=model;
			this.rowIndex=rowIndex;
			buttons=new JButton[5];

			setLayout(new FlowLayout(FlowLayout.LEFT));

			JButton b;

			final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
			toolbar.setFloatable(false);
			add(toolbar);

			toolbar.add(b=getButton("",Language.tr("ParameterCompare.Table.EditModel.Hint"),Images.GENERAL_SETUP.getIcon(),()->commandEdit(panelModel,rowIndex)));
			buttons[0]=b;

			toolbar.add(getButton("",(panelModel==null)?Language.tr("ParameterCompare.Table.DeleteModel.HintAll"):Language.tr("ParameterCompare.Table.DeleteModel.Hint"),Images.EDIT_DELETE.getIcon(),()->commandDelete(rowIndex,model)));

			toolbar.add(b=getButton("",Language.tr("ParameterCompare.Table.MoveModelUp.Hint"),Images.ARROW_UP.getIcon(),()->commandMoveUp(rowIndex)));
			buttons[1]=b;

			toolbar.add(b=getButton("",Language.tr("ParameterCompare.Table.MoveModelDown.Hint"),Images.ARROW_DOWN.getIcon(),()->commandMoveDown(rowIndex)));
			buttons[2]=b;

			toolbar.add(b=getButton("",Language.tr("ParameterCompare.Table.ShowStatistics.Hint"),Images.PARAMETERSERIES_PROCESS_RESULTS_CHARTS.getIcon(),()->commandShowStatistics(rowIndex)));
			buttons[3]=b;

			if (rowIndex>=0) {
				toolbar.add(b=getButton("",Language.tr("ParameterCompare.Table.SaveStatistics.Hint"),Images.GENERAL_SAVE.getIcon(),()->commandSaveStatistics(rowIndex)));
				buttons[4]=b;
			}

			toolbar.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		}

		public void updateButtons() {
			buttons[0].setEnabled(panelModel!=null);
			buttons[1].setEnabled(panelModel!=null && rowIndex>0);
			buttons[2].setEnabled(panelModel!=null && rowIndex<setup.getModels().size()-1);
			buttons[3].setEnabled((panelModel==null && hasResults()) || (panelModel!=null && panelModel.isStatisticsAvailable()));
			if (rowIndex>=0) {
				buttons[4].setEnabled((panelModel==null && hasResults()) || (panelModel!=null && panelModel.isStatisticsAvailable()));
			}
		}

		private JPanel firstColumn;

		public JPanel getFirstColumn() {
			if (firstColumn==null) {
				firstColumn=new JPanel(new BorderLayout());
				final JLabel label=new JLabel(panelModel.getName());

				label.setIcon(Images.MODEL.getIcon());
				firstColumn.add(label,BorderLayout.CENTER);
				firstColumn.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			}
			return firstColumn;
		}
	}
}