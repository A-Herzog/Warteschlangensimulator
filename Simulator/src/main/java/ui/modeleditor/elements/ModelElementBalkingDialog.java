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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementBalking}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementBalking
 */
public class ModelElementBalkingDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1324564696068004152L;

	/** Wert "Global" + Liste aller Kundentypennamen */
	private String[] clientNames;
	/** Icons für die Einträge in {@link #clientNames} */
	private Object[] clientIcons;
	/** Gewähltes Zurückschreckverhalten pro Eintrag von {@link #clientNames} */
	private Object[] clientData;

	/** Letzter gewählter Eintrag in {@link #combo} */
	private int lastCombo;
	/** Auswahlbox des Kundentyps */
	private JComboBox<String> combo;
	/** Option: Globale Vorgabe verwenden */
	private JRadioButton optionGlobal;
	/** Option: Zurückschreckwahrscheinlichkeit */
	private JRadioButton optionProbability;
	/** Option: Zurückschrecken gemäß Bedingung */
	private JRadioButton optionCondition;

	/** Eingabefeld für die Zurückschreckwahrscheinlichkeit ({@link #optionProbability}) */
	private JTextField probability;
	/** Eingabefeld für die Zurückschreckbedingung ({@link #optionCondition}) */
	private JTextField expression;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementBalking}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementBalkingDialog(final Component owner, final ModelElementBalking element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Balking.Dialog.Title"),element,"ModelElementBalking",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Bereitet {@link #clientNames} und {@link #clientIcons} vor
	 * und lädt die Daten aus der Station in {@link #clientData}.
	 * @see #clientNames
	 * @see #clientIcons
	 * @see #clientData
	 */
	private void loadData() {
		final ModelElementBalking balking=(ModelElementBalking)element;

		final List<String> namesList=((element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface()).getClientTypes();
		namesList.add(0,Language.tr("Surface.Balking.Dialog.ClientType.GlobalName"));
		clientNames=namesList.toArray(new String[0]);

		clientIcons=new Object[clientNames.length];
		clientIcons[0]=Images.MODELPROPERTIES_CLIENTS_GROUPS.getIcon();
		final AnimationImageSource imageSource=new AnimationImageSource();
		for (int i=1;i<clientNames.length;i++) {
			String icon=element.getModel().clientData.getIcon(clientNames[i]);
			if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
			clientIcons[i]=imageSource.get(icon,element.getModel().animationImages,16);
		}

		clientData=new Object[clientNames.length];
		clientData[0]=balking.getGlobalData().getObject();
		if (clientData[0]==null) clientData[0]=Double.valueOf(0.0);

		final List<ModelElementBalkingData> list=balking.getClientTypeData();
		for (int i=1;i<clientData.length;i++) {
			int index=-1;
			for (int j=0;j<list.size();j++) if (list.get(j).getClientType().equals(clientNames[i])) {index=j; break;}
			if (index>=0) clientData[i]=list.get(index).getObject();
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationBalking;
	}

	@Override
	protected JComponent getContentPanel() {
		loadData();

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		/* Kundentyp-Combobox */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(label=new JLabel(Language.tr("Surface.Balking.Dialog.ClientType")+":"));
		line.add(combo=new JComboBox<>(clientNames));
		combo.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildIconsList(this,clientIcons)));
		label.setLabelFor(combo);
		combo.addActionListener(e->comboChanged());

		/* Option: "Globale Vorgabe verwenden" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(optionGlobal=new JRadioButton(Language.tr("Surface.Balking.Dialog.Option.Global")));

		/* Option: "Zurückschrecken gemäß Wahrscheinlichkeit" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(optionProbability=new JRadioButton(Language.tr("Surface.Balking.Dialog.Option.Probability")+":"));
		optionProbability.setEnabled(!readOnly);
		line.add(probability=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(probability);
		probability.setEditable(!readOnly);
		probability.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionProbability.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionProbability.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionProbability.setSelected(true); checkData(false);}
		});

		/* Option: "Zurückschrecken gemäß Bedingung" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(optionCondition=new JRadioButton(Language.tr("Surface.Balking.Dialog.Option.Condition")));
		optionCondition.setEnabled(!readOnly);

		final Object[] data=getInputPanel(Language.tr("Surface.Balking.Dialog.Expression")+":","");
		line=(JPanel)data[0];
		content.add(line);
		expression=(JTextField)data[1];
		expression.setEditable(!readOnly);
		expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionCondition.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionCondition.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionCondition.setSelected(true); checkData(false);}
		});
		line.add(getExpressionEditButton(this,expression,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionGlobal);
		buttonGroup.add(optionProbability);
		buttonGroup.add(optionCondition);

		lastCombo=-1;
		combo.setSelectedIndex(0);
		checkData(false);

		return content;
	}

	/**
	 * Reagiert auf Änderungen in der Auswahl in {@link #combo}
	 * @see #combo
	 */
	private void comboChanged() {
		if (lastCombo>=0) {
			Object obj=null;
			if (optionProbability.isSelected()) {
				final Double D=NumberTools.getProbability(probability,false);
				if (D==null) obj=Double.valueOf(0.0); else obj=D;
			}
			if (optionCondition.isSelected()) {
				obj=expression.getText();
			}
			clientData[lastCombo]=obj;
		}

		lastCombo=combo.getSelectedIndex();

		final Object obj=clientData[lastCombo];
		if (obj==null) {
			optionGlobal.setSelected(true);
			probability.setText("0%");
			expression.setText("");
		} else {
			if (obj instanceof Double) {
				optionProbability.setSelected(true);
				probability.setText(NumberTools.formatPercent((Double)obj));
				expression.setText("");
			}
			if (obj instanceof String) {
				optionCondition.setSelected(true);
				probability.setText("0%");
				expression.setText((String)obj);
			}
		}

		optionGlobal.setEnabled(!readOnly && lastCombo>0);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		if (optionProbability.isSelected()) {
			final Double D=NumberTools.getProbability(probability,true);
			if (D==null) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Balking.Dialog.Probability.Error.Title"),String.format(Language.tr("Surface.Balking.Dialog.Probability.Error.Info"),probability.getText()));
					return false;
				}
			}
		} else {
			probability.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (optionCondition.isSelected()) {
			final int error=ExpressionMultiEval.check(expression.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),element.getModel().userFunctions);
			if (error>=0) {
				ok=false;
				expression.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Balking.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.Balking.Dialog.Expression.Error.Info"),expression.getText(),error+1));
					return false;
				}
			} else {
				expression.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			expression.setBackground(NumberTools.getTextFieldDefaultBackground());
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

		comboChanged();
		final ModelElementBalking balking=(ModelElementBalking)element;

		if (clientData[0] instanceof String) {
			balking.getGlobalData().setExpression((String)clientData[0]);
		} else {
			if (clientData[0] instanceof Double) {
				balking.getGlobalData().setProbability((Double)clientData[0]);
			} else {
				balking.getGlobalData().setProbability(0);
			}
		}

		balking.getClientTypeData().clear();
		for (int i=1;i<clientData.length;i++) {
			if (clientData[i] instanceof String) {
				balking.getClientTypeData().add(new ModelElementBalkingData((String)clientData[i],clientNames[i]));
			}
			if (clientData[i] instanceof Double) {
				balking.getClientTypeData().add(new ModelElementBalkingData((Double)clientData[i],clientNames[i]));
			}
		}
	}
}
