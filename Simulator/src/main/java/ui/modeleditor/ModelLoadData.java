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
package ui.modeleditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ext.com.google.common.base.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.MultiTable;
import mathtools.NumberTools;
import mathtools.Table;
import net.dde.DDEConnect;
import simulator.editmodel.EditModel;
import ui.parameterseries.ParameterCompareSetupValueInput;
import ui.parameterseries.ParameterCompareTools;

/**
 * Diese Klasse hält die Verbindungen zu den externen Datenquellen,
 * die vor Simulationsbeginn ausgelesen werden sollen und das Modell
 * entsprechend verändern, vor.
 * @see EditModel#modelLoadData
 * @author Alexander Herzog
 */
public final class ModelLoadData implements Cloneable {
	/**
	 * Name des XML-Elements, das die Datensätze enthält
	 */
	public static String[] XML_NODE_NAME=new String[] {"ExterneDaten"};

	/**
	 * Wie sollen die Daten eingelesen werden?
	 * @author Alexander Herzog
	 * @see ModelLoadData#getMode()
	 * @see ModelLoadData#setMode(Mode)
	 */
	public enum Mode {
		/** Daten aus Tabellendatei laden */
		FILE,
		/** Daten über DDE-Verbindung aus Excel laden */
		DDE
	}

	private boolean active;
	private Mode mode;
	private String workbook;
	private String table;

	private final List<ModelLoadDataRecord> list;

	private final List<String> changeWarnings;
	private Table sourceTable;
	private String sourceDDEWorkbook;
	private String sourceDDETable;
	private DDEConnect sourceDDE;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelLoadData() {
		list=new ArrayList<>();
		changeWarnings=new ArrayList<>();
		clear();
	}

	/**
	 * Löscht alle Daten in diesem Objekt.
	 */
	public void clear() {
		active=false;
		mode=Mode.FILE;
		workbook="";
		table="";
		list.clear();
		changeWarnings.clear();
	}

	/**
	 * Vergleicht dieses Objekt mit einem weiteren
	 * @param otherModelLoadData	Weiteres Datenobjekt das mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn beide Objekte inhaltlich identisch sind
	 */
	public boolean equalsModelLoadData(final ModelLoadData otherModelLoadData) {
		if (otherModelLoadData==null) return false;

		if (active!=otherModelLoadData.active) return false;
		if (mode!=otherModelLoadData.mode) return false;
		if (!Objects.equal(workbook,otherModelLoadData.workbook)) return false;
		if (!Objects.equal(table,otherModelLoadData.table)) return false;

		if (list.size()!=otherModelLoadData.list.size()) return false;
		for (int i=0;i<list.size();i++) if (!list.get(i).equalsRecord(otherModelLoadData.list.get(i))) return false;

		return true;
	}

	@Override
	public ModelLoadData clone() {
		final ModelLoadData clone=new ModelLoadData();
		clone.copyDataFrom(this);
		return clone;
	}

	/**
	 * Kopiert die Daten aus einem anderen Objekt in dieses
	 * @param source	Ausgangsobjekt aus dem die Daten geladen werden sollen
	 */
	public void copyDataFrom(final ModelLoadData source) {
		clear();
		if (source==null) return;

		active=source.active;
		mode=source.mode;
		workbook=source.workbook;
		table=source.table;
		for (ModelLoadDataRecord record: source.list) list.add(record.clone());
	}

	/**
	 * Ist das Daten laden zu Simulationsbeginn aktiv?
	 * @return	Daten laden zu Simulationsbeginn aktiv
	 * @see #setActive(boolean)
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Stellt ein, ob das Daten laden zu Simulationsbeginn aktiv sein soll.
	 * @param active	Daten laden zu Simulationsbeginn aktiv
	 * @see #isActive()
	 */
	public void setActive(boolean active) {
		this.active=active;
	}

	/**
	 * Liefert die Art der Datenquelle
	 * @return	Art der Datenquelle
	 * @see #setMode(Mode)
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Stellt die Art der Datenquelle ein.
	 * @param mode	Art der Datenquelle
	 * @see #getMode()
	 */
	public void setMode(Mode mode) {
		if (mode!=null) this.mode=mode;
	}

