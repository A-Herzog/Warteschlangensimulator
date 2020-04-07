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
package ui.parameterseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementTank;

/**
 * Liefert Einträge für ein Popupmenü zur Auswahl von Modellparametern
 * (z.B. zur Nutzung als Parameterreihen-Eingabeparameter)
 * @author Alexander Herzog
 * @see ParameterCompareSetupValueInputListDialog
 */
public class ParameterCompareInputValuesTemplates {
	private final EditModel model;
	private final Predicate<ParameterCompareSetupValueInput> isParameterInUse;

	/**
	 * Im Popupmenü anzubietende Einträge
	 * @author Alexander Herzog
	 * @see ParameterCompareInputValuesTemplates#getList(Set)
	 */
	public enum Mode {
		/** Anzahl an Bedienern in den einzelnen Ressourcen */
		RESOURCES,

		/** Initiale Werte für Variablen */
		VARIABLES,

		/** Zwischenankunftszeiten */
		INTERARRIVAL,

		/** Bedienzeiten */
		PROCESS,

		/** Verzögerungszeiten */
		DELAY,

		/** Analoge Werte */
		ANALOG
	}

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Modell aus dem die Daten entnommen werden
	 * @param isParameterInUse Prüft, ob der Parameter bereits verwendet wird, und bietet ihn dann nicht an.
	 */
	public ParameterCompareInputValuesTemplates(final EditModel editModel, final Predicate<ParameterCompareSetupValueInput> isParameterInUse) {
		model=editModel;
		this.isParameterInUse=(isParameterInUse==null)?input->false:isParameterInUse;
	}

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Modell aus dem die Daten entnommen werden
	 */
	public ParameterCompareInputValuesTemplates(final EditModel editModel) {
		this(editModel,input->false);
	}

	private boolean processHasMultiTimes(final ModelElementProcess process) {
		if (process.getWorking().getNames().length>0) return true;
		if (process.getPostProcessing().get()!=null) return true;
		if (process.getCancel().get()!=null) return true;
		if (process.getSetupTimes()!=null) return true;
		return false;
	}

	private boolean delayHasMultiTimes(final ModelElementDelay delay) {
		return delay.getSubDataCount()>0;
	}

	/**
	 * Liefert die Einträge für das Popupmenü
	 * @param modes	Gewählte Kategorien (<code>null</code> führt zu einem leeren Menü)
	 * @return	Einträge für das Popupmenü
	 */
	public List<Object> getList(final Set<Mode> modes) {
		if (modes==null) return new ArrayList<>();

		final List<Object> list=new ArrayList<>();
		Sub sub;
		List<Object> list2;

		/* Ressourcen */

		if (modes.contains(Mode.RESOURCES)) {
			list2=null;
			for (String name : model.resources.list()) {
				final ModelResource resource=model.resources.get(name);
				if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER) continue;
				final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
				input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.Resource"),name));
				input.setMode(ModelChanger.Mode.MODE_RESOURCE);
				input.setTag(name);
				if (isParameterInUse.test(input)) continue;
				if (list2==null) {
					sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.Resource.Title"),ModelChanger.Mode.MODE_RESOURCE);
					list.add(sub);
					list2=sub.list;
				}
				list2.add(input);
			}
		}


		/* Globale Variablen */

		if (modes.contains(Mode.VARIABLES)) {
			list2=null;
			for (String name: model.globalVariablesNames) {
				final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
				input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.GlobalVariable"),name));
				input.setMode(ModelChanger.Mode.MODE_VARIABLE);
				input.setTag(name);
				if (isParameterInUse.test(input)) continue;
				if (list2==null) {
					sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.GlobalVariable.Title"),ModelChanger.Mode.MODE_VARIABLE);
					list.add(sub);
					list2=sub.list;
				}
				list2.add(input);
			}
		}

		/* Zwischenankunftszeiten */

