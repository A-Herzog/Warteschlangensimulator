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

import java.util.function.Supplier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse kapselt einen einzelnen Batch-Bildungs-Datensatz.
 * @author Alexander Herzog
 * @see ModelElementBatch
 * @see ModelElementBatchMulti
 */
public class BatchRecord implements Cloneable {
	/**
	 * Art der Batch-Bildung
	 * @author Alexander Herzog
	 * @see BatchRecord#getBatchMode()
	 * @see BatchRecord#setBatchMode(BatchMode)
	 */
	public enum BatchMode {
		/** Sammelt die Kunden und leitet sie dann gemeinsam weiter (kein Batching im engeren Sinne). */
		BATCH_MODE_COLLECT,

		/** Fasst die Kunden zu einem temporären Batch zusammen. */
		BATCH_MODE_TEMPORARY,

		/** Fasst die Kunden zu einem neuen Kunden zusammen (permanentes Batching). */
		BATCH_MODE_PERMANENT
	}

	/**
	 * Feste oder variable Batch-Größe
	 * @author Alexander Herzog
	 * @see BatchRecord#getBatchSizeMode()
	 * @see BatchRecord#setBatchSizeMode(BatchSizeMode)
	 */
	public enum BatchSizeMode {
		/** Feste Batch-Größe */
		FIXED,
		/** Variable Batch-Größe */
		RANGE
	}

	/** Feste Batch-Größe */
	private String batchSizeFixed;
	/** Minimale Batch-Größe im Fall einer variablen Batch-Größe */
	private String batchSizeMin;
	/** Maximale Batch-Größe im Fall einer variablen Batch-Größe */
	private String batchSizeMax;
	/** Art der Batch-Bildung */
	private BatchMode batchMode;
	/** Feste oder variable Batch-Größe */
	private BatchSizeMode batchSizeMode;
	/** Neuer Kundentyp (sofern die Kunden nicht einzeln weitergeleitet werden sollen) */
	private String newClientType;

	/**
	 * Verschiedene Modi, wie Zeiten und Kundendaten von den Ausgangskunden auf den neuen Batch-Kunden übertragen werden sollen
	 */
	public enum DataTransferMode {
		/** Modus: Nicht übertragen */
		OFF(()->Language.trPrimary("Surface.Batch.XML.TransferMode.Off"),()->Language.trAll("Surface.Batch.XML.TransferMode.Off")),
		/** Modus: Minimalwert der Einzelwerte verwenden */
		MIN(()->Language.trPrimary("Surface.Batch.XML.TransferMode.Min"),()->Language.trAll("Surface.Batch.XML.TransferMode.Min")),
		/** Modus: Maximalwert der Einzelwerte verwenden */
		MAX(()->Language.trPrimary("Surface.Batch.XML.TransferMode.Max"),()->Language.trAll("Surface.Batch.XML.TransferMode.Max")),
		/** Modus: Durchschnitt der Einzelwerte verwenden */
		MEAN(()->Language.trPrimary("Surface.Batch.XML.TransferMode.Mean"),()->Language.trAll("Surface.Batch.XML.TransferMode.Mean")),
		/** Modus: Summe der Einzelwerte verwenden */
		SUM(()->Language.trPrimary("Surface.Batch.XML.TransferMode.Sum"),()->Language.trAll("Surface.Batch.XML.TransferMode.Sum")),
		/** Modus: Produkt der Einzelwerte verwenden */
		MULTIPLY(()->Language.trPrimary("Surface.Batch.XML.TransferMode.Multiply"),()->Language.trAll("Surface.Batch.XML.TransferMode.Multiply"));

		/**
		 * Callback zur Ermittlung des primären XML-Namens für den Modus
		 */
		private final Supplier<String> getNameCallback;

		/**
		 * Callback zur Ermittlung aller XML-Namens für den Modus
		 */
		private final Supplier<String[]> getNamesCallback;

		/**
		 * Konstruktor des Enum
		 * @param getName	Callback zur Ermittlung des primären XML-Namens für den Modus
		 * @param getNames	Callback zur Ermittlung aller XML-Namens für den Modus
		 */
		DataTransferMode(final Supplier<String> getName, Supplier<String[]> getNames) {
			getNameCallback=getName;
			getNamesCallback=getNames;
		}

		/**
		 * Liefert den primären XML-Namen für den Modus.
		 * @return	Primärer XML-Namen für den Modus
		 */
		public String getName() {
			return getNameCallback.get();
		}

