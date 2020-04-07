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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.coreelements.DataCheckResult;

/**
 * Pr�ft die Verkn�pfungen von Stationen zu externen Datenquellen
 * @author Alexander Herzog
 * @see EditModel#getDataCheckResults()
 * @see DataCheckResult
 */
public final class DataCheckDialog extends BaseDialog {
	private static final long serialVersionUID = 4022711184917153072L;

	private final EditModel model;
	private final ResultsTableModel tableData;
	private int numberOfExternalDataSources;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param model	Modell aus dem die Liste der Stationen ausgelesen wird
	 */
	public DataCheckDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("CheckData.Title"));
		this.model=model;

		showCloseButton=true;
		addUserButton(Language.tr("CheckData.Button.Recheck"),Images.DATA_CHECK.getURL());
		final JPanel content=createGUI(()->Help.topicModal(this,"CheckData"));
		content.setLayout(new BorderLayout());

		JTableExt table=new JTableExt();
		table.setModel(tableData=new ResultsTableModel());
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.getColumnModel().getColumn(2).setMinWidth(150);
		table.getColumnModel().getColumn(3).setMinWidth(225);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setIsPanelCellTable(2);
		table.setIsPanelCellTable(3);
		table.setEnabled(false);
		content.add(new JScrollPane(table),BorderLayout.CENTER);

		check();
		setMinSizeRespectingScreensize(700,500);
		setSizeRespectingScreensize(700,500);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Liefert die Anzahl der in dem Modell genutzten externen Datenquellen
	 * @return	Anzahl der in dem Modell genutzten externen Datenquellen
	 */
	public int getNumberOfExternalDataSources() {
		return numberOfExternalDataSources;
	}

	private void check() {
		final List<DataCheckResult> results=model.getDataCheckResults();

		numberOfExternalDataSources=0;
		for (final DataCheckResult result: results) if (result.status!=DataCheckResult.Status.NO_EXTERNAL_DATA) numberOfExternalDataSources++;

		tableData.setResults(results);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		check();
	}

	private class ResultsTableModel extends JTableExtAbstractTableModel {
		private static final long serialVersionUID = 4229271216146250189L;

		private final List<DataCheckResult> results;

		public ResultsTableModel() {
			super();
			results=new ArrayList<>();
		}

		public void setResults(final List<DataCheckResult> results) {
			this.results.clear();
			for (final DataCheckResult result: results) if (result.status!=DataCheckResult.Status.NO_EXTERNAL_DATA) this.results.add(result);
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return Math.max(1,results.size());
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (results.size()==0) switch (columnIndex) {
			case 1: return Language.tr("CheckData.NoDataElements");
			default: return "";
			}

			if (rowIndex<0 || rowIndex>=results.size()) return "";
			final DataCheckResult result=results.get(rowIndex);

			JLabel label;
			String s,t;
			URL url;

			String id;
			String name;
			if (result.element==null) {
				id=Language.tr("CheckData.Model");
				name=Language.tr("CheckData.Model");
			} else {
				id=""+result.element.getId();
				name=(result.element.getName().isEmpty())?result.element.getTypeName():(result.element.getTypeName()+" ("+result.element.getName()+")");
			}
			switch (columnIndex) {
			case 0:
				label=new JLabel(id);
				label.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
				return label;
			case 1:
				label=new JLabel(name);
				label.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
				label.setToolTipText(name);
				label.setIcon(Images.DATA_CHECK_STATION.getIcon());
				return label;
			case 2:
				label=new JLabel(result.data);
				label.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
				label.setToolTipText(result.data);
				switch (result.dataType) {
				case FILE: url=Images.DATA_CHECK_MODE_FILE.getURL(); break;
				case DB: url=Images.DATA_CHECK_MODE_DB.getURL(); break;
				case DDE: url=Images.DATA_CHECK_MODE_DDE.getURL(); break;
				case NONE: url=null; break;
				default: url=null; break;
				}
				if (url!=null) label.setIcon(new ImageIcon(url));
				return label;
			case 3:
				if (result.status==DataCheckResult.Status.OK) {
					t=Language.tr("CheckData.CheckOK");
					s="<span style=\"color: green;\">"+t+"</span>";
					url=Images.DATA_CHECK_RESULT_OK.getURL();
				} else {
					t=result.errorMessage;
					s="<span style=\"color: red;\">"+t+"</span>";
					url=Images.DATA_CHECK_RESULT_ERROR.getURL();
				}
				label=new JLabel("<html><body>"+s+"</body></html>");
				label.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
				label.setToolTipText(t);
				if (url!=null) label.setIcon(new ImageIcon(url));
				return label;
			default:
				return "";
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return Language.tr("CheckData.ColumnTitle.ID");
			case 1: return Language.tr("CheckData.ColumnTitle.Element");
			case 2: return Language.tr("CheckData.ColumnTitle.Data");
			case 3: return Language.tr("CheckData.ColumnTitle.Status");
			default: return super.getColumnName(column);
			}
		}
	}
}