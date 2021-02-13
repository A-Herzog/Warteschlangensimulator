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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zur Konfiguration der zu erzeugenden Histogramms an
 * (über das Kontextmenü von Stationen).
 * @author Alexander Herzog
 * @see ModelElementBox#addVisualizationMenuItem(javax.swing.JMenu, java.util.function.Consumer, ui.modeleditor.coreelements.ModelElementBox.VisualizationType)
 * @see ModelElementBox.VisualizationType#HISTOGRAM_WIP
 * @see ModelElementBox.VisualizationType#HISTOGRAM_NQ
 */
public class ModelElementBoxHistogramDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2886436112922001876L;

	/**
	 * Eingabefeld für die Anzahl an Balken im Histogramm
	 * @see #getBarCount()
	 */
	private final JTextField editCount;

	/**
	 * Eingabefeld für die Anzahl an Werten pro Balken im Histogramm
	 * @see #getValuesPerBar()
	 */
	private final JTextField editStepWidth;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ModelElementBoxHistogramDialog(final Component owner) {
		super(owner,Language.tr("Surface.AnimationBarChart.HistogramWizard.Title"));

		/* GUI aufbauen */
		final JPanel content=createGUI(null);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] data;

		/* Anzahl an Balken */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarChart.HistogramWizard.Count")+":","20",5);
		content.add((JPanel)data[0]);
		editCount=(JTextField)data[1];
		editCount.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Schrittweite */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarChart.HistogramWizard.StepWidth")+":","5",5);
		content.add((JPanel)data[0]);
		editStepWidth=(JTextField)data[1];
		editStepWidth.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;
		Integer I;

		/* Anzahl an Balken */
		I=NumberTools.getInteger(editCount,true);
		if (I==0 || I<2) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.AnimationBarChart.HistogramWizard.Count.ErrorTitle"),String.format(Language.tr("Surface.AnimationBarChart.HistogramWizard.Count.ErrorInfo2"),editCount.getText()));
				return false;
			}
		}

		/* Schrittweite */
		I=NumberTools.getInteger(editStepWidth,true);
		if (I==0 || I<1) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.AnimationBarChart.HistogramWizard.StepWidth.ErrorTitle"),String.format(Language.tr("Surface.AnimationBarChart.HistogramWizard.StepWidth.ErrorInfo"),editStepWidth.getText()));
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
	 * Liefert die Anzahl an Balken im Histogramm.
	 * @return	Anzahl an Balken im Histogramm
	 * @see #editCount
	 */
	public int getBarCount() {
		return NumberTools.getInteger(editCount,true);
	}

	/**
	 * Liefert die Anzahl an Werten pro Balken im Histogramm.
	 * @return	Anzahl an Werten pro Balken im Histogramm
	 * @see #editStepWidth
	 */
	public int getValuesPerBar() {
		return NumberTools.getInteger(editStepWidth,true);
	}
}
