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
package ui.parameterseries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import ui.ModelChanger;
import ui.ModelChanger.Mode;

/**
 * Hält die Definition einer Eingabegröße für die Parameter-Vergleichs-Funktion vor.
 * @author Alexander Herzog
 * @see ParameterCompareSetup
 */
public final class ParameterCompareSetupValueInput extends ParameterCompareSetupBase implements Cloneable {
	private ModelChanger.Mode mode;
	private int xmlMode;
	private String tag;

	/**
	 * Konstruktor der Klasse
	 */
	public ParameterCompareSetupValueInput() {
		super();
		clear();
	}

	/**
	 * Löscht alle Daten in diesem Objekt.
	 */
	public void clear() {
		mode=ModelChanger.Mode.MODE_RESOURCE;
		xmlMode=0;
		tag="";
	}

	/**
	 * Liefert den aktuellen Modus der Eingabegröße
	 * @return	Modus der Eingabegröße
	 * @see Mode
	 */
	public ModelChanger.Mode getMode() {
		return mode;
	}

	/**
	 * Stellt den Modus der Eingabegröße ein
	 * @param mode	Modus der Eingabegröße
	 * @see Mode
	 */
	public void setMode(final ModelChanger.Mode mode) {
		if (mode!=null) this.mode=mode;
	}

	/**
	 * Ist als Modus ein XML-Element gewählt, so liefert diese Funktion die Art, wie
	 * die Größe in dem XML-Element verändert werden soll
	 * @return	Art, wie die Größe im XML-Element verändert werden soll
	 * @see ModelChanger#XML_ELEMENT_MODES
	 */
	public int getXMLMode() {
		return xmlMode;
	}

	/**
	 * Stellt im Falle, dass ein XML-Element gewählt wurde,
	 * die Art, wie die Größe in dem XML-Element verändert werden soll, ein.
	 * @param xmlMode	Art wie die Größe in dem XML-Element verändert werden soll
	 * @see ModelChanger#XML_ELEMENT_MODES
	 */
	public void setXMLMode(final int xmlMode) {
		if (xmlMode>=0 && xmlMode<=6) this.xmlMode=xmlMode;
	}

	/**
	 * Liefert den Namen der gewählten Ressource / der globalen Variable / des XML-Elements
	 * @return	Namen der gewählten Ressource / der globalen Variable / des XML-Elements
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Stellt den Namen der gewählten Ressource / der globalen Variable / des XML-Elements ein.
	 * @param tag	Namen der gewählten Ressource / der globalen Variable / des XML-Elements
	 */
	public void setTag(final String tag) {
		if (tag!=null) this.tag=tag.trim();
	}

	/**
	 * Vergleich den Eingabe-Einstellungen-Datensatz mit einem anderen Einstellungen-Objekt
	 * @param otherInput	Anderes Einstellungen-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsParameterCompareSetupValueInput(final ParameterCompareSetupValueInput otherInput) {
		if (!getName().equals(otherInput.getName())) return false;
		if (mode!=otherInput.mode) return false;
		if (xmlMode!=otherInput.xmlMode) return false;
		if (!tag.equals(otherInput.tag)) return false;
		return true;
	}

	/**
	 * Kopiert die Daten aus einem anderen Objekt in dieses
	 * @param source	Ausgangsobjekt aus dem die Daten geladen werden sollen
	 */
	public void copyDataFrom(final ParameterCompareSetupValueInput source) {
		setName(source.getName());
		setMode(source.mode);
		setXMLMode(source.xmlMode);
		setTag(source.tag);
	}

	@Override
	public ParameterCompareSetupValueInput clone() {
		final ParameterCompareSetupValueInput clone=new ParameterCompareSetupValueInput();
		clone.copyDataFrom(this);
		return clone;
	}

	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("ParameterCompare.XML.Inputs.Root");
	}

	@Override
	protected String loadPropertyFromXML(final String name, final String content, final Element node) {
		final String error=super.loadPropertyFromXML(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("ParameterCompare.XML.Inputs.Data",name)) {
			String s;
			s=Language.trAllAttribute("ParameterCompare.XML.Inputs.Data.Mode",node);
			if (s!=null && !s.trim().isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null || I.intValue()>2) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("ParameterCompare.XML.Inputs.Data.Mode"),name,node.getParentNode().getNodeName());
				switch (I.intValue()) {
				case 0: mode=ModelChanger.Mode.MODE_RESOURCE; break;
				case 1: mode=ModelChanger.Mode.MODE_VARIABLE; break;
				case 2: mode=ModelChanger.Mode.MODE_XML; break;
				}
			}
			s=Language.trAllAttribute("ParameterCompare.XML.Inputs.Data.XMLMode",node);
			if (s!=null && !s.trim().isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null || I.intValue()>6) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("ParameterCompare.XML.Inputs.Data.XMLMode"),name,node.getParentNode().getNodeName());
				xmlMode=I.intValue();
			}
			tag=content;
			return null;
		}

		return null;
	}

	@Override
	protected void addPropertiesToXML(Document doc, Element node) {
		super.addPropertiesToXML(doc,node);

		final Element sub=doc.createElement(Language.tr("ParameterCompare.XML.Inputs.Data"));
		node.appendChild(sub);
		int modeInt=0;
		switch (mode) {
		case MODE_RESOURCE: modeInt=0; break;
		case MODE_VARIABLE: modeInt=1; break;
		case MODE_XML: modeInt=2; break;
		}
		sub.setAttribute(Language.tr("ParameterCompare.XML.Inputs.Data.Mode"),""+modeInt);
		if (mode==ModelChanger.Mode.MODE_XML) sub.setAttribute(Language.tr("ParameterCompare.XML.Inputs.Data.XMLMode"),""+xmlMode);
		sub.setTextContent(tag);
	}
}
