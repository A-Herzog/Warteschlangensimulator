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
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Editor für die Batch-Raten in Form von mehreren Eingabefeldern
 * @author Alexander Herzog
 * @see ModelElementSourceBatchDialog
 * @see ModelElementSourceBatchEditor
 */
public class ModelElementSourceBatchEditor1 extends ModelElementSourceBatchEditorAbstract {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-7072092110112499033L;

	/** Eingabefeld für die minimale Batch-Größe */
	private final JTextField sizeMin;
	/** Eingabefeld für die maximale Batch-Größe */
	private final JTextField sizeMax;
	/** Tools-Schaltfläche */
	private final JButton tools;
	/** Bereich in dem {@link #rates} angelegt werden */
	private final JComponent scroll;
	/** Bisherige Eingabe in {@link #sizeMax} (um entsprechend viele Eingabefelder vorzuhalten) */
	private int lastSizeMax=-1;
	/** Eingabefelder für die Raten mit denen die möglichen Batch-Größen auftreten */
	private List<JTextField> rates;

	/**
	 * Konstruktor der Klasse
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementSourceBatchEditor1(final boolean readOnly) {
		super(readOnly);

		JLabel label;

		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(panel,BorderLayout.NORTH);

		panel.add(label=new JLabel(Language.tr("Surface.Source.DialogBatchSize.Minimum")+":"));
		panel.add(sizeMin=new JTextField(5));
		sizeMin.setEditable(false);
		sizeMin.setText("1");
		label.setLabelFor(sizeMin);
		panel.add(label=new JLabel(Language.tr("Surface.Source.DialogBatchSize.Maximum")+":"));
		panel.add(sizeMax=new JTextField(5));
		sizeMax.setEditable(!readOnly);
		sizeMax.setText("1");
		label.setLabelFor(sizeMax);

		panel.add(tools=new JButton(Images.GENERAL_TOOLS.getIcon()));
		tools.setToolTipText(Language.tr("Surface.Source.DialogBatchSize.Tools.Hint"));
		tools.addActionListener(e->showToolsPopup(tools));

		add(new JScrollPane(scroll=Box.createVerticalBox()),BorderLayout.CENTER);
		rates=new ArrayList<>();

		sizeMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (changeMaxSize()) fireChangeListeners();}
			@Override public void keyReleased(KeyEvent e) {if (changeMaxSize()) fireChangeListeners();}
			@Override public void keyPressed(KeyEvent e) {if (changeMaxSize()) fireChangeListeners();}
		});
	}

	/**
	 * Konfiguriert {@link #rates}
	 * @return	Gibt <code>true</code> zurück, wenn sich die Anzahl an Raten-Feldern geändert hat
	 */
	private boolean changeMaxSize() {
		final Long newSizeMax=NumberTools.getPositiveLong(sizeMax,true);
		if (newSizeMax==null) return false;

		if (newSizeMax==lastSizeMax) return false;
		lastSizeMax=newSizeMax.intValue();

		final String[] oldData=new String[rates.size()];
		for (int i=0;i<rates.size();i++) oldData[i]=rates.get(i).getText();

		scroll.removeAll();
		rates.clear();

		for (int i=0;i<newSizeMax;i++) {
			final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.DialogBatchSize.Rate")+" "+(i+1)+":",(i>=oldData.length)?"1":oldData[i],5);
			scroll.add((JPanel)data[0]);
			final JTextField input=(JTextField)data[1];
			rates.add(input);
			input.setEditable(!readOnly);
			input.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false); fireChangeListeners();}
				@Override public void keyReleased(KeyEvent e) {checkData(false); fireChangeListeners();}
				@Override public void keyPressed(KeyEvent e) {checkData(false); fireChangeListeners();}
			});
		}

		scroll.add(Box.createVerticalGlue());
		checkData(false);
		scroll.revalidate();

		return true;
	}

	@Override
	public boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;
		double sum=0.0;

		for (JTextField input: rates) {
			final Double D=NumberTools.getNotNegativeDouble(input,true);
			if (D==null || D<0) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.DialogBatchSize.Rate.Error.Title"),String.format(Language.tr("Surface.Source.DialogBatchSize.Rate.Error.Info"),rates.indexOf(input)+1,input.getText()));
					return false;
				}
				ok=false;
			} else {
				sum+=D;
			}
		}

		if (ok && sum==0.0) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Source.DialogBatchSize.Rate.ErrorSum.Title"),Language.tr("Surface.Source.DialogBatchSize.Rate.ErrorSum.Info"));
				return false;
			}
		}

		return ok;
	}

	@Override
	public void setDistribution(double[] distribution) {
		if (distribution==null || distribution.length==0) return;

		if (lastSizeMax!=distribution.length) {
			sizeMax.setText(""+distribution.length);
			changeMaxSize();
		}

		for (int i=0;i<distribution.length;i++) rates.get(i).setText(NumberTools.formatNumber(distribution[i],3));
	}

	@Override
	public double[] getDistribution() {
		if (lastSizeMax<=0) return new double[0];
		final double[] result=new double[lastSizeMax];
		for (int i=0;i<rates.size();i++) {
			final Double D=NumberTools.getNotNegativeDouble(rates.get(i),false);
			if (D!=null) result[i]=D;
		}
		return result;
	}

	@Override
	public String getEditorName() {
		return Language.tr("Surface.Source.DialogBatchSize.EditorName.Rates");
	}

	/**
	 * Zeigt das Tools-Popupmenü zur Konfiguration aller Raten an.
	 * @param parent	Übergeordnetes Element zur Ausrichtung des Popupmenüs
	 * @see #tools
	 */
	private void showToolsPopup(final JButton parent) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Surface.Source.DialogBatchSize.Tools.All0")));
		item.addActionListener(e->{rates.forEach(rate->rate.setText("0")); fireChangeListeners();});

		popup.add(item=new JMenuItem(Language.tr("Surface.Source.DialogBatchSize.Tools.All1")));
		item.addActionListener(e->{rates.forEach(rate->rate.setText("1")); fireChangeListeners();});

		popup.show(parent,0,parent.getHeight());
	}
}
