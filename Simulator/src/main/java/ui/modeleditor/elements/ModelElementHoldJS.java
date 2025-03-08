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
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.elements.ModelElementDelay.DelayType;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Hält die Kunden auf, bis eine bestimmte Javascript-basierende Bedingung erfüllt ist
 * @author Alexander Herzog
 */
public class ModelElementHoldJS extends ModelElementMultiInSingleOutBox implements ModelElementAnimationForceMove, ElementWithScript {
	/**
	 * Optionale zusätzliche Bedingung
	 * @see #getCondition()
	 * @see #setCondition(String)
	 */
	private String condition;

	/**
	 * Skript auf dessen Basis die Kunden weitergeleitet werden sollen
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
	 * Regelmäßige Prüfung der Bedingung
	 * @see #isUseTimedChecks()
	 * @see #setUseTimedChecks(boolean)
	 */
	private boolean useTimedChecks;

	/**
	 * Nur bei Kundenankunft prüfen?
	 * @see #isOnlyCheckOnArrival()
	 * @see #setOnlyCheckOnArrival(boolean)
	 */
	private boolean onlyCheckOnArrival;

	/**
	 * Art wie die Verzögerung für die Kundenstatistik gezählt werden soll
	 * @see #getDelayType()
	 * @see #setDelayType(DelayType)
	 */
	private ModelElementDelay.DelayType delayType;

	/**
	 * Konstruktor der Klasse <code>ModelElementHoldJS</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementHoldJS(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		condition="";
		script="";
		mode=ScriptMode.Javascript;
		useTimedChecks=false;
		onlyCheckOnArrival=false;
		delayType=DelayType.DELAY_TYPE_WAITING;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_HOLD_JS.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.HoldJS.Tooltip");
	}

	/**
	 * Liefert die optionale zusätzliche Bedingung, um überhaupt eine Skriptausführung zu starten.
	 * @return	Optionale zusätzliche Bedingung
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die optionale zusätzliche Bedingung, um überhaupt eine Skriptausführung zu starten, ein.
	 * @param condition	Optionale zusätzliche Bedingung
	 */
	public void setCondition(String condition) {
		this.condition=(condition==null)?"":condition;
	}

	/**
	 * Liefert das Skript, auf dessen Basis Kunden für die Weiterleitung freigegeben werden sollen.
	 * @return Skript auf dessen Basis die Kunden weitergeleitet werden sollen
	 */
	@Override
	public String getScript() {
		return script;
	}

	/**
	 * Stellt das Skript ein, auf dessen Basis Kunden für die Weiterleitung freigegeben werden sollen.
	 * @param newScript	Neues Skript auf dessen Basis die Kunden weitergeleitet werden sollen
	 */
	@Override
	public void setScript(final String newScript) {
		if (newScript!=null) script=newScript;
	}

	/**
	 * Regelmäßige Prüfung der Bedingung
	 * @return	Regelmäßige Prüfung der Bedingung
	 */
	public boolean isUseTimedChecks() {
		return useTimedChecks;
	}

	/**
	 * Regelmäßige Prüfung der Bedingung einstellen
	 * @param useTimedChecks	Regelmäßige Prüfung der Bedingung
	 */
	public void setUseTimedChecks(final boolean useTimedChecks) {
		this.useTimedChecks=useTimedChecks;
	}

	/**
	 * Nur bei Kundenankunft prüfen?
	 * @return	Nur bei Kundenankunft prüfen?
	 */
	public boolean isOnlyCheckOnArrival() {
		return onlyCheckOnArrival;
	}

	/**
	 * Stellt ein, ob die Bedingung bei jeder Zustandsändung oder nur bei Kundenankunft geprüft werden soll.
	 * @param onlyCheckOnArrival	Bedingung nur bei Kundenankunft prüfen?
	 */
	public void setOnlyCheckOnArrival(final boolean onlyCheckOnArrival) {
		this.onlyCheckOnArrival=onlyCheckOnArrival;
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
	 * Gibt an, ob die Wartezeiten als Bedienzeiten, Transferzeiten oder als Wartezeiten gezählt werden sollen.
	 * @return	Gibt den Typ der Verzögerung zurück.
	 */
	public ModelElementDelay.DelayType getDelayType() {
		return delayType;
	}

	/**
	 * Stellt ein, ob die Wartezeiten als Bedienzeiten, Transferzeiten oder als Wartezeiten gezählt werden sollen.
	 * @param delayType	Art der Verzögerung
	 */
	public void setDelayType(final ModelElementDelay.DelayType delayType) {
		this.delayType=delayType;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementHoldJS)) return false;
		final ModelElementHoldJS otherHold=(ModelElementHoldJS)element;

		if (!otherHold.condition.equalsIgnoreCase(condition)) return false;
		if (!otherHold.script.equalsIgnoreCase(script)) return false;
		if (otherHold.mode!=mode) return false;
		if (otherHold.useTimedChecks!=useTimedChecks) return false;
		if (otherHold.onlyCheckOnArrival!=onlyCheckOnArrival) return false;

