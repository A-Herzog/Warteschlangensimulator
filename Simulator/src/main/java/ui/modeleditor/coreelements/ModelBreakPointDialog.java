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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Zeigt einen Dialog zum Bearbeiten eines Animations-Haltepunktes f�r eine Station an.
 * @author Alexander Herzog
 * @see ModelSurfaceAnimatorBase#getBreakPoint(int)
 * @see ModelSurfaceAnimatorBase#setBreakPoint(int, ui.modeleditor.ModelSurfaceAnimatorBase.BreakPoint)
 */
public class ModelBreakPointDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=170893493440017556L;

	/**
	 * ID der Station auf die sich der Haltepunkt beziehen soll
	 */
	private final int stationID;

	/**
	 * Editor-Modell (f�r Expression-Pr�fung)
	 */
	private final EditModel model;

	/**
	 * Soll an der angegebenen Station ein Haltepunkt aktiv sein?
	 */
	private final JCheckBox checkBoxActive;

	/**
	 * Kundentyp (kann "alle" sein) bei dessen Ankunft der Haltepunkt wirksam werden soll
	 */
	private final JComboBox<?> comboBoxClientTypes;

	/**
	 * Bedingung, die f�r die Ausl�sung des Haltepunktes erf�llt sein muss
	 */
	private final JTextField editCondition;

	/**
	 * Soll sich der Haltepunkt nach einmaliger Aktivierung automatisch l�schen?
	 */
	private final JCheckBox checkBoxAutoDelete;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param model	Editor-Modell (f�r den Expression-Builder / Expression-Pr�fung)
	 * @param simData	Simulationsdatenobjekt (um die Liste der Kundentypen auszulesen)
	 * @param allowEnableDisable	Soll die M�glichkeit geboten werden, den Haltepunkt ein- oder auszuschalten?
	 * @param stationID	ID der Station auf die sich der Haltepunkt beziehen soll
	 * @param breakPoint	Bisheriger Haltepunkt f�r diese Station (kann <code>null</code> sein)
	 */
	public ModelBreakPointDialog(final Component owner, final EditModel model, final SimulationData simData, final boolean allowEnableDisable, final int stationID, final ModelSurfaceAnimatorBase.BreakPoint breakPoint) {
		super(owner,Language.tr("Surface.PopupMenu.BreakPoint.Title"));
		this.stationID=stationID;
		this.model=model;

		/* GUI */
		final JPanel main=createGUI(null);
		main.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		main.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		Object[] data;

		/* Dialog aufbauen */

		if (allowEnableDisable) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(checkBoxActive=new JCheckBox("<html><body><b>"+Language.tr("Surface.PopupMenu.BreakPoint.Option.Active")+"</b></body></html>",breakPoint!=null));
		} else {
			checkBoxActive=new JCheckBox("",breakPoint!=null);
		}

		data=getClientTypesComboBox(model,simData.runModel);
		content.add((JPanel)data[0]);
		comboBoxClientTypes=(JComboBox<?>)data[1];
		comboBoxClientTypes.setSelectedIndex((breakPoint==null)?0:(breakPoint.clientType+1));
		comboBoxClientTypes.addActionListener(e->checkBoxActive.setSelected(true));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.PopupMenu.BreakPoint.Condition")+":",(breakPoint==null)?"":breakPoint.condition);
		content.add(line=(JPanel)data[0]);
		editCondition=(JTextField)data[1];
		editCondition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false); checkBoxActive.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); checkBoxActive.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); checkBoxActive.setSelected(true);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,editCondition,true,true,model,model.surface),BorderLayout.EAST);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkBoxAutoDelete=new JCheckBox(Language.tr("Surface.PopupMenu.BreakPoint.Option.AutoDelete"),(breakPoint!=null && breakPoint.autoDelete)));
		checkBoxAutoDelete.addActionListener(e->checkBoxActive.setSelected(true));

		/* Dialog starten */
		setMinSizeRespectingScreensize(640,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liste die Liste f�r die Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param list	Liste der Kundentypennamen
	 * @return	Liste bestehen aus "Alle" als erstem Eintrag und dann den Kundentypennamen
	 */
	public static String[] getClientTypesList(final String[] list) {
		final List<String> temp=new ArrayList<>(Arrays.asList(list));
		temp.add(0,Language.tr("Surface.PopupMenu.BreakPoint.Option.ClientType.All"));
		return temp.toArray(String[]::new);
	}

	/**
	 * Liste der Kundentypen f�r die Combobox f�r die Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param runModel	Laufzeitmodell
	 * @return	Liste bestehen aus "Alle" als erstem Eintrag und dann den Kundentypennamen
	 */
	public static String[] getClientTypesList(final RunModel runModel) {
		return getClientTypesList(runModel.clientTypes);
	}

	/**
	 * Liste der Kundentypen f�r die Combobox f�r die Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param editModel	Editor-Modell
	 * @return	Liste bestehen aus "Alle" als erstem Eintrag und dann den Kundentypennamen
	 */
	public static String[] getClientTypesList(final EditModel editModel) {
		return getClientTypesList(editModel.surface.getClientTypes().toArray(String[]::new));
	}

	/**
	 * Liste der Icons Kundentypen f�r die Combobox f�r die Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeitmodell
	 * @return	Icons-Liste bestehen aus "Alle" als erstem Eintrag und dann den Kundentypennamen
	 */
	public static Icon[] getClientTypesListIcons(final EditModel editModel, final RunModel runModel) {
		final String[] clientTypes=runModel.clientTypes;
		final List<Icon> icons=new ArrayList<>(Arrays.asList(IconListCellRenderer.buildClientTypeIcons(clientTypes,editModel)));

		icons.add(0,Images.MODELPROPERTIES_CLIENTS_GROUPS.getIcon());

		return icons.toArray(Icon[]::new);
	}

	/**
	 * Liste der Icons Kundentypen f�r die Combobox f�r die Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param editModel	Editor-Modell
	 * @return	Icons-Liste bestehen aus "Alle" als erstem Eintrag und dann den Kundentypennamen
	 */
	public static Icon[] getClientTypesListIcons(final EditModel editModel) {
		final String[] clientTypes=editModel.surface.getClientTypes().toArray(String[]::new);
		final List<Icon> icons=new ArrayList<>(Arrays.asList(IconListCellRenderer.buildClientTypeIcons(clientTypes,editModel)));

		icons.add(0,Images.MODELPROPERTIES_CLIENTS_GROUPS.getIcon());

		return icons.toArray(Icon[]::new);
	}

	/**
	 * Erzeugt ein Panel mit einer Combobox zur Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeitmodell
	 * @return	Array aus zwei Elementen: {@link JPanel} und {@link JComboBox}
	 */
	public static Object[] getClientTypesComboBox(final EditModel editModel, final RunModel runModel) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(Language.tr("Surface.PopupMenu.BreakPoint.Option.ClientType")+":");
		line.add(label);
		final JComboBox<?> comboBox=new JComboBox<>(getClientTypesList(runModel));
		line.add(comboBox);
		label.setLabelFor(comboBox);
		comboBox.setRenderer(new IconListCellRenderer(getClientTypesListIcons(editModel,runModel)));
		return new Object[] {line, comboBox};
	}

	/**
	 * Erzeugt ein Panel mit einer Combobox zur Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param editModel	Editor-Modell
	 * @return	Array aus zwei Elementen: {@link JPanel} und {@link JComboBox}
	 */
	public static Object[] getClientTypesComboBox(final EditModel editModel) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(Language.tr("Surface.PopupMenu.BreakPoint.Option.ClientType")+":");
		line.add(label);
		final JComboBox<?> comboBox=new JComboBox<>(getClientTypesList(editModel));
		line.add(comboBox);
		label.setLabelFor(comboBox);
		comboBox.setRenderer(new IconListCellRenderer(getClientTypesListIcons(editModel)));
		return new Object[] {line, comboBox};
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final String condition=editCondition.getText().trim();
		if (condition.isEmpty()) {
			editCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
		} else {
			final int error=ExpressionMultiEval.check(condition,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				editCondition.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.PopupMenu.BreakPoint.Condition.Error.Title"),String.format(Language.tr("Surface.PopupMenu.BreakPoint.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				editCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert nach dem Schlie�en des Dialogs den neuen Haltepunkt.
	 * @return	Neuer Haltepunkt (kann <code>null</code> sein, wenn kein Haltepunkt f�r die aktuelle Station aktiv sein soll)
	 */
	public ModelSurfaceAnimatorBase.BreakPoint getBreakPoint() {
		if (!checkBoxActive.isSelected()) return null;

		final String condition=editCondition.getText().trim();
		return new ModelSurfaceAnimatorBase.BreakPoint(stationID,comboBoxClientTypes.getSelectedIndex()-1,(condition.isEmpty())?null:condition,checkBoxAutoDelete.isSelected());
	}
}
