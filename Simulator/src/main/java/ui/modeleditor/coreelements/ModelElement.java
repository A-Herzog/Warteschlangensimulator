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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.KeyStroke;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.Simulator;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementDataWithWaitingClients;
import simulator.elements.RunElementSub;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataClients;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import statistics.StatisticsLongRunPerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ElementRendererTools;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimator;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.ClientInfo;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.elements.SimDataBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.parameterseries.ParameterCompareTemplatesDialog;

/**
 * Basisklasse f�r alle Modell-Elemente
 * @author Alexander Herzog
 */
public class ModelElement {
	/**
	 * Maximal zul�ssiger Abstand in x- und in y-Richtung, um einen Mausklick noch einer Ecke zuzuordnen
	 */
	protected static final int MAX_POINT_DELTA=5;

	/**
	 * Modell zu dem dieses Element geh�rt.
	 */
	private EditModel model;

	/**
	 * Zeichenfl�che zu der dieses Element geh�rt.
	 */
	protected final ModelSurface surface;

	/**
	 * ID des Elements
	 * @see #getId()
	 * @see #setId(int)
	 */
	private int id;

	/**
	 * Namen des Elements
	 * @see #getName()
	 * @see #setName(String)
	 */
	private String name;

	/**
	 * Beschreibung f�r das Element
	 * @see #getDescription()
	 * @see #setDescription(String)
	 */
	private String description;

	/**
	 * L�schschutz f�r das Element
	 * @see #isDeleteProtected()
	 * @see #setDeleteProtection(boolean)
	 */
	private boolean deleteProtection;

	/**
	 * Ist das Element momentan selektiert?
	 * @see #isSelected()
	 * @see #setSelected(boolean)
	 */
	private boolean selected;

	/**
	 * Ist das Element momentan �ber eine Bereichsselektion ausgew�hlt?
	 * @see #isSelectedArea()
	 * @see #setSelectedArea(boolean)
	 * @see #setSelectedArea(Rectangle)
	 */
	private boolean selectedArea;

	/**
	 * Liste der Ebenen auf denen dieses Element dargestellt werden soll
	 * @see #getLayers()
	 * @see #isVisibleOnLayer(List, List)
	 */
	private final List<String> layers;

	/**
	 * Konstruktor der Klasse <code>ModelElement</code>
	 * @param model	Modell zu dem diese Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden).
	 * @param surface	Zeichenfl�che zu dem diese Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElement(final EditModel model, final ModelSurface surface) {
		this.surface=surface;
		this.model=(model==null)?new EditModel():model;
		if (surface!=null) id=surface.getNextFreeId();
		setName("");
		setDescription("");
		deleteProtection=false;
		selected=false;
		selectedArea=false;
		layers=new ArrayList<>();
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements �ndert.
	 */
	public void fireChanged() {
		if (surface!=null) {
			surface.fireStateChangeListener();
			surface.fireRedraw();
		}
	}

	/**
	 * <code>ModelSurface</code>-Element, in dem sich dieses Element befindet
	 * @return	Zugeh�riges <code>ModelSurface</code>-Element
	 */
	public ModelSurface getSurface() {
		return surface;
	}

	/**
	 * {@link EditModel}-Element, in dem sich dieses Element befindet
	 * @return	Zugeh�riges {@link EditModel}-Element
	 */
	public EditModel getModel() {
		return model;
	}

	/**
	 * Setzt ein neues {@link EditModel}-Element f�r dieses Element
	 * @param model	Neues zugeh�riges {@link EditModel}-Element
	 */
	public void setModel(final EditModel model) {
		if (model!=null) this.model=model;
	}

	/**
	 * Liefert die ID des Elements innerhalb des <code>ModelElementSurface</code>-Elements.
	 * @return ID des Elements
	 */
	public final int getId() {
		return id;
	}

	/**
	 * Setzt eine neue ID f�r das Element innerhalb des <code>ModelElementSurface</code>-Elements.
	 * @param id Neue ID f�r das Element
	 */
	public final void setId(final int id) {
		this.id=id;
	}

	/**
	 * Liefert den Namen des Elements.
	 * @return	Name des Elements
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Stellt den Namen des Elements ein.
	 * @param name	Name des Elements
	 */
	public void setName(final String name) {
		if (setNameInt(name)) fireChanged();
	}

	/**
	 * Liefert die Beschreibung f�r das Element.
	 * @return	Beschreibung f�r das Element
	 */
	public final String getDescription() {
		return (description==null)?"":description.trim();
	}

	/**
	 * Stellt die Beschreibung f�r das Element ein.
	 * @param description	Beschreibung f�r das Element
	 */
	public final void setDescription(String description) {
		if (description==null) description="";
		description=description.trim();
		if (this.description!=null && this.description.equals(description)) return;
		this.description=description;
	}

	/**
	 * Stellt den Namen des Elements ein, ohne ein Changed-Ereignis auszul�sen.
	 * @param name	Name des Elements
	 * @return	Gibt <code>true</code> zur�ck, wenn der Name tats�chlich ge�ndert wurde (und nicht schon dem bisherigen Namen entspricht).
	 */
	protected boolean setNameInt(String name) {
		if (name==null) name="";
		if (this.name!=null && this.name.equals(name.trim())) return false;
		this.name=name.trim();
		if (this.name.length()>1024) this.name=this.name.substring(0,1024);
		return true;
	}

	/**
	 * Gibt an, ob das Element vor L�schversuchen gesch�tzt werden soll.
	 * @return	L�schschutzstatus
	 */
	public boolean isDeleteProtected() {
		return deleteProtection;
	}

	/**
	 * Stellt ein, ob das Element vor L�schversuchen gesch�tzt werden soll.
	 * @param deleteProtection	L�schschutzstatus
	 */
	public void setDeleteProtection(final boolean deleteProtection) {
		this.deleteProtection=deleteProtection;
	}

	/**
	 * Gibt an, ob das Element momentan ausgew�hlt ist.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element ausgew�hlt ist.
	 */
	public final boolean isSelected() {
		return selected;
	}

	/**
	 * Gibt an, ob das Element momentan als Bereich ausgew�hlt ist.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element ausgew�hlt ist.
	 */
	public final boolean isSelectedArea() {
		return selectedArea;
	}

	/**
	 * Stellt ein, ob das Element momentan selektiert dargestellt werden soll.
	 * @param selected	Gibt an, ob das Element selektiert dargestellt werden soll
	 */
	public final void setSelected(final boolean selected) {
		if (this.selected==selected) return;
		if (!canSelect() && selected) return;
		this.selected=selected;
		fireChanged();
	}

