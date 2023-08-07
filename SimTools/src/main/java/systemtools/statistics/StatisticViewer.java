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
package systemtools.statistics;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Das Interface <code>StatisticViewer</code> definiert Methoden um beliebige Statistikdaten
 * innerhalb eines <code>StatisticsBasePanel</code> anzuzeigen.
 * @see StatisticsBasePanel
 * @author Alexander Herzog
 * @version 1.3
 */
public interface StatisticViewer {
	/**
	 * Gibt an, ob der Viewer Text, Tabelle oder Grafik ausgibt.
	 * @see StatisticViewer#getType()
	 */
	public enum ViewerType {
		/** Der Viewer gibt Text aus. */
		TYPE_TEXT,

		/** Der Viewer gibt eine Tabelle aus. */
		TYPE_TABLE,

		/** Der Viewer gibt eine Grafik aus.
		 * @see StatisticViewer#getImageType()
		 */
		TYPE_IMAGE,

		/** Der Viewer zeigt den Reportgenerator an. */
		TYPE_REPORT,

		/** Der Viewer zeigt besonderen Inhalt an. */
		TYPE_SPECIAL
	}

	/**
	 * Gibt im Falle des Typs "Grafik" an, von welchem Typ die Grafik ist.
	 * @see StatisticViewer#getImageType()
	 * @author Alexander Herzog
	 *
	 */
	public enum ViewerImageType {
		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt keine herkömmliche Grafik an. */
		IMAGE_TYPE_NOIMAGE,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt ein Liniendiagramm an. */
		IMAGE_TYPE_LINE,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt ein Balkendiagramm an. */
		IMAGE_TYPE_BAR,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt ein Tortendiagramm an. */
		IMAGE_TYPE_PIE,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt ein Fotos-Symbol an. */
		IMAGE_TYPE_PICTURE,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt eine X-Y-Punktewolke an. */
		IMAGE_TYPE_XY,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt einen Schichtplan (horizontales Balkendiagramm) an. */
		IMAGE_TYPE_SHIFTPLAN,

		/** Der Viewer ist vom Typ "Grafik" (siehe <code>getType</code>) und zeigt ein Sankey-Diagramm an. */
		IMAGE_TYPE_SANKEY
	}

	/**
	 * Welche Aktionen können mit den aktuellen Viewer ausgeführt werden?
	 * @see StatisticViewer#getCanDo(CanDoAction)
	 */
	public enum CanDoAction {

		/**
		 * Gibt an, ob es einen Zoomfaktor gibt, der durch die <code>unZoom</code>-Methode zurückgesetzt werden könnte.
		 * @see StatisticViewer#unZoom()
		 */
		CAN_DO_UNZOOM,

		/**
		 * Gibt an, ob der Viewer die Daten ausdrucken kann
		 * @see StatisticViewer#print()
		 */
		CAN_DO_PRINT,

		/**
		 * Gibt an, ob die Viewer-Daten in die Zwischenablage kopiert werden können
		 * @see StatisticViewer#copyToClipboard(Clipboard)
		 */
		CAN_DO_COPY,

		/**
		 * Gibt an, ob die Viewer-Daten ohne Rahmen in die Zwischenablage kopiert werden können
		 * @see StatisticViewer#copyToClipboardPlain(Clipboard)
		 */
		CAN_DO_COPY_PLAIN,

		/**
		 * Gibt an, ob die Viewer-Daten gespeichert werden können
		 * @see StatisticViewer#save(Component)
		 */
		CAN_DO_SAVE,

		/**
		 * Gibt an, ob in dem Viewer gesucht werden kann
		 * @see StatisticViewer#search(Component)
		 */
		CAN_DO_SEARCH,

		/**
		 * Gibt an, ob eine Navigation zwischen den Überschriften möglich ist
		 * @see StatisticViewer#navigation(JButton)
		 */
		CAN_DO_NAVIGATION
	}

	/**
	 * Gibt an, ob der Viewer Text, Tabelle oder Grafik ausgibt.
	 * @return	Typ des Viewers (siehe <code>TYPE_*</code>-Konstanten)
	 */
	ViewerType getType();

	/**
	 * Gibt im Falle des Typs "Grafik" an, von welchem Typ die Grafik ist.
	 * @return	Type der Grafik in dem Viewer (siehe <code>IMAGE_TYPE_*</code>-Konstanten)
	 */
	ViewerImageType getImageType();

	/**
	 * Gibt an, welche Symbolleisten-Elemente für diesen Viewer aktiviert werden sollen.
	 * @param canDoType	Prüft eine der <code>CAN_DO_*</code>-Eigenschaften
	 * @return	Gibt an, ob die jeweils abgefragte Eigenschaft verfügbar ist.
	 */
	boolean getCanDo(CanDoAction canDoType);

