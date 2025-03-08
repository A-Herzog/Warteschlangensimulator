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

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementUserStatisticData;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsTimeContinuousPerformanceIndicator;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Passiert ein Kunde diese Station, so werden ein oder mehrere
 * Rechenausdrücke ausgewertet und die Ergebnisse als zusätzliche
 * Statistikdaten erfasst.
 * @author Alexander Herzog
 * @see Statistics#userStatistics
 * @see Statistics#userStatisticsContinuous
 */
public class ModelElementUserStatistic extends ModelElementMultiInSingleOutBox {
	/**
	 * Art der Erfassung der Kenngrößen
	 */
	public enum RecordMode {
		/** Nur global über alle Kundentypen hinweg */
		GLOBAL(true,false,true,()->Language.trPrimary("Surface.UserStatistic.XML.Mode.Global"),()->Language.trAll("Surface.UserStatistic.XML.Mode.Global")),
		/** Nur pro Kundentyp */
		CLIENT_TYPE(false,true,false,()->Language.trPrimary("Surface.UserStatistic.XML.Mode.ClientType"),()->Language.trAll("Surface.UserStatistic.XML.Mode.ClientType")),
		/** Sowohl global als auch pro Kundentyp */
		BOTH(true,true,false,()->Language.trPrimary("Surface.UserStatistic.XML.Mode.Both"),()->Language.trAll("Surface.UserStatistic.XML.Mode.Both"));

		/** Enthält der Eintrag eine globale Erfassung? */
		public final boolean containsGlobal;
		/** Enthält der Eintrag eine Erfassung pro Kundentyp? */
		public final boolean containsClientType;
		/** Handelt es sich um den standardmäßig aktiven Eintrag? */
		public final boolean isDefaultMode;

		/** Liefert den primären xml-Namen in der aktiven Sprache für den Eintrag */
		private final Supplier<String> nameGetter;
		/** Liefert alle xml-Namen in allen Sprachen für den Eintrag */
		private final Supplier<String[]> allNamesGetter;

		/**
		 * Konstruktor des Enum
		 * @param containsGlobal	Enthält der Eintrag eine globale Erfassung?
		 * @param containsClientType	Enthält der Eintrag eine Erfassung pro Kundentyp?
		 * @param isDefaultMode	Handelt es sich um den standardmäßig aktiven Eintrag?
		 * @param nameGetter	Liefert den primären xml-Namen in der aktiven Sprache für den Eintrag
		 * @param allNamesGetter	Liefert alle xml-Namen in allen Sprachen für den Eintrag
		 */
		RecordMode(final boolean containsGlobal, final boolean containsClientType, final boolean isDefaultMode, final Supplier<String> nameGetter, final Supplier<String[]> allNamesGetter) {
			this.containsGlobal=containsGlobal;
			this.containsClientType=containsClientType;
			this.isDefaultMode=isDefaultMode;
			this.nameGetter=nameGetter;
			this.allNamesGetter=allNamesGetter;
		}

		/**
		 * Liefert den xml-Namen des Eintrags (in der aktuellen Sprache).
		 * @return	xml-Name des Eintrags (in der aktuellen Sprache)
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Liefert den Eintrag zu einem xml-Namen
		 * @param name	xml-Name zu dem der zugehörige Eintrag ermittelt werden soll
		 * @return	Eintrag zu dem Namen oder Vorgabe-Eintrag, wenn kein Eintrag zu dem Namen passt
		 */
		public static RecordMode byName(final String name) {
			for (RecordMode mode: values()) {
				final String[] names=mode.allNamesGetter.get();
				for (String test: names) if (test.equalsIgnoreCase(name)) return mode;
			}
			return getDefault();
		}

		/**
		 * Liefert den standardmäßig aktiven Eintrag.
		 * @return	Standardmäßig aktiver Eintrag
		 */
		public static RecordMode getDefault() {
			return Stream.of(values()).filter(mode->mode.isDefaultMode).findFirst().get();
		}
	}

