package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt einen Dialog an, in dem eingestellt werden kann, für welche Stationen
 * eine Statistikaufzeichnung erfolgen soll.
 * @author Alexander Herzog
 * @see ModelElementPosition#isStationStatisticsActive()
 * @see ModelElementPosition#setStationStatisticsActive(boolean)
 */
public class StationStatisticsDialog extends BaseDialog {
	private static final long serialVersionUID=-3653892607306372543L;

	private final List<Record> listRecords;
	private final DefaultListModel<JCheckBox> listModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Zu bearbeitendes Modell
	 */
	public StationStatisticsDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("StationStatistics.Title"));

		/* Liste der Stationen erstellen */
		listRecords=getElements(null,model.surface);

		/* GUI erstellen */
		addUserButton(Language.tr("StationStatistics.SelectAll"),Images.EDIT_ADD.getURL());
		addUserButton(Language.tr("StationStatistics.SelectNone"),Images.EDIT_DELETE.getURL());
		final JPanel content=createGUI(()->Help.topicModal(this,"StationStatisticsDialog"));
		content.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(content,InfoPanel.globalStationStatistics);

		/* Listbox anlegen */
		final JList<JCheckBox> list=new JList<>();
		content.add(new JScrollPane(list),BorderLayout.CENTER);
		list.setCellRenderer(new JCheckBoxCellRenderer());
		list.setModel(listModel=new DefaultListModel<>());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index=list.locationToIndex(e.getPoint());
				if (index<0) return;
				final JCheckBox checkbox=list.getModel().getElementAt(index);
				checkbox.setSelected(!checkbox.isSelected());
				repaint();
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (Record record: listRecords) listModel.addElement(new JCheckBox(record.name,record.element.isStationStatisticsActive()));

		/* Dialog starten */
		setMinSizeRespectingScreensize(575,500);
		setSizeRespectingScreensize(575,500);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private List<Record> getElements(final ModelElementSub parent, final ModelSurface surface) {
		final List<Record> list=new ArrayList<>();
		for (ModelElement element: surface.getElements()) {
			if (!(element instanceof ModelElementBox)) continue;
			final ModelElementBox box=(ModelElementBox)element;
			if (!box.inputConnected()) continue;

			if (box instanceof ModelElementSub) {
				final ModelElementSub sub=(ModelElementSub)box;
				list.addAll(getElements(sub,sub.getSubSurface()));
			} else {
				list.add(new Record(parent,box));
			}
		}
		return list;
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0:
			for (int i=0;i<listModel.getSize();i++) listModel.getElementAt(i).setSelected(true);
			repaint();
			break;
		case 1:
			for (int i=0;i<listModel.getSize();i++) listModel.getElementAt(i).setSelected(false);
			repaint();
			break;
		}
	}

	@Override
	public void storeData() {
		for (int i=0;i<listModel.getSize();i++) {
			listRecords.get(i).element.setStationStatisticsActive(listModel.getElementAt(i).isSelected());
		}
	}

	private static class Record {
		public final String name;
		public final ModelElementBox element;

		public Record(final ModelElementSub parent, final ModelElementBox element) {
			this.element=element;
			if (parent==null) {
				name=buildName(element);
			} else {
				name=buildName(parent)+" - "+buildName(element);
			}
		}

		private static String buildName(final ModelElementBox element) {
			final StringBuilder name=new StringBuilder();
			name.append(element.getTypeName());
			name.append(String.format(" (id=%d)",element.getId()));
			if (!element.getName().isEmpty()) name.append(String.format(" \"%s\"",element.getName()));
			return name.toString();
		}
	}

	private class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {
		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
			value.setForeground(list.getForeground());
			value.setBackground(list.getBackground());
			return value;
		}
	}
}