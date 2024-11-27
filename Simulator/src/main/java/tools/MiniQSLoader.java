/**
 * Copyright 2023 Alexander Herzog
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
package tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.LogNormalDistributionImpl;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelResource;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.AnimationExpression;
import ui.modeleditor.elements.BatchRecord;
import ui.modeleditor.elements.BatchRecord.BatchMode;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementBarrier;
import ui.modeleditor.elements.ModelElementBarrierSignalOption;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementDuplicate;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSeparate;
import ui.modeleditor.elements.ModelElementSet;
import ui.modeleditor.elements.ModelElementSignal;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementText;

/**
 * Erlaubt das Laden von Modellen aus dem "Mini Warteschlangensimulator"
 * <a href="https://a-herzog.github.io/MiniWarteschlangensimulator">https://a-herzog.github.io/MiniWarteschlangensimulator</a>
 * @author Alexander Herzog
 */
public class MiniQSLoader {
	/**
	 * Zu ladende json-Datei
	 */
	private final File file;

	/**
	 * Liste der Datensätze zur Kombination der json-Daten mit den Modelldaten
	 */
	private List<ElementData> list;

	/**
	 * Erstelltes Modell
	 * @see #getModelRootElement()
	 */
	private EditModel model;

	/**
	 * Konstruktor der Klasse
	 * @param file	Zu ladende json-Datei
	 */
	public MiniQSLoader(final File file) {
		this.file=file;
		model=null;
	}

	/**
	 * Lädt die json-Datei und interpretiert die json-Daten.
	 * @return	JSON-Basisobjekt oder <code>null</code>, wenn die Daten nicht geladen werden konnten
	 */
	private JSONObject loadJSON() {
		if (!file.isFile()) return null;
		if (!file.getName().endsWith(".json")) return null;

		String content=Table.loadTextFromFile(file);
		if (content==null) return null;

		content=content.trim();
		if (content.length()<=2) return null;
		if (content.charAt(0)!='{' || content.charAt(content.length()-1)!='}') return null;

		try {
			return new JSONObject(content);
		} catch (JSONException e) {
			return null;
		}
	}

	/**
	 * Lädt ein Modellelement aus einem json-Datensatz
	 * @param element	json-Datensatz
	 */
	private void loadElement(final JSONObject element) {
		final ElementData data=new ElementData(element);
		if (!data.load(model)) return;

		for (var modelElement: data.modelElements) {
			modelElement.setId(model.surface.getNextFreeId());
			model.surface.add(modelElement);
		}

		for (int i=1;i<data.modelElements.length;i++) {
			final ModelElementEdge edge=new ModelElementEdge(model,model.surface,data.modelElements[i-1],data.modelElements[i]);
			model.surface.add(edge);
			data.modelElements[i-1].addEdgeOut(edge);
			data.modelElements[i].addEdgeIn(edge);
		}

		list.add(data);
	}

	/**
	 * Lädt eine Kante aus einem json-Datensatz
	 * @param element	json-Datensatz
	 */
	private void loadEdge(final JSONObject element) {
		if (!element.has("boxId1")) return;
		if (!element.has("boxId2")) return;

		Object obj;

		obj=element.get("boxId1");
		if (!(obj instanceof String)) return;
		final String boxId1=(String)obj;
		final ModelElementPosition box1=list.stream().filter(data->data.boxId.equals(boxId1)).map(data->data.getLastElement()).findFirst().orElseGet(()->null);

		obj=element.get("boxId2");
		if (!(obj instanceof String)) return;
		final String boxId2=(String)obj;
		final ModelElementPosition box2=list.stream().filter(data->data.boxId.equals(boxId2)).map(data->data.getFirstElement()).findFirst().orElseGet(()->null);

		if (box1!=null && box2!=null) {
			final ModelElementEdge edge=new ModelElementEdge(model,model.surface,box1,box2);
			model.surface.add(edge);
			box1.addEdgeOut(edge);
			box2.addEdgeIn(edge);
		}
	}

