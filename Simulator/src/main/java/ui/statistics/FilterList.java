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
package ui.statistics;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Liste mit allen Filter-Listen-Einträgen
 * @author Alexander Herzog
 * @see FilterListRecord
 */
public final class FilterList {
	/**
	 * Liste der Einträge
	 * @see #getList()
	 */
	private final List<FilterListRecord> list;

	/**
	 * Konstruktor der Klasse
	 */
	public FilterList() {
		list=new ArrayList<>();
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param list	Ausgangsliste von der die Einträge kopiert werden sollen
	 */
	public FilterList(final List<FilterListRecord> list) {
		this.list=new ArrayList<>(list.stream().map(record->new FilterListRecord(record)).collect(Collectors.toList()));
	}

	@Override
	public int hashCode() { /* Andernfalls meint FindBugs, dass ich equals nicht überschreiben darf. */
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object list) {
		if (!(list instanceof FilterList)) return false;
		return Objects.deepEquals(((FilterList)list).list,list);
	}

	/**
	 * Liefert die Einträge der Liste
	 * @return	Einträge der Liste
	 */
	public List<FilterListRecord> getList() {
		return list;
	}

	/**
	 * Lädt die Daten der Liste aus einem String
	 * @param data	String aus dem die Daten geladen werden sollen
	 * @return	Gibt an, ob alle Zeilen gelesen werden konnten.
	 */
	public boolean load(final String data) {
		boolean canReadAll=true;
		list.clear();
		final String[] lines=data.split("\n");
		int position=0;
		while (position<lines.length) {
			final String line=lines[position].trim();
			boolean ok=false;
			for (FilterListRecord.Mode m: FilterListRecord.Mode.values()) {
				if (m.fileTag.equals(line)) {
					final FilterListRecord record=new FilterListRecord();
					record.mode=m;
					if (m.hasText && position<lines.length-1) {
						record.text=decodeMultiLine(lines[position+1].trim());
						position++;
					}
					list.add(record);
					ok=true;
					break;
				}
			}
			if (!ok) canReadAll=false;
			position++;
		}
		return canReadAll;
	}

	/**
	 * Speichert die Daten der Liste in einem String
	 * @return	String in dem die Daten der Liste gespeichert werden
	 */
	public String save() {
		return save(list.iterator());
	}

	private static String decodeMultiLine(final String line) {
		byte[] buffer=null;
		try {buffer=Base64.getDecoder().decode(line);} catch (IllegalArgumentException e) {buffer=null;}
		if (buffer==null) return "";
		return new String(buffer);
	}

	private static String encodeMultiLine(final String lines) {
		return Base64.getEncoder().encodeToString(lines.getBytes());
	}

	/**
	 * Überträgt die Einträge aus einer Liste in einen String
	 * @param list	Liste aus der die Einträge in einem String gespeichert werden sollen
	 * @return	String in dem die Daten der Liste gespeichert werden
	 */
	public static String save(final Enumeration<FilterListRecord> list) {
		final StringBuilder sb=new StringBuilder();
		while (list.hasMoreElements()) {
			final FilterListRecord record=list.nextElement();
			sb.append(record.mode.fileTag);
			sb.append("\n");
			if (record.mode.hasText) {
				sb.append(encodeMultiLine(record.text));
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Überträgt die Einträge aus einer Liste in einen String
	 * @param list	Liste aus der die Einträge in einem String gespeichert werden sollen
	 * @return	String in dem die Daten der Liste gespeichert werden
	 */
	public static String save(final Iterator<FilterListRecord> list) {
		final StringBuilder sb=new StringBuilder();
		while (list.hasNext()) {
			final FilterListRecord record=list.next();
			sb.append(record.mode.fileTag);
			sb.append("\n");
			if (record.mode.hasText) {
				sb.append(encodeMultiLine(record.text));
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}