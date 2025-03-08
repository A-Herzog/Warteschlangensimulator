/**
 * Copyright 2023 Alexander Herzog
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
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Stellt ein Panel zur Bearbeitung der Daten in {@link CounterCondition} bereit.
 * @author Alexander Herzog
 * @see CounterCondition
 */
public class CounterConditionPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7202385740634603214L;

	/** Liste mit allen im Modell vorhandenen Kundentypnamen */
	private final List<String> clientTypesAll;
	/** Aktuell ausgewählte Kundentypen */
	private final List<String> clientTypes;
	/** Liste mit allen Variablennamen im Modell */
	private final String[] variableNames;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private final ExpressionCalcModelUserFunctions userFunctions;

	/** Auswahlbox für die Bedingung */
	private final JCheckBox checkCondition;
	/** Eingabefeld für die Bedingung */
	private final JTextField editCondition;
	/** Auswahlbox für die Kundentypen */
	private final JCheckBox checkClientTypes;
	/** Info-Label zur Anzeige der Anzahl an ausgewählten Kundentypen */
	private final JLabel infoClientTypes;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editormodell
	 * @param surface	Zeichenfläche
	 * @param readOnly	Nur-Lese-Status
	 */
	public CounterConditionPanel(final EditModel model, final ModelSurface surface, final boolean readOnly) {
		clientTypesAll=surface.getClientTypes();
		clientTypes=new ArrayList<>();
		variableNames=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true);
		userFunctions=model.userFunctions;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		JPanel line;

		/* Bedingung */
		add(line=new JPanel(new BorderLayout()));
		line.add(checkCondition=new JCheckBox(Language.tr("Surface.CounterCondition.Panel.Condition")+":"),BorderLayout.WEST);
		checkCondition.setEnabled(!readOnly);
		checkCondition.addActionListener(e->checkData(false));
		line.add(editCondition=new JTextField(),BorderLayout.CENTER);
		editCondition.setEnabled(!readOnly);
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,editCondition,true,true,model,surface),BorderLayout.EAST);
		editCondition.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) {checkCondition.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkCondition.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkCondition.setSelected(true); checkData(false);}
		});

		/* Kundentypen */
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT,0,5)));
		line.add(checkClientTypes=new JCheckBox(Language.tr("Surface.CounterCondition.Panel.ClientTypes")));
		checkClientTypes.setEnabled(!readOnly);
		checkClientTypes.addActionListener(e->checkData(false));
		line.add(Box.createHorizontalStrut(5));
		final JButton button=new JButton(Language.tr("Surface.CounterCondition.Panel.ClientTypes.Button"),Images.MODELPROPERTIES_CLIENTS.getIcon());
		button.setEnabled(!readOnly);
		line.add(button);
		button.addActionListener(e->editClientTypesList());
		line.add(Box.createHorizontalStrut(5));
		line.add(infoClientTypes=new JLabel());
	}

	/**
	 * Befehl: Liste der Kundentypen bearbeiten
	 */
	private void editClientTypesList() {
		final CounterConditionDialog dialog=new CounterConditionDialog(this,clientTypesAll,clientTypes);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			clientTypes.clear();
			clientTypes.addAll(dialog.getSelectedClientTypes());
			checkClientTypes.setSelected(clientTypes.size()>0 && clientTypes.size()<clientTypesAll.size());
			checkData(false);
		}
	}

	/**
	 * Stellt die Daten aus einem {@link CounterCondition}-Objekt in diesem Panel ein.
	 * @param counterCondition	Auszulesendes Objekt
	 * @see #getData(CounterCondition)
	 */
	public void setData(final CounterCondition counterCondition) {
		/* Bedingung */
		final String condition=counterCondition.getCondition().trim();
		editCondition.setText(condition);
		checkCondition.setSelected(!condition.isEmpty());

		/* Kundentypen */
		clientTypes.clear();
		clientTypes.addAll(counterCondition.getClientTypes());
		checkClientTypes.setSelected(clientTypes.size()>0);

		checkData(false);
	}

	/**
	 * Prüft die Einstellungen in diesem Panel.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Bedingung */
		if (checkCondition.isSelected()) {
			final String text=editCondition.getText();
			if (text.isBlank()) {
				ok=false;
				editCondition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.CounterCondition.Panel.Condition.Error.Title"),Language.tr("Surface.CounterCondition.Panel.Condition.Error.InfoEmpty"));
					return false;
				}
			} else {
				final int error=ExpressionMultiEval.check(text,variableNames,userFunctions);
				if (error>=0) {
					ok=false;
					editCondition.setBackground(Color.RED);
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("Surface.CounterCondition.Panel.Condition.Error.Title"),String.format(Language.tr("Surface.CounterCondition.Panel.Condition.Error.Info"),text,error+1));
						return false;
					}
				} else {
					editCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
				}
			}
		} else {
			editCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Kundentypen */
		if (checkClientTypes.isSelected() && clientTypes.size()>0 && clientTypes.size()<clientTypesAll.size()) {
			if (clientTypes.size()==1) {
				infoClientTypes.setText(Language.tr("Surface.CounterCondition.Panel.ClientTypes.InfoSingular"));
			} else {
				infoClientTypes.setText(String.format(Language.tr("Surface.CounterCondition.Panel.ClientTypes.InfoPlural"),clientTypes.size()));
			}
		} else {
			infoClientTypes.setText(Language.tr("Surface.CounterCondition.Panel.ClientTypes.InfoAll"));
		}

		return ok;
	}

	/**
	 * Schreibt die Daten aus diesem Panel in ein {@link CounterCondition}-Objekt zurück
	 * @param counterCondition	Objekt in das die Daten geschrieben werden sollen
	 * @see #setData(CounterCondition)
	 * @see #checkData(boolean)
	 */
	public void getData(final CounterCondition counterCondition) {
		/* Bedingung */
		if (checkCondition.isSelected()) counterCondition.setCondition(editCondition.getText().trim());
		counterCondition.getClientTypes().clear();

		/* Kundentypen */
		if (clientTypes.size()==0 || clientTypes.size()==clientTypesAll.size()) {
			counterCondition.getClientTypes().clear();
		} else {
			counterCondition.getClientTypes().clear();
			counterCondition.getClientTypes().addAll(clientTypes);
		}
	}

}
