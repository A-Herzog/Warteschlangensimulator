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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTank}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTank
 */
public class ModelElementTankDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2385029706949492016L;

	/**
	 * Umrechnungsfaktor um die Änderungsrate auf Sekunden zu normieren
	 * @see #analogNotifyUnit
	 */
	private static double[] MULTIPLY=new double[]{1,60,3600,86400};

	/**
	 * Eingabefeld für die Kapazität des Tanks
	 */
	private JTextField capacity;

	/**
	 * Eingabefeld für den initialen Füllstand des Tanks
	 */
	private JTextField initalValue;

	/**
	 * Eingabefeld für den Änderungsbenachrichtigungsabstand
	 */
	private JTextField analogNotify;

	/**
	 * Anzusetzende Zeiteinheit für den Änderungsbenachrichtigungsabstand
	 */
	private JComboBox<String> analogNotifyUnit;

	/**
	 * Tabellen zur Konfiguration der Ventile des Tanks
	 */
	private ModelElementTankTableModel valvesTableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTank}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTankDialog(final Component owner, final ModelElementTank element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Tank.Dialog.Title"),element,"ModelElementTank",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,500);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Lädt einen Wert in ein Eingabefeld und stellt die Zeiteinheiten-Auswahlbox passend ein.
	 * @param value	Zu verwendender Wert
	 * @param text	Eingabefeld in das der Wert (ggf. skaliert) eingetragen werden soll
	 * @param unit	Zeiteinheiten-Auswahlbox
	 * @param mul	Mit {@link #MULTIPLY} multiplizieren oder beim Umrechnen dadurch dividieren?
	 */
	private void loadValue(double value, final JTextField text, final JComboBox<String> unit, boolean mul) {
		text.setEditable(!readOnly);
		text.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		unit.setEnabled(!readOnly);
		unit.addActionListener(e->{checkData(false);});

		boolean minus=(value<0);
		value=Math.abs(value);
		int index=0;
		double scaled=value;
		if (mul) {
			while (index<MULTIPLY.length-1 && scaled<1) {
				index++;
				scaled=value*MULTIPLY[index];
			}
		} else {
			while (index<MULTIPLY.length-1 && value/MULTIPLY[index+1]>=1) {
				index++;
				scaled=value/MULTIPLY[index];
			}
		}

		double v=(minus?-1:1)*scaled;
		text.setText(NumberTools.formatNumberMax(NumberTools.reduceDigits(v,10)));
		unit.setSelectedIndex(index);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTank;
	}

	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();
		JPanel tab, tabInner;
		Object[] data;
		JPanel line;

		if (element instanceof ModelElementTank) {
			final ModelElementTank tank=(ModelElementTank)element;

			/* Tab: Allgemeine Einstellungen */
			tabs.add(Language.tr("Surface.Tank.Dialog.Tab.General"),tab=new JPanel(new BorderLayout()));
			tab.add(tabInner=new JPanel(),BorderLayout.NORTH);
			tabInner.setLayout(new BoxLayout(tabInner,BoxLayout.PAGE_AXIS));

			/* Kapazität */
			data=getInputPanel(Language.tr("Surface.Tank.Dialog.Capacity")+":",NumberTools.formatNumberMax(tank.getCapacity()),10);
			tabInner.add((JPanel)data[0]);
			capacity=(JTextField)data[1];
			capacity.setEditable(!readOnly);
			capacity.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});

			/* Initialwert */
			data=getInputPanel(Language.tr("Surface.Tank.Dialog.InitialValue")+":",NumberTools.formatNumberMax(tank.getInitialValue()),10);
			tabInner.add((JPanel)data[0]);
			initalValue=(JTextField)data[1];
			initalValue.setEditable(!readOnly);
			initalValue.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});

			/* Änderungsbenachrichtigung */
			final String[] unitNames=new String[]{
					Language.tr("Surface.Tank.Dialog.Unit.DeltaSeconds"),
					Language.tr("Surface.Tank.Dialog.Unit.DeltaMinutes"),
					Language.tr("Surface.Tank.Dialog.Unit.DeltaHours"),
					Language.tr("Surface.Tank.Dialog.Unit.DeltaDays")
			};
			data=getInputPanel(Language.tr("Surface.Tank.Dialog.AnalogNotify")+":","",10);
			tabInner.add(line=(JPanel)data[0]);
			analogNotify=(JTextField)data[1];
			line.add(analogNotifyUnit=new JComboBox<>(unitNames));
			loadValue(tank.getAnalogNotify(),analogNotify,analogNotifyUnit,false);

			/* Tab: Ventile */
			tabs.add(Language.tr("Surface.Tank.Dialog.Tab.Valves"),tab=new JPanel(new BorderLayout()));

			final JTableExt valvesTable;
			tab.add(new JScrollPane(valvesTable=new JTableExt()),BorderLayout.CENTER);
			valvesTable.setModel(valvesTableModel=new ModelElementTankTableModel(valvesTable,tank.getValves(),readOnly));
			valvesTable.getColumnModel().getColumn(0).setMaxWidth(50);
			valvesTable.getColumnModel().getColumn(0).setMinWidth(50);
			valvesTable.getColumnModel().getColumn(2).setMaxWidth(125);
			valvesTable.getColumnModel().getColumn(2).setMinWidth(125);
			valvesTable.getColumnModel().getColumn(3).setMaxWidth(150);
			valvesTable.getColumnModel().getColumn(3).setMinWidth(150);
			valvesTable.setIsPanelCellTable(2);
			valvesTable.setIsPanelCellTable(3);
			valvesTable.setEnabled(!readOnly);

			/* Icons */
			tabs.setIconAt(0,Images.GENERAL_SETUP.getIcon());
			tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_TANK_VALVE.getIcon());

			/* Start */
			checkData(false);
		}

		return tabs;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;
		Double D;

		/* Tab: Allgemeine Einstellungen */

		/* Kapazität */
		D=NumberTools.getPositiveDouble(capacity,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Tank.Dialog.Capacity.ErrorTitle"),String.format(Language.tr("Surface.Tank.Dialog.Capacity.ErrorInfo"),capacity.getText()));
				return false;
			}
		}

		/* Initialwert */
		D=NumberTools.getNotNegativeDouble(initalValue,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Tank.Dialog.InitialValue.ErrorTitle"),String.format(Language.tr("Surface.Tank.Dialog.InitialValue.ErrorInfo"),initalValue.getText()));
				return false;
			}
		}

		/* Änderungsbenachrichtigung */
		D=NumberTools.getPositiveDouble(analogNotify,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Tank.Dialog.AnalogNotify.ErrorTitle"),String.format(Language.tr("Surface.Tank.Dialog.AnalogNotify.ErrorInfo"),analogNotify.getText()));
				return false;
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementTank) {
			final ModelElementTank tank=(ModelElementTank)element;

			/* Tab: Allgemeine Einstellungen */
			tank.setCapacity(NumberTools.getDouble(capacity,true));
			tank.setInitialValue(NumberTools.getDouble(initalValue,true));
			final Double D=NumberTools.getPositiveDouble(analogNotify,true);
			tank.setAnalogNotify(D.doubleValue()*MULTIPLY[analogNotifyUnit.getSelectedIndex()]);

			/* Tab: Ventile */
			valvesTableModel.storeData();
		}
	}
}
