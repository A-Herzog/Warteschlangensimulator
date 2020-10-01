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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;
import org.apache.jena.ext.com.google.common.base.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModelFixer;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
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
 * Hauptklasse für die Bearbeitung von Kunden (Warteschlange und Bedienschalter)
 * @author Alexander Herzog
 */
public class ModelElementProcess extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementEdgeMultiOut, ModelElementAnimationForceMove {
	/**
	 * Standardmäßige Priorität für Kundentypen
	 */
	public static final String DEFAULT_CLIENT_PRIORITY="w";

	/**
	 * Standardmäßige Priorität zum Bezug von Ressourcen
	 */
	public static final String DEFAULT_RESOURCE_PRIORITY="1";

	/**
	 * Art wie die Bedienzeit für die Kundenstatistik gezählt werden soll
	 * @author Alexander Herzog
	 * @see ModelElementProcess#getProcessTimeType()
	 * @see ModelElementProcess#setProcessTimeType(ProcessType)
	 */
	public enum ProcessType {
		/** Die Verarbeitungszeit soll als Wartezeit gezählt werden. */
		PROCESS_TYPE_WAITING,

		/** Die Verarbeitungszeit soll als Transferzeit gezählt werden. */
		PROCESS_TYPE_TRANSFER,

		/** Die Verarbeitungszeit soll als Bedienzeit gezählt werden. */
		PROCESS_TYPE_PROCESS,

		/** Die Verarbeitungszeit soll nicht erfasst werden. */
		PROCESS_TYPE_NOTHING
	}

	private List<ModelElementEdge> connectionsIn;
	private ModelElementEdge connectionOutSuccess, connectionOutCancel;

	/* Wird nur beim Laden und Clonen verwendet. */
	private List<Integer> connectionsInIds=null;
	private int connectionOutSuccessId=-1;
	private int connectionOutCancelId=-1;

	private ModelSurface.TimeBase timeBase;
	private ProcessType processTimeType;
	private int batchMin; /* >=1 */
	private int batchMax; /* >=batchMin */
	private DistributionSystem working;
	private DistributionSystem postProcessing;
	private DistributionSystem cancel;
	private DistributionSystemSetupTimes setupTimes;
	private final Map<String,String> priority; /*  Kundentyp, Priorität-Formel */
	private final List<Map<String,Integer>> resources; /* Name der Ressource, benötigte Anzahl */
	private String resourcePriority;
	private String costs;
	private String costsPerProcessSecond;
	private String costsPerPostProcessSecond;

	/**
	 * Konstruktor der Klasse <code>ModelElementProcess</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementProcess(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE_DOUBLE_LINE);
		connectionsIn=new ArrayList<>();

		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;
		processTimeType=ProcessType.PROCESS_TYPE_PROCESS;
		batchMin=1;
		batchMax=1;

		/* Um sicher zu stellen, dass die Language-Strings auch in den Sprachdateien vorhanden sind. Der folgende DistributionSystem-Konstruktor ist kein Scan-Ziel für die Sprachdateien. */
		Language.tr("Surface.Process.XML.Distribution.ClientType");
		Language.tr("Surface.Process.XML.Distribution.Type.ProcessingTime");
		Language.tr("Surface.Process.XML.Distribution.Type.PostProcessingTime");
		Language.tr("Surface.Process.XML.Distribution.Type.CancelationTime");

		setupTimes=new DistributionSystemSetupTimes();
		working=new DistributionSystem("Surface.Process.XML.Distribution.ClientType","Surface.Process.XML.Distribution.Type.ProcessingTime",false);
		postProcessing=new DistributionSystem("Surface.Process.XML.Distribution.ClientType","Surface.Process.XML.Distribution.Type.PostProcessingTime",true);
		cancel=new DistributionSystem("Surface.Process.XML.Distribution.ClientType","Surface.Process.XML.Distribution.Type.CancelationTime",true);

		priority=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		resources=new ArrayList<>();
		resourcePriority=DEFAULT_RESOURCE_PRIORITY;

		costs="0";
		costsPerProcessSecond="0";
		costsPerPostProcessSecond="0";
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	/**
	 * Gibt die Möglichkeit, das Label an der auslaufenden Kante zu aktualisieren, nachdem sich im Element Veränderungen ergeben haben.
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
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
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
		if (((ModelElementProcess)element).timeBase!=timeBase) return false;

		/* Bedienzeit ist ... */
		if (((ModelElementProcess)element).processTimeType!=processTimeType) return false;

		/* Batch-Größe */
		if (batchMin!=process.batchMin) return false;
		if (batchMax!=process.batchMax) return false;

