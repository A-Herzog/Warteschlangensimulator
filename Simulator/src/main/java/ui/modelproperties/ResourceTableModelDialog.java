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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelDataResourceUsage;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Diese Klasse stellt einen Dialog zum Bearbeiten eines einzelnen
 * Eintrags in einem {@link ResourceTableModel} bereit.
 * @author Alexander Herzog
 * @see ResourceTableModel
 */
public class ResourceTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5239819610274811096L;

	/**
	 * Betrachtetes Modell
	 */
	private final EditModel model;

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource;

	/**
	 * Namen der modellweiten Variablen
	 */
	private final String[] variables;

	/**
	 * Liste der bereits vorhandenen Bedienergruppennamen (inkl. des Names der aktuellen Gruppe)
	 */
	private final String[] names;

	/**
	 * Index des aktuellen Namens in der Liste der vorhandenen Namen {@link #names} (zum Zeitpunkt des Aufrufs des Dialogs)
	 */
	private final int index;

	/**
	 * Liste mit den Namen der verfügbaren Schichtpläne
	 */
	private final String[] scheduleNames;

	/**
	 * Zu bearbeitende Bedienergruppe
	 */
	private final ModelResource resource;

	/** Eingabefeld "Name der Bedienergruppe" */
	private final JTextField inputField;
	/** Optional sichtbarer Hinweis auf ungültige Namen */
	private final JLabel infoLabel;

	/* Anzahl und Aussehen */

	/** Option "Feste Anzahl" */
	private final JRadioButton optionNumber;
	/** Eingabefeld für "Feste Anzahl" */
	private final JTextField inputNumber;
	/** Option "Unendlich viele" */
	private final JRadioButton optionInfinite;
	/** Option "Zeitplan" */
	private final JRadioButton optionSchedule;
	/** Auswahlfeld für "Zeitplan" */
	private final JComboBox<String> selectSchedule;
	/** Datenmodell für Auswahlfeld "Icon für Ressource" ({@link #iconChooserList}) */
	private final DefaultComboBoxModel<JLabel> iconChooserList;
	/** Auswahlfeld "Icon für Ressource" */
	private final JComboBox<JLabel> iconChooser;

	/* Kosten */

	/** Eingabefeld "Kosten pro aktiver Stunde" */
	private final JTextField inputCostsPerActiveHour;
	/** Eingabefeld "Kosten pro Stunde Arbeit" */
	private final JTextField inputCostsPerProcessHour;
	/** Eingabefeld "Kosten pro Stunde Leerlauf" */
	private final JTextField inputCostsPerIdleHour;

	/* Ausfälle / Pausen */

	/** Datenmodell für die Tabelle der Ausfälle */
	private final ResourceFailureTableModel failureData;

	/* Rüstzeiten */

	/** Modus für die Art der Rüstzeiten */
	private final JComboBox<String> moveTimesMode;
	/** Panel zur Auswahl der Zeiteinheit für die Rüstzeit */
	private final JPanel timeBasePanel;
	/** Auswahlfeld zur Festlegung der Zeiteinheit für die Rüstzeit */
	private final JComboBox<String> timeBaseCombo;
	/** Panel das den Wahrscheinlichkeitseditor oder das Eingabefeld für die Formel enthält */
	private final JPanel moveTimesCards;
	/** Verteilungseditor für die Rüstzeiten */
	private final JDistributionPanel moveTimesDistribution;
	/** Formel-Eingabefeld für die Rüstzeiten */
	private final JTextField moveTimeField;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param names	Liste der bereits vorhandenen Bedienergruppennamen (inkl. des Names der aktuellen Gruppe)
	 * @param resource	Zu bearbeitende Bedienergruppe
	 * @param scheduleNames	Liste mit den Namen der verfügbaren Schichtpläne
	 * @param model	Vollständiges Editor-Modell (wird für den Expression-Builder benötigt)
	 * @param surface	Haupt-Zeichenfläche (wird benötigt um zu vermitteln, wo eine Bedienergruppe im Einsatz ist, und für den Expression-Builder)
	 * @param modelImages	Liste mit den Animations-Icons (zur Auswahl von einem für die Bedienergruppe)
	 */
	public ResourceTableModelDialog(final Component owner, final Runnable help, final String[] names, final ModelResource resource, final String[] scheduleNames, final EditModel model, final ModelSurface surface, final ModelAnimationImages modelImages) {
		super(owner,Language.tr("Resources.Group.EditName.Dialog.Title"));

		this.model=model;
		variables=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false);
		imageSource=new AnimationImageSource();

		/* Globale Daten speichern */
		this.names=names;
		final String name=resource.getName();
		int nr=-1;
		for (int i=0;i<names.length;i++) if (names[i].equalsIgnoreCase(name)) {nr=i; break;}
		index=nr;
		this.scheduleNames=scheduleNames;
		this.resource=resource;

		/* GUI anlegen */
		if (index>=0) {
			addUserButton(Language.tr("Resources.Usage"),Images.MODEL_ADD_STATION.getIcon());
			if (ModelPropertiesDialogPageOperatorsUsageDialog.getPossibleNewStationsForRessource(names[index],model.surface).size()>0) addUserButton("Zu Station hinzufügen",Images.MODELPROPERTIES_OPERATORS_ADD.getIcon());
		}
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel tabOuter, tab, panel, line;
		ButtonGroup group;
		JLabel label;

		/* Name */
		content.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Resources.Group.EditName.Dialog.Name")+":",name);
		tab.add((JPanel)data[0],BorderLayout.NORTH);
		inputField=(JTextField)data[1];
		inputField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(infoLabel=new JLabel(Language.tr("Resources.Group.EditName.Dialog.ErrorNoName")));

		/* Tabs */
		JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		/* Tab: Anzahl */
		tabs.add(Language.tr("Resources.Group.EditName.Dialog.Tab.Count"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(new JLabel(Language.tr("Resources.Group.EditName.Dialog.Number")+":"));

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(optionNumber=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Number.Fixed")+":"));
		panel.add(inputNumber=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(inputNumber);
		inputNumber.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionNumber.setSelected(true); NumberTools.getPositiveLong(inputNumber,true);}
			@Override public void keyReleased(KeyEvent e) {optionNumber.setSelected(true); NumberTools.getPositiveLong(inputNumber,true);}
			@Override public void keyPressed(KeyEvent e) {optionNumber.setSelected(true); NumberTools.getPositiveLong(inputNumber,true);}
		});

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(optionInfinite=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Number.Infinite")));

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(optionSchedule=new JRadioButton(Language.tr("Resources.Group.EditName.Dialog.Number.Schedule")+":"));
		panel.add(selectSchedule=new JComboBox<>(scheduleNames));
		selectSchedule.addActionListener(e->optionSchedule.setSelected(true));

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(new JLabel(Language.tr("Resources.Group.EditName.Dialog.Number.Schedule.Info")));

		group=new ButtonGroup();
		group.add(optionNumber);
		group.add(optionInfinite);
		group.add(optionSchedule);

		/* Icon-Combobox */
		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		panel.add(label=new JLabel(Language.tr("Resources.Group.EditName.Dialog.IconForResource")+":"));
		panel.add(iconChooser=new JComboBox<>());
		iconChooserList=imageSource.getIconsComboBox(modelImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		int index=0;
		final String icon=(resource.getIcon()!=null && !resource.getIcon().isEmpty())?resource.getIcon():ModelSurfaceAnimatorBase.DEFAULT_OPERATOR_ICON_NAME;
		for (int i=0;i<iconChooserList.getSize();i++) {
			String n=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(n,n);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);

		/* Tab: Kosten */
		tabs.add(Language.tr("Resources.Group.EditName.Dialog.Tab.Costs"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Resources.Group.EditName.Dialog.CostsPerHour")+":",NumberTools.formatNumber(resource.getCostsPerActiveHour()),10);
		tab.add((JPanel)data[0]);
		inputCostsPerActiveHour=(JTextField)data[1];
		inputCostsPerActiveHour.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Resources.Group.EditName.Dialog.CostsPerWorkHour")+":",NumberTools.formatNumber(resource.getCostsPerProcessHour()),10);
		tab.add((JPanel)data[0]);
		inputCostsPerProcessHour=(JTextField)data[1];
		inputCostsPerProcessHour.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Resources.Group.EditName.Dialog.CostsPerIdleHour")+":",NumberTools.formatNumber(resource.getCostsPerIdleHour()),10);
		tab.add((JPanel)data[0]);
		inputCostsPerIdleHour=(JTextField)data[1];
		inputCostsPerIdleHour.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Tab: Ausfälle */
		tabs.add(Language.tr("Resources.Group.EditName.Dialog.Tab.Failures"),tab=new JPanel(new BorderLayout()));

		final JTableExt table=new JTableExt();
		failureData=new ResourceFailureTableModel(resource,model,model.surface,table,readOnly,help);
		table.setModel(failureData);
		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		tab.add(new JScrollPane(table));

		/* Tab: Rüstzeiten */

		tabs.add(Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes"),tab=new JPanel(new BorderLayout()));
		tab.add(moveTimesCards=new JPanel(new CardLayout()),BorderLayout.CENTER);
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Mode")+":"));
		line.add(moveTimesMode=new JComboBox<>(new String[] {
				Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Mode.Off"),
				Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Mode.Distribution"),
				Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Mode.Expression")
		}));
		label.setLabelFor(moveTimesMode);
		moveTimesMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		panel.add(timeBasePanel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		timeBasePanel.add(label=new JLabel(Language.tr("Surface.Source.Dialog.TimeBase")+":"));
		timeBasePanel.add(timeBaseCombo=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBaseCombo.setSelectedIndex(resource.getMoveTimeBase().id);
		moveTimesMode.addActionListener(e->{
			final int cardIndex=moveTimesMode.getSelectedIndex();
			((CardLayout)moveTimesCards.getLayout()).show(moveTimesCards,"Seite"+(cardIndex+1));
			timeBasePanel.setVisible(cardIndex>0);
			checkData(false);
		});

		/* Tab: Rüstzeiten - keine */

		moveTimesCards.add(new JPanel(),"Seite1");

		/* Tab: Rüstzeiten - Verteilung */

		moveTimesCards.add(panel=new JPanel(new BorderLayout()),"Seite2");
		panel.add(moveTimesDistribution=new JDistributionPanel(new ExponentialDistribution(null,300,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY),3600,true));

		/* Tab: Rüstzeiten - Audruck */

		moveTimesCards.add(panel=new JPanel(new BorderLayout()),"Seite3");
		final JPanel sub=new JPanel();
		panel.add(sub,BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Mode.Expression")+":","0");
		sub.add((JPanel)data[0]);
		moveTimeField=(JTextField)data[1];
		moveTimeField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		((JPanel)data[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,moveTimeField,false,false,model,surface),BorderLayout.EAST);

		/* Rüstzeiten-Daten laden */

		final Object moveTime=resource.getMoveTimes();
		moveTimesMode.setSelectedIndex(0);
		((CardLayout)moveTimesCards.getLayout()).show(moveTimesCards,"Seite1");
		timeBasePanel.setVisible(false);
		if (moveTime instanceof AbstractRealDistribution) {
			moveTimesMode.setSelectedIndex(1);
			((CardLayout)moveTimesCards.getLayout()).show(moveTimesCards,"Seite2");
			timeBasePanel.setVisible(true);
			moveTimesDistribution.setDistribution((AbstractRealDistribution)moveTime);
		}
		if (moveTime instanceof String) {
			moveTimesMode.setSelectedIndex(2);
			((CardLayout)moveTimesCards.getLayout()).show(moveTimesCards,"Seite3");
			timeBasePanel.setVisible(true);
			moveTimeField.setText((String)moveTime);
		}

		/* Icons */
		tabs.setIconAt(0,Images.MODELPROPERTIES_OPERATORS.getIcon());
		tabs.setIconAt(1,Images.MODELPROPERTIES_OPERATORS_COSTS.getIcon());
		tabs.setIconAt(2,Images.MODELPROPERTIES_OPERATORS_FAILURES.getIcon());
		tabs.setIconAt(3,Images.MODELPROPERTIES_OPERATORS_SETUP.getIcon());

		/* Daten eintragen: Anzahl */
		if (resource.getMode()==ModelResource.Mode.MODE_NUMBER) {
			if (resource.getCount()<0) {
				inputNumber.setText("1");
				optionInfinite.setSelected(true);
			} else {
				inputNumber.setText(""+resource.getCount());
				optionNumber.setSelected(true);
			}
		}
		if (resource.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
			for (int i=0;i<scheduleNames.length;i++) if (scheduleNames[i].equals(resource.getSchedule())) {selectSchedule.setSelectedIndex(i); break;}
			optionSchedule.setSelected(true);
		}

		/* Dialog vorbereiten */
		setMinSizeRespectingScreensize(800,400);
		pack();
		checkData(false);
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorDialog	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorDialog) {
		final String text=inputField.getText().trim();
		boolean ok=true;
		String info="";

		/* Name */

		if (text.isEmpty()) {
			info=Language.tr("Resources.Group.EditName.Dialog.ErrorNoName");
			ok=false;
		}

		if (ok) for (int i=0;i<names.length;i++) {
			if (names[i].equalsIgnoreCase(text) && i!=index) {
				info=Language.tr("Resources.Group.EditName.Dialog.ErrorNameInUse");
				ok=false;
				break;
			}
		}

		infoLabel.setText(info);
		if (ok) inputField.setBackground(NumberTools.getTextFieldDefaultBackground()); else inputField.setBackground(Color.red);
		if (!ok && showErrorDialog) {
			MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.ErrorName"),info);
			return false;
		}

		/* Anzahl */

		if (optionNumber.isSelected() && NumberTools.getPositiveLong(inputNumber,true)==null) {
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Number.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.Number.Error.Info"));
				return false;
			}
			ok=false;
		}

		if (optionSchedule.isSelected() && selectSchedule.getSelectedIndex()<0) {
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Number.Schedule.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.Number.Schedule.Error.Info"));
				return false;
			}
			ok=false;
		}

		/* Kosten */

		if (NumberTools.getDouble(inputCostsPerActiveHour,true)==null) {
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.CostsPerHour.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.CostsPerHour.Error.Info"));
				return false;
			}
			ok=false;
		}

		if (NumberTools.getDouble(inputCostsPerProcessHour,true)==null) {
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.CostsPerWorkHour.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.CostsPerWorkHour.Error.Info"));
				return false;
			}
			ok=false;
		}

		if (NumberTools.getDouble(inputCostsPerIdleHour,true)==null) {
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.CostsPerIdleHour.Error.Title"),Language.tr("Resources.Group.EditName.Dialog.CostsPerIdleHour.Error.Info"));
				return false;
			}
			ok=false;
		}

		/* Rüstzeiten */

		if (moveTimesMode.getSelectedIndex()==2) {
			final String moveTime=moveTimeField.getText().trim();
			final int error=ExpressionCalc.check(moveTime,variables);
			if (error>=0) {
				moveTimeField.setBackground(Color.RED);
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Error.Title"),String.format(Language.tr("Resources.Group.EditName.Dialog.Tab.SetupTimes.Error.Info"),moveTime,error+1));
					return false;
				}
				ok=false;
			} else {
				moveTimeField.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		/* Name */
		resource.setName(inputField.getText());

		/* Anzahl */
		if (optionNumber.isSelected()) resource.setCount(NumberTools.getInteger(inputNumber,true));
		if (optionInfinite.isSelected()) resource.setCount(-1);
		if (optionSchedule.isSelected()) {
			int index=selectSchedule.getSelectedIndex();
			if (index>=0 && index<scheduleNames.length) resource.setSchedule(scheduleNames[index]);
		}

		/* Icon */
		String name=iconChooserList.getElementAt(iconChooser.getSelectedIndex()).getText();
		resource.setIcon(AnimationImageSource.ICONS.getOrDefault(name,name));

		/* Kosten */
		resource.setCostsPerActiveHour(NumberTools.getDouble(inputCostsPerActiveHour,true));
		resource.setCostsPerProcessHour(NumberTools.getDouble(inputCostsPerProcessHour,true));
		resource.setCostsPerIdleHour(NumberTools.getDouble(inputCostsPerIdleHour,true));

		/* Ausfälle */
		resource.getFailures().clear();
		resource.getFailures().addAll(failureData.getFailures());

		/* Rüstzeiten */
		switch (moveTimesMode.getSelectedIndex()) {
		case 0:
			resource.setMoveTimes(null);
			break;
		case 1:
			resource.setMoveTimes(moveTimesDistribution.getDistribution());
			break;
		case 2:
			resource.setMoveTimes(moveTimeField.getText().trim());
			break;
		}
		resource.setMoveTimeBase(ModelSurface.TimeBase.byId(timeBaseCombo.getSelectedIndex()));
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (nr==0) {
			new ModelPropertiesDialogPageOperatorsUsageDialog(this,model,names[index]);
			return;
		}

		if (nr==1) {
			final JPopupMenu menu=new JPopupMenu();
			final Map<String,ModelElement> map=new HashMap<>();
			ModelPropertiesDialogPageOperatorsUsageDialog.getPossibleNewStationsForRessource(names[index],model.surface).forEach(element->map.put(ModelPropertiesDialogPageOperatorsUsageDialog.getStationName(element),element));
			for (String stationName: map.keySet().stream().sorted().toArray(String[]::new)) {
				final ModelElement element=map.get(stationName);
				final JMenuItem item=new JMenuItem(stationName);
				item.addActionListener(e->{
					((ModelDataResourceUsage)element).addResourceUsage(names[index],1);
					button.setVisible(ModelPropertiesDialogPageOperatorsUsageDialog.getPossibleNewStationsForRessource(names[index],model.surface).size()>0);
				});
				menu.add(item);
			}
			menu.show(button,0,button.getHeight());
			return;
		}
	}
}