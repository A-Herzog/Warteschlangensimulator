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
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.LabeledColorChooserButton;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementAnimationLCD}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationLCD
 */
public class ModelElementAnimationLCDDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1510688071490180154L;

	/**
	 * Eingabefeld f�r den auszugebenden Ausdruck
	 */
	private AnimationExpressionPanel editExpression;

	/**
	 * Anzahl an darzustellenden 7-Segment-Ziffern
	 */
	private SpinnerNumberModel spinDigits;

	/**
	 * Linienbreite f�r die darzustellenden 7-Segment-Ziffern
	 */
	private SpinnerNumberModel spinSegmentLineWidth;

	/**
	 * Auswahl der Farbe f�r die aktiven Segmente
	 */
	private LabeledColorChooserButton colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationLCD}
	 * @param readOnly	Nur-Lese-Modus
	 */
	public ModelElementAnimationLCDDialog(final Component owner, final ModelElementAnimationLCD element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationLCD.Dialog.Title"),element,"ModelElementAnimationLCD",readOnly);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationLCD;
	}

	@Override
	protected JComponent getContentPanel() {
		JPanel line;
		JLabel label;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		content.add(editExpression=new AnimationExpressionPanel(element,((ModelElementAnimationLCD)element).getExpression(),readOnly,helpRunnable,new ArrayList<>()));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		/* Anzahl an Ziffern */
		line.add(label=new JLabel(Language.tr("Surface.AnimationLCD.Dialog.Digits")+":"));
		final JSpinner spinDigitsSpinner=new JSpinner(spinDigits=new SpinnerNumberModel(1,1,10,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinDigitsSpinner);
		editor.getFormat().setGroupingUsed(false);
		spinDigitsSpinner.setEditor(editor);
		line.add(spinDigitsSpinner);
		label.setLabelFor(spinDigitsSpinner);
		spinDigitsSpinner.setEnabled(!readOnly);

		/* Linienbreite */
		line.add(label=new JLabel(Language.tr("Surface.AnimationLCD.Dialog.SegmentLineWidth")+":"));
		final JSpinner spinSegmentLineWidthSpinner=new JSpinner(spinSegmentLineWidth=new SpinnerNumberModel(1,1,10,1));
		final JSpinner.NumberEditor editorSegmentLineWidth=new JSpinner.NumberEditor(spinSegmentLineWidthSpinner);
		editorSegmentLineWidth.getFormat().setGroupingUsed(false);
		spinSegmentLineWidthSpinner.setEditor(editorSegmentLineWidth);
		line.add(spinSegmentLineWidthSpinner);
		label.setLabelFor(spinSegmentLineWidthSpinner);
		spinSegmentLineWidthSpinner.setEnabled(!readOnly);

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.AnimationLCD.Dialog.Color")+":",Color.BLACK));
		colorChooser.setEnabled(!readOnly);

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationLCD) {
			final ModelElementAnimationLCD lcd=(ModelElementAnimationLCD)element;
			spinDigits.setValue(lcd.getDigits());
			spinSegmentLineWidth.setValue(lcd.getSegmentLineWidth());
			colorChooser.setColor(lcd.getColor());
		}

		checkData(false);

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

		/* Ausdruck */
		if (!editExpression.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationLCD) {
			final ModelElementAnimationLCD lcd=(ModelElementAnimationLCD)element;
			editExpression.storeData();
			lcd.setDigits((Integer)spinDigits.getValue());
			lcd.setSegmentLineWidth((Integer)spinSegmentLineWidth.getValue());
			lcd.setColor(colorChooser.getColor());
		}
	}
}
