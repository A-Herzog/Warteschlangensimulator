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
import java.awt.Desktop;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Schreibt Daten in eine Datei.
 * @author Alexander Herzog
 */
public class ModelElementOutput extends ModelElementMultiInSingleOutBox implements ElementNoRemoteSimulation, ElementWithOutputFile {
	/**
	 * Ausgabemodi für die einzelnen Einträge
	 * @see ModelElementOutput#getModes()
	 * @see ModelElementOutput#getModeNameDescriptions()
	 */
	public enum OutputMode {
		/** Gibt die Systemzeit aus */
		MODE_TIMESTAMP,

		/** Gibt einen Text aus (siehe <code>data</code>) */
		MODE_TEXT,

		/** Gibt einen Tabulator aus */
		MODE_TABULATOR,

		/** Gibt einen Zeilenumbruch aus */
		MODE_NEWLINE,

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

	/**
	 * Dateiname der Datei für die Ausgaben
	 * @see #getOutputFile()
	 * @see #setOutputFile(String)
	 */
	private String outputFile;

	/**
	 * Liste mit den Modi der Ausgabeelemente
	 * @see #getModes()
	 */
	private List<OutputMode> mode;

	/**
	 * Liste mit den zusätzlichen Daten der Ausgabeelemente
	 * @see #getData()
	 */
	private List<String> data;

	/**
	 * Konstruktor der Klasse <code>ModelElementOutput</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementOutput(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		mode=new ArrayList<>();
		data=new ArrayList<>();
		outputFile="";
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
				Language.tr("Surface.Output.XML.Element.TypeDescription.Tabulator"),
				Language.tr("Surface.Output.XML.Element.TypeDescription.LineBreak"),
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
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_OUTPUT.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Output.Tooltip");
	}

	/**
	 * Liefert die Liste mit den Modi der einzelnen Ausgabeelemente
	 * @return	Liste mit den Modi der Ausgabeelemente
	 */
	public List<OutputMode> getModes() {
		return mode;
	}

	/**
	 * Liefert die Liste mit den zusätzlichen Daten der einzelnen Ausgabeelemente
	 * @return	Liste mit den zusätzlichen Daten der Ausgabeelemente
	 */
	public List<String> getData() {
		return data;
	}

	/**
	 * Liefert den Dateinamen der Datei, die für die Speicherung der Ausgaben verwendet werden soll.
	 * @return	Dateiname der Datei für die Ausgaben
	 */
	@Override
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Stellt den Dateinamen der Datei, die für die Speicherung der Ausgaben verwendet werden soll, ein.
	 * @param outputFile	Dateiname der Datei für die Ausgaben
	 */
	@Override
	public void setOutputFile(final String outputFile) {
		if (outputFile!=null) this.outputFile=outputFile;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementOutput)) return false;

