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

import javax.swing.Icon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementInteractiveButton;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Schaltfläche, die während der Animation des Modells angeklickt werden kann, um so eine
 * bestimmte Aktion auszulösen.<br>
 * Für eine normale Simulation (ohne Animationsausgabe) ist diese Station ohne Bedeutung.
 * @author Alexander Herzog
 */
public class ModelElementInteractiveButton extends ModelElementBox implements ElementWithAnimationDisplay, ModelElementSignalTrigger, ElementWithNewVariableNames, ElementAnimationClickable, ElementWithAnimationScripts {
	/**
	 * Liste mit allen auszulösenden Aktionen
	 * @see #getRecordsList()
	 */
	private final List<ModelElementActionRecord> records;

	/**
	 * Konstruktor der Klasse <code>ModelElementInteractiveButton</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementInteractiveButton(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_BUTTON);
		records=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_INTERACTIVE_BUTTON.getIcon();
	}

	@Override
	public boolean isVisualOnly() {
		return true;
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InteractiveButton.Tooltip");
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InteractiveButton.Name");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementInteractiveButton)) return false;

		final ModelElementInteractiveButton other=(ModelElementInteractiveButton)element;
		if (records.size()!=other.records.size()) return false;
		for (int i=0;i<records.size();i++) if (!records.get(i).equalsRecord(other.records.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementInteractiveButton) {
			records.clear();
			final ModelElementInteractiveButton copySource=(ModelElementInteractiveButton)element;
			for (ModelElementActionRecord record: copySource.records) records.add(new ModelElementActionRecord(record));
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementInteractiveButton clone(final EditModel model, final ModelSurface surface) {
		final ModelElementInteractiveButton element=new ModelElementInteractiveButton(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.InteractiveButton.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(220,220,220);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert die Liste mit allen auszulösenden Aktionen
	 * @return	Liste mit allen auszulösenden Aktionen
	 * @see ModelElementActionRecord
	 */
	public List<ModelElementActionRecord> getRecordsList() {
		return records;
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
			new ModelElementInteractiveButtonDialog(owner,ModelElementInteractiveButton.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.InteractiveButton.XML.Root");

	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		for (ModelElementActionRecord record: records) record.saveToXML(doc,node);
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

		if (Language.trAll("Surface.Action.XML.Record",name)) {
			final ModelElementActionRecord record=new ModelElementActionRecord(ModelElementActionRecord.ActionMode.ACTION_ONLY);
			error=record.loadFromXML(node);
			if (error!=null) return error;
			records.add(record);
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementInteractiveButton";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
		for (int i=0;i<records.size();i++) records.get(i).buildDescription(descriptionBuilder,1000+100*i);
	}

	/**
	 * Wurde die Schaltfläche angeklickt?
	 * @see #clicked
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private volatile boolean clicked=false;

	@Override
	public void clicked(final int x, final int y, final SimulationData simData) {
		if (simData!=null && simData.simulator.isPaused()) {
			triggerAction(simData,true);
			return;
		}
		clicked=true;
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		clicked=false;
	}

	/**
	 * Löst die in dem Objekt hinterlegten Aktionen aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param processDirect	Soll der Klick direkt an das Laufzeit-Element durchgereicht werden (<code>true</code>) oder über ein Ereignis abgewickelt werden (<code>false</code>)
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private void triggerAction(final SimulationData simData, final boolean processDirect) {
		final RunElementInteractiveButton runElement=(RunElementInteractiveButton)simData.runModel.elementsFast[getId()];
		if (processDirect) runElement.triggered(simData); else runElement.clicked(simData);
	}

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (simData!=null && clicked) {
			clicked=false;
			triggerAction(simData,false);
		}

		return false;
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
