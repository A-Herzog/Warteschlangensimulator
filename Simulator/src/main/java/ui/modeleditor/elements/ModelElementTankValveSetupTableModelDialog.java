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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieser Dialog ermöglicht das Bearbeiten eines einzelnen Datensatzes
 * aus einer {@link ModelElementTankValveSetupTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see ModelElementTankValveSetupTableModel
 */
public class ModelElementTankValveSetupTableModelDialog extends BaseDialog {
	private static final long serialVersionUID = -3934000835288739956L;

	private final ModelSurface mainSurface;
	private final EditModel model;
	private final ModelElementTankValveSetup.ValveSetup valveSetup;
	private final List<Integer> tankIds;
	private final int[] tankValvesCount;
	private int lastValveNr;
	private final JComboBox<String> tankCombo;
	private final JComboBox<String> valveCombo;
	private final JTextField maxFlowEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param valveSetup	Zu bearbeitender Ventil-Konfigurationsdatensatz
	 * @param mainSurface	Haupt-Zeichenfläche (für den Expression-Builder)
	 * @param model	Gesamtes Editor-Modell (für den Expression-Builder)
	 * @param helpRunnable	Hilfe-Callback
	 */
	public ModelElementTankValveSetupTableModelDialog(final Component owner, final ModelElementTankValveSetup.ValveSetup valveSetup, final ModelSurface mainSurface, final EditModel model, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.TankValveSetup.Table.Edit"));
		this.mainSurface=mainSurface;
		this.model=model;
		this.valveSetup=valveSetup;

		final List<ModelElementTank> tanks=listAllTanks(mainSurface);
		final String[] tankNames=tanks.stream().map(tank->String.format(Language.tr("Surface.TankValveSetup.Table.Edit.TankName"),tank.getName(),tank.getId())).toArray(String[]::new);
		tankIds=tanks.stream().map(tank->Integer.valueOf(tank.getId())).collect(Collectors.toList());
		tankValvesCount=tanks.stream().mapToInt(tank->tank.getValves().size()).toArray();

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TankValveSetup.Table.Edit.Tank")+":"));
		line.add(tankCombo=new JComboBox<>(tankNames));
		label.setLabelFor(tankCombo);

		int index=tankIds.indexOf(valveSetup.tankId);
		if (index<0 && tankIds.size()>0) index=0;
		if (index>=0) tankCombo.setSelectedIndex(index);
		tankCombo.addActionListener(e->updateValveCombo());

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TankValveSetup.Table.Edit.Valve")+":"));
		line.add(valveCombo=new JComboBox<>());
		label.setLabelFor(valveCombo);
		lastValveNr=valveSetup.valveNr;

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.TankValveSetup.Table.Edit.MaxFlow")+":",valveSetup.maxFlow,10);
		content.add(line=(JPanel)data[0]);
		maxFlowEdit=(JTextField)data[1];
		maxFlowEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,maxFlowEdit,false,true,model,mainSurface));

		updateValveCombo();

		checkData(false);

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private List<ModelElementTank> listAllTanks(final ModelSurface mainSurface) {
		final List<ModelElementTank> list=new ArrayList<>();
		for (ModelElement element1: mainSurface.getElements()) {
			if (element1 instanceof ModelElementTank) list.add((ModelElementTank)element1);
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementTank) list.add((ModelElementTank)element2);
			}
		}
		return list;
	}

	private void updateValveCombo() {
		if (lastValveNr<0) lastValveNr=valveCombo.getSelectedIndex();

		final int tankIndex=tankCombo.getSelectedIndex();
		valveCombo.setEnabled(tankIndex>=0 && tankValvesCount[tankIndex]>0);
		if (tankIndex<0 || tankValvesCount[tankIndex]<1) return;

		final List<String> list=new ArrayList<>();
		for (int i=0;i<tankValvesCount[tankIndex];i++) list.add(Language.tr("Surface.TankValveSetup.Table.Edit.Valve")+" "+(i+1));
		valveCombo.setModel(new DefaultComboBoxModel<>(list.toArray(new String[0])));

		lastValveNr=Math.max(0,Math.min(tankValvesCount[tankIndex]-1,lastValveNr));
		valveCombo.setSelectedIndex(lastValveNr);

		lastValveNr=-1;
	}

	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (tankCombo.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankValveSetup.Table.Edit.Tank.ErrorTitle"),Language.tr("Surface.TankValveSetup.Table.Edit.Tank.ErrorInfo"));
				return false;
			}
		}

		if (valveCombo.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankValveSetup.Table.Edit.Valve.ErrorTitle"),Language.tr("Surface.TankValveSetup.Table.Edit.Valve.ErrorInfo"));
				return false;
			}
		}

		final String text=maxFlowEdit.getText();
		final int error=ExpressionCalc.check(text,mainSurface.getMainSurfaceVariableNames(model.getModelVariableNames(),true));
		if (error>=0) {
			maxFlowEdit.setBackground(Color.red);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Hold.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Hold.Dialog.Condition.Error.Info"),text,error+1));
				return false;
			}
		} else {
			maxFlowEdit.setBackground(SystemColor.text);
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		valveSetup.tankId=tankIds.get(tankCombo.getSelectedIndex());
		valveSetup.valveNr=valveCombo.getSelectedIndex();
		valveSetup.maxFlow=maxFlowEdit.getText();
	}
}