	/**
	 * Stellt ein, ob das Element momentan als bereich-selektiert dargestellt werden soll.
	 * @param selectedArea	Gibt an, ob das Element selektiert dargestellt werden soll
	 */
	public final void setSelectedArea(final boolean selectedArea) {
		if (this.selectedArea==selectedArea) return;
		if (!canSelect() && selectedArea) return;
		this.selectedArea=selectedArea;
		fireChanged();
	}

	/**
	 * Stellt ein, ob das Element momentan als bereich-selektiert dargestellt werden soll.
	 * @param selectedArea	Bereich, der ausgew�hlt werden soll (oder <code>null</code>, wenn nichts ausgew�hlt werden soll)
	 */
	public void setSelectedArea(final Rectangle selectedArea) {
	}

	/**
	 * Zeichenmodus
	 */
	public enum DrawMode {
		/**
		 * Element normal zeichnen (ggf. selektiert)
		 */
		NORMAL,

		/**
		 * Element hervorgehoben zeichnen (m�gliche Selektion kann dabei ignoriert werden)
		 */
		HIGHLIGHTED,

		/**
		 * Element reduziert zeichnen (m�gliche Selektion kann dabei ignoriert werden)
		 */
		GRAYED_OUT
	}

	/**
	 * Zeichenmodus
	 */
	protected DrawMode drawMode=DrawMode.NORMAL;

	/**
	 * Liefert den aktuellen Zeichenmodus.
	 * @return	Aktuelle Zeichenmodus
	 */
	public DrawMode getDrawMode() {
		return drawMode;
	}

	/**
	 * Stellt einen neuen Zeichenmodus ein.
	 * @param drawMode	Neuer Zeichenmodus
	 */
	public void setDrawMode(final DrawMode drawMode) {
		if (drawMode==null) this.drawMode=DrawMode.NORMAL; else this.drawMode=drawMode;
		fireChanged();
	}

