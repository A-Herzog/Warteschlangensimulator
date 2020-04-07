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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelLoadDataRecordDialog;
import ui.modeleditor.ModelResource;
import ui.optimizer.OptimizerSetup;

/**
 * Dieses Panel enthält die konkreten Eingabefelder für einen
 * {@link ParameterCompareSetupValueInputDialog} oder einen
 * {@link ModelLoadDataRecordDialog} Dialog.
 * @author Alexander Herzog
 * @see ParameterCompareSetupValueInputDialog
 * @see ModelLoadDataRecordDialog
 */
public class ParameterCompareSetupValueInputPanel extends JPanel {
	private static final long serialVersionUID=6378925927076313698L;

	private final ParameterCompareSetupValueInput input;
	private final EditModel model;

	private final JTextField nameEdit;
	private final JComboBox<String> modeCombo;
	private final JPanel cardPanel;
	private final CardLayout cardLayout;

	private final JComboBox<String> resCombo;
	private final JComboBox<String> varCombo;
	private final JTextField xmlTagEdit;
	private final JButton xmlTagButton;
	private final JComboBox<String> xmlMode;

	private final JLabel infoLabel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param input	Eingabeparameter-Datensatz
	 * @param model	Editor-Modell, welches die Basis für die Parameterstudie darstellt
	 * @param help	Hilfe-Runnable
	 * @param hasName	Soll ein Name-Eingabefeld angezeigt werden?
	 */
	public ParameterCompareSetupValueInputPanel(final Window owner, final ParameterCompareSetupValueInput input, final EditModel model, final Runnable help, final boolean hasName) {
		super();
		this.input=input;
		this.model=model;

		setLayout(new BorderLayout());

		JPanel sub, line;
		JLabel label;

		/* Oben */

		add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		if (hasName) {
			final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Settings.Input.Name")+":",input.getName());
			sub.add((JPanel)data[0]);
			nameEdit=(JTextField)data[1];
			nameEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {updateInfo();}
				@Override public void keyReleased(KeyEvent e) {updateInfo();}
				@Override public void keyPressed(KeyEvent e) {updateInfo();}
			});
		} else {
			nameEdit=null;
		}

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("ParameterCompare.Settings.Input.Mode.Label")+":"));
		line.add(modeCombo=new JComboBox<>(new String[]{
				Language.tr("ParameterCompare.Settings.Input.Mode.Resource"),
				Language.tr("ParameterCompare.Settings.Input.Mode.GlobalVariable"),
				Language.tr("ParameterCompare.Settings.Input.Mode.XML")
		}));
		modeCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.PARAMETERSERIES_INPUT_MODE_RESOURCE,
				Images.PARAMETERSERIES_INPUT_MODE_VARIABLE,
				Images.PARAMETERSERIES_INPUT_MODE_XML
		}));
		label.setLabelFor(modeCombo);

		/* Mitte */

		add(cardPanel=new JPanel(),BorderLayout.CENTER);
		cardPanel.setLayout(cardLayout=new CardLayout());

		cardPanel.add(sub=new JPanel(),"0");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		resCombo=getComboBox(sub,Language.tr("ParameterCompare.Settings.Input.Mode.Resource.Label"),OptimizerSetup.getResourceNames(model));
		resCombo.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildResourceTypeIcons(OptimizerSetup.getResourceNames(model),model)));
		if (resCombo.getItemCount()>0) resCombo.setSelectedIndex(0);
		resCombo.addActionListener(e->updateInfo());

		cardPanel.add(sub=new JPanel(),"1");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		varCombo=getComboBox(sub,Language.tr("ParameterCompare.Settings.Input.Mode.GlobalVariable.Label"),OptimizerSetup.getGlobalVariables(model));
		if (varCombo.getItemCount()>0) varCombo.setSelectedIndex(0);
		varCombo.addActionListener(e->updateInfo());

		cardPanel.add(sub=new JPanel(),"2");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		line.add(new JLabel(Language.tr("ParameterCompare.Settings.Input.Mode.XML.Label")+": "),BorderLayout.WEST);
		line.add(xmlTagEdit=new JTextField(),BorderLayout.CENTER);
		xmlTagEdit.setEditable(false);
		line.add(xmlTagButton=new JButton(Language.tr("ParameterCompare.Settings.Input.Mode.XML.Button")),BorderLayout.EAST);
		xmlTagButton.setToolTipText(Language.tr("ParameterCompare.Settings.Input.Mode.XML.Button.Hint"));
		xmlTagButton.setIcon(Images.PARAMETERSERIES_SELECT_XML.getIcon());
		xmlTagButton.addActionListener(e->{
			final String xml=ParameterCompareTools.selectXML(owner,model.saveToXMLDocument(),help);
			if (xml==null) return;
			xmlTagEdit.setText(xml);
			updateInfo();
		});
		xmlMode=getComboBox(sub,Language.tr("ParameterCompare.Settings.Input.Mode.XML.Mode"),ModelChanger.XML_ELEMENT_MODES);
		xmlMode.setSelectedIndex(0);
		xmlMode.addActionListener(e->updateInfo());

		/* Unten */

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(infoLabel=new JLabel());

		/* Daten laden */

		modeCombo.addActionListener(e->{cardLayout.show(cardPanel,""+modeCombo.getSelectedIndex()); updateInfo();});
		int index;
		switch (input.getMode()) {
		case MODE_RESOURCE:
			modeCombo.setSelectedIndex(0);
			index=-1;
			for (int i=0;i<resCombo.getItemCount();i++) if (resCombo.getItemAt(i).equals(input.getTag())) {index=i; break;}
			if (index>=0) resCombo.setSelectedIndex(index);
			break;
		case MODE_VARIABLE:
			modeCombo.setSelectedIndex(1);
			index=-1;
			for (int i=0;i<varCombo.getItemCount();i++) if (varCombo.getItemAt(i).equals(input.getTag())) {index=i; break;}
			if (index>=0) varCombo.setSelectedIndex(index);
			break;
		case MODE_XML:
			modeCombo.setSelectedIndex(2);
			xmlTagEdit.setText(input.getTag());
			xmlMode.setSelectedIndex(Math.max(0,Math.min(xmlMode.getItemCount()-1,input.getXMLMode())));
			break;
		}
		updateInfo();
	}

	private JComboBox<String> getComboBox(final JPanel sub, final String label, final String[] content) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		sub.add(line);

		final JLabel comboLabel;
		final JComboBox<String> combo;

		if (label!=null) line.add(comboLabel=new JLabel(label+":")); else comboLabel=null;
		line.add(combo=new JComboBox<>(content));
		if (comboLabel!=null) comboLabel.setLabelFor(combo);

		return combo;
	}

	private void updateInfo() {
		final String head="<html><body><b>";
		final String foot="</b></body></html>";

		if (!checkData(false)) {
			infoLabel.setText(head+Language.tr("ParameterCompare.Settings.Input.Info.Error")+foot);
			return;
		}

		String info="";
		switch (modeCombo.getSelectedIndex()) {
		case 0:
			final ModelResource resource=model.resources.get((String)resCombo.getSelectedItem());
			if (resource!=null) info=String.format(Language.tr("ParameterCompare.Settings.Input.Mode.Resource.Info"),resource.getCount());
			break;
		case 1:
			final int index=model.globalVariablesNames.indexOf(varCombo.getSelectedItem());
			if (index>=0) info=String.format(Language.tr("ParameterCompare.Settings.Input.Mode.GlobalVariable.Info"),model.globalVariablesExpressions.get(index));
			break;
		case 2:
			final String xmlKey=xmlTagEdit.getText();
			final String value=ModelChanger.getValue(model,xmlKey,xmlMode.getSelectedIndex());
			if (value==null) {
				info=Language.tr("ParameterCompare.Settings.Input.Mode.XML.InfoError");
			} else {
				info=String.format(Language.tr("ParameterCompare.Settings.Input.Mode.XML.Info"),value);
			}
			break;
		}

		infoLabel.setText(head+info+foot);
	}

	/**
	 * Prüft die Eingabedaten und zeigt ggf. eine Fehlermeldung an.
	 * @param showErrorMessage	Soll eine Meldung im Fehlerfall ausgegeben werden?
	 * @return	Sind die Daten gültig?
	 */
	public boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (nameEdit!=null) {
			if (nameEdit.getText().trim().isEmpty()) {
				nameEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Input.Name.ErrorTitle"),Language.tr("ParameterCompare.Settings.Input.Name.ErrorInfo"));
					return false;
				}
			} else {
				nameEdit.setBackground(SystemColor.text);
			}
		}

		switch (modeCombo.getSelectedIndex()) {
		case 0:
			if (resCombo.getSelectedIndex()<0) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Input.Mode.Resource.ErrorTitle"),Language.tr("ParameterCompare.Settings.Input.Mode.Resource.ErrorInfo"));
					return false;
				}
			}
			break;
		case 1:
			if (varCombo.getSelectedIndex()<0) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Input.Mode.GlobalVariable.ErrorTitle"),Language.tr("ParameterCompare.Settings.Input.Mode.GlobalVariable.ErrorInfo"));
					return false;
				}
			}
			break;
		case 2:
			if (xmlTagEdit.getText().isEmpty()) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Input.Mode.XML.ErrorTitle"),Language.tr("ParameterCompare.Settings.Input.Mode.XML.ErrorInfo"));
					return false;
				}
			}
			break;
		}

		return ok;
	}

	/**
	 * Schreibt die Daten in das im Konstruktor angegebene
	 * {@link ParameterCompareSetupValueInput} Objekt zurück.
	 */
	public void storeData() {
		if (nameEdit!=null) {
			input.setName(nameEdit.getText().trim());
		}
		switch (modeCombo.getSelectedIndex()) {
		case 0:
			input.setMode(ModelChanger.Mode.MODE_RESOURCE);
			input.setTag((String)resCombo.getSelectedItem());
			break;
		case 1:
			input.setMode(ModelChanger.Mode.MODE_VARIABLE);
			input.setTag((String)varCombo.getSelectedItem());
			break;
		case 2:
			input.setMode(ModelChanger.Mode.MODE_XML);
			input.setTag(xmlTagEdit.getText());
			input.setXMLMode(xmlMode.getSelectedIndex());
			break;
		}
	}
}
