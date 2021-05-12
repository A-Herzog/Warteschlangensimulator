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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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
import mathtools.distribution.swing.CommonVariables;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import swingtools.ImageIOFormatCheck;
import systemtools.ImageTools;
import systemtools.MsgBox;
import systemtools.statistics.PDFWriter;
import systemtools.statistics.StatisticsBasePanel;
import systemtools.statistics.XWPFDocumentPictureTools;
import tools.SetupData;
import ui.images.Images;

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
	/** Wird von {@link #unzoom()}, {@link #inputXChanged()} und {@link #inputYChanged()} temporär auf <code>true</code> gesetzt, um Benachrichtigungsschleifen zu verhindern */
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
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Copy"),Language.tr("CalculatorDialog.Plotter.Toolbar.Copy.Hint"),Images.EDIT_COPY.getIcon(),e->copyToClipboard());
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Print"),Language.tr("CalculatorDialog.Plotter.Toolbar.Print.Hint"),Images.GENERAL_PRINT.getIcon(),e->print());
		addToolbarIcon(toolbar,Language.tr("CalculatorDialog.Plotter.Toolbar.Save"),Language.tr("CalculatorDialog.Plotter.Toolbar.Save.Hint"),Images.GENERAL_SAVE.getIcon(),e->save());

		/* Panel mit Zoomfeldern */
		final JPanel outer=new JPanel(new BorderLayout());
		add(outer);
		final JToolBar left=new JToolBar();
		left.setOrientation(SwingConstants.VERTICAL);
		left.setFloatable(false);
		outer.add(left,BorderLayout.WEST);
		left.add(inputMaxY=new JTextField(5));
		left.add(Box.createVerticalGlue());
		left.add(inputMinY=new JTextField(5));
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
		bottom.add(Box.createHorizontalGlue());
		bottom.add(inputMaxX=new JTextField(5));

		inputMaxX.setMaximumSize(inputMaxX.getPreferredSize());
		inputMinX.setMaximumSize(inputMinX.getPreferredSize());
		inputMinX.addKeyListener(new KeyAdapter() {@Override public void keyReleased(KeyEvent e) {inputXChanged();}});
		inputMaxX.addKeyListener(new KeyAdapter() {@Override public void keyReleased(KeyEvent e) {inputXChanged();}});

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
	 * Kopiert die aktuelle Darstellung in die Zwischenablage.
	 */
	public void copyToClipboard() {
		final SetupData setup=SetupData.getSetup();
		ImageTools.copyImageToClipboard(ImageTools.drawToImage(chart,setup.imageSize,setup.imageSize));
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
			pjob.print();
			return true;
		} catch (Exception e) {return false;}
	}

	/**
	 * Fragt einen Dateinamen ab und speichert die aktuelle Darstellung
	 * unter dem gewählten Namen.
	 * @return	Gibt an, ob die Darstellung gespeichert werden konnte. (Gründe für <code>false</code> können sowohl Fehler aus auch nutzerseitige Abbrüche sein.)
	 * @see #save(File)
	 */
	public boolean save() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveImage);
		final FileFilter jpg=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeJPG+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeGIF+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePNG+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeBMP+" (*.bmp)","bmp");
		final FileFilter tiff=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeTIFF+" (*.tiff, *.tif)","tiff","tif");
		final FileFilter docx=new FileNameExtensionFilter(StatisticsBasePanel.fileTypeWordWithImage+" (*.docx)","docx");
		final FileFilter pdf=new FileNameExtensionFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		if (ImageIOFormatCheck.hasTIFF()) fc.addChoosableFileFilter(tiff);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pdf);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==tiff) file=new File(file.getAbsoluteFile()+".tiff");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
		}

		if (!save(file)) {
			MsgBox.error(this,StatisticsBasePanel.viewersSaveImageErrorTitle,String.format(StatisticsBasePanel.viewersSaveImageErrorInfo,file.toString()));
			return false;
		}

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
	 * Speichert die Darstellung unter dem angegebenen Namen.
	 * Existiert die Ausgabedatei bereits, so wird diese überschrieben.
	 * @param file	Dateiname unter dem die Darstellung gespeichert werden soll.
	 * @return	Gibt an, ob das Speichern erfolgreich ausgeführt werden konnte.
	 * @see #save()
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
			final PDFWriter pdf=new PDFWriter(this,15,10);
			if (!pdf.systemOK) return false;
			if (!pdf.writeImage(image,25)) return false;
			return pdf.save(file);
		}

		try {return ImageIO.write(image,s,file);} catch (IOException e) {return false;}
	}

	/**
	 * Wird aufgerufen, wenn der Nutzer den minimalen oder den maximalen X-Wert verändert.
	 */
	private void inputXChanged() {
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
		inputMinY.setText(NumberTools.formatNumber(minY));
		inputMaxY.setText(NumberTools.formatNumber(maxY));

		final XYItemRenderer renderer=plot.getRenderer();
		for (Graph graph: graphs) {
			XYSeries series=graph.getSeries(minX,maxX,steps);
			if (series==null) continue;
			data.addSeries(series);
			renderer.setSeriesPaint(data.getSeriesCount()-1,graph.color);
		}

		justChangingZoom=true;
		try {
			plot.getDomainAxis().setRange(minX,maxX);
			plot.getRangeAxis().setRange(minY,maxY);
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
			plot.getRangeAxis().setRange(minY,maxY);
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
	 * Lädt die Graphen nach einer Änderung der Daten neu in die Darstellung.
	 * Diese Methode muss manuell aufgerufen werden, das Panel kann Änderungen
	 * an der Liste der Graphen nicht automatisch erkennen.
	 * @see #getGraphs()
	 */
	public void reload() {
		inputMinX.setText("-10");
		inputMaxX.setText("10");
		inputXChanged();
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
	 * @see #reload()
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
			if (expression==null || expression.trim().isEmpty()) return null;
			final ExpressionCalc calc=new ExpressionCalc(variableName);
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

			final XYSeries series=new XYSeries(expression);

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