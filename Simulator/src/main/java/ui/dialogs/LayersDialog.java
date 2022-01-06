package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog zum Bearbeiten der Ebenen in einem Modell.
 * @author Alexander Herzog
 * @see ModelSurface#getLayers()
 * @see ModelSurface#getVisibleLayers()
 * @see ModelSurface#getActiveLayer()
 */
public class LayersDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6848493693191362564L;

	/** Modell aus dem die Ebenendaten ausgelesen werden und auch in das die Daten zur�ckgeschrieben werden */
	private final EditModel model;
	/** Liste der Ebenen */
	private JList<JLabel> list;
	/** Datenmodell f�r die Liste der Ebenen */
	private DefaultListModel<JLabel> listModel;
	/** "Hinzuf�gen"-Schaltfl�che */
	private JButton buttonAdd;
	/** "Umbenennen"-Schaltfl�che */
	private JButton buttonRename;
	/** "L�schen"-Schaltfl�che */
	private JButton buttonDelete;
	/** Sichtbar/Unsichtbar-Umschalten-Schaltfl�che */
	private JButton buttonVisible;
	/** Aktiv/Nicht-aktiv-Umschalten-Schaltfl�che */
	private JButton buttonActive;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param model	Modell aus dem die Ebenendaten ausgelesen werden und auch in das die Daten zur�ckgeschrieben werden
	 * @param readOnly	Nur-Lese-Status
	 */
	public LayersDialog(final Component owner, final EditModel model, final boolean readOnly) {
		super(owner,Language.tr("Window.Layers.Title"),readOnly);
		this.model=model;

		final JPanel all=createGUI(()->Help.topicModal(this,"Layers"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalLayers);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Symbolleiste */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.add(buttonAdd=addButton(Language.tr("Window.Layers.Add.Title"),Language.tr("Window.Layers.Add.Hint"),Images.EDIT_ADD,modifiers->commandAdd()));
		toolbar.add(buttonRename=addButton(Language.tr("Window.Layers.Rename.Title"),Language.tr("Window.Layers.Rename.Hint"),Images.GENERAL_SETUP,modifiers->commandRename(list.getSelectedIndex())));
		toolbar.add(buttonDelete=addButton(Language.tr("Window.Layers.Delete.Title"),Language.tr("Window.Layers.Delete.Hint"),Images.EDIT_DELETE,modifiers->commandDelete(list.getSelectedIndex(),(modifiers & ActionEvent.SHIFT_MASK)!=0)));
		toolbar.add(buttonVisible=addButton(Language.tr("Window.Layers.Visible.Title"),Language.tr("Window.Layers.Visible.Hint"),Images.EDIT_LAYERS_VISIBLE,modifiers->commandChangeVisible(list.getSelectedIndex())));
		toolbar.add(buttonActive=addButton(Language.tr("Window.Layers.Active.Title"),Language.tr("Window.Layers.Active.Hint"),Images.MODEL_LOAD,modifiers->commandSetActive()));

		/* Liste */
		listModel=new DefaultListModel<>();
		content.add(new JScrollPane(list=new JList<>(listModel)),BorderLayout.CENTER);

		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID=-3396935987663371955L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel l=(JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				l.setText(((JLabel)value).getText());
				l.setIcon(((JLabel)value).getIcon());
				return l;
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					if (list.getSelectedIndex()>=0) commandChangeVisible(list.getSelectedIndex());
					e.consume();
					return;
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					showContextMenu(e);
					e.consume();
					return;
				}
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT && !e.isControlDown() && !e.isShiftDown()) {commandAdd(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandChangeVisible(list.getSelectedIndex()); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandRename(list.getSelectedIndex()); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown()) {if (list.getSelectedIndex()<0) return; commandDelete(list.getSelectedIndex(),e.isShiftDown()); e.consume(); return;}
			}
		});
		list.addListSelectionListener(e->updateButtons());
		updateList(null);

		/* Dialog starten */
		setMinSizeRespectingScreensize(400,300);
		setSizeRespectingScreensize(500,400);
		setResizable(true);
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Erstellt eine neue Schaltfl�che
	 * @param title	Beschriftung der Schaltfl�che
	 * @param hint	Tooltip f�r die Schaltfl�che
	 * @param icon	Icon f�r die Schaltfl�che
	 * @param command	Beim Anklicken der Schaltfl�che auszuf�hrender Befehl
	 * @return	Neue Schaltfl�che
	 */
	private JButton addButton(final String title, final String hint, final Images icon, final IntConsumer command) {
		final JButton button=new JButton(title,icon.getIcon());
		button.setToolTipText(hint);
		button.addActionListener(e->command.accept(e.getModifiers()));
		return button;
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entit�ten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private static String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Aktualisiert die Liste der Ebenen.
	 * @param selectByName	Nach dem Aufbau der Liste zu selektierende Ebene (wird <code>null</code> �bergeben, so versucht die Funktion die vorher selektrierte Ebene wieder auszuw�hlen)
	 */
	private void updateList(final String selectByName) {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();
		final String activeLayer=model.surface.getActiveLayer();

		/* Bisher selektierte Ebene */
		String lastSelected=null;
		if (selectByName==null) {
			if (list.getSelectedIndex()>=0 && list.getSelectedIndex()<layers.size()) lastSelected=layers.get(list.getSelectedIndex());
		} else {
			lastSelected=selectByName;
		}

		/* Neue Liste aufbauen */
		int lastIndex=-1;
		listModel.clear();
		for (int i=0;i<layers.size();i++) {
			final String layer=layers.get(i);
			final boolean visible=visibleLayers.contains(layer);
			if (Objects.equals(lastSelected,layer)) lastIndex=i;
			final StringBuilder text=new StringBuilder();
			text.append("<html><body>\n");
			text.append(Language.tr("Window.Layers.List.Layer")+": <b>"+encodeHTMLentities(layer)+"</b><br>\n");
			if (visible) text.append(Language.tr("Window.Layers.List.IsVisible")); else text.append(Language.tr("Window.Layers.List.IsNotVisible"));
			if (Objects.equals(layer,activeLayer)) text.append(", <b>"+Language.tr("Window.Layers.List.Active")+"</b>");
			text.append("</body></html>\n");
			final JLabel label=new JLabel(text.toString());
			if (visible) label.setIcon(Images.EDIT_LAYERS_VISIBLE.getIcon()); else label.setIcon(Images.EDIT_LAYERS_INVISIBLE.getIcon());
			listModel.addElement(label);
		}

		/* Evtl. Selektion wiederherstellen */
		list.setSelectedIndex(lastIndex);
		updateButtons();
	}

	/**
	 * Aktiviert in Abh�ngigkeit von der in der Liste gew�hlten Ebene
	 * die Schaltfl�chen zur Konfiguration der Ebenen.
	 * @see #list
	 */
	private void updateButtons() {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();
		final String activeLayer=model.surface.getActiveLayer();

		/* Buttons anpassen */
		buttonAdd.setEnabled(!readOnly);
		buttonRename.setEnabled(!readOnly && list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(!readOnly && list.getSelectedIndex()>=0);
		buttonVisible.setEnabled(!readOnly && list.getSelectedIndex()>=0);

		if (list.getSelectedIndex()>=0) {
			final String layer=layers.get(list.getSelectedIndex());

			if (visibleLayers.contains(layer)) {
				buttonVisible.setText(Language.tr("Window.Layers.Invisible.Title"));
				buttonVisible.setToolTipText(Language.tr("Window.Layers.Invisible.Hint"));
				buttonVisible.setIcon(Images.EDIT_LAYERS_INVISIBLE.getIcon());
			} else {
				buttonVisible.setText(Language.tr("Window.Layers.Visible.Title"));
				buttonVisible.setToolTipText(Language.tr("Window.Layers.Visible.Hint"));
				buttonVisible.setIcon(Images.EDIT_LAYERS_VISIBLE.getIcon());
			}

			buttonVisible.setEnabled(true);
			buttonActive.setEnabled(!Objects.equals(layer,activeLayer));
		} else {
			buttonVisible.setEnabled(false);
			buttonActive.setEnabled(false);
		}
	}

	/**
	 * Befehl: Ebene hinzuf�gen
	 */
	private void commandAdd() {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();

		/* Vorschlag f�r Name f�r neue Ebene */
		String initialName=Language.tr("Window.Layers.Add.DefaultName");
		int nr=1;
		while (layers.contains(initialName)) {
			nr++;
			initialName=Language.tr("Window.Layers.Add.DefaultName")+nr;
		}

		/* Name eingaben und pr�fen */
		String name;
		while (true) {
			name=JOptionPane.showInputDialog(this,Language.tr("Window.Layers.Add.NewLayerLabel"),initialName);
			if (name==null) return;
			if (!layers.contains(name)) break;
			MsgBox.error(this,Language.tr("Window.Layers.Add.ErrorTitle"),Language.tr("Window.Layers.Add.ErrorInfo"));
		}

		/* Ebene hinzuf�gen */
		layers.add(name);
		visibleLayers.add(name);
		if (model.surface.getActiveLayer()==null) model.surface.setActiveLayer(name);
		updateList(name);
		list.setSelectedIndex(layers.size()-1);
		updateButtons();
	}

	/**
	 * Befehl: Ebene umbenennen
	 * @param index	Index der Ebene in der Liste
	 */
	private void commandRename(final int index) {
		final String oldName=model.surface.getLayers().get(index);
		final String newName=JOptionPane.showInputDialog(this,Language.tr("Window.Layers.Rename.RenameLayerLabel"),oldName);
		if (newName==null) return;

		model.renameLayer(oldName,newName);

		updateList(newName);
	}

	/**
	 * Befehl: Ebene l�schen
	 * @param index	Index der Ebene in der Liste
	 *  @param isShiftDown	Ist die Umschalttaste gedr�ckt? (Wenn ja, l�schen ohne Nachfrage.)
	 */
	private void commandDelete(final int index, final boolean isShiftDown) {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();
		final String activeLayer=model.surface.getActiveLayer();

		/* Abfrage */
		final String layer=layers.get(index);
		if (!isShiftDown) {
			if (!MsgBox.confirm(this,Language.tr("Window.Layers.Delete.ConfirmTitle"),String.format(Language.tr("Window.Layers.Delete.ConfirmInfo"),layer),Language.tr("Window.Layers.Delete.ConfirmInfo.Yes"),Language.tr("Window.Layers.Delete.ConfirmInfo.No"))) return;
		}

		/* Layer l�schen */
		layers.remove(layer);
		visibleLayers.remove(layer);
		if (Objects.equals(activeLayer,layer)) model.surface.setActiveLayer(null);

		/* Elemente anpassen */
		for (ModelElement element: model.surface.getElementsIncludingSubModels()) {
			final List<String> elementLayers=element.getLayers();
			elementLayers.remove(layer);
		}

		updateList(null);
	}

	/**
	 * Befehl: Sichtbarkeitsstatus der Ebene umschalten
	 * @param index	Index der Ebene in der Liste
	 */
	private void commandChangeVisible(final int index) {
		final String layer=model.surface.getLayers().get(index);

		final List<String> visibleLayers=model.surface.getVisibleLayers();
		if (visibleLayers.contains(layer)) visibleLayers.remove(layer); else visibleLayers.add(layer);

		updateList(null);
	}

	/**
	 * Befehl: Gew�hlte Ebene als aktuelle Ebene (f�r das Hinzuf�gen von Elementen) w�hlen
	 */
	private void commandSetActive() {
		final String layer=model.surface.getLayers().get(list.getSelectedIndex());
		model.surface.setActiveLayer(layer);

		updateList(null);
	}

	/**
	 * Erstellt auf Basis einer Schaltfl�che einen Men�punkt
	 * @param button	Ausgangsschaltfl�che
	 * @return	Neuer Men�punkt
	 * @see #showContextMenu(MouseEvent)
	 */
	private JMenuItem buttonToMenu(final JButton button) {
		final JMenuItem item=new JMenuItem(button.getText(),button.getIcon());
		item.setToolTipText(button.getToolTipText());
		for (ActionListener listener : button.getActionListeners()) item.addActionListener(listener);
		item.setEnabled(button.isEnabled());
		return item;
	}

	/**
	 * Zeigt das Kontextmen� zu einem Listeneintrag an.
	 * @param event	Ausl�sendes Mausereignis
	 */
	private void showContextMenu(final MouseEvent event) {
		final JPopupMenu menu=new JPopupMenu();

		menu.add(buttonToMenu(buttonAdd));
		menu.add(buttonToMenu(buttonRename));
		menu.add(buttonToMenu(buttonDelete));
		menu.addSeparator();
		menu.add(buttonToMenu(buttonVisible));
		menu.add(buttonToMenu(buttonActive));

		menu.show(list,event.getX(),event.getY());
	}

	@Override
	protected void storeData() {
		final String activeLayer=model.surface.getActiveLayer();
		if (activeLayer!=null) {
			final List<String> visibleLayers=model.surface.getVisibleLayers();
			if (!visibleLayers.contains(activeLayer)) model.surface.setActiveLayer(null);
		}
	}

	/**
	 * Liefert das im Konstruktor �bergebene Modell (ge�ndert oder unver�ndert) zur�ck.<br>
	 * Das Modell ist evtl. auch dann ver�ndert, wenn der Nutzer auf "Abbrechen" klickt.
	 * @return	Im Konstruktor �bergebene Modell
	 */
	public EditModel getModel() {
		return model;
	}
}
