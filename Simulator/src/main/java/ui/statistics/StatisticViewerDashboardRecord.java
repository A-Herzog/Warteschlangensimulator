/**
 * Copyright 2022 Alexander Herzog
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import scripting.js.JSCommandXML;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.images.Images;
import ui.modeleditor.fastpaint.GradientFill;
import ui.statistics.ListPopup.ScriptHelperRecord;
import ui.statistics.ListPopup.ScriptHelperSub;

/**
 * Datensatz für eine einzelne Kachel in der Dashboard-Ansicht
 * @see StatisticViewerDashboard
 * @author Alexander Herzog
 */
public class StatisticViewerDashboardRecord {
	/**
	 * Hintergrundfarbe (kann <code>null</code> sein)
	 */
	private Color backgroundColor;

	/**
	 * Farbverlaufsfarbe (kann <code>null</code> sein)
	 */
	private Color gradientColor;

	/**
	 * Ausgabe-Zahlenformat
	 */
	public enum Format {
		/** Wert als Fließkommazahl ausgeben */
		NUMBER("number"),
		/** Wert als Prozentwert ausgeben */
		PERCENT("percent"),
		/** Wert als Zeitangabe ausgeben */
		TIME("time");

		/**
		 * ID für das Format
		 */
		public final String id;

		/**
		 * Konstruktor des Enum
		 * @param id	ID für das Format
		 */
		Format(final String id) {
			this.id=id;
		}

		/**
		 * Liefert einen Eintrag auf Basis einer ID.
		 * @param id	ID für das Format
		 * @return	Liefert das zu der ID passende Format (und im ggf. einen Fallback)
		 */
		static Format fromID(final String id) {
			for (Format format: values()) if (format.id.equals(id)) return format;
			return Format.NUMBER;
		}
	}

	/**
	 * Ausgabe-Zahlenformat
	 * @see Format
	 */
	private Format format;

	/**
	 * Anzahl der auszugebenden Nachkommastellen
	 */
	private int digits;

	/**
	 * Optionaler Text vor dem Wert (kann <code>null</code> sein)
	 */
	private String preText;

	/**
	 * Optionaler Text nach dem Wert (kann <code>null</code> sein)
	 */
	private String postText;

	/**
	 * Soll versucht werden, die Überschrift automatisch zu generieren
	 */
	private boolean autoHeading;

	/**
	 * Überschrift (kann <code>null</code> sein)
	 */
	private String heading;

	/**
	 * Auszulesendes XML-Element
	 */
	private String xmlData;

	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerDashboardRecord() {
		backgroundColor=null;
		gradientColor=null;
		format=Format.NUMBER;
		digits=2;
		autoHeading=true;
		heading=null;
		xmlData="";
	}

	/**
	 * Liefert eine Liste mit Datensätzen zur Zuordnung von XML-Elementen zu Titeln.
	 * @param statistics		Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @return	Liste mit Datensätzen zur Zuordnung von XML-Elementen zu Titeln
	 * @see #getViewer(Statistics, List, Consumer, Consumer, Consumer, BiConsumer)
	 */
	public static List<ScriptHelperRecord> getTitleRecords(final Statistics statistics) {
		final List<ScriptHelperRecord> results=new ArrayList<>();

		final ListPopup helper=new ListPopup(null,null);
		final List<Object> list=helper.getStatisticsTemplatesList(statistics,record->true);
		addRecords(results,list);
		return results;
	}

	/**
	 * Überträgt eine Teilliste in die Datensatzausgabeliste
	 * @param results	Datensatzausgabeliste
	 * @param list	Zu verarbeitende Teilliste
	 * @see #getTitleRecords(Statistics)
	 */
	private static void addRecords(final List<ScriptHelperRecord> results, final List<Object> list) {
		for (Object obj: list) {
			if (obj instanceof ScriptHelperRecord) results.add((ScriptHelperRecord)obj);
			if (obj instanceof ScriptHelperSub) addRecords(results,((ScriptHelperSub)obj).list);
		}
	}

