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
import mathtools.NumberTools;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Hält die Daten zu Quell- und Zielventil und Durchflussmenge für
 * ein {@link ModelElementTankFlowByClient}- oder ein {@link ModelElementTankFlowBySignal}-Element vor.
 * @author Alexander Herzog
 * @see ModelElementTankFlowByClient
 * @see ModelElementTankFlowBySignal
 */
public final class ModelElementTankFlowData implements Cloneable {
	/**
	 * Art auf die der Fluss gestoppt wird
	 * @author Alexander Herzog
	 * @see ModelElementTankFlowData#getStopCondition()
	 * @see ModelElementTankFlowData#setStopByTime(double)
	 * @see ModelElementTankFlowData#setStopByQuantity(double)
	 * @see ModelElementTankFlowData#setStopBySignal(String)
	 */
	public enum FlowStopCondition {
		/**
		 * Fluss nach bestimmter Zeit stoppen
		 * @see ModelElementTankFlowData#setStopByTime(double)
		 */
		STOP_BY_TIME,

		/**
		 * Fluss nach bestimmter Durchflussmenge stoppen
		 * @see ModelElementTankFlowData#setStopByQuantity(double)
		 */
		STOP_BY_QUANTITY,

		/**
		 * Fluss stoppen, wenn Signal auftritt
		 * @see ModelElementTankFlowData#setStopBySignal(String)
		 */
		STOP_BY_SIGNAL
	}

	/**
	 * ID des Quell-Elements
	 * @see #getSourceID()
	 * @see #setSourceID(int)
	 */
	private int sourceID;

	/**
	 * 0-basierende Nummer des Ventils an dem Quell-Element
	 * @see #getSourceValveNr()
	 * @see #setSourceValveNr(int)
	 */
	private int sourceValveNr;

	/**
	 * ID des Ziel-Elements
	 * @see #getDestinationID()
	 * @see #setDestinationID(int)
	 */
	private int destinationID;

	/**
	 * 0-basierende Nummer des Ventils an dem Ziel-Element
	 * @see #getDestinationValveNr()
	 * @see #setDestinationValveNr(int)
	 */
	private int destinationValveNr;

	/**
	 * Eigenschaft, durch die der Fluss gestoppt werden soll.
	 * @see #getStopCondition()
	 * @see FlowStopCondition
	 */
	private FlowStopCondition stopCondition;

	/**
	 * Zeitdauer nach der der Fluss gestoppt werden soll
	 * @see #getStopTime()
	 * @see #setStopByTime(double)
	 */
	private double stopTime;

	/**
	 * Durchflussmenge nach der der Fluss gestoppt werden soll
	 * @see #getStopQuantity()
	 * @see #setStopByQuantity(double)
	 */
	private double stopQuantity;

	/**
	 * Name des Signals, das den Fluss stoppt
	 * @see #getStopSignal()
	 * @see #setStopBySignal(String)
	 */
	private String stopSignal;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementTankFlowData() {
		sourceID=-1;
		sourceValveNr=0;
		destinationID=-1;
		destinationValveNr=0;
		stopCondition=FlowStopCondition.STOP_BY_TIME;
		stopTime=1;
		stopQuantity=1;
		stopSignal="";
	}

	/**
	 * Copy-Konstruktor
	 * @param copySource	Ausgangsobjekt dessen Daten kopiert werden sollen
	 */
	public ModelElementTankFlowData(final ModelElementTankFlowData copySource) {
		sourceID=copySource.sourceID;
		sourceValveNr=copySource.sourceValveNr;
		destinationID=copySource.destinationID;
		destinationValveNr=copySource.destinationValveNr;
		stopCondition=copySource.stopCondition;
		stopTime=copySource.stopTime;
		stopQuantity=copySource.stopQuantity;
		stopSignal=copySource.stopSignal;
	}

