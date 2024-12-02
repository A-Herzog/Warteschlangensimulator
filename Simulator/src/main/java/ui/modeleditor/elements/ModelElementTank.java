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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPanel;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElementData;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementTankData;
import simulator.elements.RunElementTankFlow;
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
 * Tank mit analogem F�llstand und Ventilen, um Fl�ssigkeit in und aus dem Tank zu leiten
 * @author Alexander Herzog
 */
public class ModelElementTank extends ModelElementBox {
	/**
	 * Kapazit�t des Tanks
	 * @see #getCapacity()
	 * @see #setCapacity(double)
	 */
	private double capacity;

	/**
	 * Initialwert
	 * @see #getInitialValue()
	 * @see #setInitialValue(double)
	 */
	private double initialValue;

	/**
	 * Liste der Ventile
	 * @see #getValves()
	 */
	private List<Valve> valves;

	/**
	 * Gibt an, wie h�ufig das System �ber die �nderung des Wertes benachrichtigt werden soll (in Sekunden).
	 * @see #getAnalogNotify()
	 * @see #setAnalogNotify(double)
	 */
	private double analogNotify;

	/**
	 * Konstruktor der Klasse <code>ModelElementTank</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementTank(final EditModel model, ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		capacity=100.0;
		initialValue=30.0;
		valves=new ArrayList<>();
		analogNotify=60.0;
		shape.setPartialFillColors(null,new Color(230,230,230));
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TANK.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Tank.Tooltip");
	}

	/**
	 * Liefert die eingestellte Kapazit�t des Tanks
	 * @return	Eingestellte Kapazit�t des Tanks
	 */
	public double getCapacity() {
		return capacity;
	}

	/**
	 * Setzt eine neue Kapazit�t f�r den Tank
	 * @param capacity	Kapazit�t f�r den Tank
	 */
	public void setCapacity(final double capacity) {
		if (capacity>0) this.capacity=capacity;
	}

	/**
	 * Liefert den aktuellen Initialwert.
	 * @return	Aktueller Initialwert
	 * @see ModelElementTank#setInitialValue(double)
	 */
	public double getInitialValue() {
		return initialValue;
	}

	/**
	 * Listet alle Vetile auf
	 * @return	Liste der Ventile
	 */
	public List<Valve> getValves() {
		return valves;
	}

	/**
	 * Stellt den Initialwert ein.
	 * @param initialValue	Neuer Initialwert
	 * @see ModelElementTank#getInitialValue()
	 */
	public void setInitialValue(final double initialValue) {
		if (initialValue>=0) this.initialValue=initialValue; else this.initialValue=0.0;
	}

	/**
	 * Gibt an, wie h�ufig das System �ber die �nderung des Wertes benachrichtigt werden soll (in Sekunden).
	 * @return	System-Benachrichtigungsabstand (in Sekunden)
	 * @see ModelElementTank#setAnalogNotify(double)
	 */
	public double getAnalogNotify() {
		return analogNotify;
	}

