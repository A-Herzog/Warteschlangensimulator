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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

/**
 * Diese Klasse kapselt einen kompletten Verteilungseditor-Dialog.
 * @author Alexander Herzog
 * @version 1.1
 */
public class JDistributionEditorDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1643400760560410606L;

	/** Verteilungsplotter */
	private JDistributionPanel plotter;

	/** Editor-Panel für Verteilungstyp und Parameter */
	private JDistributionEditorPanel editor;

	/** "Ok"-Schaltfläche */
	private JButton okButton;

	/** "Abbrechen"-Schaltfläche */
	private JButton cancelButton;

	/**
	 * Gibt an, ob die "Ok"-Schaltfläche angeklickt wurde.
	 * @see #getNewDistribution()
	 */
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
		super(owner,JDistributionEditorPanel.DialogTitle,Dialog.ModalityType.DOCUMENT_MODAL);

		/* GUI */
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent event) {setVisible(false);}
		});
		setLayout(new BorderLayout());
		final JPanel mainPanel=new JPanel(new BorderLayout()); add(mainPanel,BorderLayout.CENTER);

		/* Plotter */
		mainPanel.add(plotter=new JDistributionPanel(distribution,maxXValue,false),BorderLayout.CENTER);
		plotter.setImageSaveSize(imageSize);
		plotter.setPreferredSize(new Dimension(675,450));
		plotter.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		plotter.setPlotType(plotType);
		mainPanel.add(editor=new JDistributionEditorPanel(
				distribution,
				maxXValue,
				e->{if (e.getSource()==editor) plotter.setDistribution(editor.getDistribution());},
				allowDistributionTypeChange
				),BorderLayout.SOUTH);

		/* Button-Zeile */
		JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); add(buttonPanel,BorderLayout.SOUTH);

		/* Ok */
		buttonPanel.add(okButton=new JButton(JDistributionEditorPanel.ButtonOk));
		okButton.addActionListener(e->{okButtonPressed=true; setVisible(false);});
		okButton.setEnabled(allowOk);
		okButton.setIcon(SimSystemsSwingImages.OK.getIcon());

		/* Abbruch */
		buttonPanel.add(cancelButton=new JButton(JDistributionEditorPanel.ButtonCancel));
		cancelButton.addActionListener(e->setVisible(false));
		cancelButton.setIcon(SimSystemsSwingImages.CANCEL.getIcon());

		/* Hotkey für "Ok" */
		getRootPane().setDefaultButton(okButton);

		/* Dialog vorbereiten */
		setResizable(false);
		final Dimension sc=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d;
		d=getMaximumSize(); d.width=Math.min(Math.min(d.width,sc.width-50),1000); setMaximumSize(d);
		d=getPreferredSize(); d.width=Math.min(Math.min(d.width,sc.width-50),1000); setPreferredSize(d);
		pack();
		setLocationRelativeTo(owner);
	}

	@Override
	protected JRootPane createRootPane() {
		final JRootPane rootPane=new JRootPane();
		final InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=5496549055021258321L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		return rootPane;
	}

	/**
	 * Liefert nach dem Schließen des Dialogs die neue Verteilung zurück.
	 * @return	Wurde der Dialog per "Ok"-Button geschlossen, so liefert die Funktion ein Objekt des Typs <code>AbstractContinuousDistribution</code> zurück, andernfalls wird <code>null</code> zurückgeliefert.
	 */
	public AbstractRealDistribution getNewDistribution() {
		if (okButtonPressed) return plotter.getDistribution(); else return null;
	}
}
