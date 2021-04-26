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
package mathtools.distribution.tools;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.JTextField;

/**
 * Ermöglicht es Datei-Drag&amp;Drop-Operationen über einen <code>ActionListener</code> zu verarbeiten.
 * @author Alexander Herzog
 * @see FileDropperData
 * @version 1.2
 */
public class FileDropper {
	/**
	 * <code>ActionListener</code> der bei einem Drop-Ereignis aufgerufen werden soll. Der <code>ActionListener</code> kann dann über die <code>dragDropFile</code> Methode des <code>FileDropper</code>-Objekts den Dateinamen abfragen
	 */
	private final ActionListener actionListener;

	/**
	 * An die Zielkomponente angebundene Drop-Listener
	 */
	private final FileDropListener[] listener;

	/**
	 * Wurde die Drag&amp;drop-Operation vollständig abgearbeitet?
	 * @see #dragDropConsumed()
	 */
	private boolean dragDropConsumed=false;

	/**
	 * Intern verwendete Drag&amp;drop-Zielkomponenten
	 * @see DropTarget
	 */
	private final DropTarget[] target;

	/**
	 * Komponenten, auf denen Dateien per Drag&amp;Drop abgelegt werden können sollen
	 */
	private final Component[] components;

	/**
	 * Konstruktor der Klasse
	 * @param comp	Komponente, auf der Dateien per Drag&amp;Drop abgelegt werden können sollen
	 * @param actionListener	<code>ActionListener</code> der bei einem Drop-Ereignis aufgerufen werden soll. Der <code>ActionListener</code> kann dann über die <code>dragDropFile</code> Methode des <code>FileDropper</code>-Objekts den Dateinamen abfragen
	 */
	public FileDropper(Component comp, ActionListener actionListener) {
		this(new Component[]{comp},actionListener);
	}

	/**
	 * Konstruktor der Klasse
	 * @param comp	Komponenten, auf denen Dateien per Drag&amp;Drop abgelegt werden können sollen
	 * @param actionListener	<code>ActionListener</code> der bei einem Drop-Ereignis aufgerufen werden soll. Der <code>ActionListener</code> kann dann über die <code>dragDropFile</code> Methode des <code>FileDropper</code>-Objekts den Dateinamen abfragen
	 */
	public FileDropper(Component[] comp, ActionListener actionListener) {
		this.actionListener=actionListener;

		if (GraphicsEnvironment.isHeadless()) {
			listener=new FileDropListener[0];
			target=new DropTarget[0];
			components=new Component[0];
		} else {
			listener=new FileDropListener[comp.length];
			target=new DropTarget[comp.length];
			for (int i=0;i<comp.length;i++) target[i]=new DropTarget(comp[i],listener[i]=new FileDropListener(comp[i]));
			components=comp;
		}
	}

	/**
	 * Liefert die Komponenten, auf die sich das Drag&amp;Drop-Objekt bezieht.
	 * @return	Array mit den Komponenten, die auf Drag&amp;Drop reagieren sollen.
	 */
	public Component[] getComponents() {
		return components;
	}

	/**
	 * Beendet das Akzeptieren von Drag&amp;Drop-Operationen auf die angegebene Komponente.
	 */
	public void quit() {
		for (int i=0;i<target.length;i++) {
			target[i].removeDropTargetListener(listener[i]);
			target[i].setActive(false);
			target[i]=null;
		}
	}

	/**
	 * Wurden mehrere Dateien auf der Komponente abgelegt, aber sollen nach der momentan per <code>dragDropFile</code> abgefragten Datei keine weiteren mehr
	 * abgerufen werden (für jede Datei wird jeweils der <code>ActionListener</code> aufgerufen), so kann dies durch den Aufruf dieser
	 * Methode signalisiert werden.
	 */
	public void dragDropConsumed() {
		dragDropConsumed=true;
	}

