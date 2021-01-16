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
package ui.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import language.Language;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;

/**
 * Zeigt einen Dialog zur Auswahl der anzuzeigenden Teil-Einträge auf der Statistik-Übersichtseite an.
 * @author Alexander Herzog
 * @see StatisticViewerOverviewText
 */
public class StatisticViewerOverviewTextFilterDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=293606245324479350L;

	/**
	 * Globales Einstellungen-Objekt
	 */
	private final SetupData setup;

	/**
	 * Datenmodell für die Liste der Filtereinträge
	 */
	private final DefaultListModel<JCheckBox> model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public StatisticViewerOverviewTextFilterDialog(final Component owner) {
		super(owner,Language.tr("Statistics.OverviewFilter.Title"));

		setup=SetupData.getSetup();

		/* GUI */

		addUserButton(Language.tr("Statistics.OverviewFilter.All"),Images.EDIT_ADD.getIcon());
		addUserButton(Language.tr("Statistics.OverviewFilter.None"),Images.EDIT_DELETE.getIcon());
		getUserButton(0).setToolTipText(Language.tr("Statistics.OverviewFilter.All.Hint"));
		getUserButton(1).setToolTipText(Language.tr("Statistics.OverviewFilter.None.Hint"));
		final JPanel content=createGUI(()->Help.topicModal(this,"MainStatistik"));
		content.setLayout(new BorderLayout());

		final JList<JCheckBox> list=new JList<>();
		content.add(new JScrollPane(list),BorderLayout.CENTER);
		list.setCellRenderer(new JCheckBoxCellRenderer());
		list.setModel(model=new DefaultListModel<>());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index=list.locationToIndex(e.getPoint());
				if (index<0) return;
				final JCheckBox checkbox=list.getModel().getElementAt(index);
				checkbox.setSelected(!checkbox.isSelected());
				repaint();
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/* Daten laden */

		final Set<StatisticViewerOverviewText.Filter> set=StatisticViewerOverviewText.Filter.stringToSet(setup.statisticOverviewFilter);
		for (StatisticViewerOverviewText.Filter filter: StatisticViewerOverviewText.Filter.values()) model.addElement(new JCheckBox(filter.getName(),set.contains(filter)));

		/* Dialog starten */

		setMinSizeRespectingScreensize(0,400);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		for (int i=0;i<model.size();i++) model.get(i).setSelected(nr==0);
	}

	@Override
	protected void storeData() {
		final StringBuilder filters=new StringBuilder();
		for (int i=0;i<model.size();i++) if (model.get(i).isSelected()) filters.append('X'); else filters.append('-');
		setup.statisticOverviewFilter=filters.toString();
		setup.saveSetupWithWarning(this);
	}

	/**
	 * Renderer für die Filter-Einträge
	 */
	private class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {
		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
			value.setForeground(list.getForeground());
			value.setBackground(list.getBackground());
			return value;
		}
	}
}
