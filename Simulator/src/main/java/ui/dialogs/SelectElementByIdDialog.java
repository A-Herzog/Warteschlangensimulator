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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt eine Liste aller im Surface vorhandenen Elemente an und ermöglicht dem
 * Nutzer die Auswahl eines Elements
 * @author Alexander Herzog
 */
public class SelectElementByIdDialog extends BaseDialog {
	private static final long serialVersionUID = -2028948710421522249L;

	private final ModelSurface surface;
	private int[] ids;
	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse <code>SelectElementByIdDialog</code>
	 * @param owner	Übergeordnetes Element
	 * @param surface	Modell-Haupt-Surface, welches alle Elemente enthält
	 */
	public SelectElementByIdDialog(final Component owner, final ModelSurface surface) {
		super(owner,Language.tr("FindElement.Title"));
		this.surface=surface;

		final JPanel content=createGUI(550,700,()->Help.topicModal(SelectElementByIdDialog.this,"SelectElementById"));
		content.setLayout(new BorderLayout());

		content.add(new JScrollPane(list=getList(surface)),BorderLayout.CENTER);
		list.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {close(BaseDialog.CLOSED_BY_OK); e.consume(); return;}}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});

		JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(panel,BorderLayout.SOUTH);
		panel.add(new JLabel(getInfo(surface)));

		setLocationRelativeTo(this.owner);
	}

	private String getInfo(final ModelSurface surface) {
		/* Zählung */
		int countStations=0;
		int countEdges=0;
		int countOthers=0;
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementBox) {
				countStations++;
				if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
					if (sub instanceof ModelElementBox) {countStations++; continue;}
					if (sub instanceof ModelElementEdge) {countEdges++; continue;}
					countOthers++;
				}
				continue;
			}
			if (element instanceof ModelElementEdge) {countEdges++; continue;}
			countOthers++;
		}

		/* Ergebnis zusammenstellen */
		return String.format("<html><body>"+Language.tr("FindElement.Info")+"</body></html>",countStations,countEdges,countOthers,countStations+countEdges+countOthers);
	}

	private JLabel getLabel(final ModelSurface surface, final int id) {
		/* Element und übergeordnetes Element finden */
		ModelElementBox element=null;
		ModelElementSub parent=null;
		for (ModelElement el: surface.getElements()) if (el instanceof ModelElementBox) {
			if (el.getId()==id) {element=(ModelElementBox)el; break;}
			if (el instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)el).getSubSurface().getElements()) if (sub instanceof ModelElementBox) {
				if (sub.getId()==id) {parent=(ModelElementSub)el; element=(ModelElementBox)sub; break;}
				if (parent!=null) break;
			}
		}

		if (element==null) return new JLabel("");

		/* Text aufbauen */
		final StringBuilder sb=new StringBuilder();
		sb.append("<b><span style=\"font-size: larger;\">");
		sb.append(element.getContextMenuElementName());
		sb.append("</span> (");
		final String name=element.getName();
		if (name.isEmpty()) sb.append(Language.tr("FindElement.NoName")); else sb.append(name);
		sb.append(")</b><br><span style=\"color: orange;\">");
		sb.append("id="+element.getId());
		sb.append("</span><br><span style=\"color: blue;\"><i>");
		if (parent==null) sb.append(Language.tr("FindElement.Level.Top")); else sb.append(String.format(Language.tr("FindElement.Level.Sub"),parent.getId()));
		sb.append("</span></i>");

		/* Bild aufbauen */
		final Icon icon=element.buildIcon();

		/* Label erstellen */
		final JLabel label=new JLabel("<html><body>"+sb.toString()+"</body></html>");
		if (icon!=null) label.setIcon(icon);
		return label;
	}

	private JList<JLabel> getList(final ModelSurface surface) {
		/* Alle IDs auslesen */
		List<Integer> idsList=new ArrayList<>();
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			idsList.add(element.getId());
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) if (sub instanceof ModelElementBox) {
				idsList.add(sub.getId());
			}
		}
		ids=new int[idsList.size()];
		for (int i=0;i<ids.length;i++) ids[i]=idsList.get(i);
		Arrays.sort(ids);

		/* Liste zusammensetzen */
		List<JLabel> data=new ArrayList<>();
		for (int i=0;i<ids.length;i++) data.add(getLabel(surface,ids[i]));

		/* ... und ausgeben */
		JList<JLabel> list=new JList<>(data.toArray(new JLabel[0]));
		list.setCellRenderer(new ElementListCellRenderer());
		return list;
	}

	private class ElementListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 4327039078742103357L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((ElementListCellRenderer)renderer).setText(((JLabel)value).getText());
				((ElementListCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}

	/**
	 * Liefert die ID des ausgewählten Elements bezogen auf die Hauptebene, d.h.
	 * bei Kind-Elementen in einem Untermodell die ID des Untermodell-Elements.
	 * Wenn nichts ausgewählt ist, wird -1 zurückgeliefert
	 * @return	ID des ausgewählten Elements oder -1, wenn nichts gewählt ist
	 */
	public int getSelectedId() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return -1;
		if (list.getSelectedIndex()<0) return -1;
		final int selectedID=ids[list.getSelectedIndex()];

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			if (element.getId()==selectedID) return selectedID;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) if (sub instanceof ModelElementBox) {
				if (sub.getId()==selectedID) return element.getId();
			}
		}

		return -1;
	}
}
