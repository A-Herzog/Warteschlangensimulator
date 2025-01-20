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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
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

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt eine Implementierung des {@link StatisticViewer}-Interfaces zur
 * Anzeige von Tabellen dar.
 * @author Alexander Herzog
 * @see StatisticViewer
 * @version 1.6
 */
public class StatisticViewerTable implements StatisticViewer {
	/**
	 * Tabelle mit Daten (kann <code>null</code> sein wenn die Daten nicht über ein {@link Table}-Objekt geladen werden)
	 */
	private Table table;

	/**
	 * Spaltentitel der Tabelle
	 */
	protected List<String> columnNames;

	/**
	 * Tabelle mit Daten in sortierter und/oder gefilterter Form
	 * @see #buildTableModel()
	 * @see #table
	 */
	private Table showTable;

	/**
	 * Spaltentitel der Tabelle inkl. möglicher Ergänzungen
	 * @see #buildTableModel()
	 * @see #columnNames
	 */
	protected List<String> showColumnNames;

	/**
	 * Spalte nach der aktuell sortiert werden soll (Werte &lt;0 für "keine Sortierung")
	 * @see #buildTableModel()
	 */
	private int sortByColumn;

	/**
	 * Soll absteigend (<code>true</code>) oder aufsteigend (<code>false</code>) sortiert werden?
	 * @see #sortByColumn
	 * @see #buildTableModel()
	 */
	private boolean sortDescending;

	/**
	 * Filter für die Werte in den einzelnen Spalten
	 * @see #buildTableModel()
	 */
	private List<Set<String>> filter;

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
	 * Erzeugte Tabelle
	 * @see #getViewer(boolean)
	 */
	private JTable viewerTable;

	/**
	 * Datenmodell für die erzeugte Tabelle
	 * @see #getViewer(boolean)
	 */
	private StatisticViewerTableModel viewerTableModel;

