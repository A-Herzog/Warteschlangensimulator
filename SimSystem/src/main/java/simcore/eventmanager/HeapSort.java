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
package simcore.eventmanager;

/**
 * HeapSort
 * @see <a href="https://de.wikibooks.org/wiki/Algorithmensammlung:_Sortierverfahren:_Heapsort#Java">https://de.wikibooks.org/wiki/Algorithmensammlung:_Sortierverfahren:_Heapsort#Java</a>
 */
public class HeapSort {
	private HeapSort() {}

	/**
	 * Sortiert ein Object-Array mit heapsort
	 * @param a das Array
	 * @param toIndex bis zu diesem index sortieren (exklusive)
	 */
	public static void sort(Object[] a, int toIndex) {
		generateMaxHeap(a,toIndex);

		//hier wird sortiert
		for(int i = toIndex -1; i > 0; i--) {
			vertausche(a, i, 0);
			versenke(a, 0, i);
		}
	}

	/**
	 * Erstellt einen MaxHeap Baum im Array
	 * @param a das Array
	 * @param toIndex bis zu diesem index sortieren (exklusive)
	 */
	private static void generateMaxHeap(Object[] a, int toIndex) {
		//starte von der Mitte rückwärts.
		for(int i = (toIndex / 2) - 1; i >= 0 ; i--) {
			versenke(a, i, toIndex);
		}
	}

	/**
	 * versenkt ein element im baum
	 * @param a Das Array
	 * @param i Das zu versenkende Element
	 * @param n Die letzte Stelle im Baum die beachtet werden soll
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void versenke(Object[] a, int i, int n) {
		while(i <= (n / 2) - 1) {
			int kindIndex = ((i+1) * 2) - 1; // berechnet den Index des linken kind

			//bestimme ob ein rechtes Kind existiert
			if(kindIndex + 1 <= n -1) {
				//rechtes kind existiert

				if(((Comparable)a[kindIndex]).compareTo(a[kindIndex+1])<0) {
					kindIndex++; // wenn rechtes kind größer ist nimm das
				}
			}

			//teste ob element sinken muss
			if(((Comparable)a[i]).compareTo(a[kindIndex])<0) {
				vertausche(a,i,kindIndex); //element versenken
				i = kindIndex; // wiederhole den vorgang mit der neuen position
			} else break;
		}
	}

	/**
	 * Vertauscht die arraypositionen von i und kindIndex
	 * @param a Das Array in dem getauscht wird
	 * @param i der erste index
	 * @param kindIndex der 2. index
	 */
	private static void vertausche(Object[] a, int i, int kindIndex) {
		Object z = a[i];
		a[i] = a[kindIndex];
		a[kindIndex] = z;
	}


}
