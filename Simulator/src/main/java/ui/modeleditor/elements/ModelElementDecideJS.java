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
import java.util.List;
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
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Verzweigt die eintreffenden Kunden in verschiedene Richtungen
 * gemäß einem Javascript Skript
 * @author Alexander Herzog
 */
public class ModelElementDecideJS extends ModelElementBox implements ModelElementEdgeMultiIn, ModelElementEdgeMultiOutNumbered, ElementWithScript {
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
	 * Skript zur Fallunterscheidung
	 * @see #getScript()
	 * @see #setScript(String)
	 */
	private String script;

	/**
	 * Skriptsprache
	 * @see #getMode()
	 * @see #setMode(ScriptMode)
	 */
	private ScriptMode mode;

	/**
	 * Konstruktor der Klasse <code>ModelElementDecideJS</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementDecideJS(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_OCTAGON);
		connectionsIn=new ArrayList<>();
		connectionsOut=new ArrayList<>();
		script="";
		mode=ScriptMode.Javascript;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DECIDE_JS.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.DecideJS.Tooltip");
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
	 * Aktualisiert die Beschriftung der auslaufenden Kante
	 * @see #fireChanged()
	 */
	private void updateEdgeLabel() {
		if (connectionsOut==null) return;

		for (int i=0;i<connectionsOut.size();i++) {
			final ModelElementEdge connection=connectionsOut.get(i);
			connection.setName(String.format(Language.tr("Surface.DecideJS.EdgeNumber"),i+1));
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
		if (!(element instanceof ModelElementDecideJS)) return false;

		final List<ModelElementEdge> connectionsIn2=((ModelElementDecideJS)element).connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		final List<ModelElementEdge> connectionsOut2=((ModelElementDecideJS)element).connectionsOut;
		if (connectionsOut==null || connectionsOut2==null || connectionsOut.size()!=connectionsOut2.size()) return false;
		for (int i=0;i<connectionsOut.size();i++) if (connectionsOut.get(i).getId()!=connectionsOut2.get(i).getId()) return false;

		if (!((ModelElementDecideJS)element).script.equals(script)) return false;
		if (((ModelElementDecideJS)element).mode!=mode) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDecideJS) {

			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=((ModelElementDecideJS)element).connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			connectionsOut.clear();
			final List<ModelElementEdge> connectionsOut2=((ModelElementDecideJS)element).connectionsOut;
			if (connectionsOut2!=null) {
				connectionsOutIds=new ArrayList<>();
				for (int i=0;i<connectionsOut2.size();i++) connectionsOutIds.add(connectionsOut2.get(i).getId());
			}

			script=((ModelElementDecideJS)element).script;
			mode=((ModelElementDecideJS)element).mode;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDecideJS clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDecideJS element=new ModelElementDecideJS(model,surface);
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
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.DecideJS.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.DecideJS.Name.Short");
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
			new ModelElementDecideJSDialog(owner,ModelElementDecideJS.this,readOnly);
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
	 * Liefert das Skript zur Fallunterscheidung zurück
	 * @return	Skript
	 */
	@Override
	public String getScript() {
		return script;
	}

	/**
	 * Stellt ein neues Skript zur Fallunterscheidung ein
	 * @param script	Skript
	 */
	@Override
	public void setScript(final String script) {
		if (script==null) this.script=""; else this.script=script.trim();
	}

	/**
	 * Gibt die Skriptsprache an
	 * @return	Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	@Override
	public ScriptMode getMode() {
		return mode;
	}

	/**
	 * Stellt die Skriptsprache ein.
	 * @param mode	Skriptsprache
	 * @see ElementWithScript.ScriptMode
	 */
	@Override
	public void setMode(final ScriptMode mode) {
		if (mode!=null) this.mode=mode;
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.DecideJS.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DecideJS.XML.Script")));
		switch (mode) {
		case Java:
			sub.setAttribute(Language.trPrimary("Surface.DecideJS.XML.Script.Language"),Language.trPrimary("Surface.DecideJS.XML.Script.Java"));
			break;
		case Javascript:
			sub.setAttribute(Language.trPrimary("Surface.DecideJS.XML.Script.Language"),Language.trPrimary("Surface.DecideJS.XML.Script.Javascript"));
			break;
		}
		sub.setTextContent(script);

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

		if (Language.trAll("Surface.DecideJS.XML.Script",name)) {
			script=content;
			final String langName=Language.trAllAttribute("Surface.DecideJS.XML.Script.Language",node);
			if (Language.trAll("Surface.DecideJS.XML.Script.Java",langName)) mode=ScriptMode.Java;
			if (Language.trAll("Surface.DecideJS.XML.Script.Javascript",langName)) mode=ScriptMode.Javascript;
			return null;
		}

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
		return connectionsIn.toArray(new ModelElementEdge[0]);
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
		return connectionsOut.toArray(new ModelElementEdge[0]);
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
	public String getHelpPageName() {
		return "ModelElementDecideJS";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		for (ModelElementEdge edge: connectionsOut) {
			descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.DecideJS.Next"),edge);
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
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.duplicate,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Script"),script,newScript->{script=newScript;});
	}
}