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
package ui.modeleditor.elements;

/**
 * Elemente, die dieses Interface implementieren, können über
 * das {@link DDEEditPanel} konfiguriert werden.
 * @author Alexander Herzog
 */
public interface ElementWithDDEInputOutput {

	/**
	 * Liefert den Namen der Arbeitsmappe aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen.
	 * @return	Name der Arbeitsmappe aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen
	 */
	String getWorkbook();

	/**
	 * Stellt den Namen der Arbeitsmappe aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen ein.
	 * @param workbook	Name der Arbeitsmappe aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen
	 */
	void setWorkbook(final String workbook);

	/**
	 * Liefert den Namen der Tabelle aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen.
	 * @return	Name der Tabelle aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen
	 */
	String getTable();

	/**
	 * Stellt den Namen der Tabelle aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen ein.
	 * @param table	Name der Tabelle aus der die Daten ausgelesen bzw. in die die Daten geschrieben werden sollen
	 */
	void setTable(final String table);

	/**
	 * Liefert die 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen bzw. in die Daten geschrieben werden sollen.
	 * @return	1-basierende Nummer der ersten Zeile aus der Daten ausgelesen bzw. in die Daten geschrieben werden sollen
	 */
	int getStartRow();

	/**
	 * Stellt die 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen bzw. in die Daten geschrieben werden sollen ein.
	 * @param startRow	1-basierende Nummer der ersten Zeile aus der Daten ausgelesen bzw. in die Daten geschrieben werden sollen
	 */
	void setStartRow(final int startRow);

	/**
	 * Liefert den Namen der Spalte aus der die Daten ausgelesen bzw. in die Daten geschrieben werden sollen.
	 * @return	Name der Spalte aus der die Daten ausgelesen bzw. in die Daten geschrieben werden sollen
	 */
	String getColumn();

	/**
	 * Stellt den Namen der Spalte aus der die Daten ausgelesen bzw. in die Daten geschrieben werden sollen ein.
	 * @param column	Name der Spalte aus der die Daten ausgelesen bzw. in die Daten geschrieben werden sollen
	 */
	void setColumn(final String column);
}