	/**
	 * Gibt f�r die Klasse an, ob der Name bei Vergleichen mit einbezogen werden soll.<br>
	 * (F�r normale Elemente sollte hier <code>true</code> zur�ckgegeben werden. Nur f�r Kanten usw. ist <code>false</code> sinnvoll.)
	 * @return	Gibt an, ob der Name des Elements bei Vergleichen mit einbezogen werden soll.
	 */
	protected boolean getEqualsIncludesName() {
		return true;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	public boolean equalsModelElement(final ModelElement element) {
		if (id!=element.id) return false;
		if (getEqualsIncludesName()) {
			if (!name.equals(element.name)) return false;
			if (!description.equals(element.description)) return false;
		}
		if (deleteProtection!=element.deleteProtection) return false;

		if (!Objects.deepEquals(layers,element.layers)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	public void copyDataFrom(final ModelElement element) {
		if (element.id>0) {
			id=element.id;
		} else {
			if (surface!=null) id=surface.getNextFreeId();
		}
		deleteProtection=element.deleteProtection;
		name=element.name;
		description=element.description;
		selected=element.selected;
		drawMode=element.drawMode;

		layers.clear();
		layers.addAll(element.getLayers());
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	public ModelElement clone(final EditModel model, final ModelSurface surface) {
		final ModelElement element=new ModelElement(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	public void initAfterLoadOrClone() {
		fireChanged();
	}

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
	}

	/**
	 * Pr�ft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt innerhalb des Elements befindet
	 */
	public boolean containsPoint(final Point point, final double zoom) {
		return false;
	}

	/**
	 * Gibt an, ob das Element per Drag&amp;Drop verschoben werden kann.
	 * @return	Liefert <code>true</code>, wenn das Element vom Nutzer verschoben werden kann.
	 */
	public boolean isUserMoveable() {
		return false;
	}

	/**
	 * Liefert die obere linke Ecke des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param readOnly	Gibt an, ob der Punkt selbst (<code>true</code>), der dann nicht ver�ndert werden darf, oder eine Kopie (<code>false</code>) geliefert werden soll
	 * @return	Position der oberen linken Ecke oder <code>null</code>, wenn das Element keine obere linke Ecke besitzt.
	 */
	public Point getPosition(final boolean readOnly) {
		return null;
	}

	/**
	 * Mittelpunkt des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param readOnly	Gibt an, ob der Punkt selbst (<code>true</code>), der dann nicht ver�ndert werden darf, oder eine Kopie (<code>false</code>) geliefert werden soll
	 * @return	Mittelpunkt des Elements oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen k�nnen.
	 */
	public Point getMiddlePosition(final boolean readOnly) {
		return getPosition(readOnly);
	}

	/**
	 * Unterer rechter Rand des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @return	Unterer rechter Rand des Elements oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen k�nnen.
	 */
	public Point getLowerRightPosition() {
		return getPosition(false);
	}

	/**
	 * Verbindungspunkt zu anderem Objekt (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param point	Punkt zu dem die Verbindung erfolgen soll
	 * @return	Position, an der Verkn�pfungen zu dem angegebenen Punkt ansetzen k�nnen oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen k�nnen.
	 */
	public Point getConnectionToPosition(final Point point) {
		return getPosition(false);
	}

	/**
	 * Gibt an, ob sich der Punkt im Bereich eines der Anfasspunkte befindet.
	 * @param point	Punkt bei dem gepr�ft werden soll, ob er sich in der N�he eines Anfasspunktes befindet
	 * @return	Gibt -1 zur�ck, wenn sich der Punkt nicht im Bereich eines der Anfasspunkte befindet, sonst einen Wert &ge;0.
	 */
	public int getBorderPointNr(final Point point) {
		int index=0;
		while (true) {
			final Point borderPoint=getBorderPointPosition(index);
			if (borderPoint==null) break;
			if (Math.abs(borderPoint.x-point.x)<=MAX_POINT_DELTA && Math.abs(borderPoint.y-point.y)<=MAX_POINT_DELTA) return index;
			index++;
		}
		return -1;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index au�erhalb des g�ltigen Bereichs liegt
	 */
	public Point getBorderPointPosition(final int index) {
		return null;
	}

	/**
	 * Setzt die Position eines Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @param point	Neue Position des Randpunktes
	 */
	public void setBorderPointPosition(final int index, final Point point) {
	}

	/**
	 * Stellt (wenn m�glich) die Position des Elements ein.
	 * @param point	Position der oberen linken Ecke. Kann von einigen Element-Typen ignoriert werden.
	 */
	public void setPosition(final Point point) {
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	public String getContextMenuElementName() {
		return "";
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return null;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements w�hrend einer angehaltenen Animation ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	public Runnable getPropertiesSemiEditable(final Component owner, final ModelClientData clientData, final ModelSequences sequences) {
		return null;
	}

	/**
	 * F�gt stations-bedingte zus�tzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
	}

	/**
	 * Stellt Statistikdaten basierend auf der Simulation zusammen, um diese in einem Dialog darzustellen
	 * @param simData	Simulationsdaten (Ist immer <code>!=null</code> bzw. wird nicht aufgerufen, wenn keine Simulationsdaten vorhanden sind)
	 * @return	Statistikdaten (kann <code>null</code> sein, wenn f�r das Element keine Statistikdaten angezeigt werden sollen)
	 */
	protected String getAnimationRunTimeStatisticsData(final SimulationData simData) {
		final SimDataBuilder builder=new SimDataBuilder(simData,getId());
		if (builder.results!=null) {
			addInformationToAnimationRunTimeData(builder);
			return builder.results.toString();
		} else {
			return null;
		}
	}

	/**
	 * Stellt Statistikdaten �ber die an der Station wartenden Kunden zusammen.
	 * @param simulator	Simulator um die Animation w�hrend des Auslesens anhalten zu k�nnen
	 * @param model	Statische Simulationsmodelldaten
	 * @param data	Datenobjekt f�r die aktuelle Station
	 * @return	Liste der wartenden Kunden
	 */
	private List<ModelElementAnimationInfoDialog.ClientInfo> getAnimationRunTimeWaitingClientData(final Simulator simulator, final RunModel model, final RunElementDataWithWaitingClients data) {
		final List<RunDataClient> clients=data.getWaitingClients();
		if (clients.size()==0) return new ArrayList<>(); /* Die Gr��e abfragen k�nnen wir auch, ohne die Simulation anzuhalten. */

		final boolean wasPaused=simulator.isPaused();
		if (!wasPaused) simulator.pauseExecutionAndWait();
		try {
			return ModelElementAnimationInfoDialog.ClientInfo.getList(this.model.animationImages,model,clients);
		} finally {
			if (!wasPaused) simulator.resumeExecution();
		}
	}

	/**
	 * Stellt Statistikdaten �ber die an der Station befindlichen Kunden zusammen.
	 * @param simulator	Simulator um die Animation w�hrend des Auslesens anhalten zu k�nnen
	 * @param model	Statische Simulationsmodelldaten
	 * @param clients	Datenobjekt, welche die Liste aller Kunden vorh�lt
	 * @param id	Stations-ID der Station f�r die die Kundenliste ermittelt werden soll
	 * @return	Liste der Kunden
	 */
	private List<ModelElementAnimationInfoDialog.ClientInfo> getAnimationRunTimeAllClientData(final Simulator simulator, final RunModel model, final RunDataClients clients, final int id) {
		final boolean wasPaused=simulator.isPaused();
		if (!wasPaused) simulator.pauseExecutionAndWait();
		try {
			final List<ModelElementAnimationInfoDialog.ClientInfo> list=new ArrayList<>();

			final boolean searchInSub=(model.elementsFast[id] instanceof RunElementSub);

			final List<RunDataClient> clientsList=clients.requestClientsInUseList();
			final int size=clientsList.size();
			for (int i=0;i<size;i++) {
				final RunDataClient client=clientsList.get(i);
				if (client==null) continue;
				final int clientPosition=client.nextStationID;
				/* Kunde an Station */
				if (clientPosition==id) {
					list.add(new ModelElementAnimationInfoDialog.ClientInfo(this.model.animationImages,model,client));
					continue;
				}
				/* Kunde an Station, die in dem betrachteten Untermodell liegt */
				if (searchInSub && clientPosition>=0 && clientPosition<model.elementsFast.length) {
					final RunElement station=model.elementsFast[clientPosition];
					if (station!=null && station.parentId==id) {
						list.add(new ModelElementAnimationInfoDialog.ClientInfo(this.model.animationImages,model,client));
						continue;
					}
				}
			}

			return list;
		} finally {
			if (!wasPaused) simulator.resumeExecution();
		}
	}

	/**
	 * Erstellt einen Men�punkt im Kontextmen� zum Hinzuf�gen eines Animationselements zu diesem Element
	 * @param elements	Animationselemente, die hinzugef�gt werden sollen, wenn der Men�punkt angeklickt wird
	 * @param text	Optionaler Text f�r den Men�punkt (wird hier <code>null</code> �bergeben, so wird der Kontextmen�-Name von <code>element</code> verwendet)
	 * @param parentMenu	�bergeordnetes Men� (Rubrik innerhalb des Kontextmen�s)
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 * @see ModelElement#addVisualizationContextMenuItems(JMenu, Consumer)
	 */
	protected final JMenuItem addVisualizationMenuItem(final ModelElementPosition[] elements, final String text, final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		final ModelElementPosition element=elements[0];
		final JMenuItem item=new JMenuItem((text==null || text.isBlank())?element.getContextMenuElementName():text);
		item.setToolTipText(element.getToolTip());
		final Icon icon=element.getAddElementIcon();
		if (icon!=null) item.setIcon(icon);
		item.addActionListener(e->addElements.accept(elements));
		parentMenu.add(item);
		return item;
	}

	/**
	 * Erstellt einen Men�punkt im Kontextmen� zum Hinzuf�gen einen Laufzeitstatistik-Datenfeldes
	 * @param text	Name f�r den Men�punkt
	 * @param value	Eintrag f�r die Laufzeitstatistik
	 * @param parentMenu	�bergeordnetes Men� (Rubrik innerhalb des Kontextmen�s)
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 * @return	Neuer Men�punkt (schon in <code>parentMenu</code> eingef�gt)
	 * @see ModelElement#addLongRunStatisticsContextMenuItems(JMenu, Consumer)
	 */
	protected final JMenuItem addLongRunStatisticsMenuItem(final String text, final String value, final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		final JMenuItem item=new JMenuItem(text);
		item.addActionListener(e->addLongRunStatistics.accept(value));
		parentMenu.add(item);
		return item;
	}

	/**
	 * Erstellt Panel, welches in ein Men� eingebettet werden kann, welches einen Schieberegler enth�lt
	 * @param title	Titel �ber dem Schieberegler
	 * @param timeBase	Zeitbasis (wird als String an das Callback �bergeben; darf aber auch <code>null</code> sein, dann wird eine leere Zeichenkette �bergeben)
	 * @param value	Aktueller Wert
	 * @param minMaxValue	Mindestwert f�r den maximalen Wert (Maximalwert ist das Maximum dieses Mindestwertes und des doppelten des Startwertes)
	 * @param change	Callback, welches zu Beginn und bei �nderungen aufgerufen wird. Parameter: Timebase als Name und neuer Wert (ist beim initialen Aufruf <code>null</code>). R�ckgabe ist der String f�r den anzuzeigenden Wert.
	 * @return	Panel, welches Slider und Bezeichnungen enth�lt
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderDistribution(final String title, final ModelSurface.TimeBase timeBase, final double value, final int minMaxValue, final Function<Integer,AbstractRealDistribution> change) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));
		panel.setOpaque(false);

		final String timeBaseName=(timeBase==null)?"":("<br>"+Language.tr("ModelDescription.TimeBase")+": "+ModelSurface.getTimeBaseString(timeBase));

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final int intValue=Math.max(1,(int)Math.round(value));
		final int max=Math.max(minMaxValue,intValue*2);
		final JSlider slider=new JSlider(1,max,intValue);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing((int)Math.round(Math.max(1,Math.ceil((max-1)/5.0))));
		final int minor=(int)Math.round(Math.max(1,Math.ceil((max-1)/20.0)));
		slider.setMinorTickSpacing(minor);
		slider.setOpaque(false);
		if (minor==1) slider.setSnapToTicks(true);
		panel.add(slider,BorderLayout.CENTER);
		final AbstractRealDistribution distribution=change.apply(null);
		final String initialLabelText="<html><body>"+DistributionTools.getDistributionName(distribution)+"<br>"+DistributionTools.getDistributionInfo(distribution)+timeBaseName+"</body></html>";
		final JLabel info=new JLabel(initialLabelText);
		panel.add(info,BorderLayout.SOUTH);

		slider.addChangeListener(e->{
			final int newValue=slider.getValue();
			final AbstractRealDistribution newDistribution=change.apply(newValue);
			final String labelText="<html><body>"+DistributionTools.getDistributionName(newDistribution)+"<br>"+DistributionTools.getDistributionInfo(newDistribution)+timeBaseName+"</body></html>";
			info.setText(labelText);
		});

		return panel;
	}

	/**
	 * Erstellt Panel, welches in ein Men� eingebettet werden kann, welches einen Schieberegler zum Einstellen des Mittelwertes einer Verteilung enth�lt.
	 * L�sst sich bei der angegebenen Verteilung der Mittelwert nicht einstellen, so wird <code>null</code> zur�ckgegeben.
	 * @param title	Titel �ber dem Schieberegler
	 * @param timeBase	Zeitbasis (wird als String an das Callback �bergeben; darf aber auch <code>null</code> sein, dann wird eine leere Zeichenkette �bergeben)
	 * @param initialDistribution	Anf�ngliche Verteilung
	 * @param minMaxValue	Mindestwert f�r den maximalen Wert (Maximalwert ist das Maximum dieses Mindestwertes und des doppelten des Startwertes)
	 * @param change	Callback, welches zu Beginn und bei �nderungen aufgerufen wird.
	 * @return	Panel, welches Slider und Bezeichnungen enth�lt (oder <code>null</code>, wenn bei der Verteilung der Mittelwert nicht eingestellt werden kann)
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderDistributionMean(final String title, final ModelSurface.TimeBase timeBase, final AbstractRealDistribution initialDistribution, final int minMaxValue, final Consumer<AbstractRealDistribution> change) {
		if (!DistributionTools.canSetMean(initialDistribution)) return null;

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));
		panel.setOpaque(false);

		final String timeBaseName=(timeBase==null)?"":("<br> "+Language.tr("ModelDescription.TimeBase")+": "+ModelSurface.getTimeBaseString(timeBase));

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final int intValue=Math.max(1,(int)Math.round(DistributionTools.getMean(initialDistribution)));
		final int max=Math.max(minMaxValue,intValue*2);
		final JSlider slider=new JSlider(1,max,intValue);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing((int)Math.round(Math.max(1,Math.ceil((max-1)/5.0))));
		final int minor=(int)Math.round(Math.max(1,Math.ceil((max-1)/20.0)));
		slider.setMinorTickSpacing(minor);
		if (minor==1) slider.setSnapToTicks(true);
		slider.setOpaque(false);
		panel.add(slider,BorderLayout.CENTER);
		final String initialLabelText="<html><body>"+DistributionTools.getDistributionName(initialDistribution)+"<br>"+DistributionTools.getDistributionInfo(initialDistribution)+timeBaseName+"</body></html>";
		final JLabel info=new JLabel(initialLabelText);
		panel.add(info,BorderLayout.SOUTH);

		slider.addChangeListener(e->{
			final int newValue=slider.getValue();
			final AbstractRealDistribution newDistribution=DistributionTools.setMean(initialDistribution,newValue);
			change.accept(newDistribution);
			final String labelText="<html><body>"+DistributionTools.getDistributionName(newDistribution)+"<br>"+DistributionTools.getDistributionInfo(newDistribution)+timeBaseName+"</body></html>";
			info.setText(labelText);
		});

		return panel;
	}

	/**
	 * Erstellt Panel, welches in ein Men� eingebettet werden kann, welches einen Schieberegler enth�lt
	 * @param title	Titel �ber dem Schieberegler
	 * @param value	Aktueller Wert
	 * @param minMaxValue	Mindestwert f�r den maximalen Wert (Maximalwert ist das Maximum dieses Mindestwertes und des doppelten des Startwertes)
	 * @param change	Callback, welches zu Beginn und bei �nderungen aufgerufen wird. Parameter: neuer Wert (ist beim initialen Aufruf <code>null</code>). R�ckgabe ist der String f�r den anzuzeigenden Wert.
	 * @return	Panel, welches Slider und Bezeichnungen enth�lt
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderValue(final String title, final double value, final int minMaxValue, final Function<Integer,String> change) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));
		panel.setOpaque(false);

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final int intValue=Math.max(1,(int)Math.round(value));
		final int max=Math.max(minMaxValue,intValue*2);
		final JSlider slider=new JSlider(1,max,intValue);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing((int)Math.round(Math.max(1,Math.ceil((max-1)/5.0))));
		slider.setOpaque(false);
		final int minor=(int)Math.round(Math.max(1,Math.ceil((max-1)/20.0)));
		slider.setMinorTickSpacing(minor);
		if (minor==1) slider.setSnapToTicks(true);
		panel.add(slider,BorderLayout.CENTER);
		final JLabel info=new JLabel(change.apply(null));
		panel.add(info,BorderLayout.SOUTH);

		slider.addChangeListener(e->{
			final int newValue=slider.getValue();
			info.setText(change.apply(newValue));
		});

		return panel;
	}

	/**
	 * Erstellt Panel, welches in ein Men� eingebettet werden kann, welches einen Schieberegler enth�lt
	 * @param title	Titel �ber dem Schieberegler
	 * @param initialValue	Aktueller Wert
	 * @param change	Callback, welches zu Beginn und bei �nderungen aufgerufen wird.
	 * @return	Panel, welches Slider und Bezeichnungen enth�lt
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderProbability(final String title, final double initialValue, final DoubleConsumer change) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));
		panel.setOpaque(false);

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final JSlider slider=new JSlider(0,1000,(int)Math.round(initialValue*1000));
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(200);
		slider.setMinorTickSpacing(20);
		slider.setOpaque(false);
		Hashtable<Integer,JComponent> labels=new Hashtable<>();
		for (int i=0;i<=5;i++) labels.put(i*200,new JLabel(NumberTools.formatPercent(i/5.0)));
		slider.setLabelTable(labels);
		panel.add(slider,BorderLayout.CENTER);
		final JLabel info=new JLabel(NumberTools.formatPercent(initialValue));
		panel.add(info,BorderLayout.SOUTH);

		slider.addChangeListener(e->{
			final double newValue=slider.getValue()/1000.0;
			change.accept(newValue);
			info.setText(NumberTools.formatPercent(newValue));
		});

		return panel;
	}

	/**
	 * Erstellte optional weitere Men�punkte (in Form von Panels),
	 * die das direkte Bearbeiten von Einstellungen aus dem
	 * Kontextmen� heraus erlauben.
	 * @return	Array mit Panels (Array kann leer oder <code>null</code> sein; auch Eintr�ge d�rfen <code>null</code> sein)
	 */
	protected JPanel[] addCustomSettingsToContextMenu() {
		return null;
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 */
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
	}

	/**
	 * F�gt optionale Men�punkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmen� ein
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param surfacePanel	Zeichenfl�che
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmen� angezeigt werden soll.
	 * @return	Icon zur Beschriftung des Elements im Kontextmen� oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	public Icon buildIcon() {
		return null;
	}

	/**
	 * Gibt an, ob zu diesem Element ein Simulationdaten-Dialog angezeigt werden kann
	 * @param simData	Simulationsdatenobjekt (kann <code>null</code> sein, dann liefert diese Funktion auf jeden Fall <code>false</code>)
	 * @return	Gibt <code>true</code> zur�ck, wenn momentan ein Simulationdaten-Dialog sinnvoll angezeigt werden kann
	 * @see ModelElement#showElementAnimationStatisticsData(Component, SimulationData)
	 */
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		if (simData==null) return false;
		final String info=getAnimationRunTimeStatisticsData(simData);
		return (info!=null && !info.isBlank());
	}

	/**
	 * Zeigt den Simulationdaten-Dialog zu diesem Element an
	 * @param owner	�bergeordnetes Element
	 * @param simData	Simulationsdatenobjekt
	 * @see ModelElement#hasAnimationStatisticsData(SimulationData)
	 */
	public void showElementAnimationStatisticsData(final Component owner, final SimulationData simData) {
		if (simData==null) return;
		final int id=getId();

		Supplier<List<ClientInfo>> clientWaitingInfo=null;
		Supplier<List<ClientInfo>> clientAllInfo=null;
		Function<Long,RunDataClient> getRealClient=null;
		final RunElement element=simData.runModel.elements.get(id);

		if (element!=null) {
			/* Liste der wartenden Kunden */
			final RunElementData data=element.getData(simData);
			if (data instanceof RunElementDataWithWaitingClients) clientWaitingInfo=()->getAnimationRunTimeWaitingClientData(simData.simulator,simData.runModel,(RunElementDataWithWaitingClients)data);

			/* Liste aller Kunden */
			clientAllInfo=()->getAnimationRunTimeAllClientData(simData.simulator,simData.runModel,simData.runData.clients,id);

			/* Schreibzugriff auf das reale Kundenobjekt */
			if (simData.simulator.isPaused()) {
				getRealClient=nr->{
					final List<RunDataClient> clientsList=simData.runData.clients.requestClientsInUseList();
					final int size=clientsList.size();
					for (int i=0;i<size;i++) if (clientsList.get(i).clientNumber==nr) return clientsList.get(i);
					return null;
				};
			}
		}

		new ModelElementAnimationInfoDialog(owner,getContextMenuElementName()+" (id="+id+")",simData.runModel,model.animationImages,()->getAnimationRunTimeStatisticsData(simData),clientWaitingInfo,clientAllInfo,getRealClient);
	}

	/**
	 * Zeigt einen Dialog zur Bearbeitung eines Haltepunktes f�r die aktuelle Station an.
	 * @param owner	�bergeordnetes Element
	 * @param simData	Simulationsdatenobjekt
	 * @param animator	Animationssystem-Objekt
	 */
	private void showBreakPointDialog(final Component owner, final SimulationData simData, final ModelSurfaceAnimator animator) {
		if (simData==null || animator==null) return;
		final int id=getId();

		final ModelBreakPointDialog dialog=new ModelBreakPointDialog(owner,model,simData,true,id,animator.getBreakPoint(id));
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			animator.setBreakPoint(id,dialog.getBreakPoint());
		}
	}

	/**
	 * Bietet die M�glichkeit unter dem Men�punkt f�r den Simulationdaten-Dialog noch weitere Men�punkte anzuzeigen.
	 * @param owner	�bergeordnetes Element
	 * @param menu	Popup-Men� in das die weiteren Eintr�ge eingef�gt werden k�nnen
	 * @param simData	Simulationsdatenobjekt
	 */
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
	}

	/**
	 * F�gt ein QuickFix-Untermen� oder QuickFix-Men�punkte selbst zu einem Men� hinzu
	 * @param menu	Men� zu dem das Untermen� oder die Men�punkte hinzugef�gt werden sollen
	 * @param asSubMenu	Nur Men�punkte (<code>false</code>) oder ein Men�punkt, der dann die Punkte als Untermen� enth�lt, (<code>true</code>)
	 */
	protected void addQuickFix(final JPopupMenu menu, final boolean asSubMenu) {}

	/**
	 * Erstellt ein Panel in dem das Element und ein Infotext angezeigt werden.
	 * (f�r die Einbettung in ein Kontextmen�).
	 * @return	Panel in dem das Element und ein Infotext angezeigt werden
	 * @see #showContextMenu(Component, Point, boolean, boolean, boolean, ModelSurfacePanel, Consumer, ModelClientData, ModelSequences, SimulationData, ModelSurfaceAnimator)
	 */
	public JComponent getElementSymbol() {
		final String info="<html><body>"+getContextMenuElementName()+(getId()>0?("<br><b>id="+getId()+"</b>"):"")+"</body></html>";

		if (this instanceof ModelElementPosition) {
			return ElementRendererTools.getElementRenderer((ModelElementPosition)this,info,false,null,200,100);
		}

		final JLabel label=new JLabel(info);
		final Icon icon=buildIcon();
		if (icon!=null) label.setIcon(icon);
		return label;
	}

	/**
	 * Sollen an der Station Haltepunkte gesetzt werden k�nnen?
	 * @return	Sollen an der Station Haltepunkte gesetzt werden k�nnen?
	 */
	protected boolean canHandleBreakPoints() {
		return false;
	}

	/**
	 * Zeigt as Kontextmen� zu dem Element an einer bestimmten Stelle auf dem Bildschirm an
	 * @param invoker	Aufrufendes Element (zu dem die Position des Kontextmen�s relativ ist)
	 * @param point	Position, an der das Men� angezeigt werden soll
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 * @param allowEditOnReadOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so kann auch im Read-Only-Modus �ber das Kontextmen� die Reihenfolge der Elemente variiert werden. Au�erdem wird der Bearbeiten-Dialog im Nicht-Read-Only-Modus aufgerufen.
	 * @param nextStationOnly	Soll das vollst�ndige Men� angezeigt werden (<code>false</code>) oder nur das Men� zur Auswahl einer Folgestation (<code>true</code>), welches sonst Teil des vollst�ndigen Men�s ist
	 * @param surfacePanel	Zeichenfl�che
	 * @param buildParameterSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @param simData	Simulationsdaten (w�hrend der Animation, sonst <code>null</code>)
	 * @param animator	Animationssystem-Objekt
	 */
	public final void showContextMenu(final Component invoker, final Point point, final boolean readOnly, final boolean allowEditOnReadOnly, final boolean nextStationOnly, final ModelSurfacePanel surfacePanel, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildParameterSeries, final ModelClientData clientData, final ModelSequences sequences, final SimulationData simData, final ModelSurfaceAnimator animator) {
		/* Nur das Folgestation-Men� anzeigen? */
		if (nextStationOnly) {
			final Consumer<ModelElementBox> addNextStation=e->surfacePanel.startAddElement((ModelElementBox)this,e);
			final JMenu menu=new JMenu();
			addNextStationContextMenuItems(menu,addNextStation);
			final int count=menu.getMenuComponentCount();
			if (count>0) {
				final JPopupMenu popupMenu=new JPopupMenu();
				for (int i=0;i<count;i++) popupMenu.add(menu.getMenuComponent(0));
				popupMenu.show(invoker,point.x,point.y);
				return;
			}
		}

		final JPopupMenu popupMenu=new JPopupMenu();
		JMenu sub;
		JMenuItem item;
		Icon icon;

		/* Darstellung des Elements */
		final JPanel panel=new JPanel(new BorderLayout());
		final JComponent symbol=getElementSymbol();
		symbol.setOpaque(false);
		panel.add(symbol,BorderLayout.WEST);
		panel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		panel.setOpaque(false);
		popupMenu.add(panel);
		/* F�hrt zu falscher Anordnung, wenn weitere Panels im Men� vorhanden sind: popupMenu.add(label); */

		popupMenu.addSeparator();

		/* Einstellungen (z.B. per Slider) direkt �ber das Kontextmen� vornehmen */
		if (!readOnly) {
			final JPanel[] panelItems=addCustomSettingsToContextMenu();
			if (panelItems!=null) for (JPanel panelItem: panelItems) if (panelItem!=null) {
				popupMenu.add(panelItem);
				popupMenu.addSeparator();
			}
		}

		/* Fehlerpr�fung */
		if (!readOnly) addQuickFix(popupMenu,true);

		/* Elementeigenschaften w�hrend angehaltener Animation bearbeiten */
		Runnable propertiesRunnable=null;
		if (simData!=null && simData.simulator.isPaused()) {
			propertiesRunnable=getPropertiesSemiEditable(invoker,clientData,sequences);
		}

		/* Einstellungen */
		if (propertiesRunnable==null) propertiesRunnable=getProperties(invoker,readOnly && !allowEditOnReadOnly,clientData,sequences);
		if (propertiesRunnable!=null) {
			final Runnable propertiesRunnableFinal=propertiesRunnable;
			item=new JMenuItem(Language.tr("Surface.PopupMenu.Properties"));
			item.setFont(item.getFont().deriveFont(Font.BOLD));
			item.addActionListener(e->propertiesRunnableFinal.run());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,ActionEvent.CTRL_MASK));
			item.setIcon(Images.MODELEDITOR_ELEMENT_PROPERTIES.getIcon());
			popupMenu.add(item);
		}

		/* Hilfe */
		item=new JMenuItem(Language.tr("Surface.PopupMenu.Help"));
		Component c=invoker;
		while (c!=null && !(c instanceof Container)) c=c.getParent();
		final Container container=(Container)c;
		item.addActionListener(e->Help.topicModal(container,getHelpPageName()));
		item.setIcon(Images.HELP.getIcon());
		popupMenu.add(item);

		if (!readOnly) {
			JMenu menu;
			/* Visualisierungen hinzuf�gen */
			menu=new JMenu(Language.tr("Surface.Popup.AddVisualization"));
			final Consumer<ModelElementPosition[]> addElements=elements->{
				final ModelElementPosition element=elements[0];
				final ModelElementPosition[] additions=Stream.of(elements).skip(1).toArray(ModelElementPosition[]::new);
				surfacePanel.startAddElementMulti(element,additions);
			};

			addVisualizationContextMenuItems(menu,addElements);
			if (menu.getItemCount()>0) {
				ModelSurfacePanel.sortMenu(menu);
				menu.setIcon(Images.MODELEDITOR_ELEMENT_ADD_VISUALIZATION.getIcon());
				popupMenu.add(menu);
			}

			/* Laufzeitstatistik hinzuf�gen */
			menu=new JMenu(Language.tr("Surface.Popup.AddLongRunStatistics"));
			final Consumer<String> addLongRunStatistics=text->{
				final List<ModelLongRunStatisticsElement> list=getModel().longRunStatistics.getData();
				for (ModelLongRunStatisticsElement element: list) {
					if (element.expression.equals(text)) {
						MsgBox.info(invoker,Language.tr("Surface.Popup.AddLongRunStatistics.Error.Title"),String.format(Language.tr("Surface.Popup.AddLongRunStatistics.Error.Info"),text));
						return;
					}
				}
				list.add(new ModelLongRunStatisticsElement(text,StatisticsLongRunPerformanceIndicator.Mode.MODE_AVERAGE));
				MsgBox.info(invoker,Language.tr("Surface.Popup.AddLongRunStatistics.Success.Title"),String.format(Language.tr("Surface.Popup.AddLongRunStatistics.Success.Info"),text));
			};
			addLongRunStatisticsContextMenuItems(menu,addLongRunStatistics);
			if (menu.getItemCount()>0) {
				menu.setIcon(Images.MODELEDITOR_ELEMENT_ADD_LONG_RUN_STATISTICS.getIcon());
				popupMenu.add(menu);
			}

			/* Folgestationen */
			if (this instanceof ModelElementBox) {
				menu=new JMenu(Language.tr("Surface.Popup.AddNextStation"));
				final Consumer<ModelElementBox> addNextStation=e->surfacePanel.startAddElement((ModelElementBox)this,e);
				addNextStationContextMenuItems(menu,addNextStation);
				if (menu.getItemCount()>0) {
					if (!nextStationOnly) {
						menu.insert(item=new JMenuItem("<html><body><span style=\"font-size:90%\">"+Language.tr("Surface.Popup.AddNextStation.Info")+"</span></body></html>"),0);
						item.setEnabled(false);
						menu.insertSeparator(1);
					}
					menu.setIcon(Images.MODELEDITOR_ELEMENT_NEXT_STATIONS.getIcon());
					popupMenu.add(menu);
				}
			}

			/* Parameterreihenfunktion starten */
			addParameterSeriesMenuItem(popupMenu,buildParameterSeries);
		}

		/* Simulationsdaten w�hrend der Animation */
		if (hasAnimationStatisticsData(simData)) {
			item=new JMenuItem(Language.tr("Surface.PopupMenu.SimulationStatisticsData"));
			item.setFont(item.getFont().deriveFont(Font.BOLD));
			item.addActionListener(e->showElementAnimationStatisticsData(invoker,simData));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,ActionEvent.SHIFT_MASK+ActionEvent.CTRL_MASK));
			item.setIcon(Images.SIMULATION.getIcon());
			popupMenu.add(item);
			storeElementAnimationStatisticsData(invoker,popupMenu,simData);
		}

		/* Haltepunkte konfigurieren */
		if (simData!=null && animator!=null && canHandleBreakPoints()) {

			item=new JMenuItem(Language.tr("Surface.PopupMenu.BreakPoint"));
			item.addActionListener(e->showBreakPointDialog(invoker,simData,animator));
			item.setIcon(Images.ANIMATION_PAUSE.getIcon());
			popupMenu.add(item);
		}

		/* Benutzerdefiniert Eintr�ge */
		addContextMenuItems(invoker,popupMenu,surfacePanel,point,readOnly);

		if (popupMenu.getComponentCount()>0) {
			c=popupMenu.getComponent(popupMenu.getComponentCount()-1);
			if (c!=null && !(c instanceof JPopupMenu.Separator)) popupMenu.addSeparator();
		}

		/* Kopieren, Ausschneiden, L�schen, L�schschutz, Anordnung */

		if (canCopy()) {

			item=new JMenuItem(Language.tr("Dialog.Button.Copy"));
			/* item.setEnabled(!readOnly); */
			item.addActionListener(e->{surface.setSelectedElement(ModelElement.this); surface.fireRequestCopy();});
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
			item.setIcon(Images.EDIT_COPY.getIcon());
			popupMenu.add(item);

			item=new JMenuItem(Language.tr("Dialog.Button.Cut"));
			item.setEnabled(!readOnly);
			item.addActionListener(e->{surface.setSelectedElement(ModelElement.this); surface.fireRequestCut();});
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK));
			item.setIcon(Images.EDIT_CUT.getIcon());
			popupMenu.add(item);

		}

