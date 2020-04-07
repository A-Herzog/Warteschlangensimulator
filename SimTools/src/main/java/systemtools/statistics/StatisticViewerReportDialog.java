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
package systemtools.statistics;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import systemtools.BaseDialog;

/**
 * Zeigt einen Dialog an, in dem der Nutzer auswählen kann, ob Bilder bei einem HTML-Report inline oder als
 * separate Dateien angelegt werden sollen.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
class StatisticViewerReportDialog extends BaseDialog {
	private static final long serialVersionUID = -6451843541862856961L;

	private final JRadioButton radioInline;
	private final JRadioButton radioFile;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param imagesInline	Sollen Bilder inline in HTML-Reports eingebettet werden
	 * @param help	Hilfe-Callback
	 */
	public StatisticViewerReportDialog(Component owner, boolean imagesInline, Runnable help) {
		super(owner,StatisticsBasePanel.viewersReport);
		final JPanel content=createGUI(500,200,help);

		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));

		content.add(new JLabel(StatisticsBasePanel.viewersReportSaveHTMLImages+":"));

		content.add(radioInline=new JRadioButton(StatisticsBasePanel.viewersReportSaveHTMLImagesInline));
		content.add(radioFile=new JRadioButton(StatisticsBasePanel.viewersReportSaveHTMLImagesFile));

		radioInline.setSelected(imagesInline);
		radioFile.setSelected(!imagesInline);

		final ButtonGroup bg=new ButtonGroup();
		bg.add(radioInline);
		bg.add(radioFile);

		pack();
		setVisible(true);
	}

	/**
	 * Liefert die möglicherweise in dem Dialog geänderte, neue Einstellung dazu, ob Bilder inline in HTML-Reports eingebettet werden sollen.
	 * @return	Bilder in HTML-Reports inline einbetten?
	 */
	public boolean isInline() {
		return radioInline.isSelected();
	}
}
