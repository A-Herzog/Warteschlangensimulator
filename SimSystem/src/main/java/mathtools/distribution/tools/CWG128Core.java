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

/**
 * Basis Collatz-Weyl 128-Bit-Generator noch ohne Seed-Aufbereitung
 * und ohne die notwendigen Funktionen, um Zufallszahlen gemäß
 * bestimmter Datentypen zu generieren. Der Seed kann hier ebenfalls
 * nachträglich nicht verändert werden. Der übergeordnete Generator
 * legt einfach ein neues Objekt dieses Typs an, um einen neuen
 * Seed einzustellen.<br>
 * <a href="https://arxiv.org/pdf/2312.17043">https://arxiv.org/pdf/2312.17043</a>
 * @see CWG128Random
 */
public final class CWG128Core {
	/**
	 * Interner Zustand
	 */
	private final UInt128[] c=new UInt128[4];

	/**
	 * Konstruktor.<br>
	 * c[0] MUSS ungerade sein.<br>
	 * Alle Werte werden als unsigned 128-Bit interpretiert.<br>
	 * @param c0	Seed-Wert 1
	 * @param c1	Seed-Wert 2
	 * @param c2	Seed-Wert 3
	 * @param c3	Seed-Wert 4
	 */
	public CWG128Core(final UInt128 c0, final UInt128 c1, final UInt128 c2, final UInt128 c3) {
		this.c[0]=new UInt128(c0);
		this.c[1]=new UInt128(c1);
		this.c[2]=new UInt128(c2);
		this.c[3]=new UInt128(c3);

		/* Sicherstellen, dass c[0] ungerade ist */
		this.c[0].or1();
	}

	/**
	 * Objekt c1Shift aus {@link #nextBlock(UInt128)} aufheben
	 * @see #nextBlock(UInt128)
	 */
	private final UInt128 c1Shift=new UInt128();

	/**
	 * Objekt factor aus {@link #nextBlock(UInt128)} aufheben
	 * @see #nextBlock(UInt128)
	 */
	private final UInt128 factor=new UInt128();

	/**
	 * Objekt mult aus {@link #nextBlock(UInt128)} aufheben
	 * @see #nextBlock(UInt128)
	 */
	private final UInt128 mult=new UInt128();

	/**
	 * Führt einen CWG128-Schritt aus und liefert das 128-Bit-Ergebnis
	 * (statt nur 32 Bits). Das Resultat entspricht konzeptionell der
	 * "internen" 128-Bit-Ausgabe. Daraus können beliebig viele
	 * ints/longs abgeleitet werden.
	 * @param out	 128-Bit-Ausgabewert (Inhalt wird überschrieben)
	 */
	public void nextBlock(final UInt128 out) {
		/* c[1] >> 1 */
		c1Shift.set(c[1]);
		c1Shift.shiftRight1();

		/* (c[2] += c[1]) */
		c[2].add(c[1]);

		/* ((c[2]) | 1) */
		factor.set(c[2]);
		factor.or1();

		/* (c[1] >> 1) * ((c[2] += c[1]) | 1) */
		multiply128(c1Shift, factor, mult);

		/* (c[3] += c[0]) */
		c[3].add(c[0]);

		/* c[1] = mult ^ c[3] */
		c[1] = mult;
		c[1].xor(c[3]);

		final int upper32 = c[2].top32();
		final int low32 = (int)c[1].lo;
		final long lo = ((long)upper32 << 32) ^ (low32 & 0xffff_ffffL);
		final long hi = c[1].hi ^ c[2].hi;

		out.hi = hi;
		out.lo = lo;
	}

	/**
	 * Hilfsfunktion: 128x128 -> 128 (niedrigste 128 Bits von Produkt)
	 * @param x	Faktor 1
	 * @param y	Faktor 2
	 * @param result	Produkt (Inhalt wird überschrieben)
	 */
	private static void multiply128(final UInt128 x, final UInt128 y, final UInt128 result) {
		final long x0 = x.lo;
		final long x1 = x.hi;
		final long y0 = y.lo;
		final long y1 = y.hi;

		/* 1) 64x64 -> 128: low product p0 = x0*y0 */
		final long x0l = x0 & 0xffff_ffffL;
		final long x0h = x0 >>> 32;
		final long y0l = y0 & 0xffff_ffffL;
		final long y0h = y0 >>> 32;

		final long p00 = x0l * y0l; /* <= 64 bits */
		final long p01 = x0l * y0h;
		final long p10 = x0h * y0l;
		final long p11 = x0h * y0h;

		final long carry = ((p01 & 0xffff_ffffL) + (p10 & 0xffff_ffffL) + (p00 >>> 32)) >>> 32;
		final long hi0 = p11 + (p01 >>> 32) + (p10 >>> 32) + carry;
		final long lo0 = (p00 & 0xffff_ffffL) | ((p01 + p10 + (p00 >>> 32)) << 32);

		/* 2) mid = x0*y1 + x1*y0 (nur untere 64 Bits; overflow nach oben fällt in >128 Bits) */
		final long mid1 = x0 * y1;
		final long mid2 = x1 * y0;
		final long mid = mid1 + mid2;

		/* 3) high = hi0 + mid (mit Carry aus lo0 + (mid << 64), aber (mid<<64) beeinflusst nur high) */
		final long hi = hi0 + mid;

		/* Ergebnis modulo 2^128: high=hi, low=lo0 */
		result.hi=hi;
		result.lo=lo0;
	}
}
