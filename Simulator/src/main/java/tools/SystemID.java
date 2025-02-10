/**
 * Copyright 2024 Alexander Herzog
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
package tools;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import systemtools.SetupBase;

/*
System.out.println("ID: "+SystemID.getID());

var keys=SystemID.generateRSAKeys();

var encoded=SystemID.getIDEncoded(keys[1]);

var decoded=SystemID.decodeID(encoded,keys[0]);
var result=SystemID.encodeResult(decoded,"QS1234");

System.out.println(result.length()+": "+result);
System.out.println(SystemID.decodeResult(result));
 */

/**
 * Liefert eine eindeutige ID des Systems.
 */
public class SystemID {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse liefert nur statische Methoden und kann nicht instanziert werden.
	 */
	private SystemID() {}

	/**
	 * Zum Ver- und Entschlüsseln zu verwendender Algorithmus
	 */
	private static final String algorithm="RSA/ECB/PKCS1Padding";

	/**
	 * Algorithmus zur Key-Generierung
	 */
	private static final String keyAlgorithm="RSA";

	/**
	 * Liefert eine eindeutige ID des Systems.
	 * @return	System-ID (stets 32 Zeichen lang)
	 */
	public static String getID() {
		final String osName=System.getProperty("os.name").toLowerCase();
		if (osName.contains("windows")) return cleanID(getWindows());
		if (osName.contains("linux")) return cleanID(getLinux());
		return cleanID(null);
	}

	/**
	 * Formatiert eine ausgelesene ID in eine 32 Zeichen lange Zeichenkette.
	 * @param id	Ausgelesene ID (kann <code>null</code> oder leer sein oder auch "-"-Zeichen enthalten)
	 * @return	32 Zeichen lange hexadezimale ID (in Kleinbuchstaben)
	 */
	private static String cleanID(String id) {
		if (id==null) id="";
		id=id.replace("-","");
		while (id.length()<32) id+="0";
		return id.toLowerCase();
	}

	/**
	 * Pfad zum "MachineGuid" Feld in der Registry
	 * @see #MACHINE_ID_KEY
	 * @see #getWindows()
	 */
	private static final String MACHINE_ID_REG_PATH="HKLM\\SOFTWARE\\Microsoft\\Cryptography";

	/**
	 * "MachineGuid" Schlüssel in der Registry
	 * @see #MACHINE_ID_REG_PATH
	 * @see #getWindows()
	 */
	private static final String MACHINE_ID_KEY="MachineGuid";

	/**
	 * Liefert unter Windows eine eindeutige ID für das System.
	 * @return	System-ID (ist im Fehlerfall <code>null</code>)
	 */
	private static String getWindows() {
		return SetupBase.processRegistryResult(MACHINE_ID_REG_PATH,MACHINE_ID_KEY,SetupBase.getRegistryValue(MACHINE_ID_REG_PATH,MACHINE_ID_KEY));
	}

