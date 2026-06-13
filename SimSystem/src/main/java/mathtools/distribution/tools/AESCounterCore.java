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
package mathtools.distribution.tools;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-basierter PRNG mit Counter-Modus.
 *
 * - Seed = 128-Bit-Key (16 Bytes)
 * - Counter = 128-Bit-Integer, pro Block inkrementiert
 */
public class AESCounterCore extends Random {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2407652402970361420L;

	/**
	 * Verschlüsselungsmodus
	 */
	private static final String AES_TRANSFORMATION="AES/ECB/NoPadding";

	/**
	 * Blockgröße
	 */
	private static final int BLOCK_SIZE=16; /* 128 Bit */

	/**
	 * Internes Objekt zur Durchführung der Verschlüsselung
	 */
	private Cipher cipher;

	/**
	 * Zähler für die Zähler + Verschlüsselung Generierung
	 */
	private byte[] counter;

	/**
	 * Puffer der erzeugten Werte
	 */
	private byte[] buffer;

	/**
	 * Aktuelle Position innerhalb des Puffers
	 * (da die erzeugte Größe und die Ausgabegröße abweichen können)
	 */
	private int bufferPos;

	/**
	 * Erzeugt einen neuen PRNG mit zufälligem 128-Bit-Key (über SecureRandom).
	 */
	public AESCounterCore() {
		this(generateRandomSeed());
	}

	/**
	 * Erzeugt einen neuen PRNG aus einem gegebenen 128-Bit-Key.
	 * @param seed	Seed-Wert
	 */
	public AESCounterCore(final byte[] seed) {
		setSeed(seed);
	}

	/**
	 * Standard-Seed-Konstruktor von java.util.Random.<br<
	 * (Wir mappen den 64-Bit-seed deterministisch auf 128 Bit.)
	 * @param seed	Seed-Wert
	 */
	public AESCounterCore(final long seed) {
		this(longTo128BitSeed(seed));
	}

	@Override
	public void setSeed(final long seed) {
		setSeed(longTo128BitSeed(seed));
	}

	/**
	 * Neuen Seed einstellen.
	 * @param seed	Seed-Wert
	 */
	public void setSeed(final byte[] seed) {
		if (seed.length!=16) {
			throw new IllegalArgumentException("Seed has to be 16 bytes (128 bit) long.");
		}
		try {
			SecretKeySpec keySpec=new SecretKeySpec(seed,"AES");
			cipher=Cipher.getInstance(AES_TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE,keySpec);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("AES initialisation failed", e);
		}

		counter=new byte[BLOCK_SIZE];
		buffer=new byte[BLOCK_SIZE];
		bufferPos=BLOCK_SIZE; /* Start: Buffer ist leer */
	}

	/**
	 * Zufälligen initialen Seed erzeugen.
	 * @return	Seed-Wert
	 */
	private static byte[] generateRandomSeed() {
		byte[] seed = new byte[BLOCK_SIZE];
		new SecureRandom().nextBytes(seed);
		return seed;
	}

	/**
	 * Wandelt einen long-Wert in ein 8 Byte langes byte-Array um
	 * (für die Seed-Generierung).
	 * @param seed	Seed-Wert as long
	 * @return	Seed-Wert als byte[]
	 */
	private static byte[] longTo128BitSeed(final long seed) {
		byte[] buf = new byte[16];
		/* Primitive, aber deterministische Abbildung: Schreibe den long in die ersten 8 Bytes, die nächsten 8 Bytes = invertiert. */
		for (int i = 0; i < 8; i++) {
			buf[i] = (byte) (seed >>> (8 * i));
			buf[8 + i] = (byte) (~seed >>> (8 * i));
		}
		return buf;
	}

	/**
	 * Erhöhung des Zählers nach jedem Schritt.
	 * @see #counter
	 */
	private void incrementCounter() {
		/* 128-bit big-endian Counter */
		for (int i = BLOCK_SIZE - 1; i >= 0; i--) {
			counter[i]++;
			if (counter[i] != 0) {
				break;
			}
		}
	}

	/**
	 * Ausgabepuffer mit den nächsten 8 Bytes auffüllen.
	 */
	private void refillBuffer() {
		incrementCounter();
		try {
			cipher.update(counter, 0, BLOCK_SIZE, buffer, 0);
		} catch (Exception e) {
			throw new RuntimeException("Cipher-Update fehlgeschlagen", e);
		}
		bufferPos = 0;
	}

	/**
	 * Liefert das nächste zufällige Byte aus dem Puffer und füllt diesen, wenn nötig, wieder auf.
	 * @return	Zufälliges Byte
	 * @see #buffer
	 * @see #bufferPos
	 * @see #refillBuffer()
	 */
	private byte nextByteInternal() {
		if (bufferPos>=BLOCK_SIZE) {
			refillBuffer();
		}
		return buffer[bufferPos++];
	}

	/**
	 * Liefert den nächsten zufällige Int-Wert aus dem Puffer und füllt diesen, wenn nötig, wieder auf.
	 * @return	Zufälliges Byte
	 * @see #buffer
	 * @see #bufferPos
	 * @see #refillBuffer()
	 */
	private int nextIntInternal() {
		final int b1=nextByteInternal() & 0xFF;
		final int b2=nextByteInternal() & 0xFF;
		final int b3=nextByteInternal() & 0xFF;
		final int b4=nextByteInternal() & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	@Override
	protected int next(final int bits) {
		/* java.util.Random erwartet, dass next(bits) die unteren 'bits' Bits einer Zufallszahl liefert. Wir verwenden 32-Bit-Blöcke. */
		return nextIntInternal() >>> (32 - bits);
	}

	@Override
	public void nextBytes(final byte[] bytes) {
		int offset = 0;
		int len = bytes.length;
		while (len > 0) {
			if (bufferPos >= BLOCK_SIZE) {
				refillBuffer();
			}
			int toCopy = Math.min(len, BLOCK_SIZE - bufferPos);
			System.arraycopy(buffer, bufferPos, bytes, offset, toCopy);
			bufferPos += toCopy;
			offset += toCopy;
			len -= toCopy;
		}
	}

	@Override
	public int nextInt() {
		return nextIntInternal();
	}

	@Override
	public long nextLong() {
		/* zwei aufeinanderfolgende 32-Bit-Blöcke */
		final long hi=(nextIntInternal() & 0xFFFFFFFFL) << 32;
		final long lo=(nextIntInternal() & 0xFFFFFFFFL);
		return hi | lo;
	}

	@Override
	public boolean nextBoolean() {
		return (next(1) != 0);
	}

	@Override
	public float nextFloat() {
		return next(24) / ((float) (1 << 24));
	}

	@Override
	public double nextDouble() {
		return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
	}
}
