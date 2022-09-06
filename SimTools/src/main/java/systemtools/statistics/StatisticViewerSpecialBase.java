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

import java.io.BufferedWriter;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Basisklasse für Reports und sonstige Sonderklassen, die nicht direkt Texte, Tabellen oder Grafiken anzeigen.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
public abstract class StatisticViewerSpecialBase implements StatisticViewer {
	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerSpecialBase() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_NOIMAGE;
	}

	@Override
	public int saveHtml(BufferedWriter bw, File mainFile, int nextImageNr, boolean imagesInline) {
		return 0;
	}

	@Override
	public int saveLaTeX(BufferedWriter bw, File mainFile, int nextImageNr) {
		return 0;
	}

	@Override
	public boolean saveDOCX(DOCXWriter doc) {
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
	public String[] ownSettingsName() {
		return null;
	}

	@Override
	public Icon[] ownSettingsIcon() {
		return null;
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		return false;
	}

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {}

	@Override
	public void setRequestChartSetup(Supplier<ChartSetup> getChartSetup) {}

	@Override
	public void setUpdateChartSetup(Consumer<ChartSetup> setChartSetup) {	}
}