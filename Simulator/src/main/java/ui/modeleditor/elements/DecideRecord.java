/**
 * Copyright 2025 Alexander Herzog
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Hält die Konfiguration für die Verzweigung an einer Verzweigung-, Multi-Teleport- oder Multi-Typzuweisung-Station vor.
 * @see ModelElementDecide
 * @see ModelElementDecideAndTeleport
 * @see ModelElementAssignMulti
 * @author Alexander Herzog
 */
public class DecideRecord {
	/**
	 * Art der Verzweigung
	 * @see #getMode()
	 * @see #setMode(DecideMode)
	 */
	public enum DecideMode {
		/** Zufällig */
		MODE_CHANCE(
				()->Language.tr("Surface.Decide.ByChance"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByChance"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByChance",text)	),
		/** Gemäß Bedingung */
		MODE_CONDITION(
				()->Language.tr("Surface.Decide.ByCondition"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByCondition"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByCondition",text)),
		/** Nach Kundentyp */
		MODE_CLIENTTYPE(
				()->Language.tr("Surface.Decide.ByClientType"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByClientType"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByClientType",text)	),
		/** Reihum */
		MODE_SEQUENCE(
				()->Language.tr("Surface.Decide.BySequence"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.BySequence"),
				text->Language.trAll("Surface.Decide.XML.Mode.BySequence",text)),
		/** Kürzeste Warteschlange an der nächsten Station */
		MODE_SHORTEST_QUEUE_NEXT_STATION(
				()->Language.tr("Surface.Decide.ByQueueLength"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByQueueLengthNext"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthNext",text)),
		/** Kürzeste Warteschlange an der nächsten Bedienstation */
		MODE_SHORTEST_QUEUE_PROCESS_STATION(
				()->Language.tr("Surface.Decide.ByQueueLength"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByQueueLengthProcess"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthProcess",text)),
		/** Wenigste Kunden an der nächsten Station */
		MODE_MIN_CLIENTS_NEXT_STATION(
				()->Language.tr("Surface.Decide.ByClientsAtStation"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByClientsAtStationNext"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationNext",text)),
		/** Wenigste Kunden an der nächsten Bedienstation */
		MODE_MIN_CLIENTS_PROCESS_STATION(
				()->Language.tr("Surface.Decide.ByClientsAtStation"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByClientsAtStationProcess"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationProcess",text)),
		/** Längste Warteschlange an der nächsten Station */
		MODE_LONGEST_QUEUE_NEXT_STATION(
				()->Language.tr("Surface.Decide.ByQueueLengthMax"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByQueueLengthNextMax"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthNextMax",text)),
		/** Längste Warteschlange an der nächsten Bedienstation */
		MODE_LONGEST_QUEUE_PROCESS_STATION(
				()->Language.tr("Surface.Decide.ByQueueLengthMax"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByQueueLengthProcessMax"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthProcessMax",text)),
		/** Meiste Kunden an der nächsten Station */
		MODE_MAX_CLIENTS_NEXT_STATION(
				()->Language.tr("Surface.Decide.ByClientsAtStationMax"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByClientsAtStationNextMax"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationNextMax",text)),
		/** Meiste Kunden an der nächsten Bedienstation */
		MODE_MAX_CLIENTS_PROCESS_STATION(
				()->Language.tr("Surface.Decide.ByClientsAtStationMax"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByClientsAtStationProcessMax"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationProcessMax",text)),
		/** Gemäß Wert eines Kundendaten-Textfeldes */
		MODE_KEY_VALUE(
				()->Language.tr("Surface.Decide.ByStringProperty"),
				()->Language.trPrimary("Surface.Decide.XML.Mode.ByStringProperty"),
				text->Language.trAll("Surface.Decide.XML.Mode.ByStringProperty",text));

		/**
		 * Callback für den Anzeigenamen
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Callback für den primären XML-Namen
		 */
		private final Supplier<String> xmlGetter;

		/**
		 * Callback zum Vergleichen eines XML-Bezeichners
		 */
		private  final Predicate<String> xmlTester;

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback für den Anzeigenamen
		 * @param xmlGetter	Callback für den primären XML-Namen
		 * @param xmlTester	Callback zum Vergleichen eines XML-Bezeichners
		 */
		DecideMode(final Supplier<String> nameGetter, final Supplier<String> xmlGetter, final Predicate<String> xmlTester) {
			this.nameGetter=nameGetter;
			this.xmlGetter=xmlGetter;
			this.xmlTester=xmlTester;
		}

		/**
		 * Liefert den Anzeigenamen in der aktuellen Sprache.
		 * @return	Anzeigename in der aktuellen Sprache
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Liefert den primären XML-Namen.
		 * @return	Primärer XML-Name
		 */
		public String getXmlName() {
			return xmlGetter.get();
		}

		/**
		 * Liefert das passende {@code DecideMode}-Element zu einem XML-Namen.
		 * @param xmlName	XML-Name zu dem das Element gesucht werden soll
		 * @return	{@code DecideMode}-Element oder <code>null</code>, wenn der Name zu keinem Element passt
		 */
		public static DecideMode getFromXmlName(final String xmlName) {
			for (var value: values()) if (value.xmlTester.test(xmlName)) return value;
			return null;
		}

		/**
		 * Liefert den per Vorgabe zu verwendenden Verzweigungsmodus.
		 * @return	Vorgabe-Verzweigungsmodus
		 */
		public static DecideMode getDefaultMode() {
			return MODE_CHANCE;
		}
	}

