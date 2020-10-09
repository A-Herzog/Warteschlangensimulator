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
package tools;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import language.Language;
import simulator.editmodel.EditModel;
import ui.MainFrame;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;

/**
 * Diese Klasse erlaubt es, Modelle (Bild+nutzerdefinierte Beschreibung+automatische Beschreibung)
 * als PowerPoint-Präsentationen zu exportieren.
 * @author Alexander Herzog
 */
public class SlidesGenerator extends AbstractSlidesGenerator {
	/** Editor-Modell welches exportiert werden soll */
	private final EditModel model;
	/** Bild des Modells */
	private final BufferedImage image;

	/**
	 * Konstruktor der Klasse
	 * @param model	Editor-Modell welches exportiert werden soll
	 * @param image	Bild des Modells
	 */
	public SlidesGenerator(final EditModel model, final BufferedImage image) {
		this.model=(model==null)?new EditModel():model;
		this.image=image;
	}

	@Override
	protected boolean buildSlides(XMLSlideShow pptx) {
		/* Titelfolie */
		final String name;
		if (model.name.trim().isEmpty()) name=Language.tr("SlidesGenerator.Modell"); else name=model.name;
		addTitleSlide(pptx,name,MainFrame.PROGRAM_NAME+" "+Language.tr("SlidesGenerator.ModelType"));

		/* Abbildung */
		if (image!=null) {
			final XSLFSlide slide=addContentSlide(pptx,name,(String)null);
			if (!addPicture(pptx,slide,image)) return false;
		}

		/* Beschreibung */
		if (model.description!=null && !model.description.trim().isEmpty()) {
			final List<String> lines=new ArrayList<>(Arrays.asList(model.description.split("\\n")));
			final String[] content=lines.stream().filter(line->!line.trim().isEmpty()).toArray(String[]::new);
			if (content.length>0) addContentSlide(pptx,Language.tr("SlidesGenerator.ModellDescription"),content,12);
		}

		/* Parameter */
		final ModelDescriptionBuilderStyled description=new ModelDescriptionBuilderStyled(model);
		description.run();
		addContentSlide(pptx,Language.tr("SlidesGenerator.ModellParameters"),shape->description.saveSlideShape(shape));

		return true;
	}
}
