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

import java.net.URL;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import parser.MathCalcError;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import ui.ModelChanger;
import ui.images.Images;

/**
 * Diese Klasse repräsentiert einen einzelnen Listen-Filter-Eintrag
 * @author Alexander Herzog
 * @see FilterList
 */
public final class FilterListRecord {
	/**
	 * Mögliche Arten, was ein Eintrag repräsentieren kann
	 * @author Alexander Herzog
	 * @see FilterListRecord#mode
	 */
	public enum Mode {
		/**
		 * Zeilenumbruch
		 */
		NewLine("NewLine",false,Images.SCRIPT_RECORD_TEXT.getURL()),

		/**
		 * Tabulator
		 */
		Tabulator("Tabulator",false,Images.SCRIPT_RECORD_TEXT.getURL()),

		/**
		 * Leerzeichen
		 */
		Space("Space",false,Images.SCRIPT_RECORD_TEXT.getURL()),

		/**
		 * Freitext
		 * @see FilterListRecord#text
		 */
		Text("Text",true,Images.SCRIPT_RECORD_TEXT.getURL()),

		/**
		 * Rechenausdruck
		 * @see FilterListRecord#text
		 */
		Expression("Expression",true,Images.SCRIPT_RECORD_EXPRESSION.getURL()),

		/**
		 * XML-Element
		 * @see FilterListRecord#text
		 */
		XML("XML",true,Images.SCRIPT_RECORD_XML.getURL()),

		/**
		 * Umschaltung des Zahlenformats auf System-Notation
		 * @see FilterListRecord.Mode#FormatLocal
		 */
		FormatSystem("FormatSystem",false,Images.SCRIPT_RECORD_FORMAT.getURL()),

		/**
		 * Umschaltung des Zahlenformats auf lokale Notation
		 * @see FilterListRecord.Mode#FormatSystem
		 */
		FormatLocal("FormatLocal",false,Images.SCRIPT_RECORD_FORMAT.getURL()),

		/**
		 * Umschaltung auf Ausgabe von Dezimalwerten (statt Prozentwerten)
		 * @see FilterListRecord.Mode#FormatPercent
		 */
		FormatFraction("FormatFraction",false,Images.SCRIPT_RECORD_FORMAT.getURL()),

		/**
		 * Umschaltung auf Ausgabe von Prozentwerten (statt Dezimalwerten)
		 * @see FilterListRecord.Mode#FormatFraction
		 */
		FormatPercent("FormatPercent",false,Images.SCRIPT_RECORD_FORMAT.getURL()),

		/**
		 * Umschalten auf Ausgabe von Zahlen (statt Zeitangaben)
		 * @see FilterListRecord.Mode#FormatTime
		 */
		FormatNumber("FormatNumber",false,Images.SCRIPT_RECORD_FORMAT.getURL()),

		/**
		 * Umschalten auf Ausgabe von Zahlen (statt Zeitangaben)
		 * @see FilterListRecord.Mode#FormatTime
		 */
		FormatTime("FormatTime",false,Images.SCRIPT_RECORD_FORMAT.getURL());

		/**
		 * Bezeichner für dieses Element in der Datei
		 */
		public final String fileTag;

		/**
		 * Verwendet dieses Element das Text-Feld?
		 * @see FilterListRecord#text
		 */
		public final boolean hasText;

		/**
		 * Icon für den aktuellen Modus
		 */
		public final URL imgURL;

		/**
		 * Konstruktor der Enumeration
		 * @param fileTag	Bezeichner für dieses Element in der Datei
		 * @param hasText	Verwendet dieses Element das Text-Feld?
		 * @param imgURL	String zu der Icon-Ressource
		 */
		private Mode(final String fileTag, final boolean hasText, final URL imgURL) {
			this.fileTag=fileTag;
			this.hasText=hasText;
			this.imgURL=imgURL;
		}
	}

