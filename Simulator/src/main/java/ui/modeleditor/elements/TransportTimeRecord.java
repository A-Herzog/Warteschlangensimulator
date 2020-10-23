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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse hält die Daten zu den Transportzeiten
 * für Transportquellen vor.
 * @author Alexander Herzog
 * @see ModelElementTransportSource
 */
public final class TransportTimeRecord implements Cloneable {

	/**
	 * @see TransportTimeRecord#getDelayType()
	 * @see TransportTimeRecord#setDelayType(DelayType)
	 */
	public enum DelayType {
		/** Transportzeit ist Wartezeit */
		DELAY_TYPE_WAITING,

		/** Transportzeit ist Transferzeit */
		DELAY_TYPE_TRANSFER,

		/** Transportzeit ist Bedienzeit */
		DELAY_TYPE_PROCESS,

		/** Transportzeit nicht erfassen */
		DELAY_TYPE_NOTHING
	}

	/**
	 * Objekt, welches die Verteilungen und Ausdrücke für die Transportzeiten vorhält
	 * @see #getTransportTime()
	 */
	private DistributionSystem transportTime;

	/**
	 * Verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Art der Verzögerung beim Transport
	 * @see #getDelayType()
	 * @see #setDelayType(DelayType)
	 * @see DelayType
	 */
	private DelayType delayType=DelayType.DELAY_TYPE_TRANSFER;

	/**
	 * Konstruktor der Klasse
	 */
	public TransportTimeRecord() {
		/* Um sicher zu stellen, dass die Language-Strings auch in den Sprachdateien vorhanden sind. Der folgende DistributionSystem-Konstruktor ist kein Scan-Ziel für die Sprachdateien. */
		Language.tr("Surface.TransportSource.XML.DestinationTime");

		transportTime=new DistributionSystem("Surface.TransportSource.XML.DestinationTime",null,false);
		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		delayType=DelayType.DELAY_TYPE_TRANSFER;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdrücke für die Transportzeiten vorhält
	 * @return	Transportzeiten
	 */
	public DistributionSystem getTransportTime() {
		return transportTime;
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=timeBase;
	}

	/**
	 * Gibt an, ob die Verzögerung beim Transport als Warte-, Transfer- oder Bedienzeit gezählt werden soll.
	 * @return	Art der Verzögerung beim Transport
	 * @see DelayType
	 */
	public DelayType getDelayType() {
		return delayType;
	}

	/**
	 * Stellt ein, ob die Verzögerung beim Transport als Warte-, Transfer- oder Bedienzeit gezählt werden soll.
	 * @param delayType	Art der Verzögerung beim Transport
	 * @see DelayType
	 */
	public void setDelayType(final DelayType delayType) {
		this.delayType=delayType;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param transportTimeRecord	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	public boolean equalsTransportTimeRecord(final TransportTimeRecord transportTimeRecord) {
		if (transportTimeRecord==null) return false;

		if (!transportTime.equalsDistributionSystem(transportTimeRecord.transportTime)) return false;
		if (timeBase!=transportTimeRecord.timeBase) return false;
		if (delayType!=transportTimeRecord.delayType) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @return	Kopiertes Element
	 */
	@Override
	public TransportTimeRecord clone() {
		final TransportTimeRecord clone=new TransportTimeRecord();

		clone.transportTime=transportTime.clone();
		clone.timeBase=timeBase;
		clone.delayType=delayType;

		return clone;
	}

	/**
	 * Informiert das Objekt, dass sich der Name einer Ziel-Station geändert hat
	 * @param oldName	Alter Zielstationname
	 * @param newName	Neuer Zielstationname
	 */
	public void destinationRenamed(final String oldName, final String newName) {
		if (oldName==null || oldName.isEmpty() || newName==null || newName.isEmpty()) return;
		if (!transportTime.nameInUse(newName)) transportTime.renameSubType(oldName,newName);
	}

	private void addAttributesToGlobalElement(final Element sub) {
		sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Type"),Language.trPrimary("Surface.TransportSource.XML.Type.WaitingTime"));
			break;
		case DELAY_TYPE_TRANSFER:
			sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Type"),Language.trPrimary("Surface.TransportSource.XML.Type.TransferTime"));
			break;
		case DELAY_TYPE_PROCESS:
			sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Type"),Language.trPrimary("Surface.TransportSource.XML.Type.ProcessTime"));
			break;
		case DELAY_TYPE_NOTHING:
			sub.setAttribute(Language.trPrimary("Surface.TransportSource.XML.Type"),Language.trPrimary("Surface.TransportSource.XML.Type.Nothing"));
			break;
		}
	}

	/**
	 * Speichert die Eigenschaften des Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void addPropertiesToXML(final Document doc, final Element node) {
		transportTime.save(doc,node,element->addAttributesToGlobalElement(element));
	}

	private void loadGlobalProperties(final Element node) {
		final String timeBaseName=Language.trAllAttribute("Surface.TransportSource.XML.TimeBase",node);
		timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
		final String type=Language.trAllAttribute("Surface.TransportSource.XML.Type",node);
		if (Language.trAll("Surface.TransportSource.XML.Type.WaitingTime",type)) delayType=DelayType.DELAY_TYPE_WAITING;
		if (Language.trAll("Surface.TransportSource.XML.Type.TransferTime",type)) delayType=DelayType.DELAY_TYPE_TRANSFER;
		if (Language.trAll("Surface.TransportSource.XML.Type.ProcessTime",type)) delayType=DelayType.DELAY_TYPE_PROCESS;
		if (Language.trAll("Surface.TransportSource.XML.Type.Nothing",type)) delayType=DelayType.DELAY_TYPE_NOTHING;
	}

	/**
	 * Versucht eine Teilinformation aus dem angegebenen xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen. (Passt dieser nicht zu dem Element, so wird er ignoriert.)
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String loadProperties(final Element node) {
		if (DistributionSystem.isDistribution(node)) {
			if (transportTime.isGlobal(node)) loadGlobalProperties(node);
			return transportTime.loadDistribution(node);
		}

		if (DistributionSystem.isExpression(node)) {
			if (transportTime.isGlobal(node)) loadGlobalProperties(node);
			return transportTime.loadExpression(node);
		}

		return null;
	}

	/**
	 * Fügt die Beschreibung für die Daten dieses Objekts als Eigenschaft zu der Beschreibung hinzu
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	public void buildDescriptionProperty(final ModelDescriptionBuilder descriptionBuilder) {
		/* Transportzeiten */
		transportTime.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.TransportTimeRecord.TimeClientType"),Language.tr("ModelDescription.TransportTimeRecord.TimeGeneralCase"),1000);

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,2000);

		/* Wie soll die Transportzeit erfasst werden */
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTimeRecord.TimeMode"),Language.tr("ModelDescription.TransportTimeRecord.TimeMode.Waiting"),3000);
			break;
		case DELAY_TYPE_TRANSFER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTimeRecord.TimeMode"),Language.tr("ModelDescription.TransportTimeRecord.TimeMode.Transfer"),3000);
			break;
		case DELAY_TYPE_PROCESS:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTimeRecord.TimeMode"),Language.tr("ModelDescription.TransportTimeRecord.TimeMode.Process"),3000);
			break;
		case DELAY_TYPE_NOTHING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTimeRecord.TimeMode"),Language.tr("ModelDescription.TransportTimeRecord.TimeMode.Nothing"),3000);
			break;
		}
	}
}