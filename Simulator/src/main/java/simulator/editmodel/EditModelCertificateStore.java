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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import mathtools.Table;
import tools.SetupData;
import ui.MainFrame;

/**
 * Diese Klasse hält die öffentlichen und privaten Schlüssel
 * zum Signieren von potentiell kritischen Stationen vor.
 * @author Alexander Herzog
 * @see EditModelCertificate
 */
public class EditModelCertificateStore {
	/**
	 * Dateiname für die Datei zum Speichern der Zertifikatdaten
	 */
	private static final String CERTIFICATE_FILE_NAME="Certificate.cfg";

	/**
	 * Datei zum Speichern der Zertifikatdaten
	 * @see #CERTIFICATE_FILE_NAME
	 */
	private final File certStoreFile;

	/**
	 * Eigener privater und öffentlicher Schlüssel zum
	 * Signieren und Prüfen von Signaturen
	 */
	private KeyPair keyPair;

	/**
	 * Öffentliche Schlüssel anderer Personen, denen vertraut wird
	 */
	public final List<TrustedPublicKey> trustedPublicKeys;

	/**
	 * Konstruktor der Klasse
	 */
	public EditModelCertificateStore() {
		certStoreFile=new File(SetupData.getSetupFolder(),CERTIFICATE_FILE_NAME);
		trustedPublicKeys=new ArrayList<>();

		if (load()) return;
		if (generateCertificate()) {
			save();
		}
	}

