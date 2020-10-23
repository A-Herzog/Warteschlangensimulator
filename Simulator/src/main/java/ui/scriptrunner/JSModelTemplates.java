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
package ui.scriptrunner;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.script.ScriptEditorPanel;
import ui.script.ScriptPanel;

/**
 * Zeigt ein Kontextmenü zur Auswahl von Javascipt-Beispiel-Vorlagen an.
 * @author Alexander Herzog
 * @see ScriptPanel
 */
public class JSModelTemplates {
	/** Für welche Sprache sollen die Vorlagen bereitgestellt werden? */
	private final ScriptEditorPanel.ScriptMode mode;
	/** Editor-Modell auf dessen Basis Javascipt-Beispiel-Vorlagen angeboten werden sollen */
	private final EditModel model;

	/**
	 * Konstruktor der Klasse
	 * @param mode	Für welche Sprache sollen die Vorlagen bereitgestellt werden?
	 * @param model	Editor-Modell auf dessen Basis Javascipt-Beispiel-Vorlagen angeboten werden sollen
	 */
	public JSModelTemplates(final ScriptEditorPanel.ScriptMode mode, final EditModel model) {
		this.mode=mode;
		this.model=model;
	}

	private int calcStep(final int min, final int max) {
		int count=max-min;
		int step=1;
		while (count>=20) {step*=2; count/=2;}
		return step;
	}

	private Record changeResourceCount(final String name) {
		final ModelResource resource=model.resources.get(name);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER || resource.getCount()<0) return null;

		final int min=resource.getCount();
		final int max=Math.max(min*2,min+1);
		final int step=calcStep(min,max);

