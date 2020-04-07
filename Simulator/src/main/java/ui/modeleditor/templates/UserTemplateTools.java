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
package ui.modeleditor.templates;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ModelElementEdge;

/**
 * Diese Klasse stellt statische Hilfsroutinen zum Umgang mit den Elementenvorlagen
 * bereit. Diese Klasse kann nicht instanziert werden.
 * @author Alexander Herzog
 * @see UserTemplate
 */
public class UserTemplateTools {
	/**
	 * Konstruktor der Klasse.<br>
	 * Kann nicht aufgerufen werden. Diese Klasse stellt nur statische Methoden bereit.
	 */
	private UserTemplateTools() {
	}

	private static void addEdges(final ModelElementEdge[] edges, final List<ModelElement> elements) {
		for (ModelElementEdge edge: edges) addEdge(edge,elements);
	}

	private static void addEdge(final ModelElementEdge edge, final List<ModelElement> elements) {
		if (!elements.contains(edge)) elements.add(edge);
	}

	/**
	 * Speichert eine Anzahl an Elementen in einem Stream
	 * @param surface	Zeichenoberfläche aus der ggf. weitere Kanten entnommen werden
	 * @param copyElements	Elemente der Zeichenfläche (direkt, keine Clone), die kopiert werden sollen
	 * @return	Stream bestehend aus den zu kopierenden Elementen
	 */
	public static ByteArrayInputStream getElements(final ModelSurface surface, final List<ModelElement> copyElements) {
		/* Kanten ergänzen */
		final List<ModelElement> elements1=new ArrayList<>();
		for (ModelElement element: copyElements) {
			if (elements1.contains(element)) continue;
			elements1.add(element);
			if (element instanceof ModelElementEdgeMultiIn) addEdges(((ModelElementEdgeMultiIn)element).getEdgesIn(),elements1);
			if (element instanceof ModelElementEdgeMultiOut) addEdges(((ModelElementEdgeMultiOut)element).getEdgesOut(),elements1);
			if (element instanceof ModelElementEdgeOut) addEdge(((ModelElementEdgeOut)element).getEdgeOut(),elements1);
		}

		/* Kanten entfernen, die nicht zwischen zwei enthaltenen Elementen sind */
		final List<ModelElement> elements2=new ArrayList<>();
		final List<Integer> ids=elements1.stream().map(element->element.getId()).collect(Collectors.toList());
		for (ModelElement element: elements1) {
			if (element instanceof ModelElementEdge) {
				final ModelElementEdge edge=(ModelElementEdge)element;
				if (edge.getConnectionStart()==null || !ids.contains(edge.getConnectionStart().getId())) continue;
				if (edge.getConnectionEnd()==null || !ids.contains(edge.getConnectionEnd().getId())) continue;
			}
			elements2.add(element);
		}

		/* Elemente in Stream kopieren */
		final ByteArrayOutputStream outputStream=surface.getTransferData(elements2);
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	/**
	 * Fügt die Elemente aus einem Stream in die Zeichenfläche ein
	 * @param inputStream	Stream, der die Elemente enthält
	 * @param surface	Zeichenfläche auf der die Elemente eingefügt werden sollen
	 */
	public static void copyToSurface(final ByteArrayInputStream inputStream, final ModelSurface surface) {
		surface.setTransferData(inputStream,new Point(50,50),1.0);
	}

	/**
	 * Überträgt eine Reihe von Elementen aus einer Zeichenoberfläche in ein Vorlageelement
	 * @param surface	Ausgangszeichenfläche
	 * @param copyElements	Liste der zu übertragenden Elemente (keine Kopien, sondern die Elemente, die in <code>surface</code> enthalten sind)
	 * @param template	Vorlage in die die Elemente als Kopien übertragen werden sollen
	 */
	public static void copyToTemplate(final ModelSurface surface, final List<ModelElement> copyElements, final UserTemplate template) {
		final ByteArrayInputStream inputStream=getElements(surface,copyElements);
		copyToTemplate(inputStream,template);
	}

	/**
	 * Überträgt eine Reihe von Elementen aus einem Stream in ein Vorlageelement
	 * @param inputStream	Stream, aus dem die Elemente geladen werden sollen
	 * @param template	Vorlage in die die Elemente übertragen werden sollen
	 */
	public static void copyToTemplate(final ByteArrayInputStream inputStream, final UserTemplate template) {
		final ModelSurface templateSurface=template.getSurface();
		copyToSurface(inputStream,templateSurface);
	}

	/**
	 * Liefert die Elemente einer Zeichenoberfläche in einer Form, in der sie in die reale Zeichenfläche eingefügt werden können.
	 * @param surface	Zeichenoberfläche deren Elemente übertragen werden sollen
	 * @return	Element im Zwischenablagenformat
	 */
	public static ByteArrayInputStream getAllElements(final ModelSurface surface) {
		surface.setSelectedAreaAll();
		final ByteArrayOutputStream outputStream=surface.getTransferData();
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	/**
	 * Liefert die Elemente einer Vorlage in einer Form, in der sie in die reale Zeichenfläche eingefügt werden können.
	 * @param template	Vorlage deren Elemente in die reale Zeichenfläche eingetragen werden sollen
	 * @return	Element im Zwischenablagenformat
	 */
	public static ByteArrayInputStream getAllElements(final UserTemplate template) {
		final ModelSurface templateSurface=template.getSurface();
		return getAllElements(templateSurface);
	}
}