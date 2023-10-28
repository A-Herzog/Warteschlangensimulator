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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse kapselt einen Verteilungseditor als Panel.
 * @author Alexander Herzog
 * @version 2.1
 * @see JDistributionEditorDialog
 */
public class JDistributionEditorPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2950211874092327021L;

	/** Dialogtitel für den {@link JDistributionEditorDialog} */
	public static String DialogTitle="Verteilungseditor";

	/** Bezeichner für Dialogschaltfläche "Ok" */
	public static String ButtonOk="Ok";

	/** Bezeichner für Dialogschaltfläche "Abbrechen" */
	public static String ButtonCancel="Abbrechen";

	/** Bezeichner für Dialogschaltfläche "Kopieren" */
	public static String ButtonCopyData="Kopieren";

	/** Bezeichner für Dialogschaltfläche "Einfügen" */
	public static String ButtonPasteData="Einfügen";

	/** Bezeichner für Dialogschaltfläche "Einfügen &amp; auffüllen" */
	public static String ButtonPasteAndFillData="Einfügen (und auffüllen)";

	/** Tooltip für Dialogschaltfläche "Einfügen &amp; auffüllen" */
	public static String ButtonPasteAndFillDataTooltip="Normalerweise werden die Daten beim Einfügen skaliert. Es können aber auch Nullen angefügt werden.";

	/** Bezeichner für Dialogschaltfläche "Laden" */
	public static String ButtonLoadData="Laden";

	/** Titel für Dialog zum Laden von Verteilungsdaten */
	public static String ButtonLoadDataDialogTitle="Verteilung laden";

	/** Bezeichner für Dialogschaltfläche "Speichern" */
	public static String ButtonSaveData="Speichern";

	/** Titel für Dialog zum Speichern von Verteilungsdaten */
	public static String ButtonSaveDataDialogTitle="Verteilung speichern";

	/** Bezeichner "Zähldichtenvektor" */
	public static String DistData="Zähldichtenvektor";

	/** Bezeichner "Erwartungswer" */
	public static String DistMean="Erwartungswert";

	/** Bezeichner "Standardabweichung" */
	public static String DistStdDev="Standardabweichung";

	/** Bezeichner "Star" (für Gleichverteilung) */
	public static String DistUniformStart="Start";

	/** Bezeichner "Ende" (für Gleichverteilung) */
	public static String DistUniformEnd="Ende";

	/** Bezeichner "Freiheitsgrade" */
	public static String DistDegreesOfFreedom="Freiheitsgrade";

	/** Bezeichner "Freiheitsgrade (Zähler)" */
	public static String DistDegreesOfFreedomNumerator="Freiheitsgrade (Zähler)";

	/** Bezeichner "Freiheitsgrade (Nenner)" */
	public static String DistDegreesOfFreedomDenominator="Freiheitsgrade (Nenner)";

	/** Bezeichner "Verringert den Wert des Parameters um %s" (Wert verringern Schaltfläche) */
	public static String ChangeValueDown="Verringert den Wert des Parameters um %s";

	/** Bezeichner "Erhöht den Wert des Parameters um %s" (Wert vergrößern Schaltfläche) */
	public static String ChangeValueUp="Erhöht den Wert des Parameters um %s";

	/** Bezeichner "Am wahrscheinlichsten" (für Dreiecksverteilung) */
	public static String DistMostLikely="Am wahrscheinlichsten";

	/** Dialogtitel "Liste der hervorgehobene Verteilungen bearbeiten" */
	public static String SetupListTitle="Liste der hervorgehobene Verteilungen bearbeiten";

	/** Infozeile oben im Dialog zum Bearbeiten der hervorgehobenen Verteilungen */
	public static String SetupListInfo="<html><body>Die jeweils ausgewählte Verteilung kann per Strg+Hoch<br>und Strg+Runter verschoben werden.</body></html>";

	/** Infozeile unten im Dialog zum Bearbeiten der hervorgehobenen Verteilungen (eine Verteilung hervorgehoben) */
	public static String SetupListInfoSingular="%d von %d Verteilungen ist hervorgehoben.";

	/** Infozeile unten im Dialog zum Bearbeiten der hervorgehobenen Verteilungen (mehrere Verteilungen hervorgehoben) */
	public static String SetupListInfoPlural="%d von %d Verteilungen sind hervorgehoben.";

	/** Trenner zwischen den hervorgehobenen und den normalen Verteilungen */
	public static String SetupListDivier="<html><body>Oben: hervorgehobene Verteilungen (angegebene Reihenfolge)<br>Unten: Normale Verteilungen (werden alphabetisch sortiert)</body></html>";

	/**
	 * Aktuelle Verteilung
	 */
	private AbstractRealDistribution distribution;

	/**
	 * Maximal darzustellender x-Wert
	 */
	private final double maxXValue;

	/**
	 * Optionaler externer {@link ActionListener}, der ausgelöst werden soll, wenn der Benutzer die Verteilung ändert
	 */
	private final ActionListener dataChangedNotify;

	/**
	 * Eingabefelder für die Verteilungsparameter (auf allen möglichen Cards)
	 */
	private final JTextField[][] distributionFields;

	/** Auswahl des Verteilungstyp */
	private JComboBox<JDistributionEditorPanelRecord> distributionType;

	/** Panel zum Bearbeiten der jeweiligenVerteilungsparameter */
	private JPanel editPanel;

	/** Zuletzt aktives Panel<br>
	 * Um auch zu Beginn, wenn noch kein Panel, also -1, aktiv ist, als "geändert" angesehen zu werden, wird hier -2 als Startwert verwendet.
	 * @see #itemStateChanged()
	 */
	private int lastIndex=-2;

	/** "Kopieren"-Schaltfläche */
	private final JButton buttonValueCopy;

	/** "Einfügen"-Schaltfläche */
	private final JButton buttonValuePaste;

	/** "Einfügen (und auffüllen)"-Schaltfläche */
	private final JButton buttonValuePasteNoScale;

	/** "Laden"-Schaltfläche */
	private final JButton buttonValueLoad;

	/** "Speichern"-Schaltfläche */
	private final JButton buttonValueSave;

	/**
	 * Liste mit den Verteilungsdaten für die verschiedenen Panels
	 * @see #editPanel
	 * @see #distributionType
	 */
	private List<JDistributionEditorPanelRecord> records;

	/**
	 * Konstruktor der Klasse <code>DistributionEditorPanel</code>
	 * @param distribution	Darzustellende Verteilung
	 * @param maxXValue	Maximal darzustellender x-Wert
	 * @param dataChangedNotify	Optionaler {@link ActionListener}, der ausgelöst werden soll, wenn der Benutzer die Verteilung ändert.
	 * @param allowDistributionTypeChange	Gibt an, ob der Typ der Verteilung geändert werden darf.
	 */
	public JDistributionEditorPanel(final AbstractRealDistribution distribution, final double maxXValue, final ActionListener dataChangedNotify, final boolean allowDistributionTypeChange) {
		this.distribution=distribution;
		this.maxXValue=maxXValue;
		this.dataChangedNotify=dataChangedNotify;

		if (filterGetter==null || filterGetter.get()==null) {
			records=JDistributionEditorPanelRecord.getList(null,false,false);
		} else {
			records=JDistributionEditorPanelRecord.getList(Arrays.asList(filterGetter.get().trim().split("\\n")),true,false);
		}

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		final JPanel line=new JPanel(new BorderLayout());
		add(line,BorderLayout.NORTH);
		line.add(distributionType=new JComboBox<>(),BorderLayout.CENTER);
		for (JDistributionEditorPanelRecord record: records) distributionType.addItem(record);
		distributionType.addItemListener(e->itemStateChanged());
		final DistributionComboBoxRenderer renderer=new DistributionComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(50,27));
		distributionType.setRenderer(renderer);
		if (filterGetter!=null && filterSetter!=null && allowDistributionTypeChange) {
			final JButton editFilterButton=new JButton(SimSystemsSwingImages.SETUP.getIcon());
			editFilterButton.setToolTipText(SetupListTitle);
			editFilterButton.addActionListener(e->editFilterList());
			final Dimension size=editFilterButton.getPreferredSize();
			editFilterButton.setPreferredSize(new Dimension(size.height,size.height));
			line.add(editFilterButton,BorderLayout.EAST);
		}

		add(editPanel=new JPanel(new CardLayout()));

		JButton[] buttons;

		buttonValueCopy=new JButton(ButtonCopyData);
		buttonValueCopy.addActionListener(new ButtonListener());
		buttonValueCopy.setIcon(SimSystemsSwingImages.COPY.getIcon());

		buttonValuePaste=new JButton(ButtonPasteData);
		buttonValuePaste.addActionListener(new ButtonListener());
		buttonValuePaste.setIcon(SimSystemsSwingImages.PASTE.getIcon());

		buttonValuePasteNoScale=new JButton(ButtonPasteAndFillData);
		buttonValuePasteNoScale.setToolTipText(ButtonPasteAndFillDataTooltip);
		buttonValuePasteNoScale.addActionListener(new ButtonListener());
		buttonValuePasteNoScale.setIcon(SimSystemsSwingImages.PASTE.getIcon());

		buttonValueLoad=new JButton(ButtonLoadData);
		buttonValueLoad.addActionListener(new ButtonListener());
		buttonValueLoad.setIcon(SimSystemsSwingImages.LOAD.getIcon());

		buttonValueSave=new JButton(ButtonSaveData);
		buttonValueSave.addActionListener(new ButtonListener());
		buttonValueSave.setIcon(SimSystemsSwingImages.SAVE.getIcon());

		buttons=new JButton[]{buttonValueCopy,buttonValuePaste,buttonValuePasteNoScale,buttonValueLoad,buttonValueSave};

		distributionFields=new JTextField[records.size()][];

		double meanD=DistributionTools.getMean(distribution); if (meanD<=0) meanD=maxXValue/2;
		final String mean=NumberTools.formatNumberMax(meanD);
		double stdD=DistributionTools.getStandardDeviation(distribution); if (stdD<=0) stdD=maxXValue/10;
		final String std=NumberTools.formatNumberMax(stdD);
		final String lower=NumberTools.formatNumberMax(maxXValue/4);
		final String upper=NumberTools.formatNumberMax(3*maxXValue/4);

		for (int i=0;i<distributionFields.length;i++) {
			final JDistributionEditorPanelRecord record=records.get(i);
			if (record.isDataDistribution()) {
				distributionFields[i]=addCardPanel(record.getName(),record.getEditLabels(),record.getEditValues(meanD,mean,stdD,std,lower,upper,maxXValue),buttons,false);
			} else {
				distributionFields[i]=addCardPanel(record.getName(),record.getEditLabels(),record.getEditValues(meanD,mean,stdD,std,lower,upper,maxXValue),null,true);
			}
		}

		setDataFromDistribution();

		distributionType.setEnabled(allowDistributionTypeChange);
	}

	/**
	 * Callback zum Abruf der Liste der hervorzuheben darzustellenden Verteilungen<br>
	 * (Ist das Callback <code>null</code>, so erfolgt keine Hervorhebung.)
	 * @see #registerFilterGetter(Supplier)
	 */
	private static Supplier<String> filterGetter=null;

	/**
	 * Callback zum Speichern der Liste der hervorzuheben darzustellenden Verteilungen<br>
	 * (Ist das Callback <code>null</code>, so wird der Editor nicht angeboten.)
	 * @see #registerFilterSetter(Consumer)
	 */
	private static Consumer<String> filterSetter=null;

	/**
	 * Stellt das Callback zum Abruf der Liste der hervorzuheben darzustellenden Verteilungen ein.
	 * @param filterGetter	Callback zum Abruf der Liste der hervorzuheben darzustellenden Verteilungen
	 * @see #filterGetter
	 */
	public static void registerFilterGetter(final Supplier<String> filterGetter) {
		JDistributionEditorPanel.filterGetter=filterGetter;
	}

	/**
	 * Stellt das Callback zum Speichern der Liste der hervorzuheben darzustellenden Verteilungen ein.
	 * @param filterSetter	Callback zum Speichern der Liste der hervorzuheben darzustellenden Verteilungen
	 * @see #filterSetter
	 */
	public static void registerFilterSetter(final Consumer<String> filterSetter) {
		JDistributionEditorPanel.filterSetter=filterSetter;
	}

	/**
	 * Auslesen der momentanen Verteilung
	 * @return	Aktuelles Verteilungsobjekt
	 * @see #setDistribution(AbstractRealDistribution)
	 */
	public AbstractRealDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Einstellen einer neuen Verteilung
	 * @param distribution	Neue Verteilung
	 * @see #getDistribution()
	 */
	public void setDistribution(final AbstractRealDistribution distribution) {
		if (distribution==null) return;
		this.distribution=distribution;

		setDataFromDistribution();
	}

	/**
	 * Erstellt ein Panel mit Eingabefeldern
	 * @param name	Name unter dem das Panel im {@link CardLayout} registriert werden soll
	 * @param fields	Beschriftungen der Eingabefelder
	 * @param initialValues	Anfängliche Werte der Eingabefelder
	 * @param buttons	Optionale zusätzliche Schaltflächen, die angezeigt werden sollen (z.B. Lade/Speicher-Schaltflächen für empirische Verteilungen; kann im Normalfall <code>null</code> sein)
	 * @param spinButtons	Erhöhen/Verringern-Schaltflächen anzeigen (für empirische Verteilungen deaktivieren; sonst aktivieren)
	 * @return	Array mit den erzeugten Eingabefeldern
	 */
	private JTextField[] addCardPanel(String name, String[] fields, String[] initialValues, JButton[] buttons, boolean spinButtons) {
		final JTextField[] textFields=new JTextField[fields.length];
		final JButton[] shiftButtons=new JButton[4];
		final JPanel panel=new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

		for (int i=0;i<fields.length;i++) {
			final JLabel l=new JLabel(fields[i]);

			GridBagConstraints gbc;

			gbc=new GridBagConstraints();
			gbc.gridx=0; gbc.gridy=i;
			gbc.weightx=1; gbc.weighty=(i==fields.length-1)?1:0;
			gbc.anchor=GridBagConstraints.NORTHWEST;
			gbc.fill=GridBagConstraints.HORIZONTAL;

			((GridBagLayout)panel.getLayout()).setConstraints(l,gbc);
			panel.add(l);

			if (spinButtons) {
				shiftButtons[0]=new JButton(SimSystemsSwingImages.NUMBER_DOWN_10.getIcon());
				shiftButtons[0].setToolTipText(String.format(ChangeValueDown,"10"));
				shiftButtons[1]=new JButton(SimSystemsSwingImages.NUMBER_DOWN_1.getIcon());
				shiftButtons[1].setToolTipText(String.format(ChangeValueDown,"1"));
				shiftButtons[2]=new JButton(SimSystemsSwingImages.NUMBER_UP_1.getIcon());
				shiftButtons[2].setToolTipText(String.format(ChangeValueUp,"1"));
				shiftButtons[3]=new JButton(SimSystemsSwingImages.NUMBER_UP_10.getIcon());
				shiftButtons[3].setToolTipText(String.format(ChangeValueUp,"10"));
				for (int j=0;j<2;j++) {
					gbc=new GridBagConstraints();
					gbc.gridx=j+1; gbc.gridy=i;
					gbc.weightx=0; gbc.weighty=(i==fields.length-1)?1:0;
					gbc.anchor=GridBagConstraints.NORTHWEST;
					gbc.fill=GridBagConstraints.HORIZONTAL;
					final JToolBar toolbar=new JToolBar();
					toolbar.setFloatable(false);
					toolbar.add(shiftButtons[j]);
					((GridBagLayout)panel.getLayout()).setConstraints(toolbar,gbc);
					panel.add(toolbar);
				}
			}

			textFields[i]=new JTextField(initialValues[i]);
			addUndoFeature(textFields[i]);
			textFields[i].addKeyListener(new TextFieldsEvents());

			if (spinButtons) {
				for (int j=0;j<4;j++) shiftButtons[j].addActionListener(new ShiftButtonListener(j,textFields[i]));
			}

			gbc=new GridBagConstraints();
			gbc.gridx=spinButtons?3:1; gbc.gridy=i;
			gbc.weightx=0; gbc.weighty=(i==fields.length-1)?1:0;
			gbc.anchor=GridBagConstraints.NORTHWEST;
			gbc.fill=GridBagConstraints.HORIZONTAL;
			gbc.ipadx=200;

			((GridBagLayout)panel.getLayout()).setConstraints(textFields[i],gbc);
			panel.add(textFields[i]);

			if (spinButtons) {
				for (int j=2;j<4;j++) {
					gbc=new GridBagConstraints();
					gbc.gridx=j+2; gbc.gridy=i;
					gbc.weightx=0; gbc.weighty=(i==fields.length-1)?1:0;
					gbc.anchor=GridBagConstraints.NORTHWEST;
					gbc.fill=GridBagConstraints.HORIZONTAL;
					final JToolBar toolbar=new JToolBar();
					toolbar.setFloatable(false);
					toolbar.add(shiftButtons[j]);
					((GridBagLayout)panel.getLayout()).setConstraints(toolbar,gbc);
					panel.add(toolbar);
				}
			}
		}

		if (buttons!=null) {
			JPanel buttonsPanel=new JPanel();
			buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
			final GridBagConstraints gbc=new GridBagConstraints();
			gbc.gridx=1; gbc.gridy=fields.length;
			gbc.weightx=1; gbc.weighty=3;
			gbc.anchor=GridBagConstraints.NORTHWEST;
			gbc.fill=GridBagConstraints.HORIZONTAL;
			((GridBagLayout)panel.getLayout()).setConstraints(buttonsPanel,gbc);
			panel.add(buttonsPanel);
			for (int i=0;i<buttons.length;i++) buttonsPanel.add(buttons[i]);
		}

		editPanel.add(panel,name);

		return textFields;
	}

	/**
	 * Stellt die Werte in den Eingabefeldern gemäß der Verteilung ein.
	 */
	private void setDataFromDistribution() {
		int index=-1;
		final String name=DistributionTools.getDistributionName(distribution);
		for (int i=0;i<records.size();i++) if (records.get(i).getName().equals(name)) {index=i; break;}

		if (index<0) return;

		final JTextField[] currentFields=distributionFields[index];
		final String[] text=records.get(index).getValues(distribution);
		for (int i=0;i<text.length;i++) currentFields[i].setText(text[i]);

		listUpdating=true;
		try {
			distributionType.setSelectedIndex(index);
			((CardLayout)editPanel.getLayout()).show(editPanel,records.get(index).getName());
			lastIndex=index;
		} finally {
			listUpdating=false;
		}

		if (dataChangedNotify!=null) dataChangedNotify.actionPerformed(new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,""));
	}

	/**
	 * Stellt die Verteilung gemäß den Werten in den Eingabefeldern ein.
	 */
	private void makeDistributionFromData() {
		final int index=distributionType.getSelectedIndex();

		final JTextField[] currentFields=distributionFields[index];

		final AbstractRealDistribution newDistribution=records.get(index).getDistribution(currentFields,maxXValue);
		if (newDistribution==null) return;
		distribution=newDistribution;

		if (dataChangedNotify!=null) dataChangedNotify.actionPerformed(new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,""));
	}

	/**
	 * Liefert die Verteilungsdaten in einem Textformat,
	 * welches für den Export (per Zwischenablage oder
	 * per Datei) geeignet ist.
	 * @return	Verteilungsdaten als Text
	 */
	private String getDataDistributionExportFormat() {
		DataDistributionImpl d=DataDistributionImpl.createFromString(distributionFields[0][0].getText(),(int)maxXValue);
		if (d==null || d.densityData.length==0) return null;
		String s=NumberTools.formatNumberMax(d.densityData[0]);
		for (int i=1;i<d.densityData.length;i++) s+="\n"+NumberTools.formatNumberMax(d.densityData[i]);
		return s;
	}

	/**
	 * Lädt die Verteilungsdaten für eine empirische Daten Verteilung aus einer Datei.
	 * @param file	Zu ladende Datei
	 * @return	Gibt an, ob die Datei verarbeitet werden konnte
	 */
	public boolean loadFromFile(File file) {
		double[] newData=JDataLoader.loadNumbersFromFile(JDistributionEditorPanel.this,file,1,Integer.MAX_VALUE);
		if (newData==null) return false;
		distribution=new DataDistributionImpl(maxXValue,newData);
		setDataFromDistribution();
		return true;
	}

	/**
	 * Erzeugt eine Wertetabelle für die Verteilung und kopiert diese in die Zwischenablage.
	 */
	public void copyTableOfValues() {
		if (distribution==null) return;
		DistributionTools.copyTableOfValues(distribution);
	}

	/**
	 * Erzeugt und speichert eine Wertetabelle für die Verteilung.
	 */
	public void saveTableOfValues() {
		if (distribution==null) return;
		DistributionTools.saveTableOfValues(this,distribution);
	}

	/**
	 * Wird die Liste gerade extern aktualisiert?
	 * (Dann soll {@link #itemStateChanged()} nicht ausgeführt werden.)
	 * @see #itemStateChanged()
	 */
	private boolean listUpdating=false;

	/**
	 * Listener der aktiviert wird, wenn in {@link #distributionType} ein
	 * neuer Verteilungstyp ausgewählt wird.
	 * @see #listUpdating
	 * @see #editFilterList()
	 */
	private void itemStateChanged() {
		if (listUpdating) return;
		if (distributionType.getSelectedIndex()==lastIndex) return;
		lastIndex=distributionType.getSelectedIndex();
		double mean=DistributionTools.getMean(distribution);
		if (Double.isNaN(mean) || Double.isInfinite(mean) || mean<0 || mean>10E10) mean=10;
		mean=NumberTools.reduceDigits(mean,10);
		double sd=DistributionTools.getStandardDeviation(distribution);
		if (Double.isNaN(sd) || Double.isInfinite(sd) || sd<0 || sd>10E10) sd=1;
		sd=NumberTools.reduceDigits(sd,10);

		((CardLayout)editPanel.getLayout()).show(editPanel,((JDistributionEditorPanelRecord)distributionType.getSelectedItem()).getName());

		records.get(lastIndex).setValues(distributionFields[lastIndex],mean,sd);

		makeDistributionFromData();
	}

	/**
	 * Reagiert auf Tastendrücken in den Eingabefeldern
	 * und aktualisiert die Verteilung.
	 * @see JDistributionEditorPanel#addCardPanel(String, String[], String[], JButton[], boolean)
	 * @see JDistributionEditorPanel#makeDistributionFromData()
	 */
	private class TextFieldsEvents implements KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public TextFieldsEvents() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void keyTyped(KeyEvent e) {makeDistributionFromData();}
		@Override
		public void keyPressed(KeyEvent e) {makeDistributionFromData();}
		@Override
		public void keyReleased(KeyEvent e) {makeDistributionFromData();}
	}

	/**
	 * Versucht eine Datenzeile aus der Zwischenablage zu laden
	 * @return	Liefert im Erfolgsfall die Datenzeile, sonst <code>null</code>
	 */
	private double[] getDataFromClipboard() {
		Transferable cont=getToolkit().getSystemClipboard().getContents(this);
		if (cont==null) return null;
		String s=null;
		try {s=(String)cont.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {return null;}
		if (s==null) return null;
		return JDataLoader.loadNumbersFromString(JDistributionEditorPanel.this,s,1,Integer.MAX_VALUE);
	}

	/**
	 * Ruft den Dialog zum Bearbeiten der Liste der hervorzuhebenden Verteilungen auf.
	 */
	private void editFilterList() {
		final JDistributionEditorPanelRecordDialog dialog=new JDistributionEditorPanelRecordDialog(SwingUtilities.getWindowAncestor(this),filterGetter.get());
		dialog.setVisible(true);
		final String filter=dialog.getFilter();
		if (filter!=null) {
			filterSetter.accept(filter);
			final String selectedRecordName=((JDistributionEditorPanelRecord)distributionType.getSelectedItem()).getName();
			listUpdating=true;
			try {
				distributionType.removeAllItems();
				records=JDistributionEditorPanelRecord.getList(Arrays.asList(filterGetter.get().trim().split("\\n")),true,false);
				for (JDistributionEditorPanelRecord record: records) distributionType.addItem(record);
				lastIndex=0;
				for (int i=0;i<records.size();i++) if (records.get(i).getName().equals(selectedRecordName)) {lastIndex=i; break;}
				distributionType.setSelectedIndex(lastIndex);
			} finally {
				listUpdating=false;
			}
		}
	}

	/**
	 * Liefert die Namen der hervorgehoben darzustellenden Verteilungen.
	 * @return	Namen der hervorgehoben darzustellenden Verteilungen
	 */
	public static String[] getHighlightedDistributions() {
		if (filterGetter==null) return new String[0];
		return filterGetter.get().split("\\n");
	}

	/**
	 * Listener, der auf Klicks auf die Schaltflächen reagiert
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==buttonValueCopy) {
				String s=getDataDistributionExportFormat();
				if (s==null) return;
				getToolkit().getSystemClipboard().setContents(new StringSelection(s),null);
				return;
			}
			if (e.getSource()==buttonValuePaste) {
				double[] newData=getDataFromClipboard();
				if (newData==null) return;
				distribution=new DataDistributionImpl(maxXValue,newData);
				setDataFromDistribution();
				return;
			}
			if (e.getSource()==buttonValuePasteNoScale) {
				double[] newData=getDataFromClipboard();
				if (newData==null) return;
				List<Double> newDataList=new ArrayList<>();
				for (int i=0;i<newData.length;i++) newDataList.add(newData[i]);
				while (newDataList.size()<maxXValue+1) newDataList.add(0.0);
				distribution=new DataDistributionImpl(maxXValue,newDataList);
				setDataFromDistribution();
				return;
			}
			if (e.getSource()==buttonValueLoad) {
				double[] newData=JDataLoader.loadNumbers(JDistributionEditorPanel.this,ButtonLoadDataDialogTitle,1,Integer.MAX_VALUE);
				if (newData==null) return;
				distribution=new DataDistributionImpl(maxXValue,newData);
				setDataFromDistribution();
				return;
			}
			if (e.getSource()==buttonValueSave) {
				String s=getDataDistributionExportFormat();
				if (s==null) return;
				File file=Table.showSaveDialog(JDistributionEditorPanel.this,ButtonSaveDataDialogTitle); if (file==null) return;
				Table table=new Table(s);
				table.save(file);
				return;
			}
		}
	}

	/**
	 * Listener für die Schaltflächen zum Verändern der Werte
	 * in einem Zahleneingabefeld nach oben oder unten
	 */
	private final class ShiftButtonListener implements ActionListener {
		/** Art des Buttons (0..3) */
		private final int nr;
		/** Zugehöriges Eingabefeld */
		private final JTextField field;

		/**
		 * Konstruktor der Klasse
		 * @param nr	Art des Buttons (0..3)
		 * @param field	Zugehöriges Eingabefeld
		 */
		public ShiftButtonListener(final int nr, final JTextField field) {
			this.nr=nr;
			this.field=field;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final int BIG=10;
			final int SMALL=1;
			Double d=NumberTools.getDouble(field,false);
			if (d==null) return;
			switch (nr) {
			case 0: d-=BIG; break;
			case 1: d-=SMALL; break;
			case 2: d+=SMALL; break;
			case 3: d+=BIG; break;
			}
			field.setText(NumberTools.formatNumberMax(d));
			makeDistributionFromData();
		}
	}

	/**
	 * ComboBox-Renderer zur Anzeige von Verteilungs-Symbolen in der Liste
	 * @see JDistributionEditorPanel#distributionType
	 */
	public static final class DistributionComboBoxRenderer extends JLabel implements ListCellRenderer<JDistributionEditorPanelRecord> {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 6456312299286699520L;

		/**
		 * Konstruktor der Klasse
		 */
		public DistributionComboBoxRenderer() {
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JDistributionEditorPanelRecord> list, JDistributionEditorPanelRecord value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected && index>=0) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				setOpaque(true);
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				setOpaque(false);
			}

			if (value.isSeparator()) {
				setIcon(SimSystemsSwingImages.LIST_DIVIDER.getIcon());
				setText(value.getName());
				setFont(getFont().deriveFont(Font.ITALIC+Font.BOLD));
			} else {
				final ImageIcon image=DistributionTools.getThumbnailImageForDistributionName(value.getName());
				setIcon(image);
				setText(((image==null)?" ":"")+value.getName());
				setFont(getFont().deriveFont(value.highlight?Font.BOLD:Font.PLAIN));
			}
			return this;
		}
	}

	/**
	 * Aktiviert die Undo/Redo-Funktionen für ein Textfeld
	 * @param textField Textfeld, bei dem die Funktionen aktiviert werden sollen
	 */
	private static void addUndoFeature(final JTextField textField) {
		final UndoManager manager=new UndoManager();
		textField.getDocument().addUndoableEditListener(manager);

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown()) {
					try {
						manager.undo();
					} catch (CannotUndoException e2) {
					}
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_Y && e.isControlDown()) {
					try {
						manager.redo();
					} catch (CannotRedoException e2) {
					}
					e.consume();
					return;
				}
			}
		});
	}
}