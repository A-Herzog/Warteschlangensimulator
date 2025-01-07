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
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * In diesem Panel können die Daten eines einzelnen Batch-Datensatzes bearbeitet werden.
 * @author Alexander Herzog
 * @see BatchRecord
 */
public class BatchRecordPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 540893978614092794L;

	/** Batch-Datensatz der in diesem Panel zum Bearbeiten angeboten werden soll */
	private final BatchRecord batchRecord;

	/** Namen aller modellweit verfügbaren Variablennamen */
	private String[] variableNames;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private ExpressionCalcModelUserFunctions userFunctions;

	/** Soll der Datensatz aktiv sein? */
	private JCheckBox active;

	/** Option: Feste Batch-Größe */
	private JRadioButton modeFixed;
	/** Option: Batch-Größe in einem bestimmten Bereich */
	private JRadioButton modeRange;
	/** Eingabefeld für die feste Batch-Größe */
	private final JTextField batchFieldFixed;
	/** Eingabefeld für die minimale Batch-Größe bei Verwendung einer variablen Batch-Größe */
	private final JTextField batchFieldMin;
	/** Eingabefeld für die maximale Batch-Größe bei Verwendung einer variablen Batch-Größe */
	private final JTextField batchFieldMax;
	/** Option: Kunden gemeinsam weiterleiten */
	private final JRadioButton optionForward;
	/** Option: Temporären Batch bilden */
	private final JRadioButton optionTemporary;
	/** Eingabefeld für den neuen Kundentyp für einen temporären Batch */
	private final JTextField tempTypeField;
	/** Option: Permanenten Batch bilden */
	private final JRadioButton optionNewType;
	/** Eingabefeld für den neuen Kundentyp für einen permanenten Batch */
	private final JTextField newTypeField;

	/** Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private final JComboBox<?> transferTimes;
	/** Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden? */
	private final JComboBox<?> transferNumbers;

	/**
	 * Konstruktor der Klasse
	 * @param batchRecord	Batch-Datensatz der in diesem Panel zum Bearbeiten angeboten werden soll
	 * @param readOnly	Nur-Lese-Status
	 * @param useActiveCheckbox	Soll eine Checkbox zum Aktivieren/Deaktivieren dieses Datensatzes angezeigt werden?
	 * @param model	Editor-Modell (für Expression-Builder)
	 * @param surface	Zeichenfläche (für Expression-Builder)
	 */
	public BatchRecordPanel(final BatchRecord batchRecord, final boolean readOnly, final boolean useActiveCheckbox, final EditModel model, final ModelSurface surface) {
		super();
		this.batchRecord=batchRecord;

		variableNames=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false);
		userFunctions=model.userFunctions;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		ButtonGroup buttonGroup;
		Object[] data;

		if (useActiveCheckbox) {
			add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(active=new JCheckBox("<html><body><b>"+Language.tr("Surface.Batch.Dialog.TypeActive")+"</b></body></html"));
			active.addActionListener(e->checkData(false));
		}

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Batch.Dialog.BatchSizeMode.Heading")+"</b></body></html>"));

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		line.add(modeFixed=new JRadioButton(Language.tr("Surface.Batch.Dialog.BatchSizeMode.Fixed"),batchRecord.getBatchSizeMode()==BatchRecord.BatchSizeMode.FIXED));
		modeFixed.setEnabled(!readOnly);

		line.add(label=new JLabel(Language.tr("Surface.Batch.Dialog.BatchSizeFixed")+":"));
		line.add(batchFieldFixed=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(batchFieldFixed);
		batchFieldFixed.setEnabled(!readOnly);
		batchFieldFixed.setText(""+batchRecord.getBatchSizeFixed());
		label.setLabelFor(batchFieldFixed);
		batchFieldFixed.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); modeFixed.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); modeFixed.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); modeFixed.setSelected(true); checkData(false);}
		});
		if (!readOnly) line.add(ModelElementBaseDialog.getExpressionEditButton(this,batchFieldFixed,false,false,model,surface));

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		line.add(modeRange=new JRadioButton(Language.tr("Surface.Batch.Dialog.BatchSizeMode.Range"),batchRecord.getBatchSizeMode()==BatchRecord.BatchSizeMode.RANGE));
		modeRange.setEnabled(!readOnly);

		line.add(label=new JLabel(Language.tr("Surface.Batch.Dialog.BatchSizeMin")+":"));
		line.add(batchFieldMin=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(batchFieldMin);
		batchFieldMin.setEnabled(!readOnly);
		batchFieldMin.setText(""+batchRecord.getBatchSizeMin());
		label.setLabelFor(batchFieldMin);
		batchFieldMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); modeRange.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); modeRange.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); modeRange.setSelected(true); checkData(false);}
		});
		if (!readOnly) line.add(ModelElementBaseDialog.getExpressionEditButton(this,batchFieldMin,false,false,model,surface));

		line.add(label=new JLabel(Language.tr("Surface.Batch.Dialog.BatchSizeMax")+":"));
		line.add(batchFieldMax=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(batchFieldMax);
		batchFieldMax.setEnabled(!readOnly);
		batchFieldMax.setText(""+batchRecord.getBatchSizeMax());
		label.setLabelFor(batchFieldMax);
		batchFieldMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); modeRange.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); modeRange.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); modeRange.setSelected(true); checkData(false);}
		});
		if (!readOnly) line.add(ModelElementBaseDialog.getExpressionEditButton(this,batchFieldMax,false,false,model,surface));

		buttonGroup=new ButtonGroup();
		buttonGroup.add(modeFixed);
		buttonGroup.add(modeRange);

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("Surface.Batch.Dialog.BatchSizeInfo")+"</body></html>"));

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Batch.Dialog.SendMode.Heading")+"</b></body></html>"));

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionForward=new JRadioButton(Language.tr("Surface.Batch.Dialog.SendSeparate")));
		optionForward.setEnabled(!readOnly);
		optionForward.addActionListener(e->{setActive(); checkData(false);});

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionTemporary=new JRadioButton(Language.tr("Surface.Batch.Dialog.SendTemporaryBatched")));
		optionTemporary.setEnabled(!readOnly);
		optionTemporary.addActionListener(e->{setActive(); checkData(false);});
		line.add(tempTypeField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(tempTypeField);
		tempTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); optionTemporary.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); optionTemporary.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); optionTemporary.setSelected(true); checkData(false);}
		});
		tempTypeField.setEnabled(!readOnly);

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNewType=new JRadioButton(Language.tr("Surface.Batch.Dialog.SendAsNewClient")));
		optionNewType.setEnabled(!readOnly);
		optionNewType.addActionListener(e->{setActive(); checkData(false);});
		line.add(newTypeField=new JTextField(25));
		ModelElementBaseDialog.addUndoFeature(newTypeField);
		newTypeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {setActive(); optionNewType.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {setActive(); optionNewType.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {setActive(); optionNewType.setSelected(true); checkData(false);}
		});
		newTypeField.setEnabled(!readOnly);

		buttonGroup=new ButtonGroup();
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

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.Batch.Dialog.TransferData")+"</b></body></html>"));
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Batch.Dialog.TransferData.Times")+":",new String[] {
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Off"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Min"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Max"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Mean"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Sum"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Multiply")
		});
		add((JPanel)data[0]);
		transferTimes=(JComboBox<?>)data[1];
		transferTimes.setEnabled(!readOnly);
		switch (batchRecord.getTransferTimes()) {
		case OFF: transferTimes.setSelectedIndex(0); break;
		case MIN: transferTimes.setSelectedIndex(1); break;
		case MAX: transferTimes.setSelectedIndex(2); break;
		case MEAN: transferTimes.setSelectedIndex(3); break;
		case SUM: transferTimes.setSelectedIndex(4); break;
		case MULTIPLY: transferTimes.setSelectedIndex(5); break;
		default: transferTimes.setSelectedIndex(0); break;
		}
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Batch.Dialog.TransferData.Numbers")+":",new String[] {
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Off"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Min"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Max"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Mean"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Sum"),
				Language.tr("Surface.Batch.Dialog.TransferData.Mode.Multiply")
		});
		add((JPanel)data[0]);
		transferNumbers=(JComboBox<?>)data[1];
		transferNumbers.setEnabled(!readOnly);
		switch (batchRecord.getTransferNumbers()) {
		case OFF: transferNumbers.setSelectedIndex(0); break;
		case MIN: transferNumbers.setSelectedIndex(1); break;
		case MAX: transferNumbers.setSelectedIndex(2); break;
		case MEAN: transferNumbers.setSelectedIndex(3); break;
		case SUM: transferNumbers.setSelectedIndex(4); break;
		case MULTIPLY: transferNumbers.setSelectedIndex(5); break;
		default: transferNumbers.setSelectedIndex(0); break;
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param batchRecord	Batch-Datensatz der in diesem Panel zum Bearbeiten angeboten werden soll
	 * @param readOnly	Nur-Lese-Status
	 * @param model	Editor-Modell (für Expression-Builder)
	 * @param surface	Zeichenfläche (für Expression-Builder)
	 */
	public BatchRecordPanel(final BatchRecord batchRecord, final boolean readOnly, final EditModel model, final ModelSurface surface) {
		this(batchRecord,readOnly,false,model,surface);
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

	/**
	 * Wurde im Konstruktor angegeben, dass eine Checkbox zum Aktivieren/Deaktivieren
	 * dieses Datensatzes angezeigt werden soll, so kann über diese Methode der Status
	 * der Checkbox auf "aktiv" eingestellt werden.
	 */
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

		/* Fester Wert */
		if (modeFixed.isSelected()) {
			boolean error=false;
			final String value=batchFieldFixed.getText().trim();
			if (ExpressionCalc.check(value,variableNames,userFunctions)>=0) {
				error=true;
			} else {
				final Double D=NumberTools.getDouble(batchFieldFixed,true);
				if (D!=null) {
					if (Math.round(D)<1) error=true;
				}
			}
			if (error) {
				batchFieldFixed.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoFixed"),value));
					return false;
				}
				ok=false;
			} else {
				batchFieldFixed.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			batchFieldFixed.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Bereich */
		if (modeRange.isSelected()) {
			boolean error1=false;
			boolean error2=false;
			Integer min=null;
			Integer max=null;
			final String minStr=batchFieldMin.getText().trim();
			final String maxStr=batchFieldMax.getText().trim();

			if (ExpressionCalc.check(minStr,variableNames,userFunctions)>=0) {
				error1=true;
			} else {
				final Double D=NumberTools.getDouble(batchFieldMin,true);
				if (D!=null) {
					if (Math.round(D)<1) error1=true; else min=(int)Math.round(D);
				}
			}
			if (error1) {
				batchFieldMin.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoMin"),minStr));
					return false;
				}
				ok=false;
			} else {
				batchFieldMin.setBackground(NumberTools.getTextFieldDefaultBackground());
			}

			if (ExpressionCalc.check(maxStr,variableNames,userFunctions)>=0) {
				error2=true;
			} else {
				final Double D=NumberTools.getDouble(batchFieldMax,true);
				if (D!=null) {
					if (Math.round(D)<1) error2=true; else max=(int)Math.round(D);
				}
			}
			if (error2) {
				batchFieldMax.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoMax"),maxStr));
					return false;
				}
				ok=false;
			} else {
				batchFieldMax.setBackground(NumberTools.getTextFieldDefaultBackground());
			}

			if (min!=null && max!=null && min>max) {
				batchFieldMin.setBackground(Color.RED);
				batchFieldMax.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Batch.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Batch.Dialog.BatchSize.Error.InfoRange"),min,max));
					return false;
				}
				ok=false;
			}
		} else {
			batchFieldMin.setBackground(NumberTools.getTextFieldDefaultBackground());
			batchFieldMax.setBackground(NumberTools.getTextFieldDefaultBackground());
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
	 * @see #BatchRecordPanel(BatchRecord, boolean, EditModel, ModelSurface)
	 * @see #BatchRecordPanel(BatchRecord, boolean, boolean, EditModel, ModelSurface)
	 */
	public void storeData() {
		if (modeFixed.isSelected()) {
			batchRecord.setBatchSizeMode(BatchRecord.BatchSizeMode.FIXED);
			batchRecord.setBatchSizeFixed(batchFieldFixed.getText().trim());
		}

		if (modeRange.isSelected()) {
			batchRecord.setBatchSizeMode(BatchRecord.BatchSizeMode.RANGE);
			batchRecord.setBatchSizeMin(batchFieldMin.getText().trim());
			batchRecord.setBatchSizeMax(batchFieldMax.getText().trim());
		}

		if (optionForward.isSelected()) batchRecord.setBatchMode(BatchRecord.BatchMode.BATCH_MODE_COLLECT);
		if (optionTemporary.isSelected()) {
			batchRecord.setBatchMode(BatchRecord.BatchMode.BATCH_MODE_TEMPORARY);
			batchRecord.setNewClientType(tempTypeField.getText());
		}
		if (optionNewType.isSelected()) {
			batchRecord.setBatchMode(BatchRecord.BatchMode.BATCH_MODE_PERMANENT);
			batchRecord.setNewClientType(newTypeField.getText());
		}
		switch (transferTimes.getSelectedIndex()) {
		case 0: batchRecord.setTransferTimes(BatchRecord.DataTransferMode.OFF); break;
		case 1: batchRecord.setTransferTimes(BatchRecord.DataTransferMode.MIN); break;
		case 2: batchRecord.setTransferTimes(BatchRecord.DataTransferMode.MAX); break;
		case 3: batchRecord.setTransferTimes(BatchRecord.DataTransferMode.MEAN); break;
		case 4: batchRecord.setTransferTimes(BatchRecord.DataTransferMode.SUM); break;
		case 5: batchRecord.setTransferTimes(BatchRecord.DataTransferMode.MULTIPLY); break;
		}
		switch (transferNumbers.getSelectedIndex()) {
		case 0: batchRecord.setTransferNumbers(BatchRecord.DataTransferMode.OFF); break;
		case 1: batchRecord.setTransferNumbers(BatchRecord.DataTransferMode.MIN); break;
		case 2: batchRecord.setTransferNumbers(BatchRecord.DataTransferMode.MAX); break;
		case 3: batchRecord.setTransferNumbers(BatchRecord.DataTransferMode.MEAN); break;
		case 4: batchRecord.setTransferNumbers(BatchRecord.DataTransferMode.SUM); break;
		case 5: batchRecord.setTransferNumbers(BatchRecord.DataTransferMode.MULTIPLY); break;
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
