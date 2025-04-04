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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * L�st in Abh�ngigkeit vom verschiedenen Bedingungen verschiedene Aktionen aus
 * @author Alexander Herzog
 * @see ModelElementActionRecord
 */
public class ModelElementAction extends ModelElementBox implements ModelElementSignalTrigger, ElementWithNewVariableNames, ElementWithAnimationScripts {
	/**
	 * Liste mit allen Bedingungen und auszul�senden Aktionen
	 */
	private final List<ModelElementActionRecord> records;

	/**
	 * Konstruktor der Klasse <code>ModelElementAction</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAction(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		records=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ACTION.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Action.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAction)) return false;

		final ModelElementAction other=(ModelElementAction)element;
		if (records.size()!=other.records.size()) return false;
		for (int i=0;i<records.size();i++) if (!records.get(i).equalsRecord(other.records.get(i))) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAction) {
			records.clear();
			final ModelElementAction copySource=(ModelElementAction)element;
			for (ModelElementActionRecord record: copySource.records) records.add(new ModelElementActionRecord(record));
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAction clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAction element=new ModelElementAction(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Action.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Action.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,200,80);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert die Liste mit allen Bedingungen und auszul�senden Aktionen
	 * @return	Liste mit allen Bedingungen und auszul�senden Aktionen
	 * @see ModelElementActionRecord
	 */
	public List<ModelElementActionRecord> getRecordsList() {
		return records;
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
			new ModelElementActionDialog(owner,ModelElementAction.this,readOnly);
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
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Action.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		for (ModelElementActionRecord record: records) record.saveToXML(doc,node);
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

		if (Language.trAll("Surface.Action.XML.Record",name)) {
			final ModelElementActionRecord record=new ModelElementActionRecord(ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION);
			error=record.loadFromXML(node);
			if (error!=null) return error;
			records.add(record);
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAction";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		for (int i=0;i<records.size();i++) records.get(i).buildDescription(descriptionBuilder,1000+100*i);
	}

	@Override
	public String[] getVariables() {
		final List<String> variableNames=new ArrayList<>();
		for (ModelElementActionRecord record: records) if (record.getActionType()==ModelElementActionRecord.ActionType.ACTION_ASSIGN) {
			final String variable=record.getAssignVariable();
			if (variable!=null && !variable.isEmpty()) variableNames.add(variable);
		}
		return variableNames.toArray(String[]::new);
	}

	@Override
	public String[] getSignalNames() {
		final List<String> signalNames=new ArrayList<>();
		for (ModelElementActionRecord record: records) if (record.getActionType()==ModelElementActionRecord.ActionType.ACTION_SIGNAL) {
			final String signal=record.getSignalName();
			if (signal!=null && !signal.isEmpty()) signalNames.add(signal);
		}
		return signalNames.toArray(String[]::new);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (!connectionsIn.isEmpty()) return false;
		if (!connectionsOut.isEmpty()) return false;

		return false;
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (ModelElementActionRecord record: records) record.search(searcher,this);
	}

	@Override
	public AnimationExpression[] getAnimationExpressions() {
		final List<AnimationExpression> list=new ArrayList<>();
		for (ModelElementActionRecord record: records) if (record.getActionType()==ModelElementActionRecord.ActionType.ACTION_SCRIPT) {
			final AnimationExpression animationExpression=new AnimationExpression();
			switch (record.getScriptMode()) {
			case Javascript: animationExpression.setJavascript(record.getScript()); break;
			case Java: animationExpression.setJava(record.getScript()); break;
			default: animationExpression.setJavascript(record.getScript()); break;
			}
			list.add(animationExpression);
		}
		return list.toArray(AnimationExpression[]::new);
	}
}