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

/**
 * Hält die Definition einer Ausgabegröße für die Parameter-Vergleichs-Funktion vor.
 * @author Alexander Herzog
 * @see ParameterCompareSetup
 */
public final class ParameterCompareSetupValueOutput extends ParameterCompareSetupBase implements Cloneable {
	/**
	 * Modus der Ausgabegröße
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

	/**
	 * Modus der Ausgabegröße
	 */
	private OutputMode mode;

	/**
	 * XML-Namen für den Ergebniswert
	 */
	private String tag;

	/**
	 * Wie soll der Ausgabewert dargestellt werden?
	 * @see ParameterCompareSetupValueOutput#getFormat()
	 * @see ParameterCompareSetupValueOutput#setFormat(OutputFormat)
	 */
	public enum OutputFormat {
		/** Ausgabe als Fließkommazahl */
		FORMAT_NUMBER,
		/** Ausgabe als Prozentwert */
		FORMAT_PERCENT,
		/** Ausgabe als Zeitangabe */
		FORMAT_TIME
	}

	/**
	 * Wie soll der Ausgabewert dargestellt werden?
	 */
	private OutputFormat format;

	/**
	 * Anzahl an anzuzeigenden Nachkommastellen (-1 steht für globale Vorgabe verwenden)
	 */
	private int digits;

	/**
	 * Konstruktor der Klasse
	 */
	public ParameterCompareSetupValueOutput() {
		super();
		mode=OutputMode.MODE_XML;
		tag="";
		format=OutputFormat.FORMAT_NUMBER;
		digits=-1;
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
	 * Gibt an, wie der Ergebniswert angezeigt werden soll.
	 * @return	Darstellungsformat für den Ausgabewert
	 */
	public OutputFormat getFormat() {
		return format;
	}

	/**
	 * Stellt ein, ob wie der Ergebniswert angezeigt werden soll.
	 * @param format	Darstellungsformat für den Ausgabewert
	 */
	public void setFormat(final OutputFormat format) {
		if (format!=null) this.format=format;
	}

	/**
	 * Lieferte die eingestellte Anzahl an anzuzeigenden Nachkommastellen.
	 * @return	Anzahl an anzuzeigenden Nachkommastellen (-1 steht für globale Vorgabe verwenden)
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * Stellt die Anzahl an anzuzeigenden Nachkommastellen ein.
	 * @param digits	Anzahl an anzuzeigenden Nachkommastellen (-1 steht für globale Vorgabe verwenden)
	 */
	public void setDigits(int digits) {
		this.digits=digits;
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
		if (format!=otherOutput.format) return false;
		if (digits!=otherOutput.digits) return false;
		return true;
	}

	@Override
	public ParameterCompareSetupValueOutput clone() {
		final ParameterCompareSetupValueOutput clone=new ParameterCompareSetupValueOutput();
		clone.setName(getName());
		clone.setMode(mode);
		clone.setTag(tag);
		clone.setFormat(format);
		clone.setDigits(digits);
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

			/* Zeit ja/nein (altes XML-Format) */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.IsTime",node);
			if (!s.trim().isEmpty() && !s.trim().equals("0")) format=OutputFormat.FORMAT_TIME;

			/* Format */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.Format",node);
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Format.Number",s)) format=OutputFormat.FORMAT_NUMBER;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Format.Percent",s)) format=OutputFormat.FORMAT_PERCENT;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Format.Time",s)) format=OutputFormat.FORMAT_TIME;

			/* Modus */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.Mode",node);
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.XML",s)) mode=OutputMode.MODE_XML;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.Command",s)) mode=OutputMode.MODE_COMMAND;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.Script",s)) mode=OutputMode.MODE_SCRIPT_JS;
			if (Language.trAll("ParameterCompare.XML.Outputs.Data.Mode.Java",s)) mode=OutputMode.MODE_SCRIPT_JAVA;

			/* Nachkommastellen */
			s=Language.trAllAttribute("ParameterCompare.XML.Outputs.Data.Digits",node);
			if (!s.trim().isEmpty()) {
				final Integer I=NumberTools.getInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("ParameterCompare.XML.Outputs.Data.Digits"),name,node.getParentNode().getNodeName());
				if (I.intValue()>0) digits=I.intValue();
			}

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

		/* Format */
		if (format!=OutputFormat.FORMAT_NUMBER) switch (format) {
		case FORMAT_NUMBER:
			sub.setAttribute(Language.tr("ParameterCompare.XML.Outputs.Data.Format"),Language.tr("ParameterCompare.XML.Outputs.Data.Format.Number"));
			break;
		case FORMAT_PERCENT:
			sub.setAttribute(Language.tr("ParameterCompare.XML.Outputs.Data.Format"),Language.tr("ParameterCompare.XML.Outputs.Data.Format.Percent"));
			break;
		case FORMAT_TIME:
			sub.setAttribute(Language.tr("ParameterCompare.XML.Outputs.Data.Format"),Language.tr("ParameterCompare.XML.Outputs.Data.Format.Time"));
			break;
		}

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

		/* Nachkommastellen */
		if (digits>=0) {
			sub.setAttribute(Language.tr("ParameterCompare.XML.Outputs.Data.Digits"),""+digits);
		}

		/* Inhalt */
		sub.setTextContent(tag);
	}
}
