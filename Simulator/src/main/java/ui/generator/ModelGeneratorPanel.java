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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.AnimationExpression;
import ui.modeleditor.elements.AxisDrawer;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementTeleportSource;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Panel mit Einstellungen zur automatischen Modellerstellung
 * @see ModelGeneratorDialog
 * @author Alexander Herzog
 */
public class ModelGeneratorPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
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
	/** Weiterleitungen verwenden? */
	private final JCheckBox checkForwarding;
	/** Wiederholer (nach Warteabbruch)? */
	private final JCheckBox	checkRetry;
	/** Visualisierungen zur Anzeige der Anzahl an Kunden im System zum Modell hinzufügen? */
	private final JCheckBox checkAddWIPVisualization;
	/** Visualisierungen zur Anzeige der mittleren Auslastung zum Modell hinzufügen? */
	private final JCheckBox checkAddRhoVisualization;
	/** Visualisierungen zur Anzeige der mittleren Wartezeit zum Modell hinzufügen? */
	private final JCheckBox checkAddWVisualization;
	/** Visualisierungen zur Anzeige der mittleren Anzahl an Kunden in der Warteschlange zum Modell hinzufügen? */
	private final JCheckBox checkAddNQVisualization;
	/** Visualisierungen zur Anzeige der mittleren Anzahl an Kunden im System zum Modell hinzufügen? */
	private final JCheckBox checkAddNVisualization;

	/**
	 * Konstruktor der Klasse
	 * @param specialModelCallback	Callback zur Erzeugung von besonders großen Modellen (kann <code>null</code> sein, dann wird die Funktion deaktiviert)
	 */
	public ModelGeneratorPanel(final Runnable specialModelCallback) {
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
		checkRetry=addCheckBox(this,Language.tr("ModelGenerator.UseRetry"),false);
		checkRetry.setEnabled(false);
		checkWaitingTimeTolerance.addActionListener(e->{
			checkRetry.setEnabled(checkWaitingTimeTolerance.isSelected());
			if (!checkWaitingTimeTolerance.isSelected()) checkRetry.setSelected(false);
		});

		comboServiceDiscipline=addCombo(this,Language.tr("ModelGenerator.ServiceDiscipline"),new String[]{
				Language.tr("ModelGenerator.ServiceDiscipline.FIFO"),
				Language.tr("ModelGenerator.ServiceDiscipline.LIFO"),
				Language.tr("ModelGenerator.ServiceDiscipline.Random")
		});

		/* Bedienstation */
		addHeading(this,Language.tr("ModelGenerator.Process"),true);
		spinnerStationCount=addSpinner(this,Language.tr("ModelGenerator.StationCount"),1,5,1);
		comboServiceDistribution=addCombo(this,Language.tr("ModelGenerator.ServiceDistribution"),new String[]{
				Language.tr("ModelGenerator.ServiceDistribution.Deterministic"),
				Language.tr("ModelGenerator.ServiceDistribution.Exp"),
				Language.tr("ModelGenerator.ServiceDistribution.LogNormalLowCV"),
				Language.tr("ModelGenerator.ServiceDistribution.LogNormalHighCV"),
				Language.tr("ModelGenerator.ServiceDistribution.LogNormalVeryHighCV")
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
		checkSharedResource.setEnabled(false);
		checkForwarding=addCheckBox(this,Language.tr("ModelGenerator.UseForwarding"),false);

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
			checkSharedResource.setEnabled(stationCount>1);
			if (stationCount==1) checkSharedResource.setSelected(false);
			comboSelectQueue.setEnabled(stationCount>1);
		});
		checkSharedResource.addActionListener(e->{
			spinnerStationCount.setValue(Math.max(2,(int)spinnerStationCount.getValue()));
		});

		/* Visualisierung hinzufügen */
		addHeading(this,Language.tr("ModelGenerator.AddVisualization"),true);
		checkAddWIPVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.WIP"),true);
		checkAddRhoVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.MeanRho"),true);
		checkAddWVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.MeanWaitingTime"),true);
		checkAddNQVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.MeanNQ"),true);
		checkAddNVisualization=addCheckBox(this,Language.tr("ModelGenerator.AddVisualization.MeanN"),true);

		/* Besondere Modelle */
		if (specialModelCallback!=null) {
			add(Box.createVerticalStrut(30));
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			add(line);
			final JButton specialModels=new JButton(Language.tr("ModelGenerator.LargeModel.Button"),Images.GENERAL_TOOLS.getIcon());
			line.add(specialModels);
			specialModels.addActionListener(e->specialModelCallback.run());
		}
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

	/**
	 * Zwischenüberschrift anzeigen
	 * @param panel	Panel in das die Überschrift eingefügt werden soll
	 * @param text	Text der Überschrift
	 * @param spaceBefore	Vertikalen Abstand vor der Überschrift einfügen?
	 */
	private void addHeading(final JPanel panel, final String text, final boolean spaceBefore) {
		if (spaceBefore) panel.add(Box.createVerticalStrut(15));

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		line.add(new JLabel("<html><body><b>"+text+"</b></body></html>"));
	}

	/**
	 * Auswahlfeld einfügen
	 * @param panel	Panel in das das Auswahlfeld eingefügt werden soll
	 * @param text	Beschriftung des Auswahlfeldes
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param value	Initialer Wert
	 * @return	Neues Auswahlfeld
	 */
	private SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value) {
		return addSpinner(panel,text,min,max,value,null);
	}

	/**
	 * Auswahlfeld einfügen
	 * @param panel	Panel in das das Auswahlfeld eingefügt werden soll
	 * @param text	Beschriftung des Auswahlfeldes
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param value	Initialer Wert
	 * @param info	Optionaler Infotext nach dem Feld (kann <code>null</code> sein)
	 * @return	Neues Auswahlfeld
	 */
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

	/**
	 * Checkbox einfügen
	 * @param panel	Panel in das die Checkbox eingefügt werden soll
	 * @param text	Beschriftung der Checkbox
	 * @param selected	Soll die Checkbox anfänglich markiert sein?
	 * @return	Neue Checkbox
	 */
	private JCheckBox addCheckBox(final JPanel panel, final String text, final boolean selected) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JCheckBox checkBox=new JCheckBox(text,selected);
		line.add(checkBox);
		checkBox.addActionListener(e->fireModelChanged());
		return checkBox;
	}

	/**
	 * Combobox einfügen
	 * @param panel	Panel in das die Combobox eingefügt werden soll
	 * @param text	Beschriftung der Combobox
	 * @param options	Auswahloptionen in der Combobox
	 * @return	Neue Combobox
	 */
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

	/**
	 * Fügt in dem Modell eine Verbindungskante zwischen zwei Stationen ein
	 * @param model	Modell bei dem die Kante auf der Hauptzeichenfläche eingefügt werden soll
	 * @param station1	Ausgangsstation
	 * @param station2	Zielstation
	 * @see #getModel()
	 */
	private static void addEdge(final EditModel model, final ModelElementPosition station1, final ModelElementPosition station2) {
		addEdge(model,station1,station2,false);
	}

	/**
	 * Fügt in dem Modell eine Verbindungskante zwischen zwei Stationen ein
	 * @param model	Modell bei dem die Kante auf der Hauptzeichenfläche eingefügt werden soll
	 * @param station1	Ausgangsstation
	 * @param station2	Zielstation
	 * @param direct	 Soll die Verbindungskante normal (<code>false</code>) oder zwingend als gerade Linie (<code>true</code>) gezeichnet werden?
	 * @see #getModel()
	 */
	private static void addEdge(final EditModel model, final ModelElementPosition station1, final ModelElementPosition station2, final boolean direct) {
		final ModelElementEdge edge=new ModelElementEdge(model,model.surface,station1,station2);
		station1.addEdgeOut(edge);
		station2.addEdgeIn(edge);
		if (direct) edge.setLineMode(ModelElementEdge.LineMode.DIRECT);
		model.surface.add(edge);
	}

	/**
	 * Fügt ein Simulationsdatenausgabetextfeld in das Modell ein
	 * @param model	Modell in das das Textfeld eingefügt werden soll
	 * @param xPosition	X-Position auf der Zeichenfläche
	 * @param yPosition	Y-Position auf der Zeichenfläche
	 * @param labelText	Beschriftungstext
	 * @param labelColor	Farbe für den Beschriftungstext
	 * @param dataText	Rechenausdruck dessen Ergebnis angezeigt werden soll
	 */
	private static void addSimDataText(final EditModel model, final int xPosition, final int yPosition, final String labelText, final Color labelColor, final String dataText) {
		final ModelElementText label;
		model.surface.add(label=new ModelElementText(model,model.surface));
		label.setPosition(new Point(xPosition,yPosition-20));
		label.setText(labelText);
		label.setTextItalic(true);

		final ModelElementAnimationTextValue textValue=new ModelElementAnimationTextValue(model,model.surface);
		textValue.setPosition(new Point(xPosition,yPosition));
		textValue.setTextSize(14);
		textValue.setTextBold(true);
		textValue.setColor(labelColor);
		textValue.setDigits(1);
		textValue.setExpression(dataText);
		model.surface.add(textValue);
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
		final boolean useRetry=checkRetry.isSelected();
		final boolean useForwarding=checkForwarding.isSelected();
		final boolean addWIPVisualization=checkAddWIPVisualization.isSelected();
		final boolean addRhoVisualization=checkAddRhoVisualization.isSelected();
		final boolean addWVisualization=checkAddWVisualization.isSelected();
		final boolean addNQVisualization=checkAddNQVisualization.isSelected();
		final boolean addNVisualization=checkAddNVisualization.isSelected();


		StringBuilder description;
		ModelElementText label;
		final ModelElementSource[] sources=new ModelElementSource[clientTypes];
		ModelElementVertex sourcesVertex=null;
		ModelElementDelay retryDelay=null;
		ModelElementTeleportDestination retryTeleportDestination=null;
		ModelElementTeleportDestination forwardingTeleportDestination=null;
		final ModelElementProcess[] processes=new ModelElementProcess[stations];
		final ModelElementCounter[] counter=new ModelElementCounter[stations*2];
		final ModelElementDecide[] retryDecide=new ModelElementDecide[stations];
		final ModelElementTeleportSource[] retryTeleportSource=new ModelElementTeleportSource[stations];
		final ModelElementVertex[] disposeVertex=new ModelElementVertex[stations];
		ModelElementDecide forwardingDecide=null;
		ModelElementTeleportSource forwardingTeleportSource=null;
		ModelElementVertex forwardingCancelVertex=null;

		/* Daten berechnen */
		final double interArrivalTime=70*clientTypes;
		double serviceTimeBase=60;
		switch (utilization) {
		case 0: serviceTimeBase=50; break;
		case 1: serviceTimeBase=60; break;
		case 2: serviceTimeBase=65; break;
		}
		if (useForwarding) serviceTimeBase-=5;
		final double serviceTime=((stations==1 || sharedResource)?1:stations)*serviceTimeBase;
		final int resourceGroups=(stations==1 || sharedResource)?1:stations;
		final double waitingTimeTolerance=600;

		/* Modell anlegen */
		final EditModel model=new EditModel();
		model.name=Language.tr("ModelGenerator.Model.Name");

		/* Beschreibung */
		description=new StringBuilder();
		description.append(Language.tr("ModelGenerator.Model.Description"));
		description.append("\n\n");
		description.append(Language.tr("ModelGenerator.Model.Description.Properties")+":");
		if (clientTypes>1) description.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.ClientTypes"),clientTypes));
		if (priorities) description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Priorities"));
		if (arrivalBatch>1) description.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.ArrivalBatch"),arrivalBatch));
		if (serviceBatch>1) description.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.ServiceBatch"),serviceBatch));
		switch (serviceDistribution) {
		case 0:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceDeterministic"));
			break;
		case 1:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceExp"));
			break;
		case 2:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLognormal"));
			break;
		case 3:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLognormalHighCV"));
			break;
		case 4:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLognormalVeryHighCV"));
			break;
		}
		switch (discipline) {
		case 0:
			/* Kein besonderer Hinweis auf FIFO */
			break;
		case 1:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceLIFO"));
			break;
		case 2:
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.ServiceRandom"));
			break;
		}
		if (stations>1) {
			description.append("\n- "+String.format(Language.tr("ModelGenerator.Model.Description.Properties.MultiStations"),stations));
			if (shortestQueue) {
				description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Queue.Shortest"));
			} else {
				description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Queue.Random"));
			}
			if (sharedResource) description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.SharedResource"));
		}
		if (useWaitingTimeTolerance) {
			description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.LimitedWaitingTimeTolerance"));
			if (useRetry) description.append("\n- "+Language.tr("ModelGenerator.Model.Description.Properties.Retry"));
		}
		model.description=description.toString();

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
		final int yPositionCenter=50+50*(Math.max(clientTypes,stations))+(((useWaitingTimeTolerance && stations>1) || (useForwarding && clientTypes==1 && stations==1))?50:0);

		/* Quellen */
		if (clientTypes>=stations) yPosition=100; else yPosition=100+50*stations-50*clientTypes;
		if ((useForwarding && clientTypes==1) || (useWaitingTimeTolerance && stations>1)) yPosition+=50;
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
		description=new StringBuilder();
		description.append(Language.tr("Statistics.InterArrivalTimes")+":\nE[I]:="+NumberTools.formatNumber(interArrivalTime)+" "+Language.tr("Statistics.Seconds"));
		if (arrivalBatch>1) {
			description.append("\n\n");
			description.append(Language.tr("ModelGenerator.ArrivalBatch")+":\nb:="+arrivalBatch);
		}
		label.setText(description.toString());

		if (useRetry || useForwarding) {
			/* Nächste Spalte */
			if (sources.length==5) {
				xPosition+=300;
			} else {
				xPosition+=250;
			}

			/* Knotenpunkt für Erstanrufer und Wiederholer und Weiterleitungen */
			model.surface.add(sourcesVertex=new ModelElementVertex(model,model.surface));
			yPosition=yPositionCenter+20;
			sourcesVertex.setPosition(new Point(xPosition-5,yPosition));

			if (useRetry) {
				model.surface.add(retryDelay=new ModelElementDelay(model,model.surface));
				retryDelay.setDelayTime(new ExponentialDistribution(900),null);
				yPosition=yPositionCenter+100;
				retryDelay.setPosition(new Point(xPosition-50,yPosition));

				model.surface.add(retryTeleportDestination=new ModelElementTeleportDestination(model,model.surface));
				retryTeleportDestination.setName(Language.tr("ModelGenerator.RetryTeleportDestinationName"));
				yPosition=yPositionCenter+200;
				retryTeleportDestination.setPosition(new Point(xPosition-15,yPosition));
			}

			if (useForwarding) {
				model.surface.add(forwardingTeleportDestination=new ModelElementTeleportDestination(model,model.surface));
				forwardingTeleportDestination.setName(Language.tr("ModelGenerator.ForwardingTeleportDestinationName"));
				yPosition=yPositionCenter-50;
				forwardingTeleportDestination.setPosition(new Point(xPosition-15,yPosition));
			}

			/* Nächste Spalte */
			xPosition+=100;
		} else {
			/* Nächste Spalte */
			xPosition+=250;
		}

		/* Verzweigen? */
		final ModelElementDecide decide;
		if (stations>1) {
			model.surface.add(decide=new ModelElementDecide(model,model.surface));
			yPosition=yPositionCenter;
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
			description=new StringBuilder();
			description.append(Language.tr("ModelGenerator.SelectQueue")+":\n");
			if (shortestQueue) {
				description.append(Language.tr("ModelGenerator.SelectQueue.Shortest"));
			} else {
				description.append(Language.tr("ModelGenerator.SelectQueue.Random"));
			}
			label.setText(description.toString());

			/* Nächste Spalte */
			xPosition+=200;
		} else {
			decide=null;
		}

		/* Bedienstationen */
		if (clientTypes>stations) yPosition=100+(clientTypes-stations)*50; else yPosition=100;
		if (clientTypes==1 && stations==1 && useForwarding) yPosition+=50;
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
			case 4:
				processes[i].getWorking().set(new LogNormalDistributionImpl(serviceTime,3*serviceTime/2));
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
		description=new StringBuilder();
		description.append(Language.tr("ModelGenerator.ServiceDistribution")+":\n");
		double cvS=0;
		switch (serviceDistribution) {
		case 0:
			description.append(Language.tr("ModelGenerator.ServiceDistribution.Deterministic")+"\n");
			cvS=0;
			break;
		case 1:
			description.append(Language.tr("ModelGenerator.ServiceDistribution.Exp")+"\n");
			cvS=1;
			break;
		case 2:
			cvS=0.25;
			description.append(Language.tr("ModelGenerator.ServiceDistribution.LogNormal")+"\n");
			break;
		case 3:
			description.append(Language.tr("ModelGenerator.ServiceDistribution.LogNormal")+"\n");
			cvS=0.75;
			break;
		case 4:
			description.append(Language.tr("ModelGenerator.ServiceDistribution.LogNormal")+"\n");
			cvS=1.5;
			break;
		}
		description.append("E[S]:="+NumberTools.formatNumber(serviceTime)+" "+Language.tr("Statistics.Seconds")+"\n");
		description.append("CV[S]:="+NumberTools.formatNumber(cvS,2)+"\n");
		if (stations==1) {
			description.append(Language.tr("ModelGenerator.NumberOfOperators")+": c:=1");
		} else {
			if (sharedResource) {
				description.append(Language.tr("ModelGenerator.NumberOfOperators")+": c:=1 ("+Language.tr("ModelGenerator.NumberOfOperators.shared")+")");
			} else {
				description.append(Language.tr("ModelGenerator.NumberOfOperators")+": c:=1 ("+Language.tr("ModelGenerator.NumberOfOperators.perStation")+")");
			}
		}
		if (serviceBatch>1) {
			description.append("\n");
			description.append(Language.tr("ModelGenerator.ServiceBatchSize")+": b="+serviceBatch);
		}
		if (priorities) {
			description.append("\n");
			switch (clientTypes) {
			case 2:
				description.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.BA"));
				break;
			case 3:
				description.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.CBA"));
				break;
			case 4:
				description.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.DCBA"));
				break;
			case 5:
				description.append(Language.tr("ModelGenerator.PrioritiesStrategy")+": "+Language.tr("ModelGenerator.PrioritiesStrategy.EDCBA"));
				break;
			}
		} else {
			description.append("\n");

			switch (discipline) {
			case 0:
				description.append(Language.tr("ModelGenerator.ServiceDiscipline")+": "+Language.tr("ModelGenerator.ServiceDiscipline.FIFO"));
				break;
			case 1:
				description.append(Language.tr("ModelGenerator.ServiceDiscipline")+": "+Language.tr("ModelGenerator.ServiceDiscipline.LIFO"));
				break;
			case 2:
				description.append(Language.tr("ModelGenerator.ServiceDiscipline")+": "+Language.tr("ModelGenerator.ServiceDiscipline.Random"));
				break;
			}
		}
		if (useWaitingTimeTolerance) {
			description.append("\n\n");
			description.append(Language.tr("ModelGenerator.WaitingTimeToleranceDistribution")+":\n");
			description.append(Language.tr("ModelGenerator.WaitingTimeToleranceDistribution.Exp")+"\n");
			description.append("E[WT]:="+NumberTools.formatNumber(waitingTimeTolerance)+" "+Language.tr("Statistics.Seconds")+"\n");
			description.append("CV[WT]:=1");
			if (useRetry) {
				description.append("\n\n");
				description.append(Language.tr("ModelGenerator.RetryProbability")+" P(Retry)=25%");
				description.append(Language.tr("ModelGenerator.RetryDelayDistribution")+":\n");
				description.append(Language.tr("ModelGenerator.RetryDelayDistribution.Exp")+"\n");
				description.append("E[Retry]:="+NumberTools.formatNumber(900)+" "+Language.tr("Statistics.Seconds")+"\n");
				description.append("CV[Retry]:=1");
			}
		}
		label.setText(description.toString());

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

				if (useRetry && retryTeleportDestination!=null) {
					model.surface.add(retryDecide[i]=new ModelElementDecide(model,model.surface));
					retryDecide[i].setMode(ModelElementDecide.DecideMode.MODE_CHANCE);
					retryDecide[i].getRates().clear();
					retryDecide[i].getRates().add(NumberTools.formatNumberMax(0.75));
					retryDecide[i].getRates().add(NumberTools.formatNumberMax(0.25));
					retryDecide[i].setPosition(new Point(xPosition+150,processes[i].getPosition(true).y+50));

					model.surface.add(retryTeleportSource[i]=new ModelElementTeleportSource(model,model.surface));
					retryTeleportSource[i].setDestination(retryTeleportDestination.getName());
					retryTeleportSource[i].setPosition(new Point(xPosition+370,processes[i].getPosition(true).y+60));

					model.surface.add(disposeVertex[i]=new ModelElementVertex(model,model.surface));
					disposeVertex[i].setPosition(new Point(xPosition+370,processes[i].getPosition(true).y-30));
				}
			}
		}

		/* Nächste Spalte */
		if ((stations==1 && useWaitingTimeTolerance) || (stations>1 && !useWaitingTimeTolerance)) {
			xPosition+=150;
		} else {
			xPosition+=200;
		}
		if (useRetry) xPosition+=300;

		/* Weiterleitungen? */
		if (useForwarding && forwardingTeleportDestination!=null) {
			yPosition=50+50*(Math.max(clientTypes,stations));
			if (clientTypes==1 && stations==1 && useForwarding) yPosition+=50;
			if (useWaitingTimeTolerance) {
				yPosition+=(stations-1)*50;
				if (useRetry && stations==1) yPosition-=50;
			}

			model.surface.add(forwardingDecide=new ModelElementDecide(model,model.surface));
			forwardingDecide.setMode(ModelElementDecide.DecideMode.MODE_CHANCE);
			forwardingDecide.getRates().clear();
			forwardingDecide.getRates().add(NumberTools.formatNumberMax(0.1));
			forwardingDecide.getRates().add(NumberTools.formatNumberMax(0.9));
			forwardingDecide.setPosition(new Point(xPosition,yPosition+(useWaitingTimeTolerance?-50:0)));

			model.surface.add(forwardingTeleportSource=new ModelElementTeleportSource(model,model.surface));
			forwardingTeleportSource.setDestination(forwardingTeleportDestination.getName());
			forwardingTeleportSource.setPosition(new Point(xPosition+35,yPosition-80+(useWaitingTimeTolerance?-50:0)));

			if (useWaitingTimeTolerance) {
				model.surface.add(forwardingCancelVertex=new ModelElementVertex(model,model.surface));
				forwardingCancelVertex.setPosition(new Point(xPosition+45,yPosition+70));
			}

			xPosition+=200;
		}

		/* Ausgang */
		yPosition=50+50*(Math.max(clientTypes,stations));
		if (clientTypes==1 && stations==1 && useForwarding) yPosition+=50;
		if (useWaitingTimeTolerance) {
			yPosition+=(stations-1)*50;
			if (useRetry && stations==1) yPosition-=50;
		}
		final ModelElementDispose dispose=new ModelElementDispose(model,model.surface);
		dispose.setPosition(new Point(xPosition,yPosition));
		model.surface.add(dispose);

		/* Kanten einfügen */
		if (decide==null) {
			if (sourcesVertex==null) {
				for (ModelElementSource source: sources) addEdge(model,source,processes[0]);
			} else {
				for (ModelElementSource source: sources) addEdge(model,source,sourcesVertex);
				addEdge(model,sourcesVertex,processes[0]);
				if (useRetry) {
					addEdge(model,retryDelay,sourcesVertex);
					addEdge(model,retryTeleportDestination,retryDelay);
				}
				if (useForwarding) {
					addEdge(model,forwardingTeleportDestination,sourcesVertex);
				}
			}
		} else {
			if (sourcesVertex==null) {
				for (ModelElementSource source: sources) addEdge(model,source,decide);
			} else {
				for (ModelElementSource source: sources) addEdge(model,source,sourcesVertex);
				addEdge(model,sourcesVertex,decide);
				if (useRetry) {
					addEdge(model,retryDelay,sourcesVertex);
					addEdge(model,retryTeleportDestination,retryDelay);
				}
				if (useForwarding) {
					addEdge(model,forwardingTeleportDestination,sourcesVertex);
				}
			}
			for (int i=0;i<processes.length;i++) {
				addEdge(model,decide,processes[i],processes.length>2);
			}
		}
		final ModelElementPosition disposeLikeSuccess=useForwarding?forwardingDecide:dispose;
		final ModelElementPosition disposeLikeCancel=useForwarding?forwardingCancelVertex:dispose;
		if (!useWaitingTimeTolerance) {
			for (ModelElementProcess process: processes) addEdge(model,process,disposeLikeSuccess,processes.length>2);
		} else {
			for (int i=0;i<processes.length;i++) {
				addEdge(model,processes[i],counter[2*i]);
				addEdge(model,processes[i],counter[2*i+1]);
				if (useRetry) {
					addEdge(model,counter[2*i],disposeVertex[i]);
					addEdge(model,counter[2*i+1],retryDecide[i]);
					if (useForwarding) {
						addEdge(model,retryDecide[i],disposeLikeCancel,true);
					} else {
						addEdge(model,retryDecide[i],disposeVertex[i],true);
					}
					addEdge(model,retryDecide[i],retryTeleportSource[i]);
					addEdge(model,disposeVertex[i],disposeLikeSuccess,processes.length>1);
				} else {
					addEdge(model,counter[2*i],disposeLikeSuccess,processes.length>1);
					addEdge(model,counter[2*i+1],disposeLikeCancel,processes.length>1);
				}
				if (disposeLikeCancel!=dispose) addEdge(model,disposeLikeCancel,dispose);
			}
		}
		if (useForwarding) {
			addEdge(model,forwardingDecide,forwardingTeleportSource);
			addEdge(model,forwardingDecide,dispose);
		}

		/* Erst in drawToGraphics wird die Größe von Textfeldern berechnet, daher muss für ein valides Ergebnis in model.surface.getLowerRightModelCorner() das Modell einmal gezeichnet werden. */
		final JPanel dummy=new JPanel();
		add(dummy);
		model.surface.drawToGraphics(dummy.getGraphics(),new Rectangle(1000,1000),1.0,true,ModelSurface.BackgroundImageMode.OFF,false,ModelSurface.Grid.OFF,new Color[] {Color.GRAY,Color.WHITE},null,null,1.0,false);
		remove(dummy);

		xPosition=50;
		yPosition=model.surface.getLowerRightModelCorner().y+100;
		if (yPosition%50!=0) yPosition=((int)(yPosition/50.0))*50+50;

		/* Visualisierung */

		if (addWIPVisualization) {
			model.surface.add(label=new ModelElementText(model,model.surface));
			label.setPosition(new Point(xPosition,yPosition-20));
			label.setText(Language.tr("ModelGenerator.Visualization.Title"));
			label.setTextItalic(true);

			final ModelElementAnimationLineDiagram diagram=new ModelElementAnimationLineDiagram(model,model.surface);
			diagram.setPosition(new Point(xPosition,yPosition));
			diagram.setBorderPointPosition(2,new Point(xPosition+650,yPosition+200));
			diagram.setBackgroundColor(Color.WHITE);
			diagram.setGradientFillColor(new Color(230,230,250));
			diagram.setTimeArea(5*60*60);
			diagram.setXAxisLabels(AxisDrawer.Mode.FULL);
			diagram.setYAxisLabels(AxisDrawer.Mode.FULL);
			model.surface.add(diagram);

			final List<Object[]> list=new ArrayList<>();
			final int maxY=(utilization==2)?20:10;
			if (sources.length==1) {
				list.add(new Object[] {new AnimationExpression("WIP()"),Integer.valueOf(0),Integer.valueOf(maxY),Color.BLUE,Integer.valueOf(1)});
				list.add(new Object[] {new AnimationExpression("WIP_avg()"),Integer.valueOf(0),Integer.valueOf(maxY),Color.BLUE,Integer.valueOf(3)});
			} else {
				final Color[] colors=new Color[] {Color.BLUE,Color.RED,Color.GREEN,Color.GRAY,Color.ORANGE};
				for (int i=0;i<sources.length;i++) {
					list.add(new Object[] {new AnimationExpression("WIP("+sources[i].getId()+")"),Integer.valueOf(0),Integer.valueOf(maxY),colors[i],Integer.valueOf(1)});
					list.add(new Object[] {new AnimationExpression("WIP_avg("+sources[i].getId()+")"),Integer.valueOf(0),Integer.valueOf(maxY),colors[i],Integer.valueOf(3)});
				}
			}
			diagram.setExpressionData(list);

			model.surface.add(label=new ModelElementText(model,model.surface));
			label.setPosition(new Point(xPosition,yPosition+215));
			label.setText(Language.tr("ModelGenerator.Visualization.WIPDiagramInfo"));
			label.setTextSize(11);

			xPosition+=700;
		}

		if (addRhoVisualization) {
			addSimDataText(model,xPosition,yPosition,Language.tr("ModelGenerator.Visualization.MeanRhoTitle"),Color.MAGENTA,"Resource_avg()/Resource_count()");
			yPosition+=50;
		}

		if (addWVisualization) {
			addSimDataText(model,xPosition,yPosition,Language.tr("ModelGenerator.Visualization.MeanWaitingTimeTitle"),Color.RED,"WaitingTime_avg()");
			yPosition+=50;
		}

		if (addNQVisualization) {
			addSimDataText(model,xPosition,yPosition,Language.tr("ModelGenerator.Visualization.MeanNQTitle"),Color.BLUE,"NQ_avg()");
			yPosition+=50;
		}

		if (addNVisualization) {
			addSimDataText(model,xPosition,yPosition,Language.tr("ModelGenerator.Visualization.MeanNTitle"),Color.ORANGE,"N_avg()");
			yPosition+=50;
		}

		return model;
	}

	/**
	 * Erzeugt ein großes Testmodell zur Untersuchung des Verhaltens des Simulators
	 * bei Modellen aus sehr vielen Stationen.
	 * @param clientCount	Zu simulierende Kundenankünfte
	 * @param stationCount	Anzahl an Stationen
	 * @param useProcessStations	Sollen Bedienstationen (<code>true</code>) oder Verzögerungsstationen (<code>false</code>) verwendet werden?
	 * @return	Testmodell
	 */
	public static EditModel getLargeModel(final long clientCount, final int stationCount, final boolean useProcessStations) {
		final double meanInterArrivalTime=100;
		final double delayTime=1;
		final double meanServiceTime=10;
		final int stationsPerRow=30;

		/* Modell anlegen */
		final EditModel model=new EditModel();
		model.name=Language.tr("ModelGenerator.LargeModel.Description");
		model.clientCount=clientCount;
		model.warmUpTime=0.0;
		model.distributionRecordHours=0;
		model.distributionRecordClientDataValues=0;

		/* Überschrift */
		final ModelElementText label=new ModelElementText(model,model.surface);
		model.surface.add(label);
		label.setPosition(new Point(50,50));
		label.setText(Language.tr("ModelGenerator.Model.Name"));
		label.setTextBold(true);
		label.setTextSize(label.getTextSize()+2);

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
}
