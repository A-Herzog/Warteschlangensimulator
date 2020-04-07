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
	private static final long serialVersionUID = -1111382540141167151L;

	private URL iconConst=null;
	private URL iconVariable=null;
	private URL iconFunction=null;
	private URL iconDistribution=null;
	private URL iconSimData=null;
	private URL iconStationID=null;
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

	private boolean changeFont(Object value) {
		/* Kein leeres Objekt ? */
		if (value==null || !(value instanceof DefaultMutableTreeNode)) return false;
		DefaultMutableTreeNode node=((DefaultMutableTreeNode)value);

		/* Fett, wenn's kein Symbol-Eintrag ist. */
		return (node.getUserObject()==null || !(node.getUserObject() instanceof ExpressionSymbol));
	}

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