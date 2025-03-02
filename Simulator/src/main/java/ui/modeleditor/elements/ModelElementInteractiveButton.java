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
 * Schaltfl�che, die w�hrend der Animation des Modells angeklickt werden kann, um so eine
 * bestimmte Aktion auszul�sen.<br>
 * F�r eine normale Simulation (ohne Animationsausgabe) ist diese Station ohne Bedeutung.
 * @author Alexander Herzog
 */
public class ModelElementInteractiveButton extends ModelElementBox implements ElementWithAnimationDisplay, ModelElementSignalTrigger, ElementWithNewVariableNames, ElementAnimationClickable, ElementWithAnimationScripts {
	/**
	 * Liste mit allen auszul�senden Aktionen
	 * @see #getRecordsList()
	 */
	private final List<ModelElementActionRecord> records;

	/**
	 * Konstruktor der Klasse <code>ModelElementInteractiveButton</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementInteractiveButton(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_BUTTON);
		records=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
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
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.InteractiveButton.Tooltip");
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.InteractiveButton.Name");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
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
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
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
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
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
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(220,220,220);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert die Liste mit allen auszul�senden Aktionen
	 * @return	Liste mit allen auszul�senden Aktionen
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
			new ModelElementInteractiveButtonDialog(owner,ModelElementInteractiveButton.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.InteractiveButton.XML.Root");

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
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
		for (int i=0;i<records.size();i++) records.get(i).buildDescription(descriptionBuilder,1000+100*i);
	}

	/**
	 * Wurde die Schaltfl�che angeklickt?
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
	 * L�st die in dem Objekt hinterlegten Aktionen aus.
	 * @param simData	Simulationsdatenobjekt
	 * @param processDirect	Soll der Klick direkt an das Laufzeit-Element durchgereicht werden (<code>true</code>) oder �ber ein Ereignis abgewickelt werden (<code>false</code>)
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
