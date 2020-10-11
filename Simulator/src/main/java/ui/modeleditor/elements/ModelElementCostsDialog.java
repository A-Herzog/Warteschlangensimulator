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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionCalc;
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

	private String[] variables;

	private JTextField stationCosts;
	private JTextField clientWaitingCosts;
	private JTextField clientTransferCosts;
	private JTextField clientProcessCosts;

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
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		variables=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true);

		Object[] data;
		JPanel line;

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.StationCosts")+":",((ModelElementCosts)element).getStationCosts());
		content.add(line=(JPanel)data[0]);
		stationCosts=(JTextField)data[1];
		stationCosts.setEditable(!readOnly);
		stationCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,stationCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.WaitingCosts")+":",((ModelElementCosts)element).getClientWaitingCosts());
		content.add(line=(JPanel)data[0]);
		clientWaitingCosts=(JTextField)data[1];
		clientWaitingCosts.setEditable(!readOnly);
		clientWaitingCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,clientWaitingCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.TransferCosts")+":",((ModelElementCosts)element).getClientTransferCosts());
		content.add(line=(JPanel)data[0]);
		clientTransferCosts=(JTextField)data[1];
		clientTransferCosts.setEditable(!readOnly);
		clientTransferCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,clientTransferCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Costs.Dialog.ProcessCosts")+":",((ModelElementCosts)element).getClientProcessCosts());
		content.add(line=(JPanel)data[0]);
		clientProcessCosts=(JTextField)data[1];
		clientProcessCosts.setEditable(!readOnly);
		clientProcessCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(getExpressionEditButton(this,clientProcessCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

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

		String text;

		boolean ok=true;

		text=stationCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				stationCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.StationCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				stationCosts.setBackground(SystemColor.text);
			}
		} else {
			stationCosts.setBackground(SystemColor.text);
		}

		text=clientWaitingCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				clientWaitingCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.WaitingCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				clientWaitingCosts.setBackground(SystemColor.text);
			}
		} else {
			clientWaitingCosts.setBackground(SystemColor.text);
		}

		text=clientTransferCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				clientTransferCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.TransferCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				clientTransferCosts.setBackground(SystemColor.text);
			}
		} else {
			clientTransferCosts.setBackground(SystemColor.text);
		}

		text=clientProcessCosts.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				clientProcessCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Costs.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Costs.Dialog.ProcessCosts.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				clientProcessCosts.setBackground(SystemColor.text);
			}
		} else {
			clientProcessCosts.setBackground(SystemColor.text);
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
		((ModelElementCosts)element).setStationCosts(stationCosts.getText().trim());
		((ModelElementCosts)element).setClientWaitingCosts(clientWaitingCosts.getText().trim());
		((ModelElementCosts)element).setClientTransferCosts(clientTransferCosts.getText().trim());
		((ModelElementCosts)element).setClientProcessCosts(clientProcessCosts.getText().trim());
	}
}