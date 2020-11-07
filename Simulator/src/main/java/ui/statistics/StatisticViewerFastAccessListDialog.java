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
package ui.statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog zum Bearbeiten eines einzelnen {@link FilterListRecord}-Elements
 * @author Alexander Herzog
 * @see FilterList
 * @see FilterListRecord
 * @see StatisticViewerFastAccessList
 */
public final class StatisticViewerFastAccessListDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7668518779952201569L;

	/**
	 * Statistik-Objekt für die Abfrage der möglichen XML-Elemente
	 */
	private final Statistics statistics;

	/**
	 * Hilfe-Runnable
	 */
	private final Runnable help;

	/** Auswahlbox für die Art zu Eintrags */
	private final JComboBox<JLabel> modeCombo;
	/** Gewählte Texte je {@link #modeCombo}-Eintrag */
	private String[] lastTexts;
	/** Letzter in {@link #modeCombo} gewählter Index */
	private int lastComboIndex;
	/** Eingabefeld für Texte */
	private final JTextField textEdit;
	/** Schaltfläche zur Auswahl eines XML-Elements */
	private final JButton selectButton;
	/** Schaltfläche zum Bearbeiten eines Rechenausdrucks */
	private final JButton expressionButton;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param statistics	Statistik-Objekt für die Abfrage der möglichen XML-Elemente
	 * @param record	Bisheriger Listeneintrag (kann <code>null</code> sein)
	 * @param help	Hilfe-Runnable
	 * @param isAdd	Handelt es sich um einen neuen Eintrag (<code>true</code>) oder um die Bearbeitung eines bestehenden (<code>false</code>)
	 */
	public StatisticViewerFastAccessListDialog(final Component owner, final Statistics statistics, final FilterListRecord record, final Runnable help, final boolean isAdd) {
		super(owner,Language.tr("Statistic.FastAccess.FilterList.Edit.Dialog.Title"));
		this.statistics=statistics;
		this.help=help;

		/* GUI aufbauen */

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		final JPanel main=new JPanel();
		content.add(main,BorderLayout.NORTH);
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Statistic.FastAccess.FilterList.Edit.Dialog.Mode")+":"));
		line.add(modeCombo=new JComboBox<>(getModes()));
		label.setLabelFor(modeCombo);
		modeCombo.setRenderer(new DefaultListCellRenderer() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -8968209869189192315L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				final Component result=super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
				if ((result instanceof JLabel) && (value instanceof JLabel)) {
					((JLabel)result).setText(((JLabel)value).getText());
					((JLabel)result).setIcon(((JLabel)value).getIcon());
				}
				return result;
			}
		});

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Statistic.FastAccess.FilterList.Edit.Dialog.Text")+":","");
		main.add(line=(JPanel)data[0]);
		textEdit=(JTextField)data[1];

		final JPanel buttonsPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(buttonsPanel,BorderLayout.EAST);

		buttonsPanel.add(selectButton=new JButton());
		selectButton.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Edit.Dialog.Select.Hint"));
		selectButton.setIcon(Images.EDIT_ADD.getIcon());
		selectButton.addActionListener(e->commandSelect());

		buttonsPanel.add(expressionButton=new JButton());
		expressionButton.setToolTipText(Language.tr("Statistic.FastAccess.FilterList.Edit.Dialog.Expression.Hint"));
		expressionButton.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		expressionButton.addActionListener(e->commandExpression());

		/* Dialog vorbereiten (bevor Daten in das Eingabefeld geladen werden) */

		setMinSizeRespectingScreensize(700,0);
		setMaximumSize(new Dimension(1000,500));
		pack();
		setLocationRelativeTo(getOwner());

		/* Daten laden */

		if (record==null) {
			modeCombo.setSelectedIndex(0);
			lastComboIndex=-1;
			comboUpdated();
		} else {
			modeCombo.setSelectedIndex(0);
			FilterListRecord.Mode[] modes=FilterListRecord.Mode.values();
			for (int i=0;i<modes.length;i++) if (modes[i]==record.mode) {modeCombo.setSelectedIndex(i); break;}
			lastComboIndex=-1;
			comboUpdated();
			textEdit.setText(record.text);
		}
		modeCombo.addActionListener(e->comboUpdated());

		/* Bei neuen Listeneintrag ggf. automatisch weitere Dialoge öffnen */

		if (isAdd && record!=null) {
			if (record.mode==FilterListRecord.Mode.Expression) SwingUtilities.invokeLater(()->commandExpression());
		}

		/* Dialog starten */

		setVisible(true);
	}

	/**
	 * Liefert die in {@link FilterListRecord.Mode} verfügbaren
	 * Modi für {@link #modeCombo}.
	 * @return	Liste der verfügbaren Modi für {@link FilterListRecord}
	 * @see FilterListRecord.Mode
	 * @see #modeCombo
	 */
	private JLabel[] getModes() {
		return Arrays.asList(FilterListRecord.Mode.values()).stream().map(mode->{JLabel label=new JLabel(); FilterListRecord.writeToJLabel(mode,null,label); return label;}).toArray(JLabel[]::new);
	}

	/**
	 * Wird aufgerufen, wenn sich der gewählte Eintrag
	 * in {@link #modeCombo} geändert hat.
	 * @see #modeCombo
	 */
	private void comboUpdated() {
		/* Alten Text sichern*/
		if (lastComboIndex>=0) {
			if (lastTexts==null) lastTexts=new String[FilterListRecord.Mode.values().length];
			lastTexts[lastComboIndex]=textEdit.getText();
		}

		/* Text für neuen Modus wiederherstellen */
		lastComboIndex=modeCombo.getSelectedIndex();
		String newText="";
		if (lastTexts!=null && lastTexts[lastComboIndex]!=null) newText=lastTexts[lastComboIndex];
		textEdit.setText(newText);

		/* Eingabezeile aktivieren/deaktivieren */
		final FilterListRecord.Mode mode=FilterListRecord.Mode.values()[lastComboIndex];
		textEdit.setEditable(mode.hasText && mode!=FilterListRecord.Mode.XML);
		selectButton.setVisible(mode==FilterListRecord.Mode.XML);
		expressionButton.setVisible(mode==FilterListRecord.Mode.Expression);
	}

	/**
	 * Wird beim Klicken auf {@link #selectButton} aufgerufen.
	 * Zeigt ein Popupmenü zur Auswahl eines XML-Elements an.
	 * @see #selectButton
	 */
	private void commandSelect() {
		final ListPopup helper=new ListPopup(selectButton,help);
		helper.popupCustom(statistics,record->textEdit.setText(record.xml),record->true,true);
	}

	/**
	 * Wird beim Klicken auf {@link #expressionButton} aufgerufen.
	 * Zeigt einen Dialog zum Bearbeiten des Ausdrucks an.
	 * @see #expressionButton
	 * @see ExpressionBuilder
	 */
	private void commandExpression() {
		final ModelSurface mainSurface=statistics.editModel.surface;
		final ExpressionBuilder builder=new ExpressionBuilder(this,textEdit.getText(),false,new String[0],new HashMap<>(),ExpressionBuilder.getStationIDs(mainSurface),ExpressionBuilder.getStationNameIDs(mainSurface),false,true,false);
		builder.setVisible(true);
		if (builder.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		textEdit.setText(builder.getExpression());
	}

	/**
	 * Liefert für den Fall, dass der Dialog mit "Ok" geschlossen wurde, ein neues {@link FilterListRecord}-Element.
	 * @return	Neues {@link FilterListRecord}-Element
	 */
	public FilterListRecord getRecord() {
		final FilterListRecord record=new FilterListRecord();
		record.mode=FilterListRecord.Mode.values()[modeCombo.getSelectedIndex()];
		if (record.mode.hasText) record.text=textEdit.getText();
		return record;
	}
}
