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
package mathtools;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarGrouping;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLegendPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.chart.STOrientation;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import mathtools.Table.IndexMode;

/**
 * Diese Klasse stellt Basisfunktionen zum Einfügen eines Diagramms in ein
 * Excel-Sheet ({@link Sheet}), welches Daten basierend auf einer
 * {@link Table} enthält, bereit.
 * @author Alexander Herzog
 * @version 1.0
 */
public abstract class TableChartBase {
	/**
	 * Tabelle, die die Daten enthält.<br>
	 * (Wird bereits im Konstruktor übergeben)
	 */
	private final Table table;

	/**
	 * Zuordnung von Zeichenflächen zu Tabellenblättern.
	 * @see #createChart(String, Sheet, Point, Dimension)
	 */
	private final Map<Sheet,XSSFDrawing> drawing;

	/**
	 * Diagramm-Objekt in der Tabelle.<br>
	 * (Nur während des Aufrufs von {@link #build(String, int, Sheet, String)} gültig.)
	 */
	private CTChart chart;

	/**
	 * Zeichenfläche innerhalb des Diagramm-Objekts.<br>
	 * (Nur während des Aufrufs von {@link #build(String, int, Sheet, String)} gültig.)
	 */
	private CTPlotArea plotArea;

	/**
	 * Titel der x-Achse
	 * @see #setupAxis(String, String)
	 */
	private String catAxisTitle;

	/**
	 * Titel der y-Achse
	 * @see #setupAxis(String, String)
	 */
	private String valAxisTitle;

	/**
	 * Minimalwert für die y-Achse (kann <code>null</code> sein, dann erfolgt die Festlegung implizit.)
	 * @see #setupAxisMinY(double)
	 */
	private Double valAxisMin;

	/**
	 * Maximalwert für die y-Achse (kann <code>null</code> sein, dann erfolgt die Festlegung implizit.)
	 * @see #setupAxisMaxY(double)
	 */
	private Double valAxisMax;

	/**
	 * Konstruktor der Klasse
	 * @param table	Tabelle, die die Daten enthält
	 */
	public TableChartBase(final Table table) {
		this.table=table;
		drawing=new HashMap<>();
	}

	/**
	 * Liefert die Größe der Datentabelle
	 * @return	Größe der Datentabelle
	 */
	protected final Dimension getTableSize() {
		if (table.getMode()==IndexMode.COLS) {
			return new Dimension(table.getSize(0),table.getSize(1));
		} else {
			return new Dimension(table.getSize(1),table.getSize(0));
		}
	}

	/**
	 * Erstellt das Diagramm-Objekt
	 * @param chartTitle	Titel des Diagramms
	 * @param sheet	Tabellenblatt in das das Diagramm eingebettet werden soll
	 * @param start	0-basierte Startposition des Diagramms
	 * @param size	Größe des Diagramms
	 * @return	Liefert das neue Diagramm-Objekt zurück
	 */
	private CTChart createChart(final String chartTitle, final Sheet sheet, final Point start, final Dimension size) {
		XSSFDrawing draw=drawing.get(sheet);
		if (draw==null) drawing.put(sheet,draw=(XSSFDrawing)sheet.createDrawingPatriarch());

		final ClientAnchor anchor=draw.createAnchor(0,0,0,0,start.x,start.y,start.x+size.width,start.y+size.height);
		final XSSFChart chart=draw.createChart(anchor);

		if (chartTitle!=null && !chartTitle.isEmpty()) chart.setTitleText(chartTitle);

		if (chart.getCTChartSpace().getRoundedCorners()==null) chart.getCTChartSpace().addNewRoundedCorners();
		chart.getCTChartSpace().getRoundedCorners().setVal(false);

		return chart.getCTChart();
	}

	/**
	 * Für eine Legende in das aktuelle Diagramm ein.<br>
	 * Darf in {@link #buildChart(CTPlotArea, String, int)} und davon aufgerufenen
	 * Methoden aufgerufen werden.
	 */
	protected final void addLegend() {
		if (chart==null) return;
		final CTLegend ctLegend=chart.addNewLegend();
		ctLegend.addNewLegendPos().setVal(STLegendPos.B);
		ctLegend.addNewOverlay().setVal(false);
	}