	/**
	 * Lädt die json-Datei.
	 * @return	Gibt an, ob das Laden erfolgreich war.
	 */
	public boolean load() {
		final JSONObject root=loadJSON();
		if (root==null) return false;

		list=new ArrayList<>();
		model=new EditModel();

		if (root.has("elements")) {
			final Object obj=root.get("elements");
			if (obj instanceof JSONArray) for (Object element: (JSONArray)obj) if (element instanceof JSONObject) loadElement((JSONObject)element);
		}
		if (root.has("edges")) {
			final Object obj=root.get("edges");
			if (obj instanceof JSONArray) for (Object edge: (JSONArray)obj) if (edge instanceof JSONObject) loadEdge((JSONObject)edge);
		}

		for (ElementData element: list) element.init(list);

		return true;
	}

	/**
	 * Liefert das XML-Wurzelelement des geladenen Modells.
	 * @return	XML-Wurzelelement des geladenen Modells oder <code>null</code>, wenn das Modell nicht geladen werden konnte
	 */
	public Element getModelRootElement() {
		if (model==null) return null;
		final Document doc=model.saveToXMLDocument();
		return doc.getDocumentElement();
	}

	/**
	 * Datensatz-Element zur Kombination der json-Daten mit den Modelldaten
	 */
	private static class ElementData {
		/**
		 * json-Daten
		 */
		private final JSONObject element;

		/**
		 * ID des Elements
		 */
		private int id;

		/**
		 * Box-ID des Elements
		 */
		public String boxId;

		/**
		 * Name des Elements
		 */
		private String name;

		/**
		 * Typ des Elements
		 */
		public String type;

		/**
		 * Nummer des Elements
		 */
		public int nr;

		/**
		 * Aus den json-Daten generierte Modellelemente
		 * @see #load(EditModel)
		 */
		public ModelElementPosition[] modelElements;

		/**
		 * Konstruktor
		 * @param element	json-Daten
		 */
		public ElementData(final JSONObject element) {
			this.element=element;
		}

		/**
		 * Liefert das erstes Modellelement aus der Reihe der Modellelemente, die in Summe das json-Element repräsentieren.
		 * @return	Erstes Modellelement aus der Reihe der Modellelemente, die in Summe das json-Element repräsentieren
		 */
		public ModelElementPosition getFirstElement() {
			return modelElements[0];
		}

		/**
		 * Liefert das letzte Modellelement aus der Reihe der Modellelemente, die in Summe das json-Element repräsentieren.
		 * @return	Letzte Modellelement aus der Reihe der Modellelemente, die in Summe das json-Element repräsentieren
		 */
		public ModelElementPosition getLastElement() {
			return modelElements[modelElements.length-1];
		}

		/**
		 * Lädt einen nicht-negativen int-Wert aus einem json-Objekt.
		 * @param setup	json-Objekt aus dem der Wert geladen werden soll
		 * @param key	Schlüssel unter dem der Wert abgelegt ist
		 * @return	Liefert im Erfolgsfall den Wert, sonst -1
		 */
		private int loadInt(final JSONObject setup, final String key) {
			if (!setup.has(key)) return -1;

			final Object obj=setup.get(key);
			if (obj instanceof Integer) return ((Integer)obj).intValue();
			if (obj instanceof String) {
				final Integer I=NumberTools.getInteger((String)obj);
				if (I==null) return -1;
				return I.intValue();
			}
			return -1;
		}

		/**
		 * Lädt einen nicht-negativen int-Wert aus dem json-Basisobjekt.
		 * @param key	Schlüssel unter dem der Wert abgelegt ist
		 * @return	Liefert im Erfolgsfall den Wert, sonst -1
		 */
		private int loadInt(final String key) {
			return loadInt(element,key);
		}

		/**
		 * Lädt einen nicht-negativen double-Wert aus einem json-Objekt.
		 * @param setup	json-Objekt aus dem der Wert geladen werden soll
		 * @param key	Schlüssel unter dem der Wert abgelegt ist
		 * @return	Liefert im Erfolgsfall den Wert, sonst -1
		 */
		private double loadDouble(final JSONObject setup, final String key) {
			if (!setup.has(key)) return -1;

			final Object obj=setup.get(key);
			if (obj instanceof Integer) return ((Integer)obj).intValue();
			if (obj instanceof Double) return ((Double)obj).doubleValue();
			if (obj instanceof Float) return ((Float)obj).doubleValue();
			if (obj instanceof String) {
				final Double D=NumberTools.getDouble((String)obj);
				if (D==null) return -1;
				return D;
			}
			return -1;
		}