	/**
	 * Stellt ein, wie h�ufig das System �ber die �nderung des Wertes benachrichtigt werden soll (in Sekunden).
	 * @param analogNotify	System-Benachrichtigungsabstand (in Sekunden)
	 * @see ModelElementTank#getAnalogNotify()
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
		if (!(element instanceof ModelElementTank)) return false;

		if (capacity!=((ModelElementTank)element).capacity) return false;
		if (initialValue!=((ModelElementTank)element).initialValue) return false;
		if (valves.size()!=((ModelElementTank)element).valves.size()) return false;
		for (int i=0;i<valves.size();i++) if (!valves.get(i).equalsValve(((ModelElementTank)element).valves.get(i))) return false;
		if (analogNotify!=((ModelElementTank)element).analogNotify) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementTank) {
			capacity=((ModelElementTank)element).capacity;
			initialValue=((ModelElementTank)element).initialValue;
			for (Valve valve: ((ModelElementTank)element).valves) valves.add(new Valve(valve));
			analogNotify=((ModelElementTank)element).analogNotify;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementTank clone(final EditModel model, final ModelSurface surface) {
		final ModelElementTank element=new ModelElementTank(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Tank.Name");
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zur�ckgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	@Override
	protected String getErrorMessage() {
		if (analogNotify<=0) return Language.tr("Surface.ErrorInfo.InvalidAnalogStep");
		if (valves.size()==0) return Language.tr("Surface.ErrorInfo.NoValves");
		return null;
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Tank.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=Color.BLUE;

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
			new ModelElementTankDialog(owner,ModelElementTank.this,readOnly);
		};
	}

	/**
	 * F�gt stations-bedingte zus�tzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		final RunElementTankData data=(RunElementTankData)builder.data;

		builder.results.append("\n"+Language.tr("Statistics.AnalogValue.Current")+": "+NumberTools.formatNumber(data.getValueNoUpdate())+"\n");

		builder.results.append("\n");
		final double[] valveValues=data.getValveValues();
		for (int i=0;i<valveValues.length;i++) builder.results.append(String.format(Language.tr("Statistics.AnalogValue.ValveValue"),i+1)+": "+NumberTools.formatNumber(valveValues[i],5)+"\n");

		final RunElementTankFlow[] outgoing=data.getOutgoingFlows();
		if (outgoing.length>0) {
			builder.results.append("\n");
			builder.results.append(Language.tr("Statistics.AnalogValue.Flow.Outgoing")+":\n");
			for (RunElementTankFlow flow: outgoing) builder.results.append(flow.getAnimationInfoText(builder.simData)+"\n");
		}

		final RunElementTankFlow[] incoming=data.getIncomingFlows();
		if (incoming.length>0) {
			builder.results.append("\n");
			builder.results.append(Language.tr("Statistics.AnalogValue.Flow.Incoming")+":\n");
			for (RunElementTankFlow flow: incoming) builder.results.append(flow.getAnimationInfoText(builder.simData)+"\n");
		}
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

		final Function<Integer,String> capacityChanger=newCapacity->{
			if (newCapacity!=null) {
				capacity=newCapacity.intValue();
				if (initialValue>capacity) initialValue=capacity;
			}
			return Language.tr("Surface.Tank.Dialog.Capacity")+": "+NumberTools.formatNumberMax(capacity)+"; "+Language.tr("Surface.Tank.Dialog.InitialValue")+": "+NumberTools.formatNumberMax(initialValue);
		};
		panels.add(createContextMenuSliderValue(Language.tr("Surface.Tank.Dialog.Capacity"),capacity,100,capacityChanger));

		return panels.toArray(new JPanel[0]);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_ANALOG_VALUE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.CHART_ANALOG_VALUE);
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
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Tank.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Tank.Capacity")));
		sub.setTextContent(NumberTools.formatSystemNumber(capacity));

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Tank.InitialValue")));
		sub.setTextContent(NumberTools.formatSystemNumber(initialValue));

		for (Valve valve: valves) valve.saveToXML(doc,node);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Tank.UpdateStepWide")));
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

		if (Language.trAll("Surface.XML.Tank.Capacity",name)) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			capacity=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.XML.Tank.InitialValue",name)) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			initialValue=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.XML.Tank.Valve",name)) {
			final Valve valve=new Valve();
			error=valve.loadFromXML(node);
			if (error!=null) return error;
			valves.add(valve);
			return null;
		}

		if (Language.trAll("Surface.XML.Tank.UpdateStepWide",name)) {
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

	/**
	 * Cache der verf�gbaren {@link StringBuilder}
	 * f�r {@link #updateSimulationData(SimulationData, boolean)}
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private StringBuilder[] animationSB;

	/**
	 * N�chster in {@link #animationSB} zu verwendender Eintrag
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private int animationSBNext;


	/**
	 * Letzter in {@link #updateSimulationData(SimulationData, boolean)}
	 * dargestellter Wert
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private double lastValue=0;

	/**
	 * Zeichenkettenrepr�sentation von {@link #lastValue}
	 * die ggf. wiederverwendet werden kann.
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private String valueName;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		final RunElementData runData=getRunData();
		if (runData==null || isPreview) return false;

		if (!(runData instanceof RunElementTankData)) return false;
		final RunElementTankData data=(RunElementTankData)runData;
		final double value=data.getValue(simData);
		final double percent=value/FastMath.max(capacity,0.0001);

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
		sb.append(NumberTools.formatNumber(value));
		sb.append(' ');
		sb.append('(');
		sb.append(NumberTools.formatPercent(percent));
		sb.append(')');

		setAnimationStringBuilder(animationSB[animationSBNext],()->{animationSBNext=(animationSBNext++)%2;});

		return true;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementTank";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		descriptionBuilder.addProperty(Language.tr("ModelDescription.Tank.Capacity"),NumberTools.formatNumberMax(capacity),1000);
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Tank.InitialValue"),NumberTools.formatNumberMax(initialValue),2000);
		for (int i=0;i<valves.size();i++) {
			final Valve valve=valves.get(i);
			final String timeBaseString;
			switch (valve.getTimeBase()) {
			case TIMEBASE_HOURS:
				timeBaseString=Language.tr("ModelDescription.Tank.Valve.TimeBase.PerHour");
				break;
			case TIMEBASE_MINUTES:
				timeBaseString=Language.tr("ModelDescription.Tank.Valve.TimeBase.PerMinute");
				break;
			case TIMEBASE_SECONDS:
				timeBaseString=Language.tr("ModelDescription.Tank.Valve.TimeBase.PerSecond");
				break;
			default:
				timeBaseString=Language.tr("ModelDescription.Tank.Valve.TimeBase.PerSecond");
				break;
			}
			descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Tank.Valve"),i+1),NumberTools.formatNumberMax(valve.getInitialValue())+" ("+timeBaseString+")",3000);
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.AnalogValue.UpdateStepWide"),NumberTools.formatNumberMax(analogNotify),5000);
	}

	/**
	 * Wird vor dem ersten Zeichnen in der Animation aufgerufen,
	 * um {@link ModelElementPosition#shape} initial zu konfigurieren.
	 */
	private void prepareDraw() {
		double part=-1;
		if (initialValue>=0 && capacity>0) {
			part=((getRunData()!=null)?lastValue:initialValue)/capacity;
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

	/**
	 * Ventil f�r einen Tank
	 * @author Alexander Herzog
	 * @see ModelElementTank
	 */
	public static class Valve {
		/** Startwert f�r die �ffnung des Ventils */
		private double initialValue;
		/** Zeiteinheit f�r Festlegung des Ventildurchflusses pro Zeiteinheit */
		private ModelSurface.TimeBase timeBase;

		/**
		 * Konstruktor der Klasse
		 */
		public Valve() {
			initialValue=0.0;
			timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		}

		/**
		 * Copy-Konstruktor der Klasse
		 * @param copySource	Ausgangselement das kopiert werden soll
		 */
		public Valve(final Valve copySource) {
			initialValue=copySource.initialValue;
			timeBase=copySource.timeBase;
		}

		/**
		 * Liefert den Startwert f�r die �ffnung des Ventils (in Durchfluss pro Zeiteinheit).
		 * @return	Startwert f�r die �ffnung des Ventils
		 */
		public double getInitialValue() {
			return initialValue;
		}

		/**
		 * Stellt den Startwert f�r die �ffnung des Ventils ein (in Durchfluss pro Zeiteinheit).
		 * @param initialValue	Neuer Startwert f�r die �ffnung des Ventils
		 */
		public void setInitialValue(double initialValue) {
			if (initialValue>=0) this.initialValue=initialValue;
		}

		/**
		 * Liefert die Zeiteinheit f�r Festlegung des Ventildurchflusses pro Zeiteinheit.
		 * @return	Zeiteinheit f�r Festlegung des Ventildurchflusses pro Zeiteinheit
		 */
		public ModelSurface.TimeBase getTimeBase() {
			return timeBase;
		}

		/**
		 * Stellt die Zeiteinheit f�r Festlegung des Ventildurchflusses pro Zeiteinheit ein.
		 * @param timeBase	Neue Zeiteinheit f�r Festlegung des Ventildurchflusses pro Zeiteinheit
		 */
		public void setTimeBase(final ModelSurface.TimeBase timeBase) {
			if (timeBase!=null) this.timeBase=timeBase;
		}

		/**
		 * Vergleicht zwei Ventilobjekte
		 * @param otherValve	Weiteres Ventilobjekt welches mit diesem Objekt verglichen werden soll
		 * @return	Liefert <code>true</code> zur�ck, wenn die beiden Objekte inhaltlich identisch sind
		 */
		public boolean equalsValve(final Valve otherValve) {
			if (otherValve==null) return false;
			if (initialValue!=otherValve.initialValue) return false;
			if (timeBase!=otherValve.timeBase) return false;
			return true;
		}

		/**
		 * Speichert die Eigenschaften des Ventilobjektes als Untereintr�ge eines xml-Knotens
		 * @param doc	�bergeordnetes xml-Dokument
		 * @param parent	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
		 */
		public void saveToXML(final Document doc, final Element parent) {
			Element sub;
			parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Tank.Valve")));

			/* Initialer Wert */
			sub.setTextContent(NumberTools.formatSystemNumber(initialValue));

			/* Zeitbasis */
			final String timeBaseString;
			switch (timeBase) {
			case TIMEBASE_HOURS:
				timeBaseString=Language.trPrimary("Surface.XML.Tank.Valve.TimeBase.PerHour");
				break;
			case TIMEBASE_MINUTES:
				timeBaseString=Language.trPrimary("Surface.XML.Tank.Valve.TimeBase.PerMinute");
				break;
			case TIMEBASE_SECONDS:
				timeBaseString=Language.trPrimary("Surface.XML.Tank.Valve.TimeBase.PerSecond");
				break;
			default:
				timeBaseString=Language.trPrimary("Surface.XML.Tank.Valve.TimeBase.PerSecond");
				break;
			}
			sub.setAttribute(Language.trPrimary("Surface.XML.Tank.Valve.TimeBase"),timeBaseString);
		}

		/**
		 * L�dt die Einstellungen des Ventilobjektes aus einem einzelnen xml-Element.
		 * @param node	xml-Element, aus dem die Daten geladen werden soll
		 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
		 */
		public String loadFromXML(final Element node) {
			/* Initialer Wert */
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(node.getTextContent()));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			initialValue=D.doubleValue();

			/* Zeitbasis */
			final String timeBaseString=Language.trAllAttribute("Surface.XML.Tank.Valve.TimeBase",node);
			if (Language.trAll("Surface.XML.Tank.Valve.TimeBase.PerHour",timeBaseString)) timeBase=ModelSurface.TimeBase.TIMEBASE_HOURS;
			if (Language.trAll("Surface.XML.Tank.Valve.TimeBase.PerMinute",timeBaseString)) timeBase=ModelSurface.TimeBase.TIMEBASE_MINUTES;
			if (Language.trAll("Surface.XML.Tank.Valve.TimeBase.PerSecond",timeBaseString)) timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;

			return null;
		}
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsIn.size()>0) return false;
		if (connectionsOut.size()>0) return false;

		return false;
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Kapazit�t des Tanks */
		searcher.testDouble(this,Language.tr("Surface.Tank.Dialog.Capacity"),capacity,newCapacity->{if (newCapacity>0) capacity=newCapacity;});

		/* Initialwert */
		searcher.testDouble(this,Language.tr("Surface.Tank.Dialog.InitialValue"),initialValue,newInitialValue->{initialValue=newInitialValue;});

		/* Liste der Ventile */
		for (int i=0;i<valves.size();i++) {
			final int index=i;
			searcher.testDouble(this,String.format(Language.tr("Editor.DialogBase.Search.ValveMaximumFlow"),index),valves.get(index).initialValue,newMaxFlow->{if (newMaxFlow>0) valves.get(index).initialValue=newMaxFlow;});
		}

		/* Gibt an, wie h�ufig das System �ber die �nderung des Wertes benachrichtigt werden soll (in Sekunden). */
		searcher.testDouble(this,Language.tr("Surface.Tank.Dialog.AnalogNotify"),analogNotify,newAnalogNotify->{if (newAnalogNotify>0) analogNotify=newAnalogNotify;});
	}
}