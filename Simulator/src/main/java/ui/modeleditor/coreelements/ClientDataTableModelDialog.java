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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zum Bearbeiten eines einzelnen Datensatzes in {@link ClientDataTableModel} an.
 * @author Alexander Herzog
 * @see ClientDataTableModel
 */
public class ClientDataTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1062598294101418694L;

	/**
	 * Sollen die numerischen Datenfelder (<code>true</code>) oder die textbasierten Datenfelder (<code>false</code>) bearbeitet werden?
	 */
	private final boolean numbersMode;

	/**
	 * Liste aller vorhandenen Datensätze
	 */
	private final List<ClientDataTableModel.Record> records;

	/**
	 * Index des zu bearbeitenden Datensatzes in der Liste (-1 für neuer Datensatz)
	 */
	private final int index;

	/**
	 * Eingabefeld für den Schlüssel
	 */
	private final JTextField editKey;

	/**
	 * Eingabefeld für den Wert
	 */
	private final JTextField editValue;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param numbersMode	Sollen die numerischen Datenfelder (<code>true</code>) oder die textbasierten Datenfelder (<code>false</code>) bearbeitet werden?
	 * @param records	Liste aller vorhandenen Datensätze
	 * @param index	Index des zu bearbeitenden Datensatzes in der Liste (-1 für neuer Datensatz)
	 */
	public ClientDataTableModelDialog(final Component owner, final boolean numbersMode, final List<ClientDataTableModel.Record> records, final int index) {
		super(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditRecord.Title"));
		this.numbersMode=numbersMode;
		this.records=records;
		this.index=index;

		/* GUI */
		final JPanel main=createGUI(null);
		main.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		main.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] data;
		String s;

		/* Schlüssel */
		s=(index<0)?"":records.get(index).key;
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey")+":",s);
		content.add((JPanel)data[0]);
		editKey=(JTextField)data[1];
		editKey.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Wert */
		s=(index<0)?"":records.get(index).value;
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditValue")+":",s);
		content.add((JPanel)data[0]);
		editValue=(JTextField)data[1];
		editValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */
		checkData(false);
		setMinSizeRespectingScreensize(600,0);
		pack();
		if (getWidth()>600) setSize(600,getHeight());
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Schlüssel prüfen */
		final String key=editKey.getText().trim();
		if (key.isEmpty()) {
			editKey.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey.ErrorTitle"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey.ErrorEmpty"));
				return false;
			}
		} else {
			if (!checkKey(key)) {
				editKey.setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					if (numbersMode) {
						MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey.ErrorNumber"),key));
					} else {
						MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditKey.ErrorText"),key));
					}
					return false;
				}
			} else {
				editKey.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		/* Wert prüfen */
		final String value=editValue.getText().trim();
		if (value.isEmpty()) {
			editValue.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditValue.ErrorTitle"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditValue.ErrorEmpty"));
				return false;
			}
		} else {
			if (!checkValue(value)) {
				editValue.setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditValue.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.FieldTable.EditValue.ErrorNumber"),value));
					return false;
				}
			} else {
				editValue.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	/**
	 * Prüft, ob der angegebene Schlüssel gültig ist.
	 * @param key	Zu prüfender Schlüssel
	 * @return	Liefert <code>true</code>, wenn der Schlüssel gültig ist
	 * @see #checkData(boolean)
	 */
	private boolean checkKey(final String key) {
		if (numbersMode) {
			if (NumberTools.getNotNegativeInteger(key)==null) return false;
		}

		final int size=records.size();
		for (int i=0;i<size;i++) {
			if (i==index) continue;
			if (records.get(i).key.equals(key)) return false;
		}

		return true;
	}

	/**
	 * Prüft, ob der angegebene Wert gültig ist.
	 * @param value	Zu prüfender Wert
	 * @return	Liefert <code>true</code>, wenn der Wert gültig ist
	 * @see #checkData(boolean)
	 */
	private boolean checkValue(final String value) {
		if (!numbersMode) return true;

		return NumberTools.getDouble(value)!=null;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		final String key=editKey.getText().trim();
		final String value=editValue.getText().trim();

		if (index<0) {
			records.add(new ClientDataTableModel.Record(key,value));
		} else {
			final ClientDataTableModel.Record record=records.get(index);
			record.key=key;
			record.value=value;
		}
	}
}
