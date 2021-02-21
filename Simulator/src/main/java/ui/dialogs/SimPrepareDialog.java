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
 * @see #workString(Component, Supplier)
 * @see #workObject(Component, Supplier)
 */
public class SimPrepareDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2337255602031599538L;

	/**
	 * Nach einer wie langen Laufzeit des Model-Builder-Threads
	 * soll der "Bitte warten"-Dialog angezeigt werden?
	 */
	private static final int MS_BEFORE_SHOW_DIALOG=500;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param thread	Thread auf dessen Ende gewartet werden soll
	 */
	private SimPrepareDialog(final Component owner, final Thread thread) {
		super((owner instanceof Window)?((Window)owner):SwingUtilities.windowForComponent(owner),MainFrame.PROGRAM_NAME,Dialog.ModalityType.DOCUMENT_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

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
		line.add(label=new JLabel("<html><body style=\"margin-left: 20px;\">"+"<b>"+Language.tr("SimPrepare.Preparing")+"</b><br>"+Language.tr("SimPrepare.PleaseWait")+"</body></html>"));
		final URL[] urls=Images.EXTRAS_QUEUE.getURLs();
		label.setIcon(new ImageIcon(urls[urls.length-1]));
		label.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));

		/* Fenster vorbereiten */
		setMinimumSize(new Dimension(300,0));
		pack();
		setLocationRelativeTo(this.getOwner());
		SwingUtilities.invokeLater(()->{
			try {thread.join();} catch (InterruptedException e) {}
			setVisible(false);
		});
		setVisible(true);
	}

	/**
	 * Führt eine bestimmte Methode, die einen String zurückliefert,
	 * aus und zeigt, wenn die Ausführung länger dauert, einen
	 * "Bitte warten"-Dialog an.
	 * @param owner	Übergeordnetes Element
	 * @param worker	Auszuführende Methode
	 * @return	Rückgabewert der Methode
	 */
	public static String workString(final Component owner, final Supplier<String> worker) {
		final WorkThread<String> thread=new WorkThread<>(worker);
		thread.start();

		try {thread.join(MS_BEFORE_SHOW_DIALOG);} catch (InterruptedException e) {}
		if (!thread.isAlive()) return thread.result;

		new SimPrepareDialog(owner,thread);
		return thread.result;
	}

	/**
	 * Führt eine bestimmte Methode, die ein Objekt zurückliefert,
	 * aus und zeigt, wenn die Ausführung länger dauert, einen
	 * "Bitte warten"-Dialog an.
	 * @param owner	Übergeordnetes Element
	 * @param worker	Auszuführende Methode
	 * @return	Rückgabewert der Methode
	 */
	public static Object workObject(final Component owner, final Supplier<Object> worker) {
		final WorkThread<Object> thread=new WorkThread<>(worker);
		thread.start();

		try {thread.join(MS_BEFORE_SHOW_DIALOG);} catch (InterruptedException e) {}
		if (!thread.isAlive()) return thread.result;

		new SimPrepareDialog(owner,thread);
		return thread.result;
	}

	/**
	 * Hintergrund-Thread in dem die in {@link SimPrepareDialog#workString(Component, Supplier)}
	 * übergebene Methode ausgeführt wird.
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
		public T result;

		/**
		 * Konstruktor der Klasse
		 * @param worker	Auszuführende Methode
		 */
		public WorkThread(final Supplier<T> worker) {
			super("ModelPrepare");
			this.worker=worker;
		}

		@Override public void run() {
			result=worker.get();
		}
	}
}
