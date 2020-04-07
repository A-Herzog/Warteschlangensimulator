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

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.io.BufferedWriter;
import java.io.File;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Basisklasse für Reports und sonstige Sonderklassen, die nicht direkt Texte, Tabellen oder Grafiken anzeigen.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
public abstract class StatisticViewerSpecialBase implements StatisticViewer {
	@Override
	public abstract ViewerType getType();

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_NOIMAGE;
	}

	@Override
	public abstract Container getViewer(boolean needReInit);

	@Override
	public abstract void copyToClipboard(Clipboard clipboard);

	@Override
	public abstract boolean print();

	@Override
	public abstract void save(Component owner);

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) {
		return 0;
	}

	@Override
	public boolean saveDOCX(XWPFDocument doc) {
		return false;
	}

	@Override
	public boolean savePDF(PDFWriter pdf) {
		return false;
	}

	@Override
	public void unZoom() {}

	@Override
	public JButton[] getAdditionalButton() {
		return null;
	}

	@Override
	public String ownSettingsName() {
		return null;
	}

	@Override
	public Icon ownSettingsIcon() {
		return null;
	}

	@Override
	public boolean ownSettings(JPanel owner) {
		return false;
	}

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {}
}