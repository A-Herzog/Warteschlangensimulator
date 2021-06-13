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
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Liest eine Zahl aus einer Datei und speichert diese in einer Variable.
 * @author Alexander Herzog
 */
public class ModelElementInput extends ModelElementMultiInSingleOutBox implements ElementWithNewVariableNames, ElementNoRemoteSimulation, ElementWithInputFile {
	/**
	 * Name der Datei, aus der die Werte gelesen werden sollen
	 * @see #getInputFile()
	 * @see #setInputFile(String)
	 */
	private String inputFile;

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
	 * Vorgabewert an, der verwendet wird, wenn im Modus {@link ModelElementInput.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @see #getDefaultValue()
	 * @see #setDefaultValue(String)
	 */
	private String defaultValue;

	/**
	 * Verhalten am Dateiende
	 * @see ModelElementInput#getEofMode()
	 * @see ModelElementInput#setEofMode(EofModes)
	 * @see ModelElementInput#getDefaultValue()
	 * @see ModelElementInput#setDefaultValue(String)
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
	 * Konstruktor der Klasse <code>ModelElementInput</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInput(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		inputFile="";
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
		return Images.MODELEDITOR_ELEMENT_INPUT.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Input.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementInput)) return false;

		if (!inputFile.equals(((ModelElementInput)element).inputFile)) return false;
		if (!variable.equals(((ModelElementInput)element).variable)) return false;
		if (eofMode!=((ModelElementInput)element).eofMode) return false;
		if (!defaultValue.equals(((ModelElementInput)element).defaultValue)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInput) {
			inputFile=((ModelElementInput)element).inputFile;
			variable=((ModelElementInput)element).variable;
			eofMode=((ModelElementInput)element).eofMode;
			defaultValue=((ModelElementInput)element).defaultValue;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInput clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInput element=new ModelElementInput(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Input.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Input.Name");
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
	 * Liefert den Namen der Datei, aus der die Werte gelesen werden sollen
	 * @return	Name der Datei, aus der die Werte gelesen werden sollen
	 * @see ModelElementInput#setInputFile(String)
	 */
	@Override
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * Stellt den Namen der Datei, aus der die Werte gelesen werden sollen, ein.
	 * @param inputFile	Name der Datei, aus der die Werte gelesen werden sollen
	 * @see ModelElementInput#getInputFile()
	 */
	@Override
	public void setInputFile(final String inputFile) {
		if (inputFile==null) this.inputFile=""; else this.inputFile=inputFile;
	}

	/**
	 * Liefert den Namen der Variable, in die der Wert geschrieben werden soll
	 * @return	Name der Variable, in die der Wert geschrieben werden soll
	 * @see ModelElementInput#setVariable(String)
	 */
	public String getVariable() {
		return variable;
	}

	/**
	 * Stellt den Namen der Variable, in die der Wert geschrieben werden soll, ein.
	 * @param variable	Name der Variable, in die der Wert geschrieben werden soll
	 * @see ModelElementInput#getVariable()
	 */
	public void setVariable(String variable) {
		if (variable==null) this.variable=""; else this.variable=variable;
	}

	/**
	 * Gibt an wie beim Erreichen des Dateiendes verfahren werden soll.
	 * @return	Verhalten beim Erreichen des Dateiendes
	 * @see ModelElementInput#setEofMode(EofModes)
	 * @see EofModes
	 */
	public EofModes getEofMode() {
		return eofMode;
	}

	/**
	 * Stellt ein wie beim Erreichen des Dateiendes verfahren werden soll.
	 * @param eofMode	Verhalten beim Erreichen des Dateiendes
	 * @see ModelElementInput#getEofMode()
	 * @see EofModes
	 */
	public void setEofMode(EofModes eofMode) {
		this.eofMode=eofMode;
	}

	/**
	 * Gibt den Vorgabewert an, der verwendet wird, wenn im Modus {@link ModelElementInput.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @return	Vorgabewert
	 * @see ModelElementInput#setDefaultValue(String)
	 * @see EofModes#EOF_MODE_DEFAULT_VALUE
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Stellt den Vorgabewert ein, der verwendet wird, wenn im Modus {@link ModelElementInput.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @param defaultValue	Vorgabewert
	 * @see ModelElementInput#getDefaultValue()
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
			new ModelElementInputDialog(owner,ModelElementInput.this,readOnly);
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
		if (inputFile!=null && !inputFile.trim().isEmpty()) {
			final File input=new File(inputFile);
			if (input.isFile()) {
				final JMenuItem item=new JMenuItem("<html><body><b>"+Language.tr("Surface.Output.OpenInputFile")+"</b></body></html>");
				item.setIcon(Images.MODELEDITOR_OPEN_INPUT_FILE.getIcon());
				item.addActionListener(e->openInputFile());
				popupMenu.add(item);
			}
		}

		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Öffnet die definierte Eingabedatei mit dem Standardprogramm
	 * @return	Gibt <code>true</code> zurück, wenn die Datei geöffnet werden konnte.
	 */
	public boolean openInputFile() {
		if (inputFile==null || inputFile.trim().isEmpty()) return false;
		final File input=new File(inputFile);
		if (!input.isFile()) return false;
		try {
			Desktop.getDesktop().open(input);
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
		return Language.trAll("Surface.Input.XML.Root");
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

		if (!inputFile.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Input.XML.File")));
			sub.setTextContent(inputFile);
			String modeString=null;
			switch (eofMode) {
			case EOF_MODE_SKIP:
				modeString=Language.trPrimary("Surface.Input.XML.EofMode.Skip");
				break;
			case EOF_MODE_DEFAULT_VALUE:
				modeString=Language.trPrimary("Surface.Input.XML.EofMode.DefaultValue");
				break;
			case EOF_MODE_LOOP:
				modeString=Language.trPrimary("Surface.Input.XML.EofMode.Loop");
				break;
			case EOF_MODE_TERMINATE:
				modeString=Language.trPrimary("Surface.Input.XML.EofMode.Terminate");
				break;
			}
			if (modeString!=null) sub.setAttribute(Language.trPrimary("Surface.Input.XML.EofMode"),modeString);
		}

		if (eofMode==EofModes.EOF_MODE_DEFAULT_VALUE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Input.XML.DefaultValue")));
			sub.setTextContent(defaultValue);
		}

		if (!variable.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Input.XML.Variable")));
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

		if (Language.trAll("Surface.Input.XML.File",name)) {
			inputFile=content;
			final String modeString=Language.trAllAttribute("Surface.Input.XML.EofMode",node);
			if (Language.trAll("Surface.Input.XML.EofMode.Skip",modeString)) eofMode=EofModes.EOF_MODE_SKIP;
			if (Language.trAll("Surface.Input.XML.EofMode.DefaultValue",modeString)) eofMode=EofModes.EOF_MODE_DEFAULT_VALUE;
			if (Language.trAll("Surface.Input.XML.EofMode.Loop",modeString)) eofMode=EofModes.EOF_MODE_LOOP;
			if (Language.trAll("Surface.Input.XML.EofMode.Terminate",modeString)) eofMode=EofModes.EOF_MODE_TERMINATE;
			return null;
		}

		if (Language.trAll("Surface.Input.XML.DefaultValue",name)) {
			defaultValue=content;
			return null;
		}

		if (Language.trAll("Surface.Input.XML.Variable",name)) {
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
		return "ModelElementInput";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!inputFile.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Input.File"),inputFile,2000);
		}

		String modeInfo="";
		switch (eofMode) {
		case EOF_MODE_SKIP:
			modeInfo=Language.tr("ModelDescription.Input.EOFMode.Skip");
			break;
		case EOF_MODE_DEFAULT_VALUE:
			modeInfo=Language.tr("ModelDescription.Input.EOFMode.DefaultValue")+": "+defaultValue;
			break;
		case EOF_MODE_LOOP:
			modeInfo=Language.tr("ModelDescription.Input.EOFMode.Loop");
			break;
		case EOF_MODE_TERMINATE:
			modeInfo=Language.tr("ModelDescription.Input.EOFMode.Terminate");
			break;
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Input.EOFMode"),modeInfo,3000);

		if (!variable.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Input.Variable"),variable,4000);
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
		return DataCheckResult.checkFile(this,inputFile);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.InputFile"),inputFile,newInputFile->{inputFile=newInputFile;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.AssignedVariable"),variable,newVariable->{variable=newVariable;});
		if (eofMode==EofModes.EOF_MODE_DEFAULT_VALUE) {
			searcher.testString(this,Language.tr("Surface.Input.Dialog.Mode.DefaultValue"),defaultValue,newDefaultValue->{defaultValue=newDefaultValue;});
		}
	}
}
