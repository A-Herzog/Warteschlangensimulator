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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ScaledImageCache;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Ermöglicht die Auswahl eines Elements durch die Eingabe der ID oder des Namens.
 * @author Alexander Herzog
 */
public class FindElementDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1687364491601255550L;

	/** Modell-Haupt-Surface, welches alle Elemente enthält */
	private final ModelSurface surface;
	/** Eingabefeld für die Suche */
	private final JTextField searchEdit;
	/** Suchmodus (IDs, Namen, alles) */
	private final JComboBox<String> optionsCombo;
	/** Auch Elemente auf ausgeblendeten Ebenen berücksichtigen? */
	private final JCheckBox includeHidden;
	/** Liste mit den Suchergebnissen */
	private final JList<JLabel> resultsList;
	/** Datenmodell für die Liste mit den Suchergebnissen */
	private final DefaultListModel<JLabel> resultsModel;
	/** IDs der Suchergebnisse */
	private final List<Integer> resultsIds;
	/** Ausgabefeld für Informationen zu den Suchergebnissen (Anzahl der Treffer usw.) */
	private final JLabel resultsInfo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param surface	Modell-Haupt-Surface, welches alle Elemente enthält
	 */
	public FindElementDialog(final Component owner, final ModelSurface surface) {
		super(owner,Language.tr("FindElementDirect.Title"));
		this.surface=surface;

		showCloseButton=true;
		final JPanel all=createGUI(()->Help.topicModal(getOwner(),"FindElement"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalFindElement);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		final JPanel setupArea=new JPanel();
		setupArea.setLayout(new BoxLayout(setupArea,BoxLayout.PAGE_AXIS));
		content.add(setupArea,BorderLayout.NORTH);

		/* Einstellungenbereich */

		JPanel line;
		JLabel label;

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("FindElementDirect.IdOrName")+":",""+getSmallestId());
		setupArea.add((JPanel)data[0]);
		searchEdit=(JTextField)data[1];
		searchEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {search();}
			@Override public void keyReleased(KeyEvent e) {search();}
			@Override public void keyPressed(KeyEvent e) {search();}
		});

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("FindElementDirect.Option")+":"));
		line.add(optionsCombo=new JComboBox<>(new String[]{
				Language.tr("FindElementDirect.Option.ID"),
				Language.tr("FindElementDirect.Option.Names"),
				Language.tr("FindElementDirect.Option.All")
		}));
		label.setLabelFor(optionsCombo);
		optionsCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_FIND_BY_ID,
				Images.GENERAL_FIND_BY_NAME,
				Images.GENERAL_FIND_BY_ALL
		}));
		optionsCombo.setSelectedIndex(2);
		optionsCombo.addActionListener(e->search());

		includeHidden=new JCheckBox(Language.tr("FindElementDirect.IncludeHidden"));
		if (!surface.getLayers().isEmpty()) line.add(includeHidden);
		includeHidden.setToolTipText(Language.tr("FindElementDirect.IncludeHidden.Info"));
		includeHidden.addActionListener(e->search());

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(resultsInfo=new JLabel());

		/* Anzeigebereich */

		resultsIds=new ArrayList<>();
		content.add(new JScrollPane(resultsList=new JList<>(resultsModel=new DefaultListModel<>())),BorderLayout.CENTER);
		resultsList.setCellRenderer(new ElementListCellRenderer());
		resultsList.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {close(BaseDialog.CLOSED_BY_OK); e.consume(); return;}}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});

		/* Fußzeile */

		final JPanel footer=new JPanel();
		footer.setLayout(new BoxLayout(footer,BoxLayout.PAGE_AXIS));
		content.add(footer,BorderLayout.SOUTH);
		footer.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("FindElementDirect.ClickInfo")));

		/* Starten */

		search();
		setMinSizeRespectingScreensize(550,500);
		setSizeRespectingScreensize(550,500);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	private int getSmallestId() {
		int minId=Integer.MAX_VALUE;

		for (ModelElement element1: surface.getElements()) {
			if (element1.getId()<minId) minId=element1.getId();
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2.getId()<minId) minId=element2.getId();
			}
		}

		return (minId==Integer.MAX_VALUE)?0:minId;
	}

	private void setInfo(final String htmlColor, final String text) {
		resultsInfo.setText("<html><body><span style=\"color: "+htmlColor+"\">"+text+"</span></body></html>");
	}

	private Icon drawElementToIcon(final ModelElement element) {
		final Point p1=element.getPosition(true);
		final Point p2=element.getLowerRightPosition();
		if (p1==null || p2==null) return null; /* Kanten haben keine Größe */

		final BufferedImage image1=new BufferedImage(p2.x+1,p2.y+1,BufferedImage.TYPE_4BYTE_ABGR);
		element.drawToGraphics(image1.getGraphics(),new Rectangle(0,0,p2.x,p2.y),1,false);

		final BufferedImage image2=new BufferedImage(p2.x+1-p1.x,p2.y+1-p1.y,BufferedImage.TYPE_4BYTE_ABGR);
		image2.getGraphics().drawImage(image1,-p1.x,-p1.y,null);

		if (image2.getWidth()>50 || image2.getHeight()>50) {
			int w=image2.getWidth();
			int h=image2.getHeight();
			if (w>50) {h=h*50/w; w=50;}
			if (h>50) {w=w*50/h; h=50;}
			return new ImageIcon(ScaledImageCache.getScaledImageCache().getScaledImage(image2,w,h));
		} else {
			return new ImageIcon(image2);
		}
	}

	private JLabel getLabel(final ModelSurface surface, final int id) {
		/* Element und übergeordnetes Element finden */
		ModelElement element=null;
		ModelElementSub parent=null;
		for (ModelElement el: surface.getElements()) {
			if (el.getId()==id) {element=el; break;}
			if (el instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)el).getSubSurface().getElements()) {
				if (sub.getId()==id) {parent=(ModelElementSub)el; element=sub; break;}
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
		if (!element.getSurface().isVisibleOnLayer(element)) sb.append(", "+Language.tr("FindElement.Invisible"));
		sb.append("</span></i>");

		/* Bild aufbauen */
		Icon icon=element.buildIcon();
		if (icon==null) icon=drawElementToIcon(element);

		/* Label erstellen */
		final JLabel label=new JLabel("<html><body>"+sb.toString()+"</body></html>");
		if (icon!=null) label.setIcon(icon);
		return label;
	}

	private boolean testName(final ModelElement element, final String search) {
		final String searchLower=search.toLowerCase();

		if (element.getName().toLowerCase().contains(searchLower)) return true;
		if (element.getContextMenuElementName().toLowerCase().contains(searchLower)) return true;

		return false;
	}

	private void buildIDsList() {
		resultsIds.clear();

		final String search=searchEdit.getText().trim();
		if (search.isEmpty()) {
			setInfo("red",Language.tr("FindElementDirect.NoSearchStringEntered"));
			return;
		}

		/* Suche nach IDs */

		if (optionsCombo.getSelectedIndex()==0 || optionsCombo.getSelectedIndex()==2) {
			final Integer I=NumberTools.getNotNegativeInteger(search);
			if (I==null) {
				if (optionsCombo.getSelectedIndex()==0) {
					setInfo("red",String.format(Language.tr("FindElementDirect.InvalidNumber.Info"),search));
					return;
				}
			} else {
				ModelElement element=surface.getByIdIncludingSubModels(I.intValue());
				if (!includeHidden.isSelected() && !surface.isVisibleOnLayer(element)) element=null;
				if (element==null) {
					if (optionsCombo.getSelectedIndex()==0) {
						setInfo("red",String.format(Language.tr("FindElementDirect.UnknownId.Info"),I.intValue()));
						return;
					}
				} else {
					resultsIds.add(I);
				}
			}
		}

		/* Suche nach Namen */

		if (optionsCombo.getSelectedIndex()==1 || optionsCombo.getSelectedIndex()==2) {
			for (ModelElement element1: surface.getElements()) {
				if (!includeHidden.isSelected() && !surface.isVisibleOnLayer(element1)) continue;
				if (!resultsIds.contains(element1.getId()) && testName(element1,search)) resultsIds.add(element1.getId());
				if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
					if (!includeHidden.isSelected() && !((ModelElementSub)element1).getSubSurface().isVisibleOnLayer(element2)) continue;
					if (!resultsIds.contains(element2.getId()) && testName(element2,search)) resultsIds.add(element2.getId());
				}
			}
		}

		/* Info ausgeben */

		if (resultsIds.isEmpty()) {
			setInfo("red",Language.tr("FindElementDirect.NoElementsFound"));
		} else {
			if (resultsIds.size()==1) {
				setInfo("green",Language.tr("FindElementDirect.OneElementFound"));
			} else {
				setInfo("green",String.format(Language.tr("FindElementDirect.ElementsFound"),resultsIds.size()));
			}
		}
	}

	private void search() {
		buildIDsList();

		resultsModel.clear();
		for (Integer id: resultsIds) resultsModel.addElement(getLabel(surface,id));
		resultsList.setModel(resultsModel);
	}

	/**
	 * Liefert die ID des ausgewählten Elements bezogen auf die Hauptebene, d.h.
	 * bei Kind-Elementen in einem Untermodell die ID des Untermodell-Elements.
	 * Wenn nichts ausgewählt ist, wird -1 zurückgeliefert
	 * @return	ID des ausgewählten Elements oder -1, wenn nichts gewählt ist
	 */
	public int getSelectedId() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return -1;
		if (resultsList.getSelectedIndex()<0) return -1;
		final int selectedID=resultsIds.get(resultsList.getSelectedIndex());

		for (ModelElement element: surface.getElements()) {
			if (element.getId()==selectedID) return selectedID;
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) {
				if (sub.getId()==selectedID) return element.getId();
			}
		}

		return -1;
	}

	/**
	 * Renderer für die Stationen in der Liste mit den Suchergebnissen
	 * @see FindElementDialog#resultsList
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
			}
			return renderer;
		}
	}
}
