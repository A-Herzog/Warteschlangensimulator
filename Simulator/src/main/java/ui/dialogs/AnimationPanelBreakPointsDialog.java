/**
 * Copyright 2022 Alexander Herzog
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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.AnimationPanel;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ElementRendererTools;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelSurfaceAnimatorBase.BreakPoint;
import ui.modeleditor.coreelements.ModelBreakPointDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAnimationPause;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt einen Dialog zum Bearbeiten der Animations-Haltepunkte an.
 * @author Alexander Herzog
 * @see AnimationPanel
 * @see ModelSurfaceAnimatorBase#getBreakPoints()
 * @see ModelSurfaceAnimatorBase#setBreakPoints(List)
 */
public class AnimationPanelBreakPointsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8689557992253471887L;

	/**
	 * Simulationsdatenobjekt
	 */
	private final SimulationData simData;

	/**
	 * Modell dem die Daten zu den Pause-Stationen entnommen werden
	 */
	private final EditModel model;

	/**
	 * Animator-System zum Auslesen und Setzen der Haltepunkte
	 */
	private final ModelSurfaceAnimatorBase animator;

	/**
	 * Liste der Kundentypen im Modell
	 */
	private final String[] clientTypes;

	/**
	 * Lokale Liste der Haltepunkte im Editordialog
	 */
	private final List<ModelSurfaceAnimatorBase.BreakPoint> breakpoints;

	/**
	 * Listenmodell für die Liste der Haltepunkte
	 */
	private final DefaultListModel<ModelSurfaceAnimatorBase.BreakPoint> breakpointData;

	/**
	 * Liste der Haltepunkte
	 */
	private final JList<ModelSurfaceAnimatorBase.BreakPoint> breakpointList;

	/**
	 * Schaltfläche: Haltepunkt bearbeiten
	 */
	private final JButton breakpointButtonEdit;

	/**
	 * Schaltfläche: Haltepunkt löschen
	 */
	private final JButton breakpointButtonDelete;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param simData	Simulationsdatenobjekt
	 * @param model	Modell dem die Daten zu den Pause-Stationen entnommen werden (es erfolgen keine Veränderungen)
	 * @param animator	Animator-System zum Auslesen und Setzen der Haltepunkte
	 */
	public AnimationPanelBreakPointsDialog(final Component owner, final SimulationData simData, final EditModel model, final ModelSurfaceAnimatorBase animator) {
		super(owner,Language.tr("Editor.Breakpoints.Dialog.Title"));

		this.simData=simData;
		this.model=model;
		this.animator=animator;
		clientTypes=simData.runModel.clientTypes;
		breakpoints=animator.getBreakPoints();
		final List<ModelElementAnimationPause> pauseStations=getAllPauseStations(model);

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this.owner,"AnimationBreakpoints"));
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalEditBreakpoints);
		all.add(content,BorderLayout.CENTER);
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tab;

		/* Tab "Haltepunkte" */
		if (breakpoints.size()>0) {
			tabs.addTab(Language.tr("Editor.Breakpoints.TabBreakpoints"),tab=new JPanel(new BorderLayout()));
			final JToolBar toolBar=new JToolBar(SwingConstants.HORIZONTAL);
			toolBar.setFloatable(false);
			tab.add(toolBar,BorderLayout.NORTH);
			toolBar.add(breakpointButtonEdit=new JButton(Language.tr("Editor.Breakpoints.TabBreakpoints.Edit"),Images.GENERAL_EDIT.getIcon()));
			breakpointButtonEdit.setToolTipText(Language.tr("Editor.Breakpoints.TabBreakpoints.Edit.Tooltip"));
			breakpointButtonEdit.addActionListener(e->editBreakpoint());
			toolBar.add(breakpointButtonDelete=new JButton(Language.tr("Editor.Breakpoints.TabBreakpoints.Delete"),Images.EDIT_DELETE.getIcon()));
			breakpointButtonDelete.setToolTipText(Language.tr("Editor.Breakpoints.TabBreakpoints.Delete.Tooltip"));
			breakpointButtonDelete.addActionListener(e->deleteBreakpoint());
			breakpointList=new JList<>(breakpointData=new DefaultListModel<>());
			tab.add(new JScrollPane(breakpointList),BorderLayout.CENTER);
			breakpointList.setCellRenderer(new BreakpointListCellRenderer());
			breakpointList.addListSelectionListener(e->updateButtons());
			breakpointList.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ENTER) {editBreakpoint(); e.consume(); return;}
					if (e.getKeyCode()==KeyEvent.VK_DELETE) {deleteBreakpoint(); e.consume(); return;}
				}
			});
			breakpointList.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {editBreakpoint(); e.consume(); return;}
					if (SwingUtilities.isRightMouseButton(e)) {showBreakpointContextMenu(e); e.consume(); return;}
				}
			});
			updateBreakpointsList(0);
		} else {
			breakpointButtonEdit=null;
			breakpointButtonDelete=null;
			breakpointList=null;
			breakpointData=null;
		}

		/* Tab "Pause-Stationen" */
		if (pauseStations.size()>0) {
			tabs.addTab(Language.tr("Editor.Breakpoints.TabPauseStations"),tab=new JPanel(new BorderLayout()));
			final DefaultListModel<ElementRendererTools.InfoRecord> data=new DefaultListModel<>();
			for (ModelElementAnimationPause station: pauseStations) data.addElement(ElementRendererTools.getRecord(model.surface,station));
			final JList<ElementRendererTools.InfoRecord> list=new JList<>(data);
			tab.add(new JScrollPane(list));
			list.setCellRenderer(new ElementRendererTools.InfoRecordListCellRenderer(ElementRendererTools.GradientStyle.OFF));
		}

		/* Icons auf Tabs */
		int index=0;
		if (breakpoints.size()>0) {
			tabs.setIconAt(index,Images.ANIMATION_BREAKPOINTS.getIcon());
			index++;
		}
		if (pauseStations.size()>0) {
			tabs.setIconAt(index,Images.ANIMATION_BREAKPOINTS_PAUSE_STATIONS.getIcon());
			index++;
		}

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Aktualisiert die Liste der Haltepunkte
	 * @param selectDelta	Relative Verschiebung der Selektion
	 */
	private void updateBreakpointsList(final int selectDelta) {
		if (breakpointList==null) return;
		int lastIndex=breakpointList.getSelectedIndex();
		if (lastIndex>=0) lastIndex+=selectDelta;

		breakpointData.clear();
		for (ModelSurfaceAnimatorBase.BreakPoint breakpoint: breakpoints) breakpointData.addElement(breakpoint);

		if (lastIndex>=0 && lastIndex<breakpointData.getSize()) {
			breakpointList.setSelectedIndex(lastIndex);
		} else {
			if (breakpointData.getSize()>0) breakpointList.setSelectedIndex(lastIndex);
		}
		updateButtons();
	}

	/**
	 * Aktualisiert den Aktivierungsstatus der Schaltflächen
	 */
	private void updateButtons() {
		if (breakpointList==null) return;
		if (breakpointButtonEdit!=null) breakpointButtonEdit.setEnabled(breakpointList.getSelectedIndex()>=0);
		if (breakpointButtonDelete!=null) breakpointButtonDelete.setEnabled(breakpointList.getSelectedIndex()>=0);
	}

	/**
	 * Befehl: Haltepunkt bearbeiten
	 */
	private void editBreakpoint() {
		if (breakpointList==null || breakpointList.getSelectedIndex()<0) return;

		final int index=breakpointList.getSelectedIndex();
		final ModelSurfaceAnimatorBase.BreakPoint breakpoint=breakpoints.get(index);
		final ModelBreakPointDialog dialog=new ModelBreakPointDialog(this,model,simData,false,breakpoint.stationID,breakpoint);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			breakpoints.set(index,dialog.getBreakPoint());
			updateBreakpointsList(0);
		}
	}

	/**
	 * Befehl: Haltepunkt löschen
	 */
	private void deleteBreakpoint() {
		if (breakpointList==null || breakpointList.getSelectedIndex()<0) return;

		final int index=breakpointList.getSelectedIndex();
		final int stationID=breakpoints.get(index).stationID;

		if (!MsgBox.confirm(this,Language.tr("Editor.Breakpoints.TabBreakpoints.Delete.ConfirmTitle"),String.format(Language.tr("Editor.Breakpoints.TabBreakpoints.Delete.ConfirmInfo"),stationID),Language.tr("Editor.Breakpoints.TabBreakpoints.Delete.ConfirmInfoYes"),Language.tr("Editor.Breakpoints.TabBreakpoints.Delete.ConfirmInfoNo"))) return;

		breakpoints.remove(index);
		updateBreakpointsList(-1);
	}

	/**
	 * Zeigt das Kontextmenü zu einem Haltepunkte-Listeneintrag an.
	 * @param event	Auslösendes Mausereignis
	 */
	private void showBreakpointContextMenu(final MouseEvent event) {
		final JPopupMenu menu=new JPopupMenu();

		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("Editor.Breakpoints.TabBreakpoints.Edit"),Images.GENERAL_EDIT.getIcon()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
		item.addActionListener(e->editBreakpoint());

		menu.add(item=new JMenuItem(Language.tr("Editor.Breakpoints.TabBreakpoints.Delete"),Images.EDIT_DELETE.getIcon()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		item.addActionListener(e->deleteBreakpoint());

		menu.show(breakpointList,event.getX(),event.getY());
	}

	@Override
	protected void storeData() {
		animator.setBreakPoints(breakpoints);
	}

	/**
	 * Liefert eine Liste aller in dem Modell enthaltenen Pause-Stationen.
	 * @param model	Modell dem die Daten zu den Pause-Stationen entnommen werden sollen
	 * @return	Liste aller in dem Modell enthaltenen Pause-Stationen (kann leer sein, ist aber nie <code>null</code>)
	 */
	public static List<ModelElementAnimationPause> getAllPauseStations(final EditModel model) {
		final List<ModelElementAnimationPause> list=new ArrayList<>();
		for (ModelElement e1: model.surface.getElements()) {
			if (e1 instanceof ModelElementAnimationPause) list.add((ModelElementAnimationPause)e1);
			if (e1 instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e1).getSubSurface().getElements()) if (e2 instanceof ModelElementAnimationPause) list.add((ModelElementAnimationPause)e2);
		}
		return list;
	}

	/**
	 * Listenrenderer für einen Haltepunkt
	 */
	private class BreakpointListCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=6290449032349134345L;

		/**
		 * Allgemeiner HTML-Vorspann (damit eine Zeichenkette als html-Daten erkannt und interpretiert wird)
		 */
		public static final String htmlHead="<html><body>";

		/**
		 * Allgemeiner HTML-Abspann (damit eine Zeichenkette als html-Daten erkannt und interpretiert wird)
		 */
		public static final String htmlFoot="</body></html>";

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Color[] backgroundColors=ElementRendererTools.getBackgroundColor(isSelected,ElementRendererTools.GradientStyle.OFF);
			if (value instanceof BreakPoint) {
				final BreakPoint breakpoint=(BreakPoint)value;
				final ModelElement element=model.surface.getByIdIncludingSubModels(breakpoint.stationID);
				if (element instanceof ModelElementPosition) {
					final ModelElementPosition elementPosition=(ModelElementPosition)element;
					final StringBuilder info=new StringBuilder();
					info.append(ElementRendererTools.getElementHTMLInfo(elementPosition));
					if (breakpoint.clientType>=0) info.append(String.format("<br><i>"+Language.tr("Editor.Breakpoints.TabBreakpoints.ModeClientType")+"</i>",clientTypes[breakpoint.clientType]));
					if (breakpoint.condition!=null) info.append(String.format("<br><i>"+Language.tr("Editor.Breakpoints.TabBreakpoints.ModeCondition")+"</i>",breakpoint.condition));
					if (breakpoint.autoDelete) info.append("<br><i>"+Language.tr("Editor.Breakpoints.TabBreakpoints.ModeAutoDelete")+"</i>");
					return ElementRendererTools.getElementRenderer(elementPosition,htmlHead+info.toString()+htmlFoot,true,backgroundColors,-1,-1);
				}
			}
			return super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		}
	}
}