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
package ui.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.swing.CommonVariables;
import simulator.statistics.Statistics;
import statistics.StatisticsLongRunPerformanceIndicator;
import systemtools.MsgBox;
import systemtools.statistics.StatisticViewerBarChart;
import systemtools.statistics.StatisticsBasePanel;
import ui.images.Images;
import ui.mjpeg.MJPEGSystem;

/**
 * Dieser Viewer stellt die Daten der Laufzeitstatistik als interaktives
 * (=Schieberegler für die Zeit) Balkendiagramm dar.
 * @see StatisticViewerBarChart
 * @author Alexander Herzog
 * @see Statistics#longRunStatistics
 */
public class StatisticViewerInteractiveBarChart extends StatisticViewerBarChart {
	/**
	 * Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	private final Statistics statistics;

	/** Farben für die Diagrammlinien */
	private static final Color[] COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN};

	private JPanel fullPanel;
	private JButton sliderButton;
	private JSlider slider;
	private JLabel sliderInfo;
	private double maxValue;
	private int runStepWide;
	private Timer sliderTimer;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerInteractiveBarChart(final Statistics statistics) {
		super();
		this.statistics=statistics;
	}

	private double getMaxValue() {
		double max=0;
		for (StatisticsLongRunPerformanceIndicator indicator: (StatisticsLongRunPerformanceIndicator[])statistics.longRunStatistics.getAll(StatisticsLongRunPerformanceIndicator.class)) {
			max=FastMath.max(max,indicator.getDistribution().getMax());
		}
		return max;
	}

	private int getSteps() {
		if (statistics.longRunStatistics.size()==0) return 0;
		return ((StatisticsLongRunPerformanceIndicator)statistics.longRunStatistics.getAll()[0]).getValueCount();
	}

	@Override
	protected void firstChartRequest() {
		initBarChart(Language.tr("Statistics.AdditionalStatistics"));
		setupBarChart(Language.tr("Statistics.AdditionalStatistics"),Language.tr("Statistics.AdditionalStatistics.PerformanceIndicator"),Language.tr("Statistic.Viewer.Chart.Value"),false);

		maxValue=getMaxValue();

		if (statistics.longRunStatistics.size()>0)	{
			runStepWide=Math.max(1,((StatisticsLongRunPerformanceIndicator)statistics.longRunStatistics.getAll()[0]).getValueCount()/40);
		} else {
			runStepWide=1;
		}
	}

	private void drawDiagram(final int index) {
		data.clear();
		int count=0;
		for (String station: statistics.longRunStatistics.getNames()) {
			final StatisticsLongRunPerformanceIndicator indicator=(StatisticsLongRunPerformanceIndicator)statistics.longRunStatistics.get(station);
			final double value=indicator.getValue(index);
			data.addValue(value,station,station);
			final Color color=COLORS[count%COLORS.length];
			plot.getRendererForDataset(data).setSeriesPaint(count,color);
			count++;
		}
		if (maxValue>0) plot.getRangeAxis().setRange(0,maxValue);
	}

	private void changeSlider(final int index) {
		final long delta=statistics.editModel.longRunStatistics.getStepWideSec();
		final long startSec=index*delta;
		final long endSec=(index+1)*delta;
		sliderInfo.setText(TimeTools.formatLongTime(startSec)+" -\n"+TimeTools.formatLongTime(endSec));
		drawDiagram(index);
	}

	private void setButtonMode(final boolean play) {
		sliderButton.setIcon(play?Images.STATISTICS_ANIMATION_PAUSE.getIcon():Images.STATISTICS_ANIMATION_PLAY.getIcon());
		sliderButton.setToolTipText(play?Language.tr("Statistics.AdditionalStatistics.InteractivePause"):Language.tr("Statistics.AdditionalStatistics.InteractivePlay"));
	}

	private void buttonClick() {
		if (sliderTimer!=null) {
			/* Stop */
			sliderTimer.stop();
			sliderTimer=null;
			setButtonMode(false);
		} else {
			/* Start */
			if (slider.getValue()==slider.getMaximum()) return;

			sliderTimer=new Timer(250,e->{
				int value=slider.getValue();
				if (value==slider.getMaximum()) {
					sliderTimer.stop();
					sliderTimer=null;
					setButtonMode(false);
				} else {
					value=Math.min(value+runStepWide,slider.getMaximum());
					slider.setValue(value);
					changeSlider(value);
				}

			});
			sliderTimer.start();
			setButtonMode(true);
		}
	}

	private void initFullPanel() {
		fullPanel=new JPanel(new BorderLayout());

		fullPanel.add(super.getViewer(true),BorderLayout.CENTER);

		final JPanel setupArea=new JPanel(new BorderLayout());
		setupArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		fullPanel.add(setupArea,BorderLayout.SOUTH);
		setupArea.add(sliderButton=new JButton(),BorderLayout.WEST);
		sliderButton.addActionListener(e->buttonClick());
		slider=new JSlider(SwingConstants.HORIZONTAL);
		slider.setMinimum(0);
		slider.setMaximum(getSteps()-1);
		setupArea.add(slider,BorderLayout.CENTER);
		setupArea.add(sliderInfo=new JLabel(),BorderLayout.EAST);
		sliderInfo.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		slider.addChangeListener(e->changeSlider(slider.getValue()));
		slider.setValue(0);
		changeSlider(0);
		setButtonMode(false);
	}

	@Override
	public Container getViewer(boolean needReInit) {
		if (fullPanel==null || needReInit) initFullPanel();
		return fullPanel;
	}

	private static File selectImageOrVideoFile(final Component owner, final boolean allowXLSX) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveImage);
		final FileFilter avi=new FileNameExtensionFilter(Language.tr("FileType.VideoFile")+" (*.avi)","avi");
		final FileFilter jpg=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeJPG+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeGIF+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePNG+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeBMP+" (*.bmp)","bmp");
		final FileFilter docx=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeWordWithImage+" (*.docx)","docx");
		final FileFilter pdf=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		final FileFilter xlsx=allowXLSX?new FileNameExtensionFilter(Table.FileTypeExcel+" (*.xlsx)","xlsx"):null;
		fc.addChoosableFileFilter(avi);
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pdf);
		if (xlsx!=null) fc.addChoosableFileFilter(xlsx);
		fc.setFileFilter(avi);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==avi) file=new File(file.getAbsoluteFile()+".avi");
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (xlsx!=null && fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return null;
		}

		return file;
	}

	@Override
	public void save(Component owner) {
		final File file=selectImageOrVideoFile(owner,canStoreExcelFile());
		if (file==null) return;

		if (!save(owner,file)) {
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveImageErrorTitle,String.format(StatisticsBasePanel.viewersSaveImageErrorInfo,file.toString()));
		}
	}

	@Override
	public boolean save(Component owner, File file) {
		if (!file.toString().toLowerCase().endsWith(".avi")) {
			/* Bilddatei */
			return super.save(owner,file);
		}

		/* Video */
		if (chartPanel==null) firstChartRequest();

		try {
			final MJPEGSystem mjpeg=new MJPEGSystem(file,true);
			if (!mjpeg.isReady()) return false;

			final long delta=statistics.editModel.longRunStatistics.getStepWideSec();
			final int steps=getSteps();
			final int stepWidth=Math.max(1,runStepWide/2);
			final int size=Math.min(1000,getImageSize());

			int i=0;
			while (i<steps) {
				final long startSec=i*delta;
				final long endSec=(i+1)*delta;
				final String timeInfo=TimeTools.formatLongTime(startSec)+" - "+TimeTools.formatLongTime(endSec);

				drawDiagram(i);

				final BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);
				final Graphics2D g2=image.createGraphics();
				g2.setBackground(Color.WHITE);
				g2.clearRect(0,0,size,size);
				chart.draw(g2,new Rectangle2D.Double(0,0,size,size));
				g2.drawString(timeInfo,5,size-g2.getFontMetrics().getDescent());
				g2.dispose();

				mjpeg.addFrame(image,i*1000);

				i+=stepWidth;
			}

			return mjpeg.done();
		} finally {
			changeSlider(slider.getValue());
		}
	}
}
