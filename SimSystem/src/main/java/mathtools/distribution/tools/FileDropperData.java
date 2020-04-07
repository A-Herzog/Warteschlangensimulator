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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Ein Objekt dieser Klasse wird als Sender verwendet, wenn eine Datei mit Hilfe
 * eines {@link FileDropper}-Objektes per Drag&amp;Drop empfangen wird.
 * @author Alexander Herzog
 * @see FileDropper
 */
public class FileDropperData {
	private final FileDropper fileDropper;
	private final File file;
	private final Component dropComponent;
	private final Point dropPosition;

	/**
	 * Konstruktor der Klasse
	 * @param fileDropper	Zugehöriges {@link FileDropper}-Objekt (kann <code>null</code> sein). Kann vom Empfänger dieses Objektes verwendet werden, um die Drag&amp;Drop-Operation abzuschließen und um zu ermitteln, von welchem Objekt das Ereignis ausging.
	 * @param file	Abgelegte Datei
	 * @param dropComponent	Komponente auf der die Datei abgelegt wurde
	 * @param dropPosition	Position (relativ zu der Komponente) an der die Datei abgelegt wurde
	 */
	public FileDropperData(final FileDropper fileDropper, final File file, final Component dropComponent, final Point dropPosition) {
		this.fileDropper=fileDropper;
		this.file=file;
		this.dropComponent=dropComponent;
		this.dropPosition=dropPosition;
	}

	/**
	 * Liefert das {@link FileDropper}-Objekt, von dem die Operation ausgeht
	 * @return	{@link FileDropper}-Objekt, von dem die Operation ausgeht (kann <code>null</code> sein)
	 */
	public FileDropper getFileDropper() {
		return fileDropper;
	}

	/**
	 * Liefert die abgelegte Datei
	 * @return	Abgelegte Datei
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Liefert die Komponente auf der die Datei abgelegt wurde.
	 * @return	Komponente auf der die Datei abgelegt wurde
	 */
	public Component getDropComponent() {
		return dropComponent;
	}

	/**
	 * Liefert die Position (relativ zu der Komponente) an der die Datei abgelegt wurde.
	 * @return	Position (relativ zu der Komponente) an der die Datei abgelegt wurde
	 * @see FileDropperData#getDropComponent()
	 */
	public Point getDropPosition() {
		return dropPosition;
	}

	/**
	 * Erklärt die Operation für abgeschlossen.
	 * @see FileDropper#dragDropConsumed()
	 */
	public void dragDropConsumed() {
		if (fileDropper!=null) fileDropper.dragDropConsumed();
	}

	/**
	 * Legt ein {@link ActionEvent} an, welches als Sender ein Objekt vom Typ {@link FileDropperData} erhält
	 * @param fileDropper	Zugehöriges {@link FileDropper}-Objekt (kann <code>null</code> sein). Kann vom Empfänger dieses Objektes verwendet werden, um die Drag&amp;Drop-Operation abzuschließen und um zu ermitteln, von welchem Objekt das Ereignis ausging.
	 * @param file	Abgelegte Datei
	 * @param dropComponent	Komponente auf der die Datei abgelegt wurde
	 * @param dropPosition	Position (relativ zu der Komponente) an der die Datei abgelegt wurde
	 * @return	{@link ActionEvent}, welches an entsprechende Eventlistener geleitet werden kann
	 */
	public static ActionEvent getActionEvent(final FileDropper fileDropper, final File file, final Component dropComponent, final Point dropPosition) {
		return getActionEvent(new FileDropperData(fileDropper,file,dropComponent,dropPosition));
	}

	/**
	 * Legt ein {@link ActionEvent} an, welches als Sender ein Objekt vom Typ {@link FileDropperData} erhält
	 * @param fileDropperData	Als Sender zu verwendendes 	{@link FileDropperData}-Objekt
	 * @return	{@link ActionEvent}, welches an entsprechende Eventlistener geleitet werden kann
	 */
	public static ActionEvent getActionEvent(final FileDropperData fileDropperData) {
		return new ActionEvent(fileDropperData,AWTEvent.RESERVED_ID_MAX+1,"FileDropped");
	}
}
