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
package ui.modeleditor.coreelements;

import java.util.List;

import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Dies ist kein echtes Modell-Element, sondern dient in Listen aus {@link ModelElementPosition} zum Gruppieren von Elementen.
 * @author Alexander Herzog
 */
public class ModelElementListGroup extends ModelElementBox {
	/**
	 * 0-basierender Index der Gruppe
	 * @see #getIndex()
	 */
	private final int groupIndex;

	/**
	 * Name der Gruppe
	 * @see #getTypeName()
	 */
	private final String groupName;

	/**
	 * Untereintr�ge anzeigen
	 * @see #isShowSub()
	 */
	private boolean showSub;

	/**
	 * Liste der Elemente in dieser Gruppe
	 */
	private final List<ModelElement> subElements;

	/**
	 * Konstruktor der Klasse <code>ModelElementListGroup</code>
	 * @param groupIndex	0-basierender Index der Gruppe
	 * @param groupName	Name der Gruppe
	 * @param showSub Sollen die Untereintr�ge angezeigt werden?
	 * @param subElements	Liste der Elemente in dieser Gruppe
	 */
	public ModelElementListGroup(final int groupIndex, final String groupName, final boolean showSub, final List<ModelElement> subElements) {
		super(null,null,Shapes.ShapeType.SHAPE_NONE);
		this.groupIndex=groupIndex;
		this.groupName=groupName;
		this.showSub=showSub;
		this.subElements=subElements;
	}

	@Override
	public String getTypeName() {
		return groupName;
	}

	/**
	 * Gibt an, ob die Untereintr�ge angezeigt werden sollen.
	 * @return	Untereintr�ge anzeigen
	 */
	public boolean isShowSub() {
		return showSub;
	}

	/**
	 * Stellt ein, ob die Untereintr�ge angezeigt werden sollen.
	 * @param showSub	Untereintr�ge anzeigen
	 */
	public void setShowSub(final boolean showSub) {
		this.showSub=showSub;
	}

	/**
	 * Anzeige von Untereintr�gen umschalten.
	 * @see #isShowSub()
	 * @see #setShowSub(boolean)
	 */
	public void toggleShowSub() {
		showSub=!showSub;
	}

	/**
	 * Liefert den 0-basierenden Index der Gruppe wie im Konstruktor �bergeben.
	 * @return	0-basierender Index der Gruppe
	 */
	public int getIndex() {
		return groupIndex;
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		return false;
	}

	/**
	 * Pr�ft, ob sich ein {@link ModelElement} in der Gruppe befindet.
	 * @param testElement	Element bei dem gepr�ft werden soll, ob es sich in der aktuellen Gruppe befindet.
	 * @return	Liefert <code>true</code>, wenn sich das Element in der aktuellen Gruppe befindet
	 */
	public boolean isInGroup(final ModelElement testElement) {
		for (ModelElement element: subElements) if (element.getClass().getName().equals(testElement.getClass().getName())) return true;
		return false;
	}
}
