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
package ui.modeleditor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;

import language.Language;
import net.dde.DDEConnect;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.*;

/**
 * Katalog, an dem sich alle von {@link ModelElement} abgeleiteten final nutzbaren Modell-Elemente registrieren sollen.
 * @author Alexander Herzog
 * @see ModelElement
 * @see #getCatalog()
 */
public final class ModelElementCatalog {
	/**
	 * Für welche Betriebsart sollen die Elemente angezeigt werden?
	 * @author Alexander Herzog
	 * @see ModelElementCatalog#mode
	 */
	public enum Mode {
		/** Betriebsart: Vollständiger Simulator */
		FULL,
		/** Betriebsart: Player */
		PLAYER
	}

	/**
	 * Für welche Betriebsart sollen die Elemente angezeigt werden?
	 * @see Mode
	 */
	public static Mode mode=Mode.FULL;

	/**
	 * Singleton-Instanz von {@link ModelElementCatalog}
	 * @see #getCatalog()
	 */
	private static ModelElementCatalog catalog;

	/**
	 * Zuordnung der Elemente zu ihren Namen
	 * @see #getMenuNames()
	 */
	private Map<String,ModelElementPosition> elementsAdd;

	/**
	 * Zuordnung der Elemente zu ihren xml-Namen
	 * @see #getXMLElement(String)
	 */
	private Map<String,ModelElement> elementsLoad;


	/**
	 * Alle Elemente nach Gruppen sortiert
	 * @see #getGroupCount()
	 * @see #getAll()
	 */
	private Map<String,Map<String,ModelElementPosition>> elementsGroups;

	/** Bezeichner für die Elementenvorlagengruppe "Eingang/Ausgang" */
	public static String GROUP_INPUTOUTPUT="Eingang/Ausgang";
	/** Bezeichner für die Elementenvorlagengruppe "Verarbeitung" */
	public static String GROUP_PROCESSING="Verarbeitung";
	/** Bezeichner für die Elementenvorlagengruppe "Kunden verbinden" */
	public static String GROUP_BATCH="Kunden verbinden";
	/** Bezeichner für die Elementenvorlagengruppe "Zuweisungen" */
	public static String GROUP_ASSIGN="Zuweisungen";
	/** Bezeichner für die Elementenvorlagengruppe "Verzweigungen" */
	public static String GROUP_BRANCH="Verzweigungen";
	/** Bezeichner für die Elementenvorlagengruppe "Schranken" */
	public static String GROUP_BARRIER="Schranken";
	/** Bezeichner für die Elementenvorlagengruppe "Transport" */
	public static String GROUP_TRANSPORT="Transport";
	/** Bezeichner für die Elementenvorlagengruppe "Daten Ein- und Ausgabe" */
	public static String GROUP_DATAINPUTOUTPUT="Daten Ein- und Ausgabe";
	/** Bezeichner für die Elementenvorlagengruppe "Flusslogik" */
	public static String GROUP_LOGIC="Flusslogik";
	/** Bezeichner für die Elementenvorlagengruppe "Analoge Werte" */
	public static String GROUP_ANALOG="Analoge Werte";
	/** Bezeichner für die Elementenvorlagengruppe "Animation" */
	public static String GROUP_ANIMATION="Animation";
	/** Bezeichner für die Elementenvorlagengruppe "Animation - Interaktiv" */
	public static String GROUP_INTERACTIVE="Animation - Interaktiv";
	/** Bezeichner für die Elementenvorlagengruppe "Optische Gestaltung" */
	public static String GROUP_DECORATION="Optische Gestaltung";
	/** Bezeichner für die Elementenvorlagengruppe "Sonstiges" */
	public static String GROUP_OTHERS="Sonstiges";

	/** Reihenfolge der Gruppen in der Vorlagenleiste */
	public static String[] GROUP_ORDER=new String[]{GROUP_INPUTOUTPUT,GROUP_PROCESSING,GROUP_ASSIGN,GROUP_BRANCH,GROUP_BARRIER,GROUP_BATCH,GROUP_TRANSPORT,GROUP_DATAINPUTOUTPUT,GROUP_LOGIC,GROUP_ANALOG,GROUP_ANIMATION,GROUP_DECORATION,GROUP_OTHERS};

