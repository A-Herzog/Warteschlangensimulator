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
package systemtools.statistics;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt eine Implementierung des {@link StatisticViewer}-Interfaces zur
 * Anzeige von Tabellen dar.
 * @author Alexander Herzog
 * @see StatisticViewer
 * @version 1.5
 */
public class StatisticViewerTable implements StatisticViewer {
	/**
	 * Tabelle mit Daten (kann <code>null</code> sein wenn die Daten nicht über ein {@link Table}-Objekt geladen werden)
	 */
	private Table table;

	/**
	 * Inhalt der Tabelle (kann <code>null</code> sein, wenn die Daten noch nicht aus der Tabelle ausgelesen wurden)
	 * @see #initData()
	 */
	private List<List<String>> data;

	/**
	 * Spaltentitel der Tabelle
	 */
	protected List<String> columnNames;

	/**
	 * html-Seite mit einer zusätzlichen Erklärung zu dieser Statistikseite
	 * @see #addDescription(URL, Consumer)
	 */
	private URL descriptionURL=null;

	/**
	 * Handler, der Themennamen (angegeben über "help:..."-Links) zum Aufruf normaler Hilfeseiten entgegen nimmt
	 * @see #addDescription(URL, Consumer)
	 */
	private Consumer<String> descriptionHelpCallback=null;