	/**
	 * Wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden?
	 */
	public enum DecideByStationOnTie {
		/** Folgestation unter den gleichguten besten Möglichkeiten zufällig wählen */
		RANDOM(()->Language.tr("Surface.Decide.DecideByStationOnTie.XMLNameRandom"),()->Language.trAll("Surface.Decide.DecideByStationOnTie.XMLNameRandom")),
		/** Erste der gleichguten besten Möglichkeiten wählen */
		FIRST(()->Language.tr("Surface.Decide.DecideByStationOnTie.XMLNameFirst"),()->Language.trAll("Surface.Decide.DecideByStationOnTie.XMLNameFirst")),
		/** Letzte der gleichguten besten Möglichkeiten wählen */
		LAST(()->Language.tr("Surface.Decide.DecideByStationOnTie.XMLNameLast"),()->Language.trAll("Surface.Decide.DecideByStationOnTie.XMLNameLast"));

		/**
		 * Callback, welches den primären Namen des Elements liefert
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Callback, welches alle Namen des Elements liefert
		 */
		private final Supplier<String[]> allNamesGetter;

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback, welches den primären Namen des Elements liefert
		 * @param allNamesGetter	Callback, welches alle Namen des Elements liefert
		 */
		DecideByStationOnTie(final Supplier<String> nameGetter, final Supplier<String[]> allNamesGetter) {
			this.nameGetter=nameGetter;
			this.allNamesGetter=allNamesGetter;
		}

		/**
		 * Liefert den primären Namen des Elements.
		 * @return	Primärer Name des Elements
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Versucht zu einem vorgegebenen Namen den passenden Eintrag zu liefern.
		 * @param name	Name für den der passende Enum-Eintrag geliefert werden soll.
		 * @return	Enum-Eintrag oder Vorgabewert, wenn kein Eintrag zu dem Namen passt
		 */
		public static DecideByStationOnTie byName(final String name) {
			if (name==null) return RANDOM;
			for (DecideByStationOnTie value: values()) {
				final String[] names=value.allNamesGetter.get();
				for (String test: names) if (test.equalsIgnoreCase(name)) return value;
			}
			return RANDOM;
		}
	}

	/**
	 * Verzweigungsmodus
	 * @see #getMode()
	 * @see DecideMode
	 */
	private DecideMode mode;

