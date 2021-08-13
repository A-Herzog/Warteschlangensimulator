/**
 * Copyright 2021 Alexander Herzog
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
package simulator.editmodel;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import tools.SetupData;
import ui.dialogs.ModelSecurityCheckDialog;

/**
 * Signiert Modelle vor dem Speichern und ermöglicht es,
 * die Signaturen beim Laden zu prüfen, um so Warnmeldungen
 * vermeiden zu können.
 * @author Alexander Herzog
 */
public class EditModelCertificate {
	/**
	 * Speicherobjekt für Zertifikate
	 */
	private EditModelCertificateStore certStore;

	/**
	 * Geladener Nutzername
	 * @see #loadCertificateData(Element)
	 */
	private String loadedUserName;

	/**
	 * Signatur des geladenen Nutzernamens
	 * @see #loadCertificateData(Element)
	 */
	private String loadedUserNameSignature;

	/**
	 * Im Modell hinterlegter, öffentlicher Schlüssel
	 * @see #loadCertificateData(Element)
	 */
	private String loadedPublicKeyString;

	/**
	 * Im Modell hinterlegte Signaturen für die Stationen
	 * @see #loadCertificateData(Element)
	 */
	private final Map<Integer,Map<String,String>> loadedStationData;

	/**
	 * Konstruktor der Klasse
	 */
	public EditModelCertificate() {
		loadedStationData=new HashMap<>();
	}

	/**
	 * Speichert die Modell-Zertifikatdaten in einem XML-Knoten (sofern Zertifikatdaten benötigt werden)
	 * @param doc	XMK-Dokument
	 * @param parentNode	Übergeordneter XML-Knoten
	 * @param model	Modell dessen Daten mit Zertifikaten abgesichert werden sollen
	 */
	public void storeCertificateData(final Document doc, final Element parentNode, final EditModel model) {
		/* Sollen Modelle signiert werden? */
		if (!SetupData.getSetup().signModels) return;

		/* Kritische Elemente identifizieren */
		final List<ModelSecurityCheckDialog.CriticalElement> criticalElements=ModelSecurityCheckDialog.getCriticalElements(model.surface);
		criticalElements.addAll(ModelSecurityCheckDialog.getCriticalModelProperties(model));
		if (criticalElements.size()==0) return;

		/* Zertifikat laden oder erstellen */
		if (certStore==null) certStore=new EditModelCertificateStore();
		final KeyPair keyPair=certStore.getKeyPair();
		if (keyPair==null) return;

		final Element mainNode=doc.createElement(Language.tr("CriticalStationsSignature.Root"));
		parentNode.appendChild(mainNode);
		Element node;

		/* Daten signieren und speichern */
		for (ModelSecurityCheckDialog.CriticalElement criticalElement: criticalElements) {
			mainNode.appendChild(node=doc.createElement(Language.tr("CriticalStationsSignature.Station")));
			node.setAttribute(Language.tr("CriticalStationsSignature.Station.id"),""+criticalElement.stationId);
			node.setAttribute(Language.tr("CriticalStationsSignature.Station.Hash"),EditModelCertificateStore.hash(criticalElement.info));
			node.setAttribute(Language.tr("CriticalStationsSignature.Station.Signature"),certStore.sign(criticalElement.info));
		}

		/* Benutzername öffentlichen Schlüssel speichern */
		mainNode.appendChild(node=doc.createElement(Language.tr("CriticalStationsSignature.UserName")));
		final String userName=SetupData.getSetup().defaultUserName;
		node.setTextContent(userName);
		node.setAttribute(Language.tr("CriticalStationsSignature.UserName.Signature"),certStore.sign(userName));

		mainNode.appendChild(node=doc.createElement(Language.tr("CriticalStationsSignature.PublicKey")));
		node.setTextContent(EditModelCertificateStore.getEncodedPublicKey(certStore.getKeyPair().getPublic()));
	}

	/**
	 * Prüft, ob der Namen eines XML-Knoten zu dem Modell-Zertifikat-System passt.
	 * @param nodeName	Zu prüfender XML-Knoten-Name
	 * @return	Liefert <code>true</code>, wenn es sich bei dem XML-Knoten um den Basisknoten für das Modell-Zertifikat-System handelt
	 * @see #loadCertificateData(Element)
	 */
	public static boolean isCertificateNode(final String nodeName) {
		return Language.trAll("CriticalStationsSignature.Root",nodeName);
	}

