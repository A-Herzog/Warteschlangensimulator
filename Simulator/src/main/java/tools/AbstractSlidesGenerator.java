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

import java.awt.Component;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import systemtools.MsgBox;

/**
 * Diese abstrakte Klasse stellt Basismethoden zur
 * Erstellung von PowerPoint-Präsentationen zur Verfügung.
 * @author Alexander Herzog
 * @see <a href="https://poi.apache.org/components/slideshow/xslf-cookbook.html">https://poi.apache.org/components/slideshow/xslf-cookbook.html</a>
 */
public abstract class AbstractSlidesGenerator {
	/**
	 * Konstruktor der Klasse
	 */
	public AbstractSlidesGenerator() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Diese Methode wird zur eigentlichen Erstellung der Präsentation
	 * aufgerufen. Sie muss von abgeleiteten Klassen implementiert werden.
	 * @param pptx	Präsentations-Datenobjekt
	 * @return	Gibt an, ob die Erstellung erfolgreich war.
	 * @see #save(String)
	 * @see #save(File)
	 */
	protected abstract boolean buildSlides(final XMLSlideShow pptx);

	/**
	 * Fügt eine neue Folie zur Präsentation hinzu.
	 * @param pptx	Präsentations-Datenobjekt
	 * @param layout	Zu verwendendes Layout für die neue Folie
	 * @return	Neue Folie (ist bereits zur Präsentation hinzugefügt)
	 */
	protected final XSLFSlide addSlide(final XMLSlideShow pptx, final SlideLayout layout) {
		final XSLFSlideMaster defaultMaster=pptx.getSlideMasters().get(0);
		final XSLFSlideLayout slideLayout=defaultMaster.getLayout(layout);
		return pptx.createSlide(slideLayout);
	}

	/**
	 * Erstellt eine Titelfolie
	 * @param pptx	Präsentations-Datenobjekt
	 * @param title	Haupt-Titel (darf <code>null</code> oder leer sein)
	 * @param subTitle	Untertitel (darf <code>null</code> oder leer sein)
	 * @return	Neue Folie (ist bereits zur Präsentation hinzugefügt)
	 */
	protected final XSLFSlide addTitleSlide(final XMLSlideShow pptx, final String title, final String subTitle) {
		final XSLFSlide slide=addSlide(pptx,SlideLayout.TITLE);

		if (title!=null && !title.isBlank()) {
			final XSLFTextShape text=slide.getPlaceholder(0);
			if (text!=null) text.setText(title);
		}

		if (subTitle!=null && !subTitle.isBlank()) {
			final XSLFTextShape text=slide.getPlaceholder(1);
			if (text!=null) text.setText(subTitle);
		}

		return slide;
	}

	/**
	 * Erstellt eine Inhaltsfolie
	 * @param pptx	Präsentations-Datenobjekt
	 * @param title	Titel (darf <code>null</code> oder leer sein)
	 * @param content	Textinhalt (darf <code>null</code> oder leer sein)
	 * @return	Neue Folie (ist bereits zur Präsentation hinzugefügt)
	 */
	protected final XSLFSlide addContentSlide(final XMLSlideShow pptx, final String title, final String content) {
		final XSLFSlide slide=addSlide(pptx,SlideLayout.TITLE_AND_CONTENT);

		if (title!=null && !title.isBlank()) {
			final XSLFTextShape text=slide.getPlaceholder(0);
			if (text!=null) text.setText(title);
		}

		if (content!=null && !content.isBlank()) {
			final XSLFTextShape text=slide.getPlaceholder(1);
			if (text!=null) {
				text.setText(content);
				text.resizeToFitText();
			}
		}

		return slide;
	}

	/**
	 * Erstellt eine Inhaltsfolie
	 * @param pptx	Präsentations-Datenobjekt
	 * @param title	Titel (darf <code>null</code> oder leer sein)
	 * @param content	Textinhalt (darf <code>null</code> oder leer sein)
	 * @param contentTextSize	Schriftgröße für den Contentbereich (Werte &le;0 bedeuten "keine Anpassung")
	 * @return	Neue Folie (ist bereits zur Präsentation hinzugefügt)
	 */
	protected final XSLFSlide addContentSlide(final XMLSlideShow pptx, final String title, final String[] content, final double contentTextSize) {
		final XSLFSlide slide=addSlide(pptx,SlideLayout.TITLE_AND_CONTENT);

		if (title!=null && !title.isBlank()) {
			final XSLFTextShape text=slide.getPlaceholder(0);
			if (text!=null) text.setText(title);
		}

		if (content!=null) {
			final XSLFTextShape text=slide.getPlaceholder(1);
			if (text!=null) {
				text.clearText();
				for (String line: content) {
					final XSLFTextParagraph paragraph=text.addNewTextParagraph();
					paragraph.setBullet(false);
					final XSLFTextRun run=paragraph.addNewTextRun();
					run.setText(line);
					if (contentTextSize>0) run.setFontSize(contentTextSize);
				}
			}
		}

		return slide;
	}

