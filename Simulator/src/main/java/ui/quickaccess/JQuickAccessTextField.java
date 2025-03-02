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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import ui.images.Images;

/**
 * Textfeld mit Platzhalter und Popupmenü gemäß der Eingabe.
 * @author Alexander Herzog
 */
public abstract class JQuickAccessTextField extends JPlaceholderTextField {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8844775209475362163L;

	/**
	 * Art des QuickAccess-Popupmenüs
	 * @see PopupMode
	 */
	private final PopupMode popupMode;

	/**
	 * Hintergrundverarbeitung für die Eingaben.
	 * @see #process()
	 */
	private ExecutorService executor;

	/**
	 * Zuletzt eingegebener Suchtext
	 */
	private String lastText;

	/**
	 * Zuvor generiertes Popupmenü<br>
	 * (Wird beim Öffnen eines neuen Popupmenüs geschlossen.)
	 * @see #closePopup()
	 */
	private JPopupMenu lastMenu;

	/**
	 * Art des QuickAccess-Popupmenüs
	 * @author Alexander Herzog
	 */
	public enum PopupMode {
		/**
		 * Anzeige als Tooltip-Panel (platzsparend, keine Tastatursteuerung)
		 */
		PANEL,

		/**
		 * Anzeige als normales Popupmenü (verbraucht mehr Platz, Tastatursteuerung möglich)
		 */
		DIRECT
	}