	/**
	 * Art der Erfassung der Kenngrößen
	 * @see RecordMode
	 */
	private RecordMode recordMode;

	/**
	 * Liste mit den Statistikbezeichnern
	 * @see #getKeys()
	 */
	private List<String> key;

	/**
	 * Liste mit den Angaben, ob es sich bei den Werten um Zeiten handelt
	 * @see #getIsTime()
	 */
	private List<Boolean> isTime;

	/**
	 * Liste mit den auszuwertenden Ausdrücken
	 * @see #getExpressions()
	 */
	private List<String> expression;

	/**
	 * Liste mit den Angaben, ob diskrete Werte (<code>false</code>) oder zeitliche Verläufe (<code>true</code>) erfasst werden sollen
	 * @see #getIsContinuous
	 */
	private List<Boolean> isContinuous;

	/**
	 * Konstruktor der Klasse <code>ModelElementUserStatistic</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementUserStatistic(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		recordMode=RecordMode.getDefault();
		key=new ArrayList<>();
		isTime=new ArrayList<>();
		expression=new ArrayList<>();
		isContinuous=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_USER_STATISTICS.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.UserStatistic.Tooltip");
	}

	/**
	 * Liefert den Erfassungsmodus für die Kenngrößen.
	 * @return	Erfassungsmodus für die Kenngrößen
	 * @see RecordMode
	 * @see #setRecordMode(RecordMode)
	 */
	public RecordMode getRecordMode() {
		return recordMode;
	}

	/**
	 * Stellt den Erfassungsmodus für die Kenngrößen ein.
	 * @param recordMode	Erfassungsmodus für die Kenngrößen
	 * @see RecordMode
	 * @see #getRecordMode()
	 */
	public void setRecordMode(RecordMode recordMode) {
		this.recordMode=(recordMode==null)?RecordMode.GLOBAL:recordMode;
	}

	/**
	 * Liefert die Liste mit den Statistikbezeichnern
	 * @return	Liste mit Statistikbezeichnern
	 */
	public List<String> getKeys() {
		return key;
	}

	/**
	 * Liefert die Liste mit den Angaben, ob es sich bei den Werten um Zeiten handelt
	 * @return	Liste mit Angaben, ob es sich um Zeitangaben handelt
	 */
	public List<Boolean> getIsTime() {
		return isTime;
	}

	/**
	 * Liefert die Liste mit den Angaben, ob diskrete Werte oder zeitliche Verläufe erfasst werden sollen
	 * @return	Liste mit den Angaben, ob diskrete Werte (<code>false</code>) oder zeitliche Verläufe (<code>true</code>) erfasst werden sollen
	 */
	public List<Boolean> getIsContinuous() {
		return isContinuous;
	}

	/**
	 * Rückgabewert für {@link ModelElementUserStatistic#getIsTimeForKey(String)}
	 * @see ModelElementUserStatistic#getIsTimeForKey(String)
	 */
	public enum IsTime {
		/** Bezeichner ist Zeitangabe */
		YES(true),
		/** Bezeichner ist keine Zeitangabe */
		NO(false),
		/** Bezeichner existiert nicht */
		NOT_FOUND(true);

		/**
		 * Boolean-Repräsentation des Wertes
		 */
		public boolean bool;

		/**
		 * Konstruktor des Enum
		 * @param bool	Boolean-Repräsentation des Wertes
		 */
		IsTime(final boolean bool) {
			this.bool=bool;
		}

		/**
		 * Liefert die zu einem boolschen Wert gehörige Repräsentation in diesem Enum
		 * @param bool	Boolscher Wert zu dem die Repräsentation in diesem Enum geliefert werden soll
		 * @return	Enum passend zu dem boolschen Wert
		 */
		public static IsTime fromBoolean(final boolean bool) {
			return bool?YES:NO;
		}
	}

