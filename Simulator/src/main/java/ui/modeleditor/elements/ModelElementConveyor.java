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
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
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
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateMode;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord;

/**
 * Flie�band
 * @author Alexander Herzog
 */
public class ModelElementConveyor extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener, ModelElementAnimationForceMove {
	/**
	 * Als was soll die Transportzeit erfasst werden?
	 * @author Alexander Herzog
	 * @see ModelElementConveyor#getTransportTimeType()
	 * @see ModelElementConveyor#setTransportTimeType(TransportTimeType)
	 */
	public enum TransportTimeType {
		/** Die Transportzeit soll als Wartezeit gez�hlt werden. */
		TRANSPORT_TYPE_WAITING,

		/** Die Transportzeit soll als Transferzeit gez�hlt werden. */
		TRANSPORT_TYPE_TRANSFER,

		/** Die Transportzeit soll als Bedienzeit gez�hlt werden. */
		TRANSPORT_TYPE_PROCESS,
	}

	/**
	 * In welche Richtung soll sich das F�rderband w�hrend der Animation bewegen?<br>
	 * (Hat f�r die Simulation als solches keine Auswirkungen.)
	 * @author Alexander Herzog
	 * @see ModelElementConveyor#getMoveDirection()
	 * @see ModelElementConveyor#setMoveDirection(MoveDirection)
	 */
	public enum MoveDirection {
		/** Bewegungsrichtung w�hrend der Animation: nach rechts */
		MOVE_LEFT_TO_RIGHT,

		/** Bewegungsrichtung w�hrend der Animation: nach link */
		MOVE_RIGHT_TO_LEFT;
	}

	/**
	 * Auf dem Flie�band verf�gbare Kapazit�t
	 * @see #getCapacityAvailable()
	 * @see #setCapacityAvailable(double)
	 */
	private double capacityAvailable;

	/**
	 * Im allgemeinen g�ltige Formel zur Bestimmung des Platzbedarfes
	 * @see #getCapacityNeededGlobal()
	 * @see #setCapacityNeededGlobal(String)
	 */
	private String capacityNeededGlobal;

	/**
	 * Formel zur Bestimmung des Platzbedarfes oder <code>null</code>, wenn keine Formel hinterlegt ist.
	 * @see #getCapacityNeeded(String)
	 * @see #setCapacityNeeded(String, String)
	 */
	private Map<String,String> capacityNeeded;

	/**
	 * Verwendete Zeitbasis
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Zeit, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Flie�bandes zu bef�rdern
	 * @see #getTransportTime()
	 * @see #setTransportTime(double)
	 */
	private double transportTime;

	/**
	 * Statistiktyp als was die Transportzeit erfasst werden soll
	 * @see #getTransportTimeType()
	 * @see #setTransportTime(double)
	 */
	private TransportTimeType transportTimeType;

	/**
	 * Bewegungsrichtung f�r die Animation
	 * @see #getMoveDirection()
	 * @see #setMoveDirection(MoveDirection)
	 */
	private MoveDirection moveDirection;

	/**
	 * Konstruktor der Klasse <code>ModelElementConveyor</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementConveyor(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);

		capacityAvailable=100;
		capacityNeededGlobal="1";
		capacityNeeded=new HashMap<>();

		timeBase=ModelSurface.TimeBase.TIMEBASE_MINUTES;
		transportTime=1;
		transportTimeType=TransportTimeType.TRANSPORT_TYPE_TRANSFER;

		moveDirection=MoveDirection.MOVE_LEFT_TO_RIGHT;
		setSize(new Dimension(150,30));
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_CONVEYOR.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Conveyor.Tooltip");
	}

	/**
	 * Liefert die auf dem Flie�band verf�gbare Kapazit�t
	 * @return	Auf dem Flie�band verf�gbare Kapazit�t
	 */
	public double getCapacityAvailable() {
		return capacityAvailable;
	}

