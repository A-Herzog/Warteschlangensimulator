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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSeize}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSeize
 */
public class ModelElementSeizeDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1005631480133240168L;

	/** Eingabefeld für die Ressourcenpriorität */
	private JTextField textResourcePriority;
	/** Tabelle zur Definition der zu belegenden Ressourcen */
	private ResourceTableModel tableResource;

	/** Auswahlbox: Timeout verwenden? */
	private JCheckBox hasTimeOut;
	/** Eingabefeld für Timeout-Werte */
	private JTextField timeOut;
	/** Zeiteinheit für Timeout-Werte */
	private JComboBox<String> timeOutTimeBase;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSeize}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSeizeDialog(final Component owner, final ModelElementSeize element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Seize.Dialog.Title"),element,"ModelElementSeize",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSeize;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		JPanel sub;

		/* Ressourcenpriorität */
		final Object[] data=getInputPanel(Language.tr("Surface.Seize.Dialog.ResourcePriority")+":",((ModelElementSeize)element).getResourcePriority());
		sub=(JPanel)data[0];
		textResourcePriority=(JTextField)data[1];
		sub.add(getExpressionEditButton(this,textResourcePriority,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		textResourcePriority.setEnabled(!readOnly);
		content.add(sub,BorderLayout.NORTH);
		textResourcePriority.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});

		/* Ressourcen */
		final JTable table;
		content.add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(tableResource=new ResourceTableModel(((JTableExt)table),((ModelElementSeize)element).getNeededResources(),element.getModel(),element.getSurface().getResources(),readOnly,helpRunnable));
		table.getColumnModel().getColumn(1).setMaxWidth(275);
		table.getColumnModel().getColumn(1).setMinWidth(275);
		((JTableExt)table).setIsPanelCellTable(0);
		((JTableExt)table).setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		/* Setup-Bereich unten */
		final JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.SOUTH);

		/* Timeout */
		setup.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(hasTimeOut=new JCheckBox(Language.tr("Surface.Seize.Dialog.Timeout")+":"));
		hasTimeOut.setEnabled(!readOnly);
		sub.add(timeOut=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(timeOut);
		timeOut.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {hasTimeOut.setSelected(true); checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {hasTimeOut.setSelected(true); checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {hasTimeOut.setSelected(true); checkInput(false);}
		});
		timeOut.setEnabled(!readOnly);
		sub.add(timeOutTimeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeOutTimeBase.addActionListener(e->{hasTimeOut.setSelected(true); checkInput(false);});
		timeOutTimeBase.setEnabled(!readOnly);
		double timeOutValue=((ModelElementSeize)element).getTimeOut();
		if (timeOutValue<0) {
			timeOut.setText("0");
			timeOutTimeBase.setSelectedIndex(0);
			hasTimeOut.setSelected(false);
		} else {
			hasTimeOut.setSelected(true);
			int index=0;
			if (timeOutValue>60) {
				/* Sekunden -> Minuten */
				timeOutValue/=60;
				index++;
				if (timeOutValue>60) {
					/* Minuten -> Stunden */
					timeOutValue/=60;
					index++;
					if (timeOutValue>24) {
						/* Stunden -> Tage */
						timeOutValue/=24;
						index++;
					}
				}
			}
			timeOut.setText(NumberTools.formatNumberMax(timeOutValue));
			timeOutTimeBase.setSelectedIndex(index);
		}

		/* Button zum Öffnen der Ressourceneinstellungen */
		final JButton resourceButton=getOpenModelOperatorsButton();
		if (resourceButton!=null) {
			setup.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			sub.add(resourceButton);
		}

		return content;
	}

	/**
	 * Überprüft die Eingaben
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird im Fehlerfall eine Fehlermeldung ausgegeben
	 * @return	Gibt <code>true</code> zurück, wenn die Eingaben in Ordnung sind
	 */
	private boolean checkInput(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		final int error=ExpressionCalc.check(textResourcePriority.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false),element.getModel().userFunctions);
		if (error>=0) {
			textResourcePriority.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Seize.Dialog.ResourcePriority.Error.Title"),String.format(Language.tr("Surface.Seize.Dialog.ResourcePriority.Error.Info"),textResourcePriority.getText()));
				return false;
			}
			ok=false;
		} else {
			textResourcePriority.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (hasTimeOut.isSelected()) {
			final Double D=NumberTools.getNotNegativeDouble(timeOut,true);
			if (D==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Seize.Dialog.Timeout.ErrorTitle"),Language.tr("Surface.Seize.Dialog.Timeout.ErrorInfo"));
					return false;
				}
				ok=false;
			}
		} else {
			timeOut.setBackground(NumberTools.getTextFieldDefaultBackground());
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
		return checkInput(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSeize seize=(ModelElementSeize)element;

		seize.setResourcePriority(textResourcePriority.getText());
		tableResource.storeData(seize.getNeededResources());

		if (hasTimeOut.isSelected()) {
			seize.setTimeOut(NumberTools.getNotNegativeDouble(timeOut,true)*ModelSurface.TimeBase.byId(timeOutTimeBase.getSelectedIndex()).multiply);
		} else {
			seize.setTimeOut(-1);
		}
	}
}
