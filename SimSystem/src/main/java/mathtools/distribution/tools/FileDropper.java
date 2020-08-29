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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Ermöglicht es Datei-Drag&amp;Drop-Operationen über einen <code>ActionListener</code> zu verarbeiten.
 * @author Alexander Herzog
 * @see FileDropperData
 * @version 1.1
 */
public class FileDropper {
	private final ActionListener actionListener;
	private final FileDropListener[] listener;
	private boolean dragDropConsumed=false;
	private final DropTarget[] target;
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

	private boolean dropFile(final File file, final Component dropComponent, final Point dropPosition) {
		actionListener.actionPerformed(FileDropperData.getActionEvent(this,file,dropComponent,dropPosition));
		if (dragDropConsumed) {dragDropConsumed=false; return true;} else return false;
	}

	private class FileDropListener implements DropTargetListener {
		private final Component component;

		public FileDropListener(final Component component) {
			super();
			this.component=component;
		}

		@Override
		public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);}

		@Override
		public void dragExit(DropTargetEvent dropTargetDragEvent) {}

		@Override
		public void dragOver(DropTargetDragEvent dropTargetDragEvent) {}

		@SuppressWarnings("unchecked")
		@Override
		public void drop(DropTargetDropEvent dropTargetDropEvent) {
			try {
				Transferable tr=dropTargetDropEvent.getTransferable();
				if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
					dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					final List<File> fileList=(List<File>)(tr.getTransferData(DataFlavor.javaFileListFlavor));
					final Iterator<File> iterator=fileList.iterator();
					while (iterator.hasNext()) if (dropFile(iterator.next(),component,dropTargetDropEvent.getLocation())) break;
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
}