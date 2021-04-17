/**
 * Copyright 2021 Alexander Hbrerzog
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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import language.Language;
import tools.SetupData;
import ui.EditorPanelStatistics;
import ui.images.Images;
import ui.tools.WindowSizeStorage;

/**
 * Zeigt ein nicht-modales Fenster zur Auswahl der jeweils anzuzeigenden Heatmap-Modus an.
 * @author Alexander Herzog
 * @see	ui.EditorPanelStatistics.HeatMapMode
 */
public class HeatMapSelectWindow extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=9039350099977490317L;

	/**
	 * Callback zum Aktualisieren der Zeichenfl�che nach
	 * der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 */
	private final Runnable updateEditor;

	/**
	 * Globales Setup-Objekt
	 */
	private final SetupData setup;

	/**
	 * Liste der m�glichen Heatmap-Modi
	 */
	private final List<JRadioButton> modes;

	/**
	 * Wird auf <code>true</code> gesetzt, wenn gerade eine
	 * Aktualisierung durch {@link #updateSelectedMode()} bzw.
	 * durch {@link #updateSelection()} erfolgt, damit dann durch
	 * {@link #selectMode(ui.EditorPanelStatistics.HeatMapMode)}
	 * keine Endlosschleife entsteht
	 * @see #updateSelectedMode()
	 * @see #updateSelection()
	 * @see #selectMode(ui.EditorPanelStatistics.HeatMapMode)
	 */
	private boolean updating;

	/**
	 * Instanz des Fensters
	 * @see #show(Component, Runnable)
	 * @see #closeWindow()
	 */
	private static HeatMapSelectWindow instance;

	/**
	 * Zeigt das Fenster an.
	 * (Erstellt entweder ein neues Fenster oder holt das aktuelle in den Vordergrund.)
	 * @param owner	�bergeordnetes Element
	 * @param updateEditor	Callback zum Aktualisieren der Zeichenfl�che nach der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 */
	public static void show(final Component owner, final Runnable updateEditor) {
		if (instance==null) {
			instance=new HeatMapSelectWindow(owner,updateEditor);
			instance.setVisible(true);
		} else {
			if ((instance.getExtendedState() & ICONIFIED)!=0) instance.setState(NORMAL);
			instance.toFront();
		}
	}

	/**
	 * Aktualisiert das aktiviert anzuzeigende Radiobutton nach dem
	 * der Heatmap-Modus im Hauptfenster selbst ver�ndert wurde.
	 */
	public static void updateSelection() {
		if (instance==null) return;
		instance.updateSelectedMode();
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param updateEditor	Callback zum Aktualisieren der Zeichenfl�che nach der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 * @see #show(Component, Runnable)
	 */
	private HeatMapSelectWindow(final Component owner, final Runnable updateEditor) {
		super(Language.tr("HeatMapSelect.Title"));
		setIconImage(Images.STATISTIC_INFO.getImage());
		this.updateEditor=updateEditor;
		setup=SetupData.getSetup();

		/* �bergeordnetes Fenster */
		Component o=owner;
		while (o!=null && !(o instanceof Window)) o=o.getParent();
		final Window ownerWindow=(Window)o;

		/* Aktionen bei Schlie�en des Fensters */
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){closeWindow();}
			@Override public void windowIconified(WindowEvent e) {setState(Frame.NORMAL);}
		});

		/* Gesamter Inhaltsbereich */
		final Container all=getContentPane();
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Modi */
		final ButtonGroup buttonGroup=new ButtonGroup();
		modes=new ArrayList<>();
		for (EditorPanelStatistics.HeatMapMode mode: EditorPanelStatistics.HeatMapMode.values()) {
			final EditorPanelStatistics.HeatMapMode modeFinal=mode;
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			content.add(line);
			final JRadioButton button=new JRadioButton(mode.getName());
			buttonGroup.add(button);
			modes.add(button);
			line.add(button);
			button.addActionListener(e->selectMode(modeFinal));
		}
		updateSelectedMode();

		/* Fenster vorbereiten */
		setResizable(false);
		pack();
		setLocationRelativeTo(ownerWindow);
		WindowSizeStorage.window(this,"heatmapselect");
	}

	/**
	 * Aktualisiert das aktiviert anzuzeigende Radiobutton nach dem
	 * der Heatmap-Modus im Hauptfenster selbst ver�ndert wurde.
	 */
	private void updateSelectedMode() {
		updating=true;
		try {
			int index=0;
			if (setup.statisticHeatMap!=null) index=Arrays.asList(EditorPanelStatistics.HeatMapMode.values()).indexOf(setup.statisticHeatMap);
			for (int i=0;i<modes.size();i++) modes.get(i).setSelected(i==index);
		} finally {
			updating=false;
		}
	}

	/**
	 * Wird aufgerufen, wenn ein Heatmap-Modus-Radiobutton ausgew�hlt wurde
	 * @param mode	Ausgew�hltes Heatmap-Modus-Radiobutton
	 */
	private void selectMode(final EditorPanelStatistics.HeatMapMode mode) {
		if (updating) return;

		int index=0;
		for (int i=0;i<modes.size();i++) if (modes.get(i).isSelected()) {index=i; break;}
		setup.statisticHeatMap=EditorPanelStatistics.HeatMapMode.values()[index];
		setup.saveSetup();
		if (updateEditor!=null) updateEditor.run();
	}

	/**
	 * Aktionen beim Schlie�en des Fensters.
	 */
	private void closeWindow() {
		instance=null;
	}
}