		/**
		 * Lädt einen String aus einem json-Objekt.
		 * @param setup	json-Objekt aus dem der Wert geladen werden soll
		 * @param key	Schlüssel unter dem der Wert abgelegt ist
		 * @return	Liefert im Erfolgsfall den Wert, sonst <code>null</code>
		 */
		private String loadString(final JSONObject setup, final String key) {
			if (!setup.has(key)) return null;

			final Object obj=setup.get(key);
			if (obj instanceof Integer) return ""+((Integer)obj).intValue();
			if ((obj instanceof String)) return (String)obj;
			return null;
		}

		/**
		 * Lädt einen String aus einem json-Basisobjekt.
		 * @param key	Schlüssel unter dem der Wert abgelegt ist
		 * @return	Liefert im Erfolgsfall den Wert, sonst <code>null</code>
		 */
		private String loadString(final String key) {
			return loadString(element,key);
		}

		/**
		 * Lädt einen boolschen Wert aus einem json-Objekt.
		 * @param setup	json-Objekt aus dem der Wert geladen werden soll
		 * @param key	Schlüssel unter dem der Wert abgelegt ist
		 * @param defaultValue	Wert der zurückgegeben werden soll, wenn der Schlüssel in dem json-Objekt nicht existiert
		 * @return	Boolscher Wert
		 */
		private boolean loadBoolean(final JSONObject setup, final String key, final boolean defaultValue) {
			if (!setup.has(key)) return defaultValue;

			final Object obj=setup.get(key);
			if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
			if (obj instanceof Integer) {
				final int i=((Integer)obj).intValue();
				if (defaultValue) {
					if (i==0) return false;
				} else {
					if (i!=0) return true;
				}
				return defaultValue;
			}
			if (obj instanceof String) {
				final Integer I=NumberTools.getInteger((String)obj);
				if (I==null) return defaultValue;
				if (defaultValue) {
					if (I.intValue()==0) return false;
				} else {
					if (I.intValue()!=0) return true;
				}
				return defaultValue;
			}
			return defaultValue;

		}

		/**
		 * Liefert (sofern vorhanden) das Einstellungen-Unterobjekt des aktuellen json-Objektes.
		 * @return	Einstellungen-json-Unterobjekt (oder <code>null</code>, wenn nicht vorhanden)
		 */
		private JSONObject getSetup() {
			if (!element.has("setup")) return null;
			final Object obj=element.get("setup");
			if (!(obj instanceof JSONObject)) return null;
			return (JSONObject)obj;
		}

