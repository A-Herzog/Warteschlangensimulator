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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Kundenausgang
 * @author Alexander Herzog
 */
public class ModelElementDispose extends ModelElementBox implements ModelElementEdgeMultiIn {
	private List<ModelElementEdge> connections;

	/* Wird nur beim Laden und Clonen verwendet. */
	private List<Integer> connectionIds=null;

	private boolean stoppSimulationOnClientArrival;

	/**
	 * Konstruktor der Klasse <code>ModelElementSource</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementDispose(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_LEFT);
		connections=new ArrayList<>();
		stoppSimulationOnClientArrival=false;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DISPOSE.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		if (stoppSimulationOnClientArrival) {
			return Language.tr("Surface.Dispose.TooltipStopp");
		} else {
			return Language.tr("Surface.Dispose.Tooltip");
		}
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDispose)) return false;
		final ModelElementDispose otherDispose=(ModelElementDispose)element;


		final List<ModelElementEdge> connections2=otherDispose.connections;
		if (connections==null || connections2==null || connections.size()!=connections2.size()) return false;
		for (int i=0;i<connections.size();i++) if (connections.get(i).getId()!=connections2.get(i).getId()) return false;

		if (stoppSimulationOnClientArrival!=otherDispose.stoppSimulationOnClientArrival) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDispose) {
			final ModelElementDispose copySource=(ModelElementDispose)element;

			connections.clear();
			final List<ModelElementEdge> connections2=copySource.connections;
			if (connections2!=null) {
				connectionIds=new ArrayList<>();
				for (int i=0;i<connections2.size();i++) connectionIds.add(connections2.get(i).getId());
			}

			stoppSimulationOnClientArrival=copySource.stoppSimulationOnClientArrival;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDispose clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDispose element=new ModelElementDispose(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		ModelElement element;
		if (connectionIds!=null) {
			for (int i=0;i<connectionIds.size();i++) {
				element=surface.getById(connectionIds.get(i));
				if (element instanceof ModelElementEdge) connections.add((ModelElementEdge)element);
			}
			connectionIds=null;
		}
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		if (stoppSimulationOnClientArrival) {
			return Language.tr("Surface.Dispose.NameStopp");
		} else {
			return Language.tr("Surface.Dispose.Name");
		}
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		if (stoppSimulationOnClientArrival) {
			return Language.tr("Surface.Dispose.NameStopp");
		} else {
			return Language.tr("Surface.Dispose.Name");
		}
	}

	private static final Color defaultBackgroundColor=new Color(255,148,148);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Gibt an, ob die Simulation abgebrochen werden soll, wenn an dieser Station ein Kunde eintrifft.
	 * @return	Liefert <code>true</code> wenn die Simulation abgebrochen werden soll, wenn an dieser Station ein Kunde eintrifft.
	 */
	public boolean isStoppSimulationOnClientArrival() {
		return stoppSimulationOnClientArrival;
	}

	/**
	 * Stellt ein, ob die Simulation abgebrochen werden soll, wenn an dieser Station ein Kunde eintrifft.
	 * @param stoppSimulationOnClientArrival	Wird <code>true</code> angegeben, so wird die Simulation abgebrochen, wenn an dieser Station ein Kunde eintrifft.
	 */
	public void setStoppSimulationOnClientArrival(final boolean stoppSimulationOnClientArrival) {
		this.stoppSimulationOnClientArrival=stoppSimulationOnClientArrival;
		fireChanged();
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
			new ModelElementDisposeDialog(owner,ModelElementDispose.this,readOnly);
		};
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
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();

		if (connections!=null && connections.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveAllEdges")));
			item.addActionListener((e)->{
				for (ModelElementEdge element : new ArrayList<>(connections)) surface.remove(element);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			popupMenu.addSeparator();
		}
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connections!=null) {
			while (connections.size()>0) {
				ModelElement element=connections.remove(0);
				surface.remove(element);
			}
		}
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connections!=null && connections.indexOf(element)>=0) {connections.remove(element); fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Dispose.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (stoppSimulationOnClientArrival) {
			Element sub=doc.createElement(Language.trPrimary("Surface.XML.Dispose.Stopp"));
			node.appendChild(sub);
			sub.setTextContent("1");
		}

		if (connections!=null) for (ModelElementEdge element: connections) {
			Element sub=doc.createElement(Language.trPrimary("Surface.XML.Connection"));
			node.appendChild(sub);
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
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

		if (Language.trAll("Surface.XML.Dispose.Stopp",name)) {
			stoppSimulationOnClientArrival=content.equals("1");
			return null;
		}

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.In",Language.trAllAttribute("Surface.XML.Connection.Type",node))) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			if (connectionIds==null) connectionIds=new ArrayList<>();
			connectionIds.add(I);
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connections.toArray(new ModelElementEdge[0]);
	}

	/**
	 * F�gt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die einlaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connections.indexOf(edge)<0) {
			connections.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation �berhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung �bersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation ber�cksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden m�ssten).
	 * @return	Gibt <code>true</code> zur�ck, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connections.size()>0;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDispose";
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsOut.size()>0) return false;

		this.connections.clear();
		this.connections.addAll(connectionsIn);

		return true;
	}
}