		if (!outputFile.equals(((ModelElementOutput)element).outputFile)) return false;
		if (mode.size()!=((ModelElementOutput)element).mode.size()) return false;
		if (data.size()!=((ModelElementOutput)element).data.size()) return false;
		for (int i=0;i<mode.size();i++) if (!((ModelElementOutput)element).mode.get(i).equals(mode.get(i))) return false;
		for (int i=0;i<data.size();i++) if (!((ModelElementOutput)element).data.get(i).equals(data.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementOutput) {
			outputFile=((ModelElementOutput)element).outputFile;
			mode.addAll(((ModelElementOutput)element).mode);
			data.addAll(((ModelElementOutput)element).data);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementOutput clone(final EditModel model, final ModelSurface surface) {
		final ModelElementOutput element=new ModelElementOutput(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Output.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Output.Name.Short");
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
			new ModelElementOutputDialog(owner,ModelElementOutput.this,readOnly);
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
		if (outputFile!=null && !outputFile.trim().isEmpty()) {
			final File output=new File(outputFile);
			if (output.isFile()) {
				final JMenuItem item=new JMenuItem("<html><body><b>"+Language.tr("Surface.Output.OpenOutputFile")+"</b></body></html>");
				item.setIcon(Images.MODELEDITOR_OPEN_OUTPUT_FILE.getIcon());
				item.addActionListener(e->openOutputFile());
				popupMenu.add(item);
			}
		}

		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Öffnet die definierte Ausgabedatei mit dem Standardprogramm
	 * @return	Gibt <code>true</code> zurück, wenn die Datei geöffnet werden konnte.
	 */
	public boolean openOutputFile() {
		if (outputFile==null || outputFile.trim().isEmpty()) return false;
		final File output=new File(outputFile);
		if (!output.isFile()) return false;
		try {
			Desktop.getDesktop().open(output);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Output.XML.Root");
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

		if (!outputFile.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Output.XML.File")));
			sub.setTextContent(outputFile);
		}

		for (int i=0;i<Math.min(mode.size(),data.size());i++) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Output.XML.Element")));

			String type="";
			switch (mode.get(i)) {
			case MODE_TIMESTAMP: type=Language.tr("Surface.Output.XML.Element.Type.TimeStamp"); break;
			case MODE_TEXT: type=Language.tr("Surface.Output.XML.Element.Type.Text"); break;
			case MODE_TABULATOR: type=Language.tr("Surface.Output.XML.Element.Type.Tabulator"); break;
			case MODE_NEWLINE: type=Language.tr("Surface.Output.XML.Element.Type.LineBreak"); break;
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
			sub.setAttribute(Language.trPrimary("Surface.Output.XML.Element.Type"),type);
			if (!data.get(i).isEmpty()) sub.setAttribute(Language.trPrimary("Surface.Output.XML.Element.Data"),data.get(i));
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

		if (Language.trAll("Surface.Output.XML.File",name)) {
			outputFile=content;
			return null;
		}

		if (Language.trAll("Surface.Output.XML.Element",name)) {
			final String m=Language.trAllAttribute("Surface.Output.XML.Element.Type",node);
			final String d=Language.trAllAttribute("Surface.Output.XML.Element.Data",node);
			OutputMode index=OutputMode.MODE_TIMESTAMP;
			boolean ok=false;
			if (Language.trAll("Surface.Output.XML.Element.Type.TimeStamp",m)) {index=OutputMode.MODE_TIMESTAMP; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.Text",m)) {index=OutputMode.MODE_TEXT; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.Tabulator",m)) {index=OutputMode.MODE_TABULATOR; ok=true;}
			if (Language.trAll("Surface.Output.XML.Element.Type.LineBreak",m)) {index=OutputMode.MODE_NEWLINE; ok=true;}
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
			if (!ok) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Output.XML.Element.Type"),name,node.getParentNode().getNodeName());
			mode.add(index);
			data.add(d);
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementOutput";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		final String[] modeDesciptions=getModeNameDescriptions();
		for (int i=0;i<mode.size();i++) {
			final OutputMode m=mode.get(i);
			final String value;
			String text="";
			switch (m) {
			case MODE_TIMESTAMP: text=modeDesciptions[0]; break;
			case MODE_TEXT: text=modeDesciptions[1]; break;
			case MODE_TABULATOR: text=modeDesciptions[2]; break;
			case MODE_NEWLINE: text=modeDesciptions[3]; break;
			case MODE_EXPRESSION: text=modeDesciptions[4]; break;
			case MODE_CLIENT: text=modeDesciptions[5]; break;
			case MODE_WAITINGTIME_NUMBER: text=modeDesciptions[6]; break;
			case MODE_WAITINGTIME_TIME: text=modeDesciptions[7]; break;
			case MODE_TRANSFERTIME_NUMBER: text=modeDesciptions[8]; break;
			case MODE_TRANSFERTIME_TIME: text=modeDesciptions[9]; break;
			case MODE_PROCESSTIME_NUMBER: text=modeDesciptions[10]; break;
			case MODE_PROCESSTIME_TIME: text=modeDesciptions[11]; break;
			case MODE_RESIDENCETIME_NUMBER: text=modeDesciptions[12]; break;
			case MODE_RESIDENCETIME_TIME: text=modeDesciptions[13]; break;
			case MODE_STRING: text=modeDesciptions[14]; break;
			}

			if (m==OutputMode.MODE_TEXT || m==OutputMode.MODE_EXPRESSION || m==OutputMode.MODE_STRING) value=text+": "+data.get(i); else value=text;
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.Property"),value,1000);
		}

		if (!outputFile.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.File"),outputFile,2000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputFile"),outputFile,newOutputFile->{outputFile=newOutputFile;});

		/* Ausgabedaten */
		for (int i=0;i<mode.size();i++) {
			final int index=i;
			if (mode.get(index)==OutputMode.MODE_TEXT) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputText"),data.get(index),newText->data.set(index,newText));
			}
			if (mode.get(index)==OutputMode.MODE_EXPRESSION) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputExpression"),data.get(index),newExpression->data.set(index,newExpression));
			}
		}
	}
}
