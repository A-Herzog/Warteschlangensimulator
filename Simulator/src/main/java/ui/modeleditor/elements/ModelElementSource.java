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
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.RunModelFixer;
import simulator.simparser.ExpressionCalc;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateMode;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord;

/**
 * Kundenquelle
 * @author Alexander Herzog
 */
public class ModelElementSource extends ModelElementBox implements ElementWithNewClientNames, ModelElementEdgeOut, ModelDataRenameListener, ElementWithNewVariableNames {
	/** Auslaufende Kante */
	private ModelElementEdge connection;

	/** ID der auslaufenden Kante (wird nur beim Laden und Clonen verwendet) */
	private int connectionId=-1;

	/**
	 * Kundenank�nfte-Datensatz
	 * @see #getRecord()
	 */
	private final ModelElementSourceRecord record;

	/**
	 * Konstruktor der Klasse <code>ModelElementSource</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementSource(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_RIGHT);
		setName(Language.tr("Surface.Source.DefaultGroupName"));
		record=new ModelElementSourceRecord(false,false,true);
		record.addChangeListener(()->fireChanged());
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SOURCE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Source.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements �ndert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		updateIcon();
		super.fireChanged();
	}

	/**
	 * Aktualisiert die Beschriftung der auslaufenden Kante
	 * @see #fireChanged()
	 */
	private void updateEdgeLabel() {
		final String name=getName().trim();
		if (connection!=null) connection.setName(Language.tr("Surface.Source.LabelArrivals")+((name.isEmpty())?"":(" ("+name+")")));
	}

	/**
	 * Aktualisiert die Darstellung des zus�tzlichen Icons auf der Station.
	 * @see #fireChanged()
	 */
	private void updateIcon() {
		setAdditionalClientIconFromName(getName());
	}

	/**
	 * Liefert die Daten f�r die Kundenank�nfte
	 * @return	Kundenank�nfte-Datensatz
	 */
	public ModelElementSourceRecord getRecord() {
		return record;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSource)) return false;

		if (connection==null) {
			if (((ModelElementSource)element).connection!=null) return false;
		} else {
			if (((ModelElementSource)element).connection==null) return false;
			if (connection.getId()!=((ModelElementSource)element).connection.getId()) return false;
		}

		if (!((ModelElementSource)element).record.equalsRecord(record)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSource) {
			if (((ModelElementSource)element).connection!=null) connectionId=((ModelElementSource)element).connection.getId();
			record.copyDataFrom(((ModelElementSource)element).record);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSource clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSource element=new ModelElementSource(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		ModelElement element;
		if (connectionId>=0) {
			element=surface.getById(connectionId);
			if (element instanceof ModelElementEdge) connection=(ModelElementEdge)element;
			connectionId=-1;
			updateEdgeLabel();
		}
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		if (connection==null) return Language.tr("Surface.Source.Name")+" - "+Language.tr("Surface.Source.NotConnected");
		return Language.tr("Surface.Source.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Source.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,255,180);

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
			new ModelElementSourceDialog(owner,ModelElementSource.this,readOnly,clientData);
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

		if (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) {
			final Consumer<AbstractRealDistribution> distributionChanger=newDistribution->record.setInterarrivalTimeDistribution(newDistribution);
			panels.add(createContextMenuSliderDistributionMean(Language.tr("Surface.Source.AverageInterArrivalTime"),record.getTimeBase(),record.getInterarrivalTimeDistribution(),300,distributionChanger));
		}

		final Double D=ExpressionCalc.calcDirect(record.getBatchSize());
		if (D!=null) {
			final double initial=Math.max(1,D.doubleValue());
			final Function<Integer,String> batchChanger=value->{
				if (value==null) return NumberTools.formatNumber(initial)+" "+((initial==1)?Language.tr("Surface.Source.Dialog.Tab.BatchSize.ClientSingular"):Language.tr("Surface.Source.Dialog.Tab.BatchSize.ClientPlural"));
				final int count=value.intValue();
				record.setBatchSize(""+count);
				return count+" "+((count==1)?Language.tr("Surface.Source.Dialog.Tab.BatchSize.ClientSingular"):Language.tr("Surface.Source.Dialog.Tab.BatchSize.ClientPlural"));
			};
			panels.add(createContextMenuSliderValue(Language.tr("Surface.Source.Dialog.Tab.BatchSize"),initial,20,batchChanger));
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
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_CLIENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_CLIENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_CLIENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_W_CLIENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_W_CLIENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_W_CLIENT);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
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
		NextStationHelper.nextStationsSource(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optionale Men�punkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmen� ein
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	@Override
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
		if (record.getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) return;
		if (!DistributionTools.canSetMean(record.getInterarrivalTimeDistribution())) return;

		final JMenuItem item;
		final Icon icon=Images.PARAMETERSERIES.getIcon();
		popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeInterarrivalTime")));
		item.addActionListener(e->{
			TemplateRecord record=new TemplateRecord(TemplateMode.MODE_INTERARRIVAL,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeInterarrivalTime.Short"));
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setXMLMode(1);
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution"));
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
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();

		if (connection!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdge")));
			item.addActionListener(e->surface.remove(connection));
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);
			popupMenu.addSeparator();
		}
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connection!=null) surface.remove(connection);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (element==connection) {connection=null; fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Source.XML.Root");
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

		if (connection!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connection.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
		}

		record.saveToXML(doc,node);
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

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.Out",Language.trAllAttribute("Surface.XML.Connection.Type", node))) {
			final String s=Language.trAllAttribute("Surface.XML.Connection.Element",node);
			if (!s.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
				connectionId=I;
			}
			return null;
		}

		return record.loadPropertyFromXML(node);
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return connection==null;
	}

	/**
	 * F�gt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die auslaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (connection!=null) return false;
		connection=edge;
		connectionId=-1;
		fireChanged();
		return true;
	}

	/**
	 * Auslaufende Kante
	 * @return	Auslaufende Kante
	 */
	@Override
	public ModelElementEdge getEdgeOut() {
		return connection;
	}

	@Override
	public String[] getNewClientTypes() {
		return new String[]{getName()};
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSource";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);
		descriptionBuilder.addEdgeOut(getEdgeOut());
		record.buildDescriptionProperty(descriptionBuilder);
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_SIGNAL)) {
			record.signalRenamed(oldName,newName);
		}
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsIn.size()>0) return false;
		if (connectionsOut.size()!=1) return false;

		this.connection=connectionsOut.get(0);

		return true;
	}

	@Override
	public String[] getVariables() {
		return record.getSetRecord().getVariables();
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.source,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		record.search(searcher,this);
	}
}