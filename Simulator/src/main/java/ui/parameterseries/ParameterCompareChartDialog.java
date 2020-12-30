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
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jfree.data.xy.XYSeries;

import language.Language;
import systemtools.BaseDialog;
import systemtools.statistics.StatisticViewerLineChart;
import ui.help.Help;
import ui.images.Images;

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

		/* User-Buttons */
		addUserButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Unzoom"),Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Unzoom.Hint"),Images.ZOOM_OUT.getURL());
		addUserButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Copy"),Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Copy.Hint"),Images.EDIT_COPY.getURL());
		addUserButton(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Save"),Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.Save.Hint"),Images.GENERAL_SAVE.getURL());

		/* GUI */
		showCloseButton=true;
		content=createGUI(()->Help.topicModal(ParameterCompareChartDialog.this,"ParameterSeries"));
		content.setLayout(new BorderLayout());

		/* Auswahl */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ParameterCompare.Toolbar.ProcessResults.ResultsChart.OutputValue")+":"));
		line.add(select=new JComboBox<>(data.keySet().stream().sorted().toArray(String[]::new)));
		select.setSelectedItem(initialHeading);
		select.addActionListener(e->{
			setChart((String)select.getSelectedItem());
			content.doLayout();
		});

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
		setMinSizeRespectingScreensize(800,600);
		setSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(this.owner);
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

			addFillColor(0);
			smartZoom(0);
		}
	}
}
