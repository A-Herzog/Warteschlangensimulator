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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementCosts}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementCosts
 */
public class ModelElementCostsDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7008913895872273448L;

	/** Liste aller globalen Variablen in dem Modell */
	private String[] variables;

	/** Eingabefeld für die Kosten an der Station */
	private JTextField stationCosts;
	/** Eingabefeld für die zusätzlichen Kunden-Wartezeit-Kosten */
	private JTextField clientWaitingCosts;
	/** Eingabefeld für die zusätzlichen Kunden-Transferzeit-Kosten */
	private JTextField clientTransferCosts;
	/** Eingabefeld für die zusätzlichen Kunden-Bedienzeit-Kosten */
	private JTextField clientProcessCosts;

	/**
	 * Checkbox: Soll die Bedingung verwendet werden?
	 */
	private JCheckBox useCondition;

	/**
	 * Eingabefeld für die Bedingung zur Auslösung der Aktion
	 */
	private JTextField condition;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementCosts}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementCostsDialog(final Component owner, final ModelElementCosts element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Costs.Dialog.Title"),element,"ModelElementCosts",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(400,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationCosts;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementCosts costs=(ModelElementCosts)element;
		final EditModel model=costs.getModel();

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		variables=element.getSurface().getMainSurfaceVariableNames(model.getModelVariableNames(),true);

		Object[] data;
		JPanel line;

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.StationCosts")+":",costs.getStationCosts());
		content.add(line=(JPanel)data[0]);
		stationCosts=(JTextField)data[1];
		stationCosts.setEditable(!readOnly);
		stationCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,stationCosts,false,true,model,element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.WaitingCosts")+":",costs.getClientWaitingCosts());
		content.add(line=(JPanel)data[0]);
		clientWaitingCosts=(JTextField)data[1];
		clientWaitingCosts.setEditable(!readOnly);
		clientWaitingCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,clientWaitingCosts,false,true,model,element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.TransferCosts")+":",costs.getClientTransferCosts());
		content.add(line=(JPanel)data[0]);
		clientTransferCosts=(JTextField)data[1];
		clientTransferCosts.setEditable(!readOnly);
		clientTransferCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,clientTransferCosts,false,true,model,element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.ProcessCosts")+":",costs.getClientProcessCosts());
		content.add(line=(JPanel)data[0]);
		clientProcessCosts=(JTextField)data[1];
		clientProcessCosts.setEditable(!readOnly);
		clientProcessCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,clientProcessCosts,false,true,model,element.getSurface()),BorderLayout.EAST);

		/* Optionale Bedingung */
		final JPanel bottomArea=new JPanel();
		bottomArea.setLayout(new BoxLayout(bottomArea,BoxLayout.PAGE_AXIS));
		content.add(bottomArea,BorderLayout.SOUTH);

		bottomArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.Costs.Dialog.Condition.UseCondition")+":",!costs.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Costs.Dialog.Condition.Condition")+":",costs.getCondition());
		bottomArea.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,model,model.surface),BorderLayout.EAST);


		checkData(false);
		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		final EditModel model=element.getModel();

		String text;

		boolean ok=true;

		text=stationCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				stationCosts.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.StationCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				stationCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			stationCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		text=clientWaitingCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				clientWaitingCosts.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.WaitingCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				clientWaitingCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			clientWaitingCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		text=clientTransferCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				clientTransferCosts.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.TransferCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				clientTransferCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			clientTransferCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		text=clientProcessCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				clientProcessCosts.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.ProcessCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				clientProcessCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			clientProcessCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Costs.Dialog.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
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

		final ModelElementCosts costs=(ModelElementCosts)element;

		costs.setStationCosts(stationCosts.getText().trim());
		costs.setClientWaitingCosts(clientWaitingCosts.getText().trim());
		costs.setClientTransferCosts(clientTransferCosts.getText().trim());
		costs.setClientProcessCosts(clientProcessCosts.getText().trim());
		if (useCondition.isSelected()) costs.setCondition(condition.getText().trim()); else costs.setCondition("");
	}
}