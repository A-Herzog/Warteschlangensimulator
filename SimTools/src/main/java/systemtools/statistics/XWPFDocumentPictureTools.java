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
package systemtools.statistics;

import java.io.ByteArrayOutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

/**
 * F�gt ein Bild in ein docx-Dokument ein.
 * Diese Hilfsklasse stellt nur eine Reihe von statischen Methoden zur Verf�gung.
 * @see StatisticViewerImage
 * @author Alexander Herzog
 * @version 1.1
 */
public class XWPFDocumentPictureTools {
	private XWPFDocumentPictureTools() {}

	private static boolean createPicture(XWPFDocument doc, String blipId, int id, int width, int height) {
		final int EMU=9525;
		width*=EMU;
		height*=EMU;

		CTInline inline = doc.createParagraph().createRun().getCTR().addNewDrawing().addNewInline();

		String picXml = "" +
				"<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +
				"   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
				"      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +
				"         <pic:nvPicPr>" +
				"            <pic:cNvPr id=\"" + id + "\" name=\"Generated\"/>" +
				"            <pic:cNvPicPr/>" +
				"         </pic:nvPicPr>" +
				"         <pic:blipFill>" +
				"            <a:blip r:embed=\"" + blipId + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +
				"            <a:stretch>" +
				"               <a:fillRect/>" +
				"            </a:stretch>" +
				"         </pic:blipFill>" +
				"         <pic:spPr>" +
				"            <a:xfrm>" +
				"               <a:off x=\"0\" y=\"0\"/>" +
				"               <a:ext cx=\"" + width + "\" cy=\"" + height + "\"/>" +
				"            </a:xfrm>" +
				"            <a:prstGeom prst=\"rect\">" +
				"               <a:avLst/>" +
				"            </a:prstGeom>" +
				"         </pic:spPr>" +
				"      </pic:pic>" +
				"   </a:graphicData>" +
				"</a:graphic>";

		XmlToken xmlToken=null;
		try	{xmlToken=XmlToken.Factory.parse(picXml);} catch(XmlException xe) {return false;}
		inline.set(xmlToken);

		inline.setDistT(0);
		inline.setDistB(0);
		inline.setDistL(0);
		inline.setDistR(0);

		CTPositiveSize2D extent = inline.addNewExtent();
		extent.setCx(width);
		extent.setCy(height);

		CTNonVisualDrawingProps docPr = inline.addNewDocPr();
		docPr.setId(id);
		docPr.setName("Picture " + id);
		docPr.setDescr("Generated");

		return true;
	}

	/**
	 * F�gt ein Bild in ein docx-Dokument ein
	 * @param doc	Aktives docx-Dokument, in das das Bild eingef�gt werden soll
	 * @param data	Bildaten, die in das Dokument eingef�gt werden sollen
	 * @param pictureType	Kann z.B. <code>Document.PICTURE_TYPE_JPEG</code> sein
	 * @param originalSizeX	Breite des Bildes (zur Berechnung des Seitenverh�ltnisses)
	 * @param originalSizeY	H�he des Bildes (zur Berechnung des Seitenverh�ltnisses)
	 * @return	Liefert <code>true</code> zur�ck, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	public static boolean addPicture(XWPFDocument doc, byte[] data, int pictureType, int originalSizeX, int originalSizeY) {
		final int sizeX=600; /* Seitenbreite */
		final int sizeY=sizeX*originalSizeY/originalSizeX;

		String blipId;

		try {
			blipId=doc.addPictureData(data,pictureType);
		} catch (InvalidFormatException e) {return false;}

		try {
			createPicture(doc,blipId,doc.getNextPicNameNumber(pictureType),sizeX,sizeY);
		} catch (InvalidFormatException e) {return false;}

		return true;
	}

	/**
	 * F�gt ein Bild in ein docx-Dokument ein
	 * @param doc	Aktives docx-Dokument, in das das Bild eingef�gt werden soll
	 * @param data	Bildaten, die in das Dokument eingef�gt werden sollen
	 * @param pictureType	Kann z.B. <code>Document.PICTURE_TYPE_JPEG</code> sein
	 * @param originalSizeX	Breite des Bildes (zur Berechnung des Seitenverh�ltnisses)
	 * @param originalSizeY	H�he des Bildes (zur Berechnung des Seitenverh�ltnisses)
	 * @return	Liefert <code>true</code> zur�ck, wenn die Daten erfolgreich geschrieben werden konnten.
	 */
	public static boolean addPicture(XWPFDocument doc, ByteArrayOutputStream data, int pictureType, int originalSizeX, int originalSizeY) {
		return addPicture(doc,data.toByteArray(),pictureType,originalSizeX,originalSizeY);
	}
}