	/**
	 * Liefert das Panel zur konkreten Darstellung der Kachel
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param titleRecords	Liste mit Datensätzen zur Zuordnung von XML-Elementen zu Titeln
	 * @param moveUp	Optionales Callback um die Kachel nach vorne zu verschieben
	 * @param moveDown	Optionales Callback um die Kachel nach hinten zu verschieben
	 * @param edit	Optionales Callback um die Kachel zu bearbeiten
	 * @param remove	Optionales Callback um die Kachel zu löschen
	 * @return	Kachel zum aktuellen Datensatz
	 */
	public JPanel getViewer(final Statistics statistics, final List<ScriptHelperRecord> titleRecords, final Consumer<StatisticViewerDashboardRecord> moveUp, final Consumer<StatisticViewerDashboardRecord> moveDown, final Consumer<StatisticViewerDashboardRecord> edit, final BiConsumer<StatisticViewerDashboardRecord,Boolean> remove) {
		final String heading=getHeading(titleRecords,xmlData);
		final String text=getXMLData(statistics,xmlData);

		return new Viewer(this,backgroundColor,gradientColor,heading,text,moveUp,moveDown,edit,remove);
	}

	/**
	 * Ermittelt die Überschrift für die Kachel.
	 * @param titleRecords	Liste mit Datensätzen zur Zuordnung von XML-Elementen zu Titeln
	 * @param xml	Auszulesendes XML-Element
	 * @return	Überschrift für die Kachel
	 */
	private String getHeading(final List<ScriptHelperRecord> titleRecords, final String xml) {
		if (autoHeading && xml!=null) {
			for (ScriptHelperRecord record: titleRecords) {
				if (record.xml.equals(xml)) return record.title;
			}
		}

		return heading;
	}

	/**
	 * Ermittelt den Textinhalt für die Kachel.
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param xml	Auszulesendes XML-Element
	 * @return	Textinhalt für die Kachel
	 */
	private String getXMLData(final Statistics statistics, final String xml) {
		if (xml==null || xml.trim().isEmpty()) return null;

		final Document doc=statistics.saveToXMLDocument();
		try (Scanner selectors=new Scanner(xml)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return Language.tr("Statistics.Filter.InvalidParameters")+" ("+xml+")";
			if (doc==null) return Language.tr("Statistics.Filter.InvalidSelector")+" ("+xml+")";
			final String[] result=JSCommandXML.findElement(selectors,doc.getDocumentElement(),new ArrayList<>(),false,format==Format.PERCENT,format==Format.TIME,digits,';');
			if (result[0]!=null) return result[0];
			if (result[1]!=null) {
				final StringBuilder resultText=new StringBuilder();
				if (preText!=null) resultText.append(preText);
				resultText.append(result[1]);
				if (postText!=null) resultText.append(postText);
				return resultText.toString();
			}
			return Language.tr("Statistics.Filter.InvalidParameters");
		}
	}

	/**
	 * Liefert die aktuelle Hintergrundfarbe.
	 * @return	Hintergrundfarbe (kann <code>null</code> sein)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Stellt eine neue Hintergrundfarbe ein.
	 * @param backgroundColor	Hintergrundfarbe (kann <code>null</code> sein)
	 */
	public void setBackgroundColor(final Color backgroundColor) {
		this.backgroundColor=backgroundColor;
	}

	/**
	 * Liefert die aktuelle Farbverlaufsfarbe.
	 * @return	Farbverlaufsfarbe (kann <code>null</code> sein)
	 */
	public Color getGradientColor() {
		return gradientColor;
	}

	/**
	 * Stellt eine neue Farbverlaufsfarbe ein.
	 * @param gradientColor	Farbverlaufsfarbe (kann <code>null</code> sein)
	 */
	public void setGradientColor(final Color gradientColor) {
		this.gradientColor=gradientColor;
	}

	/**
	 * Liefert das aktuell eingestellte Zahlenformat.
	 * @return	Zahlenformat für die Anzeige
	 * @see Format
	 * @see #setFormat(Format)
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Stellt das zu verwendende Zahlenformat ein.
	 * @param format	Zahlenformat für die Anzeige
	 * @see Format
	 * @see #getFormat()
	 */
	public void setFormat(final Format format) {
		if (format!=null) this.format=format;
	}

