/**
 * Copyright 2022 Alexander Herzog
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
package systemtools.statistics;

/**
 * Hält die Seitenränder in pt vor.
 * @author Alexander Herzog
 * @see PDFWriter
 */
public final class PDFWriterBorderPT {
	/**
	 * Oberer Seitenrand (in pt)
	 */
	public final int top;

	/**
	 * Rechter Seitenrand (in pt)
	 */
	public final int right;

	/**
	 * Unterer Seitenrand (in pt)
	 */
	public final int bottom;

	/**
	 * Linker Seitenrand (in pt)
	 */
	public final int left;

	/**
	 * Konstruktor der Klasse
	 * @param topMM	Oberer Seitenrand in mm
	 * @param rightMM	Rechter Seitenrand in mm
	 * @param bottomMM	Unterer Seitenrand in mm
	 * @param leftMM	Linker Seitenrand in mm
	 */
	public PDFWriterBorderPT(final int topMM, final int rightMM, final int bottomMM, final int leftMM) {
		top=(int)Math.round(topMM/25.4*72);
		right=(int)Math.round(rightMM/25.4*72);
		bottom=(int)Math.round(bottomMM/25.4*72);
		left=(int)Math.round(leftMM/25.4*72);
	}

	/**
	 * Konstruktor der Klasse
	 * @param style	Konfigurationsobjekt aus dem die Seitenränder ausgelesen werden sollen
	 */
	public PDFWriterBorderPT(final ReportStyle style) {
		this(style.borderTopMM,style.borderRightMM,style.borderBottomMM,style.borderLeftMM);
	}
}
