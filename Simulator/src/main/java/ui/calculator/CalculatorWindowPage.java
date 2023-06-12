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
package ui.calculator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Basisklasse f¸r die Tabs in {@link CalculatorWindow}
 * @author Alexander Herzog
 * @see CalculatorWindow
 */
public abstract class CalculatorWindowPage extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6866918464131956741L;

	/**
	 * Tabs-Element in das dieses Tab eingef¸gt wurde
	 * @see #addToJTabbedPane(JTabbedPane)
	 */
	private JTabbedPane tabs;

	/**
	 * Index dieses Tabs in {@link #tabs}
	 * @see #addToJTabbedPane(JTabbedPane)
	 */
	private int tabIndex;

	/**
	 * Konstruktor der Klasse
	 * @param tabs	Tabs-Element in das dieses Tab eingef¸gt werden soll
	 */
	public CalculatorWindowPage(final JTabbedPane tabs) {
		addToJTabbedPane(tabs);
		setLayout(new BorderLayout());
	}

	/**
	 * Liefert den Titel f¸r diesen Tabs.
	 * @return	Titel f¸r diesen Tabs
	 */
	protected abstract String getTabTitle();

	/**
	 * Liefert das Icon f¸r diesen Tabs.
	 * @return	Icon f¸r diesen Tabs
	 */
	protected abstract Images getTabIcon();

	/**
	 * Erzeugt eine Eingabezeile mit zugehˆrigem Label
	 * @param labelText	Beschriftungstext
	 * @param value	Initialer Text f¸r die Eingabezeile
	 * @param size	L‰nge der Eingabezeile; wird hier ein Wert &le;0 angegeben, so wird die maximal mˆgliche Breite verwendet
	 * @return	Array aus: Panel das Beschriftung und Eingabezeile enth‰lt und Eingabezeile selbst
	 */
	public static final Object[] getInputPanel(final String labelText, final String value, final int size) {
		JPanel panel;
		JLabel label;
		JTextField field;

		if (size>0) {
			panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(label=new JLabel(labelText));
			panel.add(field=new JTextField(size));
		} else {
			panel=new JPanel(new BorderLayout(5,0));

			Box box;

			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			final JPanel panelLeft=new JPanel(new FlowLayout());
			panelLeft.add(label=new JLabel(labelText));
			box.add(panelLeft);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.WEST);

			field=new JTextField();
			field.setMaximumSize(new Dimension(field.getMaximumSize().width,field.getPreferredSize().height));
			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			box.add(field);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.CENTER);
		}

		ModelElementBaseDialog.addUndoFeature(field);
		label.setLabelFor(field);
		field.setText(value);
		return new Object[]{panel,field};
	}

	/**
	 * F¸gt dieses Panel zu den Tabs hinzu.
	 * @param tabs	Tabs zu denen dieses Panel hinzugef¸gt werden soll
	 * @see #getTabTitle()
	 * @see #getTabIcon()
	 */
	private void addToJTabbedPane(final JTabbedPane tabs) {
		if (tabs==null) return;
		this.tabs=tabs;
		tabs.addTab(getTabTitle(),this);
		tabIndex=tabs.getTabCount()-1;
		tabs.setIconAt(tabIndex,getTabIcon().getIcon());
	}

	/**
	 * Zeigt dieses Tab innerhalb der Tab-‹bersicht an.
	 */
	public final void showPage() {
		if (tabs!=null) SwingUtilities.invokeLater(()->tabs.setSelectedIndex(tabIndex));
	}

	/**
	 * Wird von {@link CalculatorWindow#closeWindow()} aufgerufen,
	 * um den Tabs die Gelegenheit zu geben, vor dem Schlieﬂen Daten zu speichern.
	 * @see CalculatorWindow#closeWindow()
	 */
	public void storeData() {
	}
}
