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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private static final long serialVersionUID = 2493379995138787054L;

	private final Runnable help;

	private ModelSchedule originalSchedule;
	private int durationPerSlot;
	private int editorMaxY;
	private ModelSchedule.RepeatMode repeatMode;

	private final JButton buttonHome;
	private final JButton buttonLeft;
	private final JButton buttonRight;
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

		addUserButton(Language.tr("Schedule.EditDialog.Settings"),Images.GENERAL_SETUP.getURL());
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JToolBar toolBar=new JToolBar();
		toolBar.setFloatable(false);
		content.add(toolBar,BorderLayout.NORTH);
		toolBar.add(buttonHome=new JButton(Language.tr("Schedule.EditDialog.ToTheStart")));
		buttonHome.setToolTipText(Language.tr("Schedule.EditDialog.ToTheStart.Hint"));
		buttonHome.addActionListener(new ToolBarButtonListener());
		buttonHome.setIcon(Images.GENERAL_HOME.getIcon());
		toolBar.addSeparator();
		toolBar.add(buttonLeft=new JButton(Language.tr("Schedule.EditDialog.TimeStepBack")));
		buttonLeft.setToolTipText(Language.tr("Schedule.EditDialog.TimeStepBack.Hint"));
		buttonLeft.addActionListener(new ToolBarButtonListener());
		buttonLeft.setIcon(Images.ARROW_LEFT.getIcon());
		toolBar.add(buttonRight=new JButton(Language.tr("Schedule.EditDialog.TimeStepFurther")));
		buttonRight.setToolTipText(Language.tr("Schedule.EditDialog.TimeStepFurther.Hint"));
		buttonRight.addActionListener(new ToolBarButtonListener());
		buttonRight.setIcon(Images.ARROW_RIGHT.getIcon());

		content.add(schedulePanel=new SchedulePanel(schedule.getSlots(),editorMaxY,durationPerSlot,20),BorderLayout.CENTER);

		enableButtons();
	}

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

	private class ToolBarButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object sender=e.getSource();

			if (sender==buttonHome) {
				schedulePanel.setStartPosition(0);
				enableButtons();
				return;
			}

			if (sender==buttonLeft) {
				schedulePanel.setStartPosition(schedulePanel.getStartPosition()-1);
				enableButtons();
				return;
			}

			if (sender==buttonRight) {
				schedulePanel.setStartPosition(schedulePanel.getStartPosition()+1);
				enableButtons();
				return;
			}
		}
	}
}