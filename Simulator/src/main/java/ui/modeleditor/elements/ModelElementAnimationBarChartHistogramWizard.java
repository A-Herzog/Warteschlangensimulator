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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseColorDialog;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelTransporter;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Zeigt einen Dialog zum automatischen Anlegen von mehreren Histogrammbalken an.
 * @author Alexander Herzog
 * @see ModelElementAnimationBarChartDialog
 */
public class ModelElementAnimationBarChartHistogramWizard extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4525789972511231412L;

	private final Runnable helpRunnable;

	private final List<HistogramType> types;
	private final List<HistogramIDRecord> idsBox;
	private final List<HistogramIDRecord> idsResource;
	private final List<HistogramIDRecord> idsTransporter;
	private final List<HistogramIDRecord> idsStatistics;

	private final JComboBox<String> comboType;
	private final JComboBox<String> comboID;
	private final JTextField editNr;
	private final JTextField editStart;
	private final JTextField editStepWidth;
	private final JTextField editCount;
	private final JButton buttonColor;
	private final JCheckBox replaceRecords;

	private Color barColor;
	private int lastType;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell
	 * @param help	Hilfe-Callback
	 */
	public ModelElementAnimationBarChartHistogramWizard(final Component owner, final EditModel model, final Runnable help) {
		super(owner,Language.tr("Surface.AnimationBarChart.HistogramWizard.Title"));

		helpRunnable=help;
		barColor=Color.BLUE;

		/* Daten vorbereiten: BoxElement IDs */

		idsBox=getBoxElements(model);

		/* Daten vorbereiten: Ressourcen IDs */

		idsResource=getResources(model);

		/* Daten vorbereiten: Transporter IDs */

		idsTransporter=getTransporters(model);

		/* Daten vorbereiten: Statistik-Station IDs */

		idsStatistics=getStatisticElements(model);

		/* Daten vorbereiten: Typen */

		types=new ArrayList<>();

		if (idsBox.size()>0) {
			types.add(new HistogramType(Language.tr("ExpressionBuilder.ClientsAtStation"),"WIP_hist",HistogramID.ID_BOX_ELEMENT));
			types.add(new HistogramType(Language.tr("ExpressionBuilder.ClientsAtQueue"),"NQ_hist",HistogramID.ID_BOX_ELEMENT));

			types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations"),Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_hist",HistogramID.ID_BOX_ELEMENT));
			types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations"),Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_hist",HistogramID.ID_BOX_ELEMENT));
			types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations"),Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_hist",HistogramID.ID_BOX_ELEMENT));
		}

		types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll"),Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_histAll",HistogramID.ID_NON));
		types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll"),Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_histAll",HistogramID.ID_NON));
		types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll"),Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_histAll",HistogramID.ID_NON));

		if (idsResource.size()>0) {
			types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization"),Language.tr("ExpressionBuilder.CommandName.Resource")+"_hist",HistogramID.ID_RESOURCE));
		}

		if (idsTransporter.size()>0) {
			types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization"),Language.tr("ExpressionBuilder.CommandName.Transporter")+"_hist",HistogramID.ID_TRANSPORTER));
		}

		if (idsStatistics.size()>0) {
			types.add(new HistogramType(Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics"),Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_hist",HistogramID.ID_STATISTIC_STATION));
		}
		/* GUI aufbauen */

		final JPanel content=createGUI(500,400,help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] data;
		JPanel line;
		JLabel label;

		/* Auswahl */

		comboType=addCombo(content,Language.tr("Surface.AnimationBarChart.HistogramWizard.Type")+":",types.stream().map(type->type.name).collect(Collectors.toList()));
		comboID=addCombo(content,Language.tr("Surface.AnimationBarChart.HistogramWizard.ID")+":",null);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarChart.HistogramWizard.Nr")+":","0",5);
		content.add((JPanel)data[0]);
		editNr=(JTextField)data[1];
		editNr.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Balken */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarChart.HistogramWizard.Start")+":","0",5);
		content.add((JPanel)data[0]);
		editStart=(JTextField)data[1];
		editStart.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarChart.HistogramWizard.StepWidth")+":","5",5);
		content.add((JPanel)data[0]);
		editStepWidth=(JTextField)data[1];
		editStepWidth.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationBarChart.HistogramWizard.Count")+":","20",5);
		content.add((JPanel)data[0]);
		editCount=(JTextField)data[1];
		editCount.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Farbe */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationBarChart.HistogramWizard.SelectColor")+":"));
		line.add(buttonColor=new JButton());
		buttonColor.setPreferredSize(new Dimension(26,26));
		setupColorButton();
		buttonColor.addActionListener(e->showColorSelectDialog());
		label.setLabelFor(buttonColor);

		/* Überschreiben */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(replaceRecords=new JCheckBox(Language.tr("Surface.AnimationBarChart.HistogramWizard.ReplaceRecords")));

		/* Verarbeitung starten */

		lastType=-1;
		comboType.setSelectedIndex(0);
		comboType.addActionListener(e->typeChanged());
		typeChanged();
		checkData(false);

		/* Anzeigen */

		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private JComboBox<String> addCombo(final JPanel parent, final String name, final List<String> entries) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);
		final JLabel label=new JLabel(name);
		line.add(label);
		final JComboBox<String> combo=new JComboBox<>(entries!=null?entries.toArray(new String[0]):new String[0]);
		line.add(combo);
		label.setLabelFor(combo);
		return combo;
	}

	private List<HistogramIDRecord> getBoxElements(final EditModel model) {
		final List<HistogramIDRecord> list=new ArrayList<>();

		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ModelElementBox) list.add(new HistogramIDRecord((ModelElementBox)element1));
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementBox) list.add(new HistogramIDRecord((ModelElementBox)element2));
			}
		}

		return list;
	}

	private List<HistogramIDRecord> getResources(final EditModel model) {
		final List<HistogramIDRecord> list=new ArrayList<>();

		int id=0;
		for (ModelResource resource: model.resources.getResources()) {
			id++;
			final StringBuilder sb=new StringBuilder();
			sb.append(Language.tr("Statistics.Resource"));
			if (!resource.getName().trim().isEmpty()) {
				sb.append(" \"");
				sb.append(resource.getName());
				sb.append("\"");
			}
			sb.append(String.format(" (id=%d)",id));
			list.add(new HistogramIDRecord(sb.toString(),id));
		}

		return list;
	}

	private List<HistogramIDRecord> getTransporters(final EditModel model) {
		final List<HistogramIDRecord> list=new ArrayList<>();

		int id=0;
		for (ModelTransporter transporter: model.transporters.getTransporters()) {
			id++;
			final StringBuilder sb=new StringBuilder();
			sb.append(Language.tr("Statistics.Transporter"));
			if (!transporter.getName().trim().isEmpty()) {
				sb.append(" \"");
				sb.append(transporter.getName());
				sb.append("\"");
			}
			sb.append(String.format(" (id=%d)",id));
			list.add(new HistogramIDRecord(sb.toString(),id));
		}

		return list;
	}

	private List<HistogramIDRecord> getStatisticElements(final EditModel model) {
		final List<HistogramIDRecord> list=new ArrayList<>();

		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ModelElementUserStatistic) list.add(new HistogramIDRecord((ModelElementUserStatistic)element1));
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementUserStatistic) list.add(new HistogramIDRecord((ModelElementUserStatistic)element2));
			}
		}

		return list;
	}

	private void typeChanged() {
		int lastID=Math.max(0,comboID.getSelectedIndex());
		if (lastType>=0) {
			if (types.get(lastType).idMode!=types.get(comboType.getSelectedIndex()).idMode) lastID=0;
		}

		lastType=comboType.getSelectedIndex();

		final List<HistogramIDRecord> items;
		switch (types.get(lastType).idMode) {
		case ID_BOX_ELEMENT: items=idsBox; break;
		case ID_NON: items=null; break;
		case ID_RESOURCE: items=idsResource; break;
		case ID_STATISTIC_STATION: items=idsStatistics; break;
		case ID_TRANSPORTER: items=idsTransporter; break;
		default: items=null; break;
		}

		if (items==null) {
			comboID.setEnabled(false);
		} else {
			comboID.setModel(new DefaultComboBoxModel<>(items.stream().map(idRecord->idRecord.name).toArray(String[]::new)));
			comboID.setSelectedIndex(lastID);
			comboID.setEnabled(true);
		}

		editNr.setEnabled(types.get(lastType).idMode==HistogramID.ID_STATISTIC_STATION);

		checkData(false);
	}

	private void setupColorButton() {
		final BufferedImage image=new BufferedImage(16,16,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		g.setColor(barColor);
		g.fillRect(0,0,15,15);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(0,0,15,15);
		buttonColor.setIcon(new ImageIcon(image));
	}

	private void showColorSelectDialog() {
		final ModelElementBaseColorDialog dialog=new ModelElementBaseColorDialog(this,helpRunnable,barColor);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			barColor=dialog.getUserColor();
			setupColorButton();
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		Integer I;
		Long L;

		if (editNr.isEnabled()) {
			L=NumberTools.getPositiveLong(editNr,true);
			if (L==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationBarChart.HistogramWizard.Nr.ErrorTitle"),String.format(Language.tr("Surface.AnimationBarChart.HistogramWizard.Nr.ErrorInfo"),editNr.getText()));
					return false;
				}
			}
		} else {
			editNr.setBackground(SystemColor.text);
		}

		I=NumberTools.getNotNegativeInteger(editStart,true);
		if (I==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBarChart.HistogramWizard.Start.ErrorTitle"),String.format(Language.tr("Surface.AnimationBarChart.HistogramWizard.Start.ErrorInfo"),editStart.getText()));
				return false;
			}
		}

		L=NumberTools.getPositiveLong(editStepWidth,true);
		if (L==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBarChart.HistogramWizard.StepWidth.ErrorTitle"),String.format(Language.tr("Surface.AnimationBarChart.HistogramWizard.StepWidth.ErrorInfo"),editStepWidth.getText()));
				return false;
			}
		}

		L=NumberTools.getPositiveLong(editCount,true);
		if (L==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBarChart.HistogramWizard.Count.ErrorTitle"),String.format(Language.tr("Surface.AnimationBarChart.HistogramWizard.Count.ErrorInfo"),editCount.getText()));
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
	 * Liefert die Ausdrücke für die neuen Histogrammbalken
	 * @return	Ausdrücke für die neuen Histogrammbalken
	 */
	public String[] getCommands() {
		final List<String> commands=new ArrayList<>();

		int start=NumberTools.getNotNegativeInteger(editStart,true);
		int stepWidth=NumberTools.getPositiveLong(editStepWidth,true).intValue();
		int count=NumberTools.getPositiveLong(editCount,true).intValue();

		final HistogramType type=types.get(comboType.getSelectedIndex());
		final StringBuilder sb=new StringBuilder();
		sb.append(type.command);
		sb.append("(");
		switch (type.idMode) {
		case ID_BOX_ELEMENT:
			sb.append(""+idsBox.get(comboID.getSelectedIndex()).id);
			sb.append(";");
			break;
		case ID_NON:
			/* nichts */
			break;
		case ID_RESOURCE:
			sb.append(""+idsResource.get(comboID.getSelectedIndex()).id);
			sb.append(";");
			break;
		case ID_STATISTIC_STATION:
			sb.append(""+idsStatistics.get(comboID.getSelectedIndex()).id);
			sb.append(";");
			break;
		case ID_TRANSPORTER:
			sb.append(""+idsTransporter.get(comboID.getSelectedIndex()).id);
			sb.append(";");
			break;
		}
		if (type.idMode==HistogramID.ID_STATISTIC_STATION) {
			sb.append(editNr.getText().trim());
			sb.append(";");
		}
		sb.append("%s)");
		final String command=sb.toString();

		if (stepWidth==1) {
			for (int i=0;i<count;i++) {
				final String val=String.format("%d",start+i);
				commands.add(String.format(command,val));
			}
			final String val=String.format("%d;%d",start+count-1,9_999_999);
			commands.add(String.format(command,val));
		} else {
			sb.append("%d;%d)");
			for (int i=0;i<count;i++) {
				if (i==0 && start==0) {
					final String val1=String.format("%d",start);
					final String val2=String.format("%d;%d",0,stepWidth-1);
					commands.add(String.format(command,val1)+"+"+String.format(command,val2));
				} else {
					final String val=String.format("%d;%d",start+i*stepWidth-1,start+(i+1)*stepWidth-1);
					commands.add(String.format(command,val));
				}
			}
			final String val=String.format("%d;%d",start+count*stepWidth-1,9_999_999);
			commands.add(String.format(command,val));
		}

		return commands.toArray(new String[0]);
	}

	/**
	 * Liefert die Farbe für die Histogrammbalken (außer für den letzten)
	 * @return	Farbe für die Histogrammbalken (außer für den letzten)
	 */
	public Color getBarColor() {
		return barColor;
	}

	/**
	 * Gibt an, ob die neuen Balken die alten ersetzen oder ergänzen sollen
	 * @return	Alte Balken ersetzen (<code>true</code>) oder ergänzen (<code>false</code>)
	 */
	public boolean getReplaceRecords() {
		return replaceRecords.isSelected();
	}

	private enum HistogramID {ID_NON, ID_BOX_ELEMENT, ID_RESOURCE, ID_TRANSPORTER, ID_STATISTIC_STATION}

	private class HistogramType {
		public final String name;
		public final String command;
		public final HistogramID idMode;

		public HistogramType(final String name, final String command, final HistogramID idMode) {
			this.name=name;
			this.command=command;
			this.idMode=idMode;
		}
	}

	private class HistogramIDRecord {
		public final String name;
		public final int id;

		public HistogramIDRecord(final ModelElementBox element) {
			final StringBuilder sb=new StringBuilder();
			sb.append(element.getTypeName());
			if (!element.getName().trim().isEmpty()) {
				sb.append(" \"");
				sb.append(element.getName());
				sb.append("\"");
			}
			sb.append(String.format(" (id=%d)",element.getId()));
			name=sb.toString();
			id=element.getId();
		}

		public HistogramIDRecord(final String name, final int id) {
			this.name=name;
			this.id=id;
		}
	}
}