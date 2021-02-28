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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Zeigt einen Dialog zum Bearbeiten eines Animations-Haltepunktes für eine Station an.
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
	 * Editor-Modell (für Expression-Prüfung)
	 */
	private final EditModel model;

	/**
	 * Soll an der angegebenen Station ein Haltepunkt aktiv sein?
	 */
	private final JCheckBox checkBoxActive;

	/**
	 * Kundentyp (kann "alle" sein) bei dessen Ankunft der Haltepunkt wirksam werden soll
	 */
	private final JComboBox<String> comboBoxClientTypes;

	/**
	 * Bedingung, die für die Auslösung des Haltepunktes erfüllt sein muss
	 */
	private final JTextField editCondition;

	/**
	 * Soll sich der Haltepunkt nach einmaliger Aktivierung automatisch löschen?
	 */
	private final JCheckBox checkBoxAutoDelete;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell (für den Expression-Builder / Expression-Prüfung)
	 * @param simData	Simulationsdatenobjekt (um die Liste der Kundentypen auszulesen)
	 * @param stationID	ID der Station auf die sich der Haltepunkt beziehen soll
	 * @param breakPoint	Bisheriger Haltepunkt für diese Station (kann <code>null</code> sein)
	 */
	public ModelBreakPointDialog(final Component owner, final EditModel model, final SimulationData simData, final int stationID, final ModelSurfaceAnimatorBase.BreakPoint breakPoint) {
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
		JLabel label;

		/* Dialog aufbauen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkBoxActive=new JCheckBox("<html><body><b>"+Language.tr("Surface.PopupMenu.BreakPoint.Option.Active")+"</b></body></html>",breakPoint!=null));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.PopupMenu.BreakPoint.Option.ClientType")+":"));
		line.add(comboBoxClientTypes=new JComboBox<>(getClientTypesList(simData.runModel.clientTypes)));
		label.setLabelFor(comboBoxClientTypes);
		comboBoxClientTypes.setSelectedIndex((breakPoint==null)?0:(breakPoint.clientType+1));
		comboBoxClientTypes.addActionListener(e->checkBoxActive.setSelected(true));

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.PopupMenu.BreakPoint.Condition")+":",(breakPoint==null)?"":breakPoint.condition);
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
	 * Liste die Liste für die Kundentypauswahl (Kundentypen und vorab "Alle")
	 * @param list	Liste der Kundentypennamen
	 * @return	Liste bestehen aus "Alle" als erstem Eintrag und dann den Kundentypennamen
	 */
	private static String[] getClientTypesList(final String[] list) {
		final List<String> temp=new ArrayList<>(Arrays.asList(list));
		temp.add(0,Language.tr("Surface.PopupMenu.BreakPoint.Option.ClientType.All"));
		return temp.toArray(new String[0]);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final String condition=editCondition.getText().trim();
		if (condition.isEmpty()) {
			editCondition.setBackground(SystemColor.text);
		} else {
			final int error=ExpressionMultiEval.check(condition,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (error>=0) {
				editCondition.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.PopupMenu.BreakPoint.Condition.Error.Title"),String.format(Language.tr("Surface.PopupMenu.BreakPoint.Condition.Error.Info"),condition,error+1));
					return false;
				}
				ok=false;
			} else {
				editCondition.setBackground(SystemColor.text);
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert nach dem Schließen des Dialogs den neuen Haltepunkt.
	 * @return	Neuer Haltepunkt (kann <code>null</code> sein, wenn kein Haltepunkt für die aktuelle Station aktiv sein soll)
	 */
	public ModelSurfaceAnimatorBase.BreakPoint getBreakPoint() {
		if (!checkBoxActive.isSelected()) return null;

		final String condition=editCondition.getText().trim();
		return new ModelSurfaceAnimatorBase.BreakPoint(stationID,comboBoxClientTypes.getSelectedIndex()-1,(condition.isEmpty())?null:condition,checkBoxAutoDelete.isSelected());
	}
}