		/**
		 * Liefert zu einem XML-Namen der zugehörigen Modus (oder <code>OFF</code> als Fallback-Wert)
		 * @param name	XML-Name zu dem der Modus ermittelt werden soll
		 * @return	Modus zu dem XML-Namen
		 */
		public static DataTransferMode byName(final String name) {
			for (DataTransferMode mode: values()) for(String testName: mode.getNamesCallback.get()) if (testName.equalsIgnoreCase(name)) return mode;
			return DataTransferMode.OFF;
		}
	}

	/**
	 * Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 * @see #getTransferTimes()
	 * @see #setTransferTimes(DataTransferMode)
	 */
	private DataTransferMode transferTimes;

	/**
	 * Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 * @see #getTransferNumbers()
	 * @see #setTransferNumbers(DataTransferMode)
	 */
	private DataTransferMode transferNumbers;

	/**
	 * Konstruktor der Klasse
	 */
	public BatchRecord() {
		batchSizeMin="1";
		batchSizeMax="1";
		batchSizeFixed="1";
		batchMode=BatchMode.BATCH_MODE_COLLECT;
		batchSizeMode=BatchSizeMode.FIXED;
		newClientType="";
		transferTimes=DataTransferMode.OFF;
		transferNumbers=DataTransferMode.OFF;
	}

