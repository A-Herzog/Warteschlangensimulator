/**
 * Copyright 2025 Alexander Herzog
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import ui.modeleditor.ModelDataRenameListener;
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
 * Zuweisung eines Kundentyps gem�� Wahrscheinlichkeit oder Bedingung
 * @author Alexander Herzog
 */
public class ModelElementAssignMulti extends ModelElementMultiInSingleOutBox implements ElementWithNewClientNames, ModelDataRenameListener, ElementWithDecideData {
	/**
	 * Liste mit neuen Kundentypen gem�� den Ausg�ngen (leere Strings stehen f�r "keine �nderung")
	 */
	private List<String> newClientTypes;

	/**
	 * Einstellungen zur Verzweigung
	 */
	private final DecideRecord decideRecord=new DecideRecord(false);

	/**
	 * Zus�tzliche optionale Bedingung, die f�r die Zuweisung erf�llt sein muss (kann <code>null</code> sein)
	 * @see #getCondition()
	 * @see #setCondition(String)
	 */
	private String condition;

	/**
	 * Konstruktor der Klasse <code>ModelElementAssign</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAssignMulti(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE);
		newClientTypes=new ArrayList<>();
		decideRecord.addChangeListener(()->fireChanged());
		condition="";
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ASSIGN_MULTI.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AssignMulti.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAssignMulti)) return false;
		final ModelElementAssignMulti otherAssign=(ModelElementAssignMulti)element;

		if (!Objects.deepEquals(newClientTypes,otherAssign.newClientTypes)) return false;

		if (!decideRecord.equalsDecideRecord(otherAssign.decideRecord,newClientTypes.size())) return false;

		if (!otherAssign.condition.equals(condition)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAssignMulti) {
			final ModelElementAssignMulti source=(ModelElementAssignMulti)element;

			if (source.newClientTypes!=null) newClientTypes=new ArrayList<>(source.newClientTypes);

			decideRecord.copyDataFrom(source.decideRecord);

			condition=source.condition;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAssignMulti clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAssignMulti element=new ModelElementAssignMulti(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AssignMulti.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.AssignMulti.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,255,180);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert die Liste der Namen der neuen Kundentypen, die bei Weiterleitung �ber die verschiedenen Ausgangskanten zugewiesen werden sollen.
	 * @return	Liste mit neuen Kundentypen
	 */
	public List<String> getClientTypes() {
		return newClientTypes.stream().map(String::trim).collect(Collectors.toList());
	}

	/**
	 * Stellt die Namen der neuen Kundentypen, die bei Weiterleitung �ber die verschiedenen Ausgangskanten zugewiesen werden sollen, ein.
	 * @param clientTypes	Liste mit neuen Kundentypen
	 */
	public void setClientTypes(final List<String> clientTypes) {
		if (newClientTypes==null) newClientTypes=new ArrayList<>();
		newClientTypes.clear();
		newClientTypes.addAll(clientTypes.stream().map(String::trim).collect(Collectors.toList()));
	}

	@Override
	public DecideRecord getDecideRecord() {
		return decideRecord;
	}

	/**
	 * Liefert die optionale Bedingung, die f�r die Zuweisung erf�llt sein muss.
	 * @return	Bedingung, die f�r die Zuweisung erf�llt sein muss (kann <code>null</code> sein)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung, die f�r die Zuweisung erf�llt sein muss, ein.
	 * @param condition	Optionale Bedingung, die f�r die Zuweisung erf�llt sein muss (kann <code>null</code> sein oder leer sein)
	 */
	public void setCondition(final String condition) {
		this.condition=(condition==null)?"":condition;
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
			new ModelElementAssignMultiDialog(owner,ModelElementAssignMulti.this,readOnly);
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
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_CLIENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_CLIENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_CLIENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_W_CLIENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_W_CLIENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_W_CLIENT);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
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
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
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
		return Language.trAll("Surface.AssignMulti.XML.Root");
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

		decideRecord.saveToXml(doc,node);

