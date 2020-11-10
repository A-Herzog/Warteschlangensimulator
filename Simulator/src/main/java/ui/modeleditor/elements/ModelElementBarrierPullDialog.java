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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementBarrierPull}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementBarrierPull
 */
public class ModelElementBarrierPullDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8181228879880400557L;

	/** Namen (zur Speicherung) der als kontrollierte Stationen in Frage kommende Stationen */
	private List<String> stations;
	/** Ausführliche Namen (inkl. Eltern-Station) der als kontrollierte Stationen in Frage kommende Stationen */
	private List<String> stationsLong;
	/** Auswahlbox für die kontrollierte Station */
	private JComboBox<String> select;
	/** Eingabefeld für die maximale Anzahl an Kunden im Segment */
	private JTextField maxEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementBarrierPull}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementBarrierPullDialog(final Component owner, final ModelElementBarrierPull element, final boolean readOnly) {
		super(owner,Language.tr("Surface.BarrierPull.Dialog.Title"),element,"ModelElementBarrierPull",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Prüft, ob eine Station als kontrollierte Station in Frage kommt
	 * @param element	Zu prüfende Station
	 * @return	Liefert <code>true</code>, wenn die Station <b>nicht</b> als kontrollierte Station in Frage kommt
	 * @see #buildStationsList(ModelSurface, String)
	 */
	private boolean forbiddenStation(final ModelElementBox element) {
		if (element instanceof ModelElementSource) return true;
		if (element instanceof ModelElementSourceMulti) return true;
		if (element instanceof ModelElementSourceTable) return true;
		if (element instanceof ModelElementSourceDB) return true;
		if (element instanceof ModelElementSourceDDE) return true;
		if (element instanceof ModelElementDispose) return true;
		if (element instanceof ModelElementStateStatistics) return true;
		if (element instanceof ModelElementTankSensor) return true;
		if (element instanceof ModelElementTank) return true;
		if (element instanceof ModelElementAnalogValue) return true;
		return false;
	}

	/**
	 * Erstellt die Liste der Stationen, die als kontrollierte Stationen in Frage kommen.
	 * @param surface	Zeichenfläche, die durchsucht werden soll (untergeordnete Zeichenflächen werden ebenfalls durchsucht)
	 * @param parentElementName	Name für übergeordnetes Untermodell-Element (ist <code>null</code>, wenn es um die Haupt-Zeichenfläche geht)
	 * @see #stations
	 * @see #stationsLong
	 */
	private void buildStationsList(final ModelSurface surface, final String parentElementName) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementBox && !element.getName().trim().isEmpty()) {
				final String name=element.getName();
				if (!stations.contains(name) && !forbiddenStation((ModelElementBox)element)) {
					stations.add(name);
					if (parentElementName==null) {
						stationsLong.add(String.format("%s (id=%d)",name,element.getId()));
					} else {
						stationsLong.add(String.format("%s -> %s (id=%d)",parentElementName,name,element.getId()));
					}
				}
			}
			if (element instanceof ModelElementSub) {
				final String parentName;
				if (element.getName().trim().isEmpty()) {
					parentName=String.format("id=%d",element.getId());
				} else {
					parentName=String.format("%s (id=%d)",element.getName(),element.getId());
				}
				buildStationsList(((ModelElementSub)element).getSubSurface(),parentName);
			}
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationBarrierPull;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Stationenliste zusammenstellen */
		stations=new ArrayList<>();
		stationsLong=new ArrayList<>();
		ModelSurface surface=element.getSurface();
		if (surface.getParentSurface()!=null) surface=surface.getParentSurface();
		buildStationsList(surface,null);

		/* Überwachtes Element */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		final JLabel label=new JLabel(Language.tr("Surface.BarrierPull.Dialog.ControlledElement")+":");
		line.add(label);

		line.add(select=new JComboBox<>(stationsLong.toArray(new String[0])));
		select.setEnabled(!readOnly);
		final String selectString=((ModelElementBarrierPull)element).getNextName();
		int selectIndex=stations.indexOf(selectString);
		if (selectIndex<0 && stations.size()>0) selectIndex=0;
		if (selectIndex>=0) select.setSelectedIndex(selectIndex);
		label.setLabelFor(select);

		/* Maximalanzahl */
		final Object[] data=getInputPanel(Language.tr("Surface.BarrierPull.Dialog.MaxNumber")+":",""+((ModelElementBarrierPull)element).getNextMax());
		content.add((JPanel)data[0]);
		maxEdit=(JTextField)data[1];
		maxEdit.setEditable(!readOnly);
		maxEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,maxEdit,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (select.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.BarrierPull.Dialog.ControlledElement.ErrorTitle"),Language.tr("Surface.BarrierPull.Dialog.ControlledElement.ErrorInfo"));
				return false;
			}
		}

		final String text=maxEdit.getText();
		if (!text.trim().isEmpty()) {
			final int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				maxEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.BarrierPull.Dialog.MaxNumber.ErrorTitle"),String.format(Language.tr("Surface.BarrierPull.Dialog.MaxNumber.ErrorInfo"),maxEdit.getText(),error+1));
					return false;
				}
			} else {
				maxEdit.setBackground(SystemColor.text);
			}
		} else {
			maxEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.BarrierPull.Dialog.MaxNumber.ErrorTitle"),Language.tr("Surface.BarrierPull.Dialog.MaxNumber.ErrorInfoEmpty"));
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
		super.storeData();

		String station="";
		if (select.getSelectedIndex()>=0) station=stations.get(select.getSelectedIndex());
		((ModelElementBarrierPull)element).setNextName(station);
		((ModelElementBarrierPull)element).setNextMax(maxEdit.getText());
	}
}