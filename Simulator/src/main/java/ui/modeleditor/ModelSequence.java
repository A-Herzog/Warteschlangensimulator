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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import simulator.editmodel.FullTextSearch;

/**
 * Daten eines Fertigungsplans
 * @author Alexander Herzog
 * @see ModelSequences
 */
public final class ModelSequence implements Cloneable {
	/**
	 * Name des XML-Elements, das die Fertigungsplan-Daten enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"Fertigungsplan"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Name des Fertigungsplans
	 * @see #getName()
	 * @see #setName(String)
	 */
	private String name;

	/**
	 * Liste der Fertigungsplan-Schritte
	 * @see #getSteps()
	 */
	private final List<ModelSequenceStep> steps;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelSequence() {
		name="";
		steps=new ArrayList<>();
	}

	/**
	 * Liefert den Namen des Fertigungsplans
	 * @return Name des Fertigungsplans
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen des Fertigungsplans ein
	 * @param name	Namen des Fertigungsplans
	 */
	public void setName(final String name) {
		if (name!=null)	this.name=name;
	}

	/**
	 * Liefert die Liste der Fertigungsplan-Schritte
	 * @return	Liste der Fertigungsplan-Schritte
	 */
	public List<ModelSequenceStep> getSteps() {
		return steps;
	}

	/**
	 * Setzt alle Daten in dem Fertigungsplan-Schritt zurück
	 */
	public void clear() {
		name="";
		steps.clear();
	}

	/**
	 * Vergleicht zwei Fertigungspläne
	 * @param otherSequence	Anderer Fertigungsplan für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Fertigungspläne inhaltlich identisch sind
	 */
	public boolean equalsModelSequence(final ModelSequence otherSequence) {
		if (otherSequence==null) return false;
		if (!name.equals(otherSequence.name)) return false;
		if (steps.size()!=otherSequence.steps.size()) return false;
		for (int i=0;i<steps.size();i++) if (!steps.get(i).equalsModelSequenceStep(otherSequence.steps.get(i))) return false;
		return true;
	}

	/**
	 * Erstellt eine Kopie des Fertigungsplans
	 */
	@Override
	public ModelSequence clone() {
		final ModelSequence clone=new ModelSequence();
		clone.name=name;
		for (ModelSequenceStep step: steps) clone.steps.add(step.clone());
		return clone;
	}

	/**
	 * Speichert das Fertigungsplan-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		node.setAttribute(Language.trPrimary("Surface.XML.Sequence.Name"),name);

		for (ModelSequenceStep step: steps) step.addDataToXML(doc,node);
	}

	/**
	 * Versucht die Daten des Fertigungsplan-Schritt-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		name=Language.trAllAttribute("Surface.XML.Sequence.Name",node);

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			for (String test: ModelSequenceStep.XML_NODE_NAME) if (e.getNodeName().equalsIgnoreCase(test)) {
				final ModelSequenceStep step=new ModelSequenceStep();
				final String error=step.loadFromXML(e);
				if (error!=null) return error+ "("+String.format(Language.tr("Surface.XML.Sequence.LoadErrorInfo"),name)+")";
				steps.add(step);
				break;
			}
		}

		return null;
	}

	/**
	 * Sucht einen Text in den Daten des Fertigungsplans.
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher) {
		searcher.testString(Language.tr("Editor.DialogBase.Search.SequenceName"),name,newName->{name=newName;});
	}
}