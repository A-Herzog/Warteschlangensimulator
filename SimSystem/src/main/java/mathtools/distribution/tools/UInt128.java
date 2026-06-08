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
 * Implementierung des C-Datentyps unit128 als Java-Klasse<br>
 * Wird für {@link CWG128Core} benötigt.
 * @see CWG128Core
 */
public final class UInt128 {
	/**
	 * higher 64 bits
	 */
	public long hi;

	/**
	 * lower 64 bits
	 */
	public long lo;

	/**
	 * Konstruktor
	 */
	public UInt128() {
		hi=0;
		lo=0;
	}

	/**
	 * Konstruktor
	 * @param hi	higher 64 bits
	 * @param lo	lower 64 bits
	 */
	public UInt128(final long hi, final long lo) {
		this.hi=hi;
		this.lo=lo;
	}

	/**
	 * Copy-Konstruktor
	 * @param source	Zu kopierendes UInt128-Objekt
	 */
	public UInt128(final UInt128 source) {
		this.hi=source.hi;
		this.lo=source.lo;
	}

	/**
	 * Kopiert die Daten aus einem anderen UInt128-Objekt.
	 * @param other	Ausgangsobjekt aus dem die Daten übernommen werden sollen
	 */
	public void set(final UInt128 other) {
		hi=other.hi;
		lo=other.lo;
	}

	/**
	 * Add unsigned 128-bit (this += other).
	 * @param other	Other UInt128
	 */
	public void add(final UInt128 other) {
		long oldLo = this.lo;
		this.lo += other.lo;
		/* carry if unsigned overflow in low part */
		long carry = ((oldLo ^ this.lo) & (oldLo ^ other.lo)) < 0 ? 1L : 0L;
		this.hi += other.hi + carry;
	}

	/**
	 * Add unsigned 64-bit (this += value, treated as low 64 bits).
	 * @param value	Value to add
	 */
	public void add(final long value) {
		long oldLo = this.lo;
		this.lo += value;
		long carry = ((oldLo ^ this.lo) & (oldLo ^ value)) < 0 ? 1L : 0L;
		this.hi += carry;
	}

	/**
	 * Right shift logical by 1 (>> 1 unsigned).
	 */
	public void shiftRight1() {
		long newLo = this.lo >>> 1;
		long lsbHi = this.hi & 1L;
		newLo |= (lsbHi << 63); /* carry down from hi */
		this.lo = newLo;
		this.hi = this.hi >>> 1; /* logical shift; hi treated as unsigned */
	}

	/**
	 * Bitwise OR with 1 (only low word relevant).
	 */
	public void or1() {
		this.lo |= 1L;
	}

	/**
	 * XOR with other 128-bit word.
	 * @param other	Other UInt128
	 */
	public void xor(final UInt128 other) {
		this.lo ^= other.lo;
		this.hi ^= other.hi;
	}

	/**
	 * Get top 32 bits (hi >> 32).
	 * @return	Top 32 bit of hi
	 */
	public int top32() {
		return (int)(hi >>> 32);
	}
}
