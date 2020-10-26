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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import tools.SetupData;
import ui.modeleditor.ModelElementCatalog;
import xml.XMLTools;

/**
 * Diese Klasse h�lt alle Elementenvorlagen vor.<br>
 * Die Klasse ist optional als Singleton verf�gbar.
 * @author Alexander Herzog
 * @see UserTemplate
 */
public final class UserTemplates implements Cloneable {
	/** Instanz f�r die globalen Vorlagen */
	private static UserTemplates instance;
	/** Liste aller Vorlagen */
	private final List<UserTemplate> templates;
	/** Katalog aller verf�gbaren Elemente */
	private static final ModelElementCatalog catalog=ModelElementCatalog.getCatalog();

	/**
	 * Konstruktor der Klasse.<br>
	 * @see UserTemplates#getInstance()
	 */
	public UserTemplates() {
		templates=new ArrayList<>();
	}

	/**
	 * Liefert die Instanz des Singletons f�r die globalen Vorlagen.
	 * @return	Instanz des Singletons f�r die globalen Vorlagen
	 */
	public static synchronized UserTemplates getInstance() {
		if (instance==null) {
			instance=new UserTemplates();
			instance.loadGlobalTemplates();
		}
		return instance;
	}

	/**
	 * Liefert die Datei zum Speichern der globalen Vorlagen
	 * @return	Datei zum Speichern der globalen Vorlagen
	 */
	private File getGlobalTemplatesFile() {
		return new File(SetupData.getSetupFolder(),"Templates.cfg");
	}

	/**
	 * Legt eine neue Vorlage an und f�gt diese zur Vorlagenliste hinzu.
	 * @return	Neue Vorlage
	 */
	public UserTemplate add() {
		final UserTemplate template=new UserTemplate();
		templates.add(template);
		return template;
	}

	/**
	 * F�gt eine bestehende neue Vorlage zur Vorlagenliste hinzu.
	 * @param template	Vorlage, die in die Liste aufgenommen werden soll.
	 */
	public void add(final UserTemplate template) {
		templates.add(template);
	}

	/**
	 * Liefert eine Liste aller Vorlagen (Originalliste mit Originaleintr�gen)
	 * @return	Liste aller Vorlagen
	 */
	public List<UserTemplate> getTemplates() {
		return templates;
	}

	/**
	 * Pr�ft, ob eine Vorlage in der Liste enthalten ist.
	 * @param template	Vorlage bei der gepr�ft werden soll, ob diese in der Liste enthalten ist.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Vorlage in der Liste enthalten ist.
	 */
	public boolean contains(final UserTemplate template) {
		if (template==null) return false;
		return templates.contains(template);
	}

	/**
	 * L�scht alle Vorlagen.
	 */
	public void clear() {
		templates.clear();
	}

	/**
	 * Entfernt eine Vorlage aus der Liste.
	 * @param template	Zu entfernende Vorlage
	 * @return	Gibt <code>true</code> zur�ck, wenn die Vorlage in der Liste enthalten war und entfernt werden konnte.
	 */
	public boolean remove(final UserTemplate template) {
		if (template==null) return false;
		return templates.remove(template);
	}

	/**
	 * L�dt die Vorlagen aus einem Knoten
	 * @param parent	Basisknoten f�r die Vorlagen
	 * @see UserTemplates#isTemplatesNode(Element)
	 * @see UserTemplates#isTemplatesNode(String)
	 */
	public void load(final Element parent) {
		clear();

		final NodeList l=parent.getChildNodes();
		final int size=l.getLength();
		for (int i=0; i<size;i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			if (Language.trAll("UserTemplates.XML.Template",e.getNodeName())) {
				final UserTemplate template=new UserTemplate();
				if (template.load(e,catalog)) {
					templates.add(template);
				}
			}
		}
	}

	/**
	 * Initialisiert die Liste der globalen Vorlagen.
	 * @see #getInstance()
	 */
	private void loadGlobalTemplates() {
		final File templatesFile=getGlobalTemplatesFile();
		if (templatesFile==null) return;

		final XMLTools xml=new XMLTools(templatesFile);
		final Element root=xml.load();
		if (root==null) return;

		load(root);
	}

	/**
	 * Pr�ft, ob ein Knoten der Basisknoten f�r die Vorlagen ist
	 * @param node	Zu pr�fender Knoten
	 * @return	Liefert <code>true</code> zur�ck, wenn der Knoten der Basisknoten f�r die Vorlagen ist
	 */
	public static boolean isTemplatesNode(final Element node) {
		return Language.trAll("UserTemplates.XML.Templates",node.getNodeName());
	}

	/**
	 * Pr�ft, ob ein Knoten der Basisknoten f�r die Vorlagen ist
	 * @param nodeName	Name des zu pr�fenden Knoten
	 * @return	Liefert <code>true</code> zur�ck, wenn der Knoten der Basisknoten f�r die Vorlagen ist
	 */
	public static boolean isTemplatesNode(final String nodeName) {
		return Language.trAll("UserTemplates.XML.Templates",nodeName);
	}

	/**
	 * Speichert die Vorlagen als Unterelemente eines Knoten
	 * @param doc	XML-Dokument
	 * @param parent	�bergeordneter Knoten
	 */
	public void save(final Document doc, final Element parent) {
		final Element node;
		parent.appendChild(node=doc.createElement(Language.trPrimary("UserTemplates.XML.Templates")));

		templates.stream().forEach(template->template.store(doc,node));
	}

	/**
	 * Speichert die globalen Vorlagen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean saveGlobalTemplates() {
		final File templatesFile=getGlobalTemplatesFile();

		if (templates.size()==0) {
			/* Keine globalen Templates, Datei l�schen */
			if (templatesFile.isFile()) return templatesFile.delete();
			return true;
		} else {
			/* Globale Templates speichern */
			final XMLTools xml=new XMLTools(templatesFile);
			final Element root=xml.generateRoot(Language.trPrimary("UserTemplates.XML.Templates"),true);
			final Document doc=root.getOwnerDocument();
			templates.stream().forEach(template->template.store(doc,root));
			return xml.save(root,true);
		}
	}

	/**
	 * Erstellt eine Kopie der Nutzervorlagenliste.
	 */
	@Override
	public UserTemplates clone() {
		final UserTemplates clone=new UserTemplates();
		for (UserTemplate template: templates) clone.templates.add(template.clone());
		return clone;
	}

	/**
	 * Vergleicht die Nutzervorlagenliste mit einer anderen Nutzervorlagenliste.
	 * @param otherTemplates	Andere Nutzervorlagenliste die mit diesem Objekt verglichen werden soll.
	 * @return	Liefert <code>true</code>, wenn beide Nutzervorlagenliste inhaltlich identisch sind.
	 */
	public boolean equalsTemplates(final UserTemplates otherTemplates) {
		if (otherTemplates==null) return false;
		if (templates.size()!=otherTemplates.templates.size()) return false;
		for (int i=0;i<templates.size();i++) if (!templates.get(i).equalsTemplate(otherTemplates.templates.get(i))) return false;
		return true;
	}
}