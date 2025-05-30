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
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementDifferentialCounterData;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Diese Klasse stellt eine Differenzz�hler-Station dar, d.h. einen Z�hler, der erh�ht
 * oder verringert werden kann, wenn ein Kunde ihn passiert (um als Gruppe z�hlen zu k�nnen,
 * wie viele Kunden sich in einem Bereich befinden.
 * @author Alexander Herzog
 */
public class ModelElementDifferentialCounter extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener {
	/**
	 * Ver�nderung des Z�hlers beim Durchlauf eines Kunden
	 * @see #getChange()
	 * @see #setChange(int)
	 */
	private int change=1;

	/**
	 * Optionale Bedingungen f�r die Ausl�sung des Z�hlers
	 * @see #getCondition()
	 */
	private final CounterCondition counterCondition;

	/**
	 * Konstruktor der Klasse <code>ModelElementDifferentialCounter</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementDifferentialCounter(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE_PLUSMINUS);
		change=1;
		counterCondition=new CounterCondition();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DIFFERENTIAL_COUNTER.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.DifferentialCounter.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDifferentialCounter)) return false;
		final ModelElementDifferentialCounter otherCounter=(ModelElementDifferentialCounter)element;

		if (otherCounter.change!=change) return false;
		if (!otherCounter.counterCondition.equalsCounterCondition(counterCondition)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDifferentialCounter) {
			final ModelElementDifferentialCounter copySource=(ModelElementDifferentialCounter)element;
			change=copySource.change;
			counterCondition.copyFrom(copySource.counterCondition);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDifferentialCounter clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDifferentialCounter element=new ModelElementDifferentialCounter(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.DifferentialCounter.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.DifferentialCounter.Name");
	}

	/**
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null) return null; /* keinen Untertitel in der Templates-Liste anzeigen */
		if (change>=0) return Language.tr("Surface.DifferentialCounter.Value")+"+="+change; else return Language.tr("Surface.DifferentialCounter.Value")+"-="+(-change);
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(230,230,230);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
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
			new ModelElementDifferentialCounterDialog(owner,ModelElementDifferentialCounter.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt stations-bedingte zus�tzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		final long value=FastMath.round(((RunElementDifferentialCounterData)builder.data).getValue(true));
		builder.results.append("\n"+Language.tr("Statistics.Counter")+"\n");
		builder.results.append(NumberTools.formatLong(value)+"\n");
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_COUNTER_VALUE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_COUNTER_VALUE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_COUNTER_VALUE);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.COUNTER);
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param surfacePanel	Zeichenfl�che
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.DifferentialCounter.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DifferentialCounter.XML.Increment")));
		sub.setTextContent(""+change);

		counterCondition.saveToXML(node);
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

		if (Language.trAll("Surface.DifferentialCounter.XML.Increment",name)) {
			final Integer I=NumberTools.getInteger(content);
			if (I==null || I==0) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			change=I;
			return null;
		}

		if (counterCondition.isCounterConditionElement(node)) {
			counterCondition.loadFromXML(node);
			return null;
		}

		return null;
	}

	/**
	 * Liefert den Wert, um den der Z�hler ver�ndert wird, wenn ein Kunde ihn durchl�uft
	 * @return	Ver�nderung des Z�hlers beim Durchlauf eines Kunden
	 */
	public int getChange() {
		return change;
	}

	/**
	 * Stellt den Wert ein, um den der Z�hler ver�ndert wird, wenn ein Kunde ihn durchl�uft
	 * @param change	Ver�nderung des Z�hlers beim Durchlauf eines Kunden
	 */
	public void setChange(final int change) {
		if (change!=0) this.change=change;
	}

	/**
	 * Liefert die optionalen Bedingungen f�r die Ausl�sung des Z�hlers
	 * @return	Optionale Bedingungen f�r die Ausl�sung des Z�hlers
	 */
	public CounterCondition getCondition() {
		return counterCondition;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station w�hrend der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return true;
	}

	/**
	 * Cache der verf�gbaren {@link StringBuilder}
	 * f�r {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private StringBuilder[] animationSB;

	/**
	 * Tempor�rer {@link StringBuilder} zum
	 * Umwandeln von Zahlen in Zeichenketten
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private StringBuilder animationSBCache;

	/**
	 * N�chster in {@link #animationSB} zu verwendender Eintrag
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private int animationSBNext;


	/**
	 * Letzter in {@link #updateSimulationData(SimulationData, boolean)}
	 * dargestellter Wert
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private long lastValue=0;

	/**
	 * Zeichenkettenrepr�sentation von {@link #lastValue}
	 * die ggf. wiederverwendet werden kann.
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private String valueName;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		final RunElementData runData=getRunData();
		if (runData==null || isPreview) return false;

		if (!(runData instanceof RunElementDifferentialCounterData)) return false;
		final RunElementDifferentialCounterData data=(RunElementDifferentialCounterData)runData;
		long value=(long)data.getValue(true);

		if (animationSB==null) {
			animationSB=new StringBuilder[2];
			animationSB[0]=new StringBuilder(100);
			animationSB[1]=new StringBuilder(100);
			animationSBNext=0;
			valueName=Language.tr("Surface.Counter.Name.Short")+"=";
		} else {
			if (Math.abs(value-lastValue)<10E-5) return false;
		}
		lastValue=value;

		final StringBuilder sb=animationSB[animationSBNext];
		sb.setLength(0);
		sb.append(valueName);
		sb.append(NumberTools.formatLong(value));
		if (animationSBCache==null) animationSBCache=new StringBuilder();

		setAnimationStringBuilder(animationSB[animationSBNext],()->{animationSBNext=(animationSBNext++)%2;});

		return true;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDifferentialCounter";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		final String value;
		if (change>0) value="+"+NumberTools.formatLong(change); else value=NumberTools.formatLong(change);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.DifferentialCounter.Change"),value,1000);

		counterCondition.buildDescription(descriptionBuilder,2000);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testInteger(this,Language.tr("Surface.DifferentialCounter.Dialog.Increment"),change,newChange->{change=newChange;});
		searcher.testString(this,Language.tr("Surface.DifferentialCounter.Dialog.Condition"),counterCondition.getCondition(),newCondition->counterCondition.setCondition(newCondition));
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		counterCondition.clientTypeRenamed(oldName,newName);
	}
}