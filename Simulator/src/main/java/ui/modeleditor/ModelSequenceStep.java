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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;

/**
 * Daten eines einzelnen Schritts innerhalb eines Fertigungsplans
 * @author Alexander Herzog
 * @see ModelSequence
 */
public final class ModelSequenceStep implements Cloneable {
	/**
	 * Name des XML-Elements, das die Fertiungsplan-Schritt-Daten enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"FertigungsplanSchritt"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Zielstation für diesen Schritt
	 * @see #getTarget()
	 * @see #setTarget(String)
	 */
	private String target;

	/**
	 * Nächster Schritt im Fertigungsplan (0-basierend) oder -1, wenn einfach der nächste Schritt in der Liste angesteuert werden soll
	 * @see #getNext()
	 * @see #setNext(int)
	 */
	private int next;

	/**
	 * Zuordnung der Kundendaten-Zuweisungen
	 * @see #getAssignments()
	 */
	private final Map<Integer,String> assignments;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelSequenceStep() {
		target="";
		next=-1;
		assignments=new HashMap<>();
	}

	/**
	 * Liefert die Zielstation für diesen Schritt
	 * @return	Zielstation für diesen Schritt
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Setzt alle Daten in dem Fertigungsplan-Schritt zurück
	 */
	public void clear() {
		target="";
		next=-1;
		assignments.clear();
	}

	/**
	 * Stellt die Zielstation für diesen Schritt ein
	 * @param target	Zielstation für diesen Schritt
	 */
	public void setTarget(String target) {
		if (target!=null) this.target=target;
	}

	/**
	 * Gibt an, welcher Schritt im Fertigungsplan als nächstes angesteuert werden soll
	 * @return	Nächster Schritt im Fertigungsplan (0-basierend) oder -1, wenn einfach der nächste Schritt in der Liste angesteuert werden soll
	 */
	public int getNext() {
		return next;
	}

	/**
	 * Stellt den als nächstes im Fertigungsplan anzusteuernden Schritt ein
	 * @param next	Nächster Schritt im Fertigungsplan (0-basierend) oder -1, wenn einfach der nächste Schritt in der Liste angesteuert werden soll
	 */
	public void setNext(final int next) {
		if (next<0) this.next=-1; else this.next=next;
	}

	/**
	 * Kundendaten-Zuweisungen, die in diesem Schritt erfolgen sollen
	 * @return	Zuordnung der Kundendaten-Zuweisungen
	 */
	public Map<Integer,String> getAssignments() {
		return assignments;
	}

	/**
	 * Vergleicht zwei Fertigungsplan-Schritte
	 * @param otherStep	Anderer Schritt für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Schritte inhaltlich identisch sind
	 */
	public boolean equalsModelSequenceStep(final ModelSequenceStep otherStep) {
		if (otherStep==null) return false;
		if (!target.equals(otherStep.target)) return false;
		if (next!=otherStep.next) return false;
		if (!Objects.deepEquals(assignments,otherStep.assignments)) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Fertigungsplan-Schrittes
	 */
	@Override
	public ModelSequenceStep clone() {
		final ModelSequenceStep clone=new ModelSequenceStep();
		clone.target=target;
		clone.next=next;
		clone.assignments.putAll(assignments);
		return clone;
	}

	/**
	 * Speichert das Fertigungsplan-Schritt-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		if (!target.isEmpty()) node.setAttribute(Language.trPrimary("Surface.XML.SequenceStep.Target"),target);
		if (next>=0) node.setAttribute(Language.trPrimary("Surface.XML.SequenceStep.Next"),""+next);

		for (Map.Entry<Integer,String> assignment: assignments.entrySet()) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.SequenceStep.Assignment"));
			node.appendChild(sub);
			sub.setAttribute(Language.trPrimary("Surface.XML.SequenceStep.Assignment.Number"),""+assignment.getKey().intValue());
			sub.setTextContent(assignment.getValue());
		}
	}

	/**
	 * Versucht die Daten des Fertigungsplan-Schritt-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		target=Language.trAllAttribute("Surface.XML.SequenceStep.Target",node);
		final String s=Language.trAllAttribute("Surface.XML.SequenceStep.Next",node);
		if (!s.trim().isEmpty()) {
			final Integer I=NumberTools.getNotNegativeInteger(s);
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Surface.XML.SequenceStep.Next"),node.getNodeName());
			next=I.intValue();
		}

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			if (Language.trAll("Surface.XML.SequenceStep.Assignment",e.getNodeName())) {
				final String expression=e.getTextContent().trim();
				if (!expression.isEmpty()) {
					final String nr=Language.trAllAttribute("Surface.XML.SequenceStep.Assignment.Number",e);
					final Integer I=NumberTools.getInteger(nr);
					if (nr==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Surface.XML.SequenceStep.Assignment.Number"),node.getNodeName());
					assignments.put(I,expression);
				}
			}
		}

		return null;
	}
}