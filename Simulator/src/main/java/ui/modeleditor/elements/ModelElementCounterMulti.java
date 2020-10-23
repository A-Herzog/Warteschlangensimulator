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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementCounterMulti;
import simulator.elements.RunElementCounterMultiData;
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Z�hlt ausdifferenziert nach verschiedenen Bedingungen f�r die Statistik wie viele Kunden das Element durchquert haben
 * @author Alexander Herzog
 */
public class ModelElementCounterMulti extends ModelElementMultiInSingleOutBox {
	/**
	 * Gruppenname des Z�hlers
	 * @see #getGroupName()
	 */
	private String groupName;

	/**
	 * Liste der Bedingungen
	 * @see #getConditions()
	 */
	private final List<String> conditions;

	/**
	 * Liste der Z�hlernamen
	 * @see #getCounterNames()
	 */
	private final List<String> counterNames;

	/**
	 * Konstruktor der Klasse <code>ModelElementCounterMulti</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementCounterMulti(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE_123);
		groupName=Language.tr("Surface.Counter.DefaultCounterName");
		conditions=new ArrayList<>();
		counterNames=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_COUNTER_MULTI.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.CounterMulti.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementCounterMulti)) return false;
		final ModelElementCounterMulti other=(ModelElementCounterMulti)element;

		if (!other.groupName.equals(groupName)) return false;
		if (!Objects.deepEquals(other.counterNames,counterNames)) return false;
		if (!Objects.deepEquals(other.conditions,conditions)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementCounterMulti) {
			final ModelElementCounterMulti source=(ModelElementCounterMulti)element;
			groupName=source.groupName;
			conditions.clear();
			conditions.addAll(source.conditions);
			counterNames.clear();
			counterNames.addAll(source.counterNames);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementCounterMulti clone(final EditModel model, final ModelSurface surface) {
		final ModelElementCounterMulti element=new ModelElementCounterMulti(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.CounterMulti.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.CounterMulti.Name.Short");
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
			new ModelElementCounterMultiDialog(owner,ModelElementCounterMulti.this,readOnly);
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
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt stations-bedingte zus�tzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		final RunElementCounterMultiData data=(RunElementCounterMultiData)builder.data;
		final RunElementCounterMulti station=(RunElementCounterMulti)data.station;
		boolean first=true;
		for (int i=0;i<data.statistic.length;i++) {
			final long value=data.statistic[i].get();
			if (value>0) {
				if (first) {
					builder.results.append("\n"+Language.tr("Statistics.Counter")+"\n");
					first=false;
				}
				builder.results.append(String.format(Language.tr("Surface.CounterMulti.RunInfo"),station.conditions[i],station.counterNames[i],NumberTools.formatLong(value))+"\n");
			}
		}
		final long value=data.statisticElse.get();
		if (value>0) {
			if (first) {
				builder.results.append("\n"+Language.tr("Statistics.Counter")+"\n");
				first=false;
			}
			builder.results.append(String.format(Language.tr("Surface.CounterMulti.RunInfoElse"),station.counterNameElse,NumberTools.formatLong(value))+"\n");
		}
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
		return Language.trAll("Surface.CounterMulti.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.CounterMulti.XML.Group")));
		sub.setTextContent(groupName);

		final int size=Math.min(counterNames.size(),conditions.size()+1);
		for (int i=0;i<size;i++) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.CounterMulti.XML.Counter")));
			if (i<size-1) sub.setAttribute(Language.trPrimary("Surface.CounterMulti.XML.Condition"),conditions.get(i));
			sub.setTextContent(counterNames.get(i));
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

		if (Language.trAll("Surface.CounterMulti.XML.Group",name)) {
			groupName=node.getTextContent();
			return null;
		}

		if (Language.trAll("Surface.CounterMulti.XML.Counter",name)) {
			counterNames.add(content);
			conditions.add(Language.trAllAttribute("Surface.CounterMulti.XML.Condition",node));
		}

		return null;
	}

	/**
	 * Liefert den aktuellen Gruppennamen des Z�hlers
	 * @return	Gruppenname des Z�hlers
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Stellt den Gruppennamen des Z�hlers ein
	 * @param groupName	Neuer Gruppenname des Z�hlers
	 */
	public void setGroupName(final String groupName) {
		if (groupName!=null) this.groupName=groupName;
	}

	/**
	 * Liefert die Liste der Bedingungen
	 * @return	Liste der Bedingungen
	 */
	public List<String> getConditions() {
		return conditions;
	}

	/**
	 * Liefert die Liste der Z�hlernamen
	 * @return	Liste der Z�hlernamen
	 */
	public List<String> getCounterNames() {
		return counterNames;
	}

	@Override
	protected String getIDInfo() {
		if (groupName!=null && !groupName.isEmpty()) {
			return super.getIDInfo()+", "+Language.tr("Surface.CounterMulti.Dialog.GroupName.Short")+"="+groupName;
		} else {
			return super.getIDInfo();
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementCounterMulti";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!groupName.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.CounterMulti.Group"),groupName,1000);

		final int size=Math.min(counterNames.size(),conditions.size()+1);
		for (int i=0;i<size;i++) if (i<size-1) {
			descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.CounterMulti.Condition"),conditions.get(i)),counterNames.get(i),2000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.CounterMulti.ConditionElse"),counterNames.get(i),2000);
		}
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.hold,fixer);
	}
}