		if (modes.contains(Mode.INTERARRIVAL)) {
			list2=null;
			for (ModelElement element: model.surface.getElements()) {
				if (!(element instanceof ModelElementSource)) continue;
				final ModelElementSource source=(ModelElementSource)element;
				if (source.getRecord().getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) continue;
				if (!DistributionTools.canSetMean(source.getRecord().getInterarrivalTimeDistribution())) continue;
				final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
				input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.InterarrivalTime"),source.getName()+" (id="+source.getId()+")"));
				input.setMode(ModelChanger.Mode.MODE_XML);
				input.setXMLMode(1);
				input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+source.getXMLNodeNames()[0]+"[id=\""+source.getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution"));
				if (isParameterInUse.test(input)) continue;
				if (list2==null) {
					sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.InterarrivalTime.Title"),ModelChanger.Mode.MODE_XML);
					list.add(sub);
					list2=sub.list;
				}
				list2.add(input);
			}

			/* Zwischenankunftszeiten (Multi-Quellen) */

			/* list2=null; - nein, in dasselbe Untermenü */
			for (ModelElement element: model.surface.getElements()) {
				if (!(element instanceof ModelElementSourceMulti)) continue;
				final ModelElementSourceMulti source=(ModelElementSourceMulti)element;
				for (int i=0;i<source.getRecords().size();i++) {
					if (source.getRecords().get(i).getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) continue;
					if (!DistributionTools.canSetMean(source.getRecords().get(i).getInterarrivalTimeDistribution())) continue;
					final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
					input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.InterarrivalTime"),source.getName()+" (id="+source.getId()+","+source.getRecords().get(i).getName()+")"));
					input.setMode(ModelChanger.Mode.MODE_XML);
					input.setXMLMode(1);
					input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+source.getXMLNodeNames()[0]+"[id=\""+source.getId()+"\"]->"+Language.trPrimary("Surface.SourceMulti.XML.Source")+"["+(i+1)+"]->"+Language.trPrimary("Surface.Source.XML.Distribution"));
					if (isParameterInUse.test(input)) continue;
					if (list2==null) {
						sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.InterarrivalTime.Title"),ModelChanger.Mode.MODE_XML);
						list.add(sub);
						list2=sub.list;
					}
					list2.add(input);
				}
			}
		}

		/* Bedienstationen - Bedienzeiten */

		if (modes.contains(Mode.PROCESS)) {
			list2=null;
			for (ModelElement element: model.surface.getElements()) {
				if (!(element instanceof ModelElementProcess)) continue;
				final ModelElementProcess process=(ModelElementProcess)element;
				final Object obj=process.getWorking().get();
				if (obj==null || !(obj instanceof AbstractRealDistribution)) continue;
				if (!DistributionTools.canSetMean((AbstractRealDistribution)obj)) continue;
				final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
				input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.ProcessTime"),process.getName()+" (id="+process.getId()+")"));
				input.setMode(ModelChanger.Mode.MODE_XML);
				input.setXMLMode(1);
				String add="";
				if (processHasMultiTimes(process)) add="["+Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type")+"=\""+Language.trPrimary("Surface.Process.XML.Distribution.Type.ProcessingTime")+"\"]";
				input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+process.getXMLNodeNames()[0]+"[id=\""+process.getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
				if (isParameterInUse.test(input)) continue;
				if (list2==null) {
					sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.ProcessTime.Title"),ModelChanger.Mode.MODE_XML);
					list.add(sub);
					list2=sub.list;
				}
				list2.add(input);
			}
		}

		/* Verzögerungsstationen - Zeiten */


		if (modes.contains(Mode.DELAY)) {
			list2=null;
			for (ModelElement element: model.surface.getElements()) {
				if (!(element instanceof ModelElementDelay)) continue;
				final ModelElementDelay delay=(ModelElementDelay)element;
				final AbstractRealDistribution dist=delay.getDelayTime();
				if (dist==null) continue;
				if (!DistributionTools.canSetMean(dist)) continue;
				final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
				input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.DelayTime"),delay.getName()+" (id="+delay.getId()+")"));
				input.setMode(ModelChanger.Mode.MODE_XML);
				input.setXMLMode(1);
				String add="";
				if (delayHasMultiTimes(delay)) add="[1]";
				input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+delay.getXMLNodeNames()[0]+"[id=\""+delay.getId()+"\"]->"+Language.trPrimary("Surface.Source.XML.Distribution")+add);
				if (isParameterInUse.test(input)) continue;
				if (list2==null) {
					sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.DelayTime.Title"),ModelChanger.Mode.MODE_XML);
					list.add(sub);
					list2=sub.list;
				}
				list2.add(input);
			}
		}

		/* Analogwertstationen - Initiale Werte */

		if (modes.contains(Mode.ANALOG)) {
			list2=null;
			for (ModelElement element: model.surface.getElements()) {
				if (!(element instanceof ModelElementAnalogValue) && !(element instanceof ModelElementTank)) continue;
				String add=null;
				if (element instanceof ModelElementAnalogValue) add=Language.tr("Surface.XML.AnalogValue.InitialValue");
				if (element instanceof ModelElementTank) add=Language.tr("Surface.XML.Tank.InitialValue");
				if (add==null) continue;
				final ParameterCompareSetupValueInput input=new ParameterCompareSetupValueInput();
				input.setName(String.format(Language.tr("ParameterCompare.Settings.Input.List.Templates.AnalogValue"),element.getName()+" (id="+element.getId()+")"));
				input.setMode(ModelChanger.Mode.MODE_XML);
				input.setTag(ModelSurface.XML_NODE_NAME[0]+"->"+element.getXMLNodeNames()[0]+"[id=\""+element.getId()+"\"]->"+add);
				if (isParameterInUse.test(input)) continue;
				if (list2==null) {
					sub=new Sub(Language.tr("ParameterCompare.Settings.Input.List.Templates.AnalogValue.Title"),ModelChanger.Mode.MODE_XML);
					list.add(sub);
					list2=sub.list;
				}
				list2.add(input);
			}
		}

		return list;
	}

	/**
	 * Liefert die Einträge für das Popupmenü
	 * @return	Einträge für das Popupmenü
	 */
	public List<Object> getList() {
		return getList(new HashSet<Mode>(Arrays.asList(Mode.values())));
	}

	/**
	 * Stellt Einträge für ein Popupmenü zusammen und trägt diese in das Menü ein.
	 * @param modes	Gewählte Kategorien (<code>null</code> führt zu einem leeren Menü)
	 * @param popupMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 */
	public void addToMenu(final Set<Mode> modes, final JPopupMenu popupMenu, final Consumer<ParameterCompareSetupValueInput> action) {
		addTemplatesToMenu(getList(modes),popupMenu,action);
	}

	/**
	 * Stellt Einträge für ein Popupmenü zusammen und trägt diese in das Menü ein.
	 * @param popupMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 */
	public void addToMenu(final JPopupMenu popupMenu, final Consumer<ParameterCompareSetupValueInput> action) {
		addTemplatesToMenu(getList(),popupMenu,action);
	}

	/**
	 * Stellt Einträge für ein Popupmenü zusammen und trägt diese in das Menü ein.
	 * @param modes	Gewählte Kategorien (<code>null</code> führt zu einem leeren Menü)
	 * @param parentMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 */
	public void addToMenu(final Set<Mode> modes, final JMenu parentMenu, final Consumer<ParameterCompareSetupValueInput> action) {
		addTemplatesToMenu(getList(modes),parentMenu,action);
	}

	/**
	 * Stellt Einträge für ein Popupmenü zusammen und trägt diese in das Menü ein.
	 * @param parentMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 */
	public void addToMenu(final JMenu parentMenu, final Consumer<ParameterCompareSetupValueInput> action) {
		addTemplatesToMenu(getList(),parentMenu,action);
	}

	/**
	 * Unterpunkt in einer Zusammenstellung von Einträgen
	 * @author Alexander Herzog
	 * @see ParameterCompareInputValuesTemplates#getList()
	 * @see ParameterCompareInputValuesTemplates#getList(Set)
	 */
	public static class Sub {
		/**
		 * Name des Untermenüs
		 */
		public final String title;

		/**
		 * Icon-Art für das Untermenü
		 */
		public final ModelChanger.Mode iconMode;

		/**
		 * Liste mit den Untereinträgen (die optional wiederum Untermenüs enthalten können)
		 */
		public final List<Object> list;

		/**
		 * Konstruktor der Klasse
		 * @param title	Name des Untermenüs
		 * @param iconMode	Icon-Art für das Untermenü
		 */
		public Sub(final String title, final ModelChanger.Mode iconMode) {
			this.title=title;
			this.iconMode=iconMode;
			list=new ArrayList<>();
		}
	}

	private static JMenuItem getTemplateMenuItem(final ParameterCompareSetupValueInput input, final Function<ModelChanger.Mode,Icon> iconGetter, final Consumer<ParameterCompareSetupValueInput> action) {
		final JMenuItem item=new JMenuItem(input.getName());
		final Icon icon=iconGetter.apply(input.getMode());
		if (icon!=null) item.setIcon(icon);
		item.addActionListener(e->action.accept(input));
		return item;
	}


	/**
	 * Fügt Einträge zu einem Menü hinzu
	 * @param templates	Liste der Vorlagen, die hinzugefügt werden sollen
	 * @param popupMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param iconGetter	Callback über das Icons für Einträge ermittelt werden können
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 * @see #getList()
	 * @see #getList(Set)
	 */
	public static void addTemplatesToMenu(final List<Object> templates, final JPopupMenu popupMenu, final Function<ModelChanger.Mode,Icon> iconGetter, final Consumer<ParameterCompareSetupValueInput> action) {
		if (templates==null) return;

		for (Object obj: templates) {
			if (obj instanceof ParameterCompareSetupValueInput) {
				popupMenu.add(getTemplateMenuItem((ParameterCompareSetupValueInput)obj,iconGetter,action));
				continue;
			}
			if (obj instanceof Sub) {
				final Sub sub=(Sub)obj;
				final JMenu menu=new JMenu(sub.title);
				final Icon icon=iconGetter.apply(sub.iconMode);
				if (icon!=null) menu.setIcon(icon);
				popupMenu.add(menu);
				addTemplatesToMenu(sub.list,menu,iconGetter,action);
			}
		}
	}

	/**
	 * Fügt Einträge zu einem Menü hinzu
	 * @param templates	Liste der Vorlagen, die hinzugefügt werden sollen
	 * @param popupMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 * @see #getList()
	 * @see #getList(Set)
	 */
	public static void addTemplatesToMenu(final List<Object> templates, final JPopupMenu popupMenu, final Consumer<ParameterCompareSetupValueInput> action) {
		addTemplatesToMenu(templates,popupMenu,mode->getIcon(mode),action);
	}

	/**
	 * Fügt Einträge zu einem Menü hinzu
	 * @param templates	Liste der Vorlagen, die hinzugefügt werden sollen
	 * @param parentMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param iconGetter	Callback über das Icons für Einträge ermittelt werden können
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 * @see #getList()
	 * @see #getList(Set)
	 */
	public static void addTemplatesToMenu(final List<Object> templates, final JMenu parentMenu, final Function<ModelChanger.Mode,Icon> iconGetter, final Consumer<ParameterCompareSetupValueInput> action) {
		for (Object obj: templates) {
			if (obj instanceof ParameterCompareSetupValueInput) {
				parentMenu.add(getTemplateMenuItem((ParameterCompareSetupValueInput)obj,iconGetter,action));
				continue;
			}
			if (obj instanceof Sub) {
				final Sub sub=(Sub)obj;
				final JMenu menu=new JMenu(sub.title);
				final Icon icon=iconGetter.apply(sub.iconMode);
				if (icon!=null) menu.setIcon(icon);
				parentMenu.add(menu);
				addTemplatesToMenu(sub.list,menu,iconGetter,action);
			}
		}
	}

	/**
	 * Fügt Einträge zu einem Menü hinzu
	 * @param templates	Liste der Vorlagen, die hinzugefügt werden sollen
	 * @param parentMenu	Popupmenü bei dem die Einträge angehängt werden sollen.
	 * @param action	Aktion die beim Anklicken ausgeführt werden soll
	 * @see #getList()
	 * @see #getList(Set)
	 */
	public static void addTemplatesToMenu(final List<Object> templates, final JMenu parentMenu, final Consumer<ParameterCompareSetupValueInput> action) {
		addTemplatesToMenu(templates,parentMenu,mode->getIcon(mode),action);
	}

	/**
	 * Liefert ein Icon für eine Icon-Art
	 * @param iconMode	Icon-Art
	 * @return	Icon (oder <code>null</code>, wenn kein passendes Icon existiert)
	 */
	public static Icon getIcon(final ModelChanger.Mode iconMode) {
		switch (iconMode) {
		case MODE_RESOURCE: return Images.PARAMETERSERIES_INPUT_MODE_RESOURCE.getIcon();
		case MODE_VARIABLE: return Images.PARAMETERSERIES_INPUT_MODE_VARIABLE.getIcon();
		case MODE_XML: return Images.PARAMETERSERIES_INPUT_MODE_XML.getIcon();
		default: return null;
		}
	}
}
