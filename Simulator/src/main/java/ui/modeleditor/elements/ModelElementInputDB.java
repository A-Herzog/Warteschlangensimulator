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
package ui.modeleditor.elements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.db.DBConnect;
import simulator.db.DBConnect.SortMode;
import simulator.db.DBSettings;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Liest eine Zahl aus einer Datenbanktabelle und speichert diese in einer Variable.
 * @author Alexander Herzog
 */
public class ModelElementInputDB extends ModelElementMultiInSingleOutBox implements ElementWithNewVariableNames, ElementNoRemoteSimulation, ElementWithDB {
	/**
	 * Einstellungen zur Verbindung zur Datenbank
	 * @see #getDb()
	 */
	private DBSettings db;

	/**
	 * Name der Tabelle aus der die Daten ausgelesen werden sollen
	 * @see #getTable()
	 * @see #setTable(String)
	 */
	private String table;

	/**
	 * Name der zu ladenden Tabellenspalte
	 * @see #getLoadColumn()
	 * @see #setLoadColumn(String)
	 */
	private String loadColumn;

	/**
	 * Name der Spalte nach der die Werte sortiert werden sollen
	 * @see #getSortColumn()
	 * @see #setSortColumn(String)
	 */
	private String sortColumn;

	/**
	 * Sortierrichtung für die Sortierspalte
	 * @see #getSortMode()
	 * @see #setSortMode(simulator.db.DBConnect.SortMode)
	 * @see SortMode
	 */
	private DBConnect.SortMode sortMode;

	/**
	 * Name der Variable, in die der Wert geschrieben werden soll
	 * @see #getVariable()
	 * @see #setVariable(String)
	 */
	private String variable;

	/**
	 * Verhalten beim Erreichen des Dateiendes
	 * @see #getEofMode()
	 * @see #setEofMode(EofModes)
	 * @see EofModes
	 */
	private EofModes eofMode;

	/**
	 * Vorgabewert, der verwendet wird, wenn im Modus {@link ModelElementInputDB.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @see #getDefaultValue()
	 * @see #setDefaultValue(String)
	 */
	private String defaultValue;

	/**
	 * Verhalten am Tabellenende
	 * @see ModelElementInputDB#getEofMode()
	 * @see ModelElementInputDB#setEofMode(EofModes)
	 * @see ModelElementInputDB#getDefaultValue()
	 * @see ModelElementInputDB#setDefaultValue(String)
	 */
	public enum EofModes {
		/** Führt, wenn das Dateiende erreicht wurde, keine weiteren Zuweisungen durch. */
		EOF_MODE_SKIP,
		/** Liefert, wenn das Dateiende erreicht wurde, immer einen Vorgabewert */
		EOF_MODE_DEFAULT_VALUE,
		/** Beginnt, wenn das Dateiende erreicht wurde, wieder von vorne in der Datei. */
		EOF_MODE_LOOP,
		/** Beendet die Simulation, wenn das Dateiende erreicht wurde. */
		EOF_MODE_TERMINATE
	}

	/**
	 * Konstruktor der Klasse <code>ModelElementInputDB</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInputDB(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);

		db=new DBSettings();
		table="";
		loadColumn="";
		sortColumn="";
		sortMode=DBConnect.SortMode.ASCENDING;
		variable="";
		eofMode=EofModes.EOF_MODE_SKIP;
		defaultValue="0";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INPUT_DB.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InputDB.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementInputDB)) return false;

		final ModelElementInputDB otherInput=((ModelElementInputDB)element);

		if (!db.equalsDBSettings(otherInput.db)) return false;
		if (!table.equals(otherInput.table)) return false;
		if (!loadColumn.equals(otherInput.loadColumn)) return false;
		if (!sortColumn.equals(otherInput.sortColumn)) return false;
		if (sortMode!=otherInput.sortMode) return false;
		if (!variable.equals(otherInput.variable)) return false;
		if (eofMode!=otherInput.eofMode) return false;
		if (!defaultValue.equals(otherInput.defaultValue)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInputDB) {
			final ModelElementInputDB copySource=(ModelElementInputDB)element;

			db=copySource.db.clone();
			table=copySource.table;
			loadColumn=copySource.loadColumn;
			sortColumn=copySource.sortColumn;
			sortMode=copySource.sortMode;
			variable=copySource.variable;
			eofMode=copySource.eofMode;
			defaultValue=copySource.defaultValue;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInputDB clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInputDB element=new ModelElementInputDB(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InputDB.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.InputDB.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(230,230,230);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert die Einstellungen zur Verbindung zur Datenbank
	 * @return	Einstellungen zur Verbindung zur Datenbank
	 */
	@Override
	public DBSettings getDb() {
		return db;
	}