	/**
	 * Berechnet den SHA256-Hash über eine Zeichenkette.
	 * @param data	Zeichenkette für die der Hashwert berechnet werden soll
	 * @return	SHA256-Hashwert
	 */
	public static String hash(final String data) {
		if (data==null || data.trim().isEmpty()) return "";
		try {
			final MessageDigest digest=MessageDigest.getInstance("SHA-256");
			final byte[] hash=digest.digest(data.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	/**
	 * Prüft, ob bereits ein eigenes Zertifikat angelegt wurde.
	 * @return	Liefert <code>true</code>, wenn Zertifikatinformationen vorhanden sind
	 */
	public static boolean certificateFileExists() {
		final File file=new File(SetupData.getSetupFolder(),CERTIFICATE_FILE_NAME);
		return file.isFile();
	}

	/**
	 * Erstellt ein neues Schlüsselpaar aus privatem und öffentlichem Schlüssel.
	 * @return	Liefert <code>true</code>, wenn der Schlüssel generiert werden konnte
	 * @see #keyPair
	 */
	private boolean generateCertificate() {
		try {
			final KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(4096);
			keyPair=keyGen.generateKeyPair();
			return true;
		} catch (NoSuchAlgorithmException e) {
			return false;
		}
	}

	/**
	 * Lädt die Schlüsseldaten aus der entsprechenden Datei.
	 * @return	Liefert <code>true</code>, wenn die Daten geladen werden konnten.
	 * @see #CERTIFICATE_FILE_NAME
	 */
	private boolean load() {
		final KeyFactory kf;
		try {
			kf=KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e1) {
			return false;
		}

		/* Datei laden */
		if (!certStoreFile.isFile()) return false;
		final List<String> lines=Table.loadTextLinesFromFile(certStoreFile);
		if (lines==null) return false;

		/* Zeilen interpretieren */
		byte[] publicKeyBytes=null;
		byte[] privateKeyBytes=null;
		String userName=null;
		int mode=0;
		for (String line: lines) switch (mode) {
		case 1:
			if (line.trim().isEmpty()) break;
			try {
				publicKeyBytes=Base64.getDecoder().decode(line.trim());
			} catch (IllegalArgumentException e) {publicKeyBytes=null;}
			mode=0;
			break;
		case 2:
			if (line.trim().isEmpty()) break;
			try {
				privateKeyBytes=Base64.getDecoder().decode(line.trim());
			} catch (IllegalArgumentException e) {privateKeyBytes=null;}
			mode=0;
			break;
		case 3:
			if (line.trim().isEmpty()) break;
			try {
				userName=new String(Base64.getDecoder().decode(line.trim()));
			} catch (IllegalArgumentException e) {userName=null;}
			mode=4;
			break;
		case 4:
			if (line.trim().isEmpty()) break;
			try {
				final byte[] bytes=Base64.getDecoder().decode(line.trim());
				final PublicKey key=kf.generatePublic(new X509EncodedKeySpec(bytes));
				trustedPublicKeys.add(new TrustedPublicKey(userName,key));
			} catch (InvalidKeySpecException | IllegalArgumentException e) {}
			userName=null;
			mode=0;
			break;
		default:
			if (line.equalsIgnoreCase("Own public key")) {mode=1; break;}
			if (line.equalsIgnoreCase("Own private key")) {mode=2; break;}
			if (line.equalsIgnoreCase("Trusted public key")) {mode=3; break;}
			mode=0;
		}
		if (publicKeyBytes==null || privateKeyBytes==null) return false;

		/* KeyPair-Objekt erzeugen */
		try {
			final PublicKey publicKey=kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
			final PrivateKey privateKey=kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
			keyPair=new KeyPair(publicKey,privateKey);
			return true;
		} catch (InvalidKeySpecException e) {
			return false;
		}
	}

	/**
	 * Liefert die zu speichernden Zertifikatdaten.
	 * @return	Zu speichernde Zertifikatdaten
	 * @see #save()
	 */
	private String getCertificateText() {
		final StringBuilder text=new StringBuilder();
		text.append(MainFrame.PROGRAM_NAME);
		text.append(" key store\n");

		if (keyPair!=null) {
			text.append("Own public key\n");
			text.append(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
			text.append("\n");
			text.append("Own private key\n");
			text.append(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
			text.append("\n");
		}

		for (TrustedPublicKey trustedPublicKey: trustedPublicKeys) {
			text.append("Trusted public key\n");
			text.append(Base64.getEncoder().encodeToString(trustedPublicKey.userName.getBytes()));
			text.append("\n");
			text.append(Base64.getEncoder().encodeToString(trustedPublicKey.publicKey.getEncoded()));
			text.append("\n");
		}

		return text.toString();
	}

	/**
	 * Speichert die Zertifikatdaten (eigener öffentlicher und privater Schlüssel
	 * und externe öffentliche Schlüssel, denen vertraut wird).
	 */
	public void save() {
		Table.saveTextToFile(getCertificateText(),certStoreFile);
	}

	/**
	 * Liefert den eigenen öffentlichen und privaten Schlüssel.
	 * @return	Eigener öffentlicher und privater Schlüssel
	 */
	public KeyPair getKeyPair() {
		return keyPair;
	}

	/**
	 * Signiert eine Zeichenkette mit dem eigenen privaten Schlüssel.
	 * @param data	Zu signierte Zeichenkette
	 * @return	Liefert die Signatur der Zeichenkette
	 * @see #checkSign(String, String, PublicKey)
	 */
	public String sign(final String data) {
		if (data==null || keyPair==null) return "";

		final Signature sign;
		try {
			sign=Signature.getInstance("SHA256withRSA");
		} catch (NoSuchAlgorithmException e) {
			return "";
		}

		try {
			sign.initSign(keyPair.getPrivate());
		} catch (InvalidKeyException e1) {
			return "";
		}

		try {
			sign.update(data.getBytes());
		} catch (SignatureException e) {
			return "";
		}

		try {
			return Base64.getEncoder().encodeToString(sign.sign());
		} catch (SignatureException e) {
			return "";
		}
	}

	/**
	 * Prüft eine Signatur.
	 * @param original	Originaldaten
	 * @param signature	Signatur zu den Originaldaten
	 * @param publicKey	Öffentlicher Schlüssel mit dem die Signatur dekodiert werden soll
	 * @return	Liefert <code>true</code>, wenn die Signatur in Bezug auf den angegebenen Schlüssel zu den Originaldaten passt
	 */
	public static boolean checkSign(final String original, final String signature, final PublicKey publicKey) {
		if (original==null) return false;
		if (signature==null || signature.isEmpty()) return false;
		if (publicKey==null) return false;

		final Signature sign;
		try {
			sign=Signature.getInstance("SHA256withRSA");
		} catch (NoSuchAlgorithmException e) {
			return false;
		}

		try {
			sign.initVerify(publicKey);
		} catch (InvalidKeyException e) {
			return false;
		}

		try {
			sign.update(original.getBytes());
		} catch (SignatureException e) {
			return false;
		}

		try {
			return sign.verify(Base64.getDecoder().decode(signature));
		} catch (IllegalArgumentException | SignatureException e) {
			return false;
		}
	}

	/**
	 * Wandelt einen öffentlichen Schlüssel in eine Zeichenkette um.
	 * @param publicKey	Öffentlicher Schlüssel
	 * @return	Zeichenkette
	 * @see #getDecodedPublicKey(String)
	 */
	public static String getEncodedPublicKey(final PublicKey publicKey) {
		if (publicKey==null) return "";
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}

	/**
	 * Wandelt eine Zeichenkette in einen öffentlichen Schlüssel um.
	 * @param publicKeyString	Zeichenkette
	 * @return	Öffentlicher Schlüssel oder <code>null</code>, wenn die Zeichenkette nicht interpretiert werden konnte
	 * @see #getEncodedPublicKey(PublicKey)
	 */
	public static PublicKey getDecodedPublicKey(final String publicKeyString) {
		if (publicKeyString==null) return null;

		final byte[] publicKeyBytes;
		try {
			publicKeyBytes=Base64.getDecoder().decode(publicKeyString);
		} catch (IllegalArgumentException e) {
			return null;
		}

		final KeyFactory kf;
		try {
			kf=KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e1) {
			return null;
		}

		try {
			return kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		} catch (InvalidKeySpecException e) {
			return null;
		}
	}

	/**
	 * Vergleicht zwei öffentliche Schlüssel.
	 * @param publicKey1	Erster öffentlicher Schlüssel
	 * @param publicKey2	Zweiter öffentlicher Schlüssel
	 * @return	Liefert <code>true</code>, wenn die beiden Schlüssel inhaltlich identisch sind
	 */
	public static boolean equals(final PublicKey publicKey1, final PublicKey publicKey2) {
		if (publicKey1==null && publicKey2==null) return true;
		if (publicKey1==null || publicKey2==null) return false;

		final byte[] data1=publicKey1.getEncoded();
		final byte[] data2=publicKey2.getEncoded();
		if (data1==null || data2==null) return false;
		if (data1.length!=data2.length) return false;
		for (int i=0;i<data1.length;i++) if (data1[i]!=data2[i]) return false;
		return true;
	}

	/**
	 * Liefert die Liste der (externen) öffentlichen Schlüssel, denen vertraut wird.
	 * @return	Liste der (externen) öffentlichen Schlüssel, denen vertraut wird
	 * @see EditModelCertificateStore.TrustedPublicKey
	 */
	public List<TrustedPublicKey> getTrustedPublicKeys() {
		return trustedPublicKeys;
	}

	/**
	 * Fügt einen neuen öffentlichen Schlüssel zur Liste der vertrauten öffentlichen Schlüssel.
	 * hinzu und speichert diese Liste.
	 * @param userName	Nutzername
	 * @param publicKey	Öffentlicher Schlüssel
	 * @see #getTrustedPublicKeys()
	 * @see #clearTrustedPublicKeys()
	 */
	public void registerTrustedPublicKey(final String userName, final PublicKey publicKey) {
		if (userName==null || publicKey==null) return;
		trustedPublicKeys.add(new TrustedPublicKey(userName,publicKey));
		save();
	}

	/**
	 * Löscht die Liste der vertrauten öffentlichen Schlüssel.
	 * @see #getTrustedPublicKeys()
	 * @see #registerTrustedPublicKey(String, PublicKey)
	 */
	public void clearTrustedPublicKeys() {
		if (trustedPublicKeys.size()==0) return;
		trustedPublicKeys.clear();
		save();
	}

	/**
	 * Datensatz zu einem einzelnen (externen) öffentlichen Schlüssel, dem vertraut wird
	 * @see EditModelCertificateStore#getTrustedPublicKeys()
	 */
	public static class TrustedPublicKey {
		/**
		 * Nutzername
		 */
		public final String userName;

		/**
		 * Öffentlicher Schlüssel
		 */
		public final PublicKey publicKey;

		/**
		 * Konstruktor der Klasse
		 * @param userName	Nutzername
		 * @param publicKey	Öffentlicher Schlüssel
		 */
		public TrustedPublicKey(final String userName, final PublicKey publicKey) {
			this.userName=userName;
			this.publicKey=publicKey;
		}
	}
}
