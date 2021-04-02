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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.elements.RunElementTeleportSource;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementHoldMulti}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementHoldMulti
 */
public class ModelElementHoldMultiDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4497874184675168386L;

	/**
	 * Eingabefelder für die Bedingungen
	 */
	private List<JTextField> conditions;

	/**
	 * Option: Bedingung zusätzlich zeitgesteuert prüfen
	 */
	private JCheckBox useTimedChecks;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementHoldMulti}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementHoldMultiDialog(final Component owner, final ModelElementHoldMulti element, final boolean readOnly) {
		super(owner,Language.tr("Surface.HoldMulti.Dialog.Title"),element,"ModelElementHoldMulti",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Liefert den Namen des Ziels, das über eine auslaufende Kante erreicht werden kann
	 * @param edge	Auslaufende Kante
	 * @return	Name des Ziels
	 */
	private String getDestination(ModelElementEdge edge) {
		while (true) {
			if (edge==null) return null;
			final ModelElement edgeEnd=edge.getConnectionEnd();
			if (edgeEnd==null) return null;

			if (edgeEnd instanceof ModelElementVertex) {
				edge=((ModelElementVertex)edgeEnd).getEdgeOut();
				continue;
			}

			if (edgeEnd instanceof ModelElementTeleportSource) {
				final ModelElementTeleportDestination destination=RunElementTeleportSource.getDestination(edgeEnd.getModel(),((ModelElementTeleportSource)edgeEnd).getDestination());
				if (destination==null) return null;
				edge=destination.getEdgeOut();
				continue;
			}

			String name;
			if (edgeEnd instanceof ModelElementBox) {
				name=((ModelElementBox)edgeEnd).getTypeName();
			} else {
				name=edgeEnd.getName();
			}

			return name+" (id="+edgeEnd.getId()+")";
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationHoldMulti;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementHoldMulti holdMulti=(ModelElementHoldMulti)element;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final ModelElementEdge[] edges=holdMulti.getEdgesOut();

		conditions=new ArrayList<>();
		for (int i=0;i<edges.length;i++) {
			String name=Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1);
			String destination=getDestination(edges[i]);
			if (destination!=null) name+=" zu "+destination;

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			final JLabel label=new JLabel(name); labelPanel.add(label);

			final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.HoldMulti.Dialog.OutgoingEdge.Condition")+":","");
			final JPanel inputPanel=(JPanel)data[0];
			option.add(inputPanel,BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];
			inputPanel.add(getExpressionEditButton(this,input,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

			input.setEditable(!readOnly);
			String condition=holdMulti.getConditions().get(edges[i].getId());
			if (condition==null) condition="";
			input.setText(condition);
			input.addKeyListener(new KeyListener(){
				@Override public void keyTyped(KeyEvent e) {getConditions(false);}
				@Override public void keyPressed(KeyEvent e) {getConditions(false);}
				@Override public void keyReleased(KeyEvent e) {getConditions(false);}
			});

			conditions.add(input);
		}

		getConditions(false);

		final JPanel line;
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useTimedChecks=new JCheckBox(Language.tr("Surface.HoldMulti.Dialog.TimeBasedCheck"),holdMulti.isUseTimedChecks()));
		useTimedChecks.setEnabled(!readOnly);

		return content;
	}

	/**
	 * Prüft die in {@link #conditions} angegebenen Bedingungen
	 * @param showErrorDialog	Im Fehlerfall eine Meldung ausgeben?
	 * @return	Liefert im Erfolgsfall die Bedingungen, sonst <code>null</code>
	 */
	private List<String> getConditions(final boolean showErrorDialog) {
		List<String> values=new ArrayList<>();

		for (int i=0;i<conditions.size();i++) {
			final JTextField field=conditions.get(i);
			final String condition=field.getText();
			final int error=ExpressionMultiEval.check(condition,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) field.setBackground(Color.red); else field.setBackground(NumberTools.getTextFieldDefaultBackground());

			if (error>=0) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Surface.HoldMulti.Dialog.OutgoingEdge.Condition.Error.Title"),String.format(Language.tr("Surface.HoldMulti.Dialog.OutgoingEdge.Condition.Error.Info"),i+1,error+1));
					return null;
				}
				values=null;
			}
			if (values!=null) values.add(condition);
		}

		return values;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return getConditions(true)!=null;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementHoldMulti) {
			final ModelElementHoldMulti holdMulti=(ModelElementHoldMulti)element;
			final ModelElementEdge[] edges=holdMulti.getEdgesOut();

			final Map<Integer,String> conditionsMap=holdMulti.getConditions();
			conditionsMap.clear();
			final List<String> c=getConditions(false);
			if (c!=null) for (int i=0;i<edges.length;i++) conditionsMap.put(edges[i].getId(),c.get(i));

			holdMulti.setUseTimedChecks(useTimedChecks.isSelected());
		}
	}
}
