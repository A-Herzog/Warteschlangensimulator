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
package ui;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import ui.modeleditor.ModelResource;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;

/**
 * Versucht ein Modell automatisiert (nach Nutzerrückfrage) zu reparieren.
 * @author Alexander Herzog
 */
public class EditorPanelRepair {
	private enum RepairState {NOT_CHANGED, FIXED, USER_CANCELED}

	private final EditorPanel editorPanel;

	/**
	 * Konstruktor der Klasse
	 * @param editorPanel	Editor-Panel aus dem das Modell entnommen und das korrigierte zurückgegeben werden soll
	 */
	public EditorPanelRepair(final EditorPanel editorPanel) {
		this.editorPanel=editorPanel;
	}

	private void reloadToEditor(final EditModel model) {
		final File file=editorPanel.getLastFile();
		editorPanel.setModel(model);
		editorPanel.setLastFile(file);
		editorPanel.setModelChanged(true);
	}

	private RepairState fixResources(final EditModel model) {
		/* Enthält das Modell eine einzelne Bedienstation? */
		ModelElementProcess process=null;
		for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementProcess) {
			if (process==null) process=(ModelElementProcess)element; else return RepairState.NOT_CHANGED;
		}
		if (process==null) return RepairState.NOT_CHANGED;
		final String processName=(process.getName().trim().isEmpty())?("id="+process.getId()):("\""+process.getName()+"\" (id="+process.getId()+")");

		/* Keine Bediener? */
		if (process.getNeededResources().size()!=1) return RepairState.NOT_CHANGED;
		if (process.getNeededResources().get(0).size()!=0) return RepairState.NOT_CHANGED;

		/* Name der AutoAdd-Ressource festlegen. */
		String resourceName=String.format(Language.tr("Window.Check.AutoFixResources.ResourceName"),process.getId());

		/* Kein AutoAdd, wenn es die Ressource schon gibt. */
		ModelResource resource=model.resources.getNoAutoAdd(resourceName);
		if (resource!=null) return RepairState.NOT_CHANGED;

		/* Nutzer fragen */
		if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixResources.Title"),String.format(Language.tr("Window.Check.AutoFixResources.Info"),processName),Language.tr("Window.Check.AutoFixResources.YesInfo"),Language.tr("Window.Check.AutoFixResources.NoInfo"))) return RepairState.USER_CANCELED;

		/* Ressource anlegen */
		resource=model.resources.get(resourceName);
		resource.clear();
		resource.setName(resourceName);

		/* Ressource in Station eintragen */
		process.getNeededResources().get(0).put(resourceName,1);

