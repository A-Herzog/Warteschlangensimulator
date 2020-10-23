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
import java.util.List;

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
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Löst in Abhängigkeit vom Füllstand eines Tanks ein Signal aus
 * @author Alexander Herzog
 * @see ModelElementTank
 */
public class ModelElementTankSensor extends ModelElementBox implements ModelElementSignalTrigger {
	/**
	 * Signal bei Unter- oder Überschreitung des Schwellenwerts auslösen
	 * @author Alexander Herzog
	 * @see ModelElementTankSensor#getThresholdDirection()
	 * @see ModelElementTankSensor#setThresholdDirection(ThresholdDirection)
	 */
	public enum ThresholdDirection {
		/** Signal beim Überschreiten des Schwellenwertes auslösen */
		DIRECTION_UP,
		/** Signal beim Unterschreiten des Schwellenwertes auslösen */
		DIRECTION_DOWN
	}

	/**
	 * ID des Tanks, bei dem der Füllstand überwacht werden soll
	 * @see #getTankId()
	 * @see #setTankId(int)
	 */
	private int tankId;

	/**
	 * Schwellenwert bei dessen Über- oder Unterschreibung das Signal ausgewählt wird
	 * @see #getThreshold()
	 * @see #setThreshold(double)
	 */
	private double threshold;

	/**
	 * Gibt an, ob der Schwellenwert ein Absolutwert (<code>false</code>) oder ein Prozentwert (<code>true</code>) ist.
	 * @see #isThresholdIsPercent()
	 * @see #setThresholdIsPercent(boolean)
	 */
	private boolean thresholdIsPercent;

	/**
	 * Signal bei Unter- oder Überschreitung des Schwellenwerts auslösen
	 * @see #getThresholdDirection()
	 * @see #setThresholdDirection(ThresholdDirection)
	 * @see ThresholdDirection
	 */
	private ThresholdDirection thresholdDirection;

	/**
	 * Konstruktor der Klasse <code>ModelElementTankSensor</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementTankSensor(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		tankId=-1;
		threshold=0;
		thresholdIsPercent=false;
		thresholdDirection=ThresholdDirection.DIRECTION_UP;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SENSOR.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.TankSensor.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementTankSensor)) return false;

		final ModelElementTankSensor other=(ModelElementTankSensor)element;
		if (tankId!=other.tankId) return false;
		if (threshold!=other.threshold) return false;
		if (thresholdIsPercent!=other.thresholdIsPercent) return false;
		if (thresholdDirection!=other.thresholdDirection) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementTankSensor) {
			final ModelElementTankSensor copySource=(ModelElementTankSensor)element;
			tankId=copySource.tankId;
			threshold=copySource.threshold;
			thresholdIsPercent=copySource.thresholdIsPercent;
			thresholdDirection=copySource.thresholdDirection;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementTankSensor clone(final EditModel model, final ModelSurface surface) {
		final ModelElementTankSensor element=new ModelElementTankSensor(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.TankSensor.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.TankSensor.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,200,80);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert die ID des Tanks, bei dem der Füllstand überwacht werden soll.
	 * @return	ID des Tanks, bei dem der Füllstand überwacht werden soll
	 * @see ModelElementTankSensor#setTankId(int)
	 */
	public int getTankId() {
		return tankId;
	}

	/**
	 * Stellt die ID des Tanks, bei dem der Füllstand überwacht werden soll.
	 * @param tankId	ID des Tanks, bei dem der Füllstand überwacht werden soll
	 * @see ModelElementTankSensor#getTankId()
	 */
	public void setTankId(final int tankId) {
		if (tankId>=0) this.tankId=tankId;
	}

	/**
	 * Liefert den Schwellenwert bei dessen Über- oder Unterschreibung das Signal ausgewählt wird.
	 * @return	Schwellenwert bei dessen Über- oder Unterschreibung das Signal ausgewählt wird
	 * @see ModelElementTankSensor#setThreshold(double)
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Stellt den Schwellenwert bei dessen Über- oder Unterschreibung das Signal ausgewählt wird ein.
	 * @param threshold	Schwellenwert bei dessen Über- oder Unterschreibung das Signal ausgewählt wird
	 * @see ModelElementTankSensor#getThreshold()
	 */
	public void setThreshold(double threshold) {
		if (threshold>=0) this.threshold=threshold; else this.threshold=0;
	}

