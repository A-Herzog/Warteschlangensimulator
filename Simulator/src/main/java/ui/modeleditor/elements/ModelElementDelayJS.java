/**
 * Copyright 2022 Alexander Herzog
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
import java.util.Objects;
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
 * Verz�gerung der Kunden auf Basis eines Skripts
 * @author Alexander Herzog
 */
public class ModelElementDelayJS extends ModelElementMultiInSingleOutBox implements ModelElementAnimationForceMove, ElementWithScript {
	/**
	 * Skript zur Fallunterscheidung
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
	 * Art wie die Verz�gerung f�r die Kundenstatistik gez�hlt werden soll
	 * @see #getDelayType()
	 * @see #setDelayType(DelayType)
	 */
	private DelayType delayType;

	/**
	 * Kosten pro Bedienvorgang
	 * @see #getCosts()
	 * @see #setCosts(String)
	 */
	private String costs;

	/**
	 * Soll eine Liste der Kunden an der Station gef�hrt werden?
	 * @see #hasClientsList()
	 * @see #setHasClientsList(boolean)
	 */
	private boolean hasClientsList;

	/**
	 * Konstruktor der Klasse <code>ModelElementDelay</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementDelayJS(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE_DOUBLE_LINE);
		script="";
		mode=ScriptMode.Javascript;
		delayType=DelayType.DELAY_TYPE_PROCESS;
		costs="";
		hasClientsList=false;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DELAY_JS.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.DelayJS.Tooltip");
	}

	/**
	 * Liefert das Skript zur Fallunterscheidung zur�ck
	 * @return	Skript
	 */
	@Override
	public String getScript() {
		return script;
	}

	/**
	 * Stellt ein neues Skript zur Fallunterscheidung ein
	 * @param script	Skript
	 */
	@Override
	public void setScript(final String script) {
		if (script==null) this.script=""; else this.script=script.trim();
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
	 * Gibt an, ob die Prozesszeiten als Bedienzeiten, Transferzeiten oder als Wartezeiten gez�hlt werden sollen.
	 * @return	Gibt den Typ der Verz�gerung zur�ck.
	 */
	public DelayType getDelayType() {
		return delayType;
	}

	/**
	 * Stellt ein, ob die Prozesszeiten als Bedienzeiten, Transferzeiten oder als Wartezeiten gez�hlt werden sollen.
	 * @param delayType	Art der Verz�gerung
	 */
	public void setDelayType(final DelayType delayType) {
		this.delayType=delayType;
	}

	/**
	 * Liefert die eingestellten Kosten pro Bedienvorgang in der Station
	 * @return	Kosten pro Bedienvorgang
	 */
	public String getCosts() {
		return (costs==null)?"":costs;
	}

	/**
	 * Stellt die Kosten pro Bedienvorgang in der Station ein
	 * @param costs	Kosten pro Bedienvorgang
	 */
	public void setCosts(final String costs) {
		this.costs=(costs==null)?"":costs;
	}

	/**
	 * Soll eine Liste der Kunden an der Station gef�hrt werden?
	 * @return	Soll eine Liste der Kunden an der Station gef�hrt werden?
	 */
	public boolean hasClientsList() {
		return hasClientsList;
	}

	/**
	 * Stellt ein, ob an der Station eine Liste der Kunden gef�hrt werden soll.
	 * @param hasClientsList	Soll eine Liste der Kunden an der Station gef�hrt werden?
	 */
	public void setHasClientsList(final boolean hasClientsList) {
		this.hasClientsList=hasClientsList;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDelayJS)) return false;
		final ModelElementDelayJS otherDelay=(ModelElementDelayJS)element;

		if (!otherDelay.script.equals(script)) return false;
		if (otherDelay.mode!=mode) return false;

		if (otherDelay.delayType!=delayType) return false;

		if (!Objects.equals(costs,otherDelay.costs)) return false;

