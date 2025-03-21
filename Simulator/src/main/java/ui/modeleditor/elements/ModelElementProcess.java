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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.RunModelFixer;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelDataResourceUsage;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateMode;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord;

/**
 * Hauptklasse f�r die Bearbeitung von Kunden (Warteschlange und Bedienschalter)
 * @author Alexander Herzog
 */
public class ModelElementProcess extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementEdgeMultiOut, ModelElementAnimationForceMove, ModelDataResourceUsage {
	/**
	 * Standardm��ige Priorit�t f�r Kundentypen
	 */
	public static final String DEFAULT_CLIENT_PRIORITY="w";

	/**
	 * Standardm��ige Priorit�t zum Bezug von Ressourcen
	 */
	public static final String DEFAULT_RESOURCE_PRIORITY="1";

	/**
	 * Art wie die Bedienzeit f�r die Kundenstatistik gez�hlt werden soll
	 * @author Alexander Herzog
	 * @see ModelElementProcess#getProcessTimeType()
	 * @see ModelElementProcess#setProcessTimeType(ProcessType)
	 */
	public enum ProcessType {
		/** Die Verarbeitungszeit soll als Wartezeit gez�hlt werden. */
		PROCESS_TYPE_WAITING,

		/** Die Verarbeitungszeit soll als Transferzeit gez�hlt werden. */
		PROCESS_TYPE_TRANSFER,

		/** Die Verarbeitungszeit soll als Bedienzeit gez�hlt werden. */
		PROCESS_TYPE_PROCESS,

		/** Die Verarbeitungszeit soll nicht erfasst werden. */
		PROCESS_TYPE_NOTHING
	}

	/**
	 * Einlaufende Kanten
	 */
	private List<ModelElementEdge> connectionsIn;

	/**
	 * Auslaufende Kante f�r erfolgreiche Kunden
	 */
	private ModelElementEdge connectionOutSuccess;

	/**
	 * Auslaufende Kante f�r Warteabbrecher (optional)
	 */
	private ModelElementEdge connectionOutCancel;

	/**
	 * IDs der einlaufenden Kanten (wird nur beim Laden und Clonen verwendet)
	 */
	private List<Integer> connectionsInIds=null;

	/**
	 * IDs der auslaufenden Kante f�r erfolgreiche Kunden (wird nur beim Laden und Clonen verwendet)
	 * @see #connectionOutSuccess
	 */
	private int connectionOutSuccessId=-1;

	/**
	 * IDs der auslaufenden Kante f�r Warteabbrecher (wird nur beim Laden und Clonen verwendet)
	 * @see #connectionOutCancel
	 */
	private int connectionOutCancelId=-1;

	/**
	 * Verwendete Zeitbasis
	 * @see #getTimeBase()
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Art der Z�hlung der Prozesszeiten
	 * @see #getProcessTimeType()
	 * @see #setProcessTimeType(ProcessType)
	 */
	private ProcessType processTimeType;

	/**
	 * Minimale Batch-Gr��e
	 * @see #getBatchMinimum()
	 * @see #setBatchMinimum(int)
	 */
	private int batchMin; /* >=1 */

	/**
	 * Maximale Batch-Gr��e
	 * @see #getBatchMaximum()
	 * @see #setBatchMaximum(int)
	 */
	private int batchMax; /* >=batchMin */

	/**
	 * Sollen durch die Priorisierung Kampagnen (m�glichst wenige Kundentyp-Wechsel) gebildet werden?
	 * @see #isCampaignMode()
	 * @see #setCampaignMode(boolean)
	 */
	private boolean campaignMode;

	/**
	 * Objekt, welches die Verteilungen und Ausdr�cke f�r die Bedienzeiten vorh�lt
	 * @see #getWorking()
	 */
	private DistributionSystem working;

	/**
	 * Objekt, welches die Verteilungen und Ausdr�cke f�r die Nachbearbeitungszeiten vorh�lt
	 * @see #getPostProcessing()
	 */
	private DistributionSystem postProcessing;

	/**
	 * Objekt, welches die Verteilungen und Ausdr�cke f�r die Wartezeittoleranzen vorh�lt
	 * @see #getCancel()
	 */
	private DistributionSystem cancel;

	/**
	 * Objekt, welches die Verteilungen und Ausdr�cke f�r die R�stzeiten vorh�lt
	 * @see #getSetupTimes()
	 */
	private DistributionSystemSetupTimes setupTimes;

	/**
	 * Kann ein Kunde das Warten auch noch w�hrend der R�stzeit aufgeben?
	 * @see #isCanCancelInSetupTime()
	 * @see #setCanCancelInSetupTime(boolean)
	 */
	private boolean canCancelInSetupTime;

	/**
	 * Priorit�t f�r Kunden eines bestimmten Kundentyp
	 * @see #getPriority(String)
	 * @see #setPriority(String, String)
	 */
	private final Map<String,String> priority; /*  Kundentyp, Priorit�t-Formel */

	/**
	 * Bedienergruppen und deren Anzahlen, die f�r die Bedienung der Kunden notwendig sind
	 * @see #getNeededResources()
	 */
	private final List<Map<String,Integer>> resources; /* Name der Ressource, ben�tigte Anzahl */

	/**
	 * Ressorcen-Priorisierungs-Formel
	 * @see #getResourcePriority()
	 * @see #setResourcePriority(String)
	 */
	private String resourcePriority;

	/**
	 * Soll die Ressourcenverf�gbarkeit in der angegebenen Ressourcen-Alternativen-Reihenfolge (<code>false</code>) oder in zuf�lliger Reihenfolge (<code>true</code>) gepr�ft werden?
	 * @see #isResourceCheckInRandomOrder()
	 * @see #setResourceCheckInRandomOrder(boolean)
	 */
	private boolean resourceCheckInRandomOrder;

	/**
	 * Kosten pro Bedienvorgang
	 * @see #getCosts()
	 * @see #setCosts(String)
	 */
	private String costs;

	/**
	 * Kosten pro Bediensekunde
	 * @see #getCostsPerProcessSecond()
	 * @see #setCostsPerProcessSecond(String)
	 */
	private String costsPerProcessSecond;

	/**
	 * Kosten pro Nachbearbeitungssekunde
	 * @see #getCostsPerPostProcessSecond()
	 * @see #setCostsPerPostProcessSecond(String)
	 */
	private String costsPerPostProcessSecond;

	/**
	 * Konstruktor der Klasse <code>ModelElementProcess</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementProcess(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE_DOUBLE_LINE);
		connectionsIn=new ArrayList<>();

		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		processTimeType=ProcessType.PROCESS_TYPE_PROCESS;
		batchMin=1;
		batchMax=1;
		campaignMode=false;

		/* Um sicher zu stellen, dass die Language-Strings auch in den Sprachdateien vorhanden sind. Der folgende DistributionSystem-Konstruktor ist kein Scan-Ziel f�r die Sprachdateien. */
		Language.tr("Surface.Process.XML.Distribution.ClientType");
		Language.tr("Surface.Process.XML.Distribution.Type.ProcessingTime");
		Language.tr("Surface.Process.XML.Distribution.Type.PostProcessingTime");
		Language.tr("Surface.Process.XML.Distribution.Type.CancelationTime");