	/**
	 * Konstruktor der Klasse
	 * @param columns	Breite des Eingabefeldes
	 * @param placeholder	Platzhalter Text, der angezeigt wird, wenn das Feld leer ist
	 * @param popupMode	Wie soll das Popupmenü aufgebaut bzw. dargestellt werden?
	 * @see PopupMode
	 */
	public JQuickAccessTextField(final int columns, final String placeholder, final PopupMode popupMode) {
		super(columns);
		this.popupMode=popupMode;

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (lastMenu!=null && lastMenu.isVisible()) {if (processKey(e)) return;}
				if (e.getKeyCode()!=KeyEvent.VK_ESCAPE) {process(); return;}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {if (SwingUtilities.isLeftMouseButton(e)) process();}
		});

		setPlaceholder(placeholder);

		setMaximumSize(new Dimension(500,1000));
		SwingUtilities.invokeLater(()->{
			final Container c=getParent();
			if (!(c instanceof JMenuBar)) return;
			final Dimension dimension=new Dimension(getWidth(),c.getSize().height-4);
			setMaximumSize(dimension);
			setSize(dimension);
		});
	}

	/**
	 * Liefert die Treffer zu einem Suchbegriff
	 * @param text	Suchbegriff
	 * @return	Liste der Treffer (kann auch <code>null</code> sein)
	 */
	private List<JQuickAccessRecord> getPopupRecords(String text) {
		if (text==null) return null;
		text=text.trim();
		if (text.isEmpty()) return null;

		/* Liste der Ergebnisse abrufen */
		final List<JQuickAccessRecord> results=getQuickAccessRecords(text);
		if (results.isEmpty()) return null;

		/* Gruppieren */
		final Map<String,List<JQuickAccessRecord>> map=new HashMap<>();
		results.stream().forEach(record->{
			List<JQuickAccessRecord> list=map.get(record.category);
			if (list==null) map.put(record.category,list=new ArrayList<>());
			list.add(record);
		});

		/* Darzustellende Liste zusammenstellen */
		final String[] groups=map.keySet().stream().sorted().toArray(String[]::new);
		final List<JQuickAccessRecord> data=new ArrayList<>();
		for (String group: groups) {
			final List<JQuickAccessRecord> records=map.get(group);
			if (records==null || records.isEmpty()) continue;
			data.add(new JQuickAccessRecord(group,null,null,null,null));
			data.addAll(records);
		}

		return data;
	}

	/**
	 * Generiert auf Basis der Suchtreffer ein Popupmenü.
	 * @param data	Liste der Suchtreffer
	 * @return	Neues Popupmenü
	 * @see PopupMode#PANEL
	 */
	private JPopupMenu getPopupWithPanel(final List<JQuickAccessRecord> data) {
		/* Panel mit den Einträgen */
		final JList<JQuickAccessRecord> list=new JList<>(data.toArray(JQuickAccessRecord[]::new));
		list.setCellRenderer(new QuickAccessListCellRenderer());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final JQuickAccessRecord record=list.getSelectedValue();
				if (record==null || record.text==null) return;
				if (lastMenu!=null) {
					lastMenu.setVisible(false);
					lastMenu=null;
				}
				record.runAction();
			}
		});
		list.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e){
				final JQuickAccessRecord record=list.getModel().getElementAt(list.locationToIndex(e.getPoint()));
				if (record==null || record.text==null || record.action==null) {
					list.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} else {
					list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}
		});
		list.setBackground(SystemColor.info);
		list.setVisibleRowCount(Math.min(20,data.size()));
		final int w=list.getPreferredSize().width;
		list.setPrototypeCellValue(new JQuickAccessRecord("cat","text","textDisplay","tooltip",Images.MODEL.getIcon(),null));
		list.setFixedCellWidth(w);
		final JScrollPane scroll=new JScrollPane(list);
		scroll.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scroll.setBackground(SystemColor.info);

		/* Popupmenü */
		final JPopupMenu menu=new JPopupMenu();
		menu.add(scroll);
		menu.setBackground(SystemColor.info);

		return menu;
	}

	/**
	 * Generiert auf Basis der Suchtreffer ein Popupmenü.
	 * @param data	Liste der Suchtreffer
	 * @return	Neues Popupmenü
	 * @see PopupMode#DIRECT
	 */
	private JPopupMenu getPopupDirect(final List<JQuickAccessRecord> data) {
		final JPopupMenu menu=new JPopupMenu();

		for (JQuickAccessRecord record: data) {
			if (record.textDisplay==null) {
				if (menu.getComponentCount()>0) menu.addSeparator();
				final JMenuItem category=new JMenuItem("<html><body><b>"+record.category+"</b></body></html>");
				category.setEnabled(false);
				menu.add(category);
				continue;
			}

			final JMenuItem item=new JMenuItem(record.textDisplay,record.icon);
			item.addActionListener(e->record.action.accept(record));
			item.setToolTipText(record.tooltip);
			menu.add(item);
		}

		return menu;
	}

	/**
	 * Zuletzt gestartetes Verarbeitungsobjekt<br>
	 * (Wird abgebrochen, wenn während es noch arbeitet eine neue Eingabe erfolgt.)
	 * @see #process()
	 */
	private QuickAccessRunner lastRunner;

	/**
	 * Schließt das aktuelle Popupmenü.
	 * @see #lastMenu
	 */
	private void closePopup() {
		if (lastMenu==null) return;
		lastMenu.setVisible(false);
		lastMenu=null;
	}

	/**
	 * Reagiert auf einen Tastendruck in der Eingabezeile.
	 * @param e	Tastendruck-Ereignis
	 * @return	Wurde das Ereignis verarbeitet?
	 */
	private boolean processKey(final KeyEvent e) {
		if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
			closePopup();
			return true;
		}

		if (e.getKeyCode()==KeyEvent.VK_DOWN || e.getKeyCode()==KeyEvent.VK_UP) {
			lastMenu.requestFocus();
			return true;
		}

		if (e.getKeyCode()==KeyEvent.VK_ENTER) {
			processEnter();
			return true;
		}

		return false;
	}

	/**
	 * Stößt die Verarbeitung des eingegebenen Textes an.
	 */
	private void process() {
		if (lastText!=null && getText().equals(lastText) && lastMenu!=null && lastMenu.isVisible()) return;
		lastText=getText();

		if (lastRunner!=null) {
			lastRunner.cancel();
			lastRunner=null;
		}
		lastRunner=new QuickAccessRunner(lastText);

		if (executor==null) {
			final int coreCount=Runtime.getRuntime().availableProcessors();
			executor=new ThreadPoolExecutor(coreCount,coreCount,2,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),(ThreadFactory)r->new Thread(r,"QuickAccess"));
			((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);
		}
		executor.execute(lastRunner);
	}

	/**
	 * Reagiert auf einen Enter-Tastendruck in der Eingabezeile oder im Menü.
	 * @see #process()
	 * @see QuickAccessRunner#run()
	 */
	private void processEnter() {
		final MenuElement[] path=MenuSelectionManager.defaultManager().getSelectedPath();
		if (path!=null && path.length>0 && path[path.length-1] instanceof JMenuItem) {
			processMenuItemAction((JMenuItem)path[path.length-1]);
		} else {
			if (lastMenu==null || !lastMenu.isVisible()) return;
			if (lastMenu.getComponentCount()!=2) return;
			if (!(lastMenu.getComponent(1) instanceof JMenuItem)) return;
			processMenuItemAction((JMenuItem)lastMenu.getComponent(1));
		}
	}

	/**
	 * Löst die Klick-Aktionen für einen Menüpunkt aus
	 * @param item	Menüpunkt dessen {@link ActionListener} ausgelöst werden sollen
	 * @see #processEnter()
	 */
	private void processMenuItemAction(final JMenuItem item) {
		if (item==null) return;
		final ActionEvent event=new ActionEvent(lastMenu,1,"click");
		SwingUtilities.invokeLater(()->{
			for (ActionListener listener: item.getActionListeners()) listener.actionPerformed(event);
		});
		closePopup();
	}

	/**
	 * Verarbeitet eine Sucheingabe.
	 * @see JQuickAccessTextField#process()
	 */
	private class QuickAccessRunner implements Runnable {
		/** Eingegebener Suchbegriff */
		private final String text;
		/** Wurde die Suche abgebrochen? */
		private volatile boolean canceled;

		/**
		 * Konstruktor der Klasse
		 * @param text	Eingegebener Suchbegriff
		 */
		public QuickAccessRunner(final String text) {
			this.text=text;
			canceled=false;
		}

		/**
		 * Bricht die aktuelle Suche ab.
		 */
		public void cancel() {
			canceled=true;
		}

		@Override
		public void run() {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				if (canceled) return;
			}

			final List<JQuickAccessRecord> data=getPopupRecords(text);

			final JPopupMenu menu;
			if (data==null) {
				menu=null;
			} else {
				switch (popupMode) {
				case DIRECT: menu=getPopupDirect(data); break;
				case PANEL: menu=getPopupWithPanel(data); break;
				default: menu=null; break;
				}
			}

			if (canceled) return;
			if (menu==null) {closePopup(); return;}


			/* Popupmenü */
			menu.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ENTER) processEnter();
				}
			});
			synchronized (JQuickAccessTextField.this) {
				if (popupMode==PopupMode.PANEL) menu.setFocusable(false);
				menu.show(JQuickAccessTextField.this,0,getHeight());
				if (popupMode==PopupMode.PANEL) menu.setFocusable(true);
				closePopup();
				requestFocus();
				lastMenu=menu;
			}
		}
	}

	/**
	 * Stellt die Einträge für das QuickAccess-Menü zusammen
	 * @param quickAccessText	Eingegebener Text
	 * @return	Liste mit den Treffern
	 */
	public abstract List<JQuickAccessRecord> getQuickAccessRecords(final String quickAccessText);

	/**
	 * Renderer für die Anzeige der Einträge des in {@link JQuickAccessTextField#getPopupWithPanel(List)}
	 * generierten Popupmenüs.
	 * @see JQuickAccessTextField#getPopupWithPanel(List)
	 */
	private static class QuickAccessListCellRenderer extends JLabel implements ListCellRenderer<JQuickAccessRecord> {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 2375121310093542345L;

		/**
		 * Konstruktor der Klasse
		 */
		public QuickAccessListCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JQuickAccessRecord> list, JQuickAccessRecord value, int index, boolean isSelected, boolean cellHasFocus) {
			setIcon(null);
			setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
			setToolTipText(value.tooltip);

			if (value.text==null) {
				setText("<html><body><span style='color: gray;'><b><u>"+value.category+"</b></u></span></body></html>");
			} else {
				if (value.icon!=null) setIcon(value.icon);
				setText(value.textDisplay);
			}

			return this;
		}
	}
}
