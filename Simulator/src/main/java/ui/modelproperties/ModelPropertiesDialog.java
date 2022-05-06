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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSchedules;
import ui.tools.WindowSizeStorage;

/**
 * Dieser Dialog ermöglicht das Bearbeiten der zentralen Eigenschaften
 * eines Editor-Modells, die nicht direkt über die Zeichenfläche
 * konfiguriert werden können (wie Bedienergruppen, Einstellungen zur
 * Simulation als solches, Ausgabeanalyse usw.)
 * @author Alexander Herzog
 * @see EditModel
 */
public class ModelPropertiesDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3736946126004680597L;

	/**
	 * Legt fegt, welche Dialogseite beim Aufruf des Dialogs initial angezeigt werden soll.
	 * @author Alexander Herzog
	 */
	public enum InitialPage {
		/** Dialogseite "Modellbeschreibung" */
		DESCRIPTION(ModelPropertiesDialogPageDescription.class,()->Language.tr("Editor.Dialog.Tab.ModelDescription"),Images.MODELPROPERTIES_DESCRIPTION,InfoPanel.modelDescription),

		/** Dialogseite "Simulation" */
		SIMULATION(ModelPropertiesDialogPageSimulation.class,()->Language.tr("Editor.Dialog.Tab.Simulation"),Images.MODELPROPERTIES_SIMULATION,InfoPanel.modelSimulation),

		/** Dialogseite "Kunden" */
		CLIENTS(ModelPropertiesDialogPageClients.class,()->Language.tr("Editor.Dialog.Tab.Clients"),Images.MODELPROPERTIES_CLIENTS,InfoPanel.modelClients),

		/** Dialogseite "Bediener" */
		OPERATORS(ModelPropertiesDialogPageOperators.class,()->Language.tr("Editor.Dialog.Tab.Operators"),Images.MODELPROPERTIES_OPERATORS,InfoPanel.modelOperators),

		/** Dialogseite "Transporter" */
		TRANSPORTERS(ModelPropertiesDialogPageTransporters.class,()->Language.tr("Editor.Dialog.Tab.Transporters"),Images.MODELPROPERTIES_TRANSPORTERS,InfoPanel.modelTransporters),

		/** Dialogseite "Zeitpläne" */
		SCHEDULES(ModelPropertiesDialogPageSchedules.class,()->Language.tr("Editor.Dialog.Tab.Schedule"),Images.MODELPROPERTIES_SCHEDULES,InfoPanel.modelSchedule),

		/** Dialogseite "Fertigungspläne" */
		SEQUENCES(ModelPropertiesDialogPageSequences.class,()->Language.tr("Editor.Dialog.Tab.Sequences"),Images.MODELPROPERTIES_SEQUENCES,InfoPanel.modelSequences),

		/** Dialogseite "Initiale Variablenwerte" */
		INITIAL_VALUES(ModelPropertiesDialogPageInitialValues.class,()->Language.tr("Editor.Dialog.Tab.InitialVariableValues"),Images.MODELPROPERTIES_INITIAL_VALUES,InfoPanel.modelInitialValues),

		/** Dialogseite "Laufzeitstatistik" */
		RUN_TIME_STATISTICS(ModelPropertiesDialogPageRunTimeStatistics.class,()->Language.tr("Editor.Dialog.Tab.RunTimeStatistics"),Images.MODELPROPERTIES_RUNTIME_STATISTICS,InfoPanel.modelRunTimeStatistics),

		/** Dialogseite "Ausgabeanalyse" */
		OUTPUT_ANALYSIS(ModelPropertiesDialogPageOutputAnalysis.class,()->Language.tr("Editor.Dialog.Tab.OutputAnalysis"),Images.MODELPROPERTIES_OUTPUT_ANALYSIS,InfoPanel.modelOutputAnalysis),

		/** Dialogseite "Ausgabeanalyse" */
		PATH_RECORDING(ModelPropertiesDialogPagePathRecording.class,()->Language.tr("Editor.Dialog.Tab.PathRecording"),Images.MODELPROPERTIES_PATH_RECORDING,InfoPanel.modelPathRecording),

		/** Dialogseite "Simulationssystem" */
		INFO(ModelPropertiesDialogPageInfo.class,()->Language.tr("Editor.Dialog.Tab.SimulationSystem"),Images.MODELPROPERTIES_INFO,InfoPanel.modelSimulationSystem);

		/** Klasse die die eigentliche Dialogseite darstellt */
		private final Class<? extends ModelPropertiesDialogPage> pageClass;
		/** Callback das den Namen der Seite in der passenden Sprache liefert */
		private final Supplier<String> nameSupplier;
		/** Zu verwendendes Icon */
		private final Images image;
		/** Bezeichner für die Beschreibung oberhalb der Dialogseite */
		private final String descriptionID;

		/**
		 * Konstruktor der Enum-Klasse
		 * @param pageClass	Klasse die die eigentliche Dialogseite darstellt
		 * @param nameSupplier	Callback das den Namen der Seite in der passenden Sprache liefert
		 * @param image	Zu verwendendes Icon
		 * @param descriptionID	Bezeichner für die Beschreibung oberhalb der Dialogseite
		 */
		InitialPage(final Class<? extends ModelPropertiesDialogPage> pageClass, final Supplier<String> nameSupplier, final Images image, final String descriptionID) {
			this.pageClass=pageClass;
			this.nameSupplier=nameSupplier;
			this.image=image;
			this.descriptionID=descriptionID;
		}

		/**
		 * Erstellt die entsprechende Dialogseite.
		 * @param dialog	 Dialog in dem sich die Seite befinden soll
		 * @return	Neue Dialogseite
		 */
		public ModelPropertiesDialogPage getPage(final ModelPropertiesDialog dialog) {
			try {
				return pageClass.getDeclaredConstructor(ModelPropertiesDialog.class,EditModel.class,boolean.class,Runnable.class).newInstance(dialog,dialog.model,dialog.readOnly,dialog.help);
			} catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchMethodException|SecurityException e) {
				return null;
			}
		}

		/**
		 * Liefert den Namen der Dialogseite in der aktuellen Sprache.
		 * @return	Name der Dialogseite in der aktuellen Sprache
		 */
		public String getName() {
			return nameSupplier.get();
		}

		/**
		 * Liefert das Icon der Dialogseite.
		 * @return	Icon der Dialogseite
		 */
		public Icon getIcon() {
			return image.getIcon();
		}

		/**
		 * Fügt oben in dem Panel eine Beschreibung ein.
		 * @param panel	Bisheriges Panel
		 * @return	Neues Panel unterhalb der Beschreibung
		 */
		public JPanel buildDescriptionPanel(final JPanel panel) {
			return InfoPanel.addTopPanelAndGetNewContent(panel,descriptionID);
		}
	}

	/**
	 * Liefert nach dem Schließen des Dialogs eine Information darüber, welche Aktion als nächstes ausgeführt werden soll.
	 * @author Alexander Herzog
	 * @see ModelPropertiesDialog#getNextAction()
	 */
	public enum NextAction {
		/** Keine weitere geplante Aktion */
		NONE,
		/** Statistik-Batch-Größe bestimmen */
		FIND_BATCH_SIZE
	}

	/**
	 * Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 */
	private final EditModel model;

	/**
	 * Liefert nach dem Schließen des Dialogs eine Information darüber, welche Aktion als nächstes ausgeführt werden soll.
	 * @see #getNextAction()
	 */
	private NextAction nextAction=NextAction.NONE;

	/** Objekt zum lokalen Speichern der Zeitpläne (außerhalb des Modells, in das sie erst beim Schließen des Dialogs zurückgeschrieben werden */
	public final ModelSchedules localSchedules;
	/** Objekt zum lokalen Speichern der Bedienergruppen (außerhalb des Modells, in das sie erst beim Schließen des Dialogs zurückgeschrieben werden */
	public final ModelResources localResources;
	/** Hilfe-Callback für diesen Dialog (zur Übergabe beim Aufruf von Unterdialogen) */
	private final Runnable help;
	/** Dialogseiten */
	private List<ModelPropertiesDialogPage> pages;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param initialPage	Beim Aufruf des Dialogs anzuzeigende Seite (darf <code>null</code> sein)
	 * @see ModelPropertiesDialog.InitialPage
	 */
	public ModelPropertiesDialog(final Component owner, final EditModel model, final boolean readOnly, final InitialPage initialPage) {
		super(owner,Language.tr("Editor.Dialog.Title"),readOnly);

		this.model=model;
		localSchedules=model.schedules.clone();
		localResources=model.resources.clone();

		/* GUI */
		help=()->Help.topicModal(ModelPropertiesDialog.this.owner,"EditorModelDialog");
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		/* Tabs */
		final JTabbedPane tabs=new JTabbedPane();
		tabs.setTabPlacement(SwingConstants.LEFT);
		content.add(tabs,BorderLayout.CENTER);

		pages=new ArrayList<>();
		for (InitialPage page: InitialPage.values()) {
			final JPanel sub=new JPanel(new BorderLayout());
			tabs.addTab(page.getName(),sub);
			final ModelPropertiesDialogPage tab=page.getPage(this);
			pages.add(tab);
			tab.build(page.buildDescriptionPanel(sub));
			tabs.setIconAt(tabs.getTabCount()-1,page.getIcon());
		}

		/* Vergrößert die Tabs und setzt die Titel linksbündig */
		int maxWidth=0;
		for (int i=0;i<tabs.getTabCount();i++) {
			final JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JLabel l=new JLabel(tabs.getTitleAt(i));
			l.setIcon(tabs.getIconAt(i));
			p.setOpaque(false);
			l.setOpaque(false);
			p.add(l);
			maxWidth=Math.max(maxWidth,p.getPreferredSize().width);
			tabs.setTabComponentAt(i,p);
		}
		for (int i=0;i<tabs.getTabCount();i++) {
			final JPanel p=(JPanel)tabs.getTabComponentAt(i);
			p.setPreferredSize(new Dimension(maxWidth,p.getPreferredSize().height));
		}

		/* Dialog konfigurieren */
		setMinSizeRespectingScreensize(825,825);
		pack();
		if (getSize().width>900) setSize(900,getSize().height);
		if (getSize().height>900) setSize(getSize().width,900);
		setLocationRelativeTo(getOwner());
		setResizable(true);
		WindowSizeStorage.window(this,"modelproperties");

		/* Initial anzuzeigende Seite wählen */
		if (initialPage!=null) for (int i=0;i<InitialPage.values().length;i++) if (InitialPage.values()[i]==initialPage) {
			tabs.setSelectedIndex(i);
			break;
		}
	}

	/**
	 * Zeigt eine Warnung aus, wenn die gewählte Länge für die Korrelationsaufzeichnung
	 * oberhalb bzw. nah bei der Anzahl an insgesamt zu simulierenden Kunden liegt und
	 * damit eine vollständige Erfassung nicht möglich ist.
	 */
	public void testCorrelationWarning() {
		ModelPropertiesDialogPageSimulation pageSimulation=null;
		ModelPropertiesDialogPageOutputAnalysis pageOutputAnalysis=null;
		for (ModelPropertiesDialogPage page: pages) {
			if (page instanceof ModelPropertiesDialogPageSimulation) pageSimulation=(ModelPropertiesDialogPageSimulation)page;
			if (page instanceof ModelPropertiesDialogPageOutputAnalysis) pageOutputAnalysis=(ModelPropertiesDialogPageOutputAnalysis)page;
			if (pageSimulation!=null && pageOutputAnalysis!=null) break;
		}

		if (pageSimulation==null || pageOutputAnalysis==null) return;
		pageOutputAnalysis.testCorrelationWarning(pageSimulation.getTerminationClientCount());
	}

	/**
	 * Aktualisiert die Darstellung der Bedienergruppen.
	 */
	public void updateResourceTable() {
		for (ModelPropertiesDialogPage page: pages) if (page instanceof ModelPropertiesDialogPageOperators) {
			((ModelPropertiesDialogPageOperators)page).updateResourceTable();
			break;
		}
	}

	@Override
	protected boolean checkData() {
		for (ModelPropertiesDialogPage page: pages) if (!page.checkData()) return false;
		return true;
	}

	@Override
	protected void storeData() {
		for (ModelPropertiesDialogPage page: pages) page.storeData();
	}

	/**
	 * Schließt den Dialog per "Ok".
	 * @param nextAction	Nächste austzführende Aktion
	 * @see NextAction
	 */
	public void doClose(final NextAction nextAction) {
		if (!checkData()) return;
		this.nextAction=nextAction;
		close(CLOSED_BY_OK);
	}

	/**
	 * Liefert nach dem Schließen des Dialogs das bearbeitete Modell.
	 * @return	Bearbeitetes Modell
	 */
	public EditModel getModel() {
		return model;
	}

	/**
	 * Liefert nach dem Schließen des Dialogs eine Information darüber, welche Aktion als nächstes ausgeführt werden soll.
	 * @return	Nächste auszuführende Aktion
	 * @see ModelPropertiesDialog.NextAction
	 */
	public NextAction getNextAction() {
		return nextAction;
	}
}