	/**
	 * Gibt an, ob der Schwellenwert ein Absolutwert oder ein Prozentwert ist.
	 * @return	Schwellenwert ist Absolutwert (<code>false</code>) oder Prozentwert (<code>true</code>)
	 * @see ModelElementTankSensor#setThresholdIsPercent(boolean)
	 */
	public boolean isThresholdIsPercent() {
		return thresholdIsPercent;
	}

	/**
	 * Stellt ein, ob der Schwellenwert ein Absolutwert oder ein Prozentwert ist.
	 * @param thresholdIsPercent	Schwellenwert ist Absolutwert (<code>false</code>) oder Prozentwert (<code>true</code>)
	 * @see ModelElementTankSensor#isThresholdIsPercent()
	 */
	public void setThresholdIsPercent(final boolean thresholdIsPercent) {
		this.thresholdIsPercent=thresholdIsPercent;
	}

	/**
	 * Gibt an, ob das Signal bei Unter- oder Überschreitung des Schwellenwerts ausgelöst werden soll.
	 * @return	Signal bei Unter- oder Überschreitung des Schwellenwerts auslösen
	 */
	public ThresholdDirection getThresholdDirection() {
		return thresholdDirection;
	}

	/**
	 * Stellt ein, ob das Signal bei Unter- oder Überschreitung des Schwellenwerts ausgelöst werden soll.
	 * @param thresholdDirection	Signal bei Unter- oder Überschreitung des Schwellenwerts auslösen
	 */
	public void setThresholdDirection(final ThresholdDirection thresholdDirection) {
		if (thresholdDirection!=null) this.thresholdDirection=thresholdDirection;
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
			new ModelElementTankSensorDialog(owner,ModelElementTankSensor.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.TankSensor.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TankSensor.XML.TankID")));
		sub.setTextContent(""+tankId);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TankSensor.XML.Threshold")));
		sub.setTextContent(NumberTools.formatSystemNumber(threshold));
		if (thresholdIsPercent) sub.setAttribute(Language.trPrimary("Surface.TankSensor.XML.Threshold.IsPercent"),Language.trPrimary("Surface.TankSensor.XML.Threshold.IsPercent.Yes"));
		switch (thresholdDirection) {
		case DIRECTION_DOWN:
			sub.setAttribute(Language.trPrimary("Surface.TankSensor.XML.Threshold.Direction"),Language.trPrimary("Surface.TankSensor.XML.Threshold.Direction.Down"));
			break;
		case DIRECTION_UP:
			sub.setAttribute(Language.trPrimary("Surface.TankSensor.XML.Threshold.Direction"),Language.trPrimary("Surface.TankSensor.XML.Threshold.Direction.Up"));
			break;
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

		if (Language.trAll("Surface.TankSensor.XML.TankID",name)) {
			final Integer I=NumberTools.getInteger(NumberTools.systemNumberToLocalNumber(content));
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			tankId=I.intValue();
		}

		if (Language.trAll("Surface.TankSensor.XML.Threshold",name)) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			threshold=D.doubleValue();
			final String percent=Language.trAllAttribute("Surface.TankSensor.XML.Threshold.IsPercent",node);
			thresholdIsPercent=Language.trAll("Surface.TankSensor.XML.Threshold.IsPercent.Yes",percent);
			final String direction=Language.trAllAttribute("Surface.TankSensor.XML.Threshold.Direction",node);
			if (Language.trAll("Surface.TankSensor.XML.Threshold.Direction.Up",direction)) {
				thresholdDirection=ThresholdDirection.DIRECTION_UP;
			} else {
				thresholdDirection=ThresholdDirection.DIRECTION_DOWN;
			}
		}

		return null;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station während der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return false;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementTankSensor";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (tankId>=0) descriptionBuilder.addProperty(Language.tr("ModelDescription.TankSensor.TankID"),""+tankId,1000);

		if (thresholdIsPercent) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TankSensor.Threshold.Percent"),NumberTools.formatNumberMax(threshold)+"%",2000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TankSensor.Threshold.Absolute"),NumberTools.formatNumberMax(threshold),2000);
		}

		switch (thresholdDirection) {
		case DIRECTION_DOWN:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TankSensor.Threshold.Direction"),Language.tr("ModelDescription.TankSensor.Threshold.Direction.Down"),3000);
			break;
		case DIRECTION_UP:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TankSensor.Threshold.Direction"),Language.tr("ModelDescription.TankSensor.Threshold.Direction.Up"),3000);
			break;
		}
	}

	@Override
	public String[] getSignalNames() {
		return new String[]{getName()};
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsIn.size()>0) return false;
		if (connectionsOut.size()>0) return false;

		return false;
	}
}
