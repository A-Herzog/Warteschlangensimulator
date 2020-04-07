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
package mathtools.distribution.swing;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.distribution.tools.FileDropperData;

/**
 * Diese Klasse kapselt einen kompletten Verteilungseditor-Dialog.
 * @author Alexander Herzog
 * @version 1.0
 */
public class JDistributionEditorDialog extends JDialog {
	private static final long serialVersionUID = -1643400760560410606L;

	private JDistributionPanel plotter;
	private JDistributionEditorPanel editor;
	private JButton okButton;
	private JButton cancelButton;
	private boolean okButtonPressed=false;

	/**
	 * Konstruktor der Klasse <code>DistributionEditorDialog</code>
	 * @param owner	 Übergeordnetes Fenster
	 * @param distribution	Zu bearbeitende Ausgangsverteilung
	 * @param maxXValue	Maximal darzustellender x-Wert
	 * @param plotType 	Einstellen des Darstellungstyps (nur Dichte, nur Verteilungsfunktion, beides)
	 * @param allowDistributionTypeChange	Gibt an, ob im Editor der Typ der Verteilung geändert werden darf.
	 * @param allowOk Gibt an, ob der Dialog mit "Ok" geschlossen werden darf.
	 * @param imageSize	Gibt die Größe des Bildes beim Speichern an.
	 */
	public JDistributionEditorDialog(final Window owner, final AbstractRealDistribution distribution, final double maxXValue, final int plotType, final boolean allowDistributionTypeChange, final boolean allowOk, final int imageSize) {
		super(owner,JDistributionEditorPanel.DialogTitle,Dialog.ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {@Override
			public void windowClosing(WindowEvent event) {setVisible(false);}});
		setResizable(false);
		setLayout(new BorderLayout());

		JPanel mainPanel=new JPanel(new BorderLayout()); add(mainPanel,BorderLayout.CENTER);

		mainPanel.add(plotter=new JDistributionPanel(distribution,maxXValue,false),BorderLayout.CENTER);
		plotter.setImageSaveSize(imageSize);
		plotter.setPreferredSize(new Dimension(675,450));
		plotter.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		plotter.setPlotType(plotType);
		mainPanel.add(editor=new JDistributionEditorPanel(distribution,maxXValue,new EditorActionEvents(),allowDistributionTypeChange),BorderLayout.SOUTH);

		JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); add(buttonPanel,BorderLayout.SOUTH);

		buttonPanel.add(okButton=new JButton(JDistributionEditorPanel.ButtonOk));
		okButton.addActionListener(new ButtonActionEvents());
		okButton.setEnabled(allowOk);
		okButton.setIcon(SimSystemsSwingImages.OK.getIcon());

		buttonPanel.add(cancelButton=new JButton(JDistributionEditorPanel.ButtonCancel));
		cancelButton.addActionListener(new ButtonActionEvents());
		cancelButton.setIcon(SimSystemsSwingImages.CANCEL.getIcon());

		getRootPane().setDefaultButton(okButton);

		Dimension sc=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d;
		d=getMaximumSize(); d.width=Math.min(Math.min(d.width,sc.width-50),1000); setMaximumSize(d);
		d=getPreferredSize(); d.width=Math.min(Math.min(d.width,sc.width-50),1000); setPreferredSize(d);

		pack();
		setLocationRelativeTo(owner);
	}

	private class ButtonActionEvents implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			if (e.getSource()==okButton) {okButtonPressed=true;	setVisible(false); return;}
			if (e.getSource()==cancelButton) {setVisible(false); return;}
			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				if (editor.loadFromFile(data.getFile())) data.dragDropConsumed();
				return;
			}
		}
	}

	private class EditorActionEvents implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			if (e.getSource()!=editor) return;
			plotter.setDistribution(editor.getDistribution());
		}
	}

	/**
	 * Liefert nach dem Schließen des Dialogs die neue Verteilung zurück.
	 * @return	Wurde der Dialog per "Ok"-Button geschlossen, so liefert die Funktion ein Objekt des Typs <code>AbstractContinuousDistribution</code> zurück, andernfalls wird <code>null</code> zurückgeliefert.
	 */
	public AbstractRealDistribution getNewDistribution() {
		if (okButtonPressed) return plotter.getDistribution(); else return null;
	}
}
