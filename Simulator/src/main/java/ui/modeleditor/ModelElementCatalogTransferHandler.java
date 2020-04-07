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

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;

/**
 * Ermöglicht das Übertragen von Vorlagen (<code>ModelElementPosition</code>) aus einer Liste in
 * ein <code>ModelSurfacePanel</code>-Element.
 * @author Alexander Herzog
 */
public class ModelElementCatalogTransferHandler extends TransferHandler {
	private static final long serialVersionUID = -6846928178641998737L;

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	private void generateImage(final ModelElementPosition element) {
		final Point point=element.getLowerRightPosition();
		BufferedImage image=new BufferedImage(point.x+1,point.y+1,BufferedImage.TYPE_4BYTE_ABGR);
		element.drawToGraphics(image.getGraphics(),new Rectangle(0,0,point.x,point.y),1.0,false);
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
