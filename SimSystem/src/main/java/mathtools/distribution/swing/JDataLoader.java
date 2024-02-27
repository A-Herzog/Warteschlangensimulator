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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import mathtools.MultiTable;
import mathtools.NumberTools;
import mathtools.Table;

/**
 * Dialog, der das Laden von Zahlenreihen oder auch Textreihen aus beliebigen Tabellendateien ermöglicht
 * (Mit Hilfe der statischen Funktionen kann auch eine automatisch Auswahl erfolgen: Gibt es eine
 * eindeutige, passende Reihe in der Datei, so wird diese sofort zurückgeliefert. Andernfalls wird der
 * Auswahldialog angezeigt.)
 * @author Alexander Herzog
 * @version 1.1
 */
public class JDataLoader extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2109504488623985873L;

	/** Titel für den "Daten importieren"-Dialog */
	public static String Title="Daten importieren";

	/** Bezeichner "Tabellenblatt" */
	public static String Sheet="Tabellenblatt";

	/** Anweisung "Bitte wählen Sie den zu importierenden Bereich." */
	public static String SelectArea="Bitte wählen Sie den zu importierenden Bereich.";

	/** Dialogschaltfläche "Ok" */
	public static String ButtonOk="Ok";

	/** Dialogschaltfläche "Abbrechen" */
	public static String ButtonCancel="Abbrechen";

	/** Titel für Fehlermeldungen */
	public static String ImportErrorTitle="Fehler";

	/** Fehler "Es wurde kein zu importierender Bereich gewählt." */
	public static String ImportErrorNoArea="Es wurde kein zu importierender Bereich gewählt.";

	/** Fehler "Es wurden %s Zellen ausgewählt. Es müssen jedoch mindestens %s Zellen importiert werden." */
	public static String ImportErrorTooFewCells="Es wurden %s Zellen ausgewählt. Es müssen jedoch mindestens %s Zellen importiert werden.";

	/** Fehler "Es wurden %s Zellen ausgewählt. Es dürfen jedoch höchstens %s Zellen importiert werden." */
	public static String ImportErrorTooManyCells="Es wurden %s Zellen ausgewählt. Es dürfen jedoch höchstens %s Zellen importiert werden.";

	/** Fehler "Der %s. selektierte Wert %s ist keine gültige Zahl." */
	public static String ImportErrorInvalidValue="Der %s. selektierte Wert %s ist keine gültige Zahl.";

	/** Fehler "Die Daten konnten nicht aufbereitet werden." */
	public static String ImportErrorInvalidData="Die Daten konnten nicht aufbereitet werden.";

	/** Fehler "Die Datei %s konnte nicht geladen werden." */
	public static String ImportErrorFileError="Die Datei %s konnte nicht geladen werden.";

	/** Übergeordnetes Fenster */
	private final Window owner;

	/** Tabellendatenblatt vom Typ {@link MultiTable} aus dem die Daten entnommen werden sollen. */
	private final MultiTable data;

	/** Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden. */
	private final int minValues;

	/** Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden. */
	private final int maxValues;

	/** Gibt an, ob Zahlen (<code>true</code>) oder beliebige Daten (<code>false</code>) verlangt werden. */
	private final boolean numbersOnly;

	/** Tabs für die Tabellenblätter */
	private final JTabbedPane tabs;

	/** Anzeige des selektierten (=zu ladenden) Bereichs */
	private final JTextField edit;

	/** Einzelne Tabellen der Arbeitsmappe */
	private final JTable[] table;

	/** Datenmodelle der Tabellendaten */
	private final TableModel[] model;

	/** "Ok"-Schaltfläche */
	private final JButton buttonOk;

	/** "Abbrechen"-Schaltfläche */
	private final JButton buttonCancel;

	/** Tabellenblatt, in dem zuletzt ein Bereich ausgewählt wurde */
	private int lastSelectedTable=-1;

	/**
	 * Liefert im Erfolgfalls die angeforderten Daten zurück, sonst bleibt hier <code>null</code> stehen.
	 */
	public String[] resultData=null;

	/**
	 * Liefert im Erfolgfalls die angeforderten Zahlen zurück, sonst bleibt hier <code>null</code> stehen.
	 */
	public double[] resultNumbers=null;

	/**
	 * Konstruktor der Klasse <code>JDataLoader</code> zur Anzeige eines Daten-Auswahldialogs
	 * @param owner	Übergeordnetes Fenster
	 * @param data	Tabellendatenblatt vom Typ {@link MultiTable} aus dem die Daten entnommen werden sollen.
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param numbersOnly	Gibt an, ob Zahlen (<code>true</code>) oder beliebige Daten (<code>false</code>) verlangt werden.
	 */
	public JDataLoader(final Window owner, final MultiTable data, final int minValues, final int maxValues, final boolean numbersOnly) {
		super(owner,Title,Dialog.ModalityType.DOCUMENT_MODAL);

		this.owner=owner;
		this.data=data;
		this.minValues=minValues;
		this.maxValues=maxValues;
		this.numbersOnly=numbersOnly;

		JPanel p;
		getContentPane().setLayout(new BorderLayout());

		/* Top */
		getContentPane().add(p=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		p.add(edit=new JTextField(SelectArea),BorderLayout.CENTER);
		edit.setEditable(false);

		/* Content */
		table=new JTable[data.size()];
		model=new TableModel[data.size()];
		getContentPane().add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		for (int i=0;i<data.size();i++) {
			model[i]=new ImportTableModel(data.get(i));
			String s=data.getName(i);
			if (s==null || s.isBlank()) s=Sheet;
			tabs.add(new JScrollPane(table[i]=new JTable(model[i])),s);
			table[i].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table[i].getTableHeader().setReorderingAllowed(false);
			table[i].setColumnSelectionAllowed(true);
			table[i].getSelectionModel().addListSelectionListener(new ImportTableSelectionListener(i));
		}

		/* Bottom */
		getContentPane().add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		p.add(buttonOk=new JButton(ButtonOk));
		buttonOk.setIcon(SimSystemsSwingImages.OK.getIcon());
		getRootPane().setDefaultButton(buttonOk);
		buttonOk.addActionListener(new ButtonListener());
		p.add(buttonCancel=new JButton(ButtonCancel));
		buttonCancel.setIcon(SimSystemsSwingImages.CANCEL.getIcon());
		buttonCancel.addActionListener(new ButtonListener());

		addWindowListener(new WindowAdapter() {@Override public void windowClosing(final WindowEvent event) {setVisible(false); dispose();}});
		Rectangle area=getGraphicsConfiguration().getBounds();
		setSize(Math.min(area.width-50,750),Math.min(area.height-50,650));
		setLocationRelativeTo(owner);
	}

	/**
	 * Konstruktor der Klasse <code>JDataLoader</code> zur Anzeige eines Daten-Auswahldialogs
	 * @param owner	Übergeordnetes Fenster
	 * @param data	Tabellendatenblatt vom Typ <code>MultiTable</code> aus dem die Daten entnommen werden sollen.
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param numbersOnly	Gibt an, ob Zahlen (<code>true</code>) oder beliebige Daten (<code>false</code>) verlangt werden.
	 */
	public JDataLoader(final Window owner, final MultiTable data, final int countValues, final boolean numbersOnly) {
		this(owner,data,countValues,countValues,numbersOnly);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new AbstractAction(){
			private static final long serialVersionUID = 1283354112070324457L;
			@Override public void actionPerformed(ActionEvent e) {setVisible(false); dispose();}
		});

		return rootPane;
	}

	/**
	 * Berechnet die minimale und die maximale Zeile oder Spalte aus einem Array mit Zeilen oder Spalten
	 * @param cells	Array aus Zeilen- oder Spaltennummern
	 * @return	Minimum und Maximum des selektierten Bereichs
	 */
	private int[] getRange(final int[] cells) {
		int a=Integer.MAX_VALUE;
		for (int i=0;i<cells.length;i++) if (cells[i]<a) a=cells[i];

		int b=a;
		boolean ok=true;
		while (ok) {
			ok=false;
			for (int i=0;i<cells.length;i++) if (cells[i]==b+1) {ok=true; b++; break;}
		}

		return new int[]{a,b};
	}

	/**
	 * Liefert den gewählten Bereich in einer Tabelle
	 * @param nr	Nummer der Tabelle (0-basierend) in der Arbeitsmappe
	 * @return	Selektierter Bereich (Array 1: Startpunkt, Array 2: Endpunkt; jeweils Zeile+Spalte) oder <code>null</code>, wenn keine Selektion erfolgt ist
	 */
	private int[][] getSelectedRange(final int nr) {
		if (nr<0) return null;

		int[] cols=table[nr].getSelectedColumns();
		int[] rows=table[nr].getSelectedRows();

		if (cols.length==0 || rows.length==0) return null;

		if (cols.length==1) {
			/* Zeilenbereich */
			int[] r=getRange(rows);
			return new int[][]{new int[]{r[0],cols[0]},new int[]{r[1],cols[0]}};
		} else {
			if (rows.length>1) return null;
			/* Spaltenbereich */
			int[] c=getRange(cols);
			return new int[][]{new int[]{rows[0],c[0]},new int[]{rows[0],c[1]}};
		}
	}

	/**
	 * Liefert bei einer einzeiligen oder einspaltigen Selektion die Anzahl der selektierten Zellen
	 * @param range	Selektierter Bereich (muss aus einer Zeile oder einer Spalte bestehen)
	 * @return	Anzahl der selektierten Zellen
	 */
	private int getCellCount(final int[][] range) {
		int count=0;

		if (range[0][0]==range[1][0]) {
			/* Zeilen identisch */
			count=Math.abs(range[0][1]-range[1][1])+1;
		} else {
			/* Spalten identisch */
			count=Math.abs(range[0][0]-range[1][0])+1;
		}

		return count;
	}

	/**
	 * Liefert die Inhalte der selektierten Zellen (Selektion muss eine Zeile oder eine Spalte sein)
	 * @param nr	Tabellenblatt (0-basierend)
	 * @param range	Selektrierter Bereich
	 * @return	Inhalte der Zellen
	 */
	private String[] getSelectedCells(final int nr, final int[][] range) {
		Table t=data.get(nr);

		if (range[0][0]==range[1][0]) {
			/* Zeilen identisch */
			int fix=range[0][0];
			int a=Math.min(range[0][1],range[1][1]);
			int b=Math.max(range[0][1],range[1][1]);
			String[] result=new String[b-a+1];
			for (int i=a;i<=b;i++) result[i-a]=t.getValue(fix,i);
			return result;
		} else {
			/* Spalten identisch */
			int fix=range[0][1];
			int a=Math.min(range[0][0],range[1][0]);
			int b=Math.max(range[0][0],range[1][0]);
			String[] result=new String[b-a+1];
			for (int i=a;i<=b;i++) result[i-a]=t.getValue(i,fix);
			return result;
		}
	}

	/**
	 * Liefert den zur Vorgabe passenden selektrierten Bereich
	 * @return	Selektierter Bereich (Array 1: Startpunkt, Array 2: Endpunkt; jeweils Zeile+Spalte) oder <code>null</code>, wenn keine passende Selektion erfolgt ist
	 */
	private int[][] getValidSelectedRange() {
		/* Wurde etwas ausgewählt? */
		int[][] range=getSelectedRange(lastSelectedTable);
		if (range==null) {
			JOptionPane.showMessageDialog(owner,ImportErrorNoArea,ImportErrorTitle,JOptionPane.ERROR_MESSAGE);
			return null;
		}

		/* Wurde die richtige Menge an Zellen gewählt? */
		int count=getCellCount(range);
		if (count<minValues) {
			JOptionPane.showMessageDialog(owner,String.format(ImportErrorTooFewCells,""+count,""+minValues),ImportErrorTitle,JOptionPane.ERROR_MESSAGE);
			return null;
		}

		if (count>maxValues) {
			JOptionPane.showMessageDialog(owner,String.format(ImportErrorTooManyCells,""+count,""+maxValues),ImportErrorTitle,JOptionPane.ERROR_MESSAGE);
			return null;
		}

		if (!numbersOnly) return range;
		/* Prüfen ob überall korrekte Zahlen hinterlegt sind */
		String[] textData=getSelectedCells(lastSelectedTable,range);
		for (int i=0;i<textData.length;i++) {
			Double D=NumberTools.getExtProbability(textData[i]);
			if (D==null) {
				JOptionPane.showMessageDialog(owner,String.format(ImportErrorInvalidValue,""+(i+1),textData[i]),ImportErrorTitle,JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}

		return range;
	}

	/**
	 * Reagiert auf das Anklicken von Schaltflächen
	 * @see #buttonOk
	 * @see #buttonCancel
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
		public void actionPerformed(final ActionEvent e) {

			if (e.getSource()==buttonOk) {
				/* Wurde etwas ausgewählt? */
				int[][] range=getValidSelectedRange();
				if (range==null) return;

				if (numbersOnly) {
					String[] textData=getSelectedCells(lastSelectedTable,range);
					resultNumbers=new double[textData.length];
					for (int i=0;i<textData.length;i++) resultNumbers[i]=NumberTools.getExtProbability(textData[i]);
				} else {
					resultData=getSelectedCells(lastSelectedTable,range);
				}
				setVisible(false); dispose();
				return;
			}

			if (e.getSource()==buttonCancel) {
				setVisible(false); dispose();
				return;
			}
		}
	}

	/**
	 * Tabellenmodell für die Vorschautabelle für die zu ladenden Daten
	 * @see #model
	 */
	private static class ImportTableModel extends AbstractTableModel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 4210448324814080272L;

		/** Anzuzeigende Tabelle */
		private final Table table;

		/**
		 * Konstruktor der Klasse
		 * @param table	Anzuzeigende Tabelle
		 */
		public ImportTableModel(final Table table) {
			this.table=table;
		}

		@Override
		public int getRowCount() {
			return table.getSize(0);
		}

		@Override
		public int getColumnCount() {
			return table.getSize(1);
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return table.getValue(rowIndex,columnIndex);
		}
	}

	/**
	 * Listener der auf die Auswahl von Tabellenzellen reagiert
	 * @see #table
	 */
	private class ImportTableSelectionListener implements ListSelectionListener {
		/** Tabellenblatt (0-basierend) für den dieser Listener zuständig ist */
		private final int nr;

		/**
		 * Konstruktor der Klasse
		 * @param nr	Tabellenblatt (0-basierend) für den dieser Listener zuständig ist
		 */
		public ImportTableSelectionListener(int nr) {
			this.nr=nr;
		}

		@Override
		public void valueChanged(final ListSelectionEvent e) {
			String name=data.getName(nr);
			String s="";
			int[][] range=getSelectedRange(nr);
			if (range!=null) {
				s=name+"!"+Table.cellIDFromNumber(range[0])+" - "+name+"!"+Table.cellIDFromNumber(range[1]);
				lastSelectedTable=nr;
			} else {
				lastSelectedTable=-1;
			}
			edit.setText(s);
		}
	}

	/**
	 * Versucht Tabellendaten aus einer Zeichenkette zu laden.
	 * @param owner	Übergeordnetes Element (zur Anzeige von Fehlermeldungen)
	 * @param data	Tabellendaten in Textform
	 * @return	Liefert im Erfolgsfall eine {@link MultiTable}, sonst <code>null</code>.
	 */
	private static MultiTable loadDataToMultiTable(final Component owner, final String data) {
		Table table=new Table();
		table.load(data);
		if (table.getSize(0)==0 || table.getSize(1)==0) {
			JOptionPane.showMessageDialog(owner,ImportErrorInvalidData,ImportErrorTitle,JOptionPane.ERROR_MESSAGE);
			return null;
		}

		MultiTable multiTable=new MultiTable();
		multiTable.add("Tabelle 1",table);

		return multiTable;
	}

	/**
	 * Versucht Tabellendaten aus einer Datei zu laden.
	 * @param owner	Übergeordnetes Element (zur Anzeige von Fehlermeldungen)
	 * @param file	Tabellendatei
	 * @return	Liefert im Erfolgsfall eine {@link MultiTable}, sonst <code>null</code>.
	 */
	private static MultiTable loadTable(final Component owner, final File file) {
		MultiTable multiTable=new MultiTable();
		if (!multiTable.load(file)) {
			if (file!=null) JOptionPane.showMessageDialog(owner,String.format(ImportErrorFileError,file.toString()),ImportErrorTitle,JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return multiTable;
	}

	/**
	 * Versucht Tabellendaten aus einer Datei zu laden.
	 * @param owner	Übergeordnetes Element (zur Anzeige von Dialogen)
	 * @param fileLoadTitle	Titel des Dateiladedialogs
	 * @return	Liefert im Erfolgsfall eine {@link MultiTable}, sonst <code>null</code>.
	 */
	private static MultiTable loadTable(final Component owner, final String fileLoadTitle) {
		File file=Table.showLoadDialog(owner,fileLoadTitle); if (file==null) return null;
		MultiTable multiTable=loadTable(owner,file); if (multiTable==null) return null;

		/* Leere Tabellen weglassen */
		MultiTable result=new MultiTable();
		for (int i=0;i<multiTable.size();i++) {
			Table table=multiTable.get(i);
			if (table.getSize(0)>0 && table.getSize(1)>0) result.add(multiTable.getName(i),table);
		}

		/* Bleibt noch was übrig? */
		return (result.size()>0)?result:null;
	}

	/**
	 * Liefert eine in einer {@link MultiTable} enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param multiTable	Tabellenarbeitsmappe der die Daten entnommen werden sollen
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 * @see #loadData(Component, String, int, int)
	 * @see #loadDataFromFile(Component, File, int, int)
	 * @see #loadDataFromString(Component, String, int, int)
	 */
	private static String[] loadDataFromMultiTable(Component owner, final MultiTable multiTable, final int minValues, final int maxValues) {
		String[] newData=multiTable.getDataLine(minValues,maxValues);
		if (newData==null) {
			while (owner!=null && !(owner instanceof Window)) owner=owner.getParent();
			JDataLoader dataLoader=new JDataLoader((Window)owner,multiTable,minValues,maxValues,false);
			dataLoader.setVisible(true);
			newData=dataLoader.resultData;
		}

		return newData;
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param fileLoadTitle	Titel des Dateiauswahl-Dialogs
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static String[] loadData(final Component owner, final String fileLoadTitle, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadTable(owner,fileLoadTitle);
		if (multiTable==null) return null;
		return loadDataFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param fileLoadTitle	Titel des Dateiauswahl-Dialogs
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static String[] loadData(final Component owner, final String fileLoadTitle, final int countValues) {
		return loadData(owner,fileLoadTitle,countValues,countValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param file	Dateiname der zu ladenden Tabelle
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static String[] loadDataFromFile(final Component owner, final File file, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadTable(owner,file);
		if (multiTable==null) return null;
		return loadDataFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param file	Dateiname der zu ladenden Tabelle
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static String[] loadDataFromFile(final Component owner, final File file, final int countValues) {
		return loadDataFromFile(owner,file,countValues,countValues);
	}

	/**
	 * Lädt Tabellendaten aus einem String und liefert die darin enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Fehlermeldungen und Auswahl-Dialog)
	 * @param data	Zu ladende Tabellendaten
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static String[] loadDataFromString(final Component owner, final String data, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadDataToMultiTable(owner,data);
		if (multiTable==null) return null;
		return loadDataFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt Tabellendaten aus einem String und liefert die darin enthaltene Zeichenketten-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Fehlermeldungen und Auswahl-Dialog)
	 * @param data	Zu ladende Tabellendaten
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static String[] loadDataFromString(final Component owner, final String data, final int countValues) {
		return loadDataFromString(owner,data,countValues,countValues);
	}

	/**
	 * Liefert eine in einer {@link MultiTable} enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param multiTable	Tabellenarbeitsmappe der die Daten entnommen werden sollen
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 * @see #loadNumbers(Component, String, int, int)
	 * @see #loadNumbersFromFile(Component, File, int, int)
	 * @see #loadNumbersFromString(Component, String, int, int)
	 */
	private static double[] loadNumbersFromMultiTable(Component owner, final MultiTable multiTable, final int minValues, final int maxValues) {
		double[] newData=multiTable.getNumbersLine(minValues,maxValues);
		if (newData==null) {
			while (owner!=null && !(owner instanceof Window)) owner=owner.getParent();
			final JDataLoader dataLoader=new JDataLoader((Window)owner,multiTable,minValues,maxValues,true);
			dataLoader.setVisible(true);
			newData=dataLoader.resultNumbers;
		}
		return newData;
	}

	/**
	 * Liefert eine in einer {@link MultiTable} enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param multiTable	Tabellenarbeitsmappe der die Daten entnommen werden sollen
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 * @see #loadNumbersTwoRows(Component, String, int, int)
	 * @see #loadNumbersTwoRowsFromFile(Component, File, int, int)
	 * @see #loadNumbersTwoRowsFromString(Component, String, int, int)
	 */
	private static double[][] loadNumbersTwoRowsFromMultiTable(Component owner, final MultiTable multiTable, final int minValues, final int maxValues) {
		double[][] newData=multiTable.getNumbersTwoLines(minValues,maxValues);
		if (newData==null) {
			while (owner!=null && !(owner instanceof Window)) owner=owner.getParent();
			final JDataLoader dataLoader=new JDataLoader((Window)owner,multiTable,minValues,maxValues,true);
			dataLoader.setVisible(true);
			newData=new double[][]{dataLoader.resultNumbers};
		}
		return newData;
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param fileLoadTitle	Titel des Dateiauswahl-Dialogs
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[] loadNumbers(final Component owner, final String fileLoadTitle, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadTable(owner,fileLoadTitle);
		if (multiTable==null) return null;
		return loadNumbersFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param fileLoadTitle	Titel des Dateiauswahl-Dialogs
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[] loadNumbers(final Component owner, final String fileLoadTitle, final int countValues) {
		return loadNumbers(owner,fileLoadTitle,countValues,countValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param fileLoadTitle	Titel des Dateiauswahl-Dialogs
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[][] loadNumbersTwoRows(final Component owner, final String fileLoadTitle, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadTable(owner,fileLoadTitle);
		if (multiTable==null) return null;
		return loadNumbersTwoRowsFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param fileLoadTitle	Titel des Dateiauswahl-Dialogs
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[][] loadNumbersTwoRows(final Component owner, final String fileLoadTitle, final int countValues) {
		return loadNumbersTwoRows(owner,fileLoadTitle,countValues,countValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param file	Dateiname der zu ladenden Tabelle
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[] loadNumbersFromFile(final Component owner, final File file, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadTable(owner,file);
		if (multiTable==null) return null;
		return loadNumbersFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param file	Dateiname der zu ladenden Tabelle
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[] loadNumbersFromFile(final Component owner, final File file, final int countValues) {
		return loadNumbersFromFile(owner,file,countValues,countValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param file	Dateiname der zu ladenden Tabelle
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[][] loadNumbersTwoRowsFromFile(final Component owner, final File file, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadTable(owner,file);
		if (multiTable==null) return null;
		return loadNumbersTwoRowsFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt die angegebene Tabellendatei und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Dateiauswahl-Dialog, Fehlermeldungen und Auswahl-Dialog)
	 * @param file	Dateiname der zu ladenden Tabelle
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[][] loadNumbersTwoRowsFromFile(final Component owner, final File file, final int countValues) {
		return loadNumbersTwoRowsFromFile(owner,file,countValues,countValues);
	}

	/**
	 * Lädt Tabellendaten aus einem String und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Fehlermeldungen und Auswahl-Dialog)
	 * @param data	Zu ladende Tabellendaten
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[] loadNumbersFromString(final Component owner, final String data, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadDataToMultiTable(owner,data);
		if (multiTable==null) return null;
		return loadNumbersFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt Tabellendaten aus einem String und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Ist diese eindeutig, so wird sie direkt zurückgegeben;
	 * andernfalls wird ein Auswahldialog geöffnet.
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Fehlermeldungen und Auswahl-Dialog)
	 * @param data	Zu ladende Tabellendaten
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[] loadNumbersFromString(final Component owner, final String data, final int countValues) {
		return loadNumbersFromString(owner,data,countValues,countValues);
	}

	/**
	 * Lädt Tabellendaten aus einem String und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Fehlermeldungen und Auswahl-Dialog)
	 * @param data	Zu ladende Tabellendaten
	 * @param minValues	Minimale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @param maxValues	Maximale Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[][] loadNumbersTwoRowsFromString(final Component owner, final String data, final int minValues, final int maxValues) {
		final MultiTable multiTable=loadDataToMultiTable(owner,data);
		if (multiTable==null) return null;
		return loadNumbersTwoRowsFromMultiTable(owner,multiTable,minValues,maxValues);
	}

	/**
	 * Lädt Tabellendaten aus einem String und liefert die darin enthaltene Zahlen-Reihe
	 * der angegebenen Länge zurück. Es werden ein oder zwei Zeilen geliefert.
	 * Ist diese eindeutig, so wird sie direkt zurückgegeben; andernfalls wird ein Auswahldialog
	 * geöffnet (dieser erlaubt aber nur die Auswahl einer Zeile/Spalte).
	 * Mögliche Fehlermeldungen werden in Form von <code>JOptionPane</code>-Fenstern angezeigt.
	 * @param owner	Übergeordnetes Fenster (für Fehlermeldungen und Auswahl-Dialog)
	 * @param data	Zu ladende Tabellendaten
	 * @param countValues	Anzahl an Daten in einer Zeile oder Spalte, die verlangt werden.
	 * @return	Im Erfolgsfall wird ein Array der verlangten Größe zurückgegeben. Im Fehlerfall <code>null</code>. Im Fehlerfall wird außerdem ein <code>JOptionPane</code>-Fehlerdialog angezeigt.
	 */
	public static double[][] loadNumbersTwoRowsFromString(final Component owner, final String data, final int countValues) {
		return loadNumbersTwoRowsFromString(owner,data,countValues,countValues);
	}
}
