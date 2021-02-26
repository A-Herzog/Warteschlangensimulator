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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.elements.RunModelAnimationViewer;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.SimulationData;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.AnimationPanel;
import ui.help.Help;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimator;
import ui.modeleditor.ModelSurfacePanel;

/**
 * In diesem Dialog wird die Animation eines Unter-Modells dargestellt.
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubAnimationDialog extends BaseDialog implements RunModelAnimationViewer {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2350842116679968356L;

	/** Editor-Modell */
	private final EditModel model;
	/** Panel zur Anzeige des Untermodells */
	private final ModelSurfacePanel surfacePanel;
	/** Animator-System für {@link #surfacePanel} */
	private final ModelSurfaceAnimator surfaceAnimator;
	/** Animations-Panel der Haupt-Zeichenfläche */
	private final AnimationPanel mainAnimationPanel;

	/**
	 * Gewählter Delay-Wert
	 * @see AnimationPanel#getDelayIntern()
	 */
	private int delayInt;
	/**
	 * Simulationsdatenobjekt
	 * @see #updateViewer(SimulationData)
	 * @see #updateViewer(SimulationData, RunDataTransporter)
	 * @see #updateViewer(SimulationData, RunDataClient, boolean)
	 */
	private SimulationData simData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param mainSurface	Haupt-Zeichenfläche
	 * @param subSurface	Hier darzustellende Unter-Zeichenfläche
	 * @param mainAnimationPanel	Animations-Panel der Haupt-Zeichenfläche
	 */
	public ModelElementSubAnimationDialog(final Component owner, final ModelSurface mainSurface, final ModelSurface subSurface, final AnimationPanel mainAnimationPanel) {
		super(owner,Language.tr("Surface.Sub.Dialog.Title"),true);
		this.mainAnimationPanel=mainAnimationPanel;

		model=mainAnimationPanel.getSimulator().getEditModel();

		final JPanel content=createGUI(()->Help.topicModal(ModelElementSubAnimationDialog.this,"ModelElementSub"));
		content.setBorder(null);
		content.setLayout(new BorderLayout());
		content.add(new JScrollPane(surfacePanel=new ModelSurfacePanel(true,false)),BorderLayout.CENTER);
		surfacePanel.setColors(model.surfaceColors);
		surfacePanel.setBackgroundImage(model.surfaceBackgroundImageInSubModels?model.surfaceBackgroundImage:null,model.surfaceBackgroundImageScale,model.surfaceBackgroundImageMode);
		surfacePanel.setSurface(model,subSurface,model.clientData,model.sequences);
		final SetupData setup=SetupData.getSetup();
		surfaceAnimator=new ModelSurfaceAnimator(null,surfacePanel,model.animationImages,ModelSurfaceAnimator.AnimationMoveMode.MODE_MULTI,setup.useMultiCoreAnimation,setup.animateResources);
		surfaceAnimator.calcSurfaceSize();
		delayInt=mainAnimationPanel.getDelayIntern();

		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(this.owner); /* this.owner==ownerWindow; owner==nur JPanel oder sowas */

		updateViewer(mainAnimationPanel.getSimData());
	}

	@Override
	public boolean updateViewer(SimulationData simData) {
		return updateViewer(simData,null,false);
	}

	/**
	 * Zeitpunkt des letzten Aufrufs von
	 * {@link #updateViewer(SimulationData)} oder
	 * {@link #updateViewer(SimulationData, RunDataTransporter)} oder
	 * {@link #updateViewer(SimulationData, RunDataClient, boolean)}.
	 * @see #updateViewer(SimulationData)
	 * @see #updateViewer(SimulationData, RunDataTransporter)
	 * @see #updateViewer(SimulationData, RunDataClient, boolean)
	 */
	private long lastUpdateStep=0;

	@Override
	public boolean updateViewer(SimulationData simData, RunDataClient client, boolean moveByTransport) {
		surfacePanel.setAnimationSimulationData(simData);

		if (mainAnimationPanel.isRunning()) {
			if (surfaceAnimator.breakPointTest(client)) {
				mainAnimationPanel.playPause();
				surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,true,false);
				if (!moveByTransport) surfaceAnimator.process(simData,client,FastMath.min(20,delayInt/4));
				surfacePanel.repaint();
			}
		}

		final long currentTime=System.currentTimeMillis();
		if (currentTime<=lastUpdateStep+5 && delayInt==0) {
			surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,false,true);
			return true;
		}
		lastUpdateStep=currentTime;

		if (simData==null) simData=this.simData;
		if (simData==null) return true;
		this.simData=simData;

		/* keine normale Verzögerung hier, da diese schon auf der Hauptebene erfolgt */
		if (!moveByTransport) surfaceAnimator.process(simData,client,FastMath.min(20,delayInt/4));
		return true;
	}

	@Override
	public boolean updateViewer(SimulationData simData, RunDataTransporter transporter) {
		surfacePanel.setAnimationSimulationData(simData);

		long currentTime=System.currentTimeMillis();
		if (currentTime<=lastUpdateStep+5 && delayInt==0) {
			surfaceAnimator.updateSurfaceAnimationDisplayElements(simData,false,true);
			return true;
		}
		lastUpdateStep=currentTime;

		if (simData==null) simData=this.simData;
		if (simData==null) return true;
		this.simData=simData;

		/* keine normale Verzögerung hier, da diese schon auf der Hauptebene erfolgt */
		surfaceAnimator.process(simData,transporter,FastMath.min(20,delayInt/4));
		return true;
	}
}