	/**
	 * Führt die Verarbeitung einer Drag&amp;drop-Operation aus
	 * @param file	Abgelegte Datei
	 * @param dropComponent	Zielkomponente
	 * @param dropPosition	Position auf der Zielkomponente
	 * @return	Wurde die Operation vollständig verarbeitet?
	 * @see #dragDropConsumed()
	 */
	private boolean dropFile(final File file, final Component dropComponent, final Point dropPosition) {
		actionListener.actionPerformed(FileDropperData.getActionEvent(this,file,dropComponent,dropPosition));

		if (dragDropConsumed) {
			dragDropConsumed=false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Interne Klasse um auf Drag&amp;drop-Ereignisse reagieren zu können.
	 */
	private class FileDropListener implements DropTargetListener {
		/** Komponenten, auf denen Dateien per Drag&amp;Drop abgelegt werden können sollen */
		private final Component component;

		/**
		 * Konstruktor der Klasse
		 * @param component	Komponenten, auf denen Dateien per Drag&amp;Drop abgelegt werden können sollen
		 */
		public FileDropListener(final Component component) {
			this.component=component;
		}

		@Override
		public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);}

		@Override
		public void dragExit(DropTargetEvent dropTargetDragEvent) {}

		@Override
		public void dragOver(DropTargetDragEvent dropTargetDragEvent) {}

		@Override
		public void drop(DropTargetDropEvent dropTargetDropEvent) {
			try {
				Transferable tr=dropTargetDropEvent.getTransferable();
				if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
					dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					final Object obj=tr.getTransferData(DataFlavor.javaFileListFlavor);
					if (obj instanceof List) for (Object entry: ((List<?>)obj)) if (entry instanceof File) {
						if (dropFile((File)entry,component,dropTargetDropEvent.getLocation())) break;
					}
					dropTargetDropEvent.getDropTargetContext().dropComplete(true);
				} else {
					dropTargetDropEvent.rejectDrop();
				}
			} catch (IOException | UnsupportedFlavorException e) {
				dropTargetDropEvent.rejectDrop();
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {}
	}

	/**
	 * Statische Hilfsroutine, die für eine Komponente eine {@link File}-Dropper-Funktion registriert.
	 * @param comp	Komponente auf die Dateien und Verzeichnisse abgelegt werden können sollen
	 * @param drop	Methode, die bei einem Drop-Ereignis ausgeführt werden soll
	 */
	public static void add(final Component comp, final Function<File,Boolean> drop) {
		new FileDropper(comp,e->{
			final FileDropperData data=(FileDropperData)e.getSource();
			if (drop.apply(data.getFile())) data.dragDropConsumed();
		});
	}

	/**
	 * Fügt einen Dateinamen in ein Eingabefeld ein, wenn eine Datei auf eine Komponente abgelegt wird.
	 * @param comp	Komponente auf die Dateien abgelegt werden können sollen
	 * @param input	Eingabefeld in das der Dateiname eingefügt werden soll
	 * @param action	Optionale zusätzliche Methode, die bei einem Drop-Ereignis ausgeführt werden soll (kann <code>null</code> sein)
	 */
	public static void addFileDropper(final Component comp, final JTextField input, final Runnable action) {
		final Function<File,Boolean> drop=file->{
			if (file==null || !file.isFile()) return false;
			input.setText(file.toString());
			Arrays.asList(input.getKeyListeners()).forEach(a->{
				final KeyEvent e=new KeyEvent(input,ActionEvent.ACTION_PERFORMED,System.currentTimeMillis(),0,KeyEvent.VK_LEFT,KeyEvent.CHAR_UNDEFINED);
				a.keyPressed(e);
				a.keyReleased(e);
				a.keyTyped(e);
			});
			Arrays.asList(input.getActionListeners()).forEach(a->a.actionPerformed(new ActionEvent(input,ActionEvent.ACTION_PERFORMED,null)));
			if (action!=null) action.run();
			return true;
		};

		add(comp,drop);
		add(input,drop);
	}

	/**
	 * Fügt einen Dateinamen in ein Eingabefeld ein, wenn eine Datei auf eine Komponente abgelegt wird.
	 * @param comp	Komponente auf die Dateien abgelegt werden können sollen
	 * @param input	Eingabefeld in das der Dateiname eingefügt werden soll
	 */
	public static void addFileDropper(final Component comp, final JTextField input) {
		addFileDropper(comp,input,null);
	}

	/**
	 * Fügt einen Verzeichnisnamen in ein Eingabefeld ein, wenn ein Verzeichnis auf eine Komponente abgelegt wird.
	 * @param comp	Komponente auf die Verzeichnisse abgelegt werden können sollen
	 * @param input	Eingabefeld in das der Verzeichnisname eingefügt werden soll
	 * @param action	Optionale zusätzliche Methode, die bei einem Drop-Ereignis ausgeführt werden soll (kann <code>null</code> sein)
	 */
	public static void addDirectoryDropper(final Component comp, final JTextField input, final Runnable action) {
		final Function<File,Boolean> drop=file->{
			if (file==null || !file.isDirectory()) return false;
			input.setText(file.toString());
			Arrays.asList(input.getKeyListeners()).forEach(a->{
				final KeyEvent e=new KeyEvent(input,ActionEvent.ACTION_PERFORMED,System.currentTimeMillis(),0,KeyEvent.VK_LEFT,KeyEvent.CHAR_UNDEFINED);
				a.keyPressed(e);
				a.keyReleased(e);
				a.keyTyped(e);
			});
			Arrays.asList(input.getActionListeners()).forEach(a->a.actionPerformed(new ActionEvent(input,ActionEvent.ACTION_PERFORMED,null)));
			if (action!=null) action.run();
			return true;
		};

		add(comp,drop);
		add(input,drop);
	}

	/**
	 * Fügt einen Verzeichnisnamen in ein Eingabefeld ein, wenn ein Verzeichnis auf eine Komponente abgelegt wird.
	 * @param comp	Komponente auf die Verzeichnisse abgelegt werden können sollen
	 * @param input	Eingabefeld in das der Verzeichnisname eingefügt werden soll
	 */
	public static void addDirectoryDropper(final Component comp, final JTextField input) {
		addDirectoryDropper(comp,input,null);
	}
}