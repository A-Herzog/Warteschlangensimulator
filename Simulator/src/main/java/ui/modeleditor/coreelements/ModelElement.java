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
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import statistics.StatisticsLongRunPerformanceIndicator;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.elements.SimDataBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.parameterseries.ParameterCompareTemplatesDialog;

/**
 * Basisklasse für alle Modell-Elemente
 * @author Alexander Herzog
 */
public class ModelElement {
	/**
	 * Maximal zulässiger Abstand in x- und in y-Richtung, um einen Mausklick noch einer Ecke zuzuordnen
	 */
	protected final static int MAX_POINT_DELTA=5;

	/**
	 * Modell zu dem dieses Element gehört.
	 */
	private EditModel model;

	/**
	 * Zeichenfläche zu der dieses Element gehört.
	 */
	protected final ModelSurface surface;

	private int id;
	private String name;
	private String description;
	private boolean deleteProtection;
	private boolean selected;
	private boolean selectedArea;

	private final List<String> layers;

	/**
	 * Konstruktor der Klasse <code>ModelElement</code>
	 * @param model	Modell zu dem diese Element gehören soll (kann später nicht mehr geändert werden).
	 * @param surface	Zeichenfläche zu dem diese Element gehören soll (kann später nicht mehr geändert werden)
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
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	public void fireChanged() {
		if (surface!=null) {
			surface.fireStateChangeListener();
			surface.fireRedraw();
		}
	}

	/**
	 * <code>ModelSurface</code>-Element, in dem sich dieses Element befindet
	 * @return	Zugehöriges <code>ModelSurface</code>-Element
	 */
	public ModelSurface getSurface() {
		return surface;
	}

	/**
	 * {@link EditModel}-Element, in dem sich dieses Element befindet
	 * @return	Zugehöriges {@link EditModel}-Element
	 */
	public EditModel getModel() {
		return model;
	}

	/**
	 * Setzt ein neues {@link EditModel}-Element für dieses Element
	 * @param model	Neues zugehöriges {@link EditModel}-Element
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
	 * Setzt eine neue ID für das Element innerhalb des <code>ModelElementSurface</code>-Elements.
	 * @param id Neue ID für das Element
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
	 * Liefert die Beschreibung für das Element.
	 * @return	Beschreibung für das Element
	 */
	public final String getDescription() {
		return (description==null)?"":description.trim();
	}

	/**
	 * Stellt die Beschreibung für das Element ein.
	 * @param description	Beschreibung für das Element
	 */
	public final void setDescription(String description) {
		if (description==null) description="";
		description=description.trim();
		if (this.description!=null && this.description.equals(description)) return;
		this.description=description;
	}

	/**
	 * Stellt den Namen des Elements ein, ohne ein Changed-Ereignis auszulösen.
	 * @param name	Name des Elements
	 * @return	Gibt <code>true</code> zurück, wenn der Name tatsächlich geändert wurde (und nicht schon dem bisherigen Namen entspricht).
	 */
	protected boolean setNameInt(String name) {
		if (name==null) name="";
		if (this.name!=null && this.name.equals(name.trim())) return false;
		this.name=name.trim();
		if (this.name.length()>1024) this.name=this.name.substring(0,1024);
		return true;
	}

	/**
	 * Gibt an, ob das Element vor Löschversuchen geschützt werden soll.
	 * @return	Löschschutzstatus
	 */
	public boolean isDeleteProtected() {
		return deleteProtection;
	}

	/**
	 * Stellt ein, ob das Element vor Löschversuchen geschützt werden soll.
	 * @param deleteProtection	Löschschutzstatus
	 */
	public void setDeleteProtection(final boolean deleteProtection) {
		this.deleteProtection=deleteProtection;
	}

	/**
	 * Gibt an, ob das Element momentan ausgewählt ist.
	 * @return	Gibt <code>true</code> zurück, wenn das Element ausgewählt ist.
	 */
	public final boolean isSelected() {
		return selected;
	}

