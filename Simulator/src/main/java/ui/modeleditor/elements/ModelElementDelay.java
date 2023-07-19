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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.RunModelFixer;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateMode;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord;

/**
 * Verz�gerung der Kunden
 * @author Alexander Herzog
 */
public class ModelElementDelay extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener, ModelElementAnimationForceMove {
	/**
	 * Art wie die Verz�gerung f�r die Kundenstatistik gez�hlt werden soll
	 * @author Alexander Herzog
	 * @see ModelElementDelay#getDelayType()
	 * @see ModelElementDelay#setDelayType(DelayType)
	 */
	public enum DelayType {
		/** Die Verz�gerung soll als Wartezeit gez�hlt werden. */
		DELAY_TYPE_WAITING,

		/** Die Verz�gerung soll als Transferzeit gez�hlt werden. */
		DELAY_TYPE_TRANSFER,

		/** Die Verz�gerung soll als Bedienzeit gez�hlt werden. */
		DELAY_TYPE_PROCESS,

		/** Die Verz�gerung soll nicht erfasst werden. */
		DELAY_TYPE_NOTHING
	}

	/**
	 * Verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Art wie die Verz�gerung f�r die Kundenstatistik gez�hlt werden soll
	 * @see #getDelayType()
	 * @see #setDelayType(DelayType)
	 */
	private DelayType delayType;

	/**
	 * Verteilung der Verz�gerungszeiten, die im allgemeinen Fall gelten soll
	 * @see #getDelayTime()
	 * @see #setDelayTime(String, AbstractRealDistribution, String)
	 */
	private AbstractRealDistribution distributionGlobal;

	/**
	 * Ausdruck zur Bestimmung der Wartezeiten oder <code>null</code> wenn eine Verteilung und kein Ausdruck verwendet werden soll
	 * @see #getDelayExpression()
	 * @see #setDelayTime(AbstractRealDistribution, String)
	 */
	private String expressionGlobal;

	/**
	 * Verteilungen der Verz�gerungszeiten pro Kundentyp (optional)
	 * @see #getDelayTime(String)
	 * @see #setDelayTime(String, AbstractRealDistribution, String)
	 */
	private Map<String,AbstractRealDistribution> distributionByType;

	/**
	 * Ausdruck zur Bestimmung der Verz�gerungszeiten pro Kundentyp (optional)
	 * @see #getDelayExpression(String)
	 * @see #setDelayTime(String, AbstractRealDistribution, String)
	 */
	private Map<String,String> expressionByType;

	/**
	 * Kosten pro Bedienvorgang
	 * @see #getCosts()
	 * @see #setCosts(String)
	 */
	private String costs;

	/**
	 * Soll eine Liste der Kunden an der Station gef�hrt werden?
	 * @see #hasClientsList()
	 * @see #setHasClientsList(boolean)
	 */
	private boolean hasClientsList;

	/**
	 * Konstruktor der Klasse <code>ModelElementDelay</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementDelay(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE_DOUBLE_LINE);
		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		delayType=DelayType.DELAY_TYPE_PROCESS;
		distributionGlobal=new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		expressionGlobal=null;
		distributionByType=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		expressionByType=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		costs="";
		hasClientsList=false;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DELAY.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Delay.Tooltip");
	}

	/**
	 * Liefert die Verteilung der Wartezeiten, die im allgemeinen Fall gelten soll
	 * @return Verteilung der Wartezeiten oder <code>null</code> wenn ein Ausdruck und keine Verteilung verwendet werden soll
	 */
	public AbstractRealDistribution getDelayTime() {
		if (expressionGlobal==null && distributionGlobal==null) distributionGlobal=new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		return distributionGlobal;
	}

	/**
	 * Liefert den Ausdruck, �ber den die Verz�gerung, die im allgemeinen Fall gelten soll, bestimmt werden soll
	 * @return Ausdruck zur Bestimmung der Wartezeiten oder <code>null</code> wenn eine Verteilung und kein Ausdruck verwendet werden soll
	 */
	public String getDelayExpression() {
		return expressionGlobal;
	}

	/**
	 * Liefert die Anzahl an Kundentypen, f�r die individuelle Verz�gerungsdaten vorliegen
	 * @return	Anzahl an Kundentypen, f�r die individuelle Verz�gerungsdaten vorliegen
	 */
	public int getSubDataCount() {
		return distributionByType.size()+expressionByType.size();
	}