	/**
	 * Liefert unter Linux eine eindeutige ID für das System.
	 * @return	System-ID (ist im Fehlerfall <code>null</code>)
	 */
	private static String getLinux() {
		final var file=new File(File.separator+"etc"+File.separator+"machine-id");
		try {
			return Files.readAllLines(file.toPath()).stream().filter(line->!line.isBlank()).findFirst().orElse(null);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Berechnet einen 32-Hex-Zeichen langen zufälligen Wert
	 * @return	32 zufällige Hexadezimalzeichen
	 */
	private static String getSalt() {
		final byte[] bytes=new byte[16];
		for (int i=0;i<bytes.length;i++) bytes[i]=(byte)(Math.random()*256);
		return cleanID(HexBin.encode(bytes));
	}

	/**
	 * Liefert eine eindeutige ID des Systems mit angefügten zufälligen Zeichen.
	 * @return	System-ID (stets 32 Zeichen lang) gefolgt von "-" gefolgt von 32 zufälligen Hexadezimalzeichen
	 */
	private static String getIDSalted() {
		return getID()+getSalt();
	}

	/**
	 * Lädt ein Schlüssel-Objekt für einen öffentlichen RSA-Schlüssel aus einer Zeichenkette
	 * @param publicKeyString	Zeichenkette, die die Schlüsseldaten enthält
	 * @return	Geladener Schlüssel (oder <code>null</code> im Falle eines Fehlers)
	 */
	private static PublicKey loadPublicKey(final String publicKeyString) {
		final byte[] encodedPublicKey=HexBin.decode(publicKeyString);
		final EncodedKeySpec publicKeySpec=new X509EncodedKeySpec(encodedPublicKey);

		try {
			final KeyFactory generator=KeyFactory.getInstance(keyAlgorithm);
			return generator.generatePublic(publicKeySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return null;
		}
	}

	/**
	 * Lädt ein Schlüssel-Objekt für einen privaten RSA-Schlüssel aus einer Zeichenkette
	 * @param privateKeyString	Zeichenkette, die die Schlüsseldaten enthält
	 * @return	Geladener Schlüssel (oder <code>null</code> im Falle eines Fehlers)
	 */
	private static PrivateKey loadPrivateKey(final String privateKeyString) {
		final byte[] encodedPrivateKey=HexBin.decode(privateKeyString);
		final EncodedKeySpec privateKeySpec=new PKCS8EncodedKeySpec(encodedPrivateKey);

		try {
			final KeyFactory generator=KeyFactory.getInstance(keyAlgorithm);
			return generator.generatePrivate(privateKeySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			return null;
		}
	}

	/**
	 * Liefert eine RSA-Instanz des Verschlüsselungssystems.
	 * @return	RSA-Instanz des Verschlüsselungssystems oder im Fehlerfall <code>null</code>
	 */
	private static Cipher getRSACipher() {
		try {
			return Cipher.getInstance(algorithm);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			return null;
		}
	}

	/**
	 * Liefert eine Blowfish-Instanz des Verschlüsselungssystems.
	 * @param password	Zu verwendendes Passwort
	 * @param mode	Verschlüsselung (<code>Cipher.ENCRYPT_MODE</code>)  oder Entschlüsselung (<code>Cipher.DECRYPT_MODE</code>)
	 * @return	Blowfish-Instanz des Verschlüsselungssystems oder im Fehlerfall <code>null</code>
	 */
	private static Cipher getBlowfishCipher(final String password, final int mode) {
		if (password==null || password.isEmpty()) return null;
		try {
			SecretKeySpec key = new SecretKeySpec(password.getBytes("UTF-8"),"Blowfish");
			final Cipher cipher=Cipher.getInstance("Blowfish");
			cipher.init(mode,key);
			return cipher;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | InvalidKeyException e) {
			return null;
		}
	}

	/**
	 * Verschlüsselt einen Text.
	 * @param data	Zu verschlüsselnde Daten
	 * @param key	Zu verwendender Schlüssel
	 * @return	Liefert im Erfolgsfall die verschlüsselten Daten; im Fehlerfall <code>null</code>
	 */
	private static String encode(final byte[] data, final Key key) {
		if (key==null) return null;
		final Cipher cipher=getRSACipher();
		if (cipher==null) return null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE,key);
			cipher.update(data);
			return Base64.getEncoder().encodeToString(cipher.doFinal());
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			return null;
		}
	}

	/**
	 * Entschlüsselt einen Text.
	 * @param text	Zu entschlüsselnder Text
	 * @param key	Zu verwendender Schlüssel
	 * @return	Liefert im Erfolgsfall die entschlüsselten Daten; im Fehlerfall <code>null</code>
	 */
	private static byte[] decode(final String text, final Key key) {
		if (key==null) return null;
		final Cipher cipher=getRSACipher();
		if (cipher==null) return null;

		try {
			cipher.init(Cipher.DECRYPT_MODE,key);
			cipher.update(Base64.getDecoder().decode(text));
			return cipher.doFinal();
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			return null;
		}
	}

	/**
	 * Erzeugt eine verschlüsselte Form der System-ID (inkl. angefügten zufälligen Zeichen)
	 * @param publicKeyString	Zum Verschlüsseln zu verwendender öffentlicher RSA-Schlüssel
	 * @return	Verschlüsselte System-ID (oder <code>null</code>, wenn die Verschlüsselung fehlgeschlagen ist)
	 */
	public static String getIDEncoded(final String publicKeyString) {
		return encode(HexBin.decode(getIDSalted()),loadPublicKey(publicKeyString));
	}

	/**
	 * Decodiert eine mit {@link #getIDEncoded(String)} verschlüsselte System-ID
	 * @param encoded	Verschlüsselte System-ID
	 * @param privateKeyString	Zum Entschlüsseln zu verwendender privater RSA-Schlüssel
	 * @return	Entschlüsselte System-ID (oder <code>null</code>, wenn die Entschlüsselung fehlgeschlagen ist)
	 */
	public static String decodeID(final String encoded, final String privateKeyString) {
		final byte[] data=decode(encoded,loadPrivateKey(privateKeyString));
		if (data==null) return null;
		final String text=HexBin.encode(data);
		if (text.length()!=64) return null;
		return text.substring(0,32).toLowerCase();
	}

	/**
	 * Verschlüsselt eine Antwort für eine bestimmte System-ID.
	 * @param id	System-ID
	 * @param info	Zusätzliche Antwort
	 * @param privateKeyString	Zum Verschlüsseln zu verwendender privater RSA-Schlüssel
	 * @return	Verschlüsselte Antwort (oder <code>null</code>, wenn die Verschlüsselung fehlgeschlagen ist)
	 */
	public static String encodeResult(final String id, final String info, final String privateKeyString) {
		return encode((id+"-"+info).getBytes(),loadPrivateKey(privateKeyString));
	}

	/**
	 * Verschlüsselt eine Antwort für eine bestimmte System-ID.
	 * @param id	System-ID
	 * @param info	Zusätzliche Antwort
	 * @return	Verschlüsselte Antwort (oder <code>null</code>, wenn die Verschlüsselung fehlgeschlagen ist)
	 */
	public static String encodeResult(final String id, final String info) {
		final Cipher cipher=getBlowfishCipher(id,Cipher.ENCRYPT_MODE);
		if (cipher==null) return null;
		try {
			return HexBin.encode(cipher.doFinal(info.getBytes()));
		} catch (IllegalBlockSizeException|BadPaddingException e) {
			return null;
		}
	}

	/**
	 * Entschlüsselt eine Antwort und prüft, ob diese zur eigenen System-ID passt.
	 * @param encodedResult	Verschlüsselte Antwort
	 * @param publicKeyString	Zum Entschlüsseln zu verwendender öffentlicher RSA-Schlüssel
	 * @return	Liefert im Erfolgsfall die zusätzliche Antwort, sonst (Fehlerfall oder System-ID passt nicht zur System-ID in der Antwort) <code>null</code>
	 */
	public static String decodeResult(final String encodedResult, final String publicKeyString) {
		var b=decode(encodedResult,loadPublicKey(publicKeyString));
		final String text=new String(b);
		if (text==null || text.indexOf("-")<0) return null;
		if (!getID().equalsIgnoreCase(text.substring(0,text.indexOf("-")))) return null;
		return text.substring(text.indexOf("-")+1);
	}

	/**
	 * Entschlüsselt eine Antwort und prüft, ob diese zur eigenen System-ID passt.
	 * @param encodedResult	Verschlüsselte Antwort
	 * @return	Liefert im Erfolgsfall die zusätzliche Antwort, sonst (Fehlerfall oder System-ID passt nicht zur System-ID in der Antwort) <code>null</code>
	 */
	public static String decodeResult(final String encodedResult) {
		final Cipher cipher=getBlowfishCipher(getID(),Cipher.DECRYPT_MODE);
		if (cipher==null) return null;
		try {
			return new String(cipher.doFinal(HexBin.decode(encodedResult)));
		} catch (IllegalBlockSizeException|BadPaddingException e) {
			return null;
		}
	}

	/**
	 * Erzeugt einen privaten und einen öffentlichen RSA-Schlüssel.
	 * @return	Liefert im Erfolgsfall ein Array mit zwei Elementen: private Schlüssel, öffentlicher Schlüssel (und <code>null</code> im Fehlerfall)
	 */
	public static String[] generateRSAKeys() {
		KeyPairGenerator kpg;
		try {
			kpg=KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		kpg.initialize(512);
		final KeyPair kp=kpg.generateKeyPair();
		return new String[] {
				HexBin.encode(kp.getPrivate().getEncoded()),
				HexBin.encode(kp.getPublic().getEncoded())
		};
	}
}
