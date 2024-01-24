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
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationLCD}-Element anbietet
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
	 * Eingabefeld für den auszugebenden Ausdruck
	 */
	private AnimationExpressionPanel editExpression;

	/**
	 * Anzahl an darzustellenden 7-Segment-Ziffern
	 */
	private SpinnerNumberModel spinDigits;

	/**
	 * Auswahl der Farbe für die aktiven Segmente
	 */
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationLCD}
	 * @param readOnly	Nur-Lese-Modus
	 */
	public ModelElementAnimationLCDDialog(final Component owner, final ModelElementAnimationLCD element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationLCD.Dialog.Title"),element,"ModelElementAnimationLCD",readOnly);
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
		return InfoPanel.stationAnimationLCD;
	}

	@Override
	protected JComponent getContentPanel() {
		JPanel line, cell;
		JLabel label;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		content.add(editExpression=new AnimationExpressionPanel(element,((ModelElementAnimationLCD)element).getExpression(),readOnly,helpRunnable,new ArrayList<>()));

		/* Ziffern */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationLCD.Dialog.Digits")+":"));
		final JSpinner spinDigitsSpinner=new JSpinner(spinDigits=new SpinnerNumberModel(1,1,10,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinDigitsSpinner);
		editor.getFormat().setGroupingUsed(false);
		spinDigitsSpinner.setEditor(editor);
		line.add(spinDigitsSpinner);
		label.setLabelFor(spinDigitsSpinner);
		spinDigitsSpinner.setEnabled(!readOnly);

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationLCD.Dialog.Color")+":"),BorderLayout.NORTH);
		cell.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);
		label.setLabelFor(colorChooser);

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationLCD) {
			final ModelElementAnimationLCD lcd=(ModelElementAnimationLCD)element;
			spinDigits.setValue(lcd.getDigits());
			colorChooser.setColor(lcd.getColor());
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

		if (element instanceof ModelElementAnimationLCD) {
			final ModelElementAnimationLCD lcd=(ModelElementAnimationLCD)element;
			editExpression.storeData();
			lcd.setDigits((Integer)spinDigits.getValue());
			lcd.setColor(colorChooser.getColor());
		}
	}
}