		final StringBuilder code=new StringBuilder();
		switch (mode) {
		case Javascript:
			code.append("Output.println(\"c\\tE[W]\");\n");
			code.append("for (var i="+min+";i<="+max+";i+="+step+") {\n");
			code.append("  Model.reset();\n");
			code.append("  Model.setResourceCount(\""+name+"\",i);\n");
			code.append("  Model.run();\n");
			code.append("  var w=Statistics.xml(\""+Language.tr("Statistics.XML.Element.WaitingAllClients")+"["+Language.tr("Statistics.XML.Mean")+"]\");\n");
			code.append("  Output.println(i+\"\\t\"+w);\n");
			code.append("}\n");
			break;
		case Java:
			code.append("void function(SimulationInterface sim) {\n");
			code.append("  sim.getOutput().println(\"c\\tE[W]\");\n");
			code.append("  for (int i="+min+";i<="+max+";i+="+step+") {\n");
			code.append("    sim.getModel().reset();\n");
			code.append("    sim.getModel().setResourceCount(\""+name+"\",i);\n");
			code.append("    sim.getModel().run();\n");
			code.append("    final String w=sim.getStatistics().xml(\""+Language.tr("Statistics.XML.Element.WaitingAllClients")+"["+Language.tr("Statistics.XML.Mean")+"]\");\n");
			code.append("    sim.getOutput().println(i+\"\\t\"+w);\n");
			code.append("  }\n");
			code.append("}\n");
			break;
		}
		return new Record(String.format(Language.tr("JSRunner.Templates.ChangeResource.Info"),name),code.toString());
	}

	private List<Record> changeResourceCount() {
		final List<Record> list=new ArrayList<>();
		for (String name: model.resources.list()) {
			final Record record=changeResourceCount(name);
			if (record!=null) list.add(record);
		}
		return list;
	}

	private Record changeInterArrival(final ModelElementSource source) {
		if (source.getRecord().getNextMode()!=ModelElementSourceRecord.NextMode.NEXT_DISTRIBUTION) return null;
		if (!DistributionTools.canSetMean(source.getRecord().getInterarrivalTimeDistribution())) return null;

		final int min=(int)Math.round(DistributionTools.getMean(source.getRecord().getInterarrivalTimeDistribution()));
		final int max=Math.max(min*2,min+2);
		final int step=calcStep(min,max);

		final StringBuilder code=new StringBuilder();
		switch (mode) {
		case Javascript:
			code.append("Output.println(\"E[I]\\tE[W]\");\n");
			code.append("for (var i="+min+";i<="+max+";i+="+step+") {\n");
			code.append("  Model.reset();\n");
			code.append("  Model.setMean(\""+Language.tr("Surface.XML.RootName.Model")+"->"+Language.tr("Surface.Source.XML.Root")+"[id=\\\""+source.getId()+"\\\"]->"+Language.tr("Surface.DistributionSystem.XML.Distribution")+"\",i);\n");
			code.append("  Model.run();\n");
			code.append("  var w=Statistics.xml(\""+Language.tr("Statistics.XML.Element.WaitingAllClients")+"["+Language.tr("Statistics.XML.Mean")+"]\");\n");
			code.append("  Output.println(i+\"\\t\"+w);\n");
			code.append("}\n");
			break;
		case Java:
			code.append("void function(SimulationInterface sim) {\n");
			code.append("  sim.getOutput().println(\"E[I]\\tE[W]\");\n");
			code.append("  for (int i="+min+";i<="+max+";i+="+step+") {\n");
			code.append("    sim.getModel().reset();\n");
			code.append("    sim.getModel().setMean(\""+Language.tr("Surface.XML.RootName.Model")+"->"+Language.tr("Surface.Source.XML.Root")+"[id=\\\""+source.getId()+"\\\"]->"+Language.tr("Surface.DistributionSystem.XML.Distribution")+"\",i);\n");
			code.append("    sim.getModel().run();\n");
			code.append("    final String w=sim.getStatistics().xml(\""+Language.tr("Statistics.XML.Element.WaitingAllClients")+"["+Language.tr("Statistics.XML.Mean")+"]\");\n");
			code.append("    sim.getOutput().println(i+\"\\t\"+w);\n");
			code.append("  }\n");
			code.append("}\n");
			break;
		}
		return new Record(String.format(Language.tr("JSRunner.Templates.ChangeInterArrival.Info"),source.getName()+" (id="+source.getId()+")"),code.toString());
	}

	private Record changeService(final ModelElementProcess process) {
		if (!(process.getWorking().get() instanceof AbstractRealDistribution)) return null;
		final AbstractRealDistribution distribution=(AbstractRealDistribution)process.getWorking().get();
		if (!DistributionTools.canSetMean(distribution)) return null;

		final int max=(int)Math.round(DistributionTools.getMean(distribution));
		final int min=max/2;
		final int step=calcStep(min,max);

		final StringBuilder code=new StringBuilder();
		switch (mode) {
		case Javascript:
			code.append("Output.println(\"E[S]\\tE[W]\");\n");
			code.append("for (var i="+min+";i<="+max+";i+="+step+") {\n");
			code.append("  Model.reset();\n");
			code.append("  Model.setMean(\""+Language.tr("Surface.XML.RootName.Model")+"->"+Language.tr("Surface.Process.XML.Root")+"[id=\\\""+process.getId()+"\\\"]->"+Language.tr("Surface.DistributionSystem.XML.Distribution")+"[1]\",i);\n");
			code.append("  Model.run();\n");
			code.append("  var w=Statistics.xml(\""+Language.tr("Statistics.XML.Element.WaitingAllClients")+"["+Language.tr("Statistics.XML.Mean")+"]\");\n");
			code.append("  Output.println(i+\"\\t\"+w);\n");
			code.append("}\n");
			break;
		case Java:
			code.append("void function(SimulationInterface sim) {\n");
			code.append("  sim.getOutput().println(\"E[S]\\tE[W]\");\n");
			code.append("  for (int i="+min+";i<="+max+";i+="+step+") {\n");
			code.append("    sim.getModel().reset();\n");
			code.append("    sim.getModel().setMean(\""+Language.tr("Surface.XML.RootName.Model")+"->"+Language.tr("Surface.Process.XML.Root")+"[id=\\\""+process.getId()+"\\\"]->"+Language.tr("Surface.DistributionSystem.XML.Distribution")+"[1]\",i);\n");
			code.append("    sim.getModel().run();\n");
			code.append("    final String w=sim.getStatistics().xml(\""+Language.tr("Statistics.XML.Element.WaitingAllClients")+"["+Language.tr("Statistics.XML.Mean")+"]\");\n");
			code.append("    sim.getOutput().println(i+\"\\t\"+w);\n");
			code.append("  }\n");
			code.append("}\n");
			break;
		}
		return new Record(String.format(Language.tr("JSRunner.Templates.ChangeService.Info"),process.getName()+" (id="+process.getId()+")"),code.toString());
	}

	private List<Record> changeInterArrival() {
		final List<Record> list=new ArrayList<>();
		for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementSource) {
			final Record record=changeInterArrival((ModelElementSource)element);
			if (record!=null) list.add(record);
		}
		return list;
	}

	private List<Record> changeService() {
		final List<Record> list=new ArrayList<>();
		for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementProcess) {
			final Record record=changeService((ModelElementProcess)element);
			if (record!=null) list.add(record);
		}
		return list;
	}

	private void addRecordListToSub(final JMenu sub, final List<Record> list, final Consumer<String> listener) {
		for (Record record: list) {
			final JMenuItem item=new JMenuItem(record.name);
			item.addActionListener(e->listener.accept(record.code));
			sub.add(item);
		}
	}

	/**
	 * Trägt Menüpunkte in ein Popup-Menü ein
	 * @param popupMenu	Popup-Menü an das die neuen Menüpunkte angefügt werden sollen
	 * @param listener	Listener der aufgerufen wird, wenn der Nutzer ein bestimmtes Codefragment aufrufen möchte
	 */
	public void buildMenu(final JPopupMenu popupMenu, final Consumer<String> listener) {
		boolean isEmpty=true;
		List<Record> list;
		JMenu sub;

		list=changeResourceCount();
		if (!list.isEmpty()) {
			isEmpty=false;
			popupMenu.add(sub=new JMenu(Language.tr("JSRunner.Templates.ChangeResource.Title")));
			sub.setIcon(Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon());
			addRecordListToSub(sub,list,listener);
		}

		list=changeInterArrival();
		if (!list.isEmpty()) {
			isEmpty=false;
			popupMenu.add(sub=new JMenu(Language.tr("JSRunner.Templates.ChangeInterArrival.Title")));
			sub.setIcon(Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
			addRecordListToSub(sub,list,listener);
		}

		list=changeService();
		if (!list.isEmpty()) {
			isEmpty=false;
			popupMenu.add(sub=new JMenu(Language.tr("JSRunner.Templates.ChangeService.Title")));
			sub.setIcon(Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon());
			addRecordListToSub(sub,list,listener);
		}

		if (isEmpty) {
			final JMenuItem item=new JMenuItem(Language.tr("JSRunner.Templates.Empty"));
			item.setEnabled(false);
			popupMenu.add(item);
		}
	}

	/**
	 * Zeigt ein Popup-Menü an
	 * @param anchor	Element, an dem das Popup-Menü ausgerichtet werden soll
	 * @param listener	Listener der aufgerufen wird, wenn der Nutzer ein bestimmtes Codefragment aufrufen möchte
	 */
	public void showMenu(final Component anchor, final Consumer<String> listener) {
		final JPopupMenu popupMenu=new JPopupMenu();
		buildMenu(popupMenu,listener);
		popupMenu.show(anchor,0,anchor.getHeight());
	}

	private class Record {
		public final String name;
		public final String code;
		public Record(final String name, final String code) {
			this.name=name;
			this.code=code;
		}
	}
}