	/**
	 * Liefert die Verteilung der Wartezeiten f�r einen bestimmten Kundentyp
	 * @param clientType	Kundentyp f�r den die Verteilung der Wartezeiten geliefert werden soll
	 * @return Verteilung der Wartezeiten oder <code>null</code>, wenn f�r diesen Kundentyp keine Verteilung hinterlegt ist oder ein Ausdruck statt einer Verteilung verwendet werden soll
	 */
	public AbstractRealDistribution getDelayTime(final String clientType) {
		return distributionByType.get(clientType);
	}

	/**
	 * Liefert den Ausdruck, �ber den die Verz�gerung f�r einen bestimmten Kundentyp bestimmt werden soll
	 * @param clientType	Kundentyp f�r den der Ausdruck zur Bestimmung der Wartezeiten geliefert werden soll
	 * @return Ausdruck zur Bestimmung der Wartezeiten oder <code>null</code>, wenn f�r diesen Kundentyp kein Ausdruck hinterlegt ist oder eine Verteilung statt eines Ausdrucks verwendet werden soll
	 */
	public String getDelayExpression(final String clientType) {
		return expressionByType.get(clientType);
	}

	/**
	 * Stellt die Verteilung der Wartezeiten oder den Ausdruck zur Bestimmung der Wartezeiten f�r den allgemeinen Fall ein.
	 * @param distribution	Neue Verteilung der Wartezeiten (genau einer der beiden Verteilung oder Ausdruck muss ungleich <code>null</code> sein)
	 * @param expression	Neuer Ausdruck zur Bestimmung der Wartezeiten (genau einer der beiden Verteilung oder Ausdruck muss ungleich <code>null</code> sein)
	 */
	public void setDelayTime(final AbstractRealDistribution distribution, final String expression) {
		if (distribution!=null) {
			distributionGlobal=DistributionTools.cloneDistribution(distribution);
			expressionGlobal=null;
		} else {
			if (expression!=null) {
				distributionGlobal=null;
				expressionGlobal=expression;
			}
		}
	}

	/**
	 * Stellt die Verteilung der Wartezeiten oder den Ausdruck zur Bestimmung der Wartezeiten f�r einen bestimmten Kundentyp ein.
	 * @param clientType	Kundentyp f�r den die Verteilung bzw. der Audruck der Wartezeiten eingestellt werden soll
	 * @param distribution	Neue Verteilung der Wartezeiten oder <code>null</code>, wenn f�r den Kundentyp keine individuelle Verteilung definiert werden soll oder statt dessen ein Ausdruck verwendet werden soll
	 * @param expression	Neuer Ausdruck zur Bestimmung der Wartezeiten oder <code>null</code>, wenn f�r den Kundentyp kein individueller Ausdruck definiert werden soll oder statt dessen eine Verteilung verwendet werden soll
	 */
	public void setDelayTime(final String clientType, final AbstractRealDistribution distribution, final String expression) {
		if (clientType==null || clientType.trim().isEmpty()) return;
		if (distribution==null) {
			distributionByType.remove(clientType);
			if (expression!=null) expressionByType.put(clientType,expression); else expressionByType.remove(clientType);
		} else {
			distributionByType.put(clientType,distribution);
			expressionByType.remove(clientType);
		}
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=timeBase;
	}

	/**
	 * Gibt an, ob die Prozesszeiten als Bedienzeiten, Transferzeiten oder als Wartezeiten gez�hlt werden sollen.
	 * @return	Gibt den Typ der Verz�gerung zur�ck.
	 */
	public DelayType getDelayType() {
		return delayType;
	}

	/**
	 * Stellt ein, ob die Prozesszeiten als Bedienzeiten, Transferzeiten oder als Wartezeiten gez�hlt werden sollen.
	 * @param delayType	Art der Verz�gerung
	 */
	public void setDelayType(final DelayType delayType) {
		this.delayType=delayType;
	}

	/**
	 * Liefert die eingestellten Kosten pro Bedienvorgang in der Station
	 * @return	Kosten pro Bedienvorgang
	 */
	public String getCosts() {
		return (costs==null)?"":costs;
	}

	/**
	 * Stellt die Kosten pro Bedienvorgang in der Station ein
	 * @param costs	Kosten pro Bedienvorgang
	 */
	public void setCosts(final String costs) {
		this.costs=(costs==null)?"":costs;
	}

	/**
	 * Soll eine Liste der Kunden an der Station gef�hrt werden?
	 * @return	Soll eine Liste der Kunden an der Station gef�hrt werden?
	 */
	public boolean hasClientsList() {
		return hasClientsList;
	}

