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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * In diesem Panel kann ein Rechenausdruck oder ein Skript aus {@link AnimationExpression}
 * bearbeitet werden. Das Objekt wird dabei direkt im Konstruktor übergeben und die
 * Daten werden beim Aufruf von {@link #storeData()} direkt dorthin zurückgeschrieben.
 * @author Alexander Herzog
 * @see AnimationExpression
 */
public class AnimationExpressionPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6129350509255100815L;

	/**
	 * Modellelement in dem der Ausdruck verwendet werden soll
	 */
	private final ModelElement element;

	/**
	 * Zu bearbeitender Ausdruck (beim Speichern werden die Daten in dieses Objekt zurückgeschrieben)
	 */
	private final AnimationExpression expression;

	/**
	 * Nur-Lese-Status
	 */
	private final boolean readOnly;

	/**
	 * Callback für Klicks auf die Hilfe-Schaltfläche im Skript-Editor-Dialog
	 */
	private final Runnable helpRunnable;

	/**
	 * Auswahlbox für Modus (Ausdruck oder Skript)
	 */
	private final JComboBox<String> modeSelect;

	/**
	 * Bereich in dem der Ausdruck oder die Bearbeiten-Schaltfläche für das
	 * Skript angezeigt werden. Wird über die Auswahl in {@link #modeSelect} umgestellt.
	 * @see #modeSelect
	 * @see #updateCard()
	 */
	private final JPanel cards;

	/**
	 * Layout für {@link #cards}
	 * @see #modeSelect
	 * @see #updateCard()
	 */
	private final CardLayout cardsLayout;

	/**
	 * Eingabefeld zum Bearbeiten des Ausdrucks
	 */
	private final JTextField expressionEdit;

	/**
	 * Aktuelle Skriptsprache
	 * @see #script
	 */
	private AnimationExpression.ExpressionMode scriptMode;

	/**
	 * Aktuelles Skript
	 */
	private String script;

	/**
	 * Konstruktor der Klasse
	 * @param element	Modellelement in dem der Ausdruck verwendet werden soll
	 * @param expression	Bisheriger Ausdruck
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Callback für Klicks auf die Hilfe-Schaltfläche im Skript-Editor-Dialog
	 */
	public AnimationExpressionPanel(final ModelElement element, final AnimationExpression expression, final boolean readOnly, final Runnable helpRunnable) {
		this.element=element;
		this.expression=expression;
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		setBorder(BorderFactory.createEmptyBorder(0,5,0,5));

		setLayout(new BorderLayout(5,0));

		Box box;

		/* Modus */
		add(box=new Box(BoxLayout.PAGE_AXIS),BorderLayout.WEST);
		box.add(Box.createVerticalGlue());
		box.add(modeSelect=new JComboBox<>(new String[] {
				Language.tr("AnimationExpression.Expression"),
				Language.tr("AnimationExpression.Script")
		}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[] {
				Images.MODE_EXPRESSION,
				Images.SCRIPT_MODE_JAVASCRIPT
		}));
		modeSelect.setEditable(false);
		modeSelect.setEnabled(!readOnly);
		box.add(Box.createVerticalGlue());

		/* Cards */
		add(cards=new JPanel(cardsLayout=new CardLayout()),BorderLayout.CENTER);
		JPanel card;

		/* Ausdruck */
		cards.add(card=new JPanel(new BorderLayout(5,0)),"0");
		card.add(box=new Box(BoxLayout.PAGE_AXIS),BorderLayout.CENTER);
		box.add(Box.createVerticalGlue());
		box.add(expressionEdit=new JTextField());
		expressionEdit.setEnabled(!readOnly);
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		box.add(Box.createVerticalGlue());

		card.add(box=new Box(BoxLayout.PAGE_AXIS),BorderLayout.EAST);
		box.add(Box.createVerticalGlue());
		box.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,false,element.getModel(),element.getSurface()));
		box.add(Box.createVerticalGlue());

		/* Skript */
		cards.add(card=new JPanel(new FlowLayout(FlowLayout.LEFT)),"1");
		final JButton button=new JButton(Language.tr("AnimationExpression.Script.Edit"),Images.GENERAL_EDIT.getIcon());
		card.add(button);
		button.addActionListener(e->editScript());
		switch (expression.getMode()) {
		case Expression:
			scriptMode=AnimationExpression.ExpressionMode.Javascript;
			break;
		case Javascript:
		case Java:
			scriptMode=expression.getMode();
			break;
		}
		script=expression.getScript();

		expressionEdit.setMaximumSize(new Dimension(10000,button.getHeight()));

		/* Starten */
		modeSelect.setSelectedIndex((expression.getMode()==AnimationExpression.ExpressionMode.Expression)?0:1);
		modeSelect.addActionListener(e->updateCard());
		updateCard();
		expressionEdit.setText(expression.getExpression());
	}

	/**
	 * Aktualisiert die Darstellung in {@link #cards} nachdem
	 * in {@link #modeSelect} ein anderer Modus ausgewählt wurde.
	 * @see #modeSelect
	 * @see #cards
	 * @see #cardsLayout
	 */
	private void updateCard() {
		cardsLayout.show(cards,""+modeSelect.getSelectedIndex());
	}

	/**
	 * Befehl: Skript bearbeiten
	 * @see AnimationExpressionDialog
	 */
	private void editScript() {
		final AnimationExpressionDialog dialog=new AnimationExpressionDialog(this,scriptMode,script,element.getModel(),readOnly,helpRunnable);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			scriptMode=dialog.getMode();
			script=dialog.getScript();
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData(final boolean showErrorMessages) {
		if (readOnly || modeSelect.getSelectedIndex()!=0) return true;

		boolean ok=true;

		final String text=expressionEdit.getText().trim();
		if (text.isEmpty()) {
			ok=false;
			expressionEdit.setBackground(Color.RED);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("AnimationExpression.Expression.ErrorTitle"),Language.tr("AnimationExpression.Expression.ErrorInfoNoExpression"));
				return false;
			}
		} else {
			int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				ok=false;
				expressionEdit.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("AnimationExpression.Expression.ErrorTitle"),String.format(Language.tr("AnimationExpression.Expression.ErrorInfoInvalidExpression"),text,error+1));
					return false;
				}
			} else {
				expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Daten in das im Konstruktor übergebene
	 * {@link AnimationExpression}-Objekt zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		if (modeSelect.getSelectedIndex()==0) {
			expression.setExpression(expressionEdit.getText().trim());
		} else {
			switch (scriptMode) {
			case Expression:
				/* Haben wir in diesem Fall nicht. */
				break;
			case Javascript:
				expression.setJavascript(script);
				break;
			case Java:
				expression.setJava(script);
				break;
			}
		}
	}
}