	/**
	 * Der Elemente-Katalog ist ein Singleton. Es können keine Instanzen angelegt werden.
	 * Es muss stattdessen die Methode <code>getCatalog</code> verwendet werden.
	 * @see #getCatalog()
	 */
	private ModelElementCatalog() {
		elementsAdd=new HashMap<>();
		elementsLoad=new HashMap<>();
		elementsGroups=new HashMap<>();
		initCatalog();
	}

	/**
	 * Initialisiert den Elementvorlagen-Katalog.
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean initCatalog() {
		final ExecutorService executor=new ThreadPoolExecutor(0,Runtime.getRuntime().availableProcessors(),5000,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(),new ThreadFactory() {
			private final AtomicInteger threadNumber=new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"Catalog Builder "+threadNumber.getAndIncrement());
			}
		});

		final List<FutureTask<Integer>> tasks=new ArrayList<>();
		FutureTask<Integer> task;

		/* Eingang/Ausgang */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementSource(null,null),null,GROUP_INPUTOUTPUT);
				addElement(new ModelElementSourceMulti(null,null),null,GROUP_INPUTOUTPUT);
				if (mode==Mode.FULL) {
					addElement(new ModelElementSourceTable(null,null),null,GROUP_INPUTOUTPUT);
					addElement(new ModelElementSourceDB(null,null),null,GROUP_INPUTOUTPUT);
					if (new DDEConnect().available()) addElement(new ModelElementSourceDDE(null,null),null,GROUP_INPUTOUTPUT); else addElementHidden(new ModelElementSourceDDE(null,null),null);
				}
				addElement(new ModelElementDispose(null,null),null,GROUP_INPUTOUTPUT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Verarbeitung */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementProcess(null,null),null,GROUP_PROCESSING);
				addElement(new ModelElementDelay(null,null),null,GROUP_PROCESSING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Zuweisungen */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementAssign(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementAssignString(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementCosts(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementSet(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementSetJS(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementCounter(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementCounterMulti(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementThroughput(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementCounterBatch(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementStateStatistics(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementDifferentialCounter(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementSectionStart(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementSectionEnd(null,null),null,GROUP_ASSIGN);
				addElement(new ModelElementSetStatisticsMode(null,null),null,GROUP_ASSIGN);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Verzweigungen */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementDuplicate(null,null),null,GROUP_BRANCH);
				addElement(new ModelElementDecide(null,null),null,GROUP_BRANCH);
				addElement(new ModelElementDecideJS(null,null),null,GROUP_BRANCH);
				addElement(new ModelElementBalking(null,null),null,GROUP_BRANCH);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Schranken */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementHold(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementHoldMulti(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementHoldJS(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementSignal(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementBarrier(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementBarrierPull(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementSeize(null,null),null,GROUP_BARRIER);
				addElement(new ModelElementRelease(null,null),null,GROUP_BARRIER);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Kunden verbinden */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementBatch(null,null),null,GROUP_BATCH);
				addElement(new ModelElementBatchMulti(null,null),null,GROUP_BATCH);
				addElement(new ModelElementSeparate(null,null),null,GROUP_BATCH);
				addElement(new ModelElementMatch(null,null),null,GROUP_BATCH);
				addElement(new ModelElementPickUp(null,null),null,GROUP_BATCH);
				addElement(new ModelElementSplit(null,null),null,GROUP_BATCH);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Transport */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementTransportSource(null,null),null,GROUP_TRANSPORT);
				addElement(new ModelElementTransportTransporterSource(null,null),null,GROUP_TRANSPORT);
				addElementHidden(new ModelElementTransportSourceRouter(null,null),null);
				addElement(new ModelElementTransportDestination(null,null),null,GROUP_TRANSPORT);
				addElement(new ModelElementTransportParking(null,null),null,GROUP_TRANSPORT);
				addElement(new ModelElementAssignSequence(null,null),null,GROUP_TRANSPORT);
				addElement(new ModelElementWayPoint(null,null),Language.tr("Surface.WayPoint.Name"),GROUP_TRANSPORT);
				addElement(new ModelElementTeleportSource(null,null),null,GROUP_TRANSPORT);
				addElement(new ModelElementTeleportDestination(null,null),null,GROUP_TRANSPORT);
				addElement(new ModelElementConveyor(null,null),null,GROUP_TRANSPORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Daten Ein- und Ausgabe */
		task=new FutureTask<>(()->{
			if (mode==Mode.FULL) {
				try {
					addElement(new ModelElementInput(null,null),null,GROUP_DATAINPUTOUTPUT);
					addElement(new ModelElementInputJS(null,null),null,GROUP_DATAINPUTOUTPUT);
					addElement(new ModelElementInputDB(null,null),null,GROUP_DATAINPUTOUTPUT);
					if (new DDEConnect().available()) addElement(new ModelElementInputDDE(null,null),null,GROUP_DATAINPUTOUTPUT); else addElementHidden(new ModelElementInputDDE(null,null),null);
					addElement(new ModelElementOutput(null,null),null,GROUP_DATAINPUTOUTPUT);
					addElement(new ModelElementOutputJS(null,null),null,GROUP_DATAINPUTOUTPUT);
					addElement(new ModelElementOutputDB(null,null),null,GROUP_DATAINPUTOUTPUT);
					if (new DDEConnect().available()) addElement(new ModelElementOutputDDE(null,null),null,GROUP_DATAINPUTOUTPUT); else addElementHidden(new ModelElementOutputDDE(null,null),null);
					addElement(new ModelElementOutputLog(null,null),null,GROUP_DATAINPUTOUTPUT);
					addElement(new ModelElementRecord(null,null),null,GROUP_DATAINPUTOUTPUT);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Flusslogik */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementLogicIf(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicElseIf(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicElse(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicEndIf(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicWhile(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicEndWhile(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicDo(null,null),null,GROUP_LOGIC);
				addElement(new ModelElementLogicUntil(null,null),null,GROUP_LOGIC);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Analoge Werte */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementAnalogValue(null,null),null,GROUP_ANALOG);
				addElement(new ModelElementAnalogAssign(null,null),null,GROUP_ANALOG);
				addElement(new ModelElementTank(null,null),null,GROUP_ANALOG);
				addElement(new ModelElementTankFlowByClient(null,null),null,GROUP_ANALOG);
				addElement(new ModelElementTankFlowBySignal(null,null),null,GROUP_ANALOG);
				addElement(new ModelElementTankSensor(null,null),null,GROUP_ANALOG);
				addElement(new ModelElementTankValveSetup(null,null),null,GROUP_ANALOG);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Animation */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementClientIcon(null,null),null,GROUP_ANIMATION);
				addElement(new ModelElementAnimationTextValue(null,null),Language.tr("Surface.AnimationText.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationTextValueJS(null,null),Language.tr("Surface.AnimationTextJS.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationTextSelect(null,null),Language.tr("Surface.AnimationTextSelect.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationBar(null,null),Language.tr("Surface.AnimationBar.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationBarStack(null,null),Language.tr("Surface.AnimationBarStack.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationLCD(null,null),Language.tr("Surface.AnimationLCD.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationTrafficLights(null,null),Language.tr("Surface.AnimationTrafficLights.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationLineDiagram(null,null),Language.tr("Surface.AnimationDiagram.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationBarChart(null,null),Language.tr("Surface.AnimationBarChart.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationPieChart(null,null),Language.tr("Surface.AnimationPieChart.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationClock(null,null),Language.tr("Surface.AnimationClock.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationImage(null,null),Language.tr("Surface.AnimationImage.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationRecord(null,null),Language.tr("Surface.AnimationRecord.Name"),GROUP_ANIMATION);
				addElement(new ModelElementAnimationPointerMeasuring(null,null),Language.tr("Surface.AnimationPointerMeasuring.Name"),GROUP_ANIMATION);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Animation - Interaktiv */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementInteractiveButton(null,null),Language.tr("Surface.InteractiveButton.Name"),GROUP_INTERACTIVE);
				addElement(new ModelElementInteractiveSlider(null,null),Language.tr("Surface.InteractiveSlider.Name"),GROUP_INTERACTIVE);
				addElement(new ModelElementInteractiveCheckbox(null,null),Language.tr("Surface.InteractiveCheckbox.Name"),GROUP_INTERACTIVE);
				addElement(new ModelElementInteractiveRadiobutton(null,null),Language.tr("Surface.InteractiveRadiobutton.Name"),GROUP_INTERACTIVE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Sonstiges */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementUserStatistic(null,null),null,GROUP_OTHERS);
				addElement(new ModelElementAction(null,null),null,GROUP_OTHERS);
				addElement(new ModelElementSub(null,null),null,GROUP_OTHERS);
				addElement(new ModelElementSubIn(null,null,0,0));
				addElement(new ModelElementSubOut(null,null,0,0));
				addElement(new ModelElementReference(null,null),null,GROUP_OTHERS);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		/* Optische Gestaltung */
		task=new FutureTask<>(()->{
			try {
				addElement(new ModelElementText(null,null),Language.tr("Surface.Text.Name.Long"),GROUP_DECORATION);
				addElement(new ModelElementVertex(null,null),Language.tr("Surface.Vertex.Name"),GROUP_DECORATION);
				addElement(new ModelElementLine(null,null),Language.tr("Surface.Line.Name"),GROUP_DECORATION);
				addElement(new ModelElementRectangle(null,null),Language.tr("Surface.Rectangle.Name"),GROUP_DECORATION);
				addElement(new ModelElementEllipse(null,null),Language.tr("Surface.Ellipse.Name"),GROUP_DECORATION);
				addElement(new ModelElementImage(null,null),Language.tr("Surface.Image.Name"),GROUP_DECORATION);
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0);
		executor.execute(task);
		tasks.add(task);

		boolean ok=true;
		for (int i=0;i<tasks.size();i++) try {
			tasks.get(i).get();
		} catch (ExecutionException | InterruptedException e) {ok=false;}
		executor.shutdown();
		return ok;
	}

	/**
	 * Liefert eine Instanz des {@link ModelElementCatalog} Singletons.
	 * @return	Singleton-Instanz von {@link ModelElementCatalog}
	 */
	public static synchronized ModelElementCatalog getCatalog() {
		if (catalog==null) catalog=new ModelElementCatalog();
		return catalog;
	}

	/**
	 * Erzwingt das Neuanlegen des Elements (z.B. nach einem Sprachwechsel).
	 */
	public static void forceReinit() {
		catalog=null;
	}

	/**
	 * Fügt ein Element zu dem Katalog hinzu, aber macht es in der Liste unsichtbar.
	 * @param template	Vorlagenelement für den Katalog (konkrete Elemente werden später per <code>clone(realSurface)</code> hiervon abgeleitet).
	 * @param menuName	Name des Elements im Menü (wird <code>null</code> übergeben, so wird der Name über die <code>getTypeName()</code>-Methode ermittelt - sofern es sich um ein <code>ModelElementBox</code>-Element handelt)
	 */
	private void addElementHidden(final ModelElementPosition template, final String menuName) {
		addElement(template,menuName,null);
	}

	/**
	 * Fügt ein Element zu dem Katalog hinzu.
	 * @param template	Vorlagenelement für den Katalog (konkrete Elemente werden später per <code>clone(realSurface)</code> hiervon abgeleitet).
	 * @param menuName	Name des Elements im Menü (wird <code>null</code> übergeben, so wird der Name über die <code>getTypeName()</code>-Methode ermittelt - sofern es sich um ein <code>ModelElementBox</code>-Element handelt)
	 * @param groupName	Name der Gruppe in der Liste
	 */
	private void addElement(final ModelElementPosition template, final String menuName, final String groupName) {
		synchronized (elementsLoad) {
			for (String langname: template.getXMLNodeNames()) elementsLoad.put(langname,template);
		}

		String name=null;

		if (menuName==null) {
			if (template instanceof ModelElementBox) name=((ModelElementBox) template).getTypeName();
		} else {
			name=menuName;
		}

		if (name!=null) {
			synchronized (elementsAdd) {
				elementsAdd.put(name,template);

				if (groupName!=null) {
					Map<String,ModelElementPosition> group=elementsGroups.get(groupName);
					if (group==null) {group=new HashMap<>(); elementsGroups.put(groupName,group);}
					group.put(name,template);
				}
			}
		}
	}

	/**
	 * Fügt ein Element zu dem Katalog hinzu.
	 * @param template	Vorlagenelement für den Katalog (konkrete Elemente werden später per <code>clone(realSurface)</code> hiervon abgeleitet).
	 */
	public void addElement(final ModelElement template) {
		synchronized (elementsLoad) {
			for (String langname: template.getXMLNodeNames()) elementsLoad.put(langname,template);
		}
	}

	/**
	 * Liefert eine alphabetisch sortierte Liste aller für das Auswahlmenü verfügbaren Elemente.
	 * @return	Alphabetisch sortierte Liste aller Elemente
	 */
	public List<String> getMenuNames() {
		List<String> list=new ArrayList<>();
		list.addAll(elementsAdd.keySet());
		Collections.sort(list);
		return list;
	}

	/**
	 * Liefert ein Icon zu einem Menüpunkt für das Elemente-Auswahlmenü.
	 * @param name	Name, für den das Icon geliefert werden soll.
	 * @return	Icon oder <code>null</code>, wenn für den Menüeintrag kein Icon hinterlegt wurde.
	 * @see #getMenuNames()
	 */
	public ImageIcon getMenuIcon(final String name) {
		final ModelElementPosition element=elementsAdd.get(name);
		if (element==null) return null;
		URL imgURL=element.getAddElementIcon();
		if (imgURL==null) return null;
		return new ImageIcon(imgURL);
	}

	/**
	 * Liefert einen Tooltip zu einem Menüpunkt für das Elemente-Auswahlmenü.
	 * @param name	Name, für den der Tooltip geliefert werden soll.
	 * @return	Tooltip oder <code>null</code>, wenn für den Menüeintrag kein Tooltip hinterlegt wurde.
	 * @see #getMenuNames()
	 */
	public String getMenuToolTip(final String name) {
		final ModelElementPosition element=elementsAdd.get(name);
		if (element==null) return null;
		String tooltip=element.getToolTip();
		if (tooltip==null) tooltip=element.getContextMenuElementName();
		return tooltip;
	}

	/**
	 * Liefert die Vorlage für ein Element basierend auf dem Namen im Menüelemente-Katalog
	 * @param nameMenu	Elementenname zu dem ein Vorlagenelement geliefert werden soll
	 * @return	Vorlagenelement vom Typ <code>ModelElementPosition</code> oder <code>null</code>, wenn kein Element mit dem angegebenen Namen im Katalog existiert.
	 * @see #getMenuNames()
	 */
	public ModelElementPosition getMenuElement(final String nameMenu) {
		ModelElementPosition element=elementsAdd.get(nameMenu);
		if (element!=null) return element;

		for (Map.Entry<String,ModelElementPosition> entry: elementsAdd.entrySet())
			if (entry.getValue().getContextMenuElementName().equals(nameMenu)) return entry.getValue();

		return null;
	}

	/**
	 * Liefert die Vorlage für ein Element basierend auf dem Namen im xml-Namen-Katalog
	 * @param nameXML xml-Elementenname zu dem ein Vorlagenelement geliefert werden soll
	 * @return	Vorlagenelement vom Typ <code>ModelElement</code> oder <code>null</code>, wenn kein Element mit dem angegebenen Namen im Katalog existiert.
	 */
	public ModelElement getXMLElement(final String nameXML) {
		return elementsLoad.get(nameXML);
	}

	/**
	 * Liefert ein Liste mit Einträgen für alle Modell-Element-Vorlagen
	 * @param visibleGroups Sichtbare Gruppen; es werden nur Elemente in den Gruppen ausgegeben, die sichtbar sein sollen (kann leer oder <code>null</code> sein)
	 * @param openGroups	Ausgeklappte Gruppen; Elemente in nicht offenen Gruppen werden eingeklappt (kann leer oder <code>null</code> sein)
	 * @param filter	Filter-String; es werden nur Elemente ausgegeben, die zu dem Filter passen (kann leer oder <code>null</code> sein)
	 * @param isSubModel	Gibt an, ob die Vorlagen für ein Submodell (d.h. dann ohne das Submodell-Element) ausgegeben werden soll
	 * @return	Liste für eine Auswahlliste zum Einfügen von neuen Elementen
	 * @see #getTemplatesListModel(String, String, String, boolean)
	 */
	private List<ModelElementPosition> getTemplatesList(final String visibleGroups, final String openGroups, final String filter, final boolean isSubModel) {
		List<ModelElementPosition> list=new ArrayList<>();

		int index=0;
		for (String groupName: GROUP_ORDER) {
			if (visibleGroups!=null && visibleGroups.length()>index) {
				if (visibleGroups.charAt(index)=='0' || visibleGroups.charAt(index)=='-') {index++; continue;}
			}

			boolean showSub=true;
			if (openGroups!=null && openGroups.length()>index) {
				if (openGroups.charAt(index)=='0' || openGroups.charAt(index)=='-') showSub=false;
			}

			Map<String,ModelElementPosition> elements=elementsGroups.get(groupName);
			if (elements!=null) {
				boolean first=true;
				String[] names=new ArrayList<>(elements.keySet()).toArray(new String[0]);
				Arrays.sort(names);

				List<ModelElement> sub=new ArrayList<>();
				for (String name: names) {
					ModelElementPosition element=elements.get(name);
					sub.add(element);
					if (filter!=null && !filter.trim().isEmpty()) {
						if (!name.toLowerCase().contains(filter.trim().toLowerCase())) continue;
					}
					if (isSubModel && !element.canAddToSub()) continue;
					if (first) {list.add(new ModelElementListGroup(index,groupName,showSub,sub)); first=false;}
					list.add(element);
				}
			}

			index++;
		}

		return list;
	}

	/**
	 * Liefert ein Listenmodell mit Einträgen für alle Modell-Element-Vorlagen
	 * @param visibleGroups Sichtbare Gruppen; es werden nur Elemente in den Gruppen ausgegeben, die sichtbar sein sollen (kann leer oder <code>null</code> sein)
	 * @param openGroups	Ausgeklappte Gruppen; Elemente in nicht offenen Gruppen werden eingeklappt (kann leer oder <code>null</code> sein)
	 * @param filter	Filter-String; es werden nur Elemente ausgegeben, die zu dem Filter passen (kann leer oder <code>null</code> sein)
	 * @param isSubModel	Gibt an, ob die Vorlagen für ein Submodell (d.h. dann ohne das Submodell-Element) ausgegeben werden soll
	 * @return	Listenmodell für eine Auswahlliste zum Einfügen von neuen Elementen
	 */
	public DefaultListModel<ModelElementPosition> getTemplatesListModel(final String visibleGroups, final String openGroups, final String filter, final boolean isSubModel) {
		final DefaultListModel<ModelElementPosition> model=new DefaultListModel<>();
		for (ModelElementPosition element: getTemplatesList(visibleGroups,openGroups,filter,isSubModel)) model.addElement(element);
		return model;
	}

	/**
	 * Liefert den Namen für ein Element in der Form, in der er im Vorlagen-Menü angezeigt werden soll
	 * (kann <code>null</code>) sein
	 * @param element	Element, für das der Name bestimmt werden soll
	 * @return	Name für das Element im Vorlagen-Menü
	 * @see #getMenuNameWithDefault(ModelElementPosition)
	 */
	public String getMenuName(final ModelElementPosition element) {
		final String className=element.getClass().getName();
		for (Map.Entry<String,ModelElementPosition> entry: elementsAdd.entrySet()) {
			if (entry.getValue().getClass().getName().equals(className)) return entry.getKey();
		}
		return null;
	}

	/**
	 * Liefert den Namen für ein Element in der Form, in der er im Vorlagen-Menü angezeigt werden soll und
	 * verwendet verschiedene Fallbacks, so dass nie <code>null</code> geliefert wird.
	 * @param element	Element, für das der Name bestimmt werden soll
	 * @return	Name für das Element im Vorlagen-Menü
	 */
	public String getMenuNameWithDefault(final ModelElementPosition element) {
		if (element==null) return "";
		String name=ModelElementCatalog.getCatalog().getMenuName(element);
		if (name==null) {
			if (element instanceof ModelElementBox) {
				name=((ModelElementBox)element).getTypeName();
			} else {
				name=element.getContextMenuElementName();
			}
		}
		return name;
	}

	/**
	 * Liefert die Anzahl an Kategoriegruppen im Vorlagen-Menü
	 * @return	Anzahl an Kategoriegruppen
	 */
	public int getGroupCount() {
		return elementsGroups.size();
	}

	/**
	 * Liefert den gesamten Katalog
	 * @return	Gesamter Katalog
	 */
	public Map<String,Map<String,ModelElementPosition>> getAll() {
		return elementsGroups;
	}
}