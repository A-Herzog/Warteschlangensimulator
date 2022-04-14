/**
 * Copyright  Rob Camick, jirkapenzes (https://gist.github.com/jirkapenzes)
 * <a href="https://gist.github.com/jirkapenzes/4560255">https://gist.github.com/jirkapenzes/4560255</a>
 * License (see <a href="https://tips4java.wordpress.com/2008/11/06/wrap-layout/">https://tips4java.wordpress.com/2008/11/06/wrap-layout/</a>):<br>
 * <tt>
 * Rob Camick said
 * January 12, 2012 at 10:46 am
 * You can use/modify the code however you wish.
 * </tt>
 */
package ui.tools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * WrapLayout based on FlowLayout
 */
public class WrapLayout extends FlowLayout {
	/**
	 * Serialization id
	 */
	private static final long serialVersionUID=-5855729559300650379L;

	/**
	 * Constructor
	 */
	public WrapLayout() {
		super();
	}

	/**
	 * Constructor with alignment info
	 * @param align	alignment info
	 */
	public WrapLayout(int align) {
		super(align);
	}

	/**
	 * Constructor with alignment info and spacing
	 * @param align	Alignment info
	 * @param hgap	Horizontal gap
	 * @param vgap	Vertical gap
	 */
	public WrapLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

	@Override
	public Dimension preferredLayoutSize(Container target) {
		return layoutSize(target, true);
	}

	@Override
	public Dimension minimumLayoutSize(Container target) {
		Dimension minimum = layoutSize(target, false);
		minimum.width -= (getHgap() + 1);
		return minimum;
	}

	/**
	 * Update layout
	 * @param target	the container that needs to be laid out
	 * @param preferred	Use perferred size
	 * @return	the minimum dimensions to lay out thesubcomponents of the specified container
	 */
	private Dimension layoutSize(Container target, boolean preferred) {
		synchronized (target.getTreeLock()) {
			int targetWidth = target.getSize().width;

			if (targetWidth == 0)
				targetWidth = Integer.MAX_VALUE;

			int hgap = getHgap();
			int vgap = getVgap();
			Insets insets = target.getInsets();
			int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
			int maxWidth = targetWidth - horizontalInsetsAndGap;

			Dimension dim = new Dimension(0, 0);
			int rowWidth = 0;
			int rowHeight = 0;

			int nmembers = target.getComponentCount();

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);

				if (m.isVisible()) {
					Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

					if (rowWidth + d.width > maxWidth) {
						addRow(dim, rowWidth, rowHeight);
						rowWidth = 0;
						rowHeight = 0;
					}

					if (rowWidth != 0) {
						rowWidth += hgap;
					}

					rowWidth += d.width;
					rowHeight = Math.max(rowHeight, d.height);
				}
			}

			addRow(dim, rowWidth, rowHeight);

			dim.width += horizontalInsetsAndGap;
			dim.height += insets.top + insets.bottom + vgap * 2;

			Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
			if (scrollPane != null) {
				dim.width -= (hgap + 1);
			}

			return dim;
		}
	}

	/**
	 * Add a new row to layout
	 * @param dim	Current container size
	 * @param rowWidth	Row width
	 * @param rowHeight	Row height
	 */
	private void addRow(Dimension dim, int rowWidth, int rowHeight) {
		dim.width = Math.max(dim.width, rowWidth);

		if (dim.height > 0) {
			dim.height += getVgap();
		}

		dim.height += rowHeight;
	}
}