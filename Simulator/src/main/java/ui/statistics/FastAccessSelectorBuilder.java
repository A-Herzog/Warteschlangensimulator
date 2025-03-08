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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import language.Language;
import simulator.statistics.Statistics;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import systemtools.statistics.StatisticViewerText;
import systemtools.statistics.StatisticsBasePanel;
import ui.images.Images;

/**
 * Diese Klasse h�lt Hilfsroutinen bereit, um Textzeilen in einem
 * {@link StatisticViewerText}-Element mit Kontextmen�s auszustatten,
 * �ber die XML-Elemente ausgew�hlt werden k�nnen, die dann zu einem
 * Schnellzugriffs-Element hinzugef�gt werden.
 * @author Alexander Herzog
 * @see StatisticViewerText
 * @see StatisticViewerFastAccess#addXML(ui.statistics.StatisticViewerFastAccess.AddXMLMode, String)
 */
public class FastAccessSelectorBuilder {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Callback, das aufgerufen wird, wenn in dem XML-Selektor-Kontextmen� ein Eintrag angeklickt wurde */
	private final BiConsumer<StatisticViewerFastAccess.AddXMLMode,String> fastAccessAdd;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param fastAccessAdd	Callback, das aufgerufen wird, wenn in dem XML-Selektor-Kontextmen� ein Eintrag angeklickt wurde
	 */
	public FastAccessSelectorBuilder(final Statistics statistics, final BiConsumer<StatisticViewerFastAccess.AddXMLMode,String> fastAccessAdd) {
		this.statistics=statistics;
		this.fastAccessAdd=fastAccessAdd;
	}

	/**
	 * Art der einzuf�genden Kenngr��e basierend auf einem Indikator
	 * @author Alexander Herzog
	 * @see FastAccessSelectorBuilder#getXMLSelector(StatisticsPerformanceIndicator, IndicatorMode)
	 */
	public enum IndicatorMode {
		/** Anzahl */
		COUNT(()->Language.tr("Statistics.XML.Count")),

		/** Summe */
		SUM(()->Language.tr("Statistics.XML.Sum")),

		/** Wert */
		VALUE(()->Language.tr("Statistics.XML.Value")),

		/** Wert */
		MINIMUM(()->Language.tr("Statistics.XML.Minimum")),

		/** Wert */
		MAXIMUM(()->Language.tr("Statistics.XML.Maximum")),

		/** 10%-Quantil */
		QUANTIL10(()->Language.tr("Statistics.XML.Quantil")+"10"),

		/** 25%-Quantil */
		QUANTIL25(()->Language.tr("Statistics.XML.Quantil")+"25"),

		/** 50%-Quantil */
		QUANTIL50(()->Language.tr("Statistics.XML.Quantil")+"50"),

		/** 75%-Quantil */
		QUANTIL75(()->Language.tr("Statistics.XML.Quantil")+"75"),

		/** 90%-Quantil */
		QUANTIL90(()->Language.tr("Statistics.XML.Quantil")+"90"),

		/** Mittelwert */
		MEAN(()->Language.tr("Statistics.XML.Mean")),

		/** Standardabweichung */
		SD(()->Language.tr("Statistics.XML.StdDev")),

		/** Variationskoeffizient */
		CV(()->Language.tr("Statistics.XML.CV")),

		/** Schiefe */
		Sk(()->Language.tr("Statistics.XML.Sk")),

		/** W�lbung */
		Kurt(()->Language.tr("Statistics.XML.Kurt")),

		/** Quotient */
		QUOTIENT(()->Language.tr("Statistics.XML.Quotient"));

		/**
		 * Liefert den XML-Attribut-Bezeichner f�r diese Eigenschaft.
		 * @return	XML-Attribut-Bezeichner f�r diese Eigenschaft
		 */
		public String getName() {
			return xmlAttributeName.get();
		}

		/**
		 * Liefert abh�ngig vom Quantil-Wahrscheinlichkeitswert ein Quantil-Objekt.
		 * @param p	Quantil-Wahrscheinlichkeitswert f�r den das zugeh�rige Quantil-Objekt geliefert werden soll
		 * @return	Quantil-Objekt oder <code>null</code>, wenn es kein Quantil-Objekt f�r diesen Wahrscheinlichkeitswert gibt
		 */
		public static IndicatorMode quantil(final double p) {
			if (Math.abs(p-0.10)<0.001) return QUANTIL10;
			if (Math.abs(p-0.25)<0.001) return QUANTIL25;
			if (Math.abs(p-0.50)<0.001) return QUANTIL50;
			if (Math.abs(p-0.75)<0.001) return QUANTIL75;
			if (Math.abs(p-0.90)<0.001) return QUANTIL90;
			return null;
		}

		/**
		 * Getter f�r den xml-Attribut-Namen
		 * @see #getName()
		 */
		private final Supplier<String> xmlAttributeName;