	/**
	 * Liefert den Namen der Tabelle aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	public String getTable() {
		if (table==null) return ""; else return table;
	}

	/**
	 * Stellt den Namen der Tabelle aus der die Daten ausgelesen werden sollen ein.
	 * @param table	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	public void setTable(final String table) {
		if (table==null) this.table=""; else this.table=table;
	}

	/**
	 * Liefert den Namen der zu ladenden Tabellenspalte.
	 * @return	Name der zu ladenden Tabellenspalte
	 * @see ModelElementInputDB#setLoadColumn(String)
	 */
	public String getLoadColumn() {
		if (loadColumn==null) return ""; else return loadColumn;
	}

	/**
	 * Stellt den Namen der zu ladenden Tabellenspalte ein.
	 * @param loadColumn	Name der zu ladenden Tabellenspalte
	 * @see ModelElementInputDB#getLoadColumn()
	 */
	public void setLoadColumn(final String loadColumn) {
		if (loadColumn==null) this.loadColumn=""; else this.loadColumn=loadColumn;
	}

	/**
	 * Liefert den Namen der Spalte nach der die Werte sortiert werden sollen (darf leer sein)
	 * @return	Name der Spalte nach der die Werte sortiert werden sollen
	 * @see ModelElementInputDB#setSortColumn(String)
	 * @see ModelElementInputDB#getSortMode()
	 */
	public String getSortColumn() {
		if (sortColumn==null) return ""; else return sortColumn;
	}

	/**
	 * Stellt den Namen der Spalte nach der die Werte sortiert werden sollen ein (darf leer sein).
	 * @param sortColumn	Name der Spalte nach der die Werte sortiert werden sollen
	 * @see ModelElementInputDB#getSortColumn()
	 * @see ModelElementInputDB#setSortMode(simulator.db.DBConnect.SortMode)
	 */
	public void setSortColumn(final String sortColumn) {
		if (sortColumn==null) this.sortColumn=""; else this.sortColumn=sortColumn;
	}

	/**
	 * Liefert die Sortierrichtung für die Sortierspalte.
	 * @return	Sortierrichtung für die Sortierspalte
	 * @see ModelElementInputDB#setSortMode(simulator.db.DBConnect.SortMode)
	 * @see ModelElementInputDB#getSortColumn()
	 * @see simulator.db.DBConnect.SortMode
	 */
	public DBConnect.SortMode getSortMode() {
		if (sortMode==null) return DBConnect.SortMode.ASCENDING; else return sortMode;
	}

	/**
	 * Stellt die Sortierrichtung für die Sortierspalte ein.
	 * @param sortMode	Sortierrichtung für die Sortierspalte
	 * @see ModelElementInputDB#getSortMode()
	 * @see ModelElementInputDB#setSortColumn(String)
	 * @see simulator.db.DBConnect.SortMode
	 */
	public void setSortMode(final DBConnect.SortMode sortMode) {
		if (sortMode!=null) this.sortMode=sortMode;
	}

	/**
	 * Liefert den Namen der Variable, in die der Wert geschrieben werden soll
	 * @return	Name der Variable, in die der Wert geschrieben werden soll
	 * @see ModelElementInputDB#setVariable(String)
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * Stellt den Namen der Variable, in die der Wert geschrieben werden soll, ein.
	 * @param variable	Name der Variable, in die der Wert geschrieben werden soll
	 * @see ModelElementInputDB#getVariable()
	 */
	public void setVariable(String variable) {
		if (variable==null) this.variable=""; else this.variable=variable;
	}