	/**
	 * Stellt ein, ob an der Station eine Liste der Kunden gef�hrt werden soll.
	 * @param hasClientsList	Soll eine Liste der Kunden an der Station gef�hrt werden?
	 */
	public void setHasClientsList(final boolean hasClientsList) {
		this.hasClientsList=hasClientsList;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDelay)) return false;
		final ModelElementDelay otherDelay=(ModelElementDelay)element;

		if (otherDelay.timeBase!=timeBase) return false;
		if (otherDelay.delayType!=delayType) return false;

		if (distributionGlobal!=null) {
			if (otherDelay.distributionGlobal==null) return false;
			if (!DistributionTools.compare(distributionGlobal,otherDelay.distributionGlobal)) return false;
		} else {
			if (expressionGlobal!=null) {
				if (otherDelay.expressionGlobal==null) return false;
				if (!expressionGlobal.equals(otherDelay.expressionGlobal)) return false;
			}
		}

		final Map<String,AbstractRealDistribution> mapA=distributionByType;
		final Map<String,AbstractRealDistribution> mapB=otherDelay.distributionByType;

		for (Map.Entry<String,AbstractRealDistribution> entry : mapA.entrySet()) {
			if (!DistributionTools.compare(entry.getValue(),mapB.get(entry.getKey()))) return false;
		}

		for (Map.Entry<String,AbstractRealDistribution> entry : mapB.entrySet()) {
			if (!DistributionTools.compare(entry.getValue(),mapA.get(entry.getKey()))) return false;
		}

		final Map<String,String> mapC=expressionByType;
		final Map<String,String> mapD=otherDelay.expressionByType;

		for (Map.Entry<String,String> entry : mapC.entrySet()) {
			final String c=entry.getValue();
			final String d=mapD.get(entry.getKey());
			if (c!=null && d!=null) {
				if (!c.equals(d)) return false;
			} else {
				if (c!=null || d!=null) return false;
			}
		}

		for (Map.Entry<String,String> entry : mapD.entrySet()) {
			final String c=entry.getValue();
			final String d=mapC.get(entry.getKey());
			if (c!=null && d!=null) {
				if (!c.equals(d)) return false;
			} else {
				if (c!=null || d!=null) return false;
			}
		}

		if (!Objects.equals(costs,otherDelay.costs)) return false;

		if (hasClientsList!=otherDelay.hasClientsList) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDelay) {
			final ModelElementDelay copySource=(ModelElementDelay)element;

			timeBase=copySource.timeBase;
			delayType=copySource.delayType;

			distributionGlobal=null;
			if (copySource.distributionGlobal!=null) distributionGlobal=DistributionTools.cloneDistribution(copySource.distributionGlobal);
			expressionGlobal=null;
			if (copySource.expressionGlobal!=null) expressionGlobal=copySource.expressionGlobal;

			distributionByType.clear();
			for (Map.Entry<String,AbstractRealDistribution> entry : copySource.distributionByType.entrySet()) {
				final AbstractRealDistribution dist=entry.getValue();
				if (dist!=null) distributionByType.put(entry.getKey(),DistributionTools.cloneDistribution(dist));
			}

			expressionByType.clear();
			for (Map.Entry<String,String> entry : copySource.expressionByType.entrySet()) {
				final String expression=entry.getValue();
				if (expression!=null) expressionByType.put(entry.getKey(),expression);
			}

			costs=copySource.costs;

			hasClientsList=copySource.hasClientsList;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDelay clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDelay element=new ModelElementDelay(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Delay.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Delay.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,225,255);

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
			new ModelElementDelayDialog(owner,ModelElementDelay.this,readOnly);
		};
	}

	/**
	 * Erstellte optional weitere Men�punkte (in Form von Panels),
	 * die das direkte Bearbeiten von Einstellungen aus dem
	 * Kontextmen� heraus erlauben.
	 * @return	Array mit Panels (Array kann leer oder <code>null</code> sein; auch Eintr�ge d�rfen <code>null</code> sein)
	 */
	@Override
	protected JPanel[] addCustomSettingsToContextMenu() {
		final List<JPanel> panels=new ArrayList<>();

		if (distributionGlobal!=null && distributionByType.size()==0 && expressionByType.size()==0) {
			final Consumer<AbstractRealDistribution> distributionChanger=newDistribution->{distributionGlobal=newDistribution;};
			panels.add(createContextMenuSliderDistributionMean(Language.tr("Surface.Delay.AverageDelayTime"),timeBase,distributionGlobal,300,distributionChanger));
		}

		return panels.toArray(new JPanel[0]);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.CHART_WIP);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.HISTOGRAM_WIP);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsProcessing(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optionale Men�punkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmen� ein
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	@Override
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
		final AbstractRealDistribution dist=getDelayTime();
		if (dist==null) return;

		JMenuItem item;
		final Icon icon=Images.PARAMETERSERIES.getIcon();

		if (DistributionTools.canSetMean(dist)) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeDelayTime")));
			item.addActionListener(e->{
				final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_DELAY,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeDelayTime.Short"));
				record.input.setMode(ModelChanger.Mode.MODE_XML);
				record.input.setXMLMode(1);
				String add="";
				if (getSubDataCount()>0) add="[1]";
				record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
				buildSeries.accept(record);
			});
			if (icon!=null) item.setIcon(icon);
		}

		if (DistributionTools.canSetStandardDeviationExactIndependent(dist)) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeDelayTimeStd")));
			item.addActionListener(e->{
				final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_DELAY,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeDelayTimeStd.Short"));
				record.input.setMode(ModelChanger.Mode.MODE_XML);
				record.input.setXMLMode(2);
				String add="";
				if (getSubDataCount()>0) add="[1]";
				record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
				buildSeries.accept(record);
			});
			if (icon!=null) item.setIcon(icon);
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
		return Language.trAll("Surface.Delay.XML.Root");
	}

	/**
	 * F�gt die Attribute (Zeitbasis, Art der Z�hlung der Verz�gerung) zu dem globalen
	 * Verz�gerungs-XML-Element hinzu
	 * @param sub	XML-Element
	 * @see #addPropertiesDataToXML(Document, Element)
	 */
	private void addAttributesToGlobalElement(final Element sub) {
		sub.setAttribute(Language.trPrimary("Surface.Delay.XML.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			sub.setAttribute(Language.trPrimary("Surface.Delay.XML.Type"),Language.trPrimary("Surface.Delay.XML.Type.WaitingTime"));
			break;
		case DELAY_TYPE_TRANSFER:
			sub.setAttribute(Language.trPrimary("Surface.Delay.XML.Type"),Language.trPrimary("Surface.Delay.XML.Type.TransferTime"));
			break;
		case DELAY_TYPE_PROCESS:
			sub.setAttribute(Language.trPrimary("Surface.Delay.XML.Type"),Language.trPrimary("Surface.Delay.XML.Type.ProcessTime"));
			break;
		case DELAY_TYPE_NOTHING:
			sub.setAttribute(Language.trPrimary("Surface.Delay.XML.Type"),Language.trPrimary("Surface.Delay.XML.Type.Nothing"));
			break;
		}
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (hasClientsList) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Delay.XML.ClientsList")));
			sub.setTextContent("1");
		}

		if (distributionGlobal!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Delay.XML.Distribution")));
			sub.setTextContent(DistributionTools.distributionToString(distributionGlobal));
			addAttributesToGlobalElement(sub);
		}

		if (expressionGlobal!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Delay.XML.Expression")));
			sub.setTextContent(expressionGlobal);
			addAttributesToGlobalElement(sub);
		}

		for (Map.Entry<String,AbstractRealDistribution> entry : distributionByType.entrySet()) if (entry.getValue()!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Delay.XML.Distribution")));
			sub.setAttribute(Language.trPrimary("Surface.Delay.XML.Distribution.ClientType"),entry.getKey());
			sub.setTextContent(DistributionTools.distributionToString(entry.getValue()));
		}

		for (Map.Entry<String,String> entry : expressionByType.entrySet()) if (entry.getValue()!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Delay.XML.Expression")));
			sub.setAttribute(Language.trPrimary("Surface.Delay.XML.Expression.ClientType"),entry.getKey());
			sub.setTextContent(entry.getValue());
		}

		if (costs!=null && !costs.trim().isEmpty() && !costs.trim().equals("0")) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Delay.XML.Costs")));
			sub.setTextContent(costs);
		}
	}

	/**
	 * L�dt die Attribute (Zeitbasis, Art der Z�hlung der Verz�gerung) aus dem globalen
	 * Verz�gerungs-XML-Element
	 * @param node	XML-Element
	 */
	private void loadGlobalProperties(final Element node) {
		final String timeBaseName=Language.trAllAttribute("Surface.Delay.XML.TimeBase",node);
		timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
		final String type=Language.trAllAttribute("Surface.Delay.XML.Type",node);
		if (Language.trAll("Surface.Delay.XML.Type.WaitingTime",type)) delayType=DelayType.DELAY_TYPE_WAITING;
		if (Language.trAll("Surface.Delay.XML.Type.TransferTime",type)) delayType=DelayType.DELAY_TYPE_TRANSFER;
		if (Language.trAll("Surface.Delay.XML.Type.ProcessTime",type)) delayType=DelayType.DELAY_TYPE_PROCESS;
		if (Language.trAll("Surface.Delay.XML.Type.Nothing",type)) delayType=DelayType.DELAY_TYPE_NOTHING;
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

		if (Language.trAll("Surface.Delay.XML.ClientsList",name)) {
			hasClientsList=content.trim().equals("1");
			return null;
		}

		if (Language.trAll("Surface.Delay.XML.Distribution",name)) {
			final String typ=Language.trAllAttribute("Surface.Delay.XML.Distribution.ClientType",node);
			final AbstractRealDistribution dist=DistributionTools.distributionFromString(content,3000);
			if (dist==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			if (typ==null || typ.trim().isEmpty()) {
				distributionGlobal=dist;
				expressionGlobal=null;
				loadGlobalProperties(node);
			} else {
				distributionByType.put(typ,dist);
			}
			return null;
		}

		if (Language.trAll("Surface.Delay.XML.Expression",name)) {
			final String typ=Language.trAllAttribute("Surface.Delay.XML.Expression.ClientType",node);
			final String expression=content;
			if (typ==null || typ.trim().isEmpty()) {
				expressionGlobal=expression;
				distributionGlobal=null;
				loadGlobalProperties(node);
			} else {
				expressionByType.put(typ,expression);
			}
			return null;
		}

		if (Language.trAll("Surface.Delay.XML.Costs",name)) {
			costs=content;
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zus�tzlich auch die aktuelle Anzahl an Kunden an der Station
	 * w�hrend der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	@Override
	public boolean showFullAnimationRunData() {
		return true;
	}

	@Override
	public boolean hasQueue() {
		return true;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		/* Keine Kopie anlegen, wenn es bereits Daten f�r den neuen Namen gibt. */
		if (getDelayTime(newName)!=null) return;
		if (getDelayExpression(newName)!=null) return;

		/* Daten �bertragen */
		final AbstractRealDistribution dist=getDelayTime(oldName);
		final String expression=getDelayExpression(oldName);
		if (dist!=null || expression!=null) setDelayTime(newName,dist,expression);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDelay";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Verz�gerung erfassen als ... */
		switch (delayType) {
		case DELAY_TYPE_WAITING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Waiting"),1000);
			break;
		case DELAY_TYPE_TRANSFER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Transfer"),1000);
			break;
		case DELAY_TYPE_PROCESS:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Process"),1000);
			break;
		case DELAY_TYPE_NOTHING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Mode"),Language.tr("ModelDescription.Delay.Mode.Nothing"),1000);
			break;
		}

		/* Daten pro Kundentyp */
		for (String clientType: descriptionBuilder.getClientTypes()) {
			final AbstractRealDistribution distribution=distributionByType.get(clientType);
			if (distribution!=null) {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Delay.Distribution.ClientType"),clientType),ModelDescriptionBuilder.getDistributionInfo(distribution),2000);
			} else {
				final String expression=expressionByType.get(clientType);
				if (expression!=null) {
					descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Delay.Expression.ClientType"),clientType),expression,2000);
				}
			}
		}

		/* Globale Daten */
		if (distributionGlobal!=null) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Distribution"),ModelDescriptionBuilder.getDistributionInfo(distributionGlobal),3000);
		} else {
			if (expressionGlobal!=null) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.Expression"),expressionGlobal,3000);
			}
		}

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,5000);

		/* Kosten an der Station */
		if (costs!=null && !costs.trim().isEmpty() && !costs.trim().equals("0")) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Delay.StationCostsPerClient"),costs,1000);
		}
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.process,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Globale Werte */
		if (expressionGlobal!=null) {
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.GlobalExpression"),expressionGlobal,newExpressionGlobal->{expressionGlobal=newExpressionGlobal;});
		} else {
			searcher.testDistribution(this,Language.tr("Editor.DialogBase.Search.GlobalDistribution"),distributionGlobal);
		}

		/* Kundentypspezifische Werte */
		for (Map.Entry<String,String> expression: expressionByType.entrySet()) {
			final String clientType=expression.getKey();
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.ClientTypeExpression"),clientType),expression.getValue(),newExpression->expressionByType.put(clientType,newExpression));
		}
		for (Map.Entry<String,AbstractRealDistribution> distribution: distributionByType.entrySet()) {
			final String clientType=distribution.getKey();
			if (expressionByType.get(clientType)!=null) continue;
			searcher.testDistribution(this,String.format(Language.tr("Editor.DialogBase.Search.ClientTypeDistribution"),clientType),distribution.getValue());
		}

		/* Kosten */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Costs"),costs,newCosts->{costs=newCosts;});
	}
}