		/**
		 * Konstruktor des Enum
		 * @param xmlAttributeName	Getter f�r den xml-Attribut-Namen
		 */
		IndicatorMode(final Supplier<String> xmlAttributeName) {
			this.xmlAttributeName=xmlAttributeName;
		}
	}

	/**
	 * Erg�nzt ein Popupmen� mit Eintr�gen, �ber die die Art der Einf�gung ausgew�hlt werden kann.
	 * @param menu	Popupmen� an das die neuen Eintr�ge angeh�ngt werden sollen
	 * @param xmlSelector	In den Schnellzugriff einzuf�gender XML-Selektor
	 * @return	Gibt an, ob das Popupmen� erg�nzt
	 * @see StatisticViewerText#processContextClick(StatisticsBasePanel, String)
	 */
	public boolean addToPopup(final JPopupMenu menu, final String xmlSelector) {
		if (xmlSelector==null || xmlSelector.isBlank() || fastAccessAdd==null) return false;

		if (menu.getComponentCount()>0) menu.addSeparator();

		JMenuItem item;

		menu.add(item=new JMenuItem("<html><body><b>"+Language.tr("FastAccessBuilder.xmlSelector")+"</b></body></html>"));
		item.setEnabled(false);

		menu.add(item=new JMenuItem(Language.tr("Dialog.Button.Copy")));
		item.setToolTipText(Language.tr("FastAccessBuilder.CopyHint")+": "+xmlSelector);
		item.addActionListener(e->Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(xmlSelector),null));
		item.setIcon(Images.EDIT_COPY.getIcon());

		menu.add(item=new JMenuItem(Language.tr("FastAccessBuilder.Add.List")));
		item.addActionListener(e->fastAccessAdd.accept(StatisticViewerFastAccess.AddXMLMode.LIST,xmlSelector));
		item.setIcon(Images.SCRIPT_MODE_LIST.getIcon());

		menu.add(item=new JMenuItem(Language.tr("FastAccessBuilder.Add.Javascript")));
		item.addActionListener(e->fastAccessAdd.accept(StatisticViewerFastAccess.AddXMLMode.JAVASCRIPT,xmlSelector));
		item.setIcon(Images.SCRIPT_MODE_JAVASCRIPT.getIcon());

		menu.add(item=new JMenuItem(Language.tr("FastAccessBuilder.Add.Java")));
		item.addActionListener(e->fastAccessAdd.accept(StatisticViewerFastAccess.AddXMLMode.JAVA,xmlSelector));
		item.setIcon(Images.SCRIPT_MODE_JAVA.getIcon());

		return true;
	}

	/**
	 * Beim letzten Aufruf von {@link #getXMLSelector(StatisticsPerformanceIndicator, IndicatorMode)} ermittelter
	 * Eltern-Statistikindikator (wird beim n�chsten Aufruf als erstes getestet)
	 * @see #getXMLSelector(StatisticsPerformanceIndicator, IndicatorMode)
	 */
	private StatisticsMultiPerformanceIndicator lastParent;

	/**
	 * Findet den zu dem Indikator und der Kenngr��e passenden XML-Selektor
	 * @param indicator	Indikator aus dem die Daten stammen
	 * @param indicatorMode	Art der Kenngr��e
	 * @return	XML-Selektor f�r Schnellzugriff oder <code>null</code>, wenn kein Selektor ermittelt werden konnte
	 * @see IndicatorMode
	 */
	public String getXMLSelector(final StatisticsPerformanceIndicator indicator, final IndicatorMode indicatorMode) {
		final StringBuilder result=new StringBuilder();

		final StatisticsMultiPerformanceIndicator multi;
		if (lastParent!=null && lastParent.contains(indicator)) {
			multi=lastParent;
		} else {
			multi=statistics.getParent(indicator);
			if (multi!=null) lastParent=multi;
		}

		if (multi!=null && multi.xmlNodeNames!=null && multi.xmlNodeNames.length>0) {
			result.append(multi.xmlNodeNames[0]);
			result.append("->");
		}

		if (indicator.xmlNodeNames!=null && indicator.xmlNodeNames.length>0) {
			result.append(indicator.xmlNodeNames[0]);
			if (multi!=null && StatisticsMultiPerformanceIndicator.xmlTypeName!=null && StatisticsMultiPerformanceIndicator.xmlTypeName.length>0) {
				final String name=multi.getName(indicator);
				if (name!=null) {
					result.append("[");
					result.append(StatisticsMultiPerformanceIndicator.xmlTypeName[0]);
					result.append("=\"");
					result.append(name);
					result.append("\"]");
				}
			}
		}

		if (indicatorMode!=null) {
			result.append(String.format("->[%s]",indicatorMode.getName()));
		}

		return result.toString();
	}
}
