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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementUserStatistic}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementUserStatistic
 */
public class ModelElementUserStatisticDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1758925669523956820L;

	/**
	 * Tabellenmodell der in dem Dialog darzustellenden Tabelle
	 */
	private UserStatisticTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementUserStatistic}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementUserStatisticDialog(final Component owner, final ModelElementUserStatistic element, final boolean readOnly) {
		super(owner,Language.tr("Surface.UserStatistic.Dialog.Title"),element,"ModelElementUserStatistic",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationUserStatistic;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementUserStatistic) {
			final ModelElementUserStatistic station=(ModelElementUserStatistic)element;
			JTableExt table=new JTableExt();
			table.setModel(tableModel=new UserStatisticTableModel(table,helpRunnable,element.getId(),station.getKeys(),station.getIsTime(),station.getExpressions(),station.getIsContinuous(),element.getModel(),element.getSurface(),readOnly));
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.getColumnModel().getColumn(0).setMaxWidth(200);
			table.getColumnModel().getColumn(0).setMinWidth(200);
			table.setEnabled(!readOnly);
			content.add(new JScrollPane(table),BorderLayout.CENTER);
		}

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementUserStatistic) {
			final List<String> keys=((ModelElementUserStatistic)element).getKeys();
			final List<Boolean> isTime=((ModelElementUserStatistic)element).getIsTime();
			final List<String> expressions=((ModelElementUserStatistic)element).getExpressions();
			final List<Boolean> isContinuous=((ModelElementUserStatistic)element).getIsContinuous();
			keys.clear();
			isTime.clear();
			expressions.clear();
			isContinuous.clear();
			keys.addAll(tableModel.getKeys());
			isTime.addAll(tableModel.getIsTime());
			expressions.addAll(tableModel.getExpressions());
			isContinuous.addAll(tableModel.getIsContinuous());
		}

		/* Formate für alle Schlüssel auflisten */
		final Map<String,Boolean> newIsTime=new HashMap<>();
		final Map<String,Boolean> newIsContinuous=new HashMap<>();
		for (int i=0;i<tableModel.getKeys().size();i++) {
			final String key=tableModel.getKeys().get(i);
			newIsTime.put(key,tableModel.getIsTime().get(i));
			newIsContinuous.put(key,tableModel.getIsContinuous().get(i));
		}

		/* Alle Statistik-Stationen identifizieren */
		ModelSurface surface=element.getSurface();
		if (surface.getParentSurface()!=null) surface=surface.getParentSurface();
		final List<ModelElementUserStatistic> userStatisticStations=new ArrayList<>();
		for (ModelElement e1: surface.getElements()) {
			if (e1 instanceof ModelElementUserStatistic) userStatisticStations.add((ModelElementUserStatistic)e1);
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) {
				if (e2 instanceof ModelElementUserStatistic) userStatisticStations.add((ModelElementUserStatistic)e2);
			}
		}

		/* Gleiches Format bei allen Stationen für die jeweiligen Schlüsse einstellen */
		for (ModelElementUserStatistic station: userStatisticStations) {
			final List<String> keys=station.getKeys();
			final List<Boolean> isTimeSettings=station.getIsTime();
			final List<Boolean> isContinuousSettings=station.getIsContinuous();

			for (int i=0;i<keys.size();i++) {
				final String key=keys.get(i);
				if (newIsTime.containsKey(key)) isTimeSettings.set(i,newIsTime.get(key));
				if (newIsContinuous.containsKey(key)) isContinuousSettings.set(i,newIsContinuous.get(key));
			}
		}
	}
}