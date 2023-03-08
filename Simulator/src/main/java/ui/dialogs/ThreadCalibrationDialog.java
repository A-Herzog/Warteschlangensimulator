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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.datatransfer.StringSelection;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import language.Language;
import mathtools.Table;
import mathtools.distribution.swing.SimSystemsSwingImages;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.images.Images;

/**
 * Dialog zur Ermittelt die Simulationsleistung in Abhängigkeit von der Anzahl der eingesetzten Threads.
 * @author Alexander Herzog
 * @see ThreadCalibration
 */
public class ThreadCalibrationDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5787892997864139530L;

	/**
	 * Zu verwendendes Modell (kann <code>null</code> sein, dann wird das eingebaute Callcenter-Beispielmodell verwendet)
	 */
	private final EditModel model;

	/**
	 * Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	private final String editModelPath;

	/**
	 * Auswahlbox: Art des Modells
	 */
	private final JComboBox<String> selectModel;

	/**
	 * Auswahlbox: Ziellaufzeit
	 */
	private final JComboBox<String> selectRuntime;

	/**
	 * Ausgabefeld
	 */
	private final JTextArea output;

	/**
	 * Fortschrittsbalken für den Kalibrierungsprozess
	 */
	private final JProgressBar progress;

	/**
	 * Timer zur Aktualisierung des Fortschrittsbalkens
	 * @see #setRunMode(boolean)
	 * @see #progress
	 * @see ThreadCalibration#getProgress()
	 */
	private Timer timer;

	/**
	 * Schaltfläche zum Starten/Abbrechen der Simulation
	 */
	private final JButton buttonStartCancel;

	/**
	 * Schaltfläche zum Kopieren der Ergebnisse
	 */
	private final JButton buttonCopy;

	/**
	 * Internes Arbeitssystem
	 */
	private ThreadCalibration threadCalibration;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Zu verwendendes Modell (kann <code>null</code> sein, dann wird das eingebaute Callcenter-Beispielmodell verwendet)
	 * @param editModelPath	Pfad zur zugehörigen Modelldatei (als Basis für relative Pfade in Ausgabeelementen)
	 */
	public ThreadCalibrationDialog(final Component owner, final EditModel model, final String editModelPath) {
		super(owner,Language.tr("ThreadCalibration.Dialog.Title"));
		this.model=model;
		this.editModelPath=editModelPath;

		/* GUI */
		addUserButton(Language.tr("ThreadCalibration.Dialog.Start"),Images.SIMULATION.getIcon());
		addUserButton(Language.tr("ThreadCalibration.Dialog.CopyResults"),Images.EDIT_COPY.getIcon());
		buttonStartCancel=getUserButton(0);
		buttonCopy=getUserButton(1);
		buttonCopy.setEnabled(false);
		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		JPanel line;
		JLabel label;

		/* Setup-Bereich */
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		if (model==null) {
			selectModel=null;
		} else {
			setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(label=new JLabel(Language.tr("ThreadCalibration.Dialog.ModelToUse")+":"));
			line.add(selectModel=new JComboBox<>(new String[] {
					Language.tr("ThreadCalibration.Dialog.ModelToUse.CallcenterExampleModel"),
					Language.tr("ThreadCalibration.Dialog.ModelToUse.CurrentModelInEditor")
			}));
			selectModel.setSelectedIndex(0);
			label.setLabelFor(selectModel);
		}

		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("ThreadCalibration.Dialog.RunTime")+":"));
		line.add(selectRuntime=new JComboBox<>(calculateRunTimes()));
		selectRuntime.setSelectedIndex(4);
		label.setLabelFor(selectRuntime);
		line.add(Box.createHorizontalStrut(5));
		line.add(new JLabel(Language.tr("ThreadCalibration.Dialog.RunTime.Info")));

		/* Ausgabebereich */
		content.add(new JScrollPane(output=new JTextArea()),BorderLayout.CENTER);

		/* Fortschrittsbalken */
		content.add(line=new JPanel(new BorderLayout()),BorderLayout.SOUTH);
		line.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
		line.add(progress=new JProgressBar(SwingConstants.HORIZONTAL,0,100),BorderLayout.CENTER);
		progress.setStringPainted(true);

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erstellt die Liste der geschätzten Gesamtlaufzeiten für
	 * Individuallaufzeiten von 1 bis 10 Sekunden.
	 * @return	Geschätzte Gesamtlaufzeiten als Auswahloptionen
	 * @see #selectRuntime
	 */
	private String[] calculateRunTimes() {
		final String[] result=new String[10];

		final int coreCount=ThreadCalibration.coreCount;
		final int baseTime=coreCount*(coreCount+1)/2+1;

		for (int i=0;i<result.length;i++) {
			final int seconds=baseTime*(i+1);
			if (seconds<60) {
				result[i]=seconds+" "+Language.tr("Statistics.Seconds");
			} else {
				final int minutes=seconds/60;
				if (minutes<60) {
					result[i]=minutes+" "+Language.tr("Statistics.Minutes");
				} else {
					result[i]=(minutes/60)+" "+Language.tr("Statistics.Hours");
				}
			}
		}

		return result;
	}

	/**
	 * Konfiguriert die Schaltflächen in Abhängigkeit vom Status der Thread-Kalibrierung.
	 * @param isRunning	Läuft die Verarbeitung gerade?
	 */
	private void setRunMode(final boolean isRunning) {
		if (isRunning) {
			output.setText("");
			buttonCopy.setEnabled(false);
			buttonStartCancel.setText(Language.tr("Dialog.Button.Abort"));
			buttonStartCancel.setIcon(SimSystemsSwingImages.CANCEL.getIcon());
			progress.setValue(0);

			timer=new Timer("CalibrationProgressUpdate",true);
			timer.schedule(new TimerTask() {
				@Override public void run() {if (threadCalibration!=null && threadCalibration.isRunning()) progress.setValue((int)(threadCalibration.getProgress()*100));}
			},2_000,2_000);

		} else {
			buttonStartCancel.setText(Language.tr("ThreadCalibration.Dialog.Start"));
			buttonStartCancel.setIcon(Images.SIMULATION.getIcon());
			progress.setValue(0);
			if (timer!=null) timer.cancel();
			buttonCopy.setEnabled(threadCalibration!=null && threadCalibration.getPerformance()!=null);
		}
	}

	@Override
	public void setVisible(boolean b) {
		if (!b) {
			if (threadCalibration!=null && threadCalibration.isRunning()) threadCalibration.cancel();
			if (timer!=null) timer.cancel();
		}
		super.setVisible(b);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (nr==0) {
			/* Run/Pause */
			if (threadCalibration==null || !threadCalibration.isRunning()) {
				threadCalibration=new ThreadCalibration(
						(selectModel==null || selectModel.getSelectedIndex()==0)?null:model,editModelPath,
								line->{
									output.setText(output.getText()+line+"\n");
									output.setCaretPosition(output.getDocument().getLength());
								},
								selectRuntime.getSelectedIndex()+1);
				setRunMode(true);
				threadCalibration.start(()->setRunMode(false));
			} else {
				threadCalibration.cancel();
				setRunMode(false);
			}
			return;
		}

		if (nr==1) {
			/* Kopieren */
			if (threadCalibration!=null) {
				final Table table=threadCalibration.getPerformanceTable();
				if (table==null) return;
				getToolkit().getSystemClipboard().setContents(new StringSelection(table.toString()),null);
			}
			return;
		}
	}
}
