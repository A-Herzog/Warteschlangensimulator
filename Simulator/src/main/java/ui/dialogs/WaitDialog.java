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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.Serializable;
import java.net.URL;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import language.Language;
import ui.MainFrame;
import ui.images.Images;

/**
 * Führt eine Methode, die einen String oder ein Objekt zurückliefert, aus und zeigt,
 * wenn die Ausführung länger dauert, einen "Bitte warten"-Dialog an.
 * @author Alexander Herzog
 * @see #workString(Component, Supplier, Mode)
 * @see #workObject(Component, Supplier, Mode)
 * @see #workBytes(Component, Supplier, Mode)
 */
public class WaitDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2337255602031599538L;

	/**
	 * In dem "Bitte warten"-Dialog anzuzeigender Info-Text
	 */
	public enum Mode {
		/** Das Modell wird vorbereitet. */
		MODEL_PREPARE,
		/** Eine Datei wird heruntergeladen. */
		DOWNLOAD_FILE,
		/** Daten werden verarbeitet. */
		PROCESS_DATA,
		/** Daten werden geladen. */
		LOAD_DATA,
		/** Daten werden gespeichert. */
		SAVE_DATA
	}

	/**
	 * Nach einer wie langen Laufzeit des Model-Builder-Threads
	 * soll der "Bitte warten"-Dialog angezeigt werden?
	 */
	private static final int MS_BEFORE_SHOW_DIALOG=500;

	/**
	 * Thread auf dessen Ende gewartet werden soll
	 */
	private final Thread thread;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param thread	Thread auf dessen Ende gewartet werden soll
	 * @param mode	Anzuzeigender Info-Text
	 */
	private WaitDialog(final Component owner, final Thread thread, final Mode mode) {
		super((owner instanceof Window)?((Window)owner):SwingUtilities.windowForComponent(owner),MainFrame.PROGRAM_NAME,Dialog.ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.thread=thread;

		/* Konfiguration des Dialogs */
		final String infoText;
		final Images image;
		switch (mode) {
		case MODEL_PREPARE:
			infoText=Language.tr("SimPrepare.Preparing");
			image=Images.EXTRAS_QUEUE;
			break;
		case DOWNLOAD_FILE:
			infoText=Language.tr("SimPrepare.Downloading");
			image=Images.HELP_HOMEPAGE;
			break;
		case PROCESS_DATA:
			infoText=Language.tr("SimPrepare.ProcessData");
			image=Images.EXTRAS_QUEUE;
			break;
		case LOAD_DATA:
			infoText=Language.tr("SimPrepare.LoadData");
			image=Images.GENERAL_LOAD;
			break;
		case SAVE_DATA:
			infoText=Language.tr("SimPrepare.SaveData");
			image=Images.GENERAL_SAVE;
			break;
		default:
			infoText=Language.tr("SimPrepare.Preparing");
			image=Images.EXTRAS_QUEUE;
			break;
		}

		/* GUI */
		final Container content=getContentPane();
		content.setLayout(new BorderLayout());
		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));

		/* Infozeile */
		JPanel line;
		JLabel label;
		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel("<html><body style=\"margin-left: 20px;\">"+"<b>"+infoText+"</b><br>"+Language.tr("SimPrepare.PleaseWait")+"</body></html>"));
		final URL[] urls=image.getURLs();
		label.setIcon(new ImageIcon(urls[urls.length-1]));
		label.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));

		/* Fenster vorbereiten */
		setMinimumSize(new Dimension(300,0));
		pack();
		setLocationRelativeTo(this.getOwner());
		SwingUtilities.invokeLater(()->closeTest());
		setVisible(true);
	}

	/**
	 * Prüft, ob {@link #thread} mittlerweile beendet wurde
	 * und schließt dann den Dialog. Ansonsten wartet die Methode
	 * etwas und ruft sich dann über den AWT-Thread erneut auf
	 * (so dass auch andere GUI-Ereignisse abgearbeitet werden können).
	 */
	private void closeTest() {
		if (!thread.isAlive()) {
			setVisible(false);
			return;
		}

		try {thread.join(50);} catch (InterruptedException e) {}

		if (!thread.isAlive()) {
			setVisible(false);
			return;
		}

		SwingUtilities.invokeLater(()->closeTest());
	}

	/**
	 * Führt eine bestimmte Methode, die einen String zurückliefert,
	 * aus und zeigt, wenn die Ausführung länger dauert, einen
	 * "Bitte warten"-Dialog an.
	 * @param owner	Übergeordnetes Element
	 * @param worker	Auszuführende Methode
	 * @param mode	Anzuzeigender Info-Text in dem Dialog
	 * @return	Rückgabewert der Methode
	 */
	public static String workString(final Component owner, final Supplier<String> worker, final Mode mode) {
		return new WorkThread<>(worker).startAndWait(owner,mode);
	}

	/**
	 * Führt eine bestimmte Methode, die ein Objekt zurückliefert,
	 * aus und zeigt, wenn die Ausführung länger dauert, einen
	 * "Bitte warten"-Dialog an.
	 * @param owner	Übergeordnetes Element
	 * @param worker	Auszuführende Methode
	 * @param mode	Anzuzeigender Info-Text in dem Dialog
	 * @return	Rückgabewert der Methode
	 */
	public static Object workObject(final Component owner, final Supplier<Object> worker, final Mode mode) {
		return new WorkThread<>(worker).startAndWait(owner,mode);
	}

	/**
	 * Führt eine bestimmte Methode, die ein Objekt zurückliefert,
	 * aus und zeigt, wenn die Ausführung länger dauert, einen
	 * "Bitte warten"-Dialog an.
	 * @param owner	Übergeordnetes Element
	 * @param worker	Auszuführende Methode
	 * @param mode	Anzuzeigender Info-Text in dem Dialog
	 * @return	Rückgabewert der Methode
	 */
	public static byte[] workBytes(final Component owner, final Supplier<byte[]> worker, final Mode mode) {
		return new WorkThread<>(worker).startAndWait(owner,mode);
	}

	/**
	 * Führt eine bestimmte Methode, die einen boolschen Wert zurückliefert,
	 * aus und zeigt, wenn die Ausführung länger dauert, einen
	 * "Bitte warten"-Dialog an.
	 * @param owner	Übergeordnetes Element
	 * @param worker	Auszuführende Methode
	 * @param mode	Anzuzeigender Info-Text in dem Dialog
	 * @return	Rückgabewert der Methode
	 */
	public static boolean workBoolean(final Component owner, final BooleanSupplier worker, final Mode mode) {
		return new WorkThread<>(()->worker.getAsBoolean()).startAndWait(owner,mode);
	}

	/**
	 * Hintergrund-Thread in dem die übergebene Methode ausgeführt wird.
	 * @param <T> Rückgabewert der auszuführenden Methode
	 */
	private static class WorkThread<T> extends Thread {

		/**
		 * Auszuführende Methode
		 */
		private final Supplier<T> worker;

		/**
		 * Rückgabewert der Methode
		 */
		private T result;

		/**
		 * Konstruktor der Klasse
		 * @param worker	Auszuführende Methode
		 */
		public WorkThread(final Supplier<T> worker) {
			super("ModelPrepareWaitDialog");
			this.worker=worker;
		}

		@Override
		public void run() {
			result=worker.get();
		}

		/**
		 * Startet den Thread und wartet auch gleich auf dessen Abschluss.
		 * @param owner	Übergeordnetes Element
		 * @param mode	Anzuzeigender Info-Text in dem Dialog
		 * @return	Rückgabewert der Methode
		 */
		public T startAndWait(final Component owner, final Mode mode) {
			start();
			try {join(MS_BEFORE_SHOW_DIALOG);} catch (InterruptedException e) {}
			if (isAlive()) new WaitDialog(owner,this,mode);
			return result;
		}
	}
}
