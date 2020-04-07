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

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionEval;
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
	private static final long serialVersionUID = 96327428990066609L;

	private final String[] clientTypes;
	private final String[] stations;
	private final String[] variables;

	private final JRadioButton optionClientType;
	private final JComboBox<String> selectClientType;
	private final JRadioButton optionExpression;
	private final JTextField editExpression;
	private final JComboBox<String> selectDestination;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param route	Bisheriges, zu bearbeitendes Routing-Ziel
	 * @param clientTypes	Liste mit allen Kundentypennamen (f�r Kundentyp-abh�ngiges Routing)
	 * @param stations	Liste mit m�glichen Zielstationen
	 * @param variables	Liste mit den globalen Variablen (zur Pr�fung der Ausdr�cke)
	 * @param model	Gesamtes Modell (f�r den Expression-Builder)
	 * @param surface	Haupt-Zeichenfl�che (f�r den Expression-Builder)
	 */
	public TransportRouteTableModelDialog(final Component owner, final Runnable help, final TransportTargetRecord route, final String[] clientTypes, final String[] stations, final String[] variables, final EditModel model, final ModelSurface surface) {
		super(owner,Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.Title"),false);
		this.stations=stations;
		this.variables=variables;
		this.clientTypes=clientTypes;

		JPanel panel;
		JLabel label;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(optionClientType=new JRadioButton(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByClientType")+":"));
		panel.add(selectClientType=new JComboBox<>(clientTypes));
		selectClientType.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildClientTypeIcons(clientTypes,model)));
		selectClientType.addActionListener((e)->{optionClientType.setSelected(true);});

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
				index=-1;
				for (int i=0;i<clientTypes.length;i++) if (clientTypes[i].equals(route.routingCondition)) {index=i; break;}
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

	private boolean checkData(final boolean showErrorMessage) {
		if (!optionExpression.isSelected()) {
			editExpression.setBackground(SystemColor.text);
			return true;
		}
		final int error=ExpressionEval.check(editExpression.getText(),variables);
		if (error>=0) {
			editExpression.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByExpression.Error.Title"),String.format(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets.Edit.Dialog.ByExpression.Error.Info"),editExpression.getText(),error+1));
			}
		} else {
			editExpression.setBackground(SystemColor.text);
		}
		return error<0;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so kann �ber diese Methode das neue Routing-Ziel abgefragt werden.
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