	/**
	 * Stellt die auf dem Flie�band verf�gbare Kapazit�t ein.
	 * @param capacityAvailable	Auf dem Flie�band verf�gbare Kapazit�t
	 */
	public void setCapacityAvailable(final double capacityAvailable) {
		if (capacityAvailable<=0) return;
		this.capacityAvailable=capacityAvailable;
		fireChanged();
	}

	/**
	 * Liefert die im allgemeinen g�ltige Formel zur Bestimmung des Platzbedarfes f�r einen Kunden.
	 * @return	Im allgemeinen g�ltige Formel zur Bestimmung des Platzbedarfes
	 */
	public String getCapacityNeededGlobal() {
		return capacityNeededGlobal;
	}

	/**
	 * Stellt die im allgemeinen g�ltige Formel zur Bestimmung des Platzbedarfes f�r einen Kunden ein.
	 * @param capacityNeededGlobal	Im allgemeinen g�ltige Formel zur Bestimmung des Platzbedarfes
	 */
	public void setCapacityNeededGlobal(final String capacityNeededGlobal) {
		if (capacityNeededGlobal==null || capacityNeededGlobal.isBlank()) return;
		this.capacityNeededGlobal=capacityNeededGlobal;
		fireChanged();
	}

	/**
	 * Liefert die f�r einen Kundentyp g�ltige Formel zur Bestimmung des Platzbedarfes f�r einen Kunden.
	 * @param clientType	Kundentyp
	 * @return	Formel zur Bestimmung des Platzbedarfes oder <code>null</code>, wenn keine Formel hinterlegt ist.
	 */
	public String getCapacityNeeded(final String clientType) {
		return capacityNeeded.get(clientType);
	}

	/**
	 * Stellt die f�r einen Kundentyp g�ltige Formel zur Bestimmung des Platzbedarfes f�r einen Kunden ein.
	 * @param clientType	Kundentyp
	 * @param capacityNeeded	Formel oder <code>null</code>, wenn die Standardformel f�r diesen Kundentyp verwendet werden eoll
	 */
	public void setCapacityNeeded(final String clientType, final String capacityNeeded) {
		if (clientType==null || clientType.isBlank()) return;
		if (capacityNeeded==null || capacityNeeded.isBlank()) {
			this.capacityNeeded.remove(clientType);
		} else {
			this.capacityNeeded.put(clientType,capacityNeeded);
		}
		fireChanged();
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Transportzeit als Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Transportzeit als Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		if (timeBase==null) return;
		this.timeBase=timeBase;
		fireChanged();
	}

	/**
	 * Liefert die Zeit, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Flie�bandes zu bef�rdern.
	 * @return	Zeit, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Flie�bandes zu bef�rdern
	 */
	public double getTransportTime() {
		return transportTime;
	}

	/**
	 * Stellt die Zeit, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Flie�bandes zu bef�rdern, ein.
	 * @param transportTime	Zeit, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Flie�bandes zu bef�rdern
	 */
	public void setTransportTime(final double transportTime) {
		if (transportTime<0) return;
		this.transportTime=transportTime;
		fireChanged();
	}

	/**
	 * Gibt an, als was die Transportzeit erfasst werden soll.
	 * @return	Statistiktyp als was die Transportzeit erfasst werden soll
	 */
	public TransportTimeType getTransportTimeType() {
		return transportTimeType;
	}

	/**
	 * Stellt ein, als was die Transportzeit erfasst werden soll.
	 * @param transportTimeType	Statistiktyp als was die Transportzeit erfasst werden soll
	 */
	public void setTransportTimeType(TransportTimeType transportTimeType) {
		if (transportTimeType==null) return;
		this.transportTimeType=transportTimeType;
		fireChanged();
	}

	/**
	 * Liefert die Bewegungsrichtung f�r die Animation.
	 * @return	Bewegungsrichtung f�r die Animation
	 */
	public MoveDirection getMoveDirection() {
		return moveDirection;
	}