	/**
	 * Liefert die aktuell eingestellte Anzahl an anzuzeigenden Nachkommastellen.
	 * @return	Anzuzeigende Anzahl an Nachkommastellen
	 * @see #setDigits(int)
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * Stellt die Anzahl an anzuzeigenden Nachkommastellen ein.
	 * @param digits	Anzuzeigende Anzahl an Nachkommastellen
	 * @see #getDigits()
	 */
	public void setDigits(final int digits) {
		if (digits>=0) this.digits=digits;
	}

	/**
	 * Liefert den optionalen Text, der vor dem Wert angezeigt wird.
	 * @return	Text, der vor dem eigentlichen Wert angezeigt wird
	 * @see #setPreText(String)
	 */
	public String getPreText() {
		return (preText==null)?"":preText;
	}

	/**
	 * Stellt den optionalen Text, der vor dem Wert angezeigt werden soll, ein.
	 * @param preText	Text, der vor dem eigentlichen Wert angezeigt wird (kann <code>null</code> sein)
	 * @see #getPreText()
	 */
	public void setPreText(final String preText) {
		this.preText=preText;
	}

	/**
	 * Liefert den optionalen Text, der hinter dem Wert angezeigt wird.
	 * @return	Text, der hinter dem eigentlichen Wert angezeigt wird
	 * @see #setPostText(String)
	 */
	public String getPostText() {
		return (postText==null)?"":postText;
	}

	/**
	 * Stellt den optionalen Text, der hinter dem Wert angezeigt werden soll, ein.
	 * @param postText	Text, der hinter dem eigentlichen Wert angezeigt wird (kann <code>null</code> sein)
	 * @see #getPostText()
	 */
	public void setPostText(final String postText) {
		this.postText=postText;
	}

	/**
	 * Soll versucht werden, die Überschrift aus dem xml-Element automatisch zu bestimmen?
	 * @return	Liefert <code>true</code>, wenn versucht werden soll, die Überschrift automatisch zu bestimmen
	 * @see #setAutoHeading(boolean)
	 * @see #getHeading()
	 * @see #setHeading(String)
	 */
	public boolean isAutoHeading() {
		return autoHeading;
	}

	/**
	 * Stellt ein, ob versucht werden soll, die Überschrift aus dem xml-Element automatisch zu bestimmen.
	 * @param autoHeading	Überschrift aus dem xml-Element automatisch zu bestimmen?
	 * @see #isAutoHeading()
	 * @see #getHeading()
	 * @see #setHeading(String)
	 */
	public void setAutoHeading(final boolean autoHeading) {
		this.autoHeading=autoHeading;
	}

	/**
	 * Liefert die aktuell eingestellte Überschrift.
	 * @return	Überschrift
	 * @see #setHeading(String)
	 */
	public String getHeading() {
		return (heading==null)?"":heading;
	}

	/**
	 * Stellt ein neue Überschrift ein.
	 * @param heading	Überschrift (kann <code>null</code> sein)
	 * @see #getHeading()
	 */
	public void setHeading(final String heading) {
		this.heading=heading;
	}

	/**
	 * Liefert das XML-Element aus dem die Daten ausgelesen werden soll.
	 * @return	XML-Element aus dem die Daten ausgelesen werden
	 * @see #setXMLData(String)
	 */
	public String getXMLData() {
		return (xmlData==null)?"":xmlData;
	}

	/**
	 * Stellt das XML-Element aus dem die Daten ausgelesen werden sollen ein.
	 * @param xmlData	XML-Element aus dem die Daten ausgelesen werden
	 * @see #getXMLData()
	 */
	public void setXMLData(final String xmlData) {
		if (xmlData!=null) this.xmlData=xmlData;
	}

	/**
	 * Anzahl der Textzeilen, die ein einzelner Datensatz
	 * beim Import bzw. Export benötigt.
	 *  @see #storeToString(List)
	 *  @see #loadFromString(List, int)
	 */
	public static final int LINES_PER_RECORD=8;

