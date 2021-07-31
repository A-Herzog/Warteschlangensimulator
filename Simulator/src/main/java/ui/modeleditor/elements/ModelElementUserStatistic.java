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
 */
public class ModelElementUserStatistic extends ModelElementMultiInSingleOutBox {

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
	 * Konstruktor der Klasse <code>ModelElementUserStatistic</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementUserStatistic(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		key=new ArrayList<>();
		isTime=new ArrayList<>();
		expression=new ArrayList<>();
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
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementUserStatistic)) return false;

		if (key.size()!=((ModelElementUserStatistic)element).key.size()) return false;
		if (isTime.size()!=((ModelElementUserStatistic)element).isTime.size()) return false;
		if (expression.size()!=((ModelElementUserStatistic)element).expression.size()) return false;
		for (int i=0;i<key.size();i++) if (!((ModelElementUserStatistic)element).key.get(i).equals(key.get(i))) return false;
		for (int i=0;i<isTime.size();i++) if (!((ModelElementUserStatistic)element).isTime.get(i).equals(isTime.get(i))) return false;
		for (int i=0;i<expression.size();i++) if (!((ModelElementUserStatistic)element).expression.get(i).equals(expression.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementUserStatistic) {
			key.addAll(((ModelElementUserStatistic)element).key);
			isTime.addAll(((ModelElementUserStatistic)element).isTime);
			expression.addAll(((ModelElementUserStatistic)element).expression);
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

		for (int i=0;i<Math.min(Math.min(key.size(),expression.size()),isTime.size());i++) {
			final String k=key.get(i);
			final String e=expression.get(i);
			if (k==null || k.trim().isEmpty() || e==null || e.trim().isEmpty()) continue;
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.UserStatistic.XML.Record")));
			sub.setAttribute(Language.trPrimary("Surface.UserStatistic.XML.Record.Key"),k);
			sub.setAttribute(Language.trPrimary("Surface.UserStatistic.XML.Record.IsTime"),isTime.get(i)?"1":"0");
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

		if (Language.trAll("Surface.UserStatistic.XML.Record",name)) {
			final String k=Language.trAllAttribute("Surface.UserStatistic.XML.Record.Key",node);
			final String t=Language.trAllAttribute("Surface.UserStatistic.XML.Record.IsTime",node);
			final String e=content;
			if (!k.trim().isEmpty() && !e.trim().isEmpty()) {
				key.add(k.trim());
				isTime.add(t==null || t.isEmpty() || !t.equals("0"));
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
		final String[] keys=((RunElementUserStatisticData)builder.data).getKeys();
		final boolean[] isTime=((RunElementUserStatisticData)builder.data).getIsTime();
		final StatisticsDataPerformanceIndicatorWithNegativeValues[] indicators=((RunElementUserStatisticData)builder.data).getIndicators();

		for (int i=0;i<keys.length;i++) if (indicators[i]!=null) {
			builder.results.append("\n"+keys[i]+":\n");
			builder.results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(indicators[i].getCount())+"\n");
			if (isTime[i]) {
				builder.results.append(Language.tr("Statistics.AverageUserTime")+" E[X]="+TimeTools.formatExactTime(indicators[i].getMean())+" ("+NumberTools.formatNumber(indicators[i].getMean())+")\n");
				builder.results.append(Language.tr("Statistics.StdDevUserTime")+" Std[X]="+TimeTools.formatExactTime(indicators[i].getSD())+" ("+NumberTools.formatNumber(indicators[i].getSD())+")\n");
				builder.results.append(Language.tr("Statistics.VarianceUserTime")+" Var[X]="+TimeTools.formatExactTime(indicators[i].getVar())+" ("+NumberTools.formatNumber(indicators[i].getVar())+")\n");
				builder.results.append(Language.tr("Statistics.CVUserTime")+" CV[X]="+NumberTools.formatNumber(indicators[i].getCV())+"\n");
				builder.results.append(Language.tr("Statistics.Skewness")+" Sk[X]="+NumberTools.formatNumber(indicators[i].getSk())+"\n");
				builder.results.append(Language.tr("Statistics.MinimumUserTime")+" Min[X]="+TimeTools.formatExactTime(indicators[i].getMin())+" ("+NumberTools.formatNumber(indicators[i].getMin())+")\n");
				builder.results.append(Language.tr("Statistics.MaximumUserTime")+" Max[X]="+TimeTools.formatExactTime(indicators[i].getMax())+" ("+NumberTools.formatNumber(indicators[i].getMax())+")\n");
			} else {
				builder.results.append(Language.tr("Statistics.AverageUser")+" E[X]="+NumberTools.formatNumber(indicators[i].getMean())+"\n");
				builder.results.append(Language.tr("Statistics.StdDevUser")+" Std[X]="+NumberTools.formatNumber(indicators[i].getSD())+"\n");
				builder.results.append(Language.tr("Statistics.VarianceUser")+" Var[X]="+NumberTools.formatNumber(indicators[i].getVar())+"\n");
				builder.results.append(Language.tr("Statistics.CVUser")+" CV[X]="+NumberTools.formatNumber(indicators[i].getCV())+"\n");
				builder.results.append(Language.tr("Statistics.Skewness")+" Sk[X]="+NumberTools.formatNumber(indicators[i].getSk())+"\n");
				builder.results.append(Language.tr("Statistics.MinimumUser")+" Min[X]="+NumberTools.formatNumber(indicators[i].getMin())+"\n");
				builder.results.append(Language.tr("Statistics.MaximumUser")+" Max[X]="+NumberTools.formatNumber(indicators[i].getMax())+"\n");
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
			final StringBuilder sb=new StringBuilder();
			sb.append(String.format(Language.tr("ModelDescription.UserStatistic.Expression"),expression.get(i)));
			if (isTime.get(i)) sb.append(Language.tr("ModelDescription.UserStatistic.IsTime"));
			descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.UserStatistic.Record"),key.get(i)),sb.toString(),1000);
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