		/**
		 * Erzeugt eine Kundenquelle aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadSource(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final double EI=loadDouble(setup,"EI");
			final double CVI=loadDouble(setup,"CVI");
			final int b=loadInt(setup,"b");
			if (EI<=0 || CVI<0 || b<1) return null;

			final ModelElementSource element=new ModelElementSource(model,model.surface);
			final ModelElementSourceRecord record=element.getRecord();
			record.setBatchSize(""+b);
			if (CVI==1.0) {
				record.setInterarrivalTimeDistribution(new ExponentialDistribution(EI));
			} else {
				record.setInterarrivalTimeDistribution(new LogNormalDistributionImpl(EI,CVI*EI));
			}
			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Verzögerungsstation aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadDelay(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final double ES=loadDouble(setup,"ES");
			final double CVS=loadDouble(setup,"CVS");
			if (ES<=0 || CVS<0) return null;

			final ModelElementDelay element=new ModelElementDelay(model,model.surface);
			if (CVS==1.0) {
				element.setDelayTime(new ExponentialDistribution(ES),null);
			} else {
				element.setDelayTime(new LogNormalDistributionImpl(ES,CVS*ES),null);
			}
			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Bedienstation aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadProcess(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final double ES=loadDouble(setup,"ES");
			final double CVS=loadDouble(setup,"CVS");
			final int b=loadInt(setup,"b");
			final int c=loadInt(setup,"c");
			final int policy=loadInt(setup,"policy");
			if (ES<=0 || CVS<0 || b<1 || c<1) return null;

			final List<ModelElementBox> elements=new ArrayList<>();

			final ModelElementProcess element=new ModelElementProcess(model,model.surface);
			if (policy==2 || policy==-2) {
				element.getWorking().set("ClientData(1)");
			} else {
				if (CVS==1.0) {
					element.getWorking().set(new ExponentialDistribution(ES));
				} else {
					element.getWorking().set(new LogNormalDistributionImpl(ES,CVS*ES));
				}
			}
			element.setBatchMinimum(b);
			element.setBatchMaximum(b);
			element.getNeededResources().get(0).put("Operators "+name,1);
			element.setName(name);

			model.resources.add(new ModelResource("Operators "+name,1));

			elements.add(element);

			if (policy==2 || policy==-2) {
				final ModelElementSet set=new ModelElementSet(model,model.surface);
				String expression="";
				if (CVS==1.0) {
					expression="ExpDist("+NumberTools.formatNumberMax(ES)+")";
				} else {
					expression="LogNormalDist("+NumberTools.formatNumberMax(ES)+";"+NumberTools.formatNumberMax(CVS*ES)+")";
				}
				set.getRecord().setData(new String[]{"ClientData(1)"},new String[]{expression});
				set.setName("S");
				elements.add(0,set);
			}

			return elements.toArray(ModelElementBox[]::new);
		}

		/**
		 * Nachgelagerte Initialisierung für eine Bedienstation
		 * @param elements	Liste mit allen json-Elementen
		 * @param stations	Liste mit allen Modellstationen
		 * @see #init(List)
		 */
		private void initProcess(final List<ElementData> elements, final List<ModelElement> stations) {
			final JSONObject setup=getSetup();

			final double EWT=loadDouble(setup,"EWT");
			final double CVWT=loadDouble(setup,"CVWT");
			final int policy=loadInt(setup,"policy");
			final String successNextBox=loadString(setup,"SuccessNextBox");

			final ModelElementProcess process=(ModelElementProcess)getLastElement();

			/* Bedienreihenfolge */
			if (policy!=1) {
				String priority="";
				switch (policy) {
				case 0:
					priority="random()";
					break;
				case -1:
					priority="-w";
					break;
				case 2:
					priority="-ClientData(1)";
					break;
				case -2:
					priority="ClientData(1)";
					break;
				}
				for (String clientType: stations.stream().filter(station->station instanceof ModelElementSource).map(source->source.getName()).toArray(String[]::new)) {
					process.setPriority(clientType,priority);
				}
			}

			/* Warteabbrecher */
			if (EWT>0 && CVWT>=0 && successNextBox!=null && !successNextBox.trim().isEmpty() && process.getEdgeOutSuccess()!=null && process.getEdgeOutCancel()!=null) {
				/* Ausgänge in der richtigen Reihenfolge angeordnet? */
				if (!process.getEdgeOutSuccess().getConnectionEnd().getName().equals(successNextBox)) {
					/* Umsortieren */
					initProcessSwapEdgesOut(process);
				}
				/* Wartezeittoleranz einstellen */
				if (CVWT==1.0) {
					process.getCancel().set(new ExponentialDistribution(EWT));
				} else {
					process.getCancel().set(new LogNormalDistributionImpl(EWT,CVWT*EWT));
				}
			}
		}

		/**
		 * Vertaucht die auslaufenden Kanten für Erfolg und Warteabbruch bei einer Bedienstation.
		 * @param process	Bedienstation bei der die auslaufenden Kanten vertauscht werden sollen
		 * @see #initProcess(List, List)
		 */
		private void initProcessSwapEdgesOut(final ModelElementProcess process) {
			final ModelElementEdge newCancelEdge=process.getEdgeOutSuccess();
			final ModelElementEdge newSuccessEdge=process.getEdgeOutCancel();
			process.removeConnectionNotify(newCancelEdge);
			process.removeConnectionNotify(newSuccessEdge);
			process.addEdgeOut(newSuccessEdge);
			process.addEdgeOut(newCancelEdge);
		}

