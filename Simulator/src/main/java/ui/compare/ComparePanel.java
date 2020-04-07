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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.ModelViewerFrame;
import ui.help.Help;
import ui.images.Images;
import ui.statistics.StatisticsPanel;
import ui.tools.SpecialPanel;

/**
 * Ermöglicht den Vergleich von Statistik-Dateien
 * @author Alexander Herzog
 */
public class ComparePanel extends SpecialPanel {
	private static final long serialVersionUID = 1696555531378569922L;

	private final Window owner;
	private final boolean allowLoadToEditor;
	private final Statistics[] statistic;
	private final String[] titleArray;
	private final JButton showModelButton;
	private final JButton helpButton;
	private EditModel loadModelIntoEditor=null;

	/**
	 * Konstruktor der Klasse <code>ComparePanel</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param statistic	Anzuzeigende Statistikdateien
	 * @param title	Zusätzliche Überschriften über den Statistikdateien
	 * @param allowLoadToEditor	Gibt an, ob angeboten werden soll, die zugehörigen Modelle in den Editor zu laden
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schließen möchte
	 */
	public ComparePanel(Window owner, Statistics[] statistic, String[] title, boolean allowLoadToEditor, Runnable doneNotify) {
		super(doneNotify);

		this.owner=owner;
		this.statistic=statistic;
		titleArray=new String[statistic.length];
		for (int i=0;i<titleArray.length;i++) {
			if (title==null || title.length<=i || title[i]==null) titleArray[i]=(statistic[i]==null || statistic[i].editModel==null)?"":statistic[i].editModel.name; else titleArray[i]=title[i];
		}

		this.allowLoadToEditor=allowLoadToEditor;
		StatisticsPanel statisticPanel=new StatisticsPanel(statistic.length);
		statisticPanel.setStatistics(statistic,titleArray);
		add(statisticPanel,BorderLayout.CENTER);

		addCloseButton();
		addSeparator();
		showModelButton=addUserButton(Language.tr("Main.Toolbar.ShowModelForTheseResults2"),Language.tr("Main.Toolbar.ShowModelForTheseResults2.Hint"),Images.MODEL.getURL());
		addSeparator();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getURL());

		/* F1-Hotkey */
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID = 1738622101739292954L;
			@Override public void actionPerformed(ActionEvent event) {Help.topicModal(ComparePanel.this,"Compare");}
		});
	}

	/**
	 * Lädt die angegebenen Statistikdateien in Statistikobjekte
	 * @param statisticFiles	Array der Statistikdateien
	 * @return	Array der Statistikobjekte; lässt sich eine Datei nicht laden, so wird die Verarbeitung abgebrochen und an der entsprechenden Stelle im Array ein <code>null</code> zurückgegeben.
	 */
	public static final Statistics[] getStatisticFiles(File[] statisticFiles) {
		Statistics[] statistic=new Statistics[statisticFiles.length];
		Arrays.fill(statistic,null);
		for (int i=0;i<statisticFiles.length;i++) {
			Statistics data=new Statistics();
			String s=data.loadFromFile(statisticFiles[i]); if (s!=null) break;
			statistic[i]=data;
		}
		return statistic;
	}

	@Override
	protected void userButtonClick(int index, JButton button) {
		if (button==showModelButton) {
			JPopupMenu popupMenu=new JPopupMenu();
			for (int i=0;i<statistic.length;i++) {
				final Statistics statisticData=statistic[i];
				if (statisticData==null || statisticData.editModel==null) continue;
				JMenuItem item=new JMenuItem(titleArray[i]);
				item.addActionListener((e)->{
					Runnable loadToEditor=null;
					if (allowLoadToEditor) {
						loadToEditor=()->{loadModelIntoEditor=statisticData.editModel; close();};
					}
					ModelViewerFrame modelViewer=new ModelViewerFrame(owner,statisticData.editModel,statisticData,loadToEditor);
					modelViewer.setVisible(true);
				});
				popupMenu.add(item);
			}
			popupMenu.show(ComparePanel.this,button.getX(),button.getY()+button.getHeight());
			return;
		}
		if (button==helpButton) {
			Help.topicModal(ComparePanel.this,"Compare");
			return;
		}
	}

	/**
	 * Gibt an, ob beim Schließen der Vergleichsansicht ein Modell in den Editor geladen werden soll.
	 * @return	Zu ladendes Modell oder <code>null</code> wenn nichts geladen werden soll.
	 */
	public EditModel getModelForEditor() {
		return loadModelIntoEditor;
	}
}