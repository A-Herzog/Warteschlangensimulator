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
import mathtools.NumberTools;
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
import ui.modeleditor.elements.ModelElementInput.EofModes;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Liest eine Zahl aus einer Datei und verarbeitet diese per Javascript.
 * @author Alexander Herzog
 */
public class ModelElementInputJS extends ModelElementMultiInSingleOutBox implements ElementNoRemoteSimulation, ElementWithScript {
	/**
	 * Skript
	 * @see #getScript()
	 * @see #setScript(String)
	 */
	private String script;

	/**
	 * Skriptsprache
	 * @see #getMode()
	 * @see #setMode(ScriptMode)
	 */
	private ScriptMode mode;

	/**
	 * Name der Datei, aus der die Werte gelesen werden sollen
	 * @see #getInputFile()
	 * @see #setInputFile(String)
	 */
	private String inputFile;

	/**
	 * Verhalten beim Erreichen des Dateiendes
	 * @see #getEofMode()
	 * @see #setEofMode(EofModes)
	 * @see EofModes
	 */
	private EofModes eofMode;

	/**
	 * Vorgabewert, der verwendet wird, wenn im Modus {@link ModelElementInput.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @see #getDefaultValue()
	 * @see #setDefaultValue(double)
	 */
	private double defaultValue;

	/**
	 * Gibt an, ob die Datei von unten nach oben gelesen werden soll.
	 * @see #isReadBottomUp()
	 * @see #setReadBottomUp(boolean)
	 */
	private boolean readBottomUp;

	/**
	 * Konstruktor der Klasse <code>ModelElementInputJS</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInputJS(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		script="";
		mode=ScriptMode.Javascript;
		inputFile="";
		eofMode=ModelElementInput.EofModes.EOF_MODE_SKIP;
		defaultValue=0;
		readBottomUp=false;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INPUT_JS.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InputJS.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementInputJS)) return false;
		final ModelElementInputJS otherInput=(ModelElementInputJS)element;

		if (!script.equals(otherInput.script)) return false;
		if (otherInput.mode!=mode) return false;
		if (!inputFile.equals(otherInput.inputFile)) return false;
		if (eofMode!=otherInput.eofMode) return false;
		if (defaultValue!=otherInput.defaultValue) return false;
		if (readBottomUp!=otherInput.readBottomUp) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInputJS) {
			final ModelElementInputJS source=(ModelElementInputJS)element;
			script=source.script;
			mode=source.mode;
			inputFile=source.inputFile;
			eofMode=source.eofMode;
			defaultValue=source.defaultValue;
			readBottomUp=source.readBottomUp;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInputJS clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInputJS element=new ModelElementInputJS(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InputJS.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.InputJS.Name.Short");
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
	 * Das aktuelle Skript.
	 * @return	Skript
	 */
	@Override
	public String getScript() {
		return script;
	}

	/**
	 * Setzt das aktuelle Skript
	 * @param script	Neues Skript
	 */
	@Override
	public void setScript(final String script) {
		if (script!=null) this.script=script;
	}

	/**
	 * Gibt die Skriptsprache an
	 * @return	Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	@Override
	public ScriptMode getMode() {
		return mode;
	}

	/**
	 * Stellt die Skriptsprache ein.
	 * @param mode	Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	@Override
	public void setMode(final ScriptMode mode) {
		if (mode!=null) this.mode=mode;
	}

	/**
	 * Liefert den Namen der Datei, aus der die Werte gelesen werden sollen
	 * @return	Name der Datei, aus der die Werte gelesen werden sollen
	 * @see ModelElementInput#setInputFile(String)
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * Stellt den Namen der Datei, aus der die Werte gelesen werden sollen, ein.
	 * @param inputFile	Name der Datei, aus der die Werte gelesen werden sollen
	 * @see ModelElementInput#getInputFile()
	 */
	public void setInputFile(final String inputFile) {
		if (inputFile==null) this.inputFile=""; else this.inputFile=inputFile;
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
	public double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Stellt den Vorgabewert ein, der verwendet wird, wenn im Modus {@link ModelElementInput.EofModes#EOF_MODE_DEFAULT_VALUE} das Dateiende erreicht wurde
	 * @param defaultValue	Vorgabewert
	 * @see ModelElementInput#getDefaultValue()
	 * @see EofModes#EOF_MODE_DEFAULT_VALUE
	 */
	public void setDefaultValue(double defaultValue) {
		this.defaultValue=defaultValue;
	}

	/**
	 * Gibt an, ob die Datei von unten nach oben gelesen werden soll.
	 * @return	Datei von unten nach oben lesen
	 */
	public boolean isReadBottomUp() {
		return readBottomUp;
	}

	/**
	 * Stellt ein, ob die Datei von unten nach oben gelesen werden soll.
	 * @param readBottomUp	Datei von unten nach oben lesen
	 */
	public void setReadBottomUp(final boolean readBottomUp) {
		this.readBottomUp=readBottomUp;
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
			new ModelElementInputJSDialog(owner,ModelElementInputJS.this,readOnly);
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
		return Language.trAll("Surface.InputJS.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputJS.XML.Script")));
		switch (mode) {
		case Java:
			sub.setAttribute(Language.trPrimary("Surface.InputJS.XML.Script.Language"),Language.trPrimary("Surface.InputJS.XML.Script.Java"));
			break;
		case Javascript:
			sub.setAttribute(Language.trPrimary("Surface.InputJS.XML.Script.Language"),Language.trPrimary("Surface.InputJS.XML.Script.Javascript"));
			break;
		}
		sub.setTextContent(script);

		if (!inputFile.isEmpty() || readBottomUp) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputJS.XML.File")));
			sub.setTextContent(inputFile);
			String modeString=null;
			switch (eofMode) {
			case EOF_MODE_SKIP:
				modeString=Language.trPrimary("Surface.InputJS.XML.EofMode.Skip");
				break;
			case EOF_MODE_DEFAULT_VALUE:
				modeString=Language.trPrimary("Surface.InputJS.XML.EofMode.DefaultValue");
				break;
			case EOF_MODE_LOOP:
				modeString=Language.trPrimary("Surface.InputJS.XML.EofMode.Loop");
				break;
			case EOF_MODE_TERMINATE:
				modeString=Language.trPrimary("Surface.InputJS.XML.EofMode.Terminate");
				break;
			}
			if (modeString!=null) sub.setAttribute(Language.trPrimary("Surface.InputJS.XML.EofMode"),modeString);

			if (readBottomUp) sub.setAttribute(Language.trPrimary("Surface.InputJS.XML.ReadOrder"),Language.trPrimary("Surface.InputJS.XML.ReadOrder.BottomToTop"));
		}

