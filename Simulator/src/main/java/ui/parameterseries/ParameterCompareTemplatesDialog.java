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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTank;

/**
 * Dialog zur automatischen Erstellung von Parameterreiheneinstellungen
 * @author Alexander Herzog
 */
public class ParameterCompareTemplatesDialog extends BaseDialog {
	private static final long serialVersionUID = 3116092252907329358L;

	/**
	 * Mögliche Vorlagentypen
	 * @author Alexander Herzog
	 * @see ParameterCompareTemplatesDialog#getTemplates(EditModel)
	 */
	public enum TemplateMode {
		/**
		 * Zwischenankunftszeiten variieren
		 */
		MODE_INTERARRIVAL,

		/**
		 * Anzahl an Bedienern variieren
		 */
		MODE_OPERATORS,

		/**
		 * Bedienzeiten variieren
		 */
		MODE_SERVICETIMES,

		/**
		 * Initialwert für Variable variieren
		 */
		MODE_VARIABLES,

		/**
		 * Verzögerungszeiten variieren
		 */
		MODE_DELAY,

		/**
		 * Initialwert für Analogwert variieren
		 */
		MODE_ANALOG,

		/**
		 * Fließband-Transportzeit variieren
		 */
		MODE_CONVEYOR
	}

	private final ParameterCompareTemplatesDialog.TemplateMode mode;
	private final ParameterCompareSetupValueInput inputRecord;
	private final EditModel model;
	private final JCheckBox buildModels;
	private final JTextField editMin;
	private final JTextField editMax;
	private final JTextField editStep;

	/**
	 * Liefert den Namen zu einem Vorlagentyp
	 * @param mode	Vorlagentyp
	 * @return	Name des Vorlagentyps
	 */
	public static String getTemplateModeName(final TemplateMode mode) {
		switch (mode) {
		case MODE_INTERARRIVAL: return Language.tr("ParameterCompare.Mode.Interarrival");
		case MODE_OPERATORS: return Language.tr("ParameterCompare.Mode.Operators");
		case MODE_SERVICETIMES: return Language.tr("ParameterCompare.Mode.ServiceTimes");
		case MODE_VARIABLES: return Language.tr("ParameterCompare.Mode.Variables");
		case MODE_DELAY: return Language.tr("ParameterCompare.Mode.Delay");
		case MODE_ANALOG: return Language.tr("ParameterCompare.Mode.Analog");
		case MODE_CONVEYOR: return Language.tr("ParameterCompare.Mode.Conveyor");
		default: return "";
		}
	}

	/**
	 * Liefert das Icon zu einem Vorlagentyp
	 * @param mode	Vorlagentyp
	 * @return	Icon des Vorlagentyps (oder <code>null</code>, wenn kein Icon vorhanden ist)
	 */
	public static ImageIcon getTemplateModeIcon(final TemplateMode mode) {
		final URL imgURL;
		switch (mode) {
		case MODE_INTERARRIVAL: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_INTERARRIVAL.getURL(); break;
		case MODE_OPERATORS: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_OPERATORS.getURL(); break;
		case MODE_SERVICETIMES: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_SERVICETIMES.getURL(); break;
		case MODE_VARIABLES: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_VARIABLES.getURL(); break;
		case MODE_DELAY: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_DELAY.getURL(); break;
		case MODE_ANALOG: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_ANALOG.getURL(); break;
		case MODE_CONVEYOR: imgURL=Images.PARAMETERSERIES_TEMPLATE_MODE_CONVEYOR.getURL(); break;
		default: imgURL=null; break;
		}
		if (imgURL==null) return null;
		return new ImageIcon(imgURL);
	}

