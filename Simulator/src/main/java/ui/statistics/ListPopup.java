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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.w3c.dom.Document;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import ui.images.Images;
import ui.modeleditor.ModelResource;

/**
 * Diese Klasse zeigt ein Kontextmenü an, in dem JS-Befehle zur Ausgabe
 * bestimmter Werte aus der Statistik ausgewählt werden können.
 * @author Alexander Herzog
 */
public class ListPopup {
	/** Element unter dem das Popup-Menü eingeblendet werden soll */
	private final Component owner;
	/** Hilfe-Runnable */
	private final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Element unter dem das Popup-Menü eingeblendet werden soll
	 * @param help	Hilfe-Runnable
	 */
	public ListPopup(final Component owner, final Runnable help) {
		this.owner=owner;
		this.help=help;
	}

	/**
	 * Erstellt ein {@link ScriptHelperSub}-Untermenü
	 * @param list	Liste der bisherigen Menüpunkte zu der das neue Untermenü hinzugefügt werden soll
	 * @param title	Name des Untermenüs
	 * @param tooltip	Tooltip für das Untermenü
	 * @param icon	Icon für das Untermenü
	 * @return	Liefert das neu erstellte Untermenü
	 */
	private ScriptHelperSub getSubList(final List<Object> list, final String title, final String tooltip, final Icon icon) {
		final ScriptHelperSub sub=new ScriptHelperSub(null,title,tooltip,icon);
		list.add(sub);
		return sub;
	}

	/**
	 * Erstellt ein {@link ScriptHelperSub}-Untermenü
	 * @param parent	Übergeordnetes Menü zu der das neue Untermenü hinzugefügt werden soll
	 * @param title	Name des Untermenüs
	 * @param tooltip	Tooltip für das Untermenü
	 * @param icon	Icon für das Untermenü
	 * @return	Liefert das neu erstellte Untermenü
	 */
	private ScriptHelperSub getSubList(final ScriptHelperSub parent, final String title, final String tooltip, final Icon icon) {
		final ScriptHelperSub sub=new ScriptHelperSub(parent,title,tooltip,icon);
		parent.list.add(sub);
		return sub;
	}

	/**
	 * Erstellt ein {@link ScriptHelperSub}-Untermenü
	 * @param list	Liste der bisherigen Menüpunkte zu der das neue Untermenü hinzugefügt werden soll
	 * @param allowAdd	Optionales Callback zum prüfen, ob der Eintrag wirklich hinzugefügt werden soll
	 * @param title	Name des Untermenüs
	 * @param tooltip	Tooltip für das Untermenü
	 * @param icon	Icon für das Untermenü
	 * @param xmlMode	Um was für eine Information handelt es sich?
	 * @param xmlSelectionCommand	XML-Auswahl-Befehl
	 */
	private void tryAddRecord(final List<Object> list, final Predicate<ScriptHelperRecord> allowAdd, final String title, final String tooltip, final Icon icon, final XMLMode xmlMode, final String xmlSelectionCommand) {
		final ScriptHelperRecord record=new ScriptHelperRecord(title,title,tooltip,icon,xmlMode,xmlSelectionCommand);
		if (allowAdd!=null) {
			if (!allowAdd.test(record)) return;
		}
		list.add(record);
	}

	/**
	 * Liefert den Gesamttitel eines Eintrag
	 * @param parent	Eintrag
	 * @return	Gesamttitel (inkl. der Elternelemente)
	 */
	private String buildParentTitle(final ScriptHelperSub parent) {
		if (parent.parent!=null) return buildParentTitle(parent.parent)+" - "+parent.title;
		return parent.title;
	}

	/**
	 * Erstellt ein {@link ScriptHelperSub}-Untermenü
	 * @param parent	Übergeordnetes Menü zu der das neue Untermenü hinzugefügt werden soll
	 * @param allowAdd	Optionales Callback zum prüfen, ob der Eintrag wirklich hinzugefügt werden soll
	 * @param title	Name des Untermenüs
	 * @param tooltip	Tooltip für das Untermenü
	 * @param icon	Icon für das Untermenü
	 * @param xmlMode	Um was für eine Information handelt es sich?
	 * @param xmlSelectionCommand	XML-Auswahl-Befehl
	 */
	private void tryAddRecord(final ScriptHelperSub parent, final Predicate<ScriptHelperRecord> allowAdd, final String title, final String tooltip, final Icon icon, final XMLMode xmlMode, final String xmlSelectionCommand) {
		final ScriptHelperRecord record=new ScriptHelperRecord(buildParentTitle(parent)+" - "+title,title,tooltip,icon,xmlMode,xmlSelectionCommand);
		if (allowAdd!=null) {
			if (!allowAdd.test(record)) return;
		}
		parent.list.add(record);
	}

