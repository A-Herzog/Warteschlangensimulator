package xml;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hilfsklasse mit statischen Methoden zum Ver- und Entschl�sseln von Daten,
 * wird von {@link XMLTools} verwendet.
 * @author Alexander Herzog
 * @see XMLTools
 */
public class ChiperTools {
	private ChiperTools() {}

	private static final Cipher getChiper(String password, int mode) {
		SecretKeyFactory factory;
		try {
			factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		} catch (NoSuchAlgorithmException e1) {return null;}
		KeySpec spec=new PBEKeySpec(password.toCharArray(),"nosalt".getBytes(),256,128);
		SecretKey tmp;
		SecretKey secret=null;
		try {
			tmp=factory.generateSecret(spec);
			secret=new SecretKeySpec(tmp.getEncoded(),"AES");
		} catch (InvalidKeySpecException e1) {return null;}

		Cipher cipher;
		try {
			cipher=Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {return null;}

		try {
			cipher.init(mode,secret);
		} catch (InvalidKeyException e) {return null;}

		return cipher;
	}

	private static final byte[] getChecksum(byte[] b) {
		MessageDigest md;
		try {
			md=MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {return null;}

		md.update(b);
		byte[] sha=md.digest();
		if (sha.length!=20) return null;
		return sha;
	}

	/**
	 * Verschl�sselt Daten mit AES
	 * @param data	Zu verschl�sselnde Daten
	 * @param password	Passwort
	 * @return	Verschl�sselte Daten (oder im Fehlerfall <code>null</code>)
	 * @see #decrypt(ByteArrayOutputStream, String)
	 */
	public static final ByteArrayOutputStream encrypt(final ByteArrayOutputStream data, final String password) {
		final ByteArrayOutputStream data2=new ByteArrayOutputStream();

		/* Pr�fsumme berechnen und eigentlichen Daten voran stellen */
		byte[] sha=getChecksum(data.toByteArray()); if (sha==null) return null;
		try {
			data2.write(sha);
			data2.write(data.toByteArray());
		} catch (IOException e1) {return null;}

		/* Daten verschl�sseln */
		Cipher cipher=getChiper(password,Cipher.ENCRYPT_MODE);
		if (cipher==null) return null;

		ByteArrayOutputStream output=new ByteArrayOutputStream();
		try {
			byte[] b=cipher.doFinal(data2.toByteArray());
			output.write(b);
		} catch (IllegalBlockSizeException | BadPaddingException | IOException e) {return null;}

		return output;
	}

	/**
	 * Verschl�sselt Daten mit AES
	 * @param data	Zu verschl�sselnde Daten
	 * @param password	Passwort
	 * @return	Verschl�sselte Daten (oder im Fehlerfall <code>null</code>)
	 * @see #decrypt(byte[], String)
	 */
	public static final byte[] encrypt(final byte[] data, final String password) {
		final ByteArrayOutputStream data2=new ByteArrayOutputStream();

		/* Pr�fsumme berechnen und eigentlichen Daten voran stellen */
		byte[] sha=getChecksum(data); if (sha==null) return null;
		try {
			data2.write(sha);
			data2.write(data);
		} catch (IOException e1) {return null;}

		/* Daten verschl�sseln */
		final Cipher cipher=getChiper(password,Cipher.ENCRYPT_MODE);
		if (cipher==null) return null;

		ByteArrayOutputStream output=new ByteArrayOutputStream();
		try {
			byte[] b=cipher.doFinal(data2.toByteArray());
			output.write(b);
		} catch (IllegalBlockSizeException | BadPaddingException | IOException e) {return null;}

		return output.toByteArray();
	}

	/**
	 * Entschl�sselt Daten mit AES
	 * @param data	Zu entschl�sselnde Daten
	 * @param password	Passwort
	 * @return	Entschl�sselte Daten (oder im Fehlerfall <code>null</code>)
	 * @see #encrypt(ByteArrayOutputStream, String)
	 */
	public static final ByteArrayOutputStream decrypt(final ByteArrayOutputStream data, final String password) {
		/* Daten entschl�sseln */
		final Cipher cipher=getChiper(password,Cipher.DECRYPT_MODE);
		if (cipher==null) return null;

		final ByteArrayInputStream input;
		try {
			input=new ByteArrayInputStream(cipher.doFinal(data.toByteArray()));
			input.reset();
		} catch (IllegalBlockSizeException | BadPaddingException e) {return null;}

		/* Pr�fsumme und Daten trennen */
		if (input.available()<20) {return null;}
		byte[] sha_orig=new byte[20];
		input.read(sha_orig,0,20);
		byte[] b=new byte[input.available()];
		try {
			input.read(b);
		} catch (IOException e) {return null;}
		byte[] sha_msg=getChecksum(b); if (sha_msg==null) {return null;}
		for (int i=0;i<sha_orig.length;i++) if (sha_orig[i]!=sha_msg[i]) {return null;}

		ByteArrayOutputStream output=new ByteArrayOutputStream();
		try {
			output.write(b);
		} catch (IOException e) {return null;}
		return output;
	}

	/**
	 * Entschl�sselt Daten mit AES
	 * @param data	Zu entschl�sselnde Daten
	 * @param password	Passwort
	 * @return	Entschl�sselte Daten (oder im Fehlerfall <code>null</code>)
	 * @see #encrypt(byte[], String)
	 */
	public static final byte[] decrypt(final byte[] data, final String password) {
		/* Daten entschl�sseln */
		final Cipher cipher=getChiper(password,Cipher.DECRYPT_MODE);
		if (cipher==null) return null;

		final ByteArrayInputStream input;
		try {
			input=new ByteArrayInputStream(cipher.doFinal(data));
			input.reset();
		} catch (IllegalBlockSizeException | BadPaddingException e) {return null;}

		/* Pr�fsumme und Daten trennen */
		if (input.available()<20) {return null;}
		byte[] sha_orig=new byte[20];
		input.read(sha_orig,0,20);
		byte[] b=new byte[input.available()];
		try {
			input.read(b);
		} catch (IOException e) {return null;}
		byte[] sha_msg=getChecksum(b); if (sha_msg==null) {return null;}
		for (int i=0;i<sha_orig.length;i++) if (sha_orig[i]!=sha_msg[i]) {return null;}

		ByteArrayOutputStream output=new ByteArrayOutputStream();
		try {
			output.write(b);
		} catch (IOException e) {return null;}
		return output.toByteArray();
	}
}