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
import mathtools.distribution.tools.ThreadLocalRandomGenerator;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionEval;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialogseite "Simulation"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageSimulation extends ModelPropertiesDialogPage {
	/** Option "Anzahl an Kundenankünften als Kriterium für das Simulationsende verwenden" */
	private JCheckBox terminationByClientClount;
	/** Eingabefeld "Zu simulierende Ankünfte" */
	private JTextField clientCount;
	/** Eingabefeld "Einschwingphase" */
	private JTextField warmUpTime;
	/** Erklärung zu Eingabefeld {@link #warmUpTime} */
	private JLabel warmUpTimeInfo;
	/** Option "Zu prüfende Bedingung als Kriterium für das Simulationsende verwenden" */
	private JCheckBox terminationByCondition;
	/** Eingabefeld "Bedingung für Simulationsende" */
	private JTextField terminationCondition;
	/** Option "Simulation nach bestimmter Zeit beenden" */
	private JCheckBox terminationByTime;
	/** Eingabefeld "Zeitpunkt an dem die Simulation endet" */
	private JTextField terminationTime;
	/** Option "Fester Startwert für Zufallszahlengenerator" */
	private JCheckBox useFixedSeed;
	/** Eingabefeld "Startwert" */
	private JTextField fixedSeed;
	/** Eingabefeld "Anzahl an Wiederholungen des gesamten Simulationslaufs" */
	private JTextField repeatCount;
	/** Option "Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann" */
	private JCheckBox stoppOnCalcError;
	/** Option "Zeitabhängige Bedingungsprüfungen aktivieren" */
	private JCheckBox useTimedChecks;
	/** Eingabefeld "Zeitabstand" */
	private JTextField editTimedChecks;
	/** Option "Kunden, die am Simulationsende das System noch nicht verlassen haben, erfassen" */
	private JCheckBox recordIncompleteClients;

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
	 * wenn der Zahlenwert in {@link #warmUpTime} verändert wurde.
	 * @see #warmUpTime
	 * @see #warmUpTimeInfo
	 */
	private void updateWarmUpTimeInfo() {
		final Integer I=NumberTools.getNotNegativeInteger(clientCount,true);
		final Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (I==null || I==0 || D==null) {
			warmUpTimeInfo.setVisible(false);
		} else {
			final int additionalClients=(int)Math.round(I.intValue()*D.doubleValue());
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
		lines.add((JPanel)data[0]);
		clientCount=(JTextField)data[1];
		clientCount.setEditable(!readOnly);
		addKeyListener(clientCount,()->{
			terminationByClientClount.setSelected(true);
			NumberTools.getNotNegativeInteger(clientCount,true);
			dialog.testCorrelationWarning();
			updateWarmUpTimeInfo();
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase")+":",NumberTools.formatPercent(model.warmUpTime,3),6);
		lines.add((JPanel)data[0]);
		warmUpTime=(JTextField)data[1];
		warmUpTime.setEditable(!readOnly);
		addKeyListener(warmUpTime,()->{
			NumberTools.getNotNegativeDouble(warmUpTime,true);
			updateWarmUpTimeInfo();
		});
		((JPanel)data[0]).add(new JLabel("("+Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase.Info")+")"));
		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(warmUpTimeInfo=new JLabel());
		updateWarmUpTimeInfo();

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
		terminationCondition.setEditable(!readOnly);
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
		terminationTime.setEditable(!readOnly);
		addKeyListener(terminationTime,()->{
			terminationByTime.setSelected(true);
			checkTerminationTime();
		});

		lines.add(Box.createVerticalStrut(25));

		/* Fester Startwert für Zufallszahlengenerator */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(useFixedSeed=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed")+"</b></html>"));
		useFixedSeed.setEnabled(!readOnly);
		useFixedSeed.setSelected(model.useFixedSeed);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.Value")+":",""+model.fixedSeed,20);
		sub=(JPanel)data[0];
		lines.add(sub);
		fixedSeed=(JTextField)data[1];
		fixedSeed.setEditable(!readOnly);
		addKeyListener(fixedSeed,()->{
			useFixedSeed.setSelected(true);
			checkFixedSeed();
		});
		if (!readOnly) {
			final JButton fixedSeedButton=new JButton(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.RandomButton"));
			fixedSeedButton.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.RandomButton.Hint"));
			fixedSeedButton.setIcon(Images.MODELPROPERTIES_SIMULATION_RANDOM_SEED.getIcon());
			sub.add(fixedSeedButton);
			fixedSeedButton.addActionListener(e->{
				fixedSeed.setText(""+new ThreadLocalRandomGenerator().nextLong());
				useFixedSeed.setSelected(true);
			});
		}

		lines.add(Box.createVerticalStrut(25));

		/* Weitere Simulationseinstellungen */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.MoreSimulationSettings")+"</b></html>"));

		/* Wiederholungen */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.RepeatCount.Value")+":",""+model.repeatCount,5);
		sub=(JPanel)data[0];
		lines.add(sub);
		repeatCount=(JTextField)data[1];
		repeatCount.setEditable(!readOnly);
		addKeyListener(repeatCount,()->checkRepeatCount());

		/* Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann. */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		sub.add(stoppOnCalcError=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.StoppOnCalcError"),model.stoppOnCalcError));
		stoppOnCalcError.setEnabled(!readOnly);
		lines.add(sub);

		/* Zeitabhängige Bedingungsprüfungen */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(useTimedChecks=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks"),model.timedChecksDelta>0));
		useTimedChecks.setEnabled(!readOnly);
		useTimedChecks.addActionListener(e->checkTimedChecks());
		sub.add(editTimedChecks=new JTextField(NumberTools.formatNumber((model.timedChecksDelta>0)?(model.timedChecksDelta/1000.0):1.0),5));
		editTimedChecks.setEnabled(!readOnly);
		addKeyListener(editTimedChecks,()->{
			useTimedChecks.setSelected(true);
			checkTimedChecks();
		});
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks.Seconds")));

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(recordIncompleteClients=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.RecordIncompleteClients"),model.recordIncompleteClients));
		recordIncompleteClients.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.RecordIncompleteClients.Hint"));
		recordIncompleteClients.setEnabled(!readOnly);
	}

	/**
	 * Prüft die eingegebene Abbruchbedingung für die Simulation.
	 * @return	Liefert im Erfolgsfall -1, sonst die 0-basierende Position des Fehlers innerhalb der Zeichenkette
	 * @see #terminationCondition
	 * @see #checkData()
	 */
	private int checkTerminationCondition() {
		if (terminationCondition.getText().trim().isEmpty()) {
			terminationCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
			return -1;
		}

		final int error=ExpressionEval.check(terminationCondition.getText(),model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
		if (error>=0) terminationCondition.setBackground(Color.red); else terminationCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
		return error;
	}

	/**
	 * Prüft die eingegebene Abbruchzeit für die Simulation.
	 * @return	Liefert <code>true</code>, wenn die eingegebene Abbruchzeit gültig ist.
	 * @see #terminationTime
	 * @see #checkData()
	 */
	private boolean checkTerminationTime() {
		return (TimeTools.getTime(terminationTime,true)!=null);
	}

	/**
	 * Prüft den eingegebenen Startwert für den Zufallszahlengenerator.
	 * @return	Liefert <code>true</code>, wenn der eingegebene Startwert für den Zufallszahlengenerator gültig ist.
	 * @see #fixedSeed
	 * @see #checkData()
	 */
	private boolean checkFixedSeed() {
		return (NumberTools.getLong(fixedSeed,true)!=null);
	}

	/**
	 * Prüft die angegebene Anzahl an Wiederholungen der Simulation.
	 * @return	Liefert <code>true</code>, wenn die angegebene Anzahl an Wiederholungen der Simulation gültig ist.
	 * @see #repeatCount
	 * @see #checkData()
	 */
	private boolean checkRepeatCount() {
		return (NumberTools.getPositiveLong(repeatCount,true)!=null);
	}

	/**
	 * Prüft ob das angegebene Intervall für die zeitabhängigen Prüfungen gültig ist.
	 * @return	Liefert <code>true</code>, wenn das Prüfungsintervall gültig ist.
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
		if (!terminationByClientClount.isSelected() && !terminationByCondition.isSelected() && !terminationByTime.isSelected()) {
			MsgBox.error(dialog,Language.tr("Editor.Dialog.Tab.Simulation.Criteria.Title"),Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorAtLeastOne"));
			return false;
		}

		Integer I=NumberTools.getNotNegativeInteger(clientCount,true);
		if (I==null) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorClients"),clientCount.getText()));
			return false;
		}
		Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (D==null) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorWarmUp"),warmUpTime.getText()));
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

		final boolean seedOk=checkFixedSeed();
		if (!seedOk && useFixedSeed.isSelected()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.Error"),fixedSeed.getText()));
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
		model.useClientCount=terminationByClientClount.isSelected();
		Long L=NumberTools.getNotNegativeLong(clientCount,true);
		if (L!=null) model.clientCount=L;
		Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (D!=null) model.warmUpTime=D;
		model.useTerminationCondition=terminationByCondition.isSelected();
		model.terminationCondition=terminationCondition.getText();
		model.useFinishTime=terminationByTime.isSelected();
		Integer I=TimeTools.getTime(terminationTime,true);
		if (I==null) model.finishTime=10*86400; else model.finishTime=I;
		model.useFixedSeed=useFixedSeed.isSelected();
		L=NumberTools.getLong(fixedSeed,true);
		if (L!=null) model.fixedSeed=L;
		L=NumberTools.getPositiveLong(repeatCount,true);
		if (L!=null) model.repeatCount=(int)L.longValue();
		model.stoppOnCalcError=stoppOnCalcError.isSelected();
		if (useTimedChecks.isSelected()) {
			D=NumberTools.getPositiveDouble(editTimedChecks,true);
			if (D!=null) model.timedChecksDelta=(int)Math.round(D.doubleValue()*1000);
		} else {
			model.timedChecksDelta=-1;
		}
		model.recordIncompleteClients=recordIncompleteClients.isSelected();
	}

	/**
	 * Liefert die eingestellte Anzahl an zu simulierenden Kunden.
	 * @return	Anzahl an zu simulierenden Kunden (kann -1 sein, wenn keine Zahl ermittelt werden konnte oder aber wenn die Anzahl an Ankünften kein Abbruchkriterium ist)
	 */
	public long getTerminationClientCount() {
		if (!terminationByClientClount.isSelected()) return -1;

		Long L=NumberTools.getNotNegativeLong(clientCount,true);
		if (L==null) return -1;
		return L.longValue();
	}
}
