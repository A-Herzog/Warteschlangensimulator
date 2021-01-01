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

import javax.swing.Icon;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElementLogicWithCondition;

/**
 * Diese Station bildet in der Flusssteuerung ein logisches "While" ab.
 * @author Alexander Herzog
 * @see ModelElementLogicEndWhile
 */
public class ModelElementLogicWhile extends ModelElementLogicWithCondition {
	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementLogicWhile(EditModel model, ModelSurface surface) {
		super(model,surface);
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_LOGIC_WHILE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.LogicWhile.Tooltip");
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementLogicWhile clone(final EditModel model, final ModelSurface surface) {
		final ModelElementLogicWhile element=new ModelElementLogicWhile(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.LogicWhile.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.LogicWhile.Name");
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.LogicWhile.XML.Root");
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementLogicWhile";
	}

	/**
	 * Gibt optional die ID f�r einen Infotext oben im zugeh�rigen Dialog zur�ck.
	 * @return	ID f�r einen Infotext oben im Dialog (kann <code>null</code> sein)
	 * @see InfoPanel
	 */
	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationLogicWhile;
	}
}
