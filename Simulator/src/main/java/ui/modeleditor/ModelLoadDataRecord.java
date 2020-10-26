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
package ui.modeleditor;

import org.apache.jena.ext.com.google.common.base.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import ui.parameterseries.ParameterCompareSetupValueInput;
import ui.parameterseries.ParameterCompareSetupValueInputListDialog;

/**
 * Einzelner Datensatz innerhalb eines {@link ModelLoadData}-Elements
 * @author Alexander Herzog
 * @see ModelLoadData
 */
public final class ModelLoadDataRecord implements Cloneable {
	/**
	 * Bezeichner der Tabellenzelle, dessen Wert verwendet werden soll
	 * @see #getCell()
	 * @see #setCell(String)
	 */
	private String cell;

	/**
	 * Objekt, welches den Datenpunkt innerhalb des Modells, der verändert werden soll, spezifiziert
	 * @see #getChange()
	 */
	private final ParameterCompareSetupValueInput change;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelLoadDataRecord() {
		change=new ParameterCompareSetupValueInput();
		clear();
	}

	/**
	 * Löscht alle Daten in diesem Objekt.
	 */
	public void clear() {
		cell="";
		change.clear();
	}

	@Override
	public ModelLoadDataRecord clone() {
		final ModelLoadDataRecord clone=new ModelLoadDataRecord();
		clone.copyDataFrom(this);
		return clone;
	}

	/**
	 * Kopiert die Daten aus einem anderen Objekt in dieses
	 * @param source	Ausgangsobjekt aus dem die Daten geladen werden sollen
	 */
	public void copyDataFrom(final ModelLoadDataRecord source) {
		cell=source.cell;
		change.copyDataFrom(source.change);
	}

	/**
	 * Vergleicht dieses Objekt mit einem weiteren
	 * @param otherRecord	Weiteres Datenobjekt das mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn beide Objekte inhaltlich identisch sind
	 */
	public boolean equalsRecord(final ModelLoadDataRecord otherRecord) {
		if (otherRecord==null) return false;

		if (!Objects.equal(cell,otherRecord.cell)) return false;
		if (!change.equalsParameterCompareSetupValueInput(otherRecord.change)) return false;

		return true;
	}

	/**
	 * Liefert den Bezeichner der Tabellenzelle, dessen Wert verwendet werden soll.
	 * @return	Bezeichner der Tabellenzelle, dessen Wert verwendet werden soll
	 */
	public String getCell() {
		return (cell==null)?"":cell;
	}

	/**
	 * Stellt den Bezeichner der Tabellenzelle, dessen Wert verwendet werden soll, ein.
	 * @param cell	Bezeichner der Tabellenzelle, dessen Wert verwendet werden soll
	 */
	public void setCell(String cell) {
		this.cell=(cell==null)?"":cell;
	}

	/**
	 * Liefert das Objekt, welches den Datenpunkt innerhalb des Modells, der verändert werden soll, spezifiziert.
	 * @return	Objekt, welches den Datenpunkt innerhalb des Modells, der verändert werden soll, spezifiziert
	 */
	public ParameterCompareSetupValueInput getChange() {
		return change;
	}

	/**
	 * Speichert die Informationen zu den zu ladenden Daten in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {

		final Element node=doc.createElement(Language.tr("ModelLoadData.XML.Record"));
		parent.appendChild(node);

		final Element sub=doc.createElement(Language.tr("ModelLoadData.XML.Record.Cell"));
		node.appendChild(sub);
		sub.setTextContent(cell);

		change.addDataToXML(doc,node);
	}

	/**
	 * Versucht den Datensatz aus einem xml-Element zu laden
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			if (Language.trAll("ModelLoadData.XML.Record.Cell",e.getNodeName())) {
				cell=e.getTextContent();
				continue;
			}

			if (Language.trAll("ParameterCompare.XML.Inputs.Root",e.getNodeName())) {
				final ParameterCompareSetupValueInput change=new ParameterCompareSetupValueInput();
				final String error=change.loadFromXML(e);
				if (error!=null) return error;
				this.change.copyDataFrom(change);
				continue;
			}
		}

		return null;
	}

	/**
	 * Liefert einen HTML-Text zur Anzeige in der Liste im Bearbeiten-Dialog {@link ModelLoadDataDialog}.
	 * @return	HTML-Text zur Anzeige in der Liste im Bearbeiten-Dialog
	 * @see ModelLoadDataDialog
	 */
	public String getListText() {
		final StringBuilder text=new StringBuilder();
		text.append("<html><body>\n");
		text.append("<p>"+Language.tr("ModelLoadData.ListRecord.Cell")+": <b>"+cell+"</b></p>\n");
		text.append("<p>"+ParameterCompareSetupValueInputListDialog.getInputInfo(change)+"</p>\n");
		text.append("</body></html>\n");
		return text.toString();
	}
}