	/**
	 * Gibt an, ob die Statistik zu einem Bezeichner eine Zeitangabe ist oder nicht
	 * @param key	Bezeichner, zu dem ermittelt werden soll, ob es sich um eine Zeitangabe handelt oder nicht
	 * @return	Gibt <code>null</code> zurück, wenn es keine Statistik zu dem angegebenen Bezeichner gibt, sonst wahr oder falsch.
	 * @see IsTime
	 */
	public IsTime getIsTimeForKey(final String key) {
		for (int i=0;i<Math.min(this.key.size(),isTime.size());i++) {
			if (this.key.get(i).equalsIgnoreCase(key)) return IsTime.fromBoolean(isTime.get(i));
		}
		return IsTime.NOT_FOUND;
	}

	/**
	 * Liefert die Liste mit den auszuwertenden Ausdrücken
	 * @return	Liste mit den auszuwertenden Ausdrücken
	 */
	public List<String> getExpressions() {
		return expression;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementUserStatistic)) return false;
		final ModelElementUserStatistic otherStation=(ModelElementUserStatistic)element;

		if (recordMode!=otherStation.recordMode) return false;
		if (key.size()!=otherStation.key.size()) return false;
		if (isTime.size()!=otherStation.isTime.size()) return false;
		if (expression.size()!=otherStation.expression.size()) return false;
		if (isContinuous.size()!=otherStation.isContinuous.size()) return false;
		for (int i=0;i<key.size();i++) if (!otherStation.key.get(i).equals(key.get(i))) return false;
		for (int i=0;i<isTime.size();i++) if (!otherStation.isTime.get(i).equals(isTime.get(i))) return false;
		for (int i=0;i<expression.size();i++) if (!otherStation.expression.get(i).equals(expression.get(i))) return false;
		for (int i=0;i<isContinuous.size();i++) if (!otherStation.isContinuous.get(i).equals(isContinuous.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementUserStatistic) {
			final ModelElementUserStatistic copySource=(ModelElementUserStatistic)element;
			recordMode=copySource.recordMode;
			key.addAll(copySource.key);
			isTime.addAll(copySource.isTime);
			expression.addAll(copySource.expression);
			isContinuous.addAll(copySource.isContinuous);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementUserStatistic clone(final EditModel model, final ModelSurface surface) {
		final ModelElementUserStatistic element=new ModelElementUserStatistic(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.UserStatistic.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.UserStatistic.Name.Short");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(230,230,230);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementUserStatisticDialog(owner,ModelElementUserStatistic.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.UserStatistic.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (!recordMode.isDefaultMode) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.UserStatistic.XML.Mode")));
			sub.setTextContent(recordMode.getName());
		}

		for (int i=0;i<Math.min(Math.min(Math.min(key.size(),expression.size()),isTime.size()),isContinuous.size());i++) {
			final String k=key.get(i);
			final String e=expression.get(i);
			if (k==null || k.isBlank() || e==null || e.isBlank()) continue;
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.UserStatistic.XML.Record")));
			sub.setAttribute(Language.trPrimary("Surface.UserStatistic.XML.Record.Key"),k);
			sub.setAttribute(Language.trPrimary("Surface.UserStatistic.XML.Record.IsTime"),isTime.get(i)?"1":"0");
			sub.setAttribute(Language.trPrimary("Surface.UserStatistic.XML.Record.IsContinuous"),isContinuous.get(i)?"1":"0");
			sub.setTextContent(e);
		}
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.UserStatistic.XML.Mode",name)) {
			recordMode=RecordMode.byName(content);
			return null;
		}

		if (Language.trAll("Surface.UserStatistic.XML.Record",name)) {
			final String keyString=Language.trAllAttribute("Surface.UserStatistic.XML.Record.Key",node);
			final String isTimeString=Language.trAllAttribute("Surface.UserStatistic.XML.Record.IsTime",node);
			final String isContinuousString=Language.trAllAttribute("Surface.UserStatistic.XML.Record.IsContinuous",node);
			final String e=content;
			if (!keyString.isBlank() && !e.isBlank()) {
				key.add(keyString.trim());
				isTime.add(isTimeString==null || isTimeString.isEmpty() || !isTimeString.equals("0"));
				isContinuous.add(isContinuousString!=null && isContinuousString.equals("1"));
				expression.add(e.trim());
			}
			return null;
		}

		return null;
	}

	/**
	 * Fügt stations-bedingte zusätzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		String[] keys;
		boolean[] isTime;

		/* Diskret */

		keys=((RunElementUserStatisticData)builder.data).getKeysDiscrete();
		isTime=((RunElementUserStatisticData)builder.data).getIsTimeDiscrete();
		final StatisticsDataPerformanceIndicatorWithNegativeValues[] indicatorsDiscrete=((RunElementUserStatisticData)builder.data).getIndicatorsDiscrete();

		for (int i=0;i<keys.length;i++) if (indicatorsDiscrete[i]!=null) {
			builder.results.append("\n"+keys[i]+":\n");
			builder.results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(indicatorsDiscrete[i].getCount())+"\n");
			if (isTime[i]) {
				builder.results.append(Language.tr("Statistics.AverageUserTime")+" E[X]="+TimeTools.formatExactTime(indicatorsDiscrete[i].getMean())+" ("+NumberTools.formatNumber(indicatorsDiscrete[i].getMean())+")\n");
				builder.results.append(Language.tr("Statistics.StdDevUserTime")+" Std[X]="+TimeTools.formatExactTime(indicatorsDiscrete[i].getSD())+" ("+NumberTools.formatNumber(indicatorsDiscrete[i].getSD())+")\n");
				builder.results.append(Language.tr("Statistics.VarianceUserTime")+" Var[X]="+TimeTools.formatExactTime(indicatorsDiscrete[i].getVar())+" ("+NumberTools.formatNumber(indicatorsDiscrete[i].getVar())+")\n");
				builder.results.append(Language.tr("Statistics.CVUserTime")+" CV[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getCV())+"\n");
				builder.results.append(Language.tr("Statistics.Skewness")+" Sk[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getSk())+"\n");
				builder.results.append(Language.tr("Statistics.Kurt")+" Kurt[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getKurt())+"\n");
				builder.results.append(Language.tr("Statistics.MinimumUserTime")+" Min[X]="+TimeTools.formatExactTime(indicatorsDiscrete[i].getMin())+" ("+NumberTools.formatNumber(indicatorsDiscrete[i].getMin())+")\n");
				builder.results.append(Language.tr("Statistics.MaximumUserTime")+" Max[X]="+TimeTools.formatExactTime(indicatorsDiscrete[i].getMax())+" ("+NumberTools.formatNumber(indicatorsDiscrete[i].getMax())+")\n");
			} else {
				builder.results.append(Language.tr("Statistics.AverageUser")+" E[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getMean())+"\n");
				builder.results.append(Language.tr("Statistics.StdDevUser")+" Std[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getSD())+"\n");
				builder.results.append(Language.tr("Statistics.VarianceUser")+" Var[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getVar())+"\n");
				builder.results.append(Language.tr("Statistics.CVUser")+" CV[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getCV())+"\n");
				builder.results.append(Language.tr("Statistics.Skewness")+" Sk[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getSk())+"\n");
				builder.results.append(Language.tr("Statistics.Kurt")+" Kurt[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getKurt())+"\n");
				builder.results.append(Language.tr("Statistics.MinimumUser")+" Min[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getMin())+"\n");
				builder.results.append(Language.tr("Statistics.MaximumUser")+" Max[X]="+NumberTools.formatNumber(indicatorsDiscrete[i].getMax())+"\n");
			}
		}

		/* Kontinuierlich */

		keys=((RunElementUserStatisticData)builder.data).getKeysContinuous();
		isTime=((RunElementUserStatisticData)builder.data).getIsTimeContinuous();
		final StatisticsTimeContinuousPerformanceIndicator[] indicatorsContinuous=((RunElementUserStatisticData)builder.data).getIndicatorsContinuous();

		for (int i=0;i<keys.length;i++) if (indicatorsContinuous[i]!=null) {
			builder.results.append("\n"+keys[i]+":\n");
			if (isTime[i]) {
				builder.results.append(Language.tr("Statistics.AverageUserTime")+" E[X]="+TimeTools.formatExactTime(indicatorsContinuous[i].getTimeMean())+" ("+NumberTools.formatNumber(indicatorsContinuous[i].getTimeMean())+")\n");
				builder.results.append(Language.tr("Statistics.StdDevUserTime")+" Std[X]="+TimeTools.formatExactTime(indicatorsContinuous[i].getTimeSD())+" ("+NumberTools.formatNumber(indicatorsContinuous[i].getTimeSD())+")\n");
				builder.results.append(Language.tr("Statistics.VarianceUserTime")+" Var[X]="+TimeTools.formatExactTime(indicatorsContinuous[i].getTimeVar())+" ("+NumberTools.formatNumber(indicatorsContinuous[i].getTimeVar())+")\n");
				builder.results.append(Language.tr("Statistics.CVUserTime")+" CV[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeCV())+"\n");
				builder.results.append(Language.tr("Statistics.Skewness")+" Sk[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeSk())+"\n");
				builder.results.append(Language.tr("Statistics.Kurt")+" Kurt[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeKurt())+"\n");
				builder.results.append(Language.tr("Statistics.MinimumUserTime")+" Min[X]="+TimeTools.formatExactTime(indicatorsContinuous[i].getTimeMin())+" ("+NumberTools.formatNumber(indicatorsContinuous[i].getTimeMin())+")\n");
				builder.results.append(Language.tr("Statistics.MaximumUserTime")+" Max[X]="+TimeTools.formatExactTime(indicatorsContinuous[i].getTimeMax())+" ("+NumberTools.formatNumber(indicatorsContinuous[i].getTimeMax())+")\n");
			} else {
				builder.results.append(Language.tr("Statistics.AverageUser")+" E[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeMean())+"\n");
				builder.results.append(Language.tr("Statistics.StdDevUser")+" Std[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeSD())+"\n");
				builder.results.append(Language.tr("Statistics.VarianceUser")+" Var[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeVar())+"\n");
				builder.results.append(Language.tr("Statistics.CVUser")+" CV[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeCV())+"\n");
				builder.results.append(Language.tr("Statistics.Skewness")+" Sk[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeSk())+"\n");
				builder.results.append(Language.tr("Statistics.Kurt")+" Kurt[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeKurt())+"\n");
				builder.results.append(Language.tr("Statistics.MinimumUser")+" Min[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeMin())+"\n");
				builder.results.append(Language.tr("Statistics.MaximumUser")+" Max[X]="+NumberTools.formatNumber(indicatorsContinuous[i].getTimeMax())+"\n");
			}
		}


	}

	@Override
	public String getHelpPageName() {
		return "ModelElementUserStatistic";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		for (int i=0;i<key.size();i++) {
			final StringBuilder expressionInfo=new StringBuilder();
			expressionInfo.append(String.format(Language.tr("ModelDescription.UserStatistic.Expression"),expression.get(i)));
			if (isContinuous.get(i)) expressionInfo.append(Language.tr("ModelDescription.UserStatistic.IsContinuous"));
			if (isTime.get(i)) expressionInfo.append(Language.tr("ModelDescription.UserStatistic.IsTime"));
			descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.UserStatistic.Record"),key.get(i)),expressionInfo.toString(),1000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<key.size();i++) {
			final int index=i;
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.Key"),key.get(index),newKey->key.set(index,newKey));
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.ExpressionForKey"),key.get(index)),expression.get(index),newExpression->expression.set(index,newExpression));
		}
	}
}