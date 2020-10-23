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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
 * Dieser Dialog ermöglicht das Bearbeiten eines der Datensätze
 * in einer {@link ModelElementAnalogAssignTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see ModelElementAnalogAssignTableModel
 * @see ModelElementAnalogAssign
 */
public class ModelElementAnalogAssignTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -612344610045167231L;

	private String[] idNames;
	private boolean[] idAllowRates;
	private int[] ids;

	/**
	 * Namen der modellweiten Variablen
	 */
	private final String[] variables;

	private final JComboBox<String> comboID;
	private final JComboBox<String> comboMode;
	private final JTextField edit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes element
	 * @param id	ID der Station auf die die Zuweisung wirken soll
	 * @param mode	Bisher gewählte Zuweisungsart (Wert oder Rate)
	 * @param expression	Bisheriger Ausdruck
	 * @param surface	Haupt-Zeichenfläche (für Expression-Builder)
	 * @param model	Vollständiges Modell (für Expression-Builder)
	 * @param help	Hilfe-Callback
	 * @see ModelElementAnalogAssign.ChangeMode
	 */
	public ModelElementAnalogAssignTableModelDialog(final Component owner, final int id, final ModelElementAnalogAssign.ChangeMode mode, final String expression, final ModelSurface surface, final EditModel model, final Runnable help) {
		super(owner,Language.tr("Surface.AnalogAssign.Dialog.Edit.DialogTitle"));

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.setBorder(BorderFactory.createEmptyBorder(0,5,10,5));
		JPanel line;
		JLabel label;

		buildIDNames(surface.getParentSurface()==null?surface:surface.getParentSurface());
		variables=model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true);

		/* IDs */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnalogAssign.Dialog.Station")+":"));
		line.add(comboID=new JComboBox<>(idNames));
		label.setLabelFor(comboID);
		int index=-1;
		for (int i=0;i<ids.length;i++) if (ids[i]==id) {index=i; break;}
		if (index<0 && ids.length>0) index=0;
		if (index>=0) comboID.setSelectedIndex(index);
		comboID.addActionListener(e->selectedIDChanged());

		/* Modi */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnalogAssign.Dialog.Mode")+":"));
		line.add(comboMode=new JComboBox<>(new String[] {
				Language.tr("Surface.AnalogAssign.Dialog.ModeValue"),
				Language.tr("Surface.AnalogAssign.Dialog.ModeRate")
		}));
		label.setLabelFor(comboMode);
		switch (mode) {
		case CHANGE_MODE_VALUE: comboMode.setSelectedIndex(0); break;
		case CHANGE_MODE_RATE: comboMode.setSelectedIndex(1); break;
		}
		selectedIDChanged();

		/* Ausdruck */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnalogAssign.Dialog.Expression")+":",expression);
		content.add(line=(JPanel)data[0]);
		edit=(JTextField)data[1];
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,edit,false,true,model,model.surface),BorderLayout.EAST);
		edit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(this.owner);
		setResizable(true);
	}

	private void selectedIDChanged() {
		if (comboID.getSelectedIndex()<0) return;
		if (idAllowRates[comboID.getSelectedIndex()]) {
			comboMode.setEnabled(true);
		} else {
			comboMode.setEnabled(false);
			comboMode.setSelectedIndex(0);
		}
	}

	private void buildIDNames(final ModelSurface mainSurface) {
		final List<String> names=new ArrayList<>();
		final List<Boolean> rates=new ArrayList<>();
		final List<Integer> ids=new ArrayList<>();

		for (ModelElement element1: mainSurface.getElements()) {
			if (element1 instanceof ModelElementAnalogValue) {names.add(element1.getName()); rates.add(true); ids.add(element1.getId());}
			if (element1 instanceof ModelElementTank) {names.add(element1.getName()); rates.add(false); ids.add(element1.getId());}
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementAnalogValue) {names.add(element2.getName()); rates.add(true); ids.add(element2.getId());}
				if (element2 instanceof ModelElementTank) {names.add(element2.getName()); rates.add(false); ids.add(element2.getId());}
			}
		}

		for (int i=0;i<names.size();i++) {
			if (names.get(i).isEmpty()) {
				names.set(i,String.format(Language.tr("Surface.AnalogAssign.Dialog.ID.NoName"),ids.get(i)));
			} else {
				names.set(i,String.format(Language.tr("Surface.AnalogAssign.Dialog.ID.Name"),ids.get(i),names.get(i)));
			}
		}

		idNames=names.toArray(new String[0]);
		idAllowRates=new boolean[rates.size()]; for (int i=0;i<rates.size();i++) idAllowRates[i]=rates.get(i);
		this.ids=ids.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final int error=ExpressionCalc.check(edit.getText(),variables);
		if (error<0) {
			edit.setBackground(SystemColor.text);
		} else {
			edit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnalogAssign.Dialog.Expression.ErrorTitle"),String.format(Language.tr("Surface.AnalogAssign.Dialog.Expression.ErrorInfo"),edit.getText(),error+1));
				return false;
			}
		}

		if (comboID.getSelectedIndex()<0) ok=false;

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die ID der Station auf die die Zuweisung wirken soll.
	 * @return	ID der Station auf die die Zuweisung wirken soll
	 */
	public int getID() {
		if (comboID.getSelectedIndex()<0) return -1;
		return ids[comboID.getSelectedIndex()];
	}

	/**
	 * Liefert die eingestellte Zuweisungsart.
	 * @return	Zuweisungsart (Wert oder Rate)
	 * @see ModelElementAnalogAssign.ChangeMode
	 */
	public ModelElementAnalogAssign.ChangeMode getMode() {
		if (comboMode.getSelectedIndex()==0) {
			return ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_VALUE;
		} else {
			return ModelElementAnalogAssign.ChangeMode.CHANGE_MODE_RATE;
		}
	}

	/**
	 * Liefert den neu eingestellten Ausdruck.
	 * @return	Neuer Ausdruck
	 */
	public String getExpression() {
		return edit.getText();
	}
}
