package ui.tools;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import language.Language;
import mathtools.TimeTools;
import tools.DateTools;
import ui.EditorPanelBase;

/**
 * Dieses Panel stellt einen Datum- &amp; Zeit-Editor dar.
 * @author Alexander Herzog
 * @version 1.1
 */
public class DateTimePanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7930790169278463380L;

	/** Datenmodell f�r die Datumsauswahl */
	private UtilDateModel dateModel;
	/** Eingabefeld f�r die Zeit */
	private JTextField timeEdit;
	/** Letzter g�ltiger Zeitstempel */
	private int lastValidTime=0;

	/**
	 * Konstruktor der Klasse
	 * @param readOnly	Gibt an, ob Datum und Zeit ver�ndert werden d�rfen.
	 */
	public DateTimePanel(final boolean readOnly) {
		super(new FlowLayout(FlowLayout.LEFT));

		Properties i18nStrings=new Properties();
		i18nStrings.setProperty("text.today",Language.tr("DateTimeEditor.Today"));
		i18nStrings.setProperty("text.month",Language.tr("DateTimeEditor.Month"));
		i18nStrings.setProperty("text.year",Language.tr("DateTimeEditor.Year"));
		dateModel=new UtilDateModel();
		final JDatePanelImpl datePanel=new JDatePanelImpl(dateModel,i18nStrings);
		final JDatePickerImpl datePicker=new JDatePickerImpl(datePanel,new DateLabelFormatter());
		datePicker.setEnabled(!readOnly);
		if (readOnly) for (Component component: datePicker.getComponents()) if (component instanceof JComponent) ((JComponent)component).setEnabled(false);
		datePicker.addActionListener(e->fireChangeListener());
		add(datePicker);

		add(timeEdit=new JTextField(8));
		timeEdit.setEditable(!readOnly);
		EditorPanelBase.addCheckInput(timeEdit,()->{
			Integer I=TimeTools.getTime(timeEdit,true);
			if (I!=null) lastValidTime=I;
			fireChangeListener();
		});

		setDate(DateTools.getNow(false));
	}

	/**
	 * Konstruktor der Klasse
	 * @param readOnly	Gibt an, ob Datum und Zeit ver�ndert werden d�rfen.
	 * @param ms	Einzustellender Zeitwert (in Millisekunden seit 1.1.70 00:00:00). Der Wert kann auch sp�ter �ber {@link DateTimePanel#setDate(long)} eingestellt werden.
	 * @see DateTimePanel#setDate(long)
	 */
	public DateTimePanel(final boolean readOnly, final long ms) {
		this(readOnly);
		setDate(ms);
	}

	/**
	 * Klasse zur Formatierung von Datumsangaben
	 * @see JDatePickerImpl
	 */
	private static class DateLabelFormatter extends AbstractFormatter {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -6177382334742454499L;

		/**
		 * Konstruktor der Klasse
		 */
		public DateLabelFormatter() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Object stringToValue(String text) {
			long l=DateTools.getUserDate(text);
			return new Date((l<0)?0:l);
		}

		@Override
		public String valueToString(Object value) {
			if (!(value instanceof Calendar)) return "";
			return DateTools.formatUserDateShort(((Calendar)value).getTimeInMillis()+TimeZone.getDefault().getRawOffset());
		}
	}

	/**
	 * Stellt den aktuellen Datum-Uhrzeit-Wert ein.
	 * @param ms	Einzustellender Zeitwert (in Millisekunden seit 1.1.70 00:00:00).
	 * @see DateTimePanel#getDate()
	 */
	public void setDate(final long ms) {
		final Date[] parts=DateTools.split(ms);

		dateModel.setValue(DateTools.toDate(DateTools.toMS(parts[0])+TimeZone.getDefault().getRawOffset()));
		lastValidTime=(int)(DateTools.toMS(parts[1])/1000);
		timeEdit.setText(TimeTools.formatTime(lastValidTime));
	}

	/**
	 * Pr�ft, on der eingestellte Wert g�ltig ist.
	 * @return	Gibt <code>true</code> zur�ck, wenn der momentane Editor-Wert g�ltig ist.
	 */
	public boolean check() {
		return (dateModel.getValue()!=null && TimeTools.getTime(timeEdit,true)!=null);
	}

	/**
	 * Liefert den aktuellen Datum-Uhrzeit-Wert.
	 * @return Zeitwert (in Millisekunden seit 1.1.70 00:00:00).
	 * @see DateTimePanel#setDate(long)
	 */
	public long getDate() {
		Date d=dateModel.getValue(); /* hier keine Umrechnung n�tig */
		return DateTools.toMS(d)+lastValidTime*1000;
	}

	/**
	 * Listener, die bei �nderungen der Einstellungen benachrichtigt werden sollen.
	 * @see #fireChangeListener()
	 * @see #addChangeListener(ActionListener)
	 * @see #removeChangeListener(ActionListener)
	 */
	private List<ActionListener> changeListeners=new ArrayList<>();

	/**
	 * Benachrichtigt alle in {@link #changeListeners} registrierten Listener,
	 * dass sich die Einstellungen ver�ndert haben.
	 */
	private void fireChangeListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"change");
		for (ActionListener changeListener: changeListeners) changeListener.actionPerformed(event);
	}

	/**
	 * F�gt einen Listener zu der Liste der bei �nderungen zu benachrichtigenden Listener hinzu
	 * @param changeListener	Listener, der bei �nderungen benachrichtigt werden soll
	 */
	public void addChangeListener(final ActionListener changeListener) {
		if (changeListeners.indexOf(changeListener)<0) changeListeners.add(changeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der bei �nderungen zu benachrichtigenden Listener
	 * @param changeListener	Listener, der bei �nderungen nicht mehr benachrichtigt werden soll
	 */
	public void removeChangeListener(final ActionListener changeListener) {
		changeListeners.remove(changeListener);
	}
}