	/**
	 * Vergleicht zwei {@link ModelElementTankFlowData}-Objekte
	 * @param otherData	Weiteres {@link ModelElementTankFlowData}-Objekt das mit diesem Objekt verglichen werden soll
	 * @return	Liefert <code>true</code> wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsData(final ModelElementTankFlowData otherData) {
		if (otherData==null) return false;
		if (sourceID!=otherData.sourceID) return false;
		if (sourceValveNr!=otherData.sourceValveNr) return false;
		if (destinationID!=otherData.destinationID) return false;
		if (destinationValveNr!=otherData.destinationValveNr) return false;
		if (stopCondition!=otherData.stopCondition) return false;
		if (stopTime!=otherData.stopTime) return false;
		if (stopQuantity!=otherData.stopQuantity) return false;
		if (!stopSignal.equals(otherData.stopSignal)) return false;
		return true;
	}

	/**
	 * Kopiert die Daten aus einem anderen {@link ModelElementTankFlowData}-Objekt in dieses.
	 * @param otherData	Quelle, aus der die Daten kopiert werden sollen
	 */
	public void setDataFrom(final ModelElementTankFlowData otherData) {
		if (otherData==null) return;
		sourceID=otherData.sourceID;
		sourceValveNr=otherData.sourceValveNr;
		destinationID=otherData.destinationID;
		destinationValveNr=otherData.destinationValveNr;
		stopCondition=otherData.stopCondition;
		stopTime=otherData.stopTime;
		stopQuantity=otherData.stopQuantity;
		stopSignal=otherData.stopSignal;
	}

	/**
	 * Erstellt dieses Kopie des {@link ModelElementTankFlowData}-Objektes
	 */
	@Override
	public ModelElementTankFlowData clone() {
		return new ModelElementTankFlowData(this);
	}

	/**
	 * Liefert die ID des Quell-Elements des Flusses (oder -1, wenn der Fluss aus dem Nichts kommt).
	 * @return	ID des Quell-Elements
	 * @see ModelElementTankFlowData#setSourceID(int)
	 * @see ModelElementTankFlowData#getSourceValveNr()
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * Stellt die ID des Quell-Elements des Flusses ein. (-1, wenn der Fluss aus dem Nichts kommt.)
	 * @param sourceID	ID des Quell-Elements
	 * @see ModelElementTankFlowData#getSourceID()
	 * @see ModelElementTankFlowData#setSourceValveNr(int)
	 */
	public void setSourceID(final int sourceID) {
		this.sourceID=sourceID;
	}

	/**
	 * Liefert die 0-basierende Nummer des Ventils an dem Quell-Element
	 * @return	0-basierende Nummer des Ventils an dem Quell-Element
	 * @see ModelElementTankFlowData#setSourceValveNr(int)
	 * @see ModelElementTankFlowData#getSourceID()
	 */
	public int getSourceValveNr() {
		return sourceValveNr;
	}

	/**
	 * Stellt die 0-basierende Nummer des Ventils an dem Quell-Element ein.
	 * @param sourceValveNr	0-basierende Nummer des Ventils an dem Quell-Element
	 * @see ModelElementTankFlowData#getSourceValveNr()
	 * @see ModelElementTankFlowData#setSourceID(int)
	 */
	public void setSourceValveNr(final int sourceValveNr) {
		this.sourceValveNr=sourceValveNr;
	}

	/**
	 * Liefert die ID des Ziel-Elements des Flusses (oder -1, wenn der Fluss ins Nichts verschwindet).
	 * @return	ID des Ziel-Elements
	 * @see ModelElementTankFlowData#setDestinationID(int)
	 * @see ModelElementTankFlowData#getDestinationValveNr()
	 */
	public int getDestinationID() {
		return destinationID;
	}

	/**
	 * Stellt die ID des Ziel-Elements des Flusses ein. (-1, wenn der Fluss ins Nichts verschwindet.)
	 * @param destinationID	ID des Ziel-Elements
	 * @see ModelElementTankFlowData#getDestinationID()
	 * @see ModelElementTankFlowData#setDestinationValveNr(int)
	 */
	public void setDestinationID(final int destinationID) {
		this.destinationID=destinationID;
	}

	/**
	 * Liefert die 0-basierende Nummer des Ventils an dem Ziel-Element
	 * @return	0-basierende Nummer des Ventils an dem Ziel-Element
	 * @see ModelElementTankFlowData#setDestinationValveNr(int)
	 * @see ModelElementTankFlowData#getDestinationID()
	 */
	public int getDestinationValveNr() {
		return destinationValveNr;
	}

	/**
	 * Stellt die 0-basierende Nummer des Ventils an dem Ziel-Element ein.
	 * @param destinationValveNr	0-basierende Nummer des Ventils an dem Ziel-Element
	 * @see ModelElementTankFlowData#getDestinationValveNr()
	 * @see ModelElementTankFlowData#setDestinationID(int)
	 */
	public void setDestinationValveNr(final int destinationValveNr) {
		this.destinationValveNr=destinationValveNr;
	}