		/**
		 * Erzeugt eine Verzweigenstation aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadDecide(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final int mode=loadInt(setup,"mode");
			final String rates=loadString(setup,"rates");
			if (mode<0 || mode>2) return null;

			final ModelElementDecide element=new ModelElementDecide(model,model.surface);
			switch (mode) {
			case 0: element.setMode(ModelElementDecide.DecideMode.MODE_CHANCE); break;
			case 1: element.setMode(ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION); break;
			case 2: element.setMode(ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION); break;
			}
			if (mode==0) {
				if (rates==null) return null;
				element.getRates().addAll(Arrays.asList(rates.split(";")));
			}
			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Duplizierenstation aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadDuplicate(final EditModel model) {
			final ModelElementDuplicate element=new ModelElementDuplicate(model,model.surface);

			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Zählerstation aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadCounter(final EditModel model) {
			final ModelElementCounter element=new ModelElementCounter(model,model.surface);

			element.setName(name);
			element.setGroupName(Language.tr("Editor.ImportWebQS.CounterGroup"));

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt einen Ausgang aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadDispose(final EditModel model) {
			final ModelElementDispose element=new ModelElementDispose(model,model.surface);

			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Batch-Station aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadBatch(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final int b=loadInt(setup,"b");
			if (b<1) return null;

			final ModelElementBatch element=new ModelElementBatch(model,model.surface);
			final BatchRecord record=element.getBatchRecord();
			record.setBatchSizeMin(""+b);
			record.setBatchSizeMax(""+b);
			record.setBatchMode(BatchMode.BATCH_MODE_TEMPORARY);
			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Trennenstation aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadSeparate(final EditModel model) {
			final ModelElementSeparate element=new ModelElementSeparate(model,model.surface);

			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt ein Signal aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadSignal(final EditModel model) {
			final ModelElementSignal element=new ModelElementSignal(model,model.surface);

			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Erzeugt eine Schranke aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadBarrier(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final int release=loadInt(setup,"release");
			final int signalNr=loadInt(setup,"signal");
			final boolean storeSignals=loadBoolean(setup,"storeSignals",true);
			if (release<0 || signalNr<=0) return null;

			final ModelElementBarrier element=new ModelElementBarrier(model,model.surface);
			final ModelElementBarrierSignalOption option=new ModelElementBarrierSignalOption();
			option.setInitialClients(release);
			option.setStoreSignals(storeSignals);
			element.getOptions().add(option);
			element.setName(name);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Nachgelagerte Initialisierung für eine Schranke
		 * @param elements	Liste mit allen json-Elementen
		 * @param stations	Liste mit allen Modellstationen
		 * @see #init(List)
		 */
		private void initBarrier(final List<ElementData> elements, final List<ModelElement> stations) {
			final JSONObject setup=getSetup();
			final int signalNr=loadInt(setup,"signal");

			final ElementData signalElement=elements.stream().filter(element->element.type.equals("Signal")).filter(signal->nr==signalNr).findFirst().orElseGet(()->null);
			if (signalElement==null) return;

			final ModelElementBarrier barrier=(ModelElementBarrier)getFirstElement();
			barrier.getOptions().get(0).setSignalName(signalElement.getFirstElement().getName());
		}