		if (eofMode==ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.InputJS.XML.DefaultValue")));
			sub.setTextContent(NumberTools.formatSystemNumber(defaultValue));
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

		if (Language.trAll("Surface.InputJS.XML.Script",name)) {
			script=content;
			final String langName=Language.trAllAttribute("Surface.InputJS.XML.Script.Language",node);
			if (Language.trAll("Surface.InputJS.XML.Script.Java",langName)) mode=ScriptMode.Java;
			if (Language.trAll("Surface.InputJS.XML.Script.Javascript",langName)) mode=ScriptMode.Javascript;
			return null;
		}

		if (Language.trAll("Surface.InputJS.XML.File",name)) {
			inputFile=content;
			final String modeString=Language.trAllAttribute("Surface.InputJS.XML.EofMode",node);
			if (Language.trAll("Surface.InputJS.XML.EofMode.Skip",modeString)) eofMode=ModelElementInput.EofModes.EOF_MODE_SKIP;
			if (Language.trAll("Surface.InputJS.XML.EofMode.DefaultValue",modeString)) eofMode=ModelElementInput.EofModes.EOF_MODE_DEFAULT_VALUE;
			if (Language.trAll("Surface.InputJS.XML.EofMode.Loop",modeString)) eofMode=ModelElementInput.EofModes.EOF_MODE_LOOP;
			if (Language.trAll("Surface.InputJS.XML.EofMode.Terminate",modeString)) eofMode=ModelElementInput.EofModes.EOF_MODE_TERMINATE;
			return null;
		}

		if (Language.trAll("Surface.InputJS.XML.DefaultValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			defaultValue=D.doubleValue();
			return null;
		}

		final String readOrder=Language.trAllAttribute("Surface.InputJS.XML.ReadOrder",node);
		if (Language.trAll("Surface.InputJS.XML.ReadOrder.BottomToTop",readOrder)) readBottomUp=true;

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
		return "ModelElementInputJS";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!inputFile.isBlank()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.InputJS.File"),inputFile,2000);
		}

		String modeInfo="";
		switch (eofMode) {
		case EOF_MODE_SKIP:
			modeInfo=Language.tr("ModelDescription.InputJS.EOFMode.Skip");
			break;
		case EOF_MODE_DEFAULT_VALUE:
			modeInfo=Language.tr("ModelDescription.InputJS.EOFMode.DefaultValue")+": "+NumberTools.formatNumber(defaultValue);
			break;
		case EOF_MODE_LOOP:
			modeInfo=Language.tr("ModelDescription.InputJS.EOFMode.Loop");
			break;
		case EOF_MODE_TERMINATE:
			modeInfo=Language.tr("ModelDescription.InputJS.EOFMode.Terminate");
			break;
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.InputJS.EOFMode"),modeInfo,3000);

		if (readBottomUp) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.InputJS.ReadDirection"),Language.tr("ModelDescription.InputJS.ReadDirection.BottomToTop"),4000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Script"),script,newScript->{script=newScript;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.InputFile"),inputFile,newInputFile->{inputFile=newInputFile;});
		if (eofMode==EofModes.EOF_MODE_DEFAULT_VALUE) {
			searcher.testDouble(this,Language.tr("Surface.InputJS.Dialog.Mode.DefaultValue"),defaultValue,newDefaultValue->{defaultValue=newDefaultValue;});
		}
	}

	@Override
	public boolean isOutputActive() {
		/* Keine Remote-Simulation */
		return true;
	}
}
