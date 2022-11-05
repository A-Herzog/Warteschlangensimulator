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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dieser Dialog erlaubt das Bearbeiten eines einzelnen Datensatzes
 * aus einer {@link TransportRouteTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see TransportRouteTableModel
 */
public class TransportRouteTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 96327428990066609L;

	/**
	 * Liste mit allen Kundentypennamen (für Kundentyp-abhängiges Routing)
	 */
	private final String[] clientTypes;

	/**
	 * Liste mit möglichen Zielstationen
	 */
	private final String[] stations;

	/**
	 * Namen der modellweiten Variablen
	 */
	private final String[] variables;

	/** Option: Transport (gemäß dieser Regel) auslösen bei Kundentyp */
	private final JRadioButton optionClientType;
	/** Auswahlbox für den Kundentyp im Fall {@link #optionClientType} */
	private final JComboBox<String> selectClientType;
	/** Option: Transport (gemäß dieser Regel) bei erfüllter Bedingung auslösen */
	private final JRadioButton optionExpression;
	/** Eingabefeld für die Bedignung im Fall {@link #optionExpression} */
	private final JTextField editExpression;
	/** Auswahlbox für das Transportziel dieser Regel */
	private final JComboBox<String> selectDestination;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param route	Bisheriges, zu bearbeitendes Routing-Ziel
	 * @param clientTypes	Liste mit allen Kundentypennamen (für Kundentyp-abhängiges Routing)
	 * @param stations	Liste mit möglichen Zielstationen
	 * @param variables	Liste mit den globalen Variablen (zur Prüfung der Ausdrücke)
	 * @param model	Gesamtes Modell (für den Expression-Builder)
	 * @param surface	Haupt-Zeichenfläche (für den Expression-Builder)
	 */
	public TransportRouteTableModelDialog(final Component owner, final Runnable help, final TransportTargetRecord route, final String[] clientTypes, final String[] stations, final String[] variables, final EditModel model, final ModelSurface surface) {
		super(owner,Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.Title"),false);
		this.stations=stations;
		this.variables=variables;
		this.clientTypes=clientTypes;

		final Map<String,Integer> clientTypesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (int i=0;i<clientTypes.length;i++) clientTypesMap.put(clientTypes[i],i);

		JPanel panel;
		JLabel label;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(optionClientType=new JRadioButton(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByClientType")+":"));
		panel.add(selectClientType=new JComboBox<>(clientTypes));
		selectClientType.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildClientTypeIcons(clientTypes,model)));
		selectClientType.addActionListener(e->optionClientType.setSelected(true));

		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(optionExpression=new JRadioButton(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByExpression")+":"));
		panel.add(editExpression=new JTextField(30));
		panel.add(ModelElementBaseDialog.getExpressionEditButton(this,editExpression,true,true,model,surface));
		editExpression.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false); optionExpression.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {checkData(false); optionExpression.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {checkData(false); optionExpression.setSelected(true);}
		});

		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(label=new JLabel(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.DestinationStation")+":"));
		panel.add(selectDestination=new JComboBox<>(stations));
		label.setLabelFor(selectDestination);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionClientType);
		buttonGroup.add(optionExpression);

		if (route==null) {
			optionClientType.setSelected(clientTypes.length>0);
			if (clientTypes.length>0) selectClientType.setSelectedIndex(0);
			if (stations.length>0) selectDestination.setSelectedIndex(0);
		} else {
			int index;
			if (route.routingMode==TransportTargetRecord.RoutingMode.ROUTING_MODE_CLIENT_TYPE) {
				optionClientType.setSelected(true);
				index=clientTypesMap.getOrDefault(route.routingCondition,-1);
				if (index>=0) selectClientType.setSelectedIndex(index);
			}
			if (route.routingMode==TransportTargetRecord.RoutingMode.ROUTING_MODE_EXPRESSION) {
				optionExpression.setSelected(true);
				editExpression.setText(route.routingCondition);
			}
			index=-1;
			for (int i=0;i<stations.length;i++) if (stations[i].equals(route.station)) {index=i; break;}
			if (index>=0) selectDestination.setSelectedIndex(index);
		}

		checkData(false);
		pack();
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (!optionExpression.isSelected()) {
			editExpression.setBackground(NumberTools.getTextFieldDefaultBackground());
			return true;
		}
		final int error=ExpressionMultiEval.check(editExpression.getText(),variables);
		if (error>=0) {
			editExpression.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByExpression.Error.Title"),String.format(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByExpression.Error.Info"),editExpression.getText(),error+1));
			}
		} else {
			editExpression.setBackground(NumberTools.getTextFieldDefaultBackground());
		}
		return error<0;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so kann über diese Methode das neue Routing-Ziel abgefragt werden.
	 * @return	Neues Routing-Ziel
	 */
	public TransportTargetRecord getRoute() {
		final String station=(selectDestination.getSelectedIndex()>=0)?stations[selectDestination.getSelectedIndex()]:"";

		if (optionClientType.isSelected()) {
			final String clientType=(selectClientType.getSelectedIndex()>=0)?clientTypes[selectClientType.getSelectedIndex()]:"";
			return TransportTargetRecord.getByClientType(clientType,station);
		}
		if (optionExpression.isSelected()) {
			return TransportTargetRecord.getByExpression(editExpression.getText(),station);

		}
		return null;
	}
}
