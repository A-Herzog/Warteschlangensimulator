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
package ui.tutorial;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import simulator.editmodel.EditModel;
import ui.MainPanel;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;

/**
 * Diese nicht instanzierbare Klasse enth�lt ausschlie�lich statische Methoden,
 * um bestimmte Modelleigenschaften abzupr�fen, um festzustellen, ob der Nutzer
 * die im interaktiven Tutorial angezeigten Anweisengen befolgt hat.
 * @author Alexander Herzog
 * @see TutorialWindow
 */
public class TutorialTools {
	/**
	 * Konstruktor der Klasse <code>TutorialTools</code>.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 */
	private TutorialTools() {}

	private static ModelSurface surface=null;
	private static EditModel model=null;

	private static synchronized ModelSurface getSurface(final MainPanel mainPanel) {
		surface=null;
		try {
			SwingUtilities.invokeAndWait(()->{
				surface=mainPanel.editorPanel.getModel().surface;
			});
		} catch (InvocationTargetException | InterruptedException e) {return null;}
		return surface;
	}

	private static synchronized EditModel getModel(final MainPanel mainPanel) {
		model=null;
		try {
			SwingUtilities.invokeAndWait(()->{
				model=mainPanel.editorPanel.getModel();
			});
		} catch (InvocationTargetException | InterruptedException e) {return null;}
		return model;
	}

	/**
	 * Pr�ft, ob das Modell eine Quelle besitzt
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Gibt <code>true</code> zur�ck, wenn in dem Modell eine Element vom Typ <code>ModelElementSource</code> enthalten ist.
	 */
	public static boolean hasSource(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return false;
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementSource) return true;
		return false;
	}

	/**
	 * Pr�ft, ob das Modell eine Bedienstation besitzt
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Gibt <code>true</code> zur�ck, wenn in dem Modell eine Element vom Typ <code>ModelElementProcess</code> enthalten ist.
	 */
	public static boolean hasProcess(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return false;
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementProcess) return true;
		return false;
	}

	/**
	 * Pr�ft, ob das Modell einen Ausgang besitzt
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Gibt <code>true</code> zur�ck, wenn in dem Modell eine Element vom Typ <code>ModelElementDispose</code> enthalten ist.
	 */
	public static boolean hasDispose(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return false;
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementDispose) return true;
		return false;
	}

	/**
	 * Pr�ft, ob in dem Modell eine Quelle und eine Bedienstation vorhanden sind und ob diese �ber eine Kante verbunden sind
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Gibt <code>true</code> zur�ck, wenn Quelle und Bedienstation vorhanden sind und verbunden sind
	 */
	public static boolean connect1(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return false;

		ModelElementSource source=null;
		ModelElementProcess process=null;
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSource) source=(ModelElementSource)element;
			if (element instanceof ModelElementProcess) process=(ModelElementProcess)element;
		}
		if (source==null || process==null) return false;

		final ModelElementEdge edge=source.getEdgeOut();
		if (edge==null) return false;
		if (edge.getConnectionEnd()!=process) return false;

		return true;
	}

	/**
	 * Pr�ft, ob in dem Modell eine Bedienstation und ein Ausgang vorhanden sind und ob diese �ber eine Kante verbunden sind
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Gibt <code>true</code> zur�ck, wenn Bedienstation und Ausgang vorhanden sind und verbunden sind
	 */
	public static boolean connect2(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return false;

		ModelElementProcess process=null;
		ModelElementDispose dispose=null;
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementProcess) process=(ModelElementProcess)element;
			if (element instanceof ModelElementDispose) dispose=(ModelElementDispose)element;
		}
		if (process==null || dispose==null) return false;

		final ModelElementEdge edge=process.getEdgeOutSuccess();
		if (edge==null) return false;
		if (process.getEdgeOutCancel()!=null) return false;
		if (edge.getConnectionEnd()!=dispose) return false;

		return true;
	}

	/**
	 * Liefert das Quelle-Element des Modells zur�ck
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Quelle-Element des Modells oder <code>null</code>, wenn kein solches Element existiert
	 */
	public static ModelElementSource getSource(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return null;
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementSource) return (ModelElementSource)element;
		return null;
	}

	/**
	 * Liefert das Bedienstation-Element des Modells zur�ck
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Bedienstation-Element des Modells oder <code>null</code>, wenn kein solches Element existiert
	 */
	public static ModelElementProcess getProcess(final MainPanel mainPanel) {
		final ModelSurface surface=getSurface(mainPanel); if (surface==null) return null;
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementProcess) return (ModelElementProcess)element;
		return null;
	}

	/**
	 * Pr�ft, ob die angegebene Ressource existiert, aus genau einem Bediener besteht und f�r diesen keine Ausf�lle definiert sind.
	 * @param mainPanel	Programm-{@link MainPanel} mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @param resourceName	Name der zu pr�fenden Ressource
	 * @param levelTwo	Stellt ein, ob nur die Existenz der Ressource (Level 1, Parameterwert <code>false</code>) oder auch die Anzahl an Bedienern (Level 2, Parameterwert <code>true</code>) abgepr�ft werden sollen
	 * @return	Gibt <code>true</code> zur�ck, wenn die Ressource passend konfiguriert ist
	 */
	public static boolean testResource(final MainPanel mainPanel, final String resourceName, boolean levelTwo) {
		final EditModel model=getModel(mainPanel);
		if (model==null) return true;

		final ModelResource resource=model.resources.getNoAutoAdd(resourceName);
		if (resource==null) return false;

		if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return false;
		if (levelTwo) {
			if (resource.getCount()!=2) return false;
		}
		if (resource.getFailures().size()>0) return false;

		return true;
	}
}