		if (canDelete()) {

			item=new JMenuItem(Language.tr("Dialog.Button.Delete"));
			item.setEnabled(!readOnly && !deleteProtection);
			item.addActionListener(e->surface.remove(ModelElement.this));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
			item.setIcon(Images.EDIT_DELETE.getIcon());
			popupMenu.add(item);

			if (surface.canDeleteAndCloseGap(ModelElement.this)) {
				item=new JMenuItem(Language.tr("Dialog.Button.DeleteAndCloseGap"));
				item.setEnabled(!readOnly && !deleteProtection);
				item.addActionListener(e->surface.removeAndCloseGap(ModelElement.this));
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,InputEvent.SHIFT_DOWN_MASK));
				item.setIcon(Images.EDIT_EDGES_ADD.getIcon());
				popupMenu.add(item);
			}

			if (canSetDeleteProtection()) {
				final JCheckBoxMenuItem itemCheck=new JCheckBoxMenuItem(Language.tr("Dialog.Button.DeleteProtection"));
				itemCheck.setSelected(deleteProtection);
				itemCheck.setEnabled(!readOnly);
				itemCheck.addActionListener(e->deleteProtection=!deleteProtection);
				if (deleteProtection) {
					icon=Images.GENERAL_LOCK_CLOSED.getIcon();
				} else {
					icon=Images.GENERAL_LOCK_OPEN.getIcon();
				}
				if (icon!=null) itemCheck.setIcon(icon);
				popupMenu.add(itemCheck);
			}
		}

		if (canArrange()) {

			popupMenu.addSeparator();

			popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.Arrange")));

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveToFront"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener(e->surface.moveElementToFront(ModelElement.this,true));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,ActionEvent.CTRL_MASK));
			item.setIcon(Images.MOVE_FRONT.getIcon());
			sub.add(item);

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveForwards"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener(e->surface.moveElementToFront(ModelElement.this,false));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0));
			item.setIcon(Images.MOVE_FRONT_STEP.getIcon());
			sub.add(item);

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveBackwards"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener(e->surface.moveElementToBack(ModelElement.this,false));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0));
			item.setIcon(Images.MOVE_BACK_STEP.getIcon());
			sub.add(item);

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveToBack"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener(e->surface.moveElementToBack(ModelElement.this,true));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,ActionEvent.CTRL_MASK));
			item.setIcon(Images.MOVE_BACK.getIcon());
			sub.add(item);

			if (!surface.getLayers().isEmpty()) {
				popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.Layers")));
				sub.setIcon(Images.EDIT_LAYERS.getIcon());
				for (final String layer: surface.getLayers()) {
					final JCheckBoxMenuItem check=new JCheckBoxMenuItem(layer);
					final boolean onLayer=layers.isEmpty() || layers.contains(layer);
					check.setSelected(onLayer);
					if (onLayer) {
						check.addActionListener(e->{
							if (layers.isEmpty()) {
								layers.addAll(surface.getLayers());
								layers.remove(layer);
							} else {
								layers.remove(layer);
							}
							fireChanged();
						});
					} else {
						check.addActionListener(e->{
							layers.add(layer);
							fireChanged();
						});
					}
					sub.add(check);
				}
			}

		}

		/* Popupmen� anzeigen */

		popupMenu.show(invoker,point.x,point.y);
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	public void removeNotify() {
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 * @param element	Element, das entfernt wird
	 */
	public void removeConnectionNotify(final ModelElement element) {
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	public String[] getXMLNodeNames() {
		return new String[]{getClass().getName()};
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		if (!name.isEmpty()) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.Element.Name"));
			node.appendChild(sub);
			sub.setTextContent(name);
		}
		if (!description.isEmpty()) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.ModelElementDescription"));
			node.appendChild(sub);
			sub.setTextContent(description);
		}

		for (String layer: layers) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.ModelElementLayer"));
			node.appendChild(sub);
			sub.setTextContent(layer);
		}
	}

	/**
	 * Speichert das Modell-Element in einem xml-Knoten
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		Element node=doc.createElement(getXMLNodeNames()[0]);
		parent.appendChild(node);

		node.setAttribute(Language.trPrimary("Surface.XML.Element.id"),""+id);
		if (deleteProtection) node.setAttribute(Language.trPrimary("Surface.XML.Element.DeleteProtection"),"1");
		addPropertiesDataToXML(doc,node);
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	protected String loadProperty(final String name, final String content, final Element node) {
		if (Language.trAll("Surface.XML.Element.Name",name) && content!=null) {
			this.name=content.trim();
			return null;
		}

		if (Language.trAll("Surface.XML.ModelElementDescription",name) && content!=null) {
			this.description=content.trim();
			return null;
		}

		if (Language.trAll("Surface.XML.ModelElementLayer",name) && content!=null && !content.isBlank()) {
			layers.add(content);
		}

		return null;
	}

	/**
	 * Erm�glicht das Laden von Eigenschaften aus dem Haupt-Knoten des xml-Elements.
	 * @param node	xml-Element deren Eigenschaften verarbeitet werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	protected String loadPropertiesFromMainNode(final Element node) {
		return null;
	}

	/**
	 * Versucht die Daten des Modell-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Modell-Element beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	public String loadFromXML(final Element node) {
		final Integer I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Element.id",node));
		if (I==null || I<1) return String.format(Language.tr("Surface.Element.InvalidID"),node.getTagName());
		id=I;

		if (canSetDeleteProtection()) {
			final String s=Language.trAllAttribute("Surface.XML.Element.DeleteProtection",node);
			if (!s.isBlank() && !s.equals("0")) deleteProtection=true;
		}

		String error=loadPropertiesFromMainNode(node);
		if (error!=null) return error;

		final NodeList l=node.getChildNodes();
		final int size=l.getLength();
		for (int i=0; i<size;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			Element e=(Element)sub;
			error=loadProperty(e.getNodeName(),e.getTextContent(),e);
			if (error!=null) return error;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element gel�scht werden darf.<br>
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=L�schen zul�ssig) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element gel�scht werden darf.
	 */
	public boolean canDelete() {
		return true;
	}

	/**
	 * Gibt an, ob das Element kopiert werden darf.<br>
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=Kopieren zul�ssig) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element kopiert werden darf.
	 */
	public boolean canCopy() {
		return true;
	}

	/**
	 * Gibt an, ob das Element selektiert werden darf.<br>
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=Selektieren zul�ssig) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element selektiert werden darf.
	 */
	public boolean canSelect() {
		return true;
	}

	/**
	 * Gibt an, ob das Element nach vorne/nach hinten verschoben werden darf.<br>
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=Verschieben zul�ssig) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element nach vorne/nach hinten verschoben werden darf.
	 */
	public boolean canArrange() {
		return true;
	}

	/**
	 * Gibt an, ob es m�glich sein soll, das Element vor L�schversuchen zu sch�tzen.<br>
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=L�schschutz-Status m�glich) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn es m�glich sein soll, das Element vor L�schversuchen zu sch�tzen.
	 * @see ModelElement#isDeleteProtected()
	 * @see ModelElement#setDeleteProtection(boolean)
	 */
	protected boolean canSetDeleteProtection() {
		return true;
	}

	/**
	 * Wird vom Surface aufgerufen direkt nach dem das Element zu der Surface-Elementen-Liste hinzugef�gt wurde.
	 */
	public void addedToSurface() {
	}

	/**
	 * Darf dieses Element zu einem Untermodell hinzugef�gt werden?<br>
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=Einf�gen in Untermodell zul�ssig) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element in ein Untermodell eingef�gt werden darf.
	 */
	public boolean canAddToSub() {
		return true;
	}

	/**
	 * Darf dieses Element in das Diagramm-Dashboard eingef�gt werden?
	 * Diese Methode muss nur �berschrieben werden, wenn dieses Standardverhalten (=Einf�gen in Diagramm-Dashboard nicht zul�ssig) f�r ein besonderes Element ver�ndert werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element in das Diagramm-Dashboard eingef�gt werden darf.
	 */
	public boolean isVisualOnly() {
		return false;
	}

	/**
	 * Liefert den Namen (ohne Pfad und ohne ".html"-Erweiterung) der zu diesem Element geh�rigen Hilfeseite
	 * @return	Name der zugeh�rigen Hilfeseite (oder <code>null</code>, wenn zu diesem Element keine Hilfe vorhanden ist)
	 */
	public String getHelpPageName() {
		return null;
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
	}

	/**
	 * Liefert die Liste der Ebenen auf denen dieses Element dargestellt werden soll.<br>
	 * Diese Liste kann leer (aber nicht <code>null</code>) sein; in diesem Fall ist das
	 * Element auf allen Ebenen sichtbar.
	 * @return	Liste der Ebenen, auf denen sich dieses Element befindet (Originalliste, kann direkt ver�ndert werden)
	 */
	public List<String> getLayers() {
		return layers;
	}

	/**
	 * Pr�ft, ob das Element momentan sichtbar sein soll.
	 * @param layers	Liste mit allen verf�gbaren Ebenen (siehe {@link ModelSurface#getLayers()}
	 * @param visibleLayers	Liste der sichtbaren Ebenen (siehe {@link ModelSurface#getVisibleLayers()}
	 * @return	Gibt an, ob das Element moment sichtbar ist. (Achtung: Funktioniert nicht f�r Kanten; deren Sichtbarkeit muss relativ zu den Elementen gepr�ft werden)
	 * @see ModelSurface#isVisibleOnLayer(ModelElement)
	 * @see ModelSurface#getLayers()
	 * @see ModelSurface#getVisibleLayers()
	 */
	public boolean isVisibleOnLayer(final List<String> layers, final List<String> visibleLayers) {
		if (layers.isEmpty() || this.layers.isEmpty() || visibleLayers.isEmpty()) return true;

		boolean isOnLayer=false; /* Ist das Element zumindest auf irgendeinem Layer? */
		for (String layer: this.layers) {
			if (visibleLayers.contains(layer)) return true;
			if (layers.contains(layer)) isOnLayer=true;
		}
		if (!isOnLayer) return true; /* Wenn das Element nur Layer kennt, die es gar nicht gibt, ist das gleichwertig dazu, dass das Element nicht gelistet ist. */

		return false;
	}

	@Override
	public String toString() {
		return super.toString()+" (id="+id+")";
	}

	/**
	 * Sucht einen Text in den Daten des Elements.
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher) {
		if (searcher.isTestIDs()) searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.ID"),id);

		/* Name */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Name"),name,newName->setName(newName));

		/* Beschreibung */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Description"),description,newDescriptiong->setDescription(newDescriptiong));
	}
}