	/**
	 * Stellt die Bewegungsrichtung f�r die Animation ein.
	 * @param moveDirection	Bewegungsrichtung f�r die Animation
	 */
	public void setMoveDirection(final MoveDirection moveDirection) {
		if (moveDirection==null) return;
		this.moveDirection=moveDirection;
		fireChanged();
	}

	/**
	 * Liefert die L�nge des F�rderbandes.
	 * @return	L�nge des F�rderbandes
	 */
	public int getWidth() {
		return getSize().width;
	}

	/**
	 * Stellt die L�nge des F�rderbandes ein.
	 * @param width	L�nge des F�rderbandes
	 */
	public void setWidth(final int width) {
		if (width<=0) return;
		setSize(new Dimension(width,getSize().height));
		fireChanged();
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementConveyor)) return false;
		final ModelElementConveyor otherConveyor=(ModelElementConveyor)element;

		if (capacityAvailable!=otherConveyor.capacityAvailable) return false;
		if (!capacityNeededGlobal.equals(otherConveyor.capacityNeededGlobal)) return false;
		if (!Objects.deepEquals(capacityNeeded,otherConveyor.capacityNeeded)) return false;

		if (timeBase!=otherConveyor.timeBase) return false;
		if (transportTime!=otherConveyor.transportTime) return false;
		if (transportTimeType!=otherConveyor.transportTimeType) return false;

		if (moveDirection!=otherConveyor.moveDirection) return false;
		if (getSize().width!=otherConveyor.getSize().width) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementConveyor) {
			final ModelElementConveyor source=(ModelElementConveyor)element;

			capacityAvailable=source.capacityAvailable;
			capacityNeededGlobal=source.capacityNeededGlobal;
			capacityNeeded.clear();
			capacityNeeded.putAll(source.capacityNeeded);

			timeBase=source.timeBase;
			transportTime=source.transportTime;
			transportTimeType=source.transportTimeType;

			moveDirection=source.moveDirection;
			setSize(source.getSize());
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementConveyor clone(final EditModel model, final ModelSurface surface) {
		final ModelElementConveyor element=new ModelElementConveyor(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Conveyor.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Conveyor.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,212,212);

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
			new ModelElementConveyorDialog(owner,ModelElementConveyor.this,readOnly);
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

		if (capacityAvailable>0) {
			final Function<Integer,String> capacityChanger=value->{
				if (value==null) return Language.tr("Surface.Conveyor.Dialog.CapacityAvailable")+": "+NumberTools.formatNumber(capacityAvailable);
				final int count=value.intValue();
				capacityAvailable=count;
				return Language.tr("Surface.Conveyor.Dialog.CapacityAvailable")+": "+count;
			};
			panels.add(createContextMenuSliderValue(Language.tr("Surface.Conveyor.Dialog.CapacityAvailable"),capacityAvailable,100,capacityChanger));
		}

		final String timeBaseText;
		if (timeBase==null) {
			timeBaseText="";
		} else {
			timeBaseText=" "+ModelSurface.getTimeBaseString(timeBase);
		}

		if (transportTime>0) {
			final Function<Integer,String> transportTimeChanger=value->{
				if (value==null) return NumberTools.formatNumber(transportTime)+timeBaseText;
				final int count=value.intValue();
				transportTime=count;
				return ""+count+timeBaseText;
			};
			panels.add(createContextMenuSliderValue(Language.tr("Surface.Conveyor.Dialog.TransportTime"),transportTime,300,transportTimeChanger));
		}

		return panels.toArray(JPanel[]::new);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.CHART_NQ);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.CHART_WIP);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.HISTOGRAM_NQ);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.HISTOGRAM_WIP);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NQ);
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
		final JMenuItem item;
		final Icon icon=Images.PARAMETERSERIES.getIcon();
		popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeTransportTime")));
		item.addActionListener(e->{
			final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_CONVEYOR,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeTransportTime.Short"));
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setXMLMode(0);
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Conveyor.XML.TransportTime"));
			buildSeries.accept(record);
		});
		if (icon!=null) item.setIcon(icon);
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
		return Language.trAll("Surface.Conveyor.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Conveyor.XML.CapacityAvailable")));
		sub.setTextContent(NumberTools.formatSystemNumber(capacityAvailable));

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Conveyor.XML.CapacityNeeded")));
		sub.setTextContent(capacityNeededGlobal);

		for(Map.Entry<String,String> entry: capacityNeeded.entrySet()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Conveyor.XML.CapacityNeeded")));
			sub.setAttribute(Language.trPrimary("Surface.Conveyor.XML.CapacityNeeded.ClientType"),entry.getKey());
			sub.setTextContent(entry.getValue());
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Conveyor.XML.TransportTime")));
		sub.setTextContent(NumberTools.formatSystemNumber(transportTime));
		sub.setAttribute(Language.trPrimary("Surface.Conveyor.XML.TransportTime.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
		switch (transportTimeType) {
		case TRANSPORT_TYPE_WAITING:
			sub.setAttribute(Language.trPrimary("Surface.Conveyor.XML.TransportTime.Type"),Language.trPrimary("Surface.Conveyor.XML.TransportTime.Type.WaitingTime"));
			break;
		case TRANSPORT_TYPE_TRANSFER:
			sub.setAttribute(Language.trPrimary("Surface.Conveyor.XML.TransportTime.Type"),Language.trPrimary("Surface.Conveyor.XML.TransportTime.Type.TransferTime"));
			break;
		case TRANSPORT_TYPE_PROCESS:
			sub.setAttribute(Language.trPrimary("Surface.Conveyor.XML.TransportTime.Type"),Language.trPrimary("Surface.Conveyor.XML.TransportTime.Type.ProcessTime"));
			break;
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Conveyor.XML.MoveDirection")));
		switch (moveDirection) {
		case MOVE_LEFT_TO_RIGHT: sub.setTextContent(Language.trPrimary("Surface.Conveyor.XML.MoveDirection.LeftToRight")); break;
		case MOVE_RIGHT_TO_LEFT: sub.setTextContent(Language.trPrimary("Surface.Conveyor.XML.MoveDirection.RightToLeft")); break;
		}
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

		if (Language.trAll("Surface.Conveyor.XML.CapacityAvailable",name)) {
			final Double D=NumberTools.getPositiveDouble(NumberTools.localNumberToSystemNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			capacityAvailable=D.doubleValue();
			return null;
		}

		if (Language.trAll("Surface.Conveyor.XML.CapacityNeeded",name)) {
			final String clientType=Language.trAllAttribute("Surface.Conveyor.XML.CapacityNeeded.ClientType",node);
			if (clientType.isEmpty()) capacityNeededGlobal=content; else capacityNeeded.put(clientType,content);
			return null;
		}

		if (Language.trAll("Surface.Conveyor.XML.TransportTime",name)) {
			final Double D=NumberTools.getNotNegativeDouble(NumberTools.localNumberToSystemNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			transportTime=D.doubleValue();

			final String timeBaseName=Language.trAllAttribute("Surface.Conveyor.XML.TransportTime.TimeBase",node);
			timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);

			final String timeType=Language.trAllAttribute("Surface.Conveyor.XML.TransportTime.Type",node);
			if (Language.trAll("Surface.Conveyor.XML.TransportTime.Type.WaitingTime",timeType)) transportTimeType=TransportTimeType.TRANSPORT_TYPE_WAITING;
			if (Language.trAll("Surface.Conveyor.XML.TransportTime.Type.TransferTime",timeType)) transportTimeType=TransportTimeType.TRANSPORT_TYPE_TRANSFER;
			if (Language.trAll("Surface.Conveyor.XML.TransportTime.Type.ProcessTime",timeType)) transportTimeType=TransportTimeType.TRANSPORT_TYPE_PROCESS;

			return null;
		}

		if (Language.trAll("Surface.Conveyor.XML.MoveDirection",name)) {
			if (Language.trAll("Surface.Conveyor.XML.MoveDirection.LeftToRight",content)) moveDirection=MoveDirection.MOVE_LEFT_TO_RIGHT;
			if (Language.trAll("Surface.Conveyor.XML.MoveDirection.RightToLeft",content)) moveDirection=MoveDirection.MOVE_RIGHT_TO_LEFT;
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
	public String getHelpPageName() {
		return "ModelElementConveyor";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Vorhandene Kapazit�t */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Conveyor.CapacityAvailable"),NumberTools.formatNumber(capacityAvailable),2000);

		/* Ben�tigte Kapazit�t pro Kunde (allgemeiner Fall) */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Conveyor.CapacityNeededGlobal"),capacityNeededGlobal,3000);

		/* Ben�tigte Kapazit�t pro Kunde (kundentypabh�ngig) */
		for (String clientType: getSurface().getClientTypes()) {
			final String value=getCapacityNeeded(clientType);
			if (value!=null) descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Conveyor.CapacityNeeded"),clientType),value,3100);
		}

		/* Transportzeit erfassen als ... */
		switch (transportTimeType) {
		case TRANSPORT_TYPE_WAITING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Conveyor.Mode"),Language.tr("ModelDescription.Conveyor.Mode.Waiting"),5000);
			break;
		case TRANSPORT_TYPE_TRANSFER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Conveyor.Mode"),Language.tr("ModelDescription.Conveyor.Mode.Transfer"),5000);
			break;
		case TRANSPORT_TYPE_PROCESS:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Conveyor.Mode"),Language.tr("ModelDescription.Conveyor.Mode.Process"),5000);
			break;
		}

		/* Transportzeit */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Conveyor.Time"),NumberTools.formatNumber(transportTime),5100);

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,5200);
	}

	@Override
	public void objectRenamed(String oldName, String newName, RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		/* Keine Kopie anlegen, wenn es bereits Daten f�r den neuen Namen gibt. */
		if (getCapacityNeeded(newName)!=null) return;

		/* Daten �bertragen */
		final String needed=getCapacityNeeded(oldName);
		setCapacityNeeded(newName,needed);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Auf dem Flie�band verf�gbare Kapazit�t */
		searcher.testDouble(this,Language.tr("Surface.Conveyor.Dialog.CapacityAvailable"),capacityAvailable,newCapacityAvailable->{if (newCapacityAvailable>0) capacityAvailable=newCapacityAvailable;});

		/* Im allgemeinen g�ltige Formel zur Bestimmung des Platzbedarfes  */
		searcher.testString(this,Language.tr("Surface.Conveyor.Dialog.CapacityNeededGlobal"),capacityNeededGlobal,newCapacityNeededGlobal->{capacityNeededGlobal=newCapacityNeededGlobal;});

		/* Formel zur Bestimmung des Platzbedarfes */
		if (capacityNeeded!=null) for (Map.Entry<String,String> capacityNeededClientType: capacityNeeded.entrySet()) {
			final String clientType=capacityNeededClientType.getKey();
			searcher.testString(this,String.format(Language.tr("Surface.Conveyor.Dialog.CapacityNeeded"),clientType),capacityNeededClientType.getValue(),newNeededCapacity->capacityNeeded.put(clientType,newNeededCapacity));
		}

		/* Zeit, die notwendig ist, um einen Kunden von der einen zur anderen Seite des Flie�bandes zu bef�rdern */
		searcher.testDouble(this,Language.tr("Surface.Conveyor.Dialog.TransportTime"),transportTime,newTransportTime->{if (newTransportTime>=0) transportTime=newTransportTime;});
	}
}
