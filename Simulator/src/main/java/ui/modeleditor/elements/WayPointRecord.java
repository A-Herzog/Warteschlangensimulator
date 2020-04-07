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

/**
 * Diese Klasse kapselt einen einzelnen Eintrag in der Liste,
 * wofür ein {@link ModelElementWayPoint}-Element als
 * Wegpunkt fungieren soll.
 * @author Alexander Herzog
 * @see ModelElementWayPoint
 */
public class WayPointRecord {
	private String stationA;
	private String stationB;
	private int index;

	/**
	 * Konstruktor der Klasse
	 * @param stationA	Startstation der Transporter, die diesen Wegpunkt passieren sollen
	 * @param stationB	Zielstation der Transporter, die diesen Wegpunkt passieren sollen
	 * @param index	Reihenfolge zur Ansteuerung dieses Wegspunkts auf dem Weg von der Start- zur Zielstation
	 */
	public WayPointRecord(final String stationA, final String stationB, final int index) {
		this.stationA=(stationA!=null)?stationA:"";
		this.stationB=(stationB!=null)?stationB:"";
		this.index=Math.max(0,index);
	}

	/**
	 * Konstruktor der Klasse
	 */
	public WayPointRecord() {
		this(null,null,0);
	}

	/**
	 * Konstruktor der Klasse
	 * @param copyFrom	Anderer Datensatz, von dem die Daten kopiert werden sollen
	 */
	public WayPointRecord(final WayPointRecord copyFrom) {
		this(copyFrom.stationA,copyFrom.stationB,copyFrom.index);
	}

	/**
	 * Vergleich diesen Wegpunkt-Datensatz mit einem anderen Wegpunkt-Datensatz
	 * @param otherWayPointRecord	Anderer Wegpunkt-Datensatz, der mit diesem Wegpunkt-Datensatz verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Wegpunkt-Datensätze inhaltlich identisch sind
	 */
	public boolean equalsWayPointRecord(final WayPointRecord otherWayPointRecord) {
		if (otherWayPointRecord==null) return false;
		if (!otherWayPointRecord.stationA.equals(stationA)) return false;
		if (!otherWayPointRecord.stationB.equals(stationB)) return false;
		if (otherWayPointRecord.index!=index)return false;
		return true;
	}

	/**
	 * Liefert die Startstation der Transporter, die diesen Wegpunkt passieren sollen.
	 * @return	Startstation der Transporter, die diesen Wegpunkt passieren sollen
	 */
	public String getStationA() {
		return stationA;
	}

	/**
	 * Liefer die Zielstation der Transporter, die diesen Wegpunkt passieren sollen
	 * @return	Zielstation der Transporter, die diesen Wegpunkt passieren sollen
	 */
	public String getStationB() {
		return stationB;
	}

	/**
	 * Liefer den Reihenfolgeindex zur Ansteuerung dieses Wegpunktes auf dem Weg von der Start- zur Zielstation
	 * @return	Reihenfolgeindex zur Ansteuerung dieses Wegpunktes auf dem Weg von der Start- zur Zielstation
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Stelt die Daten für diesen Wegpunkt-Datensatz ein
	 * @param stationA	Startstation der Transporter, die diesen Wegpunkt passieren sollen
	 * @param stationB	Zielstation der Transporter, die diesen Wegpunkt passieren sollen
	 * @param index	Reihenfolge zur Ansteuerung dieses Wegpunktes auf dem Weg von der Start- zur Zielstation
	 */
	public void set(final String stationA, final String stationB, final int index) {
		this.stationA=(stationA!=null)?stationA:"";
		this.stationB=(stationB!=null)?stationB:"";
		this.index=Math.max(0,index);
	}

	/**
	 * Stellt die Startstation der Transporter, die diesen Wegpunkt passieren sollen, ein.
	 * @param stationA	Startstation der Transporter, die diesen Wegpunkt passieren sollen
	 */
	public void setStationA(final String stationA) {
		this.stationA=(stationA!=null)?stationA:"";
	}

	/**
	 * Stellt die Zielstation der Transporter, die diesen Wegpunkt passieren sollen, ein.
	 * @param stationB	Zielstation der Transporter, die diesen Wegpunkt passieren sollen
	 */
	public void setStationB(final String stationB) {
		this.stationB=(stationB!=null)?stationB:"";
	}

	/**
	 * Stellt die Reihenfolge zur Ansteuerung dieses Wegpunktes auf dem Weg von der Start- zur Zielstation ein.
	 * @param index	Reihenfolge zur Ansteuerung dieses Wegpunktes auf dem Weg von der Start- zur Zielstation
	 */
	public void setIndex(final int index) {
		this.index=Math.max(0,index);
	}

	/**
	 * Speichert diesen Wegpunkt-Datensatz in einem XML-Dokument
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void addToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(Language.tr("Surface.WayPoint.XML.Record"));
		parent.appendChild(node);
		node.setAttribute(Language.tr("Surface.WayPoint.XML.Record.StationFrom"),stationA);
		node.setAttribute(Language.tr("Surface.WayPoint.XML.Record.StationTo"),stationB);
		node.setTextContent(""+index);
	}

	/**
	 * Versucht die Daten eines Wegpunkt-Datensatz aus einem XML-Knoten zu lasen
	 * @param node	XML-Knoten aus dem die Daten des Wegpunkt-Datensatz geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		stationA=Language.trAllAttribute("Surface.WayPoint.XML.Record.StationFrom",node);
		if (stationA.isEmpty()) return Language.tr("Surface.WayPoint.XML.Record.StationFrom.ErrorEmpty");
		stationB=Language.trAllAttribute("Surface.WayPoint.XML.Record.StationTo",node);
		if (stationB.isEmpty()) return Language.tr("Surface.WayPoint.XML.Record.StationTo.ErrorEmpty");

		final Integer I=NumberTools.getNotNegativeInteger(node.getTextContent());
		if (I==null) return String.format(Language.tr("Surface.WayPoint.XML.Record.InvalidIndex"),node.getTextContent());
		index=I.intValue();

		return null;
	}
}