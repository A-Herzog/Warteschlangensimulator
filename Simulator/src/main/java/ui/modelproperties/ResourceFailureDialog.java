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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionEditorDialog;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResourceFailure;
import ui.modeleditor.ModelSurface;

/**
 * Dialog zur Bearbeitung eines einzelnen Ressourcenausfalls
 * @author Alexander Herzog
 */
public class ResourceFailureDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5525453066628106106L;

	/** Editor-Modell (für {@link ExpressionBuilder}) */
	private final EditModel model;
	/** Zeichenfläche (für {@link ExpressionBuilder}) */
	private final ModelSurface surface;

	/** Option "Ausfall nach Anzahl bedienter Kunden" */
	private final JRadioButton failureNumber;
	/** Option "Ausfall nach Anwesenheitszeit" */
	private final JRadioButton failureAvailable;
	/** Option "Ausfall nach gearbeiteter Zeit" */
	private final JRadioButton failureWorking;
	/** Option "Ausfallabstände gemäß Verteilung" */
	private final JRadioButton failureDistribution;
	/** Option "Ausfallabstände gemäß Ausdruck" */
	private final JRadioButton failureExpression;
	/** Eingabefeld für Option "Ausfall nach Anzahl bedienter Kunden" ({@link #failureNumber}) */
	private final JTextField failureNumberEdit;
	/** Eingabefeld für Option "Ausfall nach Anwesenheitszeit ({@link #failureAvailable})" */
	private final JTextField failureAvailableEdit;
	/** Eingabefeld für Option "Ausfall nach gearbeiteter Zeit" ({@link #failureWorking}) */
	private final JTextField failureWorkingEdit;
	/** Schaltfläche zum Bearbeiten der Verteilung für Option "Ausfallabstände gemäß Verteilung" ({@link #failureDistribution}) */
	private final JButton failureDistributionButton;
	/** Verteilung für {@link #failureDistribution} */
	private AbstractRealDistribution failureDistributionDist;
	/** Eingabefeld für Option "Ausfallabstände gemäß Ausdruck" ({@link #failureExpression}) */
	private final JTextField failureExpressionEdit;
	/** Auswahlfeld zur Wahl des Typs der Länge der Ausfallzeiten */
	private final JComboBox<String> downTimeSelect;
	/** Platzhalter für die Eingabebereiche für die Längen der Ausfallzeiten */
	private final JPanel downTimeCards;
	/** Layout zur Festlegung der aktuellen Anzeige in {@link #downTimeCards} */
	private final CardLayout downTimeCardLayout;
	/** Festlegung der Länge der Ausfallzeiten über eine Verteilung */
	private final JDistributionPanel downTimeDistribution;
	/** Festlegung der Länge der Ausfallzeiten über einen Rechenausdruck */
	private final JTextField downTimeExpression;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param failure	Ressourcen-Ausfall-Datensatz (kann auch <code>null</code> sein)
	 * @param model	Editor-Modell (für {@link ExpressionBuilder})
	 * @param surface	Zeichenfläche (für {@link ExpressionBuilder})
	 * @param help	Hilfe-Callback
	 */
	public ResourceFailureDialog(final Component owner, ModelResourceFailure failure, final EditModel model, final ModelSurface surface, final Runnable help) {
		super(owner,Language.tr("Resources.Group.EditName.Dialog.FailureTitle"));

		this.model=model;
		this.surface=surface;
		if (failure==null) failure=new ModelResourceFailure();

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		JPanel inner;
		content.add(inner=new JPanel(),BorderLayout.NORTH);
		inner.setLayout(new BoxLayout(inner,BoxLayout.PAGE_AXIS));

		JPanel panel;
		JLabel label;

		inner.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(failureNumber=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Failure.Number")));
		panel.add(failureNumberEdit=new JTextField("100",10));
		ModelElementBaseDialog.addUndoFeature(failureNumberEdit);
		if (failure.getFailureMode()==ModelResourceFailure.FailureMode.FAILURE_BY_NUMBER) {failureNumberEdit.setText(""+failure.getFailureNumber()); failureNumber.setSelected(true);}
		failureNumber.addActionListener(e->checkData(false));
		failureNumberEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {failureNumber.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {failureNumber.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {failureNumber.setSelected(true); checkData(false);}
		});

		inner.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(failureAvailable=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Failure.Available")));
		panel.add(failureAvailableEdit=new JTextField("86400",10));
		ModelElementBaseDialog.addUndoFeature(failureAvailableEdit);
		if (failure.getFailureMode()==ModelResourceFailure.FailureMode.FAILURE_BY_AVAILABLE_TIME) {failureAvailableEdit.setText(NumberTools.formatNumber(failure.getFailureTime())); failureAvailable.setSelected(true);}
		failureAvailable.addActionListener(e->checkData(false));
		failureAvailableEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {failureAvailable.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {failureAvailable.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {failureAvailable.setSelected(true); checkData(false);}
		});

		inner.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(failureWorking=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Failure.Working")));
		panel.add(failureWorkingEdit=new JTextField("86400",10));
		ModelElementBaseDialog.addUndoFeature(failureWorkingEdit);
		if (failure.getFailureMode()==ModelResourceFailure.FailureMode.FAILURE_BY_WORKING_TIME) {failureWorkingEdit.setText(NumberTools.formatNumber(failure.getFailureTime())); failureWorking.setSelected(true);}
		failureWorking.addActionListener(e->checkData(false));
		failureWorkingEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {failureWorking.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {failureWorking.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {failureWorking.setSelected(true); checkData(false);}
		});

		inner.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(failureDistribution=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Failure.Distribution")));
		if (failure.getFailureMode()==ModelResourceFailure.FailureMode.FAILURE_BY_DISTRIBUTION) {failureDistributionDist=failure.getFailureDistribution(); failureDistribution.setSelected(true);}
		failureDistribution.addActionListener(e->checkData(false));
		if (failureDistributionDist==null) failureDistributionDist=new ExponentialDistribution(null,86400,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		panel.add(failureDistributionButton=new JButton(Language.tr("Resources.Group.EditName.Dialog.Failure.Distribution.Edit")));
		failureDistributionButton.setIcon(Images.MODE_DISTRIBUTION.getIcon());
		failureDistributionButton.addActionListener(e->editInterDownTimeDistribution());

		inner.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(failureExpression=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Failure.Expression")));
		panel.add(failureExpressionEdit=new JTextField("86400",30));
		ModelElementBaseDialog.addUndoFeature(failureExpressionEdit);
		panel.add(ModelElementBaseDialog.getExpressionEditButton(this,failureExpressionEdit,false,false,model,surface));
		if (failure.getFailureMode()==ModelResourceFailure.FailureMode.FAILURE_BY_EXPRESSION) {failureExpressionEdit.setText(failure.getFailureExpression()); failureExpression.setSelected(true);}
		failureExpression.addActionListener(e->checkData(false));
		failureExpressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {failureExpression.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {failureExpression.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {failureExpression.setSelected(true); checkData(false);}
		});

		final ButtonGroup group=new ButtonGroup();
		group.add(failureNumber);
		group.add(failureAvailable);
		group.add(failureWorking);
		group.add(failureDistribution);
		group.add(failureExpression);

		inner.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(label=new JLabel(Language.tr("Resources.Group.EditName.Dialog.DownTime")));
		panel.add(downTimeSelect=new JComboBox<>(new String[]{
				Language.tr("Resources.Group.EditName.Dialog.DownTime.Distribution"),
				Language.tr("Resources.Group.EditName.Dialog.DownTime.Expression")
		}));
		downTimeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		label.setLabelFor(downTimeSelect);

		content.add(downTimeCards=new JPanel(downTimeCardLayout=new CardLayout()),BorderLayout.CENTER);

		downTimeCards.add(downTimeDistribution=new JDistributionPanel(failure.getDownTimeDistribution(),86400,true),"0");

		downTimeCards.add(panel=new JPanel(new BorderLayout()),"1");

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Resources.Group.EditName.Dialog.DownTime.Expression.Label")+":",(failure.getDownTimeExpression()==null)?"":failure.getDownTimeExpression());
		panel.add((JPanel)data[0],BorderLayout.NORTH);
		downTimeExpression=(JTextField)data[1];
		downTimeExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		((JPanel)data[0]).add(ModelElementBaseDialog.getExpressionEditButton(this.owner,downTimeExpression,false,false,model,surface),BorderLayout.EAST);

		if (failure.getDownTimeExpression()==null) {
			downTimeSelect.setSelectedIndex(0);
			downTimeCardLayout.show(downTimeCards,"0");
		} else {
			downTimeSelect.setSelectedIndex(1);
			downTimeCardLayout.show(downTimeCards,"1");
		}

		downTimeSelect.addActionListener(e->{
			downTimeCardLayout.show(downTimeCards,""+downTimeSelect.getSelectedIndex());
			checkData(false);
		});

		/* Dialog vorbereiten */
		setMinSizeRespectingScreensize(550,650);
		pack();
		checkData(false);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Reagiert auf Klicks auf {@link #failureDistributionButton}
	 * zur Bearbeitung der Ausfallabstände über eine Verteilung.
	 */
	private void editInterDownTimeDistribution() {
		final JDistributionEditorDialog dialog=new JDistributionEditorDialog(getOwner(),failureDistributionDist,10*86400,JDistributionPanel.BOTH,true,true,SetupData.getSetup().imageSize);
		dialog.setVisible(true);
		final AbstractRealDistribution newFailureDistributionDist=dialog.getNewDistribution();
		if (newFailureDistributionDist!=null) {
			failureDistributionDist=newFailureDistributionDist;
			failureDistribution.setSelected(true);
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorDialog	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorDialog) {
		boolean ok=true;

		if (failureNumber.isSelected()) {
			final Long L=NumberTools.getPositiveLong(failureNumberEdit,true);
			if (L==null) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Failure.Number.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.Failure.Number.Error.Info"));
					return false;
				}
				ok=false;
			}
		} else {
			failureNumberEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (failureAvailable.isSelected()) {
			final Double D=NumberTools.getPositiveDouble(failureAvailableEdit,true);
			if (D==null) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Failure.Available.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.Failure.Available.Error.Info"));
					return false;
				}
				ok=false;
			}
		} else {
			failureAvailableEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (failureWorking.isSelected()) {
			final Double D=NumberTools.getPositiveDouble(failureWorkingEdit,true);
			if (D==null) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Failure.Working.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.Failure.Working.Error.Info"));
					return false;
				}
				ok=false;
			}
		} else {
			failureWorkingEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (failureExpression.isSelected()) {
			final int i=ExpressionCalc.check(failureExpressionEdit.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (i<0) {
				failureExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				failureExpressionEdit.setBackground(Color.RED);
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Failure.Expression.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.Failure.Expression.Error.Info"));
					return false;
				}
				ok=false;
			}
		}

		if (downTimeSelect.getSelectedIndex()==1) {
			final int i=ExpressionCalc.check(downTimeExpression.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (i<0) {
				downTimeExpression.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				downTimeExpression.setBackground(Color.RED);
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.DownTime.Expression.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.DownTime.Expression.Error.Info"));
					return false;
				}
				ok=false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert, sofern der Dialog mit "Ok" geschlossen wurde, den neuen Ressourcen-Ausfall-Datensatz
	 * @return Neuer Ressourcen-Ausfall-Datensatz
	 */
	public ModelResourceFailure getFailure() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;

		final ModelResourceFailure failure=new ModelResourceFailure();

		if (failureNumber.isSelected()) failure.setFailureByNumber(NumberTools.getLong(failureNumberEdit,true).intValue());
		if (failureAvailable.isSelected()) failure.setFailureByAvailableTime(NumberTools.getDouble(failureAvailableEdit,true));
		if (failureWorking.isSelected()) failure.setFailureByWorkingTime(NumberTools.getDouble(failureWorkingEdit,true));
		if (failureDistribution.isSelected()) failure.setFailureByDistribution(failureDistributionDist);
		if (failureExpression.isSelected()) failure.setFailureByExpression(failureExpressionEdit.getText());
		switch (downTimeSelect.getSelectedIndex()) {
		case 0: failure.setDownTimeDistribution(downTimeDistribution.getDistribution()); break;
		case 1: failure.setDownTimeExpression(downTimeExpression.getText()); break;
		}

		return failure;
	}
}