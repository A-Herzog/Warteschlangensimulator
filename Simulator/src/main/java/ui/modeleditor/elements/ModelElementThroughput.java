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
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementThroughputData;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
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
 * Erfasst den Durchsatz (in Kunden pro Zeiteinheit) an der Station
 * @author Alexander Herzog
 */
public class ModelElementThroughput extends ModelElementMultiInSingleOutBox {
	/**
	 * Konstruktor der Klasse <code>ModelElementThroughput</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementThroughput(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE_123);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_THROUGHPUT.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Throughput.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementThroughput)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementThroughput clone(final EditModel model, final ModelSurface surface) {
		final ModelElementThroughput element=new ModelElementThroughput(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Throughput.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Throughput.Name");
	}

	private static final Color defaultBackgroundColor=new Color(230,230,230);

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
			new ModelElementThroughputDialog(owner,ModelElementThroughput.this,readOnly);
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
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
	}

	/**
	 * Fügt stations-bedingte zusätzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		if (((RunElementThroughputData)builder.data).statistic==null) return;
		double value=((RunElementThroughputData)builder.data).statistic.getQuotient();
		if (value==0) return;
		builder.sb.append("\n"+Language.tr("Statistics.Throughput")+": ");
		if (value>=1) {
			builder.sb.append(NumberTools.formatNumber(value));
			builder.sb.append(" (1/"+Language.tr("Statistics.TimeUnit.Second")+")");
		} else {
			value*=60;
			if (value>=1) {
				builder.sb.append(NumberTools.formatNumber(value));
				builder.sb.append(" (1/"+Language.tr("Statistics.TimeUnit.Minute")+")");
			} else {
				value*=60;
				if (value>=1) {
					builder.sb.append(NumberTools.formatNumber(value));
					builder.sb.append(" (1/"+Language.tr("Statistics.TimeUnit.Hour")+")");
				} else {
					value*=24;
					builder.sb.append(NumberTools.formatNumber(value));
					builder.sb.append(" (1/"+Language.tr("Statistics.TimeUnit.Day")+")");
				}
			}
		}
		builder.sb.append("\n");
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Popupmenüs, welches die Einträge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_THROUGHPUT);
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
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Throughput.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);
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

		return null;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station während der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return true;
	}

	private StringBuilder[] animationSB;
	private StringBuilder animationSBCache;
	private int animationSBNext;
	private double lastValue=0;
	private String valueName;
	private String unitSecond, unitMinute, unitHour, unitDay;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		final RunElementData runData=getRunData();
		if (runData==null || isPreview) return false;

		if (!(runData instanceof RunElementThroughputData)) return false;
		final RunElementThroughputData data=(RunElementThroughputData)runData;
		double value=data.getValue(true);

		if (animationSB==null) {
			animationSB=new StringBuilder[2];
			animationSB[0]=new StringBuilder(100);
			animationSB[1]=new StringBuilder(100);
			animationSBNext=0;
			valueName=Language.tr("Statistics.Throughput")+"=";
			unitSecond=" (1/"+Language.tr("Statistics.TimeUnit.Second")+")";
			unitMinute=" (1/"+Language.tr("Statistics.TimeUnit.Minute")+")";
			unitHour=" (1/"+Language.tr("Statistics.TimeUnit.Hour")+")";
			unitDay=" (1/"+Language.tr("Statistics.TimeUnit.Day")+")";
		} else {
			if (FastMath.abs(value-lastValue)<10E-5) return false;
		}
		lastValue=value;

		final StringBuilder sb=animationSB[animationSBNext];
		sb.setLength(0);
		sb.append(valueName);
		if (animationSBCache==null) animationSBCache=new StringBuilder();
		if (value>=1) {
			sb.append(NumberTools.formatNumber(value,animationSBCache));
			sb.append(unitSecond);
		} else {
			value*=60;
			if (value>=1) {
				sb.append(NumberTools.formatNumber(value,animationSBCache));
				sb.append(unitMinute);
			} else {
				value*=60;
				if (value>=1) {
					sb.append(NumberTools.formatNumber(value,animationSBCache));
					sb.append(unitHour);
				} else {
					value*=24;
					sb.append(NumberTools.formatNumber(value,animationSBCache));
					sb.append(unitDay);
				}
			}
		}

		setAnimationStringBuilder(animationSB[animationSBNext],()->{animationSBNext=(animationSBNext++)%2;});

		return true;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementThroughput";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
	}
}
