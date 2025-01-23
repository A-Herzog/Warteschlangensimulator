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
import java.io.File;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
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
 * F�hrt, wenn ein Kunde diese Station passiert, ein Skript aus und h�ngt das Ergebnis
 * des Skriptes an eine Ausgabedatei an.
 * @author Alexander Herzog
 *
 */
public class ModelElementOutputJS extends ModelElementMultiInSingleOutBox implements ElementNoRemoteSimulation, ElementWithScript, ElementWithOutputFile {
	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @see #isOutputActive()
	 * @see #setOutputActive(boolean)
	 */
	private boolean outputActive;

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
	 * Soll das Skript zur Ausgabe von �berschriften verwendet werden?
	 */
	private boolean useHeadingScript;

	/**
	 * Skript
	 * @see #getScriptHeading()
	 * @see #setScriptHeading(String)
	 */
	private String scriptHeading;

	/**
	 * Skriptsprache
	 * @see #getModeHeading()
	 * @see #setModeHeading(ScriptMode)
	 */
	private ScriptMode modeHeading;

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
	 * Konstruktor der Klasse <code>ModelElementOutputJS</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementOutputJS(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		outputActive=true;
		script="";
		mode=ScriptMode.Javascript;
		scriptHeading="";
		modeHeading=ScriptMode.Javascript;
		outputFile="";
		outputFileOverwrite=false;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_OUTPUT_JS.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.OutputJS.Tooltip");
	}

	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @return	Ausgabe aktiv
	 * @see #setOutputActive(boolean)
	 */
	@Override
	public boolean isOutputActive() {
		return outputActive;
	}

	/**
	 * Stellt ein, ob die Ausgabe aktiv sein soll.
	 * @param outputActive	Ausgabe aktiv
	 * @see #isOutputActive()
	 */
	@Override
	public void setOutputActive(boolean outputActive) {
		this.outputActive=outputActive;
	}

	/**
	 * Liefert das aktuelle Skript.
	 * @return	Skript
	 */
	@Override
	public String getScript() {
		return script;
	}

	/**
	 * Setzt das aktuelle Skript.
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
	 * Soll das Skript zur Ausgabe von �berschriften verwendet werden?
	 * @return	Skript zur Ausgabe von �berschriften verwendet
	 */
	public boolean isUseHeadingScript() {
		return useHeadingScript;
	}

	/**
	 * Soll das Skript zur Ausgabe von �berschriften verwendet werden?
	 * @param useHeadingScript	Skript zur Ausgabe von �berschriften verwendet
	 */
	public void setUseHeadingScript(boolean useHeadingScript) {
		this.useHeadingScript=useHeadingScript;
	}

	/**
	 * Liefert das aktuelle �berschriften-Skript.
	 * @return	�berschriften-Skript
	 */
	public String getScriptHeading() {
		return scriptHeading;
	}

	/**
	 * Setzt das aktuelle �berschriften-Skript.
	 * @param script	Neues �berschriften-Skript
	 */
	public void setScriptHeading(final String script) {
		if (script!=null) this.scriptHeading=script;
	}

