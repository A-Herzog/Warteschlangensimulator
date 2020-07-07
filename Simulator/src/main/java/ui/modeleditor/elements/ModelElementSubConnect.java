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

import javax.swing.JPopupMenu;

import simulator.editmodel.EditModel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Dies ist die Basisklasse für die Ein- und Ausgänge in bzw. aus einem Submodell
 * @author Alexander Herzog
 * @see ModelElementSub
 * @see ModelElementSubIn
 * @see ModelElementSubOut
 */
public class ModelElementSubConnect extends ModelElementBox {
	/**
	 * 0-basierende Nummer des Ein- bzw. Ausgangs
	 */
	protected int connectionNr;

	/**
	 * ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll
	 */
	protected int connectionStationID;

	/**
	 * Konstruktor der Klasse <code>ModelElementSubConnect</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param connectionNr	0-basierende Nummer des Ein- bzw. Ausgangs
	 * @param connectionStationID ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll
	 */
	public ModelElementSubConnect(final EditModel model, final ModelSurface surface, int connectionNr, int connectionStationID) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		this.connectionNr=connectionNr;
		this.connectionStationID=connectionStationID;
	}

	/**
	 * Stellt die anzuzeigenden Daten bzgl. der Verknüpfung an
	 * @param connectionNr	0-basierende Nummer des Ein- bzw. Ausgangs
	 * @param connectionStationID ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll
	 */
	public void setConnectionData(int connectionNr, int connectionStationID) {
		this.connectionNr=connectionNr;
		this.connectionStationID=connectionStationID;
		fireChanged();
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSubConnect)) return false;

		if (((ModelElementSubConnect)element).connectionNr!=connectionNr) return false;
		if (((ModelElementSubConnect)element).connectionStationID!=connectionStationID) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSubConnect) {
			connectionNr=((ModelElementSubConnect)element).connectionNr;
			connectionStationID=((ModelElementSubConnect)element).connectionStationID;
		}
	}

	private static final Color defaultBackgroundColor=new Color(250,250,250);

	/**
	 * Liefert die Hintergrundfarbe für die Box
	 * @return	Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Fügt Menüpunkte zum Hinzufügen von einlaufenden und auslaufender Kante zum Kontextmenü
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zurück, wenn Elemente in das Kontextmenü eingefügt wurden (und ggf. ein Separator vor dem nächsten Abschnitt gesetzt werden sollte)
	 */
	protected boolean addRemoveEdgesContextMenuItems(final JPopupMenu popupMenu, final boolean readOnly) {
		return false;
	}

	@Override
	public boolean canCopy() {
		return false;
	}

	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public boolean canSelect() {
		return false;
	}

	/**
	 * Liefert die 0-basierende Nummer des Ein- bzw. Ausgangs.
	 * @return	0-basierende Nummer des Ein- bzw. Ausgangs
	 */
	public int getConnectionNr() {
		return connectionNr;
	}

	/**
	 * Liefert die ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll.
	 * @return ID des Elements, mit dem diese Station auf der höheren Ebene verbunden sein soll
	 */
	public int getConnectionStationID() {
		return connectionStationID;
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		return false;
	}
}