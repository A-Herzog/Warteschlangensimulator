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
import java.io.Serializable;
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
 * Erm�glicht den Vergleich von Statistik-Dateien
 * @author Alexander Herzog
 */
public class ComparePanel extends SpecialPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1696555531378569922L;

	/** �bergeordnetes Fenster */
	private final Window owner;
	/** Gibt an, ob angeboten werden soll, die zugeh�rigen Modelle in den Editor zu laden */
	private final boolean allowLoadToEditor;
	/** Anzuzeigende Statistikdateien */
	private final Statistics[] statistic;
	/** Titel f�r die einzelnen Viewer */
	private final String[] titleArray;
	/** Schaltfl�che "Modell und Statistikdaten anzeigen" */
	private final JButton showModelButton;
	/** "Hilfe" Schaltfl�che */
	private final JButton helpButton;
	/** Wird hier ein Modell eingetragen, so steht dieses �ber {@link #getModelForEditor()} nach dem Schlie�en des Panels zum Laden in den Editor bereit */
	private EditModel loadModelIntoEditor=null;

	/**
	 * Konstruktor der Klasse <code>ComparePanel</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param statistic	Anzuzeigende Statistikdateien
	 * @param title	Zus�tzliche �berschriften �ber den Statistikdateien
	 * @param allowLoadToEditor	Gibt an, ob angeboten werden soll, die zugeh�rigen Modelle in den Editor zu laden
	 * @param doneNotify	Wird aufgerufen, wenn sich das Panel schlie�en m�chte
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
		showModelButton=addUserButton(Language.tr("Main.Toolbar.ShowModelForTheseResults2"),Language.tr("Main.Toolbar.ShowModelForTheseResults2.Hint"),Images.MODEL.getIcon());
		addSeparator();
		helpButton=addUserButton(Language.tr("Main.Toolbar.Help"),Language.tr("Main.Toolbar.Help.Hint"),Images.HELP.getIcon());

		/* F1-Hotkey */
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),"actionHelp");
		getActionMap().put("actionHelp",new AbstractAction("actionHelp") {
			private static final long serialVersionUID = 1738622101739292954L;
			@Override public void actionPerformed(ActionEvent event) {Help.topicModal(ComparePanel.this,"Compare");}
		});
	}

	/**
	 * L�dt die angegebenen Statistikdateien in Statistikobjekte
	 * @param statisticFiles	Array der Statistikdateien
	 * @return	Array der Statistikobjekte; l�sst sich eine Datei nicht laden, so wird die Verarbeitung abgebrochen und an der entsprechenden Stelle im Array ein <code>null</code> zur�ckgegeben.
	 */
	public static final Statistics[] getStatisticFiles(File[] statisticFiles) {
		Statistics[] statistic=new Statistics[statisticFiles.length];
		Arrays.fill(statistic,null);
		for (int i=0;i<statisticFiles.length;i++) {
			Statistics data=new Statistics();
			String s=data.loadFromFile(statisticFiles[i]); if (s!=null) break;
			statistic[i]=data;
			statistic[i].loadedStatistics=statisticFiles[i];
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
				item.addActionListener(e->{
					Runnable loadToEditor=null;
					if (allowLoadToEditor) {
						loadToEditor=()->{loadModelIntoEditor=statisticData.editModel; close();};
					}
					ModelViewerFrame modelViewer=new ModelViewerFrame(owner,statisticData.editModel,statisticData,true,loadToEditor);
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
	 * Gibt an, ob beim Schlie�en der Vergleichsansicht ein Modell in den Editor geladen werden soll.
	 * @return	Zu ladendes Modell oder <code>null</code> wenn nichts geladen werden soll.
	 */
	public EditModel getModelForEditor() {
		return loadModelIntoEditor;
	}
}