	/**
	 * Liefert den Namen der Tabellendatei (Dateimodus) bzw. des Excel-Workbook (DDE-Modus).
	 * @return	Namen der Tabellendatei (Dateimodus) bzw. des Excel-Workbook (DDE-Modus)
	 * @see #setWorkbook(String)
	 */
	public String getWorkbook() {
		return (workbook==null)?"":workbook;
	}

	/**
	 * Stellt den Namen der Tabellendatei (Dateimodus) bzw. des Excel-Workbook (DDE-Modus) ein.
	 * @param workbook	Namen der Tabellendatei (Dateimodus) bzw. des Excel-Workbook (DDE-Modus)
	 * @see #getWorkbook()
	 */
	public void setWorkbook(String workbook) {
		this.workbook=(workbook==null)?"":workbook;
	}

	/**
	 * Liefert den Namen des Arbeitsblattes (leer für erstes Arbeitsblatt).
	 * @return	Namen des Arbeitsblattes (leer für erstes Arbeitsblatt)
	 * @see #setTable(String)
	 */
	public String getTable() {
		return (table==null)?"":table;
	}

	/**
	 * Stellt den Namen des Arbeitsblattes (leer für erstes Arbeitsblatt) ein.
	 * @param table	Namen des Arbeitsblattes (leer für erstes Arbeitsblatt)
	 */
	public void setTable(String table) {
		this.table=(table==null)?"":table;
	}

	/**
	 * Liefert die Liste der einzelnen Datensätze.
	 * @return	Liste der einzelnen Datensätze
	 */
	public List<ModelLoadDataRecord> getList() {
		return list;
	}

	/**
	 * Speichert die Informationen zu den zu ladenden Daten in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final String workbook=getWorkbook().trim();
		final String table=getTable().trim();

		if (!active && workbook.isEmpty() && table.isEmpty() && list.size()==0) return;

		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		node.setAttribute(Language.tr("ModelLoadData.XML.Active"),active?"1":"0");
		if (mode!=null) {
			String modeString=null;
			switch (mode) {
			case FILE: modeString=Language.tr("ModelLoadData.XML.Mode.File"); break;
			case DDE: modeString=Language.tr("ModelLoadData.XML.Mode.DDE"); break;
			}
			node.setAttribute(Language.tr("ModelLoadData.XML.Mode"),modeString);
		}
		if (!workbook.isEmpty()) node.setAttribute(Language.tr("ModelLoadData.XML.Workbook"),workbook);
		if (!table.isEmpty()) node.setAttribute(Language.tr("ModelLoadData.XML.Table"),table);

		for (ModelLoadDataRecord record: list) record.addDataToXML(doc,node);
	}

	/**
	 * Versucht die Informationen zu den zu ladenden Daten aus einem xml-Element zu laden
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		String s;

		s=Language.trAllAttribute("ModelLoadData.XML.Active",node);
		if (!s.isEmpty()) active=(!s.equals("0"));

		s=Language.trAllAttribute("ModelLoadData.XML.Mode",node);
		if (Language.trAll("ModelLoadData.XML.Mode.File",s)) mode=Mode.FILE;
		if (Language.trAll("ModelLoadData.XML.Mode.DDE",s)) mode=Mode.DDE;

		s=Language.trAllAttribute("ModelLoadData.XML.Workbook",node);
		if (!s.isEmpty()) workbook=s;

		s=Language.trAllAttribute("ModelLoadData.XML.Table",node);
		if (!s.isEmpty()) table=s;

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			if (Language.trAll("ModelLoadData.XML.Record",e.getNodeName())) {
				final ModelLoadDataRecord record=new ModelLoadDataRecord();
				final String error=record.loadFromXML(e);
				if (error!=null) return error;
				list.add(record);
				continue;
			}
		}

		return null;
	}

	/**
	 * Gibt an, ob ein Aufruf von {@link #changeModel(EditModel, File, boolean)} zu Veränderungen
	 * des Modells führen würde.
	 * @return	Wird das Modell beim Aufruf von {@link #changeModel(EditModel, File, boolean)} verändert?
	 * @see #changeModel(EditModel, File)
	 * @see #changeModel(EditModel, File, boolean)
	 */
	public boolean willChangeModel() {
		return active && list.size()>0;
	}

