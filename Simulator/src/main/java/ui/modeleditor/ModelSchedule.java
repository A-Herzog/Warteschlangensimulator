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
package ui.modeleditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.FullTextSearch;

/**
 * Diese Klasse bildet einen Zeitplan (für Schichten oder für Kundenankünfte) ab.
 * @author Alexander Herzog
 */
public final class ModelSchedule implements Cloneable {
	/**
	 * Name des XML-Elements, das die Zeitplan-Daten enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellZeitplan"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Art der Fortsetzung des Plans
	 * @see ModelSchedule#getRepeatMode()
	 * @see ModelSchedule#setRepeatMode(RepeatMode)
	 */
	public enum RepeatMode {
		/** Wiederholung des Plans nach seinem Ende */
		REPEAT_MODE_REPEAT,

		/** Wiederholung des Plans nach seinem Ende, aber füllt vorher bis Ende des Tages mit Nullen auf */
		REPEAT_MODE_REPEAT_FILL_DAY,

		/** Setzt den Plan mit dem letzten Wert beliebig weit fort */
		REPEAT_MODE_STAY_AT_LAST_VALUE,

		/** Setzt den Plan mit 0 nach dem letzten Slot fort */
		REPEAT_MODE_ZERO
	}

	/**
	 * Name des Zeitplans
	 * @see #getName()
	 * @see #setName(String)
	 */
	private String name;

	/**
	 * Anzahl an Sekunden, die ein Zeitslot dauern soll
	 * @see #getDurationPerSlot()
	 * @see #setDurationPerSlot(int)
	 */
	private int durationPerSlot;

	/**
	 * Maximaler y-Achsen-Wert im Editor
	 * @see #getEditorMaxY()
	 * @see #setEditorMaxY(int)
	 */
	private int editorMaxY;

	/**
	 * Art der Fortsetzung des Plans
	 * @see #getRepeatMode()
	 * @see #setRepeatMode(RepeatMode)
	 */
	private RepeatMode repeatMode;

	/**
	 * Werte pro Zeitslot
	 * @see #getSlots()
	 * @see #setSlots(List)
	 */
	private int[] slots;

	/**
	 * Konstruktor der Klasse <code>Schedule</code>
	 */
	public ModelSchedule() {
		reset();
	}

	/**
	 * Konstruktor der Klasse <code>Schedule</code>
	 * @param name	Name des Zeitplans
	 */
	public ModelSchedule(final String name) {
		reset();
		setName(name);
	}

	/**
	 * Setzt alle Einstellungen im Zeitplan zurück.
	 */
	public void reset() {
		name="";
		durationPerSlot=1800;
		editorMaxY=10;
		repeatMode=RepeatMode.REPEAT_MODE_REPEAT_FILL_DAY;
		slots=new int[0];
	}

	/**
	 * Liefert den Namen des Zeitplans
	 * @return	Name des Zeitplans
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen des Zeitplans ein
	 * @param name	Neuer Name des Zeitplans
	 */
	public void setName(final String name) {
		if (name==null) this.name=""; else this.name=name.trim();
	}

	/**
	 * Liefert die Anzahl an Sekunden, die ein Zeitslot dauern soll
	 * @return	Anzahl an Sekunden, die ein Zeitslot dauern soll
	 */
	public int getDurationPerSlot() {
		return durationPerSlot;
	}

	/**
	 * Stellt die Anzahl an Sekunden ein, die ein Zeitslot dauern soll
	 * @param durationPerSlot	Neuer Wert für die Anzahl an Sekunden, die ein Zeitslot dauern soll
	 */
	public void setDurationPerSlot(final int durationPerSlot) {
		if (durationPerSlot<=0) return;
		this.durationPerSlot=durationPerSlot;
	}

	/**
	 * Gibt an, wie groß der maximale y-Achsen-Wert im Editor sein soll
	 * @return	Maximaler y-Achsen-Wert im Editor
	 */
	public int getEditorMaxY() {
		return editorMaxY;
	}

	/**
	 * Stellt den maximalen y-Achsen-Wert im Editor ein
	 * @param editorMaxY	Neuer maximaler y-Achsen-Wert im Editor
	 */
	public void setEditorMaxY(final int editorMaxY) {
		if (editorMaxY>=1) this.editorMaxY=editorMaxY;
	}

	/**
	 * Gibt an, wie der Plan nach seinem Ende fortgesetzt werden soll
	 * @return	Art der Fortsetzung des Plans
	 * @see RepeatMode
	 */
	public RepeatMode getRepeatMode() {
		return repeatMode;
	}

	/**
	 * Stellt ein, wie der Plan nach seinem Ende fortgesetzt werden soll
	 * @param repeatMode	Neue Art der Fortsetzung des Plans
	 * @see RepeatMode
	 */
	public void setRepeatMode(final RepeatMode repeatMode) {
		this.repeatMode=repeatMode;
	}

