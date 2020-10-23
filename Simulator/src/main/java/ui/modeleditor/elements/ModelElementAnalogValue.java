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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementAnalogValueData;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Analoger Wert mit �nderungsrate
 * @author Alexander Herzog
 */
public class ModelElementAnalogValue extends ModelElementBox {
	private double initialValue;
	private double valueMin;
	private double valueMax;
	private boolean valueMinUse;
	private boolean valueMaxUse;
	private double changeRatePerSecond;
	private double analogNotify;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnalogValue</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnalogValue(final EditModel model, ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		initialValue=0.0;
		valueMin=-1000.0;
		valueMax=1000.0;
		valueMinUse=false;
		valueMaxUse=false;
		changeRatePerSecond=0.0;
		analogNotify=60.0;
		shape.setPartialFillColors(null,new Color(230,230,230));
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANALOG_VALUE.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnalogValue.Tooltip");
	}

	/**
	 * Liefert den aktuellen Initialwert.
	 * @return	Aktueller Initialwert
	 * @see ModelElementAnalogValue#setInitialValue(double)
	 */
	public double getInitialValue() {
		return initialValue;
	}

	/**
	 * Stellt den Initialwert ein.
	 * @param initialValue	Neuer Initialwert
	 * @see ModelElementAnalogValue#getInitialValue()
	 */
	public void setInitialValue(final double initialValue) {
		this.initialValue=initialValue;
	}

	/**
	 * Liefert den minimalen Wert, den der analoge Wert annehmen kann
	 * @return	Minimalen Wert, den der analoge Wert annehmen kann
	 * @see ModelElementAnalogValue#setValueMin(double)
	 * @see ModelElementAnalogValue#isValueMinUse()
	 * @see ModelElementAnalogValue#setValueMinUse(boolean)
	 */
	public double getValueMin() {
		return valueMin;
	}

	/**
	 * Stellt den minimalen Wert, den der analoge Wert annehmen kann, ein.
	 * @param valueMin	Minimalen Wert, den der analoge Wert annehmen kann
	 * @see ModelElementAnalogValue#getValueMin()
	 * @see ModelElementAnalogValue#isValueMinUse()
	 * @see ModelElementAnalogValue#setValueMinUse(boolean)
	 */
	public void setValueMin(final double valueMin) {
		this.valueMin=valueMin;
	}

	/**
	 * Liefert den maximalen Wert, den der analoge Wert annehmen kann
	 * @return	Maximalen Wert, den der analoge Wert annehmen kann
	 * @see ModelElementAnalogValue#setValueMax(double)
	 * @see ModelElementAnalogValue#isValueMaxUse()
	 * @see ModelElementAnalogValue#setValueMaxUse(boolean)
	 */
	public double getValueMax() {
		return valueMax;
	}

	/**
	 * Stellt den maximalen Wert, den der analoge Wert annehmen kann, ein.
	 * @param valueMax	Maximalen Wert, den der analoge Wert annehmen kann
	 * @see ModelElementAnalogValue#getValueMax()
	 * @see ModelElementAnalogValue#isValueMaxUse()
	 * @see ModelElementAnalogValue#setValueMaxUse(boolean)
	 */
	public void setValueMax(final double valueMax) {
		this.valueMax=valueMax;
	}

	/**
	 * Gibt an, ob der Minimalwert verwendet werden soll.
	 * @return	Liefert <code>true</code>, wenn der Minimalwert verwendet werden soll
	 * @see ModelElementAnalogValue#getValueMin()
	 * @see ModelElementAnalogValue#setValueMin(double)
	 * @see ModelElementAnalogValue#setValueMinUse(boolean)
	 */
	public boolean isValueMinUse() {
		return valueMinUse;
	}

	/**
	 * Stellt ein, ob der Minimalwert verwendet werden soll.
	 * @param valueMinUse	Angabe, ob der Minimalwert verwendet werden soll
	 * @see ModelElementAnalogValue#getValueMin()
	 * @see ModelElementAnalogValue#setValueMin(double)
	 * @see ModelElementAnalogValue#isValueMinUse()
	 */
	public void setValueMinUse(final boolean valueMinUse) {
		this.valueMinUse=valueMinUse;
	}

	/**
	 * Gibt an, ob der Maximalwert verwendet werden soll.
	 * @return	Liefert <code>true</code>, wenn der Maximalwert verwendet werden soll
	 * @see ModelElementAnalogValue#getValueMax()
	 * @see ModelElementAnalogValue#setValueMax(double)
	 * @see ModelElementAnalogValue#setValueMaxUse(boolean)
	 */
	public boolean isValueMaxUse() {
		return valueMaxUse;
	}