	/**
	 * Legt Achsen im Diagramm an.<br>
	 * Darf in {@link #buildChart(CTPlotArea, String, int)} und davon aufgerufenen
	 * Methoden aufgerufen werden.
	 * @param catAxisId	ID der x-Achse
	 * @param valAxisId	ID der y-Achse
	 */
	protected final void addAxis(final int catAxisId, final int valAxisId) {
		CTScaling ctScaling;

		/* Category axis */
		final CTCatAx ctCatAx=plotArea.addNewCatAx();
		ctCatAx.addNewAxId().setVal(catAxisId);
		ctScaling=ctCatAx.addNewScaling();
		ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
		ctCatAx.addNewDelete().setVal(false);
		ctCatAx.addNewAxPos().setVal(STAxPos.B);
		ctCatAx.addNewCrossAx().setVal(valAxisId);
		ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
		if (catAxisTitle!=null && !catAxisTitle.isEmpty()) {
			final CTTitle ctTitle=ctCatAx.addNewTitle();
			ctTitle.addNewLayout();
			ctTitle.addNewOverlay().setVal(false);
			final CTTextBody rich=ctTitle.addNewTx().addNewRich();
			rich.addNewBodyPr();
			rich.addNewLstStyle();
			CTTextParagraph p = rich.addNewP();
			p.addNewPPr().addNewDefRPr();
			p.addNewR().setT(catAxisTitle);
			p.addNewEndParaRPr();
		}

		/* Value axis */
		final CTValAx ctValAx=plotArea.addNewValAx();
		ctValAx.addNewAxId().setVal(valAxisId);
		ctScaling=ctValAx.addNewScaling();
		ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
		ctValAx.addNewDelete().setVal(false);
		ctValAx.addNewAxPos().setVal(STAxPos.L);
		ctValAx.addNewCrossAx().setVal(catAxisId);
		ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);
		if (valAxisMin!=null) ctValAx.addNewScaling().addNewMin().setVal(valAxisMin);
		if (valAxisMax!=null) ctValAx.addNewScaling().addNewMax().setVal(valAxisMax);

