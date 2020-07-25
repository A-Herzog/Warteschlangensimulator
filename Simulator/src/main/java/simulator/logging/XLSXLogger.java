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
package simulator.logging;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import mathtools.NumberTools;
import simcore.SimData;
import simcore.logging.SimLogging;

/**
 * Schreibt die Logging-Daten in eine XLSX-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 */
public class XLSXLogger implements SimLogging {
	private final File logFile;
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useColors;
	private final boolean formatedTime;
	private long lastEventTime=-1;

	private final Workbook workbook;
	private final Sheet sheet;
	private int rowCount=0;
	private CellStyle defaultStyle;
	private CellStyle defaultStyleBold;
	private Map<Color,CellStyle> style;
	private Map<Color,CellStyle> styleBold;

	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>XLSXLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param headings	Auszugebende Überschriftzeilen
	 * @param oldFileFormat	Gibt an, ob eine XLSX- (<code>false</code>) oder eine alte XLS-Datei (<code>true</code>) erzeugt werden soll.
	 */
	public XLSXLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final String[] headings, final boolean oldFileFormat) {
		this.logFile=logFile;
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		this.formatedTime=formatedTime;

		String[] h;
		if (headings==null || headings.length==0) h=new String[]{"Simulationsergebnisse"}; else h=headings;

		if (oldFileFormat) {
			workbook=new HSSFWorkbook();
			sheet=workbook.createSheet(h[0]);
		} else {
			workbook=new XSSFWorkbook();
			sheet=workbook.createSheet(h[0]);
		}

		Row row=sheet.createRow(rowCount); rowCount++;
		Cell cell=row.createCell(0);

		CellStyle style=workbook.createCellStyle();
		Font font=workbook.createFont();
		font.setFontHeightInPoints((short)(font.getFontHeightInPoints()+4));
		if (font instanceof XSSFFont) ((XSSFFont)font).setBold(true);
		style.setFont(font);
		cell.setCellStyle(style);
		cell.setCellValue(h[0]);

		style=workbook.createCellStyle();
		font=workbook.createFont();
		font.setFontHeightInPoints((short)(font.getFontHeightInPoints()+2));
		if (font instanceof XSSFFont) ((XSSFFont)font).setBold(true);
		style.setFont(font);
		for (int i=1;i<h.length;i++) {
			row=sheet.createRow(rowCount); rowCount++;
			cell=row.createCell(0);
			cell.setCellStyle(style);
			cell.setCellValue(h[i]);
		}
		/* Leerzeile */ rowCount++;

		/* Styles vorbereiten */

		defaultStyleBold=workbook.createCellStyle();
		font=workbook.createFont();
		if (font instanceof XSSFFont) ((XSSFFont)font).setBold(true);
		defaultStyleBold.setFont(font);

		defaultStyle=workbook.createCellStyle();

		this.style=new HashMap<>();
		styleBold=new HashMap<>();
	}

	@Override
	public boolean ready() {
		return true;
	}

	private CellStyle getCellStyle(Color color, final boolean bold) {
		if (!useColors) return bold?defaultStyleBold:defaultStyle;

		Map<Color,CellStyle> list=bold?styleBold:style;

		if (color==null) color=Color.BLACK;
		CellStyle style=list.get(color);
		if (style!=null) return style;

		style=workbook.createCellStyle();
		if (style instanceof XSSFCellStyle) {
			((XSSFCellStyle)style).setFillForegroundColor(new XSSFColor(new byte[]{(byte)color.getRed(),(byte)color.getGreen(),(byte)color.getBlue()},null));
			((XSSFCellStyle)style).setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		Font font=workbook.createFont();
		if (font instanceof XSSFFont) ((XSSFFont)font).setColor(new XSSFColor(new byte[]{(byte)(255-color.getRed()),(byte)(255-color.getGreen()),(byte)(255-color.getBlue())},null));
		if (bold && font instanceof XSSFFont) ((XSSFFont)font).setBold(true);

		style.setFont(font);

		list.put(color,style);
		return style;
	}

	@Override
	public boolean log(long time, Color color, String event, String info) {
		final String timeString=formatedTime?SimData.formatSimTime(time):NumberTools.formatNumber(time/1000.0);

		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if (lastEventTime!=time) {
				if (lastEventTime>=0) /* Leerzeile */ rowCount++;
				Row row=sheet.createRow(rowCount); rowCount++;
				Cell cell=row.createCell(0);
				cell.setCellStyle(defaultStyleBold);
				cell.setCellValue(timeString);
				/* Leerzeile */ rowCount++;
				lastEventTime=time;
			}
		}

		/* Daten ausgeben */
		if (singleLineMode) {
			Row row=sheet.createRow(rowCount); rowCount++;
			int colCount=0;
			Cell cell;
			if (!groupSameTimeEvents) {
				cell=row.createCell(colCount); colCount++;
				cell.setCellStyle(getCellStyle(color,false));
				cell.setCellValue(timeString);
			}
			if (event!=null && !event.isEmpty()) {
				cell=row.createCell(colCount); colCount++;
				cell.setCellStyle(getCellStyle(color,true));
				cell.setCellValue(event);
			}
			if (info!=null && !info.isEmpty()) {
				cell=row.createCell(colCount); colCount++;
				cell.setCellStyle(getCellStyle(color,false));
				cell.setCellValue(info);
			}
		} else {
			Row row=sheet.createRow(rowCount); rowCount++;
			int colCount=0;
			Cell cell;
			if (!groupSameTimeEvents) {
				cell=row.createCell(colCount); colCount++;
				cell.setCellStyle(getCellStyle(color,false));
				cell.setCellValue(timeString);
			}
			if (event!=null && !event.isEmpty()) {
				cell=row.createCell(colCount); colCount++;
				cell.setCellStyle(getCellStyle(color,true));
				cell.setCellValue(event);
			}
			if (info!=null && !info.isEmpty()) {
				if (event!=null && !event.isEmpty()) {
					row=sheet.createRow(rowCount); rowCount++;
					colCount--;
				}
				cell=row.createCell(colCount); colCount++;
				cell.setCellStyle(getCellStyle(color,false));
				cell.setCellValue(info);
			}
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,info);

		return true;
	}

	@Override
	public boolean done() {
		if (nextLogger!=null) nextLogger.done();

		try {
			try (FileOutputStream fo=new FileOutputStream(logFile)) {
				workbook.write(fo);
			}
		} catch (IOException e) {return false;}
		return true;
	}

	@Override
	public void setNextLogger(final SimLogging logger) {
		nextLogger=logger;
	}

	@Override
	public SimLogging getNextLogger() {
		return nextLogger;
	}
}