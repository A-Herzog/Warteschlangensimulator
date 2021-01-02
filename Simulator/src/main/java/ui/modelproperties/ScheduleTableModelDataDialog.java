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
import java.awt.Component;
import java.io.Serializable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import language.Language;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.modeleditor.ModelSchedule;

/**
 * Dieser Dialog erlaubt das grafische Bearbeiten eines Zeitplans.
 * @author Alexander Herzog
 * @see ModelSchedule
 * @see ScheduleTableModelDataDialog
 */
public class ScheduleTableModelDataDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2493379995138787054L;

	/** Hilfe-Callback */
	private final Runnable help;

	/** Im Konstruktor übergebenes {@link ModelSchedule}-Objekt in das beim Schließen des Dialogs die Daten zurückgeschrieben werden */
	private ModelSchedule originalSchedule;
	/** Anzahl an Sekunden, die ein Zeitslot dauern soll */
	private int durationPerSlot;
	/**  Maximaler y-Achsen-Wert im Editor */
	private int editorMaxY;
	/**  Wie soll der Plan nach seinem Ende fortgesetzt werden? */
	private ModelSchedule.RepeatMode repeatMode;

	/** Schaltfläche "An den Anfang" */
	private final JButton buttonHome;
	/** Schaltfläche "Zurück" */
	private final JButton buttonLeft;
	/** Schaltfläche "Weiter" */
	private final JButton buttonRight;
	/** Panel in dem die Zeitslots des Zeitplans als Balken dargestellt werden */
	private final SchedulePanel schedulePanel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param schedule	Zu bearbeitender Zeitplan
	 */
	public ScheduleTableModelDataDialog(final Component owner, final Runnable help, final ModelSchedule schedule) {
		super(owner,String.format(Language.tr("Schedule.EditDialog.Title"),schedule.getName()));
		this.help=help;
		originalSchedule=schedule;
		durationPerSlot=schedule.getDurationPerSlot();
		editorMaxY=schedule.getEditorMaxY();
		repeatMode=schedule.getRepeatMode();

		addUserButton(Language.tr("Schedule.EditDialog.Settings"),Images.GENERAL_SETUP.getIcon());
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		content.add(schedulePanel=new SchedulePanel(schedule.getSlots(),editorMaxY,durationPerSlot,20),BorderLayout.CENTER);

		final JToolBar toolBar=new JToolBar();
		toolBar.setFloatable(false);
		content.add(toolBar,BorderLayout.NORTH);
		toolBar.add(buttonHome=new JButton(Language.tr("Schedule.EditDialog.ToTheStart")));
		buttonHome.setToolTipText(Language.tr("Schedule.EditDialog.ToTheStart.Hint"));
		buttonHome.addActionListener(e->{
			schedulePanel.setStartPosition(0);
			enableButtons();
		});
		buttonHome.setIcon(Images.GENERAL_HOME.getIcon());
		toolBar.addSeparator();
		toolBar.add(buttonLeft=new JButton(Language.tr("Schedule.EditDialog.TimeStepBack")));
		buttonLeft.setToolTipText(Language.tr("Schedule.EditDialog.TimeStepBack.Hint"));
		buttonLeft.addActionListener(e->{
			schedulePanel.setStartPosition(schedulePanel.getStartPosition()-1);
			enableButtons();
		});
		buttonLeft.setIcon(Images.ARROW_LEFT_SHORT.getIcon());
		toolBar.add(buttonRight=new JButton(Language.tr("Schedule.EditDialog.TimeStepFurther")));
		buttonRight.setToolTipText(Language.tr("Schedule.EditDialog.TimeStepFurther.Hint"));
		buttonRight.addActionListener(e->{
			schedulePanel.setStartPosition(schedulePanel.getStartPosition()+1);
			enableButtons();
		});
		buttonRight.setIcon(Images.ARROW_RIGHT_SHORT.getIcon());

		enableButtons();
	}

	/**
	 * Aktiviert oder deaktiviert {@link #buttonHome} und {@link #buttonLeft}
	 * in Abhängigkeit davon, welcher Balken gerade ganz links in dem Diagramm angezeigt wird.
	 * @see #buttonHome
	 * @see #buttonLeft
	 */
	private void enableButtons() {
		buttonHome.setEnabled(schedulePanel.getStartPosition()>0);
		buttonLeft.setEnabled(schedulePanel.getStartPosition()>0);
	}

	@Override
	protected void storeData() {
		originalSchedule.setDurationPerSlot(durationPerSlot);
		originalSchedule.setEditorMaxY(editorMaxY);
		originalSchedule.setRepeatMode(repeatMode);

		final List<Integer> data=schedulePanel.getData();
		while (data.size()>1 && data.get(data.size()-1)==0) data.remove(data.size()-1);
		originalSchedule.setSlots(data);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		final ScheduleTableModelSetupDialog dialog=new ScheduleTableModelSetupDialog(this,help,durationPerSlot,editorMaxY,repeatMode);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		durationPerSlot=dialog.getDurationPerSlot();
		editorMaxY=dialog.getEditorMaxY();
		repeatMode=dialog.getRepeatMode();
		schedulePanel.setSetupData(editorMaxY,durationPerSlot);
	}
}