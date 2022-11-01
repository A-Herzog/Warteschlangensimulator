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

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementAnimationTrafficLights}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationTrafficLights
 */
public class ModelElementAnimationTrafficLightsDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8546156095792170133L;

	/** Auswahlbox f�r die Anzahl der Lichter der Ampel (kann 2 oder 3 sein) */
	private JComboBox<String> numberOfLights;
	/** Eingabefeld f�r die Bedingung f�r das erste Licht */
	private JTextField expressionOne;
	/** Eingabefeld f�r die Bedingung f�r das zweite Licht */
	private JTextField expressionTwo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationTrafficLights}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementAnimationTrafficLightsDialog(final Component owner, final ModelElementAnimationTrafficLights element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationTrafficLights.Dialog.Title"),element,"ModelElementAnimationTrafficLights",readOnly);
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationTrafficLights;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationTrafficLights.Dialog.NumberOfLights")+":"));
		line.add(numberOfLights=new JComboBox<>(new String[]{
				Language.tr("Surface.AnimationTrafficLights.Dialog.NumberOfLights.2Lights"),
				Language.tr("Surface.AnimationTrafficLights.Dialog.NumberOfLights.3Lights")
		}));
		numberOfLights.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS,
				Images.MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS3
		}));
		numberOfLights.setEnabled(!readOnly);
		label.setLabelFor(numberOfLights);
		numberOfLights.addActionListener(e->{
			if (numberOfLights.getSelectedIndex()==0) {
				expressionTwo.setEnabled(false);
			} else {
				expressionTwo.setEnabled(true);
				expressionTwo.setEditable(!readOnly);
			}
			checkData(false);
		});

		data=getInputPanel(Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionFor.Red")+":","");
		content.add(line=(JPanel)data[0]);
		expressionOne=(JTextField)data[1];
		line.add(getExpressionEditButton(this,expressionOne,true,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		expressionOne.setEditable(!readOnly);
		expressionOne.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionFor.Yellow")+":","");
		content.add(line=(JPanel)data[0]);
		expressionTwo=(JTextField)data[1];
		line.add(getExpressionEditButton(this,expressionTwo,true,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		expressionTwo.setEditable(!readOnly);
		expressionTwo.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionFor.Green")+":",Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionFor.Green.Info"));
		content.add((JPanel)data[0]);
		((JTextField)data[1]).setEnabled(false);

		if (element instanceof ModelElementAnimationTrafficLights) {
			numberOfLights.setSelectedIndex(((ModelElementAnimationTrafficLights)element).getLightsCount()-2);
			expressionOne.setText(((ModelElementAnimationTrafficLights)element).getExpressionOne());
			expressionTwo.setText(((ModelElementAnimationTrafficLights)element).getExpressionTwo());
		}

		checkData(false);

		return content;
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		int error=ExpressionMultiEval.check(expressionOne.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
		if (error>=0) {
			ok=false;
			expressionOne.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionError.Title"),String.format(Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionError.InfoRed"),expressionOne.getText(),error+1));
				return false;
			}
		} else {
			expressionOne.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (numberOfLights.getSelectedIndex()==1) {
			/* rot, gelb, gr�n */
			error=ExpressionMultiEval.check(expressionTwo.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				ok=false;
				expressionTwo.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionError.Title"),String.format(Language.tr("Surface.AnimationTrafficLights.Dialog.ConditionError.InfoYellow"),expressionTwo.getText(),error+1));
					return false;
				}
			} else {
				expressionTwo.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			/* rot, gr�n */
			expressionTwo.setBackground(SystemColor.control);
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationTrafficLights) {
			((ModelElementAnimationTrafficLights)element).setLightsCount(numberOfLights.getSelectedIndex()+2);
			((ModelElementAnimationTrafficLights)element).setExpressionOne(expressionOne.getText());
			((ModelElementAnimationTrafficLights)element).setExpressionTwo(expressionTwo.getText());
		}
	}
}
