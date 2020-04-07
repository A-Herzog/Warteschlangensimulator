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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.db.DBSettings;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Schreibt Daten in eine Datenbanktabelle.
 * @author Alexander Herzog
 */
public class ModelElementOutputDB extends ModelElementMultiInSingleOutBox implements ElementNoRemoteSimulation, ElementWithDB {
	/**
	 * Ausgabemodi für die einzelnen Einträge
	 * @see ModelElementOutputDB#getModes()
	 * @see ModelElementOutputDB#getModeNameDescriptions()
	 */
	public enum OutputMode {
		/** Gibt die Systemzeit aus */
		MODE_TIMESTAMP,

		/** Gibt einen Text aus (siehe <code>data</code>) */
		MODE_TEXT,

		/** Berechnet einen Ausdruck und gibt das Ergebnis aus (siehe <code>data</code>) */
		MODE_EXPRESSION,

		/** Gibt den Namen des Kundentyps aus */
		MODE_CLIENT,

		/** Gibt die bisherige Wartezeit des Kunden als Zahl aus */
		MODE_WAITINGTIME_NUMBER,

		/** Gibt die bisherige Wartezeit des Kunden als Zeit aus */
		MODE_WAITINGTIME_TIME,

		/** Gibt die bisherige Transferzeit des Kunden als Zahl aus */
		MODE_TRANSFERTIME_NUMBER,

		/** Gibt die bisherige Transferzeit des Kunden als Zeit aus */
		MODE_TRANSFERTIME_TIME,

		/** Gibt die bisherige Bedienzeit des Kunden als Zahl aus */
		MODE_PROCESSTIME_NUMBER,

		/** Gibt die bisherige Bedienzeit des Kunden als Zeit aus */
		MODE_PROCESSTIME_TIME,

		/** Gibt die bisherige Verweilzeit des Kunden als Zahl aus */
		MODE_RESIDENCETIME_NUMBER,

		/** Gibt die bisherige Verweilzeit des Kunden als Zeit aus */
		MODE_RESIDENCETIME_TIME,

		/** Gibt eine dem Kunden zugeordnete Zeichenkette aus */
		MODE_STRING
	}

	private DBSettings db;
	private String table;
	private List<OutputMode> mode;
	private List<String> column;
	private List<String> data;

	/**
	 * Konstruktor der Klasse <code>ModelElementOutputDB</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementOutputDB(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		db=new DBSettings();
		table="";
		mode=new ArrayList<>();
		column=new ArrayList<>();
		data=new ArrayList<>();
	}

	/**
	 * Liefert eine Liste mit Beschreibungen zu den Ausgabemodi
	 * @return	Liste mit Beschreibungen zu den Ausgabemodi
	 * @see OutputMode
	 */
	public String[] getModeNameDescriptions() {
		return new String[] {
				Language.tr("Surface.Output.XML.Element.TypeDescription.TimeStamp"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.Text"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.Expression"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.ClientType"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.WaitingTimeNumber"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.WaitingTime"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.TransferTimeNumber"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.TransferTime"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.ProcessTimeNumber"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.ProcessTime"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.ResidenceTimeNumber"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.ResidenceTime"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.String")
		};
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_OUTPUT_DB.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.OutputDB.Tooltip");
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
	 * Liefert den Namen der Tabelle in die die Daten geschrieben werden sollen.
	 * @return	Name der Tabelle in die die Daten geschrieben werden sollen
	 */
	public String getTable() {
		if (table==null) return ""; else return table;
	}

	/**
	 * Stellt den Namen der Tabelle in die die Daten geschrieben werden sollen ein.
	 * @param table	Name der Tabelle in die die Daten geschrieben werden sollen
	 */
	public void setTable(final String table) {
		if (table==null) this.table=""; else this.table=table;
	}

	/**
	 * Liefert die Liste mit den Modi der einzelnen Ausgabeelemente
	 * @return	Liste mit den Modi der Ausgabeelemente
	 */
	public List<OutputMode> getModes() {
		return mode;
	}

	/**
	 * Liefert die Liste mit den Tabellenspalten für die einzelnen Ausgabeelemente
	 * @return	Liste mit den Tabellenspalten für die einzelnen Ausgabeelemente
	 */
	public List<String> getColumns() {
		return column;
	}

	/**
	 * Liefert die Liste mit den zusätzlichen Daten der einzelnen Ausgabeelemente
	 * @return	Liste mit den zusätzlichen Daten der Ausgabeelemente
	 */
	public List<String> getData() {
		return data;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementOutputDB)) return false;

