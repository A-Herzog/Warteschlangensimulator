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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieser Dialog erlaubt das Bearbeiten eines einzelnen Datensatzes
 * aus einer {@link UserStatisticTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see UserStatisticTableModel
 */
public class UserStatisticTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2104982799396808067L;

	/** Liste aller anderen Statistik-Stationen im Modell */
	private ModelElementUserStatistic[] otherStations;
	/** Liste der Namen aller modellweit verfügbaren Variablennamen */
	private String[] variableNames;
	/** Eingabefeld für den Bezeichner für die Kenngröße */
	private JTextField key;
	/** Eingabefeld für die zu erfassende Kenngröße */
	private JTextField expression;
	/** Option: Handelt es sich um eine Zeitangabe? */
	private JCheckBox isTime;
	/** Label zur Anzeige von Warnungen, wenn derselbe Schlüssel an einer anderen Station ein anderes Format hat */
	private JLabel formatWarning;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param id	ID der aktuellen Station
	 * @param key	Bisheriger Bezeichner für die Kenngröße
	 * @param isTime	Handelt es sich um eine Zeitangabe (<code>true</code>) oder eine Zahl (<code>false</code>)
	 * @param expression	Bisherige zu erfassende Kenngröße
	 * @param model	Gesamtes Modell (für den Expression-Builder)
	 * @param surface	Haupt-Zeichenfläche (für den Expression-Builder)
	 */
	public UserStatisticTableModelDialog(final Component owner, final Runnable help, final int id, final String key, final boolean isTime, final String expression, final EditModel model, final ModelSurface surface) {
		super(owner,Language.tr("Surface.UserStatistic.Table.Edit"));

		/* Alle anderen Statistik-Stationen identifizieren */

		final List<ModelElementUserStatistic> otherStations=new ArrayList<>();
		for (ModelElement e1: model.surface.getElements()) {
			if ((e1 instanceof ModelElementUserStatistic) && (e1.getId()!=id)) otherStations.add((ModelElementUserStatistic)e1);
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if ((e2 instanceof ModelElementUserStatistic) && (e2.getId()!=id)) otherStations.add((ModelElementUserStatistic)e2);
			}
		}
		this.otherStations=otherStations.toArray(new ModelElementUserStatistic[0]);

		/* Variablen (für Expression-Editor) ermitteln */
		variableNames=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true);

		/* GUI */
		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] obj;
		JPanel line;

		/* Schlüssel */
		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.UserStatistic.Table.Edit.Key"),key);
		content.add((JPanel)obj[0]);
		this.key=(JTextField)obj[1];
		this.key.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false); updataFormatWarning();}
			@Override public void keyPressed(KeyEvent e) {checkData(false); updataFormatWarning();}
			@Override public void keyReleased(KeyEvent e) {checkData(false); updataFormatWarning();}
		});

		/* Ausdruck */
		obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.UserStatistic.Table.Edit.Expression"),expression);
		content.add((JPanel)obj[0]);
		this.expression=(JTextField)obj[1];
		this.expression.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});
		((JPanel)obj[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,this.expression,false,true,model,surface),BorderLayout.EAST);

		/* In der Statistik als Zeit formatiert ausgeben? */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(this.isTime=new JCheckBox(Language.tr("Surface.UserStatistic.Table.Edit.IsTime"),isTime));
		this.isTime.addActionListener(e->updataFormatWarning());

		/* Inkonsistenzen mit anderen Stationen in Bezug auf die Formatierung */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(formatWarning=new JLabel());
		formatWarning.setVisible(false);

		updataFormatWarning();

		/* Dialog starten */
		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Zeigt wenn nötig eine Warnung an, dass der Schlüssel an einer
	 * anderen Station mit einem anderen Format definiert ist.
	 */
	private void updataFormatWarning() {
		final boolean isTime=this.isTime.isSelected();
		final String key=this.key.getText().trim();

		final List<Integer> otherFormatAt=new ArrayList<>();

		/* Stationen ermitteln, an der der Schlüssel ein anderes Format hat */
		for (ModelElementUserStatistic otherStation: otherStations) {
			int index=-1;
			final List<String> otherKeys=otherStation.getKeys();
			for (int i=0;i<otherKeys.size();i++) if (otherKeys.get(i).equalsIgnoreCase(key)) {index=i; break;}
			if (index>=0) {
				if (otherStation.getIsTime().get(index).booleanValue()!=isTime) otherFormatAt.add(otherStation.getId());
			}
		}

		/* Ggf. Warnung anzeigen */
		if (otherFormatAt.size()==0) {
			formatWarning.setVisible(false);
		} else {
			final StringBuilder warning=new StringBuilder();
			warning.append("<html><body>\n");
			if (isTime) {
				warning.append(String.format(Language.tr("Surface.UserStatistic.Table.Edit.IsTime.Warning.ThisFormat.Time"),key));
			} else {
				warning.append(String.format(Language.tr("Surface.UserStatistic.Table.Edit.IsTime.Warning.ThisFormat.Number"),key));
			}
			warning.append("<br>\n");
			if (otherFormatAt.size()==1) {
				warning.append(String.format(Language.tr("Surface.UserStatistic.Table.Edit.IsTime.Warning.OtherFormat.Single"),otherFormatAt.get(0)));
			} else {
				warning.append(String.format(Language.tr("Surface.UserStatistic.Table.Edit.IsTime.Warning.OtherFormat.Multi"),otherFormatAt.get(0)));
			}
			warning.append("</body></html>\n");
			formatWarning.setText(warning.toString());
			formatWarning.setVisible(true);
		}
		pack();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (key.getText().trim().isEmpty()) {
			key.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.UserStatistic.Table.Edit.Key.ErrorTitle"),Language.tr("Surface.UserStatistic.Table.Edit.Key.ErrorInfo"));
				return false;
			}
		} else {
			key.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (expression.getText().trim().isEmpty()) {
			expression.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorTitle"),Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorInfoEmpty"));
				return false;
			}
		} else {
			final int error=ExpressionCalc.check(expression.getText().trim(),variableNames);
			if (error<0) {
				expression.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorTitle"),String.format(Language.tr("Surface.UserStatistic.Table.Edit.Expression.ErrorInfoInvalid"),expression.getText().trim(),error));
					return false;
				}
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so liefert diese Methode den neuen Bezeichner für die Kenngrößenerfassung.
	 * @return	Neuer Bezeichner für die Kenngrößenerfassung
	 */
	public String getKey() {
		return key.getText().trim();
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so liefert diese Methode die Information darüber,
	 * ob es sich bei der Kenngröße um eine Zeitangabe oder um eine Zahl handelt.
	 * @return	Kenngröße ist Zeit (<code>true</code>) oder Zahl (<code>false</code>)
	 */
	public boolean getIsTime() {
		return isTime.isSelected();
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so liefert diese Methode den neuen Rechenausdruck für die Kenngrößenerfassung.
	 * @return	Neuer Rechenausdruck für die Kenngrößenerfassung
	 */
	public String getExpression() {
		return expression.getText().trim();
	}
}
