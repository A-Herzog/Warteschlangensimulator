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
package ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import tools.HexBin;

/**
 * Prüft, ob eine Update-Datei eine gültige Signatur besitzt, und ermöglicht das Signieren von Update-Dateien.
 * @author Alexander Herzog
 * @version 1.0
 */
public class UpdateSystemSignature {
	private final File fileToCheck;

	/**
	 * Konstruktor der Klasse <code>UpdateSystemSignature</code>
	 * @param fileToCheck	Dateiname der Update-Datei, deren Signatur entweder geprüft werden soll oder für die eine Signatur erstellt werden soll
	 */
	public UpdateSystemSignature(File fileToCheck) {
		this.fileToCheck=fileToCheck;
	}

	/**
	 * Legt ein neues Key-Paar bestehend aus private und public key an.
	 * Die Ausgabe erfolgt auf der Konsole.
	 */
	public static void genKeys() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			KeyPair kp=keyGen.generateKeyPair();
			System.out.println("Private");
			String privKey=HexBin.encode(kp.getPrivate().getEncoded());
			System.out.println(privKey);
			System.out.println("Public");
			String pubKey=HexBin.encode(kp.getPublic().getEncoded());
			System.out.println(pubKey);
		} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
	}

	private static String signaturePublicKey="30820122300D06092A864886F70D01010105000382010F003082010A0282010100919C475DF248B4414EB9F6B58AA5DE54C650876D1D4CED787FD9B4C8CA8630E229990DDF0E5DB0C1CD5121FD7D7AF2BC8B8C553B1098FFAC7BF55C6D77965E0067602CB34EF0F6185B84CE640224BFD1CA187C5A36AB7AD547F08E15B8496F88B339DB3C374A5F684CB1EF8CDF0A00FCDAE22482E984A3BE85DE1017F7244C3A3799853328F9F16A69C7CAEEB7D753F871D4987B1D89AA11258A96C60B4C16ABA09954DFDDE65D7B03CCCDBF01EDB3C0C1ECB2D5C71FC07CA3A2A6AC699D95BA665C9F209A8FDE34F831E64396CC0D831A3F0AFAA02BF6AE3855FD8495F03295DD3B7929C58025ED83628AD26ECC936E3463133DEDAE4E217FCFBF6B9327F6DD0203010001";

	private final byte[] getFile(File file) {
		try (FileInputStream in=new FileInputStream(file)) {
			byte[] data=new byte[in.available()];
			in.read(data);
			return data;
		} catch (IOException e) {return null;}
	}

	private final String generateSignature(String privateKeyString, File file) {
		byte[] encodedPrivateKey=HexBin.decode(privateKeyString);
		byte[] data=getFile(file); if (data==null) return "";

		EncodedKeySpec privateKeySpec=new PKCS8EncodedKeySpec(encodedPrivateKey);
		KeyFactory generator; try {generator=KeyFactory.getInstance("RSA");} catch (NoSuchAlgorithmException e) {return "";}
		PrivateKey privateKey; try {privateKey=generator.generatePrivate(privateKeySpec);} catch (InvalidKeySpecException e) {return "";}
		Signature sign;	try {sign=Signature.getInstance("SHA256withRSA");} catch (NoSuchAlgorithmException e) {return "";}
		try {sign.initSign(privateKey);} catch (InvalidKeyException e) {return "";}
		try {sign.update(data);} catch (SignatureException e) {return "";}
		try {return HexBin.encode(sign.sign());} catch (SignatureException e) {return "";}
	}

	private final boolean validateSignature(String publicKeyString, File file, String signature) {
		byte[] encodedPublicKey=HexBin.decode(publicKeyString);
		byte[] data=getFile(file); if (data==null) return false;

		EncodedKeySpec publicKeySpec=new X509EncodedKeySpec(encodedPublicKey);
		KeyFactory generator; try {generator=KeyFactory.getInstance("RSA");} catch (NoSuchAlgorithmException e) {return false;}
		PublicKey publicKey; try {publicKey=generator.generatePublic(publicKeySpec);} catch (InvalidKeySpecException e) {return false;}
		Signature sign;	try {sign=Signature.getInstance("SHA256withRSA");} catch (NoSuchAlgorithmException e) {return false;}
		try {sign.initVerify(publicKey);} catch (InvalidKeyException e) {return false;}
		try {sign.update(data);} catch (SignatureException e) {return false;}
		try {return sign.verify(HexBin.decode(signature));} catch (SignatureException e) {return false;}
	}

	/**
	 * Berechnet die Signatur für die Update-Datei.
	 * @param privateKeyString	Privater Schlüssel, der für die Signaturberechnung verwendet werden soll. Der öffentliche Schlüssel ist in der Klasse als Programmcode enthalten. Der private Schlüssel befindet sich nur als Kommentar in der Datei.
	 * @return	Gibt die Signatur zurück oder im Fehlerfall einen leeren String.
	 */
	public final String sign(String privateKeyString) {
		return generateSignature(privateKeyString,fileToCheck);
	}

	/**
	 * Berechnet die Signatur für die Update-Datei.
	 * @param privateKeyString	Privater Schlüssel, der für die Signaturberechnung verwendet werden soll. Der öffentliche Schlüssel ist in der Klasse als Programmcode enthalten. Der private Schlüssel befindet sich nur als Kommentar in der Datei.
	 * @param signatureFile	Datei, in der die Signatur gespeichert werden soll.
	 * @return	Gibt an, ob die Signierung erfolgreich war.
	 */
	public final boolean sign(String privateKeyString, File signatureFile) {
		String sign=generateSignature(privateKeyString,fileToCheck);
		if (sign.isEmpty()) return false;
		try (FileOutputStream out=new FileOutputStream(signatureFile);) {
			out.write(sign.getBytes());
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Prüft ob die Update-Datei eine gültige Signatur besitzt.
	 * @param signature	Signatur der Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Prüfsumme der Datei mit dem private key unterschrieben war.
	 */
	public final boolean verify(String signature) {
		return validateSignature(signaturePublicKey,fileToCheck,signature);
	}
}