	/**
	 * Liefert die Werte pro Zeitslot
	 * @return	Werte pro Zeitslot
	 */
	public List<Integer> getSlots() {
		final List<Integer> result=new ArrayList<>();
		for (int slot: slots) result.add(slot);
		return result;
	}

	/**
	 * Liefert die Anzahl an Zeitslots
	 * @return	Anzahl an Zeitslots
	 */
	public int getSlotCount() {
		return slots.length;
	}

	/**
	 * Stellt die Werte pro Zeitslot ein
	 * @param slots	Werte pro Zeitslot
	 */
	public void setSlots(final List<Integer> slots) {
		if (slots==null) return;
		this.slots=new int[slots.size()];
		for (int i=0;i<slots.size();i++) this.slots[i]=Math.max(0,slots.get(i));
	}

	/**
	 * Überprüft, ob der Zeitplan inhaltlich mit einem anderen Zeitplan übereinstimmt
	 * @param otherSchedule	Zeitplan, der mit dem aktuellen Zeitplan verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Zeitpläne übereinstimmen
	 */
	public boolean equalsSchedule(final ModelSchedule otherSchedule) {
		if (otherSchedule==null) return false;

		if (!name.equals(otherSchedule.name)) return false;
		if (durationPerSlot!=otherSchedule.durationPerSlot) return false;
		if (editorMaxY!=otherSchedule.editorMaxY) return false;
		if (repeatMode!=otherSchedule.repeatMode) return false;
		if (slots.length!=otherSchedule.slots.length) return false;
		for (int i=0;i<slots.length;i++) if (slots[i]!=otherSchedule.slots[i]) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Zeitplans
	 * @return	Kopie des Zeitplans
	 */
	@Override
	public ModelSchedule clone() {
		final ModelSchedule clone=new ModelSchedule();

		clone.name=name;
		clone.durationPerSlot=durationPerSlot;
		clone.editorMaxY=editorMaxY;
		clone.repeatMode=repeatMode;
		clone.slots=new int[slots.length];
		for (int i=0;i<slots.length;i++) clone.slots[i]=slots[i];

		return clone;
	}

	/**
	 * Speichert den Zeitplan in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter Knoten des Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addToXML(final Document doc, final Element parent) {
		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		node.setAttribute(Language.trPrimary("Surface.XML.Schedule.Name"),name);
		node.setAttribute(Language.trPrimary("Surface.XML.Schedule.DurationPerSlot"),""+durationPerSlot);
		node.setAttribute(Language.trPrimary("Surface.XML.Schedule.EditorMaxY"),""+editorMaxY);

		String repeat="";
		switch (repeatMode) {
		case REPEAT_MODE_REPEAT:
			repeat=Language.trPrimary("Surface.XML.Schedule.RepeatSchedule.Repeat");
			break;
		case REPEAT_MODE_REPEAT_FILL_DAY:
			repeat=Language.trPrimary("Surface.XML.Schedule.RepeatSchedule.RepeatDays");
			break;
		case REPEAT_MODE_STAY_AT_LAST_VALUE:
			repeat=Language.trPrimary("Surface.XML.Schedule.RepeatSchedule.LastValue");
			break;
		case REPEAT_MODE_ZERO:
			repeat=Language.trPrimary("Surface.XML.Schedule.RepeatSchedule.Zero");
			break;
		}

		node.setAttribute(Language.trPrimary("Surface.XML.Schedule.RepeatSchedule"),repeat);

		StringBuilder sb=new StringBuilder();
		for (int slot: slots) {
			if (sb.length()>0) sb.append(";");
			sb.append(slot);
		}
		node.setTextContent(sb.toString());
	}

	/**
	 * Versucht einen Zeitplan aus einem gegebenen xml-Element zu laden
	 * @param node	XML-Element, aus dem der Zeitplan geladen werden soll
	 * @return Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		Long L;

		name=Language.trAllAttribute("Surface.XML.Schedule.Name",node);

		L=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.XML.Schedule.DurationPerSlot",node));
		if (L==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Surface.XML.Schedule.DurationPerSlot"),node.getNodeName())+" "+Language.tr("Surface.XML.ErrorInfo.PositiveIntegerNeeded");
		durationPerSlot=(int)((long)L);

		L=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.XML.Schedule.EditorMaxY",node));
		if (L==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Surface.XML.Schedule.EditorMaxY"),node.getNodeName())+" "+Language.tr("Surface.XML.ErrorInfo.PositiveIntegerNeeded");
		editorMaxY=(int)((long)L);

		final String repeat=Language.trAllAttribute("Surface.XML.Schedule.RepeatSchedule",node);
		repeatMode=null;
		if (Language.trAll("Surface.XML.Schedule.RepeatSchedule.Repeat",repeat)) repeatMode=RepeatMode.REPEAT_MODE_REPEAT;
		if (Language.trAll("Surface.XML.Schedule.RepeatSchedule.RepeatDays",repeat)) repeatMode=RepeatMode.REPEAT_MODE_REPEAT_FILL_DAY;
		if (Language.trAll("Surface.XML.Schedule.RepeatSchedule.LastValue",repeat)) repeatMode=RepeatMode.REPEAT_MODE_STAY_AT_LAST_VALUE;
		if (Language.trAll("Surface.XML.Schedule.RepeatSchedule.Zero",repeat)) repeatMode=RepeatMode.REPEAT_MODE_ZERO;
		if (repeatMode==null) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Surface.XML.Schedule.RepeatSchedule"),node.getNodeName());

		String[] values=node.getTextContent().split(";");
		slots=new int[values.length];
		for (int i=0;i<values.length;i++) {
			Integer I=0;
			if (!values[i].trim().isEmpty()) I=NumberTools.getNotNegativeInteger(values[i]);
			if (I==null) return String.format(Language.tr("Surface.Schedule.ErrorScheduleValue"),i+1,node.getNodeName());
			slots[i]=I;
		}

		return null;
	}

	/**
	 * Liefert den Zeitplan-Wert an einem bestimmten Zeitpunkt
	 * @param timeInSeconds	Zeitpunkt (gemessen in Sekunden) für den der Wert gemäß Zeitplan ermittelt werden soll
	 * @return	Wert an dem Zeitpunkt
	 */
	public int getValueAtTime(final long timeInSeconds) {
		final long slotNumber=timeInSeconds/durationPerSlot;
		return getValueBySlotNumber(slotNumber);
	}

	/**
	 * Berechnet wie viel Bedienleistung (in Mitarbeiterstunden) in einem bestimmten Zeitfesnter zur Verfügung steht
	 * @param timeSec1	Startzeitpunkt in Sekunden
	 * @param timeSec2	Endzeitpunkt in Sekunden
	 * @return	Verfügbare Mitarbeiterstunden in dem Bereich
	 */
	public double getAvailbaleHoursByTimeRange(final long timeSec1, final long timeSec2) {
		final int slotNr1=(int)(timeSec1/durationPerSlot);
		final int slotNr2=(int)(timeSec2/durationPerSlot);
		final double partIn1=((double)timeSec1%durationPerSlot)/durationPerSlot;
		final double partIn2=1-((double)timeSec2%durationPerSlot)/durationPerSlot;

		double sum=0;
		for (int nr=slotNr1;nr<=slotNr2;nr++) {
			final int count=getValueBySlotNumber(nr);
			double hours=count*durationPerSlot/3600.0;
			double removePart=0;
			if (nr==slotNr1) removePart+=partIn1;
			if (nr==slotNr2) removePart+=partIn2;
			hours*=(1-removePart);
			sum+=hours;
		}

		return sum;
	}

	/**
	 * Liefert den Zeitplan-Wert für einen bestimmten Zeitslot
	 * @param slotNumber	Nummer des Zeitslots (kann größer als die Anzahl an Slots sein; es wird dann die Repeat-Regel verwendet)
	 * @return	Wert für den Zeitslot (kann -1 sein, wenn 0 und nie wieder ein Wert &gt;0 folgt)
	 */
	public int getValueBySlotNumber(final long slotNumber) {
		if (slots.length==0) return 0;
		if (slotNumber<slots.length) return slots[(int)slotNumber];

		switch (repeatMode) {
		case REPEAT_MODE_REPEAT:
			return slots[(int)(slotNumber%(slots.length))];
		case REPEAT_MODE_REPEAT_FILL_DAY:
			final int slotsPerDay=86400/durationPerSlot;
			int missing=slots.length%slotsPerDay;
			if (missing>0) missing=slotsPerDay-missing;
			if (missing!=0) slots=Arrays.copyOf(slots,slots.length+missing);
			return slots[(int)(slotNumber%(slots.length))];
		case REPEAT_MODE_STAY_AT_LAST_VALUE:
			return slots[slots.length-1];
		case REPEAT_MODE_ZERO:
			return -1;
		default:
			return -1;
		}
	}

	/**
	 * Gibt an wie viele Bediener maximal in einem Slot aktiv sind
	 * @return	Maximale Anzahl an gleichzeitig aktiven Bedienern
	 */
	public int getMaxValue() {
		int max=0;
		for (int slot: slots) max=Math.max(max,slot);
		return max;
	}

	/**
	 * Sucht einen Text in den Daten des Zeitplans.
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher) {
		searcher.testString(Language.tr("Schedule.Dialog.Name"),name,newName->{name=newName;});
	}
}
