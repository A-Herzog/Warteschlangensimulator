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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import parser.MathCalcError;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialogseite "Simulation"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageSimulation extends ModelPropertiesDialogPage {
	/** Option "Anzahl an Kundenank�nften als Kriterium f�r das Simulationsende verwenden" */
	private JCheckBox terminationByClientClount;
	/** Eingabefeld "Zu simulierende Ank�nfte" */
	private JTextField clientCount;
	/** Eingabefeld "Einschwingphase" */
	private JTextField warmUpTime;
	/** Erkl�rung zu Eingabefeld {@link #warmUpTime} */
	private JLabel warmUpTimeInfo;
	/** Option "Einschwingphase zeitgesteuert beenden" */
	private JCheckBox warmUpTimeTimeCheck;
	/** Eingabefeld f�r "Einschwingphase zeitgesteuert beenden" */
	private JTextField warmUpTimeTimeEdit;
	/** Option "Zu pr�fende Bedingung als Kriterium f�r das Simulationsende verwenden" */
	private JCheckBox terminationByCondition;
	/** Eingabefeld "Bedingung f�r Simulationsende" */
	private JTextField terminationCondition;
	/** Option "Simulation nach bestimmter Zeit beenden" */
	private JCheckBox terminationByTime;
	/** Eingabefeld "Zeitpunkt an dem die Simulation endet" */
	private JTextField terminationTime;
	/** Eingabefeld "Anzahl an Wiederholungen des gesamten Simulationslaufs" */
	private JTextField repeatCount;
	/** Option "Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann" */
	private JCheckBox stoppOnCalcError;
	/** Option "Zeitabh�ngige Bedingungspr�fungen aktivieren" */
	private JCheckBox useTimedChecks;
	/** Eingabefeld "Zeitabstand" */
	private JTextField editTimedChecks;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageSimulation(final ModelPropertiesDialog dialog, final EditModel model, final boolean readOnly, final Runnable help) {
		super(dialog,model,readOnly,help);
	}

	/**
	 * Aktualisiert den Info-Label zu der Einschwingphase {@link #warmUpTimeInfo},
	 * wenn der Zahlenwert in {@link #warmUpTime} ver�ndert wurde.
	 * @see #warmUpTime
	 * @see #warmUpTimeInfo
	 */
	private void updateWarmUpTimeInfo() {
		final long clientCount=calcClientCount(false);
		final Double D=NumberTools.getNotNegativeDouble(warmUpTime,!readOnly);
		if (clientCount<=0 || D==null) {
			warmUpTimeInfo.setVisible(false);
		} else {
			final int additionalClients=(int)Math.round(clientCount*D.doubleValue());
			warmUpTimeInfo.setText("<html><body>"+String.format(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase.Info2"),NumberTools.formatLong(additionalClients)));
			warmUpTimeInfo.setVisible(true);
		}
	}

	@Override
	public void build(JPanel content) {
		JPanel sub;
		Object[] data;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Anzahl an Kunden */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(terminationByClientClount=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.UseNumberOfArrivals")+"</b></html>"));
		terminationByClientClount.setEnabled(!readOnly);
		terminationByClientClount.setSelected(model.useClientCount);
		terminationByClientClount.addActionListener(e->dialog.testCorrelationWarning());

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.NumberOfArrivals")+":",""+model.clientCount,10);
		lines.add(sub=(JPanel)data[0]);
		clientCount=(JTextField)data[1];
		clientCount.setEnabled(!readOnly);
		addKeyListener(clientCount,()->{
			terminationByClientClount.setSelected(true);
			calcClientCount(false);
			dialog.testCorrelationWarning();
			updateWarmUpTimeInfo();
		});
		final JButton buildButton=new JButton();
		sub.add(buildButton);
		buildButton.setPreferredSize(new Dimension(26,26));
		buildButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		buildButton.setToolTipText(Language.tr("Editor.DialogBase.ExpressionEditTooltip"));
		buildButton.setEnabled(!readOnly);
		buildButton.addActionListener(e->{
			final ExpressionBuilder builderDialog=new ExpressionBuilder(dialog,clientCount.getText(),false,new String[0],null,null,null,false,true,true);
			builderDialog.setVisible(true);
			if (builderDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) clientCount.setText(builderDialog.getExpression());
		});

		/* Einschwingphase als Kundenanzahl-Anteil */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase")+":",NumberTools.formatPercent(model.warmUpTime,3),6);
		lines.add((JPanel)data[0]);
		warmUpTime=(JTextField)data[1];
		warmUpTime.setEnabled(!readOnly);
		addKeyListener(warmUpTime,()->{
			NumberTools.getNotNegativeDouble(warmUpTime,true);
			updateWarmUpTimeInfo();
		});
		((JPanel)data[0]).add(new JLabel("("+Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase.Info")+")"));
		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(warmUpTimeInfo=new JLabel());
		updateWarmUpTimeInfo();

		/* Einschwingphase zeitgesteuert beenden */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(warmUpTimeTimeCheck=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhaseTime")+":"));
		warmUpTimeTimeCheck.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhaseTime.Info"));
		warmUpTimeTimeCheck.setEnabled(!readOnly);
		warmUpTimeTimeCheck.setSelected(model.warmUpTimeTime>0);
		warmUpTimeTimeCheck.addActionListener(e->checkWarmUpTimeTime());
		sub.add(warmUpTimeTimeEdit=new JTextField(TimeTools.formatLongTime((model.warmUpTimeTime>0)?model.warmUpTimeTime:300),15));
		ModelElementBaseDialog.addUndoFeature(warmUpTimeTimeEdit);
		warmUpTimeTimeEdit.setEnabled(!readOnly);
		addKeyListener(warmUpTimeTimeEdit,()->{
			warmUpTimeTimeCheck.setSelected(true);
			checkWarmUpTimeTime();
		});

		lines.add(Box.createVerticalStrut(25));

		/* Abbruchbedingung */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(terminationByCondition=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.UseCondition")+"</b></html>"));
		terminationByCondition.setEnabled(!readOnly);
		terminationByCondition.setSelected(model.useTerminationCondition);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.Condition")+":",model.terminationCondition);
		sub=(JPanel)data[0];
		lines.add(sub);
		terminationCondition=(JTextField)data[1];
		sub.add(ModelElementBaseDialog.getExpressionEditButton(dialog,terminationCondition,true,false,model,model.surface),BorderLayout.EAST);
		terminationCondition.setEnabled(!readOnly);
		addKeyListener(terminationCondition,()->{
			terminationByCondition.setSelected(true);
			checkTerminationCondition();
		});

		lines.add(Box.createVerticalStrut(25));

		/* Abbruchzeit */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(terminationByTime=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.UseTime")+"</b></html>"));
		terminationByTime.setEnabled(!readOnly);
		terminationByTime.setSelected(model.useFinishTime);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.Time")+":",TimeTools.formatLongTime(model.finishTime),15);
		sub=(JPanel)data[0];
		lines.add(sub);
		terminationTime=(JTextField)data[1];
		terminationTime.setEnabled(!readOnly);
		addKeyListener(terminationTime,()->{
			terminationByTime.setSelected(true);
			checkTerminationTime();
		});

		lines.add(Box.createVerticalStrut(25));

		/* Weitere Simulationseinstellungen */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.MoreSimulationSettings")+"</b></html>"));

		/* Wiederholungen */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.RepeatCount.Value")+":",""+model.repeatCount,5);
		sub=(JPanel)data[0];
		lines.add(sub);
		repeatCount=(JTextField)data[1];
		repeatCount.setEnabled(!readOnly);
		addKeyListener(repeatCount,()->checkRepeatCount());

		/* Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann. */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		sub.add(stoppOnCalcError=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.StoppOnCalcError"),model.stoppOnCalcError));
		stoppOnCalcError.setEnabled(!readOnly);
		lines.add(sub);

		/* Zeitabh�ngige Bedingungspr�fungen */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(useTimedChecks=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks"),model.timedChecksDelta>0));
		useTimedChecks.setEnabled(!readOnly);
		useTimedChecks.addActionListener(e->checkTimedChecks());
		sub.add(editTimedChecks=new JTextField(NumberTools.formatNumber((model.timedChecksDelta>0)?(model.timedChecksDelta/1000.0):1.0),5));
		ModelElementBaseDialog.addUndoFeature(editTimedChecks);
		editTimedChecks.setEnabled(!readOnly);
		addKeyListener(editTimedChecks,()->{
			useTimedChecks.setSelected(true);
			checkTimedChecks();
		});
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks.Seconds")));
	}

	/**
	 * Pr�ft die angegebene Einschwingphasen-Zeitdauer.
	 * @return	Liefert <code>true</code>, wenn die Einschwingphasen-Zeitdauer g�ltig ist oder �berhaupt nicht verwendet werden soll.
	 */
	private boolean checkWarmUpTimeTime() {
		if (!warmUpTimeTimeCheck.isSelected()) return true;
		return TimeTools.getTime(warmUpTimeTimeEdit,true)!=null;
	}

	/**
	 * Pr�ft die eingegebene Abbruchbedingung f�r die Simulation.
	 * @return	Liefert im Erfolgsfall -1, sonst die 0-basierende Position des Fehlers innerhalb der Zeichenkette
	 * @see #terminationCondition
	 * @see #checkData()
	 */
	private int checkTerminationCondition() {
		if (terminationCondition.getText().isBlank()) {
			terminationCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
			return -1;
		}

		final int error=ExpressionMultiEval.check(terminationCondition.getText(),model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
		if (error>=0) terminationCondition.setBackground(Color.red); else terminationCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
		return error;
	}

	/**
	 * Pr�ft die eingegebene Abbruchzeit f�r die Simulation.
	 * @return	Liefert <code>true</code>, wenn die eingegebene Abbruchzeit g�ltig ist.
	 * @see #terminationTime
	 * @see #checkData()
	 */
	private boolean checkTerminationTime() {
		return (TimeTools.getTime(terminationTime,true)!=null);
	}

	/**
	 * Pr�ft die angegebene Anzahl an Wiederholungen der Simulation.
	 * @return	Liefert <code>true</code>, wenn die angegebene Anzahl an Wiederholungen der Simulation g�ltig ist.
	 * @see #repeatCount
	 * @see #checkData()
	 */
	private boolean checkRepeatCount() {
		return (NumberTools.getPositiveLong(repeatCount,true)!=null);
	}

	/**
	 * Pr�ft ob das angegebene Intervall f�r die zeitabh�ngigen Pr�fungen g�ltig ist.
	 * @return	Liefert <code>true</code>, wenn das Pr�fungsintervall g�ltig ist.
	 * @see #editTimedChecks
	 * @see #checkData()
	 */
	private boolean checkTimedChecks() {
		if (!useTimedChecks.isSelected()) {
			editTimedChecks.setBackground(NumberTools.getTextFieldDefaultBackground());
			return true;
		}
		return (NumberTools.getPositiveDouble(editTimedChecks,true)!=null);
	}

	@Override
	public boolean checkData() {
		if (readOnly) return true;

		if (!terminationByClientClount.isSelected() && !terminationByCondition.isSelected() && !terminationByTime.isSelected()) {
			if (!MsgBox.confirm(dialog,Language.tr("Editor.Dialog.Tab.Simulation.Criteria.Title"),Language.tr("Editor.Dialog.Tab.Simulation.Criteria.WarningNoCriterium"),Language.tr("Editor.Dialog.Tab.Simulation.Criteria.YesRunWithout"),Language.tr("Editor.Dialog.Tab.Simulation.Criteria.NoKeepDialogOpen"))) return false;
			/* Auch Modelle ohne explizites Ende-Kriterium zulassen. Siehe auch RunModel.initGeneralData. */
			/* MsgBox.error(dialog,Language.tr("Editor.Dialog.Tab.Simulation.Criteria.Title"),Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorAtLeastOne"));
			return false; */
		}

		calcClientCount(true);
		Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (D==null) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorWarmUp"),warmUpTime.getText()));
			return false;
		}

		if (!checkWarmUpTimeTime()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorWarmUpTime"),warmUpTimeTimeEdit.getText()));
			return false;
		}

		final int error=checkTerminationCondition();
		if (error>=0 && terminationByCondition.isSelected()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorCondition"),terminationCondition.getText(),error+1));
			return false;
		}

		final boolean timeOk=checkTerminationTime();
		if (!timeOk && terminationByTime.isSelected()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorTime"),terminationTime.getText()));
			return false;
		}

		if (!checkRepeatCount()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.RepeatCount.Error"),repeatCount.getText()));
			return false;
		}

		if (!checkTimedChecks()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks.Error"),editTimedChecks.getText()));
			return false;
		}

		return true;
	}

	@Override
	public void storeData() {
		Double D;
		Long L;

		model.useClientCount=terminationByClientClount.isSelected();
		final long clientCount=calcClientCount(false);
		if (clientCount>0) model.clientCount=clientCount;

		D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (D!=null) model.warmUpTime=D;

		if (warmUpTimeTimeCheck.isSelected()) {
			model.warmUpTimeTime=TimeTools.getTime(warmUpTimeTimeEdit,true);
		} else {
			model.warmUpTimeTime=-1;
		}

		model.useTerminationCondition=terminationByCondition.isSelected();
		model.terminationCondition=terminationCondition.getText();

		model.useFinishTime=terminationByTime.isSelected();
		L=TimeTools.getTime(terminationTime,true);
		if (L==null) model.finishTime=10*86400; else model.finishTime=L;

		L=NumberTools.getPositiveLong(repeatCount,true);
		if (L!=null) model.repeatCount=(int)L.longValue();

		model.stoppOnCalcError=stoppOnCalcError.isSelected();

		if (useTimedChecks.isSelected()) {
			D=NumberTools.getPositiveDouble(editTimedChecks,true);
			if (D!=null) model.timedChecksDelta=(int)Math.round(D.doubleValue()*1000);
		} else {
			model.timedChecksDelta=-1;
		}
	}

	/**
	 * Berechnet die Anzahl an Ank�nften aus der Text-Eingabe.
	 * @param showErrorMessage	Soll eine Fehlermeldung ausgegeben werden, wenn die Eingabe nicht interpretiert werden konnte?
	 * @return	Anzahl an Ank�nften oder -1, wenn diese nicht bestimmt werden konnte
	 */
	private long calcClientCount(final boolean showErrorMessage) {
		long l;

		final String expression=clientCount.getText().trim();
		if (expression.isEmpty()) {
			l=-1;
		} else {
			final ExpressionCalc calc=new ExpressionCalc(null,model.userFunctions);
			final int error=calc.parse(expression);
			if (error>=0) {
				l=-1;
			} else {
				try {
					final double d=calc.calc();
					l=Math.round(d);
				} catch (MathCalcError e) {
					l=-1;
				}
			}
		}

		if (l<=0) {
			clientCount.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorClients"),clientCount.getText()));
			}
			return -1;
		} else {
			if (!readOnly) clientCount.setBackground(NumberTools.getTextFieldDefaultBackground());
			return l;
		}
	}

	/**
	 * Liefert die eingestellte Anzahl an zu simulierenden Kunden.
	 * @return	Anzahl an zu simulierenden Kunden (kann -1 sein, wenn keine Zahl ermittelt werden konnte oder aber wenn die Anzahl an Ank�nften kein Abbruchkriterium ist)
	 */
	public long getTerminationClientCount() {
		if (!terminationByClientClount.isSelected()) return -1;

		return calcClientCount(false);
	}
}
