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
package ui.tutorial.pages;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import mathtools.distribution.tools.DistributionTools;
import ui.MainPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.tutorial.TutorialPage;
import ui.tutorial.TutorialTools;
import ui.tutorial.TutorialWindow;

/**
 * Tutorial-Seite: Dialog zum Konfigurieren der Quelle öffnen
 * @author Alexander Herzog
 * @see TutorialPage
 * @see TutorialWindow
 */
public class PageSetupSource implements TutorialPage {

	@Override
	public String getPageName() {
		return "SetupSource";
	}

	/** Zwischenankunftszeitenverteilung, die der Nutzer einstellen soll */
	private static AbstractRealDistribution	interarrivalDistribution=new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Prüft, ob die Quelle korrekt (gemäß den Vorgaben des Tutorials) konfiguriert ist.
	 * @param source	Zu prüfendes Quelle
	 * @return	Gibt <code>true</code> zurück, wenn die Quelle gemäß den Tutorial-Vorgabe konfiguriert ist
	 */
	public static boolean testSource(final ModelElementSource source) {
		if (source==null) return false;

		final ModelElementSourceRecord record=source.getRecord();

		if (record.getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) return false;
		if (!DistributionTools.compare(record.getInterarrivalTimeDistribution(),interarrivalDistribution)) return false;

		if (record.getTimeBase()!=ModelSurface.TimeBase.TIMEBASE_SECONDS) return false;
		if (record.getBatchSize()==null || !record.getBatchSize().trim().equals("1")) return false;
		if (record.getMaxArrivalCount()>=0) return false;
		if (record.getMaxArrivalClientCount()>=0) return false;
		if (record.getArrivalStart()!=0.0) return false;

		return true;
	}

	@Override
	public String checkNextCondition(MainPanel mainPanel) {
		if (!TutorialTools.hasSource(mainPanel)) return new PageAddSource().getPageName();
		if (!TutorialTools.hasProcess(mainPanel)) return new PageAddProcess().getPageName();
		if (!TutorialTools.hasDispose(mainPanel)) return new PageAddDispose().getPageName();
		if (!TutorialTools.connect1(mainPanel)) return new PageConnect1().getPageName();
		if (!TutorialTools.connect2(mainPanel)) return new PageConnect2().getPageName();

		final ModelElementSource source=TutorialTools.getSource(mainPanel);
		if (testSource(source)) return new PageSetupProcess().getPageName();

		return null;
	}
}