	/**
	 * Liste der Raten für die Verzweigungen
	 * @see #getRates()
	 * @see DecideMode#MODE_CHANCE
	 */
	private final List<String> rates;

	/**
	 * Liste der Bedingungen für die Verzweigungen
	 * @see #getConditions()
	 * @see DecideMode#MODE_CONDITION
	 */
	private final List<String> conditions;

	/**
	 * Liste der Namen der Kundentypen für die Verzweigungen
	 * @see #getClientTypes()
	 * @see DecideMode#MODE_CLIENTTYPE
	 */
	private final List<List<String>> clientTypes;

	/**
	 * Liste der Vielfachheiten für die Verzweigungen
	 * @see #getMultiplicity()
	 * @see DecideMode#MODE_SEQUENCE
	 */
	private final List<Integer> multiplicity;

	/**
	 * Schlüssel gemäß dessen Werten die Verzweigung erfolgen soll
	 * @see #getKey()
	 * @see #setKey(String)
	 * @see DecideMode#MODE_KEY_VALUE
	 */
	private String key;

	/**
	 * Verzweigungswerte
	 * @see #getValues()
	 * @see DecideMode#MODE_KEY_VALUE
	 */
	private final List<String> values;

	/**
	 * Kann jeweils ein {@link #values}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 * @see #values
	 * @see DecideMode#MODE_KEY_VALUE
	 */
	private boolean multiTextValues;

	/**
	 * Wie soll im Modus kürzeste Warteschlange / geringstes WIP bei Gleichstand zwischen mehreren Ausgängen entschieden werden?
	 */
	private DecideByStationOnTie decideByStationOnTie;

	/**
	 * Sind die Verzweigungs-Modi, die Daten zu Folgestationen benötigen, zulässig?
	 */
	public final boolean allowQueueDecideModes;

	/**
	 * Konstruktor
	 * @param allowQueueDecideModes	Sind die Verzweigungs-Modi, die Daten zu Folgestationen benötigen, zulässig?
	 */
	public DecideRecord(final boolean allowQueueDecideModes) {
		mode=DecideMode.getDefaultMode();
		key="";
		rates=new ArrayList<>();
		conditions=new ArrayList<>();
		values=new ArrayList<>();
		multiTextValues=true;
		decideByStationOnTie=DecideByStationOnTie.RANDOM;
		clientTypes=new ArrayList<>();
		multiplicity=new ArrayList<>();
		this.allowQueueDecideModes=allowQueueDecideModes;
	}

	/**
	 * Copy-Konstruktor
	 * @param copySource	Zu kopierendes Ausgangsobjekt
	 */
	public DecideRecord(final DecideRecord copySource) {
		this(copySource.allowQueueDecideModes);
		copyDataFrom(copySource);
	}

	/**
	 * Vergleicht diesen Datensatz mit einem anderen.
	 * @param otherRecord	Zweiter Verzweigungsdatensatz
	 * @param numberOfDecideOptions	Anzahl an Verzweigungsmöglichkeiten, die existieren
	 * @return	Liefert <code>true</code>, wenn die beiden Datensätze inhaltlich identisch sind
	 */
	public boolean equalsDecideRecord(final DecideRecord otherRecord, final int numberOfDecideOptions) {
		if (otherRecord==null) return false;

		if (otherRecord.mode!=mode) return false;

		if (numberOfDecideOptions==0) return true;

		switch (mode) {
		case MODE_CHANCE:
			if (!Objects.deepEquals(rates,otherRecord.rates)) return false;
			break;
		case MODE_CONDITION:
			if (!Objects.deepEquals(conditions,otherRecord.conditions)) return false;
			break;
		case MODE_CLIENTTYPE:
			final List<List<String>> clientTypes2=otherRecord.clientTypes;
			if (clientTypes.size()!=clientTypes2.size()) return false;
			for (int i=0;i<clientTypes.size();i++) if (!Objects.deepEquals(clientTypes.get(i),clientTypes2.get(i))) return false;
			break;
		case MODE_SEQUENCE:
			if (!Objects.deepEquals(multiplicity,otherRecord.multiplicity)) return false;
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			if (decideByStationOnTie!=otherRecord.decideByStationOnTie) return false;
			break;
		case MODE_KEY_VALUE:
			if (!key.equals(otherRecord.key)) return false;
			if (!Objects.deepEquals(values,otherRecord.values)) return false;
			if (multiTextValues!=otherRecord.multiTextValues) return false;
			break;
		}

		return true;
	}

