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

/**
 * Hält die Definition einer Ausgabegröße für die Parameter-Vergleichs-Funktion vor.
 * @author Alexander Herzog
 * @see ParameterCompareSetup
 */
public final class ParameterCompareSetupValueOutput extends ParameterCompareSetupBase implements Cloneable {
	/**
	 * Modus der Ausgabegröße
	 * @author Alexander Herzog
	 * @see ParameterCompareSetupValueOutput#getMode()
	 * @see ParameterCompareSetupValueOutput#setMode(OutputMode)
	 */
	public enum OutputMode {
		/** Inhalt eines XML-Elements oder -Attributs ausgeben */
		MODE_XML,
		/** Rückgabewert eines Javascript-Skripts ausgeben */
		MODE_SCRIPT_JS,
		/** Rückgabewert eines Java-Codefragments ausgeben */
		MODE_SCRIPT_JAVA,
		/** Ergebnis eines Rechenbefehls ausgeben */
		MODE_COMMAND
	}

	private OutputMode mode;
	private String tag;
	private boolean isTime;

	/**
	 * Konstruktor der Klasse
	 */
	public ParameterCompareSetupValueOutput() {
		super();
		mode=OutputMode.MODE_XML;
		tag="";
		isTime=false;
	}

	/**
	 * Liefert den aktuellen Modus der Ausgabegröße
	 * @return	Modus der Eingabegröße
	 */
	public OutputMode getMode() {
		return mode;
	}

	/**
	 * Stellt den Modus der Ausgabegröße ein
	 * @param mode	Modus der Eingabegröße
	 */
	public void setMode(final OutputMode mode) {
		if (mode!=null) this.mode=mode;
	}

	/**
	 * Liefert den XML-Namen für den Ergebniswert
	 * @return	XML-Name des Ergebniswerts
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Stellt den XML-Namen für den Ergebniswert ein
	 * @param tag	XML-Name des Ergebniswerts
	 */
	public void setTag(final String tag) {
		if (tag!=null) this.tag=tag.trim();
	}

	/**
	 * Gibt an, ob der Ergebniswert eine Zeitangabe sein soll
	 * @return	Gibt <code>true</code> zurück, wenn der Ergebniswert eine Zeitangabe sein soll
	 */
	public boolean getIsTime() {
		return isTime;
	}

	/**
	 * Stellt ein, ob der Ergebniswert eine Zeitangabe sein soll
	 * @param isTime	Wird <code>true</code> übergeben, so wird der Ergebniswert als Zeitangabe interpretiert
	 */
	public void setIsTime(final boolean isTime) {
		this.isTime=isTime;
	}

	/**
	 * Vergleich den Ausgabe-Einstellungen-Datensatz mit einem anderen Einstellungen-Objekt
	 * @param otherOutput	Anderes Einstellungen-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsParameterCompareSetupValueOutput(final ParameterCompareSetupValueOutput otherOutput) {
		if (!getName().equals(otherOutput.getName())) return false;
		if (mode!=otherOutput.mode) return false;
		if (!tag.equals(otherOutput.tag)) return false;
		if (isTime!=otherOutput.isTime) return false;
		return true;
	}

	@Override
	public ParameterCompareSetupValueOutput clone() {
		final ParameterCompareSetupValueOutput clone=new ParameterCompareSetupValueOutput();
		clone.setName(getName());
		clone.setMode(mode);
		clone.setTag(tag);
		clone.setIsTime(isTime);
		return clone;
	}

	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("ParameterCompare.XML.Outputs.Root");
	}

	@Override
	protected String loadPropertyFromXML(final String name, final String content, final Element node) {
		final String error=super.loadPropertyFromXML(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("ParameterCompare.XML.Outputs.Data",name)) {
			String s;

			/* Zeit ja/nein */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.IsTime",node);
			isTime=(!s.trim().isEmpty() && !s.trim().equals("0"));

			/* Modus */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.Mode",node);
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.XML",s)) mode=OutputMode.MODE_XML;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.Command",s)) mode=OutputMode.MODE_COMMAND;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.Script",s)) mode=OutputMode.MODE_SCRIPT_JS;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.Java",s)) mode=OutputMode.MODE_SCRIPT_JAVA;

			/* Alte Art der Unterscheidung: XML / Script */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.IsScript",node);
			if (!s.trim().isEmpty() && !s.trim().equals("0")) mode=OutputMode.MODE_SCRIPT_JS;

			/* Inhalt */
			tag=content;
			return null;
		}

		return null;
	}

	@Override
	protected void addPropertiesToXML(Document doc, Element node) {
		super.addPropertiesToXML(doc,node);

		final Element sub=doc.createElement(Language.tr("ParameterCompare.XML.Outputs.Data"));
		node.appendChild(sub);

		/* Zeit ja / nein */
		if (isTime) sub.setAttribute(Language.tr("ParameterCompare.XML.Outputs.Data.IsTime"),"1");

		/* Modus */
		final String modeString;
		switch (mode) {
		case MODE_XML: modeString=Language.tr("ParameterCompare.XML.Outputs.Data.Mode.XML"); break;
		case MODE_COMMAND: modeString=Language.tr("ParameterCompare.XML.Outputs.Data.Mode.Command"); break;
		case MODE_SCRIPT_JS: modeString=Language.tr("ParameterCompare.XML.Outputs.Data.Mode.Script"); break;
		case MODE_SCRIPT_JAVA: modeString=Language.tr("ParameterCompare.XML.Outputs.Data.Mode.Java"); break;
		default: modeString=Language.tr("ParameterCompare.XML.Outputs.Data.Mode.Command"); break;
		}
		sub.setAttribute(Language.tr("ParameterCompare.XML.Outputs.Data.Mode"),modeString);

		/* Inhalt */
		sub.setTextContent(tag);
	}
}
