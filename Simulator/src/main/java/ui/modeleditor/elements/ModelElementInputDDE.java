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
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
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
 * Liest per DDE-Verbindung eine Zahl aus einer Excel-Tabelle und speichert diese in einer Variable.
 * @author Alexander Herzog
 */
public class ModelElementInputDDE extends ModelElementMultiInSingleOutBox implements ElementWithNewVariableNames, ElementWithDDEInputOutput, ElementNoRemoteSimulation {
	/**
	 * Name der Arbeitsmappe aus der die Daten ausgelesen werden sollen
	 * @see #getWorkbook()
	 * @see #setWorkbook(String)
	 */
	private String workbook;

	/**
	 * Name der Tabelle aus der die Daten ausgelesen werden sollen
	 * @see #getTable()
	 * @see #setTable(String)
	 */
	private String table;

	/**
	 * 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen
	 * @see #getStartRow()
	 * @see #setStartRow(int)
	 */
	private int startRow;

	/**
	 * Name der Spalte aus der die Daten ausgelesen werden sollen
	 * @see #getColumn()
	 * @see #setColumn(String)
	 */
	private String column;

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
	 * Vorgabewert, der verwendet wird, wenn im Modus {@link ModelElementInputDDE.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @see #getDefaultValue()
	 * @see #setDefaultValue(String)
	 */
	private String defaultValue;

	/**
	 * Verhalten am Dateiende
	 * @see ModelElementInputDDE#getEofMode()
	 * @see ModelElementInputDDE#setEofMode(EofModes)
	 * @see ModelElementInputDDE#getDefaultValue()
	 * @see ModelElementInputDDE#setDefaultValue(String)
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
	 * Konstruktor der Klasse <code>ModelElementInputDDE</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInputDDE(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);

		workbook="";
		table="";
		startRow=1;
		column="";
		variable="";
		eofMode=EofModes.EOF_MODE_SKIP;
		defaultValue="0";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INPUT_DDE.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InputDDE.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementInputDDE)) return false;

		final ModelElementInputDDE otherInput=((ModelElementInputDDE)element);

		if (!Objects.equals(workbook,otherInput.workbook)) return false;
		if (!Objects.equals(table,otherInput.table)) return false;
		if (startRow!=otherInput.startRow) return false;
		if (!Objects.equals(column,otherInput.column)) return false;
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
		if (element instanceof ModelElementInputDDE) {
			final ModelElementInputDDE copySource=(ModelElementInputDDE)element;

			workbook=copySource.workbook;
			table=copySource.table;
			startRow=copySource.startRow;
			column=copySource.column;
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
	public ModelElementInputDDE clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInputDDE element=new ModelElementInputDDE(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InputDDE.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.InputDDE.Name");
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
	 * Liefert den Namen der Arbeitsmappe aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Arbeitsmappe aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public String getWorkbook() {
		if (workbook==null) return ""; else return workbook;
	}

	/**
	 * Stellt den Namen der Arbeitsmappe aus der die Daten ausgelesen werden sollen ein.
	 * @param workbook	Name der Arbeitsmappe aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public void setWorkbook(final String workbook) {
		if (workbook==null) this.workbook=""; else this.workbook=workbook;
	}

	/**
	 * Liefert den Namen der Tabelle aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public String getTable() {
		if (table==null) return ""; else return table;
	}

	/**
	 * Stellt den Namen der Tabelle aus der die Daten ausgelesen werden sollen ein.
	 * @param table	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public void setTable(final String table) {
		if (table==null) this.table=""; else this.table=table;
	}

	/**
	 * Liefert die 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen.
	 * @return	1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen
	 */
	@Override
	public int getStartRow() {
		if (startRow<1) return 1;
		return startRow;
	}

	/**
	 * Stellt die 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen ein.
	 * @param startRow	1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen
	 */
	@Override
	public void setStartRow(final int startRow) {
		if (startRow>=1) this.startRow=startRow;
	}

	/**
	 * Liefert den Namen der Spalte aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Spalte aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public String getColumn() {
		if (column==null || column.trim().isEmpty()) return "A"; else return column;
	}

	/**
	 * Stellt den Namen der Spalte aus der die Daten ausgelesen werden sollen ein.
	 * @param column	Name der Spalte aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public void setColumn(final String column) {
		if (column==null) this.column="A"; else this.column=column;
	}

	/**
	 * Liefert den Namen der Variable, in die der Wert geschrieben werden soll
	 * @return	Name der Variable, in die der Wert geschrieben werden soll
	 * @see ModelElementInputDDE#setVariable(String)
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * Stellt den Namen der Variable, in die der Wert geschrieben werden soll, ein.
	 * @param variable	Name der Variable, in die der Wert geschrieben werden soll
	 * @see ModelElementInputDDE#getVariable()
	 */
	public void setVariable(String variable) {
		if (variable==null) this.variable=""; else this.variable=variable;
	}

	/**
	 * Gibt an wie beim Erreichen des Dateiendes verfahren werden soll.
	 * @return	Verhalten beim Erreichen des Dateiendes
	 * @see ModelElementInputDDE#setEofMode(EofModes)
	 * @see EofModes
	 */
	public EofModes getEofMode() {
		return eofMode;
	}

	/**
	 * Stellt ein wie beim Erreichen des Dateiendes verfahren werden soll.
	 * @param eofMode	Verhalten beim Erreichen des Dateiendes
	 * @see ModelElementInputDDE#getEofMode()
	 * @see EofModes
	 */
	public void setEofMode(EofModes eofMode) {
		this.eofMode=eofMode;
	}

	/**
	 * Gibt den Vorgabewert an, der verwendet wird, wenn im Modus {@link ModelElementInputDDE.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @return	Vorgabewert
	 * @see ModelElementInputDDE#setDefaultValue(String)
	 * @see EofModes#EOF_MODE_DEFAULT_VALUE
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Stellt den Vorgabewert ein, der verwendet wird, wenn im Modus {@link ModelElementInputDDE.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @param defaultValue	Vorgabewert
	 * @see ModelElementInputDDE#getDefaultValue()
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
			new ModelElementInputDDEDialog(owner,ModelElementInputDDE.this,readOnly);
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
		return Language.trAll("Surface.InputDDE.XML.Root");
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

		if (workbook!=null && !workbook.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.Workbook")));
			sub.setTextContent(workbook);
		}

		if (table!=null && !table.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.Table")));
			sub.setTextContent(table);
		}

		if (startRow>=1) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.StartRow")));
			sub.setTextContent(""+startRow);
		}

		if (column!=null && !column.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.Column")));
			sub.setTextContent(column);
		}

		String modeString=null;
		switch (eofMode) {
		case EOF_MODE_SKIP:
			modeString=Language.trPrimary("Surface.InputDDE.XML.EofMode.Skip");
			break;
		case EOF_MODE_DEFAULT_VALUE:
			modeString=Language.trPrimary("Surface.InputDDE.XML.EofMode.DefaultValue");
			break;
		case EOF_MODE_LOOP:
			modeString=Language.trPrimary("Surface.InputDDE.XML.EofMode.Loop");
			break;
		case EOF_MODE_TERMINATE:
			modeString=Language.trPrimary("Surface.InputDDE.XML.EofMode.Terminate");
			break;
		}
		if (modeString!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.EofMode")));
			sub.setTextContent(modeString);
		}

		if (eofMode==EofModes.EOF_MODE_DEFAULT_VALUE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.DefaultValue")));
			sub.setTextContent(defaultValue);
		}

		if (!variable.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputDDE.XML.Variable")));
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

		if (Language.trAll("Surface.InputDDE.XML.Workbook",name)) {
			workbook=content;
			return null;
		}

		if (Language.trAll("Surface.InputDDE.XML.Table",name)) {
			table=content;
			return null;
		}

		if (Language.trAll("Surface.InputDDE.XML.StartRow",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null || I<1) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			startRow=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.InputDDE.XML.Column",name)) {
			column=content;
			return null;
		}

		if (Language.trAll("Surface.InputDDE.XML.EofMode",name)) {
			if (Language.trAll("Surface.InputDDE.XML.EofMode.Skip",content)) eofMode=EofModes.EOF_MODE_SKIP;
			if (Language.trAll("Surface.InputDDE.XML.EofMode.DefaultValue",content)) eofMode=EofModes.EOF_MODE_DEFAULT_VALUE;
			if (Language.trAll("Surface.InputDDE.XML.EofMode.Loop",content)) eofMode=EofModes.EOF_MODE_LOOP;
			if (Language.trAll("Surface.InputDDE.XML.EofMode.Terminate",content)) eofMode=EofModes.EOF_MODE_TERMINATE;
			return null;
		}

		if (Language.trAll("Surface.InputDDE.XML.DefaultValue",name)) {
			defaultValue=content;
			return null;
		}

		if (Language.trAll("Surface.InputDDE.XML.Variable",name)) {
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
		return "ModelElementInputDDE";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (workbook!=null && !workbook.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDDE.Workbook"),table,1000);
		if (table!=null && !table.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDDE.Table"),table,2000);
		if (startRow>0) descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDDE.StartRow"),""+startRow,3000);
		if (column!=null && !column.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDDE.Column"),table,4000);

		String modeInfo="";
		switch (eofMode) {
		case EOF_MODE_SKIP:
			modeInfo=Language.tr("ModelDescription.InputDDE.EOFMode.Skip");
			break;
		case EOF_MODE_DEFAULT_VALUE:
			modeInfo=Language.tr("ModelDescription.InputDDE.EOFMode.DefaultValue")+": "+defaultValue;
			break;
		case EOF_MODE_LOOP:
			modeInfo=Language.tr("ModelDescription.InputDDE.EOFMode.Loop");
			break;
		case EOF_MODE_TERMINATE:
			modeInfo=Language.tr("ModelDescription.InputDDE.EOFMode.Terminate");
			break;
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDDE.EOFMode"),modeInfo,5000);

		if (!variable.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.InputDDE.Variable"),variable,6000);
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
		return DataCheckResult.checkDDE(this,workbook,table);
	}
}