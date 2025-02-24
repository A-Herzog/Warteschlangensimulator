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
import simulator.elements.RunElementCounterBatchData;
import simulator.runmodel.RunModelFixer;
import statistics.StatisticsDataPerformanceIndicator;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.statistics.StatisticTools;

/**
 * Z�hlt f�r die Statistik wie viele Batche das Element durchquert haben
 * @author Alexander Herzog
 */
public class ModelElementCounterBatch extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener {
	/**
	 * Optionale Bedingungen f�r die Ausl�sung des Z�hlers
	 * @see #getCondition()
	 */
	private final CounterCondition counterCondition;

	/**
	 * Konstruktor der Klasse {@link ModelElementCounterBatch}
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementCounterBatch(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE_123);
		counterCondition=new CounterCondition();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_COUNTER_BATCH.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.CounterBatch.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementCounterBatch)) return false;
		final ModelElementCounterBatch otherCounter=(ModelElementCounterBatch)element;

		if (!otherCounter.counterCondition.equalsCounterCondition(counterCondition)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementCounterBatch) {
			final ModelElementCounterBatch copySource=(ModelElementCounterBatch)element;
			counterCondition.copyFrom(copySource.counterCondition);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementCounterBatch clone(final EditModel model, final ModelSurface surface) {
		final ModelElementCounterBatch element=new ModelElementCounterBatch(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.CounterBatch.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.CounterBatch.Name.Short");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(230,230,230);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementCounterBatchDialog(owner,ModelElementCounterBatch.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt stations-bedingte zus�tzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		if (((RunElementCounterBatchData)builder.data).indicators==null) return;
		StatisticsDataPerformanceIndicator[] indicators=((RunElementCounterBatchData)builder.data).indicators;

		for (int i=0;i<indicators.length;i++) {
			final StatisticsDataPerformanceIndicator indicator=indicators[i];
			if (indicator==null) continue;
			if (indicator.getCount()==0) continue;
			builder.results.append("\n");
			if (i==0) builder.results.append(Language.tr("Surface.CounterBatch.SimInfo.BatchGeneral")+":\n"); else builder.results.append(String.format(Language.tr("Surface.CounterBatch.SimInfo.BatchSize"),i)+":\n");
			builder.results.append(Language.tr("Statistics.AverageInterArrivalCount")+": "+NumberTools.formatLong(indicator.getCount())+"\n");
			builder.results.append(Language.tr("Statistics.AverageInterArrivalTime")+": E[IB]="+TimeTools.formatExactTime(indicator.getMean())+" ("+StatisticTools.formatNumberExt(indicator.getMean(),false)+"\n");
			builder.results.append(Language.tr("Statistics.StdDevInterArrivalTime")+": Std[IB]="+TimeTools.formatExactTime(indicator.getSD())+" ("+StatisticTools.formatNumberExt(indicator.getSD(),false)+"\n");
			builder.results.append(Language.tr("Statistics.VarianceInterArrivalTime")+": Var[IB]="+TimeTools.formatExactTime(indicator.getVar())+"\n");
			builder.results.append(Language.tr("Statistics.CVInterArrivalTime")+": CV[IB]"+StatisticTools.formatNumberExt(indicator.getCV(),true)+"\n");
			builder.results.append(Language.tr("Statistics.Skewness")+": Sk[IB]"+StatisticTools.formatNumberExt(indicator.getSk(),true)+"\n");
			builder.results.append(Language.tr("Statistics.Kurt")+": Kurt[IB]"+StatisticTools.formatNumberExt(indicator.getKurt(),true)+"\n");
			builder.results.append(Language.tr("Statistics.MinimalInterArrivalTime")+": Min[IB]="+TimeTools.formatExactTime(indicator.getMin())+" ("+StatisticTools.formatNumberExt(indicator.getMin(),false)+"\n");
			builder.results.append(Language.tr("Statistics.MaximalInterArrivalTime")+": Max[IB]="+TimeTools.formatExactTime(indicator.getMax())+" ("+StatisticTools.formatNumberExt(indicator.getMax(),false)+"\n");
		}
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param surfacePanel	Zeichenfl�che
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.CounterBatch.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		counterCondition.saveToXML(node);
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (counterCondition.isCounterConditionElement(node)) {
			counterCondition.loadFromXML(node);
			return null;
		}

		return null;
	}

	/**
	 * Liefert die optionalen Bedingungen f�r die Ausl�sung des Z�hlers
	 * @return	Optionale Bedingungen f�r die Ausl�sung des Z�hlers
	 */
	public CounterCondition getCondition() {
		return counterCondition;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementCounterBatch";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		counterCondition.buildDescription(descriptionBuilder,1000);
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.hold,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		searcher.testString(this,Language.tr("Surface.CounterBatch.Dialog.Condition"),counterCondition.getCondition(),newCondition->counterCondition.setCondition(newCondition));
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		counterCondition.clientTypeRenamed(oldName,newName);
	}
}