	/**
	 * Gibt an, ob das Element momentan als Bereich ausgewählt ist.
	 * @return	Gibt <code>true</code> zurück, wenn das Element ausgewählt ist.
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
	 * @param selectedArea	Bereich, der ausgewählt werden soll (oder <code>null</code>, wenn nichts ausgewählt werden soll)
	 */
	public void setSelectedArea(final Rectangle selectedArea) {
	}

	/**
	 * Gibt für die Klasse an, ob der Name bei Vergleichen mit einbezogen werden soll.<br>
	 * (Für normale Elemente sollte hier <code>true</code> zurückgegeben werden. Nur für Kanten usw. ist <code>false</code> sinnvoll.)
	 * @return	Gibt an, ob der Name des Elements bei Vergleichen mit einbezogen werden soll.
	 */
	protected boolean getEqualsIncludesName() {
		return true;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
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
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
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

		layers.clear();
		layers.addAll(element.getLayers());
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
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
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
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
	 * @param readOnly	Gibt an, ob der Punkt selbst (<code>true</code>), der dann nicht verändert werden darf, oder eine Kopie (<code>false</code>) geliefert werden soll
	 * @return	Position der oberen linken Ecke oder <code>null</code>, wenn das Element keine obere linke Ecke besitzt.
	 */
	public Point getPosition(final boolean readOnly) {
		return null;
	}

	/**
	 * Mittelpunkt des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param readOnly	Gibt an, ob der Punkt selbst (<code>true</code>), der dann nicht verändert werden darf, oder eine Kopie (<code>false</code>) geliefert werden soll
	 * @return	Mittelpunkt des Elements oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen können.
	 */
	public Point getMiddlePosition(final boolean readOnly) {
		return getPosition(readOnly);
	}

	/**
	 * Unterer rechter Rand des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @return	Unterer rechter Rand des Elements oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen können.
	 */
	public Point getLowerRightPosition() {
		return getPosition(false);
	}

	/**
	 * Verbindungspunkt zu anderem Objekt (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param point	Punkt zu dem die Verbindung erfolgen soll
	 * @return	Position, an der Verknüpfungen zu dem angegebenen Punkt ansetzen können oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen können.
	 */
	public Point getConnectionToPosition(final Point point) {
		return getPosition(false);
	}

	/**
	 * Gibt an, ob sich der Punkt im Bereich eines der Anfasspunkte befindet.
	 * @param point	Punkt bei dem geprüft werden soll, ob er sich in der Nähe eines Anfasspunktes befindet
	 * @return	Gibt -1 zurück, wenn sich der Punkt nicht im Bereich eines der Anfasspunkte befindet, sonst einen Wert &ge;0.
	 */
	public int getBorderPointNr(final Point point) {
		int index=0;
		while (true) {
			final Point borderPoint=getBorderPointPosition(index);
			if (borderPoint==null) break;
			if (FastMath.abs(borderPoint.x-point.x)<=MAX_POINT_DELTA && FastMath.abs(borderPoint.y-point.y)<=MAX_POINT_DELTA) return index;
			index++;
		}
		return -1;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index außerhalb des gültigen Bereichs liegt
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
	 * Stellt (wenn möglich) die Position des Elements ein.
	 * @param point	Position der oberen linken Ecke. Kann von einigen Element-Typen ignoriert werden.
	 */
	public void setPosition(final Point point) {
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	public String getContextMenuElementName() {
		return "";
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return null;
	}

	/**
	 * Fügt stations-bedingte zusätzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
	}

	/**
	 * Stellt Statistikdaten basierend auf der Simulation zusammen, um diese in einem Dialog darzustellen
	 * @param simData	Simulationsdaten (Ist immer <code>!=null</code> bzw. wird nicht aufgerufen, wenn keine Simulationsdaten vorhanden sind)
	 * @return	Statistikdaten (kann <code>null</code> sein, wenn für das Element keine Statistikdaten angezeigt werden sollen)
	 */
	protected synchronized String getAnimationRunTimeStatisticsData(SimulationData simData) {
		final SimDataBuilder builder=new SimDataBuilder(simData,getId());
		if (builder.results!=null) {
			addInformationToAnimationRunTimeData(builder);
			return builder.results.toString();
		} else {
			return null;
		}
	}

	/**
	 * Erstellt einen Menüpunkt im Kontextmenü zum Hinzufügen eines Animationselements zu diesem Element
	 * @param element	Animationselement, das hinzugefügt werden soll, wenn der Menüpunkt angeklickt wird
	 * @param text	Optionaler Text für den Menüpunkt (wird hier <code>null</code> übergeben, so wird der Kontextmenü-Name von <code>element</code> verwendet)
	 * @param parentMenu	Übergeordnetes Menü (Rubrik innerhalb des Kontextmenüs)
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 * @see ModelElement#addVisualizationContextMenuItems(JMenu, Consumer)
	 */
	protected final JMenuItem addVisualizationMenuItem(final ModelElementPosition element, final String text, final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		final JMenuItem item=new JMenuItem((text==null || text.trim().isEmpty())?element.getContextMenuElementName():text);
		item.setToolTipText(element.getToolTip());
		final URL imgURL=element.getAddElementIcon();
		if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
		item.addActionListener(e->addElement.accept(element));
		parentMenu.add(item);
		return item;
	}

	/**
	 * Erstellt einen Menüpunkt im Kontextmenü zum Hinzufügen einen Laufzeitstatistik-Datenfeldes
	 * @param text	Name für den Menüpunkt
	 * @param value	Eintrag für die Laufzeitstatistik
	 * @param parentMenu	Übergeordnetes Menü (Rubrik innerhalb des Kontextmenüs)
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugefügt werden soll
	 * @return	Neuer Menüpunkt (schon in <code>parentMenu</code> eingefügt)
	 * @see ModelElement#addLongRunStatisticsContextMenuItems(JMenu, Consumer)
	 */
	protected final JMenuItem addLongRunStatisticsMenuItem(final String text, final String value, final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		final JMenuItem item=new JMenuItem(text);
		item.addActionListener(e->addLongRunStatistics.accept(value));
		parentMenu.add(item);
		return item;
	}

	/**
	 * Erstellt Panel, welches in ein Menü eingebettet werden kann, welches einen Schieberegler enthält
	 * @param title	Titel über dem Schieberegler
	 * @param timeBase	Zeitbasis (wird als String an das Callback übergeben; darf aber auch <code>null</code> sein, dann wird eine leere Zeichenkette übergeben)
	 * @param value	Aktueller Wert
	 * @param minMaxValue	Mindestwert für den maximalen Wert (Maximalwert ist das Maximum dieses Mindestwertes und des doppelten des Startwertes)
	 * @param change	Callback, welches zu Beginn und bei Änderungen aufgerufen wird. Parameter: Timebase als Name und neuer Wert (ist beim initialen Aufruf <code>null</code>). Rückgabe ist der String für den anzuzeigenden Wert.
	 * @return	Panel, welches Slider und Bezeichnungen enthält
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderDistribution(final String title, final ModelSurface.TimeBase timeBase, final double value, final int minMaxValue, final Function<Integer,AbstractRealDistribution> change) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));

		final String timeBaseName=(timeBase==null)?"":("; "+Language.tr("ModelDescription.TimeBase")+": "+ModelSurface.getTimeBaseString(timeBase));

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final int intValue=Math.max(1,(int)Math.round(value));
		final int max=Math.max(minMaxValue,intValue*2);
		final JSlider slider=new JSlider(1,max,intValue);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing((int)Math.round(Math.max(1,Math.ceil((max-1)/5.0))));
		final int minor=(int)Math.round(Math.max(1,Math.ceil((max-1)/20.0)));
		slider.setMinorTickSpacing(minor);
		if (minor==1) slider.setSnapToTicks(true);
		panel.add(slider,BorderLayout.CENTER);
		final AbstractRealDistribution distribution=change.apply(null);
		final String initialLabelText=DistributionTools.getDistributionName(distribution)+"; "+DistributionTools.getDistributionInfo(distribution)+timeBaseName;
		final JLabel info=new JLabel(initialLabelText);
		panel.add(info,BorderLayout.SOUTH);

		slider.addChangeListener(e->{
			final int newValue=slider.getValue();
			final AbstractRealDistribution newDistribution=change.apply(newValue);
			final String labelText=DistributionTools.getDistributionName(newDistribution)+"; "+DistributionTools.getDistributionInfo(newDistribution)+timeBaseName;
			info.setText(labelText);
		});

		return panel;
	}

	/**
	 * Erstellt Panel, welches in ein Menü eingebettet werden kann, welches einen Schieberegler zum Einstellen des Mittelwertes einer Verteilung enthält.
	 * Lässt sich bei der angegebenen Verteilung der Mittelwert nicht einstellen, so wird <code>null</code> zurückgegeben.
	 * @param title	Titel über dem Schieberegler
	 * @param timeBase	Zeitbasis (wird als String an das Callback übergeben; darf aber auch <code>null</code> sein, dann wird eine leere Zeichenkette übergeben)
	 * @param initialDistribution	Anfängliche Verteilung
	 * @param minMaxValue	Mindestwert für den maximalen Wert (Maximalwert ist das Maximum dieses Mindestwertes und des doppelten des Startwertes)
	 * @param change	Callback, welches zu Beginn und bei Änderungen aufgerufen wird.
	 * @return	Panel, welches Slider und Bezeichnungen enthält (oder <code>null</code>, wenn bei der Verteilung der Mittelwert nicht eingestellt werden kann)
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderDistributionMean(final String title, final ModelSurface.TimeBase timeBase, final AbstractRealDistribution initialDistribution, final int minMaxValue, final Consumer<AbstractRealDistribution> change) {
		if (!DistributionTools.canSetMean(initialDistribution)) return null;

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));

		final String timeBaseName=(timeBase==null)?"":("; "+Language.tr("ModelDescription.TimeBase")+": "+ModelSurface.getTimeBaseString(timeBase));

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
		panel.add(slider,BorderLayout.CENTER);
		final String initialLabelText=DistributionTools.getDistributionName(initialDistribution)+"; "+DistributionTools.getDistributionInfo(initialDistribution)+timeBaseName;
		final JLabel info=new JLabel(initialLabelText);
		panel.add(info,BorderLayout.SOUTH);

		slider.addChangeListener(e->{
			final int newValue=slider.getValue();
			final AbstractRealDistribution newDistribution=DistributionTools.setMean(initialDistribution,newValue);
			change.accept(newDistribution);
			final String labelText=DistributionTools.getDistributionName(newDistribution)+"; "+DistributionTools.getDistributionInfo(newDistribution)+timeBaseName;
			info.setText(labelText);
		});

		return panel;
	}

	/**
	 * Erstellt Panel, welches in ein Menü eingebettet werden kann, welches einen Schieberegler enthält
	 * @param title	Titel über dem Schieberegler
	 * @param value	Aktueller Wert
	 * @param minMaxValue	Mindestwert für den maximalen Wert (Maximalwert ist das Maximum dieses Mindestwertes und des doppelten des Startwertes)
	 * @param change	Callback, welches zu Beginn und bei Änderungen aufgerufen wird. Parameter: neuer Wert (ist beim initialen Aufruf <code>null</code>). Rückgabe ist der String für den anzuzeigenden Wert.
	 * @return	Panel, welches Slider und Bezeichnungen enthält
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderValue(final String title, final double value, final int minMaxValue, final Function<Integer,String> change) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final int intValue=Math.max(1,(int)Math.round(value));
		final int max=Math.max(minMaxValue,intValue*2);
		final JSlider slider=new JSlider(1,max,intValue);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing((int)Math.round(Math.max(1,Math.ceil((max-1)/5.0))));
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
	 * Erstellt Panel, welches in ein Menü eingebettet werden kann, welches einen Schieberegler enthält
	 * @param title	Titel über dem Schieberegler
	 * @param initialValue	Aktueller Wert
	 * @param change	Callback, welches zu Beginn und bei Änderungen aufgerufen wird.
	 * @return	Panel, welches Slider und Bezeichnungen enthält
	 * @see ModelElement#addCustomSettingsToContextMenu()
	 */
	public static final JPanel createContextMenuSliderProbability(final String title, final double initialValue, final Consumer<Double> change) {
		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10,5+32,10,5));

		panel.add(new JLabel("<html><body><b>"+title+"</b></body></html>"),BorderLayout.NORTH);
		final JSlider slider=new JSlider(0,1000,(int)Math.round(initialValue*1000));
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setMajorTickSpacing(200);
		slider.setMinorTickSpacing(20);
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
	 * Erstellte optional weitere Menüpunkte (in Form von Panels),
	 * die das direkte Bearbeiten von Einstellungen aus dem
	 * Kontextmenü heraus erlauben.
	 * @return	Array mit Panels (Array kann leer oder <code>null</code> sein; auch Einträge dürfen <code>null</code> sein)
	 */
	protected JPanel[] addCustomSettingsToContextMenu() {
		return null;
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Laufzeitstatistik hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugefügt werden soll
	 */
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
	}

	/**
	 * Fügt optionale Menüpunkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmenü ein
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmenü angezeigt werden soll.
	 * @return	Icon zur Beschriftung des Elements im Kontextmenü oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	public Icon buildIcon() {
		return null;
	}

	/**
	 * Gibt an, ob zu diesem Element ein Simulationdaten-Dialog angezeigt werden kann
	 * @param simData	Simulationsdatenobjekt (kann <code>null</code> sein, dann liefert diese Funktion auf jeden Fall <code>false</code>)
	 * @return	Gibt <code>true</code> zurück, wenn momentan ein Simulationdaten-Dialog sinnvoll angezeigt werden kann
	 * @see ModelElement#showElementAnimationStatisticsData(Component, SimulationData)
	 */
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		if (simData==null) return false;
		final String info=getAnimationRunTimeStatisticsData(simData);
		return (info!=null && !info.trim().isEmpty());
	}

	/**
	 * Zeigt den Simulationdaten-Dialog zu diesem Element an
	 * @param owner	Übergeordnetes Element
	 * @param simData	Simulationsdatenobjekt
	 * @see ModelElement#hasAnimationStatisticsData(SimulationData)
	 */
	public void showElementAnimationStatisticsData(final Component owner, final SimulationData simData) {
		if (simData==null) return;
		new ModelElementAnimationInfoDialog(owner,getContextMenuElementName()+" (id="+getId()+")",()->getAnimationRunTimeStatisticsData(simData));
	}

	/**
	 * Bietet die Möglichkeit unter dem Menüpunkt für den Simulationdaten-Dialog noch weitere Menüpunkte anzuzeigen.
	 * @param owner	Übergeordnetes Element
	 * @param menu	Popup-Menü in das die weiteren Einträge eingefügt werden können
	 * @param simData	Simulationsdatenobjekt
	 */
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
	}

