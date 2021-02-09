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
package gitconnect;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.Serializable;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.eclipse.jgit.lib.EmptyProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;

import language.Language;
import systemtools.BaseDialog;
import ui.images.Images;

/**
 * Zeigt einen "Bitte warten"-Dialog für Git-Operationen an.
 * @author Alexander Herzog
 * @see GitDialogProcessMonitor#run(Component, String, Function)
 */
public abstract class GitDialogProcessMonitor extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5897225862059956389L;

	/**
	 * Erstellter und für weitere Anfragen gespeicherter {@link ProgressMonitor}.
	 * @see #getProgressMonitor()
	 * @see #buildProgressMonitor()
	 */
	private ProgressMonitor progressMonitor;

	/**
	 * Label-Element in dem der jeweils aktuelle Arbeitsschritt angezeigt wird
	 */
	private JLabel label;

	/**
	 * Wurde die Abbrechen-Schaltfläche angeklickt?
	 * Wenn ja, wird diese Information über {@link #progressMonitor} an das Git-System weitergegeben.
	 */
	private boolean setCanceled;

	/**
	 * Rückgabe der internen Verarbeitung
	 * @see #work()
	 * @see #getResult()
	 */
	private Object result;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param title	Dialogtitel
	 */
	public GitDialogProcessMonitor(final Component owner, final String title) {
		super((owner instanceof Window)?((Window)owner):SwingUtilities.windowForComponent(owner),title,Dialog.ModalityType.DOCUMENT_MODAL);

		/* GUI vorbereiten */
		final Container content=getContentPane();
		content.setLayout(new BorderLayout());
		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));
		JPanel line;

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Git.System.PleaseWait")+"</b></body></html>"));

		top.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel());

		top.add(line=new JPanel(new FlowLayout(FlowLayout.CENTER)));
		final JButton button=new JButton(BaseDialog.buttonTitleCancel,Images.GENERAL_CANCEL.getIcon());
		line.add(button);
		button.addActionListener(e->setCanceled=true);
	}

	/**
	 * Startet die Verarbeitung.
	 */
	public void start() { /* Könnte eigentlich auch direkt im Konstruktor erfolgen, aber das mag SpotBugs nicht. Also muss das getrennt werden in der Hoffnung, das später niemand vergisst, start() auszuführen. */
		/* Verarbeitung starten */
		new Thread(()->{
			try {result=work();} finally {done();}
		},"GitBackgroundProcessing").start();

		/* Fenster als solches vorbereiten */
		setMinimumSize(new Dimension(500,0));
		pack();
		setLocationRelativeTo(this.getOwner());
		setVisible(true);
	}

	/**
	 * Methode in der die eigentliche Verarbeitung erfolgt.<br>
	 * Diese Methode wird in einem eigenen Thread aufgerufen.
	 * @return	Rückgabewert, den der Dialog nach Außen liefern soll
	 * @see #getResult()
	 */
	protected abstract Object work();

	/**
	 * Macht den Dialog wieder unsichtbar.<br>
	 * Wird nach dem Ende von {@link #work()} aufgerufen.
	 */
	private void done() {
		SwingUtilities.invokeLater(()->setVisible(false));
	}

	/**
	 * Erstellt ein neues {@link ProgressMonitor}-Objekt.
	 * @return	Neues {@link ProgressMonitor}-Objekt
	 */
	private ProgressMonitor buildProgressMonitor() {
		return new EmptyProgressMonitor() {
			@Override public void beginTask(String title, int totalWork) {SwingUtilities.invokeLater(()->{label.setText(title); pack();});}
			@Override public boolean isCancelled() {return setCanceled;}
		};
	}

	/**
	 * Liefert ein {@link ProgressMonitor}-Objekt
	 * (entweder neu erstellt oder dasselbe wie beim letzten Aufruf dieser Methode).
	 * @return	{@link ProgressMonitor}-Objekt
	 */
	protected ProgressMonitor getProgressMonitor() {
		if (progressMonitor==null) progressMonitor=buildProgressMonitor();
		return progressMonitor;
	}

	/**
	 * Liefert den Rückgabewert von {@link #work()}.
	 * @return	Rückgabewert von {@link #work()}
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * Zeigt den Wartedialog an, führt die Verarbeitung durch und blendet den Dialog dann wieder aus.
	 * @param owner	Übergeordnetes Element
	 * @param title	Dialogtitel
	 * @param process	Methode, die innerhalb des Dialogs in einem eigenen Thread ausgeführt werden soll
	 * @return	Rückgabewert der internen Verarbeitungsmethode
	 */
	public static Object run(final Component owner, final String title, Function<ProgressMonitor,Object> process) {
		final GitDialogProcessMonitor dialog=new GitDialogProcessMonitor(owner,title) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=3055066684773803484L;

			@Override
			protected Object work() {
				return process.apply(getProgressMonitor());
			}
		};
		dialog.start();
		return dialog.getResult();
	}
}
