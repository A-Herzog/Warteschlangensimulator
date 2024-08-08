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
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementOutputDDE}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementOutputDDE
 */
public class ModelElementOutputDDEDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4133614521655543844L;

	/**
	 * Checkbox: Ausgabe aktiv?
	 */
	private JCheckBox outputActive;

	/**
	 * Panel zur Konfiguration der DDE-Verbindung
	 */
	private DDEEditPanel editDDE;

	/**
	 * Tabelle zur Konfiguration der auszugebenden Daten
	 */
	private OutputDDETableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementOutputDDE}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementOutputDDEDialog(final Component owner, final ModelElementOutputDDE element, final boolean readOnly) {
		super(owner,Language.tr("Surface.OutputDDE.Dialog.Title"),element,"ModelElementOutputDDE",readOnly);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationOutputDDE;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementOutputDDE) {
			final JPanel upperPanel=new JPanel();
			content.add(upperPanel,BorderLayout.NORTH);
			upperPanel.setLayout(new BoxLayout(upperPanel,BoxLayout.PAGE_AXIS));

			/* Aktiv? */
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			upperPanel.add(line);
			line.add(outputActive=new JCheckBox("<html><body><b>"+Language.tr("Surface.OutputDDE.Dialog.OutputActive")+"</b></body></html>",((ModelElementOutputDDE)element).isOutputActive()));

			/* DDE */
			upperPanel.add(editDDE=new DDEEditPanel(this,(ElementWithDDEInputOutput)element,readOnly,helpRunnable));

			/* Main-Area */
			final JTableExt table=new JTableExt();
			table.setModel(tableModel=new OutputDDETableModel(table,((ModelElementOutputDDE)element).getModes(),((ModelElementOutputDDE)element).getData(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),element.getModel().userFunctions,readOnly));
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.setIsPanelCellTable(2);
			table.getColumnModel().getColumn(1).setMaxWidth(300);
			table.getColumnModel().getColumn(1).setMinWidth(300);
			table.setEnabled(!readOnly);
			content.add(new JScrollPane(table),BorderLayout.CENTER);

			/* Start */
			checkData(false);
		}

		return content;
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		/* DDE */

		if (!editDDE.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		((ModelElementOutputDDE)element).setOutputActive(outputActive.isSelected());

		/* DDE */

		editDDE.storeData();

		/* Elemente */


		final List<ModelElementOutputDDE.OutputMode> modes=((ModelElementOutputDDE)element).getModes();
		modes.clear();
		modes.addAll(tableModel.getModes());
		List<String> data=((ModelElementOutputDDE)element).getData();
		data.clear();
		data.addAll(tableModel.getData());
		while (data.size()<modes.size()) data.add("");
	}
}