	/**
	 * Überprüft, ob der Datensatz mit dem angegebenen Datensatz inhaltlich identisch ist.
	 * @param otherBatchRecord	Anderer Datensatz mit dem dieser Datensatz verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Datensätze identisch sind.
	 */
	public boolean equalsBatchRecord(final BatchRecord otherBatchRecord) {
		if (otherBatchRecord==null) return false;

		if (batchMode!=otherBatchRecord.batchMode) return false;
		if (batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) {
			if (!otherBatchRecord.newClientType.equals(newClientType)) return false;
		}
		if (batchSizeMode!=otherBatchRecord.batchSizeMode) return false;

		switch (batchSizeMode) {
		case FIXED:
			if (!batchSizeFixed.equals(otherBatchRecord.batchSizeFixed)) return false;
			break;
		case RANGE:
			if (!batchSizeMin.equals(otherBatchRecord.batchSizeMin)) return false;
			if (!batchSizeMax.equals(otherBatchRecord.batchSizeMax)) return false;
			break;
		}

		if (transferTimes!=otherBatchRecord.transferTimes) return false;
		if (transferNumbers!=otherBatchRecord.transferNumbers) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param copySource	Datensatz, von dem alle Einstellungen übernommen werden sollen
	 */
	public void copyDataFrom(final BatchRecord copySource) {
		if (copySource==null) return;

		batchMode=copySource.batchMode;
		if (batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) {
			newClientType=copySource.newClientType;
		} else {
			newClientType="";
		}
		batchSizeMode=copySource.batchSizeMode;
		batchSizeFixed=copySource.batchSizeFixed;
		batchSizeMin=copySource.batchSizeMin;
		batchSizeMax=copySource.batchSizeMax;

		transferTimes=copySource.transferTimes;
		transferNumbers=copySource.transferNumbers;
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @return	Kopiertes Element
	 */
	@Override
	public BatchRecord clone() {
		final BatchRecord batchRecord=new BatchRecord();
		batchRecord.copyDataFrom(this);
		return batchRecord;
	}

	/**
	 * Liefert den aktuell gewählten Batch-Modus.
	 * @return	Aktueller Batch-Modus
	 * @see BatchMode
	 */
	public BatchMode getBatchMode() {
		return batchMode;
	}

	/**
	 * Stellt den Batch-Modus ein.
	 * @param batchMode	Neuer Batch-Modus
	 * @see BatchMode
	 */
	public void setBatchMode(BatchMode batchMode) {
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			this.batchMode=BatchMode.BATCH_MODE_COLLECT;
			newClientType="";
			break;
		case BATCH_MODE_TEMPORARY:
			this.batchMode=BatchMode.BATCH_MODE_TEMPORARY;
			break;
		case BATCH_MODE_PERMANENT:
			this.batchMode=BatchMode.BATCH_MODE_PERMANENT;
			break;
		}
	}

	/**
	 * Liefert den Kundentyp, unter dem die zusammengefassten Kunden weitergeleitet werden sollen.
	 * @return	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public String getNewClientType() {
		return newClientType;
	}

	/**
	 * Stellt den Kundentyp ein, unter dem die zusammengefassten Kunden weitergeleitet werden sollen.
	 * @param newClientType	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public void setNewClientType(final String newClientType) {
		if (newClientType==null || newClientType.isBlank()) {
			this.newClientType="";
			batchMode=BatchMode.BATCH_MODE_COLLECT;
		} else {
			this.newClientType=newClientType;
			if (batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
		}
	}

	/**
	 * Liefert den eingestellten Batch-Größen-Modus (einzelner Wert oder Bereich).
	 * @return	Batch-Größen-Modus
	 */
	public BatchSizeMode getBatchSizeMode() {
		return batchSizeMode;
	}

	/**
	 * Stellt den eingestellten Batch-Größen-Modus (einzelner Wert oder Bereich) ein.
	 * @param batchSizeMode	Batch-Größen-Modus
	 */
	public void setBatchSizeMode(BatchSizeMode batchSizeMode) {
		this.batchSizeMode=batchSizeMode;
	}


	/**
	 * Liefert die aktuelle Batch-Größe (im Modus fester Batch-Größe).
	 * @return	Batch-Größe
	 * @see BatchSizeMode
	 */
	public String getBatchSizeFixed() {
		return batchSizeFixed;
	}

	/**
	 * Stellt die Batch-Größe ein (im Modus fester Batch-Größe).
	 * @param batchSizeFixed	Neue Batch-Größe
	 * @see BatchSizeMode
	 */
	public void setBatchSizeFixed(final String batchSizeFixed) {
		this.batchSizeFixed=(batchSizeFixed==null)?"1":batchSizeFixed;
	}


	/**
	 * Liefert die aktuelle minimale Batch-Größe (im Modus variabler Batch-Größe).
	 * @return Minimale Batch-Größe
	 * @see BatchSizeMode
	 */
	public String getBatchSizeMin() {
		return batchSizeMin;
	}

	/**
	 * Liefert die aktuelle maximale Batch-Größe (im Modus variabler Batch-Größe).
	 * @return Maximale Batch-Größe
	 * @see BatchSizeMode
	 */
	public String getBatchSizeMax() {
		return batchSizeMax;
	}

	/**
	 * Stellt die minimale Batch-Größe ein (im Modus variabler Batch-Größe).
	 * @param batchSizeMin	Neue minimale Batch-Größe
	 * @see BatchSizeMode
	 */
	public void setBatchSizeMin(final String batchSizeMin) {
		this.batchSizeMin=(batchSizeMin==null)?"0":batchSizeMin;
	}

	/**
	 * Stellt die maximale Batch-Größe ein (im Modus variabler Batch-Größe).
	 * @param batchSizeMax	Neue maximale Batch-Größe
	 * @see BatchSizeMode
	 */
	public void setBatchSizeMax(final String batchSizeMax) {
		this.batchSizeMax=(batchSizeMax==null)?"1":batchSizeMax;
	}

	/**
	 * Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 * @return	Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 */
	public DataTransferMode getTransferTimes() {
		return transferTimes;
	}

	/**
	 * Stellt ein, wie die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden sollen.
	 * @param transferTimes	Wie sollen die Zeiten der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 */
	public void setTransferTimes(final DataTransferMode transferTimes) {
		this.transferTimes=(transferTimes==null)?DataTransferMode.OFF:transferTimes;
	}

	/**
	 * Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 * @return	Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 */
	public DataTransferMode getTransferNumbers() {
		return transferNumbers;
	}

	/**
	 * Stellt ein, wie die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden sollen.
	 * @param transferNumbers	Wie sollen die numerischen Datenfelder der Einzelkunden bei der Batch-Bildung auf den neuen Batch-Kunden übertragen werden?
	 */
	public void setTransferNumbers(final DataTransferMode transferNumbers) {
		this.transferNumbers=(transferNumbers==null)?DataTransferMode.OFF:transferNumbers;
	}

	/**
	 * Speichert die Einstellungen des Datensatzes als Untereinträge eines xml-Knotens.
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 * @see #loadProperty(String, String, Element)
	 */
	public void addDataToXML(final Document doc, final Element node) {
		Element sub;

		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
			sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Collect"));
			break;
		case BATCH_MODE_TEMPORARY:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Temporary"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Collect"));
			}
			break;
		case BATCH_MODE_PERMANENT:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Permanent"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Collect"));
			}
			break;
		}

		switch (batchSizeMode) {
		case FIXED:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.Batch")));
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.Size"),batchSizeFixed);
			break;
		case RANGE:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.Batch")));
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.SizeMin"),batchSizeMin);
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.SizeMax"),batchSizeMax);
			break;
		}

		if (transferTimes!=DataTransferMode.OFF) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.TransferTimes")));
			sub.setTextContent(transferTimes.getName());
		}

		if (transferNumbers!=DataTransferMode.OFF) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.TransferNumbers")));
			sub.setTextContent(transferNumbers.getName());
		}
	}

	/**
	 * Speichert die Einstellungen des Datensatzes als Untereinträge eines xml-Knotens.<br>
	 * Die Daten werden dabei in ein Zwischenelement, welches als Attribut einen Namen besitzt, geschrieben.
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 * @param recordName	Name der im Attribut im Zwischenelement verwendet werden soll
	 * @see #loadFromXML(Element)
	 */
	public void addDataToXML(final Document doc, final Element node, final String recordName) {
		Element sub;
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.Record")));
		sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Record.Name"),recordName);
		addDataToXML(doc,sub);
	}

	/**
	 * Lädt eine einzelne Einstellung des Batch-Datensatzes aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 * @see #addDataToXML(Document, Element)
	 */
	public String loadProperty(final String name, final String content, final Element node) {
		if (Language.trAll("Surface.Batch.XML.Batch",name)) {
			String size;

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.SizeMin",node);
			if (!size.isEmpty()) {
				batchSizeMin=size;
				batchSizeMode=BatchSizeMode.RANGE;
			}

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.SizeMax",node);
			if (!size.isEmpty()) {
				batchSizeMax=size;
				batchSizeMode=BatchSizeMode.RANGE;
			}

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.Size",node);
			if (!size.isEmpty()) {
				batchSizeFixed=size;
				batchSizeMode=BatchSizeMode.FIXED;
			}

			return null;
		}

		if (Language.trAll("Surface.Batch.XML.BatchMode",name)) {
			if (Language.trAll("Surface.Batch.XML.BatchMode.Collect",content)) batchMode=BatchMode.BATCH_MODE_COLLECT;
			if (Language.trAll("Surface.Batch.XML.BatchMode.Temporary",content)) batchMode=BatchMode.BATCH_MODE_TEMPORARY;
			if (Language.trAll("Surface.Batch.XML.BatchMode.Permanent",content)) batchMode=BatchMode.BATCH_MODE_PERMANENT;
			return null;
		}

		if (Language.trAll("Surface.Batch.XML.ClientType",name)) {
			newClientType=content;
			if (!content.isBlank() && batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
			return null;
		}

		if (Language.trAll("Surface.Batch.XML.TransferTimes",name)) {
			transferTimes=DataTransferMode.byName(content);
			return null;
		}

		if (Language.trAll("Surface.Batch.XML.TransferNumbers",name)) {
			transferNumbers=DataTransferMode.byName(content);
			return null;
		}

		return null;
	}

	/**
	 * Name für den geladenen Datensatz
	 * @see #loadFromXML(Element)
	 * @see #getLoadedRecordName()
	 */
	private String loadedRecordName="";

	/**
	 * Lädt einen kompletten Batch-Datensatz aus den Untereinträgen eines xml-Elements
	 * @param node	xml-Zwischenelement in dessen Untereinträgen die Daten stehen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 * @see #addDataToXML(Document, Element, String)
	 * @see #getLoadedRecordName()
	 */
	public String loadFromXML(final Element node) {
		if (!Language.trAll("Surface.Batch.XML.Record",node.getNodeName())) return null;

		loadedRecordName=Language.trAllAttribute("Surface.Batch.XML.Record.Name",node);

		final NodeList l=node.getChildNodes();
		final int size=l.getLength();
		for (int i=0; i<size;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			Element e=(Element)sub;
			String error=loadProperty(e.getNodeName(),e.getTextContent(),e);
			if (error!=null) return error;
		}

		return null;
	}

	/**
	 * Liefert den beim Aufruf von {@link #loadFromXML(Element)} mit eingelesenen Namen für den Datensatz.
	 * @return	Name für den Datensatz oder ein leerer String, wenn keine Daten geladen wurden.
	 * @see #loadFromXML(Element)
	 */
	public String getLoadedRecordName() {
		return loadedRecordName;
	}

	/**
	 * Erstellt eine Beschreibung für den Datensatz
	 * @param clientType	Ausgangskundentyp für den dieser Datensatz gilt (kann <code>null</code> oder leer sein, dann erfolgt keine Angabe des Ausgangskundentyps)
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param level	Start-Level für die Ausgabe. Es werden <code>level</code>, <code>level+1</code> und <code>level+2</code> verwendet.
	 */
	public void buildDescription(final String clientType, final ModelDescriptionBuilder descriptionBuilder, final int level) {
		/* Ausgangskundentyp */
		if (clientType!=null && !clientType.isBlank()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.SourceClientType"),clientType,level);
		}

		/* Batching Modus */
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Mode"),Language.tr("ModelDescription.Batch.Mode.Collect"),level);
			break;
		case BATCH_MODE_TEMPORARY:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Mode"),Language.tr("ModelDescription.Batch.Mode.Temporary"),level);
			break;
		case BATCH_MODE_PERMANENT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Mode"),Language.tr("ModelDescription.Batch.Mode.Permanent"),level);
			break;
		}

		/* Batchgröße */
		switch (batchSizeMode) {
		case FIXED:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),batchSizeFixed,level+1);
			break;
		case RANGE:
			if (batchSizeMin.equals(batchSizeMax)) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),batchSizeMin,level+1);
			} else {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),batchSizeMin+".."+batchSizeMax,level+1);
			}
			break;
		}

		/* Neuer Kundentyp */
		if ((batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) && !newClientType.isBlank()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.NewClientType"),newClientType,level+2);
		}

		if (transferTimes!=DataTransferMode.OFF) {
			final String mode;
			switch(transferTimes) {
			case OFF: mode=null; break;
			case MIN: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Min"); break;
			case MAX: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Max"); break;
			case MEAN: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Mean"); break;
			case SUM: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Sum"); break;
			case MULTIPLY: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Multiply"); break;
			default: mode=null; break;
			}
			if (mode!=null) descriptionBuilder.addProperty(Language.tr("Surface.Batch.Dialog.TransferData.Times"),mode,level+3);
		}

		if (transferNumbers!=DataTransferMode.OFF) {
			final String mode;
			switch(transferNumbers) {
			case OFF: mode=null; break;
			case MIN: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Min"); break;
			case MAX: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Max"); break;
			case MEAN: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Mean"); break;
			case SUM: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Sum"); break;
			case MULTIPLY: mode=Language.tr("Surface.Batch.Dialog.TransferData.Mode.Multiply"); break;
			default: mode=null; break;
			}
			if (mode!=null) descriptionBuilder.addProperty(Language.tr("Surface.Batch.Dialog.TransferData.Numbers"),mode,level+3);
		}
	}

	/**
	 * Sucht einen Text in den Daten dieses Datensatzes.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @param clientType	Kundentyp für den dieser Datensatz gilt (kann <code>null</code> sein; dann gilt er für alle Kundentypen)
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station, final String clientType) {
		final String addon;
		if (clientType==null) {
			addon="";
		} else {
			addon=" "+String.format(Language.tr("Editor.DialogBase.Search.ForClientType"),clientType);
		}

		/* Batch-Größe */
		if (batchSizeMode==BatchSizeMode.FIXED) {
			searcher.testString(station,Language.tr("Surface.Batch.Dialog.BatchSizeFixed")+addon,batchSizeFixed,newBatchSizeFixed->{if (newBatchSizeFixed!=null) batchSizeFixed=newBatchSizeFixed;});
		} else {
			searcher.testString(station,Language.tr("Surface.Batch.Dialog.BatchSizeMin")+addon,batchSizeMin,newBatchSizeMin->{if (newBatchSizeMin!=null) batchSizeMin=newBatchSizeMin;});
			searcher.testString(station,Language.tr("Surface.Batch.Dialog.BatchSizeMax")+addon,batchSizeMax,newBatchSizeMax->{if (newBatchSizeMax!=null) batchSizeMax=newBatchSizeMax;});
		}

		/* Neuer Kundentyp */
		if (batchMode!=BatchMode.BATCH_MODE_COLLECT) {
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.NewClientType"),newClientType,newNewClientType->{newClientType=newNewClientType;});
		}
	}
}
