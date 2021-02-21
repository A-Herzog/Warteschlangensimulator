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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ElementRendererTools;
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
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2028948710421522249L;

	/** Modell-Haupt-Surface, welches alle Elemente enthält */
	private final ModelSurface surface;
	/** Alle im Modell vorhandenen IDs */
	private final int[] ids;
	/** Sortierung der Elemente */
	private final JComboBox<String> listSorting;
	/** Anzeige der Elemente */
	private final JList<ElementRendererTools.InfoRecord> list;
	/** Datenmodell für {@link #list} */
	private final DefaultListModel<ElementRendererTools.InfoRecord> model;

	/**
	 * Konstruktor der Klasse <code>SelectElementByIdDialog</code>
	 * @param owner	Übergeordnetes Element
	 * @param surface	Modell-Haupt-Surface, welches alle Elemente enthält
	 */
	@SuppressWarnings("unchecked")
	public SelectElementByIdDialog(final Component owner, final ModelSurface surface) {
		super(owner,Language.tr("FindElement.Title"));
		this.surface=surface;

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

		/* GUI */
		final JPanel content=createGUI(550,700,()->Help.topicModal(SelectElementByIdDialog.this,"SelectElementById"));
		content.setLayout(new BorderLayout());

		/* Sortierung */
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.NORTH);
		final JLabel label=new JLabel(Language.tr("FindElement.Sorting")+":");
		label.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		line.add(label);
		line.add(listSorting=new JComboBox<>(new String[]{
				Language.tr("FindElement.Sorting.IDs"),
				Language.tr("FindElement.Sorting.Names")
		}));
		label.setLabelFor(listSorting);
		listSorting.setRenderer(new IconListCellRenderer(new Images[] {
				Images.GENERAL_NUMBERS,
				Images.GENERAL_FONT
		}));
		listSorting.addActionListener(e->updateList());

		/* Liste */
		final Object[] data=ElementRendererTools.buildListAndModel(null);
		list=(JList<ElementRendererTools.InfoRecord>)data[0];
		model=(DefaultListModel<ElementRendererTools.InfoRecord>)data[1];

		content.add(new JScrollPane(list),BorderLayout.CENTER);
		list.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {close(BaseDialog.CLOSED_BY_OK); e.consume(); return;}}
		});

		/* Info-Text unten */
		JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(panel,BorderLayout.SOUTH);
		panel.add(new JLabel(getInfo(surface)));

		/* Start */
		updateList();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Liefert Informationen zu dem Modell.
	 * @param surface	Haupt-Zeichenfläche
	 * @return	Informationen zu dem Modell
	 */
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

	/**
	 * Vergleichsfunktion bei Sortierung nach Stationsnamen.
	 * @param id1	ID der ersten zu vergleichenden Station
	 * @param id2	ID der zweiten zu vergleichenden Station
	 * @return	Compare-Ergebnis
	 * @see #updateList()
	 */
	private int compareByName(final int id1, final int id2) {
		final ModelElement e1=surface.getByIdIncludingSubModels(id1);
		final ModelElement e2=surface.getByIdIncludingSubModels(id2);
		if (!(e1 instanceof ModelElementBox)) return 0;
		if (!(e2 instanceof ModelElementBox)) return 0;

		String s;

		final StringBuilder s1=new StringBuilder();
		s=((ModelElementBox)e1).getTypeName();
		if (s!=null && !s.isEmpty()) s1.append(s);
		s1.append(" - ");
		s=e1.getName();
		if (s!=null && !s.isEmpty()) s1.append(s);

		final StringBuilder s2=new StringBuilder();
		s=((ModelElementBox)e2).getTypeName();
		if (s!=null && !s.isEmpty()) s2.append(s);
		s2.append(" - ");
		s=e2.getName();
		if (s!=null && !s.isEmpty()) s2.append(s);

		return s1.toString().compareTo(s2.toString());
	}

	/**
	 * Aktualisiert die Listendarstellung
	 * (z.B. nach einer Änderung der Sortierreihenfolge).
	 */
	private void updateList() {
		int[] ids=null;
		final int index=listSorting.getSelectedIndex();

		/* Sortieren nach IDs */
		if (index==0) {
			ids=Arrays.copyOf(this.ids,this.ids.length);
			Arrays.sort(ids);
		}

		if (index==1) {
			/* Sortieren nach Namen */
			ids=Arrays.stream(this.ids).boxed().sorted((id1,id2)->compareByName(id1,id2)).mapToInt(Integer::intValue).toArray();
		}

		/* Fallback */
		if (ids==null) {
			ids=Arrays.copyOf(this.ids,this.ids.length);
		}

		/* ... und ausgeben */
		ElementRendererTools.buildList(surface,model,ids);
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
