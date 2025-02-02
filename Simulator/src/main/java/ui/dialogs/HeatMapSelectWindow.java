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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import language.Language;
import mathtools.distribution.swing.SimSystemsSwingImages;
import systemtools.BaseDialog;
import systemtools.images.SimToolsImages;
import tools.SetupData;
import ui.EditorPanelStatistics;
import ui.images.Images;
import ui.infopanel.InfoPanel;
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
	 * Callback zum Aktualisieren der Zeichenfläche nach
	 * der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 */
	private final Runnable updateEditor;

	/**
	 * Callback zur Abfrage, ob Statistikdaten verfügbar sind
	 */
	private final BooleanSupplier isStatisticAvailable;

	/**
	 * Globales Setup-Objekt
	 */
	private final SetupData setup;

	/**
	 * Beim letzten Aufruf von {@link #updateHeatMapSetup()}
	 * gewählter Heatmap (um unnötige Aktualisierungen des
	 * Dialogs zu vermeiden).
	 * @see #updateHeatMapSetup()
	 */
	private EditorPanelStatistics.HeatMapMode lastHeatMapMode;

	/**
	 * Beim letzten Aufruf von {@link #updateHeatMapSetup()}
	 * gültiger Statistikstatus (um unnötige Aktualisierungen
	 * des Dialogs zu vermeiden).
	 * @see #updateHeatMapSetup()
	 */
	private Boolean lastStatisticAvailable;

	/**
	 * Liste der möglichen Heatmap-Modi
	 */
	private final List<JRadioButton> modes;

	/**
	 * Anzeige des Statistik-Status
	 */
	private final JLabel statisticInfo;

	/**
	 * Wird auf <code>true</code> gesetzt, wenn gerade eine
	 * Aktualisierung durch {@link #updateSelectedMode()} bzw.
	 * durch {@link #updateHeatMapSetup()} erfolgt, damit dann durch
	 * {@link #selectMode(ui.EditorPanelStatistics.HeatMapMode)}
	 * keine Endlosschleife entsteht
	 * @see #updateSelectedMode()
	 * @see #updateHeatMapSetup()
	 * @see #selectMode(ui.EditorPanelStatistics.HeatMapMode)
	 */
	private boolean updating;

	/**
	 * Instanz des Fensters
	 * @see #show(Component, Runnable, BooleanSupplier)
	 * @see #closeWindow()
	 */
	private static HeatMapSelectWindow instance;

	/**
	 * Zeigt das Fenster an.
	 * (Erstellt entweder ein neues Fenster oder holt das aktuelle in den Vordergrund.)
	 * @param owner	Übergeordnetes Element
	 * @param updateEditor	Callback zum Aktualisieren der Zeichenfläche nach der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 * @param isStatisticAvailable	Callback zur Abfrage des Statistik-Verfügbarkeitsstatus
	 */
	public static void show(final Component owner, final Runnable updateEditor, final BooleanSupplier isStatisticAvailable) {
		if (instance==null) {
			instance=new HeatMapSelectWindow(owner,updateEditor,isStatisticAvailable);
			instance.setVisible(true);
		} else {
			if ((instance.getExtendedState() & ICONIFIED)!=0) instance.setState(NORMAL);
			instance.toFront();
		}
	}

	/**
	 * Schaltet die Sichtbarkeit des Fensters um.
	 * @param owner	Übergeordnetes Element
	 * @param updateEditor	Callback zum Aktualisieren der Zeichenfläche nach der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 * @param isStatisticAvailable	Callback zur Abfrage des Statistik-Verfügbarkeitsstatus
	 * @see #show(Component, Runnable, BooleanSupplier)
	 */
	public static void toggleVisible(final Component owner, final Runnable updateEditor, final BooleanSupplier isStatisticAvailable) {
		if (instance==null) {
			show(owner,updateEditor,isStatisticAvailable);
		} else {
			instance.setVisible(false);
			instance.closeWindow();
		}
	}

	/**
	 * Aktualisiert das aktiviert anzuzeigende Radiobutton nach dem
	 * der Heatmap-Modus im Hauptfenster selbst verändert wurde.
	 */
	public static void updateHeatMapSetup() {
		if (instance==null) return;
		instance.updateSelectedMode();
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param updateEditor	Callback zum Aktualisieren der Zeichenfläche nach der Auswahl eines anderen Heatmap-Modus in diesem Fenster
	 * @param isStatisticAvailable	Callback zur Abfrage des Statistik-Verfügbarkeitsstatus
	 * @see #show(Component, Runnable, BooleanSupplier)
	 */
	private HeatMapSelectWindow(final Component owner, final Runnable updateEditor, final BooleanSupplier isStatisticAvailable) {
		super(Language.tr("HeatMapSelect.Title"));

		lastHeatMapMode=null;
		lastStatisticAvailable=null;

		setIconImage(Images.HEATMAP.getImage());
		this.updateEditor=updateEditor;
		this.isStatisticAvailable=isStatisticAvailable;
		setup=SetupData.getSetup();

		/* Übergeordnetes Fenster */
		Component o=owner;
		while (o!=null && !(o instanceof Window)) o=o.getParent();
		final Window ownerWindow=(Window)o;

		/* Aktionen bei Schließen des Fensters */
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){closeWindow();}
			@Override public void windowIconified(WindowEvent e) {setState(Frame.NORMAL);}
		});

		/* Gesamter Inhaltsbereich */
		final Container all=getContentPane();
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalHeatMapSelect);
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.CENTER);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Modi */
		final ButtonGroup buttonGroup=new ButtonGroup();
		modes=new ArrayList<>();
		for (EditorPanelStatistics.HeatMapMode mode: EditorPanelStatistics.HeatMapMode.values()) {
			final EditorPanelStatistics.HeatMapMode modeFinal=mode;
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			content.add(line);
			final JRadioButton button=new JRadioButton(mode.getName());
			button.setToolTipText(mode.getTooltip());
			buttonGroup.add(button);
			modes.add(button);
			line.add(button);
			button.addActionListener(e->selectMode(modeFinal));
		}
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(statisticInfo=new JLabel());
		statisticInfo.setBorder(BorderFactory.createEmptyBorder(5,3,5,0));

		updateSelectedMode();

		/* Zeile unten */
		final JPanel bottom=new JPanel(new FlowLayout(FlowLayout.LEFT));
		all.add(bottom,BorderLayout.SOUTH);
		JButton button;
		bottom.add(button=new JButton(BaseDialog.buttonTitleClose,SimToolsImages.EXIT.getIcon()));
		button.addActionListener(e->{closeWindow(); setVisible(false);});
		bottom.add(button=new JButton(Language.tr("Main.Menu.View.Statistics.HeatMapSetup"),Images.GENERAL_TOOLS.getIcon()));
		button.addActionListener(e->{
			final HeatMapSetupDialog dialog=new HeatMapSetupDialog(this);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				setup.saveSetup();
				if (updateEditor!=null) updateEditor.run();
			}
		});

		/* Schließen des Fensters auch über Escape */
		final InputMap inputMap=getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke("ESCAPE"),"keyESCAPE");
		rootPane.getActionMap().put("keyESCAPE",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=7790977398052296174L;
			@Override
			public void actionPerformed(ActionEvent e) {
				closeWindow();
				setVisible(false);
			}
		});

		/* Fenster vorbereiten */
		setResizable(false);
		pack();
		SwingUtilities.invokeLater(()->{
			final int maxWidth=Collections.list(buttonGroup.getElements()).stream().map(b->b.getWidth()).reduce(Math::max).orElse(0);
			all.setPreferredSize(new Dimension(maxWidth+25,all.getPreferredSize().height+75));
			pack();
		});

		setLocationRelativeTo(ownerWindow);
		WindowSizeStorage.window(this,"heatmapselect",false);
	}

	/**
	 * Aktualisiert das aktiviert anzuzeigende Radiobutton nach dem
	 * der Heatmap-Modus im Hauptfenster selbst verändert wurde.
	 */
	private void updateSelectedMode() {
		if (setup.statisticHeatMap!=lastHeatMapMode) {
			lastHeatMapMode=setup.statisticHeatMap;
			updating=true;
			try {
				int index=0;
				if (setup.statisticHeatMap!=null) index=Arrays.asList(EditorPanelStatistics.HeatMapMode.values()).indexOf(setup.statisticHeatMap);
				for (int i=0;i<modes.size();i++) modes.get(i).setSelected(i==index);
			} finally {
				updating=false;
			}
		}

		if (lastStatisticAvailable==null || lastStatisticAvailable!=isStatisticAvailable.getAsBoolean()) {
			lastStatisticAvailable=isStatisticAvailable.getAsBoolean();
			if (lastStatisticAvailable) {
				statisticInfo.setText("<html><body style=\"color: green;\">"+Language.tr("HeatMapSelect.Statistics.Available")+"</body></html>");
				statisticInfo.setIcon(SimSystemsSwingImages.OK.getIcon());
			} else {
				statisticInfo.setText("<html><body style=\"color: red;\">"+Language.tr("HeatMapSelect.Statistics.NotAvailable")+"</body></html>");
				statisticInfo.setIcon(SimSystemsSwingImages.CANCEL.getIcon());
			}
		}
	}

	/**
	 * Wird aufgerufen, wenn ein Heatmap-Modus-Radiobutton ausgewählt wurde
	 * @param mode	Ausgewähltes Heatmap-Modus-Radiobutton
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
	 * Aktionen beim Schließen des Fensters.
	 */
	private void closeWindow() {
		instance=null;
	}
}
