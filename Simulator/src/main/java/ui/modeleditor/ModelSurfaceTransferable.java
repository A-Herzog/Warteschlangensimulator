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
package ui.modeleditor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Diese Klasse ermöglicht das Übertragen von Elementen per Zwischenablage
 * @author Alexander Herzog
 * @see ModelSurfacePanel#copyToClipboard()
 * @see ModelSurfacePanel#pasteFromClipboard()
 */
public class ModelSurfaceTransferable implements Transferable, Serializable {
	private static final long serialVersionUID = -9109642719467049507L;

	/** {@link DataFlavor} für binäre Modelldaten */
	public final static DataFlavor MODEL_FLAVOR=new DataFlavor(ModelSurfaceTransferable.class,DataFlavor.javaSerializedObjectMimeType);
	/** {@link DataFlavor} für Modellbeschreibung als formatierter Text */
	public final static DataFlavor RTF_FLAVOR=new DataFlavor("text/rtf","Rich Formatted Text");
	/** {@link DataFlavor} für Modellbeschreibung als unformatierter Text */
	public final static DataFlavor TXT_FLAVOR=DataFlavor.stringFlavor;
	/** {@link DataFlavor} für Modell als Bild */
	public final static DataFlavor IMAGE_FLAVOR=DataFlavor.imageFlavor;

	private final byte[] transferModel;
	private final String transferRtf;
	private final String transferTxt;
	private final byte[] transferImage;
	private final DataFlavor[] flavors;

	/**
	 * Konstruktor der Klasse <code>ModelSurfaceTransferable</code>
	 * @param model	Zu übertrangende als Stream gespeicherte Elemente
	 * @param txt	Zu übertragende Modellbeschreibung als Plain-Text
	 * @param rtf	Zu übertragende Modellbeschreibung als RTF-Text
	 * @param image	Zu übertragendes Bild des Modells
	 * @see ModelSurface#getTransferData()
	 */
	public ModelSurfaceTransferable(final ByteArrayOutputStream model, final String txt, final String rtf, final BufferedImage image) {
		this.transferModel=model.toByteArray();
		this.transferTxt=txt;
		this.transferRtf=rtf;

		byte[] imageBytes=null;
		try (final ByteArrayOutputStream imageBuffer=new ByteArrayOutputStream()) {
			ImageIO.write(image,"png",imageBuffer);
			imageBytes=imageBuffer.toByteArray();
		} catch (IOException e) {}
		transferImage=imageBytes;

		this.flavors=getFlavors();
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		for (DataFlavor test: flavors) if (test.equals(flavor)) return true;
		return false;
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(RTF_FLAVOR)) return new ByteArrayInputStream(transferRtf.getBytes());
		if (flavor.equals(TXT_FLAVOR)) return transferTxt;
		if (flavor.equals(IMAGE_FLAVOR)) {
			if (transferImage==null) return new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB);
			return ImageIO.read(new ByteArrayInputStream(transferImage));
		}
		if (flavor.equals(MODEL_FLAVOR)) return this;

		throw new UnsupportedFlavorException(flavor);
	}

	/**
	 * Stream aus dem die Daten ein das Modell eingefügt werden können
	 * @return	Stream mit einzufügenden Elementen
	 * @see ModelSurface#getSurfaceFromTransferData(ByteArrayInputStream)
	 */
	public ByteArrayInputStream getStream() {
		if (transferModel==null) return null;
		return new ByteArrayInputStream(transferModel);
	}

	/**
	 * Liefert den Flavor unter dem die Daten in der Zwischenablage abgelegt werden
	 * @return	Flavor, nach dem beim Einfügen gesucht werden muss
	 */
	public static DataFlavor[] getFlavors() {
		final List<DataFlavor> dataFlavors=new ArrayList<>();

		dataFlavors.add(MODEL_FLAVOR);
		dataFlavors.add(RTF_FLAVOR);
		dataFlavors.add(TXT_FLAVOR);
		dataFlavors.add(IMAGE_FLAVOR);

		return dataFlavors.toArray(new DataFlavor[0]);
	}
}