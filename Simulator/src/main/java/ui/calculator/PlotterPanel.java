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
package ui.calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DialogTypeSelection;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.swing.PlugableFileChooser;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import systemtools.ImageTools;
import systemtools.MsgBox;
import systemtools.statistics.StatisticsBasePanel;
import systemtools.statistics.XWPFDocumentPictureTools;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Stellt ein von {@link JPanel} abgeleitetes Element bereit,
 * welches eine {@link JFreeChart}-basierendes Plotter inklusive
 * Eingabefeldern für den Darstellungsbereich usw. enthält. Es
 * können direkt Ausdrücke angegeben werden, die dargestellt
 * werden sollen.
 * @author Alexander Herzog
 *
 */
public class PlotterPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8604825152023324546L;

	/** Liste der Graphen */
	private final List<Graph> graphs;

	/** Eingabefeld für den minimalen X-Wert */
	private final JTextField inputMinX;
	/** Eingabefeld für den maximalen X-Wert */
	private final JTextField inputMaxX;
	/** Eingabefeld für den minimalen Y-Wert */
	private final JTextField inputMinY;
	/** Eingabefeld für den maximalen Y-Wert */
	private final JTextField inputMaxY;

	/** Diagramm */
	private final JFreeChart chart;
	/** Panel für das Diagramm */
	private final ChartPanel chartPanel;
	/** X-Y-Darstellung innerhalb des Diagramms */
	private final XYPlot plot;
	/** Datenmenge für die X-Y-Darstellung */
	private final XYSeriesCollection data;

	/** Vom Nutzer festgelegter minimaler X-Wert (der beim Klicken auf "Standardzoom" wieder eingestellt wird) */
	private double currentUnzoomMinX=-10;
	/** Vom Nutzer festgelegter maximaler X-Wert (der beim Klicken auf "Standardzoom" wieder eingestellt wird) */
	private double currentUnzoomMaxX=10;
	/** Vom Nutzer festgelegter minimaler Y-Wert (der beim Klicken auf "Standardzoom" wieder eingestellt wird) */
	private double currentUnzoomMinY=-10;
	/** Vom Nutzer festgelegter maximaler Y-Wert (der beim Klicken auf "Standardzoom" wieder eingestellt wird) */
	private double currentUnzoomMaxY=10;
	/** Wird von {@link #unzoom()}, {@link #inputXChanged(boolean)} und {@link #inputYChanged()} temporär auf <code>true</code> gesetzt, um Benachrichtigungsschleifen zu verhindern */
	private boolean justChangingZoom=false;

	/**
	 * Konstruktor der Klasse
	 */
	public PlotterPanel() {
		super();
		graphs=new ArrayList<>();
		setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		add(toolbar,BorderLayout.NORTH);
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Zoom"),Language.tr("CalculatorDialog.Plotter.Toolbar.Zoom.Hint"),Images.ZOOM.getIcon(),e->unzoom());
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Aspect"),Language.tr("CalculatorDialog.Plotter.Toolbar.Aspect.Hint"),Images.ZOOM_ASPECT.getIcon(),e->aspect());
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.AutoY"),Language.tr("CalculatorDialog.Plotter.Toolbar.AutoY.Hint"),Images.ZOOM_ASPECT.getIcon(),e->aspect());
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Copy"),Language.tr("CalculatorDialog.Plotter.Toolbar.Copy.Hint"),Images.EDIT_COPY.getIcon(),e->copyToClipboard((JButton)e.getSource()));
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Print"),Language.tr("CalculatorDialog.Plotter.Toolbar.Print.Hint"),Images.GENERAL_PRINT.getIcon(),e->print());
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Save"),Language.tr("CalculatorDialog.Plotter.Toolbar.Save.Hint"),Images.GENERAL_SAVE.getIcon(),e->save((JButton)e.getSource()));

		/* Panel mit Zoomfeldern */
		final JPanel outer=new JPanel(new BorderLayout());
		add(outer);
		final JToolBar left=new JToolBar();
		left.setOrientation(SwingConstants.VERTICAL);
		left.setFloatable(false);
		outer.add(left,BorderLayout.WEST);
		left.add(inputMaxY=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(inputMaxY);
		left.add(Box.createVerticalGlue());
		left.add(inputMinY=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(inputMinY);
		left.add(Box.createVerticalStrut(2*inputMaxY.getPreferredSize().height));

		inputMaxY.setMaximumSize(inputMaxY.getPreferredSize());
		inputMinY.setMaximumSize(inputMinY.getPreferredSize());
		inputMinY.addKeyListener(new KeyAdapter() {@Override public void keyReleased(KeyEvent e) {inputYChanged();}});
		inputMaxY.addKeyListener(new KeyAdapter() {@Override public void keyReleased(KeyEvent e) {inputYChanged();}});

		final JPanel inner=new JPanel(new BorderLayout());
		outer.add(inner,BorderLayout.CENTER);
		final JToolBar bottom=new JToolBar();
		bottom.setFloatable(false);
		inner.add(bottom,BorderLayout.SOUTH);
		bottom.add(Box.createHorizontalStrut(inputMaxY.getPreferredSize().width));
		bottom.add(inputMinX=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(inputMinX);
		bottom.add(Box.createHorizontalGlue());
		bottom.add(inputMaxX=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(inputMaxX);

		inputMaxX.setMaximumSize(inputMaxX.getPreferredSize());
		inputMinX.setMaximumSize(inputMinX.getPreferredSize());
		inputMinX.addKeyListener(new KeyAdapter() {@Override public void keyReleased(KeyEvent e) {inputXChanged(true);}});
		inputMaxX.addKeyListener(new KeyAdapter() {@Override public void keyReleased(KeyEvent e) {inputXChanged(true);}});

		inputMinX.setText("-10");
		inputMaxX.setText("10");

		/* Zeichenfläche */
		data=new XYSeriesCollection();
		chart=ChartFactory.createXYLineChart(null,"x","y",data,PlotOrientation.VERTICAL,false,true,false);
		plot=chart.getXYPlot();
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		plot.setDomainGridlinePaint(Color.black);
		inner.add(chartPanel=initChartPanel(chart),BorderLayout.CENTER);
		plot.getDomainAxis().addChangeListener(e->{
			if (!justChangingZoom) {
				final Range range=plot.getDomainAxis().getRange();
				inputMinX.setText(NumberTools.formatNumber(range.getLowerBound(),3));
				inputMaxX.setText(NumberTools.formatNumber(range.getUpperBound(),3));
			}
		});
		plot.getRangeAxis().addChangeListener(e->{
			if (!justChangingZoom) {
				final Range range=plot.getDomainAxis().getRange();
				inputMinY.setText(NumberTools.formatNumber(range.getLowerBound(),3));
				inputMaxY.setText(NumberTools.formatNumber(range.getUpperBound(),3));
			}
		});
	}

	/**
	 * Fügt eine Schaltfläche zu einer Symbolleiste hinzu
	 * @param toolbar	Symbolleiste zu der die Schaltfläche hinzugefügt werden soll
	 * @param title	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche (kann <code>null</code> sein)
	 * @param icon	Icon für die Schaltfläche (kann <code>null</code> sein)
	 * @param action	Aktion, die beim Anklicken der Schaltfläche ausgeführt werden soll (kann <code>null</code> sein)
	 */
	private void addToolbarIcon(final JToolBar toolbar, final String title, final String hint, final Icon icon, final ActionListener action) {
		final JButton button=new JButton(title==null?"":title);
		if (hint!=null) button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		if (action!=null) button.addActionListener(action);
		toolbar.add(button);
	}

	/**
	 * Erstellt ein Panel in dem das Diagramm angezeigt werden soll
	 * @param chart	Anzuzeigendes Diagramm
	 * @return	Panel das das Diagramm enthält
	 */
	private ChartPanel initChartPanel(final JFreeChart chart) {
		final ChartPanel chartPanel=new ChartPanel(
				chart,
				ChartPanel.DEFAULT_WIDTH,
				ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED,
				true,  /* properties */
				false,  /* save */
				true,  /* print */
				true,  /* zoom */
				true   /* tooltips */
				);
		chartPanel.setPopupMenu(null);

		chart.setBackgroundPaint(null);
		chart.getPlot().setBackgroundPaint(new GradientPaint(1,0,new Color(0xFA,0xFA,0xFF),1,150,new Color(0xEA,0xEA,0xFF)));

		final Color textBackground=UIManager.getColor("TextField.background");
		final boolean isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
		if (isDark) {
			chart.getPlot().setBackgroundPaint(Color.DARK_GRAY);
			ValueAxis axis;
			axis=((XYPlot)chart.getPlot()).getDomainAxis();
			axis.setAxisLinePaint(Color.LIGHT_GRAY);
			axis.setLabelPaint(Color.LIGHT_GRAY);
			axis.setTickLabelPaint(Color.LIGHT_GRAY);
			axis.setTickMarkPaint(Color.LIGHT_GRAY);
			axis=((XYPlot)chart.getPlot()).getRangeAxis();
			axis.setAxisLinePaint(Color.LIGHT_GRAY);
			axis.setLabelPaint(Color.LIGHT_GRAY);
			axis.setTickLabelPaint(Color.LIGHT_GRAY);
			axis.setTickMarkPaint(Color.LIGHT_GRAY);
		}

		TextTitle t=chart.getTitle();
		if (t!=null) {Font f=t.getFont(); t.setFont(new Font(f.getFontName(),Font.PLAIN,f.getSize()-4));}

		return chartPanel;
	}

	/**
	 * Anzahl der Schritte in der Wertetabelle
	 * @see #buildTable()
	 */
	private static final int TABLE_STEPS=1000;

	/**
	 * Erstellt eine Wertetabelle.
	 * @return	Wertetabelle
	 */
	private Table buildTable() {
		final Table table=new Table();
		final List<String> row=new ArrayList<>();

		row.add("x");
		final List<ExpressionCalc> calcs=new ArrayList<>();
		for (var graph: graphs) {
			final ExpressionCalc calc=graph.getParser();
			if (calc!=null) {
				row.add(graph.expression);
				calcs.add(calc);
			}
		}
		table.addLine(row);

		final double[] xArr=new double[1];
		final double minX=plot.getDomainAxis().getRange().getLowerBound();
		final double maxX=plot.getDomainAxis().getRange().getUpperBound();
		for (int i=0;i<=TABLE_STEPS;i++) {
			final double x=minX+(maxX-minX)*i/TABLE_STEPS;
			row.clear();
			row.add(NumberTools.formatNumberMax(x));
			for (var calc: calcs) {
				try {
					xArr[0]=x;
					final double y=calc.calc(xArr);
					row.add(NumberTools.formatNumberMax(y));
				} catch (MathCalcError e) {
					row.add("-");
				}
			}
			table.addLine(row);
		}

		return table;
	}

	/**
	 * Kopiert die aktuelle Darstellung in die Zwischenablage.
	 * @param parent	Kopieren-Schaltfläche (zum Ausrichten des Popup-Menüs)
	 */
	public void copyToClipboard(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();
		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Plotter.Toolbar.Copy.Table"),Images.GENERAL_TABLE.getIcon()));
		item.addActionListener(e->{
			final Table table=buildTable();
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(table.toString()),null);
		});

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Plotter.Toolbar.Copy.Graphics"),Images.EXTRAS_CALCULATOR_PLOTTER.getIcon()));
		item.addActionListener(e->{
			final SetupData setup=SetupData.getSetup();
			ImageTools.copyImageToClipboard(ImageTools.drawToImage(chart,setup.imageSize,setup.imageSize));
		});

		popup.show(parent,0,parent.getHeight());
	}

	/**
	 * Druckt die aktuelle Darstellung aus.
	 * @return	Gibt an, ob der Druckbefehl erfolgreich ausgeführt werden konnte.
	 */
	public boolean print() {
		try {
			final PrinterJob pjob=PrinterJob.getPrinterJob();

			final PrintRequestAttributeSet attributes=new HashPrintRequestAttributeSet();
			attributes.add(DialogTypeSelection.COMMON);

			if (!pjob.printDialog(attributes)) return false;
			pjob.setPrintable(chartPanel);
			pjob.print(attributes);
			return true;
		} catch (Exception e) {return false;}
	}

	/**
	 * Fragt einen Dateinamen ab und speichert die aktuelle Darstellung
	 * unter dem gewählten Namen.
	 * @param parent	Speichern-Schaltfläche (zum Ausrichten des Popup-Menüs)
	 * @return	Gibt an, ob die Darstellung gespeichert werden konnte. (Gründe für <code>false</code> können sowohl Fehler aus auch nutzerseitige Abbrüche sein.)
	 * @see #save(File)
	 */
	public boolean save(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();
		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Plotter.Toolbar.Save.Table"),Images.GENERAL_TABLE.getIcon()));
		item.addActionListener(e->{
			final File file=Table.showSaveDialog(this,Language.tr("CalculatorDialog.Plotter.Toolbar.Save.Table"),null);
			if (file==null) return;
			final Table table=buildTable();
			if (!table.save(file)) {
				MsgBox.error(this,StatisticsBasePanel.viewersSaveTableErrorTitle,String.format(StatisticsBasePanel.viewersSaveTableErrorInfo,file.toString()));
			}
		});

		popup.add(item=new JMenuItem(Language.tr("CalculatorDialog.Plotter.Toolbar.Save.Graphics"),Images.EXTRAS_CALCULATOR_PLOTTER.getIcon()));
		item.addActionListener(e->{
			final File file=selectImageFile();
			if (file==null) return;
			if (!save(file)) {
				MsgBox.error(this,StatisticsBasePanel.viewersSaveImageErrorTitle,String.format(StatisticsBasePanel.viewersSaveImageErrorInfo,file.toString()));
			}

		});

		popup.show(parent,0,parent.getHeight());



		return true;
	}

	/**
	 * Zeichnet das Diagramm in eine Bitmap
	 * @param chart	Zu zeichnendes Diagramm
	 * @param width	Breite des Bitmaps
	 * @param height	Höhe des Bitmaps
	 * @return	Bitmap in das das Diagramm gezeichnet wurde
	 */
	private static BufferedImage draw(JFreeChart chart, int width, int height) {
		final BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2=img.createGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0,0,width,height);
		chart.draw(g2,new Rectangle2D.Double(0, 0, width, height));
		g2.dispose();
		return img;
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Dateinamens zum Speichern einer Grafik an.
	 * @return	Liefert im Erfolgsfall den Dateinamen, sonst <code>null</code>
	 */
	private File selectImageFile() {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveImage);
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeJPG+" (*.jpg, *.jpeg)","jpg","jpeg");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeGIF+" (*.gif)","gif");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypePNG+" (*.png)","png");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeBMP+" (*.bmp)","bmp");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeTIFF+" (*.tiff, *.tif)","tiff","tif");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeWordWithImage+" (*.docx)","docx");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		fc.setFileFilter("png");
		fc.setAcceptAllFileFilterUsed(false);

		final File file=fc.showSaveDialogFileWithExtension(this);
		if (file==null) return null;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return null;
		}

		return file;
	}

	/**
	 * Speichert die Darstellung unter dem angegebenen Namen.
	 * Existiert die Ausgabedatei bereits, so wird diese überschrieben.
	 * @param file	Dateiname unter dem die Darstellung gespeichert werden soll.
	 * @return	Gibt an, ob das Speichern erfolgreich ausgeführt werden konnte.
	 * @see #save(Component)
	 */
	public boolean save(File file) {
		String s="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) s="gif";
		if (file.getName().toLowerCase().endsWith(".bmp")) s="bmp";
		if (file.getName().toLowerCase().endsWith(".tiff")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".tif")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".docx")) s="docx";
		if (file.getName().toLowerCase().endsWith(".pdf")) s="pdf";

		final SetupData setup=SetupData.getSetup();
		/* Führt bei jpgs zu falschen Farben: BufferedImage image=chart.createBufferedImage(setup.imageSize,setup.imageSize); */
		final BufferedImage image=draw(chart,setup.imageSize,setup.imageSize);

		if (s.equals("docx")) {
			try (XWPFDocument doc=new XWPFDocument()) {
				try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
					try {if (!ImageIO.write(image,"png",streamOut)) return false;} catch (IOException e) {return false;}
					if (!XWPFDocumentPictureTools.addPicture(doc,streamOut,Document.PICTURE_TYPE_PNG,image.getWidth(),image.getHeight())) return false;
				}
				try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);} catch (IOException e) {return false;}
				return true;
			} catch (IOException e) {return false;}
		}

		if (s.equals("pdf")) {
			return ImageTools.saveImage(this,image,file);
		}

		try {return ImageIO.write(image,s,file);} catch (IOException e) {return false;}
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer den minimalen oder den maximalen X-Wert verändert.
	 * @param keepYRange	Soll der bisherige y-Bereich beibehalten (<code>true</code>) oder auf Basis der Funktionen automatisch neu bestimmt werden (<code>false</code>)
	 */
	private void inputXChanged(final boolean keepYRange) {
		data.removeAllSeries();

		final Double minD=NumberTools.getDouble(inputMinX,true);
		final Double maxD=NumberTools.getDouble(inputMaxX,true);
		if (minD==null || maxD==null) return;
		double minX=minD;
		double maxX=maxD;
		if (maxX<minX) maxX=minX+1;
		final int steps=Math.max(1000,chartPanel.getWidth());

		double minY=Double.MAX_VALUE;
		double maxY=-Double.MAX_VALUE;
		for (Graph graph: graphs) {
			double[] range=graph.getMinMax(minX,maxX,steps);
			if (range!=null) {
				minY=Math.min(minY,range[0]);
				maxY=Math.max(maxY,range[1]);
			}
		}
		if (minY==0 && maxY==0) {
			minY=0;
			maxY=1;
		}
		if (minY>maxY) {
			minY=-1;
			maxY=1;
		}
		minY=Math.floor(minY);
		maxY=Math.ceil(maxY);
		if (!keepYRange) {
			inputMinY.setText(NumberTools.formatNumber(minY));
			inputMaxY.setText(NumberTools.formatNumber(maxY));
		}

		final XYItemRenderer renderer=plot.getRenderer();
		for (Graph graph: graphs) {
			XYSeries series=graph.getSeries(minX,maxX,steps);
			if (series==null) continue;
			data.addSeries(series);
			renderer.setSeriesPaint(data.getSeriesCount()-1,graph.color);
		}

		justChangingZoom=true;
		try {
			if (maxX>minX) plot.getDomainAxis().setRange(minX,maxX);
			if (!keepYRange) {
				if (maxY>minY) plot.getRangeAxis().setRange(minY,maxY);
			}
		} finally {
			justChangingZoom=false;
		}

		currentUnzoomMinX=minX;
		currentUnzoomMaxX=maxX;
		currentUnzoomMinY=minY;
		currentUnzoomMaxY=maxY;

		fireRedrawDone();
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer den minimalen oder den maximalen Y-Wert verändert.
	 */
	private void inputYChanged() {
		final Double minD=NumberTools.getDouble(inputMinY,true);
		final Double maxD=NumberTools.getDouble(inputMaxY,true);
		if (minD==null || maxD==null) return;
		double minY=minD;
		double maxY=maxD;
		if (maxY<minY) maxY=minY+1;

		justChangingZoom=true;
		try {
			if (maxY>minY) plot.getRangeAxis().setRange(minY,maxY);
		} finally {
			justChangingZoom=false;
		}
	}

	/**
	 * Stellt den Standardzoomfaktor wieder her.
	 */
	public void unzoom() {
		justChangingZoom=true;
		try {
			plot.getDomainAxis().setRange(currentUnzoomMinX,currentUnzoomMaxX);
			plot.getRangeAxis().setRange(currentUnzoomMinY,currentUnzoomMaxY);
		} finally {
			justChangingZoom=false;
		}

		inputMinX.setText(NumberTools.formatNumber(currentUnzoomMinX));
		inputMaxX.setText(NumberTools.formatNumber(currentUnzoomMaxX));
		inputMinY.setText(NumberTools.formatNumber(currentUnzoomMinY));
		inputMaxY.setText(NumberTools.formatNumber(currentUnzoomMaxY));
	}

	/**
	 * Stellt ein Seitenverhältnis von 1: ein.
	 */
	public void aspect() {
		final double zoomMinX=plot.getDomainAxis().getRange().getLowerBound();
		final double zoomMaxX=plot.getDomainAxis().getRange().getUpperBound();
		final double oldZoomMinY=plot.getRangeAxis().getRange().getLowerBound();
		final double oldZoomMaxY=plot.getRangeAxis().getRange().getUpperBound();

		final double screenWidth=chartPanel.getScreenDataArea().getWidth();
		final double screenHeight=chartPanel.getScreenDataArea().getHeight();

		final double newYRange=(zoomMaxX-zoomMinX)/screenWidth*screenHeight;

		double newZoomMinY=(oldZoomMaxY+oldZoomMinY)/2-newYRange/2;
		double newZoomMaxY=(oldZoomMaxY+oldZoomMinY)/2+newYRange/2;
		if (oldZoomMaxY+oldZoomMinY>1) {
			newZoomMinY=Math.round(newZoomMinY*1000)/1000;
			newZoomMaxY=Math.round(newZoomMaxY*1000)/1000;
		}

		justChangingZoom=true;
		try {
			plot.getRangeAxis().setRange(newZoomMinY,newZoomMaxY);
		} finally {
			justChangingZoom=false;
		}

		inputMinY.setText(NumberTools.formatNumber(newZoomMinY));
		inputMaxY.setText(NumberTools.formatNumber(newZoomMaxY));
	}

	/**
	 * Skaliert den dargestellten y-Bereich passend zu den angezeigten Funktionen.
	 */
	public void autoZoomY() {
		/* x-Bereich */
		final double minX=plot.getDomainAxis().getRange().getLowerBound();
		final double maxX=plot.getDomainAxis().getRange().getUpperBound();

		/* y-Werte ermitteln & sortieren */
		final List<Double> valuesList=new ArrayList<>();
		final double[] xArr=new double[1];
		for (var graph: graphs) if (!graph.expression.isBlank()) {
			final ExpressionCalc calc=graph.getParser();
			if (calc!=null) for (int i=0;i<=TABLE_STEPS;i++) {
				final double x=minX+(maxX-minX)*i/TABLE_STEPS;
				xArr[0]=x;
				double y;
				try {
					y=calc.calc(xArr);
					valuesList.add(y);
				} catch (MathCalcError e) {}
			}
		}
		if (valuesList.size()==0) return;
		final double[] values=valuesList.stream().mapToDouble(Double::doubleValue).sorted().toArray();

		/* y-Bereich */
		final double minY=values[0];
		final double maxY=values[values.length-1];

		/* 80%-Bereich */
		final double minYmain=values[(int)Math.round(values.length*0.1)];
		final double maxYmain=values[(int)Math.round(values.length*0.9)];

		double useMinY;
		if (minY>=minYmain-(maxYmain-minYmain)*0.5) {
			useMinY=minY;
		} else {
			/* Kleine y-Werte ragen weit raus */
			useMinY=minYmain;
		}

		double useMaxY;
		if (maxY<=maxYmain+(maxYmain-minYmain)*0.5) {
			useMaxY=maxY;
		} else {
			/* Kleine y-Werte ragen weit raus */
			useMaxY=maxYmain;
		}

		/* y-Bereich runden */
		if (Math.abs(useMinY)>=1000) useMinY=Math.floor(useMinY/100)*100;
		if (Math.abs(useMinY)>=100) useMinY=Math.floor(useMinY/10)*10;
		if (Math.abs(useMinY)>=5) useMinY=Math.floor(useMinY);
		if (Math.abs(useMaxY)>=1000) useMaxY=Math.ceil(useMaxY/100)*100;
		if (Math.abs(useMaxY)>=100) useMaxY=Math.ceil(useMaxY/10)*10;
		if (Math.abs(useMaxY)>=5) useMaxY=Math.ceil(useMaxY);

		/* Werte einstellen */
		justChangingZoom=true;
		try {
			plot.getRangeAxis().setRange(useMinY,useMaxY);
		} finally {
			justChangingZoom=false;
		}

		inputMinY.setText(NumberTools.formatNumber(useMinY));
		inputMaxY.setText(NumberTools.formatNumber(useMaxY));
	}

	/**
	 * Lädt die Graphen nach einer Änderung der Daten neu in die Darstellung.
	 * Diese Methode muss manuell aufgerufen werden, das Panel kann Änderungen
	 * an der Liste der Graphen nicht automatisch erkennen.
	 * @param keepYRange	Soll der bisherige y-Bereich beibehalten (<code>true</code>) oder auf Basis der Funktionen automatisch neu bestimmt werden (<code>false</code>)
	 * @see #getGraphs()
	 */
	public void reload(final boolean keepYRange) {
		inputXChanged(keepYRange);
	}

	/**
	 * Listener die nach dem Neuzeichnen benachrichtigt werden sollen
	 * @see #fireRedrawDone()
	 */
	private Set<Runnable> redrawDoneListeners=new HashSet<>();

	/**
	 * Benachrichtigt die Listener die nach dem Neuzeichnen benachrichtigt werden sollen
	 * @see #addRedrawDoneListener(Runnable)
	 * @see #removeRedrawDoneListener(Runnable)
	 */
	private void fireRedrawDone() {
		for (Runnable listener: redrawDoneListeners) listener.run();
	}

	/**
	 * Fügt einen Listener zu der Liste der Callbacks, die nach einem Neuzeichnen benachrichtigt werden sollen, hinzu.
	 * @param redrawDoneListener	Neuer Listener, der nach einem Neuzeichnen benachrichtigt werden soll
	 * @return	Gibt an, ob der Listener zu der Liste der Callbacks, die nach einem Neuzeichnen benachrichtigt werden sollen, hinzugefügt werden konnte.
	 */
	public boolean addRedrawDoneListener(final Runnable redrawDoneListener) {
		return redrawDoneListeners.add(redrawDoneListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Callbacks, die nach einem Neuzeichnen benachrichtigt werden sollen.
	 * @param redrawDoneListener	Listener, der nach einem Neuzeichnen nicht mehr benachrichtigt werden soll
	 * @return	Gibt an, ob der Listener aus der Liste der Callbacks, die nach einem Neuzeichnen benachrichtigt werden sollen, entfernt werden konnte.
	 */
	public boolean removeRedrawDoneListener(final Runnable redrawDoneListener) {
		return redrawDoneListeners.remove(redrawDoneListener);
	}

	/**
	 * Liefert die Liste der Graphen
	 * @return	Liste der Graphen
	 * @see #reload(boolean)
	 */
	public List<Graph> getGraphs() {
		return graphs;
	}

	/**
	 * Diese Klasse kapselt einen Graphen für die Darstellung in
	 * {@link PlotterPanel}
	 * @author Alexander Herzog
	 * @see PlotterPanel#getGraphs()
	 */
	public static class Graph {
		/**
		 * Name der Variable für {@link ExpressionCalc}
		 */
		private static final String[] variableName=new String[]{"x"};
		/**
		 * Array mit dem Wert für die Variable
		 */
		private final double[] variableValue=new double[1];

		/**
		 * Funktionsterm; Variable ist "x"
		 */
		public String expression;

		/**
		 * Farbe für den Graphen
		 */
		public Color color;

		/**
		 * Gibt an, ob der Ausdruck beim Plotten mindestens für einen x-Wert erfolgreich berechnet werden konnte.
		 */
		private boolean lastPlotOk;

		/**
		 * Konstruktor der Klasse
		 * @param expression	Funktionsterm; Variable ist "x"
		 * @param color	Farbe für den Graphen
		 */
		public Graph(final String expression, final Color color) {
			this.expression=expression;
			this.color=color;
			lastPlotOk=true;
		}

		/**
		 * Gibt an, ob der Ausdruck beim Plotten mindestens für einen x-Wert erfolgreich berechnet werden konnte.
		 * @return	Gibt an, ob der Ausdruck beim Plotten mindestens für einen x-Wert erfolgreich berechnet werden konnte.
		 */
		public boolean isLastPlotOk() {
			return lastPlotOk;
		}

		/**
		 * Konstruktor der Klasse.<br>
		 * Als Farbe wird Schwarz gewählt.
		 * @param expression	Funktionsterm; Variable ist "x"
		 */
		public Graph(final String expression) {
			this.expression=expression;
			this.color=Color.BLACK;
		}

		/**
		 * Liefert einen Parser, der den Ausdruck {@link #expression} und die Variablen {@link #variableName} verwendet.
		 * @return	Parser
		 * @see #expression
		 * @see #variableName
		 */
		private ExpressionCalc getParser() {
			if (expression==null || expression.isBlank()) return null;
			final ExpressionCalc calc=new ExpressionCalc(variableName,null);
			if (calc.parse(expression)>=0) return null;
			return calc;
		}

		/**
		 * Berechnet den Minimal- und den Maximalwert der Funktion in einem angegebenen Bereich
		 * @param xMin	Minimaler x-Wert
		 * @param xMax	Maximaler x-Wert
		 * @param steps	x-Schrittweite
		 * @return	Liefert im Erfolgsfall ein Array aus minimalem oder maximalem y-Wert; im Fehlerfall <code>null</code>.
		 */
		private double[] getMinMax(final double xMin, final double xMax, final int steps) {
			lastPlotOk=true;
			if (color==null) {lastPlotOk=false; return null;}
			final ExpressionCalc calc=getParser();
			if (calc==null) {lastPlotOk=false; return null;}

			double min=Double.MAX_VALUE;
			double max=-Double.MAX_VALUE;

			boolean atLeastOneValueOk=false;
			for (int i=0;i<steps;i++) {
				double x=xMin+i*(xMax-xMin)/(steps-1);
				variableValue[0]=x;
				try {
					final double d=calc.calc(variableValue);
					if (d>max) max=d;
					if (d<min) min=d;
					atLeastOneValueOk=true;
				} catch (MathCalcError e) {}
			}
			if (!atLeastOneValueOk) lastPlotOk=false;

			return new double[]{min,max};
		}

		/**
		 * Stellt sicher, dass es keine Kollisionen bei den Schlüsseln für die Serien gibt,
		 * wenn zwei Serien mit demselben Funktionsterm angezeigt werden sollen.
		 */
		private static long uniqueID=0;

		/**
		 * Erstellt basierend auf dem Funktionterm eine {@link JFreeChart}-Serie ({@link XYSeries})
		 * @param xMin	Minimaler x-Wert
		 * @param xMax	Maximaler x-Wert
		 * @param steps	x-Schrittweite
		 * @return	{@link XYSeries} zur Darstellung im Diagramm
		 */
		private XYSeries getSeries(final double xMin, final double xMax, final int steps) {
			lastPlotOk=true;
			if (color==null) {lastPlotOk=false; return null;}
			final ExpressionCalc calc=getParser();
			if (calc==null) {lastPlotOk=false; return null;}

			final XYSeries series=new XYSeries(expression+""+(uniqueID++));

			boolean atLeastOneValueOk=false;
			for (int i=0;i<steps;i++) {
				double x=xMin+i*(xMax-xMin)/(steps-1);
				variableValue[0]=x;
				try {
					series.add(x,calc.calc(variableValue));
					atLeastOneValueOk=true;
				} catch (MathCalcError e) {}
			}
			if (!atLeastOneValueOk) lastPlotOk=false;

			return series;
		}
	}
}