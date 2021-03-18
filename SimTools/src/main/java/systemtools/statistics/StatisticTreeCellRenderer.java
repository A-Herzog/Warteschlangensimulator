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
package systemtools.statistics;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import systemtools.images.SimToolsImages;

/**
 * Renderer für die Baumeinträge eines <code>StatisticTree</code> auf Basis der <code>StatisticNode</code>-Elemente pro Baumelement
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @see StatisticTree
 * @see StatisticNode
 * @author Alexander Herzog
 */
public class StatisticTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2366015952361517477L;

	/** Icon für Textseiten */
	private static final Image iconText;
	/** Icon für Tabellenseiten */
	private static final Image iconTable;
	/** Icon für Liniendiagrammseiten */
	private static final Image iconImageLine;
	/** Icon für Balkendiagrammseiten */
	private static final Image iconImageBar;
	/** Icon für Tortendiagrammseiten */
	private static final Image iconImagePie;
	/** Icon für Bilderseiten */
	private static final Image iconImagePicture;
	/** Icon für XY-Diagrammseiten */
	private static final Image iconImageXY;
	/** Icon für Schichtplanseiten */
	private static final Image iconImageShiftPlan;
	/** Icon für Reportgeneratorseiten */
	private static final Image iconReport;
	/** Icon für sonstige Infoseiten */
	private static final Image iconSpecial;

	static {
		iconText=SimToolsImages.STATISTICS_TEXT.getImage();
		iconTable=SimToolsImages.STATISTICS_TABLE.getImage();
		iconImageLine=SimToolsImages.STATISTICS_DIAGRAM_LINE.getImage();
		iconImageBar=SimToolsImages.STATISTICS_DIAGRAM_BAR.getImage();
		iconImagePie=SimToolsImages.STATISTICS_DIAGRAM_PIE.getImage();
		iconImagePicture=SimToolsImages.STATISTICS_DIAGRAM_PICTURE.getImage();
		iconImageXY=SimToolsImages.STATISTICS_DIAGRAM_XY.getImage();
		iconImageShiftPlan=SimToolsImages.STATISTICS_DIAGRAM_SHIFT_PLAN.getImage();
		iconReport=SimToolsImages.STATISTICS_REPORT.getImage();
		iconSpecial=SimToolsImages.STATISTICS_SPECIAL.getImage();
	}

	/**
	 * Konstruktor der Klasse <code>StatisticTreeCellRenderer</code>
	 */
	public StatisticTreeCellRenderer() {
	}

	/**
	 * Soll der Eintrag fett dargestellt werden
	 * @param value	Baumeintrag
	 * @return	Eintrag fett dargestellt?
	 */
	private boolean changeFont(Object value) {
		/* Kein leeres Objekt ? */
		if (!(value instanceof DefaultMutableTreeNode)) return false;
		DefaultMutableTreeNode node=((DefaultMutableTreeNode)value);

		/* Oberste Ebene, aber nicht root ? */
		if (node.getParent()==null || node.getParent().getParent()!=null) return false;

		/* Benutzerobjekt, welches nicht Report ist ? */
		if (node.getUserObject()==null) return true;
		if (node.getUserObject() instanceof StatisticNode) {
			StatisticNode statistic=(StatisticNode)node.getUserObject();
			return statistic.viewer.length==0 || statistic.viewer[0] instanceof StatisticViewerSpecialBase;
		}

		return true;
	}

	/**
	 * Stellt den aktuellen Baumeintrag passend zu den Daten ein.
	 * @param tree	Baumstruktur
	 * @param value	Baumeintrag
	 * @param sel	Selektiert?
	 * @param expanded	Ausgeklappt?
	 * @param leaf	Zweig?
	 * @param row	Zeilennummer
	 * @param hasFocus	Fokussiert?
	 */
	private void defaultProcessing(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		String stringValue=tree.convertValueToText(value,sel,expanded,leaf,row,hasFocus);
		if (stringValue==null) return;
		this.hasFocus=hasFocus;
		Font font=getFont();
		if (font!=null) setFont(font.deriveFont(changeFont(value)?Font.BOLD:Font.PLAIN));
		setText(stringValue);
		if (sel) setForeground(getTextSelectionColor()); else setForeground(getTextNonSelectionColor());
		setEnabled(tree.isEnabled());
		setComponentOrientation(tree.getComponentOrientation());
		selected=sel;
	}

	/**
	 * Liefert das zugehörige Icon zu einem <code>DefaultMutableTreeNode</code> bzw. dem als Nutzerobjekt eingebetteten <code>StatisticNode</code>
	 * @param value	Objekt vom Typ <code>DefaultMutableTreeNode</code> zu dem das passende Icon gesucht werden soll
	 * @return	Icon-URL zu dem passenden Icon
	 * @see StatisticNode
	 */
	public Image getIcon(Object value) {
		if (value==null) return null;
		if (!(value instanceof DefaultMutableTreeNode)) return null;
		if (!(((DefaultMutableTreeNode)value).getUserObject() instanceof StatisticNode)) return null;
		if (((StatisticNode)(((DefaultMutableTreeNode)value).getUserObject())).viewer.length==0) return null;

		return getImageViewerIcon(((StatisticNode)((DefaultMutableTreeNode)value).getUserObject()).viewer[0]);
	}

	/**
	 * Liefert die Icon-URL für einen Diagrammtyp
	 * @param viewerImageType	Diagrammtyp
	 * @return	Zugehörige Icon-URL
	 */
	public static Image getImageViewerIcon(final StatisticViewer.ViewerImageType viewerImageType) {
		switch (viewerImageType) {
		case IMAGE_TYPE_LINE: return iconImageLine;
		case IMAGE_TYPE_BAR: return iconImageBar;
		case IMAGE_TYPE_PIE: return iconImagePie;
		case IMAGE_TYPE_NOIMAGE: return iconImageLine;
		case IMAGE_TYPE_PICTURE: return iconImagePicture;
		case IMAGE_TYPE_XY: return iconImageXY;
		case IMAGE_TYPE_SHIFTPLAN: return iconImageShiftPlan;
		default: return iconImageLine;
		}
	}

	/**
	 * Liefert das zu einem {@link StatisticViewer} zugehörige Icon.
	 * @param viewer	{@link StatisticViewer} zu dem das Icon geliefert werden soll
	 * @return	Icon-URL zu dem passenden Icon oder im Fehlerfall <code>null</code>
	 */
	public static Image getImageViewerIcon(final StatisticViewer viewer) {
		if (viewer==null) return null;

		switch (viewer.getType()) {
		case TYPE_TEXT: return iconText;
		case TYPE_TABLE: return iconTable;
		case TYPE_IMAGE: return getImageViewerIcon(viewer.getImageType());
		case TYPE_REPORT: return iconReport;
		case TYPE_SPECIAL: return iconSpecial;
		default: return iconSpecial;
		}
	}

	/**
	 * Aktiviert ein Icon für einen Baumeintrag
	 * @param value	Baumeintrag
	 * @return	Gibt an, ob für den Eintrag ein Icon gefunden wurde
	 */
	private boolean iconProcessing(Object value) {
		final Image image=getIcon(value);
		if (image==null) return false;
		setIcon(new ImageIcon(image));
		return true;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		defaultProcessing(tree,value,sel,expanded,leaf,row,hasFocus);
		iconProcessing(value);
		return this;
	}
}