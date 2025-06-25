/**
 * Copyright 2025 Alexander Herzog
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAssignMulti}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAssignMultiDialog
 */
public class ModelElementAssignMultiDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8936818365912399316L;

	/**
	 * Eingabepanel für die Namen der neuen Kundentypen
	 */
	private NewClientTypePanel newClientTypes;


	/**
	 * Eingabepanel für die Verzweigungsregeln
	 */
	private DecideDataPanel decideDataPanel;

	/**
	 * Anzahl an Verzweigungsoptionsn
	 */
	private int lastOptionsCount;

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
	 * @param element	Zu bearbeitendes {@link ModelElementAssignMulti}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAssignMultiDialog(final Component owner, final ModelElementAssignMulti element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AssignMulti.Dialog.Title"),element,"ModelElementAssignMulti",readOnly,false);
		setVisible(true);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAssignMulti;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementAssignMulti assignMulti=(ModelElementAssignMulti)element;
		final EditModel model=element.getModel();

		final JPanel content=new JPanel(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tab;
		JPanel line;

		/* Neue Kundentypen */
		tabs.addTab(Language.tr("Surface.AssignMulti.Dialog.NewClientTypes"),tab=new JPanel(new BorderLayout()));
		tab.add(newClientTypes=new NewClientTypePanel(element.getModel(),readOnly,Arrays.asList(assignMulti.getNewClientTypes()),element.getModel().surface.getClientTypes()));

		/* Optionale Bedingung */
		final JPanel bottomArea=new JPanel();
		bottomArea.setLayout(new BoxLayout(bottomArea,BoxLayout.PAGE_AXIS));
		tab.add(bottomArea,BorderLayout.SOUTH);

		bottomArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.AssignMulti.Dialog.Condition.UseCondition")+":",!assignMulti.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AssignMulti.Dialog.Condition.Condition")+":",assignMulti.getCondition());
		bottomArea.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,model,model.surface),BorderLayout.EAST);


		/* Verzweigungsregeln */
		tabs.addTab(Language.tr("Surface.AssignMulti.Dialog.BranchingRules"),tab=new JPanel(new BorderLayout()));
		tab.add(decideDataPanel=new DecideAndTeleportDecideDataPanel());

		/* Icons */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ASSIGN_MULTI_CLIENT_TYPE.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_ASSIGN_MULTI_CONDITION.getIcon());

		lastOptionsCount=newClientTypes.getNewClientTypes().size();

		/* Starten */
		newClientTypes.addDataChangedListener(()->rebuildDecidePanel());

		return content;
	}

	/**
	 * Verzweigungsregeln-Panel
	 * @see DecideDataPanel
	 */
	private class DecideAndTeleportDecideDataPanel extends DecideDataPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-146294682529312397L;

		/**
		 * Konstruktor der Klasse
		 * (legt basierend auf dem Element ein neues Panel an)
		 */
		public DecideAndTeleportDecideDataPanel() {
			super(element,readOnly);
		}

		/**
		 * Konstruktor der Klasse
		 * (überträgt die Daten aus einem alten Panel in ein neues)
		 * @param oldDecideDataPanel	Altes Panel aus dem die Daten übernommen werden sollen
		 */
		public DecideAndTeleportDecideDataPanel(final DecideDataPanel oldDecideDataPanel) {
			super(element,oldDecideDataPanel,readOnly);
		}

		@Override
		protected List<String> getDestinations() {
			return newClientTypes.getNewClientTypes().stream().map(name->String.format(Language.tr("Surface.AssignMulti.Dialog.ClientTypeLabel"),name)).collect(Collectors.toList());
		}
	}

	/**
	 * Aktualisiert das Verzweigungsregeln-Panel vollständig
	 */
	private void rebuildDecidePanelNow() {
		final JPanel tab=(JPanel)decideDataPanel.getParent();
		final DecideDataPanel oldDecideDataPanel=decideDataPanel;
		tab.remove(oldDecideDataPanel);
		tab.add(decideDataPanel=new DecideAndTeleportDecideDataPanel(oldDecideDataPanel));
	}

	/**
	 * Timer zur Verzögerten Aktualisierung des Verzweigungsregeln-Panels
	 * @see #rebuildDecidePanel()
	 */
	private Timer rebuildDecidePanelTimer=null;

	/**
	 * Aktualisiert Anzahl und Bezeichnungen im Verzweigungsregeln-Panel
	 */
	private void rebuildDecidePanel() {
		final int newOptionCount=newClientTypes.getNewClientTypes().size();
		if (lastOptionsCount==newOptionCount) {
			decideDataPanel.updateLabels();
			return;
		}
		lastOptionsCount=newOptionCount;

		if (rebuildDecidePanelTimer!=null) {
			rebuildDecidePanelTimer.stop();
			rebuildDecidePanelTimer=null;
		}

		rebuildDecidePanelTimer=new Timer(200,e->{
			((Timer)e.getSource()).stop();
			rebuildDecidePanelTimer=null;
			rebuildDecidePanelNow();
		});
		rebuildDecidePanelTimer.start();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		final EditModel model=element.getModel();

		/* Neue Kundentypen */
		if (!newClientTypes.checkData(showErrorMessage)) {
			ok=false;
			if (showErrorMessage) return false;
		}

		/* Optionale Bedingung */
		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AssignMulti.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.AssignMulti.Dialog.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		/* Verzweigungsregeln */
		if (!decideDataPanel.checkData(showErrorMessage)) {
			ok=false;
			if (showErrorMessage) return false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementAssignMulti assignMulti=(ModelElementAssignMulti)element;

		/* Neue Kundentypen */
		assignMulti.getNewClientTypesList().clear();
		assignMulti.getNewClientTypesList().addAll(newClientTypes.getNewClientTypes());

		/* Optionale Bedingung */
		if (useCondition.isSelected()) assignMulti.setCondition(condition.getText().trim()); else assignMulti.setCondition("");

		/* Verzweigungsregeln */
		decideDataPanel.storeData();
	}
}
