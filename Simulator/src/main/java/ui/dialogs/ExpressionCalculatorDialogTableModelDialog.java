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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zur Bearbeitung eines einzelnen Eintrags aus {@link ExpressionCalculatorDialogTableModel}
 * @author Alexander Herzog
 * @see ExpressionCalculatorDialogTableModel
 */
public class ExpressionCalculatorDialogTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2282072136354082632L;

	/**
	 * Namen aller bekannten Schlüssel (um Kollisionen zu vermeiden)
	 */
	private final String[] keys;

	/**
	 * Index des bisherigen Schlüssels in der Liste aller Schlüssel (kann -1 sein, wenn nicht enthalten)
	 * @see #keys
	 */
	private int keyIndex;

	/**
	 * Eingabefeld für den Schlüssel
	 */
	private final JTextField keyEdit;

	/**
	 * Auswahlbox zur Wahl des Typs des Wertes
	 */
	private final JComboBox<String> valueCombo;

	/**
	 * Eingabefeld für den Wert
	 */
	private final JTextField valueEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param key	Bisheriger Name des Schlüssels (kann <code>null</code> sein)
	 * @param value	Bisheriger Wert (kann <code>null</code> sein)
	 * @param keys	Namen aller bekannten Schlüssel (um Kollisionen zu vermeiden)
	 */
	@SuppressWarnings("unchecked")
	public ExpressionCalculatorDialogTableModelDialog(final Component owner, final String key, final Object value, final String[] keys) {
		super(owner,Language.tr("ExpressionCalculator.Tab.Map.Edit.Title"));

		/* Bisherigen Key in der Liste aller Keys finden */
		this.keys=keys;
		keyIndex=-1;
		if (key!=null) {
			for (int i=0;i<keys.length;i++) if (key.equals(keys[i])) {keyIndex=i; break;}
		}

		/* GUI */
		final JPanel all=createGUI(null);
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		Object[] data;

		/* Schlüssel */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("ExpressionCalculator.Tab.Map.Column.Key")+"</b></body></html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ExpressionCalculator.Tab.Map.Column.Key")+":",(key==null)?"":key,50);
		content.add((JPanel)data[0]);
		keyEdit=(JTextField)data[1];

		/* Bisheriger Wert */
		if (value!=null) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel("<html><body><b>"+Language.tr("ExpressionCalculator.Tab.Map.Column.Value.Old")+"</b></body></html>"));
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel("<html><body>"+ExpressionCalculatorDialogTableModel.processValue(value)+"</body></html>"));
		}

		/* Neuer Wert */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("ExpressionCalculator.Tab.Map.Column.Value.New")+"</b></body></html>"));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("ExpressionCalculator.Tab.Map.Column.Type")+":",Arrays.asList("Integer","Long","Double","String"));
		content.add((JPanel)data[0]);
		valueCombo=(JComboBox<String>)data[1];
		valueCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_NUMBERS,
				Images.GENERAL_NUMBERS,
				Images.GENERAL_NUMBERS,
				Images.GENERAL_FONT
		}));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("ExpressionCalculator.Tab.Map.Column.Value.New")+"</b></body></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("ExpressionCalculator.Tab.Map.Column.Value")+":","",50);
		content.add((JPanel)data[0]);
		valueEdit=(JTextField)data[1];

		/* Initiale Belegung für neuen Wert */
		valueCombo.setSelectedIndex(3);
		valueEdit.setText("");
		if (value instanceof Integer) {
			valueCombo.setSelectedIndex(0);
			valueEdit.setText(""+value);
		}
		if (value instanceof Long) {
			valueCombo.setSelectedIndex(1);
			valueEdit.setText(""+value);
		}
		if (value instanceof Double) {
			valueCombo.setSelectedIndex(2);
			valueEdit.setText(NumberTools.formatNumberMax((Double)value));
		}
		if (value instanceof String) {
			valueCombo.setSelectedIndex(3);
			valueEdit.setText((String)value);
		}

		/* Auf Änderungen reagieren */
		keyEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		valueCombo.addActionListener(e->checkData(false));
		valueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */
		checkData(false);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}


	/**
	 * Prüft, ob ein Schlüssel gültig ist (z.B. sich nicht mit anderen existierenden Schlüssel überschneidet).
	 * @param key	Zu prüfender Schlüssel
	 * @return	Liefer <code>true</code>, wenn der Schlüssel gültig ist
	 */
	private boolean testKey(final String key) {
		if (key==null || key.isBlank()) return false;
		for (int i=0;i<keys.length;i++) {
			if (i==keyIndex) continue;
			if (key.equals(keys[i])) return false;
		}
		return true;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Schlüssel */
		final String key=keyEdit.getText().trim();
		if (testKey(key)) {
			keyEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			ok=false;
			keyEdit.setBackground(Color.RED);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("ExpressionCalculator.Tab.Map.Column.Key.InvalidTitle"),Language.tr("ExpressionCalculator.Tab.Map.Column.Key.InvalidInfo"));
				return false;
			}
		}

		/* Wert */
		final Object value=getNewValue();
		if (value==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("ExpressionCalculator.Tab.Map.Column.Value.InvalidTitle"),Language.tr("ExpressionCalculator.Tab.Map.Column.Value.InvalidInfo"));
				return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert den neuen Schlüssel.
	 * @return	Schlüssel
	 */
	public String getNewKey() {
		return keyEdit.getText().trim();
	}

	/**
	 * Liefert den neuen Wert.
	 * @return	Wert
	 */
	public Object getNewValue() {
		switch (valueCombo.getSelectedIndex()) {
		case 0:
			/* Integer */
			return NumberTools.getInteger(valueEdit,true);
		case 1:
			/* Long */
			return NumberTools.getLong(valueEdit,true);
		case 2:
			/* Double */
			return NumberTools.getDouble(valueEdit,true);
		case 3:
			/* String */
			valueEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			return valueEdit.getText();
		default:
			return null;
		}
	}
}
