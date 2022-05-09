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
import java.io.File;

import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 * Diese Klasse stellt eine Implementierung für <code>MsgBoxBackend</code> dar und wird
 * in <code>MsgBox</code> als Standard-Backend zur Anzeige von Meldungsdialogen verwendet.
 * Die Klasse verwendet intern <code>JOptionPane</code> zur Anzeige von Dialogen.
 * @author Alexander Herzog
 * @see MsgBoxBackend
 * @see MsgBox
 * @version 1.2
 */
public class MsgBoxBackendJOptionPane implements MsgBoxBackend {
	/**
	 * Konstruktor der Klasse
	 */
	public MsgBoxBackendJOptionPane() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void info(final Component parentComponent, final String title, String message) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		JOptionPane.showMessageDialog(parentComponent,message,title,JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void warning(final Component parentComponent, final String title, String message) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		JOptionPane.showMessageDialog(parentComponent,message,title,JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void error(final Component parentComponent, final String title, String message) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		JOptionPane.showMessageDialog(parentComponent,message,title,JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public int confirm(final Component parentComponent, final String title, String message, final String infoYes, final String infoNo, final String infoCancel) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		return JOptionPane.showConfirmDialog(parentComponent,message,title,JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
	}

	@Override
	public int confirmSave(final Component parentComponent, final String title, String message) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		return JOptionPane.showConfirmDialog(parentComponent,message,title,JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
	}

	@Override
	public boolean confirm(final Component parentComponent, final String title, String message, final String infoYes, final String infoNo) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		return JOptionPane.showConfirmDialog(parentComponent,message,title,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION;
	}

	@Override
	public boolean confirmOverwrite(final Component parentComponent, final File file) {
		return JOptionPane.showConfirmDialog(parentComponent,String.format(MsgBox.OverwriteInfo,file.toString()),MsgBox.TitleWarning,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION;
	}

	@Override
	public int options(final Component parentComponent, final String title, String message, final String[] options, final String[] info, final Icon[] icons) {
		if (message.toLowerCase().indexOf("<html>")>=0) message=message.replaceAll("\\n"," ");
		final int result=JOptionPane.showOptionDialog(parentComponent,message,title,JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,null);
		if (result==JOptionPane.CLOSED_OPTION) return -1;
		return result;
	}
}
