package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

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
	private static final long serialVersionUID=6848493693191362564L;

	private final EditModel model;
	private DefaultListModel<JLabel> listModel;
	private JList<JLabel> list;
	private JButton buttonAdd;
	private JButton buttonRename;
	private JButton buttonDelete;
	private JButton buttonVisible;
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

		final JPanel content=createGUI(()->Help.topicModal(this,"Layers"));
		content.setLayout(new BorderLayout());

		/* Symbolleiste */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.add(buttonAdd=addButton(Language.tr("Window.Layers.Add.Title"),Language.tr("Window.Layers.Add.Hint"),Images.EDIT_ADD,()->commandAdd()));
		toolbar.add(buttonRename=addButton(Language.tr("Window.Layers.Rename.Title"),Language.tr("Window.Layers.Rename.Hint"),Images.GENERAL_SETUP,()->commandRename(list.getSelectedIndex())));
		toolbar.add(buttonDelete=addButton(Language.tr("Window.Layers.Delete.Title"),Language.tr("Window.Layers.Delete.Hint"),Images.EDIT_DELETE,()->commandDelete(list.getSelectedIndex())));
		toolbar.add(buttonVisible=addButton(Language.tr("Window.Layers.Visible.Title"),Language.tr("Window.Layers.Visible.Hint"),Images.EDIT_LAYERS_VISIBLE,()->commandChangeVisible(list.getSelectedIndex())));
		toolbar.add(buttonActive=addButton(Language.tr("Window.Layers.Active.Title"),Language.tr("Window.Layers.Active.Hint"),Images.MODEL_LOAD,()->commandSetActive()));

		/* Liste */
		listModel=new DefaultListModel<>();
		content.add(new JScrollPane(list=new JList<JLabel>(listModel)),BorderLayout.CENTER);

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
				if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandDelete(list.getSelectedIndex()); e.consume(); return;}
			}
		});
		list.addListSelectionListener(e->updateButtons());
		updateList();

		/* Dialog starten */
		setMinSizeRespectingScreensize(400,300);
		setSizeRespectingScreensize(500,400);
		setResizable(true);
		setLocationRelativeTo(getOwner());
	}

	private JButton addButton(final String title, final String hint, final Images icon, final Runnable command) {
		final JButton button=new JButton(title,icon.getIcon());
		button.setToolTipText(hint);
		button.addActionListener(e->command.run());
		return button;
	}

	private static String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	private void updateList() {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();
		final String activeLayer=model.surface.getActiveLayer();

		/* Bisher selektierte Ebene */
		String lastSelected=null;
		if (list.getSelectedIndex()>=0) lastSelected=layers.get(list.getSelectedIndex());

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
		updateList();
		list.setSelectedIndex(layers.size()-1);
		updateButtons();
	}

	private void commandRename(final int index) {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();
		final String activeLayer=model.surface.getActiveLayer();

		/* Neuen Namen abfragen */
		final String currentName=layers.get(index);
		final String newName=JOptionPane.showInputDialog(this,Language.tr("Window.Layers.Rename.RenameLayerLabel"),currentName);
		if (newName==null) return;
		if (!newName.equals(currentName) && model.surface.getLayers().contains(newName)) {
			MsgBox.error(this,Language.tr("Window.Layers.Rename.ErrorTitle"),Language.tr("Window.Layers.Rename.ErrorInfo"));
			return;
		}

		/* Layer umbenennen */
		layers.set(index,newName);
		int i=visibleLayers.indexOf(currentName);
		if (i>=0) visibleLayers.set(i,newName);
		if (Objects.equals(activeLayer,currentName)) model.surface.setActiveLayer(newName);

		/* Elemente anpassen */
		for (ModelElement element: model.surface.getElementsIncludingSubModels()) {
			final List<String> elementLayers=element.getLayers();
			final int j=elementLayers.indexOf(currentName);
			if (j>=0) elementLayers.set(j,newName);
		}

		updateList();
	}

	private void commandDelete(final int index) {
		/* Daten aus Modell */
		final List<String> layers=model.surface.getLayers();
		final List<String> visibleLayers=model.surface.getVisibleLayers();
		final String activeLayer=model.surface.getActiveLayer();

		/* Abfrage */
		final String layer=layers.get(index);
		if (!MsgBox.confirm(this,Language.tr("Window.Layers.Delete.ConfirmTitle"),String.format(Language.tr("Window.Layers.Delete.ConfirmInfo"),layer),Language.tr("Window.Layers.Delete.ConfirmInfo.Yes"),Language.tr("Window.Layers.Delete.ConfirmInfo.No"))) return;

		/* Layer l�schen */
		layers.remove(layer);
		visibleLayers.remove(layer);
		if (Objects.equals(activeLayer,layer)) model.surface.setActiveLayer(null);

		/* Elemente anpassen */
		for (ModelElement element: model.surface.getElementsIncludingSubModels()) {
			final List<String> elementLayers=element.getLayers();
			elementLayers.remove(layer);
		}

		updateList();
	}

	private void commandChangeVisible(final int index) {
		final String layer=model.surface.getLayers().get(index);

		final List<String> visibleLayers=model.surface.getVisibleLayers();
		if (visibleLayers.contains(layer)) visibleLayers.remove(layer); else visibleLayers.add(layer);

		updateList();
	}

	private void commandSetActive() {
		final String layer=model.surface.getLayers().get(list.getSelectedIndex());
		model.surface.setActiveLayer(layer);

		updateList();
	}

	private JMenuItem buttonToMenu(final JButton button) {
		final JMenuItem item=new JMenuItem(button.getText(),button.getIcon());
		item.setToolTipText(button.getToolTipText());
		for (ActionListener listener : button.getActionListeners()) item.addActionListener(listener);
		item.setEnabled(button.isEnabled());
		return item;
	}

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