	/**
	 * Speichert den aktuellen Datensatz als Zeichenkette
	 * @param output	Liste zu der die Zeilen für den aktuellen Datensatz hinzugefügt werden sollen
	 * @see #loadFromString(List, int)
	 */
	public void storeToString(List<String> output) {
		if (backgroundColor!=null) {
			if (gradientColor!=null) {
				output.add(EditModel.saveColor(backgroundColor)+" "+EditModel.saveColor(gradientColor));
			} else {
				output.add(EditModel.saveColor(backgroundColor));
			}
		} else {
			output.add("");
		}
		output.add(format.id);
		output.add(""+digits);
		if (preText!=null && !preText.trim().isEmpty()) output.add(preText); else output.add("");
		if (postText!=null && !postText.trim().isEmpty()) output.add(postText); else output.add("");
		if (autoHeading) output.add("1"); else output.add("0");
		if (heading!=null && !heading.trim().isEmpty()) output.add(heading); else output.add("");
		if (xmlData!=null && !xmlData.trim().isEmpty()) output.add(xmlData); else output.add("");
	}

	/**
	 * Versucht eine Datensatz aus einer Zeichenkette zu laden
	 * @param lines	Liste mit Zeichenketten aus der der Datensatz geladen werden soll
	 * @param startLine	Erste zu berücksichtigende Zeile in der Liste
	 * @return	Liefert im Erfolgsfall den Datensatz, sonst <code>null</code>
	 * @see #storeToString(List)
	 */
	public static StatisticViewerDashboardRecord loadFromString(final List<String> lines, int startLine) {
		if (lines==null) return null;
		if (startLine>=lines.size()) return null;

		final StatisticViewerDashboardRecord record=new StatisticViewerDashboardRecord();

		String line=lines.get(startLine++).trim();
		if (line.contains(" ")) {
			final String[] parts=line.split(" ");
			if (parts.length==2) {
				record.setBackgroundColor(EditModel.loadColor(parts[0]));
				record.setGradientColor(EditModel.loadColor(parts[1]));
			}
		} else {
			record.setBackgroundColor(EditModel.loadColor(line));
		}

		if (lines.size()>startLine) record.setFormat(Format.fromID(lines.get(startLine++)));

		if (lines.size()>startLine) {
			final Integer I=NumberTools.getNotNegativeInteger(lines.get(startLine++));
			record.setDigits((I==null)?1:I);
		}

		if (lines.size()>startLine) record.setPreText(lines.get(startLine++));

		if (lines.size()>startLine) record.setPostText(lines.get(startLine++));

		if (lines.size()>startLine) {
			final String s=lines.get(startLine++);
			record.setAutoHeading(s.equals("1"));
		}

		if (lines.size()>startLine) record.setHeading(lines.get(startLine++));

		if (lines.size()>startLine) record.setXMLData(lines.get(startLine++));

		return record;
	}

	/**
	 * Darstellungselement für eine Kachel
	 */
	private static class Viewer extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-2955036740150140455L;

		/**
		 * Hintergrundfarbe (kann <code>null</code> sein)
		 */
		private final Color backgroundColor;

		/**
		 * Farbverlaufsfarbe (kann <code>null</code> sein)
		 */
		private final Color gradientColor;

		/**
		 * Objekt zum Füllen des Hintergrund bei aktivem Farbverlauf
		 * @see #paintComponent(Graphics)
		 */
		private final Rectangle drawRectangle;

		/**
		 * Objekt für eine Farbverlaufsfüllung
		 * @see #paintComponent(Graphics)
		 */
		private GradientFill gradientFill=null;

