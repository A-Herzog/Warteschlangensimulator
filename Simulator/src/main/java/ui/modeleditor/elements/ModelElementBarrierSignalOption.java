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
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse hält eine einzelne Signal-Option zur Auslösung eines
 * <code>ModelElementBarrier</code>-Elements vor.
 * @author Alexander Herzog
 * @see ModelElementBarrier
 */
public final class ModelElementBarrierSignalOption implements Cloneable {
	/**
	 * Namen des Signals, auf das dieses Element hören soll
	 * @see #getSignalName()
	 * @see #setSignalName(String)
	 */
	private String signalName="";

	/**
	 * Name eines Kundentyps oder <code>null</code>, wenn die Freigabe auf alle Kundentypen wirken soll.
	 * @see #getClientType()
	 * @see #setClientType(String)
	 */
	private String clientType;

	/**
	 * Anzahl an Kunden, die das Element passieren können, bevor die Schrankenwirkung einsetzt
	 * @see #getInitialClients()
	 * @see #setInitialClients(int)
	 */
	private int initialClients;

	/**
	 * Maximale Anzahl an wartenden Kunden, die freigegeben werden, wenn das zugehörige Signal ausgelöst wird
	 * @see #getClientsPerSignal()
	 * @see #setClientsPerSignal(int)
	 */
	private int clientsPerSignal;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementBarrierSignalOption() {
		signalName="";
		initialClients=0;
		clientsPerSignal=1;
	}

	/**
	 * Prüft, ob diese Signal-Option einer anderen inhaltlich gleicht.
	 * @param otherOption	Andere Signal-Option die mit dieser verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsOption(final ModelElementBarrierSignalOption otherOption) {
		if (otherOption==null) return false;

		if (!signalName.equals(otherOption.signalName)) return false;
		if (clientType==null) {
			if (otherOption.clientType!=null) return false;
		} else {
			if (otherOption.clientType==null) return false;
			if (!clientType.equals(otherOption.clientType)) return false;
		}
		if (initialClients!=otherOption.initialClients) return false;
		if (clientsPerSignal!=otherOption.clientsPerSignal) return false;
		return true;
	}

	@Override
	public ModelElementBarrierSignalOption clone() {
		final ModelElementBarrierSignalOption clone=new ModelElementBarrierSignalOption();
		clone.signalName=signalName;
		clone.clientType=clientType;
		clone.initialClients=initialClients;
		clone.clientsPerSignal=clientsPerSignal;
		return clone;
	}

	/**
	 * Liefert den Namen des Signals, bei dessen Auslösung wartende Kunden freigegeben werden sollen.
	 * @return	Namen des Signals, auf das dieses Element hören soll
	 */
	public String getSignalName() {
		return signalName;
	}

	/**
	 * Stellt den Namen des Signals, bei dessen Auslösung wartende Kunden freigegeben werden sollen, ein.
	 * @param signalName	Namen des Signals, auf das dieses Element hören soll
	 */
	public void setSignalName(final String signalName) {
		if (signalName!=null) this.signalName=signalName;
	}

	/**
	 * Liefert die Anzahl an Kunden, die das Element passieren können, bevor die Schrankenwirkung einsetzt.
	 * @return	Anzahl an Kunden, die das Element passieren können, bevor die Schrankenwirkung einsetzt
	 */
	public int getInitialClients() {
		return initialClients;
	}

	/**
	 * Stellt die Anzahl an Kunden, die das Element passieren können, bevor die Schrankenwirkung einsetzt, ein.
	 * @param initialClients	Anzahl an Kunden, die das Element passieren können, bevor die Schrankenwirkung einsetzt
	 */
	public void setInitialClients(final int initialClients) {
		if (initialClients>=0) this.initialClients=initialClients;
	}

	/**
	 * Liefert die maximale Anzahl an wartenden Kunden, die freigegeben werden, wenn das zugehörige Signal ausgelöst wird.
	 * @return	Maximale Anzahl an wartenden Kunden, die freigegeben werden, wenn das zugehörige Signal ausgelöst wird
	 */
	public int getClientsPerSignal() {
		return clientsPerSignal;
	}

	/**
	 * Gibt an, auf welchen Kundentyp die Freigabe wirken soll.
	 * @return	Name eines Kundentyps oder <code>null</code>, wenn die Freigabe auf alle Kundentypen wirken soll.
	 */
	public String getClientType() {
		return clientType;
	}

	/**
	 * Stellt ein, auf welchen Kundentyp die Freigabe wirken soll.
	 * @param clientType	Name eines Kundentyps oder <code>null</code>, wenn die Freigabe auf alle Kundentypen wirken soll.
	 */
	public void setClientType(final String clientType) {
		this.clientType=clientType;
	}

	/**
	 * Stellt die maximale Anzahl an wartenden Kunden, die freigegeben werden, wenn das zugehörige Signal ausgelöst wird, ein.
	 * @param clientsPerSignal	Maximale Anzahl an wartenden Kunden, die freigegeben werden, wenn das zugehörige Signal ausgelöst wird
	 */
	public void setClientsPerSignal(final int clientsPerSignal) {
		if (clientsPerSignal<=0) this.clientsPerSignal=-1; else this.clientsPerSignal=clientsPerSignal;
	}