		/* Bedienzeiten */
		if (!working.equalsDistributionSystem(process.working)) return false;

		/* Nachbearbeitungszeiten */
		if (!postProcessing.equalsDistributionSystem(process.postProcessing)) return false;

		/* Abbruchzeiten */
		if (!cancel.equalsDistributionSystem(process.cancel)) return false;

		/* Rüstzeiten */
		if (!setupTimes.equalsDistributionSystem(process.setupTimes)) return false;

		/* Prioritäten */
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

		/* Kosten */
		if (!Objects.equal(costs,process.costs)) return false;
		if (!Objects.equal(costsPerProcessSecond,process.costsPerProcessSecond)) return false;
		if (!Objects.equal(costsPerPostProcessSecond,process.costsPerPostProcessSecond)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
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

			/* Batch-Größe */
			batchMin=process.batchMin;
			batchMax=process.batchMax;

			/* Bedienzeiten */
			working=process.working.clone();

			/* Nachbearbeitungszeiten */
			postProcessing=process.postProcessing.clone();

			/* Abbruchzeiten */
			cancel=process.cancel.clone();

			/* Rüstzeiten */
			setupTimes=process.setupTimes.clone();

			/* Prioritäten */
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

			/* Kosten */
			costs=process.costs;
			costsPerProcessSecond=process.costsPerProcessSecond;
			costsPerPostProcessSecond=process.costsPerPostProcessSecond;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
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
	 * Fügt Menüpunkte zum Hinzufügen von einlaufenden und auslaufender Kante zum Kontextmenü
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zurück, wenn Elemente in das Kontextmenü eingefügt wurden (und ggf. ein Separator vor dem nächsten Abschnitt gesetzt werden sollte)
	 */
	protected final boolean addRemoveEdgesContextMenuItems(final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();
		boolean needSeparator=false;

		if (connectionsIn!=null && connectionsIn.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesIn")));
			item.addActionListener(e->{
				for (ModelElementEdge element : new ArrayList<>(connectionsIn)) {
					surface.remove(element);
				}
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}

		if (connectionOutSuccess!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener(e->{
				if (connectionOutCancel!=null) surface.remove(connectionOutCancel);
				surface.remove(connectionOutSuccess);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}

		return needSeparator;
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
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
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

		if (costs!=null && !costs.trim().isEmpty() && ! costs.trim().equals("0")) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.StationCosts")));
			sub.setTextContent(costs);
		}

		if ((costsPerProcessSecond!=null && !costsPerProcessSecond.trim().isEmpty() && !costsPerProcessSecond.trim().equals("0")) || (costsPerPostProcessSecond!=null && !costsPerPostProcessSecond.trim().isEmpty() && !costsPerPostProcessSecond.trim().equals("0"))) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Process.XML.TimeCosts")));
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.TimeCosts.ProcessingSecond"),costsPerProcessSecond);
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.TimeCosts.PostProcessingSecond"),costsPerPostProcessSecond);
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

		if (Language.trAll("Surface.Process.XML.Priority",name)) {
			final String typ=Language.trAllAttribute("Surface.Process.XML.Distribution.ClientType",node);
			if (!typ.trim().isEmpty()) priority.put(typ,content);
			return null;
		}

		if (Language.trAll("Surface.Process.XML.Operators",name)) {
			final String type=Language.trAllAttribute("Surface.Process.XML.Operators.Group",node);
			if (!type.trim().isEmpty()) {
				Long L=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.Process.XML.Operators.Count",node));
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Process.XML.Operators.Count"),name,node.getParentNode().getNodeName());
				int count=L.intValue();
				String alt=Language.trAllAttribute("Surface.Process.XML.Operators.Alternative",node);
				if (alt.trim().isEmpty()) alt="1";
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
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * Fügt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die einlaufende Kante hinzugefügt werden konnte.
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
		return connectionsIn.toArray(new ModelElementEdge[0]);
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
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return (connectionOutSuccess==null) || (connectionOutCancel==null);
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
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
	 * Liefert die auslaufende Kante für erfolgreiche Kunden.
	 * @return	Auslaufende Kante (erfolgreiche Kunden)
	 */
	public ModelElementEdge getEdgeOutSuccess() {
		return connectionOutSuccess;
	}

	/**
	 * Liefert die auslaufende Kante für Warteabbrecher.
	 * @return	Auslaufende Kante (Warteabbrecher)
	 */
	public ModelElementEdge getEdgeOutCancel() {
		return connectionOutCancel;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_PROCESS.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Process.Tooltip");
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
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

	private static final Color defaultBackgroundColor=new Color(64,127,255);

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
			new ModelElementProcessDialog(owner,ModelElementProcess.this,readOnly);
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

		return panels.toArray(new JPanel[0]);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Popupmenüs, welches die Einträge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.CHART_NQ);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.CHART_WIP);
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
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NQ);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.PROCESS);
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
		NextStationHelper.nextStationsProcessing(this,parentMenu,addNextStation);
	}

	private boolean processHasMultiTimes() {
		if (getWorking().getNames().length>0) return true;
		if (getPostProcessing().get()!=null) return true;
		if (getCancel().get()!=null) return true;
		if (getSetupTimes()!=null) return true;
		return false;
	}

	/**
	 * Fügt optionale Menüpunkte zum direkten Aufruf der Parameterreihenfunktion in das Kontextmenü ein
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param buildSeries	Callback das zum Aktivieren der Parameterreihenfunktion aufgerufen werden soll
	 */
	@Override
	protected void addParameterSeriesMenuItem(final JPopupMenu popupMenu, final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> buildSeries) {
		JMenuItem item;
		final URL imgURL=Images.PARAMETERSERIES.getURL();

		/* Bedienzeiten (global) */
		final Object obj1=getWorking().get();
		if ((obj1 instanceof AbstractRealDistribution) && DistributionTools.canSetMean((AbstractRealDistribution)obj1)) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTime")));
			item.addActionListener(e->{
				TemplateRecord record=new TemplateRecord(TemplateMode.MODE_INTERARRIVAL,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTime.Short"));
				record.input.setMode(ModelChanger.Mode.MODE_XML);
				record.input.setXMLMode(1);
				String add="";
				if (processHasMultiTimes()) add="["+Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type")+"=\""+Language.trPrimary("Surface.Process.XML.Distribution.Type.ProcessingTime")+"\"]";
				record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
				buildSeries.accept(record);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
		}

		/* Bedienzeiten nach Kundentypen */
		final List<String> clientTypeData=new ArrayList<>();
		for (String clientType: getModel().surface.getClientTypes()) {
			final Object obj2=working.get(clientType);
			if ((obj2 instanceof AbstractRealDistribution) && DistributionTools.canSetMean((AbstractRealDistribution)obj1)) clientTypeData.add(clientType);
		}
		if (clientTypeData.size()>0) {
			final JMenu sub;
			popupMenu.add(sub=new JMenu(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTimeClientType")));
			if (imgURL!=null) sub.setIcon(new ImageIcon(imgURL));
			final URL imgURL2=Images.MODELPROPERTIES_CLIENTS.getURL();
			for (String clientType: clientTypeData) {
				final String clientTypeFinal=clientType;
				sub.add(item=new JMenuItem(clientTypeFinal));
				item.addActionListener(e->{
					TemplateRecord record=new TemplateRecord(TemplateMode.MODE_INTERARRIVAL,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeServiceTimeClientType.Short")+" - "+clientTypeFinal);
					record.input.setMode(ModelChanger.Mode.MODE_XML);
					record.input.setXMLMode(1);
					final String add="["+(working.getSubNumber(clientTypeFinal)+2)+"]";
					record.input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+getXMLNodeNames()[0]+"[id=\""+getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
					buildSeries.accept(record);
				});
				if (imgURL2!=null) item.setIcon(new ImageIcon(imgURL2));
			}
		}
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
		return Language.trAll("Surface.Process.XML.Root");
	}

	/**
	 * Liefert die minimale Batch-Größe.
	 * @return	Minimale Batch-Größe
	 */
	public int getBatchMinimum() {
		return batchMin;
	}

	/**
	 * Liefert die maximale Batch-Größe.
	 * @return	Maximale Batch-Größe
	 */
	public int getBatchMaximum() {
		return batchMax;
	}

	/**
	 * Stellt die minimale Batch-Größe ein.
	 * @param batchMin	Neue minimale Batch-Größe
	 */
	public void setBatchMinimum(final int batchMin) {
		this.batchMin=batchMin;
	}

	/**
	 * Stellt die maximale Batch-Größe ein.
	 * @param batchMax	Neue maximale Batch-Größe
	 */
	public void setBatchMaximum(final int batchMax) {
		this.batchMax=batchMax;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdrücke für die Bedienzeiten vorhält
	 * @return	Bedienzeiten
	 */
	public DistributionSystem getWorking() {
		return working;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdrücke für die Nachbearbeitungszeiten vorhält
	 * @return	Nachbearbeitungszeiten
	 */
	public DistributionSystem getPostProcessing() {
		return postProcessing;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdrücke für die Wartezeittoleranzen vorhält
	 * @return	Wartezeittoleranzen
	 */
	public DistributionSystem getCancel() {
		return cancel;
	}

	/**
	 * Liefert das Objekt, welches die Verteilungen und Ausdrücke für die Rüstzeiten vorhält
	 * @return	Rüstzeiten
	 */
	public DistributionSystemSetupTimes getSetupTimes() {
		return setupTimes;
	}

	/**
	 * Liefert die Priorität für Kunden eines bestimmten Kundentyp.
	 * @param clientType	Kundentyp für den die Priorität geliefert werden soll
	 * @return Priorität für Kunden dieses Kundentyps
	 */
	public String getPriority(final String clientType) {
		final String p=priority.get(clientType);
		if (p==null) return DEFAULT_CLIENT_PRIORITY; else return p;
	}

	/**
	 * Stellt Priorität für die Kunden eines bestimmten Kundentyps ein.
	 * @param clientType	Kundentyp für den die Priorität eingestellt werden soll
	 * @param priority	Neue Priorität für Kundes des Kundentyps
	 */
	public void setPriority(final String clientType, final String priority) {
		if (clientType==null || clientType.trim().isEmpty()) return;
		if (priority==null) this.priority.put(clientType,""); else this.priority.put(clientType,priority);
	}

	/**
	 * Liefert die Aufstellung der zur Bearbeitung von Kunden benötigten Ressourcen.<br>
	 * Es existiert immer mindestens ein Eintrag in der Liste.
	 * @return	Bedienergruppen und deren Anzahlen, die für die Bedienung der Kunden notwendig sind
	 */
	public List<Map<String,Integer>> getNeededResources() {
		if (resources.size()==0) resources.add(createNewResourceMap());
		return resources;
	}

	/**
	 * Erstellt einen neuen Eintrag für die Ressourcenliste (fügt ihn dort aber noch nicht ein)
	 * @return	Neue Ressourcenzuordnungsalternative
	 * @see #getNeededResources()
	 */
	public Map<String,Integer> createNewResourceMap() {
		return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Liefert die Formel zurück, gemäß derer die Priorisierung der Bedienstation bei der Zuweisung von verfügbaren Ressourcen erfolgen soll.
	 * @return	Aktuelle Ressorcen-Priorisierungs-Formel
	 */
	public String getResourcePriority() {
		return resourcePriority;
	}

	/**
	 * Stellt die Formel ein, gemäß derer die Priorisierung der Bedienstation bei der Zuweisung von verfügbaren Ressourcen erfolgen soll.
	 * @param newResourcePriority Neue Ressorcen-Priorisierungs-Formel
	 */
	public void setResourcePriority(final String newResourcePriority) {
		if (newResourcePriority==null || newResourcePriority.trim().isEmpty()) return;
		resourcePriority=newResourcePriority;
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
	 * Gibt an, ob die Prozesszeiten als Bedienzeiten, als Transferzeiten oder als Wartezeiten gezählt werden sollen.
	 * @return	Art der Zählung der Prozesszeiten
	 * @see ProcessType
	 */
	public ProcessType getProcessTimeType() {
		return processTimeType;
	}

	/**
	 * Stellt ein, ob die Prozesszeiten als Bedienzeiten, als Transferzeiten oder als Wartezeiten gezählt werden sollen.
	 * @param processTimeType	Art der Zählung der Prozesszeiten
	 */
	public void setProcessTimeType(final ProcessType processTimeType) {
		this.processTimeType=processTimeType;
	}

	/**
	 * Liefert die eingestellten Kosten pro Bedienvorgang in der Station
	 * @return	Kosten pro Bedienvorgang
	 */
	public String getCosts() {
		if (costs==null || costs.trim().isEmpty()) return "0";
		return costs.trim();
	}

	/**
	 * Stellt die Kosten pro Bedienvorgang in der Station ein
	 * @param costs	Kosten pro Bedienvorgang
	 */
	public void setCosts(final String costs) {
		if (costs==null || costs.trim().isEmpty()) {
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
		if (costsPerProcessSecond==null || costsPerProcessSecond.trim().isEmpty()) return "0";
		return costsPerProcessSecond.trim();
	}

	/**
	 * Stellt die Kosten pro Bediensekunde in der Station ein
	 * @param costsPerProcessSecond	Kosten pro Bediensekunde
	 */
	public void setCostsPerProcessSecond(final String costsPerProcessSecond) {
		if (costsPerProcessSecond==null || costsPerProcessSecond.trim().isEmpty()) {
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
		if (costsPerPostProcessSecond==null || costsPerPostProcessSecond.trim().isEmpty()) return "0";
		return costsPerPostProcessSecond.trim();
	}

	/**
	 * Stellt die Kosten pro Nachbearbeitungssekunde in der Station ein
	 * @param costsPerPostProcessSecond	Kosten pro Nachbearbeitungssekunde
	 */
	public void setCostsPerPostProcessSecond(final String costsPerPostProcessSecond) {
		if (costsPerPostProcessSecond==null || costsPerPostProcessSecond.trim().isEmpty()) {
			this.costsPerPostProcessSecond="0";
		} else {
			this.costsPerPostProcessSecond=costsPerPostProcessSecond.trim();
		}
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation überhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung übersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation berücksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden müssten).
	 * @return	Gibt <code>true</code> zurück, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connectionsIn.size()>0;
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
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) {
			/* Keine Kopie anlegen, wenn es bereits Daten für den neuen Namen gibt. */
			if (!getPriority(newName).equals(ModelElementProcess.DEFAULT_CLIENT_PRIORITY)) return;
			if (getWorking().nameInUse(newName)) return;
			if (getPostProcessing().nameInUse(newName)) return;
			if (getCancel().nameInUse(newName)) return;
			if (getSetupTimes().nameInUse(newName)) return;

			/* Daten übertragen */
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

	private String getResourceAvailable(final String name) {
		final ModelResource resource=getModel().resources.getNoAutoAdd(name);
		if (resource==null) return "";
		if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return " ("+String.format(Language.tr("ModelDescription.Process.Resources.Available.BySchedule"),resource.getSchedule())+")";
		final int count=resource.getCount();
		if (count<0) return " ("+Language.tr("ModelDescription.Process.Resources.Available.InfiniteNumber")+")";
		return " ("+String.format(Language.tr("ModelDescription.Process.Resources.Available.Number"),count)+")";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Bearbeitungszeit ist für den Kunden... */
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

		/* Rüstzeiten */
		if (setupTimes.isActive()) {
			final String[] clientTypeNames=setupTimes.getNames();
			for (String clientA: clientTypeNames) for (String clientB: clientTypeNames) {
				final Object obj=setupTimes.get(clientA,clientB);
				final String propertyName=String.format(Language.tr("ModelDescription.Process.SetupTime"),clientA,clientB);
				if (obj instanceof String) descriptionBuilder.addProperty(propertyName,Language.tr("ModelDescription.Process.SetupTime.Expression")+": "+(String)obj,2000);
				if (obj instanceof AbstractRealDistribution) descriptionBuilder.addProperty(propertyName,ModelDescriptionBuilder.getDistributionInfo((AbstractRealDistribution)obj),2000);
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

		/* Batch-Größen */
		if (batchMin>1) {
			final String size;
			if (batchMax>batchMin) {
				size=NumberTools.formatLong(batchMin)+".."+NumberTools.formatLong(batchMax);
			} else {
				size=NumberTools.formatLong(batchMin);
			}
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.BatchSize"),size,7000);
		}

		/* Prioritäten */
		final List<String> clientTypes=descriptionBuilder.getModel().surface.getClientTypes();
		boolean needPrioInfo=false;
		for (String clientType: clientTypes) {
			final String prio=priority.get(clientType);
			if (prio!=null && !prio.trim().isEmpty() && !prio.equals(DEFAULT_CLIENT_PRIORITY)) {needPrioInfo=true; break;}
		}
		if (needPrioInfo) for (String clientType: clientTypes) {
			final String prio=priority.get(clientType);
			if (prio!=null && !prio.trim().isEmpty()) {
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

		/* Ressourcenpriorität */
		if (resourcePriority!=null && !resourcePriority.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ResourcePriority"),resourcePriority,10000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.ResourcePriority"),DEFAULT_RESOURCE_PRIORITY,10000);
		}

		/* Kosten */
		if (costs!=null && !costs.trim().isEmpty() && ! costs.trim().equals("0")) descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Costs.PerClient"),costs,20000);
		if (costsPerProcessSecond!=null && !costsPerProcessSecond.trim().isEmpty() && !costsPerProcessSecond.trim().equals("0")) descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Costs.PerProcessSecond"),costsPerProcessSecond,20000);
		if (costsPerPostProcessSecond!=null && !costsPerPostProcessSecond.trim().isEmpty() && !costsPerPostProcessSecond.trim().equals("0")) descriptionBuilder.addProperty(Language.tr("ModelDescription.Process.Costs.PerPostProcessSecond"),costsPerPostProcessSecond,20000);

		/* Nächste Stationen */
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
}