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
package ui.expressionbuilder;

import java.awt.Component;
import java.awt.Font;
import java.io.Serializable;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbol;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbolType;
import ui.images.Images;

/**
 * Diese Klasse zeichnet die Elemente in der Baumstruktur im
 * <code>ExpressionBuilder</code>-Dialog.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
class ExpressionBuilderTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -1111382540141167151L;

	/** Icon f�r Konstanten */
	private URL iconConst=null;
	/** Icon f�r Variablen */
	private URL iconVariable=null;
	/** Icon f�r Funktionen */
	private URL iconFunction=null;
	/** Icon f�r Wahrscheinlichkeitsverteilungen */
	private URL iconDistribution=null;
	/** Icon f�r Simulationsdaten */
	private URL iconSimData=null;
	/** Icon f�r Stations-IDs */
	private URL iconStationID=null;
	/** Icon f�r Kundendaten */
	private URL iconClientData=null;

	/**
	 * Konstruktor der Klasse
	 */
	public ExpressionBuilderTreeCellRenderer() {
		super();
		iconConst=Images.EXPRESSION_BUILDER_CONST.getURL();
		iconVariable=Images.EXPRESSION_BUILDER_VARIABLE.getURL();
		iconFunction=Images.EXPRESSION_BUILDER_FUNCTION.getURL();
		iconDistribution=Images.EXPRESSION_BUILDER_DISTRIBUTION.getURL();
		iconSimData=Images.EXPRESSION_BUILDER_SIMDATA.getURL();
		iconStationID=Images.EXPRESSION_BUILDER_STATION_ID.getURL();
		iconClientData=Images.EXPRESSION_BUILDER_CLIENT_DATA.getURL();
	}

	/**
	 * Soll der Eintrag fett dargestellt werden?
	 * @param value	Darzustellender Baumeintrag
	 * @return	Liefert <code>true</code>, wenn der Eintrag eine Gruppe bezeichnet und daher fett dargestellt werden soll
	 */
	private boolean changeFont(Object value) {
		/* Kein leeres Objekt ? */
		if (!(value instanceof DefaultMutableTreeNode)) return false;
		DefaultMutableTreeNode node=((DefaultMutableTreeNode)value);

		/* Fett, wenn's kein Symbol-Eintrag ist. */
		return (!(node.getUserObject() instanceof ExpressionSymbol));
	}

	/**
	 * Konfiguration der Darstellung f�r den Cell-Renderer.
	 * @param tree	Baumstruktur
	 * @param value	Aktueller Eintrag
	 * @param sel	Selektiert?
	 * @param expanded	Ausgeklappt?
	 * @param leaf	Endpunkt?
	 * @param row	Nummer der Zeile
	 * @param hasFocus	Selektiert?
	 * @see #getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
	 */
	private void defaultProcessing(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		String stringValue=tree.convertValueToText(value,sel,expanded,leaf,row,hasFocus);
		if (stringValue==null) return;
		this.hasFocus=hasFocus;
		Font font=getFont();
		if (font!=null) setFont(font.deriveFont(changeFont(value)?Font.BOLD:Font.PLAIN));
		setText(stringValue);
		if (sel) setForeground(getTextSelectionColor()); else setForeground(getTextNonSelectionColor());
		if (!tree.isEnabled()) setEnabled(false); else setEnabled(true);
		setComponentOrientation(tree.getComponentOrientation());
		selected=sel;
	}

	/**
	 * Liefert die URL des Icons zu einem Baumeintrag
	 * @param value	Baumeintrag zu dem die Icon-URL bestimmt werden soll
	 * @return	Icon-URL zu dem Baumeintrag oder <code>null</code>, wenn keine URL bestimmt werden konnte
	 */
	private URL getIconURL(Object value) {
		if (value==null) return null;

		if (!(value instanceof DefaultMutableTreeNode)) return null;
		if (!(((DefaultMutableTreeNode)value).getUserObject() instanceof ExpressionSymbol)) return null;

		final ExpressionSymbolType type=((ExpressionSymbol)((DefaultMutableTreeNode)value).getUserObject()).type;
		switch (type) {
		case TYPE_CONST: return iconConst;
		case TYPE_VARIABLE: return iconVariable;
		case TYPE_FUNCTION: return iconFunction;
		case TYPE_DISTRIBUTION: return iconDistribution;
		case TYPE_SIMDATA: return iconSimData;
		case TYPE_STATION_ID: return iconStationID;
		case TYPE_CLIENTDATA: return iconClientData;
		}

		return null;
	}

	/**
	 * Liefert die URL des Icons zu einem Baumeintrag
	 * @param value	Baumeintrag zu dem die Icon-URL bestimmt werden soll
	 * @return	Icon-URL zu dem Baumeintrag oder <code>null</code>, wenn keine URL bestimmt werden konnte
	 */
	private boolean iconProcessing(Object value) {
		URL url=getIconURL(value);
		if (url==null) return false;
		setIcon(new ImageIcon(url));
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