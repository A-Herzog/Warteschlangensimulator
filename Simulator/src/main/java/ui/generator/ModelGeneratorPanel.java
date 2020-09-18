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
package ui.generator;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementText;

/**
 * Panel mit Einstellungen zur automatischen Modellerstellung
 * @see ModelGeneratorDialog
 * @author Alexander Herzog
 */
public class ModelGeneratorPanel extends JPanel {
	private static final long serialVersionUID = -8231845358497240268L;

	/** Anzahl an Kundentypen */
	private final SpinnerNumberModel spinnerClientTypes;
	/** Verschiedene  Prioritäten? */
	private final JCheckBox checkPriorities;
	/** Ankunfts-Batch-Größe */
	private final SpinnerNumberModel spinnerArrivalBatch;
	/** Bedien-Batch-Größe */
	private final SpinnerNumberModel spinnerServiceBatch;
	/** Bedienzeitenverteilung */
	private final JComboBox<String> comboServiceDistribution;
	/** Auslastung der Bediener */
	private final JComboBox<String> comboServiceUtilization;
	/** Bedienreihenfolge */
	private final JComboBox<String> comboServiceDiscipline;
	/** Anzahl an Bedienstationen */
	private final SpinnerNumberModel spinnerStationCount;
	/** Bediener zwischen den Stationen teilen? */
	private final JCheckBox checkSharedResource;
	/** Auswahl der Bedienstation (zufällig oder kürzeste Warteschlange) */
	private final JComboBox<String> comboSelectQueue;
	/** Begrenzte Wartezeittoleranz verwenden? */
	private final JCheckBox	checkWaitingTimeTolerance;
	/** Visualisierungen zum Modell hinzufügen? */
	private final JCheckBox checkAddVisualization;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelGeneratorPanel() {
		super();

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		/* Quelle */
		addHeading(this,Language.tr("ModelGenerator.Source"),false);
		spinnerClientTypes=addSpinner(this,Language.tr("ModelGenerator.ClientTypes"),1,5,1);
		checkPriorities=addCheckBox(this,Language.tr("ModelGenerator.Priorities"),false);
		spinnerArrivalBatch=addSpinner(this,Language.tr("ModelGenerator.ArrivalBatch"),1,10,1,"("+Language.tr("ModelGenerator.ArrivalBatch.Info")+")");

		/* Warteschlange */
		addHeading(this,Language.tr("ModelGenerator.Queue"),true);
		comboSelectQueue=addCombo(this,Language.tr("ModelGenerator.SelectQueue"),new String[]{
				Language.tr("ModelGenerator.SelectQueue.Random"),
				Language.tr("ModelGenerator.SelectQueue.Shortest")
		});
		comboSelectQueue.setEnabled(false);
		checkWaitingTimeTolerance=addCheckBox(this,Language.tr("ModelGenerator.UseWaitingTimeTolerance"),false);
		comboServiceDiscipline=addCombo(this,Language.tr("ModelGenerator.ServiceDiscipline"),new String[]{
				Language.tr("ModelGenerator.ServiceDiscipline.FIFO"),
				Language.tr("ModelGenerator.ServiceDiscipline.LIFO"),
				Language.tr("ModelGenerator.ServiceDiscipline.Random")
		});

		/* Bedienstation */
		addHeading(this,Language.tr("ModelGenerator.Process"),true);
		spinnerStationCount=addSpinner(this,Language.tr("ModelGenerator.StationCount"),1,2,1);
		comboServiceDistribution=addCombo(this,Language.tr("ModelGenerator.ServiceDistribution"),new String[]{
				Language.tr("ModelGenerator.ServiceDistribution.Deterministic"),
				Language.tr("ModelGenerator.ServiceDistribution.Exp"),
				Language.tr("ModelGenerator.ServiceDistribution.LogNormalLowCV"),
				Language.tr("ModelGenerator.ServiceDistribution.LogNormalHighCV"),
		});
		comboServiceDistribution.setSelectedIndex(1);
		comboServiceUtilization=addCombo(this,Language.tr("ModelGenerator.ServiceUtilization"),new String[]{
				Language.tr("ModelGenerator.ServiceUtilization.Low"),
				Language.tr("ModelGenerator.ServiceUtilization.Medium"),
				Language.tr("ModelGenerator.ServiceUtilization.High")
		});
		comboServiceUtilization.setSelectedIndex(1);
		spinnerServiceBatch=addSpinner(this,Language.tr("ModelGenerator.ServiceBatch"),1,10,1);
		checkSharedResource=addCheckBox(this,Language.tr("ModelGenerator.SharedResource"),false);
		checkSharedResource.setVisible(false);

		/* Verknüpfung der Stationen */

		/* Prioritäten an -> Kundentypen mindestens auf 2, Bedienung auf FIFO */
		checkPriorities.addActionListener(e->{
			if (checkPriorities.isSelected()) {
				if (spinnerClientTypes.getNumber().intValue()==1) spinnerClientTypes.setValue(2);
				comboServiceDiscipline.setSelectedIndex(0); /* FIFO */
			}
		});

		/* Kundentypen auf >1 -> FIFO */
		/* Kundentypen auf 1 -> Prioritäten aus */
		spinnerClientTypes.addChangeListener(e->{
			if (spinnerClientTypes.getNumber().intValue()>1) {
				comboServiceDiscipline.setSelectedIndex(0); /* FIFO */
			} else {
				checkPriorities.setSelected(false);
			}
		});

		/* LIFO -> Kundentypen auf 1, Prioritäten aus */
		comboServiceDiscipline.addActionListener(e->{
			if (comboServiceDiscipline.getSelectedIndex()==1) { /* LIFO */
				spinnerClientTypes.setValue(1);
				checkPriorities.setSelected(false);
			}
		});

		/* geteilte Ressource und Warteschlangenwahl nur bei zwei Stationen */
		spinnerStationCount.addChangeListener(e->{
			final int stationCount=spinnerStationCount.getNumber().intValue();
			if (stationCount==1) checkSharedResource.setSelected(false);
			checkSharedResource.setVisible(stationCount>1);
			comboSelectQueue.setEnabled(stationCount>1);
		});
		checkSharedResource.addActionListener(e->{
			spinnerStationCount.setValue(2);
		});

		/* Visualisierung hinzufügen */
		addHeading(this,Language.tr("ModelGenerator.AddVisualization"),true);
		checkAddVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.WIP"),true);
	}

