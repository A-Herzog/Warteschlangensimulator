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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import systemtools.GUITools;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;

/**
 * Ermöglicht das Übertragen von Vorlagen ({@link ModelElementPosition}) aus einer Liste in
 * ein {@link ModelSurfacePanel}-Element.
 * @author Alexander Herzog
 */
public class ModelElementCatalogTransferHandler extends TransferHandler {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6846928178641998737L;

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	/**
	 * Erzeugt und stellt das Bild, das während des Drag&amp;drop-Vorgangs angezeigt werden soll, ein.
	 * @param element	Element das in dem Bild zu sehen sein soll
	 */
	private void generateImage(final ModelElementPosition element) {
		final double scale=GUITools.getOSScaleFactor();

		final Point point=element.getLowerRightPosition();
		final int width=(int)Math.round(point.x*scale);
		final int height=(int)Math.round(point.y*scale);
		final BufferedImage image=new BufferedImage(width+1,height+1,BufferedImage.TYPE_4BYTE_ABGR);
		element.drawToGraphics(image.getGraphics(),new Rectangle(0,0,width,height),1.0*scale,false);
		setDragImage(image);
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		/* Prüfen ob die Quelle brauchbar ist */
		if (!(c instanceof JList)) return null;
		@SuppressWarnings("rawtypes")
		Object obj=((JList)c).getSelectedValue();
		if (obj instanceof ModelElementListGroup) return null;
		if (!(obj instanceof ModelElementPosition)) return null;

		/* Drag-Bild erzeugen */
		generateImage((ModelElementPosition)obj);

		/* Name für Transfer */
		String name;
		if (obj instanceof ModelElementBox) {
			name=((ModelElementBox)obj).getTypeName();
		} else {
			name=((ModelElementPosition)obj).getContextMenuElementName();
		}
		return new StringSelection(name);
	}
}