	/**
	 * Liefert ein Objekt vom Typ <code>Container</code> zurück, welches die Statistikdaten in der vom Viewer bestimmten Form anzeigt.
	 * @param needReInit	Gibt an, ob eine bereits erstellte Fassung des Containers zurückgegeben werden kann oder ob zwingend ein neues Objekt angelegt werden muss.
	 * @return	Liefert den Container, der die Statistikdaten ausgibt.
	 */
	Container getViewer(boolean needReInit);

	/**
	 * Wurde bereits per {@link #getViewer(boolean)} ein Viewer erzeugt oder würde der Aufruf von {@link #getViewer(boolean)} erstmals einen neuen Viewer anlegen?
	 * @return	Wurde bereits per {@link #getViewer(boolean)} ein Viewer erzeugt?
	 */
	boolean isViewerGenerated();

	/**
	 * Liefert ein {@link Transferable}-Objekt für den Viewer.
	 * Dieser ist dann verfügbar, wenn auch ein Kopieren möglich ist.
	 * @return	{@link Transferable}-Objekt für den Viewer
	 */
	Transferable getTransferable();

	/**
	 * Kopiert die Daten in der aktuellen Variante in die Zwischenablage.
	 * @param clipboard	System-Zwischenablage
	 */
	void copyToClipboard(Clipboard clipboard);

	/**
	 * Kopiert die Daten in der aktuellen Variante ohne Rahmen in die Zwischenablage.
	 * @param clipboard	System-Zwischenablage
	 */
	default void copyToClipboardPlain(Clipboard clipboard) {}

	/**
	 * Druckt die Daten in der aktuellen Variante aus.
	 * @return	Liefert <code>true</code> zurück, wenn der Ausdruck erfolgreich war.
	 */
	boolean print();

	/**
	 * Speichert die Daten in der aktuellen Variante.
	 * Dafür muss ein Dateiauswahldialog angezeigt werden usw.
	 * @param owner	Übergeordnete Komponente für die Anzeige von Dialogen
	 */
	void save(Component owner);

	/**
	 * Blendet die Navigationsbaumstruktur ein oder aus.
	 * @param button	Schaltfläche von der die Aktion ausging
	 */
	void navigation(final JButton button);

	/**
	 * Führt eine Suche im aktuellen Viewer durch.
	 * @param owner	Übergeordnete Komponente für die Anzeige von Dialogen
	 */
	void search(Component owner);