	/**
	 * Liefert die beim letzten Aufruf von {@link #changeModel(EditModel, File, boolean)} entstandenen Fehlermeldungen.
	 * @return	Fehlermeldungen (kann leer sein, aber ist nie <code>null</code>)
	 * @see #changeModel(EditModel, File)
	 * @see #changeModel(EditModel, File, boolean)
	 */
	public List<String> getChangeWarnings() {
		return changeWarnings;
	}

	private String prepareSource(final File baseFolder) {
		if (mode==null) return Language.tr("ModelLoadData.ProcessError.NoMode");
		final String workbook=getWorkbook().trim();
		final String table=getTable().trim();
		if (workbook.isEmpty()) return Language.tr("ModelLoadData.ProcessError.NoWorkbook");

		switch (mode) {
		case FILE:
			final MultiTable multi=new MultiTable();
			File file=new File(workbook);
			if (!file.isFile() && baseFolder!=null && baseFolder.isDirectory()) file=new File(baseFolder,file.getName());
			if (!multi.load(file)) return String.format(Language.tr("ModelLoadData.ProcessError.FileLoadError"),workbook);
			if (table.isEmpty()) {
				sourceTable=multi.get(0);
				if (sourceTable==null) return String.format(Language.tr("ModelLoadData.ProcessError.WorkbookIsEmpty"),workbook);
			} else {
				sourceTable=multi.get(table);
				if (sourceTable==null) return String.format(Language.tr("ModelLoadData.ProcessError.WorkbookDoesNotContainSheet"),workbook,table);
			}
			break;
		case DDE:
			sourceDDE=new DDEConnect();
			if (!sourceDDE.available()) return Language.tr("ModelLoadData.ProcessError.DDENotAvailable");
			final List<String> tables=sourceDDE.listTables().get(workbook);
			if (tables==null) return String.format(Language.tr("ModelLoadData.ProcessError.NoDDEWorkbook"),workbook);
			sourceDDEWorkbook=workbook;
			if (table.isEmpty()) {
				if (tables.size()==0) return Language.tr("ModelLoadData.ProcessError.WorkbookIsEmpty");
			} else {
				sourceDDETable=null;
				for (int i=0;i<tables.size();i++) if (tables.get(i).equalsIgnoreCase(table)) {
					sourceDDETable=tables.get(i);
					break;
				}
				if (sourceDDETable==null) {
					sourceDDE=null;
					return String.format(Language.tr("ModelLoadData.ProcessError.WorkbookDoesNotContainSheet"),table);
				}
			}
			break;
		}

		return null;
	}

	private void finalizeSource() {
		sourceTable=null;
		sourceDDE=null;
	}

	private Object getCellValue(final String cellName, final int nr) {
		if (cellName==null || cellName.trim().isEmpty()) return String.format(Language.tr("ModelLoadData.ProcessError.NoCellID"),nr+1);
		final int[] index=Table.cellIDToNumbers(cellName);
		if (index==null || index.length!=2) return String.format(Language.tr("ModelLoadData.ProcessError.InvalidCellID"),cellName,nr+1);

		String value=null;

		if (sourceTable!=null) {
			if (index[0]<0 || index[0]>=sourceTable.getSize(0) || index[1]<0 || index[1]>=sourceTable.getSize(1)) return String.format(Language.tr("ModelLoadData.ProcessError.CellIDOutOfRange"),cellName,nr+1);
			value=sourceTable.getValue(index[0],index[1]);
		}

		if (sourceDDE!=null) {
			value=sourceDDE.readCell(sourceDDEWorkbook,sourceDDETable,index[0],index[1]);
		}

		if (value==null) return Language.tr("ModelLoadData.ProcessError.NoMode");

		final Double D=NumberTools.getDouble(value);
		if (D==null) return String.format(Language.tr("ModelLoadData.ProcessError.CellValueIsNotNumber"),value,cellName,nr+1);
		return D;
	}

