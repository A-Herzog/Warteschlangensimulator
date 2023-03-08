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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementOutput;
import simulator.simparser.ExpressionCalc;
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
	 * Ausgabemodi f�r die einzelnen Eintr�ge
	 * @see ModelElementOutput#getOutput()
	 * @see ModelElementOutput#getModeNameDescriptions()
	 */
	public enum OutputMode {
		/** Gibt die Systemzeit aus */
		MODE_TIMESTAMP(),

		/** Gibt einen Text aus (siehe <code>data</code>) */
		MODE_TEXT(true),

		/** Gibt einen Tabulator aus */
		MODE_TABULATOR(),

		/** Gibt einen Zeilenumbruch aus */
		MODE_NEWLINE(),

		/** Berechnet einen Ausdruck und gibt das Ergebnis aus (siehe <code>data</code>) */
		MODE_EXPRESSION(true),

		/** Gibt den Namen des Kundentyps aus */
		MODE_CLIENT(),

		/** Gibt die bisherige Wartezeit des Kunden als Zahl aus */
		MODE_WAITINGTIME_NUMBER(),

		/** Gibt die bisherige Wartezeit des Kunden als Zeit aus */
		MODE_WAITINGTIME_TIME(),

		/** Gibt die bisherige Transferzeit des Kunden als Zahl aus */
		MODE_TRANSFERTIME_NUMBER(),

		/** Gibt die bisherige Transferzeit des Kunden als Zeit aus */
		MODE_TRANSFERTIME_TIME(),

		/** Gibt die bisherige Bedienzeit des Kunden als Zahl aus */
		MODE_PROCESSTIME_NUMBER(),

		/** Gibt die bisherige Bedienzeit des Kunden als Zeit aus */
		MODE_PROCESSTIME_TIME(),

		/** Gibt die bisherige Verweilzeit des Kunden als Zahl aus */
		MODE_RESIDENCETIME_NUMBER(),

		/** Gibt die bisherige Verweilzeit des Kunden als Zeit aus */
		MODE_RESIDENCETIME_TIME(),

		/** Gibt eine dem Kunden zugeordnete Zeichenkette aus */
		MODE_STRING(true);

		/**
		 * Gibt es zu diesem Ausgabetyp zus�tzlich Daten
		 * @see OutputRecord#data
		 */
		public final boolean hasData;

		/**
		 * Konstruktor der Enum<br>
		 * (Modus: keine zus�tzlichen Daten)
		 */
		OutputMode() {
			hasData=false;
		}

		/**
		 * Konstruktor der Enum
		 * @param hasData	Gibt es zu diesem Ausgabetyp zus�tzlich Daten
		 */
		OutputMode(final boolean hasData) {
			this.hasData=hasData;
		}
	}

	/**
	 * �berschriftenmodus f�r die Ausgabe
	 * @see ModelElementOutput#headingMode
	 */
	public enum HeadingMode {
		/** Keine �berschriften */
		OFF,
		/** Automatisch generierte �berschriften f�r Tabellen, f�r Texte nichts */
		AUTO,
		/** Benutzerdefinierte �berschriften */
		USER_DEFINED
	}

	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @see #isOutputActive()
	 * @see #setOutputActive(boolean)
	 */
	private boolean outputActive;

	/**
	 * Dateiname der Datei f�r die Ausgaben
	 * @see #getOutputFile()
	 * @see #setOutputFile(String)
	 */
	private String outputFile;

	/**
	 * Soll eine m�glicherweise bestehende Datei beim Start der Ausgabe �berschrieben werden? (Ansonsten wird angeh�ngt)
	 */
	private boolean outputFileOverwrite;

	/**
	 * Zahlen im lokalen Format (<code>false</code>) oder im System-Format (<code>true</code>) ausgeben?
	 * @see #isSystemFormat()
	 * @see #setSystemFormat(boolean)
	 */
	private boolean systemFormat;

	/**
	 * Liste der Ausgabeelemente
	 * @see #getOutput()
	 */
	private final List<OutputRecord> output;

	/**
	 * �berschriftenmodus f�r die Ausgabe
	 * @see #getHeadingMode()
	 * @see #setHeadingMode(HeadingMode)
	 * @see HeadingMode
	 */
	private HeadingMode headingMode;

	/**
	 * Liste mit den Ausgabeelementen f�r die �berschrift im Modus {@link HeadingMode#USER_DEFINED}
	 * @see HeadingMode#USER_DEFINED
	 * @see #getHeadingMode()
	 * @see #setHeadingMode(HeadingMode)
	 * @see #getOutputHeading()
	 */
	private final List<OutputRecord> outputHeading;

	/**
	 * Konstruktor der Klasse <code>ModelElementOutput</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementOutput(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		outputActive=true;
		output=new ArrayList<>();
		headingMode=HeadingMode.AUTO;
		outputHeading=new ArrayList<>();
		outputFile="";
		outputFileOverwrite=false;
		systemFormat=false;
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
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_OUTPUT.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Output.Tooltip");
	}

	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @return	Ausgabe aktiv
	 * @see #setOutputActive(boolean)
	 */
	public boolean isOutputActive() {
		return outputActive;
	}

	/**
	 * Stellt ein, ob die Ausgabe aktiv sein soll.
	 * @param outputActive	Ausgabe aktiv
	 * @see #isOutputActive()
	 */
	public void setOutputActive(boolean outputActive) {
		this.outputActive=outputActive;
	}

	/**
	 * Liefert die Liste der einzelnen Ausgabeelemente.
	 * @return	Liste der Ausgabeelemente
	 */
	public List<OutputRecord> getOutput() {
		return output;
	}


	/**
	 * Liefert den �berschriftenmodus f�r die Ausgabe.
	 * @return	�berschriftenmodus f�r die Ausgabe
	 * @see #setHeadingMode(HeadingMode)
	 * @see #getOutputHeading()
	 */
	public HeadingMode getHeadingMode() {
		return headingMode;
	}

	/**
	 * Stellt den �berschriftenmodus f�r die Ausgabe ein.
	 * @param headingMode	�berschriftenmodus f�r die Ausgabe
	 * @see #getHeadingMode()
	 * @see #getOutputHeading()
	 */
	public void setHeadingMode(final HeadingMode headingMode) {
		this.headingMode=(headingMode==null)?HeadingMode.AUTO:headingMode;
	}

	/**
	 * Liefert die Liste der einzelnen Ausgabeelemente f�r die �berschrift.
	 * @return	Liste der Ausgabeelemente f�r die �berschrift
	 * @see HeadingMode#USER_DEFINED
	 */
	public List<OutputRecord> getOutputHeading() {
		return outputHeading;
	}

	/**
	 * Liefert den Dateinamen der Datei, die f�r die Speicherung der Ausgaben verwendet werden soll.
	 * @return	Dateiname der Datei f�r die Ausgaben
	 * @see #setOutputFile(String)
	 */
	@Override
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Stellt den Dateinamen der Datei, die f�r die Speicherung der Ausgaben verwendet werden soll, ein.
	 * @param outputFile	Dateiname der Datei f�r die Ausgaben
	 * @see #getOutputFile()
	 */
	@Override
	public void setOutputFile(final String outputFile) {
		if (outputFile!=null) this.outputFile=outputFile;
	}

	/**
	 * Liefert den Dateinamen der Datei, die f�r die Speicherung der Ausgaben verwendet werden soll.
	 * Der Dateiname wird dabei gegen�ber {@link #getOutputFile()} wenn n�tig um einen Pfad erg�nzt.
	 * @param modelPath	Pfad zur aktuellen Modelldatei (dient als Basis f�r relative Pfade)
	 * @return	Dateiname der Datei f�r die Ausgaben
	 */
	public File getOutputFileWithFullPath(String modelPath) {
		if (modelPath==null) modelPath="";
		modelPath=modelPath.trim();
		if (!modelPath.endsWith("\\")) modelPath=modelPath+"\\";


		if (this.outputFile==null) return new File(modelPath+"output.txt");
		String outputFile=this.outputFile.trim();

		if (outputFile.startsWith("\\\\") || (outputFile.length()>1 && outputFile.charAt(1)==':')) return new File(outputFile);

		if (outputFile.startsWith("\\")) outputFile=outputFile.substring(1);

		return new File(modelPath+outputFile);
	}

	/**
	 * Soll die Ausgabedatei beim Start einer Simulation �berschrieben werden?
	 * @return	�berschreiben (<code>true</code>) oder anh�ngen (<code>false</code>)
	 * @see #setOutputFileOverwrite(boolean)
	 */
	public boolean isOutputFileOverwrite() {
		return outputFileOverwrite;
	}

	/**
	 * Soll die Ausgabedatei beim Start einer Simulation �berschrieben werden?
	 * @param outputFileOverwrite	�berschreiben (<code>true</code>) oder anh�ngen (<code>false</code>)
	 * @see #isOutputFileOverwrite()
	 */
	public void setOutputFileOverwrite(boolean outputFileOverwrite) {
		this.outputFileOverwrite=outputFileOverwrite;
	}

	/**
	 * Sollen Zahlen im lokalen Format (<code>false</code>) oder im System-Format (<code>true</code>) ausgeben werden?
	 * @return	Ausgabe von Zahlen im System-Format (d.h. immer mit Dezimalpunkt)?
	 */
	public boolean isSystemFormat() {
		return systemFormat;
	}

	/**
	 * Stellt ein, ob Zahlen im lokalen Format (<code>false</code>) oder im System-Format (<code>true</code>) ausgeben werden sollen.
	 * @param systemFormat	Ausgabe im System-Format (d.h. immer mit Dezimalpunkt)?
	 */
	public void setSystemFormat(boolean systemFormat) {
		this.systemFormat=systemFormat;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementOutput)) return false;
		final ModelElementOutput other=(ModelElementOutput)element;

		if (outputActive!=other.outputActive) return false;

		if (outputFile!=null) {
			if (!outputFile.equals(other.outputFile)) return false;
			final String outputFileLower=outputFile.toLowerCase();
			if (outputFileLower.endsWith(".txt") || outputFileLower.endsWith(".tsv")) {
				if (systemFormat!=other.systemFormat) return false;
			}
		} else {
			if (other.outputFile!=null) return false;
		}
		if (outputFileOverwrite!=other.outputFileOverwrite) return false;

		if (output.size()!=other.output.size()) return false;
		for (int i=0;i<output.size();i++) if (!other.output.get(i).equalsOutputRecord(output.get(i))) return false;

		if (headingMode!=other.headingMode) return false;

		if (outputHeading.size()!=other.outputHeading.size()) return false;
		for (int i=0;i<outputHeading.size();i++) if (!other.outputHeading.get(i).equalsOutputRecord(outputHeading.get(i))) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementOutput) {
			final ModelElementOutput source=(ModelElementOutput)element;
			outputActive=source.outputActive;
			outputFile=source.outputFile;
			outputFileOverwrite=source.outputFileOverwrite;
			systemFormat=source.systemFormat;
			output.addAll(source.output.stream().map(record->new OutputRecord(record)).collect(Collectors.toList()));
			headingMode=source.headingMode;
			outputHeading.addAll(source.outputHeading.stream().map(record->new OutputRecord(record)).collect(Collectors.toList()));
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementOutput clone(final EditModel model, final ModelSurface surface) {
		final ModelElementOutput element=new ModelElementOutput(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
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
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null || outputActive) return null;
		return Language.tr("Surface.Output.Disabled");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(230,230,230);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementOutputDialog(owner,ModelElementOutput.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsData(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param surfacePanel	Zeichenfl�che
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
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
	 * �ffnet die definierte Ausgabedatei mit dem Standardprogramm
	 * @return	Gibt <code>true</code> zur�ck, wenn die Datei ge�ffnet werden konnte.
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
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Output.XML.Root");
	}

	/**
	 * Schreibt die Daten eines einzelnen Ausgabeelements in einen xml-Knoten
	 * @param record	Auszugebendes Ausgabeelement
	 * @param sub	xml-Knoten in den die Daten geschrieben werden sollen (es wird kein Unterknoten erzeugt, sondern direkt in den angegebenen Knoten geschrieben)
	 * @see #addPropertiesDataToXML(Document, Element)
	 */
	private void writeOutputRecord(final OutputRecord record, final Element sub) {
		String type="";
		switch (record.mode) {
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
		if (!record.data.isEmpty()) sub.setAttribute(Language.trPrimary("Surface.Output.XML.Element.Data"),record.data);
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (!outputActive) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Output.XML.Active")));
			sub.setTextContent("0");
		}

		if (!outputFile.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Output.XML.File")));
			sub.setTextContent(outputFile);
			if (systemFormat) {
				sub.setAttribute(Language.trPrimary("Surface.Output.XML.File.SystemFormat"),"1");
			}
			if (outputFileOverwrite) sub.setAttribute(Language.trPrimary("Surface.Output.XML.File.Overwrite"),"1");
			switch (headingMode) {
			case OFF:
				sub.setAttribute(Language.trPrimary("Surface.Output.XML.File.HeadingMode"),Language.trPrimary("Surface.Output.XML.File.HeadingMode.Off"));
				break;
			case AUTO:
				/* Dies ist der Vorgabemodus, der muss nicht in der Konfiguration erw�hnt werden. */
				break;
			case USER_DEFINED:
				sub.setAttribute(Language.trPrimary("Surface.Output.XML.File.HeadingMode"),Language.trPrimary("Surface.Output.XML.File.HeadingMode.UserDefined"));
				break;
			}
		}

		for (OutputRecord record: output) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Output.XML.Element")));
			writeOutputRecord(record,sub);
		}

		for (OutputRecord record: outputHeading) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Output.XML.ElementHeading")));
			writeOutputRecord(record,sub);
		}
	}

	/**
	 * L�dt die Daten zu einem einzelnen Ausgabeelement aus einem xml-Knoten
	 * @param name	Name des xml-Elements
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Liefert im Erfolgsfall das neue Ausgabeelement, sonst eine Fehlermeldung
	 */
	private Object loadOutputRecord(final String name, final Element node) {
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
		return new OutputRecord(index,d);
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.Output.XML.Active",name)) {
			outputActive=!content.equals("0");
			return null;
		}

		if (Language.trAll("Surface.Output.XML.File",name)) {
			outputFile=content;

			final String systemFormat=Language.trAllAttribute("Surface.Output.XML.File.SystemFormat",node);
			if (!systemFormat.isEmpty() && !systemFormat.equals("0")) this.systemFormat=true;

			final String overwrite=Language.trAllAttribute("Surface.Output.XML.File.Overwrite",node);
			if (!overwrite.isEmpty() && !overwrite.equals("0")) outputFileOverwrite=true;

			final String headingModeString=Language.trAllAttribute("Surface.Output.XML.File.HeadingMode",node);
			if (Language.trAll("Surface.Output.XML.File.HeadingMode.Off",headingModeString)) headingMode=HeadingMode.OFF;
			if (Language.trAll("Surface.Output.XML.File.HeadingMode.Auto",headingModeString)) headingMode=HeadingMode.AUTO;
			if (Language.trAll("Surface.Output.XML.File.HeadingMode.UserDefined",headingModeString)) headingMode=HeadingMode.USER_DEFINED;

			return null;
		}

		if (Language.trAll("Surface.Output.XML.Element",name)) {
			final Object record=loadOutputRecord(name,node);
			if (record instanceof String) return (String)record;
			output.add((OutputRecord)record);
			return null;
		}

		if (Language.trAll("Surface.Output.XML.ElementHeading",name)) {
			final Object record=loadOutputRecord(name,node);
			if (record instanceof String) return (String)record;
			outputHeading.add((OutputRecord)record);
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementOutput";
	}

	/**
	 * Gibt die Daten zu einem Ausgabeelement in der Modellbeschreibung aus
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param record	Auszugebendes Ausgabeelement
	 * @param name	Name des Elements
	 * @param position	Position des Elements
	 * @see #buildDescription(ModelDescriptionBuilder)
	 */
	private void buildOutputDescription(final ModelDescriptionBuilder descriptionBuilder, final OutputRecord record, final String name, final int position) {
		final String[] modeDesciptions=getModeNameDescriptions();

		final String value;
		String text="";
		switch (record.mode) {
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

		if (record.mode.hasData) value=text+": "+record.data; else value=text;
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.Property"),value,position);
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (headingMode==HeadingMode.USER_DEFINED) for (OutputRecord record: outputHeading) {
			buildOutputDescription(descriptionBuilder,record,Language.tr("ModelDescription.Output.PropertyHeading"),1000);
		}

		for (OutputRecord record: output) {
			buildOutputDescription(descriptionBuilder,record,Language.tr("ModelDescription.Output.Property"),2000);
		}

		if (!outputFile.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.File"),outputFile,3000);
		}

		if (!outputActive) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.Active"),Language.tr("ModelDescription.Output.Active.Off"),10000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputFile"),outputFile,newOutputFile->{outputFile=newOutputFile;});

		/* Ausgabedaten */
		for (int i=0;i<output.size();i++) {
			final int index=i;
			final OutputMode mode=output.get(index).mode;
			final String data=output.get(index).data;
			if (mode==OutputMode.MODE_TEXT) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputText"),data,newText->output.set(index,new OutputRecord(mode,newText)));
			}
			if (mode==OutputMode.MODE_EXPRESSION) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputExpression"),data,newExpression->output.set(index,new OutputRecord(mode,newExpression)));
			}
			if (mode==OutputMode.MODE_STRING) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputString"),data,newString->output.set(index,new OutputRecord(mode,newString)));
			}
		}

		/* �berschrift-Ausgaben */
		if (headingMode==HeadingMode.USER_DEFINED) for (int i=0;i<outputHeading.size();i++) {
			final int index=i;
			final OutputMode mode=outputHeading.get(index).mode;
			final String data=outputHeading.get(index).data;
			if (mode==OutputMode.MODE_TEXT) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputText"),data,newText->outputHeading.set(index,new OutputRecord(mode,newText)));
			}
			if (mode==OutputMode.MODE_EXPRESSION) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputExpression"),data,newExpression->outputHeading.set(index,new OutputRecord(mode,newExpression)));
			}
			if (mode==OutputMode.MODE_STRING) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputString"),data,newString->outputHeading.set(index,new OutputRecord(mode,newString)));
			}
		}
	}

	/**
	 * Diese Klasse kapselt ein einzelnes Ausgabeelement.
	 * @see ModelElementOutput#output
	 * @see ModelElementOutput#outputHeading
	 * @see ModelElementOutput#getOutput()
	 * @see ModelElementOutput#getOutputHeading()
	 */
	public static class OutputRecord {
		/**
		 * Was soll ausgegeben werden?
		 * @see OutputMode
		 */
		public final OutputMode mode;

		/**
		 * Optionale zus�tzliche Daten (kann leer sein, ist aber nie <code>null</code>)
		 */
		public final String data;

		/**
		 * Rechenausdruck (wird nur verwendet, wenn dieses Objekt von {@link RunElementOutput} erstellt wurde)
		 */
		public final ExpressionCalc calc;

		/**
		 * Konstruktor der Klasse
		 */
		public OutputRecord() {
			mode=OutputMode.MODE_TIMESTAMP;
			data="";
			calc=null;
		}

		/**
		 * Konstruktor der Klasse
		 * @param mode	Was soll ausgegeben werden?
		 * @param data	Optionale zus�tzliche Daten
		 */
		public OutputRecord(final OutputMode mode, final String data) {
			this.mode=(mode==null)?OutputMode.MODE_TIMESTAMP:mode;
			this.data=(data==null)?"":data;
			calc=null;
		}

		/**
		 * Konstruktor der Klasse (wird nur von {@link RunElementOutput} verwendet)
		 * @param data	Rechenausdruck
		 */
		public OutputRecord(final ExpressionCalc data) {
			this.mode=OutputMode.MODE_EXPRESSION;
			this.data=data.getText();
			this.calc=data;
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param source	Zu kopierendes Ausgangselement
		 */
		public OutputRecord(final OutputRecord source) {
			if (source!=null) {
				mode=source.mode;
				data=source.data;
				calc=source.calc;
			} else {
				mode=OutputMode.MODE_TIMESTAMP;
				data="";
				calc=null;
			}
		}

		/**
		 * Vergleicht dieses Ausgabeelement mit einem weiteren Ausgabeelement
		 * @param other	Weiteres Ausgabeelement f�r den Vergleich
		 * @return	Liefert <code>true</code>, wenn beide Ausgabeelement inhaltlich identisch sind
		 */
		public boolean equalsOutputRecord(final OutputRecord other) {
			if (other==null) return false;
			if (other==this) return true;
			if (mode!=other.mode) return false;
			if (!mode.hasData) return true;
			return Objects.equals(data,other.data);
		}
	}
}
