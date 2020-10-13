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
package ui.mjpeg;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import language.Language;

/**
 * Zeit einen Wartedialog an, der auf das Beenden des Speicherns der Videodaten wartet
 * @author Alexander Herzog
 * @see MJPEGSystem
 */

public class AnimationRecordWaitDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -728835664535448688L;

	/** MJPEG-System bei dem die Speicherung im Hintergrund durch Aufruf von <code>done()</code> gestartet wurde. */
	private final MJPEGSystem mjpeg;
	/** Fortschritt der Speicherung */
	private final JProgressBar progress;
	/** Timer der in regelm��igen Abst�nden {@link #progress} aktualisiert und am Ende den Dialog schlie�t */
	private final Timer timer;

	/**
	 * Konstruktor der Klasse <code>AnimationRecordWaitDialog</code>
	 * @param owner	�bergeordnetes Element
	 * @param mjpeg	MJPEG-System bei dem die Speicherung im Hintergrund durch Aufruf von <code>done()</code> gestartet wurde.
	 */
	public AnimationRecordWaitDialog(final Container owner, final MJPEGSystem mjpeg) {
		super(getOwnerWindow(owner),Language.tr("Animation.CreatingVideo"));
		this.mjpeg=mjpeg;

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		Container container=getContentPane();
		container.setLayout(new BorderLayout());
		container.add(progress=new JProgressBar(SwingConstants.HORIZONTAL));
		progress.setMinimum(0);
		progress.setStringPainted(true);

		timer=new Timer("MJPEGStoreProgress",false);
		timer.schedule(new UpdateInfoTask(),100,100);

		setMinimumSize(new Dimension(400,50));
		pack();
		setLocationRelativeTo(getOwnerWindow(owner));
		setVisible(true);
	}

	/**
	 * Liefert das �bergeordnete Fenster eines {@link Container}-Elements.
	 * @param owner	Element f�r das das �bergeordnete Fenster gesucht werden soll
	 * @return	�bergeordnetes Fenster oder <code>null</code>, wenn kein �bergeordnetes Fenster ermittelt werden konnte
	 */
	private static JFrame getOwnerWindow(Container owner) {
		while (owner!=null && !(owner instanceof JFrame)) owner=owner.getParent();
		return (JFrame)owner;
	}

	/**
	 * Aktualisiert die Fortschrittsanzeige in regelm��igen Abst�nden
	 * @see AnimationRecordWaitDialog#timer
	 */
	private class UpdateInfoTask extends TimerTask {
		@Override
		public void run() {
			if (mjpeg.getFramesToWrite()==0) return;
			progress.setMaximum(mjpeg.getFramesToWrite());
			if (mjpeg.getFramesWritten()>=0) {
				progress.setValue(mjpeg.getFramesWritten());
			} else {
				timer.cancel();
				setVisible(false);
			}
		}
	}
}
