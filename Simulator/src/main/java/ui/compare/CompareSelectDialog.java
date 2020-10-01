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
package ui.compare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import xml.XMLTools;

/**
 * Ermöglicht die Auswahl von mehreren Statistik-Dateien, die im Folgenden verglichen werden sollen.
 * @author Alexander Herzog
 */
public class CompareSelectDialog extends BaseDialog  {
	private static final long serialVersionUID = -5204836029062935247L;

	/**
	 * Textfelder zur Eingabe der Dateinamen
	 */
	private final JTextField[] statisticTextFields;

	/**
	 * Schaltflächen zur Auswahl der Dateien über Dialoge
	 */
	private final JButton[] statisticButton;

	/**
	 * Gewählte Dateien
	 * @see #storeData()
	 * @see #getSelectedFiles()
	 */
	private File[] statisticFiles;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param numberOfFiles	Anzahl an Eingabefelder die zum Auswählen von Statistikdateien angeboten werden sollen
	 */
	public CompareSelectDialog(Window owner, int numberOfFiles) {
		super(owner,Language.tr("Compare.Title"));

		statisticTextFields=new JTextField[numberOfFiles];
		statisticButton=new JButton[numberOfFiles];

		final JPanel contentOuter=createGUI(500,400,()->Help.topicModal(CompareSelectDialog.this,"Compare"));
		contentOuter.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		final JPanel infoPanel=InfoPanel.addTopPanel(contentOuter,InfoPanel.globlCompare);
		contentOuter.add(content,BorderLayout.CENTER);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel p;

		if (statisticTextFields.length>2) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel(Language.tr("Compare.ErrorAtLeastTwoModels")));
		}

		for (int i=0;i<statisticTextFields.length;i++) {
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(new JLabel(Language.tr("Compare.StatisticFile")+" "+(i+1)));
			content.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			p.add(statisticTextFields[i]=new JTextField(50));
			p.add(statisticButton[i]=new JButton(Language.tr("Compare.SelectStatisticFile"),Images.STATISTICS_LOAD.getIcon()));
			statisticButton[i].setToolTipText(Language.tr("Compare.SelectStatisticFile.Info"));
			statisticButton[i].addActionListener(e->selectButtonClick((JButton)e.getSource()));
		}

		new FileDropper(this,e->{
			final FileDropperData data=(FileDropperData)e.getSource();
			if (dropFile(data.getFile())) data.dragDropConsumed();
		});

		pack();

		SwingUtilities.invokeLater(()->{
			if (infoPanel!=null) infoPanel.setPreferredSize(infoPanel.getSize());
			pack();
		});

		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert die Liste der ausgewählten Statistikdateien
	 * @return	Liste der ausgewählten Statistikdateien (die Liste kann leer sein, aber ist selbst nie <code>null</code>)
	 */
	public File[] getSelectedFiles() {
		if (statisticFiles==null) return new File[0];
		List<File> files=new ArrayList<>();
		for (File file : statisticFiles) if (file!=null) files.add(file);
		return files.toArray(new File[0]);
	}

	@Override
	protected boolean checkData() {
		List<File> files=new ArrayList<>();
		List<Integer> nr=new ArrayList<>();

		for (int i=0;i<statisticTextFields.length;i++) {
			String s=statisticTextFields[i].getText().trim();
			if (s.isEmpty()) continue;
			File file=new File(s);
			if (!file.exists()) {
				MsgBox.error(this,String.format(Language.tr("Compare.FileDoesNotExist.Title"),""+(i+1)),String.format(Language.tr("Compare.FileDoesNotExist.Info"),""+(i+1),file.toString()));
				return false;
			}
			for (int j=0;j<files.size();j++) if (file.equals(files.get(j))) {
				MsgBox.error(this,Language.tr("Compare.DoubleFile.Title"),String.format(Language.tr("Compare.DoubleFile.Info"),""+(i+1),""+(nr.get(j)+1),file.toString()));
				return false;
			}
			files.add(file);
			nr.add(i);
		}

		if (files.size()<2) {
			String s=(statisticTextFields.length==2)?Language.tr("Compare.TooFewModels.InfoTwo"):Language.tr("Compare.TooFewModels.InfoAtLeastTwo");
			MsgBox.error(this,Language.tr("Compare.TooFewModels.Title"),s);
			return false;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see tools.ui.BaseDialog#storeData()
	 */
	@Override
	protected void storeData() {
		List<File> files=new ArrayList<>();
		for (int i=0;i<statisticTextFields.length;i++) {
			String s=statisticTextFields[i].getText().trim();
			if (s.isEmpty()) continue;
			files.add(new File(s));
		}
		statisticFiles=files.toArray(new File[0]);
	}

	/**
	 * Ruft den Dialog zur Auswahl einer Statistikdatei auf
	 * @param initialFolder	Anfänglich zu selektierender Ordner
	 * @return	Dateiname oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 */
	private File selectFile(final File initialFolder) {

		File file=XMLTools.showLoadDialog(getParent(),Language.tr("Compare.LoadStatisticData"),initialFolder);
		if (file==null) return null;

		Statistics newData=new Statistics();
		String s=newData.loadFromFile(file); if (s!=null) {
			MsgBox.error(this,Language.tr("Compare.InvalidStatisticFile.Title"),String.format(Language.tr("Compare.InvalidStatisticFile.Info"),file.toString()));
			return null;
		}

		return file;
	}

	/**
	 * Reaktion auf einen Klick auf eine Statistikauswahl-Schaltfläche
	 * @param button	Angeklickte Schaltfläche
	 */
	private void selectButtonClick(final JButton button) {
		for (int i=0;i<statisticButton.length;i++) if (button==statisticButton[i]) {
			File initialFile=null;
			for (int j=i;j>=0;j--) {
				String s=statisticTextFields[j].getText().trim();
				if (s!=null && !s.isEmpty()) {
					final File f=new File(s);
					if (f.exists()) initialFile=f;
					break;
				}
			}
			File newFile=selectFile((initialFile==null)?null:initialFile.getParentFile());
			if (newFile!=null) statisticTextFields[i].setText(newFile.toString());
			return;
		}
	}

	/**
	 * Index der Eingabezeile in die die letzte Drag&amp;Drop-Operation erfolgte
	 * @see #dropFile(File)
	 */
	private int lastDrop=-1;

	/**
	 * Reagiert auf Drag&amp;Drop einer Datei auf den Dialog
	 * @param file	Datei, die übermittelt wirde
	 * @return	Gibt an, ob die Datei erfolgreich in die Liste der zu vergleichenden Statistikdateien aufgenommen werden konnte
	 */
	private final boolean dropFile(File file) {
		if (!file.exists()) return false;

		Statistics newData=new Statistics();
		String s=newData.loadFromFile(file); if (s!=null) {
			MsgBox.error(this,Language.tr("Compare.InvalidStatisticFile.Title"),String.format(Language.tr("Compare.InvalidStatisticFile.Info"),file.toString()));
			return false;
		}

		int nextFree=-1;
		for (int i=0;i<statisticTextFields.length;i++) if (statisticTextFields[i].getText().trim().isEmpty()) {nextFree=i; break;}
		if (nextFree==-1) {
			if (lastDrop==-1) nextFree=0; else {
				if (lastDrop==statisticTextFields.length-1) nextFree=0; else nextFree=lastDrop+1;
			}
		}
		lastDrop=nextFree;

		statisticTextFields[nextFree].setText(file.toString());
		return true;
	}
}
