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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationPointerMeasuring}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationPointerMeasuring
 */
public class ModelElementAnimationPointerMeasuringDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4565047267829598870L;

	/** Eingabefeld für den Rechenausdruck */
	private AnimationExpressionPanel editExpression;
	/** Eingabefeld für den Minimalwert der Anzeigeakala */
	private JTextField editMinValue;
	/** Eingabefeld für den Maximalwert der Anzeigeakala */
	private JTextField editMaxValue;
	/** Option: Gelben Bereich verwenden? */
	private JCheckBox optionUseYellowArea;
	/** Eingabefeld für den Startwert des gelben Bereichs */
	private JTextField editYellowAreaStartValue;
	/** Option: Roten Bereich verwenden? */
	private JCheckBox optionUseRedArea;
	/** Eingabefeld für den Startwert des roten Bereichs */
	private JTextField editRedAreaStartValue;

	/** Auswahl der Farbe des Zeigers */
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationPointerMeasuring}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationPointerMeasuringDialog(final Component owner, final ModelElementAnimationPointerMeasuring element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationPointerMeasuring.Dialog.Title"),element,"ModelElementAnimationPointerMeasuring",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationPointerMeasuring;
	}

	@Override
	protected JComponent getContentPanel() {
		JPanel line, cell;
		JLabel label;
		Object[] data;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		content.add(editExpression=new AnimationExpressionPanel(element,((ModelElementAnimationPointerMeasuring)element).getExpression(),readOnly,helpRunnable));

		/* Minimalwert */
		data=getInputPanel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MinValue")+":","",10);
		content.add((JPanel)data[0]);
		editMinValue=(JTextField)data[1];
		editMinValue.setEditable(!readOnly);
		editMinValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Maximalwert */
		data=getInputPanel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MaxValue")+":","",10);
		content.add((JPanel)data[0]);
		editMaxValue=(JTextField)data[1];
		editMaxValue.setEditable(!readOnly);
		editMaxValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Gelber Bereich */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionUseYellowArea=new JCheckBox(Language.tr("Surface.AnimationPointerMeasuring.Dialog.YellowAreaStartValue")));
		optionUseYellowArea.setEnabled(!readOnly);
		optionUseYellowArea.addActionListener(e->checkData(false));
		line.add(Box.createHorizontalStrut(10));
		line.add(editYellowAreaStartValue=new JTextField("",7));
		ModelElementBaseDialog.addUndoFeature(editYellowAreaStartValue);
		editYellowAreaStartValue.setEditable(!readOnly);
		editYellowAreaStartValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Roter Bereich */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionUseRedArea=new JCheckBox(Language.tr("Surface.AnimationPointerMeasuring.Dialog.RedAreaStartValue")));
		optionUseRedArea.setEnabled(!readOnly);
		optionUseRedArea.addActionListener(e->checkData(false));
		line.add(Box.createHorizontalStrut(10));
		line.add(editRedAreaStartValue=new JTextField("",7));
		ModelElementBaseDialog.addUndoFeature(editRedAreaStartValue);
		editRedAreaStartValue.setEditable(!readOnly);
		editRedAreaStartValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.Color")+":"),BorderLayout.NORTH);
		cell.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);
		label.setLabelFor(colorChooser);

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationPointerMeasuring) {
			final ModelElementAnimationPointerMeasuring pointerMeasuring=(ModelElementAnimationPointerMeasuring)element;
			editMinValue.setText(""+pointerMeasuring.getMinValue());
			editMaxValue.setText(""+pointerMeasuring.getMaxValue());
			colorChooser.setColor(pointerMeasuring.getColor());
			optionUseYellowArea.setSelected(pointerMeasuring.isYellowRangeUse());
			int yellowStart;
			if (pointerMeasuring.isYellowRangeUse()) {
				yellowStart=pointerMeasuring.getYellowRangeStart();
				if (yellowStart<pointerMeasuring.getMinValue()) yellowStart=pointerMeasuring.getMinValue();
				if (yellowStart>pointerMeasuring.getMaxValue()) yellowStart=pointerMeasuring.getMaxValue();
			} else {
				yellowStart=(int)Math.round(0.7*pointerMeasuring.getMaxValue());
			}
			editYellowAreaStartValue.setText(""+yellowStart);
			optionUseRedArea.setSelected(pointerMeasuring.isRedRangeUse());
			int redStart;
			if (pointerMeasuring.isRedRangeUse()) {
				redStart=pointerMeasuring.getRedRangeStart();
				if (redStart<pointerMeasuring.getMinValue()) redStart=pointerMeasuring.getMinValue();
				if (redStart>pointerMeasuring.getMaxValue()) redStart=pointerMeasuring.getMaxValue();
			} else {
				redStart=(int)Math.round(0.9*pointerMeasuring.getMaxValue());
			}
			editRedAreaStartValue.setText(""+redStart);
		}

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		/* Ausdruck */
		if (!editExpression.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		/* Minimalwert */
		final Long L1=NumberTools.getNotNegativeLong(editMinValue,true);
		if (L1==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.MinValue.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MinValue.Error.Info"),editMinValue.getText()));
				return false;
			}
		}

		/* Maximalwert */
		final Long L2=NumberTools.getPositiveLong(editMaxValue,true);
		if (L2==null || (L1!=null && L2.longValue()<=L1.longValue())) {
			editMaxValue.setBackground(Color.red); /* Müssen wir manuell einfärben, da die obige Funktion nur prüft, ob die Zahl als solches gültig ist. */
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.MaxValue.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MaxValue.Error.Info"),editMaxValue.getText()));
				return false;
			}
		}

		/* Gelber Bereich */
		if (optionUseYellowArea.isSelected()) {
			final Long L=NumberTools.getNotNegativeLong(editYellowAreaStartValue,true);
			if (L==null || (L1!=null && L.longValue()<L1.longValue()) || (L2!=null && L.longValue()>L2.longValue())) {
				editYellowAreaStartValue.setBackground(Color.red); /* Müssen wir manuell einfärben, da die obige Funktion nur prüft, ob die Zahl als solches gültig ist. */
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.YellowAreaStartValue.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.YellowAreaStartValue.Error.Info"),editYellowAreaStartValue.getText()));
					return false;
				}
			}
		} else {
			editYellowAreaStartValue.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Roter Bereich */
		if (optionUseRedArea.isSelected()) {
			final Long L=NumberTools.getNotNegativeLong(editRedAreaStartValue,true);
			if (L==null || (L1!=null && L.longValue()<L1.longValue()) || (L2!=null && L.longValue()>L2.longValue())) {
				editRedAreaStartValue.setBackground(Color.red); /* Müssen wir manuell einfärben, da die obige Funktion nur prüft, ob die Zahl als solches gültig ist. */
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.RedAreaStartValue.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.RedAreaStartValue.Error.Info"),editRedAreaStartValue.getText()));
					return false;
				}
			}
		} else {
			editRedAreaStartValue.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationPointerMeasuring) {
			final ModelElementAnimationPointerMeasuring pointerMeasuring=(ModelElementAnimationPointerMeasuring)element;
			editExpression.storeData();
			pointerMeasuring.setMinValue(NumberTools.getNotNegativeLong(editMinValue,true).intValue());
			pointerMeasuring.setMaxValue(NumberTools.getNotNegativeLong(editMaxValue,true).intValue());
			pointerMeasuring.setYellowRangeUse(optionUseYellowArea.isSelected());
			if (optionUseYellowArea.isSelected()) pointerMeasuring.setYellowRangeStart(NumberTools.getNotNegativeLong(editYellowAreaStartValue,true).intValue());
			pointerMeasuring.setRedRangeUse(optionUseRedArea.isSelected());
			if (optionUseRedArea.isSelected()) pointerMeasuring.setRedRangeStart(NumberTools.getNotNegativeLong(editRedAreaStartValue,true).intValue());
			pointerMeasuring.setColor(colorChooser.getColor());
		}
	}
}