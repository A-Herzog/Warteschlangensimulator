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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;

/**
 * Zeigt aktuelle Animations-/Simulationsdaten zu einer bestimmten Station an
 * @author Alexander Herzog
 * @see ModelElement
 */
public class ModelElementAnimationInfoDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2428681388058860543L;

	/** Timer für automatische Aktualisierungen */
	private Timer timer=null;
	/** Anzuzeigender Text im Content-Bereich */
	private final Supplier<String> info;
	/** Ausgabebereich */
	private final JTextArea textArea;
	/** Schaltfläche zum Umschalten zwischen automatischer und manueller Aktualisierung */
	private final JButton buttonAutoUpdate;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationInfoDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Anzuzeigender Titel
	 * @param info	Anzuzeigender Text im Content-Bereich
	 */
	public ModelElementAnimationInfoDialog(final Component owner, final String title, final Supplier<String> info) {
		super(owner,title);
		this.info=info;

		timer=null;

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(ModelElementAnimationInfoDialog.this.owner,"AnimationStatistics"));
		content.setLayout(new BorderLayout());

		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		addButton(toolbar,Language.tr("Dialog.Button.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.CopyHint"),e->commandCopy());
		addButton(toolbar,Language.tr("Dialog.Button.Save"),Images.GENERAL_SAVE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveHint"),e->commandSave());
		toolbar.addSeparator();
		addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
		buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());

		textArea=new JTextArea(info.get());
		content.add(new JScrollPane(textArea),BorderLayout.CENTER);
		textArea.setEditable(false);

		setMinSizeRespectingScreensize(400,300);
		setSizeRespectingScreensize(800,600);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	private JButton addButton(final JToolBar toolbar, final String name, final Icon icon, final String hint, final ActionListener listener) {
		final JButton button=new JButton(name);
		if (icon!=null) button.setIcon(icon);
		if (hint!=null && !hint.trim().isEmpty()) button.setToolTipText(hint);
		button.addActionListener(listener);
		toolbar.add(button);
		return button;
	}

	private void commandCopy() {
		buttonAutoUpdate.setSelected(false);
		if (timer!=null) timer.cancel();

		getToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()),null);
	}

	private void commandSave() {
		buttonAutoUpdate.setSelected(false);
		if (timer!=null) timer.cancel();

		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter filter;
		fc.setDialogTitle(Language.tr("FileType.Save.Text"));
		filter=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".txt");
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		try {
			if (file.isFile()) {
				if (!file.delete()) {
					MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
					return;
				}
			}
			Files.write(Paths.get(file.toURI()),textArea.getText().getBytes(),StandardOpenOption.CREATE_NEW);
		} catch (IOException e1) {
			MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
		}
	}

	private void commandUpdate() {
		textArea.setText(info.get());
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);
	}

	private void commandAutoUpdate() {
		buttonAutoUpdate.setSelected(!buttonAutoUpdate.isSelected());

		if (buttonAutoUpdate.isSelected()) {
			timer=new Timer();
			timer.schedule(new TimerTask() {
				@Override public synchronized void run() {if (buttonAutoUpdate.isSelected()) commandUpdate();}
			},100,250);
		} else {
			if (timer!=null) {timer.cancel(); timer=null;}
		}
	}

	@Override
	protected boolean closeButtonOK() {
		if (timer!=null) {timer.cancel(); timer=null;}
		return true;
	}
}
