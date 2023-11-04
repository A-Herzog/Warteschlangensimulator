/**
 * Copyright 2023 Alexander Herzog
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
package ui.generator;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JRadioButton;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.distribution.OnePointDistributionImpl;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelResource;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;

/**
 * Panel mit Einstellungen zur automatischen Modellerstellung eines Belastungsmodells
 * @see ModelGeneratorDialog
 * @author Alexander Herzog
 */
public class ModelGeneratorPanelLoad extends ModelGeneratorPanelBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5654876428274849063L;

	/**
	 * Auswahlfeld: Anzahl an Kundenankünften
	 */
	private final SpinnerNumberModel clientCount;

	/**
	 * Auswahlfeld: Anzahl an Stationen
	 */
	private final SpinnerNumberModel stationCount;

	/**
	 * Option: Bedienstationen (statt Verzögerungsstationen) erzeugen
	 */
	private final JRadioButton[] options;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelGeneratorPanelLoad() {
		super();

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		/* Quelle */
		addHeading(this,Language.tr("ModelGenerator.Source"),true);

		/* Anzahl an Kundenankünften */
		clientCount=addSpinner(this,Language.tr("ModelGenerator.LargeModel.ArrivalCount"),1,1_000_000_000,100_000);

		/* Bedienstation */
		addHeading(this,Language.tr("ModelGenerator.Process"),true);

		/* Anzahl an Stationen */
		stationCount=addSpinner(this,Language.tr("ModelGenerator.LargeModel.StationCount"),1,10_000,2_500);

		/* Stationstypen */
		options=addRadioButtons(this,new String[] {
				Language.tr("ModelGenerator.LargeModel.StationType.Delay"),
				Language.tr("ModelGenerator.LargeModel.StationType.Process")
		},0);
	}

	@Override
	public String getTypeName() {
		return Language.tr("ModelGenerator.LargeModel.SelectName");
	}

	/**
	 * Erzeugt ein großes Testmodell zur Untersuchung des Verhaltens des Simulators
	 * bei Modellen aus sehr vielen Stationen.
	 * @param clientCount	Zu simulierende Kundenankünfte
	 * @param stationCount	Anzahl an Stationen
	 * @param useProcessStations	Sollen Bedienstationen (<code>true</code>) oder Verzögerungsstationen (<code>false</code>) verwendet werden?
	 * @return	Testmodell
	 */
	private static EditModel getLargeModel(final long clientCount, final int stationCount, final boolean useProcessStations) {
		final double meanInterArrivalTime=100;
		final double delayTime=1;
		final double meanServiceTime=10;
		final int stationsPerRow=30;

		/* Modell anlegen */
		final EditModel model=buildModel(Language.tr("ModelGenerator.LargeModel.Description"),Language.tr("ModelGenerator.LargeModel.DescriptionLong"));
		model.clientCount=clientCount;
		model.warmUpTime=0.0;
		model.distributionRecordHours=0;
		model.distributionRecordClientDataValues=0;

		/* Überschrift */
		addHeading(model,Language.tr("ModelGenerator.LargeModel.Description"));

		/* x-Position für nächste Station */
		int xPosition=50;
		int yPosition=100;
		int xDirection=1;

		/* Quelle */
		final ModelElementSource source=new ModelElementSource(model,model.surface);
		model.surface.add(source);
		source.setPosition(new Point(xPosition,yPosition));
		source.getRecord().setInterarrivalTimeDistribution(new ExponentialDistribution(null,meanInterArrivalTime,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
		xPosition+=200;

		ModelElementBox lastStation=source;

		/* Stationen */
		model.surface.startFastMultiAdd();
		try {
			for (int i=0;i<stationCount-2;i++) {
				final ModelElementBox station;
				if (useProcessStations) {
					final String operatorName="Operator "+(i+1);
					final ModelElementProcess process=new ModelElementProcess(model,model.surface);
					process.getWorking().set(new ExponentialDistribution(null,meanServiceTime,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
					final Map<String,Integer> resourceRecord=new HashMap<>();
					resourceRecord.put(operatorName,1);
					process.getNeededResources().set(0,resourceRecord);
					model.resources.add(new ModelResource(operatorName,1));
					station=process;
				} else {
					final ModelElementDelay delay=new ModelElementDelay(model,model.surface);
					delay.setDelayTime(new OnePointDistributionImpl(delayTime),null);
					station=delay;
				}
				station.setPosition(new Point(xPosition,yPosition));
				model.surface.add(station);
				addEdge(model,lastStation,station);
				lastStation=station;
				if (xPosition>=200*stationsPerRow-50 && xDirection==1) {
					yPosition+=100;
					xDirection=-1;
					continue;
				}
				if (xPosition<=200 && xDirection==-1) {
					yPosition+=100;
					xDirection=1;
					continue;
				}
				xPosition+=200*xDirection;
			}
		} finally {
			model.surface.endFastMultiAdd();
		}

		/* Ausgang */
		final ModelElementDispose dispose=new ModelElementDispose(model,model.surface);
		model.surface.add(dispose);
		dispose.setPosition(new Point(xPosition,yPosition));
		addEdge(model,lastStation,dispose);

		return model;
	}

	@Override
	public EditModel getModel() {
		return getLargeModel((Integer)clientCount.getValue(),(Integer)stationCount.getValue(),options[1].isSelected());
	}
}
