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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

	private final JPanel content;
	private final JComboBox<String> select;
	private final Map<String,double[]> data;
	private final Map<String,LineChart> lineCharts;
	private LineChart lineChart;
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

		/* Starten */
		setMinSizeRespectingScreensize(800,600);
		setSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private LineChart getLineChart(final String heading) {
		LineChart chart=lineCharts.get(heading);
		if (chart==null) lineCharts.put(heading,chart=new LineChart(heading,data.get(heading)));
		return chart;
	}

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

	private class LineChart extends StatisticViewerLineChart {
		private final String heading;
		private final double[] data;

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
