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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;

/**
 * In diesem Panel können die Daten eines einzelnen Batch-Datensatzes bearbeitet werden.
 * @author Alexander Herzog
 * @see BatchRecord
 */
public class BatchRecordPanel extends JPanel {
	private static final long serialVersionUID = 540893978614092794L;

	private final BatchRecord batchRecord;

	private JCheckBox active;

	private final JTextField batchFieldMin;
	private final JTextField batchFieldMax;
	private final JRadioButton optionForward;
	private final JRadioButton optionTemporary;
	private final JTextField tempTypeField;
	private final JRadioButton optionNewType;
	private final JTextField newTypeField;

	/**
	 * Konstruktor der Klasse
	 * @param batchRecord	Batch-Datensatz der in diesem Panel zum Bearbeiten angeboten werden soll
	 * @param readOnly	Nur-Lese-Status
	 * @param useActiveCheckbox	Soll eine Checkbox zum Aktivieren/Deaktivieren dieses Datensatzes angezeigt werden?
	 */
	public BatchRecordPanel(final BatchRecord batchRecord, final boolean readOnly, final boolean useActiveCheckbox) {
		super();
		this.batchRecord=batchRecord;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		if (useActiveCheckbox) {
			add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(active=new JCheckBox("<html><body><b>"+Language.tr("Surface.Batch.Dialog.TypeActive")+"</b></body></html"));
			active.addActionListener(e->checkData(false));
		}

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		line.add(label=new JLabel(Language.tr("Surface.Batch.Dialog.BatchSizeMin")+":"));
		line.add(batchFieldMin=new JTextField(4));
		batchFieldMin.setEditable(!readOnly);
		batchFieldMin.setText(""+batchRecord.getBatchSizeMin());
		label.setLabelFor(batchFieldMin);
		batchFieldMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); checkData(false);}
		});

		line.add(label=new JLabel(Language.tr("Surface.Batch.Dialog.BatchSizeMax")+":"));
		line.add(batchFieldMax=new JTextField(4));
		batchFieldMax.setEditable(!readOnly);
		batchFieldMax.setText(""+batchRecord.getBatchSizeMax());
		label.setLabelFor(batchFieldMax);
		batchFieldMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); checkData(false);}
		});

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionForward=new JRadioButton(Language.tr("Surface.Batch.Dialog.SendSeparate")));
		optionForward.setEnabled(!readOnly);
		optionForward.addActionListener(e->{setActive(); checkData(false);});

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTemporary=new JRadioButton(Language.tr("Surface.Batch.Dialog.SendTemporaryBatched")));
		optionTemporary.setEnabled(!readOnly);
		optionTemporary.addActionListener(e->{setActive(); checkData(false);});
		line.add(tempTypeField=new JTextField(25));
		tempTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); optionTemporary.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {setActive(); optionTemporary.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {setActive(); optionTemporary.setSelected(true);}
		});
		tempTypeField.setEditable(!readOnly);

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNewType=new JRadioButton(Language.tr("Surface.Batch.Dialog.SendAsNewClient")));
		optionNewType.setEnabled(!readOnly);
		optionNewType.addActionListener(e->{setActive(); checkData(false);});
		line.add(newTypeField=new JTextField(25));
		newTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); optionNewType.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {setActive(); optionNewType.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {setActive(); optionNewType.setSelected(true);}
		});
		newTypeField.setEditable(!readOnly);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionForward);
		buttonGroup.add(optionTemporary);
		buttonGroup.add(optionNewType);

		switch (batchRecord.getBatchMode()) {
		case BATCH_MODE_COLLECT:
			optionForward.setSelected(true);
			break;
		case BATCH_MODE_TEMPORARY:
			optionTemporary.setSelected(true);
			tempTypeField.setText(batchRecord.getNewClientType());
			break;
		case BATCH_MODE_PERMANENT:
			optionNewType.setSelected(true);
			newTypeField.setText(batchRecord.getNewClientType());
			break;
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param batchRecord	Batch-Datensatz der in diesem Panel zum Bearbeiten angeboten werden soll
	 * @param readOnly	Nur-Lese-Status
	 */
	public BatchRecordPanel(final BatchRecord batchRecord, final boolean readOnly) {
		this(batchRecord,readOnly,false);
	}

	/**
	 * Wurde im Konstruktor angegeben, dass eine Checkbox zum Aktivieren/Deaktivieren
	 * dieses Datensatzes angezeigt werden soll, so kann über diese Methode der Status
	 * der Checkbox abgefragt werden.
	 * @return	Ist der Datensatz aktiv?
	 */
	public boolean isActive() {
		return active==null || active.isSelected();
	}

	/**
	 * Wurde im Konstruktor angegeben, dass eine Checkbox zum Aktivieren/Deaktivieren
	 * dieses Datensatzes angezeigt werden soll, so kann über diese Methode der Status
	 * der Checkbox eingestellt werden.
	 * @param active	Soll der Datensatz aktiv sein?
	 */
	public void setActive(final boolean active) {
		if (this.active!=null) this.active.setSelected(active);
	}

	private void setActive() {
		if (active!=null) active.setSelected(true);
	}

	/**
	 * Prüft die Einstellungen.
	 * @param showErrorMessage	Soll im Fehlerfall eine Fehlermeldung angezeigt werden?
	 * @return	Sind alle Einstellungen in Ordnung?
	 */
	public boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (active!=null && !active.isSelected()) return true;

		final Long Lmin=NumberTools.getPositiveLong(batchFieldMin,true);
		if (Lmin==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoMin"),batchFieldMin.getText()));
				return false;
			}
			ok=false;
		}

		final Long Lmax=NumberTools.getPositiveLong(batchFieldMax,true);
		if (Lmax==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoMax"),batchFieldMax.getText()));
				return false;
			}
			ok=false;
		}

		if (Lmin!=null && Lmax!=null && Lmin.longValue()>Lmax.longValue()) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoRange"),Lmin.longValue(),Lmax.longValue()));
				return false;
			} else {
				batchFieldMax.setBackground(Color.red);
			}
			ok=false;
		}

		if (optionNewType.isSelected() && newTypeField.getText().isEmpty()) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Batch.Dialog.SendAsNewClient.Error.Title"),Language.tr("Surface.Batch.Dialog.SendAsNewClient.Error.Info"));
				return false;
			}
			ok=false;
		}

		if (optionTemporary.isSelected() && tempTypeField.getText().isEmpty()) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Batch.Dialog.SendTemporaryBatched.Error.Title"),Language.tr("Surface.Batch.Dialog.SendTemporaryBatched.Error.Info"));
				return false;
			}
			ok=false;
		}

		return ok;
	}

	/**
	 * Schreibt die Einstellungen in den im Konstruktor angegeben Batch-Datensatz zurück.
	 * @see #BatchRecordPanel(BatchRecord, boolean)
	 */
	public void storeData() {
		Long L;

		L=NumberTools.getPositiveLong(batchFieldMin,true);
		if (L!=null) batchRecord.setBatchSizeMin((int)((long)L));
		L=NumberTools.getPositiveLong(batchFieldMax,true);
		if (L!=null) batchRecord.setBatchSizeMax((int)((long)L));

		if (optionForward.isSelected()) batchRecord.setBatchMode(BatchRecord.BatchMode.BATCH_MODE_COLLECT);
		if (optionTemporary.isSelected()) {
			batchRecord.setBatchMode(BatchRecord.BatchMode.BATCH_MODE_TEMPORARY);
			batchRecord.setNewClientType(tempTypeField.getText());
		}
		if (optionNewType.isSelected()) {
			batchRecord.setBatchMode(BatchRecord.BatchMode.BATCH_MODE_PERMANENT);
			batchRecord.setNewClientType(newTypeField.getText());
		}
	}

	/**
	 * Liefert den im Konstruktor übergebenen Batch-Datensatz
	 * @return	Batch-Datensatz der in diesem Panel bearbeitet wird (wird durch {@link #storeData()} aktualisiert)
	 */
	public BatchRecord getBatchRecord() {
		return batchRecord;
	}
}