		if (catAxisTitle!=null && !catAxisTitle.isEmpty()) {
			final CTTitle ctTitle=ctValAx.addNewTitle();
			ctTitle.addNewLayout();
			ctTitle.addNewOverlay().setVal(false);
			final CTTextBody rich=ctTitle.addNewTx().addNewRich();
			rich.addNewBodyPr();
			rich.addNewLstStyle();
			CTTextParagraph p = rich.addNewP();
			p.addNewPPr().addNewDefRPr();
			p.addNewR().setT(valAxisTitle);
			p.addNewEndParaRPr();
		}
	}

	/**
	 * Liefert die Startposition (0-basierend) für das Diagramm
	 * @param chartYIndex	0-basierter Index für die vertikale Anordnung des Diagramms
	 * @return	Startposition des Diagramms
	 */
	protected abstract Point getChartPosition(final int chartYIndex);

	/**
	 * Liefert die Größe des Diagramms
	 * @return	Größe des Diagramms
	 */
	protected abstract Dimension getChartDimension();

	/**
	 * Fügt ein Liniendiagramm in das Excel-Sheet ein.<br>
	 * Darf in {@link #buildChart(CTPlotArea, String, int)} und davon aufgerufenen
	 * Methoden aufgerufen werden.
	 * @param sheetName	Name des Tabellenblattes in der Mappe
	 * @param series	Datenserien für das Diagramm
	 * @param seriesNames	Namen der Datenserien
	 * @param categories	Kategorien für die x-Achse (darf <code>null</code> sein)
	 */
	protected final void buildLineChart(final String sheetName, final List<Range> series, final List<Object> seriesNames, final Range categories) {
		if (series==null || seriesNames==null) return;
		final int seriesCount=Math.min(series.size(),seriesNames.size());

		final CTLineChart ctLineChart=plotArea.addNewLineChart();

		/* Verschiedene Farben? */
		final CTBoolean ctBoolean=ctLineChart.addNewVaryColors();
		ctBoolean.setVal(seriesCount>1);

		CTStrRef ctStrRef;

		for (int i=0;i<seriesCount;i++) {
			final CTLineSer ctLineSer=ctLineChart.addNewSer();

			/* Kein Glätten */
			final CTBoolean ctBool=CTBoolean.Factory.newInstance();
			ctBool.setVal(false);
			ctLineSer.setSmooth(ctBool);

			/* Keine Marker */
			final CTMarker ctMarker=CTMarker.Factory.newInstance();
			CTMarkerStyle starMarkerStyle = CTMarkerStyle.Factory.newInstance();
			starMarkerStyle.setVal(STMarkerStyle.NONE);
			ctMarker.setSymbol(starMarkerStyle);
			ctLineSer.setMarker(ctMarker);

			/* Name für Datenreihe */
			final CTSerTx ctSerTx=ctLineSer.addNewTx();
			ctStrRef=ctSerTx.addNewStrRef();
			final Object name=seriesNames.get(i);
			if (name instanceof String) ctStrRef.setF("=\""+((String)name)+"\"");
			if (name instanceof Cell) ctStrRef.setF(sheetName+"!"+((Cell)name).toString());

			/* ID */
			ctLineSer.addNewIdx().setVal(i); /* Zählung muss ab 0 beginnen */

			/* Kategorienbezeichnungen */
			if (i==0 && categories!=null) {
				CTAxDataSource cttAxDataSource=ctLineSer.addNewCat();
				ctStrRef=cttAxDataSource.addNewStrRef();
				ctStrRef.setF(sheetName+"!"+categories);
			}

			/* Daten */
			CTNumDataSource ctNumDataSource = ctLineSer.addNewVal();
			CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
			ctNumRef.setF(sheetName+"!"+series.get(i));
		}

		/* Achsen */
		ctLineChart.addNewAxId().setVal(998);
		ctLineChart.addNewAxId().setVal(999);
		addAxis(998,999);

		/* Legende */
		addLegend();
	}

	/**
	 * Fügt ein Balkendiagramm in das Excel-Sheet ein.<br>
	 * Darf in {@link #buildChart(CTPlotArea, String, int)} und davon aufgerufenen
	 * Methoden aufgerufen werden.
	 * @param sheetName	Name des Tabellenblattes in der Mappe
	 * @param series	Datenserien für das Diagramm
	 * @param seriesNames	Namen der Datenserien
	 * @param categories	Kategorien für die x-Achse (darf <code>null</code> sein)
	 */
	protected final void buildBarChart(final String sheetName, final List<Range> series, final List<Object> seriesNames, final Range categories) {
		if (series==null || seriesNames==null) return;
		final int seriesCount=Math.min(series.size(),seriesNames.size());

		final CTBarChart ctBarChart=plotArea.addNewBarChart();

		/* Verschiedene Farben? */
		final CTBoolean ctBoolean=ctBarChart.addNewVaryColors();
		ctBoolean.setVal(seriesCount>1);

		/* Darstellung */
		ctBarChart.addNewBarDir().setVal(STBarDir.COL); /* Vertikal */
		ctBarChart.addNewGrouping().setVal(STBarGrouping.STACKED);
		ctBarChart.addNewOverlap().setVal((byte)100); /* Stapel nicht nebeneinander */

		CTStrRef ctStrRef;

		for (int i=0;i<seriesCount;i++) {
			final CTBarSer ctBarSer=ctBarChart.addNewSer();

			/* Name für Datenreihe */
			final CTSerTx ctSerTx=ctBarSer.addNewTx();
			ctStrRef=ctSerTx.addNewStrRef();
			final Object name=seriesNames.get(i);
			if (name instanceof String) ctStrRef.setF("=\""+((String)name)+"\"");
			if (name instanceof Cell) ctStrRef.setF(sheetName+"!"+((Cell)name).toString());

			/* ID */
			ctBarSer.addNewIdx().setVal(i); /* Zählung muss ab 0 beginnen */

			/* Kategorienbezeichnungen */
			if (categories!=null) {
				final CTAxDataSource cttAxDataSource=ctBarSer.addNewCat();
				ctStrRef=cttAxDataSource.addNewStrRef();
				ctStrRef.setF(sheetName+"!"+categories.toString());
			}

			/* Daten */
			final CTNumDataSource ctNumDataSource=ctBarSer.addNewVal();
			final CTNumRef ctNumRef=ctNumDataSource.addNewNumRef();
			ctNumRef.setF(sheetName+"!"+series.get(i).toString());
		}

		/* Achsen */
		ctBarChart.addNewAxId().setVal(998);
		ctBarChart.addNewAxId().setVal(999);
		addAxis(998,999);

		/* Legende */
		addLegend();
	}

	/**
	 * Fügt ein Balkendiagramm in das Excel-Sheet ein.<br>
	 * Darf in {@link #buildChart(CTPlotArea, String, int)} und davon aufgerufenen
	 * Methoden aufgerufen werden.
	 * @param sheetName	Name des Tabellenblattes in der Mappe
	 * @param series	Datenserien für das Diagramm
	 * @param seriesNames	Namen der Datenserien
	 * @param categories	Kategorien für die x-Achse (darf <code>null</code> sein)
	 */
	protected final void buildPieChart(final String sheetName, final List<Range> series, final List<Object> seriesNames, final Range categories) {
		if (series==null || seriesNames==null) return;
		final int seriesCount=Math.min(series.size(),seriesNames.size());

		final CTPieChart ctPieChart=plotArea.addNewPieChart();

		/* Verschiedene Farben? */
		final CTBoolean ctBoolean=ctPieChart.addNewVaryColors();
		ctBoolean.setVal(true);

		CTStrRef ctStrRef;

		for (int i=0;i<seriesCount;i++) {
			final CTPieSer ctPieSer=ctPieChart.addNewSer();

			/* Name für Datenreihe */
			final CTSerTx ctSerTx=ctPieSer.addNewTx();
			ctStrRef=ctSerTx.addNewStrRef();
			final Object name=seriesNames.get(i);
			if (name instanceof String) ctStrRef.setF("=\""+((String)name)+"\"");
			if (name instanceof Cell) ctStrRef.setF(sheetName+"!"+((Cell)name).toString());

			/* ID */
			ctPieSer.addNewIdx().setVal(i); /* Zählung muss ab 0 beginnen */

			/* Kategorienbezeichnungen */
			if (categories!=null) {
				final CTAxDataSource cttAxDataSource=ctPieSer.addNewCat();
				ctStrRef=cttAxDataSource.addNewStrRef();
				ctStrRef.setF(sheetName+"!"+categories.toString());
			}

			/* Daten */
			final CTNumDataSource ctNumDataSource=ctPieSer.addNewVal();
			final CTNumRef ctNumRef=ctNumDataSource.addNewNumRef();
			ctNumRef.setF(sheetName+"!"+series.get(i).toString());
		}

		/* Legende */
		addLegend();
	}

	/**
	 * Konfiguriert die Achsenbeschriftungen für das Diagramm
	 * @param catAxisTitle	Optionaler Titel der x-Achse (kann <code>null</code> sein, dann wird keine Achsenbeschriftung angelegt)
	 * @param valAxisTitle	Optionaler Titel der y-Achse (kann <code>null</code> sein, dann wird keine Achsenbeschriftung angelegt)
	 */
	public final void setupAxis(final String catAxisTitle, final String valAxisTitle) {
		this.catAxisTitle=catAxisTitle;
		this.valAxisTitle=valAxisTitle;
	}

	/**
	 * Legt einen Minimalwert für den darzustellenden Zahlenbereich auf der y-Achse
	 * @param minY	Minimalwert für den darzustellenden Zahlenbereich auf der y-Achse
	 */
	public final void setupAxisMinY(final double minY) {
		valAxisMin=minY;
	}

	/**
	 * Legt einen Maximalwert für den darzustellenden Zahlenbereich auf der y-Achse
	 * @param maxY	Maximalwert für den darzustellenden Zahlenbereich auf der y-Achse
	 */
	public final void setupAxisMaxY(final double maxY) {
		valAxisMax=maxY;
	}

	/**
	 * Erstellt das Diagramm innerhalb eines {@link CTPlotArea}.
	 * @param plotArea	Zeichenbereich
	 * @param sheetName	Name des Tabellenblattes in der Mappe
	 * @param chartYIndex	0-basierter Index für die vertikale Anordnung des Diagramms
	 */
	protected abstract void buildChart(final CTPlotArea plotArea, final String sheetName, final int chartYIndex);

	/**
	 * Fügt vorab konfiguriertes Diagramm in ein {@link Sheet} ein.
	 * @param chartTitle	Optionale Überschrift des Diagramms (kann <code>null</code> sein, dann wird keine Überschrift ausgegeben)
	 * @param chartYIndex	0-basierter Index für die vertikale Anordnung des Diagramms
	 * @param sheet	Tabellenblatt, in das die Ausgabe erfolgen soll
	 * @param sheetName	Name des Tabellenblattes in der Mappe
	 * @see #setupAxis(String, String)
	 */
	public final void build(final String chartTitle, int chartYIndex, final Sheet sheet, final String sheetName) {
		chartYIndex=Math.max(0,chartYIndex);
		chart=createChart(chartTitle,sheet,getChartPosition(chartYIndex),getChartDimension());
		plotArea=chart.getPlotArea();

		buildChart(plotArea,sheetName,chartYIndex);

		chart=null;
		plotArea=null;
		catAxisTitle=null;
		valAxisTitle=null;
	}

	/**
	 * Erstellt eine Excel-Datei mit Daten aus der Tabelle und einem Diagramm.
	 * @param chartTitle	Optionale Überschrift des Diagramms (kann <code>null</code> sein, dann wird keine Überschrift ausgegeben)
	 * @param file	Ausgabedateiname
	 * @return	Gibt an, ob das Speichern erfolgreich war
	 */
	public final boolean save(final String chartTitle, final File file) {
		try (Workbook wb=new XSSFWorkbook()) {
			final Sheet sheet=wb.createSheet(Table.TableFileTableName);

			table.saveToSheet(wb,sheet);
			build(chartTitle,0,sheet,Table.TableFileTableName);

			try (FileOutputStream fo=new FileOutputStream(file)) {wb.write(fo);}
		} catch (IOException e) {return false;}

		return true;
	}

	/**
	 * Bezeichnet eine Zelle in der Tabelle
	 * @author Alexander Herzog
	 */
	public static class Cell {
		/**
		 * Zelle als 0-basierende Zahlenwerte
		 */
		public final Point point;

		/**
		 * Konstruktor der Klasse
		 * @param x	0-basierende Spalte
		 * @param y	0-basierende Zeile
		 */
		public Cell(final int x, final int y) {
			point=new Point(x,y);
		}

		/**
		 * Konstruktor der Klasse
		 * @param point	Zelle als 0-basierende Zahlenwerte
		 */
		public Cell(final Point point) {
			this.point=(point==null)?new Point(0,0):new Point(point);
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param cell	Zu kopierendes Ausgangsobjekt
		 */
		public Cell(final Cell cell) {
			point=(cell==null)?new Point(0,0):new Point(cell.point);
		}

		/**
		 * Liefert die Zelle in Excel-Notation
		 */
		@Override
		public String toString() {
			return Table.columnNameFromNumber(point.x)+(point.y+1);
		}
	}

	/**
	 * Bezeichnet einen Bereich einer Tabelle
	 * @author Alexander Herzog
	 */
	public static class Range {
		/**
		 * Startzelle als 0-basierende Zahlenwerte
		 */
		public final Point start;

		/**
		 * Endzelle als 0-basierende Zahlenwerte
		 */
		public final Point end;

		/**
		 * Konstruktor der Klasse
		 * @param start	Startzelle als 0-basierende Zahlenwerte
		 * @param end	Endzelle als 0-basierende Zahlenwerte
		 */
		public Range(final Point start, final Point end) {
			this.start=(start==null)?new Point(0,0):new Point(start);
			this.end=(end==null)?new Point(0,0):new Point(end);
		}

		/**
		 * Konstruktor der Klasse
		 * @param startX	0-basierende Spalte der Startzelle
		 * @param startY	0-basierende Zeile der Startzelle
		 * @param endX	0-basierende Spalte der Endzelle
		 * @param endY	0-basierende Zeile der Endzelle
		 */
		public Range(final int startX, final int startY, final int endX, final int endY) {
			start=new Point(startX,startY);
			end=new Point(endX,endY);
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param range	Zu kopierendes Ausgangsobjekt
		 */
		public Range(final Range range) {
			start=(range==null)?new Point(0,0):new Point(range.start);
			end=(range==null)?new Point(0,0):new Point(range.end);
		}

		/**
		 * Liefert den Bereich in Excel-Notation
		 */
		@Override
		public String toString() {
			return start()+":"+end();
		}

		/**
		 * Liefer die erste Zelle des Bereichs
		 * @return	Erste Zelle des Bereichs
		 * @see #toString()
		 */
		public String start() {
			return Table.columnNameFromNumber(start.x)+(start.y+1);
		}

		/**
		 * Liefert die letzte Zelle des Bereichs
		 * @return	Letzte Zelle des Bereichs
		 * @see #toString()
		 */
		public String end() {
			return Table.columnNameFromNumber(end.x)+(end.y+1);
		}
	}
}