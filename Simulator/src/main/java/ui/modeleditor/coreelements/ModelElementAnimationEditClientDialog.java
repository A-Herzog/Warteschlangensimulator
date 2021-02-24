/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Bearbeitendialog für die Daten zu einem einzelnen Kunden an.
 * @author Alexander Herzog
 * @see ModelElementAnimationInfoDialog
 */
public class ModelElementAnimationEditClientDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2042230018826768806L;

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource;

	/**
	 * Zu bearbeitendes Kundenobjekt
	 */
	private final RunDataClient client;

	/**
	 * Auswahlbox für den Kundentyp
	 */
	private final JComboBox<String> comboClientType;

	/**
	 * Auswahlbox für das Icon für den Kunden
	 */
	private final JComboBox<JLabel> comboIcon;

	/**
	 * Datenmodell für Animations-Icon Auswahlfeld {@link #comboIcon}
	 */
	private final DefaultComboBoxModel<JLabel> comboIconModel;

	/**
	 * Sollen die Daten des Kunden in der Statistik erfasst werden?
	 */
	private final JCheckBox checkInStatistics;

	/**
	 * Eingabefeld für die Wartezeit
	 */
	private final JTextField editWaitingTime;

	/**
	 * Eingabefeld für die Transferzeit
	 */
	private final JTextField editTransferTime;

	/**
	 * Eingabefeld für die Bedienzeit
	 */
	private final JTextField editProcessTime;

	/**
	 * Eingabefeld für die Verweilzeit
	 */
	private final JTextField editResidenceTime;

	/**
	 * Eingabefeld für die Wartezeitkosten
	 */
	private final JTextField editWaitingCosts;

	/**
	 * Eingabefeld für die Transferzeitkosten
	 */
	private final JTextField editTransferCosts;

	/**
	 * Eingabefeld für die Bedienzeitkosten
	 */
	private final JTextField editProcessCosts;

	/**
	 * Datenmodell für die Tabelle der numerischen Kundendaten
	 */
	private final ClientDataTableModel numbersModel;

	/**
	 * Datenmodell für die Tabelle der textbasierten Kundendaten
	 */
	private final ClientDataTableModel textsModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param model	Simulationsmodell mit Informationen zu den Stationen usw.
	 * @param client	Zu bearbeitendes Kundenobjekt
	 */
	public ModelElementAnimationEditClientDialog(final Component owner, final ModelAnimationImages modelImages, final RunModel model, final RunDataClient client) {
		super(owner,Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient"));
		this.client=client;
		imageSource=new AnimationImageSource();

		/* GUI */

		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs);

		JPanel tabOuter;
		JPanel tab;
		JPanel line;
		JLabel label;
		JTableExt table;

		/* Tab "Allgemeines" */

		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ClientType")+":"));
		line.add(comboClientType=new JComboBox<>(buildClientTypesList(model)));
		comboClientType.setSelectedIndex(client.type);
		label.setLabelFor(comboClientType);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Icon")+":"));
		line.add(comboIcon=new JComboBox<>());
		comboIconModel=imageSource.getIconsComboBox(modelImages);
		comboIcon.setModel(comboIconModel);
		comboIcon.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		label.setLabelFor(comboIcon);

		if (client.isWarmUp) {
			checkInStatistics=null;
		} else {
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(checkInStatistics=new JCheckBox(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.General.InStatistics"),client.inStatistics));
		}

		/* Tab "Zeiten" */

		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		editWaitingTime=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Waiting"),client.waitingTime/1000.0);
		editTransferTime=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Transfer"),client.transferTime/1000.0);
		editProcessTime=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Process"),client.processTime/1000.0);
		editResidenceTime=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Residence"),client.residenceTime/1000.0);

		/* Tab "Kosten" */

		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		editWaitingCosts=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Waiting"),client.waitingAdditionalCosts);
		editTransferCosts=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Transfer"),client.transferAdditionalCosts);
		editProcessCosts=addInput(tab,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Process"),client.processAdditionalCosts);

		/* Tab "Numerische Datenfelder" */

		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Number.Short"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		numbersModel=new ClientDataTableModel(table,client,true);
		table.setModel(numbersModel);
		table.getColumnModel().getColumn(0).setMaxWidth(150);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);

		/* Tab "Text Datenfelder" */

		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Text.Short"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		textsModel=new ClientDataTableModel(table,client,false);
		table.setModel(textsModel);
		table.getColumnModel().getColumn(0).setMaxWidth(150);
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);

		/* Icons auf den Tabs */

		tabs.setIconAt(0,Images.GENERAL_INFO.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_ANIMATION_CLOCK.getIcon());
		tabs.setIconAt(2,Images.MODELEDITOR_ELEMENT_COSTS.getIcon());
		tabs.setIconAt(3,Images.GENERAL_NUMBERS.getIcon());
		tabs.setIconAt(4,Images.GENERAL_FONT.getIcon());

		/* Icon-Combobox mit Vorgabe belegen */

		int index=0;
		if (client.icon!=null) for (int i=0;i<comboIconModel.getSize();i++) {
			String name=comboIconModel.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(name,name);
			if (client.icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		comboIcon.setSelectedIndex(index);

		/* Dialog starten */

		checkData(false);
		pack();
		setMinSizeRespectingScreensize(600,400);
		setLocationRelativeTo(getOwner());
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Liefert eine Liste mit allen Kundentypen für die Darstellung in {@link #comboClientType}
	 * @param model	Laufzeitmodell dem die Kundentypnamen entnommen werden sollen
	 * @return	Liste mit allen Kundentypen (Name und ID), indiziert nach IDs
	 * @see #comboClientType
	 */
	private static String[] buildClientTypesList(final RunModel model)  {
		final List<String> list=new ArrayList<>();
		for (int i=0;i<model.clientTypes.length;i++) list.add(String.format("%s (id=%d)",model.clientTypes[i],i));
		return list.toArray(new String[0]);
	}

	/**
	 * Legt ein neues Eingabefeld an und verknüpft es mit der Werteprüfung.
	 * @param parent	Übergeordnetes Element in das die neue Zeile (die das Eingabefeld enthält) eingefügt werden soll
	 * @param labelText	Beschriftung für das Eingabefeld
	 * @param initialValue	Initial anzuzeigender Wert
	 * @return	Neues Eingabefeld
	 * @see #checkData(boolean)
	 */
	private JTextField addInput(final JPanel parent, final String labelText, final double initialValue) {
		final Object[] data=ModelElementBaseDialog.getInputPanel(labelText+":",NumberTools.formatNumberMax(initialValue),20);
		parent.add((JPanel)data[0]);
		final JTextField textField=(JTextField)data[1];
		textField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		return textField;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;
		Double D;

		/* Tab "Zeiten" */

		D=NumberTools.getNotNegativeDouble(editWaitingTime,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Waiting.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Waiting.ErrorInfo"),editWaitingTime.getText()));
				return false;
			}
		}

		D=NumberTools.getNotNegativeDouble(editTransferTime,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Transfer.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Transfer.ErrorInfo"),editTransferTime.getText()));
				return false;
			}
		}

		D=NumberTools.getNotNegativeDouble(editProcessTime,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Process.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Process.ErrorInfo"),editProcessTime.getText()));
				return false;
			}
		}

		D=NumberTools.getNotNegativeDouble(editResidenceTime,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Residence.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Times.Residence.ErrorInfo"),editResidenceTime.getText()));
				return false;
			}
		}

		/* Tab "Kosten" */

		D=NumberTools.getDouble(editWaitingCosts,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Waiting.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Waiting.ErrorInfo"),editWaitingCosts.getText()));
				return false;
			}
		}

		D=NumberTools.getDouble(editTransferCosts,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Transfer.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Transfer.ErrorInfo"),editTransferCosts.getText()));
				return false;
			}
		}

		D=NumberTools.getDouble(editProcessCosts,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Process.ErrorTitle"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Costs.Process.ErrorInfo"),editProcessCosts.getText()));
				return false;
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
		/* Tab "Allgemeines" */

		final int newType=comboClientType.getSelectedIndex();
		if (newType!=client.type) {
			client.typeLast=client.type;
			client.type=newType;
		}

		String newIcon=comboIconModel.getElementAt(comboIcon.getSelectedIndex()).getText();
		newIcon=AnimationImageSource.ICONS.getOrDefault(newIcon,newIcon);
		if (!newIcon.equals(client.icon)) {
			client.iconLast=client.icon;
			client.icon=newIcon;
		}

		if (checkInStatistics!=null) {
			final boolean newInStatistics=checkInStatistics.isSelected();
			if (newInStatistics!=client.inStatistics) {
				client.inStatistics=newInStatistics;
			}
		}

		/* Tab "Zeiten" */

		client.waitingTime=Math.round(NumberTools.getDouble(editWaitingTime,true)*1000);
		client.transferTime=Math.round(NumberTools.getDouble(editTransferTime,true)*1000);
		client.processTime=Math.round(NumberTools.getDouble(editProcessTime,true)*1000);
		client.residenceTime=Math.round(NumberTools.getDouble(editResidenceTime,true)*1000);

		/* Tab "Kosten" */

		client.waitingAdditionalCosts=NumberTools.getDouble(editWaitingCosts,true);
		client.transferAdditionalCosts=NumberTools.getDouble(editTransferCosts,true);
		client.processAdditionalCosts=NumberTools.getDouble(editProcessCosts,true);

		/* Tab "Numerische Datenfelder" */

		numbersModel.storeData();

		/* Tab "Text Datenfelder" */

		textsModel.storeData();
	}
}