	private static List<ModelElementBox> getStations(final EditModel model) {
		final List<ModelElementBox> list=new ArrayList<>();
		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ModelElementBox) list.add((ModelElementBox)element1);
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementBox) list.add((ModelElementBox)element2);
			}
		}
		return list;
	}

	private static List<TemplateRecord> getTemplatesInterarrival(final EditModel model, final List<ModelElementBox> stations) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (ModelElementBox element: stations) if (element instanceof ModelElementSource) {
			final ModelElementSource source=(ModelElementSource)element;
			if (source.getRecord().getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) continue;
			if (!DistributionTools.canSetMean(source.getRecord().getInterarrivalTimeDistribution())) continue;

			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_INTERARRIVAL,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.InterarrivalTime"),source.getName()+" (id="+source.getId()+")")
					);
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setXMLMode(1);
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+source.getXMLNodeNames()[0]+"[id=\""+source.getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution"));

			list.add(record);
		}

		for (ModelElementBox element: stations) if (element instanceof ModelElementSourceMulti) {
			final ModelElementSourceMulti source=(ModelElementSourceMulti)element;
			for (int i=0;i<source.getRecords().size();i++) {
				if (source.getRecords().get(i).getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) continue;
				if (!DistributionTools.canSetMean(source.getRecords().get(i).getInterarrivalTimeDistribution())) continue;

				final TemplateRecord record=new TemplateRecord(
						TemplateMode.MODE_INTERARRIVAL,
						String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.InterarrivalTime"),source.getName()+" (id="+source.getId()+","+source.getRecords().get(i).getName()+")")
						);
				record.input.setMode(ModelChanger.Mode.MODE_XML);
				record.input.setXMLMode(1);
				record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+source.getXMLNodeNames()[0]+"[id=\""+source.getId()+"\"]->"+Language.trPrimary("Surface.SourceMulti.XML.Source")+"["+(i+1)+"]->"+Language.trPrimary("Surface.Source.XML.Distribution"));

				list.add(record);
			}
		}

		return list;
	}

	private static List<TemplateRecord> getTemplatesResources(final EditModel model) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (String name : model.resources.list()) {
			final ModelResource resource=model.resources.get(name);
			if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER) continue;

			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_OPERATORS,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.Resource"),name)
					);
			record.input.setMode(ModelChanger.Mode.MODE_RESOURCE);
			record.input.setTag(name);

			list.add(record);
		}

		return list;
	}

	private static boolean processHasMultiTimes(final ModelElementProcess process) {
		if (process.getWorking().getNames().length>0) return true;
		if (process.getPostProcessing().get()!=null) return true;
		if (process.getCancel().get()!=null) return true;
		if (process.getSetupTimes()!=null) return true;
		return false;
	}

	private static List<TemplateRecord> getTemplatesServiceTimes(final EditModel model, final List<ModelElementBox> stations) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (ModelElementBox element: stations) if (element instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)element;
			final Object obj=process.getWorking().get();
			if (obj==null || !(obj instanceof AbstractRealDistribution)) continue;
			if (!DistributionTools.canSetMean((AbstractRealDistribution)obj)) continue;

			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_SERVICETIMES,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.ProcessTime"),process.getName()+" (id="+process.getId()+")")
					);
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setXMLMode(1);
			String add="";
			if (processHasMultiTimes(process)) add="["+Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type")+"=\""+Language.trPrimary("Surface.Process.XML.Distribution.Type.ProcessingTime")+"\"]";
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+process.getXMLNodeNames()[0]+"[id=\""+process.getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);

			list.add(record);
		}

		return list;
	}

	private static List<TemplateRecord> getTemplatesVariables(final EditModel model) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (String name: model.globalVariablesNames) {
			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_VARIABLES,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.GlobalVariable"),name)
					);
			record.input.setMode(ModelChanger.Mode.MODE_VARIABLE);
			record.input.setTag(name);

			list.add(record);
		}

		return list;
	}

	private static boolean delayHasMultiTimes(final ModelElementDelay delay) {
		return delay.getSubDataCount()>0;
	}

	private static List<TemplateRecord> getTemplatesDelayTimes(final EditModel model, final List<ModelElementBox> stations) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (ModelElementBox element: stations) if (element instanceof ModelElementDelay) {
			final ModelElementDelay delay=(ModelElementDelay)element;
			final AbstractRealDistribution dist=delay.getDelayTime();
			if (dist==null) continue;
			if (!DistributionTools.canSetMean(dist)) continue;

			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_DELAY,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.DelayTime"),delay.getName()+" (id="+delay.getId()+")")
					);
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setXMLMode(1);
			String add="";
			if (delayHasMultiTimes(delay)) add="[1]";
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+delay.getXMLNodeNames()[0]+"[id=\""+delay.getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);

			list.add(record);
		}

		return list;
	}

	private static List<TemplateRecord> getTemplatesAnalogValues(final EditModel model, final List<ModelElementBox> stations) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (ModelElementBox element: stations) if ((element instanceof ModelElementAnalogValue) || (element instanceof ModelElementTank)) {
			String add=null;
			if (element instanceof ModelElementAnalogValue) add=Language.tr("Surface.XML.AnalogValue.InitialValue");
			if (element instanceof ModelElementTank) add=Language.tr("Surface.XML.Tank.InitialValue");
			if (add==null) continue;

			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_ANALOG,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.AnalogValue"),element.getName()+" (id="+element.getId()+")")
					);
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+element.getXMLNodeNames()[0]+"[id=\""+element.getId()+"\"]->"+add);

			list.add(record);
		}

		return list;
	}

	private static List<TemplateRecord> getTemplatesConveyor(final EditModel model, final List<ModelElementBox> stations) {
		final List<TemplateRecord> list=new ArrayList<>();

		for (ModelElementBox element: stations) if (element instanceof ModelElementConveyor) {
			final TemplateRecord record=new TemplateRecord(
					TemplateMode.MODE_CONVEYOR,
					String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.Conveyor"),element.getName()+" (id="+element.getId()+")")
					);
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+element.getXMLNodeNames()[0]+"[id=\""+element.getId()+"\"]->"+Language.trPrimary("Surface.Conveyor.XML.TransportTime"));

			list.add(record);
		}

		return list;
	}

	/**
	 * Liefert eine Liste aller verfügbaren Vorlagen für ein Modell
	 * @param model	Modell für das die Vorlagen aufgelistet werden sollen
	 * @return	Zuordnung vom Vorlagentypen zu Listen mit konkreten Vorlagen
	 */
	public static Map<TemplateMode,List<TemplateRecord>> getTemplates(final EditModel model) {
		final Map<TemplateMode,List<TemplateRecord>> map=new HashMap<>();
		final List<ModelElementBox> stations=getStations(model);

		map.put(TemplateMode.MODE_INTERARRIVAL,getTemplatesInterarrival(model,stations));
		map.put(TemplateMode.MODE_OPERATORS,getTemplatesResources(model));
		map.put(TemplateMode.MODE_SERVICETIMES,getTemplatesServiceTimes(model,stations));
		map.put(TemplateMode.MODE_VARIABLES,getTemplatesVariables(model));
		map.put(TemplateMode.MODE_DELAY,getTemplatesDelayTimes(model,stations));
		map.put(TemplateMode.MODE_ANALOG,getTemplatesAnalogValues(model,stations));
		map.put(TemplateMode.MODE_CONVEYOR,getTemplatesConveyor(model,stations));

		return map;
	}

	private String getDefaultParameterValue(final EditModel model, final ParameterCompareSetupValueInput record) {
		switch (record.getMode()) {
		case MODE_RESOURCE:
			final ModelResource resource=model.resources.get(record.getTag());
			if (resource!=null) return ""+resource.getCount();
			break;
		case MODE_VARIABLE:
			final int i=model.globalVariablesNames.indexOf(record.getTag());
			if (i>=0) return model.globalVariablesExpressions.get(i);
			break;
		case MODE_XML:
			final String value=ModelChanger.getValue(model,record.getTag(),record.getXMLMode());
			if (value!=null) return value;
			break;
		}
		return "";
	}

	private double calcStepWide(final double range) {
		if (range==0) return 1;
		return range/20;
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Basismodell
	 * @param record	Ausgewählter Vorlagendatensatz
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareTemplatesDialog(final Component owner, final EditModel model, final TemplateRecord record, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Templates.Title"));
		this.model=model;
		inputRecord=record.input;
		mode=record.mode;

		/* Daten aufbereiten */

		final String initialValueString=getDefaultParameterValue(model,record.input);
		final Double initialValue=NumberTools.getDouble(initialValueString);

		final String minValue;
		final String maxValue;
		final String stepValue;
		if (initialValue==null) {
			minValue=initialValueString;
			maxValue=initialValueString;
			stepValue="1";
		} else {
			double min=initialValue.doubleValue()/2;
			double max=initialValue.doubleValue()*2;
			if (max<min) {final double d=min; min=max; max=d;}
			final double stepWide=calcStepWide(max-min);
			if (record.mode==TemplateMode.MODE_OPERATORS) {
				long l;
				l=Math.round(min);
				if (l==0) l=1;
				minValue=NumberTools.formatLong(l);
				maxValue=NumberTools.formatLong(Math.round(max));
				l=Math.round(stepWide);
				if (l==0) l=1;
				stepValue=NumberTools.formatLong(l);
			} else {
				minValue=NumberTools.formatNumber(min);
				maxValue=NumberTools.formatNumber(max);
				stepValue=NumberTools.formatNumber(stepWide);
			}
		}

		/* GUI erstellen */

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		JPanel main=new JPanel();
		content.add(main,BorderLayout.NORTH);
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));

		/* Inhalte anlegen */

		JPanel line;

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("ParameterCompare.Templates.InputParameter")+": <b>"+record.menuName+"</b></body></html>"));

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(buildModels=new JCheckBox(Language.tr("ParameterCompare.Templates.BuildModels"),true));

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("ParameterCompare.Templates.CurrentValue")+": <b>"+initialValueString+"</b></body></html>"));

		Object[] data;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Templates.Minimum")+":",minValue,10);
		main.add((JPanel)data[0]);
		editMin=(JTextField)data[1];
		editMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Templates.Maximum")+":",maxValue,10);
		main.add((JPanel)data[0]);
		editMax=(JTextField)data[1];
		editMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Templates.Step")+":",stepValue,10);
		main.add((JPanel)data[0]);
		editStep=(JTextField)data[1];
		editStep.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Funktionalität verknüpfen */

		buildModels.addActionListener(e->{
			editMin.setEnabled(buildModels.isSelected());
			editMax.setEnabled(buildModels.isSelected());
			editStep.setEnabled(buildModels.isSelected());
		});

		/* Dialog starten */

		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		Double D;

		D=NumberTools.getDouble(editMin,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("ParameterCompare.Templates.Minimum.ErrorTitle"),String.format(Language.tr("ParameterCompare.Templates.Minimum.ErrorInfo"),editMin.getText()));
				return false;
			}
		}

		D=NumberTools.getDouble(editMax,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("ParameterCompare.Templates.Maximum.ErrorTitle"),String.format(Language.tr("ParameterCompare.Templates.Maximum.ErrorInfo"),editMax.getText()));
				return false;
			}
		}

		D=NumberTools.getPositiveDouble(editStep,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("ParameterCompare.Templates.Step.ErrorTitle"),String.format(Language.tr("ParameterCompare.Templates.Step.ErrorInfo"),editStep.getText()));
				return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Standardmäßige Ausgaben zu der Liste der Ausgabegrößen hinzufügen
	 * @param model	Editor-Modell (aus dem die Anzahlen an Ressourcengruppen ausgelesen werden)
	 * @param output	Ausgabegrößenliste zu der die neuen Ausgaben hinzugefügt werden sollen
	 * @param asTime	Zeitangaben als Zeiten formatieren (<code>true</code>) oder als Sekundenwerte (<code>false</code>) ausgeben
	 */
	public static void buildDefaultOutput(final EditModel model, final List<ParameterCompareSetupValueOutput> output, final boolean asTime) {
		final String mean="["+Language.tr("Statistics.XML.Mean")+"]";
		ParameterCompareSetupValueOutput outputRecord;

		outputRecord=new ParameterCompareSetupValueOutput();
		outputRecord.setName(Language.tr("Statistic.FastAccess.Template.ClientsInSystem"));
		outputRecord.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
		outputRecord.setTag(Language.tr("Statistics.XML.Element.ClientsInSystem")+mean);
		output.add(outputRecord);

		outputRecord=new ParameterCompareSetupValueOutput();
		outputRecord.setName(Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue"));
		outputRecord.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
		outputRecord.setTag(Language.tr("Statistics.XML.Element.ClientsInSystemWaiting")+mean);
		output.add(outputRecord);

		outputRecord=new ParameterCompareSetupValueOutput();
		outputRecord.setName(Language.tr("Statistic.FastAccess.Template.WaitingTime"));
		outputRecord.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
		outputRecord.setTag(Language.tr("Statistics.XML.Element.WaitingAllClients")+mean);
		outputRecord.setIsTime(asTime);
		output.add(outputRecord);

		outputRecord=new ParameterCompareSetupValueOutput();
		outputRecord.setName(Language.tr("Statistic.FastAccess.Template.ProcessTime"));
		outputRecord.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
		outputRecord.setTag(Language.tr("Statistics.XML.Element.ProcessAllClients")+mean);
		outputRecord.setIsTime(asTime);
		output.add(outputRecord);

		if (model.resources.getResources().length>0) {
			final String xmlMain=Language.tr("Statistics.XML.Element.Utilization");
			for (ModelResource resource: model.resources.getResources()) {
				outputRecord=new ParameterCompareSetupValueOutput();
				final String name=resource.getName();
				outputRecord.setName(Language.tr("Statistic.FastAccess.Template.ResourceUtilization")+" - "+name);
				outputRecord.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
				final String xmlSub=Language.tr("Statistics.XML.Element.UtilizationResource")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				outputRecord.setTag(xmlMain+"->"+xmlSub+"->"+mean);
				output.add(outputRecord);
			}
		}
	}

	/**
	 * Im Falle, dass der Dialog per "Ok" geschlossen wurde, kann über diese
	 * Funktion eine Parameterreiheneinstellung abgerufen werden.
	 * @return	Parameterreiheneinstellung auf Basis der Vorlage
	 */
	public ParameterCompareSetup buildParameterCompareSetup() {
		final ParameterCompareSetup setup=new ParameterCompareSetup(model);

		/* Eingabe */

		final List<ParameterCompareSetupValueInput> input=setup.getInput();
		input.add(inputRecord);

		/* Ausgabe */

		final List<ParameterCompareSetupValueOutput> output=setup.getOutput();
		buildDefaultOutput(model,output,true);

		/* Modelle */

		final List<ParameterCompareSetupModel> models=setup.getModels();
		ParameterCompareSetupModel modelRecord;
		final String inputName=inputRecord.getName();

		if (!buildModels.isSelected()) {
			final ParameterCompareSetupModel simModel=new ParameterCompareSetupModel();
			simModel.setName(Language.tr("ParameterCompare.Settings.BaseModel"));
			setup.getModels().add(simModel);
		} else {
			double min=NumberTools.getDouble(editMin,true).doubleValue();
			double max=NumberTools.getDouble(editMax,true).doubleValue();
			double step=NumberTools.getDouble(editStep,true).doubleValue();
			if (min>max) {final double d=min; min=max; max=d;}
			if (mode==ParameterCompareTemplatesDialog.TemplateMode.MODE_OPERATORS) {
				min=Math.round(min);
				max=Math.round(max);
				if (min<0) min=0;
				step=Math.round(step);
				if (step<=0) step=1;
			}

			int modelNr=1;
			double value=min;
			while (value<=max) {
				modelRecord=new ParameterCompareSetupModel();
				if (mode==ParameterCompareTemplatesDialog.TemplateMode.MODE_OPERATORS) {
					modelRecord.getInput().put(inputName,(double)Math.round(value));
				} else {
					modelRecord.getInput().put(inputName,value);
				}
				modelRecord.setName(String.format(Language.tr("ParameterCompare.Table.AddModelByAssistant.ModelName"),modelNr));
				models.add(modelRecord);
				value+=step;
				modelNr++;
			}
		}

		return setup;
	}

	/**
	 * Vorlage aus der eine Parameterreihen-Konfiguration aufgebaut werden kann
	 * @author Alexander Herzog
	 * @see ParameterCompareTemplatesDialog#getTemplates(EditModel)
	 */
	public static class TemplateRecord {
		/**
		 * Typ der Vorlage
		 */
		public final TemplateMode mode;

		/**
		 * Im Menü anzuzeigender Name
		 */
		public final String menuName;

		/**
		 * Zu variierender Eingabeparameter
		 */
		public final ParameterCompareSetupValueInput input;

		/**
		 * Konstruktor der Klasse
		 * @param mode	Typ der Vorlage
		 * @param menuName	Im Menü anzuzeigender Name
		 */
		public TemplateRecord(final TemplateMode mode, final String menuName) {
			this.mode=mode;
			this.menuName=menuName;
			input=new ParameterCompareSetupValueInput();
			input.setName(menuName);
		}
	}
}
