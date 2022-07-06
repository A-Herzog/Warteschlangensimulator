/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementSourceExtern;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.dialogs.WaitDialog;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Zeigt eine Vorschau einer zu ladendenen Kundenquellentabelle an.
 * @author Alexander Herzog
 * @see ModelElementSourceTableDialog
 * @see ModelElementSourceTable
 */
public class ModelElementSourceTablePreviewDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1702022592809039270L;

	/**
	 * Maximale Anzahl an in der Vorschau anzuzeigenden Tabellenzeilen
	 */
	private static final int MAX_PREVIEW_ROWS=200;

	/**
	 * Maximale Anzahl an in der Vorschau anzuzeigenden Kundentypen bzw. Tabs
	 */
	private static final int MAX_PREVIEW_CLIENT_TYPES=20;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param stationId	ID der zugehörigen Tabellenkundenquellen-Station
	 * @param tableFileName	Dateiname der Tabelle
	 * @param setup	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird dieses Setup für eine on-the-fly Konvertierung verwendet
	 * @param clientTypes	Zu berücksichtigende Kundentypen
	 * @param isInterarrival	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param model	Gesamtes Modell (aus dem die Icons der Kundentypen ausgelesen werden)
	 */
	public ModelElementSourceTablePreviewDialog(final Component owner, final int stationId, final String tableFileName, final String setup, final String[] clientTypes, final boolean isInterarrival, final EditModel model) {
		super(owner,Language.tr("Surface.SourceTable.Dialog.Table.Preview"));

		/* Tabelle laden */
		final Table table=(Table)WaitDialog.workObject(owner,()->loadTable(tableFileName),WaitDialog.Mode.LOAD_DATA);
		if (table==null) return;

		final RunElementSourceExtern.Arrival[][] arrivals;
		final Object processResult=WaitDialog.workObject(owner,()->{
			if (setup==null) {
				return RunElementSourceExtern.loadTableToArrivals(stationId,table,Arrays.asList(clientTypes),isInterarrival);
			} else {
				return RunElementSourceExtern.loadTableToArrivals(stationId,table,setup,Arrays.asList(clientTypes),isInterarrival);
			}
		},WaitDialog.Mode.PROCESS_DATA);
		if (processResult instanceof String) {
			MsgBox.error(owner,Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorProcess.Title"),Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorProcess.Info")+"\n"+((String)processResult));
			arrivals=new RunElementSourceExtern.Arrival[0][];
		} else {
			arrivals=(RunElementSourceExtern.Arrival[][])processResult;
		}

		/* Tabs */
		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"ModelElementSourceTable"));
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		JPanel tab;

		/* Rohdaten */
		tabs.addTab(Language.tr("Surface.SourceTable.Dialog.Table.Preview.TabRaw"),tab=new JPanel(new BorderLayout()));
		buildTableTab(tab,table,setup!=null,isInterarrival);

		/* Aufbereitete Daten */
		final List<String> typesUsed=new ArrayList<>();
		for (int i=0;i<arrivals.length;i++) {
			final RunElementSourceExtern.Arrival[] clientTypeArrivals=arrivals[i];
			if (clientTypeArrivals.length==0) continue;
			typesUsed.add(clientTypeArrivals[0].clientType);
			tabs.addTab(Language.tr("Surface.SourceTable.Dialog.Table.Preview.TabProcessed")+" \""+clientTypeArrivals[0].clientType+"\"",tab=new JPanel(new BorderLayout()));
			buildTableTab(tab,processTable(clientTypeArrivals),true,false);
			if (typesUsed.size()>=MAX_PREVIEW_CLIENT_TYPES) break;
		}

		/* Icons */
		tabs.setIconAt(0,Images.GENERAL_TABLE.getIcon());
		final AnimationImageSource imageSource=new AnimationImageSource();
		final ModelClientData clientData=model.clientData;
		for (int i=0;i<typesUsed.size();i++) {
			String icon=clientData.getIcon(typesUsed.get(i));
			if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
			tabs.setIconAt(i+1,new ImageIcon(imageSource.get(icon,model.animationImages,16)));
		}

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(800,600);
		setSizeRespectingScreensize(1024,768);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Lädt die angegebene Tabellendatei.
	 * @param tableFileName	Zu ladende Tabellendatei
	 * @return	Liefert im Erfolgsfall die Tabelle, sonst <code>null</code>. (Im Fehlerfall wird direkt eine Fehlermeldung angezeigt.)
	 */
	private Table loadTable(final String tableFileName) {
		if (tableFileName==null || tableFileName.trim().isEmpty()) {
			MsgBox.error(owner,Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorNoFile.Title"),Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorNoFile.Info"));
			return null;
		}

		final File tableFile=new File(tableFileName);
		if (!tableFile.isFile()) {
			MsgBox.error(owner,Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorFileNotFound.Title"),String.format(Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorFileNotFound.Info"),tableFileName));
			return null;
		}

		final Table table=new Table();
		if (!table.load(tableFile)) {
			MsgBox.error(owner,Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorNoTableData.Title"),String.format(Language.tr("Surface.SourceTable.Dialog.Table.Preview.ErrorNoTableData.Info"),tableFileName));
			return null;
		}

		return table;
	}

	/**
	 * Erzeugt aus den Ankunftsobjekten für einen Kundentyp eine Tabelle
	 * @param arrivals	Ankunftsobjekte für einen Kundentyp
	 * @return	Tabelle mit den Ankünften
	 */
	private Table processTable(final RunElementSourceExtern.Arrival[] arrivals) {
		final Table table=new Table();

		/* Obermenge über alle Datenfelder aufstellen */
		final Set<Integer> setFieldsNumbers=new HashSet<>();
		final Set<String> setFieldsStrings=new HashSet<>();
		for (RunElementSourceExtern.Arrival arrival: arrivals) {
			if (arrival.dataIndex!=null) for (int index: arrival.dataIndex) setFieldsNumbers.add(index);
			if (arrival.dataKeyValue!=null) setFieldsStrings.addAll(arrival.dataKeyValue.keySet());
		}
		final int[] fieldsNumbers=setFieldsNumbers.stream().mapToInt(I->I.intValue()).sorted().toArray();
		final String[] fieldsStrings=setFieldsStrings.stream().sorted().toArray(String[]::new);
		final int countfieldsNumbers=fieldsNumbers.length;
		final int countfieldsStrings=fieldsStrings.length;

		/* Überschriften für Tabelle */
		final String[] heading=new String[2+countfieldsNumbers+countfieldsStrings];
		heading[0]=Language.tr("Surface.SourceTable.Dialog.Table.Preview.Header.Time");
		heading[1]=Language.tr("Surface.SourceTable.Dialog.Table.Preview.Header.ClientType");
		for (int i=0;i<countfieldsNumbers;i++) {
			switch (i) {
			case -1:
				heading[2+i]=Language.tr("ModelDescription.Set.WaitingTime");
				continue;
			case -2:
				heading[2+i]=Language.tr("ModelDescription.Set.TransferTime");
				continue;
			case -3:
				heading[2+i]=Language.tr("ModelDescription.Set.ProcessTime");
				continue;
			case -4:
				heading[2+i]=Language.tr("ModelDescription.Costs.ClientWaiting");
				continue;
			case -5:
				heading[2+i]=Language.tr("ModelDescription.Costs.ClientTransfer");
				continue;
			case -6:
				heading[2+i]=Language.tr("ModelDescription.Costs.ClientProcess");
				continue;
			default:
				heading[2+i]=String.format("ClientData(%d)",fieldsNumbers[i]);
			}
		}
		for (int i=0;i<countfieldsStrings;i++) heading[2+countfieldsNumbers+i]=String.format("ClientData(\"%s\")",fieldsStrings[i]);
		table.addLine(heading);

		/* Tabelle aufbauen */
		for (RunElementSourceExtern.Arrival arrival: arrivals) {
			final String[] line=new String[2+countfieldsNumbers+countfieldsStrings];
			line[0]=NumberTools.formatNumber(arrival.time/1000.0);
			line[1]=arrival.clientType;
			for (int i=0;i<countfieldsNumbers;i++) {
				final int nr=fieldsNumbers[i];
				int index=-1;
				for (int j=0;j<arrival.dataIndex.length;j++) if (arrival.dataIndex[j]==nr) {index=j; break;}
				line[2+i]=(index<0)?"0":arrival.dataFormula[index];
			}
			for (int i=0;i<countfieldsStrings;i++) {
				final String key=fieldsStrings[i];
				final String value=arrival.dataKeyValue.get(key);
				line[2+countfieldsNumbers+i]=(value==null)?"":value;
			}
			table.addLine(line);
		}

		return table;
	}

	/**
	 * Zeigt eine Tabelle in einem Tab an
	 * @param panel	Tab in dem die Tabelle angezeigt werden soll
	 * @param table	Tabellendaten
	 * @param firstRowIsHeading	Enthält die Tabelle in der ersten Zeile die anzuzeigenden Überschriften?
	 * @param isInterarrival	Sind die Angaben in der ersten Spalte absolute Zeitangaben (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>)?
	 */
	private void buildTableTab(final JPanel panel, final Table table, final boolean firstRowIsHeading, final boolean isInterarrival) {
		if (table.getSize(0)-(firstRowIsHeading?1:0)>MAX_PREVIEW_ROWS) {
			final JPanel infoPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(infoPanel,BorderLayout.NORTH);
			infoPanel.add(new JLabel(String.format(Language.tr("Surface.SourceTable.Dialog.Table.Preview.MaxSize"),NumberTools.formatLong(MAX_PREVIEW_ROWS),NumberTools.formatLong(table.getSize(0)-(firstRowIsHeading?1:0)))));
		}

		final JTableExt tableExt=new JTableExt();
		tableExt.setModel(new PreviewTable(table,firstRowIsHeading,isInterarrival));
		tableExt.getTableHeader().setReorderingAllowed(false);
		tableExt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i=0;i<table.getSize(1);i++) autoSizeColumn(tableExt,i,true);
		panel.add(new JScrollPane(tableExt),BorderLayout.CENTER);
	}

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
	 * Datenmodell für die anzuzeigende Vorschautabelle
	 * @see ModelElementSourceTablePreviewDialog#buildTableTab(JPanel, Table, boolean, boolean)
	 *
	 */
	private static class PreviewTable extends JTableExtAbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=7354119531573826266L;

		/**
		 * Anzuzeigende Tabelle
		 */
		private final Table table;

		/**
		 * Enthält die Tabelle in der ersten Zeile die anzuzeigenden Überschriften?
		 */
		private final boolean firstRowIsHeading;

		/**
		 * Sind die Angaben in der ersten Spalte absolute Zeitangaben (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>)?
		 */
		private final boolean isInterarrival;

		/**
		 * Konstruktor der Klasse
		 * @param table	Anzuzeigende Tabelle
		 * @param firstRowIsHeading	Enthält die Tabelle in der ersten Zeile die anzuzeigenden Überschriften?
		 * @param isInterarrival	Sind die Angaben in der ersten Spalte absolute Zeitangaben (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>)?
		 */
		public PreviewTable(final Table table, final boolean firstRowIsHeading, final boolean isInterarrival) {
			this.table=table;
			this.firstRowIsHeading=firstRowIsHeading;
			this.isInterarrival=isInterarrival;
		}

		@Override
		public int getRowCount() {
			return Math.min(table.getSize(0)-(firstRowIsHeading?1:0),MAX_PREVIEW_ROWS);
		}

		@Override
		public int getColumnCount() {
			return table.getSize(1);
		}

		@Override
		public String getColumnName(final int column) {
			if (firstRowIsHeading) {
				return table.getValue(0,column);
			} else {
				if (column==0) return isInterarrival?Language.tr("Surface.SourceTable.Dialog.Table.Preview.Header.Interarrival"):Language.tr("Surface.SourceTable.Dialog.Table.Preview.Header.Time");
				if (column==1) return Language.tr("Surface.SourceTable.Dialog.Table.Preview.Header.ClientType");
				return Language.tr("Surface.SourceTable.Dialog.Table.Preview.Header.ClientProperties");
			}
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return table.getValue(rowIndex+(firstRowIsHeading?1:0),columnIndex);
		}
	}
}