	/**
	 * Listener, die benachrichtigt werden, wenn die Einstellungen verändert wurden.
	 * @see #fireModelChanged()
	 */
	private Set<Runnable> modelChangeListeners=new HashSet<>();

	/**
	 * Registriert einen Listener, der benachrichtigt werden soll, wenn die Einstellungen verändert wurden.
	 * @param modelChangeListener	Zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener in die Liste neu aufgenommen wurde (und nicht bereits enthalten ist)
	 */
	public boolean addModelChangeListener(final Runnable modelChangeListener) {
		return modelChangeListeners.add(modelChangeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste  der zu benachrichtigen Listener, wenn die Einstellungen verändert wurden.
	 * @param modelChangeListener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener in der Liste enthalten war und entfernt werden konnte
	 */
	public boolean removeModelChangeListener(final Runnable modelChangeListener) {
		return modelChangeListeners.remove(modelChangeListener);
	}

	/**
	 * Benachrichtigt die Listener über Änderungen an den Einstellungen.
	 * @see #addModelChangeListener(Runnable)
	 * @see #removeModelChangeListener(Runnable)
	 */
	private void fireModelChanged() {
		modelChangeListeners.stream().forEach(listener->listener.run());
	}

	private void addHeading(final JPanel panel, final String text, final boolean spaceBefore) {
		if (spaceBefore) panel.add(Box.createVerticalStrut(15));

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		line.add(new JLabel("<html><body><b>"+text+"</b></body></html>"));
	}

	private SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value) {
		return addSpinner(panel,text,min,max,value,null);
	}

	private SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value, final String info) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JLabel label=new JLabel(text+":");
		line.add(label);
		final JSpinner spinner=new JSpinner(new SpinnerNumberModel(value,min,max,1));
		line.add(spinner);
		label.setLabelFor(spinner);
		if (info!=null) line.add(new JLabel(info));
		spinner.addChangeListener(e->fireModelChanged());
		return (SpinnerNumberModel)spinner.getModel();
	}

	private JCheckBox addCheckBox(final JPanel panel, final String text, final boolean selected) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JCheckBox checkBox=new JCheckBox(text,selected);
		line.add(checkBox);
		checkBox.addActionListener(e->fireModelChanged());
		return checkBox;
	}

	private JComboBox<String> addCombo(final JPanel panel, final String text, String[] options) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JLabel label=new JLabel(text+":");
		line.add(label);
		if (options==null || options.length==0) options=new String[]{""};
		final JComboBox<String> combo=new JComboBox<>(options);
		combo.setSelectedIndex(0);
		line.add(combo);
		label.setLabelFor(combo);
		combo.addActionListener(e->fireModelChanged());
		return combo;
	}

	private void addEdge(final EditModel model, final ModelElementBox station1, final ModelElementBox station2) {
		final ModelElementEdge edge=new ModelElementEdge(model,model.surface,station1,station2);
		station1.addEdgeOut(edge);
		station2.addEdgeIn(edge);
		model.surface.add(edge);
	}

	/**
	 * Liefert das neu erstellte Modell
	 * @return	Neu erstelltes Modell
	 */
	public EditModel getModel() {
		/* Parameter zusammenstellen */
		final int clientTypes=spinnerClientTypes.getNumber().intValue();
		final boolean priorities=checkPriorities.isSelected();
		final int arrivalBatch=spinnerArrivalBatch.getNumber().intValue();
		final int serviceBatch=spinnerServiceBatch.getNumber().intValue();
		final int serviceDistribution=comboServiceDistribution.getSelectedIndex();
		final int discipline=comboServiceDiscipline.getSelectedIndex();
		final int utilization=comboServiceUtilization.getSelectedIndex();
		final int stations=spinnerStationCount.getNumber().intValue();
		final boolean sharedResource=checkSharedResource.isSelected();
		final boolean shortestQueue=(comboSelectQueue.getSelectedIndex()==1);
		final boolean useWaitingTimeTolerance=checkWaitingTimeTolerance.isSelected();
		final boolean addVisualization=checkAddVisualization.isSelected();

		StringBuilder sb;
		ModelElementText label;
		final ModelElementSource[] sources=new ModelElementSource[clientTypes];
		final ModelElementProcess[] processes=new ModelElementProcess[stations];
		final ModelElementCounter[] counter=new ModelElementCounter[stations*2];

		/* Daten berechnen */
		final double interArrivalTime=70*clientTypes;
		double serviceTimeBase=60;
		switch (utilization) {
		case 0: serviceTimeBase=50; break;
		case 1: serviceTimeBase=60; break;
		case 2: serviceTimeBase=65; break;
		}
		final double serviceTime=((stations==1 || sharedResource)?1:2)*serviceTimeBase;
		final int resourceGroups=(stations==1 || sharedResource)?1:2;
		final double waitingTimeTolerance=600;

		/* Modell anlegen */
		final EditModel model=new EditModel();
		model.name=Language.tr("ModelGenerator.Model.Name");

		/* Beschreibung */
		sb=new StringBuilder();
		sb.append(Language.tr("ModelGenerator.Model.Description"));
		sb.append("\n\n");
		sb.append(Language.tr("ModelGenerator.Model.Description.Properties")+":");
		if (clientTypes>1) sb.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.ClientTypes"),clientTypes));
		if (priorities) sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Priorities"));
		if (arrivalBatch>1) sb.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.ArrivalBatch"),arrivalBatch));
		if (serviceBatch>1) sb.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.ServiceBatch"),serviceBatch));
		switch (serviceDistribution) {
		case 0:
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceDeterministic"));
			break;
		case 1:
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceExp"));
			break;
		case 2:
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLognormal"));
			break;
		case 3:
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLognormalHighCV"));
			break;
		}
		switch (discipline) {
		case 0:
			/* Kein besonderer Hinweis auf FIFO */
			break;
		case 1:
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLIFO"));
			break;
		case 2:
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceRandom"));
			break;
		}
		if (stations>1) {
			sb.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.MultiStations"),stations));
			if (shortestQueue) {
				sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Queue.Shortest"));
			} else {
				sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Queue.Random"));
			}
			if (sharedResource) sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.SharedResource"));
		}
		if (useWaitingTimeTolerance) {
			sb.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.LimitedWaitingTimeTolerance"));
		}
		model.description=sb.toString();

		/* Ressourcen */
		for (int i=0;i<resourceGroups;i++) {
			String name=(resourceGroups==1)?Language.tr("ModelGenerator.Operator"):(Language.tr("ModelGenerator.OperatorGroup")+" "+(i+1));
			final ModelResource resource=new ModelResource(name);
			resource.setCount(1);
			model.resources.add(resource);
		}

		/* Überschrift */
		model.surface.add(label=new ModelElementText(model,model.surface));
		label.setPosition(new Point(50,50));
		label.setText(Language.tr("ModelGenerator.Model.Name"));
		label.setTextBold(true);
		label.setTextSize(label.getTextSize()+2);

		/* x-Position für nächste Station */
		int xPosition=50;
		int yPosition;

		/* Quellen */
		if (clientTypes>=stations) yPosition=100; else yPosition=150;
		if (useWaitingTimeTolerance && stations>1) yPosition+=50;
		for (int i=0;i<sources.length;i++) {
			model.surface.add(sources[i]=new ModelElementSource(model,model.surface));
			sources[i].setPosition(new Point(xPosition,yPosition));
			final String name;
			if (sources.length>1) {
				name=sources[i].getName()+" "+((char)('A'+i));
				sources[i].setName(name);
			} else {
				name=sources[i].getName();
			}
			if (arrivalBatch>1) sources[i].getRecord().setBatchSize(""+arrivalBatch);
			sources[i].getRecord().setInterarrivalTimeDistribution(new ExponentialDistribution(null,interArrivalTime,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
			yPosition+=100;
			if (i==1) model.clientData.setIcon(name,"user_red");
			if (i==2) model.clientData.setIcon(name,"user_green");
			if (i==3) model.clientData.setIcon(name,"user_gray");
			if (i==4) model.clientData.setIcon(name,"user_orange");
		}

		/* Infotext unter Quellen */
		model.surface.add(label=new ModelElementText(model,model.surface));
		label.setPosition(new Point(xPosition,yPosition));
		label.setTextItalic(true);
		label.setTextSize(label.getTextSize()-2);
		sb=new StringBuilder();
		sb.append(Language.tr("Statistics.InterArrivalTimes")+":\nE[I]:="+NumberTools.formatNumber(interArrivalTime)+" "+Language.tr("Statistics.Seconds"));
		if (arrivalBatch>1) {
			sb.append("\n\n");
			sb.append(Language.tr("ModelGenerator.ArrivalBatch")+":\nb:="+arrivalBatch);
		}
		label.setText(sb.toString());

		/* Nächste Spalte */
		xPosition+=250;

		/* Verzweigen? */
		final ModelElementDecide decide;
		if (stations>1) {
			model.surface.add(decide=new ModelElementDecide(model,model.surface));
			yPosition=50+50*(Math.max(clientTypes,stations));
			if (useWaitingTimeTolerance && stations>1) yPosition+=50;
			decide.setPosition(new Point(xPosition,yPosition));
			if (shortestQueue) {
				decide.setMode(ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION);
			} else {
				decide.setMode(ModelElementDecide.DecideMode.MODE_CHANCE);
			}
			yPosition+=100;

			/* Infotext unter Verzweigen */
			model.surface.add(label=new ModelElementText(model,model.surface));
			label.setPosition(new Point(xPosition,yPosition));
			label.setTextItalic(true);
			label.setTextSize(label.getTextSize()-2);
			sb=new StringBuilder();
			sb.append(Language.tr("ModelGenerator.SelectQueue")+":\n");
			if (shortestQueue) {
				sb.append(Language.tr("ModelGenerator.SelectQueue.Shortest"));
			} else {
				sb.append(Language.tr("ModelGenerator.SelectQueue.Random"));
			}
			label.setText(sb.toString());

			/* Nächste Spalte */
			xPosition+=200;
		} else {
			decide=null;
		}

		/* Bedienstationen */
		if (clientTypes>stations) yPosition=100+(clientTypes-stations)*50; else yPosition=100;
		for (int i=0;i<processes.length;i++) {
			model.surface.add(processes[i]=new ModelElementProcess(model,model.surface));
			processes[i].setPosition(new Point(xPosition,yPosition));
			if (serviceBatch>1) {
				processes[i].setBatchMinimum(serviceBatch);
				processes[i].setBatchMaximum(serviceBatch);
			}
			final String name=(resourceGroups==1)?model.resources.getName(0):model.resources.getName(i);
			processes[i].getNeededResources().get(0).put(name,1);
			switch (serviceDistribution) {
			case 0:
				processes[i].getWorking().set(new OnePointDistributionImpl(serviceTime));
				break;
			case 1:
				processes[i].getWorking().set(new ExponentialDistribution(null,serviceTime,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
				break;
			case 2:
				processes[i].getWorking().set(new LogNormalDistributionImpl(serviceTime,serviceTime/4));
				break;
			case 3:
				processes[i].getWorking().set(new LogNormalDistributionImpl(serviceTime,3*serviceTime/4));
				break;
			}
			switch (discipline) {
			case 0:
				/* Nichts einzustellen */
				break;
			case 1:
				processes[i].setPriority(sources[0].getName(),"-w");
				break;
			case 2:
				processes[i].setPriority(sources[0].getName(),"random()");
				break;
			}
			if (priorities) for (int j=0;j<sources.length;j++) processes[i].setPriority(sources[j].getName(),(100*j)+"+w");
			if (useWaitingTimeTolerance) processes[i].getCancel().set(new ExponentialDistribution(null,waitingTimeTolerance,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));

			if (useWaitingTimeTolerance && i<processes.length-1) yPosition+=200; else yPosition+=100;
		}

		/* Infotext unter Bedienstationen */
		model.surface.add(label=new ModelElementText(model,model.surface));
		label.setPosition(new Point(xPosition,yPosition));
		label.setTextItalic(true);
		label.setTextSize(label.getTextSize()-2);
		sb=new StringBuilder();
		sb.append(Language.tr("ModelGenerator.ServiceDistribution")+":\n");
		double cvS=0;
		switch (serviceDistribution) {
		case 0:
			sb.append(Language.tr("ModelGenerator.ServiceDistribution.Deterministic")+"\n");
			cvS=0;
			break;
		case 1:
			sb.append(Language.tr("ModelGenerator.ServiceDistribution.Exp")+"\n");
			cvS=1;
			break;
		case 2:
			cvS=0.25;
			sb.append(Language.tr("ModelGenerator.ServiceDistribution.LogNormal")+"\n");
			break;
		case 3:
			sb.append(Language.tr("ModelGenerator.ServiceDistribution.LogNormal")+"\n");
			cvS=0.75;
			break;
		}
		sb.append("E[S]:="+NumberTools.formatNumber(serviceTime)+" "+Language.tr("Statistics.Seconds")+"\n");
		sb.append("CV[S]:="+NumberTools.formatNumber(cvS,2)+"\n");
		if (stations==1) {
			sb.append(Language.tr("ModelGenerator.NumberOfOperators")+": c:=1");
		} else {
			if (sharedResource) {
				sb.append(Language.tr("ModelGenerator.NumberOfOperators")+": c:=1 ("+Language.tr("ModelGenerator.NumberOfOperators.shared")+")");
			} else {
				sb.append(Language.tr("ModelGenerator.NumberOfOperators")+": c:=1 ("+Language.tr("ModelGenerator.NumberOfOperators.perStation")+")");
			}
		}
		if (serviceBatch>1) {
			sb.append("\n");
			sb.append(Language.tr("ModelGenerator.ServiceBatchSize")+": b="+serviceBatch);
		}
		if (priorities) {
			sb.append("\n");
			switch (clientTypes) {
			case 2:
				sb.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.BA"));
				break;
			case 3:
				sb.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.CBA"));
				break;
			case 4:
				sb.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.DCBA"));
				break;
			case 5:
				sb.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.EDCBA"));
				break;
			}
		} else {
			sb.append("\n");

			switch (discipline) {
			case 0:
				sb.append(Language.tr("ModelGenerator.ServiceDiscipline")+": "+Language.tr("ModelGenerator.ServiceDiscipline.FIFO"));
				break;
			case 1:
				sb.append(Language.tr("ModelGenerator.ServiceDiscipline")+": "+Language.tr("ModelGenerator.ServiceDiscipline.LIFO"));
				break;
			case 2:
				sb.append(Language.tr("ModelGenerator.ServiceDiscipline")+": "+Language.tr("ModelGenerator.ServiceDiscipline.Random"));
				break;
			}
		}
		if (useWaitingTimeTolerance) {
			sb.append("\n\n");
			sb.append(Language.tr("ModelGenerator.WaitingTimeToleranceDistribution")+":\n");
			sb.append(Language.tr("ModelGenerator.WaitingTimeToleranceDistribution.Exp")+"\n");
			sb.append("E[WT]:="+NumberTools.formatNumber(waitingTimeTolerance)+" "+Language.tr("Statistics.Seconds")+"\n");
			sb.append("CV[WT]:=1\n");
		}
		label.setText(sb.toString());

		if (useWaitingTimeTolerance) {
			/* Nächste Spalte */
			xPosition+=200;

			for (int i=0;i<processes.length;i++) {
				counter[2*i]=new ModelElementCounter(model,model.surface);
				counter[2*i].setPosition(new Point(xPosition,processes[i].getPosition(true).y-50));
				counter[2*i].setName(Language.tr("ModelGenerator.Counter.Success"));
				counter[2*i].setGroupName(String.format(Language.tr("ModelGenerator.Counter.Group"),i+1));
				model.surface.add(counter[2*i]);

				counter[2*i+1]=new ModelElementCounter(model,model.surface);
				counter[2*i+1].setPosition(new Point(xPosition,processes[i].getPosition(true).y+50));
				counter[2*i+1].setName(Language.tr("ModelGenerator.Counter.Cancel"));
				counter[2*i+1].setGroupName(String.format(Language.tr("ModelGenerator.Counter.Group"),i+1));
				model.surface.add(counter[2*i+1]);
			}
		}

		/* Nächste Spalte */
		if ((stations==1 && useWaitingTimeTolerance) || (stations>1 && !useWaitingTimeTolerance)) {
			xPosition+=150;
		} else {
			xPosition+=200;
		}

		/* Ausgang */
		yPosition=50+50*(Math.max(clientTypes,stations));
		if (useWaitingTimeTolerance) yPosition+=(stations-1)*50;
		final ModelElementDispose dispose=new ModelElementDispose(model,model.surface);
		dispose.setPosition(new Point(xPosition,yPosition));
		model.surface.add(dispose);

		/* Kanten einfügen */
		if (decide==null) {
			for (ModelElementSource source: sources) addEdge(model,source,processes[0]);
		} else {
			for (ModelElementSource source: sources) addEdge(model,source,decide);
			addEdge(model,decide,processes[0]);
			addEdge(model,decide,processes[1]);
		}
		if (!useWaitingTimeTolerance) {
			for (ModelElementProcess process: processes) addEdge(model,process,dispose);
		} else {
			for (int i=0;i<processes.length;i++) {
				addEdge(model,processes[i],counter[2*i]);
				addEdge(model,processes[i],counter[2*i+1]);
				addEdge(model,counter[2*i],dispose);
				addEdge(model,counter[2*i+1],dispose);
			}
		}

		/* Visualisierung */
		if (addVisualization) {
			xPosition=50;

			/* Erst in drawToGraphics wird die Größe von Textfeldern berechnet, daher muss für ein valides Ergebnis in model.surface.getLowerRightModelCorner() das Modell einmal gezeichnet werden. */
			final JPanel dummy=new JPanel();
			add(dummy);
			model.surface.drawToGraphics(dummy.getGraphics(),new Rectangle(1000,1000),1.0,false,false,ModelSurface.Grid.OFF,new Color[] {Color.GRAY,Color.WHITE},false);
			remove(dummy);

			yPosition=model.surface.getLowerRightModelCorner().y+50;
			if (yPosition%50!=0) yPosition=((int)(yPosition/50.0))*50+50;

			model.surface.add(label=new ModelElementText(model,model.surface));
			label.setPosition(new Point(xPosition,yPosition-20));
			label.setText(Language.tr("ModelGenerator.Visualization.Title"));
			label.setTextItalic(true);

			final ModelElementAnimationLineDiagram diagram=new ModelElementAnimationLineDiagram(model,model.surface);
			diagram.setPosition(new Point(xPosition,yPosition));
			diagram.setBorderPointPosition(2,new Point(xPosition+550,yPosition+200));
			diagram.setBackgroundColor(new Color(240,240,240));
			diagram.setTimeArea(5*60*60);
			model.surface.add(diagram);

			final List<Object[]> list=new ArrayList<>();
			if (sources.length==1) {
				list.add(new Object[] {"WIP()",Integer.valueOf(0),Integer.valueOf(10),Color.BLUE,Integer.valueOf(1)});
				list.add(new Object[] {"WIP_avg()",Integer.valueOf(0),Integer.valueOf(10),Color.BLUE,Integer.valueOf(3)});
			} else {
				final Color[] colors=new Color[] {Color.BLUE,Color.RED,Color.GREEN,Color.GRAY,Color.ORANGE};
				for (int i=0;i<sources.length;i++) {
					list.add(new Object[] {"WIP("+sources[i].getId()+")",Integer.valueOf(0),Integer.valueOf(10),colors[i],Integer.valueOf(1)});
					list.add(new Object[] {"WIP_avg("+sources[i].getId()+")",Integer.valueOf(0),Integer.valueOf(10),colors[i],Integer.valueOf(3)});
				}
			}
			diagram.setExpressionData(list);
		}

		return model;
	}
}