	/**
	 * Gibt an, durch was der Fluss gestoppt werden soll.
	 * @return	Eigenschaft, durch die der Fluss gestoppt werden soll.
	 * @see ModelElementTankFlowData#getStopTime()
	 * @see ModelElementTankFlowData#getStopQuantity()
	 * @see ModelElementTankFlowData#getStopSignal()
	 * @see ModelElementTankFlowData#setStopByTime(double)
	 * @see ModelElementTankFlowData#setStopByQuantity(double)
	 * @see ModelElementTankFlowData#setStopBySignal(String)
	 */
	public FlowStopCondition getStopCondition() {
		return stopCondition;
	}


	/**
	 * Liefert die Zeitdauer, nach der der Fluss gestoppt werden soll.
	 * @return	Zeitdauer nach der der Fluss gestoppt werden soll
	 * @see ModelElementTankFlowData#setStopByTime(double)
	 * @see ModelElementTankFlowData#getStopCondition()
	 */
	public double getStopTime() {
		return stopTime;
	}

	/**
	 * Liefert die Durchflussmenge, nach der der Fluss gestoppt werden soll.
	 * @return	Durchflussmenge nach der der Fluss gestoppt werden soll
	 * @see ModelElementTankFlowData#setStopByQuantity(double)
	 * @see ModelElementTankFlowData#getStopCondition()
	 */
	public double getStopQuantity() {
		return stopQuantity;
	}

	/**
	 * Liefert den Namen des Signals, das den Fluss stoppt.
	 * @return	Name des Signals, das den Fluss stoppt
	 * @see ModelElementTankFlowData#setStopBySignal(String)
	 * @see ModelElementTankFlowData#getStopCondition()
	 */
	public String getStopSignal() {
		return stopSignal;
	}

	/**
	 * Stellt eine Zeit ein, nach der der Fluss gestoppt werden soll.
	 * @param stopTime	Zeitdauer, nach der der Fluss gestoppt werden soll
	 * @see ModelElementTankFlowData#getStopTime()
	 * @see ModelElementTankFlowData#getStopCondition()
	 */
	public void setStopByTime(final double stopTime) {
		if (stopTime>0) this.stopTime=stopTime;
		stopCondition=FlowStopCondition.STOP_BY_TIME;
	}

	/**
	 * Stellt die Durchflussmenge ein, nach der der Fluss gestoppt werden soll.
	 * @param stopQuantity	Durchflussmenge, nach der der Fluss gestoppt werden soll
	 * @see ModelElementTankFlowData#getStopQuantity()
	 * @see ModelElementTankFlowData#getStopCondition()
	 */
	public void setStopByQuantity(final double stopQuantity) {
		if (stopQuantity>0) this.stopQuantity=stopQuantity;
		stopCondition=FlowStopCondition.STOP_BY_QUANTITY;
	}

	/**
	 * Stellt den Namen des Signals ein, das den Fluss stoppt.
	 * @param stopSignal	Namen des Signals ein, das den Fluss stoppt
	 * @see ModelElementTankFlowData#getStopSignal()
	 * @see ModelElementTankFlowData#getStopCondition()
	 */
	public void setStopBySignal(final String stopSignal) {
		if (stopSignal==null) this.stopSignal=""; else this.stopSignal=stopSignal;
		stopCondition=FlowStopCondition.STOP_BY_SIGNAL;
	}

