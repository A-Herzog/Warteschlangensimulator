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

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationClock}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationClock
 */
public class ModelElementAnimationClockDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -4923048473167643207L;

	private JTextField editExpression;
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationClock}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationClockDialog(final Component owner, final ModelElementAnimationClock element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationClock.Dialog.Title"),element,"ModelElementAnimationClock",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationClock;
	}

	@Override
	protected JComponent getContentPanel() {
		JPanel line, cell;
		JLabel label;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		Object[] data=getInputPanel(Language.tr("Surface.AnimationClock.Dialog.Expression")+":","");
		content.add((JPanel)data[0]);
		editExpression=(JTextField)data[1];
		editExpression.setEditable(!readOnly);
		((JPanel)data[0]).add(getExpressionEditButton(this,editExpression,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		editExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationClock.Dialog.Color")+":"),BorderLayout.NORTH);
		cell.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);
		label.setLabelFor(colorChooser);

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationClock) {
			editExpression.setText(((ModelElementAnimationClock)element).getExpression());
			colorChooser.setColor(((ModelElementAnimationClock)element).getColor());
		}

		checkData(false);

		return content;
	}

	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		final String text=editExpression.getText().trim();
		if (text.isEmpty()) {
			ok=false;
			editExpression.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationClock.Dialog.Expression.Error.Title"),Language.tr("Surface.AnimationClock.Dialog.Expression.ErrorNoExpression.Info"));
				return false;
			}
		} else {
			int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				ok=false;
				editExpression.setBackground(Color.red);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationClock.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationClock.Dialog.Expression.ErrorInvalidExpression.Info"),text,error+1));
					return false;
				}
			} else {
				editExpression.setBackground(SystemColor.text);
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementAnimationClock clock=(ModelElementAnimationClock)element;
		clock.setExpression(editExpression.getText().trim());
		clock.setColor(colorChooser.getColor());
	}
}