	/**
	 * Kopiert die Einstellungen aus einem anderen Verzweigungsdatensatz in diesen.
	 * @param copySource	Quelle aus der die Daten für diesen Datensatz übernommen werden sollen
	 */
	public void copyDataFrom(final DecideRecord copySource) {
		mode=copySource.mode;

		switch (mode) {
		case MODE_CHANCE:
			rates.clear();
			rates.addAll(copySource.rates);
			break;
		case MODE_CONDITION:
			conditions.clear();
			conditions.addAll(copySource.conditions);
			break;
		case MODE_CLIENTTYPE:
			clientTypes.clear();
			copySource.clientTypes.stream().map(l->{List<String> l2=new ArrayList<>(); l2.addAll(l); return l2;}).forEach(l->clientTypes.add(l));
			break;
		case MODE_SEQUENCE:
			multiplicity.clear();
			multiplicity.addAll(copySource.multiplicity);
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			/* nichts zu kopieren */
			break;
		case MODE_KEY_VALUE:
			key=copySource.key;
			values.clear();
			values.addAll(copySource.values);
			break;
		}

		multiTextValues=copySource.multiTextValues;

		decideByStationOnTie=copySource.decideByStationOnTie;
	}

	/**
	 * Liste der Listener, die benachrichtigt werden sollen, wenn sich
	 * Eigenschaften, die direkt von diesem Objekt überwacht werden
	 * können, ändern.
	 * @see #addChangeListener(Runnable)
	 * @see #removeChangeListener(Runnable)
	 * @see #fireChanged()
	 */
	private final List<Runnable> changeListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener zu der Liste der Listener, die benachrichtigt
	 * werden sollen, wenn sich Eigenschaften, die direkt von diesem Objekt
	 * überwacht werden können, ändern, hinzu.
	 * @param changeListener	Hinzuzufügender Listener
	 * @return	Liefert <code>true</code>, wenn der Listener nicht bereits in der Liste war und hinzugefügt werden konnte
	 */
	public boolean addChangeListener(final Runnable changeListener) {
		return changeListeners.add(changeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt
	 * werden sollen, wenn sich Eigenschaften, die direkt von diesem Objekt
	 * überwacht werden können, ändern.
	 * @param changeListener	Hinzuzufügender Listener
	 * @return	Liefert <code>true</code>, wenn der Listener in der Liste war und entfern werden konnte
	 */
	public boolean removeChangeListener(final Runnable changeListener) {
		return changeListeners.remove(changeListener);
	}

	/**
	 * Wird aufgerufen, wenn eine direkt von diesem Objekte überwachte Eigenschaft
	 * (d.h. nicht ein Wert in einer Liste) verändert wurde.
	 * @see #changeListeners
	 * @see #addChangeListener(Runnable)
	 * @see #removeChangeListener(Runnable)
	 */
	private void fireChanged() {
		changeListeners.forEach(Runnable::run);
	}

	/**
	 * Gibt an, ob die Verzweigung zufällig (gemäß vorgegebenen Raten), nach Bedingungen oder nach Kundentypen erfolgen soll
	 * @return	Verzweigungsmodus
	 * @see DecideMode
	 */
	public DecideMode getMode() {
		if (mode==null) return DecideMode.getDefaultMode();
		return mode;
	}

	/**
	 * Stellt den Verzweigungsmodus (zufällig gemäß vorgegebenen Raten, nach Bedingungen oder nach Kundentypen) ein.
	 * @param mode	Verzweigungsmodus
	 */
	public void setMode(final DecideMode mode) {
		if (mode==null) this.mode=DecideMode.getDefaultMode(); else this.mode=mode;
		fireChanged();
	}

	/**
	 * Liefert die Raten, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Liste der Raten für die Verzweigungen
	 */
	public List<String> getRates() {
		return rates;
	}

	/**
	 * Liefert die Bedingungen, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Liste der Bedingungen für die Verzweigungen
	 */
	public List<String> getConditions() {
		return conditions;
	}

	/**
	 * Liefert eine Liste aller Kundentypen-Verzweigungen
	 * @return	Liste der Namen der Kundentypen für die Verzweigungen
	 */
	public List<List<String>> getClientTypes() {
		return clientTypes;
	}

	/**
	 * Liefert die Vielfachheiten zur Ansteuerung einzelner Ausgänge im Reihum-Modus.
	 * @return	Vielfachheiten für die einzelnen Ausgänge
	 */
	public List<Integer> getMultiplicity() {
		return multiplicity;
	}

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so liefert diese Funktion den Schlüssel
	 * @return	Schlüssel gemäß dessen Werten die Verzweigung erfolgen soll
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so kann über diese Funktion der Schlüssel eingestellt werden
	 * @param key	Schlüssel gemäß dessen Werten die Verzweigung erfolgen soll
	 */
	public void setKey(final String key) {
		if (key!=null) this.key=key;
		fireChanged();
	}

	/**
	 * Werte anhand denen die Verzweigung erfolgen soll
	 * @return	Verzweigungswerte
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Kann jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 * @return	Kann jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 */
	public boolean isMultiTextValues() {
		return multiTextValues;
	}

	/**
	 * Stellt ein, ob jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten darf.
	 * @param multiTextValues	Kann jeweils ein {@link #getValues()}-Eintrag mehrere, durch ";" getrennte Werte enthalten?
	 */
	public void setMultiTextValues(boolean multiTextValues) {
		this.multiTextValues=multiTextValues;
		fireChanged();
	}

	/**
	 * Liefert die Einstellung, wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden soll.
	 * @return	Wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden?
	 * @see DecideByStationOnTie
	 */
	public DecideByStationOnTie getDecideByStationOnTie() {
		return (decideByStationOnTie==null)?DecideByStationOnTie.RANDOM:decideByStationOnTie;
	}

	/**
	 * Stellt ein, wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden soll.
	 * @param decideByStationOnTie	Wie soll bei Gleichstand zwischen mehreren Ausgängen entschieden werden?
	 * @see DecideByStationOnTie
	 */
	public void setDecideByStationOnTie(DecideByStationOnTie decideByStationOnTie) {
		this.decideByStationOnTie=(decideByStationOnTie==null)?DecideByStationOnTie.RANDOM:decideByStationOnTie;
		fireChanged();
	}

	/**
	 * Speichert die Basis-Eigenschaften des Datensatzes als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXml(final Document doc, final Element node) {
		Element sub;

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Decide.XML.Mode")));
		sub.setTextContent(mode.getXmlName());

		if (mode==DecideMode.MODE_KEY_VALUE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Decide.XML.Key")));
			sub.setTextContent(key);
			sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Key.MultiTextValues"),multiTextValues?"1":"0");
		}

		if (mode==DecideMode.MODE_MIN_CLIENTS_NEXT_STATION || mode==DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION || mode==DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION || mode==DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION ||
				mode==DecideMode.MODE_MAX_CLIENTS_NEXT_STATION || mode==DecideMode.MODE_LONGEST_QUEUE_NEXT_STATION || mode==DecideMode.MODE_MAX_CLIENTS_PROCESS_STATION || mode==DecideMode.MODE_LONGEST_QUEUE_PROCESS_STATION) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Decide.DecideByStationOnTie.XMLName")));
			sub.setTextContent(decideByStationOnTie.getName());
		}
	}


	/**
	 * Speichert die Eigenschaften zu einer Verzweigungsoption als Attribute eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param sub	xml-Knoten, in dessen Attributen die Daten des Objekts gespeichert werden sollen
	 * @param nr	0-basierte Nummer der zu speichernden Verzweigung
	 * @param isLastNr Handelt es sich dabei um die letzte mögliche Option?
	 */
	public void saveToXmlOutput(final Document doc, final Element sub, final int nr, final boolean isLastNr) {
		switch (mode) {
		case MODE_CHANCE:
			sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Rate"),rates.get(nr));
			break;
		case MODE_CONDITION:
			if (!isLastNr) {
				String condition=(nr>=conditions.size())?"":conditions.get(nr);
				if (condition==null) condition="";
				sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Condition"),condition);
			}
			break;
		case MODE_CLIENTTYPE:
			if (!isLastNr) {
				String clientType;
				if (nr>=clientTypes.size()) {
					clientType="";
				} else {
					clientType=String.join(";",clientTypes.get(nr).stream().map(s->s.replace(";","\\;")).toArray(String[]::new));
				}
				sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.ClientType"),clientType);
			}
			break;
		case MODE_SEQUENCE:
			sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Multiplicity"),""+multiplicity.get(nr));
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			/* nichts zu speichern */
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			/* nichts zu speichern */
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			/* nichts zu speichern */
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			/* nichts zu speichern */
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			/* nichts zu speichern */
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			/* nichts zu speichern */
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			/* nichts zu speichern */
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			/* nichts zu speichern */
			break;
		case MODE_KEY_VALUE:
			if (!isLastNr) {
				String value=(nr>=values.size())?"":values.get(nr);
				sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Value"),value);
			}
			break;
		}
	}

	/**
	 * Lädt die Basis-Eigenschaften des Datensatzes aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXml(final String name, final String content, final Element node) {
		if (Language.trAll("Surface.Decide.XML.Mode",name)) {
			boolean ok=false;
			if (Language.trAll("Surface.Decide.XML.Mode.ByChance",content)) {mode=DecideMode.MODE_CHANCE; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByCondition",content)) {mode=DecideMode.MODE_CONDITION; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByClientType",content)) {mode=DecideMode.MODE_CLIENTTYPE; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.BySequence",content)) {mode=DecideMode.MODE_SEQUENCE; ok=true;}
			if (allowQueueDecideModes) {
				if (Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthNext",content)) {mode=DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthProcess",content)) {mode=DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationNext",content)) {mode=DecideMode.MODE_MIN_CLIENTS_NEXT_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationProcess",content)) {mode=DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthNextMax",content)) {mode=DecideMode.MODE_LONGEST_QUEUE_NEXT_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthProcessMax",content)) {mode=DecideMode.MODE_LONGEST_QUEUE_PROCESS_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationNextMax",content)) {mode=DecideMode.MODE_MAX_CLIENTS_NEXT_STATION; ok=true;}
				if (Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationProcessMax",content)) {mode=DecideMode.MODE_MAX_CLIENTS_PROCESS_STATION; ok=true;}
			}
			if (Language.trAll("Surface.Decide.XML.Mode.ByStringProperty",content)) {mode=DecideMode.MODE_KEY_VALUE; ok=true;}
			if (!ok) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.Decide.XML.Key",name)) {
			key=content;
			final String strMultiTextValues=Language.trAllAttribute("Surface.Decide.XML.Key.MultiTextValues",node);
			if (strMultiTextValues.equals("0")) multiTextValues=false; else multiTextValues=true;
			return null;
		}

		if (Language.trAll("Surface.Decide.DecideByStationOnTie.XMLName",name)) {
			decideByStationOnTie=DecideByStationOnTie.byName(content);
			return null;
		}

		return null;
	}

	/**
	 * Lädt die Eigenschaften zu einer Verzweigungsoption aus den Attributen einzelnen xml-Elements.
	 * @param name	Name des xml-Elements
	 * @param node	xml-Element, aus dessen Attributen die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadOptionFromXml(final String name, final Element node) {
		/* Chance */
		final String rateString=Language.trAllAttribute("Surface.Decide.XML.Connection.Rate",node);
		if (!rateString.isEmpty()) {
			rates.add(rateString);
		} else {
			rates.add("1");
		}

		/* Condition */
		conditions.add(Language.trAllAttribute("Surface.Decide.XML.Connection.Condition",node));

		/* ClientType */
		final String line=Language.trAllAttribute("Surface.Decide.XML.Connection.ClientType",node);
		final List<String> list=new ArrayList<>();
		final StringBuilder type=new StringBuilder();
		for (int i=0;i<line.length();i++) {
			final char c=line.charAt(i);
			if (c==';') {
				if (i>0 && line.charAt(i-1)=='\\') {
					type.setLength(type.length()-1);
					type.append(';');
				} else {
					if (type.length()>0) list.add(type.toString());
					type.setLength(0);
				}
			} else {
				type.append(c);
			}
		}
		if (type.length()>0) list.add(type.toString());
		clientTypes.add(list);

		/* Sequence */
		final String multiplicityString=Language.trAllAttribute("Surface.Decide.XML.Connection.Multiplicity",node);
		if (multiplicityString.isEmpty()) {
			multiplicity.add(1);
		} else {
			final Long multiplicity=NumberTools.getPositiveLong(multiplicityString);
			if (multiplicity==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Decide.XML.Connection.Multiplicity"),name,node.getParentNode().getNodeName());
			this.multiplicity.add(multiplicity.intValue());
		}

		/* Key=Value */
		values.add(Language.trAllAttribute("Surface.Decide.XML.Connection.Value",node));

		return null;
	}

	/**
	 * Sucht einen Text in dem Datensatz.
	 * @param element	Zugehörige Station
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	public void search(final ModelElementBox element, final FullTextSearch searcher) {
		switch (mode) {
		case MODE_CHANCE:
			for (int i=0;i<rates.size();i++) {
				final int index=i;
				searcher.testString(element,Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1),rates.get(i),newRate->rates.set(index,newRate));
			}
			break;
		case MODE_CONDITION:
			for (int i=0;i<conditions.size();i++) {
				final int index=i;
				searcher.testString(element,Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1),conditions.get(i),newCondition->conditions.set(index,newCondition));
			}
			break;
		case MODE_CLIENTTYPE:
			for (int i=0;i<clientTypes.size();i++) {
				final int index1=i;
				for (int j=0;j<clientTypes.get(i).size();j++) {
					final int index2=j;
					searcher.testString(element,Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1),clientTypes.get(i).get(j),newClientType->clientTypes.get(index1).set(index2,newClientType));
				}
			}
			break;
		case MODE_SEQUENCE:
			for (int i=0;i<multiplicity.size();i++) {
				final int index=i;
				searcher.testInteger(element,Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1),multiplicity.get(i),newMul->multiplicity.set(index,newMul));
			}
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			/* Keine Konfiguration */
			break;
		case MODE_KEY_VALUE:
			searcher.testString(element,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key"),key,newKey->{key=newKey;});
			for (int i=0;i<values.size();i++) {
				final int index=i;
				searcher.testString(element,Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1),values.get(i),newValue->values.set(index,newValue));
			}
			break;
		}
	}

	/**
	 * Benennt einen Kundentyp, sofern dieser in dem Datensatz vorkommt, um.
	 * @param oldName	Alter Kundentypname
	 * @param newName	Neuer Kundentypname
	 * @return	Liefert <code>true</code>, wenn mindestens eine Umbenennung vorgekommen ist
	 */
	public boolean renameClientType(final String oldName, final String newName) {
		if (mode!=DecideMode.MODE_CLIENTTYPE) return false;

		boolean renamed=false;
		for (int i=0;i<clientTypes.size();i++) for (int j=0;j<clientTypes.get(i).size();j++) if (clientTypes.get(i).get(j).equals(oldName)) {
			clientTypes.get(i).set(j,newName);
			renamed=true;
		}
		return renamed;
	}
}
