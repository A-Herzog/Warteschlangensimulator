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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelTransporter;

/**
 * Diese Klasse stellt einen Dialog zum Bearbeiten eines einzelnen
 * Eintrags in einem {@link TransporterTableModel} bereit.
 * @author Alexander Herzog
 * @see TransporterTableModel
 */
public class TransporterTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -888684759824862063L;

	private final AnimationImageSource imageSource;

	private final ModelSurface surface;
	private final String[] variables;
	private final String[] variablesWithDistances;
	private final String[] names;
	private final int index;
	private final ModelTransporter transporter;

	private final JTextField inputField;
	private final JLabel infoLabel;

	private final JTextField capacity;
	private final DefaultComboBoxModel<JLabel> iconChooserListEastEmpty;
	private final JComboBox<JLabel> iconChooserEastEmpty;
	private final DefaultComboBoxModel<JLabel> iconChooserListWestEmpty;
	private final JComboBox<JLabel> iconChooserWestEmpty;
	private final DefaultComboBoxModel<JLabel> iconChooserListEastLoaded;
	private final JComboBox<JLabel> iconChooserEastLoaded;
	private final DefaultComboBoxModel<JLabel> iconChooserListWestLoaded;
	private final JComboBox<JLabel> iconChooserWestLoaded;

	private final JTextField expression;
	private final JButton tableButton;
	private final TransporterDistancesTableModel distancesTableModel;

	private final TransporterStationsTableModel countTableModel;

	private final TransporterFailureTableModel failureData;

	private final TransporterTableModelDialogLoadingTimes loadingTimes;
	private final TransporterTableModelDialogLoadingTimes unloadingTimes;

	private Object[] getIconChooser(final JComponent parent, final ModelAnimationImages modelImages, final String labelText, final String iconName, final String iconDefaultName) {
		final JPanel panel;
		final JLabel label;
		final JComboBox<JLabel> iconChooser;
		final DefaultComboBoxModel<JLabel> iconChooserList;

		/* Icon-Combobox */
		parent.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		panel.add(label=new JLabel(labelText+":"));
		panel.add(iconChooser=new JComboBox<>());
		iconChooserList=imageSource.getIconsComboBox(modelImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		int index=0;
		final String icon=(iconName!=null && !iconName.isEmpty())?iconName:iconDefaultName;
		for (int i=0;i<iconChooserList.getSize();i++) {
			String n=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(n,n);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);

		return new Object[]{iconChooser,iconChooserList};
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param names	Liste der bereits vorhandenen Transportergruppennamen (inkl. des Names der aktuellen Gruppe)
	 * @param transporter	Zu bearbeitende Transportergruppe
	 * @param model	Vollständiges Editor-Modell (wird für den Expression-Builder benötigt)
	 * @param surface	Haupt-Zeichenfläche (wird benötigt um zu vermitteln, wo eine Transportergruppe im Einsatz ist, und für den Expression-Builder)
	 * @param modelImages	Liste mit den Animations-Icons (zur Auswahl von einem für die Transportergruppe)
	 */
	@SuppressWarnings("unchecked")
	protected TransporterTableModelDialog(final Component owner, final Runnable help, final String[] names, final ModelTransporter transporter, final EditModel model, final ModelSurface surface, final ModelAnimationImages modelImages) {
		super(owner,Language.tr("Transporters.Group.Edit.Dialog.Title"));
		this.surface=surface;

		imageSource=new AnimationImageSource();

		/* Globale Daten speichern */
		variables=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false);
		final List<String> list=new ArrayList<>(Arrays.asList(variables));
		list.add(ModelTransporter.DEFAULT_DISTANCE);
		variablesWithDistances=list.toArray(new String[0]);
		this.names=names;
		final String name=transporter.getName();
		int nr=-1;
		for (int i=0;i<names.length;i++) if (names[i].equalsIgnoreCase(name)) {nr=i; break;}
		index=nr;
		this.transporter=transporter;

		/* GUI anlegen */
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel tabOuter, tab, panel;
		Object[] data;

		/* Name */
		content.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Transporters.Group.Edit.Dialog.Name")+":",name);
		tab.add((JPanel)data[0],BorderLayout.NORTH);
		inputField=(JTextField)data[1];
		inputField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(infoLabel=new JLabel(Language.tr("Transporters.Group.Edit.Dialog.ErrorNoName")));

		/* Tabs */
		JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		/* Tab: Kapazität */
		tabs.add(Language.tr("Transporters.Group.Edit.Dialog.Tab.Capacity"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Transporters.Group.Edit.Dialog.Capacity")+":",""+transporter.getCapacity(),5);
		tab.add((JPanel)data[0]);
		capacity=(JTextField)data[1];
		capacity.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Icon-Combobox (rechts, leer) */
		data=getIconChooser(tab,modelImages,Language.tr("Transporters.Group.Edit.Dialog.Tab.IconEastEmpty"),transporter.getEastEmptyIcon(),ModelSurfaceAnimatorBase.DEFAULT_TRANSPORTER_EAST_EMPTY_ICON_NAME);
		iconChooserEastEmpty=(JComboBox<JLabel>)data[0];
		iconChooserListEastEmpty=(DefaultComboBoxModel<JLabel>)data[1];

		/* Icon-Combobox (links, leer) */
		data=getIconChooser(tab,modelImages,Language.tr("Transporters.Group.Edit.Dialog.Tab.IconWestEmpty"),transporter.getWestEmptyIcon(),ModelSurfaceAnimatorBase.DEFAULT_TRANSPORTER_WEST_EMPTY_ICON_NAME);
		iconChooserWestEmpty=(JComboBox<JLabel>)data[0];
		iconChooserListWestEmpty=(DefaultComboBoxModel<JLabel>)data[1];

		/* Icon-Combobox (rechts, beladen) */
		data=getIconChooser(tab,modelImages,Language.tr("Transporters.Group.Edit.Dialog.Tab.IconEastLoaded"),transporter.getEastLoadedIcon(),ModelSurfaceAnimatorBase.DEFAULT_TRANSPORTER_EAST_ICON_NAME);
		iconChooserEastLoaded=(JComboBox<JLabel>)data[0];
		iconChooserListEastLoaded=(DefaultComboBoxModel<JLabel>)data[1];

		/* Icon-Combobox (links, beladen) */
		data=getIconChooser(tab,modelImages,Language.tr("Transporters.Group.Edit.Dialog.Tab.IconWestLoaded"),transporter.getWestLoadedIcon(),ModelSurfaceAnimatorBase.DEFAULT_TRANSPORTER_WEST_ICON_NAME);
		iconChooserWestLoaded=(JComboBox<JLabel>)data[0];
		iconChooserListWestLoaded=(DefaultComboBoxModel<JLabel>)data[1];

		/* Tab: Anzahl */
		tabs.add(Language.tr("Transporters.Group.Edit.Dialog.Tab.Count"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		final JTable tablePriority;
		tab.add(new JScrollPane(tablePriority=new JTable()),BorderLayout.CENTER);
		tablePriority.setModel(countTableModel=new TransporterStationsTableModel(this,tablePriority,transporter,surface,readOnly));
		tablePriority.getColumnModel().getColumn(1).setMaxWidth(100);
		tablePriority.getColumnModel().getColumn(1).setMinWidth(100);
		tablePriority.setEnabled(!readOnly);
		tablePriority.putClientProperty("terminateEditOnFocusLost",true);
		tablePriority.getTableHeader().setReorderingAllowed(false);

		/* Tab: Entfernungen */
		tabs.add(Language.tr("Transporters.Group.Edit.Dialog.Tab.Distances"),tabOuter=new JPanel(new BorderLayout()));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Transporters.Group.Edit.Dialog.Expression")+":",""+transporter.getExpression());
		tabOuter.add((JPanel)data[0],BorderLayout.NORTH);
		expression=(JTextField)data[1];
		expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		final JPanel eastArea=new JPanel(new FlowLayout(FlowLayout.LEFT));
		((JPanel)data[0]).add(eastArea,BorderLayout.EAST);
		eastArea.add(ModelElementBaseDialog.getExpressionEditButton(this.owner,expression,false,false,model,surface,new String[] {ModelTransporter.DEFAULT_DISTANCE}));
		eastArea.add(tableButton=new JButton(Language.tr("Transporters.Group.Edit.Dialog.Table")));
		tableButton.setToolTipText(Language.tr("Transporters.Group.Edit.Dialog.Table.Hint"));
		tableButton.setIcon(Images.MODELPROPERTIES_TRANSPORTERS_DISTANCES_TABLE_TOOLS.getIcon());
		tableButton.addActionListener(e->commandTableMenu());

		final JTable tableDistances;
		tabOuter.add(new JScrollPane(tableDistances=new JTable()),BorderLayout.CENTER);
		tableDistances.setModel(distancesTableModel=new TransporterDistancesTableModel(this,tableDistances,transporter,surface,readOnly,help));
		tableDistances.getColumnModel().getColumn(0).setMaxWidth(200);
		tableDistances.getColumnModel().getColumn(0).setMinWidth(200);
		for (int i=1;i<tableDistances.getColumnModel().getColumnCount();i++) tableDistances.getColumnModel().getColumn(i).setMinWidth(150);
		tableDistances.setEnabled(!readOnly);
		tableDistances.putClientProperty("terminateEditOnFocusLost",true);
		tableDistances.getTableHeader().setReorderingAllowed(false);
		tableDistances.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		/* Tab: Ausfälle */
		tabs.add(Language.tr("Transporters.Group.Edit.Dialog.Tab.Failures"),tabOuter=new JPanel(new BorderLayout()));
		final JTableExt table=new JTableExt();
		failureData=new TransporterFailureTableModel(transporter,model,model.surface,table,readOnly,help);
		table.setModel(failureData);
		table.getColumnModel().getColumn(1).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		tabOuter.add(new JScrollPane(table));

		/* Tab: Beladezeit */
		tabs.add(Language.tr("Transporters.Group.Edit.Dialog.Tab.LoadingTime"),loadingTimes=new TransporterTableModelDialogLoadingTimes(transporter.getLoadTime(),true,model));

		/* Tab: Entladezeit */
		tabs.add(Language.tr("Transporters.Group.Edit.Dialog.Tab.UnloadingTime"),unloadingTimes=new TransporterTableModelDialogLoadingTimes(transporter.getUnloadTime(),false,model));

		/* Icons */
		tabs.setIconAt(0,Images.MODELPROPERTIES_TRANSPORTERS.getIcon());
		tabs.setIconAt(1,Images.MODELPROPERTIES_TRANSPORTERS_COUNT.getIcon());
		tabs.setIconAt(2,Images.MODELPROPERTIES_TRANSPORTERS_DISTANCES.getIcon());
		tabs.setIconAt(3,Images.MODELPROPERTIES_TRANSPORTERS_FAILURE.getIcon());
		tabs.setIconAt(4,Images.MODELPROPERTIES_TRANSPORTERS_LOADING_TIMES.getIcon());
		tabs.setIconAt(5,Images.MODELPROPERTIES_TRANSPORTERS_UNLOADING_TIMES.getIcon());

		/* Dialogverarbeitung starten */
		checkData(false);

		/* Dialog vorbereiten */
		setMinSizeRespectingScreensize(700,500);
		setSizeRespectingScreensize(700,500);
		setResizable(true);
		setLocationRelativeTo(getOwner());
	}

	private boolean checkData(final boolean showErrorDialog) {
		final String text=inputField.getText().trim();
		boolean ok=true;
		String info="";

		/* Name */

		if (text.isEmpty()) {
			info=Language.tr("Transporters.Group.Edit.Dialog.ErrorNoName");
			ok=false;
		}

		if (ok) for (int i=0;i<names.length;i++) {
			if (names[i].equalsIgnoreCase(text) && i!=index) {
				info=Language.tr("Transporters.Group.Edit.Dialog.ErrorNameInUse");
				ok=false;
				break;
			}
		}

		infoLabel.setText(info);
		if (ok) inputField.setBackground(SystemColor.text); else inputField.setBackground(Color.red);
		if (!ok && showErrorDialog) {
			MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.ErrorName"),info);
			return false;
		}

		/* Kapazität */

		final Long L=NumberTools.getPositiveLong(capacity,true);
		if (L==null) {
			ok=false;
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Capacity.ErrorTitle"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Capacity.ErrorInfo"),capacity.getText()));
				return false;
			}
		}

		/* Anzahl */

		if (!countTableModel.checkInput(showErrorDialog)) ok=false;

		/* Entfernungen */

		final int error=ExpressionCalc.check(expression.getText(),variablesWithDistances);
		if (error>=0) {
			expression.setBackground(Color.RED);
			ok=false;
			if (showErrorDialog) {
				MsgBox.error(this,Language.tr("Transporters.Group.Edit.Dialog.Expression.ErrorTitle"),String.format(Language.tr("Transporters.Group.Edit.Dialog.Expression.ErrorInfo"),expression.getText(),error+1));
				return false;
			}
		} else {
			expression.setBackground(SystemColor.text);
		}

		if (!distancesTableModel.checkInput(showErrorDialog)) ok=false;

		/* Be- und Entladezeiten */

		if (!loadingTimes.checkData(showErrorDialog)) {
			ok=false;
			if (showErrorDialog) return false;
		}

		if (!unloadingTimes.checkData(showErrorDialog)) {
			ok=false;
			if (showErrorDialog) return false;
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
		transporter.setName(inputField.getText());

		/* Kapazität */
		transporter.setCapacity(NumberTools.getPositiveLong(capacity,true).intValue());

		/* Icon */
		String name;
		name=iconChooserListEastEmpty.getElementAt(iconChooserEastEmpty.getSelectedIndex()).getText();
		transporter.setEastEmptyIcon(AnimationImageSource.ICONS.getOrDefault(name,name));
		name=iconChooserListWestEmpty.getElementAt(iconChooserWestEmpty.getSelectedIndex()).getText();
		transporter.setWestEmptyIcon(AnimationImageSource.ICONS.getOrDefault(name,name));
		name=iconChooserListEastLoaded.getElementAt(iconChooserEastLoaded.getSelectedIndex()).getText();
		transporter.setEastLoadedIcon(AnimationImageSource.ICONS.getOrDefault(name,name));
		name=iconChooserListWestLoaded.getElementAt(iconChooserWestLoaded.getSelectedIndex()).getText();
		transporter.setWestLoadedIcon(AnimationImageSource.ICONS.getOrDefault(name,name));

		/* Anzahl */
		countTableModel.storeData();

		/* Entfernungen */
		transporter.setExpression(expression.getText());
		distancesTableModel.storeData();

		/* Ausfälle */
		transporter.getFailures().clear();
		transporter.getFailures().addAll(failureData.getFailures());

		/* Be- und Entladezeiten */
		transporter.setLoadTime(loadingTimes.getData());
		transporter.setUnloadTime(unloadingTimes.getData());
	}

	private void commandTableMenu() {
		final JPopupMenu popupMenu=new JPopupMenu();

		JMenuItem item;

		popupMenu.add(item=new JMenuItem(Language.tr("Dialog.Button.Copy")));
		item.setToolTipText(Language.tr("Transporters.Group.Edit.Dialog.Table.Copy.Hint"));
		item.setIcon(Images.EDIT_COPY.getIcon());
		item.addActionListener(e->distancesTableModel.copyToClipboard());
		item.setEnabled(!readOnly && distancesTableModel.getRowCount()>0);

		popupMenu.add(item=new JMenuItem(Language.tr("Dialog.Button.Paste")));
		item.setToolTipText(Language.tr("Transporters.Group.Edit.Dialog.Table.Paste.Hint"));
		item.setIcon(Images.EDIT_PASTE.getIcon());
		item.addActionListener(e->distancesTableModel.pasteFromClipboard());
		item.setEnabled(!readOnly && distancesTableModel.getRowCount()>0);

		popupMenu.addSeparator();

		popupMenu.add(item=new JMenuItem(Language.tr("Transporters.Group.Edit.Dialog.Table.FillDown")));
		item.setToolTipText(Language.tr("Transporters.Group.Edit.Dialog.Table.FillDown.Hint"));
		item.setIcon(Images.ARROW_DOWN.getIcon());
		item.addActionListener(e->distancesTableModel.fillDown());
		item.setEnabled(!readOnly && distancesTableModel.getRowCount()>0);

		popupMenu.add(item=new JMenuItem(Language.tr("Transporters.Group.Edit.Dialog.Table.FillUp")));
		item.setToolTipText(Language.tr("Transporters.Group.Edit.Dialog.Table.FillUp.Hint"));
		item.setIcon(Images.ARROW_UP.getIcon());
		item.addActionListener(e->distancesTableModel.fillUp());
		item.setEnabled(!readOnly && distancesTableModel.getRowCount()>0);

		popupMenu.addSeparator();

		popupMenu.add(item=new JMenuItem(Language.tr("Transporters.Group.Edit.Dialog.Table.ByModel")));
		item.setToolTipText(Language.tr("Transporters.Group.Edit.Dialog.Table.ByModel.Hint"));
		item.setIcon(Images.MODEL.getIcon());
		item.addActionListener(e->distancesTableModel.fillByModel(surface));
		item.setEnabled(!readOnly && distancesTableModel.getRowCount()>1);

		popupMenu.show(tableButton,0,tableButton.getHeight());
	}
}