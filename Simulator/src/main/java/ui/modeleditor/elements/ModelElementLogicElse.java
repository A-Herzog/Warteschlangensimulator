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

import java.net.URL;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElementLogic;

/**
 * Diese Station bildet in der Flusssteuerung ein logisches "Else" ab.
 * @author Alexander Herzog
 * @see ModelElementLogicIf
 * @see ModelElementLogicElseIf
 * @see ModelElementLogicEndIf
 */
public class ModelElementLogicElse extends ModelElementLogic {
	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementLogicElse(EditModel model, ModelSurface surface) {
		super(model,surface);
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_LOGIC_ELSE.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.LogicElse.Tooltip");
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementLogicElse clone(final EditModel model, final ModelSurface surface) {
		final ModelElementLogicElse element=new ModelElementLogicElse(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.LogicElse.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.LogicElse.Name");
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.LogicElse.XML.Root");
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementLogicElse";
	}

	/**
	 * Gibt optional die ID f�r einen Infotext oben im zugeh�rigen Dialog zur�ck.
	 * @return	ID f�r einen Infotext oben im Dialog (kann <code>null</code> sein)
	 * @see InfoPanel
	 */
	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationLogicElse;
	}
}
