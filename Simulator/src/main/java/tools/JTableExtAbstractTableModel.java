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
package tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

/**
 * Stellt gegenüber dem {@link AbstractTableModel} einige Funktionen bereit,
 * um {@link JPanel}-basierende Zellen zu erzeugen, die durch {@link JTableExt}
 * angezeigt werden können.
 * @see JTableExt
 * @author Alexander Herzog
 * @version 1.3
 */
public abstract class JTableExtAbstractTableModel extends AbstractTableModel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4249212671820906541L;

	/**
	 * Erstellt ein Panel für eine Tabellenzelle aus mehreren Schaltflächen
	 * @param title	Namen der Schaltflächen (darf <code>null</code> sein)
	 * @param iconURL	Icons für die Schaltflächen (darf <code>null</code> sein)
	 * @param listener	Listener die beim Anklicken der Schaltflächen ausgelöst werden
	 * @return	Panel welches die angegebenen Schaltflächen enthält
	 */
	protected final JPanel makeButtonPanel(final String[] title, final URL[] iconURL, final ActionListener[] listener) {
		String[] tooltip=new String[title.length];
		return makeButtonPanel(title,tooltip,iconURL,listener);
	}

	/**
	 * Erstellt ein Panel für eine Tabellenzelle aus mehreren Schaltflächen
	 * @param title	Namen der Schaltflächen (darf <code>null</code> sein)
	 * @param icons	Icons für die Schaltflächen (darf <code>null</code> sein)
	 * @param listener	Listener die beim Anklicken der Schaltflächen ausgelöst werden
	 * @return	Panel welches die angegebenen Schaltflächen enthält
	 */
	protected final JPanel makeButtonPanel(final String[] title, final Icon[] icons, final ActionListener[] listener) {
		String[] tooltip=new String[title.length];
		return makeButtonPanel(title,tooltip,icons,listener);
	}

	/**
	 * Erstellt ein Panel für eine Tabellenzelle aus mehreren Schaltflächen
	 * @param title	Namen der Schaltflächen (darf <code>null</code> sein)
	 * @param tooltip	Tooltips für die Schaltflächen (darf <code>null</code> sein)
	 * @param iconURL	Icons für die Schaltflächen (darf <code>null</code> sein)
	 * @param listener	Listener die beim Anklicken der Schaltflächen ausgelöst werden
	 * @return	Panel welches die angegebenen Schaltflächen enthält
	 */
	protected final JPanel makeButtonPanel(String[] title, String[] tooltip, URL[] iconURL, ActionListener[] listener) {
		if (title==null) title=new String[0];
		if (tooltip==null) tooltip=new String[0];
		if (iconURL==null) iconURL=new URL[0];
		if (listener==null) listener=new ActionListener[0];
		int max=title.length;
		max=Math.max(max,tooltip.length);
		max=Math.max(max,iconURL.length);
		max=Math.max(max,listener.length);
		for (int i=1;i<=max;i++) {
			if (title.length<i) {List<String> l=new ArrayList<>(Arrays.asList(title)); l.add(null); title=l.toArray(new String[0]);}
			if (tooltip.length<i) {List<String> l=new ArrayList<>(Arrays.asList(tooltip)); l.add(null); tooltip=l.toArray(new String[0]);}
			if (iconURL.length<i) {List<URL> l=new ArrayList<>(Arrays.asList(iconURL)); l.add(null); iconURL=l.toArray(new URL[0]);}
			if (listener.length<i) {List<ActionListener> l=new ArrayList<>(Arrays.asList(listener)); l.add(null); listener=l.toArray(new ActionListener[0]);}
		}

		JPanel p=new JPanel(new BorderLayout());
		JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		p.add(toolbar,BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder());
		for (int i=0;i<Math.min(title.length,Math.min(tooltip.length,Math.min(iconURL.length,listener.length)));i++) {
			JButton button;
			if (iconURL[i]==null) {
				if (title[i]==null || title[i].isEmpty()) title[i]="-";
				button=new JButton(title[i]);
			} else {
				if (title[i]==null || title[i].isEmpty()) button=new JButton(new ImageIcon(iconURL[i])); else button=new JButton(title[i],new ImageIcon(iconURL[i]));
			}
			button.addActionListener(listener[i]);
			if (tooltip[i]!=null && !tooltip[i].isEmpty()) {
				button.setToolTipText(tooltip[i]);
			}
			toolbar.add(button);
		}
		return p;
	}

	/**
	 * Erstellt ein Panel für eine Tabellenzelle aus mehreren Schaltflächen
	 * @param title	Namen der Schaltflächen (darf <code>null</code> sein)
	 * @param tooltip	Tooltips für die Schaltflächen (darf <code>null</code> sein)
	 * @param icons	Icons für die Schaltflächen (darf <code>null</code> sein)
	 * @param listener	Listener die beim Anklicken der Schaltflächen ausgelöst werden
	 * @return	Panel welches die angegebenen Schaltflächen enthält
	 */
	protected final JPanel makeButtonPanel(String[] title, String[] tooltip, Icon[] icons, ActionListener[] listener) {
		if (title==null) title=new String[0];
		if (tooltip==null) tooltip=new String[0];
		if (icons==null) icons=new Icon[0];
		if (listener==null) listener=new ActionListener[0];
		int max=title.length;
		max=Math.max(max,tooltip.length);
		max=Math.max(max,icons.length);
		max=Math.max(max,listener.length);
		for (int i=1;i<=max;i++) {
			if (title.length<i) {List<String> l=new ArrayList<>(Arrays.asList(title)); l.add(null); title=l.toArray(new String[0]);}
			if (tooltip.length<i) {List<String> l=new ArrayList<>(Arrays.asList(tooltip)); l.add(null); tooltip=l.toArray(new String[0]);}
			if (icons.length<i) {List<Icon> l=new ArrayList<>(Arrays.asList(icons)); l.add(null); icons=l.toArray(new Icon[0]);}
			if (listener.length<i) {List<ActionListener> l=new ArrayList<>(Arrays.asList(listener)); l.add(null); listener=l.toArray(new ActionListener[0]);}
		}

		JPanel p=new JPanel(new BorderLayout());
		JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		p.add(toolbar,BorderLayout.CENTER);
		p.setBorder(BorderFactory.createEmptyBorder());
		for (int i=0;i<Math.min(title.length,Math.min(tooltip.length,Math.min(icons.length,listener.length)));i++) {
			JButton button;
			if (icons[i]==null) {
				if (title[i]==null || title[i].isEmpty()) title[i]="-";
				button=new JButton(title[i]);
			} else {
				if (title[i]==null || title[i].isEmpty()) button=new JButton(icons[i]); else button=new JButton(title[i],icons[i]);
			}
			button.addActionListener(listener[i]);
			if (tooltip[i]!=null && !tooltip[i].isEmpty()) {
				button.setToolTipText(tooltip[i]);
			}
			toolbar.add(button);
		}
		return p;
	}

	/**
	 * Erstellt ein Textpanel
	 * @param text	Anzuzeigender Text
	 * @param iconURL	Vor dem Text anzuzeigendes Icon (kann <code>null</code> sein)
	 * @return	Panel welches den Text enthält
	 */
	protected final JPanel makePanel(final String text, final URL iconURL) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(text);
		if (iconURL!=null) label.setIcon(new ImageIcon(iconURL));
		p.add(label,BorderLayout.CENTER);
		return p;
	}

	/**
	 * Erstellt ein Textpanel
	 * @param text	Anzuzeigender Text
	 * @param icon	Vor dem Text anzuzeigendes Icon (kann <code>null</code> sein)
	 * @return	Panel welches den Text enthält
	 */
	protected final JPanel makePanelIcon(final String text, final Icon icon) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(text);
		if (icon!=null) label.setIcon(icon);
		p.add(label,BorderLayout.CENTER);
		return p;
	}

	/**
	 * Erstellt ein Icon-Panel
	 * @param image	Anzuzeigendes Bild
	 * @return	Panel welches das Bild enthält
	 */
	protected final JPanel makePanel(final BufferedImage image) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel();
		label.setIcon(new ImageIcon(image));
		p.add(label,BorderLayout.CENTER);
		return p;
	}

	/**
	 * Erstellt ein Panel aus einem Text und einer Schaltfläche
	 * @param title	Beschriftung
	 * @param iconURL	Icon der Schaltfläche
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanel(final String title, final URL iconURL, final ActionListener listener) {
		return makeEditPanel(null,title,iconURL,listener);
	}

	/**
	 * Erstellt ein Panel aus einem Text und einer Schaltfläche
	 * @param title	Beschriftung
	 * @param icon	Icon der Schaltfläche
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanel(final String title, final Icon icon, final ActionListener listener) {
		return makeEditPanel(null,title,icon,listener);
	}

	/**
	 * Erstellt ein Panel aus einem Text (mit optionalem Icon) und einer Schaltfläche
	 * @param labelIconURL	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param iconURL	Icon der Schaltfläche
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanel(final URL labelIconURL, final String title, final URL iconURL, final ActionListener listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIconURL!=null) label.setIcon(new ImageIcon(labelIconURL));
		p.add(label,BorderLayout.CENTER);
		JButton button; p.add(button=new JButton(new ImageIcon(iconURL)),BorderLayout.EAST);
		button.addActionListener(listener);
		return p;
	}

	/**
	 * Erstellt ein Panel aus einem Text (mit optionalem Icon) und einer Schaltfläche
	 * @param labelIconURL	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param icon	Icon der Schaltfläche
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanel(final URL labelIconURL, final String title, final Icon icon, final ActionListener listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIconURL!=null) label.setIcon(new ImageIcon(labelIconURL));
		p.add(label,BorderLayout.CENTER);
		JButton button; p.add(button=new JButton(icon),BorderLayout.EAST);
		button.addActionListener(listener);
		return p;
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text und einer Schaltfläche
	 * @param title	Beschriftung
	 * @param iconURL	Icon der Schaltfläche
	 * @param hint	Tooltiptext für die Schaltfläche (kann <code>null</code> sein)
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanelSmallBorder(final String title, final URL iconURL, final String hint, final ActionListener listener) {
		return makeEditPanelSmallBorder(null,title,iconURL,hint,listener);
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text und einer Schaltfläche
	 * @param title	Beschriftung
	 * @param icon	Icon der Schaltfläche
	 * @param hint	Tooltiptext für die Schaltfläche (kann <code>null</code> sein)
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanelSmallBorder(final String title, final Icon icon, final String hint, final ActionListener listener) {
		return makeEditPanelSmallBorder((URL)null,title,icon,hint,listener);
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text (mit optionalem Icon) und einer Schaltfläche
	 * @param labelIconURL	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param iconURL	Icon der Schaltfläche
	 * @param hint	Tooltiptext für die Schaltfläche (kann <code>null</code> sein)
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanelSmallBorder(final URL labelIconURL, final String title, final URL iconURL, final String hint, final ActionListener listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIconURL!=null) label.setIcon(new ImageIcon(labelIconURL));
		p.add(label,BorderLayout.CENTER);
		JButton button; p.add(button=new JButton(new ImageIcon(iconURL)),BorderLayout.EAST);
		if (hint!=null) button.setToolTipText(hint);
		button.setPreferredSize(new Dimension(24,24));
		button.addActionListener(listener);
		return p;
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text (mit optionalem Icon) und einer Schaltfläche
	 * @param labelIconURL	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param icon	Icon der Schaltfläche
	 * @param hint	Tooltiptext für die Schaltfläche (kann <code>null</code> sein)
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanelSmallBorder(final URL labelIconURL, final String title, final Icon icon, final String hint, final ActionListener listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIconURL!=null) label.setIcon(new ImageIcon(labelIconURL));
		p.add(label,BorderLayout.CENTER);
		JButton button; p.add(button=new JButton(icon),BorderLayout.EAST);
		if (hint!=null) button.setToolTipText(hint);
		button.setPreferredSize(new Dimension(24,24));
		button.addActionListener(listener);
		return p;
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text (mit optionalem Icon) und einer Schaltfläche
	 * @param labelIcon	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param icon	Icon der Schaltfläche
	 * @param hint	Tooltiptext für die Schaltfläche (kann <code>null</code> sein)
	 * @param listener	Callback das beim Anklicken der Schaltfläche aufgerufen werden soll
	 * @return	Panel aus Text und Schaltfläche
	 */
	protected final JPanel makeEditPanelSmallBorder(final Icon labelIcon, final String title, final Icon icon, final String hint, final ActionListener listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIcon!=null) label.setIcon(labelIcon);
		p.add(label,BorderLayout.CENTER);
		JButton button; p.add(button=new JButton(icon),BorderLayout.EAST);
		if (hint!=null) button.setToolTipText(hint);
		button.setPreferredSize(new Dimension(24,24));
		button.addActionListener(listener);
		return p;
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text und mehreren Schaltfläche
	 * @param title	Beschriftung
	 * @param iconURL	Icons der Schaltflächen
	 * @param hint	Tooltiptexts für die Schaltflächen (kann ganz oder teilweise <code>null</code> sein)
	 * @param listener	Callbacks die beim Anklicken der Schaltflächen aufgerufen werden sollen
	 * @return	Panel aus Text und Schaltflächen
	 */
	protected final JPanel makeEditPanelSmallBorder(final String title, final URL[] iconURL, final String[] hint, final ActionListener[] listener) {
		return makeEditPanelSmallBorder((URL)null,title,iconURL,hint,listener);
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text und mehreren Schaltfläche
	 * @param title	Beschriftung
	 * @param icons	Icons der Schaltflächen
	 * @param hint	Tooltiptexts für die Schaltflächen (kann ganz oder teilweise <code>null</code> sein)
	 * @param listener	Callbacks die beim Anklicken der Schaltflächen aufgerufen werden sollen
	 * @return	Panel aus Text und Schaltflächen
	 */
	protected final JPanel makeEditPanelSmallBorder(final String title, final Icon[] icons, final String[] hint, final ActionListener[] listener) {
		return makeEditPanelSmallBorderIcon(null,title,icons,hint,listener);
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text (mit optionalem Icon) und mehreren Schaltfläche
	 * @param labelIconURL	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param iconURL	Icons der Schaltflächen
	 * @param hint	Tooltiptexts für die Schaltflächen (kann ganz oder teilweise <code>null</code> sein)
	 * @param listener	Callbacks die beim Anklicken der Schaltflächen aufgerufen werden sollen
	 * @return	Panel aus Text und Schaltflächen
	 */
	protected final JPanel makeEditPanelSmallBorder(final URL labelIconURL, final String title, final URL[] iconURL, final String[] hint, final ActionListener[] listener) {
		return makeEditPanelSmallBorderIcon((labelIconURL!=null)?new ImageIcon(labelIconURL):null,title,iconURL,hint,listener);
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text (mit optionalem Icon) und mehreren Schaltfläche
	 * @param labelIcon	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param iconURL	Icons der Schaltflächen
	 * @param hint	Tooltiptexts für die Schaltflächen (kann ganz oder teilweise <code>null</code> sein)
	 * @param listener	Callbacks die beim Anklicken der Schaltflächen aufgerufen werden sollen
	 * @return	Panel aus Text und Schaltflächen
	 */
	protected final JPanel makeEditPanelSmallBorderIcon(final Icon labelIcon, final String title, final URL[] iconURL, final String[] hint, final ActionListener[] listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIcon!=null) label.setIcon(labelIcon);
		p.add(label,BorderLayout.CENTER);
		JButton button;
		JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		buttonPanel.setBackground(new Color(255,255,255,0));

		p.add(buttonPanel,BorderLayout.EAST);
		for (int i=0;i<Math.min(iconURL.length,listener.length);i++) {
			final Icon icon=(iconURL[i]!=null)?new ImageIcon(iconURL[i]):null;
			buttonPanel.add(button=new JButton(icon));
			if (hint!=null && hint.length>i && hint[i]!=null) button.setToolTipText(hint[i]);
			button.setPreferredSize(new Dimension(24,24));
			button.addActionListener(listener[i]);
		}
		return p;
	}

	/**
	 * Erstellt ein Panel mit reduziertem Rahmenabstand aus einem Text (mit optionalem Icon) und mehreren Schaltfläche
	 * @param labelIcon	Icon für Beschriftung (kann <code>null</code> sein)
	 * @param title	Beschriftung
	 * @param icons	Icons der Schaltflächen
	 * @param hint	Tooltiptexts für die Schaltflächen (kann ganz oder teilweise <code>null</code> sein)
	 * @param listener	Callbacks die beim Anklicken der Schaltflächen aufgerufen werden sollen
	 * @return	Panel aus Text und Schaltflächen
	 */
	protected final JPanel makeEditPanelSmallBorderIcon(final Icon labelIcon, final String title, final Icon[] icons, final String[] hint, final ActionListener[] listener) {
		JPanel p=new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		p.setBackground(Color.WHITE);
		final JLabel label=new JLabel(title);
		if (labelIcon!=null) label.setIcon(labelIcon);
		p.add(label,BorderLayout.CENTER);
		JButton button;
		JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		buttonPanel.setBackground(new Color(255,255,255,0));

		p.add(buttonPanel,BorderLayout.EAST);
		for (int i=0;i<Math.min(icons.length,listener.length);i++) {
			buttonPanel.add(button=new JButton(icons[i]));
			if (hint!=null && hint.length>i && hint[i]!=null) button.setToolTipText(hint[i]);
			button.setPreferredSize(new Dimension(24,24));
			button.addActionListener(listener[i]);
		}
		return p;
	}

	/**
	 * Erstellt ein leeres Panel
	 * @return	Leeres Panel
	 */
	protected final JPanel makeEmptyPanel() {
		JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createEmptyBorder());
		p.setBackground(Color.WHITE);
		return p;
	}
}