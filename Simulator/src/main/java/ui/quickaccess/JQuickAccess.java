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
package ui.quickaccess;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.im.InputContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import tools.SetupData;
import ui.help.BookData;
import ui.tools.InputContextFix;

/**
 * Zusammenstellung der Schnellzugriffs-Funktionen
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 * @see JQuickAccessTextField
 */
public class JQuickAccess {
	/**
	 * Liste aller verfügbaren QuickAccess-Builder (aktiv und deaktiviert).
	 */
	private List<JQuickAccessBuilder> list;

	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText Eingegebener Text (darf <code>null</code> sein)
	 */
	public JQuickAccess(final String quickAccessText) {
		list=new ArrayList<>();

		list.add(new JQuickAccessBuilderNewElements(quickAccessText));
		list.add(new JQuickAccessBuilderElementsList(quickAccessText));
		list.add(new JQuickAccessBuilderMenu(quickAccessText));
		list.add(new JQuickAccessBuilderLastFiles(quickAccessText));
		list.add(new JQuickAccessBuilderStatistics(quickAccessText));
		list.add(new JQuickAccessBuilderModelProperties(quickAccessText));
		list.add(new JQuickAccessBuilderExamples(quickAccessText));
		list.add(new JQuickAccessBuilderCalc(quickAccessText));
		list.add(new JQuickAccessBuilderDistributions(quickAccessText));
		list.add(new JQuickAccessBuilderSettings(quickAccessText));
		if (BookData.getInstance().isDataAvailable()) list.add(new JQuickAccessBuilderBook(quickAccessText));
	}

	/**
	 * Konstruktor der Klasse
	 */
	public JQuickAccess() {
		this(null);
	}

	/**
	 * Liefert die Anzahl der verfügbaren QuickAccess-Builder (aktiv und deaktiviert).
	 * @return Anzahl der verfügbaren QuickAccess-Builder
	 */
	public int getCount() {
		return list.size();
	}

	/**
	 * Liefert die Namen alle verfügbaren QuickAccess-Builder (aktiv und deaktiviert).
	 * @return Namen der verfügbaren QuickAccess-Builder
	 */
	public String[] getNames() {
		return list.stream().map(quick->quick.getCategory()).toArray(String[]::new);
	}

	/**
	 * Zeigt ein Popup-Menü zur Auswahl der aktiven QuickAccess-Builder an.
	 * @param parent Übergeordnetes Element an dem das Popup-Menü ausgerichtet werden soll
	 */
	public void showPopup(final Component parent) {
		final SetupData setup=SetupData.getSetup();
		final String[] names=getNames();

		/* Einstellungen laden */
		String filter=setup.quickAccessFilter;
		if (filter==null) filter="";
		while (filter.length()<names.length) filter+="X";
		int countActive=0;
		for (int i=0;i<filter.length();i++) if (filter.charAt(i)!='-') countActive++;
		if (countActive==0) {
			countActive=names.length;
			final StringBuilder sb=new StringBuilder();
			for (int i=0;i<names.length;i++) sb.append('X');
			filter=sb.toString();
		}

		/* Menü erstellen */
		final JPopupMenu menu=new JPopupMenu();

		for (int i=0;i<names.length;i++) {
			final JCheckBoxMenuItem item=new JCheckBoxMenuItem(names[i],filter.charAt(i)!='-');
			menu.add(item);
			item.setEnabled(!item.isSelected() || countActive>0);
			final int nr=i;
			final String currentFilter=filter;
			item.addActionListener(ev-> {
				final boolean selected=((JCheckBoxMenuItem)ev.getSource()).isSelected();
				StringBuilder sb=new StringBuilder();
				for (int j=0;j<names.length;j++) if (j==nr) sb.append(selected?'X':'-');
				else sb.append(currentFilter.charAt(j));
				setup.quickAccessFilter=sb.toString();
				setup.saveSetup();
			});
		}

		/* Menü anzeigen */
		menu.show(parent,0,parent.getHeight());
	}