	/**
	 * Speichert die Daten in der aktuellen Variante in der angegebenen Datei.
	 * @param owner	Übergeordnete Komponente für die eventuelle Anzeige von Dialogen
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> übergeben werden.
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	boolean save(Component owner, File file);

	/**
	 * Schreibt die Daten in ein html-Dokument
	 * @param bw	Ausgabestream, der später zur html-Datei wird
	 * @param mainFile	Name der Hauptdatei (zur Bestimmung der Namen von externen Bildern)
	 * @param nextImageNr	Nummer für das nächste (externe) Bild
	 * @param imagesInline	Gibt an, ob Bilder inline in die html-Datei eingebettet werden sollen
	 * @return	Nummer des nächsten Bildes (entspricht <code>nextImageNr</code>, wenn kein Bild ausgegeben wurde)
	 * @throws IOException	Die Exception wird ausgelöst, wenn die Dateiausgabe nicht durchgeführt werden konnte.
	 */
	int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) throws IOException;

	/**
	 * Schreibt die Daten in ein LaTeX-Dokument
	 * @param bw	Ausgabestream, der später zur LaTeX-Datei wird
	 * @param mainFile	Name der Hauptdatei (zur Bestimmung der Namen von externen Bildern)
	 * @param nextImageNr	Nummer für das nächste (externe) Bild
	 * @return	Nummer des nächsten Bildes (entspricht <code>nextImageNr</code>, wenn kein Bild ausgegeben wurde)
	 * @throws IOException	Die Exception wird ausgelöst, wenn die Dateiausgabe nicht durchgeführt werden konnte.
	 */
	int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) throws IOException;

	/**
	 * Schreibt die Daten in ein docx-Dokument
	 * @param doc	Aktives docx-Dokument
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	boolean saveDOCX(DOCXWriter doc);

	/**
	 * Schreibt die Daten in ein pdf-Dokument
	 * @param pdf	Aktives pdf-Dokument
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	boolean savePDF(PDFWriter pdf);

	/**
	 * Stellt den Standardzoomfaktor wieder her.
	 * @see #getCanDo(CanDoAction)
	 * @see CanDoAction#CAN_DO_UNZOOM
	 */
	void unZoom();

	/**
	 * Liefert optionale zusätzliche Buttons, die in der Symbolleiste angezeigt werden.
	 * @return	Optionale zusätzliche Buttons (kann <code>null</code> oder leer sein)
	 */
	JButton[] getAdditionalButton();

	/**
	 * Namen für einen zusätzliche Konfigurationeinträge in der Symbolleiste
	 * @return	Namen für Menüpunkte, um eine Konfigurationen zu dieser Statistik-Ansicht aufzurufen (wird hier <code>null</code> zurückgegeben, so wird kein zusätzlicher Konfigurationsmenüpunkt angezeigt)
	 */
	String[] ownSettingsName();

	/**
	 * Icons für die zusätzlichen Konfigurationeinträge in der Symbolleiste
	 * @return	Icons für Menüpunkte, um eine Konfiguration zu dieser Statistik-Ansicht aufzurufen (wird hier <code>null</code> zurückgegeben, so wird kein Icon angezeigt)
	 */
	Icon[] ownSettingsIcon();

	/**
	 * Wurde in {@link #ownSettingsName()} ein Konfigurationsmenüpunktname definiert, so wird diese Methode aufgerufen, wenn dieser Menüpunkt angeklickt wurde
	 * @param owner	Übergeordnetes Element
	 * @param nr	Ausgewählter Menüpunkt (0-basierend)
	 * @return	Gibt <code>true</code> zurück, wenn die Konfiguration erfolgreich verändert wurde
	 * @see #ownSettingsName()
	 */
	boolean ownSettings(StatisticsBasePanel owner, final int nr);

	/**
	 * Liefert das übergeordnete {@link StatisticsBasePanel} (welches z.B. von {@link #ownSettings(StatisticsBasePanel, int)}) verwendet wird.
	 * @param viewer	Viewer von dem ausgehend das Panel ermittelt werden soll
	 * @return	Liefert im Erfolgsfall das übergeordnete {@link StatisticsBasePanel}, sonst <code>null</code>
	 */
	default StatisticsBasePanel getParentStatisticPanel(final Component viewer) {
		Container c=viewer.getParent();
		while (c!=null && !(c instanceof StatisticsBasePanel)) c=c.getParent();
		return (StatisticsBasePanel)c;
	}

	/**
	 * Fügt die Einstellungen aus {@link #ownSettingsName()} usw. in ein Popupmenü ein.
	 * @param owner	Übergeordnetes Element (für {@link #ownSettings(StatisticsBasePanel, int)})
	 * @param menu	Popupmenü, an das die Einträge angehängt werden sollen
	 * @return	Liefert <code>true</code>, wenn Einträge generiert wurden
	 */
	default boolean addOwnSettingsToPopup(final StatisticsBasePanel owner, final JPopupMenu menu) {
		if (menu==null) return false;

		boolean first=true;
		final String[] settingsNames=ownSettingsName();
		final Icon[] settingsIcons=ownSettingsIcon();
		if (settingsNames!=null) for (int j=0;j<settingsNames.length;j++) {
			final String settingsName=settingsNames[j];
			if (settingsName==null) continue;
			if (first) {
				final JMenuItem label=new JMenuItem("<html><body><b>"+StatisticsBasePanel.viewersToolbarSettings+"</b></body></html>");
				label.setEnabled(false);
				menu.add(label);
				first=false;
			}
			final Icon settingsIcon=(settingsIcons==null || settingsIcons.length<=j)?null:settingsIcons[j];
			final JMenuItem item=new JMenuItem(settingsName,settingsIcon);
			menu.add(item);
			final int nr=j;
			item.addActionListener(e->ownSettings(owner,nr));
		}

		return !first;
	}

	/**
	 * Liefert dem Viewer ein Callback über das er die Größe zum Speichern von Bildern erfragen kann.
	 * @param getImageSize	Callback zum Erfragen der Größe zum Speichern von Bildern
	 */
	void setRequestImageSize(final IntSupplier getImageSize);

	/**
	 * Liefert dem Viewer ein Callback über das er die Größe zum Speichern von Bildern verändern kann.
	 * @param setImageSize	Callback zum Einstellen der Größe zum Speichern von Bildern
	 */
	void setUpdateImageSize(final IntConsumer setImageSize);

	/**
	 * Liefert dem Viewer ein Callback über das er die Einstellungen zu den Diagrammen erfragt kann.
	 * @param getChartSetup	Callback zum Erfragen der Einstellungen zu den Diagrammen
	 */
	void setRequestChartSetup(final Supplier<ChartSetup> getChartSetup);

	/**
	 * Liefert dem Viewer ein Callback über das er die Einstellungen zu den Diagrammen verändern kann.
	 * @param setChartSetup	Callback zum Einstellen der Einstellungen zu den Diagrammen
	 */
	void setUpdateChartSetup(final Consumer<ChartSetup> setChartSetup);

	/**
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	boolean hasOwnFileDropListener();
}
