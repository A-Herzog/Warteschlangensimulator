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

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelResource;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Panel mit Einstellungen zur automatischen Modellerstellung eines geschlossenen Warteschlangenmodells
 * @see ModelGeneratorDialog
 * @author Alexander Herzog
 */
public class ModelGeneratorPanelClosed extends ModelGeneratorPanelBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6929203902642576789L;

	/**
	 * Auswahlfeld: Populationsgröße
	 */
	private final SpinnerNumberModel clientCountSpinner;

	/**
	 * Auswahlfeld: Anzahl an Stationen
	 */
	private final SpinnerNumberModel stationCountSpinner;

	/**
	 * Auswahlbox: Verzweigungen verwenden?
	 */
	private final JCheckBox decideCheckBox;

	/**
	 * Visualisierungen zur Anzeige der mittleren Anzahl an Kunden im System zum Modell hinzufügen?
	 */
	private final JCheckBox checkAddNVisualization;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelGeneratorPanelClosed() {
		super();

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		/* Quelle */
		addHeading(this,Language.tr("ModelGenerator.Source"),true);

		/* Anzahl an Kundenankünften */
		clientCountSpinner=addSpinner(this,Language.tr("ModelGenerator.ClosedModel.PopulationSize"),1,1_000,20);

		/* Bedienstation */
		addHeading(this,Language.tr("ModelGenerator.Process"),true);

		/* Anzahl an Stationen */
		stationCountSpinner=addSpinner(this,Language.tr("ModelGenerator.ClosedModel.StationCount"),1,6,5);

		/* Verzweigungen? */
		decideCheckBox=addCheckBox(this,Language.tr("ModelGenerator.ClosedModel.Decide"),true);

		stationCountSpinner.addChangeListener(e->{
			if ((Integer)stationCountSpinner.getValue()<4) decideCheckBox.setSelected(false);
		});
		decideCheckBox.addActionListener(e->{
			if (decideCheckBox.isSelected() && ((Integer)stationCountSpinner.getValue())<4) stationCountSpinner.setValue(4);
		});

		/* Visualisierung hinzufügen */
		addHeading(this,Language.tr("ModelGenerator.AddVisualization"),true);
		checkAddNVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.MeanNStation"),true);
	}

	@Override
	public String getTypeName() {
		return Language.tr("ModelGenerator.ClosedModel.SelectName");
	}

	@Override
	public EditModel getModel() {
		/* Parameter zusammenstellen */
		final int arrivalCount=(Integer)clientCountSpinner.getValue();
		final int stationCount=(Integer)stationCountSpinner.getValue();
		final boolean useDecide=decideCheckBox.isSelected();
		final boolean addNVisualization=checkAddNVisualization.isSelected();

		/* Variablen */
		StringBuilder description;
		ModelElementSource source;
		ModelElementVertex sourcesVertex, loopVertex1, loopVertex2, loopVertex3;
		ModelElementProcess[] process;
		ModelElementDecide[] decide;

		/* Daten berechnen */
		final int resourceGroups=stationCount;
		final double serviceTime=120;

		/* Modell anlegen */
		final EditModel model=buildModel(Language.tr("ModelGenerator.ClosedModel.Name"),Language.tr("ModelGenerator.ClosedModel.Description"));
		model.warmUpTime=0;
		model.warmUpTimeTime=0;
		model.useClientCount=false;
		model.finishTime=86400*10_000;
		model.useFinishTime=true;

		/* Ressourcen */
		for (int i=0;i<resourceGroups;i++) {
			String name=(resourceGroups==1)?Language.tr("ModelGenerator.Operator"):(Language.tr("ModelGenerator.OperatorGroup")+" "+(i+1));
			final ModelResource resource=new ModelResource(name);
			resource.setCount(1);
			model.resources.add(resource);
		}

		/* Überschrift */
		addHeading(model,Language.tr("ModelGenerator.ClosedModel.Name"));

		/* x-Position für nächste Station */
		int xPosition=50;
		int yPosition=100;

		/* Quelle */
		model.surface.add(source=new ModelElementSource(model,model.surface));
		source.setPosition(new Point(xPosition,yPosition));
		source.getRecord().setInterarrivalTimeDistribution(new OnePointDistributionImpl(1));
		source.getRecord().setMaxArrivalClientCount(arrivalCount);

		/* Infotext unter Quelle */
		description=new StringBuilder();
		description.append(Language.tr("Statistics.PopulationSize")+":\np:="+arrivalCount);
		addSmallInfo(model,description.toString(),xPosition,yPosition+100);

		xPosition+=250;

		/* Kante zum Zusammenführen von Neuankünften und Weiterleitungen */
		final int xStartVertex=xPosition-5;
		final int yStartVertex=yPosition+20;
		final int xEndVertex=xPosition-5+100+(useDecide?400*Math.min(stationCount,3):200*(((stationCount-1)%3)+1))-(useDecide?200:150);
		final int yEndVertex=yPosition+20+100+((stationCount>3)?200:0);

		model.surface.add(sourcesVertex=new ModelElementVertex(model,model.surface));
		sourcesVertex.setPosition(new Point(xStartVertex,yStartVertex));
		model.surface.add(loopVertex3=new ModelElementVertex(model,model.surface));
		loopVertex3.setPosition(new Point(xStartVertex,yEndVertex));
		model.surface.add(loopVertex2=new ModelElementVertex(model,model.surface));
		loopVertex2.setPosition(new Point(xEndVertex,yEndVertex));
		if (useDecide) {
			model.surface.add(loopVertex1=new ModelElementVertex(model,model.surface));
			loopVertex1.setPosition(new Point(xEndVertex,(!useDecide && stationCount>3)?(yStartVertex+200):yStartVertex));
		} else {
			loopVertex1=null;
		}

		xPosition+=100;

		/* Bedienstationen und Verzweigungen hinzufügen */
		process=new ModelElementProcess[stationCount];
		decide=new ModelElementDecide[stationCount];
		for (int i=0;i<stationCount;i++) {
			final int x=xPosition+(i%3)*(useDecide?400:200);
			final int y=yPosition+(i/3)*200;
			model.surface.add(process[i]=new ModelElementProcess(model,model.surface));
			process[i].getNeededResources().get(0).put(model.resources.getName(i),1);
			final double currentServiceTime;
			if (useDecide && i<3) {
				currentServiceTime=serviceTime;
			} else {
				currentServiceTime=serviceTime*1.75;
			}
			process[i].getWorking().set(new LogNormalDistributionImpl(currentServiceTime,currentServiceTime/4));
			process[i].setName(""+(i+1));
			process[i].setPosition(new Point(x,y));
			if (useDecide && i!=2 && i!=stationCount-1) {
				model.surface.add(decide[i]=new ModelElementDecide(model,model.surface));
				decide[i].setPosition(new Point(x+200,y));
			}
			if (addNVisualization) {
				addSimDataText(model,x,y+85,Language.tr("ModelGenerator.Visualization.MeanNStationTitle"),Color.RED,"N_avg("+process[i].getId()+")");
			}
		}

		/* Kanten einfügen */
		addEdge(model,source,sourcesVertex);
		addEdge(model,sourcesVertex,process[0]);
		if (useDecide) {
			for (int i=0;i<process.length;i++) {
				if (decide[i]!=null) {
					addEdge(model,process[i],decide[i]);
					addEdge(model,decide[i],process[i+1]);
					if (i>=3) {
						addEdge(model,decide[i],process[i+1-3],true);
					} else {
						if (i+1+2>=process.length) {
							addEdge(model,decide[i],process[i+1+2-1],true);
						} else {
							addEdge(model,decide[i],process[i+1+2],true);
						}
					}
				} else {
					if (i==2 || i==process.length-1) {
						addEdge(model,process[i],loopVertex1,true);
					} else {
						addEdge(model,process[i],process[i+1],true);
					}
				}
			}
		} else {
			for (int i=0;i<process.length;i++) {
				if (i==process.length-1) {
					addEdge(model,process[i],loopVertex2,true);
				} else {
					if (i==2) {
						final ModelElementVertex v1=new ModelElementVertex(model,model.surface);
						model.surface.add(v1);
						v1.setPosition(new Point(process[i].getPosition(true).x+45,process[i].getPosition(true).y+120));
						final ModelElementVertex v2=new ModelElementVertex(model,model.surface);
						model.surface.add(v2);
						v2.setPosition(new Point(process[i+1].getPosition(true).x+45,process[i].getPosition(true).y+120));
						addEdge(model,process[i],v1,true);
						addEdge(model,v1,v2,true);
						addEdge(model,v2,process[i+1],true);
					} else {
						addEdge(model,process[i],process[i+1],true);
					}
				}
			}
		}
		if (loopVertex1!=null) addEdge(model,loopVertex1,loopVertex2);
		addEdge(model,loopVertex2,loopVertex3);
		addEdge(model,loopVertex3,sourcesVertex);

		return model;
	}

}
