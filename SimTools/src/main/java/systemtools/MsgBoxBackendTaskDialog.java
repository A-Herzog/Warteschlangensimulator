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
package systemtools;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.oxbow.swingbits.dialog.task.CommandLink;
import org.oxbow.swingbits.dialog.task.TaskDialog;
import org.oxbow.swingbits.dialog.task.TaskDialogs;

import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt ein Backend für {@link MsgBox}-Dialoge dar
 * und leitet diese auf <code>TaskDialog</code>-Dialoge um.
 * @author Alexander Herzog
 * @see MsgBox
 */
public class MsgBoxBackendTaskDialog implements MsgBoxBackend {
	/**
	 * Liefert das zugehörige Fenster zu einer Komponente
	 * @param component	Komponente zu der das zugehörige Fenster geliefert werden soll
	 * @return	Fenster oder <code>null</code>, wenn kein passendes Fenster gefunden werden konnte
	 */
	private static Window getWindow(Component component) {
		while (component!=null && !(component instanceof Window)) component=component.getParent();
		return (Window)component;
	}

	@Override
	public void info(Component parentComponent, String title, String message) {
		final TaskDialog dialog=new TaskDialog(getWindow(parentComponent),MsgBox.TitleInformation);
		dialog.setInstruction(title);
		dialog.setText(message);
		dialog.setCommands(TaskDialog.StandardCommand.OK);
		dialog.setIcon(TaskDialog.StandardIcon.INFO);
		dialog.setLocale(MsgBox.ActiveLocale);
		dialog.show();
	}

	@Override
	public void warning(Component parentComponent, String title, String message) {
		final TaskDialog dialog=new TaskDialog(getWindow(parentComponent),MsgBox.TitleWarning);
		dialog.setInstruction(title);
		dialog.setText(message);
		dialog.setCommands(TaskDialog.StandardCommand.OK);
		dialog.setIcon(TaskDialog.StandardIcon.WARNING);
		dialog.setLocale(MsgBox.ActiveLocale);
		dialog.show();
	}

	@Override
	public void error(Component parentComponent, String title, String message) {
		final TaskDialog dialog=new TaskDialog(getWindow(parentComponent),MsgBox.TitleError);
		dialog.setInstruction(title);
		dialog.setText(message);
		dialog.setCommands(TaskDialog.StandardCommand.OK);
		dialog.setIcon(TaskDialog.StandardIcon.ERROR);
		dialog.setLocale(MsgBox.ActiveLocale);
		dialog.show();
	}

	@Override
	public int confirm(Component parentComponent, String title, String message, String infoYes, String infoNo, String infoCancel) {
		final int wahl=TaskDialogs.choice(getWindow(parentComponent),title,message,0,
				new CommandLink(SimToolsImages.MSGBOX_YES.getIcon(),MsgBox.OptionYes,infoYes),
				new CommandLink(SimToolsImages.MSGBOX_NO.getIcon(),MsgBox.OptionNo,infoNo),
				new CommandLink(SimToolsImages.MSGBOX_CANCEL.getIcon(),MsgBox.OptionCancel,infoCancel));
		switch (wahl) {
		case 0: return JOptionPane.YES_OPTION;
		case 1: return JOptionPane.NO_OPTION;
		case 2: return JOptionPane.CANCEL_OPTION;
		default: return JOptionPane.CANCEL_OPTION;
		}
	}

	@Override
	public int confirmSave(Component parentComponent, String title, String message) {
		final int wahl=TaskDialogs.choice(getWindow(parentComponent),title,message,0,
				new CommandLink(SimToolsImages.MSGBOX_YES_SAVE.getIcon(),MsgBox.OptionSaveYes,MsgBox.OptionSaveYesInfo),
				new CommandLink(SimToolsImages.MSGBOX_NO.getIcon(),MsgBox.OptionSaveNo,MsgBox.OptionSaveNoInfo),
				new CommandLink(SimToolsImages.MSGBOX_CANCEL.getIcon(),MsgBox.OptionCancel,MsgBox.OptionSaveCancelInfo));
		switch (wahl) {
		case 0: return JOptionPane.YES_OPTION;
		case 1: return JOptionPane.NO_OPTION;
		case 2: return JOptionPane.CANCEL_OPTION;
		default: return JOptionPane.CANCEL_OPTION;
		}
	}

	@Override
	public boolean confirm(Component parentComponent, String title, String message, String infoYes, String infoNo) {
		final int wahl=TaskDialogs.choice(getWindow(parentComponent),title,message,0,
				new CommandLink(SimToolsImages.MSGBOX_YES.getIcon(),MsgBox.OptionYes,infoYes),
				new CommandLink(SimToolsImages.MSGBOX_NO.getIcon(),MsgBox.OptionNo,infoNo));
		switch (wahl) {
		case 0: return true;
		case 1: return false;
		default: return false;
		}
	}

	@Override
	public boolean confirmOverwrite(Component parentComponent, File file) {
		final int wahl=TaskDialogs.choice(getWindow(parentComponent),MsgBox.OverwriteTitle,String.format(MsgBox.OverwriteInfo,file.toString()),0,
				new CommandLink(SimToolsImages.MSGBOX_YES_SAVE.getIcon(),MsgBox.OverwriteYes,MsgBox.OverwriteYesInfo),
				new CommandLink(SimToolsImages.MSGBOX_NO.getIcon(),MsgBox.OverwriteNo,MsgBox.OverwriteNoInfo));
		return (wahl==0);
	}

	@Override
	public int options(Component parentComponent, String title, String message, String[] options, String[] info) {
		final List<CommandLink> commands=new ArrayList<>();

		if (options==null || options.length==0) return 0;
		for (int i=0;i<options.length;i++) commands.add(new CommandLink((options[i]==null)?"":options[i],(info==null || info.length<=i || info[i]==null)?"":info[i]));

		return TaskDialogs.choice(getWindow(parentComponent),title,message,0,commands);
	}
}