	/**
	 * Gibt die �berschriften-Skriptsprache an
	 * @return	�berschriften-Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	public ScriptMode getModeHeading() {
		return modeHeading;
	}

	/**
	 * Stellt die �berschriften-Skriptsprache ein.
	 * @param mode	�berschriften-Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	public void setModeHeading(final ScriptMode mode) {
		if (mode!=null) this.modeHeading=mode;
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
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementOutputJS)) return false;
		final ModelElementOutputJS other=(ModelElementOutputJS)element;

		if (outputActive!=other.outputActive) return false;
		if (!script.equals(other.script)) return false;
		if (other.mode!=mode) return false;
		if (useHeadingScript!=other.useHeadingScript) return false;
		if (!scriptHeading.equals(other.scriptHeading)) return false;
		if (other.modeHeading!=modeHeading) return false;
		if (!outputFile.equals(other.outputFile)) return false;
		if (outputFileOverwrite!=other.outputFileOverwrite) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementOutputJS) {
			final ModelElementOutputJS source=(ModelElementOutputJS)element;
			outputActive=source.outputActive;
			script=source.script;
			mode=source.mode;
			useHeadingScript=source.useHeadingScript;
			scriptHeading=source.scriptHeading;
			modeHeading=source.modeHeading;
			outputFile=source.outputFile;
			outputFileOverwrite=source.outputFileOverwrite;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementOutputJS clone(final EditModel model, final ModelSurface surface) {
		final ModelElementOutputJS element=new ModelElementOutputJS(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.OutputJS.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.OutputJS.Name.Short");
	}

	/**
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null || outputActive) return null;
		return Language.tr("Surface.OutputJS.Disabled");
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
			new ModelElementOutputJSDialog(owner,ModelElementOutputJS.this,readOnly);
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
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.OutputJS.XML.Root");
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
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputJS.XML.Active")));
			sub.setTextContent("0");
		}

		if (!script.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputJS.XML.Script")));
			switch (mode) {
			case Java:
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.Script.Language"),Language.trPrimary("Surface.OutputJS.XML.Script.Java"));
				break;
			case Javascript:
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.Script.Language"),Language.trPrimary("Surface.OutputJS.XML.Script.Javascript"));
				break;
			}
			sub.setTextContent(script);
		}

		if (!scriptHeading.isEmpty() || useHeadingScript) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputJS.XML.ScriptHeading")));
			if (useHeadingScript) {
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.ScriptHeading.Active"),"1");
			} else {
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.ScriptHeading.Active"),"0");
			}
			switch (modeHeading) {
			case Java:
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.Script.Language"),Language.trPrimary("Surface.OutputJS.XML.Script.Java"));
				break;
			case Javascript:
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.Script.Language"),Language.trPrimary("Surface.OutputJS.XML.Script.Javascript"));
				break;
			}
			sub.setTextContent(scriptHeading);
		}

		if (!outputFile.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputJS.XML.File")));
			sub.setTextContent(outputFile);
			if (outputFileOverwrite) sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.File.Overwrite"),"1");
		}
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

		if (Language.trAll("Surface.OutputJS.XML.Active",name)) {
			outputActive=!content.equals("0");
			return null;
		}

		if (Language.trAll("Surface.OutputJS.XML.Script",name)) {
			script=content;
			final String langName=Language.trAllAttribute("Surface.OutputJS.XML.Script.Language",node);
			if (Language.trAll("Surface.OutputJS.XML.Script.Java",langName)) mode=ScriptMode.Java;
			if (Language.trAll("Surface.OutputJS.XML.Script.Javascript",langName)) mode=ScriptMode.Javascript;
			return null;
		}

		if (Language.trAll("Surface.OutputJS.XML.ScriptHeading",name)) {
			scriptHeading=content;
			final String activeString=Language.trAllAttribute("Surface.OutputJS.XML.ScriptHeading.Active",node);
			if (!activeString.isEmpty() && !activeString.equals("0")) useHeadingScript=true;
			final String langName=Language.trAllAttribute("Surface.OutputJS.XML.Script.Language",node);
			if (Language.trAll("Surface.OutputJS.XML.Script.Java",langName)) modeHeading=ScriptMode.Java;
			if (Language.trAll("Surface.OutputJS.XML.Script.Javascript",langName)) modeHeading=ScriptMode.Javascript;
			return null;
		}

		if (Language.trAll("Surface.OutputJS.XML.File",name)) {
			outputFile=content;
			final String overwrite=Language.trAllAttribute("Surface.OutputJS.XML.File.Overwrite",node);
			if (!overwrite.isEmpty() && !overwrite.equals("0")) outputFileOverwrite=true;
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementOutputJS";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!outputFile.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.OutputJS.File"),outputFile,2000);
		}

		if (!outputActive) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.OutputJS.Active"),Language.tr("ModelDescription.OutputJS.Active.Off"),10000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Script"),script,newScript->{script=newScript;});
		if (useHeadingScript) {
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.ScriptHeading"),scriptHeading,newScriptHeading->{scriptHeading=newScriptHeading;});
		}
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.OutputFile"),outputFile,newOutputFile->{outputFile=newOutputFile;});
	}
}
