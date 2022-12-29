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
import java.util.Map;
import java.util.TreeMap;
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
import ui.modeleditor.fastpaint.Shapes;

/**
 * H�lt die Kunden auf, bis eine bestimmte Bedingung erf�llt ist
 * @author Alexander Herzog
 */
public class ModelElementHold extends ModelElementMultiInSingleOutBox implements ModelElementAnimationForceMove {
	/**
	 * Standardm��ige Priorit�t f�r Kundentypen
	 */
	public static final String DEFAULT_CLIENT_PRIORITY="w";

	/**
	 * Bedingung, die f�r eine Weitergabe der Kunden erf�llt sein muss
	 * @see #getCondition()
	 * @see #setCondition(String)
	 */
	private String condition;

	/**
	 * Priorit�t f�r Kunden eines bestimmten Kundentyp
	 * @see #getPriority(String)
	 * @see #setPriority(String, String)
	 */
	private final Map<String,String> priority; /*  Kundentyp, Priorit�t-Formel */

	/**
	 * Individuelle kundenbasierende Pr�fung
	 * @see #isClientBasedCheck()
	 * @see #setClientBasedCheck(boolean)
	 */
	private boolean clientBasedCheck;

	/**
	 * Regelm��ige Pr�fung der Bedingung
	 * @see #isUseTimedChecks()
	 * @see #setUseTimedChecks(boolean)
	 */
	private boolean useTimedChecks;

	/**
	 * Konstruktor der Klasse <code>ModelElementHold</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementHold(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		condition="";
		priority=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		clientBasedCheck=false;
		useTimedChecks=false;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_HOLD.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Hold.Tooltip");
	}

	/**
	 * Liefert die Bedingung, die erf�llt sein muss, damit Kunden weitergeleitet werden.
	 * @return Bedingung, die f�r eine Weitergabe der Kunden erf�llt sein muss
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung ein, die erf�llt sein muss, damit Kunden weitergeleitet werden.
	 * @param newCondition	Neue zu erf�llende Bedingung
	 */
	public void setCondition(final String newCondition) {
		if (newCondition!=null) condition=newCondition;
	}

	/**
	 * Liefert die Priorit�t f�r Kunden eines bestimmten Kundentyp.
	 * @param clientType	Kundentyp f�r den die Priorit�t geliefert werden soll
	 * @return Priorit�t f�r Kunden dieses Kundentyps
	 */
	public String getPriority(final String clientType) {
		final String p=priority.get(clientType);
		if (p==null) return DEFAULT_CLIENT_PRIORITY; else return p;
	}

	/**
	 * Stellt Priorit�t f�r die Kunden eines bestimmten Kundentyps ein.
	 * @param clientType	Kundentyp f�r den die Priorit�t eingestellt werden soll
	 * @param priority	Neue Priorit�t f�r Kundes des Kundentyps
	 */
	public void setPriority(final String clientType, final String priority) {
		if (clientType==null || clientType.trim().isEmpty()) return;
		if (priority==null) this.priority.put(clientType,""); else this.priority.put(clientType,priority);
	}

	/**
	 * Soll die Pr�fung individuell pro wartendem Kunden und unter Verwendung der Kundendaten erfolgen?
	 * @return	Individuelle kundenbasierende Pr�fung
	 */
	public boolean isClientBasedCheck() {
		return clientBasedCheck;
	}

	/**
	 * Stellt ein, ob die Pr�fung individuell pro wartendem Kunden und unter Verwendung der Kundendaten erfolgen soll.
	 * @param clientBasedCheck	Individuelle kundenbasierende Pr�fung
	 */
	public void setClientBasedCheck(final boolean clientBasedCheck) {
		this.clientBasedCheck=clientBasedCheck;
	}

	/**
	 * Regelm��ige Pr�fung der Bedingung
	 * @return	Regelm��ige Pr�fung der Bedingung
	 */
	public boolean isUseTimedChecks() {
		return useTimedChecks;
	}

