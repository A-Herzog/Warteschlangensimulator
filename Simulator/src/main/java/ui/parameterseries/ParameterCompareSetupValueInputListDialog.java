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

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.ModelChanger;
import ui.images.Images;

/**
 * Dieser Dialog ermöglicht das Einstellen der Eingabeparameter
 * für die Parameter-Vergleichs-Funktion
 * @author Alexander Herzog
 * @see ParameterComparePanel
 */
public class ParameterCompareSetupValueInputListDialog extends ParameterCompareSetupValueBaseListDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6451260447527475586L;

	/** Liste der Eingabeparameter-Einstellungen (Objekt, das dem Dialog übergeben wurde) */
	private final List<ParameterCompareSetupValueInput> inputOriginal;
	/** Liste der Eingabeparameter-Einstellungen (Arbeitskopie) */
	private final List<ParameterCompareSetupValueInput> input;

	/**
	 * Konstruktor der Klasse.<br>
	 * Macht den Dialog auch direkt sichtbar
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell, welches die Basis für die Parameterstudie darstellt
	 * @param input	Liste der Eingabeparameter-Einstellungen
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareSetupValueInputListDialog(final Component owner, final EditModel model, final List<ParameterCompareSetupValueInput> input, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Settings.Input.List.Title"),model,help);

		this.inputOriginal=input;
		this.input=new ArrayList<>();
		for (ParameterCompareSetupValueInput record: input) this.input.add(record.clone());

		initToolbar(
				Language.tr("ParameterCompare.Settings.Input.List.Add"),
				Language.tr("ParameterCompare.Settings.Input.List.Add.Hint"),
				Language.tr("ParameterCompare.Settings.Input.List.Edit"),
				Language.tr("ParameterCompare.Settings.Input.List.Edit.Hint"),
				Language.tr("ParameterCompare.Settings.Input.List.Delete"),
				Language.tr("ParameterCompare.Settings.Input.List.Delete.Hint"),
				Language.tr("ParameterCompare.Settings.Input.List.MoveUp"),
				Language.tr("ParameterCompare.Settings.Input.List.MoveUp.Hint"),
				Language.tr("ParameterCompare.Settings.Input.List.MoveDown"),
				Language.tr("ParameterCompare.Settings.Input.List.MoveDown.Hint"));

		start();
	}

	/**
	 * Liefert den Beschreibungstext für einen Input-Parameter
	 * @param record	Input-Parameter Datensatz
	 * @return	Beschreibungstext
	 */
	public static String getInputInfo(final ParameterCompareSetupValueInput record) {
		switch (record.getMode()) {
		case MODE_RESOURCE:
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoResource"),record.getTag());
		case MODE_VARIABLE:
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoVariable"),record.getTag());
		case MODE_MAP:
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoMap"),record.getTag());
		case MODE_XML:
			String type=ModelChanger.XML_ELEMENT_MODES[Math.max(0,Math.min(ModelChanger.XML_ELEMENT_MODES.length-1,record.getXMLMode()))];
			return String.format(Language.tr("ParameterCompare.Settings.Input.List.InfoXML"),record.getTag(),type);
		default:
			return "";
		}
	}

	@Override
	protected DefaultListModel<JLabel> getListModel() {
		final DefaultListModel<JLabel> listModel=new DefaultListModel<>();

		for (ParameterCompareSetupValueInput record: input) {
			final JLabel label=new JLabel();
			final StringBuilder sb=new StringBuilder();
			sb.append("<html><body>");
			sb.append(Language.tr("ParameterCompare.Table.Column.Input")+"<br>");
			sb.append("<b>"+record.getName()+"</b><br>");
			sb.append(getInputInfo(record));
			sb.append("</html></body>");
			label.setText(sb.toString());
			final Icon icon=ParameterCompareInputValuesTemplates.getIcon(record.getMode());
			if (icon!=null) label.setIcon(icon);
			listModel.addElement(label);
		}

		return listModel;
	}

	/**
	 * Zeigt einen Dialog zum Bearbeiten eines Eingabeparameters an.
	 * @param record	Eingabeparameters-Datensatz
	 * @return	Liefert <code>true</code>, wenn der Dialog mit "Ok" geschlossen wurde
	 */
	private boolean editInput(final ParameterCompareSetupValueInput record) {
		final ParameterCompareSetupValueInputDialog dialog=new ParameterCompareSetupValueInputDialog(owner,record,model,help);
		return dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK;
	}

	@Override
	protected void addAddModesToMenu(final JButton anchor, final JPopupMenu popupMenu) {
		super.addAddModesToMenu(anchor,popupMenu);

		addAddButton(popupMenu,Language.tr("ParameterCompare.Settings.List.AddVariable"),Language.tr("ParameterCompare.Settings.List.AddVariable.Hint"),Images.PARAMETERSERIES_INPUT_MODE_VARIABLE.getIcon(),1);

		/*
		Nicht nötig, da diese Option für alle möglichen Bedienergruppen direkt als Vorlage angeboten wird:
		addAddButton(popupMenu,Language.tr("ParameterCompare.Settings.List.AddResource"),Language.tr("ParameterCompare.Settings.List.AddResource.Hint"),Images.PARAMETERSERIES_INPUT_MODE_RESOURCE.getIcon(),2);
		 */
	}

	@Override
	protected void addTemplatesToMenu(final JButton anchor, final JPopupMenu popupMenu) {
		new ParameterCompareInputValuesTemplates(model,input->isParameterInUse(input)).addToMenu(popupMenu,input->addParameterFromTemplate(input));
	}

	@Override
	protected void commandAdd(final int nr) {
		final ParameterCompareSetupValueInput record=new ParameterCompareSetupValueInput();
		switch (nr) {
		case 0:
			record.setMode(ModelChanger.Mode.MODE_XML);
			break;
		case 1:
			record.setMode(ModelChanger.Mode.MODE_VARIABLE);
			break;
			/*
		case 2:
			record.setMode(ModelChanger.Mode.MODE_MAP);
			break;
		case 3:
			record.setMode(ModelChanger.Mode.MODE_RESOURCE);
			break;
			 */
		}
		record.setName(String.format(Language.tr("ParameterCompare.Settings.Input.Name.Default"),input.size()+1));
		if (!editInput(record)) return;
		input.add(record);

		updateList(Integer.MAX_VALUE);
	}

	@Override
	protected void commandEdit(final int index) {
		if (!editInput(input.get(index))) return;

		updateList(0);
	}

	@Override
	protected void commandDelete(final int index, final boolean shiftDown) {
		if (!shiftDown) {
			if (!MsgBox.confirm(
					this,
					Language.tr("ParameterCompare.Settings.Input.List.Delete.Confirm.Title"),
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Delete.Confirm.Info"),input.get(index).getName()),
					Language.tr("ParameterCompare.Settings.Input.List.Delete.Confirm.YesInfo"),
					Language.tr("ParameterCompare.Settings.Input.List.Delete.Confirm.NoInfo"))) return;
		}
		input.remove(index);
		updateList(-1);
	}

	@Override
	protected void commandSwap(final int index1, final int index2) {
		Collections.swap(input,index1,index2);
	}

	/**
	 * Prüft, ob ein bestimmter Parameter bereits in der Liste {@link #input} enthalten ist.
	 * @param input	Evtl. neuer Eingabeparameter
	 * @return	Liefert <code>true</code>, wenn der angegebene Eingabeparameter bereits in der Liste der Eingabeparameter enthalten ist
	 */
	private boolean isParameterInUse(final ParameterCompareSetupValueInput input) {
		for (ParameterCompareSetupValueInput test: this.input) {
			if (test.getMode()!=input.getMode()) continue;
			if (!test.getTag().equals(input.getTag())) continue;
			if (test.getMode()==ModelChanger.Mode.MODE_XML && (test.getXMLMode()!=input.getXMLMode())) continue;
			return true;
		}
		return false;
	}

	/**
	 * Fügt einen Eingabeparameter basierend auf einer Vorlage zur Liste der Eingabeparameter hinzu
	 * @param template	Eingabeparameter-Vorlage
	 * @see #input
	 */
	private void addParameterFromTemplate(final ParameterCompareSetupValueInput template) {
		input.add(template);
		updateList(Integer.MAX_VALUE);
	}

	@Override
	public void storeData() {
		inputOriginal.clear();
		for (ParameterCompareSetupValueInput record: input) inputOriginal.add(record.clone());
	}
}