	/**
	 * Legt ein QuickAccess-Eingabefeld ein an.
	 * @param quickAccessProcessor Callback, das die Verarbeitung der Eingaben durchführen soll
	 * @param compactMode	Soll das Eingabefeld die normale Breite (<code>false</code>) oder in einer schmalen Version (<code>true</code>) gezeichnet werden?
	 * @return Neues QuickAccess-Eingabefeld
	 */
	public static JQuickAccessTextField buildQuickAccessField(final Function<String,List<JQuickAccessRecord>> quickAccessProcessor, final boolean compactMode) {
		final KeyStroke ctrlE=KeyStroke.getKeyStroke('E',InputEvent.CTRL_DOWN_MASK);
		final int modifiers=ctrlE.getModifiers();
		String acceleratorText=(modifiers==0)?"":InputEvent.getModifiersExText(modifiers)+"+";
		acceleratorText+=KeyEvent.getKeyText(ctrlE.getKeyCode());
		final JQuickAccessTextField quickAccessTextField=new JQuickAccessTextField(compactMode?9:14,Language.tr("QuickAccess")+(compactMode?"":(" ("+acceleratorText+")")),JQuickAccessTextField.PopupMode.DIRECT) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID=2746691534913517513L;

			@Override
			public List<JQuickAccessRecord> getQuickAccessRecords(final String quickAccessText) {
				return quickAccessProcessor.apply(quickAccessText);
			}

			@Override
			public InputContext getInputContext() {
				return new InputContextFix(super.getInputContext());
			}
		};
		quickAccessTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final JQuickAccess quickAccess=new JQuickAccess();
					quickAccess.showPopup((Component)e.getSource());
					e.consume();
				}
			}
		});
		quickAccessTextField.setToolTipText(Language.tr("QuickAccess.Hint")+(compactMode?(" ("+acceleratorText+")"):""));

		return quickAccessTextField;
	}

	/**
	 * Liefert die aktivierten QuickAccess-Builder (mit dem im Konstruktor angegebenen Suchbegriff)
	 * @return Liste der aktiven QuickAccess-Builder
	 */
	public List<JQuickAccessBuilder> getActiveQuickAccessBuilders() {
		final SetupData setup=SetupData.getSetup();
		final List<JQuickAccessBuilder> results=new ArrayList<>();

		/* Einstellungen laden */
		String filter=setup.quickAccessFilter;
		if (filter==null) filter="";
		while (filter.length()<list.size()) filter+="X";
		int countActive=0;
		for (int i=0;i<filter.length();i++) if (filter.charAt(i)!='-') countActive++;
		if (countActive==0) {
			final StringBuilder sb=new StringBuilder();
			for (int i=0;i<list.size();i++) sb.append('X');
			filter=sb.toString();
		}

		for (int i=0;i<list.size();i++) if (filter.charAt(i)!='-') results.add(list.get(i));

		return results;
	}

	/**
	 * Konfiguriert ein Textfeld so, dass es je nach dem, ob es selektiert ist oder nicht,
	 * verschiedene Größen erhält.
	 * @param textField	Zu konfigurierendes Textfeld
	 * @param sizeSmall	Größe im nicht-selektierten Fall (in Spalten)
	 * @param sizeLarge	Größe im selektierten Fall (in Spalten)
	 */
	public static void textFieldResizer(final JTextField textField, final int sizeSmall, final int sizeLarge) {
		textField.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {setTextFieldSize(textField,sizeSmall);}
			@Override public void focusGained(FocusEvent e) {setTextFieldSize(textField,sizeLarge);}
		});
		textField.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {setTextFieldSize(textField,sizeLarge);}
		});
		textField.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {setTextFieldSize(textField,sizeLarge);}
		});

		setTextFieldSize(textField,sizeSmall);
	}

	/**
	 * Stellt die Größe eines Textfeldes ein und sorgt dafür,
	 * dass es in der neuen Größe sichtbar wird.
	 * @param textField	Zu konfigurierendes Textfeld
	 * @param size	Neue Größe (in Spalten)
	 */
	private static void setTextFieldSize(final JTextField textField, final int size) {
		if (textField.getColumns()==size) return;
		textField.setColumns(size);
		textField.revalidate();
		final Container c=textField.getParent();
		if (c!=null) c.revalidate();
	}

	/**
	 * Liefert eine Fokussierreihenfolge, die das QuickAccess-Feld nicht enthält,
	 * so dass es nicht ständig ausversehen den Fokus erhält.
	 * @return	Fokussierreihenfolge für die Menüzeile
	 */
	public static FocusTraversalPolicy getMenuTraversalPolicy() {
		return new FocusTraversalPolicy() {
			@Override
			public Component getLastComponent(Container aContainer) {
				int nr=aContainer.getComponentCount();
				while (nr>=0) {
					final Component component=aContainer.getComponent(nr);
					if (!(component instanceof JTextField)) return component;
					nr--;
				}
				return null;
			}

			@Override
			public Component getFirstComponent(Container aContainer) {
				final int count=aContainer.getComponentCount();
				for (int i=0;i<count;i++) {
					final Component component=aContainer.getComponent(i);
					if (!(component instanceof JTextField)) return component;
				}
				return null;
			}

			@Override
			public Component getDefaultComponent(Container aContainer) {
				return getFirstComponent(aContainer);
			}

			private int getIndex(Container aContainer, Component aComponent) {
				final int count=aContainer.getComponentCount();
				for (int i=0;i<count;i++) if (aContainer.getComponent(i)==aComponent) return i;
				return -1;
			}

			@Override
			public Component getComponentBefore(Container aContainer, Component aComponent) {
				final int index=getIndex(aContainer,aComponent);
				if (index<0) return null;

				final int count=aContainer.getComponentCount();
				int nr=index-1;
				if (nr<0) nr=count-1;
				while (nr!=index) {
					final Component component=aContainer.getComponent(nr);
					if (!(component instanceof JTextField)) return component;
					nr--;
					if (nr<0) nr=count-1;
				}
				return null;
			}

			@Override
			public Component getComponentAfter(Container aContainer, Component aComponent) {
				final int index=getIndex(aContainer,aComponent);
				if (index<0) return null;

				final int count=aContainer.getComponentCount();
				int nr=index+1;
				if (nr>=count) nr=0;
				while (nr!=index) {
					final Component component=aContainer.getComponent(nr);
					if (!(component instanceof JTextField)) return component;
					nr++;
					if (nr>=count) nr=0;
				}
				return null;
			}
		};
	}
}
