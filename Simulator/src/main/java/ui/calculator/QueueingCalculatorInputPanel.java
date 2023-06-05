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
package ui.calculator;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mathtools.NumberTools;

/**
 * Multi-Eingabeelement zur Verwendung in von {@link QueueingCalculatorTabBase}
 * abgeleiteten Klassen
 * @author Alexander Herzog
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorInputPanel {
	/**
	 * Gibt das Format für die Standard-Eingabeoption an
	 * @author Alexander Herzog
	 * @see QueueingCalculatorInputPanel#addDefault(String, NumberMode, double, String)
	 */
	public enum NumberMode {
		/**
		 * Alle Zahlen sind gültig
		 */
		DOUBLE,

		/**
		 * Nichtnegative Fließkommazahlen
		 */
		NOT_NEGATIVE_DOUBLE,

		/**
		 * Wahrscheinlichkeit (0..1)
		 */
		PROBABILITY,

		/**
		 * Positive Fließkommazahlen
		 */
		POSITIVE_DOUBLE,

		/**
		 * Ganzzahlen
		 */
		LONG,

		/**
		 * Nichtnegative Ganzzahlen
		 */
		NOT_NEGATIVE_LONG,

		/**
		 * Natürliche Zahlen
		 */
		POSITIVE_LONG,
	}

	/** Angabe, welche Zahlenbereiche gültig sind */
	private NumberMode mode;
	/** Vorgabewert für das Eingabefeld */
	private double defaulValue;
	/** Mögliche Varianten für den Wert (z.B. Zeit und Rate) */
	private List<Record> records;

	/** Titel des Eingabeelements */
	private final String title;
	/** Listener, der aufgerufen wird, wenn die Eingaben verwendet wurden und das Ergebnis neu berechnet werden soll. Darf <b>nicht</b> <code>null</code> sein. */
	private final Runnable changeListener;
	/** Das eigentliche Panel das die Elemente enthält */
	private JPanel panel;
	/** Auswahlbox für den Typ des Wertes */
	private JComboBox<String> fieldType;
	/** Eingabefeld für den Wert */
	private JTextField field;
	/** Index des zuletzt gewählten Typs */
	private int lastFieldType;
	/** Anzeige von zusätzlichen Informationen hinter dem Eingabefeld in Klammern */
	private JLabel info;
	/** Handelt es sich bei der Eingabegröße um eine Rate? */
	private final boolean isRate;

	/**
	 * Konstruktor der Klasse
	 * @param title	Titel des Eingabeelements
	 * @param changeListener	Listener, der aufgerufen wird, wenn die Eingaben verwendet wurden und das Ergebnis neu berechnet werden soll. Darf <b>nicht</b> <code>null</code> sein.
	 * @param isRate	Handelt es sich bei der Eingabegröße um eine Rate?
	 */
	public QueueingCalculatorInputPanel(final String title, final Runnable changeListener, final boolean isRate) {
		this.title=title;
		this.changeListener=changeListener;
		records=new ArrayList<>();
		lastFieldType=0;
		this.isRate=isRate;
	}

	/**
	 * Fügt die erste und damit Referenz-Option in die Liste der Eingabeoptionen ein
	 * @param label	Bezeichnung
	 * @param mode	Angabe, welche Zahlenbereiche gültig sind
	 * @param defaultValue	Vorgabewert für das Eingabefeld
	 * @param isPercent	Handelt es sich hier um eine Prozentangabe
	 * @param info	Zusätzliche Informationen hinter dem Eingabefeld in Klammern (darf <code>null</code> oder leer sein)
	 */
	public void addDefault(final String label, final NumberMode mode, final double defaultValue, final boolean isPercent, final String info) {
		if (records.size()>0) throw new RuntimeException("Default option has to be first option.");
		this.mode=mode;
		this.defaulValue=defaultValue;
		records.add(new Record(label,1,false,isPercent,info));
	}

	/**
	 * Fügt die erste und damit Referenz-Option in die Liste der Eingabeoptionen ein
	 * @param label	Bezeichnung
	 * @param mode	Angabe, welche Zahlenbereiche gültig sind
	 * @param defaultValue	Vorgabewert für das Eingabefeld
	 * @param info	Zusätzliche Informationen hinter dem Eingabefeld in Klammern (darf <code>null</code> oder leer sein)
	 */
	public void addDefault(final String label, final NumberMode mode, final double defaultValue, final String info) {
		addDefault(label,mode,defaultValue,false,info);
	}

	/**
	 * Fügt eine weitere Eingabeoption hinzu.<br>
	 * (Eine Referenz-Option muss bereits vorab definiert worden sein.)
	 * @param label	Bezeichnung
	 * @param scale	Skalierung gegenüber der Referenzoption (ist Referenz "Sekunden" und diese Option "Minuten", so muss hier 1/60 angegeben werden)
	 * @param isInverse	Sind die Werte gegenüber der Referenz invertiert? (Die Invertierung wird nach der Skalierung durchgeführt.)
	 * @param isPercent	Handelt es sich hier um eine Prozentangabe
	 * @param info	Zusätzliche Informationen hinter dem Eingabefeld in Klammern (darf <code>null</code> oder leer sein)
	 */
	public void addOption(final String label, final double scale, final boolean isInverse, final boolean isPercent, final String info) {
		if (records.size()==0) throw new RuntimeException("Default option has to be first option.");
		records.add(new Record(label,scale,isInverse,isPercent,info));
	}

	/**
	 * Fügt eine weitere Eingabeoption hinzu.<br>
	 * (Eine Referenz-Option muss bereits vorab definiert worden sein.)
	 * @param label	Bezeichnung
	 * @param scale	Skalierung gegenüber der Referenzoption (ist Referenz "Sekunden" und diese Option "Minuten", so muss hier 1/60 angegeben werden)
	 * @param isInverse	Sind die Werte gegenüber der Referenz invertiert? (Die Invertierung wird nach der Skalierung durchgeführt.)
	 * @param info	Zusätzliche Informationen hinter dem Eingabefeld in Klammern (darf <code>null</code> oder leer sein)
	 */
	public void addOption(final String label, final double scale, final boolean isInverse, final String info) {
		addOption(label,scale,isInverse,false,info);
	}

	/**
	 * Stellt ein, welche der definierten Eingabeoptionen initial ausgewählt sein soll
	 * @param index	Index der initial sichtbaren Eingabeoption
	 */
	public void setVisibleOptionIndex(final int index) {
		lastFieldType=index;
	}

	/**
	 * Erstellt das Panel, welche die Eingabeelemente beinhaltet
	 * @return	Panel, welche die Eingabeelemente beinhaltet
	 * @see #get()
	 */
	private JPanel build() {
		final JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		JPanel line;

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+title+"</b></body></html>"));

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final int currentIndex;
		if (records.size()==1) {
			final Record record=records.get(0);
			line.add(new JLabel("<html><body>"+record.label+"</body></html>"));
			currentIndex=0;
		} else {
			line.add(fieldType=new JComboBox<>(records.stream().map(record->"<html><body>"+record.label+"</body></html>").toArray(String[]::new)));
			line.add(new JLabel("="));
			line.add(Box.createHorizontalStrut(2));
			fieldType.setSelectedIndex(lastFieldType);
			currentIndex=lastFieldType;
			lastFieldType=0;
		}
		final String value;
		if (records.get(currentIndex).isPercent) {
			value=NumberTools.formatPercent(NumberTools.reduceDigits(defaulValue,9),10);
		} else {
			value=NumberTools.formatNumber(NumberTools.reduceDigits(defaulValue,10),14);
		}
		line.add(field=new JTextField(value,15));
		final String infoText=records.get(lastFieldType).info;
		line.add(info=new JLabel((infoText!=null && !infoText.trim().isEmpty())?"<html><body>("+infoText+")</body></html>":""));

		field.addActionListener(e->changeListener.run());
		field.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {changeListener.run();}
		});

		if (fieldType!=null) {
			changeFieldType();
			fieldType.addActionListener(e->{changeFieldType(); changeListener.run();});
		}

		return panel;
	}

	/**
	 * Wird aufgerufen, wenn der Typ des Wertes geändert wird.
	 * @see #fieldType
	 */
	private void changeFieldType() {
		final int newFieldType=fieldType.getSelectedIndex();
		if (lastFieldType!=newFieldType) {
			final Double D=NumberTools.getDouble(field.getText().trim());
			if (D!=null) {
				double d=D.doubleValue();
				if (lastFieldType>0) {
					final Record record=records.get(lastFieldType);
					if (record.isInverse) d=1/d;
					if (isRate) d=d/record.scale; else d=d*record.scale;
				}
				if (newFieldType>0) {
					final Record record=records.get(newFieldType);
					if (isRate) d=d*record.scale; else d=d/record.scale;
					if (record.isInverse) d=1/d;
				}
				final Record record=records.get(newFieldType);
				if (record.isPercent) {
					field.setText(NumberTools.formatPercent(NumberTools.reduceDigits(d,9),10));
				} else {
					field.setText(NumberTools.formatNumber(NumberTools.reduceDigits(d,10),14));
				}
			}
			final Record record=records.get(newFieldType);
			if (record.info==null || record.info.trim().isEmpty()) {
				info.setText("");
			} else {
				info.setText("<html><body>("+record.info+")</body></html>");
			}
		}
		lastFieldType=newFieldType;
	}

	/**
	 * Liefert ein Panel, welche die Eingabeelemente beinhaltet
	 * @return	Panel, welche die Eingabeelemente beinhaltet
	 */
	public JPanel get() {
		if (panel==null) panel=build();
		return panel;
	}

	/**
	 * Prüft, ob der Wert ganzzahlig ist
	 * @param value	Zu prüfender Wert
	 * @return	Liefert <code>true</code>, wenn der Wert ganzzahlig ist
	 */
	private boolean isLong(final double value) {
		return (Math.abs(value-Math.round(value))<10E-10);
	}

	/**
	 * Prüft, ob die Eingaben gültig sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Eingaben gültig sind.
	 * @see #isValueOk()
	 */
	private boolean isValueOkIntern() {
		final Double D=NumberTools.getDouble(field.getText().trim());
		if (D==null) return false;
		double d=D.doubleValue();

		if (fieldType!=null && fieldType.getSelectedIndex()!=0) {
			final Record record=records.get(fieldType.getSelectedIndex());
			if (record.isInverse) d=1/d;
			if (isRate) d=d/record.scale; else d=d*record.scale;
		}

		switch (mode) {
		case DOUBLE: return true;
		case LONG: return isLong(d);
		case NOT_NEGATIVE_DOUBLE: return d>=0;
		case PROBABILITY: return d>=0 && d<=1;
		case NOT_NEGATIVE_LONG: return isLong(d) && d>=0;
		case POSITIVE_DOUBLE: return d>0;
		case POSITIVE_LONG: return isLong(d) && d>0;
		default: return false;
		}
	}

	/**
	 * Prüft, ob die Eingaben gültig sind und färbt das Eingabefeld entsprechend ein.
	 * @return	Gibt <code>true</code> zurück, wenn die Eingaben gültig sind.
	 * @see QueueingCalculatorInputPanel#getDouble()
	 * {@link QueueingCalculatorInputPanel#getLong()}
	 */
	public boolean isValueOk() {
		final boolean ok=isValueOkIntern();
		field.setBackground(ok?NumberTools.getTextFieldDefaultBackground():Color.RED);
		return ok;
	}

	/**
	 * Liefert die Eingabe in der Standardform als Fließkommazahl zurück.
	 * @return	Eingabe in der Standardform als Fließkommazahl
	 * @see QueueingCalculatorInputPanel#isValueOk()
	 */
	public double getDouble() {
		if (!isValueOk()) return 0;

		double d=NumberTools.getDouble(field.getText().trim());

		if (fieldType!=null && fieldType.getSelectedIndex()!=0) {
			final Record record=records.get(fieldType.getSelectedIndex());
			if (record.isInverse) d=1/d;
			if (isRate) d=d/record.scale; else d=d*record.scale;

		}

		return d;
	}

	/**
	 * Liefert die Eingabe in der Standardform als Ganzzahl zurück.
	 * @return	Eingabe in der Standardform als Ganzzahl
	 * @see QueueingCalculatorInputPanel#isValueOk()
	 */
	public long getLong() {
		if (!isValueOk()) return 0;

		double d=NumberTools.getDouble(field.getText().trim());

		if (fieldType!=null && fieldType.getSelectedIndex()!=0) {
			final Record record=records.get(fieldType.getSelectedIndex());
			if (record.isInverse) d=1/d;
			if (isRate) d=d/record.scale; else d=d*record.scale;
		}

		return Math.round(d);
	}

	/**
	 * Diese Klasse hält die Informationen für eine mögliche Darstellungsform
	 * des Wertes vor.
	 * @see QueueingCalculatorInputPanel#addOption(String, double, boolean, String)
	 * @see QueueingCalculatorInputPanel#addOption(String, double, boolean, boolean, String)
	 */
	private static class Record {
		/** Beschriftung */
		final String label;
		/** Skalierung */
		final double scale;
		/** Ist der Wert Invers zum Basiswert anzusehen? */
		final boolean isInverse;
		/** Handelt es sich um eine Prozentangabe? */
		final boolean isPercent;
		/** Zusätzliche Informationen hinter dem Eingabefeld in Klammern (darf <code>null</code> oder leer sein) */
		final String info;

		/**
		 * Konstruktor der Klasse
		 * @param label	Beschriftung
		 * @param scale	Skalierung
		 * @param isInverse	Ist der Wert Invers zum Basiswert anzusehen?
		 * @param isPercent	Handelt es sich um eine Prozentangabe?
		 * @param info	Zusätzliche Informationen hinter dem Eingabefeld in Klammern (darf <code>null</code> oder leer sein)
		 */
		public Record(final String label, final double scale, final boolean isInverse, final boolean isPercent, final String info) {
			this.label=label;
			this.scale=scale;
			this.isInverse=isInverse;
			this.isPercent=isPercent;
			this.info=info;
		}
	}
}