	/**
	 * Erstellt eine Inhaltsfolie
	 * @param pptx	Präsentations-Datenobjekt
	 * @param title	Titel (darf <code>null</code> oder leer sein)
	 * @param content	Callback welches den Inhalt in das Inhalts-Shape einfügt
	 * @return	Neue Folie (ist bereits zur Präsentation hinzugefügt)
	 */
	protected final XSLFSlide addContentSlide(final XMLSlideShow pptx, final String title, final Consumer<XSLFTextShape> content) {
		final XSLFSlide slide=addSlide(pptx,SlideLayout.TITLE_AND_CONTENT);

		if (title!=null && !title.isBlank()) {
			final XSLFTextShape text=slide.getPlaceholder(0);
			if (text!=null) text.setText(title);
		}

		if (content!=null) {
			final XSLFTextShape text=slide.getPlaceholder(1);
			if (text!=null) {
				text.clearText();
				content.accept(text);
			}
		}

		return slide;
	}

	/**
	 * Fügt ein Bild zu einer bestehenden Folie hinzu
	 * @param pptx	Präsentations-Datenobjekt
	 * @param slide	Folie zu der das Bild hinzugefügt werden soll
	 * @param image	Bild
	 * @return	Gibt an, ob das Hinzufügen erfolgreich war
	 */
	protected final boolean addPicture(final XMLSlideShow pptx, final XSLFSlide slide, final BufferedImage image) {
		if (image==null) return false;

		try (ByteArrayOutputStream out=new ByteArrayOutputStream()) {
			if (!ImageIO.write(image,"png",out)) return false;
			return addPicture(pptx,slide,out.toByteArray());
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Fügt ein Bild zu einer bestehenden Folie hinzu
	 * @param pptx	Präsentations-Datenobjekt
	 * @param slide	Folie zu der das Bild hinzugefügt werden soll
	 * @param pngBytes	Bild im png-Format
	 * @return	Gibt an, ob das Hinzufügen erfolgreich war
	 */
	protected final boolean addPicture(final XMLSlideShow pptx, final XSLFSlide slide, final byte[] pngBytes) {
		if (pngBytes==null) return false;

		/* Größe Content-Platzhalter auslesen */
		final XSLFTextShape content=slide.getPlaceholder(1);
		final Rectangle2D sizeContent;
		if (content==null) {
			sizeContent=null;
		} else {
			sizeContent=content.getAnchor();
			slide.removeShape(content);
		}

		final XSLFPictureData pd=pptx.addPicture(pngBytes,PictureType.PNG);
		final XSLFPictureShape picture=slide.createPicture(pd);

		/* Bildgröße an Größe des Content-Platzhalters anpassen */
		if (sizeContent!=null) {
			final double contentW=sizeContent.getMaxX()-sizeContent.getMinX();
			final double contentH=sizeContent.getMaxY()-sizeContent.getMinY();
			final Rectangle2D sizePicture=picture.getAnchor();
			double w=sizePicture.getMaxX()-sizePicture.getMinX();
			double h=sizePicture.getMaxY()-sizePicture.getMinY();
			if (w>0 && h>0) {
				final double ratio=w/h;
				w=contentW;
				h=w/ratio;
				if (h>contentH) {h=contentH; w=h*ratio;}
				sizePicture.setFrame(sizeContent.getMinX(),sizeContent.getMinY(),w,h);
				picture.setAnchor(sizePicture);
			}

		}

		return true;
	}

	/**
	 * Speichert die Präsentation in einer Datei.
	 * @param file	Ausgabedatei
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 * @see #buildSlides(XMLSlideShow)
	 */
	public final boolean save(final File file) {
		if (file==null) return false;
		try (XMLSlideShow pptx=new XMLSlideShow()) {
			if (!buildSlides(pptx)) return false;
			try (FileOutputStream out=new FileOutputStream(file)) {
				try (BufferedOutputStream buf=new BufferedOutputStream(out)) {
					pptx.write(buf);
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Speichert die Präsentation in einer Datei.
	 * @param file	Ausgabedatei
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 * @see #buildSlides(XMLSlideShow)
	 */
	public final  boolean save(final String file) {
		if (file==null) return save((File)null);
		return save(new File(file));
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines Dateinamens unter dem
	 * die Präsentation gespeichert werden soll an.
	 * @param owner	Übergeordnetes Element
	 * @param title	Title des Dialogs
	 * @return	Liefert im Erfolgsfall den Dateinamen. Wurde der Dialog abgebrochen, so liefert die Methode <code>null</code>.
	 */
	public static final File showSaveDialog(final Component owner, final String title) {
		Component c=owner; while ((c!=null) && (!(c instanceof Frame))) c=c.getParent();

		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);

		fc.setDialogTitle(title);
		final FileFilter pptx=new FileNameExtensionFilter(Language.tr("SlidesGenerator.FileTypePPTX")+" (*.pptx)","pptx");
		fc.addChoosableFileFilter(pptx);
		fc.setFileFilter(pptx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showOpenDialog(c)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==pptx) file=new File(file.getAbsoluteFile()+".pptx");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(c,file)) return null;
		}

		return file;
	}
}
