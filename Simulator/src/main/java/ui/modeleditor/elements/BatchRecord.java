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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
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
	private int batchSizeFixed;
	/** Minimale Batch-Größe im Fall einer variablen Batch-Größe */
	private int batchSizeMin;
	/** Maximale Batch-Größe im Fall einer variablen Batch-Größe */
	private int batchSizeMax;
	/** Art der Batch-Bildung */
	private BatchMode batchMode;
	/** Feste oder variable Batch-Größe */
	private BatchSizeMode batchSizeMode;
	/** Neuer Kundentyp (sofern die Kunden nicht einzeln weitergeleitet werden sollen) */
	private String newClientType;

	/**
	 * Konstruktor der Klasse
	 */
	public BatchRecord() {
		batchSizeMin=1;
		batchSizeMax=1;
		batchSizeFixed=1;
		batchMode=BatchMode.BATCH_MODE_COLLECT;
		batchSizeMode=BatchSizeMode.FIXED;
		newClientType="";
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
			if (batchSizeFixed!=otherBatchRecord.batchSizeFixed) return false;
			break;
		case RANGE:
			if (batchSizeMin!=otherBatchRecord.batchSizeMin) return false;
			if (batchSizeMax!=otherBatchRecord.batchSizeMax) return false;
			break;
		}

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
		if (newClientType==null || newClientType.trim().isEmpty()) {
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
	public int getBatchSizeFixed() {
		return batchSizeFixed;
	}

	/**
	 * Stellt die Batch-Größe ein (im Modus fester Batch-Größe).
	 * @param batchSizeFixed	Neue Batch-Größe
	 * @see BatchSizeMode
	 */
	public void setBatchSizeFixed(int batchSizeFixed) {
		this.batchSizeFixed=batchSizeFixed;
	}


	/**
	 * Liefert die aktuelle minimale Batch-Größe (im Modus variabler Batch-Größe).
	 * @return Minimale Batch-Größe
	 * @see BatchSizeMode
	 */
	public int getBatchSizeMin() {
		return batchSizeMin;
	}

	/**
	 * Liefert die aktuelle maximale Batch-Größe (im Modus variabler Batch-Größe).
	 * @return Maximale Batch-Größe
	 * @see BatchSizeMode
	 */
	public int getBatchSizeMax() {
		return batchSizeMax;
	}

	/**
	 * Stellt die minimale Batch-Größe ein (im Modus variabler Batch-Größe).
	 * @param batchSizeMin	Neue minimale Batch-Größe
	 * @see BatchSizeMode
	 */
	public void setBatchSizeMin(final int batchSizeMin) {
		if (batchSizeMin>=1) this.batchSizeMin=batchSizeMin;
	}

	/**
	 * Stellt die maximale Batch-Größe ein (im Modus variabler Batch-Größe).
	 * @param batchSizeMax	Neue maximale Batch-Größe
	 * @see BatchSizeMode
	 */
	public void setBatchSizeMax(final int batchSizeMax) {
		if (batchSizeMax>=1) this.batchSizeMax=batchSizeMax;
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
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.Size"),""+batchSizeFixed);
			break;
		case RANGE:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.Batch")));
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.SizeMin"),""+batchSizeMin);
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.SizeMax"),""+batchSizeMax);
			break;
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
				final Long L=NumberTools.getPositiveLong(size);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Batch.XML.Batch.SizeMin"),name,node.getParentNode().getNodeName());
				batchSizeMin=(int)((long)L);
				if (batchSizeMax<batchSizeMin) batchSizeMax=batchSizeMin;
				batchSizeMode=BatchSizeMode.RANGE;
			}

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.SizeMax",node);
			if (!size.isEmpty()) {
				final Long L=NumberTools.getPositiveLong(size);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Batch.XML.Batch.SizeMax"),name,node.getParentNode().getNodeName());
				batchSizeMax=(int)((long)L);
				if (batchSizeMin>batchSizeMax) batchSizeMin=batchSizeMax;
				batchSizeMode=BatchSizeMode.RANGE;
			}

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.Size",node);
			if (!size.isEmpty()) {
				final Long L=NumberTools.getPositiveLong(size);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Batch.XML.Batch.Size"),name,node.getParentNode().getNodeName());
				batchSizeFixed=(int)((long)L);
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
			if (!content.trim().isEmpty() && batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
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
		if (clientType!=null && !clientType.trim().isEmpty()) {
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
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),""+batchSizeFixed,level+1);
			break;
		case RANGE:
			if (batchSizeMin==batchSizeMax) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),""+batchSizeMin,level+1);
			} else {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),""+batchSizeMin+".."+batchSizeMax,level+1);
			}
			break;
		}

		/* Neuer Kundentyp */
		if ((batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) && !newClientType.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.NewClientType"),newClientType,level+2);
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
			searcher.testInteger(station,Language.tr("Surface.Batch.Dialog.BatchSizeFixed")+addon,batchSizeFixed,newBatchSizeFixed->{if (newBatchSizeFixed>0) batchSizeFixed=newBatchSizeFixed;});
		} else {
			searcher.testInteger(station,Language.tr("Surface.Batch.Dialog.BatchSizeMin")+addon,batchSizeMin,newBatchSizeMin->{if (newBatchSizeMin>0) batchSizeMin=newBatchSizeMin;});
			searcher.testInteger(station,Language.tr("Surface.Batch.Dialog.BatchSizeMax")+addon,batchSizeMax,newBatchSizeMax->{if (newBatchSizeMax>0) batchSizeMax=newBatchSizeMax;});
		}

		/* Neuer Kundentyp */
		if (batchMode!=BatchMode.BATCH_MODE_COLLECT) {
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.NewClientType"),newClientType,newNewClientType->{newClientType=newNewClientType;});
		}
	}
}