		/**
		 * Erzeugt ein Liniendiagramm aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadDiagram(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;
			final double xrange=loadDouble(setup,"xrange");
			final String source=loadString(setup,"source");
			if (xrange<=0 || source==null) return null;

			final ModelElementAnimationLineDiagram element=new ModelElementAnimationLineDiagram(model,model.surface);
			element.setTimeArea((int)Math.round(xrange*3600));
			element.setName(name);
			element.setSize(new Dimension(650,250));

			return new ModelElementPosition[]{element};
		}

		/**
		 * Nachgelagerte Initialisierung für ein Liniendiagramm
		 * @param elements	Liste mit allen json-Elementen
		 * @param stations	Liste mit allen Modellstationen
		 * @see #init(List)
		 */
		private void initDiagram(final List<ElementData> elements, final List<ModelElement> stations) {
			final JSONObject setup=getSetup();

			final String source=loadString(setup,"source");

			final ElementData dataSourceElement=elements.stream().filter(element->(element.type+"-"+element.nr).equals(source)).findFirst().orElseGet(()->null);
			if (dataSourceElement==null) return;

			final ModelElementAnimationLineDiagram diagram=(ModelElementAnimationLineDiagram)getFirstElement();
			final List<Object[]> expressionData=diagram.getExpressionData();
			expressionData.add(new Object[] {
					new AnimationExpression("WIP("+dataSourceElement.getLastElement().getId()+")"),
					Double.valueOf(0),
					Double.valueOf(10),
					Color.BLUE,
					Integer.valueOf(2)
			});
			((ModelElementAnimationLineDiagram)getFirstElement()).setExpressionData(expressionData);
		}

		/**
		 * Erzeugt ein Textelement aus dem json-Basisobjekt.
		 * @param model	Übergeordnetes Modell für das neue Element
		 * @return	Liefert im Erfolgsfall das neue Element, sonst <code>null</code>
		 */
		private ModelElementPosition[] loadText(final EditModel model) {
			final JSONObject setup=getSetup();
			if (setup==null) return null;

			final String text=loadString(setup,"text");
			final int fontSize=loadInt(setup,"fontSize");
			if (text==null || fontSize<=0) return null;

			final ModelElementText element=new ModelElementText(model,model.surface);
			element.setText(text);
			element.setTextSize(fontSize);

			return new ModelElementPosition[]{element};
		}

		/**
		 * Interpretiert die json-Daten und generiert daraus ein Modellelement.
		 * @param model	Modell in das das Element integriert werden soll
		 * @return	Liefert <code>true</code>, wenn die json-Daten geladen und interpretiert werden konnten
		 */
		public boolean load(final EditModel model) {
			id=loadInt("id"); if (id<=0) return false;
			boxId=loadString("boxId"); if (boxId==null) return false;
			type=loadString("type"); if (type==null) return false;
			name=loadString("name"); if (name==null) return false;
			nr=loadInt("nr"); if (nr<=0) return false;
			final int top=loadInt("top"); if (top<=0) return false;
			int left=loadInt("left"); if (left<=0) return false;

			switch (type) {
			case "Source": modelElements=loadSource(model); break;
			case "Delay": modelElements=loadDelay(model); break;
			case "Process": modelElements=loadProcess(model); break;
			case "Decide": modelElements=loadDecide(model); break;
			case "Duplicate": modelElements=loadDuplicate(model); break;
			case "Counter": modelElements=loadCounter(model); break;
			case "Dispose": modelElements=loadDispose(model); break;
			case "Batch": modelElements=loadBatch(model); break;
			case "Separate": modelElements=loadSeparate(model); break;
			case "Signal": modelElements=loadSignal(model); break;
			case "Barrier": modelElements=loadBarrier(model); break;
			case "Text": modelElements=loadText(model); break;
			case "Diagram": modelElements=loadDiagram(model); break;
			}

			if (modelElements==null) return false;

			if (modelElements.length==1) {
				modelElements[0].setPosition(new Point(left,top));
			} else {
				left-=50;
				for (int i=0;i<modelElements.length;i++) {
					modelElements[i].setPosition(new Point(left,top));
					left+=125;
				}
			}

			return true;
		}

		/**
		 * Nachgelagerte Initialisierung des Elements (wenn alle Stationen bekannt sind und z.B. auf IDs zugegriffen werden kann).
		 * @param list	Liste aller Elemente
		 */
		public void init(final List<ElementData> list) {
			switch (type) {
			case "Process": initProcess(list,getLastElement().getSurface().getElements()); break;
			case "Barrier": initBarrier(list,getLastElement().getSurface().getElements()); break;
			case "Diagram": initDiagram(list,getLastElement().getSurface().getElements()); break;
			}
		}
	}
}