		/* Erfassung der Aufenthaltszeit */
		if (otherHold.delayType!=delayType) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementHoldJS) {
			final ModelElementHoldJS copySource=(ModelElementHoldJS)element;
			condition=copySource.condition;
			script=copySource.script;
			mode=copySource.mode;
			useTimedChecks=copySource.useTimedChecks;
			onlyCheckOnArrival=copySource.onlyCheckOnArrival;
			delayType=copySource.delayType;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementHoldJS clone(final EditModel model, final ModelSurface surface) {
		final ModelElementHoldJS element=new ModelElementHoldJS(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.HoldJS.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.HoldJS.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,225,255);

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
			new ModelElementHoldJSDialog(owner,ModelElementHoldJS.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Popupmenüs, welches die Einträge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfläche hinzugefügt werden sollen
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.CHART_WIP);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.HISTOGRAM_WIP);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Laufzeitstatistik hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugefügt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsHold(this,parentMenu,addNextStation);
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
		return Language.trAll("Surface.HoldJS.XML.Root");
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

		if (condition!=null && !condition.isBlank()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.HoldJS.XML.AdditionalCondition")));
			sub.setTextContent(condition);
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.HoldJS.XML.Condition")));
		switch (mode) {
		case Java:
			sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.Language"),Language.trPrimary("Surface.HoldJS.XML.Condition.Java"));
			break;
		case Javascript:
			sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.Language"),Language.trPrimary("Surface.HoldJS.XML.Condition.Javascript"));
			break;
		}

		if (useTimedChecks) sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.TimedChecks"),"1");
		if (onlyCheckOnArrival) sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.OnlyCheckOnArrival"),"1");

		switch (delayType) {
		case DELAY_TYPE_WAITING:
			sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType"),Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType.WaitingTime"));
			break;
		case DELAY_TYPE_TRANSFER:
			sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType"),Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType.TransferTime"));
			break;
		case DELAY_TYPE_PROCESS:
			sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType"),Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType.ProcessTime"));
			break;
		case DELAY_TYPE_NOTHING:
			sub.setAttribute(Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType"),Language.trPrimary("Surface.HoldJS.XML.Condition.TimeType.Nothing"));
			break;
		}

		sub.setTextContent(script);
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

		if (Language.trAll("Surface.HoldJS.XML.AdditionalCondition",name)) {
			condition=content;
			return null;
		}

		if (Language.trAll("Surface.HoldJS.XML.Condition",name)) {
			script=content;

			final String langName=Language.trAllAttribute("Surface.HoldJS.XML.Condition.Language",node);
			if (Language.trAll("Surface.HoldJS.XML.Condition.Java",langName)) mode=ScriptMode.Java;
			if (Language.trAll("Surface.HoldJS.XML.Condition.Javascript",langName)) mode=ScriptMode.Javascript;

			final String useTimedChecksString=Language.trAllAttribute("Surface.HoldJS.XML.Condition.TimedChecks",node);
			if (useTimedChecksString.equals("1")) useTimedChecks=true;

			final String onlyCheckOnArrivalString=Language.trAllAttribute("Surface.HoldJS.XML.Condition.OnlyCheckOnArrival",node);
			if (onlyCheckOnArrivalString.equals("1")) onlyCheckOnArrival=true;

			final String type=Language.trAllAttribute("Surface.HoldJS.XML.Condition.TimeType",node);
			if (Language.trAll("Surface.HoldJS.XML.Condition.TimeType.WaitingTime",type)) delayType=DelayType.DELAY_TYPE_WAITING;
			if (Language.trAll("Surface.HoldJS.XML.Condition.TimeType.TransferTime",type)) delayType=DelayType.DELAY_TYPE_TRANSFER;
			if (Language.trAll("Surface.HoldJS.XML.Condition.TimeType.ProcessTime",type)) delayType=DelayType.DELAY_TYPE_PROCESS;
			if (Language.trAll("Surface.HoldJS.XML.Condition.TimeType.Nothing",type)) delayType=DelayType.DELAY_TYPE_NOTHING;

			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zusätzlich auch die aktuelle Anzahl an Kunden an der Station
	 * während der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	@Override
	public boolean showFullAnimationRunData() {
		return true;
	}

	@Override
	public boolean hasQueue() {
		return true;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementHoldJS";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Skriptsprache */
		switch (mode) {
		case Javascript:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.HoldJS.Mode"),Language.tr("ModelDescription.Expression.Javascript"),1000);
			break;
		case Java:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.HoldJS.Mode"),Language.tr("ModelDescription.Expression.Java"),1000);
			break;
		}

		/* Zeitabhängige Prüfungen */
		if (useTimedChecks) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.HoldJS.useTimedChecks"),Language.tr("ModelDescription.HoldJS.useTimedChecks.Yes"),2000);
		}

		/* Nur bei Kundenankunft prüfen? */
		if (onlyCheckOnArrival) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.HoldJS.CheckOn"),onlyCheckOnArrival?Language.tr("ModelDescription.HoldJS.CheckOn.Arrival"):Language.tr("ModelDescription.HoldJS.CheckOn.SystemStateChange"),3000);
		}

		/* Verzögerung erfassen als ... */
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Waiting"),9000);
			break;
		case DELAY_TYPE_TRANSFER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Transfer"),9000);
			break;
		case DELAY_TYPE_PROCESS:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Process"),9000);
			break;
		case DELAY_TYPE_NOTHING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Nothing"),9000);
			break;
		}
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.hold,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Surface.HoldJS.Dialog.Condition"),condition,newCondition->{condition=newCondition;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Script"),script,newScript->{script=newScript;});
	}
}