	/**
	 * Regelm��ige Pr�fung der Bedingung einstellen
	 * @param useTimedChecks	Regelm��ige Pr�fung der Bedingung
	 */
	public void setUseTimedChecks(boolean useTimedChecks) {
		this.useTimedChecks=useTimedChecks;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementHold)) return false;
		final ModelElementHold hold=(ModelElementHold)element;

		/* Bedingung */
		if (!hold.condition.equalsIgnoreCase(condition)) return false;

		/* Priorit�ten */
		Map<String,String> priorityA=priority;
		Map<String,String> priorityB=hold.priority;
		for (Map.Entry<String,String> entry : priorityA.entrySet()) {
			if (!entry.getValue().equals(priorityB.get(entry.getKey()))) return false;
		}
		for (Map.Entry<String,String> entry : priorityB.entrySet()) {
			if (!entry.getValue().equals(priorityA.get(entry.getKey()))) return false;
		}

		/* Individuelle kundenbasierende Pr�fung */
		if (hold.clientBasedCheck!=clientBasedCheck) return false;

		/* Regelm��ige Pr�fung der Bedingung */
		if (hold.useTimedChecks!=useTimedChecks) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementHold) {
			final ModelElementHold hold=(ModelElementHold)element;

			/* Bedingung */
			condition=hold.condition;

			/* Priorit�ten */
			priority.clear();
			for (Map.Entry<String,String> entry: hold.priority.entrySet()) priority.put(entry.getKey(),entry.getValue());


			/* Individuelle kundenbasierende Pr�fung */
			clientBasedCheck=((ModelElementHold)element).clientBasedCheck;

			/* Regelm��ige Pr�fung der Bedingung */
			useTimedChecks=((ModelElementHold)element).useTimedChecks;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementHold clone(final EditModel model, final ModelSurface surface) {
		final ModelElementHold element=new ModelElementHold(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Hold.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Hold.Name");
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
			new ModelElementHoldDialog(owner,ModelElementHold.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.CHART_WIP);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.HISTOGRAM_WIP);
		if (!condition.trim().isEmpty()) {
			addVisualizationTrafficLightsMenuItem("!("+condition+")",parentMenu,addElement);
		}
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
		NextStationHelper.nextStationsHold(this,parentMenu,addNextStation);
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
		return Language.trAll("Surface.Hold.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Hold.XML.Condition")));
		sub.setTextContent(condition);
		if (clientBasedCheck) sub.setAttribute(Language.trPrimary("Surface.Hold.XML.Condition.ClientBased"),"1");
		if (useTimedChecks) sub.setAttribute(Language.trPrimary("Surface.Hold.XML.Condition.TimedChecks"),"1");

		for (Map.Entry<String,String> entry : priority.entrySet()) if (entry.getValue()!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.Priority")));
			sub.setAttribute(Language.trPrimary("Surface.Hold.XML.Priority.ClientType"),entry.getKey());
			sub.setTextContent(entry.getValue());
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

		if (Language.trAll("Surface.Hold.XML.Condition",name)) {
			condition=node.getTextContent();
			final String clientBasedCheckString=Language.trAllAttribute("Surface.Hold.XML.Condition.ClientBased",node);
			if (clientBasedCheckString.equals("1")) clientBasedCheck=true;
			final String useTimedChecksString=Language.trAllAttribute("Surface.Hold.XML.Condition.TimedChecks",node);
			if (useTimedChecksString.equals("1")) useTimedChecks=true;
			return null;
		}

		if (Language.trAll("Surface.Hold.XML.Priority",name)) {
			final String typ=Language.trAllAttribute("Surface.Hold.XML.Priority.ClientType",node);
			if (!typ.trim().isEmpty()) priority.put(typ,content);
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
	protected String getIDInfo() {
		return super.getIDInfo()+", "+Language.tr("Surface.Hold.Dialog.Condition")+": "+condition;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementHold";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Bedingung */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Hold.Condition"),condition,1000);

		/* Priorit�ten */
		final String[] clientTypes=descriptionBuilder.getClientTypes();
		boolean needPrioInfo=false;
		for (String clientType: clientTypes) {
			final String prio=priority.get(clientType);
			if (prio!=null && !prio.trim().isEmpty() && !prio.equals(DEFAULT_CLIENT_PRIORITY)) {needPrioInfo=true; break;}
		}
		if (needPrioInfo) for (String clientType: clientTypes) {
			final String prio=priority.get(clientType);
			if (prio!=null && !prio.trim().isEmpty()) {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Hold.ClientTypePriority"),clientType),prio,8000);
			} else {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Hold.ClientTypePriority"),clientType),DEFAULT_CLIENT_PRIORITY,8000);
			}
		}
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.hold,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Bedingung */
		searcher.testString(this,Language.tr("Surface.Hold.Dialog.Condition"),condition,newCondition->{condition=newCondition;});

		/* Priorit�ten */
		for (Map.Entry<String,String> clientPriority: priority.entrySet()) {
			final String clientType=clientPriority.getKey();
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.PriorityForClientType"),clientType),clientPriority.getValue(),newPriority->priority.put(clientType,newPriority));
		}
	}
}