	/**
	 * Lädt die Zertifikatdaten aus einem XML-Knoten.
	 * @param node	XML-Knoten aus dem die Daten geladen werden sollen
	 * @see #isCertificateNode(String)
	 */
	public void loadCertificateData(final Element node) {
		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			final String nodeName=e.getNodeName();

			if (Language.trAll("CriticalStationsSignature.UserName",nodeName)) {
				loadedUserName=e.getTextContent();
				loadedUserNameSignature=Language.trAllAttribute("CriticalStationsSignature.UserName.Signature",e);
				continue;
			}

			if (Language.trAll("CriticalStationsSignature.PublicKey",nodeName)) {
				loadedPublicKeyString=e.getTextContent();
				continue;
			}

			if (Language.trAll("CriticalStationsSignature.Station",nodeName)) {
				final String idString=Language.trAllAttribute("CriticalStationsSignature.Station.id",e);
				final String info=Language.trAllAttribute("CriticalStationsSignature.Station.Signature",e);
				final String hash=Language.trAllAttribute("CriticalStationsSignature.Station.Hash",e);
				final Integer id=NumberTools.getInteger(idString);
				if (id!=null) {
					final Map<String,String> map=loadedStationData.computeIfAbsent(id,newId->new HashMap<>());
					map.put(hash,info);
				}
				continue;
			}
		}
	}

	/**
	 * Prüft, ob eine Signatur zu einer Ausgangszeichenkette passt.
	 * @param signedInfo	Signatur
	 * @param originalInfo	Ausgangszeichenkette zu der die Signatur passen soll
	 * @param publicKey	Öffentlicher Schlüssel auf dessen Basis die Signatur geprüft werden soll
	 * @return	Liefert <code>true</code>, wenn die Signatur zu der Ausgangszeichenkette passt
	 */
	private boolean testCriticalElement(final String signedInfo, final String originalInfo, final PublicKey publicKey) {
		if (signedInfo==null || originalInfo==null) return true;
		return EditModelCertificateStore.checkSign(originalInfo,signedInfo,publicKey);
	}

	/**
	 * Prüft, ob für alle kritischen Stationen Zertifikatdaten vorhanden sind
	 * @param criticalElements	Liste der kritischen Stationen
	 * @param publicKey	Zur Prüfung der Zertifikatdaten zu verwendender öffentlicher Schlüssel
	 * @return	Liefert <code>true</code>, wenn Zertifikatdaten für alle zu prüfenden Stationen vorliegen
	 */
	private boolean testCriticalElements(final List<ModelSecurityCheckDialog.CriticalElement> criticalElements, final PublicKey publicKey) {
		if (loadedStationData==null) return (criticalElements==null || criticalElements.size()==0);

		for (ModelSecurityCheckDialog.CriticalElement criticalElement: criticalElements) {
			final int id=criticalElement.stationId;
			final String info=criticalElement.info;
			final String hash=EditModelCertificateStore.hash(info);
			final Map<String,String> map=loadedStationData.get(id);
			if (map==null) return false;
			String signature=map.get(hash);
			if (signature==null) signature=map.get(""); /* Fallback-Verhalten; früher gab's keine Hashes und nur max. ein Skript pro Station */
			if (signature==null) return false;
			if (!testCriticalElement(signature,criticalElement.info,publicKey)) return false;
		}

		return true;
	}

	/**
	 * Muss für dieses Modell eine Sicherheitswarnung angezeigt werden
	 * (weil es kritische Stationen enthält und kein passendes Zertifikat besitzt)?
	 * @param model	Zu prüfendes Modell
	 * @param allowNewPublicKey	Funktion zur Abfrage, ob einem neuen öffentlichen Schlüssel vertraut werden soll
	 * @return	Liefert <code>true</code>, wenn eine Sicherheitswarnung angezeigt werden soll
	 */
	public boolean isSecurityWarningNeeded(final EditModel model, final BiFunction<String,PublicKey,Boolean> allowNewPublicKey) {
		/* Kritische Elemente identifizieren */
		final List<ModelSecurityCheckDialog.CriticalElement> criticalElements=ModelSecurityCheckDialog.getCriticalElements(model.surface);
		criticalElements.addAll(ModelSecurityCheckDialog.getCriticalModelProperties(model));
		if (criticalElements.size()==0) return false;

		/* Zertifikat laden oder erstellen */
		if (certStore==null) certStore=new EditModelCertificateStore();
		final KeyPair keyPair=certStore.getKeyPair();
		if (keyPair==null) return true;

		/* Wurde ein öffentlicher Schlüssel für die Signaturen angegeben? */
		final PublicKey publicKey=EditModelCertificateStore.getDecodedPublicKey(loadedPublicKeyString);
		if (publicKey==null) return true;

		/* Entspricht der öffentliche Schlüssel dem eigenen öffentlichen Schlüssel? */
		final PublicKey ownPublicKey=keyPair.getPublic();
		if (EditModelCertificateStore.equals(ownPublicKey,publicKey)) {
			return !testCriticalElements(criticalElements,ownPublicKey);
		}

		/* Wurde der Name korrekt signiert? */
		if (loadedUserName==null || loadedUserNameSignature==null) return true;
		if (!EditModelCertificateStore.checkSign(loadedUserName,loadedUserNameSignature,publicKey)) return true;

		/* Vertrauen wir dem öffentlichen Schlüssel? */
		for (EditModelCertificateStore.TrustedPublicKey trustedPublicKey: certStore.getTrustedPublicKeys()) {
			if (EditModelCertificateStore.equals(trustedPublicKey.publicKey,publicKey) && loadedUserName.equals(trustedPublicKey.userName)) {
				return !testCriticalElements(criticalElements,publicKey);
			}
		}

		/* Unbekannter öffentlicher Schlüssel? */
		if (allowNewPublicKey!=null) {
			if (!testCriticalElements(criticalElements,publicKey)) return true;
			if (allowNewPublicKey.apply(loadedUserName,publicKey)) {
				certStore.registerTrustedPublicKey(loadedUserName,publicKey);
				return false;
			}
		}

		return true;
	}
}