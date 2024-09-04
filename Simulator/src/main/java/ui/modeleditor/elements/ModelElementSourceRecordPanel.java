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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.logging.log4j.util.Strings;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.expressionbuilder.ExpressionBuilder;
import ui.expressionbuilder.ExpressionBuilderAutoComplete;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurface.TimeBase;
import ui.modeleditor.coreelements.ModelElement;
import ui.modelproperties.ModelPropertiesDialogPageClients;

/**
 * Hält die GUI-Elemente für die Bearbeitung einer Kundenquelle vor
 * @author Alexander Herzog
 * @see ModelElementSourceDialog
 * @see ModelElementSourceMultiDialog
 */
public final class ModelElementSourceRecordPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3050008572741778868L;

	/** Gibt an, ob diese Quelle von sich aus Kunden generiert (<code>true</code>) oder nur von außen angestoßen wird (<code>false</code>). */
	private final boolean hasOwnArrivals;
	/** Kann der Datensatz deaktiviert werden? */
	private final boolean hasActivation;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden) */
	private final EditModel model;
	/** Zeichenoberfläche */
	private final ModelSurface surface;
	/** Hilfe-Runnable */
	private final Runnable helpRunnable;

	/**
	 * Zu bearbeitender Datensatz
	 * @see #setData(ModelElementSourceRecord, ModelElement)
	 * @see #getData(ModelElementSourceRecord)
	 */
	private ModelElementSourceRecord lastRecord;

	/**
	 * Name des Datensatzes beim Aufruf
	 * (um bei {@link #getData(ModelElementSourceRecord)} evtl. ein Umbenenn-Ereignis auszulösen)
	 * @see #setData(ModelElementSourceRecord, ModelElement)
	 */
	private String lastName;

	/** Panel zur Namenseingabe (wenn der Datensatz einen Namen besitzt) */
	private final JPanel namePanel;
	/** Eingabefeld für den Namen */
	private final JTextField nameEdit;
	/** Dialog zum Bearbeiten der Kundentypeigenschaften aufrufen ({@link #editClientData()}) */
	private final JButton nameButton;
	/** Ist der Datensatz aktiv? */
	private final JCheckBox activeCheckBox;

	/** Registerreiter der Dialogs */
	private final JTabbedPane tabs;

	/** Inhaltsbereich zum Einblenden verschiedener Einstellungen */
	private JPanel cards;
	/** Auswahlbox für die in {@link #cards} anzuzeigenden Inhalte */
	private final JComboBox<String> selectCard;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Wahrscheinlichkeitsverteilung" */

	/** Zeiteinheit für {@link #distributionPanel} */
	private final JComboBox<String> timeBase1;
	/** Definition der Zwischenankunftszeiten über eine Verteilung */
	private final JDistributionPanel distributionPanel;
	/** Erste Ankunft zum Zeitpunkt 0? */
	private final JCheckBox distributionFirstArrivalAt0;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Ausdruck" */

	/** Zeiteinheit für {@link #expression} */
	private JComboBox<String> timeBase2;
	/** Eingabefeld zur Definition der Zwischenankunftszeiten über einen Rechenausdruck */
	private final JTextField expression;
	/** Erste Ankunft zum Zeitpunkt 0? */
	private final JCheckBox expressionFirstArrivalAt0;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Zeitplan" */

	/** Liste der verfügbaren Zeitpläne */
	private final String[] scheduleNames;
	/** Auswahlbox zur Wahl des Zeitplans für die Ankunftszeitpunkte */
	private final JComboBox<String> schedule;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Bedingung" */

	/** Eingabefeld für die Ankünfte-Bedingung */
	private final JTextField condition;
	/** Eingabefeld für den minimalen Abstand zwischen zwei durch die Bedingung {@link #condition} gesteuerten Ankünften */
	private final JTextField conditionMinDistance;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Schwellenwert" */

	/** Eingabefeld für den Schwellenwert-Ausdruck */
	private final JTextField thresholdExpression;
	/** Eingabefeld für den Wert, den der Schwellenwert-Ausdruck für eine Ankunft über- oder unterschreiten soll */
	private final JTextField thresholdExpressionValue;
	/** Auswahlbox zur Festlegung ob eine Ankunft bei Über- oder Unterschreitung des Schwellenwertes ausgelöst werden soll */
	private final JComboBox<String> thresholdDirection;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Signal" */

	/** Tabelle zur Auswahl der Ereignisse, die Ankünfte auslösen */
	private final ModelElementSourceRecordSignalTableModel signalsTableModel;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Anzahlen pro Intervall" */

	/** Eingabefeld für den Zahlenwert der Zeitdauer eines Intervalls */
	private final SpinnerModel intervalExpressionsIntervalTime;
	/** Auswahlbox für die Zeiteinheit für die Zeitdauer eines Intervalls */
	private final JComboBox<String> intervalExpressionsIntervalTimeTimeBase;
	/** Eingabebox für die verschiedenen Ausdrücke für die Anzahlen an Ankünften pro Intervall */
	private final JTextArea intervalExpressions;
	/** Info-Label zur Anzeige der Anzahl an Intervallen */
	private final JLabel intervalExpressionsInfo;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Zwischenankunftszeiten pro Intervall" */

	/** Eingabefeld für den Zahlenwert der Zeitdauer eines Intervalls */
	private final SpinnerModel intervalDistributionsIntervalTime;
	/** Auswahlbox für die Zeiteinheit für die Zeitdauer eines Intervalls */
	private final JComboBox<String> intervalDistributionsIntervalTimeTimeBase;
	/** Eingabebox für die verschiedenen Ausdrücke für die Zwischenankunftszeiten pro Intervall */
	private final JTextArea intervalDistributions;
	/** Info-Label zur Anzeige der Anzahl an Intervallen */
	private final JLabel intervalDistributionsInfo;

	/* Dialogseite "Zwichenankunftszeiten" -  Karte: "Zahlenwerte" */

	/** Zeiteinheit für {@link #dataStream} */
	private final JComboBox<String> timeBase3;
	/** Auswahlbox für die Art der Zahlenwerte in {@link #dataStream} */
	private final JComboBox<String> dataStreamType;
	/** Datenstrom-Zwischenankunftszeiten am Ende wiederholen? */
	private final JCheckBox dataStreamRepeat;
	/** Erste Ankunft zum Zeitpunkt 0? */
	private final JCheckBox dataStreamFirstArrivalAt0;
	/** Zahlenwerte zur Bestimmung der Ankunfts- oder Zwischenankunftszeiten */
	private final JTextArea dataStream;
	/** Zeigt die Anzahl der Werte in {@link #dataStream} an. */
	private final JLabel dataStreamValueCount;

	/* Dialogseite "Batch-Größe" */

	/** Option: Feste Batch-Größe */
	private final JRadioButton optionFixedSize;
	/** Option: Batch-Größen Verteilung */
	private final JRadioButton optionSizesDistribution;
	/** Eingabefeld für die feste Batch-Größe im Fall {@link #optionFixedSize} */
	private final JTextField batchField;
	/** Schaltfläche zur Bearbeitung der Batch-Größen Verteilung im Fall {@link #optionSizesDistribution} */
	private final JButton batchButton;
	/** Label zur Anzeige von weiteren Informationen zur gewählten Batch-Größe, wird von {@link #updateBatchInfo()} aktualisiert */
	private final JLabel batchInfo;

	/** Verteilung der Batch-Größen für den Modus {@link #optionSizesDistribution} */
	private double[] batchRates;

	/* Dialogseite "Anzahl an Kunden" */

	/** Option: Unbegrenzte Anzahl an Ankünften */
	private final JRadioButton optionInfinite;
	/** Option: Anzahl an Ankunftsereignissen */
	private final JRadioButton optionFixedNumberArrivals;
	/** Option: Anzahl an eintreffenden Kunden insgesamt */
	private final JRadioButton optionFixedNumberClients;
	/** Eingabefeld für die Anzahl an Ankünften im Modus {@link #optionFixedNumberArrivals} */
	private final JTextField numberFieldArrivals;
	/** Eingabefeld für die Anzahl Kunden im Modus {@link #optionFixedNumberClients} */
	private final JTextField numberFieldClients;

	/* Dialogseite "Startzeitpunkt" */

	/** Panel zur Definition des Startzeitpunkts (ist nicht immer sichtbar) */
	private JPanel arrivalStartSub;

	/** Option: Zeiteinheit gemäß Zwischenankunftszeiten */
	private JRadioButton arrivalStartTimeUnitGlobal;
	/** Option: Zeiteinheit gemäß {@link #arrivalStartTimeUnit} */
	private JRadioButton arrivalStartTimeUnitLocal;
	/** Optionale Zeiteinheit für den Startzeitpunkt {@link #arrivalStart} */
	private JComboBox<String> arrivalStartTimeUnit;

	/** Eingabefeld zur Definition des Startzeitpunkts */
	private final JTextField arrivalStart;
	/** Beschriftung (Zeiteinheit) für #arrivalStart */
	private JLabel arrivalStartTimeUnitLabel;

	/* Dialogseite "Zusätzliche Bedingung" */

	/** Zusätzliche Bedingung für Ankünfte */
	private final JTextField additionalArrivalCondition;

	/* Dialogseite "Zuweisung von Kundenvariablen" */

	/** Panel für die Konfiguration der Zuweisung von Kundenvariablen */
	private final JPanel panelNumbers;
	/** Tabelle zur Bearbeitung der Zuweisung von Kundenvariablen */
	private VariablesTableModel modelNumbers;

	/* Dialogseite "Zuweisung von Texten" */

	/** Panel für die Konfiguration der Zuweisung von Texten */
	private final JPanel panelText;
	/** Tabelle zur Bearbeitung der Zuweisung von Texten */
	private VariablesTextsTableModel modelText;

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceRecordPanel</code>
	 * @param readOnly	Gibt an, ob die Daten nur angezeigt (<code>true</code>) oder auch bearbeitet werden dürfen (<code>false</code>)
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenoberfläche
	 * @param helpRunnable	Hilfe-Runnable
	 * @param getSchedulesButton	Callback zum Erstellen der Schaltfläche zum Aufrufen der Zeitpläne
	 * @param hasOwnArrivals	Gibt an, ob diese Quelle von sich aus Kunden generiert (<code>true</code>) oder nur von außen angestoßen wird (<code>false</code>).
	 * @param hasActivation	Kann der Datensatz deaktiviert werden?
	 */
	public ModelElementSourceRecordPanel(final boolean readOnly, final EditModel model, final ModelSurface surface, final Supplier<JButton> getSchedulesButton, final Runnable helpRunnable, final boolean hasOwnArrivals, final boolean hasActivation) {
		super();
		this.readOnly=readOnly;
		this.model=model;
		this.surface=surface;
		this.helpRunnable=helpRunnable;
		this.hasOwnArrivals=hasOwnArrivals;
		this.hasActivation=hasActivation;
		setLayout(new BorderLayout());

		Object[] data;
		JPanel card, panel, sub, line;
		JLabel label;
		ButtonGroup buttonGroup;
		JSpinner.NumberEditor editor;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.ClientTypeName")+":","");
		add(namePanel=(JPanel)data[0],BorderLayout.NORTH);
		nameEdit=(JTextField)data[1];
		namePanel.setVisible(false);
		nameEdit.setEditable(!readOnly);
		nameEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		final JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));
		namePanel.add(buttons,BorderLayout.EAST);
		buttons.add(nameButton=new JButton());
		nameButton.setToolTipText(Language.tr("Surface.Source.Dialog.ClientTypeSettings"));
		nameButton.addActionListener(e->editClientData());
		if (hasActivation) {
			buttons.add(activeCheckBox=new JCheckBox(Language.tr("Surface.Source.Dialog.Active")));
		} else {
			activeCheckBox=null;
		}

		add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		JPanel tab;

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.Tab.InterArrivalTimes"),tab);

		/* Karten */

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);

		panel.add(label=new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes")+":"));
		panel.add(selectCard=new JComboBox<>(new String[]{
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Distribution"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Expression"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Schedule"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Condition"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Threshold"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Signals"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream")
		}));
		selectCard.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_DISTRIBUTION,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_EXPRESSION,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_SCHEDULE,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_CONDITION,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_THRESHOLD,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_SIGNALS,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_INTERVAL_EXPRESSIONS,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_INTERVAL_DISTRIBUTIONS,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_DATA_STREAM
		}));
		label.setLabelFor(selectCard);
		selectCard.addActionListener(e->{
			final int index=selectCard.getSelectedIndex();
			((CardLayout)cards.getLayout()).show(cards,"Seite"+(index+1));
			if (arrivalStartSub!=null) arrivalStartSub.setVisible(index==0 || index==1 || index==4);
			checkData(false);
			updateBatchInfo();
			updateTabTitle();
		});
		selectCard.setEnabled(!readOnly);

		tab.add(cards=new JPanel(new CardLayout()),BorderLayout.CENTER);

		/* Karte: Verteilung */

		cards.add(card=new JPanel(new BorderLayout()),"Seite1");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Distribution")+":</b></html>"));
		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		timeBase1=buildSyncedTimeBaseComboBox(sub);
		timeBase1.addActionListener(e->{checkData(false); updateBatchInfo();});
		sub.add(distributionFirstArrivalAt0=new JCheckBox(Language.tr("Surface.Source.Dialog.FirstArrivalAt0")));
		distributionFirstArrivalAt0.addActionListener(e->syncFirstArrivalAt0CheckBoxes(e));
		distributionFirstArrivalAt0.setEnabled(!readOnly);
		card.add(distributionPanel=new JDistributionPanel(new ExponentialDistribution(null,100,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY),3600,!readOnly,s->toExpression(s)) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=3060879128715618646L;

			@Override
			public void setDistribution(AbstractRealDistribution distribution) {
				super.setDistribution(distribution);
				updateBatchInfo();
			}
		},BorderLayout.CENTER);

		/* Karte: Ausdruck */

		cards.add(card=new JPanel(new BorderLayout()),"Seite2");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Expression")+":</b></html>"));
		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		timeBase2=buildSyncedTimeBaseComboBox(sub);
		timeBase2.addActionListener(e->checkData(false));
		sub.add(expressionFirstArrivalAt0=new JCheckBox(Language.tr("Surface.Source.Dialog.FirstArrivalAt0")));
		expressionFirstArrivalAt0.setEnabled(!readOnly);
		expressionFirstArrivalAt0.addActionListener(e->syncFirstArrivalAt0CheckBoxes(e));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Expression.Expression")+":","");
		sub=(JPanel)data[0];
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],false,true,model,surface),BorderLayout.EAST);
		expression=(JTextField)data[1];
		expression.setEditable(!readOnly);
		expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Karte: Zeitplan */

		cards.add(card=new JPanel(new BorderLayout()),"Seite3");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Schedule")+":</b></html>"));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(label=new JLabel(Language.tr("Surface.Source.Dialog.Schedule.Schedule")+":"));
		scheduleNames=surface.getSchedules().getScheduleNames();
		sub.add(schedule=new JComboBox<>(scheduleNames));
		schedule.setEnabled(!readOnly);

		if (!readOnly && getSchedulesButton!=null) {
			final JButton schedulesButton=getSchedulesButton.get();
			if (schedulesButton!=null) {
				panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
				sub.add(schedulesButton);
			}
		}

		/* Karte: Bedingung */

		cards.add(card=new JPanel(new BorderLayout()),"Seite4");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Condition")+":</b></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Condition.Condition")+":","");
		sub=(JPanel)data[0];
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],true,false,model,surface),BorderLayout.EAST);
		condition=(JTextField)data[1];
		condition.setEditable(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Condition.MinDistance")+":","",20);
		sub=(JPanel)data[0];
		sub.add(new JLabel(Language.tr("Surface.Source.Dialog.Condition.MinDistance.InSeconds")));
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],false,false,model,surface),BorderLayout.EAST);
		conditionMinDistance=(JTextField)data[1];
		conditionMinDistance.setEditable(!readOnly);
		conditionMinDistance.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Karte: Schwellenwert */

		cards.add(card=new JPanel(new BorderLayout()),"Seite5");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Threshold")+":</b></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Threshold.ThresholdExpression")+":","");
		sub=(JPanel)data[0];
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],false,false,model,surface),BorderLayout.EAST);
		thresholdExpression=(JTextField)data[1];
		thresholdExpression.setEditable(!readOnly);
		thresholdExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Threshold.ThresholdValue")+":","",10);
		panel.add((JPanel)data[0]);
		thresholdExpressionValue=(JTextField)data[1];
		thresholdExpressionValue.setEditable(!readOnly);
		thresholdExpressionValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(label=new JLabel(Language.tr("Surface.Source.Dialog.Threshold.Direction")));
		sub.add(thresholdDirection=new JComboBox<>(new String[]{
				Language.tr("Surface.Source.Dialog.Threshold.Direction.Up"),
				Language.tr("Surface.Source.Dialog.Threshold.Direction.Down")
		}));
		thresholdDirection.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_UP,
				Images.ARROW_DOWN
		}));
		label.setLabelFor(thresholdDirection);

		/* Karte: Signale */

		cards.add(card=new JPanel(new BorderLayout()),"Seite6");
		card.add(panel=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		final JTableExt signalTable;
		panel.add(new JScrollPane(signalTable=new JTableExt()),BorderLayout.CENTER);
		signalTable.setModel(signalsTableModel=new ModelElementSourceRecordSignalTableModel(signalTable,model.surface,readOnly));
		signalTable.setIsPanelCellTable(0);
		signalTable.setIsPanelCellTable(1);
		signalTable.getColumnModel().getColumn(0).setMinWidth(250);
		signalTable.getColumnModel().getColumn(1).setMaxWidth(100);
		signalTable.getColumnModel().getColumn(1).setMinWidth(100);
		signalTable.setEnabled(!readOnly);
		signalTable.putClientProperty("terminateEditOnFocusLost",true);
		signalTable.getTableHeader().setReorderingAllowed(false);
		signalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		/* Karte: Anzahlen pro Intervall */

		cards.add(card=new JPanel(new BorderLayout()),"Seite7");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.IntervalTime")+":"));
		final JSpinner intervalExpressionsIntervalTimeSpinner=new JSpinner(intervalExpressionsIntervalTime=new SpinnerNumberModel(1,1,50_000_000,1));
		editor=new JSpinner.NumberEditor(intervalExpressionsIntervalTimeSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(8);
		intervalExpressionsIntervalTimeSpinner.setEditor(editor);
		intervalExpressionsIntervalTimeSpinner.setEnabled(!readOnly);
		intervalExpressionsIntervalTimeSpinner.addChangeListener(e->checkData(false));
		line.add(intervalExpressionsIntervalTimeSpinner);
		label.setLabelFor(intervalExpressionsIntervalTimeSpinner);
		line.add(intervalExpressionsIntervalTimeTimeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		intervalExpressionsIntervalTimeTimeBase.setEnabled(!readOnly);
		intervalExpressionsIntervalTimeTimeBase.addActionListener(e->checkData(false));

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.Label")+":"));

		card.add(new JScrollPane(intervalExpressions=new JTextArea()),BorderLayout.CENTER);
		intervalExpressions.setEditable(!readOnly);
		intervalExpressions.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		ExpressionBuilderAutoComplete.process(new ExpressionBuilder(this,"",false,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(model.surface),ExpressionBuilder.getStationNameIDs(model.surface),true,false,false,model.userFunctions),intervalExpressions);

		card.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		final JButton expressionBuilderButton=new JButton(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.EditExpression"),Images.EXPRESSION_BUILDER.getIcon());
		panel.add(expressionBuilderButton);
		expressionBuilderButton.setEnabled(!readOnly);
		expressionBuilderButton.addActionListener(e->editIntervalExpressions());
		panel.add(Box.createHorizontalStrut(10));
		panel.add(intervalExpressionsInfo=new JLabel());

		/* Karte: Zwischenankunftszeiten pro Intervall */

		cards.add(card=new JPanel(new BorderLayout()),"Seite8");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.IntervalTime")+":"));
		final JSpinner intervalDistributionsIntervalTimeSpinner=new JSpinner(intervalDistributionsIntervalTime=new SpinnerNumberModel(1,1,50_000_000,1));
		editor=new JSpinner.NumberEditor(intervalDistributionsIntervalTimeSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(8);
		intervalDistributionsIntervalTimeSpinner.setEditor(editor);
		intervalDistributionsIntervalTimeSpinner.setEnabled(!readOnly);
		intervalDistributionsIntervalTimeSpinner.addChangeListener(e->checkData(false));
		line.add(intervalDistributionsIntervalTimeSpinner);
		label.setLabelFor(intervalDistributionsIntervalTimeSpinner);
		line.add(intervalDistributionsIntervalTimeTimeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		intervalDistributionsIntervalTimeTimeBase.setEnabled(!readOnly);
		intervalDistributionsIntervalTimeTimeBase.addActionListener(e->checkData(false));

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.Label")+":"));

		card.add(new JScrollPane(intervalDistributions=new JTextArea()),BorderLayout.CENTER);
		intervalDistributions.setEditable(!readOnly);
		intervalDistributions.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		ExpressionBuilderAutoComplete.process(new ExpressionBuilder(this,"",false,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(model.surface),ExpressionBuilder.getStationNameIDs(model.surface),true,false,false,model.userFunctions),intervalDistributions);

		card.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		final JButton distributionBuilderButton=new JButton(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.EditExpression"),Images.EXPRESSION_BUILDER.getIcon());
		panel.add(distributionBuilderButton);
		distributionBuilderButton.setEnabled(!readOnly);
		distributionBuilderButton.addActionListener(e->editIntervalDistributions());
		panel.add(Box.createHorizontalStrut(10));
		panel.add(intervalDistributionsInfo=new JLabel());

		/* Karte: Zahlenwerte */

		cards.add(card=new JPanel(new BorderLayout()),"Seite9");

		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		timeBase3=buildSyncedTimeBaseComboBox(line);
		line.add(label=new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Mode")+":"));
		line.add(dataStreamType=new JComboBox<>(new String[] {
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Mode.ArrivalTimes"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Mode.InterArrivalTimes")
		}));
		dataStreamType.setEnabled(!readOnly);
		label.setLabelFor(dataStreamType);
		line.add(dataStreamRepeat=new JCheckBox(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Mode.InterArrivalTimes.Repeat")));
		dataStreamType.addActionListener(e->dataStreamRepeat.setEnabled(dataStreamType.getSelectedIndex()==1));
		line.add(dataStreamFirstArrivalAt0=new JCheckBox(Language.tr("Surface.Source.Dialog.FirstArrivalAt0")));
		dataStreamFirstArrivalAt0.setEnabled(!readOnly);
		dataStreamFirstArrivalAt0.addActionListener(e->syncFirstArrivalAt0CheckBoxes(e));

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Values")+":"));

		card.add(new JScrollPane(dataStream=new JTextArea()),BorderLayout.CENTER);
		dataStream.setEditable(!readOnly);
		dataStream.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		label.setLabelFor(dataStream);

		card.add(panel=new JPanel(),BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JButton generateButton=new JButton("Zufallszahlen generieren",Images.MODELEDITOR_ELEMENT_SOURCE_MODE_DISTRIBUTION.getIcon());
		line.add(generateButton);
		generateButton.addActionListener(e->generateDataStream());
		line.add(Box.createHorizontalStrut(5));
		line.add(dataStreamValueCount=new JLabel());

		/* Batch */

		tabs.add(Language.tr("Surface.Source.Dialog.Tab.BatchSize"),tab=new JPanel(new BorderLayout(0,5)));
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.ClientsPerArrival")+":</b></html>"));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionFixedSize=new JRadioButton(Language.tr("Surface.Source.Dialog.BatchSize.Fixed")+":"));
		optionFixedSize.setEnabled(!readOnly);
		optionFixedSize.addActionListener(e->{updateBatchInfo(); checkData(false);});
		sub.add(batchField=new JTextField(15));
		ModelElementBaseDialog.addUndoFeature(batchField);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,batchField,false,false,model,surface));
		batchField.setEditable(!readOnly);
		batchField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionFixedSize.setSelected(true); updateBatchInfo();checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionFixedSize.setSelected(true); updateBatchInfo();checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionFixedSize.setSelected(true); updateBatchInfo();checkData(false);}
		});

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionSizesDistribution=new JRadioButton(Language.tr("Surface.Source.Dialog.BatchSize.Distribution")));
		optionSizesDistribution.setEnabled(!readOnly);
		optionSizesDistribution.addActionListener(e->{updateBatchInfo(); checkData(false);});
		sub.add(batchButton=new JButton(Language.tr("Surface.Source.Dialog.BatchSize.Distribution.Edit")));
		batchButton.setIcon(Images.MODE_DISTRIBUTION.getIcon());
		batchButton.addActionListener(e->editBatchSizesDistibution());

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionFixedSize);
		buttonGroup.add(optionSizesDistribution);

		batchButton.setEnabled(!readOnly || optionSizesDistribution.isSelected());

		tab.add(batchInfo=new JLabel(),BorderLayout.CENTER);
		batchInfo.setVerticalAlignment(SwingConstants.TOP);
		batchInfo.setVerticalTextPosition(SwingConstants.TOP);
		batchInfo.setPreferredSize(new Dimension(100,100));
		updateBatchInfo();

		/* Anzahl an Ankünften */

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals"),tab);
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.ArrivalCount")+":</b></html>"));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionInfinite=new JRadioButton(Language.tr("Surface.Source.Dialog.ArrivalCount.Unlimited")));
		optionInfinite.setEnabled(!readOnly);
		optionInfinite.addActionListener(e->checkData(false));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionFixedNumberArrivals=new JRadioButton(Language.tr("Surface.Source.Dialog.ArrivalCount.Number")+":"));
		optionFixedNumberArrivals.setEnabled(!readOnly);
		optionFixedNumberArrivals.addActionListener(e->checkData(false));
		sub.add(numberFieldArrivals=new JTextField(7));
		ModelElementBaseDialog.addUndoFeature(numberFieldArrivals);
		numberFieldArrivals.setEnabled(!readOnly);
		numberFieldArrivals.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (!readOnly) optionFixedNumberArrivals.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (!readOnly) optionFixedNumberArrivals.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (!readOnly) optionFixedNumberArrivals.setSelected(true); checkData(false);}
		});

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionFixedNumberClients=new JRadioButton(Language.tr("Surface.Source.Dialog.ClientCount.Number")+":"));
		optionFixedNumberClients.setEnabled(!readOnly);
		optionFixedNumberClients.addActionListener(e->checkData(false));
		sub.add(numberFieldClients=new JTextField(7));
		ModelElementBaseDialog.addUndoFeature(numberFieldClients);
		numberFieldClients.setEnabled(!readOnly);
		numberFieldClients.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (!readOnly) optionFixedNumberClients.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (!readOnly) optionFixedNumberClients.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (!readOnly) optionFixedNumberClients.setSelected(true); checkData(false);}
		});

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JTextPane pane=new JTextPane();
		sub.add(pane);
		pane.setOpaque(false);
		pane.setEditable(false);
		pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		pane.setPreferredSize(new Dimension(650,150));
		pane.setText(Language.tr("Surface.Source.Dialog.CountInfo"));

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionInfinite);
		buttonGroup.add(optionFixedNumberArrivals);
		buttonGroup.add(optionFixedNumberClients);

		/* Startzeitpunkt */

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.Tab.StartingTime"),tab);
		tab.add(arrivalStartSub=new JPanel(),BorderLayout.NORTH);
		arrivalStartSub.setLayout(new BoxLayout(arrivalStartSub,BoxLayout.PAGE_AXIS));

		arrivalStartSub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(arrivalStartTimeUnitGlobal=new JRadioButton(Language.tr("Surface.Source.Dialog.Tab.StartingTime.UnitGlobal")));
		arrivalStartTimeUnitGlobal.addActionListener(e->checkData(false));
		arrivalStartTimeUnitGlobal.setEnabled(!readOnly);

		arrivalStartSub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(arrivalStartTimeUnitLocal=new JRadioButton(Language.tr("Surface.Source.Dialog.Tab.StartingTime.UnitLocal")+":"));
		arrivalStartTimeUnitLocal.setEnabled(!readOnly);
		arrivalStartTimeUnitLocal.addActionListener(e->checkData(false));
		line.add(Box.createHorizontalStrut(5));
		line.add(arrivalStartTimeUnit=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		arrivalStartTimeUnit.setEnabled(!readOnly);
		arrivalStartTimeUnit.setSelectedIndex(0);
		arrivalStartTimeUnit.addActionListener(e->{arrivalStartTimeUnitLocal.setSelected(true); checkData(false);});

		buttonGroup=new ButtonGroup();
		buttonGroup.add(arrivalStartTimeUnitGlobal);
		buttonGroup.add(arrivalStartTimeUnitLocal);

		arrivalStartSub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.ArrivalStart")+":</b></html>"));

		arrivalStartSub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Source.Dialog.ArrivalStart.Label")+":"));
		line.add(arrivalStart=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(arrivalStart);
		label.setLabelFor(arrivalStart);
		arrivalStart.setEnabled(!readOnly);
		arrivalStart.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(arrivalStartTimeUnitLabel=new JLabel((String)timeBase1.getSelectedItem()));

		final int cardIndex=selectCard.getSelectedIndex();
		line.setVisible(cardIndex==0 || cardIndex==1 || cardIndex==4);
		line.setVisible(cardIndex==0 || cardIndex==1 || cardIndex==4);

		/* Zusätzliche Bedingung */

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.AdditionalCondition"),tab);
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.AdditionalCondition.Label")+":","");
		panel.add(line=(JPanel)data[0]);
		additionalArrivalCondition=(JTextField)data[1];
		additionalArrivalCondition.setEditable(!readOnly);
		additionalArrivalCondition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],true,false,model,surface),BorderLayout.EAST);
		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body>"+Language.tr("Surface.Source.Dialog.AdditionalCondition.Info")+"</body></html>"));

		/* Zuweisungen (Zahlen) */

		tabs.add(Language.tr("Surface.Source.Dialog.Tab.SetNumbers"),panelNumbers=new JPanel(new BorderLayout(0,5)));

		/* Zuweisungen (Texte) */

		tabs.add(Language.tr("Surface.Source.Dialog.Tab.SetTexts"),panelText=new JPanel(new BorderLayout(0,5)));

		/* Icons */

		int index=0;
		if (hasOwnArrivals) tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_INTERARRIVAL.getIcon());
		tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_BATCH.getIcon());
		if (hasOwnArrivals) {
			tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_COUNT.getIcon());
			tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_START.getIcon());
			tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_ADDITIONAL_CONDITION.getIcon());
		}
		tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_NUMBERS.getIcon());
		tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_TEXTS.getIcon());
	}

	/**
	 * Stellt den angegebenen Rechenausdruck ein.
	 * @param expression	Rechenausdruck
	 */
	private void toExpression(final String expression) {
		selectCard.setSelectedIndex(1);
		for (var listener: selectCard.getActionListeners()) listener.actionPerformed(new ActionEvent(selectCard,ActionEvent.ACTION_PERFORMED,"comboBoxChanged"));
		this.expression.setText(expression);
		checkData(false);
	}

	/**
	 * Stellt sicher, dass alle "Erste Ankunft zum Zeitpunkt 0"-Checkboxen denselben Wert haben.
	 * @param e	Ereignis, über das diese Methode aufgerufen wurde
	 */
	private void syncFirstArrivalAt0CheckBoxes(final ActionEvent e) {
		if (!(e.getSource() instanceof JCheckBox)) return;
		final boolean selected=((JCheckBox)e.getSource()).isSelected();

		distributionFirstArrivalAt0.setSelected(selected);
		expressionFirstArrivalAt0.setSelected(selected);
		dataStreamFirstArrivalAt0.setSelected(selected);
	}

	/**
	 * Liste aller über {@link #buildSyncedTimeBaseComboBox(JPanel)} erzeugten Zeitbasis-Comboboxen
	 * @see #buildSyncedTimeBaseComboBox(JPanel)
	 */
	private final List<JComboBox<String>> syncedTimeBased=new ArrayList<>();

	/**
	 * Erzeugt eine beschriftete und synchronisierte Zeitbasis-Combobox
	 * @param parent	Übergeordnetes Element in das Beschriftung und Combobox eingefügt werden sollen
	 * @return	Neue Combobox
	 */
	private JComboBox<String> buildSyncedTimeBaseComboBox(final JPanel parent) {
		final JLabel label=new JLabel(Language.tr("Surface.Source.Dialog.TimeBase")+":");
		parent.add(label);
		final JComboBox<String> timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings());
		parent.add(timeBase);
		timeBase.setEnabled(!readOnly);
		label.setLabelFor(timeBase);
		timeBase.addActionListener(e->{
			if (!(e.getSource() instanceof JComboBox)) return;
			@SuppressWarnings("unchecked")
			final int index=((JComboBox<String>)e.getSource()).getSelectedIndex();
			syncedTimeBased.forEach(comboBox->comboBox.setSelectedIndex(index));
			updateTabTitle();
		});
		syncedTimeBased.add(timeBase);
		return timeBase;
	}

	/**
	 * Trägt die Daten eines Kundenquellen-Datensatz in die GUI-Element ein
	 * @param record	Datensatz, dessen Daten in die GUI-Element eingestellt werden sollen
	 * @param element	Element, dessen Zuweisungen bearbeitet werden sollen (für den ExpressionBuilder und um die Variablenliste zusammenzustellen)
	 */
	public void setData(final ModelElementSourceRecord record, final ModelElement element) {
		int time;
		int timeBase;

		lastRecord=record;
		lastName=record.getName();

		/* Name */
		if (record.hasName()) {
			namePanel.setVisible(true);
			nameEdit.setText(record.getName());
			ModelElementBaseDialog.setClientIcon(record.getName(),nameButton,model);
		}

		/* Aktivierungsstatus */
		if (hasActivation) {
			activeCheckBox.setSelected(record.isActive());
		}

		/* Combobox einstellen */
		switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION: selectCard.setSelectedIndex(0); break;
		case NEXT_EXPRESSION: selectCard.setSelectedIndex(1); break;
		case NEXT_SCHEDULE: selectCard.setSelectedIndex(2); break;
		case NEXT_CONDITION: selectCard.setSelectedIndex(3); break;
		case NEXT_THRESHOLD: selectCard.setSelectedIndex(4); break;
		case NEXT_SIGNAL: selectCard.setSelectedIndex(5); break;
		case NEXT_INTERVAL_EXPRESSIONS: selectCard.setSelectedIndex(6); break;
		case NEXT_INTERVAL_DISTRIBUTIONS: selectCard.setSelectedIndex(7); break;
		case NEXT_STREAM: selectCard.setSelectedIndex(8); break;
		}

		/* Verteilung */
		timeBase1.setSelectedIndex(record.getTimeBase().id);
		distributionPanel.setDistribution(record.getInterarrivalTimeDistribution());
		distributionFirstArrivalAt0.setSelected(record.isFirstArrivalAt0());

		/* Ausdruck */
		timeBase2.setSelectedIndex(record.getTimeBase().id);
		expression.setText(record.getInterarrivalTimeExpression());
		expressionFirstArrivalAt0.setSelected(record.isFirstArrivalAt0());

		/* Zeitplan */
		int index=-1;
		for (int i=0;i<scheduleNames.length;i++) if (scheduleNames[i].equals(record.getInterarrivalTimeSchedule())) {index=i; break;}
		if (index>=0) schedule.setSelectedIndex(index);

		/* Bedingung */
		condition.setText(record.getArrivalCondition());
		conditionMinDistance.setText(record.getArrivalConditionMinDistance());

		/* Schwellenwert */
		thresholdExpression.setText(record.getThresholdExpression());
		thresholdExpressionValue.setText(NumberTools.formatNumberMax(record.getThresholdValue()));
		if (record.isThresholdDirectionUp()) thresholdDirection.setSelectedIndex(0); else thresholdDirection.setSelectedIndex(1);

		/* Signale */
		signalsTableModel.setData(record.getArrivalSignalNames());

		/* Anzahlen pro Intervall */
		time=record.getIntervalExpressionsIntervalTime();
		timeBase=0;
		while (timeBase<2 && time>=60) {
			if (time%60!=0) break;
			time/=60;
			timeBase++;
		}
		intervalExpressionsIntervalTime.setValue(time);
		intervalExpressionsIntervalTimeTimeBase.setSelectedIndex(timeBase);
		intervalExpressions.setText(Strings.join(record.getIntervalExpressions(),'\n').trim());

		/* Zwischenankunftszeit pro Intervall */
		time=record.getIntervalDistributionsIntervalTime();
		timeBase=0;
		while (timeBase<2 && time>=60) {
			if (time%60!=0) break;
			time/=60;
			timeBase++;
		}
		intervalDistributionsIntervalTime.setValue(time);
		intervalDistributionsIntervalTimeTimeBase.setSelectedIndex(timeBase);
		intervalDistributions.setText(Strings.join(record.getIntervalDistributions(),'\n').trim());

		/* Zahlenwerte */
		timeBase3.setSelectedIndex(record.getTimeBase().id);
		dataStreamType.setSelectedIndex(record.isDataStreamIsInterArrival()?1:0);
		dataStreamRepeat.setSelected(record.isDataStreamRepeat());
		dataStreamRepeat.setEnabled(dataStreamType.getSelectedIndex()==1);
		dataStreamFirstArrivalAt0.setSelected(record.isFirstArrivalAt0());
		dataStream.setText(String.join("\n",record.getDataStream()));

		/* Passende Seite aktivieren */
		switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
			((CardLayout)cards.getLayout()).show(cards,"Seite1");
			break;
		case NEXT_EXPRESSION:
			((CardLayout)cards.getLayout()).show(cards,"Seite2");
			break;
		case NEXT_SCHEDULE:
			((CardLayout)cards.getLayout()).show(cards,"Seite3");
			break;
		case NEXT_CONDITION:
			((CardLayout)cards.getLayout()).show(cards,"Seite4");
			break;
		case NEXT_THRESHOLD:
			((CardLayout)cards.getLayout()).show(cards,"Seite5");
			break;
		case NEXT_SIGNAL:
			((CardLayout)cards.getLayout()).show(cards,"Seite6");
			break;
		case NEXT_INTERVAL_EXPRESSIONS:
			((CardLayout)cards.getLayout()).show(cards,"Seite7");
			break;
		case NEXT_INTERVAL_DISTRIBUTIONS:
			((CardLayout)cards.getLayout()).show(cards,"Seite8");
			break;
		case NEXT_STREAM:
			((CardLayout)cards.getLayout()).show(cards,"Seite9");
			break;
		}

		/* Batch */
		if (record.getBatchSize()!=null) {
			optionFixedSize.setSelected(true);
			batchField.setText(record.getBatchSize());
		} else {
			optionSizesDistribution.setSelected(true);
			batchField.setText("1");
		}
		batchRates=record.getMultiBatchSize();
		if (batchRates==null) batchRates=new double[]{1.0};
		updateBatchInfo();

		/* Ankünfte-Anzahl */
		optionInfinite.setSelected(true);
		numberFieldArrivals.setText("1000");
		numberFieldClients.setText("1000");
		if (record.getMaxArrivalCount()>0) {
			optionFixedNumberArrivals.setSelected(true);
			numberFieldArrivals.setText(""+record.getMaxArrivalCount());
		}
		if (record.getMaxArrivalClientCount()>0) {
			optionFixedNumberClients.setSelected(true);
			numberFieldClients.setText(""+record.getMaxArrivalClientCount());
		}

		/* Start der Ankünfte */
		arrivalStart.setText(NumberTools.formatNumber(record.getArrivalStart()));
		final TimeBase arrivalStartTimeBase=record.getArrivalStartTimeBase();
		if (arrivalStartTimeBase==null) {
			arrivalStartTimeUnitGlobal.setSelected(true);
		} else {
			arrivalStartTimeUnitLocal.setSelected(true);
			arrivalStartTimeUnit.setSelectedIndex(arrivalStartTimeBase.id);
		}

		Object[] data;

		/* Zusätzliche Bedingung */
		additionalArrivalCondition.setText(record.getAdditionalArrivalCondition());

		/* Zuweisungen (Zahlen) */
		data=VariablesTableModel.buildTable(record.getSetRecord(),element,readOnly,helpRunnable,true);
		panelNumbers.add((JScrollPane)data[0],BorderLayout.CENTER);
		modelNumbers=(VariablesTableModel)data[1];

		/* Zuweisungen (Texte) */
		data=VariablesTextsTableModel.buildTable(record.getStringRecord(),readOnly,helpRunnable);
		panelText.add((JScrollPane)data[0],BorderLayout.CENTER);
		modelText=(VariablesTextsTableModel)data[1];

		/* Start */
		checkData(false);
	}

	/**
	 * Öffnet den Dialog zum Bearbeiten einer Zeile in {@link #intervalExpressions}
	 * @see #intervalExpressions
	 */
	private void editIntervalExpressions() {
		final int caretOffset=intervalExpressions.getCaretPosition();
		final int lineNumber;
		try {
			lineNumber=intervalExpressions.getLineOfOffset(caretOffset);
		} catch (BadLocationException e) {
			return;
		}

		final String[] lines=intervalExpressions.getText().split("\\n");
		if (lineNumber<0 || lineNumber>=lines.length) return;

		final ExpressionBuilder dialog=new ExpressionBuilder(this,lines[lineNumber],false,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(model.surface),ExpressionBuilder.getStationNameIDs(model.surface),true,false,false,model.userFunctions);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		lines[lineNumber]=dialog.getExpression();

		intervalExpressions.setText(Strings.join(Arrays.asList(lines),'\n'));
	}

	/**
	 * Öffnet den Dialog zum Bearbeiten einer Zeile in {@link #intervalDistributions}
	 * @see #intervalDistributions
	 */
	private void editIntervalDistributions() {
		final int caretOffset=intervalDistributions.getCaretPosition();
		final int lineNumber;
		try {
			lineNumber=intervalDistributions.getLineOfOffset(caretOffset);
		} catch (BadLocationException e) {
			return;
		}

		final String[] lines=intervalDistributions.getText().split("\\n");
		if (lineNumber<0 || lineNumber>=lines.length) return;

		final ExpressionBuilder dialog=new ExpressionBuilder(this,lines[lineNumber],false,model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(model.surface),ExpressionBuilder.getStationNameIDs(model.surface),true,false,false,model.userFunctions);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		lines[lineNumber]=dialog.getExpression();

		intervalDistributions.setText(Strings.join(Arrays.asList(lines),'\n'));
	}

	/**
	 * Öffnet den Dialog zum Bearbeiten der Batch-Größen Verteilung.
	 * @see ModelElementSourceBatchDialog
	 * @see #batchButton
	 */
	private void editBatchSizesDistibution() {
		final ModelElementSourceBatchDialog dialog=new ModelElementSourceBatchDialog(this,"ModelElementSource",readOnly,batchRates);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			batchRates=dialog.getBatchRates();
			optionSizesDistribution.setSelected(true);
			updateBatchInfo();
		}
	}

	/**
	 * Aktualisiert {@link #batchInfo}, wenn sich die Daten zur Umrechnung
	 * der Batch-Zwischenankunftszeit auf die Einzelkunden-Zwischenankunftszeiten
	 * verändert haben.
	 * @see #batchInfo
	 */
	private void updateBatchInfo() {
		boolean scaleInterArrivalTimes=false;
		double factorInterArrivalTimes=1.0;
		if (hasOwnArrivals) {
			if (optionFixedSize.isSelected()) {
				final Double D=ExpressionCalc.isConstValue(batchField.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
				if (D!=null && Math.round(D)>=1) { /* Kann auch Rechenausdruck sein, dann können wir keinen a-priori Skalierungsfaktor angeben */
					scaleInterArrivalTimes=true;
					factorInterArrivalTimes=1.0/Math.round(D);
				}
			} else {
				if (batchRates!=null && batchRates.length>0) {
					double sum=0;
					for (double rate: batchRates) sum+=rate;
					if (sum>0) {
						scaleInterArrivalTimes=true;
						factorInterArrivalTimes=0;
						for (int i=0;i<batchRates.length;i++) factorInterArrivalTimes+=(i+1)*batchRates[i]/sum;
						factorInterArrivalTimes=1/factorInterArrivalTimes;
					}
				}
			}
		}

		final StringBuilder text=new StringBuilder();
		text.append("<html><body style=\"margin: 5px 10px;\">\n");
		text.append("<p>"+Language.tr("Surface.Source.Dialog.Tab.BatchSize.Info")+"</p>\n");
		if (scaleInterArrivalTimes && factorInterArrivalTimes!=1.0) {
			text.append("<p style=\"margin-top: 5px;\"><b>"+String.format(Language.tr("Surface.Source.Dialog.Tab.BatchSize.ScaleInfo1"),NumberTools.formatNumber(factorInterArrivalTimes,3))+"</b></p>\n");
			if (selectCard.getSelectedIndex()==0) {
				final AbstractRealDistribution distribution=distributionPanel.getDistribution();
				final double mean=DistributionTools.getMean(distribution);
				text.append("<p style=\"margin-top: 5px;\"><b>"+String.format(Language.tr("Surface.Source.Dialog.Tab.BatchSize.ScaleInfo2"),NumberTools.formatNumber(mean*factorInterArrivalTimes),timeBase1.getSelectedItem())+"</b></p>\n");
			}
		}
		text.append("</body></html>\n");

		batchInfo.setText(text.toString());
	}

	/**
	 * Passt die Beschriftungen auf den Registerreitern an,
	 * wenn sich die Einstellungen im Dialog verändert haben.
	 */
	private void updateTabTitle() {
		String info;
		Long L;

		/* Batch-Größe */
		info="";
		if (optionFixedSize.isSelected()) {
			final Double D=ExpressionCalc.isConstValue(batchField.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (D==null) info=Language.tr("Surface.Source.Dialog.Tab.BatchSize.CalcExpression"); else info=batchField.getText();
		} else {
			info=Language.tr("Surface.Source.Dialog.Tab.BatchSize.DifferentSizes");
		}
		tabs.setTitleAt(hasOwnArrivals?1:0,Language.tr("Surface.Source.Dialog.Tab.BatchSize")+": "+info);

		/* Anzahl an Ankünften */
		if (hasOwnArrivals) {
			if (optionInfinite.isSelected()) {
				info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Clients")+": "+Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Infinite");
			} else {
				numberFieldArrivals.setBackground(NumberTools.getTextFieldDefaultBackground());
				numberFieldClients.setBackground(NumberTools.getTextFieldDefaultBackground());
				if (optionFixedNumberArrivals.isSelected()) {
					L=NumberTools.getPositiveLong(numberFieldArrivals,true);
					if (L==null) {
						info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Invalide");
					} else {
						info=NumberTools.formatLong(L.longValue());
					}
					info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Events")+": "+info;
				}
				if (optionFixedNumberClients.isSelected()) {
					L=NumberTools.getPositiveLong(numberFieldClients,true);
					if (L==null) {
						info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Clients.Invalide");
					} else {
						info=NumberTools.formatLong(L.longValue());
					}
					info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Clients")+": "+info;
				}
			}
			tabs.setTitleAt(2,info);
		}

		/* Startzeit */
		if (hasOwnArrivals) {
			if (arrivalStartSub.isVisible()) {
				final String unit;
				final String unitTab;
				if (arrivalStartTimeUnitGlobal.isSelected()) {
					unit=ModelSurface.getTimeBaseString(TimeBase.byId(timeBase1.getSelectedIndex()));
					unitTab=ModelSurface.getTimeBaseStringTab(TimeBase.byId(timeBase1.getSelectedIndex()));
				} else {
					unit=ModelSurface.getTimeBaseString(TimeBase.byId(arrivalStartTimeUnit.getSelectedIndex()));
					unitTab=ModelSurface.getTimeBaseStringTab(TimeBase.byId(arrivalStartTimeUnit.getSelectedIndex()));
				}
				arrivalStartTimeUnitLabel.setText(unit);
				final Double D=NumberTools.getNotNegativeDouble(arrivalStart,true);
				if (D==null) {
					info=Language.tr("Surface.Source.Dialog.Tab.StartingTime.Invalid");
				} else {
					if (D.doubleValue()==0.0) {
						info=Language.tr("Surface.Source.Dialog.Tab.StartingTime.Immediately");
					} else {
						info=String.format(Language.tr("Surface.Source.Dialog.Tab.StartingTime.AfterTime"),NumberTools.formatNumber(D.doubleValue())+" "+unitTab);
					}
				}
				tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+info);
			} else {
				tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime"));
			}
		}
	}

	/**
	 * Liefert einen String der eine Anzahl an Sekunden beschreibt.
	 * @param seconds	Anzahl an Sekunden
	 * @return	Zeichenkette, in der die Anzahl mit einer möglichst passenden Einheit beschrieben wird
	 * @see #checkData(boolean)
	 */
	private static String durationString(final long seconds) {
		if (seconds>=86400 && seconds%86400==0) return ""+(seconds/86400)+" "+Language.tr("Statistics.Days");
		if (seconds>=3600 && seconds%3600==0) return ""+(seconds/3600)+" "+Language.tr("Statistics.Hours");
		if (seconds>=60 && seconds%60==0) return ""+(seconds/60)+" "+Language.tr("Statistics.Minutes");
		return ""+Math.max(0,seconds)+" "+Language.tr("Statistics.Seconds");
	}

	/**
	 * Prüft, ob die aktuellen GUI-Daten gültig sind
	 * @param showErrorMessage	Gibt an, ob im Fehlerfall auch eine Meldung ausgegeben werden soll
	 * @return	Liefert <code>true</code>, wenn alle Daten in Ordnung sind
	 */
	public boolean checkData(final boolean showErrorMessage) {
		updateTabTitle();
		if (readOnly) return false;

		boolean ok=true;
		Long L;
		int error;
		String[] lines;
		boolean linesOk;

		if (namePanel.isVisible() && nameEdit.getText().trim().isEmpty()) {
			nameEdit.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Source.Dialog.ErrorName.Title"),Language.tr("Surface.Source.Dialog.ErrorName.Info"));
				return false;
			}
			ok=false;
		} else {
			nameEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		String[] intervals;
		int intervalCount;

		/* Infotext zu Anzahl an intervallabhängigen Ankünften aktualisieren */
		intervals=intervalExpressions.getText().trim().split("\\n");
		intervalCount=(intervals.length==1 && intervals[0].trim().isEmpty())?0:intervals.length;
		if (intervalCount==0) {
			intervalExpressionsInfo.setText(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.Info.Empty"));
		} else {
			final long intervalDuration=((Integer)intervalExpressionsIntervalTime.getValue()).intValue()*ModelSurface.TimeBase.byId(intervalExpressionsIntervalTimeTimeBase.getSelectedIndex()).multiply;
			if (intervalCount==1) {
				intervalExpressionsInfo.setText(String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.Info.One"),durationString(intervalDuration)));
			} else {
				intervalExpressionsInfo.setText(String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.Info.Multi"),intervalCount,durationString(intervalCount*intervalDuration)));
			}
		}

		/* Infotext zu Anzahl an intervallabhängigen Zwischenankunftszeiten aktualisieren */
		intervals=intervalDistributions.getText().trim().split("\\n");
		intervalCount=(intervals.length==1 && intervals[0].trim().isEmpty())?0:intervals.length;
		if (intervalCount==0) {
			intervalDistributionsInfo.setText(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.Info.Empty"));
		} else {
			final long intervalDuration=((Integer)intervalDistributionsIntervalTime.getValue()).intValue()*ModelSurface.TimeBase.byId(intervalDistributionsIntervalTimeTimeBase.getSelectedIndex()).multiply;
			if (intervalCount==1) {
				intervalDistributionsInfo.setText(String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.Info.One"),durationString(intervalDuration)));
			} else {
				intervalDistributionsInfo.setText(String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.Info.Multi"),intervalCount,durationString(intervalCount*intervalDuration)));
			}
		}

		switch (selectCard.getSelectedIndex()) {
		case 0: /* Verteilung */
			/* nichts zu prüfen */
			break;
		case 1: /* Ausdruck */
			error=ExpressionCalc.check(expression.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				expression.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.Expression.Error.Info"),expression.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				expression.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
			break;
		case 2: /* Zeitplan */
			if (schedule.getSelectedIndex()<0 && schedule.getItemCount()>0) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Schedule.Error.Title"),Language.tr("Surface.Source.Dialog.Schedule.Error.Info"));
					return false;
				}
				return false;
			}
			break;
		case 3: /* Bedingung */
			error=ExpressionMultiEval.check(condition.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.Condition.Error.Info"),condition.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
			error=ExpressionCalc.check(conditionMinDistance.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				conditionMinDistance.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ConditionMinDistance.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ConditionMinDistance.Error.Info"),conditionMinDistance.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				conditionMinDistance.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
			break;
		case 4: /* Schwellenwert */
			error=ExpressionCalc.check(thresholdExpression.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true),model.userFunctions);
			if (error>=0) {
				thresholdExpression.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Threshold.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.Threshold.Error.Info"),thresholdExpression.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				thresholdExpression.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
			if (NumberTools.getDouble(thresholdExpressionValue,true)==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ThresholdValue.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ThresholdValue.Error.Info"),thresholdExpressionValue.getText()));
					return false;
				}
				ok=false;
			}
			break;
		case 5: /* Signale */
			if (signalsTableModel.getData().size()==0) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Signals.Error.Title"),Language.tr("Surface.Source.Dialog.Signals.Error.Info"));
					return false;
				}
				ok=false;
			}
			break;
		case 6: /* Anzahlen pro Intervall */
			lines=intervalExpressions.getText().trim().split("\\n");
			linesOk=true;
			for (int i=0;i<lines.length;i++) {
				final String line=lines[i].trim();
				if (!line.isEmpty()) {
					error=ExpressionCalc.check(line,surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
					if (error>=0) {
						linesOk=false;
						ok=false;
						if (showErrorMessage) {
							MsgBox.error(this,Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalExpressions.Error.Info"),i+1,line,error+1));
							break;
						}
					}
				}
			}
			intervalExpressions.setBackground(linesOk?NumberTools.getTextFieldDefaultBackground():Color.RED);
			if (showErrorMessage && !linesOk) return false;
			break;
		case 7: /* Zwischenankunftszeiten pro Intervall */
			lines=intervalDistributions.getText().trim().split("\\n");
			linesOk=true;
			for (int i=0;i<lines.length;i++) {
				final String line=lines[i].trim();
				if (!line.isEmpty()) {
					error=ExpressionCalc.check(line,surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
					if (error>=0) {
						linesOk=false;
						ok=false;
						if (showErrorMessage) {
							MsgBox.error(this,Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.IntervalDistributions.Error.Info"),i+1,line,error+1));
							break;
						}
					}
				}
			}
			intervalDistributions.setBackground(linesOk?NumberTools.getTextFieldDefaultBackground():Color.RED);
			if (showErrorMessage && !linesOk) return false;
			break;
		case 8: /* Zahlenwerte */
			lines=dataStream.getText().trim().split("\\n");
			linesOk=true;
			int count=0;
			for (int i=0;i<lines.length;i++) {
				final String line=lines[i].trim();
				if (!line.isEmpty()) {
					count++;
					final Double D=NumberTools.getDouble(line);
					if (D==null) {
						linesOk=false;
						ok=false;
						if (showErrorMessage) {
							MsgBox.error(this,Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Values.ErrorTitle"),String.format(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Values.ErrorInfo"),i+1,line));
							break;
						}
					}
				}
			}
			if (ok) {
				dataStreamValueCount.setText(String.format(((count==1)?Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Values.InfoSingular"):Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.DataStream.Values.InfoPlural")),NumberTools.formatLong(count)));
				dataStreamValueCount.setVisible(true);
			} else {
				dataStreamValueCount.setVisible(false);
			}
			dataStream.setBackground(linesOk?NumberTools.getTextFieldDefaultBackground():Color.RED);
			if (showErrorMessage && !linesOk) return false;
			break;
		}

		/* Batch-Größe */
		if (optionFixedSize.isSelected()) {
			error=ExpressionCalc.check(batchField.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
			if (error>=0) {
				batchField.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.BatchSize.Error.Info"),batchField.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				batchField.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			batchField.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Anzahl an Ankünften */
		if (hasOwnArrivals) {
			L=NumberTools.getPositiveLong(numberFieldArrivals,true);
			if (L==null && optionFixedNumberArrivals.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ArrivalCount.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ArrivalCount.Error.Info"),numberFieldArrivals.getText()));
					return false;
				}
				ok=false;
			}
			L=NumberTools.getPositiveLong(numberFieldClients,true);
			if (L==null && optionFixedNumberClients.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientCount.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ClientCount.Error.Info"),numberFieldClients.getText()));
					return false;
				}
				ok=false;
			}

			final Double D=NumberTools.getNotNegativeDouble(arrivalStart,true);
			final String unit;
			final String unitTab;
			if (arrivalStartTimeUnitGlobal.isSelected()) {
				unit=ModelSurface.getTimeBaseString(TimeBase.byId(timeBase1.getSelectedIndex()));
				unitTab=ModelSurface.getTimeBaseStringTab(TimeBase.byId(timeBase1.getSelectedIndex()));
			} else {
				unit=ModelSurface.getTimeBaseString(TimeBase.byId(arrivalStartTimeUnit.getSelectedIndex()));
				unitTab=ModelSurface.getTimeBaseStringTab(TimeBase.byId(arrivalStartTimeUnit.getSelectedIndex()));
			}
			arrivalStartTimeUnitLabel.setText(unit);
			if (D==null) {
				tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+Language.tr("Surface.Source.Dialog.Tab.StartingTime.Invalid"));
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ArrivalStart.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ArrivalStart.Error.Info"),arrivalStart.getText()));
					return false;
				}
				ok=false;
			} else {
				if (D.doubleValue()==0.0) {
					tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+Language.tr("Surface.Source.Dialog.Tab.StartingTime.Immediately"));
				} else {
					tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+String.format(Language.tr("Surface.Source.Dialog.Tab.StartingTime.AfterTime"),NumberTools.formatNumber(D.doubleValue())+" "+unitTab));
				}
			}
		}

		/* Zusätzliche Bedingung */
		if (hasOwnArrivals) {
			if (additionalArrivalCondition.getText().isBlank()) {
				additionalArrivalCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				error=ExpressionMultiEval.check(additionalArrivalCondition.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false),model.userFunctions);
				if (error>=0) {
					additionalArrivalCondition.setBackground(Color.RED);
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("Surface.Source.Dialog.AdditionalCondition.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.AdditionalCondition.Error.Info"),additionalArrivalCondition.getText(),error+1));
						return false;
					}
					ok=false;
				} else {
					additionalArrivalCondition.setBackground(NumberTools.getTextFieldDefaultBackground());
				}
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Daten aus der GUI in ein Datensatz-Objekt zurück
	 * @param record	Datensatz-Objekt, in das die Daten eingetragen werden sollen
	 */
	public void getData(final ModelElementSourceRecord record) {
		/* Name */
		if (nameEdit.isVisible()) {
			record.setName(nameEdit.getText());
		}

		/* Aktiv */
		if (hasActivation) {
			record.setActive(activeCheckBox.isSelected());
		}

		Double D;
		int time;

		switch (selectCard.getSelectedIndex()) {
		case 0: /* Verteilung */
			record.setTimeBase(ModelSurface.TimeBase.byId(timeBase1.getSelectedIndex()));
			record.setInterarrivalTimeDistribution(distributionPanel.getDistribution());
			record.setFirstArrivalAt0(distributionFirstArrivalAt0.isSelected());
			break;
		case 1: /* Ausdruck */
			record.setTimeBase(ModelSurface.TimeBase.byId(timeBase2.getSelectedIndex()));
			record.setInterarrivalTimeExpression(expression.getText());
			record.setFirstArrivalAt0(expressionFirstArrivalAt0.isSelected());
			break;
		case 2: /* Zeitplan */
			String s="";
			if (schedule.getSelectedIndex()>=0 && schedule.getSelectedIndex()<scheduleNames.length) s=scheduleNames[schedule.getSelectedIndex()];
			record.setInterarrivalTimeSchedule(s);
			break;
		case 3: /* Bedingung */
			record.setArrivalCondition(condition.getText(),conditionMinDistance.getText());
			break;
		case 4: /* Schwellenwert */
			D=NumberTools.getDouble(thresholdExpressionValue,true);
			final double thresholdValue=(D!=null)?D.doubleValue():0.0;
			record.setThreshold(thresholdExpression.getText(),thresholdValue,thresholdDirection.getSelectedIndex()==0);
			break;
		case 5: /* Signale */
			record.getArrivalSignalNames().clear();
			record.getArrivalSignalNames().addAll(signalsTableModel.getData());
			record.setSignalMode();
			break;
		case 6: /* Anzahlen pro Intervall */
			time=(Integer)intervalExpressionsIntervalTime.getValue();
			time*=ModelSurface.TimeBase.byId(intervalExpressionsIntervalTimeTimeBase.getSelectedIndex()).multiply;
			record.setIntervalExpressionsIntervalTime(time);
			record.getIntervalExpressions().clear();
			record.getIntervalExpressions().addAll(Arrays.asList(intervalExpressions.getText().trim().split("\\n")));
			break;
		case 7: /* Zwischenankunftszeiten pro Intervall */
			time=(Integer)intervalDistributionsIntervalTime.getValue();
			time*=ModelSurface.TimeBase.byId(intervalDistributionsIntervalTimeTimeBase.getSelectedIndex()).multiply;
			record.setIntervalDistributionsIntervalTime(time);
			record.getIntervalDistributions().clear();
			record.getIntervalDistributions().addAll(Arrays.asList(intervalDistributions.getText().trim().split("\\n")));
			break;
		case 8: /* Zahlenwerte */
			record.setTimeBase(ModelSurface.TimeBase.byId(timeBase3.getSelectedIndex()));
			record.setDataStreamIsInterArrival(dataStreamType.getSelectedIndex()==1);
			record.setDataStreamRepeat((dataStreamType.getSelectedIndex()==1) && dataStreamRepeat.isSelected());
			record.setFirstArrivalAt0(dataStreamFirstArrivalAt0.isSelected());
			record.setDataStream(dataStream.getText().trim());
			break;
		}

		if (optionFixedSize.isSelected()) {
			record.setBatchSize(batchField.getText().trim());
		} else {
			record.setMultiBatchSize(batchRates);
		}

		record.setMaxArrivalCount(-1);
		record.setMaxArrivalClientCount(-1);
		if (optionFixedNumberArrivals.isSelected()) {
			final Long L=NumberTools.getPositiveLong(numberFieldArrivals,true);
			if (L!=null) record.setMaxArrivalCount(L);
		}
		if (optionFixedNumberClients.isSelected()) {
			final Long L=NumberTools.getPositiveLong(numberFieldClients,true);
			if (L!=null) record.setMaxArrivalClientCount(L);
		}

		if (selectCard.getSelectedIndex()!=2 && selectCard.getSelectedIndex()!=3 && selectCard.getSelectedIndex()!=4) {
			record.setArrivalStart(NumberTools.getNotNegativeDouble(arrivalStart,true));
			if (arrivalStartTimeUnitGlobal.isSelected()) {
				record.setArrivalStartTimeBase(null);
			} else {
				record.setArrivalStartTimeBase(TimeBase.byId(arrivalStartTimeUnit.getSelectedIndex()));
			}
		}

		/* Zusätzliche Bedingung */
		record.setAdditionalArrivalCondition(additionalArrivalCondition.getText().trim());

		/* Zuweisungen (Zahlen) */
		modelNumbers.storeData();

		/* Zuweisungen (Texte) */
		modelText.storeData();
	}

	/**
	 * Schreibt die Daten in das Datensatz-Objekt, aus dem sie ausgelesen wurden, zurück
	 * @param updateSystem	Stellt ein, ob Namensänderungen ins System übertragen werden sollen,
	 * @param clientData	Datenelement, welches die Kundentypenliste vorhält
	 */
	public void getData(final boolean updateSystem, final ModelClientData clientData) {
		if (lastRecord==null) return;
		getData(lastRecord);
		if (!surface.getClientTypes().contains(lastName)) {
			if (lastRecord.hasName() && updateSystem) renameClients(lastName,lastRecord.getName(),clientData,surface);
		}
	}

	/**
	 * Prüft, ob sich der Name einer Kundengruppe geändert hat und überträgt die Änderungen ggf. in die anderen Surface-Elemente.<br>
	 * Besitzt das Datensatz-Element einen eigenen Namen und wird <code>getData()</code> (ohne Parameter) verwendet, so wird
	 * diese Methode automatisch aufgerufen. Dient jedoch der Name des Elementes als Kundengruppenname, so muss diese
	 * Methode manuell aufgerufen werden.
	 * @param oldName	Alter Name
	 * @param newName	Neuer Name
	 * @param clientData	Kundendatenobjekt aus dem Modell, in dem der Kundendatensatz ggf. umbenannt werden soll
	 * @param surface	Zeichenfläche (muss bei Umbenennung benachrichtigt werden). Es kann sich auch um eine Sub-Zeichenfläche handeln, die Information wird dann zur Hauptzeichenfläche weitergereicht.
	 */
	public static void renameClients(final String oldName, final String newName, final ModelClientData clientData, final ModelSurface surface) {
		if (oldName!=null && newName!=null && !oldName.trim().isEmpty() && !newName.trim().isEmpty() && !oldName.equals(newName)) {
			if (clientData!=null) clientData.copyDataIfNotExistent(oldName,newName);
			surface.objectRenamed(oldName,newName,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE,true);
		}
	}

	/**
	 * Ruft den Dialog zum Bearbeiten der Kundentypeigenschaften auf.
	 * @see #nameButton
	 */
	private void editClientData() {
		final String name=(lastName.isEmpty())?nameEdit.getText().trim():lastName;
		if (name.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Title"),Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Info"));
			return;
		}

		if (ModelPropertiesDialogPageClients.editClientData(this,helpRunnable,model,name,readOnly)) ModelElementBaseDialog.setClientIcon(name,nameButton,model);
	}

	/**
	 * Zeigt einen Dialog zur Generierung von Werten für {@link #dataStream} an.
	 * @see #dataStream
	 */
	private void generateDataStream() {
		final ModelElementSourceRecordPanelGenerateDialog dialog=new ModelElementSourceRecordPanelGenerateDialog(this,model);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			dataStream.setText(dialog.getNewValues());
			checkData(false);
		}
	}

	/**
	 * Liefert den 0-basierten Index des in dem Panel aktuell aktiven Tabs.
	 * @return	0-basierter Index des in dem Panel aktuell aktiven Tabs
	 */
	public int getActiveTabIndex() {
		return tabs.getSelectedIndex();
	}

	/**
	 * Stellt den 0-basierten Index des in dem Panel aktuell aktiven Tabs ein.
	 * @param index	0-basierter Index des in dem Panel aktuell aktiven Tabs
	 */
	public void setActiveTabIndex(final int index) {
		if (index>=0 && index<tabs.getTabCount()) tabs.setSelectedIndex(index);
	}
}
