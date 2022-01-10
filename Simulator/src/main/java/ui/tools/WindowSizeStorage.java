/**
 * Copyright 2021 Alexander Herzog
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
package ui.tools;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import tools.SetupData;

/**
 * Diese Klasse erm�glicht das Speichern und Wiederherstellen von Fensterpositionen und -gr��en.
 * Die Fenster werden dabei anhand ihrer Klassen sowie eines zus�tzlichen Keys unterschieden.<br>
 * Diese Klasse stellt nur statische Methoden zur Verf�gung und kann nicht instanziert werden.
 * @author Alexander Herzog
 */
public class WindowSizeStorage {
	/**
	 * Zuordnung zur Speicherung der Fensterpositionen und -gr��en in Abh�ngigkeit vom
	 * Klassennamen sowie eines weiteren, frei w�hlbaren Schl�ssels
	 * @see #getRecord(Window, String)
	 */
	private static Map<String,Map<String,SizeData>> map;

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse stellt nur statische Methoden zur Verf�gung und kann nicht instanziert werden.
	 */
	private WindowSizeStorage() {
	}

	/**
	 * Liefert einen Position+Gr��e-Datensatz f�r ein Fenster
	 * @param window	Fenster (von dem der Klassenname abgeleitet wird)
	 * @param key	Zus�tzlicher Schl�ssel zur Identifikation des Datensatzes (darf nicht <code>null</code> sein)
	 * @return	Position+Gr��e-Datensatz (ist nie <code>null</code>, kann aber 0-Werte f�r Gr��e und Position enthalten)
	 */
	private static SizeData getRecord(final Window window, final String key) {
		if (map==null) map=new HashMap<>();
		final String className=window.getClass().getName();
		Map<String,SizeData> subMap=map.get(className);
		if (subMap==null) map.put(className,subMap=new HashMap<>());
		SizeData sizeData=subMap.get(key);
		if (sizeData==null) subMap.put(key,sizeData=new SizeData());
		return sizeData;
	}

	/**
	 * Stellt Gr��e und Position eines Fensters wieder her und registriert es f�r die Speicherung dieser Daten bei Ver�nderungen.
	 * @param window	Fenster
	 * @param key	Zus�tzlicher Schl�ssel zur Identifikation des Datensatzes (darf nicht <code>null</code> sein)
	 * @param restoreSize	Auch die Gr��e wiederherstellen? (Bei <code>false</code> wird nur die Position wiederhergestellt.)
	 */
	public static void window(final Window window, final String key, final boolean restoreSize) {
		if (window==null) return;

		if (!SetupData.getSetup().restoreSubEditWindowSize) return;

		final SizeData sizeData=getRecord(window,key);
		SwingUtilities.invokeLater(()->{
			sizeData.restoreSize(window,restoreSize);
			window.addComponentListener(new ComponentAdapter() {
				@Override public void componentMoved(final ComponentEvent componentEvent) {sizeData.saveSize(window);}
				@Override public void componentResized(final ComponentEvent componentEvent) {sizeData.saveSize(window);}
			});
		});
	}

	/**
	 * Stellt Gr��e und Position eines Fensters wieder her und registriert es f�r die Speicherung dieser Daten bei Ver�nderungen.
	 * @param window	Fenster
	 * @param key	Zus�tzlicher Schl�ssel zur Identifikation des Datensatzes (darf nicht <code>null</code> sein)
	 */
	public static void window(final Window window, final String key) {
		window(window,key,true);
	}

	/**
	 * Diese Klasse h�lt die Position- und Gr��edaten
	 * f�r ein einzelnes Fenster vor.
	 * @see WindowSizeStorage#getRecord(Window, String)
	 */
	private static class SizeData {
		/**
		 * Gespeicherte Fensterposition
		 */
		private final Point location;

		/**
		 * Gespeicherte Fenstergr��e
		 */
		private final Dimension size;

		/**
		 * Konstruktor der Klasse
		 */
		public SizeData() {
			location=new Point();
			size=new Dimension();
		}

		/**
		 * Speichert Position und Gr��e eines Fensters in diesem Objekt
		 * @param window	Fenster dessen Daten gespeichert werden sollen
		 */
		public void saveSize(final Window window) {
			if (window==null) return;
			final Point location=window.getLocation();
			final Dimension size=window.getSize();
			this.location.x=location.x;
			this.location.y=location.y;
			this.size.width=size.width;
			this.size.height=size.height;
		}

		/**
		 * Stellt Position und Gr��e eines Fensters gem�� den in diesem
		 * Objekt gespeicherten Daten wieder her. Sind in dem Objekt noch
		 * keine Daten gespeichert, so erfolgt keine weitere Verarbeitung.
		 * @param window	Fenster dessen Einstellungen ver�ndert werden sollen
		 * @param restoreSize	Auch die Gr��e wiederherstellen? (Bei <code>false</code> wird nur die Position wiederhergestellt.)
		 */
		public void restoreSize(final Window window, final boolean restoreSize) {
			if (window==null) return;
			if (size.width<=0 || size.height<=0) return;
			window.setLocation(location);
			final Dimension minSize=window.getMinimumSize();
			if (restoreSize) {
				if (minSize!=null) {
					minSize.width=Math.min(minSize.width,size.width);
					minSize.height=Math.min(minSize.height,size.height);
					window.setMinimumSize(minSize);
				}
				window.setSize(size);
			}
		}
	}
}