	/**
	 * Fügt ein QuickFix-Untermenü oder QuickFix-Menüpunkte selbst zu einem Menü hinzu
	 * @param menu	Menü zu dem das Untermenü oder die Menüpunkte hinzugefügt werden sollen
	 * @param asSubMenu	Nur Menüpunkte (<code>false</code>) oder ein Menüpunkt, der dann die Punkte als Untermenü enthält, (<code>true</code>)
	 */
	protected void addQuickFix(final JPopupMenu menu, final boolean asSubMenu) {}

	/**
	 * Zeigt as Kontextmenü zu dem Element an einer bestimmten Stelle auf dem Bildschirm an
	 * @param invoker	Aufrufendes Element (zu dem die Position des Kontextmenüs relativ ist)
	 * @param point	Position, an der das Menü angezeigt werden soll
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @param allowEditOnReadOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so kann auch im Read-Only-Modus über das Kontextmenü die Reihenfolge der Elemente variiert werden. Außerdem wird der Bearbeiten-Dialog im Nicht-Read-Only-Modus aufgerufen.
	 * @param surfacePanel	Zeichenfläche
	 * @param buildParameterSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @param simData	Simulationsdaten (während der Animation, sonst <code>null</code>)
	 */
	public final void showContextMenu(final Component invoker, final Point point, final boolean readOnly, final boolean allowEditOnReadOnly, final ModelSurfacePanel surfacePanel, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildParameterSeries, final ModelClientData clientData, final ModelSequences sequences, final SimulationData simData) {
		final JPopupMenu popupMenu=new JPopupMenu();
		JMenu sub;
		JMenuItem item;
		URL url;

		/* Darstellung des Elements */
		final JLabel label=new JLabel("<html><body>"+getContextMenuElementName()+"<br><b>id="+getId()+"</b></body></html>");
		final Icon icon=buildIcon();
		if (icon!=null) label.setIcon(icon);

		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(label,BorderLayout.WEST);
		panel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
		popupMenu.add(panel);
		/* Führt zu falscher Anordnung, wenn weitere Panels im Menü vorhanden sind: popupMenu.add(label); */

		popupMenu.addSeparator();

		/* Einstellungen (z.B. per Slider) direkt über das Kontextmenü vornehmen */
		if (!readOnly) {
			final JPanel[] panelItems=addCustomSettingsToContextMenu();
			if (panelItems!=null) for (JPanel panelItem: panelItems) if (panelItem!=null) {
				popupMenu.add(panelItem);
				popupMenu.addSeparator();
			}
		}

		/* Fehlerprüfung */
		if (!readOnly) addQuickFix(popupMenu,true);

		/* Einstellungen */
		final Runnable propertiesRunnable=getProperties(invoker,readOnly && !allowEditOnReadOnly,clientData,sequences);
		if (propertiesRunnable!=null) {
			item=new JMenuItem(Language.tr("Surface.PopupMenu.Properties"));
			item.setFont(item.getFont().deriveFont(Font.BOLD));
			item.addActionListener(e->propertiesRunnable.run());
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
			/* Visualisierungen hinzufügen */
			menu=new JMenu(Language.tr("Surface.Popup.AddVisualization"));
			final Consumer<ModelElementPosition> addElement=e->surfacePanel.startAddElement(e);
			addVisualizationContextMenuItems(menu,addElement);
			if (menu.getItemCount()>0) {
				menu.setIcon(Images.MODELEDITOR_ELEMENT_ADD_VISUALIZATION.getIcon());
				popupMenu.add(menu);
			}

			/* Laufzeitstatistik hinzufügen */
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
					menu.setIcon(Images.MODELEDITOR_ELEMENT_NEXT_STATIONS.getIcon());
					popupMenu.add(menu);
				}
			}

