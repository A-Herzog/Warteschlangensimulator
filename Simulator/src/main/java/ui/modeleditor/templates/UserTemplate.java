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
package ui.modeleditor.templates;

import java.io.ByteArrayInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.ModelSurface;

/**
 * Diese Klasse hält eine einzelne Elementenvorlage vor.
 * @author Alexander Herzog
 * @see UserTemplates
 */
public final class UserTemplate implements Cloneable {
	/** Oberfläche, die die Elemente enthält */
	private ModelSurface surface;
	/** Name der Elementenvorlage */
	private String name;

	/**
	 * Konstruktor der Klasse
	 */
	public UserTemplate() {
		final EditModel model=new EditModel();
		surface=new ModelSurface(model,model.resources,model.schedules,null);
		name="";
	}

	/**
	 * Konstruktor der Klasse
	 * @param data	Elementdaten, die in die Vorlage übernommen werden sollen
	 * @param name	Name der neuen Vorlage
	 */
	public UserTemplate(final ByteArrayInputStream data, final String name) {
		this();
		setName(name);
		UserTemplateTools.copyToTemplate(data,this);
	}

	/**
	 * Liefert die Oberfläche, die die Elemente enthält.
	 * @return	Oberfläche, die die Elemente enthält
	 */
	public ModelSurface getSurface() {
		return surface;
	}

	/**
	 * Liefert den Namen der Elementenvorlage
	 * @return	Name der Elementenvorlage
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen der Elementenvorlage ein
	 * @param name	Name der Elementenvorlage
	 */
	public void setName(final String name) {
		this.name=(name!=null)?name:"";
	}

	/**
	 * Speichert die Elementenvorlage in einem XML-Element
	 * @param doc	XML-Dokument
	 * @param parent	Übergeordnetes Element
	 */
	void store(final Document doc, final Element parent) {
		final Element node=doc.createElement(Language.trPrimary("UserTemplates.XML.Template"));
		parent.appendChild(node);
		node.setAttribute(Language.trPrimary("UserTemplates.XML.Template.Name"),name);
		surface.addDataToXML(doc,node);
	}

	/**
	 * Lädt die Elementenvorlage aus einem XML-Element
	 * @param node	Knoten aus dem die Daten geladen werden sollen
	 * @param catalog	Elementekatalog zur Identifikation und Instanzierung der Elemente
	 * @return	Liefert <code>true</code> zurück, wenn die Elementenvorlage korrekt geladen werden konnte
	 */
	boolean load(final Element node, final ModelElementCatalog catalog) {
		name=Language.trAllAttribute("UserTemplates.XML.Template.Name",node);

		final NodeList l=node.getChildNodes();
		final int size=l.getLength();
		for (int i=0; i<size;i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			final String name=e.getNodeName();

			boolean ok=false;
			for (String test: ModelSurface.XML_NODE_NAME) if (test.equalsIgnoreCase(name)) {ok=true; break;}
			if (!ok) continue;

			return surface.loadFromXML(e)==null;
		}

		return false;
	}

	/**
	 * Erstellt eine Kopie der Nutzervorlage.
	 */
	@Override
	public UserTemplate clone() {
		final UserTemplate clone=new UserTemplate();
		clone.name=name;
		final EditModel model=new EditModel();
		clone.surface=surface.clone(false,model.resources,model.schedules,null,model);
		return clone;
	}

	/**
	 * Vergleicht die Nutzervorlage mit einer anderen Nutzervorlage.
	 * @param otherTemplate	Andere Nutzervorlage die mit diesem Objekt verglichen werden soll.
	 * @return	Liefert <code>true</code>, wenn beide Nutzervorlagen inhaltlich identisch sind.
	 */
	public boolean equalsTemplate(final UserTemplate otherTemplate) {
		if (otherTemplate==null) return false;
		if (!name.equals(otherTemplate.name)) return false;
		if (!surface.equalsModelSurface(otherTemplate.surface)) return false;
		return true;
	}

	/**
	 * Gibt an, wie viele Elemente und Stationen in dem Template enthalten sind.
	 * @return	2-elementiges Array aus Anzahl an Elementen insgesamt und Anzahl an Stationen
	 */
	public int[] getInfo() {
		return surface.getElementAndStationCount();
	}
}