	/**
	 * Erstellt eine Liste mit passenden Vorlagen.
	 * @param statistics	Statistikobjekt
	 * @param allowAdd	Optionales Callback zum prüfen, ob ein Eintrag wirklich hinzugefügt werden soll
	 * @return	Liste mit Vorlagen
	 */
	public List<Object> getStatisticsTemplatesList(final Statistics statistics, final Predicate<ScriptHelperRecord> allowAdd) {
		final List<Object> list=new ArrayList<>();

		final String mean="["+Language.tr("Statistics.XML.Mean")+"]";
		final String Std="["+Language.tr("Statistics.XML.StdDev")+"]";
		final String CV="["+Language.tr("Statistics.XML.CV")+"]";
		final String Min="["+Language.tr("Statistics.XML.Minimum")+"]";
		final String Max="["+Language.tr("Statistics.XML.Maximum")+"]";
		final String count="["+Language.tr("Statistics.XML.Count")+"]";
		final String part="["+Language.tr("Statistics.XML.Part")+"]";
		final String value="["+Language.tr("Statistics.XML.Value")+"]";
		final String quotient="["+Language.tr("Statistics.XML.Quotient")+"]";

		String xmlSub;
		ScriptHelperSub main, sub, sub2;

		/* Wartezeiten */

		sub=getSubList(
				list,
				Language.tr("Statistic.FastAccess.Template.WaitingTime"),
				Language.tr("Statistic.FastAccess.Template.WaitingTime.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
		xmlSub=Language.tr("Statistics.XML.Element.WaitingAllClients");
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);

		if (statistics.clientsWaitingTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.WaitingTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingClients");
			for (String name: statistics.clientsWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsWaitingTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.WaitingTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingStations");
			for (String name: statistics.stationsWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsTotalWaitingTimes.getNames().length>0) { /* Auch relevant, wenn nur eine Station vorhanden ist, daher ">0". */
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.WaitingTime.ByStationTotal"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingStationsTotal");
			for (String name: statistics.stationsTotalWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsWaitingTimesByClientType.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.WaitingTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingStationsByClientType");
			for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		/* Transferzeiten */

		sub=getSubList(
				list,
				Language.tr("Statistic.FastAccess.Template.TransferTime"),
				Language.tr("Statistic.FastAccess.Template.TransferTime.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
		xmlSub=Language.tr("Statistics.XML.Element.TransferAllClients");
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);

		if (statistics.clientsTransferTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.TransferTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferClients");
			for (String name: statistics.clientsWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsTransferTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.TransferTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferStations");
			for (String name: statistics.stationsTransferTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsTotalTransferTimes.getNames().length>0) { /* Auch relevant, wenn nur eine Station vorhanden ist, daher ">0". */
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.TransferTime.ByStationTotal"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferStationsTotal");
			for (String name: statistics.stationsTotalTransferTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsTransferTimesByClientType.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.TransferTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferStationsByClientType");
			for (String name: statistics.stationsTransferTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		/* Bedienzeiten */

		sub=getSubList(
				list,
				Language.tr("Statistic.FastAccess.Template.ProcessTime"),
				Language.tr("Statistic.FastAccess.Template.ProcessTime.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
		xmlSub=Language.tr("Statistics.XML.Element.ProcessAllClients");
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);

		if (statistics.clientsProcessingTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ProcessTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessClients");
			for (String name: statistics.clientsProcessingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsProcessingTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ProcessTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessStations");
			for (String name: statistics.stationsProcessingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsTotalProcessingTimes.getNames().length>0) { /* Auch relevant, wenn nur eine Station vorhanden ist, daher ">0". */
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ProcessTime.ByStationTotal"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessStationsTotal");
			for (String name: statistics.stationsTotalProcessingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsProcessingTimesByClientType.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ProcessTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessStationsByClientType");
			for (String name: statistics.stationsProcessingTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		/* Verweilzeiten */

		sub=getSubList(
				list,
				Language.tr("Statistic.FastAccess.Template.ResidenceTime"),
				Language.tr("Statistic.FastAccess.Template.ResidenceTime.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
		xmlSub=Language.tr("Statistics.XML.Element.ResidenceAllClients");
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
		tryAddRecord(sub,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);

		if (statistics.clientsResidenceTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceClients");
			for (String name: statistics.clientsResidenceTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsResidenceTimes.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceStations");
			for (String name: statistics.stationsResidenceTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsTotalResidenceTimes.getNames().length>0) { /* Auch relevant, wenn nur eine Station vorhanden ist, daher ">0". */
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByStationTotal"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceStationsTotal");
			for (String name: statistics.stationsTotalResidenceTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.stationsResidenceTimesByClientType.getNames().length>1) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceStationsByClientType");
			for (String name: statistics.stationsResidenceTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		/* Trenner */

		list.add(null);

		/* Kunden im System */

		tryAddRecord(
				list,
				allowAdd,
				Language.tr("Statistic.FastAccess.Template.ClientsInSystem"),
				Language.tr("Statistic.FastAccess.Template.ClientsInSystem.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),
				XMLMode.XML_NUMBER,
				Language.tr("Statistics.XML.Element.ClientsInSystem")+mean);

		tryAddRecord(
				list,
				allowAdd,
				Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue"),
				Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),
				XMLMode.XML_NUMBER,
				Language.tr("Statistics.XML.Element.ClientsInSystemWaiting")+mean);

		tryAddRecord(
				list,
				allowAdd,
				Language.tr("Statistic.FastAccess.Template.ClientsInSystemProcess"),
				Language.tr("Statistic.FastAccess.Template.ClientsInSystemProcess.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),
				XMLMode.XML_NUMBER,
				Language.tr("Statistics.XML.Element.ClientsInSystemProcessAll")+mean);

		/* Kunden an den Stationen */

		if (statistics.clientsInSystemByClient.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStationByStation"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationByType");
			for (String name: statistics.clientsInSystemByClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationByStation.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStation"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStation");
			for (String name: statistics.clientsAtStationByStation.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationByStationAndClient.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStationByStationAndType"),null,Images.SCRIPT_RECORD_DATA_STATION.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationByClientType");
			for (String name: statistics.clientsAtStationByStationAndClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationQueueByClient.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsInSystemQueue");
			for (String name: statistics.clientsAtStationQueueByClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationQueueByStation.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStationQueue"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationQueue");
			for (String name: statistics.clientsAtStationQueueByStation.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationQueueByStationAndClient.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStationQueueByClientType"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationQueueByClientType");
			for (String name: statistics.clientsAtStationQueueByStationAndClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationProcessByClient.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsInSystemProcess"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsInSystemProcess");
			for (String name: statistics.clientsAtStationQueueByClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationProcessByStation.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStationProcess"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationProcess");
			for (String name: statistics.clientsAtStationProcessByStation.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.clientsAtStationProcessByStationAndClient.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ClientsAtStationProcessByClientType"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationProcessByClientType");
			for (String name: statistics.clientsAtStationProcessByStationAndClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		/* Trenner */

		list.add(null);

		/* Auslastung */

		if (statistics.editModel.resources.getResources().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ResourceUtilization")+" ("+Language.tr("Statistic.FastAccess.Template.ResourceUtilization.AverageNumber")+")",null,Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon());
			final String xmlMainAll=Language.tr("Statistics.XML.Element.UtilizationAll");
			tryAddRecord(sub,allowAdd,Language.tr("Statistic.FastAccess.Template.ResourceUtilization.Total"),null,null,XMLMode.XML_NUMBER,xmlMainAll+mean);
			final String xmlMain=Language.tr("Statistics.XML.Element.Utilization");
			for (ModelResource resource: statistics.editModel.resources.getResources()) {
				final String name=resource.getName();
				xmlSub=Language.tr("Statistics.XML.Element.UtilizationResource")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}
		if (statistics.resourceRho.size()>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ResourceUtilization")+" ("+Language.tr("Statistic.FastAccess.Template.ResourceUtilization.rho")+")",null,Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon());
			final String xmlMainAll=Language.tr("Statistics.XML.Element.UtilizationResourceRhoAll");
			tryAddRecord(sub,allowAdd,Language.tr("Statistic.FastAccess.Template.ResourceUtilization.Total"),null,null,XMLMode.XML_NUMBER,xmlMainAll+value);
			final String xmlMain=Language.tr("Statistics.XML.Element.Rho");
			for (String name: statistics.resourceRho.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Element.UtilizationResourceRho")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		/* Trenner */

		list.add(null);

		/* Zähler & Nutzerstatistik */

		if (statistics.counter.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.Counter"),null,Images.SCRIPT_RECORD_DATA_COUNTER.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Element.Counter");
			final String countName=Language.tr("Statistics.Number");
			final String partName=Language.tr("Statistics.Part");
			for (String name: statistics.counter.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Element.CounterName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name+" ("+countName+")",null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+count);
				tryAddRecord(sub,allowAdd,name+" ("+partName+")",null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+part);
			}
		}

		if (statistics.differentialCounter.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.DifferentialCounter"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.DifferenceCounter");
			for (String name: statistics.differentialCounter.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Element.DifferenceCounterName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		if (statistics.throughputStatistics.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.ThroughputCounter"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.ThroughputStatistics");
			for (String name: statistics.throughputStatistics.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ThroughputStatisticsName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+quotient);
			}
		}

		if (statistics.userStatistics.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.UserStatistics"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.UserStatistics");
			for (String name: statistics.userStatistics.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.UserStatisticsKey")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		if (statistics.userStatisticsContinuous.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.UserStatisticsContinuous"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.UserStatisticsContinuous");
			for (String name: statistics.userStatisticsContinuous.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.UserStatisticsContinuousKey")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		/* Variablenwerte */

		if (statistics.userVariables.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.Variables"),null,Images.EXPRESSION_BUILDER_VARIABLE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.Variables");
			for (String name: statistics.userVariables.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.VariablesKey")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub2=getSubList(sub,name,null,null);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min);
				tryAddRecord(sub2,allowAdd,Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max);
			}
		}

		/* Analoge Werte */

		if (statistics.analogStatistics.getNames().length>0) {
			sub=getSubList(list,Language.tr("Statistic.FastAccess.Template.AnalogValues"),null,Images.SCRIPT_RECORD_ANALOG_VALUE.getIcon());
			final String xmlMain=Language.tr("Statistics.XML.AnalogStatistics");
			for (String name: statistics.analogStatistics.getNames()) {
				xmlSub=Language.tr("Statistics.XML.AnalogStatisticsName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean);
			}
		}

		/* Trenner */

		list.add(null);

		/* Kosten */

		main=getSubList(list,Language.tr("Statistic.FastAccess.Template.Costs"),null,Images.SCRIPT_RECORD_DATA_COSTS.getIcon());

		/* Kosten - Kunden */

		if (statistics.clientsCostsWaiting.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.ClientsCostsWaiting"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsWaiting");
			for (String name: statistics.clientsCostsWaiting.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		if (statistics.clientsCostsTransfer.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.ClientsCostsTransfer"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsTransfer");
			for (String name: statistics.clientsCostsTransfer.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		if (statistics.clientsCostsProcess.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.ClientsCostsProcess"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsProcess");
			for (String name: statistics.clientsCostsProcess.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		/* Kosten - Stationen */

		if (statistics.stationCosts.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.StationCosts"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsStations");
			for (String name: statistics.stationCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		/* Kosten - Ressourcen */

		if (statistics.resourceTimeCosts.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.ResourceTimeCosts"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.ResourceTimeCosts");
			for (String name: statistics.resourceTimeCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		if (statistics.resourceIdleCosts.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.ResourceIdleCosts"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.ResourceIdleCosts");
			for (String name: statistics.resourceIdleCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		if (statistics.resourceWorkCosts.getNames().length>0) {
			sub=getSubList(main,Language.tr("Statistic.FastAccess.Template.ResourceWorkCosts"),null,null);
			final String xmlMain=Language.tr("Statistics.XML.Element.ResourceWorkCosts");
			for (String name: statistics.resourceIdleCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				tryAddRecord(sub,allowAdd,name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value);
			}
		}

		return list;
	}

	/**
	 * Erstellt einen Eintrag für ein Popup-Menü
	 * @param title	Name des Menüpunkts
	 * @param hint	Tooltip für den Menüpunkt
	 * @param icon	Icon für den Menüpunkt
	 * @param listener	Callback, der beim Anklicken des Menüpunkts ausgeführt werden soll
	 * @return	Neuer Menüpunkt
	 */
	public static JMenuItem getMenuItem(final String title, final String hint, final Icon icon, final ActionListener listener) {
		final JMenuItem item=new JMenuItem(title);
		if (hint!=null && !hint.trim().isEmpty()) item.setToolTipText(hint);
		if (icon!=null) item.setIcon(icon);
		item.addActionListener(listener);
		return item;
	}

	/**
	 * Erstellt ein Untermenü für ein Popup-Menü
	 * @param title	Name des Untermenüs
	 * @param hint	Tooltip für das Untermenü
	 * @param icon	Icon für das Untermenü
	 * @return	Neues Untermenü
	 */
	public static JMenu getSubMenu(final String title, final String hint, final Icon icon) {
		final JMenu menu=new JMenu(title);
		if (hint!=null && !hint.trim().isEmpty()) menu.setToolTipText(hint);
		if (icon!=null) menu.setIcon(icon);
		return menu;
	}

	/**
	 * Fügt Bearbeiten-Menüpunkte zu einem Popupmenü hinzu
	 * @param popupMenu	Popupmenü
	 * @param model	Editor-Modell
	 * @param statistics	Statistik-Objekt, aus dem die Bezeichner für Stationen usw. ausgelesen werden
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 * @param help	Hilfe-Runnable
	 * @param allowSave	Gibt an, ob der Menüpunkt zur Generierung eines Speichern-Befehls in dem Menü angezeigt werden soll
	 */
	private void addEditToPopup(final JPopupMenu popupMenu, final EditModel model, final Statistics statistics, final JTextArea textArea, final Runnable update, final Runnable help, final boolean allowSave) {
		JMenu menu;

		if (model!=null) {
			popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.SelectXMLTag.Model"),Language.tr("Statistic.FastAccess.SelectXMLTag.Model.Tooltip"),Images.SCRIPT_RECORD_XML.getIcon(),e->commandSelectModelXMLTag(popupMenu,model,help,textArea,update)));
			popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.SelectXMLTag.Statistics"),Language.tr("Statistic.FastAccess.SelectXMLTag.Statistics.Tooltip"),Images.SCRIPT_RECORD_XML.getIcon(),e->commandSelectStatisticsXMLTag(popupMenu,statistics,help,textArea,update)));
		} else {
			popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.SelectXMLTag"),Language.tr("Statistic.FastAccess.SelectXMLTag.Tooltip"),Images.SCRIPT_RECORD_XML.getIcon(),e->commandSelectStatisticsXMLTag(popupMenu,statistics,help,textArea,update)));
		}

		popupMenu.addSeparator();

		popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Tab"),Language.tr("Statistic.FastAccess.Template.Tab.Tooltip"),Images.SCRIPT_RECORD_TEXT.getIcon(),e->commandAddTab(textArea,update)));
		popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.NewLine"),Language.tr("Statistic.FastAccess.Template.NewLine.Tooltip"),Images.SCRIPT_RECORD_TEXT.getIcon(),e->commandAddNewLine(textArea,update)));

		popupMenu.add(menu=getSubMenu(Language.tr("Statistic.FastAccess.Template.Format"),Language.tr("Statistic.FastAccess.Template.Format.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon()));
		menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.System"),Language.tr("Statistic.FastAccess.Template.Format.System.Hint"),null,e->commandText("Statistics.setFormat(\"System\");",textArea,update)));
		menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Local"),Language.tr("Statistic.FastAccess.Template.Format.Local.Hint"),null,e->commandText("Statistics.setFormat(\"Local\");",textArea,update)));
		menu.addSeparator();
		menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Fraction"),Language.tr("Statistic.FastAccess.Template.Format.Fraction.Hint"),null,e->commandText("Statistics.setFormat(\"Fraction\");",textArea,update)));
		menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Percent"),Language.tr("Statistic.FastAccess.Template.Format.Percent.Hint"),null,e->commandText("Statistics.setFormat(\"Percent\");",textArea,update)));
		menu.addSeparator();
		menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Time"),Language.tr("Statistic.FastAccess.Template.Format.Time.Hint"),null,e->commandText("Statistics.setFormat(\"Time\");",textArea,update)));
		menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Format.Number"),Language.tr("Statistic.FastAccess.Template.Format.Number.Hint"),null,e->commandText("Statistics.setFormat(\"Number\");",textArea,update)));

		final String path=Language.tr("Statistic.FastAccess.Template.Parameter.Path");
		final String number=Language.tr("Statistic.FastAccess.Template.Parameter.Number");
		final String resource=Language.tr("Statistic.FastAccess.Template.Parameter.ResourceName");
		final String variable=Language.tr("Statistic.FastAccess.Template.Parameter.VariableName");
		final String expression=Language.tr("Statistic.FastAccess.Template.Parameter.Expression");

		if (model!=null) {
			popupMenu.addSeparator();

			popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Reset"),Language.tr("Statistic.FastAccess.Template.Reset.Tooltip"),Images.SCRIPT_RECORD_MODEL.getIcon(),e->commandText("Model.reset();",textArea,update)));
			popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Run"),Language.tr("Statistic.FastAccess.Template.Run.Tooltip"),Images.SCRIPT_RECORD_MODEL.getIcon(),e->commandText("Model.run();",textArea,update)));

			popupMenu.add(menu=getSubMenu(Language.tr("Statistic.FastAccess.Template.ChangeModel"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon()));

			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.SetValue"),Language.tr("Statistic.FastAccess.Template.SetValue.Tooltip"),null,e->commandText("Model.setValue("+path+","+number+");",textArea,update)));
			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.SetMean"),Language.tr("Statistic.FastAccess.Template.SetMean.Tooltip"),null,e->commandText("Model.setMean("+path+","+number+");",textArea,update)));
			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.SetSD"),Language.tr("Statistic.FastAccess.Template.SetSD.Tooltip"),null,e->commandText("Model.setSD("+path+","+number+");",textArea,update)));

			menu.addSeparator();

			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Resource.Get"),Language.tr("Statistic.FastAccess.Template.Resource.Get.Tooltip"),null,e->commandText("Model.getResourceCount(\""+resource+"\");",textArea,update)));
			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Resource.Set"),Language.tr("Statistic.FastAccess.Template.Resource.Set.Tooltip"),null,e->commandText("Model.setResourceCount(\""+resource+"\","+number+");",textArea,update)));

			menu.addSeparator();

			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Variable.Get"),Language.tr("Statistic.FastAccess.Template.Variable.Get.Tooltip"),null,e->commandText("Model.getGlobalVariableInitialValue(\""+variable+"\");",textArea,update)));
			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Variable.Set"),Language.tr("Statistic.FastAccess.Template.Variable.Set.Tooltip"),null,e->commandText("Model.setGlobalVariableInitialValue(\""+variable+"\",\""+expression+"\");",textArea,update)));

			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Map.Get"),Language.tr("Statistic.FastAccess.Template.Map.Get.Tooltip"),null,e->commandText("Model.getGlobalMapInitialValue(\""+variable+"\");",textArea,update)));
			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.Map.Set"),Language.tr("Statistic.FastAccess.Template.Map.Set.Tooltip"),null,e->commandText("Model.setGlobalMapInitialValue(\""+variable+"\",\""+expression+"\");",textArea,update)));

			menu.addSeparator();

			menu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.StationID.Get"),Language.tr("Statistic.FastAccess.Template.StationID.Get.Tooltip"),null,e->commandText("Model.getStationID(\"StationName\");",textArea,update)));
		}

		if (allowSave) {
			popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.Template.SaveStatistics"),Language.tr("Statistic.FastAccess.Template.SaveStatistics.Tooltip"),Images.SCRIPT_RECORD_STATISTICS_SAVE.getIcon(),e->commandText("Statistics.save(\"FileName\");",textArea,update)));
		}
	}

	/**
	 * Erstellt ein Untermenü
	 * @param parent	Übergeordnetes Menü
	 * @param main	Wurzel-Script-Helper-Objekt
	 * @param listener	Liste der Unterpunkte
	 * @return	Liefert <code>true</code>, wenn Einträge hinzugefügt werden konnten
	 */
	private boolean addSub(final JMenu parent, final ScriptHelperSub main, final Consumer<ScriptHelperRecord> listener) {
		if (main.list.isEmpty()) return false;
		final JMenu menu=main.addToPopup(parent);
		boolean lastSubIsSeparator=false;
		for (Object sub: main.list) {
			if (sub==null) {
				if (menu.getComponentCount()>0) {
					if (!lastSubIsSeparator) menu.addSeparator();
					lastSubIsSeparator=true;
				}
				continue;
			}
			lastSubIsSeparator=false;
			if (sub instanceof ScriptHelperRecord) ((ScriptHelperRecord)sub).addToPopup(menu,listener);
			if (sub instanceof ScriptHelperSub) {
				if (addSub(menu,(ScriptHelperSub)sub,listener)) lastSubIsSeparator=false;
			}
		}
		if (menu.getComponentCount()>0 && menu.getComponent(menu.getComponentCount()-1) instanceof javax.swing.JPopupMenu.Separator) menu.remove(menu.getComponentCount()-1);
		return true;
	}

	/**
	 * Fügt Einträge zur Auswahl von Vorlage-Elementen an ein Popup-Menü an.
	 * @param popupMenu	Popup-Menü an das die Einträge angefügt werden sollen
	 * @param statistics	Statistik-Objekt, aus dem die Bezeichner für Stationen usw. ausgelesen werden
	 * @param listener	Listener, der benachrichtigt wird, wenn ein Menüpunkt angeklickt wird
	 * @param allowAdd	Prüft, ob ein Menüpunkt zum Menü hinzugefügt werden darf (kann <code>null</code> sein)
	 */
	public void popupCustom(final JPopupMenu popupMenu, final Statistics statistics, final Consumer<ScriptHelperRecord> listener, final Predicate<ScriptHelperRecord> allowAdd) {
		boolean lastObjIsSeparator=false;
		for (Object obj: getStatisticsTemplatesList(statistics,allowAdd)) {
			if (obj==null) {
				if (popupMenu.getComponentCount()>0 && !lastObjIsSeparator) {popupMenu.addSeparator(); lastObjIsSeparator=true;}
				continue;
			}
			if (obj instanceof ScriptHelperRecord) {
				lastObjIsSeparator=false;
				((ScriptHelperRecord)obj).addToPopup(popupMenu,listener);
			}
			if (obj instanceof ScriptHelperSub) {
				if (((ScriptHelperSub)obj).list.isEmpty()) continue;
				lastObjIsSeparator=false;
				final JMenu menu=((ScriptHelperSub)obj).addToPopup(popupMenu);
				boolean lastSubIsSeparator=false;
				for (Object sub: ((ScriptHelperSub)obj).list) {
					if (sub==null) {
						if (menu.getComponentCount()>0 && !lastSubIsSeparator) {menu.addSeparator(); lastSubIsSeparator=true;}
						continue;
					}
					lastSubIsSeparator=false;
					if (sub instanceof ScriptHelperRecord) ((ScriptHelperRecord)sub).addToPopup(menu,listener);
					if (sub instanceof ScriptHelperSub) {
						if (addSub(menu,(ScriptHelperSub)sub,listener)) lastSubIsSeparator=false;
					}
				}
				if (menu.getComponentCount()>0 && menu.getComponent(menu.getComponentCount()-1) instanceof javax.swing.JPopupMenu.Separator) menu.remove(menu.getComponentCount()-1);
			}
		}
		if (popupMenu.getComponentCount()>0 && popupMenu.getComponent(popupMenu.getComponentCount()-1) instanceof javax.swing.JPopupMenu.Separator) popupMenu.remove(popupMenu.getComponentCount()-1);
	}

	/**
	 * Zeigt ein Popup-Menü mit möglichen Befehlen, die in ein Textfeld eingefügt werden können, an.
	 * @param model	Editor-Model, aus dem Bezeichner für Stationen usw. ausgelesen werden (optional, kann <code>null</code> sein)
	 * @param statistics	Statistik-Objekt, aus dem die Bezeichner für Stationen usw. ausgelesen werden
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 * @param allowSave	Gibt an, ob der Menüpunkt zur Generierung eines Speichern-Befehls in dem Menü angezeigt werden soll
	 */
	public void popupFull(final EditModel model, final Statistics statistics, final JTextArea textArea, final Runnable update, final boolean allowSave) {
		final JPopupMenu popupMenu=new JPopupMenu();
		addEditToPopup(popupMenu,model,statistics,textArea,update,help,allowSave);
		popupMenu.addSeparator();
		popupCustom(popupMenu,statistics,record->commandXMLOutput(record.xml,record.xmlMode,textArea,update),null);
		popupMenu.show(owner,0,owner.getHeight());
	}

	/**
	 * Zeigt ein Popup-Menü mit möglichen Befehlen, die in ein Textfeld eingefügt werden können, an.
	 * @param statistics	Statistik-Objekt, aus dem die Bezeichner für Stationen usw. ausgelesen werden
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 * @param allowSave	Gibt an, ob der Menüpunkt zur Generierung eines Speichern-Befehls in dem Menü angezeigt werden soll
	 */
	public void popupFull(final Statistics statistics, final JTextArea textArea, final Runnable update, final boolean allowSave) {
		popupFull(null,statistics,textArea,update,allowSave);
	}

	/**
	 * Fügt den "XML-Element auswählen"-Eintrag zu einem Popupmenü hinzu.
	 * @param popupMenu	Popup-Menü an das der Eintrag angefügt werden sollen
	 * @param statistics	Statistik-Objekt, aus dem die Bezeichner für Stationen usw. ausgelesen werden
	 * @param listener	Listener, der benachrichtigt wird, wenn ein Menüpunkt angeklickt wird
	 * @param help	Hilfe-Runnable
	 */
	public void addSelectXML(final JPopupMenu popupMenu, final Statistics statistics, final Consumer<ScriptHelperRecord> listener, final Runnable help) {
		popupMenu.add(getMenuItem(Language.tr("Statistic.FastAccess.SelectXMLTag"),Language.tr("Statistic.FastAccess.SelectXMLTag.Tooltip"),Images.SCRIPT_RECORD_XML.getIcon(),e->{
			final StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(owner,statistics.saveToXMLDocument(),help,true);
			dialog.setVisible(true);
			if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
			listener.accept(new ScriptHelperRecord("","","",null,XMLMode.XML_TEXT,dialog.getXMLSelector()));
		}));
	}

	/**
	 * Zeigt ein Popup-Menü mit Befehlen zur Auswahl von Vorlage-Elementen an.
	 * @param statistics	Statistik-Objekt, aus dem die Bezeichner für Stationen usw. ausgelesen werden
	 * @param listener	Listener, der benachrichtigt wird, wenn ein Menüpunkt angeklickt wird
	 * @param allowAdd	Prüft, ob ein Menüpunkt zum Menü hinzugefügt werden darf (kann <code>null</code> sein)
	 * @param addSelectXML	Soll angeboten werden, xml-Elemente über den Dialog auszuwählen?
	 */
	public void popupCustom(final Statistics statistics, final Consumer<ScriptHelperRecord> listener, final Predicate<ScriptHelperRecord> allowAdd, final boolean addSelectXML) {
		final JPopupMenu popupMenu=new JPopupMenu();
		if (addSelectXML) {
			addSelectXML(popupMenu,statistics,listener,help);
			popupMenu.addSeparator();
		}
		popupCustom(popupMenu,statistics,listener,allowAdd);
		popupMenu.show(owner,0,owner.getHeight());
	}

	/**
	 * Fügt einen XML-Daten-Ausgabebefehl in in das Skript-Eingabefeld ein.
	 * @param text	XML-Pfad
	 * @param xmlMode	Um was für eine Information handelt es sich?
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandXMLOutput(final String text, final XMLMode xmlMode, final JTextArea textArea, final Runnable update) {
		if (xmlMode==XMLMode.XML_NUMBER || xmlMode==XMLMode.XML_NUMBER_TIME) {
			commandText("Output.println(Statistics.xmlNumber(\""+text.replace("\"","\\\"")+"\"));",textArea,update);
		} else {
			commandText("Output.println(Statistics.xml(\""+text.replace("\"","\\\"")+"\"));",textArea,update);
		}
	}

	/**
	 * Fügt einen Text in das Skript-Eingabefeld ein.
	 * @param text	Auszugebender Text
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandText(final String text, final JTextArea textArea, final Runnable update) {
		if (textArea!=null) {
			String s=textArea.getText();
			s=s.substring(0,textArea.getCaretPosition())+text+"\n"+s.substring(textArea.getCaretPosition());
			textArea.setText(s);
		}

		if (update!=null) {
			update.run();
		}
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines XML-Elements an und fügt dann einen entsprechenden Ausgabebefehl in das Skript-Eingabefeld ein.
	 * @param owner	Übergeordnetes Elemente (zur Ausrichtung des Dialogs)
	 * @param doc	XML-Dokument
	 * @param command	Befehl zur Ausgabe des XML-Pfades
	 * @param help	Hilfe-Runnable
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandSelect(final Component owner, final Document doc, final String command, final Runnable help, final JTextArea textArea, final Runnable update) {
		final StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(owner,doc,help,false);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		String xmlSelector=dialog.getXMLSelector();
		xmlSelector=command+"(\""+xmlSelector.replace("\"","\\\"")+"\")";
		int insertType=dialog.getInsertType();
		if (insertType==0 || insertType==1) xmlSelector="Output.println("+xmlSelector+");"; else xmlSelector=xmlSelector+";";
		String s=textArea.getText();
		if (insertType==1 || insertType==3) {
			s=s.substring(0,textArea.getCaretPosition())+xmlSelector+s.substring(textArea.getCaretPosition());
		} else {
			if (!s.endsWith("\n")) s+="\n";
			s+=xmlSelector;
		}
		textArea.setText(s);
		if (update!=null) update.run();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines XML-Elements aus den Modelldaten an und fügt dann einen entsprechenden Ausgabebefehl in das Skript-Eingabefeld ein.
	 * @param owner	Übergeordnetes Elemente (zur Ausrichtung des Dialogs)
	 * @param model	Editor-Modell
	 * @param help	Hilfe-Runnable
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandSelectModelXMLTag(final Component owner, final EditModel model, final Runnable help, final JTextArea textArea, final Runnable update) {
		commandSelect(owner,model.saveToXMLDocument(),"Model.xml",help,textArea,update);
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines XML-Elements aus den Statistikdaten an und fügt dann einen entsprechenden Ausgabebefehl in das Skript-Eingabefeld ein.
	 * @param owner	Übergeordnetes Elemente (zur Ausrichtung des Dialogs)
	 * @param statistics	Statistikobjekt
	 * @param help	Hilfe-Runnable
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandSelectStatisticsXMLTag(final Component owner, final Statistics statistics, final Runnable help, final JTextArea textArea, final Runnable update) {
		commandSelect(owner,statistics.saveToXMLDocument(),"Statistics.xml",help,textArea,update);
	}

	/**
	 * Fügt einen Befehl zur Ausgabe eines Tabulators in das Skript-Eingabefeld ein.
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandAddTab(final JTextArea textArea, final Runnable update) {
		commandText("Output.tab();",textArea,update);
	}

	/**
	 * Fügt einen Befehl zur Ausgabe eines Zeilenumbruchs in das Skript-Eingabefeld ein.
	 * @param textArea	Textfeld, in das die Daten eingefügt werden sollen
	 * @param update	Runnable, das aufgerufen wird, wenn ein Befehl im Popup-Menü gewählt wurde (kann <code>null</code> sein)
	 */
	private void commandAddNewLine(final JTextArea textArea, final Runnable update) {
		commandText("Output.newLine();",textArea,update);
	}

	/**
	 * Um was für eine Information handelt es sich
	 * @author Alexander Herzog
	 */
	public enum XMLMode {
		/** Text-Information */
		XML_TEXT,
		/** Zeit-Information */
		XML_NUMBER_TIME,
		/** Zahlen-Information */
		XML_NUMBER
	}

	/**
	 * Diese Klasse hält Informationen zu einem einzelnen Menüpunkt,
	 * der die Auswahl von Statistikdaten (im Skript-Kontext) ermöglicht.
	 * @author Alexander Herzog
	 */
	public static class ScriptHelperRecord {
		/**
		 * Vollständiger Name
		 */
		public final String title;

		/**
		 * Im Menü anzuzeigender Name
		 */
		private final String showTitle;

		/**
		 * Tooltip für den Menüpunkt
		 */
		private final String tooltip;


		/**
		 * Um was für eine Information handelt es sich
		 * @see ListPopup.XMLMode
		 */
		public final XMLMode xmlMode;

		/**
		 * Bezeichner des xml-Elements
		 */
		public final String xml;

		/**
		 * Icon für den Menüpunkt
		 */
		private final Icon icon;

		/**
		 * Konstruktor der Klasse
		 * @param fullTitle	Vollständiger Name
		 * @param title	Im Menü anzuzeigender Name
		 * @param tooltip	Tooltip für den Menüpunkt
		 * @param icon	Icon für den Menüpunkt
		 * @param xmlMode	Um was für eine Information handelt es sich
		 * @param xml	Bezeichner des xml-Elements
		 * @see ListPopup.XMLMode
		 */
		public ScriptHelperRecord(final String fullTitle, final String title, final String tooltip, final Icon icon, final XMLMode xmlMode, final String xml) {
			this.title=fullTitle;
			this.showTitle=title;
			this.tooltip=tooltip;
			this.xmlMode=xmlMode;
			this.xml=xml;
			this.icon=icon;
		}

		/**
		 * Fügt den Menüpunktdatensatz zu einem Popupmenü hin
		 * @param popup	Popupmenü zu dem der Menüpunktdatensatz hinzugefügt werden soll
		 * @param listener	Listener der aufgerufen wird, wenn der Menüpunkt angeklickt wird
		 */
		public void addToPopup(final JPopupMenu popup, final Consumer<ScriptHelperRecord> listener) {
			final JMenuItem item=getMenuItem(showTitle,tooltip,icon,e->listener.accept(this));
			popup.add(item);
		}

		/**
		 * Fügt den Menüpunktdatensatz zu einem Menü hin
		 * @param menu	Menü zu dem der Menüpunktdatensatz hinzugefügt werden soll
		 * @param listener	Listener der aufgerufen wird, wenn der Menüpunkt angeklickt wird
		 */
		public void addToPopup(final JMenu menu, final Consumer<ScriptHelperRecord> listener) {
			final JMenuItem item=getMenuItem(showTitle,tooltip,icon,e->listener.accept(this));
			menu.add(item);
		}
	}

	/**
	 * Hält die Daten für ein Untermenü für das Popupmenü vor.
	 * @see ListPopup#addSub(JMenu, ScriptHelperSub, Consumer)
	 */
	public static class ScriptHelperSub {
		/** Name des Untermenüs */
		public final String title;
		/** Tooltip für das Untermenü */
		private final String tooltip;
		/** Icon für das Untermenü */
		private final Icon icon;
		/** Einträge im Untermenü */
		public final List<Object> list;
		/** Übergeordnetes Menü */
		public final ScriptHelperSub parent;

		/**
		 * Konstruktor der Klasse
		 * @param parent	Übergeordnetes Menü
		 * @param title	Name des Untermenüs
		 * @param tooltip	Tooltip für das Untermenü
		 * @param icon	Icon für das Untermenü
		 */
		public ScriptHelperSub(final ScriptHelperSub parent, final String title, final String tooltip, final Icon icon) {
			this.parent=parent;
			this.title=title;
			this.tooltip=tooltip;
			this.icon=icon;
			list=new ArrayList<>();
		}

		/**
		 * Fügt das Untermenü an ein Popupmenü an.
		 * @param popup	Popupmenü
		 * @return	Untermenü welches angefügt wurde
		 */
		public JMenu addToPopup(final JPopupMenu popup) {
			final JMenu menu=getSubMenu(title,tooltip,icon);
			popup.add(menu);
			return menu;
		}

		/**
		 * Fügt das Untermenü an ein Menü an.
		 * @param popup	Übergeordnetes Menü
		 * @return	Untermenü welches angefügt wurde
		 */
		public JMenu addToPopup(final JMenu popup) {
			final JMenu menu=getSubMenu(title,tooltip,icon);
			popup.add(menu);
			return menu;
		}
	}
}