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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Panel welches die Konfiguration der Lade- oder Entladezeiten
 * einer Transportergruppe darstellt.
 * @author Alexander Herzog
 * @see TransporterTableModelDialog
 */
public class TransporterTableModelDialogLoadingTimes extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8218899396948508334L;

	private final String[] variables;

	private final JComboBox<String> mode;

	private final JPanel main;
	private final CardLayout mainCards;

	private final JDistributionPanel distribution;
	private final JTextField expression;

	/**
	 * Konstruktor der Klasse
	 * @param data	Bisherige Lade- bzw. Entladezeit (<code>null</code>, Verteilung ({@link AbstractRealDistribution}) oder ein Rechenausdruck ({@link String}))
	 * @param loading	Handelt es sich um Ladezeiten (<code>true</code>) oder Entladezeiten (<code>false</code>)
	 * @param model	Editor-Modell (aus dem Variablennamen ausgelesen werden)
	 */
	public TransporterTableModelDialogLoadingTimes(final Object data, final boolean loading, final EditModel model) {
		super();
		setLayout(new BorderLayout());

		this.variables=model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false);

		/* Modusauswahl */
		JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(top,BorderLayout.NORTH);
		top.add(mode=new JComboBox<>(getModeStrings(loading)));
		mode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		mode.setSelectedIndex(0);
		if (data instanceof AbstractRealDistribution) mode.setSelectedIndex(1);
		if (data instanceof String) mode.setSelectedIndex(2);

		/* Karten */
		add(main=new JPanel(mainCards=new CardLayout()),BorderLayout.CENTER);

		/* Karte: Leer */
		main.add(new JPanel(),"Seite0");

		/* Karte: Verteilung */
		final AbstractRealDistribution dist=(data instanceof AbstractRealDistribution)?((AbstractRealDistribution)data):new ExponentialDistribution(null,300,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		main.add(distribution=new JDistributionPanel(dist,3600,true),"Seite1");

		/* Karte: Ausdruck */
		final String expr=(data instanceof String)?((String)data):"0";
		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Transporters.Group.Edit.Dialog.Times.LoadingExpression")+":",expr);
		final JPanel line=(JPanel)obj[0];
		final JPanel page=new JPanel(new BorderLayout());
		page.add(line,BorderLayout.NORTH);
		main.add(page,"Seite2");
		expression=(JTextField)obj[1];
		expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,expression,false,false,model,model.surface),BorderLayout.EAST);

		/* Start */
		mainCards.show(main,"Seite"+mode.getSelectedIndex());
		mode.addActionListener(e->{
			mainCards.show(main,"Seite"+mode.getSelectedIndex());
			checkData(false);
		});
		checkData(false);
	}

	private String[] getModeStrings(final boolean loading) {
		if (loading) {
			return new String[] {
					Language.tr("Transporters.Group.Edit.Dialog.Times.Mode.Loading.No"),
					Language.tr("Transporters.Group.Edit.Dialog.Times.Mode.Loading.Distribution"),
					Language.tr("Transporters.Group.Edit.Dialog.Times.Mode.Loading.Expression")
			};
		} else {
			return new String[] {
					Language.tr("Transporters.Group.Edit.Dialog.Times.Mode.Unloading.No"),
					Language.tr("Transporters.Group.Edit.Dialog.Times.Mode.Unloading.Distribution"),
					Language.tr("Transporters.Group.Edit.Dialog.Times.Mode.Unloading.Expression")
			};
		}
	}

	/**
	 * Prüft die Einstellungen auf Gültigkeit.
	 * @param showErrorMessage	Fehlermeldung im Fall von ungültigen Daten anzeigen?
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 * @see TransporterTableModelDialog
	 */
	public boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		/* Nur Ausdruck muss geprüft werden */
		if (mode.getSelectedIndex()==2) {
			final String expr=expression.getText().trim();
			if (expr.isEmpty()) {
				ok=false;
				expression.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Times.LoadingExpression.ErrorTitle"),Language.tr("Transporters.Group.Edit.Dialog.Times.LoadingExpression.ErrorInfoEmpty"));
					return false;
				}
			} else {
				final int error=ExpressionCalc.check(expr,variables);
				if (error>=0) {
					ok=false;
					expression.setBackground(Color.RED);
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Times.LoadingExpression.ErrorTitle"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Times.LoadingExpression.ErrorInfoInvalid"),expr,error));
						return false;
					}
				} else {
					expression.setBackground(SystemColor.text);
				}
			}
		}

		return ok;
	}

	/**
	 * Liefert die gewählte Lade- bzw. Entladezeit.
	 * @return	Lade- bzw. Entladezeit (<code>null</code>, Verteilung ({@link AbstractRealDistribution}) oder ein Rechenausdruck ({@link String}))
	 */
	public Object getData() {
		switch (mode.getSelectedIndex()) {
		case 0: return null;
		case 1: return distribution.getDistribution();
		case 2: return expression.getText();
		default: return null;
		}
	}
}
