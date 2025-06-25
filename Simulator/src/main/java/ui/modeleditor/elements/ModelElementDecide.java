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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
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
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.elements.DecideRecord.DecideMode;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Verzweigt die eintreffenden Kunden in verschiedene Richtungen
 * @author Alexander Herzog
 */
public class ModelElementDecide extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementEdgeMultiOutNumbered, ElementWithNewClientNames, ElementWithDecideData {
	/** Liste der einlaufenden Kanten */
	private final List<ModelElementEdge> connectionsIn;
	/** Liste der auslaufenden Kanten */
	private final List<ModelElementEdge> connectionsOut;

	/**
	 * Liste der IDs der einlaufenden Kanten (wird nur beim Laden und Clonen verwendet, ist sonst <code>null</code>)
	 * @see #connectionsIn
	 */
	private List<Integer> connectionsInIds=null;

	/**
	 * Liste der IDs der auslaufenden Kanten (wird nur beim Laden und Clonen verwendet, ist sonst <code>null</code>)
	 * @see #connectionsOut
	 */
	private List<Integer> connectionsOutIds=null;

	/**
	 * Einstellungen zur Verzweigung
	 */
	private final DecideRecord decideRecord=new DecideRecord(true);

	/**
	 * Liste mit neuen Kundentypen gemäß den Ausgängen (leere Strings stehen für "keine Änderung")
	 */
	private List<String> newClientTypes=null;

	/**
	 * Konstruktor der Klasse <code>ModelElementDecide</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementDecide(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_OCTAGON);
		connectionsIn=new ArrayList<>();
		connectionsOut=new ArrayList<>();
		decideRecord.addChangeListener(()->fireChanged());
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DECIDE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Decide.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	/**
	 * Liefert den Kundentyp je Ausgangskante
	 * @param index	Index der Ausgangskante
	 * @return	Kundentypbeschriftung an Ausgangskante
	 */
	private String getNewClientType(final int index) {
		if (index<0 || newClientTypes==null || newClientTypes.size()<=index) return "";
		final String newClientType=newClientTypes.get(index).trim();
		if (newClientType.isEmpty()) return "";
		return Language.tr("Surface.Duplicate.NewClientType")+": "+newClientType;
	}

