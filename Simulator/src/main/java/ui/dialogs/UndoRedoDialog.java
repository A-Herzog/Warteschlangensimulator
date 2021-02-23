package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import gitconnect.GitSetup;
import gitconnect.GitTools;
import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.EditorPanel;
import ui.EditorPanelBase;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import xml.XMLTools;

/**
 * Zeigt einen Dialog zur Auswahl des Rückgängig- oder Wiederholen-Schritts an.
 * @see ModelSurfacePanel#doUnDoRedoByDialog()
 */
public class UndoRedoDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4596812098370479880L;

	/**
	 * Kopie des Gesamt-Modell (zur Einbettung der Zeichenfläche)
	 */
	private final EditModel baseModel;

	/**
	 * Original-Undo-Zeichenflächen (werden bei der Anzeige kopiert)
	 */
	private final List<ModelSurface> undoModels;

	/**
	 * Original-Zeichenfläche (wird bei der Anzeige kopiert)
	 */
	private final ModelSurface currentModel;

	/**
	 * Original-Redo-Zeichenflächen (werden bei der Anzeige kopiert)
	 */
	private final List<ModelSurface> redoModels;

	/**
	 * Listenansicht der verfügbaren Modelle
	 */
	private final JList<JLabel> list;

	/**
	 * Ansicht des gewählten Modells
	 */
	private final EditorPanel viewer;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Gesamt-Modell (zur Einbettung der Zeichenfläche)
	 * @param undoModels	Liste der möglichen Undo-Modelle
	 * @param currentModel	Aktuelles Modell
	 * @param redoModels	Liste der möglichen Redo-Modelle
	 */
	public UndoRedoDialog(final Component owner, final EditModel model, final List<ModelSurface> undoModels, final ModelSurface currentModel, final List<ModelSurface> redoModels) {
		super(owner,Language.tr("UndoRedoSelect.Title"));

		/* Daten übernehmen */
		this.baseModel=model.clone();
		this.undoModels=(undoModels==null)?new ArrayList<>():undoModels;
		this.currentModel=currentModel;
		this.redoModels=(redoModels==null)?new ArrayList<>():redoModels;

		addUserButton(Language.tr("UndoRedoSelect.Save"),Images.GENERAL_SAVE.getIcon());
		getUserButton(0).setToolTipText(Language.tr("UndoRedoSelect.Save.Tooltip"));

		/* GUI erstellen */
		final JPanel content=createGUI(1200,800,()->Help.topicModal(UndoRedoDialog.this,"UndoRedoDialog"));
		content.setLayout(new BorderLayout());
		final JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		content.add(split);

		/* Liste */
		split.setLeftComponent(new JScrollPane(list=new JList<>(getModelsList())));
		list.addListSelectionListener(e->setModel(getStep(getSelectedStep())));
		list.setCellRenderer(new ElementListCellRenderer());
		list.getParent().setMinimumSize(new Dimension(100,0));

		/* Modellansicht */
		split.setRightComponent(viewer=new EditorPanel(this,null,true,true,false,false));
		viewer.setSavedViewsButtonVisible(false);
		viewer.setMinimumSize(new Dimension(500,0));
		list.setSelectedIndex(this.undoModels.size());
		setModel(getStep(0));

		/* Split einstellen */
		split.setDividerLocation(0.2);

		/* Dialog starten */
		setMinSizeRespectingScreensize(1200,800);
		setSizeRespectingScreensize(1200,800);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Namen der verfügbaren Modelle für {@link #list}.
	 * @return	Namen der verfügbaren Modelle
	 * @see #list
	 */
	private Vector<JLabel> getModelsList() {
		final Vector<JLabel> list=new Vector<>();

		for (int i=undoModels.size();i>=1;i--) {
			list.add(new JLabel(String.format(Language.tr("UndoRedoSelect.UndoStep"),i),Images.MODEL.getIcon(),SwingConstants.LEADING));
		}

		list.add(new JLabel(Language.tr("UndoRedoSelect.CurrentStep"),Images.MODEL.getIcon(),SwingConstants.LEADING));

		for (int i=1;i<=redoModels.size();i++) {
			list.add(new JLabel(String.format(Language.tr("UndoRedoSelect.RedoStep"),i),Images.MODEL.getIcon(),SwingConstants.LEADING));
		}

		return list;
	}

	/**
	 * Gibt die Nummer des aktuell gewählten Modells zurück.<br>
	 * (Werte &lt;0 für Undo-Modelle, Werte &gt;0 für Redo-Modelle und 0 für das aktuelle Modell)
	 * @return	Nummer des aktuell gewählten Modells
	 */
	public int getSelectedStep() {
		final int index=list.getSelectedIndex();
		if (index<0) return 0;

		return index-undoModels.size();
	}

	/**
	 * Liefert die anzuzeigende Zeichenfläche.<br>
	 * (Werte &lt;0 für Undo-Modelle, Werte &gt;0 für Redo-Modelle und 0 für das aktuelle Modell)
	 * @param step	Nummer der anzuzeigenden Zeichenfläche
	 * @return	Zeichenfläche
	 */
	private ModelSurface getStep(final int step) {
		if (step<0) {
			/* Undo */
			final int undoStep=step+undoModels.size();
			if (undoStep>=0 && undoStep<undoModels.size()) {
				return undoModels.get(undoStep);
			}
		}

		if (step>0) {
			/* Redo */
			final int redoStep=redoModels.size()-step;
			if (redoStep>=0 && redoStep<redoModels.size()) {
				return redoModels.get(redoStep);
			}
		}

		return currentModel;
	}

	/**
	 * Erstellt aus einer Zeichenfläche ein vollständiges Modell
	 * @param surface	Zeichenfläche
	 * @return	Neues Modell (mit Kopie des Basismodells und Kopie der Zeichenfläche)
	 */
	private EditModel getModel(final ModelSurface surface) {
		final EditModel model=baseModel.clone();
		model.surface=surface.clone(false,model.resources,model.schedules,null,model);
		return model;
	}

	/**
	 * Stellt das anzuzeigende Modell ein.
	 * @param surface	Anzuzeigendes Modell
	 */
	private void setModel(final ModelSurface surface) {
		viewer.setModel(getModel(surface));
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		final File file=XMLTools.showSaveDialog(this,EditorPanelBase.SAVE_MODEL,SetupData.getSetup().defaultSaveFormatModels);
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return;
		}

		final EditModel model=getModel(getStep(getSelectedStep()));

		if (!model.saveToFile(file)) {
			MsgBox.error(this,Language.tr("XML.SaveErrorTitle"),EditorPanelBase.SAVE_MODEL_ERROR);
			return;
		}

		GitTools.saveFile(this,model.author,model.authorEMail,file,GitSetup.GitSaveMode.MODELS);
	}

	/**
	 * Renderer für die Liste der Elemente
	 * @see UndoRedoDialog#list
	 */
	private class ElementListCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 4327039078742103357L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((ElementListCellRenderer)renderer).setText(((JLabel)value).getText());
				((ElementListCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
				((ElementListCellRenderer)renderer).setBorder(BorderFactory.createEmptyBorder(2,5,2,10));
			}
			return renderer;
		}
	}
}
