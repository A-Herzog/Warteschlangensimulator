/**
 * Copyright 2023 Alexander Herzog
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
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.images.SimToolsImages;

/**
 * Zeigt einen Dialog zur Auswahl der Werte zum Filtern einer Tabelle nach einer Spalte an.
 * @author Alexander Herzog
 * @see StatisticViewerTable
 */
public class StatisticViewerTableFilterDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5205292632873838927L;

	/**
	 * Liste der Filtereinträge
	 */
	private final JList<JCheckBox> list;

	/**
	 * Datenmodell für die Liste der Filtereinträge
	 */
	private final DefaultListModel<JCheckBox> model;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param values	Alle verfügbaren Werte in der Spalte
	 * @param active	Momentan ausgewählte Filterwerte
	 */
	public StatisticViewerTableFilterDialog(final Component owner, final Set<String> values, final Set<String> active) {
		super(owner,StatisticsBasePanel.contextFilterSelectTitle);

		/* GUI */
		addUserButton(StatisticsBasePanel.contextFilterSelectAll,SimToolsImages.ADD.getIcon());
		addUserButton(StatisticsBasePanel.contextFilterSelectNone,SimToolsImages.DELETE.getIcon());
		final JPanel content=createGUI(600,800,null);
		content.setLayout(new BorderLayout());

		/* Liste */
		final JScrollPane scrollPane=new JScrollPane(list=new JList<>());
		list.setOpaque(false);
		content.add(scrollPane,BorderLayout.CENTER);
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
		final boolean allActive=active.size()==0;
		for (String value: sortedValues(values)) {
			final boolean isActive=active.contains(value) || allActive;
			final JCheckBox checkBox=new JCheckBox(value,isActive);
			model.addElement(checkBox);
		}

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(600,400);
		pack();
		setMaxSizeRespectingScreensize(600,800);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Zwischenobjekt zur Sortierung von Listen aus Zahlenwerten
	 * @see StatisticViewerTableFilterDialog#sortedValues(Set)
	 */
	private static class SortDouble {
		/** Zeichenkette, die einen Zahlenwert enthält */
		public final String value;
		/** Zahlenwert */
		public final double d;

		/**
		 * Konstruktor der Klasse
		 * @param value	Zeichenkette, die einen Zahlenwert enthält
		 */
		public SortDouble(final String value) {
			this.value=value;
			d=NumberTools.getDouble(value);
		}
	}

	/**
	 * Wandelt eine Menge in eine (nach Zahlenwerten oder Zeichenketten) sortiert Liste um.
	 * @param values	Zu sortierende Menge von Zeichenketten
	 * @return	Sortierte Liste aus Zeichenketten
	 */
	private static List<String> sortedValues(final Set<String> values) {
		boolean allNumbers=true;
		for (String value: values) if (NumberTools.getDouble(value)==null) {allNumbers=false; break;}

		if (allNumbers) {
			return values.stream().map(SortDouble::new).sorted((value1,value2)->Double.compare(value1.d,value2.d)).map(value->value.value).collect(Collectors.toList());
		} else {
			return values.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		for (int i=0;i<model.size();i++) model.get(i).setSelected(nr==0);
		list.repaint();
	}

	/**
	 * Liefert nach dem Schließen des Dialogs die neue Menge der ausgewählten Filterwerte-
	 * @return	Menge der ausgewählten Filterwerte
	 */
	public Set<String> getActiveValues() {
		boolean allChecked=true;
		final Set<String> active=new HashSet<>();
		for (int i=0;i<model.size();i++) if (model.get(i).isSelected()) active.add(model.get(i).getText()); else allChecked=false;
		if (allChecked) return new HashSet<>();
		return active;
	}

	/**
	 * Renderer für die Filter-Einträge
	 */
	private static class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {
		/**
		 * Konstruktor der Klasse
		 */
		public JCheckBoxCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
			value.setForeground(list.getForeground());
			value.setBackground(list.getBackground());
			return value;
		}
	}
}
