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

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import mathtools.distribution.tools.DistributionTools;
import ui.MainPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.tutorial.TutorialPage;
import ui.tutorial.TutorialTools;
import ui.tutorial.TutorialWindow;

/**
 * Tutorial-Seite: Dialog zum Konfigurieren der Bedienstation öffnen
 * @author Alexander Herzog
 * @see TutorialPage
 * @see TutorialWindow
 */
public class PageSetupProcess implements TutorialPage {
	/**
	 * Konstruktor der Klasse
	 */
	public PageSetupProcess() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String getPageName() {
		return "SetupProcess";
	}

	/** Bedienzeitenverteilung, die der Nutzer einstellen soll */
	private static AbstractRealDistribution	workingDistribution=new ExponentialDistribution(null,80,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

	/**
	 * Liefert den Namen der an der Bedienstation eingestellten notwendigen Ressource
	 * @param process	Bedienstation deren notwendige Ressource ausgelesen werden soll
	 * @return	Name der Ressource oder <code>null</code> wenn keine eindeutige Ressource identifiziert werden konnte
	 */
	public static String getProcessResource(final ModelElementProcess process) {
		final List<Map<String,Integer>> resList=process.getNeededResources();
		if (resList.size()!=1) return null;
		final Map<String,Integer> res=resList.get(0);
		final String[] resNames=res.keySet().toArray(String[]::new);
		if (resNames.length!=1) return null;
		Integer I=res.get(resNames[0]);
		if (I==null || I.intValue()!=1) return null;

		return resNames[0];
	}

	/**
	 * Prüft, ob die Bedienstation korrekt (gemäß den Vorgaben des Tutorials) konfiguriert ist.
	 * @param process	Zu prüfendes Bedienstation
	 * @param mainPanel	Programm-{@link MainPanel} mit dem das interaktive Tutorial zusammenarbeiten soll
	 * @return	Gibt <code>true</code> zurück, wenn die Bedienstation gemäß den Tutorial-Vorgabe konfiguriert ist
	 */
	public static boolean testProcess(final ModelElementProcess process, final MainPanel mainPanel) {
		if (process==null) return false;

		if (process.getTimeBase()!=ModelSurface.TimeBase.TIMEBASE_SECONDS) return false;
		if (process.getProcessTimeType()!=ModelElementProcess.ProcessType.PROCESS_TYPE_PROCESS) return false;
		if (process.getBatchMinimum()!=1) return false;
		if (process.getBatchMaximum()!=1) return false;
		final Object obj=process.getWorking().get();
		if (obj==null || obj instanceof String) return false;
		if (!DistributionTools.compare((AbstractRealDistribution)obj,workingDistribution)) return false;
		if (process.getSetupTimes().isActive()) return false;
		if (process.getPostProcessing().get()!=null) return false;
		if (process.getCancel().get()!=null) return false;

		if (!process.getResourcePriority().equals("1")) return false;

		for (String clientType: process.getSurface().getClientTypes()) if (!process.getPriority(clientType).equals("w")) return false;

		final String resName=getProcessResource(process);
		if (resName==null) return false;

		if (!TutorialTools.testResource(mainPanel,resName,false)) return false;

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
		if (!PageSetupSource.testSource(source)) return new PageSetupSource().getPageName();

		final ModelElementProcess process=TutorialTools.getProcess(mainPanel);
		if (testProcess(process,mainPanel)) return new PageSetupModel().getPageName();

		return null;
	}
}