		return RepairState.FIXED;
	}

	private RepairState fixRepeatCount(final EditModel model) {
		/* Nur eine Wiederholung? -> Nichts zu tun*/
		if (model.repeatCount<=1) return RepairState.NOT_CHANGED;

		/* Prüfen, ob es Gründe gibt, dass das Modell nicht mehrfach simuliert werden kann. */
		final String noRepeat=model.getNoRepeatReason();
		if (noRepeat==null) return RepairState.NOT_CHANGED;

		/* Nutzer fragen */
		if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixRepeatCount.Title"),String.format(Language.tr("Window.Check.AutoFixRepeatCount.Info"),model.repeatCount,noRepeat),Language.tr("Window.Check.AutoFixRepeatCount.YesInfo"),Language.tr("Window.Check.AutoFixRepeatCount.NoInfo"))) return RepairState.USER_CANCELED;

		/* Anzahl an Wiederholungen auf 1 reduzieren */
		model.repeatCount=1;
		return RepairState.FIXED;
	}

	private RepairState fixConnections(final EditModel model) {
		ModelElementSource source=null;
		ModelElementDelay delay=null;
		ModelElementProcess process=null;
		ModelElementDispose dispose=null;
		int maxX=0;

		/* Elemente finden */
		for (ModelElement element:	model.surface.getElements()) {
			if (element instanceof ModelElementBox) {
				final int x=((ModelElementBox)element).getPosition(true).x;
				final int width=Math.max(0,((ModelElementBox)element).getSize().width);
				maxX=Math.max(maxX,x+width);
			}

			if (element instanceof ModelElementSource) {
				if (source!=null) return RepairState.NOT_CHANGED;
				source=(ModelElementSource)element;
			}
			if (element instanceof ModelElementProcess) {
				if (process!=null || delay!=null) return RepairState.NOT_CHANGED;
				process=(ModelElementProcess)element;
			}
			if (element instanceof ModelElementDelay) {
				if (process!=null || delay!=null) return RepairState.NOT_CHANGED;
				delay=(ModelElementDelay)element;
			}
			if (element instanceof ModelElementDispose) {
				if (dispose!=null) return RepairState.NOT_CHANGED;
				dispose=(ModelElementDispose)element;
			}
		}
		if (source==null) return RepairState.NOT_CHANGED;
		if (process==null && delay==null) return RepairState.NOT_CHANGED;

		RepairState state=RepairState.NOT_CHANGED;

		/* Modell hat kein Ausgang-Element? */
		if (dispose==null) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixDispose.Title"),Language.tr("Window.Check.AutoFixDispose.Info"),Language.tr("Window.Check.AutoFixDispose.YesInfo"),Language.tr("Window.Check.AutoFixDispose.NoInfo"))) return RepairState.USER_CANCELED;
			final int x=maxX+100;
			final int y;
			if (process!=null) y=process.getPosition(true).y; else {
				if (delay!=null) y=delay.getPosition(true).y; else y=0;
			}
			dispose=new ModelElementDispose(model,model.surface);
			dispose.setPosition(new Point(x,y));
			model.surface.add(dispose);
			state=RepairState.FIXED;
		}

		/* Quelle hat keinen Ausgang? */
		if (source.getEdgeOut()==null) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixConnection.Title"),Language.tr("Window.Check.AutoFixConnection.InfoSourceProcess"),Language.tr("Window.Check.AutoFixConnection.YesInfo"),Language.tr("Window.Check.AutoFixConnection.NoInfo"))) return RepairState.USER_CANCELED;
			ModelElementEdge edge=null;
			if (process!=null) {
				edge=new ModelElementEdge(model,model.surface,source,process);
				source.addEdgeOut(edge);
				process.addEdgeIn(edge);
			}
			if (delay!=null) {
				edge=new ModelElementEdge(model,model.surface,source,delay);
				source.addEdgeOut(edge);
				delay.addEdgeIn(edge);
			}
			if (edge!=null) {
				model.surface.add(edge);
				state=RepairState.FIXED;
			}
		}

		/* Verzögerungstation hat keinen Ausgang? */
		if (delay!=null && delay.getEdgeOut()==null) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixConnection.Title"),Language.tr("Window.Check.AutoFixConnection.InfoDelayDispose"),Language.tr("Window.Check.AutoFixConnection.YesInfo"),Language.tr("Window.Check.AutoFixConnection.NoInfo"))) return RepairState.USER_CANCELED;
			final ModelElementEdge edge=new ModelElementEdge(model,model.surface,delay,dispose);
			delay.addEdgeOut(edge);
			dispose.addEdgeIn(edge);
			model.surface.add(edge);
			state=RepairState.FIXED;
		}

		/* Bedienstation hat keinen Ausgang? */
		if (process!=null && process.getEdgeOutSuccess()==null) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixConnection.Title"),Language.tr("Window.Check.AutoFixConnection.InfoProcessDispose"),Language.tr("Window.Check.AutoFixConnection.YesInfo"),Language.tr("Window.Check.AutoFixConnection.NoInfo"))) return RepairState.USER_CANCELED;
			final ModelElementEdge edge=new ModelElementEdge(model,model.surface,process,dispose);
			process.addEdgeOut(edge);
			dispose.addEdgeIn(edge);
			model.surface.add(edge);
			state=RepairState.FIXED;
		}

		return state;
	}

	/**
	 * Versucht das Modell zu reparieren.
	 * @return	Gibt an, ob das Modell korrigiert wurde
	 */
	public boolean work() {
		final EditModel model=editorPanel.getModel();

		final List<Function<EditModel,RepairState>> fixFunctions=new ArrayList<>();
		fixFunctions.add(m->fixResources(m));
		fixFunctions.add(m->fixRepeatCount(m));
		fixFunctions.add(m->fixConnections(m));

		boolean fixed=false;
		for (Function<EditModel,RepairState> func: fixFunctions) {
			final RepairState state=func.apply(model);
			if (state==RepairState.USER_CANCELED) return false;
			if (state==RepairState.FIXED) fixed=true;
		}

		if (fixed) {
			reloadToEditor(model);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Hilfsroutine, die die Erstellung eines Objektes einspart.
	 * @param editorPanel	Editor-Panel aus dem das Modell entnommen und das korrigierte zurückgegeben werden soll
	 * @return	Gibt an, ob das Modell korrigiert wurde
	 */
	public static boolean autoFix(final EditorPanel editorPanel) {
		final EditorPanelRepair modelRepairer=new EditorPanelRepair(editorPanel);
		return modelRepairer.work();
	}
}
