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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
 * @version 1.2
 */
public class StatisticViewerTable implements StatisticViewer {
	/**
	 * Sollen die Tabellenspalten entsprechend schmal gestaltet werden, um in das Fenster zu passen (<code>false</code>)
	 * oder soll für die Spalten eine Mindestbreite beibehalten werden und ggf. ein horizontaler Scrollbalken verwendet
	 * werden (<code>true</code>)?
	 */
	public static boolean NO_NOT_SHRINK_COLUMNS=true;

	/**
	 * Inhalt der Tabelle
	 */
	protected List<List<String>> data;

	/**
	 * Spaltentitel der Tabelle
	 */
	protected List<String> columnNames;

	private URL descriptionURL=null;
	private Consumer<String> descriptionHelpCallback=null;
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
		data=new ArrayList<List<String>>();
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
		this.data=data;
		this.columnNames=columnNames;
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param arrayData	Doppeltes Strings-Array, die die anzuzeigenden Texte enthalten
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(String[][] arrayData, String[] arrayColumnNames) {
		data=new ArrayList<List<String>>();
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
		setData(table.getData(),new ArrayList<String>(Arrays.asList(arrayColumnNames)));
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param table	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(Table table, List<String> columnNames) {
		setData(table.getData(),columnNames);
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

		if (excel && ods) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarOpenTable);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarOpenTableHint);
			button.setIcon(SimToolsImages.OPEN.getIcon());
			button.addActionListener(e->{
				final JPopupMenu menu=new JPopupMenu();
				JMenuItem item;
				menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarExcel));
				item.setIcon(SimToolsImages.SAVE_TABLE_EXCEL.getIcon());
				item.setToolTipText(StatisticsBasePanel.viewersToolbarExcelHint);
				item.addActionListener(ev->openExcel());
				menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarODS));
				item.setIcon(SimToolsImages.SAVE_TABLE.getIcon());
				item.setToolTipText(StatisticsBasePanel.viewersToolbarODSHint);
				item.addActionListener(ev->openODS());
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

		return null;
	}

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

	private void initDescriptionPane() {
		if (descriptionPane!=null) return;
		if (descriptionURL==null) return;

		descriptionPane=new DescriptionViewer(descriptionURL,link->{
			if (link.toLowerCase().startsWith("help:") && descriptionHelpCallback!=null) {
				descriptionHelpCallback.accept(link.substring("help:".length()));
			}
		});
	}

	private Container viewer=null;

	@Override
	public Container getViewer(final boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;

		if (columnNames.isEmpty() || needReInit) buildTable();
		final TableModel dataModel=new DefaultReadOnlyTableModel(data,columnNames);
		final JTable table=new JTable(dataModel) {
			private static final long serialVersionUID=6146939967881035328L;
			@Override
			public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
				final Component component=super.prepareRenderer(renderer,row,column);
				if (NO_NOT_SHRINK_COLUMNS && columnNames.size()>5) {
					final int rendererWidth=component.getPreferredSize().width;
					final TableColumn tableColumn=getColumnModel().getColumn(column);
					tableColumn.setPreferredWidth(Math.max(rendererWidth+getIntercellSpacing().width,tableColumn.getPreferredWidth()));
				}
				return component;
			}
		};
		table.getTableHeader().setReorderingAllowed(false);
		if (NO_NOT_SHRINK_COLUMNS && columnNames.size()>5) {
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); /* Sonst wird der horizontale Scrollbalken nie angezeigt. */
			final TableColumnModel columnModel=table.getColumnModel();
			for (int i=0;i<columnModel.getColumnCount();i++) columnModel.getColumn(i).setMinWidth(75);
		}
		final JScrollPane tableScroller=new JScrollPane(table);

		initDescriptionPane();
		if (descriptionPane==null) return viewer=tableScroller;
		return viewer=descriptionPane.getSplitPanel(tableScroller);
	}

	private static final Vector<Vector<String>> dataAsVector(final List<List<String>> data) {
		final Vector<Vector<String>> newData=new Vector<Vector<String>>();
		for (int i=0;i<data.size();i++) {
			final Vector<String> line=new Vector<>();
			line.addAll(data.get(i));
			newData.add(line);
		}
		return newData;
	}

	private static final Vector<String> columnNamesAsVector(final List<String> columnNames) {
		final Vector<String> newColumnNames=new Vector<>();
		newColumnNames.addAll(columnNames);
		return newColumnNames;
	}

	private class DefaultReadOnlyTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 6650829856358608399L;

		public DefaultReadOnlyTableModel(final List<List<String>> data, final List<String> columnNames) {
			super(dataAsVector(data),columnNamesAsVector(columnNames));
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	private void addVectorToStringBuilder(final StringBuilder s, final List<String> v) {
		final int size=v.size();
		if (size>0) s.append(v.get(0));
		for (int i=1;i<size;i++) {s.append('\t'); s.append(v.get(i));}
		s.append('\n');
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		if (columnNames.isEmpty()) buildTable();
		final StringBuilder s=new StringBuilder();
		addVectorToStringBuilder(s,columnNames);
		final int size=data.size();
		for (int i=0;i<size;i++) addVectorToStringBuilder(s,data.get(i));
		StringSelection cont=new StringSelection(s.toString());
		clipboard.setContents(cont,null);
	}

	@Override
	public boolean print() {
		if (columnNames.isEmpty()) buildTable();
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
	public boolean save(Component owner, File file) {
		if (columnNames.isEmpty()) buildTable();

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			PDFWriter pdf=new PDFWriter(owner,15,10);
			if (!pdf.systemOK) return false;
			if (!savePDF(pdf)) return false;
			return pdf.save(file);
		}

		return toTable().save(file);
	}

	private void saveLineToTable(BufferedWriter bw, List<String> line) throws IOException {
		bw.write("  <tr>");
		for (int i=0;i<line.size();i++) bw.write("<td>"+line.get(i)+"</td>");
		bw.write("</tr>");
		bw.newLine();
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		if (columnNames.isEmpty()) buildTable();
		bw.write("<table>");
		bw.newLine();
		saveLineToTable(bw,columnNames);
		for (int i=0;i<data.size();i++) saveLineToTable(bw,data.get(i));
		bw.write("</table>");
		bw.newLine();
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

		if (!pdf.writeTableLine(columnNames,11,true,0)) return false;
		for (int i=0;i<data.size();i++) if (!pdf.writeTableLine(data.get(i),11,false,(i==data.size()-1)?25:0)) return false;

		return true;
	}

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {}

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
}
