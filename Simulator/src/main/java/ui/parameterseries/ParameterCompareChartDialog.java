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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.xy.XYSeries;

import language.Language;
import systemtools.BaseDialog;
import systemtools.images.SimToolsImages;
import systemtools.statistics.ChartSetup;
import systemtools.statistics.ChartSetupDialog;
import systemtools.statistics.StatisticViewer;
import systemtools.statistics.StatisticViewerJFreeChart;
import systemtools.statistics.StatisticViewerLineChart;
import systemtools.statistics.StatisticsBasePanel;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.tools.WindowSizeStorage;

/**
 * Ermöglicht die direkte Anzeige des Verlaufs einer Ausgabegröße als Liniendiagramm
 * @author Alexander Herzog
 */
public final class ParameterCompareChartDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2783273631806165939L;

	/**
	 * Standardexportgröße für Bilder
	 */
	private final int defaultExportSize;

	/** Inhaltspanel */
	private final JPanel content;
	/** Auswahlfeld für die anzuzeigende Größe */
	private final JComboBox<String> select;
	/** Anzuzeigende Datenreihen */
	private final Map<String,double[]> data;
	/** Diagramme die die Datenreihen anzeigen */
	private final Map<String,LineChart> lineCharts;
	/** Aktuelles Diagramm */
	private LineChart lineChart;
	/** Container für die Diagramm {@link #lineChart} */
	private Container lineChartContainer;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param initialHeading	Name der Datenreihe
	 * @param data	Anzuzeigende Datenreihe
	 */
	public ParameterCompareChartDialog(final Component owner, final String initialHeading, final Map<String,double[]> data) {
		super(owner,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Title"));
		this.data=data;
		this.lineCharts=new HashMap<>();

		defaultExportSize=SetupData.getSetup().imageSize;

		/* GUI */
		showCloseButton=true;
		content=createGUI(()->Help.topicModal(ParameterCompareChartDialog.this,"ParameterSeries"));
		content.setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);

		final JLabel label=new JLabel(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.OutputValue")+":");
		toolbar.add(label);
		toolbar.add(Box.createHorizontalStrut(5));
		toolbar.add(select=new JComboBox<>(data.keySet().stream().sorted().toArray(String[]::new)));
		select.setSelectedItem(initialHeading);
		select.addActionListener(e->{
			setChart((String)select.getSelectedItem());
			/* Alle drei folgende doLayout()-Anweisungen werden benötigt und in dieser Reihenfolge. */
			content.doLayout();
			lineChartContainer.doLayout();
			for (Component c: lineChartContainer.getComponents()) c.doLayout();
		});
		label.setLabelFor(select);
		toolbar.add(Box.createHorizontalStrut(5));

		JButton button;

		if (data.size()>1) {
			toolbar.add(button=new JButton(Images.ARROW_LEFT_SHORT.getIcon()));
			button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.OutputValue.SelectLast"));
			button.addActionListener(e->select(-1));

			toolbar.add(button=new JButton(Images.ARROW_RIGHT_SHORT.getIcon()));
			button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.OutputValue.SelectNext"));
			button.addActionListener(e->select(1));
		}

		toolbar.addSeparator();

		toolbar.add(button=new JButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Unzoom"),Images.ZOOM_OUT.getIcon()));
		button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Unzoom.Hint"));
		button.addActionListener(e->lineChart.unZoom());

		toolbar.add(button=new JButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Copy"),Images.EDIT_COPY.getIcon()));
		button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Copy.Hint"));
		button.addActionListener(e->copyViewer((JButton)e.getSource(),lineChart));

		toolbar.add(button=new JButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Save"),Images.GENERAL_SAVE.getIcon()));
		button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Save.Hint"));
		button.addActionListener(e->saveViewer((JButton)e.getSource(),lineChart));

		toolbar.add(button=new JButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Settings"),Images.GENERAL_SETUP.getIcon()));
		button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Settings.Hint"));
		button.addActionListener(e->setupChart(lineChart));

		toolbar.add(Box.createHorizontalGlue());

		toolbar.add(button=new JButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.WindowSize"),Images.SETUP_WINDOW_SIZE_FULL.getIcon()));
		button.setToolTipText(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.WindowSize.Hint"));
		button.addActionListener(e->showSizePopup((JButton)e.getSource()));

		/* Diagramm */
		setChart(initialHeading);

		/* Kopier-Hotkeys setzen */
		final JRootPane root=getRootPane();

		SwingUtilities.invokeLater(()->{
			final KeyStroke keyCtrlC=KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK,true); /* true=Beim Loslassen erkennen; muss gesetzt sein, da die Subviewer die anderen Hotkeys teilweise aufhalten */
			final KeyStroke keyCtrlIns=KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_DOWN_MASK,true);  /* true=Beim Loslassen erkennen; muss gesetzt sein, da die Subviewer die anderen Hotkeys teilweise aufhalten */
			root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyCtrlC,"CopyViewer");
			root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyCtrlIns,"CopyViewer");
		});

		root.getActionMap().put("CopyViewer",new AbstractAction() {
			private static final long serialVersionUID=3692362064092790425L;
			@Override
			public void actionPerformed(ActionEvent e) {
				lineChart.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
			}
		});

		/* Starten */
		setMinSizeRespectingScreensize(1000,700);
		setSizeRespectingScreensize(1000,700);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		WindowSizeStorage.window(this,"parameterseries_diagram_viewer");
		setVisible(true);
	}

	/**
	 * Liefert oder generiert ein Diagramm zu einem bestimmten Ausgabewert
	 * @param heading	Überschrift des Diagramms
	 * @return	Entsprechendes Diagramm
	 * @see #setChart(String)
	 */
	private LineChart getLineChart(final String heading) {
		LineChart chart=lineCharts.get(heading);
		if (chart==null) lineCharts.put(heading,chart=new LineChart(heading,data.get(heading)));
		return chart;
	}

	/**
	 * Stellt das aktuell anzuzeigende Diagramm ein
	 * @param heading	Überschrift des neuen Diagramms
	 * @see #getLineChart(String)
	 */
	private void setChart(final String heading) {
		if (lineChartContainer!=null) content.remove(lineChartContainer);
		lineChart=getLineChart(heading);
		content.add(lineChartContainer=lineChart.getViewer(true),BorderLayout.CENTER);
	}

	/**
	 * Wechselt zu einer vorherigen oder einer weiteren Diagrammansicht.
	 * @param delta	Verschiebung der aktuellen Ansicht (sollte +1 oder -1 sein)
	 */
	private void select(final int delta) {
		int nr=select.getSelectedIndex()+delta;
		if (nr<0) nr=data.size()-1;
		if (nr>=data.size()) nr=0;
		select.setSelectedIndex(nr);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0:
			lineChart.unZoom();
			break;
		case 1:
			lineChart.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
			break;
		case 2:
			lineChart.save(this);
			break;
		}
	}

	/**
	 * Liniendiagramm
	 * @see ParameterCompareChartDialog#getLineChart(String)
	 */
	private class LineChart extends StatisticViewerLineChart {
		/** Überschrift */
		private final String heading;
		/** Datenreihe */
		private final double[] data;

		/**
		 * Konstruktor der Klasse
		 * @param heading	Überschrift
		 * @param data	Datenreihe
		 */
		public LineChart(final String heading, final double[] data) {
			this.heading=heading;
			this.data=data;
		}

		@Override
		protected void firstChartRequest() {
			initLineChart(heading);
			setupChartValue(heading,Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.ModelNumber"),heading);

			final XYSeries series=addSeries(heading,Color.RED);
			for (int i=0;i<data.length;i++) series.add(i+1,data[i]);

			final NumberAxis xAxis=(NumberAxis)plot.getDomainAxis();
			xAxis.setNumberFormatOverride(new DecimalFormat("0"));
			xAxis.setTickUnit(new NumberTickUnit(Math.round(Math.max(xAxis.getTickUnit().getSize(),1))));

			addFillColor(0);
			smartZoom(0);
		}
	}

	/**
	 * Erzeugt einen Menüpunkt zur Wahl einer Fenstergröße.
	 * @param width	Einzustellende Fensterbreite (Werte kleiner als 0 für Vollbild)
	 * @param height	Einzustellende Fensterhöhe (Werte kleiner als 0 für Vollbild)
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem getSizeItem(final int width, final int height) {
		JMenuItem item;
		if (width<0 || height<0) {
			item=new JMenuItem(StatisticsBasePanel.viewersToolbarFullscreen,SimToolsImages.FULLSCREEN.getIcon());
			item.setToolTipText(StatisticsBasePanel.viewersToolbarFullscreenHint);
			item.addActionListener(e->{
				final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
				final Insets border=getInsets();
				final int wPlus=border.left+border.right;
				setBounds(-border.left,0,screenSize.width+wPlus,screenSize.height);
				toFront();
			});
		} else {
			item=new JMenuItem(width+"x"+height);
			item.addActionListener(e->{
				setSize(width,height);
				setLocationRelativeTo(getOwner());
			});
		}
		return item;
	}

	/**
	 * Zeigt das Popupmenü zur Auswahl der Fenstergröße ein.
	 * @param parent	Übergeordnetes Element für das Popupmenü
	 */
	private void showSizePopup(final JComponent parent) {
		final JPopupMenu menu=new JPopupMenu();

		menu.add(getSizeItem(800,600));
		menu.add(getSizeItem(1024,768));
		menu.add(getSizeItem(1280,720));
		menu.add(getSizeItem(1440,810));
		menu.add(getSizeItem(1920,1080));
		menu.addSeparator();
		menu.add(getSizeItem(-1,-1));

		menu.show(parent,0,parent.getHeight());
	}

	/**
	 * Stellt die Bildgröße zum Exportieren ein.
	 * @param size	Bildgröße zum Exportieren
	 */
	private void setImageSize(final int size) {
		final SetupData setup=SetupData.getSetup();
		setup.imageSize=size;
		setup.saveSetup();
	}

	/**
	 * Kopiert den Viewer in der angegebenen Größe in die Zwischenablage.
	 * @param viewer	Zu kopierender Viewer
	 * @param size	Größe (-1 für Standardexportgröße)
	 */
	private void copyViewer(final StatisticViewer viewer, final int size) {
		if (size>=0) setImageSize(size);
		viewer.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
		if (size>=0) setImageSize(defaultExportSize);
	}

	/**
	 * Speichert den Inhalt des Viewers in der angegebenen Größe in einer Datei.
	 * @param viewer	Zu speichernder Viewer
	 * @param size	Größe (-1 für Standardexportgröße)
	 */
	private void saveViewer(final StatisticViewer viewer, final int size) {
		if (size>=0) setImageSize(size);
		viewer.save(this);
		if (size>=0) setImageSize(defaultExportSize);
	}

	/**
	 * Liefert die Exportgröße gemäß Fenstergröße.
	 * @return	Exportgröße gemäß Fenstergröße
	 */
	private int getScreenExportSize() {
		return Math.max(getWidth(),getHeight());
	}

	/**
	 * Kopiert den Viewer in die Zwischenablage.
	 * @param sender	Auslösendes Button (zur Ausrichtung des Popupmenüs, wenn nötig)
	 * @param viewer	Zu kopierender Viewer
	 */
	private void copyViewer(final JButton sender, final StatisticViewer viewer) {
		if (viewer instanceof StatisticViewerJFreeChart) {
			final int screenExportSize=getScreenExportSize();
			final JPopupMenu menu=new JPopupMenu();
			JMenuItem item;
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarCopyDefaultSize,defaultExportSize,defaultExportSize),SimToolsImages.COPY.getIcon()));
			item.addActionListener(e->copyViewer(viewer,-1));
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarCopyWindowSize,screenExportSize,screenExportSize),SimToolsImages.FULLSCREEN.getIcon()));
			item.addActionListener(e->copyViewer(viewer,screenExportSize));
			menu.show(sender,0,sender.getHeight());

		} else {
			copyViewer(viewer,-1);
		}
	}

	/**
	 * Speichert den Inhalt des Viewers in der angegebenen Größe in einer Datei.
	 * @param sender	Auslösendes Button (zur Ausrichtung des Popupmenüs, wenn nötig)
	 * @param viewer	Zu speichernder Viewer
	 */
	private void saveViewer(final JButton sender, final StatisticViewer viewer) {
		if (viewer instanceof StatisticViewerJFreeChart) {
			final int screenExportSize=getScreenExportSize();
			final JPopupMenu menu=new JPopupMenu();
			JMenuItem item;
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarSaveDefaultSize,defaultExportSize,defaultExportSize),SimToolsImages.SAVE.getIcon()));
			item.addActionListener(e->saveViewer(viewer,-1));
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarSaveWindowSize,screenExportSize,screenExportSize),SimToolsImages.FULLSCREEN.getIcon()));
			item.addActionListener(e->saveViewer(viewer,screenExportSize));
			menu.show(sender,0,sender.getHeight());

		} else {
			saveViewer(viewer,-1);
		}
	}

	/**
	 * Zeigt den Dialog zum Bearbeiten der Diagrammeinstellungen an.
	 * @param viewer	Viewer bei dem die Formatierung angepasst werden soll
	 */
	private void setupChart(final StatisticViewerJFreeChart viewer) {
		final SetupData setup=SetupData.getSetup();

		final ChartSetupDialog dialog=new ChartSetupDialog(this,setup.imageSize,setup.chartSetup);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			setup.imageSize=dialog.getSaveSize();
			final ChartSetup newChartSetup=dialog.getChartSetup();
			setup.chartSetup.copyFrom(newChartSetup);
			setup.saveSetup();
			viewer.setChartSetup(newChartSetup);
		}
	}
}