			/* Parameterreihenfunktion starten */
			addParameterSeriesMenuItem(popupMenu,buildParameterSeries);
		}

		/* Simulationsdaten während der Animation */
		if (hasAnimationStatisticsData(simData)) {
			item=new JMenuItem(Language.tr("Surface.PopupMenu.SimulationStatisticsData"));
			item.setFont(item.getFont().deriveFont(Font.BOLD));
			item.addActionListener(e->showElementAnimationStatisticsData(invoker,simData));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,ActionEvent.SHIFT_MASK+ActionEvent.CTRL_MASK));
			item.setIcon(Images.SIMULATION.getIcon());
			popupMenu.add(item);
			storeElementAnimationStatisticsData(invoker,popupMenu,simData);
		}

		/* Benutzerdefiniert Einträge */
		addContextMenuItems(invoker,popupMenu,surfacePanel,point,readOnly);

		if (popupMenu.getComponentCount()>0) {
			c=popupMenu.getComponent(popupMenu.getComponentCount()-1);
			if (c!=null && !(c instanceof JPopupMenu.Separator)) popupMenu.addSeparator();
		}

		/* Kopieren, Ausschneiden, Löschen, Löschschutz, Anordnung */

		if (canCopy()) {

			item=new JMenuItem(Language.tr("Dialog.Button.Copy"));
			/* item.setEnabled(!readOnly); */
			item.addActionListener((e)->{surface.setSelectedElement(ModelElement.this); surface.fireRequestCopy();});
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
			item.setIcon(Images.EDIT_COPY.getIcon());
			popupMenu.add(item);

			item=new JMenuItem(Language.tr("Dialog.Button.Cut"));
			item.setEnabled(!readOnly);
			item.addActionListener((e)->{surface.setSelectedElement(ModelElement.this); surface.fireRequestCut();});
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK));
			item.setIcon(Images.EDIT_CUT.getIcon());
			popupMenu.add(item);

		}

		if (canDelete()) {

			item=new JMenuItem(Language.tr("Dialog.Button.Delete"));
			item.setEnabled(!readOnly && !deleteProtection);
			item.addActionListener((e)->surface.remove(ModelElement.this));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
			item.setIcon(Images.EDIT_DELETE.getIcon());
			popupMenu.add(item);

			if (canSetDeleteProtection()) {
				final JCheckBoxMenuItem itemCheck=new JCheckBoxMenuItem(Language.tr("Dialog.Button.DeleteProtection"));
				itemCheck.setSelected(deleteProtection);
				itemCheck.setEnabled(!readOnly);
				itemCheck.addActionListener((e)->deleteProtection=!deleteProtection);
				if (deleteProtection) {
					url=Images.GENERAL_LOCK_CLOSED.getURL();
				} else {
					url=Images.GENERAL_LOCK_OPEN.getURL();
				}
				if (url!=null) itemCheck.setIcon(new ImageIcon(url));
				popupMenu.add(itemCheck);
			}
		}

		if (canArrange()) {

			popupMenu.addSeparator();

			popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.Arrange")));

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveToFront"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener((e)->surface.moveElementToFront(ModelElement.this,true));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,ActionEvent.CTRL_MASK));
			item.setIcon(Images.MOVE_FRONT.getIcon());
			sub.add(item);

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveForwards"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener((e)->surface.moveElementToFront(ModelElement.this,false));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0));
			item.setIcon(Images.MOVE_FRONT_STEP.getIcon());
			sub.add(item);

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveBackwards"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener((e)->surface.moveElementToBack(ModelElement.this,false));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0));
			item.setIcon(Images.MOVE_BACK_STEP.getIcon());
			sub.add(item);

			item=new JMenuItem(Language.tr("Surface.PopupMenu.MoveToBack"));
			item.setEnabled(!readOnly || allowEditOnReadOnly);
			item.addActionListener((e)->surface.moveElementToBack(ModelElement.this,true));
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,ActionEvent.CTRL_MASK));
			item.setIcon(Images.MOVE_BACK.getIcon());
			sub.add(item);

			if (surface.getLayers().size()>0) {
				popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.Layers")));
				sub.setIcon(Images.EDIT_LAYERS.getIcon());
				for (final String layer: surface.getLayers()) {
					final JCheckBoxMenuItem check=new JCheckBoxMenuItem(layer);
					final boolean onLayer=layers.size()==0 || layers.contains(layer);
					check.setSelected(onLayer);
					if (onLayer) {
						check.addActionListener(e->{
							if (layers.size()==0) {
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

		/* Popupmenü anzeigen */

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
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	public String[] getXMLNodeNames() {
		return new String[]{getClass().getName()};
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
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
	 * @param doc	Übergeordnetes xml-Dokument
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
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
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

		if (Language.trAll("Surface.XML.ModelElementLayer",name) && content!=null && !content.trim().isEmpty()) {
			layers.add(content);
		}

		return null;
	}

	/**
	 * Ermöglicht das Laden von Eigenschaften aus dem Haupt-Knoten des xml-Elements.
	 * @param node	xml-Element deren Eigenschaften verarbeitet werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	protected String loadPropertiesFromMainNode(final Element node) {
		return null;
	}

	/**
	 * Versucht die Daten des Modell-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Modell-Element beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final Integer I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Element.id",node));
		if (I==null || I<1) return String.format(Language.tr("Surface.Element.InvalidID"),node.getTagName());
		id=I;

		if (canSetDeleteProtection()) {
			final String s=Language.trAllAttribute("Surface.XML.Element.DeleteProtection",node);
			if (!s.trim().isEmpty() && !s.equals("0")) deleteProtection=true;
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
	 * Gibt an, ob das Element gelöscht werden darf.<br>
	 * Diese Methode muss nur überschrieben werden, wenn dieses Standardverhalten (=Löschen zulässig) für ein besonderes Element verändert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn das Element gelöscht werden darf.
	 */
	public boolean canDelete() {
		return true;
	}

	/**
	 * Gibt an, ob das Element kopiert werden darf.<br>
	 * Diese Methode muss nur überschrieben werden, wenn dieses Standardverhalten (=Kopieren zulässig) für ein besonderes Element verändert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn das Element kopiert werden darf.
	 */
	public boolean canCopy() {
		return true;
	}

	/**
	 * Gibt an, ob das Element selektiert werden darf.<br>
	 * Diese Methode muss nur überschrieben werden, wenn dieses Standardverhalten (=Selektieren zulässig) für ein besonderes Element verändert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn das Element selektiert werden darf.
	 */
	public boolean canSelect() {
		return true;
	}

	/**
	 * Gibt an, ob das Element nach vorne/nach hinten verschoben werden darf.<br>
	 * Diese Methode muss nur überschrieben werden, wenn dieses Standardverhalten (=Verschieben zulässig) für ein besonderes Element verändert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn das Element nach vorne/nach hinten verschoben werden darf.
	 */
	public boolean canArrange() {
		return true;
	}

	/**
	 * Gibt an, ob es möglich sein soll, das Element vor Löschversuchen zu schützen.<br>
	 * Diese Methode muss nur überschrieben werden, wenn dieses Standardverhalten (=Löschschutz-Status möglich) für ein besonderes Element verändert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn es möglich sein soll, das Element vor Löschversuchen zu schützen.
	 * @see ModelElement#isDeleteProtected()
	 * @see ModelElement#setDeleteProtection(boolean)
	 */
	protected boolean canSetDeleteProtection() {
		return true;
	}

	/**
	 * Wird vom Surface aufgerufen direkt nach dem das Element zu der Surface-Elementen-Liste hinzugefügt wurde.
	 */
	public void addedToSurface() {
	}

	/**
	 * Darf dieses Element zu einem Untermodell hinzugefügt werden?<br>
	 * Diese Methode muss nur überschrieben werden, wenn dieses Standardverhalten (=Einfügen in Untermodell zulässig) für ein besonderes Element verändert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn das Element in ein Untermodell eingefügt werden darf.
	 */
	public boolean canAddToSub() {
		return true;
	}

	/**
	 * Liefert den Namen (ohne Pfad und ohne ".html"-Erweiterung) der zu diesem Element gehörigen Hilfeseite
	 * @return	Name der zugehörigen Hilfeseite (oder <code>null</code>, wenn zu diesem Element keine Hilfe vorhanden ist)
	 */
	public String getHelpPageName() {
		return null;
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
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
	 * @return	Liste der Ebenen, auf denen sich dieses Element befindet (Originalliste, kann direkt verändert werden)
	 */
	public List<String> getLayers() {
		return layers;
	}

	/**
	 * Prüft, ob das Element momentan sichtbar sein soll.
	 * @param layers	Liste mit allen verfügbaren Ebenen (siehe {@link ModelSurface#getLayers()}
	 * @param visibleLayers	Liste der sichtbaren Ebenen (siehe {@link ModelSurface#getVisibleLayers()}
	 * @return	Gibt an, ob das Element moment sichtbar ist. (Achtung: Funktioniert nicht für Kanten; deren Sichtbarkeit muss relativ zu den Elementen geprüft werden)
	 * @see ModelSurface#isVisibleOnLayer(ModelElement)
	 * @see ModelSurface#getLayers()
	 * @see ModelSurface#getVisibleLayers()
	 */
	public boolean isVisibleOnLayer(final List<String> layers, final List<String> visibleLayers) {
		if (layers.size()==0 || this.layers.size()==0 || visibleLayers.size()==0) return true;

		boolean isOnLayer=false; /* Ist das Element zumindest auf irgendeinem Layer? */
		for (String layer: this.layers) {
			if (visibleLayers.contains(layer)) return true;
			if (layers.contains(layer)) isOnLayer=true;
		}
		if (!isOnLayer) return true; /* Wenn das Element nur Layer kennt, die es gar nicht gibt, ist das gleichwertig dazu, dass das Element nicht gelistet ist. */

		return false;
	}
}