	/**
	 * Prüft, ob die konfigurierte Datenquelle verfügbar ist.
	 * @param baseFolder Optionales Basisverzeichnis, welches verwendet wird, wenn die Datei relativ angegeben ist oder nicht im angegebene Ordner existiert (wird hier <code>null</code> übergeben, so erfolgt kein Basisverzeichnis-Abgleich)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung zu der Datenquelle.
	 */
	public String testSourceAvailable(final File baseFolder) {
		if (!willChangeModel()) return null;

		final String error=prepareSource(baseFolder);
		finalizeSource();

		return error;
	}

	/**
	 * Lädt (wenn aktiv) die Daten aus der externen Quelle und verändert das Modell entsprechend.
	 * @param model	Zu veränderndes Modell (dieses Ausgangsobjekt wird nicht verändert)
	 * @param baseFolder Optionales Basisverzeichnis, welches verwendet wird, wenn die Datei relativ angegeben ist oder nicht im angegebene Ordner existiert (wird hier <code>null</code> übergeben, so erfolgt kein Basisverzeichnis-Abgleich)
	 * @return	Liefert, wenn nicht aktiv <code>null</code>, sonst das veränderte Modell (kann auch einfach das übergebene Modell sein, wenn alle Veränderungen fehlgeschlagen sind).
	 * @see #willChangeModel()
	 * @see #getChangeWarnings()
	 */
	public EditModel changeModel(final EditModel model, final File baseFolder) {
		return changeModel(model,baseFolder,false);
	}

	/**
	 * Lädt die Daten aus der externen Quelle und verändert das Modell entsprechend.<br>
	 * @param model	Zu veränderndes Modell (dieses Ausgangsobjekt wird nicht verändert)
	 * @param baseFolder Optionales Basisverzeichnis, welches verwendet wird, wenn die Datei relativ angegeben ist oder nicht im angegebene Ordner existiert (wird hier <code>null</code> übergeben, so erfolgt kein Basisverzeichnis-Abgleich)
	 * @param force	Gibt an, ob der Wert der "active"-Eigenschaft berücksichtigt werden soll (<code>false</code>) oder ob die Daten immer geladen werden sollen (<code>true</code>).
	 * @return	Liefert, wenn nicht aktiv <code>null</code>, sonst das veränderte Modell (kann auch einfach das übergebene Modell sein, wenn alle Veränderungen fehlgeschlagen sind).
	 * @see #willChangeModel()
	 * @see #getChangeWarnings()
	 */
	public EditModel changeModel(final EditModel model, final File baseFolder, final boolean force) {
		changeWarnings.clear();

		if (force) {
			if (list.size()==0) return null;
		} else {
			if (!willChangeModel()) return null;
		}

		EditModel changedModel=model;

		final String sourceError=prepareSource(baseFolder);
		if (sourceError!=null) {
			changeWarnings.add(sourceError);
			return changedModel;
		}

		try {
			for (int nr=0;nr<list.size();nr++) {
				final ModelLoadDataRecord record=list.get(nr);
				final ParameterCompareSetupValueInput change=record.getChange();

				final Object newValueObj=getCellValue(record.getCell(),nr);
				if (newValueObj instanceof String) {
					changeWarnings.add((String)newValueObj);
					continue;
				}
				final double newValue=(Double)newValueObj;

				final Object result=ParameterCompareTools.setModelValue(changedModel,change,newValue);
				if (result==null) {
					changeWarnings.add(String.format(Language.tr("ModelLoadData.ProcessError.WriteError"),nr+1,NumberTools.formatNumber(newValue)));
					continue;
				}
				if (result instanceof String) {
					changeWarnings.add(String.format(Language.tr("ModelLoadData.ProcessError.WriteErrorInfo"),nr+1,NumberTools.formatNumber(newValue),result));
					continue;
				}
				changedModel=(EditModel)result;
			}
		} finally {
			finalizeSource();
		}
		return changedModel;
	}
}