	/**
	 * Suchbegriff beim letzten Aufruf der Suchfunktion
	 * @see #search(Component)
	 */
	private String lastSearchString;

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
		columnNames=new ArrayList<>();
		sortByColumn=-1;
		sortDescending=false;
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param data	List aus List-Objekten, die die anzuzeigenden Texte enthalten
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(List<List<String>> data, List<String> columnNames) {
		this();
		setData(data,columnNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param arrayData	Doppeltes Strings-Array, die die anzuzeigenden Texte enthalten
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(String[][] arrayData, String[] arrayColumnNames) {
		this();
		setData(arrayData,arrayColumnNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param data	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(Table data, String[] arrayColumnNames) {
		this();
		setData(data,arrayColumnNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticViewerTable</code>
	 * @param data	<code>Table</code>-Objekt, dass die anzuzeigenden Texte enthält
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public StatisticViewerTable(Table data, List<String> columnNames) {
		this();
		setData(data,columnNames);
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param data	List aus List-Objekten, die die anzuzeigenden Texte enthalten
	 * @param columnNames	List, die die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(List<List<String>> data, List<String> columnNames) {
		table=new Table(Table.IndexMode.ROWS,data);
		this.columnNames=columnNames;

		filter=new ArrayList<>();
		for (int i=0;i<columnNames.size();i++) filter.add(new HashSet<>());
	}

	/**
	 * Setzt die Zeilen der Tabelle
	 * @param arrayData	Doppeltes Strings-Array, die die anzuzeigenden Texte enthalten
	 * @param arrayColumnNames	String-Array, das die anzuzeigenden Spaltentitel enthält
	 */
	public void setData(String[][] arrayData, String[] arrayColumnNames) {
		table=new Table(Table.IndexMode.ROWS,arrayData);
		columnNames=Arrays.asList(arrayColumnNames);

		filter=new ArrayList<>();
		for (int i=0;i<columnNames.size();i++) filter.add(new HashSet<>());
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
		this.columnNames=columnNames;

		filter=new ArrayList<>();
		for (int i=0;i<columnNames.size();i++) filter.add(new HashSet<>());
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
		case CAN_DO_COPY_PLAIN: return true;
		case CAN_DO_PRINT: return true;
		case CAN_DO_SAVE: return true;
		case CAN_DO_SEARCH: return true;
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
	 * Wird aufgerufen, um eine externe Datei (mit dem Standardprogramm) zu öffnen.
	 * @param file	Zu öffnende Datei
	 * @throws IOException	Kann ausgelöst werden, wenn die Datei nicht geöffnet werden konnte
	 */
	protected void openExternalFile(final File file) throws IOException {
		Desktop.getDesktop().open(file);
	}

	/**
	 * Öffnet die Tabelle (über eine temporäre Datei) mit Excel
	 */
	private void openExcel() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".xlsx");
			if (toTable().save(file)) {
				file.deleteOnExit();
				openExternalFile(file);
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

			final PDFWriter pdf=new PDFWriter(owner,new ReportStyle());
			if (!pdf.systemOK) return;
			if (!savePDF(pdf)) return;
			if (!pdf.save(file)) return;

			file.deleteOnExit();
			openExternalFile(file);
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
				openExternalFile(file);
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
		},getDescriptionCustomStyles());
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

	/**
	 * Ergänzt das Kontextmenü um nutzerdefinierte Einträge
	 * @param owner	Übergeordnetes Element
	 * @param popup	Zu ergänzendes Popupmenü
	 * @return	Liefert <code>true</code>, wenn Einträge generiert wurden
	 */
	protected boolean extendPopupMenu(final StatisticsBasePanel owner, final JPopupMenu popup) {
		return addOwnSettingsToPopup(owner,popup);
	}

	/**
	 * Wendet die eingestellten Filter und Sortierungen auf eine Tabelle an.
	 * @param table	Zu verarbeitende Tabelle (wird nicht verändert)
	 * @return	Neue Tabelle, die durch Filterung und Sortierung aus der Originaltabelle hervorgeht
	 */
	protected Table filterAndSortTable(final Table table) {
		/* Filtern */
		final Table filterTable;
		if (filter.stream().mapToInt(set->set.size()).max().orElse(0)==0) {
			filterTable=table;
		} else {
			filterTable=new Table();
			final int colsFilter=filter.size();
			final int rows=table.getSize(0);
			for (int i=0;i<rows;i++) {
				final List<String> line=table.getLine(i);
				final int cols=line.size();
				boolean ok=true;
				for (int j=0;j<colsFilter;j++) {
					final Set<String> colFilter=filter.get(j);
					if (colFilter.size()==0) continue;
					final String cell=(j>=cols)?"":line.get(j);
					if (!colFilter.contains(cell)) {ok=false; break;}
				}
				if (ok) filterTable.addLine(line);
			}
		}

		/* Sortieren */
		return filterTable.getSorted(sortByColumn,sortDescending);
	}

	/**
	 * Aktualisiert das Datenmodell für die Tabelle.
	 * @see #getViewer(boolean)
	 */
	private void buildTableModel() {
		showTable=filterAndSortTable(table);

		/* Spaltenüberschriftung mit Icons versehen */
		showColumnNames=new ArrayList<>(columnNames);
		if (sortByColumn>=0) showColumnNames.set(sortByColumn,showColumnNames.get(sortByColumn)+" "+new String(Character.toChars(sortDescending?9660:9650)));
		for (int i=0;i<showColumnNames.size();i++) if (filter.get(i).size()>0) showColumnNames.set(i,showColumnNames.get(i)+" "+new String(Character.toChars(9745)));

		if (viewerTable!=null) {
			/* Datenmodell aufstellen und eintragen */
			viewerTableModel=new StatisticViewerTableModel(showTable,showColumnNames);
			viewerTable.setModel(viewerTableModel);

			/* Spaltenbreiten neu einstellen */
			SwingUtilities.invokeLater(()->{
				final int colCount=viewerTable.getColumnCount();
				for (int i=0;i<colCount;i++) autoSizeColumn(viewerTable,i,true);
			});
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl einer Tabellenspalte an.
	 * @return	0-basierter Index der ausgewählten Spalte oder -1, wenn der Dialog abgebrochen wurde
	 */
	protected int showColSelectDialog() {
		final StatisticViewerTableColumnSelectDialog dialog=new StatisticViewerTableColumnSelectDialog(viewerTable,columnNames);
		return dialog.getSelectedColumnIndex();
	}

	/**
	 * Zeigt einen Dialog zur Filterung der Werte in einer Spalte an.
	 * @param col	0-basierter Spaltenindex
	 * @return	Liefert <code>true</code>, wenn der Dialog mit "Ok" geschossen wurde
	 */
	protected boolean showColValueSelectFilterDialog(final int col) {
		final Set<String> values=new HashSet<>();
		final int size=table.getSize(0);
		for (int i=0;i<size;i++) {
			final List<String> line=table.getLine(i);
			if (line.size()<=col) continue;
			final String cell=line.get(col);
			if (!cell.isEmpty()) values.add(cell);
		}
		final StatisticViewerTableFilterDialog dialog=new StatisticViewerTableFilterDialog(viewerTable,values,filter.get(col));
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return false;
		filter.set(col,dialog.getActiveValues());
		buildTableModel();
		return true;
	}

	@Override
	public Container getViewer(final boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;

		if (columnNames.isEmpty() || needReInit) buildTable();

		viewerTable=new JTable();
		buildTableModel();
		viewerTable.getTableHeader().setReorderingAllowed(false);
		viewerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		viewerTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final int col=viewerTable.columnAtPoint(e.getPoint());
				if (col<0) return;

				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					autoSizeColumn(viewerTable,col,true);
					return;
				}

				if (SwingUtilities.isRightMouseButton(e)) {
					final JPopupMenu popup=new JPopupMenu();
					boolean hasItems=false;
					JMenuItem item;

					final StatisticsBasePanel owner=getParentStatisticPanel(viewerTable);
					if (owner!=null) {
						hasItems=extendPopupMenu(owner,popup);
					}

					if (hasItems) popup.addSeparator();

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextCopy+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextCopyTable,SimToolsImages.COPY.getIcon()));
					item.addActionListener(e2->{
						final Transferable transferable=getTransferable();
						if (transferable!=null) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable,null);
					});

					if (col>=0) {
						popup.add(item=new JMenuItem(StatisticsBasePanel.contextCopyColumn,null));
						item.addActionListener(e2->{
							if (columnNames.isEmpty()) buildTable();

							final StringBuilder text=new StringBuilder();
							final int size=showTable.getSize(0);
							for (int i=0;i<size;i++) {
								final List<String> line=new ArrayList<>(showTable.getLine(i));
								text.append(line.get(col));
								text.append("\n");
							}
							final Transferable transferable=new StringSelection(text.toString());
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable,null);
						});
					}

					popup.addSeparator();

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextSort+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextSortAscending,SimToolsImages.ARROW_UP.getIcon()));
					item.addActionListener(e2->{
						sortByColumn=col;
						sortDescending=false;
						buildTableModel();
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextSortDescending,SimToolsImages.ARROW_DOWN.getIcon()));
					item.addActionListener(e2->{
						sortByColumn=col;
						sortDescending=true;
						buildTableModel();
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextSortOriginal));
					item.addActionListener(e2->{
						sortByColumn=-1;
						sortDescending=false;
						buildTableModel();
					});

					popup.addSeparator();

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextFilter+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextFilterReset,SimToolsImages.ADD.getIcon()));
					item.addActionListener(e2->{
						filter.get(col).clear();
						buildTableModel();
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextFilterSelect,SimToolsImages.STATISTICS_TABLE_FILTER.getIcon()));
					item.addActionListener(e2->{
						showColValueSelectFilterDialog(col);
					});

					popup.addSeparator();

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextColWidthThis+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthDefault,SimToolsImages.STATISTICS_TABLE_COL_WIDTH.getIcon()));
					item.addActionListener(e2->setColWidth(viewerTable,col,50));

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContent));
					item.addActionListener(e2->autoSizeColumn(viewerTable,col,false));

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContentAndHeader));
					item.addActionListener(e2->autoSizeColumn(viewerTable,col,true));

					popup.addSeparator();

					popup.add(item=new JMenuItem("<html><body><b>"+StatisticsBasePanel.contextColWidthAll+"</b></body></html>"));
					item.setEnabled(false);

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthDefault,SimToolsImages.STATISTICS_TABLE_COL_WIDTH.getIcon()));
					item.addActionListener(e2->{
						autoSizeColumn(viewerTable,0,false);
						for (int i=1;i<viewerTable.getColumnCount();i++) setColWidth(viewerTable,i,50);
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByWindowWidth));
					item.addActionListener(e2->{
						if (viewerTable.getParent() instanceof JViewport) {
							final JViewport viewport=(JViewport)viewerTable.getParent();
							final int w=viewport.getWidth()/viewerTable.getColumnCount();
							final int spacing=getSpacing(viewerTable);
							for (int i=0;i<viewerTable.getColumnCount();i++) setColWidth(viewerTable,i,w-2*spacing);
						}
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContent));
					item.addActionListener(e2->{
						for (int i=0;i<viewerTable.getColumnCount();i++) autoSizeColumn(viewerTable,i,false);
					});

					popup.add(item=new JMenuItem(StatisticsBasePanel.contextColWidthByContentAndHeader));
					item.addActionListener(e2->{
						for (int i=0;i<viewerTable.getColumnCount();i++) autoSizeColumn(viewerTable,i,true);
					});

					popup.show(e.getComponent(),e.getX(),e.getY());
					return;
				}
			}
		});

		viewerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final StatisticsBasePanel owner=getParentStatisticPanel(viewerTable);
					if (owner==null) return;
					final JPopupMenu popup=new JPopupMenu();
					if (!extendPopupMenu(owner,popup)) return;
					popup.show(e.getComponent(),e.getX(),e.getY());
				}
			}
		});

		final JScrollPane tableScroller=new JScrollPane(viewerTable);

		initDescriptionPane();
		if (descriptionPane==null) return viewer=tableScroller;
		return viewer=descriptionPane.getSplitPanel(tableScroller);
	}

	@Override
	public void navigation(JButton button) {
	}

	@Override
	public void search(final Component owner) {
		getViewer(false);

		final StatisticViewerSearchDialog dialog=new StatisticViewerSearchDialog(owner,lastSearchString);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK || dialog.getSearchString().isEmpty()) {
			viewerTableModel.setSearchString(null,false,false);
			viewerTableModel.fireTableDataChanged();
			return;
		}

		lastSearchString=dialog.getSearchString();

		viewerTableModel.setSearchString(lastSearchString,dialog.isCaseSensitive(),dialog.isRegularExpression());
		viewerTableModel.fireTableDataChanged();
	}

	@Override
	public boolean isViewerGenerated() {
		return viewer!=null;
	}

	/**
	 * Fügt eine Textzeile bestehend aus mehreren Spalten zu einem {@link StringBuilder} hinzu.
	 * @param output	{@link StringBuilder}  zu dem die Zeile hinzugefügt werden soll
	 * @param line	Auszugebende Zeile (die Spalten werden durch Tabulatoren getrennt)
	 * @see #copyToClipboard(Clipboard)
	 */
	protected static void addListToStringBuilder(final StringBuilder output, final List<String> line) {
		final int size=line.size();
		if (size>0) output.append(line.get(0));
		for (int i=1;i<size;i++) {output.append('\t'); output.append(line.get(i));}
		output.append('\n');
	}

	@Override
	public Transferable getTransferable() {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();
		return getTransferableFromTable(false,showColumnNames);
	}

	/**
	 * Liefert ein {@link Transferable}-Objekt für den Viewer zum Kopieren der Tabelle ohne die Rahmenzeile und -spalte.
	 * Dieser ist dann verfügbar, wenn auch ein Kopieren möglich ist.
	 * @return	{@link Transferable}-Objekt für den Viewer
	 */
	protected Transferable getTransferablePlain() {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();
		return getTransferableFromTable(true,showColumnNames);
	}

	/**
	 * Liefert ein {@link Transferable}-Objekt für den Viewer zum Kopieren der Tabelle
	 * @param plain	Rahmenzeile und -spalte weglassen?
	 * @param columnNames	Namen der Spaltenüberschriften (wird nur verwendet, wenn <code>plain</code> nicht auf <code>true</code> steht)
	 * @return	{@link Transferable}-Objekt zum Kopieren in die Zwischenablage
	 */
	protected Transferable getTransferableFromTable(final boolean plain, final List<String> columnNames) {
		return getTransferableFromTable(showTable,plain,columnNames);
	}

	/**
	 * Liefert ein {@link Transferable}-Objekt für eine Tabelle
	 * @param table	Umzuwandelnde Tabelle
	 * @param plain	Rahmenzeile und -spalte weglassen?
	 * @param columnNames	Namen der Spaltenüberschriften (wird nur verwendet, wenn <code>plain</code> nicht auf <code>true</code> steht)
	 * @return	{@link Transferable}-Objekt zum Kopieren in die Zwischenablage
	 */
	protected static Transferable getTransferableFromTable(final Table table, final boolean plain, final List<String> columnNames) {
		final StringBuilder result=new StringBuilder();
		if (!plain) addListToStringBuilder(result,columnNames);
		final int size=table.getSize(0);
		for (int i=0;i<size;i++) {
			final List<String> line=new ArrayList<>(table.getLine(i));
			if (plain) line.remove(0);
			addListToStringBuilder(result,line);
		}
		return new StringSelection(result.toString());
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		final Transferable transferable=getTransferable();
		if (transferable!=null) clipboard.setContents(transferable,null);
	}

	@Override
	public void copyToClipboardPlain(Clipboard clipboard) {
		final Transferable transferable=getTransferablePlain();
		if (transferable!=null) clipboard.setContents(transferable,null);
	}

	@Override
	public boolean print() {
		if (columnNames.isEmpty()) buildTable();

		Table t=new Table();
		t.addLine(showColumnNames);
		t.addLines(showTable.getData());
		JTextComponent tc=new JTextArea(t.toString());
		try {tc.print(null,null,true,null,null,true);} catch (PrinterException e) {return false;}
		return true;
	}

	/**
	 * Wandelt die intern vorliegenden Tabellendaten in ein {@link Table}-Objekt um und liefert dieses zurück.
	 * @return	{@link Table}-Objekt, welches die in dem Viewer vorliegenden Tabellendaten enthält
	 */
	public Table toTable() {
		return toTableInt();
	}

	/**
	 * Wandelt die intern vorliegenden Tabellendaten in ein {@link Table}-Objekt um und liefert dieses zurück.
	 * @return	{@link Table}-Objekt, welches die in dem Viewer vorliegenden Tabellendaten enthält
	 */
	private Table toTableInt() {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();

		final Table t=new Table();
		t.addLine(showColumnNames);
		t.addLines(showTable.getData());
		return t;
	}

	@Override
	public void save(Component owner) {
		final File file=Table.showSaveDialog(owner,StatisticsBasePanel.viewersSaveTable,null,StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf"); if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (!save(owner,file)) {
			MsgBox.error(owner,StatisticsBasePanel.viewersSaveTableErrorTitle,String.format(StatisticsBasePanel.viewersSaveTableErrorInfo,file.toString()));
		}
	}

	@Override
	public boolean save(Component owner, File file) {
		if (columnNames.isEmpty()) buildTable();

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			final PDFWriter pdf=new PDFWriter(owner,new ReportStyle());
			if (!pdf.systemOK) return false;
			if (!savePDF(pdf)) return false;
			return pdf.save(file);
		}

		return toTable().save(file);
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();

		bw.write(showTable.saveToHTML(showColumnNames,true));
		bw.newLine();

		return nextImageNr;
	}

	@Override
	public int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();

		bw.write(showTable.saveToLaTeX(showColumnNames,true));
		bw.newLine();

		return nextImageNr;
	}

	@Override
	public int saveTypst(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();

		bw.write(showTable.saveToTypst(showColumnNames,true));
		bw.newLine();

		return nextImageNr;
	}

	@Override
	public String[] ownSettingsName() {
		return null;
	}

	@Override
	public Icon[] ownSettingsIcon() {
		return null;
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		return false;
	}

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
	public boolean saveDOCX(final DOCXWriter doc) {
		final Table t=toTableInt();
		final int lines=Math.min(t.getSize(0),t.findLastNonNullRow(true)+2);
		doc.writeTable(t,lines);
		return true;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		if (columnNames.isEmpty()) buildTable();
		buildTableModel();

		final var filteredShowColumnNames=showColumnNames.stream().map(name->{
			name=name.replace(" "+new String(Character.toChars(9660)),"");
			name=name.replace(" "+new String(Character.toChars(9650)),"");
			name=name.replace(" "+new String(Character.toChars(9745)),"");
			return name;
		}).collect(Collectors.toList());
		if (!pdf.writeStyledTableHeader(filteredShowColumnNames)) return false;
		final int size=Math.min(showTable.getSize(0),showTable.findLastNonNullRow(true)+2);
		for (int i=0;i<size;i++) if (!pdf.writeStyledTableLine(showTable.getLine(i),i==size-1)) return false;

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
	 * Ermöglicht das Laden zusätzlicher Styles für die Erklärungstexte.
	 * @return	Zusätzliche Stylesheets für Erklärungstexte (kann <code>null</code> oder leer sein)
	 */
	protected String getDescriptionCustomStyles() {
		return null;
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