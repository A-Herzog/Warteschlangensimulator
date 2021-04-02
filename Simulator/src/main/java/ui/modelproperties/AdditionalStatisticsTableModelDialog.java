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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import statistics.StatisticsLongRunPerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelLongRunStatisticsElement;

/**
 * Diese Klasse stellt einen Dialog zum Bearbeiten eines einzelnen
 * Eintrags in einem {@link AdditionalStatisticsTableModel} bereit.
 * @author Alexander Herzog
 * @see AdditionalStatisticsTableModel
 */
public class AdditionalStatisticsTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5752129414122427124L;

	/** Vollständiges Editor-Modell (wird für den Expression-Builder benötigt) */
	private final EditModel model;
	/** Eingabefeld für den Rechenausdruck, der in der Statistik erfasst werden soll */
	private JTextField expressionEdit;
	/** Art der Aufzeichnung des Ausdrucks */
	private JComboBox<String> modeCombo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param element	Zu bearbeitender Datensatz
	 * @param model	Vollständiges Editor-Modell (wird für den Expression-Builder benötigt)
	 * @param helpRunnable	Hilfe-Callback
	 */
	public AdditionalStatisticsTableModelDialog(final Component owner, final ModelLongRunStatisticsElement element, final EditModel model, final Runnable helpRunnable) {
		super(owner,Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Title"));
		this.model=model;

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */

		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Expression"),(element==null)?"":element.expression);
		content.add((JPanel)obj[0]);
		expressionEdit=(JTextField)obj[1];
		((JPanel)obj[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,false,model,model.surface),BorderLayout.EAST);
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Modus */

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		final JLabel label=new JLabel(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Mode")+":");
		line.add(label);
		line.add(modeCombo=new JComboBox<>(new String[] {
				Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Mode.Average"),
				Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Mode.Min"),
				Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Mode.Max")
		}));
		label.setLabelFor(modeCombo);
		if (element==null) {
			modeCombo.setSelectedIndex(0);
		} else switch (element.mode) {
		case MODE_AVERAGE: modeCombo.setSelectedIndex(0); break;
		case MODE_MIN: modeCombo.setSelectedIndex(1); break;
		case MODE_MAX: modeCombo.setSelectedIndex(2); break;
		}

		/* Größe des Dialogs */

		setMinSizeRespectingScreensize(600,0);
		pack();

		/* Start */

		checkData(false);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		int i=ExpressionCalc.check(expressionEdit.getText(),model.surface.getVariableNames(model.getModelVariableNames()));
		if (i>=0) {
			expressionEdit.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Expression.ErrorTitle"),String.format(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Table.Edit.Expression.ErrorInfo"),expressionEdit.getText(),i+1));
			return false;
		} else {
			expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert, wenn der Dialog per "Ok" geschlossen wurde, den neuen,
	 * bearbeiteten Statistikdatensatz
	 * @return	Neuer Statistikdatensatz
	 */
	public ModelLongRunStatisticsElement getElement() {
		final ModelLongRunStatisticsElement element=new ModelLongRunStatisticsElement();

		element.expression=expressionEdit.getText().trim();
		switch (modeCombo.getSelectedIndex()) {
		case 0: element.mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_AVERAGE; break;
		case 1: element.mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_MIN; break;
		case 2: element.mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX; break;
		}

		return element;
	}
}