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
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
	 * Liste der Auswahlboxen für die verschiedenen Werte
	 */
	private final List<JCheckBox> checkBoxes;

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
		final JPanel scrollPaneOuter=new JPanel(new BorderLayout());
		content.add(new JScrollPane(scrollPaneOuter));

		final JPanel scrollPaneInner=new JPanel();
		scrollPaneInner.setLayout(new BoxLayout(scrollPaneInner,BoxLayout.PAGE_AXIS));
		scrollPaneOuter.add(scrollPaneInner,BorderLayout.NORTH);

		checkBoxes=new ArrayList<>();
		final boolean allActive=active.size()==0;
		for (String value: sortedValues(values)) {
			final boolean isActive=active.contains(value) || allActive;
			final JCheckBox checkBox=new JCheckBox(value,isActive);
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			line.add(checkBox);
			scrollPaneInner.add(line);
			checkBoxes.add(checkBox);
		}

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(600,800);
		pack();
		setMaxSizeRespectingScreensize(800,800);
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
		checkBoxes.forEach(checkBox->checkBox.setSelected(nr==0));
	}

	/**
	 * Liefert nach dem Schließen des Dialogs die neue Menge der ausgewählten Filterwerte-
	 * @return	Menge der ausgewählten Filterwerte
	 */
	public Set<String> getActiveValues() {
		boolean allChecked=true;
		for (JCheckBox checkBox: checkBoxes) if (!checkBox.isSelected()) {allChecked=false; break;}
		if (allChecked) return new HashSet<>();

		final Set<String> active=new HashSet<>();
		for (JCheckBox checkBox: checkBoxes) if (checkBox.isSelected()) active.add(checkBox.getText());
		return active;
	}
}
