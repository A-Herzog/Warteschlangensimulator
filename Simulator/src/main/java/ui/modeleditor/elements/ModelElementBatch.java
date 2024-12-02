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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
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
 * Sammelt die Kunden an der Station und leitet sie dann
 * entweder gleichzeitig einzeln oder als temporären
 * oder permanenten Batch weiter.
 * @author Alexander Herzog
 * @see ModelElementSeparate
 */
public class ModelElementBatch extends ModelElementMultiInSingleOutBox implements ElementWithNewClientNames, ModelElementAnimationForceMove {
	/**
	 * Batch-Datensatz
	 * @see #getBatchRecord()
	 */
	private final BatchRecord batchRecord;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementBatch(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_WEDGE_ARROW_RIGHT);
		batchRecord=new BatchRecord();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_BATCH.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Batch.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateIcon();
		super.fireChanged();
	}

	/**
	 * Aktualisiert die Darstellung des zusätzlichen Icons auf der Station.
	 * @see #fireChanged()
	 */
	private void updateIcon() {
		if (batchRecord!=null && batchRecord.getBatchMode()!=null) switch (batchRecord.getBatchMode()) {
		case BATCH_MODE_COLLECT:
			setAdditionalClientIconFromName(null);
			break;
		case BATCH_MODE_PERMANENT:
			setAdditionalClientIconFromName(batchRecord.getNewClientType());
			break;
		case BATCH_MODE_TEMPORARY:
			setAdditionalClientIconFromName(batchRecord.getNewClientType());
			break;
		}
	}

	@Override
	protected void updateEdgeLabel() {
		final String s=(batchRecord!=null)?batchRecord.getNewClientType():"";
		if (connectionOut!=null) connectionOut.setName(s.isEmpty()?"":(Language.tr("Surface.Batch.NewClientType")+" \""+s+"\""));
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementBatch)) return false;

		if (!batchRecord.equalsBatchRecord(((ModelElementBatch)element).batchRecord)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementBatch) {

			batchRecord.copyDataFrom(((ModelElementBatch)element).batchRecord);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementBatch clone(final EditModel model, final ModelSurface surface) {
		final ModelElementBatch element=new ModelElementBatch(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Liefert den Batch-Datensatz, der die Basis für dieses Element bildet.
	 * @return	Batch-Datensatz
	 * @see BatchRecord
	 */
	public BatchRecord getBatchRecord() {
		return batchRecord;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Batch.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Batch.Name.Short");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,225,255);

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
			new ModelElementBatchDialog(owner,ModelElementBatch.this,readOnly);
		};
	}

	/**
	 * Erstellte optional weitere Menüpunkte (in Form von Panels),
	 * die das direkte Bearbeiten von Einstellungen aus dem
	 * Kontextmenü heraus erlauben.
	 * @return	Array mit Panels (Array kann leer oder <code>null</code> sein; auch Einträge dürfen <code>null</code> sein)
	 */
	@Override
	protected JPanel[] addCustomSettingsToContextMenu() {
		final List<JPanel> panels=new ArrayList<>();

		if (batchRecord.getBatchSizeMode()==BatchRecord.BatchSizeMode.FIXED) {
			final Double D=NumberTools.getDouble(batchRecord.getBatchSizeFixed());
			if (D!=null && Math.round(D)>=1) {
				final int currentValue=(int)Math.round(D);
				final Function<Integer,String> batchChanger=value->{
					if (value==null) return batchRecord.getBatchSizeFixed()+" "+((currentValue==1)?Language.tr("Surface.Batch.BatchSize.ClientSingular"):Language.tr("Surface.Batch.BatchSize.ClientPlural"));
					final int count=value.intValue();
					batchRecord.setBatchSizeFixed(""+count);
					return count+" "+((count==1)?Language.tr("Surface.Batch.BatchSize.ClientSingular"):Language.tr("Surface.Batch.BatchSize.ClientPlural"));
				};
				panels.add(createContextMenuSliderValue(Language.tr("Surface.Batch.BatchSize"),currentValue,20,batchChanger));
			}

		}

		if (batchRecord.getBatchSizeMode()==BatchRecord.BatchSizeMode.RANGE) {
			final Double Dmin=NumberTools.getDouble(batchRecord.getBatchSizeMin());
			final Double Dmax=NumberTools.getDouble(batchRecord.getBatchSizeMax());
			if (Dmin!=null && Dmax!=null && Dmin.doubleValue()==Dmax.doubleValue() && Math.round(Dmin)>=1) {
				final int currentValue=(int)Math.round(Dmin);
				final Function<Integer,String> batchChanger=value->{
					if (value==null) return batchRecord.getBatchSizeMin()+" "+((currentValue==1)?Language.tr("Surface.Batch.BatchSize.ClientSingular"):Language.tr("Surface.Batch.BatchSize.ClientPlural"));
					final int count=value.intValue();
					batchRecord.setBatchSizeMin(""+count);
					batchRecord.setBatchSizeMax(""+count);
					return count+" "+((count==1)?Language.tr("Surface.Batch.BatchSize.ClientSingular"):Language.tr("Surface.Batch.BatchSize.ClientPlural"));
				};
				panels.add(createContextMenuSliderValue(Language.tr("Surface.Batch.BatchSize"),currentValue,20,batchChanger));
			}
		}

		return panels.toArray(new JPanel[0]);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Popupmenüs, welches die Einträge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfläche hinzugefügt werden sollen
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.CHART_WIP);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.HISTOGRAM_WIP);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Laufzeitstatistik hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugefügt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsBatch(this,parentMenu,addNextStation);
	}

	/**
	 * Fügt optionale Menüpunkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmenü ein
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	@Override
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
		if (batchRecord.getBatchSizeMode()!=BatchRecord.BatchSizeMode.FIXED) return;

		final JMenuItem item;
		final Icon icon=Images.PARAMETERSERIES.getIcon();
		popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeBatchSize")));
		item.addActionListener(e->{
			final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_BATCH_SIZE,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeBatchSize.Short"));
			record.input.setMode(ModelChanger.Mode.MODE_XML);
			record.input.setXMLMode(0);
			record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Batch.XML.Batch")+"->["+Language.trPrimary("Surface.Batch.XML.Batch.Size")+"]");
			buildSeries.accept(record);
		});
		if (icon!=null) item.setIcon(icon);
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
		return Language.trAll("Surface.Batch.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);
		batchRecord.addDataToXML(doc,node);
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

		return batchRecord.loadProperty(name,content,node);
	}

	@Override
	public String[] getNewClientTypes() {
		if (batchRecord.getBatchMode()==BatchRecord.BatchMode.BATCH_MODE_COLLECT) return null;
		return new String[]{batchRecord.getNewClientType()};
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zusätzlich auch die aktuelle Anzahl an Kunden an der Station
	 * während der Animation angezeigt werden sollen.
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
	protected String getIDInfo() {
		if (batchRecord.getBatchMode()==BatchRecord.BatchMode.BATCH_MODE_TEMPORARY || batchRecord.getBatchMode()==BatchRecord.BatchMode.BATCH_MODE_PERMANENT) {
			return super.getIDInfo()+", "+Language.tr("Surface.Batch.Dialog.NewType")+"="+batchRecord.getNewClientType();
		} else {
			return super.getIDInfo();
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementBatch";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		batchRecord.buildDescription(null,descriptionBuilder,1000);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		batchRecord.search(searcher,this,null);
	}
}