	/**
	 * Aktualisiert die Beschriftung der auslaufenden Kante
	 * @see #fireChanged()
	 */
	private void updateEdgeLabel() {
		if (connectionsOut==null) return;

		final DecideMode mode=decideRecord.getMode();

		double sum=0;
		if (mode==DecideMode.MODE_CHANCE) {
			final List<String> rates=decideRecord.getRates();
			while (rates.size()<connectionsOut.size()) rates.add("1");
			for (int i=0;i<connectionsOut.size();i++) {
				final Double rate=NumberTools.getPlainDouble(rates.get(i));
				if (rate==null) {sum=-1; break;}
				sum+=Math.max(0,rate);
			}
			if (sum==0) sum=1;
		}

		if (mode==DecideMode.MODE_SEQUENCE) {
			final List<Integer> multiplicity=decideRecord.getMultiplicity();
			while (multiplicity.size()<connectionsOut.size()) multiplicity.add(1);
		}

		for (int i=0;i<connectionsOut.size();i++) {
			final ModelElementEdge connection=connectionsOut.get(i);
			String name="";
			String s;
			switch (mode) {
			case MODE_CHANCE:
				final List<String> rates=decideRecord.getRates();
				final String rateString=(i>=rates.size())?"1":rates.get(i);
				String info="";
				if (sum>0) {
					final Double rate=NumberTools.getPlainDouble(rateString);
					if (rate!=null) info=" ("+NumberTools.formatPercent(rate/sum)+")";
				}
				name=Language.tr("Surface.Decide.Rate")+" "+rateString+info;
				break;
			case MODE_CONDITION:
				name=(i<connectionsOut.size()-1)?(Language.tr("Surface.Decide.Condition")+" "+(i+1)):Language.tr("Surface.Decide.Condition.ElseCase");
				break;
			case MODE_CLIENTTYPE:
				final List<List<String>> clientTypes=decideRecord.getClientTypes();
				if (i>=clientTypes.size() || clientTypes.get(i).size()==0) {
					s=Language.tr("Dialog.Title.Error").toUpperCase();
				} else {
					final List<String> list=clientTypes.get(i);
					s=list.get(0);
					for (int j=1;j<Math.min(3,list.size());j++) s+=","+list.get(j);
					if (list.size()>3) s+=",...";
				}
				name=(i<connectionsOut.size()-1)?s:Language.tr("Surface.Decide.AllOtherClientTypes");
				break;
			case MODE_SEQUENCE:
				name=String.format(Language.tr("Surface.Decide.SequenceNumber"),i+1);
				final List<Integer> multiplicity=decideRecord.getMultiplicity();
				int mul=multiplicity.get(i);
				if (mul>1) name+=" ("+mul+"x)";
				break;
			case MODE_SHORTEST_QUEUE_NEXT_STATION:
				name="";
				break;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION:
				name="";
				break;
			case MODE_MIN_CLIENTS_NEXT_STATION:
				name="";
				break;
			case MODE_MIN_CLIENTS_PROCESS_STATION:
				name="";
				break;
			case MODE_LONGEST_QUEUE_NEXT_STATION:
				name="";
				break;
			case MODE_LONGEST_QUEUE_PROCESS_STATION:
				name="";
				break;
			case MODE_MAX_CLIENTS_NEXT_STATION:
				name="";
				break;
			case MODE_MAX_CLIENTS_PROCESS_STATION:
				name="";
				break;
			case MODE_KEY_VALUE:
				final List<String> values=decideRecord.getValues();
				s=(i>=values.size())?Language.tr("Dialog.Title.Error").toUpperCase():(decideRecord.getKey()+"="+values.get(i));
				name=(i<connectionsOut.size()-1)?s:Language.tr("Surface.Decide.AllOtherValues");
				break;
			}

			final String newClientType=getNewClientType(i);
			if (!newClientType.isEmpty()) name=name+", "+newClientType;
			connection.setName(name);
		}
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDecide)) return false;

		final ModelElementDecide decide=(ModelElementDecide)element;

		final List<ModelElementEdge> connectionsIn2=decide.connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		final List<ModelElementEdge> connectionsOut2=decide.connectionsOut;
		if (connectionsOut==null || connectionsOut2==null || connectionsOut.size()!=connectionsOut2.size()) return false;
		for (int i=0;i<connectionsOut.size();i++) if (connectionsOut.get(i).getId()!=connectionsOut2.get(i).getId()) return false;

		if (!decideRecord.equalsDecideRecord(decide.decideRecord,connectionsOut.size())) return false;

		if (connectionsOut.size()>0) {
			final List<String> newClientTypes2=decide.newClientTypes;
			for (int i=0;i<connectionsOut.size();i++) {
				String name1="";
				String name2="";
				if (newClientTypes!=null && newClientTypes.size()>i) name1=newClientTypes.get(i);
				if (newClientTypes2!=null && newClientTypes2.size()>i) name2=newClientTypes2.get(i);
				if (!name1.equals(name2)) return false;
			}
		}

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDecide) {
			final ModelElementDecide source=(ModelElementDecide)element;

			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=source.connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			connectionsOut.clear();
			final List<ModelElementEdge> connectionsOut2=source.connectionsOut;
			if (connectionsOut2!=null) {
				connectionsOutIds=new ArrayList<>();
				for (int i=0;i<connectionsOut2.size();i++) connectionsOutIds.add(connectionsOut2.get(i).getId());
			}

			decideRecord.copyDataFrom(source.decideRecord);

			if (source.newClientTypes!=null) newClientTypes=new ArrayList<>(source.newClientTypes);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDecide clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDecide element=new ModelElementDecide(model,surface);
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

		if (connectionsInIds!=null) {
			for (int i=0;i<connectionsInIds.size();i++) {
				element=surface.getById(connectionsInIds.get(i));
				if (element instanceof ModelElementEdge) connectionsIn.add((ModelElementEdge)element);
			}
			connectionsInIds=null;
			updateEdgeLabel();
		}

		if (connectionsOutIds!=null) {
			for (int i=0;i<connectionsOutIds.size();i++) {
				element=surface.getById(connectionsOutIds.get(i));
				if (element instanceof ModelElementEdge) connectionsOut.add((ModelElementEdge)element);
			}
			connectionsOutIds=null;
			updateEdgeLabel();
		}

		if (newClientTypes!=null) {
			while (newClientTypes.size()>connectionsOut.size()) newClientTypes.remove(newClientTypes.size()-1);
			while (newClientTypes.size()<connectionsOut.size()) newClientTypes.add("");
			updateEdgeLabel();
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Decide.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Decide.Name.Short");
	}

	/**
	 * Liefert optional eine zusätzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zusätzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null) return null; /* keinen Untertitel in der Templates-Liste anzeigen */
		return decideRecord.getMode().getName();
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(204,99,255);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
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
			new ModelElementDecideDialog(owner,ModelElementDecide.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsDecide(this,parentMenu,addNextStation);
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
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();
		boolean needSeparator=false;

		needSeparator=needSeparator || addEdgesInContextMenu(popupMenu,surface,readOnly);

		if (connectionsOut!=null && connectionsOut.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener(e->{
				for (ModelElementEdge element : new ArrayList<>(connectionsOut)) surface.remove(element);
			});
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);
			needSeparator=true;

			if (connectionsOut.size()>1) {
				final JMenu menu=new JMenu(Language.tr("Surface.Connection.LineMode.ChangeAllEdgesOut"));
				popupMenu.add(menu);

				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Global"),Images.MODEL.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(null));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Direct"),Images.EDGE_MODE_DIRECT.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.DIRECT));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLine"),Images.EDGE_MODE_MULTI_LINE.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.MULTI_LINE));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLineRounded"),Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.MULTI_LINE_ROUNDED));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.CubicCurve"),Images.EDGE_MODE_CUBIC_CURVE.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.CUBIC_CURVE));
			}
		}

		if (needSeparator) popupMenu.addSeparator();
	}

	/**
	 * Stellt den Darstellungsmodus für alle auslaufenden Kanten ein.
	 * @param lineMode	Neuer Darstellungsmodus
	 */
	private void setEdgeOutLineMode(final ModelElementEdge.LineMode lineMode) {
		for (ModelElementEdge edge: connectionsOut) {
			edge.setLineMode(lineMode);
			edge.fireChanged();
		}
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionsIn!=null) {
			while (connectionsIn.size()>0) {
				ModelElement element=connectionsIn.remove(0);
				surface.remove(element);
			}
		}
		if (connectionsOut!=null) {
			while (connectionsOut.size()>0) {
				ModelElement element=connectionsOut.remove(0);
				surface.remove(element);
			}
		}
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionsOut!=null && connectionsOut.indexOf(element)>=0) {connectionsOut.remove(element); fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Decide.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		decideRecord.saveToXml(doc,node);

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionsOut!=null) for (int i=0;i<connectionsOut.size();i++) {
			ModelElementEdge element=connectionsOut.get(i);
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
			if (newClientTypes!=null && newClientTypes.size()>i && !newClientTypes.get(i).isBlank()) sub.setAttribute(Language.trPrimary("Surface.XML.Connection.NewClientType"),newClientTypes.get(i).trim());
			decideRecord.saveToXmlOutput(doc,sub,i,i==connectionsOut.size()-1);
		}
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

		error=decideRecord.loadFromXml(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
			}
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				if (connectionsOutIds==null) connectionsOutIds=new ArrayList<>();
				connectionsOutIds.add(I);

				final String newClientType=Language.trAllAttribute("Surface.XML.Connection.NewClientType",node);
				if (!newClientType.isBlank()) {
					if (newClientTypes==null) newClientTypes=new ArrayList<>();
					while (newClientTypes.size()<connectionsOutIds.size()-1) newClientTypes.add("");
					newClientTypes.add(newClientType);
				}

				error=decideRecord.loadOptionFromXml(name,node);
				if (error!=null) return error;
			}
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * Fügt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die einlaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0 && connectionsOut.indexOf(edge)<0) {
			connectionsIn.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connectionsIn.toArray(ModelElementEdge[]::new);
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return true;
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0 && connectionsOut.indexOf(edge)<0) {
			connectionsOut.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Fügt eine auslaufende Kante an einer bestimmten Stelle in der Liste der auslaufenden Kanten hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @param index	Index der neuen Kante in der Liste der Kanten
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge, int index) {
		if (edge==null || connectionsIn.indexOf(edge)>=0 || connectionsOut.indexOf(edge)>=0) return false;
		if (index<0 || index>connectionsOut.size()) return addEdgeOut(edge);
		connectionsOut.add(index,edge);
		fireChanged();
		return true;
	}

	/**
	 * Auslaufende Kanten
	 * @return	Auslaufenden Kante
	 */
	@Override
	public ModelElementEdge[] getEdgesOut() {
		return connectionsOut.toArray(ModelElementEdge[]::new);
	}

	@Override
	public DecideRecord getDecideRecord() {
		return decideRecord;
	}

	/**
	 * Liefert die Liste der Namen der neuen Kundentypen, die bei Weiterleitung über die verschiedenen Ausgangskanten zugewiesen werden sollen.
	 * @return	Liste mit neuen Kundentypen (Länge entspricht der Anzahl an Ausgangskanten; leere Strings stehen für "keine Änderung"; Rückgabewert ist nie <code>null</code> und Einträge sind ebenfalls nie <code>null</code>)
	 */
	public List<String> getChangedClientTypes() {
		final List<String> result=new ArrayList<>();
		if (newClientTypes!=null) for (int i=0;i<Math.min(newClientTypes.size(),connectionsOut.size());i++) result.add(newClientTypes.get(i).trim());
		while (result.size()<connectionsOut.size()) result.add("");
		return result;
	}

	/**
	 * Stellt die Namen der neuen Kundentypen, die bei Weiterleitung über die verschiedenen Ausgangskanten zugewiesen werden sollen, ein.
	 * @param changedClientTypes	Liste mit neuen Kundentypen (leere Strings stehen für "keine Änderung")
	 */
	public void setChangedClientTypes(final List<String> changedClientTypes) {
		if (newClientTypes==null) newClientTypes=new ArrayList<>();
		newClientTypes.clear();
		if (changedClientTypes!=null) for (int i=0;i<Math.min(changedClientTypes.size(),connectionsOut.size());i++) {
			if (changedClientTypes.get(i)==null) newClientTypes.add(""); else  newClientTypes.add(changedClientTypes.get(i).trim());
		}
		while (newClientTypes.size()<connectionsOut.size()) newClientTypes.add("");
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation überhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung übersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation berücksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden müssten).
	 * @return	Gibt <code>true</code> zurück, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connectionsIn.size()>0;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		if (decideRecord.renameClientType(oldName,newName)) updateEdgeLabel();

		if (newClientTypes!=null) for (int i=0;i<newClientTypes.size();i++) if (newClientTypes.get(i).equals(oldName)) {
			newClientTypes.set(i,newName);
			updateEdgeLabel();
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDecide";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		switch (decideRecord.getMode()) {
		case MODE_CHANCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Rate"),1000);
			final List<String> rates=decideRecord.getRates();
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription=String.format(Language.tr("ModelDescription.Decide.Rate"),(i>=rates.size())?"1":rates.get(i));
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		case MODE_CONDITION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Condition"),1000);
			final List<String> conditions=decideRecord.getConditions();
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription;
				if (i<connectionsOut.size()-1) {
					edgeDescription=String.format(Language.tr("ModelDescription.Decide.Condition"),(i>=conditions.size())?"":conditions.get(i));
				} else {
					edgeDescription=Language.tr("ModelDescription.Decide.Condition.Else");
				}
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		case MODE_CLIENTTYPE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ClientType"),1000);
			final List<List<String>> clientTypes=decideRecord.getClientTypes();
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription;
				if (i<connectionsOut.size()-1) {
					final String s=(i<clientTypes.size() && clientTypes.get(i).size()>0)?String.join(", ",clientTypes.get(i)):"";
					edgeDescription=String.format(Language.tr("ModelDescription.Decide.ClientType"),s);
				} else {
					edgeDescription=Language.tr("ModelDescription.Decide.ClientType.Else");
				}
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		case MODE_SEQUENCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Sequence"),1000);
			final List<Integer> multiplicity=decideRecord.getMultiplicity();
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String info=(i>=multiplicity.size() || multiplicity.get(i).intValue()==1)?"":(" ("+multiplicity.get(i).intValue()+"x)");
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+info+newClientType,edge);
			}
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ShortestQueueNextStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ShortestQueueNextProcessStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LeastClientsNextStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LeastClientsNextProcessStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LongestQueueNextStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LongestQueueNextProcessStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.MostClientsNextStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.MostClientsNextProcessStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_KEY_VALUE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.StringProperty"),1000);
			final List<String> values=decideRecord.getValues();
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription;
				if (i<connectionsOut.size()-1) {
					final String s=(i<values.size())?values.get(i):"";
					edgeDescription=String.format(Language.tr("ModelDescription.Decide.StringProperty"),decideRecord.getKey(),s);
				} else {
					edgeDescription=Language.tr("ModelDescription.Decide.StringProperty.Else");
				}
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		}
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		this.connectionsOut.clear();
		this.connectionsOut.addAll(connectionsOut);

		return true;
	}

	@Override
	public String[] getNewClientTypes() {
		final Set<String> set=new HashSet<>();
		if (newClientTypes!=null) for (String newClientType: newClientTypes) set.add(newClientType);
		return set.toArray(String[]::new);
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.duplicate,fixer);
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
	}
}