		if (hasClientsList!=otherDelay.hasClientsList) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDelayJS) {
			final ModelElementDelayJS copySource=(ModelElementDelayJS)element;

			script=copySource.script;
			mode=copySource.mode;

			delayType=copySource.delayType;

			costs=copySource.costs;

			hasClientsList=copySource.hasClientsList;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDelayJS clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDelayJS element=new ModelElementDelayJS(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.DelayJS.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.DelayJS.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,225,255);

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
			new ModelElementDelayJSDialog(owner,ModelElementDelayJS.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
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
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsProcessing(this,parentMenu,addNextStation);
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
		return Language.trAll("Surface.DelayJS.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DelayJS.XML.Script")));
		switch (mode) {
		case Java:
			sub.setAttribute(Language.trPrimary("Surface.DelayJS.XML.Script.Language"),Language.trPrimary("Surface.DelayJS.XML.Script.Java"));
			break;
		case Javascript:
			sub.setAttribute(Language.trPrimary("Surface.DelayJS.XML.Script.Language"),Language.trPrimary("Surface.DelayJS.XML.Script.Javascript"));
			break;
		}
		sub.setTextContent(script);

		switch (delayType) {
		case DELAY_TYPE_WAITING:
			sub.setAttribute(Language.trPrimary("Surface.DelayJS.XML.Type"),Language.trPrimary("Surface.DelayJS.XML.Type.WaitingTime"));
			break;
		case DELAY_TYPE_TRANSFER:
			sub.setAttribute(Language.trPrimary("Surface.DelayJS.XML.Type"),Language.trPrimary("Surface.DelayJS.XML.Type.TransferTime"));
			break;
		case DELAY_TYPE_PROCESS:
			sub.setAttribute(Language.trPrimary("Surface.DelayJS.XML.Type"),Language.trPrimary("Surface.DelayJS.XML.Type.ProcessTime"));
			break;
		case DELAY_TYPE_NOTHING:
			sub.setAttribute(Language.trPrimary("Surface.DelayJS.XML.Type"),Language.trPrimary("Surface.DelayJS.XML.Type.Nothing"));
			break;
		}

		if (costs!=null && !costs.isBlank() && !costs.trim().equals("0")) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DelayJS.XML.Costs")));
			sub.setTextContent(costs);
		}

		if (hasClientsList) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DelayJS.XML.ClientsList")));
			sub.setTextContent("1");
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

		if (Language.trAll("Surface.DelayJS.XML.Script",name)) {
			script=content;
			final String langName=Language.trAllAttribute("Surface.DelayJS.XML.Script.Language",node);
			if (Language.trAll("Surface.DelayJS.XML.Script.Java",langName)) mode=ScriptMode.Java;
			if (Language.trAll("Surface.DelayJS.XML.Script.Javascript",langName)) mode=ScriptMode.Javascript;

			final String type=Language.trAllAttribute("Surface.DelayJS.XML.Type",node);
			if (Language.trAll("Surface.DelayJS.XML.Type.WaitingTime",type)) delayType=DelayType.DELAY_TYPE_WAITING;
			if (Language.trAll("Surface.DelayJS.XML.Type.TransferTime",type)) delayType=DelayType.DELAY_TYPE_TRANSFER;
			if (Language.trAll("Surface.DelayJS.XML.Type.ProcessTime",type)) delayType=DelayType.DELAY_TYPE_PROCESS;
			if (Language.trAll("Surface.DelayJS.XML.Type.Nothing",type)) delayType=DelayType.DELAY_TYPE_NOTHING;

			return null;
		}

		if (Language.trAll("Surface.DelayJS.XML.Costs",name)) {
			costs=content;
			return null;
		}

		if (Language.trAll("Surface.DelayJS.XML.ClientsList",name)) {
			hasClientsList=content.trim().equals("1");
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zus�tzlich auch die aktuelle Anzahl an Kunden an der Station
	 * w�hrend der Animation angezeigt werden sollen.
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
		return "ModelElementDelayJS";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Verz�gerung erfassen als ... */
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.DelayJS.Mode"),Language.tr("ModelDescription.DelayJS.Mode.Waiting"),1000);
			break;
		case DELAY_TYPE_TRANSFER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.DelayJS.Mode"),Language.tr("ModelDescription.DelayJS.Mode.Transfer"),1000);
			break;
		case DELAY_TYPE_PROCESS:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.DelayJS.Mode"),Language.tr("ModelDescription.DelayJS.Mode.Process"),1000);
			break;
		case DELAY_TYPE_NOTHING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.DelayJS.Mode"),Language.tr("ModelDescription.DelayJS.Mode.Nothing"),1000);
			break;
		}

		/* Kosten an der Station */
		if (costs!=null && !costs.isBlank() && !costs.trim().equals("0")) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.DelayJS.StationCostsPerClient"),costs,1000);
		}
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.process,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Skript */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Script"),script,newScript->{script=newScript;});

		/* Kosten */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Costs"),costs,newCosts->{costs=newCosts;});
	}
}
