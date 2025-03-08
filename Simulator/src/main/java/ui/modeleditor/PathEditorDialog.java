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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;
import ui.modeleditor.elements.ModelElementWayPoint;

/**
 * Dialog zur Festlegung von gewünschten Pfaden für die Transporter.<br>
 * Die Pfade werden später durch Einstellungen in den {@link ModelElementWayPoint}-Elementen erzeugt.
 * @author Alexander Herzog
 * @see PathBuilder
 */
public class PathEditorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3832675453026139621L;

	/** System zur Erstellung der Pfade */
	private final PathBuilder builder;
	/** Liste der Pfadpunkte */
	private final List<PathPoint> data;
	/** Vorschaubereich */
	private final JTextArea previewArea;
	/** Vorschaubereich nicht aktualisieren? */
	private boolean disablePreviewUpdate;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param mainSurface	Zeichenfläche auf der Hauptebene
	 * @param setup	Objekt, aus dem die Einstellungen geladen werden sollen
	 */
	public PathEditorDialog(final Component owner, final ModelSurface mainSurface, final ModelPaths setup) {
		super(owner,Language.tr("PathEditor.Title"));
		disablePreviewUpdate=false;

		/* Daten vorbereiten */
		builder=new PathBuilder(mainSurface);
		data=new ArrayList<>();
		buildStationsList(mainSurface,null);
		data.sort((p1,p2)->p1.getName().compareTo(p2.getName()));
		final long wayPoints=data.stream().filter(p->p.getElement() instanceof ModelElementWayPoint).count();
		if (wayPoints==0) {
			MsgBox.error(owner,Language.tr("PathEditor.NoStationsError.Title"),Language.tr("PathEditor.NoStationsError.Info"));
			previewArea=null;
			return;
		}
		for (PathPoint point: data) point.initOptions(data);

		/* GUI aufbauen */
		addUserButton(Language.tr("PathEditor.ResetAll"),Language.tr("PathEditor.ResetAll.Hint"),Images.EDIT_UNDO.getIcon());
		JPanel content=createGUI(()->Help.topicModal(PathEditorDialog.this,"PathEditorDialog"));
		content.setLayout(new BorderLayout());
		content=InfoPanel.addTopPanelAndGetNewContent(content,InfoPanel.globalPathEditor);

		JTabbedPane tabs=new JTabbedPane();

		content.add(tabs,BorderLayout.CENTER);
		JPanel tab;
		JScrollPane scroll;
		tabs.add(Language.tr("PathEditor.Tab.Setup"),tab=new JPanel(new BorderLayout()));
		tab.add(scroll=new JScrollPane(getSetupPanel()),BorderLayout.CENTER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		tabs.add(Language.tr("PathEditor.Tab.Result"),tab=new JPanel(new BorderLayout()));
		tab.add(new JScrollPane(previewArea=new JTextArea()),BorderLayout.CENTER);
		previewArea.setEditable(false);

		tabs.setIconAt(0,Images.EDIT_EDGES_ADD.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_VERTEX.getIcon());

		/* Daten laden */
		if (setup!=null) loadSetup(setup);

		/* Dialog anzeigen */
		setMinSizeRespectingScreensize(600,700);
		setSizeRespectingScreensize(600,700);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Generiert das Einstellungen-Panel.
	 * @return	Einstellungen-Panel
	 */
	private JPanel getSetupPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		for (PathPoint point: data) content.add(point.getPanel());
		return content;
	}

	/**
	 * Bereitet den Pfad-Builder vor.
	 */
	private void prepareBuilder() {
		builder.clear();
		for (PathPoint point: data) point.addToBuilder(builder);
	}

	/**
	 * Aktualisiert die Vorschauansicht.
	 * @see #previewArea
	 */
	private void updatePreview() {
		if (previewArea==null || disablePreviewUpdate) return;
		prepareBuilder();
		previewArea.setText(String.join("\n",builder.runInfo()));
	}

	/**
	 * Erstellt die Liste der Stationen
	 * @param surface	Zeichenfläche
	 * @param parentSurface	Übergeordnete Zeichenfläche oder <code>null</code>, wenn die Zeichenfläche aus dem vorherigen Parameter bereits die Haupt-Zeichenfläche ist
	 */
	private void buildStationsList(final ModelSurface surface, final ModelSurface parentSurface) {
		for (ModelElement element: surface.getElements()) {
			/* Untermodelle */
			if (parentSurface==null && (element instanceof ModelElementSub)) {
				buildStationsList(((ModelElementSub)element).getSubSurface(),surface);
				continue;
			}

			/* Test: Transporter-relevantes Element? */
			if (element.getName().isBlank()) continue;
			boolean transporterElement=false;
			if (element instanceof ModelElementTransportTransporterSource) transporterElement=true;
			if (element instanceof ModelElementTransportParking) transporterElement=true;
			if (element instanceof ModelElementTransportDestination) transporterElement=true;
			if (element instanceof ModelElementWayPoint) transporterElement=true;
			if (!transporterElement) continue;

			/* Zur Liste hinzufügen */
			final PathPoint newPoint=new PathPoint(element,(parentSurface==null)?surface:parentSurface);
			boolean ok=true;
			for (PathPoint point: data) if (point.getName().equals(newPoint.getName())) {ok=false; break;}
			if (ok) {
				data.add(newPoint);
				newPoint.addChangeListener(()->updatePreview());
			}
		}
	}

	@Override
	protected boolean checkData() {
		return MsgBox.confirm(this,Language.tr("PathEditor.ProcessConfirm.Title"),Language.tr("PathEditor.ProcessConfirm.Info"),Language.tr("PathEditor.ProcessConfirm.YesInfo"),Language.tr("PathEditor.ProcessConfirm.NoInfo"));
	}

	@Override
	protected void storeData() {
		prepareBuilder();
		builder.run();
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (!MsgBox.confirm(this,Language.tr("PathEditor.ResetAll.ConfirmTitle"),Language.tr("PathEditor.ResetAll.ConfirmInfo"),Language.tr("PathEditor.ResetAll.ConfirmInfoYes"),Language.tr("PathEditor.ResetAll.ConfirmInfoNo"))) return;

		boolean disable=disablePreviewUpdate;
		disablePreviewUpdate=true;
		try {
			for (PathPoint point: data) point.reset();
		} finally {
			disablePreviewUpdate=disable;
		}
		updatePreview();
	}

	/**
	 * Lädt die Einstellungen aus einem {@link ModelPaths}-Element.<br>
	 * Wird vom Konstruktor aufgerufen
	 * @param paths	Objekt, aus dem die Einstellungen geladen werden sollen
	 * @see ModelPaths
	 */
	public void loadSetup(final ModelPaths paths) {
		for (Map.Entry<String,List<String>> entry: paths.connections.entrySet()) {
			PathPoint point=null;
			for (PathPoint test: data) if (test.getName().equals(entry.getKey())) {point=test; break;}
			if (point!=null) point.setNextNamesList(entry.getValue());
		}

		for (PathPoint point: data) point.setNextNamesListCheckBoxes();
	}

	/**
	 * Speichert die Einstellungen in einem {@link ModelPaths}-Element.<br>
	 * Wird nicht automatisch aufgerufen.
	 * @param paths	Objekt, in dem die Einstellungen gespeichert werden sollen
	 * @see ModelPaths
	 */
	public void storeSetup(final ModelPaths paths) {
		paths.clear();
		for (PathPoint point: data) paths.connections.put(point.getName(),point.getNextNamesList());
	}

}