	/**
	 * Stellt ein, ob der Maximalwert verwendet werden soll.
	 * @param valueMaxUse	Angabe, ob der Maximalwert verwendet werden soll
	 * @see ModelElementAnalogValue#getValueMax()
	 * @see ModelElementAnalogValue#setValueMax(double)
	 * @see ModelElementAnalogValue#isValueMaxUse()
	 */
	public void setValueMaxUse(final boolean valueMaxUse) {
		this.valueMaxUse=valueMaxUse;
	}

	/**
	 * Liefert die �nderungsrate (bezogen auf die Zeiteinheit Sekunde).
	 * @return	�nderungsrate (bezogen auf die Zeiteinheit Sekunde)
	 * @see ModelElementAnalogValue#setChangeRatePerSecond(double)
	 */
	public double getChangeRatePerSecond() {
		return changeRatePerSecond;
	}

	/**
	 * Stellt die �nderungsrate (bezogen auf die Zeiteinheit Sekunde) ein.
	 * @param changeRatePerSecond	�nderungsrate (bezogen auf die Zeiteinheit Sekunde)
	 * @see ModelElementAnalogValue#getChangeRatePerSecond()
	 */
	public void setChangeRatePerSecond(final double changeRatePerSecond) {
		this.changeRatePerSecond=changeRatePerSecond;
	}

	/**
	 * Gibt an, wie h�ufig das System �ber die �nderung des Wertes benachrichtigt werden soll (in Sekunden).
	 * @return	System-Benachrichtigungsabstand (in Sekunden)
	 * @see ModelElementAnalogValue#setAnalogNotify(double)
	 */
	public double getAnalogNotify() {
		return analogNotify;
	}