	/**
	 * Konstruktor der Klasse
	 */
	public FilterListRecord() {
		mode=Mode.NewLine;
		text="";
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param record	Anderes Listenelement aus dem die Daten kopiert werden sollen
	 */
	public FilterListRecord(final FilterListRecord record) {
		mode=record.mode;
		text=record.text;
	}

	@Override
	public int hashCode() { /* Andernfalls meint FindBugs, dass ich equals nicht überschreiben darf. */
		return super.hashCode();
	}

	@Override
	public boolean equals(final Object record) {
		if (!(record instanceof FilterListRecord)) return false;
		if (((FilterListRecord)record).mode!=mode) return false;
		if (!Objects.equals(((FilterListRecord)record).text,text)) return false;
		return true;
	}

	/**
	 * Modus des Listenelements
	 * @see FilterListRecord.Mode
	 */
	public Mode mode;

	/**
	 * Optional notwendiger Text des Listenelements
	 * @see FilterListRecord.Mode#Text
	 * @see FilterListRecord.Mode#XML
	 */
	public String text;

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Trägt die Modus und Text in ein {@link JLabel}-Element ein.
	 * @param mode	Zu verwendender Modus (für Icon und Beschreibung)
	 * @param text	Für manche Modus-Varianten verwendeter Text (wird hier <code>null</code> übergeben so erfolgt die Ausgabe auch dann nur einzeilig, wenn eigentlich ein Text vorhanden sein müsste)
	 * @param label	{@link JLabel}-Element in das die Daten eingetragen werden sollen.
	 */
	public static void writeToJLabel(final Mode mode, final String text, final JLabel label) {
		String result="";

		switch (mode) {
		case FormatFraction:
			result=Language.tr("Statistic.FastAccess.FilterList.Fraction");
			break;
		case FormatLocal:
			result=Language.tr("Statistic.FastAccess.FilterList.NumberFormatLocal");
			break;
		case FormatNumber:
			result=Language.tr("Statistic.FastAccess.FilterList.Numbers");
			break;
		case FormatPercent:
			result=Language.tr("Statistic.FastAccess.FilterList.Percent");
			break;
		case FormatSystem:
			result=Language.tr("Statistic.FastAccess.FilterList.NumberFormatSystem");
			break;
		case FormatTime:
			result=Language.tr("Statistic.FastAccess.FilterList.Time");
			break;
		case NewLine:
			result=Language.tr("Statistic.FastAccess.FilterList.NewLine");
			break;
		case Space:
			result=Language.tr("Statistic.FastAccess.FilterList.Space");
			break;
		case Tabulator:
			result=Language.tr("Statistic.FastAccess.FilterList.Tabulator");
			break;
		case Text:
			result=Language.tr("Statistic.FastAccess.FilterList.Text")+((text==null)?"":("<br><b>"+encodeHTMLentities(text)+"</b>"));
			break;
		case Expression:
			result=Language.tr("Statistic.FastAccess.FilterList.Expression")+((text==null)?"":("<br><b>"+encodeHTMLentities(text)+"</b>"));
			break;
		case XML:
			result=Language.tr("Statistic.FastAccess.FilterList.XML")+((text==null)?"":("<br><b>"+encodeHTMLentities(text)+"</b>"));
			break;
		}

		label.setText("<html><body>"+result+"</body></html>");
		if (mode.imgURL!=null) label.setIcon(new ImageIcon(mode.imgURL));
	}

	/**
	 * Trägt die Daten des Eintrag in ein {@link JLabel}-Element ein.
	 * @param label	{@link JLabel}-Element in das die Daten dieses Listenelements eingetragen werden sollen.
	 */
	public void writeToJLabel(final JLabel label) {
		writeToJLabel(mode,text,label);
	}

	private String formatNumber(final double value, final FilterListFormat format) {
		if (format.isTime()) {
			return (format.isSystem())?TimeTools.formatExactSystemTime(value):TimeTools.formatExactTime(value);
		} else {
			if (format.isPercent()) {
				return NumberTools.formatPercent(value);
			} else {
				return (format.isSystem())?NumberTools.formatSystemNumber(value):NumberTools.formatNumberMax(value);
			}
		}
	}

	private String processXML(final Statistics statistics, final FilterListFormat format, final String xml) {
		final String text=ModelChanger.getStatisticValue(statistics,xml);
		final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(text));
		if (D==null) return text;
		return formatNumber(D.doubleValue(),format);
	}

	private String processExpression(final Statistics statistics, final FilterListFormat format, final String xml) {
		final ExpressionCalc expression=new ExpressionCalc(new String[0]);
		final int errorPos=expression.parse(text);
		if (errorPos>=0) return text+"\n"+String.format(Language.tr("Statistics.Filter.CoundNotProcessExpression.Info"),errorPos+1);
		try {
			return formatNumber(expression.calc(statistics),format);
		} catch (MathCalcError e) {
			return text;
		}
	}

	/**
	 * Führt die Verarbeitungen zu diesem Listenelement durch
	 * @param statistics	Statistik-Objekt
	 * @param format	Formate-Objekt, welches die momentan getätigten Formateinstellungen bereithält
	 * @return	Neuer auszugebender Text (kann leer aber nie <code>null</code> sein)
	 */
	public String process(final Statistics statistics, final FilterListFormat format) {
		switch (mode) {
		case FormatFraction:
			format.setFraction();
			return "";
		case FormatLocal:
			format.setLocal();
			return "";
		case FormatNumber:
			format.setNumber();
			return "";
		case FormatPercent:
			format.setPercent();
			return "";
		case FormatSystem:
			format.setSystem();
			return "";
		case FormatTime:
			format.setTime();
			return "";
		case NewLine:
			return "\n";
		case Space:
			return " ";
		case Tabulator:
			return"\t";
		case Text:
			if (text!=null) return text; else return "";
		case Expression:
			if (text!=null) return processExpression(statistics,format,text); else return "";
		case XML:
			if (text!=null)	return processXML(statistics,format,text); else return "";
		default:
			return "";
		}
	}
}