	/**
	 * Gibt an wie beim Erreichen des Dateiendes verfahren werden soll.
	 * @return	Verhalten beim Erreichen des Dateiendes
	 * @see ModelElementInputDB#setEofMode(EofModes)
	 * @see EofModes
	 */
	public EofModes getEofMode() {
		return eofMode;
	}

	/**
	 * Stellt ein wie beim Erreichen des Dateiendes verfahren werden soll.
	 * @param eofMode	Verhalten beim Erreichen des Dateiendes
	 * @see ModelElementInputDB#getEofMode()
	 * @see EofModes
	 */
	public void setEofMode(EofModes eofMode) {
		this.eofMode=eofMode;
	}

	/**
	 * Gibt den Vorgabewert an, der verwendet wird, wenn im Modus {@link ModelElementInputDB.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @return	Vorgabewert
	 * @see ModelElementInputDB#setDefaultValue(String)
	 * @see EofModes#EOF_MODE_DEFAULT_VALUE
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Stellt den Vorgabewert ein, der verwendet wird, wenn im Modus {@link ModelElementInputDB.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @param defaultValue	Vorgabewert
	 * @see ModelElementInputDB#getDefaultValue()
	 * @see EofModes#EOF_MODE_DEFAULT_VALUE
	 */
	public void setDefaultValue(final String defaultValue) {
		if (defaultValue==null) this.defaultValue=""; else this.defaultValue=defaultValue;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementInputDBDialog(owner,ModelElementInputDB.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsData(this,parentMenu,addNextStation);
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.InputDB.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		db.saveToXML(doc,node);

		if (table!=null && !table.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDB.XML.Table")));
			sub.setTextContent(table);
		}

		if (loadColumn!=null && !loadColumn.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDB.XML.LoadColumn")));
			sub.setTextContent(loadColumn);
		}

		if (sortColumn!=null && !sortColumn.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDB.XML.SortColumn")));
			String sortModeString=null;
			switch (sortMode) {
			case ASCENDING:
				sortModeString=Language.trPrimary("Surface.InputDB.XML.SortColumn.SortMode.Ascending");
				break;
			case DESCENDING:
				sortModeString=Language.trPrimary("Surface.InputDB.XML.SortColumn.SortMode.Descending");
				break;
			}
			if (sortModeString!=null) sub.setAttribute(Language.trPrimary("Surface.InputDB.XML.SortColumn.SortMode"),sortModeString);
			sub.setTextContent(sortColumn);
		}

		String modeString=null;
		switch (eofMode) {
		case EOF_MODE_SKIP:
			modeString=Language.trPrimary("Surface.InputDB.XML.EofMode.Skip");
			break;
		case EOF_MODE_DEFAULT_VALUE:
			modeString=Language.trPrimary("Surface.InputDB.XML.EofMode.DefaultValue");
			break;
		case EOF_MODE_LOOP:
			modeString=Language.trPrimary("Surface.InputDB.XML.EofMode.Loop");
			break;
		case EOF_MODE_TERMINATE:
			modeString=Language.trPrimary("Surface.InputDB.XML.EofMode.Terminate");
			break;
		}
		if (modeString!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDB.XML.EofMode")));
			sub.setTextContent(modeString);
		}

		if (eofMode==EofModes.EOF_MODE_DEFAULT_VALUE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDB.XML.DefaultValue")));
			sub.setTextContent(defaultValue);
		}

		if (!variable.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDB.XML.Variable")));
			sub.setTextContent(variable);
		}
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (DBSettings.isDBSettingsNode(node)) {
			return db.loadFromXML(node);
		}

		if (Language.trAll("Surface.InputDB.XML.Table",name)) {
			table=content;
			return null;
		}

		if (Language.trAll("Surface.InputDB.XML.LoadColumn",name)) {
			loadColumn=content;
			return null;
		}

		if (Language.trAll("Surface.InputDB.XML.SortColumn",name)) {
			sortColumn=content;
			final String sortModeString=Language.trAllAttribute("Surface.InputDB.XML.SortColumn.SortMode",node);
			if (Language.trAll("Surface.InputDB.XML.SortColumn.SortMode.Ascending",sortModeString)) sortMode=DBConnect.SortMode.ASCENDING;
			if (Language.trAll("Surface.InputDB.XML.SortColumn.SortMode.Descending",sortModeString)) sortMode=DBConnect.SortMode.DESCENDING;
			return null;
		}

		if (Language.trAll("Surface.InputDB.XML.EofMode",name)) {
			if (Language.trAll("Surface.InputDB.XML.EofMode.Skip",content)) eofMode=EofModes.EOF_MODE_SKIP;
			if (Language.trAll("Surface.InputDB.XML.EofMode.DefaultValue",content)) eofMode=EofModes.EOF_MODE_DEFAULT_VALUE;
			if (Language.trAll("Surface.InputDB.XML.EofMode.Loop",content)) eofMode=EofModes.EOF_MODE_LOOP;
			if (Language.trAll("Surface.InputDB.XML.EofMode.Terminate",content)) eofMode=EofModes.EOF_MODE_TERMINATE;
			return null;
		}

		if (Language.trAll("Surface.InputDB.XML.DefaultValue",name)) {
			defaultValue=content;
			return null;
		}

		if (Language.trAll("Surface.InputDB.XML.Variable",name)) {
			variable=content;
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station während der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return super.showAnimationRunData(); /* statt einfach "false". Schadet ja auch an dieser Station nicht. */
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementInputDB";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		db.buildDescription(descriptionBuilder,1000);

		if (table!=null && !table.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDB.Table"),table,2000);
		if (loadColumn!=null && !loadColumn.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDB.ColumnLoad"),loadColumn,3000);

		if (sortColumn!=null && !sortColumn.trim().isEmpty()) {
			final String dir;
			switch (sortMode) {
			case ASCENDING: dir=Language.tr("ModelDescription.InputDB.ColumnSort.Ascending"); break;
			case DESCENDING: dir=Language.tr("ModelDescription.InputDB.ColumnSort.Descending"); break;
			default: dir=""; break;
			}
			descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDB.ColumnSort"),sortColumn+" ("+dir+")",4000);
		}

		String modeInfo="";
		switch (eofMode) {
		case EOF_MODE_SKIP:
			modeInfo=Language.tr("ModelDescription.InputDB.EOFMode.Skip");
			break;
		case EOF_MODE_DEFAULT_VALUE:
			modeInfo=Language.tr("ModelDescription.InputDB.EOFMode.DefaultValue")+": "+defaultValue;
			break;
		case EOF_MODE_LOOP:
			modeInfo=Language.tr("ModelDescription.InputDB.EOFMode.Loop");
			break;
		case EOF_MODE_TERMINATE:
			modeInfo=Language.tr("ModelDescription.InputDB.EOFMode.Terminate");
			break;
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDB.EOFMode"),modeInfo,5000);

		if (!variable.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDB.Variable"),variable,6000);
		}
	}

	/**
	 * Listet alle Variablennamen auf
	 * @return	Liste aller Variablennamen
	 */
	@Override
	public String[] getVariables() {
		if (variable==null) return new String[0];
		return new String[]{variable};
	}

	@Override
	public DataCheckResult checkExternalData() {
		return DataCheckResult.checkDB(this,db);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Tabelleneinstellungen */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Table"),table,newTable->{table=newTable;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.TableLoadColumn"),loadColumn,newLoadColumn->{loadColumn=newLoadColumn;});
		searcher.testString(this,Language.tr("Surface.InputDB.Dialog.ColumnSort"),sortColumn,newSortColumn->{sortColumn=newSortColumn;});

		/* Zuweisung */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.AssignedVariable"),variable,newVariable->{variable=newVariable;});
		if (eofMode==EofModes.EOF_MODE_DEFAULT_VALUE) {
			searcher.testString(this,Language.tr("Surface.InputDB.Dialog.Mode.DefaultValue"),defaultValue,newDefaultValue->{defaultValue=newDefaultValue;});
		}
	}

	@Override
	public boolean isOutputActive() {
		/* Keine Remote-Simulation */
		return true;
	}
}
