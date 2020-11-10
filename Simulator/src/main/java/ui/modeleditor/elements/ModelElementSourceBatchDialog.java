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
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dieser Dialog ermöglicht das Konfigurieren von Batch-Größen
 * bei Kundenankünften.
 * @author Alexander Herzog
 * @see ModelElementSourceRecordPanel
 */
public class ModelElementSourceBatchDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4182770820151357513L;

	/** Eingabefeld für die minimale Batch-Größe */
	private final JTextField sizeMin;
	/** Eingabefeld für die maximale Batch-Größe */
	private final JTextField sizeMax;
	/** Bereich in dem {@link #rates} angelegt werden */
	private final JComponent scroll;
	/** Bisherige Eingabe in {@link #sizeMax} (um entsprechend viele Eingabefelder vorzuhalten) */
	private long lastSizeMax=-1;
	/** Eingabefelder für die Raten mit denen die möglichen Batch-Größen auftreten */
	private List<JTextField> rates;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param helpTopic	Hilfe-Callback
	 * @param readOnly	Nur-Lese-Status
	 * @param distribution	Anfängliche Raten für die Ankunfts-Batch-Größen
	 */
	public ModelElementSourceBatchDialog(final Component owner, final String helpTopic, final boolean readOnly, final double[] distribution) {
		super(owner,Language.tr("Surface.Source.DialogBatchSize.Title"),readOnly);

		final Container finalContainer=this.owner;
		final JPanel contentPanel=createGUI(()->Help.topicModal(finalContainer,helpTopic));
		contentPanel.setLayout(new BorderLayout());

		JPanel panel;
		JLabel label;

		panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentPanel.add(panel,BorderLayout.NORTH);

		panel.add(label=new JLabel(Language.tr("Surface.Source.DialogBatchSize.Minimum")+":"));
		panel.add(sizeMin=new JTextField(5));
		sizeMin.setEditable(false);
		sizeMin.setText("1");
		label.setLabelFor(sizeMin);
		panel.add(label=new JLabel(Language.tr("Surface.Source.DialogBatchSize.Maximum")+":"));
		panel.add(sizeMax=new JTextField(5));
		sizeMax.setEditable(!readOnly);
		sizeMax.setText(""+distribution.length);
		label.setLabelFor(sizeMax);

		contentPanel.add(new JScrollPane(scroll=Box.createVerticalBox()),BorderLayout.CENTER);
		rates=new ArrayList<>();

		sizeMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {changeMaxSize(null);}
			@Override public void keyReleased(KeyEvent e) {changeMaxSize(null);}
			@Override public void keyPressed(KeyEvent e) {changeMaxSize(null);}
		});
		changeMaxSize(distribution);

		setMinSizeRespectingScreensize(450,450);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konfiguriert {@link #rates}
	 * @param distribution	Anfängliche Raten für die Ankunfts-Batch-Größen oder <code>null</code>, um nur Felder anzulegen
	 */
	private void changeMaxSize(final double[] distribution) {
		Long newSizeMax=NumberTools.getPositiveLong(sizeMax,true);
		if (newSizeMax==null) return;

		if (newSizeMax==lastSizeMax) return;
		lastSizeMax=newSizeMax;

		String[] oldData;
		if (distribution==null) {
			oldData=new String[rates.size()];
			for (int i=0;i<rates.size();i++) oldData[i]=rates.get(i).getText();

		} else {
			oldData=new String[distribution.length];
			for (int i=0;i<distribution.length;i++) oldData[i]=NumberTools.formatNumber(distribution[i]);
		}

		scroll.removeAll();
		rates.clear();

		for (int i=0;i<newSizeMax;i++) {
			final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.DialogBatchSize.Rate")+" "+(i+1)+":",(i>=oldData.length)?"1":oldData[i],5);
			scroll.add((JPanel)data[0]);
			final JTextField input=(JTextField)data[1];
			rates.add(input);
			input.setEditable(!readOnly);
			input.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});
		}

		scroll.add(Box.createVerticalGlue());
		checkData(false);
		scroll.revalidate();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;
		for (JTextField input: rates) {
			final Double D=NumberTools.getNotNegativeDouble(input,true);
			if (D==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.DialogBatchSize.Rate.Error.Title"),String.format(Language.tr("Surface.Source.DialogBatchSize.Rate.Error.Info"),rates.indexOf(input)+1,input.getText()));
					return false;
				}
				ok=false;
			}
		}
		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wurde,
	 * die Raten mit denen die jeweiligen Ankunfts-Batch-Größen
	 * gewählt werden sollen.
	 * @return	Raten für die Ankunfts-Batch-Größen
	 */
	public double[] getBatchRates() {
		final double[] result=new double[rates.size()];
		for (int i=0;i<rates.size();i++) {
			final Double D=NumberTools.getNotNegativeDouble(rates.get(i),false);
			if (D!=null) result[i]=D;
		}
		return result;
	}
}
