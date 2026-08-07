/**
 * Copyright 2026 Alexander Herzog
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
 * Hält einen Signaldatensatz für ein {@link ModelElementSignalMulti}-Element vor.
 * @see ModelElementSignalMulti
 */
public class ModelElementSignalMultiRecord {
	/**
	 * Name des Signals
	 */
	private String name="";

	/**
	 * Optionale verzögerte Auslösung des Signals (in Sekunden)
	 */
	private double signalDelay=0;

	/**
	 * Zusätzliche optionale Bedingung, die für die Zuweisung erfüllt sein muss (kann <code>null</code> sein)
	 * @see #getCondition()
	 * @see #setCondition(String)
	 */
	private String condition="";

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementSignalMultiRecord() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Konstruktor
	 * @param name	Name des neuen Signals
	 */
	public ModelElementSignalMultiRecord(final String name) {
		setName(name);
	}

	/**
	 * Copy-Konstruktor
	 * @param copySource	Zu kopierender Datensatz
	 */
	public ModelElementSignalMultiRecord(final ModelElementSignalMultiRecord copySource) {
		copyFrom(copySource);
	}

	/**
	 * Liefert den Sekundenwert der optionalen verzögerten Auslösung des Signals.
	 * @return	Sekundenwert der optionalen verzögerten Auslösung des Signals
	 */
	public double getSignalDelay() {
		return Math.max(0,signalDelay);
	}

	/**
	 * Liefert den Namen des Signals.
	 * @return	Name des Signals
	 */

	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen des Signals ein.
	 * @param name	Name des Signals
	 */
	public void setName(String name) {
		this.name=(name==null)?"":name;
	}

	/**
	 * Stellt einen Sekundenwert für eine optionale verzögerte Auslösung des Signals ein.
	 * @param signalDelay	Sekundenwert der optionalen verzögerten Auslösung des Signals
	 */
	public void setSignalDelay(double signalDelay) {
		this.signalDelay=Math.max(0,signalDelay);
	}

	/**
	 * Liefert die optionale Bedingung, die für die Signalauslösung erfüllt sein muss.
	 * @return	Bedingung, die für die Signalauslösung erfüllt sein muss (kann <code>null</code> sein)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung, die für die Signalauslösung erfüllt sein muss, ein.
	 * @param condition	Optionale Bedingung, die für die Signalauslösung erfüllt sein muss (kann <code>null</code> sein oder leer sein)
	 */
	public void setCondition(final String condition) {
		this.condition=(condition==null)?"":condition;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Datensatz auf diesen.
	 * @param copySource	Datensatz, von dem alle Einstellungen übernommen werden sollen
	 */
	public void copyFrom(final ModelElementSignalMultiRecord copySource) {
		if (copySource==null) return;
		this.name=copySource.name;
		this.signalDelay=copySource.signalDelay;
		this.condition=copySource.condition;
	}

	/**
	 * Vergleicht diesen Datensatz mit einem weiteren
	 * @param otherRecord	Zweiter Datensatz, der mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn beide Datensätze inhaltlich übereinstimmen
	 */
	public boolean equalsRecord(final ModelElementSignalMultiRecord otherRecord) {
		if (otherRecord==null) return false;

		if (!name.equals(otherRecord.name)) return false;
		if (signalDelay!=otherRecord.signalDelay) return false;
		if (!condition.equals(otherRecord.condition)) return false;

		return true;
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void addPropertiesDataToXML(final Document doc, final Element parent) {
		final var node=doc.createElement(Language.trPrimary("Surface.MultiSignal.XML.Signal"));
		parent.appendChild(node);

		node.setAttribute(Language.trPrimary("Surface.MultiSignal.XML.SignalName"),name);

		if (signalDelay>0) {
			node.setAttribute(Language.trPrimary("Surface.MultiSignal.XML.SignalDelay"),NumberTools.formatSystemNumber(signalDelay));
		}

		if (!condition.isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.MultiSignal.XML.SignalCondition"),condition);
		}
	}

	/**
	 * Lädt eine Einstellungen des Datensatzes aus einem xml-Element.
	 * @param node	xml-Element, aus dem die Daten geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		name=Language.trAllAttribute("Surface.MultiSignal.XML.SignalName",node);

		final String delayString=Language.trAllAttribute("Surface.MultiSignal.XML.SignalDelay",node);
		if (!delayString.isBlank()) {
			final Double D=NumberTools.getNotNegativeDouble(delayString);
			if (D==null) return String.format(Language.tr("Surface.MultiSignal.XML.SignalDelay.Error"),name,node.getParentNode().getNodeName());
			signalDelay=D;
			return null;
		}

		condition=Language.trAllAttribute("Surface.MultiSignal.XML.SignalCondition",node);

		return null;
	}
}
