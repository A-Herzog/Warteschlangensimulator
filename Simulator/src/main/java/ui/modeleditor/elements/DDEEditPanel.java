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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import net.dde.DDEConnect;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Diese Klasse stellt ein Panel zur Eingabe von DDE-Verbindungsdaten bereit.
 * @author Alexander Herzog
 * @see ElementWithDDEInputOutput
 */
public class DDEEditPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3684756803611814267L;

	private final Component owner;
	private final ElementWithDDEInputOutput element;
	private final boolean readOnly;
	private final Runnable helpRunnable;

	private final JTextField editWorkbook;
	private final JTextField editTable;
	private final JTextField editStartRow;
	private final JTextField editColumn;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (wird für Auswahldialog, den dieses Panel initiieren kann, benötigt)
	 * @param element	Modellelement dessen Daten bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Runnable
	 */
	public DDEEditPanel(final Component owner, final ElementWithDDEInputOutput element, final boolean readOnly, final Runnable helpRunnable) {
		super();
		this.owner=owner;
		this.element=element;
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DDE.Workbook")+":",element.getWorkbook());
		final JPanel line=(JPanel)data[0];
		add(line);
		editWorkbook=(JTextField)data[1];
		editWorkbook.setEditable(!readOnly);

		if (!readOnly) {
			final JButton selectButton=new JButton(Language.tr("Surface.DDE.Select.Button.Title"));
			selectButton.setIcon(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon());
			selectButton.setToolTipText(Language.tr("Surface.DDE.Select.Button.Hint"));
			line.add(selectButton,BorderLayout.EAST);
			selectButton.addActionListener(e->selectDDEData());
		}

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DDE.Table")+":",element.getTable());
		add((JPanel)data[0]);
		editTable=(JTextField)data[1];
		editTable.setEditable(!readOnly);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DDE.StartRow")+":",""+element.getStartRow());
		add((JPanel)data[0]);
		editStartRow=(JTextField)data[1];
		editStartRow.setEditable(!readOnly);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DDE.Column")+":",""+element.getColumn());
		add((JPanel)data[0]);
		editColumn=(JTextField)data[1];
		editColumn.setEditable(!readOnly);

		checkData(false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (wird für Auswahldialog, den dieses Panel initiieren kann, benötigt)
	 * @param workbook	Bisherige Arbeitsmappe
	 * @param table	Bisherige Tabelle
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Hilfe-Runnable
	 */
	public DDEEditPanel(final Component owner, final String workbook, final String table, final boolean readOnly, final Runnable helpRunnable) {
		super();
		this.owner=owner;
		this.element=null;
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DDE.Workbook")+":",workbook);
		final JPanel line=(JPanel)data[0];
		add(line);
		editWorkbook=(JTextField)data[1];
		editWorkbook.setEditable(!readOnly);

		if (!readOnly) {
			final JButton selectButton=new JButton(Language.tr("Surface.DDE.Select.Button.Title"));
			selectButton.setIcon(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon());
			selectButton.setToolTipText(Language.tr("Surface.DDE.Select.Button.Hint"));
			line.add(selectButton,BorderLayout.EAST);
			selectButton.addActionListener(e->selectDDEData());
		}

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DDE.Table")+":",table);
		add((JPanel)data[0]);
		editTable=(JTextField)data[1];
		editTable.setEditable(!readOnly);

		editStartRow=null;
		editColumn=null;

		checkData(false);
	}

	private void selectDDEData() {
		final Map<String,List<String>> list=new DDEConnect().listTables();
		if (list.size()==0) {
			MsgBox.error(this,Language.tr("Surface.DDE.Select.NoData.ErrorTitle"),Language.tr("Surface.DDE.Select.NoData.ErrorInfo"));
			return;
		}

		final DDEEditPanelDialog dialog=new DDEEditPanelDialog(owner,list,editWorkbook.getText().trim(),editTable.getText().trim(),helpRunnable);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			editWorkbook.setText(dialog.getSelectedWorkbook());
			editTable.setText(dialog.getSelectedTable());
			checkData(false);
		}
	}

	/**
	 * Prüft die Eingaben.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird bei fehlerhaften Daten ein Meldungsdialog angezeigt.
	 * @return	Liefert <code>true</code>, wenn die Eingaben in Ordnung sind.
	 */
	public boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;
		boolean ok=true;

		if (editWorkbook.getText().trim().isEmpty()) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.DDE.Workbook.ErrorTitle"),Language.tr("Surface.DDE.Workbook.ErrorInfo"));
				return false;
			}
		}

		if (editTable.getText().trim().isEmpty()) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.DDE.Table.ErrorTitle"),Language.tr("Surface.DDE.Table.ErrorInfo"));
				return false;
			}
		}

		if (editStartRow!=null) {
			final Long L=NumberTools.getPositiveLong(editStartRow,true);
			if (L==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.DDE.StartRow.ErrorTitle"),String.format(Language.tr("Surface.DDE.StartRow.ErrorInfo"),editStartRow.getText()));
					return false;
				}
			}
		}

		if (editColumn!=null) {
			if (Table.numberFromColumnName(editColumn.getText().trim())<0) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.DDE.Column.ErrorTitle"),String.format(Language.tr("Surface.DDE.Column.ErrorInfo"),editColumn.getText()));
					return false;
				}
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Einstellungen in das im Konstruktor übergebene Modellelement zurück.
	 */
	public void storeData() {
		if (element!=null) {
			element.setWorkbook(editWorkbook.getText().trim());
			element.setTable(editTable.getText().trim());
			final Long L=NumberTools.getPositiveLong(editStartRow,true);
			element.setStartRow(L.intValue());
			element.setColumn(editColumn.getText().trim());
		}
	}

	/**
	 * Liefert die gewählte Arbeitsmappe
	 * @return	Ausgewählte Arbeitsmappe
	 */
	public String getWorkbook() {
		return editWorkbook.getText().trim();
	}

	/**
	 * Stellt die gewünschte Arbeitsmappe ein
	 * @param workbook	Arbeitsmappe
	 */
	public void setWorkbook(final String workbook) {
		editWorkbook.setText(workbook);
	}

	/**
	 * Liefert die gewählte Tabelle
	 * @return	Ausgewählte Tabelle
	 */
	public String getTable() {
		return editTable.getText().trim();
	}

	/**
	 * Stellt die gewünschte Tabelle ein
	 * @param table	Tabelle
	 */
	public void setTable(final String table) {
		editTable.setText(table);
	}
}