		/**
		 * Konstruktor der Klasse
		 * @param record Datensatz auf den sich dieser Viewer bezieht
		 * @param backgroundColor	Hintergrundfarbe (kann <code>null</code> sein)
		 * @param gradientColor	Farbverlaufsfarbe (kann <code>null</code> sein)
		 * @param heading	Überschrift (kann <code>null</code> sein)
		 * @param text	Darzustellender Text (kann <code>null</code> sein)
		 * @param moveUp	Optionales Callback um die Kachel nach vorne zu verschieben
		 * @param moveDown	Optionales Callback um die Kachel nach hinten zu verschieben
		 * @param edit	Optionales Callback um die Kachel zu bearbeiten
		 * @param remove	Optionales Callback um die Kachel zu löschen
		 */
		public Viewer(final StatisticViewerDashboardRecord record, final Color backgroundColor, final Color gradientColor, final String heading, final String text, final Consumer<StatisticViewerDashboardRecord> moveUp, final Consumer<StatisticViewerDashboardRecord> moveDown, final Consumer<StatisticViewerDashboardRecord> edit, final BiConsumer<StatisticViewerDashboardRecord,Boolean> remove) {
			this.backgroundColor=backgroundColor;
			this.gradientColor=gradientColor;

			setPreferredSize(new Dimension(275,125));
			setBorder(BorderFactory.createLineBorder(Color.GRAY));
			setLayout(new BorderLayout());

			drawRectangle=new Rectangle(0,0,10,10);

			/* Überschrift oben */

			if (heading!=null && !heading.trim().isEmpty()) {
				final JPanel headingLine=new JPanel(new FlowLayout(FlowLayout.CENTER));
				add(headingLine,BorderLayout.NORTH);
				headingLine.setOpaque(false);
				headingLine.add(new JLabel("<html><body><b>"+heading+"</b></body></html>"));
			}

			/* Daten in der Mitte */

			if (text!=null) {
				final JPanel mainArea=new JPanel();
				mainArea.setOpaque(false);
				mainArea.setLayout(new BoxLayout(mainArea,BoxLayout.PAGE_AXIS));
				add(mainArea,BorderLayout.CENTER);

				mainArea.add(Box.createVerticalGlue());

				final JPanel dataLine=new JPanel(new FlowLayout(FlowLayout.CENTER));
				mainArea.add(dataLine);
				dataLine.setOpaque(false);
				dataLine.add(new JLabel("<html><body><p style=\"font-size: 180%;\">"+text+"</p></body></html>"));

				mainArea.add(Box.createVerticalGlue());

				setToolTipText(text);
			}

			/* Symbolleiste unten */

			final JToolBar setupLine=new JToolBar(SwingConstants.HORIZONTAL);
			setupLine.setFloatable(false);
			setupLine.setOpaque(false);
			add(setupLine,BorderLayout.SOUTH);

			setupLine.add(Box.createHorizontalGlue());

			JButton button;

			if (moveUp!=null) {
				setupLine.add(button=new JButton(Images.ARROW_LEFT.getIcon()));
				button.setOpaque(false);
				button.setToolTipText(Language.tr("Statistics.Dashboard.Buttons.MoveUp"));
				button.addActionListener(e->moveUp.accept(record));
			}

			if (moveDown!=null) {
				setupLine.add(button=new JButton(Images.ARROW_RIGHT.getIcon()));
				button.setOpaque(false);
				button.setToolTipText(Language.tr("Statistics.Dashboard.Buttons.MoveDown"));
				button.addActionListener(e->moveDown.accept(record));
			}

			if (edit!=null) {
				setupLine.add(button=new JButton(Images.GENERAL_SETUP.getIcon()));
				button.setOpaque(false);
				button.setToolTipText(Language.tr("Statistics.Dashboard.Buttons.EditTile"));
				button.addActionListener(e->edit.accept(record));
			}

			if (remove!=null) {
				setupLine.add(button=new JButton(Images.EDIT_DELETE.getIcon()));
				button.setOpaque(false);
				button.setToolTipText(Language.tr("Statistics.Dashboard.Buttons.DeleteTile"));
				button.addActionListener(e->remove.accept(record,(e.getModifiers() & ActionEvent.SHIFT_MASK)!=0));
			}
		}

		@Override
		public void paintComponent(final Graphics g) {
			if (backgroundColor!=null) {
				if (gradientColor!=null) {
					if (gradientFill==null) gradientFill=new GradientFill(false);
					drawRectangle.width=getWidth();
					drawRectangle.height=getHeight();
					gradientFill.set(g,drawRectangle,gradientColor,backgroundColor,true);
					((Graphics2D)g).fill(drawRectangle);
				} else {
					g.setColor(backgroundColor);
					g.fillRect(0,0,getWidth(),getHeight());
				}
			} else {
				super.paintComponent(g);
			}
		}
	}
}