		setupTimes=new DistributionSystemSetupTimes();
		canCancelInSetupTime=false;
		working=new DistributionSystem("Surface.Process.XML.Distribution.ClientType","Surface.Process.XML.Distribution.Type.ProcessingTime",false);
		postProcessing=new DistributionSystem("Surface.Process.XML.Distribution.ClientType","Surface.Process.XML.Distribution.Type.PostProcessingTime",true);
		cancel=new DistributionSystem("Surface.Process.XML.Distribution.ClientType","Surface.Process.XML.Distribution.Type.CancelationTime",true);

		priority=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		resources=new ArrayList<>();
		resourcePriority=DEFAULT_RESOURCE_PRIORITY;
		resourceCheckInRandomOrder=false;

		costs="0";
		costsPerProcessSecond="0";
		costsPerPostProcessSecond="0";
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements �ndert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	/**
	 * Gibt die M�glichkeit, das Label an der auslaufenden Kante zu aktualisieren, nachdem sich im Element Ver�nderungen ergeben haben.
	 */
	protected void updateEdgeLabel() {
		if (connectionOutCancel==null) {
			if (connectionOutSuccess!=null) connectionOutSuccess.setName("");
		} else {
			if (connectionOutSuccess!=null) connectionOutSuccess.setName(Language.tr("Surface.Process.Label.Success"));
			connectionOutCancel.setName(Language.tr("Surface.Process.Label.WaitingCancelation"));
		}
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementProcess)) return false;

		final ModelElementProcess process=(ModelElementProcess)element;

		/* Einlaufende Kanten */
		final List<ModelElementEdge> connectionsIn2=process.connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		/* Auslaufende Kante (Erfolg) */
		if (connectionOutSuccess==null) {
			if (process.connectionOutSuccess!=null) return false;
		} else {
			if (process.connectionOutSuccess==null) return false;
			if (connectionOutSuccess.getId()!=process.connectionOutSuccess.getId()) return false;
		}

		/* Auslaufende Kante (Warteabbrecher) */
		if (connectionOutCancel==null) {
			if (process.connectionOutCancel!=null) return false;
		} else {
			if (process.connectionOutCancel==null) return false;
			if (connectionOutCancel.getId()!=process.connectionOutCancel.getId()) return false;
		}

		/* Zeitbasis */
		if (process.timeBase!=timeBase) return false;

		/* Bedienzeit ist ... */
		if (process.processTimeType!=processTimeType) return false;

		/* Batch-Gr��e */
		if (batchMin!=process.batchMin) return false;
		if (batchMax!=process.batchMax) return false;

		/* Kampagnen-Modus */
		if (campaignMode!=process.campaignMode) return false;

		/* Bedienzeiten */
		if (!working.equalsDistributionSystem(process.working)) return false;

		/* Nachbearbeitungszeiten */
		if (!postProcessing.equalsDistributionSystem(process.postProcessing)) return false;

		/* Abbruchzeiten */
		if (!cancel.equalsDistributionSystem(process.cancel)) return false;

		/* R�stzeiten */
		if (!setupTimes.equalsDistributionSystem(process.setupTimes)) return false;
		if (canCancelInSetupTime!=process.canCancelInSetupTime) return false;

		/* Priorit�ten */
		Map<String,String> priorityA=priority;
		Map<String,String> priorityB=process.priority;
		for (Map.Entry<String,String> entry : priorityA.entrySet()) {
			if (!entry.getValue().equals(priorityB.get(entry.getKey()))) return false;
		}
		for (Map.Entry<String,String> entry : priorityB.entrySet()) {
			if (!entry.getValue().equals(priorityA.get(entry.getKey()))) return false;
		}

		/* Ressourcen*/
		if (resources.size()!=process.resources.size()) {
			/* Existiert in einem Element ein (automatisch erstellter) leerer Alternativen-Eintrag und im anderen gar keiner, so sind beide immer noch gleich. */
			if (resources.size()==0) {
				if (process.resources.size()!=1) return false;
				if (process.resources.get(0).size()!=0) return false;
			} else {
				if (resources.size()!=1) return false;
				if (process.resources.size()!=0) return false;
				if (resources.get(0).size()!=0) return false;
			}
		} else {
			for (int i=0;i<resources.size();i++) {
				final Map<String,Integer> resourcesA=resources.get(i);
				final Map<String,Integer> resourcesB=process.resources.get(i);
				for (Map.Entry<String,Integer> entry : resourcesA.entrySet()) {
					if (!entry.getValue().equals(resourcesB.get(entry.getKey()))) return false;
				}
				for (Map.Entry<String,Integer> entry : resourcesB.entrySet()) {
					if (!entry.getValue().equals(resourcesA.get(entry.getKey()))) return false;
				}
			}
		}
		if (!resourcePriority.equalsIgnoreCase(process.resourcePriority)) return false;
		if (resourceCheckInRandomOrder!=process.resourceCheckInRandomOrder) return false;

		/* Kosten */
		if (!Objects.equals(costs,process.costs)) return false;
		if (!Objects.equals(costsPerProcessSecond,process.costsPerProcessSecond)) return false;
		if (!Objects.equals(costsPerPostProcessSecond,process.costsPerPostProcessSecond)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementProcess) {

			final ModelElementProcess process=(ModelElementProcess)element;

			/* Einlaufende Kanten */
			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=process.connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			/* Auslaufende Kanten */
			if (process.connectionOutSuccess!=null) connectionOutSuccessId=process.connectionOutSuccess.getId();
			if (process.connectionOutCancel!=null) connectionOutCancelId=process.connectionOutCancel.getId();

			/* Zeitbasis */
			timeBase=process.timeBase;

			/* Bedienzeit ist ... */
			processTimeType=process.processTimeType;

			/* Batch-Gr��e */
			batchMin=process.batchMin;
			batchMax=process.batchMax;

			/* Kampagnen-Modus */
			campaignMode=process.campaignMode;

			/* Bedienzeiten */
			working=process.working.clone();

			/* Nachbearbeitungszeiten */
			postProcessing=process.postProcessing.clone();

			/* Abbruchzeiten */
			cancel=process.cancel.clone();

			/* R�stzeiten */
			setupTimes=process.setupTimes.clone();
			canCancelInSetupTime=process.canCancelInSetupTime;

			/* Priorit�ten */
			priority.clear();
			for (Map.Entry<String,String> entry: process.priority.entrySet()) priority.put(entry.getKey(),entry.getValue());

			/* Ressourcen*/
			resources.clear();
			for (Map<String,Integer> map: process.resources) {
				final Map<String,Integer> copy=createNewResourceMap();
				resources.add(copy);
				for (Map.Entry<String,Integer> entry: map.entrySet()) copy.put(entry.getKey(),entry.getValue());
			}
			resourcePriority=process.resourcePriority;
			resourceCheckInRandomOrder=process.resourceCheckInRandomOrder;

			/* Kosten */
			costs=process.costs;
			costsPerProcessSecond=process.costsPerProcessSecond;
			costsPerPostProcessSecond=process.costsPerPostProcessSecond;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementProcess clone(final EditModel model, final ModelSurface surface) {
		final ModelElementProcess element=new ModelElementProcess(model,surface);
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

		if (connectionsInIds!=null) {
			for (int i=0;i<connectionsInIds.size();i++) {
				element=surface.getById(connectionsInIds.get(i));
				if (element instanceof ModelElementEdge) connectionsIn.add((ModelElementEdge)element);
			}
			connectionsInIds=null;
		}

		if (connectionOutSuccessId>=0) {
			element=surface.getById(connectionOutSuccessId);
			if (element instanceof ModelElementEdge) connectionOutSuccess=(ModelElementEdge)element;
			connectionOutSuccessId=-1;
			updateEdgeLabel();
		}

		if (connectionOutCancelId>=0) {
			if (connectionOutSuccess!=null) { /* Ohne Erfolgskante keine Abbruchkante */
				element=surface.getById(connectionOutCancelId);
				if (element instanceof ModelElementEdge) connectionOutCancel=(ModelElementEdge)element;
			}
			connectionOutCancelId=-1;
			updateEdgeLabel();
		}
	}

	/**
	 * F�gt Men�punkte zum Hinzuf�gen von einlaufenden und auslaufender Kante zum Kontextmen�
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zur�ck, wenn Elemente in das Kontextmen� eingef�gt wurden (und ggf. ein Separator vor dem n�chsten Abschnitt gesetzt werden sollte)
	 */
	protected final boolean addRemoveEdgesContextMenuItems(final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();
		boolean needSeparator=false;

		needSeparator=needSeparator || addEdgesInContextMenu(popupMenu,surface,readOnly);

		if (connectionOutSuccess!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener(e->{
				if (connectionOutCancel!=null) surface.remove(connectionOutCancel);
				surface.remove(connectionOutSuccess);
			});
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);
			needSeparator=true;

			if (connectionOutCancel!=null) {
				final JMenu menu=new JMenu(Language.tr("Surface.Connection.LineMode.ChangeAllEdgesOut"));
				popupMenu.add(menu);

				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Global"),Images.MODEL.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(null));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Direct"),Images.EDGE_MODE_DIRECT.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.DIRECT));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLine"),Images.EDGE_MODE_MULTI_LINE.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.MULTI_LINE));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLineRounded"),Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.MULTI_LINE_ROUNDED));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.CubicCurve"),Images.EDGE_MODE_CUBIC_CURVE.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.CUBIC_CURVE));
			}
		}

		return needSeparator;
	}

	/**
	 * Stellt den Darstellungsmodus f�r alle auslaufenden Kanten ein.
	 * @param lineMode	Neuer Darstellungsmodus
	 */
	private void setEdgeOutLineMode(final ModelElementEdge.LineMode lineMode) {
		connectionOutSuccess.setLineMode(lineMode);
		connectionOutSuccess.fireChanged();
		connectionOutCancel.setLineMode(lineMode);
		connectionOutCancel.fireChanged();
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionsIn!=null) {
			while (connectionsIn.size()>0) {
				ModelElement element=connectionsIn.remove(0);
				surface.remove(element);
			}
		}
		if (connectionOutSuccess!=null) surface.remove(connectionOutSuccess);
		if (connectionOutCancel!=null) surface.remove(connectionOutCancel);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionOutSuccess==element) {connectionOutSuccess=null; fireChanged();}
		if (connectionOutCancel==element) {connectionOutCancel=null; fireChanged();}
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

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionOutSuccess!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connectionOutSuccess.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.tr("Surface.XML.Connection.Type.Out"));
		}
		if (connectionOutCancel!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connectionOutCancel.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.tr("Surface.XML.Connection.Type.Out"));
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.Connection.Status"),Language.tr("Surface.Process.XML.Connection.Status.WaitingCancelation"));
		}

		if (batchMin!=1 || batchMax!=1) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.Batch")));
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.Batch.Minimum"),""+batchMin);
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.Batch.Maximum"),""+batchMax);
		}

		if (campaignMode) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.CampaignMode")));
			sub.setTextContent("1");
		}

		working.save(doc,node,element->{
			element.setAttribute(Language.trPrimary("Surface.Process.XML.Distribution.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
			switch (processTimeType) {
			case PROCESS_TYPE_WAITING:
				element.setAttribute(Language.trPrimary("Surface.Process.XML.TimeType"),Language.trPrimary("Surface.Process.XML.TimeType.WaitingTime"));
				break;
			case PROCESS_TYPE_TRANSFER:
				element.setAttribute(Language.trPrimary("Surface.Process.XML.TimeType"),Language.trPrimary("Surface.Process.XML.TimeType.TransferTime"));
				break;
			case PROCESS_TYPE_PROCESS:
				element.setAttribute(Language.trPrimary("Surface.Process.XML.TimeType"),Language.trPrimary("Surface.Process.XML.TimeType.ProcessTime"));
				break;
			case PROCESS_TYPE_NOTHING:
				element.setAttribute(Language.trPrimary("Surface.Process.XML.TimeType"),Language.trPrimary("Surface.Process.XML.TimeType.Nothing"));
				break;
			}
		});
		postProcessing.save(doc,node);
		cancel.save(doc,node);
		setupTimes.save(doc,node);

		if (canCancelInSetupTime) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.CanCancelInSetupTime")));
			sub.setTextContent("1");
		}

		for (Map.Entry<String,String> entry : priority.entrySet()) if (entry.getValue()!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.Priority")));
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.Distribution.ClientType"),entry.getKey());
			sub.setTextContent(entry.getValue());
		}

		for (int i=0;i<resources.size();i++) {
			final Map<String,Integer> map=resources.get(i);
			for (Map.Entry<String,Integer> entry : map.entrySet()) if (entry.getValue()!=null) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.Operators")));
				sub.setAttribute(Language.trPrimary("Surface.Process.XML.Operators.Group"),entry.getKey());
				sub.setAttribute(Language.trPrimary("Surface.Process.XML.Operators.Count"),""+entry.getValue());
				sub.setAttribute(Language.trPrimary("Surface.Process.XML.Operators.Alternative"),""+(i+1));
			}
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.OperatorsPriority")));
		sub.setTextContent(resourcePriority);

		if (resourceCheckInRandomOrder) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.OperatorsCheckInRandomOrder")));
			sub.setTextContent("1");
		}

		if (costs!=null && !costs.isBlank() && ! costs.trim().equals("0")) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.StationCosts")));
			sub.setTextContent(costs);
		}

		if ((costsPerProcessSecond!=null && !costsPerProcessSecond.isBlank() && !costsPerProcessSecond.trim().equals("0")) || (costsPerPostProcessSecond!=null && !costsPerPostProcessSecond.isBlank() && !costsPerPostProcessSecond.trim().equals("0"))) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.TimeCosts")));
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.TimeCosts.ProcessingSecond"),costsPerProcessSecond);
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.TimeCosts.PostProcessingSecond"),costsPerPostProcessSecond);
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

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
			}
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				final String status=Language.trAllAttribute("Surface.Process.XML.Connection.Status",node);
				if (Language.trAll("Surface.Process.XML.Connection.Status.WaitingCancelation",status)) {
					connectionOutCancelId=I;
				} else {
					connectionOutSuccessId=I;
				}
			}
			return null;
		}

		if (Language.trAll("Surface.Process.XML.Batch",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.Process.XML.Batch.Minimum",node));
			if (I==null || I<1) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Process.XML.Batch.Minimum"),name,node.getParentNode().getNodeName());
			batchMin=I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.Process.XML.Batch.Maximum",node));
			if (I==null || I<batchMin) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Process.XML.Batch.Maximum"),name,node.getParentNode().getNodeName());
			batchMax=I;
			return null;
		}

		if (Language.trAll("Surface.Process.XML.CampaignMode",name)) {
			campaignMode=(!content.isEmpty() && !content.equals("0"));
			return null;
		}

		if (DistributionSystem.isDistribution(node)) {
			final String modeString=DistributionSystem.getTypeAttribute(node);
			if (Language.trAll("Surface.Process.XML.Distribution.Type.ProcessingTime",modeString)) {
				if (working.isGlobal(node)) {
					final String timeBaseName=Language.trAllAttribute("Surface.Process.XML.Distribution.TimeBase",node);
					timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
					final String type=Language.trAllAttribute("Surface.Process.XML.TimeType",node);
					if (Language.trAll("Surface.Process.XML.TimeType.WaitingTime",type)) processTimeType=ProcessType.PROCESS_TYPE_WAITING;
					if (Language.trAll("Surface.Process.XML.TimeType.TransferTime",type)) processTimeType=ProcessType.PROCESS_TYPE_TRANSFER;
					if (Language.trAll("Surface.Process.XML.TimeType.ProcessTime",type)) processTimeType=ProcessType.PROCESS_TYPE_PROCESS;
					if (Language.trAll("Surface.Process.XML.TimeType.Nothing",type)) processTimeType=ProcessType.PROCESS_TYPE_NOTHING;
				}
				return working.loadDistribution(node);
			}
			if (Language.trAll("Surface.Process.XML.Distribution.Type.PostProcessingTime",modeString)) {
				return postProcessing.loadDistribution(node);
			}
			if (Language.trAll("Surface.Process.XML.Distribution.Type.CancelationTime",modeString)) {
				return cancel.loadDistribution(node);
			}
			return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type"),name,node.getParentNode().getNodeName());
		}

		if (DistributionSystem.isExpression(node)) {
			final String modeString=DistributionSystem.getTypeAttribute(node);
			if (Language.trAll("Surface.Process.XML.Distribution.Type.ProcessingTime",modeString)) {
				if (working.isGlobal(node)) {
					final String timeBaseName=Language.trAllAttribute("Surface.Process.XML.Distribution.TimeBase",node);
					timeBase=ModelSurface.getTimeBaseInteger(timeBaseName);
					final String type=Language.trAllAttribute("Surface.Process.XML.TimeType",node);
					if (Language.trAll("Surface.Process.XML.TimeType.WaitingTime",type)) processTimeType=ProcessType.PROCESS_TYPE_WAITING;
					if (Language.trAll("Surface.Process.XML.TimeType.TransferTime",type)) processTimeType=ProcessType.PROCESS_TYPE_TRANSFER;
					if (Language.trAll("Surface.Process.XML.TimeType.ProcessTime",type)) processTimeType=ProcessType.PROCESS_TYPE_PROCESS;
				}
				return working.loadExpression(node);
			}
			if (Language.trAll("Surface.Process.XML.Distribution.Type.PostProcessingTime",modeString)) {
				return postProcessing.loadExpression(node);
			}
			if (Language.trAll("Surface.Process.XML.Distribution.Type.CancelationTime",modeString)) {
				return cancel.loadExpression(node);
			}
			return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type"),name,node.getParentNode().getNodeName());
		}

		if (DistributionSystemSetupTimes.isSetupTimesNode(node)) {
			return setupTimes.load(node);
		}

		if (Language.trAll("Surface.Process.XML.CanCancelInSetupTime",name)) {
			canCancelInSetupTime=(!content.isEmpty() && !content.equals("0"));
			return null;
		}

		if (Language.trAll("Surface.Process.XML.Priority",name)) {
			final String typ=Language.trAllAttribute("Surface.Process.XML.Distribution.ClientType",node);
			if (!typ.isBlank()) priority.put(typ,content);
			return null;
		}

		if (Language.trAll("Surface.Process.XML.Operators",name)) {
			final String type=Language.trAllAttribute("Surface.Process.XML.Operators.Group",node);
			if (!type.isBlank()) {
				Long L=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.Process.XML.Operators.Count",node));
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Process.XML.Operators.Count"),name,node.getParentNode().getNodeName());
				int count=L.intValue();
				String alt=Language.trAllAttribute("Surface.Process.XML.Operators.Alternative",node);
				if (alt.isBlank()) alt="1";
				L=NumberTools.getPositiveLong(alt);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Process.XML.Operators.Alternative"),name,node.getParentNode().getNodeName());
				int alternative=FastMath.min(L.intValue()-1,10_000);
				while (resources.size()<=alternative) resources.add(createNewResourceMap());
				resources.get(alternative).put(type,count);
			}
			return null;
		}

		if (Language.trAll("Surface.Process.XML.OperatorsPriority",name)) {
			resourcePriority=content;
			return null;
		}

		if (Language.trAll("Surface.Process.XML.OperatorsCheckInRandomOrder",name)) {
			resourceCheckInRandomOrder=content.equals("1");
			return null;
		}

		if (Language.trAll("Surface.Process.XML.StationCosts",name)) {
			if (!content.isEmpty()) costs=content;
			return null;
		}

		if (Language.trAll("Surface.Process.XML.TimeCosts",name)) {
			String s;
			s=Language.trAllAttribute("Surface.Process.XML.TimeCosts.ProcessingSecond",node);
			if (!s.isEmpty()) {
				costsPerProcessSecond=s;
			}
			s=Language.trAllAttribute("Surface.Process.XML.TimeCosts.PostProcessingSecond",node);
			if (!s.isEmpty()) {
				costsPerPostProcessSecond=s;
			}
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * F�gt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die einlaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0) {
			connectionsIn.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connectionsIn.toArray(ModelElementEdge[]::new);
	}

	/**
	 * Auslaufende Kanten
	 * @return	Auslaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesOut() {
		if (connectionOutSuccess==null) {
			if (connectionOutCancel==null) return new ModelElementEdge[0];
			return new ModelElementEdge[]{connectionOutCancel};
		} else {
			if (connectionOutCancel==null) return new ModelElementEdge[]{connectionOutSuccess};
			return new ModelElementEdge[]{connectionOutSuccess, connectionOutCancel};
		}
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return (connectionOutSuccess==null) || (connectionOutCancel==null);
	}

	/**
	 * F�gt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die auslaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (connectionOutSuccess!=null && connectionOutCancel!=null) return false;
		if (edge==null || connectionsIn.indexOf(edge)>=0) return false;

		if (connectionOutSuccess==null) {
			connectionOutSuccess=edge;
			connectionOutSuccessId=-1;
		} else {
			connectionOutCancel=edge;
			connectionOutCancelId=-1;
		}
		fireChanged();
		return true;
	}

	/**
	 * Liefert die auslaufende Kante f�r erfolgreiche Kunden.
	 * @return	Auslaufende Kante (erfolgreiche Kunden)
	 */
	public ModelElementEdge getEdgeOutSuccess() {
		return connectionOutSuccess;
	}

	/**
	 * Liefert die auslaufende Kante f�r Warteabbrecher.
	 * @return	Auslaufende Kante (Warteabbrecher)
	 */
	public ModelElementEdge getEdgeOutCancel() {
		return connectionOutCancel;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_PROCESS.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Process.Tooltip");
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Process.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Process.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(64,127,255);

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
			new ModelElementProcessDialog(owner,ModelElementProcess.this,readOnly);
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

		if (working.get() instanceof AbstractRealDistribution) {
			final Consumer<AbstractRealDistribution> distributionChanger=newDistribution->working.set(newDistribution);
			panels.add(createContextMenuSliderDistributionMean(Language.tr("Surface.Process.AverageProcessingTime"),timeBase,(AbstractRealDistribution)working.get(),300,distributionChanger));
		}

		if (cancel.get() instanceof AbstractRealDistribution) {
			final Consumer<AbstractRealDistribution> distributionChanger=newDistribution->cancel.set(newDistribution);
			panels.add(createContextMenuSliderDistributionMean(Language.tr("Surface.Process.AverageCancelTime"),timeBase,(AbstractRealDistribution)cancel.get(),300,distributionChanger));
		}

		if (batchMin==batchMax && batchMin>=1) {
			final int initial=batchMin;
			final Function<Integer,String> batchChanger=value->{
				if (value==null) return NumberTools.formatLong(initial)+" "+((initial==1)?Language.tr("Surface.Process.Dialog.ClientSingular"):Language.tr("Surface.Process.Dialog.ClientPlural"));
				final int count=value.intValue();
				batchMin=count;
				batchMax=count;
				return count+" "+((count==1)?Language.tr("Surface.Process.Dialog.ClientSingular"):Language.tr("Surface.Process.Dialog.ClientPlural"));
			};
			panels.add(createContextMenuSliderValue(Language.tr("Surface.Process.Dialog.BatchSize"),initial,20,batchChanger));
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
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_NQ_AVERAGE);
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
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NQ);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.PROCESS);
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
	 * Sind an der Bedienstation mehrere Verteilungen hinterlegt?
	 * @return	Liefert <code>true</code>, wenn mehrere Verteilungen vorhanden sind
	 */
	private boolean processHasMultiTimes() {
		if (getWorking().getNames().length>0) return true;
		if (getPostProcessing().get()!=null) return true;
		if (getCancel().get()!=null) return true;
		if (getSetupTimes()!=null) return true;
		return false;
	}

	/**
	 * F�gt optionale Men�punkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmen� ein
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	@Override
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
		JMenuItem item;
		final Icon icon=Images.PARAMETERSERIES.getIcon();
		final List<String> clientTypeData=new ArrayList<>();

		/* Bedienzeiten (global) */
		final Object obj1=getWorking().get();
		if (obj1 instanceof AbstractRealDistribution) {
			if (DistributionTools.canSetMean((AbstractRealDistribution)obj1)) {
				popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTime")));
				item.addActionListener(e->{
					final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_SERVICETIMES,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTime.Short"));
					record.input.setMode(ModelChanger.Mode.MODE_XML);
					record.input.setXMLMode(1);
					String add="";
					if (processHasMultiTimes()) add="["+Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type")+"=\""+Language.trPrimary("Surface.Process.XML.Distribution.Type.ProcessingTime")+"\"]";
					record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
					buildSeries.accept(record);
				});
				if (icon!=null) item.setIcon(icon);
			}
			if (DistributionTools.canSetStandardDeviationExactIndependent((AbstractRealDistribution)obj1)) {
				popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTimeStd")));
				item.addActionListener(e->{
					final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_SERVICETIMES,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTimeStd.Short"));
					record.input.setMode(ModelChanger.Mode.MODE_XML);
					record.input.setXMLMode(2);
					String add="";
					if (processHasMultiTimes()) add="["+Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type")+"=\""+Language.trPrimary("Surface.Process.XML.Distribution.Type.ProcessingTime")+"\"]";
					record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
					buildSeries.accept(record);
				});
				if (icon!=null) item.setIcon(icon);
			}
		}

		/* Bedienzeiten nach Kundentypen */
		clientTypeData.clear();
		for (String clientType: getModel().surface.getClientTypes()) {
			final Object obj2=working.get(clientType);
			if ((obj2 instanceof AbstractRealDistribution) && DistributionTools.canSetMean((AbstractRealDistribution)obj2)) clientTypeData.add(clientType);
		}
		if (clientTypeData.size()>0) {
			final JMenu sub;
			popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTimeClientType")));
			if (icon!=null) sub.setIcon(icon);
			final Icon icon2=Images.MODELPROPERTIES_CLIENTS.getIcon();
			for (String clientType: clientTypeData) {
				final String clientTypeFinal=clientType;
				sub.add(item=new JMenuItem(clientTypeFinal));
				item.addActionListener(e->{
					final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_SERVICETIMES,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTimeClientType.Short")+" - "+clientTypeFinal);
					record.input.setMode(ModelChanger.Mode.MODE_XML);
					record.input.setXMLMode(1);
					final String add="["+(working.getSubNumber(clientTypeFinal)+2)+"]";
					record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
					buildSeries.accept(record);
				});
				if (icon2!=null) item.setIcon(icon2);
			}
		}

		/* Wartezeittoleranzen (global) */
		final Object obj3=getCancel().get();
		if ((obj3 instanceof AbstractRealDistribution) && DistributionTools.canSetMean((AbstractRealDistribution)obj3)) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeWaitingTimeTolerance")));
			item.addActionListener(e->{
				final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_WAITINGTIME_TOLERANCES,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeWaitingTimeTolerance.Short"));
				record.input.setMode(ModelChanger.Mode.MODE_XML);
				record.input.setXMLMode(1);
				String add="["+Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type")+"=\""+Language.trPrimary("Surface.Process.XML.Distribution.Type.CancelationTime")+"\"]";
				record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
				buildSeries.accept(record);
			});
			if (icon!=null) item.setIcon(icon);
		}

		/* Wartezeittoleranzen nach Kundentypen */
		clientTypeData.clear();
		for (String clientType: getModel().surface.getClientTypes()) {
			final Object obj4=cancel.get(clientType);
			if ((obj4 instanceof AbstractRealDistribution) && DistributionTools.canSetMean((AbstractRealDistribution)obj4)) clientTypeData.add(clientType);
		}
		if (clientTypeData.size()>0) {
			final JMenu sub;
			popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeWaitingTimeTolerance")));
			if (icon!=null) sub.setIcon(icon);
			final Icon icon2=Images.MODELPROPERTIES_CLIENTS.getIcon();
			for (String clientType: clientTypeData) {
				final String clientTypeFinal=clientType;
				sub.add(item=new JMenuItem(clientTypeFinal));
				item.addActionListener(e->{
					final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_WAITINGTIME_TOLERANCES,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeWaitingTimeTolerance.Short")+" - "+clientTypeFinal);
					record.input.setMode(ModelChanger.Mode.MODE_XML);
					record.input.setXMLMode(1);
					int nr=1; /* Bedienzeit (1-basierend in xml-Datei) */
					nr+=working.getSubNumberCount();
					if (postProcessing.get()!=null) nr++;
					nr+=postProcessing.getSubNumberCount();
					if (cancel.get()!=null) nr++;
					nr+=(cancel.getSubNumber(clientTypeFinal)+1); /* Z�hlung intern 0-basierend, daher m�ssen wir hier 1 addieren */
					final String add="["+nr+"]";
					record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
					buildSeries.accept(record);
				});
				if (icon2!=null) item.setIcon(icon2);
			}
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
		return Language.trAll("Surface.Process.XML.Root");
	}

	/**
	 * Liefert die minimale Batch-Gr��e.
	 * @return	Minimale Batch-Gr��e
	 */
	public int getBatchMinimum() {
		return batchMin;
	}

	/**
	 * Liefert die maximale Batch-Gr��e.
	 * @return	Maximale Batch-Gr��e
	 */
	public int getBatchMaximum() {
		return batchMax;
	}

	/**
	 * Stellt die minimale Batch-Gr��e ein.
	 * @param batchMin	Neue minimale Batch-Gr��e
	 */
	public void setBatchMinimum(final int batchMin) {
		this.batchMin=batchMin;
	}

	/**
	 * Stellt die maximale Batch-Gr��e ein.
	 * @param batchMax	Neue maximale Batch-Gr��e
	 */
	public void setBatchMaximum(final int batchMax) {
		this.batchMax=batchMax;
	}

	/**
	 * Gibt an, ob durch die Priorisierung Kampagnen (m�glichst wenige Kundentyp-Wechsel) gebildet werden sollen.
	 * @return	Kampagnen-Modus ja oder nein
	 * @see #setCampaignMode(boolean)
	 */
	public boolean isCampaignMode() {
		return campaignMode;
	}

	/**
	 * Stellt ein, ob durch die Priorisierung Kampagnen (m�glichst wenige Kundentyp-Wechsel) gebildet werden sollen.
	 * @param campaignMode	Kampagnen-Modus ja oder nein
	 * @see #isCampaignMode()
	 */
	public void setCampaignMode(final boolean campaignMode) {
		this.campaignMode=campaignMode;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdr�cke f�r die Bedienzeiten vorh�lt
	 * @return	Bedienzeiten
	 */
	public DistributionSystem getWorking() {
		return working;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdr�cke f�r die Nachbearbeitungszeiten vorh�lt
	 * @return	Nachbearbeitungszeiten
	 */
	public DistributionSystem getPostProcessing() {
		return postProcessing;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdr�cke f�r die Wartezeittoleranzen vorh�lt
	 * @return	Wartezeittoleranzen
	 */
	public DistributionSystem getCancel() {
		return cancel;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdr�cke f�r die R�stzeiten vorh�lt
	 * @return	R�stzeiten
	 */
	public DistributionSystemSetupTimes getSetupTimes() {
		return setupTimes;
	}

	/**
	 * Kann ein Kunde das Warten auch noch w�hrend der R�stzeit aufgeben?
	 * @return	Kann ein Kunde das Warten auch noch w�hrend der R�stzeit aufgeben?
	 */
	public boolean isCanCancelInSetupTime() {
		return canCancelInSetupTime;
	}

	/**
	 * Kann ein Kunde das Warten auch noch w�hrend der R�stzeit aufgeben?
	 * @param canCancelInSetupTime	Kann ein Kunde das Warten auch noch w�hrend der R�stzeit aufgeben?
	 */
	public void setCanCancelInSetupTime(final boolean canCancelInSetupTime) {
		this.canCancelInSetupTime=canCancelInSetupTime;
	}

	/**
	 * Liefert die Priorit�t f�r Kunden eines bestimmten Kundentyp.
	 * @param clientType	Kundentyp f�r den die Priorit�t geliefert werden soll
	 * @return Priorit�t f�r Kunden dieses Kundentyps
	 */
	public String getPriority(final String clientType) {
		final String p=priority.get(clientType);
		if (p==null) return DEFAULT_CLIENT_PRIORITY; else return p;
	}

	/**
	 * Stellt Priorit�t f�r die Kunden eines bestimmten Kundentyps ein.
	 * @param clientType	Kundentyp f�r den die Priorit�t eingestellt werden soll
	 * @param priority	Neue Priorit�t f�r Kundes des Kundentyps
	 */
	public void setPriority(final String clientType, final String priority) {
		if (clientType==null || clientType.isBlank()) return;
		if (priority==null) this.priority.put(clientType,""); else this.priority.put(clientType,priority);
	}

	/**
	 * Liefert die Aufstellung der zur Bearbeitung von Kunden ben�tigten Ressourcen.<br>
	 * Es existiert immer mindestens ein Eintrag in der Liste.
	 * @return	Bedienergruppen und deren Anzahlen, die f�r die Bedienung der Kunden notwendig sind
	 */
	public List<Map<String,Integer>> getNeededResources() {
		if (resources.size()==0) resources.add(createNewResourceMap());
		return resources;
	}

	/**
	 * Erstellt einen neuen Eintrag f�r die Ressourcenliste (f�gt ihn dort aber noch nicht ein)
	 * @return	Neue Ressourcenzuordnungsalternative
	 * @see #getNeededResources()
	 */
	public Map<String,Integer> createNewResourceMap() {
		return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Liefert die Formel zur�ck, gem�� derer die Priorisierung der Bedienstation bei der Zuweisung von verf�gbaren Ressourcen erfolgen soll.
	 * @return	Aktuelle Ressorcen-Priorisierungs-Formel
	 */
	public String getResourcePriority() {
		return resourcePriority;
	}

	/**
	 * Stellt die Formel ein, gem�� derer die Priorisierung der Bedienstation bei der Zuweisung von verf�gbaren Ressourcen erfolgen soll.
	 * @param newResourcePriority Neue Ressorcen-Priorisierungs-Formel
	 */
	public void setResourcePriority(final String newResourcePriority) {
		if (newResourcePriority==null || newResourcePriority.isBlank()) return;
		resourcePriority=newResourcePriority;
	}


	/**
	 * Soll die Ressourcenverf�gbarkeit in der angegebenen Ressourcen-Alternativen-Reihenfolge oder in zuf�lliger Reihenfolge gepr�ft werden?
	 * @return	Soll die Ressourcenverf�gbarkeit in der angegebenen Ressourcen-Alternativen-Reihenfolge (<code>false</code>) oder in zuf�lliger Reihenfolge (<code>true</code>) gepr�ft werden?
	 * @see #setResourceCheckInRandomOrder(boolean)
	 */
	public boolean isResourceCheckInRandomOrder() {
		return resourceCheckInRandomOrder;
	}

	/**
	 * Soll die Ressourcenverf�gbarkeit in der angegebenen Ressourcen-Alternativen-Reihenfolge oder in zuf�lliger Reihenfolge gepr�ft werden?
	 * @param resourceCheckInRandomOrder	Soll die Ressourcenverf�gbarkeit in der angegebenen Ressourcen-Alternativen-Reihenfolge (<code>false</code>) oder in zuf�lliger Reihenfolge (<code>true</code>) gepr�ft werden?
	 * @see #isResourceCheckInRandomOrder()
	 */
	public void setResourceCheckInRandomOrder(boolean resourceCheckInRandomOrder) {
		this.resourceCheckInRandomOrder=resourceCheckInRandomOrder;
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungs-/Ausdruckswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungs-/Ausdruckswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=timeBase;
	}

	/**
	 * Gibt an, ob die Prozesszeiten als Bedienzeiten, als Transferzeiten oder als Wartezeiten gez�hlt werden sollen.
	 * @return	Art der Z�hlung der Prozesszeiten
	 * @see ProcessType
	 */
	public ProcessType getProcessTimeType() {
		return processTimeType;
	}

	/**
	 * Stellt ein, ob die Prozesszeiten als Bedienzeiten, als Transferzeiten oder als Wartezeiten gez�hlt werden sollen.
	 * @param processTimeType	Art der Z�hlung der Prozesszeiten
	 */
	public void setProcessTimeType(final ProcessType processTimeType) {
		this.processTimeType=processTimeType;
	}

	/**
	 * Liefert die eingestellten Kosten pro Bedienvorgang in der Station
	 * @return	Kosten pro Bedienvorgang
	 */
	public String getCosts() {
		if (costs==null || costs.isBlank()) return "0";
		return costs.trim();
	}

	/**
	 * Stellt die Kosten pro Bedienvorgang in der Station ein
	 * @param costs	Kosten pro Bedienvorgang
	 */
	public void setCosts(final String costs) {
		if (costs==null || costs.isBlank()) {
			this.costs="0";
		} else {
			this.costs=costs.trim();
		}
	}

	/**
	 * Liefert die eingestellten Kosten pro Bediensekunde in der Station
	 * @return	Kosten pro Bediensekunde
	 */
	public String getCostsPerProcessSecond() {
		if (costsPerProcessSecond==null || costsPerProcessSecond.isBlank()) return "0";
		return costsPerProcessSecond.trim();
	}

	/**
	 * Stellt die Kosten pro Bediensekunde in der Station ein
	 * @param costsPerProcessSecond	Kosten pro Bediensekunde
	 */
	public void setCostsPerProcessSecond(final String costsPerProcessSecond) {
		if (costsPerProcessSecond==null || costsPerProcessSecond.isBlank()) {
			this.costsPerProcessSecond="0";
		} else {
			this.costsPerProcessSecond=costsPerProcessSecond.trim();
		}
	}

	/**
	 * Liefert die eingestellten Kosten pro Nachbearbeitungssekunde in der Station
	 * @return	Kosten pro Nachbearbeitungssekunde
	 */
	public String getCostsPerPostProcessSecond() {
		if (costsPerPostProcessSecond==null || costsPerPostProcessSecond.isBlank()) return "0";
		return costsPerPostProcessSecond.trim();
	}

	/**
	 * Stellt die Kosten pro Nachbearbeitungssekunde in der Station ein
	 * @param costsPerPostProcessSecond	Kosten pro Nachbearbeitungssekunde
	 */
	public void setCostsPerPostProcessSecond(final String costsPerPostProcessSecond) {
		if (costsPerPostProcessSecond==null || costsPerPostProcessSecond.isBlank()) {
			this.costsPerPostProcessSecond="0";
		} else {
			this.costsPerPostProcessSecond=costsPerPostProcessSecond.trim();
		}
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation �berhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung �bersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation ber�cksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden m�ssten).
	 * @return	Gibt <code>true</code> zur�ck, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connectionsIn.size()>0;
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
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) {
			/* Keine Kopie anlegen, wenn es bereits Daten f�r den neuen Namen gibt. */
			if (!getPriority(newName).equals(ModelElementProcess.DEFAULT_CLIENT_PRIORITY)) return;
			if (getWorking().nameInUse(newName)) return;
			if (getPostProcessing().nameInUse(newName)) return;
			if (getCancel().nameInUse(newName)) return;
			if (getSetupTimes().nameInUse(newName)) return;

			/* Daten �bertragen */
			setPriority(newName,getPriority(oldName));
			getWorking().renameSubType(oldName,newName);
			getPostProcessing().renameSubType(oldName,newName);
			getCancel().renameSubType(oldName,newName);
			getSetupTimes().renameSubType(oldName,newName);
		}

		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_RESOURCE)) {
			for (Map<String,Integer> map : getNeededResources()) {
				final Integer neededNumber=map.get(oldName);
				if (neededNumber!=null) {map.remove(oldName); map.put(newName,neededNumber);}
			}
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementProcess";
	}

	/**
	 * Liefert f�r die Modellbeschreibung eine Information dar�ber,
	 * wie viele Bediener in einer Bedienergruppe vorhanden sind.
	 * Dies kann ein Zahlenwert, aber auch "unendlich viele" usw. sein.
	 * @param name	Name der Bedienergruppe
	 * @return	Anzahl an Bedienern in der Gruppe
	 * @see #buildDescription(ModelDescriptionBuilder)
	 */
	private String getResourceAvailable(final String name) {
		final ModelResource resource=getModel().resources.getNoAutoAdd(name);
		if (resource==null) return "";
		if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return " ("+String.format(Language.tr("ModelDescription.Process.Resources.Available.BySchedule"),resource.getSchedule())+")";
		final int count=resource.getCount();
		if (count<0) return " ("+Language.tr("ModelDescription.Process.Resources.Available.InfiniteNumber")+")";
		return " ("+String.format(Language.tr("ModelDescription.Process.Resources.Available.Number"),count)+")";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Bearbeitungszeit ist f�r den Kunden... */
		switch (processTimeType) {
		case PROCESS_TYPE_WAITING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ProcessTimeType"),Language.tr("ModelDescription.Process.ProcessTimeType.Waiting"),1000);
			break;
		case PROCESS_TYPE_TRANSFER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ProcessTimeType"),Language.tr("ModelDescription.Process.ProcessTimeType.Transfer"),1000);
			break;
		case PROCESS_TYPE_PROCESS:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ProcessTimeType"),Language.tr("ModelDescription.Process.ProcessTimeType.Process"),1000);
			break;
		case PROCESS_TYPE_NOTHING:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ProcessTimeType"),Language.tr("ModelDescription.Process.ProcessTimeType.Nothing"),1000);
			break;
		}

		/* R�stzeiten */
		if (setupTimes.isActive()) {
			final String[] clientTypeNames=setupTimes.getNames();
			for (String clientA: clientTypeNames) for (String clientB: clientTypeNames) {
				final Object obj=setupTimes.get(clientA,clientB);
				final String propertyName=String.format(Language.tr("ModelDescription.Process.SetupTime"),clientA,clientB);
				if (obj instanceof String) descriptionBuilder.addProperty(propertyName,Language.tr("ModelDescription.Process.SetupTime.Expression")+": "+(String)obj,2000);
				if (obj instanceof AbstractRealDistribution) descriptionBuilder.addProperty(propertyName,ModelDescriptionBuilder.getDistributionInfo((AbstractRealDistribution)obj),2000);
			}
			if (canCancelInSetupTime && cancel.hasData()) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.CanCancelInSetupTime"),Language.tr("ModelDescription.Process.CanCancelInSetupTime.Yes"),2500);
			}
		}

		/* Bedienzeiten */
		working.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.Process.WorkingTime.ClientType"),Language.tr("ModelDescription.Process.WorkingTime.GeneralCase"),3000);

		/* Nachbearbeitungszeiten */
		postProcessing.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.Process.PostProcessingTime.ClientType"),Language.tr("ModelDescription.Process.PostProcessingTime.GeneralCase"),3000);

		/* Abbruchzeiten */
		cancel.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.Process.CancelTime.ClientType"),Language.tr("ModelDescription.Process.CancelTime.GeneralCase"),3000);

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,6000);

		/* Batch-Gr��en */
		if (batchMin>1) {
			final String size;
			if (batchMax>batchMin) {
				size=NumberTools.formatLong(batchMin)+".."+NumberTools.formatLong(batchMax);
			} else {
				size=NumberTools.formatLong(batchMin);
			}
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.BatchSize"),size,7000);
		}

		/* Kampagnen-Modus */
		if (campaignMode) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.CampaignMode"),Language.tr("ModelDescription.Process.CampaignMode.Yes"),7500);
		}

		/* Priorit�ten */
		final String[] clientTypes=descriptionBuilder.getClientTypes();
		boolean needPrioInfo=false;
		for (String clientType: clientTypes) {
			final String prio=priority.get(clientType);
			if (prio!=null && !prio.isBlank() && !prio.equals(DEFAULT_CLIENT_PRIORITY)) {needPrioInfo=true; break;}
		}
		if (needPrioInfo) for (String clientType: clientTypes) {
			final String prio=priority.get(clientType);
			if (prio!=null && !prio.isBlank()) {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Process.ClientTypePriority"),clientType),prio,8000);
			} else {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Process.ClientTypePriority"),clientType),DEFAULT_CLIENT_PRIORITY,8000);
			}
		}

		/* Ressourcen */
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<resources.size();i++) {
			final Map<String,Integer> map=resources.get(i);
			if (resources.size()>1) {
				if (sb.length()>0) sb.append("\n");
				sb.append(String.format(Language.tr("ModelDescription.Process.Resources.Alternative"),i+1));
			}
			for (Map.Entry<String,Integer> entry: map.entrySet()) {
				if (entry.getValue().intValue()>0) {
					if (sb.length()>0) sb.append("\n");
					final String name=entry.getKey();
					sb.append(String.format(Language.tr("ModelDescription.Process.Resources.Resource"),name,entry.getValue().intValue()));
					sb.append(getResourceAvailable(name));
				}
			}
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Resources"),sb.toString(),9000);

		/* Pr�fung der Alternativen in zuf�lliger Reihenfolge */
		if (resources.size()>1) {
			if (resourceCheckInRandomOrder) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ResourceCheckOrder"),Language.tr("ModelDescription.Process.ResourceCheckOrder.Random"),9500);
			} else {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ResourceCheckOrder"),Language.tr("ModelDescription.Process.ResourceCheckOrder.InOrder"),9500);
			}
		}

		/* Ressourcenpriorit�t */
		if (resourcePriority!=null && !resourcePriority.isBlank()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ResourcePriority"),resourcePriority,10000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ResourcePriority"),DEFAULT_RESOURCE_PRIORITY,10000);
		}

		/* Kosten */
		if (costs!=null && !costs.isBlank() && ! costs.trim().equals("0")) descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Costs.PerClient"),costs,20000);
		if (costsPerProcessSecond!=null && !costsPerProcessSecond.isBlank() && !costsPerProcessSecond.trim().equals("0")) descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Costs.PerProcessSecond"),costsPerProcessSecond,20000);
		if (costsPerPostProcessSecond!=null && !costsPerPostProcessSecond.isBlank() && !costsPerPostProcessSecond.trim().equals("0")) descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Costs.PerPostProcessSecond"),costsPerPostProcessSecond,20000);

		/* N�chste Stationen */
		descriptionBuilder.addEdgeOut(connectionOutSuccess);
		if (connectionOutCancel!=null) descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.Process.EdgeOutCancel"),connectionOutCancel);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsOut.size()>2) return false;

		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		this.connectionOutSuccess=null;
		if (connectionsOut.size()>0) this.connectionOutSuccess=connectionsOut.get(0);
		this.connectionOutCancel=null;
		if (connectionsOut.size()>1) this.connectionOutCancel=connectionsOut.get(0);

		return true;
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.process,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Batch-Gr��en */
		searcher.testInteger(this,Language.tr("Surface.Process.Dialog.MinimumBatchSize"),batchMin,newBatchMin->{if (newBatchMin>=1) batchMin=newBatchMin;});
		searcher.testInteger(this,Language.tr("Surface.Process.Dialog.MaximumBatchSize"),batchMax,newBatchMax->{if (newBatchMax>=1) batchMax=newBatchMax;});

		/* Bedienzeiten */
		working.search(searcher,this,Language.tr("Surface.Process.Dialog.Tab.ProcessingTimes"));

		/* Nachbearbeitungszeiten */
		postProcessing.search(searcher,this,Language.tr("Surface.Process.Dialog.Tab.PostProcessingTimes"));

		/* Wartezeittoleranzen */
		cancel.search(searcher,this,Language.tr("Surface.Process.Dialog.Tab.WaitingTimeTolerances"));

		/* R�stzeiten */
		setupTimes.search(searcher,this,Language.tr("Surface.Process.Dialog.Tab.SetupTimes"));

		/* Priorit�ten */
		for (Map.Entry<String,String> clientPriority: priority.entrySet()) {
			final String clientType=clientPriority.getKey();
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.PriorityForClientType"),clientType),clientPriority.getValue(),newPriority->priority.put(clientType,newPriority));
		}

		/* Ressourcenzuordnung -> keine Suche */

		/* Ressorcen-Priorisierungs-Formel */
		searcher.testString(this,Language.tr("Surface.Process.Dialog.ResourcePriority"),resourcePriority,newResourcePriority->{resourcePriority=newResourcePriority;});

		/* Kosten pro Bedienvorgang */
		searcher.testString(this,Language.tr("Surface.Process.Dialog.CostsPerClient"),costs,newCosts->{costs=newCosts;});

		/* Kosten pro Bediensekunde */
		searcher.testString(this,Language.tr("Surface.Process.Dialog.CostsPerProcessingSecond"),costsPerProcessSecond,newCostsPerProcessSecond->{costsPerProcessSecond=newCostsPerProcessSecond;});

		/* Kosten pro Nachbearbeitungssekunde */
		searcher.testString(this,Language.tr("Surface.Process.Dialog.CostsPerPostProcessingSecond"),costsPerPostProcessSecond,newCostsPerPostProcessSecond->{costsPerPostProcessSecond=newCostsPerPostProcessSecond;});
	}

	@Override
	public Map<String,Integer> getUsedResourcesInfo() {
		final Map<String,Integer> map=new HashMap<>();

		for (Map<String,Integer> alternative: resources) {
			for (Map.Entry<String,Integer> entry: alternative.entrySet()) {
				map.compute(entry.getKey(),(key,value)->(value==null)?entry.getValue():value+entry.getValue());
			}
		}

		return map;
	}

	@Override
	public void addResourceUsage(final String resourceName, final int neededNumber) {
		/* Leere Alternative verwenden */
		for (Map<String,Integer> alternative: resources) if (alternative.size()==0) {
			alternative.put(resourceName,neededNumber);
			return;
		}

		/* Oder neue Alternative anlegen */
		final Map<String,Integer> alternative=createNewResourceMap();
		alternative.put(resourceName,neededNumber);
		resources.add(alternative);
	}
}