	/**
	 * Darstellung der Hilfe-Seite {@link #descriptionURL}
	 * @see #initDescriptionPane()
	 * @see #addDescription(URL, Consumer)
	 */
	private DescriptionViewer descriptionPane=null;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * (Wird dieser Konstruktor verwendet, so müssen die Daten später per <code>setData</code> gesetzt werden.)
	 * @see #setData(Table, List)
	 * @see #setData(List, List)
	 * @see #setData(String[][], String[])
	 * @see #setData(Table, String[])
	 */
	public StatisticViewerTable() {
		table=null;
		data=new ArrayList<>();
		columnNames=new ArrayList<>();
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param data	List aus List-Objekten, die die anzuzeigenden Texte enthalten
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(List<List<String>> data, List<String> columnNames) {
		setData(data,columnNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param arrayData	Doppeltes Strings-Array, die die anzuzeigenden Texte enthalten
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(String[][] arrayData, String[] arrayColumnNames) {
		setData(arrayData,arrayColumnNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param data	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(Table data, String[] arrayColumnNames) {
		setData(data,arrayColumnNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param data	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(Table data, List<String> columnNames) {
		setData(data,columnNames);
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param data	List aus List-Objekten, die die anzuzeigenden Texte enthalten
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(List<List<String>> data, List<String> columnNames) {
		table=null;
		this.data=data;
		this.columnNames=columnNames;
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param arrayData	Doppeltes Strings-Array, die die anzuzeigenden Texte enthalten
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(String[][] arrayData, String[] arrayColumnNames) {
		table=null;
		data=new ArrayList<>();
		for (int i=0;i<arrayData.length;i++) {
			List<String> v=new ArrayList<>(); data.add(v);
			for (int j=0;j<arrayData[i].length;j++) v.add(arrayData[i][j]);
		}

		columnNames=new ArrayList<>();
		for (int i=0;i<arrayColumnNames.length;i++) columnNames.add(arrayColumnNames[i]);
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param table	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(Table table, String[] arrayColumnNames) {
		setData(table,Arrays.asList(arrayColumnNames));
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param table	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(Table table, List<String> columnNames) {
		this.table=table;
		data=null;
		this.columnNames=columnNames;
	}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_TABLE;
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_NOIMAGE;
	}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_UNZOOM: return false;
		case CAN_DO_COPY: return true;
		case CAN_DO_PRINT: return true;
		case CAN_DO_SAVE: return true;
		default: return false;
		}
	}

	@Override
	public void unZoom() {}

	@Override
	public JButton[] getAdditionalButton() {
		final boolean excel=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.EXCEL);
		final boolean ods=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.ODS);
		final boolean pdf=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.PDF);

		int count=0;
		if (excel) count++;
		if (ods) count++;
		if (pdf) count++;

		if (count>1) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarOpenTable);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarOpenTableHint);
			button.setIcon(SimToolsImages.OPEN.getIcon());
			button.addActionListener(e->{
				final JPopupMenu menu=new JPopupMenu();
				JMenuItem item;
				if (excel) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarExcel));
					item.setIcon(SimToolsImages.SAVE_TABLE_EXCEL.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarExcelHint);
					item.addActionListener(ev->openExcel());
				}
				if (ods) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarODS));
					item.setIcon(SimToolsImages.SAVE_TABLE.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarODSHint);
					item.addActionListener(ev->openODS());
				}
				if (pdf) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarPDF));
					item.setIcon(SimToolsImages.SAVE_PDF.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarPDFHint);
					item.addActionListener(ev->openPDF(SwingUtilities.getWindowAncestor(viewer)));
				}
				menu.show(button,0,button.getHeight());

			});
			return new JButton[]{button};
		}

		if (excel) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarExcel);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarExcelHint);
			button.setIcon(SimToolsImages.SAVE_TABLE_EXCEL.getIcon());
			button.addActionListener(e->openExcel());
			return new JButton[]{button};
		}

		if (ods) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarODS);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarODSHint);
			button.setIcon(SimToolsImages.SAVE_TABLE.getIcon());
			button.addActionListener(e->openODS());
			return new JButton[]{button};
		}

		if (pdf) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarPDF);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarPDFHint);
			button.setIcon(SimToolsImages.SAVE_PDF.getIcon());
			button.addActionListener(e->openPDF(SwingUtilities.getWindowAncestor(viewer)));
			return new JButton[]{button};
		}

		return null;
	}

	/**
	 * Öffnet die Tabelle (über eine temporäre Datei) mit Excel
	 */
	private void openExcel() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".xlsx");
			if (toTable().save(file)) {
				file.deleteOnExit();
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Öffnet den Text (über eine temporäre Datei) als pdf
	 * @param owner	Übergeordnete Komponente für die eventuelle Anzeige von Dialogen
	 */
	private void openPDF(final Component owner) {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".pdf");

			final PDFWriter pdf=new PDFWriter(owner,15,10);
			if (!pdf.systemOK) return;
			if (!savePDF(pdf)) return;
			if (!pdf.save(file)) return;

			file.deleteOnExit();
			Desktop.getDesktop().open(file);
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Öffnet die Tabelle (über eine temporäre Datei) mit OpenOffice/LibreOffice
	 */
	private void openODS() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".ods");
			if (toTable().save(file)) {
				file.deleteOnExit();
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Wird unmittelbar vor der ersten Verwendung der Tabelle aufgerufen, sofern die Anzahl der Spalten gleich 0 ist.
	 */
	protected void buildTable() {}

	/**
	 * Initialisiert die Anzeige der zusätzlichen Beschreibung.
	 * @see #addDescription(URL, Consumer)
	 * @see #descriptionURL
	 * @see #descriptionHelpCallback
	 * @see #descriptionPane
	 */
	private void initDescriptionPane() {
		if (descriptionPane!=null) return;
		if (descriptionURL==null) return;

		descriptionPane=new DescriptionViewer(descriptionURL,link->{
			if (link.toLowerCase().startsWith("help:") && descriptionHelpCallback!=null) {
				descriptionHelpCallback.accept(link.substring("help:".length()));
			}
		});
	}

	/**
	 * Konkretes Anzeigeobjekt, das üger {@link #getViewer(boolean)} geliefert wird.
	 * @see #getViewer(boolean)
	 */
	private Container viewer=null;

	/**
	 * Stellt die Breite einer Spalte basierend auf dem Inhalt der Spalte ein.
	 * @param table	Tabelle bei der die Breite einer Spalte eingestellt werden soll.
	 * @param columnIndex	0-basierter Spaltenindex
	 * @param includeHeader	Nur den Inhalt der Spalte (<code>false</code>) oder auch die Spaltenüberschrift (<code>true</code>) in die Berechnung der benötigten Breite mit einbeziehen.
	 */
	private void autoSizeColumn(final JTable table, final int columnIndex, final boolean includeHeader) {
		/* Spaltenbreite */
		int widthContent=0;
		final TableModel model=table.getModel();
		final FontMetrics fontMetrics=table.getFontMetrics(table.getFont());
		final int rowCount=table.getRowCount();
		for (int i=0;i<rowCount;i++) {
			widthContent=Math.max(widthContent,SwingUtilities.computeStringWidth(fontMetrics,(String)model.getValueAt(i,columnIndex)));
		}

		/* Überschriftenbreite */
		int widthHeader=0;
		if (includeHeader) {
			final JTableHeader header=table.getTableHeader();
			final String title=table.getColumnName(columnIndex);
			widthHeader=SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()),title);
		}

		/* Neue Breite einstellen */
		setColWidth(table,columnIndex,Math.max(widthContent,widthHeader));
	}

	/**
	 * Liefert die Spaltenabstände.
	 * @param table	Tabelle von der die Spaltenabstände berechnet werden sollen.
	 * @return	Spaltenabstände
	 */
	private int getSpacing(final JTable table) {
		return table.getIntercellSpacing().width+5; /* "+5"=Border+Inset */
	}

	/**
	 * Stellt die Spalte einer Breite ein.
	 * @param table	Tabelle bei der die Breite einer Spalte eingestellt werden soll.
	 * @param columnIndex	0-basierter Spaltenindex
	 * @param width	Neue Spaltenbreite
	 */
	private void setColWidth(final JTable table, final int columnIndex, final int width) {
		/* Abstände zwischen den Zellen */
		final int spacing=getSpacing(table);

		/* Neue Breite einstellen */
		final TableColumnModel columnModel=table.getColumnModel();
		final TableColumn column=columnModel.getColumn(columnIndex);
		column.setMinWidth(30);
		column.setPreferredWidth(width+2*spacing);
		column.setWidth(width+2*spacing);
	}

	@Override
	public Container getViewer(final boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;

		if (columnNames.isEmpty() || needReInit) buildTable();

		final TableModel dataModel;
		if (table==null) {
			dataModel=new StatisticViewerTableModel(data,columnNames);
		} else {
			dataModel=new StatisticViewerTableModel(table,columnNames);
		}

		final JTable table=new JTable(dataModel);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		SwingUtilities.invokeLater(()->{
			for (int i=0;i<table.getColumnCount();i++) autoSizeColumn(table,i,true);
		});

		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final int col=table.columnAtPoint(e.getPoint());
				if (col<0) return;

				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					autoSizeColumn(table,col,true);
					return;
				}

				if (SwingUtilities.isRightMouseButton(e)) {
					final JPopupMenu popup=new JPopupMenu();
					JMenuItem item;

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextColWidthThis+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthDefault));
					item.addActionListener(e2->setColWidth(table,col,50));

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContent));
					item.addActionListener(e2->autoSizeColumn(table,col,false));

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContentAndHeader));
					item.addActionListener(e2->autoSizeColumn(table,col,true));

					popup.addSeparator();

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextColWidthAll+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthDefault));
					item.addActionListener(e2->{
						autoSizeColumn(table,0,false);
						for (int i=1;i<table.getColumnCount();i++) setColWidth(table,i,50);
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByWindowWidth));
					item.addActionListener(e2->{
						if (table.getParent() instanceof JViewport) {
							final JViewport viewport=(JViewport)table.getParent();
							final int w=viewport.getWidth()/table.getColumnCount();
							final int spacing=getSpacing(table);
							for (int i=0;i<table.getColumnCount();i++) setColWidth(table,i,w-2*spacing);
						}
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContent));
					item.addActionListener(e2->{
						for (int i=0;i<table.getColumnCount();i++) autoSizeColumn(table,i,false);
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContentAndHeader));
					item.addActionListener(e2->{
						for (int i=0;i<table.getColumnCount();i++) autoSizeColumn(table,i,true);
					});

					popup.show(e.getComponent(),e.getX(),e.getY());
					return;
				}
			}
		});

		final JScrollPane tableScroller=new JScrollPane(table);

		initDescriptionPane();
		if (descriptionPane==null) return viewer=tableScroller;
		return viewer=descriptionPane.getSplitPanel(tableScroller);
	}

	/**
	 * Fügt eine Textzeile bestehend aus mehreren Spalten zu einem {@link StringBuilder} hinzu.
	 * @param output	{@link StringBuilder}  zu dem die Zeile hinzugefügt werden soll
	 * @param line	Auszugebende Zeile (die Spalten werden durch Tabulatoren getrennt)
	 * @see #copyToClipboard(Clipboard)
	 */
	private void addListToStringBuilder(final StringBuilder output, final List<String> line) {
		final int size=line.size();
		if (size>0) output.append(line.get(0));
		for (int i=1;i<size;i++) {output.append('\t'); output.append(line.get(i));}
		output.append('\n');
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		if (columnNames.isEmpty()) buildTable();
		initData();
		final StringBuilder s=new StringBuilder();
		addListToStringBuilder(s,columnNames);
		final int size=data.size();
		for (int i=0;i<size;i++) addListToStringBuilder(s,data.get(i));
		StringSelection cont=new StringSelection(s.toString());
		clipboard.setContents(cont,null);
	}

	@Override
	public boolean print() {
		if (columnNames.isEmpty()) buildTable();
		initData();
		Table t=new Table();
		t.addLine(columnNames);
		t.addLines(data);
		JTextComponent tc=new JTextArea(t.toString());
		try {tc.print(null,null,true,null,null,true);} catch (PrinterException e) {return false;}
		return true;
	}

	/**
	 * Wandelt die intern vorliegenden Tabellendaten in ein {@link Table}-Objekt um und liefert dieses zurück.
	 * @return	{@link Table}-Objekt, welches die in dem Viewer vorliegenden Tabellendaten enthält
	 */
	public Table toTable() {
		if (columnNames.isEmpty()) buildTable();
		initData();
		final Table t=new Table();
		t.addLine(columnNames);
		t.addLines(data);
		return t;
	}

	@Override
	public void save(Component owner) {
		File file=Table.showSaveDialog(owner,StatisticsBasePanel.viewersSaveTable,null,StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf"); if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		save(owner,file);
	}

	@Override
	public void search(Component owner) {
	}


	@Override
	public boolean save(Component owner, File file) {
		if (columnNames.isEmpty()) buildTable();

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			final PDFWriter pdf=new PDFWriter(owner,15,10);
			if (!pdf.systemOK) return false;
			if (!savePDF(pdf)) return false;
			return pdf.save(file);
		}

		return toTable().save(file);
	}

	/**
	 * Fügt eine einzelne Zeile zu einer {@link BufferedWriter}-Ausgabe hinzu.
	 * @param bw	Ausgabestream, der später zur html-Datei wird
	 * @param line	Auszugebende Datenzeile
	 * @throws IOException	Die Exception wird ausgelöst, wenn die Dateiausgabe nicht durchgeführt werden konnte.
	 * @see #saveHtml(BufferedWriter, File, int, boolean)
	 */
	private void saveLineToTable(BufferedWriter bw, List<String> line) throws IOException {
		bw.write("  <tr>");
		for (int i=0;i<line.size();i++) bw.write("<td>"+line.get(i)+"</td>");
		bw.write("</tr>");
		bw.newLine();
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		if (columnNames.isEmpty()) buildTable();
		initData();
		bw.write("<table>");
		bw.newLine();
		saveLineToTable(bw,columnNames);
		for (int i=0;i<data.size();i++) saveLineToTable(bw,data.get(i));
		bw.write("</table>");
		bw.newLine();
		return nextImageNr;
	}

	@Override
	public int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		if (columnNames.isEmpty()) buildTable();

		try (final ByteArrayOutputStream stream=new ByteArrayOutputStream()) {
			toTable().save(stream,Table.SaveMode.SAVEMODE_TEX);
			final byte[] b=stream.toByteArray();
			char[] c=new char[b.length];
			for (int i=0;i<b.length;i++) c[i]=(char)b[i];
			bw.write(c);
		}

		return nextImageNr;
	}

	@Override
	public String ownSettingsName() {return null;}

	@Override
	public Icon ownSettingsIcon() {return null;}

	@Override
	public boolean ownSettings(JPanel owner) {return false;}


	/**
	 * Erstellt einen Tabelleneintrag mit einer Dezimalzahl
	 * @param data	Zahlenwert (Summe)
	 * @param days	Anzahl an Simulationswiederholungen durch die der Zahlenwert geteilt werden soll
	 * @return	Tabelleneintrag
	 */
	protected static String addCell(long data, long days) {
		return NumberTools.formatLong(Math.round(((double)data)/days));
	}

	/**
	 * Erstellt einen Tabelleneintrag mit einem Prozentwert
	 * @param data	Zahlenwert
	 * @param digits	Anzahl an Nachkommastellen
	 * @return	Tabelleneintrag
	 */
	protected static String addPercentCell(double data, int digits) {
		return NumberTools.formatPercent(data,digits);
	}

	/**
	 * Erstellt einen Tabelleneintrag mit einem Prozentwert<br>
	 * Es wird eine Nachkommastelle ausgegeben.
	 * @param data	Zahlenwert
	 * @return	Tabelleneintrag
	 */
	protected static String addPercentCell(double data) {
		return addPercentCell(data,1);
	}

	/**
	 * Erstellt einen Tabelleneintrag mit einem Prozentwert
	 * @param zaehler	Zähler für den Bruch der als Prozentwert dargestellt werden soll
	 * @param nenner	Nenner für den Bruch der als Prozentwert dargestellt werden soll
	 * @param digits	Anzahl an Nachkommastellen
	 * @return	Tabelleneintrag
	 */
	protected static String addPercentCellParts(long zaehler, long nenner, int digits) {
		if (nenner==0) return "-";
		return addPercentCell((double)zaehler/Math.max(1,nenner),digits);
	}

	/**
	 * Erstellt einen Tabelleneintrag mit einem Prozentwert<br>
	 * Es wird eine Nachkommastelle ausgegeben.
	 * @param zaehler	Zähler für den Bruch der als Prozentwert dargestellt werden soll
	 * @param nenner	Nenner für den Bruch der als Prozentwert dargestellt werden soll
	 * @return	Tabelleneintrag
	 */
	protected static String addPercentCellParts(long zaehler, long nenner) {
		return addPercentCellParts(zaehler,nenner,1);
	}

	/**
	 * Erstellt einen Tabelleneintrag mit einer Zeitangabe<br>
	 * @param data	Zahlenwert (Summe)
	 * @param days	Anzahl an Simulationswiederholungen durch die der Zahlenwert geteilt werden soll
	 * @return	Tabelleneintrag
	 */
	protected static String addTimeCell(long data, long days) {
		return TimeTools.formatTime((int)Math.round(((double)data)/days));
	}

	/**
	 * Berechnet die Standardabweichung aus X, X² und n.
	 * @param x2	Summe der quadrierten Werte der Messreihe
	 * @param x	Summe der Werte der Messreihe
	 * @param n	Anzahl der Werte in der Messreihe
	 * @return	Standardabweichung
	 */
	protected static long calcStd(long x2, long x, long n) {
		if (n>0) return Math.round(Math.sqrt(((double)x2)/n-((double)x*x)/n/n)); else return 0;
	}

	@Override
	public boolean saveDOCX(XWPFDocument doc) {
		toTable().saveToDOCX(doc);
		return true;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		if (columnNames.isEmpty()) buildTable();
		initData();

		if (!pdf.writeTableLine(columnNames,11,true,0)) return false;
		for (int i=0;i<data.size();i++) if (!pdf.writeTableLine(data.get(i),11,false,(i==data.size()-1)?25:0)) return false;

		return true;
	}

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {}

	@Override
	public void setRequestChartSetup(Supplier<ChartSetup> getChartSetup) {}

	@Override
	public void setUpdateChartSetup(Consumer<ChartSetup> setChartSetup) {	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, die html-Seite der angegebenen Adresse anzeigt.
	 * @param descriptionURL	html-Seite mit einer zusätzlichen Erklärung zu dieser Statistikseite
	 * @param descriptionHelpCallback	Handler, der Themennamen (angegeben über "help:..."-Links) zum Aufruf normaler Hilfeseiten entgegen nimmt
	 */
	protected final void addDescription(final URL descriptionURL, final Consumer<String> descriptionHelpCallback) {
		this.descriptionURL=descriptionURL;
		this.descriptionHelpCallback=descriptionHelpCallback;
	}

	/**
	 * Überträgt die Daten aus einer {@link Table} in das {@link #data} Feld
	 */
	protected void initData() {
		if (table==null) {
			if (data==null) data=new ArrayList<>();
		} else {
			data=table.getData();
			table=null;
		}
	}

	/**
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}
}