	/**
	 * Stellt ein, wie h�ufig das System �ber die �nderung des Wertes benachrichtigt werden soll (in Sekunden).
	 * @param analogNotify	System-Benachrichtigungsabstand (in Sekunden)
	 * @see ModelElementAnalogValue#getAnalogNotify()
	 */
	public void setAnalogNotify(final double analogNotify) {
		this.analogNotify=(analogNotify>0)?analogNotify:1.0;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnalogValue)) return false;

		if (initialValue!=((ModelElementAnalogValue)element).initialValue) return false;
		if (valueMinUse!=((ModelElementAnalogValue)element).valueMinUse) return false;
		if (valueMinUse) {
			if (valueMin!=((ModelElementAnalogValue)element).valueMin) return false;
		}
		if (valueMaxUse!=((ModelElementAnalogValue)element).valueMaxUse) return false;
		if (valueMaxUse) {
			if (valueMax!=((ModelElementAnalogValue)element).valueMax) return false;
		}
		if (changeRatePerSecond!=((ModelElementAnalogValue)element).changeRatePerSecond) return false;
		if (analogNotify!=((ModelElementAnalogValue)element).analogNotify) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnalogValue) {
			initialValue=((ModelElementAnalogValue)element).initialValue;
			valueMin=((ModelElementAnalogValue)element).valueMin;
			valueMax=((ModelElementAnalogValue)element).valueMax;
			valueMinUse=((ModelElementAnalogValue)element).valueMinUse;
			valueMaxUse=((ModelElementAnalogValue)element).valueMaxUse;
			changeRatePerSecond=((ModelElementAnalogValue)element).changeRatePerSecond;
			analogNotify=((ModelElementAnalogValue)element).analogNotify;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnalogValue clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnalogValue element=new ModelElementAnalogValue(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnalogValue.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.AnalogValue.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=Color.RED;

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
			new ModelElementAnalogValueDialog(owner,ModelElementAnalogValue.this,readOnly);
			fireChanged();
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
		NextStationHelper.nextStationsAnalog(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt stations-bedingte zus�tzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		final RunElementAnalogValueData data=(RunElementAnalogValueData)builder.data;

		builder.results.append("\n"+Language.tr("Statistics.AnalogValue")+"\n");
		builder.results.append("\n"+Language.tr("Statistics.AnalogValue.Current")+": "+NumberTools.formatNumber(data.getValueNoUpdate())+"\n");
		builder.results.append("\n"+Language.tr("Statistics.AnalogValue.Rate")+": "+NumberTools.formatNumber(data.getRateNoUpdate(),5)+"\n");
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_ANALOG_RATE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.CHART_ANALOG_VALUE);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.ANALOG_VALUE);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.ANALOG_RATE);
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnalogValue.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.AnalogValue.InitialValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(initialValue));

		if (valueMinUse) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.AnalogValue.MinimalValue")));
			sub.setTextContent(NumberTools.formatSystemNumber(valueMin));
		}

		if (valueMaxUse) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.AnalogValue.MaximalValue")));
			sub.setTextContent(NumberTools.formatSystemNumber(valueMax));
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.AnalogValue.Rate")));
		sub.setTextContent(NumberTools.formatSystemNumber(changeRatePerSecond));

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.AnalogValue.UpdateStepWide")));
		sub.setTextContent(NumberTools.formatSystemNumber((analogNotify>0)?analogNotify:1));
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

		if (Language.trAll("Surface.XML.AnalogValue.InitialValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			initialValue=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.XML.AnalogValue.MinimalValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			valueMin=D.doubleValue();
			valueMinUse=true;
			return null;
		}

		if (Language.trAll("Surface.XML.AnalogValue.MaximalValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			valueMax=D.doubleValue();
			valueMaxUse=true;
			return null;
		}

		if (Language.trAll("Surface.XML.AnalogValue.Rate",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			changeRatePerSecond=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.XML.AnalogValue.UpdateStepWide",name)) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			analogNotify=D.doubleValue();
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station w�hrend der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return true;
	}

	private StringBuilder[] animationSB;
	private StringBuilder animationSBCache;
	private int animationSBNext;
	private double lastValue=0;
	private String valueName;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		final RunElementData runData=getRunData();
		if (runData==null || isPreview) return false;

		if (!(runData instanceof RunElementAnalogValueData)) return false;
		final RunElementAnalogValueData data=(RunElementAnalogValueData)runData;
		final double value=data.getValue(simData);

		if (animationSB==null) {
			animationSB=new StringBuilder[2];
			animationSB[0]=new StringBuilder(100);
			animationSB[1]=new StringBuilder(100);
			animationSBNext=0;
			valueName=Language.tr("Statistics.Value")+"=";
		} else {
			if (Math.abs(value-lastValue)<10E-5) return false;
		}
		lastValue=value;

		final StringBuilder sb=animationSB[animationSBNext];
		sb.setLength(0);
		sb.append(valueName);
		if (animationSBCache==null) animationSBCache=new StringBuilder();
		sb.append(NumberTools.formatNumber(value,animationSBCache));
		if (valueMinUse && valueMaxUse) {
			double delta=valueMax-valueMin;
			if (delta>0) {
				sb.append(' ');
				sb.append('(');
				double percent=FastMath.max(0,FastMath.min(1,(value-valueMin)/delta));
				sb.append(NumberTools.formatPercent(percent,animationSBCache));
				sb.append(')');
			}
		}

		setAnimationStringBuilder(animationSB[animationSBNext],()->{animationSBNext=(animationSBNext++)%2;});

		return true;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnalogValue";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		descriptionBuilder.addProperty(Language.tr("ModelDescription.AnalogValue.InitialValue"),NumberTools.formatNumberMax(initialValue),1000);
		if (valueMinUse) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AnalogValue.MinimumValue"),NumberTools.formatNumberMax(valueMin),2000);
		}
		if (valueMaxUse) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.AnalogValue.MaximumValue"),NumberTools.formatNumberMax(valueMax),3000);
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.AnalogValue.Rate"),NumberTools.formatNumberMax(changeRatePerSecond),2000);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.AnalogValue.UpdateStepWide"),NumberTools.formatNumberMax(analogNotify),5000);
	}

	private void prepareDraw() {
		double part=-1;
		if (valueMinUse && valueMaxUse) {
			double delta=valueMax-valueMin;
			if (delta>0) {
				final double value=(getRunData()!=null)?lastValue:initialValue;
				part=(FastMath.min(valueMax,FastMath.max(valueMin,value))-valueMin)/delta;
			}
		}

		shape.setFillLevel(part);
	}

	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		prepareDraw();
		super.drawToGraphics(graphics,drawRect,zoom,showSelectionFrames);
	}

	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		prepareDraw();
		super.specialOutput(outputBuilder);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (!connectionsIn.isEmpty()) return false;
		if (!connectionsOut.isEmpty()) return false;

		return false;
	}
}