	/**
	 * Speichert die Einstellungen dieses Objekts als xml-Unterelemente eines übergebenen xml-Elements
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element sub;

		if (sourceID>=0) {
			parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.FlowData.Source")));
			sub.setAttribute(Language.trPrimary("Surface.XML.FlowData.SourceDestination.Id"),""+sourceID);
			sub.setAttribute(Language.trPrimary("Surface.XML.FlowData.SourceDestination.Valve"),""+(sourceValveNr+1));
		}

		if (destinationID>=0) {
			parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.FlowData.Destination")));
			sub.setAttribute(Language.trPrimary("Surface.XML.FlowData.SourceDestination.Id"),""+destinationID);
			sub.setAttribute(Language.trPrimary("Surface.XML.FlowData.SourceDestination.Valve"),""+(destinationValveNr+1));
		}

		switch (stopCondition) {
		case STOP_BY_TIME:
			if (stopTime>0) {
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.FlowData.StopTime")));
				sub.setTextContent(NumberTools.formatSystemNumber(stopTime));
			}
			break;
		case STOP_BY_QUANTITY:
			if (stopQuantity>0) {
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.FlowData.StopQuantity")));
				sub.setTextContent(NumberTools.formatSystemNumber(stopQuantity));
			}
			break;
		case STOP_BY_SIGNAL:
			if (stopSignal!=null && !stopSignal.trim().isEmpty()) {
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.FlowData.StopSignal")));
				sub.setTextContent(stopSignal);
			}
			break;
		}
	}

	/**
	 * Lädt eine Eigenschaft dieses Objektes aus einem xml-Knoten
	 * @param node	xml-Knoten aus dem (ggf., wenn es ein passender Knoten ist) eine Eigenschaft gelesen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadPropertyFromXML(final Element node) {
		if (Language.trAll("Surface.XML.FlowData.Source",node.getNodeName())) {
			final String idString=Language.trAllAttribute("Surface.XML.FlowData.SourceDestination.Id",node);
			final String nrString=Language.trAllAttribute("Surface.XML.FlowData.SourceDestination.Valve",node);
			final Integer I=NumberTools.getNotNegativeInteger(idString);
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.FlowData.SourceDestination.Id"),node.getNodeName(),node.getParentNode().getNodeName());
			sourceID=I.intValue();
			final Long L=NumberTools.getPositiveLong(nrString);
			if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.FlowData.SourceDestination.Valve"),node.getNodeName(),node.getParentNode().getNodeName());
			sourceValveNr=L.intValue()-1;
			return null;
		}

		if (Language.trAll("Surface.XML.FlowData.Destination",node.getNodeName())) {
			final String idString=Language.trAllAttribute("Surface.XML.FlowData.SourceDestination.Id",node);
			final String nrString=Language.trAllAttribute("Surface.XML.FlowData.SourceDestination.Valve",node);
			final Integer I=NumberTools.getNotNegativeInteger(idString);
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.FlowData.SourceDestination.Id"),node.getNodeName(),node.getParentNode().getNodeName());
			destinationID=I.intValue();
			final Long L=NumberTools.getPositiveLong(nrString);
			if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.FlowData.SourceDestination.Valve"),node.getNodeName(),node.getParentNode().getNodeName());
			destinationValveNr=L.intValue()-1;
			return null;
		}

		if (Language.trAll("Surface.XML.FlowData.StopTime",node.getNodeName())) {
			stopCondition=FlowStopCondition.STOP_BY_TIME;
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(node.getTextContent()));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			stopTime=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.XML.FlowData.StopQuantity",node.getNodeName())) {
			stopCondition=FlowStopCondition.STOP_BY_QUANTITY;
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(node.getTextContent()));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			stopQuantity=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.XML.FlowData.StopSignal",node.getNodeName())) {
			stopCondition=FlowStopCondition.STOP_BY_SIGNAL;
			stopSignal=node.getTextContent();
			return null;
		}

		return null;
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param position	Position in der Reihenfolge der Description-Builder-Eigenschaften
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder, final int position) {
		if (sourceID>=0) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.Source"),String.format(Language.tr("ModelDescription.FlowData.Source.Element"),sourceID,sourceValveNr+1),position+0);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.Source"),Language.tr("ModelDescription.FlowData.Source.NotConnected"),position+0);
		}

		if (destinationID>=0) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.Destination"),String.format(Language.tr("ModelDescription.FlowData.Destination.Element"),destinationID,destinationValveNr+1),position+1);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.Destination"),Language.tr("ModelDescription.FlowData.Destination.NotConnected"),position+1);
		}

		switch (stopCondition) {
		case STOP_BY_TIME:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.StopCondition"),String.format(Language.tr("ModelDescription.FlowData.StopCondition.Time"),NumberTools.formatNumberMax(stopTime)),position+2);
			break;
		case STOP_BY_QUANTITY:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.StopCondition"),String.format(Language.tr("ModelDescription.FlowData.StopCondition.Quantity"),NumberTools.formatNumberMax(stopQuantity)),position+2);
			break;
		case STOP_BY_SIGNAL:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.FlowData.StopCondition"),String.format(Language.tr("ModelDescription.FlowData.StopCondition.Signal"),stopSignal),position+2);
			break;
		}
	}
}
