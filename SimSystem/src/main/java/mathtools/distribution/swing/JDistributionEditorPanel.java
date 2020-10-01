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
import java.awt.AWTPermission;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse kapselt einen Verteilungseditor als Panel.
 * @author Alexander Herzog
 * @version 2.0
 * @see JDistributionEditorDialog
 */
public class JDistributionEditorPanel extends JPanel {
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
	private JComboBox<String> distributionType;

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
	private final List<JDistributionEditorPanelRecord> records=JDistributionEditorPanelRecord.getList();

	/**
	 * Konstruktor der Klasse <code>DistributionEditorPanel</code>
	 * @param distribution	Darzustellende Verteilung
	 * @param maxXValue	Maximal darzustellender x-Wert
	 * @param dataChangedNotify	Optionaler {@link ActionListener}, der ausgelöst werden soll, wenn der Benutzer die Verteilung ändert.
	 * @param allowDistributionTypeChange	Gibt an, ob der Typ der Verteilung geändert werden darf.
	 */
	public JDistributionEditorPanel(AbstractRealDistribution distribution, double maxXValue, ActionListener dataChangedNotify, boolean allowDistributionTypeChange) {
		this.distribution=distribution;
		this.maxXValue=maxXValue;
		this.dataChangedNotify=dataChangedNotify;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		add(distributionType=new JComboBox<>(),BorderLayout.NORTH);
		for (JDistributionEditorPanelRecord record: records) distributionType.addItem(record.getName());
		distributionType.addItemListener(e->itemStateChanged());
		DistributionComboBoxRenderer renderer=new DistributionComboBoxRenderer();
		renderer.setPreferredSize(new Dimension(50,27));
		distributionType.setRenderer(renderer);

		add(editPanel=new JPanel(new CardLayout()));

		JButton[] buttons;

		if (isFullAccess()) {
			buttonValueCopy=new JButton(ButtonCopyData);
			buttonValueCopy.addActionListener(new ButtonListener(this));
			buttonValueCopy.setIcon(SimSystemsSwingImages.COPY.getIcon());

			buttonValuePaste=new JButton(ButtonPasteData);
			buttonValuePaste.addActionListener(new ButtonListener(this));
			buttonValuePaste.setIcon(SimSystemsSwingImages.PASTE.getIcon());

			buttonValuePasteNoScale=new JButton(ButtonPasteAndFillData);
			buttonValuePasteNoScale.setToolTipText(ButtonPasteAndFillDataTooltip);
			buttonValuePasteNoScale.addActionListener(new ButtonListener(this));
			buttonValuePasteNoScale.setIcon(SimSystemsSwingImages.PASTE.getIcon());

			buttonValueLoad=new JButton(ButtonLoadData);
			buttonValueLoad.addActionListener(new ButtonListener(this));
			buttonValueLoad.setIcon(SimSystemsSwingImages.LOAD.getIcon());

			buttonValueSave=new JButton(ButtonSaveData);
			buttonValueSave.addActionListener(new ButtonListener(this));
			buttonValueSave.setIcon(SimSystemsSwingImages.SAVE.getIcon());

			buttons=new JButton[]{buttonValueCopy,buttonValuePaste,buttonValuePasteNoScale,buttonValueLoad,buttonValueSave};
		} else {
			buttonValueCopy=null;
			buttonValuePaste=null;
			buttonValuePasteNoScale=null;
			buttonValueLoad=null;
			buttonValueSave=null;
			buttons=null;
		}

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

	private boolean isFullAccess() {
		SecurityManager security=System.getSecurityManager();
		if (security!=null) try {
			security.checkPermission(new AWTPermission("accessClipboard"));	/* ist gleichwertig zu "SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION", das aber aus dem privaten Paket sun.* ist */
			security.checkPropertiesAccess();
		} catch (SecurityException e) {return false;}
		return true;
	}

	/**
	 * Auslesen der momentanen Verteilung
	 * @return	Aktuelles Verteilungsobjekt
	 */
	public AbstractRealDistribution getDistribution() {return distribution;}

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

	private void setDataFromDistribution() {
		int index=-1;
		final String name=DistributionTools.getDistributionName(distribution);
		for (int i=0;i<records.size();i++) if (records.get(i).getName().equals(name)) {index=i; break;}

		if (index<0) return;

		final JTextField[] currentFields=distributionFields[index];
		final String[] text=records.get(index).getValues(distribution);
		for (int i=0;i<text.length;i++) currentFields[i].setText(text[i]);

		distributionType.setSelectedIndex(index);
		((CardLayout)editPanel.getLayout()).show(editPanel,records.get(index).getName());

		if (dataChangedNotify!=null) dataChangedNotify.actionPerformed(new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,""));
	}

	private void makeDistributionFromData() {
		final int index=distributionType.getSelectedIndex();

		final JTextField[] currentFields=distributionFields[index];

		final AbstractRealDistribution newDistribution=records.get(index).getDistribution(currentFields,maxXValue);
		if (newDistribution==null) return;
		distribution=newDistribution;

		if (dataChangedNotify!=null) dataChangedNotify.actionPerformed(new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,""));
	}

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

	private void itemStateChanged() {
		if (distributionType.getSelectedIndex()==lastIndex) return;
		lastIndex=distributionType.getSelectedIndex();
		final double mean=NumberTools.reduceDigits(DistributionTools.getMean(distribution),10);
		final double sd=NumberTools.reduceDigits(DistributionTools.getStandardDeviation(distribution),10);

		((CardLayout)editPanel.getLayout()).show(editPanel,(String)distributionType.getSelectedItem());

		records.get(lastIndex).setValues(distributionFields[lastIndex],mean,sd);

		makeDistributionFromData();
	}

	private class TextFieldsEvents implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {makeDistributionFromData();}
		@Override
		public void keyPressed(KeyEvent e) {makeDistributionFromData();}
		@Override
		public void keyReleased(KeyEvent e) {makeDistributionFromData();}
	}

	private double[] getDataFromClipboard() {
		Transferable cont=getToolkit().getSystemClipboard().getContents(this);
		if (cont==null) return null;
		String s=null;
		try {s=(String)cont.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {return null;}
		if (s==null) return null;
		return JDataLoader.loadNumbersFromString(JDistributionEditorPanel.this,s,1,Integer.MAX_VALUE);
	}

	private class ButtonListener implements ActionListener {
		private final JPanel parent;

		public ButtonListener(JPanel parent) {this.parent=parent;}

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
				File file=Table.showSaveDialog(parent,ButtonSaveDataDialogTitle); if (file==null) return;
				Table table=new Table(s);
				table.save(file);
				return;
			}
		}
	}

	private final class ShiftButtonListener implements ActionListener {
		private final int nr;
		private final JTextField field;

		public ShiftButtonListener(int nr, JTextField field) {
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
	private final class DistributionComboBoxRenderer extends JLabel implements ListCellRenderer<String> {
		private static final long serialVersionUID = 6456312299286699520L;

		/**
		 * Konstruktor der Klasse
		 */
		public DistributionComboBoxRenderer() {
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected && index>=0) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				setOpaque(true);
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				setOpaque(false);
			}

			ImageIcon image=DistributionTools.getThumbnailImageForDistributionName(value);
			setIcon(image);
			setText(((image==null)?" ":"")+value);

			return this;
		}
	}
}