		if (!db.equalsDBSettings(((ModelElementOutputDB)element).db)) return false;
		if (!table.equals(((ModelElementOutputDB)element).table)) return false;
		if (mode.size()!=((ModelElementOutputDB)element).mode.size()) return false;
		if (column.size()!=((ModelElementOutputDB)element).column.size()) return false;
		if (data.size()!=((ModelElementOutputDB)element).data.size()) return false;
		for (int i=0;i<mode.size();i++) if (!((ModelElementOutputDB)element).mode.get(i).equals(mode.get(i))) return false;
		for (int i=0;i<column.size();i++) if (!((ModelElementOutputDB)element).column.get(i).equals(column.get(i))) return false;
		for (int i=0;i<data.size();i++) if (!((ModelElementOutputDB)element).data.get(i).equals(data.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementOutputDB) {
			db=((ModelElementOutputDB)element).db.clone();
			table=((ModelElementOutputDB)element).table;
			mode.addAll(((ModelElementOutputDB)element).mode);
			column.addAll(((ModelElementOutputDB)element).column);
			data.addAll(((ModelElementOutputDB)element).data);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementOutputDB clone(final EditModel model, final ModelSurface surface) {
		final ModelElementOutputDB element=new ModelElementOutputDB(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.OutputDB.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.OutputDB.Name.Short");
	}

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
			new ModelElementOutputDBDialog(owner,ModelElementOutputDB.this,readOnly);
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
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.OutputDB.XML.Root");
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
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputDB.XML.Table")));
			sub.setTextContent(table);
		}

		for (int i=0;i<Math.min(mode.size(),data.size());i++) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputDB.XML.Element")));

			String type="";
			switch (mode.get(i)) {
			case MODE_TIMESTAMP: type=Language.tr("Surface.Output.XML.Element.Type.TimeStamp"); break;
			case MODE_TEXT: type=Language.tr("Surface.Output.XML.Element.Type.Text"); break;
			case MODE_EXPRESSION: type=Language.tr("Surface.Output.XML.Element.Type.Expression"); break;
			case MODE_CLIENT: type=Language.tr("Surface.Output.XML.Element.Type.ClientType"); break;
			case MODE_WAITINGTIME_NUMBER: type=Language.tr("Surface.Output.XML.Element.Type.WaitingTimeNumber"); break;
			case MODE_WAITINGTIME_TIME: type=Language.tr("Surface.Output.XML.Element.Type.WaitingTime"); break;
			case MODE_TRANSFERTIME_NUMBER: type=Language.tr("Surface.Output.XML.Element.Type.TransferTimeNumber"); break;
			case MODE_TRANSFERTIME_TIME: type=Language.tr("Surface.Output.XML.Element.Type.TransferTime"); break;
			case MODE_PROCESSTIME_NUMBER: type=Language.tr("Surface.Output.XML.Element.Type.ProcessTimeNumber"); break;
			case MODE_PROCESSTIME_TIME: type=Language.tr("Surface.Output.XML.Element.Type.ProcessTime"); break;
			case MODE_RESIDENCETIME_NUMBER: type=Language.tr("Surface.Output.XML.Element.Type.ResidenceTimeNumber"); break;
			case MODE_RESIDENCETIME_TIME: type=Language.tr("Surface.Output.XML.Element.Type.ResidenceTime"); break;
			case MODE_STRING: type=Language.tr("Surface.Output.XML.Element.Type.String"); break;
			}
			sub.setAttribute(Language.trPrimary("Surface.OutputDB.XML.Element.Type"),type);
			sub.setAttribute(Language.trPrimary("Surface.OutputDB.XML.Element.Column"),column.get(i));
			if (!data.get(i).isEmpty()) sub.setAttribute(Language.trPrimary("Surface.OutputDB.XML.Element.Data"),data.get(i));
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

		if (Language.trAll("Surface.OutputDB.XML.Table",name)) {
			table=content;
			return null;
		}

		if (Language.trAll("Surface.OutputDB.XML.Element",name)) {
			final String m=Language.trAllAttribute("Surface.OutputDB.XML.Element.Type",node);
			final String c=Language.trAllAttribute("Surface.OutputDB.XML.Element.Column",node);
			final String d=Language.trAllAttribute("Surface.OutputDB.XML.Element.Data",node);
			OutputMode index=OutputMode.MODE_TIMESTAMP;
			boolean ok=false;
			if (Language.trAll("Surface.Output.XML.Element.Type.TimeStamp",m)) {index=OutputMode.MODE_TIMESTAMP; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.Text",m)) {index=OutputMode.MODE_TEXT; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.Expression",m)) {index=OutputMode.MODE_EXPRESSION; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.ClientType",m)) {index=OutputMode.MODE_CLIENT; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.WaitingTimeNumber",m)) {index=OutputMode.MODE_WAITINGTIME_NUMBER; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.WaitingTime",m)) {index=OutputMode.MODE_WAITINGTIME_TIME; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.TransferTimeNumber",m)) {index=OutputMode.MODE_TRANSFERTIME_NUMBER; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.TransferTime",m)) {index=OutputMode.MODE_TRANSFERTIME_TIME; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.ProcessTimeNumber",m)) {index=OutputMode.MODE_PROCESSTIME_NUMBER; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.ProcessTime",m)) {index=OutputMode.MODE_PROCESSTIME_TIME; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.ResidenceTimeNumber",m)) {index=OutputMode.MODE_RESIDENCETIME_NUMBER; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.ResidenceTime",m)) {index=OutputMode.MODE_RESIDENCETIME_TIME; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.String",m)) {index=OutputMode.MODE_STRING; ok=true;}
			if (!ok) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.OutputDB.XML.Element.Type"),name,node.getParentNode().getNodeName());
			mode.add(index);
			column.add(c);
			data.add(d);
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementOutputDB";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		db.buildDescription(descriptionBuilder,1000);

		if (table!=null && !table.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.OutputDB.Table"),table,2000);


		final String[] modeDesciptions=getModeNameDescriptions();
		for (int i=0;i<mode.size();i++) {
			final OutputMode m=mode.get(i);
			final String value;
			String text="";
			switch (m) {
			case MODE_TIMESTAMP: text=modeDesciptions[0]; break;
			case MODE_TEXT: text=modeDesciptions[1]; break;
			case MODE_EXPRESSION: text=modeDesciptions[2]; break;
			case MODE_CLIENT: text=modeDesciptions[3]; break;
			case MODE_WAITINGTIME_NUMBER: text=modeDesciptions[4]; break;
			case MODE_WAITINGTIME_TIME: text=modeDesciptions[5]; break;
			case MODE_TRANSFERTIME_NUMBER: text=modeDesciptions[6]; break;
			case MODE_TRANSFERTIME_TIME: text=modeDesciptions[7]; break;
			case MODE_PROCESSTIME_NUMBER: text=modeDesciptions[8]; break;
			case MODE_PROCESSTIME_TIME: text=modeDesciptions[9]; break;
			case MODE_RESIDENCETIME_NUMBER: text=modeDesciptions[10]; break;
			case MODE_RESIDENCETIME_TIME: text=modeDesciptions[11]; break;
			case MODE_STRING: text=modeDesciptions[12]; break;
			}

			if (m==OutputMode.MODE_TEXT || m==OutputMode.MODE_EXPRESSION || m==OutputMode.MODE_STRING) value=text+": "+data.get(i); else value=text;
			final String output=value+" ("+Language.tr("ModelDescription.OutputDB.Column")+": "+column.get(i)+")";
			descriptionBuilder.addProperty(Language.tr("ModelDescription.OutputDB.Property"),output,3000);
		}
	}

	@Override
	public DataCheckResult checkExternalData() {
		return DataCheckResult.checkDB(this,db);
	}
}