	/**
	 * Speichert die Daten in einem xml-Element
	 * @param doc	xml-Dokument
	 * @param parent	Übergeordnetes xml-Element
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element node=doc.createElement(Language.trPrimary("Surface.Barrier.XML.SignalOption"));
		parent.appendChild(node);

		Element sub;

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Barrier.XML.SignalName")));
		sub.setTextContent(signalName);

		if (initialClients>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Barrier.XML.InitialRelease")));
			sub.setAttribute(Language.trPrimary("Surface.Barrier.XML.Count"),""+initialClients);
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Barrier.XML.Release")));
		if (clientsPerSignal>=1) {
			sub.setAttribute(Language.trPrimary("Surface.Barrier.XML.Count"),""+clientsPerSignal);
		} else {
			sub.setAttribute(Language.trPrimary("Surface.Barrier.XML.Count"),Language.trPrimary("Surface.Barrier.XML.Count.All"));
		}

		if (clientType!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Barrier.XML.ClientType")));
			sub.setTextContent(clientType);
		}
	}

	/**
	 * Prüft, ob es sich bei einem xml-Element um ein (vom Tag her) passendes Element für einen Signal-Option-Datensatz handelt.
	 * @param node	Zu prüfendes xml-Element
	 * @return	Gibt <code>true</code> zurück, wenn das xml-Tag für dieses Objekt passt
	 */
	public static boolean isXMLNode(final Element node) {
		return Language.trAll("Surface.Barrier.XML.SignalOption",node.getNodeName());
	}

	/**
	 * Versucht einen Signal-Option-Datensatz aus einem xml-Element zu laden
	 * @param node	xml-Element aus dem der Signal-Option-Datensatz geladen werden soll
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	public String loadFromXML(final Element node) {
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String error=loadProperty(e.getNodeName(),e.getTextContent(),e);
			if (error!=null) return error;
		}
		return null;
	}

	/**
	 * Lädt einen Eintrag aus einem xml-Element
	 * @param name	Name des XML-Elements
	 * @param content	Inhalt dex XML-Elements
	 * @param node	XML-Elements
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	private String loadProperty(final String name, final String content, final Element node) {
		if (Language.trAll("Surface.Barrier.XML.SignalName",name)) {
			signalName=node.getTextContent();
			return null;
		}

		if (Language.trAll("Surface.Barrier.XML.InitialRelease",name)) {
			final String count=Language.trAllAttribute("Surface.Barrier.XML.Count",node);
			if (!count.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(node.getAttribute("Anzahl"));
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Barrier.XML.Count"),name,node.getParentNode().getNodeName());
				initialClients=(int)((long)L);
			}
			return null;
		}

		if (Language.trAll("Surface.Barrier.XML.Release",name)) {
			final String count=Language.trAllAttribute("Surface.Barrier.XML.Count",node);
			if (!count.isEmpty()) {
				if (Language.trAll("Surface.Barrier.XML.Count.All",count)) {
					clientsPerSignal=-1;
				} else {
					final Long L=NumberTools.getPositiveLong(count);
					if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Barrier.XML.Count"),name,node.getParentNode().getNodeName());
					clientsPerSignal=(int)((long)L);
				}
			}
			return null;
		}

		if (Language.trAll("Surface.Barrier.XML.ClientType",name)) {
			clientType=content;
			return null;
		}

		return null;
	}

	/**
	 * Fügt die Beschreibung für die Daten dieses Objekts als Eigenschaft zu der Beschreibung hinzu
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	public void buildDescriptionProperty(final ModelDescriptionBuilder descriptionBuilder) {
		final StringBuilder sb=new StringBuilder();

		/* Kundentyp(en) */
		if (clientType==null) {
			sb.append(Language.tr("ModelDescription.Barrier.ClientType.All"));
		} else {
			sb.append(Language.tr("ModelDescription.Barrier.ClientType"));
			sb.append(": ");
			sb.append(clientType);
		}
		sb.append("\n");

		/* Kunden pro Signal */
		sb.append(Language.tr("ModelDescription.Barrier.ClientsPerSignal"));
		sb.append(": ");
		sb.append(clientsPerSignal);
		sb.append(" ");
		sb.append((clientsPerSignal==1)?Language.tr("ModelDescription.Barrier.Client.Singular"):Language.tr("ModelDescription.Barrier.Client.Plural"));
		sb.append("\n");

		/* Initiale Freigabe */
		if (initialClients>0) {
			sb.append(Language.tr("ModelDescription.Barrier.InitialClients"));
			sb.append(": ");
			sb.append(initialClients);
			sb.append(" ");
			sb.append((initialClients==1)?Language.tr("ModelDescription.Barrier.Client.Singular"):Language.tr("ModelDescription.Barrier.Client.Plural"));
			sb.append("\n");
		}

		/* Name für Eigenschaft bestimmen */
		final String propertyName=Language.tr("ModelDescription.Barrier.Signal")+" \""+signalName+"\"";

		/* Ergebnis ausgeben */
		descriptionBuilder.addProperty(propertyName,sb.toString(),1000);
	}

	/**
	 * Sucht einen Text in den Daten des Elements.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		searcher.testString(station,Language.tr("Surface.Barrier.Dialog.ReleaseSignal"),signalName,newSignalName->{signalName=newSignalName;});
		searcher.testString(station,Language.tr("Surface.Barrier.Dialog.ClientType"),clientType,newClientType->{clientType=newClientType;});
		searcher.testInteger(station,Language.tr("Surface.Barrier.Dialog.ClientsToBeReleaseBeforeBarrierActivates"),initialClients,newInitialClients->{if (newInitialClients>=0) initialClients=newInitialClients;});
		if (clientsPerSignal>0) searcher.testInteger(station,Language.tr("Surface.Barrier.Dialog.ClientsPerRelease"),clientsPerSignal,newClientsPerSignal->{if (newClientsPerSignal>0) clientsPerSignal=newClientsPerSignal;});
	}
}
