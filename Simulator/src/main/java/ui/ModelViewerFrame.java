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
package ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.statistics.StatisticsPanel;
import ui.tools.WindowSizeStorage;

/**
 * Dieser Dialog ermöglicht es, ein Editor-Modell im read-only Model zu betrachten.
 * Optional kann angeboten werden, dass Modell (per <code>Runnable</code>-Callback) in den Editor zu laden.
 * @author Alexander Herzog
 */
public class ModelViewerFrame extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7004585654111284032L;

	/** Anzuzeigendes Modell */
	private EditModel model;
	/** Callback zum Laden des Modells in den Editor (wird hier <code>null</code> übergeben, so wird die Option zum Laden des Modells in den Editor nicht angeboten) */
	private final Runnable loadModel;

	/** Modell-Editor (im Read-Only-Modus) */
	private final EditorPanel editorPanel;
	/** Statistik-Viewer */
	private final StatisticsPanel statisticsPanel;

	/** "Schließen"-Schaltfläche */
	private final JButton buttonClose;
	/** "Modell speichern"-Schaltfläche */
	private final JButton buttonFileSaveModel;
	/** "Statistik speichern"-Schaltfläche */
	private final JButton buttonFileSaveStatistics;
	/** "Modell-Editor"-Schaltfläche */
	private final JButton buttonViewEditor;
	/** "Simulationsergebnisse"-Schaltfläche */
	private final JButton buttonViewStatistics;
	/** "Modell in Editor laden"-Schaltfläche */
	private final JButton buttonLoadToCurrentEditor;
	/** "Modell in Editor in neuem Fenster laden"-Schaltfläche */
	private final JButton buttonLoadToNewEditor;

	/**
	 * Konstruktor der Klasse <code>ModelViewerFrame</code>
	 * @param owner	Übergeordnetes Fenster (an dem sich der Dialog ausrichtet)
	 * @param model	Anzuzeigendes Modell
	 * @param statistics Anzuzeigende Statistikinformationen (kann <code>null</code> sein, dann wird nur das Modell angezeigt)
	 * @param showStatistics	Sollen die Statistikdaten in einem eigenen Panel angezeigt werden (<code>true</code>) oder sollen sie nur zur Anreicherung der Tooltips im Editor verwendet werden (<code>false</code>)
	 * @param loadModel	Callback zum Laden des Modells in den Editor (wird hier <code>null</code> übergeben, so wird die Option zum Laden des Modells in den Editor nicht angeboten)
	 */
	public ModelViewerFrame(final Window owner, final EditModel model, final Statistics statistics, final boolean showStatistics, final Runnable loadModel) {
		super(owner,Language.tr("Viewer.Title"));
		this.model=model;
		this.loadModel=loadModel;

		final Container content=getContentPane();
		content.setLayout(new BorderLayout());

		final JToolBar toolbar=new JToolBar();
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);

		buttonClose=createToolbarButton(toolbar,Language.tr("Dialog.Button.Close"),Language.tr("Viewer.Close.Hint"),Images.GENERAL_EXIT.getIcon());
		toolbar.addSeparator();
		buttonFileSaveModel=createToolbarButton(toolbar,Language.tr("Main.Toolbar.SaveModel"),Language.tr("Main.Toolbar.SaveModel.Hint"),Images.MODEL_SAVE.getIcon());
		if (statistics!=null && showStatistics) {
			buttonFileSaveStatistics=createToolbarButton(toolbar,Language.tr("Main.Toolbar.SaveStatistics"),Language.tr("Main.Toolbar.SaveStatistics.Hint"),Images.STATISTICS_SAVE.getIcon());
		} else {
			buttonFileSaveStatistics=null;
		}

		if (statistics!=null && showStatistics) {
			toolbar.addSeparator();
			buttonViewEditor=createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowEditor"),Language.tr("Main.Toolbar.ShowEditor.Hint"),Images.MODEL.getIcon());
			buttonViewStatistics=createToolbarButton(toolbar,Language.tr("Main.Toolbar.ShowStatistics"),Language.tr("Main.Toolbar.ShowStatistics.Hint"),Images.STATISTICS.getIcon());
			buttonViewEditor.setSelected(true);
		} else {
			buttonViewEditor=null;
			buttonViewStatistics=null;
		}

		if (loadModel!=null) {
			toolbar.addSeparator();
			buttonLoadToCurrentEditor=createToolbarButton(toolbar,Language.tr("Viewer.LoadModel"),Language.tr("Viewer.LoadModel.Hint"),Images.MODEL_LOAD.getIcon());
			buttonLoadToNewEditor=createToolbarButton(toolbar,Language.tr("Viewer.LoadModel.NewWindow"),Language.tr("Viewer.LoadModel.NewWindow.Hint"),Images.GENERAL_APPLICATION.getIcon());
		} else {
			buttonLoadToCurrentEditor=null;
			buttonLoadToNewEditor=null;
		}

		editorPanel=new EditorPanel(this,model,true,true,true,false);
		content.add(editorPanel,BorderLayout.CENTER);

		if (statistics!=null) {
			if (showStatistics) {
				statisticsPanel=new StatisticsPanel(1);
				statisticsPanel.setStatistics(statistics);
			} else {
				statisticsPanel=null;
			}
			editorPanel.setStatisticsGetter(()->statistics);
			if (this.model==null) this.model=statistics.editModel;
		} else {
			statisticsPanel=null;
		}

		setMinimumSize(new Dimension((int)Math.round(800*SetupData.getSetup().scaleGUI),(int)Math.round(600*SetupData.getSetup().scaleGUI)));
		setLocationRelativeTo(owner);
		WindowSizeStorage.window(this,"modelviewer");

		setModalityType(DEFAULT_MODALITY_TYPE);
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an
	 * @param toolbar	Übergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zusätzlich anzuzeigender Tooltip für den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	private JButton createToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(new ButtonListener());
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Liefert das aktuell angezeigte Modell zurück.<br>
	 * (Kann z.B. verwendet werden, wenn das Modell in den Editor geladen werden soll.)
	 * @return Aktuell angezeigtes Modell
	 */
	public final EditModel getModel() {
		return model;
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new AbstractAction(){
			private static final long serialVersionUID = -6894097779421071249L;
			@Override public void actionPerformed(ActionEvent e) {setVisible(false); dispose();}
		});

		return rootPane;
	}

	/** Reaktion auf das Anklicken einer Schaltfläche in der Symbolleiste */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object sender=e.getSource();
			if (sender==buttonClose) {
				setVisible(false);
				dispose();
				return;
			}
			if (sender==buttonFileSaveModel) {
				final String error=editorPanel.saveModelCopy();
				if (error!=null) MsgBox.error(ModelViewerFrame.this,Language.tr("XML.SaveErrorTitle"),error);
				return;
			}
			if (sender==buttonFileSaveStatistics) {
				final String error=statisticsPanel.saveStatistics(null);
				if (error!=null)	MsgBox.error(ModelViewerFrame.this,Language.tr("XML.SaveErrorTitle"),error);
				return;
			}
			if (sender==buttonViewEditor) {
				if (buttonViewEditor.isSelected()) return;
				buttonViewEditor.setSelected(true);
				buttonViewStatistics.setSelected(false);
				Container c=getContentPane();
				c.remove(statisticsPanel);
				c.add(editorPanel,BorderLayout.CENTER);
				c.revalidate();
				editorPanel.setVisible(false);
				editorPanel.setVisible(true);
				return;
			}
			if (sender==buttonViewStatistics) {
				if (buttonViewStatistics.isSelected()) return;
				buttonViewEditor.setSelected(false);
				buttonViewStatistics.setSelected(true);
				Container c=getContentPane();
				c.remove(editorPanel);
				c.add(statisticsPanel,BorderLayout.CENTER);
				c.revalidate();
				statisticsPanel.setVisible(false);
				statisticsPanel.setVisible(true);
				return;
			}
			if (sender==buttonLoadToCurrentEditor) {
				loadModel.run();
				setVisible(false);
				dispose();
				return;
			}
			if (sender==buttonLoadToNewEditor) {
				final MainFrame frame=new MainFrame(null,model);
				SwingUtilities.invokeLater(()->frame.toFront());
				setVisible(false);
				dispose();
			}
		}
	}
}