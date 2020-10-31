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
package ui.modeleditor.descriptionbuilder;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JTextPane;

import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.odftoolkit.simple.TextDocument;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.statistics.PDFWriter;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Erzeugt eine Beschreibungen für das Modell
 * und stellt diese in formatierter Form in verschiedenen
 * Formaten zur Verfügung
 * @author Alexander Herzog
 * @see ModelDescriptionBuilder
 */
public class ModelDescriptionBuilderStyled extends ModelDescriptionBuilder {
	/**
	 * Objekt zur Erstellung allgemeiner formatierter Textausgaben
	 */
	private final StyledTextBuilder textBuilder;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 * @param plainMode	Gibt an, ob eine Überschrift für das gesamte Modell ausgegeben werden soll (<code>false</code>) oder nicht (<code>true</code>)
	 */
	public ModelDescriptionBuilderStyled(final EditModel model, final boolean plainMode) {
		this(model,null,plainMode);
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 */
	public ModelDescriptionBuilderStyled(final EditModel model) {
		this(model,null,false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell zu dem die Beschreibung generiert werden soll
	 * @param elements	Liste der Elemente, die berücksichtigt werden sollen (kann <code>null</code> sein, dann werden alle Elemente verarbeitet)
	 * @param plainMode	Gibt an, ob eine Überschrift für das gesamte Modell ausgegeben werden soll (<code>false</code>) oder nicht (<code>true</code>)
	 */
	public ModelDescriptionBuilderStyled(final EditModel model, final List<ModelElement> elements, final boolean plainMode) {
		super(model,elements);
		textBuilder=new StyledTextBuilder();
		if (!plainMode) textBuilder.addHeading(1,getModelDescriptionTitle());
	}

	@Override
	protected void processStation(ModelElementBox station, Map<Integer, List<String[]>> properties) {
		textBuilder.addHeading(2,getStationName(station));
		for (int key: properties.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray()) {
			for (String[] property: properties.get(key)) {
				textBuilder.addHeading(3,property[0]);
				textBuilder.beginParagraph();
				textBuilder.addLines(property[1]);
				textBuilder.endParagraph();
			}
		}
	}

	@Override
	protected void processResources(final List<String> resources) {
		textBuilder.addHeading(2,Language.tr("ModelDescription.Resources"));
		textBuilder.beginParagraph();
		textBuilder.addLines(resources);
		textBuilder.endParagraph();
	}

	@Override
	protected void processVariables(final List<String> variables) {
		textBuilder.addHeading(2,Language.tr("ModelDescription.InitialValuesForVariables"));
		textBuilder.beginParagraph();
		textBuilder.addLines(variables);
		textBuilder.endParagraph();
	}

	/**
	 * Schreibt die Modellbeschreibung in ein <code>JTextPane</code>-Element
	 * @param textPane	<code>JTextPane</code>-Element in das die Modellbeschreibung geschrieben werden soll
	 */
	public void writeToTextPane(final JTextPane textPane) {
		done();
		textBuilder.writeToTextPane(textPane);
	}

	/**
	 * Liefert die Modellbeschreibung als Text ohne Formatierungen
	 * @return	Modellbeschreibung als Text ohne Formatierungen
	 */
	public String getText() {
		done();
		return textBuilder.getText();
	}

	/**
	 * Liefert die Modellbeschreibung als RTF-String
	 * @return	Modellbeschreibung als RTF-String
	 */
	public String getRTFText() {
		done();
		return textBuilder.getRTFText();
	}

	/**
	 * Schreibt die Modellbeschreibung in ein Word-Dokument
	 * @param doc	Word-Dokument in das die Modellbeschreibung geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveDOCX(XWPFDocument doc) {
		done();
		return textBuilder.saveDOCX(doc);
	}

	/**
	 * Schreibt die Modellbeschreibung in ein Word-Dokument
	 * @param file	Dateiname unter dem das Word-Dokument gespeichert werden soll
	 * @param includeModelPicture	Fügt ein Bild des Modells in das Dokument ein
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveDOCX(final File file, final boolean includeModelPicture) {
		done();
		return textBuilder.saveDOCX(file,includeModelPicture?getModelImage():null);
	}

	/**
	 * Schreibt die Modellbeschreibung in ein odt-Dokument
	 * @param odt	Odt-Dokument in das die Modellbeschreibung geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveODT(TextDocument odt) {
		done();
		return textBuilder.saveODT(odt);
	}

	/**
	 * Schreibt die Modellbeschreibung in ein odt-Dokument
	 * @param file	Dateiname unter dem das odt-Dokument gespeichert werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean saveODT(final File file) {
		done();
		return textBuilder.saveODT(file);
	}

	/**
	 * Schreibt die Modellbeschreibung in eine pdf-Datei
	 * @param pdf	pdf-Datei-Objekt in das die Modellbeschreibung geschrieben werden soll
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean savePDF(final PDFWriter pdf) {
		done();
		return textBuilder.savePDF(pdf);
	}

	/**
	 * Schreibt die Modellbeschreibung in eine pdf-Datei
	 * @param owner	Übergeordnetes Element
	 * @param file	Dateiname unter dem die pdf-Datei gespeichert werden soll
	 * @param includeModelPicture	Fügt ein Bild des Modells in das Dokument ein
	 * @return	Gibt im Erfolgsfall <code>true</code> zurück
	 */
	public boolean savePDF(final Component owner, final File file, final boolean includeModelPicture) {
		done();
		return textBuilder.savePDF(owner,file,includeModelPicture?getModelImage():null);
	}

	/**
	 * Schreibt die Modellbeschreibung in ein Shape-Objekt einer PowerPoint-Folie
	 * @param shape	Shape-Objekt in das die Beschreibung eingefügt werden soll
	 */
	public void saveSlideShape(final XSLFTextShape shape) {
		done();
		textBuilder.saveSlideShape(shape,1);
	}

	/**
	 * Liefert ein Bild des Modells
	 * @return	Bild des Modells
	 */
	private BufferedImage getModelImage() {
		ModelSurfacePanel surfacePanel=new ModelSurfacePanel();
		surfacePanel.setSurface(getModel(),getModel().surface,getModel().clientData,getModel().sequences);
		return surfacePanel.getImage(2000,2000);
	}

	/**
	 * Kopiert die Modellbeschreibung als reinen Text ohne Formatierungen und gleichzeitig
	 * als RTF-Text in die Zwischenablage.
	 */
	public void copyToClipboard() {
		textBuilder.copyToClipboard();
	}
}