		for (int i=0;i<newClientTypes.size();i++) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.AssignMulti.XML.AssignRecord")));
			sub.setTextContent(newClientTypes.get(i));
			decideRecord.saveToXmlOutput(doc,sub,i,i==newClientTypes.size()-1);
		}

		if (!condition.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.AssignMulti.XML.Condition")));
			sub.setTextContent(condition);
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

		error=decideRecord.loadFromXml(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.AssignMulti.XML.AssignRecord",name)) {
			newClientTypes.add(content);
			error=decideRecord.loadOptionFromXml(name,node);
			if (error!=null) return error;
			return null;
		}

		if (Language.trAll("Surface.AssignMulti.XML.Condition",name)) {
			condition=content;
			return null;
		}

		return null;
	}

	@Override
	public String[] getNewClientTypes() {
		return newClientTypes.toArray(String[]::new);
	}

	/**
	 * Liefert die ver�nderbare Liste der neuen Kundentypen.
	 * @return	Liste der neuen Kundentypen
	 */
	public List<String> getNewClientTypesList() {
		return newClientTypes;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station w�hrend der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return super.showAnimationRunData(); /* statt einfach "false". Schadet ja auch an dieser Station nicht. */
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAssignMulti";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		switch (decideRecord.getMode()) {
		case MODE_CHANCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AssignMulti.Mode"),Language.tr("ModelDescription.AssignMulti.Mode.Rate"),1000);
			final List<String> rates=decideRecord.getRates();
			for (int i=0;i<newClientTypes.size();i++) {
				final String newClientType=newClientTypes.get(i);
				final String edgeDescription=String.format(Language.tr("ModelDescription.AssignMulti.Rate"),(i>=rates.size())?"1":rates.get(i));
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AssignMulti.NewClientType"),newClientType),edgeDescription,1000);
			}
			break;
		case MODE_CONDITION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AssignMulti.Mode"),Language.tr("ModelDescription.AssignMulti.Mode.Condition"),1000);
			final List<String> conditions=decideRecord.getConditions();
			for (int i=0;i<newClientTypes.size();i++) {
				final String newClientType=newClientTypes.get(i);
				final String edgeDescription;
				if (i<newClientTypes.size()-1) {
					edgeDescription=String.format(Language.tr("ModelDescription.AssignMulti.Condition"),(i>=conditions.size())?"":conditions.get(i));
				} else {
					edgeDescription=Language.tr("ModelDescription.AssignMulti.Condition.Else");
				}
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AssignMulti.NewClientType"),newClientType),edgeDescription,1000);
			}
			break;
		case MODE_CLIENTTYPE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AssignMulti.Mode"),Language.tr("ModelDescription.AssignMulti.Mode.ClientType"),1000);
			final List<List<String>> clientTypes=decideRecord.getClientTypes();
			for (int i=0;i<newClientTypes.size();i++) {
				final String newClientType=newClientTypes.get(i);
				final String edgeDescription;
				if (i<newClientTypes.size()-1) {
					final String s=(i<clientTypes.size() && clientTypes.get(i).size()>0)?String.join(", ",clientTypes.get(i)):"";
					edgeDescription=String.format(Language.tr("ModelDescription.AssignMulti.ClientType"),s);
				} else {
					edgeDescription=Language.tr("ModelDescription.AssignMulti.ClientType.Else");
				}
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AssignMulti.NewClientType"),newClientType),edgeDescription,1000);
			}
			break;
		case MODE_SEQUENCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AssignMulti.Mode"),Language.tr("ModelDescription.AssignMulti.Mode.Sequence"),1000);
			final List<Integer> multiplicity=decideRecord.getMultiplicity();
			for (int i=0;i<newClientTypes.size();i++) {
				final String newClientType=newClientTypes.get(i);
				final String info=multiplicity.get(i).intValue()+"x";
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AssignMulti.NewClientType"),newClientType),info,1000);
			}
			break;
		case MODE_KEY_VALUE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AssignMulti.Mode"),Language.tr("ModelDescription.AssignMulti.Mode.StringProperty"),1000);
			final List<String> values=decideRecord.getValues();
			for (int i=0;i<newClientTypes.size();i++) {
				final String newClientType=newClientTypes.get(i);
				final String edgeDescription;
				if (i<newClientTypes.size()-1) {
					final String s=(i<values.size())?values.get(i):"";
					edgeDescription=String.format(Language.tr("ModelDescription.AssignMulti.StringProperty"),decideRecord.getKey(),s);
				} else {
					edgeDescription=Language.tr("ModelDescription.AssignMulti.StringProperty.Else");
				}
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AssignMulti.NewClientType"),newClientType),edgeDescription,1000);
			}
			break;
		default:
			/* Keine weiteren der Optionen hier nutzbar. */
			break;
		}

		if (!condition.isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.AssignMulti.Condition"),condition,5000);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		decideRecord.search(this,searcher);

		for (int i=0;i<newClientTypes.size();i++) {
			final String name=newClientTypes.get(i);
			if (name==null || name.isBlank()) continue;
			final int nr=i;
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.ClientType"),name,newName->{newClientTypes.set(nr,newName);});
		}

		if (!condition.isEmpty()) searcher.testString(this,Language.tr("Editor.DialogBase.Search.Condition"),condition,newCondition->condition=newCondition);
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.hold,fixer);
	}

	@Override
	public void objectRenamed(String oldName, String newName, RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		if (decideRecord.renameClientType(oldName,newName)) updateEdgeLabel();

		if (newClientTypes!=null) for (int i=0;i<newClientTypes.size();i++) if (newClientTypes.get(i).equals(oldName)) {
			newClientTypes.set(i,newName);
			updateEdgeLabel();
		}
	}
}
