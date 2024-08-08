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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementClientIcon}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementClientIcon
 */
public class ModelElementClientIconDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3856457045122236178L;

	/** Datenmodell für die Auswahlbox zur Wahl des Icons ({@link #iconChooser}) */
	private DefaultComboBoxModel<JLabel> iconChooserList;
	/** Auswahlbox zur Wahl des Icons */
	private JComboBox<JLabel> iconChooser;

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
	 * @param element	Zu bearbeitendes {@link ModelElementClientIcon}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementClientIconDialog(final Component owner, final ModelElementClientIcon element, final boolean readOnly) {
		super(owner,Language.tr("Surface.ClientIcon.Dialog.Title"),element,"ModelElementClientIcon",readOnly);
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
		return InfoPanel.stationClientIcon;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementClientIcon clientIcon=(ModelElementClientIcon)element;
		final EditModel model=clientIcon.getModel();
		final AnimationImageSource imageSource=new AnimationImageSource();

		final JPanel content=new JPanel(new BorderLayout());

		JPanel line;
		JLabel label;

		final JPanel centerArea=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(centerArea,BorderLayout.CENTER);
		centerArea.add(label=new JLabel(Language.tr("Surface.ClientIcon.Dialog.IconForClient")+":"));
		centerArea.add(iconChooser=new JComboBox<>());
		iconChooserList=imageSource.getIconsComboBox(element.getModel().animationImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		iconChooser.setEnabled(!readOnly);
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		final String icon=clientIcon.getIcon();
		int index=0;
		if (icon!=null) for (int i=0;i<iconChooserList.getSize();i++) {
			String name=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(name,name);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);

		/* Optionale Bedingung */
		final JPanel bottomArea=new JPanel();
		bottomArea.setLayout(new BoxLayout(bottomArea,BoxLayout.PAGE_AXIS));
		content.add(bottomArea,BorderLayout.SOUTH);

		bottomArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useCondition=new JCheckBox(Language.tr("Surface.ClientIcon.Dialog.Condition.UseCondition")+":",!clientIcon.getCondition().isEmpty()));
		useCondition.addActionListener(e->checkData(false));
		useCondition.setEnabled(!readOnly);

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.ClientIcon.Dialog.Condition.Condition")+":",clientIcon.getCondition());
		bottomArea.add(line=(JPanel)data[0]);
		condition=(JTextField)data[1];
		condition.setEnabled(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); useCondition.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,condition,true,true,model,model.surface),BorderLayout.EAST);

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

		boolean ok=true;

		final String conditionString=condition.getText().trim();
		if (!useCondition.isSelected() || conditionString.isEmpty()) {
			condition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(conditionString,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.ClientIcon.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.ClientIcon.Dialog.Condition.Error.Info"),condition,error+1));
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

		final ModelElementClientIcon clientIcon=(ModelElementClientIcon)element;


		String name=iconChooserList.getElementAt(iconChooser.getSelectedIndex()).getText();
		String s=AnimationImageSource.ICONS.getOrDefault(name,name);
		clientIcon.setIcon(s);

		if (useCondition.isSelected()) clientIcon.setCondition(condition.getText().trim()); else clientIcon.setCondition("");
	}
}