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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.EditorPanel;
import ui.help.Help;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.tools.GlassInfo;
import ui.tools.WindowSizeStorage;

/**
 * In diesem Dialog kann ein in einem {@link ModelElementSub}-Element
 * enthaltenes Unter-Modell bearbeitet werden.
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6057942278443843417L;

	/** Editor-Panel in dem das Untermodell bearbeitet werden kann */
	private final EditorPanel editorPanel;

	/**
	 * Konstruktor der Klasse <code>ModelElementSubEditDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param id	ID des Sub-Elements, dessen Inhalt hier bearbeitet werden soll
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param mainSurface	Surface der obersten Ebene (enthält Ressourcen usw.)
	 * @param subSurface	Zu bearbeitendes Surface
	 * @param edgesIn	In das untergeordnete Modell einlaufende Ecken (mit ids)
	 * @param edgesOut	Aus dem untergeordneten Modell auslaufende Ecken (mit ids)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param wasTriggeredViaEditDialog	Wurde der Dialog auf dem Umweg über den Untermodell-Bearbeiten-Dialog aufgerufen? (Wenn ja, wird auf der Untermodell-Zeichenfläche ein Hinweis zum direkten Aufruf angezeigt.)
	 * @param isFullSubModel	Handelt es sich um ein vollwertiges Untermodell (<code>true</code>) oder um die Diagramm-Anzeige (<code>false</code>)
	 */
	public ModelElementSubEditDialog(final Component owner, final int id, final EditModel model, final ModelSurface mainSurface, final ModelSurface subSurface, final int[] edgesIn, final int[] edgesOut, final boolean readOnly, final boolean wasTriggeredViaEditDialog, final boolean isFullSubModel) {
		super(owner,isFullSubModel?Language.tr("Surface.Sub.Dialog.Title"):Language.tr("Surface.Dashboard.Dialog.Title"),readOnly);

		/* Modell */
		final EditModel ownModel=model.clone();
		ownModel.resources=mainSurface.getResources().clone();
		ownModel.surface=subSurface.clone(false,null,null,mainSurface,model);

		/* Beschriftungen der Ein- und Ausgänge anpassen */
		int countIn=0;
		int countOut=0;
		for (ModelElement element: ownModel.surface.getElements()) {
			if (element instanceof ModelElementSubIn) {
				if (countIn<edgesIn.length) {
					final ModelElementSubIn in=(ModelElementSubIn)element;
					final ModelElement previous=mainSurface.getById(edgesIn[countIn]);
					if (previous!=null) in.setConnectionData(countIn,previous.getId());
				}
				countIn++;

			}
			if (element instanceof ModelElementSubOut) {
				if (countOut<edgesOut.length) {
					final ModelElementSubOut out=(ModelElementSubOut)element;
					final ModelElement next=mainSurface.getById(edgesOut[countOut]);
					if (next!=null) out.setConnectionData(countOut,next.getId());
				}
				countOut++;
			}
		}

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(ModelElementSubEditDialog.this,isFullSubModel?"ModelElementSub":"ModelElementDashboard"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,isFullSubModel?InfoPanel.stationSub:InfoPanel.stationDashboard);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Zeichenfläche */
		content.add(editorPanel=new EditorPanel(this,ownModel,false,readOnly,false,false),BorderLayout.CENTER);
		editorPanel.setSavedViewsButtonVisible(false);
		if (!isFullSubModel) editorPanel.setRestrictedCatalog(true);

		/* Hotkeys */
		final InputMap im=content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap am=content.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK),"zoomOut");
		am.put("zoomOut",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -8149785411312199622L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.zoomOut();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK),"zoomIn");
		am.put("zoomIn",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -4571322864625867012L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.zoomIn();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,InputEvent.CTRL_DOWN_MASK),"zoomDefault");
		am.put("zoomDefault",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -8292205126167185688L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.zoomDefault();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,InputEvent.CTRL_DOWN_MASK),"center");
		am.put("center",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -6623607719135188208L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.centerModel();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,InputEvent.CTRL_DOWN_MASK),"scrollTop");
		am.put("scrollTop",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 1935060801261351379L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.scrollToTop();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0),"templates");
		am.put("templates",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=1935060801261351379L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.setTemplatesVisible(!editorPanel.isTemplatesVisible(),false);}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,0),"navigator");
		am.put("navigator",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=-2815050201810091625L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.setNavigatorVisible(!editorPanel.isNavigatorVisible(),false);}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,InputEvent.CTRL_DOWN_MASK),"explorer");
		am.put("explorer",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=1935060801261351379L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.showExplorer();}
		});

		/* Dialog starten */
		setSizeRespectingScreensize(1024,768);
		setResizable(true);
		setMinSizeRespectingScreensize(800,600);
		setLocationRelativeTo(getOwner());
		WindowSizeStorage.window(this,""+id);

		if (wasTriggeredViaEditDialog) {
			GlassInfo.info(this,Language.tr("Surface.Sub.Dialog.DirectAccessHint"),500);
		}
	}

	/**
	 * Bereitet die Untermodell-Zeichenfläche vor.
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param mainSurface	Surface der obersten Ebene (enthält Ressourcen usw.)
	 * @param original	Original Untermodell-Zeichenfläche
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param edgesIn	IDs der von außen einlaufenden Kanten
	 * @param edgesOut	IDs der nach außen auslaufenden Kanten
	 * @return	Neue Untermodell-Zeichenfläche
	 */
	public static ModelSurface prepareSurface(final EditModel model, final ModelSurface mainSurface, final ModelSurface original, final boolean readOnly, final int[] edgesIn, final int[] edgesOut) {
		ModelSurface prepared=original.clone(false,null,null,mainSurface,model);

		if (!readOnly) {

			/* Überzählige Ein- und Ausgänge löschen */
			List<ModelElement> deleteElements=new ArrayList<>();
			int countIn=0;
			int countOut=0;
			for (ModelElement element: prepared.getElements()) {
				if (element instanceof ModelElementSubIn) {
					countIn++;
					if (countIn>edgesIn.length) deleteElements.add(element); else {
						((ModelElementSubConnect)element).setConnectionData(countIn-1,edgesIn[countIn-1]);
					}
				}
				if (element instanceof ModelElementSubOut) {
					countOut++;
					if (countOut>edgesOut.length) deleteElements.add(element); else {
						((ModelElementSubConnect)element).setConnectionData(countOut-1,edgesOut[countOut-1]);
					}
				}
			}

			for (ModelElement element: deleteElements) prepared.remove(element);

			Point pos=new Point(50,50);
			/* Fehlende Verbindungen anlegen */
			for (int i=countIn;i<edgesIn.length;i++) {
				final ModelElementSubIn connectionIn=new ModelElementSubIn(model,prepared,i,edgesIn[i]);
				while (prepared.getElementAtPosition(pos,1.0)!=null) pos.x+=50;
				connectionIn.setPosition(pos);
				pos.x+=150;
				prepared.add(connectionIn);
			}
			for (int i=countOut;i<edgesOut.length;i++) {
				final ModelElementSubOut connectionOut=new ModelElementSubOut(model,prepared,i,edgesOut[i]);
				while (prepared.getElementAtPosition(pos,1.0)!=null) pos.x+=50;
				connectionOut.setPosition(pos);
				pos.x+=150;
				prepared.add(connectionOut);
			}
		}

		/* Ebenen */
		prepared.getLayers().clear();
		prepared.getLayers().addAll(mainSurface.getLayers());
		prepared.getVisibleLayers().clear();
		prepared.getVisibleLayers().addAll(mainSurface.getVisibleLayers());
		prepared.setActiveLayer(mainSurface.getActiveLayer());

		return prepared;
	}

	/**
	 * Liefert das Untermodell nach dem Schließen des Dialogs zurück
	 * @return	Untergeordnetes Modell
	 */
	public ModelSurface getSurface() {
		final EditModel model=editorPanel.getModel();
		model.surface.setSelectedElement(null);
		return model.surface;
	}

	/**
	 * Liefert die möglicherweise veränderten Kunden-Einstellungen.
	 * @return	Kunden-Einstellungen
	 */
	public ModelClientData getClientData() {
		return editorPanel.getModel().clientData;
	}
}